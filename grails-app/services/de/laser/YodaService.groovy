package de.laser

import de.laser.config.ConfigMapper
import de.laser.exceptions.SyncException
import de.laser.http.BasicHttpClient
import de.laser.properties.OrgProperty
import de.laser.remote.GlobalRecordSource
import de.laser.storage.Constants
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

import java.time.Duration
import java.util.concurrent.ExecutorService

/**
 * This service handles bulk and cleanup operations, testing areas and debug information
 */
//@Transactional
class YodaService {

    ContextService contextService
    DeletionService deletionService
    GlobalSourceSyncService globalSourceSyncService
    GlobalService globalService
    PackageService packageService
    ExecutorService executorService

    boolean bulkOperationRunning = false

    /**
     * Checks whether debug information should be displayed
     * @return true if setting is enabled by config or the viewer has admin rights, false otherwise
     */
    boolean showDebugInfo() {
        //enhanced as of ERMS-829
        return ( SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || ConfigMapper.getShowDebugInfo() )
    }

    /**
     * Copies missing values into the issue entitlements; values are being taken from the title the holding records have been derived
     */
    void fillValue(String toUpdate) {
        if(toUpdate == 'globalUID') {
            int max = 200000
            Sql sql = globalService.obtainSqlConnection()
            int total = sql.rows('select count(*) from issue_entitlement ie where ie_guid is null')[0]['count']
            for(int ctr = 0; ctr < total; ctr += max) {
                sql.executeUpdate("update issue_entitlement set ie_last_updated = now(), ie_guid = concat('issueentitlement:',gen_random_uuid()) where ie_id in (select ie_id from issue_entitlement where ie_guid is null limit "+max+")")
                log.debug("processed: ${ctr}")
            }
        }
        else {
            IssueEntitlement.withNewSession { Session sess ->
                int max = 100000
                int total = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub where ((sub.startDate >= :start and sub.endDate <= :end) or sub.startDate = null or sub.endDate = null) and tipp.'+toUpdate+' != null and tipp.status != :removed and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, start: DateUtils.getSDF_yyyyMMdd().parse('2022-01-01'), end: DateUtils.getSDF_yyyyMMdd().parse('2023-12-31')])[0]
                for(int ctr = 0; ctr < total; ctr += max) {
                    IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub where ((sub.startDate >= :start and sub.endDate <= :end) or sub.startDate = null or sub.endDate = null) and tipp.'+toUpdate+' != null and tipp.status != :removed and ie.status != :removed order by sub.startDate', [removed: RDStore.TIPP_STATUS_REMOVED, start: DateUtils.getSDF_yyyyMMdd().parse('2022-01-01'), end: DateUtils.getSDF_yyyyMMdd().parse('2023-12-31')], [max: max, offset: ctr]).each { IssueEntitlement ie ->
                        ie[toUpdate] = ie.tipp[toUpdate]
                        ie.save()
                    }
                    log.debug("flush after ${ctr+max}")
                    sess.flush()
                }
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
     * Correction method. Use with care.
     * Processes all {@link IssueEntitlement}s whose reference {@link TitleInstancePackagePlatform} have a different status and matches the issue entitlement status to the reference title one's
     */
    void matchTitleStatus() {
        int max = 100000
        bulkOperationRunning = true
        executorService.execute({
            int total = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.tipp.status != ie.status and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED])[0]
            log.debug("${total} titles concerned")
            for(int offset = 0; offset < total; offset += max) {
                Set<IssueEntitlement> iesConcerned = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.status != ie.status and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED], [max: max, offset: offset])
                iesConcerned.eachWithIndex { IssueEntitlement ie, int i ->
                    log.debug("now processing record #${i+offset} from total ${total}")
                    ie.status = ie.tipp.status
                    ie.save()
                }
                globalService.cleanUpGorm()
            }
            log.debug("release lock ...")
            bulkOperationRunning = false
        })
    }

    /**
     * Call to load titles marked as deleted; if the confirm is checked, the deletion of titles and issue entitlements marked as deleted as well is executed
     * @param doIt execute the cleanup?
     * @return a result map of titles whose we:kb entry has been marked as deleted
     * @deprecated does not work as should and it should be more appropriate to reload the packages themselves; use reloadPackage() instead
     */
    @Deprecated
    void expungeRemovedTIPPs() {
        executorService.execute( {
            GlobalRecordSource grs = GlobalRecordSource.findByActiveAndRectype(true, GlobalSourceSyncService.RECTYPE_TIPP)
            Map<String, Object> result = [:]
            Map<String, String> wekbUuids = [:]
            HttpClientConfiguration config = new DefaultHttpClientConfiguration()
            config.maxContentLength = 1024 * 1024 * 100
            config.readTimeout = Duration.ofMinutes(2)
            BasicHttpClient http = new BasicHttpClient(grs.uri+'searchApi', config)
            int offset = 0, max = 20000
            boolean more = true
            Map<String, Object> queryParams = [componentType: 'TitleInstancePackagePlatform',
                                               status: ['Removed', Constants.PERMANENTLY_DELETED],
                                               username: ConfigMapper.getWekbApiUsername(),
                                               password: ConfigMapper.getWekbApiPassword(),
                                               max:max, offset:offset]
            Closure failure = { resp, reader ->
                if(resp?.code() == 404) {
                    result.error = resp.code()
                }
                else
                    throw new SyncException("error on request: ${resp?.status()} : ${reader}")
            }
            Closure success
            success = { resp, json ->
                if(resp.code() == 200) {
                    json.result.each{ Map record ->
                        wekbUuids.put(record.uuid, record.status)
                    }
                    more = json.page_current < json.page_total
                    if(more) {
                        offset += max
                        queryParams.offset = offset
                        http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, success, failure)
                    }
                }
                else {
                    throw new SyncException("erroneous response")
                }
            }
            http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, success, failure)
            //invert: check if non-deleted TIPPs still exist in we:kb instance
            Set<Package> allPackages = Package.findAllByPackageStatusNotEqual(RDStore.PACKAGE_STATUS_DELETED)
            GParsPool.withPool(8) {
                allPackages.eachWithIndexParallel { Package pkg, int i ->
                    Package.withTransaction {
                        log.debug("Thread ${Thread.currentThread().getName()} processes now package record ${i} out of ${allPackages.size()} records!")
                        Map<String, String> wekbPkgTipps = [:]
                        offset = 0
                        Set<TitleInstancePackagePlatform> tipps = TitleInstancePackagePlatform.findAllByPkg(pkg)
                        queryParams = [componentType: 'TitleInstancePackagePlatform',
                                       tippPackageUuid: pkg.gokbId,
                                       username: ConfigMapper.getWekbApiUsername(),
                                       password: ConfigMapper.getWekbApiPassword(),
                                       status: ['Current', 'Expected', 'Retired', 'Deleted', 'Removed', Constants.PERMANENTLY_DELETED], max: max, offset: offset]
                        more = true
                        Closure checkSuccess
                        checkSuccess = { resp, json ->
                            if(resp.code() == 200) {
                                if(json.result_count == 0) {
                                    tipps.each { TitleInstancePackagePlatform tipp ->
                                        wekbUuids.put(tipp.gokbId, Constants.PERMANENTLY_DELETED)
                                    }
                                    //because of parallel process; session mismatch when accessing via GORM
                                    Package.executeUpdate('update Package pkg set pkg.packageStatus = :deleted where pkg = :pkg',[deleted: RDStore.PACKAGE_STATUS_DELETED, pkg: pkg])
                                }
                                else {
                                    wekbPkgTipps.putAll(json.result.collectEntries { Map record -> [record.uuid, record.status] })
                                    more = json.page_current < json.page_total
                                    if(more) {
                                        offset += max
                                        queryParams.offset = offset
                                        http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, checkSuccess, failure)
                                    }
                                }
                            }
                            else {
                                throw new SyncException("erroneous response")
                            }
                        }
                        http.post(BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, checkSuccess, failure)
                        tipps.each { TitleInstancePackagePlatform tipp ->
                            if (!wekbPkgTipps.containsKey(tipp.gokbId) || wekbPkgTipps.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value] || wekbUuids.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value]) {
                                log.info("mark ${tipp.gokbId} in package ${tipp.pkg.gokbId} as removed:")
                                log.info("reason: !wekbPkgTipps.contains(tipp.gokbId): ${!wekbPkgTipps.containsKey(tipp.gokbId)} / ")
                                log.info("wekbPkgTipps.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value] / ${wekbPkgTipps.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value]}")
                                log.info("wekbUuids.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value]: ${wekbUuids.get(tipp.gokbId) in [Constants.PERMANENTLY_DELETED, RDStore.TIPP_STATUS_REMOVED.value]}")
                                tipp.status = RDStore.TIPP_STATUS_REMOVED
                                tipp.save()
                                log.info("marked as deleted ${IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :removed, ie.lastUpdated = :now where ie.tipp = :tipp and ie.status != :removed', [removed: RDStore.TIPP_STATUS_REMOVED, tipp: tipp, now: new Date()])} issue entitlements")
                            }
                        }
                    }
                }
            }
            http.close()
            /*
            if(wekbUuids) {
                GParsPool.withPool(8) { pool ->
                    wekbUuids.eachParallel { String key, String status ->
                        TitleInstancePackagePlatform.withTransaction {
                            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbIdAndStatusNotEqual(key, RDStore.TIPP_STATUS_REMOVED)
                            if (tipp) {
                                tipp.status = RDStore.TIPP_STATUS_REMOVED
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
                        toDelete.collate(50).each { List<TitleInstancePackagePlatform> subList ->
                            deletionService.deleteTIPPsCascaded(subList)
                        }
                    }
                    else log.info("no titles to delete")
                }
            }
            */
        })
    }

    /**
     * Matches the subscription holdings against the package stock where a pending change configuration for new title has been set to auto accept. This method
     * fetches those packages where auto-accept has been configured and inserts missing titles which should have been registered already on sync run but
     * failed to do so because of bugs
     */
    @Transactional
    void matchPackageHoldings(Long pkgId) {
        Sql sql = GlobalService.obtainSqlConnection()
        sql.withTransaction {
            List subscriptionPackagesConcerned = sql.rows("select sp_sub_fk, sp_pkg_fk, sub_has_perpetual_access, sub_holding_selection_rv_fk " +
                    "from subscription_package join subscription on sp_sub_fk = sub_id " +
                    "where sp_pkg_fk = :pkgId",
            [pkgId: pkgId])
            subscriptionPackagesConcerned.eachWithIndex { GroovyRowResult row, int ax ->
                Set<Long> subIds = [row['sp_sub_fk']]
                List inheritingSubs = sql.rows("select sub_id from subscription join audit_config on auc_reference_id = sub_parent_sub_fk where auc_reference_field = 'holdingSelection' and sub_parent_sub_fk = :parent", [parent: row['sp_sub_fk']])
                subIds.addAll(inheritingSubs.collect { GroovyRowResult inherit -> inherit['sub_id'] })
                boolean perpetualAccess = row['sub_has_perpetual_access'], entire = row['sub_holding_selection_rv_fk'] == RDStore.SUBSCRIPTION_HOLDING_ENTIRE.id
                subIds.each { Long subId ->
                    log.debug("now processing package ${subId}:${pkgId}")
                    if(entire)
                        packageService.bulkAddHolding(sql, subId, pkgId, perpetualAccess)
                    log.debug("${sql.executeUpdate('update issue_entitlement set ie_status_rv_fk = tipp_status_rv_fk from title_instance_package_platform where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk != tipp_status_rv_fk and ie_status_rv_fk != :removed', [subId: subId, removed: RDStore.TIPP_STATUS_REMOVED.id])} rows updated")
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
                        PersonRole.executeUpdate('delete from PersonRole pr where pr.org = :org', [org: obj])
                        Identifier.executeUpdate('delete from Identifier id where id.org = :org', [org: obj])
                        Address.executeUpdate('delete from Address a where a.org = :org', [org: obj])
                        Contact.executeUpdate('delete from Contact c where c.org = :org', [org: obj])
                        OrgProperty.executeUpdate('delete from OrgProperty op where op.owner = :org', [org: obj])
                        DocContext.executeUpdate('update DocContext dc set dc.targetOrg = null where dc.targetOrg = :org', [org: obj])
                        DocContext.executeUpdate('update DocContext dc set dc.org = (select doc.owner from Doc doc where doc = dc.owner) where dc.org = :org', [org: obj])
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

    /**
     * Correction method to trigger again inhertis for unset audit configs due to bugs
     * @param field the field whose inheritance / audit should be triggered
     */
    @Transactional
    void retriggerInheritance(String field) {
        String query = "update Subscription s set s.${field} = (select parent.${field} from Subscription parent where parent = s.instanceOf) where s.instanceOf != null and exists(select auc.id from AuditConfig auc where auc.referenceId = s.instanceOf.id and auc.referenceClass = '${Subscription.class.name}' and auc.referenceField = '${field}')"
        log.debug("updated subscriptions: ${Subscription.executeUpdate(query)}")
    }
}
