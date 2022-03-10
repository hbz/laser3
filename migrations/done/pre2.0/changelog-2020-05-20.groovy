databaseChangeLog = {

	changeSet(author: "agalffy (generated)", id: "1589956203846-1") {
		dropColumn(columnName: "lic_lastmod", tableName: "license")
	}

}
