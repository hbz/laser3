package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1650897308135-1") {
        addColumn(tableName: "survey_info") {
            column(name: "surin_license", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1650897308135-2") {
        addColumn(tableName: "survey_info") {
            column(name: "surin_provider", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1650897308135-3") {
        addForeignKeyConstraint(baseColumnNames: "surin_license", baseTableName: "survey_info", constraintName: "FK3s4vvxi6ntckgr57w6bv7s63u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license")
    }

    changeSet(author: "djebeniani (generated)", id: "1650897308135-4") {
        addForeignKeyConstraint(baseColumnNames: "surin_provider", baseTableName: "survey_info", constraintName: "FKlson0w3je1s7amoj2x712jg00", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

}
