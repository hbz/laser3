databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1585297387311-1") {
		dropForeignKeyConstraint(baseTableName: "pending_change", baseTableSchemaName: "public", constraintName: "fk65cbdf58a358b930")
	}

	changeSet(author: "klober (generated)", id: "1585297387311-2") {
		dropColumn(columnName: "pc_action_user_fk", tableName: "pending_change")
	}

	changeSet(author: "klober (generated)", id: "1585297387311-3") {
		addForeignKeyConstraint(baseColumnNames: "ti_medium_rv_fk", baseTableName: "title_instance", baseTableSchemaName: "public", constraintName: "FKACC69CBFCF2A91", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
