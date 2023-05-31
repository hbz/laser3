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

        if (user.formalOrg) {
            if (uss == UserSetting.SETTING_NOT_FOUND) {
                user.getSetting(UserSetting.KEYS.DASHBOARD, user.formalOrg)
            }
            else if (! uss.getValue()) {
                uss.setValue(user.formalOrg)
            }
            if(user.formalOrg.isCustomerType_Inst())
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
            if (params.org) {
                whereQuery.add( 'u.formalOrg = :org' )
                queryParams.put( 'org', genericOIDService.resolveOID(params.org) )
            }
            // params.authority vs params.role ???
            if (params.role) {
                whereQuery.add( 'u.formalRole = :role' )
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
                    if (user.hasRoleForOrg(formalRole, formalOrg)) { // only on success
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
                    check = check || userToCheck.hasRoleForOrg(role, orgToCheck)
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
            result = result || userToCheck.hasRoleForOrg(Role.findByAuthority(rot), orgToCheck)
        }
        result
    }
}
