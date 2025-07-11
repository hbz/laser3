package changelogs

databaseChangeLog = {

    changeSet(author: "klober (mofified)", id: "1752220991851-1") {
        grailsChange {
            change {
                sql.executeUpdate("alter table access_point_data rename column apd_guid to apd_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-2") {
        grailsChange {
            change {
                sql.executeUpdate("alter table cost_item rename column ci_guid to ci_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-3") {
        grailsChange {
            change {
                sql.executeUpdate("alter table cost_item_element_configuration rename column ciec_guid to ciec_laser_id")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (mofified)", id: "1752220991851-4") {
        grailsChange {
            change {
                sql.executeUpdate("alter table issue_entitlement rename column ie_guid to ie_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-5") {
        grailsChange {
            change {
                sql.executeUpdate("alter table license rename column lic_guid to lic_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-6") {
        grailsChange {
            change {
                sql.executeUpdate("alter table org_access_point_link rename column oapl_guid to oapl_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-7") {
        grailsChange {
            change {
                sql.executeUpdate("alter table org_access_point rename column oar_guid to oar_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-8") {
        grailsChange {
            change {
                sql.executeUpdate("alter table price_item rename column pi_guid to pi_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-9") {
        grailsChange {
            change {
                sql.executeUpdate("alter table package rename column pkg_guid to pkg_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-10") {
        grailsChange {
            change {
                sql.executeUpdate("alter table platform rename column plat_guid to plat_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-11") {
        grailsChange {
            change {
                sql.executeUpdate("alter table provider rename column prov_guid to prov_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-12") {
        grailsChange {
            change {
                sql.executeUpdate("alter table person rename column prs_guid to prs_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-13") {
        grailsChange {
            change {
                sql.executeUpdate("alter table subscription rename column sub_guid to sub_laser_id")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (mofified)", id: "1752220991851-14") {
        grailsChange {
            change {
                sql.executeUpdate("alter table title_instance_package_platform rename column tipp_guid to tipp_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-15") {
        grailsChange {
            change {
                sql.executeUpdate("alter table vendor rename column ven_guid to ven_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752220991851-16") {
        grailsChange {
            change {
                sql.executeUpdate("alter table org rename column org_guid to org_laser_id")
            }
            rollback {}
        }
    }
}
