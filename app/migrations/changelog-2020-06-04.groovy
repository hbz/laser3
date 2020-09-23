databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1591265890522-1") {
		addColumn(schemaName: "public", tableName: "survey_org") {
			column(name: "surorg_owner_comment", type: "text")
		}
	}

}
