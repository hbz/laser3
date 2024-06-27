package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1716370668136-1") {
        addColumn(tableName: "alternative_name") {
            column(name: "altname_instance_of_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716370668136-2") {
        createIndex(indexName: "altname_instanceof_idx", tableName: "alternative_name") {
            column(name: "altname_instance_of_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716370668136-3") {
        addForeignKeyConstraint(baseColumnNames: "altname_instance_of_fk", baseTableName: "alternative_name", constraintName: "FK11gigj53lsiqud5toyyg0gwl1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "altname_id", referencedTableName: "alternative_name", validate: "true")
    }
}
