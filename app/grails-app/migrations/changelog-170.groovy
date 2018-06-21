databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1529586175912-1") {
		createTable(tableName: "elasticsearch_source") {
			column(autoIncrement: "true", name: "ess_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "elasticsearchPK")
			}

			column(name: "ess_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ess_active", type: "bit")

			column(name: "ess_cluster", type: "varchar(255)")

			column(name: "ess_gokb_es", type: "bit")

			column(name: "ess_host", type: "varchar(255)")

			column(name: "ess_identifier", type: "varchar(255)")

			column(name: "ess_index", type: "varchar(255)")

			column(name: "ess_laser_es", type: "bit")

			column(name: "ess_name", type: "longtext")

			column(name: "ess_port", type: "integer")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1529586175912-2") {
		createTable(tableName: "numbers") {
			column(autoIncrement: "true", name: "num_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "numbersPK")
			}

			column(name: "num_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "num_create_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "num_end_date", type: "datetime")

			column(name: "num_lastUpdate_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "num_number", type: "integer")

			column(name: "num_org_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "num_start_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "num_typ_rdv_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1529586175912-6") {
		createIndex(indexName: "FK88C28E4A3026029E", tableName: "numbers") {
			column(name: "num_typ_rdv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1529586175912-7") {
		createIndex(indexName: "FK88C28E4AB8FA5820", tableName: "numbers") {
			column(name: "num_org_fk")
		}
	}

	//changeSet(author: "kloberd (generated)", id: "1529586175912-3") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1529586175912-4") {
		addForeignKeyConstraint(baseColumnNames: "num_org_fk", baseTableName: "numbers", constraintName: "FK88C28E4AB8FA5820", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1529586175912-5") {
		addForeignKeyConstraint(baseColumnNames: "num_typ_rdv_fk", baseTableName: "numbers", constraintName: "FK88C28E4A3026029E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}
}
