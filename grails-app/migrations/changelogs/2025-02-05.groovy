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
}
