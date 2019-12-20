package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import de.laser.DeletionService
import com.k_int.kbplus.GlobalRecordSource
import com.k_int.kbplus.GlobalSourceSyncService
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.domain.TIPPCoverage
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class YodaService {

    GrailsApplication grailsApplication
    def sessionRegistry = Holders.grailsApplication.mainContext.getBean('sessionRegistry')
    def contextService = Holders.grailsApplication.mainContext.getBean('contextService')
    GlobalSourceSyncService globalSourceSyncService
    DeletionService deletionService

    // gsp:
    // grailsApplication.mainContext.getBean("yodaService")
    // <g:set var="yodaService" bean="yodaService"/>

    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || grailsApplication.config.showDebugInfo )
    }

    int getNumberOfActiveUsers() {
        int count = 0

        sessionRegistry.getAllPrincipals().each { user ->
            List lastAccessTimes = []

            sessionRegistry.getAllSessions(user, false).each { userSession ->
                if (user.username == contextService.getUser()?.username) {
                    userSession.refreshLastRequest()
                }
                lastAccessTimes << userSession.getLastRequest().getTime()
            }
            if (lastAccessTimes.max() > System.currentTimeMillis() - (1000 * 600)) { // 10 minutes
                count++
            }
        }
        count
    }

    Map<String,Object> processDeletedTIPPs() {
        globalSourceSyncService.cleanUpGorm()
        //merge duplicate tipps
        List<String,Integer> duplicateTIPPRows = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId,count(tipp.gokbId) from TitleInstancePackagePlatform tipp group by tipp.gokbId having count(tipp.gokbId) > 1')
        List<String> duplicateTIPPKeys = []
        List<Long> excludes = []
        List<Map<String,Object>> mergingTIPPs = []
        List<Map<String,Object>> reports = []
        duplicateTIPPRows.eachWithIndex { row, int ctr ->
            log.debug("Processing entry ${ctr}. TIPP UUID ${row[0]} occurs ${row[1]} times in DB. Merging!")
            duplicateTIPPKeys << row[0]
            TitleInstancePackagePlatform mergeTarget = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(row[0], RDStore.TIPP_DELETED)
            if(!mergeTarget) {
                log.debug("no equivalent found, taking first ...")
                mergeTarget = TitleInstancePackagePlatform.findByGokbId(row[0])
            }
            excludes << mergeTarget.id
            log.debug("merge target with LAS:eR object ${mergeTarget} located")
            List<Long> iesToMerge = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.tipp.gokbId = :gokbId and ie.tipp != :mergeTarget',[gokbId:row[0], mergeTarget:mergeTarget])
            if(iesToMerge) {
                log.debug("found IEs to merge: ${iesToMerge}")
                mergingTIPPs << [mergeTarget:mergeTarget.id,iesToMerge:iesToMerge]
            }
        }
        globalSourceSyncService.cleanUpGorm()
        Map<String,RefdataValue> refdatas = [:]
        RefdataCategory.getAllRefdataValues(RefdataCategory.TIPP_STATUS).each { tippStatus ->
            refdatas[tippStatus.value] = tippStatus
        }
        //get to deleted tipps
        List<IssueEntitlement> allIE = IssueEntitlement.findAll()
        Map<Long,List<IssueEntitlement>> tippIEMap = [:]
        allIE.each { ie ->
            List<IssueEntitlement> tippIEs = tippIEMap.get(ie.tipp)
            if(!tippIEs)
                tippIEs = []
            tippIEs << ie
            tippIEMap.put(ie.tipp.id,tippIEs)
        }
        List<TitleInstancePackagePlatform> deletedTIPPs = TitleInstancePackagePlatform.findAllByStatus(RDStore.TIPP_DELETED,[sort:'pkg.name',order:'asc'])
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        HTTPBuilder http = new HTTPBuilder(grs.uri)
        Map<String, NodeChildren> oaiRecords = [:]
        List<Map<TitleInstancePackagePlatform,Map<String,Object>>> deletedWithoutGOKbRecord = [], deletedWithGOKbRecord = []
        deletedTIPPs.each { delTIPP ->
            log.debug("now processing entry #${delTIPP.id} ${delTIPP.gokbId} of package ${delTIPP.pkg} with uuid ${delTIPP.pkg.gokbId}")
            NodeChildren oaiRecord = oaiRecords.get(delTIPP.pkg.gokbId)
            if(!oaiRecord) {
                def packageRecord = http.get(path:'packages',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.pkg.gokbId],contentType:'xml') { resp, xml ->
                    GPathResult record = new XmlSlurper().parseText(xml.text)
                    if(record.error.@code == 'idDoesNotExist')
                        return "package ${delTIPP.pkg.gokbId} inexistent"
                    else return record.'GetRecord'.record.metadata.gokb.package
                }
                if(packageRecord instanceof GString)
                    log.debug(packageRecord)
                else if(packageRecord instanceof NodeChildren) {
                    oaiRecords[delTIPP.pkg.gokbId] = packageRecord
                    oaiRecord = packageRecord
                }
            }
            def gokbTIPP = oaiRecord.'**'.find { tipp ->
                tipp.@uuid == delTIPP.gokbId && tipp.status.text() != RDStore.TIPP_DELETED.value
            }
            if(!gokbTIPP) {
                /*
                case: there is a TIPP in LAS:eR with an invalid GOKb UUID, thus no record.
                If we have IssueEntitlements depending on it: check subscription state
                    if deleted: delete subscription, delete IE, delete TIPP
                    else check if there is an equivalent GOKb record -> load package, check if there is an equivalent TitleInstance-Package-Platform entry (so a TIPP entry!)
                    if so: remap to new UUID
                    else show subscriber
                 */
                List<Map<String,Object>> issueEntitlements = []
                tippIEMap.get(delTIPP.id).each { ie ->
                    Map<String,Object> ieDetails = [ie:ie]
                    if(ie.subscription.status == RDStore.TIPP_DELETED) {
                        log.debug("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                        ieDetails.action = "deleteCascade"
                    }
                    else {
                        log.debug("${ie.subscription} is current, check in GOKb if equivalent TIPP exists ...")
                        def titleRecord = http.get(path:'titles',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.title.gokbId],contentType:'xml') { resp, xml ->
                            GPathResult record = new XmlSlurper().parseText(xml.text)
                            if(record.error.@code == 'idDoesNotExist')
                                return "title ${delTIPP.title.gokbId} inexistent, name is ${delTIPP.title.title}"
                            else return record.'GetRecord'.record.metadata.gokb.title
                        }
                        Map<String,Object> report = [subscriber:ie.subscription.getSubscriber().shortname,subscription:ie.subscription.name,title:delTIPP.title.title,package:delTIPP.pkg.name]
                        if(ie.subscription.getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE,TemplateSupport.CALCULATED_TYPE_PARTICIPATION]) {
                            report.consortium = ie.subscription.getConsortia().shortname
                        }
                        else {
                            report.consortium = ""
                        }
                        if(titleRecord instanceof GString){
                            log.debug(titleRecord)
                            ieDetails.action = "report"
                            ieDetails.report = report+[cause:"Titel existiert nicht"]
                            log.debug(ieDetails.report)
                        }
                        else if(titleRecord instanceof NodeChildren) {
                            log.debug("title instance ${delTIPP.title.gokbId} found, reconcile UUID by retrieving package and platform")
                            def equivalentTIPP = titleRecord.TIPPs.TIPP.find { node ->
                                node.package.name == delTIPP.pkg.name && node.platform.name == delTIPP.platform.name
                            }
                            if(equivalentTIPP) {
                                log.debug("TIPP found: should remapped to UUID ${equivalentTIPP.@uuid}")
                                ieDetails.action = "remap"
                                ieDetails.target = equivalentTIPP.@uuid
                            }
                            else {
                                ieDetails.action = "report"
                                ieDetails.report = report+[cause:"Kein äquivalentes TIPP gefunden"]
                                log.debug(ieDetails.report)
                            }
                        }
                    }
                    issueEntitlements << ieDetails
                }
                Map<TitleInstancePackagePlatform,Map<String,Object>> result = [:]
                result[delTIPP] = issueEntitlements
                deletedWithoutGOKbRecord << result
            }
            else {
                /*
                    case: there is a TIPP marked deleted with GOKb entry
                    do further checks as follows:
                    set TIPP and IssueEntitlement (by pending change) to that status
                    otherwise do nothing
                 */
                Map<TitleInstancePackagePlatform,Map<String,Object>> result = [:]
                RefdataValue currTippStatus = refdatas[gokbTIPP.status.text()]
                Map<String,Object> tippDetails = [issueEntitlements: tippIEMap.get(delTIPP.id), action: 'updateStatus', status: currTippStatus]
                result[delTIPP] = tippDetails
                deletedWithGOKbRecord << result
            }
        }
        [deletedWithoutGOKbRecord:deletedWithoutGOKbRecord,deletedWithGOKbRecord:deletedWithGOKbRecord,mergingTIPPs:mergingTIPPs,duplicateTIPPKeys:duplicateTIPPKeys,excludes:excludes]
    }

}
