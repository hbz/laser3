databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1516956768739-1") {
		dropForeignKeyConstraint(baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE05B522EC25")
	}

	changeSet(author: "kloberd (generated)", id: "1516956768739-2") {
		dropForeignKeyConstraint(baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE051D1B4283")
	}

	changeSet(author: "kloberd (generated)", id: "1516956768739-4") {
		dropColumn(columnName: "cig_budgetcode_fk", tableName: "cost_item_group")
	}

	changeSet(author: "kloberd (generated)", id: "1516956768739-5") {
		dropColumn(columnName: "cig_costItem_fk", tableName: "cost_item_group")
	}

	//changeSet(author: "kloberd (generated)", id: "1516956768739-3") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
