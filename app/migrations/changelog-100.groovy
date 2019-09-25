databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1569389997414-1") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_is_done", type: "bool")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-2") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_is_hide", type: "bool")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-3") {
		addColumn(schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_accept_status_rv_fk", type: "int8")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-4") {
		addColumn(schemaName: "public", tableName: "price_item") {
			column(name: "pi_list_currency_rv_fk", type: "int8")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-5") {
		addColumn(schemaName: "public", tableName: "task") {
			column(name: "tsk_system_create_date", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-6") {
		modifyDataType(columnName: "idns_is_hidden", newDataType: "bool", tableName: "identifier_namespace")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-7") {
		dropColumn(columnName: "local_currency_id", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-8") {
		modifyDataType(columnName: "ic_id", newDataType: "int8", tableName: "issue_entitlement_coverage")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-9") {
		modifyDataType(columnName: "pi_id", newDataType: "int8", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-10") {
		modifyDataType(columnName: "pi_list_price", newDataType: "numeric(19, 2)", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-11") {
		dropNotNullConstraint(columnDataType: "numeric(19, 2)", columnName: "pi_list_price", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-12") {
		dropNotNullConstraint(columnDataType: "int8", columnName: "pi_local_currency_rv_fk", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-13") {
		modifyDataType(columnName: "pi_local_price", newDataType: "numeric(19, 2)", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-14") {
		dropNotNullConstraint(columnDataType: "numeric(19, 2)", columnName: "pi_local_price", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-15") {
		modifyDataType(columnName: "pi_price_date", newDataType: "timestamp", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-16") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "pi_price_date", tableName: "price_item")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-17") {
		modifyDataType(columnName: "surconf_evaluation_finish", newDataType: "bool", tableName: "survey_config")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-18") {
		modifyDataType(columnName: "surconf_is_subscription_survey_fix", newDataType: "bool", tableName: "survey_config")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-19") {
		modifyDataType(columnName: "surin_is_subscription_survey", newDataType: "bool", tableName: "survey_info")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-20") {
		modifyDataType(columnName: "tc_id", newDataType: "int8", tableName: "tippcoverage")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-21") {
		modifyDataType(columnName: "date_created", newDataType: "timestamp", tableName: "user")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-22") {
		modifyDataType(columnName: "last_updated", newDataType: "timestamp", tableName: "user")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-23") {
		addForeignKeyConstraint(baseColumnNames: "pi_list_currency_rv_fk", baseTableName: "price_item", baseTableSchemaName: "public", constraintName: "FKA8C4E84921F4F71E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "klober (generated)", id: "1569389997414-24") {
		addForeignKeyConstraint(baseColumnNames: "ie_accept_status_rv_fk", baseTableName: "issue_entitlement", baseTableSchemaName: "public", constraintName: "FK2D45F6C7F06B2DF8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	//changeSet(author: "klober (generated)", id: "1569389997414-25") {
	//	dropForeignKeyConstraint(baseTableName: "price_item", baseTableSchemaName: "public", constraintName: "fka8c4e849bf9846e6")
	//}
}
