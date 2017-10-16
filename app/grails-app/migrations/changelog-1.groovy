databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1508147805752-1") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "adr_type_rv_fk", tableName: "address")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-2") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "ct_type_rv_fk", tableName: "contact")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-3") {
		modifyDataType(columnName: "gri_desc", newDataType: "longtext", tableName: "global_record_info")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-4") {
		modifyDataType(columnName: "gri_name", newDataType: "longtext", tableName: "global_record_info")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-5") {
		modifyDataType(columnName: "grs_name", newDataType: "longtext", tableName: "global_record_source")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-6") {
		modifyDataType(columnName: "grt_name", newDataType: "longtext", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-7") {
		modifyDataType(columnName: "last_updated", newDataType: "datetime", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-8") {
		dropNotNullConstraint(columnDataType: "datetime", columnName: "last_updated", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-9") {
		modifyDataType(columnName: "string_value", newDataType: "varchar(255)", tableName: "license_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-10") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "org_name", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-11") {
		modifyDataType(columnName: "last_updated", newDataType: "datetime", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-12") {
		dropNotNullConstraint(columnDataType: "datetime", columnName: "last_updated", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-13") {
		modifyDataType(columnName: "sort_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-14") {
		modifyDataType(columnName: "ti_key_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-15") {
		modifyDataType(columnName: "ti_norm_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-16") {
		modifyDataType(columnName: "ti_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-17") {
		modifyDataType(columnName: "tipp_host_platform_url", newDataType: "longtext", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-18") {
		dropForeignKeyConstraint(baseTableName: "cluster", baseTableSchemaName: "KBPlusDev", constraintName: "FK33FB11FA69E7EA2E")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-19") {
		dropForeignKeyConstraint(baseTableName: "doc", baseTableSchemaName: "KBPlusDev", constraintName: "FK18538F206CBF4")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-20") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlusDev", constraintName: "FK2FD66C5CC2FB63")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-21") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlusDev", constraintName: "FK2FD66CD2A25EFB")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-22") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlusDev", constraintName: "FK2FD66C467CFA43")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-23") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlusDev", constraintName: "FK2FD66C4CB39BA6")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-24") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlusDev", constraintName: "FK2FD66C40C7D5B5")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-25") {
		dropForeignKeyConstraint(baseTableName: "global_record_tracker", baseTableSchemaName: "KBPlusDev", constraintName: "FK808F5966D92AE946")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-26") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F08441B6A1F9E4")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-27") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F08441595820F7")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-28") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F0844177A5D483")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-29") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F084416C7CF136")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-30") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F084414B2F78C0")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-31") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F0844119B9B694")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-32") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F08441D4BECC91")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-33") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F084414C1CBBFB")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-34") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F08441D77ABF2C")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-35") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F08441DBC1FD3A")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-36") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlusDev", constraintName: "FK9F0844197334194")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-37") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlusDev", constraintName: "FKF1E88AC0F8B5EA69")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-38") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlusDev", constraintName: "FKF1E88AC0EF8C4AB4")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-39") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlusDev", constraintName: "FK11797DDF2F1DD172")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-40") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlusDev", constraintName: "FK11797DDFE9F8A801")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-41") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlusDev", constraintName: "FK11797DDFCF47BC89")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-42") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlusDev", constraintName: "FKC989A8CB55D5F69B")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-43") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlusDev", constraintName: "FKC989A8CB3313FD03")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-44") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlusDev", constraintName: "FKB744770F7B0024B0")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-45") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlusDev", constraintName: "FKB744770FE27A4895")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-46") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlusDev", constraintName: "FKB744770FAAD0839C")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-47") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlusDev", constraintName: "FKBFF1439FB7B3B52")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-48") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlusDev", constraintName: "FKBFF1439F39056212")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-49") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlusDev", constraintName: "FK41AF6157CC172760")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-50") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlusDev", constraintName: "FK41AF6157F0E2D5FD")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-51") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlusDev", constraintName: "FKC4E39B5526759820")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-52") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlusDev", constraintName: "FKC4E39B555406FA95")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-53") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlusDev", constraintName: "FKC4E39B552F08D4E6")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-55") {
		dropIndex(indexName: "fact_uid_idx", tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-56") {
		dropIndex(indexName: "oplt_el_id_idx", tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-57") {
		dropIndex(indexName: "oput_entry_idx", tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-58") {
		dropIndex(indexName: "opul_entry_idx", tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-59") {
		createIndex(indexName: "FK808F5966F6287F86", tableName: "global_record_tracker") {
			column(name: "grt_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-60") {
		createIndex(indexName: "fact_uid_uniq_1508147805077", tableName: "kbplus_fact", unique: "true") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-61") {
		//Error executing SQL CREATE INDEX `org_imp_id_idx` ON `org`(`org_imp_id`): Specified key was too long; max key length is 767 bytes

		//createIndex(indexName: "org_imp_id_idx", tableName: "org") {
		//	column(name: "org_imp_id")
		//}
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-62") {
		//Error executing SQL CREATE INDEX `td_name_idx` ON `property_definition`(`pd_name`): Duplicate key name 'td_name_idx'

		//createIndex(indexName: "td_name_idx", tableName: "property_definition") {
		//	column(name: "pd_name")
		//}
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-63") {
		createIndex(indexName: "tfmr_url_uniq_1508147805108", tableName: "transformer", unique: "true") {
			column(name: "tfmr_url")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-64") {
		dropColumn(columnName: "cl_owner_fk", tableName: "cluster")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-65") {
		dropColumn(columnName: "doc_migrated", tableName: "doc")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-66") {
		dropColumn(columnName: "owner_id", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-67") {
		dropColumn(columnName: "lic_alumni_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-68") {
		dropColumn(columnName: "lic_concurrent_user_count", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-69") {
		dropColumn(columnName: "lic_concurrent_users_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-70") {
		dropColumn(columnName: "lic_coursepack_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-71") {
		dropColumn(columnName: "lic_enterprise_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-72") {
		dropColumn(columnName: "lic_ill_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-73") {
		dropColumn(columnName: "lic_license_type_str", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-74") {
		dropColumn(columnName: "lic_multisite_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-75") {
		dropColumn(columnName: "lic_partners_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-76") {
		dropColumn(columnName: "lic_pca_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-77") {
		dropColumn(columnName: "lic_remote_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-78") {
		dropColumn(columnName: "lic_vle_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-79") {
		dropColumn(columnName: "lic_walkin_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-80") {
		dropColumn(columnName: "date", tableName: "license_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-81") {
		dropColumn(columnName: "date", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-82") {
		dropColumn(columnName: "org_address", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-83") {
		dropColumn(columnName: "date", tableName: "org_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-84") {
		dropColumn(columnName: "date", tableName: "org_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-85") {
		dropColumn(columnName: "prs_is_public_rdv_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-86") {
		dropColumn(columnName: "prs_org_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-87") {
		dropColumn(columnName: "prs_owner_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-88") {
		dropColumn(columnName: "date", tableName: "person_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-89") {
		dropColumn(columnName: "date", tableName: "subscription_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-90") {
		dropColumn(columnName: "date", tableName: "system_admin_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-91") {
		dropTable(tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-92") {
		dropTable(tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-93") {
		dropTable(tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-94") {
		dropTable(tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-95") {
		dropTable(tableName: "onixpl_usage_term_refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-96") {
		dropTable(tableName: "org_bck")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-97") {
		dropTable(tableName: "org_cluster")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-98") {
		dropTable(tableName: "org_title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-99") {
		dropTable(tableName: "type_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1508147805752-54") {
        //Error executing SQL ALTER TABLE `global_record_tracker` ADD CONSTRAINT `FK808F5966F6287F86` FOREIGN KEY (`grt_owner_fk`) REFERENCES `global_record_info` (`gri_id`): Cannot add or update a child row: a foreign key constraint fails (`KBPlusDev`.`#sql-121a_489`, CONSTRAINT `FK808F5966F6287F86` FOREIGN KEY (`grt_owner_fk`) REFERENCES `global_record_info` (`gri_id`))

        //addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	}
}
