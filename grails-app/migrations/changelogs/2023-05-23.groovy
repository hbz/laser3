databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1684824321864-1") {
        grailsChange {
            change {
                sql.execute("delete from audit_config where auc_reference_field = 'pendingChange.message_TP02'")
            }
            rollback {}
        }
    }


}
