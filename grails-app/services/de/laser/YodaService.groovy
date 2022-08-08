package de.laser


import de.laser.exceptions.SyncException
import de.laser.config.ConfigMapper
import de.laser.http.BasicHttpClient
import de.laser.remote.GlobalRecordSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.oap.OrgAccessPointLink
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.slurpersupport.GPathResult

/**
 * This service handles bulk and cleanup operations, testing areas and debug information
 */
//@Transactional
class YodaService {

    ContextService contextService
    DeletionService deletionService
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
            List<Package> pkgDupsWithTipps = Package.executeQuery('select distinct(tipp.pkg) from TitleInstancePackagePlatform tipp where tipp.pkg in (:pkg) and tipp.status != :removed',[pkg:pkgDuplicates,removed:RDStore.TIPP_STATUS_REMOVED])
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
     * Executes the cleanup of titles marked as removed
     * @return a {@link List} of title records which should be reported because there are holdings on them
     */
    def executeTIPPCleanup(Map result) {

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
    Map<String, Object> expungeRemovedTIPPs(boolean doIt) {
        GlobalRecordSource grs = GlobalRecordSource.findByActiveAndRectype(true, GlobalSourceSyncService.RECTYPE_TIPP)
        Map<String, Object> result = [:]
        Map<String, String> wekbUuids = [:]
        Set<Map> titles = []
        BasicHttpClient http = new BasicHttpClient(grs.uri+'/find') //we presume that the count will never get beyond 10000
        Closure success = { resp, json ->
            if(resp.code() == 200) {
                json.records.each{ Map record ->
                    wekbUuids.put(record.uuid, record.status)
                }
            }
            else {
                throw new SyncException("erroneous response")
            }
        }
        Closure failure = { resp, reader ->
            log.warn ('Response: ' + resp.getStatus().getCode() + ' - ' + resp.getStatus().getReason())
            if(resp.code() == 404) {
                result.error = resp.status
            }
            else
                throw new SyncException("error on request: ${resp.getStatus().getCode()} : ${reader}")
        }
        Map<String, Object> queryParams = [componentType: 'TitleInstancePackagePlatform',
                                           max: 10000,
                                           status: ['Removed', GlobalSourceSyncService.PERMANENTLY_DELETED]]
        http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, success, failure)
        http.close()

        if(wekbUuids) {
            wekbUuids.each { String key, String status ->
                TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(key)
                if(tipp) {
                    tipp.status = RDStore.TIPP_STATUS_REMOVED
                    PendingChange.construct([msgToken:PendingChangeConfiguration.TITLE_REMOVED,target:tipp,status:RDStore.PENDING_CHANGE_HISTORY])
                    tipp.save()
                }
            }
            List deletedLaserTIPPs = TitleInstancePackagePlatform.executeQuery('select new map(tipp.id as tippId, tipp.gokbId as wekbId, tipp.status as laserStatus, tipp.name as title) from TitleInstancePackagePlatform tipp where tipp.status = :removed or tipp.gokbId in (:deletedWekbIDs)', [removed: RDStore.TIPP_STATUS_REMOVED, deletedWekbIDs: wekbUuids.keySet()])
            Set<String> keysToDelete = []
            deletedLaserTIPPs.each { Map row ->
                Map<String, Object> titleRow = row
                titleRow.wekbStatus = wekbUuids.get(row.wekbId)
                List issueEntitlements = IssueEntitlement.executeQuery("select new map(ie.id as id, concat(s.name, ' (', s.startDate, '-', s.endDate, ') (', oo.org.sortname, ')') as subscriptionName) from IssueEntitlement ie join ie.tipp tipp join ie.subscription s join s.orgRelations oo where oo.roleType in (:roleTypes) and tipp.gokbId = :wekbId and ie.status != :removed", [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER], wekbId: row.wekbId, removed: RDStore.TIPP_STATUS_REMOVED])
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
                    //we should check the underlying queries instead of chunking
                    deletionService.deleteTIPPsCascaded(toDelete)
                    /*toDelete.collate(50).each { List<TitleInstancePackagePlatform> subList ->
                        deletionService.deleteTIPPsCascaded(subList)
                    }*/
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
        Map<String,Object> result = [ flagContentElasticsearch : true ] // gokbService.queryElasticsearch
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
                log.debug("processing platform ${platform} with duplicate GOKb ID ${platform.gokbId}")
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
            log.debug("processing platform ${platform} without TIPP ${platform.gokbId} to check correctness ...")
            Map esQuery = gokbService.queryElasticsearch('https://wekb.hbz-nrw.de/api/find?uuid='+platform.gokbId)
            List esResult
            //is a consequent error of GOKbService's copy-paste-mess ...
            if(esQuery.warning)
                esResult = esQuery.warning.records
//            else if(esQuery.info)
//                esResult = esQuery.info.records
            if(esResult) {
                Map gokbPlatformRecord = esResult[0]
                if(gokbPlatformRecord.name == platform.name)
                    log.debug("Name ${platform.name} is correct")
                else {
                    log.debug("Name ${platform.name} is not correct, should actually be ${gokbPlatformRecord.name}")
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
