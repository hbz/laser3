databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1574777012842-1") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_list_verified_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1574777012842-2") {
		createIndex(indexName: "or_pkg_idx", schemaName: "public", tableName: "org_role") {
			column(name: "or_pkg_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1574777012842-3") {
		dropTable(tableName: "site_page")
	}
}
