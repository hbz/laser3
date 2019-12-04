databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1575274311748-1") {
		grailsChange {
			change {
				sql.execute("""
alter table cluster alter column cl_date_created type timestamp using cl_date_created::timestamp;
update cluster set cl_date_created = (cl_date_created + '1 hour'::interval);
alter table cluster alter column cl_last_updated type timestamp using cl_last_updated::timestamp;
update cluster set cl_last_updated = (cl_last_updated + '1 hour'::interval);
alter table access_point_data alter column apd_date_created type timestamp using apd_date_created::timestamp;
update access_point_data set apd_date_created = (apd_date_created + '1 hour'::interval);
alter table access_point_data alter column apd_last_updated type timestamp using apd_last_updated::timestamp;
update access_point_data set apd_last_updated = (apd_last_updated + '1 hour'::interval);
alter table change_notification_queue_item alter column cnqi_date_created type timestamp using cnqi_date_created::timestamp;
update change_notification_queue_item set cnqi_date_created = (cnqi_date_created + '1 hour'::interval);
alter table change_notification_queue_item alter column cnqi_last_updated type timestamp using cnqi_last_updated::timestamp;
update change_notification_queue_item set cnqi_last_updated = (cnqi_last_updated + '1 hour'::interval);
alter table combo alter column combo_date_created type timestamp using combo_date_created::timestamp;
update combo set combo_date_created = (combo_date_created + '1 hour'::interval);
alter table combo alter column combo_last_updated type timestamp using combo_last_updated::timestamp;
update combo set combo_last_updated = (combo_last_updated + '1 hour'::interval);
alter table contact alter column ct_date_created type timestamp using ct_date_created::timestamp;
update contact set ct_date_created = (ct_date_created + '1 hour'::interval);
alter table contact alter column ct_last_updated type timestamp using ct_last_updated::timestamp;
alter table org_custom_property alter column ocp_last_updated type timestamp using ocp_last_updated::timestamp;
update org_custom_property set ocp_last_updated = (ocp_last_updated + '1 hour'::interval);
alter table mail_template alter column mt_date_created type timestamp using mt_date_created::timestamp;
update mail_template set mt_date_created = (mt_date_created + '1 hour'::interval);
alter table mail_template alter column mt_last_updated type timestamp using mt_last_updated::timestamp;
update mail_template set mt_last_updated = (mt_last_updated + '1 hour'::interval);
alter table org_private_property alter column opp_date_created type timestamp using opp_date_created::timestamp;
update org_private_property set opp_date_created = (opp_date_created + '1 hour'::interval);
alter table org_private_property alter column opp_last_updated type timestamp using opp_last_updated::timestamp;
update org_private_property set opp_last_updated = (opp_last_updated + '1 hour'::interval);
alter table ordering alter column ord_date_created type timestamp using ord_date_created::timestamp;
update ordering set ord_date_created = (ord_date_created + '1 hour'::interval);
alter table ordering alter column ord_last_updated type timestamp using ord_last_updated::timestamp;
update ordering set ord_last_updated = (ord_last_updated + '1 hour'::interval);
alter table links alter column last_updated type timestamp using last_updated::timestamp;
update links set last_updated = (last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-2") {
		grailsChange {
			change {
				sql.execute("""
update contact set ct_last_updated = (ct_last_updated + '1 hour'::interval);
alter table address alter column adr_date_created type timestamp using adr_date_created::timestamp;
update address set adr_date_created = (adr_date_created + '1 hour'::interval);
alter table address alter column adr_last_updated type timestamp using adr_last_updated::timestamp;
update address set adr_last_updated = (adr_last_updated + '1 hour'::interval);
alter table content_item alter column ci_date_created type timestamp using ci_date_created::timestamp;
update content_item set ci_date_created = (ci_date_created + '1 hour'::interval);
alter table content_item alter column ci_last_updated type timestamp using ci_last_updated::timestamp;
update content_item set ci_last_updated = (ci_last_updated + '1 hour'::interval);
alter table core_assertion alter column ca_date_created type timestamp using ca_date_created::timestamp;
update core_assertion set ca_date_created = (ca_date_created + '1 hour'::interval);
alter table core_assertion alter column ca_last_updated type timestamp using ca_last_updated::timestamp;
update core_assertion set ca_last_updated = (ca_last_updated + '1 hour'::interval);
alter table cost_item_element_configuration alter column date_created type timestamp using date_created::timestamp;
update cost_item_element_configuration set date_created = (date_created + '1 hour'::interval);
alter table cost_item_element_configuration alter column last_updated type timestamp using last_updated::timestamp;
update cost_item_element_configuration set last_updated = (last_updated + '1 hour'::interval);
alter table databasechangeloglock alter column lockgranted type timestamp using lockgranted::timestamp;
update databasechangeloglock set lockgranted = (lockgranted + '1 hour'::interval);
alter table databasechangelog alter column dateexecuted type timestamp using dateexecuted::timestamp;
update databasechangelog set dateexecuted = (dateexecuted + '1 hour'::interval);
alter table links alter column l_date_created type timestamp using l_date_created::timestamp;
update links set l_date_created = (l_date_created + '1 hour'::interval);
alter table org_title_stats alter column ots_date_created type timestamp using ots_date_created::timestamp;
update org_title_stats set ots_date_created = (ots_date_created + '1 hour'::interval);
alter table org_title_stats alter column ots_last_updated type timestamp using ots_last_updated::timestamp;
update org_title_stats set ots_last_updated = (ots_last_updated + '1 hour'::interval);
alter table org_settings alter column os_date_created type timestamp using os_date_created::timestamp;
update org_settings set os_date_created = (os_date_created + '1 hour'::interval);
alter table org_settings alter column os_last_updated type timestamp using os_last_updated::timestamp;
update org_settings set os_last_updated = (os_last_updated + '1 hour'::interval);
alter table org_role alter column or_date_created type timestamp using or_date_created::timestamp;
update org_role set or_date_created = (or_date_created + '1 hour'::interval);
alter table org_role alter column or_last_updated type timestamp using or_last_updated::timestamp;
update org_role set or_last_updated = (or_last_updated + '1 hour'::interval);
alter table pending_change alter column pc_date_created type timestamp using pc_date_created::timestamp;
update pending_change set pc_date_created = (pc_date_created + '1 hour'::interval);
alter table pending_change alter column pc_last_updated type timestamp using pc_last_updated::timestamp;
update pending_change set pc_last_updated = (pc_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-3") {
		grailsChange {
			change {
				sql.execute("""
alter table elasticsearch_source alter column ess_date_created type timestamp using ess_date_created::timestamp;
update elasticsearch_source set ess_date_created = (ess_date_created + '1 hour'::interval);
alter table elasticsearch_source alter column ess_last_updated type timestamp using ess_last_updated::timestamp;
update elasticsearch_source set ess_last_updated = (ess_last_updated + '1 hour'::interval);
alter table ftcontrol alter column date_created type timestamp using date_created::timestamp;
update ftcontrol set date_created = (date_created + '1 hour'::interval);
alter table ftcontrol alter column last_updated type timestamp using last_updated::timestamp;
update ftcontrol set last_updated = (last_updated + '1 hour'::interval);
alter table global_record_source alter column grs_date_created type timestamp using grs_date_created::timestamp;
update global_record_source set grs_date_created = (grs_date_created + '1 hour'::interval);
alter table global_record_source alter column grs_last_updated type timestamp using grs_last_updated::timestamp;
update global_record_source set grs_last_updated = (grs_last_updated + '1 hour'::interval);
alter table fact alter column fact_date_created type timestamp using fact_date_created::timestamp;
update fact set fact_date_created = (fact_date_created + '1 hour'::interval);
alter table fact alter column fact_last_updated type timestamp using fact_last_updated::timestamp;
update fact set fact_last_updated = (fact_last_updated + '1 hour'::interval);
alter table identifier_backup alter column id_date_created type timestamp using id_date_created::timestamp;
update identifier_backup set id_date_created = (id_date_created + '1 hour'::interval);
alter table identifier_backup alter column id_last_updated type timestamp using id_last_updated::timestamp;
update identifier_backup set id_last_updated = (id_last_updated + '1 hour'::interval);
alter table issue_entitlement alter column ie_date_created type timestamp using ie_date_created::timestamp;
update issue_entitlement set ie_date_created = (ie_date_created + '1 hour'::interval);
alter table price_item alter column pi_price_date type timestamp using pi_price_date::timestamp;
update price_item set pi_price_date = (pi_price_date + '1 hour'::interval);
alter table person_private_property alter column ppp_date_created type timestamp using ppp_date_created::timestamp;
update person_private_property set ppp_date_created = (ppp_date_created + '1 hour'::interval);
alter table person_private_property alter column ppp_last_updated type timestamp using ppp_last_updated::timestamp;
update person_private_property set ppp_last_updated = (ppp_last_updated + '1 hour'::interval);
alter table refdata_value alter column rdv_date_created type timestamp using rdv_date_created::timestamp;
update refdata_value set rdv_date_created = (rdv_date_created + '1 hour'::interval);
alter table refdata_value alter column rdv_last_updated type timestamp using rdv_last_updated::timestamp;
update refdata_value set rdv_last_updated = (rdv_last_updated + '1 hour'::interval);
alter table setting alter column set_date_created type timestamp using set_date_created::timestamp;
update setting set set_date_created = (set_date_created + '1 hour'::interval);
alter table setting alter column set_last_updated type timestamp using set_last_updated::timestamp;
update setting set set_last_updated = (set_last_updated + '1 hour'::interval);
alter table subscription_package alter column sp_finish_date type timestamp using sp_finish_date::timestamp;
update subscription_package set sp_finish_date = (sp_finish_date + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-4") {
		grailsChange {
			change {
				sql.execute("""
alter table issue_entitlement alter column ie_last_updated type timestamp using ie_last_updated::timestamp;
update issue_entitlement set ie_last_updated = (ie_last_updated + '1 hour'::interval);
alter table invoice alter column inv_date_created type timestamp using inv_date_created::timestamp;
update invoice set inv_date_created = (inv_date_created + '1 hour'::interval);
alter table invoice alter column inv_last_updated type timestamp using inv_last_updated::timestamp;
update invoice set inv_last_updated = (inv_last_updated + '1 hour'::interval);
alter table identifier_namespace_backup alter column idns_date_created type timestamp using idns_date_created::timestamp;
update identifier_namespace_backup set idns_date_created = (idns_date_created + '1 hour'::interval);
alter table identifier_namespace_backup alter column idns_last_updated type timestamp using idns_last_updated::timestamp;
update identifier_namespace_backup set idns_last_updated = (idns_last_updated + '1 hour'::interval);
alter table identifier_occurrence_backup alter column io_date_created type timestamp using io_date_created::timestamp;
update identifier_occurrence_backup set io_date_created = (io_date_created + '1 hour'::interval);
alter table identifier_occurrence_backup alter column io_last_updated type timestamp using io_last_updated::timestamp;
update identifier_occurrence_backup set io_last_updated = (io_last_updated + '1 hour'::interval);
alter table identifier_namespace alter column idns_date_created type timestamp using idns_date_created::timestamp;
update identifier_namespace set idns_date_created = (idns_date_created + '1 hour'::interval);
alter table identifier_namespace alter column idns_last_updated type timestamp using idns_last_updated::timestamp;
update identifier_namespace set idns_last_updated = (idns_last_updated + '1 hour'::interval);
alter table identifier_group alter column ig_date_created type timestamp using ig_date_created::timestamp;
update identifier_group set ig_date_created = (ig_date_created + '1 hour'::interval);
alter table identifier_group alter column ig_last_updated type timestamp using ig_last_updated::timestamp;
update identifier_group set ig_last_updated = (ig_last_updated + '1 hour'::interval);
alter table subscription_package alter column sp_date_created type timestamp using sp_date_created::timestamp;
update subscription_package set sp_date_created = (sp_date_created + '1 hour'::interval);
alter table subscription_package alter column sp_last_updated type timestamp using sp_last_updated::timestamp;
update subscription_package set sp_last_updated = (sp_last_updated + '1 hour'::interval);
alter table stats_triple_cursor alter column avail_from type timestamp using avail_from::timestamp;
update stats_triple_cursor set avail_from = (avail_from + '1 hour'::interval);
alter table stats_triple_cursor alter column avail_to type timestamp using avail_to::timestamp;
update stats_triple_cursor set avail_to = (avail_to + '1 hour'::interval);
alter table refdata_category alter column rdc_date_created type timestamp using rdc_date_created::timestamp;
update refdata_category set rdc_date_created = (rdc_date_created + '1 hour'::interval);
alter table refdata_category alter column rdc_last_updated type timestamp using rdc_last_updated::timestamp;
update refdata_category set rdc_last_updated = (rdc_last_updated + '1 hour'::interval);
alter table reminder alter column date_created type timestamp using date_created::timestamp;
update reminder set date_created = (date_created + '1 hour'::interval);
alter table subscription_custom_property alter column scp_date_created type timestamp using scp_date_created::timestamp;
update subscription_custom_property set scp_date_created = (scp_date_created + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-5") {
		grailsChange {
			change {
				sql.execute("""
alter table license_custom_property alter column lcp_date_created type timestamp using lcp_date_created::timestamp;
update license_custom_property set lcp_date_created = (lcp_date_created + '1 hour'::interval);
alter table license_custom_property alter column lcp_last_updated type timestamp using lcp_last_updated::timestamp;
update license_custom_property set lcp_last_updated = (lcp_last_updated + '1 hour'::interval);
alter table identifier alter column id_date_created type timestamp using id_date_created::timestamp;
update identifier set id_date_created = (id_date_created + '1 hour'::interval);
alter table identifier alter column id_last_updated type timestamp using id_last_updated::timestamp;
update identifier set id_last_updated = (id_last_updated + '1 hour'::interval);
alter table license_private_property alter column lpp_date_created type timestamp using lpp_date_created::timestamp;
update license_private_property set lpp_date_created = (lpp_date_created + '1 hour'::interval);
alter table license_private_property alter column lpp_last_updated type timestamp using lpp_last_updated::timestamp;
update license_private_property set lpp_last_updated = (lpp_last_updated + '1 hour'::interval);
alter table package alter column pkg_list_verified_date type timestamp using pkg_list_verified_date::timestamp;
update package set pkg_list_verified_date = (pkg_list_verified_date + '1 hour'::interval);
alter table onixpl_license alter column opl_date_created type timestamp using opl_date_created::timestamp;
update onixpl_license set opl_date_created = (opl_date_created + '1 hour'::interval);
alter table onixpl_license alter column opl_last_updated type timestamp using opl_last_updated::timestamp;
update onixpl_license set opl_last_updated = (opl_last_updated + '1 hour'::interval);
alter table reader_number alter column num_due_date type timestamp using num_due_date::timestamp;
update reader_number set num_due_date = (num_due_date + '1 hour'::interval);
alter table org_custom_property alter column ocp_date_created type timestamp using ocp_date_created::timestamp;
update org_custom_property set ocp_date_created = (ocp_date_created + '1 hour'::interval);
alter table subscription_custom_property alter column scp_last_updated type timestamp using scp_last_updated::timestamp;
update subscription_custom_property set scp_last_updated = (scp_last_updated + '1 hour'::interval);
alter table survey_property alter column surpro_date_created type timestamp using surpro_date_created::timestamp;
update survey_property set surpro_date_created = (surpro_date_created + '1 hour'::interval);
alter table survey_property alter column surpro_last_updated type timestamp using surpro_last_updated::timestamp;
update survey_property set surpro_last_updated = (surpro_last_updated + '1 hour'::interval);
alter table survey_result alter column date_value type timestamp using date_value::timestamp;
update survey_result set date_value = (date_value + '1 hour'::interval);
alter table survey_result alter column surre_date_created type timestamp using surre_date_created::timestamp;
update survey_result set surre_date_created = (surre_date_created + '1 hour'::interval);
alter table survey_result alter column surre_end_date type timestamp using surre_end_date::timestamp;
update survey_result set surre_end_date = (surre_end_date + '1 hour'::interval);
alter table survey_result alter column surre_finish_date type timestamp using surre_finish_date::timestamp;
update survey_result set surre_finish_date = (surre_finish_date + '1 hour'::interval);
alter table survey_result alter column surre_last_updated type timestamp using surre_last_updated::timestamp;
update survey_result set surre_last_updated = (surre_last_updated + '1 hour'::interval);
alter table survey_result alter column surre_start_date type timestamp using surre_start_date::timestamp;
update survey_result set surre_start_date = (surre_start_date + '1 hour'::interval);
alter table system_object alter column sys_date_created type timestamp using sys_date_created::timestamp;
update system_object set sys_date_created = (sys_date_created + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-6") {
		grailsChange {
			change {
				sql.execute("""			
alter table system_object alter column sys_last_updated type timestamp using sys_last_updated::timestamp;
update system_object set sys_last_updated = (sys_last_updated + '1 hour'::interval);
alter table survey_org alter column surorg_date_created type timestamp using surorg_date_created::timestamp;
update survey_org set surorg_date_created = (surorg_date_created + '1 hour'::interval);
alter table survey_org alter column surorg_last_updated type timestamp using surorg_last_updated::timestamp;
update survey_org set surorg_last_updated = (surorg_last_updated + '1 hour'::interval);
alter table survey_org alter column surorg_finish_date type timestamp using surorg_finish_date::timestamp;
update survey_org set surorg_finish_date = (surorg_finish_date + '1 hour'::interval);
alter table system_event alter column se_created type timestamp using se_created::timestamp;
update system_event set se_created = (se_created + '1 hour'::interval);
alter table survey_info alter column surin_date_created type timestamp using surin_date_created::timestamp;
update survey_info set surin_date_created = (surin_date_created + '1 hour'::interval);
alter table survey_info alter column surin_end_date type timestamp using surin_end_date::timestamp;
update survey_info set surin_end_date = (surin_end_date + '1 hour'::interval);
alter table survey_info alter column surin_last_updated type timestamp using surin_last_updated::timestamp;
update survey_info set surin_last_updated = (surin_last_updated + '1 hour'::interval);
alter table survey_info alter column surin_start_date type timestamp using surin_start_date::timestamp;
update survey_info set surin_start_date = (surin_start_date + '1 hour'::interval);
alter table survey_config_properties alter column surconpro_date_created type timestamp using surconpro_date_created::timestamp;
update survey_config_properties set surconpro_date_created = (surconpro_date_created + '1 hour'::interval);
alter table survey_config_properties alter column surconpro_last_updated type timestamp using surconpro_last_updated::timestamp;
update survey_config_properties set surconpro_last_updated = (surconpro_last_updated + '1 hour'::interval);
alter table title_instance_package_platform alter column tipp_date_created type timestamp using tipp_date_created::timestamp;
update title_instance_package_platform set tipp_date_created = (tipp_date_created + '1 hour'::interval);
alter table title_instance_package_platform alter column tipp_last_updated type timestamp using tipp_last_updated::timestamp;
update title_instance_package_platform set tipp_last_updated = (tipp_last_updated + '1 hour'::interval);
alter table title_history_event alter column the_date_created type timestamp using the_date_created::timestamp;
update title_history_event set the_date_created = (the_date_created + '1 hour'::interval);
alter table title_history_event alter column the_last_updated type timestamp using the_last_updated::timestamp;
update title_history_event set the_last_updated = (the_last_updated + '1 hour'::interval);
alter table transformer alter column tfmr_date_created type timestamp using tfmr_date_created::timestamp;
update transformer set tfmr_date_created = (tfmr_date_created + '1 hour'::interval);
alter table transformer alter column tfmr_last_updated type timestamp using tfmr_last_updated::timestamp;
update transformer set tfmr_last_updated = (tfmr_last_updated + '1 hour'::interval);
alter table transforms alter column tr_date_created type timestamp using tr_date_created::timestamp;
update transforms set tr_date_created = (tr_date_created + '1 hour'::interval);
alter table transforms alter column tr_last_updated type timestamp using tr_last_updated::timestamp;
update transforms set tr_last_updated = (tr_last_updated + '1 hour'::interval);
alter table user_settings alter column us_date_created type timestamp using us_date_created::timestamp;
update user_settings set us_date_created = (us_date_created + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-7") {
		grailsChange {
			change {
				sql.execute("""	
alter table user_settings alter column us_last_updated type timestamp using us_last_updated::timestamp;
update user_settings set us_last_updated = (us_last_updated + '1 hour'::interval);
alter table title_history_event_participant alter column thep_date_created type timestamp using thep_date_created::timestamp;
update title_history_event_participant set thep_date_created = (thep_date_created + '1 hour'::interval);
alter table title_history_event_participant alter column thep_last_updated type timestamp using thep_last_updated::timestamp;
update title_history_event_participant set thep_last_updated = (thep_last_updated + '1 hour'::interval);
alter table user_transforms alter column ut_date_created type timestamp using ut_date_created::timestamp;
update user_transforms set ut_date_created = (ut_date_created + '1 hour'::interval);
alter table user_transforms alter column ut_last_updated type timestamp using ut_last_updated::timestamp;
update user_transforms set ut_last_updated = (ut_last_updated + '1 hour'::interval);
alter table tippcoverage alter column tc_end_date type timestamp using tc_end_date::timestamp;
update tippcoverage set tc_end_date = (tc_end_date + '1 hour'::interval);
alter table tippcoverage alter column tc_start_date type timestamp using tc_start_date::timestamp;
update tippcoverage set tc_start_date = (tc_start_date + '1 hour'::interval);
alter table subscription_private_property alter column spp_date_created type timestamp using spp_date_created::timestamp;
update subscription_private_property set spp_date_created = (spp_date_created + '1 hour'::interval);
alter table subscription_private_property alter column spp_last_updated type timestamp using spp_last_updated::timestamp;
update subscription_private_property set spp_last_updated = (spp_last_updated + '1 hour'::interval);
alter table doc_context alter column dc_date_created type timestamp using dc_date_created::timestamp;
update doc_context set dc_date_created = (dc_date_created + '1 hour'::interval);
alter table doc_context alter column dc_last_updated type timestamp using dc_last_updated::timestamp;
update doc_context set dc_last_updated = (dc_last_updated + '1 hour'::interval);
alter table "user" alter column date_created type timestamp using date_created::timestamp;
update "user" set date_created = (date_created + '1 hour'::interval);
alter table "user" alter column last_updated type timestamp using last_updated::timestamp;
update "user" set last_updated = (last_updated + '1 hour'::interval);
alter table task alter column tsk_system_create_date type timestamp using tsk_system_create_date::timestamp;
update task set tsk_system_create_date = (tsk_system_create_date + '1 hour'::interval);
alter table task alter column tsk_date_created type timestamp using tsk_date_created::timestamp;
update task set tsk_date_created = (tsk_date_created + '1 hour'::interval);
alter table task alter column tsk_last_updated type timestamp using tsk_last_updated::timestamp;
update task set tsk_last_updated = (tsk_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575274311748-8") {
		grailsChange {
			change {
				sql.execute("""
alter table platform_custom_property alter column date_value type timestamp using date_value::timestamp;
update platform_custom_property set date_value = (date_value + '1 hour'::interval);
alter table issue_entitlement_coverage alter column ic_end_date type timestamp using ic_end_date::timestamp;
update issue_entitlement_coverage set ic_end_date = (ic_end_date + '1 hour'::interval);
alter table issue_entitlement_coverage alter column ic_start_date type timestamp using ic_start_date::timestamp;
update issue_entitlement_coverage set ic_start_date = (ic_start_date + '1 hour'::interval);
alter table budget_code alter column bc_date_created type timestamp using bc_date_created::timestamp;
update budget_code set bc_date_created = (bc_date_created + '1 hour'::interval);
alter table budget_code alter column bc_last_updated type timestamp using bc_last_updated::timestamp;
update budget_code set bc_last_updated = (bc_last_updated + '1 hour'::interval);
alter table global_record_tracker alter column grt_date_created type timestamp using grt_date_created::timestamp;
update global_record_tracker set grt_date_created = (grt_date_created + '1 hour'::interval);
alter table global_record_tracker alter column grt_last_updated type timestamp using grt_last_updated::timestamp;
update global_record_tracker set grt_last_updated = (grt_last_updated + '1 hour'::interval);
alter table global_record_info alter column gri_date_created type timestamp using gri_date_created::timestamp;
update global_record_info set gri_date_created = (gri_date_created + '1 hour'::interval);
alter table global_record_info alter column gri_last_updated type timestamp using gri_last_updated::timestamp;
update global_record_info set gri_last_updated = (gri_last_updated + '1 hour'::interval);
alter table title_institution_provider alter column tttnp_date_created type timestamp using tttnp_date_created::timestamp;
update title_institution_provider set tttnp_date_created = (tttnp_date_created + '1 hour'::interval);
alter table title_institution_provider alter column tttnp_last_updated type timestamp using tttnp_last_updated::timestamp;
update title_institution_provider set tttnp_last_updated = (tttnp_last_updated + '1 hour'::interval);
alter table survey_config alter column surconf_date_created type timestamp using surconf_date_created::timestamp;
update survey_config set surconf_date_created = (surconf_date_created + '1 hour'::interval);
alter table survey_config alter column surconf_last_updated type timestamp using surconf_last_updated::timestamp;
update survey_config set surconf_last_updated = (surconf_last_updated + '1 hour'::interval);
alter table survey_config alter column surconf_scheduled_enddate type timestamp using surconf_scheduled_enddate::timestamp;
update survey_config set surconf_scheduled_enddate = (surconf_scheduled_enddate + '1 hour'::interval);
alter table survey_config alter column surconf_scheduled_startdate type timestamp using surconf_scheduled_startdate::timestamp;
update survey_config set surconf_scheduled_startdate = (surconf_scheduled_startdate + '1 hour'::interval);
alter table dashboard_due_date alter column das_last_updated type timestamp using das_last_updated::timestamp;
update dashboard_due_date set das_last_updated = (das_last_updated + '1 hour'::interval);
alter table person_role alter column pr_date_created type timestamp using pr_date_created::timestamp;
update person_role set pr_date_created = (pr_date_created + '1 hour'::interval);
alter table person_role alter column pr_last_updated type timestamp using pr_last_updated::timestamp;
update person_role set pr_last_updated = (pr_last_updated + '1 hour'::interval);
alter table person alter column prs_date_created type timestamp using prs_date_created::timestamp;
update person set prs_date_created = (prs_date_created + '1 hour'::interval);
alter table person alter column prs_last_updated type timestamp using prs_last_updated::timestamp;
update person set prs_last_updated = (prs_last_updated + '1 hour'::interval);
alter table platformtipp alter column ptipp_date_created type timestamp using ptipp_date_created::timestamp;
update platformtipp set ptipp_date_created = (ptipp_date_created + '1 hour'::interval);
alter table platformtipp alter column ptipp_last_updated type timestamp using ptipp_last_updated::timestamp;
update platformtipp set ptipp_last_updated = (ptipp_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}
}
