package de.laser

import de.laser.auth.*
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
     *      orgToCheck: context institution,
     *      orgPerms: customer type of the target institution
     *      instUserRole: user's rights for the target institution
     * ]
     * @return true if clauses one and two or three succeed, false otherwise
     */
    boolean otherOrgAndComboCheckPermAffiliation_or_ROLEADMIN(Org orgToCheck, String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        Org ctx = contextService.getOrg()

        // combo check
        boolean check1 = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), instUserRole)
        boolean check2 = (orgToCheck.id == ctx.id) || Combo.findByToOrgAndFromOrg(ctx, orgToCheck)

        // orgToCheck check
        boolean check3 = (orgToCheck.id == ctx.id) && SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        // boolean check3 = (ctx.id == orgToCheck.id) && contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN(null) // legacy - no affiliation given

        (check1 && check2) || check3
    }

    // --- private methods ONLY
    // --- NO direct call

    /**
     * Checks for the context institution if one of the given customer types are granted
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @param orgPerms customer type depending permissions to check against
     * @return true if access is granted, false otherwise
     */
//    private boolean _hasPerm_forOrg_withFakeRole(String[] orgPerms, Org orgToCheck) {
    boolean _hasPerm_forOrg_withFakeRole(String[] orgPerms, Org orgToCheck) {
        boolean check = false

        if (orgPerms) {

            Role fakeRole
            boolean isOrgBasicMemberView = false
            try {
                isOrgBasicMemberView = RequestContextHolder.currentRequestAttributes().params.orgBasicMemberView
            } catch (IllegalStateException e) {}

            if (isOrgBasicMemberView && orgToCheck.isCustomerType_Consortium()) {
                fakeRole = Role.findByAuthority('ORG_INST_BASIC')
                // TODO: ERMS-4920 - ORG_INST_BASIC or ORG_INST_PRO
            }

            def oss = OrgSetting.get(orgToCheck, OrgSetting.KEYS.CUSTOMER_TYPE)
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
//    private boolean _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(String[] orgPerms, String instUserRole) {
    boolean _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(String[] orgPerms, String instUserRole) {

        if (contextService.getUser() && instUserRole) {
            if (contextService.getUser().hasCtxAffiliation_or_ROLEADMIN( instUserRole )) {
                return _hasPerm_forOrg_withFakeRole(orgPerms, contextService.getOrg())
            }
        }
        return false
    }

    // ----- REFACTORING -----
    // ----- CONSTRAINT CHECKS -----

    /**
     * Replacement call for the abandoned ROLE_ORG_COM_EDITOR
     */
    // TODO
    boolean is_ORG_COM_EDITOR() {
        _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')
    }

    // TODO
    // ctxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_BASIC, 'INST_EDITOR') || (ctxPermAffiliation(CustomerTypeService.ORG_INST_BASIC, 'INST_EDITOR') && inContextOrg)
    boolean is_INST_EDITOR_with_PERMS_BASIC(boolean inContextOrg) {
        boolean a = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.ORG_INST_BASIC.split(','), 'INST_EDITOR') && inContextOrg
        boolean b = _hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(CustomerTypeService.ORG_CONSORTIUM_BASIC.split(','), 'INST_EDITOR')

        return (a || b)
    }
}
