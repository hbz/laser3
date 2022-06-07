package de.laser

import de.laser.finance.PriceItem
import de.laser.system.SystemEvent
import de.laser.base.AbstractCoverage
import de.laser.exceptions.SyncException
import de.laser.helper.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.base.AbstractLockableService
import de.laser.titles.TitleHistoryEvent
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.hibernate.Session

import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

/**
 * Implements the synchronisation workflow with the we:kb. It is currently used for title data and provider (organisation) data
 * and triggers the subscription holding notifications
 */
@Transactional
class GlobalSourceSyncService extends AbstractLockableService {

    EscapeService escapeService
    ExecutorService executorService
    GenericOIDService genericOIDService
    PendingChangeService pendingChangeService
    PackageService packageService
    ApiSource apiSource
    GlobalRecordSource source

    final static long RECTYPE_PACKAGE = 0
    final static long RECTYPE_TITLE = 1
    final static long RECTYPE_ORG = 2
    final static long RECTYPE_TIPP = 3
    final static String PERMANENTLY_DELETED = "Permanently Deleted"

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
    Map<String,Integer> initialPackagesCounter
    Map<String,Set<Map<String,Object>>> pkgPropDiffsContainer
    Map<String,Set<Map<String,Object>>> packagesToNotify
    Set<PendingChange> titlesToRemove

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
                    case RECTYPE_ORG: componentType = 'Org'
                        break
                    case RECTYPE_TIPP: componentType = 'TitleInstancePackagePlatform'
                        break
                }
                //do prequest: are we needing the scroll api?
                //5000 records because of local testing ability
                Map<String,Object> result = fetchRecordJSON(false,[componentType:componentType,changedSince:sdf.format(oldDate),max:5000])
                if(result.error == 404) {
                    log.error("we:kb server is down")
                    SystemEvent.createEvent('GSSS_JSON_ERROR',['jobId':source.id])
                }
                else {
                    if(result) {
                        processScrollPage(result, componentType, sdf.format(oldDate))
                    }
                    else {
                        log.info("no records updated - leaving everything as is ...")
                    }
                    if(source.rectype == RECTYPE_TIPP) {
                        if(packagesToNotify.keySet().size() > 0) {
                            log.info("notifying subscriptions ...")
                            Map<String, Set<PendingChange>> packagePendingChanges = trackPackageHistory()
                            //get subscription packages and their respective holders, parent level only!
                            packagePendingChanges.each { String packageKey, Set<PendingChange> packageChanges ->
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
                                    autoAcceptPendingChanges(org, sp, packageChanges)
                                    //nonAutoAcceptPendingChanges(org, sp)
                                }
                            }
                            log.info("end notifying subscriptions")
                            log.info("clearing removed titles")
                            packageService.clearRemovedTitles()
                            log.info("end clearing titles")
                        }
                        else {
                            log.info("no diffs recorded ...")
                        }
                    }
                    if(maxTimestamp+1000 > source.haveUpTo.getTime()) {
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
     * Reloads all data of the given component type from the connected we:kb instance connected by {@link GlobalRecordSource}
     * @param componentType the component type (one of Org, TitleInstancePackagePlatform) to update
     */
    void reloadData(String componentType) {
        running = true
        defineMapFields()
        executorService.execute({
            Thread.currentThread().setName("GlobalDataUpdate_${componentType}")
            this.source = GlobalRecordSource.findByActiveAndRectype(true,RECTYPE_TIPP)
            this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
            log.info("getting all records from job #${source.id} with uri ${source.uri}")
            try {
                //5000 records because of local testing ability
                Map<String,Object> result = fetchRecordJSON(false,[componentType: componentType, max: 5000])
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
        })
    }

    /**
     * Reloads a concrete property from the we:kb instance. Depending on the property to load, the domain objects having this property both in ElasticSearch index and in LAS:eR are being updated
     * @param dataToLoad the property to update for every object (one of identifier, ddc, language or editionStatement)
     */
    void updateData(String dataToLoad) {
        running = true
        executorService.execute({
            Thread.currentThread().setName("UpdateData")
            this.source = GlobalRecordSource.findByActiveAndRectype(true,RECTYPE_TIPP)
            this.apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
            List<String> triggeredTypes
            int max
            switch(dataToLoad) {
                case "identifier": triggeredTypes = ['Package','Org','TitleInstancePackagePlatform']
                    max = 100
                    break
                case "ddc": triggeredTypes = ['TitleInstancePackagePlatform']
                    RefdataCategory.getAllRefdataValues(RDConstants.DDC).each { RefdataValue rdv ->
                        ddc.put(rdv.value, rdv)
                    }
                    max = 5000
                    break
                case "accessType": triggeredTypes = ['TitleInstancePackagePlatform']
                    RefdataCategory.getAllRefdataValues(RDConstants.TIPP_ACCESS_TYPE).each { RefdataValue rdv ->
                        accessType.put(rdv.value, rdv)
                    }
                    max = 5000
                    break
                case "openAccess": triggeredTypes = ['TitleInstancePackagePlatform']
                    buildWekbLaserRefdataMap(RDConstants.LICENSE_OA_TYPE)
                    max = 5000
                    break
                case [ "language", "editionStatement" ]:
                    triggeredTypes = ['TitleInstancePackagePlatform']
                    max = 5000
                    break
                case "titleNamespace": triggeredTypes = ['Platform']
                    max = 5000
                    break
                default: triggeredTypes = []
                    break
            }
            try {
                triggeredTypes.each { String componentType ->
                    GlobalRecordSource.withNewSession { Session sess ->
                        int offset = 0
                        Map<String, Object> queryParams = [component_type: componentType, max: max]
                        Map<String,Object> result = fetchRecordJSON(true,queryParams)
                        if(result) {
                            if(result.error == 404) {
                                log.error("we:kb server currently down")
                            }
                            else {
                                boolean more = true
                                while(more) {
                                    log.debug("processing entries ${offset}-${offset+max} for ${componentType} ...")
                                    Set<String> uuids = result.records.collect { Map entry -> entry.uuid } as Set<String>
                                    switch(componentType) {
                                        case 'Package': List<Package> packages = Package.findAllByGokbIdInList(uuids.toList())
                                            log.debug("from current page, ${packages.size()} packages exist in LAS:eR")
                                            packages.eachWithIndex { Package pkg, int idx ->
                                                log.debug("now processing package ${idx} with uuid ${pkg.gokbId}, total entry: ${offset+idx}")
                                                List identifiers = result.records.find { record -> record.uuid == pkg.gokbId }.identifiers
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
                                            break
                                        case 'Platform': List<Platform> platforms = Platform.findAllByGokbIdInList(uuids.toList())
                                            log.debug("from current page, ${platforms.size()} packages exist in LAS:eR")
                                            platforms.eachWithIndex { Platform platform, int idx ->
                                                log.debug("now processing platform ${idx} with uuid ${platform.gokbId}, total entry: ${offset+idx}")
                                                platform.titleNamespace = result.records.find { record -> record.uuid == platform.gokbId }.titleNamespace
                                                platform.save()
                                            }
                                            break
                                        case 'Org': List<Org> providers = Org.findAllByGokbIdInList(uuids.toList())
                                            log.debug("from current page, ${providers.size()} providers exist in LAS:eR")
                                            providers.eachWithIndex { Org provider, int idx ->
                                                log.debug("now processing org ${idx} with uuid ${provider.gokbId}, total entry: ${offset+idx}")
                                                List identifiers = result.records.find { record -> record.uuid == provider.gokbId }.identifiers
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
                                            }
                                            break
                                        case 'TitleInstancePackagePlatform': List<TitleInstancePackagePlatform> tipps = TitleInstancePackagePlatform.findAllByGokbIdInList(uuids.toList())
                                            log.debug("from this page, ${tipps.size()} TIPPs do exist in LAS:eR")
                                            tipps.eachWithIndex { TitleInstancePackagePlatform tipp, int idx ->
                                                log.debug("now processing tipp ${idx} with uuid ${tipp.gokbId}, total entry: ${offset+idx}")
                                                switch(dataToLoad) {
                                                    case "identifier":
                                                        List identifiers = result.records.find { record -> record.uuid == tipp.gokbId }.identifiers
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
                                                        List ddcs = result.records.find { record -> record.uuid == tipp.gokbId }.ddcs
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
                                                        List languages = result.records.find { record -> record.uuid == tipp.gokbId }.languages
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
                                                        tipp.editionStatement = result.records.find { record -> record.uuid == tipp.gokbId }.editionStatement
                                                        tipp.save()
                                                        break
                                                    case "accessType":
                                                        String newAccessType = result.records.find { record -> record.uuid == tipp.gokbId }.accessType
                                                        tipp.accessType = accessType.get(newAccessType)
                                                        tipp.save()
                                                        break
                                                    case "openAccess":
                                                        String newOpenAccess = result.records.find { record -> record.uuid == tipp.gokbId }.openAccess
                                                        tipp.openAccess = openAccess.get(newOpenAccess)
                                                        tipp.save()
                                                        break
                                                }
                                            }
                                            log.debug("interim flush at end of load: ${offset}")
                                            sess.flush()
                                            break
                                    }
                                    if(result.hasMoreRecords) {
                                        String scrollId = result.scrollId
                                        offset += max
                                        log.debug("using scrollId ${scrollId}")
                                        result = fetchRecordJSON(true, queryParams+[scrollId: scrollId])
                                    }
                                    else {
                                        more = false
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
        })
    }

    /**
     * Takes the prequest result and enters the data load loop until all scroll pages are being processed
     * @param result the prequest result, containing the set of records and/or the information whether there are further records to process
     * @param componentType the object type (TitleInstancePackagePlatform or Org) to update
     * @param changedSince the timestamp from which new records should be loaded
     * @param pkgFilter an optional package filter to restrict the data to be loaded
     * @throws SyncException if an error occurs during the update process
     */
    void processScrollPage(Map<String, Object> result, String componentType, String changedSince, String pkgFilter = null) throws SyncException {
        if(result.count >= 5000) {
            int offset = 0, max = 5000
            Map<String, Object> queryParams = [component_type: componentType, max: max]
            if(changedSince)
                queryParams.changedSince = changedSince
            if(pkgFilter)
                queryParams.pkg = pkgFilter
            String scrollId
            boolean more = true
            while(more) {
                //actually, scrollId alone should do the trick but tests revealed that other parameters are necessary, too, because of current workaround solution
                log.debug("using scrollId ${scrollId}")
                if(scrollId) {
                    result = fetchRecordJSON(true, queryParams+[scrollId: scrollId])
                }
                else {
                    result = fetchRecordJSON(true, queryParams)
                }
                if(result.error && result.error == 404) {
                    more = false
                }
                else if(result.count > 0) {
                    switch (source.rectype) {
                        case RECTYPE_ORG:
                            result.records.each { record ->
                                record.platforms.each { Map platformData ->
                                    try {
                                        createOrUpdatePlatformJSON(platformData.uuid)
                                    }
                                    catch (SyncException e) {
                                        log.error("Error on updating platform ${platformData.uuid}: ",e)
                                        SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                                    }
                                }
                                createOrUpdateOrgJSON(record)
                            }
                            break
                        case RECTYPE_TIPP: updateRecords(result.records, offset)
                            break
                    }
                    if(result.hasMoreRecords) {
                        scrollId = result.scrollId
                        offset += max
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
        else if(result.count > 0 && result.count < 5000) {
            switch (source.rectype) {
                case RECTYPE_ORG:
                    result.records.each { record ->
                        record.platforms.each { Map platformData ->
                            try {
                                createOrUpdatePlatformJSON(platformData.uuid)
                            }
                            catch (SyncException e) {
                                log.error("Error on updating platform ${platformData.uuid}: ",e)
                                SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                            }
                        }
                        createOrUpdateOrgJSON(record)
                    }
                    break
                case RECTYPE_TIPP: updateRecords(result.records, 0)
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
    void updateRecords(List<Map> rawRecords, int offset) {
        //necessary filter for DEV database
        List<Map> records = rawRecords.findAll { Map tipp -> tipp.containsKey("hostPlatformUuid") && tipp.containsKey("tippPackageUuid") }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        Set<String> platformUUIDs = records.collect { Map tipp -> tipp.hostPlatformUuid } as Set<String>
        log.debug("found platform UUIDs: ${platformUUIDs.toListString()}")
        Set<String> packageUUIDs = records.collect { Map tipp -> tipp.tippPackageUuid } as Set<String>
        log.debug("found package UUIDs: ${packageUUIDs.toListString()}")
        Set<String> tippUUIDs = records.collect { Map tipp -> tipp.uuid } as Set<String>
        Map<String,Package> packagesOnPage = [:]
        Map<String,Platform> platformsOnPage = [:]

        //packageUUIDs is null if package has no tipps
        Set<String> existingPackageUUIDs = packageUUIDs ? Platform.executeQuery('select pkg.gokbId from Package pkg where pkg.gokbId in (:pkgUUIDs)',[pkgUUIDs:packageUUIDs]) : []
        Map<String,TitleInstancePackagePlatform> tippsInLaser = [:]
        //collect existing TIPPs
        if(tippUUIDs) {
            TitleInstancePackagePlatform.findAllByGokbIdInList(tippUUIDs.toList()).each { TitleInstancePackagePlatform tipp ->
                tippsInLaser.put(tipp.gokbId, tipp)
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
        //create or update packages
        packageUUIDs.each { String packageUUID ->
            try {
                Package pkg = createOrUpdatePackage(packageUUID)
                if(pkg)
                    packagesOnPage.put(packageUUID,pkg)
            }
            catch (SyncException e) {
                log.error("Error on updating package ${packageUUID}: ",e)
                SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:packageUUID])
            }
        }

        records.eachWithIndex { Map tipp, int idx ->
            log.debug("now processing entry #${idx}, total entry #${offset+idx} with uuid ${tipp.uuid}")
            try {
                Map<String,Object> updatedTIPP = [
                    titleType: tipp.titleType,
                    name: tipp.name,
                    altnames: [],
                    packageUUID: tipp.tippPackageUuid ?: null,
                    platformUUID: tipp.hostPlatformUuid ?: null,
                    titlePublishers: [],
                    publisherName: tipp.publisherName,
                    firstAuthor: tipp.firstAuthor ?: null,
                    firstEditor: tipp.firstEditor ?: null,
                    editionStatement: tipp.editionStatement ?: null,
                    dateFirstInPrint: tipp.dateFirstInPrint ? DateUtils.parseDateGeneric(tipp.dateFirstInPrint) : null,
                    dateFirstOnline: tipp.dateFirstOnline ? DateUtils.parseDateGeneric(tipp.dateFirstOnline) : null,
                    imprint: tipp.titleImprint ?: null,
                    status: tipp.status,
                    seriesName: tipp.series ?: null,
                    subjectReference: tipp.subjectArea ?: null,
                    volume: tipp.volumeNumber ?: null,
                    coverages: [],
                    priceItems: [],
                    hostPlatformURL: tipp.url ?: null,
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
                else if(!(updatedTIPP.status in [RDStore.TIPP_STATUS_DELETED.value, PERMANENTLY_DELETED])) {
                    Package pkg = packagesOnPage.get(updatedTIPP.packageUUID)
                    if(pkg)
                        addNewTIPP(pkg, updatedTIPP, platformsOnPage)
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
     * This records the package changes so that subscription holders may decide whether they apply them or not except price changes which are auto-applied
     * @param packagesToTrack the packages to be tracked
     */
    Map<String, Set<PendingChange>> trackPackageHistory() {
        Map<String, Set<PendingChange>> result = [:]
        //Package.withSession { Session sess ->
            //loop through all packages
            packagesToNotify.each { String packageUUID, Set<Map<String,Object>> diffsOfPackage ->
                Set<PendingChange> packagePendingChanges = []
                //println("diffsOfPackage:"+diffsOfPackage)
                diffsOfPackage.each { Map<String,Object> diff ->
                    log.debug(diff.toMapString())
                    //[event:update, target:de.laser.TitleInstancePackagePlatform : 196477, diffs:[[prop:price, priceDiffs:[[event:add, target:de.laser.finance.PriceItem : 10791]]]]]
                    switch(diff.event) {
                        case 'add': packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_TITLE,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY])
                            break
                        case 'update':
                            diff.diffs.each { tippDiff ->
                                switch(tippDiff.prop) {
                                    case 'coverage': tippDiff.covDiffs.each { covEntry ->
                                        switch(covEntry.event) {
                                            case 'add': packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_COVERAGE,target:covEntry.target,status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                            case 'update': covEntry.diffs.each { covDiff ->
                                                    packagePendingChanges << PendingChange.construct([msgToken: PendingChangeConfiguration.COVERAGE_UPDATED, target: covEntry.target, status: RDStore.PENDING_CHANGE_HISTORY, prop: covDiff.prop, newValue: covDiff.newValue, oldValue: covDiff.oldValue])
                                                    //log.debug("tippDiff.covDiffs.covDiff: " + covDiff)
                                                }
                                                break
                                            case 'delete': JSON oldMap = covEntry.target.properties as JSON
                                                packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.COVERAGE_DELETED, target:covEntry.targetParent, oldValue: oldMap.toString() , status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                        }
                                    }
                                        break
                                    /*
                                    case 'price': tippDiff.priceDiffs.each { priceEntry ->
                                        switch(priceEntry.event) {
                                            case 'add': packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.NEW_PRICE,target:priceEntry.target,status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                            case 'update':
                                                priceEntry.diffs.each { priceDiff ->
                                                    //packagePendingChanges << PendingChange.construct([msgToken: PendingChangeConfiguration.PRICE_UPDATED, target: priceEntry.target, status: RDStore.PENDING_CHANGE_HISTORY, prop: priceDiff.prop, newValue: priceDiff.newValue, oldValue: priceDiff.oldValue])
                                                }
                                                //log.debug("tippDiff.priceDiffs: "+ priceEntry)
                                                break
                                            case 'delete': //JSON oldMap = priceEntry.target.properties as JSON
                                                //packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.PRICE_DELETED,target:priceEntry.targetParent,oldValue:oldMap.toString(),status:RDStore.PENDING_CHANGE_HISTORY])
                                                break
                                        }
                                    }
                                        break
                                     */
                                    default:
                                        packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_UPDATED,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY,prop:tippDiff.prop,newValue:tippDiff.newValue,oldValue:tippDiff.oldValue])
                                        break
                                }
                            }
                            break
                        case 'delete': packagePendingChanges << PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_DELETED,target:diff.target,oldValue:diff.oldValue,status:RDStore.PENDING_CHANGE_HISTORY])
                            break
                        case 'remove': titlesToRemove << PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_REMOVED,target:diff.target,status:RDStore.PENDING_CHANGE_HISTORY]) //dealt elsewhere!
                            break
                        case 'pkgPropDiffs':
                            diff.diffs.each { pkgPropDiff ->
                                packagePendingChanges << PendingChange.construct([mskTogen: PendingChangeConfiguration.PACKAGE_PROP, target: diff.target, prop: pkgPropDiff.prop, newValue: pkgPropDiff.newValue, oldValue: pkgPropDiff.oldValue, status: RDStore.PENDING_CHANGE_HISTORY])
                            }

                            break
                    }
                    //PendingChange.construct([msgToken,target,status,prop,newValue,oldValue])
                }
                //sess.flush()
                result.put(packageUUID, packagePendingChanges)
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
     * @see PendingChange
     */
    void autoAcceptPendingChanges(Org contextOrg, SubscriptionPackage subPkg, Set<PendingChange> packageChanges) {
        //get for each subscription package the tokens which should be accepted
        String query = 'select pcc.settingKey from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where pcc.settingValue = :accept and sp = :sp and pcc.settingKey not in (:excludes)'
        List<String> pendingChangeConfigurations = PendingChangeConfiguration.executeQuery(query,[accept:RDStore.PENDING_CHANGE_CONFIG_ACCEPT,sp:subPkg,excludes:[PendingChangeConfiguration.NEW_PRICE,PendingChangeConfiguration.PRICE_DELETED,PendingChangeConfiguration.PRICE_UPDATED]])
        if(pendingChangeConfigurations) {
            Map<String,Object> changeParams = [pkg:subPkg.pkg,history:RDStore.PENDING_CHANGE_HISTORY,subscriptionJoin:subPkg.dateCreated,msgTokens:pendingChangeConfigurations,oid:genericOIDService.getOID(subPkg.subscription),accepted:RDStore.PENDING_CHANGE_ACCEPTED]
            Set<PendingChange> acceptedChanges = PendingChange.findAllByOidAndStatusAndMsgTokenIsNotNull(genericOIDService.getOID(subPkg.subscription),RDStore.PENDING_CHANGE_ACCEPTED)
            /*newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = pc.targetProperty and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and pc.targetProperty = null and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = null and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = pc.targetProperty and pca.status = :accepted)',changeParams))
            newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.tippCoverage tc join tc.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens) and pc.targetProperty = null and not exists (select pca.id from PendingChange pca where pca.tipp = pc.tipp and pca.oid = :oid and pca.targetProperty = null and pca.status = :accepted)',changeParams))*/
            //newChanges.addAll(PendingChange.executeQuery('select pc from PendingChange pc join pc.priceItem pi join pi.tipp tipp join tipp.pkg pkg where pkg = :pkg and pc.status = :history and pc.ts > :subscriptionJoin and pc.msgToken in (:msgTokens)',changeParams))
            packageChanges.each { PendingChange newChange ->
                if(newChange.msgToken in pendingChangeConfigurations) {
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
    }

    /**
     * Updates a given {@link TitleInstancePackagePlatform} record (record A) to the state of record B, if A does not exist, it will be created if the package exists
     * @param tippA the record existing in LAS:eR (a {@link TitleInstancePackagePlatform} object)
     * @param tippB the record coming from we:kb (a {@link Map} reflecting the objectdata)
     * @param newPackages the {@link Package}s of the scroll page
     * @param newPlatforms the {@link Platform}s of the scroll page
     * @return a {@link Map} containing the recorded differences which are recorded as {@link PendingChange}s
     */
    Map<String,Object> createOrUpdateTIPP(TitleInstancePackagePlatform tippA,Map tippB, Map<String,Package> newPackages,Map<String,Platform> newPlatforms) {
        Map<String,Object> result = [:]
        //TitleInstancePackagePlatform.withSession { Session sess ->
            if(tippA) {
                //update or delete TIPP
                result.putAll(processTippDiffs(tippA,tippB))
            }
            else {
                Package pkg = newPackages.get(tippB.packageUUID)
                //Unbelievable! But package may miss at this point!
                if(pkg && pkg?.packageStatus != packageStatus.get("Deleted") && !(tippB.status in [PERMANENTLY_DELETED, RDStore.TIPP_STATUS_DELETED.value, RDStore.TIPP_STATUS_REMOVED.value])) {
                    //new TIPP
                    TitleInstancePackagePlatform target = addNewTIPP(pkg, tippB, newPlatforms)
                    result.event = 'add'
                    result.target = target
                }
            }
        //}
        result
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
    Package createOrUpdatePackage(String packageUUID) throws SyncException {
        Map<String,Object> packageJSON = fetchRecordJSON(false,[uuid: packageUUID])
        if(packageJSON.records) {
            Map packageRecord = (Map) packageJSON.records[0]
            Package result = Package.findByGokbId(packageUUID)
            Date lastUpdatedDisplay = DateUtils.parseDateGeneric(packageRecord.lastUpdatedDisplay)
            if(!result || result?.lastUpdated < lastUpdatedDisplay) {
                log.info("package record loaded, reconciling package record for UUID ${packageUUID}")
                RefdataValue packageStatus = packageRecord.status ? packageStatus.get(packageRecord.status) : null
                RefdataValue contentType = packageRecord.contentType ? RefdataValue.getByValueAndCategory(packageRecord.contentType,RDConstants.PACKAGE_CONTENT_TYPE) : null
                RefdataValue file = packageRecord.file ? RefdataValue.getByValueAndCategory(packageRecord.file,RDConstants.PACKAGE_FILE) : null
                RefdataValue scope = packageRecord.scope ? RefdataValue.getByValueAndCategory(packageRecord.scope,RDConstants.PACKAGE_SCOPE) : null
                Map<String,Object> newPackageProps = [
                    uuid: packageUUID,
                    name: packageRecord.name,
                    packageStatus: packageStatus,
                    contentType: packageRecord.contentType,
                    file: file,
                    scope: scope
                ]
                if(result) {
                    if(packageStatus == packageStatus.get("Deleted") && result.packageStatus != packageStatus.get("Deleted")) {
                        log.info("package #${result.id}, with GOKb id ${result.gokbId} got deleted, mark as deleted and rapport!")
                        result.packageStatus = packageStatus
                    }
                    else {
                        if(packageRecord.nominalPlatformUuid) {
                            Platform nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                            if(!nominalPlatform) {
                                nominalPlatform = createOrUpdatePlatformJSON(packageRecord.nominalPlatformUuid)
                            }
                            newPackageProps.nominalPlatform = nominalPlatform
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
                result.contentType = contentType
                result.scope = scope
                result.file = file
                if(result.save()) {
                    if(packageRecord.nominalPlatformUuid) {
                        Platform nominalPlatform = Platform.findByGokbId(packageRecord.nominalPlatformUuid)
                        if(!nominalPlatform) {
                            nominalPlatform = createOrUpdatePlatformJSON(packageRecord.nominalPlatformUuid)
                        }
                        result.nominalPlatform = nominalPlatform
                        if(!result.save())
                            throw new SyncException(result.errors)
                    }
                    if(packageRecord.providerUuid) {
                        try {
                            Map<String, Object> providerRecord = fetchRecordJSON(false,[uuid:packageRecord.providerUuid])
                            if(providerRecord && !providerRecord.error) {
                                Org provider = createOrUpdateOrgJSON(providerRecord)
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
            result
        }
        else if(packageJSON.error == 404) {
            log.error("we:kb server is unavailable!")
            throw new SyncException("we:kb server was down!")
        }
        else {
            log.warn("Package ${packageUUID} seems to be unexistent!")
            //test if local record trace exists ...
            Package result = Package.findByGokbId(packageUUID)
            if(result) {
                log.warn("Package found, set cascading delete ...")
                result.packageStatus = RDStore.PACKAGE_STATUS_DELETED
                result.save()
                result
            }
            else null
        }
    }

    /**
     * Was formerly in the {@link Org} domain class; deployed for better maintainability
     * Checks for a given UUID if the provider exists, otherwise, it will be created.
     *
     * @param providerUUID the GOKb UUID of the given provider {@link Org}
     * @throws SyncException
     */
    Org createOrUpdateOrgJSON(Map<String,Object> providerJSON) throws SyncException {
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
        //second attempt failed - create new org
        if(!provider) {
            provider = new Org(
                    name: providerRecord.name,
                    gokbId: providerRecord.uuid
            )
        }
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
                if(oldPersons) {
                    PersonRole.executeUpdate('delete from PersonRole pr where pr.org = :provider and pr.prs in (:oldPersons) and pr.functionType.id in (:funcTypes)', [provider: provider, oldPersons: oldPersons, funcTypes: contactTypes.values().collect { RefdataValue cct -> cct.id }])
                    Contact.executeUpdate('delete from Contact c where c.prs in (:oldPersons)', [oldPersons: oldPersons])
                    Person.executeUpdate('delete from Person p where p in (:oldPersons)', [oldPersons: oldPersons])
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
                    plat = createOrUpdatePlatformJSON(platformData.uuid)
                plat.org = provider
                plat.save()
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
            Date lastUpdatedTime = DateUtils.parseDateGeneric(providerRecord.lastUpdatedDisplay)
            if(lastUpdatedTime.getTime() > maxTimestamp) {
                maxTimestamp = lastUpdatedTime.getTime()
            }
            provider
        }
        else throw new SyncException(provider.errors)
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
                Org publisher = createOrUpdateOrgJSON(publisherData)
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
            platform.status = RefdataValue.getByValueAndCategory(platformRecord.status, RDConstants.PLATFORM_STATUS)
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
                if(packagesOfPlatform) {
                    packagesOfPlatform.records.each { pkgRecord ->
                        Package pkg = Package.findByGokbId(pkgRecord.uuid)
                        if(pkg) {
                            pkg.nominalPlatform = platform
                            pkg.save()
                        }
                    }
                }
                platform
            }
            else throw new SyncException("Error on saving platform: ${platform.errors}")
            //}
        }
        else if(platformJSON && platformJSON.error == 404) {
            throw new SyncException("we:kb server is down")
        }
        else {
            log.warn("Platform ${platformUUID} seems to be unexistent!")
            //test if local record trace exists ...
            Platform result = Platform.findByGokbId(platformUUID)
            if(result) {
                log.warn("Platform found, set cascading delete ...")
                result.status = RDStore.PLATFORM_STATUS_DELETED
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
        Set<String> controlledProperties = ['name','packageStatus']

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
    Map<String,Object> processTippDiffs(TitleInstancePackagePlatform tippA, Map tippB) {
        //ex updatedTippClosure / tippUnchangedClosure
        RefdataValue status = tippStatus.get(tippB.status)
        if(status == RDStore.TIPP_STATUS_REMOVED && tippA.status != status) {
            //the difference to event: delete is that the title is an error and should have never been appeared in LAS:eR!
            log.info("TIPP with UUID ${tippA.gokbId} has been marked as erroneous and removed")
            tippA.status = RDStore.TIPP_STATUS_REMOVED
            tippA.save()
            [event: "remove", target: tippA]
        }
        else if ((status == RDStore.TIPP_STATUS_DELETED || tippA.pkg.packageStatus == RDStore.PACKAGE_STATUS_DELETED || tippA.platform.status == RDStore.PLATFORM_STATUS_DELETED) && tippA.status != status) {
            log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${tippA.pkg.gokbId} or package/platform itself are marked as deleted")
            RefdataValue oldStatus = tippA.status
            tippA.status = RDStore.TIPP_STATUS_DELETED
            tippA.save()
            [event: "delete", oldValue: oldStatus, target: tippA]
        }
        else if(!(tippA.status in [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED]) && !(status in [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED])) {
            //process central differences which are without effect to issue entitlements
            tippA.titleType = tippB.titleType
            //tippA.name = tippB.name //TODO include name, sortname in IssueEntitlements, then, this property may move to the controlled ones
            if(tippA.altnames) {
                List<String> oldAltNames = tippA.altnames.collect { AlternativeName altname -> altname.name }
                tippB.altnames.each { String newAltName ->
                    if(!oldAltNames.contains(newAltName)) {
                        if(!AlternativeName.construct([tipp: tippA, name: newAltName]))
                            throw new SyncException("error on creating new alternative name for title ${tippA}")
                    }
                }
            }
            tippA.firstAuthor = tippB.firstAuthor
            tippA.firstEditor = tippB.firstEditor
            tippA.editionStatement = tippB.editionStatement
            tippA.publisherName = tippB.publisherName
            tippA.hostPlatformURL = tippB.hostPlatformURL
            tippA.dateFirstInPrint = (Date) tippB.dateFirstInPrint
            tippA.dateFirstOnline = (Date) tippB.dateFirstOnline
            tippA.seriesName = tippB.seriesName
            tippA.subjectReference = tippB.subjectReference
            tippA.volume = tippB.volume
            tippA.medium = titleMedium.get(tippB.medium)
            tippA.accessType = accessType.get(tippB.accessType)
            tippA.openAccess = openAccess.get(tippB.openAccess)
            if(!tippA.save())
                throw new SyncException("Error on updating base title data: ${tippA.errors}")
            if(tippB.titlePublishers) {
                if(tippA.publishers) {
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.tipp = :tippA',[tippA:tippA])
                }
                tippB.titlePublishers.each { publisher ->
                    lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid], tippA)
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
        else [:]
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
        //if(!priceDiffs.isEmpty())
            //result.add([prop: 'price', priceDiffs: priceDiffs]) are auto-applied

        if (tippb.containsKey("name") && tippa.name != tippb.name) {
            result.add([prop: 'name', newValue: tippb.name, oldValue: tippa.name])
        }

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
     * @param tippA the old {@link TitleInstancePackagePlatform} object, containing the current {@link Set} of  or price items
     * @param listB the new statements (a {@link List} of remote records, kept in {@link Map}s)
     * @param instanceType the container class (may be coverage or price)
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
                println "Statement ${equivalent.id} located as equivalent to ${itemB} by ${k}: ${itemB[k]}"
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
        //I need to address a bulk output endpoint like https://github.com/hbz/lobid-resources/blob/f93201bec043cc732b27814a6ab4aea390d1aa9e/web/app/controllers/resources/Application.java, method bulkResult().
        //By then, I should query the "normal" endpoint /wekb/api/find?
        HTTPBuilder http
        if(useScroll) {
            http = new HTTPBuilder(source.uri + '/scroll')
            String debugString = source.uri+'/scroll?'
            queryParams.each { String k, v ->
                debugString += '&' + k + '=' + v
            }
            log.debug(debugString)
        }
        else http = new HTTPBuilder(source.uri+'/find')
        Map<String,Object> result = [:]
        //setting default status
        if(queryParams.componentType == 'TitleInstancePackagePlatform' || queryParams.component_type == 'TitleInstancePackagePlatform') {
            queryParams.status = ["Current","Expected","Retired","Deleted",PERMANENTLY_DELETED,"Removed"]
            //queryParams.status = ["Removed"] //debug only
        }
        //log.debug(queryParams.toMapString())
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
                if(resp.status == 404) {
                    result.error = resp.status
                }
                else
                    throw new SyncException("error on request: ${resp.statusLine} : ${reader}")
            }
        }
        http.shutdown()
        result
    }

    /**
     * In order to save performance, reference data is being loaded prior to the synchronisation
     */
    void defineMapFields() {
        //define map fields
        tippStatus.put(RDStore.TIPP_STATUS_CURRENT.value,RDStore.TIPP_STATUS_CURRENT)
        tippStatus.put(RDStore.TIPP_STATUS_DELETED.value,RDStore.TIPP_STATUS_DELETED)
        tippStatus.put(PERMANENTLY_DELETED,RDStore.TIPP_STATUS_DELETED)
        tippStatus.put(RDStore.TIPP_STATUS_RETIRED.value,RDStore.TIPP_STATUS_RETIRED)
        tippStatus.put(RDStore.TIPP_STATUS_EXPECTED.value,RDStore.TIPP_STATUS_EXPECTED)
        tippStatus.put(RDStore.TIPP_STATUS_REMOVED.value,RDStore.TIPP_STATUS_REMOVED)
        tippStatus.put(RDStore.TIPP_STATUS_UNKNOWN.value,RDStore.TIPP_STATUS_UNKNOWN)
        contactTypes.put(RDStore.PRS_FUNC_TECHNICAL_SUPPORT.value,RDStore.PRS_FUNC_TECHNICAL_SUPPORT)
        contactTypes.put(RDStore.PRS_FUNC_SERVICE_SUPPORT.value,RDStore.PRS_FUNC_SERVICE_SUPPORT)
        //this complicated way is necessary because of static in order to avoid a NonUniqueObjectException
        List<RefdataValue> staticMediumTypes = [RDStore.TITLE_TYPE_DATABASE,RDStore.TITLE_TYPE_EBOOK,RDStore.TITLE_TYPE_JOURNAL]
        RefdataValue.findAllByIdNotInListAndOwner(staticMediumTypes.collect { RefdataValue rdv -> rdv.id }, RefdataCategory.findByDesc(RDConstants.TITLE_MEDIUM)).each { RefdataValue rdv ->
            titleMedium.put(rdv.value, rdv)
        }
        staticMediumTypes.each { RefdataValue rdv ->
            titleMedium.put(rdv.value, rdv)
        }
        List<RefdataValue> staticPackageStatus = [RDStore.PACKAGE_STATUS_DELETED]
        RefdataValue.findAllByIdNotInListAndOwner(staticPackageStatus.collect { RefdataValue rdv -> rdv.id }, RefdataCategory.findByDesc(RDConstants.PACKAGE_STATUS)).each { RefdataValue rdv ->
            packageStatus.put(rdv.value, rdv)
        }
        staticPackageStatus.each { RefdataValue rdv ->
            packageStatus.put(rdv.value, rdv)
        }
        packageStatus.put(PERMANENTLY_DELETED, RDStore.PACKAGE_STATUS_DELETED)
        orgStatus.put(RDStore.ORG_STATUS_CURRENT.value,RDStore.ORG_STATUS_CURRENT)
        orgStatus.put(RDStore.ORG_STATUS_DELETED.value,RDStore.ORG_STATUS_DELETED)
        orgStatus.put(PERMANENTLY_DELETED,RDStore.ORG_STATUS_DELETED)
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
        initialPackagesCounter = [:]
        pkgPropDiffsContainer = [:]
        packagesToNotify = [:]
        titlesToRemove = []
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
                openAccess.put('Empty', RefdataValue.getByValueAndCategory('Empty', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Gold OA', RefdataValue.getByValueAndCategory('Gold Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Green OA', RefdataValue.getByValueAndCategory('Green Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Hybrid', RefdataValue.getByValueAndCategory('Hybrid', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('White OA', RefdataValue.getByValueAndCategory('White Open Access', RDConstants.LICENSE_OA_TYPE))
                openAccess.put('Yellow OA', RefdataValue.getByValueAndCategory('Yellow Open Access', RDConstants.LICENSE_OA_TYPE))
                break
        }
    }
}
