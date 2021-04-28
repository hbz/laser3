package de.laser


import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.Invoice
import de.laser.finance.Order
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ControlledListService {

    def contextService
    def genericOIDService
    def messageSource
    def accessService

    /**
     * Retrieves a list of providers and agencies
     * @param params - eventual request params
     * @return a map containing a sorted list of providers, an empty one if no providers match the filter
     */
    Map getProvidersAgencies(Map params) {
        LinkedHashMap result = [results:[]]
        Set<RefdataValue> providerAgency = []
        Org org = contextService.getOrg()
        if(params.orgType) {
            providerAgency << RefdataValue.get(params.orgType)
        }
        else providerAgency.addAll([RDStore.OT_PROVIDER,RDStore.OT_AGENCY,RDStore.OT_LICENSOR])
        if(params.forFinanceView) {
            //PLEASE! Do not assign providers or agencies to administrative subscriptions! That will screw up this query ...
            List subscriptions = Subscription.executeQuery('select s from CostItem ci join ci.sub s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in (:orgRoles)',[org:org,orgRoles:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIPTION_CONSORTIA]])
            if(subscriptions) {
                Map filter = [providerAgency: [RDStore.OR_PROVIDER,RDStore.OR_AGENCY],subscriptions:subscriptions]
                String filterString = " "
                if(params.query && params.query.length() > 0) {
                    filter.put("query",params.query)
                    filterString += " and genfunc_filter_matcher(oo.org.name,:query) = true "
                }
                List providers = Org.executeQuery('select distinct oo.org, oo.org.name from OrgRole oo where oo.sub in (:subscriptions) and oo.roleType in (:providerAgency)'+filterString+'order by oo.org.name asc',filter)
                providers.each { p ->
                    result.results.add([name:p[1],value:genericOIDService.getOID(p[0])])
                }
            }
        }
        else {
            String queryString = 'select o from Org o join o.orgType ot where ot in (:providerTypes)'
            LinkedHashMap filter = [providerTypes:providerAgency]
            if(params.query && params.query.length() > 0) {
                filter.put("query",params.query)
                queryString += " and genfunc_filter_matcher(o.name,:query) = true "
            }
            Set providers = Org.executeQuery(queryString+" order by o.name asc",filter)
            providers.each { p ->
                result.results.add([name:p.name,value:genericOIDService.getOID(p)])
            }
        }
        result
    }

    /**
     * Retrieves a list of subscriptions owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of subscriptions, an empty one if no subscriptions match the filter
     */
    Map getSubscriptions(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, org.sortname from Subscription s join s.orgRelations orgRoles join orgRoles.org org left join s.propertySet sp where org = :org and orgRoles.roleType in ( :orgRoles )'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN,RDStore.OR_SUBSCRIPTION_CONSORTIA]]
        //may be generalised later - here it is where to expand the query filter
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            queryString += " and (genfunc_filter_matcher(s.name,:query) = true or genfunc_filter_matcher(orgRoles.org.sortname,:query) = true) "
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
            case CalculatedType.TYPE_CONSORTIAL:
            case CalculatedType.TYPE_ADMINISTRATIVE:
                queryString += " and s.instanceOf = null "
                break
            case CalculatedType.TYPE_PARTICIPATION:
                queryString += " and s.instanceOf != null "
                break
        }
        if(params.restrictLevel) {
            if(org.hasPerm("ORG_CONSORTIUM") && !params.member) {
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
                        filterPropVal << DateUtils.SDF_NoTime.parse(val)
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
                        if(org.id == s.getConsortia().id)
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
		//log.debug ("getSubscriptions(): ${result.results.size()} Matches")
        result
    }

    /**
     * Retrieves a list of issue entitlements owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getIssueEntitlements(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlements = [results:[]]
        //build up set of subscriptions which are owned by the current organisation or instances of such - or filter for a given subscription
        String filter = 'in (select distinct o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) and o.sub.status = :current ) '
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
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
        List result = IssueEntitlement.executeQuery('select ie from IssueEntitlement as ie where ie.subscription '+filter+' order by ie.tipp.name asc, ie.subscription asc, ie.subscription.startDate asc, ie.subscription.endDate asc',filterParams)
        if(result.size() > 0) {
            result.each { res ->
                Subscription s = (Subscription) res.subscription

                issueEntitlements.results.add([name:"${res.tipp.name} (${res.tipp.titleType}) (${s.dropdownNamingConvention(org)})",value:genericOIDService.getOID(res)])
            }
        }
        issueEntitlements
    }

    /**
     * Retrieves a list of issue entitlement groups owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a list of issue entitlement groups, an empty one if no issue entitlement group match the filter
     */
    Map getTitleGroups(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlementGroup = [results:[]]
        //build up set of subscriptions which are owned by the current organisation or instances of such - or filter for a given subscription
        String filter = 'in (select distinct o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) and o.sub.status = :current ) '
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
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
     * Retrieves a list of licenses owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing licenses, an empty one if no licenses match the filter
     */
    Map getLicenses(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap licenses = [results:[]]
        List<License> result = []
        String licFilter = ''
        LinkedHashMap filterParams = [org:org,orgRoles:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE]]
        if(params.query && params.query.length() > 0) {
            licFilter = ' and genfunc_filter_matcher(l.reference,:query) = true '
            filterParams.put('query',params.query)
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
            case CalculatedType.TYPE_CONSORTIAL:
            case CalculatedType.TYPE_ADMINISTRATIVE:
                licFilter += " and l.instanceOf = null "
                break
            case CalculatedType.TYPE_PARTICIPATION:
                licFilter += " and l.instanceOf != null "
                break
        }
        result = License.executeQuery('select l from License as l join l.orgRelations ol where ol.org = :org and ol.roleType in (:orgRoles)'+licFilter+" order by l.reference asc",filterParams)
        if(result.size() > 0) {
            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            log.debug("licenses found")
            result.each { res ->
                licenses.results += ([name:"${res.reference} (${res.startDate ? sdf.format(res.startDate) : '???'} - ${res.endDate ? sdf.format(res.endDate) : ''})",value:genericOIDService.getOID(res)])
            }
        }
        licenses
    }

    /**
     * Retrieves a list of issue entitlements owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getSubscriptionPackages(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, orgRoles.org.sortname from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles )'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        //may be generalised later - here it is where to expand the query filter
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            queryString += " and (genfunc_filter_matcher(s.name,:query) = true or genfunc_filter_matcher(orgRoles.org.sortname,:query) = true) "
        }
        if(params.ctx) {
            Subscription ctx = (Subscription) genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            if(org.hasPerm("ORG_CONSORTIUM"))
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
        else {
            filter.status = RDStore.SUBSCRIPTION_CURRENT
            queryString += " and s.status = :status "
        }
        List subscriptions = Subscription.executeQuery(queryString+" order by s.name asc, orgRoles.org.sortname asc, s.startDate asc, s.endDate asc",filter)
        subscriptions.each { row ->
            Subscription s = (Subscription) row[0]
            s.packages.each { sp ->
                result.results.add([name:"${sp.pkg.name}/${s.dropdownNamingConvention(org)}",value:genericOIDService.getOID(sp)])
            }
        }
        result
    }

    Map getBudgetCodes(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select bc from BudgetCode bc where bc.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            queryString += " and genfunc_filter_matcher(bc.value,:query) = true"
        }
        queryString += " order by bc.value asc"
        List budgetCodes = BudgetCode.executeQuery(queryString,filter)
        budgetCodes.each { BudgetCode bc ->
            result.results.add([name:bc.value,value:bc.id])
        }
        result
    }

    Map getInvoiceNumbers(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select i from Invoice i where i.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            queryString += " and genfunc_filter_matcher(i.invoiceNumber,:query) = true"
        }
        queryString += " order by i.invoiceNumber asc"
        List invoiceNumbers = Invoice.executeQuery(queryString,filter)
        invoiceNumbers.each { Invoice inv ->
            result.results.add([name:inv.invoiceNumber,value:inv.invoiceNumber])
        }
        result
    }

    Map getOrderNumbers(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select ord from Order ord where ord.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            //queryString += " and ord.orderNumber like :query"
            queryString += " and genfunc_filter_matcher(ord.orderNumber,:query) = true"
        }
        queryString += " order by ord.orderNumber asc"
        List orderNumbers = Order.executeQuery(queryString,filter)
        orderNumbers.each { Order ord ->
            result.results.add([name:ord.orderNumber,value:ord.orderNumber])
        }
        result
    }

    Map getReferences(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select distinct(ci.reference) from CostItem ci where ci.owner = :owner and ci.reference != null'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",params.query)
            queryString += " and genfunc_filter_matcher(ci.reference,:query) = true"
        }
        queryString += " order by ci.reference asc"
        List references = CostItem.executeQuery(queryString,filter)
        references.each { String r ->
            result.results.add([name:r,value:r])
        }
        result
    }

    List getOrgs() {
        Org org = contextService.getOrg()
        List<Map<String,Object>> result = []
        //to translate in hql: select org_name from org left join combo on org_id = combo_from_org_fk where combo_to_org_fk = 1 or org_sector_rv_fk = 82 order by org_sortname asc, org_name asc;
        List orgs = Org.executeQuery("select new map(o.id as id,o.name as name,o.sortname as sortname) from Combo c right join c.fromOrg o where (o.status = null or o.status != :deleted) and (c.toOrg = :contextOrg or o.sector = :publisher) order by o.sortname asc, o.name asc",[contextOrg:org, deleted:RDStore.O_STATUS_DELETED, publisher: RDStore.O_SECTOR_PUBLISHER])
        orgs.each { row ->
            if(row.id != org.id) {
                String text = row.sortname ? "${row.sortname} (${row.name})" : "${row.name}"
                result.add([id:row.id,text:text])
            }
        }
        result
    }

    Map getElements(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        if(params.org == "true") {
            List allOrgs = DocContext.executeQuery('select distinct dc.org,dc.org.sortname from DocContext dc where dc.owner.owner = :ctxOrg and dc.org != null and (genfunc_filter_matcher(dc.org.name,:query) = true or genfunc_filter_matcher(dc.org.sortname,:query) = true) order by dc.org.sortname asc',[ctxOrg:org,query:params.query])
            allOrgs.each { DocContext it ->
                result.results.add([name:"(${messageSource.getMessage('spotlight.organisation',null,LocaleContextHolder.locale)}) ${it[0].name}",value:genericOIDService.getOID(it[0])])
            }
        }
        if(params.license == "true") {
            List allLicenses = DocContext.executeQuery('select distinct dc.license,dc.license.reference from DocContext dc where dc.owner.owner = :ctxOrg and dc.license != null and genfunc_filter_matcher(dc.license.reference,:query) = true order by dc.license.reference asc',[ctxOrg:org,query:params.query])
            allLicenses.each { DocContext it ->
                License license = (License) it[0]
                String licenseStartDate = license.startDate ? sdf.format(license.startDate) : '???'
                String licenseEndDate = license.endDate ? sdf.format(license.endDate) : ''
                result.results.add([name:"(${messageSource.getMessage('spotlight.license',null,LocaleContextHolder.locale)}) ${it[1]} - (${licenseStartDate} - ${licenseEndDate})",value:genericOIDService.getOID(license)])
            }
        }
        if(params.subscription == "true") {
            List allSubscriptions = DocContext.executeQuery('select distinct dc.subscription,dc.subscription.name from DocContext dc where dc.owner.owner = :ctxOrg and dc.subscription != null and genfunc_filter_matcher(dc.subscription.name,:query) = true order by dc.subscription.name asc',[ctxOrg:org,query:params.query])
            allSubscriptions.each { DocContext it ->
                Subscription subscription = (Subscription) it[0]
                /*
                String tenant
                if(subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION && subscription.getConsortia().id == org.id) {
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
                result.results.add([name:"(${messageSource.getMessage('spotlight.subscription',null,LocaleContextHolder.locale)}) ${subscription.dropdownNamingConvention()}",value:genericOIDService.getOID(it[0])])
            }
        }
        if(params.package == "true") {
            List allPackages = DocContext.executeQuery('select distinct dc.pkg,dc.pkg.name from DocContext dc where dc.owner.owner = :ctxOrg and dc.pkg != null and genfunc_filter_matcher(dc.pkg.name,:query) = true order by dc.pkg.name asc', [ctxOrg: org, query: params.query])
            allPackages.each { DocContext it ->
                result.results.add([name: "(${messageSource.getMessage('spotlight.package', null, LocaleContextHolder.locale)}) ${it[1]}", value: genericOIDService.getOID(it[0])])
            }
        }
        result
    }


    List getAllPossibleTitleTypes() {
        return TitleInstancePackagePlatform.executeQuery('select distinct(tipp.titleType) from TitleInstancePackagePlatform tipp where tipp.titleType is not null')
    }

    Set<String> getAllPossibleSeriesByPackage(Package pkg, String forTitles) {
        Locale locale = LocaleContextHolder.getLocale()
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> seriesName = []

        seriesName = TitleInstancePackagePlatform.executeQuery("select distinct(seriesName) from TitleInstancePackagePlatform where seriesName is not null and pkg = :pkg and status = :status order by seriesName", [pkg: pkg, status: tippStatus])

        if(seriesName.size() == 0){
            seriesName << messageSource.getMessage('titleInstance.noSeriesName.label', null, locale)
        }
        seriesName
    }

    Set<String> getAllPossibleSeriesBySub(Subscription subscription) {
        Locale locale = LocaleContextHolder.getLocale()
        Set<String> seriesName = []

        if(subscription.packages){
            seriesName = TitleInstancePackagePlatform.executeQuery("select distinct(seriesName) from TitleInstancePackagePlatform where seriesName is not null and pkg in (:pkg) order by seriesName", [pkg: subscription.packages.pkg])
        }
        if(seriesName.size() == 0){
            seriesName << messageSource.getMessage('titleInstance.noSeriesName.label', null, locale)
        }
        seriesName
    }

    Set<String> getAllPossibleSubjectsByPackage(Package pkg, String forTitles) {
        Locale locale = LocaleContextHolder.getLocale()
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        SortedSet<String> subjects = new TreeSet<String>()

        List<String> rawSubjects = TitleInstancePackagePlatform.executeQuery("select distinct(subjectReference) from TitleInstancePackagePlatform where subjectReference is not null and pkg = :pkg and status = :status order by subjectReference", [pkg: pkg, status: tippStatus])

        if(rawSubjects.size() == 0){
            subjects << messageSource.getMessage('titleInstance.noSubjectReference.label', null, locale)
        }
        else {
            rawSubjects.each { String rawSubject ->
                rawSubject.tokenize(',;|').each { String rs ->
                    subjects.add(rs.trim())
                }
            }
        }

        subjects
    }

    Set<String> getAllPossibleSubjectsBySub(Subscription subscription) {
        Locale locale = LocaleContextHolder.getLocale()
        SortedSet<String> subjects = new TreeSet<String>()
        List<String> rawSubjects = []

        if(subscription.packages){
            rawSubjects = TitleInstancePackagePlatform.executeQuery("select distinct(subjectReference) from TitleInstancePackagePlatform where subjectReference is not null and pkg in (:pkg) order by subjectReference", [pkg: subscription.packages.pkg])
        }
        if(rawSubjects.size() == 0){
            subjects << messageSource.getMessage('titleInstance.noSubjectReference.label', null, locale)
        }
        else {
            rawSubjects.each { String rawSubject ->
                rawSubject.tokenize(',;|').each { String rs ->
                    subjects.addAll(rs.trim())
                }
            }
        }

        subjects
    }

    Set<String> getAllPossibleDateFirstOnlineYearByPackage(Package pkg, String forTitles) {
        Locale locale = LocaleContextHolder.getLocale()
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> subjects = []

        subjects = TitleInstancePackagePlatform.executeQuery("select distinct(Year(dateFirstOnline)) from TitleInstancePackagePlatform where dateFirstOnline is not null and pkg = :pkg and status = :status order by YEAR(dateFirstOnline)", [pkg: pkg, status: tippStatus])

        if(subjects.size() == 0){
            subjects << messageSource.getMessage('default.selectionNotPossible.label', null, locale)
        }

        subjects
    }

    Set<String> getAllPossibleDateFirstOnlineYearBySub(Subscription subscription) {
        Locale locale = LocaleContextHolder.getLocale()
        Set<String> yearsFirstOnline = []

        if(subscription.packages){
            yearsFirstOnline = TitleInstancePackagePlatform.executeQuery("select distinct(YEAR(dateFirstOnline)) from TitleInstancePackagePlatform where dateFirstOnline is not null and pkg in (:pkg) and status = :current order by YEAR(dateFirstOnline)", [pkg: subscription.packages.pkg,current: RDStore.TIPP_STATUS_CURRENT])
        }
        if(yearsFirstOnline.size() == 0){
            yearsFirstOnline << messageSource.getMessage('default.selectionNotPossible.label', null, locale)
        }

        yearsFirstOnline
    }

    Set<String> getAllPossiblePublisherByPackage(Package pkg,String forTitles) {
        Locale locale = LocaleContextHolder.getLocale()
        RefdataValue tippStatus = getTippStatusForRequest(forTitles)
        Set<String> publishers = []

        //publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(orgRole.org.name) from TitleInstancePackagePlatform tipp left join tipp.orgs orgRole where orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and tipp.pkg = :pkg and tipp.status = :status order by orgRole.org.name", [pkg: pkg, status: tippStatus]))
        publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(publisherName) from TitleInstancePackagePlatform where publisherName is not null and pkg = :pkg and status = :status order by publisherName", [pkg: pkg, status: tippStatus]))

        /*if(publishers.size() == 0){
            publishers << messageSource.getMessage('default.selectionNotPossible.label', null, locale)
        }*/
        publishers
    }

    Set<String> getAllPossiblePublisherBySub(Subscription subscription) {
        Locale locale = LocaleContextHolder.getLocale()
        Set<String> publishers = []

        if(subscription.packages){
            //publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(orgRole.org.name) from TitleInstancePackagePlatform tipp left join tipp.orgs orgRole where orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and tipp.pkg in (:pkg) order by orgRole.org.name", [pkg: subscription.packages.pkg]))
            publishers.addAll(TitleInstancePackagePlatform.executeQuery("select distinct(publisherName) from TitleInstancePackagePlatform where publisherName is not null and pkg in (:pkg) and status = :current order by publisherName", [pkg: subscription.packages.pkg,current: RDStore.TIPP_STATUS_CURRENT]))
        }
        /*if(publishers.size() == 0){
            publishers << messageSource.getMessage('default.selectionNotPossible.label', null, locale)
        }*/


        publishers
    }

    RefdataValue getTippStatusForRequest(String forTitles) {
        switch(forTitles) {
            case 'planned': RDStore.TIPP_STATUS_EXPECTED
                break
            case 'expired': RDStore.TIPP_STATUS_RETIRED
                break
            default: RDStore.TIPP_STATUS_CURRENT
                break
        }
    }
}
