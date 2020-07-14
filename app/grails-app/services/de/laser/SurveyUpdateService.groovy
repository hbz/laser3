package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SurveyInfo
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import de.laser.helper.ConfigUtils
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import de.laser.interfaces.AbstractLockableService
import grails.transaction.Transactional
import grails.util.Holders

@Transactional
class SurveyUpdateService extends AbstractLockableService {

    def grailsApplication
    def mailService
    EscapeService escapeService
    def messageSource
    String from
    String replyTo


    @javax.annotation.PostConstruct
    void init() {
        from = ConfigUtils.getNotificationsEmailFrom()
        //replyTo = ConfigUtils.getNotificationsEmailReplyTo()
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        log.debug("Initialised SurveyUpdateService...")
    }

    boolean surveyCheck() {
        if(!running) {
            running = true
            def currentDate = new Date(System.currentTimeMillis())

            Map<String,Object> updatedObjs = [:]

            // Ready -> Started
            List readySurveysIds = SurveyInfo.findAllByStatusAndStartDateLessThanEquals(RDStore.SURVEY_READY, currentDate).collect {it.id}

            log.info("surveyCheck (Ready to Started) readySurveysIds: " + readySurveysIds)

            if (readySurveysIds) {

                updatedObjs << ["readyToStarted (${readySurveysIds.size()})" : readySurveysIds]

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_STARTED, ids: readySurveysIds]
                )
                //
                emailsToSurveyUsers(readySurveysIds)

            }

            // Started -> Completed

            Set<Long> startedSurveyIds = SurveyInfo.executeQuery('select sur.id from SurveyInfo sur where sur.status = :status and sur.startDate < :currentDate and sur.endDate != null and sur.endDate < :currentDate',
                    [status: RDStore.SURVEY_SURVEY_STARTED,currentDate: currentDate])

            log.info("surveyCheck (Started to Completed) startedSurveyIds: " + startedSurveyIds)

            if (startedSurveyIds) {
                updatedObjs << ["StartedToCompleted (${startedSurveyIds.size()})" : startedSurveyIds]

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_COMPLETED, ids: startedSurveyIds]
                )
            }

            def oldStartedSurveyIds = SurveyInfo.where {
                (status == RDStore.SURVEY_SURVEY_STARTED) && (startDate < currentDate) && (endDate != null && endDate < currentDate)
            }.collect { it.id }

            if (oldStartedSurveyIds) {
                updatedObjs << ["old_StartedToCompleted (${oldStartedSurveyIds.size()})": oldStartedSurveyIds]
            }

            SystemEvent.createEvent('SURVEY_UPDATE_SERVICE_PROCESSING', updatedObjs)
            running = false
            return true
        }
        else {
            log.warn("Surveys already checked ... not starting again.")
            return false
        }
    }

    def emailsToSurveyUsers(List surveyInfoIds){

        def surveys = SurveyInfo.findAllByIdInList(surveyInfoIds)

        def orgs = surveys?.surveyConfigs?.orgs?.org?.flatten()

        if(orgs)
        {
            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrgInListAndStatus(orgs, 1)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    def orgSurveys = SurveyInfo.executeQuery("SELECT s FROM SurveyInfo s " +
                            "LEFT JOIN s.surveyConfigs surConf " +
                            "LEFT JOIN surConf.orgs surOrg  " +
                            "WHERE surOrg.org IN (:org) " +
                            "AND s.id IN (:survey)", [org: userOrg.org, survey: surveys?.id])

                    sendEmail(userOrg.user, userOrg.org, orgSurveys)
                }
            }

        }

    }

    def emailsToSurveyUsersOfOrg(SurveyInfo surveyInfo, Org org){

            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrgAndStatus(org, UserOrg.STATUS_APPROVED)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {
                    sendEmail(userOrg.user, userOrg.org, [surveyInfo])
                }
            }
    }

    private void sendEmail(User user, Org org, List<SurveyInfo> surveyEntries) {

        if (grailsApplication.config.grails.mail.disabled == true) {
            println 'surveyUpdateService.sendEmail() failed due grailsApplication.config.grails.mail.disabled = true'
        }else {

            String emailReceiver = user.getEmail()
            String currentServer = ServerUtils.getCurrentServer()
            String subjectSystemPraefix = (currentServer == ServerUtils.SERVER_PROD) ? "LAS:eR - " : (ConfigUtils.getLaserSystemId() + " - ")

            surveyEntries.each { survey ->
                try {
                    if (emailReceiver == null || emailReceiver.isEmpty()) {
                        log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                    } else {
                        boolean isNotificationCCbyEmail = user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                        String ccAddress = null
                        if (isNotificationCCbyEmail) {
                            ccAddress = user.getSetting(UserSettings.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                        }

                        List generalContactsEMails = []

                        survey.owner.getGeneralContactPersons(false)?.each { person ->
                            person.contacts.each { contact ->
                                if (['Mail', 'E-Mail'].contains(contact.contentType?.value)) {
                                    generalContactsEMails << contact.content
                                }
                            }
                        }

                        replyTo = generalContactsEMails.size() > 1 ? generalContactsEMails.join(";") : (generalContactsEMails[0].toString() ?: null)
                        Object[] args = ["${survey.type.getI10n('value')}"]
                        Locale language = new Locale(user.getSetting(UserSettings.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', de.laser.helper.RDConstants.LANGUAGE)).value.toString())

                        String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + messageSource.getMessage('email.subject.surveys', args, language) + " (" + org.name + ")")

                        if (isNotificationCCbyEmail && ccAddress) {
                            mailService.sendMail {
                                to emailReceiver
                                from from
                                cc ccAddress
                                replyTo replyTo
                                subject mailSubject
                                body(view: "/mailTemplates/text/notificationSurvey", model: [user: user, org: org, survey: survey])
                            }
                        } else {
                            mailService.sendMail {
                                to emailReceiver
                                from from
                                replyTo replyTo
                                subject mailSubject
                                body(view: "/mailTemplates/text/notificationSurvey", model: [user: user, org: org, survey: survey])
                            }
                        }

                        log.debug("SurveyUpdateService - finished sendEmail() to " + user.displayName + " (" + user.email + ") " + org.name);
                    }
                } catch (Exception e) {
                    String eMsg = e.message

                    log.error("SurveyUpdateService - sendEmail() :: Unable to perform email due to exception ${eMsg}")
                    SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: org.name, survey: survey.name])
                }
            }
        }
    }

}
