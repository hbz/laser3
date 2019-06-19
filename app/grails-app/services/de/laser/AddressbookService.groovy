package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.PersonRole
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.syntax.Numbers

class AddressbookService {

    def springSecurityService
    def contextService
    def accessService
    def propertyService

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

    List getVisiblePersons(String fromSite,max,offset,params) {
        def qParts = [
                'p.tenant = :tenant',
                'p.isPublic = :public'
        ]
        def qParams = [
                tenant: contextService.org,

        ]
        switch(fromSite) {
            case "addressbook": qParams.public = RDStore.YN_NO
                break
            case "myPublicContacts": qParams.public = RDStore.YN_YES
                break
        }

        if (params.prs) {
            qParts << "(LOWER(p.last_name) LIKE :prsName OR LOWER(p.middle_name) LIKE :prsName OR LOWER(p.first_name) LIKE :prsName)"
            qParams << [prsName: "%${params.prs.toLowerCase()}%"]
        }
        if (params.org) {
            qParts << """(EXISTS (SELECT pr FROM p.roleLinks AS pr WHERE (LOWER(pr.org.name) LIKE :orgName OR LOWER(pr.org.shortname) LIKE :orgName OR LOWER(pr.org.sortname) LIKE :orgName)))"""
            qParams << [orgName: "%${params.org.toLowerCase()}%"]
        }

        def query = "SELECT p FROM Person AS p WHERE " + qParts.join(" AND ")

        if (params.filterPropDef) {
            def psq = propertyService.evalFilterQuery(params, query, 'p', qParams)
            query = psq.query
            qParams = psq.queryParams
        }

        List result = Person.executeQuery(query + " ORDER BY p.last_name, p.first_name ASC", qParams, [max:max, offset:offset])
        result
    }

}
