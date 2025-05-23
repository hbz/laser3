package de.laser

import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

/**
 * This service is a container for methods which are resource-intensive or bulk process methods
 */
@Transactional
class BatchQueryService {

    /**
     * Adds the given set of package titles, retrieved by native database query, to the given subscription. Insertion as issue entitlements is being done by native SQL as well as it performs much better than GORM
     * @param sql the SQL connection, established at latest in the calling method
     * @param subId the ID of the subscription whose holding should be enriched by the given title set
     * @param pkgId the ID of the package whose holding should be added to the subscription
     * @param hasPerpetualAccess the flag whether the title access have been purchased perpetually
     */
    void bulkAddHolding(Sql sql, Long subId, Long pkgId, boolean hasPerpetualAccess, Long consortiumId = null, Long sourceSubId = null) {
        String perpetualAccessCol = '', perpetualAccessColHeader = '', whereClause = ''
        int bulkStep = 5000
        Map<String, Object> queryParams
        if(hasPerpetualAccess) {
            perpetualAccessColHeader = ', ie_perpetual_access_by_sub_fk'
            perpetualAccessCol = ", ${subId}"
        }
        if(consortiumId) {
            whereClause = "where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and tipp_id in (select ie_tipp_fk from issue_entitlement where ie_subscription_fk = :consortiumId and ie_status_rv_fk != :removed) and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)"
            queryParams = [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, consortiumId: consortiumId]
            Set<Long> total = sql.rows("select tipp_id from title_instance_package_platform ${whereClause}", queryParams).tipp_id
            for(int i = 0; i < total.size(); i += bulkStep) {
                Map<String, Object> subQueryParams = [idSubSet: sql.getDataSource().getConnection().createArrayOf('bigint', total.drop(i).take(bulkStep) as Object[])]
                sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk ${perpetualAccessColHeader}) " +
                        "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${subId}, tipp_id, tipp_access_start_date, tipp_access_end_date, tipp_status_rv_fk ${perpetualAccessCol} from title_instance_package_platform where tipp_id = any(:idSubSet)", subQueryParams)
                sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated) " +
                        "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = ${subId} and ie_status_rv_fk = tipp_status_rv_fk), now(), now() from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_id = any(:idSubSet)", subQueryParams)
            }
        }
        else if(sourceSubId) {
            whereClause = "where ie_subscription_fk = :sourceSubId and ie_status_rv_fk != :removed and tipp_pkg_fk = :pkgId and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)"
            //whereClauseIECoverage = "where ie_subscription_fk = :sourceSubId and tipp_pkg_fk = :pkgId and ie_status_rv_fk != :removed and not exists(select ic_id from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id where ic_ie_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)",
            //whereClauseIEPrice = "where ie_subscription_fk = :sourceSubId and tipp_pkg_fk = :pkgId and ie_status_rv_fk != :removed and not exists(select pi_id from price_item join issue_entitlement on pi_ie_fk = ie_id where pi_ie_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)"
            queryParams = [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, sourceSubId: sourceSubId]
            Set<Long> totalIEs = sql.rows("select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id ${whereClause}", queryParams).ie_id
            for(int i = 0; i < totalIEs.size(); i += bulkStep) {
                Map<String, Object> subQueryParams = [idSubSet: sql.getDataSource().getConnection().createArrayOf('bigint', totalIEs.drop(i).take(bulkStep) as Object[])]
                sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_notes ${perpetualAccessColHeader}) " +
                        "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${subId}, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_notes ${perpetualAccessCol} from issue_entitlement where ie_id = any(:idSubSet)", subQueryParams)
                sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_date_created, ic_last_updated) " +
                        "select 0, ic_ie_fk, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, now(), now() from issue_entitlement_coverage where ic_ie_fk = any(:idSubSet)", subQueryParams)
                sql.executeInsert("insert into price_item (pi_version, pi_ie_fk, pi_local_price, pi_local_currency_rv_fk, pi_date_created, pi_last_updated, pi_guid) " +
                        "select 0, pi_ie_fk, pi_local_price, pi_local_currency_rv_fk, now(), now(), concat('priceitem:',gen_random_uuid()) from price_item where pi_ie_fk = any(:idSubSet)", subQueryParams)
            }
        }
        else {
            whereClause = "where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)"
            queryParams = [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id]
            Set<Long> total = sql.rows("select tipp_id from title_instance_package_platform ${whereClause}", queryParams).tipp_id
            for(int i = 0; i < total.size(); i += bulkStep) {
                Map<String, Object> subQueryParams = [subId: subId, idSubSet: sql.getDataSource().getConnection().createArrayOf('bigint', total.drop(i).take(bulkStep) as Object[])]
                sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk ${perpetualAccessColHeader}) " +
                        "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), :subId, tipp_id, tipp_access_start_date, tipp_access_end_date, tipp_status_rv_fk ${perpetualAccessCol} from title_instance_package_platform where tipp_id = any(:idSubSet)", subQueryParams)
                sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated) " +
                        "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tc_tipp_fk and ie_subscription_fk = :subId and ie_status_rv_fk != :removed), now(), now() from tippcoverage where tc_tipp_fk = any(:idSubSet)", subQueryParams+[removed: RDStore.TIPP_STATUS_REMOVED.id])
                sql.executeInsert("insert into price_item (pi_version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid) " +
                        "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = pi_tipp_fk and ie_subscription_fk = :subId and ie_status_rv_fk != :removed), now(), now(), concat('priceitem:',gen_random_uuid()) from price_item where pi_tipp_fk = any(:idSubSet)", subQueryParams+[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
        }
        if(hasPerpetualAccess) {
            Long ownerId = Subscription.get(subId).getSubscriberRespConsortia().id
            sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), "+subId+", now(), ie_tipp_fk, "+ownerId+" from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])
        }
    }

    /**
     * Marks for the given subscriptions the entire holding of the given package as deleted. It uses
     * a temp table to process the marking in batches
     * @param configMap the request parameter map
     */
    void clearIssueEntitlements(Map configMap) {
        Sql sql = GlobalService.obtainSqlConnection()
        try {
            Map queryParams = [pkg_id: configMap.pkg_id,
                    sub: sql.getDataSource().getConnection().createArrayOf('bigint', configMap.sub as Object[]),
                    removed: RDStore.TIPP_STATUS_REMOVED.id]
            int offset = 0, step = 1000, total = sql.rows('select count(*) as count from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = any(:sub) and tipp_pkg_fk = :pkg_id and ie_status_rv_fk != :removed', queryParams)[0]['count']
            sql.execute('create temp table ie_to_deleted_'+configMap.sub[0]+' as ' +
                    'select row_number() over(order by(ie_id)) row_id, ie_id ' +
                    'from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id ' +
                    'where ie_subscription_fk = any(:sub) and tipp_pkg_fk = :pkg_id and ie_status_rv_fk != :removed', queryParams)
            sql.execute('create index on ie_to_deleted_'+configMap.sub[0]+'(row_id)')
            for(offset; offset < total; offset+=step) {
                int nextstep = offset+step
                log.debug("now processing entries ${offset}-${nextstep-1} out of ${total} records")
                sql.execute('update issue_entitlement ie set ie_status_rv_fk = :removed '+
                        'from ie_to_deleted_'+configMap.sub[0]+' td '+
                        'where td.ie_id = ie.ie_id '+
                        'and td.row_id > '+offset+' and td.row_id <= '+nextstep, [removed: RDStore.TIPP_STATUS_REMOVED.id])
                /*
                Set<Long> idSet = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription.id in (:sub) and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) and ie.status.id != :removed', queryParams, [max: step, offset: offset])
                String updateQuery = "update IssueEntitlement ie set ie.status.id = :removed where ie.id in (:idSet)"
                IssueEntitlement.executeUpdate(updateQuery, [idSet: idSet, removed: RDStore.TIPP_STATUS_REMOVED.id])
                */
            }
        }
        finally {
            sql.close()
        }
    }

    List<GroovyRowResult> longArrayQuery(String query, Map arrayParams, Map queryParams = [:]) {
        Sql sql = GlobalService.obtainSqlConnection()
        List<GroovyRowResult> result = []
        try {
            arrayParams.each { String k, v ->
                String type
                if(k.toLowerCase().contains('ids') || v[0] instanceof Long) {
                    type = 'bigint'
                }
                else {
                    type = 'varchar'
                }
                queryParams.put(k, sql.getDataSource().getConnection().createArrayOf(type, v as Object[]))
            }
            result = sql.rows(query, queryParams)
        }
        finally {
            sql.close()
        }
        result
    }
}
