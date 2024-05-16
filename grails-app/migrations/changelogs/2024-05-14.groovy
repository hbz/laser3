package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715677834372-1") {
        createIndex(indexName: "or_shared_from_idx", tableName: "org_role") {
            column(name: "or_shared_from_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715677834372-2") {
        createIndex(indexName: "pr_shared_from_idx", tableName: "provider_role") {
            column(name: "pr_shared_from_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715677834372-3") {
        createIndex(indexName: "vr_shared_from_idx", tableName: "vendor_role") {
            column(name: "vr_shared_from_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715677834372-4") {
        addColumn(tableName: "survey_info") {
            column(name: "surin_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715677834372-5") {
        addForeignKeyConstraint(baseColumnNames: "surin_provider_fk", baseTableName: "survey_info", constraintName: "FKb8hry8tepuv55hdg8sgc2g2de", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }
}
