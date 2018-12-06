databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1544001337306-1") {
		createTable(tableName: "system_profiler") {
			column(autoIncrement: "true", name: "sp_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_profilPK")
			}

			column(name: "sp_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sp_context_fk", type: "bigint")

			column(name: "sp_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "sp_ms", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "sp_params", type: "longtext")

			column(name: "sp_uri", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544001337306-4") {
		createIndex(indexName: "FK4BE76FF931B885BE", tableName: "system_profiler") {
			column(name: "sp_context_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544001337306-5") {
		createIndex(indexName: "sp_uri_idx", tableName: "system_profiler") {
			column(name: "sp_uri")
		}
	}

	//changeSet(author: "kloberd (generated)", id: "1544001337306-2") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1544001337306-3") {
		addForeignKeyConstraint(baseColumnNames: "sp_context_fk", baseTableName: "system_profiler", constraintName: "FK4BE76FF931B885BE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}
}
