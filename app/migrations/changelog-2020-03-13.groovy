databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1584088132653-1") {
		addColumn(schemaName: "public", tableName: "system_profiler") {
			column(name: "sp_archive", type: "varchar(255)")
		}
	}
}
