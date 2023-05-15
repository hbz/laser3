package changelogs

databaseChangeLog = {

    //needed because new refdatas are being used already in changesets
    changeSet(author: "galffy (hand-coded)", id: "1684135445696-1") {
        grailsChange {
            change {
                sql.executeInsert("insert into refdata_category (rdc_version, rdc_description, rdc_is_hard_data, rdc_date_created, rdc_last_updated) VALUES (0, 'subscription.holding', true, now(), now())")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1684135445696-2") {
        grailsChange {
            change {
                sql.executeInsert("insert into refdata_value (rdv_version, rdv_is_hard_data, rdv_owner, rdv_value, rdv_date_created, rdv_last_updated, rdv_value_de, rdv_value_en) " +
                        "VALUES (0, true, (select rdc_id from refdata_category where rdc_description = 'subscription.holding'), 'entire', now(), now(), 'Gesamtpaket', 'Entire Stock')," +
                        "(0, true, (select rdc_id from refdata_category where rdc_description = 'subscription.holding'), 'partial', now(), now(), 'Teilpaket', 'Partial Stock')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1684135445696-3") {
        addColumn(tableName: "subscription") {
            column(name: "sub_holding_selection_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684135445696-4") {
        createIndex(indexName: "sub_holding_selection_idx", tableName: "subscription") {
            column(name: "sub_holding_selection_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684135445696-5") {
        addForeignKeyConstraint(baseColumnNames: "sub_holding_selection_rv_fk", baseTableName: "subscription", constraintName: "FKccxtuj7pdo1v4u4yuwc3slwjf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1684135445696-6") {
        grailsChange {
            change {
                sql.executeUpdate("update subscription set sub_holding_selection_rv_fk = (select rdv_id from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'subscription.holding') and rdv_value = 'entire') where sub_id in (select sp_sub_fk from subscription_package join pending_change_configuration on sp_id = pcc_sp_fk where pcc_setting_key_enum = 'pendingChange.message_TP01' and pcc_setting_value_rv_fk = (select rdv_id from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'pending.change.configuration.setting') and rdv_value = 'Accept'))")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1684135445696-7") {
        grailsChange {
            change {
                sql.executeUpdate("update subscription set sub_holding_selection_rv_fk = (select rdv_id from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'subscription.holding') and rdv_value = 'partial') where sub_id in (select sp_sub_fk from subscription_package join pending_change_configuration on sp_id = pcc_sp_fk where pcc_setting_key_enum = 'pendingChange.message_TP01' and pcc_setting_value_rv_fk != (select rdv_id from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'pending.change.configuration.setting') and rdv_value = 'Accept'))")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1684135445696-8") {
        grailsChange {
            change {
                sql.executeUpdate("update audit_config set auc_reference_field = 'holdingSelection' where auc_reference_field = 'pendingChange.message_TP01'")
            }
            rollback {}
        }
    }
}
