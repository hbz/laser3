package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.base.AbstractLockableService
import de.laser.config.ConfigMapper
import de.laser.exceptions.SyncException
import de.laser.finance.PriceItem
import de.laser.http.BasicHttpClient
import de.laser.remote.GlobalRecordSource
import de.laser.storage.Constants
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.system.SystemEvent
import de.laser.titles.TitleHistoryEvent
import de.laser.utils.DateUtils
import de.laser.wekb.DeweyDecimalClassification
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.InvoicingVendor
import de.laser.wekb.Language
import de.laser.wekb.LibrarySystem
import de.laser.wekb.Package
import de.laser.wekb.PackageVendor
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TIPPCoverage
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
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
 * For the reference: the following record type constants hold:
 * <ol start="0">
 *  <li>{@link #RECTYPE_PACKAGE}</li>
 *  <li>{@link #RECTYPE_PLATFORM}</li>
 *  <li>{@link #RECTYPE_PROVIDER}</li>
 *  <li>{@link #RECTYPE_TIPP}</li>
 *  <li>{@link #RECTYPE_VENDOR}</li>
 * </ol>
 */
@Transactional
class GlobalSourceSyncService extends AbstractLockableService {

    EscapeService escapeService
    ExecutorService executorService
    GlobalService globalService
    PackageService packageService
    GlobalRecordSource source

    static final long RECTYPE_PACKAGE = 0
    static final long RECTYPE_PLATFORM = 1
    static final long RECTYPE_PROVIDER = 2
    static final long RECTYPE_TIPP = 3
    static final long RECTYPE_VENDOR = 4
    static final int MAX_CONTENT_LENGTH = 1024 * 1024 * 100
    static final int MAX_TIPP_COUNT_PER_PAGE = 20000

    Map<String, RefdataValue> titleMedium = [:],
            tippStatus = [:],
            packageStatus = [:],
//            orgStatus = [:],
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
        List finishedJobs = []
        SystemEvent.createEvent('GSSS_JSON_START', ['jobs started': jobs.size()])

        jobs.each { GlobalRecordSource source ->
            this.source = source
            maxTimestamp = 0
            try {
                Thread.currentThread().setName("GlobalDataSync_Json")
                Date oldDate = source.haveUpTo
                //Date oldDate = DateUtils.getSDF_ymd().parse('2022-01-01') //debug only
                log.info("getting records from job #${source.id} with uri ${source.getUri()} since ${oldDate}")
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
                    case RECTYPE_PROVIDER: componentType = 'Org'
                        break
                    case RECTYPE_VENDOR: componentType = 'Vendor'
                        break
                    case RECTYPE_PACKAGE: componentType = 'Package'
                        break
                    case RECTYPE_PLATFORM: componentType = 'Platform'
                        break
                    case RECTYPE_TIPP: componentType = 'TitleInstancePackagePlatform'
                        break
                }
                //do prequest: are we needing the scroll api?
                Map<String,Object> result = fetchRecordJSON(false,[componentType:componentType,changedSince:sdf.format(oldDate),max:MAX_TIPP_COUNT_PER_PAGE,sort:'lastUpdated']) //changedBefore:'2022-09-27 00:00:00',
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
                                List subPkgHolders = SubscriptionPackage.executeQuery(query,[packageKey:packageKey,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER]])
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
                    finishedJobs << source.id
                }
            }
            catch (Exception e) {
                SystemEvent.createEvent('GSSS_JSON_ERROR',['jobId':source.id])
                log.error("sync job has failed, please consult stacktrace as follows: ",e)
            }
        }
        if (finishedJobs) {
            SystemEvent.createEvent('GSSS_JSON_COMPLETE', ['finishedJobs': finishedJobs])
        }
        running = false
    }

    /**
     * Updates the data for a single package, using the global update mechanism
     * @param packageUUID the UUID of the package to be updated
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
                Map<String,Object> result = fetchRecordJSON(false,[componentType:componentType,tippPackageUuid:packageUUID,max:MAX_TIPP_COUNT_PER_PAGE,sort:'lastUpdated'])
                if(result.error == 404) {
                    log.error("we:kb server is down")
                }
                else {
                    Set<String> wekbTippUUIDs = []
                    if(result) {
                        wekbTippUUIDs.addAll(processScrollPage(result, componentType, null, packageUUID, permanentlyDeletedTitles))
                    }
                    else {
                        log.info("no records updated - leaving everything as is ...")
                    }
                    Package pkg = Package.findByGokbId(packageUUID)
                    if(pkg) {
                        permanentlyDeletedTitles.collate(5000).each { subSet ->
                            Set<TitleInstancePackagePlatform> delTippsInPackage = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.gokbId in (:delUUIDs) and tipp.pkg = :pkg and tipp.status != :removed', [delUUIDs: subSet, pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])
                            delTippsInPackage.each { TitleInstancePackagePlatform tipp ->
                                tipp.status = RDStore.TIPP_STATUS_REMOVED
                                tipp.save()
                                IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tipp, now: new Date()])
                                PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp = :tipp', [tipp: tipp])
                            }
                        }
                        Set<String> orphanedTippUUIDs = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])
                        orphanedTippUUIDs.removeAll(wekbTippUUIDs)
                        if(orphanedTippUUIDs) {
                            log.info("located ${orphanedTippUUIDs.size()} without connection")
                            orphanedTippUUIDs.collate(5000).each { subSet ->
                                Set<TitleInstancePackagePlatform> orphanedTipps = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.gokbId in (:delUUIDs) and tipp.pkg = :pkg and tipp.status != :removed', [delUUIDs: subSet, pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])
                                orphanedTipps.each { TitleInstancePackagePlatform tipp ->
                                    tipp.status = RDStore.TIPP_STATUS_REMOVED
                                    tipp.save()
                                    IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tipp, now: new Date()])
                                    PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp = :tipp', [tipp: tipp])
                                }
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
                            List subPkgHolders = SubscriptionPackage.executeQuery(query,[packageKey:packageKey,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIUM,RDStore.OR_SUBSCRIBER]])
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
     * @param componentType the component type (one of Org, Package, Platform, TitleInstancePackagePlatform) to update
     */
    void reloadData(String componentType) {
        running = true
        defineMapFields()
            long rectype
            switch(componentType) {
                case 'Org': rectype = RECTYPE_PROVIDER
                    break
                case 'Package': rectype = RECTYPE_PACKAGE
                    break
                case 'Platform': rectype = RECTYPE_PLATFORM
                    break
                case 'TitleInstancePackagePlatform': rectype = RECTYPE_TIPP
                    break
                case 'Vendor': rectype = RECTYPE_VENDOR
                    break
                default: rectype = -1
                    break
            }
            this.source = GlobalRecordSource.findByActiveAndRectype(true,rectype)
            log.info("getting all records from job #${source.id} with uri ${source.getUri()}")
            try {
                Map<String,Object> result = fetchRecordJSON(false,[componentType: componentType, max: MAX_TIPP_COUNT_PER_PAGE, sort:'lastUpdated'])
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
     * @param dataToLoad the property to update for every object (one of identifier, abbreviatedName, sortTitle, ddc, accessType, openAccess, language, editionStatement or titleNamespace)
     */
    void updateData(String dataToLoad) {
        running = true
            this.source = GlobalRecordSource.findByActiveAndRectype(true,RECTYPE_TIPP)
            List<String> triggeredTypes
            int max
            switch(dataToLoad) {
                case "identifier": triggeredTypes = ['Package','Org','TitleInstancePackagePlatform']
                    max = 100
                    break
                case "abbreviatedName": triggeredTypes = ['Org', 'Vendor']
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
                        Map<String, Object> queryParams = [componentType: componentType, max: max, offset: offset, sort: 'lastUpdated']
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
                                        case 'Org': List<Provider> providers = Provider.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from current page, ${providers.size()} providers exist in LAS:eR")
                                            providers.eachWithIndex { Provider provider, int idx ->
                                                log.debug("now processing provider ${idx} with uuid ${provider.gokbId}, total entry: ${offset+idx}")
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
                                        case 'Vendor': List<Vendor> vendors = Vendor.findAllByGokbIdInList(wekbRecords.keySet().toList())
                                            log.debug("from current page, ${vendors.size()} agencies exist in LAS:eR")
                                            vendors.eachWithIndex { Vendor vendor, int idx ->
                                                log.debug("now processing vendor ${idx} with uuid ${vendor.gokbId}, total entry: ${offset+idx}")
                                                //switch kept for possible extension
                                                switch(dataToLoad) {
                                                    case "abbreviatedName": vendor.sortname = wekbRecords.get(vendor.gokbId).abbreviatedName
                                                        vendor.save()
                                                        break
                                                }
                                            }
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
     * @param permanentlyDeletedTitles a set of keys of permanently deleted titles in order to clear them in LAS:eR too
     * @throws SyncException if an error occurs during the update process
     * @returns a {@link Set} of we:kb title UUIDs (used only for RECTYPE_TIPP)
     */
    Set<String> processScrollPage(Map<String, Object> result, String componentType, String changedSince, String pkgFilter = null, Set<String> permanentlyDeletedTitles = []) throws SyncException {
        Set<String> wekbTippUUIDs = []
        if(result.count >= MAX_TIPP_COUNT_PER_PAGE) {
            int offset = 0, max = MAX_TIPP_COUNT_PER_PAGE
            Map<String, Object> queryParams = [componentType: componentType, offset: offset, max: max, sort: 'lastUpdated']
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
                            case RECTYPE_PROVIDER:
                                result.records.each { record ->
                                    /*
                                    structure not existing in we:kb
                                    record.platforms.each { Map platformData ->
                                        try {
                                            createOrUpdatePlatform(platformData.uuid)
                                        }
                                        catch (SyncException e) {
                                            log.error("Error on updating platform ${platformData.uuid}: ",e)
                                            SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                                        }
                                    }
                                    */
                                    createOrUpdateProvider(record)
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
                                            log.info("${PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp.pkg = :pkg and pt.tipp.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])} permanent title (tipps) really deleted")
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
                                wekbTippUUIDs.addAll( result.records.collect { tipp -> tipp.uuid } )
                                if(offset == 0)
                                    updateRecords(result.records, offset, permanentlyDeletedTitles)
                                else updateRecords(result.records, offset)
                                break
                            case RECTYPE_VENDOR:
                                result.records.each { record ->
                                    record.packages.eachWithIndex { Map packageData, int i ->
                                        try {
                                            //log.debug("now processing vendor package ${i} out of ${record.packages.size()} packages")
                                            Package pkg = Package.findByGokbId(packageData.packageUuid)
                                            if(!pkg)
                                                createOrUpdatePackage(packageData.packageUuid)
                                        }
                                        catch (SyncException e) {
                                            log.error("Error on updating package ${packageData.uuid}: ",e)
                                            SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:packageData.uuid])
                                        }
                                    }
                                    createOrUpdateVendor(record)
                                }
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
                case RECTYPE_PROVIDER:
                    result.records.each { record ->
                        /*
                        structure not existing in we:kb
                        record.platforms.each { Map platformData ->
                            try {
                                createOrUpdatePlatform(platformData.uuid)
                            }
                            catch (SyncException e) {
                                log.error("Error on updating platform ${platformData.uuid}: ",e)
                                SystemEvent.createEvent("GSSS_JSON_WARNING",[platformRecordKey:platformData.uuid])
                            }
                        }
                        */
                        createOrUpdateProvider(record)
                    }
                    break
                case RECTYPE_PACKAGE:
                    result.records.eachWithIndex { record, int i ->
                        try {
//                            log.debug("now processing record #${i}")
                            Package pkg = createOrUpdatePackage(record)
                            //package may be null in case it has been marked as removed and did not exist in LAS:eR before
                            if(pkg?.packageStatus == RDStore.PACKAGE_STATUS_REMOVED) {
                                log.info("${pkg.name} / ${pkg.gokbId} has been removed, record status is ${record.status}, mark titles in package as removed ...")
                                log.info("${IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg) and ie.status != :removed', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED, now: new Date()])} issue entitlements marked as removed")
                                log.info("${PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status != :removed)', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])} permanent title (tipps) really deleted")
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
                case RECTYPE_TIPP: wekbTippUUIDs.addAll( result.records.collect { tipp -> tipp.uuid } )
                    updateRecords(result.records, 0, permanentlyDeletedTitles)
                    break
                case RECTYPE_VENDOR:
                    result.records.each { record ->
                        record.packages.eachWithIndex { Map packageData, int i ->
                            try {
                                //log.debug("now processing vendor package ${i} out of ${record.packages.size()} packages")
                                Package pkg = Package.findByGokbId(packageData.packageUuid)
                                if(!pkg)
                                    createOrUpdatePackage(packageData.packageUuid)
                            }
                            catch (SyncException e) {
                                log.error("Error on updating package ${packageData.uuid}: ",e)
                                SystemEvent.createEvent("GSSS_JSON_WARNING",[packageRecordKey:packageData.uuid])
                            }
                        }
                        createOrUpdateVendor(record)
                    }
                    break
            }
        }
        else if(result.error && result.error == 404) {
            log.error("we:kb server is down")
            throw new SyncException("we:kb server is unavailable!")
        }
        wekbTippUUIDs
    }

    /**
     * Updates the records on the given page
     * @param rawRecords the scroll page (JSON result) containing the updated entries
     * @param offset the total record counter offset which has to be added to the entry loop counter
     * @param permanentlyDeletedTitles a set of keys of permanently deleted titles in order to delete the LAS:eR records as well
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
            tippUUIDs.collate(5000).each { List<String> subList ->
                TitleInstancePackagePlatform.findAllByGokbIdInList(subList).each { TitleInstancePackagePlatform tipp ->
                    tippsInLaser.put(tipp.gokbId, tipp)
                }
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
//            log.debug("now processing entry #${idx}, total entry #${offset+idx} with uuid ${tipp.uuid}")
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
                if(tipp.titleType == 'serial') {
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
                else if(updatedTIPP.status == Constants.PERMANENTLY_DELETED) {
                    TitleInstancePackagePlatform tippA = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(updatedTIPP.uuid, RDStore.TIPP_STATUS_REMOVED)
                    if(tippA) {
                        log.info("TIPP with UUID ${tippA.gokbId} has been permanently deleted")
                        tippA.status = RDStore.TIPP_STATUS_REMOVED
                        tippA.save()
                        IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tippA, now: new Date()])
                        PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp = :tipp', [tipp: tippA])
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
                else if(!(updatedTIPP.status in [RDStore.TIPP_STATUS_REMOVED.value, Constants.PERMANENTLY_DELETED])) {
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
                PermanentTitle.executeUpdate('delete from PermanentTitle pt where pt.tipp = :tipp', [tipp: tippA])
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
            Set<Subscription> subsConcerned = Subscription.executeQuery("select s from Subscription s join s.packages sp where ((s.endDate is not null and s.endDate >= :now) or s.hasPerpetualAccess = true) and s.holdingSelection = :entire and sp.pkg = :pkg and s.instanceOf = null", [now: now, entire: RDStore.SUBSCRIPTION_HOLDING_ENTIRE, pkg: tipp.pkg])
            //we may need to switch to native sql ...
            subsConcerned.each { Subscription s ->
                IssueEntitlement ie = IssueEntitlement.construct([subscription: s, tipp: tipp])
                if(s.hasPerpetualAccess) {
                    Org owner = s.getSubscriberRespConsortia()
                    PermanentTitle perm = PermanentTitle.findByOwnerAndTipp(owner, tipp)
                    if(!perm) {
                        perm = new PermanentTitle(owner: owner, tipp: tipp, subscription: s, issueEntitlement: ie)
                        perm.save()
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
                if(pkg && pkg?.packageStatus != RDStore.PACKAGE_STATUS_REMOVED && !(tippB.status in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value])) {
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
     * Looks up for a given UUID if a local record exists or not. If no {@link de.laser.wekb.Package} record exists, it will be
     * created with the given remote record data, otherwise, the local record is going to be updated. The {@link TitleInstancePackagePlatform records}
     * in the {@link de.laser.wekb.Package} will be checked for differences and if there are such, the according fields updated. Same counts for the {@link de.laser.wekb.TIPPCoverage} records
     * in the {@link TitleInstancePackagePlatform}s
     * @param packageData A UUID pointing to record extract or the record itself for a given package
     * @return the updated package record
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
                                Provider provider = Provider.findByGokbId(packageRecord.providerUuid)
                                if(!provider) {
                                    Map<String, Object> providerRecord = fetchRecordJSON(false,[uuid:packageRecord.providerUuid])
                                    if(providerRecord && !providerRecord.error) {
                                        result.provider = createOrUpdateProvider(providerRecord)
                                    }
                                    else if(providerRecord && providerRecord.error == 404) {
                                        log.error("we:kb server is down")
                                        throw new SyncException("we:kb server is unvailable")
                                    }
                                    else
                                        throw new SyncException("Provider loading failed for UUID ${packageRecord.providerUuid}!")
                                }
                                else result.provider = provider
                                if (!result.save())
                                    throw new SyncException(result.errors)
                            }
                            catch (SyncException e) {
                                throw e
                            }
                        }
                        if(packageRecord.vendors) {
                            List<String> packageVendorsB = packageRecord.vendors.collect { Map vendorData -> vendorData.vendorUuid }
                            PackageVendor.executeUpdate('delete from PackageVendor pv where pv.pkg = :pkg and pv.vendor not in (select v from Vendor v where v.gokbId in (:pvB))', [pkg: result, pvB: packageVendorsB])
                            packageVendorsB.each { String vendorUuid ->
                                Vendor vendor = Vendor.findByGokbId(vendorUuid)
                                if(vendor) {
                                    setupPkgVendor(vendor, result)
                                }
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
     * Checks for a given UUID if the provider exists, otherwise, it will be created
     *
     * @param providerUUID the GOKb UUID or JSON record of the given {@link Provider}
     * @return the updated provider record
     * @throws SyncException
     */
    Provider createOrUpdateProvider(Map<String,Object> providerJSON) throws SyncException {
        Map providerRecord
        if(providerJSON.records)
            providerRecord = providerJSON.records[0]
        else providerRecord = providerJSON
        log.info("provider record loaded, reconciling provider record for UUID ${providerRecord.uuid}")
        //first attempt
        Provider provider = Provider.findByGokbId(providerRecord.uuid)
        //attempt succeeded
        if(provider) {
            provider.name = providerRecord.name
        }
        //second attempt
        else if(!provider) {
            provider = Provider.findByNameAndGokbIdIsNull(providerRecord.name)
            //second attempt succeeded - map gokbId to provider who already exists
            if(provider)
                provider.gokbId = providerRecord.uuid
        }
        //second attempt failed - create new provider if record is not deleted
        if(!provider) {
            if(!(providerRecord.status in [Constants.PERMANENTLY_DELETED, 'Removed'])) {
                provider = new Provider(
                        name: providerRecord.name,
                        gokbId: providerRecord.uuid
                )
                provider.setGlobalUID()
            }
        }
        //avoid creating new deleted entries
        if(provider) {
            provider.status = RefdataValue.getByValueAndCategory(providerRecord.status, RDConstants.PROVIDER_STATUS)
            provider.sortname = providerRecord.abbreviatedName
            provider.homepage = providerRecord.homepage
            provider.metadataDownloaderURL = providerRecord.metadataDownloaderURL
            provider.kbartDownloaderURL = providerRecord.kbartDownloaderURL
            provider.paperInvoice = providerRecord.paperInvoice == RDStore.YN_YES.value
            provider.managementOfCredits = providerRecord.managementOfCredits == RDStore.YN_YES.value
            provider.processingOfCompensationPayments = providerRecord.processingOfCompensationPayments == RDStore.YN_YES.value
            provider.individualInvoiceDesign = providerRecord.individualInvoiceDesign == RDStore.YN_YES.value
            provider.inhouseInvoicing = providerRecord.invoicingYourself == RDStore.YN_YES.value
            if((provider.status == RDStore.PROVIDER_STATUS_CURRENT || !provider.status) && providerRecord.status == RDStore.PROVIDER_STATUS_RETIRED.value) {
                //value is not implemented in we:kb yet
                if(providerRecord.retirementDate) {
                    provider.retirementDate = DateUtils.parseDateGeneric(providerRecord.retirementDate)
                }
                else provider.retirementDate = new Date()
            }
            if(provider.save()) {
                if(providerRecord.contacts) {
                    List<String> typeNames = contactTypes.values().collect { RefdataValue cct -> cct.getI10n("value") }
                    typeNames.addAll(contactTypes.keySet())
                    List<Person> oldPersons = Person.executeQuery('select p from Person p join p.roleLinks pr where p.tenant = null and p.isPublic = true and p.last_name in (:contactTypes) and :provider in (pr.provider)',[provider: provider, contactTypes: typeNames])
                    List<Long> funcTypes = contactTypes.values().collect { RefdataValue cct -> cct.id }
                    oldPersons.each { Person old ->
                        PersonRole.executeUpdate('delete from PersonRole pr where pr.provider = :provider and pr.prs = :oldPerson and pr.functionType.id in (:funcTypes)', [provider: provider, oldPerson: old, funcTypes: funcTypes])
                        Contact.executeUpdate('delete from Contact c where c.prs = :oldPerson', [oldPerson: old])
                        if(PersonRole.executeQuery('select count(*) from PersonRole pr where pr.prs = :oldPerson and pr.provider = :provider and (pr.functionType.id not in (:funcTypes) or pr.functionType = null)', [provider: provider, oldPerson: old, funcTypes: funcTypes])[0] == 0) {
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
                            case "Statistical Support":
                                contact.rdType = RDStore.PRS_FUNC_STATS_SUPPORT
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
                            if(!AlternativeName.construct([provider: provider, name: newAltName]))
                                throw new SyncException("error on creating new alternative name for provider ${provider}")
                        }
                    }
                }
                /*
                structure not provided in we:kb
                providerRecord.platforms.each { Map platformData ->
                    Platform plat = Platform.findByGokbId(platformData.uuid)
                    if(!plat)
                        plat = createOrUpdatePlatform(platformData.uuid)
                    if(plat) {
                        plat.org = provider
                        plat.save()
                    }
                }
                providerRecord.packages.each { Map packageData ->
                    Package pkg = Package.findByGokbId(packageData.packageUuid)
                    if(pkg) {
                        setupOrgRole([org: provider, pkg: pkg, roleTypeCheckup: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER], definiteRoleType: RDStore.OR_PROVIDER])
                    }
                }
                */
                if(providerRecord.identifiers) {
                    if(provider.ids) {
                        Identifier.executeUpdate('delete from Identifier i where i.provider = :provider',[provider: provider]) //damn those wrestlers ...
                    }
                    providerRecord.identifiers.each { id ->
                        if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                            Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: provider, isUnique: false, nsType: Org.class.name])
                        }
                    }
                }
                List<String> electronicBillingsB = providerRecord.electronicBillings.collect { ebB -> ebB.electronicBilling },
                             invoiceDispatchsB = providerRecord.invoiceDispatchs.collect { idiB -> idiB.invoiceDispatch },
                             invoicingVendorsB = providerRecord.invoicingVendors.collect { ivB -> ivB.vendorUuid }
                provider.electronicBillings.each { ElectronicBilling ebA ->
                    if(!electronicBillingsB.contains(ebA.invoicingFormat.value))
                        ebA.delete()
                }
                electronicBillingsB.each { String ebB ->
                    if(!provider.hasElectronicBilling(ebB)) {
                        new ElectronicBilling(provider: provider, invoicingFormat: RefdataValue.getByValueAndCategory(ebB, RDConstants.VENDOR_INVOICING_FORMAT)).save()
                    }
                }
                provider.invoiceDispatchs.each { InvoiceDispatch idiA ->
                    if(!invoiceDispatchsB.contains(idiA.invoiceDispatch.value))
                        idiA.delete()
                }
                invoiceDispatchsB.each { String idiB ->
                    if(!provider.hasInvoiceDispatch(idiB)) {
                        new InvoiceDispatch(provider: provider, invoiceDispatch: RefdataValue.getByValueAndCategory(idiB, RDConstants.VENDOR_INVOICING_DISPATCH)).save()
                    }
                }
                if(provider.invoicingVendors) {
                    provider.invoicingVendors.each { InvoicingVendor ivA ->
                        if(!(ivA.vendor.gokbId in invoicingVendorsB))
                            ivA.delete()
                    }
                }
                providerRecord.invoicingVendors.each { Map vendorData ->
                    Vendor v = Vendor.findByGokbId(vendorData.vendorUuid)
                    if(!v) {
                        createOrUpdateVendor(vendorData.vendorUuid)
                    }
                    if(v) {
                        setupInvoicingVendor(provider, v)
                    }
                }
                if(source.rectype == RECTYPE_PROVIDER) {
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
     * Checks for a given UUID if the vendor / agency exists, otherwise, it will be created
     * @param vendorInput the UUID or JSON record of the given {@link Vendor}
     * @return the updated vendor record
     * @throws SyncException
     */
    Vendor createOrUpdateVendor(vendorInput) throws SyncException {
        Map vendorRecord, vendorJSON
        if(vendorInput instanceof String) {
            vendorJSON = fetchRecordJSON(false, [uuid: vendorInput])
            vendorRecord = vendorJSON.records[0]
        }
        else if(vendorInput instanceof Map) {
            if(vendorInput.records)
                vendorRecord = vendorInput.records[0]
            else vendorRecord = vendorInput
        }
        if(vendorRecord) {
            log.info("vendor record loaded, reconciling vendor record for UUID ${vendorRecord.uuid}")
            //first attempt
            Vendor vendor = Vendor.findByGokbId(vendorRecord.uuid)
            //attempt succeeded
            if(vendor) {
                vendor.name = vendorRecord.name
            }
            //second attempt
            else if(!vendor) {
                vendor = Vendor.findByNameAndGokbIdIsNull(vendorRecord.name)
                //second attempt succeeded - map gokbId to provider who already exists
                if(vendor)
                    vendor.gokbId = vendorRecord.uuid
            }
            //second attempt failed - create new vendor if record is not deleted
            if(!vendor) {
                if(!(vendorRecord.status in [Constants.PERMANENTLY_DELETED, 'Removed'])) {
                    vendor = new Vendor(
                            name: vendorRecord.name,
                            gokbId: vendorRecord.uuid
                    )
                    vendor.setGlobalUID() //needed because beforeInsertHandler() is being executed after validation only!
                }
            }
            //avoid creating new deleted entries
            if(vendor) {
                vendor.sortname = vendorRecord.abbreviatedName
                /*
                if((vendor.status == RDStore.ORG_STATUS_CURRENT || !vendor.status) && vendorRecord.status == RDStore.ORG_STATUS_RETIRED.value) {
                    //value is not implemented in we:kb yet
                    if(vendorRecord.retirementDate) {
                        vendor.retirementDate = DateUtils.parseDateGeneric(vendorRecord.retirementDate)
                    }
                    else vendor.retirementDate = new Date()
                }
                */
                vendor.status = RefdataValue.getByValueAndCategory(vendorRecord.status, RDConstants.VENDOR_STATUS)
                vendor.homepage = vendorRecord.homepage
                vendor.webShopOrders = vendorRecord.webShopOrders == RDStore.YN_YES.value
                vendor.xmlOrders = vendorRecord.xmlOrders == RDStore.YN_YES.value
                vendor.ediOrders = vendorRecord.ediOrders == RDStore.YN_YES.value
                vendor.paperInvoice = vendorRecord.paperInvoice == RDStore.YN_YES.value
                vendor.managementOfCredits = vendorRecord.managementOfCredits == RDStore.YN_YES.value
                vendor.processingOfCompensationPayments = vendorRecord.processingOfCompensationPayments == RDStore.YN_YES.value
                vendor.individualInvoiceDesign = vendorRecord.individualInvoiceDesign == RDStore.YN_YES.value
                vendor.technicalSupport = vendorRecord.technicalSupport == RDStore.YN_YES.value
                vendor.shippingMetadata = vendorRecord.shippingMetadata == RDStore.YN_YES.value
                vendor.forwardingUsageStatisticsFromPublisher = vendorRecord.forwardingUsageStatisticsFromPublisher == RDStore.YN_YES.value
                vendor.activationForNewReleases = vendorRecord.activationForNewReleases == RDStore.YN_YES.value
                vendor.exchangeOfIndividualTitles = vendorRecord.exchangeOfIndividualTitles == RDStore.YN_YES.value
                vendor.researchPlatformForEbooks = vendorRecord.researchPlatformForEbooks
                vendor.prequalification = vendorRecord.prequalification == RDStore.YN_YES.value
                vendor.prequalificationInfo = vendorRecord.prequalificationInfo
                if(vendor.save()) {
                    if(vendorRecord.contacts) {
                        List<String> typeNames = contactTypes.values().collect { RefdataValue cct -> cct.getI10n("value") }
                        typeNames.addAll(contactTypes.keySet())
                        List<Person> oldPersons = Person.executeQuery('select p from PersonRole pr join pr.prs p where pr.vendor = :vendor and p.tenant = null and p.isPublic = true and p.last_name in (:contactTypes)', [vendor: vendor, contactTypes: typeNames])
                        List<Long> funcTypes = contactTypes.values().collect { RefdataValue cct -> cct.id }
                        oldPersons.each { Person old ->
                            PersonRole.executeUpdate('delete from PersonRole pr where pr.vendor = :vendor and pr.prs = :oldPerson and pr.functionType.id in (:funcTypes)', [vendor: vendor, oldPerson: old, funcTypes: funcTypes])
                            Contact.executeUpdate('delete from Contact c where c.prs = :oldPerson', [oldPerson: old])
                            if (PersonRole.executeQuery('select count(*) from PersonRole pr where pr.prs = :oldPerson and pr.vendor = :vendor and (pr.functionType.id not in (:funcTypes) or pr.functionType = null)', [vendor: vendor, oldPerson: old, funcTypes: funcTypes])[0] == 0) {
                                Person.executeUpdate('delete from Person p where p = :oldPerson', [oldPerson: old])
                            }
                        }
                        vendorRecord.contacts.findAll { Map<String, String> cParams -> cParams.content != null }.each { contact ->
                            switch (contact.type) {
                                case "Invoicing Contact":
                                    contact.rdType = RDStore.PRS_FUNC_INVOICING_CONTACT
                                    break
                                case "Metadata Contact":
                                    contact.rdType = RDStore.PRS_FUNC_METADATA
                                    break
                                case "Service Support":
                                    contact.rdType = RDStore.PRS_FUNC_SERVICE_SUPPORT
                                    break
                                case "Statistical Support":
                                    contact.rdType = RDStore.PRS_FUNC_STATS_SUPPORT
                                    break
                                case "Technical Support":
                                    contact.rdType = RDStore.PRS_FUNC_TECHNICAL_SUPPORT
                                    break
                                default: log.warn("unhandled additional property type for ${vendor.gokbId}: ${contact.name}")
                                    break
                            }
                            if (contact.rdType && contact.contentType != null) {
                                createOrUpdateSupport(vendor, contact)
                            } else log.warn("contact submitted without content type, rejecting contact")
                        }
                    }
                    if(vendorRecord.altname) {
                        List<String> oldAltNames = vendor.altnames.collect { AlternativeName altname -> altname.name }
                        vendorRecord.altname.each { String newAltName ->
                            if(!oldAltNames.contains(newAltName)) {
                                if(!AlternativeName.construct([vendor: vendor, name: newAltName]))
                                    throw new SyncException("error on creating new alternative name for provider ${vendor}")
                            }
                        }
                    }
                    if(vendorRecord.identifiers) {
                        if(vendor.ids) {
                            Identifier.executeUpdate('delete from Identifier i where i.vendor = :vendor',[vendor: vendor]) //damn those wrestlers ...
                        }
                        vendorRecord.identifiers.each { id ->
                            if(!(id.namespace.toLowerCase() in ['originediturl','uri'])) {
                                Identifier.construct([namespace: id.namespace, value: id.value, name_de: id.namespaceName, reference: vendor, isUnique: false, nsType: Org.class.name])
                            }
                        }
                    }
                    List<String> vendorPackagesB = vendorRecord.packages.collect { pvB -> pvB.packageUuid },
                                 supportedLibrarySystemsB = vendorRecord.supportedLibrarySystems.collect { slsB -> slsB.supportedLibrarySystem },
                                 electronicBillingsB = vendorRecord.electronicBillings.collect { ebB -> ebB.electronicBilling },
                                 invoiceDispatchsB = vendorRecord.invoiceDispatchs.collect { idiB -> idiB.invoiceDispatch },
                                 electronicDeliveryDelaysB = vendorRecord.electronicDeliveryDelays.collect { eddnB -> eddnB.electronicDeliveryDelay }
                    if(vendor.packages) {
                        PackageVendor.executeUpdate('delete from PackageVendor pv where pv.vendor = :vendor and pv.pkg not in (select pkg from Package pkg where pkg.gokbId in (:pvB))', [vendor: vendor, pvB: vendorPackagesB]) //cascading ...
                        /*
                        vendor.packages.each { PackageVendor pvA ->
                            if(!(pvA.pkg.gokbId in vendorPackagesB))
                                pvA.delete()
                        }
                        */
                    }
                    vendorPackagesB.each { String packageUuid ->
                        Package pkg = Package.findByGokbId(packageUuid)
                        if(pkg && !(pkg in vendor.packages?.pkg)) {
                            setupPkgVendor(vendor, pkg)
                        }
                    }
                    LibrarySystem.executeUpdate('delete from LibrarySystem lsA where lsA.vendor = :vendor and lsA.librarySystem not in (:lsB)', [vendor: vendor, lsB: RefdataValue.findAllByValueInListAndOwner(supportedLibrarySystemsB, RefdataCategory.findByDesc(RDConstants.SUPPORTED_LIBRARY_SYSTEM))])
                    supportedLibrarySystemsB.each { String lsB ->
                        if(!vendor.isLibrarySystemSupported(lsB)) {
                            new LibrarySystem(vendor: vendor, librarySystem: RefdataValue.getByValueAndCategory(lsB, RDConstants.SUPPORTED_LIBRARY_SYSTEM)).save()
                        }
                    }
                    ElectronicBilling.executeUpdate('delete from ElectronicBilling ebA where ebA.vendor = :vendor and ebA.invoicingFormat not in (:ebB)', [vendor: vendor, ebB: RefdataValue.findAllByValueInListAndOwner(electronicBillingsB, RefdataCategory.findByDesc(RDConstants.VENDOR_INVOICING_FORMAT))])
                    electronicBillingsB.each { String ebB ->
                        if(!vendor.hasElectronicBilling(ebB)) {
                            new ElectronicBilling(vendor: vendor, invoicingFormat: RefdataValue.getByValueAndCategory(ebB, RDConstants.VENDOR_INVOICING_FORMAT)).save()
                        }
                    }
                    InvoiceDispatch.executeUpdate('delete from InvoiceDispatch idA where idA.vendor = :vendor and idA.invoiceDispatch not in (:idB)', [vendor: vendor, idB: RefdataValue.findAllByValueInListAndOwner(invoiceDispatchsB, RefdataCategory.findByDesc(RDConstants.VENDOR_INVOICING_DISPATCH))])
                    invoiceDispatchsB.each { String idiB ->
                        if(!vendor.hasInvoiceDispatch(idiB)) {
                            new InvoiceDispatch(vendor: vendor, invoiceDispatch: RefdataValue.getByValueAndCategory(idiB, RDConstants.VENDOR_INVOICING_DISPATCH)).save()
                        }
                    }
                    ElectronicDeliveryDelayNotification.executeUpdate('delete from ElectronicDeliveryDelayNotification eddnA where eddnA.vendor = :vendor and eddnA.delayNotification not in (:eddnB)', [vendor: vendor, eddnB: RefdataValue.findAllByValueInListAndOwner(electronicDeliveryDelaysB, RefdataCategory.findByDesc(RDConstants.VENDOR_ELECTRONIC_DELIVERY_DELAY))])
                    electronicDeliveryDelaysB.each { String eddnB ->
                        if(!vendor.hasElectronicDeliveryDelayNotification(eddnB)) {
                            new ElectronicDeliveryDelayNotification(vendor: vendor, delayNotification: RefdataValue.getByValueAndCategory(eddnB, RDConstants.VENDOR_ELECTRONIC_DELIVERY_DELAY)).save()
                        }
                    }
                    if(source.rectype == RECTYPE_VENDOR) {
                        Date lastUpdatedTime = DateUtils.parseDateGeneric(vendorRecord.lastUpdatedDisplay)
                        if(lastUpdatedTime.getTime() > maxTimestamp) {
                            maxTimestamp = lastUpdatedTime.getTime()
                        }
                    }
                    vendor
                }
                else throw new SyncException(vendor.errors)
            }
        }
        else throw new SyncException("no vendor record loaded!")
    }

    void setupPkgVendor(Vendor vendor, Package pkg) throws SyncException {
        PackageVendor pv = PackageVendor.findByVendorAndPkg(vendor, pkg)
        if(!pv) {
            pv = new PackageVendor(vendor: vendor, pkg: pkg)
            if(!pv.save())
                throw new SyncException("Error on saving vendor-package link: ${pv.getErrors().getAllErrors().toListString()}")
        }
    }

    void setupInvoicingVendor(Provider provider, Vendor vendor) throws SyncException {
        InvoicingVendor iv = InvoicingVendor.findByVendorAndProvider(vendor, provider)
        if(!iv) {
            iv = new InvoicingVendor(vendor: vendor, provider: provider)
            if(!iv.save())
                throw new SyncException("Error on saving vendor-package link: ${iv.getErrors().getAllErrors().toListString()}")
        }
    }

    /**
     * Updates a technical or service support for a given {@link Provider}; overrides an eventually created one and creates if it does not exist
     * @param provider the {@link Provider} to which the given support address should be created/updated
     * @param supportProps the configuration {@link Map} containing the support address properties
     * @throws SyncException
     */
    void createOrUpdateSupport(Provider provider, Map<String, Object> supportProps) throws SyncException {
        List<Person> personCheck = Person.executeQuery('select p from PersonRole pr join pr.prs p where p.tenant = null and pr.provider = :provider and p.isPublic = true and p.last_name = :type', [provider: provider, type: supportProps.rdType.getI10n("value")])
        Person personInstance
        if(!personCheck) {
            personInstance = new Person(isPublic: true, last_name: supportProps.rdType.getI10n("value"))
            if(!personInstance.save()) {
                throw new SyncException("Error on setting up contact for ${provider}, concerning person instance: ${personInstance.getErrors().getAllErrors().toListString()}")
            }
        }
        else personInstance = personCheck[0]
        PersonRole personRole = PersonRole.findByPrsAndProviderAndFunctionType(personInstance, provider, supportProps.rdType)
        if(!personRole) {
            personRole = new PersonRole(prs: personInstance, provider: provider, functionType: supportProps.rdType)
            if(!personRole.save()) {
                throw new SyncException("Error on setting contact for ${provider}, concerning person role: ${personRole.getErrors().getAllErrors().toListString()}")
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
            throw new SyncException("Error on setting contact for ${provider}, concerning contact: ${contact.getErrors().getAllErrors().toListString()}")
        }
    }

    /**
     * Updates a contact for a given {@link Vendor}; overrides an eventually created one and creates if it does not exist
     * @param org the {@link Vendor} to which the given support address should be created/updated
     * @param supportProps the configuration {@link Map} containing the address properties
     * @throws SyncException
     */
    void createOrUpdateSupport(Vendor vendor, Map<String, Object> supportProps) throws SyncException {
        List<Person> personCheck = Person.executeQuery('select p from PersonRole pr join pr.prs p where p.tenant = null and pr.vendor = :vendor and p.isPublic = true and p.last_name = :type', [vendor: vendor, type: supportProps.rdType.getI10n("value")])
        Person personInstance
        if(!personCheck) {
            personInstance = new Person(isPublic: true, last_name: supportProps.rdType.getI10n("value"))
            if(!personInstance.save()) {
                throw new SyncException("Error on setting up contact for ${vendor}, concerning person instance: ${personInstance.getErrors().getAllErrors().toListString()}")
            }
        }
        else personInstance = personCheck[0]
        PersonRole personRole = PersonRole.findByPrsAndVendorAndFunctionType(personInstance, vendor, supportProps.rdType)
        if(!personRole) {
            personRole = new PersonRole(prs: personInstance, vendor: vendor, functionType: supportProps.rdType)
            if(!personRole.save()) {
                throw new SyncException("Error on setting contact for ${vendor}, concerning person role: ${personRole.getErrors().getAllErrors().toListString()}")
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
            throw new SyncException("Error on setting contact for ${vendor}, concerning contact: ${contact.getErrors().getAllErrors().toListString()}")
        }
        //log.debug(contact.getProperties().toMapString())
    }

    /**
     * Updates a {@link Platform} with the given parameters. If it does not exist, it will be created
     *
     * @param platformInput the platform record, either a UUID or a JSON object
     * @return the updated platform record
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
            else if(!(platformRecord.status in [Constants.PERMANENTLY_DELETED, 'Removed'])){
                platform = new Platform(name: platformRecord.name, gokbId: platformRecord.uuid)
            }
            //trigger if both records exist or if we:kb record is deleted but LAS:eR is not
            if(platform) {
                platform.status = platformRecord.status == Constants.PERMANENTLY_DELETED ? RDStore.PLATFORM_STATUS_REMOVED : RefdataValue.getByValueAndCategory(platformRecord.status, RDConstants.PLATFORM_STATUS)
                platform.normname = platformRecord.name.toLowerCase()
                if(platformRecord.primaryUrl)
                    platform.primaryUrl = new URL(platformRecord.primaryUrl)
                platform.accessPlatform = platformRecord.accessPlatform ? RefdataValue.getByValueAndCategory(platformRecord.accessPlatform, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.viewerForPdf = platformRecord.viewerForPdf ? RefdataValue.getByValueAndCategory(platformRecord.viewerForPdf, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.viewerForEpub = platformRecord.viewerForEpub ? RefdataValue.getByValueAndCategory(platformRecord.viewerForEpub, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.playerForAudio = platformRecord.playerForAudio ? RefdataValue.getByValueAndCategory(platformRecord.playerForAudio, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.playerForVideo = platformRecord.playerForVideo ? RefdataValue.getByValueAndCategory(platformRecord.playerForVideo, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessEPub = platformRecord.accessEPub ? RefdataValue.getByValueAndCategory(platformRecord.accessEPub, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                //currently no input at we:kb
                platform.onixMetadata = platformRecord.onixMetadata ? RefdataValue.getByValueAndCategory(platformRecord.onixMetadata, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessPdf = platformRecord.accessPdf ? RefdataValue.getByValueAndCategory(platformRecord.accessPdf, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessAudio = platformRecord.accessAudio ? RefdataValue.getByValueAndCategory(platformRecord.accessAudio, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessVideo = platformRecord.accessVideo ? RefdataValue.getByValueAndCategory(platformRecord.accessVideo, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessDatabase = platformRecord.accessDatabase ? RefdataValue.getByValueAndCategory(platformRecord.accessDatabase, RDConstants.ACCESSIBILITY_COMPLIANCE) : null
                platform.accessibilityStatementAvailable = platformRecord.accessibilityStatementAvailable ? RefdataValue.getByValueAndCategory(platformRecord.accessibilityStatementAvailable, RDConstants.Y_N) : null
                platform.accessibilityStatementUrl = platformRecord.accessibilityStatementUrl
                if(platformRecord.providerUuid) {
                    Provider provider = Provider.findByGokbId(platformRecord.providerUuid)
                    if(!provider) {
                        Map<String, Object> providerData = fetchRecordJSON(false,[uuid: platformRecord.providerUuid])
                        if(providerData && !providerData.error)
                            provider = createOrUpdateProvider(providerData)
                        else if(providerData && providerData.error == 404) {
                            throw new SyncException("we:kb server is currently down")
                        }
                        else {
                            throw new SyncException("Provider loading failed for ${platformRecord.providerUuid}")
                        }
                    }
                    platform.provider = provider
                }
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
                    Map<String, Object> packagesOfPlatform = fetchRecordJSON(false, [componentType: 'Package', platformUuid: platformRecord.uuid])
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
     * Updates the given record with the data of the updated one. The new title data is automatically handed to the
     * issue entitlements derived from the title instances of the sales unit
     * @param tippA the existing title record (in the app)
     * @param tippB the updated title record (ex we:kb)
     */
    void processTippDiffs(TitleInstancePackagePlatform tippA, Map tippB) {
        //ex updatedTippClosure / tippUnchangedClosure
        //tippA may be deleted; it occurred that deleted titles have been reactivated - whilst deactivation (tippB deleted/removed) needs a different handler!
        //else if(tippA.status != RDStore.TIPP_STATUS_REMOVED && !(status in [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED])) {
            //process central differences which are without effect to issue entitlements
            tippA.titleType = tippB.titleType
            tippA.name = tippB.name
            tippA.sortname = tippB.sortname ?: escapeService.generateSortTitle(tippB.name)
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
            //rare case, but occurred ...
            if(tippA.pkg.gokbId != tippB.packageUUID) {
                log.warn("Package UUID mismatch! Assign title to correct package ...")
                Package correct = Package.findByGokbId(tippB.packageUUID)
                if(correct) {
                    tippA.pkg = correct
                }
            }
            if(!tippA.save())
                throw new SyncException("Error on updating base title data: ${tippA.getErrors().getAllErrors().toListString()}")
            //these queries have to be observed very closely. They may first cause an extreme bottleneck (the underlying query may have many Sequence Scans), then they direct the issue holdings
            if(oldStatus != newStatus) {
                int updateCount = IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :newStatus, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :newStatus and ie.status != :removed', [now: new Date(), tipp: tippA, newStatus: newStatus, removed: RDStore.TIPP_STATUS_REMOVED])
                log.debug("status updated for ${tippA.gokbId}: ${oldStatus} to ${newStatus}, concerned are ${updateCount} entitlements")
                if(newStatus == RDStore.TIPP_STATUS_CURRENT) {
                    IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription s where ie.tipp = :title and ie.status in (:considered) and s.hasPerpetualAccess = true', [title: tippA, considered: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED]]).each { IssueEntitlement ie ->
                        ie.perpetualAccessBySub = ie.subscription
                        Org owner = ie.subscription.getSubscriberRespConsortia()
                        PermanentTitle perm = PermanentTitle.findByOwnerAndTipp(owner, ie.tipp)
                        if(!perm) {
                            perm = new PermanentTitle(owner: owner, tipp: ie.tipp, subscription: ie.subscription, issueEntitlement: ie)
                            perm.save()
                        }
                        ie.save()
                    }
                }
            }
            /*
            not provided by we:kb structure
            if(tippB.titlePublishers) {
                if(tippA.publishers) {
                    OrgRole.executeUpdate('delete from OrgRole oo where oo.tipp = :tippA',[tippA:tippA])
                }
                tippB.titlePublishers.each { publisher ->
                    lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid], tippA)
                }
            }
            */
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
    }

    /**
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
                sortname: tippData.sortname ?: escapeService.generateSortTitle(tippData.name),
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
            /*
            structure not provided in we:kb
            tippData.titlePublishers.each { publisher ->
                lookupOrCreateTitlePublisher([name: publisher.name, gokbId: publisher.uuid], newTIPP)
            }
            */
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
     * Fetches a JSON record from the we:kb API endpoint which has been defined in the {@link GlobalRecordSource} being used in the synchronisation process
     * @param useScroll use the scroll endpoint for huge data loads?
     * @param queryParams the parameter {@link Map} to be used for the query
     * @return a JSON {@link Map} containing the query result
     * @throws SyncException
     */
    Map<String,Object> fetchRecordJSON(boolean useScroll, Map<String,Object> queryParams) throws SyncException {
        BasicHttpClient http
        String uri = source.getUri() + '/'
        HttpClientConfiguration config = new DefaultHttpClientConfiguration()
        config.readTimeout = Duration.ofMinutes(5)
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
            queryParams.status = ["Current","Expected","Retired","Deleted",Constants.PERMANENTLY_DELETED,"Removed"]
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

    /**
     * Retrieves the title keys from the we:kb knowledge base which have been permanently deleted there.
     * If submitted, deletions from a certain time only are being regarded
     * @param changedFrom the time from when deletions should be fetched
     * @return a {@link Set} of UUIDs pointing to deleted titles
     */
    Set<String> getPermanentlyDeletedTitles(String changedFrom = null) {
        Set<String> result = []
        Map<String, Object> queryParams = [componentType: 'deletedkbcomponent', status: Constants.PERMANENTLY_DELETED, max: MAX_TIPP_COUNT_PER_PAGE, sort: 'lastUpdated']
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
        tippStatus.put(Constants.PERMANENTLY_DELETED,RDStore.TIPP_STATUS_REMOVED)
        tippStatus.put(RDStore.TIPP_STATUS_RETIRED.value,RDStore.TIPP_STATUS_RETIRED)
        tippStatus.put(RDStore.TIPP_STATUS_EXPECTED.value,RDStore.TIPP_STATUS_EXPECTED)
        tippStatus.put(RDStore.TIPP_STATUS_REMOVED.value,RDStore.TIPP_STATUS_REMOVED)
        contactTypes.put(RDStore.PRS_FUNC_INVOICING_CONTACT.value,RDStore.PRS_FUNC_INVOICING_CONTACT)
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
        packageStatus.put(Constants.PERMANENTLY_DELETED, RDStore.PACKAGE_STATUS_REMOVED)
        // ERMS-6224 - removed org.status
//        orgStatus.put(RDStore.ORG_STATUS_CURRENT.value,RDStore.ORG_STATUS_CURRENT)
//        orgStatus.put(RDStore.ORG_STATUS_DELETED.value,RDStore.ORG_STATUS_DELETED)
//        orgStatus.put(Constants.PERMANENTLY_DELETED,RDStore.ORG_STATUS_REMOVED)
//        orgStatus.put(RDStore.ORG_STATUS_REMOVED.value,RDStore.ORG_STATUS_REMOVED)
//        orgStatus.put(RDStore.ORG_STATUS_RETIRED.value,RDStore.ORG_STATUS_RETIRED)
        RefdataCategory.getAllRefdataValues(RDConstants.CURRENCY).each { RefdataValue rdv ->
            currency.put(rdv.value, rdv)
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
