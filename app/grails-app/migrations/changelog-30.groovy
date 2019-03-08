databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1551166000616-1") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_is_shared", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-2") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_shared_from_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-3") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_gokb_id", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-4") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_gokb_id", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-5") {
		addColumn(schemaName: "public", tableName: "person_role") {
			column(name: "pr_position_type_rv_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-6") {
		addColumn(schemaName: "public", tableName: "platform") {
			column(name: "plat_gokb_id", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-7") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "bk_edition_differentiator", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-8") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "bk_edition_number", type: "int4")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-9") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "bk_edition_statement", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-10") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "bk_first_author", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-11") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "bk_first_editor", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-12") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_gokb_id", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-13") {
		addColumn(schemaName: "public", tableName: "title_instance_package_platform") {
			column(name: "tipp_gokb_id", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-14") {
		modifyDataType(columnName: "pd_used_for_logic", newDataType: "bool", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-15") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pd_used_for_logic", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-16") {
		modifyDataType(columnName: "se_id", newDataType: "int8", tableName: "system_event")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-17") {
		dropForeignKeyConstraint(baseTableName: "alert", baseTableSchemaName: "public", constraintName: "fk589895cc431e3db")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-18") {
		dropForeignKeyConstraint(baseTableName: "alert", baseTableSchemaName: "public", constraintName: "fk589895ce0a42659")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-19") {
		dropForeignKeyConstraint(baseTableName: "comment", baseTableSchemaName: "public", constraintName: "fk38a5ee5f1226d95a")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-20") {
		dropForeignKeyConstraint(baseTableName: "comment", baseTableSchemaName: "public", constraintName: "fk38a5ee5f20b74198")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-21") {
		dropForeignKeyConstraint(baseTableName: "doc", baseTableSchemaName: "public", constraintName: "fk185381fc434ae")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-22") {
		dropForeignKeyConstraint(baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "fk30eba9a8b88bed47")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-26") {
		dropColumn(columnName: "doc_alert_fk", tableName: "doc")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-27") {
		dropColumn(columnName: "dc_alert_fk", tableName: "doc_context")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-28") {
		dropColumn(columnName: "rdv_soft_data", tableName: "refdata_category")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-29") {
		dropColumn(columnName: "rdv_soft_data", tableName: "refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-30") {
		dropTable(tableName: "alert")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-31") {
		dropTable(tableName: "comment")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-23") {
		addForeignKeyConstraint(baseColumnNames: "dc_shared_from_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8E2E554F4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1551166000616-24") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", baseTableSchemaName: "public", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1551166000616-25") {
		addForeignKeyConstraint(baseColumnNames: "pr_position_type_rv_fk", baseTableName: "person_role", baseTableSchemaName: "public", constraintName: "FKE6A16B206F376025", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1551166000616-32") {
		dropColumn(columnName: "pd_soft_data", tableName: "property_definition")
	}
}
