databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1519999470618-1") {
		addColumn(tableName: "folder_item") {
			column(name: "fi_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-2") {
		addColumn(tableName: "folder_item") {
			column(name: "fi_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-3") {
		addColumn(tableName: "identifier_namespace") {
			column(name: "idns_non_unique", type: "bit")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-4") {
		addColumn(tableName: "identifier_namespace") {
			column(name: "idns_type", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-5") {
		addColumn(tableName: "invoice") {
			column(name: "inv_description", type: "longtext")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-6") {
		addColumn(tableName: "user_folder") {
			column(name: "uf_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-7") {
		addColumn(tableName: "user_folder") {
			column(name: "uf_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-8") {
		dropForeignKeyConstraint(baseTableName: "identifier_namespace", baseTableSchemaName: "laser", constraintName: "FKBA7FFD4534D73C7D")
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-10") {
		dropColumn(columnName: "idns_nonUnique", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-11") {
		dropColumn(columnName: "idns_type_fl", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1519999470618-12") {
		dropColumn(columnName: "inv_date_description", tableName: "invoice")
	}

	//changeSet(author: "kloberd (generated)", id: "1519999470618-9") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
