package de.laser

import de.laser.auth.User
import de.laser.helper.Params
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional

/**
 * This service handles retrieval and processing of contact data
 * @see Person
 * @see Address
 * @see Contact
 */
@Transactional
class AddressbookService {

    ContextService contextService
    PropertyService propertyService
    UserService userService

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
        userService.hasFormalAffiliation_or_ROLEADMIN(user, address.tenant ?: address.org, 'INST_EDITOR')
    }

    /**
     * Checks whether the given contact is editable by the given user
     * @param address the contact which should be accessed
     * @param user the user whose grants should be checked
     * @return true if the user is affiliated at least as INST_EDITOR with the given tenant or institution or is a global admin, false otherwise
     */
    boolean isContactEditable(Contact contact, User user) {
        Org org = contact.getPrs()?.tenant ?: contact.org
        userService.hasFormalAffiliation_or_ROLEADMIN(user, org, 'INST_EDITOR')
    }

    /**
     * Checks whether the given person is editable by the given user
     * @param person the person which should be accessed
     * @param user the user whose grants should be checked
     * @return true if the user is affiliated at least as INST_EDITOR with the given tenant or is a global admin, false otherwise
     */
    boolean isPersonEditable(Person person, User user) {
        userService.hasFormalAffiliation_or_ROLEADMIN(user, person.tenant , 'INST_EDITOR')
    }

    /**
     * Retrieves for the given page the visible persons. If it is for the address book page
     * (coming from {@link MyInstitutionController#addressbook()} or {@link OrganisationController#addressbook}),
     * then the list is restricted to the private contacts of the context institution. Otherwise, if an address book
     * page of a foreign institution has been called (coming then from {@link OrganisationController#contacts()}),
     * all public contacts are being returned, whoever is tenant of the given contact. The result may be filtered by
     * the given parameter map
     * @param fromSite the page for which the list is being returned
     * @param params a parameter map to filter the results
     * @return an eventually filtered list of person contacts
     */
    List getVisiblePersons(String fromSite, Map params) {
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
            case "contacts":
                qParams.public = true
                break
        }

        if (params.containsKey('prs')) {
            qParts << "( genfunc_filter_matcher(p.last_name, :prsName) = true OR genfunc_filter_matcher(p.middle_name, :prsName) = true OR genfunc_filter_matcher(p.first_name, :prsName) = true )"
            qParams << [prsName: "${params.prs}"]
        }
        if (params.containsKey('org')) {
            if (params.org instanceof Org) {
                qParts << "pr.org = :org"
                qParams << [org: params.org]
            }
            else if(params.org instanceof String) {
                qParts << "( genfunc_filter_matcher(pr.org.name, :name) = true or genfunc_filter_matcher(pr.org.sortname, :name) = true )"
                qParams << [name: "${params.org}"]
            }
        }
        else if(params.containsKey('vendor')) {
            if (params.vendor instanceof Vendor) {
                qParts << "pr.vendor = :vendor"
                qParams << [vendor: params.vendor]
            }
            else if(params.vendor instanceof String) {
                qParts << "( genfunc_filter_matcher(pr.vendor.name, :name) = true or genfunc_filter_matcher(pr.vendor.sortname, :name) = true )"
                qParams << [name: "${params.vendor}"]
            }
        }

        if (params.function || params.position) {
            List<String> posParts = []
            if (params.function){
                posParts << "pr.functionType.id in (:selectedFunctions) "
                qParams << [selectedFunctions: Params.getLongList(params, 'function')]
            }

            if (params.position){
                posParts << "pr.positionType.id in (:selectedPositions) "
                qParams << [selectedPositions: Params.getLongList(params, 'position')]
            }
            qParts << '('+posParts.join(' OR ')+')'
        }

        if (params.showOnlyContactPersonForInstitution || params.exportOnlyContactPersonForInstitution){
            qParts << "(exists (select roletype from pr.org.orgType as roletype where roletype.id = :instType ) and pr.org.sector.id = :instSector )"
            qParams << [instSector: RDStore.O_SECTOR_HIGHER_EDU.id, instType: RDStore.OT_INSTITUTION.id]
        }

        if (params.showOnlyContactPersonForProviderAgency || params.exportOnlyContactPersonForProviderAgency){
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

        query = query + " ORDER BY ${params.sort} ${order}, p.last_name"
        List result = Person.executeQuery(query, qParams)

        if(result) {
            result = result.collect { row -> row[0] }
        }
        result
    }

    /**
     * Retrieves for the given page the visible addresses; is an adapted copy of {@link #getVisiblePersons(java.lang.String, java.util.Map)}. If it is for the address book page
     * (coming from {@link MyInstitutionController#addressbook()} or {@link OrganisationController#addressbook}),
     * then the list is restricted to the private addresses of the context institution. Otherwise, if an address book
     * page of a foreign institution has been called (coming then from {@link OrganisationController#contacts()}),
     * all public addresses are being returned, whoever is tenant of the given address record. The result may be filtered by
     * the given parameter map
     * @param fromSite the page for which the list is being returned
     * @param params a parameter map to filter the results
     * @return an eventually filtered list of person contacts
     */
    List getVisibleAddresses(String fromSite, Map params) {
        List qParts = []
        Map qParams = [:]
        String sort = params.sort
        if(!params.containsKey('sort'))
            sort = 'a.org.sortname'
        else if(params.sort.contains('pr.org'))
            sort = params.sort.replaceAll('pr.org', 'a.org')
        else if(params.sort.contains('_name'))
            sort = 'a.name'
        switch(fromSite) {
            case "addressbook":
                qParts << 'a.tenant = :tenant'
                qParams.tenant = contextService.getOrg()
                break
            case "contacts":
                qParts << 'a.tenant is null'
                break
        }

        /*
        if (params.prs) {
            qParts << "( genfunc_filter_matcher(p.last_name, :prsName) = true OR genfunc_filter_matcher(p.middle_name, :prsName) = true OR genfunc_filter_matcher(p.first_name, :prsName) = true )"
            qParams << [prsName: "${params.prs}"]
        }
        */
        if (params.org && params.org instanceof Org) {
            qParts << "a.org = :org"
            qParams << [org: params.org]
        }
        else if(params.org && params.org instanceof String) {
            qParts << "( genfunc_filter_matcher(a.org.name, :name) = true or genfunc_filter_matcher(a.org.sortname, :name) = true )"
            qParams << [name: "${params.org}"]
        }

        if (params.type) {
            qParts << "(exists (select at from a.type as at where at.id in (:selectedTypes))) "
            qParams << [selectedTypes: Params.getLongList(params, 'type')]
        }

        if (params.showOnlyContactPersonForInstitution || params.exportOnlyContactPersonForInstitution){
            qParts << "(exists (select roletype from a.org.orgType as roletype where roletype.id = :instType ) and a.org.sector.id = :instSector )"
            qParams << [instSector: RDStore.O_SECTOR_HIGHER_EDU.id, instType: RDStore.OT_INSTITUTION.id]
        }

        if (params.showOnlyContactPersonForProvider || params.exportOnlyContactPersonForProvider){
            qParts << "(exists (select roletype from a.org.orgType as roletype where roletype.id in (:orgType)) and a.org.sector.id = :orgSector )"
            qParams << [orgSector: RDStore.O_SECTOR_PUBLISHER.id, orgType: [RDStore.OT_PROVIDER.id, RDStore.OT_AGENCY.id]]
        }

        String query = "SELECT distinct(a), ${sort} FROM Address AS a WHERE " + qParts.join(" AND ")

        /*
        if (params.filterPropDef) {
            Map<String, Object> psq = propertyService.evalFilterQuery(params, query, 'a', qParams)
            query = psq.query
            qParams = psq.queryParams
        }
        */

        String order = "ASC"
        if (params?.order != null && params?.order != 'null'){
            order = params.order
        }

        query = query + " ORDER BY ${sort} ${order}"
        List result = Address.executeQuery(query, qParams)

        if(result) {
            result = result.collect { row -> row[0] }
        }
        result
    }

}
