package com.k_int.kbplus

import de.laser.ApiSource
import de.laser.AuditConfig
import de.laser.AuditService
import de.laser.EscapeService
import de.laser.GlobalRecordSource
import de.laser.Identifier
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.OrgRole
import de.laser.Package
import de.laser.PendingChange
import de.laser.Platform
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.finance.PriceItem
import de.laser.system.SystemEvent
import de.laser.base.AbstractCoverage
import de.laser.IssueEntitlementCoverage
import de.laser.PendingChangeConfiguration
import de.laser.TIPPCoverage
import de.laser.exceptions.SyncException
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import de.laser.titles.BookInstance
import de.laser.titles.DatabaseInstance
import de.laser.titles.JournalInstance
import de.laser.titles.TitleHistoryEvent
import de.laser.titles.TitleHistoryEventParticipant
import de.laser.titles.TitleInstance
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.Method
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.hibernate.Session

import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

/**
 * Implements the synchronisation workflow according to https://dienst-wiki.hbz-nrw.de/display/GDI/GOKB+Sync+mit+LASER
 */
@Transactional
class GlobalSourceSyncService extends AbstractLockableService {

    ExecutorService executorService
    ChangeNotificationService changeNotificationService
    PendingChangeService pendingChangeService
    def genericOIDService
    EscapeService escapeService
    GlobalRecordSource source
    ApiSource apiSource
    AuditService auditService

    final static long RECTYPE_PACKAGE = 0
    final static long RECTYPE_TITLE = 1
    final static long RECTYPE_ORG = 2
    final static long RECTYPE_TIPP = 3
    final static Map<Long,String> RECTYPES = [(RECTYPE_PACKAGE):'packages',(RECTYPE_TITLE):'titles',(RECTYPE_ORG):'orgs',(RECTYPE_TIPP):'tipps']

    Map<String, RefdataValue> titleStatus = [:], titleMedium = [:], tippStatus = [:], packageStatus = [:], orgStatus = [:], currency = [:]
    Long maxTimestamp = 0
    Map<String,Integer> initialPackagesCounter = [:]
    Map<String,Set<Map<String,Object>>> pkgPropDiffsContainer = [:]
    Map<String,Set<Map<String,Object>>> packagesToNotify = [:]

    SimpleDateFormat xmlTimestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    boolean running = false

    /**
     * This is the entry point for triggering the sync workflow. To ensure locking, a flag will be set when a process is already running
     * @return a flag whether a process is already running
     */
    boolean startSync() {
        if (!running) {
            executorService.execute({ doSync() })
            //doSync()
            return true
        }
        else {
            log.warn("Sync already running, not starting again")
            return false
        }
    }

