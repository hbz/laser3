databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1542877375482-1") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_hard_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-2") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "fact_type_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-3") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "jerror", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-4") {
		addColumn(tableName: "stats_triple_cursor") {
			column(name: "num_facts", type: "integer") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-5") {
		addColumn(tableName: "subscription") {
			column(name: "sub_form_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-6") {
		addColumn(tableName: "subscription") {
			column(name: "sub_resource_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-11") {
		createIndex(indexName: "adr_org_idx", tableName: "address") {
			column(name: "adr_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-12") {
		createIndex(indexName: "adr_prs_idx", tableName: "address") {
			column(name: "adr_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-13") {
		createIndex(indexName: "ct_org_idx", tableName: "contact") {
			column(name: "ct_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-14") {
		createIndex(indexName: "ct_prs_idx", tableName: "contact") {
			column(name: "ct_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-15") {
		createIndex(indexName: "ci_owner_idx", tableName: "cost_item") {
			column(name: "ci_owner")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-16") {
		createIndex(indexName: "ci_sub_idx", tableName: "cost_item") {
			column(name: "ci_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-17") {
		createIndex(indexName: "i10n_ref_idx", tableName: "i10n_translation") {
			column(name: "i10n_reference_class")

			column(name: "i10n_reference_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-18") {
		createIndex(indexName: "lic_parent_idx", tableName: "license") {
			column(name: "lic_parent_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-19") {
		createIndex(indexName: "owner_idx", tableName: "license_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-20") {
		createIndex(indexName: "lpp_owner_idx", tableName: "license_private_property") {
			column(name: "lpp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-21") {
		createIndex(indexName: "owner_idx", tableName: "org_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-22") {
		createIndex(indexName: "opp_owner_idx", tableName: "org_private_property") {
			column(name: "opp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-23") {
		createIndex(indexName: "or_lic_idx", tableName: "org_role") {
			column(name: "or_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-24") {
		createIndex(indexName: "or_sub_idx", tableName: "org_role") {
			column(name: "or_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-25") {
		createIndex(indexName: "ppp_owner_idx", tableName: "person_private_property") {
			column(name: "ppp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-26") {
		createIndex(indexName: "pr_prs_org_idx", tableName: "person_role") {
			column(name: "pr_org_fk")

			column(name: "pr_prs_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-27") {
		createIndex(indexName: "unique_pgb_lic_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-28") {
		createIndex(indexName: "unique_pgb_org_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-29") {
		createIndex(indexName: "unique_pgb_sub_fk", tableName: "property_definition_group_binding", unique: "true") {
			column(name: "pgb_property_definition_group_fk")

			column(name: "pgb_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-30") {
		createIndex(indexName: "unique_pde_property_definition_fk", tableName: "property_definition_group_item", unique: "true") {
			column(name: "pde_property_definition_group_fk")

			column(name: "pde_property_definition_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-31") {
		createIndex(indexName: "FKB71D92B77430C71E", tableName: "stats_triple_cursor") {
			column(name: "fact_type_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-32") {
		createIndex(indexName: "FK1456591D239DCC8", tableName: "subscription") {
			column(name: "sub_resource_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-33") {
		createIndex(indexName: "FK1456591DA64FB8D2", tableName: "subscription") {
			column(name: "sub_form_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-34") {
		createIndex(indexName: "sub_parent_idx", tableName: "subscription") {
			column(name: "sub_parent_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-35") {
		createIndex(indexName: "owner_idx", tableName: "subscription_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-36") {
		createIndex(indexName: "spp_owner_idx", tableName: "subscription_private_property") {
			column(name: "spp_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-37") {
		createIndex(indexName: "owner_idx", tableName: "system_admin_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-38") {
		createIndex(indexName: "us_user_idx", tableName: "user_settings") {
			column(name: "us_user_fk")
		}
	}

	//changeSet(author: "kloberd (generated)", id: "1542877375482-7") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1542877375482-8") {
		addForeignKeyConstraint(baseColumnNames: "fact_type_id", baseTableName: "stats_triple_cursor", constraintName: "FKB71D92B77430C71E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-9") {
		addForeignKeyConstraint(baseColumnNames: "sub_form_fk", baseTableName: "subscription", constraintName: "FK1456591DA64FB8D2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1542877375482-10") {
		addForeignKeyConstraint(baseColumnNames: "sub_resource_fk", baseTableName: "subscription", constraintName: "FK1456591D239DCC8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}
}
