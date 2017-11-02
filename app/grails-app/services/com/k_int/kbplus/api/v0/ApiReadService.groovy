package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserRole
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiReadService {

    ApiReadHelperService apiReadHelperService

    /**
     * @param com.k_int.kbplus.SubscriptionPackage subPkg
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def exportIssueEntitlements(SubscriptionPackage subPkg, def ignoreRelation, Org context){
        def result = []

        def tipps = TitleInstancePackagePlatform.findAllBySubAndPkg(subPkg.subscription, subPkg.pkg)
        tipps.each{ tipp ->
            def ie = IssueEntitlement.findBySubscriptionAndTipp(subPkg.subscription, tipp)
            if (ie) {
                result << apiReadHelperService.resolveIssueEntitlement(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
            }
        }
        return apiReadHelperService.cleanUp(result, true, true)
    }

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

        result.identifiers      = apiReadHelperService.resolveIdentifiers(lic.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.properties       = apiReadHelperService.resolveProperties(lic, context)  // com.k_int.kbplus.(LicenseCustomProperty, LicensePrivateProperty)
        result.documents        = apiReadHelperService.resolveDocuments(lic.documents) // com.k_int.kbplus.DocContext
        result.onixplLicense    = apiReadHelperService.resolveOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != apiReadHelperService.IGNORE_ALL) {
            if (ignoreRelation != apiReadHelperService.IGNORE_SUBSCRIPTION) {
                result.subscriptions = apiReadHelperService.resolveStubs(lic.subscriptions, apiReadHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            }
            if (ignoreRelation != apiReadHelperService.IGNORE_LICENSE) {
                result.organisations = apiReadHelperService.resolveOrgLinks(lic.orgLinks, apiReadHelperService.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
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
        return apiReadHelperService.cleanUp(result, true, true)
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

        result.addresses    = apiReadHelperService.resolveAddresses(org.addresses, apiReadHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Address
        result.contacts     = apiReadHelperService.resolveContacts(org.contacts, apiReadHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = apiReadHelperService.resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.persons      = apiReadHelperService.resolvePrsLinks(
                org.prsLinks, apiReadHelperService.NO_CONSTRAINT, apiReadHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole

        result.properties   = apiReadHelperService.resolveProperties(org, context) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        return apiReadHelperService.cleanUp(result, true, true)
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

        result.documents        = apiReadHelperService.resolveDocuments(pkg.documents) // com.k_int.kbplus.DocContext
        result.identifiers      = apiReadHelperService.resolveIdentifiers(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.license          = apiReadHelperService.resolveLicenseStub(pkg.license, context) // com.k_int.kbplus.License
        result.nominalPlatform  = apiReadHelperService.resolvePlatform(pkg.nominalPlatform) // com.k_int.kbplus.Platform
        result.organisations    = apiReadHelperService.resolveOrgLinks(pkg.orgs, apiReadHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
        result.subscriptions    = apiReadHelperService.resolveSubscriptionPackageStubs(pkg.subscriptions, apiReadHelperService.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
        result.tipps            = apiReadHelperService.resolveTipps(pkg.tipps, apiReadHelperService.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

        // Ignored
        /*
        result.persons          = exportHelperService.resolvePrsLinks(
                pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return apiReadHelperService.cleanUp(result, true, true)
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

        result.documents        = apiReadHelperService.resolveDocuments(sub.documents) // com.k_int.kbplus.DocContext
        result.derivedSubscriptions = apiReadHelperService.resolveStubs(sub.derivedSubscriptions, apiReadHelperService.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
        result.identifiers      = apiReadHelperService.resolveIdentifiers(sub.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf       = apiReadHelperService.resolveSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
        result.license          = apiReadHelperService.resolveLicense(sub.owner, apiReadHelperService.IGNORE_ALL, context) // com.k_int.kbplus.Lice
        result.organisations    = apiReadHelperService.resolveOrgLinks(sub.orgRelations, apiReadHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole
        result.properties       = apiReadHelperService.resolveCustomProperties(sub.customProperties) // com.k_int.kbplus.SubscriptionCustomProperty

        // TODO refactoring with issueEntitlementService
        result.packages = apiReadHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

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

        return apiReadHelperService.cleanUp(result, true, true)
    }

    // ################### HELPER ###################

    def isDataManager(User user) {
        def role = UserRole.findAllWhere(user: user, role: Role.findByAuthority('ROLE_API_DATAMANAGER'))
        return ! role.isEmpty()
    }
}
