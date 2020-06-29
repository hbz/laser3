databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1570447420591-1") {
		createTable(schemaName: "public", tableName: "customer_identifier") {
			column(autoIncrement: "true", name: "cid_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "customer_idenPK")
			}

			column(name: "cid_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "cid_owner_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "cid_platform_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "cid_type_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "cid_value", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570447420591-2") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_created_by_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570447420591-3") {
		addForeignKeyConstraint(baseColumnNames: "cid_owner_fk", baseTableName: "customer_identifier", baseTableSchemaName: "public", constraintName: "FK5ABD8BAA3EAB8579", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570447420591-4") {
		addForeignKeyConstraint(baseColumnNames: "cid_platform_fk", baseTableName: "customer_identifier", baseTableSchemaName: "public", constraintName: "FK5ABD8BAA7A11DE00", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570447420591-5") {
		addForeignKeyConstraint(baseColumnNames: "cid_type_rv_fk", baseTableName: "customer_identifier", baseTableSchemaName: "public", constraintName: "FK5ABD8BAA25F9D32D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570447420591-6") {
		addForeignKeyConstraint(baseColumnNames: "org_created_by_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE4377D68C2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
