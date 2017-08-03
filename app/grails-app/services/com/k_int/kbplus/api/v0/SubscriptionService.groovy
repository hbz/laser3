package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.out.OutService
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class SubscriptionService {

    OutService outService

    /**
     * @return Subscription | BAD_REQUEST
     */
    def findSubscriptionBy(String query, String value) {

        switch(query) {
            case 'id':
                return Subscription.findWhere(id: Long.parseLong(value))
                break
            case 'identifier':
                return Subscription.findWhere(identifier: value) // != identifiers
                break
            case 'impId':
                return Subscription.findWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getSubscription(Subscription sub, User user, Org context){
        def hasAccess = false

        sub.orgRelations.each{ orgRole ->
            if(orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        def result = []
        if (hasAccess) {
            result = outService.exportSubscription(sub, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
