package de.laser

import com.k_int.kbplus.Address
import com.k_int.kbplus.Contact
import com.k_int.kbplus.Org
import com.k_int.kbplus.Person
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.transaction.Transactional
import org.codehaus.groovy.syntax.Numbers

@Transactional
class AddressbookService {

    def springSecurityService
    def contextService
    def accessService
    def propertyService

    List<Person> getAllVisiblePersonsByOrgRoles(User user, orgRoles) {
        List orgList = []
        orgRoles.each { or ->
            orgList << or.org
        }
        getAllVisiblePersons(user, orgList)
    }

    List<Person> getAllVisiblePersons(User user, Org org) {
        List orgList = [org]
        getAllVisiblePersons(user, orgList)
    }

    List<Person> getAllVisiblePersons(User user, List<Org> orgs) {
        List membershipOrgIds = [contextService.org.id]

        List visiblePersons = []
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
        List result = []

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
        Org org = address.getPrs()?.tenant ?: address.org
        accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
    }
    boolean isContactEditable(Contact contact, User user) {
        Org org = contact.getPrs()?.tenant ?: contact.org
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
        List qParts = [
                'p.isPublic = :public'
        ]
        Map qParams = [:]
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
            qParts << "( genfunc_filter_matcher(p.last_name, :prsName) = true OR genfunc_filter_matcher(p.middle_name, :prsName) = true OR genfunc_filter_matcher(p.first_name, :prsName) = true )"
            qParams << [prsName: "${params.prs}"]
        }
        if (params.org && params.org instanceof Org) {
            qParts << "pr.org = :org"
            qParams << [org: params.org]
        }
        else if(params.org && params.org instanceof String) {
            qParts << "( genfunc_filter_matcher(pr.org.name, :name) = true or genfunc_filter_matcher(pr.org.shortname, :name) = true or genfunc_filter_matcher(pr.org.sortname, :name) = true )"
            qParams << [name: "${params.org}"]
        }

        if (params.position || params.function){
            List s = []
            if (params.position) { s.addAll(params.position) }
            if (params.function) { s.addAll(params.function) }
            List<Long> selectedRoleTypIds = []
            s.each{ if (it && it !="null") {selectedRoleTypIds.add(Long.valueOf(it))}}
            List<RefdataValue> selectedRoleTypes = null
            if (selectedRoleTypIds) {
                selectedRoleTypes = RefdataValue.findAllByIdInList(selectedRoleTypIds)
                if (selectedRoleTypes) {
                    qParts << "pr.functionType in (:selectedRoleTypes) "
                    qParams << [selectedRoleTypes: selectedRoleTypes]
                }
            }

        }

        String query = "SELECT distinct p FROM Person AS p join p.roleLinks pr WHERE " + qParts.join(" AND ")

        if (params.filterPropDef) {
            Map<String, Object> psq = propertyService.evalFilterQuery(params, query, 'p', qParams)
            query = psq.query
            qParams = psq.queryParams
        }

        String order = "ASC"
        if (params?.order != null && params?.order != 'null'){
            order = params.order
        }
        query = query + " ORDER BY p.last_name ${order}, p.first_name ${order}"
        List result = Person.executeQuery(query, qParams)
        result
    }

}
