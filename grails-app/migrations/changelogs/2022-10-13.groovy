package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1665637834716-1") {
        createIndex(indexName: "ie_tipp_status_accept_status_idx", tableName: "issue_entitlement") {
            column(name: "ie_accept_status_rv_fk")

            column(name: "ie_tipp_fk")

            column(name: "ie_subscription_fk")

            column(name: "ie_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1665655512553-2") {
        addColumn(tableName: "doc") {
            column(name: "doc_confidentiality_rv_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1665655512553-3") {
        addForeignKeyConstraint(baseColumnNames: "doc_confidentiality_rv_fk", baseTableName: "doc", constraintName: "FKmpmjh0epusbfipg6tyk6ejn3n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }
}
