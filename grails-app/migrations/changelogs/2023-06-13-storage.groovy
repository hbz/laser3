package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1686659635654-1") {
        createTable(tableName: "del_combo") {
            column(autoIncrement: "true", name: "delc_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "del_comboPK")
            }

            column(name: "delc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "delc_trace_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "delc_acc_org_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1686659635654-2") {
        createIndex(indexName: "delc_org_trace_idx", tableName: "del_combo") {
            column(name: "delc_trace_fk")

            column(name: "delc_acc_org_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686659635654-3") {
        addForeignKeyConstraint(baseColumnNames: "delc_trace_fk", baseTableName: "del_combo", constraintName: "FKjw1rtkxwh6cp9531wc9vks757", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "do_id", referencedTableName: "deleted_object", validate: "true")
    }
}
