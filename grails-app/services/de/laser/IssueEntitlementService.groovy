package de.laser

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import com.opencsv.exceptions.CsvMalformedLineException
import de.laser.cache.EhcacheWrapper
import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import org.springframework.web.multipart.MultipartFile

@Transactional
class IssueEntitlementService {

    AuditService auditService
    BatchQueryService batchQueryService
    ContextService contextService
    FilterService filterService
    TitleService titleService

    Subscription getTargetSubscription(Subscription s) {
        if(s.instanceOf && auditService.getAuditConfig(s.instanceOf, 'holdingSelection'))
            s.instanceOf
        else s
    }

    Map<String, Object> getCounts(Map configMap) {
        int currentIECounts = 0, plannedIECounts = 0, expiredIECounts = 0, deletedIECounts = 0, allIECounts = 0
        Map<String, Object> parameterGenerics = getParameterGenerics(configMap)
        Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                            identifierConfigMap = parameterGenerics.identifierConfigMap,
                            issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
        titleConfigMap.noSort = true
        Set<Long> tippIDs = []
        Map<String, Object> queryPart1 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap), queryPart2 = filterService.getTippSubsetQuery(titleConfigMap)
        List<Map<String, Object>> tippIeMap = IssueEntitlement.executeQuery(queryPart1.query, queryPart1.queryParams)
        tippIeMap.each { row ->
            tippIDs << row.tippID
        }
        tippIDs = tippIDs.intersect(TitleInstancePackagePlatform.executeQuery(queryPart2.query, queryPart2.queryParams))
        if(configMap.identifier) {
            tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, configMap.identifier))
        }
        if (configMap.containsKey('hasPerpetualAccess')) {
            String permanentTitleQuery = "select pt.tipp.id from PermanentTitle pt where pt.owner = :subscriber"
            Map<String, Object> permanentTitleParams = [subscriber: configMap.subscription.getSubscriberRespConsortia()]
            Set<Long> permanentTitles = TitleInstancePackagePlatform.executeQuery(permanentTitleQuery, permanentTitleParams)
            if (RefdataValue.get(configMap.hasPerpetualAccess) == RDStore.YN_YES) {
                tippIDs = tippIDs.intersect(permanentTitles)
            }else{
                tippIDs.removeAll(permanentTitles)
            }
        }
        tippIDs.collate(65000).each { List<Long> subset ->
            List counts = IssueEntitlement.executeQuery('select count(*), rv.id from IssueEntitlement ie join ie.status rv where ie.tipp.id in (:subset) and ie.subscription.id = :subscription and ie.status.id != :removed group by rv.id', [subset: subset, subscription: configMap.subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
            counts.each { row ->
                switch (row[1]) {
                    case RDStore.TIPP_STATUS_CURRENT.id: currentIECounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_EXPECTED.id: plannedIECounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_RETIRED.id: expiredIECounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_DELETED.id: deletedIECounts += row[0]
                        break
                }
                allIECounts += row[0]
            }
        }
        [currentIECounts: currentIECounts, plannedIECounts: plannedIECounts, expiredIECounts: expiredIECounts, deletedIECounts: deletedIECounts, allIECounts: allIECounts]
    }

    int getCurrentIssueEntitlementCountOfPackage(SubscriptionPackage sp){
        Subscription targetSubscription = getTargetSubscription(sp.subscription)
        IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and ie.subscription = :sub and ie.status = :current', [sub: targetSubscription, pkg: sp.pkg, current: RDStore.TIPP_STATUS_CURRENT])[0]
    }

    Map<String, Object> getKeys(Map configMap) {
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
        Set<Long> tippIDs = [], ieIDs = []
        //may become a performance bottleneck; keep under observation!
        String permanentTitleQuery = "select pt.tipp.id from PermanentTitle pt where pt.owner = :subscriber"
        Map<String, Object> permanentTitleParams = [subscriber: configMap.subscription.getSubscriberRespConsortia()]
        if(configMap.containsKey('newEntitlements')) {
            //process here the title-related parameters
            Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(titleConfigMap)
            tippIDs = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams)
            if(configMap.identifier) {
                tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, configMap.identifier))
            }
            tippIDs.removeAll(IssueEntitlement.executeQuery('select ie.tipp.id from IssueEntitlement ie where ie.subscription = :subscription and ie.status != :removed', [subscription: configMap.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
            tippIDs.removeAll(TitleInstancePackagePlatform.executeQuery(permanentTitleQuery, permanentTitleParams))
        }
        else {
            //process here the issue entitlement-related parameters
            Map<String, Object> queryPart1 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap)
            List<Map<String, Object>> tippIeMap = IssueEntitlement.executeQuery(queryPart1.query, queryPart1.queryParams)
            tippIeMap.each { row ->
                tippIDs << row.tippID
            }
            //process here the title-related parameters
            Map<String, Object> queryPart2 = filterService.getTippSubsetQuery(titleConfigMap)
            tippIDs = tippIDs.intersect(TitleInstancePackagePlatform.executeQuery(queryPart2.query, queryPart2.queryParams))
            if(configMap.identifier) {
                tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, configMap.identifier))
            }
            if (configMap.containsKey('hasPerpetualAccess')) {
                Set<Long> permanentTitles = TitleInstancePackagePlatform.executeQuery(permanentTitleQuery, permanentTitleParams)
                if (RefdataValue.get(configMap.hasPerpetualAccess) == RDStore.YN_YES) {
                    tippIDs = tippIDs.intersect(permanentTitles)
                }else{
                    tippIDs.removeAll(permanentTitles)
                }
            }
            tippIeMap.each { row ->
                if(row.tippID in tippIDs)
                    ieIDs << row.ieID
            }
            /*tippIDs.collate(65000).each { List<Long> subset ->
                queryPart2.queryParams.subset = subset
                ieIDs.addAll(IssueEntitlement.executeQuery(queryPart2.query, queryPart2.queryParams))
            }*/
        }
        [tippIDs: tippIDs, ieIDs: ieIDs]
    }

    Set<String> getPerpetuallyPurchasedTitleHostPlatformURLs(Org context, Set<Subscription> subscriptions) {
        Set<String> perpetuallyPurchasedTippHostPlatformURLs = []
        //own hostPlatformURLs
        perpetuallyPurchasedTippHostPlatformURLs.addAll(PermanentTitle.executeQuery('select tipp.hostPlatformURL from PermanentTitle pt join pt.tipp tipp where pt.owner = :context and tipp.status != :removed', [context: context, removed: RDStore.TIPP_STATUS_REMOVED]))
        perpetuallyPurchasedTippHostPlatformURLs.addAll(PermanentTitle.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.perpetualAccessBySub in (:subscriptions) and ie.status != :removed', [subscriptions: subscriptions, removed: RDStore.TIPP_STATUS_REMOVED]))
        //consortial hostPlatformURLs
        perpetuallyPurchasedTippHostPlatformURLs.addAll(PermanentTitle.executeQuery("select tipp.hostPlatformURL from PermanentTitle pt join pt.issueEntitlement ie join pt.tipp tipp where ie.status != :removed and ie.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :context and oo.roleType = :subscriber and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection'))", [context: context, subscriber: RDStore.OR_SUBSCRIBER_CONS, removed: RDStore.TIPP_STATUS_REMOVED]))
        perpetuallyPurchasedTippHostPlatformURLs
    }

    /**
     * ex <ul>
     *     <li>{@link SubscriptionService#tippSelectForSurvey(org.springframework.web.multipart.MultipartFile, java.util.Map)}</li>
     *     <li>{@link SubscriptionService#selectEntitlementsWithKBART(org.springframework.web.multipart.MultipartFile, java.util.Map)}</li>
     *     <li>{@link SubscriptionService#issueEntitlementEnrichment(org.springframework.web.multipart.MultipartFile, java.util.Map)}</li>
     *     </ul>
     */
    Map<String, Object> matchTippsFromFile(MultipartFile inputFile, Map configMap) {
        if(!configMap.subscription) {
            [error: 'noValidSubscription']
        }
        else {
            EhcacheWrapper userCache = contextService.getUserCache(configMap.progressCacheKey)
            userCache.put('label', 'Verarbeite Titel ...')
            userCache.put('progress', configMap.floor)
            boolean pickWithNoPick = false, wrongSeparator = false
            String fileErrors = null
            int countRows = 0, total = 0, toAddCount = 0, start, percentage = 0
            Set<String> titleRow = []
            Map<TitleInstancePackagePlatform, Map<String, Object>> matchedTitles = [:] //use keySet() to use only the retrieved we:kb keys
            Set<Map<String, Object>> notAddedTitles = [], notInPackage = []
            //now, assemble the identifiers available to highlight
            Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                           eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name),
                                                           isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                           issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name),
                                                           eisbn: IdentifierNamespace.findByNsAndNsType('eisbn', TitleInstancePackagePlatform.class.name),
                                                           doi: IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name),
                                                           title_id: IdentifierNamespace.findByNsAndNsType('title_id', TitleInstancePackagePlatform.class.name)]
            Map<String, Integer> colMap = [publicationTitleCol: -1, zdbCol: -1, onlineIdentifierCol: -1, printIdentifierCol: -1, dateFirstInPrintCol: -1, dateFirstOnlineCol: -1,
                                           startDateCol       : -1, startVolumeCol: -1, startIssueCol: -1,
                                           endDateCol         : -1, endVolumeCol: -1, endIssueCol: -1,
                                           accessStartDateCol : -1, accessEndDateCol: -1, coverageDepthCol: -1, coverageNotesCol: -1, embargoCol: -1,
                                           listPriceCol       : -1, listCurrencyCol: -1, listPriceEurCol: -1, listPriceUsdCol: -1, listPriceGbpCol: -1, localPriceCol: -1, localCurrencyCol: -1, priceDateCol: -1,
                                           titleUrlCol: -1, titleIdCol: -1, doiCol: -1, pick: -1]
            inputFile.getInputStream().withReader(configMap.encoding) { reader ->
                try {
                    char separator = '\t'
                    ICSVParser csvp = new CSVParserBuilder().withSeparator(separator).build() // csvp.DEFAULT_SEPARATOR, csvp.DEFAULT_QUOTE_CHARACTER, csvp.DEFAULT_ESCAPE_CHARACTER
                    CSVReader csvr = new CSVReaderBuilder( reader ).withCSVParser( csvp ).build()
                    List<String[]> lines = csvr.readAll()
                    if(lines[0].size() == 1) {
                        wrongSeparator = true
                    }
                    else {
                        total = lines.size()
                        if(!configMap.containsKey('withPick'))
                            toAddCount = total
                        if(configMap.containsKey('withIDOnly')) {
                            start = 0
                        }
                        else {
                            start = 1
                            titleRow.addAll(lines[0])
                            if(titleRow[0]) {
                                titleRow.eachWithIndex{ String headerCol, int c ->
                                    switch (headerCol.toLowerCase().trim()) {
                                        case ["title_url", "zugriffs-url", "access url"]: colMap.titleUrlCol = c
                                            break
                                        case "title_id": colMap.titleIdCol = c
                                            break
                                        case ["doi", "doi_identifier"]: colMap.doiCol= c
                                            break
                                        case "zdb_id": colMap.zdbCol = c
                                            break
                                        case ["print_identifier","print identifier"]: colMap.printIdentifierCol = c
                                            break
                                        case ["online_identifier","online identifier"]: colMap.onlineIdentifierCol = c
                                            break
                                        case ["title", "publication_title"]: colMap.publicationTitleCol = c
                                            break
                                        case "date_monograph_published_print": colMap.dateFirstInPrintCol = c
                                            break
                                        case "date_monograph_published_online": colMap.dateFirstOnlineCol = c
                                            break
                                        case "date_first_issue_online": colMap.startDateCol = c
                                            break
                                        case "num_first_vol_online": colMap.startVolumeCol = c
                                            break
                                        case "num_first_issue_online": colMap.startIssueCol = c
                                            break
                                        case "date_last_issue_online": colMap.endDateCol = c
                                            break
                                        case "num_last_vol_online": colMap.endVolumeCol = c
                                            break
                                        case "num_last_issue_online": colMap.endIssueCol = c
                                            break
                                        case "access_start_date": colMap.accessStartDateCol = c
                                            break
                                        case "access_end_date": colMap.accessEndDateCol = c
                                            break
                                        case "embargo_info": colMap.embargoCol = c
                                            break
                                        case "coverage_depth": colMap.coverageDepthCol = c
                                            break
                                        case "notes": colMap.coverageNotesCol = c
                                            break
                                        case "listprice_eur": colMap.listPriceEurCol = c
                                            break
                                        case "listprice_usd": colMap.listPriceUsdCol = c
                                            break
                                        case "listprice_gbp": colMap.listPriceGbpCol = c
                                            break
                                        case "localprice_eur": colMap.localPriceEurCol = c
                                            break
                                        case "localprice_usd": colMap.localPriceUsdCol = c
                                            break
                                        case "localprice_gbp": colMap.localPriceGbpCol = c
                                            break
                                        case ["pick", "auswahl"]: colMap.pick = c
                                            break
                                    }
                                }
                            }
                        }
                        if(!configMap.containsKey('withPick') || (configMap.containsKey('withPick') && colMap.pick > -1)) {
                            for(int i = start; i < total; i++) {
                                String[] line = lines[i]
                                //if(line[0]) {
                                /*
                                    here the switch between the matching methods:
                                    Select from total list (KBART) -> used now in tippSelectForSurvey()
                                    Select (KBART) -> used now in selectEntitlementsWithKBART()
                                    Select (with ID only) -> should contain one single column without header which contains the ID
                                    continue with tests
                                 */
                                boolean select = !configMap.containsKey('withPick') || (configMap.containsKey('withPick') && colMap.pick >= 0 && line[colMap.pick].trim().toLowerCase() in [RDStore.YN_YES.value.toLowerCase(), RDStore.YN_YES.getI10n('value').toLowerCase(), 'x'])
                                if((line.size() == titleRow.size() && select) || configMap.containsKey('withIDOnly')) {
                                    countRows++
                                    log.debug("now processing row ${countRows}")
                                    TitleInstancePackagePlatform match = null
                                    //cascade: 1. title_url, 2. title_id, 3. identifier map
                                    if(configMap.containsKey('withIDOnly')) {
                                        match = TitleInstancePackagePlatform.findByHostPlatformURLAndPkgInListAndStatusNotEqual(line[0].trim(), configMap.subPkgs, RDStore.TIPP_STATUS_REMOVED)
                                        if(!match) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[0].trim(), ns: namespaces.values(), removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                    }
                                    else {
                                        if (colMap.titleUrlCol >= 0 && line[colMap.titleUrlCol] != null && !line[colMap.titleUrlCol].trim().isEmpty()) {
                                            match = TitleInstancePackagePlatform.findByHostPlatformURLAndPkgInListAndStatusNotEqual(line[colMap.titleUrlCol].trim(), configMap.subPkgs, RDStore.TIPP_STATUS_REMOVED)
                                        }
                                        if (!match && colMap.titleIdCol >= 0 && line[colMap.titleIdCol] != null && !line[colMap.titleIdCol].trim().isEmpty()) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[colMap.titleIdCol].trim(), ns: namespaces.title_id, removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                        if (!match && colMap.doiCol >= 0 && line[colMap.doiCol] != null && !line[colMap.doiCol].trim().isEmpty()) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[colMap.doiCol].trim(), ns: namespaces.doi, removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                        if (!match && colMap.onlineIdentifierCol >= 0 && line[colMap.onlineIdentifierCol] != null && !line[colMap.onlineIdentifierCol].trim().isEmpty()) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[colMap.onlineIdentifierCol].trim(), ns: [namespaces.eisbn, namespaces.eissn], removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                        if (!match && colMap.printIdentifierCol >= 0 && line[colMap.printIdentifierCol] != null && !line[colMap.printIdentifierCol].trim().isEmpty()) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[colMap.printIdentifierCol].trim(), ns: [namespaces.isbn, namespaces.issn], removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                        if (!match && colMap.zdbCol >= 0 && line[colMap.zdbCol] != null && !line[colMap.zdbCol].trim().isEmpty()) {
                                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: configMap.subPkgs, value: line[colMap.zdbCol].trim(), ns: namespaces.zdb, removed: RDStore.TIPP_STATUS_REMOVED])
                                            if (matchList.size() == 1)
                                                match = matchList[0] as TitleInstancePackagePlatform
                                        }
                                    }
                                    Map<String, Object> externalTitleData = [:]
                                    if(configMap.containsKey('withIDOnly')) {
                                        externalTitleData.put('id', line[0].trim())
                                    }
                                    else {
                                        colMap.each { String colName, int colNo ->
                                            if (colNo > -1 && line[colNo]) {
                                                String cellEntry = line[colNo].trim()
                                                if (cellEntry)
                                                    externalTitleData.put(titleRow[colNo], cellEntry) //NOTE! when migrating issueEntitlementEnrichment, match against ORIGINAL header names (= titleRow) and NOT against internal ones (= colMap)!
                                            }
                                            //fill up empty cells for an eventual return file
                                            else if(colName in ['found_in_package', 'already_purchased_at']) {
                                                if(colName == 'already_purchased_at' && !configMap.containsKey('issueEntitlementEnrichment'))
                                                    externalTitleData.put(colName, null)
                                            }
                                            else if(colNo > -1) externalTitleData.put(titleRow[colNo], null)
                                        }
                                    }
                                    if (match) {
                                        matchedTitles.put(match, externalTitleData)
                                        if(configMap.containsKey('withPick'))
                                            toAddCount++
                                    }
                                    else if(!match) {
                                        notInPackage << externalTitleData
                                        externalTitleData.put('found_in_package', RDStore.YN_NO.getI10n('value'))
                                        notAddedTitles << externalTitleData
                                    }
                                }
                                //start from floor, end at ceil
                                percentage = configMap.floor+countRows*((configMap.ceil-configMap.floor)/total)
                                userCache.put('progress', percentage)
                                //}
                            }
                        }
                        else pickWithNoPick = true
                    }
                }
                catch (CsvMalformedLineException e) {
                    fileErrors = e.getMessage()
                }
            }
            userCache.put('progress', configMap.ceil)
            [titleRow: titleRow, pickWithNoPick: pickWithNoPick, fileErrors: fileErrors, wrongSeparator: wrongSeparator, matchedTitles: matchedTitles, notAddedTitles: notAddedTitles, toAddCount: toAddCount, processedCount: countRows, notInPackage: notInPackage, notInPackageCount: notInPackage.size()]
        }
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

    boolean existsSerialInHolding(Subscription subscription, List statusKeys) {
        String query = 'select distinct(tipp.titleType) from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :sub and ie.status in (:status)'
        List titleTypes = IssueEntitlement.executeQuery(query, [sub: subscription, status: statusKeys.collect { key -> RefdataValue.get(key) }])
        return titleTypes.contains('serial')
    }

    Map<String, Object> getParameterGenerics(configMap) {
        String sort = configMap.containsKey('sort') && configMap.sort ? configMap.sort : 'tipp.sortname'
        String order = configMap.containsKey('order') && configMap.order ? configMap.order : 'asc'
        Map<String, Object> titleConfigMap = [filter: configMap.filter, packages: configMap.packages, platforms: configMap.platforms, ddcs: configMap.ddcs, languages: configMap.languages,
                                              first_author: configMap.first_author, first_editor: configMap.first_editor,
                                              subject_references: configMap.subject_references, series_names: configMap.series_names, summaryOfContent: configMap.summaryOfContent,
                                              ebookFirstAutorOrFirstEditor: configMap.ebookFirstAutorOrFirstEditor, dateFirstOnlineFrom: configMap.dateFirstOnlineFrom,
                                              dateFirstOnlineTo: configMap.dateFirstOnlineFrom, yearsFirstOnline: configMap.yearsFirstOnline, publishers: configMap.publishers, provider: configMap.provider,
                                              coverageDepth: configMap.coverageDepth, title_types: configMap.title_types, medium: configMap.medium, sort: sort, order: order],
                            identifierConfigMap = [identifier: configMap.identifier, packages: configMap.packages, titleNS: IdentifierNamespace.CORE_TITLE_NS, titleObj: IdentifierNamespace.NS_TITLE], //may be needed elsewhere sort: sort, order: order
                            issueEntitlementConfigMap = [subscription: configMap.subscription, asAt: configMap.asAt, hasPerpetualAccess: configMap.hasPerpetualAccess, titleGroup: configMap.titleGroup, sort: sort, order: order]
        [titleConfigMap: titleConfigMap, identifierConfigMap: identifierConfigMap, issueEntitlementConfigMap: issueEntitlementConfigMap]
    }
}
