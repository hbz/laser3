package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.transaction.TransactionStatus

/**
 * This controller is responsible for the creation and sending of automatised mails. It is currently used only for
 * the communication of survey related details, but should be used for other communication as well
 * @see SurveyInfo
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class MailController {

    ContextService contextService
    MailSendService mailSendService

    /**
     * Call to create a custom mail, attached to the given type of object. Currently, only {@link SurveyInfo}s are being supported
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    def createOwnMail() {
        log.debug("createOwnMail: " + params)
        Map<String, Object> result = [:]
        User user = contextService.getUser()
        Org contextOrg = contextService.getOrg()

        result.user = user
        result.institution = contextOrg
        result.contextOrg = contextOrg
        result.contextCustomerType = contextOrg.getCustomerType()

        result.orgList = []

        if (params.list('selectedOrgs')) {
            List idList = params.list('selectedOrgs')
            result.orgList = idList.isEmpty() ? [] : Org.findAllByIdInList(idList)
        }

        result.object = null

        result.objectId = params.objectId ?: params.id
        result.objectType = params.objectType

        if (result.objectType && result.objectId) {
            switch (result.objectType) {
                case SurveyInfo.class.name:

                    result.editable = contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

                    if (!result.editable) {
                        flash.error = g.message(code: "default.notAutorized.message")
                        redirect(url: request.getHeader('referer'))
                    }

                    result.surveyInfo = SurveyInfo.get(Long.parseLong(result.objectId))
                    result.surveyConfig = result.surveyInfo.surveyConfigs[0]

                    result.editable = (result.surveyInfo && result.surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? result.surveyInfo.isEditable() : false

                    if(!result.orgList){
                        flash.error = message(code: 'default.no.selected.org')
                        redirect(url: request.getHeader("referer"))
                        return
                    }

                    result.reminderMail = (params.openOption == 'ReminderMail')  ?: false
                    result.openAndSendMail = (params.openOption == 'OpenWithMail')  ?: false
                    result.openOnly = (params.openOption == 'OpenWithoutMail')  ?: false

                    result.editable = result.reminderMail ? result.surveyInfo.isEditable() : result.editable

                    if (result.editable) {

                        if(result.reminderMail || result.openAndSendMail) {
                            result << mailSendService.mailSendConfigBySurvey(result.surveyInfo, result.reminderMail)

                            List<User> formalUserList = result.orgList ? User.findAllByFormalOrgInList(result.orgList) : []
                            List<String> userSurveyNotification = []

                            formalUserList.each { fu ->
                                if (fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                                        fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES) {
                                    userSurveyNotification << fu.email
                                }
                            }

                            result.userSurveyNotificationMails = userSurveyNotification ? userSurveyNotification.join('; ') : ''
                        }else if(result.openOnly){
                            Integer countOpenParticipants = 0
                            result.orgList.each { Org org ->
                                    SurveyOrg.withTransaction { TransactionStatus ts ->
                                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(org, result.surveyConfig)
                                        surveyOrg.finishDate = null
                                        surveyOrg.save()
                                        countOpenParticipants++
                                    }
                            }

                            if(countOpenParticipants > 0){
                                flash.message =  g.message(code: 'openParticipantsAgain.open.count', args: [countOpenParticipants])
                            }
                            redirect(url: request.getHeader("referer"))
                            return
                        }
                    } else {
                        flash.error = message(code: 'default.noPermissions')
                        redirect(url: request.getHeader("referer"))
                        return
                    }

                    break
            }


        } else {
            flash.error = message(code: 'default.error')
            redirect(url: request.getHeader("referer"))
            return
        }

        result
    }

    /**
     * Sends the given mail. Depending on the object type the mail is attached to, certain procedures are being followed sending the mail
     * @return redirect back to the referer
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    def processSendMail() {
        log.debug("processSendMail: " + params)
        Map<String, Object> result = [:]
        User user = contextService.getUser()
        Org contextOrg = contextService.getOrg()

        result.user = user
        result.institution = contextOrg
        result.contextOrg = contextOrg
        result.contextCustomerType = contextOrg.getCustomerType()

        result.orgList = []

        if (params.list('selectedOrgs')) {
            List idList = params.list('selectedOrgs')
            result.orgList = idList.isEmpty() ? [] : Org.findAllByIdInList(idList)
        }

        result.object = null

        result.objectId = params.objectId ?: params.id

        if (params.objectType && result.objectId) {
            Map config = [:]

            switch (params.objectType) {
                case SurveyInfo.class.name:
                    result.editable = contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

                    if (!result.editable) {
                        flash.error = g.message(code: "default.notAutorized.message")
                        redirect(url: request.getHeader('referer'))
                    }

                    result.surveyInfo = SurveyInfo.get(Long.parseLong(result.objectId))

                    result.editable = (result.surveyInfo && result.surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

                    boolean reminderMail = params.reminderMail == 'false' ? false : true

                    if(result.editable) {
                        result << mailSendService.mailSendProcessBySurvey(result.surveyInfo, reminderMail, params)
                    }else {
                        flash.error = message(code: 'default.notAutorized.message')
                    }

                    String surveyView = reminderMail ? 'participantsReminder' : 'openParticipantsAgain'

                    redirect(action: surveyView, controller: 'survey', id: result.surveyInfo.id, params:[tab: params.tab])
                    return
                    break
            }


        } else {
            flash.error = message(code: 'default.error')
        }

        redirect(url: request.getHeader("referer"))
    }


}
