package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.UserSetting.KEYS
import de.laser.helper.PasswordUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

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
        result.isOrgBasicMember = contextService.getOrg().getCustomerType() == 'ORG_BASIC_MEMBER'
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

        Locale locale = LocaleContextHolder.getLocale()

        String[] custPropDefs = PropertyDefinition.AVAILABLE_CUSTOM_DESCR.sort {a, b ->
            messageSource.getMessage("propertyDefinition.${a}.label", null, locale) <=> messageSource.getMessage("propertyDefinition.${b}.label", null, locale)
        }

        String i10nAttr = locale.getLanguage() == Locale.GERMAN.getLanguage() ? 'name_de' : 'name_en'

        custPropDefs.each { String it ->
            List<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: i10nAttr]) // NO private properties!
            propDefs.putAt( it, itResult )
        }

        List usedRdvList = refdataService.getUsageDetails()
        List usedPdList = propertyService.getUsageDetails()

        i10nAttr = locale.getLanguage() == Locale.GERMAN.getLanguage() ? 'desc_de' : 'desc_en'

        render view: 'properties', model: [
                editable    : false,
                propertyDefinitions: propDefs,
                rdCategories: RefdataCategory.where{}.sort( i10nAttr ),
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
    def addAffiliation() {
        log.debug("addAffiliation() org: ${params.org} role: ${params.formalRole}")
        User user       = contextService.getUser()
        Org org         = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, flash)
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
        UserOrg userOrg  = UserOrg.findByUserAndId(user, params.assoc)

        if (userOrg) {
            userOrg.delete()
        }
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

        List<Org> orgList = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :self', [self: result.user])
        result.substituteList = orgList ? User.executeQuery(
                'select distinct u from User u join u.affiliations ua where ua.org in :orgList and u != :self and ua.formalRole = :instAdm order by u.username',
                [orgList: orgList, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
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

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicensePrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForLicensePrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForLicenseCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForLicenseCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForOrgCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForOrgPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForOrgPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForPersonPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForPersonPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsCustomProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionsCustomProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionsPrivateProp',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionsPrivateProp' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionNoticeperiod',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionNoticeperiod' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSubscriptionEnddate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSubscriptionEnddate' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_TASKS, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForTasks',
                        null,
                        'profile.updateProfile.token.remindPeriodForTasks' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysEndDate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSurveyEndDate' )

        changeValue( user.getSetting(KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD),
                        'remindPeriodForSurveysMandatoryEndDate',
                        null,
                        'profile.updateProfile.token.remindPeriodForSurveyMandatoryEndDate' )

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
                'profile.updateProfile.token.subscriptions.noticePeriod' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, RDStore.YN_NO),
                'isSubscriptionsEnddate',
                "N",
                'profile.updateProfile.token.subscriptions.enddate' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, RDStore.YN_NO),
                'isSubscriptionsCustomProp',
                "N",
                'profile.updateProfile.token.subscriptions.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, RDStore.YN_NO),
                'isSubscriptionsPrivateProp',
                "N",
                'profile.updateProfile.token.subscriptions.privateProperty')

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP, RDStore.YN_NO),
                'isLicenseCustomProp',
                "N",
                'profile.updateProfile.token.license.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, RDStore.YN_NO),
                'isLicensePrivateProp',
                "N",
                'profile.updateProfile.token.license.privateProperty')

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP, RDStore.YN_NO),
                'isOrgCustomProp',
                "N",
                'profile.updateProfile.token.org.customProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP, RDStore.YN_NO),
                'isOrgPrivateProp',
                "N",
                'profile.updateProfile.token.org.privateProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP, RDStore.YN_NO),
                'isPersonPrivateProp',
                "N",
                'profile.updateProfile.token.person.privateProperty' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_TASKS, RDStore.YN_NO),
                'isTasks',
                "N",
                'profile.updateProfile.token.tasks' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE, RDStore.YN_NO),
                'isSurveysNotMandatoryEndDate',
                "N",
                'profile.updateProfile.token.surveysEndDate' )

        changeValue( user.getSetting(KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_NO),
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
                'profile.updateProfile.token.surveysStart' )

        changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_NO),
                'isNotificationForSystemMessages',
                "N",
                'profile.updateProfile.token.systemMessages' )

        String messageToken = ((contextService.getOrg().getCustomerType()  == 'ORG_CONSORTIUM') ? 'profile.notification.for.SurveysParticipationFinish' : 'profile.notification.for.SurveysParticipationFinish2')

        changeValue( user.getSetting(KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_NO),
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
    private void changeValue(UserSetting userSetting, String key, String fallbackValue, String messageToken) {

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

        changeValue( userSetting, key, fallbackValue, messageSuccess, messageError )
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
    private void changeValue(UserSetting userSetting, String key, String fallbackValue, String messageSuccess, String messageError) {

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
     * Checks if the user has submitted an email address before updating the reminder setting,
     * updates the setting in case of success
     * @return the updated profile view
     */
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
                flash.error = message(code:'profile.updateProfile.updated.isRemindByEmail.error') as String
            }
        }
        user.save()

        redirect(action: "index")
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
