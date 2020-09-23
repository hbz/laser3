databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1571059168404-1") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_legally_obliged_by_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571059168404-2") {
		addForeignKeyConstraint(baseColumnNames: "org_legally_obliged_by_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE46B0BEA97", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
