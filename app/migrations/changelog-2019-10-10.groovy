databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1570696933657-1") {
		createTable(schemaName: "public", tableName: "platform_custom_property") {
			column(autoIncrement: "true", name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "platform_custPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "date_value", type: "timestamp")

			column(name: "dec_value", type: "numeric(19, 2)")

			column(name: "int_value", type: "int4")

			column(name: "note", type: "text")

			column(name: "owner_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "int8")

			column(name: "string_value", type: "text")

			column(name: "type_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-2") {
		addColumn(schemaName: "public", tableName: "api_source") {
			column(name: "as_editUrl", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-3") {
		addColumn(schemaName: "public", tableName: "global_record_source") {
			column(name: "grs_edit_uri", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-4") {
		createIndex(indexName: "pcp_owner_idx", schemaName: "public", tableName: "platform_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-5") {
		dropColumn(columnName: "apikey", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-6") {
		dropColumn(columnName: "apisecret", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-7") {
		dropTable(tableName: "dataload_file_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-8") {
		dropTable(tableName: "dataload_file_type")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-9") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B1728A77A17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-10") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B172992A286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-11") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B17638A6383", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
