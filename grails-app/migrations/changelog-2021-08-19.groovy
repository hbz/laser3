databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1629371303717-1") {
        dropForeignKeyConstraint(baseTableName: "wf_task", constraintName: "FK9fndcq5rv7jej3wwwnd0xb5ee")
    }

    changeSet(author: "klober (generated)", id: "1629371303717-2") {
        dropForeignKeyConstraint(baseTableName: "wf_condition", constraintName: "FKgql6c2h76kqlsf0k3t7p7e9dp")
    }

    changeSet(author: "klober (generated)", id: "1629371303717-3") {
        dropColumn(columnName: "wfc_prototype_fk", tableName: "wf_condition")
    }

    changeSet(author: "klober (generated)", id: "1629371303717-4") {
        dropColumn(columnName: "wft_prototype_fk", tableName: "wf_task")
    }
  
   changeSet(author: "djebeniani (generated)", id: "1629371303717-5") {
        dropColumn(columnName: "surre_finish_date", tableName: "survey_result")
    }
}

