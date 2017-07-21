package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import groovy.util.logging.Log4j

@Log4j
class SubscriptionService {

    ExportHelperService exportHelperService

    static def findSubscription(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Subscription.findWhere(id: Long.parseLong(value))
        }
        else if('identifier'.equalsIgnoreCase(query)) {
            obj = Subscription.findWhere(identifier: value)
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = Subscription.findWhere(impId: value)
        }
        else {
            obj = ApiService.BAD_REQUEST
        }
        obj
    }

    /**
     * @param com.k_int.kbplus.Subscription sub
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolveSubscription(Subscription sub, Org context){
        def result = [:]

        result.id                   = sub.id
        result.cancellationAllowances = sub.cancellationAllowances
        result.dateCreated          = sub.dateCreated
        result.endDate              = sub.endDate
        result.identifier           = sub.identifier
        result.lastUpdated          = sub.lastUpdated
        result.manualRenewalDate    = sub.manualRenewalDate
        result.name                 = sub.name
        result.noticePeriod         = sub.noticePeriod
        result.startDate            = sub.startDate

        // RefdataValues

        result.isSlaved     = sub.isSlaved?.value
        result.isPublic     = sub.isPublic?.value
        result.status       = sub.status?.value
        result.type         = sub.type?.value

        // References

        result.documents        = exportHelperService.resolveDocuments(sub.documents) // com.k_int.kbplus.DocContext
        result.derivedSubscriptions = exportHelperService.resolveStubs(sub.derivedSubscriptions, exportHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
        result.identifiers      = exportHelperService.resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf       = exportHelperService.resolveSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
        result.license          = exportHelperService.resolveLicense(sub.owner, exportHelperService.IGNORE_ALL, context) // com.k_int.kbplus.Lice
        result.organisations    = exportHelperService.resolveOrgLinks(sub.orgRelations, exportHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole
        result.properties       = exportHelperService.resolveCustomProperties(sub.customProperties) // com.k_int.kbplus.SubscriptionCustomProperty

        result.packages = exportHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

        // Ignored

        //result.packages = exportHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage
        //result.issueEntitlements = exportHelperService.resolveIssueEntitlements(sub.issueEntitlements, context) // com.k_int.kbplus.IssueEntitlement
        //result.packages = exportHelperService.resolveSubscriptionPackageStubs(sub.packages, exportHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
        /*
        result.persons      = exportHelperService.resolvePrsLinks(
                sub.prsLinks,  true, true, context
        ) // com.k_int.kbplus.PersonRole
        */
        // result.costItems    = exportHelperService.resolveCostItems(sub.costItems) // com.k_int.kbplus.CostItem

        return exportHelperService.cleanUp(result, true, true)
    }
}
