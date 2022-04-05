databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1628659096581-1") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file1", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1628659096581-2") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file1_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1628659096581-3") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file1", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1628659096581-4") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file1_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1628659096581-5") {
        addForeignKeyConstraint(baseColumnNames: "wfc_file1", baseTableName: "wf_condition", constraintName: "FKm03nu3eci3lnbvnyt0i3ia0c1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1628659096581-6") {
        addForeignKeyConstraint(baseColumnNames: "wfcp_file1", baseTableName: "wf_condition_prototype", constraintName: "FKns4gcufusd76cuqw60ss5b6gy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context")
    }
}