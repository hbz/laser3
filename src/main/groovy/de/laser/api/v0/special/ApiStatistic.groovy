package de.laser.api.v0.special


import de.laser.License
import de.laser.Org
import de.laser.OrgRole
import de.laser.OrgSetting
import de.laser.Package
import de.laser.RefdataValue
import de.laser.SubscriptionPackage
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import grails.converters.JSON
import groovy.util.logging.Slf4j

/**
 * This class in a special endpoint implementation exclusively for the National Statistics Server (Nationaler Statistikserver)
 */
@Slf4j
class ApiStatistic {

    /**
     * Checks implicit NATSTAT_SERVER_ACCESS, i.e. if there are package subscribers gave permission to their data for the Nationaler Statistikserver
     * harvesters
     * @param pkg the {@link Package} which should be accessed
     * @return true if the package belongs to the accessible ones, false otherwise
     */
    static boolean calculateAccess(Package pkg) {

        if (pkg in getAccessiblePackages()) {
            return true
        }
        else {
            return false
        }
    }

    /**
     * Checks NATSTAT_SERVER_ACCESS; lists the institutions which gave permission to the Nationaler Statistikserver to access their data
     * @return a {@link List} of institutions ({@link Org}) who opened their data to the Nationaler Statistikserver
     */
    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSetting.executeQuery(
                "select o from OrgSetting os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                            key    : OrgSetting.KEYS.NATSTAT_SERVER_ACCESS,
                            rdValue: RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N),
                            deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)
                    ])

        orgs
    }

    /**
     * Checks implicit NATSTAT_SERVER_ACCESS; lists the packages which are subscribed by institutions who gave permission to the
     * Nationaler Statistikserver to access their data
     * @return a {@link List} of {@link Package}s that are subscribed by institutions who opened up their data for Nationaler Statistikserver
     */
    static private List<Package> getAccessiblePackages() {

        List<Package> packages = []
        List<Org> orgs = getAccessibleOrgs()

        if (orgs) {
            packages = Package.executeQuery(
                    "select distinct(pkg) from SubscriptionPackage sp " +
                            "join sp.pkg pkg join sp.subscription s join s.orgRelations ogr join ogr.org o " +
                            "where o in (:orgs) and (pkg.packageStatus is null or pkg.packageStatus != :deleted)", [
                    orgs: orgs,
                    deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.PACKAGE_STATUS)
                ]
            )
        }

        packages
    }

    /**
     * Gets a list of package stubs of packages subscribed by NatStat access permitting institutions.
     * Checks implicit NATSTAT_SERVER_ACCESS; i.e. only those packages are being listed which are subscribed by at least one
     * subscriber who gave permission to the Nationaler Statistikserver to access its data
     * @return JSON
     * @see ApiUnsecuredMapReader#getPackageStubMap(de.laser.Package)
     */
    static JSON getAllPackages() {
        Collection<Object> result = []

        getAccessiblePackages().each { p ->
            result << ApiUnsecuredMapReader.getPackageStubMap(p)
        }

        return result ? new JSON(result) : null
    }

    /**
     * Checks if there is access for the given package, i.e. if there is at least one subscriber to this package
     * that gave permission to access its data to the Nationaler Statistikserver
     * @param pkg the {@link Package} to which access is being requested
     * @return JSON | FORBIDDEN
     */
    static requestPackage(Package pkg) {
        if (! pkg || pkg.packageStatus?.value == 'Deleted') {
            return null
        }

        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(pkg)
        if (hasAccess) {

            result.globalUID        = pkg.globalUID
            result.startDate        = ApiToolkit.formatInternalDate(pkg.startDate)
            result.endDate          = ApiToolkit.formatInternalDate(pkg.endDate)
            result.lastUpdated      = ApiToolkit.formatInternalDate(pkg._getCalculatedLastUpdated())
            result.packageType      = pkg.contentType?.value
            result.packageStatus    = pkg.packageStatus?.value
            result.name             = pkg.name
            result.variantNames     = ['TODO-TODO-TODO'] // todo

            // References
            result.contentProvider  = getPkgOrganisationCollection(pkg.orgs)
            //result.license          = getPkgLicense(pkg.license)
            result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // de.laser.Identifier
            //result.platforms        = resolvePkgPlatforms(pkg.nominalPlatform)
            //result.tipps            = resolvePkgTipps(pkg.tipps)
            result.subscriptions    = getPkgSubscriptionCollection(pkg.subscriptions, getAccessibleOrgs())

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Retrieves a collection of organisations (i.e. content providers) linked to a package
     * @param orgRoles the outgoing relation set from the given package
     * @return a {@link Map} of stubs showing the organisations linked to the given package
     * @see OrgRole
     * @see ApiUnsecuredMapReader#getOrganisationStubMap(de.laser.Org)
     */
    static private Collection<Object> getPkgOrganisationCollection(Set<OrgRole> orgRoles) {
        if (! orgRoles) {
            return null
        }

        Collection<Object> result = []
        orgRoles.each { ogr ->
            if (ogr.roleType.id == RDStore.OR_CONTENT_PROVIDER.id) {
                if (ogr.org.status?.value == 'Deleted') {
                }
                else {
                    result.add(ApiUnsecuredMapReader.getOrganisationStubMap(ogr.org))
                }
            }
        }

        ApiToolkit.cleanUp(result, true, true)
    }

    @Deprecated
    static private Map<String, Object> getPkgLicense(License lic) {
        if (! lic) {
            return null
        }
        else if (! lic.isPublicForApi) {
            if (ApiToolkit.isDebugMode()) {
                return ["NO_ACCESS": ApiToolkit.NO_ACCESS_DUE_NOT_PUBLIC]
            }
            return null
        }
        def result = ApiUnsecuredMapReader.getLicenseStubMap(lic)

        ApiToolkit.cleanUp(result, true, true)
    }

    /*
    // TODO nominalPlatform? or tipps?
    static resolvePkgPlatforms(Platform  pform) {
        if (! pform) {
            return null
        }
        Map<String, Object> result = [:]

        result.globalUID    = pform.globalUID
        result.name         = pform.name
        //result.identifiers  = ApiCollectionReader.resolveIdentifiers(pform.ids) // de.laser.Identifier

        return ApiToolkit.cleanUp(result, true, true)
    }
    */

    /*
    static resolvePkgTipps(Set<TitleInstancePackagePlatform> tipps) {
        // TODO: def tipps = TitleInstancePackagePlatform.findAllByPkgAndSub(subPkg.pkg, subPkg.subscription) ??
        if (! tipps) {
            return null
        }
        def result = []
        tipps.each{ tipp ->
            result.add( ApiCollectionReader.resolveTipp(tipp, ApiCollectionReader.IGNORE_NONE, null))
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
    */

    /**
     * Gets all subscriptions with their respective holdings. Shown are those subscriptions whose tenants gave permission to the Nationaler
     * Statistikserver to access their data and are themselves public for API usage
     * @param subscriptionPackages the set of subscriptions attached to the package
     * @param accessibleOrgs the list of institutions open to Nationaler Statistikserver (that includes the check for NATSTAT_SERVER_ACCESS)
     * @return a {@link List} of {@link Map}s containing subscription stubs with holdings and subscriber stubs
     */
    static private Collection<Object> getPkgSubscriptionCollection(Set<SubscriptionPackage> subscriptionPackages, List<Org> accessibleOrgs) {
        if (!subscriptionPackages) {
            return null
        }

        Collection<Object> result = []
        subscriptionPackages.each { subPkg ->

            if (! subPkg.subscription.isPublicForApi) {
                if (ApiToolkit.isDebugMode()) {
                    result.add(["NO_ACCESS": ApiToolkit.NO_ACCESS_DUE_NOT_PUBLIC])
                }
            }
            else {
                Map<String, Object> sub = ApiUnsecuredMapReader.getSubscriptionStubMap(subPkg.subscription)
                sub.kind = subPkg.subscription.kind?.value // TODO : implement sub.kind in stub ??

                List<Org> orgList = []

                OrgRole.findAllBySub(subPkg.subscription).each { ogr ->

                    if (ogr.roleType?.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIPTION_CONSORTIA.id]) {
                        if (ogr.org.id in accessibleOrgs.collect{ it.id }) {

                            if (ogr.org.status?.value == 'Deleted') {
                            }
                            else {
                                Map<String, Object> org = ApiUnsecuredMapReader.getOrganisationStubMap(ogr.org)
                                if (org) {
                                    orgList.add(ApiToolkit.cleanUp(org, true, true))
                                }
                            }
                        }
                    }
                }

                if (orgList) {
                    sub.put('organisations', ApiToolkit.cleanUp(orgList, true, true))

                    List ieList = ApiCollectionReader.getIssueEntitlementCollection(subPkg, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, null)

                    if (ieList) {
                        sub.put('issueEntitlements', ApiToolkit.cleanUp(ieList, true, true))
                    }

                    //result.add( ApiStubReader.resolveSubscriptionStub(subPkg.subscription, null, true))
                    //result.add( ApiReader.exportIssueEntitlements(subPkg, ApiCollectionReader.IGNORE_TIPP, null))

                    // only add sub if orgList is not empty
                    result.add(sub)
                }
                else {
                    if (ApiToolkit.isDebugMode()) {
                        // result.add( ['NO_APPROVAL': subPkg.subscription.globalUID] )
                        result.add(["NO_ACCESS": ApiToolkit.NO_ACCESS_DUE_NO_APPROVAL])
                    }
                }
            }
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}
