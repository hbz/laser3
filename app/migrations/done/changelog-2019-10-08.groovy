databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1570513567938-1") {
		addColumn(schemaName: "public", tableName: "access_point_data") {
			column(name: "apd_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-2") {
		addColumn(schemaName: "public", tableName: "access_point_data") {
			column(name: "apd_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-3") {
		addColumn(schemaName: "public", tableName: "address") {
			column(name: "adr_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-4") {
		addColumn(schemaName: "public", tableName: "address") {
			column(name: "adr_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-5") {
		addColumn(schemaName: "public", tableName: "budget_code") {
			column(name: "bc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-6") {
		addColumn(schemaName: "public", tableName: "budget_code") {
			column(name: "bc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-7") {
		addColumn(schemaName: "public", tableName: "change_notification_queue_item") {
			column(name: "cnqi_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-8") {
		addColumn(schemaName: "public", tableName: "change_notification_queue_item") {
			column(name: "cnqi_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-9") {
		addColumn(schemaName: "public", tableName: "cluster") {
			column(name: "cl_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-10") {
		addColumn(schemaName: "public", tableName: "cluster") {
			column(name: "cl_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-11") {
		addColumn(schemaName: "public", tableName: "combo") {
			column(name: "combo_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-12") {
		addColumn(schemaName: "public", tableName: "combo") {
			column(name: "combo_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-13") {
		addColumn(schemaName: "public", tableName: "contact") {
			column(name: "ct_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-14") {
		addColumn(schemaName: "public", tableName: "contact") {
			column(name: "ct_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-15") {
		addColumn(schemaName: "public", tableName: "content_item") {
			column(name: "ci_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-16") {
		addColumn(schemaName: "public", tableName: "content_item") {
			column(name: "ci_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-17") {
		addColumn(schemaName: "public", tableName: "core_assertion") {
			column(name: "ca_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-18") {
		addColumn(schemaName: "public", tableName: "core_assertion") {
			column(name: "ca_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-19") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-20") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-21") {
		addColumn(schemaName: "public", tableName: "elasticsearch_source") {
			column(name: "ess_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-22") {
		addColumn(schemaName: "public", tableName: "elasticsearch_source") {
			column(name: "ess_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-23") {
		addColumn(schemaName: "public", tableName: "event_log") {
			column(name: "el_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-24") {
		addColumn(schemaName: "public", tableName: "event_log") {
			column(name: "el_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-25") {
		addColumn(schemaName: "public", tableName: "fact") {
			column(name: "fact_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-26") {
		addColumn(schemaName: "public", tableName: "fact") {
			column(name: "fact_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-27") {
		addColumn(schemaName: "public", tableName: "ftcontrol") {
			column(name: "date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-28") {
		addColumn(schemaName: "public", tableName: "ftcontrol") {
			column(name: "last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-29") {
		addColumn(schemaName: "public", tableName: "global_record_info") {
			column(name: "gri_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-30") {
		addColumn(schemaName: "public", tableName: "global_record_info") {
			column(name: "gri_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-31") {
		addColumn(schemaName: "public", tableName: "global_record_source") {
			column(name: "grs_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-32") {
		addColumn(schemaName: "public", tableName: "global_record_source") {
			column(name: "grs_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-33") {
		addColumn(schemaName: "public", tableName: "global_record_tracker") {
			column(name: "grt_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-34") {
		addColumn(schemaName: "public", tableName: "global_record_tracker") {
			column(name: "grt_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-35") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-36") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-37") {
		addColumn(schemaName: "public", tableName: "identifier_group") {
			column(name: "ig_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-38") {
		addColumn(schemaName: "public", tableName: "identifier_group") {
			column(name: "ig_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-39") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-40") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-41") {
		addColumn(schemaName: "public", tableName: "identifier_occurrence") {
			column(name: "io_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-42") {
		addColumn(schemaName: "public", tableName: "identifier_occurrence") {
			column(name: "io_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-43") {
		addColumn(schemaName: "public", tableName: "invoice") {
			column(name: "inv_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-44") {
		addColumn(schemaName: "public", tableName: "invoice") {
			column(name: "inv_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-45") {
		addColumn(schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-46") {
		addColumn(schemaName: "public", tableName: "issue_entitlement") {
			column(name: "ie_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-47") {
		addColumn(schemaName: "public", tableName: "license_custom_property") {
			column(name: "lcp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-48") {
		addColumn(schemaName: "public", tableName: "license_custom_property") {
			column(name: "lcp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-49") {
		addColumn(schemaName: "public", tableName: "license_private_property") {
			column(name: "lpp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-50") {
		addColumn(schemaName: "public", tableName: "license_private_property") {
			column(name: "lpp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-51") {
		addColumn(schemaName: "public", tableName: "links") {
			column(name: "l_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-52") {
		addColumn(schemaName: "public", tableName: "onixpl_license") {
			column(name: "opl_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-53") {
		addColumn(schemaName: "public", tableName: "onixpl_license") {
			column(name: "opl_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-54") {
		addColumn(schemaName: "public", tableName: "ordering") {
			column(name: "ord_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-55") {
		addColumn(schemaName: "public", tableName: "ordering") {
			column(name: "ord_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-56") {
		addColumn(schemaName: "public", tableName: "org_custom_property") {
			column(name: "ocp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-57") {
		addColumn(schemaName: "public", tableName: "org_custom_property") {
			column(name: "ocp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-58") {
		addColumn(schemaName: "public", tableName: "org_private_property") {
			column(name: "opp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-59") {
		addColumn(schemaName: "public", tableName: "org_private_property") {
			column(name: "opp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-60") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-61") {
		addColumn(schemaName: "public", tableName: "org_role") {
			column(name: "or_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-62") {
		addColumn(schemaName: "public", tableName: "org_settings") {
			column(name: "os_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-63") {
		addColumn(schemaName: "public", tableName: "org_settings") {
			column(name: "os_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-64") {
		addColumn(schemaName: "public", tableName: "org_title_stats") {
			column(name: "ots_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-65") {
		addColumn(schemaName: "public", tableName: "org_title_stats") {
			column(name: "ots_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-66") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-67") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-68") {
		addColumn(schemaName: "public", tableName: "person") {
			column(name: "prs_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-69") {
		addColumn(schemaName: "public", tableName: "person") {
			column(name: "prs_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-70") {
		addColumn(schemaName: "public", tableName: "person_private_property") {
			column(name: "ppp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-71") {
		addColumn(schemaName: "public", tableName: "person_private_property") {
			column(name: "ppp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-72") {
		addColumn(schemaName: "public", tableName: "person_role") {
			column(name: "pr_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-73") {
		addColumn(schemaName: "public", tableName: "person_role") {
			column(name: "pr_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-74") {
		addColumn(schemaName: "public", tableName: "platformtipp") {
			column(name: "ptipp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-75") {
		addColumn(schemaName: "public", tableName: "platformtipp") {
			column(name: "ptipp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-76") {
		addColumn(schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-77") {
		addColumn(schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-78") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-79") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-80") {
		addColumn(schemaName: "public", tableName: "reminder") {
			column(name: "date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-81") {
		addColumn(schemaName: "public", tableName: "setting") {
			column(name: "set_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-82") {
		addColumn(schemaName: "public", tableName: "setting") {
			column(name: "set_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-83") {
		addColumn(schemaName: "public", tableName: "site_page") {
			column(name: "date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-84") {
		addColumn(schemaName: "public", tableName: "site_page") {
			column(name: "last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-85") {
		addColumn(schemaName: "public", tableName: "subscription_custom_property") {
			column(name: "scp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-86") {
		addColumn(schemaName: "public", tableName: "subscription_custom_property") {
			column(name: "scp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-87") {
		addColumn(schemaName: "public", tableName: "subscription_package") {
			column(name: "sp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-88") {
		addColumn(schemaName: "public", tableName: "subscription_package") {
			column(name: "sp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-89") {
		addColumn(schemaName: "public", tableName: "subscription_private_property") {
			column(name: "spp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-90") {
		addColumn(schemaName: "public", tableName: "subscription_private_property") {
			column(name: "spp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-91") {
		addColumn(schemaName: "public", tableName: "system_object") {
			column(name: "sys_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-92") {
		addColumn(schemaName: "public", tableName: "system_object") {
			column(name: "sys_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-93") {
		addColumn(schemaName: "public", tableName: "task") {
			column(name: "tsk_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-94") {
		addColumn(schemaName: "public", tableName: "task") {
			column(name: "tsk_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-95") {
		addColumn(schemaName: "public", tableName: "title_history_event") {
			column(name: "the_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-96") {
		addColumn(schemaName: "public", tableName: "title_history_event") {
			column(name: "the_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-97") {
		addColumn(schemaName: "public", tableName: "title_history_event_participant") {
			column(name: "thep_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-98") {
		addColumn(schemaName: "public", tableName: "title_history_event_participant") {
			column(name: "thep_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-99") {
		addColumn(schemaName: "public", tableName: "title_instance_package_platform") {
			column(name: "tipp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-100") {
		addColumn(schemaName: "public", tableName: "title_instance_package_platform") {
			column(name: "tipp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-101") {
		addColumn(schemaName: "public", tableName: "title_institution_provider") {
			column(name: "tttnp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-102") {
		addColumn(schemaName: "public", tableName: "title_institution_provider") {
			column(name: "tttnp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-103") {
		addColumn(schemaName: "public", tableName: "transformer") {
			column(name: "tfmr_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-104") {
		addColumn(schemaName: "public", tableName: "transformer") {
			column(name: "tfmr_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-105") {
		addColumn(schemaName: "public", tableName: "transforms") {
			column(name: "tr_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-106") {
		addColumn(schemaName: "public", tableName: "transforms") {
			column(name: "tr_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-107") {
		addColumn(schemaName: "public", tableName: "user_settings") {
			column(name: "us_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-108") {
		addColumn(schemaName: "public", tableName: "user_settings") {
			column(name: "us_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-109") {
		addColumn(schemaName: "public", tableName: "user_transforms") {
			column(name: "ut_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570513567938-110") {
		addColumn(schemaName: "public", tableName: "user_transforms") {
			column(name: "ut_last_updated", type: "timestamp")
		}
	}
}
