package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1683199515302-1") {
        grailsChange {
            change {
                sql.execute("delete from issue_entitlement_change where iec_id in (select iec_id from issue_entitlement_change join title_change on iec_tic_fk = tic_id where tic_event = 'pendingChange.message_TP03')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1683199515302-2") {
        grailsChange {
            change {
                sql.execute("delete from audit_config where auc_reference_field = 'pendingChange.message_TP03'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1683199515302-3") {
        grailsChange {
            change {
                sql.execute("delete from pending_change_configuration where pcc_setting_key_enum = 'pendingChange.message_TP03'")
            }
            rollback {}
        }
    }

}
