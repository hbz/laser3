package de.laser

import de.laser.convenience.Marker
import de.laser.interfaces.MarkerSupport
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional

/**
 * This service handles the we:kb bookmarks of a given user
 * @see Marker
 */
@Transactional
class MarkerService {

    ContextService contextService

    /**
     * Gets the bookmarks for the given class of the given type
     * @param cls the class for which to fetch markers - one of {@link Org}, {@link de.laser.wekb.Package} or {@link de.laser.wekb.Platform}
     * @param type
     * @return
     */
    List<Marker> getMarkersByClassAndType(Class cls, Marker.TYPE type) {
        List<Marker> markers = []
        String sql

             if (cls == Org.class)      { sql = 'where m.org != null' }
        else if (cls == Package.class)  { sql = 'where m.pkg != null' }
        else if (cls == Platform.class) { sql = 'where m.plt != null' }
        else if (cls == Provider.class) { sql = 'where m.prov != null' }
        else if (cls == Vendor.class)   { sql = 'where m.ven != null' }
        else if (cls == TitleInstancePackagePlatform.class) { sql = 'where m.tipp != null' }

        if (sql) {
            markers = Marker.executeQuery(
                    'select m from Marker m ' + sql + ' and m.type = :type and m.user = :user',
                    [type: type, user: contextService.getUser()]
            )
        }
        markers
    }

    /**
     * Gets all objects belonging to the given class which have been bookmarked by the given type
     * @param cls the class of objects to fetch - one of {@link Org}, {@link Package}, {@link Platform}
     * @param type the type of marker to fetch - one of {@link Marker.TYPE}
     * @return a {@link List} of {@link MarkerSupport} bookmarks
     */
    List<MarkerSupport> getMyObjectsByClassAndType(Class cls, Marker.TYPE type) {
        List<MarkerSupport> objects = []
        String sql

             if (cls == Org.class)      { sql = 'Org obj where m.org = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name' }
        else if (cls == Package.class)  { sql = 'Package obj where m.pkg = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name' }
        else if (cls == Platform.class) { sql = 'Platform obj where m.plt = obj and m.type = :type and m.user = :user order by obj.normname, obj.name' }
        else if (cls == Provider.class) { sql = 'Provider obj where m.prov = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name' }
        else if (cls == Vendor.class)   { sql = 'Vendor obj where m.ven = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name' }
        else if (cls == TitleInstancePackagePlatform.class) { sql = 'TitleInstancePackagePlatform obj where m.tipp = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name' }

        if (sql) {
            objects = Marker.executeQuery('select obj from Marker m, ' + sql, [type: type, user: contextService.getUser()])
        }
        objects
    }

    /**
     * Builds a map of currently subscribed packages and platforms
     * @return a {@link Map} of {@link de.laser.wekb.Package} IDs and {@link Platform} IDs to which there exists a link from a {@link Subscription} (= are subscribed by the context institution)
     */
    Map<String, List> getMyCurrentXMap() {
        Map<String, List> result = [
                currentOrgIdList : [], // todo
                currentProviderIdList : [],
                currentVendorIdList : [],
                currentPlatformIdList : [],
                currentPackageIdList : [],
                currentTippIdList : [] // todo
        ]

        String subStatusQuery = '(sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true))'

        result.currentPackageIdList = SubscriptionPackage.executeQuery(
                'select distinct sp.pkg.id from SubscriptionPackage sp where sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and ' + subStatusQuery + ')',
                [context: contextService.getOrg(), current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED]
        )

        // todo - provider.status // oo.roleType
        result.currentProviderIdList = Provider.executeQuery(
                'select distinct pr.provider.id from ProviderRole pr, OrgRole oo where ('
                + '(pr.subscription = oo.sub and (oo.sub.status = :subCurrent or (oo.sub.status = :subExpired and oo.sub.hasPerpetualAccess = true))) '
                + 'or '
                + '(pr.license = oo.lic and oo.lic.status = :licCurrent) '
                + ') and oo.org = :context',
                [context: contextService.getOrg(), subCurrent: RDStore.SUBSCRIPTION_CURRENT, subExpired: RDStore.SUBSCRIPTION_EXPIRED, licCurrent: RDStore.LICENSE_CURRENT]
        )
        // todo - vendor.status // oo.roleType
        result.currentVendorIdList = Vendor.executeQuery(
                'select distinct vr.vendor.id from VendorRole vr, OrgRole oo where ('
                        + '(vr.subscription = oo.sub and (oo.sub.status = :subCurrent or (oo.sub.status = :subExpired and oo.sub.hasPerpetualAccess = true))) '
                        + 'or '
                        + '(vr.license = oo.lic and oo.lic.status = :licCurrent) '
                        + ') and oo.org = :context',
                [context: contextService.getOrg(), subCurrent: RDStore.SUBSCRIPTION_CURRENT, subExpired: RDStore.SUBSCRIPTION_EXPIRED, licCurrent: RDStore.LICENSE_CURRENT]
        )

        List<Long> currentSubscriptionIdList = Subscription.executeQuery(
                'select distinct sub.id from OrgRole oo join oo.sub sub where oo.org = :context and oo.roleType in (:roleTypes) and '
                        + subStatusQuery
                        + (contextService.getOrg().isCustomerType_Consortium() ? ' and sub.instanceOf = null' : ''),
                [
                        context:    contextService.getOrg(),
                        roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM],
                        current:    RDStore.SUBSCRIPTION_CURRENT,
                        expired:    RDStore.SUBSCRIPTION_EXPIRED
                ]
        )

        result.currentPlatformIdList = Platform.executeQuery(
                'select distinct p.id from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, ' +
                'TitleInstancePackagePlatform tipp join tipp.platform p left join p.org o where tipp.pkg = pkg and s.id in (:subIds) ' +
                'and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted)) and ((tipp.status is null) or (tipp.status != :tippRemoved)) ',
                [subIds: currentSubscriptionIdList, pkgDeleted: RDStore.PACKAGE_STATUS_DELETED, tippRemoved: RDStore.TIPP_STATUS_REMOVED]
        )

        result
    }
}
