package de.laser


import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserRole
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.mvc.FlashScope
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

/**
 * This service handles generic user-related matters
 */
@Transactional
class UserService {

    ContextService contextService
    MessageSource messageSource

    User getUserByUsername(String username) {
        ConfigObject cfg = SpringSecurityUtils.securityConfig
        User user = cfg.userLookup.usernameIgnoreCase ? User.findByUsernameIlike(username) : User.findByUsername(username)
        user
    }

    List<User> getAllUsersByEmail(String username) {
        ConfigObject cfg = SpringSecurityUtils.securityConfig
        List<User> users = cfg.userLookup.usernameIgnoreCase ? User.findAllByEmailIlike(username) : User.findAllByEmail(username)
        users
    }

    /**
     * This method is called after every successful login and checks if mandatory settings have been made for the given user.
     * If the settings are missing, they will be created to defaults which the user may configure afterwards
     * @param user the user whose presets should be verified
     */
    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)

        if (user.formalOrg) {
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
    Map<String, Object> getUserMap(GrailsParameterMap params) {
        // only context org depending
        String baseQuery = 'select distinct u from User u'
        List whereQuery = []
        Map queryParams = [:]

        if (params.org || params.role || params.authority) {
            if (params.org) {
                whereQuery.add( 'u.formalOrg != null and u.formalOrg.id = :org' )
                queryParams.put('org', params.long('org'))
            }
            if (params.role) {
                whereQuery.add( 'u.formalRole != null and u.formalRole.id = :role' )
                queryParams.put('role', params.long('role'))
            }
            if (params.authority) {
                whereQuery.add( 'u.formalRole != null and u.formalRole.authority = :authority' )
                queryParams.put('authority', params.authority)
            }
        }

        if (params.status) {
            if (params.status == 'expired') {
                whereQuery.add( 'u.accountExpired = true' )
            }
            else if (params.status == 'locked') {
                whereQuery.add( 'u.accountLocked = true' )
            }
            else if (params.status == 'disabled') {
                whereQuery.add( 'u.enabled = false' )
            }
            else if (params.status == 'enabled') {
                whereQuery.add( 'u.enabled = true' )
            }
        }

        if (params.name && params.name != '' ) {
            whereQuery.add('(genfunc_filter_matcher(u.username, :name) = true or genfunc_filter_matcher(u.display, :name) = true)')
            queryParams.put('name', params.name)
        }
        String query = baseQuery + (whereQuery ? ' where ' + whereQuery.join(' and ') : '') + ' order by u.username',
        countQuery = 'select count(distinct(u)) from User u' + (whereQuery ? ' where ' + whereQuery.join(' and ') : '')

        [count: User.executeQuery(countQuery, queryParams)[0], data: User.executeQuery(query, queryParams, [max: params.max, offset: params.offset])]
    }

    /**
     * Inserts a new user account with the given parameters
     * @param params the parameters specifying the account details
     * @param flash the message container
     * @return the new user object or the error messages (null) in case of failure
     */
    def addNewUser(GrailsParameterMap params, FlashScope flash) {
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
                setAffiliation(user, formalOrg.id, formalRole.id, flash)

                if (formalRole.authority == 'INST_ADM' && existingUserOrgs == 0 && ! formalOrg.legallyObligedBy) { // only if new instAdm
                    if (user.isFormal(formalRole, formalOrg)) { // only on success
                        formalOrg.legallyObligedBy = contextService.getOrg()
                        formalOrg.save()
                        log.debug("set legallyObligedBy for ${formalOrg} -> ${contextService.getOrg()}")
                    }
                }
                user.getSetting(UserSetting.KEYS.DASHBOARD_TAB, RDStore.US_DASHBOARD_TAB_SURVEYS)
            }
        }
        user
    }

    /**
     * Links the given user to the given institution with the given role
     * @param user the user to link
     * @param formalOrgId the institution ID to which the user should be linked
     * @param formalRoleId the ID of the role to attribute to the given user
     * @param flash the message container
     */
    void setAffiliation(User user, Serializable formalOrgId, Serializable formalRoleId, FlashScope flash) {
        boolean success = false // instAdmService.setAffiliation(user, formalOrg, formalRole, flash)

        try {
            Org formalOrg   = Org.get(formalOrgId)
            Role formalRole = Role.get(formalRoleId)

            // TODO - handle formalOrg change
            if (user && formalOrg && formalRole ) {
                if (user.formalOrg?.id != formalOrg.id) {
                    user.formalOrg = formalOrg
                    user.formalRole = formalRole

                    if (user.save()) {
                        success = true
                    }
                }
            }
        }
        catch (Exception e) {}

        if (flash) {
            Locale loc = LocaleUtils.getCurrentLocale()

            if (success) {
                flash.message = messageSource.getMessage('user.affiliation.request.success', null, loc)
            }
            else {
                flash.error = messageSource.getMessage('user.affiliation.request.failed', null, loc)
            }
        }
    }

    /**
     * Checks the current user's permissions in the given institution
     * @param instUserRole the user's role (permission grant) in the institution to be checked
     * @param orgToCheck the institution to which affiliation should be checked
     * @return true if the given permission is granted to the user in the given institution, false otherwise
     */
    boolean hasAffiliation(Org orgToCheck, String instUserRole) {
        if (! SpringSecurityUtils.ifAnyGranted('ROLE_USER')) {
            return false // min restriction fail
        }
        _checkUserOrgRole(contextService.getUser(), orgToCheck, instUserRole)
    }

    /**
     * Checks the current user's permissions in the given institution
     * @param instUserRole the user's role (permission grant) in the institution to be checked
     * @param orgToCheck the institution to which affiliation should be checked
     * @return true if the given permission is granted to the user in the given institution which is also the context institution, false otherwise
     */
    boolean hasFormalAffiliation(Org orgToCheck, String instUserRole) {
        User userToCheck = contextService.getUser()

        if (! userToCheck || ! orgToCheck) {
            return false
        }
        if (orgToCheck.id != contextService.getOrg().id) { // NEW CONSTRAINT
            return false
        }
        _checkUserOrgRole(userToCheck, orgToCheck, instUserRole)
    }

    /**
     * Checks if the given institution role is granted to the given user at the given institution. The role
     * may be granted implicitly by another role
     * @param userToCheck the user to check
     * @param orgToCheck the institution to which the user is belonging
     * @param instUserRole the role to check
     * @return true if the given role is granted to the user, false otherwise
     */
    private boolean _checkUserOrgRole(User userToCheck, Org orgToCheck, String instUserRole) {
        boolean check = false

        List<String> rolesToCheck = [instUserRole]

        // handling inst role hierarchy
        if (instUserRole == Role.INST_USER) {
            rolesToCheck << Role.INST_EDITOR
            rolesToCheck << Role.INST_ADM
        }
        else if (instUserRole == Role.INST_EDITOR) {
            rolesToCheck << Role.INST_ADM
        }

        rolesToCheck.each { String rtc ->
            if (!check) {
                Role role = Role.findByAuthority(rtc)
                if (role) {
                    check = userToCheck.isFormal(role, orgToCheck)
                }
            }
        }
        //TODO: log.debug("_checkUserOrgRole(): ${userToCheck} ${orgToCheck} ${instUserRole} -> ${check}")
        check
    }

    // -- todo: check logic

    /**
     * Checks if the current user belongs as administrator to an institution linked to the given consortium
     * @param org the consortium ({@link Org}) whose institutions should be checked
     * @return true if there is at least one institution belonging to the given consortium for which the user has {@link Role#INST_ADM} rights
     */
    boolean hasComboInstAdmPivileges(Org org) {
        boolean result = hasFormalAffiliation(org, 'INST_ADM')

        List<Org> topOrgs = Org.executeQuery(
                'select c.toOrg from Combo c where c.fromOrg = :org and c.type = :type', [org: org, type: RDStore.COMBO_TYPE_CONSORTIUM]
        )
        topOrgs.each { top ->
            if (hasFormalAffiliation(top as Org, 'INST_ADM')) {
                result = true
            }
        }
        result
    }

    // -- todo: check logic

    /**
     * Checks if the given user can be edited by current user
     * @param user the user who should be edited
     * @return true if the user is an {@link Role#INST_ADM} of a consortium to which the target institution is belonging, a consortium administrator at all or a superadmin, false otherwise
     */
    boolean isUserEditableForInstAdm(User user) {
        if (user.formalOrg) {
            // User editor = contextService.getUser()
            // return hasComboInstAdmPivileges(editor, user.formalOrg)
            return hasComboInstAdmPivileges(user.formalOrg)
        }
        else {
            return SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || contextService.isInstAdm_denySupport(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
        }
    }
}
