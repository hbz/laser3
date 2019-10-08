databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566540202427-1") {
		createIndex(indexName: "rdv_owner_value_idx", schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_owner")
			column(name: "rdv_value")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566540202427-2") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_config SET surconf_evaluation_finish = false WHERE surconf_evaluation_finish is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566540202427-3") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org RENAME date_created TO org_date_created")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566540202427-4") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org RENAME last_updated TO org_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566540202427-5") {
		addColumn(schemaName: "public", tableName: "user") {
			column(name: "date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566540202427-6") {
		addColumn(schemaName: "public", tableName: "user") {
			column(name: "last_updated", type: "timestamp")
		}
	}
}