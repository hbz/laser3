databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1527152496767-1") {
		addColumn(tableName: "ftcontrol") {
			column(name: "db_elements", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1527152496767-2") {
		addColumn(tableName: "ftcontrol") {
			column(name: "es_elements", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1527152496767-3") {
		modifyDataType(columnName: "doc_creator", newDataType: "bigint", tableName: "doc")
	}

	changeSet(author: "kloberd (generated)", id: "1527152496767-6") {
		createIndex(indexName: "FK1853875A9D4D9", tableName: "doc") {
			column(name: "doc_creator")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1527152496767-4") {
		addForeignKeyConstraint(baseColumnNames: "doc_creator", baseTableName: "doc", constraintName: "FK1853875A9D4D9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (modified)", id: "1527152496767-5") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
