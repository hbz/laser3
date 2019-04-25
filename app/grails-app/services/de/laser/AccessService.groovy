package de.laser

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

    boolean checkPerm(String codes) {
        checkOrgPerm(codes.split(','))
    }
    boolean checkPermType(String codes, String orgTypes) {
        checkOrgPermAndOrgType(codes.split(','), orgTypes.split(','))
    }
    boolean checkPermAffiliation(String codes, String userRole) {
        checkOrgPermAndUserAffiliation(codes.split(','), userRole)
    }
    boolean checkPermTypeAffiliation(String codes, String orgTypes, String userRole) {
        checkOrgPermAndOrgTypeAndUserAffiliation(codes.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: shortcuts / with special global role check ---

    boolean checkPermX(String codes, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPerm(codes.split(','))
    }
    boolean checkPermTypeX(String codes, String orgTypes, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgType(codes.split(','), orgTypes.split(','))
    }
    boolean checkPermAffiliationX(String codes, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndUserAffiliation(codes.split(','), userRole)
    }
    boolean checkPermTypeAffiliationX(String codes, String orgTypes, String userRole, String specRoles) {
        if (contextService.getUser()?.hasRole(specRoles)) {
            return true
        }
        checkOrgPermAndOrgTypeAndUserAffiliation(codes.split(','), orgTypes.split(','), userRole)
    }

    // --- for action closures: implementations ---

    private boolean checkOrgPerm(String[] codes) {
        boolean check = false

        if (codes) {
            Org ctx = contextService.getOrg()
            def oss = OrgSettings.get(ctx, OrgSettings.KEYS.CUSTOMER_TYPE)

            if (oss != OrgSettings.SETTING_NOT_FOUND) {
                codes.each{ cd ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(cd?.toLowerCase()?.trim()), (Role) oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
    }

    private boolean checkOrgPermAndOrgType(String[] codes, String[] orgTypes) {
        boolean check1 = checkOrgPerm(codes)
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

    private boolean checkOrgPermAndUserAffiliation(String[] codes, String userRole) {
        boolean check1 = checkOrgPerm(codes)
        boolean check2 = userRole ? contextService.getUser()?.hasAffiliation(userRole?.toUpperCase()) : false

        check1 && check2
    }

    private boolean checkOrgPermAndOrgTypeAndUserAffiliation(String[] codes, String[] orgTypes, String userRole) {
        boolean check1 = checkOrgPermAndOrgType(codes, orgTypes)
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
