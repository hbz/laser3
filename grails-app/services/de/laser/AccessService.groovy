package de.laser

import de.laser.auth.*
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
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

    /**
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    boolean ctxPerm(String orgPerms) {
        _hasPerm_forOrg_withFakeRole(orgPerms.split(','), contextService.getOrg())
    }
    boolean ctxPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPerm_forOrg_withFakeRole(orgPerms.split(','), contextService.getOrg())
    }

    /**
     * Substitution call for {@link #_hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(java.lang.String[], java.lang.String)}
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the user permissions to check
     * @return true if the user has the permissions granted and his context institution is one of the given customer types, false otherwise
     */
    boolean ctxPermAffiliation(String orgPerms, String instUserRole) {
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), instUserRole)
    }

    /**
     * Checks
     * <ul>
     *     <li>if the context institution is one of the given customer and organisation types and the user has the given rights granted</li>
     *     <li>or if the user has the given global rights granted</li>
     * </ul>
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the user's affiliation to the context institution
     * @return true if the user has one of the global permissions
     * or if the context institution is one of the given customer and organisation types
     * and if the user has the given permissions within the institution, false otherwise
     */
    boolean ctxConsortiumCheckPermAffiliation_or_ROLEADMIN(String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        boolean check1 = _hasPerm_forOrg_withFakeRole(orgPerms.split(','), contextService.getOrg())
        boolean check2 = contextService.getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )
        boolean check3 = instUserRole ? contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN(instUserRole.toUpperCase()) : false

        check1 && check2 && check3
    }

    boolean ctxInstUserCheckPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_USER')
    }

    boolean ctxInstEditorCheckPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_EDITOR')
    }

    boolean ctxInstAdmCheckPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_ADM')
    }

    // --- checks for other orgs ---

    /**
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    boolean otherOrgPerm(Org orgToCheck, String orgPerms) {
        _hasPerm_forOrg_withFakeRole(orgPerms.split(','), orgToCheck)
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
     *      comboPerms: customer type of the target institution
     *      comboAffiliation: user's rights for the target institution
     * ]
     * @return true if clauses one and two or three succeed, false otherwise
     */
    boolean otherOrgAndComboCheckPermAffiliation_or_ROLEADMIN(Org orgToCheck, String comboPerms, String comboAffiliation) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        Org ctx                 = contextService.getOrg()
        String orgPerms         = comboPerms
        String instUserRole     = comboAffiliation

        // combo check
        boolean check1 = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), instUserRole)
        boolean check2 = (ctx.id == orgToCheck.id) || Combo.findByToOrgAndFromOrg(ctx, orgToCheck)

        // orgToCheck check
        boolean check3 = (ctx.id == orgToCheck.id) && contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN(null) // TODO: legacy - no affiliation given

        (check1 && check2) || check3
    }

    // --- private methods ONLY

    /**
     * Checks for the context institution if one of the given customer types are granted
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
    private boolean _hasPerm_forOrg_withFakeRole(String[] orgPerms, Org orgToCheck) {
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
     * Checks if the context institution has at least one of the given customer types attrbited and if the context user
     * has the given rights attributed
     * @param orgPerms customer type depending permissions to check against
     * @param instUserRole the given institutional permissions to check
     * @return true if the institution has the given customer type and the user the given institutional permissions, false otherwise
     */
    private boolean _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(String[] orgPerms, String instUserRole) {
        boolean check1 = _hasPerm_forOrg_withFakeRole(orgPerms, contextService.getOrg())
        boolean check2 = instUserRole ? contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN(instUserRole.toUpperCase()) : false

        check1 && check2
    }

    // ---- new stuff here
    // ---- new stuff here

    // ----- REFACTORING -----

    // TODO
    boolean checkMinUserOrgRole_and_CtxOrg_or_ROLEADMIN(User user, Org orgToCheck, String userRoleName) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
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
    // TODO
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
     * @return the result of {@link #ctxPermAffiliation(java.lang.String, java.lang.String)} for [ORG_INST_PRO, ORG_CONSORTIUM_BASIC] and INST_EDTOR as arguments
     */
    // TODO
    boolean is_ORG_COM_EDITOR() {
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')
    }

    // TODO
    boolean is_INST_EDITOR_with_PERMS_BASIC(boolean inContextOrg) {
        boolean a = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.ORG_INST_BASIC.split(','), 'INST_EDITOR') && inContextOrg
        boolean b = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.ORG_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')

        return (a || b)
    }
}
