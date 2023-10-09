package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.convenience.Marker
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.ZoneId

/**
 * This service keeps track of the changes performed in the <a href="https://wekb.hbz-nrw.de">we:kb knowledge base</a>. It replaces the entirely
 * functionality of {@link PendingChange}s for {@link TitleInstancePackagePlatform}s (not for cost items and subscriptions!) just as the immediate
 * successors {@link TitleChange}s and {@link IssueEntitlementChange}s. Periodically, via a Cronjob, the last changes are being retrieved from the we:kb
 * using the {@link ApiSource} to fetch the data which is then being cached
 */
@Transactional
class WekbStatsService {

    CacheService cacheService
    ContextService contextService
    GokbService gokbService
    MarkerService markerService

    static final String CACHE_KEY = 'WekbStatsService/wekbChanges'

    /**
     * Gets the current changes from the cache and assembles them in a map of counts being recently performed. Also the count
     * of bookmarked objects are being put in the map. The map containis the counts of:
     * <ul>
     *     <li>in we:kb altogether</li>
     *     <li>in LAS:eR altogether</li>
     *     <li>subscribed</li>
     *     <li>bookmarked</li>
     *     <li>newly created</li>
     *     <li>updated</li>
     * </ul>
     * objects. The following objects are being traced: {@link Org} (provider), {@link de.laser.Package} and {@link Platform}
     * @return a {@link Map} containing the counts: [all: all, inLaser: in LAS:eR, my: subscribed, marker: bookmarked, created: newly created, updated: updated objects]
     */
    Map getCurrentChanges() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        if (! cache.get('data')) {
            return [:]
        }
        Map result = cache.get('data') as Map

        List<String> orgList    = result.org.all.collect{ it.id }
        List<String> pkgList    = result.package.all.collect{ it.id }
        List<String> pltList    = result.platform.all.collect{ it.id }

        Map<String, List> myXMap = markerService.getMyXMap()

        result.org.my           = myXMap.currentProviderIdList.intersect(orgList)
        result.package.my       = myXMap.currentPackageIdList.intersect(pkgList)
        result.platform.my      = myXMap.currentPlatformIdList.intersect(pltList)

        result.org.marker       = markerService.getObjectsByClassAndType(Org.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(orgList)
        result.package.marker   = markerService.getObjectsByClassAndType(Package.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(pkgList)
        result.platform.marker  = markerService.getObjectsByClassAndType(Platform.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(pltList)

        result.counts = [
                all:        result.org.count            + result.platform.count            + result.package.count,
                inLaser:    result.org.countInLaser     + result.platform.countInLaser     + result.package.countInLaser,
                my:         result.org.my.size()        + result.platform.my.size()        + result.package.my.size(),
                marker:     result.org.marker.size()    + result.platform.marker.size()    + result.package.marker.size(),
                created:    result.org.created.size()   + result.platform.created.size()   + result.package.created.size(),
                updated:    result.org.updated.size()   + result.platform.updated.size()   + result.package.updated.size(),
                deleted:    result.org.deleted.size()   + result.platform.deleted.size()   + result.package.deleted.size()
        ]

        result
    }

    /**
     * Triggered by Cronjob: {@link de.laser.jobs.MuleJob}
     * Triggers the update of the cache of the recent changes performed in the we:kb
     */
    void updateCache() {
        EhcacheWrapper cache = cacheService.getTTL1800Cache(CACHE_KEY)

        Map<String, Object> result = processData()
        cache.put('data', result)
    }

    /**
     * Fetches the changes of the last given amount of days and assembles the counts of:
     * <ul>
     *     <li>providers ({@link Org})</li>
     *     <li>{@link de.laser.Package}s</li>
     *     <li>{@link Platform}</li>
     * </ul>
     * Counted are for each class:
     * <ul>
     *     <li>overall object count in we:kb</li>
     *     <li>overall object count in LAS:eR</li>
     *     <li>created objects</li>
     *     <li>updated objects</li>
     *     <li>all modified objects</li>
     * </ul>
     * The map being returned reflects this structure
     * @param days days to count backwards - from when should changes being considered?
     * @return a {@link Map} containing the counts of objects
     */
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
                org         : [ count: 0, countInLaser: 0, created: [], updated: [], deleted: [], all: [] ],
                platform    : [ count: 0, countInLaser: 0, created: [], updated: [], deleted: [], all: [] ],
                package     : [ count: 0, countInLaser: 0, created: [], updated: [], deleted: [], all: [] ]
        ]

        Closure process = { Map map, String key ->
            if (map.result) {
//                map.result.sort { it.lastUpdatedDisplay }.each {
                map.result.sort { it.sortname }.each { it ->
                    it.dateCreatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.dateCreatedDisplay))
                    it.lastUpdatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.lastUpdatedDisplay))
                    if (it.lastUpdatedDisplay == it.dateCreatedDisplay) {
                        result[key].created << it.uuid
                    }
                    else {
                        result[key].updated << it.uuid
                    }
                    if (it.status.toLowerCase() in ['removed', 'deleted']) {
                        result[key].deleted << it.uuid
                    }

                    it.remove('componentType')
                    it.remove('dateCreatedDisplay')
                    it.remove('sortname')
                    it.remove('status')

                    if (key == 'org')       {
                        Org o = Org.findByGokbId(it.uuid)
                        it.id = o?.id
                        it.globalUID = o?.globalUID
                    }
                    if (key == 'platform')  {
                        Platform p = Platform.findByGokbId(it.uuid)
                        it.id = p?.id
                        it.globalUID = p?.globalUID
                    }
                    if (key == 'package')   {
                        Package p = Package.findByGokbId(it.uuid)
                        it.id = p?.id
                        it.globalUID = p?.globalUID
                    }

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
