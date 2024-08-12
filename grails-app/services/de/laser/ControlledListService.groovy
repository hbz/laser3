package de.laser


import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.Invoice
import de.laser.finance.Order
import de.laser.helper.Params
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.storage.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

/**
 * This service is a centralised container for dropdown list filling queries.
 * It is used by views and AJAX queries where dropdown entries may be filtered
 */
@Transactional
class ControlledListService {

    ContextService contextService
    GenericOIDService genericOIDService
    MessageSource messageSource

    /**
     * Retrieves a list of organisations
     * @param params eventual request params
     * @return a map containing a sorted list of organisations, an empty one if no organisations match the filter
     */
    Map getOrgs(GrailsParameterMap params) {
        LinkedHashMap result = [results:[]]
        Org org = genericOIDService.resolveOID(params.ctx)
        String queryString = 'select o from Org o where o.status != :deleted and o != :context'
        LinkedHashMap filter = [deleted: RDStore.ORG_STATUS_DELETED, context: org]
        if (params.query && params.query.length() > 0) {
            queryString += " and (genfunc_filter_matcher(o.name, :query) = true or genfunc_filter_matcher(o.sortname, :query) = true) "
            filter.put('query', params.query)
        }
        Set<Org> orgs = Org.executeQuery(queryString+" order by o.name asc",filter)
        orgs.each { Org o ->
            result.results.add([name:o.name,value:o.id])
        }
        result
    }

