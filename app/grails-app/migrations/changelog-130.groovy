databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1518509124190-1") {
		addColumn(tableName: "global_record_info") {
			column(name: "gri_status_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1518509124190-2") {
		addColumn(tableName: "invoice") {
			column(name: "inv_date_description", type: "longtext")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1518509124190-5") {
		createIndex(indexName: "FKB057C14074D2C985", tableName: "global_record_info") {
			column(name: "gri_status_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1518509124190-3") {
		addForeignKeyConstraint(baseColumnNames: "gri_status_rv_fk", baseTableName: "global_record_info", constraintName: "FKB057C14074D2C985", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}
}
