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
}
