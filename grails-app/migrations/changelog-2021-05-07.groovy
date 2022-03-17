databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1620365933647-1") {
        grailsChange {
            change {
                sql.execute("update refdata_category set rdc_description = 'package.file', rdc_description_de = 'Package.File', rdc_description_en = 'Package.File' where rdc_description = 'package.scope'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1620365933647-2") {
        renameColumn(tableName: "package", oldColumnName: "pkg_scope_rv_fk", newColumnName: "pkg_file_rv_fk")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-3") {
        dropForeignKeyConstraint(baseTableName: "package", constraintName: "fkcfe53446d4a9c3d3")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-4") {
        dropColumn(columnName: "pkg_fixed_rv_fk", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-5") {
        dropColumn(columnName: "pkg_forum_id", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-6") {
        dropColumn(columnName: "pkg_list_status_rv_fk", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-7") {
        dropColumn(columnName: "pkg_list_verified_date", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-8") {
        addColumn(tableName: "package") {
            column(name: "pkg_scope_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1620365933647-9") {
        addForeignKeyConstraint(baseColumnNames: "pkg_scope_rv_fk", baseTableName: "package", constraintName: "FKb94p7yqnvpica6fg1d5i2tcl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "galffy (hand-coded)", id: "1620365933647-10") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = concat('0',rdv_value) where rdv_value::numeric::integer >= 10 and rdv_value::numeric::integer < 100 and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ddc');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620365933647-11") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = concat('00',rdv_value) where rdv_value::numeric::integer < 10 and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ddc');")
            }
            rollback {}
        }
    }
}
