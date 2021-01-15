package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.UserSetting.KEYS
import de.laser.helper.EhcacheWrapper
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.system.SystemTicket
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {

    def contextService
    def genericOIDService
    def passwordEncoder
    def instAdmService
    def deletionService
    FormService formService
    CacheService cacheService
    RefdataService refdataService
    PropertyService propertyService

    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.editable = true
        result.isOrgBasicMember = contextService.getOrg().getCustomerType() == 'ORG_BASIC_MEMBER'
        result.availableOrgs  = Org.executeQuery('from Org o where o.sector = :sector order by o.sortname', [sector: RDStore.O_SECTOR_HIGHER_EDU])
        result.availableOrgRoles = Role.findAllByRoleType('user')

        result
    }

    @Secured(['ROLE_ADMIN'])
    def errorReport() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.title = params.title
        result.described = params.described
        result.expected = params.expected
        result.info = params.info

        result
    }

    @Secured(['ROLE_ADMIN'])
    def errorOverview() {
        Map<String, Object> result = [:]
        result.user     = contextService.getUser()
        result.tickets  = SystemTicket.where{}.list(sort: 'dateCreated', order: 'desc')
        result.editable = SpringSecurityUtils.ifAnyGranted("ROLE_YODA,ROLE_TICKET_EDITOR")

        result
    }

    @Secured(['ROLE_USER'])
    def help() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result
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

        List usedRdvList, usedPdList

        if (cache.get('usedRdvList')) {
            usedRdvList = cache.get('usedRdvList')
            log.debug('usedRdvList from cache')
        }
        else {
            usedRdvList = refdataService.getUsageDetails()
            cache.put('usedRdvList', usedRdvList)
        }

        if (cache.get('usedPdList')) {
            usedPdList = cache.get('usedPdList')
            log.debug('usedPdList from cache')
        }
        else {
            usedPdList = propertyService.getUsageDetails()
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

    @Secured(['ROLE_USER'])
    def dsgvo() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result
    }

    @Secured(['ROLE_USER'])
    def processJoinRequest() {
        log.debug("processJoinRequest(${params}) org with id ${params.org} role ${params.formalRole}")

        User user       = contextService.getUser()
        Org org         = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_PENDING, flash)
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def processCancelRequest() {
        log.debug("processCancelRequest(${params}) userOrg with id ${params.assoc}")
        User user        = contextService.getUser()
        UserOrg userOrg  = UserOrg.findByUserAndId(user, params.assoc)

        if (userOrg) {
            userOrg.delete()
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def deleteProfile() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()

         // TODO : isLastAdminForOrg

        if (params.process) {
            User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

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
    @Transactional
    def updateProfile() {

        User user = contextService.getUser()
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
                    if ( new_long != user.getDefaultPageSize() ) {
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
            catch ( Exception e ) {}
        }

        user.save()

        if (params.defaultDash) {
            Org org = (Org) genericOIDService.resolveOID(params.defaultDash)
            UserSetting us = user.getSetting(KEYS.DASHBOARD, null)

            if (org?.id != us.getValue()?.id) {
                us.setValue(org)
                flash.message += message(code: 'profile.updateProfile.updated.dash')
            }
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def updateReminderSettings() {
        User user = contextService.getUser()

        flash.message = ""
        flash.error = ""

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicensePrivateProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForLicensePrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicenseCustomProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForLicenseCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgCustomProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForOrgCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgPrivateProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForOrgPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForPersonPrivateProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForPersonPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsCustomProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSubscriptionsCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsPrivateProp',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSubscriptionsPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionNoticeperiod',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSubscriptionNoticeperiod' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionEnddate',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSubscriptionEnddate' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_TASKS, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForTasks',
                        null,
                        'profile.updateProfile.updated.remindPeriodForTasks' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysEndDate',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSurveyEndDate' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysMandatoryEndDate',
                        null,
                        'profile.updateProfile.updated.remindPeriodForSurveyMandatoryEndDate' )

    //Error: Emailreminder without Emailaddress
    if ( (! user.email) && params.isRemindByEmail) {
      flash.error += message(code:'profile.updateProfile.updated.isRemindByEmail.error')
    } else {
      changeValue( user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO),
              'isRemindByEmail',
              "N",
              'profile.updateProfile.updated.isRemindByEmail' )
    }

    //Error: EmailCCReminder without EmailReminder
    if (user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).equals(RDStore.YN_NO) && params.isRemindCCByEmail){
        flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.isRemindByEmailNotChecked')
    } else {
        if ( params.isRemindCCByEmail && ( ! params.remindCCEmailaddress) ) {
            flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.noCCEmailAddressError')
        } else {
            if (params.remindCCEmailaddress == null || params.remindCCEmailaddress.trim() == '' || formService.validateEmailAddress(params.remindCCEmailaddress)){
                changeValue( user.getSetting(KEYS.REMIND_CC_EMAILADDRESS, null),
                        'remindCCEmailaddress',
                        null,
                        'profile.updateProfile.updated.remindCCEmailaddress' )
            } else {
                flash.error += message(code:'profile.updateProfile.updated.email.error')
            }
            changeValue( user.getSetting(KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO),
                    'isRemindCCByEmail',
                    "N",
                    'profile.updateProfile.updated.isRemindCCByEmail' )
        }
    }

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, RDStore.YN_NO),
                'isSubscriptionsNoticePeriod',
                "N",
                'profile.updateProfile.updated.subscriptions.noticePeriod' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_NO),
                'isSubscriptionsEnddate',
                "N",
                'profile.updateProfile.updated.subscriptions.enddate' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_NO),
                'isSubscriptionsCustomProp',
                "N",
                'profile.updateProfile.updated.subscriptions.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_NO),
                'isSubscriptionsPrivateProp',
                "N",
                'profile.updateProfile.updated.subscriptions.privateProperty')

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_NO),
                'isLicenseCustomProp',
                "N",
                'profile.updateProfile.updated.license.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_NO),
                'isLicensePrivateProp',
                "N",
                'profile.updateProfile.updated.license.privateProperty')

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_NO),
                'isOrgCustomProp',
                "N",
                'profile.updateProfile.updated.org.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_NO),
                'isOrgPrivateProp',
                "N",
                'profile.updateProfile.updated.org.privateProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_NO),
                'isPersonPrivateProp',
                "N",
                'profile.updateProfile.updated.person.privateProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_NO),
                'isTasks',
                "N",
                'profile.updateProfile.updated.tasks' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, RDStore.YN_NO),
                'isSurveysNotMandatoryEndDate',
                "N",
                'profile.updateProfile.updated.surveysEndDate' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_NO),
                'isSurveysMandatoryEndDate',
                "N",
                'profile.updateProfile.updated.surveysMandatoryEndDate' )

        user.save()

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def updateNotificationSettings() {
        User user = contextService.getUser()

        flash.message = ""
        flash.error = ""

        //Error: Emailreminder without Emailaddress
        if ( (! user.email) && params.isNotificationByEmail) {
            flash.error += message(code:'profile.updateProfile.updated.isNotificationByEmail.error')
        } else {
            changeValue( user.getSetting(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO),
                    'isNotificationByEmail',
                    "N",
                    'profile.updateProfile.updated.isNotificationByEmail' )
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
                        changeValue( user.getSetting(KEYS.NOTIFICATION_CC_EMAILADDRESS, null),
                                'notificationCCEmailaddress',
                                null,
                                'profile.updateProfile.updated.notificationCCEmailaddress' )
                    } else {
                        flash.error += message(code:'profile.updateProfile.updated.email.error')
                    }
                }
                changeValue( user.getSetting(KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO),
                        'isNotificationCCByEmail',
                        "N",
                        'profile.updateProfile.updated.isNotificationCCByEmail' )
            }
        }

        changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_NO),
                'isNotificationForSurveysStart',
                "N",
                'profile.updateProfile.updated.surveysStart' )

        changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_NO),
                'isNotificationForSystemMessages',
                "N",
                'profile.updateProfile.updated.systemMessages' )

        changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_NO),
                'isNotificationForSurveysParticipationFinish',
                "N",
                'profile.updateProfile.updated.surveysParticipationFinish' )

        user.save()

        redirect(action: "index")
    }

    private void changeValue(UserSetting userSetting, String key, String fallbackValue, String messageSuccessfull) {

        if (! params.containsKey(key) && ! fallbackValue) {
            log.debug(userSetting.key.toString() + ' ignored; because ' + key + ' not sent and no fallback given')
            return
        }

        def newValue = (params.get(key) != null) ? params.get(key) : fallbackValue
        def oldValue = userSetting.value

        // println key + ' : ' + newValue + ' <----' + params.get(key) + ' || ' + fallbackValue

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
                flash.error += (message(code: 'profile.updateProfile.updated.error.dashboardReminderPeriod') + "<br/>")
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
    @Transactional
    def updateIsRemindByEmail() {
        User user = contextService.getUser()

        flash.message=""
        def was_isRemindByEmail = user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO)
        if ( was_isRemindByEmail != params.isRemindByEmail ) {
            was_isRemindByEmail = params.isRemindByEmail
            flash.message += message(code:'profile.updateProfile.updated.isRemindByEmail')
            if ( ! user.email && was_isRemindByEmail.equals(RDStore.YN_YES)) {
                flash.error = message(code:'profile.updateProfile.updated.isRemindByEmail.error')
            }
        }
        user.save()

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def updatePassword() {
        User user = contextService.getUser()
        flash.message = ""

        if (passwordEncoder.isPasswordValid(user.password, params.passwordCurrent, null)) {
            if (params.passwordNew.trim().size() < 5) {
                flash.message += message(code:'profile.password.update.enterValidNewPassword')
            }
            else {
                user.password = params.passwordNew

                if (user.save()) {
                    flash.message += message(code:'profile.password.update.success')
                }
            }
        }
        else {
            flash.error = message(code:'profile.password.update.enterValidCurrentPassword')
        }
        redirect(action: "index")
    }
}
