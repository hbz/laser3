package de.laser.api.v0.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Org
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import grails.converters.JSON

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
            result = ApiReader.retrieveCostItemMap(costItem, context)
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
}


