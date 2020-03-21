package de.laser.api.v0.special

import com.k_int.kbplus.*
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.api.v0.ApiUnsecuredMapReader
import de.laser.helper.Constants
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiStatistic {

    /**
     * checks implicit NATSTAT_SERVER_ACCESS
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
     * checks NATSTAT_SERVER_ACCESS
     */
    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                            key    : OrgSettings.KEYS.NATSTAT_SERVER_ACCESS,
                            rdValue: RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N),
                            deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)
                    ])

        orgs
    }

    /**
     * checks implicit NATSTAT_SERVER_ACCESS
     */
    static private List<Package> getAccessiblePackages() {

        List<Package> packages = []
        List<Org> orgs = getAccessibleOrgs()

        if (orgs) {
            packages = com.k_int.kbplus.Package.executeQuery(
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
     * checks implicit NATSTAT_SERVER_ACCESS
     *
     * @return JSON
     */
    static JSON getAllPackages() {
        Collection<Object> result = []

        getAccessiblePackages().each { p ->
            result << ApiUnsecuredMapReader.getPackageStubMap(p)
        }

        return new JSON(result)
    }

    /**
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
            result.lastUpdated      = ApiToolkit.formatInternalDate(pkg.lastUpdated)
            result.packageType      = pkg.contentType?.value
            result.packageStatus    = pkg.packageStatus?.value
            result.name             = pkg.name
            result.variantNames     = ['TODO-TODO-TODO'] // todo

            // References
            result.contentProvider  = getPkgOrganisationCollection(pkg.orgs)
            result.license          = getPkgLicense(pkg.license)
            result.identifiers      = ApiCollectionReader.getIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier
            //result.platforms        = resolvePkgPlatforms(pkg.nominalPlatform)
            //result.tipps            = resolvePkgTipps(pkg.tipps)
            result.subscriptions    = getPkgSubscriptionCollection(pkg.subscriptions, getAccessibleOrgs())

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

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

    static private getPkgLicense(License lic) {
        if (! lic) {
            return null
        }
        else if (! lic.isPublicForApi) {
            return ["NO_ACCESS" : ApiToolkit.NO_ACCESS_DUE_NOT_PUBLIC]
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
        //result.identifiers  = ApiCollectionReader.resolveIdentifiers(pform.ids) // com.k_int.kbplus.IdentifierOccurrence

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

    static private Collection<Object> getPkgSubscriptionCollection(Set<SubscriptionPackage> subscriptionPackages, List<Org> accessibleOrgs) {
        if (!subscriptionPackages) {
            return null
        }

        Collection<Object> result = []
        subscriptionPackages.each { subPkg ->

            if (! subPkg.subscription.isPublicForApi) {
                result.add(["NO_ACCESS" : ApiToolkit.NO_ACCESS_DUE_NOT_PUBLIC])
            }
            else {
                Map<String, Object> sub = ApiUnsecuredMapReader.getSubscriptionStubMap(subPkg.subscription)

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
                    // result.add( ['NO_APPROVAL': subPkg.subscription.globalUID] )
                    result.add( ["NO_ACCESS" : ApiToolkit.NO_ACCESS_DUE_NO_APPROVAL] )
                }
            }
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}
