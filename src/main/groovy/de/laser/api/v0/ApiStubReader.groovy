package de.laser.api.v0


import de.laser.License
import de.laser.Org
import de.laser.Package
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.entities.ApiLicense
import de.laser.api.v0.entities.ApiOrgAccessPoint
import de.laser.api.v0.entities.ApiSubscription
import de.laser.storage.Constants
import de.laser.traces.DeletedObject

/**
 * This class is responsible for delivering stubs, i.e. object fragments with just the essential details
 * and identifiers in order to request full object details if necessary
 */
class ApiStubReader {

    // ################### HELPER ###################

    /**
     * Resolving collection of items to stubs. Delegate context to gain access
     *
     * @param list the {@link Collection<Object>} of objects to return
     * @param type the type of stub to deliver
     * @param context {@link Org}
     * @return a {@link Collection<Object>} of object stubs
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
     * Checks if the requesting institution has access to the given license and returns its stub if access is granted
     * @param lic the {@link License} being requested
     * @param context the institution ({@link Org}) requesting access
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

    // ################### STUBS ###################

    /**
     * Checks if the requesting institution has access to the given access point and returns its stub if access is granted
     * @param orgAccessPoint the {@link OrgAccessPoint} being requested
     * @param context the institution ({@link Org}) requesting access
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestOrgAccessPointStub(OrgAccessPoint orgAccessPoint, Org context) {
        Map<String, Object> result = [:]

        if (! orgAccessPoint) {
            return null
        }
        boolean hasAccess = ApiOrgAccessPoint.calculateAccess(orgAccessPoint, context)

        if (hasAccess) {
            result = ApiUnsecuredMapReader.getOrgAccessPointStubMap(orgAccessPoint)
        }

        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Substitution call, setting the invoice tool flag to false as default
     * @param sub the {@link Subscription} being requested
     * @param context the institution ({@link Org}) requesting access
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestSubscriptionStub(Subscription sub, Org context) {
        requestSubscriptionStub(sub, context, false)
    }

    /**
     * Checks if the requesting institution has access to the given subscription and returns its stub if access is granted
     * @param sub the {@link Subscription} being requested
     * @param context the institution ({@link Org}) requesting access
     * @param isInvoiceTool is the hbz invoice tool doing the request?
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
     * Requests access either to the package or to the subscription with respective entitlements
     * @param subpkg the {@link SubscriptionPackage} whose subscription/package and entitlements should be retrieved
     * @param ignoreRelation should further relations be followed up?
     * @param context the institution ({@link Org}) requesting access
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

    /**
     * Requests access to the given trace of deleted object.
     * Is a substitution call for {@link #requestDeletedObjectStub(de.laser.traces.DeletedObject, de.laser.Org, boolean)}
     * with the isInvoiceTool flag set to false per default. Returns the deleted object in case of success
     * @param deletedObject the {@link DeletedObject} trace being requested
     * @param context the institution ({@link Org}) requesting access
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestDeletedObjectStub(DeletedObject deletedObject, Org context) {
        requestDeletedObjectStub(deletedObject, context, false)
    }

    /**
     * Requests access to the given trace of deleted object. Returns the deleted object in case of success
     * @param delObj the {@link DeletedObject} trace being requested
     * @param context the institution ({@link Org}) requesting access
     * @param isInvoiceTool
     * @return MAP | Constants.HTTP_FORBIDDEN
     */
    static requestDeletedObjectStub(DeletedObject delObj, Org context, boolean isInvoiceTool) {
        Map<String, Object> result = [:]
        if(!delObj) {
            return null
        }
        boolean hasAccess = isInvoiceTool || ApiDeletedObject.calculateAccess(delObj, context)
        if (hasAccess) {
            result = ApiUnsecuredMapReader.getDeletedObjectStubMap(delObj)
        }
        return (hasAccess ? result : Constants.HTTP_FORBIDDEN)
    }
}
