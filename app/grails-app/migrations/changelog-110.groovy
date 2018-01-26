databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1516952536377-1") {
		createTable(tableName: "budget_code") {
			column(autoIncrement: "true", name: "bc_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "budget_codePK")
			}

			column(name: "bc_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "bc_owner_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "bc_value", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-2") {
		createTable(tableName: "subscription_private_property") {
			column(autoIncrement: "true", name: "spp_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "subscription_PK")
			}

			column(name: "spp_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_value", type: "datetime")

			column(name: "dec_value", type: "decimal(19,2)")

			column(name: "int_value", type: "integer")

			column(name: "note", type: "varchar(255)")

			column(name: "spp_owner_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "bigint")

			column(name: "string_value", type: "varchar(255)")

			column(name: "spp_type_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-3") {
		createTable(tableName: "task") {
			column(autoIncrement: "true", name: "tsk_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "taskPK")
			}

			column(name: "tsk_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tsk_create_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "tsk_creator_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tsk_description", type: "varchar(255)")

			column(name: "tsk_end_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "tsk_lic_fk", type: "bigint")

			column(name: "tsk_org_fk", type: "bigint")

			column(name: "tsk_pkg_fk", type: "bigint")

			column(name: "tsk_responsible_org_fk", type: "bigint")

			column(name: "tsk_responsible_user_fk", type: "bigint")

			column(name: "tsk_status_rdv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "tsk_sub_fk", type: "bigint")

			column(name: "tsk_title", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-4") {
		addColumn(tableName: "address") {
			column(name: "adr_country_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-5") {
		addColumn(tableName: "address") {
			column(name: "adr_state_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-6") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_currency_rate", type: "double precision")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-7") {
		addColumn(tableName: "cost_item_group") {
			column(name: "cig_budget_code_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-8") {
		addColumn(tableName: "cost_item_group") {
			column(name: "cig_cost_item_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-9") {
		addColumn(tableName: "org") {
			column(name: "org_country_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-10") {
		addColumn(tableName: "org") {
			column(name: "org_federal_state_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-11") {
		addColumn(tableName: "org") {
			column(name: "org_fte_staff", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-12") {
		addColumn(tableName: "org") {
			column(name: "org_fte_students", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-13") {
		addColumn(tableName: "org") {
			column(name: "org_funder_type_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-14") {
		addColumn(tableName: "org") {
			column(name: "org_import_source", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-15") {
		addColumn(tableName: "org") {
			column(name: "org_last_import_date", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-16") {
		addColumn(tableName: "org") {
			column(name: "org_library_network_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-17") {
		addColumn(tableName: "org") {
			column(name: "org_library_type_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-18") {
		addColumn(tableName: "org") {
			column(name: "org_shortname", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-19") {
		addColumn(tableName: "org") {
			column(name: "org_sortname", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-20") {
		addColumn(tableName: "org") {
			column(name: "org_url", type: "varchar(512)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-21") {
		addColumn(tableName: "person") {
			column(name: "prs_contact_type_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-22") {
		addColumn(tableName: "person") {
			column(name: "prs_role_type_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-23") {
		addColumn(tableName: "subscription") {
			column(name: "sub_manual_cancellation_date", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-24") {
		addColumn(tableName: "subscription") {
			column(name: "sub_previous_subscription_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-25") {
		dropForeignKeyConstraint(baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE05B522EC25")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-26") {
		dropForeignKeyConstraint(baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE051D1B4283")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-27") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C5CC2FB63")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-28") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66CD2A25EFB")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-29") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C467CFA43")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-30") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C4CB39BA6")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-31") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C40C7D5B5")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-32") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0F8B5EA69")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-33") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0EF8C4AB4")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-34") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDF2F1DD172")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-35") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFE9F8A801")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-36") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFCF47BC89")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-37") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB55D5F69B")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-38") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB3313FD03")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-39") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770F7B0024B0")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-40") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FE27A4895")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-41") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FAAD0839C")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-42") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439FB7B3B52")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-43") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439F39056212")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-44") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157CC172760")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-45") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157F0E2D5FD")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-46") {
		dropForeignKeyConstraint(baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656AC258E067")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-47") {
		dropForeignKeyConstraint(baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656ACB238934")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-73") {
		dropIndex(indexName: "fact_uid_idx", tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-74") {
		dropIndex(indexName: "oplt_el_id_idx", tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-75") {
		dropIndex(indexName: "oput_entry_idx", tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-76") {
		dropIndex(indexName: "opul_entry_idx", tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-77") {
		createIndex(indexName: "FKBB979BF42B7740D8", tableName: "address") {
			column(name: "adr_country_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-78") {
		createIndex(indexName: "FKBB979BF4AB162193", tableName: "address") {
			column(name: "adr_state_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-79") {
		createIndex(indexName: "FK5A27AA73E6E3D56", tableName: "budget_code") {
			column(name: "bc_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-80") {
		createIndex(indexName: "FK1C3AAE05354541BA", tableName: "cost_item_group") {
			column(name: "cig_cost_item_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-81") {
		createIndex(indexName: "FK1C3AAE05C76503AA", tableName: "cost_item_group") {
			column(name: "cig_budget_code_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-82") {
		createIndex(indexName: "FK1AEE43BA0844B", tableName: "org") {
			column(name: "org_library_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-83") {
		createIndex(indexName: "FK1AEE46358561", tableName: "org") {
			column(name: "org_library_network_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-84") {
		createIndex(indexName: "FK1AEE47FFCF8A6", tableName: "org") {
			column(name: "org_federal_state_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-85") {
		createIndex(indexName: "FK1AEE4DF37389E", tableName: "org") {
			column(name: "org_funder_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-86") {
		createIndex(indexName: "FK1AEE4E4DF64D", tableName: "org") {
			column(name: "org_country_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-87") {
		createIndex(indexName: "org_shortname_idx", tableName: "org") {
			column(name: "org_shortname")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-88") {
		createIndex(indexName: "org_sortname_idx", tableName: "org") {
			column(name: "org_sortname")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-89") {
		createIndex(indexName: "FKC4E39B55840D6E7", tableName: "person") {
			column(name: "prs_role_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-90") {
		createIndex(indexName: "FKC4E39B55BEFE2AD9", tableName: "person") {
			column(name: "prs_contact_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-91") {
		createIndex(indexName: "FK1456591D93740D18", tableName: "subscription") {
			column(name: "sub_previous_subscription_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-92") {
		createIndex(indexName: "FK229733F32992A286", tableName: "subscription_private_property") {
			column(name: "ref_value_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-93") {
		createIndex(indexName: "FK229733F3831290F7", tableName: "subscription_private_property") {
			column(name: "spp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-94") {
		createIndex(indexName: "FK229733F390E864A1", tableName: "subscription_private_property") {
			column(name: "spp_type_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-95") {
		createIndex(indexName: "FK36358511779714", tableName: "task") {
			column(name: "tsk_pkg_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-96") {
		createIndex(indexName: "FK36358544918415", tableName: "task") {
			column(name: "tsk_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-97") {
		createIndex(indexName: "FK3635857A67C71A", tableName: "task") {
			column(name: "tsk_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-98") {
		createIndex(indexName: "FK36358582D7B803", tableName: "task") {
			column(name: "tsk_responsible_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-99") {
		createIndex(indexName: "FK36358587D78EBF", tableName: "task") {
			column(name: "tsk_creator_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-100") {
		createIndex(indexName: "FK363585C4FDAF11", tableName: "task") {
			column(name: "tsk_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-101") {
		createIndex(indexName: "FK363585CD8C87CB", tableName: "task") {
			column(name: "tsk_status_rdv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-102") {
		createIndex(indexName: "FK363585F6B8D731", tableName: "task") {
			column(name: "tsk_responsible_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-103") {
		dropColumn(columnName: "adr_country", tableName: "address")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-104") {
		dropColumn(columnName: "adr_state", tableName: "address")
	}

	//changeSet(author: "kloberd (generated)", id: "1516952536377-105") {
	//	dropColumn(columnName: "cig_budgetcode_fk", tableName: "cost_item_group")
	//}

	//changeSet(author: "kloberd (generated)", id: "1516952536377-106") {
	//	dropColumn(columnName: "cig_costItem_fk", tableName: "cost_item_group")
	//}

	changeSet(author: "kloberd (generated)", id: "1516952536377-107") {
		dropColumn(columnName: "lic_contact", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-108") {
		dropColumn(columnName: "lic_licensee_ref", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-109") {
		dropColumn(columnName: "lic_licensor_ref", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-110") {
		dropTable(tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-111") {
		dropTable(tableName: "object_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-112") {
		dropTable(tableName: "object_property")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-113") {
		dropTable(tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-114") {
		dropTable(tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-115") {
		dropTable(tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-116") {
		dropTable(tableName: "onixpl_usage_term_refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-117") {
		dropTable(tableName: "org_bck")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-118") {
		dropTable(tableName: "org_cluster")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-119") {
		dropTable(tableName: "org_title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-120") {
		dropTable(tableName: "private_property_rule")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-121") {
		dropTable(tableName: "property_value")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-122") {
		dropTable(tableName: "type_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-48") {
		addForeignKeyConstraint(baseColumnNames: "adr_country_rv_fk", baseTableName: "address", constraintName: "FKBB979BF42B7740D8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-49") {
		addForeignKeyConstraint(baseColumnNames: "adr_state_rv_fk", baseTableName: "address", constraintName: "FKBB979BF4AB162193", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-50") {
		addForeignKeyConstraint(baseColumnNames: "bc_owner_fk", baseTableName: "budget_code", constraintName: "FK5A27AA73E6E3D56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-51") {
		addForeignKeyConstraint(baseColumnNames: "cig_budget_code_fk", baseTableName: "cost_item_group", constraintName: "FK1C3AAE05C76503AA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "bc_id", referencedTableName: "budget_code", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-52") {
		addForeignKeyConstraint(baseColumnNames: "cig_cost_item_fk", baseTableName: "cost_item_group", constraintName: "FK1C3AAE05354541BA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ci_id", referencedTableName: "cost_item", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1516952536377-53") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1516952536377-54") {
		addForeignKeyConstraint(baseColumnNames: "org_country_rv_fk", baseTableName: "org", constraintName: "FK1AEE4E4DF64D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-55") {
		addForeignKeyConstraint(baseColumnNames: "org_federal_state_rv_fk", baseTableName: "org", constraintName: "FK1AEE47FFCF8A6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-56") {
		addForeignKeyConstraint(baseColumnNames: "org_funder_type_rv_fk", baseTableName: "org", constraintName: "FK1AEE4DF37389E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-57") {
		addForeignKeyConstraint(baseColumnNames: "org_library_network_rv_fk", baseTableName: "org", constraintName: "FK1AEE46358561", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-58") {
		addForeignKeyConstraint(baseColumnNames: "org_library_type_rv_fk", baseTableName: "org", constraintName: "FK1AEE43BA0844B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-59") {
		addForeignKeyConstraint(baseColumnNames: "prs_contact_type_rv_fk", baseTableName: "person", constraintName: "FKC4E39B55BEFE2AD9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-60") {
		addForeignKeyConstraint(baseColumnNames: "prs_role_type_rv_fk", baseTableName: "person", constraintName: "FKC4E39B55840D6E7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-61") {
		addForeignKeyConstraint(baseColumnNames: "sub_previous_subscription_fk", baseTableName: "subscription", constraintName: "FK1456591D93740D18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-62") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "subscription_private_property", constraintName: "FK229733F32992A286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-63") {
		addForeignKeyConstraint(baseColumnNames: "spp_owner_fk", baseTableName: "subscription_private_property", constraintName: "FK229733F3831290F7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-64") {
		addForeignKeyConstraint(baseColumnNames: "spp_type_fk", baseTableName: "subscription_private_property", constraintName: "FK229733F390E864A1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-65") {
		addForeignKeyConstraint(baseColumnNames: "tsk_creator_fk", baseTableName: "task", constraintName: "FK36358587D78EBF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-66") {
		addForeignKeyConstraint(baseColumnNames: "tsk_lic_fk", baseTableName: "task", constraintName: "FK36358544918415", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-67") {
		addForeignKeyConstraint(baseColumnNames: "tsk_org_fk", baseTableName: "task", constraintName: "FK3635857A67C71A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-68") {
		addForeignKeyConstraint(baseColumnNames: "tsk_pkg_fk", baseTableName: "task", constraintName: "FK36358511779714", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-69") {
		addForeignKeyConstraint(baseColumnNames: "tsk_responsible_org_fk", baseTableName: "task", constraintName: "FK36358582D7B803", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-70") {
		addForeignKeyConstraint(baseColumnNames: "tsk_responsible_user_fk", baseTableName: "task", constraintName: "FK363585F6B8D731", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-71") {
		addForeignKeyConstraint(baseColumnNames: "tsk_status_rdv_fk", baseTableName: "task", constraintName: "FK363585CD8C87CB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1516952536377-72") {
		addForeignKeyConstraint(baseColumnNames: "tsk_sub_fk", baseTableName: "task", constraintName: "FK363585C4FDAF11", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencesUniqueColumn: "false")
	}
}
