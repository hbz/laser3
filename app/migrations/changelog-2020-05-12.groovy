databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1589272928554-1") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "public", constraintName: "fk9f08441f5a55c6c")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-2") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license", baseTableSchemaName: "public", constraintName: "fk620852cc4d1ae0db")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-3") {
		dropColumn(columnName: "lic_opl_fk", tableName: "license")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-4") {
		dropTable(tableName: "onixpl_license")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-5") {
		addColumn(schemaName: "public", tableName: "platform") {
			column(name: "plat_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-6") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-7") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-8") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-9") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-10") {
		addColumn(schemaName: "public", tableName: "person") {
			column(name: "prs_last_updated_cascading", type: "timestamp")
		}
	}
}
