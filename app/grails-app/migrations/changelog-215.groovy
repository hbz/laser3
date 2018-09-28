databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1537519826286-1") {
		modifyDataType(columnName: "sti_described", newDataType: "longtext", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-2") {
		dropNotNullConstraint(columnDataType: "longtext", columnName: "sti_described", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-3") {
		modifyDataType(columnName: "sti_expected", newDataType: "longtext", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-4") {
		dropNotNullConstraint(columnDataType: "longtext", columnName: "sti_expected", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-5") {
		modifyDataType(columnName: "sti_info", newDataType: "longtext", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-6") {
		dropNotNullConstraint(columnDataType: "longtext", columnName: "sti_info", tableName: "system_ticket")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-8") {
		dropColumn(columnName: "ci_cost_in_billing_currency_after_tax", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1537519826286-9") {
		dropColumn(columnName: "ci_cost_in_local_currency_after_tax", tableName: "cost_item")
	}

	//changeSet(author: "kloberd (generated)", id: "1537519826286-7") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
