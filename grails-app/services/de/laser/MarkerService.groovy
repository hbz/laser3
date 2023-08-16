package de.laser

import de.laser.convenience.Marker
import de.laser.interfaces.MarkerSupport
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional


@Transactional
class MarkerService {

    ContextService contextService
    OrgTypeService orgTypeService

    List<Marker> getMarkersByClassAndType(Class cls, Marker.TYPE type) {
        List<Marker> markers = []
        String sql

        if (cls == Org.class) {
            sql = 'select m from Marker m where m.org != null and m.type = :type and m.user = :user'
        }
        else if (cls == Package.class) {
            sql = 'select m from Marker m where m.pkg != null and m.type = :type and m.user = :user'
        }
        else if (cls == Platform.class) {
            sql = 'select m from Marker m where m.plt != null and m.type = :type and m.user = :user'
        }

        if (sql) {
            markers = Marker.executeQuery(sql, [type: type, user: contextService.getUser()])
        }
        markers
    }

    List<MarkerSupport> getObjectsByClassAndType(Class cls, Marker.TYPE type) {
        List<MarkerSupport> objects = []
        String sql

        if (cls == Org.class) {
            sql = 'select obj from Org obj, Marker m where m.org = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name'
        }
        else if (cls == Package.class) {
            sql = 'select obj from Package obj, Marker m where m.pkg = obj and m.type = :type and m.user = :user order by obj.sortname, obj.name'
        }
        else if (cls == Platform.class) {
            sql = 'select obj from Platform obj, Marker m where m.plt = obj and m.type = :type and m.user = :user order by obj.normname, obj.name'
        }

        if (sql) {
            objects = Marker.executeQuery(sql, [type: type, user: contextService.getUser()])
        }
        objects
    }

    Map<String, List> getMyXMap() {
        Map<String, List> result = [:]

        result.currentPackageIdList = SubscriptionPackage.executeQuery(
                'select distinct sp.pkg.gokbId from SubscriptionPackage sp where sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true)))',
                [context: contextService.getOrg(), current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED]
        )

        result.currentProviderIdList = orgTypeService.getCurrentOrgsOfProvidersAndAgencies(contextService.getOrg()).collect{ it.gokbId }

        // todo --
        List<Long> currentSubscriptionIdList = Subscription.executeQuery(
                'select distinct s.id from OrgRole oo join oo.sub s where oo.org = :context and oo.roleType in (:roleTypes) and (s.status = :current or (s.status = :expired and s.hasPerpetualAccess = true))'
                        + (contextService.getOrg().isCustomerType_Consortium() ? ' and s.instanceOf = null' : ''),
                [context: contextService.getOrg(), roleTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA], current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED]
        )
        // println currentSubscriptionIdList.size()

        result.currentPlatformIdList = Platform.executeQuery('select distinct p.id from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, ' +
                'TitleInstancePackagePlatform tipp join tipp.platform p left join p.org o where tipp.pkg = pkg and s.id in (:subIds) ' +
                'and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted)) and ((tipp.status is null) or (tipp.status != :tippRemoved)) ',
                [subIds: currentSubscriptionIdList, pkgDeleted: RDStore.PACKAGE_STATUS_DELETED, tippRemoved: RDStore.TIPP_STATUS_REMOVED]
        )

        result
    }
}