    /**
     * Retrieves a list of subscriptions owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of subscriptions, an empty one if no subscriptions match the filter
     */
    Map getSubscriptions(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, org.sortname from Subscription s join s.orgRelations orgRoles join orgRoles.org org left join s.propertySet sp where org = :org and orgRoles.roleType in ( :orgRoles )'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN,RDStore.OR_SUBSCRIPTION_CONSORTIUM]]
        //may be generalised later - here it is where to expand the query filter
        if (params.query && params.query.length() > 0) {
            queryString += " and (genfunc_filter_matcher(s.name, :query) = true or genfunc_filter_matcher(orgRoles.org.sortname, :query) = true) "
            filter.put('query', params.query)
        }
        def ctx = null
        if(params.ctx && params.ctx.contains(Subscription.class.name)) {
            ctx = genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            queryString += " and s != :ctx "
        }
        else if(params.ctx && params.ctx.contains(License.class.name))
            ctx = genericOIDService.resolveOID(params.ctx)
        switch(ctx?._getCalculatedType()) {
            case [ CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE ]:
                queryString += " and s.instanceOf = null "
                break
            case CalculatedType.TYPE_PARTICIPATION:
                Org subscriber = ctx.getSubscriberRespConsortia()
                queryString += " and s.instanceOf != null and exists (select os from OrgRole os where os.sub = s and os.roleType in (:subscriberCons) and os.org = :subscriber) "
                filter.subscriberCons = [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
                filter.subscriber = subscriber
                break
        }
        if(params.restrictLevel) {
            if (org.isCustomerType_Consortium() && !params.member) {
                queryString += " and s.instanceOf = null "
            }
        }
        if(params.status) {
            if (params.status instanceof List || params.status.contains(',')){
                if (params.status.contains(','))
                    params.status = params.status.split(',')
                if (params.status.size() > 0) {
                    queryString += " and s.status in (:status) "
                    if (params.status instanceof List<RefdataValue>){
                        filter.status = params.status
                    } else {
                        List statusList = []
                        params.status.each{
                            statusList += RefdataValue.get(it)
                        }
                        filter.status = statusList
                    }
                }
            } else {
                if(params.status != 'FETCH_ALL') { //FETCH_ALL may be sent from finances/_filter.gsp and _consortiaSubscriptionFilter.gsp
                    if(params.status instanceof RefdataValue)
                        filter.status = params.status
                    else filter.status = RefdataValue.get(params.status)
                    queryString += " and s.status = :status "
                }
            }
        }
        else {
            filter.status = RDStore.SUBSCRIPTION_CURRENT
            queryString += " and s.status = :status "
        }
        if(params.propDef) {
            PropertyDefinition filterPropDef = (PropertyDefinition) genericOIDService.resolveOID(params.propDef)
            queryString += " and sp.type = :propDef "
            filter.propDef = filterPropDef
            if(params.propVal) {
                Set<String> propValInput = []
                Set filterPropVal = []
                if(params.propVal.contains(',')) {
                    propValInput.addAll(params.propVal.split(','))
                }
                else propValInput << params.propVal
                boolean dateFlag = false, refFlag = false, urlFlag = false
                switch(filterPropDef.getImplClassValueProperty()) {
                    case 'intValue': queryString += " and sp.intValue in (:values)"
                        break
                    case 'decValue': queryString += " and sp.decValue in (:values)"
                        break
                    case 'stringValue': queryString += " and sp.stringValue in (:values)"
                        break
                    case 'dateValue': queryString += " and sp.dateValue in (:values)"
                        dateFlag = true
                        break
                    case 'urlValue': queryString += " and sp.urlValue in (:values)"
                        urlFlag = true
                        break
                    case 'refValue': queryString += " and sp.refValue in (:values)"
                        refFlag = true
                        break
                }
                propValInput.each { String val ->
                    if(dateFlag) {
                        filterPropVal << DateUtils.getLocalizedSDF_noTime().parse(val)
                    }
                    else if(refFlag) {
                        if(val.contains("de.laser."))
                            filterPropVal << genericOIDService.resolveOID(val)
                        else
                            filterPropVal << RefdataValue.getByValueAndCategory(val,filterPropDef.refdataCategory)
                    }
                    else if(urlFlag) {
                        filterPropVal << new URL(val)
                    }
                    else {
                        filterPropVal << val
                    }
                }
                filter.values = filterPropVal
            }
        }
        if(params.providerFilter) {
            queryString += " and exists (select pvr from ProviderRole pvr where pvr.subscription = s and pvr.provider = :filterProvider) "
            filter.filterProvider = genericOIDService.resolveOID(params.providerFilter)
        }
        //weird naming ... Fomantic UI API does it so
        else if(params.'providerFilter[]') {
            queryString += " and exists (select pvr from ProviderRole pvr where pvr.subscription = s and pvr.provider in (:filterProvider)) "
            filter.filterProvider = params.list('providerFilter[]').collect { String key -> genericOIDService.resolveOID(key) }
        }
        if(params.vendorFilter) {
            queryString += " and exists (select vr from VendorRole vr where vr.subscription = s and vr.vendor = :filterVendor) "
            filter.filterVendor = genericOIDService.resolveOID(params.providerVendor)
        }
        else if(params.'vendorFilter[]') {
            queryString += " and exists (select vr from VendorRole vr where vr.subscription = s and vr.vendor in (:filterVendor)) "
            filter.filterVendor = params.list('vendorFilter[]').collect { String key -> genericOIDService.resolveOID(key) }
        }
        Set<String> refdataFields = ['form','resource','kind']
        refdataFields.each { String refdataField ->
            if(params[refdataField]) {
                if (params[refdataField].contains(',')) {
                    List refList = []
                    params[refdataField].split(',').each { String val ->
                        refList << RefdataValue.get(val)
                    }
                    filter[refdataField] = refList
                    queryString += " and s.${refdataField} in (:${refdataField}) "
                } else {
                    filter[refdataField] = RefdataValue.get(params[refdataField])
                    queryString += " and s.${refdataField} = :${refdataField} "
                }
            }
        }
        //println(queryString)
        List subscriptions = Subscription.executeQuery(queryString+" order by s.name asc, s.startDate asc, s.endDate asc, org.sortname asc",filter)

        subscriptions.each { row ->
            Subscription s = (Subscription) row[0]

            switch (params.ltype) {
                case CalculatedType.TYPE_PARTICIPATION:
                    if (s._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]){
                        if(org.id == s.getConsortium().id)
                            result.results.add([name:s.dropdownNamingConvention(org), value:genericOIDService.getOID(s)])
                    }
                    break
                case CalculatedType.TYPE_CONSORTIAL:
                    if (s._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
                        result.results.add([name:s.dropdownNamingConvention(org), value:genericOIDService.getOID(s)])
                    break
                default:
                    if(!params.nameOnly)
                        result.results.add([name:s.dropdownNamingConvention(org), value:genericOIDService.getOID(s)])
                    else result.results.add([name:s.name,value:genericOIDService.getOID(s)])
                    break
            }
        }
        result
    }

    /**
     * Retrieves a list of issue entitlements owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getIssueEntitlements(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlements = [results:[]]
        //build up set of subscriptions which are owned by the current institution or instances of such - or filter for a given subscription
        String filter = 'in (select distinct o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) and o.sub.status = :current ) '
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
        if(params.sub) {
            filter = '= :sub'
            filterParams = ['sub':genericOIDService.resolveOID(params.sub)]
        }
        if(params.pkg) {
            try {
                def pkgObj = genericOIDService.resolveOID(params.pkg)
                if(pkgObj && pkgObj instanceof SubscriptionPackage) {
                    SubscriptionPackage pkg = (SubscriptionPackage) pkgObj
                    filter += ' and ie.tipp.pkg.gokbId = :pkg'
                    filterParams.pkg = pkg.pkg.gokbId
                }
            }
            catch (Exception e) {
                return [results:[]]
            }
        }
        if(params.query && params.query.length() > 0) {
            filter += ' and genfunc_filter_matcher(ie.tipp.name,:query) = true '
            filterParams.put('query',params.query)
        }
        List result = IssueEntitlement.executeQuery('select ie from IssueEntitlement as ie where ie.subscription '+filter+' and ie.status != :removed order by ie.tipp.sortname asc, ie.subscription asc, ie.subscription.startDate asc, ie.subscription.endDate asc',filterParams+[removed: RDStore.TIPP_STATUS_REMOVED])
        if(result.size() > 0) {
            result.each { res ->
                Subscription s = (Subscription) res.subscription

                issueEntitlements.results.add([name:"${res.tipp.name} (${res.tipp.titleType}) (${s.dropdownNamingConvention(org)})",value:genericOIDService.getOID(res)])
            }
        }
        issueEntitlements
    }

    /**
     * Retrieves a list of issue entitlement groups owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a list of issue entitlement groups, an empty one if no issue entitlement group match the filter
     */
    Map getTitleGroups(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlementGroup = [results:[]]
        //build up set of subscriptions which are owned by the current institution or instances of such - or filter for a given subscription
        String filter = 'in (select distinct o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) and o.sub.status = :current ) '
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
        if(params.sub) {
            filter = '= :sub'
            filterParams = ['sub':genericOIDService.resolveOID(params.sub)]
        }

        if(params.query && params.query.length() > 0) {
            filter += ' and genfunc_filter_matcher(ieg.name,:query) = true '
            filterParams.put('query',params.query)
        }
        List result = IssueEntitlementGroup.executeQuery('select ieg from IssueEntitlementGroup as ieg where ieg.sub '+filter+' order by ieg.name asc, ieg.sub asc, ieg.sub.startDate asc, ieg.sub.endDate asc',filterParams)
        if(result.size() > 0) {
            result.each { res ->
                Subscription s = (Subscription) res.sub
                issueEntitlementGroup.results.add([name:"${res.name} (${s.dropdownNamingConvention(org)})",value:genericOIDService.getOID(res)])
            }
        }
        issueEntitlementGroup
    }

    /**
     * Retrieves a list of licenses owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing licenses, an empty one if no licenses match the filter
     */
    Map getLicenses(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap licenses = [results:[]]
        List<License> result = []
        String licFilter = ''
        LinkedHashMap filterParams = [org:org,orgRoles:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE]]
        if (params.query && params.query.length() > 0) {
            licFilter = ' and genfunc_filter_matcher(l.reference, :query) = true '
            filterParams.put('query', params.query)
        }
        def ctx = null
        if(params.ctx && params.ctx.contains(License.class.name)) {
            ctx = genericOIDService.resolveOID(params.ctx)
            filterParams.ctx = ctx
            licFilter += " and l != :ctx "
        }
        else if(params.ctx && params.ctx.contains(Subscription.class.name)) {
            ctx = genericOIDService.resolveOID(params.ctx)
        }
        switch(ctx?._getCalculatedType()) {
            case [ CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE ]:
                licFilter += " and l.instanceOf = null "
                break
            case CalculatedType.TYPE_PARTICIPATION:
                licFilter += " and l.instanceOf != null "
                break
        }
        if(params.providerFilter) {
            licFilter += " and exists (select ol from OrgRole ol where ol.lic = l and ol.roleType = :providerRoleType and ol.org = :filterProvider) "
            filterParams.providerRoleType = RDStore.OR_LICENSOR
            filterParams.filterProvider = genericOIDService.resolveOID(params.providerFilter)
        }
        result = License.executeQuery('select l from License as l join l.orgRelations ol where ol.org = :org and ol.roleType in (:orgRoles)'+licFilter+" order by l.reference asc",filterParams)
        if(result.size() > 0) {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            log.debug("licenses found")
            result.each { res ->
                licenses.results += ([name:"${res.reference} (${res.startDate ? sdf.format(res.startDate) : '???'} - ${res.endDate ? sdf.format(res.endDate) : ''})",value:genericOIDService.getOID(res)])
            }
        }
        licenses
    }

