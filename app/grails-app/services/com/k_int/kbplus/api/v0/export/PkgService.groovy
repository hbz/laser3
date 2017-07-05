package com.k_int.kbplus.api.v0.export

import groovy.util.logging.Log4j

@Log4j
class PkgService {

    ExportHelperService exportHelperService

    /**
     *
     * @param com.k_int.kbplus.Package pkg
     * @param allowedAddressTypes
     * @param allowedContactTypes
     * @return
     */
    def resolvePackage(com.k_int.kbplus.Package pkg, allowedAddressTypes, allowedContactTypes) {
        def result = [:]

        result.id               = pkg.id
        result.autoAccept       = pkg.autoAccept
        result.cancellationAllowances = pkg.cancellationAllowances
        result.dateCreated      = pkg.dateCreated
        result.endDate          = pkg.endDate
        result.forumId          = pkg.forumId
        result.identifier       = pkg.identifier
        result.impId            = pkg.impId
        result.lastUpdated      = pkg.lastUpdated
        result.name             = pkg.name
        result.identifier       = pkg.identifier
        result.vendorURL        = pkg.vendorURL
        result.sortName         = pkg.sortName
        result.startDate        = pkg.startDate

        // RefdataValues
        result.packageType      = pkg.packageType?.value
        result.packageStatus    = pkg.packageStatus?.value
        result.packageListStatus = pkg.packageListStatus?.value
        result.breakable        = pkg.breakable?.value
        result.consistent       = pkg.consistent?.value
        result.fixed            = pkg.fixed?.value
        result.isPublic         = pkg.isPublic?.value
        result.packageScope     = pkg.packageScope?.value

        // References
        result.documents        = exportHelperService.resolveDocuments(pkg.documents) // com.k_int.kbplus.DocContext
        result.identifiers      = exportHelperService.resolveIdentifiers(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.license          = exportHelperService.resolveLicenseStub(pkg.license) // com.k_int.kbplus.License
        result.nominalPlatform  = exportHelperService.resolvePlatform(pkg.nominalPlatform) // com.k_int.kbplus.Platform
        result.organisations    = exportHelperService.resolveOrgLinks(pkg.orgs, exportHelperService.IGNORE_PACKAGE) // com.k_int.kbplus.OrgRole
        // TODO
        result.persons          = exportHelperService.resolvePrsLinks(
                pkg.prsLinks, allowedAddressTypes, allowedContactTypes, true, false
        ) // com.k_int.kbplus.PersonRole

        result.subscriptions    = exportHelperService.resolveSubscriptionPackageStubs(pkg.subscriptions ,exportHelperService.SUBPKG_SUBSCRIPTION) // com.k_int.kbplus.SubscriptionPackage
        result.tipps            = exportHelperService.resolveTipps(pkg.tipps) // com.k_int.kbplus.TitleInstancePackagePlatform

        return exportHelperService.cleanUp(result, true, true)
    }
}
