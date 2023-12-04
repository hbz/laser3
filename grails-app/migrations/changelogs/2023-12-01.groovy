package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1701436936157-1") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("update cost_item set ci_cost_in_local_currency = ci_cost_in_billing_currency, ci_currency_rate = 1 from refdata_value where rdv_id = ci_billing_currency_rv_fk and ci_owner = 1 and rdv_value = 'EUR' and ci_currency_rate != 1 or ci_currency_rate is null")
                confirm("update cost_item set ci_cost_in_local_currency = ci_cost_in_billing_currency, ci_currency_rate = 1 from refdata_value where rdv_id = ci_billing_currency_rv_fk and ci_owner = 1 and rdv_value = 'EUR' and ci_currency_rate != 1 or ci_currency_rate is null: ${updated}")
                changeSet.setComments("update cost_item set ci_cost_in_local_currency = ci_cost_in_billing_currency, ci_currency_rate = 1 from refdata_value where rdv_id = ci_billing_currency_rv_fk and ci_owner = 1 and rdv_value = 'EUR' and ci_currency_rate != 1 or ci_currency_rate is null: ${updated}")
            }
            rollback {}
        }
    }
}
