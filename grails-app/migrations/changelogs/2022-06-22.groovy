package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1655879679495-1") {
        grailsChange {
            change {
                sql.execute('alter table cost_item rename column date_created to ci_date_created')
                sql.execute('alter table cost_item rename column last_updated to ci_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-2") {
        grailsChange {
            change {
                sql.execute('alter table cost_item_element_configuration rename column date_created to ciec_date_created')
                sql.execute('alter table cost_item_element_configuration rename column last_updated to ciec_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-3") {
        grailsChange {
            change {
                sql.execute('alter table fact rename column inst_id to fact_inst_fk')
                sql.execute('alter table fact rename column related_title_id to fact_related_title_fk')
                sql.execute('alter table fact rename column supplier_id to fact_supplier_fk')
                sql.execute('alter table fact rename column reporting_month to fact_reporting_month')
                sql.execute('alter table fact rename column reporting_year to fact_reporting_year')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-4") {
        grailsChange {
            change {
                sql.execute('alter table ftcontrol rename column domain_class_name to ftc_domain_class_name')
                sql.execute('alter table ftcontrol rename column db_elements to ftc_db_elements')
                sql.execute('alter table ftcontrol rename column es_elements to ftc_es_elements')
                sql.execute('alter table ftcontrol rename column date_created to ftc_date_created')
                sql.execute('alter table ftcontrol rename column last_updated to ftc_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-5") {
        grailsChange {
            change {
                sql.execute('alter table org_access_point_link rename column active to oapl_active')
                sql.execute('alter table org_access_point_link rename column globaluid to oapl_guid')
                sql.execute('alter table org_access_point_link rename column oap_id to oapl_oap_fk')
                sql.execute('alter table org_access_point_link rename column platform_id to oapl_platform_fk')
                sql.execute('alter table org_access_point_link rename column sub_pkg_id to oapl_sub_pkg_fk')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-6") {
        grailsChange {
            change {
                sql.execute('alter table perm rename column id to pm_id')
                sql.execute('alter table perm rename column code to pm_code')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-7") {
        grailsChange {
            change {
                sql.execute('alter table perm_grant rename column id to pmgr_id')
                sql.execute('alter table perm_grant rename column perm_id to pmgr_perm_fk')
                sql.execute('alter table perm_grant rename column role_id to pmgr_role_fk')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-8") {
        grailsChange {
            change {
                sql.execute('alter table role rename column id to r_id')
                sql.execute('alter table role rename column authority to r_authority')
                sql.execute('alter table role rename column authority_de to r_authority_de')
                sql.execute('alter table role rename column authority_en to r_authority_en')
                sql.execute('alter table role rename column role_type to r_role_type')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-9") {
        grailsChange {
            change {
                sql.execute('alter table stats_triple_cursor rename column fact_type_id to stats_fact_type_rdv_fk')
                sql.execute('alter table stats_triple_cursor rename column identifier_type_id to stats_identifier_type_fk')
            }
        }
        rollback {}
    }

        changeSet(author: "klober (modified)", id: "1655879679495-10") {
        grailsChange {
            change {
                sql.execute('alter table "user" rename column id to usr_id')
                sql.execute('alter table "user" rename column version to usr_version')

                sql.execute('alter table "user" rename column account_expired to usr_account_expired')
                sql.execute('alter table "user" rename column account_locked to usr_account_locked')
                sql.execute('alter table "user" rename column display to usr_display')
                sql.execute('alter table "user" rename column email to usr_email')
                sql.execute('alter table "user" rename column enabled to usr_enabled')
                sql.execute('alter table "user" rename column image to usr_image')
                sql.execute('alter table "user" rename column password to usr_password')
                sql.execute('alter table "user" rename column password_expired to usr_password_expired')
                sql.execute('alter table "user" rename column shibb_scope to usr_shibb_scope')
                sql.execute('alter table "user" rename column username to usr_username')
                sql.execute('alter table "user" rename column last_updated to usr_last_updated')
                sql.execute('alter table "user" rename column date_created to usr_date_created')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-11") {
        grailsChange {
            change {
                sql.execute('alter table user_org rename column id to uo_id')
                sql.execute('alter table user_org rename column version to uo_version')
                sql.execute('alter table user_org rename column formal_role_id to uo_formal_role_fk')
                sql.execute('alter table user_org rename column org_id to uo_org_fk')
                sql.execute('alter table user_org rename column user_id to uo_user_fk')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-12") {
        grailsChange {
            change {
                sql.execute('alter table user_role rename column role_id to ur_role_fk')
                sql.execute('alter table user_role rename column user_id to ur_user_fk')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-13") {
        grailsChange {
            change {
                sql.execute('alter table license rename column last_updated to lic_last_updated')
                sql.execute('alter table license rename column date_created to lic_date_created')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-14") {
        grailsChange {
            change {
                sql.execute('alter table package rename column last_updated to pkg_last_updated')
                sql.execute('alter table package rename column date_created to pkg_date_created')
                sql.execute('alter table package rename column auto_accept to pkg_auto_accept')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-15") {
        grailsChange {
            change {
                sql.execute('alter table platform rename column last_updated to plat_last_updated')
                sql.execute('alter table platform rename column date_created to plat_date_created')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-16") {
        grailsChange {
            change {
                sql.execute('alter table subscription rename column last_updated to sub_last_updated')
                sql.execute('alter table subscription rename column date_created to sub_date_created')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-17") {
        grailsChange {
            change {
                sql.execute('alter table title_instance rename column last_updated to ti_last_updated')
                sql.execute('alter table title_instance rename column date_created to ti_date_created')
                sql.execute('alter table title_instance rename column sort_title to ti_sort_title')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-18") {
        grailsChange {
            change {
                sql.execute('alter table platform_access_method rename column plat_guid to pam_guid')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655879679495-19") {
        grailsChange {
            change {
                sql.execute('alter table property_definition_group_binding rename column pgb_id to pdgb_id')
                sql.execute('alter table property_definition_group_binding rename column pgb_version to pdgb_version')
                sql.execute('alter table property_definition_group_binding rename column pgb_lic_fk to pdgb_lic_fk')
                sql.execute('alter table property_definition_group_binding rename column pgb_org_fk to pdgb_org_fk')
                sql.execute('alter table property_definition_group_binding rename column pgb_sub_fk to pdgb_sub_fk')
                sql.execute('alter table property_definition_group_binding rename column pgb_property_definition_group_fk to pdgb_property_definition_group_fk')
                sql.execute('alter table property_definition_group_binding rename column pbg_is_visible to pdgb_is_visible')
                sql.execute('alter table property_definition_group_binding rename column pbg_is_visible_for_cons_member to pdgb_is_visible_for_cons_member')
                sql.execute('alter table property_definition_group_binding rename column pbg_last_updated to pdgb_last_updated')
                sql.execute('alter table property_definition_group_binding rename column pbg_date_created to pdgb_date_created')
            }
        }
        rollback {}
    }
}
