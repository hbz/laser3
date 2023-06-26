package de.laser.ajax

import de.laser.AlternativeName
import de.laser.CustomerTypeService
import de.laser.GenericOIDService
import de.laser.AccessService
import de.laser.CompareService
import de.laser.ContextService
import de.laser.ControlledListService
import de.laser.DataConsistencyService
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.LicenseService
import de.laser.LinksGenerationService
import de.laser.ReportingGlobalService
import de.laser.ReportingLocalService
import de.laser.SubscriptionDiscountScale
import de.laser.SubscriptionService
import de.laser.auth.Role
import de.laser.ctrl.SubscriptionControllerService
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.properties.LicenseProperty
import de.laser.Org
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.Platform
import de.laser.properties.PlatformProperty
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.Contact
import de.laser.Person
import de.laser.PersonRole
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.base.AbstractI10n
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.report.myInstitution.base.BaseConfig
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.text.SimpleDateFormat

/**
 * This controller manages calls to render maps of entries; mostly for live update of dropdown menus
 * IMPORTANT: only json rendering here, no object manipulation done here!
 * Object manipulation is done in the general AJAX controller!
 * @see AjaxController
 * @see AjaxHtmlController
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxJsonController {

    AccessService accessService
    CompareService compareService
    ContextService contextService
    ControlledListService controlledListService
    DataConsistencyService dataConsistencyService
    GenericOIDService genericOIDService
    LicenseService licenseService
    LinksGenerationService linksGenerationService
    ReportingGlobalService reportingGlobalService
    ReportingLocalService reportingLocalService
    SubscriptionService subscriptionService
    SubscriptionControllerService subscriptionControllerService

    /**
     * Test call
     * @return a sample object
     */
    @Secured(['ROLE_USER'])
    def test() {
        Map<String, Object> result = [status: 'ok']
        result.id = params.id
        render result as JSON
    }

    /**
     * Updates the subscription list for the copy elements target list; the subscription name output follows the dropdown naming convention specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: subscription name]
     */
    @Secured(['ROLE_USER'])
    def adjustSubscriptionList(){
        List data
        Set result = []
        Map queryParams = [:]

        queryParams.status = []
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }
        queryParams.showSubscriber = params.showSubscriber == 'true'
        queryParams.showConnectedObjs = params.showConnectedObjs == 'true'
        queryParams.forDropdown = true
        Org contextOrg = contextService.getOrg()

        data = subscriptionService.getMySubscriptions_readRights(queryParams)
        Map<Long, Map> subscriptionRows = [:]
        if (data) {

            if(params.showConnectedObjs == 'true') {
                data.addAll(linksGenerationService.getAllLinkedSubscriptionsForDropdown(data.collect { s -> s[0] } as Set<Long>))
            }
            data.each { s ->
                if(s[0] != params.long("context")) {
                    Map subscriptionRow = subscriptionRows.get(s[0])
                    if(!subscriptionRow)
                        subscriptionRow = [name: s[1], startDate: s[2], endDate: s[3], status: s[4], instanceOf: s[7], holdingSelection: s[8], orgRelations: [:]]
                    subscriptionRow.orgRelations.put(s[6].id, s[5])
                    subscriptionRows.put(s[0], subscriptionRow)
                }
            }
            subscriptionRows.each {Long subId, Map entry ->
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                String startDate = "", endDate = "", additionalInfo = ""
                if(entry.startDate)
                    startDate = sdf.format(entry.startDate)
                if(entry.endDate)
                    endDate = sdf.format(entry.endDate)
                if(entry.instanceOf) {
                    if(entry.orgRelations.get(RDStore.OR_SUBSCRIPTION_CONSORTIA.id).id == contextOrg.id) {
                        Org subscriber = entry.orgRelations.get(RDStore.OR_SUBSCRIBER_CONS.id)
                        if(!subscriber)
                            subscriber = entry.orgRelations.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id)
                        additionalInfo = " - ${subscriber?.sortname}"
                    }
                    else additionalInfo = " - ${message(code: 'gasco.filter.consortialLicence')}"
                }
                String text = "${entry.name} - ${entry.status.getI10n("value")} (${startDate} - ${endDate})${additionalInfo}"
                if (params.valueAsOID) {
                    result.add([value: "${Subscription.class.name}:${subId}", text: text, holdingSelection: entry.holdingSelection])
                }
                else {
                    result.add([value: subId, text: text])
                }
            }
        }
        render result as JSON
    }

    /**
     * Updates the license list for the copy elements target list; the license name output follows the dropdown naming convention specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: license name]
     */
    @Secured(['ROLE_USER'])
    def adjustLicenseList(){
        Set<License> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedObjs = params.showConnectedObjs == 'true'
        Map queryParams = [:]

        queryParams.status = []
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data =  licenseService.getMyLicenses_writeRights(queryParams)
        if (data) {
            data = data-License.get(params.context)
            if (params.valueAsOID){
                data.each { License l ->
                    result.add([value: genericOIDService.getOID(l), text: l.dropdownNamingConvention()])
                }
            } else {
                data.each { License l ->
                    result.add([value: l.id, text: l.dropdownNamingConvention()])
                }
            }
        }
        render result as JSON
    }

    /**
     * Updates the subscription list for the comparison pair list; the subscription name output follows the dropdown naming convention specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: subscription name]
     */
    @Secured(['ROLE_USER'])
    def adjustCompareSubscriptionList(){
        List<Subscription> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedObjs = params.showConnectedObjs == 'true'
        Map queryParams = [:]
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data = compareService.getMySubscriptions(queryParams)
        if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
            if (showSubscriber) {
                List parents = data.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
                data.addAll(Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc', [parents: parents, subscriberRoleTypes: subscriberRoleTypes]))
            }
        }

        if (showConnectedObjs){
            data.addAll(linksGenerationService.getAllLinkedSubscriptions(data, contextService.getUser()))
        }

        if (data) {
            data.unique()
            data.each { Subscription s ->
                result.add([value: s.id, text: s.dropdownNamingConvention()])
            }
            result.sort{it.text.toLowerCase()}
        }
        render result as JSON
    }

    /**
     * Updates the license list for the comparison pair list; the license name output follows the dropdown naming convention specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: license name]
     */
    @Secured(['ROLE_USER'])
    def adjustCompareLicenseList(){
        List<License> data
        List result = []
        boolean showSubscriber = params.showSubscriber == 'true'
        boolean showConnectedLics = params.showConnectedLics == 'true'
        Map queryParams = [:]
        if (params.get('status')){
            queryParams.status = params.list('status').collect{ Long.parseLong(it) }
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedLics = showConnectedLics

        data = compareService.getMyLicenses(queryParams)
        if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
            if (showSubscriber) {
                List parents = data.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                data.addAll(License.executeQuery('select l from License l join l.orgRelations oo where l.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc', [parents: parents, subscriberRoleTypes: subscriberRoleTypes]))
            }
        }

        if (data) {
            data.each { License l ->
                result.add([value: l.id, text: l.dropdownNamingConvention()])
            }
            result.sort{it.text}
        }
        render result as JSON
    }

    /**
     * Performs a query among the given domain objects matching the given key to find inconsistent entries
     * @return a {@link List} of entries matching the domain key, field key and value
     */
    @Secured(['ROLE_USER'])
    def consistencyCheck() {
        List result = dataConsistencyService.ajaxQuery(params.key, params.key2, params.value)
        render result as JSON
    }

    /**
     * Checks for the cost item input whether the given issue entitlement belongs indeed to the given package and subscription, if they are set at all
     * @return a {@link Map} of validation results
     */
    @Secured(['ROLE_USER'])
    def checkCascade() {
        Map<String, Object> result = [sub:true, subPkg:true, ie:true]
        if (!params.subscription && ((params.package && params.issueEntitlement) || params.issueEntitlement)) {
            result.sub = false
            result.subPkg = false
            result.ie = false
        }
        else if (params.subscription) {
            Subscription sub = (Subscription) genericOIDService.resolveOID(params.subscription)
            if (!sub) {
                result.sub = false
                result.subPkg = false
                result.ie = false
            }
            else if (params.issueEntitlement) {
                if (!params.package || params.package.contains('null')) {
                    result.subPkg = false
                    result.ie = false
                }
                else if (params.package && !params.package.contains('null')) {
                    SubscriptionPackage subPkg = (SubscriptionPackage) genericOIDService.resolveOID(params.package)
                    if(!subPkg || subPkg.subscription != sub) {
                        result.subPkg = false
                        result.ie = false
                    }
                    else {
                        IssueEntitlement ie = (IssueEntitlement) genericOIDService.resolveOID(params.issueEntitlement)
                        if(!ie || ie.subscription != subPkg.subscription || ie.tipp.pkg != subPkg.pkg) {
                            result.ie = false
                        }
                    }
                }
            }
        }
        render result as JSON
    }

    /**
     * Retrieves the dropdown values for an xEditableBoolean field
     * @return the dropdown value {@link Map} with structure [value: 0/1, text: RefdataValue of category {@link RDConstants#Y_N}]
     */
    @Secured(['ROLE_USER'])
    def getBooleans() {
        List result = [
                [value: 1, text: RDStore.YN_YES.getI10n('value')],
                [value: 0, text: RDStore.YN_NO.getI10n('value')]
        ]
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getProfilPageSizeList() {
        List result = [
                [value: 10, text: '10'],
                [value: 25, text: '25'],
                [value: 50, text: '50'],
                [value: 100, text: '100'],
        ]
        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getSubscriptionDiscountScaleList() {
        List result = []
        if(params.sub) {
            Subscription subscription = Subscription.findById(params.sub)
            println(subscription)
            if (subscription) {
                List<SubscriptionDiscountScale> subscriptionDiscountScaleList = SubscriptionDiscountScale.findAllBySubscription(subscription)
                subscriptionDiscountScaleList.each {
                    result << [value: it.id, text: it.name +': '+it.discount ]
                }
            }
        }
        render result as JSON
    }

    /*@Secured(['ROLE_USER'])
    def getLinkedLicenses() {
        render controlledListService.getLinkedObjects([destination:params.subscription, sourceType: License.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }

    @Secured(['ROLE_USER'])
    def getLinkedSubscriptions() {
        render controlledListService.getLinkedObjects([source:params.license, destinationType: Subscription.class.name, linkTypes:[RDStore.LINKTYPE_LICENSE], status:params.status]) as JSON
    }*/

    /**
     * Retrieves a list of reference data values belonging to the category linked to the property definition
     * @return a {@link List} of {@link Map}s of structure [value: database id, name: translated name] fpr dropdown display
     */
    @Secured(['ROLE_USER'])
    def getPropRdValues() {
        List<Map<String, Object>> result = []
        if (params.oid) {
            PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
            if (pd && pd.isRefdataValueType()) {
                RefdataCategory.getAllRefdataValues(pd.refdataCategory).each {
                    result.add([ value: it.id, name: it.getI10n('value') ])
                }
                result = result.sort { x, y -> x.name.compareToIgnoreCase(y.name) }
            }
        }
        render result as JSON
    }

    /**
     * Retrieves a list of values belonging to the given property definition
     * @return a {@link List} of {@link Map}s of structure [value: value, name: translated name] fpr dropdown display; value may be: reference data value key, date or integer/free text value
     */
    @Secured(['ROLE_USER'])
    def getPropValues() {
        List<Map<String, Object>> result = []

        if (params.oid != "undefined") {
            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
            if (propDef) {
                List<AbstractPropertyWithCalculatedLastUpdated> values
                if (propDef.tenant) {
                    switch (propDef.descr) {
                        case PropertyDefinition.SUB_PROP: values = SubscriptionProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.ORG_PROP: values = OrgProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.PLA_PROP: values = PlatformProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.PRS_PROP: values = PersonProperty.findAllByType(propDef)
                            break
                        case PropertyDefinition.LIC_PROP: values = LicenseProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                    }
                }
                else {
                    switch (propDef.descr) {
                        case PropertyDefinition.SUB_PROP:
                            String consortialFilter = contextService.getOrg().isCustomerType_Consortium() ? ' and sp.owner.instanceOf = null' : ''
                            values = SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp left join sp.owner.orgRelations oo where sp.type = :propDef and ((sp.tenant = :tenant or ((sp.tenant != :tenant and sp.isPublic = true) or sp.instanceOf != null) and :tenant in oo.org))'+consortialFilter,[propDef:propDef, tenant:contextService.getOrg()])
                            break
                        case PropertyDefinition.ORG_PROP: values = OrgProperty.executeQuery('select op from OrgProperty op where op.type = :propDef and ((op.tenant = :tenant and op.isPublic = true) or op.tenant = null)',[propDef:propDef,tenant:contextService.getOrg()])
                            break
                    /*case PropertyDefinition.PLA_PROP: values = PlatformProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                        break
                    case PropertyDefinition.PRS_PROP: values = PersonProperty.findAllByType(propDef)
                        break*/
                        case PropertyDefinition.LIC_PROP:
                            String consortialFilter = contextService.getOrg().isCustomerType_Consortium() ? ' and lp.owner.instanceOf = null' : ''
                            values = LicenseProperty.executeQuery('select lp from LicenseProperty lp left join lp.owner.orgRelations oo where lp.type = :propDef and ((lp.tenant = :tenant or ((lp.tenant != :tenant and lp.isPublic = true) or lp.instanceOf != null) and :tenant in oo.org))'+consortialFilter,[propDef:propDef, tenant:contextService.getOrg()])
                            break
                    }
                }
                if (values) {
                    //very ugly, needs a more elegant solution
                    if (propDef.isIntegerType()) {
                        values.intValue.findAll().unique().each { v ->
                            result.add([value:v,text:v])
                        }
                        result = result.sort { x, y -> x.text.compareTo(y.text) }
                    }
                    else if (propDef.isDateType()) {
                        values.dateValue.findAll().unique().sort().reverse().each { v ->
                            String vt = g.formatDate(formatName:"default.date.format.notime", date:v)
                            result.add([value: vt, text: vt])
                        }
                    }
                    else if (propDef.isRefdataValueType()) {
                        values.each { AbstractPropertyWithCalculatedLastUpdated v ->
                            if (v.getValue() != null)
                                result.add([value:v.getValue(),text:v.refValue.getI10n("value")])
                        }
                        result = result.sort { x, y -> x.text.compareToIgnoreCase(y.text) }
                    }
                    else {
                        values.value?.findAll()?.unique()?.each { v ->
                            result.add([value:v,text:v])
                        }
                        result = result.sort { x, y -> x.text.compareToIgnoreCase(y.text) }
                    }
                }
            }
        }
        //excepted structure: [[value:,text:],[value:,text:]]

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def getOwnerStatus() {
        List<Map<String, Object>> result = []

        if (params.oid != "undefined") {
            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
            if (propDef) {
                List<RefdataValue> statusList = []
                switch(propDef.descr) {
                    case PropertyDefinition.SUB_PROP: statusList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                        break
                    case PropertyDefinition.ORG_PROP: statusList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ORG_STATUS)-RDStore.ORG_STATUS_DELETED)
                        break
                    case PropertyDefinition.PLA_PROP: statusList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATUS)-RDStore.PLATFORM_STATUS_DELETED)
                        break
                    case PropertyDefinition.LIC_PROP: statusList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS))
                        break
                }
                //excepted structure: [[value:,text:],[value:,text:]]
                statusList.each { RefdataValue status ->
                    result << [value: status.id, text: status.getI10n("value")]
                }
            }
        }
        render result as JSON
    }

    /**
     * Retrieves provider {@link Org}s with their private contacts; the result may be filtered by name
     * @return a {@link Map} containing entries for a DataTables table output
     */
    @Secured(['ROLE_USER'])
    def getProvidersWithPrivateContacts() {
        Map<String, Object> result = [:]
        String fuzzyString = params.sSearch ? ('%' + params.sSearch.trim().toLowerCase() + '%') : '%'

        Map<String, Object> query_params = [
                name: fuzzyString,
                status: RDStore.O_STATUS_DELETED
        ]
        String countQry = "select count(o) from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like :name and (o.status is null or o.status != :status)"
        String rowQry = "select o from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like :name and (o.status is null or o.status != :status) order by o.name asc"

        List cq = Org.executeQuery(countQry,query_params)

        List<Org> rq = Org.executeQuery(rowQry,
                query_params,
                [max:params.iDisplayLength?:1000,offset:params.iDisplayStart?:0])

        result.aaData = []
        result.sEcho = params.sEcho
        result.iTotalRecords = cq[0]
        result.iTotalDisplayRecords = cq[0]

        Org currOrg = (Org) genericOIDService.resolveOID(params.oid)
        List<Person> contacts = Person.findAllByContactTypeAndTenant(RDStore.PERSON_CONTACT_TYPE_PERSONAL, currOrg)

        LinkedHashMap personRoles = [:]
        PersonRole.findAll().each { PersonRole prs ->
            personRoles.put(prs.org, prs.prs)
        }
        rq.each { Org it ->
            int ctr = 0
            LinkedHashMap row = [:]
            String name = it["name"]
            if (personRoles.get(it) && contacts.indexOf(personRoles.get(it)) > -1)
                name += '<span data-tooltip="Persönlicher Kontakt vorhanden"><i class="address book icon"></i></span>'
            row["${ctr++}"] = name
            row["DT_RowId"] = "${it.class.name}:${it.id}"
            result.aaData.add(row)
        }

        render result as JSON
    }

    /**
     * Retrieves the region list for German speaking countries
     * @return a {@link Set} of reference data entries
     */
    @Secured(['ROLE_USER'])
    def getRegions() {
        SortedSet<RefdataValue> result = new TreeSet<RefdataValue>()
        if (params.country) {
            List<String> countryIds = params.country.split(',')
            countryIds.each { String c ->
                switch (RefdataValue.get(Long.parseLong(c)).value) {
                    case 'DE':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE]) )
                        break
                    case 'AT':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_AT]) )
                        break
                    case 'CH':
                        result.addAll( RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_CH]) )
                        break
                }
            }
        }
        else {
            result.addAll(RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE,RDConstants.REGIONS_AT,RDConstants.REGIONS_CH]))
        }

        if (params.simple) {
            render result.collect { it -> [id: it.id, value: it.value, value_de: it.value_de, value_en: it.value_en]} as JSON
        }
        else {
            render result as JSON
        }
    }

    /**
     * Triggers the lookup of values for the given domain class; serves as fallback for static refdataFind calls
     * @return a sorted {@link List} of {@link Map}s of structure [id: oid, text: subscription text] with the query results
     */
    @Secured(['ROLE_USER'])
    def lookup() {
        params.shortcode  = contextService.getOrg().shortcode

        Map<String, Object> result = [values: []]
        params.max = params.max ?: 40

        Class dc = CodeUtils.getDomainClass(params.baseClass)
        if (dc) {
            result.values = dc.refdataFind(params)
            result.values.sort{ x,y -> x.text.compareToIgnoreCase y.text }
        }
        else {
            log.error("Unable to locate domain class ${params.baseClass}")
        }

        render result as JSON
    }

    /**
     * Retrieves a list of budget codes for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getBudgetCodes(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupBudgetCodes() {
        render controlledListService.getBudgetCodes(params) as JSON
    }

    /**
     * Retrieves a list of various elements for dropdown display; was used for the myInstitution/document view to attach documents to all kinds of objects
     * @see de.laser.DocContext
     * @see de.laser.Doc
     * @return the result of {@link de.laser.ControlledListService#getElements(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupCombined() {
        render controlledListService.getElements(params) as JSON
    }

    /**
     * Retrieves a list of invoice numbers for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getInvoiceNumbers(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupInvoiceNumbers() {
        render controlledListService.getInvoiceNumbers(params) as JSON
    }

    /**
     * Retrieves a list of issue entitlements for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getIssueEntitlements(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupIssueEntitlements() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getIssueEntitlements(params) as JSON
        } else {
            Map entry = ["results": []]
            render entry as JSON
        }
    }

    /**
     * Retrieves a list of licenses for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getLicenses(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupLicenses() {
        render controlledListService.getLicenses(params) as JSON
    }

    /**
     * Retrieves a list of order numbers for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getOrderNumbers(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupOrderNumbers() {
        render controlledListService.getOrderNumbers(params) as JSON
    }

    /**
     * Retrieves a list of provider and agency {@link Org}s for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getProvidersAgencies(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupProvidersAgencies() {
        render controlledListService.getProvidersAgencies(params) as JSON
    }

    /**
     * Retrieves a list of {@link Org}s in general for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getOrgs(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupOrgs() {
        render controlledListService.getOrgs(params) as JSON
    }

    /**
     * Retrieves a list of provider {@link Org}s and their associated {@link Platform}s for dropdown display
     * @return a {@link List} of {@link Map}s of structure
     * {
     *   name: provider name,
     *   value: platform oid,
     *   platforms: {
     *     name: platform name,
     *     value: platform oid
     *   }
     * }
     */
    @Secured(['ROLE_USER'])
    def lookupProviderAndPlatforms() {
        List result = []

        List<Org> provider = Org.executeQuery('SELECT o FROM Org o JOIN o.orgType ot WHERE ot = :ot', [ot: RDStore.OT_PROVIDER])
        provider.each{ prov ->
            Map<String, Object> pp = [name: prov.name, value: prov.class.name + ":" + prov.id, platforms:[]]

            Platform.findAllByOrg(prov).each { plt ->
                pp.platforms.add([name: plt.name, value: plt.class.name + ":" + plt.id])
            }
            result.add(pp)
        }
        render result as JSON
    }

    /**
     * Retrieves a list of cost item references for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getReferences(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupReferences() {
        render controlledListService.getReferences(params) as JSON
    }

    /**
     * Retrieves all {@link Role}s; the roles may be filtered by type
     * @return a {@link List} of {@link Map}s of structure [text: translated role designator string, key: translated {@link Role#authority} string, value: role oid]
     */
    @Secured(['ROLE_USER'])
    def lookupRoles() {
        List result = []
        List<Role> roles = params.type ? Role.findAllByRoleType(params.type.toLowerCase()) :  Role.findAll()

        roles.each { r ->
            result.add([text: message(code:'cv.roles.' + r.authority), key: "${r.getI10n('authority')}", value: "${r.class.name}:${r.id}"])
        }

        render result as JSON
    }

    /**
     * Retrieves a list of subscriptions for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getSubscriptions(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupSubscriptions() {
        render controlledListService.getSubscriptions(params) as JSON
    }

    /**
     * Retrieves a list of subscription packages for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getSubscriptionPackages(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupSubscriptionPackages() {
        if (params.ctx != "undefined") {
            render controlledListService.getSubscriptionPackages(params) as JSON
        }
        else {
            Map empty = [results: []]
            render empty as JSON
        }
    }

    /**
     * Retrieves a list of subscriptions and licenses for dropdown display
     * @return the composite result of {@link de.laser.ControlledListService#getLicenses(java.util.Map)} and {@link de.laser.ControlledListService#getSubscriptions(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupSubscriptionsLicenses() {
        Map<String, Object> result = [results:[]]
        result.results.addAll(controlledListService.getSubscriptions(params).results)
        result.results.addAll(controlledListService.getLicenses(params).results)

        render result as JSON
    }

    /**
     * Retrieves a list of current and intended subscriptions for dropdown display
     * @return the filtered result of {@link de.laser.ControlledListService#getSubscriptions(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupCurrentAndIndendedSubscriptions() {
        params.status = [RDStore.SUBSCRIPTION_INTENDED, RDStore.SUBSCRIPTION_CURRENT]

        render controlledListService.getSubscriptions(params) as JSON
    }

    /**
     * Retrieves a list of title groups for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getTitleGroups(java.util.Map)}
     */
    @Secured(['ROLE_USER'])
    def lookupTitleGroups() {
        params.checkView = true
        if(params.sub != "undefined") {
            render controlledListService.getTitleGroups(params) as JSON
        } else {
            Map empty = [results: []]
            render empty as JSON
        }
    }

    /**
     * Retrieves all reference data values by the given category for dropdown display
     * @return a {@link List} of {@link Map}s of structure [value: reference data value oid, text: translated reference data value]
     */
    @Secured(['ROLE_USER'])
    def refdataSearchByCategory() {
        List result = []

        RefdataCategory rdc
        if(params.oid)
            rdc = (RefdataCategory) genericOIDService.resolveOID(params.oid)
        else if(params.cat)
            rdc = RefdataCategory.getByDesc(params.cat)
        if (rdc) {
            String lang = LocaleUtils.getCurrentLang()
            String query = "select rdv from RefdataValue as rdv where rdv.owner.id='${rdc.id}' order by rdv.order, rdv.value_" + lang

            List<RefdataValue> rq = RefdataValue.executeQuery(query, [], [max: params.iDisplayLength ?: 1000, offset: params.iDisplayStart ?: 0])

            rq.each { RefdataValue it ->
                if (it instanceof AbstractI10n) {
                    result.add([value: "${genericOIDService.getOID(it)}", text: "${it.getI10n('value')}"])
                }
                else {
                    String value = it.value
                    if (value) {
                        String no_ws = value.replaceAll(' ', '')
                        String locale_text = message(code: "refdata.${no_ws}", default: "${value}")
                        result.add([value: "${it.class.name}:${it.id}", text: "${locale_text}"])
                    }
                }
            }
        }
        if (result) {
            RefdataValue notSet = RDStore.GENERIC_NULL_VALUE
            result.add([value: "${genericOIDService.getOID(notSet)}", text: "${notSet.getI10n('value')}"])
        }

        render result as JSON
    }

    @Secured(['ROLE_USER'])
    def removeObject() {
        int removed = 0
        switch(params.object) {
            case "altname": removed = AlternativeName.executeUpdate('delete from AlternativeName altname where altname.id = :id', [id: params.long("objId")])
                break
            case "coverage": //TODO
                break
        }
        Boolean success = removed > 0
        Map<String, Boolean> result = [success: success]
        render result as JSON
    }

    /**
     * Searches for property definition alternatives based on the OID of the property definition which should be replaced
     * @return a {@link List} of {@link Map}s of structure [value: result oid, text: translated property definition name, isPrivate: does the property definition have a tenant?]
     * @see PropertyDefinition
     */
    @Secured(['ROLE_USER'])
    def searchPropertyAlternativesByOID() {
        List<Map<String, Object>> result = []
        PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
        Org contextOrg = contextService.getOrg()
        List<PropertyDefinition> queryResult
        if(pd.refdataCategory) {
            queryResult = PropertyDefinition.executeQuery('select pd from PropertyDefinition pd where pd.descr = :descr and pd.refdataCategory = :refdataCategory and pd.type = :type and pd.multipleOccurrence = :multiple and (pd.tenant = :tenant or pd.tenant is null)',
                    [descr   : pd.descr,
                     refdataCategory: pd.refdataCategory,
                     type    : pd.type,
                     multiple: pd.multipleOccurrence,
                     tenant  : contextOrg])
        }
        else {
            queryResult = PropertyDefinition.executeQuery('select pd from PropertyDefinition pd where pd.descr = :descr and pd.type = :type and pd.multipleOccurrence = :multiple and (pd.tenant = :tenant or pd.tenant is null)',
                    [descr   : pd.descr,
                     type    : pd.type,
                     multiple: pd.multipleOccurrence,
                     tenant  : contextOrg])
        }

        queryResult.each { PropertyDefinition it ->
            PropertyDefinition rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
            if (pd.isUsedForLogic) {
                if (it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}", isPrivate: rowobj.tenant ? true : false])
                }
            }
            else {
                if (! it.isUsedForLogic) {
                    result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n('name')}", isPrivate: rowobj.tenant ? true : false])
                }
            }
        }
        if (result.size() > 1) {
            result.sort{ x,y -> x.text.compareToIgnoreCase y.text }
        }

        render result as JSON
    }

    /**
     * Validation query; checks if the user with the given username exists
     * @return true if there is a {@link User} matching the given input query, false otherwise
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_ADM'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_ADM')
    })
    def checkExistingUser() {
        Map<String, Object> result = [result: false]

        if (params.input) {
            result.result = null != User.findByUsernameIlike(params.input)
        }
        render result as JSON
    }

    /**
     * Retrieves a list of email addresses, matching the specified request parameters
     * @return a {@link List} of email addresses matching the parameters
     */
    @Secured(['ROLE_USER'])
    def getEmailAddresses() {
        List result = []

        if (params.orgIdList) {
            List<Long> orgIds = params.orgIdList.split(',').collect{ Long.parseLong(it) }
            List<Org> orgList = orgIds ? Org.findAllByIdInList(orgIds) : []

            String query = "select distinct p from Person as p inner join p.roleLinks pr where pr.org in (:orgs) "
            Map<String, Object> queryParams = [orgs: orgList]

            boolean showPrivateContactEmails = Boolean.valueOf(params.isPrivate)
            boolean showPublicContactEmails = Boolean.valueOf(params.isPublic)

            if (showPublicContactEmails && showPrivateContactEmails){
                query += "and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) ) "
                queryParams << [ctx: contextService.getOrg()]
            } else {
                if (showPublicContactEmails){
                    query += "and p.isPublic = true "
                } else if (showPrivateContactEmails){
                    query += "and (p.isPublic = false and p.tenant = :ctx) "
                    queryParams << [ctx: contextService.getOrg()]
                } else {
                    return [] as JSON
                }
            }

            if (params.selectedRoleTypIds) {
                List<Long> selectedRoleTypIds = params.selectedRoleTypIds.split(',').collect { Long.parseLong(it) }
                List<RefdataValue> selectedRoleTypes = selectedRoleTypIds ? RefdataValue.findAllByIdInList(selectedRoleTypIds) : []

                if (selectedRoleTypes) {
                    query += "and pr.functionType in (:selectedRoleTypes) "
                    queryParams << [selectedRoleTypes: selectedRoleTypes]
                }
            }

            List<Person> persons = Person.executeQuery(query, queryParams)
            if (persons) {
                result = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                        [persons: persons, contentType: RDStore.CCT_EMAIL])
            }
        }

        render result as JSON
    }

    // ----- reporting -----

    /**
     * Checks the current state of the reporting cache
     * @return a {@link Map} reflecting the existence, the filter cache, query cache and details cache states
     */
    @Secured(['ROLE_USER'])
    def checkReportingCache() {

        Map<String, Object> result = [
            exists: false
        ]

        if (params.context in [ BaseConfig.KEY_MYINST, BaseConfig.KEY_SUBSCRIPTION ]) {
            ReportingCache rCache

            if (params.token) {
                rCache = new ReportingCache( params.context, params.token )
                result.token = params.token
            }
            else {
                rCache = new ReportingCache( params.context )
            }

            result.exists       = rCache.exists()
            result.filterCache  = rCache.get().filterCache ? true : false
            result.queryCache   = rCache.get().queryCache ? true : false
            result.detailsCache = rCache.get().detailsCache ? true : false
        }

        render result as JSON
    }

    /**
     * Outputs a chart from the given report parameters
     * @return the template to output and the one of the results {@link de.laser.ReportingGlobalService#doChart(java.util.Map, grails.web.servlet.mvc.GrailsParameterMap)} or {@link de.laser.ReportingLocalService#doChart(java.util.Map, grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @DebugInfo(ctxPermAffiliation = [CustomerTypeService.PERMS_PRO, 'INST_USER'])
    @Secured(closure = {
        ctx.accessService.ctxPermAffiliation(CustomerTypeService.PERMS_PRO, 'INST_USER')
    })
    def chart() {
        Map<String, Object> result = [:]

        try {
            if (params.context == BaseConfig.KEY_MYINST) {
                reportingGlobalService.doChart(result, params) // manipulates result
            }
            else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
                reportingLocalService.doChart(result, params) // manipulates result
            }
        } catch (Exception e) {
            log.error( e.getMessage() )
            e.printStackTrace()
            result.remove('data')
            result.remove('dataDetails')
        }
        if (result.tmpl) {
            render template: result.tmpl, model: result
        }
        else {
            render result as JSON
        }
    }
}