    /**
     * Retrieves a list of issue entitlements owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getSubscriptionPackages(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, orgRoles.org.sortname from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles )'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        //may be generalised later - here it is where to expand the query filter
        if(params.query && params.query.length() > 0) {
            filter.put('query', params.query)
            queryString += " and (genfunc_filter_matcher(s.name,:query) = true or genfunc_filter_matcher(orgRoles.org.sortname,:query) = true) "
        }
        if(params.ctx) {
            Subscription ctx = (Subscription) genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            if (org.isCustomerType_Consortium())
                queryString += " and (s = :ctx or s.instanceOf = :ctx)"
            else
                queryString += " and s = :ctx"
        }
        else if(params.sub) {
            filter.sub = genericOIDService.resolveOID(params.sub)
            queryString += " and s = :sub"
        }
        if(params.status) {
            if(params.status != 'FETCH_ALL') { //FETCH_ALL may be sent from finances/_filter.gsp
                if(params.status instanceof RefdataValue)
                    filter.status = params.status
                else filter.status = RefdataValue.get(params.status)
                queryString += " and s.status = :status "
            }
        }
        else if(filter.ctx) {
            filter.status = filter.ctx.status
            queryString += " and s.status = :status "
        }
        else if(filter.sub) {
            filter.status = filter.sub.status
            queryString += " and s.status = :status "
        }
        else {
            filter.status = RDStore.SUBSCRIPTION_CURRENT
            queryString += " and s.status = :status "
        }
        List subscriptions = Subscription.executeQuery(queryString+" order by s.name asc, orgRoles.org.sortname asc, s.startDate asc, s.endDate asc",filter)
        subscriptions.each { row ->
            Subscription s = (Subscription) row[0]
            s.packages.each { sp ->
                result.results.add([name:"${sp.pkg.name}/${s.dropdownNamingConvention(org)}",value:genericOIDService.getOID(sp.pkg)])
            }
        }
        result
    }

    /**
     * Retrieves a list of budget codes owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of budget codes, an empty one if no budget codes match the filter
     */
    Map getBudgetCodes(GrailsParameterMap params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select bc from BudgetCode bc where bc.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put('query', params.query)
            queryString += " and genfunc_filter_matcher(bc.value,:query) = true"
        }
        queryString += " order by bc.value asc"
        List budgetCodes = BudgetCode.executeQuery(queryString,filter)
        budgetCodes.each { BudgetCode bc ->
            result.results.add([name:bc.value,value:bc.id])
        }
        result
    }

    /**
     * Retrieves a list of invoice numbers owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of invoice numbers, an empty one if no invoice numbers match the filter
     */
    Map getInvoiceNumbers(GrailsParameterMap params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select distinct(i.invoiceNumber) from Invoice i where i.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put('query', params.query)
            queryString += " and genfunc_filter_matcher(i.invoiceNumber,:query) = true"
        }
        queryString += " order by i.invoiceNumber asc"
        List invoiceNumbers = Invoice.executeQuery(queryString,filter)
        invoiceNumbers.each { String invoiceNumber ->
            result.results.add([name:invoiceNumber,value:invoiceNumber])
        }
        result
    }

    /**
     * Retrieves a list of invoice numbers owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of invoice numbers, an empty one if no invoice numbers match the filter
     */
    Map getOrderNumbers(GrailsParameterMap params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select distinct(ord.orderNumber) from Order ord where ord.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put('query', params.query)
            //queryString += " and ord.orderNumber like :query"
            queryString += " and genfunc_filter_matcher(ord.orderNumber,:query) = true"
        }
        queryString += " order by ord.orderNumber asc"
        List orderNumbers = Order.executeQuery(queryString,filter)
        orderNumbers.each { String orderNumber ->
            result.results.add([name:orderNumber,value:orderNumber])
        }
        result
    }

    /**
     * Retrieves a list of references owned by the context institution matching given parameters
     * @param params eventual request params
     * @return a map containing a sorted list of references, an empty one if no references match the filter
     */
    Map getReferences(GrailsParameterMap params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select distinct(ci.reference) from CostItem ci where ci.owner = :owner and ci.reference != null'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put('query', params.query)
            queryString += " and genfunc_filter_matcher(ci.reference,:query) = true"
        }
        queryString += " order by ci.reference asc"
        List references = CostItem.executeQuery(queryString,filter)
        references.each { String r ->
            result.results.add([name:r,value:r])
        }
        result
    }

    /**
     * Moved from {@link de.laser.ctrl.OrganisationControllerService#getResultGenericsAndCheckAccess(de.laser.OrganisationController, grails.web.servlet.mvc.GrailsParameterMap)}
     * Retrieves a list of organisations, too, just like {@link #getOrgs(grails.web.servlet.mvc.GrailsParameterMap)} does, but used is the context institution, the list is moreover not
     * filterable and retrieved are the instititutions linked by combo to the given institution or providers and agencies
     * @return a map containing a sorted list of organisations, an empty one if no results are being obtained
     * @see Combo
     */
    List getOrgs() {
        Org org = contextService.getOrg()
        List<Map<String,Object>> result = []
        //to translate in hql: select org_name from org left join combo on org_id = combo_from_org_fk where combo_to_org_fk = 1 or org_sector_rv_fk = 82 order by org_sortname asc, org_name asc;
        List orgs = Org.executeQuery("select new map(o.id as id,o.name as name,o.sortname as sortname) from Combo c right join c.fromOrg o where (o.status = null or o.status != :deleted) and (c.toOrg = :contextOrg) order by o.sortname asc, o.name asc",[contextOrg:org, deleted:RDStore.O_STATUS_DELETED])
        orgs.each { row ->
            if(row.id != org.id) {
                String text = row.sortname ? "${row.sortname} (${row.name})" : "${row.name}"
                result.add([id:row.id,text:text])
            }
        }
        result
    }

    /**
     * This unused method was developed for a generic document assignal. It retrieves the selected kinds of objects
     * to which the context institution has reading access
     * @param params eventual request params
     * @return a map containing a sorted list of objects, sorted by their name; an empty list if no objects match the filter
     */
    Map getElements(GrailsParameterMap params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        if(params.org == "true") {
            List allOrgs = DocContext.executeQuery('select distinct dc.org,dc.org.sortname from DocContext dc where dc.owner.owner = :ctxOrg and dc.org != null and (genfunc_filter_matcher(dc.org.name,:query) = true or genfunc_filter_matcher(dc.org.sortname,:query) = true) order by dc.org.sortname asc',[ctxOrg:org,query:params.query])
            allOrgs.each { DocContext it ->
                result.results.add([name:"(${messageSource.getMessage('spotlight.organisation',null, locale)}) ${it[0].name}",value:genericOIDService.getOID(it[0])])
            }
        }
        if(params.license == "true") {
            List allLicenses = DocContext.executeQuery('select distinct dc.license,dc.license.reference from DocContext dc where dc.owner.owner = :ctxOrg and dc.license != null and genfunc_filter_matcher(dc.license.reference,:query) = true order by dc.license.reference asc',[ctxOrg:org,query:params.query])
            allLicenses.each { DocContext it ->
                License license = (License) it[0]
                String licenseStartDate = license.startDate ? sdf.format(license.startDate) : '???'
                String licenseEndDate = license.endDate ? sdf.format(license.endDate) : ''
                result.results.add([name:"(${messageSource.getMessage('spotlight.license',null, locale)}) ${it[1]} - (${licenseStartDate} - ${licenseEndDate})",value:genericOIDService.getOID(license)])
            }
        }
        if(params.subscription == "true") {
            List allSubscriptions = DocContext.executeQuery('select distinct dc.subscription,dc.subscription.name from DocContext dc where dc.owner.owner = :ctxOrg and dc.subscription != null and genfunc_filter_matcher(dc.subscription.name,:query) = true order by dc.subscription.name asc',[ctxOrg:org,query:params.query])
            allSubscriptions.each { DocContext it ->
                Subscription subscription = (Subscription) it[0]
                /*
                String tenant
                if(subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && subscription.getConsortium().id == org.id) {
                    try {
                        tenant = " - ${subscription.getAllSubscribers().get(0).sortname}"
                    }
                    catch (IndexOutOfBoundsException e) {
                        log.debug("Please check subscription #${subscription.id}")
                    }
                }
                else {
                    tenant = ''
                }
                String dateString = "("
                if (subscription.startDate)
                    dateString += sdf.format(subscription.startDate) + " - "
                else dateString += "???"
                if (subscription.endDate)
                    dateString += sdf.format(subscription.endDate)
                else dateString += ""
                dateString += ")"
                */
                result.results.add([name:"(${messageSource.getMessage('default.subscription.label',null, locale)}) ${subscription.dropdownNamingConvention()}",value:genericOIDService.getOID(it[0])])
            }
        }
        result
    }

    /**
     * Called from title filter views
     * Retrieves all possible title types for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @param query a query filter to restrict on certain title types
     * @param forTitles the title status considered
     * @return a set of possible title types
     */
    Set<String> getAllPossibleTitleTypesByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> titleTypes = []
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        String nameFilter = ""
        if (query) {
            nameFilter += " and genfunc_filter_matcher(titleType, :query) = true "
            queryParams.query = query
        }

        titleTypes = TitleInstancePackagePlatform.executeQuery("select new map(titleType as name, titleType as value) from TitleInstancePackagePlatform where titleType is not null and pkg = :pkg and status = :status "+nameFilter+" group by titleType", queryParams)

