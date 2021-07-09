databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1625727149064-1") {
        createTable(tableName: "wf_sequence") {
            column(autoIncrement: "true", name: "wfs_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_sequencePK")
            }

            column(name: "wfs_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfs_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfs_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfs_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfs_prototype_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfs_comment", type: "TEXT")

            column(name: "wfs_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfs_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfs_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfs_head_fk", type: "BIGINT")

            column(name: "wfs_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1625727149064-2") {
        createTable(tableName: "wf_sequence_prototype") {
            column(autoIncrement: "true", name: "wfsp_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_sequence_prototypePK")
            }

            column(name: "wfsp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfsp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfsp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfsp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfsp_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfsp_description", type: "TEXT")

            column(name: "wfsp_head_fk", type: "BIGINT")
        }
    }

    changeSet(author: "klober (generated)", id: "1625727149064-3") {
        createTable(tableName: "wf_task") {
            column(autoIncrement: "true", name: "wft_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_taskPK")
            }

            column(name: "wft_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wft_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

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

            column(name: "wft_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wft_description", type: "TEXT")

            column(name: "wft_head_fk", type: "BIGINT")
        }
    }

    changeSet(author: "klober (generated)", id: "1625727149064-4") {
        createTable(tableName: "wf_task_prototype") {
            column(autoIncrement: "true", name: "wftp_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "wf_task_prototypePK")
            }

            column(name: "wftp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wftp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wftp_next_fk", type: "BIGINT")

            column(name: "wftp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wftp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wftp_priority_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wftp_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wftp_description", type: "TEXT")

            column(name: "wftp_head_fk", type: "BIGINT")
        }
    }

    changeSet(author: "klober (generated)", id: "1625727149064-5") {
        addForeignKeyConstraint(baseColumnNames: "wft_priority_rv_fk", baseTableName: "wf_task", constraintName: "FK1rsix0jyqxs0rbxcl3qx4jw5c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-6") {
        addForeignKeyConstraint(baseColumnNames: "wfsp_head_fk", baseTableName: "wf_sequence_prototype", constraintName: "FK4f5pplyfphvsoci87c5eqimkn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-7") {
        addForeignKeyConstraint(baseColumnNames: "wfsp_type_rv_fk", baseTableName: "wf_sequence_prototype", constraintName: "FK4ukc16fyi7apyswf5si53tk08", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-8") {
        addForeignKeyConstraint(baseColumnNames: "wfs_type_rv_fk", baseTableName: "wf_sequence", constraintName: "FK6l6cxsclpabrvanxfndehjh8e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-9") {
        addForeignKeyConstraint(baseColumnNames: "wfs_prototype_fk", baseTableName: "wf_sequence", constraintName: "FK8hcup9ssrsm2s8xtoe3d42qk6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfsp_id", referencedTableName: "wf_sequence_prototype")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-10") {
        addForeignKeyConstraint(baseColumnNames: "wft_prototype_fk", baseTableName: "wf_task", constraintName: "FK9fndcq5rv7jej3wwwnd0xb5ee", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-11") {
        addForeignKeyConstraint(baseColumnNames: "wftp_type_rv_fk", baseTableName: "wf_task_prototype", constraintName: "FKafwf96y0l9wkhi0b1ns8ccxpb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-12") {
        addForeignKeyConstraint(baseColumnNames: "wftp_next_fk", baseTableName: "wf_task_prototype", constraintName: "FKc91sx7fmcgynvs4w4yocvfpy4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-13") {
        addForeignKeyConstraint(baseColumnNames: "wft_status_rv_fk", baseTableName: "wf_task", constraintName: "FKds5ygqwjuej407b75pj7iq51", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-14") {
        addForeignKeyConstraint(baseColumnNames: "wft_next_fk", baseTableName: "wf_task", constraintName: "FKe74m60asqwupbq5oh5m8si8rg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-15") {
        addForeignKeyConstraint(baseColumnNames: "wftp_head_fk", baseTableName: "wf_task_prototype", constraintName: "FKhc1bdqcgirgyffc9a66se16x1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wftp_id", referencedTableName: "wf_task_prototype")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-16") {
        addForeignKeyConstraint(baseColumnNames: "wfs_status_rv_fk", baseTableName: "wf_sequence", constraintName: "FKicwb82uoh5xfgqcm7u34eto4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-17") {
        addForeignKeyConstraint(baseColumnNames: "wfs_head_fk", baseTableName: "wf_sequence", constraintName: "FKklnghjo9n6fipouavgdadegr1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-18") {
        addForeignKeyConstraint(baseColumnNames: "wfs_subscription_fk", baseTableName: "wf_sequence", constraintName: "FKodekunk1d02bjd26vr0qrbdh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-19") {
        addForeignKeyConstraint(baseColumnNames: "wftp_priority_rv_fk", baseTableName: "wf_task_prototype", constraintName: "FKoft1xxaw3wlrmq731gab400bk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-20") {
        addForeignKeyConstraint(baseColumnNames: "wft_head_fk", baseTableName: "wf_task", constraintName: "FKq442ado5p6y5v42punphxa015", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wft_id", referencedTableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1625727149064-21") {
        addForeignKeyConstraint(baseColumnNames: "wft_type_rv_fk", baseTableName: "wf_task", constraintName: "FKsalsxxcl86anjaylv85duh3kn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }
}
