package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1672837464906-1") {
        dropTable(tableName: "change_notification_queue_item")
    }

    changeSet(author: "galffy (hand-coded)", id: "1672837464906-2") {
        grailsChange {
            change {
                sql.execute('delete from pending_change where pc_lic_fk is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1672837464906-3") {
        grailsChange {
            change {
                sql.execute('delete from pending_change where pc_payload is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-4") {
        dropForeignKeyConstraint(baseTableName: "pending_change", constraintName: "fk65cbdf5897738e0e")
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-5") {
        dropColumn(columnName: "pc_change_doc_oid", tableName: "pending_change")
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-6") {
        dropColumn(columnName: "pc_change_target_oid", tableName: "pending_change")
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-7") {
        dropColumn(columnName: "pc_change_type", tableName: "pending_change")
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-8") {
        dropColumn(columnName: "pc_lic_fk", tableName: "pending_change")
    }

    changeSet(author: "galffy (generated)", id: "1672837464906-9") {
        dropColumn(columnName: "pc_payload", tableName: "pending_change")
    }

    changeSet(author: "galffy (hand-coded)", id: "1672837464906-10") {
        grailsChange {
            change {
                sql.execute("delete from pending_change WHERE pc_sub_fk is not null and pc_msg_token != 'pendingChange.message_SU_NEW_01'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1672837464906-11") {
        grailsChange {
            change {
                sql.execute("delete from pending_change WHERE pc_msg_token like '%message_TC%' or pc_msg_token like '%message_TR%'")
            }
            rollback {}
        }
    }
}
