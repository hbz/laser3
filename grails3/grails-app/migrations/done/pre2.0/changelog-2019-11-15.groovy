databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1573820751233-1") {
		addColumn(schemaName: "public", tableName: "stats_triple_cursor") {
			column(name: "identifier_type_id", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1573820751233-2") {
		dropColumn(columnName: "juspio_id", tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1573820751233-3") {
		addForeignKeyConstraint(baseColumnNames: "identifier_type_id", baseTableName: "stats_triple_cursor", baseTableSchemaName: "public", constraintName: "FKB71D92B7D149DDBF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "idns_id", referencedTableName: "identifier_namespace", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
