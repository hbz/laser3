databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-1") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Consortial Subscription' from refdata_category where rdv_value = 'Consortial Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.type'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-2") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Local Subscription' from refdata_category where rdv_value = 'Local Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.type'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-3") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Local Subscription' from refdata_category where rdv_value = 'Local Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-4") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Consortial Subscription' from refdata_category where rdv_value = 'Consortial Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-5") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'National Subscription' from refdata_category where rdv_value = 'National Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-6") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Alliance Subscription' from refdata_category where rdv_value = 'Alliance Licence' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-7") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'fidSubscription' from refdata_category where rdv_value = 'fidLicense' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1648808711619-8") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'dealSubscription' from refdata_category where rdv_value = 'dealLicense' and rdv_owner = rdc_id and rdc_description = 'subscription.kind'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1648804786911-9") {
        dropTable(tableName: "system_ticket")
    }

    changeSet(author: "klober (modified)", id: "1648804786911-10") {
        grailsChange {
            change {
                sql.execute("delete from system_setting where set_name = 'StatusUpdateInterval'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1648804786911-11") {
        grailsChange {
            change {
                sql.execute("delete from system_activity_profiler where date_part('year', sap_date_created) in ('2019', '2020')")
                sql.execute("delete from system_event where date_part('year', se_created) in ('2019', '2020')")
            }
            rollback {}
        }
    }

}
