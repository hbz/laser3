package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserRole
import de.laser.config.ConfigMapper
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
            baseQuery.add( 'UserOrgRole uo' )

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
            Org formalOrg   = Org.get(params.org)
            Role formalRole = Role.get(params.formalRole)

            if (formalOrg && formalRole) {
                int existingUserOrgs = User.findAllByFormalOrgAndFormalRole(formalOrg, formalRole).size()

//                instAdmService.createAffiliation(user, formalOrg, formalRole, flash) // TODO refactoring
                instAdmService.setAffiliation(user, formalOrg, formalRole, flash)

                if (formalRole.authority == 'INST_ADM' && existingUserOrgs == 0 && ! formalOrg.legallyObligedBy) { // only if new instAdm
                    if (User.findByIdAndFormalOrgAndFormalRole(user.id, formalOrg, formalRole)) { // only on success
                        formalOrg.legallyObligedBy = contextService.getOrg()
                        formalOrg.save()
                        log.debug("set legallyObligedBy for ${formalOrg} -> ${contextService.getOrg()}")
                    }
                }
                user.getSetting(UserSetting.KEYS.DASHBOARD, formalOrg)
                user.getSetting(UserSetting.KEYS.DASHBOARD_TAB, RDStore.US_DASHBOARD_TAB_DUE_DATES)
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
    def setAffiliation(User user, Serializable formalOrgId, Serializable formalRoleId, flash) {
        Org formalOrg   = Org.get(formalOrgId)
        Role formalRole = Role.get(formalRoleId)

        if (user && formalOrg && formalRole) {
            instAdmService.setAffiliation(user, formalOrg, formalRole, flash)
        }
    }

    /**
     * Checks the user's permissions in the given institution
     * @param userToCheck the user to check
     * @param instUserRole the user's role (permission grant) in the institution to be checked
     * @param orgToCheck the institution to which affiliation should be checked
     * @return true if the given permission is granted to the user in the given institution (or a missing one overridden by global roles), false otherwise
     */
    boolean checkAffiliation_or_ROLEADMIN(User userToCheck, Org orgToCheck, String instUserRole) {
        boolean check = false

        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            check = true // may the force be with you
        }
        if (! SpringSecurityUtils.ifAnyGranted('ROLE_USER')) {
            check = false // min restriction fail
        }

        // TODO:

        if (! check) {
            List<String> rolesToCheck = [instUserRole]

            // handling inst role hierarchy
            if (instUserRole == Role.INST_USER) {
                rolesToCheck << Role.INST_EDITOR
                rolesToCheck << Role.INST_ADM
            }
            else if (instUserRole == Role.INST_EDITOR) {
                rolesToCheck << Role.INST_ADM
            }

            rolesToCheck.each { String rot ->
                Role role = Role.findByAuthority(rot)
                if (role) {
//                    UserOrgRole uo = UserOrgRole.findByUserAndOrgAndFormalRole(userToCheck, orgToCheck, role)
//                    //for users with multiple affiliations, login fails because of LazyInitializationException of the domain collection
//                    List<UserOrgRole> affiliations = UserOrgRole.findAllByUser(userToCheck)
//                    check = check || (uo && affiliations.contains(uo))

                    check = check || User.findByIdAndFormalOrgAndFormalRole(userToCheck.id, orgToCheck, role) // TODO refactoring
                }
            }
        }

        //TODO: log.debug("checkAffiliation_or_ROLEADMIN(): ${user} ${orgToCheck} ${instUserRole} -> ${check}")
        check
    }

    boolean checkAffiliationAndCtxOrg_or_ROLEADMIN(User userToCheck, Org orgToCheck, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }

        checkAffiliationAndCtxOrg(userToCheck, orgToCheck, instUserRole)
    }

    boolean checkAffiliationAndCtxOrg(User userToCheck, Org orgToCheck, String instUserRole) {
        boolean result = false

        if (! userToCheck || ! orgToCheck) {
            return result
        }
        // NEW CONSTRAINT:
        if (orgToCheck.id != contextService.getOrg().id) {
            return result
        }

        List<String> rolesToCheck = [instUserRole]

        // handling inst role hierarchy
        if (instUserRole == Role.INST_USER) {
            rolesToCheck << Role.INST_EDITOR
            rolesToCheck << Role.INST_ADM
        }
        else if (instUserRole == Role.INST_EDITOR) {
            rolesToCheck << Role.INST_ADM
        }

        rolesToCheck.each { String rot ->
//            Role role = Role.findByAuthority(rot)
//            UserOrgRole userOrg = UserOrgRole.findByUserAndOrgAndFormalRole(userToCheck, orgToCheck, role)
            result = result || (User.findByIdAndFormalOrgAndFormalRole(userToCheck.id, orgToCheck, Role.findByAuthority(rot)))  // TODO refactoring
        }
        result
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
                            if (! user.hasOrgAffiliation_or_ROLEADMIN(orgs[customerKey], rightKey)) {

                                instAdmService.setAffiliation(user, orgs[customerKey], userRole, null) // TODO refactoring
                                user.getSetting(UserSetting.KEYS.DASHBOARD, orgs[customerKey])
                            }
                        }
                    }
                }
            }
        }
    }
}
