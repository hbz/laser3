package de.laser.api.v0

import com.k_int.kbplus.*
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
        //result.impId            = lic.impId
        result.reference        = lic.reference
        result.normReference    = lic.sortableReference
        result.calculatedType   = lic.getCalculatedType()
        result.startDate        = lic.startDate
        result.endDate          = lic.endDate

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
        //result.impId        = pkg.impId
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
        //result.impId        = pform.impId
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
        //result.impId            = sub.impId
        result.calculatedType   = sub.getCalculatedType()
        result.startDate        = sub.startDate
        result.endDate          = sub.endDate

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
        //result.impId        = title.impId
        result.gokbId       = title.gokbId
        result.title        = title.title
        result.normTitle    = title.normTitle
        result.type         = title.type?.value

        // References
        result.identifiers = ApiCollectionReader.getIdentifierCollection(title.ids) // com.k_int.kbplus.Identifier

        ApiToolkit.cleanUp(result, true, true)
    }

    // -------------------- FULL OBJECTS --------------------

    static Map<String, Object> getInvoiceMap(Invoice invoice) {
        if(! invoice) {
            return null
        }
        Map<String, Object> result = [:]

        result.id                  = invoice.id
        result.dateOfPayment       = invoice.dateOfPayment
        result.dateOfInvoice       = invoice.dateOfInvoice
        result.datePassedToFinance = invoice.datePassedToFinance
        result.endDate             = invoice.endDate
        result.invoiceNumber       = invoice.invoiceNumber
        result.startDate           = invoice.startDate
        result.lastUpdated         = invoice.lastUpdated

        // References
        result.owner               = getOrganisationStubMap(invoice.owner) // com.k_int.kbplus.Org

        ApiToolkit.cleanUp(result, true, true)
    }

    static Map<String, Object> getOrderMap(Order order) {
        if (!order) {
            return null
        }
        Map<String, Object> result = [:]

        result.id           = order.id
        result.orderNumber  = order.orderNumber
        result.lastUpdated  = order.lastUpdated

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
        //result.impId            = pform.impId
        result.gokbId           = pform.gokbId
        result.name             = pform.name
        result.normName         = pform.normname
        result.primaryUrl       = pform.primaryUrl
        result.provenance       = pform.provenance
        result.dateCreated      = pform.dateCreated
        result.lastUpdated      = pform.lastUpdated

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
