package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Person
import com.k_int.kbplus.auth.User
import org.codehaus.groovy.grails.web.util.WebUtils

class AddressbookService {

    def springSecurityService
    def contextService

    def getVisiblePersonsByOrgRoles(User user, orgRoles) {
        def orgList = []
        orgRoles.each { or ->
            orgList << or.org
        }
        getVisiblePersons(user, orgList)
    }

    def getVisiblePersons(User user, Org org) {
        def orgList = [org]
        getVisiblePersons(user, orgList)
    }

    def getVisiblePersons(User user, List orgs) {
        def membershipOrgIds = []
        user.authorizedOrgs?.each{ ao ->
            membershipOrgIds << ao.id
        }

        def visiblePersons = []
        orgs.each { org ->
            org.prsLinks.each { pl ->
                if (pl.prs?.isPublic?.value == 'No') {
                    if (pl.prs?.tenant?.id && membershipOrgIds.contains(pl.prs?.tenant?.id)) {
                        if (!visiblePersons.contains(pl.prs)) {
                            visiblePersons << pl.prs
                        }
                    }
                }
            }
        }
        visiblePersons
    }

    def isAddressEditable(Address address, User user) {
        true // TODO
    }
    def isContactEditable(Contact contact, User user) {
        true // TODO
    }
    def isPersonEditable(Person person, User user) {
        true // TODO
    }
}
