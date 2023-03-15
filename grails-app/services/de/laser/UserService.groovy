package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.auth.UserRole
import de.laser.config.ConfigMapper
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.mvc.FlashScope
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

/**
 * This service handles generic user-related matters
 */
@Transactional
class UserService {

    ContextService contextService
    GenericOIDService genericOIDService
    InstAdmService instAdmService
    MessageSource messageSource

    /**
     * This method is called after every successful login and checks if mandatory settings have been made for the given user.
     * If the settings are missing, they will be created to defaults which the user may configure afterwards
     * @param user the user whose presets should be verified
     */
    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)

        def uss = UserSetting.get(user, UserSetting.KEYS.DASHBOARD)

        List<Long> userOrgMatches = user.getAffiliationOrgsIdList()
        if (userOrgMatches.size() > 0) {
            Org firstOrg = Org.findById(userOrgMatches.first()) //we presume that (except my test ladies) no one can be simultaneously member of a consortia and of a single user
            if (uss == UserSetting.SETTING_NOT_FOUND) {
                user.getSetting(UserSetting.KEYS.DASHBOARD, firstOrg)
            }
            else if (! uss.getValue()) {
                uss.setValue(firstOrg)
            }
            if(firstOrg.isCustomerType_Inst())
                user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH, RDStore.YN_YES)
        }

        user.getSetting(UserSetting.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSetting.KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE, RDStore.YN_YES)
        user.getSetting(UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE, UserSetting.DEFAULT_REMINDER_PERIOD)

        user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_YES)
        user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, RDStore.YN_YES)
    }

    /**
     * Lists the users matching the given request parameters
     * @param params the request parameter map
     * @return a list of users, either globally or belonging to a given institution
     */
    Set<User> getUserSet(Map params) {
        // only context org depending
        List baseQuery = ['select distinct u from User u']
        List whereQuery = []
        Map queryParams = [:]

        if (params.org || params.authority) {
            baseQuery.add( 'UserOrg uo' )

            if (params.org) {
                whereQuery.add( 'uo.user = u and uo.org = :org' )
                queryParams.put( 'org', genericOIDService.resolveOID(params.org) )
            }
            if (params.role) {
                whereQuery.add( 'uo.user = u and uo.formalRole = :role' )
                queryParams.put('role', genericOIDService.resolveOID(params.role) )
            }
        }

        if (params.name && params.name != '' ) {
            whereQuery.add( '(genfunc_filter_matcher(u.username, :name) = true or genfunc_filter_matcher(u.display, :name) = true)' )
            queryParams.put('name', "%${params.name.toLowerCase()}%")
        }
        String query = baseQuery.join(', ') + (whereQuery ? ' where ' + whereQuery.join(' and ') : '') + ' order by u.username'
        User.executeQuery(query, queryParams /*,params */)
    }

    /**
     * Inserts a new user account with the given parameters
     * @param params the parameters specifying the account details
     * @param flash the message container
     * @return the new user object or the error messages (null) in case of failure
     */
    User addNewUser(Map params, FlashScope flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        User user = new User(params)
        user.enabled = true

        if (! user.save()) {
            Set errMess = []
            Object[] withArticle = new Object[messageSource.getMessage('user.withArticle.label',null,locale)]
            user.errors.fieldErrors.each { FieldError e ->
                if(e.field == 'username' && e.code == 'unique')
                    errMess.add(messageSource.getMessage('user.not.created.message',null,locale))
                else errMess.add(messageSource.getMessage('default.not.created.message',withArticle,locale))
            }
            errMess
        }

        log.debug("created new user: " + user)

        UserRole defaultRole = new UserRole(user: user, role: Role.findByAuthority('ROLE_USER'))
        defaultRole.save()

        if (params.org && params.formalRole) {
            Org org = Org.get(params.org)
            Role formalRole = Role.get(params.formalRole)

            if (org && formalRole) {
                int existingUserOrgs = UserOrg.findAllByOrgAndFormalRole(org, formalRole).size()

                instAdmService.createAffiliation(user, org, formalRole, flash)

                if (formalRole.authority == 'INST_ADM' && existingUserOrgs == 0 && ! org.legallyObligedBy) { // only if new instAdm
                    if (UserOrg.findByOrgAndUserAndFormalRole(org, user, formalRole)) { // only on success
                        org.legallyObligedBy = contextService.getOrg()
                        org.save()
                        log.debug("set legallyObligedBy for ${org} -> ${contextService.getOrg()}")
                    }
                }
                user.getSetting(UserSetting.KEYS.DASHBOARD, org)
                user.getSetting(UserSetting.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))
            }
        }

        user
    }

    /**
     * Checks whether the arguments are set to link the given user to the given institution, gets the institution
     * and formal role objects and hands the call to the {@link InstAdmService} to perform linking
     * @param user the user to link
     * @param orgId the institution ID to which the user should be linked
     * @param formalRoleId the ID of the role to attribute to the given user
     * @param flash the message container
     */
    def addAffiliation(User user, orgId, formalRoleId, flash) {
        Org org = Org.get(orgId)
        Role formalRole = Role.get(formalRoleId)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, flash)
        }
    }

    /**
     * Checks the user's permissions in the given institution
     * @param user the user to check
     * @param userRoleName the user's role (permission grant) in the institution to be checked
     * @param globalRoleName the (eventual) global permission which may override local permissions
     * @param mode AND: local affiliation is necessary even if global roles are granted, OR: global roles override missing local affiliation
     * @param orgToCheck the institution to which affiliation should be checked
     * @return true if the given permission is granted to the user in the given institution (or a missing one overridden by global roles), false otherwise
     */
    boolean checkAffiliation(User user, String userRoleName, String globalRoleName, String mode, Org orgToCheck) {

        boolean check = false

        // TODO:

        if (SpringSecurityUtils.ifAnyGranted("ROLE_YODA")) {
            check = true // may the force be with you
        }
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            check = true // may the force be with you
        }

        if (mode == 'AND') {
            if (! SpringSecurityUtils.ifAnyGranted(globalRoleName)) {
                check = false // min restriction fail
            }
        }
        else if (mode == 'OR') {
            if (SpringSecurityUtils.ifAnyGranted(globalRoleName)) {
                check = true // min level granted
            }
        }

        // TODO:

        if (! check) {
            List<String> rolesToCheck = [userRoleName]

            // handling role hierarchy
            if (userRoleName == "INST_USER") {
                rolesToCheck << "INST_EDITOR"
                rolesToCheck << "INST_ADM"
            }
            else if (userRoleName == "INST_EDITOR") {
                rolesToCheck << "INST_ADM"
            }

            rolesToCheck.each { String rot ->
                Role role = Role.findByAuthority(rot)
                if (role) {
                    UserOrg uo = UserOrg.findByUserAndOrgAndFormalRole(user, orgToCheck, role)
                    //for users with multiple affiliations, login fails because of LazyInitializationException of the domain collection
                    List<UserOrg> affiliations = UserOrg.findAllByUser(user)
                    check = check || (uo && affiliations.contains(uo))
                }
            }
        }

        //TODO: log.debug("affiliationCheck(): ${user} - ${userRoleName}, ${globalRoleName}, ${mode} @ ${orgToCheck} -> ${check}")
        check
    }

    /**
     * This setup was used only for QA in order to create test accounts for the hbz employees. Is disused as everyone should start from scratch when using the system
     * @param orgs the configuration {@link Map} containing the affiliation configurations to process
     */
    @Deprecated
    void setupAdminAccounts(Map<String,Org> orgs) {
        List adminUsers = ConfigMapper.getConfig('adminUsers', List) as List
        List<String> customerTypes = ['konsorte','vollnutzer','konsortium']
        Map<String,Role> userRights = ['benutzer':Role.findByAuthority('INST_USER'), //internal 'Anina'
                                       'redakteur':Role.findByAuthority('INST_EDITOR'), //internal 'Rahel'
                                       'admin':Role.findByAuthority('INST_ADM')] //internal 'Viola'

        adminUsers.each { adminUser ->
            customerTypes.each { String customerKey ->
                userRights.each { String rightKey, Role userRole ->
                    String username = "${adminUser.name}_${customerKey}_${rightKey}"
                    User user = User.findByUsername(username)

                    if(! user) {
                        log.debug("trying to create new user: ${username}")
                        user = addNewUser([username: username, password: "${adminUser.pass}", display: username, email: "${adminUser.email}", enabled: true, org: orgs[customerKey]],null)

                        if (user && orgs[customerKey]) {
                            if (! user.hasAffiliationForForeignOrg(rightKey, orgs[customerKey])) {

                                instAdmService.createAffiliation(user, orgs[customerKey], userRole, null)
                                user.getSetting(UserSetting.KEYS.DASHBOARD, orgs[customerKey])
                            }
                        }
                    }
                }
            }
        }
    }
}
