package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715152972155-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "eb_vendor_fk", tableName: "electronic_billing")
    }

    changeSet(author: "galffy (generated)", id: "1715152972155-2") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "idi_vendor_fk", tableName: "invoice_dispatch")
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-3") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_address_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-4") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_person_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-5") {
        addForeignKeyConstraint(baseColumnNames: "surorg_address_fk", baseTableName: "survey_org", constraintName: "FK92a46nyjyfsqyvtyuugj3u631", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "adr_id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-6") {
        addForeignKeyConstraint(baseColumnNames: "surorg_person_fk", baseTableName: "survey_org", constraintName: "FKn783yxocre1e6atmoqomixul6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prs_id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-7") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_leitkriterium", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-8") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_leitweg_id", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715152972155-9") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_e_invoice_portal_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1715332331238-10") {
        addForeignKeyConstraint(baseColumnNames: "surorg_e_invoice_portal_fk", baseTableName: "survey_org", constraintName: "FKga099l0mn5ga9m5wsg7kd5l4f", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "djebeniani (modified)", id: "1715332331238-11") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_name = 'Possible time of billing' where pd_hard_data = true and pd_name = 'Time of billing' and pd_type = 'java.lang.String'")
            }
            rollback {}
        }
    }

}
