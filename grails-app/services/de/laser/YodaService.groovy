package de.laser


import de.laser.exceptions.SyncException
import de.laser.helper.ConfigMapper
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.oap.OrgAccessPointLink
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChildren
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * This service handles bulk and cleanup operations, testing areas and debug information
 */
//@CompileStatic
//@Transactional
class YodaService {

    ContextService contextService
    ChangeNotificationService changeNotificationService
    DeletionService deletionService
    GlobalService globalService
    GlobalSourceSyncService globalSourceSyncService
    GokbService gokbService
    PackageService packageService

    /**
     * Checks whether debug information should be displayed
     * @return true if setting is enabled by config or the viewer has admin rights, false otherwise
     */
    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigMapper.getShowDebugInfo() )
    }

    /**
     * Copies missing medium values into the issue entitlements; values are being taken from the title the holding records have been derived
     */
    void fillIEMedium() {
        IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.medium = (select tipp.medium from TitleInstancePackagePlatform tipp where tipp = ie.tipp and tipp.medium != null) where ie.medium = null")
    }

    /**
     * Locates duplicate packages in the system
     * @return a map of packages duplicates, grouped by such with and such without titles
     */
    Map<String,Object> listDuplicatePackages() {
        List<Package> pkgDuplicates = Package.executeQuery('select pkg from Package pkg where pkg.gokbId in (select p.gokbId from Package p group by p.gokbId having count(p.gokbId) > 1)')
        pkgDuplicates.addAll(Package.findAllByGokbIdIsNullOrGokbIdLike(RDStore.GENERIC_NULL_VALUE.value))
        Map<String,List<Package>> result = [pkgDuplicates: pkgDuplicates]
        if(pkgDuplicates) {
            log.debug("located package duplicates")
            List<Package> pkgDupsWithTipps = Package.executeQuery('select distinct(tipp.pkg) from TitleInstancePackagePlatform tipp where tipp.pkg in (:pkg) and tipp.status != :deleted',[pkg:pkgDuplicates,deleted:RDStore.TIPP_STATUS_DELETED])
            List<Package> pkgDupsWithoutTipps = []
            pkgDuplicates.each { pkg ->
                if(!pkgDupsWithTipps.contains(pkg))
                    pkgDupsWithoutTipps << pkg
            }
            result.pkgDupsWithTipps = pkgDupsWithTipps
            result.pkgDupsWithoutTipps = pkgDupsWithoutTipps
        }
        result
    }

    /**
     * Removes the given list of packages
     * @param toDelete the list of package database identifiers which should be deleted
     */
    void executePackageCleanup(List<Long> toDelete) {
        toDelete.each { pkgId ->
            Package pkg = Package.get(pkgId)
            deletionService.deletePackage(pkg)
        }
    }

    /**
     * Checks the titles marked as deleted and verifies its holding state and we:kb equivalency state. It needs
     * refactoring because it uses the OAI endpoint to determine titles. The complex decision procedure to mark a record
     * as purgeable is explained along the code
     * @return a {@link Map} containing the concerned title records, grouped by their state
     */
    Map<String,Object> listDeletedTIPPs() {
        globalService.cleanUpGorm()
        //merge duplicate tipps
//        List<String,Integer> duplicateTIPPRows = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId,count(tipp.gokbId) from TitleInstancePackagePlatform tipp group by tipp.gokbId having count(tipp.gokbId) > 1')
        List duplicateTIPPRows = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId,count(tipp.gokbId) from TitleInstancePackagePlatform tipp group by tipp.gokbId having count(tipp.gokbId) > 1')
        List<String> duplicateTIPPKeys = []
        List<Long> excludes = []
        List<Map<String,Object>> mergingTIPPs = []
        duplicateTIPPRows.eachWithIndex { row, int ctr ->
            println("Processing entry ${ctr}. TIPP UUID ${row[0]} occurs ${row[1]} times in DB. Merging!")
            duplicateTIPPKeys << row[0]
            TitleInstancePackagePlatform mergeTarget = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(row[0], RDStore.TIPP_STATUS_DELETED)
            if(!mergeTarget) {
                println("no equivalent found, taking first ...")
                mergeTarget = TitleInstancePackagePlatform.findByGokbId(row[0])
            }
            excludes << mergeTarget.id
            println("merge target with LAS:eR object ${mergeTarget} located")
            List<Long> iesToMerge = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.tipp.gokbId = :gokbId and ie.tipp != :mergeTarget',[gokbId:row[0], mergeTarget:mergeTarget])
            if(iesToMerge) {
                println("found IEs to merge: ${iesToMerge}")
                mergingTIPPs << [mergeTarget:mergeTarget.id,iesToMerge:iesToMerge]
            }
        }
        Map<String,RefdataValue> refdatas = [:]
        RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS).each { tippStatus ->
            refdatas[tippStatus.value] = tippStatus
        }
        //get to deleted tipps
        globalService.cleanUpGorm()
        println("move to TIPPs marked as deleted")
        //aim is to exclude resp. update those which has been erroneously marked as deleted (duplicate etc.)
        List<TitleInstancePackagePlatform> deletedTIPPs = TitleInstancePackagePlatform.findAllByStatus(RDStore.TIPP_STATUS_DELETED,[sort:'pkg.name',order:'asc'])
        deletedTIPPs.addAll(TitleInstancePackagePlatform.findAllByGokbIdIsNull())
        println "deleted TIPPs located: ${deletedTIPPs.size()}"
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        HTTPBuilder http = new HTTPBuilder(grs.uri)
        Map<String, NodeChildren> oaiRecords = [:]
        List<Map<TitleInstancePackagePlatform,Map<String,Object>>> deletedWithoutGOKbRecord = []
        List<Map<String,Map<String,Object>>> deletedWithGOKbRecord = []
        /*
            processing list of deleted TIPPs, doing the following checks:
            - is there a remote GOKb record? Load remote package for that
         */
        deletedTIPPs.each { delTIPP ->
            println("now processing entry #${delTIPP.id} ${delTIPP.gokbId} of package ${delTIPP.pkg} with uuid ${delTIPP.pkg.gokbId}")
            if(!duplicateTIPPKeys.contains(delTIPP.gokbId)) {
                NodeChildren oaiRecord = oaiRecords.get(delTIPP.pkg.gokbId)
                if(!oaiRecord) {
                    /*
                        case: there is a TIPP in LAS:eR with an invalid GOKb package UUID, thus no record.
                        If we have IssueEntitlements depending on it: check subscription state
                            if deleted: mark IE as deleted
                            else check if there is an equivalent GOKb record -> load package, check if there is an equivalent TitleInstance-Package-Platform entry (so a TIPP entry!)
                            if so: remap to new UUID
                            else show subscriber
                    */
                    def packageRecord = http.get(path:'packages',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.pkg.gokbId],contentType:'xml') { resp, xml ->
                        GPathResult record = new XmlSlurper().parseText(xml.text)
                        if(record.error.@code == 'idDoesNotExist')
                            return "package ${delTIPP.pkg.gokbId} inexistent"
                        else return record.'GetRecord'.record.metadata.gokb.package
                    }
                    //case one: GOKb package does not exist
                    if(packageRecord instanceof GString) {
                        println(packageRecord)
                        List<Map<String,Object>> issueEntitlements = []
                        //check eventually depending issue entitlements
                        IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED).each { ie ->
                            Map<String,Object> ieDetails = [ie:ie]
                            if(ie.subscription.status == RDStore.TIPP_STATUS_DELETED) {
                                println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                                ieDetails.action = "deleteCascade"
                            }
                            else {
                                println("associated subscription is not deleted, report ...")
                                ieDetails.action = "report"
                                Map<String,Object> report = [subscriber:ie.subscription.getSubscriber().shortname,subscription:ie.subscription.name,title:delTIPP.title.title,package:delTIPP.pkg.name]
                                if(ie.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]) {
                                    report.consortium = ie.subscription.getConsortia().shortname
                                }
                                else {
                                    report.consortium = ""
                                }
                                ieDetails.report = report+[cause:"Paket ${delTIPP.pkg.gokbId} existiert nicht"]
                            }
                            issueEntitlements << ieDetails
                        }
                        Map<TitleInstancePackagePlatform,List<Map<String,Object>>> result = [:]
                        result[delTIPP] = issueEntitlements
                        deletedWithoutGOKbRecord << result
                    }
                    //case two: GOKb package does exist
                    else if(packageRecord instanceof NodeChildren) {
                        oaiRecords[delTIPP.pkg.gokbId] = packageRecord
                        oaiRecord = packageRecord
                    }
                }
                //case two continued: there is a GOKb record (preloaded by map or meanwhile fetched by OAI request)
                //do NOT set to else if because the variable may be set in structure above
                if(oaiRecord) {
                    //find TIPP in remote record
                    def gokbTIPP = oaiRecord.'**'.find { tipp ->
                        tipp.@uuid == delTIPP.gokbId && tipp.status.text() != RDStore.TIPP_STATUS_DELETED.value
                    }
                    if(!gokbTIPP) {
                        /*
                        case: there is a TIPP in LAS:eR with an invalid GOKb UUID, thus no record.
                        If we have IssueEntitlements depending on it: check subscription state
                            if deleted: mark IE as deleted
                            else check if there is an equivalent GOKb record -> load package, check if there is an equivalent TitleInstance-Package-Platform entry (so a TIPP entry!)
                            if so: remap to new UUID
                            else show subscriber
                         */
                        NodeChildren oaiTitleRecord = oaiRecords.get(delTIPP.title.gokbId)
                        List<Map<String,Object>> issueEntitlements = []
                        def equivalentTIPP
                        boolean titleRecordExists
                        boolean equivalentTIPPExists
                        //load remote title record in order to determine equivalent TitleInstance-Package-Platform link
                        if(!oaiTitleRecord) {
                            def titleRecord = http.get(path:'titles',query:[verb:'getRecord',metadataPrefix:'gokb',identifier:delTIPP.title.gokbId],contentType:'xml') { resp, xml ->
                                GPathResult record = new XmlSlurper().parseText(xml.text)
                                if(record.error.@code == 'idDoesNotExist')
                                    return "title ${delTIPP.title.gokbId} inexistent, name is ${delTIPP.title.title}"
                                else if(record.'GetRecord'.record.header.status == 'deleted')
                                    return "title ${delTIPP.title.gokbId} is marked as deleted, name is ${delTIPP.title.title}"
                                else
                                    return record.'GetRecord'.record.metadata.gokb.title
                            }
                            //no title record
                            if(titleRecord instanceof GString) {
                                println(titleRecord)
                                titleRecordExists = false
                            }
                            //title record exists
                            else if (titleRecord instanceof NodeChildren) {
                                println("title instance ${delTIPP.title.gokbId} found, reconcile UUID by retrieving package and platform")
                                titleRecordExists = true
                                oaiTitleRecord = (NodeChildren) titleRecord
                                oaiRecords.put(delTIPP.title.gokbId,oaiTitleRecord)
                            }
                        }
                        //title record exists (by OAI PMH request or by preload in map)
                        if(oaiTitleRecord) {
                            //match package and platform
                            equivalentTIPP = oaiTitleRecord.TIPPs.TIPP.find { node ->
                                node.package.'@uuid' == delTIPP.pkg.gokbId && node.platform.'@uuid' == delTIPP.platform.gokbId
                            }
                            if(equivalentTIPP) {
                                equivalentTIPPExists = true
                                println("TIPP found: should remapped to UUID ${equivalentTIPP.@uuid}")
                            }
                            else {
                                equivalentTIPPExists = false
                                println("no equivalent TIPP found")
                            }
                        }
                        IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED).each { ie ->
                            Map<String,Object> ieDetails = [ie:ie]
                            if(ie.subscription.status == RDStore.TIPP_STATUS_DELETED) {
                                println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                                ieDetails.action = "deleteCascade"
                            }
                            else {
                                println("${ie.subscription} is current, check if action needs to be taken ...")
                                Map<String,Object> report = [subscriber:ie.subscription.getSubscriber().shortname,subscription:ie.subscription.name,title:delTIPP.title.title,package:delTIPP.pkg.name]
                                if(ie.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]) {
                                    report.consortium = ie.subscription.getConsortia().shortname
                                }
                                else {
                                    report.consortium = ""
                                }
                                //does the title exist? If not, issue entitlement is void!
                                if(!titleRecordExists){
                                    ieDetails.action = "report"
                                    ieDetails.report = report+[cause:"Titel ${delTIPP.title.gokbId} existiert nicht"]
                                    println(ieDetails.report)
                                }
                                else if(titleRecordExists) {
                                    //does the TIPP exist? If so: check if it is already existing in package; if not, create it.
                                    if(equivalentTIPPExists) {
                                        if(!ie.tipp.pkg.tipps.find {it.gokbId == equivalentTIPP.@uuid}){
                                            ieDetails.action = "remap"
                                            ieDetails.target = equivalentTIPP.@uuid
                                        }
                                        else println("no remapping necessary!")
                                    }
                                    //If not, report because it is void!
                                    else {
                                        ieDetails.action = "report"
                                        ieDetails.report = report+[cause:"Kein äquivalentes TIPP gefunden"]
                                        println(ieDetails.report)
                                    }
                                }
                            }
                            if(ieDetails.action)
                                issueEntitlements << ieDetails
                        }
                        Map<TitleInstancePackagePlatform,List<Map<String,Object>>> result = [:]
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
                        Map<String,Map<String,Object>> result = [:]
                        RefdataValue currTippStatus = refdatas[gokbTIPP.status.text()]
                        Map<String,Object> tippDetails = [issueEntitlements: IssueEntitlement.findAllByTippAndStatusNotEqual(delTIPP,RDStore.TIPP_STATUS_DELETED), action: 'updateStatus', status: currTippStatus]
                        //storing key is needed in order to prevent LazyInitializationException when executing cleanup
                        result[delTIPP.globalUID] = tippDetails
                        deletedWithGOKbRecord << result
                    }
                }
            }
            else {
                println("TIPP marked as deleted is a duplicate, so already considered")
            }
        }
        http.shutdown()
        [deletedWithoutGOKbRecord:deletedWithoutGOKbRecord,deletedWithGOKbRecord:deletedWithGOKbRecord,mergingTIPPs:mergingTIPPs,duplicateTIPPKeys:duplicateTIPPKeys,excludes:excludes]
    }

    /**
     * Remaps the issue entitlements which hang on duplicates, merges title duplicates and deletes false records.
     * Very dangerous method, handle with extreme care!
     * Deprecated in its current form, it needs update if the cleanup needs to be used again one time
     * @param result the decision map build in {@link #listDeletedTIPPs()}
     * @return a {@link List} of title records which should be reported because there are holdings on them
     */
    List<List<String>> executeTIPPCleanup(Map result) {
        //first: merge duplicate entries
        result.mergingTIPPs.each { mergingTIPP ->
            IssueEntitlement.withTransaction { status ->
                try {
                    IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp.id = :mergeTarget where ie.id in (:iesToMerge)',[mergeTarget:mergingTIPP.mergeTarget,iesToMerge:mergingTIPP.iesToMerge])
                    status.flush()
                }
                catch (Exception e) {
                    log.error("failure on merging TIPPs ... rollback!")
                    status.setRollbackOnly()
                }
            }
        }
        println("remapping done, purge now duplicate entries ...")
        globalService.cleanUpGorm()
        List<List<String>> reportRows = []
        Map<RefdataValue,Set<String>> pendingChangeSetupMap = [:]
        Set<String> alreadyProcessed = []

        result.deletedWithoutGOKbRecord.each { entry ->
            entry.each { delTIPP,issueEntitlements ->
                issueEntitlements.each { ieDetails ->
                    IssueEntitlement ie = (IssueEntitlement) ieDetails.ie
                    switch(ieDetails.action) {
                        case "deleteCascade":
                            //mark as deleted!
                            println("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                            deletionService.deleteSubscription(ie.subscription,false)
                            break
                        case "report": reportRows << [ieDetails.report.consortium,ieDetails.report.subscriber,ieDetails.report.subscription,ieDetails.report.package,ieDetails.report.title,ieDetails.report.cause]
                            break
                        case "remap": if(!alreadyProcessed.contains(delTIPP.gokbId)){
                            //mark obsolete ones as deleted!
                            deletionService.deleteTIPP(delTIPP,TitleInstancePackagePlatform.findByGokbId(ieDetails.target))
                            alreadyProcessed << delTIPP.gokbId
                        }
                            break
                    }
                }
            }
        }
        result.deletedWithGOKbRecord.each { row ->
            row.each { delTIPP, tippDetails ->
                Set<Long> tippsToUpdate = pendingChangeSetupMap[tippDetails.status]
                if(!tippsToUpdate)
                    tippsToUpdate = []
                tippsToUpdate << delTIPP
                pendingChangeSetupMap[tippDetails.status] = tippsToUpdate
            }
        }
        pendingChangeSetupMap.each { RefdataValue status, Set<String> tippsToUpdate ->
            println("updating ${tippsToUpdate} to status ${status}")
            TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :status where tipp.globalUID in :tippsToUpdate',[status:status,tippsToUpdate:tippsToUpdate])
            //hook up pending changes
            tippsToUpdate.each { tippKey ->
                List<IssueEntitlement> iesToNotify = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.globalUID = :tippKey',[tippKey:tippKey])
                if(iesToNotify) {
                    TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGlobalUID(tippKey)
                    iesToNotify.each { IssueEntitlement ie ->
                        println("notifying subscription ${ie.subscription}")
                        Map<String, Object> changeMap = [target:ie.subscription,oid:"${ie.class.name}:${ie.id}",prop:'status',newValue:status.id,oldValue:ie.status.id]
                        changeNotificationService.determinePendingChangeBehavior(changeMap,PendingChangeConfiguration.TITLE_UPDATED,SubscriptionPackage.findBySubscriptionAndPkg(ie.subscription,tipp.pkg))
                        //changeNotificationService.registerPendingChange(PendingChange.PROP_SUBSCRIPTION,ie.subscription,ie.subscription.getSubscriber(),changeMap,null,null,changeDesc)
                    }
                }
                else println("no issue entitlements depending!")
            }
        }
        Set<TitleInstancePackagePlatform> tippsToDelete = TitleInstancePackagePlatform.findAllByGokbIdInListAndIdNotInList(result.duplicateTIPPKeys,result.excludes)
        //this is correct; only the duplicates should be deleted!
        if(tippsToDelete)
            deletionService.deleteTIPPsCascaded(tippsToDelete)
        println("Cleanup finished!")
        reportRows
    }

    /**
     * Retrieves titles without we:kb ID
     * @return a map containing faulty titles in the following structure:
     * <ul>
     *     <li>titles with a remapping target</li>
     *     <li>titles with issue entitlements</li>
     *     <li>deletable entries</li>
     *     <li>titles which should receive a UUID</li>
     * </ul>
     */
    Map<String,Object> getTIPPsWithoutGOKBId() {
        List<TitleInstancePackagePlatform> tippsWithoutGOKbID = TitleInstancePackagePlatform.findAllByGokbIdIsNullOrGokbIdLike(RDStore.GENERIC_NULL_VALUE.value)
        List<IssueEntitlement> issueEntitlementsAffected = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp in :tipps',[tipps:tippsWithoutGOKbID])
        Map<TitleInstancePackagePlatform,Set<IssueEntitlement>> ieTippMap = [:]
        List<Map<String,Object>> tippsWithAlternate = []
        Map<Long,Long> toDelete = [:]
        Set<Long> toUUIDfy = []
        tippsWithoutGOKbID.each { tipp ->
            TitleInstancePackagePlatform altTIPP = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status = :current and tipp.gokbId != null",[pkg:tipp.pkg,current:RDStore.TIPP_STATUS_CURRENT])[0]
            if(altTIPP) {
                toDelete[tipp.id] = altTIPP.id
                tippsWithAlternate << [tipp:tipp,altTIPP:altTIPP]
            }
            else toUUIDfy << tipp.id
        }
        issueEntitlementsAffected.each { IssueEntitlement ie ->
            if (ieTippMap.get(ie.tipp)) {
                ieTippMap[ie.tipp] << ie
            } else {
                Set<IssueEntitlement> ies = new TreeSet<IssueEntitlement>()
                ies.add(ie)
                ieTippMap[ie.tipp] = ies
            }
        }
        [tipps: tippsWithAlternate, issueEntitlements: ieTippMap, toDelete: toDelete, toUUIDfy: toUUIDfy]
    }

    /**
     * Deletes the given titles and merges duplicates with the given instance
     * @param toDelete titles to be deleted
     * @param toUUIDfy titles which should persist but marked with null entry for that the gokbId property may be set not null
     */
    void purgeTIPPsWihtoutGOKBId(toDelete,toUUIDfy) {
        toDelete.each { oldTippId, newTippId ->
            TitleInstancePackagePlatform oldTipp = TitleInstancePackagePlatform.get(oldTippId)
            TitleInstancePackagePlatform newTipp = TitleInstancePackagePlatform.get(newTippId)
            deletionService.deleteTIPP(oldTipp,newTipp)
        }
        toUUIDfy.each { tippId ->
            TitleInstancePackagePlatform.executeUpdate("update TitleInstancePackagePlatform tipp set tipp.gokbId = :missing where tipp.id = :id",[missing:"${RDStore.GENERIC_NULL_VALUE}.${tippId}",id:tippId])
        }
    }

    /**
     * Call to load titles marked as deleted; if the confirm is checked, the deletion of titles and issue entitlements marked as deleted as well is executed
     * @param doIt execute the cleanup?
     * @return a result map of titles whose we:kb entry has been marked as deleted
     */
    Map<String, Object> expungeDeletedTIPPs(boolean doIt) {
        GlobalRecordSource grs = GlobalRecordSource.findByActiveAndRectype(true, GlobalSourceSyncService.RECTYPE_TIPP)
        Map<String, Object> result = [:]
        Map<String, String> wekbUuids = [:]
        Set<Map> titles = []
        HTTPBuilder http = new HTTPBuilder(grs.uri+'/find') //we presume that the count will never get beyond 10000
        http.request(Method.POST, ContentType.JSON) { req ->
            body = [componentType: 'TitleInstancePackagePlatform',
                    max: 10000,
                    status: ['Deleted', GlobalSourceSyncService.PERMANENTLY_DELETED]]
            requestContentType = ContentType.URLENC
            response.success = { resp, json ->
                if(resp.status == 200) {
                    json.records.each{ Map record ->
                        wekbUuids.put(record.uuid, record.status)
                    }
                }
                else {
                    throw new SyncException("erroneous response")
                }
            }
            response.failure = { resp, reader ->
                log.error("server response: ${resp.statusLine}")
                if(resp.status == 404) {
                    requestResult.error = resp.status
                }
                else
                    throw new SyncException("error on request: ${resp.statusLine} : ${reader}")
            }
        }
        http.shutdown()
        if(wekbUuids) {
            List deletedLaserTIPPs = TitleInstancePackagePlatform.executeQuery('select new map(tipp.id as tippId, tipp.gokbId as wekbId, tipp.status as laserStatus, tipp.name as title) from TitleInstancePackagePlatform tipp where tipp.status = :deleted or tipp.gokbId in (:deletedWekbIDs)', [deleted: RDStore.TIPP_STATUS_DELETED, deletedWekbIDs: wekbUuids.keySet()])
            Set<String> keysToDelete = []
            deletedLaserTIPPs.each { Map row ->
                Map<String, Object> titleRow = row
                titleRow.wekbStatus = wekbUuids.get(row.wekbId)
                List issueEntitlements = IssueEntitlement.executeQuery("select new map(ie.id as id, concat(s.name, ' (', s.startDate, '-', s.endDate, ') (', oo.org.sortname, ')') as subscriptionName) from IssueEntitlement ie join ie.tipp tipp join ie.subscription s join s.orgRelations oo where oo.roleType in (:roleTypes) and tipp.gokbId = :wekbId and ie.status != :deleted", [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER], wekbId: row.wekbId, deleted: RDStore.TIPP_STATUS_DELETED])
                titleRow.issueEntitlements = issueEntitlements
                if(doIt) {
                    if(!issueEntitlements) {
                        keysToDelete << row.wekbId
                    }
                    else titles << titleRow
                }
                else
                    titles << titleRow
            }
            if(doIt && keysToDelete) {
                Set<TitleInstancePackagePlatform> toDelete = TitleInstancePackagePlatform.findAllByGokbIdInList(keysToDelete)
                if(toDelete) {
                    toDelete.collate(50).each { List<TitleInstancePackagePlatform> subList ->
                        deletionService.deleteTIPPsCascaded(subList)
                    }
                }
                else log.info("no titles to delete")
            }
        }
        result.titles = titles
        result
    }

    /**
     * Deprecated in its current form as it uses the obsolete OAI endpoint to retrieve data; was used to
     * compare the LAS:eR platform data against the we:kb (then still GOKb) mirror instance and to determine
     * those records which are obsolete in LAS:eR
     * @return a {@link Map} containing obsolete platform records
     */
    Map<String, Object> listPlatformDuplicates() {
        Map<String,Object> result = [:]
        Map<String, GPathResult> oaiRecords = [:]
        List<Platform> platformsWithoutTIPPs = Platform.executeQuery('select plat from Platform plat where plat.tipps.size = 0')
        result.platformDupsWithoutTIPPs = []
        result.platformsToUpdate = []
        result.platformsWithoutGOKb = []
        List<Platform> platformsWithoutGOKb = Platform.findAllByGokbIdIsNull()
        result.inexistentPlatforms = []
        result.incorrectPlatformDups = []
        result.database = []
        GlobalRecordSource grs = GlobalRecordSource.findAll().get(0)
        List duplicateKeys = Platform.executeQuery('select plat.gokbId,count(plat.gokbId) from Platform plat where plat.gokbId is not null group by plat.gokbId having count(plat.gokbId) > 1')
        duplicateKeys.each { row ->
            //get platform, get eventual TIPPs of platform, determine from package which platform key is correct, if it is correct: ignore, otherwise, add to result
            List<Platform> platformDuplicates = Platform.findAllByGokbId(row[0])
            platformDuplicates.each { Platform platform ->
                println("processing platform ${platform} with duplicate GOKb ID ${platform.gokbId}")
                //it ran too often into null pointer exceptions ... we set a tighter check!
                if(platform.tipps.size() > 0) {
                    TitleInstancePackagePlatform referenceTIPP = platform.tipps[0]
                    GPathResult packageRecord = oaiRecords.get(referenceTIPP.pkg.gokbId)
                    if(!packageRecord) {
                        packageRecord = globalSourceSyncService.fetchRecord(grs.uri,'packages',[verb:'GetRecord',metadataPrefix:grs.fullPrefix,identifier:referenceTIPP.pkg.gokbId])
                        oaiRecords.put(referenceTIPP.pkg.gokbId,packageRecord)
                    }
                    if(packageRecord.record.metadata.gokb.package) {
                        GPathResult referenceGOKbTIPP = packageRecord.record.metadata.gokb.package.TIPPs.TIPP.find { tipp ->
                            tipp.@uuid.text() == referenceTIPP.gokbId
                        }
                        if(referenceGOKbTIPP) {
                            String guessedCorrectPlatformKey = referenceGOKbTIPP.platform.@uuid.text()
                            if(platform.gokbId != guessedCorrectPlatformKey) {
                                result.incorrectPlatformDups << platform
                            }
                        }
                    }
                }
                else {
                    result.platformDupsWithoutTIPPs << platform
                }
            }
        }
        platformsWithoutGOKb.each { Platform platform ->
            if(!OrgAccessPointLink.findByPlatform(platform) && !TitleInstancePackagePlatform.findByPlatform(platform) && platform.propertySet.empty) {
                result.platformsWithoutGOKb << platform
            }
            else {
                result.database << [id:platform.id,name:platform.name]
            }
        }
        platformsWithoutTIPPs.each { Platform platform ->
            println("processing platform ${platform} without TIPP ${platform.gokbId} to check correctness ...")
            Map esQuery = gokbService.queryElasticsearch('https://wekb.hbz-nrw.de/api/find?uuid='+platform.gokbId)
            List esResult
            //is a consequent error of GOKbService's copy-paste-mess ...
            if(esQuery.warning)
                esResult = esQuery.warning.records
            else if(esQuery.info)
                esResult = esQuery.info.records
            if(esResult) {
                Map gokbPlatformRecord = esResult[0]
                if(gokbPlatformRecord.name == platform.name)
                    println("Name ${platform.name} is correct")
                else {
                    println("Name ${platform.name} is not correct, should actually be ${gokbPlatformRecord.name}")
                    result.platformsToUpdate << [old:platform.globalUID,correct:gokbPlatformRecord]
                }
            }
            else {
                if(!OrgAccessPointLink.findByPlatform(platform) && !TitleInstancePackagePlatform.findByPlatform(platform) && platform.propertySet.empty) {
                    result.inexistentPlatforms << platform
                }
                else {
                    if(!result.database.find{ entry -> entry.id == platform.id})
                        result.database << [id:platform.id,name:platform.name]
                }
            }
        }
        result
    }

    /**
     * Matches the subscription holdings against the package stock where a pending change configuration for new title has been set to auto accept. This method
     * fetches those packages where auto-accept has been configured and inserts missing titles which should have been registered already on sync run but
     * failed to do so because of bugs
     */
    @Transactional
    void matchPackageHoldings() {
        Sql sql = GlobalService.obtainSqlConnection()
        sql.withTransaction {
            List subscriptionPackagesConcerned = sql.rows("select sp_sub_fk, sp_pkg_fk, sub_has_perpetual_access, " +
                    "(select count(tipp_id) from title_instance_package_platform where tipp_pkg_fk = sp_pkg_fk and tipp_status_rv_fk = :current) as pkg_cnt, " +
                    "(select count(ie_id) from issue_entitlement join title_instance_package_platform as t_ie on ie_tipp_fk = t_ie.tipp_id where ie_subscription_fk = sp_sub_fk and sp_pkg_fk = t_ie.tipp_pkg_fk and ie_status_rv_fk = :current) as holding_cnt " +
                    "from subscription_package join pending_change_configuration on pcc_sp_fk = sp_id join subscription on sp_sub_fk = sub_id " +
                    "where pcc_setting_key_enum = :newTitle and pcc_setting_value_rv_fk = :accept "+
                    "and ((select count(tipp_id) from title_instance_package_platform where tipp_pkg_fk = sp_pkg_fk and tipp_status_rv_fk = :current) != " +
                    " (select count(ie_id) from issue_entitlement join title_instance_package_platform as t_ie on ie_tipp_fk = t_ie.tipp_id where ie_subscription_fk = sp_sub_fk and sp_pkg_fk = t_ie.tipp_pkg_fk and ie_status_rv_fk = :current))",
            [newTitle: PendingChangeConfiguration.NEW_TITLE, current: RDStore.TIPP_STATUS_CURRENT.id, accept: RDStore.PENDING_CHANGE_CONFIG_ACCEPT.id])
            subscriptionPackagesConcerned.eachWithIndex { GroovyRowResult row, int ax ->
                Set<Long> subIds = [row['sp_sub_fk']]
                List inheritingSubs = sql.rows("select sub_id from subscription join audit_config on auc_reference_id = sub_parent_sub_fk where auc_reference_field = :newTitle and sub_parent_sub_fk = :parent", [newTitle: PendingChangeConfiguration.NEW_TITLE, parent: row['sp_sub_fk']])
                subIds.addAll(inheritingSubs.collect { GroovyRowResult inherit -> inherit['sub_id'] })
                int pkgId = row['sp_pkg_fk'], subCount = row['holding_cnt'], pkgCount = row['pkg_cnt']
                boolean perpetualAccess = row['sub_has_perpetual_access']
                if(pkgCount > 0 && pkgCount > subCount) {
                    subIds.each { Long subId ->
                        log.debug("now processing package ${subId}:${pkgId}, counts: ${subCount} vs. ${pkgCount}")
                        packageService.bulkAddHolding(sql, subId, pkgId, perpetualAccess)
                    }
                }
            }
        }
    }

    /**
     * Clears the retrieved platform duplicates from the database:
     * <ul>
     *     <li>duplicates without titles</li>
     *     <li>platforms without we:kb IDs</li>
     *     <li>platforms without we:kb record</li>
     * </ul>
     */
    @Transactional
    void executePlatformCleanup(Map result) {
        List<String> toDelete = []
        toDelete.addAll(result.platformDupsWithoutTIPPs.collect{ plat -> plat.globalUID })
        toDelete.addAll(result.platformsWithoutGOKb.collect{plat -> plat.globalUID })
        toDelete.addAll(result.inexistentPlatforms.collect{plat -> plat.globalUID })
        result.platformsToUpdate.each { entry ->
            Platform oldRecord = Platform.findByGlobalUID(entry.old)
            Map newRecord = entry.correct
            oldRecord.name = newRecord.name
            oldRecord.primaryUrl = newRecord.primaryUrl
            oldRecord.status = RefdataValue.getByValueAndCategory(newRecord.status,RDConstants.PLATFORM_STATUS)
            oldRecord.save()
        }
        Platform.executeUpdate('delete from Platform plat where plat.globalUID in :toDelete',[toDelete:toDelete])
    }

}
