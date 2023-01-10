package de.laser

import de.laser.config.ConfigMapper
import de.laser.exceptions.SyncException
import de.laser.http.BasicHttpClient
import de.laser.remote.GlobalRecordSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import groovyx.gpars.GParsPool
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import org.hibernate.Session

import java.util.concurrent.ExecutorService

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
    ExecutorService executorService

    /**
     * Checks whether debug information should be displayed
     * @return true if setting is enabled by config or the viewer has admin rights, false otherwise
     */
    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigMapper.getShowDebugInfo() )
    }

    /**
     * Copies missing values into the issue entitlements; values are being taken from the title the holding records have been derived
     */
    void fillValue(String toUpdate) {
        IssueEntitlement.withNewSession { Session sess ->
            int total = IssueEntitlement.executeQuery('select count(ie) from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub where ((sub.startDate >= :start and sub.endDate <= :end) or sub.startDate = null or sub.endDate = null) and tipp.'+toUpdate+' != null and tipp.status != :removed and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, start: DateUtils.getSDF_yyyyMMdd().parse('2022-01-01'), end: DateUtils.getSDF_yyyyMMdd().parse('2023-12-31')])[0]
            int max = 100000
            for(int ctr = 0; ctr < total; ctr += max) {
                IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub where ((sub.startDate >= :start and sub.endDate <= :end) or sub.startDate = null or sub.endDate = null) and tipp.'+toUpdate+' != null and tipp.status != :removed and ie.status != :removed order by sub.startDate', [removed: RDStore.TIPP_STATUS_REMOVED, start: DateUtils.getSDF_yyyyMMdd().parse('2022-01-01'), end: DateUtils.getSDF_yyyyMMdd().parse('2023-12-31')], [max: max, offset: ctr]).each { IssueEntitlement ie ->
                    ie[toUpdate] = ie.tipp[toUpdate]
                    ie.save()
                }
                log.debug("flush after ${ctr+max}")
                sess.flush()
            }
            //IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.'+toUpdate+' = (select tipp.'+toUpdate+' from TitleInstancePackagePlatform tipp where tipp = ie.tipp and tipp.'+toUpdate+' != null and tipp.status != :removed) where ie.'+toUpdate+' = null and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED])
        }

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
    void expungeRemovedTIPPs() {
        executorService.execute( {
            GlobalRecordSource grs = GlobalRecordSource.findByActiveAndRectype(true, GlobalSourceSyncService.RECTYPE_TIPP)
            Map<String, Object> result = [:]
            Map<String, String> wekbUuids = [:]
            Set<Map> titles = []
            HttpClientConfiguration config = new DefaultHttpClientConfiguration()
            config.maxContentLength = 1024 * 1024 * 20
            BasicHttpClient http = new BasicHttpClient(grs.uri+'/find', config) //we presume that the count will never get beyond 10000
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
            BasicHttpClient httpScroll = new BasicHttpClient(grs.uri+'/scroll', config)
            //invert: check if non-deleted TIPPs still exist in we:kb instance
            GParsPool.withPool(3) {
                Package.findAllByPackageStatusNotEqual(RDStore.PACKAGE_STATUS_DELETED).eachWithIndexParallel { Package pkg, int i ->
                    Package.withTransaction {
                        Set<TitleInstancePackagePlatform> tipps = TitleInstancePackagePlatform.findAllByPkg(pkg)
                        queryParams = [pkg: pkg.gokbId, status: ['Current', 'Expected', 'Retired', 'Deleted', 'Removed', GlobalSourceSyncService.PERMANENTLY_DELETED], max: 10000]
                        Set<String> wekbPkgTipps = []
                        boolean more = true
                        Closure checkSuccess
                        checkSuccess = { resp, json ->
                            if(resp.code() == 200) {
                                if(json.size == 0) {
                                    tipps.each { TitleInstancePackagePlatform tipp ->
                                        wekbUuids.put(tipp.gokbId, 'inexistent')
                                    }
                                    //because of parallel process; session mismatch when accessing via GORM
                                    Package.executeUpdate('update Package pkg set pkg.packageStatus = :deleted where pkg = :pkg',[deleted: RDStore.PACKAGE_STATUS_DELETED, pkg: pkg])
                                }
                                else {
                                    wekbPkgTipps.addAll(json.records.collect { Map record -> record.uuid }.toSet())
                                    more = Boolean.valueOf(json.hasMoreRecords)
                                    if(more) {
                                        queryParams.scrollId = json.scrollId
                                        httpScroll.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, checkSuccess, failure)
                                    }
                                }
                            }
                            else {
                                throw new SyncException("erroneous response")
                            }
                        }
                        if (tipps.size() > 10000) {
                            queryParams.component_type = 'TitleInstancePackagePlatform'
                            httpScroll.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, checkSuccess, failure)
                        } else {
                            queryParams.componentType = 'TitleInstancePackagePlatform'
                            http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, checkSuccess, failure)
                        }
                        tipps.each { TitleInstancePackagePlatform tipp ->
                            if (!wekbPkgTipps.contains(tipp.gokbId))
                                wekbUuids.put(tipp.gokbId, 'inexistent')
                        }
                    }
                }
            }
            http.close()
            httpScroll.close()

            if(wekbUuids) {
                GParsPool.withPool(8) { pool ->
                    wekbUuids.eachParallel { String key, String status ->
                        TitleInstancePackagePlatform.withTransaction {
                            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(key)
                            if (tipp) {
                                tipp.status = RDStore.TIPP_STATUS_REMOVED
                                PendingChange.construct([msgToken: PendingChangeConfiguration.TITLE_REMOVED, target: tipp, status: RDStore.PENDING_CHANGE_HISTORY])
                                tipp.save()
                            }
                        }
                    }
                }
                List deletedLaserTIPPs = TitleInstancePackagePlatform.executeQuery('select new map(tipp.id as tippId, tipp.gokbId as wekbId, tipp.status as laserStatus, tipp.name as title) from TitleInstancePackagePlatform tipp where tipp.status = :removed or tipp.gokbId in (:deletedWekbIDs)', [removed: RDStore.TIPP_STATUS_REMOVED, deletedWekbIDs: wekbUuids.keySet()])
                Set<String> keysToDelete = []
                deletedLaserTIPPs.each { Map row ->
                    Map<String, Object> titleRow = row
                    titleRow.wekbStatus = wekbUuids.get(row.wekbId)
                    List issueEntitlements = IssueEntitlement.executeQuery("select new map(ie.id as id, concat(s.name, ' (', s.startDate, '-', s.endDate, ') (', oo.org.sortname, ')') as subscriptionName) from IssueEntitlement ie join ie.tipp tipp join ie.subscription s join s.orgRelations oo where oo.roleType in (:roleTypes) and tipp.gokbId = :wekbId and ie.status != :removed", [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER], wekbId: row.wekbId, removed: RDStore.TIPP_STATUS_REMOVED])
                    titleRow.issueEntitlements = issueEntitlements
                    if(!issueEntitlements) {
                        keysToDelete << row.wekbId
                    }
                }
                if(keysToDelete) {
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
        })
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
     * Deletes the obsolete (removed) objects of the given type
     */
    void expungeRemovedComponents(String className) {
        long rectype
        String componentType
        Set objects = []
        switch(className) {
            case Org.class.name: rectype = GlobalSourceSyncService.RECTYPE_ORG
                componentType = 'Org'
                objects.addAll(Org.findAllByStatusNotEqualAndGokbIdIsNotNull(RDStore.ORG_STATUS_REMOVED))
                break
            case Platform.class.name: rectype = GlobalSourceSyncService.RECTYPE_PLATFORM
                componentType = 'Platform'
                objects.addAll(Platform.findAllByStatusNotEqual(RDStore.PLATFORM_STATUS_REMOVED))
                break
            default: rectype = -1
                componentType = null
                break
        }
        if(componentType) {
            globalSourceSyncService.setSource(GlobalRecordSource.findByRectypeAndActive(rectype, true))
            objects.each { obj ->
                Map record = globalSourceSyncService.fetchRecordJSON(false, [componentType: componentType, uuid: obj.gokbId])
                if(record?.count == 0) {
                    if(obj instanceof Org) {
                        OrgRole.executeUpdate('delete from OrgRole oo where oo.org = :org', [org: obj])
                        deletionService.deleteOrganisation(obj, null, false)
                    }
                    else if(obj instanceof Platform) {
                        IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed where ie.status != :removed and ie.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.platform = :plat)', [removed: RDStore.TIPP_STATUS_REMOVED, plat: obj])
                        TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :removed where tipp.status != :removed and tipp.platform = :plat', [plat: obj, removed: RDStore.TIPP_STATUS_REMOVED])
                        obj.status = RDStore.PLATFORM_STATUS_REMOVED
                        obj.save()
                    }
                }
                else {
                    log.debug("we:kb platform record located, status is: ${record.records[0].status}")
                }
            }
        }
    }

}
