package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1672989189973-1") {
        grailsChange {
            change {
                sql.execute("SELECT setval('public.global_record_source_grs_id_seq', COALESCE((SELECT MAX(grs_id)+1 FROM global_record_source), 1), true);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1672989189973-2") {
        grailsChange {
            change {
                sql.execute("INSERT INTO global_record_source (grs_version, grs_active, grs_identifier, grs_list_prefix, grs_name, grs_rectype, grs_type, grs_uri, grs_date_created, grs_last_updated, grs_have_up_to, grs_edit_uri) VALUES " +
                        "(0, true, (select grs_identifier from global_record_source where grs_id = 1), 'oai_cd', (select grs_name from global_record_source where grs_id = 1), 1, 'JSON', (select grs_uri from global_record_source where grs_id = 1), now(), now(), '1970-01-01 00:00:00'::timestamp, (select grs_edit_uri from global_record_source where grs_id = 1))")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-3") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_access_type_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-4") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_open_access_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-5") {
        addForeignKeyConstraint(baseColumnNames: "ie_open_access_rv_fk", baseTableName: "issue_entitlement", constraintName: "FKcajcx0oxg88wk5a2rrhgnoqi2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-6") {
        addForeignKeyConstraint(baseColumnNames: "ie_access_type_rv_fk", baseTableName: "issue_entitlement", constraintName: "FKl45p65k7799i7sog0s9nkvr0t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-7") {
        createIndex(indexName: "ie_access_type_idx", tableName: "issue_entitlement") {
            column(name: "ie_access_type_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-8") {
        createIndex(indexName: "ie_open_access_idx", tableName: "issue_entitlement") {
            column(name: "ie_open_access_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-9") {
        createIndex(indexName: "tipp_access_type_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_access_type_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1672989189973-10") {
        createIndex(indexName: "tipp_open_access_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_open_access_rv_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1672989189973-11") {
        grailsChange {
            change {
                sql.execute("delete from pending_change_configuration where pcc_setting_key_enum like '%TC%' or pcc_setting_key_enum like '%TR%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1672989189973-12") {
        grailsChange {
            change {
                sql.execute("delete from audit_config where auc_reference_field like '%TC%' or auc_reference_field like '%TR%'")
            }
            rollback {}
        }
    }
}
