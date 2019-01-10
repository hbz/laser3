package de.laser.api.v0.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Org
import de.laser.api.v0.ApiReader
import de.laser.helper.Constants
import grails.converters.JSON

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
     * @return grails.converters.JSON | FORBIDDEN
     */
    static getCostItem(CostItem costItem, Org context, boolean hasAccess){
        def result = []

        if (! hasAccess) {
            if (costItem.owner?.id == context.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result = ApiReader.exportCostItem(costItem, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return [] | FORBIDDEN
     */
    static getCostItems(Org owner, Org context, boolean hasAccess){
        def result = []

        if (! hasAccess) {
            if (owner.id == context.id) {
                hasAccess = true
            }
        }
        if (hasAccess) {
            result = ApiReader.exportCostItems(owner, context)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }
}


