package de.laser

import de.laser.base.AbstractCoverage
import de.laser.base.AbstractLockableService
import de.laser.config.ConfigMapper
import de.laser.exceptions.SyncException
import de.laser.finance.PriceItem
import de.laser.http.BasicHttpClient
import de.laser.remote.ApiSource
import de.laser.remote.GlobalRecordSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.system.SystemEvent
import de.laser.titles.TitleHistoryEvent
import de.laser.utils.DateUtils
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import org.hibernate.Session

import java.text.SimpleDateFormat
import java.time.Duration
import java.util.concurrent.ExecutorService

/**
 * Implements the synchronisation workflow with the we:kb. It is currently used for title data and provider (organisation) data
 * and triggers the subscription holding notifications
 */
@Transactional
class GlobalSourceSyncService extends AbstractLockableService {

    EscapeService escapeService
    ExecutorService executorService
    GlobalService globalService
    PendingChangeService pendingChangeService
    PackageService packageService
    ApiSource apiSource
    GlobalRecordSource source

    static final long RECTYPE_PACKAGE = 0
    static final long RECTYPE_PLATFORM = 1
    static final long RECTYPE_ORG = 2
    static final long RECTYPE_TIPP = 3
    static final String PERMANENTLY_DELETED = "Permanently Deleted"
    static final int MAX_CONTENT_LENGTH = 1024 * 1024 * 100
    static final int MAX_TIPP_COUNT_PER_PAGE = 20000

