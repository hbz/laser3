package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
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
                if (pl.prs && ! pl.prs.isPublic) {
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
            if (! prs.isPublic) {
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

    List getVisiblePersons(String fromSite,params) {
        def qParts = [
                'p.isPublic = :public'
        ]
        def qParams = [:]
        switch(fromSite) {
            case "addressbook":
                qParams.public = false
                qParts << 'p.tenant = :tenant'
                qParams.tenant = contextService.org
                break
            case "myPublicContacts":
                qParams.public = true
                break
        }

        if (params.prs) {
            qParts << "(LOWER(p.last_name) LIKE :prsName OR LOWER(p.middle_name) LIKE :prsName OR LOWER(p.first_name) LIKE :prsName)"
            qParams << [prsName: "%${params.prs.toLowerCase()}%"]
        }
        if (params.org && params.org instanceof Org) {
            qParts << "pr.org = :org"
            qParams << [org: params.org]
        }
        else if(params.org && params.org instanceof String) {
            qParts << "(pr.org.name like :name or pr.org.shortname like :name or pr.org.sortname like :name)"
            qParams << [name: "%${params.org}%"]
        }

        def query = "SELECT distinct p FROM Person AS p join p.roleLinks pr WHERE " + qParts.join(" AND ")

        if (params.filterPropDef) {
            def psq = propertyService.evalFilterQuery(params, query, 'p', qParams)
            query = psq.query
            qParams = psq.queryParams
        }

        List result = Person.executeQuery(query + " ORDER BY p.last_name, p.first_name ASC", qParams)
        result
    }

}
