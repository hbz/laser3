databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1559630391991-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_internal_comment", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1559630391991-2") {
		modifyDataType(columnName: "surconf_comment", newDataType: "text", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1559630391991-3") {
		modifyDataType(columnName: "surorg_id", newDataType: "int8", tableName: "survey_org")
	}
}
