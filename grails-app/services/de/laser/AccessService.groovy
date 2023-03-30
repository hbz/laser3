package de.laser

import de.laser.auth.*
import de.laser.storage.RDConstants
import grails.gorm.transactions.Transactional
import org.springframework.web.context.request.RequestContextHolder

/**
 * This service manages access control checks
 */
@Transactional
class AccessService {

    static final CHECK_VIEW = 'CHECK_VIEW'
    static final CHECK_EDIT = 'CHECK_EDIT'
    static final CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'

    ContextService contextService

    /**
     * Test method
     */
    boolean test(boolean value) {
        value
    }

    // --- checks for contextService.getOrg() ---
    // --- checks for contextService.getOrg() ---

    /**
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    boolean checkCtxPerm(String orgPerms) {
        _checkOrgPermForForeignOrg(orgPerms.split(','), contextService.getOrg())
    }

    /**
     * Substitution call for {@link #_checkOrgPermAndUserAffiliation(java.lang.String[], java.lang.String)}
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the user permissions to check
     * @return true if the user has the permissions granted and his context institution is one of the given customer types, false otherwise
     */
    boolean checkCtxPermAffiliation(String orgPerms, String instUserRole) {
        _checkOrgPermAndUserAffiliation(orgPerms.split(','), instUserRole)
    }

    /**
     * Checks
     * <ul>
     *     <li>if the context institution is one of the given customer and organisation types and the user has the given rights granted</li>
     *     <li>or if the user has the given global rights granted</li>
     * </ul>
     * @param orgPerms customer type depending permissions to check against
     * @param orgTypes the organisation types to check
     * @param instUserRole the user's affiliation to the context institution
     * @return true if the user has one of the global permissions
     * or if the context institution is one of the given customer and organisation types
     * and if the user has the given permissions within the institution, false otherwise
     */
    boolean is_ROLE_ADMIN_or_checkPermTypeAffiliation(String orgPerms, String orgTypes, String instUserRole) {
        if (contextService.getUser()?.hasMinRole('ROLE_ADMIN')) {
            return true
        }
        _checkOrgPermAndOrgTypeAndUserAffiliation(orgPerms.split(','), orgTypes.split(','), instUserRole)
    }

    // --- public - simple naming scheme - orgToCheck
    // --- public - simple naming scheme - orgToCheck

    /**
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    boolean checkOrgPerm(Org orgToCheck, String orgPerms) {
        _checkOrgPermForForeignOrg(orgPerms.split(','), orgToCheck)
    }

    /**
     * Checks if
     * <ol>
     *     <li>the target institution is of the given customer type and the user has the given permissions granted at the target institution</li>
     *     <li>there is a combo relation to the given target institution</li>
     *     <li>or if the user has the given permissions granted at the context institution</li>
     * </ol>
     * @param attributes a configuration map:
     * [
     *      org: context institution,
     *      affiliation: user's rights at the context institution
     *      comboPerm: customer type of the target institution
     *      comboAffiliation: user's rights for the target institution
     * ]
     * @return true if clauses one and two or three succeed, false otherwise
     */
    boolean is_ROLE_ADMIN_or_checkOrgComboPermAffiliation(Org orgToCheck, String comboPerms, String comboAffiliation) {
        if (contextService.getUser()?.hasMinRole('ROLE_ADMIN')) {
            return true
        }
        Org ctx                 = contextService.getOrg()
        String orgPerms         = comboPerms
        String instUserRole     = comboAffiliation

        // combo check
        boolean check1 = _checkOrgPermAndUserAffiliation(orgPerms.split(','), instUserRole)
        boolean check2 = (ctx.id == orgToCheck.id) || Combo.findByToOrgAndFromOrg(ctx, orgToCheck)

        // orgToCheck check
        boolean check3 = (ctx.id == orgToCheck.id) && contextService.getUser()?.is_ROLE_ADMIN_or_hasAffiliation(null) // TODO: legacy - no affiliation given

        (check1 && check2) || check3
    }

    // --- private - complex naming scheme
    // --- private - complex naming scheme

    /**
     * Checks
     * <ul>
     *     <li>if the context institution is one of the given customer types and the user has the given rights granted</li>
     *     <li>or if the user has the given global rights granted</li>
     * </ul>
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the user's affiliation to the context institution
     * @return true if the user has one of the global permissions
     * or if the context institution is one of the given customer types
     * and if the user has the given permissions within the institution, false otherwise
     */
    private boolean _is_ROLE_ADMIN_or_checkOrgPermAndUserAffiliation(String orgPerms, String instUserRole) {
        if (contextService.getUser()?.hasMinRole('ROLE_ADMIN')) {
            return true
        }
        _checkOrgPermAndUserAffiliation(orgPerms.split(','), instUserRole)
    }

    /**
     * Checks for the context institution if one of the given customer types are granted
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    private boolean _checkOrgPermForForeignOrg(String[] orgPerms, Org orgToCheck) {
        boolean check = false

        if (orgPerms) {
            def oss = OrgSetting.get(orgToCheck, OrgSetting.KEYS.CUSTOMER_TYPE)

            Role fakeRole
            boolean isOrgBasicMemberView = false
            try {
                isOrgBasicMemberView = RequestContextHolder.currentRequestAttributes().params.orgBasicMemberView
            } catch (IllegalStateException e) {}

            if (isOrgBasicMemberView && orgToCheck.isCustomerType_Consortium()) {
                fakeRole = Role.findByAuthority('ORG_INST_BASIC')
                // TODO: ERMS-4920 - ORG_INST_BASIC or ORG_INST_PRO
            }

            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                orgPerms.each{ cd ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(cd.toLowerCase().trim()), (Role) fakeRole ?: oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
    }

    /**
     * Checks if the context institution has at least one of the given customer types and organisation types attributed
     * @param orgPerms customer type depending permissions to check against
     * @param orgTypes the organisation types to check
     * @return true if the context organisation passes both checks, false otherwise
     */
    private boolean _checkOrgPermAndOrgType(String[] orgPerms, String[] orgTypes) {
        boolean check1 = _checkOrgPermForForeignOrg(orgPerms, contextService.getOrg())
        boolean check2 = false

        if (orgTypes) {
            orgTypes.each { ot ->
                RefdataValue type = RefdataValue.getByValueAndCategory(ot.trim(), RDConstants.ORG_TYPE)
                check2 = check2 || contextService.getOrg()?.getAllOrgTypeIds()?.contains(type?.id)
            }
        } else {
            check2 = true
        }
        check1 && check2
    }

