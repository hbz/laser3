package de.laser.api.v0.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Org
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import grails.converters.JSON
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.sql.Timestamp

class ApiCostItem {

    /**
     * @return CostItem | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findCostItemBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = CostItem.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = CostItem.findAllWhere(globalUID: value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }
        if (result) {
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getCostItem(CostItem costItem, Org context, boolean hasAccess){
        Map<String, Object> result = [:]

        if (! hasAccess) {
            if (costItem.owner?.id == context.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result = retrieveCostItemMap(costItem, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getCostItemList(Org owner, Org context, boolean hasAccess){
        Collection<Object> result = []

        if (! hasAccess) {
            if (owner.id == context.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            // TODO
            result = CostItem.findAllByOwner(owner).globalUID
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getCostItemListWithTimeStamp(Org owner, Org context, boolean hasAccess, String timestamp){
        Collection<Object> result = []

        if (! hasAccess) {
            if (owner.id == context.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            // TODO
            Timestamp ts= new Timestamp(Long.parseLong(timestamp))
            Date apiDate= new Date(ts.getTime());
            def today = new Date()
            result = CostItem.findAllByOwnerAndLastUpdatedBetween(owner, apiDate, today).globalUID
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveCostItemMap(CostItem costItem, Org context){
        def result = [:]

        costItem = GrailsHibernateUtil.unwrapIfProxy(costItem)

        result.globalUID           = costItem.globalUID

        result.costInBillingCurrency            = costItem.costInBillingCurrency
        result.costInBillingCurrencyAfterTax    = costItem.costInBillingCurrencyAfterTax
        result.costInLocalCurrency              = costItem.costInLocalCurrency
        result.costInLocalCurrencyAfterTax      = costItem.costInLocalCurrencyAfterTax

        result.costTitle           = costItem.costTitle
        result.costDescription     = costItem.costDescription
        result.currencyRate        = costItem.currencyRate
        result.dateCreated         = costItem.dateCreated
        result.datePaid            = costItem.datePaid
        result.endDate             = costItem.endDate
        result.finalCostRounding   = costItem.finalCostRounding
        result.invoiceDate         = costItem.invoiceDate
        result.lastUpdated         = costItem.lastUpdated

        result.reference           = costItem.reference
        result.startDate           = costItem.startDate
        result.taxRate             = costItem.taxKey?.taxRate ?: ((costItem.taxKey?.taxRate == 0) ? costItem.taxKey?.taxRate : costItem.taxRate)

        // erms-888
        result.calculatedType      = costItem.getCalculatedType()

        // RefdataValues

        result.costItemStatus      = costItem.costItemStatus?.value
        result.costItemCategory    = costItem.costItemCategory?.value
        result.billingCurrency     = costItem.billingCurrency?.value
        result.costItemElement     = costItem.costItemElement?.value
        result.taxCode             = costItem.taxKey?.taxType?.value ?: costItem.taxCode?.value
        result.costItemElementConfiguration = costItem.costItemElementConfiguration?.value

        // References

        result.owner    = ApiReaderHelper.retrieveOrganisationStubMap(costItem.owner, context) // com.k_int.kbplus.Org
        result.sub      = ApiReaderHelper.requestSubscriptionStub(costItem.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
        //result.subPkg   = ApiReaderHelper.resolveSubscriptionPackageStub(costItem.subPkg, ApiReaderHelper.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
        result.issueEntitlement = ApiReaderHelper.retrieveIssueEntitlementMap(costItem.issueEntitlement, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
        result.order    = ApiReaderHelper.retrieveOrderMap(costItem.order) // com.k_int.kbplus.Order
        result.invoice  = ApiReaderHelper.retrieveInvoiceMap(costItem.invoice)
        result.surveyOrg = costItem?.surveyOrg ?: null

        return ApiToolkit.cleanUp(result, true, true)
    }
}


