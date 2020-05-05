databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1588590225056-1") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_series_name", type: "text")
		}
	}

	changeSet(author: "Moe (generated)", id: "1588590225056-2") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_subject_reference", type: "text")
		}
	}

}