//        if (titleTypes.size() == 0){
//            titleTypes << [name: messageSource.getMessage('titleInstance.noTitleType.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }
        titleTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible title types for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query a query filter to restrict on certain title types
     * @param forTitles the title tab view
     * @return a set of possible title types
     */
    Set<String> getAllPossibleTitleTypesBySub(Subscription subscription, String query, String forTitles) {
        Set<String> titleTypes = []
        String nameFilter = "", statusFilter = " and status = :status "
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(titleType, :query) = true "
            queryParams.query = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            titleTypes = TitleInstancePackagePlatform.executeQuery("select new map(titleType as name, titleType as value) from TitleInstancePackagePlatform where titleType is not null and pkg in (:pkg) "+statusFilter+nameFilter+" group by titleType", queryParams)
        }
//        if (titleTypes.size() == 0){
//            titleTypes << [name: messageSource.getMessage('titleInstance.noTitleType.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }
        titleTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible title types for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible title types
     */
    Set<String> getAllPossibleTitleTypesByStatus(GrailsParameterMap params) {
        Set<String> titleTypes = []

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(titleType as name, titleType as value) from TitleInstancePackagePlatform tipp where tipp.titleType is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }
           if (params.query) {
               query += " and genfunc_filter_matcher(titleType, :query) = true "
               queryMap.query = params.query
           }
           query += " group by titleType order by titleType"

           titleTypes = TitleInstancePackagePlatform.executeQuery(query, queryMap)
       }
