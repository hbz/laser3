package de.laser


import de.laser.auth.*
import de.laser.helper.RDConstants
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

    def contextService

    // ---- new stuff here
    // ---- new stuff here

    /**
     * Test method
     */
    boolean test(boolean value) {
        value
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org

    /**
     * Substitution call for {@link #checkOrgPerm(java.lang.String[])}
     * @param orgPerms the customer types (= institution permissions) to check
     * @return true if access is granted, false otherwise
     */
    boolean checkPerm(String orgPerms) {
        checkOrgPerm(orgPerms.split(','))
    }

    /**
     * Substitution call for {@link #checkOrgPerm(de.laser.Org, java.lang.String[])}
     * @param ctxOrg the context institution whose customer type needs to be checked
     * @param orgPerms the customer types which need to be granted to access
     * @return true if access is granted, false otherwise
     */
    boolean checkPerm(Org ctxOrg, String orgPerms) {
        checkOrgPerm(ctxOrg, orgPerms.split(','))
    }

    /**
     * Substitution call for {@link #checkOrgPermAndOrgType(java.lang.String[], java.lang.String[])}
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @return true if the context organisation passes both checks, false otherwise
     */
    boolean checkPermType(String orgPerms, String orgTypes) {
        checkOrgPermAndOrgType(orgPerms.split(','), orgTypes.split(','))
    }

    /**
     * Substitution call for {@link #checkOrgPermAndUserAffiliation(java.lang.String[], java.lang.String)}
     * @param orgPerms the customer types to check
     * @param userRole the user permissions to check
     * @return true if the user has the permissions granted and his context institution is one of the given customer types, false otherwise
     */
    boolean checkPermAffiliation(String orgPerms, String userRole) {
        checkOrgPermAndUserAffiliation(orgPerms.split(','), userRole)
    }

    /**
     * Substitution call for {@link #checkOrgPermAndOrgTypeAndUserAffiliation(java.lang.String[], java.lang.String[], java.lang.String)}
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @param userRole the user permissions to check
     * @return true if the user's context institution has both the given customer and organisation types and the user has the given permissions granted, false otherwise
     */
    boolean checkPermTypeAffiliation(String orgPerms, String orgTypes, String userRole) {
        checkOrgPermAndOrgTypeAndUserAffiliation(orgPerms.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org OR global roles

    /**
     * Checks if the context institution has the given customer types or if the user has one of the given global rights
     * @param orgPerms the customer types to check
     * @param specRoles the global permissions to check
     * @return true if the user has one of the global permissions granted or if the context institution has one of the given customer types
     */
    boolean checkPermX(String orgPerms, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPerm(orgPerms.split(','))
    }

    /**
     * Checks if the context institution has the given customer types and organisation types or if the user has one of the given global rights
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @param specRoles the global permissions to check
     * @return true if the user has one of the global permissions granted or if the context institution has one of the given customer and organisation types
     */
    boolean checkPermTypeX(String orgPerms, String orgTypes, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgType(orgPerms.split(','), orgTypes.split(','))
    }

    /**
     * Checks
     * <ul>
     *     <li>if the context institution is one of the given customer types and the user has the given rights granted</li>
     *     <li>or if the user has the given global rights granted</li>
     * </ul>
     * @param orgPerms the customer types to check
     * @param userRole the user's affiliation to the context institution
     * @param specRoles the global permissions to check
     * @return true if the user has one of the global permissions
     * or if the context institution is one of the given customer types
     * and if the user has the given permissions within the institution, false otherwise
     */
    boolean checkPermAffiliationX(String orgPerms, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndUserAffiliation(orgPerms.split(','), userRole)
    }

    /**
     * Checks
     * <ul>
     *     <li>if the context institution is one of the given customer and organisation types and the user has the given rights granted</li>
     *     <li>or if the user has the given global rights granted</li>
     * </ul>
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @param userRole the user's affiliation to the context institution
     * @param specRoles the global permissions to check
     * @return true if the user has one of the global permissions
     * or if the context institution is one of the given customer and organisation types
     * and if the user has the given permissions within the institution, false otherwise
     */
    boolean checkPermTypeAffiliationX(String orgPerms, String orgTypes, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgTypeAndUserAffiliation(orgPerms.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org and combo relation
    // --- USE FOR FOREIGN ORG CHECKS

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
    boolean checkForeignOrgComboPermAffiliation(Map<String, Object> attributes) {
        Org ctx                 = contextService.getOrg()
        Org currentOrg          = (Org) attributes.org
        String ownerUserRole    = attributes.affiliation
        String orgPerms         = attributes.comboPerm
        String userRole         = attributes.comboAffiliation

        // combo check
        boolean check1 = checkOrgPermAndUserAffiliation(orgPerms.split(','), userRole)
        boolean check2 = (ctx.id == currentOrg.id) || Combo.findByToOrgAndFromOrg(ctx, currentOrg)

        // currentOrg check
        boolean check3 = (ctx.id == currentOrg.id) && contextService.getUser()?.hasAffiliation(ownerUserRole?.toUpperCase())

        (check1 && check2) || check3
    }

    /**
     * Checks if
     * <ol>
     *     <li>the target institution is of the given customer type and the user has the given permissions granted at the target institution</li>
     *     <li>there is a combo relation to the given target institution</li>
     *     <li>or if the user has the given permissions granted at the context institution</li>
     *     <li>or if the user has one of the given global permissions granted</li>
     * </ol>
     * @param attributes a configuration map:
     * [
     *      org: context institution,
     *      affiliation: user's rights at the context institution
     *      comboPerm: customer type of the target institution
     *      comboAffiliation: user's rights for the target institution
     *      specRoles: global permissions to check
     * ]
     * @return true if clauses one and two or three or four succeed, false otherwise
     */
    boolean checkForeignOrgComboPermAffiliationX(Map<String, Object> attributes) {
          if (contextService.getUser()?.hasRole(attributes.specRoles)) {
            return true
        }

        checkForeignOrgComboPermAffiliation(attributes)
    }

    // --- for action closures: implementations ---
    // --- checking current user and context org

    /**
     * Checks for the context institution if one of the given customer types are granted
     * @param contextOrg the context institution whose customer type needs to be checked
     * @param orgPerms the customer types which need to be granted to access
     * @return true if access is granted, false otherwise
     */
    private boolean checkOrgPerm(Org contextOrg, String[] orgPerms) {
        boolean check = false

        if (orgPerms) {
            Org ctx = contextOrg
            def oss = OrgSetting.get(ctx, OrgSetting.KEYS.CUSTOMER_TYPE)

            Role fakeRole
            //println(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes().params)
            //println(oss.getValue())
            boolean isOrgBasicMemberView = false
            try {
                isOrgBasicMemberView = RequestContextHolder.currentRequestAttributes().params.orgBasicMemberView
            } catch (IllegalStateException e) {}

            if(isOrgBasicMemberView && (oss.getValue() == Role.findAllByAuthority('ORG_CONSORTIUM'))){
                fakeRole = Role.findByAuthority('ORG_BASIC_MEMBER')
            }

            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                orgPerms.each{ cd ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(cd?.toLowerCase()?.trim()), (Role) fakeRole ?: oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
    }

    /**
     * Substitution call for {@link #checkOrgPerm(de.laser.Org, java.lang.String[])} with {@link ContextService#getOrg()} as default
     * @param orgPerms the customer types (= institution permissions) to check
     * @return true if access is granted, false otherwise
     */
    private boolean checkOrgPerm(String[] orgPerms) {
        boolean check = false

        if (orgPerms) {
            Org ctx = contextService.getOrg()
            check = checkOrgPerm(ctx, orgPerms)
        } else {
            check = true
        }
        check
    }

    /**
     * Checks if the context institution has at least one of the given customer types and organisation types attributed
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @return true if the context organisation passes both checks, false otherwise
     */
    private boolean checkOrgPermAndOrgType(String[] orgPerms, String[] orgTypes) {
        boolean check1 = checkOrgPerm(orgPerms)
        boolean check2 = false

        if (orgTypes) {
            orgTypes.each { ot ->
                RefdataValue type = RefdataValue.getByValueAndCategory(ot?.trim(), RDConstants.ORG_TYPE)
                check2 = check2 || contextService.getOrg().getAllOrgTypeIds()?.contains(type?.id)
            }
        } else {
            check2 = true
        }
        check1 && check2
    }

    /**
     * Checks if the context institution has at least one of the given customer types attrbited and if the context user
     * has the given rights attributed
     * @param orgPerms the customer types to check
     * @param userRole the given institutional permissions to check
     * @return true if the institution has the given customer type and the user the given institutional permissions, false otherwise
     */
    private boolean checkOrgPermAndUserAffiliation(String[] orgPerms, String userRole) {
        boolean check1 = checkOrgPerm(orgPerms)
        boolean check2 = userRole ? contextService.getUser()?.hasAffiliation(userRole?.toUpperCase()) : false

        check1 && check2
    }

    /**
     * Checks if the context institution has at least one of the given customer and organisational types attributed and if the context user
     * has the given rights attributed
     * @param orgPerms the customer types to check
     * @param orgTypes the organisation types to check
     * @param userRole the given institutional permissions to check
     * @return true if the institution has the given customer and organisation type and the user the given institutional permissions, false otherwise
     */
    private boolean checkOrgPermAndOrgTypeAndUserAffiliation(String[] orgPerms, String[] orgTypes, String userRole) {
        boolean check1 = checkOrgPermAndOrgType(orgPerms, orgTypes)
        boolean check2 = userRole ? contextService.getUser()?.hasAffiliation(userRole?.toUpperCase()) : false

        check1 && check2
    }

    // ---- new stuff here
    // ---- new stuff here

    // ----- REFACTORING -----

    // ---- combined checks ----
    // ---- combined checks ----

    /**
     * Replacement call for the abandoned ROLE_ORG_COM_EDITOR
     * @return the result of {@link #checkPermAffiliation(java.lang.String, java.lang.String)} for [ORG_INST, ORG_CONSORTIUM] and INST_EDTOR as arguments
     */
    boolean checkConstraint_ORG_COM_EDITOR() {
        checkPermAffiliation('ORG_INST,ORG_CONSORTIUM', 'INST_EDITOR')
    }

    // ----- REFACTORING -----

    /**
     * Copied from FinanceController, LicenseCompareController, MyInstitutionsController.
     * Checks if the given user is member of the given institution
     * @param user the user to check
     * @param org the institution the user should belong to
     * @return true if the given user is affiliated to the given institution, false otherwise
     */
    boolean checkUserIsMember(User user, Org org) {

        // def uo = UserOrg.findByUserAndOrg(user,org)
        def uoq = UserOrg.where {
            (user == user && org == org)
        }

        return (uoq.count() > 0)
    }

    /**
     * Checks if the user has at least the given role at the given institution
     * @param user the user whose permissions should be checked
     * @param org the institution the user belongs to
     * @param role the minimum role the user needs at the given institution
     * @return true if the user has at least the given role at the given institution, false otherwise
     */
    boolean checkMinUserOrgRole(User user, Org org, def role) {

        boolean result = false
        def rolesToCheck = []

        if (! user || ! org) {
            return result
        }
        if (role instanceof String) {
            role = Role.findByAuthority(role)
        }
        rolesToCheck << role

        // NEW CONSTRAINT:
        if (org.id != contextService.getOrg().id) {
            return result
        }

        // sym. role hierarchy
        if (role.authority == "INST_USER") {
            rolesToCheck << Role.findByAuthority("INST_EDITOR")
            rolesToCheck << Role.findByAuthority("INST_ADM")
        }
        else if (role.authority == "INST_EDITOR") {
            rolesToCheck << Role.findByAuthority("INST_ADM")
        }

        rolesToCheck.each{ rot ->
            UserOrg userOrg = UserOrg.findByUserAndOrgAndFormalRole(user, org, rot)
            if (userOrg) {
                result = true
            }
        }
        result
    }
}
