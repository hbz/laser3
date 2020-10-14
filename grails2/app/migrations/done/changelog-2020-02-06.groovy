databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1580985695082-1") {
		grailsChange {
			change {
				sql.execute("truncate table system_profiler restart identity")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1580985695082-2") {
		dropTable(tableName: "identifier_backup")
	}

	changeSet(author: "kloberd (generated)", id: "1580985695082-3") {
		dropTable(tableName: "identifier_namespace_backup")
	}

	changeSet(author: "kloberd (generated)", id: "1580985695082-4") {
		dropTable(tableName: "identifier_occurrence_backup")
	}
}
