databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1553857368060-1") {
		createTable(schemaName: "public", tableName: "org_settings") {
			column(autoIncrement: "true", name: "os_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "org_settingsPK")
			}

			column(name: "os_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "os_key_enum", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "os_org_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "os_rv_fk", type: "int8")

			column(name: "os_string_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1553857368060-2") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.org_roletype RENAME TO org_type")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-3") {
		createTable(schemaName: "public", tableName: "survey_config") {
			column(autoIncrement: "true", name: "surconf_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_configPK")
			}

			column(name: "surconf_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surconf_comment", type: "varchar(255)")

			column(name: "surconf_config_order_fk", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "surconf_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surconf_header", type: "varchar(255)")

			column(name: "surconf_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surconf_org_ids", type: "bytea")

			column(name: "surconf_sub_fk", type: "int8")

			column(name: "surconf_surinfo_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surconf_surprop_fk", type: "int8")

			column(name: "surconf_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "survey_configs_idx", type: "int4")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-4") {
		createTable(schemaName: "public", tableName: "survey_config_doc") {
			column(name: "survey_config_docs_id", type: "int8")

			column(name: "doc_id", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-5") {
		createTable(schemaName: "public", tableName: "survey_config_properties") {
			column(autoIncrement: "true", name: "surconpro_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_configPK2")
			}

			column(name: "surconpro_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surconpro_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surconpro_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surconpro_survey_config_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surconpro_survey_property_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-6") {
		createTable(schemaName: "public", tableName: "survey_info") {
			column(autoIncrement: "true", name: "surin_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_infoPK")
			}

			column(name: "surin_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surin_comment", type: "text")

			column(name: "surin_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surin_end_date", type: "timestamp")

			column(name: "surin_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surin_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "surin_owner_org_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surin_start_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "status_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surin_type_rv_fk ", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-7") {
		createTable(schemaName: "public", tableName: "survey_property") {
			column(autoIncrement: "true", name: "surpro_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_properPK")
			}

			column(name: "surpro_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surpro_comment", type: "text")

			column(name: "surpro_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surpro_explain", type: "text")

			column(name: "hard_data", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "surpro_introduction", type: "text")

			column(name: "surpro_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surpro_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "surpro_org_fk", type: "int8")

			column(name: "surpro_refdata_category", type: "varchar(255)")

			column(name: "surpro_type", type: "text") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-8") {
		createTable(schemaName: "public", tableName: "survey_result") {
			column(autoIncrement: "true", name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_resultPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-9") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_financial_year", type: "bytea")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-10") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_tax_enum", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-11") {
		addColumn(schemaName: "public", tableName: "doc") {
			column(name: "doc_owner_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-12") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_org_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-13") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_share_conf_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-14") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_is_shared", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-15") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_shared_from_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-16") {
		dropForeignKeyConstraint(baseTableName: "org", baseTableSchemaName: "public", constraintName: "fk1aee4360f7147")
	}

	changeSet(author: "kloberd (modified)", id: "1553857368060-17") {
		grailsChange {
			change {
				sql.execute("DELETE FROM public.setting WHERE set_name = 'AutoApproveMemberships'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1553857368060-18") {
		grailsChange {
			change {
				sql.execute("DELETE FROM public.user_org WHERE status = 4")
				sql.execute("UPDATE public.user_org SET status = 1 WHERE status = 3")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-19") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcbb3bc6e31")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-20") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcbfd5bdbc3")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-21") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcb6d1f0ce8")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-41") {
		createIndex(indexName: "os_org_idx", schemaName: "public", tableName: "org_settings") {
			column(name: "os_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-42") {
		createIndex(indexName: "unique_os_org_fk", schemaName: "public", tableName: "org_settings", unique: "true") {
			column(name: "os_key_enum")

			column(name: "os_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-43") {
		dropColumn(columnName: "org_fte_staff", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-44") {
		dropColumn(columnName: "org_fte_students", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-45") {
		dropColumn(columnName: "org_type_rv_fk", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-46") {
		dropColumn(columnName: "default_dash_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-47") {
		dropColumn(columnName: "default_page_size", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-48") {
		dropColumn(columnName: "instcode", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-49") {
		dropColumn(columnName: "instname", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-50") {
		dropColumn(columnName: "show_info_icon_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-51") {
		dropColumn(columnName: "show_simple_views_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-52") {
		dropColumn(columnName: "role", tableName: "user_org")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-22") {
		addForeignKeyConstraint(baseColumnNames: "doc_owner_fk", baseTableName: "doc", baseTableSchemaName: "public", constraintName: "FK18538CF6FEA3F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-23") {
		addForeignKeyConstraint(baseColumnNames: "dc_org_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8DA149D07", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-24") {
		addForeignKeyConstraint(baseColumnNames: "dc_share_conf_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A862679151", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-25") {
		addForeignKeyConstraint(baseColumnNames: "or_shared_from_fk", baseTableName: "org_role", baseTableSchemaName: "public", constraintName: "FK4E5C38F1E9DF17D9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "or_id", referencedTableName: "org_role", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-26") {
		addForeignKeyConstraint(baseColumnNames: "os_org_fk", baseTableName: "org_settings", baseTableSchemaName: "public", constraintName: "FK4852BF1EB7183002", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-27") {
		addForeignKeyConstraint(baseColumnNames: "os_rv_fk", baseTableName: "org_settings", baseTableSchemaName: "public", constraintName: "FK4852BF1E6E4A9836", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1553857368060-28") {
	//	addForeignKeyConstraint(baseColumnNames: "org_id", baseTableName: "org_type", baseTableSchemaName: "public", constraintName: "FK4E5D47B521D4E99D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	//}

	//changeSet(author: "kloberd (generated)", id: "1553857368060-29") {
	//	addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "org_type", baseTableSchemaName: "public", constraintName: "FK4E5D47B5AAD0839C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1553857368060-30") {
		addForeignKeyConstraint(baseColumnNames: "surconf_sub_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC78CE2BE69", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-31") {
		addForeignKeyConstraint(baseColumnNames: "surconf_surinfo_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC72EDF58B6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surin_id", referencedTableName: "survey_info", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-32") {
		addForeignKeyConstraint(baseColumnNames: "surconf_surprop_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC7B109E788", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surpro_id", referencedTableName: "survey_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-33") {
		addForeignKeyConstraint(baseColumnNames: "doc_id", baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "FKA30538C0EE3591D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "doc_id", referencedTableName: "doc", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-34") {
		addForeignKeyConstraint(baseColumnNames: "survey_config_docs_id", baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "FKA30538C05C88A960", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-35") {
		addForeignKeyConstraint(baseColumnNames: "surconpro_survey_config_fk", baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "FKEE6A50AB6CCC73FA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-36") {
		addForeignKeyConstraint(baseColumnNames: "surconpro_survey_property_fk", baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "FKEE6A50ABA2C7E99A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surpro_id", referencedTableName: "survey_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-37") {
		addForeignKeyConstraint(baseColumnNames: "status_id", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE737C3CDA39", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-38") {
		addForeignKeyConstraint(baseColumnNames: "surin_owner_org_fk", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE7311798CDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-39") {
		addForeignKeyConstraint(baseColumnNames: "surin_type_rv_fk ", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE737D43A40C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553857368060-40") {
		addForeignKeyConstraint(baseColumnNames: "surpro_org_fk", baseTableName: "survey_property", baseTableSchemaName: "public", constraintName: "FK594D0D1AAB666169", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