    /**
     * Checks if the context institution has at least one of the given customer types attrbited and if the context user
     * has the given rights attributed
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the given institutional permissions to check
     * @return true if the institution has the given customer type and the user the given institutional permissions, false otherwise
     */
    private boolean _checkOrgPermAndUserAffiliation(String[] orgPerms, String instUserRole) {
        boolean check1 = _checkOrgPermForForeignOrg(orgPerms, contextService.getOrg())
        boolean check2 = instUserRole ? contextService.getUser()?.is_ROLE_ADMIN_or_hasAffiliation(instUserRole.toUpperCase()) : false

        check1 && check2
    }

    /**
     * Checks if the context institution has at least one of the given customer and organisational types attributed and if the context user
     * has the given rights attributed
     * @param orgPerms the customer types to check against
     * @param orgTypes the organisation types to check
     * @param instUserRole the given institutional permissions to check
     * @return true if the institution has the given customer and organisation type and the user the given institutional permissions, false otherwise
     */
    private boolean _checkOrgPermAndOrgTypeAndUserAffiliation(String[] orgPerms, String[] orgTypes, String instUserRole) {
        boolean check1 = _checkOrgPermAndOrgType(orgPerms, orgTypes)
        boolean check2 = instUserRole ? contextService.getUser()?.is_ROLE_ADMIN_or_hasAffiliation(instUserRole.toUpperCase()) : false

        check1 && check2
    }

    // ---- new stuff here
    // ---- new stuff here

    // ----- REFACTORING -----

    boolean is_ROLE_ADMIN_or_checkMinUserOrgRole_and_CtxOrg(User user, Org orgToCheck, String userRoleName) {
        if (user?.hasMinRole('ROLE_ADMIN')) {
            return true
        }

        checkMinUserOrgRole_and_CtxOrg(user, orgToCheck, userRoleName)
    }

    /**
     * Checks if the user has at least the given role at the given institution
     * @param user the user whose permissions should be checked
     * @param orgToCheck the institution the user belongs to
     * @param role the minimum role the user needs at the given institution
     * @return true if the user has at least the given role at the given institution, false otherwise
     */
    boolean checkMinUserOrgRole_and_CtxOrg(User user, Org orgToCheck, String userRoleName) {
        boolean result = false

        if (! user || ! orgToCheck) {
            return result
        }
        // NEW CONSTRAINT:
        if (orgToCheck.id != contextService.getOrg().id) {
            return result
        }

        List<String> rolesToCheck = [userRoleName]

        // handling inst role hierarchy
        if (userRoleName == 'INST_USER') {
            rolesToCheck << 'INST_EDITOR'
            rolesToCheck << 'INST_ADM'
        }
        else if (userRoleName == 'INST_EDITOR') {
            rolesToCheck << 'INST_ADM'
        }

        rolesToCheck.each{ String rot ->
            Role role = Role.findByAuthority(rot)
            UserOrgRole userOrg = UserOrgRole.findByUserAndOrgAndFormalRole(user, orgToCheck, role)
            if (userOrg) {
                result = true
            }
        }
        result
    }

    // ----- CONSTRAINT CHECKS -----

    /**
     * Replacement call for the abandoned ROLE_ORG_COM_EDITOR
     * @return the result of {@link #checkCtxPermAffiliation(java.lang.String, java.lang.String)} for [ORG_INST_PRO, ORG_CONSORTIUM_BASIC] and INST_EDTOR as arguments
     */
    boolean is_ORG_COM_EDITOR() {
        _checkOrgPermAndUserAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')
    }

    boolean is_INST_EDITOR_with_PERMS_BASIC(boolean inContextOrg) {
        boolean a = _checkOrgPermAndUserAffiliation(CustomerTypeService.ORG_INST_BASIC.split(','), 'INST_EDITOR') && inContextOrg
        boolean b = _checkOrgPermAndUserAffiliation(CustomerTypeService.ORG_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')

        return (a || b)
    }

    boolean is_ROLE_ADMIN_or_has_PERMS(String orgPerms) {
        if (contextService.getUser()?.hasMinRole('ROLE_ADMIN')) {
            return true
        }
        _checkOrgPermForForeignOrg(orgPerms.split(','), contextService.getOrg())
    }

    boolean is_ROLE_ADMIN_or_INST_USER_with_PERMS(String orgPerms) {
        _is_ROLE_ADMIN_or_checkOrgPermAndUserAffiliation(orgPerms, 'INST_USER')
    }

    boolean is_ROLE_ADMIN_or_INST_EDITOR_with_PERMS(String orgPerms) {
        _is_ROLE_ADMIN_or_checkOrgPermAndUserAffiliation(orgPerms, 'INST_EDITOR')
    }

    boolean is_ROLE_ADMIN_or_INST_ADM_with_PERMS(String orgPerms) {
        _is_ROLE_ADMIN_or_checkOrgPermAndUserAffiliation(orgPerms, 'INST_ADM')
    }
}
