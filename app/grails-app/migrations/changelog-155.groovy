databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1526466553199-1") {
		addColumn(tableName: "address") {
			column(name: "adr_addition_first", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-2") {
		addColumn(tableName: "address") {
			column(name: "adr_addition_second", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-3") {
		addColumn(tableName: "address") {
			column(name: "adr_name", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-4") {
		addColumn(tableName: "address") {
			column(name: "adr_pob_city", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-5") {
		addColumn(tableName: "address") {
			column(name: "adr_pob_zipcode", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-6") {
		addColumn(tableName: "org") {
			column(name: "org_url_gov", type: "varchar(512)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-7") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "adr_city", tableName: "address")
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-8") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "adr_street_1", tableName: "address")
	}

	changeSet(author: "kloberd (generated)", id: "1526466553199-9") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "adr_zipcode", tableName: "address")
	}
}
