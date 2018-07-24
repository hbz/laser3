databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1532416440170-1") {
		addColumn(tableName: "person") {
			column(name: "prs_title", type: "varchar(255)")
		}
	}
}
