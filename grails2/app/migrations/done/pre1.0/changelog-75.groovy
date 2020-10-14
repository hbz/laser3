databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1561617411135-1") {
		addColumn(schemaName: "public", tableName: "task") {
			column(name: "tsk_sur_config_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-2") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_unique", type: "bool") {
				//constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1561617411135-3") {
		grailsChange {
			change {
				sql.execute("update identifier_namespace set idns_unique = false where idns_non_unique = true")
				sql.execute("update identifier_namespace set idns_unique = true where (idns_non_unique = false or idns_non_unique is null)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1561617411135-6") {
		grailsChange {
			change {
				sql.execute("UPDATE refdata_value SET rdv_value = 'Responsible Admin' WHERE rdv_value = 'Responsible Contact'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1561617411135-4") {
		addNotNullConstraint(columnDataType: "bool", columnName: "idns_unique", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-5") {
		dropColumn(columnName: "idns_non_unique", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-7") {
		addForeignKeyConstraint(baseColumnNames: "tsk_sur_config_fk", baseTableName: "task", baseTableSchemaName: "public", constraintName: "FK36358553CCBAB9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-8") {
		modifyDataType(columnName: "ci_cost_in_billing_currency", newDataType: "float8", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-9") {
		modifyDataType(columnName: "ci_cost_in_local_currency", newDataType: "float8", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1561617411135-10") {
		modifyDataType(columnName: "ci_currency_rate", newDataType: "float8", tableName: "cost_item")
	}
}
