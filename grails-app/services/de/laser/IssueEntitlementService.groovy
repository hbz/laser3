package de.laser

import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional

@Transactional
class IssueEntitlementService {

    BatchQueryService batchQueryService
    FilterService filterService

    Subscription getTargetSubscription(Subscription s) {
        if(s.instanceOf && s.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE)
            s.instanceOf
        else s
    }

    Map<String, Object> getIssueEntitlements(Map configMap) {
        Map<String, Object> parameterGenerics = getParameterGenerics(configMap)
        Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                            identifierConfigMap = parameterGenerics.identifierConfigMap,
                            issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
        if(configMap.filter) {
            titleConfigMap.filter = configMap.filter
        }
        if(!configMap.containsKey('status')) {
            //titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT //activate if needed
            issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT
        }
        else {
            issueEntitlementConfigMap.ieStatus = Params.getRefdataList(configMap, 'status')
        }
        //process here the title-related parameters
        Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(titleConfigMap)
        Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams), ieIds = []
        if(configMap.identifier) {
            tippIds = tippIds.intersect(getTippsByIdentifier(identifierConfigMap, configMap.identifier))
        }
        //process here the issue entitlement-related parameters
        Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap)

        tippIds.collate(65000).each { List<Long> subset ->
            queryPart2.queryParams.subset = subset
            ieIds.addAll(IssueEntitlement.executeQuery(queryPart2.query, queryPart2.queryParams))
        }
        SortedSet<IssueEntitlement> result = new TreeSet<IssueEntitlement>()
        Set<Long> ieSubset = ieIds.drop(configMap.offset).take(configMap.max)
        result.addAll(IssueEntitlement.findAllByIdInList(ieSubset))
        //test B: test for export
        /*
        Set<IssueEntitlement> result = []
        ieIds.eachWithIndex { Long ieId, int i ->
            println "processing entry ${i}"
            result << IssueEntitlement.get(ieId)
        }
        */
        if(configMap.containsKey('fileformat') || configMap.containsKey('exportKBart')) {
            [entIDs: ieSubset]
        }
        else [entitlements: result, num_ies_rows: ieIds.size()]
    }

    Set<Long> getTippsByIdentifier(Map identifierConfigMap, String identifier) {
        identifierConfigMap.identifier = "%${identifier.toLowerCase()}%"
        TitleInstancePackagePlatform.executeQuery('select tipp.id from Identifier id join id.tipp tipp where tipp.pkg in (:packages) and lower(id.value) like :identifier and id.ns.ns in (:titleNS) and id.ns.nsType = :titleObj', identifierConfigMap)
    }

    Map<String, Object> calculateListPriceSumsForTitles(Set<Long> titleIDs) {
        String mainQry = "select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk = any(:tippIDs)"
        List eurCheck = batchQueryService.longArrayQuery("select sum(pi.pi_list_price) as list_price_eur from (${mainQry}) as pi where pi.rn = 1", [tippIDs: titleIDs], [currency: RDStore.CURRENCY_EUR.id]),
             usdCheck = batchQueryService.longArrayQuery("select sum(pi.pi_list_price) as list_price_usd from (${mainQry}) as pi where pi.rn = 1", [tippIDs: titleIDs], [currency: RDStore.CURRENCY_USD.id]),
             gbpCheck = batchQueryService.longArrayQuery("select sum(pi.pi_list_price) as list_price_gbp from (${mainQry}) as pi where pi.rn = 1", [tippIDs: titleIDs], [currency: RDStore.CURRENCY_GBP.id])
        BigDecimal listPriceSumEUR = eurCheck[0]['list_price_eur']
        BigDecimal listPriceSumUSD = usdCheck[0]['list_price_usd']
        BigDecimal listPriceSumGBP = gbpCheck[0]['list_price_gbp']
        [listPriceSumEUR: listPriceSumEUR, listPriceSumGBP: listPriceSumGBP, listPriceSumUSD: listPriceSumUSD]
    }

    Map<String, Object> getParameterGenerics(configMap) {
        String sort = configMap.containsKey('sort') ? configMap.sort : 'tipp.sortname'
        String order = configMap.containsKey('order') ? configMap.order : 'asc'
        Map<String, Object> titleConfigMap = [packages: configMap.packages, platforms: configMap.platforms, ddcs: configMap.ddcs, languages: configMap.languages,
                                              subject_references: configMap.subject_references, series_names: configMap.series_names, summaryOfContent: configMap.summaryOfContent,
                                              ebookFirstAutorOrFirstEditor: configMap.ebookFirstAutorOrFirstEditor, dateFirstOnlineFrom: configMap.dateFirstOnlineFrom,
                                              dateFirstOnlineTo: configMap.dateFirstOnlineFrom, yearsFirstOnline: configMap.yearsFirstOnline, publishers: configMap.publishers,
                                              coverageDepth: configMap.coverageDepth, title_types: configMap.title_types, medium: configMap.medium, sort: sort, order: order],
                            identifierConfigMap = [packages: configMap.packages, titleNS: IdentifierNamespace.CORE_TITLE_NS, titleObj: IdentifierNamespace.NS_TITLE, sort: sort, order: order],
                            issueEntitlementConfigMap = [subscription: configMap.subscription, asAt: configMap.asAt, hasPerpetualAccess: configMap.hasPerpetualAccess, titleGroup: configMap.titleGroup, sort: sort, order: order]
        [titleConfigMap: titleConfigMap, identifierConfigMap: identifierConfigMap, issueEntitlementConfigMap: issueEntitlementConfigMap]
    }
}
