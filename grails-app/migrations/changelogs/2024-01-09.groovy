package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1704813846277-1") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("update cost_item set ci_cost_in_local_currency = null, ci_currency_rate = 0.0, ci_last_updated = now() from refdata_value where rdv_id = ci_billing_currency_rv_fk and rdv_value != 'EUR' and (ci_currency_rate = 1 or  ci_currency_rate = 1.0)")
                confirm("update cost_item set ci_cost_in_local_currency = null, ci_currency_rate = 0.0, ci_last_updated = now() from refdata_value where rdv_id = ci_billing_currency_rv_fk and rdv_value != 'EUR' and (ci_currency_rate = 1 or  ci_currency_rate = 1.0): ${updated}")
                changeSet.setComments("update cost_item set ci_cost_in_local_currency = null, ci_currency_rate = 0.0, ci_last_updated = now() from refdata_value where rdv_id = ci_billing_currency_rv_fk and rdv_value != 'EUR' and (ci_currency_rate = 1 or  ci_currency_rate = 1.0): ${updated}")
            }
            rollback {}
        }
    }
}
