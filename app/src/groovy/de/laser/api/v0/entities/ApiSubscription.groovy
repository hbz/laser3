package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.Constants
import de.laser.api.v0.ApiReader
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiSubscription {

    /**
     * @return Subscription | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findSubscriptionBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Subscription.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Subscription.findAllWhere(globalUID: value)
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
    static getSubscription(Subscription sub, Org context, boolean hasAccess){
        def result = []

        if (! hasAccess) {
            if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIPTION_CONSORTIA, context)) {
                hasAccess = true
            }
            else if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIBER, context)) {
                hasAccess = true
            }
            else if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIBER_CONS, context)) {
                hasAccess = true
            }
        }

        if (hasAccess) {
            result = ApiReader.exportSubscription(sub, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
