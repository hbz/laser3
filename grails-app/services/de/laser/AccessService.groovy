package de.laser


import de.laser.auth.*
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils

/**
 * This service manages access control checks
 */
@Transactional
class AccessService {

    static final String CHECK_VIEW = 'CHECK_VIEW'
    static final String CHECK_EDIT = 'CHECK_EDIT'
    static final String CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'

    ContextService contextService

    // --- generic checks for orgs ---

    /**
     * @param orgPerms customer type depending permissions to check against
     * @param orgToCheck the context institution whose customer type needs to be checked
     * @return true if access is granted, false otherwise
     */
    boolean hasPermForOrg(String orgPerms, Org orgToCheck) {
        boolean check = false

        if (orgPerms) {
            def oss = OrgSetting.get(orgToCheck, OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                orgPerms.split(',').each { op ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(op.toLowerCase().trim()), (Role) oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
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

        // combo check @ contextUser/contextOrg
        boolean check1 = contextService._hasPermAndInstRole(orgPerms, instUserRole)
        boolean check2 = (orgToCheck.id == ctx.id) || Combo.findByToOrgAndFromOrg(ctx, orgToCheck)

        // orgToCheck check @ otherOrg
        boolean check3 = (orgToCheck.id == ctx.id) && SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        // boolean check3 = (ctx.id == orgToCheck.id) && contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN(null) // legacy - no affiliation given

        (check1 && check2) || check3
    }
}
