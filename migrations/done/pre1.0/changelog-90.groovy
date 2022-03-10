databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1565864968228-1") {
		createTable(schemaName: "public", tableName: "price_item") {
			column(autoIncrement: "true", name: "pi_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "price_itemPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "pi_guid", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "pi_ie_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "pi_local_currency_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "pi_list_price", type: "numeric(19, 2)") {
				constraints(nullable: "false")
			}

			column(name: "local_currency_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "pi_local_price", type: "numeric(19, 2)") {
				constraints(nullable: "false")
			}

			column(name: "pi_price_date", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-2") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.identifier_namespace RENAME COLUMN idns_hide TO idns_is_hidden")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-3") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.identifier_namespace RENAME COLUMN idns_unique TO idns_is_unique")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-4") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_is_public", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-5") {
		addColumn(schemaName: "public", tableName: "person") {
			column(name: "prs_is_public", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-6") {
		addColumn(schemaName: "public", tableName: "property_definition_group") {
			column(name: "pdg_is_visible", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-7") {
		addColumn(schemaName: "public", tableName: "property_definition_group_binding") {
			column(name: "pbg_is_visible", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-8") {
		addColumn(schemaName: "public", tableName: "property_definition_group_binding") {
			column(name: "pbg_is_visible_for_cons_member", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-9") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.refdata_category RENAME COLUMN rdv_hard_data TO rdc_is_hard_data")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-10") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.refdata_value RENAME COLUMN rdv_hard_data TO rdv_is_hard_data")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-11") {
		addColumn(schemaName: "public", tableName: "subscription_package") {
			column(name: "sp_finish_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-12") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_is_slaved_tmp", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-13") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE license SET lic_is_slaved_tmp = ( " +
						"CASE WHEN lic_is_slaved = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END )"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-14") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_is_public_tmp", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-15") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE package SET pkg_is_public_tmp = ( " +
						"CASE WHEN pkg_is_public = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END )"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-16") {
		modifyDataType(columnName: "avail_from", newDataType: "timestamp", tableName: "stats_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-17") {
		addNotNullConstraint(columnDataType: "timestamp", columnName: "avail_from", tableName: "stats_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-18") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_public_tmp", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-19") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE subscription SET sub_is_public_tmp = ( " +
						"CASE WHEN sub_is_public = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END )"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-20") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_slaved_tmp", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-21") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE subscription SET sub_is_slaved_tmp = ( " +
						"CASE WHEN sub_is_slaved = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END )"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-22") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "public", constraintName: "fk9f084413d2aceb")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-23") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "public", constraintName: "fk9f08441e07d095a")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-24") {
		dropForeignKeyConstraint(baseTableName: "package", baseTableSchemaName: "public", constraintName: "fkcfe53446f8dfd21c")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-25") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "public", constraintName: "fkc4e39b55750b1c62")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-26") {
		dropForeignKeyConstraint(baseTableName: "property_definition_group", baseTableSchemaName: "public", constraintName: "fked224dbda14135f8")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-27") {
		dropForeignKeyConstraint(baseTableName: "property_definition_group_binding", baseTableSchemaName: "public", constraintName: "fk3d27960376a2fe3c")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-28") {
		dropForeignKeyConstraint(baseTableName: "property_definition_group_binding", baseTableSchemaName: "public", constraintName: "fk3d279603de1d7a3a")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-29") {
		dropForeignKeyConstraint(baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "fk1456591d28e1dd90")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-30") {
		dropForeignKeyConstraint(baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "fk1456591d2d814494")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-31") {
		dropForeignKeyConstraint(baseTableName: "system_admin_custom_property", baseTableSchemaName: "public", constraintName: "fkb7f703e3afaac3ea")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-32") {
		dropForeignKeyConstraint(baseTableName: "system_admin_custom_property", baseTableSchemaName: "public", constraintName: "fkb7f703e32992a286")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-33") {
		dropForeignKeyConstraint(baseTableName: "system_admin_custom_property", baseTableSchemaName: "public", constraintName: "fkb7f703e3638a6383")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-37") {
		createIndex(indexName: "auc_ref_idx", schemaName: "public", tableName: "audit_config") {
			column(name: "auc_reference_class")

			column(name: "auc_reference_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-38") {
		createIndex(indexName: "bc_owner_idx", schemaName: "public", tableName: "budget_code") {
			column(name: "bc_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-39") {
		createIndex(indexName: "ci_dates_idx", schemaName: "public", tableName: "cost_item") {
			column(name: "ci_end_date")

			column(name: "ci_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-40") {
		createIndex(indexName: "doc_lic_idx", schemaName: "public", tableName: "doc_context") {
			column(name: "dc_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-41") {
		createIndex(indexName: "doc_org_idx", schemaName: "public", tableName: "doc_context") {
			column(name: "dc_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-42") {
		createIndex(indexName: "doc_owner_idx", schemaName: "public", tableName: "doc_context") {
			column(name: "dc_doc_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-43") {
		createIndex(indexName: "doc_sub_idx", schemaName: "public", tableName: "doc_context") {
			column(name: "dc_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-44") {
		createIndex(indexName: "grt_owner_idx", schemaName: "public", tableName: "global_record_tracker") {
			column(name: "grt_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-45") {
		createIndex(indexName: "io_title_idx", schemaName: "public", tableName: "identifier_occurrence") {
			column(name: "io_ti_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-46") {
		createIndex(indexName: "ie_dates_idx", schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_end_date")

			column(name: "ie_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-47") {
		createIndex(indexName: "ie_sub_idx", schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_subscription_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-48") {
		createIndex(indexName: "ie_tipp_idx", schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_tipp_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-49") {
		createIndex(indexName: "lic_dates_idx", schemaName: "public", tableName: "license") {
			column(name: "lic_end_date")

			column(name: "lic_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-50") {
		createIndex(indexName: "l_dest_idx", schemaName: "public", tableName: "links") {
			column(name: "l_destination_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-51") {
		createIndex(indexName: "l_source_idx", schemaName: "public", tableName: "links") {
			column(name: "l_source_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-52") {
		createIndex(indexName: "ord_owner_idx", schemaName: "public", tableName: "ordering") {
			column(name: "ord_owner")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-53") {
		createIndex(indexName: "pkg_dates_idx", schemaName: "public", tableName: "package") {
			column(name: "pkg_end_date")

			column(name: "pkg_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-54") {
		createIndex(indexName: "pending_change_costitem_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_ci_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-55") {
		createIndex(indexName: "pending_change_lic_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-56") {
		createIndex(indexName: "pending_change_pkg_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_pkg_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-57") {
		createIndex(indexName: "pending_change_sub_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-58") {
		createIndex(indexName: "plat_org_idx", schemaName: "public", tableName: "platform") {
			column(name: "plat_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-59") {
		createIndex(indexName: "pi_guid_uniq_1565864963840", schemaName: "public", tableName: "price_item", unique: "true") {
			column(name: "pi_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-60") {
		createIndex(indexName: "pd_tenant_idx", schemaName: "public", tableName: "property_definition") {
			column(name: "pd_tenant_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-61") {
		createIndex(indexName: "pdg_tenant_idx", schemaName: "public", tableName: "property_definition_group") {
			column(name: "pdg_tenant_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-62") {
		createIndex(indexName: "sub_dates_idx", schemaName: "public", tableName: "subscription") {
			column(name: "sub_end_date")

			column(name: "sub_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-63") {
		createIndex(indexName: "sub_owner_idx", schemaName: "public", tableName: "subscription") {
			column(name: "sub_owner_license_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-64") {
		createIndex(indexName: "sub_type_idx", schemaName: "public", tableName: "subscription") {
			column(name: "sub_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-65") {
		createIndex(indexName: "sp_sub_pkg_idx", schemaName: "public", tableName: "subscription_package") {
			column(name: "sp_pkg_fk")

			column(name: "sp_sub_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-66") {
		createIndex(indexName: "tipp_dates_idx", schemaName: "public", tableName: "title_instance_package_platform") {
			column(name: "tipp_end_date")

			column(name: "tipp_start_date")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-67") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE property_definition_group_binding SET pbg_is_visible = ( " +
								"CASE WHEN pbg_visible_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
								"THEN TRUE ELSE FALSE END)"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-68") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE property_definition_group_binding SET pbg_is_visible_for_cons_member = ( " +
								"CASE WHEN pbg_is_viewable_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
								"THEN TRUE ELSE FALSE END)"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-69") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE license SET lic_is_public = ( " +
						"CASE WHEN lic_is_public_rdv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END )"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-70") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE person SET prs_is_public = ( " +
								"CASE WHEN prs_is_public_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
								"THEN TRUE ELSE FALSE END)"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-71") {
		grailsChange {
			change {
				sql.execute(
						"UPDATE property_definition_group SET pdg_is_visible = ( " +
						"CASE WHEN pdg_visible_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
						"THEN TRUE ELSE FALSE END)"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-72") {
		dropColumn(columnName: "lic_is_slaved", tableName: "license")
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-73") {
		dropColumn(columnName: "sub_is_slaved", tableName: "subscription")
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-74") {
		dropColumn(columnName: "sub_is_public", tableName: "subscription")
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-75") {
		dropColumn(columnName: "pkg_is_public", tableName: "package")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-76") {
		dropTable(tableName: "jasper_report_file")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-77") {
		dropTable(tableName: "system_admin")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-78") {
		dropTable(tableName: "system_admin_custom_property")
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-79") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.license RENAME COLUMN lic_is_slaved_tmp TO lic_is_slaved")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-80") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.subscription RENAME COLUMN sub_is_slaved_tmp TO sub_is_slaved")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-81") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.subscription RENAME COLUMN sub_is_public_tmp TO sub_is_public")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1565864968228-82") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.package RENAME COLUMN pkg_is_public_tmp TO pkg_is_public")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-83") {
		dropColumn(columnName: "lic_is_public_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-84") {
		dropColumn(columnName: "prs_is_public_rv_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-85") {
		dropColumn(columnName: "pdg_visible_rv_fk", tableName: "property_definition_group")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-86") {
		dropColumn(columnName: "pbg_is_viewable_rv_fk", tableName: "property_definition_group_binding")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-87") {
		dropColumn(columnName: "pbg_visible_rv_fk", tableName: "property_definition_group_binding")
	}


	changeSet(author: "kloberd (generated)", id: "1565864968228-34") {
		addForeignKeyConstraint(baseColumnNames: "local_currency_id", baseTableName: "price_item", baseTableSchemaName: "public", constraintName: "FKA8C4E849BF9846E6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-35") {
		addForeignKeyConstraint(baseColumnNames: "pi_ie_fk", baseTableName: "price_item", baseTableSchemaName: "public", constraintName: "FKA8C4E849FC049213", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-36") {
		addForeignKeyConstraint(baseColumnNames: "pi_local_currency_rv_fk", baseTableName: "price_item", baseTableSchemaName: "public", constraintName: "FKA8C4E8494451827D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-88") {
		addNotNullConstraint(columnDataType: "bool", columnName: "lic_is_public", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-89") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sub_is_public", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-90") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sub_is_slaved", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-91") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pkg_is_public", tableName: "package")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-92") {
		addNotNullConstraint(columnDataType: "bool", columnName: "prs_is_public", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-93") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pdg_is_visible", tableName: "property_definition_group")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-94") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pbg_is_visible", tableName: "property_definition_group_binding")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-95") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pbg_is_visible_for_cons_member", tableName: "property_definition_group_binding")
	}

	changeSet(author: "kloberd (generated)", id: "1565864968228-96") {
		addNotNullConstraint(columnDataType: "bool", columnName: "lic_is_slaved", tableName: "license")
	}

//	changeSet(author: "kloberd (generated)", id: "1565864968228-88") {
//		addNotNullConstraint(columnDataType: "bool", columnName: "idns_is_hidden", tableName: "identifier_namespace")
//	}
}
