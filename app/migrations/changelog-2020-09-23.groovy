databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1600849820459-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment", type: "text")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-2") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment_2", type: "text")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1600849820459-3") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_comment_3", type: "text")
		}
	}
}
