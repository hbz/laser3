databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1537429100056-1") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_type_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1537429100056-4") {
		dropIndex(indexName: "jusp_cursor_idx", tableName: "jusp_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1537429100056-5") {
		createIndex(indexName: "FKEFE45C455C9AEE85", tableName: "cost_item") {
			column(name: "ci_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1537429100056-6") {
		dropTable(tableName: "jusp_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1537429100056-2") {
		addForeignKeyConstraint(baseColumnNames: "ci_type_rv_fk", baseTableName: "cost_item", constraintName: "FKEFE45C455C9AEE85", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1537429100056-3") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
