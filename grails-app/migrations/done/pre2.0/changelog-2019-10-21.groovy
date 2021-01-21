databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1571667332334-1") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_multi_year", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571667332334-2") {
		dropTable(tableName: "event_log")
	}

	changeSet(author: "kloberd (modified)", id: "1571667332334-3") {
		grailsChange {
			change {
				sql.execute("UPDATE subscription set sub_is_multi_year = FALSE where sub_is_multi_year is null")
			}
			rollback {}
		}
	}
}
