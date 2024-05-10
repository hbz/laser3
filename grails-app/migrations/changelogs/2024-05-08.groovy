package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1715197267552-1") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_address_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715197267552-2") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_person_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715197267552-3") {
        addForeignKeyConstraint(baseColumnNames: "surorg_address_fk", baseTableName: "survey_org", constraintName: "FK92a46nyjyfsqyvtyuugj3u631", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "adr_id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1715197267552-4") {
        addForeignKeyConstraint(baseColumnNames: "surorg_person_fk", baseTableName: "survey_org", constraintName: "FKn783yxocre1e6atmoqomixul6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prs_id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1715332331238-5") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_leitkriterium", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715332331238-6") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_leitweg_id", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715332331238-7") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_portal_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715332331238-8") {
        addForeignKeyConstraint(baseColumnNames: "surorg_e_invoice_portal_fk", baseTableName: "survey_org", constraintName: "FKga099l0mn5ga9m5wsg7kd5l4f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }


}
