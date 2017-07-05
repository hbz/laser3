package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import groovy.util.logging.Log4j

@Log4j
class SubscriptionService {

    ExportHelperService exportHelperService

    /**
     *
     * @param com.k_int.kbplus.Subscription sub
     * @param allowedAddressTypes
     * @param allowedContactTypes
     * @return
     */
    def resolveSubscription(Subscription sub, allowedAddressTypes, allowedContactTypes){
        def result = [:]

        result.id                       = sub.id
        result.cancellationAllowances   = sub.cancellationAllowances
        result.dateCreated              = sub.dateCreated
        result.endDate                  = sub.endDate
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
        //TODO .. result.costItems    = exportHelperService.resolveCostItems(sub.costItems) // com.k_int.kbplus.CostItem
        result.documents            = exportHelperService.resolveDocuments(sub.documents) // com.k_int.kbplus.DocContext
        result.derivedSubscriptions = exportHelperService.resolveStubs(sub.derivedSubscriptions, exportHelperService.SUBSCRIPTION_STUB) // com.k_int.kbplus.Subscription

        result.identifiers      = exportHelperService.resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf       = exportHelperService.resolveSubscriptionStub(sub.instanceOf)

        result.issueEntitlements    = exportHelperService.resolveIssueEntitlements(sub.issueEntitlements) // com.k_int.kbplus.IssueEntitlement

        result.license          = exportHelperService.resolveLicenseStub(sub.owner) // com.k_int.kbplus.License
        result.organisations    = exportHelperService.resolveOrgLinks(sub.orgRelations, exportHelperService.IGNORE_SUBSCRIPTION) // com.k_int.kbplus.OrgRole
        result.packages         = exportHelperService.resolveSubscriptionPackageStubs(sub.packages, exportHelperService.SUBPKG_PACKAGE) // com.k_int.kbplus.SubscriptionPackage
        result.properties       = exportHelperService.resolveCustomProperties(sub.customProperties) // com.k_int.kbplus.SubscriptionCustomProperty

        result.persons      = exportHelperService.resolvePrsLinks(
                sub.prsLinks, allowedAddressTypes, allowedContactTypes, true, true
        ) // com.k_int.kbplus.PersonRole

        return exportHelperService.cleanUp(result, true, false)
    }
}
