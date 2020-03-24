package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.AccessService
import de.laser.CacheService
import de.laser.ContextService
import de.laser.EscapeService
import de.laser.exceptions.FinancialDataException
import de.laser.helper.DateUtil
import de.laser.helper.EhcacheWrapper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Year

import static de.laser.helper.RDStore.*
import static de.laser.helper.RDConstants.*
import static de.laser.interfaces.TemplateSupport.*

/**
 * This service will subsequently replace the very complicatedly written methods in the FinanceController class.
 *
 * @author agalffy
 */
@Transactional
class FinanceService {

    ContextService contextService
    GenericOIDService genericOIDService
    MessageSource messageSource
    AccessService accessService
    EscapeService escapeService
    SpringSecurityService springSecurityService
    CacheService cacheService
    String genericExcludes = ' and ci.surveyOrg = null and ci.costItemStatus != :deleted '
    Map<String,RefdataValue> genericExcludeParams = [deleted:COST_ITEM_DELETED]

    /**
     * Will replace the methods index and financialData methods in FinanceController class for a single subscription.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @param subscription - the subscription for which the financial data is retrieved. Its type determines the views displayed on return.
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItemsForSubscription(GrailsParameterMap params,Map configMap) throws FinancialDataException {
        if(configMap.subscription) {
            Subscription sub = (Subscription) configMap.subscription
            Org org = (Org) configMap.institution
            Map<String,Object> filterQuery = processFilterParams(params)
            EhcacheWrapper cache = cacheService.getTTL300Cache('/finance/filter/')
            if(params.reset || params.submit)
                cache.put('cachedFilter',filterQuery)
            else if(cache && cache.get('cachedFilter'))
                filterQuery = (Map<String,Object>) cache.get('cachedFilter')
            Map<String,Object> result = [filterPresets:filterQuery.filterData]
            if(filterQuery.subFilter || filterQuery.ciFilter)
                result.filterSet = true
            configMap.dataToDisplay.each { String dataToDisplay ->
                switch(dataToDisplay) {
                    case "own":
                        Set<CostItem> ownCostItems = CostItem.executeQuery(
                                'select ci from CostItem ci where ci.owner = :owner and ci.sub = :sub '+
                                        genericExcludes + filterQuery.subFilter + filterQuery.ciFilter +
                                        ' order by '+configMap.sortConfig.ownSort+' '+configMap.sortConfig.ownOrder,
                                [owner:org,sub:sub]+genericExcludeParams+filterQuery.filterData)
                        result.own = [count:ownCostItems.size()]
                        if(ownCostItems){
                            result.own.costItems = ownCostItems.drop(configMap.offsets.ownOffset).take(configMap.max)
                            result.own.sums = calculateResults(ownCostItems)
                        }
                        break
                    case "cons":
                        List consCostRows = CostItem.executeQuery(
                                'select ci, ' +
                                        '(select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType in (:roleTypes)) as sortname ' +
                                'from CostItem as ci where ci.owner = :owner and ci.sub in (select sub from Subscription as sub where sub.instanceOf = :sub '+
                                filterQuery.subFilter + ')' + genericExcludes + filterQuery.ciFilter +
                                ' order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder,
                                [owner:org,sub:sub,roleTypes:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]+genericExcludeParams+filterQuery.filterData)
                        result.cons = [count:consCostRows.size()]
                        if(consCostRows) {
                            List<CostItem> consCostItems = consCostRows.collect { row -> row[0]}
                            result.cons.costItems = consCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                            result.cons.sums = calculateResults(consCostItems)
                        }
                        break
                    case "consAtSubscr": List<CostItem> consCostItems = CostItem.executeQuery('select ci from CostItem as ci join ci.sub sub where ci.owner = :owner and sub = :sub'+
                            filterQuery.subFilter + genericExcludes + filterQuery.ciFilter +
                            ' order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder,
                            [owner:org,sub:sub]+genericExcludeParams+filterQuery.filterData)
                        result.cons = [count:consCostItems.size()]
                        if(consCostItems) {
                            result.cons.costItems = consCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                            result.cons.sums = calculateResults(consCostItems)
                        }
                        break
                    case "coll":
                        List collCostRows = CostItem.executeQuery(
                                'select ci, ' +
                                        '(select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType = :roleType) as sortname ' +
                                'from CostItem as ci where ci.owner = :owner and ci.sub in (select sub from Subscription as sub where sub.instanceOf = :sub '+
                                filterQuery.subFilter + ')' + genericExcludes + filterQuery.ciFilter +
                                ' order by '+configMap.sortConfig.collSort+' '+configMap.sortConfig.collOrder,
                                [owner:org,sub:sub,roleType:OR_SUBSCRIBER_COLLECTIVE]+genericExcludeParams+filterQuery.filterData)
                        result.coll = [count:0]
                        if(collCostRows) {
                            Set<CostItem> collCostItems = collCostRows.collect { row -> row[0]}
                            result.coll.count = collCostItems.size()
                            result.coll.costItems = collCostItems.drop(configMap.offsets.collOffset).take(configMap.max)
                            result.coll.sums = calculateResults(collCostItems)
                        }
                        break
                    case "collAtSubscr": List<CostItem> collCostItems = CostItem.executeQuery('select ci from CostItem as ci join ci.sub sub where ci.owner = :owner and sub = :sub'+
                            filterQuery.subFilter + genericExcludes + filterQuery.ciFilter +
                                    ' order by '+configMap.sortConfig.collSort+' '+configMap.sortConfig.collOrder,
                            [owner:org,sub:sub]+genericExcludeParams+filterQuery.filterData)
                        result.coll = [count:collCostItems.size()]
                        if(collCostItems) {
                            result.coll.costItems = collCostItems.drop(configMap.offsets.collOffset).take(configMap.max)
                            result.coll.sums = calculateResults(collCostItems)
                        }
                        break
                    case "subscr":
                        List<CostItem> subscrCostItems = CostItem.executeQuery(
                                'select ci from CostItem as ci join ci.sub sub where ci.owner in :owner and sub = :sub and ci.isVisibleForSubscriber = true'+
                                 genericExcludes + filterQuery.subFilter + filterQuery.ciFilter +
                                 ' order by '+configMap.sortConfig.subscrSort+' '+configMap.sortConfig.subscrOrder,
                                 [owner:[sub.getConsortia(),sub.getCollective()],sub:sub]+genericExcludeParams+filterQuery.filterData)
                        result.subscr = [count:subscrCostItems.size()]
                        if(subscrCostItems) {
                            result.subscr.costItems = subscrCostItems.drop(configMap.offsets.subscrOffset).take(configMap.max)
                            result.subscr.sums = calculateResults(subscrCostItems)
                        }
                        break
                }
            }
            result
        }
        else if(!configMap.subscription) {
            throw new FinancialDataException("Method called without valid subscription data!")
        }
    }

    /**
     * Will replace the methods index and financialData methods in FinanceController class for the institution-wide overview.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map<String,Object> getCostItems(GrailsParameterMap params, Map configMap) throws FinancialDataException {
        Map<String,Object> filterQuery = processFilterParams(params)
        EhcacheWrapper cache = cacheService.getTTL300Cache('/finance/filter/')
        if(params.reset || params.submit)
            cache.put('cachedFilter',filterQuery)
        else if(cache && cache.get('cachedFilter'))
            filterQuery = (Map<String,Object>) cache.get('cachedFilter')
        Map<String,Object> result = [filterPresets:filterQuery.filterData]
        if(filterQuery.subFilter || filterQuery.ciFilter)
            result.filterSet = true
        Org org = (Org) configMap.institution
        configMap.dataToDisplay.each { String dataToDisplay ->
            switch(dataToDisplay) {
                //get own costs
                case "own":
                    //exclude double listing of cost items belonging to member subscriptions
                    String instanceFilter = ""
                    if(org.hasPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")) {
                        instanceFilter = " and sub.instanceOf = null "
                    }
                    String subJoin = filterQuery.subFilter || instanceFilter ? "join ci.sub sub " : ""
                    String queryStringBase = "select ci from CostItem ci ${subJoin}" +
                        "where ci.owner = :org ${genericExcludes+filterQuery.subFilter+instanceFilter+filterQuery.ciFilter} "+
                        "order by "+configMap.sortConfig.ownSort+" "+configMap.sortConfig.ownOrder
                    List<CostItem> ownSubscriptionCostItems = CostItem.executeQuery(queryStringBase,[org:org]+genericExcludeParams+filterQuery.filterData)
                    result.own = [count:ownSubscriptionCostItems.size()]
                    if(ownSubscriptionCostItems) {
                        result.own.costItems = ownSubscriptionCostItems.drop(configMap.offsets.ownOffset).take(configMap.max)
                        result.own.sums = calculateResults(ownSubscriptionCostItems)
                    }
                        break
                //get consortial costs
                case "cons": List consortialCostRows = CostItem.executeQuery('select ci, orgRoles.org.sortname as sortname from CostItem ci ' +
                        'join ci.owner orgC ' +
                        'join ci.sub sub ' +
                        'join sub.instanceOf subC ' +
                        'join subC.orgRelations roleC ' +
                        'join sub.orgRelations roleMC ' +
                        'join sub.orgRelations orgRoles ' +
                        'where orgC = :org and orgC = roleC.org and roleMC.roleType in :consortialType and orgRoles.roleType in (:subscrType)'+
                        genericExcludes+filterQuery.subFilter+filterQuery.ciFilter+
                        ' order by '+configMap.sortConfig.consSort+' '+configMap.sortConfig.consOrder,
                        [org:org,consortialType:[OR_SUBSCRIPTION_CONSORTIA],subscrType:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]+genericExcludeParams+filterQuery.filterData)
                    result.cons = [count:consortialCostRows.size()]
                    if(consortialCostRows) {
                        List<CostItem> consortialCostItems = consortialCostRows.collect { row -> row[0] }
                        //very ugly ... any ways to achieve this more elegantly are greatly appreciated!!
                        if(configMap.sortConfig.consSort == 'sortname') {
                            consortialCostItems = consortialCostItems.sort{ ciA, ciB ->
                                ciA.sub?.orgRelations?.find{ oo -> oo.roleType in [OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]}?.org?.sortname <=> ciB.sub?.orgRelations?.find{ oo -> oo.roleType in [OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]}?.org?.sortname ?:
                                        ciA.sub?.orgRelations?.find { oo -> oo.roleType in [OR_AGENCY,OR_PROVIDER]}?.org?.name <=> ciB.sub?.orgRelations?.find{ oo -> oo.roleType in [OR_AGENCY,OR_PROVIDER]}?.org?.name}
                        }
                        result.cons.costItems = consortialCostItems.drop(configMap.offsets.consOffset).take(configMap.max)
                        result.cons.sums = calculateResults(consortialCostItems)
                    }
                    break
                //get collective costs
                case "coll": List collectiveCostRows = CostItem.executeQuery('select ci, orgRoles.org.name as sortname from CostItem ci ' +
                        'join ci.owner orgC ' +
                        'join ci.sub sub ' +
                        'join sub.instanceOf subC ' +
                        'join subC.orgRelations roleC ' +
                        'join sub.orgRelations roleMC ' +
                        'join sub.orgRelations orgRoles ' +
                        'where orgC = :org and orgC = roleC.org and roleMC.roleType in :collectiveType and orgRoles.roleType in (:subscrType)'+
                        genericExcludes + filterQuery.subFilter + filterQuery.ciFilter +
                        ' order by '+configMap.sortConfig.collSort+' '+configMap.sortConfig.collOrder,
                        [org:org,collectiveType:[OR_SUBSCRIPTION_COLLECTIVE],subscrType:[OR_SUBSCRIBER_COLLECTIVE]]+genericExcludeParams+filterQuery.filterData)
                    result.coll = [count:0]
                    if(collectiveCostRows) {
                        Set<CostItem> collectiveCostItems = collectiveCostRows.collect { row -> row[0] }
                        result.coll.count = collectiveCostItems.size()
                        result.coll.costItems = collectiveCostItems.drop(configMap.offsets.collOffset).take(configMap.max)
                        result.coll.sums = calculateResults(collectiveCostItems)
                    }
                    break
                //get membership costs
                case "subscr": List<CostItem> consortialMemberSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci '+
                        'join ci.sub sub ' +
                        'left join ci.subPkg subPkg ' +
                        'join sub.instanceOf subC ' +
                        'join subC.orgRelations roleC ' +
                        'join sub.orgRelations orgRoles ' +
                        'join ci.owner orgC ' +
                        'where orgC = roleC.org and roleC.roleType in :consType and orgRoles.org = :org and orgRoles.roleType in :subscrType and ci.isVisibleForSubscriber = true'+
                        genericExcludes + filterQuery.subFilter + filterQuery.ciFilter +
                        ' order by '+configMap.sortConfig.subscrSort+' '+configMap.sortConfig.subscrOrder,
                        [org:org,consType:[OR_SUBSCRIPTION_CONSORTIA,OR_SUBSCRIPTION_COLLECTIVE],subscrType:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_COLLECTIVE]]+genericExcludeParams+filterQuery.filterData)
                    result.subscr = [count:consortialMemberSubscriptionCostItems.size()]
                    if(consortialMemberSubscriptionCostItems) {
                        result.subscr.sums = calculateResults(consortialMemberSubscriptionCostItems)
                        result.subscr.costItems = consortialMemberSubscriptionCostItems.drop(configMap.offsets.subscrOffset).take(configMap.max)
                    }
                    break
                default: log.info("display call ${dataToDisplay} not handled here ... skipping ...")
                    break
            }
        }
        result
    }

    /**
     * Processes the given parameters to build a query part which will be included into the base query strings
     *
     * @param params - a GrailsParameterMap containing parameters to be processed
     * @return an array with the filter string on position 0 and the filter parameter map on position 1
     */
    Map<String,Object> processFilterParams(GrailsParameterMap params) {
        String subFilterQuery = "", costItemFilterQuery = ""
        LinkedHashMap queryParams = [:]
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        //subscription filter settings
        //subscription members
        if(params.filterSubMembers) {
            subFilterQuery += " and sub in (:filterSubMembers) "
            List<Subscription> filterSubMembers = []
            String[] subMembers = params.list("filterSubMembers")
            subMembers.each { subMember ->
                filterSubMembers.add(Subscription.get(Long.parseLong(subMember)))
            }
            queryParams.filterSubMembers = filterSubMembers
            log.info(queryParams.filterSubMembers)
        }
        //providers
        if(params.filterSubProviders) {
            subFilterQuery += " and sub in (select oo.sub from OrgRole as oo where oo.org in (:filterSubProviders)) "
            List<Org> filterSubProviders = []
            String[] subProviders = params.list("filterSubProviders")
            subProviders.each { subProvider ->
                filterSubProviders.add(genericOIDService.resolveOID(subProvider))
            }
            queryParams.filterSubProviders = filterSubProviders
            log.info(queryParams.filterSubProviders)
        }
        //subscription status
        //we have to distinct between not existent and present but zero length
        if(params.filterSubStatus) {
            subFilterQuery += " and sub.status = :filterSubStatus "
            queryParams.filterSubStatus = RefdataValue.get(Long.parseLong(params.filterSubStatus))
            log.info(queryParams.filterSubStatus)
        }
        //!params.filterSubStatus is insufficient because it checks also the presence of a value - but the absence of a value is a valid setting (= all status except deleted; that is captured by the genericExcludes field)
        else if(!params.subscription && !params.sub && !params.containsKey('filterSubStatus')) {
            subFilterQuery += " and sub.status = :filterSubStatus "
            queryParams.filterSubStatus = SUBSCRIPTION_CURRENT
            params.filterSubStatus = SUBSCRIPTION_CURRENT.id.toString()
        }
        //cost item filter settings
        //cost item title
        if(params.filterCITitle) {
            costItemFilterQuery += " and (ci.costTitle like :filterCITitle or ci.costTitle like :ciTitleLowerCase) "
            queryParams.filterCITitle = "%${params.filterCITitle}%"
            queryParams.ciTitleLowerCase = "%${params.filterCITitle.toLowerCase()}%"
            log.info(queryParams.filterCITitle)
        }
        //cost item subscription
        if(params.filterCISub) {
            costItemFilterQuery += " and sub in (:filterCISub) "
            List<Subscription> filterSubs = []
            String[] subscriptions = params.filterCISub.split(',')
            subscriptions.each { sub ->
                filterSubs.add((Subscription) genericOIDService.resolveOID(sub))
            }
            queryParams.filterCISub = filterSubs
            log.info(queryParams.filterCISub)
        }
        //subscription package
        if(params.filterCISPkg) {
            costItemFilterQuery += " and ci.subPkg in (:filterCISPkg) "
            List<SubscriptionPackage> filterSubPackages = []
            String[] subscriptionPackages = params."filterCISPkg".split(',')
            subscriptionPackages.each { subPkg ->
                filterSubPackages.add((SubscriptionPackage) genericOIDService.resolveOID(subPkg))
            }
            queryParams.filterCISPkg = filterSubPackages
            log.info(queryParams.filterCISPkg)
        }
        //budget code
        if(params.filterCIBudgetCode) {
            costItemFilterQuery += " and ci in (select cig.costItem from CostItemGroup cig where cig.budgetCode in (:filterCIBudgetCode)) "
            List<BudgetCode> filterBudgetCodes = []
            String[] budgetCodes = params."filterCIBudgetCode".split(',')
            budgetCodes.each { bc ->
                filterBudgetCodes.add(BudgetCode.get(Long.parseLong(bc)))
            }
            queryParams.filterCIBudgetCode = filterBudgetCodes
            log.info(queryParams.filterCIBudgetCode)
        }
        //reference/code
        if(params.filterCIReference) {
            costItemFilterQuery += " and ci.reference in (:filterCIReference) "
            List<String> filterReferences = params."filterCIReference".split(',')
            queryParams.filterCIReference = filterReferences
            log.info(queryParams.filterCIReference)
        }
        //invoice number
        if(params.filterCIInvoiceNumber) {
            costItemFilterQuery += " and ci.invoice.invoiceNumber in (:filterCIInvoiceNumber) "
            List<String> filterInvoiceNumbers = []
            String[] invoiceNumbers = params."filterCIInvoiceNumber".split(',')
            invoiceNumbers.each { invNum ->
                filterInvoiceNumbers.add(invNum)
            }
            queryParams.filterCIInvoiceNumber = filterInvoiceNumbers
            log.info(queryParams.filterCIInvoiceNumber)
        }
        //order number
        if(params.filterCIOrderNumber) {
            costItemFilterQuery += " and ci.order.orderNumber in (:filterCIOrderNumber) "
            List<String> filterOrderNumbers = []
            String[] orderNumbers = params."filterCIOrderNumber".split(',')
            orderNumbers.each { orderNum ->
                filterOrderNumbers.add(orderNum)
            }
            queryParams.filterCIOrderNumber = filterOrderNumbers
            log.info(queryParams.filterCIOrderNumber)
        }
        //cost item element
        if(params.filterCIElement) {
            costItemFilterQuery += " and ci.costItemElement in (:filterCIElement) "
            List<RefdataValue> filterElements = []
            String[] costItemElements = params."filterCIElement".split(',')
            costItemElements.each { cie ->
                filterElements.add(genericOIDService.resolveOID(cie))
            }
            queryParams.filterCIElement = filterElements
            log.info(queryParams.filterCIElement)
        }
        //cost item status
        if(params.filterCIStatus) {
            costItemFilterQuery += " and ci.costItemStatus in (:filterCIStatus) "
            List<RefdataValue> filterStatus = []
            String[] costItemStatus = params."filterCIStatus".split(',')
            costItemStatus.each { cis ->
                filterStatus.add(genericOIDService.resolveOID(cis))
            }
            queryParams.filterCIStatus = filterStatus
            log.info(queryParams.filterCIStatus)
        }
        //tax type
        if(params.filterCITaxType) {
            if(params.filterCITaxType == 'null') {
                costItemFilterQuery += " and ci.taxKey = null"
            }
            else {
                costItemFilterQuery += " and ci.taxKey = :filterCITaxType "
                queryParams.filterCITaxType = CostItem.TAX_TYPES.valueOf(params.filterCITaxType)
            }
            log.info(params.filterCITaxType)
        }
        //financial year
        if(params.filterCIFinancialYear) {
            costItemFilterQuery += " and ci.financialYear = :filterCIFinancialYear "
            Year financialYear = Year.parse(params.filterCIFinancialYear)
            queryParams.filterCIFinancialYear = financialYear
            log.info(queryParams.filterCIFinancialYear)
        }
        //invoice from
        if(params.filterCIInvoiceFrom) {
            costItemFilterQuery += " and (ci.invoiceDate >= :filterCIInvoiceFrom AND ci.invoiceDate is not null) "
            Date invoiceFrom = sdf.parse(params.filterCIInvoiceFrom)
            queryParams.filterCIInvoiceFrom = invoiceFrom
            log.info(queryParams.filterCIInvoiceFrom)
        }
        //invoice to
        if(params.filterCIInvoiceTo) {
            costItemFilterQuery += " and (ci.invoiceDate <= :filterCIInvoiceTo AND ci.invoiceDate is not null) "
            Date invoiceTo = sdf.parse(params.filterCIInvoiceTo)
            queryParams.filterCIInvoiceTo = invoiceTo
            log.info(queryParams.filterCIInvoiceTo)
        }
        //valid on
        if(params.filterCIValidOn) {
            costItemFilterQuery += " and (ci.startDate <= :filterCIValidOn OR ci.startDate is null) and (ci.endDate >= :filterCIValidOn OR ci.endDate is null) "
            Date validOn = sdf.parse(params.filterCIValidOn)
            queryParams.filterCIValidOn = validOn
            log.info(queryParams.filterCIValidOn)
        }
        //paid from
        if(params.filterCIPaidFrom) {
            costItemFilterQuery += " and (ci.datePaid >= :filterCIPaidFrom AND ci.datePaid is not null) "
            Date invoiceFrom = sdf.parse(params.filterCIPaidFrom)
            queryParams.filterCIPaidFrom = invoiceFrom
            log.info(queryParams.filterCIPaidFrom)
        }
        //paid to
        if(params.filterCIPaidTo) {
            costItemFilterQuery += " and (ci.datePaid <= :filterCIPaidTo AND ci.datePaid is not null) "
            Date invoiceTo = sdf.parse(params.filterCIPaidTo)
            queryParams.filterCIPaidTo = invoiceTo
            log.info(queryParams.filterCIPaidTo)
        }
        return [subFilter:subFilterQuery,ciFilter:costItemFilterQuery,filterData:queryParams]
    }