//        if (titleTypes.size() == 0){
//            titleTypes << [name: messageSource.getMessage('titleInstance.noTitleType.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }
        titleTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible medium types for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @param query a query filter to restrict on certain medium types
     * @param forTitles the title status considered
     * @return a set of possible title types
     */
    Set<String> getAllPossibleMediumTypesByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> mediumTypes = []
        String nameFilter = "", i18n = LocaleUtils.getCurrentLang()
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(tipp.medium.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }

        mediumTypes.addAll(TitleInstancePackagePlatform.executeQuery("select new map(tipp.medium.value_"+i18n+" as name, tipp.medium.id as value) from TitleInstancePackagePlatform tipp where tipp.medium is not null and tipp.pkg = :pkg and tipp.status = :status "+nameFilter+" group by tipp.medium.id, tipp.medium.value_"+i18n+" order by tipp.medium.value_"+i18n, queryParams))

        mediumTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible medium types for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query a query filter to restrict on certain medium types
     * @param forTitles the title tab view
     * @return a set of possible title types
     */
    Set<String> getAllPossibleMediumTypesBySub(Subscription subscription, String query, String forTitles) {
        Set<String> mediumTypes = []
        String nameFilter = "", statusFilter = " and tipp.status = :status ", i18n = LocaleUtils.getCurrentLang()
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(tipp.medium.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }
        if(subscription.packages){
            mediumTypes.addAll(TitleInstancePackagePlatform.executeQuery("select new map(tipp.medium.value_"+i18n+" as name, tipp.medium.id as value) from TitleInstancePackagePlatform tipp where tipp.medium is not null and tipp.pkg in (:pkg) "+statusFilter+nameFilter+" group by tipp.medium.id, tipp.medium.value_"+i18n+" order by tipp.medium.value_"+i18n, queryParams))
        }
        mediumTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible medium types for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible title types
     */
    Set<String> getAllPossibleMediumTypesByStatus(GrailsParameterMap params) {
        Set<String> mediumTypes = []
        String i18n = LocaleUtils.getCurrentLang()

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(tipp.medium.value_"+i18n+" as name, tipp.medium.id as value) from TitleInstancePackagePlatform tipp where tipp.medium is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and genfunc_filter_matcher(tipp.medium.value_" + i18n + ", :query) = true "
               queryMap.query = params.query
           }
           query += " group by tipp.medium.id, tipp.medium.value_"+i18n+" order by tipp.medium.value_"+i18n

            mediumTypes.addAll(TitleInstancePackagePlatform.executeQuery(query, queryMap))
        }
        mediumTypes
    }

    /**
     * Called from title filter views
     * Retrieves all possible coverage depths for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @param query a query filter to restrict on certain coverage depths
     * @param forTitles the title status considered
     * @return a set of possible coverage depths
     */
    Set getAllPossibleCoverageDepthsByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        String nameFilter = "", i18n = LocaleUtils.getCurrentLang()
        Set<Map> coverageDepths = []
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(rdv.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }

        coverageDepths.addAll(RefdataValue.executeQuery("select new map(rdv.value_"+i18n+" as name, rdv.id as value) from RefdataValue rdv where rdv.value in (select tc.coverageDepth from TIPPCoverage tc join tc.tipp tipp where tc.coverageDepth is not null and tipp.pkg = :pkg and tipp.status = :status) "+nameFilter+" group by rdv.id, rdv.value_"+i18n+" order by rdv.value_"+i18n, queryParams))

        coverageDepths
    }

    /**
     * Called from title filter views
     * Retrieves all possible coverage depths for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query a query filter to restrict on certain coverage depths
     * @param forTitles the title tab view
     * @return a set of possible coverage depths
     */
    Set getAllPossibleCoverageDepthsBySub(Subscription subscription, String query, String forTitles) {
        Set<Map> coverageDepths = []
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        String nameFilter = "", statusFilter = " and tipp.status = :status ", i18n = LocaleUtils.getCurrentLang()
        if (query) {
            nameFilter += " and genfunc_filter_matcher(rdv.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            coverageDepths = RefdataValue.executeQuery("select new map(rdv.value_"+i18n+" as name, rdv.id as value) from RefdataValue rdv where rdv.value in (select tc.coverageDepth from TIPPCoverage tc join tc.tipp tipp where tc.coverageDepth is not null and tipp.pkg in (:pkg)) "+statusFilter+nameFilter+" group by rdv.id, rdv.value_"+i18n+" order by rdv.value_"+i18n, queryParams)
        }

        coverageDepths
    }

    /**
     * Called from title filter views
     * Retrieves all possible coverage depths for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible coverage depths
     */
    Set getAllPossibleCoverageDepthsByStatus(GrailsParameterMap params) {
        Set<Map> coverageDepths = []
        String i18n = LocaleUtils.getCurrentLang()

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(rdv.value_"+i18n+" as name, rdv.id as value) from RefdataValue rdv where rdv.value in (select tc.coverageDepth from TIPPCoverage tc join tc.tipp tipp where tc.coverageDepth is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }
           // TODO
           if (params.query) {
               query += " and genfunc_filter_matcher(rdv.value_" + i18n + ", :query) = true "
               queryMap.query = params.query
           }
           query += " ) group by rdv.id, rdv.value_"+i18n+" order by rdv.value_"+i18n

            coverageDepths = RefdataValue.executeQuery(query, queryMap)
        }

        coverageDepths
    }

    /**
     * Called from title filter views
     * Retrieves all possible series for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @param query a query filter to restrict on certain series
     * @param forTitles the title status considered
     * @return a set of possible series
     */
    Set getAllPossibleSeriesByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        Set<Map> seriesName = []
        String nameFilter = ""
        if (query) {
            nameFilter += " and genfunc_filter_matcher(seriesName, :query) = true "
            queryParams.query = query
        }
        seriesName = TitleInstancePackagePlatform.executeQuery("select new map(seriesName as name, seriesName as value) from TitleInstancePackagePlatform where seriesName is not null and pkg = :pkg and status = :status "+nameFilter+" group by seriesName order by seriesName", queryParams)

        if(seriesName.size() == 0){
            seriesName << [name: messageSource.getMessage('titleInstance.noSeriesName.label', null, LocaleUtils.getCurrentLocale()), value: null]
        }
        seriesName
    }

    /**
     * Called from title filter views
     * Retrieves all possible series for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query a query filter to restrict on certain series
     * @param forTitles the title tab view
     * @return a set of possible series
     */
    Set getAllPossibleSeriesBySub(Subscription subscription, String query, String forTitles) {
        Set<Map> seriesName = []

        if(subscription.packages){
            Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
            String nameFilter = "", statusFilter = " and tipp.status = :status "
            if (query) {
                nameFilter += " and genfunc_filter_matcher(seriesName, :query) = true "
                queryParams.query = query
            }
            if(forTitles && forTitles == 'allIEs') {
                statusFilter = " and status != :status "
                queryParams.status = RDStore.TIPP_STATUS_REMOVED
            }
            //fomantic UI dropdown expects maps in structure [name: name, value: value]; a pure set is not being accepted ...
            seriesName = TitleInstancePackagePlatform.executeQuery("select new map(seriesName as name, seriesName as value) from TitleInstancePackagePlatform where seriesName is not null and pkg in (:pkg) "+statusFilter+nameFilter+" group by seriesName order by seriesName", queryParams)
        }
        if(seriesName.size() == 0){
            seriesName << [name: messageSource.getMessage('titleInstance.noSeriesName.label', null, LocaleUtils.getCurrentLocale()), value: null]
        }
        seriesName
    }

    /**
     * Called from title filter views
     * Retrieves all possible series for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible series
     */
    Set getAllPossibleSeriesByStatus(GrailsParameterMap params) {
        Set<Map> seriesName = []

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           //fomantic UI dropdown expects maps in structure [name: name, value: value]; a pure set is not being accepted ...
           String query = "select new map(tipp.seriesName as name, tipp.seriesName as value) from TitleInstancePackagePlatform as tipp where tipp.seriesName is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and genfunc_filter_matcher(tipp.seriesName, :query) = true "
               queryMap.query = params.query
           }

           query += " group by tipp.seriesName order by tipp.seriesName"

           seriesName = TitleInstancePackagePlatform.executeQuery(query, queryMap)
        }
        if(seriesName.size() == 0){
            seriesName << [name: messageSource.getMessage('titleInstance.noSeriesName.label', null, LocaleUtils.getCurrentLocale()), value: null]
        }
        seriesName
    }

    /**
     * Called from title filter views
     * Retrieves all possible Dewey decimal classification entries for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitles the title status considered
     * @return a set of possible Dewey decimal classification entries
     */
    Set getAllPossibleDdcsByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        String nameFilter = "", i18n = LocaleUtils.getCurrentLang()
        Set<Map> ddcs = []
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        if (query) {
            nameFilter += " and (genfunc_filter_matcher(ddc.ddc.value_" + i18n + ", :query) = true or genfunc_filter_matcher(ddc.ddc.value, :query) = true) "
            queryParams.query  = query
        }

        ddcs.addAll(TitleInstancePackagePlatform.executeQuery("select new map(concat(ddc.ddc.value,' - ',ddc.ddc.value_"+i18n+") as name, ddc.ddc.id as value) from DeweyDecimalClassification ddc join ddc.tipp tipp join tipp.pkg pkg where pkg = :pkg and tipp.status = :status "+nameFilter+" group by ddc.ddc.id, ddc.ddc.value, ddc.ddc.value_"+i18n+" order by ddc.ddc.value", queryParams))

        ddcs
    }

    /**
     * Called from title filter views
     * Retrieves all possible Dewey decimal classification entries for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitles the title tab view
     * @return a set of possible Dewey decimal classification entries
     */
    Set getAllPossibleDdcsBySub(Subscription subscription, String query, String forTitles) {
        Set<Map> ddcs = []
        String nameFilter = "", statusFilter = " and tipp.status = :status ", i18n = LocaleUtils.getCurrentLang()
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        if (query) {
            nameFilter += " and (genfunc_filter_matcher(ddc.ddc.value_" + i18n + ", :query) = true or genfunc_filter_matcher(ddc.ddc.value, :query) = true) "
            queryParams.query  = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            ddcs.addAll(DeweyDecimalClassification.executeQuery("select new map(concat(ddc.ddc.value,' - ',ddc.ddc.value_"+i18n+") as name, ddc.ddc.id as value) from DeweyDecimalClassification ddc join ddc.tipp tipp join tipp.pkg pkg where pkg in (:pkg) "+statusFilter+nameFilter+" group by ddc.ddc.id, ddc.ddc.value, ddc.ddc.value_"+i18n+" order by ddc.ddc.value", queryParams))
        }
        ddcs
    }

    /**
     * Called from title filter views
     * Retrieves all possible Dewey decimal classification entries for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible Dewey decimal classification entries
     */
    Set getAllPossibleDdcsByStatus(GrailsParameterMap params) {
        Set<Map> ddcs = []
        String i18n = LocaleUtils.getCurrentLang()

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(concat(ddc.ddc.value,' - ',ddc.ddc.value_"+i18n+") as name, ddc.ddc.id as value) from DeweyDecimalClassification ddc join ddc.tipp tipp where tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and (genfunc_filter_matcher(ddc.ddc.value_" + i18n + ", :query) = true or genfunc_filter_matcher(ddc.ddc.value, :query) = true) "
               queryMap.query  = params.query
           }

           query += "group by ddc.ddc.id, ddc.ddc.value, ddc.ddc.value_"+i18n+" order by ddc.ddc.value"
            ddcs.addAll(DeweyDecimalClassification.executeQuery(query, queryMap))
        }
        ddcs
    }

    /**
     * Called from title filter views
     * Retrieves all possible languages for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @params query query filter to restrict to certain values
     * @param forTitles the title status considered
     * @return a set of possible languages
     */
    Set getAllPossibleLanguagesByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<Map> languages = []
        String nameFilter = "", i18n = LocaleUtils.getCurrentLang()
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(lang.language.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }

        languages.addAll(TitleInstancePackagePlatform.executeQuery("select new map(lang.language.value_"+i18n+" as name, lang.language.id as value) from Language lang join lang.tipp tipp join tipp.pkg pkg where pkg = :pkg and tipp.status = :status "+nameFilter+" group by lang.language.id, lang.language.value_"+i18n+" order by lang.language.value_" + i18n, queryParams))

        languages
    }

    /**
     * Called from title filter views
     * Retrieves all possible language entries for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitle the title tab view
     * @return a set of possible language entries
     */
    Set getAllPossibleLanguagesBySub(Subscription subscription, String query, String forTitles) {
        Set<Map> languages = []
        String nameFilter = "", statusFilter = " and tipp.status = :status ", i18n = LocaleUtils.getCurrentLang()
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        if (query) {
            nameFilter += " and genfunc_filter_matcher(lang.language.value_" + i18n + ", :query) = true "
            queryParams.query = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            languages.addAll(DeweyDecimalClassification.executeQuery("select new map(lang.language.value_"+i18n+" as name, lang.language.id as value) from Language lang join lang.tipp tipp join tipp.pkg pkg where pkg in (:pkg)"+statusFilter+nameFilter+" group by lang.language.id, lang.language.value_"+i18n+" order by lang.language.value_" + i18n, queryParams))
        }
        languages
    }

    /**
     * Called from title filter views
     * Retrieves all possible language entries for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible language entries
     */
    Set getAllPossibleLanguagesByStatus(GrailsParameterMap params) {
        Set<Map> languages = []
        String i18n = LocaleUtils.getCurrentLang()

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(lang.language.value_"+i18n+" as name, lang.language.id as value) from Language lang join lang.tipp tipp where tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and genfunc_filter_matcher(lang.language.value_" + i18n + ", :query) = true "
               queryMap.query = params.query
           }

           query += " group by lang.language.id, lang.language.value_"+i18n+" order by lang.language.value_" + i18n

            languages.addAll(Language.executeQuery(query, queryMap))
        }
        languages
    }

    /**
     * Called from title filter views
     * Retrieves all possible subject references for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @params query query filter to restrict to certain values
     * @param forTitles the title status considered
     * @return a set of subject references
     */
    Set getAllPossibleSubjectsByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        SortedSet<String> subjects = new TreeSet<String>()
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        String nameFilter = ""
        if (query) {
            nameFilter += " and genfunc_filter_matcher(subjectReference, :query) = true "
            queryParams.query = query
        }

        List<String> rawSubjects = TitleInstancePackagePlatform.executeQuery("select distinct(subjectReference) from TitleInstancePackagePlatform where subjectReference is not null and pkg = :pkg and status = :status "+nameFilter+" order by subjectReference", queryParams)

        if(rawSubjects.size() == 0){
//            subjects << messageSource.getMessage('titleInstance.noSubjectReference.label', null, LocaleUtils.getCurrentLocale())
        }
        else {
            rawSubjects.each { String rawSubject ->
                //ERMS-4280 point 3 is void!
                rawSubject.tokenize(',;|').each { String rs ->
                    subjects.add(rs.trim())
                }
                //subjects << rawSubject.trim()
            }
        }

        subjects.collect { String subject -> [name: subject, value: subject] }
    }

    /**
     * Called from title filter views
     * Retrieves all possible subject references for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitles title tab view
     * @return a set of possible subject references
     */
    Set getAllPossibleSubjectsBySub(Subscription subscription, String query, String forTitles) {
        SortedSet<String> subjects = new TreeSet<String>()
        List<String> rawSubjects = []
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        String nameFilter = "", statusFilter = " and status = :status "
        if (query) {
            nameFilter += " and genfunc_filter_matcher(subjectReference, :query) = true "
            queryParams.query = query
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            rawSubjects = TitleInstancePackagePlatform.executeQuery("select distinct(subjectReference) from TitleInstancePackagePlatform where subjectReference is not null and pkg in (:pkg)"+statusFilter+nameFilter+" order by subjectReference", queryParams)
        }
        if(rawSubjects.size() == 0){
//            subjects << messageSource.getMessage('titleInstance.noSubjectReference.label', null, LocaleUtils.getCurrentLocale())
        }
        else {
            rawSubjects.each { String rawSubject ->
                rawSubject.tokenize(',;|').each { String rs ->
                    subjects.add(rs.trim())
                }
                //subjects << rawSubject.trim()
            }
        }

        subjects.collect { String subject -> [name: subject, value: subject] }
    }

    /**
     * Called from title filter views
     * Retrieves all possible subject references for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible subject references
     */
    Set getAllPossibleSubjectsByStatus(GrailsParameterMap params) {
        SortedSet<String> subjects = new TreeSet<String>()
        List<String> rawSubjects = []

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select distinct(tipp.subjectReference) from TitleInstancePackagePlatform tipp where tipp.subjectReference is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and genfunc_filter_matcher(tipp.subjectReference, :query) = true "
               queryMap.query = params.query
           }

           query += " order by tipp.subjectReference"
           rawSubjects = TitleInstancePackagePlatform.executeQuery(query, queryMap)
        }
        if(rawSubjects.size() == 0){
//            return [[name: messageSource.getMessage('titleInstance.noSubjectReference.label', null, LocaleUtils.getCurrentLocale()), value: null]]
        }
        else {
            rawSubjects.each { String rawSubject ->
                rawSubject.tokenize(',;|').each { String rs ->
                    subjects.add(rs.trim())
                }
                //subjects << rawSubject.trim()
            }
        }

        subjects.collect { String subject -> [name: subject, value: subject] }
    }

    /**
     * Called from title filter views
     * Retrieves all possible years of first online publication for the given package and the given title status
     * @param pkg the package whose titles should be inspected
     * @params query query filter to restrict to certain values
     * @param forTitles the title status considered
     * @return a set of years of first online publication
     */
    Set getAllPossibleDateFirstOnlineYearByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<Map> subjects = []
        String nameFilter = ""
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        if (query) {
            nameFilter += " and to_char(Year(dateFirstOnline), '9999') like :query "
            queryParams.query = "%${query}%"
        }

        subjects = TitleInstancePackagePlatform.executeQuery("select new map(Year(dateFirstOnline) as name, Year(dateFirstOnline) as value) from TitleInstancePackagePlatform where dateFirstOnline is not null and pkg = :pkg and status = :status "+nameFilter+" group by YEAR(dateFirstOnline) order by YEAR(dateFirstOnline)", queryParams)

