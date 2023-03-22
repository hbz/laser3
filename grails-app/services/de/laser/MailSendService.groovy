package de.laser

import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.config.ConfigMapper
import de.laser.properties.PropertyDefinition
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.system.SystemAnnouncement
import de.laser.system.SystemEvent
import de.laser.utils.AppUtils
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

@Transactional
class MailSendService {

    MailService mailService
    EscapeService escapeService
    MessageSource messageSource
    AccessService accessService

    String from

    /**
     * Constructor method
     */
    @javax.annotation.PostConstruct
    void init() {
        from = ConfigMapper.getNotificationsEmailFrom()
        messageSource = BeanStore.getMessageSource()
    }

    /**
     * Sends a mail about the survey to the given user of the given institution about the given surveys
     * @param user the user to be notified
     * @param org the institution of the user
     * @param surveyEntries the survey information to process
     * @param reminderMail is it a reminder?
     */
    void sendSurveyEmail(User user, Org org, List<SurveyInfo> surveyEntries, boolean reminderMail) {

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.debug 'SurveyService.sendSurveyEmail() failed due grails.mail.disabled = true'
        }else {

            String replyTo

            String emailReceiver = user.getEmail()
            String currentServer = AppUtils.getCurrentServer()
            String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "LAS:eR - " : (ConfigMapper.getLaserSystemId() + " - ")

            surveyEntries.each { survey ->
                try {
                    if (emailReceiver == null || emailReceiver.isEmpty()) {
                        log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                    } else {
                        boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                        String ccAddress = null
                        if (isNotificationCCbyEmail) {
                            ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                        }

                        List generalContactsEMails = []

                        survey.owner.getGeneralContactPersons(true)?.each { person ->
                            person.contacts.each { contact ->
                                if (['Mail', 'E-Mail'].contains(contact.contentType?.value)) {
                                    generalContactsEMails << contact.content
                                }
                            }
                        }

                        replyTo = (generalContactsEMails.size() > 0) ? generalContactsEMails[0].toString() : null
                        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
                        Object[] args = ["${survey.type.getI10n('value', language)}"]
                        String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + (reminderMail ? messageSource.getMessage('email.subject.surveysReminder', args, language)  : messageSource.getMessage('email.subject.surveys', args, language)) + " " + survey.name + "")

                        if (isNotificationCCbyEmail && ccAddress) {
                            mailService.sendMail {
                                multipart true
                                to emailReceiver
                                from from
                                cc ccAddress
                                replyTo replyTo
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        } else {
                            mailService.sendMail {
                                multipart true
                                to emailReceiver
                                from from
                                replyTo replyTo
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        }

                        log.debug("SurveyService - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + org.name);
                    }
                } catch (Exception e) {
                    String eMsg = e.message

                    log.error("SurveyService - sendSurveyEmail() :: Unable to perform email due to exception ${eMsg}")
                    SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: org.name, survey: survey.name])
                }
            }
        }
    }

    /**
     * Sends an email to the survey participant as confirmation that the given participant finished the survey
     * @param surveyInfo the survey which has been finished
     * @param participationFinish the participant who finished the survey
     */
    def emailToSurveyParticipationByFinish(SurveyInfo surveyInfo, Org participationFinish){

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.debug 'surveyService.emailToSurveyParticipationByFinish() failed due grails.mail.disabled = true'
            return false
        }

        if(surveyInfo.owner)
        {
            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrg(participationFinish)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    User user = userOrg.user
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
                    String emailReceiver = user.getEmail()
                    String currentServer = AppUtils.getCurrentServer()
                    String subjectSystemPraefix = (currentServer == AppUtils.PROD)? "" : (ConfigMapper.getLaserSystemId() + " - ")

                    String subjectText
                    Object[] args = [surveyInfo.name]
                    if(surveyInfo.type.id == RDStore.SURVEY_TYPE_RENEWAL.id){
                        subjectText = messageSource.getMessage('email.survey.participation.finish.renewal.subject', args, language)
                    }else if(surveyInfo.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id){
                        subjectText = messageSource.getMessage('email.survey.participation.finish.subscriptionSurvey.subject', args, language)
                    }else {
                        subjectText = messageSource.getMessage('email.survey.participation.finish.subject', args, language)
                    }

                    String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + subjectText)

                    List generalContactsEMails = []

                    surveyInfo.owner.getGeneralContactPersons(true)?.each { person ->
                        person.contacts.each { contact ->
                            if (['Mail', 'E-Mail'].contains(contact.contentType?.value)) {
                                generalContactsEMails << contact.content
                            }
                        }
                    }

                    try {
                        if (emailReceiver == null || emailReceiver.isEmpty()) {
                            log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                        } else {
                            boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                            String ccAddress = null
                            if (isNotificationCCbyEmail){
                                ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                            }

                            List surveyResults = []

                            surveyInfo.surveyConfigs[0].getSortedSurveyProperties().each{ PropertyDefinition propertyDefinition ->
                                surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participationFinish, surveyInfo.surveyConfigs[0], propertyDefinition)
                            }

                            if (isNotificationCCbyEmail && ccAddress) {
                                mailService.sendMail {
                                    to      emailReceiver
                                    from    from
                                    cc      ccAddress
                                    subject mailSubject
                                    html    (view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
                                }
                            } else {
                                mailService.sendMail {
                                    to      emailReceiver
                                    from from
                                    subject mailSubject
                                    html    (view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
                                }
                            }

                            log.debug("emailToSurveyParticipationByFinish - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + participationFinish.name);
                        }
                    } catch (Exception e) {
                        String eMsg = e.message

                        log.error("emailToSurveyParticipationByFinish - sendSurveyEmail() :: Unable to perform email due to exception ${eMsg}")
                        SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: participationFinish.name, survey: surveyInfo.name])
                    }
                }
            }
        }
    }

    /**
     * Sends an email to the survey owner that the given participant finished the survey
     * @param surveyInfo the survey which has been finished
     * @param participationFinish the participant who finished the survey
     */
    def emailToSurveyOwnerbyParticipationFinish(SurveyInfo surveyInfo, Org participationFinish){

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.debug 'surveyService.emailToSurveyOwnerbyParticipationFinish() failed due grails.mail.disabled = true'
            return false
        }

        if(surveyInfo.owner)
        {
            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrg(surveyInfo.owner)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    User user = userOrg.user
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
                    String emailReceiver = user.getEmail()
                    String currentServer = AppUtils.getCurrentServer()
                    String subjectSystemPraefix = (currentServer == AppUtils.PROD)? "" : (ConfigMapper.getLaserSystemId() + " - ")
                    String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + surveyInfo.type.getI10n('value', language) + ": " + surveyInfo.name +  " (" + participationFinish.sortname + ")")

                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyInfo.surveyConfigs[0], participationFinish)
                    if(surveyOrg && surveyOrg.orgInsertedItself) {
                        mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + " " +messageSource.getMessage('default.new', null, language) + " " +surveyInfo.type.getI10n('value', language) + ": " + surveyInfo.name + " (" + participationFinish.sortname + ")")
                    }

                    try {
                        if (emailReceiver == null || emailReceiver.isEmpty()) {
                            log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                        } else {
                            boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                            String ccAddress = null
                            if (isNotificationCCbyEmail){
                                ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                            }

                            List surveyResults = []

                            surveyInfo.surveyConfigs[0].getSortedSurveyProperties().each{ PropertyDefinition propertyDefinition ->
                                surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participationFinish, surveyInfo.surveyConfigs[0], propertyDefinition)
                            }

                            if (isNotificationCCbyEmail && ccAddress) {
                                mailService.sendMail {
                                    to      emailReceiver
                                    from    from
                                    cc      ccAddress
                                    subject mailSubject
                                    html    (view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                }
                            } else {
                                mailService.sendMail {
                                    to      emailReceiver
                                    from from
                                    subject mailSubject
                                    html    (view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                }
                            }

                            log.debug("emailToSurveyOwnerbyParticipationFinish - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + surveyInfo.owner.name);
                        }
                    } catch (Exception e) {
                        String eMsg = e.message

                        log.error("emailToSurveyOwnerbyParticipationFinish - sendSurveyEmail() :: Unable to perform email due to exception ${eMsg}")
                        SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: participationFinish.name, survey: surveyInfo.name])
                    }
                }
            }
        }
    }

    /**
     * Sends a mail to a given user. The system announcement is being included in a mail template
     * @param user the {@link User} to be notified
     * @throws Exception
     */
    void sendSystemAnnouncementMail(User user, SystemAnnouncement systemAnnouncement) throws Exception {

        MessageSource messageSource = BeanStore.getMessageSource()
        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())

        String currentServer = AppUtils.getCurrentServer()
        String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "LAS:eR - " : (ConfigMapper.getLaserSystemId() + " - ")
        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.sysAnnouncement', null, language)

        boolean isRemindCCbyEmail = user.getSetting(UserSetting.KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
        String ccAddress

        if (isRemindCCbyEmail){
            ccAddress = user.getSetting(UserSetting.KEYS.REMIND_CC_EMAILADDRESS, null)?.getValue()
            // println user.toString() + " : " + isRemindCCbyEmail + " : " + ccAddress
        }

        if (isRemindCCbyEmail && ccAddress) {
            mailService.sendMail {
                to      user.getEmail()
                from    ConfigMapper.getNotificationsEmailFrom()
                cc      ccAddress
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject mailSubject
                body    (view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: systemAnnouncement])
            }
        }
        else {
            mailService.sendMail {
                to      user.getEmail()
                from    ConfigMapper.getNotificationsEmailFrom()
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject mailSubject
                body    (view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: systemAnnouncement])
            }
        }
    }

    /**
     * Sends a mail to the given user
     * @param user the user to whom the mail should be sent
     * @param subj the subject of the mail
     * @param view the template of the mail body
     * @param model the parameters for the mail template
     */
    void sendMailToUser(User user, String subj, String view, Map model) {

        if (AppUtils.getCurrentServer() == AppUtils.LOCAL) {
            log.info "--- instAdmService.sendMail() --- IGNORED SENDING MAIL because of SERVER_LOCAL ---"
            return
        }

        model.serverURL = ConfigMapper.getGrailsServerURL()

        try {

            mailService.sendMail {
                to      user.email
                from    ConfigMapper.getNotificationsEmailFrom()
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject ConfigMapper.getLaserSystemId() + ' - ' + subj
                body    view: view, model: model
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception ${e.message}"
        }
    }
}
