package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.CacheService
import de.laser.helper.Constants
import de.laser.helper.RDStore
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiReader {

    static SUPPORTED_FORMATS = [
            'costItem':             [Constants.MIME_APPLICATION_JSON],
            'document':             [],
            'issueEntitlements':    [Constants.MIME_TEXT_PLAIN, Constants.MIME_APPLICATION_JSON],
            'license':              [Constants.MIME_APPLICATION_JSON],
            'onixpl':               [Constants.MIME_APPLICATION_XML],
            'oa2020':               [Constants.MIME_APPLICATION_JSON],
            'oa2020List':           [Constants.MIME_APPLICATION_JSON],
            'organisation':         [Constants.MIME_APPLICATION_JSON],
            'package':              [Constants.MIME_APPLICATION_JSON],
            'refdataList':          [Constants.MIME_APPLICATION_JSON],
            'statistic':            [Constants.MIME_APPLICATION_JSON],
            'statisticList':        [Constants.MIME_APPLICATION_JSON],
            'subscription':         [Constants.MIME_APPLICATION_JSON]
    ]

    static SIMPLE_QUERIES = ['oa2020List', 'refdataList', 'statisticList']

    /**
     * @param com.k_int.kbplus.CostItem costItem
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveCostItemMap(CostItem costItem, Org context){
        def result = [:]

        costItem = GrailsHibernateUtil.unwrapIfProxy(costItem)

        result.globalUID           = costItem.globalUID

        result.costInBillingCurrency            = costItem.costInBillingCurrency
        result.costInBillingCurrencyAfterTax    = costItem.costInBillingCurrencyAfterTax
        result.costInLocalCurrency              = costItem.costInLocalCurrency
        result.costInLocalCurrencyAfterTax      = costItem.costInLocalCurrencyAfterTax

        result.costTitle           = costItem.costTitle
        result.costDescription     = costItem.costDescription
        result.currencyRate        = costItem.currencyRate
        result.dateCreated         = costItem.dateCreated
        result.datePaid            = costItem.datePaid
        result.endDate             = costItem.endDate
        result.finalCostRounding   = costItem.finalCostRounding
        result.invoiceDate         = costItem.invoiceDate
        result.lastUpdated         = costItem.lastUpdated

        result.reference           = costItem.reference
        result.startDate           = costItem.startDate
        result.taxRate             = costItem.taxKey?.taxRate ?: ((costItem.taxKey?.taxRate == 0) ? costItem.taxKey?.taxRate : costItem.taxRate)

        // erms-888
        result.calculatedType      = costItem.getCalculatedType()

        // RefdataValues

        result.costItemStatus      = costItem.costItemStatus?.value
        result.costItemCategory    = costItem.costItemCategory?.value
        result.billingCurrency     = costItem.billingCurrency?.value
        result.costItemElement     = costItem.costItemElement?.value
        result.taxCode             = costItem.taxKey?.taxType?.value ?: costItem.taxCode?.value
        result.costItemElementConfiguration = costItem.costItemElementConfiguration?.value

        // References

        result.owner    = ApiReaderHelper.retrieveOrganisationStubMap(costItem.owner, context) // com.k_int.kbplus.Org
        result.sub      = ApiReaderHelper.requestSubscriptionStub(costItem.sub, context) // com.k_int.kbplus.Subscription // RECURSION ???
        //result.subPkg   = ApiReaderHelper.resolveSubscriptionPackageStub(costItem.subPkg, ApiReaderHelper.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
        result.issueEntitlement = ApiReaderHelper.retrieveIssueEntitlementMap(costItem.issueEntitlement, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.issueEntitlement
        result.order    = ApiReaderHelper.retrieveOrderMap(costItem.order) // com.k_int.kbplus.Order
        result.invoice  = ApiReaderHelper.retrieveInvoiceMap(costItem.invoice)

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.SubscriptionPackage subPkg
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> retrieveIssueEntitlementCollection(SubscriptionPackage subPkg, ignoreRelation, Org context){
        def result = []

        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg', [sub: subPkg.subscription, pkg: subPkg.pkg]
        )
        ieList.each{ ie ->
            result << ApiReaderHelper.retrieveIssueEntitlementMap(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
        }

        /* 0.51
        def tipps = TitleInstancePackagePlatform.findAllByPkg(subPkg.pkg)
        tipps.each{ tipp ->
            def ie = IssueEntitlement.findBySubscriptionAndTipp(subPkg.subscription, tipp)
            if (ie) {
                result << ApiReaderHelper.resolveIssueEntitlement(ie, ignoreRelation, context) // com.k_int.kbplus.IssueEntitlement
            }
        }
        */

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.License lic
     * @param ignoreRelation
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveLicenseMap(License lic, def ignoreRelation, Org context){
        def result = [:]

        lic = GrailsHibernateUtil.unwrapIfProxy(lic)

        result.globalUID        = lic.globalUID
        // removed - result.contact          = lic.contact
        result.dateCreated      = lic.dateCreated
        result.endDate          = lic.endDate
        result.impId            = lic.impId
        // result.lastmod          = lic.lastmod // legacy ?
        result.lastUpdated      = lic.lastUpdated
        // result.licenseUrl       = lic.licenseUrl
        // removed - result.licensorRef      = lic.licensorRef
        // removed - result.licenseeRef      = lic.licenseeRef
        result.licenseType      = lic.licenseType
        result.licenseStatus    = lic.licenseStatus
        //result.noticePeriod     = lic.noticePeriod
        result.reference        = lic.reference
        result.startDate        = lic.startDate
        result.normReference= lic.sortableReference

        // erms-888
        result.calculatedType   = lic.getCalculatedType()

        // RefdataValues

        result.isPublic         = lic.isPublic?.value
        // result.licenseCategory  = lic.licenseCategory?.value // legacy
        result.status           = lic.status?.value
        // result.type             = lic.type?.value

        // References

        result.identifiers      = ApiReaderHelper.retrieveIdentifierCollection(lic.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf       = ApiReaderHelper.requestLicenseStub(lic.instanceOf, context) // com.k_int.kbplus.License
        result.properties       = ApiReaderHelper.retrievePropertyCollection(lic, context)  // com.k_int.kbplus.(LicenseCustomProperty, LicensePrivateProperty)
        result.documents        = ApiReaderHelper.retrieveDocumentCollection(lic.documents) // com.k_int.kbplus.DocContext
        result.onixplLicense    = ApiReaderHelper.requestOnixplLicense(lic.onixplLicense, lic, context) // com.k_int.kbplus.OnixplLicense

        if (ignoreRelation != ApiReaderHelper.IGNORE_ALL) {
            if (ignoreRelation != ApiReaderHelper.IGNORE_SUBSCRIPTION) {
                result.subscriptions = ApiReaderHelper.retrieveStubCollection(lic.subscriptions, ApiReaderHelper.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            }
            if (ignoreRelation != ApiReaderHelper.IGNORE_LICENSE) {
                def allOrgRoles = []

                def licenseeConsortial = OrgRole.findByOrgAndLicAndRoleType(context, lic, RDStore.OR_LICENSEE_CONS)
                // restrict, if context is OR_LICENSEE_CONS for current license
                if (licenseeConsortial) {
                    allOrgRoles.add(licenseeConsortial)
                    allOrgRoles.addAll(
                            OrgRole.executeQuery(
                                    "select oo from OrgRole oo where oo.lic = :lic and oo.roleType not in (:roles)",
                                    [lic: lic, roles: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                            )
                    )
                }
                else {
                    allOrgRoles.addAll(lic.orgLinks)

                    // add derived licenses org roles
                    if (lic.derivedLicenses) {
                        allOrgRoles.addAll(
                                OrgRole.executeQuery(
                                        "select oo from OrgRole oo where oo.lic in (:derived) and oo.roleType in (:roles)",
                                        [derived: lic.derivedLicenses, roles: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                                )
                        )
                    }

                }
                allOrgRoles = allOrgRoles.unique()

                result.organisations = ApiReaderHelper.retrieveOrgLinkCollection(allOrgRoles, ApiReaderHelper.IGNORE_LICENSE, context) // com.k_int.kbplus.OrgRole
            }
        }

        // Ignored

        //result.packages         = exportHelperService.resolveStubs(lic.pkgs, exportHelperService.PACKAGE_STUB) // com.k_int.kbplus.Package
        /*result.persons          = exportHelperService.resolvePrsLinks(
                lic.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.Org org
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveOrganisationMap(Org org, Org context) {
        def result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.comment      = org.comment
        result.name         = org.name
        result.scope        = org.scope
        result.shortname    = org.shortname
        result.sortname     = org.sortname
        result.federalState = org.federalState?.value
        result.country      = org.country?.value
        result.libraryType  = org.libraryType?.value

        //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
        //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

        // RefdataValues

        result.sector       = org.sector?.value
        result.type         = org.orgType?.collect{ it -> it.value }
        result.status       = org.status?.value

        // References

        result.addresses    = ApiReaderHelper.retrieveAddressCollection(org.addresses, ApiReaderHelper.NO_CONSTRAINT) // com.k_int.kbplus.Address
        result.contacts     = ApiReaderHelper.retrieveContactCollection(org.contacts, ApiReaderHelper.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = ApiReaderHelper.retrieveIdentifierCollection(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.persons      = ApiReaderHelper.retrievePrsLinkCollection(
                org.prsLinks, ApiReaderHelper.NO_CONSTRAINT, ApiReaderHelper.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole

        result.properties   = ApiReaderHelper.retrievePropertyCollection(org, context) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @param com.k_int.kbplus.Package pkg
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    @Deprecated
    static Map<String, Object> retrievePackageMap(com.k_int.kbplus.Package pkg, Org context) {
        def result = [:]

        pkg = GrailsHibernateUtil.unwrapIfProxy(pkg)

        result.globalUID        = pkg.globalUID
        result.autoAccept       = pkg.autoAccept
        result.cancellationAllowances = pkg.cancellationAllowances
        result.dateCreated      = pkg.dateCreated
        result.endDate          = pkg.endDate
        result.forumId          = pkg.forumId
        //result.identifier       = pkg.identifier - TODO refactoring legacy
        result.impId            = pkg.impId
        result.gokbId           = pkg.gokbId
        result.lastUpdated      = pkg.lastUpdated
        result.name             = pkg.name
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

        result.documents        = ApiReaderHelper.retrieveDocumentCollection(pkg.documents) // com.k_int.kbplus.DocContext
        result.identifiers      = ApiReaderHelper.retrieveIdentifierCollection(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.license          = ApiReaderHelper.requestLicenseStub(pkg.license, context) // com.k_int.kbplus.License
        result.nominalPlatform  = ApiReaderHelper.retrievePlatformMap(pkg.nominalPlatform) // com.k_int.kbplus.Platform
        result.organisations    = ApiReaderHelper.retrieveOrgLinkCollection(pkg.orgs, ApiReaderHelper.IGNORE_PACKAGE, context) // com.k_int.kbplus.OrgRole
        result.subscriptions    = ApiReaderHelper.retrieveSubscriptionPackageStubCollection(pkg.subscriptions, ApiReaderHelper.IGNORE_PACKAGE, context) // com.k_int.kbplus.SubscriptionPackage
        result.tipps            = ApiReaderHelper.retrieveTippCollection(pkg.tipps, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.TitleInstancePackagePlatform

        // Ignored
        /*
        result.persons          = exportHelperService.resolvePrsLinks(
                pkg.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole
        */
        return ApiToolkit.cleanUp(result, true, true)
    }


    /**
     * @param com.k_int.kbplus.Subscription sub
     * @param com.k_int.kbplus.Org context
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveSubscriptionMap(Subscription sub, Org context){
        def result = [:]

        sub = GrailsHibernateUtil.unwrapIfProxy(sub)

        result.globalUID            = sub.globalUID
        result.cancellationAllowances = sub.cancellationAllowances
        result.dateCreated          = sub.dateCreated
        result.endDate              = sub.endDate
        //result.identifier           = sub.identifier // TODO: refactor legacy
        result.lastUpdated          = sub.lastUpdated
        result.manualCancellationDate = sub.manualCancellationDate
        result.manualRenewalDate    = sub.manualRenewalDate
        result.name                 = sub.name
        result.noticePeriod         = sub.noticePeriod
        result.startDate            = sub.startDate

        // erms-888
        result.calculatedType       = sub.getCalculatedType()

        // RefdataValues

        result.form         = sub.form?.value
        result.isSlaved     = sub.isSlaved?.value
        //result.isPublic     = sub.isPublic?.value // legacy
        result.resource     = sub.resource?.value
        result.status       = sub.status?.value
        result.type         = sub.type?.value

        // References

        result.documents            = ApiReaderHelper.retrieveDocumentCollection(sub.documents) // com.k_int.kbplus.DocContext
        //result.derivedSubscriptions = ApiReaderHelper.resolveStubs(sub.derivedSubscriptions, ApiReaderHelper.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
        result.identifiers          = ApiReaderHelper.retrieveIdentifierCollection(sub.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.instanceOf           = ApiReaderHelper.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
        result.license              = ApiReaderHelper.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
        //removed: result.license          = ApiReaderHelper.resolveLicense(sub.owner, ApiReaderHelper.IGNORE_ALL, context) // com.k_int.kbplus.License

        //result.organisations        = ApiReaderHelper.resolveOrgLinks(sub.orgRelations, ApiReaderHelper.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

        //TODO contact David upon this!

        result.previousSubscription = ApiReaderHelper.requestSubscriptionStub(sub.getCalculatedPrevious(), context) // com.k_int.kbplus.Subscription
        result.properties           = ApiReaderHelper.retrievePropertyCollection(sub, context) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

        def allOrgRoles = []

        // add derived subscriptions org roles
        if (sub.derivedSubscriptions) {
            allOrgRoles = OrgRole.executeQuery(
                    "select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
                    [derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
            )
        }
        allOrgRoles.addAll(sub.orgRelations)

        result.organisations = ApiReaderHelper.retrieveOrgLinkCollection(allOrgRoles, ApiReaderHelper.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

        // TODO refactoring with issueEntitlementService
        result.packages = ApiReaderHelper.retrievePackageWithIssueEntitlementsCollection(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

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

        return ApiToolkit.cleanUp(result, true, true)
    }

    // ################### CATALOGUE ###################

    /**
     * @return
     */
    static Collection<Object> retrieveRefdataCollection(){
        CacheService cacheService = grails.util.Holders.applicationContext.getBean('cacheService') as CacheService

        def cache = cacheService.getTTL1800Cache('ApiReader/exportRefdatas/')
        def result = []

        if (cache.get('refdatas')) {
            result = cache.get('refdatas')
            log.debug('refdatas from cache')
        }
        else {
            def validLabel = { lb ->
                return (lb != 'null' && lb != 'null °') ? lb : null
            }

            RefdataCategory.where {}.sort('desc').each { rdc ->
                def rdcTmp = [:]

                rdcTmp.desc = rdc.desc
                rdcTmp.label_de = validLabel(rdc.getI10n('desc', 'de'))
                rdcTmp.label_en = validLabel(rdc.getI10n('desc', 'en'))
                rdcTmp.entries = []

                RefdataCategory.getAllRefdataValues(rdc.desc).each { rdv ->
                    def tmpRdv = [:]

                    tmpRdv.value = rdv.value
                    tmpRdv.label_de = validLabel(rdv.getI10n('value', 'de'))
                    tmpRdv.label_en = validLabel(rdv.getI10n('value', 'en'))

                    rdcTmp.entries << ApiToolkit.cleanUp(tmpRdv, true, true)
                }
                result << ApiToolkit.cleanUp(rdcTmp, true, true)
            }
            cache.put('refdatas', result)
        }
        result
    }
}
