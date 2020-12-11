import de.laser.Org
import de.laser.Subscription
import de.laser.titles.TitleInstance

String flaggingToLaser = "'Anbieter_Produkt_ID','DBIS Anker','DBS-ID','EZB anchor','ezb_collection_id','ezb_org_id','global','gnd_org_id','GRID ID','ISIL','ISIL_Paketsigel','Leitkriterium (intern)','Leitweg-ID','VAT','wibid'"

databaseChangeLog = {

    changeSet(author: "galffy (genereated)", id: "1607669186880-1") {
        addColumn(tableName: "identifier_namespace") {
            column(name: "idns_is_from_laser", type: "boolean")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-2") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_from_laser = true where idns_ns in ("+flaggingToLaser+")"
                sql.execute ( query )
            }
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-3") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_from_laser = false where idns_ns not in ("+flaggingToLaser+")"
                sql.execute ( query )
            }
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-4") {
        addNotNullConstraint(columnDataType: "bool", columnName: "idns_is_from_laser", tableName: "identifier_namespace")
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-5") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'Anbieter_Produkt_ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-6") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'cup';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-7") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'DBIS Anker';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-8") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'DBS-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-9") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'dnb';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-10") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${de.laser.Package.class.name}' where idns_ns = 'duz';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-11") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'ebookcentral';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-12") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'ezb_collection_id';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-13") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'ezb_org_id';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-14") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'global';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-15") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'gnd_org_nr';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-16") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'GRID ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-17") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'Leitkriterium (intern)';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-18") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'Leitweg-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-19") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'mitpress';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-20") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'oup';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-21") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'pisbn';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-22") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'preselect';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-23") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'statssid';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-24") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${de.laser.Package.class.name}' where idns_ns = 'thieme';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-25") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'utb';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-26") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'VAT';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-27") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'wibid';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-28") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'hbz_at-ID');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-29") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'hbz_at-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-30") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'originEditUrl');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-31") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'originEditUrl';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-32") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'unknown');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-33") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'unknown';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-34") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_unique = false where idns_ns = 'DBIS Anker';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-35") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_unique = false where idns_ns = 'EZB anchor';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    /*
    changeSet(author: "galffy (modified)", id: "1607669186880-36") {
        dropIndex(indexName: "ie_sub_idx", tableName: "issue_entitlement")

        createIndex(indexName: "ie_sub_idx", tableName: "issue_entitlement") {
            column(name: "ie_subscription_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-37") {
        dropIndex(indexName: "ie_tipp_idx", tableName: "issue_entitlement")

        createIndex(indexName: "ie_tipp_idx", tableName: "issue_entitlement") {
            column(name: "ie_tipp_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-38") {
        dropIndex(indexName: "l_dest_lic_idx", tableName: "links")

        createIndex(indexName: "l_dest_lic_idx", tableName: "links") {
            column(name: "l_dest_lic_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-39") {
        dropIndex(indexName: "l_dest_sub_idx", tableName: "links")

        createIndex(indexName: "l_dest_sub_idx", tableName: "links") {
            column(name: "l_dest_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-40") {
        dropIndex(indexName: "l_source_lic_idx", tableName: "links")

        createIndex(indexName: "l_source_lic_idx", tableName: "links") {
            column(name: "l_source_lic_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-41") {
        dropIndex(indexName: "l_source_sub_idx", tableName: "links")

        createIndex(indexName: "l_source_sub_idx", tableName: "links") {
            column(name: "l_source_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-42") {
        dropIndex(indexName: "lcp_owner_idx", tableName: "license_property")

        createIndex(indexName: "lcp_owner_idx", tableName: "license_property") {
            column(name: "lp_owner_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-43") {
        dropIndex(indexName: "lic_dates_idx", tableName: "license")

        createIndex(indexName: "lic_dates_idx", tableName: "license") {
            column(name: "lic_end_date")

            column(name: "lic_start_date")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-44") {
        dropIndex(indexName: "lic_parent_idx", tableName: "license")

        createIndex(indexName: "lic_parent_idx", tableName: "license") {
            column(name: "lic_parent_lic_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-45") {
        dropIndex(indexName: "lp_instance_of_idx", tableName: "license_property")

        createIndex(indexName: "lp_instance_of_idx", tableName: "license_property") {
            column(name: "lp_instance_of_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-46") {
        dropIndex(indexName: "lp_tenant_idx", tableName: "license_property")

        createIndex(indexName: "lp_tenant_idx", tableName: "license_property") {
            column(name: "lp_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-47") {
        dropIndex(indexName: "lp_type_idx", tableName: "license_property")

        createIndex(indexName: "lp_type_idx", tableName: "license_property") {
            column(name: "lp_type_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-48") {
        dropIndex(indexName: "op_owner_idx", tableName: "org_property")

        createIndex(indexName: "op_owner_idx", tableName: "org_property") {
            column(name: "op_owner_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-49") {
        dropIndex(indexName: "op_tenant_idx", tableName: "org_property")

        createIndex(indexName: "op_tenant_idx", tableName: "org_property") {
            column(name: "op_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-50") {
        dropIndex(indexName: "op_type_idx", tableName: "org_property")

        createIndex(indexName: "op_type_idx", tableName: "org_property") {
            column(name: "op_type_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-51") {
        dropIndex(indexName: "or_lic_idx", tableName: "org_role")

        createIndex(indexName: "or_lic_idx", tableName: "org_role") {
            column(name: "or_lic_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-52") {
        dropIndex(indexName: "or_org_rt_idx", tableName: "org_role")

        createIndex(indexName: "or_org_rt_idx", tableName: "org_role") {
            column(name: "or_org_fk")

            column(name: "or_roletype_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-53") {
        dropIndex(indexName: "or_pkg_idx", tableName: "org_role")

        createIndex(indexName: "or_pkg_idx", tableName: "org_role") {
            column(name: "or_pkg_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-54") {
        dropIndex(indexName: "or_sub_idx", tableName: "org_role")

        createIndex(indexName: "or_sub_idx", tableName: "org_role") {
            column(name: "or_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-55") {
        dropIndex(indexName: "ord_owner_idx", tableName: "ordering")

        createIndex(indexName: "ord_owner_idx", tableName: "ordering") {
            column(name: "ord_owner")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-56") {
        dropIndex(indexName: "org_name_idx", tableName: "org")

        createIndex(indexName: "org_name_idx", tableName: "org") {
            column(name: "org_name")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-57") {
        dropIndex(indexName: "org_shortcode_idx", tableName: "org")

        createIndex(indexName: "org_shortcode_idx", tableName: "org") {
            column(name: "org_shortcode")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-58") {
        dropIndex(indexName: "org_shortname_idx", tableName: "org")

        createIndex(indexName: "org_shortname_idx", tableName: "org") {
            column(name: "org_shortname")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-59") {
        dropIndex(indexName: "org_sortname_idx", tableName: "org")

        createIndex(indexName: "org_sortname_idx", tableName: "org") {
            column(name: "org_sortname")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-60") {
        dropIndex(indexName: "os_org_idx", tableName: "org_setting")

        createIndex(indexName: "os_org_idx", tableName: "org_setting") {
            column(name: "os_org_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-61") {
        dropIndex(indexName: "pd_tenant_idx", tableName: "property_definition")

        createIndex(indexName: "pd_tenant_idx", tableName: "property_definition") {
            column(name: "pd_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-62") {
        dropIndex(indexName: "pdg_tenant_idx", tableName: "property_definition_group")

        createIndex(indexName: "pdg_tenant_idx", tableName: "property_definition_group") {
            column(name: "pdg_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-63") {
        dropIndex(indexName: "pending_change_costitem_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_costitem_idx", tableName: "pending_change") {
            column(name: "pc_ci_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-64") {
        dropIndex(indexName: "pending_change_lic_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_lic_idx", tableName: "pending_change") {
            column(name: "pc_lic_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-65") {
        dropIndex(indexName: "pending_change_oid_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_oid_idx", tableName: "pending_change") {
            column(name: "pc_oid")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-66") {
        dropIndex(indexName: "pending_change_pkg_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_pkg_idx", tableName: "pending_change") {
            column(name: "pc_pkg_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-67") {
        dropIndex(indexName: "pending_change_pl_cd_oid_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_pl_cd_oid_idx", tableName: "pending_change") {
            column(name: "pc_change_doc_oid")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-68") {
        dropIndex(indexName: "pending_change_pl_ct_oid_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_pl_ct_oid_idx", tableName: "pending_change") {
            column(name: "pc_change_target_oid")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-69") {
        dropIndex(indexName: "pending_change_sub_idx", tableName: "pending_change")

        createIndex(indexName: "pending_change_sub_idx", tableName: "pending_change") {
            column(name: "pc_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-70") {
        dropIndex(indexName: "pkg_dates_idx", tableName: "package")

        createIndex(indexName: "pkg_dates_idx", tableName: "package") {
            column(name: "pkg_end_date")

            column(name: "pkg_start_date")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-71") {
        dropIndex(indexName: "plat_org_idx", tableName: "platform")

        createIndex(indexName: "plat_org_idx", tableName: "platform") {
            column(name: "plat_org_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-72") {
        dropIndex(indexName: "plp_owner_idx", tableName: "platform_property")

        createIndex(indexName: "plp_owner_idx", tableName: "platform_property") {
            column(name: "plp_owner_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-73") {
        dropIndex(indexName: "plp_tenant_idx", tableName: "platform_property")

        createIndex(indexName: "plp_tenant_idx", tableName: "platform_property") {
            column(name: "plp_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-74") {
        dropIndex(indexName: "plp_type_idx", tableName: "platform_property")

        createIndex(indexName: "plp_type_idx", tableName: "platform_property") {
            column(name: "plp_type_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-75") {
        dropIndex(indexName: "pp_owner_idx", tableName: "person_property")

        createIndex(indexName: "pp_owner_idx", tableName: "person_property") {
            column(name: "pp_owner_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-76") {
        dropIndex(indexName: "pp_tenant_fk", tableName: "person_property")

        createIndex(indexName: "pp_tenant_fk", tableName: "person_property") {
            column(name: "pp_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-77") {
        dropIndex(indexName: "pp_type_idx", tableName: "person_property")

        createIndex(indexName: "pp_type_idx", tableName: "person_property") {
            column(name: "pp_type_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-78") {
        dropIndex(indexName: "pr_prs_org_idx", tableName: "person_role")

        createIndex(indexName: "pr_prs_org_idx", tableName: "person_role") {
            column(name: "pr_org_fk")

            column(name: "pr_prs_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-79") {
        dropIndex(indexName: "rdc_description_de_idx", tableName: "refdata_category")

        createIndex(indexName: "rdc_description_de_idx", tableName: "refdata_category") {
            column(name: "rdc_description_de")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-80") {
        dropIndex(indexName: "rdc_description_en_idx", tableName: "refdata_category")

        createIndex(indexName: "rdc_description_en_idx", tableName: "refdata_category") {
            column(name: "rdc_description_en")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-81") {
        dropIndex(indexName: "rdc_description_idx", tableName: "refdata_category")

        createIndex(indexName: "rdc_description_idx", tableName: "refdata_category") {
            column(name: "rdc_description")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-82") {
        dropIndex(indexName: "rdv_owner_value_idx", tableName: "refdata_value")

        createIndex(indexName: "rdv_owner_value_idx", tableName: "refdata_value") {
            column(name: "rdv_owner")

            column(name: "rdv_value")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-83") {
        dropIndex(indexName: "rdv_value_de_idx", tableName: "refdata_value")

        createIndex(indexName: "rdv_value_de_idx", tableName: "refdata_value") {
            column(name: "rdv_value_de")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-84") {
        dropIndex(indexName: "rdv_value_en_idx", tableName: "refdata_value")

        createIndex(indexName: "rdv_value_en_idx", tableName: "refdata_value") {
            column(name: "rdv_value_en")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-85") {
        dropIndex(indexName: "sp_instance_of_idx", tableName: "subscription_property")

        createIndex(indexName: "sp_instance_of_idx", tableName: "subscription_property") {
            column(name: "sp_instance_of_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-86") {
        dropIndex(indexName: "sp_owner_idx", tableName: "subscription_property")

        createIndex(indexName: "sp_owner_idx", tableName: "subscription_property") {
            column(name: "sp_owner_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-87") {
        dropIndex(indexName: "sp_sub_pkg_idx", tableName: "subscription_package")

        createIndex(indexName: "sp_sub_pkg_idx", tableName: "subscription_package") {
            column(name: "sp_pkg_fk")

            column(name: "sp_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-88") {
        dropIndex(indexName: "sp_tenant_idx", tableName: "subscription_property")

        createIndex(indexName: "sp_tenant_idx", tableName: "subscription_property") {
            column(name: "sp_tenant_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-89") {
        dropIndex(indexName: "sp_type_idx", tableName: "subscription_property")

        createIndex(indexName: "sp_type_idx", tableName: "subscription_property") {
            column(name: "sp_type_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-90") {
        dropIndex(indexName: "sp_uri_idx", tableName: "system_profiler")

        createIndex(indexName: "sp_uri_idx", tableName: "system_profiler") {
            column(name: "sp_uri")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-91") {
        dropIndex(indexName: "stats_cursor_idx", tableName: "stats_triple_cursor")

        createIndex(indexName: "stats_cursor_idx", tableName: "stats_triple_cursor") {
            column(name: "stats_title_id")

            column(name: "stats_supplier_id")

            column(name: "stats_customer_id")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-92") {
        dropIndex(indexName: "sub_dates_idx", tableName: "subscription")

        createIndex(indexName: "sub_dates_idx", tableName: "subscription") {
            column(name: "sub_end_date")

            column(name: "sub_start_date")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-93") {
        dropIndex(indexName: "sub_parent_idx", tableName: "subscription")

        createIndex(indexName: "sub_parent_idx", tableName: "subscription") {
            column(name: "sub_parent_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-94") {
        dropIndex(indexName: "sub_type_idx", tableName: "subscription")

        createIndex(indexName: "sub_type_idx", tableName: "subscription") {
            column(name: "sub_type_rv_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-95") {
        dropIndex(indexName: "tc_dates_idx", tableName: "tippcoverage")

        createIndex(indexName: "tc_dates_idx", tableName: "tippcoverage") {
            column(name: "tc_end_date")

            column(name: "tc_start_date")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-96") {
        dropIndex(indexName: "td_new_idx", tableName: "property_definition")

        createIndex(indexName: "td_new_idx", tableName: "property_definition") {
            column(name: "pd_description")

            column(name: "pd_name")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-97") {
        dropIndex(indexName: "td_type_idx", tableName: "property_definition")

        createIndex(indexName: "td_type_idx", tableName: "property_definition") {
            column(name: "pd_rdc")

            column(name: "pd_type")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-98") {
        dropUniqueConstraint(constraintName: "UC_TITLE_INSTANCETI_GOKB_ID_COL", tableName: "title_instance")

        addUniqueConstraint(columnNames: "ti_gokb_id", constraintName: "UC_TITLE_INSTANCETI_GOKB_ID_COL", tableName: "title_instance")
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-99") {
        dropIndex(indexName: "tipp_idx", tableName: "title_instance_package_platform")

        createIndex(indexName: "tipp_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_plat_fk")

            column(name: "tipp_pkg_fk")

            column(name: "tipp_ti_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-100") {
        dropIndex(indexName: "us_user_idx", tableName: "user_setting")

        createIndex(indexName: "us_user_idx", tableName: "user_setting") {
            column(name: "us_user_fk")
        }
    }
     */
}
