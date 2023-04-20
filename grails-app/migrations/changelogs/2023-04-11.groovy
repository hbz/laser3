package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1681212470764-1") {
        createTable(tableName: "issue_entitlement_change") {
            column(autoIncrement: "true", name: "iec_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "issue_entitlement_changePK")
            }

            column(name: "iec_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iec_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "iec_sub_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iec_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "iec_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iec_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iec_action_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "iec_tic_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-2") {
        createTable(tableName: "title_change") {
            column(autoIncrement: "true", name: "tic_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "title_changePK")
            }

            column(name: "tic_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tic_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "tic_old_date_value", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "tic_new_value", type: "TEXT")

            column(name: "tic_old_value", type: "TEXT")

            column(name: "tic_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "tic_event", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "tic_tipp_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tic_new_ref_value_rv_fk", type: "BIGINT")

            column(name: "tic_new_date_value", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "tic_old_ref_value_rv_fk", type: "BIGINT")

            column(name: "tic_field", type: "TEXT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-3") {
        createIndex(indexName: "iec_owner_idx", tableName: "issue_entitlement_change") {
            column(name: "iec_owner_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-4") {
        createIndex(indexName: "iec_status_idx", tableName: "issue_entitlement_change") {
            column(name: "iec_status_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-5") {
        createIndex(indexName: "iec_sub_idx", tableName: "issue_entitlement_change") {
            column(name: "iec_sub_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-6") {
        createIndex(indexName: "iec_tic_idx", tableName: "issue_entitlement_change") {
            column(name: "iec_tic_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-7") {
        createIndex(indexName: "iec_title_sub_status_idx", tableName: "issue_entitlement_change") {
            column(name: "iec_sub_fk")

            column(name: "iec_status_rv_fk")

            column(name: "iec_tic_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-8") {
        createIndex(indexName: "tic_event_idx", tableName: "title_change") {
            column(name: "tic_event")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-9") {
        createIndex(indexName: "tic_new_date_value_idx", tableName: "title_change") {
            column(name: "tic_new_date_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-10") {
        createIndex(indexName: "tic_new_value_idx", tableName: "title_change") {
            column(name: "tic_new_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-11") {
        createIndex(indexName: "tic_new_value_rv_idx", tableName: "title_change") {
            column(name: "tic_new_ref_value_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-12") {
        createIndex(indexName: "tic_old_date_value_idx", tableName: "title_change") {
            column(name: "tic_old_date_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-13") {
        createIndex(indexName: "tic_old_value_idx", tableName: "title_change") {
            column(name: "tic_old_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-14") {
        createIndex(indexName: "tic_old_value_rv_idx", tableName: "title_change") {
            column(name: "tic_old_ref_value_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-15") {
        createIndex(indexName: "tic_tipp_idx", tableName: "title_change") {
            column(name: "tic_tipp_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-16") {
        addForeignKeyConstraint(baseColumnNames: "iec_tic_fk", baseTableName: "issue_entitlement_change", constraintName: "FK7ja69if3ltie682j1toxsj8nv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tic_id", referencedTableName: "title_change", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-17") {
        addForeignKeyConstraint(baseColumnNames: "tic_new_ref_value_rv_fk", baseTableName: "title_change", constraintName: "FKdedsm94lt4vku9r1g70kv2dvo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-18") {
        addForeignKeyConstraint(baseColumnNames: "iec_sub_fk", baseTableName: "issue_entitlement_change", constraintName: "FKjfgknpwmbru5fe3adtdaydeq1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-19") {
        addForeignKeyConstraint(baseColumnNames: "tic_old_ref_value_rv_fk", baseTableName: "title_change", constraintName: "FKl0r27e2piuefb1owtcm6q1i6b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-20") {
        addForeignKeyConstraint(baseColumnNames: "tic_tipp_fk", baseTableName: "title_change", constraintName: "FKmtr9kyh3j7d9vbs0gc7y0cg7e", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-21") {
        addForeignKeyConstraint(baseColumnNames: "iec_status_rv_fk", baseTableName: "issue_entitlement_change", constraintName: "FKo7o30s3e3h1dxfcid067vydmw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-22") {
        addForeignKeyConstraint(baseColumnNames: "iec_owner_fk", baseTableName: "issue_entitlement_change", constraintName: "FKs7syvcnkmdjuc7ltjkp1aymk8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-23") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Removed' and rdc_description = 'tipp.status') where tipp_status_rv_fk is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-24") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "tipp_status_rv_fk", tableName: "title_instance_package_platform", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-25") {
        grailsChange {
            change {
                sql.execute("update issue_entitlement set ie_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Removed' and rdc_description = 'tipp.status') where ie_status_rv_fk is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-26") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "ie_status_rv_fk", tableName: "issue_entitlement", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1681212470764-27") {
        addUniqueConstraint(columnNames: "tipp_guid", constraintName: "UC_TITLE_INSTANCE_PACKAGE_PLATFORMTIPP_GUID_COL", tableName: "title_instance_package_platform")
    }

    //remap new title
    changeSet(author: "galffy (hand-coded)", id: "1681212470764-28") {
        grailsChange {
            change {
                sql.execute("insert into title_change (tic_version, tic_tipp_fk, tic_event, tic_date_created, tic_last_updated) select distinct on(pc_tipp_fk) pc_version, pc_tipp_fk, pc_msg_token, pc_date_created, pc_last_updated from pending_change where pc_msg_token = 'pendingChange.message_TP01' and pc_status_rdv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'History' and rdc_description = 'pending.change.status') order by pc_tipp_fk, pc_date_created desc")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-29") {
        grailsChange {
            change {
                //PostgreSQL hangs if this analyze is not being executed prior to changeset #30
                sql.execute("analyze title_change")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-30") {
        grailsChange {
            change {
                sql.execute("insert into issue_entitlement_change (iec_version, iec_tic_fk, iec_sub_fk, iec_status_rv_fk, iec_action_date, iec_owner_fk, iec_date_created, iec_last_updated) select pc_version, (select tic_id from title_change where tic_tipp_fk = pc_tipp_fk and tic_event = pc_msg_token), split_part(pc_oid, ':', 2)::bigint, pc_status_rdv_fk, case when pc_action_date is not null then pc_action_date else pc_ts end, pc_owner, pc_date_created, pc_last_updated from pending_change where pc_oid is not null and pc_msg_token = 'pendingChange.message_TP01' and pc_status_rdv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value in ('Accepted', 'Rejected') and rdc_description = 'pending.change.status')")
            }
            rollback {}
        }
    }

    //remap title deleted
    changeSet(author: "galffy (hand-coded)", id: "1681212470764-31") {
        grailsChange {
            change {
                sql.execute("insert into title_change (tic_version, tic_tipp_fk, tic_event, tic_old_ref_value_rv_fk, tic_date_created, tic_last_updated) select distinct on(pc_tipp_fk) pc_version, pc_tipp_fk, pc_msg_token, (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = pc_old_value and rdc_description = 'tipp.status'), pc_date_created, pc_last_updated from pending_change where pc_msg_token = 'pendingChange.message_TP03' and pc_status_rdv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'History' and rdc_description = 'pending.change.status') order by pc_tipp_fk, pc_date_created desc")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-32") {
        grailsChange {
            change {
                sql.execute("insert into issue_entitlement_change (iec_version, iec_tic_fk, iec_sub_fk, iec_status_rv_fk, iec_action_date, iec_owner_fk, iec_date_created, iec_last_updated) select pc_version, (select tic_id from title_change where tic_tipp_fk = pc_tipp_fk and tic_event = pc_msg_token), split_part(pc_oid, ':', 2)::bigint, pc_status_rdv_fk, case when pc_action_date is not null then pc_action_date else pc_ts end case, pc_owner, pc_date_created, pc_last_updated from pending_change where pc_oid is not null and pc_msg_token = 'pendingChange.message_TP03'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-33") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_tc_fk is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-34") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_pi_fk is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681212470764-35") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_tipp_fk is not null")
            }
            rollback {}
        }
    }

}
