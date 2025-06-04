package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.addressbook.PersonRole
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.config.ConfigMapper
import de.laser.exceptions.SyncException
import de.laser.http.BasicHttpClient
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.ShareSupport
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.properties.VendorProperty
import de.laser.remote.GlobalRecordSource
import de.laser.storage.Constants
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyVendorResult
import de.laser.utils.DateUtils
import de.laser.wekb.InvoicingVendor
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.ProviderRole
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import groovyx.gpars.GParsPool
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Session
import org.hibernate.exception.ConstraintViolationException

import java.sql.Connection
import java.time.Duration
import java.util.concurrent.ExecutorService

/**
 * This service handles bulk and cleanup operations, testing areas and debug information
 */
//@Transactional
class YodaService {

    BatchQueryService batchQueryService
    ContextService contextService
    DeletionService deletionService
    GlobalSourceSyncService globalSourceSyncService
    GlobalService globalService
    ExecutorService executorService
    ProviderService providerService
    VendorService vendorService

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
     * Cleans {@link IssueEntitlement}s which should not exist because the issue entitlements of the parents hold
     */
    void cleanupIssueEntitlements() {
        Set<Subscription> subsConcerned = Subscription.executeQuery("select s from Subscription s where (s.holdingSelection = :entire or (s.holdingSelection = :partial and exists(select ac from AuditConfig ac where ac.referenceClass = '"+Subscription.class.name+"' and ac.referenceId = s.instanceOf.id and ac.referenceField = 'holdingSelection'))) and s.instanceOf != null", [partial: RDStore.SUBSCRIPTION_HOLDING_PARTIAL, entire: RDStore.SUBSCRIPTION_HOLDING_ENTIRE])
        Sql storageSql = GlobalService.obtainStorageSqlConnection(), sql = GlobalService.obtainSqlConnection()
        Connection arrayConn = sql.getDataSource().getConnection()
        int processedTotal = 0
        //in order to distribute memory load
        try {
            subsConcerned.eachWithIndex { Subscription s, int si ->
                Set<Map<String, Object>> data = IssueEntitlement.executeQuery("select new map(ie.version as version, now() as dateCreated, now() as lastUpdated, ie.globalUID as oldGlobalUID, coalesce(ie.dateCreated, ie.lastUpdated, '1970-01-01') as oldDateCreated, coalesce(ie.lastUpdated, ie.dateCreated, '1970-01-01') as oldLastUpdated, '"+IssueEntitlement.class.name+"' as oldObjectType, ie.id as oldDatabaseId) from IssueEntitlement ie where ie.subscription = :subConcerned", [subConcerned: s])
                int offset = 0, step = 5000, total = data.size()
                processedTotal += data.size()
                String query = "insert into deleted_object (do_version, do_old_date_created, do_old_last_updated, do_date_created, do_last_updated, do_old_object_type, do_old_database_id, do_old_global_uid, do_old_name, do_ref_package_wekb_id, do_ref_title_wekb_id, do_ref_subscription_uid) values (:version, :oldDateCreated, :oldLastUpdated, :dateCreated, :lastUpdated, :oldObjectType, :oldDatabaseId, :oldGlobalUID, :oldName, :referencePackageWekbId, :referenceTitleWekbId, :referenceSubscriptionUID)"
                Set<Long> toDelete = []
                log.debug("now processing entry subscription ${si+1} out of ${subsConcerned.size()} for ${total} records, processed in total: ${processedTotal}")
                if(data) {
                    storageSql.withBatch(step, query) { BatchingPreparedStatementWrapper stmt ->
                        for (offset; offset < total; offset++) {
                            Map<String, Object> ieTrace = data[offset]
                            TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.executeQuery('select ie.tipp from IssueEntitlement ie where ie.id = :ieId', [ieId: ieTrace.oldDatabaseId])[0]
                            ieTrace.oldName = tipp.name
                            ieTrace.referencePackageWekbId = tipp.pkg.gokbId
                            ieTrace.referenceTitleWekbId = tipp.gokbId
                            ieTrace.refereceSubscriptionUID = s.globalUID
                            stmt.addBatch(ieTrace)
                            if(offset % step == 0 && offset > 0) {
                                log.debug("reached ${offset} rows")
                            }
                        }
                    }
                    toDelete.addAll(data.oldDatabaseId)
                    toDelete.collate(65000).each { List<Long> part ->
                        sql.execute("delete from price_item where pi_ie_fk = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                        sql.execute("delete from permanent_title where pt_ie_fk = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                        sql.execute("delete from issue_entitlement_coverage where ic_ie_fk = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                        sql.execute("delete from issue_entitlement_group_item where igi_ie_fk = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                        sql.execute("update cost_item set ci_e_fk = null where ci_e_fk = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                        sql.execute("delete from issue_entitlement where ie_id = any(:toDelete)", [toDelete: arrayConn.createArrayOf('bigint', part as Object[])])
                    }
                }
                globalService.cleanUpGorm()
            }
        }
        finally {
            storageSql.close()
            sql.close()
        }
    }

    void automergeProviders() {
        Map<String, String> mergers = [
                "provider:f3dfbf45-1217-4c99-9582-b546ea143dd2": "provider:3ae993d5-f639-4190-ad13-37ea735fffc7",
                "provider:88bc5ebf-f409-4238-8732-8d8ccf9bb3ff": "provider:bdfb7cf4-7233-447d-b8ef-a9721184fbef",
                "provider:7e2a63e5-207c-4b3c-a703-e42dde46bd95": "provider:93c7fe0a-69b2-4ee3-a7dd-7b67afed66d5",
                "provider:8b98cf58-3efa-49a8-a0d7-95d6c2522ab1": "provider:9001d35d-a632-4687-bff3-275bd31c6c19",
                "provider:74eee2d9-bea3-4249-a41e-351cec5dc285": "provider:0b9a0e21-8449-46a1-843f-7799cd73434c",
                "provider:01da6a6d-0785-4907-9958-5c76aca9ea9c": "provider:f7f86a9a-9274-459c-97db-b3665d940c75",
                "provider:91b929d5-8b83-4aca-9f88-dad8c3ea920e": "provider:2fdf7cb8-dc1d-4729-bd8b-e96c3c92df92",
                "provider:395781b4-896c-40ae-965b-734eb8e0058e": "provider:ed6c71c7-e2ed-4bdb-a463-71b70eeeb6fe",
                "provider:52be47fc-44de-400d-b718-6d449c714cfc": "provider:f9a91c40-a02e-423f-bd97-e68d3b398b4b",
                "provider:40a23493-f750-4547-aa80-b4e84e58647a": "provider:e6541cd9-8ae3-4b99-a801-ae3febf50cee",
                "provider:c47229f9-0c8a-4131-89bf-24c92b1aa26f": "provider:e6541cd9-8ae3-4b99-a801-ae3febf50cee",
                "provider:7b975306-969c-4dcb-b224-527db4ec92f0": "provider:23b8094f-4bd8-431e-a662-1392b48de83f",
                "provider:9a6a2bfe-6b7a-48d3-8080-0dc15534cf40": "provider:ad48a3c8-5038-4745-bf5f-3fc059eb1870",
                "provider:58b42e29-8805-4851-97eb-e47f6aa2dec3": "provider:a663a8a2-159e-4553-b653-affdc773bf64",
                "provider:4752867c-6728-4a62-9726-f6b7c2d57732": "provider:a663a8a2-159e-4553-b653-affdc773bf64",
                "provider:cea84834-5ee8-415d-b0e8-d479077cb0e6": "provider:a663a8a2-159e-4553-b653-affdc773bf64",
                "provider:2507b025-7c0e-4acd-840a-153889799241": "provider:18c5804c-b70d-48f5-a79a-6ac44be6222b",
                "provider:aa596db1-cdf6-4b5b-a6fb-5e0c757a19ff": "provider:7a08730d-e11c-4545-bbd4-02879cbbfe86",
                "provider:1a27c057-34fb-42a6-8fd4-52360b08e490": "provider:bcd63ca4-ab52-4ce6-b0ba-c3201c67c32b",
                "provider:23385155-a6a5-4682-a9ff-b05ab3961356": "provider:5542aa8d-a9ac-43e6-b999-a5bd36bca649",
                "provider:d7960539-e570-4055-9a80-f4dc9040051d": "provider:6f93b85b-31ef-4dc6-95a2-a12d397e5b3e",
                "provider:cc7f12e6-cc80-436e-b1e4-dc509ca10568": "provider:54a569b6-8f9e-405d-802a-5c921af08c0c"
        ]
        mergers.each { String from, String to ->
            Provider source = Provider.findByGlobalUID(from), target = Provider.findByGlobalUID(to)
            if(source && target) {
                providerService.mergeProviders(source, target, false)
            }
        }
    }

    void cleanupLibrarySuppliers() {
        //step 0: preparatory actions and mergers
        Map<String, String> mergers = ['vendor:f9a91c40-a02e-423f-bd97-e68d3b398b4b': 'vendor:52be47fc-44de-400d-b718-6d449c714cfc', //HGV
                                       'vendor:a1f7c0ee-90fe-42c2-b677-be74d29410fb': 'vendor:e8f3f4ed-4fbf-4fbf-9fdc-eeea0da4672f', //EBSCO
                                       'vendor:4eaecda9-9a2e-4f34-966a-7f46650e12d5': 'vendor:bd01da62-126a-4fc1-8431-ca6f07a35608'] //Goethe + Schweitzer
        mergers.each { String from, String to ->
            Vendor source = Vendor.findByGlobalUID(from), target = Vendor.findByGlobalUID(to)
            if(source && target) {
                vendorService.mergeVendors(source, target, false)
            }
        }
        Set<String> lsToKeep = ["vendor:8d64c91b-88ce-4383-895f-dc350db85e0d",
                                "vendor:a9871cc3-ee3f-49fc-87e9-8e3c7341fda2",
                                "vendor:19e701cf-3c01-4cb9-9099-d4a07588fb5e",
                                "vendor:caeabadd-4a35-490e-af81-1211dd28cb0a",
                                "vendor:e75b7e91-f065-41fe-95d6-eb164bce6ed5",
                                "vendor:932e0f31-3091-459f-9faa-dd1fdd37673e",
                                "vendor:1692a17d-6fe9-46f7-ace9-59f1477f6a24",
                                "vendor:2a7e3101-5efb-444f-9c45-af7299d02512",
                                "vendor:b6c6e2fe-5403-4aba-9a9a-c7b8935e2f65",
                                "vendor:fc6ac6a2-7c45-4fc0-81b2-80fca0e47c59",
                                "vendor:0b9a0e21-8449-46a1-843f-7799cd73434c",
                                "vendor:ee89680e-ce2d-46eb-b8c2-496b8fe345b0",
                                "vendor:6088bbc5-d420-403a-99bf-8da3bc13a201",
                                "vendor:65f23d84-0d3a-4961-86e8-c6ed996a01d5",
                                "vendor:0f5817c5-f0a4-4f99-9552-8d5d73ea9846",
                                "vendor:bc7bff9c-6b6a-41ef-a34d-6f0d65f2c4c4",
                                "vendor:e8f3f4ed-4fbf-4fbf-9fdc-eeea0da4672f",
                                "vendor:97ede153-7cfd-4600-9802-162c2a0a87dd",
                                "vendor:2f362561-f119-42b7-92d7-e698d9e126de",
                                "vendor:c13a6c2e-fda7-46ff-988d-e672ee67dfa1",
                                "vendor:944277c2-ea80-4a49-b9dd-b044d76f271b",
                                "vendor:52be47fc-44de-400d-b718-6d449c714cfc",
                                "vendor:773c1e94-34aa-4dcb-aa80-846507fabbc8",
                                "vendor:38d12bed-f848-491b-9d90-6ba4e634eb0e",
                                "vendor:2ce1502c-8d8e-412d-a3ba-e0615dd87ef8",
                                "vendor:ff5899e1-b2cb-4f51-b13b-225643fe5654",
                                "vendor:b5d53e72-97d8-4866-97d0-5f9c6dbac796",
                                "vendor:273dcd22-ebda-4b21-9448-2e7a9e6c59a3",
                                "vendor:9c0577c6-6c0d-42fe-8a7e-9be301cbfd95",
                                "vendor:7633ac5a-e775-432e-af10-56c7a6c9827e",
                                "vendor:4145b76d-fc04-45a2-95e9-9dbd83016090",
                                "vendor:90b4e99c-616e-4bc1-bde8-6a49add98ec7",
                                "vendor:b8330221-f887-4eab-8291-2d8b518c45be",
                                "vendor:9e837e76-ecd8-4f9b-aa12-b63000f2ae3a",
                                "vendor:cc149fee-ecb4-4e17-84b8-c4001386f384",
                                "vendor:d392ae66-ff6c-47e3-a274-3bc3fe417aff",
                                "vendor:f52fb3d9-a767-4cc0-9b13-3edc81e21a6c",
                                "vendor:e63cafb7-77c1-4284-8d0f-03e8b79d5cb4",
                                "vendor:1c11e0cf-3023-49e9-bd4a-b5fab199769e",
                                "vendor:63c7a61d-45b3-4c43-9471-d1fdadb6caff",
                                "vendor:bd01da62-126a-4fc1-8431-ca6f07a35608",
                                "vendor:96d4f2b7-1b4f-4914-8032-6f6587f99fa9",
                                "vendor:2854b1d5-89af-4970-9724-864c3a58df9d"],
                    lsToProviderGUIDs = ["vendor:a1879cee-6087-42dc-a083-a0bf104998b0",
                                         "vendor:0dd35676-7ef2-4f44-b335-3419363d3222",
                                         "vendor:3ebd3921-7fbe-4eb5-97a4-7f7929861c71",
                                         "vendor:a508c028-e61d-4d0e-93bd-72edb2a557df",
                                         "vendor:7d484694-e960-4095-9946-ccb5479e6b97",
                                         "vendor:f9a91c40-a02e-423f-bd97-e68d3b398b4b",
                                         "vendor:0faaf3f6-7123-4fd7-9f75-a5c6684f619a",
                                         "vendor:a1a002b2-b410-412e-ad06-0c0f721a48d5",
                                         "vendor:2dbf4b63-ea15-4ee2-bb7a-df06a7a79bc9",
                                         "vendor:bc6b0944-5c6d-469b-a94b-921e4eb1a48f",
                                         "vendor:04e2ea97-f431-451e-9355-19428eb2f1d7"]
        Map<String, RefdataValue> lsConsortium = [
                "vendor:c94a234b-0ba0-4cf8-93f2-26a433362558": RefdataValue.getByValueAndCategory('Bayern-Konsortium', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:6cfbc5da-1321-48b4-961f-aefe49fca9f8": RefdataValue.getByValueAndCategory('Bayern-Konsortium', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:d189db7e-80cf-43bb-8671-ecabc1094a3f": RefdataValue.getByValueAndCategory('ZB MED', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:76e3c363-4b07-4cdb-8c80-7236cdbdd2b6": RefdataValue.getByValueAndCategory('ZB MED', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:864f7348-101c-4a72-97dd-5a833b4363c3": RefdataValue.getByValueAndCategory('FAK', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:613d2e73-3ef6-4243-bf4b-9da9fcd66441": null,
                "vendor:96f40196-9ebe-4b8a-9e53-d582d066ebec": RefdataValue.getByValueAndCategory('hbz', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:0a65f3ad-71c9-4b42-99f3-5a318e4febcd": RefdataValue.getByValueAndCategory('hbz', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:5381cfcc-bd0f-4784-bbc6-ee4139ea5077": RefdataValue.getByValueAndCategory('hebis', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:ed30a705-e150-49e9-a732-1125857e46b3": RefdataValue.getByValueAndCategory('KonsBW', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:a556a42b-37c9-477a-a665-db9f76c8d515": RefdataValue.getByValueAndCategory('MPDLS', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:cbc34a19-4516-46f8-9c9d-dd04896761f4": RefdataValue.getByValueAndCategory('SUB', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:298159b0-3aa8-4a9f-9209-4ef3b2ba10f8": RefdataValue.getByValueAndCategory('SachsenKo', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:acdd6e1c-c232-41be-9701-3404d0f3c575": RefdataValue.getByValueAndCategory('SUB', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:3900e7b3-80dc-4496-b7e5-a814d8ed5619": RefdataValue.getByValueAndCategory('TIB', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:c1d2a075-5756-48e2-8ae9-a1bb5a2338dd": RefdataValue.getByValueAndCategory('hebis', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:acd59f10-d52d-4649-87f8-cdc769554562": RefdataValue.getByValueAndCategory('hebis', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:fd8599ff-80ce-48b5-aef9-22e0117abb5d": RefdataValue.getByValueAndCategory('ZBW', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS),
                "vendor:58b76622-02b6-4c26-a5a7-2537cd63e2b4": RefdataValue.getByValueAndCategory('ZBW', RDConstants.CONSORTIUM_FOR_LOCAL_SUBS)
        ]
        /*
        step 1:
        library supplier should be kept
        1.1 create in addition a property Invoice Processing if not exists and set it to Library Supplier
        */
        lsToKeep.eachWithIndex { String lsGUID, int i ->
            Vendor sourceVen = Vendor.findByGlobalUID(lsGUID)
            log.debug("now processing library supplier to keep ${i+1} out of ${lsToKeep.size()}")
            Set<VendorRole> rolesToProcess = VendorRole.findAllByVendorAndSharedFromIsNull(sourceVen)
            rolesToProcess.eachWithIndex { VendorRole sourceVR, int j ->
                log.debug("now marking subscription/license with property ${j+1} out of ${rolesToProcess.size()}")
                if(sourceVR.subscription) {
                    Subscription s = GrailsHibernateUtil.unwrapIfProxy(sourceVR.subscription)
                    setProcessingProperty(s, PropertyStore.SUB_PROP_INVOICE_PROCESSING, RDStore.INVOICE_PROCESSING_VENDOR, sourceVR.isShared)
                }
                else if(sourceVR.license) {
                    License l = GrailsHibernateUtil.unwrapIfProxy(sourceVR.license)
                    setProcessingProperty(l, PropertyStore.LIC_LICENSE_PROCESSING, RDStore.INVOICE_PROCESSING_VENDOR, sourceVR.isShared)
                }
            }
        }
        /*
        step 2:
        library supplier is in fact a provider
        steps:
        2.1 create property Invoice Property and set the value to Provider
        2.2 match to provider with same UUID (should work because aftermath of separation) and link subscription to that (convert ProviderLink to corresponding VendorLink)
        */
        Set<String> treatedGUIDs = []
        treatedGUIDs.addAll(mergers.values())
        treatedGUIDs.addAll(lsToKeep)
        treatedGUIDs.addAll(lsToProviderGUIDs)
        treatedGUIDs.addAll(lsConsortium.keySet())
        Set<Vendor> lsToProviders = Vendor.executeQuery('select v from Vendor v where v.globalUID in (:guids)', [guids: lsToProviderGUIDs])
        lsToProviders.addAll(Vendor.executeQuery('select v from Vendor v where v.globalUID not in (:guids)', [guids: treatedGUIDs]))
        lsToProviders.eachWithIndex{ Vendor sourceVen, int i ->
            log.debug("checking existence of target ...")
            String targetGUID = sourceVen.globalUID.replace('vendor', 'provider')
            //if(sourceVen.globalUID in lsToProviderGUIDs) {
                Provider targetProv = Provider.findByGlobalUID(targetGUID)
                if(!targetProv) {
                    log.debug("*** provider not found by target global UID ${targetGUID}, creating new")
                    targetProv = new Provider(guid: targetGUID, sortname: sourceVen.sortname, name: sourceVen.name)
                    targetProv.status = RefdataValue.getByValueAndCategory(sourceVen.status.value, RDConstants.PROVIDER_STATUS)
                    targetProv.save()
                }
            //}
            /*
            else {
                log.info("library supplier unclear, further checks")
                int addresses = Address.countByVendor(sourceVen)
                int docs = DocContext.countByVendor(sourceVen)
                int tasks = Task.countByVendor(sourceVen)
                int workflows = WfChecklist.countByVendor(sourceVen)
                int invoicingVendors = InvoicingVendor.countByVendor(sourceVen)
                int surveyVendors = SurveyConfigVendor.countByVendor(sourceVen)

                /*
                if(!&& !tasks && !workflows && !invoicingVendors && !surveyVendors)
                    sourceVen.delete()
                else {
                    if(addresses > 0)
                        log.error("vendor ${sourceVen.name} in use in addresses: ${addresses} occurrences!")
                    if(docs > 0)
                        log.error("vendor ${sourceVen.name} in use in docs: ${docs} occurrences!")
                    if(tasks > 0)
                        log.error("vendor ${sourceVen.name} in use in tasks: ${tasks} occurrences!")
                    if(workflows > 0)
                        log.error("vendor ${sourceVen.name} in use in workflows: ${workflows} occurrences!")
                    if(invoicingVendors > 0)
                        log.error("vendor ${sourceVen.name} in use in invoicing: ${invoicingVendors} occurrences!")
                    if(surveyVendors > 0)
                        log.error("vendor ${sourceVen.name} in use in survey configs: ${surveyVendors} occurrences!")
                }
            }
        */
        }
        lsToProviders.eachWithIndex { Vendor sourceVen, int i ->
            log.debug("now migrating library supplier to provider ${i+1} out of ${lsToProviders.size()}")
            String targetGUID = sourceVen.globalUID.replace('vendor', 'provider')
            Provider targetProv = Provider.findByGlobalUID(targetGUID)
            Map<String, Object> genericUpdateParams = [target: targetProv, source: sourceVen]
            Set<VendorRole> rolesToMigrate = VendorRole.findAllByVendorAndSharedFromIsNull(sourceVen)
            rolesToMigrate.eachWithIndex { VendorRole sourceVR, int j ->
                ProviderRole targetPVR = null
                def owner = null
                if(sourceVR.subscription)
                    targetPVR = ProviderRole.findByProviderAndSubscription(targetProv, sourceVR.subscription)
                else if(sourceVR.license)
                    targetPVR = ProviderRole.findByProviderAndLicense(targetProv, sourceVR.license)
                if(!targetPVR) {
                    log.debug("now migrating library supplier role to provider role ${j+1} out of ${rolesToMigrate.size()}")
                    targetPVR = new ProviderRole(provider: targetProv, isShared: sourceVR.isShared)
                    if(sourceVR.subscription) {
                        owner = GrailsHibernateUtil.unwrapIfProxy(sourceVR.subscription)
                        targetPVR.subscription = owner
                    }
                    else if(sourceVR.license) {
                        owner = GrailsHibernateUtil.unwrapIfProxy(sourceVR.license)
                        targetPVR.license = owner
                    }
                    if(owner) {
                        targetPVR.save()
                        if(targetPVR.isShared) {
                            owner.updateShare(targetPVR)
                        }
                    }
                }
                else {
                    log.debug("library supplier role ${j+1} out of ${rolesToMigrate.size()} already existing as provider role")
                }
                if(targetPVR.subscription) {
                    setProcessingProperty(targetPVR.subscription, PropertyStore.SUB_PROP_INVOICE_PROCESSING, RDStore.INVOICE_PROCESSING_PROVIDER, targetPVR.isShared)
                }
                else if(targetPVR.license) {
                    setProcessingProperty(targetPVR.license, PropertyStore.LIC_LICENSE_PROCESSING, RDStore.INVOICE_PROCESSING_PROVIDER, targetPVR.isShared)
                }
                sourceVR.delete()
            }
            int info = Address.executeUpdate('update Address a set a.provider = :target, a.vendor = null where a.vendor = :source', genericUpdateParams)
            log.debug("${sourceVen.name}:${sourceVen.id}: addresses updated: ${info}")
            info = DocContext.executeUpdate('update DocContext dc set dc.provider = :target, dc.vendor = null where dc.vendor = :source', genericUpdateParams)
            log.debug("${sourceVen.name}:${sourceVen.id}: doc contexts updated: ${info}")
            info = Task.executeUpdate('update Task t set t.provider = :target, t.vendor = null where t.vendor = :source', genericUpdateParams)
            log.debug("${sourceVen.name}:${sourceVen.id}: tasks updated: ${info}")
            info = WfChecklist.executeUpdate('update WfChecklist wc set wc.provider = :target, wc.vendor = null where wc.vendor = :source', genericUpdateParams)
            log.debug("${sourceVen.name}:${sourceVen.id}: workflow checklists updated: ${info}")
            info = AlternativeName.executeUpdate('update AlternativeName altname set altname.provider = :target, altname.vendor = null where altname.vendor = :source', genericUpdateParams)
            log.debug("${sourceVen.name}:${sourceVen.id}: alternative names updated: ${info}")
            info = InvoicingVendor.executeUpdate('delete from InvoicingVendor iv where iv.vendor = :vendor', [vendor: sourceVen])
            log.debug("${sourceVen.name}:${sourceVen.id}: invoicing vendor deleted: ${info}")
            info = SurveyVendorResult.executeUpdate('delete from SurveyVendorResult svr where svr.vendor = :vendor', [vendor: sourceVen])
            log.debug("${sourceVen.name}:${sourceVen.id}: survey vendor results deleted: ${info}")
            info = SurveyConfigVendor.executeUpdate('delete from SurveyConfigVendor scv where scv.vendor = :vendor', [vendor: sourceVen])
            log.debug("${sourceVen.name}:${sourceVen.id}: survey config vendors deleted: ${info}")
            //if(sourceVen.globalUID in lsToProviderGUIDs) {
                sourceVen.delete()
            //}
            globalService.cleanUpGorm()
        }
        /*
        step 3: consortia
        map consortium linking to property and set property value to refdata
        "Bayerische Staatsbibliothek München"	"vendor:c94a234b-0ba0-4cf8-93f2-26a433362558"
        "Bayern-Konsortium"	"vendor:6cfbc5da-1321-48b4-961f-aefe49fca9f8"
        "Deutsche Zentralbibliothek für Medizin (ZB MED) - Informationszentrum Lebenswissenschaften"	"vendor:d189db7e-80cf-43bb-8671-ecabc1094a3f"
        "Deutsche Zentralbibliothek für Medizin (ZB MED) - Informationszentrum Lebenswissenschaften"	"vendor:76e3c363-4b07-4cdb-8c80-7236cdbdd2b6"
        "Friedrich-Althoff-Konsortium e.V."	"vendor:864f7348-101c-4a72-97dd-5a833b4363c3"
        "GASCO (German, Austrian and Swiss Consortia Organisation) - Arbeitsgemeinschaft Deutscher, Österreichischer und Schweizer Konsortien"	"vendor:613d2e73-3ef6-4243-bf4b-9da9fcd66441"
        "hbz Konsortium Digitale Inhalte"	"vendor:96f40196-9ebe-4b8a-9e53-d582d066ebec"
        "HeBIS Konsortium Goethe Universität Frankfurt am Main"	"vendor:5381cfcc-bd0f-4784-bbc6-ee4139ea5077"
        "Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen"	"vendor:0a65f3ad-71c9-4b42-99f3-5a318e4febcd"
        "Konsortium Baden-Württemberg"	"vendor:ed30a705-e150-49e9-a732-1125857e46b3"
        "MPDL Services GmbH"	"vendor:a556a42b-37c9-477a-a665-db9f76c8d515"
        "Niedersächsische Staats- und Universitätsbibliothek Göttingen"	"vendor:cbc34a19-4516-46f8-9c9d-dd04896761f4"
        "Sachsenkonsortium"	"vendor:298159b0-3aa8-4a9f-9209-4ef3b2ba10f8"
        "SUB Göttingen Konsortien"	"vendor:acdd6e1c-c232-41be-9701-3404d0f3c575"
        "Technische Informationsbibliothek (TIB) Hannover"	"vendor:3900e7b3-80dc-4496-b7e5-a814d8ed5619"
        "Universitätsbibliothek Johann Christian Senckenberg Frankfurt"	"vendor:c1d2a075-5756-48e2-8ae9-a1bb5a2338dd"
        "Universitätsbibliothek Johann Christian Senckenberg Frankfurt am Main"	"vendor:acd59f10-d52d-4649-87f8-cdc769554562"
        "ZBW – Leibniz-Informationszentrum Wirtschaft"	"vendor:fd8599ff-80ce-48b5-aef9-22e0117abb5d"
        "ZBW – Leibniz-Informationszentrum Wirtschaft"	"vendor:58b76622-02b6-4c26-a5a7-2537cd63e2b4"
         */
        lsConsortium.each { String sourceGUID, RefdataValue localConsortiumEquivalent ->
            Vendor sourceVen = Vendor.findByGlobalUID(sourceGUID)
            if(sourceVen) {
                Set<VendorRole> rolesToMigrate = VendorRole.findAllByVendorAndSharedFromIsNull(sourceVen)
                rolesToMigrate.eachWithIndex { VendorRole sourceVR, int j ->
                    log.debug("now remapping consortium ${j} out of ${rolesToMigrate.size()}")
                    if(sourceVR.subscription) {
                        Subscription s = GrailsHibernateUtil.unwrapIfProxy(sourceVR.subscription)
                        setProcessingProperty(s, PropertyStore.SUB_PROP_PROCESSING_CONSORTIUM, localConsortiumEquivalent, sourceVR.isShared)
                    }
                    else if(sourceVR.license) {
                        License l = GrailsHibernateUtil.unwrapIfProxy(sourceVR.license)
                        setProcessingProperty(l, PropertyStore.LIC_LICENSE_PROCESSING_CONSORTIUM, localConsortiumEquivalent, sourceVR.isShared)
                    }
                    sourceVR.delete()
                }
                Map<String, Object> genericDeleteParams = [vendor: sourceVen]
                int info = DocContext.executeUpdate('delete from DocContext dc where dc.vendor = :vendor', genericDeleteParams)
                log.debug("doc contexts deleted: ${info}")
                info = Task.executeUpdate('delete from Task t where t.vendor = :vendor', genericDeleteParams)
                log.debug("tasks deleted: ${info}")
                info = WfChecklist.executeUpdate('delete from WfChecklist wc where wc.vendor = :vendor', genericDeleteParams)
                log.debug("workflow checklists deleted: ${info}")
                info = AlternativeName.executeUpdate('delete from AlternativeName altname where altname.vendor = :vendor', genericDeleteParams)
                log.debug("alternative names deleted: ${info}")
                info = SurveyVendorResult.executeUpdate('delete from SurveyVendorResult svr where svr.vendor = :vendor', genericDeleteParams)
                log.debug("survey results deleted: ${info}")
                info = SurveyConfigVendor.executeUpdate('delete from SurveyConfigVendor scv where scv.vendor = :vendor', genericDeleteParams)
                log.debug("survey vendors deleted: ${info}")
                info = VendorProperty.executeUpdate('delete from VendorProperty vp where vp.owner = :vendor', genericDeleteParams)
                log.debug("vendor properties deleted: ${info}")
                /*
                boolean blocker = SurveyVendorResult.countByVendor(sourceVen) > 0
                if(blocker) {
                    log.error("deleting of ${sourceVen.name} blocked due to existing SurveyVendors!")
                }
                */
                sourceVen.delete()
                globalService.cleanUpGorm()
            }
            else {
                log.debug("vendor ${sourceGUID} already migrated")
            }
        }
        /*
        step 4: find doublets where exists twice the same vendor link, one is shared, the other not
         */
        Set<Subscription> doubletSubs = Subscription.executeQuery('select vr.subscription from VendorRole vr where exists (select v2 from VendorRole v2 where v2.subscription = vr.subscription and v2.sharedFrom = null) and exists (select v3 from VendorRole v3 where v3.subscription = vr.subscription and v3.sharedFrom != null)')
        doubletSubs.each { Subscription doubletSub ->
            int info = VendorRole.executeUpdate('delete from VendorRole vr where vr.subscription = :sub and vr.sharedFrom = null', [sub: doubletSub])
            log.debug("doublet vendor subscription roles deleted: ${doubletSub.id}: ${info}")
        }
        Set<License> doubletLics = License.executeQuery('select vr.license from VendorRole vr where exists (select v2 from VendorRole v2 where v2.license = vr.license and v2.sharedFrom = null) and exists (select v3 from VendorRole v3 where v3.license = vr.license and v3.sharedFrom != null)')
        doubletLics.each { License doubletLic ->
            int info = VendorRole.executeUpdate('delete from VendorRole vr where vr.license = :lic and vr.sharedFrom = null', [lic: doubletLic])
            log.debug("doublet vendor license roles deleted: ${doubletLic.id}: ${info}")
        }
    }

    void setProcessingProperty(ownObj, PropertyDefinition propType, RefdataValue value, boolean isShared) {
        if(ownObj instanceof Subscription) {
            Subscription s = (Subscription) ownObj
            SubscriptionProperty property = SubscriptionProperty.findByOwnerAndType(s, propType)
            if(!property) {
                //property = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, s, propType, s.getSubscriberRespConsortia())
                boolean isPublic = false
                Org tenant = s.getSubscriberRespConsortia()
                if(s._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE, CalculatedType.TYPE_PARTICIPATION]) {
                    isPublic = true
                    tenant = s.getConsortium()
                }
                property = new SubscriptionProperty(type: propType, owner: s, isPublic: isPublic, tenant: tenant, refValue: value, note: "")
            }
            property.save()
            if(isShared) {
                if (!AuditConfig.getConfig(property, AuditConfig.COMPLETE_OBJECT)) {
                    Subscription.findAllByInstanceOf(s).each { Subscription member ->
                        member = GrailsHibernateUtil.unwrapIfProxy(member)
                        SubscriptionProperty existingProp = SubscriptionProperty.findByOwnerAndInstanceOf(member, property)
                        if (! existingProp) {
                            List<SubscriptionProperty> matchingProps = SubscriptionProperty.findAllByOwnerAndTypeAndTenant(member, propType, s.getConsortium())
                            // unbound prop found with matching type, set backref
                            if (matchingProps) {
                                matchingProps.each { SubscriptionProperty memberProp ->
                                    memberProp.instanceOf = property
                                    memberProp.isPublic = true
                                    memberProp.save()
                                }
                            }
                            else {
                                // no match found, creating new prop with backref
                                SubscriptionProperty newProp = new SubscriptionProperty(type: propType, owner: member, tenant: member.getConsortium(), note: "")
                                newProp = property.copyInto(newProp)
                                newProp.instanceOf = property
                                newProp.isPublic = true
                                newProp.save()
                            }
                        }
                    }
                    AuditConfig.addConfig(property, AuditConfig.COMPLETE_OBJECT)
                }
            }
        }
        else if(ownObj instanceof License) {
            License l = (License) ownObj
            LicenseProperty property = LicenseProperty.findByOwnerAndType(l, propType)
            if(!property) {
                //property = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, l, propType, licensee)
                boolean isPublic = false
                Org tenant = l.getLicensee()
                if(l._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE, CalculatedType.TYPE_PARTICIPATION]) {
                    isPublic = true
                    tenant = l.getLicensingConsortium()
                }
                property = new LicenseProperty(type: propType, owner: l, isPublic: isPublic, tenant: tenant, refValue: value, note: "")
            }
            property.save()
            if(isShared) {
                if (!AuditConfig.getConfig(property, AuditConfig.COMPLETE_OBJECT)) {
                    License.findAllByInstanceOf(l).each { License member ->
                        member = GrailsHibernateUtil.unwrapIfProxy(member)
                        LicenseProperty existingProp = LicenseProperty.findByOwnerAndInstanceOf(member, property)
                        if (! existingProp) {
                            List<LicenseProperty> matchingProps = LicenseProperty.findAllByOwnerAndTypeAndTenant(member, propType, member.getLicensingConsortium())
                            // unbound prop found with matching type, set backref
                            if (matchingProps) {
                                matchingProps.each { LicenseProperty memberProp ->
                                    memberProp.instanceOf = property
                                    memberProp.isPublic = true
                                    memberProp.save()
                                }
                            }
                            else {
                                // no match found, creating new prop with backref
                                LicenseProperty newProp = new LicenseProperty(type: propType, owner: member, tenant: member.getLicensingConsortium(), note: "")
                                newProp = property.copyInto(newProp)
                                newProp.instanceOf = property
                                newProp.isPublic = true
                                newProp.save()
                            }
                        }
                    }
                    AuditConfig.addConfig(property, AuditConfig.COMPLETE_OBJECT)
                }
            }
        }
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
            BasicHttpClient http = new BasicHttpClient(grs.getUri() + '/searchApi', config)
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
                    List issueEntitlements = IssueEntitlement.executeQuery("select new map(ie.id as id, concat(s.name, ' (', s.startDate, '-', s.endDate, ') (', oo.org.sortname, ')') as subscriptionName) from IssueEntitlement ie join ie.tipp tipp join ie.subscription s join s.orgRelations oo where oo.roleType in (:roleTypes) and tipp.gokbId = :wekbId and ie.status != :removed", [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER], wekbId: row.wekbId, removed: RDStore.TIPP_STATUS_REMOVED])
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
                        batchQueryService.bulkAddHolding(sql, subId, pkgId, perpetualAccess)
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
            case Provider.class.name: rectype = GlobalSourceSyncService.RECTYPE_PROVIDER
                componentType = 'Org'
                objects.addAll(Provider.findAllByStatusNotEqualAndGokbIdIsNotNull(RDStore.PROVIDER_STATUS_REMOVED))
                break
            case Vendor.class.name: rectype = GlobalSourceSyncService.RECTYPE_VENDOR
                componentType = 'Vendor'
                objects.addAll(Vendor.findAllByStatusNotEqualAndGokbIdIsNotNull(RDStore.VENDOR_STATUS_REMOVED))
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
                    //TODO implement cleanup of providers and vendors
//                    if(obj instanceof Org) {
//                        OrgRole.executeUpdate('delete from OrgRole oo where oo.org = :org', [org: obj])
//                        PersonRole.executeUpdate('delete from PersonRole pr where pr.org = :org', [org: obj])
//                        Identifier.executeUpdate('delete from Identifier id where id.org = :org', [org: obj])
//                        Address.executeUpdate('delete from Address a where a.org = :org', [org: obj])
//                        Contact.executeUpdate('delete from Contact c where c.org = :org', [org: obj])
//                        OrgProperty.executeUpdate('delete from OrgProperty op where op.owner = :org', [org: obj])
//                        DocContext.executeUpdate('update DocContext dc set dc.targetOrg = null where dc.targetOrg = :org', [org: obj])
//                        DocContext.executeUpdate('update DocContext dc set dc.org = (select doc.owner from Doc doc where doc = dc.owner) where dc.org = :org', [org: obj])
//                        deletionService.deleteOrganisation(obj, null, false)
//                    }
//                    else
                    if (obj instanceof Platform) {
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
