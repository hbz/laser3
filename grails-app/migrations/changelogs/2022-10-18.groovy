package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1666079486075-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "wfw_user_fk", tableName: "wf_workflow")
    }

    changeSet(author: "klober (generated)", id: "1666079486075-2") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file3", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-3") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-4") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file4", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-5") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-6") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file3", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-7") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-8") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file4", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-9") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1666079486075-10") {
        addForeignKeyConstraint(baseColumnNames: "wfc_file3", baseTableName: "wf_condition", constraintName: "FKohko9wystsidkwcfgy3x2sxh7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1666079486075-11") {
        addForeignKeyConstraint(baseColumnNames: "wfc_file4", baseTableName: "wf_condition", constraintName: "FK3loqb41p8ewgawd39qkncv6h1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1666079486075-12") {
        addForeignKeyConstraint(baseColumnNames: "wfcp_file3", baseTableName: "wf_condition_prototype", constraintName: "FKbeugxj8rbcygptpp1t5wu7ogq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1666079486075-13") {
        addForeignKeyConstraint(baseColumnNames: "wfcp_file4", baseTableName: "wf_condition_prototype", constraintName: "FKegx94ihblw5q6cn9sh5pxns98", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1666079486075-14") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_user_last_updated", type: "timestamp")
        }
    }
}
