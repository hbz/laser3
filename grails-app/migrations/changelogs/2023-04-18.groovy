package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1681829852450-1") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_ie_group_name", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1681829852450-2") {
        grailsChange {
            change {
                Integer countUpdate = sql.executeUpdate('''update issue_entitlement set ie_accept_status_rv_fk = (select rdv_id from refdata_value 
                    where rdv_value = 'Fixed' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ie.accept.status'))
                    where ie_accept_status_rv_fk != (select rdv_id from refdata_value 
                    where rdv_value = 'Fixed' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ie.accept.status'))
                    and ie_date_created >= '2023-02-27 00:00:00.000000' ''')

                confirm("update issue_entitlement set ie_accept_status_rv_fk = fixed: ${countUpdate}")
                changeSet.setComments("update issue_entitlement set ie_accept_status_rv_fk = fixed: ${countUpdate}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1681829852450-3") {
        grailsChange {
            change {
                Integer countUpdate = sql.executeUpdate('''delete from issue_entitlement_coverage 
                    where ic_ie_fk in (select ie_id from issue_entitlement 
                    where ie_accept_status_rv_fk != (select rdv_id from refdata_value 
                    where rdv_value = 'Fixed' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ie.accept.status'))
                    and ie_date_created < '2023-02-27 00:00:00.000000') ''')

                confirm("delete issue_entitlement_coverage with ie_accept_status_rv_fk != fixed: ${countUpdate}")
                changeSet.setComments("delete issue_entitlement_coverage with ie_accept_status_rv_fk != fixed: ${countUpdate}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1681829852450-4") {
        grailsChange {
            change {
                Integer countUpdate = sql.executeUpdate('''delete from price_item 
                    where pi_ie_fk in (select ie_id from issue_entitlement 
                    where ie_accept_status_rv_fk != (select rdv_id from refdata_value 
                    where rdv_value = 'Fixed' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ie.accept.status'))
                    and ie_date_created < '2023-02-27 00:00:00.000000') ''')


                confirm("delete price_item with ie_accept_status_rv_fk != fixed: ${countUpdate}")
                changeSet.setComments("delete price_item with ie_accept_status_rv_fk != fixed: ${countUpdate}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1681829852450-5") {
        grailsChange {
            change {
                Integer countUpdate = sql.executeUpdate('''delete from issue_entitlement 
                    where ie_accept_status_rv_fk != (select rdv_id from refdata_value 
                    where rdv_value = 'Fixed' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'ie.accept.status'))
                    and ie_date_created < '2023-02-27 00:00:00.000000' ''')

                confirm("delete issue_entitlement with ie_accept_status_rv_fk != fixed: ${countUpdate}")
                changeSet.setComments("delete issue_entitlement with ie_accept_status_rv_fk != fixed: ${countUpdate}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-6") {
        dropIndex(indexName: "ie_status_accept_status_idx", tableName: "issue_entitlement")
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-7") {
        createIndex(indexName: "ie_status_accept_status_idx", tableName: "issue_entitlement", unique: "false") {
            column(name: "ie_subscription_fk")

            column(name: "ie_status_rv_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-8") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c7f06b2df8")
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-9") {
        dropColumn(columnName: "ie_accept_status_rv_fk", tableName: "issue_entitlement")
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-10") {
        addColumn(tableName: "issue_entitlement_group") {
            column(name: "ig_survey_config_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681829852450-11") {
        addForeignKeyConstraint(baseColumnNames: "ig_survey_config_fk", baseTableName: "issue_entitlement_group", constraintName: "FKj64bd5sm3lppyronesa1g9xt7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

}
