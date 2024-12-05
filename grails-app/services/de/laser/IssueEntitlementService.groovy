package de.laser

import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult

@Transactional
class IssueEntitlementService {

    BatchQueryService batchQueryService
    FilterService filterService

    Subscription getTargetSubscription(Subscription s) {
        if(s.instanceOf && s.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE)
            s.instanceOf
        else s
    }

    Map<String, Object> getKeys(Map configMap) {
        //continue with testing: sub/index, sub export index, sub/addEntitlements; migrate then copying of subscription holdings
        Map<String, Object> parameterGenerics = getParameterGenerics(configMap)
        Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                            identifierConfigMap = parameterGenerics.identifierConfigMap,
                            issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
        if(configMap.filter) {
            titleConfigMap.filter = configMap.filter
        }
        if(!configMap.containsKey('status')) {
            titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT.id //activate if needed
            issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
        }
        else {
            titleConfigMap.tippStatus = configMap.status
            issueEntitlementConfigMap.ieStatus = configMap.status
        }
        //process here the title-related parameters
        Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(titleConfigMap)
        Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams), ieIDs = []
        if(configMap.identifier) {
            tippIDs = tippIDs.intersect(getTippsByIdentifier(identifierConfigMap, configMap.identifier))
        }
        //process here the issue entitlement-related parameters
        Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap)

        tippIDs.collate(65000).each { List<Long> subset ->
            queryPart2.queryParams.subset = subset
            ieIDs.addAll(IssueEntitlement.executeQuery(queryPart2.query, queryPart2.queryParams))
        }
        [tippIDs: tippIDs, ieIDs: ieIDs]
    }

    Set<Long> getTippsByIdentifier(Map identifierConfigMap, String identifier) {
        identifierConfigMap.identifier = "%${identifier.toLowerCase()}%"
        TitleInstancePackagePlatform.executeQuery('select tipp.id from Identifier id join id.tipp tipp where tipp.pkg in (:packages) and lower(id.value) like :identifier and id.ns.ns in (:titleNS) and id.ns.nsType = :titleObj', identifierConfigMap)
    }

    Map<String, Object> buildIdentifierInverseMap(Set<Long> tippIDs) {
        Map<String, Object> result = [:]
        /*
        desired:
        {
            printIdentifiers: {
                value: tipp
                value: tipp
            },
            printIdentifiers: {
                value: tipp
                value: tipp
            },
            dois: {
                value: tipp
                value: tipp
            },
            proprietaryIdentifiers: {
                value: tipp
                value: tipp
            }
        }
         */
        String query = "select * from " +
                "(select json_agg(json_build_object(id_value, id_tipp_fk)) as print_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = any(:printIdentifiers)) as print," +
                "(select json_agg(json_build_object(id_value, id_tipp_fk)) as online_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = any(:onlineIdentifiers)) as online," +
                "(select json_agg(json_build_object(id_value, id_tipp_fk)) as doi from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = :doi) as doi," +
                "(select json_agg(json_build_object(tipp_host_platform_url, tipp_id)) as url from title_instance_package_platform where tipp_id = any(:tippIDs)) as url," +
                "(select json_agg(json_build_object(id_value, id_tipp_fk)) as proprietary_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = :proprietary) as proprietary"
        Set<String> printIdentifierNamespaces = [IdentifierNamespace.ISBN, IdentifierNamespace.ISSN], onlineIdentifierNamespaces = [IdentifierNamespace.EISBN, IdentifierNamespace.EISSN]
        Map<String, Object> arrayParams = [tippIDs: tippIDs, printIdentifiers: printIdentifierNamespaces, onlineIdentifiers: onlineIdentifierNamespaces], otherParams = [doi: IdentifierNamespace.DOI, proprietary: IdentifierNamespace.TITLE_ID]
        JsonSlurper slurper = new JsonSlurper()
        List<GroovyRowResult> rows = batchQueryService.longArrayQuery(query, arrayParams, otherParams)
        Map<String, String> printIdentifiers = [:], onlineIdentifiers = [:], doi = [:], url = [:], proprietaryIdentifiers = [:]
        List printIds = rows[0]['print_identifier'] ? slurper.parseText(rows[0]['print_identifier'].toString()) : [],
             onlineIds = rows[0]['online_identifier'] ? slurper.parseText(rows[0]['online_identifier'].toString()) : [],
             dois = rows[0]['doi'] ? slurper.parseText(rows[0]['doi'].toString()) : [],
             urls = rows[0]['url'] ? slurper.parseText(rows[0]['url'].toString()) : [],
             propIds = rows[0]['proprietary_identifier'] ? slurper.parseText(rows[0]['proprietary_identifier'].toString()) : []
        printIdentifiers.putAll(printIds.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        onlineIdentifiers.putAll(onlineIds.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        doi.putAll(dois.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        url.putAll(urls.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        proprietaryIdentifiers.putAll(propIds.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        result.put('printIdentifiers', printIdentifiers)
        result.put('onlineIdentifiers', onlineIdentifiers)
        result.put('doi', doi)
        result.put('url', url)
        result.put('proprietaryIdentifiers', proprietaryIdentifiers)
        result
    }

    /**
     * Determines to the given report the matching {@link TitleInstancePackagePlatform} ID. First, matching is attempted against
     * the online identifier or e-ISBN; an attempt is also made with stripped parenthesis. If that fails, another attempt is
     * made with the print identifier, again, with stripped parenthesis as well; if that fails, too, further attempts are made with
     * DOI and the proprietary identifier of the provider
     * @param titles the {@link Map} of titles, grouped by [namespace: [identifier value: title]]
     * @param report the COUNTER report result
     * @return the matching {@link TitleInstancePackagePlatform} ID or null, if no match has been found
     */
    Long matchReport(Map<String, Object> titles, Map report) {
        Long tipp = null
        if(report.onlineIdentifier || report.isbn) {
            tipp = titles.onlineIdentifiers.get(report.onlineIdentifier)
            if(!tipp)
                tipp = titles.onlineIdentifiers.get(report.onlineIdentifier?.replaceAll('-',''))
            if(!tipp)
                tipp = titles.onlineIdentifiers.get(report.isbn)
            if(!tipp)
                tipp = titles.onlineIdentifiers.get(report.isbn?.replaceAll('-',''))
        }
        if(!tipp && (report.printIdentifier || report.isbn)) {
            tipp = titles.printIdentifiers.get(report.printIdentifier)
            if(!tipp)
                tipp = titles.printIdentifiers.get(report.printIdentifier?.replaceAll('-',''))
            if(!tipp)
                tipp = titles.printIdentifiers.get(report.isbn)
            if(!tipp)
                tipp = titles.printIdentifiers.get(report.isbn?.replaceAll('-',''))
        }
        if(!tipp && report.doi) {
            tipp = titles.doi.get(report.doi)
        }
        if(!tipp && report.proprietaryIdentifier) {
            if(!tipp)
                tipp = titles.proprietaryIdentifiers.get(report.proprietaryIdentifier)
        }
        tipp
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
        String sort = configMap.containsKey('sort') && configMap.sort ? configMap.sort : 'tipp.sortname'
        String order = configMap.containsKey('order') && configMap.order ? configMap.order : 'asc'
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
