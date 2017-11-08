package de.laser.api.v0.entities

import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiIssueEntitlement {

    /**
     * @return xxx | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findSubscriptionPackageBy(String query, String value) {
        def result

        def queries = query.split(",")
        def values  = value.split(",")
        if (queries.size() != 2 || values.size() != 2) {
            return Constants.HTTP_BAD_REQUEST
        }

        def sub = ApiSubscription.findSubscriptionBy(queries[0].trim(), values[0].trim())
        def pkg = ApiPkg.findPackageBy(queries[1].trim(), values[1].trim())

        if (sub instanceof Subscription && pkg instanceof Package) {
            result = SubscriptionPackage.findAllBySubscriptionAndPkg(sub, pkg)
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    static getIssueEntitlements(SubscriptionPackage subPkg, User user, Org context){
        def result = []
        def hasAccess = ApiReader.isDataManager(user)

        if (! hasAccess) {
            def hasAccess2 = false
            // TODO
            subPkg.subscription.orgRelations.each{ orgRole ->
                if(orgRole.getOrg().id == context?.id) {
                    hasAccess2 = true
                }
            }
            subPkg.pkg.orgs.each{ orgRole ->
                if(orgRole.getOrg().id == context?.id) {
                    hasAccess = hasAccess2
                }
            }
        }

        if (hasAccess) {
            result = ApiReader.exportIssueEntitlements(subPkg, ApiReaderHelper.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        // this is different to other Api<x>.get<x>-methods;
        // result may be null here
        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }
}