//        if(subjects.size() == 0){
//            subjects << [name: messageSource.getMessage('default.selectionNotPossible.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }

        subjects
    }

    /**
     * Called from title filter views
     * Retrieves all possible years of first online publication for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitles the title tab view
     * @return a set of possible years of first online publication
     */
    Set getAllPossibleDateFirstOnlineYearBySub(Subscription subscription, String query, String forTitles) {
        Set<Map> yearsFirstOnline = []
        String nameFilter = "", statusFilter = " and status = :status "
        Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
        if (query) {
            nameFilter += " and to_char(Year(dateFirstOnline), '9999') like :query "
            queryParams.query = "%${query}%"
        }
        if(forTitles && forTitles == 'allIEs') {
            statusFilter = " and status != :status "
            queryParams.status = RDStore.TIPP_STATUS_REMOVED
        }

        if(subscription.packages){
            yearsFirstOnline = TitleInstancePackagePlatform.executeQuery("select new map(Year(dateFirstOnline) as name, Year(dateFirstOnline) as value) from TitleInstancePackagePlatform where dateFirstOnline is not null and pkg in (:pkg) "+statusFilter+nameFilter+" group by YEAR(dateFirstOnline) order by YEAR(dateFirstOnline)", queryParams)
        }
//        if(yearsFirstOnline.size() == 0){
//            yearsFirstOnline << [name: messageSource.getMessage('default.selectionNotPossible.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }

        yearsFirstOnline
    }

    /**
     * Called from title filter views
     * Retrieves all possible years of first online publication for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible years of first online publication
     */
    Set getAllPossibleDateFirstOnlineYearByStatus(GrailsParameterMap params) {
        Set<Map> yearsFirstOnline = []

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(Year(dateFirstOnline) as name, Year(dateFirstOnline) as value) from TitleInstancePackagePlatform tipp where tipp.dateFirstOnline is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }

           if (params.query) {
               query += " and to_char(Year(dateFirstOnline), '9999') like :query "
               queryMap.query = "%${params.query}%"
           }

           query += " group by YEAR(tipp.dateFirstOnline) order by YEAR(tipp.dateFirstOnline)"
           yearsFirstOnline = TitleInstancePackagePlatform.executeQuery(query, queryMap)
        }
