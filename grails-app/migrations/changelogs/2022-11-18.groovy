package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1668769531625-1") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "set_date_created", tableName: "system_setting", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-2") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ddc_date_created", tableName: "dewey_decimal_classification", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-3") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ftc_date_created", tableName: "ftcontrol", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-4") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "grs_date_created", tableName: "global_record_source", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-5") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "id_date_created", tableName: "identifier", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-6") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "igi_date_created", tableName: "issue_entitlement_group_item", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-7") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "lang_date_created", tableName: "language", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-8") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "sa_date_created", tableName: "system_announcement", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-9") {
        grailsChange {
            change {
                sql.execute("update elasticsearch_source set ess_date_created = now() where ess_date_created is null")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1668769531625-10") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ess_date_created", tableName: "elasticsearch_source", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-11") {
        grailsChange {
            change {
                sql.execute("update change_notification_queue_item set cnqi_date_created = cnqi_ts where cnqi_date_created is null")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1668769531625-12") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "cnqi_date_created", tableName: "change_notification_queue_item", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-13") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "dc_date_created", tableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-14") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "surre_date_created", tableName: "survey_result", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-15") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "tsk_date_created", tableName: "task", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-16") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "lp_date_created", tableName: "license_property", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-17") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pp_date_created", tableName: "person_property", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-18") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "sp_date_created", tableName: "subscription_property", validate: "true")
    }
}
