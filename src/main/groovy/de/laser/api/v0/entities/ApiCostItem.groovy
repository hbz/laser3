package de.laser.api.v0.entities

import de.laser.finance.CostItem
import de.laser.Org
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.sql.Timestamp

/**
 * An API representation of a {@link CostItem}
 */
class ApiCostItem {

    /**
     * Locates the given {@link CostItem} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID}
     * @param the identifier value
     * @return {@link ApiBox}(obj: CostItem | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
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
     * Checks if the requesting institution can access to the given cost item
     * @param costItem the {@link CostItem} to which access is being requested
     * @param context the institution ({@link Org}) requesting access
     * @return true if the access is granted, false otherwise
     */
    static boolean calculateAccess(CostItem costItem, Org context) {

        boolean hasAccess = false

        if (costItem.owner?.id == context.id) {
            hasAccess = true
        }

        hasAccess
    }

    /**
     * Checks if the given institution can access the given cost item. The cost item
     * is returned in case of success
     * @param costItem the {@link CostItem} whose details should be retrieved
     * @param context the institution ({@link Org}) requesting the cost item
     * @param isInvoiceTool is the request done by the hbz invoice tool?
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
     * Checks if the requesting institution can access the cost item list of the requested institution.
     * The list of cost items is returned in case of success
     * @param owner the institution whose cost items should be retrieved
     * @param context the institution who requests the list
     * @param isInvoiceTool is the hbz invoice tool doing the request?
     * @return JSON | FORBIDDEN
     * @see Org
     */
    static requestCostItemList(Org owner, Org context, boolean isInvoiceTool){
        Collection<Object> result = []

        boolean hasAccess = isInvoiceTool || (owner.id == context.id)
        if (hasAccess) {
            // TODO
            if(isInvoiceTool){
                result = CostItem.findAllByOwner(owner).globalUID
            }else {
                result = CostItem.findAllByOwnerAndCostItemStatusNotEqual(owner, RDStore.COST_ITEM_DELETED).globalUID
            }
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Checks if the requesting institution can access the cost item list of the requested institution.
     * The list of cost items is returned in case of success; the list contains only items which have
     * been updated since the given timestamp
     * @param owner the institution whose cost items should be retrieved
     * @param context the institution who requests the list
     * @param timestamp the point of time since when the cost items have been updated
     * @param isInvoiceTool is the hbz invoice tool doing the request?
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
            if(isInvoiceTool) {
                result = CostItem.findAllByOwnerAndLastUpdatedBetween(owner, apiDate, today).globalUID
            }else{
                result = CostItem.findAllByOwnerAndLastUpdatedBetweenAndCostItemStatusNotEqual(owner, apiDate, today, RDStore.COST_ITEM_DELETED).globalUID
            }
            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Assembles the given cost item attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param costItem the {@link CostItem} which should be output
     * @param context the institution ({@link Org}) requesting
     * @param isInvoiceTool is the hbz invoice tool doing the request?
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
        result.finalCostRounding   = costItem.finalCostRounding ? 'Yes' : 'No'
        result.invoiceDate         = ApiToolkit.formatInternalDate(costItem.invoiceDate)
        result.lastUpdated         = ApiToolkit.formatInternalDate(costItem.lastUpdated)

        result.reference           = costItem.reference
        result.startDate           = ApiToolkit.formatInternalDate(costItem.startDate)
        result.taxRate             = costItem.taxKey?.taxRate ?: ((costItem.taxKey?.taxRate == 0) ? costItem.taxKey?.taxRate : costItem.taxRate)

        result.isVisibleForSubscriber = costItem.isVisibleForSubscriber ? 'Yes' : 'No'

        // erms-888
        result.calculatedType      = costItem._getCalculatedType()

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
        //result.subPkg   = ApiStubReader.resolveSubscriptionPackageStub(costItem.subPkg, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // de.laser.SubscriptionPackage
        result.issueEntitlement = ApiIssueEntitlement.getIssueEntitlementMap(costItem.issueEntitlement, ApiReader.IGNORE_ALL, context) // de.laser.IssueEntitlement
        result.order    = ApiUnsecuredMapReader.getOrderMap(costItem.order) // de.laser.finance.Order
        result.invoice  = ApiUnsecuredMapReader.getInvoiceMap(costItem.invoice)
        result.surveyOrg = costItem?.surveyOrg ?: null

        ApiToolkit.cleanUp(result, true, true)
    }
}


