databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1600849820459-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment", type: "text")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-2") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment_2", type: "text")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-3") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment_3", type: "text")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-4") {
		createTable(schemaName: "public", tableName: "address_type") {
			column(name: "address_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "refdata_value_id", type: "int8")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-5") {
		addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "address_type", baseTableSchemaName: "public", constraintName: "FKFC104A51360BA4A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "adr_id", referencedTableName: "address", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-6") {
		addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "address_type", baseTableSchemaName: "public", constraintName: "FKFC104A520E9838F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (modifed)", id: "1600849820459-7") {
		grailsChange {
			change {
				sql.execute("""insert into address_type(address_id, refdata_value_id)
				select adr_id, adr_type_rv_fk from address""")
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-8") {
		dropColumn(columnName: "adr_type_rv_fk", tableName: "address")
	}
}
