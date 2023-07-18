package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1689231582345-1") {
        addColumn(tableName: "address") {
            column(name: "adr_tenant_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1689231582345-2") {
        createIndex(indexName: "adr_tenant_idx", tableName: "address") {
            column(name: "adr_tenant_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1689231582345-3") {
        addForeignKeyConstraint(baseColumnNames: "adr_tenant_fk", baseTableName: "address", constraintName: "FKq1oa0lval8d179wpihmqo6h95", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }
}
