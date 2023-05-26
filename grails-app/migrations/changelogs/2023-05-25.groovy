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
}
