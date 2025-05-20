package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.auth.User
import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional

/**
 * This service handles retrieval and processing of contact data
 * @see de.laser.addressbook.Person
 * @see de.laser.addressbook.Address
 * @see de.laser.addressbook.Contact
 */
@Transactional
class AddressbookService {

    ContextService contextService
    CustomerTypeService customerTypeService
    PropertyService propertyService

    /**
     * Retrieves all private contacts for the given tenant institution
     * @param tenant the institution ({@link Org}) whose private contacts should be retrieved
     * @return a list of private person contacts maintained by the given tenant
     * @see de.laser.addressbook.Person
     */
    List<Person> getPrivatePersonsByTenant(Org tenant) {
        List result = Person.findAllByTenantAndIsPublic(tenant, false)
        result
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
        String sort
        if(!params.sort || params.sort == "sortname")
            sort = 'coalesce(org.sortname, vendor.sortname, provider.sortname) as sortname'
        else sort = params.sort

        if (params.preferredSurveyPerson) {
            qParts << "p.preferredSurveyPerson = true"
        }

        if (params.preferredBillingPerson) {
            qParts << "p.preferredBillingPerson = true"
        }

        if (params.prs) {
            qParts << "( genfunc_filter_matcher(p.last_name, :prsName) = true OR genfunc_filter_matcher(p.middle_name, :prsName) = true OR genfunc_filter_matcher(p.first_name, :prsName) = true )"
            qParams << [prsName: "${params.prs}"]
        }
        if (params.org) {
            if (params.org instanceof Org) {
                qParts << "pr.org = :org"
                qParams << [org: params.org]
            }
            else if(params.org instanceof String) {
                // ERMS-5868
                qParts <<   "(( genfunc_filter_matcher(org.name, :name) = true or genfunc_filter_matcher(org.sortname, :name) = true ) OR " +
                            " ( genfunc_filter_matcher(vendor.name, :name) = true or genfunc_filter_matcher(vendor.sortname, :name) = true ) OR " +
                            " ( genfunc_filter_matcher(provider.name, :name) = true or genfunc_filter_matcher(provider.sortname, :name) = true ))"
                qParams << [name: "${params.org}"]
            }
        }
        else if(params.vendor) {
            if (params.vendor instanceof Vendor) {
                qParts << "pr.vendor = :vendor"
                qParams << [vendor: params.vendor]
            }
            else if(params.vendor instanceof String) {
                qParts << "( genfunc_filter_matcher(pr.vendor.name, :name) = true or genfunc_filter_matcher(pr.vendor.sortname, :name) = true )"
                qParams << [name: "${params.vendor}"]
            }
        }
        else if(params.provider) {
            if (params.provider instanceof Provider) {
                qParts << "pr.provider = :provider"
                qParams << [provider: params.provider]
            }
            else if(params.provider instanceof String) {
                qParts << "( genfunc_filter_matcher(pr.provider.name, :name) = true or genfunc_filter_matcher(pr.provider.sortname, :name) = true )"
                qParams << [name: "${params.provider}"]
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

        Map<String, Object> instProvVenFilter = _getInstitutionProviderVendorFilter(params)
        if(instProvVenFilter.containsKey('qParams')) {
            qParts.add(instProvVenFilter.qParts)
            qParams.putAll(instProvVenFilter.qParams)
        }

        String query = "SELECT distinct(p), ${sort} FROM Person AS p join p.roleLinks pr left join pr.org org left join pr.vendor vendor left join pr.provider provider WHERE " + qParts.join(" AND ")

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
        String sortquery = params.sort
        String sort = params.sort
        if(!params.containsKey('sort') || params.sort == 'sortname') {
            sortquery = 'coalesce(org.sortname, provider.sortname, vendor.sortname) as sortname'
            sort = 'sortname'
        }
        else if(params.sort.contains('_name')) {
            sortquery = 'a.name'
            sort = 'a.name'
        }
        switch(fromSite) {
            case "addressbook":
                qParts << 'a.tenant = :tenant'
                qParams.tenant = contextService.getOrg()
                break
            case "contacts":
                qParts << 'a.tenant = null'
                break
        }

        if (params.org && params.org instanceof Org) {
            qParts << "org = :org"
            qParams << [org: params.org]
        }
        else if(params.org && params.org instanceof String) {
            // ERMS-5868
            qParts <<   "(( genfunc_filter_matcher(org.name, :name) = true or genfunc_filter_matcher(org.sortname, :name) = true ) OR " +
                        " ( genfunc_filter_matcher(vendor.name, :name) = true or genfunc_filter_matcher(vendor.sortname, :name) = true ) OR " +
                        " ( genfunc_filter_matcher(provider.name, :name) = true or genfunc_filter_matcher(provider.sortname, :name) = true ))"
            qParams << [name: "${params.org}"]
        }
        else if(params.vendor) {
            if (params.vendor instanceof Vendor) {
                qParts << "vendor = :vendor"
                qParams << [vendor: params.vendor]
            }
            else if(params.vendor instanceof String) {
                qParts << "( genfunc_filter_matcher(vendor.name, :name) = true or genfunc_filter_matcher(vendor.sortname, :name) = true )"
                qParams << [name: "${params.vendor}"]
            }
        }
        else if(params.provider) {
            if (params.provider instanceof Provider) {
                qParts << "provider = :provider"
                qParams << [provider: params.provider]
            }
            else if(params.provider instanceof String) {
                qParts << "( genfunc_filter_matcher(provider.name, :name) = true or genfunc_filter_matcher(provider.sortname, :name) = true )"
                qParams << [name: "${params.provider}"]
            }
        }

        if (params.type) {
            qParts << "(exists (select at from a.type as at where at.id in (:selectedTypes))) "
            qParams << [selectedTypes: Params.getLongList(params, 'type')]
        }

        Map<String, Object> instProvVenFilter = _getInstitutionProviderVendorFilter(params)
        if(instProvVenFilter.containsKey('qParts')) {
            qParts.add(instProvVenFilter.qParts)
            qParams.putAll(instProvVenFilter.qParams)
        }

        String query = "SELECT distinct(a), ${sortquery} FROM Address AS a left join a.org org left join a.provider provider left join a.vendor vendor WHERE " + qParts.join(" AND ")

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

    private Map<String, Object> _getInstitutionProviderVendorFilter(Map params) {
        List qParts = []
        Map qParams = [:]
        if (params.showOnlyContactPersonForInstitution || params.exportOnlyContactPersonForInstitution){
            qParts  << "exists (select os from OrgSetting os where os.org = org and os.key = :ct and os.roleValue in (:roles))"
            qParams << ['ct': OrgSetting.KEYS.CUSTOMER_TYPE]
            qParams << ['roles': customerTypeService.getOrgInstRoles()]
        }

        if (params.showOnlyContactPersonForProvider || params.exportOnlyContactPersonForProvider){
            qParts << "provider != null"
        }

        if (params.showOnlyContactPersonForVendor || params.exportOnlyContactPersonForVendor){
            qParts << "vendor != null"
        }
        if(qParts) {
            [qParts: "(${qParts.join(' or ')})", qParams: qParams]
        }
        else [:]
    }

}
