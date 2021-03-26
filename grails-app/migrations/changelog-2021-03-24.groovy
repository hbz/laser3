databaseChangeLog = {

    changeSet(author: "agalffy (generated)", id: "1616594744619-1") {
        dropForeignKeyConstraint(baseTableName: "fact", constraintName: "fk2fd66c4cb39ba6")
    }

    changeSet(author: "agalffy (generated)", id: "1616594744619-2") {
        addForeignKeyConstraint(baseColumnNames: "related_title_id", baseTableName: "fact", constraintName: "FK4lmn5qtfv739joi03n53kcgnw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (generated)", id: "1616594744619-3") {
        dropIndex(indexName: "fact_access_idx", tableName: "fact")

        createIndex(indexName: "fact_access_idx", tableName: "fact") {
            column(name: "related_title_id")

            column(name: "inst_id")

            column(name: "supplier_id")
        }
    }
}
