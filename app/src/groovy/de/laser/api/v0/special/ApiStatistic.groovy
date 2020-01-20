package de.laser.api.v0.special

import com.k_int.kbplus.*
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiStubReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiStatistic {

    static boolean calculateAccess(Package pkg) {

        if (pkg in getAccessiblePackages()) {
            return true
        }
        else {
            return false
        }
    }

    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                            key    : OrgSettings.KEYS.NATSTAT_SERVER_ACCESS,
                            rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                            deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus')
                    ])

        orgs
    }

    static private List<Package> getAccessiblePackages() {

        List<Package> packages = []
        List<Org> orgs = getAccessibleOrgs()

        if (orgs) {
            packages = com.k_int.kbplus.Package.executeQuery(
                    "select pkg from SubscriptionPackage sp " +
                            "join sp.pkg pkg join sp.subscription s join s.orgRelations ogr join ogr.org o " +
                            "where o in (:orgs) and (pkg.packageStatus is null or pkg.packageStatus != :deleted)", [
                    orgs: orgs,
                    deleted: RefdataValue.getByValueAndCategory('Deleted', 'Package Status')
                ]
            )
        }

        packages
    }

    /**
     * Access checked via ApiManager.read()
     * @return JSON
     */
    static JSON getAllPackages() {
        Collection<Object> result = []

        getAccessiblePackages().each { p ->
            result << ApiStubReader.retrievePackageStubMap(p, null) // ? null
        }

        return new JSON(result)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getPackage(Package pkg, Org context) {
        if (! pkg || pkg.packageStatus?.value == 'Deleted') {
            return null
        }

        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(pkg)
        if (hasAccess) {

            result.globalUID        = pkg.globalUID
            result.startDate        = pkg.startDate
            result.endDate          = pkg.endDate
            result.lastUpdated      = pkg.lastUpdated
            result.packageType      = pkg.packageType?.value
            result.packageStatus    = pkg.packageStatus?.value
            result.name             = pkg.name
            result.variantNames     = ['TODO-TODO-TODO'] // todo

            // References
            result.contentProvider  = retrievePkgOrganisationCollection(pkg.orgs)
            result.license          = requestPkgLicense(pkg.license)
            result.identifiers      = ApiCollectionReader.retrieveIdentifierCollection(pkg.ids) // com.k_int.kbplus.Identifier
            //result.platforms        = resolvePkgPlatforms(pkg.nominalPlatform)
            //result.tipps            = resolvePkgTipps(pkg.tipps)
            result.subscriptions    = retrievePkgSubscriptionCollection(pkg.subscriptions, getAccessibleOrgs())

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    static private Collection<Object> retrievePkgOrganisationCollection(Set<OrgRole> orgRoles) {
        if (! orgRoles) {
            return null
        }

        Collection<Object> result = []
        orgRoles.each { ogr ->
            if (ogr.roleType.id == RDStore.OR_CONTENT_PROVIDER.id) {
                if (ogr.org.status?.value == 'Deleted') {
                }
                else {
                    result.add(ApiStubReader.retrieveOrganisationStubMap(ogr.org, null))
                }
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    static private requestPkgLicense(License lic) {
        if (! lic || lic.status?.value == 'Deleted') {
            return null
        }
        def result = ApiStubReader.requestLicenseStub(lic, null)

        return ApiToolkit.cleanUp(result, true, true)
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

    static private Collection<Object> retrievePkgSubscriptionCollection(Set<SubscriptionPackage> subscriptionPackages, List<Org> accessibleOrgs) {
        if (!subscriptionPackages) {
            return null
        }

        Collection<Object> result = []
        subscriptionPackages.each { subPkg ->

            def sub = [:]

            if (subPkg.subscription.status?.value == 'Deleted') {
            }
            else {
                sub = ApiStubReader.requestSubscriptionStub(subPkg.subscription, null)
            }

            List<Org> orgList = []

            OrgRole.findAllBySub(subPkg.subscription).each { ogr ->

                if (ogr.roleType?.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id]) {
                    if (ogr.org.id in accessibleOrgs.collect { it.id }) {

                        if (ogr.org.status?.value == 'Deleted') {
                        }
                        else {
                            def org = ApiStubReader.retrieveOrganisationStubMap(ogr.org, null)
                            if (org) {
                                orgList.add(ApiToolkit.cleanUp(org, true, true))
                            }
                        }
                    }
                }
            }
            if (orgList) {

                sub?.put('organisations', ApiToolkit.cleanUp(orgList, true, true))

                List<IssueEntitlement> ieList = []

                def tipps = TitleInstancePackagePlatform.findAllByPkgAndSub(subPkg.pkg, subPkg.subscription)

                //println 'subPkg (' + subPkg.pkg?.id + " , " + subPkg.subscription?.id + ") > " + tipps

                tipps.each { tipp ->
                    if (tipp.status?.value == 'Deleted') {
                    } else {
                        def ie = IssueEntitlement.findBySubscriptionAndTipp(subPkg.subscription, tipp)
                        if (ie) {
                            if (ie.status?.value == 'Deleted') {

                            } else {
                                ieList.add(ApiCollectionReader.retrieveIssueEntitlementMap(ie, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, null))
                            }
                        }
                    }
                }
                if (ieList) {
                    sub?.put('issueEntitlements', ApiToolkit.cleanUp(ieList, true, true))
                }

                //result.add( ApiStubReader.resolveSubscriptionStub(subPkg.subscription, null, true))
                //result.add( ApiReader.exportIssueEntitlements(subPkg, ApiCollectionReader.IGNORE_TIPP, null))

                // only add sub if orgList is not empty
                result.add(sub)
            }
            else {
                // result.add( ['NO_APPROVAL': subPkg.subscription.globalUID] )
                result.add( ['NO_APPROVAL': 'NO_APPROVAL'] )
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
}
