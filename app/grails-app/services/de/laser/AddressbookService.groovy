package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.PersonRole
import com.k_int.kbplus.auth.User

class AddressbookService {

    def springSecurityService
    def contextService

    def getAllVisiblePersonsByOrgRoles(User user, orgRoles) {
        def orgList = []
        orgRoles.each { or ->
            orgList << or.org
        }
        getAllVisiblePersons(user, orgList)
    }

    def getAllVisiblePersons(User user, Org org) {
        def orgList = [org]
        getAllVisiblePersons(user, orgList)
    }

    def getAllVisiblePersons(User user, List orgs) {
        def membershipOrgIds = []
        user.authorizedOrgs?.each{ ao ->
            membershipOrgIds << ao.id
        }

        def visiblePersons = []
        orgs.each { org ->
            org.prsLinks.each { pl ->
                if (pl.prs?.isPublic?.value == 'No') {
                    if (pl.prs?.tenant?.id && membershipOrgIds.contains(pl.prs?.tenant?.id)) {
                        if (! visiblePersons.contains(pl.prs)) {
                            visiblePersons << pl.prs
                        }
                    }
                }
            }
        }
        visiblePersons
    }

    def getPrivatePersonsByTenant(Org tenant) {
        def result = []

        Person.findAllByTenant(tenant)?.each{ prs ->
            if (prs.isPublic?.value == 'No') {
                if (! result.contains(prs)) {
                    result << prs
                }
            }
        }
        result
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
