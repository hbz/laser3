databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1633606031875-1") {
        addColumn(tableName: "reader_number") {
            column(name: "num_reference_group_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1633606031875-2") {
        addForeignKeyConstraint(baseColumnNames: "num_reference_group_rv_fk", baseTableName: "reader_number", constraintName: "FK93bh4460mq20qt7ksv6s7ihku", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "galffy (hand-coded)", id: "1633606031875-3") {
        grailsChange {
            change {
                sql.execute('update reader_number set num_reference_group_rv_fk = rdv_id from refdata_value where num_reference_group = rdv_value_de;')

            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1633606031875-4") {
        grailsChange {
            change {
                sql.execute('delete from reader_number where num_reference_group_rv_fk is null;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1633606031875-5") {
        addNotNullConstraint(columnDataType: "int8", columnName: "num_reference_group_rv_fk", tableName: "reader_number")
    }

    changeSet(author: "galffy (generated)", id: "1633606031875-6") {
        dropColumn(columnName: "num_reference_group", tableName: "reader_number")
    }
}
