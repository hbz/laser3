package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1738761469745-1") {
        addColumn(tableName: "org") {
            column(name: "org_supported_library_system_rv_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1738761469745-2") {
        addForeignKeyConstraint(baseColumnNames: "org_supported_library_system_rv_fk", baseTableName: "org", constraintName: "FKp4sy7q3yubj691f8j9601vpw8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1738761469745-3") {
        dropForeignKeyConstraint(baseTableName: "org", constraintName: "fk1aee4bda6253f")
    }

    changeSet(author: "klober (generated)", id: "1738761469745-4") {
        dropColumn(columnName: "org_retirement_date", tableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1738761469745-5") {
        dropColumn(columnName: "org_status_rv_fk", tableName: "org")
    }

    changeSet(author: "klober (modified)", id: "1738761469745-6") {
        grailsChange {
            change {
                int c1 = sql.executeUpdate("delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'org.status')")
                int c2 = sql.executeUpdate("delete from refdata_category where rdc_description = 'org.status'")

                String cc = 'removed refdata_value: ' + c1 + ' / removed refdata_category: ' + c2
                confirm(cc)
                changeSet.setComments(cc)
            }
        }
    }
}
