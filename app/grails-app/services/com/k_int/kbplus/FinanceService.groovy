package com.k_int.kbplus

import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

/**
 * This service will subsequently replace the very complicatedly written methods in the FinanceController class.
 *
 * @author agalffy
 */
@Transactional
class FinanceService {

    def contextService
    def controlledListService
    def genericOIDService
    def messageSource

    /**
     * Will replace the methods index and financialData methods in FinanceController class for a single subscription.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @param subscription - the subscription for which the financial data is retrieved. Its type determines the views displayed on return.
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItemsForSubscription(Subscription sub,GrailsParameterMap params,long max,int offset) {
        Org org = contextService.org
        LinkedHashMap result = [own:[:],cons:[:],subscr:[:]]
        int ownOffset = 0
        int consOffset = 0
        int subscrOffset = 0
        switch(params.view) {
            case "own": ownOffset = offset
                if(params.max) max = Long.parseLong(params.max)
                break
            case "cons":
            case "consAtSubscr": consOffset = offset
                if(params.max) max =  Long.parseLong(params.max)
                break
            case "subscr": subscrOffset = offset
                if(params.max) max = Long.parseLong(params.max)
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        List filterOwnQuery = processFilterParams(params,"own",true)
        List filterConsQuery = processFilterParams(params,"cons",true)
        List filterSubscrQuery = processFilterParams(params,"subscr",true)
        result.filterPresets = filterConsQuery[1]
        List ownCostItems = CostItem.executeQuery('select ci from CostItem ci where ci.owner = :owner and ci.sub = :sub '+filterOwnQuery[0],[owner:org,sub:sub]+filterOwnQuery[1])
        result.own.costItems = []
        long limit = ownOffset+max
        if(limit > ownCostItems.size())
            limit = ownCostItems.size()
        for(int i = ownOffset;i < limit;i++) {
            result.own.costItems.add(ownCostItems[i])
        }
        result.own.count = ownCostItems.size()
        if(result.own.count > 0) {
            result.own.sums = calculateResults(ownCostItems)
        }
        switch(sub.getCalculatedType()) {
        /*
            own costs, consortial costs
            that is: a) owner = contextOrg and sub = contextSub
            b) owner = contextOrg (which is consortium) and sub.instanceOf = contextSub
         */
            case TemplateSupport.CALCULATED_TYPE_CONSORTIAL:
                List consCostItems = CostItem.executeQuery("select ci, (select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType.value = 'Subscriber_Consortial') as sortname from CostItem as ci where ci.owner = :owner and ci.sub in (select s from Subscription as s join s.orgRelations orgRoles where s.instanceOf = :sub "+filterConsQuery[0]+" order by sortname asc",[owner:org,sub:sub]+filterConsQuery[1])
                result.cons.costItems = []
                limit = consOffset+max
                if(limit > consCostItems.size())
                    limit = consCostItems.size()
                for(int i = consOffset;i < limit;i++) {
                    result.cons.costItems.add(consCostItems[i][0])
                }
                result.cons.count = consCostItems.size()
                if(result.cons.count > 0){
                    result.cons.sums = calculateResults(consCostItems.collect { row -> row[0]})
                }
                break
        /*
            own costs, consortial participation costs
            that is: a) owner = contextOrg and sub = contextSub
            b) owner = consortium and sub = contextSub and visibleForSubscriber
         */
            case TemplateSupport.CALCULATED_TYPE_PARTICIPATION:
                Org subscrCons = Org.executeQuery("select o.org from OrgRole as o where o.sub = :sub and o.roleType = :cons",[sub:sub,cons: RDStore.OR_SUBSCRIPTION_CONSORTIA]).get(0)
                String visibility = ""
                if(!subscrCons.equals(org))
                    visibility = " and ci.isVisibleForSubscriber = true"
                List subscrCostItems = CostItem.executeQuery('select ci from CostItem as ci where ci.owner = :owner and ci.sub = :sub'+visibility+filterSubscrQuery[0],[owner:subscrCons,sub:sub]+filterSubscrQuery[1])
                List costItems = []
                limit = subscrOffset+max
                if(limit > subscrCostItems.size())
                    limit = subscrCostItems.size()
                for(int i = subscrOffset;i < limit;i++) {
                    costItems.add(subscrCostItems[i])
                }
                int count = subscrCostItems.size()
                Map sums = [:]
                if(count > 0) {
                    sums = calculateResults(subscrCostItems)
                }
                if(params.view.equals("subscr") || !params.view) {
                    result.subscr.costItems = costItems
                    result.subscr.count = count
                    result.subscr.sums = sums
                }
                else if(params.view.equals("consAtSubscr")) {
                    result.cons.costItems = costItems
                    result.cons.count = count
                    result.cons.sums = sums
                }
                break
        }
        if(!params.forExport) {
            List<CostItem> allCostItems = CostItem.findAllByOwnerAndSub(org,sub)
            if(allCostItems.size() > 0) {
                result.filterLists = assembleFilterLists()
                result.filterLists.providers = OrgRole.executeQuery('select distinct o.org, o.org.name from OrgRole o where o.roleType = :provider and o.sub = :sub order by o.org.name asc',[sub:sub,provider:RDStore.OR_PROVIDER]).collect{ it -> it[0] }
            }
        }
        result
    }

    /**
     * Will replace the methods index and financialData methods in FinanceController class for the institution-wide overview.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItems(GrailsParameterMap params,long max) {
        LinkedHashMap result = [own:[:],cons:[:],subscr:[:]]
        Org org = contextService.org
        List filterQueryOwn = processFilterParams(params,"own",false)
        List filterQueryCons = processFilterParams(params,"cons",false)
        List filterQuerySubscr = processFilterParams(params,"subscr",false)
        int ownOffset = 0
        int consOffset = 0
        int subscrOffset = 0
        switch(params.view) {
            case "own": ownOffset = params.offset ? Integer.parseInt(params.offset) : 0
                break
            case "cons": consOffset = params.offset ? Integer.parseInt(params.offset) : 0
                break
            case "subscr": subscrOffset = params.offset ? Integer.parseInt(params.offset) : 0
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        //get own costs
        List<CostItem> ownSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles ' +
                'where ci.owner = :org and orgRoles.org = :org and orgRoles.roleType = :consType and sub.instanceOf = null and sub.status != :deleted'+filterQueryOwn[0]+' order by sub.name asc',
                [org:org,consType:RDStore.OR_SUBSCRIPTION_CONSORTIA,deleted:RDStore.SUBSCRIPTION_DELETED]+filterQueryOwn[1])
        ownSubscriptionCostItems.addAll(CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles where ' +
                'ci.owner = :org and orgRoles.org = :org and orgRoles.roleType in :nonConsTypes and sub.status != :deleted'+filterQueryOwn[0]+' order by sub.name asc',
                [org:org,nonConsTypes:[RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS],deleted:RDStore.SUBSCRIPTION_DELETED]+filterQueryOwn[1]))
        ownSubscriptionCostItems.addAll(CostItem.executeQuery('select ci from CostItem ci where ci.owner = :org and ci.sub is null',[org:org]))
        result.own.costItems = []
        long limit = ownOffset+max
        if(limit > ownSubscriptionCostItems.size())
            limit = ownSubscriptionCostItems.size()
        for(int i = ownOffset;i < limit;i++) {
            result.own.costItems.add(ownSubscriptionCostItems[i])
        }
        result.own.count = ownSubscriptionCostItems.size()
        if(result.own.count > 0) {
            result.own.sums = calculateResults(ownSubscriptionCostItems)
        }
        //get consortial costs
        List<CostItem> consortialSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci ' +
                'join ci.owner orgC ' +
                'join ci.sub sub ' +
                'join sub.instanceOf subC ' +
                'join subC.orgRelations roleC ' +
                'join sub.orgRelations roleMC ' +
                'join sub.orgRelations orgRoles ' +
                'where orgC = :org and orgC = roleC.org and roleMC.roleType = :consortialType and orgRoles.roleType = :subscrType and subC.status != :statusC and sub.status != :statusM' +
                filterQueryCons[0] + ' order by orgRoles.org.sortname asc ',
                [org:org,consortialType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrType:RDStore.OR_SUBSCRIBER_CONS,statusC:RDStore.SUBSCRIPTION_DELETED,statusM:RDStore.SUBSCRIPTION_DELETED]+filterQueryCons[1])
        result.cons.costItems = []
        limit = consOffset+max
        if(limit > consortialSubscriptionCostItems.size())
            limit = consortialSubscriptionCostItems.size()
        for(int i = consOffset;i < limit;i++){
            result.cons.costItems.add(consortialSubscriptionCostItems[i])
        }
        result.cons.count = consortialSubscriptionCostItems.size()
        if(result.cons.count > 0) {
            result.cons.sums = calculateResults(consortialSubscriptionCostItems)
        }
        //get membership costs
        List<CostItem> consortialMemberSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci '+
                'join ci.sub sub ' +
                'join sub.instanceOf subC ' +
                'join subC.orgRelations roleC ' +
                'join sub.orgRelations orgRoles ' +
                'join ci.owner orgC ' +
                'where orgC = roleC.org and roleC.roleType = :consType and orgRoles.org = :org and orgRoles.roleType = :subscrType and ci.isVisibleForSubscriber = true'+
                filterQuerySubscr[0] + ' order by sub.name asc',
                [org:org,consType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrType:RDStore.OR_SUBSCRIBER_CONS]+filterQuerySubscr[1])
        result.subscr.costItems = []
        limit = subscrOffset+max
        if(limit > consortialMemberSubscriptionCostItems.size())
            limit = consortialMemberSubscriptionCostItems.size()
        for(int i = subscrOffset;i < limit;i++) {
            result.subscr.costItems.add(consortialMemberSubscriptionCostItems[i])
        }
        result.subscr.count = consortialMemberSubscriptionCostItems.size()
        if(result.subscr.count > 0) {
            result.subscr.sums = calculateResults(consortialMemberSubscriptionCostItems)
        }
        if(!params.forExport) {
            List<CostItem> allCostItems = CostItem.findAllByOwner(org)
            if(allCostItems.size() > 0) {
                result.filterLists = assembleFilterLists()
                result.filterLists.providers = OrgRole.executeQuery('select distinct o.org, o.org.name from OrgRole o where o.roleType = :provider order by o.org.name asc',[provider:RDStore.OR_PROVIDER]).collect{ it -> it[0] }
            }
            result.filterPresets = filterQueryCons[1]
        }
        result
    }

    /**
     * Processes the given parameters to build a query part which will be included into the base query strings
     *
     * @param params - a GrailsParameterMap containing parameters to be processed
     * @return an array with the filter string on position 0 and the filter parameter map on position 1
     */
    List processFilterParams(GrailsParameterMap params,String filterView,boolean forSingleSubscription) {
        String filterQuery = ""
        LinkedHashMap queryParams = [:]
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
        //subscription filter settings
        //subscription members
        if(params.filterSubMembers && filterView.equals("cons")) {
            filterQuery += " and orgRoles.org in (:filterSubMembers) "
            List<Org> filterSubMembers = []
            String[] subMembers = params.list("filterSubMembers")
            subMembers.each { subMember ->
                filterSubMembers.add(Org.get(Long.parseLong(subMember)))
            }
            queryParams.filterSubMembers = filterSubMembers
            log.info(queryParams.filterSubMembers)
        }
        //providers
        if(params.filterSubProviders) {
            filterQuery += " and sub in (select oo.sub from OrgRole as oo where oo.org in (:filterSubProviders)) "
            List<Org> filterSubProviders = []
            String[] subProviders = params.list("filterSubProviders")
            subProviders.each { subProvider ->
                filterSubProviders.add(Org.get(Long.parseLong(subProvider)))
            }
            queryParams.filterSubProviders = filterSubProviders
            log.info(queryParams.filterSubProviders)
        }
        //subscription status
        if(params.filterSubStatus) {
            if(params.filterSubStatus == RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null','filter.fake.values')) {
                filterQuery += " and sub.status is null "
            }
            else {
                filterQuery += " and sub.status = :filterSubStatus "
                queryParams.filterSubStatus = RefdataValue.get(Long.parseLong(params.filterSubStatus))
            }
            log.info(queryParams.filterSubStatus)
        }
        else if(!params.submit && !forSingleSubscription) {
            filterQuery += " and sub.status = :filterSubStatus "
            queryParams.filterSubStatus = RDStore.SUBSCRIPTION_CURRENT
            params.filterSubStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
        }
        //the bracket from the subquery has to be closed when in subscription mode and for single subscription
        if(filterView.equals("cons") && forSingleSubscription) {
            filterQuery += ") "
        }
        //cost item filter settings
        //cost item title
        if(params.filterCITitle) {
            filterQuery += " and ci.costTitle like :filterCITitle or ci.costTitle like :ciTitleLowerCase "
            queryParams.filterCITitle = "%${params.filterCITitle}%"
            queryParams.ciTitleLowerCase = "%${params.filterCITitle.toLowerCase()}%"
            log.info(queryParams.filterCITitle)
        }
        //cost item subscription
        if(params.filterCISub) {
            filterQuery += " and sub in (:filterCISub) "
            List<Subscription> filterSubs = []
            String[] subscriptions = params.list("filterCISub")
            subscriptions.each { sub ->
                filterSubs.add((Subscription) genericOIDService.resolveOID(sub))
            }
            queryParams.filterCISub = filterSubs
            log.info(queryParams.filterCISub)
        }
        //subscription package
        if(params.filterCISPkg) {
            filterQuery += " and sub in (select subscription from SubscriptionPackage where pkg in (:filterCISPkg)) "
            List<SubscriptionPackage> filterSubPackages = []
            String[] subscriptionPackages = params.list("filterCISPkg")
            subscriptionPackages.each { subPkg ->
                filterSubPackages.add((SubscriptionPackage) genericOIDService.resolveOID(subPkg))
            }
            queryParams.filterCISPkg = filterSubPackages
            log.info(queryParams.filterCISPkg)
        }
        //budget code
        if(params.filterCIBudgetCode) {
            filterQuery += " and ci in (select cig.costItem from CostItemGroup cig where cig.budgetCode in (:filterCIBudgetCode)) "
            List<BudgetCode> filterBudgetCodes = []
            String[] budgetCodes = params.list("filterCIBudgetCode")
            budgetCodes.each { bc ->
                filterBudgetCodes.add(BudgetCode.get(Long.parseLong(bc)))
            }
            queryParams.filterCIBudgetCode = filterBudgetCodes
            log.info(queryParams.filterCIBudgetCode)
        }
        //invoice number
        if(params.filterCIInvoiceNumber) {
            filterQuery += " and ci.invoice.invoiceNumber in (:filterCIInvoiceNumber) "
            List<String> filterInvoiceNumbers = []
            String[] invoiceNumbers = params.list("filterCIInvoiceNumber")
            invoiceNumbers.each { invNum ->
                filterInvoiceNumbers.add(invNum)
            }
            queryParams.filterCIInvoiceNumber = filterInvoiceNumbers
            log.info(queryParams.filterCIInvoiceNumber)
        }
        //order number
        if(params.filterCIOrderNumber) {
            filterQuery += " and ci.order.orderNumber in (:filterCIOrderNumber) "
            List<String> filterOrderNumbers = []
            String[] orderNumbers = params.list("filterCIOrderNumber")
            orderNumbers.each { orderNum ->
                filterOrderNumbers.add(orderNum)
            }
            queryParams.filterCIOrderNumber = filterOrderNumbers
            log.info(queryParams.filterCIOrderNumber)
        }
        //cost item element
        if(params.filterCIElement) {
            filterQuery += " and ci.costItemElement in (:filterCIElement) "
            List<RefdataValue> filterElements = []
            String[] costItemElements = params.list("filterCIElement")
            costItemElements.each { cie ->
                filterElements.add(genericOIDService.resolveOID(cie))
            }
            queryParams.filterCIElement = filterElements
            log.info(queryParams.filterCIElement)
        }
        //cost item status
        if(params.filterCIStatus) {
            filterQuery += " and ci.costItemStatus in (:filterCIStatus) "
            List<RefdataValue> filterStatus = []
            String[] costItemStatus = params.list("filterCIStatus")
            costItemStatus.each { cis ->
                filterStatus.add(genericOIDService.resolveOID(cis))
            }
            queryParams.filterCIStatus = filterStatus
            log.info(queryParams.filterCIStatus)
        }
        //tax type
        if(params.filterCITaxType) {
            filterQuery += " and ci.taxCode in (:filterCITaxType) "
            List<RefdataValue> filterTaxType = []
            String[] taxTypes = params.list("filterCITaxType")
            taxTypes.each { taxType ->
                filterTaxType.add(genericOIDService.resolveOID(taxType))
            }
            queryParams.filterCITaxType = filterTaxType
            log.info(params.filterCITaxType)
        }
        //valid on
        if(params.filterCIValidOn) {
            filterQuery += " and (ci.startDate <= :filterCIValidOn OR ci.startDate is null) and (ci.endDate >= :filterCIValidOn OR ci.endDate is null) "
            Date validOn = sdf.parse(params.filterCIValidOn)
            queryParams.filterCIValidOn = validOn
            log.info(queryParams.filterCIValidOn)
        }
        //invoice from
        if(params.filterCIInvoiceFrom) {
            filterQuery += " and (ci.invoiceDate >= :filterCIInvoiceFrom AND ci.invoiceDate is not null) "
            Date invoiceFrom = sdf.parse(params.filterCIInvoiceFrom)
            queryParams.filterCIInvoiceFrom = invoiceFrom
            log.info(queryParams.filterCIInvoiceFrom)
        }
        //invoice to
        if(params.filterCIInvoiceTo) {
            filterQuery += " and (ci.invoiceDate <= :filterCIInvoiceTo AND ci.invoiceDate is not null) "
            Date invoiceTo = sdf.parse(params.filterCIInvoiceTo)
            queryParams.filterCIInvoiceTo = invoiceTo
            log.info(queryParams.filterCIInvoiceTo)
        }
        //paid from
        if(params.filterCIPaidFrom) {
            filterQuery += " and (ci.datePaid >= :filterCIPaidFrom AND ci.datePaid is not null) "
            Date invoiceFrom = sdf.parse(params.filterCIPaidFrom)
            queryParams.filterCIPaidFrom = invoiceFrom
            log.info(queryParams.filterCIPaidFrom)
        }
        //paid to
        if(params.filterCIPaidTo) {
            filterQuery += " and (ci.datePaid <= :filterCIPaidTo AND ci.datePaid is not null) "
            Date invoiceTo = sdf.parse(params.filterCIPaidTo)
            queryParams.filterCIPaidTo = invoiceTo
            log.info(queryParams.filterCIPaidTo)
        }
        return [filterQuery,queryParams]
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
    Map calculateResults(List<CostItem> costItems) {
        List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * ((ci.taxRate/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value",[costItems:costItems])
        List<Map> billingSumsNegative = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * ((ci.taxRate/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative' group by ci.billingCurrency.value",[costItems:costItems])
        Map localSumsPositive = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * ((ci.taxRate / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive'",[costItems:costItems]).get(0)
        Map localSumsNegative = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * ((ci.taxRate / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative'",[costItems:costItems]).get(0)
        List<Map> billingSums = []
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
                billingSums.add([currency: posEntry.currency, billingSum: billingSum, billingSumAfterTax: billingSumAfterTax])
            }
        }
        else if(billingSumsNegative.size() > 0) {
            billingSumsNegative.each { negEntry ->
                billingSums.add([currency: negEntry.currency, billingSum: negEntry.billingSum, billingSumAfterTax: negEntry.billingSumAfterTax])
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
            localSums.localSum = localSumsNegative.localSum
            localSums.localSumAfterTax = localSumsNegative.localSumAfterTax
        }
        else {
            localSums.localSum = 0.0
            localSums.localSumAfterTax = 0.0
        }
        return [billingSums:billingSums,localSums:localSums]
    }

    int getCurrencyIndexInList(List entryList,String currency) {
        int ret = -1
        entryList.eachWithIndex { negEntry, int i ->
            if(currency.equals(negEntry.currency)) {
                ret = i
            }
        }
        return ret
    }

    Map assembleFilterLists() {
        log.info("assembling filter lists")
        Org org = contextService.getOrg()
        List budgetCodes = BudgetCode.executeQuery("select bc from BudgetCode bc where owner = :org",[org:org])
        List invoiceNumbers = Invoice.executeQuery("select i.invoiceNumber from Invoice i where i.owner = :org",[org:org])
        List orderNumbers = Order.executeQuery("select ord.orderNumber from Order ord where ord.owner = :org",[org:org])
        return [subscriptions: controlledListService.getSubscriptions([status:RDStore.SUBSCRIPTION_CURRENT]).values,
                subPackages: controlledListService.getSubscriptionPackages([status:RDStore.SUBSCRIPTION_CURRENT]).values,
                budgetCodes: budgetCodes,invoiceNumbers: invoiceNumbers,orderNumbers: orderNumbers]
    }

}
