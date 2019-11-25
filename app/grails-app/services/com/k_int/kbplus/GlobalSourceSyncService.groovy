package com.k_int.kbplus

import de.laser.SystemEvent
import de.laser.domain.TIPPCoverage
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.hibernate.SessionFactory
import org.hibernate.classic.Session

import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

/**
 * Implements the synchronisation workflow according to https://dienst-wiki.hbz-nrw.de/display/GDI/GOKB+Sync+mit+LASER
 */
class GlobalSourceSyncService extends AbstractLockableService {

    SessionFactory sessionFactory
    ExecutorService executorService
    def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    GlobalRecordSource source

    boolean running = false
    SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /**
     * This is the entry point for triggering the sync workflow. To ensure locking, a flag will be set when a process is already running
     * @return a flag whether a process is already running
     */
    boolean startSync() {
        if(!running) {
            def future = executorService.submit({ doSync() } as Callable)
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
        //TODO [ticket=1819] please ask whether in future, we may have more than one source (i.e. GlobalRecordSources)
        //for the moment, we rely upon being only one source per LAS:eR instance.
        List<GlobalRecordSource> jobs = GlobalRecordSource.findAll()
        source = jobs.first()
        /*
        in case we build upon multiple sources, move the following block into this loop and remove comment
        jobs.each { source ->

        }
        */
        try {
            SystemEvent.createEvent('GSSS_OAI_START',['jobId':source.id])
            Thread.currentThread().setName("GlobalDataSync")
            Date oldDate = source.haveUpTo
            Long maxTimestamp = 0
            log.info("getting records from job #${source.id} with uri ${source.uri} since ${oldDate} using ${source.fullPrefix}")
            //merging from OaiClient
            log.info("getting latest changes ...")
            HTTPBuilder http = new HTTPBuilder(source.uri)
            boolean more = true
            log.info("attempt get ...")
            String resumption = null
            // perform GET request, expection XML response data
            while(more) {
                log.info("in loop, making request ...")
                Map<String,String> queryParams = [verb:'ListRecords',metadataPrefix:'gokb',resumptionToken:resumption]
                if(resumption) {
                    queryParams.resumptionToken = resumption
                }
                else {
                    String fromParam = oldDate ? xmlDateFormat.format(oldDate) : ''
                    queryParams.metadataPrefix = source.fullPrefix
                    queryParams.from = fromParam
                }
                NodeChildren listRecords = fetchRecord(source.uri,'packages',queryParams)
                if(listRecords) {
                    listRecords.record.each { NodeChild r ->
                        //continue processing here, original code jumps back to GlobalSourceSyncService
                        log.info("got OAI record ${r.header.identifier} datestamp: ${r.header.datestamp} job: ${source.id} url: ${source.uri}")
                        String recUUID = r.header.uuid.text() ?: '0'
                        String recIdentifier = r.header.identifier.text()
                        String recordTimestamp = xmlDateFormat.parse(r.header.datestamp.text())
                        //leave out GlobalRecordInfo update, no need to reflect it twice since we keep the package structure internally
                        //jump to packageReconcile which includes packageConv - check if there is a package, otherwise, update package data
                        Package pkg = createOrUpdatePackage(r.metadata.gokb.package)
                    }
                    if(listRecords.resumptionToken) {
                        resumption = listRecords.resumptionToken
                        log.info("Continue with next iteration, token: ${resumption}")
                        cleanUpGorm()
                    }
                    else
                        more = false
                }
                log.info("Endloop")
            }
            log.info("all OAI info fetched")
            log.info("sync job finished")
            SystemEvent.createEvent('GSSS_OAI_COMPLETE',['jobId',source.id])
        }
        catch (Exception e) {
            SystemEvent.createEvent('GSSS_OAI_ERROR',['jobId':source.id]).save()
            log.error("sync job has failed, please consult stacktrace as follows: ")
            e.printStackTrace()
            running = false
        }
    }

    /**
     * Looks up for a given OAI-PMH extract if a local record exists or not. If no {@link Package} record exists, it will be
     * created with the given remote record data, otherwise, the local record is going to be updated. The {@link TitleInstancePackagePlatform records}
     * in the {@link Package} will be checked for differences and if there are such, the according fields updated. Same counts for the {@link TIPPCoverage} records
     * in the {@link TitleInstancePackagePlatform}s. If {@link Subscription}s are linked to the {@link Package}, the {@link IssueEntitlement}s (just as their
     * {@link de.laser.domain.IssueEntitlementCoverage}s) are going to be notified; it is up to the respective subscription tenants to accept the changes or not.
     * Replaces the method GokbDiffEngine.diff and the onNewTipp, onUpdatedTipp and onUnchangedTipp closures
     *
     * @param packageData - A {@link NodeChildren} list containing a OAI-PMH record extract for a given package
     * @return
     */
    Package createOrUpdatePackage(NodeChildren packageData) {
        log.info('converting XML record into map and reconciling new package!')
        String title = packageData.title.text()
        String name = packageData.name.text()
        RefdataValue packageStatus = RefdataValue.getByValueAndCategory(packageData.status.text(), 'Package Status')
        RefdataValue packageScope = RefdataValue.getByValueAndCategory(packageData.scope.text(),RefdataCategory.PKG_SCOPE) //needed?
        RefdataValue packageListStatus = RefdataValue.getByValueAndCategory(packageData.listStatus.text(),RefdataCategory.PKG_LIST_STAT) //needed?
        RefdataValue breakable = RefdataValue.getByValueAndCategory(packageData.breakable.text(),RefdataCategory.PKG_BREAKABLE) //needed?
        RefdataValue consistent = RefdataValue.getByValueAndCategory(packageData.consistent.text(),RefdataCategory.PKG_CONSISTENT) //needed?
        RefdataValue fixed = RefdataValue.getByValueAndCategory(packageData.fixed.text(),RefdataCategory.PKG_FIXED) //needed?
        //result.global = packageData.global.text() needed? not used in packageReconcile
        //result.paymentType = packageData.paymentType.text() needed? not used in packageReconcile
        String providerUUID = packageData.nominalProvider.'@uuid'.text()
        if(providerUUID){
            lookupOrCreateProvider(providerUUID)
        }
        //ex packageConv, processing TIPPs - conversion necessary because package may be not existent in LAS:eR; then, no comparison is needed any more
        List<Map<String,Object>> tipps = []
        packageData.TIPPs.TIPP.eachWithIndex { tipp, int ctr ->
            String titleType = tipp.title.mediumByTypClass.text() ?: (tipp.title.type.text() ?: null)
            Map<String,Object> updatedTIPP = [
                    title: [
                            id: tipp.title.'@id'.text(),
                            name: tipp.title.name.text(),
                            identifiers: [],
                            status: tipp.title.status.text(),
                            gokbId: tipp.title.'@uuid'.text(), //impId needed?
                            titleType: titleType
                    ],
                    status: tipp.status.text(),
                    platformName: tipp.platform.name.text(),
                    platformUUID: tipp.platform.'@uuid'.text(),
                    platformPrimaryUrl: tipp.platform.primaryUrl.text(),
                    coverages: [],
                    hostPlatformURL: tipp.url.text(),
                    identifiers: [],
                    id: tipp.'@id'.text(),
                    uuid: tipp.'@uuid'.text(),
                    accessStartDate : tipp.access.'@start'.text() ? xmlDateFormat.parse(tipp.access.'@start'.text()) : null,
                    accessEndDate   : tipp.access.'@end'.text() ? xmlDateFormat.parse(tipp.access.'@end'.text()) : null,
                    medium      : tipp.medium.text()
            ]
            tipp.title.identifiers.identifier.each { id ->
                updatedTIPP.title.identifiers << [namespace: id.'@namespace'.text(), value: id.'@value'.text()]
            }
            updatedTIPP.title.identifiers.add([namespace: 'uri', value: tipp.title.'@id'.titleId])
            updatedTIPP.identifiers.add([namespace: 'uri', value: tipp.'@id'.tippId])
            tipp.coverage.each { cov ->
                updatedTIPP.coverages << [
                        startDate: cov.'@startDate'.text() ? xmlDateFormat.parse(cov.'@startDate'.text()) : null,
                        endDate: cov.'@endDate'.text() ? xmlDateFormat.parse(cov.'@endDate'.text()) : null,
                        startVolume: cov.'@startVolume'.text() ?: '',
                        endVolume: cov.'@endVolume'.text() ?: '',
                        startIssue: cov.'@startIssue'.text() ?: '',
                        endIssue: cov.'@endIssue'.text() ?: '',
                        coverageDepth: cov.'@coverageDepth'.text() ?: '',
                        coverageNote: cov.'@coverageNote'.text() ?: '',
                        embargo: cov.'@embargo'.text() ?: ''
                ]
            }
            updatedTIPP.coverages = updatedTIPP.coverages.toSorted { a, b -> a.startDate <=> b.startDate }
            tipps << updatedTIPP
        }
        log.info("Rec conversion for package returns object with title ${title} and ${tipps.size()} tipps")
        Package result = Package.findByGokbId(packageData.'@uuid'.text())
        if(result) {
            //local package exists -> update closure, build up GokbDiffEngine and the horrendous closures
            log.info("package successfully found, processing LAS:eR id #${result.id}, with GOKb id ${result.gokbId}")
            result.name = name
            result.packageStatus = packageStatus
            result.packageScope = packageScope //needed?
            result.packageListStatus = packageListStatus //needed?
            result.breakable = breakable //needed?
            result.consistent = consistent //needed?
            result.fixed = fixed //needed?
            if(result.save()) {
                if(providerUUID) {
                    createOrUpdatePackageProvider(providerUUID,packageData.'@uuid'.text(),result)
                }
                List<Map<String,Object>> tippsToNotify = []
                tipps.each { Map<String, Object> tippB ->
                    TitleInstancePackagePlatform tippA = result.tipps.find { TitleInstancePackagePlatform b -> b.gokbId == tippB.uuid } //we have to consider here TIPPs, too, which were deleted but have been reactivated
                    if(tippA) {
                        //ex updatedTippClosure / tippUnchangedClosure
                        RefdataValue status = RefdataValue.getByValueAndCategory(tippB.status,RefdataCategory.TIPP_STATUS)
                        if(status == RDStore.TIPP_DELETED) {
                            log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${result.gokbId}")
                            tippA.status = status
                            tippsToNotify << [event:"delete",target:tippA]
                        }
                        else {
                            Set<Map<String,Object>> diffs = getTippDiff(tippA,tippB) //includes also changes in coverage statement set
                            if(diffs) {
                                log.info("Got tipp diffs: ${diffs}")
                                //process actual diffs
                                diffs.each { diff ->
                                    if(diff.field == 'coverage') {
                                        diff.covDiffs.each { covDiff ->
                                            switch(covDiff.event) {
                                                case 'added':
                                                    if(!covDiff.target.save())
                                                        log.error(covDiff.target.errors)
                                                    break
                                                case 'deleted': covDiff.target.delete()
                                                    break
                                                case 'updated': covDiff.target[covDiff.field] = covDiff.newValue
                                                    if(!covDiff.target.save())
                                                        log.error(covDiff.target.errors)
                                                    break
                                            }
                                        }
                                    }
                                    else {
                                        switch(diff.fieldType) {
                                            case RefdataValue.class.name:
                                                if(diff.refdataCategory)
                                                    tippA[diff.field] = RefdataValue.getByValueAndCategory(tippB[diff.field],diff.refdataCategory)
                                                else log.error("RefdataCategory missing!")
                                                break
                                            default: tippA[diff.field] = tippB[diff.field]
                                                break
                                        }
                                    }
                                }
                                if(tippA.save())
                                    tippsToNotify << [event:'update',target:tippA,diffs:diffs]
                                else
                                    log.error(tippA.errors)
                                //updatedTitleAfterPackageReconcile!
                            }
                        }
                    }
                    else {
                        //ex newTippClosure
                        TitleInstancePackagePlatform newTIPP = new TitleInstancePackagePlatform(
                                gokbId: tippB.uuid,
                                status: RefdataValue.getByValueAndCategory(tippB.status, RefdataCategory.TIPP_STATUS),
                                hostPlatformURL: tippB.url.text(),
                                accessStartDate: (Date) tippB.accessStartDate,
                                accessEndDate: (Date) tippB.accessEndDate
                        )
                        //updatedTitleAfterPackageReconcile!

                    }
                }
                //if everything went well, we should have here the list of tipps to notify ...
                log.debug(tippsToNotify)
            }
            else {
                log.error("Error on updating package: ${result.errors}")
            }
        }
        else {
            //local package does not exist -> insert new data
            log.info("creating new package ...")
            result = new Package(
                    gokbId: packageData.'@uuid'.text(), //impId needed?
                    name: name,
                    packageStatus: packageStatus,
                    packageScope: packageScope, //needed?
                    packageListStatus: packageListStatus, //needed?
                    breakable: breakable, //needed?
                    consistent: consistent, //needed?
                    fixed: fixed //needed?
            )
            if(result.save()) {
                if(providerUUID) {
                    createOrUpdatePackageProvider(providerUUID,packageData.'@uuid'.text(),result)
                }
            }
            else {
                log.error("Error on saving package: ${result.errors}")
            }
        }

        log.info("before processing identifiers")
        packageData.identifiers.each { id ->
            Identifier.construct([namespace: id.'@namespace'.text(), value: id.'@value'.text(), reference: result])
        }
        result
    }

    /**
     * Checks for a given UUID if a {@link TitleInstance} is existing in the database, if it does not exist, it will be created.
     * Replaces the former updatedTitleAfterPackageReconcile, titleConv and titleReconcile closure
     *
     * @param titleUUID - the GOKb UUID of the {@link TitleInstance} to create or update
     * @return the new or updated {@link TitleInstance}
     */
    TitleInstance createOrUpdateTitle(String titleUUID) {
        NodeChildren record = fetchRecord(source.uri,'titles',[verb:'getRecord',metadataPrefix:source.fullPrefix,identifier:titleUUID])
        if(record) {
            NodeChildren titleRecord = record.metadata.gokb.title
            log.info("title record loaded, converting XML record and reconciling title record ...")
            TitleInstance titleInstance = TitleInstance.findByGokbId(titleUUID)
            RefdataValue status = RefdataValue.getByValueAndCategory(RefdataCategory.TI_STATUS)
            if(titleInstance) {
                
            }
            else {
                titleInstance = new TitleInstance(gokbId: titleUUID)

            }
        }
    }

    /**
     * Was formerly in the {@link Org} domain class; deployed for better maintainability
     * Checks for a given UUID if the provider exists, otherwise, it will be created. The
     * default {@link Platform}s are set up or updated as well
     *
     * @param providerUUID - the GOKb UUID of the given provider {@link Org}
     * @return - the provider {@link Org}
     */
    Org lookupOrCreateProvider(providerUUID) {
        //Org.lookupOrCreate2 simplified
        Org provider = Org.findByGokbId(providerUUID)
        if(!provider) {
            provider = new Org(
                    name: packageData.nominalProvider.name.text(),
                    sector: RDStore.O_SECTOR_PUBLISHER,
                    type: [RDStore.OT_PROVIDER],
                    gokbId: providerUUID
            )
            if(!provider.save()) {
                log.error(provider.errors)
            }
        }
        NodeChildren metadata = fetchRecord(source.uri,'orgs',[verb:'getRecord',metadataPrefix:source.fullPrefix,identifier:providerUUID])
        if(metadata) {
            metadata.gokb.org.providedPlatforms.platform.each { plat ->
                //ex setOrUpdateProviderPlattform()
                log.info("checking provider with uuid ${providerUUID}")
                Platform platform = Platform.lookupOrCreatePlatform([name: plat.name.text(), gokbId: plat.'@uuid'.text(), primaryUrl: plat.primaryUrl.text()])
                if(platform.org != provider) {
                    platform.org = provider
                    platform.save()
                }
            }
        }
        provider
    }

    void createOrUpdatePackageProvider(String providerUUID, String packageUUID, Package pkg) {
        Org provider = lookupOrCreateProvider(providerUUID)
        List<OrgRole> providerRoleCheck = OrgRole.executeQuery("select oo from OrgRole oo where oo.org = :provider and oo.pkg.gokbId = :gokbUUID",[provider:provider,gokbUUID:packageUUID])
        if(!providerRoleCheck) {
            OrgRole providerRole = new OrgRole(org:provider,pkg:pkg,roleType: RDStore.OT_PROVIDER)
            if(!providerRole.save()) {
                log.error("Error on saving org role: ${providerRole.errors}")
            }
        }
    }

    void updateCoverageStatement(TIPPCoverage covA, Map<String,Object> covB) {
        covA.startDate = (Date) covB.startDate ?: null
        covA.startVolume = covB.startVolume
        covA.startIssue = covB.startIssue
        covA.endDate = (Date) covB.endDate ?: null
        covA.endVolume = covB.endVolume
        covA.endIssue = covB.endIssue
        covA.embargo = covB.embargo
        covA.coverageDepth = covB.coverageDepth
        covA.coverageNote = covB.coverageNote
        if (!covA.save())
            println("Error on saving coverage data: ${covA.getErrors()}")
    }

    /**
     * Compares two package entries against each other, retrieving the differences between both.
     * @param tippa - the old TIPP (as {@link TitleInstancePackagePlatform} which is already persisted)
     * @param tippb - the new TIPP (as unprocessed {@link Map})
     * @return a {@link Set} of {@link Map}s with the differences
     */
    Set<Map<String,Object>> getTippDiff(TitleInstancePackagePlatform tippa, Map<String,Object> tippb) {
        log.info("processing diffs; the respective GOKb UUIDs are: ${tippa.gokbId} (LAS:eR) vs. ${tippb.uuid} (remote)")
        Set<Map<String, Object>> result = []

        if (tippa.hostPlatformURL != tippb.hostPlatformURL) {
            result.add([field: 'hostPlatformURL', newValue: tippb.hostPlatformURL, oldValue: tippa.hostPlatformURL])
        }

        /* This is the boss enemy when refactoring coverage statements ... works so far, is going to be kept
        Expect ERMS-1607
        if ((tippa.coverage ?: '').toString().compareTo((tippb.coverage ?: '').toString()) != 0) {
            result.add([field: 'coverage', newValue: tippb.coverage, oldValue: tippa.coverage])
        }*/
        Map<String, Object> coverageDiffs = getCoverageDiffs(tippa.coverages,(List<Map<String,Object>>) tippb.coverages, tippa)
        if(coverageDiffs.isEmpty())
            result.add(coverageDiffs)

        if (tippa.accessStartDate != tippb.accessStartDate) {
            result.add([field: 'accessStartDate', newValue: tippb.accessStartDate, oldValue: tippa.accessStartDate])
        }

        if (tippa.accessEndDate != tippb.accessEndDate) {
            result.add([field: 'accessEndDate', newValue: tippb.accessEndDate, oldValue: tippa.accessEndDate])
        }

        if(tippa.status != RefdataValue.getByValueAndCategory(tippb.status,RefdataCategory.TIPP_STATUS)) {
            result.add([field: 'status', fieldType: RefdataValue.class.name, refdataCategory: RefdataCategory.TIPP_STATUS, newValue: tippb.status, oldValue: tippa.status])
        }

        /*
        does not make sense because that must be processed in the updatedTitleAfterPackageReconcile-closure
        if ((tippa?.title?.name ?: '').toString().compareTo((tippb?.title?.name ?: '').toString()) != 0) {
            result.add([field: 'titleName', newValue: tippb?.title?.name, oldValue: tippa?.title?.name])
        }
         */

        result
    }

    /**
     * Compares two coverage entries against each other, retrieving the differences between both.
     * @param covListA - the old coverage statements (an existing {@link Set} of {@link TIPPCoverage}s)
     * @param covListB - the new coverage statements (a {@link List} of unpersisted remote records, kept in {@link Map}s)
     * @return a {@link Map} reflecting the differences between the coverage statements
     */
    Map<String,Object> getCoverageDiffs(Set<TIPPCoverage> covListA,List<Map<String,Object>> covListB, TitleInstancePackagePlatform tippBase) {
        Set<Map<String, Object>> covDiffs = []
        covListA.each { covA ->
            Map<String, Object> equivalentCoverageEntry
            //several attempts ... take dates! Where are the unique identifiers when we REALLY need them??!!
            //here is the culprit
            for(def k: covA.properties.keySet()) {
                equivalentCoverageEntry = covListB.find { covB ->
                    covB[k] == covA[k]
                }
                if(equivalentCoverageEntry)
                    break
            }
            if(equivalentCoverageEntry) {
                TIPPCoverage.controlledProperties.each { cp ->
                    if(covA[cp] != equivalentCoverageEntry[cp]) {
                        covDiffs << [field: cp, event: 'updated', target: covA, oldValue: covA[cp], newValue: equivalentCoverageEntry[cp]]
                    }
                }
            }
            else {
                //there are coverage statements removed ...
                covDiffs << [event: 'deleted', target: covA]
            }
        }
        if(covListB.size() > covListA.size()) {
            //there are new coverage statements ...
            covListB.each { covB ->
                TIPPCoverage equivalentCoverageEntry
                for(def k: covB) {
                    equivalentCoverageEntry = covListA.find { covA ->
                        covA[k] == covB[k]
                    }
                    if(equivalentCoverageEntry)
                        break
                }
                if(!equivalentCoverageEntry) {
                    TIPPCoverage newStatement = new TIPPCoverage(
                            startDate: (Date) covB.startDate,
                            startVolume: covB.startVolume,
                            startIssue: covB.startIssue,
                            endDate: (Date) covB.endDate,
                            endVolume: covB.endVolume,
                            endIssue: covB.endIssue,
                            embargo: covB.embargo,
                            coverageDepth: covB.coverageDepth,
                            coverageNote: covB.coverageNote,
                            tipp: tippBase
                    )
                    covDiffs << [event: 'added', target: newStatement]
                }
            }
        }
        [field: 'coverage', covDiffs: covDiffs]
    }

    /**
     * Retrieves remote data with the given query parameters. Used to query a GOKb instance for changes since a given timestamp or to fetch remote package/provider data
     * Was formerly the OaiClient and OaiClientLaser classes
     *
     * @param url - the URL to query against
     * @param object - the object(s) about which records should be obtained. May be: {@link Package}, {@link TitleInstance} or {@link Org}
     * @param queryParams - parameters to pass along with the query
     * @return a {@link NodeChildren} containing the OAI-PMH conform XML extract of the given record
     */
    NodeChildren fetchRecord(String url, String object, Map<String,String> queryParams) {
        try {
            HTTPBuilder http = new HTTPBuilder(url)
            NodeChildren record = (NodeChildren) http.get(path:object,query:queryParams,contentType:'xml') { resp, xml ->
                GPathResult response = new XmlSlurper().parseText(xml.text)
                if(response[queryParams.verb] && response[queryParams.verb] instanceof NodeChildren) {
                    NodeChildren record = (NodeChildren) response[queryParams.verb]
                    record
                }
                else {
                    log.error('Request succeeded but result data invalid. Please do checks')
                    null
                }
            }
            record
        }
        catch(HttpResponseException e) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Flushes the session data to free up memory. Essential for bulk data operations like record syncing
     */
    void cleanUpGorm() {
        log.debug("Clean up GORM")

        Session session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

}
