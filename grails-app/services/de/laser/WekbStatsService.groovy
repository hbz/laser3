package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.convenience.Marker
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
    MarkerService markerService

    static final String CACHE_KEY = 'WekbStatsService/wekbChanges'

    Map getCurrentChanges() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        if (! cache.get('data')) {
            updateCache()
        }
        Map result = cache.get('data') as Map

        List<String> orgList    = result.org.all.collect{ it.uuid }
        List<String> pkgList    = result.package.all.collect{ it.uuid }
        List<String> pltList    = result.platform.all.collect{ it.uuid }

        Map<String, List> myXMap = markerService.getMyXMap()

        result.org.my           = myXMap.currentProviderIdList.intersect(orgList)
        result.package.my       = myXMap.currentPackageIdList.intersect(pkgList)
        result.platform.my      = myXMap.currentPlatformIdList.intersect(pltList)

        result.org.marker       = markerService.getObjectsByClassAndType(Org.class, Marker.TYPE.WEKB_CHANGES).collect { it.gokbId }.intersect(orgList)
        result.package.marker   = markerService.getObjectsByClassAndType(Package.class, Marker.TYPE.WEKB_CHANGES).collect { it.gokbId }.intersect(pkgList)
        result.platform.marker  = markerService.getObjectsByClassAndType(Platform.class, Marker.TYPE.WEKB_CHANGES).collect { it.gokbId }.intersect(pltList)

        result.counts = [
                all:        result.org.count            + result.platform.count            + result.package.count,
                inLaser:    result.org.countInLaser     + result.platform.countInLaser     + result.package.countInLaser,
                my:         result.org.my.size()        + result.platform.my.size()        + result.package.my.size(),
                marker:     result.org.marker.size()    + result.platform.marker.size()    + result.package.marker.size(),
                created:    result.org.created.size()   + result.platform.created.size()   + result.package.created.size(),
                updated:    result.org.updated.size()   + result.platform.updated.size()   + result.package.updated.size()
        ]

        result
    }

    void updateCache() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        Map<String, Object> result = processData()
        cache.put('data', result)
    }

    Map<String, Object> processData(int days = 14) {
        log.debug('WekbStatsService.processData(' + days + ' days)')
        Map<String, Object> result = [:]

        Date frame = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant())
        String cs = DateUtils.getSDF_yyyyMMdd_HHmmss().format(frame)
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        //log.debug('WekbStatsService.getCurrent() > ' + cs)

        Map base = [changedSince: cs, sort: 'lastUpdatedDisplay', order: 'desc', stubOnly: true, max: 10000]

        result = [
                query       : [ days: days, changedSince: DateUtils.getLocalizedSDF_noTime().format(frame), call: DateUtils.getLocalizedSDF_noZ().format(new Date()) ],
                counts      : [ : ],
                org         : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ],
                platform    : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ],
                package     : [ count: 0, countInLaser: 0, created: [], updated: [], all: [] ]
        ]

        Closure process = { Map map, String key ->
            if (map.result) {
//                map.result.sort { it.lastUpdatedDisplay }.each {
                map.result.sort { it.sortname }.each {
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
