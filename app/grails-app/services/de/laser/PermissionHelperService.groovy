package de.laser

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg

class PermissionHelperService {

    def grailsApplication
    def springSecurityService

    // copied from FinanceController, LicenseCompareController, MyInstitutionsController
    boolean checkUserIsMember(user, org) {

        // def uo = UserOrg.findByUserAndOrg(user,org)
        def uoq = UserOrg.where {
            (user == user && org == org && (status == UserOrg.STATUS_APPROVED || status == UserOrg.STATUS_AUTO_APPROVED))
        }

        return (uoq.count() > 0)
    }

    /*
    // copied from MyInstitutionsController
    boolean checkUserHasRole(user, org, role) {
        def uoq = UserOrg.createCriteria()

        def grants = uoq.list {
            and {
                eq('user', user)
                eq('org', org)
                formalRole {
                    eq('authority', role)
                }
                or {
                    eq('status', 1);
                    eq('status', 3);
                }
            }
        }

        return (grants && grants.size() > 0)
    }
    */

    // copied from Org
    boolean hasUserWithRole(user, org, role) {

        if (! user || ! org) {
            return false
        }
        if (role instanceof String) {
           role = Role.findByAuthority(role)
        }

        def userOrg = UserOrg.findByUserAndOrgAndFormalRole(user, org, role)
        if (userOrg && (userOrg.status == UserOrg.STATUS_APPROVED || userOrg.status == UserOrg.STATUS_AUTO_APPROVED)) {
            return true
        }

        false
    }
}
