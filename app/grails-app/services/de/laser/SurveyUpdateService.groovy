package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.SurveyInfo
import com.k_int.kbplus.SurveyOrg
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import grails.transaction.Transactional
import grails.util.Holders

import static de.laser.helper.RDStore.getYN_NO
import static de.laser.helper.RDStore.getYN_YES

@Transactional
class SurveyUpdateService extends AbstractLockableService {

    def grailsApplication
    def mailService
    def messageSource
    Locale locale
    String from
    String replyTo


    @javax.annotation.PostConstruct
    void init() {
        from = grailsApplication.config.notifications.email.from
        //replyTo = grailsApplication.config.notifications.email.replyTo
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        log.debug("Initialised SurveyUpdateService...")
    }

    boolean surveyCheck() {
        if(!running) {
            running = true
            def currentDate = new Date(System.currentTimeMillis())

            // Ready -> Started
            def readySurveysIds = SurveyInfo.where {
                status == RDStore.SURVEY_READY && startDate <= currentDate
            }.collect { it.id }

            log.info("surveyCheck (Ready to Started) readySurveysIds: " + readySurveysIds)

            if (readySurveysIds) {

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_STARTED, ids: readySurveysIds]
                )
                //
                emailsToSurveyUsers(readySurveysIds)

            }

            // Started -> Completed

            def startedSurveyIds = SurveyInfo.where {
                (status == RDStore.SURVEY_SURVEY_STARTED) && (startDate < currentDate) && (endDate != null && endDate < currentDate)
            }.collect { it.id }

            log.info("surveyCheck (Started to Completed) startedSurveyIds: " + startedSurveyIds)

            if (startedSurveyIds) {

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_COMPLETED, ids: startedSurveyIds]
                )
            }
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
                if(userOrg?.user?.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                        userOrg?.user?.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    def orgSurveys = SurveyInfo.executeQuery("SELECT s FROM SurveyInfo s " +
                            "LEFT JOIN s.surveyConfigs surConf " +
                            "LEFT JOIN surConf.orgs surOrg  " +
                            "WHERE surOrg.org IN (:org) " +
                            "AND s.id IN (:survey)", [org: userOrg?.org, survey: surveys?.id])

                    sendEmail(userOrg?.user, userOrg?.org, orgSurveys)
                }
            }

        }

    }

    private void sendEmail(User user, Org org, List<SurveyInfo> surveyEntries) {
        def emailReceiver = user.getEmail()
        def currentServer = grailsApplication.config.getCurrentServer()
        def subjectSystemPraefix = (currentServer == ContextService.SERVER_PROD)? "LAS:eR - " : (grailsApplication.config.laserSystemId + " - ")
        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.surveys', null, locale) + " (" + org.name + ")"

        surveyEntries.each {survey ->
            try {
                if (emailReceiver == null || emailReceiver.isEmpty()) {
                    log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                } else if (surveyEntries == null || surveyEntries.isEmpty()) {
                    log.debug("The user has no due dates, so no email will be sent (" + user.username + "/"+ org.name + ")");
                } else {
                    boolean isNotificationCCbyEmail = user.getSettingsValue(UserSettings.KEYS.IS_NOTIFICATION_CC_BY_EMAIL)?.rdValue == RDStore.YN_YES
                    String ccAddress = null
                    if (isNotificationCCbyEmail){
                        ccAddress = user.getSettingsValue(UserSettings.KEYS.NOTIFICATION_CC_EMAILADDRESS)?.getValue()
                    }

                    List generalContactsEMails = []

                    survey?.owner?.getGeneralContactPersons(false)?.each { person ->
                        person?.contacts?.each { contact ->
                            if (['Mail', 'E-Mail'].contains(contact?.contentType?.value))
                            {
                                generalContactsEMails << contact?.content
                            }
                        }
                    }

                    replyTo = generalContactsEMails.size() > 1 ? generalContactsEMails.join(";") : (generalContactsEMails[0].toString() ?: null)


                    if (isNotificationCCbyEmail && ccAddress) {
                        mailService.sendMail {
                            to      emailReceiver
                            from    from
                            cc      ccAddress
                            replyTo replyTo
                            subject mailSubject
                            body    (view: "/mailTemplates/html/notificationSurvey", model: [user: user, org: org, survey: survey])
                        }
                    } else {
                        mailService.sendMail {
                            to      emailReceiver
                            from from
                            replyTo replyTo
                            subject mailSubject
                            body    (view: "/mailTemplates/html/notificationSurvey", model: [user: user, org: org, survey: survey])
                        }
                    }

                    log.debug("SurveyUpdateService - finished sendEmail() to " + user.displayName + " (" + user.email + ") " + org.name);
                }
            } catch (Exception e) {
                String eMsg = e.message

                log.error("SurveyUpdateService - sendEmail() :: Unable to perform email due to exception ${eMsg}")
                SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', ['error': eMsg])
            }
        }
    }

}
