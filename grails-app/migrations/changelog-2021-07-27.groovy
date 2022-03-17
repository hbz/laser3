databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1627370771106-1") {
        createTable(tableName: "wf_condition") {
            column(autoIncrement: "true", name: "wfc_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_conditionPK")
            }

            column(name: "wfc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfc_checkbox1", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfc_date1_title", type: "VARCHAR(255)")

            column(name: "wfc_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfc_date2_title", type: "VARCHAR(255)")

            column(name: "wfc_checkbox2_is_trigger", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfc_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfc_checkbox2_title", type: "VARCHAR(255)")

            column(name: "wfc_date2", type: "TIMESTAMP")

            column(name: "wfc_date1", type: "TIMESTAMP")

            column(name: "wfc_checkbox1_title", type: "VARCHAR(255)")

            column(name: "wfc_checkbox1_is_trigger", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfc_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfc_type", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "wfc_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfc_description", type: "TEXT")

            column(name: "wfc_checkbox2", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-2") {
        createTable(tableName: "wf_condition_prototype") {
            column(autoIncrement: "true", name: "wfcp_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_condition_prototypePK")
            }

            column(name: "wfcp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_checkbox1", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_date1_title", type: "VARCHAR(255)")

            column(name: "wfcp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_date2_title", type: "VARCHAR(255)")

            column(name: "wfcp_checkbox2_is_trigger", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_checkbox2_title", type: "VARCHAR(255)")

            column(name: "wfcp_date2", type: "TIMESTAMP")

            column(name: "wfcp_date1", type: "TIMESTAMP")

            column(name: "wfcp_checkbox1_title", type: "VARCHAR(255)")

            column(name: "wfcp_checkbox1_is_trigger", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_type", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_description", type: "TEXT")

            column(name: "wfcp_checkbox2", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-3") {
        createTable(tableName: "wf_task") {
            column(autoIncrement: "true", name: "wft_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_taskPK")
            }

            column(name: "wft_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_condition_fk", type: "BIGINT")

            column(name: "wft_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wft_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wft_child_fk", type: "BIGINT")

            column(name: "wft_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wft_prototype_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_next_fk", type: "BIGINT")

            column(name: "wft_comment", type: "TEXT")

            column(name: "wft_priority_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-4") {
        createTable(tableName: "wf_task_prototype") {
            column(autoIncrement: "true", name: "wftp_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_task_prototypePK")
            }

            column(name: "wftp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wftp_condition_fk", type: "BIGINT")

            column(name: "wftp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wftp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wftp_child_fk", type: "BIGINT")

            column(name: "wftp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wftp_next_fk", type: "BIGINT")

            column(name: "wftp_priority_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wftp_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-5") {
        createTable(tableName: "wf_workflow") {
            column(autoIncrement: "true", name: "wfw_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_workflowPK")
            }

            column(name: "wfw_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfw_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfw_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfw_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfw_child_fk", type: "BIGINT")

            column(name: "wfw_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfw_prototype_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfw_comment", type: "TEXT")

            column(name: "wfw_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfw_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfw_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-6") {
        createTable(tableName: "wf_workflow_prototype") {
            column(autoIncrement: "true", name: "wfwp_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_workflow_prototypePK")
            }

            column(name: "wfwp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfwp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfwp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfwp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfwp_child_fk", type: "BIGINT")

            column(name: "wfwp_description", type: "TEXT")

            column(name: "wfwp_state_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }



    changeSet(author: "klober (generated)", id: "1627370771106-7") {
        addForeignKeyConstraint(baseColumnNames: "wft_priority_rv_fk", baseTableName: "wf_task", constraintName: "FK1rsix0jyqxs0rbxcl3qx4jw5c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-8") {
        addForeignKeyConstraint(baseColumnNames: "wfw_child_fk", baseTableName: "wf_workflow", constraintName: "FK7sjlxxy85kq0o5ncj1v48m3ki", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-9") {
        addForeignKeyConstraint(baseColumnNames: "wfwp_state_rv_fk", baseTableName: "wf_workflow_prototype", constraintName: "FK8pxghmhf08w884oxp4bcehtas", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-10") {
        addForeignKeyConstraint(baseColumnNames: "wft_prototype_fk", baseTableName: "wf_task", constraintName: "FK9fndcq5rv7jej3wwwnd0xb5ee", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-11") {
        addForeignKeyConstraint(baseColumnNames: "wft_child_fk", baseTableName: "wf_task", constraintName: "FKa93gneffyahjs49jjt7315nn1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-12") {
        addForeignKeyConstraint(baseColumnNames: "wfw_subscription_fk", baseTableName: "wf_workflow", constraintName: "FKb37r6ed42wgvqkyjy79p4u2qy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-13") {
        addForeignKeyConstraint(baseColumnNames: "wftp_next_fk", baseTableName: "wf_task_prototype", constraintName: "FKc91sx7fmcgynvs4w4yocvfpy4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-14") {
        addForeignKeyConstraint(baseColumnNames: "wft_status_rv_fk", baseTableName: "wf_task", constraintName: "FKds5ygqwjuej407b75pj7iq51", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-15") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_prototype_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-16") {
        addForeignKeyConstraint(baseColumnNames: "wft_next_fk", baseTableName: "wf_task", constraintName: "FKe74m60asqwupbq5oh5m8si8rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-17") {
        addForeignKeyConstraint(baseColumnNames: "wfw_status_rv_fk", baseTableName: "wf_workflow", constraintName: "FKebfm232nq6ixykvaqkah1kh8k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-18") {
        addForeignKeyConstraint(baseColumnNames: "wfw_owner_fk", baseTableName: "wf_workflow", constraintName: "FKey6s8s0ed3pb6n5wtgrmbvsyk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-19") {
        addForeignKeyConstraint(baseColumnNames: "wft_condition_fk", baseTableName: "wf_task", constraintName: "FKfa8i3pw1mqr3y5byaq7c6mt8n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfc_id", referencedTableName: "wf_condition")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-20") {
        addForeignKeyConstraint(baseColumnNames: "wfw_prototype_fk", baseTableName: "wf_workflow", constraintName: "FKfjodf9bew6vgyc2bevgal1ms7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfwp_id", referencedTableName: "wf_workflow_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-21") {
        addForeignKeyConstraint(baseColumnNames: "wftp_child_fk", baseTableName: "wf_task_prototype", constraintName: "FKfxc13dlyehreo7pko2ovtigs0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-22") {
        addForeignKeyConstraint(baseColumnNames: "wfwp_child_fk", baseTableName: "wf_workflow_prototype", constraintName: "FKhp0725g4xyfrw8i2t5yvnsmuo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-23") {
        addForeignKeyConstraint(baseColumnNames: "wfc_status_rv_fk", baseTableName: "wf_condition", constraintName: "FKieraavh2bmih79ky23kx7c4nd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-24") {
        addForeignKeyConstraint(baseColumnNames: "wftp_priority_rv_fk", baseTableName: "wf_task_prototype", constraintName: "FKoft1xxaw3wlrmq731gab400bk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-25") {
        addForeignKeyConstraint(baseColumnNames: "wftp_condition_fk", baseTableName: "wf_task_prototype", constraintName: "FKsw7g3ptk4wvlbj2m78b4as1mx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfcp_id", referencedTableName: "wf_condition_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-26") {
        addForeignKeyConstraint(baseColumnNames: "wfc_prototype_fk", baseTableName: "wf_condition", constraintName: "FKgql6c2h76kqlsf0k3t7p7e9dp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfcp_id", referencedTableName: "wf_condition_prototype")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-27") {
        createIndex(indexName: "se_created_idx", tableName: "system_event") {
            column(name: "se_created")
        }
    }

    changeSet(author: "klober (generated)", id: "1627370771106-28") {
        dropForeignKeyConstraint(baseTableName: "wf_condition", constraintName: "FKieraavh2bmih79ky23kx7c4nd")
    }

    changeSet(author: "klober (generated)", id: "1627370771106-29") {
        dropColumn(columnName: "wfc_status_rv_fk", tableName: "wf_condition")
    }

}
