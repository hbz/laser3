databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1539939992091-1") {
		addColumn(tableName: "refdata_category") {
			column(name: "rdv_hard_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539939992091-2") {
		addColumn(tableName: "refdata_value") {
			column(name: "rdv_hard_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

}
