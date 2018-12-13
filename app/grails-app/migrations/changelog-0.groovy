databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1544696164652-1") {
		modifyDataType(columnName: "as_active", newDataType: "boolean", schemaName: "public", tableName: "api_source")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-2") {
		modifyDataType(columnName: "ci_final_cost_rounding", newDataType: "boolean", schemaName: "public", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-3") {
		modifyDataType(columnName: "ci_include_in_subscr", newDataType: "boolean", schemaName: "public", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-4") {
		modifyDataType(columnName: "ci_is_viewable", newDataType: "boolean", schemaName: "public", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-5") {
		modifyDataType(columnName: "dc_is_global", newDataType: "boolean", schemaName: "public", tableName: "doc_context")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-6") {
		modifyDataType(columnName: "ess_active", newDataType: "boolean", schemaName: "public", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-7") {
		modifyDataType(columnName: "ess_gokb_es", newDataType: "boolean", schemaName: "public", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-8") {
		modifyDataType(columnName: "ess_laser_es", newDataType: "boolean", schemaName: "public", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-9") {
		modifyDataType(columnName: "grs_active", newDataType: "boolean", schemaName: "public", tableName: "global_record_source")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-10") {
		modifyDataType(columnName: "grt_auto_pkg_update", newDataType: "boolean", schemaName: "public", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-11") {
		modifyDataType(columnName: "grt_auto_tipp_add", newDataType: "boolean", schemaName: "public", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-12") {
		modifyDataType(columnName: "grt_auto_tipp_del", newDataType: "boolean", schemaName: "public", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-13") {
		modifyDataType(columnName: "grt_auto_tipp_update", newDataType: "boolean", schemaName: "public", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-14") {
		modifyDataType(columnName: "idns_hide", newDataType: "boolean", schemaName: "public", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-15") {
		modifyDataType(columnName: "idns_non_unique", newDataType: "boolean", schemaName: "public", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-16") {
		modifyDataType(columnName: "active", newDataType: "boolean", schemaName: "public", tableName: "org_access_point_link")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-17") {
		modifyDataType(columnName: "auto_accept", newDataType: "boolean", schemaName: "public", tableName: "package")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-18") {
		modifyDataType(columnName: "pd_hard_data", newDataType: "boolean", schemaName: "public", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-19") {
		modifyDataType(columnName: "pd_mandatory", newDataType: "boolean", schemaName: "public", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-20") {
		modifyDataType(columnName: "pd_multiple_occurrence", newDataType: "boolean", schemaName: "public", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-21") {
		modifyDataType(columnName: "pd_soft_data", newDataType: "boolean", schemaName: "public", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-22") {
		modifyDataType(columnName: "rdv_hard_data", newDataType: "boolean", schemaName: "public", tableName: "refdata_category")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-23") {
		modifyDataType(columnName: "rdv_soft_data", newDataType: "boolean", schemaName: "public", tableName: "refdata_category")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-24") {
		modifyDataType(columnName: "rdv_hard_data", newDataType: "boolean", schemaName: "public", tableName: "refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-25") {
		modifyDataType(columnName: "rdv_soft_data", newDataType: "boolean", schemaName: "public", tableName: "refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-26") {
		modifyDataType(columnName: "active", newDataType: "boolean", schemaName: "public", tableName: "reminder")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-27") {
		modifyDataType(columnName: "sm_shownow", newDataType: "boolean", schemaName: "public", tableName: "system_message")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-28") {
		modifyDataType(columnName: "account_expired", newDataType: "boolean", schemaName: "public", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-29") {
		modifyDataType(columnName: "account_locked", newDataType: "boolean", schemaName: "public", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-30") {
		modifyDataType(columnName: "enabled", newDataType: "boolean", schemaName: "public", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-31") {
		modifyDataType(columnName: "password_expired", newDataType: "boolean", schemaName: "public", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1544696164652-32") {
		addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", baseTableSchemaName: "public", constraintName: "FK_2sxmvx4yux8sfk336c468cmyr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
