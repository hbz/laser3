databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1569915031100-1") {
		addColumn(schemaName: "public", tableName: "survey_org") {
			column(name: "surorg_finish_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1569915031100-2") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_is_required", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1569915031100-3") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_result SET surre_is_required = false WHERE surre_is_required IS NULL")
			}
			rollback {}
		}
	}
}
