databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1571667332334-1") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_multi_year", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571667332334-123") {
		dropTable(tableName: "event_log")
	}
}
