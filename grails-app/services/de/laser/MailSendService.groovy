package de.laser

import de.laser.auth.User
import de.laser.auth.UserOrgRole
import de.laser.config.ConfigMapper
import de.laser.mail.MailReport
import de.laser.properties.PropertyDefinition
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.system.SystemAnnouncement
import de.laser.system.SystemEvent
import de.laser.utils.AppUtils
import grails.gorm.transactions.Transactional
import grails.plugin.asyncmail.AsynchronousMailMessage
import grails.plugin.asyncmail.AsynchronousMailService
import grails.plugins.mail.MailService
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

import javax.servlet.http.HttpServletRequest

@Transactional
class MailSendService {

    MailService mailService
    EscapeService escapeService
    MessageSource messageSource
    AccessService accessService
    SurveyService surveyService
    SubscriptionService subscriptionService
    AsynchronousMailService asynchronousMailService

    String from

    String currentServer = AppUtils.getCurrentServer()
    String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "" : (ConfigMapper.getLaserSystemId() + " - ")

    /**
     * Constructor method
     */
    @javax.annotation.PostConstruct
    void init() {
        from = ConfigMapper.getNotificationsEmailFrom()
        messageSource = BeanStore.getMessageSource()
    }

    Map mailSendConfigBySurvey(SurveyInfo surveyInfo, boolean reminderMail) {
        Map<String, Object> result = [:]
        result.mailFrom = from
        result.mailSubject = ""
        result.mailText = ""

        Locale language = new Locale("de")

        result.mailSubject = subjectSystemPraefix
        if(reminderMail) {
            result.mailSubject = result.mailSubject + ' ' + messageSource.getMessage('email.subject.surveysReminder', language)
        }

        result.mailSubject = result.mailSubject + ' ' + surveyInfo.name + ' ('+surveyInfo.type.getI10n('value', language)+')'
        result.mailSubject = escapeService.replaceUmlaute(result.mailSubject)

        result.mailText = surveyService.surveyMailTextAsString(surveyInfo, reminderMail)


        result
    }

