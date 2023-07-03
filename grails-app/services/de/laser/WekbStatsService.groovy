package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.remote.ApiSource
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.ZoneId

@Transactional
class WekbStatsService {

    CacheService cacheService
    ContextService contextService
    GokbService gokbService

    static final String CACHE_KEY = 'WekbStatsService/wekbChanges'

    def getCurrentChanges() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        if (! cache.get('changes')) {
            updateCache()
        }
        cache.get('changes') as Map
    }

    void updateCache() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        Map<String, Object> result = processData(14)
        cache.put('changes', result)
    }

    Map<String, Object> processData(int days) {
        log.debug('WekbStatsService.getCurrent(' + days + ')')
        Map<String, Object> result = [:]

        Date frame = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant())
        String cs = DateUtils.getSDF_yyyyMMdd_HHmmss().format(frame)
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        log.debug('WekbStatsService.getCurrent() > ' + cs)

        Map base = [changedSince: cs, sort: 'sortname', order: 'asc', stubOnly: true, max: 10000]

        result = [
                query       : [ days: days, changedSince: DateUtils.getLocalizedSDF_noTime().format(frame), call: DateUtils.getLocalizedSDF_noZ().format(new Date()) ],
                org         : [ count: 0, created: [], updated: [], all: [] ],
                platform    : [ count: 0, created: [], updated: [], all: [] ],
                package     : [ count: 0, created: [], updated: [], all: [] ]
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

                    result[key].all << it
                }
                result[key].count = result[key].created.size() + result[key].updated.size()
            }
        }

        Map orgMap = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + 'searchApi', base + [componentType: 'Org'])
        process(orgMap.warning as Map, 'org')

        Map platformMap = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + 'searchApi', base + [componentType: 'Platform'])
        process(platformMap.warning as Map, 'platform')

        Map packageMap = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + 'searchApi', base + [componentType: 'Package'])
        process(packageMap.warning as Map, 'package')

        result
    }
}