//        if(yearsFirstOnline.size() == 0){
//            yearsFirstOnline << [name: messageSource.getMessage('default.selectionNotPossible.label', null, LocaleUtils.getCurrentLocale()), value: null]
//        }

        yearsFirstOnline
    }

    /**
    * Called from title filter views
    * Retrieves all possible publishers for the given package and the given title status
    * @param pkg the package whose titles should be inspected
    * @params query query filter to restrict to certain values
    * @param forTitles the title status considered
    * @return a set of publishers
    */
    Set<String> getAllPossiblePublisherByPackage(Package pkg, String query, String forTitles) {
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> publishers = []
        Map<String, Object> queryParams = [pkg: pkg, status: tippStatus]
        String nameFilter = ""
        if (query) {
            nameFilter += " and genfunc_filter_matcher(publisherName, :query) = true "
            queryParams.query = query
        }

        //publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(orgRole.org.name) from TitleInstancePackagePlatform tipp left join tipp.orgs orgRole where orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and tipp.pkg = :pkg and tipp.status = :status order by orgRole.org.name", [pkg: pkg, status: tippStatus]))
        publishers.addAll(TitleInstancePackagePlatform.executeQuery("select new map(publisherName as name, publisherName as value) from TitleInstancePackagePlatform where publisherName is not null and pkg = :pkg and status = :status "+nameFilter+" group by publisherName order by publisherName", queryParams))

        publishers
    }

    /**
     * Called from title filter views
     * Retrieves all possible publishers for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @param query query filter to restrict to certain values
     * @param forTitles the title tab view
     * @return a set of possible publishers
     */
    Set<String> getAllPossiblePublisherBySub(Subscription subscription, String query, String forTitles) {
        Set<String> publishers = []

        if(subscription.packages){

            Map<String, Object> queryParams = [pkg: subscription.packages.pkg, status: getTippStatusForRequest(forTitles)]
            String nameFilter = "", statusFilter = " and status = :status "
            if (query) {
                nameFilter += " and genfunc_filter_matcher(publisherName, :query) = true "
                queryParams.query = query
            }
            if(forTitles && forTitles == 'allIEs') {
                statusFilter = " and status != :status "
                queryParams.status = RDStore.TIPP_STATUS_REMOVED
            }
            //publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(orgRole.org.name) from TitleInstancePackagePlatform tipp left join tipp.orgs orgRole where orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and tipp.pkg in (:pkg) order by orgRole.org.name", [pkg: subscription.packages.pkg]))
            publishers.addAll(TitleInstancePackagePlatform.executeQuery("select new map(publisherName as name, publisherName as value) from TitleInstancePackagePlatform where publisherName is not null and pkg in (:pkg) "+statusFilter+nameFilter+" group by publisherName order by publisherName", queryParams))
        }

        publishers
    }

    /**
     * Called from title filter views
     * Retrieves all possible publishers for the given subscription
     * @param subscription the subscription whose titles should be inspected
     * @return a set of possible publishers
     */
    Set<String> getAllPossiblePublisherByStatus(GrailsParameterMap params) {
        Set<String> publishers = []

       if (params.list('status').findAll()) {
           List<Long> statusList = Params.getLongList(params, 'status')
           String query = "select new map(tipp.publisherName as name, tipp.publisherName as value) from TitleInstancePackagePlatform tipp where tipp.publisherName is not null and tipp.status.id in (:status) "
           Map queryMap = [status: statusList]

           if(params.institution && params.filterForPermanentTitle){
               queryMap.inst = Org.get(params.institution)
               query += " and tipp.id in (select pt.tipp.id from PermanentTitle as pt where pt.owner = :inst)"
           }
           if (params.query) {
               query += " and genfunc_filter_matcher(tipp.publisherName, :query) = true "
               queryMap.query = params.query
           }
           query += " group by tipp.publisherName order by tipp.publisherName"
            //publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(orgRole.org.name) from TitleInstancePackagePlatform tipp left join tipp.orgs orgRole where orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and tipp.pkg in (:pkg) order by orgRole.org.name", [pkg: subscription.packages.pkg]))
            publishers.addAll(TitleInstancePackagePlatform.executeQuery(query, queryMap))
        }

        publishers
    }

    /**
     * Gets for the given parameter the title status reference value
     * @param forTitles which kind of titles should be retrieved?
     * @return the reference data value matching to the parameter
     */
    RefdataValue getTippStatusForRequest(String forTitles) {
        switch(forTitles) {
            case ['planned', 'plannedIEs']: RDStore.TIPP_STATUS_EXPECTED
                break
            case ['expired', 'expiredIEs']: RDStore.TIPP_STATUS_RETIRED
                break
            case ['deleted', 'deletedIEs']: RDStore.TIPP_STATUS_DELETED
                break
            default: RDStore.TIPP_STATUS_CURRENT
                break
        }
    }

    /**
     * Retrieves a list of platforms matching the given request parameters
     * @param params the request parameter map
     * @return a map containing platforms, an empty one if no platforms match the filter
     */
    Map getPlatforms(GrailsParameterMap params) {
        Org institution = contextService.getOrg()
        String consortiumFilter = "", platNameFilter = ""
        Map qryParams = [context: institution]
        if(institution.isCustomerType_Consortium())
            consortiumFilter = "and sub.instanceOf is null"
        if (params.query) {
            platNameFilter = " and genfunc_filter_matcher(plat.name, :query) = true "
            qryParams.query = params.query
        }
        String qryString = "select new map(concat('${Platform.class.name}:',plat.id) as value,plat.name as name) from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription in (select sub from OrgRole oo join oo.sub sub where oo.org = :context ${consortiumFilter}) ${platNameFilter} group by plat.id order by plat.name asc"
        [results: Platform.executeQuery(qryString, qryParams)]
    }

    /**
     * Retrieves a list of provider organisations matching the given request parameters
     * @param params the request parameter map
     * @return a map containing providers, an empty one if no providers match the filter
     */
    Map getProviders(GrailsParameterMap params) {
        Org institution = contextService.getOrg()
        String consortiumFilter = "", providerNameFilter = ""
        Set results = []
        Map qryParams = [:]
        if(institution.isCustomerType_Consortium())
            consortiumFilter = "and sub.instanceOf is null"
        if (params.query) {
            providerNameFilter = " (genfunc_filter_matcher(p.name, :query) = true or genfunc_filter_matcher(p.sortname, :query) = true or exists (select alt from p.altnames alt where genfunc_filter_matcher(alt.name, :query) = true)) "
            qryParams.query = params.query
        }
        if(params.forFinanceView) {
            List<Long> subIDs = Subscription.executeQuery('select s.id from CostItem ci join ci.sub s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in (:orgRoles)',[org: institution, orgRoles: [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIPTION_CONSORTIUM]])
            if(subIDs) {
                qryParams.subscriptions = subIDs
                if(providerNameFilter)
                    providerNameFilter = "and ${providerNameFilter}"
                String qryString = "select new map(concat('${Provider.class.name}:',p.id) as value, p.name as name) from ProviderRole pvr join pvr.provider p where pvr.subscription.id in (:subscriptions) ${providerNameFilter} order by p.sortname asc"
                results.addAll(Provider.executeQuery(qryString, qryParams))
            }
        }
        else if(params.tableView) {
            String qryString = "select p from Provider p where ${providerNameFilter} order by p.sortname, p.name"
            results.addAll(Provider.executeQuery(qryString, qryParams))
        }
        else {
            if(params.displayWekbFlag) {
                if(providerNameFilter)
                    providerNameFilter = "where ${providerNameFilter}"
                String qryString = "select new map(concat('${Provider.class.name}:',p.id) as value,case when p.gokbId != null then concat(p.name,' (we:kb)') else p.name end as name) from Provider p ${providerNameFilter} order by p.sortname, p.name"
                results.addAll(Provider.executeQuery(qryString, qryParams))
            }
            else {
                if(providerNameFilter)
                    providerNameFilter = "and ${providerNameFilter}"
                qryParams.context = institution
                String qryString1 = "select new map(concat('${Provider.class.name}:',p.id) as value,p.name as name,p.sortname as sortname) from SubscriptionPackage sp join sp.pkg pkg join pkg.provider p where sp.subscription in (select sub from OrgRole os join os.sub sub where os.org = :context ${consortiumFilter}) ${providerNameFilter} group by p.id order by p.sortname, p.name",
                qryString2 = "select new map(concat('${Provider.class.name}:',p.id) as value,p.name as name,p.sortname as sortname) from Provider p where p.createdBy = :context ${providerNameFilter} order by p.sortname, p.name"
                results.addAll(Provider.executeQuery(qryString1, qryParams))
                results.addAll(Provider.executeQuery(qryString2, qryParams))
                results.sort { Map rowA, Map rowB ->
                    int cmp = rowA.sortname <=> rowB.sortname
                    if(!cmp)
                        rowA.name <=> rowB.name
                }
            }
        }
        [results: results]
    }

    /**
     * Retrieves a list of {@link de.laser.wekb.Vendor}s matching the given request parameters
     * @param params the request parameter map
     * @return a map containing vendors, an empty one if no providers match the filter
     */
    Map getVendors(GrailsParameterMap params) {
        Org institution = contextService.getOrg()
        String consortiumFilter = "", vendorNameFilter = ""
        Map qryParams = [:]
        Set results = []
        /*
        we must consider child instances as well this time ...
        if(institution.isCustomerType_Consortium())
            consortiumFilter = "and sub.instanceOf is null"
        */
        if (params.query) {
            vendorNameFilter = "(genfunc_filter_matcher(vendor.name, :query) = true or genfunc_filter_matcher(vendor.sortname, :query) = true) "
            qryParams.query = params.query
        }
        if(params.forFinanceView) {
            List<Long> subIDs = Subscription.executeQuery('select s.id from CostItem ci join ci.sub s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in (:orgRoles)',[org: institution, orgRoles: [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIPTION_CONSORTIUM]])
            if(subIDs) {
                qryParams.subscriptions = subIDs
                if(vendorNameFilter)
                    vendorNameFilter = "and ${vendorNameFilter}"
                String qryString = "select new map(concat('${Vendor.class.name}:',v.id) as value, v.name as name) from VendorRole vr join vr.vendor v where vr.subscription.id in (:subscriptions) ${vendorNameFilter} order by v.sortname, v.name"
                results.addAll(Vendor.executeQuery(qryString, qryParams))
            }
        }
        else if(params.tableView) {
            String qryString = "select vendor from Vendor vendor where ${vendorNameFilter} order by vendor.sortname, vendor.name"
            results.addAll(Vendor.executeQuery(qryString, qryParams))
        }
        else {
            if(params.displayWekbFlag) {
                if(vendorNameFilter)
                    vendorNameFilter = "where ${vendorNameFilter}"
                String qryString = "select new map(concat('${Vendor.class.name}:',vendor.id) as value,case when vendor.gokbId != null then concat(vendor.name,' (we:kb)') else vendor.name end as name) from Vendor vendor ${vendorNameFilter} order by vendor.sortname, vendor.name"
                results.addAll(Vendor.executeQuery(qryString, qryParams))
            }
            else {
                if(vendorNameFilter)
                    vendorNameFilter = "and ${vendorNameFilter}"
                qryParams.context = institution
                String qryString1 = "select new map(concat('${Vendor.class.name}:',vendor.id) as value,vendor.name as name,vendor.sortname as sortname) from PackageVendor pv join pv.vendor vendor, SubscriptionPackage sp join sp.pkg pkg where sp.pkg = pv.pkg and sp.subscription in (select sub from OrgRole oo join oo.sub sub where oo.org = :context ${consortiumFilter}) ${vendorNameFilter} group by vendor.id order by vendor.sortname asc",
                qryString2 = "select new map(concat('${Vendor.class.name}:',vendor.id) as value,vendor.name as name,vendor.sortname as sortname) from Vendor vendor where vendor.createdBy = :context ${vendorNameFilter} order by vendor.sortname asc"
                results.addAll(Vendor.executeQuery(qryString1, qryParams))
                results.addAll(Vendor.executeQuery(qryString2, qryParams))
                results.sort { Map rowA, Map rowB ->
                    int cmp = rowA.sortname <=> rowB.sortname
                    if(!cmp)
                        rowA.name <=> rowB.name
                }
            }
        }
        [results: results]
    }
}
