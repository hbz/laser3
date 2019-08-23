databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566540202427-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_evaluation_finish", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566540202427-2") {
		createIndex(indexName: "rdv_owner_value_idx", schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_owner")
			column(name: "rdv_value")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566540202427-3") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_config SET surconf_evaluation_finish = false WHERE surconf_evaluation_finish is null")
			}
			rollback {}
		}
	}
}