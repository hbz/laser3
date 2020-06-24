package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.api.v0.entities.ApiLicense
import de.laser.api.v0.entities.ApiSubscription
import de.laser.helper.Constants
import groovy.util.logging.Log4j

@Log4j
class ApiStubReader {

    // ################### HELPER ###################

    /**
     * Resolving collection of items to stubs. Delegate context to gain access
     *
     * @param Collection<Object> list
     * @param type
     * @param com.k_int.kbplus.Org context
     * @return Collection<Object>
     */
    static Collection<Object> getStubCollection(Collection<Object> list, def type, Org context) {
        def result = []

        list?.each { it ->
            if(ApiReader.LICENSE_STUB == type) {
                result << requestLicenseStub((License) it, context)
            }
            else if(ApiReader.PACKAGE_STUB == type) {
                result << ApiUnsecuredMapReader.getPackageStubMap((Package) it)
            }
            else if(ApiReader.SUBSCRIPTION_STUB == type) {
                result << requestSubscriptionStub((Subscription) it, context)
            }
        }

        result
    }

    // ################### STUBS ###################

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestLicenseStub(License lic, Org context) {
        Map<String, Object> result = [:]

        if (! lic) {
            return null
        }
        boolean hasAccess = ApiLicense.calculateAccess(lic, context)

        if (hasAccess) {
            result = ApiUnsecuredMapReader.getLicenseStubMap(lic)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionStub(Subscription sub, Org context) {
        requestSubscriptionStub(sub, context, false)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionStub(Subscription sub, Org context, boolean isInvoiceTool){
        Map<String, Object> result = [:]

        if (! sub) {
            return null
        }
        boolean hasAccess = isInvoiceTool || ApiSubscription.calculateAccess(sub, context)

        if (hasAccess) {
            result = ApiUnsecuredMapReader.getSubscriptionStubMap(sub)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionPackageStubMixed(SubscriptionPackage subpkg, ignoreRelation, Org context) {
        if (subpkg) {
            if (ApiReader.IGNORE_SUBSCRIPTION == ignoreRelation) {
                return ApiUnsecuredMapReader.getPackageStubMap(subpkg.pkg)
            }
            else if (ApiReader.IGNORE_PACKAGE == ignoreRelation) {
                return requestSubscriptionStub(subpkg.subscription, context)
            }
        }
        return null
    }
}
