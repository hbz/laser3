package de.laser.api.v0


import de.laser.License
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractCoverage
import de.laser.finance.Invoice
import de.laser.finance.PriceItem
import de.laser.oap.OrgAccessPoint
import de.laser.finance.Order
import de.laser.traces.DeletedObject
import groovy.sql.GroovyRowResult

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
        result.status       = org.status?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(org.ids) // de.laser.Identifier
        result.type        = org.orgType?.collect{ it.value }

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
        result.serviceProvider      = pform.serviceProvider?.value
        result.softwareProvider     = pform.softwareProvider?.value

        // References
        result.provider = getOrganisationStubMap(pform.org) // de.laser.Org
        result.properties = ApiCollectionReader.getCustomPropertyCollection(pform.propertySet, pform, context)

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

}
