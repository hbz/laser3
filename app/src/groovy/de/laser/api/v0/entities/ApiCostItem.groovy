package de.laser.api.v0.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiStubReader
import de.laser.api.v0.ApiToolkit
import de.laser.api.v0.ApiUnsecuredMapReader
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.sql.Timestamp

class ApiCostItem {

    /**
     * @return ApiBox(obj: CostItem | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     */
    static ApiBox findCostItemBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = CostItem.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result.obj = CostItem.findAllWhere(globalUID: value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        if (result.obj instanceof CostItem) {
            result.validateDeletedStatus_2('costItemStatus', RDStore.COST_ITEM_DELETED)
        }
        result
    }

    /**
     * @return boolean
     */
    static boolean calculateAccess(CostItem costItem, Org context) {

        boolean hasAccess = false

        if (costItem.owner?.id == context.id) {
            hasAccess = true
        }

        hasAccess
    }
    /**
     * @return JSON | FORBIDDEN
     */
    static requestCostItem(CostItem costItem, Org context, boolean isInvoiceTool){
        Map<String, Object> result = [:]

        boolean hasAccess = isInvoiceTool || calculateAccess(costItem, context)
        if (hasAccess) {
            result = getCostItemMap(costItem, context, isInvoiceTool)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestCostItemList(Org owner, Org context, boolean isInvoiceTool){
        Collection<Object> result = []

        boolean hasAccess = isInvoiceTool || (owner.id == context.id)
        if (hasAccess) {
            // TODO
            result = CostItem.findAllByOwnerAndCostItemStatusNotEqual(owner, RDStore.COST_ITEM_DELETED).globalUID
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestCostItemListWithTimeStamp(Org owner, Org context, String timestamp, boolean isInvoiceTool){
        Collection<Object> result = []

        boolean hasAccess = isInvoiceTool || (owner.id == context.id)
        if (hasAccess) {
            // TODO
            Timestamp ts= new Timestamp(Long.parseLong(timestamp))
            Date apiDate= new Date(ts.getTime());
            def today = new Date()
            result = CostItem.findAllByOwnerAndLastUpdatedBetweenAndCostItemStatusNotEqual(owner, apiDate, today, RDStore.COST_ITEM_DELETED).globalUID
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */

    static Map<String, Object> getCostItemMap(CostItem costItem, Org context, boolean isInvoiceTool){
        Map<String, Object> result = [:]

        costItem = GrailsHibernateUtil.unwrapIfProxy(costItem)

        result.globalUID           = costItem.globalUID

        result.costInBillingCurrency            = costItem.costInBillingCurrency
        result.costInBillingCurrencyAfterTax    = costItem.costInBillingCurrencyAfterTax
        result.costInLocalCurrency              = costItem.costInLocalCurrency
        result.costInLocalCurrencyAfterTax      = costItem.costInLocalCurrencyAfterTax

        result.costTitle           = costItem.costTitle
        result.costDescription     = costItem.costDescription
        result.currencyRate        = costItem.currencyRate
        result.dateCreated         = ApiToolkit.formatInternalDate(costItem.dateCreated)
        result.datePaid            = ApiToolkit.formatInternalDate(costItem.datePaid)
        result.endDate             = ApiToolkit.formatInternalDate(costItem.endDate)
        result.finalCostRounding   = costItem.finalCostRounding
        result.invoiceDate         = ApiToolkit.formatInternalDate(costItem.invoiceDate)
        result.lastUpdated         = ApiToolkit.formatInternalDate(costItem.lastUpdated)

        result.reference           = costItem.reference
        result.startDate           = ApiToolkit.formatInternalDate(costItem.startDate)
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

        result.owner    = ApiUnsecuredMapReader.getOrganisationStubMap(costItem.owner) // com.k_int.kbplus.Org
        result.sub      = ApiStubReader.requestSubscriptionStub(costItem.sub, context, isInvoiceTool) // com.k_int.kbplus.Subscription // RECURSION ???
        //result.subPkg   = ApiStubReader.resolveSubscriptionPackageStub(costItem.subPkg, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
        result.issueEntitlement = ApiIssueEntitlement.getIssueEntitlementMap(costItem.issueEntitlement, ApiReader.IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
        result.order    = ApiUnsecuredMapReader.getOrderMap(costItem.order) // com.k_int.kbplus.Order
        result.invoice  = ApiUnsecuredMapReader.getInvoiceMap(costItem.invoice)
        result.surveyOrg = costItem?.surveyOrg ?: null

        ApiToolkit.cleanUp(result, true, true)
    }
}


