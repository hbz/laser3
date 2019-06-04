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

	changeSet(author: "kloberd (modified)", id: "1559630391991-4") {
		grailsChange {
			change {
				sql.execute("DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_READER')")
				sql.execute("DELETE FROM role WHERE authority = 'ROLE_API_READER'")

				sql.execute("DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_WRITER')")
				sql.execute("DELETE FROM role WHERE authority = 'ROLE_API_WRITER'")

				sql.execute("DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_DATAMANAGER')")
				sql.execute("DELETE FROM role WHERE authority = 'ROLE_API_DATAMANAGER'")
			}
			rollback {}
		}
	}
}
