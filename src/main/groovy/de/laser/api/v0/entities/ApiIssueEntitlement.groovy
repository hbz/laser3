package de.laser.api.v0.entities

import de.laser.IssueEntitlement
import de.laser.IssueEntitlementGroup
import de.laser.IssueEntitlementGroupItem
import de.laser.Org
import de.laser.api.v0.*

/**
 * An API representation of a {@link IssueEntitlement}, currently unused
 */
class ApiIssueEntitlement {

    /**
     * @return xxx | BAD_REQUEST | PRECONDITION_FAILED
     */
//    static findSubscriptionPackageBy(String query, String value) {
//        def result
//
//        def queries = query.split(",")
//        def values  = value.split(",")
//        if (queries.size() != 2 || values.size() != 2) {
//            return Constants.HTTP_BAD_REQUEST
//        }
//
//        def sub = ApiSubscription.findSubscriptionBy(queries[0].trim(), values[0].trim())
//        def pkg = ApiPkg.findPackageBy(queries[1].trim(), values[1].trim())
//
//        if (sub instanceof Subscription && pkg instanceof Package) {
//            result = SubscriptionPackage.findAllBySubscriptionAndPkg(sub, pkg)
//            result = ApiToolkit.checkPreconditionFailed(result)
//        }
//
//        result
//    }

    /**
     * @return JSON | FORBIDDEN
     */
//    static requestIssueEntitlements(SubscriptionPackage subPkg, Org context){
//        Collection<Object> result = []
//
//        boolean hasAccess = false
//        if (! hasAccess) {
//            boolean hasAccess2 = false
//            // TODO
//            subPkg.subscription.orgRelations.each{ orgRole ->
//                if(orgRole.getOrg().id == context?.id) {
//                    hasAccess2 = true
//                }
//            }
//            subPkg.pkg.orgs.each{ orgRole ->
//                if(orgRole.getOrg().id == context?.id) {
//                    hasAccess = hasAccess2
//                }
//            }
//        }
//
//        if (hasAccess) {
//            result = ApiCollectionReader.getIssueEntitlementCollection(subPkg, ApiReader.IGNORE_NONE, context) // TODO check orgRole.roleType
//        }
//
//        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
//    }

    /**
     * Assembles the given issue entitlement attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param ie the issue entitlement (presented as domain object or as map) which should be output
     * @param ignoreRelation should a relation the issue entitlement has being ignored and thus not output?
     * @return Map<String, Object>
     */
    static Map<String, Object> getIssueEntitlementMap(IssueEntitlement ie, def ignoreRelation, Org context) {
        Map<String, Object> result = [:]
        if (! ie) {
            return null
        }

        result.globalUID        = ie.globalUID
        result.name             = ie.name
        result.accessStartDate  = ApiToolkit.formatInternalDate(ie.accessStartDate)
        result.accessEndDate    = ApiToolkit.formatInternalDate(ie.accessEndDate)
        result.lastUpdated      = ApiToolkit.formatInternalDate(ie.lastUpdated)

        // RefdataValues
        result.medium           = ie.medium ? ie.medium.value : ie.tipp.medium?.value
        result.status           = ie.status?.value

        result.perpetualAccessBySub 	= ApiStubReader.requestSubscriptionStub(ie.perpetualAccessBySub, context)

        result.coverages        = ApiCollectionReader.getCoverageCollection(ie.coverages) // de.laser.IssueEntitlementCoverage
        result.priceItems       = ApiCollectionReader.getPriceItemCollection(ie.priceItems) //de.laser.PriceItem with pi.ie != null

        // References
        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation == ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE) {
                result.tipp = ApiMapReader.getTippMap(ie.tipp, ApiReader.IGNORE_ALL, context) // de.laser.TitleInstancePackagePlatform
            }
            else {
                if (ignoreRelation != ApiReader.IGNORE_TIPP) {
                    result.tipp = ApiMapReader.getTippMap(ie.tipp, ApiReader.IGNORE_SUBSCRIPTION, context)
                    // de.laser.TitleInstancePackagePlatform
                }
                if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                    result.subscription = ApiStubReader.requestSubscriptionStub(ie.subscription, context)
                    // com.k_int.kbplus.Subscription
                }
            }
        }

        ApiToolkit.cleanUp(result, true, true)
    }

    static Map<String, Object> getTitleGroupMap(IssueEntitlementGroup titleGroup, Org context) {
        Map<String, Object> result = [:]
        result.name = titleGroup.name
        result.issueEntitlements = titleGroup.items.collect { IssueEntitlementGroupItem item -> getIssueEntitlementMap(item.ie, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, context) }
        ApiToolkit.cleanUp(result, true, true)
    }
}
