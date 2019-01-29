package de.laser

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.CostItemElementConfiguration
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.transaction.Transactional

/**
 * This service will subsequently replace the very complicatedly written methods in the FinanceController class.
 *
 * @author agalffy
 */
@Transactional
class FinancialDataService {

    def contextService

    /**
     * Will replace the methods index and financialData methods in FinanceController class.
     * Retrieves the cost item data for the given subscription type and returns a map grouping the cost items per view.
     *
     * @param subscription - the subscription for which the financial data is retrieved. Its type determines the views displayed on return.
     * @return a LinkedHashMap with the cost items for each tab to display
     */
    LinkedHashMap getCostItems(Subscription sub) {
        Org org = contextService.org
        LinkedHashMap result = [ownCosts:CostItem.findAllByOwnerAndSub(org,sub),consCosts:[],subscrCosts:[]]
        /*
            sqaure one:
            I need to get all the cost items for the subscription. I am thus in inSubMode.
            Needs that first of all, I get every cost item which is linked to this subscription. Beware also the owner of the cost item!
         */
        switch(sub.getCalculatedType()) {
        /*
            own costs, consortial costs
            that is: a) owner = contextOrg and sub = contextSub
            b) owner = contextOrg (which is consortium) and sub.instanceOf = contextSub
         */
            case TemplateSupport.CALCULATED_TYPE_CONSORTIAL:
                result.consCosts = CostItem.executeQuery('select ci from CostItem as ci where ci.owner = :owner and ci.sub in (select s from Subscription as s where s.instanceOf = :sub)',[owner:org,sub:sub])
                break
        /*
            own costs, consortial participation costs
            that is: a) owner = contextOrg and sub = contextSub
            b) owner = consortium and sub = contextSub and visibleForSubscriber
         */
            case TemplateSupport.CALCULATED_TYPE_PARTICIPATION:
                Org subscrCons = Org.executeQuery("select o.org from OrgRole as o where o.sub = :sub and o.roleType = :cons",[sub:sub,cons: RDStore.OR_SUBSCRIPTION_CONSORTIA]).get(0)
                result.subscrCosts = CostItem.executeQuery('select ci from CostItem as ci where ci.order = :owner and ci.sub = :sub and ci.isVisibleForSubscriber = true',[owner:subscrCons,sub:sub])
                break
        }
        result
    }

    /**
     * Will replace the current client-side calculation of the sums.
     * Calculates to a given key the sums (local and for each currency) and assigns the resulting map to a given key.
     *
     * @param key - the key for which the sum is being calculated
     * @param costItems - a list of cost items to count
     * @return a map with the following structure:
     * {
     *     localSum (means the actual value and not the amount which is going to be paid)
     *     localSumAfterTax (for Germany, VATs of 7 and 19 per cent apply) (consider ERMS-957 about that!)
     *     currency: {
     *        billingSum
     *        billingSumAfterTax (see above; consider ERMS-957, too!)
     *     }
     * }
     *
     */
    LinkedHashMap calculateResults(List<CostItem> costItems) {
        LinkedHashMap result = [:]
        LinkedHashMap<RefdataValue,LinkedHashMap> currencyMap = [:]
        double local = 0.0
        double localAfterTax = 0.0
        costItems.each { ci ->
            LinkedHashMap currency = currencyMap.get(ci.billingCurrency)
            if(currency == null)
                currency = [billing:0.0,billingAfterTax:0.0]
            if(ci.costItemElementConfiguration) {
                if(ci.costItemElementConfiguration == RDStore.CIEC_POSITIVE) {
                    local += ci.costInLocalCurrency
                    localAfterTax += ci.costInLocalCurrencyAfterTax
                    currency.billing += ci.costInBillingCurrency
                    currency.billingAfterTax += ci.costInBillingCurrencyAfterTax
                }
                else if(ci.costItemElementConfiguration == RDStore.CIEC_NEGATIVE) {
                    local -= ci.costInLocalCurrency
                    localAfterTax -= ci.costInLocalCurrencyAfterTax
                    currency.billing -= ci.costInBillingCurrency
                    currency.billingAfterTax -= ci.costInBillingCurrencyAfterTax
                }
            }
            currencyMap.put(ci.billingCurrency,currency)
        }
        result.local = local
        result.localAfterTax = localAfterTax
        result.currencies = currencyMap
        result
    }

}