    /**
     * Will replace the current client-side calculation of the sums.
     * Calculates to a given key the sums (local and for each currency) and assigns the resulting map to a given key.
     *
     * @param key - the key for which the sum is being calculated
     * @param costItems - a list of cost items to count
     * @return a map with the following structure:
     * {
     *     localSum (means the actual value and not the amount which is going to be paid)
     *     localSumAfterTax (for Germany, VATs of 7 and 19 per cent apply) (consider ERMS-957 about that!)
     *     billingSums: {
     *        currency
     *        billingSum
     *        billingSumAfterTax (see above; consider ERMS-957, too!)
     *     }
     * }
     *
     */
    Map calculateResults(Collection<CostItem> costItems) {
        //List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * ((ci.taxKey.taxRate/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value",[costItems:costItems])
        List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax,ci.billingCurrency.order as ciOrder) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value, ci.billingCurrency.order order by ciOrder",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        List<Map> billingSumsNegative = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax,ci.billingCurrency.order as ciOrder) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative' group by ci.billingCurrency.value, ci.billingCurrency.order order by ciOrder",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        Map localSumsPositive = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive'",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
        Map localSumsNegative = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative'",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
        List<Map> billingSums = []
        Set<String> positiveCurrencies = []
        Map localSums = [:]
        double billingSum = 0.0
        double billingSumAfterTax = 0.0
        if(billingSumsPositive.size() > 0) {
            billingSumsPositive.each { posEntry ->
                if (billingSumsNegative.size() > 0) {
                    int index = getCurrencyIndexInList(billingSumsNegative,posEntry.currency)
                    if(index > -1) {
                        Map negEntry = billingSumsNegative[index]
                        billingSum = posEntry.billingSum - negEntry.billingSum
                        billingSumAfterTax = posEntry.billingSumAfterTax - negEntry.billingSumAfterTax
                    }
                    else {
                        billingSum = posEntry.billingSum
                        billingSumAfterTax = posEntry.billingSumAfterTax
                    }
                }
                else {
                    billingSum = posEntry.billingSum
                    billingSumAfterTax = posEntry.billingSumAfterTax
                }
                positiveCurrencies << posEntry.currency
                billingSums.add([currency: posEntry.currency, billingSum: billingSum, billingSumAfterTax: billingSumAfterTax])
            }
        }
        if(billingSumsNegative.size() > 0) {
            billingSumsNegative.each { negEntry ->
                if(!positiveCurrencies.contains(negEntry.currency))
                    billingSums.add([currency: negEntry.currency, billingSum: negEntry.billingSum * (-1), billingSumAfterTax: negEntry.billingSumAfterTax * (-1)])
            }
        }
        if(localSumsPositive.localSum && localSumsPositive.localSumAfterTax) {
            if(localSumsNegative.localSum && localSumsNegative.localSumAfterTax) {
                localSums.localSum = localSumsPositive.localSum-localSumsNegative.localSum
                localSums.localSumAfterTax = localSumsPositive.localSumAfterTax-localSumsNegative.localSumAfterTax
            }
            else {
                localSums.localSum = localSumsPositive.localSum
                localSums.localSumAfterTax = localSumsPositive.localSumAfterTax
            }
        }
        else if(localSumsNegative.localSum && localSumsNegative.localSumAfterTax) {
            localSums.localSum = localSumsNegative.localSum * (-1)
            localSums.localSumAfterTax = localSumsNegative.localSumAfterTax * (-1)
        }
        else {
            localSums.localSum = 0.0
            localSums.localSumAfterTax = 0.0
        }
        return [billingSums:billingSums,localSums:localSums]
    }

    /**
     * Finds the given currency in the given list of entries, returns -1 if the corrency is not found in the list.
     *
     * @param entryList - the list of currency entries
     * @param currency - the currency to be retrieved
     * @return the position index
     */
    int getCurrencyIndexInList(List entryList,String currency) {
        int ret = -1
        entryList.eachWithIndex { negEntry, int i ->
            if(currency.equals(negEntry.currency)) {
                ret = i
            }
        }
        return ret
    }


    Map<String,Map> financeImport(CommonsMultipartFile tsvFile) {

        Org contextOrg = contextService.org
        Map<String,Map> result = [:]
        Map<CostItem,Map> candidates = [:]
        Map<Integer,String> budgetCodes = [:]
        List<String> rows = tsvFile.getInputStream().text.split('\n')
        Map<String,Integer> colMap = [:]
        rows[0].split('\t').eachWithIndex { String headerCol, int c ->
            if(headerCol.startsWith("\uFEFF"))
                headerCol = headerCol.substring(1)
            switch(headerCol.toLowerCase().trim()) {
                case "bezeichnung":
                case "title": colMap.title = c
                    break
                case "element": colMap.element = c
                    break
                case "kostenvorzeichen":
                case "cost item sign": colMap.costItemSign = c
                    break
                case "budgetcode": colMap.budgetCode = c
                    break
                case "referenz/codes":
                case "reference/codes": colMap.reference = c
                    break
                case "status": colMap.status = c
                    break
                case "rechnungssumme":
                case "invoice total": colMap.invoiceTotal = c
                    break
                case "währung":
                case "waehrung":
                case "currency": colMap.currency = c
                    break
                case "umrechnungsfaktor":
                case "exchange rate": colMap.currencyRate = c
                    break
                case "steuerbar":
                case "tax type": colMap.taxType = c
                    break
                case "steuersatz":
                case "tax rate": colMap.taxRate = c
                    break
                case "wert":
                case "value": colMap.value = c
                    break
                case "lizenz":
                case "subscription": colMap.sub = c
                    break
                case "paket":
                case "package": colMap.subPkg = c
                    break
                case "einzeltitel":
                case "single title": colMap.ie = c
                    break
                case "gezahlt am":
                case "date paid": colMap.dateFrom = c
                    break
                case "haushaltsjahr":
                case "financial year": colMap.financialYear = c
                    break
                case "datum von":
                case "date from": colMap.dateFrom = c
                    break
                case "datum bis":
                case "date to": colMap.dateTo = c
                    break
                case "rechnungsdatum":
                case "invoice date": colMap.invoiceDate = c
                    break
                case "anmerkung":
                case "description": colMap.description = c
                    break
                case "rechnungsnummer":
                case "invoice number": colMap.invoiceNumber = c
                    break
                case "auftragsnummer":
                case "order number": colMap.orderNumber = c
                    break
                /*case "einrichtung":
                case "organisation": colMap.institution = c
                    break*/
                default: log.info("unhandled parameter type ${headerCol}, ignoring ...")
                    break
            }
        }
        rows.remove(0)
        Map<String,IdentifierNamespace> namespaces = [
                'wibid':IdentifierNamespace.findByNs('wibid'),
                'isil':IdentifierNamespace.findByNs('ISIL'),
                'doi':IdentifierNamespace.findByNs('doi'),
                'zdb':IdentifierNamespace.findByNs('zdb'),
                'issn':IdentifierNamespace.findByNs('issn'),
                'eissn':IdentifierNamespace.findByNs('eissn')
        ]
        rows.eachWithIndex { row, Integer r ->
            log.debug("now processing entry ${r}")
            Map mappingErrorBag = [:]
            List<String> cols = row.split('\t')
            //check if we have some mandatory properties ...
            //owner(nullable: false, blank: false) -> to institution, defaults to context org
            /*Org owner
            String orgIdentifier
            if(colMap.institution)
                orgIdentifier = cols[colMap.institution]
            if(orgIdentifier){
                //fetch possible identifier namespaces
                // TODO [ticket=1789]
                //List<Org> orgMatches = Org.executeQuery("select distinct idOcc.org from IdentifierOccurrence idOcc join idOcc.identifier id where cast(idOcc.org.id as string) = :idCandidate or idOcc.org.globalUID = :idCandidate or (id.value = :idCandidate and id.ns = :wibid)",[idCandidate:orgIdentifier,wibid:namespaces.wibid])
                List<Org> orgMatches = Org.executeQuery("select distinct id.org from Identifier id where cast(id.org.id as string) = :idCandidate or id.org.globalUID = :idCandidate or (id.value = :idCandidate and id.ns = :wibid)", [idCandidate:orgIdentifier, wibid:namespaces.wibid])
                if(orgMatches.size() > 1)
                    mappingErrorBag.multipleOrgsError = orgMatches.collect { org -> org.sortname ?: org.name }
                else if(orgMatches.size() == 1) {
                    if(accessService.checkPerm('ORG_CONSORTIUM') && orgMatches[0].id != contextOrg.id) {
                        mappingErrorBag.ownerMismatchError = orgMatches[0]
                    }
                    else {
                        owner = orgMatches[0]
                    }
                }
                else {
                    owner = contextOrg
                }
            }
            else {
                owner = contextOrg
            }*/
            CostItem costItem = new CostItem(owner: contextOrg)
            //sub(nullable: true, blank: false) -> to subscription
            Subscription subscription
            if(colMap.sub != null) {
                String subIdentifier = cols[colMap.sub]
                if(subIdentifier) {
                    //fetch possible identifier namespaces
                    List<Subscription> subMatches
                    if(accessService.checkPerm("ORG_CONSORTIUM"))
                        subMatches = Subscription.executeQuery("select oo.sub from OrgRole oo where (cast(oo.sub.id as string) = :idCandidate or oo.sub.globalUID = :idCandidate) and oo.org = :org and oo.roleType in :roleType",[idCandidate:subIdentifier,org:costItem.owner,roleType:[OR_SUBSCRIPTION_CONSORTIA,OR_SUBSCRIBER]])
                    else if(accessService.checkPerm("ORG_INST"))
                        subMatches = Subscription.executeQuery("select oo.sub from OrgRole oo where (cast(oo.sub.id as string) = :idCandidate or oo.sub.globalUID = :idCandidate) and oo.org = :org and oo.roleType in :roleType",[idCandidate:subIdentifier,org:costItem.owner,roleType:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER]])
                    if(!subMatches)
                        mappingErrorBag.noValidSubscription = subIdentifier
                    else if(subMatches.size() > 1)
                        mappingErrorBag.multipleSubError = subMatches.collect { sub -> sub.dropdownNamingConvention(contextOrg) }
                    else if(subMatches.size() == 1) {
                        subscription = subMatches[0]
                        costItem.sub = subscription
                    }
                }
            }
            /*
            subPkg(nullable: true, blank: false, validator: { val, obj ->
                if (obj.subPkg) {
                    if (obj.subPkg.subscription.id != obj.sub.id) return ['subscriptionPackageMismatch']
                }
            }) -> to package
            */
            SubscriptionPackage subPkg
            if(colMap.subPkg != null) {
                String subPkgIdentifier = cols[colMap.subPkg]
                if(subPkgIdentifier) {
                    if(subscription == null)
                        mappingErrorBag.packageWithoutSubscription = true
                    else {
                        //List<Package> pkgMatches = Package.executeQuery("select distinct idOcc.pkg from IdentifierOccurrence idOcc join idOcc.identifier id where cast(idOcc.pkg.id as string) = :idCandidate or idOcc.pkg.globalUID = :idCandidate or (id.value = :idCandidate and id.ns = :isil)",[idCandidate:subPkgIdentifier,isil:namespaces.isil])
                        List<Package> pkgMatches = []
                        if(subPkgIdentifier.isLong())
                            pkgMatches.add(Package.get(subPkgIdentifier))
                        if(!pkgMatches) {
                            pkgMatches.addAll(Package.findAllByGlobalUID(subPkgIdentifier))
                            if(!pkgMatches) {
                                pkgMatches = Package.executeQuery("select distinct ident.pkg from Identifier ident where (ident.ns = :isil and ident.value = :idCandidate)")
                                if(!pkgMatches)
                                    mappingErrorBag.noValidPackage = subPkgIdentifier
                            }
                        }
                        if(pkgMatches.size() > 1)
                            mappingErrorBag.multipleSubPkgError = pkgMatches.collect { pkg -> pkg.name }
                        else if(pkgMatches.size() == 1) {
                            subPkg = SubscriptionPackage.findBySubscriptionAndPkg(subscription,pkgMatches[0])
                            if(subPkg)
                                costItem.subPkg = subPkg
                            else if(!subPkg)
                                mappingErrorBag.packageNotInSubscription = subPkgIdentifier
                        }
                    }
                }
            }
            /*
            issueEntitlement(nullable: true, blank: false, validator: { val, obj ->
                if (obj.issueEntitlement) {
                    if (!obj.subPkg || (obj.issueEntitlement.tipp.pkg.gokbId != obj.subPkg.pkg.gokbId)) return ['issueEntitlementNotInPackage']
                }
            }) -> to issue entitlement
            */
            IssueEntitlement ie
            if(colMap.ie != null) {
                String ieIdentifier = cols[colMap.ie]
                if(ieIdentifier) {
                    if(subscription == null || subPkg == null)
                        mappingErrorBag.entitlementWithoutPackageOrSubscription = true
                    else {
                        // TODO [ticket=1789]
                        // List<TitleInstance> titleMatches = TitleInstance.executeQuery("select distinct idOcc.ti from IdentifierOccurrence idOcc join idOcc.identifier id where id.value = :idCandidate and id.ns in :namespaces",[idCandidate: ieIdentifier, namespaces: [namespaces.isbn,namespaces.doi,namespaces.zdb,namespaces.issn,namespaces.eissn]])
                        List<TitleInstance> titleMatches = TitleInstance.executeQuery("select distinct id.ti from Identifier id where id.value = :idCandidate and id.ns in :namespaces", [idCandidate: ieIdentifier, namespaces: [namespaces.isbn,namespaces.doi,namespaces.zdb,namespaces.issn,namespaces.eissn]])
                        if(!titleMatches)
                            mappingErrorBag.noValidTitle = ieIdentifier
                        else if(titleMatches.size() > 1)
                            mappingErrorBag.multipleTitleError = titleMatches.collect { ti -> ti.title }
                        else if(titleMatches.size() == 1) {
                            TitleInstance tiMatch = titleMatches[0]
                            List<IssueEntitlement> ieMatches = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription and tipp.title = :titleInstance',[subscription:subscription,titleInstance:tiMatch])
                            if(!ieMatches)
                                mappingErrorBag.noValidEntitlement = ieIdentifier
                            else if(ieMatches.size() > 1)
                                mappingErrorBag.multipleEntitlementError = ieMatches.collect { entMatch -> "${entMatch.subscription.dropdownNamingConvention(contextOrg)} - ${entMatch.tipp.title.title}" }
                            else if(ieMatches.size() == 1) {
                                ie = ieMatches[0]
                                if(ie.tipp.pkg.gokbId != subPkg.pkg.gokbId)
                                    mappingErrorBag.entitlementNotInSubscriptionPackage = ieIdentifier
                                else
                                    costItem.issueEntitlement = ie
                            }
                        }
                    }
                }
            }
            //order(nullable: true, blank: false) -> to order number
            if(colMap.orderNumber != null) {
                String orderNumber = cols[colMap.orderNumber]
                if(orderNumber) {
                    List<Order> orderMatches = Order.findAllByOrderNumberAndOwner(orderNumber,contextOrg)
                    if(orderMatches.size() > 1)
                        mappingErrorBag.multipleOrderError = orderNumber
                    else if(orderMatches.size() == 1)
                        costItem.order = orderMatches[0]
                    else if(!orderMatches)
                        costItem.order = new Order(orderNumber: orderNumber, owner: contextOrg)
                }
            }
            //invoice(nullable: true, blank: false) -> to invoice number
            if(colMap.invoiceNumber != null) {
                String invoiceNumber = cols[colMap.invoiceNumber]
                if(invoiceNumber) {
                    List<Invoice> invoiceMatches = Invoice.findAllByInvoiceNumberAndOwner(invoiceNumber,contextOrg)
                    if(invoiceMatches.size() > 1)
                        mappingErrorBag.multipleInvoiceError = invoiceNumber
                    else if(invoiceMatches.size() == 1)
                        costItem.invoice = invoiceMatches[0]
                    else if(!invoiceMatches)
                        costItem.invoice = new Invoice(invoiceNumber: invoiceNumber, owner: contextOrg)
                }
            }
            //billingCurrency(nullable: true, blank: false) -> to currency
            if(colMap.currency != null) {
                String currencyKey = cols[colMap.currency]
                if(!currencyKey)
                    mappingErrorBag.noCurrencyError = true
                else {
                    RefdataValue currency = RefdataValue.getByValueAndCategory(currencyKey,"Currency")
                    if(!currency)
                        mappingErrorBag.invalidCurrencyError = true
                    else {
                        costItem.billingCurrency = currency
                    }
                }
            }
            //costDescription(nullable: true, blank: false) -> to description
            if(colMap.description != null) {
                costItem.costDescription = cols[colMap.description]
            }
            //costTitle(nullable: true, blank: false) -> to title
            if(colMap.title != null) {
                costItem.costTitle = cols[colMap.title]
            }
            //costInBillingCurrency(nullable: true, blank: false) -> to invoice total
            if(colMap.invoiceTotal != null) {
                try {
                    costItem.costInBillingCurrency = escapeService.parseFinancialValue(cols[colMap.invoiceTotal])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.invoiceTotalInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.invoiceTotalMissing = true
                }
            }
            //datePaid(nullable: true, blank: false) -> to date paid
            if(colMap.datePaid != null) {
                Date datePaid = DateUtil.parseDateGeneric(cols[colMap.datePaid])
                if(datePaid)
                    costItem.datePaid = datePaid
            }
            //costInLocalCurrency(nullable: true, blank: false) -> to value
            if(colMap.value != null) {
                try {
                    costItem.costInLocalCurrency = escapeService.parseFinancialValue(cols[colMap.value])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.valueInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.valueMissing = true
                }
            }
            //currencyRate(nullable: true, blank: false) -> to exchange rate
            if(colMap.currencyRate != null) {
                try {
                    costItem.currencyRate = escapeService.parseFinancialValue(cols[colMap.currencyRate])
                }
                catch (NumberFormatException e) {
                    mappingErrorBag.exchangeRateInvalid = true
                }
                catch (NullPointerException | ParseException e) {
                    mappingErrorBag.exchangeRateMissing = true
                }
            }
            //substitute missing values in case of
            if(costItem.costInBillingCurrency) {
                if(!costItem.currencyRate && costItem.costInLocalCurrency) {
                    costItem.currencyRate = costItem.costInLocalCurrency / costItem.costInBillingCurrency
                    mappingErrorBag.keySet().removeAll(['exchangeRateMissing','exchangeRateInvalid'])
                    mappingErrorBag.exchangeRateCalculated = true
                }
                else if(!costItem.costInLocalCurrency && costItem.currencyRate) {
                    costItem.costInLocalCurrency = costItem.costInBillingCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['valueMissing','valueInvalid'])
                    mappingErrorBag.valueCalculated = true
                }
            }
            if(costItem.costInLocalCurrency) {
                if(!costItem.currencyRate && costItem.costInBillingCurrency) {
                    costItem.currencyRate = costItem.costInLocalCurrency / costItem.costInBillingCurrency
                    mappingErrorBag.keySet().removeAll(['exchangeRateMissing','exchangeRateInvalid'])
                    mappingErrorBag.exchangeRateCalculated = true
                }
                else if(!costItem.costInBillingCurrency && costItem.currencyRate) {
                    costItem.costInBillingCurrency = costItem.costInLocalCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['invoiceTotalMissing','invoiceTotalInvalid'])
                    mappingErrorBag.invoiceTotalCalculated = true
                }
            }
            if(costItem.currencyRate) {
                if(!costItem.costInLocalCurrency && costItem.costInBillingCurrency) {
                    costItem.costInLocalCurrency = costItem.costInBillingCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['valueMissing','valueInvalid'])
                    mappingErrorBag.valueCalculated = true
                }
                else if(!costItem.costInBillingCurrency && costItem.costInLocalCurrency) {
                    costItem.costInBillingCurrency = costItem.costInLocalCurrency * costItem.currencyRate
                    mappingErrorBag.keySet().removeAll(['invoiceTotalMissing','invoiceTotalInvalid'])
                    mappingErrorBag.invoiceTotalCalculated = true
                }
            }
            /*
            taxCode(nullable: true, blank: false) ---v
            taxRate(nullable: true, blank: false) ---v
            taxKey(nullable: true, blank: false) -> to combination of tax type and tax rate
             */
            if(colMap.taxType != null && colMap.taxRate != null) {
                String taxTypeKey = cols[colMap.taxType]
                int taxRate
                try {
                    taxRate = Integer.parseInt(cols[colMap.taxRate])
                }
                catch (Exception e) {
                    log.error(e.toString())
                    taxRate = -1
                }
                if(!taxTypeKey || taxRate == -1)
                    mappingErrorBag.invalidTaxType = true
                else {
                    CostItem.TAX_TYPES taxKey
                    if(taxRate == 7)
                        taxKey = CostItem.TAX_TYPES.TAXABLE_7
                    else if(taxRate == 19)
                        taxKey = CostItem.TAX_TYPES.TAXABLE_19
                    else if(taxRate == 0) {
                        RefdataValue taxType = RefdataValue.getByValueAndCategory(taxTypeKey, TAX_TYPE)
                        if(!taxType)
                            taxType = RefdataValue.getByCategoryDescAndI10nValueDe(TAX_TYPE, taxTypeKey)
                        switch(taxType) {
                            case RefdataValue.getByValueAndCategory('not taxable', TAX_TYPE): taxKey = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                                break
                            case RefdataValue.getByValueAndCategory('not applicable', TAX_TYPE): taxKey = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                                break
                            case RefdataValue.getByValueAndCategory('taxable tax-exempt', TAX_TYPE): taxKey = CostItem.TAX_TYPES.TAX_EXEMPT
                                break
                            default: mappingErrorBag.invalidTaxType = true
                                break
                        }
                    }
                    if(taxKey)
                        costItem.taxKey = taxKey
                    else
                        mappingErrorBag.invalidTaxType = true
                }
            }
            //invoiceDate(nullable: true, blank: false) -> to invoice date
            if(colMap.invoiceDate != null) {
                Date invoiceDate = DateUtil.parseDateGeneric(cols[colMap.invoiceDate])
                if(invoiceDate)
                    costItem.invoiceDate = invoiceDate
            }
            //financialYear(nullable: true, blank: false) -> to financial year
            if(colMap.financialYear != null) {
                if(cols[colMap.financialYear])  {
                    try {
                        Year financialYear = Year.parse(cols[colMap.financialYear])
                        costItem.financialYear = financialYear
                    }
                    catch(Exception e) {
                        mappingErrorBag.invalidYearFormat = true
                    }
                }
            }
            //costItemStatus(nullable: true, blank: false) -> to status
            if(colMap.status != null) {
                String statusKey = cols[colMap.status]
                if(statusKey) {
                    RefdataValue status = RefdataValue.getByValueAndCategory(statusKey, COST_ITEM_STATUS)
                    if(!status)
                        status = RefdataValue.getByCategoryDescAndI10nValueDe(COST_ITEM_STATUS, statusKey)
                    if(!status)
                        mappingErrorBag.noValidStatus = statusKey
                    costItem.costItemStatus = status
                }
            }
            //costItemElement(nullable: true, blank: false) -> to element
            if(colMap.element != null) {
                String elementKey = cols[colMap.element]
                if(elementKey) {
                    RefdataValue element = RefdataValue.getByValueAndCategory(elementKey, COST_ITEM_ELEMENT)
                    if(!element)
                        element = RefdataValue.getByCategoryDescAndI10nValueDe(COST_ITEM_ELEMENT,elementKey)
                    if(!element)
                        mappingErrorBag.noValidElement = elementKey
                    costItem.costItemElement = element
                }
            }
            //costItemElementConfiguration(nullable: true, blank: false) -> to cost item sign
            if(colMap.costItemSign != null) {
                String elementSign = cols[colMap.costItemSign]
                if(elementSign) {
                    RefdataValue ciec = RefdataValue.getByValueAndCategory(elementSign, COST_CONFIGURATION)
                    if(!ciec)
                        ciec = RefdataValue.getByCategoryDescAndI10nValueDe(COST_CONFIGURATION, elementSign)
                    if(!ciec)
                        mappingErrorBag.noValidSign = elementSign
                    costItem.costItemElementConfiguration = ciec
                }
            }
            //reference(nullable: true, blank: false) -> to reference/codes
            if(colMap.reference != null)
                costItem.reference = cols[colMap.reference]
            //budgetCode -> to budget code
            if(colMap.budgetCode != null) {
                budgetCodes[r] = cols[colMap.budgetCode]?.trim()
            }
            //startDate(nullable: true, blank: false) -> to date from
            if(colMap.dateFrom != null) {
                Date startDate = DateUtil.parseDateGeneric(cols[colMap.dateFrom])
                if(startDate)
                    costItem.startDate = startDate
            }
            //endDate(nullable: true, blank: false) -> to date to
            if(colMap.dateTo != null) {
                Date endDate = DateUtil.parseDateGeneric(cols[colMap.dateTo])
                if(endDate)
                    costItem.endDate = endDate
            }
            //isVisibleForSubscriber(nullable: true, blank: false) -> in second configuration step, see ticket #1204
            //costItem.save() MUST NOT be executed here, ONLY AFTER postprocessing!
            candidates.put(costItem,mappingErrorBag)
        }
        result.candidates = candidates
        result.budgetCodes = budgetCodes
        result
    }

    List<Map<String,Object>> orderedCurrency() {
        //def all_currencies = RefdataValue.executeQuery('select rdv from RefdataValue as rdv where rdv.owner.desc=?', 'Currency')
        // restrict only to new refdata values
        // created in bootstrap.groovy with e.g. [key:'EUR']
        // TODO: migrate old values to new ones
        Set<RefdataValue> allCurrencies = []
        List<String> staticOrder = grails.util.Holders.config?.financials?.currency?.split("[|]")

        staticOrder.each { String important ->
            allCurrencies << RefdataValue.getByValueAndCategory(important,CURRENCY)
        }
        allCurrencies.addAll(RefdataCategory.getAllRefdataValues(CURRENCY))

        List<Map<String,Object>> result = [[id:0,text:messageSource.getMessage('financials.currency.none',null, LocaleContextHolder.getLocale())]] //is only provisorical, TODO [ticket=2107]
        result.addAll(allCurrencies.collect { rdv ->
            [id: rdv.id, text: rdv.getI10n('value')]
        })

        result
    }

    /**
     * This method replaces the view (!!) _vars.gsp.
     * @return basic parameters for manipulating cost items
     */
    Map<String,Object> setEditVars(Org org) {
        [costItemStatus:RefdataCategory.getAllRefdataValues(COST_ITEM_STATUS)-COST_ITEM_DELETED,
         costItemCategory:RefdataCategory.getAllRefdataValues(COST_ITEM_CATEGORY),
         costItemSigns:RefdataCategory.getAllRefdataValues(COST_CONFIGURATION),
         costItemElements:CostItemElementConfiguration.findAllByForOrganisation(org),
         taxType:RefdataCategory.getAllRefdataValues(TAX_TYPE),
         yn:RefdataCategory.getAllRefdataValues(Y_N),
         budgetCodes:BudgetCode.findAllByOwner(org),
         currency:orderedCurrency()]
    }

    //ex SubscriptionDetailsController
    Map<String,Object> setResultGenerics(GrailsParameterMap params) throws FinancialDataException {
        Map<String,Object> result = [user: User.get(springSecurityService.principal.id),
                                     offsets:[collOffset:0,consOffset:0,subscrOffset:0,ownOffset:0],
                                     sortConfig:[collSort:'sortname',collOrder:'asc',
                                                 consSort:'sortname',consOrder:'asc',
                                                 subscrSort:'sub.name',subscrOrder:'asc',
                                                 ownSort:'ci.costTitle',ownOrder:'asc'],
                                     institution:contextService.getOrg(),
                                     editConf:[:]]
        if(params.forExport) {
            result.max = 1000000
        }
        else {
            result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP().toInteger()
        }
        if(!(result.user instanceof User))
            throw new FinancialDataException("Context user not loaded successfully!")
        if(!(result.institution instanceof Org))
            throw new FinancialDataException("Context org not loaded successfully!")
        if(params.sub || params.id) {
            String subId
            if(params.sub)
                subId = params.sub
            else if(params.id)
                subId = params.id
            result.subscription = Subscription.get(subId)
            if(!(result.subscription instanceof Subscription))
                throw new FinancialDataException("Invalid or no subscription found!")
            else result.editable = result.subscription.isEditableBy(result.user)
        }
        else
            result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')
        /*
            Decision tree
            We may see this view from the perspective of:
            1. consortia: parent subscription (show own and consortial tabs) (level 1)
            2. consortia: child subscription (show consortial tab) (level 2)
            3. consortia: child subscription preview (show subscriber tab) (level 2)
            4. collective user: own subscription (show own and collective tabs)
            5. collective user: child subscription of own (show collective tab) (level 2)
            6. collective user: child subscription preview of own (show subscriber tab) (level 2)
            7. collective user: child subscription of consortia (show own, collective and subscriber tabs) (level 2)
            8. collective user: child subscription of child (show collective tab) (level 3)
            9. collective user: child subscription preview of child (show subscriber tab) (level 3)
            10. single user: own subscription (show own tab) (level 1)
            11. single user: child subscription (show own and subscriber tab) (level 2)
            12. basic member or department: child subscription (show subscriber tab) (level 2 or 3)
         */
        List<String> dataToDisplay = []
        //Determine own org belonging, then, in which relationship I am to the given subscription instance
        switch(result.institution.getCustomerType()) {
        //cases one to three
            case 'ORG_CONSORTIUM':
            case 'ORG_CONSORTIUM_SURVEY':
                if(result.subscription) {
                    //cases two and three: child subscription
                    if(result.subscription.instanceOf) {
                        //case three: child subscription preview
                        if(params.orgBasicMemberView) {
                            dataToDisplay << 'subscr'
                            result.showView = 'subscr'
                        }
                        //case two: child subscription, consortial view
                        else {
                            dataToDisplay << 'consAtSubscr'
                            result.showView = 'cons'
                            result.editConf.showVisibilitySettings = true
                            result.showConsortiaFunctions = true
                            result.sortConfig.consSort = 'ci.costTitle'
                            result.subMemberLabel = messageSource.getMessage('consortium.subscriber',null,LocaleContextHolder.getLocale())
                            result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]).collect { row -> row[0]}
                            result.editConf.showVisibilitySettings = true
                        }
                    }
                    //case one: parent subscription
                    else {
                        dataToDisplay.addAll(['own','cons'])
                        result.showView = 'cons'
                        result.showConsortiaFunctions = true
                        result.subMemberLabel = messageSource.getMessage('consortium.subscriber',null,LocaleContextHolder.getLocale())
                        result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]).collect { row -> row[0]}
                        result.editConf.showVisibilitySettings = true
                    }
                }
                //case one for all subscriptions
                else {
                    dataToDisplay.addAll(['own','cons'])
                    result.showView = 'cons'
                    result.showConsortiaFunctions = true
                    result.editConf.showVisibilitySettings = true
                }
                break
        //cases four to nine
            case 'ORG_INST_COLLECTIVE':
                if(result.subscription) {
                    //cases five to nine: subscription has a parent
                    if(result.subscription.instanceOf) {
                        //case five, six, eight and nine: child of local subscription or of consortial subscription, department level
                        if(result.subscription.getCalculatedType() == CALCULATED_TYPE_PARTICIPATION){
                            //cases six and nine: department subscription preview
                            if(params.orgBasicMemberView) {
                                dataToDisplay << 'subscr'
                                result.showView = 'subscr'
                            }
                            //cases five and eight: department subscription
                            else {
                                dataToDisplay << 'collAtSubscr'
                                result.showView = 'coll'
                                result.sortConfig.collSort = 'ci.costTitle'
                                result.showCollectiveFunctions = true
                                result.editConf.showVisibilitySettings = true
                            }
                        }
                        //case seven: child of consortial subscription, collective level
                        else if(result.subscription.getCalculatedType() == CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE){
                            dataToDisplay.addAll(['own','subscr','coll'])
                            result.showView = 'subscr'
                            result.showCollectiveFunctions = true
                            result.subMemberLabel = messageSource.getMessage('collective.member',null,LocaleContextHolder.getLocale())
                            result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[OR_SUBSCRIBER_COLLECTIVE]]).collect { row -> row[0]}
                            result.editConf.showVisibilitySettings = true
                        }
                    }
                    //case four: local parent subscription
                    else {
                        dataToDisplay.addAll(['own','coll'])
                        result.showView = 'coll'
                        result.showCollectiveFunctions = true
                        result.subMemberLabel = messageSource.getMessage('collective.member',null,LocaleContextHolder.getLocale())
                        result.subMembers = Subscription.executeQuery('select s, oo.org.sortname as sortname from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscrRoles order by sortname asc',[parent:result.subscription,subscrRoles:[OR_SUBSCRIBER_COLLECTIVE]]).collect { row -> row[0]}
                        result.editConf.showVisibilitySettings = true
                    }
                }
                //case seven summing up everything what the collective user may have subscribed
                else {
                    dataToDisplay.addAll(['own','subscr','coll'])
                    result.showView = 'subscr'
                    result.showCollectiveFunctions = true
                    result.editConf.showVisibilitySettings = true
                }
                break
        //cases ten and eleven
            case 'ORG_INST':
                if(result.subscription) {
                    //case eleven: child subscription
                    if(result.subscription.instanceOf) {
                        dataToDisplay.addAll(['own','subscr'])
                        result.showView = 'subscr'
                    }
                    //case ten: local subscription
                    else {
                        dataToDisplay << 'own'
                        result.showView = 'own'
                    }
                }
                //case eleven for all subscriptions
                else {
                    dataToDisplay.addAll(['own','subscr'])
                    result.showView = 'subscr'
                }
                break
        //cases twelve: basic member
            case 'ORG_BASIC_MEMBER':
                dataToDisplay << 'subscr'
                result.showView = 'subscr'
                break
        }
        result.dataToDisplay = dataToDisplay
        //override default view to show if checked by pagination or from elsewhere
        if(params.showView){
            result.showView = params.showView
            if(params.offset && !params.forExport)
                result.offsets["${params.showView}Offset"] = Integer.parseInt(params.offset)
        }

        result
    }

    Map<String,Object> setAdditionalGenericEditResults(Map configMap) {
        Map<String,Object> result = setEditVars(configMap.institution)
        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }
        result.orgConfigurations = orgConfigurations as JSON
        if(configMap.showView in ["cons","coll"] && configMap.subMembers) {
            result.validSubChilds = [[id: 'forParent', label: messageSource.getMessage('financials.newCosts.forParentSubscription', null, LocaleContextHolder.getLocale())], [id: 'forAllSubscribers', label: configMap.licenseeTargetLabel]]
            result.validSubChilds.addAll(configMap.subMembers)
        }
        result
    }

}
