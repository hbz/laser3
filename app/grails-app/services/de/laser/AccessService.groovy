package de.laser

import com.k_int.kbplus.Combo
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.Perm
import com.k_int.kbplus.auth.PermGrant
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg

class AccessService {

    static final CHECK_VIEW = 'CHECK_VIEW'
    static final CHECK_EDIT = 'CHECK_EDIT'
    static final CHECK_VIEW_AND_EDIT = 'CHECK_VIEW_AND_EDIT'

    static final ORG_BASIC = 'ORG_BASIC'
    static final ORG_MEMBER = 'ORG_MEMBER'
    static final ORG_CONSORTIUM = 'ORG_CONSORTIUM'
    static final ORG_CONSORTIUM_SURVEY = 'ORG_CONSORTIUM_SURVEY'
    static final ORG_COLLECTIVE = 'ORG_COLLECTIVE'

    def grailsApplication
    def springSecurityService
    ContextService contextService

    // ---- new stuff here
    // ---- new stuff here

    boolean test(boolean value) {
        value
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org

    boolean checkPerm(String orgPerms) {
        checkOrgPerm(orgPerms.split(','))
    }
    boolean checkPermType(String orgPerms, String orgTypes) {
        checkOrgPermAndOrgType(orgPerms.split(','), orgTypes.split(','))
    }
    boolean checkPermAffiliation(String orgPerms, String userRole) {
        checkOrgPermAndUserAffiliation(orgPerms.split(','), userRole)
    }
    boolean checkPermTypeAffiliation(String orgPerms, String orgTypes, String userRole) {
        checkOrgPermAndOrgTypeAndUserAffiliation(orgPerms.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org and global roles

    boolean checkPermX(String orgPerms, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPerm(orgPerms.split(','))
    }
    boolean checkPermTypeX(String orgPerms, String orgTypes, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgType(orgPerms.split(','), orgTypes.split(','))
    }
    boolean checkPermAffiliationX(String orgPerms, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndUserAffiliation(orgPerms.split(','), userRole)
    }
    boolean checkPermTypeAffiliationX(String orgPerms, String orgTypes, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgTypeAndUserAffiliation(orgPerms.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: shortcuts ---
    // --- checking current user and context org and combo relation
    // --- USE FOR FOREIGN ORG CHECKS

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
    boolean checkForeignOrgComboPermAffiliationX(Map<String, Object> attributes) {
          if (contextService.getUser()?.hasRole(attributes.specRoles)) {
            return true
        }

        checkForeignOrgComboPermAffiliation(attributes)
    }

    // --- for action closures: implementations ---
    // --- checking current user and context org

    private boolean checkOrgPerm(String[] orgPerms) {
        boolean check = false

        if (orgPerms) {
            Org ctx = contextService.getOrg()
            def oss = OrgSettings.get(ctx, OrgSettings.KEYS.CUSTOMER_TYPE)

            if (oss != OrgSettings.SETTING_NOT_FOUND) {
                orgPerms.each{ cd ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(cd?.toLowerCase()?.trim()), (Role) oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
    }

    private boolean checkOrgPermAndOrgType(String[] orgPerms, String[] orgTypes) {
        boolean check1 = checkOrgPerm(orgPerms)
        boolean check2 = false

        if (orgTypes) {
            orgTypes.each { ot ->
                RefdataValue type = RefdataValue.getByValueAndCategory(ot?.trim(), 'OrgRoleType')
                check2 = check2 || contextService.getOrg()?.getallOrgTypeIds()?.contains(type?.id)
            }
        } else {
            check2 = true
        }
        check1 && check2
    }

    private boolean checkOrgPermAndUserAffiliation(String[] orgPerms, String userRole) {
        boolean check1 = checkOrgPerm(orgPerms)
        boolean check2 = userRole ? contextService.getUser()?.hasAffiliation(userRole?.toUpperCase()) : false

        check1 && check2
    }

    private boolean checkOrgPermAndOrgTypeAndUserAffiliation(String[] orgPerms, String[] orgTypes, String userRole) {
        boolean check1 = checkOrgPermAndOrgType(orgPerms, orgTypes)
        boolean check2 = userRole ? contextService.getUser()?.hasAffiliation(userRole?.toUpperCase()) : false

        check1 && check2
    }

    // ---- new stuff here
    // ---- new stuff here

    // copied from FinanceController, LicenseCompareController, MyInstitutionsController
    boolean checkUserIsMember(User user, Org org) {

        // def uo = UserOrg.findByUserAndOrg(user,org)
        def uoq = UserOrg.where {
            (user == user && org == org && status == UserOrg.STATUS_APPROVED)
        }

        return (uoq.count() > 0)
    }

    boolean checkMinUserOrgRole(User user, Org org, def role) {

        def result = false
        def rolesToCheck = []

        if (! user || ! org) {
            return result
        }
        if (role instanceof String) {
            role = Role.findByAuthority(role)
        }
        rolesToCheck << role

        // NEW CONSTRAINT:
        if (org.id != contextService.getOrg()?.id) {
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
            def userOrg = UserOrg.findByUserAndOrgAndFormalRole(user, org, rot)
            if (userOrg && userOrg.status == UserOrg.STATUS_APPROVED) {
                result = true
            }
        }
        result
    }

    boolean checkIsEditableForAdmin(User toEdit, User editor, Org org) {

        boolean roleAdmin = editor.hasRole('ROLE_ADMIN')
        boolean instAdmin = editor.hasAffiliation('INST_ADM') // check @ contextService.getOrg()
        boolean orgMatch  = checkUserIsMember(toEdit, contextService.getOrg())

        roleAdmin || (instAdmin && orgMatch)
    }
}
