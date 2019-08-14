package de.laser.api.v0.entities

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionPackage
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
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
            result = ApiToolkit.checkPreconditionFailed(result)
        }

        result
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getIssueEntitlements(SubscriptionPackage subPkg, Org context, boolean hasAccess){
        Collection<Object> result = []

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
            result = ApiReaderHelper.retrieveIssueEntitlementCollection(subPkg, ApiReaderHelper.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        // this is different to other Api<x>.get<x>-methods;
        // result may be null here
        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveIssueEntitlementMap(IssueEntitlement ie, def ignoreRelation, Org context) {
        def result = [:]
        if (! ie) {
            return null
        }

        result.globalUID        = ie.globalUID
        result.accessStartDate  = ie.accessStartDate
        result.accessEndDate    = ie.accessEndDate
        result.startDate        = ie.startDate
        result.startVolume      = ie.startVolume
        result.startIssue       = ie.startIssue
        result.endDate          = ie.endDate
        result.endVolume        = ie.endVolume
        result.endIssue         = ie.endIssue
        result.embargo          = ie.embargo
        result.coverageDepth    = ie.coverageDepth
        result.coverageNote     = ie.coverageNote
        result.ieReason         = ie.ieReason
        result.coreStatusStart  = ie.coreStatusStart
        result.coreStatusEnd    = ie.coreStatusEnd

        // RefdataValues
        result.coreStatus       = ie.coreStatus?.value
        result.medium           = ie.medium?.value
        //result.status           = ie.status?.value // legacy; not needed ?

        // References
        if (ignoreRelation != ApiReaderHelper.IGNORE_ALL) {
            if (ignoreRelation == ApiReaderHelper.IGNORE_SUBSCRIPTION_AND_PACKAGE) {
                result.tipp = ApiReaderHelper.retrieveTippMap(ie.tipp, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform
            }
            else {
                if (ignoreRelation != ApiReaderHelper.IGNORE_TIPP) {
                    result.tipp = ApiReaderHelper.retrieveTippMap(ie.tipp, ApiReaderHelper.IGNORE_NONE, context)
                    // com.k_int.kbplus.TitleInstancePackagePlatform
                }
                if (ignoreRelation != ApiReaderHelper.IGNORE_SUBSCRIPTION) {
                    result.subscription = ApiReaderHelper.requestSubscriptionStub(ie.subscription, context)
                    // com.k_int.kbplus.Subscription
                }
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
}
