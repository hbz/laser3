databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1595398087525-1") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "sm_content_de", tableName: "system_message")
	}

	changeSet(author: "kloberd (generated)", id: "1595398087525-2") {
		createIndex(indexName: "lp_instance_of_idx", schemaName: "public", tableName: "license_property") {
			column(name: "lp_instance_of_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1595398087525-3") {
		createIndex(indexName: "sp_instance_of_idx", schemaName: "public", tableName: "subscription_property") {
			column(name: "sp_instance_of_fk")
		}
	}

//	changeSet(author: "kloberd (generated)", id: "1595398087525-4") {
//		addForeignKeyConstraint(baseColumnNames: "lp_tenant_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC413F9C604C6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
//	}
//
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-5") {
//		addNotNullConstraint(columnDataType: "int8", columnName: "lp_tenant_fk", tableName: "license_property")
//	}
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-6") {
//		addNotNullConstraint(columnDataType: "int8", columnName: "sp_tenant_fk", tableName: "subscription_property")
//	}
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-7") {
//		addNotNullConstraint(columnDataType: "int8", columnName: "pp_tenant_fk", tableName: "person_property")
//	}
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-8") {
//		addNotNullConstraint(columnDataType: "bool", columnName: "active", tableName: "ftcontrol")
//	}
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-9") {
//		addNotNullConstraint(columnDataType: "bool", columnName: "surconf_create_title_groups", tableName: "survey_config")
//	}
//
//	changeSet(author: "kloberd (generated)", id: "1595398087525-10") {
//		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "sm_type", tableName: "system_message")
//	}
}
