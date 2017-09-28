package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.TitleInstancePackagePlatform
import com.k_int.kbplus.api.v0.base.OutHelperService
import com.k_int.kbplus.api.v0.base.OutService
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class IssueEntitlementService {

    OutService outService
    PkgService pkgService
    SubscriptionService subscriptionService

    /**
     * @return xxx | BAD_REQUEST | PRECONDITION_FAILED
     */
    def findSubscriptionPackageBy(String query, String value) {
        def result

        def queries = query.split(",")
        def values  = value.split(",")
        if (queries.size() != 2 || values.size() != 2) {
            return MainService.BAD_REQUEST
        }

        def sub = subscriptionService.findSubscriptionBy(queries[0].trim(), values[0].trim())
        def pkg = pkgService.findPackageBy(queries[1].trim(), values[1].trim())

        if (sub instanceof Subscription && pkg instanceof Package) {
            result = SubscriptionPackage.findAllBySubscriptionAndPkg(sub, pkg)
            result = result.size() == 1 ? result.get(0) : MainService.PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getIssueEntitlements(SubscriptionPackage subPkg, User user, Org context){
        def hasAccess  = false
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

        hasAccess = true // TODO remove !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        def result = []
        if (hasAccess) {
            result = outService.exportIssueEntitlements(subPkg,  OutHelperService.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
