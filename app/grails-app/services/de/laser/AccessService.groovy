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
    def contextService

    // ---- new stuff here
    // ---- new stuff here

    boolean test(boolean value) {
        value
    }

    // --- shortcuts ---

    boolean checkPerm(String code) {
        checkOrgPerm(code)
    }
    boolean checkPermType(String code, String orgType) {
        checkOrgPermAndOrgType(code, orgType)
    }
    boolean checkPermAffiliation(String code, String userRole) {
        checkOrgPermAndUserAffiliation(code, userRole)
    }
    boolean checkPermTypeAffiliation(String code, String orgType, String userRole) {
        checkOrgPermAndOrgTypeAndUserAffiliation(code, orgType, userRole)
    }

    // --- implementations ---

    boolean checkOrgPerm(String code) {
        boolean check = false

        Org ctx = contextService.getOrg()
        def oss = OrgSettings.get(ctx, OrgSettings.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSettings.SETTING_NOT_FOUND) {
            check = PermGrant.findByPermAndRole(Perm.findByCode(code?.toLowerCase()), (Role) oss.getValue())
        }

        check
    }

    boolean checkOrgPermAndOrgType(String code, String orgType) {
        boolean check1 = checkOrgPerm(code)

        RefdataValue type = RefdataValue.getByValueAndCategory(orgType, 'OrgRoleType')
        boolean check2 = contextService.getOrg()?.getallOrgTypeIds()?.contains(type?.id)

        check1 && check2
    }

    boolean checkOrgPermAndUserAffiliation(String code, String userRole) {
        boolean check1 = checkOrgPerm(code)
        boolean check2 = contextService.getUser()?.hasAffiliation(userRole?.toUpperCase())

        check1 && check2
    }

    boolean checkOrgPermAndOrgTypeAndUserAffiliation(String code, String orgType, String userRole) {
        boolean check1 = checkOrgPermAndOrgType(code, orgType)
        boolean check2 = contextService.getUser()?.hasAffiliation(userRole?.toUpperCase())

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
