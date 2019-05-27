package de.laser.api.v0.special

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Package
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.Platform
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiStatistic {

    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                //"select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue", [
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                            key    : OrgSettings.KEYS.STATISTICS_SERVER_ACCESS,
                            rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                            deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus')
                    ])

        orgs
    }

    static getAllPackages() {
        def result = []

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

        packages.each{ p ->
            result << ApiReaderHelper.resolvePackageStub(p, null) // ? null
        }

        return (result ? new JSON(result) : null)
    }

    static getPackage(Package pkg) {
        if (! pkg || pkg.packageStatus?.value == 'Deleted') {
            return null
        }
        def result = [:]

        result.globalUID        = pkg.globalUID
        result.startDate        = pkg.startDate
        result.endDate          = pkg.endDate
        result.lastUpdated      = pkg.lastUpdated
        result.packageType      = pkg.packageType?.value
        result.packageStatus    = pkg.packageStatus?.value
        result.name             = pkg.name
        result.variantNames     = ['TODO-TODO-TODO'] // todo

        // References
        result.contentProvider  = resolvePkgOrganisations(pkg.orgs)
        result.license          = resolvePkgLicense(pkg.license)
        result.identifiers      = ApiReaderHelper.resolveIdentifiers(pkg.ids) // com.k_int.kbplus.IdentifierOccurrence
        //result.platforms        = resolvePkgPlatforms(pkg.nominalPlatform)
        //result.tipps            = resolvePkgTipps(pkg.tipps)
        result.subscriptions    = resolvePkgSubscriptions(pkg.subscriptions, ApiStatistic.getAccessibleOrgs())

        result = ApiReaderHelper.cleanUp(result, true, true)

        return (result ? new JSON(result) : null)
    }

    static List resolvePkgOrganisations(Set<OrgRole> orgRoles) {
        if (! orgRoles) {
            return null
        }
        def result = []
        orgRoles.each { ogr ->
            if (ogr.roleType.id == RDStore.OR_CONTENT_PROVIDER.id) {
                if (ogr.org.status?.value == 'Deleted') {
                }
                else {
                    result.add(ApiReaderHelper.resolveOrganisationStub(ogr.org, null))
                }
            }
        }

        return ApiReaderHelper.cleanUp(result, true, true)
    }

    static resolvePkgLicense(License lic) {
        if (! lic || lic.status?.value == 'Deleted') {
            return null
        }
        def result = ApiReaderHelper.resolveLicenseStub(lic, null, true)

        return ApiReaderHelper.cleanUp(result, true, true)
    }

    /*
    // TODO nominalPlatform? or tipps?
    static resolvePkgPlatforms(Platform  pform) {
        if (! pform) {
            return null
        }
        def result = [:]

        result.globalUID    = pform.globalUID
        result.name         = pform.name
        //result.identifiers  = ApiReaderHelper.resolveIdentifiers(pform.ids) // com.k_int.kbplus.IdentifierOccurrence

        return ApiReaderHelper.cleanUp(result, true, true)
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
            result.add( ApiReaderHelper.resolveTipp(tipp, ApiReaderHelper.IGNORE_NONE, null))
        }

        return ApiReaderHelper.cleanUp(result, true, true)
    }
    */

    static resolvePkgSubscriptions(Set<SubscriptionPackage> subscriptionPackages, List<Org> accessibleOrgs) {
        if (! subscriptionPackages) {
            return null
        }

        def result = []
        subscriptionPackages.each { subPkg ->

            def sub = [:]

            if (subPkg.subscription.status?.value == 'Deleted') {
            }
            else {
                sub = ApiReaderHelper.resolveSubscriptionStub(subPkg.subscription, null, true)
            }

            List<Org> orgList = []

            OrgRole.findAllBySub(subPkg.subscription).each { ogr ->

                if (ogr.roleType?.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id]) {
                    if (ogr.org.id in accessibleOrgs.collect{ it -> it.id }) {

                        if (ogr.org.status?.value == 'Deleted') {
                        }
                        else {
                            def org = ApiReaderHelper.resolveOrganisationStub(ogr.org, null)
                            if (org) {
                                orgList.add(ApiReaderHelper.cleanUp(org, true, true))
                            }
                        }
                    }
                }
            }
            if (orgList) {
                sub?.put('organisations', ApiReaderHelper.cleanUp(orgList, true, true))
            }

            List<IssueEntitlement> ieList = []

            def tipps = TitleInstancePackagePlatform.findAllByPkgAndSub(subPkg.pkg, subPkg.subscription)

            //println 'subPkg (' + subPkg.pkg?.id + " , " + subPkg.subscription?.id + ") > " + tipps

            tipps.each{ tipp ->
                if (tipp.status?.value == 'Deleted') {
                }
                else {
                    def ie = IssueEntitlement.findBySubscriptionAndTipp(subPkg.subscription, tipp)
                    if (ie) {
                        if (ie.status?.value == 'Deleted') {

                        } else {
                            ieList.add(ApiReaderHelper.resolveIssueEntitlement(ie, ApiReaderHelper.IGNORE_SUBSCRIPTION_AND_PACKAGE, null))
                        }
                    }
                }
            }
            if (ieList) {
                sub?.put('issueEntitlements', ApiReaderHelper.cleanUp(ieList, true, true))
            }

            //result.add( ApiReaderHelper.resolveSubscriptionStub(subPkg.subscription, null, true))
            //result.add( ApiReader.exportIssueEntitlements(subPkg, ApiReaderHelper.IGNORE_TIPP, null))
            result.add( sub )
        }

        return ApiReaderHelper.cleanUp(result, true, true)
    }

    /**
     * @return []
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

}
