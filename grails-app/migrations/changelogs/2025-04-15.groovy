package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1744714174801-1") {
        grailsChange {
            change {
                String query = "update cost_item set ci_cost_information_ref_value_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'dbv' and rdc_description = 'cost.information.contracts.membership') where ci_cost_information_ref_value_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'regional' and rdc_description = 'cost.information.contracts.membership')"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1744714174801-2") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_value = 'regional' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'cost.information.contracts.membership')"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1744714174801-3") {
        grailsChange {
            change {
                String query = "update cost_item set ci_cost_information_ref_value_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'dbv' and rdc_description = 'cost.information.contracts.membership') where ci_cost_information_ref_value_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'vdb' and rdc_description = 'cost.information.contracts.membership')"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1744714174801-4") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_value = 'vdb' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'cost.information.contracts.membership')"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }
}
