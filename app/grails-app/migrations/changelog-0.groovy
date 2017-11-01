databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1508147394114-1") {
		createTable(tableName: "address") {
			column(autoIncrement: "true", name: "adr_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "adr_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "adr_city", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "adr_country", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "adr_org_fk", type: "BIGINT")

			column(name: "adr_pob", type: "VARCHAR(255)")

			column(name: "adr_prs_fk", type: "BIGINT")

			column(name: "adr_state", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "adr_street_1", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "adr_street_2", type: "VARCHAR(255)")

			column(name: "adr_type_rv_fk", type: "BIGINT")

			column(name: "adr_zipcode", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-2") {
		createTable(tableName: "alert") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "al_create_time", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "al_user_fk", type: "BIGINT")

			column(name: "al_org_fk", type: "BIGINT")

			column(name: "al_sharing_level", type: "INT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-3") {
		createTable(tableName: "annotation") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "component_type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "property_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "LONGTEXT")

			column(name: "view_type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-4") {
		createTable(tableName: "audit_log") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "actor", type: "VARCHAR(255)")

			column(name: "class_name", type: "VARCHAR(255)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "event_name", type: "VARCHAR(255)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "new_value", type: "VARCHAR(255)")

			column(name: "old_value", type: "VARCHAR(255)")

			column(name: "persisted_object_id", type: "VARCHAR(255)")

			column(name: "persisted_object_version", type: "BIGINT")

			column(name: "property_name", type: "VARCHAR(255)")

			column(name: "uri", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-5") {
		createTable(tableName: "change_notification_queue_item") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "cnqi_change_document", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "cnqi_oid", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "cnqi_ts", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-6") {
		createTable(tableName: "cluster") {
			column(autoIncrement: "true", name: "cl_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "cl_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "cl_definition", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "cl_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "cl_owner_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "cl_type_rv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-7") {
		createTable(tableName: "combo") {
			column(autoIncrement: "true", name: "combo_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "combo_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "combo_from_org_fk", type: "BIGINT")

			column(name: "combo_status_rv_fk", type: "BIGINT")

			column(name: "combo_to_org_fk", type: "BIGINT")

			column(name: "combo_type_rv_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-8") {
		createTable(tableName: "contact") {
			column(autoIncrement: "true", name: "ct_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ct_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ct_org_fk", type: "BIGINT")

			column(name: "ct_prs_fk", type: "BIGINT")

			column(name: "ct_type_rv_fk", type: "BIGINT")

			column(name: "ct_content", type: "VARCHAR(255)")

			column(name: "ct_content_type_rv_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-9") {
		createTable(tableName: "content_item") {
			column(autoIncrement: "true", name: "ci_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ci_content", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "ci_key", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "ci_locale", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-10") {
		createTable(tableName: "core_assertion") {
			column(autoIncrement: "true", name: "ca_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ca_ver", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ca_end_date", type: "DATETIME")

			column(name: "ca_start_date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "ca_owner", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-11") {
		createTable(tableName: "cost_item") {
			column(autoIncrement: "true", name: "ci_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ci_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ci_billing_currency_rv_fk", type: "BIGINT")

			column(name: "ci_cost_description", type: "LONGTEXT")

			column(name: "ci_cost_in_billing_currency", type: "DOUBLE")

			column(name: "ci_cost_in_local_currency", type: "DOUBLE")

			column(name: "ci_cat_rv_fk", type: "BIGINT")

			column(name: "ci_element_rv_fk", type: "BIGINT")

			column(name: "ci_status_rv_fk", type: "BIGINT")

			column(name: "created_by_id", type: "BIGINT")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "ci_date_paid", type: "DATETIME")

			column(name: "ci_end_date", type: "DATETIME")

			column(name: "ci_include_in_subscr", type: "BIT")

			column(name: "ci_inv_fk", type: "BIGINT")

			column(name: "ci_e_fk", type: "BIGINT")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "last_updated_by_id", type: "BIGINT")

			column(name: "ci_ord_fk", type: "BIGINT")

			column(name: "ci_owner", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ci_reference", type: "VARCHAR(255)")

			column(name: "ci_start_date", type: "DATETIME")

			column(name: "ci_sub_fk", type: "BIGINT")

			column(name: "ci_subPkg_fk", type: "BIGINT")

			column(name: "ci_tax_code", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-12") {
		createTable(tableName: "cost_item_group") {
			column(autoIncrement: "true", name: "cig_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "cig_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "cig_budgetcode_fk", type: "BIGINT")

			column(name: "cig_costItem_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-13") {
		createTable(tableName: "dataload_file_instance") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "upload_timestamp", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-14") {
		createTable(tableName: "dataload_file_type") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fl_ft_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-15") {
		createTable(tableName: "doc") {
			column(autoIncrement: "true", name: "doc_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "doc_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "doc_alert_fk", type: "BIGINT")

			column(name: "doc_blob_content", type: "LONGBLOB")

			column(name: "doc_content", type: "LONGTEXT")

			column(name: "doc_content_type", type: "INT")

			column(name: "doc_creator", type: "VARCHAR(255)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "doc_filename", type: "VARCHAR(255)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "migrated", type: "VARCHAR(1)")

			column(name: "doc_mimeType", type: "VARCHAR(255)")

			column(name: "doc_status_rv_fk", type: "BIGINT")

			column(name: "doc_title", type: "VARCHAR(255)")

			column(name: "doc_type_rv_fk", type: "BIGINT")

			column(name: "doc_user_fk", type: "BIGINT")

			column(name: "doc_docstore_uuid", type: "VARCHAR(255)")

			column(name: "doc_migrated", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-16") {
		createTable(tableName: "doc_context") {
			column(autoIncrement: "true", name: "dc_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "dc_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dc_alert_fk", type: "BIGINT")

			column(name: "dc_rv_doctype_fk", type: "BIGINT")

			column(name: "domain", type: "VARCHAR(255)")

			column(name: "dc_is_global", type: "BIT")

			column(name: "dc_lic_fk", type: "BIGINT")

			column(name: "dc_doc_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dc_pkg_fk", type: "BIGINT")

			column(name: "dc_status_fk", type: "BIGINT")

			column(name: "dc_sub_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-17") {
		createTable(tableName: "event_log") {
			column(autoIncrement: "true", name: "el_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "el_event", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "el_msg", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "el_tstp", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-18") {
		createTable(tableName: "fact") {
			column(autoIncrement: "true", name: "fact_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "fact_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fact_from", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "fact_to", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "fact_type_rdv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fact_uid", type: "VARCHAR(255)")

			column(name: "fact_value", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "inst_id", type: "BIGINT")

			column(name: "juspio_id", type: "BIGINT")

			column(name: "related_title_id", type: "BIGINT")

			column(name: "reporting_month", type: "BIGINT")

			column(name: "reporting_year", type: "BIGINT")

			column(name: "supplier_id", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-19") {
		createTable(tableName: "folder_item") {
			column(autoIncrement: "true", name: "fi_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "fi_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "folder_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fi_ref_oid", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "items_idx", type: "INT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-20") {
		createTable(tableName: "ftcontrol") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "activity", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "domain_class_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "last_timestamp", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-21") {
		createTable(tableName: "global_record_info") {
			column(autoIncrement: "true", name: "gri_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "gri_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "gri_desc", type: "VARCHAR(255)")

			column(name: "gri_identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "gri_kbplus_compliant", type: "BIGINT")

			column(name: "gri_name", type: "VARCHAR(255)")

			column(name: "gri_record", type: "LONGBLOB") {
				constraints(nullable: "false")
			}

			column(name: "gri_rectype", type: "BIGINT")

			column(name: "gri_source_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "gri_timestamp", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-22") {
		createTable(tableName: "global_record_source") {
			column(autoIncrement: "true", name: "grs_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "grs_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "grs_active", type: "BIT")

			column(name: "grs_creds", type: "VARCHAR(255)")

			column(name: "grs_full_prefix", type: "VARCHAR(255)")

			column(name: "grs_have_up_to", type: "DATETIME")

			column(name: "grs_identifier", type: "VARCHAR(255)")

			column(name: "grs_list_prefix", type: "VARCHAR(255)")

			column(name: "grs_name", type: "VARCHAR(255)")

			column(name: "grs_principal", type: "VARCHAR(255)")

			column(name: "grs_rectype", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "grs_type", type: "VARCHAR(255)")

			column(name: "grs_uri", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-23") {
		createTable(tableName: "global_record_tracker") {
			column(autoIncrement: "true", name: "grt_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "grt_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "grt_auto_pkg_update", type: "BIT")

			column(name: "grt_auto_tipp_add", type: "BIT")

			column(name: "grt_auto_tipp_del", type: "BIT")

			column(name: "grt_auto_tipp_update", type: "BIT")

			column(name: "grt_identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "grt_local_oid", type: "VARCHAR(255)")

			column(name: "grt_name", type: "VARCHAR(255)")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "grt_owner_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-24") {
		createTable(tableName: "i10n_translation") {
			column(autoIncrement: "true", name: "i10n_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "i10n_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "i10n_reference_class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "i10n_reference_field", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "i10n_reference_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "i10n_value_de", type: "VARCHAR(255)")

			column(name: "i10n_value_en", type: "VARCHAR(255)")

			column(name: "i10n_value_fr", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-25") {
		createTable(tableName: "identifier") {
			column(autoIncrement: "true", name: "id_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "id_ns_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "id_value", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "id_ig_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-26") {
		createTable(tableName: "identifier_group") {
			column(autoIncrement: "true", name: "ig_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-27") {
		createTable(tableName: "identifier_namespace") {
			column(autoIncrement: "true", name: "idns_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "idns_hide", type: "BIT")

			column(name: "idns_ns", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "idns_type_fl", type: "BIGINT")

			column(name: "idns_family", type: "VARCHAR(255)")

			column(name: "idns_nonUnique", type: "BIT")

			column(name: "idns_val_regex", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-28") {
		createTable(tableName: "identifier_occurrence") {
			column(autoIncrement: "true", name: "io_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "io_canonical_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "io_org_fk", type: "BIGINT")

			column(name: "io_pkg_fk", type: "BIGINT")

			column(name: "io_ti_fk", type: "BIGINT")

			column(name: "io_tipp_fk", type: "BIGINT")

			column(name: "io_sub_fk", type: "BIGINT")

			column(name: "io_lic_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-29") {
		createTable(tableName: "identifier_relation") {
			column(autoIncrement: "true", name: "ir_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ir_from_id_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ir_rel_rdv_id_fk", type: "BIGINT")

			column(name: "ir_to_id_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-30") {
		createTable(tableName: "invoice") {
			column(autoIncrement: "true", name: "inv_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "inv_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "inv_date_of_invoice", type: "DATETIME")

			column(name: "inv_date_of_payment", type: "DATETIME")

			column(name: "inv_date_passed_to_finance", type: "DATETIME")

			column(name: "inv_end_date", type: "DATETIME")

			column(name: "inv_number", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "inv_owner", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "inv_start_date", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-31") {
		createTable(tableName: "issue_entitlement") {
			column(autoIncrement: "true", name: "ie_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ie_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ie_access_end_date", type: "DATETIME")

			column(name: "ie_access_start_date", type: "DATETIME")

			column(name: "core_status_id", type: "BIGINT")

			column(name: "core_status_end", type: "DATETIME")

			column(name: "core_status_start", type: "DATETIME")

			column(name: "ie_coverage_depth", type: "VARCHAR(255)")

			column(name: "ie_coverage_note", type: "LONGTEXT")

			column(name: "ie_embargo", type: "VARCHAR(255)")

			column(name: "ie_end_date", type: "DATETIME")

			column(name: "ie_end_issue", type: "VARCHAR(255)")

			column(name: "ie_end_volume", type: "VARCHAR(255)")

			column(name: "ie_reason", type: "VARCHAR(255)")

			column(name: "ie_start_date", type: "DATETIME")

			column(name: "ie_start_issue", type: "VARCHAR(255)")

			column(name: "ie_start_volume", type: "VARCHAR(255)")

			column(name: "ie_status_rv_fk", type: "BIGINT")

			column(name: "ie_subscription_fk", type: "BIGINT")

			column(name: "ie_tipp_fk", type: "BIGINT")

			column(name: "ie_medium_rv_fk", type: "BIGINT")

			column(name: "ie_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-32") {
		createTable(tableName: "jasper_report_file") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "report_file", type: "MEDIUMBLOB") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-33") {
		createTable(tableName: "jusp_triple_cursor") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "have_up_to", type: "VARCHAR(32)") {
				constraints(nullable: "false")
			}

			column(name: "jusp_login_id", type: "VARCHAR(32)") {
				constraints(nullable: "false")
			}

			column(name: "jusp_supplier_id", type: "VARCHAR(32)") {
				constraints(nullable: "false")
			}

			column(name: "jusp_title_id", type: "VARCHAR(32)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-34") {
		createTable(tableName: "kb_comment") {
			column(autoIncrement: "true", name: "comm_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "comm_alert_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "comm_by_user_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "comm_text", type: "LONGTEXT") {
				constraints(nullable: "false")
			}

			column(name: "comm_date", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-35") {
		createTable(tableName: "kbplus_fact") {
			column(autoIncrement: "true", name: "fact_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "fact_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fact_from", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "fact_to", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "fact_type_rdv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "fact_uid", type: "VARCHAR(255)")

			column(name: "fact_value", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "inst_id", type: "BIGINT")

			column(name: "juspio_id", type: "BIGINT")

			column(name: "related_title_id", type: "BIGINT")

			column(name: "reporting_month", type: "BIGINT")

			column(name: "reporting_year", type: "BIGINT")

			column(name: "supplier_id", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-36") {
		createTable(tableName: "kbplus_ord") {
			column(autoIncrement: "true", name: "ord_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ord_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ord_number", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "ord_owner", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-37") {
		createTable(tableName: "license") {
			column(autoIncrement: "true", name: "lic_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "lic_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "lic_end_date", type: "DATETIME")

			column(name: "imp_id", type: "VARCHAR(255)")

			column(name: "lic_is_public_rdv_fk", type: "BIGINT")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "lic_lastmod", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "lic_category_rdv_fk", type: "BIGINT")

			column(name: "lic_license_status_str", type: "VARCHAR(255)")

			column(name: "lic_license_url", type: "VARCHAR(255)")

			column(name: "lic_licensee_ref", type: "VARCHAR(255)")

			column(name: "lic_licensor_ref", type: "VARCHAR(255)")

			column(name: "lic_notice_period", type: "VARCHAR(255)")

			column(name: "lic_opl_fk", type: "BIGINT")

			column(name: "lic_ref", type: "VARCHAR(255)")

			column(name: "lic_sortable_ref", type: "VARCHAR(255)")

			column(name: "lic_start_date", type: "DATETIME")

			column(name: "lic_status_rv_fk", type: "BIGINT")

			column(name: "lic_type_rv_fk", type: "BIGINT")

			column(name: "lic_alumni_access_rdv_fk", type: "BIGINT")

			column(name: "lic_concurrent_user_count", type: "BIGINT")

			column(name: "lic_concurrent_users_rdv_fk", type: "BIGINT")

			column(name: "lic_coursepack_rdv_fk", type: "BIGINT")

			column(name: "lic_enterprise_rdv_fk", type: "BIGINT")

			column(name: "lic_ill_rdv_fk", type: "BIGINT")

			column(name: "lic_license_type_str", type: "VARCHAR(255)")

			column(name: "lic_multisite_access_rdv_fk", type: "BIGINT")

			column(name: "lic_partners_access_rdv_fk", type: "BIGINT")

			column(name: "lic_pca_rdv_fk", type: "BIGINT")

			column(name: "lic_remote_access_rdv_fk", type: "BIGINT")

			column(name: "lic_vle_rdv_fk", type: "BIGINT")

			column(name: "lic_walkin_access_rdv_fk", type: "BIGINT")

			column(name: "lic_contact", type: "VARCHAR(255)")

			column(name: "lic_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-38") {
		createTable(tableName: "license_custom_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "LONGTEXT")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "LONGTEXT")

			column(name: "type_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "paragraph", type: "LONGTEXT")

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-39") {
		createTable(tableName: "license_private_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "LONGTEXT")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "paragraph", type: "LONGTEXT")

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "tenant_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "type_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-40") {
		createTable(tableName: "link") {
			column(autoIncrement: "true", name: "link_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "link_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "link_from_lic_fk", type: "BIGINT")

			column(name: "link_is_slaved", type: "BIGINT")

			column(name: "link_status_rv_fk", type: "BIGINT")

			column(name: "link_to_lic_fk", type: "BIGINT")

			column(name: "link_type_rv_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-41") {
		createTable(tableName: "object_definition") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-42") {
		createTable(tableName: "object_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "prop_type", type: "INT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-43") {
		createTable(tableName: "onixpl_license") {
			column(autoIncrement: "true", name: "opl_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "opl_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "opl_doc_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "opl_lastmod", type: "DATETIME")

			column(name: "opl_title", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-44") {
		createTable(tableName: "onixpl_license_text") {
			column(autoIncrement: "true", name: "oplt_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "oplt_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oplt_display_num", type: "VARCHAR(255)")

			column(name: "oplt_el_id", type: "VARCHAR(50)") {
				constraints(nullable: "false")
			}

			column(name: "oplt_opl_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "term_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oplt_text", type: "LONGTEXT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-45") {
		createTable(tableName: "onixpl_usage_term") {
			column(autoIncrement: "true", name: "oput_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "oput_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oput_opl_fk", type: "BIGINT")

			column(name: "oput_usage_status_rv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oput_usage_type_rv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-46") {
		createTable(tableName: "onixpl_usage_term_license_text") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "opul_oplt_fk", type: "BIGINT")

			column(name: "opul_oput_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-47") {
		createTable(tableName: "onixpl_usage_term_refdata_value") {
			column(name: "onixpl_usage_term_used_resource_id", type: "BIGINT")

			column(name: "refdata_value_id", type: "BIGINT")

			column(name: "onixpl_usage_term_user_id", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-48") {
		createTable(tableName: "org") {
			column(autoIncrement: "true", name: "org_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "org_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "org_address", type: "VARCHAR(256)")

			column(name: "org_cat", type: "VARCHAR(128)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "org_imp_id", type: "VARCHAR(256)")

			column(name: "org_ip_range", type: "VARCHAR(1024)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "org_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "org_type_rv_fk", type: "BIGINT")

			column(name: "org_scope", type: "VARCHAR(128)")

			column(name: "org_shortcode", type: "VARCHAR(128)")

			column(name: "org_status_rv_fk", type: "BIGINT")

			column(name: "org_membership", type: "BIGINT")

			column(name: "org_comment", type: "VARCHAR(2048)")

			column(name: "org_sector_rv_fk", type: "BIGINT")

			column(name: "org_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-49") {
		createTable(tableName: "org_bck") {
			column(defaultValueNumeric: "0", name: "org_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "org_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "org_address", type: "VARCHAR(256)")

			column(name: "org_cat", type: "VARCHAR(128)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "org_imp_id", type: "VARCHAR(256)")

			column(name: "org_ip_range", type: "VARCHAR(1024)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "org_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "org_type_rv_fk", type: "BIGINT")

			column(name: "org_scope", type: "VARCHAR(128)")

			column(name: "sector", type: "VARCHAR(128)")

			column(name: "org_shortcode", type: "VARCHAR(128)")

			column(name: "org_status_rv_fk", type: "BIGINT")

			column(name: "org_membership", type: "BIGINT")

			column(name: "org_comment", type: "VARCHAR(2048)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-50") {
		createTable(tableName: "org_cluster") {
			column(autoIncrement: "true", name: "oc_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "oc_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oc_cluster_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "oc_org_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-51") {
		createTable(tableName: "org_custom_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "LONGTEXT")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "type_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-52") {
		createTable(tableName: "org_perm_share") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "perm_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "rdv_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-53") {
		createTable(tableName: "org_private_property") {
			column(autoIncrement: "true", name: "opp_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "opp_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "VARCHAR(255)")

			column(name: "opp_owner_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "opp_tenant_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "opp_type_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-54") {
		createTable(tableName: "org_role") {
			column(autoIncrement: "true", name: "or_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "or_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "or_lic_fk", type: "BIGINT")

			column(name: "or_org_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "or_pkg_fk", type: "BIGINT")

			column(name: "or_roletype_fk", type: "BIGINT")

			column(name: "or_sub_fk", type: "BIGINT")

			column(name: "or_title_fk", type: "BIGINT")

			column(name: "or_end_date", type: "DATETIME")

			column(name: "or_start_date", type: "DATETIME")

			column(name: "or_cluster_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-55") {
		createTable(tableName: "org_title_instance") {
			column(autoIncrement: "true", name: "orgtitle_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "orgtitle_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "is_core", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "orgtitle_org", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "orgtitle_title", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-56") {
		createTable(tableName: "org_title_stats") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "last_retrieved_timestamp", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "org_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "title_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-57") {
		createTable(tableName: "package") {
			column(autoIncrement: "true", name: "pkg_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "pkg_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "auto_accept", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "pkg_breakable_rv_fk", type: "BIGINT")

			column(name: "pkg_cancellation_allowances", type: "LONGTEXT")

			column(name: "pkg_consistent_rv_fk", type: "BIGINT")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "pkg_end_date", type: "DATETIME")

			column(name: "pkg_fixed_rv_fk", type: "BIGINT")

			column(name: "pkg_forum_id", type: "VARCHAR(255)")

			column(name: "pkg_identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "pkg_imp_id", type: "VARCHAR(255)")

			column(name: "pkg_is_public", type: "BIGINT")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "pkg_license_fk", type: "BIGINT")

			column(name: "pkg_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "pkg_nominal_platform_fk", type: "BIGINT")

			column(name: "pkg_list_status_rv_fk", type: "BIGINT")

			column(name: "pkg_scope_rv_fk", type: "BIGINT")

			column(name: "pkg_status_rv_fk", type: "BIGINT")

			column(name: "pkg_type_rv_fk", type: "BIGINT")

			column(name: "pkg_sort_name", type: "VARCHAR(255)")

			column(name: "pkg_start_date", type: "DATETIME")

			column(name: "pkg_vendor_url", type: "VARCHAR(255)")

			column(name: "pkg_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-58") {
		createTable(tableName: "pending_change") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "pc_action_date", type: "DATETIME")

			column(name: "pc_change_doc", type: "LONGTEXT")

			column(name: "pc_desc", type: "LONGTEXT")

			column(name: "pc_lic_fk", type: "BIGINT")

			column(name: "pc_oid", type: "VARCHAR(255)")

			column(name: "pc_owner", type: "BIGINT")

			column(name: "pc_pkg_fk", type: "BIGINT")

			column(name: "pc_status_rdv_fk", type: "BIGINT")

			column(name: "pc_sub_fk", type: "BIGINT")

			column(name: "pc_sys_obj", type: "BIGINT")

			column(name: "pc_ts", type: "DATETIME")

			column(name: "pc_action_user_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-59") {
		createTable(tableName: "perm") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-60") {
		createTable(tableName: "perm_grant") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "perm_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "role_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-61") {
		createTable(tableName: "person") {
			column(autoIncrement: "true", name: "prs_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "prs_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "prs_first_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "prs_last_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "prs_middle_name", type: "VARCHAR(255)")

			column(name: "prs_org_fk", type: "BIGINT")

			column(name: "prs_is_public_rdv_fk", type: "BIGINT")

			column(name: "prs_owner_fk", type: "BIGINT")

			column(name: "prs_gender_rv_fk", type: "BIGINT")

			column(name: "prs_is_public_rv_fk", type: "BIGINT")

			column(name: "prs_tenant_fk", type: "BIGINT")

			column(name: "prs_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-62") {
		createTable(tableName: "person_private_property") {
			column(autoIncrement: "true", name: "ppp_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ppp_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "VARCHAR(255)")

			column(name: "ppp_owner_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "ppp_tenant_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ppp_type_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-63") {
		createTable(tableName: "person_role") {
			column(autoIncrement: "true", name: "pr_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "pr_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "pr_lic_fk", type: "BIGINT")

			column(name: "pr_org_fk", type: "BIGINT")

			column(name: "pr_pkg_fk", type: "BIGINT")

			column(name: "pr_prs_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "pr_sub_fk", type: "BIGINT")

			column(name: "pr_title_fk", type: "BIGINT")

			column(name: "pr_cluster_fk", type: "BIGINT")

			column(name: "pr_enddate", type: "DATETIME")

			column(name: "pr_startdate", type: "DATETIME")

			column(name: "pr_function_type_rv_fk", type: "BIGINT")

			column(name: "pr_responsibility_type_rv_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-64") {
		createTable(tableName: "platform") {
			column(autoIncrement: "true", name: "plat_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "plat_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "plat_imp_id", type: "VARCHAR(255)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "plat_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "plat_normalised_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "plat_primary_url", type: "VARCHAR(255)")

			column(name: "plat_data_provenance", type: "VARCHAR(255)")

			column(name: "plat_servprov_rv_fk", type: "BIGINT")

			column(name: "plat_softprov_rv_fk", type: "BIGINT")

			column(name: "plat_status_rv_fk", type: "BIGINT")

			column(name: "plat_type_rv_fk", type: "BIGINT")

			column(name: "plat_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-65") {
		createTable(tableName: "platformtipp") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "platform_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "rel", type: "VARCHAR(255)")

			column(name: "tipp_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "title_url", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-66") {
		createTable(tableName: "private_property_rule") {
			column(autoIncrement: "true", name: "pr_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "pr_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "pr_property_definition_fk", type: "BIGINT")

			column(name: "pr_property_owner_type", type: "VARCHAR(255)")

			column(name: "pr_property_tenant_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-67") {
		createTable(tableName: "property_definition") {
			column(autoIncrement: "true", name: "pd_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "pd_description", type: "VARCHAR(255)")

			column(name: "pd_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "pd_rdc", type: "VARCHAR(255)")

			column(name: "pd_type", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-68") {
		createTable(tableName: "property_value") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-69") {
		createTable(tableName: "refdata_category") {
			column(autoIncrement: "true", name: "rdc_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "rdc_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "rdc_description", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-70") {
		createTable(tableName: "refdata_value") {
			column(autoIncrement: "true", name: "rdv_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "rdv_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "rdv_icon", type: "VARCHAR(255)")

			column(name: "rdv_owner", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "rdv_value", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "rdv_group", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-71") {
		createTable(tableName: "reminder") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "active", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "amount", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "last_ran", type: "DATETIME")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "reminder_method_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "trigger_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "unit_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-72") {
		createTable(tableName: "role") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "role_type", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-73") {
		createTable(tableName: "setting") {
			column(autoIncrement: "true", name: "set_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "set_defvalue", type: "VARCHAR(1024)")

			column(name: "set_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "set_type", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "set_value", type: "VARCHAR(1024)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-74") {
		createTable(tableName: "site_page") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "action", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "alias", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "controller", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "rectype", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-75") {
		createTable(tableName: "subscription") {
			column(autoIncrement: "true", name: "sub_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "sub_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "cancellation_allowances", type: "VARCHAR(255)")

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "sub_end_date", type: "DATETIME")

			column(name: "sub_identifier", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "sub_imp_id", type: "VARCHAR(255)")

			column(name: "sub_parent_sub_fk", type: "BIGINT")

			column(name: "sub_is_public", type: "BIGINT")

			column(name: "sub_is_slaved", type: "BIGINT")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "sub_manual_renewal_date", type: "DATETIME")

			column(name: "sub_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "sub_notice_period", type: "VARCHAR(255)")

			column(name: "sub_owner_license_fk", type: "BIGINT")

			column(name: "sub_start_date", type: "DATETIME")

			column(name: "sub_status_rv_fk", type: "BIGINT")

			column(name: "sub_type_rv_fk", type: "BIGINT")

			column(name: "sub_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-76") {
		createTable(tableName: "subscription_custom_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "LONGTEXT")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "type_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-77") {
		createTable(tableName: "subscription_package") {
			column(autoIncrement: "true", name: "sp_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "sp_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "sp_pkg_fk", type: "BIGINT")

			column(name: "sp_sub_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-78") {
		createTable(tableName: "system_admin") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-79") {
		createTable(tableName: "system_admin_custom_property") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "dec_value", type: "DECIMAL(19,2)")

			column(name: "int_value", type: "INT")

			column(name: "note", type: "LONGTEXT")

			column(name: "owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "BIGINT")

			column(name: "string_value", type: "VARCHAR(255)")

			column(name: "type_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME")

			column(name: "date_value", type: "DATETIME")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-80") {
		createTable(tableName: "system_object") {
			column(autoIncrement: "true", name: "sys_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "sys_ann_forum_id", type: "VARCHAR(255)")

			column(name: "sys_id_str", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-81") {
		createTable(tableName: "title_history_event") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "event_date", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-82") {
		createTable(tableName: "title_history_event_participant") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "event_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "participant_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "participant_role", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-83") {
		createTable(tableName: "title_instance") {
			column(autoIncrement: "true", name: "ti_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "ti_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "ti_imp_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "ti_key_title", type: "VARCHAR(1024)")

			column(name: "last_updated", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "ti_norm_title", type: "VARCHAR(1024)")

			column(name: "sort_title", type: "VARCHAR(1024)")

			column(name: "ti_status_rv_fk", type: "BIGINT")

			column(name: "ti_title", type: "VARCHAR(1024)")

			column(name: "ti_type_rv_fk", type: "BIGINT")

			column(name: "ti_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-84") {
		createTable(tableName: "title_instance_package_platform") {
			column(autoIncrement: "true", name: "tipp_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "tipp_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tipp_access_end_date", type: "DATETIME")

			column(name: "tipp_access_start_date", type: "DATETIME")

			column(name: "tipp_core_status_end_date", type: "DATETIME")

			column(name: "tipp_core_status_start_date", type: "DATETIME")

			column(name: "tipp_coverage_depth", type: "VARCHAR(255)")

			column(name: "tipp_coverage_note", type: "LONGTEXT")

			column(name: "tipp_delayedoa_rv_fk", type: "BIGINT")

			column(name: "tipp_derived_from", type: "BIGINT")

			column(name: "tipp_embargo", type: "VARCHAR(255)")

			column(name: "tipp_end_date", type: "DATETIME")

			column(name: "tipp_end_issue", type: "VARCHAR(255)")

			column(name: "tipp_end_volume", type: "VARCHAR(255)")

			column(name: "tipp_host_platform_url", type: "VARCHAR(255)")

			column(name: "tipp_hybridoa_rv_fk", type: "BIGINT")

			column(name: "tipp_imp_id", type: "VARCHAR(255)")

			column(name: "tipp_option_rv_fk", type: "BIGINT")

			column(name: "tipp_payment_rv_fk", type: "BIGINT")

			column(name: "tipp_pkg_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tipp_plat_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tipp_rectype", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "tipp_start_date", type: "DATETIME")

			column(name: "tipp_start_issue", type: "VARCHAR(255)")

			column(name: "tipp_start_volume", type: "VARCHAR(255)")

			column(name: "tipp_status_rv_fk", type: "BIGINT")

			column(name: "tipp_status_reason_rv_fk", type: "BIGINT")

			column(name: "tipp_sub_fk", type: "BIGINT")

			column(name: "tipp_ti_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "master_tipp_id", type: "BIGINT")

			column(name: "tipp_guid", type: "VARCHAR(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-85") {
		createTable(tableName: "title_institution_provider") {
			column(autoIncrement: "true", name: "tiinp_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "title_inst_prov_ver", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tttnp_inst_org_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tttnp_prov_org_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tttnp_title", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-86") {
		createTable(tableName: "transformer") {
			column(name: "tfmr_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tfmr_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "tfmr_url", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-87") {
		createTable(tableName: "transforms") {
			column(name: "tr_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tr_accepts_format_rv_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "tr_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "tr_path_to_stylesheet", type: "VARCHAR(255)")

			column(name: "tr_return_file_extention", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "tr_return_mime", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "tr_transformer_fk", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-88") {
		createTable(tableName: "transforms_refdata_value") {
			column(name: "transforms_accepts_types_id", type: "BIGINT")

			column(name: "refdata_value_id", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-89") {
		createTable(tableName: "type_definition") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "type_name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "class", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-90") {
		createTable(tableName: "user") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "apikey", type: "VARCHAR(255)")

			column(name: "apisecret", type: "VARCHAR(255)")

			column(name: "default_dash_id", type: "BIGINT")

			column(name: "default_page_size", type: "BIGINT")

			column(name: "display", type: "VARCHAR(255)")

			column(name: "email", type: "VARCHAR(255)")

			column(name: "enabled", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "instcode", type: "VARCHAR(255)")

			column(name: "instname", type: "VARCHAR(255)")

			column(name: "password", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "shibb_scope", type: "VARCHAR(255)")

			column(name: "show_info_icon_id", type: "BIGINT")

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "show_simple_views_id", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-91") {
		createTable(tableName: "user_folder") {
			column(autoIncrement: "true", name: "uf_id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "uf_version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "uf_name", type: "VARCHAR(255)")

			column(name: "uf_shortcode", type: "VARCHAR(255)")

			column(name: "uf_owner_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-92") {
		createTable(tableName: "user_org") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_actioned", type: "BIGINT")

			column(name: "date_requested", type: "BIGINT")

			column(name: "formal_role_id", type: "BIGINT")

			column(name: "org_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "role", type: "VARCHAR(255)")

			column(name: "status", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-93") {
		createTable(tableName: "user_role") {
			column(name: "role_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-94") {
		createTable(tableName: "user_transforms") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "ut_transforms_fk", type: "BIGINT")

			column(name: "ut_user_fk", type: "BIGINT")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-95") {
		addPrimaryKey(columnNames: "role_id, user_id", tableName: "user_role")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-339") {
		createIndex(indexName: "doc_uuid_idx", tableName: "doc", unique: "false") {
			column(name: "doc_docstore_uuid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-340") {
		createIndex(indexName: "fact_uid_idx", tableName: "fact", unique: "false") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-341") {
		createIndex(indexName: "i10n_reference_field", tableName: "i10n_translation", unique: "true") {
			column(name: "i10n_reference_field")

			column(name: "i10n_reference_class")

			column(name: "i10n_reference_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-342") {
		createIndex(indexName: "id_value_idx", tableName: "identifier", unique: "false") {
			column(name: "id_ns_fk")

			column(name: "id_value")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-343") {
		createIndex(indexName: "ie_guid", tableName: "issue_entitlement", unique: "true") {
			column(name: "ie_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-344") {
		createIndex(indexName: "jusp_cursor_idx", tableName: "jusp_triple_cursor", unique: "false") {
			column(name: "jusp_login_id")

			column(name: "jusp_supplier_id")

			column(name: "jusp_title_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-345") {
		createIndex(indexName: "fact_access_idx", tableName: "kbplus_fact", unique: "false") {
			column(name: "inst_id")

			column(name: "related_title_id")

			column(name: "supplier_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-346") {
		createIndex(indexName: "fact_uid_idx", tableName: "kbplus_fact", unique: "false") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-347") {
		createIndex(indexName: "lic_guid", tableName: "license", unique: "true") {
			column(name: "lic_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-348") {
		createIndex(indexName: "oplt_el_id_idx", tableName: "onixpl_license_text", unique: "false") {
			column(name: "oplt_el_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-349") {
		createIndex(indexName: "oput_entry_idx", tableName: "onixpl_usage_term", unique: "false") {
			column(name: "oput_opl_fk")

			column(name: "oput_usage_status_rv_fk")

			column(name: "oput_usage_type_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-350") {
		createIndex(indexName: "opul_entry_idx", tableName: "onixpl_usage_term_license_text", unique: "false") {
			column(name: "opul_oplt_fk")

			column(name: "opul_oput_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-351") {
		createIndex(indexName: "org_guid", tableName: "org", unique: "true") {
			column(name: "org_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-352") {
		createIndex(indexName: "org_name_idx", tableName: "org", unique: "false") {
			column(name: "org_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-353") {
		createIndex(indexName: "org_shortcode_idx", tableName: "org", unique: "false") {
			column(name: "org_shortcode")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-354") {
		createIndex(indexName: "or_org_rt_idx", tableName: "org_role", unique: "false") {
			column(name: "or_org_fk")

			column(name: "or_roletype_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-355") {
		createIndex(indexName: "pkg_guid", tableName: "package", unique: "true") {
			column(name: "pkg_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-356") {
		createIndex(indexName: "pkg_imp_id_idx", tableName: "package", unique: "false") {
			column(name: "pkg_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-357") {
		createIndex(indexName: "pending_change_oid_idx", tableName: "pending_change", unique: "false") {
			column(name: "pc_oid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-358") {
		createIndex(indexName: "code", tableName: "perm", unique: "true") {
			column(name: "code")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-359") {
		createIndex(indexName: "prs_guid", tableName: "person", unique: "true") {
			column(name: "prs_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-360") {
		createIndex(indexName: "plat_guid", tableName: "platform", unique: "true") {
			column(name: "plat_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-361") {
		createIndex(indexName: "plat_imp_id_idx", tableName: "platform", unique: "false") {
			column(name: "plat_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-362") {
		createIndex(indexName: "td_name_idx", tableName: "property_definition", unique: "false") {
			column(name: "pd_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-363") {
		createIndex(indexName: "td_type_idx", tableName: "property_definition", unique: "false") {
			column(name: "pd_rdc")

			column(name: "pd_type")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-364") {
		createIndex(indexName: "rdc_description_idx", tableName: "refdata_category", unique: "false") {
			column(name: "rdc_description")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-365") {
		createIndex(indexName: "rdv_entry_idx", tableName: "refdata_value", unique: "false") {
			column(name: "rdv_owner")

			column(name: "rdv_value")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-366") {
		createIndex(indexName: "authority", tableName: "role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-367") {
		createIndex(indexName: "alias", tableName: "site_page", unique: "true") {
			column(name: "alias")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-368") {
		createIndex(indexName: "sub_guid", tableName: "subscription", unique: "true") {
			column(name: "sub_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-369") {
		createIndex(indexName: "sub_imp_id_idx", tableName: "subscription", unique: "false") {
			column(name: "sub_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-370") {
		createIndex(indexName: "ti_guid", tableName: "title_instance", unique: "true") {
			column(name: "ti_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-371") {
		createIndex(indexName: "ti_imp_id_idx", tableName: "title_instance", unique: "false") {
			column(name: "ti_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-372") {
		createIndex(indexName: "tipp_guid", tableName: "title_instance_package_platform", unique: "true") {
			column(name: "tipp_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-373") {
		createIndex(indexName: "tipp_idx", tableName: "title_instance_package_platform", unique: "false") {
			column(name: "tipp_pkg_fk")

			column(name: "tipp_plat_fk")

			column(name: "tipp_ti_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-374") {
		createIndex(indexName: "tipp_imp_id_idx", tableName: "title_instance_package_platform", unique: "false") {
			column(name: "tipp_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-375") {
		createIndex(indexName: "tiinp_idx", tableName: "title_institution_provider", unique: "false") {
			column(name: "tttnp_inst_org_fk")

			column(name: "tttnp_prov_org_fk")

			column(name: "tttnp_title")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-376") {
		createIndex(indexName: "username", tableName: "user", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-96") {
		addForeignKeyConstraint(baseColumnNames: "adr_org_fk", baseTableName: "address", baseTableSchemaName: "KBPlus", constraintName: "FKBB979BF4801A1AD7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-97") {
		addForeignKeyConstraint(baseColumnNames: "adr_prs_fk", baseTableName: "address", baseTableSchemaName: "KBPlus", constraintName: "FKBB979BF4B01CF0B5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "prs_id", referencedTableName: "person", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-98") {
		addForeignKeyConstraint(baseColumnNames: "adr_type_rv_fk", baseTableName: "address", baseTableSchemaName: "KBPlus", constraintName: "FKBB979BF425662A9C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-99") {
		addForeignKeyConstraint(baseColumnNames: "al_org_fk", baseTableName: "alert", baseTableSchemaName: "KBPlus", constraintName: "FK589895CC431E3DB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-100") {
		addForeignKeyConstraint(baseColumnNames: "al_user_fk", baseTableName: "alert", baseTableSchemaName: "KBPlus", constraintName: "FK589895CE0A42659", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-101") {
		addForeignKeyConstraint(baseColumnNames: "cl_owner_fk", baseTableName: "cluster", baseTableSchemaName: "KBPlus", constraintName: "FK33FB11FA69E7EA2E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-102") {
		addForeignKeyConstraint(baseColumnNames: "cl_type_rv_fk", baseTableName: "cluster", baseTableSchemaName: "KBPlus", constraintName: "FK33FB11FA73AFDEA2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-103") {
		addForeignKeyConstraint(baseColumnNames: "combo_from_org_fk", baseTableName: "combo", baseTableSchemaName: "KBPlus", constraintName: "FK5A7318E62A4664B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-104") {
		addForeignKeyConstraint(baseColumnNames: "combo_status_rv_fk", baseTableName: "combo", baseTableSchemaName: "KBPlus", constraintName: "FK5A7318E69CBC6D5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-105") {
		addForeignKeyConstraint(baseColumnNames: "combo_to_org_fk", baseTableName: "combo", baseTableSchemaName: "KBPlus", constraintName: "FK5A7318E6223BA1A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-106") {
		addForeignKeyConstraint(baseColumnNames: "combo_type_rv_fk", baseTableName: "combo", baseTableSchemaName: "KBPlus", constraintName: "FK5A7318EF93C805D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-107") {
		addForeignKeyConstraint(baseColumnNames: "ct_content_type_rv_fk", baseTableName: "contact", baseTableSchemaName: "KBPlus", constraintName: "FK38B72420D6406720", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-108") {
		addForeignKeyConstraint(baseColumnNames: "ct_org_fk", baseTableName: "contact", baseTableSchemaName: "KBPlus", constraintName: "FK38B724202BC428D5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-109") {
		addForeignKeyConstraint(baseColumnNames: "ct_prs_fk", baseTableName: "contact", baseTableSchemaName: "KBPlus", constraintName: "FK38B724205BC6FEB3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "prs_id", referencedTableName: "person", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-110") {
		addForeignKeyConstraint(baseColumnNames: "ct_type_rv_fk", baseTableName: "contact", baseTableSchemaName: "KBPlus", constraintName: "FK38B72420B13D099A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-111") {
		addForeignKeyConstraint(baseColumnNames: "ca_owner", baseTableName: "core_assertion", baseTableSchemaName: "KBPlus", constraintName: "FKE48406625AD1EB60", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tiinp_id", referencedTableName: "title_institution_provider", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-112") {
		addForeignKeyConstraint(baseColumnNames: "ci_billing_currency_rv_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C4585DC4AE0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-113") {
		addForeignKeyConstraint(baseColumnNames: "ci_cat_rv_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45FE188C0F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-114") {
		addForeignKeyConstraint(baseColumnNames: "ci_e_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C455C9F1829", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-115") {
		addForeignKeyConstraint(baseColumnNames: "ci_element_rv_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45BC27AB35", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-116") {
		addForeignKeyConstraint(baseColumnNames: "ci_inv_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45E55745DC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "inv_id", referencedTableName: "invoice", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-117") {
		addForeignKeyConstraint(baseColumnNames: "ci_ord_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C4590AA9CD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ord_id", referencedTableName: "kbplus_ord", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-118") {
		addForeignKeyConstraint(baseColumnNames: "ci_owner", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45E4E73EE1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-119") {
		addForeignKeyConstraint(baseColumnNames: "ci_status_rv_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C456F474AFD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-120") {
		addForeignKeyConstraint(baseColumnNames: "ci_sub_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45FFAD2337", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-121") {
		addForeignKeyConstraint(baseColumnNames: "ci_subPkg_fk", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45D820FE8B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sp_id", referencedTableName: "subscription_package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-122") {
		addForeignKeyConstraint(baseColumnNames: "ci_tax_code", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45B30B076B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-123") {
		addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C45B47E66A0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-124") {
		addForeignKeyConstraint(baseColumnNames: "last_updated_by_id", baseTableName: "cost_item", baseTableSchemaName: "KBPlus", constraintName: "FKEFE45C453096044A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-125") {
		addForeignKeyConstraint(baseColumnNames: "cig_budgetcode_fk", baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE05B522EC25", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-126") {
		addForeignKeyConstraint(baseColumnNames: "cig_costItem_fk", baseTableName: "cost_item_group", baseTableSchemaName: "KBPlus", constraintName: "FK1C3AAE051D1B4283", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ci_id", referencedTableName: "cost_item", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-127") {
		addForeignKeyConstraint(baseColumnNames: "doc_alert_fk", baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK185381FC434AE", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "alert", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-128") {
		addForeignKeyConstraint(baseColumnNames: "doc_migrated", baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK18538F206CBF4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-129") {
		addForeignKeyConstraint(baseColumnNames: "doc_status_rv_fk", baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK185387758376B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-130") {
		addForeignKeyConstraint(baseColumnNames: "doc_type_rv_fk", baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK1853897381E73", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-131") {
		addForeignKeyConstraint(baseColumnNames: "doc_user_fk", baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK185382F964266", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-132") {
		addForeignKeyConstraint(baseColumnNames: "dc_alert_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A8B88BED47", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "alert", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-133") {
		addForeignKeyConstraint(baseColumnNames: "dc_doc_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A8C7230C87", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "doc_id", referencedTableName: "doc", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-134") {
		addForeignKeyConstraint(baseColumnNames: "dc_lic_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A8A43E5A02", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-135") {
		addForeignKeyConstraint(baseColumnNames: "dc_pkg_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A871246D01", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-136") {
		addForeignKeyConstraint(baseColumnNames: "dc_rv_doctype_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A858752A7E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-137") {
		addForeignKeyConstraint(baseColumnNames: "dc_status_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A8B9538E23", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-138") {
		addForeignKeyConstraint(baseColumnNames: "dc_sub_fk", baseTableName: "doc_context", baseTableSchemaName: "KBPlus", constraintName: "FK30EBA9A824AA84FE", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-139") {
		addForeignKeyConstraint(baseColumnNames: "fact_type_rdv_fk", baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C5CC2FB63", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-140") {
		addForeignKeyConstraint(baseColumnNames: "inst_id", baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66CD2A25EFB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-141") {
		addForeignKeyConstraint(baseColumnNames: "juspio_id", baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C467CFA43", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "io_id", referencedTableName: "identifier_occurrence", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-142") {
		addForeignKeyConstraint(baseColumnNames: "related_title_id", baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C4CB39BA6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-143") {
		addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C40C7D5B5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-144") {
		addForeignKeyConstraint(baseColumnNames: "folder_id", baseTableName: "folder_item", baseTableSchemaName: "KBPlus", constraintName: "FK695AC446D580EE2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "uf_id", referencedTableName: "user_folder", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-145") {
		addForeignKeyConstraint(baseColumnNames: "gri_kbplus_compliant", baseTableName: "global_record_info", baseTableSchemaName: "KBPlus", constraintName: "FKB057C1402753393F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-146") {
		addForeignKeyConstraint(baseColumnNames: "gri_source_fk", baseTableName: "global_record_info", baseTableSchemaName: "KBPlus", constraintName: "FKB057C140E1AE5394", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "grs_id", referencedTableName: "global_record_source", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-147") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "global_record_tracker", baseTableSchemaName: "KBPlus", constraintName: "FK808F5966D92AE946", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-148") {
		addForeignKeyConstraint(baseColumnNames: "id_ig_fk", baseTableName: "identifier", baseTableSchemaName: "KBPlus", constraintName: "FK9F88ACA96235C89B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ig_id", referencedTableName: "identifier_group", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-149") {
		addForeignKeyConstraint(baseColumnNames: "id_ns_fk", baseTableName: "identifier", baseTableSchemaName: "KBPlus", constraintName: "FK9F88ACA9F1F42470", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "idns_id", referencedTableName: "identifier_namespace", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-150") {
		addForeignKeyConstraint(baseColumnNames: "idns_type_fl", baseTableName: "identifier_namespace", baseTableSchemaName: "KBPlus", constraintName: "FKBA7FFD4534D73C7D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-151") {
		addForeignKeyConstraint(baseColumnNames: "io_canonical_id", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F279B08D7A5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id_id", referencedTableName: "identifier", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-152") {
		addForeignKeyConstraint(baseColumnNames: "io_lic_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F2768229F7B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-153") {
		addForeignKeyConstraint(baseColumnNames: "io_org_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F279DF8E280", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-154") {
		addForeignKeyConstraint(baseColumnNames: "io_pkg_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F273508B27A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-155") {
		addForeignKeyConstraint(baseColumnNames: "io_sub_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F27E88ECA77", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-156") {
		addForeignKeyConstraint(baseColumnNames: "io_ti_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F27B37DC426", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-157") {
		addForeignKeyConstraint(baseColumnNames: "io_tipp_fk", baseTableName: "identifier_occurrence", baseTableSchemaName: "KBPlus", constraintName: "FKF0533F27CDDD0AFF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-158") {
		addForeignKeyConstraint(baseColumnNames: "ir_from_id_fk", baseTableName: "identifier_relation", baseTableSchemaName: "KBPlus", constraintName: "FKDA4DACD2BB32F750", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id_id", referencedTableName: "identifier", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-159") {
		addForeignKeyConstraint(baseColumnNames: "ir_rel_rdv_id_fk", baseTableName: "identifier_relation", baseTableSchemaName: "KBPlus", constraintName: "FKDA4DACD2AD4573E3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-160") {
		addForeignKeyConstraint(baseColumnNames: "ir_to_id_fk", baseTableName: "identifier_relation", baseTableSchemaName: "KBPlus", constraintName: "FKDA4DACD22ED80AE1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id_id", referencedTableName: "identifier", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-161") {
		addForeignKeyConstraint(baseColumnNames: "inv_owner", baseTableName: "invoice", baseTableSchemaName: "KBPlus", constraintName: "FK74D6432DDB56B62C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-162") {
		addForeignKeyConstraint(baseColumnNames: "core_status_id", baseTableName: "issue_entitlement", baseTableSchemaName: "KBPlus", constraintName: "FK2D45F6C71268C999", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-163") {
		addForeignKeyConstraint(baseColumnNames: "ie_medium_rv_fk", baseTableName: "issue_entitlement", baseTableSchemaName: "KBPlus", constraintName: "FK2D45F6C75BEA734A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-164") {
		addForeignKeyConstraint(baseColumnNames: "ie_status_rv_fk", baseTableName: "issue_entitlement", baseTableSchemaName: "KBPlus", constraintName: "FK2D45F6C72F4A207", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-165") {
		addForeignKeyConstraint(baseColumnNames: "ie_subscription_fk", baseTableName: "issue_entitlement", baseTableSchemaName: "KBPlus", constraintName: "FK2D45F6C775F8181E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-166") {
		addForeignKeyConstraint(baseColumnNames: "ie_tipp_fk", baseTableName: "issue_entitlement", baseTableSchemaName: "KBPlus", constraintName: "FK2D45F6C7330B4F5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-167") {
		addForeignKeyConstraint(baseColumnNames: "comm_alert_fk", baseTableName: "kb_comment", baseTableSchemaName: "KBPlus", constraintName: "FKA21A5B771226D95A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "alert", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-168") {
		addForeignKeyConstraint(baseColumnNames: "comm_by_user_fk", baseTableName: "kb_comment", baseTableSchemaName: "KBPlus", constraintName: "FKA21A5B7720B74198", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-169") {
		addForeignKeyConstraint(baseColumnNames: "fact_type_rdv_fk", baseTableName: "kbplus_fact", baseTableSchemaName: "KBPlus", constraintName: "FK6784767A5CC2FB63", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-170") {
		addForeignKeyConstraint(baseColumnNames: "inst_id", baseTableName: "kbplus_fact", baseTableSchemaName: "KBPlus", constraintName: "FK6784767AD2A25EFB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-171") {
		addForeignKeyConstraint(baseColumnNames: "juspio_id", baseTableName: "kbplus_fact", baseTableSchemaName: "KBPlus", constraintName: "FK6784767A467CFA43", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "io_id", referencedTableName: "identifier_occurrence", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-172") {
		addForeignKeyConstraint(baseColumnNames: "related_title_id", baseTableName: "kbplus_fact", baseTableSchemaName: "KBPlus", constraintName: "FK6784767A4CB39BA6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-173") {
		addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "kbplus_fact", baseTableSchemaName: "KBPlus", constraintName: "FK6784767A40C7D5B5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-174") {
		addForeignKeyConstraint(baseColumnNames: "ord_owner", baseTableName: "kbplus_ord", baseTableSchemaName: "KBPlus", constraintName: "FK6EB1D5133C45D91C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-175") {
		addForeignKeyConstraint(baseColumnNames: "lic_alumni_access_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441B6A1F9E4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-176") {
		addForeignKeyConstraint(baseColumnNames: "lic_category_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844110F7C2B9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-177") {
		addForeignKeyConstraint(baseColumnNames: "lic_concurrent_users_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441595820F7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-178") {
		addForeignKeyConstraint(baseColumnNames: "lic_coursepack_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844177A5D483", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-179") {
		addForeignKeyConstraint(baseColumnNames: "lic_enterprise_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084416C7CF136", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-180") {
		addForeignKeyConstraint(baseColumnNames: "lic_ill_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084414B2F78C0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-181") {
		addForeignKeyConstraint(baseColumnNames: "lic_is_public_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084413D2ACEB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-182") {
		addForeignKeyConstraint(baseColumnNames: "lic_multisite_access_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844119B9B694", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-183") {
		addForeignKeyConstraint(baseColumnNames: "lic_opl_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441F5A55C6C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "opl_id", referencedTableName: "onixpl_license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-184") {
		addForeignKeyConstraint(baseColumnNames: "lic_partners_access_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441D4BECC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-185") {
		addForeignKeyConstraint(baseColumnNames: "lic_pca_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084414C1CBBFB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-186") {
		addForeignKeyConstraint(baseColumnNames: "lic_remote_access_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441D77ABF2C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-187") {
		addForeignKeyConstraint(baseColumnNames: "lic_status_rv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844168C2A8DD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-188") {
		addForeignKeyConstraint(baseColumnNames: "lic_type_rv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441F144465", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-189") {
		addForeignKeyConstraint(baseColumnNames: "lic_vle_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441DBC1FD3A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-190") {
		addForeignKeyConstraint(baseColumnNames: "lic_walkin_access_rdv_fk", baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844197334194", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-191") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "license_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKE8DF0AE590DECB4B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-192") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "license_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKE8DF0AE52992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-193") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "license_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKE8DF0AE5D4223E79", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-194") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354F90DECB4B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-195") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354F2992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-196") {
		addForeignKeyConstraint(baseColumnNames: "tenant_fk", baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354FEF8511C1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-197") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354F638A6383", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-198") {
		addForeignKeyConstraint(baseColumnNames: "link_from_lic_fk", baseTableName: "link", baseTableSchemaName: "KBPlus", constraintName: "FK32AFFACB535FB2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-199") {
		addForeignKeyConstraint(baseColumnNames: "link_is_slaved", baseTableName: "link", baseTableSchemaName: "KBPlus", constraintName: "FK32AFFA2C22D5CE", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-200") {
		addForeignKeyConstraint(baseColumnNames: "link_status_rv_fk", baseTableName: "link", baseTableSchemaName: "KBPlus", constraintName: "FK32AFFA9BEE17E9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-201") {
		addForeignKeyConstraint(baseColumnNames: "link_to_lic_fk", baseTableName: "link", baseTableSchemaName: "KBPlus", constraintName: "FK32AFFA158EDE81", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-202") {
		addForeignKeyConstraint(baseColumnNames: "link_type_rv_fk", baseTableName: "link", baseTableSchemaName: "KBPlus", constraintName: "FK32AFFA38280671", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-203") {
		addForeignKeyConstraint(baseColumnNames: "opl_doc_fk", baseTableName: "onixpl_license", baseTableSchemaName: "KBPlus", constraintName: "FK620852CC4D1AE0DB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "doc_id", referencedTableName: "doc", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-204") {
		addForeignKeyConstraint(baseColumnNames: "oplt_opl_fk", baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0F8B5EA69", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "opl_id", referencedTableName: "onixpl_license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-205") {
		addForeignKeyConstraint(baseColumnNames: "term_id", baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0EF8C4AB4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "oput_id", referencedTableName: "onixpl_usage_term", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-206") {
		addForeignKeyConstraint(baseColumnNames: "oput_opl_fk", baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDF2F1DD172", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "opl_id", referencedTableName: "onixpl_license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-207") {
		addForeignKeyConstraint(baseColumnNames: "oput_usage_status_rv_fk", baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFE9F8A801", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-208") {
		addForeignKeyConstraint(baseColumnNames: "oput_usage_type_rv_fk", baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFCF47BC89", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-209") {
		addForeignKeyConstraint(baseColumnNames: "opul_oplt_fk", baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB55D5F69B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "oplt_id", referencedTableName: "onixpl_license_text", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-210") {
		addForeignKeyConstraint(baseColumnNames: "opul_oput_fk", baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB3313FD03", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "oput_id", referencedTableName: "onixpl_usage_term", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-211") {
		addForeignKeyConstraint(baseColumnNames: "onixpl_usage_term_used_resource_id", baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770F7B0024B0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "oput_id", referencedTableName: "onixpl_usage_term", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-212") {
		addForeignKeyConstraint(baseColumnNames: "onixpl_usage_term_user_id", baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FE27A4895", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "oput_id", referencedTableName: "onixpl_usage_term", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-213") {
		addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FAAD0839C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-214") {
		addForeignKeyConstraint(baseColumnNames: "org_membership", baseTableName: "org", baseTableSchemaName: "KBPlus", constraintName: "FK1AEE4269A4DC2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-215") {
		addForeignKeyConstraint(baseColumnNames: "org_sector_rv_fk", baseTableName: "org", baseTableSchemaName: "KBPlus", constraintName: "FK1AEE448A545B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-216") {
		addForeignKeyConstraint(baseColumnNames: "org_status_rv_fk", baseTableName: "org", baseTableSchemaName: "KBPlus", constraintName: "FK1AEE4BDA6253F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-217") {
		addForeignKeyConstraint(baseColumnNames: "org_type_rv_fk", baseTableName: "org", baseTableSchemaName: "KBPlus", constraintName: "FK1AEE4360F7147", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-218") {
		addForeignKeyConstraint(baseColumnNames: "oc_cluster_fk", baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439FB7B3B52", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "cl_id", referencedTableName: "cluster", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-219") {
		addForeignKeyConstraint(baseColumnNames: "oc_org_fk", baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439F39056212", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-220") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "org_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKD7848F88C115DF6E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-221") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "org_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKD7848F882992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-222") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "org_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKD7848F88D4223E79", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-223") {
		addForeignKeyConstraint(baseColumnNames: "perm_id", baseTableName: "org_perm_share", baseTableSchemaName: "KBPlus", constraintName: "FK4EEB620B17B140A3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "perm", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-224") {
		addForeignKeyConstraint(baseColumnNames: "rdv_id", baseTableName: "org_perm_share", baseTableSchemaName: "KBPlus", constraintName: "FK4EEB620BBFE25067", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-225") {
		addForeignKeyConstraint(baseColumnNames: "opp_owner_fk", baseTableName: "org_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKDFC7450C20B176A8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-226") {
		addForeignKeyConstraint(baseColumnNames: "opp_tenant_fk", baseTableName: "org_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKDFC7450C835C6C31", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-227") {
		addForeignKeyConstraint(baseColumnNames: "opp_type_fk", baseTableName: "org_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKDFC7450C3D55999D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-228") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "org_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKDFC7450C2992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-229") {
		addForeignKeyConstraint(baseColumnNames: "or_cluster_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F17EE3EBE3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "cl_id", referencedTableName: "cluster", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-230") {
		addForeignKeyConstraint(baseColumnNames: "or_lic_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F11960C01E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-231") {
		addForeignKeyConstraint(baseColumnNames: "or_org_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F14F370323", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-232") {
		addForeignKeyConstraint(baseColumnNames: "or_pkg_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F1E646D31D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-233") {
		addForeignKeyConstraint(baseColumnNames: "or_roletype_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F1879D5409", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-234") {
		addForeignKeyConstraint(baseColumnNames: "or_sub_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F199CCEB1A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-235") {
		addForeignKeyConstraint(baseColumnNames: "or_title_fk", baseTableName: "org_role", baseTableSchemaName: "KBPlus", constraintName: "FK4E5C38F16D6B9898", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-236") {
		addForeignKeyConstraint(baseColumnNames: "orgtitle_org", baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157CC172760", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-237") {
		addForeignKeyConstraint(baseColumnNames: "orgtitle_title", baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157F0E2D5FD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-238") {
		addForeignKeyConstraint(baseColumnNames: "org_id", baseTableName: "org_title_stats", baseTableSchemaName: "KBPlus", constraintName: "FK8FC8FF1D21D4E99D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-239") {
		addForeignKeyConstraint(baseColumnNames: "title_id", baseTableName: "org_title_stats", baseTableSchemaName: "KBPlus", constraintName: "FK8FC8FF1D10288612", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-240") {
		addForeignKeyConstraint(baseColumnNames: "pkg_breakable_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE534462B6E84F8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-241") {
		addForeignKeyConstraint(baseColumnNames: "pkg_consistent_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE534462CA44477", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-242") {
		addForeignKeyConstraint(baseColumnNames: "pkg_fixed_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE53446D4A9C3D3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-243") {
		addForeignKeyConstraint(baseColumnNames: "pkg_is_public", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE53446F8DFD21C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-244") {
		addForeignKeyConstraint(baseColumnNames: "pkg_license_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE53446B510F2FA", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-245") {
		addForeignKeyConstraint(baseColumnNames: "pkg_list_status_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE5344653438212", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-246") {
		addForeignKeyConstraint(baseColumnNames: "pkg_nominal_platform_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE53446E9794A2B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-247") {
		addForeignKeyConstraint(baseColumnNames: "pkg_scope_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE534461FB972B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-248") {
		addForeignKeyConstraint(baseColumnNames: "pkg_status_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE534462A381B57", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-249") {
		addForeignKeyConstraint(baseColumnNames: "pkg_type_rv_fk", baseTableName: "package", baseTableSchemaName: "KBPlus", constraintName: "FKCFE5344692580D5F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-250") {
		addForeignKeyConstraint(baseColumnNames: "pc_action_user_fk", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF58A358B930", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-251") {
		addForeignKeyConstraint(baseColumnNames: "pc_lic_fk", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF5897738E0E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-252") {
		addForeignKeyConstraint(baseColumnNames: "pc_owner", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF58EDF122AE", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-253") {
		addForeignKeyConstraint(baseColumnNames: "pc_pkg_fk", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF586459A10D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-254") {
		addForeignKeyConstraint(baseColumnNames: "pc_status_rdv_fk", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF58CD0303B2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-255") {
		addForeignKeyConstraint(baseColumnNames: "pc_sub_fk", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF5817DFB90A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-256") {
		addForeignKeyConstraint(baseColumnNames: "pc_sys_obj", baseTableName: "pending_change", baseTableSchemaName: "KBPlus", constraintName: "FK65CBDF58B7F24D84", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sys_id", referencedTableName: "system_object", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-257") {
		addForeignKeyConstraint(baseColumnNames: "perm_id", baseTableName: "perm_grant", baseTableSchemaName: "KBPlus", constraintName: "FKCF6BA30D17B140A3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "perm", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-258") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "perm_grant", baseTableSchemaName: "KBPlus", constraintName: "FKCF6BA30D92370AE3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-259") {
		addForeignKeyConstraint(baseColumnNames: "prs_gender_rv_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B55905E0221", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-260") {
		addForeignKeyConstraint(baseColumnNames: "prs_is_public_rdv_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B5526759820", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-261") {
		addForeignKeyConstraint(baseColumnNames: "prs_is_public_rv_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B55750B1C62", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-262") {
		addForeignKeyConstraint(baseColumnNames: "prs_org_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B555406FA95", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-263") {
		addForeignKeyConstraint(baseColumnNames: "prs_owner_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B552F08D4E6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-264") {
		addForeignKeyConstraint(baseColumnNames: "prs_tenant_fk", baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B553FF0D5B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-265") {
		addForeignKeyConstraint(baseColumnNames: "ppp_owner_fk", baseTableName: "person_private_property", baseTableSchemaName: "KBPlus", constraintName: "FK99DFA8BB56ABA4D2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "prs_id", referencedTableName: "person", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-266") {
		addForeignKeyConstraint(baseColumnNames: "ppp_tenant_fk", baseTableName: "person_private_property", baseTableSchemaName: "KBPlus", constraintName: "FK99DFA8BB71DF72B2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-267") {
		addForeignKeyConstraint(baseColumnNames: "ppp_type_fk", baseTableName: "person_private_property", baseTableSchemaName: "KBPlus", constraintName: "FK99DFA8BBD23A4C5E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-268") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "person_private_property", baseTableSchemaName: "KBPlus", constraintName: "FK99DFA8BB2992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-269") {
		addForeignKeyConstraint(baseColumnNames: "pr_cluster_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B206D66F264", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "cl_id", referencedTableName: "cluster", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-270") {
		addForeignKeyConstraint(baseColumnNames: "pr_function_type_rv_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B20253E5216", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-271") {
		addForeignKeyConstraint(baseColumnNames: "pr_lic_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B20ADA52F1F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-272") {
		addForeignKeyConstraint(baseColumnNames: "pr_org_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B20E37B7224", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-273") {
		addForeignKeyConstraint(baseColumnNames: "pr_pkg_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B207A8B421E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-274") {
		addForeignKeyConstraint(baseColumnNames: "pr_prs_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B20137E4802", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "prs_id", referencedTableName: "person", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-275") {
		addForeignKeyConstraint(baseColumnNames: "pr_responsibility_type_rv_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B20AC241BE0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-276") {
		addForeignKeyConstraint(baseColumnNames: "pr_sub_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B202E115A1B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-277") {
		addForeignKeyConstraint(baseColumnNames: "pr_title_fk", baseTableName: "person_role", baseTableSchemaName: "KBPlus", constraintName: "FKE6A16B202504B59", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-278") {
		addForeignKeyConstraint(baseColumnNames: "plat_servprov_rv_fk", baseTableName: "platform", baseTableSchemaName: "KBPlus", constraintName: "FK6FBD68739969A1A1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-279") {
		addForeignKeyConstraint(baseColumnNames: "plat_softprov_rv_fk", baseTableName: "platform", baseTableSchemaName: "KBPlus", constraintName: "FK6FBD6873646ABCB5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-280") {
		addForeignKeyConstraint(baseColumnNames: "plat_status_rv_fk", baseTableName: "platform", baseTableSchemaName: "KBPlus", constraintName: "FK6FBD6873E3F2DAD4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-281") {
		addForeignKeyConstraint(baseColumnNames: "plat_type_rv_fk", baseTableName: "platform", baseTableSchemaName: "KBPlus", constraintName: "FK6FBD68736DC6881C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-282") {
		addForeignKeyConstraint(baseColumnNames: "platform_id", baseTableName: "platformtipp", baseTableSchemaName: "KBPlus", constraintName: "FK9544A2810252C57", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-283") {
		addForeignKeyConstraint(baseColumnNames: "tipp_id", baseTableName: "platformtipp", baseTableSchemaName: "KBPlus", constraintName: "FK9544A28C581DD6E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-284") {
		addForeignKeyConstraint(baseColumnNames: "pr_property_definition_fk", baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656AC258E067", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-285") {
		addForeignKeyConstraint(baseColumnNames: "pr_property_tenant_fk", baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656ACB238934", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-286") {
		addForeignKeyConstraint(baseColumnNames: "rdv_owner", baseTableName: "refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKF33A596F18DAEBF6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdc_id", referencedTableName: "refdata_category", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-287") {
		addForeignKeyConstraint(baseColumnNames: "reminder_method_id", baseTableName: "reminder", baseTableSchemaName: "KBPlus", constraintName: "FKE116C0723D70D35D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-288") {
		addForeignKeyConstraint(baseColumnNames: "trigger_id", baseTableName: "reminder", baseTableSchemaName: "KBPlus", constraintName: "FKE116C07248E736B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-289") {
		addForeignKeyConstraint(baseColumnNames: "unit_id", baseTableName: "reminder", baseTableSchemaName: "KBPlus", constraintName: "FKE116C072E68D8F67", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-290") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "reminder", baseTableSchemaName: "KBPlus", constraintName: "FKE116C0723761CEC3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-291") {
		addForeignKeyConstraint(baseColumnNames: "sub_is_public", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591D28E1DD90", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-292") {
		addForeignKeyConstraint(baseColumnNames: "sub_is_slaved", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591D2D814494", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-293") {
		addForeignKeyConstraint(baseColumnNames: "sub_owner_license_fk", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591D7D96D7D2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-294") {
		addForeignKeyConstraint(baseColumnNames: "sub_parent_sub_fk", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591D530432B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-295") {
		addForeignKeyConstraint(baseColumnNames: "sub_status_rv_fk", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591DE82AEB63", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-296") {
		addForeignKeyConstraint(baseColumnNames: "sub_type_rv_fk", baseTableName: "subscription", baseTableSchemaName: "KBPlus", constraintName: "FK1456591D6297706B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-297") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "subscription_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FK8717A7C14B06441", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-298") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "subscription_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FK8717A7C12992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-299") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "subscription_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FK8717A7C1D4223E79", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-300") {
		addForeignKeyConstraint(baseColumnNames: "sp_pkg_fk", baseTableName: "subscription_package", baseTableSchemaName: "KBPlus", constraintName: "FK5122C72467963563", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-301") {
		addForeignKeyConstraint(baseColumnNames: "sp_sub_fk", baseTableName: "subscription_package", baseTableSchemaName: "KBPlus", constraintName: "FK5122C7241B1C4D60", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-302") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "system_admin_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKB7F703E3AFAAC3EA", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "system_admin", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-303") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "system_admin_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKB7F703E32992A286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-304") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "system_admin_custom_property", baseTableSchemaName: "KBPlus", constraintName: "FKB7F703E3D4223E79", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-305") {
		addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "title_history_event_participant", baseTableSchemaName: "KBPlus", constraintName: "FKE3AB36FCC15EF6E1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "title_history_event", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-306") {
		addForeignKeyConstraint(baseColumnNames: "participant_id", baseTableName: "title_history_event_participant", baseTableSchemaName: "KBPlus", constraintName: "FKE3AB36FC897EC757", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-307") {
		addForeignKeyConstraint(baseColumnNames: "ti_status_rv_fk", baseTableName: "title_instance", baseTableSchemaName: "KBPlus", constraintName: "FKACC69C66D9594E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-308") {
		addForeignKeyConstraint(baseColumnNames: "ti_type_rv_fk", baseTableName: "title_instance", baseTableSchemaName: "KBPlus", constraintName: "FKACC69C334E5D16", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-309") {
		addForeignKeyConstraint(baseColumnNames: "master_tipp_id", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F31072C91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-310") {
		addForeignKeyConstraint(baseColumnNames: "tipp_delayedoa_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F7130FBFC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-311") {
		addForeignKeyConstraint(baseColumnNames: "tipp_derived_from", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F922AC05F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-312") {
		addForeignKeyConstraint(baseColumnNames: "tipp_hybridoa_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8FB46EBDEA", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-313") {
		addForeignKeyConstraint(baseColumnNames: "tipp_option_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F16ABD6D1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-314") {
		addForeignKeyConstraint(baseColumnNames: "tipp_payment_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F5A337F4E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-315") {
		addForeignKeyConstraint(baseColumnNames: "tipp_pkg_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F54894D8B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-316") {
		addForeignKeyConstraint(baseColumnNames: "tipp_plat_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F810634BB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-317") {
		addForeignKeyConstraint(baseColumnNames: "tipp_status_reason_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8FF3DE1BB9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-318") {
		addForeignKeyConstraint(baseColumnNames: "tipp_status_rv_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F3E48CC8E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-319") {
		addForeignKeyConstraint(baseColumnNames: "tipp_sub_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F80F6588", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-320") {
		addForeignKeyConstraint(baseColumnNames: "tipp_ti_fk", baseTableName: "title_instance_package_platform", baseTableSchemaName: "KBPlus", constraintName: "FKE793FB8F40E502F5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-321") {
		addForeignKeyConstraint(baseColumnNames: "tttnp_inst_org_fk", baseTableName: "title_institution_provider", baseTableSchemaName: "KBPlus", constraintName: "FK89A2E01F35702557", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-322") {
		addForeignKeyConstraint(baseColumnNames: "tttnp_prov_org_fk", baseTableName: "title_institution_provider", baseTableSchemaName: "KBPlus", constraintName: "FK89A2E01F97876AD4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-323") {
		addForeignKeyConstraint(baseColumnNames: "tttnp_title", baseTableName: "title_institution_provider", baseTableSchemaName: "KBPlus", constraintName: "FK89A2E01F47B4BD3F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-324") {
		addForeignKeyConstraint(baseColumnNames: "tr_accepts_format_rv_fk", baseTableName: "transforms", baseTableSchemaName: "KBPlus", constraintName: "FK990F02873696527E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-325") {
		addForeignKeyConstraint(baseColumnNames: "tr_transformer_fk", baseTableName: "transforms", baseTableSchemaName: "KBPlus", constraintName: "FK990F0287277F0208", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tfmr_id", referencedTableName: "transformer", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-326") {
		addForeignKeyConstraint(baseColumnNames: "refdata_value_id", baseTableName: "transforms_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKF0E0B5B7AAD0839C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-327") {
		addForeignKeyConstraint(baseColumnNames: "transforms_accepts_types_id", baseTableName: "transforms_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKF0E0B5B73B259171", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tr_id", referencedTableName: "transforms", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-328") {
		addForeignKeyConstraint(baseColumnNames: "default_dash_id", baseTableName: "user", baseTableSchemaName: "KBPlus", constraintName: "FK36EBCBB3BC6E31", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-329") {
		addForeignKeyConstraint(baseColumnNames: "show_info_icon_id", baseTableName: "user", baseTableSchemaName: "KBPlus", constraintName: "FK36EBCBFD5BDBC3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-330") {
		addForeignKeyConstraint(baseColumnNames: "show_simple_views_id", baseTableName: "user", baseTableSchemaName: "KBPlus", constraintName: "FK36EBCB6D1F0CE8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-331") {
		addForeignKeyConstraint(baseColumnNames: "uf_owner_id", baseTableName: "user_folder", baseTableSchemaName: "KBPlus", constraintName: "FKE0966362A7674C9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-332") {
		addForeignKeyConstraint(baseColumnNames: "formal_role_id", baseTableName: "user_org", baseTableSchemaName: "KBPlus", constraintName: "FKF022EC7077234A93", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-333") {
		addForeignKeyConstraint(baseColumnNames: "org_id", baseTableName: "user_org", baseTableSchemaName: "KBPlus", constraintName: "FKF022EC7021D4E99D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-334") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_org", baseTableSchemaName: "KBPlus", constraintName: "FKF022EC703761CEC3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-335") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", baseTableSchemaName: "KBPlus", constraintName: "FK143BF46A92370AE3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-336") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", baseTableSchemaName: "KBPlus", constraintName: "FK143BF46A3761CEC3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-337") {
		addForeignKeyConstraint(baseColumnNames: "ut_transforms_fk", baseTableName: "user_transforms", baseTableSchemaName: "KBPlus", constraintName: "FKE8A3AF7BD8A576A1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "tr_id", referencedTableName: "transforms", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508147394114-338") {
		addForeignKeyConstraint(baseColumnNames: "ut_user_fk", baseTableName: "user_transforms", baseTableSchemaName: "KBPlus", constraintName: "FKE8A3AF7B988474CD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "KBPlus", referencesUniqueColumn: "false")
	}
}
