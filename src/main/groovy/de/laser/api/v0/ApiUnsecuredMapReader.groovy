package de.laser.api.v0

import de.laser.addressbook.Address
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.LibrarySystem
import de.laser.License
import de.laser.Org
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.base.AbstractCoverage
import de.laser.finance.Invoice
import de.laser.finance.PriceItem
import de.laser.oap.OrgAccessPoint
import de.laser.finance.Order
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import groovy.sql.GroovyRowResult
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This class delivers objects which do not need further authentication because they do not need any or authorisation
 * has already been done elsewhere. It represents the bottommost level of the API output tree
 */
class ApiUnsecuredMapReader {

    // -------------------- STUBS --------------------

    /**
     * Returns the essential information for the given license for API output
     * @param lic the {@link License} called for API
     * @return Map<String, Object> reflecting the license details
     */
    static Map<String, Object> getLicenseStubMap(License lic) {
        if (!lic) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = lic.globalUID
        result.reference        = lic.reference
        result.calculatedType   = lic._getCalculatedType()
        result.startDate        = ApiToolkit.formatInternalDate(lic.startDate)
        result.endDate          = ApiToolkit.formatInternalDate(lic.endDate)

        // RefdataValues
        result.status           = lic.status?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(lic.ids) // de.laser.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given access point for API output
     * @param orgAccessPoint the {@link OrgAccessPoint} called for API
     * @return Map<String, Object> reflecting the access point details
     */
    static Map<String, Object> getOrgAccessPointStubMap(OrgAccessPoint orgAccessPoint) {
        if (!orgAccessPoint) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = orgAccessPoint.globalUID
        result.type             = orgAccessPoint.accessMethod?.value

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given organisation for API output
     * @param org the {@link Org} called for API
     * @return Map<String, Object> reflecting the organisation details
     */
    static Map<String, Object> getOrganisationStubMap(Org org) {
        if (!org) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.name         = org.name
        result.sortname     = org.sortname
//        result.status       = org.status?.value // TODO: ERMS-6224 - remove org.status
        result.status       = org.isArchived() ? 'Deleted' : 'Current' // TODO: ERMS-6238 -> REMOVE

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(org.ids) // de.laser.Identifier
        result.type        = org.getOrgType() ? [org.getOrgType().value] : [] // TODO: ERMS-6009

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given package for API output
     * @param pkg the {@link Package} called for API
     * @return Map<String, Object> reflecting the package details
     */
    static Map<String, Object> getPackageStubMap(Package pkg) {
        if (!pkg) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pkg.globalUID
        result.name         = pkg.name
        result.altnames     = ApiCollectionReader.getAlternativeNameCollection(pkg.altnames)
        result.gokbId       = pkg.gokbId
        result.status       = pkg.packageStatus?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(pkg.ids) // de.laser.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given package for API output
     * @param pkg the {@link Package} called for API
     * @return Map<String, Object> reflecting the package details
     */
    static Map<String, Object> getPackageStubMapWithSQL(GroovyRowResult pkg) {
        if (!pkg) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pkg['pkg_guid']
        result.name         = pkg['pkg_name']
        List<String> altnames = []
        pkg['altnames'].each { altNameRow ->
            altnames << altNameRow['altname_name']
        }
        result.gokbId       = pkg['pkg_gokb_id']
        result.status       = pkg['pkg_status']

        // References
        List<Map<String, Object>> identifiers = []
        pkg['ids'].each { idRow ->
            identifiers << [namespace: idRow['idns_ns'], value: idRow['id_value']]
        }
        result.identifiers          = identifiers       // de.laser.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given platform for API output
     * @param pform the {@link Platform} called for API
     * @return Map<String, Object> reflecting the platform details
     */
    static Map<String, Object> getPlatformStubMap(Platform pform) {
        if (!pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pform.globalUID
        result.gokbId       = pform.gokbId
        result.name         = pform.name
        result.primaryUrl   = pform.primaryUrl
        result.status       = pform.status?.value

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given platform for API output
     * @param pform the {@link Platform} called for API
     * @return Map<String, Object> reflecting the platform details
     */
    static Map<String, Object> getPlatformStubMapWithSQL(GroovyRowResult pform) {
        if (!pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pform['plat_guid']
        result.gokbId       = pform['plat_gokb_id']
        result.name         = pform['plat_name']
        result.primaryUrl   = pform['plat_primary_url']
        result.status       = pform['plat_status']

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given provider for API output
     * @param provider the {@link Provider} called for API
     * @return Map<String, Object> reflecting the provider details
     */
    static Map<String, Object> getProviderStubMap(Provider provider) {
        if (!provider) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = provider.globalUID
        result.gokbId       = provider.gokbId
        result.name         = provider.name
        result.sortname     = provider.sortname
        result.status       = provider.status?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(provider.ids) // de.laser.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given subscription for API output
     * @param sub the {@link Subscription} called for API
     * @return Map<String, Object> reflecting the subscription details
     */
    static Map<String, Object> getSubscriptionStubMap(Subscription sub) {
        if (!sub) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = sub.globalUID
        result.name             = sub.name
        result.calculatedType   = sub._getCalculatedType()
        result.startDate        = ApiToolkit.formatInternalDate(sub.startDate)
        result.endDate          = ApiToolkit.formatInternalDate(sub.endDate)

        // RefdataValues
        result.status           = sub.status.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(sub.ids) // de.laser.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given title for API output
     * @param tipp the {@link TitleInstancePackagePlatform} called for API
     * @return Map<String, Object> reflecting the title details
     */
    static Map<String, Object> getTitleStubMap(TitleInstancePackagePlatform tipp) {
        if (!tipp) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = tipp.globalUID
        result.gokbId       = tipp.gokbId
        result.title        = tipp.name
        result.normTitle    = tipp.normName

        // References

        result.medium       = tipp.medium?.value
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(tipp.ids) // de.laser.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the essential information for the given vendor for API output
     * @param vendor the {@link Vendor} called for API
     * @return Map<String, Object> reflecting the vendor details
     */
    static Map<String, Object> getVendorStubMap(Vendor vendor) {
        if (!vendor) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = vendor.globalUID
        result.gokbId       = vendor.gokbId
        result.name         = vendor.name
        result.sortname     = vendor.sortname
        result.status       = vendor.status?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(vendor.ids) // de.laser.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the given deleted object stub details
     * @param delObj the {@link DeletedObject} trace to be retrieved
     * @return a {@link Map} containing the deleted object stub
     */
    static Map<String, Object> getDeletedObjectStubMap(DeletedObject delObj) {
        if (!delObj) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = delObj.oldGlobalUID
        result.name             = delObj.oldName
        result.calculatedType   = delObj.oldCalculatedType
        result.startDate        = ApiToolkit.formatInternalDate(delObj.oldStartDate)
        result.endDate          = ApiToolkit.formatInternalDate(delObj.oldEndDate)

        ApiToolkit.cleanUp(result, true, true)
    }

    // -------------------- FULL OBJECTS --------------------

    /**
     * Returns the given invoice details
     * @param invoice the {@link Invoice} to be retrieved
     * @return a {@link Map} reflecting the invoice for API output
     */
    static Map<String, Object> getInvoiceMap(Invoice invoice) {
        if(! invoice) {
            return null
        }
        Map<String, Object> result = [:]

        result.invoiceNumber       = invoice.invoiceNumber
        result.lastUpdated         = ApiToolkit.formatInternalDate(invoice.lastUpdated)

        // References
        result.owner               = getOrganisationStubMap(invoice.owner) // de.laser.Org

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the given coverage details
     * @param coverage the {@link AbstractCoverage} to be retrieved
     * @return a {@link Map} reflecting the issue entitlement coverage for API output
     */
    static Map<String, Object> getAbstractCoverageMap(AbstractCoverage coverage) {
        if (!coverage) {
            return null
        }
        Map<String, Object> result = [:]

        result.startDate        = ApiToolkit.formatInternalDate(coverage.startDate)
        result.startVolume      = coverage.startVolume
        result.startIssue       = coverage.startIssue
        result.endDate          = ApiToolkit.formatInternalDate(coverage.endDate)
        result.endVolume        = coverage.endVolume
        result.endIssue         = coverage.endIssue
        result.embargo          = coverage.embargo
        result.coverageDepth    = coverage.coverageDepth
        result.coverageNote     = coverage.coverageNote
        result.lastUpdated      = ApiToolkit.formatInternalDate(coverage.lastUpdated)

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the given price item details
     * @param pi the {@link PriceItem} to be retrieved
     * @return a {@link Map} reflecting the price item for API output
     */
    static Map<String, Object> getPriceItemMap(PriceItem pi) {
        if(!pi)
            return null
        Map<String, Object> result = [:]

        result.listPrice     = pi.listPrice
        result.listCurrency  = pi.listCurrency?.value
        result.localPrice    = pi.localPrice
        result.localCurrency = pi.localCurrency?.value

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the given order details
     * @param invoice the {@link Order} to be retrieved
     * @return a {@link Map} reflecting the order for API output
     */
    static Map<String, Object> getOrderMap(Order order) {
        if (!order) {
            return null
        }
        Map<String, Object> result = [:]

        result.orderNumber  = order.orderNumber
        result.lastUpdated  = ApiToolkit.formatInternalDate(order.lastUpdated)

        // References
        result.owner        = getOrganisationStubMap(order.owner) // de.laser.Org

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Returns the given platform details including the properties the requesting institution has
     * @param pform the {@link Platform} to be retrieved
     * @param context the institution ({@link Org}) requesting the platform and whose properties should be returned
     * @return a {@link Map} reflecting the platform for API output
     */
    static Map<String, Object> getPlatformMap(Platform pform, Org context) {
        if (!pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = pform.globalUID
        result.gokbId           = pform.gokbId
        result.name             = pform.name
        result.normName         = pform.normname
        result.primaryUrl       = pform.primaryUrl
        //result.provenance       = pform.provenance
        result.dateCreated      = ApiToolkit.formatInternalDate(pform.dateCreated)
        result.lastUpdated      = ApiToolkit.formatInternalDate(pform._getCalculatedLastUpdated())

        // RefdataValues
        //result.type                 = pform.type?.value
        result.status               = pform.status?.value
        //result.serviceProvider      = pform.serviceProvider?.value
        //result.softwareProvider     = pform.softwareProvider?.value

        // References
        result.provider = getProviderStubMap(pform.provider) // de.laser.wekb.Provider
        result.properties = ApiCollectionReader.getCustomPropertyCollection(pform.propertySet, pform, context)

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Assembles the given provider attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param provider the {@link Provider} which should be output
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getProviderMap(Provider provider, Org context) {
        Map<String, Object> result = [:]

        provider = GrailsHibernateUtil.unwrapIfProxy(provider)

        result.globalUID             = provider.globalUID
        result.gokbId                = provider.gokbId
        result.name                  = provider.name
        result.altNames              = ApiCollectionReader.getAlternativeNameCollection(provider.altnames)
        result.sortname              = provider.sortname
        result.lastUpdated           = ApiToolkit.formatInternalDate(provider._getCalculatedLastUpdated())
        result.homepage              = provider.homepage
        result.kbartDownloaderURL    = provider.kbartDownloaderURL
        result.metadataDownloaderURL = provider.metadataDownloaderURL
        result.inhouseInvoicing      = provider.inhouseInvoicing ? RDStore.YN_YES.value : RDStore.YN_NO.value
        result.paperInvoice          = provider.paperInvoice ? RDStore.YN_YES.value : RDStore.YN_NO.value
        result.managementOfCredits   = provider.managementOfCredits ? RDStore.YN_YES.value : RDStore.YN_NO.value
        result.processingOfCompensationPayments = provider.processingOfCompensationPayments ? RDStore.YN_YES.value : RDStore.YN_NO.value
        result.individualInvoiceDesign = provider.individualInvoiceDesign ? RDStore.YN_YES.value : RDStore.YN_NO.value

        result.retirementDate      = provider.retirementDate ? ApiToolkit.formatInternalDate(provider.retirementDate) : null

        // RefdataValues

        result.electronicBillings = provider.electronicBillings?.collect { ElectronicBilling eb -> eb.invoicingFormat.value }
        result.invoiceDispatchs   = provider.invoiceDispatchs?.collect { InvoiceDispatch idi -> idi.invoiceDispatch.value }
        result.status         = provider.status?.value

        // References
        Map<String, Object> queryParams = [provider:provider]

        result.publicAddresses     = ApiCollectionReader.getAddressCollection(Address.executeQuery('select a from Address a where a.provider = :provider and a.tenant = null', queryParams), ApiReader.NO_CONSTRAINT) // de.laser.addressbook.Address w/o tenant
        result.privateAddresses    = ApiCollectionReader.getAddressCollection(Address.executeQuery('select a from Address a where a.provider = :provider and a.tenant = :context', queryParams+[context: context]), ApiReader.NO_CONSTRAINT) // de.laser.addressbook.Address w/ tenant
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(provider.ids) // de.laser.Identifier
        result.packages     = ApiCollectionReader.getPackageCollection(provider.packages)
        result.platforms    = ApiCollectionReader.getPlatformCollection(provider.platforms)
        result.persons      = ApiCollectionReader.getPrsLinkCollection(
                provider.prsLinks, ApiReader.NO_CONSTRAINT, ApiReader.NO_CONSTRAINT, context
        ) // de.laser.addressbook.PersonRole
        result.invoicingVendors    = ApiCollectionReader.getVendorCollection(provider.invoicingVendors.vendor)

        result.properties   = ApiCollectionReader.getPropertyCollection(provider, context, ApiReader.IGNORE_NONE) // de.laser.ProviderProperty


        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Assembles the given vendor attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param vendor the {@link Vendor} which should be output
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getVendorMap(Vendor vendor, Org context) {
        Map<String, Object> result = [:]

        vendor = GrailsHibernateUtil.unwrapIfProxy(vendor)

        result.globalUID             = vendor.globalUID
        result.gokbId                = vendor.gokbId
        result.name                  = vendor.name
        result.altNames              = ApiCollectionReader.getAlternativeNameCollection(vendor.altnames)
        result.sortname              = vendor.sortname
        result.lastUpdated           = ApiToolkit.formatInternalDate(vendor._getCalculatedLastUpdated())
        result.homepage              = vendor.homepage
        result.researchPlatformForEbooks = vendor.researchPlatformForEbooks
        result.prequalificationInfo = vendor.prequalificationInfo
        Set<String> boolFields = ['webShopOrders', 'ediOrders', 'xmlOrders', 'paperInvoice', 'managementOfCredits', 'processingOfCompensationPayments', 'individualInvoiceDesign',
                                  'technicalSupport', 'shippingMetadata', 'forwardingUsageStatisticsFromPublisher', 'activationForNewReleases', 'exchangeOfIndividualTitles', 'prequalification']
        result.putAll(ApiToolkit.readBoolValues(vendor, boolFields))

        result.retirementDate      = vendor.retirementDate ? ApiToolkit.formatInternalDate(vendor.retirementDate) : null

        // RefdataValues

        result.supportedLibrarySystems = vendor.supportedLibrarySystems?.collect { LibrarySystem ls -> ls.librarySystem.value }
        result.electronicBillings = vendor.electronicBillings?.collect { ElectronicBilling eb -> eb.invoicingFormat.value }
        result.electronicDeliveryDelayNotifications = vendor.electronicDeliveryDelays?.collect { ElectronicDeliveryDelayNotification eddn -> eddn.delayNotification.value }
        result.invoiceDispatchs   = vendor.invoiceDispatchs?.collect { InvoiceDispatch idi -> idi.invoiceDispatch.value }
        result.status         = vendor.status?.value

        // References
        Map<String, Object> queryParams = [vendor:vendor]

        result.publicAddresses     = ApiCollectionReader.getAddressCollection(Address.executeQuery('select a from Address a where a.vendor = :vendor and a.tenant = null', queryParams), ApiReader.NO_CONSTRAINT) // de.laser.addressbook.Address w/o tenant
        result.privateAddresses    = ApiCollectionReader.getAddressCollection(Address.executeQuery('select a from Address a where a.vendor = :vendor and a.tenant = :context', queryParams+[context: context]), ApiReader.NO_CONSTRAINT) // de.laser.addressbook.Address w/ tenant
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(vendor.ids) // de.laser.Identifier
        result.packages     = ApiCollectionReader.getPackageCollection(vendor.packages.pkg)
        result.persons      = ApiCollectionReader.getPrsLinkCollection(
                vendor.prsLinks, ApiReader.NO_CONSTRAINT, ApiReader.NO_CONSTRAINT, context
        ) // de.laser.addressbook.PersonRole

        result.properties   = ApiCollectionReader.getPropertyCollection(vendor, context, ApiReader.IGNORE_NONE) // de.laser.VendorProperty


        ApiToolkit.cleanUp(result, true, true)
    }

}
