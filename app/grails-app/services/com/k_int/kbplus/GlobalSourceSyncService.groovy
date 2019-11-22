package com.k_int.kbplus

import de.laser.SystemEvent
import de.laser.domain.TIPPCoverage
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.HTTPBuilder
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.GET

/**
 * Implements the synchronisation workflow according to https://dienst-wiki.hbz-nrw.de/display/GDI/GOKB+Sync+mit+LASER
 */
class GlobalSourceSyncService extends AbstractLockableService {

    ExecutorService executorService

    boolean running = false
    SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

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

    void doSync() {
        running = true
        //TODO [ticket=1819] please ask whether in future, we may have more than one source (i.e. GlobalRecordSources)
        //for the moment, we rely upon being only one source per LAS:eR instance.
        List<GlobalRecordSource> jobs = GlobalRecordSource.findAll()
        GlobalRecordSource source = jobs.first()
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
                http.request(GET,XML) {
                    Map queryParams = [verb:'ListRecords']
                    if(resumption)
                        queryParams.resumptionToken = resumption
                    else {
                        String fromParam = oldDate ? xmlDateFormat.format(oldDate) : ''
                        queryParams.metadataPrefix = source.fullPrefix
                        queryParams.from = fromParam
                    }
                    uri.query = queryParams

                    response.success = { resp, xml ->
                        log.info(resp.statusLine)
                        xml.'ListRecords'.'record'.each { r ->
                            //continue processing here, original code jumps back to GlobalSourceSyncService
                            log.info("got OAI record ${r.header.identifier} datestamp: ${r.header.datestamp} job: ${source.id} url: ${source.uri}")
                            String recUUID = r.header.uuid.text() ?: '0'
                            String recIdentifier = r.header.identifier.text()
                            String recordTimestamp = xmlDateFormat.parse(r.header.datestamp.text())
                            //leave out GlobalRecordInfo update, no need to reflect it twice since we keep the package structure internally
                            //jump to packageReconcile which includes packageConv - check if there is a package, otherwise, update package data
                            Package pkg = createOrUpdate(r.metadata.gokb.package, source)
                        }
                        if(xml.'ListRecords'.'resumptionToken'.text()) {
                            resumption = xml.'ListRecords'.'resumptionToken'.text()
                            log.info("Continue with next iteration, token: ${resumption}")
                        }
                        else
                            more = false
                    }

                    response.failure = { resp ->
                        log.error("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}. Set more=false")
                        more = false
                    }
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


    Package createOrUpdate(NodeChildren packageData, GlobalRecordSource grs) {
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
            HTTPBuilder http2 = new HTTPBuilder(grs.uri.replaceAll("packages","orgs"))
            http2.request(GET,XML) {
                uri.query = [verb:'getRecord',metadataPrefix:grs.fullPrefix,identifier:providerUUID]
                response.success { resp, xml ->
                    log.info(resp.statusLine)
                    NodeChildren metadata = xml.'GetRecord'.record.metadata
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
                }
                response.failure = { resp ->
                    println "unexpected error on fetching provider data: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}."
                }
            }
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
        log.info("Rec conversion for package returns object with title ${result.parsed_rec.title} and ${result.parsed_rec.tipps?.size()} tipps")
        Package result = findByGokbId(packageData.'@uuid'.text())
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
                    RefdataValue status = RefdataValue.getByValueAndCategory(tippB.status,RefdataCategory.TIPP_STATUS)
                    if(status == RDStore.TIPP_DELETED) {
                        log.info("TIPP with UUID ${tippA.gokbId} has been deleted from package ${result.gokbId}")
                        tippA.status = status
                        tippsToNotify << [event:"delete",target:tippA]
                    }
                    else {
                        List<Map<String,Object>> diffs = getTippDiff(tippA,tippB) //includes also changes in coverage statement set
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
                                    tippA[diff.field] = tippB[diff.field]
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

    void createOrUpdatePackageProvider(String providerUUID, String packageUUID, Package pkg) {
        List<OrgRole> providerRoleCheck = OrgRole.executeQuery("select oo from OrgRole oo where oo.org.globalUID = :providerUUID and oo.pkg.gokbId = :gokbUUID",[providerUUID:providerUUID,gokbUUID:packageUUID])
        if(!providerRoleCheck) {
            OrgRole providerRole = new OrgRole(org:Org.findByGlobalUID(providerUUID),pkg:pkg,roleType: RDStore.OT_PROVIDER)
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
        if(coverageDiffs)
            result.add(coverageDiffs)

        if (tippa.accessStartDate != tippb.accessStartDate) {
            result.add([field: 'accessStartDate', newValue: tippb.accessStartDate, oldValue: tippa.accessStartDate])
        }

        if (tippa.accessEndDate != tippb.accessEndDate) {
            result.add([field: 'accessEndDate', newValue: tippb.accessEndDate, oldValue: tippa.accessEndDate])
        }

        if(tippa.status != tippb.status) {
            result.add([field: 'status', newValue: tippb.status, oldValue: tippa.status])
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
     *
     * @param covListA - the old coverage statement
     * @param covListB - the new coverage statement
     * @return a {@link Map} reflecting the differences between the coverage statements
     */
    Map<String,Object> getCoverageDiffs(Set<TIPPCoverage> covListA,List<Map<String,Object>> covListB, TitleInstancePackagePlatform tippBase) {
        Set<Map<String, Object>> covDiffs = []
        covListA.each { covA ->
            Map<String, Object> equivalentCoverageEntry
            //several attempts ... take dates! Where are the unique identifiers when we REALLY need them??!!
            //here is the culprit
            for(def k: covA) {
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

}
