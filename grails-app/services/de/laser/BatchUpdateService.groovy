package de.laser

import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import groovy.sql.Sql

@Transactional
class BatchUpdateService {

    void clearIssueEntitlements(Map configMap) {
        Sql sql = GlobalService.obtainSqlConnection()
        Map queryParams = [pkg_id: configMap.pkg_id,
                sub: sql.getDataSource().getConnection().createArrayOf('bigint', configMap.sub as Object[]),
                removed: RDStore.TIPP_STATUS_REMOVED.id]
        int offset = 0, step = 1000, total = sql.rows('select count(*) as count from issue_entitlement join subscription_package on ie_subscription_fk = sp_sub_fk where ie_subscription_fk = any(:sub) and sp_pkg_fk = :pkg_id and ie_status_rv_fk != :removed', queryParams)[0]['count']
        sql.execute('create temp table ie_to_deleted_'+configMap.sub[0]+' as ' +
                'select row_number() over(order by(ie_id)) row_id, ie_id ' +
                'from issue_entitlement join subscription_package on sp_sub_fk = ie_subscription_fk ' +
                'where ie_subscription_fk = any(:sub) and sp_pkg_fk = :pkg_id and ie_status_rv_fk != :removed', queryParams)
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
        sql.close()
    }
}
