databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1594642527038-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_2", type: "varchar(512)")
		}
	}

	changeSet(author: "Moe (generated)", id: "1594642527038-2") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url_3", type: "varchar(512)")
		}
	}
}
