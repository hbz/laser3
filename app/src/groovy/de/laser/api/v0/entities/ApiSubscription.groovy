package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.api.v0.ApiReaderHelper
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
     * @return boolean
     */
    static boolean calculateAccess(Subscription sub, Org context, boolean hasAccess) {

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

        hasAccess
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getSubscription(Subscription sub, Org context, boolean hasAccess){
        Map<String, Object> result = [:]
        hasAccess = calculateAccess(sub, context, hasAccess)

        if (hasAccess) {
            result = ApiReader.retrieveSubscriptionMap(sub, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getSubscriptionList(Org owner, Org context, boolean hasAccess){
        Collection<Object> result = []

        List<Subscription> available = Subscription.executeQuery(
                'SELECT sub FROM Subscription sub JOIN sub.orgRelations oo WHERE oo.org = :owner AND oo.roleType in (:roles ) AND sub.status != :del' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER],
                        del:   RDStore.SUBSCRIPTION_DELETED
                ]
        )

        available.each { sub ->
            //if (calculateAccess(sub, context, hasAccess)) {
                println sub.id + ' ' + sub.name
                result.add(ApiReaderHelper.requestSubscriptionStub(sub, context, true))
                //result.add([globalUID: sub.globalUID])
            //}
        }

        return (result ? new JSON(result) : null)
    }
}
