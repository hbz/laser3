package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinition
import de.laser.helper.EhcacheWrapper
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import static com.k_int.kbplus.UserSettings.KEYS.*
import static com.k_int.kbplus.UserSettings.DEFAULT_REMINDER_PERIOD
import static de.laser.helper.RDStore.*

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

    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = true

        result.availableOrgs  = Org.executeQuery('from Org o where o.sector = ? order by o.sortname', [RDStore.O_SECTOR_HIGHER_EDU])
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
        def user        = User.get(springSecurityService.principal.id)
        def userOrg     = UserOrg.findByUserAndId(user, params.assoc)

        if (userOrg) {
            userOrg.delete(flush:true)
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def processDeleteUser() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)

        String name = result.user.getDisplayName()
        boolean isLastAdminForOrg = false

        result.user.affiliations.each { aff ->
            if(aff.status == UserOrg.STATUS_APPROVED) {
                if (instAdmService.isLastAdminForOrg(aff.org, result.user)) {
                    isLastAdminForOrg = true
                }
            }
        }

        if (!isLastAdminForOrg) {
            result = deletionService.deleteUser(result.user, User.findByUsername('anonymous'), false)
            redirect(controller: 'logout', action: 'index')
        }else{
            flash.error = message(code:'user.affiliation.lastAdminForOrg', args: [name])
            redirect(action: "index")
        }

    }

    private validateEmailAddress(String email) {
        def mailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})/
        return ( email ==~ mailPattern )
    }

  @Secured(['ROLE_USER'])
  def updateProfile() {
    def user = User.get(springSecurityService.principal.id)

    flash.message=""

    if ( user.display != params.userDispName ) {
      user.display = params.userDispName
      flash.message += message(code:'profile.updateProfile.updated.name')
    }

    if ( user.email != params.email ) {
      if ( validateEmailAddress(params.email) ) {
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
            def setting = user.getSetting(UserSettings.KEYS.PAGE_SIZE, null)
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
            UserSettings us = user.getSetting(UserSettings.KEYS.DASHBOARD, null)

            if (org?.id != us.getValue()?.id) {
                us.setValue(org)
                flash.message += message(code: 'profile.updateProfile.updated.dash')
            }
        }

        redirect(action: "index")
    }

  @Secured(['ROLE_USER'])
  def updateReminderSettings() {
    def user = User.get(springSecurityService.principal.id)

    flash.message = ""
    flash.error = ""

    changeValue(user.getSetting(REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, DEFAULT_REMINDER_PERIOD),         params.remindPeriodForLicensePrivateProp,       'profile.updateProfile.updated.remindPeriodForLicensePrivateProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, DEFAULT_REMINDER_PERIOD),          params.remindPeriodForLicenseCustomProp,        'profile.updateProfile.updated.remindPeriodForLicenseCustomProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, DEFAULT_REMINDER_PERIOD),              params.remindPeriodForOrgCustomProp,            'profile.updateProfile.updated.remindPeriodForOrgCustomProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, DEFAULT_REMINDER_PERIOD),             params.remindPeriodForOrgPrivateProp,           'profile.updateProfile.updated.remindPeriodForOrgPrivateProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, DEFAULT_REMINDER_PERIOD),          params.remindPeriodForPersonPrivateProp,        'profile.updateProfile.updated.remindPeriodForPersonPrivateProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, DEFAULT_REMINDER_PERIOD),    params.remindPeriodForSubscriptionsCustomProp,  'profile.updateProfile.updated.remindPeriodForSubscriptionsCustomProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, DEFAULT_REMINDER_PERIOD),   params.remindPeriodForSubscriptionsPrivateProp, 'profile.updateProfile.updated.remindPeriodForSubscriptionsPrivateProp')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, DEFAULT_REMINDER_PERIOD),   params.remindPeriodForSubscriptionNoticeperiod, 'profile.updateProfile.updated.remindPeriodForSubscriptionNoticeperiod')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, DEFAULT_REMINDER_PERIOD),        params.remindPeriodForSubscriptionEnddate,      'profile.updateProfile.updated.remindPeriodForSubscriptionEnddate')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_TASKS, DEFAULT_REMINDER_PERIOD),                        params.remindPeriodForTasks,                    'profile.updateProfile.updated.remindPeriodForTasks')
    changeValue(user.getSetting(REMIND_PERIOD_FOR_SURVEYS_ENDDATE, DEFAULT_REMINDER_PERIOD),              params.remindPeriodForSurveysEndDate,           'profile.updateProfile.updated.remindPeriodForSurveyEndDate')

    //Error: Emailreminder without Emailaddress
    if ( (! user.email) && params.isRemindByEmail) {
      flash.error += message(code:'profile.updateProfile.updated.isRemindByEmail.error')
    } else {
      changeValue(user.getSetting(IS_REMIND_BY_EMAIL, YN_NO),                                             params.isRemindByEmail?:"N",                    'profile.updateProfile.updated.isRemindByEmail')
    }

    //Error: EmailCCReminder without EmailReminder
    if (user.getSetting(IS_REMIND_BY_EMAIL, YN_NO).equals(YN_NO) && params.isRemindCCByEmail){
        flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.isRemindByEmailNotChecked')
    } else {
        if ( params.isRemindCCByEmail && ( ! params.remindCCEmailaddress) ) {
            flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.noCCEmailAddressError')
        } else {
            if (params.remindCCEmailaddress == null || params.remindCCEmailaddress.trim() == '' || validateEmailAddress(params.remindCCEmailaddress)){
                changeValue(user.getSetting(REMIND_CC_EMAILADDRESS, null),       params.remindCCEmailaddress,     'profile.updateProfile.updated.remindCCEmailaddress')
            } else {
                flash.error += message(code:'profile.updateProfile.updated.email.error')
            }
            changeValue(user.getSetting(IS_REMIND_CC_BY_EMAIL, YN_NO),                   params.isRemindCCByEmail?:"N",     'profile.updateProfile.updated.isRemindCCByEmail')
        }
    }

    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, YN_NO),    params.isSubscriptionsNoticePeriod?:"N",     'profile.updateProfile.updated.subscriptions.noticePeriod')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, YN_NO),         params.isSubscriptionsEnddate?:"N",          'profile.updateProfile.updated.subscriptions.enddate')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, YN_NO),     params.isSubscriptionsCustomProp?:"N",       'profile.updateProfile.updated.subscriptions.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, YN_NO),    params.isSubscriptionsPrivateProp?:"N",      'profile.updateProfile.updated.subscriptions.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_LICENSE_CUSTOM_PROP, YN_NO),           params.isLicenseCustomProp?:"N",             'profile.updateProfile.updated.license.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, YN_NO),          params.isLicensePrivateProp?:"N",            'profile.updateProfile.updated.license.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_ORG_CUSTOM_PROP, YN_NO),               params.isOrgCustomProp?:"N",                 'profile.updateProfile.updated.org.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_ORG_PRIVATE_PROP, YN_NO),              params.isOrgPrivateProp?:"N",                'profile.updateProfile.updated.org.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_PERSON_PRIVATE_PROP, YN_NO),           params.isPersonPrivateProp?:"N",             'profile.updateProfile.updated.person.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_TASKS, YN_NO),                         params.isTasks?:"N",                         'profile.updateProfile.updated.tasks')
    changeValue(user.getSetting(IS_REMIND_FOR_SURVEYS_ENDDATE, YN_NO),               params.isSurveysEndDate?:"N",      'profile.updateProfile.updated.surveysEndDate')

    user.save();

    redirect(action: "index")
  }
    @Secured(['ROLE_USER'])
    def updateNotificationSettings() {
        def user = User.get(springSecurityService.principal.id)

        flash.message = ""
        flash.error = ""

        //Error: Emailreminder without Emailaddress
        if ( (! user.email) && params.isNotificationByEmail) {
            flash.error += message(code:'profile.updateProfile.updated.isNotificationByEmail.error')
        } else {
            changeValue(user.getSetting(IS_NOTIFICATION_BY_EMAIL, YN_NO),                                             params.isNotificationByEmail?:"N",                    'profile.updateProfile.updated.isNotificationByEmail')
        }

        //Error: EmailCCReminder without EmailReminder
        if (user.getSetting(IS_NOTIFICATION_BY_EMAIL, YN_NO).equals(YN_NO) && params.isNotificationCCByEmail){
            flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.isNotificationByEmailNotChecked')
        } else {
            if ( params.isNotificationCCByEmail && ( ! params.notificationCCEmailaddress) ) {
                flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.noCCEmailAddressError')
            } else {
                if (params.notificationCCEmailaddress){
                    if (validateEmailAddress(params.notificationCCEmailaddress)){
                        changeValue(user.getSetting(NOTIFICATION_CC_EMAILADDRESS, null),       params.notificationCCEmailaddress,     'profile.updateProfile.updated.notificationCCEmailaddress')
                    } else {
                        flash.error += message(code:'profile.updateProfile.updated.email.error')
                    }
                }
                changeValue(user.getSetting(IS_NOTIFICATION_CC_BY_EMAIL, YN_NO),                   params.isNotificationCCByEmail?:"N",     'profile.updateProfile.updated.isNotificationCCByEmail')
            }
        }

        changeValue(user.getSetting(IS_NOTIFICATION_FOR_SURVEYS_START, YN_NO),    params.isNotificationForSurveysStart?:"N",     'profile.updateProfile.updated.surveysStart')
        changeValue(user.getSetting(IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, YN_NO),    params.isNotificationForSystemMessages?:"N",     'profile.updateProfile.updated.systemMessages')

        user.save();

        redirect(action: "index")
    }

    private void changeValue(UserSettings userSetting, def newValue, String messageSuccessfull) {
        def oldValue = userSetting.value
        if (    REMIND_PERIOD_FOR_TASKS == userSetting.key ||
                REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD == userSetting.key ||
                REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE == userSetting.key ||
                REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_ORG_PRIVATE_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_ORG_CUSTOM_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP == userSetting.key ||
                REMIND_PERIOD_FOR_SURVEYS_ENDDATE == userSetting.key
        ) {
            if ( ! newValue) {
                flash.error += (message(args: userSetting.key, code: 'profile.updateProfile.updated.error.dashboardReminderPeriod') + "<br/>")
                return
            }
        }
        if (userSetting.key.type == RefdataValue && userSetting.key.rdc == RDConstants.Y_N) {
            if (newValue == 'Y') {
                newValue = YN_YES
            } else {
                newValue = YN_NO
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
        def user1 = User.get(springSecurityService.principal.id)

        flash.message=""
        def was_isRemindByEmail = user1.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO)
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
        def user = User.get(springSecurityService.principal.id)
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
            flash.message += message(code:'profile.password.update.enterValidCurrentPassword')
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