    Map mailSendProcessBySurvey(SurveyInfo surveyInfo, boolean reminderMail, GrailsParameterMap parameterMap) {
        Map<String, Object> result = [:]

        FlashScope flash = getCurrentFlashScope()
        result.mailFrom = from
        result.mailSubject = parameterMap.mailSubject

        result.editable = (surveyInfo && surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? surveyInfo.isEditable() : false

        Integer countReminderMails = 0
        Integer countOpenParticipants = 0

        if (parameterMap.selectedOrgs && result.editable) {
            result.surveyConfig = surveyInfo.surveyConfigs[0]

            parameterMap.list('selectedOrgs').each { soId ->

                Org selectedOrg = Org.get(Long.parseLong(soId))

                if (!reminderMail) {
                    SurveyOrg.withTransaction { TransactionStatus ts ->
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(selectedOrg, result.surveyConfig)

                        surveyOrg.finishDate = null
                        surveyOrg.save()
                        countOpenParticipants++
                    }
                }
                if (reminderMail) {
                    SurveyOrg.withTransaction { TransactionStatus ts ->
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(selectedOrg, result.surveyConfig)

                        surveyOrg.reminderMailDate = new Date()
                        surveyOrg.save()
                    }
                    countReminderMails++
                }

            }

            List emailAddressesToSend = []

            if (parameterMap.emailAddresses) {
                parameterMap.emailAddresses.split('; ').each {
                    emailAddressesToSend << it.trim()
                }
            }

            if (parameterMap.userSurveyNotificationMails) {
                parameterMap.userSurveyNotificationMails.split('; ').each {
                    emailAddressesToSend << it.trim()
                }
            }

            emailAddressesToSend = emailAddressesToSend.unique()

            emailAddressesToSend.each { String email ->

                Org orgOfEmail
                List<User> userList = User.findAllByEmail(email)

                if(userList.size() == 1){
                    List<UserOrgRole> userOrgRoleList = UserOrgRole.findAllByUser(userList[0])
                    if(userOrgRoleList.size() == 1){
                        orgOfEmail = userOrgRoleList[0].org
                    }else if (userOrgRoleList.size() > 1){
                        userOrgRoleList.each {}
                    }
                }

                List<UserOrgRole> userOrgs = UserOrgRole.findAllByOrgInList(result.orgList)

                List<String> userSurveyNotification = []

                userOrgs.each { userOrg ->
                    if (userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                            userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {
                        userSurveyNotification << userOrg.user.email
                    }
                }


                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                    multipart true
                    to email
                    from result.mailFrom
                    subject result.mailSubject
                    text parameterMap.mailText
                }
               /* MailReport mailReport = new MailReport(
                        subject: asynchronousMailMessage.subject,
                        content: asynchronousMailMessage,
                        from: asynchronousMailMessage.from,
                        replyTo: asynchronousMailMessage.replyTo,
                        to: asynchronousMailMessage.to,
                        ccReceiver: asynchronousMailMessage.cc,
                        bccReceiver: asynchronousMailMessage.bcc,
                        sentBySystem: false,
                        modifedByOwner: true,

                        asynchronousMailMessage: asynchronousMailMessage,

                        owner: surveyInfo.owner,
                        receiverOrg: null,

                        surveyOrg: null,

                        status: RDStore.MAIL_STATUS_SEND,
                        type: reminderMail ? RDStore.MAIL_TYPE_SURVEY_REMINDER : RDStore.MAIL_TYPE_SURVEY_OPEN,

                        sentDate: new Date(),
                )
                mailReport.save()*/
            }
        }
        Locale language = new Locale("de")
        Object[] args

        if (countReminderMails > 0) {
            args = [countReminderMails]
            flash.message = messageSource.getMessage('openParticipantsAgain.sendReminderMail.count', args, language)
        }

        if (countOpenParticipants > 0 && !reminderMail) {
            args = [countOpenParticipants]
            flash.message = messageSource.getMessage('openParticipantsAgain.openWithMail.count', args, language)
        }

        result
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
            log.error 'SurveyService.sendSurveyEmail() failed due grails.mail.disabled = true'
        }else {

            String replyTo

            String emailReceiver = user.getEmail()

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
                        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
                        String mailSubject = subjectSystemPraefix
                        if(reminderMail) {
                            mailSubject = mailSubject + ' ' + messageSource.getMessage('email.subject.surveysReminder', language)
                        }

                        mailSubject = mailSubject + ' ' + survey.name + ' ('+survey.type.getI10n('value', language)+')'
                        mailSubject = escapeService.replaceUmlaute(mailSubject)

                        if (isNotificationCCbyEmail && ccAddress) {
                            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
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
                            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
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
    def emailToSurveyParticipationByFinish(SurveyInfo surveyInfo, Org participationFinish) {

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.error 'surveyService.emailToSurveyParticipationByFinish() failed due grails.mail.disabled = true'
            return false
        }

        if (surveyInfo.owner) {
            //Only User that approved
            List<UserOrgRole> userOrgs = UserOrgRole.findAllByOrg(participationFinish)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if (userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {

                    User user = userOrg.user
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
                    String emailReceiver = user.getEmail()

                    String subjectText
                    Object[] args = [surveyInfo.name]
                    if (surveyInfo.type.id == RDStore.SURVEY_TYPE_RENEWAL.id) {
                        subjectText = messageSource.getMessage('email.survey.participation.finish.renewal.subject', args, language)
                    } else if (surveyInfo.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id) {
                        subjectText = messageSource.getMessage('email.survey.participation.finish.subscriptionSurvey.subject', args, language)
                    } else {
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
                            if (isNotificationCCbyEmail) {
                                ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                            }

                            List surveyResults = []

                            surveyInfo.surveyConfigs[0].getSortedSurveyProperties().each { PropertyDefinition propertyDefinition ->
                                surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participationFinish, surveyInfo.surveyConfigs[0], propertyDefinition)
                            }

                            if (isNotificationCCbyEmail && ccAddress) {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from from
                                    cc ccAddress
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
                                }
                            } else {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from from
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
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
    def emailToSurveyOwnerbyParticipationFinish(SurveyInfo surveyInfo, Org participationFinish) {

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.error 'emailToSurveyOwnerbyParticipationFinish() failed due grails.mail.disabled = true'
            return false
        }

        if (surveyInfo.owner) {
            //Only User that approved
            List<UserOrgRole> userOrgs = UserOrgRole.findAllByOrg(surveyInfo.owner)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if (userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {

                    User user = userOrg.user
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
                    String emailReceiver = user.getEmail()
                    String mailSubject = subjectSystemPraefix

                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyInfo.surveyConfigs[0], participationFinish)
                    if(surveyOrg && surveyOrg.orgInsertedItself) {
                        mailSubject = mailSubject + messageSource.getMessage('default.new', null, language) + ' '
                    }

                    mailSubject = mailSubject + surveyInfo.name +  ' (' + surveyInfo.type.getI10n('value', language) + ') ['
                    mailSubject = mailSubject + participationFinish.sortname + ']'

                    mailSubject = escapeService.replaceUmlaute(mailSubject)

                    try {
                        if (emailReceiver == null || emailReceiver.isEmpty()) {
                            log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                        } else {
                            boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                            String ccAddress = null
                            if (isNotificationCCbyEmail) {
                                ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                            }

                            List surveyResults = []

                            surveyInfo.surveyConfigs[0].getSortedSurveyProperties().each { PropertyDefinition propertyDefinition ->
                                surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participationFinish, surveyInfo.surveyConfigs[0], propertyDefinition)
                            }

                            if (isNotificationCCbyEmail && ccAddress) {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from from
                                    cc ccAddress
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                }
                            } else {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from from
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
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

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.error 'sendSystemAnnouncementMail failed due grails.mail.disabled = true'
            return
        }

        MessageSource messageSource = BeanStore.getMessageSource()
        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())

        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.sysAnnouncement', null, language)

        boolean isRemindCCbyEmail = user.getSetting(UserSetting.KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
        String ccAddress

        if (isRemindCCbyEmail) {
            ccAddress = user.getSetting(UserSetting.KEYS.REMIND_CC_EMAILADDRESS, null)?.getValue()
            // println user.toString() + " : " + isRemindCCbyEmail + " : " + ccAddress
        }

        if (isRemindCCbyEmail && ccAddress) {
            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                to user.getEmail()
                from ConfigMapper.getNotificationsEmailFrom()
                cc ccAddress
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject mailSubject
                body(view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: systemAnnouncement])
            }
        } else {
            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                to user.getEmail()
                from ConfigMapper.getNotificationsEmailFrom()
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject mailSubject
                body(view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: systemAnnouncement])
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

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.error 'sendMailToUser failed due grails.mail.disabled = true'
            return
        }

        model.serverURL = ConfigMapper.getGrailsServerURL()

        try {

            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                to user.email
                from ConfigMapper.getNotificationsEmailFrom()
                replyTo ConfigMapper.getNotificationsEmailReplyTo()
                subject ConfigMapper.getLaserSystemId() + ' - ' + subj
                body view: view, model: model
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception ${e.message}"
        }
    }

    FlashScope getCurrentFlashScope() {
        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        HttpServletRequest request = grailsWebRequest.getCurrentRequest()

        grailsWebRequest.attributes.getFlashScope(request)
    }
}
