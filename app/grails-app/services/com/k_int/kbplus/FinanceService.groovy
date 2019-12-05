package com.k_int.kbplus

import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Year
import static de.laser.helper.RDStore.*
import static de.laser.interfaces.TemplateSupport.*

/**
 * This service will subsequently replace the very complicatedly written methods in the FinanceController class.
 *
 * @author agalffy
 */
@Transactional
class FinanceService {

    def contextService
    def genericOIDService
    def messageSource
    def accessService
    def escapeService

    /**
     * Will replace the methods index and financialData methods in FinanceController class for a single subscription.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @param subscription - the subscription for which the financial data is retrieved. Its type determines the views displayed on return.
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItemsForSubscription(Subscription sub,params,int max,int offset) {
        Org org = contextService.org
        LinkedHashMap result = [own:[:],cons:[:],coll:[:],subscr:[:]]
        int ownOffset = 0
        int consOffset = 0
        int subscrOffset = 0
        switch(params.view) {
            case "own": ownOffset = offset
                if(params.max) max = Integer.parseInt(params.max)
                break
            case "cons":
            case "coll":
            case "consAtSubscr": consOffset = offset
                if(params.max) max =  Integer.parseInt(params.max)
                break
            case "subscr": subscrOffset = offset
                if(params.max) max = Integer.parseInt(params.max)
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        List filterOwnQuery = processFilterParams(params,"own",true)
        List filterConsQuery = processFilterParams(params,"cons",true)
        List filterSubscrQuery = processFilterParams(params,"subscr",true)
        result.filterPresets = filterConsQuery[1]
        String ownSort
        if(params.ownSort) {
            ownSort = " order by ${params.sort} ${params.order}"
        }
        else {
            ownSort = ""
        }
        List ownCostItems = CostItem.executeQuery('select ci from CostItem ci where ci.owner = :owner and ci.surveyOrg = null and ci.sub = :sub '+filterOwnQuery[0]+ownSort,[owner:org,sub:sub]+filterOwnQuery[1])
        result.own.costItems = []
        int limit = ownOffset+max
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
            case CALCULATED_TYPE_CONSORTIAL:
            case CALCULATED_TYPE_ADMINISTRATIVE:
            case CALCULATED_TYPE_COLLECTIVE:
                String consSort
                if(params.consSort)
                    consSort = " order by ${params.sort} ${params.order}"
                else
                    consSort = " order by sortname asc "
                List consCostItems = CostItem.executeQuery("select ci, (select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType in (:roleTypes)) as sortname from CostItem as ci where ci.owner = :owner and ci.surveyOrg = null and ci.sub in (select s from Subscription as s join s.orgRelations orgRoles where s.instanceOf = :sub "+filterConsQuery[0]+consSort,[owner:org,sub:sub,roleTypes:[OR_SUBSCRIBER_COLLECTIVE,OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]+filterConsQuery[1])
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
                if(accessService.checkPerm("ORG_INST_COLLECTIVE"))
                    result.coll = result.cons
                break
        /*
            own costs, consortial participation costs
            that is: a) owner = contextOrg and sub = contextSub
            b) owner = consortium and sub = contextSub and visibleForSubscriber
         */
            case CALCULATED_TYPE_PARTICIPATION:
                String visibility = ""
                if(!(org.id in [sub.getConsortia()?.id,sub.getCollective()?.id])) {
                    visibility = " and ci.isVisibleForSubscriber = true"
                }
                String subscrSort
                if(params.subscrSort)
                    subscrSort = " order by ${params.sort} ${params.order}"
                else
                    subscrSort = ""
                List subscrCostItems = CostItem.executeQuery('select ci from CostItem as ci where ci.owner in :owner and ci.surveyOrg = null and ci.sub = :sub'+visibility+filterSubscrQuery[0]+subscrSort,[owner:[sub.getConsortia(),sub.getCollective()],sub:sub]+filterSubscrQuery[1])
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
            case CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE:
                String consSort
                if(params.consSort)
                    consSort = " order by ${params.sort} ${params.order}"
                else
                    consSort = " order by sortname asc "
                List consCostItems = CostItem.executeQuery("select ci, (select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType in (:roleTypes)) as sortname from CostItem as ci where ci.owner = :owner and ci.surveyOrg = null and ci.sub in (select s from Subscription as s join s.orgRelations orgRoles where s.instanceOf = :sub "+filterConsQuery[0]+consSort,[owner:org,sub:sub.instanceOf,roleTypes:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]+filterConsQuery[1])
                List collCostItems = CostItem.executeQuery("select ci, (select oo.org.sortname from OrgRole oo where ci.sub = oo.sub and oo.roleType in (:roleTypes)) as sortname from CostItem as ci where ci.owner = :owner and ci.surveyOrg = null and ci.sub in (select s from Subscription as s join s.orgRelations orgRoles where s.instanceOf = :sub "+filterConsQuery[0]+consSort,[owner:org,sub:sub,roleTypes:[OR_SUBSCRIBER_COLLECTIVE]]+filterConsQuery[1])
                result.cons.costItems = consCostItems.collect{row -> row[0]}.drop(consOffset).take(max)
                result.coll.costItems = collCostItems.collect{row -> row[0]}.drop(consOffset).take(max)
                result.cons.count = consCostItems.size()
                result.coll.count = collCostItems.size()
                if(result.cons.count > 0){
                    result.cons.sums = calculateResults(consCostItems.collect { row -> row[0]})
                }
                if(result.coll.count > 0){
                    result.coll.sums = calculateResults(collCostItems.collect { row -> row[0]})
                }
                String visibility = ""
                if(org.id != sub.getCollective()?.id) {
                    visibility = " and ci.isVisibleForSubscriber = true"
                }
                String subscrSort
                if(params.subscrSort)
                    subscrSort = " order by ${params.sort} ${params.order}"
                else
                    subscrSort = ""
                List subscrCostItems = CostItem.executeQuery('select ci from CostItem as ci where ci.owner in :owner and ci.surveyOrg = null and ci.sub = :sub'+visibility+filterSubscrQuery[0]+subscrSort,[owner:[sub.getConsortia()],sub:sub]+filterSubscrQuery[1])
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
                if(params.view in ["subscr","collAsSubscr"] || !params.view) {
                    result.subscr.costItems = costItems
                    result.subscr.count = count
                    result.subscr.sums = sums
                }
                else if(params.view.equals("consAtSubscr")) {
                    result.coll.costItems = costItems
                    result.coll.count = count
                    result.coll.sums = sums
                }
                break
        }
        result
    }

    /**
     * Will replace the methods index and financialData methods in FinanceController class for the institution-wide overview.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    Map getCostItems(params,long max) {
        LinkedHashMap result = [own:[:],cons:[:],coll:[:],subscr:[:]]
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
            case "cons":
            case "coll":
                consOffset = params.offset ? Integer.parseInt(params.offset) : 0
                break
            case "subscr": subscrOffset = params.offset ? Integer.parseInt(params.offset) : 0
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        String ownSort
        if(params.ownSort) {
            ownSort = " order by ${params.sort} ${params.order}"
        }
        else {
            ownSort = " order by sub.name asc"
        }
        //get own costs
        List<CostItem> ownSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles ' +
                'where ci.owner = :org and orgRoles.org = :org and orgRoles.roleType in :consType and sub.instanceOf = null and ci.surveyOrg = null'+filterQueryOwn[0]+ownSort,
                [org:org,consType:[OR_SUBSCRIPTION_CONSORTIA,OR_SUBSCRIPTION_COLLECTIVE]]+filterQueryOwn[1])
        ownSubscriptionCostItems.addAll(CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles where ' +
                'ci.owner = :org and orgRoles.org = :org and orgRoles.roleType in :nonConsTypes and ci.surveyOrg = null'+filterQueryOwn[0]+ownSort,
                [org:org,nonConsTypes:[OR_SUBSCRIBER,OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_COLLECTIVE]]+filterQueryOwn[1]))
        ownSubscriptionCostItems.addAll(CostItem.executeQuery('select ci from CostItem ci where ci.owner = :org and ci.sub is null and ci.surveyOrg is null'+filterQueryOwn[0],[org:org]+filterQueryOwn[1]))
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
        String consSort
        if(params.consSort)
            consSort = " order by ${params.sort} ${params.order}"
        else
            consSort = " order by orgRoles.org.sortname asc "
        List<CostItem> parentSubscriptionCostItems
        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            parentSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci ' +
                    'join ci.owner orgC ' +
                    'join ci.sub sub ' +
                    'join sub.instanceOf subC ' +
                    'join subC.orgRelations roleC ' +
                    'join sub.orgRelations roleMC ' +
                    'join sub.orgRelations orgRoles ' +
                    'where orgC = :org and orgC = roleC.org and roleMC.roleType in :consortialType and orgRoles.roleType in (:subscrType) and ci.surveyOrg = null' +
                    filterQueryCons[0] + consSort,
                    [org:org,consortialType:[OR_SUBSCRIPTION_CONSORTIA],subscrType:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_CONS_HIDDEN]]+filterQueryCons[1])
            result.cons.costItems = []
            limit = consOffset+max
            if(limit > parentSubscriptionCostItems.size())
                limit = parentSubscriptionCostItems.size()
            for(int i = consOffset;i < limit;i++){
                result.cons.costItems.add(parentSubscriptionCostItems[i])
            }
            result.cons.count = parentSubscriptionCostItems.size()
            if(result.cons.count > 0) {
                result.cons.sums = calculateResults(parentSubscriptionCostItems)
            }
        }
        else if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
            parentSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci ' +
                    'join ci.owner orgC ' +
                    'join ci.sub sub ' +
                    'join sub.instanceOf subC ' +
                    'join subC.orgRelations roleC ' +
                    'join sub.orgRelations roleMC ' +
                    'join sub.orgRelations orgRoles ' +
                    'where orgC = :org and orgC = roleC.org and roleMC.roleType in :consortialType and orgRoles.roleType in (:subscrType) and ci.surveyOrg = null' +
                    filterQueryCons[0] + consSort,
                    [org:org,consortialType:[OR_SUBSCRIPTION_COLLECTIVE],subscrType:[OR_SUBSCRIBER_COLLECTIVE]]+filterQueryCons[1])
            result.coll.costItems = []
            limit = consOffset+max
            if(limit > parentSubscriptionCostItems.size())
                limit = parentSubscriptionCostItems.size()
            for(int i = consOffset;i < limit;i++){
                result.coll.costItems.add(parentSubscriptionCostItems[i])
            }
            result.coll.count = parentSubscriptionCostItems.size()
            if(result.coll.count > 0) {
                result.coll.sums = calculateResults(parentSubscriptionCostItems)
            }
        }
        //get membership costs
        String subscrSort
        if(params.subscrSort)
            subscrSort = " order by ${params.sort} ${params.order}"
        else
            subscrSort = " order by sub.name asc"
        List<CostItem> consortialMemberSubscriptionCostItems = CostItem.executeQuery('select ci from CostItem ci '+
                'join ci.sub sub ' +
                'left join ci.subPkg subPkg ' +
                'join sub.instanceOf subC ' +
                'join subC.orgRelations roleC ' +
                'join sub.orgRelations orgRoles ' +
                'join ci.owner orgC ' +
                'where orgC = roleC.org and roleC.roleType in :consType and orgRoles.org = :org and orgRoles.roleType in :subscrType and ci.isVisibleForSubscriber = true and ci.surveyOrg = null'+
                filterQuerySubscr[0] + subscrSort,
                [org:org,consType:[OR_SUBSCRIPTION_CONSORTIA,OR_SUBSCRIPTION_COLLECTIVE],subscrType:[OR_SUBSCRIBER_CONS,OR_SUBSCRIBER_COLLECTIVE]]+filterQuerySubscr[1])
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
        result.filterPresets = filterQueryCons[1]
        result
    }

    /**
     * Processes the given parameters to build a query part which will be included into the base query strings
     *
     * @param params - a GrailsParameterMap containing parameters to be processed
     * @return an array with the filter string on position 0 and the filter parameter map on position 1
     */
    List processFilterParams(params,String filterView,boolean forSingleSubscription) {
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
                filterSubProviders.add(genericOIDService.resolveOID(subProvider))
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
            queryParams.filterSubStatus = SUBSCRIPTION_CURRENT
            params.filterSubStatus = SUBSCRIPTION_CURRENT.id.toString()
        }
        //the bracket from the subquery has to be closed when in subscription mode and for single subscription
        if(filterView.equals("cons") && forSingleSubscription) {
            filterQuery += ") "
        }
        //cost item filter settings
        //cost item title
        if(params.filterCITitle) {
            filterQuery += " and (ci.costTitle like :filterCITitle or ci.costTitle like :ciTitleLowerCase) "
            queryParams.filterCITitle = "%${params.filterCITitle}%"
            queryParams.ciTitleLowerCase = "%${params.filterCITitle.toLowerCase()}%"
            log.info(queryParams.filterCITitle)
        }
        //cost item subscription
        if(params.filterCISub) {
            filterQuery += " and sub in (:filterCISub) "
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
            filterQuery += " and sub in (select subscription from SubscriptionPackage where pkg in (:filterCISPkg)) "
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
            filterQuery += " and ci in (select cig.costItem from CostItemGroup cig where cig.budgetCode in (:filterCIBudgetCode)) "
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
            filterQuery += " and ci.reference in (:filterCIReference) "
            List<String> filterReferences = params."filterCIReference".split(',')
            queryParams.filterCIReference = filterReferences
            log.info(queryParams.filterCIReference)
        }
        //invoice number
        if(params.filterCIInvoiceNumber) {
            filterQuery += " and ci.invoice.invoiceNumber in (:filterCIInvoiceNumber) "
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
            filterQuery += " and ci.order.orderNumber in (:filterCIOrderNumber) "
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
            filterQuery += " and ci.costItemElement in (:filterCIElement) "
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
            filterQuery += " and ci.costItemStatus in (:filterCIStatus) "
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
                filterQuery += " and ci.taxKey = null"
            }
            else {
                filterQuery += " and ci.taxKey = :filterCITaxType "
                queryParams.filterCITaxType = CostItem.TAX_TYPES.valueOf(params.filterCITaxType)
            }
            log.info(params.filterCITaxType)
        }
        //financial year
        if(params.filterCIFinancialYear) {
            filterQuery += " and ci.financialYear = :filterCIFinancialYear "
            Year financialYear = Year.parse(params.filterCIFinancialYear)
            queryParams.filterCIFinancialYear = financialYear
            log.info(queryParams.filterCIFinancialYear)
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
        //valid on
        if(params.filterCIValidOn) {
            filterQuery += " and (ci.startDate <= :filterCIValidOn OR ci.startDate is null) and (ci.endDate >= :filterCIValidOn OR ci.endDate is null) "
            Date validOn = sdf.parse(params.filterCIValidOn)
            queryParams.filterCIValidOn = validOn
            log.info(queryParams.filterCIValidOn)
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
        //List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * ((ci.taxKey.taxRate/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value",[costItems:costItems])
        List<Map> billingSumsPositive = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive' group by ci.billingCurrency.value",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        List<Map> billingSumsNegative = CostItem.executeQuery("select NEW map(ci.billingCurrency.value as currency,sum(ci.costInBillingCurrency) as billingSum,sum(ci.costInBillingCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end)/100.0) + 1)) as billingSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative' group by ci.billingCurrency.value",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19])
        Map localSumsPositive = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'positive'",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
        Map localSumsNegative = CostItem.executeQuery("select NEW map(sum(ci.costInLocalCurrency) as localSum,sum(ci.costInLocalCurrency * (((case when ci.taxKey = :tax7 then 7 when ci.taxKey = :tax19 then 19 else 0 end) / 100.0) + 1)) as localSumAfterTax) from CostItem ci where ci in :costItems and ci.costItemElementConfiguration.value = 'negative'",[costItems:costItems,tax7:CostItem.TAX_TYPES.TAXABLE_7,tax19:CostItem.TAX_TYPES.TAXABLE_19]).get(0)
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
                case "wert (in eur)":
                case "value":
                case "value (in euro)": colMap.value = c
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
                List<Org> orgMatches = Org.executeQuery("select distinct idOcc.org from IdentifierOccurrence idOcc join idOcc.identifier id where cast(idOcc.org.id as string) = :idCandidate or idOcc.org.globalUID = :idCandidate or (id.value = :idCandidate and id.ns = :wibid)",[idCandidate:orgIdentifier,wibid:namespaces.wibid])
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
                                pkgMatches = Package.executeQuery("select distinct idOcc.pkg from IdentifierOccurrence idOcc where (idOcc.identifier.ns = :isil and idOcc.identifier.value = :idCandidate)")
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
                        List<TitleInstance> titleMatches = TitleInstance.executeQuery("select distinct idOcc.ti from IdentifierOccurrence idOcc join idOcc.identifier id where id.value = :idCandidate and id.ns in :namespaces",[idCandidate: ieIdentifier, namespaces: [namespaces.isbn,namespaces.doi,namespaces.zdb,namespaces.issn,namespaces.eissn]])
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
                Date datePaid = escapeService.parseDate(cols[colMap.datePaid])
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
                        RefdataValue taxType = RefdataValue.getByValueAndCategory(taxTypeKey,'TaxType')
                        if(!taxType)
                            taxType = RefdataValue.getByCategoryDescAndI10nValueDe('TaxType',taxTypeKey)
                        switch(taxType) {
                            case RefdataValue.getByValueAndCategory('not taxable','TaxType'): taxKey = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                                break
                            case RefdataValue.getByValueAndCategory('not applicable','TaxType'): taxKey = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                                break
                            case RefdataValue.getByValueAndCategory('taxable tax-exempt','TaxType'): taxKey = CostItem.TAX_TYPES.TAX_EXEMPT
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
                Date invoiceDate = escapeService.parseDate(cols[colMap.invoiceDate])
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
                    RefdataValue status = RefdataValue.getByValueAndCategory(statusKey,'CostItemStatus')
                    if(!status)
                        status = RefdataValue.getByCategoryDescAndI10nValueDe('CostItemStatus',statusKey)
                    if(!status)
                        mappingErrorBag.noValidStatus = statusKey
                    costItem.costItemStatus = status
                }
            }
            //costItemElement(nullable: true, blank: false) -> to element
            if(colMap.element != null) {
                String elementKey = cols[colMap.element]
                if(elementKey) {
                    RefdataValue element = RefdataValue.getByValueAndCategory(elementKey, 'CostItemElement')
                    if(!element)
                        element = RefdataValue.getByCategoryDescAndI10nValueDe('CostItemElement',elementKey)
                    if(!element)
                        mappingErrorBag.noValidElement = elementKey
                    costItem.costItemElement = element
                }
            }
            //costItemElementConfiguration(nullable: true, blank: false) -> to cost item sign
            if(colMap.costItemSign != null) {
                String elementSign = cols[colMap.costItemSign]
                if(elementSign) {
                    RefdataValue ciec = RefdataValue.getByValueAndCategory(elementSign, 'Cost configuration')
                    if(!ciec)
                        ciec = RefdataValue.getByCategoryDescAndI10nValueDe('Cost configuration',elementSign)
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
                Date startDate = escapeService.parseDate(cols[colMap.dateFrom])
                if(startDate)
                    costItem.startDate = startDate
            }
            //endDate(nullable: true, blank: false) -> to date to
            if(colMap.dateTo != null) {
                Date endDate = escapeService.parseDate(cols[colMap.dateTo])
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

}
