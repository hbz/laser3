databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1601990151234-1") {
		addColumn(schemaName: "public", tableName: "links") {
			column(name: "l_dest_lic_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-2") {
		addColumn(schemaName: "public", tableName: "links") {
			column(name: "l_dest_sub_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-3") {
		addColumn(schemaName: "public", tableName: "links") {
			column(name: "l_source_lic_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-4") {
		addColumn(schemaName: "public", tableName: "links") {
			column(name: "l_source_sub_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-5") {
		createIndex(indexName: "l_dest_lic_idx", schemaName: "public", tableName: "links") {
			column(name: "l_dest_lic_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-6") {
		createIndex(indexName: "l_dest_sub_idx", schemaName: "public", tableName: "links") {
			column(name: "l_dest_sub_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-7") {
		createIndex(indexName: "l_source_lic_idx", schemaName: "public", tableName: "links") {
			column(name: "l_source_lic_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-8") {
		createIndex(indexName: "l_source_sub_idx", schemaName: "public", tableName: "links") {
			column(name: "l_source_sub_fk")
		}
	}

	changeSet(author: "galffy (modified)", id: "1601990151234-9") {
		grailsChange {
			change {
				sql.execute("update links set l_source_sub_fk = split_part(l_source_fk,':',2)::bigint where l_source_fk like '%Subscription%';")
				sql.execute("update links set l_dest_sub_fk = split_part(l_destination_fk,':',2)::bigint where l_destination_fk like '%Subscription%';")
				sql.execute("update links set l_source_lic_fk = split_part(l_source_fk,':',2)::bigint where l_source_fk like '%License%';")
				sql.execute("update links set l_dest_lic_fk = split_part(l_destination_fk,':',2)::bigint where l_destination_fk like '%License%';")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-10") {
		dropColumn(columnName: "l_destination_fk", tableName: "links")
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-11") {
		dropColumn(columnName: "l_source_fk", tableName: "links")
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-12") {
		addForeignKeyConstraint(baseColumnNames: "l_dest_lic_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB93EB7672C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-13") {
		addForeignKeyConstraint(baseColumnNames: "l_dest_sub_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB9BF239228", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-14") {
		addForeignKeyConstraint(baseColumnNames: "l_source_lic_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB9E9B79C53", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1601990151234-15") {
		addForeignKeyConstraint(baseColumnNames: "l_source_sub_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB96A23C74F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
