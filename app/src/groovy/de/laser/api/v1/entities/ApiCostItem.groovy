package de.laser.api.v1.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.auth.User
import de.laser.api.v1.ApiReaderHelper
import de.laser.api.v1.ApiReader
import de.laser.domain.Constants
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
            case 'identifier':
                result = CostItem.findAllWhere(identifier: value)
                break
            case 'impId':
                result = CostItem.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new CostItem(), value)
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
    static getCostItem(CostItem costItem, User user, Org context){
        def result = []
        def hasAccess = ApiReader.isDataManager(user)

        if (hasAccess) {
            result = ApiReader.exportCostItem(costItem, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    static getCostItems(User user, Org context){
        def result = []
        def hasAccess = de.laser.api.v1.ApiReader.isDataManager(user)

        if (hasAccess) {
            result = de.laser.api.v1.ApiReader.exportCostItems(ApiReaderHelper.IGNORE_NONE, context)
        }

        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }
}


