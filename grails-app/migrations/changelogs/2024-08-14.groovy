package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1723617251555-1") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_property_oid", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-2") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_subscription_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-3") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_survey_info_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-4") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_task_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-5") {
        addForeignKeyConstraint(baseColumnNames: "ddo_subscription_fk", baseTableName: "due_date_object", constraintName: "FKi407g2ds7wauntg1dhx18rgxi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-6") {
        addForeignKeyConstraint(baseColumnNames: "ddo_survey_info_fk", baseTableName: "due_date_object", constraintName: "FK70o29ja9fo027co114s6ikkfd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surin_id", referencedTableName: "survey_info", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-7") {
        addForeignKeyConstraint(baseColumnNames: "ddo_task_fk", baseTableName: "due_date_object", constraintName: "FKfisaip13g3kqwe6ugvs2w1vcr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tsk_id", referencedTableName: "task", validate: "true")
    }

}
