databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1661345644379-1") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_license_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-2") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_org_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-3") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_target_role_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-4") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_target_type_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-5") {
        addColumn(tableName: "wf_workflow_prototype") {
            column(name: "wfwp_target_role_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-6") {
        addColumn(tableName: "wf_workflow_prototype") {
            column(name: "wfwp_target_type_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1661345644379-7") {
        addForeignKeyConstraint(baseColumnNames: "wfw_org_fk", baseTableName: "wf_workflow", constraintName: "FK60ryw8cxgt9crqlv33yunphqm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-8") {
        addForeignKeyConstraint(baseColumnNames: "wfwp_target_role_rv_fk", baseTableName: "wf_workflow_prototype", constraintName: "FK9r5osy6rl2vggt2649c69k840", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-9") {
        addForeignKeyConstraint(baseColumnNames: "wfw_target_type_rv_fk", baseTableName: "wf_workflow", constraintName: "FKg0fg3ajcu7jlyfbsysye4os7d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-10") {
        addForeignKeyConstraint(baseColumnNames: "wfwp_target_type_rv_fk", baseTableName: "wf_workflow_prototype", constraintName: "FKgmnhu1lm94viakx9s6ttg39mc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-11") {
        addForeignKeyConstraint(baseColumnNames: "wfw_target_role_rv_fk", baseTableName: "wf_workflow", constraintName: "FKj7s18m5s6u5kamssia0u5ca1e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-12") {
        addForeignKeyConstraint(baseColumnNames: "wfw_license_fk", baseTableName: "wf_workflow", constraintName: "FKqxa5viqjgalnhcj23vua0k027", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1661345644379-13") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "wfw_subscription_fk", tableName: "wf_workflow")
    }

}
