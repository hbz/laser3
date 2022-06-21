package changelogs

import de.laser.helper.DatabaseInfo

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1651232328540-1") {
        createIndex(indexName: "c4r_metric_type_idx", tableName: "counter4report") {
            column(name: "c4r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-2") {
        createIndex(indexName: "c5r_access_method_idx", tableName: "counter5report") {
            column(name: "c5r_access_method")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-3") {
        createIndex(indexName: "c5r_access_type_idx", tableName: "counter5report") {
            column(name: "c5r_access_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-4") {
        createIndex(indexName: "c5r_metric_type_idx", tableName: "counter5report") {
            column(name: "c5r_metric_type")
        }
    }

    changeSet(author: "klober (modified)", id: "1651232328540-5") {
        grailsChange {
            change {
                List<List> todo = [
                        [ "address", "adr_city", "varchar(255)", false ],
                        [ "address", "adr_name", "varchar(255)", false ],
                        [ "address", "adr_street_1", "varchar(255)", false ],
                        [ "alternative_name", "altname_name", "text", false ],
                        [ "budget_code", "bc_value", "varchar(255)", false ],
                        [ "contact", "ct_content", "varchar(255)", false ],
                        [ "cost_item", "ci_cost_title", "varchar(255)", false ],
                        [ "doc", "doc_filename", "varchar(255)", false ],
                        [ "doc", "doc_title", "varchar(255)", false ],
                        [ "identifier", "id_value", "varchar(255)", true ],
                        [ "identifier_namespace", "idns_ns", "varchar(255)", false ],
                        [ "invoice", "inv_number", "varchar(255)", false ],
                        [ "issue_entitlement", "ie_name", "text", false ],
                        [ "issue_entitlement", "ie_sortname", "text", false ],
                        [ "issue_entitlement_group", "ig_name", "varchar(255)", false ],
                        [ "license", "lic_ref", "varchar(255)", false ],
                        [ "license", "lic_sortable_ref", "varchar(255)", false ],
                        [ "license_property", "lp_string_value", "text", false ],
                        [ "ordering", "ord_number", "varchar(255)", false ],
                        [ "org", "org_legal_patronname", "varchar(255)", false ],
                        [ "org", "org_name", "varchar(255)", true ],
                        [ "org", "org_shortcode", "varchar(128)", true ],
                        [ "org", "org_shortname", "varchar(255)", true ],
                        [ "org", "org_sortname", "varchar(255)", true ],
                        [ "org_access_point", "oar_name", "varchar(255)", false ],
                        [ "org_property", "op_string_value", "text", false ],
                        [ "package", "pkg_name", "varchar(255)", false ],
                        [ "package", "pkg_sort_name", "varchar(255)", false ],
                        [ "person", "prs_first_name", "varchar(255)", false ],
                        [ "person", "prs_last_name", "varchar(255)", false ],
                        [ "person", "prs_middle_name", "varchar(255)", false ],
                        [ "person_property", "pp_string_value", "text", false ],
                        [ "platform", "plat_name", "varchar(255)", false ],
                        [ "platform", "plat_normalised_name", "varchar(255)", false ],
                        [ "platform_property", "plp_string_value", "text", false ],
                        [ "property_definition", "pd_name", "varchar(255)", false ],
                        [ "property_definition_group", "pdg_name", "varchar(255)", false ],
                        [ "reporting_filter", "rf_title", "varchar(255)", false ],
                        [ "subscription", "sub_name", "varchar(255)", false ],
                        [ "subscription_property", "sp_string_value", "text", false ],
                        [ "survey_info", "surin_name", "varchar(255)", false ],
                        [ "system_announcement", "sa_title", "varchar(255)", false ],
                        [ "task", "tsk_title", "varchar(255)", false ],
                        [ "title_instance", "sort_title", "text", false ],
                        [ "title_instance", "ti_key_title", "text", false ],
                        [ "title_instance", "ti_norm_title", "text", false ],
                        [ "title_instance", "ti_series_name", "text", false ],
                        [ "title_instance", "ti_title", "text", false ],
                        [ "title_instance_package_platform", "tipp_name", "text", false ],
                        [ "title_instance_package_platform", "tipp_norm_name", "text", false ],
                        [ "title_instance_package_platform", "tipp_publisher_name", "text", false ],
                        [ "title_instance_package_platform", "tipp_series_name", "text", false ],
                        [ "title_instance_package_platform", "tipp_sort_name", "text", false ],
                        [ "\"user\"", "display", "varchar(255)", false ],
                        [ "\"user\"", "username", "varchar(255)", false ],
                        [ "wf_condition", "wfc_title", "varchar(255)", false ],
                        [ "wf_task", "wft_title", "varchar(255)", false ],
                        [ "wf_task_prototype", "wftp_title", "varchar(255)", false ],
                        [ "wf_workflow", "wfw_title", "varchar(255)", false ],
                        [ "wf_workflow_prototype", "wfwp_title", "varchar(255)", false ],
                ]

                todo.each { nfo ->
                    String table = nfo[0]
                    String column = nfo[1]

                    sql.execute('alter table ' + table + ' alter column ' + column + ' type ' + nfo[2] + ' collate public."' + DatabaseInfo.DE_U_CO_PHONEBK_X_ICU + '"')

                    if (nfo[3]) {
                        sql.execute('drop index ' + column + '_idx')
                        sql.execute('create index ' + column + '_idx on ' + table + '(' + column + ')')
                    }
                }
            }
            rollback {}
        }
    }

}