databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1553690957108-1") {
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

	changeSet(author: "kloberd (modified)", id: "1553690957108-2") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.org_roletype RENAME TO org_type")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-3") {
		createTable(schemaName: "public", tableName: "survey_config") {
			column(autoIncrement: "true", name: "surConf_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_configPK")
			}

			column(name: "surConf_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surConf_comment", type: "varchar(255)")

			column(name: "surConf_configOrder", type: "int4") {
				constraints(nullable: "false")
			}

			column(name: "surConf_dateCreated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surConf_header", type: "varchar(255)")

			column(name: "surConf_lastUpdated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surConf_orgIDs", type: "bytea")

			column(name: "surConf_sub_fk", type: "int8")

			column(name: "surConf_surInfo_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surConf_surProp_fk", type: "int8")

			column(name: "surConf_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "survey_configs_idx", type: "int4")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-4") {
		createTable(schemaName: "public", tableName: "survey_config_doc") {
			column(name: "survey_config_docs_id", type: "int8")

			column(name: "doc_id", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-5") {
		createTable(schemaName: "public", tableName: "survey_config_properties") {
			column(autoIncrement: "true", name: "surConPro_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_configPK2")
			}

			column(name: "surConPro_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surConPro_dateCreated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surConPro_lastUpdated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surConPro_surveyConfig", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surConPro_surveyProperty", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-6") {
		createTable(schemaName: "public", tableName: "survey_info") {
			column(autoIncrement: "true", name: "surIn_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_infoPK")
			}

			column(name: "surIn_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surIn_comment", type: "text")

			column(name: "surIn_dateCreated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surIn_endDate", type: "timestamp")

			column(name: "surIn_lastUpdated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surIn_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "surIn_owner_org_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surIn_startDate", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "status_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surIn_type_rv_fk ", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-7") {
		createTable(schemaName: "public", tableName: "survey_property") {
			column(autoIncrement: "true", name: "surPro_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_properPK")
			}

			column(name: "surPro_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surPro_comment", type: "text")

			column(name: "surPro_dateCreated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surPro_explain", type: "text")

			column(name: "hard_data", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "surPro_introduction", type: "text")

			column(name: "surPro_lastUpdated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surPro_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "surPro_org_fk", type: "int8")

			column(name: "surPro_refdataCategory", type: "varchar(255)")

			column(name: "surPro_type", type: "text") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-8") {
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

	changeSet(author: "kloberd (generated)", id: "1553690957108-9") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_financial_year", type: "bytea")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-10") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_tax_enum", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-11") {
		addColumn(schemaName: "public", tableName: "doc") {
			column(name: "doc_owner_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-12") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_org_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-13") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_share_conf_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-14") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_is_shared", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-15") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_shared_from_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-16") {
		dropForeignKeyConstraint(baseTableName: "org", baseTableSchemaName: "public", constraintName: "fk1aee4360f7147")
	}

	changeSet(author: "kloberd (modified)", id: "1553690957108-17") {
		grailsChange {
			change {
				sql.execute("DELETE FROM public.setting WHERE set_name = 'AutoApproveMemberships'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1553690957108-18") {
		grailsChange {
			change {
				sql.execute("DELETE FROM public.user_org WHERE status = 4")
				sql.execute("UPDATE public.user_org SET status = 1 WHERE status = 3")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-19") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcbb3bc6e31")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-20") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcbfd5bdbc3")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-21") {
		dropForeignKeyConstraint(baseTableName: "user", baseTableSchemaName: "public", constraintName: "fk36ebcb6d1f0ce8")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-41") {
		createIndex(indexName: "os_org_idx", schemaName: "public", tableName: "org_settings") {
			column(name: "os_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-42") {
		createIndex(indexName: "unique_os_org_fk", schemaName: "public", tableName: "org_settings", unique: "true") {
			column(name: "os_key_enum")

			column(name: "os_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-43") {
		dropColumn(columnName: "org_fte_staff", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-44") {
		dropColumn(columnName: "org_fte_students", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-45") {
		dropColumn(columnName: "org_type_rv_fk", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-46") {
		dropColumn(columnName: "default_dash_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-47") {
		dropColumn(columnName: "default_page_size", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-48") {
		dropColumn(columnName: "instcode", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-49") {
		dropColumn(columnName: "instname", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-50") {
		dropColumn(columnName: "show_info_icon_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-51") {
		dropColumn(columnName: "show_simple_views_id", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-52") {
		dropColumn(columnName: "role", tableName: "user_org")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-22") {
		addForeignKeyConstraint(baseColumnNames: "doc_owner_fk", baseTableName: "doc", baseTableSchemaName: "public", constraintName: "FK18538CF6FEA3F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-23") {
		addForeignKeyConstraint(baseColumnNames: "dc_org_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8DA149D07", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-24") {
		addForeignKeyConstraint(baseColumnNames: "dc_share_conf_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A862679151", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-25") {
		addForeignKeyConstraint(baseColumnNames: "or_shared_from_fk", baseTableName: "org_role", baseTableSchemaName: "public", constraintName: "FK4E5C38F1E9DF17D9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "or_id", referencedTableName: "org_role", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-26") {
		addForeignKeyConstraint(baseColumnNames: "os_org_fk", baseTableName: "org_settings", baseTableSchemaName: "public", constraintName: "FK4852BF1EB7183002", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-27") {
		addForeignKeyConstraint(baseColumnNames: "os_rv_fk", baseTableName: "org_settings", baseTableSchemaName: "public", constraintName: "FK4852BF1E6E4A9836", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-28") {
		addForeignKeyConstraint(baseColumnNames: "org_id", baseTableName: "org_type", baseTableSchemaName: "public", constraintName: "FK4E5D47B521D4E99D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-29") {
		addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "org_type", baseTableSchemaName: "public", constraintName: "FK4E5D47B5AAD0839C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-30") {
		addForeignKeyConstraint(baseColumnNames: "surConf_sub_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC78CE2BE69", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-31") {
		addForeignKeyConstraint(baseColumnNames: "surConf_surInfo_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC72EDF58B6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surIn_id", referencedTableName: "survey_info", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-32") {
		addForeignKeyConstraint(baseColumnNames: "surConf_surProp_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC7B109E788", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surPro_id", referencedTableName: "survey_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-33") {
		addForeignKeyConstraint(baseColumnNames: "doc_id", baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "FKA30538C0EE3591D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "doc_id", referencedTableName: "doc", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-34") {
		addForeignKeyConstraint(baseColumnNames: "survey_config_docs_id", baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "FKA30538C05C88A960", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surConf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-35") {
		addForeignKeyConstraint(baseColumnNames: "surConPro_surveyConfig", baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "FKEE6A50ABDA11CCD9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surConf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-36") {
		addForeignKeyConstraint(baseColumnNames: "surConPro_surveyProperty", baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "FKEE6A50AB6B9640BF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surPro_id", referencedTableName: "survey_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-37") {
		addForeignKeyConstraint(baseColumnNames: "status_id", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE737C3CDA39", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1553690957108-38") {
		addForeignKeyConstraint(baseColumnNames: "surIn_owner_org_fk", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE7311798CDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	// error? changeSet(author: "kloberd (generated)", id: "1553690957108-39") {
	//	addForeignKeyConstraint(baseColumnNames: "surIn_type_rv_fk ", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE737D43A40C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1553690957108-40") {
		addForeignKeyConstraint(baseColumnNames: "surPro_org_fk", baseTableName: "survey_property", baseTableSchemaName: "public", constraintName: "FK594D0D1AAB666169", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
