package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1727092155564-1") {
        dropColumn(columnName: "dc_domain", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-2") {
        addColumn(tableName: "task") {
            column(name: "tsk_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-3") {
        addForeignKeyConstraint(baseColumnNames: "tsk_tipp_fk", baseTableName: "task", constraintName: "FKbncag6buf8esq1khwm4du5xws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-4") {
        createIndex(indexName: "tsk_tipp_idx", tableName: "task") {
            column(name: "tsk_tipp_fk")
        }
    }
}
