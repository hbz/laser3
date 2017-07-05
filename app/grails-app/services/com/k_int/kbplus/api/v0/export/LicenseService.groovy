package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import groovy.util.logging.Log4j

@Log4j
class LicenseService {

    ExportHelperService exportHelperService

    /**
     *
     * @param com.k_int.kbplus.License lic
     * @param allowedAddressTypes
     * @param allowedContactTypes
     * @return
     */
    def resolveLicense(License lic, allowedAddressTypes, allowedContactTypes){
        def result = [:]

        result.id               = lic.id
        result.contact          = lic.contact
        result.dateCreated      = lic.dateCreated
        result.endDate          = lic.endDate
        result.impId            = lic.impId
        result.lastmod          = lic.lastmod
        result.lastUpdated      = lic.lastUpdated
        result.licenseUrl       = lic.licenseUrl
        result.licensorRef      = lic.licensorRef
        result.licenseeRef      = lic.licenseeRef
        result.licenseType      = lic.licenseType
        result.licenseStatus    = lic.licenseStatus
        result.noticePeriod     = lic.noticePeriod
        result.reference        = lic.reference
        result.startDate        = lic.startDate
        result.sortableReference= lic.sortableReference

        // RefdataValues
        result.isPublic         = lic.isPublic?.value
        result.licenseCategory  = lic.licenseCategory?.value
        result.status           = lic.status?.value
        result.type             = lic.type?.value

        // References
        result.properties       = exportHelperService.resolveCustomProperties(lic.customProperties) // com.k_int.kbplus.LicenseCustomProperty
        result.documents        = exportHelperService.resolveDocuments(lic.documents) // com.k_int.kbplus.DocContext
        result.incomingLinks    = exportHelperService.resolveLinks(lic.incomingLinks) // com.k_int.kbplus.Link
        result.onixplLicense    = exportHelperService.resolveOnixplLicense(lic.onixplLicense) // com.k_int.kbplus.OnixplLicense
        result.outgoinglinks    = exportHelperService.resolveLinks(lic.outgoinglinks) // com.k_int.kbplus.Link
        result.organisations    = exportHelperService.resolveOrgLinks(lic.orgLinks, exportHelperService.IGNORE_LICENSE) // com.k_int.kbplus.OrgRole

        result.packages         = exportHelperService.resolveStubs(lic.pkgs, exportHelperService.PACKAGE_STUB) // com.k_int.kbplus.Package
        result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, allowedAddressTypes, allowedContactTypes, true, false
        ) // com.k_int.kbplus.PersonRole
        result.subscriptions    = exportHelperService.resolveStubs(lic.subscriptions, exportHelperService.SUBSCRIPTION_STUB) // com.k_int.kbplus.Subscription

        return exportHelperService.cleanUp(result, true, true)
    }
}
