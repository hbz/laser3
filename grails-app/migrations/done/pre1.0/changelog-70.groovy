databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1561453819381-1") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_target_org_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1561453819381-2") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_administrative", type: "bool") {
				// constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-3") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_costitems_finish", type: "bool") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-4") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_scheduled_enddate", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-5") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_scheduled_startdate", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-6") {
		modifyDataType(columnName: "ci_cost_in_billing_currency", newDataType: "numeric(19, 2)", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-7") {
		modifyDataType(columnName: "ci_cost_in_local_currency", newDataType: "numeric(19, 2)", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-8") {
		modifyDataType(columnName: "ci_currency_rate", newDataType: "numeric(19, 2)", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-9") {
		dropForeignKeyConstraint(baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "fkefe45c45b47e66a0")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-10") {
		dropForeignKeyConstraint(baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "fkefe45c453096044a")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-12") {
		dropColumn(columnName: "created_by_id", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-13") {
		dropColumn(columnName: "last_updated_by_id", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561453819381-11") {
		addForeignKeyConstraint(baseColumnNames: "dc_target_org_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8978253F5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (modified)", id: "1561453819381-14") {
		grailsChange {
			change {
				sql.execute("UPDATE subscription SET sub_is_administrative = false WHERE sub_is_administrative IS NULL")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1561453819381-15") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sub_is_administrative", tableName: "subscription")
	}
}
