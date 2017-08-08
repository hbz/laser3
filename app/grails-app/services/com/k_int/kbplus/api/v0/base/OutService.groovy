package com.k_int.kbplus.api.v0.base

import com.k_int.kbplus.*
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class OutService {

    OutHelperService outHelperService

    /**
     * @param com.k_int.kbplus.License lic
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def exportLicense(License lic, def ignoreRelation, Org context){
        def result = [:]

        lic = GrailsHibernateUtil.unwrapIfProxy(lic)

        result.globalUID        = lic.globalUID
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
        result.properties       = outHelperService.resolveCustomProperties(lic.customProperties) // com.k_int.kbplus.LicenseCustomProperty
        result.documents        = outHelperService.resolveDocuments(lic.documents) // com.k_int.kbplus.DocContext
        result.onixplLicense    = outHelperService.resolveOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != outHelperService.IGNORE_ALL) {
            if (ignoreRelation != outHelperService.IGNORE_SUBSCRIPTION) {
                result.subscriptions = outHelperService.resolveStubs(lic.subscriptions, outHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            }
            if (ignoreRelation != outHelperService.IGNORE_LICENSE) {
                result.organisations = outHelperService.resolveOrgLinks(lic.orgLinks, outHelperService.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
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
        return outHelperService.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.Org org
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def exportOrganisation(Org org, Org context) {
        def result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

        result.globalUID    = org.globalUID
        result.comment      = org.comment
        result.name         = org.name
        result.scope        = org.scope
        result.shortcode    = org.shortcode

        // RefdataValues

        result.sector       = org.sector?.value
        result.type         = org.orgType?.value
        result.status       = org.status?.value

        // References

        result.addresses    = outHelperService.resolveAddresses(org.addresses, outHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Address
        result.contacts     = outHelperService.resolveContacts(org.contacts, outHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = outHelperService.resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.persons      = outHelperService.resolvePrsLinks(
                org.prsLinks, outHelperService.NO_CONSTRAINT, outHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole

        result.properties   = outHelperService.resolveProperties(org, context) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        return outHelperService.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.Package pkg
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def exportPackage(com.k_int.kbplus.Package pkg, Org context) {
        def result = [:]

        pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

        result.globalUID        = pkg.globalUID
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

        result.documents        = outHelperService.resolveDocuments(pkg.documents) // com.k_int.kbplus.DocContext
        result.identifiers      = outHelperService.resolveIdentifiers(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.license          = outHelperService.resolveLicenseStub(pkg.license, context) // com.k_int.kbplus.License
        result.nominalPlatform  = outHelperService.resolvePlatform(pkg.nominalPlatform) // com.k_int.kbplus.Platform
        result.organisations    = outHelperService.resolveOrgLinks(pkg.orgs, outHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
        result.subscriptions    = outHelperService.resolveSubscriptionPackageStubs(pkg.subscriptions, outHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
        result.tipps            = outHelperService.resolveTipps(pkg.tipps, outHelperService.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

        // Ignored
        /*
        result.persons          = exportHelperService.resolvePrsLinks(
                pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return outHelperService.cleanUp(result, true, true)
    }


    /**
     * @param com.k_int.kbplus.Subscription sub
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def exportSubscription(Subscription sub, Org context){
        def result = [:]

        sub = GrailsHibernateUtil.unwrapIfProxy(sub)

        result.globalUID            = sub.globalUID
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

        result.documents        = outHelperService.resolveDocuments(sub.documents) // com.k_int.kbplus.DocContext
        result.derivedSubscriptions = outHelperService.resolveStubs(sub.derivedSubscriptions, outHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
        result.identifiers      = outHelperService.resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf       = outHelperService.resolveSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
        result.license          = outHelperService.resolveLicense(sub.owner, outHelperService.IGNORE_ALL, context) // com.k_int.kbplus.Lice
        result.organisations    = outHelperService.resolveOrgLinks(sub.orgRelations, outHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole
        result.properties       = outHelperService.resolveCustomProperties(sub.customProperties) // com.k_int.kbplus.SubscriptionCustomProperty

        result.packages = outHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

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

        return outHelperService.cleanUp(result, true, true)
    }
}
