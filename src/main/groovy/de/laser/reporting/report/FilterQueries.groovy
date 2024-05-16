package de.laser.reporting.report

import de.laser.ContextService
import de.laser.Package
import de.laser.Platform
import de.laser.Provider
import de.laser.Subscription
import de.laser.Vendor
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import groovy.util.logging.Slf4j

@Slf4j
class FilterQueries {

    static List<Long> getSubscriptionIdList() {
        ContextService contextService = BeanStore.getContextService()

        List<Long> idList = Subscription.executeQuery(
                "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx)", [
                roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS],
                ctx: contextService.getOrg()
        ])

        idList
    }

    static List<Long> getAllPackageIdList() {
        List<Long> idList = Package.executeQuery( 'select pkg.id from Package pkg' )
//                queryParams.packageIdList = Package.executeQuery( 'select pkg.id from Package pkg where pkg.packageStatus != :pkgStatus',
//                        [pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
//                )
        idList
    }

    static List<Long> getMyPackageIdList() {
        List<Long> subIdList = FilterQueries.getSubscriptionIdList()

        List<Long> idList = subIdList ? Package.executeQuery(
                'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:subIdList)', [subIdList: subIdList]
        ) : []
//                queryParams.packageIdList = Package.executeQuery(
//                        'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:subIdList) and subPkg.pkg.packageStatus != :pkgStatus',
//                        [subIdList: subIdList, pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
//                )
        idList
    }

    static List<Long> getAllPlatformIdList() {
        List<Long> idList = Platform.executeQuery( 'select plt.id from Platform plt')
//                queryParams.platformIdList = Platform.executeQuery( 'select plt.id from Platform plt where plt.status != :status',
//                        [status: RDStore.PLATFORM_STATUS_DELETED]
//                )
        idList
    }

    static List<Long> getMyPlatformIdList() {
        List<Long> subIdList = FilterQueries.getSubscriptionIdList()

        List<Long> platformIdList1 = Platform.executeQuery(
                'select distinct plt.id from ProviderRole pr join pr.subscription sub join pr.provider pro join pro.platforms plt where sub.id in (:subIdList)',
                [subIdList: subIdList]
        )
        List<Long> platformIdList2 = Platform.executeQuery(
                'select distinct plt.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.nominalPlatform plt where sub.id in (:subIdList)',
                [subIdList: subIdList]
        )
        (platformIdList1 + platformIdList2).unique() as List<Long>
    }

    static List<Long> getAllProviderIdList() {
        List<Long> idList = Provider.executeQuery(
                'select pro.id from Provider pro',
//                'select pro.id from Provider pro where (pro.status is null or pro.status != :providerStatus)',
//                [providerStatus: RDStore.PROVIDER_STATUS_DELETED]
        )
        idList
    }

    static List<Long> getMyProviderIdList() {
        List<Long> subIdList = FilterQueries.getSubscriptionIdList()

        List<Long> providerIdList1 = subIdList ? Platform.executeQuery(
                'select distinct pro.id from ProviderRole pr join pr.subscription sub join pr.provider pro where sub.id in (:subIdList)',
                [subIdList: subIdList]
        ) : []
        List<Long> providerIdList2 = subIdList ? Platform.executeQuery(
                'select distinct pro.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.provider pro where sub.id in (:subIdList)',
                [subIdList: subIdList]
        ) : []

//        List<Long> idList = Provider.executeQuery( '''
//            select distinct pr.provider.id from ProviderRole pr
//                join pr.subscription sub
//                join sub.orgRelations subOr
//            where (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))
//            ''', [
//                org: contextService.getOrg(),
////                providerStatus: RDStore.PROVIDER_STATUS_DELETED,
//                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]
//            ]
//        )
//        println 'idList ' + idList.size()
        // and (pr.provider.status is null or pr.provider.status != :providerStatus)
        (providerIdList1 + providerIdList2).unique() as List<Long>
    }

    static List<Long> getAllVendorIdList() {

        List<Long> idList = Vendor.executeQuery(
                'select ven.id from Vendor ven'
//                'select ven.id from Vendor ven where (ven.status is null or ven.status != :vendorStatus)',
//                [vendorStatus: RDStore.VENDOR_STATUS_DELETED]
        )
        idList
    }

    static List<Long> getMyVendorIdList() {
        List<Long> subIdList = FilterQueries.getSubscriptionIdList()

        List<Long> vendorIdList1 = subIdList ? Platform.executeQuery(
                'select distinct ven.id from VendorRole vr join vr.subscription sub join vr.vendor ven where sub.id in (:subIdList)',
                [subIdList: subIdList]
        ) : []
        List<Long> vendorIdList2 = subIdList ? Platform.executeQuery(
                'select distinct pv.vendor.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.vendors pv where sub.id in (:subIdList)',
                [subIdList: subIdList]
        ) : []

//        List<Long> idList = Vendor.executeQuery( '''
//            select distinct vr.vendor.id from VendorRole vr
//                join vr.subscription sub
//                join sub.orgRelations subOr
//            where (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))
//            ''', [
//                org: contextService.getOrg(),
////                vendorStatus: RDStore.VENDOR_STATUS_DELETED,
//                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]
//            ]
//        )
//        println 'idList ' + idList.size()
        // and (vr.vendor.status is null or vr.vendor.status != :vendorStatus)
        (vendorIdList1 + vendorIdList2).unique() as List<Long>
    }
}
