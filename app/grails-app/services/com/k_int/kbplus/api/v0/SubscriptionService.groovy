package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.base.OutService
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class SubscriptionService {

    OutService outService

    /**
     * @return Subscription | BAD_REQUEST | PRECONDITION_FAILED
     */
    def findSubscriptionBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Subscription.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Subscription.findAllWhere(globalUID: value)
                break
            case 'identifier':
                result = Subscription.findAllWhere(identifier: value)
                break
            case 'impId':
                result = Subscription.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Subscription(), value)
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
    def getSubscription(Subscription sub, User user, Org context){
        def result = []
        def hasAccess = outService.isDataManager(user)

        if (! hasAccess) {
            sub.orgRelations.each { orgRole ->
                if (orgRole.getOrg().id == context?.id) {
                    hasAccess = true
                }
            }
        }

        if (hasAccess) {
            result = outService.exportSubscription(sub, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
