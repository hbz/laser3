databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1594195390561-1") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_comment", type: "text")
		}
	}
}
