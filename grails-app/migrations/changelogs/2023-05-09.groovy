package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1683666399030-1") {
        dropPrimaryKey(tableName: "mail_report")
    }

    changeSet(author: "djebeniani (generated)", id: "1683707515950-2") {
        addColumn(tableName: "mail_report") {
            column(autoIncrement: "true", name: "mr_id", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-3") {
        addPrimaryKey(columnNames: "mr_id", constraintName: "mail_reportPK", tableName: "mail_report")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-4") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_asynchronous_mail_message_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-5") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_cc_receiver", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-6") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_content", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-7") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_date_created", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-8") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_from", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-9") {
        addColumn(tableName: "mail_report") {
            column(autoIncrement: "true", name: "mr_id", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-10") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-11") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_modifed_by_owner", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-12") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_owner_org_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-13") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_receiver_org_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-14") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_reply_to", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-15") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_sent_by_system", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-16") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_sent_date", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-17") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_status_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-18") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_subject", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-19") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_survey_org_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-20") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_to", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-21") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_type_rv_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-22") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_version", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-23") {
        createIndex(indexName: "IX_mail_reportPK", tableName: "mail_report", unique: "true") {
            column(defaultValueComputed: "nextval('mail_report_mr_id_seq'::regclass)", name: "mr_id")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-24") {
        addForeignKeyConstraint(baseColumnNames: "mr_receiver_org_fk", baseTableName: "mail_report", constraintName: "FK1brds75ak2c25qy857tfb6yq0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-25") {
        addForeignKeyConstraint(baseColumnNames: "mr_status_rv_fk", baseTableName: "mail_report", constraintName: "FK4ow0l5ygaimre2oqghx9aas3s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-26") {
        addForeignKeyConstraint(baseColumnNames: "mr_asynchronous_mail_message_fk", baseTableName: "mail_report", constraintName: "FKfngf8vtw086m9scq6ymemrb51", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "async_mail_mess", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-27") {
        addForeignKeyConstraint(baseColumnNames: "mr_survey_org_fk", baseTableName: "mail_report", constraintName: "FKhi78vaerxevyd1u6j53pudb4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surorg_id", referencedTableName: "survey_org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-28") {
        addForeignKeyConstraint(baseColumnNames: "mr_type_rv_fk", baseTableName: "mail_report", constraintName: "FKmpqcovj6gbhef0etmr38w5j8d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-29") {
        addForeignKeyConstraint(baseColumnNames: "mr_owner_org_fk", baseTableName: "mail_report", constraintName: "FKo70l0ekf1psohmyjb6xrck8oo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-30") {
        dropColumn(columnName: "id", tableName: "mail_report")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-31") {
        dropColumn(columnName: "version", tableName: "mail_report")
    }

    changeSet(author: "djebeniani (generated)", id: "1683666399030-32") {
        addColumn(tableName: "mail_report") {
            column(name: "mr_bcc_receiver", type: "varchar(255)")
        }
    }
}
