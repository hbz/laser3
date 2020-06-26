databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1583759022240-1") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_kind_rv_fk", type: "int8")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1583759022240-2") {
		addForeignKeyConstraint(baseColumnNames: "sub_kind_rv_fk", baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "FK1456591D8312F145", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

}