    Map<String, RefdataValue> titleMedium = [:],
            tippStatus = [:],
            packageStatus = [:],
            orgStatus = [:],
            orgTypes = [:],
            currency = [:],
            accessType = [:],
            openAccess = [:],
            ddc = [:],
            contactTypes = [:]
    Long maxTimestamp
    //Map<String,Set<Map<String,Object>>> packagesToNotify
    Set<String> packagesAlreadyUpdated, platformsAlreadyUpdated
    //Map<String,Integer> initialPackagesCounter
    //Map<String,Set<Map<String,Object>>> pkgPropDiffsContainer

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
     * The sync process wrapper. It takes every {@link GlobalRecordSource}, fetches the information from a given timestamp onwards
     * and updates the local records
     */
    void doSync() {
        running = true
        defineMapFields()
        //we need to consider that there may be several sources per instance
        List<GlobalRecordSource> jobs = GlobalRecordSource.findAllByActive(true)
        jobs.each { GlobalRecordSource source ->
            this.source = source
            maxTimestamp = 0
            try {
                SystemEvent.createEvent('GSSS_JSON_START',['jobId':source.id])
                Thread.currentThread().setName("GlobalDataSync_Json")
                this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                Date oldDate = source.haveUpTo
                //Date oldDate = DateUtils.getSDF_ymd().parse('2022-01-01') //debug only
                log.info("getting records from job #${source.id} with uri ${source.uri} since ${oldDate}")
                SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd_HHmmss()
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
                    case RECTYPE_ORG: componentType = 'Org'
                        break
                    case RECTYPE_PACKAGE: componentType = 'Package'
                        break
                    case RECTYPE_PLATFORM: componentType = 'Platform'
                        break
                    case RECTYPE_TIPP: componentType = 'TitleInstancePackagePlatform'
                        break
                }
                //do prequest: are we needing the scroll api?
                Map<String,Object> result = fetchRecordJSON(false,[componentType:componentType,changedSince:sdf.format(oldDate),max:MAX_TIPP_COUNT_PER_PAGE]) //changedBefore:'2022-09-27 00:00:00',
                if(result.error == 404) {
                    log.error("we:kb server is down")
                    SystemEvent.createEvent('GSSS_JSON_ERROR',['jobId':source.id])
                }
                else {
                    if(result) {
                        Set<String> permanentlyDeletedTitles = []
                        if(source.rectype == RECTYPE_TIPP) {
                            permanentlyDeletedTitles = getPermanentlyDeletedTitles(sdf.format(oldDate))
                        }
                        processScrollPage(result, componentType, sdf.format(oldDate), null, permanentlyDeletedTitles)
                    }
                    else {
                        log.info("no records updated - leaving everything as is ...")
                    }
                    if(source.rectype == RECTYPE_TIPP) {
                        /*
                        if(packagesToNotify.keySet().size() > 0) {
                            log.info("notifying subscriptions ...")
                            Map<String, Set<TitleChange>> packageChanges = trackPackageHistory()
                            //get subscription packages and their respective holders, parent level only!
                            packageChanges.each { String packageKey, Set<TitleChange> changes ->
                                String query = 'select oo.org,sp from SubscriptionPackage sp join sp.pkg pkg ' +
                                        'join sp.subscription s ' +
                                        'join s.orgRelations oo ' +
                                        'where s.instanceOf = null and pkg.gokbId = :packageKey ' +
                                        'and oo.roleType in (:roleTypes)'
                                List subPkgHolders = SubscriptionPackage.executeQuery(query,[packageKey:packageKey,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER]])
                                log.info("getting subscription package holders for ${packageKey}: ${subPkgHolders.toListString()}")
                                subPkgHolders.each { row ->
                                    log.debug("processing ${row[1]}")
                                    Org org = (Org) row[0]
                                    SubscriptionPackage sp = (SubscriptionPackage) row[1]
                                    autoAcceptPendingChanges(org, sp, changes)
                                    //nonAutoAcceptPendingChanges(org, sp)
                                }
                            }
                            globalService.cleanUpGorm()
                            log.info("end notifying subscriptions")
                            log.info("clearing removed titles")
                            packageService.clearRemovedTitles()
                            log.info("end clearing titles")
                        }
                        else {
                            log.info("no diffs recorded ...")
                        }
                        */
                        globalService.cleanUpGorm()
                        log.info("clearing removed titles")
                        packageService.clearRemovedTitles()
                        log.info("end clearing titles")
                    }
                    else if(source.rectype == RECTYPE_PACKAGE) {
                        /*if(packagesToNotify.keySet().size() > 0) {
                            log.info("notifying subscriptions ...")
                            Map<String, Set<TitleChange>> packageChanges = trackPackageHistory()
                        }
                        else {
                            log.info("no diffs recorded ...")
                        }*/
                    }
                    if(maxTimestamp+1000 > source.haveUpTo.getTime()) {
                        source.refresh()
                        log.debug("old ${sdf.format(source.haveUpTo)}")
                        source.haveUpTo = new Date(maxTimestamp + 1000)
                        log.debug("new ${sdf.format(source.haveUpTo)}")
                        if (!source.save())
                            log.error(source.getErrors().getAllErrors().toListString())
                    }
                    log.info("sync job finished")
                    SystemEvent.createEvent('GSSS_JSON_COMPLETE',['jobId':source.id])
                }
            }
            catch (Exception e) {
                SystemEvent.createEvent('GSSS_JSON_ERROR',['jobId':source.id])
                log.error("sync job has failed, please consult stacktrace as follows: ",e)
            }
        }
        running = false
    }

    /**
     * Updates the data for a single package, using the global update mechanism
     */
    void doSingleSync(String packageUUID) {
        running = true
        defineMapFields()
        //we need to consider that there may be several sources per instance
        List<GlobalRecordSource> jobs = GlobalRecordSource.findAllByActiveAndRectype(true, RECTYPE_TIPP)
        jobs.each { GlobalRecordSource source ->
            this.source = source
            try {
                Thread.currentThread().setName("PackageReload")
                this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                String componentType = 'TitleInstancePackagePlatform'
                //preliminary: build up list of all deleted components
                Set<String> permanentlyDeletedTitles = getPermanentlyDeletedTitles()
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
                //do prequest: are we needing the scroll api?
                Map<String,Object> result = fetchRecordJSON(false,[componentType:componentType,tippPackageUuid:packageUUID,max:MAX_TIPP_COUNT_PER_PAGE])
                if(result.error == 404) {
                    log.error("we:kb server is down")
                }
                else {
                    if(result) {
                        processScrollPage(result, componentType, null, packageUUID, permanentlyDeletedTitles)
                    }
                    else {
                        log.info("no records updated - leaving everything as is ...")
                    }
                    Package pkg = Package.findByGokbId(packageUUID)
                    if(pkg) {
                        permanentlyDeletedTitles.each { String delUUID ->
                            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbIdAndPkgAndStatusNotEqual(delUUID, pkg, RDStore.TIPP_STATUS_REMOVED)
                            if(tipp) {
                                tipp.status = RDStore.TIPP_STATUS_REMOVED
                                tipp.save()
                                IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tipp, now: new Date()])
                            }
                        }
                    }
                    /*
                    if(packagesToNotify.keySet().size() > 0) {
                        log.info("notifying subscriptions ...")
                        Map<String, Set<TitleChange>> packageChanges = trackPackageHistory()
                        //get subscription packages and their respective holders, parent level only!
                        packageChanges.each { String packageKey, Set<TitleChange> changes ->
                            String query = 'select oo.org,sp from SubscriptionPackage sp join sp.pkg pkg ' +
                                    'join sp.subscription s ' +
                                    'join s.orgRelations oo ' +
                                    'where s.instanceOf = null and pkg.gokbId = :packageKey ' +
                                    'and oo.roleType in (:roleTypes)'
                            List subPkgHolders = SubscriptionPackage.executeQuery(query,[packageKey:packageKey,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER]])
                            log.info("getting subscription package holders for ${packageKey}: ${subPkgHolders.toListString()}")
                            subPkgHolders.each { row ->
                                log.debug("processing ${row[1]}")
                                Org org = (Org) row[0]
                                SubscriptionPackage sp = (SubscriptionPackage) row[1]
                                autoAcceptPendingChanges(org, sp, changes)
                                //nonAutoAcceptPendingChanges(org, sp)
                            }
                        }
                        log.info("end notifying subscriptions")
                    }
                    else {
                        log.info("no diffs recorded ...")
                    }
                    */
                    globalService.cleanUpGorm()
                    log.info("package reload finished")
                }
            }
            catch (Exception e) {
                log.error("package reload has failed, please consult stacktrace as follows: ",e)
            }
        }
        running = false
    }

    /**
     * Reloads all data of the given component type from the connected we:kb instance connected by {@link GlobalRecordSource}
     * @param componentType the component type (one of Org, TitleInstancePackagePlatform) to update
     */
    void reloadData(String componentType) {
        running = true
        defineMapFields()
            long rectype
            switch(componentType) {
                case 'Org': rectype = RECTYPE_ORG
                    break
                case 'Package': rectype = RECTYPE_PACKAGE
                    break
                case 'Platform': rectype = RECTYPE_PLATFORM
                    break
                case 'TitleInstancePackagePlatform': rectype = RECTYPE_TIPP
                    break
                default: rectype = -1
                    break
            }
            this.source = GlobalRecordSource.findByActiveAndRectype(true,rectype)
            this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
            log.info("getting all records from job #${source.id} with uri ${source.uri}")
            try {
                Map<String,Object> result = fetchRecordJSON(false,[componentType: componentType, max: MAX_TIPP_COUNT_PER_PAGE])
                if(result) {
                    if(result.error == 404) {
                        log.error("we:kb server currently down")
                    }
                    else
                        processScrollPage(result, componentType, null)
                }
            }
            catch (Exception e) {
                log.error("package reloading has failed, please consult stacktrace as follows: ",e)
            }
            running = false
    }

    /**
     * Reloads a concrete property from the we:kb instance. Depending on the property to load, the domain objects having this property both in ElasticSearch index and in LAS:eR are being updated
     * @param dataToLoad the property to update for every object (one of identifier, ddc, language or editionStatement)
     */
    void updateData(String dataToLoad) {
        running = true
            this.source = GlobalRecordSource.findByActiveAndRectype(true,RECTYPE_TIPP)
            this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
            List<String> triggeredTypes
            int max
            switch(dataToLoad) {
                case "identifier": triggeredTypes = ['Package','Org','TitleInstancePackagePlatform']
                    max = 100
                    break
                case "abbreviatedName": triggeredTypes = ['Org']
                    max = 1000
                    break
                case "sortTitle": triggeredTypes = ['Package', 'TitleInstancePackagePlatform']
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                case "ddc": triggeredTypes = ['TitleInstancePackagePlatform']
                    RefdataCategory.getAllRefdataValues(RDConstants.DDC).each { RefdataValue rdv ->
                        ddc.put(rdv.value, rdv)
                    }
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                case "accessType": triggeredTypes = ['TitleInstancePackagePlatform']
                    RefdataCategory.getAllRefdataValues(RDConstants.TIPP_ACCESS_TYPE).each { RefdataValue rdv ->
                        accessType.put(rdv.value, rdv)
                    }
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                case "openAccess": triggeredTypes = ['TitleInstancePackagePlatform']
                    buildWekbLaserRefdataMap(RDConstants.LICENSE_OA_TYPE)
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                case [ "language", "editionStatement" ]:
                    triggeredTypes = ['TitleInstancePackagePlatform']
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                case "titleNamespace": triggeredTypes = ['Platform']
                    max = MAX_TIPP_COUNT_PER_PAGE
                    break
                default: triggeredTypes = []
                    break
            }
            try {
                triggeredTypes.each { String componentType ->
                    GlobalRecordSource.withNewSession { Session sess ->
                        int offset = 0
                        Map<String, Object> queryParams = [componentType: componentType, max: max, offset: offset]
                        Map<String,Object> result = fetchRecordJSON(false,queryParams)
                        if(result) {
                            if(result.error == 404) {
                                log.error("we:kb server currently down")
                            }
                            else {
                                boolean more = true
                                while(more) {
                                    log.debug("processing entries ${offset}-${offset+max} for ${componentType} ...")
                                    Map<String, Map> wekbRecords = result.records.collectEntries { Map entry -> [entry.uuid, entry] }
                                    switch(componentType) {
                                        case 'Package': List<Package> packages = Package.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from current page, ${packages.size()} packages exist in LAS:eR")
                                            packages.eachWithIndex { Package pkg, int idx ->
                                                log.debug("now processing package ${idx} with uuid ${pkg.gokbId}, total entry: ${offset+idx}")
                                                if(dataToLoad == 'identifier') {
                                                    List identifiers = wekbRecords.get(pkg.gokbId).identifiers
                                                    if(identifiers) {
                                                        if(pkg.ids) {
                                                            Identifier.executeUpdate('delete from Identifier i where i.pkg = :pkg',[pkg:pkg]) //damn those wrestlers ...
                                                        }
                                                        identifiers.each { id ->
                                                            if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                                                                Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: pkg, isUnique: false, nsType: Package.class.name])
                                                            }
                                                        }
                                                    }
                                                }
                                                else if(dataToLoad == 'sortTitle') {
                                                    pkg.sortname = escapeService.generateSortTitle(pkg.name)
                                                    pkg.save()
                                                }
                                            }
                                            break
                                        case 'Platform': List<Platform> platforms = Platform.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from current page, ${platforms.size()} packages exist in LAS:eR")
                                            platforms.eachWithIndex { Platform platform, int idx ->
                                                log.debug("now processing platform ${idx} with uuid ${platform.gokbId}, total entry: ${offset+idx}")
                                                platform.titleNamespace = wekbRecords.get(platform.gokbId).titleNamespace
                                                platform.save()
                                            }
                                            break
                                        case 'Org': List<Org> providers = Org.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from current page, ${providers.size()} providers exist in LAS:eR")
                                            providers.eachWithIndex { Org provider, int idx ->
                                                log.debug("now processing org ${idx} with uuid ${provider.gokbId}, total entry: ${offset+idx}")
                                                switch(dataToLoad) {
                                                    case "identifier":
                                                        List identifiers = wekbRecords.get(provider.gokbId).identifiers
                                                        if(identifiers) {
                                                            if(provider.ids) {
                                                                Identifier.executeUpdate('delete from Identifier i where i.org = :org',[org:provider]) //damn those wrestlers ...
                                                            }
                                                            identifiers.each { id ->
                                                                if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                                                                    Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: provider, isUnique: false, nsType: Org.class.name])
                                                                }
                                                            }
                                                        }
                                                        break
                                                    case "abbreviatedName": provider.sortname = wekbRecords.get(provider.gokbId).abbreviatedName
                                                        provider.save()
                                                        break
                                                }
                                            }
                                            break
                                        case 'TitleInstancePackagePlatform': List<TitleInstancePackagePlatform> tipps = TitleInstancePackagePlatform.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from this page, ${tipps.size()} TIPPs do exist in LAS:eR")
                                            tipps.eachWithIndex { TitleInstancePackagePlatform tipp, int idx ->
                                                log.debug("now processing tipp ${idx} with uuid ${tipp.gokbId}, total entry: ${offset+idx}")
                                                switch(dataToLoad) {
                                                    case "identifier":
                                                        List identifiers = wekbRecords.get(tipp.gokbId).identifiers
                                                        Set<String> oldIds = []
                                                        if(tipp.ids)
                                                            oldIds.addAll(tipp.ids.collect { Identifier id -> id.value })
                                                        if(identifiers) {
                                                            if(oldIds) {
                                                                Identifier.executeUpdate('delete from Identifier i where i.tipp = :tipp and i.value not in (:newValues)', [tipp: tipp, newValues: identifiers.collect { id -> id.value }]) //damn those wrestlers ...
                                                            }
                                                            identifiers.each { id ->
                                                                if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                                                                    if(!(id.value in (oldIds)))
                                                                        Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: tipp, isUnique: false, nsType: TitleInstancePackagePlatform.class.name])
                                                                }
                                                            }
                                                        }
                                                        break
                                                    case "ddc":
                                                        List ddcs = wekbRecords.get(tipp.gokbId).ddcs
                                                        Set<String> oldDdcs = []
                                                        if(tipp.ddcs)
                                                            oldDdcs.addAll(tipp.ddcs.collect { DeweyDecimalClassification ddc -> ddc.ddc.value })
                                                        if(ddcs) {
                                                            if(oldDdcs) {
                                                                DeweyDecimalClassification.executeUpdate('delete from DeweyDecimalClassification ddc where ddc.id in (select ddc.id from DeweyDecimalClassification ddc join ddc.ddc rdv where ddc.tipp = :tipp and rdv.value not in (:newDdcs))', [tipp: tipp, newDdcs: ddcs.collect { ddc -> ddc.value }])
                                                            }
                                                            ddcs.each { ddcData ->
                                                                if(!(ddcData.value in (oldDdcs))) {
                                                                    DeweyDecimalClassification.construct([ddc: ddc.get(ddcData.value), tipp: tipp])
                                                                }
                                                            }
                                                        }
                                                        break
                                                    case "language":
                                                        List languages = wekbRecords.get(tipp.gokbId).languages
                                                        Set<String> oldLanguages = []
                                                        if(tipp.languages)
                                                            oldLanguages.addAll(tipp.languages.collect { Language language -> language.language.value })
                                                        if(languages) {
                                                            if(oldLanguages) {
                                                                //the direct way would require a join in a delete query which is not allowed
                                                                Language.executeUpdate('delete from Language lang where lang.id in (select lang.id from Language lang join lang.language rdv where lang.tipp = :tipp and rdv.value not in (:newLangs))', [tipp: tipp, newLangs: languages.collect { lang -> lang.value }])
                                                            }
                                                            languages.each { langData ->
                                                                if(!(langData.value in (oldLanguages))) {
                                                                    Language.construct([language: RefdataValue.getByValueAndCategory(langData.value,RDConstants.LANGUAGE_ISO), tipp: tipp])
                                                                }
                                                            }
                                                        }
                                                        break
                                                    case "editionStatement":
                                                        tipp.editionStatement = wekbRecords.get(tipp.gokbId).editionStatement
                                                        tipp.save()
                                                        break
                                                    case "accessType":
                                                        String newAccessType = wekbRecords.get(tipp.gokbId).accessType
                                                        tipp.accessType = accessType.get(newAccessType)
                                                        tipp.save()
                                                        break
                                                    case "openAccess":
                                                        String newOpenAccess = wekbRecords.get(tipp.gokbId).openAccess
                                                        tipp.openAccess = openAccess.get(newOpenAccess)
                                                        tipp.save()
                                                        break
                                                    case "sortTitle":
                                                        tipp.name = wekbRecords.get(tipp.gokbId).name
                                                        tipp.sortname = escapeService.generateSortTitle(tipp.name)
                                                        tipp.save()
                                                        break
                                                }
                                            }
                                            log.debug("interim flush at end of load: ${offset}")
                                            sess.flush()
                                            break
                                    }
                                    more = result.currentPage < result.lastPage
                                    if(more) {
                                        //String scrollId = result.scrollId
                                        offset += max
                                        queryParams.offset = offset
                                        //log.debug("using scrollId ${scrollId}")
                                        //result = fetchRecordJSON(false, queryParams+[scrollId: scrollId])
                                        result = fetchRecordJSON(false, queryParams)
                                    }
                                    sess.flush()
                                    sess.clear()
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                log.error("component reloading has failed, please consult stacktrace as follows: ",e)
            }
            running = false
    }

    /**
     * Takes the prequest result and enters the data load loop until all scroll pages are being processed
     * @param result the prequest result, containing the set of records and/or the information whether there are further records to process
     * @param componentType the object type (TitleInstancePackagePlatform or Org) to update
     * @param changedSince the timestamp from which new records should be loaded
     * @param pkgFilter an optional package filter to restrict the data to be loaded
     * @throws SyncException if an error occurs during the update process
     */
    void processScrollPage(Map<String, Object> result, String componentType, String changedSince, String pkgFilter = null, Set<String> permanentlyDeletedTitles = []) throws SyncException {
        if(result.count >= MAX_TIPP_COUNT_PER_PAGE) {
            int offset = 0, max = MAX_TIPP_COUNT_PER_PAGE
            Map<String, Object> queryParams = [componentType: componentType, offset: offset, max: max]
            if(changedSince) {
                queryParams.changedSince = changedSince
                //queryParams.changedBefore = "2022-09-27 00:00:00" //debug only
            }
            if(pkgFilter)
                queryParams.tippPackageUuid = pkgFilter
            if(!result.containsKey('error')) {
                while(result.currentPage < result.lastPage) {
                    //actually, scrollId alone should do the trick but tests revealed that other parameters are necessary, too, because of current workaround solution
                    //log.debug("using scrollId ${scrollId}")
                    result = fetchRecordJSON(false, queryParams)
                    log.debug("-------------- processing page ${result.currentPage} out of ${result.lastPage} ------------------")
                    if(result.count > 0) {
                        switch (source.rectype) {
                            case RECTYPE_ORG:
                                result.records.each { record ->
                                    record.platforms.each { Map platformData ->
                                        try {
                                            createOrUpdatePlatform(platformData.uuid)
                                        }
                                        catch (SyncException e) {
                                            log.error("Error on updating platform ${platformData.uuid}: ",e)
                                            SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                                        }
                                    }
                                    createOrUpdateOrg(record)
                                }
                                break
                            case RECTYPE_PACKAGE:
                                result.records.eachWithIndex { record, int i ->
                                    try {
                                        log.debug("now processing record #${i}, total #${i+offset}")
                                        Package pkg = createOrUpdatePackage(record)
                                        if(pkg.packageStatus == RDStore.PACKAGE_STATUS_REMOVED) {
                                            log.info("${pkg.name} / ${pkg.gokbId} has been removed, mark titles in package as removed ...")
                                            log.info("${IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg) and ie.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])} issue entitlements marked as removed")
                                            log.info("${TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :removed, tipp.lastUpdated = :now where tipp.pkg = :pkg and tipp.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])} package titles (tipps) marked as removed")
                                            /*
                                            TitleInstancePackagePlatform.findAllByPkgAndStatusNotEqual(pkg, RDStore.TIPP_STATUS_REMOVED).each { TitleInstancePackagePlatform tipp ->
                                                tipp.status = RDStore.TIPP_STATUS_REMOVED
                                                TitleChange.construct([event: PendingChangeConfiguration.TITLE_REMOVED, tipp: tipp])
                                                tipp.save()
                                            }
                                            */
                                        }
                                    }
                                    catch (SyncException e) {
                                        log.error("Error on updating package ${record.uuid}: ",e)
                                        SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey: record.uuid])
                                    }
                                }
                                break
                            case RECTYPE_PLATFORM:
                                result.records.each { record ->
                                    try {
                                        createOrUpdatePlatform(record)
                                    }
                                    catch (SyncException e) {
                                        log.error("Error on updating platform ${record.uuid}: ",e)
                                        SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:record.uuid])
                                    }
                                }
                                break
                            case RECTYPE_TIPP:
                                if(offset == 0)
                                    updateRecords(result.records, offset, permanentlyDeletedTitles)
                                else updateRecords(result.records, offset)
                                break
                        }
                        if(result.currentPage < result.lastPage) {
                            //scrollId = result.scrollId
                            //flush after one page ... seems that Grails 3 onwards fills memory, too!
                            offset += max
                            queryParams.offset = offset
                            if(offset % MAX_TIPP_COUNT_PER_PAGE == 0 && offset > 0) {
                                globalService.cleanUpGorm()
                            }
                        }
                    }
                    else {
                        log.info("no records updated - leaving everything as is ...")
                    }
                }
            }
        }
        else if(result.count > 0 && result.count < MAX_TIPP_COUNT_PER_PAGE) {
            switch (source.rectype) {
                case RECTYPE_ORG:
                    result.records.each { record ->
                        record.platforms.each { Map platformData ->
                            try {
                                createOrUpdatePlatform(platformData.uuid)
                            }
                            catch (SyncException e) {
                                log.error("Error on updating platform ${platformData.uuid}: ",e)
                                SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                            }
                        }
                        createOrUpdateOrg(record)
                    }
                    break
                case RECTYPE_PACKAGE:
                    result.records.eachWithIndex { record, int i ->
                        try {
                            log.debug("now processing record #${i}")
                            Package pkg = createOrUpdatePackage(record)
                            //package may be null in case it has been marked as removed and did not exist in LAS:eR before
                            if(pkg?.packageStatus == RDStore.PACKAGE_STATUS_REMOVED) {
                                log.info("${pkg.name} / ${pkg.gokbId} has been removed, record status is ${record.status}, mark titles in package as removed ...")
                                log.info("${IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg) and ie.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])} issue entitlements marked as removed")
                                log.info("${TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :removed, tipp.lastUpdated = :now where tipp.pkg = :pkg and tipp.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])} package titles (tipps) marked as removed")
                                /*
                                TitleInstancePackagePlatform.findAllByPkgAndStatusNotEqual(pkg, RDStore.TIPP_STATUS_REMOVED).each { TitleInstancePackagePlatform tipp ->
                                    tipp.status = RDStore.TIPP_STATUS_REMOVED
                                    IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [tipp: tipp, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])
                                    //TitleChange.construct([event: PendingChangeConfiguration.TITLE_REMOVED, tipp: tipp])
                                    tipp.save()
                                }
                                */
                            }
                            if(i % 500 == 0 && i > 0) {
                                globalService.cleanUpGorm()
                            }
                        }
                        catch (SyncException e) {
                            log.error("Error on updating package ${record.uuid}: ",e)
                            SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:record.uuid])
                        }
                    }
                    break
                case RECTYPE_PLATFORM:
                    result.records.each { record ->
                        try {
                            createOrUpdatePlatform(record)
                        }
                        catch (SyncException e) {
                            log.error("Error on updating platform ${record.uuid}: ",e)
                            SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:record.uuid])
                        }
                    }
                    break
                case RECTYPE_TIPP: updateRecords(result.records, 0, permanentlyDeletedTitles)
                    break
            }
        }
        else if(result.error && result.error == 404) {
            log.error("we:kb server is down")
            throw new SyncException("we:kb server is unavailable!")
        }
    }

    /**
     * Updates the records on the given page
     * @param rawRecords the scroll page (JSON result) containing the updated entries
     * @param offset the total record counter offset which has to be added to the entry loop counter
     */
    void updateRecords(List<Map> rawRecords, int offset, Set<String> permanentlyDeletedTitles = []) {
        //necessary filter for DEV database
        List<Map> records = rawRecords.findAll { Map tipp -> tipp.containsKey("hostPlatformUuid") && tipp.containsKey("tippPackageUuid") }
        Set<String> platformUUIDs = records.collect { Map tipp -> tipp.hostPlatformUuid } as Set<String>
        //log.debug("found platform UUIDs: ${platformUUIDs.toListString()}")
        Set<String> packageUUIDs = records.collect { Map tipp -> tipp.tippPackageUuid } as Set<String>
        //log.debug("found package UUIDs: ${packageUUIDs.toListString()}")
        Set<String> tippUUIDs = records.collect { Map tipp -> tipp.uuid } as Set<String>
        Map<String,Package> packagesOnPage = [:]
        Map<String,Platform> platformsOnPage = [:]

        //packageUUIDs is null if package has no tipps
        //Set<String> existingPackageUUIDs = packageUUIDs ? Platform.executeQuery('select pkg.gokbId from Package pkg where pkg.gokbId in (:pkgUUIDs)',[pkgUUIDs:packageUUIDs]) : []
        Map<String,TitleInstancePackagePlatform> tippsInLaser = [:]
        //collect existing TIPPs and purge deleted ones
        if(tippUUIDs) {
            TitleInstancePackagePlatform.findAllByGokbIdInList(tippUUIDs.toList()).each { TitleInstancePackagePlatform tipp ->
                tippsInLaser.put(tipp.gokbId, tipp)
            }
        }
        //create or update platforms
        platformUUIDs.each { String platformUUID ->
            try {
                if(platformsAlreadyUpdated.add(platformUUID))
                    platformsOnPage.put(platformUUID,createOrUpdatePlatform(platformUUID))
                else
                    platformsOnPage.put(platformUUID,Platform.findByGokbId(platformUUID))
            }
            catch (SyncException e) {
                log.error("Error on updating platform ${platformUUID}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformUUID])
            }
        }
        //create or update packages
        packageUUIDs.each { String packageUUID ->
            try {
                if(packagesAlreadyUpdated.add(packageUUID)) {
                    Package pkg = createOrUpdatePackage(packageUUID)
                    if (pkg)
                        packagesOnPage.put(packageUUID, pkg)
                }
                else {
                    packagesOnPage.put(packageUUID, Package.findByGokbId(packageUUID))
                }
            }
            catch (SyncException e) {
                log.error("Error on updating package ${packageUUID}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:packageUUID])
            }
        }

        Set<TitleInstancePackagePlatform> newTitles = []
        records.eachWithIndex { Map tipp, int idx ->
            log.debug("now processing entry #${idx}, total entry #${offset+idx} with uuid ${tipp.uuid}")
            try {
                Map<String,Object> updatedTIPP = [
                    titleType: tipp.titleType,
                    name: tipp.name,
                    altnames: [],
                    packageUUID: tipp.tippPackageUuid?.trim() ?: null,
                    platformUUID: tipp.hostPlatformUuid?.trim() ?: null,
                    titlePublishers: [],
                    publisherName: tipp.publisherName,
                    firstAuthor: tipp.firstAuthor?.trim() ?: null,
                    firstEditor: tipp.firstEditor?.trim() ?: null,
                    editionStatement: tipp.editionStatement?.trim() ?: null,
                    dateFirstInPrint: tipp.dateFirstInPrint ? DateUtils.parseDateGeneric(tipp.dateFirstInPrint) : null,
                    dateFirstOnline: tipp.dateFirstOnline ? DateUtils.parseDateGeneric(tipp.dateFirstOnline) : null,
                    imprint: tipp.titleImprint?.trim() ?: null,
                    status: tipp.status,
                    seriesName: tipp.series?.trim() ?: null,
                    subjectReference: tipp.subjectArea?.trim() ?: null,
                    volume: tipp.volumeNumber?.trim() ?: null,
                    coverages: [],
                    priceItems: [],
                    hostPlatformURL: tipp.url?.trim() ?: null,
                    identifiers: [],
                    ddcs: [],
                    languages: [],
                    history: [],
                    uuid: tipp.uuid,
                    accessStartDate : tipp.accessStartDate ? DateUtils.parseDateGeneric(tipp.accessStartDate) : null,
                    accessEndDate   : tipp.accessEndDate ? DateUtils.parseDateGeneric(tipp.accessEndDate) : null,
                    medium: tipp.medium,
                    accessType: tipp.accessType,
                    openAccess: tipp.openAccess
                ]
                if(tipp.titleType == 'Journal') {
                    tipp.coverage.each { cov ->
                        updatedTIPP.coverages << [
                                startDate: cov.startDate ? DateUtils.parseDateGeneric(cov.startDate) : null,
                                endDate: cov.endDate ? DateUtils.parseDateGeneric(cov.endDate) : null,
                                startVolume: cov.startVolume?.trim() ?: null,
                                endVolume: cov.endVolume?.trim() ?: null,
                                startIssue: cov.startIssue?.trim() ?: null,
                                endIssue: cov.endIssue?.trim() ?: null,
                                coverageDepth: cov.coverageDepth?.trim() ?: null,
                                coverageNote: cov.coverageNote?.trim() ?: null,
                                embargo: cov.embargo?.trim() ?: null
                        ]
                    }
                    updatedTIPP.coverages = updatedTIPP.coverages.toSorted { a, b -> a.startDate <=> b.startDate }
                }
                tipp.prices.each { price ->
                    updatedTIPP.priceItems << [
                            listPrice: price.amount instanceof String ? escapeService.parseFinancialValue(price.amount) : BigDecimal.valueOf(price.amount),
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
                tipp.altname.each { altname ->
                    updatedTIPP.altnames << altname
                }
                tipp.ddcs.each { ddcData ->
                    updatedTIPP.ddcs << ddc.get(ddcData.value)
                }
                tipp.languages.each { langData ->
                    updatedTIPP.languages << RefdataValue.getByValueAndCategory(langData.value, RDConstants.LANGUAGE_ISO)
                }
                tipp.titleHistory.each { historyEvent ->
                    updatedTIPP.history << [date:DateUtils.parseDateGeneric(historyEvent.date),from:historyEvent.from,to:historyEvent.to]
                }
                if(Package.findByGokbId(updatedTIPP.packageUUID)) {
                    TitleInstancePackagePlatform newTitle = createOrUpdateTIPP(tippsInLaser.get(updatedTIPP.uuid),updatedTIPP,packagesOnPage,platformsOnPage)
                    if(newTitle)
                        newTitles << newTitle
                    /*
                    Map<String,Object> diffs = createOrUpdateTIPP(tippsInLaser.get(updatedTIPP.uuid),updatedTIPP,packagesOnPage,platformsOnPage)
                    Set<Map<String,Object>> diffsOfPackage = packagesToNotify.get(updatedTIPP.packageUUID)
                    if(!diffsOfPackage) {
                        diffsOfPackage = []
                    }
                    diffsOfPackage << diffs
                    if(pkgPropDiffsContainer.get(updatedTIPP.packageUUID)) {
                        diffsOfPackage.addAll(pkgPropDiffsContainer.get(updatedTIPP.packageUUID))
                    }
                    //test with set, otherwise make check
                    packagesToNotify.put(updatedTIPP.packageUUID,diffsOfPackage)
                    */
                }
                else if(updatedTIPP.status == PERMANENTLY_DELETED) {
                    TitleInstancePackagePlatform tippA = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(updatedTIPP.uuid, RDStore.TIPP_STATUS_REMOVED)
                    if(tippA) {
                        log.info("TIPP with UUID ${tippA.gokbId} has been permanently deleted")
                        tippA.status = RDStore.TIPP_STATUS_REMOVED
                        tippA.save()
                        IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tippA, now: new Date()])
                        /*
                        Set<Map<String,Object>> diffsOfPackage = packagesToNotify.get(tippA.pkg.gokbId)
                        if(!diffsOfPackage) {
                            diffsOfPackage = []
                        }
                        diffsOfPackage << [event: "remove", target: tippA]
                        packagesToNotify.put(tippA.pkg.gokbId,diffsOfPackage)
                        */
                    }
                }
                else if(!(updatedTIPP.status in [RDStore.TIPP_STATUS_REMOVED.value, PERMANENTLY_DELETED])) {
                    Package pkg = packagesOnPage.get(updatedTIPP.packageUUID)
                    if(pkg)
                        newTitles << addNewTIPP(pkg, updatedTIPP, platformsOnPage)
                }
                if(source.rectype == RECTYPE_TIPP) {
                    Date lastUpdatedTime = DateUtils.parseDateGeneric(tipp.lastUpdatedDisplay)
                    if(lastUpdatedTime.getTime() > maxTimestamp) {
                        maxTimestamp = lastUpdatedTime.getTime()
                    }
                }
            }
            catch (SyncException e) {
                log.error("Error on updating tipp ${tipp.uuid}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[tippRecordKey:tipp.uuid])
            }
        }
        permanentlyDeletedTitles.each { String delUUID ->
            TitleInstancePackagePlatform tippA = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(delUUID, RDStore.TIPP_STATUS_REMOVED)
            if(tippA && tippA.pkg.gokbId in packageUUIDs) {
                log.info("TIPP with UUID ${tippA.gokbId} has been permanently deleted")
                tippA.status = RDStore.TIPP_STATUS_REMOVED
                tippA.save()
                IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tippA, now: new Date()])
                /*
                Set<Map<String,Object>> diffsOfPackage = packagesToNotify.get(tippA.pkg.gokbId)
                if(!diffsOfPackage) {
                    diffsOfPackage = []
                }
                diffsOfPackage << [event: "remove", target: tippA]
                packagesToNotify.put(tippA.pkg.gokbId,diffsOfPackage)
                */
            }
        }
        Date now = new Date()
        newTitles.each { TitleInstancePackagePlatform tipp ->
            Set<Subscription> subsConcerned = Subscription.executeQuery('select s from Subscription s join s.packages sp where s.startDate is not null and s.startDate <= :now and s.endDate is not null and s.endDate >= :now and s.holdingSelection = :entire and sp.pkg = :pkg', [now: now, entire: RDStore.SUBSCRIPTION_HOLDING_ENTIRE, pkg: tipp.pkg])
            //we may need to switch to native sql ...
            subsConcerned.each { Subscription s ->
                IssueEntitlement.construct([subscription: s, tipp: tipp])
            }
        }
    }

    /**
     * This records the package changes so that subscription holders may decide whether they apply them or not except price changes which are auto-applied
     * @param packagesToTrack the packages to be tracked
     */
    Map<String, Set<TitleChange>> trackPackageHistory() {
        Map<String, Set<TitleChange>> result = [:]
        //Package.withSession { Session sess ->
            //loop through all packages
            packagesToNotify.each { String packageUUID, Set<Map<String,Object>> diffsOfPackage ->
                Set<TitleChange> packageChanges = []
                //println("diffsOfPackage:"+diffsOfPackage)
                diffsOfPackage.each { Map<String,Object> diff ->
                    log.debug(diff.toMapString())
                    //[event:update, target:de.laser.TitleInstancePackagePlatform : 196477, diffs:[[prop:price, priceDiffs:[[event:add, target:de.laser.finance.PriceItem : 10791]]]]]
                    switch(diff.event) {
                        case 'add': packageChanges << TitleChange.construct([event:PendingChangeConfiguration.NEW_TITLE,tipp:diff.target])
                            break
                        /* changed as of ERMS-4585, comment February 27th and ERMS-4986
                        case 'update':
                            diff.diffs.each { tippDiff ->
                                TitleChange.construct([event:PendingChangeConfiguration.TITLE_UPDATED,target:diff.target,prop:tippDiff.prop,newValue:tippDiff.newValue,oldValue:tippDiff.oldValue])
                            }
                            break
                            */
                        case 'statusChange': TitleChange.construct([event: PendingChangeConfiguration.TITLE_STATUS_CHANGED,tipp:diff.target,oldValue:diff.oldValue,newValue:diff.newValue]) //notification only, to be applied directly
                            break
                        case 'delete': TitleChange.construct([event:PendingChangeConfiguration.TITLE_DELETED,tipp:diff.target,oldValue:diff.oldValue]) //dealt elsewhere!
                            break
                        case 'remove': TitleChange.construct([event:PendingChangeConfiguration.TITLE_REMOVED,tipp:diff.target,oldValue:diff.oldValue]) //dealt elsewhere!
                            break
                        case 'pkgPropDiffs':
                            diff.diffs.each { pkgPropDiff ->
                                packageChanges << PendingChange.construct([msgToken: PendingChangeConfiguration.PACKAGE_PROP, target: diff.target, prop: pkgPropDiff.prop, newValue: pkgPropDiff.newValue, oldValue: pkgPropDiff.oldValue, status: RDStore.PENDING_CHANGE_HISTORY])
                            }
                            break
                    }
                    //PendingChange.construct([msgToken,target,status,prop,newValue,oldValue])
                }
                //sess.flush()
                result.put(packageUUID, packageChanges)
            }
        //}
        log.info("end tracking package changes")
        result
    }

    /**
     * Applies every change done on entitlement or coverage to subscription packages where the change setting is set to accept
     * @param contextOrg the {@link Org} for which the holding change is being applied (and on whose dashboard the changes will appear)
     * @param subPkg the {@link SubscriptionPackage} to which the changes should be applied
     * @param packageChanges the changes recorded for the package
     * @see PendingChangeConfiguration
     * @see TitleChange
     */
    void autoAcceptPendingChanges(Org contextOrg, SubscriptionPackage subPkg, Set<TitleChange> packageChanges) {
        //get for each subscription package the tokens which should be accepted
        String query = 'select pcc.settingKey from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where pcc.settingValue = :accept and sp = :sp '
        List<String> pendingChangeConfigurations = PendingChangeConfiguration.executeQuery(query,[accept:RDStore.PENDING_CHANGE_CONFIG_ACCEPT,sp:subPkg])
        if(pendingChangeConfigurations) {
            //Map<String,Object> changeParams = [pkg:subPkg.pkg,history:RDStore.PENDING_CHANGE_HISTORY,subscriptionJoin:subPkg.dateCreated,msgTokens:pendingChangeConfigurations,oid:genericOIDService.getOID(subPkg.subscription),accepted:RDStore.PENDING_CHANGE_ACCEPTED]
            //Set<PendingChange> acceptedChanges = PendingChange.findAllByOidAndStatusAndMsgTokenIsNotNull(genericOIDService.getOID(subPkg.subscription),RDStore.PENDING_CHANGE_ACCEPTED)
            /*newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = pc.targetProperty and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and pc.targetProperty = null and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = null and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = pc.targetProperty and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and pc.targetProperty = null and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = null and pca.status = :accepted)',changeParams))*/
            //newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.priceItem pi join pi.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            packageChanges.each { TitleChange newChange ->
                if(newChange.event in pendingChangeConfigurations) {

                    //if(!processed) {
                        /*
                        get each change for each subscribed package and token, fetch issue entitlement equivalent and process the change
                        if a change is being accepted, create an issue entitlement change record pointing to that change
                         */
                        pendingChangeService.applyPendingChange(newChange,subPkg,contextOrg)
                    //}
                }
            }
        }
    }

    /**
     * Updates a given {@link TitleInstancePackagePlatform} record (record A) to the state of record B, if A does not exist, it will be created if the package exists
     * @param tippA the record existing in LAS:eR (a {@link TitleInstancePackagePlatform} object)
     * @param tippB the record coming from we:kb (a {@link Map} reflecting the objectdata)
     * @param newPackages the {@link Package}s of the scroll page
     * @param newPlatforms the {@link Platform}s of the scroll page
     * @return a {@link Map} containing the recorded differences which are recorded as {@link PendingChange}s
     */
    TitleInstancePackagePlatform createOrUpdateTIPP(TitleInstancePackagePlatform tippA,Map tippB, Map<String,Package> newPackages,Map<String,Platform> newPlatforms) {
        //Map<String,Object> result = [:]
        //TitleInstancePackagePlatform.withSession { Session sess ->
            if(tippA) {
                //update or delete TIPP
                //result.putAll(processTippDiffs(tippA,tippB))
                processTippDiffs(tippA,tippB)
            }
            else {
                Package pkg = newPackages.get(tippB.packageUUID)
                //Unbelievable! But package may miss at this point!
                if(pkg && pkg?.packageStatus != RDStore.PACKAGE_STATUS_REMOVED && !(tippB.status in [PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value])) {
                    //new TIPP
                    addNewTIPP(pkg, tippB, newPlatforms)
                    //result.event = 'add'
                    //result.target = target
                }
            }
        //}
        //result
    }

    /**
     * Looks up for a given UUID if a local record exists or not. If no {@link Package} record exists, it will be
     * created with the given remote record data, otherwise, the local record is going to be updated. The {@link TitleInstancePackagePlatform records}
     * in the {@link Package} will be checked for differences and if there are such, the according fields updated. Same counts for the {@link TIPPCoverage} records
     * in the {@link TitleInstancePackagePlatform}s. If {@link Subscription}s are linked to the {@link Package}, the {@link IssueEntitlement}s (just as their
     * {@link IssueEntitlementCoverage}s) are going to be notified; it is up to the respective subscription tenants to accept the changes or not.
     * Replaces the method GokbDiffEngine.diff and the onNewTipp, onUpdatedTipp and onUnchangedTipp closures
     *
     * @param packageData A UUID pointing to record extract for a given package
     * @return
     */
    Package createOrUpdatePackage(packageInput) throws SyncException {
        Map packageJSON, packageRecord
        Package result
        if(packageInput instanceof String) {
            packageJSON = fetchRecordJSON(false, [uuid: packageInput])
            if(packageJSON.records) {
                packageRecord = (Map) packageJSON.records[0]
                result = Package.findByGokbId(packageInput)
            }
            else if(packageJSON.error == 404) {
                log.error("we:kb server is unavailable!")
                throw new SyncException("we:kb server was down!")
            }
            else {
                log.warn("Package ${packageInput} seems to be unexistent!")
                //test if local record trace exists ...
                result = Package.findByGokbId(packageInput)
                if(result) {
                    log.warn("Package found, set cascading removed ...")
                    result.packageStatus = RDStore.PACKAGE_STATUS_REMOVED
                    result.save()
                    result
                }
            }
        }
        else if(packageInput instanceof Map) {
            packageRecord = packageInput
            result = Package.findByGokbId(packageRecord.uuid)
        }
        if(packageRecord) {
            Date lastUpdatedDisplay = DateUtils.parseDateGeneric(packageRecord.lastUpdatedDisplay)
            //if(!result || result?.lastUpdated < lastUpdatedDisplay) {
                log.info("package record loaded, reconciling package record for UUID ${packageRecord.uuid}")
                RefdataValue packageRecordStatus = packageRecord.status ? packageStatus.get(packageRecord.status) : null
                RefdataValue contentType = packageRecord.contentType ? RefdataValue.getByValueAndCategory(packageRecord.contentType,RDConstants.PACKAGE_CONTENT_TYPE) : null
                RefdataValue file = packageRecord.file ? RefdataValue.getByValueAndCategory(packageRecord.file,RDConstants.PACKAGE_FILE) : null
                RefdataValue scope = packageRecord.scope ? RefdataValue.getByValueAndCategory(packageRecord.scope,RDConstants.PACKAGE_SCOPE) : null
                Map<String,Object> newPackageProps = [
                    uuid: packageInput,
                    name: packageRecord.name,
                    packageStatus: packageRecordStatus,
                    contentType: packageRecord.contentType,
                    file: file,
                    scope: scope
                ]
                if(!result && packageRecordStatus != RDStore.PACKAGE_STATUS_REMOVED) {
                    result = new Package(gokbId: packageRecord.uuid)
                }
                if(result) {
                    if(packageRecordStatus == RDStore.PACKAGE_STATUS_REMOVED && result.packageStatus != RDStore.PACKAGE_STATUS_REMOVED) {
                        log.info("package #${result.id}, with GOKb id ${result.gokbId} got removed, mark as deleted and rapport!")
                        result.packageStatus = packageRecordStatus
                    }
                    else {
                        if(packageRecord.nominalPlatformUuid) {
                            Platform nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                            if(!nominalPlatform) {
                                nominalPlatform = createOrUpdatePlatform(packageRecord.nominalPlatformUuid)
                            }
                            if(nominalPlatform)
                                newPackageProps.nominalPlatform = nominalPlatform
                        }
                        if(packageRecord.providerUuid) {
                            newPackageProps.contentProvider = Org.findByGokbId(packageRecord.providerUuid)
                        }
                        /*
                        Set<Map<String,Object>> pkgPropDiffs = getPkgPropDiff(result, newPackageProps)
                        if(pkgPropDiffs) {
                            //pkgPropDiffsContainer.put(packageInput, [event: "pkgPropUpdate", diffs: pkgPropDiffs, target: result])
                            Set<Map<String,Object>> diffsOfPackage = packagesToNotify.get(packageRecord.uuid)
                            if(!diffsOfPackage) {
                                diffsOfPackage = []
                            }
                            diffsOfPackage.addAll(pkgPropDiffs)

                            if(pkgPropDiffsContainer.get(updatedTIPP.packageUUID)) {
                                diffsOfPackage.addAll(pkgPropDiffsContainer.get(updatedTIPP.packageUUID))
                            }

                            //test with set, otherwise make check
                            packagesToNotify.put(packageRecord.uuid,diffsOfPackage)
                        }
                        */
                    }
                    result.name = packageRecord.name
                    result.sortname = escapeService.generateSortTitle(packageRecord.name)
                    result.packageStatus = packageRecordStatus
                    result.contentType = contentType
                    result.scope = scope
                    result.file = file
                    if(result.save()) {
                        if(packageRecord.nominalPlatformUuid) {
                            Platform nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                            if(!nominalPlatform) {
                                nominalPlatform = createOrUpdatePlatform(packageRecord.nominalPlatformUuid)
                            }
                            if(nominalPlatform) {
                                result.nominalPlatform = nominalPlatform
                            }
                            if (!result.save())
                                throw new SyncException(result.errors)
                        }
                        if(packageRecord.providerUuid) {
                            try {
                                Map<String, Object> providerRecord = fetchRecordJSON(false,[uuid:packageRecord.providerUuid])
                                if(providerRecord && !providerRecord.error) {
                                    Org provider = createOrUpdateOrg(providerRecord)
                                    createOrUpdatePackageProvider(provider,result)
                                }
                                else if(providerRecord && providerRecord.error == 404) {
                                    log.error("we:kb server is down")
                                    throw new SyncException("we:kb server is unvailable")
                                }
                                else
                                    throw new SyncException("Provider loading failed for UUID ${packageRecord.providerUuid}!")
                            }
                            catch (SyncException e) {
                                throw e
                            }
                        }
                        if(packageRecord.identifiers) {
                            if(result.ids) {
                                Identifier.executeUpdate('delete from Identifier i where i.pkg = :pkg',[pkg:result]) //damn those wrestlers ...
                            }
                            packageRecord.identifiers.each { id ->
                                if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                                    Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: result, isUnique: false, nsType: Package.class.name])
                                }
                            }
                        }
                        if(packageRecord.ddcs) {
                            if(result.ddcs) {
                                DeweyDecimalClassification.executeUpdate('delete from DeweyDecimalClassification ddc where ddc.pkg = :pkg',[pkg:result])
                            }
                            packageRecord.ddcs.each { ddcData ->
                                if(!DeweyDecimalClassification.construct(ddc: ddc.get(ddcData.value), pkg: result))
                                    throw new SyncException("Error on saving Dewey decimal classification! See stack trace as follows:")
                            }
                        }
                        if(packageRecord.languages) {
                            if(result.languages) {
                                Language.executeUpdate('delete from Language lang where lang.pkg = :pkg',[pkg:result])
                            }
                            packageRecord.languages.each { langData ->
                                if(!Language.construct(language: RefdataValue.getByValueAndCategory(langData.value,RDConstants.LANGUAGE_ISO), pkg: result))
                                    throw new SyncException("Error on saving language! See stack trace as follows:")
                            }
                        }
                    }
                    else {
                        throw new SyncException(result.errors)
                    }
                }
            //}
            if(source.rectype == RECTYPE_PACKAGE) {
                Date lastUpdatedTime = DateUtils.parseDateGeneric(packageRecord.lastUpdatedDisplay)
                if(lastUpdatedTime.getTime() > maxTimestamp) {
                    maxTimestamp = lastUpdatedTime.getTime()
                }
            }
            result
        }
        else null
    }

    /**
     * Was formerly in the {@link Org} domain class; deployed for better maintainability
     * Checks for a given UUID if the provider exists, otherwise, it will be created.
     *
     * @param providerUUID the GOKb UUID of the given provider {@link Org}
     * @throws SyncException
     */
    Org createOrUpdateOrg(Map<String,Object> providerJSON) throws SyncException {
        Map providerRecord
        if(providerJSON.records)
            providerRecord = providerJSON.records[0]
        else providerRecord = providerJSON
        log.info("provider record loaded, reconciling provider record for UUID ${providerRecord.uuid}")
        //first attempt
        Org provider = Org.findByGokbId(providerRecord.uuid)
        //attempt succeeded
        if(provider) {
            provider.name = providerRecord.name
        }
        //second attempt
        else if(!provider) {
            provider = Org.findByNameAndGokbIdIsNull(providerRecord.name)
            //second attempt succeeded - map gokbId to provider who already exists
            if(provider)
                provider.gokbId = providerRecord.uuid
        }
        //second attempt failed - create new org if record is not deleted
        if(!provider) {
            if(!(providerRecord.status in [PERMANENTLY_DELETED, 'Removed']))
                provider = new Org(
                        name: providerRecord.name,
                        gokbId: providerRecord.uuid
                )
        }
        //avoid creating new deleted entries
        if(provider) {
            provider.sortname = providerRecord.abbreviatedName
            provider.url = providerRecord.homepage
            if((provider.status == RDStore.ORG_STATUS_CURRENT || !provider.status) && providerRecord.status == RDStore.ORG_STATUS_RETIRED.value) {
                //value is not implemented in we:kb yet
                if(providerRecord.retirementDate) {
                    provider.retirementDate = DateUtils.parseDateGeneric(providerRecord.retirementDate)
                }
                else provider.retirementDate = new Date()
            }
            provider.status = orgStatus.get(providerRecord.status)
            if(!provider.sector)
                provider.sector = RDStore.O_SECTOR_PUBLISHER
            if(provider.save()) {
                if(!provider.getAllOrgTypeIds().contains(RDStore.OT_PROVIDER.id))
                    provider.addToOrgType(RDStore.OT_PROVIDER)
                //providedPlatforms are missing in ES output -> see GOKb-ticket #378! But maybe, it is wiser to not implement it at all
                if(providerRecord.contacts) {
                    List<String> typeNames = contactTypes.values().collect { RefdataValue cct -> cct.getI10n("value") }
                    typeNames.addAll(contactTypes.keySet())
                    List<Person> oldPersons = Person.executeQuery('select p from Person p where p.tenant = :provider and p.isPublic = true and p.last_name in (:contactTypes)',[provider: provider, contactTypes: typeNames])
                    List<Long> funcTypes = contactTypes.values().collect { RefdataValue cct -> cct.id }
                    oldPersons.each { Person old ->
                        PersonRole.executeUpdate('delete from PersonRole pr where pr.org = :provider and pr.prs = :oldPerson and pr.functionType.id in (:funcTypes)', [provider: provider, oldPerson: old, funcTypes: funcTypes])
                        Contact.executeUpdate('delete from Contact c where c.prs = :oldPerson', [oldPerson: old])
                        if(PersonRole.executeQuery('select count(pr) from PersonRole pr where pr.prs = :oldPerson and pr.org = :provider and (pr.functionType.id not in (:funcTypes) or pr.functionType = null)', [provider: provider, oldPerson: old, funcTypes: funcTypes])[0] == 0) {
                            Person.executeUpdate('delete from Person p where p = :oldPerson', [oldPerson: old])
                        }
                    }
                    providerRecord.contacts.findAll{ Map<String, String> cParams -> cParams.content != null }.each { contact ->
                        switch(contact.type) {
                            case "Metadata Contact":
                                contact.rdType = RDStore.PRS_FUNC_METADATA
                                break
                            case "Service Support":
                                contact.rdType = RDStore.PRS_FUNC_SERVICE_SUPPORT
                                break
                            case "Technical Support":
                                contact.rdType = RDStore.PRS_FUNC_TECHNICAL_SUPPORT
                                break
                            default: log.warn("unhandled additional property type for ${provider.gokbId}: ${contact.name}")
                                break
                        }
                        if(contact.rdType && contact.contentType != null) {
                            createOrUpdateSupport(provider, contact)
                        }
                        else log.warn("contact submitted without content type, rejecting contact")
                    }
                }
                if(providerRecord.altname) {
                    List<String> oldAltNames = provider.altnames.collect { AlternativeName altname -> altname.name }
                    providerRecord.altname.each { String newAltName ->
                        if(!oldAltNames.contains(newAltName)) {
                            if(!AlternativeName.construct([org: provider, name: newAltName]))
                                throw new SyncException("error on creating new alternative name for provider ${provider}")
                        }
                    }
                }
                providerRecord.platforms.each { Map platformData ->
                    Platform plat = Platform.findByGokbId(platformData.uuid)
                    if(!plat)
                        plat = createOrUpdatePlatform(platformData.uuid)
                    if(plat) {
                        plat.org = provider
                        plat.save()
                    }
                }
                if(providerRecord.identifiers) {
                    if(provider.ids) {
                        Identifier.executeUpdate('delete from Identifier i where i.org = :org',[org:provider]) //damn those wrestlers ...
                    }
                    providerRecord.identifiers.each { id ->
                        if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                            Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: provider, isUnique: false, nsType: Org.class.name])
                        }
                    }
                }
                if(source.rectype == RECTYPE_ORG) {
                    Date lastUpdatedTime = DateUtils.parseDateGeneric(providerRecord.lastUpdatedDisplay)
                    if(lastUpdatedTime.getTime() > maxTimestamp) {
                        maxTimestamp = lastUpdatedTime.getTime()
                    }
                }
                provider
            }
            else throw new SyncException(provider.errors)
        }

    }

    /**
     * Was complicatedly included in the Org domain class, has been deployed externally for better maintainability
     * Retrieves an {@link Org} instance as title publisher, if the given {@link Org} instance does not exist, it will be created.
     * The TIPP given with it will be linked with the provider data retrieved.
     *
     * @param publisherParams a {@link Map} containing the OAI PMH extract of the title publisher
     * @param tipp the title to check against
     * @throws SyncException
     */
    void lookupOrCreateTitlePublisher(Map<String,Object> publisherParams, TitleInstancePackagePlatform tipp) throws SyncException {
        if(publisherParams.gokbId && publisherParams.gokbId instanceof String) {
            Map<String, Object> publisherData = fetchRecordJSON(false, [uuid: publisherParams.gokbId])
            if(publisherData && !publisherData.error) {
                Org publisher = createOrUpdateOrg(publisherData)
                setupOrgRole([org: publisher, tipp: tipp, roleTypeCheckup: [RDStore.OR_PUBLISHER,RDStore.OR_CONTENT_PROVIDER], definiteRoleType: RDStore.OR_PUBLISHER])
            }
            else if(publisherData && publisherData.error) throw new SyncException("we:kb server is down")
            else throw new SyncException("Provider record loading failed for ${publisherParams.gokbId}")
        }
        else {
            throw new SyncException("Org submitted without UUID! No checking possible!")
        }
    }

    /**
     * Checks for a given provider uuid if there is a link with the package for the given uuid
     * @param providerUUID the provider UUID
     * @param pkg the package to check against
     */
    void createOrUpdatePackageProvider(Org provider, Package pkg) {
        setupOrgRole([org: provider, pkg: pkg, roleTypeCheckup: [RDStore.OR_PROVIDER,RDStore.OR_CONTENT_PROVIDER], definiteRoleType: RDStore.OR_PROVIDER])
    }

    /**
     * Connects an {@link Org} to a {@link TitleInstancePackagePlatform} or a {@link Package}, linking a provider to its title or package
     * @param configMap the {@link Map} specifying the connection parameters
     * @throws SyncException
     */
    void setupOrgRole(Map configMap) throws SyncException {
        OrgRole role
        def reference
        if(configMap.tipp) {
            reference = configMap.tipp
            role = OrgRole.findByTippAndRoleTypeInList(reference, configMap.roleTypeCheckup)
        }
        else if(configMap.pkg) {
            reference = configMap.pkg
            role = OrgRole.findByPkgAndRoleTypeInList(reference, configMap.roleTypeCheckup)
        }
        if(reference) {
            if(!role) {
                role = new OrgRole(roleType: configMap.definiteRoleType, isShared: false)
                role.setReference(reference)
            }
            role.org = configMap.org
            if(!role.save()) {
                throw new SyncException("Error on saving org role: ${role.getErrors().getAllErrors().toListString()}")
            }
        }
        else {
            throw new SyncException("reference missing! Config map is: ${configMap.toMapString()}")
        }
    }

    /**
     * Updates a technical or service support for a given provider {@link Org}; overrides an eventually created one and creates if it does not exist
     * @param provider the provider {@link Org} to which the given support address should be created/updated
     * @param supportProps the configuration {@link Map} containing the support address properties
     * @throws SyncException
     */
    void createOrUpdateSupport(Org provider, Map<String, String> supportProps) throws SyncException {
        Person personInstance = Person.findByTenantAndIsPublicAndLast_name(provider, true, supportProps.rdType.getI10n("value"))
        if(!personInstance) {
            personInstance = new Person(tenant: provider, isPublic: true, last_name: supportProps.rdType.getI10n("value"))
            if(!personInstance.save()) {
                throw new SyncException("Error on setting up technical support for ${provider}, concerning person instance: ${personInstance.getErrors().getAllErrors().toListString()}")
            }
        }
        PersonRole personRole = PersonRole.findByPrsAndOrgAndFunctionType(personInstance, provider, supportProps.rdType)
        if(!personRole) {
            personRole = new PersonRole(prs: personInstance, org: provider, functionType: supportProps.rdType)
            if(!personRole.save()) {
                throw new SyncException("Error on setting technical support for ${provider}, concerning person role: ${personRole.getErrors().getAllErrors().toListString()}")
            }
        }
        RefdataValue contentType = RefdataValue.getByValueAndCategory(supportProps.contentType, RDConstants.CONTACT_CONTENT_TYPE)
        Contact contact = new Contact(prs: personInstance, type: RDStore.CONTACT_TYPE_JOBRELATED)
        if(supportProps.language)
            contact.language = RefdataValue.getByValueAndCategory(supportProps.language, RDConstants.LANGUAGE_ISO) ?: null
        if(!contentType) {
            log.error("Invalid contact type submitted: ${supportProps.contentType}")
        }
        else
            contact.contentType = contentType
        contact.content = supportProps.content
        if(!contact.save()) {
            throw new SyncException("Error on setting technical support for ${provider}, concerning contact: ${contact.getErrors().getAllErrors().toListString()}")
        }
    }

    /**
     * Updates a {@link Platform} with the given parameters. If it does not exist, it will be created.
     *
     * @param platformUUID the platform UUID
     * @throws SyncException
     */
    Platform createOrUpdatePlatform(platformInput) throws SyncException {
        Map<String,Object> platformJSON = [:], platformRecord = [:]
        if(platformInput instanceof String) {
            platformJSON = fetchRecordJSON(false, [uuid: platformInput])
            platformRecord = platformJSON.records[0]
        }
        else if(platformInput instanceof Map)
            platformRecord = platformInput
        if(platformRecord) {
            //Platform.withTransaction { TransactionStatus ts ->
            Platform platform = Platform.findByGokbId(platformRecord.uuid)
            if(platform) {
                platform.name = platformRecord.name
            }
            else if(!(platformRecord.status in [PERMANENTLY_DELETED, 'Removed'])){
                platform = new Platform(name: platformRecord.name, gokbId: platformRecord.uuid)
            }
            //trigger if both records exist or if we:kb record is deleted but LAS:eR is not
            if(platform) {
                platform.status = platformRecord.status == PERMANENTLY_DELETED ? RDStore.PLATFORM_STATUS_REMOVED : RefdataValue.getByValueAndCategory(platformRecord.status, RDConstants.PLATFORM_STATUS)
                platform.normname = platformRecord.name.toLowerCase()
                if(platformRecord.primaryUrl)
                    platform.primaryUrl = new URL(platformRecord.primaryUrl)
                /*
                TEST: create linking from provider to platform, not from platform to provider! Platforms should exist also without titles!
                if(platformRecord.providerUuid) {
                    Map<String, Object> providerData = fetchRecordJSON(false,[uuid: platformRecord.providerUuid])
                    if(providerData && !providerData.error)
                        platform.org = createOrUpdateOrgJSON(providerData)
                    else if(providerData && providerData.error == 404) {
                        throw new SyncException("we:kb server is currently down")
                    }
                    else {
                        throw new SyncException("Provider loading failed for ${platformRecord.providerUuid}")
                    }
                }
                */
                if(platform.save()) {
                    //update platforms
                    Map<String, Object> packagesOfPlatform = fetchRecordJSON(false, [componentType: 'Package', platform: platformRecord.uuid])
                    if(packagesOfPlatform && packagesOfPlatform.count > 0) {
                        Package.executeUpdate('update Package pkg set pkg.nominalPlatform = :plat where pkg.gokbId in (:uuids)', [uuids: packagesOfPlatform.records.uuid, plat: platform])
                    }
                    if(source.rectype == RECTYPE_PLATFORM) {
                        Date lastUpdatedTime = DateUtils.parseDateGeneric(platformRecord.lastUpdatedDisplay)
                        if(lastUpdatedTime.getTime() > maxTimestamp) {
                            maxTimestamp = lastUpdatedTime.getTime()
                        }
                    }
                    platform
                }
                else throw new SyncException("Error on saving platform: ${platform.errors}")
                //}
            }
        }
        else if(platformJSON && platformJSON.error == 404) {
            throw new SyncException("we:kb server is down")
        }
        else {
            log.warn("Platform ${platformInput} seems to be unexistent!")
            //test if local record trace exists ...
            Platform result = Platform.findByGokbId(platformInput)
            if(result) {
                log.warn("Platform found, set cascading delete ...")
                result.status = RDStore.PLATFORM_STATUS_REMOVED
                result.save()
                result
            }
            else null
        }
    }

    /**
     * Compares two packages on domain property level against each other, retrieving the differences between both.
     * @param pkgA the old package (as {@link Package} which is already persisted)
     * @param pkgB the new package (as unprocessed {@link Map}
     * @return a {@link Set} of {@link Map}s with the differences
     */
    Set<Map<String,Object>> getPkgPropDiff(Package pkgA, Map<String,Object> pkgB) {
        log.info("processing package prop diffs; the respective GOKb UUIDs are: ${pkgA.gokbId} (LAS:eR) vs. ${pkgB.uuid} (remote)")
        Set<Map<String,Object>> result = []
        Set<String> controlledProperties = ['name','breakable','file','scope','packageStatus']

        controlledProperties.each { String prop ->
            if(pkgA[prop] != pkgB[prop]) {
                if(prop in PendingChange.REFDATA_FIELDS)
                    result.add([prop: prop, newValue: pkgB[prop]?.id, oldValue: pkgA[prop]?.id])
                else result.add([prop: prop, newValue: pkgB[prop], oldValue: pkgA[prop]])
            }
        }

        if(pkgA.nominalPlatform) {
            if (pkgA.nominalPlatform.name != pkgB.nominalPlatform.name) {
                result.add([prop: 'nominalPlatform', newValue: pkgB.nominalPlatform?.name, oldValue: pkgA.nominalPlatform.name])
            }
        }

        if(pkgA.contentProvider) {
            if (pkgA.contentProvider.name != pkgB.contentProvider.name) {
                result.add([prop: 'nominalProvider', newValue: pkgB.contentProvider?.name, oldValue: pkgA.contentProvider?.name])
            }
        }

        //the tipp diff count cannot be executed at this place because it depends on TIPP processing result

        result
    }

    /**
     * Updates the given record with the data of the updated one. That data which does not differ in
     * title and holding level (price items do count here as well!) is automatically applied; the other differences
     * are being recorded and looped separately. The diff records are being then processed to create pending changes
     * for that the issue entitlement holders may decide whether they (auto-)apply them on their holdings or not
     * @param tippA the existing title record (in the app)
     * @param tippB the updated title record (ex we:kb)
     * @return a map of structure
     * [
     *     event: {"add", "update", "delete", "remove"},
     *     target: title,
     *     diffs: result of {@link #getTippDiff(java.lang.Object, java.lang.Object)}
     * ]
     * reflecting those differences in each title record which are not applied automatically on the derived issue entitlements
     */
    void processTippDiffs(TitleInstancePackagePlatform tippA, Map tippB) {
        //ex updatedTippClosure / tippUnchangedClosure
        /*
        RefdataValue status = tippStatus.get(tippB.status)
        if(status == RDStore.TIPP_STATUS_REMOVED && tippA.status != status) {
            //the difference to event: delete is that the title is an error and should have never been appeared in LAS:eR!
            log.info("TIPP with UUID ${tippA.gokbId} has been marked as erroneous and removed")
            tippA.status = RDStore.TIPP_STATUS_REMOVED
            tippA.save()
            //[event: "remove", target: tippA]
        }
        else if ((status == RDStore.TIPP_STATUS_DELETED || tippA.pkg.packageStatus == RDStore.PACKAGE_STATUS_DELETED || tippA.platform.status == RDStore.PLATFORM_STATUS_DELETED) && tippA.status != status) {
            log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${tippA.pkg.gokbId} or package/platform itself are marked as deleted")
            RefdataValue oldStatus = tippA.status
            tippA.status = RDStore.TIPP_STATUS_DELETED
            tippA.save()
            //[event: "delete", oldValue: oldStatus, target: tippA]
        }
        */
        //tippA may be deleted; it occurred that deleted titles have been reactivated - whilst deactivation (tippB deleted/removed) needs a different handler!
        //else if(tippA.status != RDStore.TIPP_STATUS_REMOVED && !(status in [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED])) {
            //process central differences which are without effect to issue entitlements
            tippA.titleType = tippB.titleType
            tippA.name = tippB.name
            tippA.sortname = escapeService.generateSortTitle(tippB.name)
            if(tippA.altnames) {
                List<String> oldAltNames = tippA.altnames.collect { AlternativeName altname -> altname.name }
                tippB.altnames.each { String newAltName ->
                    if(!oldAltNames.contains(newAltName)) {
                        if(!AlternativeName.construct([tipp: tippA, name: newAltName]))
                            throw new SyncException("error on creating new alternative name for title ${tippA}")
                    }
                }
            }
            RefdataValue oldStatus = tippA.status, newStatus = tippStatus.get(tippB.status)
            //boolean statusChanged = oldStatus != newStatus
            tippA.status = tippStatus.get(tippB.status)
            tippA.accessType = accessType.get(tippB.accessType)
            tippA.openAccess = openAccess.get(tippB.openAccess)
            tippA.firstAuthor = tippB.firstAuthor
            tippA.firstEditor = tippB.firstEditor
            tippA.editionStatement = tippB.editionStatement
            tippA.publisherName = tippB.publisherName
            tippA.hostPlatformURL = tippB.hostPlatformURL
            tippA.dateFirstInPrint = (Date) tippB.dateFirstInPrint
            tippA.dateFirstOnline = (Date) tippB.dateFirstOnline
            tippA.seriesName = tippB.seriesName
            tippA.subjectReference = tippB.subjectReference
            //is a test. It was necessary to update ID instead for any reason whatsoever. It may be outdated by now
            tippA.medium = titleMedium.get(tippB.medium)
            tippA.accessStartDate = tippB.accessStartDate
            tippA.accessEndDate = tippB.accessEndDate
            tippA.volume = tippB.volume
            if(!tippA.save())
                throw new SyncException("Error on updating base title data: ${tippA.getErrors().getAllErrors().toListString()}")
            //these queries have to be observed very closely. They may first cause an extreme bottleneck (the underlying query may have many Sequence Scans), then they direct the issue holdings
            if(oldStatus != newStatus) {
                int updateCount = IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :newStatus where ie.tipp = :tipp and ie.status != :newStatus', [tipp: tippA, newStatus: newStatus])
                log.debug("status updated for ${tippA.gokbId}: ${oldStatus} to ${newStatus}, concerned are ${updateCount} entitlements")
                if(newStatus == RDStore.TIPP_STATUS_CURRENT) {
                    IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription s where ie.tipp = :title and ie.status in (:considered) and s.hasPerpetualAccess = true', [title: tippA, considered: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED]]).each { IssueEntitlement ie ->
                        ie.perpetualAccessBySub = ie.subscription
                        Org owner = ie.subscription.getSubscriber()
                        PermanentTitle perm = PermanentTitle.findByOwnerAndTipp(owner, ie.tipp)
                        if(!perm) {
                            perm = new PermanentTitle(owner: owner, tipp: ie.tipp, subscription: ie.subscription, issueEntitlement: ie)
                            perm.save()
                        }
                        ie.save()
                    }
                }
            }
            if(tippB.titlePublishers) {
                if(tippA.publishers) {
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.tipp = :tippA',[tippA:tippA])
                }
                tippB.titlePublishers.each { publisher ->
                    lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid], tippA)
                }
            }
            if(tippB.coverages) {
                if(tippA.coverages) {
                    TIPPCoverage.executeUpdate('delete from TIPPCoverage tc where tc.tipp = :tipp',[tipp: tippA])
                }
                tippB.coverages.each { covB ->
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
                            tipp: tippA
                    )
                    if (!covStmt.save())
                        throw new SyncException("Error on saving coverage data: ${covStmt.errors}")
                }
            }
            if(tippB.priceItems) {
                if(tippA.priceItems) {
                    PriceItem.executeUpdate('delete from PriceItem pi where pi.tipp = :tipp', [tipp: tippA])
                }
                tippB.priceItems.each { piB ->
                    PriceItem priceItem = new PriceItem(startDate: (Date) piB.startDate ?: null,
                            endDate: (Date) piB.endDate ?: null,
                            listPrice: piB.listPrice,
                            listCurrency: piB.listCurrency,
                            tipp: tippA
                    )
                    priceItem.setGlobalUID()
                    if(!priceItem.save())
                        throw new SyncException("Error on saving price data: ${priceItem.errors}")
                }
            }
            if(tippB.identifiers) {
                //I hate this solution ... wrestlers of GOKb stating that Identifiers do not need UUIDs were stronger.
                Set<String> oldIDs = []
                if(tippA.ids) {
                    oldIDs.addAll(tippA.ids.collect { Identifier id -> id.value })
                }
                if(oldIDs) {
                    Identifier.executeUpdate('delete from Identifier i where i.tipp = :tipp and i.value not in (:newValues)',[tipp:tippA, newValues: tippB.identifiers.collect { idData -> idData.value }]) //damn those wrestlers ...
                }
                tippB.identifiers.each { idData ->
                    if(!(idData.namespace.toLowerCase() in ['originediturl','uri'])) {
                        if(!(idData.value in oldIDs))
                            Identifier.construct([namespace: idData.namespace, value: idData.value, name_de: idData.namespaceName, reference: tippA, isUnique: false, nsType: TitleInstancePackagePlatform.class.name])
                    }
                }
            }
            if(tippB.ddcs) {
                if(tippA.ddcs) {
                    DeweyDecimalClassification.executeUpdate('delete from DeweyDecimalClassification ddc where ddc.tipp = :tipp',[tipp:tippA])
                }
                tippB.ddcs.each { ddcData ->
                    if(!DeweyDecimalClassification.construct(ddc: ddcData, tipp: tippA))
                        throw new SyncException("Error on saving Dewey decimal classification! See stack trace as follows:")
                }
            }
            if(tippB.languages) {
                if(tippA.languages) {
                    Language.executeUpdate('delete from Language lang where lang.tipp = :tipp',[tipp:tippA])
                }
                tippB.languages.each { langData ->
                    if(!Language.construct(language: langData, tipp: tippA))
                        throw new SyncException("Error on saving language! See stack trace as follows:")
                }
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
                        TitleHistoryEvent the = new TitleHistoryEvent(tipp:tippA,to:to.name,eventDate:historyEvent.date)
                        if(!the.save())
                            throw new SyncException("Error on saving title history data: ${the.errors}")
                    }
                }
            }
            /*
            if(statusChanged) {
                //bootleneck?
                IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :newStatus where ie.tipp = :tipp and ie.status != :removed', [newStatus: tippStatus.get(tippB.status), tipp: tippA, removed: RDStore.TIPP_STATUS_REMOVED])
                [event: 'statusChange', target: tippA, oldValue: oldStatus, newValue: newStatus]
            }
            */
            //get to diffs that need to be notified
            //println("tippA:"+tippA)
            //println("tippB:"+tippB)
            //Set<Map<String, Object>> diffs = getTippDiff(tippA, tippB)
            //includes also changes in coverage statement set
            /* as of ERMS-4585, comment from February 27th, no changes should be recorded but handed on directly to issue entitlements instead
            if (diffs) {
                //process actual diffs
                diffs.each { Map<String, Object> diff ->
                    log.info("Got tipp diff: ${diff}")
                    if (diff.prop in PendingChange.REFDATA_FIELDS) {
                        RefdataValue newProp
                        switch(diff.prop) {
                            case 'status': newProp = tippStatus.values().find { RefdataValue rdv -> rdv.id == diff.newValue }
                                break
                            case 'accessType': newProp = accessType.values().find { RefdataValue rdv -> rdv.id == diff.newValue }
                                break
                            case 'openAccess': newProp = openAccess.values().find { RefdataValue rdv -> rdv.id == diff.newValue }
                                break
                            default: newProp = null
                                break
                        }
                        if(newProp)
                            tippA[diff.prop] = newProp
                    }
                    else {
                        tippA[diff.prop] = diff.newValue
                    }
                }
                if (tippA.save())
                    [event: 'update', target: tippA, diffs: diffs]
                else throw new SyncException("Error on updating TIPP with UUID ${tippA.gokbId}: ${tippA.errors}")
            }
            else [:]
            */
        //}
        //[:]
    }

    /**
     * Replaces the onNewTipp closure.
     * Creates a new {@link TitleInstancePackagePlatform} with its respective {@link TIPPCoverage} statements
     * @param pkg the {@link Package} which contains the new title
     * @param tippData the {@link Map} which contains the we:kb record of the title
     * @param platformsInPackage the {@link Platform}s contained in the given package
     * @return the new {@link TitleInstancePackagePlatform} object
     */
    TitleInstancePackagePlatform addNewTIPP(Package pkg, Map<String,Object> tippData, Map<String,Platform> platformsInPackage) throws SyncException {
        TitleInstancePackagePlatform newTIPP = new TitleInstancePackagePlatform(
                titleType: tippData.titleType,
                name: tippData.name,
                firstAuthor: tippData.firstAuthor,
                firstEditor: tippData.firstEditor,
                editionStatement: tippData.editionStatement,
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
                accessType: accessType.get(tippData.accessType),
                openAccess: openAccess.get(tippData.openAccess),
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
            platform = createOrUpdatePlatform(tippData.platformUUID)
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
                lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid], newTIPP)
            }
            tippData.identifiers.each { idB ->
                if(idB.namespace.toLowerCase() != 'originediturl') {
                    Identifier.construct([namespace: idB.namespace, value: idB.value, name_de: idB.namespaceName, reference: newTIPP, isUnique: false, nsType: TitleInstancePackagePlatform.class.name])
                }
            }
            tippData.ddcs.each { ddcB ->
                if(!DeweyDecimalClassification.construct(ddc: ddcB, tipp: newTIPP))
                    throw new SyncException("Error on saving Dewey decimal classification! See stack trace as follows:")
            }
            tippData.languages.each { langB ->
                if(!Language.construct(language: langB, tipp: newTIPP))
                    throw new SyncException("Error on saving language! See stack trace as follows:")
            }
            tippData.altnames.each { String altName ->
                if(!AlternativeName.construct([tipp: newTIPP, name: altName]))
                    throw new SyncException("error on creating alternative name for title ${newTIPP}")
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
     * Compares two title entries against each other, retrieving the differences between both.
     * @param tippa the old TIPP (as {@link TitleInstancePackagePlatform} or {@link IssueEntitlement})
     * @param tippb the new TIPP (as {@link Map} or {@link TitleInstancePackagePlatform}
     * @return a {@link Set} of {@link Map}s with the differences
     * @deprecated as of comment for ERMS-4585, all title changes should be handed on directly to the issue entitlements, no need to record diffs
     */
    @Deprecated
    Set<Map<String,Object>> getTippDiff(tippa, tippb) {
        if(tippa instanceof TitleInstancePackagePlatform && tippb instanceof Map)
            log.info("processing diffs; the respective GOKb UUIDs are: ${tippa.gokbId} (LAS:eR) vs. ${tippb.uuid} (remote)")
        else if(tippa instanceof TitleInstancePackagePlatform && tippb instanceof TitleInstancePackagePlatform)
            log.info("processing diffs; the respective objects are: ${tippa.id} (TitleInstancePackagePlatform) pointing to ${tippb.id} (TIPP)")
        Set<Map<String, Object>> result = []

        if (tippb.containsKey("name") && tippa.name != tippb.name) {
            result.add([prop: 'name', newValue: tippb.name, oldValue: tippa.name])
        }

        if(tippa.accessType?.id != accessType.get(tippb.accessType)?.id) {
            result.add([prop: 'accessType', newValue: accessType.get(tippb.accessType).id, oldValue: tippa.accessType ? tippa.accessType.id : null])
        }

        if(tippa.openAccess?.id != openAccess.get(tippb.openAccess)?.id) {
            result.add([prop: 'openAccess', newValue: openAccess.get(tippb.openAccess).id, oldValue: tippa.openAccess ? tippa.openAccess.id : null])
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
     * @param tippA the old {@link TitleInstancePackagePlatform} object, containing the current {@link Set} of  or price items
     * @param listB the new statements (a {@link List} of remote records, kept in {@link Map}s)
     * @param instanceType the container class (may be coverage or price)
     * @return a {@link Set} of {@link Map}s reflecting the differences between the statements
     * @deprecated issue entitlements should not have sub list objects reflecting the title ones; there are very few cases in which they are needed actually. Should be deleted without replacal
     */
    @Deprecated
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
                    if(instanceType == 'coverage') {
                        if (currDiffs)
                            subDiffs << [event: 'update', target: itemA, diffs: currDiffs]
                    }
                    else if(instanceType == 'price') {
                        IssueEntitlement.findAllByTipp(tippA).each { IssueEntitlement ieA ->
                            if(ieA.priceItems) {
                                PriceItem piA = locateEquivalent(itemB,ieA.priceItems) as PriceItem
                                if(!piA) {
                                    piA = ieA.priceItems[i]
                                    piA.startDate = itemB.startDate
                                    piA.endDate = itemB.endDate
                                    piA.listCurrency = itemB.listCurrency
                                }
                                piA.listPrice = itemB.listPrice
                                piA.save()
                            }
                            else {
                                addNewPriceItem(ieA, itemB)
                            }
                        }
                    }
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
                        else if(instanceType == 'price') {
                            addNewPriceItem(tippA, itemB)
                            IssueEntitlement.findAllByTipp(tippA).each { IssueEntitlement ie ->
                                addNewPriceItem(ie, itemB)
                            }
                        }
                        if(newItem)
                            subDiffs << [event: 'add', target: newItem]
                    }
                }
                listA.each { itemA ->
                    if(!toKeep.contains(itemA)) {
                        JSON oldMap = itemA.properties as JSON
                        subDiffs << [event: 'delete', target: itemA, targetObj: oldMap.toString(), targetParent: tippA]
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
                        if(instanceType == 'coverage') {
                            newItem = addNewStatement(tippA, itemB)
                            if(newItem)
                                subDiffs << [event: 'add', target: newItem]
                        }
                        else if(instanceType == 'price') {
                            addNewPriceItem(tippA, itemB)
                            IssueEntitlement.findAllByTipp(tippA).each { IssueEntitlement ie ->
                                addNewPriceItem(ie, itemB)
                            }
                        }
                    }
                }
            }
        }

        subDiffs
    }

    /**
     * Records the differences between coverage or price list elements
     * @param itemA the existing list (of {@link TIPPCoverage}s or {@link PriceItem}s)
     * @param itemB the new list (of {@link IssueEntitlementCoverage}s or {@link PriceItem}s)
     * @return the {@link Set} of {@link Map}s reflecting the differences
     */
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
                        if(itemA instanceof AbstractCoverage)
                            diffs << [prop: cp, oldValue: itemA[cp], newValue: itemB[cp]]
                        else if(itemA instanceof PriceItem) {
                            itemA[cp] = itemB[cp]
                            itemA.save()
                        }
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
                        if(itemA instanceof AbstractCoverage)
                            diffs << [prop:cp, oldValue:itemA[cp],newValue:null]
                        else if(itemA instanceof PriceItem) {
                            itemA[cp] = null
                            itemA.save()
                        }
                    }
                    else if(itemA[cp] == null && itemB[cp] != null) {
                        calB.setTime((Date) itemB[cp])
                        if(itemA instanceof AbstractCoverage)
                            diffs << [prop:cp, oldValue:null, newValue: itemB[cp]]
                        else if(itemA instanceof PriceItem) {
                            itemA[cp] = itemB[cp]
                            itemA.save()
                        }
                    }
                }
            }
            else {
                if(itemA[cp] != itemB[cp] && !((itemA[cp] == '' && itemB[cp] == null) || (itemA[cp] == null && itemB[cp] == ''))) {
                    if(itemA instanceof AbstractCoverage)
                        diffs << [prop:cp, oldValue: itemA[cp], newValue: itemB[cp]]
                    else if(itemA instanceof PriceItem) {
                        itemA[cp] = itemB[cp]
                        itemA.save()
                    }
                }
            }
        }
        diffs
    }

    /**
     * Contrary to {@link AbstractCoverage#findEquivalent(Collection)} resp. {@link PriceItem#findEquivalent(Collection)}, this method locates a non-persisted coverage statement an equivalent from the given {@link Collection}
     * @param itemB a {@link Map}, reflecting the non-persisited item
     * @param listA a {@link Collection} on {@link TIPPCoverage} or {@link PriceItem} statements, the list to be updated
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
                Calendar calA = GregorianCalendar.getInstance(), calB = GregorianCalendar.getInstance()
                listA.each { itemA ->
                    if(itemA[k] != null && itemB[k] != null) {
                        calA.setTime(itemA[k])
                        calB.setTime(itemB[k])
                        if (calA == calB)
                            equivalent = itemA
                    }
                    else if(itemA[k] == null && itemB[k] == null)
                        equivalent = itemA
                }
            }
            else
                equivalent = listA.find { it[k] == itemB[k] && it[k] != null && itemB[k] != null }
            if (equivalent != null) {
                log.debug "Statement ${equivalent.id} located as equivalent to ${itemB} by ${k}: ${itemB[k]}"
                break
            }
        }
        equivalent
    }

    /**
     * Adds a new coverage statement to the given title
     * @param tippA the {@link TitleInstancePackagePlatform} or {@link IssueEntitlement} to add the coverage to
     * @param covB the coverage statement {@link Map}, containing the we:kb data
     * @return the new {@link AbstractCoverage}
     */
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

    /**
     * Adds a new price item to the given title
     * @param entitlementA the {@link TitleInstancePackagePlatform} or {@link IssueEntitlement} to add the price item to
     * @param piB the price item {@link Map}, containing the we:kb data
     * @return the new {@link PriceItem}
     */
    PriceItem addNewPriceItem(entitlementA, piB) {
        Map<String,Object> params = [startDate: (Date) piB.startDate,
                                     endDate: (Date) piB.endDate,
                                     listPrice: piB.listPrice,
                                     listCurrency: piB.listCurrency]
        if(entitlementA instanceof TitleInstancePackagePlatform)
            params.tipp = entitlementA
        else if(entitlementA instanceof IssueEntitlement)
            params.issueEntitlement = entitlementA
        PriceItem pi = new PriceItem(params)
        pi.setGlobalUID()
        if(pi.save())
            pi
        else null
    }

    /**
     * Fetches a JSON record from the we:kb API endpoint which has been defined in the {@link GlobalRecordSource} being used in the synchronisation process
     * @param useScroll use the scroll endpoint for huge data loads?
     * @param queryParams the parameter {@link Map} to be used for the query
     * @return a JSON {@link Map} containing the query result
     * @throws SyncException
     */
    Map<String,Object> fetchRecordJSON(boolean useScroll, Map<String,Object> queryParams) throws SyncException {
        BasicHttpClient http
        String uri = source.uri.endsWith('/') ? source.uri : source.uri+'/'
        HttpClientConfiguration config = new DefaultHttpClientConfiguration()
        config.readTimeout = Duration.ofMinutes(1)
        config.maxContentLength = MAX_CONTENT_LENGTH
        queryParams.username = ConfigMapper.getWekbApiUsername()
        queryParams.password = ConfigMapper.getWekbApiPassword()
        if(useScroll) {
            http = new BasicHttpClient(uri + 'scroll', config)
            String debugString = uri+'scroll?'
            queryParams.each { String k, v ->
                debugString += '&' + k + '=' + v
            }
            log.debug(debugString)
        }
        else http = new BasicHttpClient(uri+'searchApi', config)
        Map<String,Object> result = [:]
        //setting default status
        /*
        if(!queryParams.status) {
            queryParams.status = ["Current","Expected","Retired","Deleted",PERMANENTLY_DELETED,"Removed"]
            //queryParams.status = ["Removed"] //debug only
        }
        */
        //log.debug(queryParams.toMapString())
        Closure success = { HttpResponse resp, json ->
            result.count = json.result_count_total
            result.records = json.result
            result.currentPage = json.page_current
            result.lastPage = json.page_total
            //result.scrollId = json.scrollId
            //result.hasMoreRecords = Boolean.valueOf(json.hasMoreRecords)
        }
        Closure failure = { HttpResponse resp, reader ->
            if(resp?.code() == 404) {
                result.error = resp.code()
            }
            else
                throw new SyncException("error on request: ${resp?.status()} : ${reader}")
        }
        http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, success, failure)
        http.close()

        result
    }

    Set<String> getPermanentlyDeletedTitles(String changedFrom = null) {
        Set<String> result = []
        Map<String, Object> queryParams = [componentType: 'deletedkbcomponent', status: PERMANENTLY_DELETED, max: MAX_TIPP_COUNT_PER_PAGE]
        if(changedFrom)
            queryParams.changedSince = changedFrom
        Map<String, Object> recordBatch = fetchRecordJSON(false, queryParams)
        boolean more = true
        int offset = 0
        while(more) {
            result.addAll(recordBatch.records.findAll { record -> record.componentType == 'TitleInstancePackagePlatform' }.collect { record -> record.uuid })
            more = recordBatch.currentPage < recordBatch.lastPage
            if(more) {
                offset += MAX_TIPP_COUNT_PER_PAGE
                recordBatch = fetchRecordJSON(false, queryParams + [offset: offset])
            }
        }
        result
    }

    /**
     * In order to save performance, reference data is being loaded prior to the synchronisation
     */
    void defineMapFields() {
        //define map fields
        tippStatus.put(RDStore.TIPP_STATUS_CURRENT.value,RDStore.TIPP_STATUS_CURRENT)
        tippStatus.put(RDStore.TIPP_STATUS_DELETED.value,RDStore.TIPP_STATUS_DELETED)
        tippStatus.put(PERMANENTLY_DELETED,RDStore.TIPP_STATUS_REMOVED)
        tippStatus.put(RDStore.TIPP_STATUS_RETIRED.value,RDStore.TIPP_STATUS_RETIRED)
        tippStatus.put(RDStore.TIPP_STATUS_EXPECTED.value,RDStore.TIPP_STATUS_EXPECTED)
        tippStatus.put(RDStore.TIPP_STATUS_REMOVED.value,RDStore.TIPP_STATUS_REMOVED)
        contactTypes.put(RDStore.PRS_FUNC_TECHNICAL_SUPPORT.value,RDStore.PRS_FUNC_TECHNICAL_SUPPORT)
        contactTypes.put(RDStore.PRS_FUNC_SERVICE_SUPPORT.value,RDStore.PRS_FUNC_SERVICE_SUPPORT)
        contactTypes.put(RDStore.PRS_FUNC_METADATA.value,RDStore.PRS_FUNC_METADATA)
        //this complicated way is necessary because of static in order to avoid a NonUniqueObjectException
        titleMedium.put(RDStore.TITLE_TYPE_DATABASE.value, RDStore.TITLE_TYPE_DATABASE)
        titleMedium.put(RDStore.TITLE_TYPE_EBOOK.value, RDStore.TITLE_TYPE_EBOOK)
        titleMedium.put(RDStore.TITLE_TYPE_JOURNAL.value, RDStore.TITLE_TYPE_JOURNAL)
        RefdataValue.findAllByIdNotInListAndOwner(titleMedium.values().collect { RefdataValue rdv -> rdv.id }, RefdataCategory.findByDesc(RDConstants.TITLE_MEDIUM)).each { RefdataValue rdv ->
            titleMedium.put(rdv.value, rdv)
        }
        packageStatus.put(RDStore.PACKAGE_STATUS_DELETED.value, RDStore.PACKAGE_STATUS_DELETED)
        RefdataValue.findAllByIdNotInListAndOwner(packageStatus.values().collect { RefdataValue rdv -> rdv.id }, RefdataCategory.findByDesc(RDConstants.PACKAGE_STATUS)).each { RefdataValue rdv ->
            packageStatus.put(rdv.value, rdv)
        }
        packageStatus.put(PERMANENTLY_DELETED, RDStore.PACKAGE_STATUS_REMOVED)
        orgStatus.put(RDStore.ORG_STATUS_CURRENT.value,RDStore.ORG_STATUS_CURRENT)
        orgStatus.put(RDStore.ORG_STATUS_DELETED.value,RDStore.ORG_STATUS_DELETED)
        orgStatus.put(PERMANENTLY_DELETED,RDStore.ORG_STATUS_REMOVED)
        orgStatus.put(RDStore.ORG_STATUS_REMOVED.value,RDStore.ORG_STATUS_REMOVED)
        orgStatus.put(RDStore.ORG_STATUS_RETIRED.value,RDStore.ORG_STATUS_RETIRED)
        RefdataCategory.getAllRefdataValues(RDConstants.CURRENCY).each { RefdataValue rdv ->
            currency.put(rdv.value, rdv)
        }
        List<RefdataValue> staticOrgTypes = [RDStore.OT_AGENCY, RDStore.OT_CONSORTIUM, RDStore.OT_INSTITUTION, RDStore.OT_LICENSOR, RDStore.OT_PROVIDER, RDStore.OT_PUBLISHER]
        RefdataValue.findAllByIdNotInListAndOwner(staticOrgTypes.collect { RefdataValue rdv -> rdv.id }, RDStore.OT_AGENCY.owner).each { RefdataValue rdv ->
            orgTypes.put(rdv.value, rdv)
        }
        staticOrgTypes.each { RefdataValue rdv ->
            orgTypes.put(rdv.value, rdv)
        }
        RefdataCategory.getAllRefdataValues(RDConstants.DDC).each { RefdataValue rdv ->
            ddc.put(rdv.value, rdv)
        }
        RefdataCategory.getAllRefdataValues(RDConstants.TIPP_ACCESS_TYPE).each { RefdataValue rdv ->
            accessType.put(rdv.value, rdv)
        }
        buildWekbLaserRefdataMap(RDConstants.LICENSE_OA_TYPE)
        //packagesToNotify = [:]
        packagesAlreadyUpdated = []
        platformsAlreadyUpdated = []
    }

    /**
     * Builds an equivalency map between the controlled list of the app and that of the connected we:kb instance
     * @param rdCat the reference data category constant to build
     * @see RDConstants
     */
    void buildWekbLaserRefdataMap(String rdCat) {
        switch(rdCat) {
            case RDConstants.LICENSE_OA_TYPE:
                openAccess.put('Blue OA', RefdataValue.getByValueAndCategory('Blue Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Empty', RDStore.LICENSE_OA_TYPE_EMPTY)
                openAccess.put('Gold OA', RefdataValue.getByValueAndCategory('Gold Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Green OA', RefdataValue.getByValueAndCategory('Green Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Hybrid', RefdataValue.getByValueAndCategory('Hybrid', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('White OA', RefdataValue.getByValueAndCategory('White Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Yellow OA', RefdataValue.getByValueAndCategory('Yellow Open Access', RDConstants.LICENSE_OA_TYPE))
                break
        }
    }

}
