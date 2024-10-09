package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1728392757265-1") {
        addColumn(tableName: "org") {
            column(name: "org_type_rv_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1728392757265-2") {
        addForeignKeyConstraint(baseColumnNames: "org_type_rv_fk", baseTableName: "org", constraintName: "FKk3uxe03uscg8ifybv8w6r4lvd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }
}
