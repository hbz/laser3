databaseChangeLog = {

    changeSet(author: "djebeniani (modified)", id: "1610973422255-1") {
        grailsChange {
            change {
                sql.execute("DELETE FROM issue_entitlement_group_item where igi_ie_fk in (select ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id WHERE rdv_value = 'Deleted')")
            }
            rollback {}
        }
    }

}
