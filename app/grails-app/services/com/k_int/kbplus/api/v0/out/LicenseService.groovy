package com.k_int.kbplus.api.v0.out

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.MainService
import groovy.util.logging.Log4j

@Log4j
class LicenseService {

    ExportHelperService exportHelperService

    /**
     * @return License | BAD_REQUEST
     */
    def findLicenseBy(String query, String value) {

        switch(query) {
            case 'id':
                return License.findWhere(id: Long.parseLong(value))
                break
            case 'impId':
                return License.findWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @param com.k_int.kbplus.License lic
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def resolveLicense(License lic, def ignoreRelation, Org context){
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

        // TODO support private properties, when implemented
        result.properties       = exportHelperService.resolveCustomProperties(lic.customProperties) // com.k_int.kbplus.LicenseCustomProperty
        result.documents        = exportHelperService.resolveDocuments(lic.documents) // com.k_int.kbplus.DocContext
        result.onixplLicense    = exportHelperService.resolveOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != exportHelperService.IGNORE_ALL) {
            if (ignoreRelation != exportHelperService.IGNORE_SUBSCRIPTION) {
                result.subscriptions = exportHelperService.resolveStubs(lic.subscriptions, exportHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            }
            if (ignoreRelation != exportHelperService.IGNORE_LICENSE) {
                result.organisations = exportHelperService.resolveOrgLinks(lic.orgLinks, exportHelperService.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
            }
        }

        // Ignored

        //result.incomingLinks    = exportHelperService.resolveLinks(lic.incomingLinks) // com.k_int.kbplus.Link
        //result.outgoinglinks    = exportHelperService.resolveLinks(lic.outgoinglinks) // com.k_int.kbplus.Link
        //result.packages         = exportHelperService.resolveStubs(lic.pkgs, exportHelperService.PACKAGE_STUB) // com.k_int.kbplus.Package
        /*result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return exportHelperService.cleanUp(result, true, true)
    }
}
