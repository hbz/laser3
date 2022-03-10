databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1587966467541-1") {
		dropForeignKeyConstraint(baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "fk9f88aca96235c89b")
	}

	changeSet(author: "klober (generated)", id: "1587966467541-2") {
		dropColumn(columnName: "id_ig_fk", tableName: "identifier")
	}

	changeSet(author: "klober (generated)", id: "1587966467541-3") {
		dropSequence(schemaName: "public", sequenceName: "identifier_group_ig_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1587966467541-4") {
		dropTable(tableName: "identifier_group")
	}
}
