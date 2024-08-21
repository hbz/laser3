package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1724138532317-1") {
        createIndex(indexName: "dc_link_idx", tableName: "doc_context") {
            column(name: "dc_link_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-2") {
        createIndex(indexName: "dc_share_conf_idx", tableName: "doc_context") {
            column(name: "dc_share_conf_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-3") {
        createIndex(indexName: "dc_shared_from_idx", tableName: "doc_context") {
            column(name: "dc_shared_from_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-4") {
        createIndex(indexName: "dc_status_idx", tableName: "doc_context") {
            column(name: "dc_status_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-5") {
        createIndex(indexName: "dc_survey_config_idx", tableName: "doc_context") {
            column(name: "dc_survey_config_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-6") {
        createIndex(indexName: "dc_target_org_idx", tableName: "doc_context") {
            column(name: "dc_target_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-7") {
        createIndex(indexName: "l_link_type_idx", tableName: "links") {
            column(name: "l_link_type_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-8") {
        createIndex(indexName: "l_owner_idx", tableName: "links") {
            column(name: "l_owner_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-9") {
        createIndex(indexName: "fact_type_rdv_idx", tableName: "fact") {
            column(name: "fact_type_rdv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-10") {
        createIndex(indexName: "sp_ref_value_idx", tableName: "subscription_property") {
            column(name: "sp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-11") {
        createIndex(indexName: "surre_ref_value_idx", tableName: "survey_result") {
            column(name: "surre_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-12") {
        createIndex(indexName: "ci_billing_currency_idx", tableName: "cost_item") {
            column(name: "ci_billing_currency_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-13") {
        createIndex(indexName: "ci_cat_idx", tableName: "cost_item") {
            column(name: "ci_cat_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-14") {
        createIndex(indexName: "ci_element_configuration_idx", tableName: "cost_item") {
            column(name: "ci_element_configuration_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-15") {
        createIndex(indexName: "ci_element_idx", tableName: "cost_item") {
            column(name: "ci_element_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-16") {
        createIndex(indexName: "ci_ie_group_idx", tableName: "cost_item") {
            column(name: "ci_ie_group_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-17") {
        createIndex(indexName: "ci_inv_idx", tableName: "cost_item") {
            column(name: "ci_inv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-18") {
        createIndex(indexName: "ci_ord_idx", tableName: "cost_item") {
            column(name: "ci_ord_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-19") {
        createIndex(indexName: "ci_status_idx", tableName: "cost_item") {
            column(name: "ci_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-20") {
        createIndex(indexName: "ci_surorg_idx", tableName: "cost_item") {
            column(name: "ci_surorg_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-21") {
        createIndex(indexName: "ci_type_idx", tableName: "cost_item") {
            column(name: "ci_type_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-22") {
        createIndex(indexName: "doc_confidentiality_idx", tableName: "doc") {
            column(name: "doc_confidentiality_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-23") {
        createIndex(indexName: "sub_discount_scale_idx", tableName: "subscription") {
            column(name: "sub_discount_scale_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-24") {
        createIndex(indexName: "sub_form_idx", tableName: "subscription") {
            column(name: "sub_form_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-25") {
        createIndex(indexName: "sub_kind_idx", tableName: "subscription") {
            column(name: "sub_kind_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-26") {
        createIndex(indexName: "sub_resource_idx", tableName: "subscription") {
            column(name: "sub_resource_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-27") {
        createIndex(indexName: "sub_status_idx", tableName: "subscription") {
            column(name: "sub_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-28") {
        createIndex(indexName: "surorg_address_idx", tableName: "survey_org") {
            column(name: "surorg_address_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-29") {
        createIndex(indexName: "surorg_e_invoice_portal_idx", tableName: "survey_org") {
            column(name: "surorg_e_invoice_portal_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-30") {
        createIndex(indexName: "surorg_person_idx", tableName: "survey_org") {
            column(name: "surorg_person_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-31") {
        createIndex(indexName: "ci_copy_base_idx", tableName: "cost_item") {
            column(name: "ci_copy_base")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-32") {
        createIndex(indexName: "ci_pkg_idx", tableName: "cost_item") {
            column(name: "ci_pkg_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-33") {
        createIndex(indexName: "ci_sub_pkg_idx", tableName: "cost_item") {
            column(name: "ci_sub_pkg_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-34") {
        createIndex(indexName: "cig_budget_code_idx", tableName: "cost_item_group") {
            column(name: "cig_budget_code_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-35") {
        createIndex(indexName: "cig_cost_item_idx", tableName: "cost_item_group") {
            column(name: "cig_cost_item_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-36") {
        createIndex(indexName: "lp_ref_value_idx", tableName: "license_property") {
            column(name: "lp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-37") {
        createIndex(indexName: "op_ref_value_idx", tableName: "org_property") {
            column(name: "op_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-38") {
        createIndex(indexName: "pkg_breakable_idx", tableName: "package") {
            column(name: "pkg_breakable_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-39") {
        createIndex(indexName: "pkg_consistent_idx", tableName: "package") {
            column(name: "pkg_consistent_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-40") {
        createIndex(indexName: "pkg_content_type_idx", tableName: "package") {
            column(name: "pkg_content_type_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-41") {
        createIndex(indexName: "pkg_file_idx", tableName: "package") {
            column(name: "pkg_file_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-42") {
        createIndex(indexName: "pkg_nominal_platform_idx", tableName: "package") {
            column(name: "pkg_nominal_platform_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-43") {
        createIndex(indexName: "pkg_scope_idx", tableName: "package") {
            column(name: "pkg_scope_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-44") {
        createIndex(indexName: "pkg_status_idx", tableName: "package") {
            column(name: "pkg_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-45") {
        createIndex(indexName: "plp_ref_value_idx", tableName: "platform_property") {
            column(name: "plp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-46") {
        createIndex(indexName: "pp_ref_value_idx", tableName: "person_property") {
            column(name: "pp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-47") {
        createIndex(indexName: "prp_ref_value_idx", tableName: "provider_property") {
            column(name: "prp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-48") {
        createIndex(indexName: "the_tipp_idx", tableName: "title_history_event") {
            column(name: "the_tipp_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-49") {
        createIndex(indexName: "thep_event_idx", tableName: "title_history_event_participant") {
            column(name: "thep_event_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-50") {
        createIndex(indexName: "thep_participant_idx", tableName: "title_history_event_participant") {
            column(name: "thep_participant_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-51") {
        createIndex(indexName: "vp_ref_value_idx", tableName: "vendor_property") {
            column(name: "vp_ref_value_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-52") {
        createIndex(indexName: "apd_org_access_point_idx", tableName: "access_point_data") {
            column(name: "apd_org_access_point_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-53") {
        createIndex(indexName: "cid_customer_idx", tableName: "customer_identifier") {
            column(name: "cid_customer_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-54") {
        createIndex(indexName: "cid_owner_idx", tableName: "customer_identifier") {
            column(name: "cid_owner_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-55") {
        createIndex(indexName: "cid_platform_idx", tableName: "customer_identifier") {
            column(name: "cid_platform_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-56") {
        createIndex(indexName: "das_ddobj_idx", tableName: "dashboard_due_date") {
            column(name: "das_ddobj_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-57") {
        createIndex(indexName: "wfcp_checklist_idx", tableName: "wf_checkpoint") {
            column(name: "wfcp_checklist_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-58") {
        createIndex(indexName: "lic_category_idx", tableName: "license") {
            column(name: "lic_category_rdv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-59") {
        createIndex(indexName: "lic_open_ended_idx", tableName: "license") {
            column(name: "lic_open_ended_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-60") {
        createIndex(indexName: "lic_status_idx", tableName: "license") {
            column(name: "lic_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-61") {
        createIndex(indexName: "pc_status_idx", tableName: "pending_change") {
            column(name: "pc_status_rdv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-62") {
        createIndex(indexName: "pkg_provider_idx", tableName: "package") {
            column(name: "pkg_provider_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-63") {
        createIndex(indexName: "tsk_lic_idx", tableName: "task") {
            column(name: "tsk_lic_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-64") {
        createIndex(indexName: "tsk_org_idx", tableName: "task") {
            column(name: "tsk_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-65") {
        createIndex(indexName: "tsk_prov_idx", tableName: "task") {
            column(name: "tsk_prov_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-66") {
        createIndex(indexName: "tsk_responsible_org_idx", tableName: "task") {
            column(name: "tsk_responsible_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-67") {
        createIndex(indexName: "tsk_responsible_user_idx", tableName: "task") {
            column(name: "tsk_responsible_user_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-68") {
        createIndex(indexName: "tsk_sub_idx", tableName: "task") {
            column(name: "tsk_sub_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-69") {
        createIndex(indexName: "tsk_sur_config_idx", tableName: "task") {
            column(name: "tsk_sur_config_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-70") {
        createIndex(indexName: "tsk_ven_idx", tableName: "task") {
            column(name: "tsk_ven_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-71") {
        createIndex(indexName: "us_org_idx", tableName: "user_setting") {
            column(name: "us_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-72") {
        createIndex(indexName: "os_rv_idx", tableName: "org_setting") {
            column(name: "os_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-73") {
        createIndex(indexName: "us_rv_idx", tableName: "user_setting") {
            column(name: "us_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-74") {
        createIndex(indexName: "combo_from_to_org_idx", tableName: "combo") {
            column(name: "combo_from_org_fk")

            column(name: "combo_to_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-75") {
        createIndex(indexName: "tsk_creator_idx", tableName: "task") {
            column(name: "tsk_creator_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-76") {
        createIndex(indexName: "inv_owner_idx", tableName: "invoice") {
            column(name: "inv_owner")
        }
    }
    changeSet(author: "klober (generated)", id: "1724138532317-77") {
        createIndex(indexName: "surconf_sub_idx", tableName: "survey_config") {
            column(name: "surconf_sub_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-78") {
        createIndex(indexName: "surconf_surinfo_idx", tableName: "survey_config") {
            column(name: "surconf_surinfo_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-79") {
        createIndex(indexName: "surconf_surprop_idx", tableName: "survey_config") {
            column(name: "surconf_surprop_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-80") {
        createIndex(indexName: "surin_owner_org_idx", tableName: "survey_info") {
            column(name: "surin_owner_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-81") {
        createIndex(indexName: "surin_status_idx", tableName: "survey_info") {
            column(name: "surin_status_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1724138532317-82") {
        createIndex(indexName: "surin_type_idx", tableName: "survey_info") {
            column(name: "surin_type_rv_fk")
        }
    }

}
