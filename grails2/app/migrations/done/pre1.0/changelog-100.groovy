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

	changeSet(author: "klober (generated)", id: "1569389997414-5") {
		addColumn(schemaName: "public", tableName: "task") {
			column(name: "tsk_system_create_date", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1569389997414-6") {
		modifyDataType(columnName: "idns_is_hidden", newDataType: "bool", tableName: "identifier_namespace")
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

	changeSet(author: "djebeniani (modified)", id: "1569389997414-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE price_item RENAME pi_local_currency_rv_fk  TO pi_list_currency_rv_fk")
				sql.execute("ALTER TABLE price_item RENAME local_currency_id  TO pi_local_currency_rv_fk")
			}
			rollback {}
		}
		dropNotNullConstraint(columnDataType: "int8", columnName: "pi_list_currency_rv_fk", tableName: "price_item")
		dropNotNullConstraint(columnDataType: "int8", columnName: "pi_local_currency_rv_fk", tableName: "price_item")
		dropNotNullConstraint(columnDataType: "int8", columnName: "version", tableName: "price_item")
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

	changeSet(author: "djebeniani (modified)", id: "1569389997414-25") {
		grailsChange {
			change {
				sql.execute("UPDATE task SET tsk_system_create_date = tsk_create_date where tsk_system_create_date IS NULL")
				sql.execute("ALTER TABLE task ALTER COLUMN tsk_system_create_date SET NOT NULL")
			}
			rollback {}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1569389997414-26") {
		grailsChange {
			change {
				sql.execute("UPDATE issue_entitlement SET ie_accept_status_rv_fk = (SELECT rdv.rdv_id FROM refdata_value rdv\n" +
						"    JOIN refdata_category rdc ON rdv.rdv_owner = rdc.rdc_id\n" +
						"WHERE rdv.rdv_value = 'Fixed' AND rdc.rdc_description = 'IE Accept Status') where\n" +
						"ie_id IN (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id\n" +
						"WHERE rdv_value = 'Current')")
			}
			rollback {}
		}
	}
	changeSet(author: "djebeniani (modified)", id: "1569389997414-27") {
		grailsChange {
			change {
				sql.execute("UPDATE dashboard_due_date SET das_is_hide = false where das_is_hide IS NULL")
				sql.execute("ALTER TABLE dashboard_due_date ALTER COLUMN das_is_hide SET NOT NULL")
				sql.execute("UPDATE dashboard_due_date SET das_is_done = false where das_is_done IS NULL")
				sql.execute("ALTER TABLE dashboard_due_date ALTER COLUMN das_is_done SET NOT NULL")
			}
			rollback {}
		}
	}
}
