package com.k_int.kbplus.api.v0.out

import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.api.v0.MainService
import groovy.util.logging.Log4j

@Log4j
class PkgService {

    ExportHelperService exportHelperService


    /**
     * @return Package | BAD_REQUEST
     */
    def findPackageBy(String query, String value) {

        switch(query) {
            case 'id':
                return Package.findWhere(id: Long.parseLong(value))
                break
            case 'identifier':
                return Package.findWhere(identifier: value) // != identifiers
                break
            case 'impId':
                return Package.findWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @param com.k_int.kbplus.Package pkg
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolvePackage(com.k_int.kbplus.Package pkg, Org context) {
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
        result.license          = exportHelperService.resolveLicenseStub(pkg.license, context) // com.k_int.kbplus.License
        result.nominalPlatform  = exportHelperService.resolvePlatform(pkg.nominalPlatform) // com.k_int.kbplus.Platform
        result.organisations    = exportHelperService.resolveOrgLinks(pkg.orgs, exportHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
        result.subscriptions    = exportHelperService.resolveSubscriptionPackageStubs(pkg.subscriptions, exportHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
        result.tipps            = exportHelperService.resolveTipps(pkg.tipps, exportHelperService.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

        // Ignored
        /*
        result.persons          = exportHelperService.resolvePrsLinks(
                pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return exportHelperService.cleanUp(result, true, true)
    }
}
