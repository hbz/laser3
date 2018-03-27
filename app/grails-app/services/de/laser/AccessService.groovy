package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Combo
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Doc
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Person
import com.k_int.kbplus.Platform
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.TitleInstancePackagePlatform
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg

class AccessService {

    def grailsApplication
    def springSecurityService

    def test() {
        // org context
        //ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR")
    }

    def isEditable(Address adr) {

    }

    def isEditable(Combo cmb) {

    }

    def isEditable(Contact con) {

    }

    def isEditable(Doc doc) {

    }

    def isEditable(License lic) {

    }

    def isEditable(Org org) {

    }

    def isEditable(Package pkg) {

    }

    def isEditable(Person prs) {

    }

    def isEditable(Platform plt) {

    }

    def isEditable(Subscription sub) {

    }

    def isEditable(TitleInstance title) {

    }

    def isEditable(TitleInstancePackagePlatform tipp) {

    }

    // copied from FinanceController, LicenseCompareController, MyInstitutionsController
    boolean checkUserIsMember(user, org) {

        // def uo = UserOrg.findByUserAndOrg(user,org)
        def uoq = UserOrg.where {
            (user == user && org == org && (status == UserOrg.STATUS_APPROVED || status == UserOrg.STATUS_AUTO_APPROVED))
        }

        return (uoq.count() > 0)
    }

    // copied from Org
    boolean checkUserOrgRole(user, org, role) {

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
