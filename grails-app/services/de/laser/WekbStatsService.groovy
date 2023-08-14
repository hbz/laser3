package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.convenience.Favorite
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.ZoneId

@Transactional
class WekbStatsService {

    CacheService cacheService
    ContextService contextService
    GokbService gokbService
    OrgTypeService orgTypeService

    static final String CACHE_KEY = 'WekbStatsService/wekbChanges'

    Map getCurrentChanges() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        if (! cache.get('changes')) {
            updateCache()
        }
        Map result = cache.get('changes') as Map

        List<String> currentPackageIdList = SubscriptionPackage.executeQuery(
                'select distinct sp.pkg.gokbId from SubscriptionPackage sp where sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true)))',
                [context: contextService.getOrg(), current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED]
        )

        List<String> currentProviderIdList = orgTypeService.getCurrentOrgsOfProvidersAndAgencies(contextService.getOrg()).collect{ it.gokbId }

        result.org.my       = currentProviderIdList.intersect(result.org.all.collect{ it.uuid })
        result.package.my   = currentPackageIdList.intersect(result.package.all.collect{ it.uuid })
        result.platform.my  = [] // todo

        result.org.favorites       = Favorite.executeQuery('select org.gokbId from Org org, Favorite fav where fav.org = org and fav.user = :user', [user: contextService.getUser()])
        result.package.favorites   = Favorite.executeQuery('select pkg.gokbId from Package pkg, Favorite fav where fav.pkg = pkg and fav.user = :user', [user: contextService.getUser()])
        result.platform.favorites  = Favorite.executeQuery('select plt.gokbId from Platform plt, Favorite fav where fav.plt = plt and fav.user = :user', [user: contextService.getUser()])

        // TODO
        // PlatformController.show() -> isMyPlatform
        // --- copied from myInstitutionController.currentPlatforms()

        result.counts = [
                all:        result.org.count            + result.platform.count            + result.package.count,
                inLaser:    result.org.countInLaser     + result.platform.countInLaser     + result.package.countInLaser,
                my:         result.org.my.size()        + result.platform.my.size()        + result.package.my.size(),
                favorites:  result.org.favorites.size() + result.platform.favorites.size() + result.package.favorites.size(),
                created:    result.org.created.size()   + result.platform.created.size()   + result.package.created.size(),
                updated:    result.org.updated.size()   + result.platform.updated.size()   + result.package.updated.size()
        ]

        result
    }

    void updateCache() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        Map<String, Object> result = processData(14)
        cache.put('changes', result)
    }

    Map<String, Object> processData(int days) {
        log.debug('WekbStatsService.processData(' + days + ')')
        Map<String, Object> result = [:]

        Date frame = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant())
        String cs = DateUtils.getSDF_yyyyMMdd_HHmmss().format(frame)
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        //log.debug('WekbStatsService.getCurrent() > ' + cs)

        Map base = [changedSince: cs, sort: 'sortname', order: 'asc', stubOnly: true, max: 10000]

        result = [
                query       : [ days: days, changedSince: DateUtils.getLocalizedSDF_noTime().format(frame), call: DateUtils.getLocalizedSDF_noZ().format(new Date()) ],
                counts      : [ : ],
                org         : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ],
                platform    : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ],
                package     : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ]
        ]

        Closure process = { Map map, String key ->
            if (map.result) {
                map.result.sort { it.lastUpdatedDisplay }.each {
                    it.dateCreatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.dateCreatedDisplay))
                    it.lastUpdatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.lastUpdatedDisplay))
                    if (it.lastUpdatedDisplay == it.dateCreatedDisplay) {
                        result[key].created << it.uuid
                    }
                    else {
                        result[key].updated << it.uuid
                    }
                    if (key == 'org')       { it.globalUID = Org.findByGokbId(it.uuid)?.globalUID }
                    if (key == 'platform')  { it.globalUID = Platform.findByGokbId(it.uuid)?.globalUID }
                    if (key == 'package')   { it.globalUID = Package.findByGokbId(it.uuid)?.globalUID }

                    if (it.globalUID) { result[key].countInLaser++ }
                    result[key].all << it
                }
                result[key].count = result[key].created.size() + result[key].updated.size()
            }
        }

        Map orgMap = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + '/searchApi', base + [componentType: 'Org'])
        process(orgMap.warning as Map, 'org')

        Map platformMap = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + '/searchApi', base + [componentType: 'Platform'])
        process(platformMap.warning as Map, 'platform')

        Map packageMap = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + '/searchApi', base + [componentType: 'Package'])
        process(packageMap.warning as Map, 'package')

        result
    }
}
