package de.laser

import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.properties.PropertyDefinition
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.system.SystemAnnouncement
import de.laser.system.SystemEvent
import de.laser.utils.AppUtils
import grails.gorm.transactions.Transactional
import grails.plugin.asyncmail.AsynchronousMailMessage
import grails.plugin.asyncmail.AsynchronousMailService
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

import javax.servlet.http.HttpServletRequest

@Transactional
class MailSendService {

    ContextService contextService
    EscapeService escapeService
    MessageSource messageSource
    SurveyService surveyService
    AsynchronousMailService asynchronousMailService

    String fromMail

    String currentServer = AppUtils.getCurrentServer()
    String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "" : (ConfigMapper.getLaserSystemId() + " - ")

    /**
     * Constructor method
     */
    @javax.annotation.PostConstruct
    void init() {
        fromMail = ConfigMapper.getNotificationsEmailFrom()
        messageSource = BeanStore.getMessageSource()
    }

    /**
     * Builds a notification mail about the given survey
     * @param surveyInfo the survey about which a notification should be sent
     * @param reminderMail is it a reminder mail?
     * @return a {@link Map} containing the details of the mail to be sent
     */
    Map mailSendConfigBySurvey(SurveyInfo surveyInfo, boolean reminderMail) {
        Map<String, Object> result = [:]
        String ownerFromMail
        if(OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY) != OrgSetting.SETTING_NOT_FOUND){
            ownerFromMail = OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY).strValue
        }

        result.mailFrom = ownerFromMail ?: fromMail
        result.mailSubject = ""
        result.mailText = ""

        Locale language = new Locale("de")

        result.mailSubject = subjectSystemPraefix
        if(reminderMail) {
            Object[] args
            result.mailSubject = result.mailSubject + ' ' + messageSource.getMessage('email.subject.surveysReminder', args, language)
        }

        result.mailSubject = result.mailSubject + ' ' + surveyInfo.name + ' ('+surveyInfo.type.getI10n('value', language)+')'
        result.mailSubject = escapeService.replaceUmlaute(result.mailSubject)

        result.mailText = surveyService.surveyMailTextAsString(surveyInfo, reminderMail)


        result
    }

    Map mailSendConfigBySurveys(List<SurveyInfo> surveys, boolean reminderMail) {
        Map<String, Object> result = [:]

        String ownerFromMail
        if(OrgSetting.get(surveys[0].owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY) != OrgSetting.SETTING_NOT_FOUND){
            ownerFromMail = OrgSetting.get(surveys[0].owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY).strValue
        }

        result.mailFrom = ownerFromMail ?: fromMail

        result.mailFrom = fromMail
        result.mailSubject = ""
        result.mailText = ""

        Locale language = new Locale("de")
        Object[] args
        result.mailSubject = subjectSystemPraefix
        if(reminderMail) {
            result.mailSubject = result.mailSubject + ' ' + messageSource.getMessage('email.subject.surveysReminder', args, language)
        }

        result.mailSubject = result.mailSubject + ' ' + messageSource.getMessage('survey.plural', args, language)
        result.mailSubject = escapeService.replaceUmlaute(result.mailSubject)

        result.mailText = surveyService.surveysMailTextAsString(surveys, reminderMail)


        result
    }

    /**
     * Sends survey notifications to the selected mail addresses about the given survey.
     * The mail header and body are user-defined and the addressees are some or all of the participant institutions of the given survey
     * @param surveyInfo the survey whose participants should be notified
     * @param reminderMail is this a reminder mail?
     * @param parameterMap the request parameter map, containing also the mail header and body which are being submitted via form
     * @return a ${link Map} containing the details of and about the mail to be send
     * @see #mailSendConfigBySurvey(de.laser.survey.SurveyInfo, boolean)
     * @see SurveyInfo
     */
    Map mailSendProcessBySurvey(SurveyInfo surveyInfo, boolean reminderMail, GrailsParameterMap parameterMap) {
        Map<String, Object> result = [:]

        FlashScope flash = getCurrentFlashScope()

        String ownerFromMail
        if(OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY) != OrgSetting.SETTING_NOT_FOUND){
            ownerFromMail = OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY).strValue
        }

        result.mailFrom = ownerFromMail ?: fromMail
        result.mailSubject = parameterMap.mailSubject

        result.editable = (surveyInfo && surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? surveyInfo.isEditable() : false

        Integer countReminderMails = 0
        Integer countOpenParticipants = 0

        if (parameterMap.selectedOrgs && result.editable) {
            result.surveyConfig = surveyInfo.surveyConfigs[0]
            String replyToMail

            if(ownerFromMail){
                replyToMail = ownerFromMail
            }else {
                List generalContactsEMails = []

                surveyInfo.owner.getGeneralContactPersons(true)?.each { person ->
                    person.contacts.each { contact ->
                        if (RDStore.CCT_EMAIL == contact.contentType) {
                            generalContactsEMails << contact.content
                        }
                    }
                }

                replyToMail = (generalContactsEMails.size() > 0) ? generalContactsEMails[0].toString() : null
            }

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

                        if(result.surveyConfig.subSurveyUseForTransfer){
                            result.surveyConfig.subscription.reminderSent = true
                            result.surveyConfig.subscription.reminderSentDate = new Date()
                        }
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

                // not used yet ..
//                Org orgOfEmail
//                List<User> userList = User.findAllByEmail(email)
//
//                if(userList.size() == 1){
//                    List<UserOrgRole> userOrgRoleList = UserOrgRole.findAllByUser(userList[0])
//                    if(userOrgRoleList.size() == 1){
//                        orgOfEmail = userOrgRoleList[0].org
//                    }else if (userOrgRoleList.size() > 1){
//                        userOrgRoleList.each {}
//                    }
//                }


                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                    multipart true
                    to email
                    from result.mailFrom
                    replyTo replyToMail
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

    Map mailSendProcessBySurveys(List<SurveyInfo> surveys, boolean reminderMail, Org org, GrailsParameterMap parameterMap) {
        Map<String, Object> result = [:]

        FlashScope flash = getCurrentFlashScope()

        String ownerFromMail
        if(OrgSetting.get(surveys[0].owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY) != OrgSetting.SETTING_NOT_FOUND){
            ownerFromMail = OrgSetting.get(surveys[0].owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY).strValue
        }

        result.mailFrom = ownerFromMail ?: fromMail
        result.mailSubject = parameterMap.mailSubject

        result.editable = contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (result.editable) {
            String replyToMail
            if(ownerFromMail){
                replyToMail = ownerFromMail
            }else{
                List generalContactsEMails = []

                surveys[0].owner.getGeneralContactPersons(true)?.each { person ->
                    person.contacts.each { contact ->
                        if (RDStore.CCT_EMAIL == contact.contentType) {
                            generalContactsEMails << contact.content
                        }
                    }
                }

                replyToMail = (generalContactsEMails.size() > 0) ? generalContactsEMails[0].toString() : null
            }


            surveys.each { surveyInfo ->
                SurveyConfig surveyConfig = surveyInfo.surveyConfigs[0]

                if (reminderMail) {
                    SurveyOrg.withTransaction { TransactionStatus ts ->
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(org, surveyConfig)

                        surveyOrg.reminderMailDate = new Date()
                        surveyOrg.save()

                        if (surveyConfig.subSurveyUseForTransfer) {
                            surveyConfig.subscription.reminderSent = true
                            surveyConfig.subscription.reminderSentDate = new Date()
                        }
                    }
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

                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                    multipart true
                    to email
                    from result.mailFrom
                    replyTo replyToMail
                    subject result.mailSubject
                    text parameterMap.mailText
                }
            }
        }
        Locale language = new Locale("de")
        Object[] args


        flash.message = messageSource.getMessage('mail.send.success', args, language)


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

            String replyToMail

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

                        String ownerFromMail
                        if(OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY) != OrgSetting.SETTING_NOT_FOUND){
                            ownerFromMail = OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_FROM_FOR_SURVEY).strValue
                        }

                        String mailFrom = ownerFromMail ?: fromMail

                        if(ownerFromMail){
                            replyToMail = ownerFromMail
                        }else {

                            List generalContactsEMails = []

                            survey.owner.getGeneralContactPersons(true)?.each { person ->
                                person.contacts.each { contact ->
                                    if (RDStore.CCT_EMAIL == contact.contentType) {
                                        generalContactsEMails << contact.content
                                    }
                                }
                            }
                            replyToMail = (generalContactsEMails.size() > 0) ? generalContactsEMails[0].toString() : null
                        }

                        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
                        String mailSubject = subjectSystemPraefix
                        if(reminderMail) {
                            Object[] args
                            mailSubject = mailSubject + ' ' + messageSource.getMessage('email.subject.surveysReminder', args, language)
                        }

                        mailSubject = mailSubject + ' ' + survey.name + ' ('+survey.type.getI10n('value', language)+')'
                        mailSubject = escapeService.replaceUmlaute(mailSubject)

                        if (isNotificationCCbyEmail && ccAddress) {
                            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                multipart true
                                to emailReceiver
                                from mailFrom
                                cc ccAddress
                                replyTo replyToMail
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        } else {
                            AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                multipart true
                                to emailReceiver
                                from mailFrom
                                replyTo replyToMail
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        }

                        log.debug("SurveyService - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + org.name);
                    }
                } catch (Exception e) {
                    String eMsg = e.message

                    log.error("SurveyService - sendSurveyEmail() :: Unable to perform email due to exception: ${eMsg}")
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
            List<User> formalUserList = participationFinish ? User.findAllByFormalOrg(participationFinish) : []

            //Only User with Notification by Email and for Surveys Start
            formalUserList.each { fu ->
                if (fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {

                    User user = fu
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
                            if (RDStore.CCT_EMAIL == contact.contentType) {
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
                                    from fromMail
                                    cc ccAddress
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
                                }
                            } else {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from fromMail
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults, generalContactsEMails: generalContactsEMails])
                                }
                            }

                            log.debug("emailToSurveyParticipationByFinish - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + participationFinish.name);
                        }
                    } catch (Exception e) {
                        String eMsg = e.message

                        log.error("emailToSurveyParticipationByFinish - sendSurveyEmail() :: Unable to perform email due to exception: ${eMsg}")
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
            List surveyResults = []

            surveyInfo.surveyConfigs[0].getSortedSurveyProperties().each { PropertyDefinition propertyDefinition ->
                surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participationFinish, surveyInfo.surveyConfigs[0], propertyDefinition)
            }

            String mailFinishResult
            if(OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT) != OrgSetting.SETTING_NOT_FOUND){
                mailFinishResult = OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT).strValue
            }

            if(mailFinishResult){
                Locale language = new Locale('de')
                String mailSubject = subjectSystemPraefix

                SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyInfo.surveyConfigs[0], participationFinish)
                if(surveyOrg && surveyOrg.orgInsertedItself) {
                    Object[] args
                    mailSubject = mailSubject + messageSource.getMessage('default.new', args, language) + ' '
                }

                mailSubject = mailSubject + surveyInfo.name +  ' (' + surveyInfo.type.getI10n('value', language) + ') ['
                mailSubject = mailSubject + participationFinish.sortname + ']'

                mailSubject = escapeService.replaceUmlaute(mailSubject)

                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                    to mailFinishResult
                    from fromMail
                    subject mailSubject
                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                }
            }


            //Only User that approved
            List<User> formalUserList = surveyInfo.owner ? User.findAllByFormalOrg(surveyInfo.owner) : []

            //Only User with Notification by Email and for Surveys Start
            formalUserList.each { fu ->
                if (fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {

                    User user = fu
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())
                    String emailReceiver = user.getEmail()
                    String mailSubject = subjectSystemPraefix

                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyInfo.surveyConfigs[0], participationFinish)
                    if(surveyOrg && surveyOrg.orgInsertedItself) {
                        Object[] args
                        mailSubject = mailSubject + messageSource.getMessage('default.new', args, language) + ' '
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

                            if (isNotificationCCbyEmail && ccAddress) {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from fromMail
                                    cc ccAddress
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                }
                            } else {
                                AsynchronousMailMessage asynchronousMailMessage = asynchronousMailService.sendMail {
                                    to emailReceiver
                                    from fromMail
                                    subject mailSubject
                                    html(view: "/mailTemplates/html/notificationSurveyParticipationFinishForOwner", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                }
                            }

                            log.debug("emailToSurveyOwnerbyParticipationFinish - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + surveyInfo.owner.name);
                        }
                    } catch (Exception e) {
                        String eMsg = e.message

                        log.error("emailToSurveyOwnerbyParticipationFinish - sendSurveyEmail() :: Unable to perform email due to exception: ${eMsg}")
                        SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: participationFinish.name, survey: surveyInfo.name])
                    }
                }
            }
        }
    }

    /**
     * Sends a mail to a given user. The system announcement is being included in a mail template
     * @param user the {@link User} to be notified
     * @param systemAnnouncement the {@link SystemAnnouncement} to be broadcasted
     * @throws Exception
     */
    void sendSystemAnnouncementMail(User user, SystemAnnouncement systemAnnouncement) throws Exception {

        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.error 'sendSystemAnnouncementMail failed due grails.mail.disabled = true'
            return
        }

        MessageSource messageSource = BeanStore.getMessageSource()
        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value.toString())

        Object[] args
        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.sysAnnouncement', args, language)

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
                body(view: view, model: model)
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception: ${e.message}"
        }
    }

    /**
     * Helper method to fetch the {@link FlashScope} for the current request
     * @return the current {@link FlashScope} registered to this request
     */
    FlashScope getCurrentFlashScope() {
        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        HttpServletRequest request = grailsWebRequest.getCurrentRequest()

        grailsWebRequest.attributes.getFlashScope(request)
    }
}
