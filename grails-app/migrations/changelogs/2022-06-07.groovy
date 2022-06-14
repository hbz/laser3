package changelogs

import de.laser.storage.RDStore
import groovy.sql.GroovyRowResult

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1654585599096-1") {
        grailsChange {
            change {
                sql.execute('delete from cost_item_group where cig_cost_item_fk in (select ci_id from cost_item where ci_e_fk in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted))', [deleted: RDStore.TIPP_STATUS_DELETED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1654585599096-2") {
        grailsChange {
            change {
                sql.execute('update cost_item set ci_copy_base = null where ci_copy_base in (select ci_id from cost_item where ci_e_fk in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted))', [deleted: RDStore.TIPP_STATUS_DELETED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1654585599096-3") {
        grailsChange {
            change {
                //looping in order to avoid sequence scans
                sql.rows('select distinct ie_subscription_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1').each { GroovyRowResult row ->
                    println("processing sub ${row['ie_subscription_fk']}")
                    //sql.execute('delete from cost_item where ci_e_fk in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted and ie_subscription_fk = :sub)', [deleted: RDStore.TIPP_STATUS_DELETED.id, removed: RDStore.TIPP_STATUS_REMOVED.id, sub: row['ie_subscription_fk']])
                    //sql.execute('delete from price_item where pi_ie_fk in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted and ie_subscription_fk = :sub)', [deleted: RDStore.TIPP_STATUS_DELETED.id, removed: RDStore.TIPP_STATUS_REMOVED.id, sub: row['ie_subscription_fk']])
                    //sql.execute('delete from issue_entitlement_coverage where ic_ie_fk in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted and ie_subscription_fk = :sub)', [deleted: RDStore.TIPP_STATUS_DELETED.id, removed: RDStore.TIPP_STATUS_REMOVED.id, sub: row['ie_subscription_fk']])
                    sql.execute('update issue_entitlement set ie_status_rv_fk = :removed where ie_id in (select ie_id from issue_entitlement where (ie_subscription_fk, ie_tipp_fk) in (select ie_subscription_fk, ie_tipp_fk from issue_entitlement group by ie_tipp_fk, ie_subscription_fk having count(ie_tipp_fk) > 1) and ie_status_rv_fk = :deleted and ie_subscription_fk = :sub)', [deleted: RDStore.TIPP_STATUS_DELETED.id, removed: RDStore.TIPP_STATUS_REMOVED.id, sub: row['ie_subscription_fk']])
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1654585599096-4") {
        grailsChange {
            change {
                //mark titles as deleted where package has been changed
                sql.execute('update issue_entitlement set ie_status_rv_fk = :removed where ie_id in (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join subscription_package on ie_subscription_fk = sp_sub_fk where sp_pkg_fk != tipp_pkg_fk and ie_status_rv_fk = :deleted)', [deleted: RDStore.TIPP_STATUS_DELETED.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

}
