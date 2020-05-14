package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.domain.IssueEntitlementCoverage
import groovy.util.logging.Log4j

@Log4j
class ApiUnsecuredMapReader {

    // -------------------- STUBS --------------------

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getClusterStubMap(Cluster cluster) {
        if (!cluster) {
            return null
        }

        Map<String, Object> result = [:]

        result.id           = cluster.id
        result.name         = cluster.name

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getLicenseStubMap(License lic) {
        if (!lic) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = lic.globalUID
        result.reference        = lic.reference
        result.normReference    = lic.sortableReference
        result.calculatedType   = lic.getCalculatedType()
        result.startDate        = ApiToolkit.formatInternalDate(lic.startDate)
        result.endDate          = ApiToolkit.formatInternalDate(lic.endDate)

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(lic.ids) // com.k_int.kbplus.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getOrganisationStubMap(Org org) {
        if (!org) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.name         = org.name

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(org.ids) // com.k_int.kbplus.Identifier
        result.type        = org.orgType?.collect{ it.value }

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getPackageStubMap(Package pkg) {
        if (!pkg) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pkg.globalUID
        result.name         = pkg.name
        result.gokbId       = pkg.gokbId

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getPlatformStubMap(Platform pform) {
        if (!pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pform.globalUID
        result.gokbId       = pform.gokbId
        result.name         = pform.name
        result.normname     = pform.normname
        result.primaryUrl   = pform.primaryUrl

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getSubscriptionStubMap(Subscription sub) {
        if (!sub) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = sub.globalUID
        result.name             = sub.name
        result.calculatedType   = sub.getCalculatedType()
        result.startDate        = ApiToolkit.formatInternalDate(sub.startDate)
        result.endDate          = ApiToolkit.formatInternalDate(sub.endDate)

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(sub.ids) // com.k_int.kbplus.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getTitleStubMap(TitleInstance title) {
        if (!title) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = title.globalUID
        result.gokbId       = title.gokbId
        result.title        = title.title
        result.normTitle    = title.normTitle

        // References

        result.medium       = title.medium?.value
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(title.ids) // com.k_int.kbplus.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    // -------------------- FULL OBJECTS --------------------

    static Map<String, Object> getInvoiceMap(Invoice invoice) {
        if(! invoice) {
            return null
        }
        Map<String, Object> result = [:]

        result.id                  = invoice.id
        result.dateOfPayment       = ApiToolkit.formatInternalDate(invoice.dateOfPayment)
        result.dateOfInvoice       = ApiToolkit.formatInternalDate(invoice.dateOfInvoice)
        result.datePassedToFinance = ApiToolkit.formatInternalDate(invoice.datePassedToFinance)
        result.endDate             = ApiToolkit.formatInternalDate(invoice.endDate)
        result.invoiceNumber       = invoice.invoiceNumber
        result.startDate           = ApiToolkit.formatInternalDate(invoice.startDate)
        result.lastUpdated         = ApiToolkit.formatInternalDate(invoice.lastUpdated)

        // References
        result.owner               = getOrganisationStubMap(invoice.owner) // com.k_int.kbplus.Org

        ApiToolkit.cleanUp(result, true, true)
    }

    static Map<String, Object> getIssueEntitlementCoverageMap(IssueEntitlementCoverage coverage) {
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

    static Map<String, Object> getOrderMap(Order order) {
        if (!order) {
            return null
        }
        Map<String, Object> result = [:]

        result.id           = order.id
        result.orderNumber  = order.orderNumber
        result.lastUpdated  = ApiToolkit.formatInternalDate(order.lastUpdated)

        // References
        result.owner        = getOrganisationStubMap(order.owner) // com.k_int.kbplus.Org

        ApiToolkit.cleanUp(result, true, true)
    }

    static Map<String, Object> getPlatformMap(Platform pform) {
        if (!pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID        = pform.globalUID
        result.gokbId           = pform.gokbId
        result.name             = pform.name
        result.normName         = pform.normname
        result.primaryUrl       = pform.primaryUrl
        result.provenance       = pform.provenance
        result.dateCreated      = ApiToolkit.formatInternalDate(pform.dateCreated)
        result.lastUpdated      = ApiToolkit.formatInternalDate(pform.getCalculatedLastUpdated())

        // RefdataValues
        result.type                 = pform.type?.value
        result.status               = pform.status?.value
        result.serviceProvider      = pform.serviceProvider?.value
        result.softwareProvider     = pform.softwareProvider?.value

        // References
        //result.tipps = pform.tipps

        ApiToolkit.cleanUp(result, true, true)
    }

}
