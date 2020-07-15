package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.kbplus.UserSettings.KEYS
import com.k_int.properties.PropertyDefinition
import de.laser.DeletionService
import de.laser.FormService
import de.laser.helper.EhcacheWrapper
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {

    def cacheService
    def contextService
    def genericOIDService
    def springSecurityService
    def passwordEncoder
    def errorReportService
    def refdataService
    def propertyService
    def instAdmService
    def deletionService
    FormService formService

    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = true
        result.isOrgBasicMember = contextService.org.getCustomerType() == 'ORG_BASIC_MEMBER'
        result.availableOrgs  = Org.executeQuery('from Org o where o.sector = :sector order by o.sortname', [sector: RDStore.O_SECTOR_HIGHER_EDU])
        result.availableOrgRoles = Role.findAllByRoleType('user')
        result
    }

    @Secured(['ROLE_ADMIN'])
    def errorReport() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)

        if (params.sendErrorReport) {
            def data = [
                    author:     result.user,
                    title:      params.title?.trim(),
                    described:  params.described?.trim(),
                    expected:   params.expected?.trim(),
                    info:       params.info?.trim(),
                    status:     RefdataValue.getByValueAndCategory('New', RDConstants.TICKET_STATUS),
                    category:   RefdataValue.getByValueAndCategory('Bug', RDConstants.TICKET_CATEGORY)
            ]
            result.sendingStatus = (errorReportService.writeReportIntoDB(data) ? 'ok' : 'fail')
        }

        result.title = params.title
        result.described = params.described
        result.expected = params.expected
        result.info = params.info

        result
    }

    @Secured(['ROLE_ADMIN'])
    def errorOverview() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)

        result.tickets = SystemTicket.where{}.list(sort: 'dateCreated', order: 'desc')

        result.editable = SpringSecurityUtils.ifAnyGranted("ROLE_YODA,ROLE_TICKET_EDITOR")
        result
    }

    @Secured(['ROLE_USER'])
    def help() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result
    }

    @Secured(['ROLE_USER'])
    def dsgvo() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result
    }

    @Secured(['ROLE_USER'])
    def processJoinRequest() {
        log.debug("processJoinRequest(${params}) org with id ${params.org} role ${params.formalRole}")

        User user       = User.get(springSecurityService.principal.id)
        Org org         = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_PENDING, flash)
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def processCancelRequest() {
        log.debug("processCancelRequest(${params}) userOrg with id ${params.assoc}")
        User user        = User.get(springSecurityService.principal.id)
        UserOrg userOrg  = UserOrg.findByUserAndId(user, params.assoc)

        if (userOrg) {
            userOrg.delete(flush:true)
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def deleteProfile() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)

         // TODO : isLastAdminForOrg

        if (params.process) {
            User userReplacement = genericOIDService.resolveOID(params.userReplacement)

            result.delResult = deletionService.deleteUser(result.user, userReplacement, false)

            if (result.delResult.status == DeletionService.RESULT_SUCCESS) {
                redirect controller: 'logout', action: 'index'
                return
            }
        }
        else {
            result.delResult = deletionService.deleteUser(result.user, null, DeletionService.DRY_RUN)
        }

        result.substituteList = User.executeQuery(
                'select distinct u from User u join u.affiliations ua where ua.status = :uaStatus and ua.org = :ctxOrg and u != :self',
                [uaStatus: UserOrg.STATUS_APPROVED, ctxOrg: contextService.getOrg(), self: result.user]
        )

        render view: 'deleteProfile', model: result
    }

  @Secured(['ROLE_USER'])
  def updateProfile() {
    User user = User.get(springSecurityService.principal.id)

    flash.message=""

    if ( user.display != params.userDispName ) {
      user.display = params.userDispName
      flash.message += message(code:'profile.updateProfile.updated.name')
    }

    if ( user.email != params.email ) {
      if ( formService.validateEmailAddress(params.email) ) {
        user.email = params.email
        flash.message += message(code:'profile.updateProfile.updated.email')
      }
      else {
        flash.error = message(code:'profile.updateProfile.updated.email.error')
      }
    }


    // deprecated
    if ( params.defaultPageSize != null ) {
      try {
        long l = Long.parseLong(params.defaultPageSize);
        if ( ( l >= 5 ) && ( l <= 100 ) ) {
          Long new_long = new Long(l);
          if ( new_long != user.getDefaultPageSizeTMP() ) {
            flash.message += message(code:'profile.updateProfile.updated.pageSize')
          }
            //user.setDefaultPageSizeTMP(new_long)
            def setting = user.getSetting(KEYS.PAGE_SIZE, null)
            setting.setValue(size)
     
        }
        else {
          flash.message+= message(code:'profile.updateProfile.updated.pageSize.error')
        }
      }
      catch ( Exception e ) {
      }
    }

        user.save()

        if (params.defaultDash) {
            Org org = genericOIDService.resolveOID(params.defaultDash)
            UserSettings us = user.getSetting(KEYS.DASHBOARD, null)

            if (org?.id != us.getValue()?.id) {
                us.setValue(org)
                flash.message += message(code: 'profile.updateProfile.updated.dash')
            }
        }

        redirect(action: "index")
    }

  @Secured(['ROLE_USER'])
  def updateReminderSettings() {
    User user = User.get(springSecurityService.principal.id)

    flash.message = ""
    flash.error = ""

    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),         params.remindPeriodForLicensePrivateProp,       'profile.updateProfile.updated.remindPeriodForLicensePrivateProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),          params.remindPeriodForLicenseCustomProp,        'profile.updateProfile.updated.remindPeriodForLicenseCustomProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),              params.remindPeriodForOrgCustomProp,            'profile.updateProfile.updated.remindPeriodForOrgCustomProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),             params.remindPeriodForOrgPrivateProp,           'profile.updateProfile.updated.remindPeriodForOrgPrivateProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),          params.remindPeriodForPersonPrivateProp,        'profile.updateProfile.updated.remindPeriodForPersonPrivateProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),    params.remindPeriodForSubscriptionsCustomProp,  'profile.updateProfile.updated.remindPeriodForSubscriptionsCustomProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, UserSettings.DEFAULT_REMINDER_PERIOD),   params.remindPeriodForSubscriptionsPrivateProp, 'profile.updateProfile.updated.remindPeriodForSubscriptionsPrivateProp')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, UserSettings.DEFAULT_REMINDER_PERIOD),   params.remindPeriodForSubscriptionNoticeperiod, 'profile.updateProfile.updated.remindPeriodForSubscriptionNoticeperiod')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, UserSettings.DEFAULT_REMINDER_PERIOD),        params.remindPeriodForSubscriptionEnddate,      'profile.updateProfile.updated.remindPeriodForSubscriptionEnddate')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_TASKS, UserSettings.DEFAULT_REMINDER_PERIOD),                        params.remindPeriodForTasks,                    'profile.updateProfile.updated.remindPeriodForTasks')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, UserSettings.DEFAULT_REMINDER_PERIOD),params.remindPeriodForSurveysEndDate,           'profile.updateProfile.updated.remindPeriodForSurveyEndDate')
    changeValue(user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSettings.DEFAULT_REMINDER_PERIOD),    params.remindPeriodForSurveysMandatoryEndDate, 'profile.updateProfile.updated.remindPeriodForSurveyMandatoryEndDate')

    //Error: Emailreminder without Emailaddress
    if ( (! user.email) && params.isRemindByEmail) {
      flash.error += message(code:'profile.updateProfile.updated.isRemindByEmail.error')
    } else {
      changeValue(user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO),                                             params.isRemindByEmail?:"N",                    'profile.updateProfile.updated.isRemindByEmail')
    }

    //Error: EmailCCReminder without EmailReminder
    if (user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).equals(RDStore.YN_NO) && params.isRemindCCByEmail){
        flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.isRemindByEmailNotChecked')
    } else {
        if ( params.isRemindCCByEmail && ( ! params.remindCCEmailaddress) ) {
            flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.noCCEmailAddressError')
        } else {
            if (params.remindCCEmailaddress == null || params.remindCCEmailaddress.trim() == '' || formService.validateEmailAddress(params.remindCCEmailaddress)){
                changeValue(user.getSetting(KEYS.REMIND_CC_EMAILADDRESS, null),       params.remindCCEmailaddress,     'profile.updateProfile.updated.remindCCEmailaddress')
            } else {
                flash.error += message(code:'profile.updateProfile.updated.email.error')
            }
            changeValue(user.getSetting(KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO),                   params.isRemindCCByEmail?:"N",     'profile.updateProfile.updated.isRemindCCByEmail')
        }
    }

    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, RDStore.YN_NO),    params.isSubscriptionsNoticePeriod?:"N",     'profile.updateProfile.updated.subscriptions.noticePeriod')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_NO),         params.isSubscriptionsEnddate?:"N",          'profile.updateProfile.updated.subscriptions.enddate')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_NO),     params.isSubscriptionsCustomProp?:"N",       'profile.updateProfile.updated.subscriptions.customProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_NO),    params.isSubscriptionsPrivateProp?:"N",      'profile.updateProfile.updated.subscriptions.privateProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_NO),           params.isLicenseCustomProp?:"N",             'profile.updateProfile.updated.license.customProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_NO),          params.isLicensePrivateProp?:"N",            'profile.updateProfile.updated.license.privateProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_NO),               params.isOrgCustomProp?:"N",                 'profile.updateProfile.updated.org.customProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_NO),              params.isOrgPrivateProp?:"N",                'profile.updateProfile.updated.org.privateProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_NO),           params.isPersonPrivateProp?:"N",             'profile.updateProfile.updated.person.privateProperty')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_NO),                         params.isTasks?:"N",                         'profile.updateProfile.updated.tasks')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, RDStore.YN_NO), params.isSurveysNotMandatoryEndDate?:"N",      'profile.updateProfile.updated.surveysEndDate')
    changeValue(user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_NO),     params.isSurveysMandatoryEndDate?:"N",      'profile.updateProfile.updated.surveysMandatoryEndDate')

    user.save();

    redirect(action: "index")
  }
    @Secured(['ROLE_USER'])
    def updateNotificationSettings() {
        User user = User.get(springSecurityService.principal.id)

        flash.message = ""
        flash.error = ""

        //Error: Emailreminder without Emailaddress
        if ( (! user.email) && params.isNotificationByEmail) {
            flash.error += message(code:'profile.updateProfile.updated.isNotificationByEmail.error')
        } else {
            changeValue(user.getSetting(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO),                                             params.isNotificationByEmail?:"N",                    'profile.updateProfile.updated.isNotificationByEmail')
        }

        //Error: EmailCCReminder without EmailReminder
        if (user.getSetting(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO).equals(RDStore.YN_NO) && params.isNotificationCCByEmail){
            flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.isNotificationByEmailNotChecked')
        } else {
            if ( params.isNotificationCCByEmail && ( ! params.notificationCCEmailaddress) ) {
                flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.noCCEmailAddressError')
            } else {
                if (params.notificationCCEmailaddress){
                    if (formService.validateEmailAddress(params.notificationCCEmailaddress)){
                        changeValue(user.getSetting(KEYS.NOTIFICATION_CC_EMAILADDRESS, null),       params.notificationCCEmailaddress,     'profile.updateProfile.updated.notificationCCEmailaddress')
                    } else {
                        flash.error += message(code:'profile.updateProfile.updated.email.error')
                    }
                }
                changeValue(user.getSetting(KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO),                   params.isNotificationCCByEmail?:"N",     'profile.updateProfile.updated.isNotificationCCByEmail')
            }
        }

        changeValue(user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_NO),    params.isNotificationForSurveysStart?:"N",     'profile.updateProfile.updated.surveysStart')
        changeValue(user.getSetting(KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_NO),    params.isNotificationForSystemMessages?:"N",     'profile.updateProfile.updated.systemMessages')

        changeValue(user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_NO),    params.isNotificationForSurveysParticipationFinish?:"N",     'profile.updateProfile.updated.surveysParticipationFinish')

        user.save();

        redirect(action: "index")
    }

    private void changeValue(UserSettings userSetting, def newValue, String messageSuccessfull) {
        def oldValue = userSetting.value
        if (    KEYS.REMIND_PERIOD_FOR_TASKS == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE == userSetting.key ||
                KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE == userSetting.key
        ) {
            if ( ! newValue) {
                flash.error += (message(args: userSetting.key, code: 'profile.updateProfile.updated.error.dashboardReminderPeriod') + "<br/>")
                return
            }
        }
        if (userSetting.key.type == RefdataValue && userSetting.key.rdc == RDConstants.Y_N) {
            if (newValue == 'Y') {
                newValue = RDStore.YN_YES
            } else {
                newValue = RDStore.YN_NO
            }
        }
        if (userSetting.key.type == Integer) {
            newValue = Integer.parseInt(newValue)
        }
        if (userSetting.key.type == String) {
            newValue = ((String)newValue).isEmpty() ? null : newValue.trim()
        }
        boolean valueHasChanged = oldValue != newValue
        if (valueHasChanged) {
            userSetting.setValue(newValue)
            flash.message += (message(code: messageSuccessfull) + "<br/>")
        }
    }

    @Secured(['ROLE_USER'])
    def updateIsRemindByEmail() {
        User user1 = User.get(springSecurityService.principal.id)

        flash.message=""
        def was_isRemindByEmail = user1.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO)
        if ( was_isRemindByEmail != params.isRemindByEmail ) {
            was_isRemindByEmail = params.isRemindByEmail
            flash.message += message(code:'profile.updateProfile.updated.isRemindByEmail')
            if ( ! user1.email && was_isRemindByEmail.equals(RDStore.YN_YES)) {
                flash.error = message(code:'profile.updateProfile.updated.isRemindByEmail.error')
            }
        }
        user.save();

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def updatePassword() {
        User user = User.get(springSecurityService.principal.id)
        flash.message = ""

        if (passwordEncoder.isPasswordValid(user.password, params.passwordCurrent, null)) {
            if (params.passwordNew.trim().size() < 5) {
                flash.message += message(code:'profile.password.update.enterValidNewPassword')
            } else {
                user.password = params.passwordNew

                if (user.save()) {
                    flash.message += message(code:'profile.password.update.success')
                }
            }

        } else {
            flash.error = message(code:'profile.password.update.enterValidCurrentPassword')
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def properties() {

        EhcacheWrapper cache = cacheService.getTTL300Cache('ProfileController/properties')

        def propDefs = [:]

        if (cache.get('propDefs')) {
            propDefs = cache.get('propDefs')
            log.debug('propDefs from cache')
        }
        else {
            PropertyDefinition.AVAILABLE_CUSTOM_DESCR.each { it ->
                def itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: 'name']) // NO private properties!
                propDefs << ["${it}": itResult]
            }
            cache.put('propDefs', propDefs)
        }

        def usedRdvList, rdvAttrMap, usedPdList, pdAttrMap

        if (cache.get('usedRdvList')) {
            usedRdvList = cache.get('usedRdvList')
            log.debug('usedRdvList from cache')
        }
        else {
            (usedRdvList, rdvAttrMap) = refdataService.getUsageDetails()
            cache.put('usedRdvList', usedRdvList)
        }

        if (cache.get('usedPdList')) {
            usedPdList = cache.get('usedPdList')
            log.debug('usedPdList from cache')
        }
        else {
            (usedPdList, pdAttrMap) = propertyService.getUsageDetails()
            cache.put('usedPdList', usedPdList)
        }

        render view: 'properties', model: [
                editable    : false,
                cachedContent : cache.getCache().name,
                propertyDefinitions: propDefs,
                rdCategories: RefdataCategory.where{}.sort('desc'),
                usedRdvList : usedRdvList,
                usedPdList  : usedPdList
        ]
    }
}
