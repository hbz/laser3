databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1600085404316-1") {
		dropForeignKeyConstraint(baseTableName: "pending_change", baseTableSchemaName: "public", constraintName: "fk65cbdf58b7f24d84")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-2") {
		dropColumn(columnName: "pc_sys_obj", tableName: "pending_change")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-3") {
		dropSequence(schemaName: "public", sequenceName: "system_object_sys_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-4") {
		dropTable(tableName: "system_object")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-5") {
		dropForeignKeyConstraint(baseTableName: "org_title_stats", baseTableSchemaName: "public", constraintName: "fk8fc8ff1d21d4e99d")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-6") {
		dropForeignKeyConstraint(baseTableName: "org_title_stats", baseTableSchemaName: "public", constraintName: "fk8fc8ff1d10288612")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-7") {
		dropSequence(schemaName: "public", sequenceName: "org_title_stats_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1600085404316-8") {
		dropTable(tableName: "org_title_stats")
	}
}
