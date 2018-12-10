databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1542893167281-1") {
		createTable(tableName: "property_definition_group") {
			column(autoIncrement: "true", name: "pdg_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "property_defiPK")
			}

			column(name: "pdg_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pdg_description", type: "longtext")

			column(name: "pdg_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "pdg_owner_type", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "pdg_tenant_fk", type: "bigint")

			column(name: "pdg_visible_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-2") {
		createTable(tableName: "property_definition_group_binding") {
			column(autoIncrement: "true", name: "pgb_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "property_defiPK")
			}

			column(name: "pgb_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pgb_lic_fk", type: "bigint")

			column(name: "pgb_org_fk", type: "bigint")

			column(name: "pgb_property_definition_group_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pgb_sub_fk", type: "bigint")

			column(name: "pbg_visible_rv_fk", type: "bigint")

			column(name: "pbg_is_viewable_rv_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-3") {
		createTable(tableName: "property_definition_group_item") {
			column(autoIncrement: "true", name: "pde_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "property_defiPK")
			}

			column(name: "pde_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pde_property_definition_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pde_property_definition_group_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-4") {
		createTable(tableName: "system_message") {
			column(autoIncrement: "true", name: "sm_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_messagPK")
			}

			column(name: "sm_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sm_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "sm_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "sm_org_fk", type: "bigint")

			column(name: "sm_showNow", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "sm_text", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-5") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_hard_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-6") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "fact_type_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-7") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "jerror", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-8") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "num_facts", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-9") {
		addColumn(tableName: "subscription") {
			column(name: "sub_form_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-10") {
		addColumn(tableName: "subscription") {
			column(name: "sub_resource_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-26") {
		createIndex(indexName: "adr_org_idx", tableName: "address") {
			column(name: "adr_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-27") {
		createIndex(indexName: "adr_prs_idx", tableName: "address") {
			column(name: "adr_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-28") {
		createIndex(indexName: "ct_org_idx", tableName: "contact") {
			column(name: "ct_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-29") {
		createIndex(indexName: "ct_prs_idx", tableName: "contact") {
			column(name: "ct_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-30") {
		createIndex(indexName: "ci_owner_idx", tableName: "cost_item") {
			column(name: "ci_owner")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-31") {
		createIndex(indexName: "ci_sub_idx", tableName: "cost_item") {
			column(name: "ci_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-32") {
		createIndex(indexName: "i10n_ref_idx", tableName: "i10n_translation") {
			column(name: "i10n_reference_class")

			column(name: "i10n_reference_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-33") {
		createIndex(indexName: "lic_parent_idx", tableName: "license") {
			column(name: "lic_parent_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-34") {
		createIndex(indexName: "owner_idx", tableName: "license_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-35") {
		createIndex(indexName: "lpp_owner_idx", tableName: "license_private_property") {
			column(name: "lpp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-36") {
		createIndex(indexName: "owner_idx", tableName: "org_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-37") {
		createIndex(indexName: "opp_owner_idx", tableName: "org_private_property") {
			column(name: "opp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-38") {
		createIndex(indexName: "or_lic_idx", tableName: "org_role") {
			column(name: "or_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-39") {
		createIndex(indexName: "or_sub_idx", tableName: "org_role") {
			column(name: "or_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-40") {
		createIndex(indexName: "ppp_owner_idx", tableName: "person_private_property") {
			column(name: "ppp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-41") {
		createIndex(indexName: "pr_prs_org_idx", tableName: "person_role") {
			column(name: "pr_org_fk")

			column(name: "pr_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-42") {
		createIndex(indexName: "FKED224DBDA14135F8", tableName: "property_definition_group") {
			column(name: "pdg_visible_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-43") {
		createIndex(indexName: "FKED224DBDD9816975", tableName: "property_definition_group") {
			column(name: "pdg_tenant_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-44") {
		createIndex(indexName: "FK3D279603114305F7", tableName: "property_definition_group_binding") {
			column(name: "pgb_property_definition_group_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-45") {
		createIndex(indexName: "FK3D27960359B822B2", tableName: "property_definition_group_binding") {
			column(name: "pgb_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-46") {
		createIndex(indexName: "FK3D27960376A2FE3C", tableName: "property_definition_group_binding") {
			column(name: "pbg_is_viewable_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-47") {
		createIndex(indexName: "FK3D279603D94BF7B6", tableName: "property_definition_group_binding") {
			column(name: "pgb_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-48") {
		createIndex(indexName: "FK3D279603DE1D7A3A", tableName: "property_definition_group_binding") {
			column(name: "pbg_visible_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-49") {
		createIndex(indexName: "FK3D279603F223ABB", tableName: "property_definition_group_binding") {
			column(name: "pgb_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-50") {
		createIndex(indexName: "unique_pgb_lic_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-51") {
		createIndex(indexName: "unique_pgb_org_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-52") {
		createIndex(indexName: "unique_pgb_sub_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-53") {
		createIndex(indexName: "FKA535F45593156098", tableName: "property_definition_group_item") {
			column(name: "pde_property_definition_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-54") {
		createIndex(indexName: "FKA535F455D48EB011", tableName: "property_definition_group_item") {
			column(name: "pde_property_definition_group_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-55") {
		createIndex(indexName: "unique_pde_property_definition_fk", tableName: "property_definition_group_item", unique: "true") {
			column(name: "pde_property_definition_group_fk")

			column(name: "pde_property_definition_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-56") {
		createIndex(indexName: "FKB71D92B77430C71E", tableName: "stats_triple_cursor") {
			column(name: "fact_type_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-57") {
		createIndex(indexName: "FK1456591D239DCC8", tableName: "subscription") {
			column(name: "sub_resource_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-58") {
		createIndex(indexName: "FK1456591DA64FB8D2", tableName: "subscription") {
			column(name: "sub_form_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-59") {
		createIndex(indexName: "sub_parent_idx", tableName: "subscription") {
			column(name: "sub_parent_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-60") {
		createIndex(indexName: "owner_idx", tableName: "subscription_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-61") {
		createIndex(indexName: "spp_owner_idx", tableName: "subscription_private_property") {
			column(name: "spp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-62") {
		createIndex(indexName: "owner_idx", tableName: "system_admin_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-63") {
		createIndex(indexName: "FK669579F798E2DECC", tableName: "system_message") {
			column(name: "sm_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-64") {
		createIndex(indexName: "us_user_idx", tableName: "user_settings") {
			column(name: "us_user_fk")
		}
	}

	//changeSet(author: "kloberd (generated)", id: "1542893167281-11") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1542893167281-12") {
		addForeignKeyConstraint(baseColumnNames: "pdg_tenant_fk", baseTableName: "property_definition_group", constraintName: "FKED224DBDD9816975", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-13") {
		addForeignKeyConstraint(baseColumnNames: "pdg_visible_rv_fk", baseTableName: "property_definition_group", constraintName: "FKED224DBDA14135F8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-14") {
		addForeignKeyConstraint(baseColumnNames: "pbg_is_viewable_rv_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D27960376A2FE3C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-15") {
		addForeignKeyConstraint(baseColumnNames: "pbg_visible_rv_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D279603DE1D7A3A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-16") {
		addForeignKeyConstraint(baseColumnNames: "pgb_lic_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D279603D94BF7B6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-17") {
		addForeignKeyConstraint(baseColumnNames: "pgb_org_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D279603F223ABB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-18") {
		addForeignKeyConstraint(baseColumnNames: "pgb_property_definition_group_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D279603114305F7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pdg_id", referencedTableName: "property_definition_group", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-19") {
		addForeignKeyConstraint(baseColumnNames: "pgb_sub_fk", baseTableName: "property_definition_group_binding", constraintName: "FK3D27960359B822B2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-20") {
		addForeignKeyConstraint(baseColumnNames: "pde_property_definition_fk", baseTableName: "property_definition_group_item", constraintName: "FKA535F45593156098", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-21") {
		addForeignKeyConstraint(baseColumnNames: "pde_property_definition_group_fk", baseTableName: "property_definition_group_item", constraintName: "FKA535F455D48EB011", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pdg_id", referencedTableName: "property_definition_group", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1542893167281-22") {
	//	addForeignKeyConstraint(baseColumnNames: "fact_type_id", baseTableName: "stats_triple_cursor", constraintName: "FKB71D92B77430C71E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1542893167281-23") {
		addForeignKeyConstraint(baseColumnNames: "sub_form_fk", baseTableName: "subscription", constraintName: "FK1456591DA64FB8D2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-24") {
		addForeignKeyConstraint(baseColumnNames: "sub_resource_fk", baseTableName: "subscription", constraintName: "FK1456591D239DCC8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542893167281-25") {
		addForeignKeyConstraint(baseColumnNames: "sm_org_fk", baseTableName: "system_message", constraintName: "FK669579F798E2DECC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}
}
