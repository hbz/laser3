databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1548252520602-1") {
		createTable(schemaName: "public", tableName: "cost_item_element_configuration") {
			column(autoIncrement: "true", name: "ciec_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cost_item_elePK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ciec_cie_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "created_by_id", type: "int8")

			column(name: "date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ciec_cc_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ciec_org_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ciec_guid", type: "varchar(255)")

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated_by_id", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-2") {
		createTable(schemaName: "public", tableName: "links") {
			column(autoIncrement: "true", name: "l_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "linksPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "created_by_id", type: "int8")

			column(name: "l_destination_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "last_updated_by_id", type: "int8")

			column(name: "l_link_type_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "l_object", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "l_owner_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "l_source_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-3") {
		createTable(schemaName: "public", tableName: "system_event") {
			column(autoIncrement: "true", name: "se_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_eventPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "se_category", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "se_created", type: "timestamp")

			column(name: "se_payload", type: "text")

			column(name: "se_relevance", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "se_token", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-4") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_element_configuration_rv_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-5") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_link_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-6") {
		addColumn(schemaName: "public", tableName: "global_record_info") {
			column(name: "gri_uuid", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-7") {
		addColumn(schemaName: "public", tableName: "global_record_tracker") {
			column(name: "grt_uuid", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-8") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_config_preset_rv_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-9") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_used_for_logic", type: "bool")
		}
	}

    changeSet(author: "kloberd (manually)", id: "1548252520602-10") {
        grailsChange {
            change {
                sql.execute("UPDATE public.license SET lic_ref = 'Name fehlt', lic_sortable_ref = 'name fehlt' WHERE lic_ref IS null")
            }
            confirm 'Updated Table Data'
        }
    }

	changeSet(author: "kloberd (generated)", id: "1548252520602-23") {
		createIndex(indexName: "ciec_guid_uniq_1548252516917", schemaName: "public", tableName: "cost_item_element_configuration", unique: "true") {
			column(name: "ciec_guid")
		}
	}

    changeSet(author: "kloberd (generated)", id: "1548252520602-24") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "lic_ref", tableName: "license")
    }

	changeSet(author: "kloberd (manually)", id: "1548252520602-25") {
		grailsChange {
			change {
				sql.execute("UPDATE public.property_definition SET pd_used_for_logic = false WHERE pd_used_for_logic IS null")
			}
			confirm 'Updated Table Data'
		}
	}

	/*
	changeSet(author: "kloberd (generated)", id: "1548252520602-24") {
		dropSequence(schemaName: "public", sequenceName: "access_point_data_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-25") {
		dropSequence(schemaName: "public", sequenceName: "address_adr_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-26") {
		dropSequence(schemaName: "public", sequenceName: "alert_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-27") {
		dropSequence(schemaName: "public", sequenceName: "annotation_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-28") {
		dropSequence(schemaName: "public", sequenceName: "api_source_as_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-29") {
		dropSequence(schemaName: "public", sequenceName: "audit_config_auc_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-30") {
		dropSequence(schemaName: "public", sequenceName: "audit_log_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-31") {
		dropSequence(schemaName: "public", sequenceName: "budget_code_bc_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-32") {
		dropSequence(schemaName: "public", sequenceName: "change_notification_queue_item_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-33") {
		dropSequence(schemaName: "public", sequenceName: "cluster_cl_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-34") {
		dropSequence(schemaName: "public", sequenceName: "combo_combo_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-35") {
		dropSequence(schemaName: "public", sequenceName: "comment_comm_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-36") {
		dropSequence(schemaName: "public", sequenceName: "contact_ct_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-37") {
		dropSequence(schemaName: "public", sequenceName: "content_item_ci_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-38") {
		dropSequence(schemaName: "public", sequenceName: "core_assertion_ca_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-39") {
		dropSequence(schemaName: "public", sequenceName: "cost_item_ci_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-40") {
		dropSequence(schemaName: "public", sequenceName: "cost_item_group_cig_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-41") {
		dropSequence(schemaName: "public", sequenceName: "creator_cre_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-42") {
		dropSequence(schemaName: "public", sequenceName: "creator_title_ct_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-43") {
		dropSequence(schemaName: "public", sequenceName: "dashboard_due_date_das_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-44") {
		dropSequence(schemaName: "public", sequenceName: "dataload_file_instance_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-45") {
		dropSequence(schemaName: "public", sequenceName: "dataload_file_type_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-46") {
		dropSequence(schemaName: "public", sequenceName: "doc_context_dc_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-47") {
		dropSequence(schemaName: "public", sequenceName: "doc_doc_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-48") {
		dropSequence(schemaName: "public", sequenceName: "elasticsearch_source_ess_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-49") {
		dropSequence(schemaName: "public", sequenceName: "event_log_el_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-50") {
		dropSequence(schemaName: "public", sequenceName: "fact_fact_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-51") {
		dropSequence(schemaName: "public", sequenceName: "folder_item_fi_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-52") {
		dropSequence(schemaName: "public", sequenceName: "ftcontrol_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-53") {
		dropSequence(schemaName: "public", sequenceName: "global_record_info_gri_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-54") {
		dropSequence(schemaName: "public", sequenceName: "global_record_source_grs_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-55") {
		dropSequence(schemaName: "public", sequenceName: "global_record_tracker_grt_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-56") {
		dropSequence(schemaName: "public", sequenceName: "i10n_translation_i10n_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-57") {
		dropSequence(schemaName: "public", sequenceName: "identifier_group_ig_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-58") {
		dropSequence(schemaName: "public", sequenceName: "identifier_id_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-59") {
		dropSequence(schemaName: "public", sequenceName: "identifier_namespace_idns_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-60") {
		dropSequence(schemaName: "public", sequenceName: "identifier_occurrence_io_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-61") {
		dropSequence(schemaName: "public", sequenceName: "invoice_inv_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-62") {
		dropSequence(schemaName: "public", sequenceName: "issue_entitlement_ie_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-63") {
		dropSequence(schemaName: "public", sequenceName: "jasper_report_file_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-64") {
		dropSequence(schemaName: "public", sequenceName: "license_custom_property_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-65") {
		dropSequence(schemaName: "public", sequenceName: "license_lic_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-66") {
		dropSequence(schemaName: "public", sequenceName: "license_private_property_lpp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-67") {
		dropSequence(schemaName: "public", sequenceName: "numbers_num_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-68") {
		dropSequence(schemaName: "public", sequenceName: "onixpl_license_opl_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-69") {
		dropSequence(schemaName: "public", sequenceName: "ordering_ord_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-70") {
		dropSequence(schemaName: "public", sequenceName: "org_access_point_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-71") {
		dropSequence(schemaName: "public", sequenceName: "org_access_point_link_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-72") {
		dropSequence(schemaName: "public", sequenceName: "org_custom_property_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-73") {
		dropSequence(schemaName: "public", sequenceName: "org_org_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-74") {
		dropSequence(schemaName: "public", sequenceName: "org_perm_share_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-75") {
		dropSequence(schemaName: "public", sequenceName: "org_private_property_opp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-76") {
		dropSequence(schemaName: "public", sequenceName: "org_role_or_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-77") {
		dropSequence(schemaName: "public", sequenceName: "org_title_stats_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-78") {
		dropSequence(schemaName: "public", sequenceName: "package_pkg_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-79") {
		dropSequence(schemaName: "public", sequenceName: "pending_change_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-80") {
		dropSequence(schemaName: "public", sequenceName: "perm_grant_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-81") {
		dropSequence(schemaName: "public", sequenceName: "perm_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-82") {
		dropSequence(schemaName: "public", sequenceName: "person_private_property_ppp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-83") {
		dropSequence(schemaName: "public", sequenceName: "person_prs_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-84") {
		dropSequence(schemaName: "public", sequenceName: "person_role_pr_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-85") {
		dropSequence(schemaName: "public", sequenceName: "platform_access_method_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-86") {
		dropSequence(schemaName: "public", sequenceName: "platform_plat_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-87") {
		dropSequence(schemaName: "public", sequenceName: "platformtipp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-88") {
		dropSequence(schemaName: "public", sequenceName: "property_definition_group_binding_pgb_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-89") {
		dropSequence(schemaName: "public", sequenceName: "property_definition_group_item_pde_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-90") {
		dropSequence(schemaName: "public", sequenceName: "property_definition_group_pdg_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-91") {
		dropSequence(schemaName: "public", sequenceName: "property_definition_pd_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-92") {
		dropSequence(schemaName: "public", sequenceName: "refdata_category_rdc_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-93") {
		dropSequence(schemaName: "public", sequenceName: "refdata_value_rdv_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-94") {
		dropSequence(schemaName: "public", sequenceName: "reminder_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-95") {
		dropSequence(schemaName: "public", sequenceName: "role_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-96") {
		dropSequence(schemaName: "public", sequenceName: "setting_set_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-97") {
		dropSequence(schemaName: "public", sequenceName: "site_page_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-98") {
		dropSequence(schemaName: "public", sequenceName: "stats_triple_cursor_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-99") {
		dropSequence(schemaName: "public", sequenceName: "subscription_custom_property_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-100") {
		dropSequence(schemaName: "public", sequenceName: "subscription_package_sp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-101") {
		dropSequence(schemaName: "public", sequenceName: "subscription_private_property_spp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-102") {
		dropSequence(schemaName: "public", sequenceName: "subscription_sub_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-103") {
		dropSequence(schemaName: "public", sequenceName: "system_admin_custom_property_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-104") {
		dropSequence(schemaName: "public", sequenceName: "system_admin_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-105") {
		dropSequence(schemaName: "public", sequenceName: "system_message_sm_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-106") {
		dropSequence(schemaName: "public", sequenceName: "system_object_sys_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-107") {
		dropSequence(schemaName: "public", sequenceName: "system_profiler_sp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-108") {
		dropSequence(schemaName: "public", sequenceName: "system_ticket_sti_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-109") {
		dropSequence(schemaName: "public", sequenceName: "task_tsk_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-110") {
		dropSequence(schemaName: "public", sequenceName: "title_history_event_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-111") {
		dropSequence(schemaName: "public", sequenceName: "title_history_event_participant_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-112") {
		dropSequence(schemaName: "public", sequenceName: "title_instance_package_platform_tipp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-113") {
		dropSequence(schemaName: "public", sequenceName: "title_instance_ti_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-114") {
		dropSequence(schemaName: "public", sequenceName: "title_institution_provider_tiinp_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-115") {
		dropSequence(schemaName: "public", sequenceName: "user_folder_uf_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-116") {
		dropSequence(schemaName: "public", sequenceName: "user_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-117") {
		dropSequence(schemaName: "public", sequenceName: "user_org_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-118") {
		dropSequence(schemaName: "public", sequenceName: "user_settings_us_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-119") {
		dropSequence(schemaName: "public", sequenceName: "user_transforms_id_seq")
	}
	*/

	changeSet(author: "kloberd (generated)", id: "1548252520602-11") {
		addForeignKeyConstraint(baseColumnNames: "ci_element_configuration_rv_fk", baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "FKEFE45C45F12A13AC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-12") {
		addForeignKeyConstraint(baseColumnNames: "ciec_cc_rv_fk", baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "FK2D8A6879E1E7980D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-13") {
		addForeignKeyConstraint(baseColumnNames: "ciec_cie_rv_fk", baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "FK2D8A6879E22E37D6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-14") {
		addForeignKeyConstraint(baseColumnNames: "ciec_org_fk", baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "FK2D8A687934E99262", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-15") {
		addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "FK2D8A6879B47E66A0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-16") {
		addForeignKeyConstraint(baseColumnNames: "last_updated_by_id", baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "FK2D8A68793096044A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-17") {
		addForeignKeyConstraint(baseColumnNames: "dc_link_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8BFFA0C6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "l_id", referencedTableName: "links", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-18") {
		addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB9B47E66A0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-19") {
		addForeignKeyConstraint(baseColumnNames: "l_link_type_rv_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB9F4F3049E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-20") {
		addForeignKeyConstraint(baseColumnNames: "l_owner_fk", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB9D576C98B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-21") {
		addForeignKeyConstraint(baseColumnNames: "last_updated_by_id", baseTableName: "links", baseTableSchemaName: "public", constraintName: "FK6234FB93096044A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1548252520602-22") {
		addForeignKeyConstraint(baseColumnNames: "org_config_preset_rv_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE4E06F28F3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
