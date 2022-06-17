package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1655451889981-1") {
        grailsChange {
            change {
                sql.execute('alter table access_point_data rename column id to apd_id')
                sql.execute('alter table access_point_data rename column version to apd_version')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1655451889981-2") {
        grailsChange {
            change {
                sql.execute('alter table cost_item_element_configuration rename column version to ciec_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-3") {
        grailsChange {
            change {
                sql.execute('alter table doc rename column migrated to doc_migrated')
                sql.execute('alter table doc rename column date_created to doc_date_created')
                sql.execute('alter table doc rename column last_updated to doc_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-4") {
        grailsChange {
            change {
                sql.execute('alter table ftcontrol rename column id to ftc_id')
                sql.execute('alter table ftcontrol rename column version to ftc_version')
                sql.execute('alter table ftcontrol rename column active to ftc_active')
                sql.execute('alter table ftcontrol rename column activity to ftc_activity')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-5") {
        grailsChange {
            change {
                sql.execute('alter table identifier rename column version to id_version')

            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-6") {
        grailsChange {
            change {
                sql.execute('alter table identifier_namespace rename column version to idns_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-7") {
        grailsChange {
            change {
                sql.execute('alter table links rename column version to l_version')
                sql.execute('alter table links rename column last_updated to l_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-8") {
        grailsChange {
            change {
                sql.execute('alter table org_subject_group rename column version to osg_version')
            }
        }
        rollback {}
    }


    changeSet(author: "klober (modified)", id: "1655451889981-9") {
        grailsChange {
            change {
                sql.execute('alter table pending_change rename column id to pc_id')
                sql.execute('alter table pending_change rename column version to pc_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-10") {
        grailsChange {
            change {
                sql.execute('alter table pending_change_configuration rename column id to pcc_id')
                sql.execute('alter table pending_change_configuration rename column version to pcc_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-11") {
        grailsChange {
            change {
                sql.execute('alter table platform_access_method rename column id to pam_id')
                sql.execute('alter table platform_access_method rename column version to pam_version')
                sql.execute('alter table platform_access_method rename column date_created to pam_date_created')
                sql.execute('alter table platform_access_method rename column last_updated to pam_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-12") {
        grailsChange {
            change {
                sql.execute('alter table price_item rename column version to pi_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-13") {
        grailsChange {
            change {
                sql.execute('alter table property_definition rename column version to pd_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-14") {
        grailsChange {
            change {
                sql.execute('alter table stats_triple_cursor rename column id to stats_id')
                sql.execute('alter table stats_triple_cursor rename column version to stats_version')
                sql.execute('alter table stats_triple_cursor rename column avail_from to stats_avail_from')
                sql.execute('alter table stats_triple_cursor rename column avail_to to stats_avail_to')
                sql.execute('alter table stats_triple_cursor rename column jerror to stats_jerror')
                sql.execute('alter table stats_triple_cursor rename column num_facts to stats_num_facts')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-15") {
        grailsChange {
            change {
                sql.execute('alter table system_setting rename column version to set_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-16") {
        grailsChange {
            change {
                sql.execute('alter table title_history_event rename column id to the_id')
                sql.execute('alter table title_history_event rename column version to the_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-17") {
        grailsChange {
            change {
                sql.execute('alter table doc_context rename column domain to dc_domain')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-18") {
        grailsChange {
            change {
                sql.execute('alter table content_item rename column version to ci_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-19") {
        grailsChange {
            change {
                sql.execute('alter table change_notification_queue_item rename column id to cnqi_id')
                sql.execute('alter table change_notification_queue_item rename column version to cnqi_version')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-20") {
        grailsChange {
            change {
                sql.execute('alter table org_access_point rename column id to oar_id')
                sql.execute('alter table org_access_point rename column version to oar_version')
                sql.execute('alter table org_access_point rename column date_created to oar_date_created')
                sql.execute('alter table org_access_point rename column last_updated to oar_last_updated')
            }
        }
        rollback {}
    }

    changeSet(author: "klober (modified)", id: "1655451889981-21") {
        grailsChange {
            change {
                sql.execute('alter table org_access_point_link rename column id to oapl_id')
                sql.execute('alter table org_access_point_link rename column version to oapl_version')
                sql.execute('alter table org_access_point_link rename column date_created to oapl_date_created')
                sql.execute('alter table org_access_point_link rename column last_updated to oapl_last_updated')
            }
        }
        rollback {}
    }
}
