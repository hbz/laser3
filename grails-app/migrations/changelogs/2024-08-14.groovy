package changelogs

import de.laser.DueDateObject

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

    changeSet(author: "klober (modified)", id: "1723617251555-8") {
        grailsChange {
            change {
                String sql = "delete from DueDateObject where oid like 'com.k_int.kbplus.%'"
                int done = DueDateObject.executeUpdate( sql )

                confirm( sql + ' -> ' + done )
                changeSet.setComments( sql + ' -> ' + done )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-9") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_license_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-10") {
        addForeignKeyConstraint(baseColumnNames: "ddo_license_fk", baseTableName: "due_date_object", constraintName: "FK7eiafhdvdli77rg8c76ycw4i7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-11") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_org_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-12") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_provider_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-13") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_vendor_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-14") {
        addForeignKeyConstraint(baseColumnNames: "ddo_vendor_fk", baseTableName: "due_date_object", constraintName: "FK62ufmo73xyg9rh6jcc6ersi99", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-15") {
        addForeignKeyConstraint(baseColumnNames: "ddo_provider_fk", baseTableName: "due_date_object", constraintName: "FKmiaru56e6kdhhc5tf9nsvs30q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-16") {
        addForeignKeyConstraint(baseColumnNames: "ddo_org_fk", baseTableName: "due_date_object", constraintName: "FKpgihkob4ltf3a2idxujsb4y8e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }
}
