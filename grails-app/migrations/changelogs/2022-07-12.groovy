package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1657610295746-1") {
        dropColumn(columnName: "or_title_fk", tableName: "org_role")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-2") {
        dropIndex(indexName: "id_value_idx", tableName: "identifier")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-3") {
        createIndex(indexName: "id_ns_value_idx", tableName: "identifier", unique: "false") {
            column(name: "id_ns_fk")

            column(name: "id_value")
        }
    }

    changeSet(author: "klober (generated)", id: "1657610295746-4") {
        dropIndex(indexName: "stats_cursor_idx", tableName: "stats_triple_cursor")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-5") {
        createIndex(indexName: "stats_title_supplier_customer_idx", tableName: "stats_triple_cursor", unique: "false") {
            column(name: "stats_title_id")

            column(name: "stats_supplier_id")

            column(name: "stats_customer_id")
        }
    }

    changeSet(author: "klober (generated)", id: "1657610295746-6") {
        dropIndex(indexName: "pending_change_oid_idx", tableName: "pending_change")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-7") {
        createIndex(indexName: "pending_change_oid_idx", tableName: "pending_change", unique: "false") {
            column(name: "pc_oid")
        }
    }

    changeSet(author: "klober (generated)", id: "1657610295746-8") {
        dropDefaultValue(columnDataType: "boolean", columnName: "ci_billing_sum_rounding", tableName: "cost_item")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-9") {
        dropDefaultValue(columnDataType: "boolean", columnName: "sub_has_publish_component", tableName: "subscription")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-10") {
        dropSequence(sequenceName: "counter4report_c4r_id_seq")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-11") {
        dropSequence(sequenceName: "counter5report_c5r_id_seq")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-12") {
        dropSequence(sequenceName: "laser_stats_cursor_lsc_id_seq")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-13") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_is_automatic_renew_annually", tableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-14") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconf_pac_perpetualaccess", tableName: "survey_config", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-15") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconpro_mandatory_property", tableName: "survey_config_properties", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-16") {
        addForeignKeyConstraint(baseColumnNames: "ct_language_rv_fk", baseTableName: "contact", constraintName: "FKk7cibsotlcmfot0vilqt26926", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-17") {
        addUniqueConstraint(columnNames: "pkg_gokb_id", constraintName: "UC_PACKAGEPKG_GOKB_ID_COL", tableName: "package")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-18") {
        addUniqueConstraint(columnNames: "tipp_gokb_id", constraintName: "UC_TITLE_INSTANCE_PACKAGE_PLATFORMTIPP_GOKB_ID_COL", tableName: "title_instance_package_platform")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-19") {
        addUniqueConstraint(columnNames: "plat_gokb_id", constraintName: "UC_PLATFORMPLAT_GOKB_ID_COL", tableName: "platform")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-20") {
        addUniqueConstraint(columnNames: "ciec_guid", constraintName: "UC_COST_ITEM_ELEMENT_CONFIGURATIONCIEC_GUID_COL", tableName: "cost_item_element_configuration")
    }

    changeSet(author: "klober (generated)", id: "1657610295746-21") {
        addUniqueConstraint(columnNames: "pi_guid", constraintName: "UC_PRICE_ITEMPI_GUID_COL", tableName: "price_item")
    }

    changeSet(author: "klober (modified)", id: "1657610295746-22") {
        dropIndex(indexName: "pkg_gokb_id_uniq_1582107700215", tableName: "package")
    }

    changeSet(author: "klober (modified)", id: "1657610295746-23") {
        dropIndex(indexName: "tipp_gokb_id_uniq_1582107700242", tableName: "title_instance_package_platform")
    }

    changeSet(author: "klober (modified)", id: "1657610295746-24") {
        dropIndex(indexName: "plat_gokb_id_uniq_1582107700219", tableName: "platform")
    }

    changeSet(author: "klober (modified)", id: "1657610295746-25") {
        dropIndex(indexName: "ciec_guid_uniq_1548252516917", tableName: "cost_item_element_configuration")
    }

    changeSet(author: "klober (modified)", id: "1657610295746-26") {
        dropIndex(indexName: "pi_guid_uniq_1565864963840", tableName: "price_item")
    }

}
