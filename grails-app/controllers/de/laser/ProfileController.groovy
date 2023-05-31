package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.UserSetting.KEYS
import de.laser.utils.LocaleUtils
import de.laser.utils.PasswordUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.context.MessageSource

/**
 * This controller manages calls to user profiles
 * @see User
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {

    ContextService contextService
    DeletionService deletionService
    FormService formService
    GenericOIDService genericOIDService
    InstAdmService instAdmService
    MessageSource messageSource
    def passwordEncoder
    PropertyService propertyService
    RefdataService refdataService

    /**
     * Call to the current session user's profile
     */
    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.editable = true
        result.isOrgBasicMember = contextService.getOrg().isCustomerType_Inst_Basic()
        result.availableOrgs  = Org.executeQuery('from Org o where o.sector = :sector order by o.sortname', [sector: RDStore.O_SECTOR_HIGHER_EDU])
        result.availableOrgRoles = Role.findAllByRoleType('user')

        result
    }

    /**
     * Call to the help page with a collection of basic guidelines to the app
     */
    @Secured(['ROLE_USER'])
    def help() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result
    }

    /**
     * Call for a listing of public properties and reference data values, i.e. an overview of the controlled lists
     */
    @Secured(['ROLE_USER'])
    def properties() {
        Map<String, Object> propDefs = [:]

        Locale locale = LocaleUtils.getCurrentLocale()
        String i10nAttr = locale.getLanguage() == Locale.GERMAN.getLanguage() ? 'name_de' : 'name_en'
        List usedPdList = []
        List usedRdvList = []
        List refdatas = []

        params.tab = params.tab ?: 'propertyDefinitions'

        if(params.tab == 'propertyDefinitions'){
            String[] custPropDefs = PropertyDefinition.AVAILABLE_CUSTOM_DESCR.sort {a, b ->
                messageSource.getMessage("propertyDefinition.${a}.label", null, locale) <=> messageSource.getMessage("propertyDefinition.${b}.label", null, locale)
            }
            custPropDefs.each { String it ->
                List<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: i10nAttr]) // NO private properties!
                propDefs.putAt( it, itResult )
            }
            usedPdList = propertyService.getUsageDetails()
        }else{
            usedRdvList = refdataService.getUsageDetails()
            i10nAttr = locale.getLanguage() == Locale.GERMAN.getLanguage() ? 'desc_de' : 'desc_en'
            println(i10nAttr)
            refdatas = RefdataCategory.executeQuery("from RefdataCategory order by ${i10nAttr}")
        }

        render view: 'properties', model: [
                editable    : false,
                propertyDefinitions: propDefs,
                rdCategories: refdatas,
                usedRdvList : usedRdvList,
                usedPdList  : usedPdList
        ]
    }

    /**
     * Call to open the GDPR statement page
     */
    @Secured(['ROLE_USER'])
    def dsgvo() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result
    }

    /**
     * Call to add the given user to the given institution
     * @return the profile page
     */
    @Secured(['ROLE_USER'])
    def setAffiliation() {
        log.debug("setAffiliation() org: ${params.formalOrg} role: ${params.formalRole}")
        User user       = contextService.getUser()
        Org formalOrg   = Org.get(params.formalOrg)
        Role formalRole = Role.get(params.formalRole)

        if (user && formalOrg && formalRole) {
            instAdmService.setAffiliation(user, formalOrg, formalRole, flash)
        }
        redirect(action: "index")
    }

    /**
     * Removes the given user from the given institution
     * @return the profile page
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def deleteAffiliation() {
        log.debug("deleteAffiliation() userOrg: ${params.assoc}")
        User user        = contextService.getUser()
//        UserOrgRole userOrg  = UserOrgRole.findByUserAndId(user, params.assoc)
//
//        if (userOrg) {
//            userOrg.delete()
//        }
        // todo refactoring
        redirect(action: "index")
    }

    /**
     * Calls the user deletion page or, if confirmed, deletes the given user and executes the substitutions
     * @return either the logout redirect or the deletion view, if an error occurred
     */
    @Secured(['ROLE_USER'])
    def delete() {
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

        result.substituteList = result.user.formalOrg ? User.executeQuery(
                'select u from User u where u.formalOrg = :org and u != :self and u.formalRole = :instAdm order by u.username',
                [org: result.user.formalOrg, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
        ) : []

        render view: 'delete', model: result
    }

    /**
     * Takes the submitted parameters and updates the current profile with the given parameter map
     * @return redirect to the updated profile view
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def updateProfile() {

        User user = contextService.getUser()
        flash.message=""

        if ( user.display != params.profile_display ) {
            user.display = params.profile_display
            flash.message += message(code:'profile.updateProfile.updated.name')
        }

        if ( user.email != params.profile_email ) {
            if ( formService.validateEmailAddress(params.profile_email) ) {
                user.email = params.profile_email
                flash.message += message(code:'profile.updateProfile.updated.email')
            }
            else {
                flash.error = message(code:'profile.updateProfile.updated.email.error') as String
            }
        }

        user.save()

        if (params.profile_dashboard) {
            Org org = (Org) genericOIDService.resolveOID(params.profile_dashboard)
            UserSetting us = user.getSetting(KEYS.DASHBOARD, null)

            if (org?.id != us.getValue()?.id) {
                us.setValue(org)
                flash.message += message(code: 'profile.updateProfile.updated.dash')
            }
        }

        redirect(action: "index")
    }

    /**
     * Takes the submitted parameters and updates the user settings with the given parameter map
     * @return the updated user profile view
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def updateReminderSettings() {
        User user = contextService.getUser()

        flash.message = ""
        flash.error = ""

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicensePrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForLicensePrivateProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicenseCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForLicenseCustomProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForOrgCustomProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForOrgPrivateProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForPersonPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForPersonPrivateProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionsCustomProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionsPrivateProp' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionNoticeperiod',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionNoticeperiod' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionEnddate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionEnddate' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_TASKS, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForTasks',
                        null,
                        'profile.updateProfile.token.remindPeriodForTasks' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysEndDate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSurveyEndDate' )

        _changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysMandatoryEndDate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSurveyMandatoryEndDate' )

    //Error: Emailreminder without Emailaddress
    if ( (! user.email) && params.isRemindByEmail) {
      flash.error += message(code:'profile.updateProfile.updated.isRemindByEmail.error')
    } else {
      _changeValue( user.getSetting(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO),
              'isRemindByEmail',
              "N",
              'profile.updateProfile.updated.isRemindByEmail' )
    }

    //Error: EmailCCReminder without EmailReminder
    if (user.getSettingsValue(KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO).equals(RDStore.YN_NO) && params.isRemindCCByEmail){
        flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.isRemindByEmailNotChecked')
    } else {
        if ( params.isRemindCCByEmail && ( ! params.remindCCEmailaddress) ) {
            flash.error += message(code:'profile.updateProfile.updated.isRemindCCByEmail.noCCEmailAddressError')
        } else {
            if (params.remindCCEmailaddress == null || params.remindCCEmailaddress.trim() == '' || formService.validateEmailAddress(params.remindCCEmailaddress)){
                _changeValue( user.getSetting(KEYS.REMIND_CC_EMAILADDRESS, null),
                        'remindCCEmailaddress',
                        null,
                        'profile.updateProfile.updated.remindCCEmailaddress' )
            } else {
                flash.error += message(code:'profile.updateProfile.updated.email.error')
            }
            _changeValue( user.getSetting(KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO),
                    'isRemindCCByEmail',
                    "N",
                    'profile.updateProfile.updated.isRemindCCByEmail' )
        }
    }

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, RDStore.YN_NO),
                'isSubscriptionsNoticePeriod',
                "N",
                'profile.updateProfile.token.subscriptions.noticePeriod' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_NO),
                'isSubscriptionsEnddate',
                "N",
                'profile.updateProfile.token.subscriptions.enddate' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_NO),
                'isSubscriptionsCustomProp',
                "N",
                'profile.updateProfile.token.subscriptions.customProperty' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_NO),
                'isSubscriptionsPrivateProp',
                "N",
                'profile.updateProfile.token.subscriptions.privateProperty')

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_NO),
                'isLicenseCustomProp',
                "N",
                'profile.updateProfile.token.license.customProperty' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_NO),
                'isLicensePrivateProp',
                "N",
                'profile.updateProfile.token.license.privateProperty')

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_NO),
                'isOrgCustomProp',
                "N",
                'profile.updateProfile.token.org.customProperty' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_NO),
                'isOrgPrivateProp',
                "N",
                'profile.updateProfile.token.org.privateProperty' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_NO),
                'isPersonPrivateProp',
                "N",
                'profile.updateProfile.token.person.privateProperty' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_NO),
                'isTasks',
                "N",
                'profile.updateProfile.token.tasks' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, RDStore.YN_NO),
                'isSurveysNotMandatoryEndDate',
                "N",
                'profile.updateProfile.token.surveysEndDate' )

        _changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_NO),
                'isSurveysMandatoryEndDate',
                "N",
                'profile.updateProfile.token.surveysMandatoryEndDate' )

        user.save()

        redirect(action: "index")
    }

    /**
     * Takes the submitted parameters and updates the user notification settings with the given parameter map
     * @return the updated profile view
     */
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
            _changeValue( user.getSetting(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO),
                    'isNotificationByEmail',
                    "N",
                    'profile.updateProfile.updated.isNotificationByEmail' )
        }

        //Error: EmailCCReminder without EmailReminder
        if (user.getSettingsValue(KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_NO).equals(RDStore.YN_NO) && params.isNotificationCCByEmail){
            flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.isNotificationByEmailNotChecked')
        } else {
            if ( params.isNotificationCCByEmail && ( ! params.notificationCCEmailaddress) ) {
                flash.error += message(code:'profile.updateProfile.updated.isNotificationCCByEmail.noCCEmailAddressError')
            } else {
                if (params.notificationCCEmailaddress){
                    if (formService.validateEmailAddress(params.notificationCCEmailaddress)){
                        _changeValue( user.getSetting(KEYS.NOTIFICATION_CC_EMAILADDRESS, null),
                                'notificationCCEmailaddress',
                                null,
                                'profile.updateProfile.updated.notificationCCEmailaddress' )
                    } else {
                        flash.error += message(code:'profile.updateProfile.updated.email.error')
                    }
                }
                _changeValue( user.getSetting(KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO),
                        'isNotificationCCByEmail',
                        "N",
                        'profile.updateProfile.updated.isNotificationCCByEmail' )
            }
        }

        _changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_NO),
                'isNotificationForSurveysStart',
                "N",
                'profile.updateProfile.token.surveysStart' )

        _changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_NO),
                'isNotificationForSystemMessages',
                "N",
                'profile.updateProfile.token.systemMessages' )

        String messageToken = ((contextService.getOrg().isCustomerType_Consortium()) ? 'profile.notification.for.SurveysParticipationFinish' : 'profile.notification.for.SurveysParticipationFinish2')

        _changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_NO),
                'isNotificationForSurveysParticipationFinish',
                "N",
                messageToken )

        user.save()

        redirect(action: "index")
    }

    /**
     * Builds the success / error messages and passes the setting update to the execution method
     * @param userSetting the updated setting value
     * @param key the key to be updated
     * @param fallbackValue a default value in case of failure
     * @param messageToken the message token to output to the user
     */
    private void _changeValue(UserSetting userSetting, String key, String fallbackValue, String messageToken) {

        String messageSuccess
        String messageError
        String arg = message(code: messageToken)

        if ( messageToken.contains('updateProfile.token.remindPeriodFor')) {
            messageSuccess = message(code: 'profile.updateProfile.reminderPeriod.success', args:[ "\'${arg}\'" ])
            messageError   = message(code: 'profile.updateProfile.reminderPeriod.error',   args:[ "\'${arg}\'" ])
        }
        else if ( messageToken.contains('updateProfile.token.')) {
            messageSuccess = message(code: 'profile.updateProfile.updated.success', args:[ "\'${arg}\'" ])
            messageError   = message(code: 'profile.updateProfile.updated.error',   args:[ "\'${arg}\'" ])
        }
        else {
            messageSuccess = arg
            messageError = arg
        }

        _changeValue( userSetting, key, fallbackValue, messageSuccess, messageError )
    }

    /**
     * Updates the given user setting with the given key to the given value; sets the fallback value if the
     * submitted value is invalid
     * @param userSetting the value to be set to the user setting
     * @param key the user setting key to be updated
     * @param fallbackValue the fallback value in case of invalid value
     * @param messageSuccess the success message to be rendered
     * @param messageError the error message to be rendered
     */
    private void _changeValue(UserSetting userSetting, String key, String fallbackValue, String messageSuccess, String messageError) {

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
                flash.error += ( messageError + "<br/>" )
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
            flash.message += ( messageSuccess + "<br/>" )
        }
    }

    /**
     * Checks if the submitted password is valid; if so, the user password is being updated with the given one
     * @return the updated profile view
     * @see PasswordUtils#USER_PASSWORD_REGEX
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def updatePassword() {
        User user = contextService.getUser()
        flash.message = ""

        if (passwordEncoder.matches(params.password_current, user.password)) {

            if (PasswordUtils.isUserPasswordValid(params.password_new)) {
                user.password = params.password_new

                if (user.save()) {
                    flash.message += message(code:'profile.password.update.success')
                }
            }
            else {
                flash.error = message(code:'profile.password.update.enterValidNewPassword') as String
            }
        }
        else {
            flash.error = message(code:'profile.password.update.enterValidCurrentPassword') as String
        }
        redirect(action: "index")
    }
}
