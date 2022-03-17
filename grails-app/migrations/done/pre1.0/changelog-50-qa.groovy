databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1556107615066-5") {
		addColumn(schemaName: "public", tableName: "stats_triple_cursor") {
			column(name: "avail_from", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}
}
