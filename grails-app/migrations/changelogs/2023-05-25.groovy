package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1684995746509-1") {
        grailsChange {
            change {
                sql.executeUpdate("update property_definition set pd_tenant_fk = null where pd_name = 'Global Consumer Survey Test'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1684995746509-2") {
        grailsChange {
            change {
                sql.executeUpdate("update issue_entitlement set ie_perpetual_access_by_sub_fk = null from subscription where ie_subscription_fk = sub_id and ie_perpetual_access_by_sub_fk is not null and ie_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'tipp.status' and rdv_value = 'Expected')")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684995746509-3") {
        grailsChange {
            change {
                Integer countUpdate = sql.executeUpdate("delete from permanent_title where pt_tipp_fk in (select tipp_id from title_instance_package_platform where tipp_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'tipp.status' and rdv_value = 'Expected'))")
                        confirm("delete permanent_title where tipp.status = Expected: ${countUpdate}")
                        changeSet.setComments("delete permanent_title where tipp.status = Expected: ${countUpdate}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684995746509-4") {
        grailsChange {
            change {
                sql.executeUpdate("delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'tipp.status') and rdv_value = 'Transferred'")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684995746509-5") {
        grailsChange {
            change {
                sql.executeUpdate("delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'tipp.status') and rdv_value = 'Unknown'")
            }
            rollback {}
        }
    }
}
