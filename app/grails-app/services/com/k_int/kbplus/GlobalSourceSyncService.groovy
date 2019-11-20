package com.k_int.kbplus

import de.laser.SystemEvent
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
            SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
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
                            Package pkg = Package.createOrUpdate(r.metadata.gokb.package, source)
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

    void processPackage(NodeChildren packageData) {
        Map<String,Object> updatedRecord = [:]
        List<Map<String,Object>> identifiers = []
        packageData.identifiers.each { id ->
            identifiers << [namespace: id.'@namespace'.text(), value: id.'@value'.text()]
        }
        updatedRecord.identifiers = identifiers
        List<Map<String,Object>> tipps = []
        packageData.TIPPs.TIPP.eachWithIndex { tipp, int ctr ->
            log.info("Processing tipp ${ctr++} from package ${updatedRecord.id} - ${updatedRecord.name} (source:${source.uri})")
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
                    platformUUID: tipp.platform.'@uuid'.text(),
                    platformPrimaryUrl: tipp.platform.primaryUrl.text(),
                    coverages: [],
                    url: tipp.url.text(),
                    identifiers: [],
                    id: tipp.'@id'.text(),
                    uuid: tipp.'@uuid'.text(),
                    accessStart : tipp.access.'@start'.text() ? xmlDateFormat.parse(tipp.access.'@start'.text()) : null,
                    accessEnd   : tipp.access.'@end'.text() ? xmlDateFormat.parse(tipp.access.'@end'.text()) : null,
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
    }

}
