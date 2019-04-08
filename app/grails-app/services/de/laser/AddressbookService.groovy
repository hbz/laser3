package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.PersonRole
import com.k_int.kbplus.auth.User
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.syntax.Numbers

class AddressbookService {

    def springSecurityService
    def contextService
    def accessService

    List<Person> getAllVisiblePersonsByOrgRoles(User user, orgRoles) {
        def orgList = []
        orgRoles.each { or ->
            orgList << or.org
        }
        getAllVisiblePersons(user, orgList)
    }

    List<Person> getAllVisiblePersons(User user, Org org) {
        def orgList = [org]
        getAllVisiblePersons(user, orgList)
    }

    List<Person> getAllVisiblePersons(User user, List orgs) {
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

    List<Person> getPrivatePersonsByTenant(Org tenant) {
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

    boolean isAddressEditable(Address address, User user) {
        def org = address.getPrs()?.tenant ?: address.org
        accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
    }
    boolean isContactEditable(Contact contact, User user) {
        def org = contact.getPrs()?.tenant ?: contact.org
        accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
    }
    boolean isPersonEditable(Person person, User user) {
        accessService.checkMinUserOrgRole(user, person.tenant , 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        //true // TODO: Rechte nochmal überprüfen
    }

    boolean isNumbersEditable(Numbers numbers, User user) {
        accessService.checkMinUserOrgRole(user, person.tenant , 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        //true // TODO: Rechte nochmal überprüfen
    }
}
