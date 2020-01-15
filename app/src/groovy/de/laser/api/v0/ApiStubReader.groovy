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
    static Collection<Object> retrieveStubCollection(Collection<Object> list, def type, Org context) {
        def result = []

        list?.each { it ->
            if(ApiReader.LICENSE_STUB == type) {
                result << requestLicenseStub((License) it, context)
            }
            else if(ApiReader.PACKAGE_STUB == type) {
                result << retrievePackageStubMap((Package) it, context)
            }
            else if(ApiReader.SUBSCRIPTION_STUB == type) {
                result << requestSubscriptionStub((Subscription) it, context)
            }
        }

        result
    }

    // ################### STUBS ###################

    @Deprecated
    static Map<String, Object> retrieveClusterStubMap(Cluster cluster) {
        def result = [:]
        if (cluster) {
            result.id           = cluster.id
            result.name         = cluster.name
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestLicenseStub(License lic, Org context) {
        def result = [:]

        if (!lic) {
            return null
        }

        boolean hasAccess = ApiLicense.calculateAccess(lic, context)

        if (hasAccess) {
            result.globalUID    = lic.globalUID
            result.impId        = lic.impId
            result.reference    = lic.reference
            result.normReference    = lic.sortableReference
            // erms-888
            result.calculatedType   = lic.getCalculatedType()
            result.startDate        = lic.startDate
            result.endDate          = lic.endDate

            // References
            result.identifiers = ApiCollectionReader.retrieveIdentifierCollection(lic.ids) // com.k_int.kbplus.Identifier

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrieveOrganisationStubMap(Org org, Org context) {
        if (!org) {
            return null
        }

        def result = [:]
        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.name         = org.name

        // References
        result.identifiers = ApiCollectionReader.retrieveIdentifierCollection(org.ids) // com.k_int.kbplus.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrievePackageStubMap(Package pkg, Org context) {
        if (!pkg) {
            return null
        }

        def result = [:]
        result.globalUID    = pkg.globalUID
        result.name         = pkg.name
        //result.identifier   = pkg.identifier // TODO refactor legacy
        result.impId        = pkg.impId
        result.gokbId       = pkg.gokbId

        // References
        result.identifiers = ApiCollectionReader.retrieveIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> retrievePlatformStubMap(Platform pform) {
        def result = [:]
        if (pform) {
            result.globalUID    = pform.globalUID
            result.impId        = pform.impId
            result.gokbId       = pform.gokbId
            result.name         = pform.name
            result.normname     = pform.normname
            result.primaryUrl   = pform.primaryUrl
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionStub(Subscription sub, Org context) {
        def result = [:]

        if (!sub) {
            return null
        }

        boolean hasAccess = ApiSubscription.calculateAccess(sub, context)

        if (hasAccess) {
            result.globalUID    = sub.globalUID
            result.name         = sub.name
            //result.identifier   = sub.identifier // TODO refactor identifier
            result.impId        = sub.impId
            // erms-888
            result.calculatedType = sub.getCalculatedType()
            result.startDate      = sub.startDate
            result.endDate        = sub.endDate

            // References
            result.identifiers = ApiCollectionReader.retrieveIdentifierCollection(sub.ids) // com.k_int.kbplus.Identifier

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    static retrieveSubscriptionPackageStubMixed(SubscriptionPackage subpkg, ignoreRelation, Org context) {
        if (subpkg) {
            if (ApiReader.IGNORE_SUBSCRIPTION == ignoreRelation) {
                return retrievePackageStubMap(subpkg.pkg, context)
            }
            else if (ApiReader.IGNORE_PACKAGE == ignoreRelation) {
                return requestSubscriptionStub(subpkg.subscription, context)
            }
        }
        return null
    }

    static Collection<Object> retrieveSubscriptionPackageStubCollection(Collection<SubscriptionPackage> list, def ignoreRelation, Org context) {
        def result = []
        if (! list) {
            return null
        }

        list?.each { it -> // com.k_int.kbplus.SubscriptionPackage
            result << retrieveSubscriptionPackageStubMixed(it, ignoreRelation, context)
        }
        result
    }

    static Map<String, Object> retrieveTitleStubMap(TitleInstance title) {
        def result = [:]

        result.globalUID    = title.globalUID
        result.impId        = title.impId
        result.gokbId       = title.gokbId
        result.title        = title.title
        result.normTitle    = title.normTitle
        result.type         = title.type?.value

        // References
        result.identifiers = ApiCollectionReader.retrieveIdentifierCollection(title.ids) // com.k_int.kbplus.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }
}
