databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1539850878639-1") {
		createTable(tableName: "audit_config") {
			column(autoIncrement: "true", name: "auc_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "audit_configPK")
			}

			column(name: "auc_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "auc_reference_class", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "auc_reference_field", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "auc_reference_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-2") {
		createTable(tableName: "fact") {
			column(autoIncrement: "true", name: "fact_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "factPK")
			}

			column(name: "fact_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "fact_from", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "fact_metric_rdv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "fact_to", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "fact_type_rdv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "fact_uid", type: "varchar(255)")

			column(name: "fact_value", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "inst_id", type: "bigint")

			column(name: "juspio_id", type: "bigint")

			column(name: "related_title_id", type: "bigint")

			column(name: "reporting_month", type: "bigint")

			column(name: "reporting_year", type: "bigint")

			column(name: "supplier_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-3") {
		createTable(tableName: "org_roletype") {
			column(name: "org_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "refdata_value_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-4") {
		createTable(tableName: "user_settings") {
			column(autoIncrement: "true", name: "us_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_settingsPK")
			}

			column(name: "us_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "us_key_enum", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "us_org_fk", type: "bigint")

			column(name: "us_rv_fk", type: "bigint")

			column(name: "us_string_value", type: "varchar(255)")

			column(name: "us_user_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-5") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_is_viewable", type: "bit")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-6") {
		addColumn(tableName: "license_custom_property") {
			column(name: "instance_of_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-7") {
		addColumn(tableName: "org_access_point_link") {
			column(name: "subscription_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-8") {
		addColumn(tableName: "pending_change") {
			column(name: "pc_msg_doc", type: "longtext")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-9") {
		addColumn(tableName: "pending_change") {
			column(name: "pc_msg_token", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-10") {
		addColumn(tableName: "subscription_custom_property") {
			column(name: "instance_of_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-11") {
		modifyDataType(columnName: "tsk_description", newDataType: "longtext", tableName: "task")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-12") {
		dropForeignKeyConstraint(baseTableName: "identifier_relation", baseTableSchemaName: "laser", constraintName: "FKDA4DACD2BB32F750")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-13") {
		dropForeignKeyConstraint(baseTableName: "identifier_relation", baseTableSchemaName: "laser", constraintName: "FKDA4DACD2AD4573E3")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-14") {
		dropForeignKeyConstraint(baseTableName: "identifier_relation", baseTableSchemaName: "laser", constraintName: "FKDA4DACD22ED80AE1")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-15") {
		dropForeignKeyConstraint(baseTableName: "kbplus_fact", baseTableSchemaName: "laser", constraintName: "FK6784767A5CC2FB63")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-16") {
		dropForeignKeyConstraint(baseTableName: "kbplus_fact", baseTableSchemaName: "laser", constraintName: "FK6784767AD2A25EFB")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-17") {
		dropForeignKeyConstraint(baseTableName: "kbplus_fact", baseTableSchemaName: "laser", constraintName: "FK6784767A467CFA43")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-18") {
		dropForeignKeyConstraint(baseTableName: "kbplus_fact", baseTableSchemaName: "laser", constraintName: "FK6784767A4CB39BA6")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-19") {
		dropForeignKeyConstraint(baseTableName: "kbplus_fact", baseTableSchemaName: "laser", constraintName: "FK6784767A40C7D5B5")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-20") {
		dropForeignKeyConstraint(baseTableName: "link", baseTableSchemaName: "laser", constraintName: "FK32AFFACB535FB2")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-21") {
		dropForeignKeyConstraint(baseTableName: "link", baseTableSchemaName: "laser", constraintName: "FK32AFFA2C22D5CE")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-22") {
		dropForeignKeyConstraint(baseTableName: "link", baseTableSchemaName: "laser", constraintName: "FK32AFFA9BEE17E9")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-23") {
		dropForeignKeyConstraint(baseTableName: "link", baseTableSchemaName: "laser", constraintName: "FK32AFFA158EDE81")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-24") {
		dropForeignKeyConstraint(baseTableName: "link", baseTableSchemaName: "laser", constraintName: "FK32AFFA38280671")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-25") {
		dropForeignKeyConstraint(baseTableName: "org_access_point_link", baseTableSchemaName: "laser", constraintName: "FK1C324E69D9ED26FD")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-41") {
		dropIndex(indexName: "jusp_cursor_idx", tableName: "jusp_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-42") {
		dropIndex(indexName: "fact_access_idx", tableName: "kbplus_fact")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-43") {
		dropIndex(indexName: "fact_uid_idx", tableName: "kbplus_fact")
	}

	//only qa, not master
	//changeSet(author: "kloberd (generated)", id: "1539850878639-44") {
	//	dropIndex(indexName: "fact_uid_uniq_1508320874102", tableName: "kbplus_fact")
	//}

	changeSet(author: "kloberd (generated)", id: "1539850878639-45") {
		createIndex(indexName: "FK2FD66C40C7D5B5", tableName: "fact") {
			column(name: "supplier_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-46") {
		createIndex(indexName: "FK2FD66C467CFA43", tableName: "fact") {
			column(name: "juspio_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-47") {
		createIndex(indexName: "FK2FD66C4CB39BA6", tableName: "fact") {
			column(name: "related_title_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-48") {
		createIndex(indexName: "FK2FD66C5CC2FB63", tableName: "fact") {
			column(name: "fact_type_rdv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-49") {
		createIndex(indexName: "FK2FD66CD2A25EFB", tableName: "fact") {
			column(name: "inst_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-50") {
		createIndex(indexName: "FK2FD66CDB4D8CED", tableName: "fact") {
			column(name: "fact_metric_rdv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-51") {
		createIndex(indexName: "fact_access_idx", tableName: "fact") {
			column(name: "inst_id")

			column(name: "related_title_id")

			column(name: "supplier_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-52") {
		createIndex(indexName: "fact_metric_idx", tableName: "fact") {
			column(name: "fact_metric_rdv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-53") {
		createIndex(indexName: "fact_uid_idx", tableName: "fact") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-54") {
		createIndex(indexName: "fact_uid_uniq_1539850877880", tableName: "fact", unique: "true") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-55") {
		createIndex(indexName: "FKE8DF0AE5F9C56B03", tableName: "license_custom_property") {
			column(name: "instance_of_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-56") {
		createIndex(indexName: "FK1C324E694DFC6D97", tableName: "org_access_point_link") {
			column(name: "subscription_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-57") {
		createIndex(indexName: "FKE2FAE7AB21D4E99D", tableName: "org_roletype") {
			column(name: "org_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-58") {
		createIndex(indexName: "FKE2FAE7ABAAD0839C", tableName: "org_roletype") {
			column(name: "refdata_value_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-59") {
		createIndex(indexName: "FK8717A7C18BC51D79", tableName: "subscription_custom_property") {
			column(name: "instance_of_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-60") {
		createIndex(indexName: "FK5886161730B2CA08", tableName: "user_settings") {
			column(name: "us_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-61") {
		createIndex(indexName: "FK5886161744005CC", tableName: "user_settings") {
			column(name: "us_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-62") {
		createIndex(indexName: "FK58861617DD91A570", tableName: "user_settings") {
			column(name: "us_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-63") {
		createIndex(indexName: "unique_us_user_fk", tableName: "user_settings", unique: "true") {
			column(name: "us_key_enum")

			column(name: "us_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-64") {
		dropColumn(columnName: "license_id", tableName: "org_access_point_link")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-65") {
		dropTable(tableName: "identifier_relation")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-66") {
		dropTable(tableName: "jusp_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-67") {
		dropTable(tableName: "kbplus_fact")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-68") {
		dropTable(tableName: "link")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-26") {
		addForeignKeyConstraint(baseColumnNames: "fact_metric_rdv_fk", baseTableName: "fact", constraintName: "FK2FD66CDB4D8CED", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-27") {
		addForeignKeyConstraint(baseColumnNames: "fact_type_rdv_fk", baseTableName: "fact", constraintName: "FK2FD66C5CC2FB63", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-28") {
		addForeignKeyConstraint(baseColumnNames: "inst_id", baseTableName: "fact", constraintName: "FK2FD66CD2A25EFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-29") {
		addForeignKeyConstraint(baseColumnNames: "juspio_id", baseTableName: "fact", constraintName: "FK2FD66C467CFA43", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "io_id", referencedTableName: "identifier_occurrence", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-30") {
		addForeignKeyConstraint(baseColumnNames: "related_title_id", baseTableName: "fact", constraintName: "FK2FD66C4CB39BA6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-31") {
		addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "fact", constraintName: "FK2FD66C40C7D5B5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1539850878639-32") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1539850878639-33") {
		addForeignKeyConstraint(baseColumnNames: "instance_of_id", baseTableName: "license_custom_property", constraintName: "FKE8DF0AE5F9C56B03", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "license_custom_property", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-34") {
		addForeignKeyConstraint(baseColumnNames: "subscription_id", baseTableName: "org_access_point_link", constraintName: "FK1C324E694DFC6D97", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-35") {
		addForeignKeyConstraint(baseColumnNames: "org_id", baseTableName: "org_roletype", constraintName: "FKE2FAE7AB21D4E99D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-36") {
		addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "org_roletype", constraintName: "FKE2FAE7ABAAD0839C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-37") {
		addForeignKeyConstraint(baseColumnNames: "instance_of_id", baseTableName: "subscription_custom_property", constraintName: "FK8717A7C18BC51D79", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "subscription_custom_property", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-38") {
		addForeignKeyConstraint(baseColumnNames: "us_org_fk", baseTableName: "user_settings", constraintName: "FK5886161730B2CA08", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-39") {
		addForeignKeyConstraint(baseColumnNames: "us_rv_fk", baseTableName: "user_settings", constraintName: "FK58861617DD91A570", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1539850878639-40") {
		addForeignKeyConstraint(baseColumnNames: "us_user_fk", baseTableName: "user_settings", constraintName: "FK5886161744005CC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
