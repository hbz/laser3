package de.laser.domain

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.RefdataValue

class PriceItem extends AbstractCostItem{

    Date priceDate

    static belongsTo = [issueEntitlement: IssueEntitlement]

    static mapping = {
        id                              column: 'pi_id'
        globalUID                       column: 'pi_guid'
        billingCurrency                 column: 'pi_billing_currency_rv_fk'
        costInBillingCurrency           column: 'pi_cost_in_billing_currency'
        costInLocalCurrency             column: 'pi_cost_in_local_currency'
        priceDate                       column: 'pi_price_date'
        issueEntitlement                column: 'pi_ie_fk'
    }

    static constraints = {
        globalUID                       (nullable: false, blank: false, unique: true, maxSize: 255)
        billingCurrency                 (nullable: false, blank: false)
        costInBillingCurrency           (nullable: false, blank: false)
        costInLocalCurrency             (nullable: false, blank: false)
        priceDate                       (nullable: false, blank: false)
        issueEntitlement                (nullable: false, blank: false)
    }
}
