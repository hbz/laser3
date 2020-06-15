databaseChangeLog = {

	changeSet(author: "klober (modified)", id: "1592202212497-1") {
		grailsChange {
			change {
				sql.execute("alter table system_message rename column sm_text to sm_content")
				sql.execute("alter table system_message rename column sm_show_now to sm_is_active")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1592202212497-2") {
		modifyDataType(columnName: "sm_content", newDataType: "text", tableName: "system_message")
	}

	changeSet(author: "klober (generated)", id: "1592202212497-3") {
		addColumn(schemaName: "public", tableName: "system_message") {
			column(name: "sm_type", type: "varchar(255)")
		}
	}

	changeSet(author: "klober (generated)", id: "1592202212497-4") {
		dropColumn(columnName: "sm_org_fk", tableName: "system_message")
	}
}
