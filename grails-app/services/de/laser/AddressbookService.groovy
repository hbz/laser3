package de.laser

import de.laser.auth.User
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.syntax.Numbers

/**
 * This service handles retrieval and processing of contact data
 * @see Person
 * @see Address
 * @see Contact
 */
@Transactional
class AddressbookService {

    AccessService accessService
    ContextService contextService
    PropertyService propertyService

    @Deprecated
    List<Person> getAllVisiblePersonsByOrgRoles(User user, orgRoles) {
        List orgList = []
        orgRoles.each { or ->
            orgList << or.org
        }
        getAllVisiblePersons(user, orgList)
    }

    @Deprecated
    List<Person> getAllVisiblePersons(User user, Org org) {
        List orgList = [org]
        getAllVisiblePersons(user, orgList)
    }

    @Deprecated
    List<Person> getAllVisiblePersons(User user, List<Org> orgs) {
        List membershipOrgIds = [contextService.getOrg().id]

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

    /**
     * Retrieves all private contacts for the given tenant institution
     * @param tenant the institution ({@link Org}) whose private contacts should be retrieved
     * @return a list of private person contacts maintained by the given tenant
     * @see Person
     */
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

    /**
     * Checks whether the given address is editable by the given user
     * @param address the address which should be accessed
     * @param user the user whose grants should be checked
     * @return true if the user is affiliated at least as INST_EDITOR with the given tenant or institution or is a global admin, false otherwise
     */
    boolean isAddressEditable(Address address, User user) {
        Org org = address.getPrs()?.tenant ?: address.org
        accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
    }

    /**
     * Checks whether the given contact is editable by the given user
     * @param address the contact which should be accessed
     * @param user the user whose grants should be checked
     * @return true if the user is affiliated at least as INST_EDITOR with the given tenant or institution or is a global admin, false otherwise
     */
    boolean isContactEditable(Contact contact, User user) {
        Org org = contact.getPrs()?.tenant ?: contact.org
        accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
    }

    /**
     * Checks whether the given person is editable by the given user
     * @param person the person which should be accessed
     * @param user the user whose grants should be checked
     * @return true if the user is affiliated at least as INST_EDITOR with the given tenant or is a global admin, false otherwise
     */
    boolean isPersonEditable(Person person, User user) {
        accessService.checkMinUserOrgRole(user, person.tenant , 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
    }

    @Deprecated
    boolean isNumbersEditable(Numbers numbers, User user) {
        accessService.checkMinUserOrgRole(user, person.tenant , 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
    }

    /**
     * Retrieves for the given page the visible persons. If it is for the addressbook page
     * (coming from {@link MyInstitutionController#addressbook()} or {@link OrganisationController#addressbook}),
     * then the list is restricted to the private contacts of the context institution. Otherwise, if an addressbook
     * page of a foreign institution has been called (coming then from {@link OrganisationController#myPublicContacts()}),
     * all public contacts are being returned, whoever is tenant of the given contact. The result may be filtered by
     * the given parameter map
     * @param fromSite the page for which the list is being returned
     * @param params a parameter map to filter the results
     * @return an eventually filtered list of person contacts
     */
    List getVisiblePersons(String fromSite, GrailsParameterMap params) {
        List qParts = [
                'p.isPublic = :public'
        ]
        Map qParams = [:]
        switch(fromSite) {
            case "addressbook":
                qParams.public = false
                qParts << 'p.tenant = :tenant'
                qParams.tenant = contextService.getOrg()
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

        if (params.function){
            qParts << "pr.functionType.id in (:selectedFunctions) "
            qParams << [selectedFunctions: params.list('function').collect{Long.parseLong(it)}]

        }

        if (params.position){
            qParts << "pr.positionType.id in (:selectedPositions) "
            qParams << [selectedPositions: params.list('position').collect{Long.parseLong(it)}]

        }

        if (params.showOnlyContactPersonForInstitution){
            qParts << "(exists (select roletype from pr.org.orgType as roletype where roletype.id = :orgType ) and pr.org.sector.id = :orgSector )"
            qParams << [orgSector: RDStore.O_SECTOR_HIGHER_EDU.id, orgType: RDStore.OT_INSTITUTION.id]

        }

        if (params.showOnlyContactPersonForProviderAgency){
            qParts << "(exists (select roletype from pr.org.orgType as roletype where roletype.id in (:orgType)) and pr.org.sector.id = :orgSector )"
            qParams << [orgSector: RDStore.O_SECTOR_PUBLISHER.id, orgType: [RDStore.OT_PROVIDER.id, RDStore.OT_AGENCY.id]]

        }

        String query = "SELECT distinct(p), ${params.sort} FROM Person AS p join p.roleLinks pr WHERE " + qParts.join(" AND ")

        if (params.filterPropDef) {
            Map<String, Object> psq = propertyService.evalFilterQuery(params, query, 'p', qParams)
            query = psq.query
            qParams = psq.queryParams
        }

        String order = "ASC"
        if (params?.order != null && params?.order != 'null'){
            order = params.order
        }

        query = query + " ORDER BY ${params.sort} ${order}"
        List result = Person.executeQuery(query, qParams)

        if(result) {
            result = result.collect { row -> row[0] }
        }
        result
    }

}
