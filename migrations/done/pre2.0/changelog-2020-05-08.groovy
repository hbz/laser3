databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1588864690157-1") {
		createTable(schemaName: "public", tableName: "issue_entitlement_group") {
			column(autoIncrement: "true", name: "ig_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ie_groupPK")
			}

			column(name: "ig_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ig_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ig_description", type: "text")

			column(name: "ig_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ig_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "ig_sub_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
		createIndex(indexName: "unique_ig_sub_fk", schemaName: "public", tableName: "issue_entitlement_group", unique: "true") {
			column(name: "ig_name")

			column(name: "ig_sub_fk")
		}
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-2") {
		createTable(schemaName: "public", tableName: "issue_entitlement_group_item") {
			column(autoIncrement: "true", name: "igi_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ie_group_itemPK")
			}

			column(name: "igi_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "igi_date_created", type: "timestamp")

			column(name: "igi_ie_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "igi_ie_group_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "igi_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-3") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_ie_group_fk", type: "int8")
		}
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-4") {
		addForeignKeyConstraint(baseColumnNames: "ci_ie_group_fk", baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "FKEFE45C45F0EFF40", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ig_id", referencedTableName: "issue_entitlement_group", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-5") {
		addForeignKeyConstraint(baseColumnNames: "ig_sub_fk", baseTableName: "issue_entitlement_group", baseTableSchemaName: "public", constraintName: "FK696ADF07A985637F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-6") {
		addForeignKeyConstraint(baseColumnNames: "igi_ie_fk", baseTableName: "issue_entitlement_group_item", baseTableSchemaName: "public", constraintName: "FK82E6A14B252B85A5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "Moe (generated)", id: "1588864690157-7") {
		addForeignKeyConstraint(baseColumnNames: "igi_ie_group_fk", baseTableName: "issue_entitlement_group_item", baseTableSchemaName: "public", constraintName: "FK82E6A14B7184BA65", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ig_id", referencedTableName: "issue_entitlement_group", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
