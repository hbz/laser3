databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1584524746024-1") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_description_de", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-2") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_description_en", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-3") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_name_de", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-4") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_name_en", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-5") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_url_prefix", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-6") {
		dropForeignKeyConstraint(baseTableName: "folder_item", baseTableSchemaName: "public", constraintName: "fk695ac446d580ee2")
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-7") {
		dropForeignKeyConstraint(baseTableName: "user_folder", baseTableSchemaName: "public", constraintName: "fke0966362a7674c9")
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-8") {
		dropTable(tableName: "folder_item")
	}

	changeSet(author: "kloberd (generated)", id: "1584524746024-9") {
		dropTable(tableName: "user_folder")
	}
}
