databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1521541966736-1") {
		createTable(tableName: "stats_triple_cursor") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "stats_triple_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "stats_customer_id", type: "varchar(32)") {
				constraints(nullable: "false")
			}

			column(name: "have_up_to", type: "varchar(32)") {
				constraints(nullable: "false")
			}

			column(name: "stats_supplier_id", type: "varchar(32)") {
				constraints(nullable: "false")
			}

			column(name: "stats_title_id", type: "varchar(32)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1521541966736-2") {
		addColumn(tableName: "site_page") {
			column(name: "globaluid", type: "varchar(255)") {
				constraints(unique: "true")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1521541966736-4") {
		createIndex(indexName: "globaluid_uniq_1521541966089", tableName: "site_page", unique: "true") {
			column(name: "globaluid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1521541966736-5") {
		createIndex(indexName: "stats_cursor_idx", tableName: "stats_triple_cursor") {
			column(name: "stats_customer_id")

			column(name: "stats_supplier_id")

			column(name: "stats_title_id")
		}
	}

	//changeSet(author: "kloberd (generated)", id: "1521541966736-3") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
