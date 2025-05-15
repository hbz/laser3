package de.laser.ajax

import de.laser.AlternativeName
import de.laser.CacheService
import de.laser.CustomerTypeService
import de.laser.DiscoverySystemFrontend
import de.laser.DiscoverySystemIndex
import de.laser.GenericOIDService
import de.laser.CompareService
import de.laser.ContextService
import de.laser.ControlledListService
import de.laser.DataConsistencyService
import de.laser.IssueEntitlement
import de.laser.IssueEntitlementCoverage
import de.laser.License
import de.laser.LicenseService
import de.laser.LinksGenerationService
import de.laser.PendingChange
import de.laser.finance.CostInformationDefinition
import de.laser.finance.CostItem
import de.laser.wekb.Package
import de.laser.wekb.Provider
import de.laser.ProviderService
import de.laser.ReportingGlobalService
import de.laser.ReportingLocalService
import de.laser.SubscriptionDiscountScale
import de.laser.SubscriptionService
import de.laser.wekb.Vendor
import de.laser.VendorService
import de.laser.auth.Role
import de.laser.cache.EhcacheWrapper
import de.laser.finance.PriceItem
import de.laser.helper.Params
import de.laser.properties.ProviderProperty
import de.laser.properties.VendorProperty
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.properties.LicenseProperty
import de.laser.Org
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.PlatformProperty
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.base.AbstractI10n
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
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
 * @see AjaxOpenController
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxJsonController {

    CacheService cacheService
    CompareService compareService
    ContextService contextService
    ControlledListService controlledListService
    DataConsistencyService dataConsistencyService
    GenericOIDService genericOIDService
    LicenseService licenseService
    LinksGenerationService linksGenerationService
    ProviderService providerService
    ReportingGlobalService reportingGlobalService
    ReportingLocalService reportingLocalService
    SubscriptionService subscriptionService
    VendorService vendorService

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
            queryParams.status = Params.getLongList(params, 'status')
        }
        queryParams.showSubscriber = params.showSubscriber == 'true'
        queryParams.showConnectedObjs = params.showConnectedObjs == 'true'
        queryParams.forDropdown = true

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
                    if(entry.orgRelations.get(RDStore.OR_SUBSCRIPTION_CONSORTIUM.id).id == contextService.getOrg().id) {
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
            queryParams.status = Params.getLongList(params, 'status')
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

    @Secured(['ROLE_USER'])
    def adjustCostInformationValueList() {
        List result = []
        CostInformationDefinition cif = CostInformationDefinition.get(params.costInformationDefinition)
        if(cif.type == RefdataValue.class.name) {
            RefdataCategory.getAllRefdataValues(cif.refdataCategory).each { RefdataValue rv ->
                result.add([value: rv.id, text: rv.getI10n('value')])
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
            queryParams.status = Params.getLongList(params, 'status')
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedObjs = showConnectedObjs

        data = compareService.getMySubscriptions(queryParams)
        if (contextService.getOrg().isCustomerType_Consortium()) {
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
            queryParams.status = Params.getLongList(params, 'status')
        }

        queryParams.showSubscriber = showSubscriber
        queryParams.showConnectedLics = showConnectedLics

        data = compareService.getMyLicenses(queryParams)
        if (contextService.getOrg().isCustomerType_Consortium()) {
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
        Map<String, Object> result = [sub:true, pkg:true, ie:true]
        if (!params.subscription && ((params.package && params.issueEntitlement) || params.issueEntitlement)) {
            result.sub = false
            result.pkg = false
            result.ie = false
        }
        else if (params.subscription) {
            Subscription sub = (Subscription) genericOIDService.resolveOID(params.subscription)
            if (!sub) {
                result.sub = false
                result.pkg = false
                result.ie = false
            }
            else if (params.issueEntitlement) {
                if (!params.package || params.package.contains('null')) {
                    result.pkg = false
                    result.ie = false
                }
                else if (params.package && !params.package.contains('null')) {
                    Package pkg = (Package) genericOIDService.resolveOID(params.package)
                    SubscriptionPackage subPkg = (SubscriptionPackage) SubscriptionPackage.findBySubscriptionAndPkg(sub, pkg)
                    if(!pkg || subPkg.subscription != sub) {
                        result.pkg = false
                        result.ie = false
                    }
                    else {
                        IssueEntitlement ie = (IssueEntitlement) genericOIDService.resolveOID(params.issueEntitlement)
                        if(!ie || ie.subscription != subPkg.subscription || ie.tipp.pkg != pkg) {
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

    /**
     * Called from views/profile/index.gsp
     * Gets the selectable values for the default result count per page dropdown
     * @return a {@link Map} containing the default values as value:text pairs
     */
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

    /**
     * Called from subTransfer.gsp and currentSubTransfers.gsp
     * Gets the list of subscription discount scales registered for the given subscription
     * @return a {@link List} of {@link Map}s of structure [value: database id, text: name: discount] for dropdown display
     * @see Subscription
     * @see SubscriptionDiscountScale
     */
    @Secured(['ROLE_USER'])
    def getSubscriptionDiscountScaleList() {
        List result = []
        if(params.sub) {
            Subscription subscription = Subscription.findById(params.sub)
            if (subscription) {
                List<SubscriptionDiscountScale> subscriptionDiscountScaleList = SubscriptionDiscountScale.findAllBySubscription(subscription)
                subscriptionDiscountScaleList.each {
                    result << [value: it.id, text: it.name +': '+it.discount ]
                }
            }
        }
        render result as JSON
    }

    /**
     * Retrieves a list of reference data values belonging to the category linked to the property definition
     * @return a {@link List} of {@link Map}s of structure [value: database id, name: translated name] for dropdown display
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
     * @return a {@link List} of {@link Map}s of structure [value: value, name: translated name] for dropdown display; value may be: reference data value key, date or integer/free text value
     */
    @Secured(['ROLE_USER'])
    def getPropValues() {
        List<Map<String, Object>> result = []

        if (params.oid != "undefined") {
            def obj = genericOIDService.resolveOID(params.oid)
            if (obj instanceof PropertyDefinition) {
                PropertyDefinition propDef = (PropertyDefinition) obj
                List<AbstractPropertyWithCalculatedLastUpdated> values = []
                if (propDef.tenant) {
                    switch (propDef.descr) {
                        case PropertyDefinition.SUB_PROP: values = SubscriptionProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.ORG_PROP: values = OrgProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.PRV_PROP: values = ProviderProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
                            break
                        case PropertyDefinition.VEN_PROP: values = VendorProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
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
                        /*case PropertyDefinition.ORG_PROP: values = OrgProperty.executeQuery('select op from OrgProperty op where op.type = :propDef and ((op.tenant = :tenant and op.isPublic = true) or op.tenant = null)',[propDef:propDef,tenant:contextService.getOrg()])
                            break
                    case PropertyDefinition.PLA_PROP: values = PlatformProperty.findAllByTypeAndTenantAndIsPublic(propDef,contextService.getOrg(),false)
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
                    if (propDef.isLongType()) {
                        values.longValue.findAll().unique().each { v ->
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
            else if(obj instanceof CostInformationDefinition) {
                CostInformationDefinition cif = (CostInformationDefinition) obj
                String subFilter = ''
                Map<String, Object> queryParams = [cif: cif, ctx: contextService.getOrg()]
                if(params.containsKey('subscription')) {
                    subFilter = 'and (ci.sub.id = :sub or ci.sub.instanceOf.id = :sub)'
                    queryParams.sub = params.long('subscription')
                }
                if(cif.type == RefdataValue.class.name) {
                    Set values = CostItem.executeQuery('select ci.costInformationRefValue from CostItem ci join ci.costInformationRefValue rv where ci.costInformationDefinition = :cif and ci.owner = :ctx '+subFilter+' order by rv.'+LocaleUtils.getLocalizedAttributeName('value'), queryParams)
                    result = values.collect { RefdataValue ciiRefValue -> [value: ciiRefValue.id, text: ciiRefValue.getI10n('value')] }
                }
                else {
                    Set values = CostItem.executeQuery('select ci.costInformationStringValue from CostItem ci where ci.costInformationDefinition = :cif and ci.owner = :ctx '+subFilter+'  order by lower(ci.costInformationStringValue)', queryParams)
                    result = values.collect { String ciiStrValue -> [value: ciiStrValue, text: ciiStrValue] }
                }
            }
        }
        //excepted structure: [[value:,text:],[value:,text:]]

        render result as JSON
    }

    /**
     * Gets the list of selectable status for the given property's owner object type
     * @return a {@link List} of {@link Map}s of structure [value: database id, text: translated name] for dropdown display
     * @see PropertyDefinition#descr
     */
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
//                    case PropertyDefinition.ORG_PROP: [] // not used in _genericFilter.gsp
//                        break
                    case PropertyDefinition.PLA_PROP: statusList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATUS)-RDStore.PLATFORM_STATUS_REMOVED)
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
     * Retrieves the region list for German speaking countries
     * @return a {@link Set} of reference data entries
     */
    @Secured(['ROLE_USER'])
    def getRegions() {
        SortedSet<RefdataValue> result = new TreeSet<RefdataValue>()
        if (params.country) {
            List<Long> countryIds = Params.getLongList_forCommaSeparatedString(params, 'country')
            countryIds.each { c ->
                switch (RefdataValue.get(c).value) {
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
     * Reads from cache the current completion percentage of the given process in order to update the progress bar
     */
    @Secured(['ROLE_USER'])
    def checkProgress() {
        EhcacheWrapper userCache = contextService.getUserCache(params.cachePath)
        Map<String, Object> result = [percent: userCache.get('progress'), label: userCache.get('label')]
        if(result.percent == 100) {
            userCache.remove('progress')
            userCache.remove('label')
        }
        render result as JSON
    }

    /**
     * Updates the pagination cache
     */
    @Secured(['ROLE_USER'])
    def updatePaginationCache() {
        Map result = [:]
        if(params.containsKey('cacheKeyReferer')) {
            EhcacheWrapper cache = cacheService.getTTL1800Cache("${params.cacheKeyReferer}/pagination")
            Map<String, String> checkedMap = cache.get('checkedMap') ?: [:]
            if(params.containsKey('selId')) {
                if(!checkedMap.containsKey(params.selId)) {
                    checkedMap.put(params.selId, params.selId.split('_')[1])
                    result.state = 'checked'
                }
                else {
                    cache.remove('membersListToggler')
                    checkedMap.remove(params.selId)
                    result.state = 'unchecked'
                }
            }
            else if(params.containsKey('allIds[]')) {
                if(!cache.get('membersListToggler')) {
                    cache.put('membersListToggler', 'checked')
                    result.state = 'checked'
                }
                else {
                    cache.remove('membersListToggler')
                    result.state = 'unchecked'
                }
                List allIds = params.list('allIds[]')
                allIds.each { String id ->
                    String key = 'selectedSubs_'+id
                    if(!checkedMap.containsKey(key)) {
                        checkedMap.put(key, id)
                    }
                    else {
                        checkedMap.remove(key)
                    }
                }
            }
            cache.put('checkedMap', checkedMap)
            render result as JSON
        }
        else {
            response.sendError(500, 'cacheKeyReferer missing! Which pagination should I update?!')
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
     * @return the result of {@link de.laser.ControlledListService#getBudgetCodes(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupBudgetCodes() {
        render controlledListService.getBudgetCodes(params) as JSON
    }

    /**
     * Retrieves a list of various elements for dropdown display; was used for the myInstitution/document view to attach documents to all kinds of objects
     * @see de.laser.DocContext
     * @see de.laser.Doc
     * @return the result of {@link de.laser.ControlledListService#getElements(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupCombined() {
        render controlledListService.getElements(params) as JSON
    }

    /**
     * Retrieves a list of invoice numbers for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getInvoiceNumbers(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupInvoiceNumbers() {
        render controlledListService.getInvoiceNumbers(params) as JSON
    }

    /**
     * Retrieves a list of issue entitlements for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getIssueEntitlements(grails.web.servlet.mvc.GrailsParameterMap)}
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
     * @return the result of {@link de.laser.ControlledListService#getLicenses(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupLicenses() {
        render controlledListService.getLicenses(params) as JSON
    }

    /**
     * Retrieves a list of order numbers for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getOrderNumbers(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupOrderNumbers() {
        render controlledListService.getOrderNumbers(params) as JSON
    }

    /**
     * Retrieves a list of {@link Org}s in general for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getOrgs(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupOrgs() {
        render controlledListService.getOrgs(params) as JSON
    }

    /**
     * Retrieves a list of cost item references for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getReferences(grails.web.servlet.mvc.GrailsParameterMap)}
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
     * @return the result of {@link de.laser.ControlledListService#getSubscriptions(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupSubscriptions() {
        render controlledListService.getSubscriptions(params) as JSON
    }

    /**
     * Retrieves a list of platforms for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getPlatforms(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupPlatforms() {
        render controlledListService.getPlatforms(params) as JSON
    }

    /**
     * Retrieves a list of providers for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getProviders(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupProviders() {
        render controlledListService.getProviders(params) as JSON
    }

    /**
     * Retrieves a list of vendors for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getVendors(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupVendors() {
        render controlledListService.getVendors(params) as JSON
    }

    /**
     * Retrieves a list of packages for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getPackages(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupPackages() {
        if (params.ctx != "undefined") {
            render controlledListService.getPackages(params) as JSON
        }
        else {
            Map empty = [results: []]
            render empty as JSON
        }
    }

    /**
     * Retrieves a list of subscription packages for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getSubscriptionPackages(grails.web.servlet.mvc.GrailsParameterMap)}
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
     * @return the composite result of {@link de.laser.ControlledListService#getLicenses(grails.web.servlet.mvc.GrailsParameterMap)} and {@link de.laser.ControlledListService#getSubscriptions(grails.web.servlet.mvc.GrailsParameterMap)}
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
     * @return the filtered result of {@link de.laser.ControlledListService#getSubscriptions(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupCurrentAndIndendedSubscriptions() {
        params.status = [RDStore.SUBSCRIPTION_INTENDED, RDStore.SUBSCRIPTION_CURRENT]

        render controlledListService.getSubscriptions(params) as JSON
    }

    /**
     * Retrieves a list of title groups for dropdown display
     * @return the result of {@link de.laser.ControlledListService#getTitleGroups(grails.web.servlet.mvc.GrailsParameterMap)}
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
     * Call to retrieve all possible first authors of titles for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleSimpleFieldValues() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleSimpleFieldValuesByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles, params.fieldName)
                break
            case 'status': result.results = controlledListService.getAllPossibleSimpleFieldValuesByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleSimpleFieldValuesBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles, params.fieldName)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible title subjects for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleSubjects() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleSubjectsByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleSubjectsByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleSubjectsBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible Dewey decimal classifications (DDCs) for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleDdcs() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleDdcsByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleDdcsByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleDdcsBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible title languages for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleLanguages() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleLanguagesByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleLanguagesByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleLanguagesBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible first online publishing years for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleDateFirstOnlineYears() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleDateFirstOnlineYearByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleDateFirstOnlineYearByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleDateFirstOnlineYearBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible title medium types for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleMediumTypes() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleMediumTypesByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleMediumTypesByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleMediumTypesBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible title publication types for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleTitleTypes() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleTitleTypesByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleTitleTypesByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleTitleTypesBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible title publishers (not providers nor vendors!) for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleProviders() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleProvidersByPackage(genericOIDService.resolveOID(params.obj), params.query)
                break
            case 'status': result.results = controlledListService.getAllPossibleProvidersByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleProvidersBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Call to retrieve all possible coverage depths for the given context
     */
    @Secured(['ROLE_USER'])
    def getAllPossibleCoverageDepths() {
        Map result = [:]
        switch(params.by) {
            case 'pkg': result.results = controlledListService.getAllPossibleCoverageDepthsByPackage(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
            case 'status': result.results = controlledListService.getAllPossibleCoverageDepthsByStatus(params)
                break
            case 'sub': result.results = controlledListService.getAllPossibleCoverageDepthsBySub(genericOIDService.resolveOID(params.obj), params.query, params.forTitles)
                break
        }
        render result as JSON
    }

    /**
     * Retrieves the selected organisation for the organisation merge table
     */
    @Secured(['ROLE_USER'])
    def loadProviderForMerge() {
        Map<String, Object> mergeInfo = [:]
        if(params.containsKey('source') && params.source.length() > 0) {
            mergeInfo = providerService.mergeProviders(genericOIDService.resolveOID(params.source), null, true)
        }
        else if(params.containsKey('target') && params.target.length() > 0) {
            mergeInfo = providerService.mergeProviders(genericOIDService.resolveOID(params.target), null, true)
        }
        render mergeInfo as JSON
    }

    /**
     * Retrieves the selected organisation for the organisation merge table
     */
    @Secured(['ROLE_USER'])
    def loadVendorForMerge() {
        Map<String, Object> mergeInfo = [:]
        if(params.containsKey('source') && params.source.length() > 0) {
            mergeInfo = vendorService.mergeVendors(genericOIDService.resolveOID(params.source), null, true)
        }
        else if(params.containsKey('target') && params.target.length() > 0) {
            mergeInfo = vendorService.mergeVendors(genericOIDService.resolveOID(params.target), null, true)
        }
        render mergeInfo as JSON
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

    /**
     * Removes the given object without reloading the page calling
     * @return the success flag as JSON map
     */
    @Secured(['ROLE_USER'])
    def removeObject() {
        int removed = 0
        Map<String, Object> objId = [id: params.long("objId")]
        switch(params.object) {
            case "altname": AlternativeName delAltName = AlternativeName.get(params.objId)
                removed = AlternativeName.executeUpdate('delete from AlternativeName altname where altname.id = :id', objId)
                delAltName.afterDelete()
                break
            case "frontend": removed = DiscoverySystemFrontend.executeUpdate('delete from DiscoverySystemFrontend dsf where dsf.id = :id', objId)
                break
            case "index": removed = DiscoverySystemIndex.executeUpdate('delete from DiscoverySystemIndex dsi where dsi.id = :id', objId)
                break
            case "coverage": IssueEntitlementCoverage ieCoverage = IssueEntitlementCoverage.get(params.objId)
                if(ieCoverage) {
                    PendingChange.executeUpdate('delete from PendingChange pc where pc.oid = :oid',[oid:"${ieCoverage.class.name}:${ieCoverage.id}"])
                    removed = IssueEntitlementCoverage.executeUpdate('delete from IssueEntitlementCoverage ic where ic.id = :id', objId)
                }
                break
            //ex SubscriptionControllerService.removePriceItem()
            case "priceItem": removed = PriceItem.executeUpdate('delete from PriceItem pi where pi.id = :id', objId)
                break
        }
        Boolean success = removed > 0
        Map<String, Boolean> result = [success: success]
        render result as JSON
    }

    /**
     * Call to add a new price item to the issue entitlement
     * @return the issue entitlement holding view
     */
    /*
    @Secured(['ROLE_USER'])
    def addEmptyPriceItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEmptyPriceItem(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'index', id: params.id
    }
    */

    /**
     * Call to remove a price item from the issue entitlement
     * @return the issue entitlement holding view
     */
    /*
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def removePriceItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removePriceItem(params)
        Object[] args = [message(code:'tipp.price'), params.priceItem]
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: 'default.not.found.message', args: args) as String
        }
        else
        {
            flash.message = message(code:'default.deleted.message', args: args) as String
        }
        redirect action: 'index', id: params.id
    }
    */

    /**
     * Searches for property definition alternatives based on the OID of the property definition which should be replaced
     * @return a {@link List} of {@link Map}s of structure [value: result oid, text: translated property definition name, isPrivate: does the property definition have a tenant?]
     * @see PropertyDefinition
     */
    @Secured(['ROLE_USER'])
    def searchPropertyAlternativesByOID() {
        List<Map<String, Object>> result = []
        PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.oid)
        List<PropertyDefinition> queryResult
        if(pd.refdataCategory) {
            queryResult = PropertyDefinition.executeQuery('select pd from PropertyDefinition pd where pd.descr = :descr and pd.refdataCategory = :refdataCategory and pd.type = :type and pd.multipleOccurrence = :multiple and (pd.tenant = :tenant or pd.tenant is null)',
                    [descr   : pd.descr,
                     refdataCategory: pd.refdataCategory,
                     type    : pd.type,
                     multiple: pd.multipleOccurrence,
                     tenant  : contextService.getOrg()])
        }
        else {
            queryResult = PropertyDefinition.executeQuery('select pd from PropertyDefinition pd where pd.descr = :descr and pd.type = :type and pd.multipleOccurrence = :multiple and (pd.tenant = :tenant or pd.tenant is null)',
                    [descr   : pd.descr,
                     type    : pd.type,
                     multiple: pd.multipleOccurrence,
                     tenant  : contextService.getOrg()])
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
    @DebugInfo(isInstAdm_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
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
        Map result = [:]

        if (params.orgIdList || params.providerIdList || params.vendorIdList) {

            String query
            Map<String, Object> queryParams
            if(params.containsKey('providerIdList')) {
                List<Long> providerIds = Params.getLongList_forCommaSeparatedString(params, 'providerIdList')
                List<Provider> providerList = providerIds ? Provider.findAllByIdInList(providerIds) : []
                query = "select distinct p from Person as p inner join p.roleLinks pr where pr.provider in (:providers) "
                queryParams = [providers: providerList]
            }
            else if(params.containsKey('vendorIdList')) {
                List<Long> vendorIds = Params.getLongList_forCommaSeparatedString(params, 'vendorIdList')
                List<Vendor> vendorList = vendorIds ? Vendor.findAllByIdInList(vendorIds) : []
                query = "select distinct p from Person as p inner join p.roleLinks pr where pr.vendor in (:vendors) "
                queryParams = [vendors: vendorList]
            }
            else {
                List<Long> orgIds = Params.getLongList_forCommaSeparatedString(params, 'orgIdList')
                List<Org> orgList = orgIds ? Org.findAllByIdInList(orgIds) : []
                query = "select distinct p from Person as p inner join p.roleLinks pr where pr.org in (:orgs) "
                queryParams = [orgs: orgList]
            }


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
                List<Long> selectedRoleTypIds = Params.getLongList_forCommaSeparatedString(params, 'selectedRoleTypIds')
                List<RefdataValue> selectedRoleTypes = selectedRoleTypIds ? RefdataValue.findAllByIdInList(selectedRoleTypIds) : []

                if (selectedRoleTypes) {
                    query += "and (pr.functionType in (:selectedRoleTypes) or pr.positionType in (:selectedRoleTypes)) "
                    queryParams << [selectedRoleTypes: selectedRoleTypes]
                }
            }

            List<Person> persons = Person.executeQuery(query, queryParams)
            persons.each { Person p ->
                Contact mail = Contact.findByPrsAndContentType(p, RDStore.CCT_EMAIL)
                String key
                //prefix "org" is correct for selector in template
                if(p.roleLinks[0].provider)
                    key = "org${p.roleLinks.provider.id[0]}"
                else if(p.roleLinks[0].vendor)
                    key = "org${p.roleLinks.vendor.id[0]}"
                else
                    key = "org${p.roleLinks.org.id[0]}"
                if(mail) {
                    Set<String> mails = result.get(key)
                    if(!mails)
                        mails = []
                    mails << mail.content
                    result.put(key, mails)
                }
            }
        }

        render result as JSON
    }

    // ----- reporting -----

//    /**
//     * Checks the current state of the reporting cache
//     * @return a {@link Map} reflecting the existence, the filter cache, query cache and details cache states
//     */
//    @Deprecated
//    @Secured(['ROLE_USER'])
//    def checkReportingCache() {
//
//        Map<String, Object> result = [
//            exists: false
//        ]
//
//        if (params.context in [ BaseConfig.KEY_MYINST, BaseConfig.KEY_SUBSCRIPTION ]) {
//            ReportingCache rCache
//
//            if (params.token) {
//                rCache = new ReportingCache( params.context, params.token )
//                result.token = params.token
//            }
//            else {
//                rCache = new ReportingCache( params.context )
//            }
//
//            result.exists       = rCache.exists()
//            result.filterCache  = rCache.get().filterCache ? true : false
//            result.queryCache   = rCache.get().queryCache ? true : false
//            result.detailsCache = rCache.get().detailsCache ? true : false
//        }
//
//        render result as JSON
//    }

    /**
     * Outputs a chart from the given report parameters
     * @return the template to output and the one of the results {@link de.laser.ReportingGlobalService#doChart(java.util.Map, grails.web.servlet.mvc.GrailsParameterMap)} or {@link de.laser.ReportingLocalService#doChart(java.util.Map, grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
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

    @Secured(['ROLE_USER'])
    def checkCounterAPIConnection() {
        Map<String, Object> result = subscriptionService.checkCounterAPIConnection(params.platform, params.customerId, params.requestorId)
        render result as JSON
    }
}