    /**
     * The sync process wrapper. It takes every {@link GlobalRecordSource}, fetches the information since a given timestamp
     * and updates the local records
     */
    void doSync() {
        running = true
        defineMapFields()
        //we need to consider that there may be several sources per instance
        List<GlobalRecordSource> jobs = GlobalRecordSource.findAllByActive(true)
        jobs.each { GlobalRecordSource source ->
            this.source = source
            if(source.type == "OAI") {
                try {
                    SystemEvent.createEvent('GSSS_OAI_START',['jobId':source.id])
                    Thread.currentThread().setName("GlobalDataSync")
                    Date oldDate = source.haveUpTo
                    Long maxTimestamp = 0
                    log.info("getting records from job #${source.id} with uri ${source.uri} since ${oldDate} using ${source.fullPrefix}")
                    //merging from OaiClient
                    log.info("getting latest changes ...")
                    List<List<Map<String,Object>>> tippsToNotify = []
                    boolean more = true
                    log.info("attempt get ...")
                    String resumption = null
                    // perform GET request, expection XML response data
                    while(more) {
                        Map<String,String> queryParams = [verb:'ListRecords']
                        //Map<String,String> queryParams = [verb:'GetRecord',metadataPrefix:'gokb',identifier:'a3f41aef-8316-442e-99e9-29e2f011fc22'] //for debugging
                        if(resumption) {
                            queryParams.resumptionToken = resumption
                            log.info("in loop, making request with link ${source.uri}?verb=ListRecords&metadataPrefix=${source.fullPrefix}&resumptionToken=${resumption} ...")
                        }
                        else {
                            String fromParam = oldDate ? xmlTimestampFormat.format(oldDate) : ''
                            log.info("in loop, making first request, timestamp: ${fromParam} ...")
                            queryParams.from = fromParam
                        }
                        GPathResult listOAI = fetchRecordOAI(RECTYPES[source.rectype],queryParams)
                        if(listOAI) {
                            switch(source.rectype) {
                                case RECTYPE_PACKAGE:
                                    updateNonPackageData(listOAI)
                                    listOAI.record.each { NodeChild r ->
                                        //continue processing here, original code jumps back to GlobalSourceSyncService
                                        log.info("got OAI record ${r.header.identifier} datestamp: ${r.header.datestamp} job: ${source.id} url: ${source.uri}")
                                        //String recUUID = r.header.uuid.text() ?: '0'
                                        //String recIdentifier = r.header.identifier.text()
                                        Date recordTimestamp = DateUtils.parseDateGeneric(r.header.datestamp.text())
                                        //leave out GlobalRecordInfo update, no need to reflect it twice since we keep the package structure internally
                                        //jump to packageReconcile which includes packageConv - check if there is a package, otherwise, update package data
                                        tippsToNotify << createOrUpdatePackageOAI(r.metadata.gokb.package)
                                        if(recordTimestamp.getTime() > maxTimestamp)
                                            maxTimestamp = recordTimestamp.getTime()
                                    }
                                    break
                                case RECTYPE_TIPP:
                                    List<String> tippUUIDsOnPage = [], platformsOnPage = [], titleInstancesOnPage = [], packageUUIDs = listOAI.'**'.findAll { node ->
                                        node.name() == "package"
                                    }.collect { node -> node.'@uuid'.text() }
                                    Map<String,Map<String,Object>> tippNodesOnPage = [:]
                                    //make package check: which ones do exist in LAS:eR at all, which do not?
                                    Set<Package> packagesExisting = Package.findAllByGokbIdInList(packageUUIDs)
                                    Map<String,Object> updateMap = collectDataToUpdate(listOAI,packagesExisting)
                                    updateMap.tippsOnPage.each { tippNode ->
                                        tippUUIDsOnPage << tippNode.'@uuid'.text()
                                        tippNodesOnPage.put(tippNode.'@uuid'.text(),tippConv(tippNode))
                                    }
                                    updateMap.platformsOnPage.each { platformNode ->
                                        platformsOnPage << platformNode.'@uuid'.text()
                                    }
                                    updateMap.titleInstancesOnPage.each { titleInstanceNode ->
                                        titleInstancesOnPage << titleInstanceNode.'@uuid'.text()
                                    }
                                    updateTitleInstances(updateMap.titleInstancesOnPage)
                                    updateProviders(updateMap.providersOnPage)
                                    updatePlatforms(updateMap.platformsOnPage)
                                    if(updateMap.maxTimestamp > maxTimestamp)
                                        maxTimestamp = updateMap.maxTimestamp
                                    log.info("fetching objects for keys ...")
                                    Map<String,Platform> newPlatforms = [:]
                                    if(platformsOnPage) {
                                        Platform.findAllByGokbIdInList(platformsOnPage).each { Platform plat ->
                                            newPlatforms.put(plat.gokbId,plat)
                                        }
                                    }
                                    Map<String,TitleInstance> newTitleInstances = [:]
                                    if(titleInstancesOnPage) {
                                        //may cause crashes when processing 5 000 000 entries
                                        TitleInstance.findAllByGokbIdInList(titleInstancesOnPage).each { TitleInstance ti ->
                                            newTitleInstances.put(ti.gokbId,ti)
                                        }
                                    }
                                    //process existing ones
                                    if(tippUUIDsOnPage) {
                                        Set<TitleInstancePackagePlatform> existingTIPPs = TitleInstancePackagePlatform.findAllByGokbIdInList(tippUUIDsOnPage)
                                        tippsToNotify.addAll(processTippPage(existingTIPPs,tippNodesOnPage,packagesExisting,newPlatforms,newTitleInstances))
                                    }
                                    else if(!tippUUIDsOnPage) {
                                        log.info("no tipp UUIDs on page???")
                                    }
                                    //add new ones
                                    packageUUIDs.each { String uuid ->
                                        if(!packagesExisting.find{ Package pkg -> pkg.gokbId == uuid }) {
                                            NodeChildren packageRecord = fetchRecordOAI('packages',[verb:'GetRecord', identifier:uuid])
                                            updateNonPackageData(packageRecord)
                                            createOrUpdatePackageOAI(packageRecord.record.metadata.gokb.package)
                                        }
                                    }
                                    //log.debug(packageUUIDs.toListString())
                                    break
                                default: log.error("unimplemented record type handling")
                                    break
                            }
                            if(listOAI.resumptionToken.size() > 0 && listOAI.resumptionToken.text().length() > 0) {
                                resumption = listOAI.resumptionToken
                                log.info("Continue with next iteration, token: ${resumption}")
                                //globalService.cleanUpGorm()
                            }
                            else
                                more = false
                        }
                        log.info("Endloop")
                    }
                    if(maxTimestamp+1000 > oldDate.time) { // +1000 (i.e. 1 sec) because otherwise, the last record is always dumped in list
                        log.info("all OAI info fetched, timestamp ${source.haveUpTo} updated to ${DateUtils.getSDF_NoTime().format(new Date(maxTimestamp+1000))}, notifying dependent entitlements ...")
                        source.haveUpTo = new Date(maxTimestamp+1000)
                        source.save()
                    }
                    else {
                        log.info("all OAI info fetched, no records to update. Leaving timestamp as is ...")
                    }
                    notifyDependencies(tippsToNotify)
                    log.info("sync job finished")
                    SystemEvent.createEvent('GSSS_OAI_COMPLETE',['jobId',source.id])
                }
                catch (Exception e) {
                    SystemEvent.createEvent('GSSS_OAI_ERROR',['jobId':source.id])
                    log.error("sync job has failed, please consult stacktrace as follows: ",e)
                }
            }
            else if(source.type == "JSON") {
                try {
                    SystemEvent.createEvent('GSSS_JSON_START',['jobId':source.id])
                    Thread.currentThread().setName("GlobalDataSync_Json")
                    this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                    Date oldDate = source.haveUpTo
                    log.info("getting records from job #${source.id} with uri ${source.uri} since ${oldDate}")
                    SimpleDateFormat sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
                    String componentType
                    /*
                        structure:
                        { packageUUID: [
                            diffs of tipp 1 concerned,
                            diffs of tipp 2 concerned,
                            ...
                            diffs of tipp n concerned
                            ]
                        }
                     */
                    switch(source.rectype) {
                        case RECTYPE_PACKAGE: componentType = 'Package'
                            break
                        case RECTYPE_TIPP: componentType = 'TitleInstancePackagePlatform'
                            //do prequest: are we needing the scroll api?
                            Map<String,Object> result
                            //5000 records because of local testing ability
                            result = fetchRecordJSON(false,[componentType:componentType,changedSince:sdf.format(oldDate),max:5000])
                            if(result && result.count >= 5000) {
                                String scrollId
                                boolean more = true
                                while(more) {
                                    //actually, scrollId alone should do the trick but tests revealed that other parameters are necessary, too, because of current workaround solution
                                    log.debug("using scrollId ${scrollId}")
                                    if(scrollId) {
                                        result = fetchRecordJSON(true, [component_type: componentType, changedSince: sdf.format(oldDate), scrollId: scrollId])
                                    }
                                    else
                                    {
                                        result = fetchRecordJSON(true,[component_type: componentType,changedSince:sdf.format(oldDate)])
                                    }
                                    if(result.count > 0) {
                                        updateRecords(result.records)
                                        if(result.hasMoreRecords) {
                                            scrollId = result.scrollId
                                        }
                                        else {
                                            more = false
                                        }
                                    }
                                    else {
                                        //workaround until GOKb-ES migration is done and hopefully works ...
                                        if(result.hasMoreRecords) {
                                            scrollId = result.scrollId
                                            log.info("page is empty, turning to next page ...")
                                        }
                                        else {
                                            more = false
                                            log.info("no records updated - leaving everything as is ...")
                                        }
                                    }
                                }
                            }
                            else if(result && result.count > 0 && result.count < 5000) {
                                updateRecords(result.records)
                            }
                            else {
                                log.info("no records updated - leaving everything as is ...")
                            }
                            break
                    }
                    if(maxTimestamp+1000 > source.haveUpTo.getTime()) {
                        log.debug("old ${sdf.format(source.haveUpTo)}")
                        source.haveUpTo = new Date(maxTimestamp+1000)
                        log.debug("new ${sdf.format(source.haveUpTo)}")
                        source.save()
                    }
                    if(packagesToNotify.keySet().size() > 0) {
                        log.info("notifying subscriptions ...")
                        trackPackageHistory()
                        //get subscription packages and their respective holders, parent level only!
                        String query = 'select oo.org,sp from SubscriptionPackage sp join sp.pkg pkg ' +
                                'join sp.subscription s ' +
                                'join s.orgRelations oo ' +
                                'where s.instanceOf = null and pkg.gokbId in (:packages) ' +
                                'and oo.roleType in (:roleTypes)'
                        List subPkgHolders = SubscriptionPackage.executeQuery(query,[packages:packagesToNotify.keySet(),roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER]])
                        subPkgHolders.each { row ->
                            Org org = (Org) row[0]
                            SubscriptionPackage sp = (SubscriptionPackage) row[1]
                            autoAcceptPendingChanges(org,sp)
                            //nonAutoAcceptPendingChanges(org, sp)
                        }

                    }
                    else {
                        log.info("no diffs recorded ...")
                    }
                    log.info("sync job finished")
                    SystemEvent.createEvent('GSSS_JSON_COMPLETE',['jobId',source.id])
                }
                catch (Exception e) {
                    SystemEvent.createEvent('GSSS_JSON_ERROR',['jobId':source.id])
                    log.error("sync job has failed, please consult stacktrace as follows: ",e)
                }
            }
        }
        running = false
    }

    //Used for Sync with Json
    void updateRecords(List<Map> records) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        Set<String> platformUUIDs = records.collect { Map tipp -> tipp.hostPlatformUuid } as Set<String>
        Set<String> packageUUIDs = records.collect { Map tipp -> tipp.tippPackageUuid } as Set<String>
        Set<String> tippUUIDs = records.collect { Map tipp -> tipp.uuid } as Set<String>
        Map<String,Package> packagesOnPage = [:]
        Map<String,Platform> platformsOnPage = [:]

        //packageUUIDs is null if package have no tipps
        Set<String> existingPackageUUIDs = packageUUIDs ? Platform.executeQuery('select pkg.gokbId from Package pkg where pkg.gokbId in (:pkgUUIDs)',[pkgUUIDs:packageUUIDs]) : []
        Map<String,TitleInstancePackagePlatform> tippsInLaser = [:]
        //collect existing TIPPs
        if(tippUUIDs) {
            TitleInstancePackagePlatform.findAllByGokbIdInList(tippUUIDs.toList()).each { TitleInstancePackagePlatform tipp ->
                tippsInLaser.put(tipp.gokbId, tipp)
            }
        }
        //create or update packages
        packageUUIDs.each { String packageUUID ->
            try {
                packagesOnPage.put(packageUUID,createOrUpdatePackage(packageUUID))
            }
            catch (SyncException e) {
                log.error("Error on updating package ${packageUUID}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:packageUUID])
            }
        }
        //create or update platforms
        platformUUIDs.each { String platformUUID ->
            try {
                platformsOnPage.put(platformUUID,createOrUpdatePlatformJSON(platformUUID))
            }
            catch (SyncException e) {
                log.error("Error on updating platform ${platformUUID}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformUUID])
            }
        }

        records.eachWithIndex { Map tipp, int idx ->
            log.debug("now processing entry #${idx}")
            try {
                Map<String,Object> updatedTIPP = [
                    titleType: tipp.titleType,
                    name: tipp.name ?: tipp.tippTitleName,
                    packageUUID: tipp.tippPackageUuid ?: null,
                    platformUUID: tipp.hostPlatformUuid ?: null,
                    titlePublishers: [],
                    publisherName: tipp.publisherName,
                    firstAuthor: tipp.titleFirstAuthor ?: null,
                    firstEditor: tipp.titleFirstEditor ?: null,
                    dateFirstInPrint: tipp.dateFirstInPrint ? DateUtils.parseDateGeneric(tipp.dateFirstInPrint) : null,
                    dateFirstOnline: tipp.dateFirstOnline ? DateUtils.parseDateGeneric(tipp.dateFirstOnline) : null,
                    imprint: tipp.titleImprint ?: null,
                    status: tipp.status,
                    seriesName: tipp.series ?: null,
                    subjectReference: tipp.subjectArea ?: null,
                    volume: tipp.titleVolumeNumber ?: null,
                    coverages: [],
                    priceItems: [],
                    hostPlatformURL: tipp.url ?: null,
                    identifiers: [],
                    history: [],
                    uuid: tipp.uuid,
                    accessStartDate : tipp.accessStartDate ? DateUtils.parseDateGeneric(tipp.accessStartDate) : null,
                    accessEndDate   : tipp.accessEndDate ? DateUtils.parseDateGeneric(tipp.accessEndDate) : null,
                    medium: tipp.tippTitleMedium
                ]
                if(tipp.titleType == 'Journal') {
                    tipp.coverage.each { cov ->
                        updatedTIPP.coverages << [
                                startDate: cov.startDate ? DateUtils.parseDateGeneric(cov.startDate) : null,
                                endDate: cov.endDate ? DateUtils.parseDateGeneric(cov.endDate) : null,
                                startVolume: cov.startVolume ?: null,
                                endVolume: cov.endVolume ?: null,
                                startIssue: cov.startIssue ?: null,
                                endIssue: cov.endIssue ?: null,
                                coverageDepth: cov.coverageDepth ?: null,
                                coverageNote: cov.coverageNote ?: null,
                                embargo: cov.embargo ?: null
                        ]
                    }
                    updatedTIPP.coverages = updatedTIPP.coverages.toSorted { a, b -> a.startDate <=> b.startDate }
                }
                tipp.prices.each { price ->
                    updatedTIPP.priceItems << [
                            listPrice: escapeService.parseFinancialValue(price.amount),
                            startDate: DateUtils.parseDateGeneric(price.startDate),
                            endDate: DateUtils.parseDateGeneric(price.endDate),
                            listCurrency: currency.get(price.currency)
                    ]
                }
                tipp.titlePublishers.each { publisher ->
                    updatedTIPP.titlePublishers << [uuid:publisher.uuid,name:publisher.name]
                }
                tipp.identifiers.each { identifier ->
                    updatedTIPP.identifiers << [namespace:identifier.namespace, value: identifier.value, name_de: identifier.namespaceName]
                }
                tipp.titleHistory.each { historyEvent ->
                    updatedTIPP.history << [date:DateUtils.parseDateGeneric(historyEvent.date),from:historyEvent.from,to:historyEvent.to]
                }
                if(updatedTIPP.packageUUID in existingPackageUUIDs) {
                    Map<String,Object> diffs = createOrUpdateTIPP(tippsInLaser.get(updatedTIPP.uuid),updatedTIPP,packagesOnPage,platformsOnPage)
                    Set<Map<String,Object>> diffsOfPackage = packagesToNotify.get(updatedTIPP.packageUUID)
                    if(!diffsOfPackage) {
                        diffsOfPackage = []
                    }
                    diffsOfPackage << diffs
                    if(pkgPropDiffsContainer.get(updatedTIPP.packageUUID)) {
                        diffsOfPackage.addAll(pkgPropDiffsContainer.get(updatedTIPP.packageUUID))
                    }//test with set, otherwise make check
                    packagesToNotify.put(updatedTIPP.packageUUID,diffsOfPackage)
                }
                else {
                    addNewTIPP(packagesOnPage.get(updatedTIPP.packageUUID), updatedTIPP, platformsOnPage,null)
                }
                Date lastUpdatedTime = DateUtils.parseDateGeneric(tipp.lastUpdatedDisplay)
                if(lastUpdatedTime.getTime() > maxTimestamp) {
                    maxTimestamp = lastUpdatedTime.getTime()
                }
            }
            catch (SyncException e) {
                log.error("Error on updating tipp ${tipp.uuid}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[tippRecordKey:tipp.uuid])
            }
        }
    }

    /**
     * This records the package changes so that subscription holders may decide whether they apply them or not
     * @param packagesToTrack
     */
    //Used for Sync with Json
    void trackPackageHistory() {
        //Package.withSession { Session sess ->
            //loop through all packages
            packagesToNotify.each { String packageUUID, Set<Map<String,Object>> diffsOfPackage ->
                if(diffsOfPackage.find { Map<String,Object> diff -> diff.event in [PendingChangeConfiguration.NEW_TITLE,PendingChangeConfiguration.TITLE_DELETED] }) {
                    int newCount = TitleInstancePackagePlatform.executeQuery('select count(tipp.id) from TitleInstancePackagePlatform tipp where tipp.pkg.gokbId = :gokbId',[gokbId:packageUUID])[0]
                    String newValue = newCount.toString()
                    String oldValue = initialPackagesCounter.get(packageUUID).toString()
                    PendingChange.construct([msgToken:PendingChangeConfiguration.PACKAGE_TIPP_COUNT_CHANGED,target:Package.findByGokbId(packageUUID),status:RDStore.PENDING_CHANGE_HISTORY,prop:"tippCount",newValue:newValue,oldValue:oldValue])
                }
                //println("diffsOfPackage:"+diffsOfPackage)
                diffsOfPackage.each { Map<String,Object> diff ->
                    log.debug(diff.toMapString())
                    //[event:update, target:de.laser.TitleInstancePackagePlatform : 196477, diffs:[[prop:price, priceDiffs:[[event:add, target:de.laser.finance.PriceItem : 10791]]]]]
                    switch(diff.event) {
                        case 'add': PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_TITLE,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY])
                            break
                        case 'update':
                            diff.diffs.each { tippDiff ->
                                switch(tippDiff.prop) {
                                    case 'coverage': tippDiff.covDiffs.each { covEntry ->
                                        switch(covEntry.event) {
                                            case 'add': PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_COVERAGE,target:covEntry.target,status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                            case 'update': covEntry.diffs.each { covDiff ->
                                                    PendingChange.construct([msgToken: PendingChangeConfiguration.COVERAGE_UPDATED, target: covEntry.target, status: RDStore.PENDING_CHANGE_HISTORY, prop: covDiff.prop, newValue: covDiff.newValue, oldValue: covDiff.oldValue])
                                                    //log.debug("tippDiff.covDiffs.covDiff: " + covDiff)
                                                }
                                                break
                                            case 'delete': JSON oldMap = covEntry.target.properties as JSON
                                                PendingChange.construct([msgToken:PendingChangeConfiguration.COVERAGE_DELETED, target:covEntry.targetParent, oldValue: oldMap.toString() , status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                        }
                                    }
                                        break
                                    case 'price': tippDiff.priceDiffs.each { priceEntry ->
                                        switch(priceEntry.event) {
                                            case 'add': PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_PRICE,target:priceEntry.target,status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                            case 'update':
                                                priceEntry.diffs.each { priceDiff ->
                                                    PendingChange.construct([msgToken: PendingChangeConfiguration.PRICE_UPDATED, target: priceEntry.target, status: RDStore.PENDING_CHANGE_HISTORY, prop: priceDiff.prop, newValue: priceDiff.newValue, oldValue: priceDiff.oldValue])
                                                }
                                                //log.debug("tippDiff.priceDiffs: "+ priceEntry)
                                                break
                                            case 'delete': JSON oldMap = priceEntry.target.properties as JSON
                                                PendingChange.construct([msgToken:PendingChangeConfiguration.PRICE_DELETED,target:priceEntry.targetParent,oldValue:oldMap.toString(),status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                        }
                                    }
                                        break
                                    default:
                                        PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_UPDATED,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY,prop:tippDiff.prop,newValue:tippDiff.newValue,oldValue:tippDiff.oldValue])
                                        break
                                }
                            }
                            break
                        case 'delete': PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_DELETED,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY])
                            break
                    }
                    //PendingChange.construct([msgToken,target,status,prop,newValue,oldValue])
                }
                //sess.flush()
            }
        //}
    }

    void autoAcceptPendingChanges(Org contextOrg, SubscriptionPackage subPkg) {
        //get for each subscription package the tokens which should be accepted
        String query = 'select pcc.settingKey from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where pcc.settingValue = :accept and sp = :sp and pcc.settingKey not in (:excludes)'
        List<String> pendingChangeConfigurations = PendingChangeConfiguration.executeQuery(query,[accept:RDStore.PENDING_CHANGE_CONFIG_ACCEPT,sp:subPkg,excludes:[PendingChangeConfiguration.NEW_PRICE,PendingChangeConfiguration.PRICE_DELETED,PendingChangeConfiguration.PRICE_UPDATED]])
        if(pendingChangeConfigurations) {
            Map<String,Object> changeParams = [pkg:subPkg.pkg,history:RDStore.PENDING_CHANGE_HISTORY,subscriptionJoin:subPkg.dateCreated,msgTokens:pendingChangeConfigurations]
            Set<PendingChange> newChanges = [],
                               acceptedChanges = PendingChange.findAllByOidAndStatusAndMsgTokenIsNotNull(genericOIDService.getOID(subPkg.subscription),RDStore.PENDING_CHANGE_ACCEPTED)
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            //newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.priceItem pi join pi.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            newChanges.each { PendingChange newChange ->
                boolean processed = false
                if(newChange.tipp) {
                    if(newChange.targetProperty)
                        processed = acceptedChanges.find { PendingChange accepted -> accepted.tipp == newChange.tipp && accepted.msgToken == newChange.msgToken && accepted.targetProperty == newChange.targetProperty } != null
                    else
                        processed = acceptedChanges.find { PendingChange accepted -> accepted.tipp == newChange.tipp && accepted.msgToken == newChange.msgToken } != null
                }
                else if(newChange.tippCoverage) {
                    if(newChange.targetProperty)
                        processed = acceptedChanges.find { PendingChange accepted -> accepted.tippCoverage == newChange.tippCoverage && accepted.msgToken == newChange.msgToken && accepted.targetProperty == newChange.targetProperty } != null
                    else
                        processed = acceptedChanges.find { PendingChange accepted -> accepted.tippCoverage == newChange.tippCoverage && accepted.msgToken == newChange.msgToken } != null
                }
                /*else if(newChange.priceItem && newChange.priceItem.tipp) {
                    processed = acceptedChanges.find { PendingChange accepted -> accepted.priceItem == newChange.priceItem && accepted.msgToken == newChange.msgToken } != null
                }*/

                if(!processed) {
                    /*
                    get each change for each subscribed package and token, fetch issue entitlement equivalent and process the change
                    if a change is being accepted, create a copy with target = subscription of subscription package and oid = the target of the processed change
                     */
                    pendingChangeService.applyPendingChange(newChange,subPkg,contextOrg)
                }
            }
        }
    }


    void nonAutoAcceptPendingChanges(Org contextOrg, SubscriptionPackage subPkg) {
        //get for each subscription package the tokens which should be accepted
        String query = 'select pcc.settingKey from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where pcc.settingValue != :accept and sp = :sp'
        List<String> pendingChangeConfigurations = PendingChangeConfiguration.executeQuery(query,[accept:RDStore.PENDING_CHANGE_CONFIG_ACCEPT,sp:subPkg])
        if(pendingChangeConfigurations) {
            Map<String,Object> changeParams = [pkg:subPkg.pkg,history:RDStore.PENDING_CHANGE_HISTORY,subscriptionJoin:subPkg.dateCreated,msgTokens:pendingChangeConfigurations]
            Set<PendingChange> newChanges = [],
                               acceptedChanges = PendingChange.findAllByOidAndStatusNotEqualAndMsgTokenIsNotNull(genericOIDService.getOID(subPkg.subscription),RDStore.PENDING_CHANGE_ACCEPTED)
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.priceItem pi join pi.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            newChanges.each { PendingChange newChange ->
                boolean processed = false
                if(newChange.tipp) {
                    processed = acceptedChanges.find { PendingChange accepted -> accepted.tipp == newChange.tipp && accepted.msgToken == newChange.msgToken } != null
                }
                else if(newChange.tippCoverage) {
                    processed = acceptedChanges.find { PendingChange accepted -> accepted.tippCoverage == newChange.tippCoverage && accepted.msgToken == newChange.msgToken } != null
                }
                else if(newChange.priceItem && newChange.priceItem.tipp) {
                    processed = acceptedChanges.find { PendingChange accepted -> accepted.priceItem == newChange.priceItem && accepted.msgToken == newChange.msgToken } != null
                }

                if(!processed) {

                    PendingChangeConfiguration pendingChangeConfiguration = subPkg.pendingChangeConfig.find {PendingChangeConfiguration pcc -> pcc.settingKey == newChange.msgToken}
                    if(pendingChangeConfiguration){
                        def target
                        if(newChange.tipp)
                            target = newChange.tipp
                        else if(newChange.tippCoverage)
                            target = newChange.tippCoverage
                        else if(newChange.priceItem && newChange.priceItem.tipp)
                            target = newChange.priceItem
                        if(target) {
                            Map<String,Object> changeMap = [target:target,
                                                            oid: genericOIDService.getOID(subPkg.subscription),
                                                            oldValue: newChange.oldValue,
                                                            newValue: newChange.newValue,
                                                            prop: newChange.targetProperty]
                                changeNotificationService.determinePendingChangeBehavior(changeMap, newChange.msgToken, subPkg)
                        }
                    }
                }
            }
        }
    }

    Map<String,Object> collectDataToUpdate(GPathResult listOAI, Set<Package> packagesExisting) {
        Set tippsOnPage = [], platformsOnPage = [], titleInstancesOnPage = [], providersOnPage = []
        listOAI.record.each { r ->
            Date recordTimestamp = DateUtils.parseDateGeneric(r.header.datestamp.text())
            //log.debug("timestamp of record ${r.header.datestamp.text()} vs. ${new Date(maxTimestamp)}")
            if(recordTimestamp.getTime() > maxTimestamp) {
                //log.debug("${maxTimestamp} updated to ${recordTimestamp.getTime()}")
                maxTimestamp = recordTimestamp.getTime()
            }
            NodeChildren tippNode = r.metadata.gokb.tipp
            if(packagesExisting.find{ Package pkg -> pkg.gokbId == tippNode.package.'@uuid'.text() }) {
                tippsOnPage << tippNode
                platformsOnPage << tippNode.platform
                providersOnPage << tippNode.package.provider
                titleInstancesOnPage << tippNode.title
            }
        }
        [tippsOnPage:tippsOnPage, providersOnPage: providersOnPage, platformsOnPage: platformsOnPage, titleInstancesOnPage: titleInstancesOnPage]
    }

    // Used for Sync with OAI
    List<List<Map<String,Object>>> processTippPage(Set<TitleInstancePackagePlatform> existingTIPPs, Map<String,Map<String,Object>> tippNodesOnPage, Set<Package> packagesExisting, Map<String,Platform> newPlatforms, Map<String,TitleInstance> newTitleInstances) {
        Map<String,List<Map<String,Object>>> packagesToNotify = [:]
        tippNodesOnPage.each { String tippUUID, Map<String,Object> tippB ->
            TitleInstancePackagePlatform tippA = existingTIPPs.find { TitleInstancePackagePlatform tippA -> tippA.gokbId == tippUUID }
            List<Map<String,Object>> tippDiffsOfPackage = packagesToNotify.get(tippB.packageUUID)
            if (!tippDiffsOfPackage)
                tippDiffsOfPackage = []
            if(tippA) {
                Map<String,Object> tippDiffs = processTippDiffs(tippA, tippB)
                if(tippDiffs)
                    tippDiffsOfPackage << tippDiffs
            }
            else {
                tippA = addNewTIPP(packagesExisting.find { Package pkg -> pkg.gokbId == tippB.packageUUID }, tippB, newPlatforms, newTitleInstances)
                tippDiffsOfPackage << [event: 'add', target: tippA]
            }
            packagesToNotify.put(tippA.pkg.gokbId,tippDiffsOfPackage)
        }
        List<List<Map<String,Object>>> result = []
        packagesToNotify.values().each { List<Map<String,Object>> value ->
            result << value
        }
        result
    }
    //Used for Sync with Json
    Map<String,Object> createOrUpdateTIPP(TitleInstancePackagePlatform tippA,Map tippB, Map<String,Package> newPackages,Map<String,Platform> newPlatforms) {
        Map<String,Object> result = [:]
        //TitleInstancePackagePlatform.withSession { Session sess ->
            if(tippA) {
                //update or delete TIPP
                result.putAll(processTippDiffs(tippA,tippB)) //maybe I have to make some adaptations on tippB!
            }
            else {
                //new TIPP
                TitleInstancePackagePlatform target = addNewTIPP(newPackages.get(tippB.packageUUID), tippB, newPlatforms, null)
                result.event = 'add'
                result.target = target
            }
        //}
        result
    }

    void updateNonPackageData(GPathResult oaiBranch) throws SyncException {
        Set titleNodes = oaiBranch.'**'.findAll { node ->
            node.name() == "title"
        }.toSet()
        updateTitleInstances(titleNodes)
        Set providerNodes = oaiBranch.'**'.findAll { node ->
            node.name() == "nominalProvider"
        }.toSet()
        updateProviders(providerNodes)
        Set platformNodes = oaiBranch.'**'.findAll { node ->
            node.name() in ["nominalPlatform","platform"]
        }.toSet()
        updatePlatforms(platformNodes)
    }

    void updateTitleInstances(Set titleNodes) {
        Set<String> titlesToUpdate = []
        titleNodes.each { title ->
            //temp fix
            if(title.type.text() in ['BookInstance','DatabaseInstance','JournalInstance'])
                titlesToUpdate << title.'@uuid'.text()
            else {
                log.warn("Unimplemented title type ${title.type.text()}")
                SystemEvent.createEvent('GSSS_OAI_WARNING',[titleRecordKey:title.'@uuid'.text()])
            }
        }
        titlesToUpdate.each { String titleUUID ->
            //that may destruct debugging ...
            try {
                Long start = System.currentTimeMillis()
                createOrUpdateTitle(titleUUID)
                //log.debug("execution time: ${System.currentTimeMillis()-start}")
            }
            catch (SyncException e) {
                log.error(e.getStackTrace().toList().toListString())
                SystemEvent.createEvent('GSSS_OAI_WARNING',[titleRecordKey:titleUUID])
            }
        }
    }

    void updateProviders(Set providerNodes) {
        Set<String> providersToUpdate = []
        providerNodes.each { provider ->
            providersToUpdate << provider.'@uuid'.text()
        }
        providersToUpdate.each { String providerUUID ->
            try {
                createOrUpdateProvider(providerUUID)
            }
            catch (SyncException e) {
                log.error(e.getStackTrace().toList().toListString())
                SystemEvent.createEvent('GSSS_OAI_WARNING',[providerRecordKey:providerUUID])
            }
        }
    }

    void updatePlatforms(Set platformNodes) {
        Set<Map<String,String>> platformsToUpdate = []
        platformNodes.each { platform ->
            if (!platformsToUpdate.find { plat -> plat.platformUUID == platform.'@uuid'.text() })
                platformsToUpdate << [gokbId: platform.'@uuid'.text(), name: platform.name.text(), primaryUrl: platform.primaryUrl.text()]
        }
        platformsToUpdate.each { Map<String,String> platformParams ->
            try {
                createOrUpdatePlatform(platformParams)
            }
            catch (SyncException e) {
                log.error(e.getStackTrace().toList().toListString())
                SystemEvent.createEvent('GSSS_OAI_WARNING',[platformRecordKey:platformParams.gokbId])
            }
        }
    }

    /**
     * Looks up for a given OAI-PMH extract if a local record exists or not. If no {@link Package} record exists, it will be
     * created with the given remote record data, otherwise, the local record is going to be updated. The {@link TitleInstancePackagePlatform records}
     * in the {@link Package} will be checked for differences and if there are such, the according fields updated. Same counts for the {@link TIPPCoverage} records
     * in the {@link TitleInstancePackagePlatform}s. If {@link Subscription}s are linked to the {@link Package}, the {@link IssueEntitlement}s (just as their
     * {@link IssueEntitlementCoverage}s) are going to be notified; it is up to the respective subscription tenants to accept the changes or not.
     * Replaces the method GokbDiffEngine.diff and the onNewTipp, onUpdatedTipp and onUnchangedTipp closures
     *
     * @param packageData - A {@link NodeChildren} list containing a OAI-PMH record extract for a given package
     * @return
     */
    List<Map<String,Object>> createOrUpdatePackageOAI(NodeChildren packageData) {
        log.info('converting XML record into map and reconciling new package!')
        List<Map<String,Object>> tippsToNotify = [] //this is the actual return object; a pool within which we will contain all the TIPPs whose IssueEntitlements needs to be notified
        String packageUUID = packageData.'@uuid'.text()
        String packageName = packageData.name.text()
        RefdataValue packageStatus = RefdataValue.getByValueAndCategory(packageData.status.text(), RDConstants.PACKAGE_STATUS)
        RefdataValue packageScope = RefdataValue.getByValueAndCategory(packageData.scope.text(),RDConstants.PACKAGE_SCOPE) //needed?
        RefdataValue packageListStatus = RefdataValue.getByValueAndCategory(packageData.listStatus.text(),RDConstants.PACKAGE_LIST_STATUS) //needed?
        RefdataValue breakable = RefdataValue.getByValueAndCategory(packageData.breakable.text(),RDConstants.PACKAGE_BREAKABLE) //needed?
        RefdataValue consistent = RefdataValue.getByValueAndCategory(packageData.consistent.text(),RDConstants.PACKAGE_CONSISTENT) //needed?
        RefdataValue fixed = RefdataValue.getByValueAndCategory(packageData.fixed.text(),RDConstants.PACKAGE_FIXED) //needed?
        RefdataValue contentType = RefdataValue.getByValueAndCategory(packageData.contentType.text(),RDConstants.PACKAGE_CONTENT_TYPE)
        Date listVerifiedDate = packageData.listVerifiedDate.text() ? DateUtils.parseDateGeneric(packageData.listVerifiedDate.text()) : null
        //result.global = packageData.global.text() needed? not used in packageReconcile
        String providerUUID
        String platformUUID
        if(packageData.nominalProvider) {
            providerUUID = (String) packageData.nominalProvider.'@uuid'.text()
            //lookupOrCreateProvider(providerParams)
        }
        if(packageData.nominalPlatform) {
            platformUUID = (String) packageData.nominalPlatform.'@uuid'.text()
        }
        //ex packageConv, processing TIPPs - conversion necessary because package may be not existent in LAS:eR; then, no comparison is needed any more
        List<Map<String,Object>> tipps = []
        List<String> platformsInPackage = [], titleInstancesInPackage = []
        packageData.TIPPs.TIPP.eachWithIndex { tipp, int ctr ->
            if(tipp.title.type.text() in ['BookInstance','DatabaseInstance','JournalInstance']) {
                tipps << tippConv(tipp)
                platformsInPackage << tipp.platform.'@uuid'.text()
                titleInstancesInPackage << tipp.title.'@uuid'.text()
            }
            else {
                log.warn("see above - unimplemented title type ${tipp.title.type.text()}")
            }
        }
        log.info("Rec conversion for package returns object with name ${packageName} and ${tipps.size()} tipps")
        Package.withNewSession { Session sess ->
            Package result = Package.findByGokbId(packageUUID)
            try {
                log.info("fetching objects for keys ...")
                Map<String,Platform> newPlatforms = [:]
                if(platformsInPackage) {
                    Platform.findAllByGokbIdInList(platformsInPackage).each { Platform plat ->
                        newPlatforms.put(plat.gokbId,plat)
                    }
                }
                Map<String,TitleInstance> newTitleInstances = [:]
                if(titleInstancesInPackage) {
                    //may cause crashes when processing 5 000 000 entries
                    TitleInstance.findAllByGokbIdInList(titleInstancesInPackage).each { TitleInstance ti ->
                        newTitleInstances.put(ti.gokbId,ti)
                    }
                }
                if(result) {
                    //local package exists -> update closure, build up GokbDiffEngine and the horrendous closures
                    log.info("package successfully found, processing LAS:eR id #${result.id}, with GOKb id ${result.gokbId}")
                    if(packageStatus == RDStore.PACKAGE_STATUS_DELETED && result.packageStatus != RDStore.PACKAGE_STATUS_DELETED) {
                        log.info("package #${result.id}, with GOKb id ${result.gokbId} got deleted, mark as deleted all cascaded elements and rapport!")
                        tippsToNotify << [event:"pkgDelete",diffs:[[prop: 'packageStatus', newValue: packageStatus, oldValue: result.packageStatus]],target:result]
                        result.packageStatus = packageStatus
                        if(result.save()) {
                            result.tipps.each { TitleInstancePackagePlatform tippA ->
                                //log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${result.gokbId}")
                                //tippA.status = RDStore.TIPP_STATUS_DELETED
                                tippsToNotify << [event:"delete",target:tippA]
                                //if(!tippA.save())
                                    //throw new SyncException("Error on marking TIPP with UUID ${tippA.gokbId}: ${tippA.errors} as deleted!")
                            }
                            TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :deleted where tipp.pkg = :pkg',[deleted:RDStore.TIPP_STATUS_DELETED,pkg:result])
                        }
                        else {
                            throw new SyncException("Error on marking package ${result.id}: ${result.errors} as deleted!")
                        }
                    }
                    else {
                        Map<String,Object> newPackageProps = [
                                uuid: packageUUID,
                                name: packageName,
                                packageStatus: packageStatus,
                                listVerifiedDate: listVerifiedDate,
                                packageScope: packageScope,
                                packageListStatus: packageListStatus,
                                breakable: breakable,
                                consistent: consistent,
                                fixed: fixed,
                                contentType: contentType
                        ]
                        if(platformUUID) {
                            newPackageProps.nominalPlatform = Platform.findByGokbId(platformUUID)
                        }
                        if(providerUUID) {
                            newPackageProps.contentProvider = Org.findByGokbId(providerUUID)
                        }
                        Set<Map<String,Object>> pkgPropDiffs = getPkgPropDiff(result,newPackageProps)
                        result.name = packageName
                        result.packageStatus = packageStatus
                        result.listVerifiedDate = listVerifiedDate
                        result.packageScope = packageScope //needed?
                        result.packageListStatus = packageListStatus //needed?
                        result.breakable = breakable //needed?
                        result.consistent = consistent //needed?
                        result.fixed = fixed //needed?
                        result.contentType = contentType
                        if(platformUUID)
                            result.nominalPlatform = newPackageProps.nominalPlatform
                        if(providerUUID) {
                            createOrUpdatePackageProvider(newPackageProps.contentProvider,result)
                        }
                        if(result.save()) {
                            if(pkgPropDiffs)
                                tippsToNotify << [event:"pkgPropUpdate",diffs:pkgPropDiffs,target:result]
                            tipps.eachWithIndex { Map<String, Object> tippB, int index ->
                                TitleInstancePackagePlatform tippA = result.tipps.find { TitleInstancePackagePlatform a -> a.gokbId == tippB.uuid }
                                //we have to consider here TIPPs, too, which were deleted but have been reactivated
                                if (tippA) {
                                    Map<String,Object> tippDiffs = processTippDiffs(tippA,tippB)
                                    if(tippDiffs)
                                        tippsToNotify << tippDiffs
                                }
                                else {
                                    //ex newTippClosure
                                    TitleInstancePackagePlatform target = addNewTIPP(result, tippB, newPlatforms, newTitleInstances)
                                    tippsToNotify << [event: 'add', target: target]
                                }
                            }
                        }
                        else {
                            throw new SyncException("Error on updating package ${result.id}: ${result.errors}")
                        }
                    }
                }
                else {
                    //local package does not exist -> insert new data
                    log.info("creating new package ...")
                    //Package.withTransaction { TransactionStatus ts ->
                        result = new Package(
                                gokbId: packageData.'@uuid'.text(),
                                name: packageName,
                                listVerifiedDate: listVerifiedDate,
                                packageStatus: packageStatus,
                                packageScope: packageScope, //needed?
                                packageListStatus: packageListStatus, //needed?
                                breakable: breakable, //needed?
                                consistent: consistent, //needed?
                                fixed: fixed, //needed?
                                contentType: contentType
                        )
                        if(result.save()) {
                            if(providerUUID) {
                                Org provider = Org.findByGokbId(providerUUID)
                                createOrUpdatePackageProvider(provider,result)
                            }
                            if(platformUUID) {
                                result.nominalPlatform = Platform.findByGokbId(platformUUID)
                            }
                            tipps.eachWithIndex { Map<String,Object> tipp, int idx ->
                                log.info("now processing entry #${idx} with UUID ${tipp.uuid}")
                                addNewTIPP(result,tipp,newPlatforms,newTitleInstances)
                            }
                        }
                        else {
                            throw new SyncException("Error on saving package: ${result.errors}")
                        }
                    //}
                }
                log.info("before processing identifiers - identifier count: ${packageData.identifiers.identifier.size()}")
                packageData.identifiers.identifier.each { id ->
                    if(id.'@namespace'.text().toLowerCase() != 'originediturl') {
                        Identifier.construct([namespace: id.'@namespace'.text(), value: id.'@value'.text(), reference: result, isUnique: false, nsType: Package.class.name])
                    }
                }
                log.info("after processing identifiers")
            }
            catch (Exception e) {
                log.error("Error on updating package ${result.id} ... rollback!")
                e.printStackTrace()
            }
        }
        log.debug("returning tippsToNotify")
        tippsToNotify
    }

    Package createOrUpdatePackage(String packageUUID) throws SyncException {
        Map<String,Object> packageJSON = fetchRecordJSON(false,[uuid: packageUUID])
        if(packageJSON.records) {
            Map packageRecord = (Map) packageJSON.records[0]
            Package result = Package.findByGokbId(packageUUID)
            Date lastUpdatedDisplay = DateUtils.parseDateGeneric(packageRecord.lastUpdatedDisplay)
            if(!result || result?.lastUpdated < lastUpdatedDisplay) {
                log.info("package record loaded, reconciling package record for UUID ${packageUUID}")
                RefdataValue packageStatus = RefdataValue.getByValueAndCategory(packageRecord.status, RDConstants.PACKAGE_STATUS)
                RefdataValue packageListStatus = RefdataValue.getByValueAndCategory(packageRecord.listStatus,RDConstants.PACKAGE_LIST_STATUS)
                RefdataValue contentType = packageRecord.contentType ? RefdataValue.getByValueAndCategory(packageRecord.contentType,RDConstants.PACKAGE_CONTENT_TYPE) : null
                Date listVerifiedDate = packageRecord.listVerifiedDate ? DateUtils.parseDateGeneric(packageRecord.listVerifiedDate) : null
                Map<String,Object> newPackageProps = [
                        uuid: packageUUID,
                        name: packageRecord.name,
                        packageStatus: packageStatus,
                        listVerifiedDate: listVerifiedDate,
                        packageListStatus: packageListStatus,
                        contentType: packageRecord.contentType
                ]
                if(result) {
                    if(packageStatus == RDStore.PACKAGE_STATUS_DELETED && result.packageStatus != RDStore.PACKAGE_STATUS_DELETED) {
                        log.info("package #${result.id}, with GOKb id ${result.gokbId} got deleted, mark as deleted and rapport!")
                        result.packageStatus = packageStatus
                    }
                    else {
                        if(packageRecord.nominalPlatformUuid) {
                            newPackageProps.nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                        }
                        if(packageRecord.providerUuid) {
                            newPackageProps.contentProvider = Org.findByGokbId(packageRecord.providerUuid)
                        }
                        Set<Map<String,Object>> pkgPropDiffs = getPkgPropDiff(result, newPackageProps)
                        if(pkgPropDiffs) {
                            pkgPropDiffsContainer.put(packageUUID, [event: "pkgPropUpdate", diffs: pkgPropDiffs, target: result])
                        }

                        if(!initialPackagesCounter.get(packageUUID))
                            initialPackagesCounter.put(packageUUID,TitleInstancePackagePlatform.executeQuery('select count(tipp.id) from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg',[pkg:result])[0] as Integer)
                    }
                }
                else {
                    result = new Package(gokbId: packageRecord.uuid)
                }
                result.name = packageRecord.name
                result.packageStatus = packageStatus
                result.listVerifiedDate = listVerifiedDate
                result.packageListStatus = packageListStatus
                result.contentType = contentType
                if(result.save()) {
                    if(packageRecord.nominalPlatformUuid) {
                        result.nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                    }
                    if(packageRecord.providerUuid) {
                        try {
                            Org provider = createOrUpdateProviderJSON(packageRecord.providerUuid)
                            createOrUpdatePackageProvider(provider,result)
                        }
                        catch (SyncException e) {
                            throw e
                        }
                    }
                    packageRecord.identifiers.each { id ->
                        if(id.namespace.toLowerCase() != 'originediturl') {
                            Identifier.construct([namespace: id.namespace, value: id.value, reference: result, isUnique: false, nsType: Package.class.name])
                        }
                    }
                }
                else {
                    throw new SyncException(result.errors)
                }
            }
            result
        }
        else {
            throw new SyncException("Package creation for ${packageUUID} called without record data! PANIC!")
        }
    }

    /**
     * Checks for a given UUID if a {@link TitleInstance} is existing in the database, if it does not exist, it will be created.
     * Replaces the former updatedTitleAfterPackageReconcile, titleConv and titleReconcile closure
     *
     * @param titleUUID - the GOKb UUID of the {@link TitleInstance} to create or update
     * @return the new or updated {@link TitleInstance}
     */
    TitleInstance createOrUpdateTitle(String titleUUID) throws SyncException {
        GPathResult titleOAI = fetchRecordOAI('titles',[verb:'GetRecord', identifier:titleUUID])
        if(titleOAI) {
            TitleInstance.withNewSession { Session session ->
                GPathResult titleRecord = titleOAI.record.metadata.gokb.title
                log.info("title record loaded, converting XML record and reconciling title record for UUID ${titleUUID} ...")
                TitleInstance titleInstance = TitleInstance.findByGokbId(titleUUID)
                if(titleRecord.type.text()) {
                    RefdataValue status = titleStatus.get(titleRecord.status.text())
                    //titleRecord.medium.text()
                    RefdataValue medium = titleMedium.get(titleRecord.medium.text()) //misunderstandable mapping ...
                    try {
                        switch(titleRecord.type.text()) {
                            case 'BookInstance':
                                Integer editionNumber = null
                                if(titleRecord.editionNumber.text()) {
                                    try {
                                        editionNumber = Integer.parseInt(titleRecord.editionNumber.text())
                                    }
                                    catch (NumberFormatException e) {
                                        log.warn("${titleUUID} has invalid edition number supplied: ${titleRecord.editionNumber.text()}")
                                        log.error(e.getMessage())
                                        editionNumber = null
                                    }
                                }
                                Map<String,Object> newTitleParams = [gokbId:titleUUID,
                                                                     medium: medium,
                                                                     status: status,
                                                                     title:titleRecord.name.text(),
                                                                     editionNumber: editionNumber,
                                                                     editionDifferentiator: titleRecord.editionDifferentiator.text() ?: null,
                                                                     editionStatement: titleRecord.editionStatement.text() ?: null,
                                                                     volume: titleRecord.volumeNumber.text() ?: null,
                                                                     dateFirstInPrint: titleRecord.dateFirstInPrint ? DateUtils.parseDateGeneric(titleRecord.dateFirstInPrint.text()) : null,
                                                                     dateFirstOnline: titleRecord.dateFirstOnline ? DateUtils.parseDateGeneric(titleRecord.dateFirstOnline.text()) : null,
                                                                     firstAuthor: titleRecord.firstAuthor.text() ?: null,
                                                                     firstEditor: titleRecord.firstEditor.text() ?: null]
                                titleInstance = titleInstance ? (BookInstance) titleInstance :  BookInstance.construct(newTitleParams)
                                titleInstance.editionNumber = editionNumber
                                titleInstance.editionDifferentiator = titleRecord.editionDifferentiator.text() ?: null
                                titleInstance.editionStatement = titleRecord.editionStatement.text() ?: null
                                titleInstance.volume = titleRecord.volumeNumber.text() ?: null
                                titleInstance.dateFirstInPrint = titleRecord.dateFirstInPrint ? DateUtils.parseDateGeneric(titleRecord.dateFirstInPrint.text()) : null
                                titleInstance.dateFirstOnline = titleRecord.dateFirstOnline ? DateUtils.parseDateGeneric(titleRecord.dateFirstOnline.text()) : null
                                titleInstance.firstAuthor = titleRecord.firstAuthor.text() ?: null
                                titleInstance.firstEditor = titleRecord.firstEditor.text() ?: null
                                break
                            case 'DatabaseInstance': titleInstance = titleInstance ? (DatabaseInstance) titleInstance : DatabaseInstance.construct([gokbId:titleUUID,title:titleRecord.name.text(),medium: medium,status: status])
                                break
                            case 'JournalInstance': titleInstance = titleInstance ? (JournalInstance) titleInstance : JournalInstance.construct([gokbId:titleUUID,title:titleRecord.name.text(),medium: medium,status: status])
                                break
                        }
                    }
                    catch (GroovyCastException e) {
                        log.error("Title type mismatch! This should not be possible! Inform GOKb team! -> ${titleInstance.gokbId} is corrupt!")
                        log.error(e.getMessage())
                        SystemEvent.createEvent('GSSS_OAI_WARNING',[titleInstance:titleInstance.gokbId,errorType:"titleTypeMismatch"])
                    }
                    //this is taken only if object has been persisted because of other transaction
                    log.debug("processing ${titleInstance.title} before update")
                    titleInstance.title = titleRecord.name.text()
                    titleInstance.medium = medium
                    titleInstance.status = status
                    if(titleInstance.save()) {
                        //titleInstance.refresh()
                        if(titleRecord.publishers) {
                            //OrgRole.withTransaction { TransactionStatus ts ->
                                OrgRole.executeUpdate('delete from OrgRole oo where oo.title = :titleInstance',[titleInstance: titleInstance])
                                titleRecord.publishers.publisher.each { pubData ->
                                    Map<String,Object> publisherParams = [
                                            name: pubData.name.text(),
                                            //status: RefdataValue.getByValueAndCategory(pubData.status.text(),RefdataCategory.ORG_STATUS), -> is for combo type
                                            gokbId: pubData.'@uuid'.text()
                                    ]
                                    Set<Map<String,String>> identifiers = []
                                    pubData.identifiers.identifier.each { identifier ->
                                        //the filter is temporary, should be excluded ...
                                        if(identifier.'@namespace'.text().toLowerCase() != 'originediturl') {
                                            identifiers << [namespace:(String) identifier.'@namespace'.text(),value:(String) identifier.'@value'.text()]
                                        }
                                    }
                                    publisherParams.identifiers = identifiers
                                    Org publisher = lookupOrCreateTitlePublisher(publisherParams)
                                    //ex OrgRole.assertOrgTitleLink
                                    OrgRole titleLink = OrgRole.findByTitleAndOrgAndRoleType(titleInstance,publisher,RDStore.OR_PUBLISHER)
                                    if(!titleLink) {
                                        titleLink = new OrgRole(title:titleInstance,org:publisher,roleType:RDStore.OR_PUBLISHER,isShared:false)
                                        /*
                                        its usage / relevance for LAS:eR is unclear for the moment, must be clarified
                                        if(pubData.startDate)
                                            titleLink.startDate = DateUtil.parseDateGeneric(pubData.startDate.text())
                                        if(pubData.endDate)
                                            titleLink.endDate = DateUtil.parseDateGeneric(pubData.endDate.text())
                                        */
                                        if(!titleLink.save())
                                            throw new SyncException("Error on creating title link: ${titleLink.errors}")
                                    }
                                }
                            //}
                        }
                        if(titleRecord.identifiers) {
                            //I hate this solution ... wrestlers of GOKb stating that Identifiers do not need UUIDs were stronger.
                            //Identifier.withTransaction { TransactionStatus ts ->
                                if(titleInstance.ids){
                                    Identifier.executeUpdate('delete from Identifier i where i.ti = :title',[title:titleInstance]) //damn those wrestlers ...
                                }
                                titleRecord.identifiers.identifier.each { idData ->
                                    if(idData.'@namespace'.text().toLowerCase() != 'originediturl') {
                                        Identifier.construct([namespace: idData.'@namespace'.text(), value: idData.'@value'.text(), reference: titleInstance, isUnique: false, nsType: TitleInstance.class.name])
                                        //ts.flush()
                                    }
                                }
                            //}
                        }
                        if(titleRecord.history) {
                            //TitleHistoryEvent.withTransaction { TransactionStatus ts ->
                                titleRecord.history.historyEvent.each { eventData ->
                                    if(eventData.date.text()) {
                                        Set<TitleInstance> from = [], to = []
                                        eventData.from.each { fromEv ->
                                            try {
                                                from << createOrUpdateHistoryParticipant(fromEv,titleInstance.class.name)
                                            }
                                            catch (SyncException e) {
                                                throw e
                                            }
                                        }
                                        eventData.to.each { toEv ->
                                            try {
                                                to << createOrUpdateHistoryParticipant(toEv,titleInstance.class.name)
                                            }
                                            catch (SyncException e) {
                                                throw e
                                            }
                                        }
                                        Date eventDate = DateUtils.parseDateGeneric(eventData.date.text())
                                        String baseQuery = "select the from TitleHistoryEvent the where the.eventDate = :eventDate"
                                        Map<String,Object> queryParams = [eventDate:eventDate]
                                        if(from) {
                                            baseQuery += " and exists (select p from the.participants p where p.participant in :from and p.participantRole = 'from')"
                                            queryParams.from = from
                                        }
                                        if(to) {
                                            baseQuery += " and exists (select p from the.participants p where p.participant in :to and p.participantRole = 'to')"
                                            queryParams.to = to
                                        }
                                        List<TitleHistoryEvent> events = TitleHistoryEvent.executeQuery(baseQuery,queryParams)
                                        if(!events) {
                                            Map<String,Object> event = [:]
                                            event.from = from
                                            event.to = to
                                            event.internalId = eventData.'@id'.text()
                                            event.eventDate = eventDate
                                            TitleHistoryEvent the = new TitleHistoryEvent(event)
                                            if(the.save()) {
                                                from.each { TitleInstance partData ->
                                                    TitleHistoryEventParticipant participant = new TitleHistoryEventParticipant(event:the,participant:partData,participantRole:'from')
                                                    if(!participant.save())
                                                        throw new SyncException("Error on creating from participant: ${participant.errors}")
                                                }
                                                to.each { TitleInstance partData ->
                                                    TitleHistoryEventParticipant participant = new TitleHistoryEventParticipant(event:the,participant:partData,participantRole:'to')
                                                    if(!participant.save())
                                                        throw new SyncException("Error on creating to participant: ${participant.errors}")
                                                }
                                            }
                                            else throw new SyncException("Error on creating title history event: ${the.errors}")
                                        }
                                    }
                                    else {
                                        log.error("Title history event without date, that should not be, report history event with internal ID ${eventData.@id.text()} to GOKb!")
                                        SystemEvent.createEvent('GSSS_OAI_WARNING',[titleHistoryEvent:eventData.@id.text(),errorType:"historyEventWithoutDate"])
                                    }
                                }
                            //}
                        }
                        titleInstance
                    }
                    else {
                        throw new SyncException("Error on creating title instance: ${titleInstance.errors}")
                    }
                }
                else {
                    throw new SyncException("ALARM! Title record ${titleUUID} without title type! Unable to process!")
                }
            }
        }
        else {
            throw new SyncException("Title creation for ${titleUUID} called without record data! PANIC!")
        }
    }

    /**
     * Creates or updates a {@link TitleInstance} as history participant
     * @param particData - the OAI extract of the history participant
     * @param titleType - the class name of the title history participant
     * @return the updated title history participant statement
     * @throws SyncException
     */
    TitleInstance createOrUpdateHistoryParticipant(particData, String titleType) throws SyncException {
        TitleInstance participant = TitleInstance.findByGokbId(particData.uuid.text())
        try {
            switch(titleType) {
                case BookInstance.class.name: participant = participant ? (BookInstance) participant : BookInstance.construct([gokbId:particData.uuid.text()])
                    break
                case DatabaseInstance.class.name: participant = participant ? (DatabaseInstance) participant : DatabaseInstance.construct([gokbId:particData.uuid.text()])
                    break
                case JournalInstance.class.name: participant = participant ? (JournalInstance) participant : JournalInstance.construct([gokbId:particData.uuid.text()])
                    break
            }
            participant.status = titleStatus.get(particData.status.text())
            participant.title = particData.title.text()
            if(participant.save()) {
                if(particData.identifiers) {
                    particData.identifiers.identifier.each { idData ->
                        if(idData.'@namespace'.text().toLowerCase() != 'originediturl') {
                            Identifier.construct([namespace: idData.'@namespace'.text(), value: idData.'@value'.text(), reference: participant, isUnique: false, nsType: TitleInstance.class.name])
                        }
                    }
                }
                Identifier.construct([namespace:'uri',value:particData.internalId.text(),reference:participant, isUnique: false, nsType: TitleInstance.class.name])
                participant
            }
            else {
                throw new SyncException(participant.errors)
            }
        }
        catch (GroovyCastException e) {
            log.error(e.getMessage())
            throw new SyncException("Title type mismatch! This should not be possible! Inform GOKb team! -> ${participant.gokbId} is corrupt!")
        }
    }

    /**
     * Was formerly in the {@link Org} domain class; deployed for better maintainability
     * Checks for a given UUID if the provider exists, otherwise, it will be created. The
     * default {@link Platform}s are set up or updated as well
     *
     * @param providerUUID - the GOKb UUID of the given provider {@link Org}
     * @throws SyncException
     */
    void createOrUpdateProvider(String providerUUID) throws SyncException {
        //Org.lookupOrCreate2 simplified
        GPathResult providerOAI = fetchRecordOAI('orgs',[verb:'GetRecord', identifier:providerUUID])
        if(providerOAI) {
            Org.withNewSession { Session sess ->
                GPathResult providerRecord = providerOAI.record.metadata.gokb.org
                log.info("provider record loaded, converting XML record and reconciling title record for UUID ${providerUUID} ...")
                Org provider = Org.findByGokbId(providerUUID)
                if(provider) {
                    provider.name = providerRecord.name.text()
                    provider.status = orgStatus.get(providerRecord.status.text())
                }
                else {
                    provider = new Org(
                            name: providerRecord.name.text(),
                            sector: RDStore.O_SECTOR_PUBLISHER,
                            type: [RDStore.OT_PROVIDER],
                            status: orgStatus.get(providerRecord.status.text()),
                            gokbId: providerUUID
                    )
                }
                if(provider.save()) {
                    providerRecord.providedPlatforms.platform.each { plat ->
                        //ex setOrUpdateProviderPlattform()
                        log.info("checking provider with uuid ${providerUUID}")
                        createOrUpdatePlatform([gokbId: plat.'@uuid'.text(), name: plat.name.text(), primaryUrl: plat.primaryUrl.text(), platformProvider:providerUUID])
                        Platform platform = Platform.findByGokbId(plat.'@uuid'.text())
                        if(platform.org != provider) {
                            platform.org = provider
                            platform.save()
                        }
                    }
                }
                else throw new SyncException(provider.errors)
            }
        }
        else throw new SyncException("Provider loading failed for UUID ${providerUUID}!")
    }

    Org createOrUpdateProviderJSON(String providerUUID) throws SyncException {
        //This method is under Package.newSession! Do not hook up new session here!
        Map<String,Object> providerJSON = fetchRecordJSON(false,[uuid:providerUUID])
        if(providerJSON.records) {
            Map providerRecord = providerJSON.records[0]
            log.info("provider record loaded, reconciling provider record for UUID ${providerUUID}")
            Org provider = Org.findByGokbId(providerUUID)
            if(provider) {
                provider.name = providerRecord.name
                provider.status = orgStatus.get(providerRecord.status)
                List allOrgTypeIds = provider.getAllOrgTypeIds()
                if(!(RDStore.OT_PROVIDER.id in allOrgTypeIds)){
                    provider.addToOrgType(RDStore.OT_PROVIDER)
                }

            }
            else {
                provider = new Org(
                        name: providerRecord.name,
                        sector: RDStore.O_SECTOR_PUBLISHER,
                        status: orgStatus.get(providerRecord.status),
                        gokbId: providerUUID
                )
                provider.addToOrgType(RDStore.OT_PROVIDER)
            }
            if(provider.save()) {
                //providedPlatforms are missing in ES output -> see GOKb-ticket #378! But maybe, it is wiser to not implement it at all
                provider
            }
            else throw new SyncException(provider.errors)
        }
        else
            throw new SyncException("Provider loading failed for UUID ${providerUUID}!")
    }

    /**
     * Was complicatedly included in the Org domain class, has been deployed externally for better maintainability
     * Retrieves an {@link Org} instance as title publisher, if the given {@link Org} instance does not exist, it will be created.
     *
     * @param publisherParams - a {@link Map} containing the OAI PMH extract of the title publisher
     * @return the title publisher {@link Org}
     * @throws SyncException
     */
    Org lookupOrCreateTitlePublisher(Map<String,Object> publisherParams) throws SyncException {
        if(publisherParams.gokbId && publisherParams.gokbId instanceof String) {
            //Org.lookupOrCreate simplified, leading to Org.lookupOrCreate2
            Org publisher = Org.findByGokbId((String) publisherParams.gokbId)
            if(!publisher) {
                //Org.withTransaction {
                    publisher = new Org(
                            name: publisherParams.name,
                            status: RDStore.O_STATUS_CURRENT,
                            gokbId: publisherParams.gokbId,
                            orgType: [RDStore.OT_PUBLISHER],
                            sector: RDStore.O_SECTOR_PUBLISHER
                    )
                    if(publisher.save()) {
                        publisherParams.identifiers.each { Map<String,Object> pubId ->
                            pubId.reference = publisher
                            pubId.isUnique = false
                            pubId.nsType = Org.class.name
                            Identifier.construct(pubId)
                        }
                    }
                    else {
                        throw new SyncException(publisher.errors)
                    }
                //}
            }
            publisher
        }
        else {
            throw new SyncException("Org submitted without UUID! No checking possible!")
        }
    }

    /**
     * Checks for a given provider uuid if there is a link with the package for the given uuid
     * @param providerUUID - the provider UUID
     * @param pkg - the package to check against
     */
    void createOrUpdatePackageProvider(Org provider, Package pkg) throws SyncException {
        OrgRole providerRole = OrgRole.findByPkgAndRoleTypeInList(pkg,[RDStore.OR_PROVIDER,RDStore.OR_CONTENT_PROVIDER])
        if(providerRole) {
            providerRole.org = provider
        }
        else {
            providerRole = new OrgRole(org:provider,pkg:pkg,roleType: RDStore.OR_PROVIDER, isShared: false)
        }
        if(!providerRole.save()) {
            throw new SyncException("Error on saving org role: ${providerRole.errors}")
        }
    }

    /**
     * Updates a {@link Platform} with the given parameters. If it does not exist, it will be created.
     *
     * @param platformParams - the platform parameters
     * @throws SyncException
     */
    void createOrUpdatePlatform(Map<String,String> platformParams) throws SyncException {
        Platform.withNewSession { Session sess ->
            Platform platform = Platform.findByGokbId(platformParams.gokbId)
            if(platform) {
                platform.name = platformParams.name
            }
            else {
                platform = new Platform(name: platformParams.name, gokbId: platformParams.gokbId)
            }
            platform.normname = platformParams.name.trim().toLowerCase()
            if(platformParams.primaryUrl)
                platform.primaryUrl = new URL(platformParams.primaryUrl)
            if(platformParams.platformProvider)
                platform.org = Org.findByGokbId(platformParams.platformProvider)
            if(!platform.save()) {
                throw new SyncException("Error on saving platform: ${platform.errors}")
            }
        }
    }

    Platform createOrUpdatePlatformJSON(String platformUUID) throws SyncException {
        Map<String,Object> platformJSON = fetchRecordJSON(false,[uuid: platformUUID])
        if(platformJSON.records) {
            Map platformRecord = platformJSON.records[0]
            //Platform.withTransaction { TransactionStatus ts ->
                Platform platform = Platform.findByGokbId(platformUUID)
                if(platform) {
                    platform.name = platformRecord.name
                }
                else {
                    platform = new Platform(name: platformRecord.name, gokbId: platformRecord.uuid)
                }
                platform.normname = platformRecord.name.toLowerCase()
                if(platformRecord.primaryUrl)
                    platform.primaryUrl = new URL(platformRecord.primaryUrl)
                if(platformRecord.providerUuid)
                    platform.org = createOrUpdateProviderJSON(platformRecord.providerUuid)
                if(platform.save()) {
                   platform
                }
                else throw new SyncException("Error on saving platform: ${platform.errors}")
            //}
        }
    }

    /**
     * Compares two packages on domain property level against each other, retrieving the differences between both.
     * @param pkgA - the old package (as {@link Package} which is already persisted)
     * @param pkgB - the new package (as unprocessed {@link Map}
     * @return a {@link Set} of {@link Map}s with the differences
     */
    Set<Map<String,Object>> getPkgPropDiff(Package pkgA, Map<String,Object> pkgB) {
        log.info("processing package prop diffs; the respective GOKb UUIDs are: ${pkgA.gokbId} (LAS:eR) vs. ${pkgB.uuid} (remote)")
        Set<Map<String,Object>> result = []
        Set<String> controlledProperties = ['name','packageStatus','listVerifiedDate','packageScope','packageListStatus','breakable','consistent','fixed']

        controlledProperties.each { String prop ->
            if(pkgA[prop] != pkgB[prop]) {
                if(prop in PendingChange.REFDATA_FIELDS)
                    result.add([prop: prop, newValue: pkgB[prop]?.id, oldValue: pkgA[prop]?.id])
                else result.add([prop: prop, newValue: pkgB[prop], oldValue: pkgA[prop]])
            }
        }

        if(pkgA.nominalPlatform != pkgB.nominalPlatform) {
            result.add([prop: 'nominalPlatform', newValue: pkgB.nominalPlatform?.name, oldValue: pkgA.nominalPlatform?.name])
        }

        if(pkgA.contentProvider != pkgB.contentProvider) {
            result.add([prop: 'nominalProvider', newValue: pkgB.contentProvider?.name, oldValue: pkgA.contentProvider?.name])
        }

        //the tipp diff count cannot be executed at this place because it depends on TIPP processing result

        result
    }

    /**
     * Converts the TIPP data from OAI-XML elements into an object structure, reflected by a {@link Map}, representing the {@link TitleInstancePackagePlatform} and {@link TIPPCoverage} class structures
     * @param TIPPData - the base branch ({@link NodeChildren}) containing the OAI extract for {@link TitleInstancePackagePlatform} data
     * @return a {@link Map} containing the underlying TIPP data
     */
    Map<String,Object> tippConv(tipp) {
        Map<String,Object> updatedTIPP = [
                title: [
                        gokbId: tipp.title.'@uuid'.text()
                ],
                packageUUID: tipp.package.'@uuid'.text(), //needed for TIPP endpoint processing
                platformUUID: tipp.platform.'@uuid'.text(),
                status: tipp.status.text(),
                coverages: [],
                hostPlatformURL: tipp.url.text(),
                identifiers: [],
                id: tipp.'@id'.text(),
                uuid: tipp.'@uuid'.text(),
                accessStartDate : tipp.access.'@start'.text() ? DateUtils.parseDateGeneric(tipp.access.'@start'.text()) : null,
                accessEndDate   : tipp.access.'@end'.text() ? DateUtils.parseDateGeneric(tipp.access.'@end'.text()) : null,
                medium      : tipp.medium.text()
        ]
        if(tipp.title.type.text() == 'JournalInstance') {
            tipp.coverage.each { cov ->
                updatedTIPP.coverages << [
                        startDate: cov.'@startDate'.text() ? DateUtils.parseDateGeneric(cov.'@startDate'.text()) : null,
                        endDate: cov.'@endDate'.text() ? DateUtils.parseDateGeneric(cov.'@endDate'.text()) : null,
                        startVolume: cov.'@startVolume'.text() ?: null,
                        endVolume: cov.'@endVolume'.text() ?: null,
                        startIssue: cov.'@startIssue'.text() ?: null,
                        endIssue: cov.'@endIssue'.text() ?: null,
                        coverageDepth: cov.'@coverageDepth'.text() ?: null,
                        coverageNote: cov.'@coverageNote'.text() ?: null,
                        embargo: cov.'@embargo'.text() ?: null
                ]
            }
            updatedTIPP.coverages = updatedTIPP.coverages.toSorted { a, b -> a.startDate <=> b.startDate }
        }
        updatedTIPP
    }

    Map<String,Object> processTippDiffs(TitleInstancePackagePlatform tippA, Map tippB) {
        //ex updatedTippClosure / tippUnchangedClosure
        RefdataValue status = tippStatus.get(tippB.status)
        //there are some weird diffs; make single-thread, then multi-thread checkup!
        if (status == RDStore.TIPP_STATUS_DELETED && tippA.status != status) {
            log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${tippA.pkg.gokbId}")
            tippA.status = RDStore.TIPP_STATUS_DELETED
            tippA.save()
            [event: "delete", target: tippA]
        }
        else {
            //process central differences which are without effect to issue entitlements
            tippA.titleType = tippB.titleType
            tippA.name = tippB.name //TODO include name, sortName in IssueEntitlements, then, this property may move to the controlled ones
            tippA.firstAuthor = tippB.firstAuthor
            tippA.firstEditor = tippB.firstEditor
            tippA.publisherName = tippB.publisherName
            tippA.hostPlatformURL = tippB.hostPlatformURL
            tippA.dateFirstInPrint = (Date) tippB.dateFirstInPrint
            tippA.dateFirstOnline = (Date) tippB.dateFirstOnline
            tippA.imprint = tippB.imprint
            tippA.seriesName = tippB.seriesName
            tippA.subjectReference = tippB.subjectReference
            tippA.volume = tippB.volume
            tippA.medium = titleMedium.get(tippB.medium)
            if(!tippA.save())
                throw new SyncException("Error on updating base title data: ${tippA.errors}")
            if(tippB.titlePublishers) {
                if(tippA.publishers) {
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.tipp = :tippA',[tippA:tippA])
                }
                tippB.titlePublishers.each { publisher ->
                    lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid])
                }
            }
            if(tippB.identifiers) {
                //I hate this solution ... wrestlers of GOKb stating that Identifiers do not need UUIDs were stronger.
                //Identifier.withTransaction { TransactionStatus ts ->
                    if(tippA.ids) {
                        Identifier.executeUpdate('delete from Identifier i where i.tipp = :tipp',[tipp:tippA]) //damn those wrestlers ...
                    }
                    tippB.identifiers.each { idData ->
                        if(idData.namespace.toLowerCase() != 'originediturl') {
                            Identifier.construct([namespace: idData.namespace, value: idData.value, name_de: idData.namespaceName, reference: tippA, isUnique: false, nsType: TitleInstancePackagePlatform.class.name])
                        }
                    }
                //}
            }
            if(tippB.history) {
                if(tippA.historyEvents) {
                    TitleHistoryEvent.executeUpdate('delete from TitleHistoryEvent the where the.tipp = :tipp',[tipp:tippA])
                }
                tippB.history.each { historyEvent ->
                    historyEvent.from.each { from ->
                        TitleHistoryEvent the = new TitleHistoryEvent(tipp:tippA,from:from.name,eventDate:historyEvent.date)
                        if(!the.save())
                            throw new SyncException("Error on saving title history data: ${the.errors}")
                    }
                    historyEvent.to.each { to ->
                        TitleHistoryEvent the = new TitleHistoryEvent(tipp:tippA,from:to.name,eventDate:historyEvent.date)
                        if(!the.save())
                            throw new SyncException("Error on saving title history data: ${the.errors}")
                    }
                }
            }
            //get to diffs that need to be notified
            //println("tippA:"+tippA)
            //println("tippB:"+tippB)
            Set<Map<String, Object>> diffs = getTippDiff(tippA, tippB)
            //includes also changes in coverage statement set
            if (diffs) {
                //process actual diffs
                diffs.each { Map<String,Object> diff ->
                    log.info("Got tipp diff: ${diff}")
                    switch(diff.prop) {
                        case 'coverage':
                            diff.covDiffs.each { entry ->
                                switch (entry.event) {
                                    case 'add':
                                        if (!entry.target.save())
                                            throw new SyncException("Error on adding coverage statement for TIPP ${tippA.gokbId}: ${entry.target.errors}")
                                        break
                                    case 'delete': PendingChange.executeUpdate('delete from PendingChange pc where pc.tippCoverage = :toDelete',[toDelete:entry.target])
                                        TIPPCoverage.executeUpdate('delete from TIPPCoverage tc where tc.id = :id',[id:entry.target.id])
                                        break
                                    case 'update': entry.diffs.each { covDiff ->
                                        entry.target[covDiff.prop] = covDiff.newValue
                                    }
                                        if (!entry.target.save())
                                            throw new SyncException("Error on updating coverage statement for TIPP ${tippA.gokbId}: ${entry.target.errors}")
                                        break
                                }
                            }
                            break
                        case 'price':
                            diff.priceDiffs.each { entry ->
                                switch (entry.event) {
                                    case 'add':
                                        if (!entry.target.save())
                                            throw new SyncException("Error on adding price item for TIPP ${tippA.gokbId}: ${entry.target.errors}")
                                        break
                                    case 'delete': PendingChange.executeUpdate('delete from PendingChange pc where pc.priceItem = :toDelete',[toDelete:entry.target])
                                        PriceItem.executeUpdate('delete from PriceItem pi where pi.id = :id',[id:entry.target.id])
                                        break
                                    case 'update': entry.diffs.each { priceDiff ->
                                        entry.target[priceDiff.prop] = priceDiff.newValue
                                    }
                                        if (!entry.target.save())
                                            throw new SyncException("Error on updating coverage statement for TIPP ${tippA.gokbId}: ${entry.target.errors}")
                                        break
                                }
                            }
                            break
                        default:
                            if (diff.prop in PendingChange.REFDATA_FIELDS) {
                                tippA[diff.prop] = tippStatus.values().find { RefdataValue rdv -> rdv.id == diff.newValue }
                            } else {
                                tippA[diff.prop] = diff.newValue
                            }
                            break
                    }
                }
                if (tippA.save())
                    [event: 'update', target: tippA, diffs: diffs]
                else throw new SyncException("Error on updating TIPP with UUID ${tippA.gokbId}: ${tippA.errors}")
            }
            else [:]
        }
    }

    /**
     * Replaces the onNewTipp closure.
     * Creates a new {@link TitleInstancePackagePlatform} with its respective {@link TIPPCoverage} statements
     * @param pkg
     * @param tippData
     * @return the new {@link TitleInstancePackagePlatform} object
     */
    TitleInstancePackagePlatform addNewTIPP(Package pkg, Map<String,Object> tippData,Map<String,Platform> platformsInPackage,Map<String,TitleInstance> titleInstancesInPackage) throws SyncException {
        TitleInstancePackagePlatform newTIPP = new TitleInstancePackagePlatform(
                titleType: tippData.titleType,
                name: tippData.name,
                firstAuthor: tippData.firstAuthor,
                firstEditor: tippData.firstEditor,
                dateFirstInPrint: (Date) tippData.dateFirstInPrint,
                dateFirstOnline: (Date) tippData.dateFirstOnline,
                imprint: tippData.imprint,
                publisherName: tippData.publisherName,
                seriesName: tippData.seriesName,
                subjectReference: tippData.subjectReference,
                volume: tippData.volume,
                medium: titleMedium.get(tippData.medium),
                gokbId: tippData.uuid,
                status: tippStatus.get(tippData.status),
                hostPlatformURL: tippData.hostPlatformURL,
                accessStartDate: (Date) tippData.accessStartDate,
                accessEndDate: (Date) tippData.accessEndDate,
                pkg: pkg
        )
        //ex updatedTitleAfterPackageReconcile
        //long start = System.currentTimeMillis()
        Platform platform = platformsInPackage.get(tippData.platformUUID)
        //log.debug("time needed for queries: ${System.currentTimeMillis()-start}")
        if(!platform) {
            platform = Platform.findByGokbId(tippData.platformUUID)
        }
        if(!platform) {
            platform = createOrUpdatePlatformJSON(tippData.platformUUID)
        }
        newTIPP.platform = platform
        if(newTIPP.save()) {
            tippData.coverages.each { covB ->
                TIPPCoverage covStmt = new TIPPCoverage(
                        startDate: (Date) covB.startDate ?: null,
                        startVolume: covB.startVolume,
                        startIssue: covB.startIssue,
                        endDate: (Date) covB.endDate ?: null,
                        endVolume: covB.endVolume,
                        endIssue: covB.endIssue,
                        embargo: covB.embargo,
                        coverageDepth: covB.coverageDepth,
                        coverageNote: covB.coverageNote,
                        tipp: newTIPP
                )
                if (!covStmt.save())
                    throw new SyncException("Error on saving coverage data: ${covStmt.errors}")
            }
            tippData.priceItems.each { piB ->
                PriceItem priceItem = new PriceItem(startDate: (Date) piB.startDate ?: null,
                        endDate: (Date) piB.endDate ?: null,
                        listPrice: piB.listPrice,
                        listCurrency: piB.listCurrency,
                        tipp: newTIPP
                )
                priceItem.setGlobalUID()
                if(!priceItem.save())
                    throw new SyncException("Error on saving price data: ${priceItem.errors}")
            }
            tippData.titlePublishers.each { publisher ->
                lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid])
            }
            tippData.identifiers.each { idB ->
                if(idB.namespace.toLowerCase() != 'originediturl') {
                    Identifier.construct([namespace: idB.namespace, value: idB.value, name_de: idB.name_de, reference: newTIPP, isUnique: false, nsType: TitleInstancePackagePlatform.class.name])
                }
            }
            tippData.history.each { historyEvent ->
                historyEvent.from.each { from ->
                    TitleHistoryEvent the = new TitleHistoryEvent(tipp:newTIPP,from:from.name,eventDate:historyEvent.date)
                    if(!the.save())
                        throw new SyncException("Error on saving title history data: ${the.errors}")
                }
                historyEvent.to.each { to ->
                    TitleHistoryEvent the = new TitleHistoryEvent(tipp:newTIPP,from:to.name,eventDate:historyEvent.date)
                    if(!the.save())
                        throw new SyncException("Error on saving title history data: ${the.errors}")
                }
            }
            newTIPP
        }
        else throw new SyncException("Error on saving TIPP data: ${newTIPP.errors}")
    }

    /**
     * Compares two package entries against each other, retrieving the differences between both.
     * @param tippa - the old TIPP
     * @param tippb - the new TIPP
     * @return a {@link Set} of {@link Map}s with the differences
     */
    Set<Map<String,Object>> getTippDiff(tippa, tippb) {
        if(tippa instanceof TitleInstancePackagePlatform && tippb instanceof Map)
            log.info("processing diffs; the respective GOKb UUIDs are: ${tippa.gokbId} (LAS:eR) vs. ${tippb.uuid} (remote)")
        else if(tippa instanceof TitleInstancePackagePlatform && tippb instanceof TitleInstancePackagePlatform)
            log.info("processing diffs; the respective objects are: ${tippa.id} (TitleInstancePackagePlatform) pointing to ${tippb.id} (TIPP)")
        Set<Map<String, Object>> result = []

        /*
        IssueEntitlements do not have hostPlatformURLs
        if (tippa.hasProperty("hostPlatformURL") && tippa.hostPlatformURL != tippb.hostPlatformURL) {
            if(!((tippa.hostPlatformURL == null && tippb.hostPlatformURL == "") || (tippa.hostPlatformURL == "" && tippb.hostPlatformURL == null)))
                result.add([prop: 'hostPlatformURL', newValue: tippb.hostPlatformURL, oldValue: tippa.hostPlatformURL])
        }
        */

        // This is the boss enemy when refactoring coverage statements ... works so far, is going to be kept
        // the question marks are necessary because only JournalInstance's TIPPs are supposed to have coverage statements
        Set<Map<String, Object>> coverageDiffs = getSubListDiffs(tippa,tippb.coverages,'coverage')
        if(!coverageDiffs.isEmpty())
            result.add([prop: 'coverage', covDiffs: coverageDiffs])

        Set<Map<String, Object>> priceDiffs = getSubListDiffs(tippa,tippb.priceItems,'price')
        if(!priceDiffs.isEmpty())
            result.add([prop: 'price', priceDiffs: priceDiffs])

        /* is perspectively ordered; needs more refactoring
        if (tippb.containsKey("name") && tippa.name != tippb.name) {
            result.add([prop: 'name', newValue: tippb.name, oldValue: tippa.name])
        }
        */

        if (tippb.containsKey("accessStartDate") && tippa.accessStartDate != tippb.accessStartDate) {
            result.add([prop: 'accessStartDate', newValue: tippb.accessStartDate, oldValue: tippa.accessStartDate])
        }

        if (tippb.containsKey("accessEndDate") && tippa.accessEndDate != tippb.accessEndDate) {
            result.add([prop: 'accessEndDate', newValue: tippb.accessEndDate, oldValue: tippa.accessEndDate])
        }

        if(tippa instanceof TitleInstancePackagePlatform && tippb instanceof Map) {
            if(tippa.status != tippStatus.get(tippb.status)) {
                result.add([prop: 'status', newValue: tippStatus.get(tippb.status).id, oldValue: tippa.status.id])
            }
        }
        else if(tippa instanceof IssueEntitlement && tippb instanceof TitleInstancePackagePlatform) {
            if(tippa.status != tippb.status) {
                result.add([prop: 'status', newValue: tippb.status.id, oldValue: tippa.status.id])
            }
        }

        //println("getTippDiff:"+result)
        result
    }

    /**
     * Compares two sub list entries against each other, retrieving the differences between both.
     * @param tippA - the old {@link TitleInstancePackagePlatform} object, containing the current {@link Set} of  or price items
     * @param listB - the new statements (a {@link List} of remote records, kept in {@link Map}s)
     * @return a {@link Set} of {@link Map}s reflecting the differences between the statements
     */
    Set<Map<String,Object>> getSubListDiffs(TitleInstancePackagePlatform tippA, listB, String instanceType) {
        Set subDiffs = []
        Set listA
        if(instanceType == "coverage")
            listA = tippA.coverages
        else if(instanceType == "price")
            listA = tippA.priceItems
        if(listA != null) {
            if(listA.size() == listB.size()) {
                //statements may have changed or not, no deletions or insertions
                //sorting has been done by mapping (listA) resp. when converting data (listB)
                listB.eachWithIndex { itemB, int i ->
                    def itemA = locateEquivalent(itemB,listA)
                    if(!itemA)
                        itemA = listA[i]
                    Set<Map<String,Object>> currDiffs = compareSubListItem(itemA,itemB)
                    if(currDiffs)
                        subDiffs << [event: 'update', target: itemA, diffs: currDiffs]
                }
            }
            else if(listA.size() > listB.size()) {
                //statements have been deleted
                Set toKeep = []
                listB.each { itemB ->
                    def itemA = locateEquivalent(itemB,listA)
                    if(itemA) {
                        toKeep << itemA
                        Set<Map<String,Object>> currDiffs = compareSubListItem(itemA,itemB)
                        if(currDiffs)
                            subDiffs << [event: 'update', target: itemA, diffs: currDiffs]
                    }
                    else {
                        //a new statement may have been added for which I cannot determine an equivalent
                        def newItem
                        if(instanceType == 'coverage')
                            newItem = addNewStatement(tippA,itemB)
                        else if(instanceType == 'price')
                            newItem = addNewPriceItem(tippA,itemB)
                        if(newItem)
                            subDiffs << [event: 'add', target: newItem]
                    }
                }
                listA.each { itemA ->
                    if(!toKeep.contains(itemA)) {
                        subDiffs << [event: 'delete', target: itemA, targetParent: tippA]
                    }
                }
            }
            else if(listA.size() < listB.size()) {
                //coverage statements have been added
                listB.each { itemB ->
                    def itemA = locateEquivalent(itemB,listA)
                    if(itemA) {
                        Set<Map<String,Object>> currDiffs = compareSubListItem(itemA,itemB)
                        if(currDiffs)
                            subDiffs << [event: 'update', target: itemA, diffs: currDiffs]
                    }
                    else {
                        def newItem
                        if(instanceType == 'coverage')
                            newItem = addNewStatement(tippA,itemB)
                        else if(instanceType == 'price')
                            newItem = addNewPriceItem(tippA,itemB)
                        if(newItem)
                            subDiffs << [event: 'add', target: newItem]
                    }
                }
            }
        }

        subDiffs
    }

    Set<Map<String,Object>> compareSubListItem(itemA,itemB) {
        Set<String> controlledProperties = []
        if(itemA instanceof AbstractCoverage) {
            controlledProperties.addAll([
                    'startDate',
                    'startVolume',
                    'startIssue',
                    'endDate',
                    'endVolume',
                    'endIssue',
                    'embargo',
                    'coverageDepth',
                    'coverageNote',
            ])
        }
        else if(itemA instanceof PriceItem) {
            controlledProperties.addAll([
                    'startDate',
                    'endDate',
                    'listPrice',
                    'listCurrency'
            ])
        }
        Set<Map<String,Object>> diffs = []
        controlledProperties.each { String cp ->
            if(cp in ['startDate','endDate']) {
                Calendar calA = Calendar.getInstance(), calB = Calendar.getInstance()
                if(itemA[cp] != null && itemB[cp] != null) {
                    calA.setTime((Date) itemA[cp])
                    calB.setTime((Date) itemB[cp])
                    if(!(calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) && calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR))) {
                        diffs << [prop: cp, oldValue: itemA[cp], newValue: itemB[cp]]
                    }
                }
                else {
                    /*
                    Means that one of the coverage dates is null or became null.
                    Cases to cover: null -> date (covA == null, covB instanceof Date)
                    date -> null (covA instanceof Date, covB == null)
                     */
                    if(itemA[cp] != null && itemB[cp] == null) {
                        calA.setTime((Date) itemA[cp])
                        diffs << [prop:cp, oldValue:itemA[cp],newValue:null]
                    }
                    else if(itemA[cp] == null && itemB[cp] != null) {
                        calB.setTime((Date) itemB[cp])
                        diffs << [prop:cp, oldValue:null, newValue: itemB[cp]]
                    }
                }
            }
            else {
                if(itemA[cp] != itemB[cp] && !((itemA[cp] == '' && itemB[cp] == null) || (itemA[cp] == null && itemB[cp] == ''))) {
                    diffs << [prop:cp, oldValue: itemA[cp], newValue: itemB[cp]]
                }
            }
        }
        diffs
    }

    /**
     * Contrary to {@link AbstractCoverage}.findEquivalent() resp {@link PriceItem}.findEquivalent(), this method locates a non-persisted coverage statement an equivalent from the given {@link Collection}
     * @param itemB - a {@link Map}, reflecting the non-persisited item
     * @param listA - a {@link Collection} on {@link TIPPCoverage} or {@link PriceItem} statements, the list to be updated
     * @return the equivalent LAS:eR {@link TIPPCoverage} or {@link PriceItem} from the collection
     */
    def locateEquivalent(itemB, listA) {
        def equivalent = null
        Set<String> equivalencyProperties = []
        if(listA[0] instanceof AbstractCoverage)
            equivalencyProperties.addAll(AbstractCoverage.equivalencyProperties)
        else if(listA[0] instanceof PriceItem)
            equivalencyProperties.addAll(PriceItem.equivalencyProperties)
        for (String k : equivalencyProperties) {
            if(k in ['startDate','endDate']) {
                Calendar calA = Calendar.getInstance(), calB = Calendar.getInstance()
                listA.each { itemA ->
                    if(itemA[k] != null && itemB[k] != null) {
                        calA.setTime(itemA[k])
                        calB.setTime(itemB[k])
                        if (calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) && calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR))
                            equivalent = itemA
                    }
                    else if(itemA[k] == null && itemB[k] == null)
                        equivalent = itemA
                }
            }
            else
                equivalent = listA.find { it[k] == itemB[k] }
            if (equivalent != null) {
                println "Statement ${equivalent.id} located as equivalent to ${itemB}"
                break
            }
        }
        equivalent
    }

    AbstractCoverage addNewStatement(tippA, covB) {
        Map<String,Object> params = [startDate: (Date) covB.startDate,
                                     startVolume: covB.startVolume,
                                     startIssue: covB.startIssue,
                                     endDate: (Date) covB.endDate,
                                     endVolume: covB.endVolume,
                                     endIssue: covB.endIssue,
                                     embargo: covB.embargo,
                                     coverageDepth: covB.coverageDepth,
                                     coverageNote: covB.coverageNote]
        AbstractCoverage newStatement
        if(tippA instanceof TitleInstancePackagePlatform)
            newStatement = new TIPPCoverage(params+[tipp: tippA])
        if(tippA instanceof IssueEntitlement)
            newStatement = new IssueEntitlementCoverage(params+[issueEntitlement: tippA])
        if(newStatement)
            newStatement
        else null
    }

    PriceItem addNewPriceItem(tippA, piB) {
        Map<String,Object> params = [startDate: (Date) piB.startDate,
                                     endDate: (Date) piB.endDate,
                                     listPrice: piB.listPrice,
                                     listCurrency: piB.listCurrency,
                                     tipp: tippA]
        PriceItem pi = new PriceItem(params)
        pi.setGlobalUID()
        pi
    }

    void notifyDependencies(List<List<Map<String,Object>>> tippsToNotify) {
        //if everything went well, we should have here the list of tipps to notify ...
        //TitleInstancePackagePlatform.withTransaction { TransactionStatus ts ->
            tippsToNotify.eachWithIndex { List<Map<String,Object>> entry, int i ->
                entry.eachWithIndex { Map<String,Object> notify, int index ->
                    log.debug("now processing entry #${index}, payload: ${notify}")
                    if(notify.event in ['pkgPropUpdate','pkgDelete']) {
                        Package target = (Package) notify.target
                        Set<SubscriptionPackage> spConcerned = SubscriptionPackage.findAllByPkg(target)
                        if(notify.event == 'pkgPropUpdate') {
                            spConcerned.each { SubscriptionPackage sp ->
                                Map<String,Object> changeMap = [target:sp.subscription, oid:genericOIDService.getOID(target)]
                                notify.diffs.each { diff ->
                                    changeMap.oldValue = diff.oldValue
                                    changeMap.newValue = diff.newValue
                                    changeMap.prop = diff.prop
                                    changeNotificationService.determinePendingChangeBehavior(changeMap,PendingChangeConfiguration.PACKAGE_PROP,sp)
                                }
                            }
                        }
                        else if(notify.event == 'pkgDelete') {
                            spConcerned.each { SubscriptionPackage sp ->
                                changeNotificationService.determinePendingChangeBehavior([target:sp.subscription,oid:genericOIDService.getOID(target)],PendingChangeConfiguration.PACKAGE_DELETED,sp)
                            }
                        }
                    }
                    else {
                        TitleInstancePackagePlatform target = (TitleInstancePackagePlatform) notify.target
                        if(notify.event == 'add') {
                            Set<SubscriptionPackage> spConcerned = SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg = :pkg',[pkg:target.pkg])
                            spConcerned.each { SubscriptionPackage sp ->
                                changeNotificationService.determinePendingChangeBehavior([target:sp.subscription,oid:genericOIDService.getOID(target)],PendingChangeConfiguration.NEW_TITLE,sp)
                            }
                        }
                        else {
                            Set<IssueEntitlement> ieConcerned = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp = :tipp',[tipp:target]) //exclude already deleted items as well!
                            ieConcerned.each { IssueEntitlement ie ->
                                String changeDesc = ""
                                Map<String,Object> changeMap = [target:ie.subscription]
                                switch(notify.event) {
                                    case 'update': notify.diffs.each { diff ->
                                        if(diff.prop == 'coverage') {
                                            //the city Coventry is beautiful, isn't it ... but here is the COVerageENTRY meant.
                                            diff.covDiffs.each { covEntry ->
                                                TIPPCoverage tippCov = (TIPPCoverage) covEntry.target
                                                switch(covEntry.event) {
                                                    case 'update': IssueEntitlementCoverage ieCov = (IssueEntitlementCoverage) tippCov.findEquivalent(ie.coverages)
                                                        if(ieCov) {
                                                            covEntry.diffs.each { covDiff ->
                                                                changeDesc = PendingChangeConfiguration.COVERAGE_UPDATED
                                                                changeMap.oid = genericOIDService.getOID(ieCov)
                                                                changeMap.prop = covDiff.prop
                                                                changeMap.oldValue = ieCov[covDiff.prop]
                                                                changeMap.newValue = covDiff.newValue
                                                                changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                                            }
                                                        }
                                                        else {
                                                            changeDesc = PendingChangeConfiguration.NEW_COVERAGE
                                                            changeMap.oid = "${tippCov.class.name}:${tippCov.id}"
                                                            changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                                        }
                                                        break
                                                    case 'add':
                                                        changeDesc = PendingChangeConfiguration.NEW_COVERAGE
                                                        changeMap.oid = "${tippCov.class.name}:${tippCov.id}"
                                                        changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                                        break
                                                    case 'delete':
                                                        IssueEntitlementCoverage ieCov = (IssueEntitlementCoverage) tippCov.findEquivalent(ie.coverages)
                                                        if(ieCov) {
                                                            changeDesc = PendingChangeConfiguration.COVERAGE_DELETED
                                                            changeMap.oid = genericOIDService.getOID(ieCov)
                                                            changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                                        }
                                                        break
                                                }
                                            }
                                        }
                                        else {
                                            changeDesc = PendingChangeConfiguration.TITLE_UPDATED
                                            changeMap.oid = genericOIDService.getOID(ie)
                                            changeMap.prop = diff.prop
                                            if(diff.prop in PendingChange.REFDATA_FIELDS)
                                                changeMap.oldValue = ie[diff.prop].id
                                            else if(diff.prop in ['hostPlatformURL'])
                                                changeMap.oldValue = diff.oldValue
                                            else
                                                changeMap.oldValue = ie[diff.prop]
                                            changeMap.newValue = diff.newValue
                                            changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                        }
                                    }
                                        break
                                    case 'delete':
                                        changeDesc = PendingChangeConfiguration.TITLE_DELETED
                                        changeMap.oid = genericOIDService.getOID(ie)
                                        changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,target.pkg))
                                        break
                                }
                                //changeNotificationService.registerPendingChange(PendingChange.PROP_SUBSCRIPTION,ie.subscription,ie.subscription.getSubscriber(),changeMap,null,null,changeDesc)
                            }
                        }
                    }
                }
                if(i > 0 && i % 30 == 0) {
                    log.info("clean up transaction")
                    //ts.flush()
                }
            }
        //}

    }

    /**
     * Retrieves remote data with the given query parameters. Used to query a GOKb instance for changes since a given timestamp or to fetch remote package/provider data
     * Was formerly the OaiClient and OaiClientLaser classes
     *
     * @param url - the URL to query against
     * @param object - the object(s) about which records should be obtained. May be: {@link Package}, {@link TitleInstance} or {@link Org}
     * @param queryParams - parameters to pass along with the query
     * @return a {@link GPathResult} containing the OAI-PMH conform XML extract of the given record
     */
    GPathResult fetchRecordOAI(String object, Map<String,String> queryParams) {
        try {
            HTTPBuilder http = new HTTPBuilder(source.uri)
            queryParams.metadataPrefix = source.fullPrefix
            GPathResult record = (GPathResult) http.post(path:object,query:queryParams,contentType:'xml') { resp, xml ->
                log.info("record successfully loaded, parsing XML ...")
                XmlSlurper xs = new XmlSlurper()
                GPathResult response = xs.parseText(xml.text) //bottleneck!
                if(response.error && response.error.@code == 'idDoesNotExist') {
                    log.error(response.error)
                    null
                }
                else if(response[queryParams.verb] && response[queryParams.verb] instanceof GPathResult) {
                    if(response[queryParams.verb]) {
                        response[queryParams.verb]
                    }
                    else null
                }
                else {
                    log.error('Request succeeded but result data invalid. Please do checks!')
                    null
                }
            }
            http.shutdown()
            record
        }
        catch(HttpResponseException e) {
            log.error(e.getMessage(),e)
            null
        }
    }

    Map<String,Object> fetchRecordJSON(boolean useScroll, Map<String,Object> queryParams) throws SyncException {
        //I need to address a bulk output endpoint like https://github.com/hbz/lobid-resources/blob/f93201bec043cc732b27814a6ab4aea390d1aa9e/web/app/controllers/resources/Application.java, method bulkResult().
        //By then, I should query the "normal" endpoint /wekb/api/find?
        HTTPBuilder http
        if(useScroll) {
            http = new HTTPBuilder(source.uri + '/scroll')
        }
        else http = new HTTPBuilder(source.uri+'/find')
        Map<String,Object> result = [:]
        //setting default status
        queryParams.status = ["Current","Expected","Retired"]
        log.debug("mem check: ${Runtime.getRuntime().freeMemory()} bytes")
        http.request(Method.POST, ContentType.JSON) { req ->
            body = queryParams
            requestContentType = ContentType.URLENC
            response.success = { resp, json ->
                if(resp.status == 200) {
                    result.count = json.size ?: json.count
                    result.records = json.records
                    result.scrollId = json.scrollId
                    result.hasMoreRecords = Boolean.valueOf(json.hasMoreRecords)
                }
                else {
                    throw new SyncException("erroneous response")
                }
            }
            response.failure = { resp, reader ->
                log.error("server response: ${resp.statusLine}")
                throw new SyncException("error on request: ${resp.statusLine} : ${reader}")
            }
        }
        http.shutdown()
        result
    }

    void defineMapFields() {
        //define map fields
        tippStatus.put(RDStore.TIPP_STATUS_CURRENT.value,RDStore.TIPP_STATUS_CURRENT)
        tippStatus.put(RDStore.TIPP_STATUS_DELETED.value,RDStore.TIPP_STATUS_DELETED)
        tippStatus.put(RDStore.TIPP_STATUS_RETIRED.value,RDStore.TIPP_STATUS_RETIRED)
        tippStatus.put(RDStore.TIPP_STATUS_EXPECTED.value,RDStore.TIPP_STATUS_EXPECTED)
        tippStatus.put(RDStore.TIPP_STATUS_TRANSFERRED.value,RDStore.TIPP_STATUS_TRANSFERRED)
        tippStatus.put(RDStore.TIPP_STATUS_UNKNOWN.value,RDStore.TIPP_STATUS_UNKNOWN)
        titleStatus.put(RDStore.TITLE_STATUS_CURRENT.value,RDStore.TITLE_STATUS_CURRENT)
        titleStatus.put(RDStore.TITLE_STATUS_RETIRED.value,RDStore.TITLE_STATUS_RETIRED)
        titleStatus.put(RDStore.TITLE_STATUS_DELETED.value,RDStore.TITLE_STATUS_DELETED)
        //this complicated way is necessary because of static in order to avoid a NonUniqueObjectException
        List<RefdataValue> staticMediumTypes = [RDStore.TITLE_TYPE_DATABASE,RDStore.TITLE_TYPE_EBOOK,RDStore.TITLE_TYPE_JOURNAL]
        RefdataValue.findAllByIdNotInListAndOwner(staticMediumTypes.collect { RefdataValue rdv -> rdv.id },RefdataCategory.findByDesc(RDConstants.TITLE_MEDIUM)).each { RefdataValue rdv ->
            titleMedium.put(rdv.value,rdv)
        }
        staticMediumTypes.each { RefdataValue rdv ->
            titleMedium.put(rdv.value,rdv)
        }
        RefdataCategory.getAllRefdataValues(RDConstants.PACKAGE_STATUS).each { RefdataValue rdv ->
            packageStatus.put(rdv.value,rdv)
        }
        RefdataCategory.getAllRefdataValues(RDConstants.ORG_STATUS).each { RefdataValue rdv ->
            orgStatus.put(rdv.value,rdv)
        }
        RefdataCategory.getAllRefdataValues(RDConstants.CURRENCY).each { RefdataValue rdv ->
            currency.put(rdv.value,rdv)
        }
    }
}
