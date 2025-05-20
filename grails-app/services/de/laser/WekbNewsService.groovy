package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.convenience.Marker
import de.laser.remote.Wekb
import de.laser.utils.DateUtils
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional

import java.time.LocalDate
import java.time.ZoneId

/**
 * This service keeps track of the changes performed in the <a href="https://wekb.hbz-nrw.de">we:kb knowledge base</a>. It replaces the entirely
 * functionality of {@link PendingChange}s for {@link de.laser.wekb.TitleInstancePackagePlatform}s (not for cost items and subscriptions!) just as the immediate
 * successors {@link TitleChange}s and {@link IssueEntitlementChange}s. Periodically, via a Cronjob, the last changes are being retrieved from the we:kb
 * using the {@link Wekb} to fetch the data which is then being cached
 */
@Transactional
class WekbNewsService {

    CacheService cacheService
    ContextService contextService
    GokbService gokbService
    MarkerService markerService

    static final String CACHE_KEY = 'WekbNewsService'

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
     * objects. The following objects are being traced: {@link Org} (provider), {@link de.laser.wekb.Package} and {@link de.laser.wekb.Platform}
     * @return a {@link Map} containing the counts: [all: all, inLaser: in LAS:eR, my: subscribed, marker: bookmarked, created: newly created, updated: updated objects]
     */
    Map getCurrentNews() {
        EhcacheWrapper ttl1800 = cacheService.getTTL1800Cache(CACHE_KEY)

        if (! ttl1800.get('wekbNews')) {
            return [:]
        }
        Map result = ttl1800.get('wekbNews') as Map

        ['package', 'platform', 'provider', 'vendor'].each { type ->
            ['all', 'created', 'deleted'].each { lst -> // ensure lists
                if (result[type][lst] == null) {
                    result[type][lst] = []
                    log.debug('wekbNews: missing ' + type + '.' + lst + ' > set default to []')
                }
            }
        }

        List<String> pkgList    = result.package.all.collect{ it.id }
        List<String> pltList    = result.platform.all.collect{ it.id }
        List<String> prvList    = result.provider.all.collect{ it.id }
        List<String> venList    = result.vendor.all.collect{ it.id }

        Map<String, List> myXMap = markerService.getMyCurrentXMap()

        result.package.my       = myXMap.currentPackageIdList.intersect(pkgList)    ?: []
        result.platform.my      = myXMap.currentPlatformIdList.intersect(pltList)   ?: []
        result.provider.my      = myXMap.currentProviderIdList.intersect(prvList)   ?: []
        result.vendor.my        = myXMap.currentVendorIdList.intersect(venList)     ?: []

        result.package.marker   = markerService.getMyObjectsByClassAndType(Package.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(pkgList)    ?: []
        result.platform.marker  = markerService.getMyObjectsByClassAndType(Platform.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(pltList)   ?: []
        result.provider.marker  = markerService.getMyObjectsByClassAndType(Provider.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(prvList)   ?: []
        result.vendor.marker    = markerService.getMyObjectsByClassAndType(Vendor.class, Marker.TYPE.WEKB_CHANGES).collect { it.id }.intersect(venList)     ?: []

        ['package', 'platform', 'provider', 'vendor'].each { type ->
            ['count', 'countInLaser', 'countUpdated'].each { cnt -> // ensure counts
                if (result[type][cnt] == null) {
                    result[type][cnt] = 0
                    log.debug('wekbNews: missing ' + type + '.' + cnt + ' > set default to 0')
                }
            }
        }

        try {
            result.counts.all       = result.package.count          + result.platform.count             + result.provider.count             + result.vendor.count
            result.counts.inLaser   = result.package.countInLaser   + result.platform.countInLaser      + result.provider.countInLaser      + result.vendor.countInLaser
            result.counts.updated   = result.package.countUpdated   + result.platform.countUpdated      + result.provider.countUpdated      + result.vendor.countUpdated

            result.counts.my        = result.package.my.size()       + result.platform.my.size()        + result.provider.my.size()         + result.vendor.my.size()
            result.counts.marker    = result.package.marker.size()   + result.platform.marker.size()    + result.provider.marker.size()     + result.vendor.marker.size()
            result.counts.created   = result.package.created.size()  + result.platform.created.size()   + result.provider.created.size()    + result.vendor.created.size()
            result.counts.deleted   = result.package.deleted.size()  + result.platform.deleted.size()   + result.provider.deleted.size()    + result.vendor.deleted.size()
        }
        catch (Exception e) {
            log.error 'failed getCurrentNews() -> ' + e.getMessage()

            ['package', 'platform', 'provider', 'vendor'].each { type ->
                ['all', 'my', 'marker', 'created', 'deleted', 'count', 'countInLaser', 'countUpdated'].each { x ->
                    if (result[type][x] == null) {
                        log.warn ('wekbNews: IS NULL > ' + type + '.' + x)
                    }
                }
            }
            return [:]
        }
        result
    }

    /**
     * Triggered by Cronjob: {@link de.laser.jobs.MuleJob}
     * Triggers the update of the cache of the recent changes performed in the we:kb
     */
    void updateCache() {
        EhcacheWrapper ttl1800 = cacheService.getTTL1800Cache(CACHE_KEY)

        Map<String, Object> result = processData()
        ttl1800.put('wekbNews', result)
    }

    void clearCache() {
        EhcacheWrapper ttl1800 = cacheService.getTTL1800Cache(CACHE_KEY)
        ttl1800.remove('wekbNews')
    }

    /**
     * Fetches the changes of the last given amount of days and assembles the counts of:
     * <ul>
     *     <li>providers ({@link Org})</li>
     *     <li>{@link de.laser.wekb.Package}s</li>
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
        log.debug('WekbNewsService.processData(' + days + ' days)')
        Map<String, Object> result = [:]

        Date frame = Date.from(LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant())
        String cs = DateUtils.getSDF_yyyyMMdd_HHmmss().format(frame)

        String apiUrl = Wekb.getSearchApiURL()
        //log.debug('WekbNewsService.getCurrent() > ' + cs)

        Map base = [changedSince: cs, sort: 'lastUpdatedDisplay', order: 'desc', stubOnly: true, max: 10000]

        result = [
                query       : [ days: days, changedSince: DateUtils.getLocalizedSDF_noTime().format(frame), call: DateUtils.getLocalizedSDF_noZ().format(new Date()) ],
                counts      : [ : ],
                provider    : [ count: 0, countInLaser: 0, countUpdated: 0, created: [], deleted: [], all: [] ],
                vendor      : [ count: 0, countInLaser: 0, countUpdated: 0, created: [], deleted: [], all: [] ],
                platform    : [ count: 0, countInLaser: 0, countUpdated: 0, created: [], deleted: [], all: [] ],
                package     : [ count: 0, countInLaser: 0, countUpdated: 0, created: [], deleted: [], all: [] ]
        ]

        Closure process = { Map map, String key ->
            if (map.result) {
//                map.result.sort { it.lastUpdatedDisplay }.each {
                map.result.sort { it.sortname }.each { it ->
                    it.dateCreatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.dateCreatedDisplay))
                    it.lastUpdatedDisplay = DateUtils.getLocalizedSDF_noZ().format(DateUtils.parseDateGeneric(it.lastUpdatedDisplay))

                    if (it.status.toLowerCase() in ['removed', 'deleted']) {
                        result[key].deleted << it.uuid
                    }
                    else if (it.lastUpdatedDisplay == it.dateCreatedDisplay) {
                        result[key].created << it.uuid
                    }
                    else {
                        //result[key].updated << it.uuid
                        result[key].countUpdated = result[key].countUpdated + 1
                    }

                    it.remove('componentType')
                    it.remove('dateCreatedDisplay')
                    it.remove('sortname')
                    it.remove('status')

                    List match = []

                    if (key == 'package') {
                        match = Package.executeQuery('select id, globalUID from Package where gokbId = :gokbId', [gokbId: it.uuid])
                    }
                    else if (key == 'platform') {
                        match = Platform.executeQuery('select id, globalUID from Platform where gokbId = :gokbId', [gokbId: it.uuid])
                    }
                    else if (key == 'provider') {
                        match = Provider.executeQuery('select id, globalUID from Provider where gokbId = :gokbId', [gokbId: it.uuid])
                    }
                    else if (key == 'vendor') {
                        match = Vendor.executeQuery('select id, globalUID from Vendor where gokbId = :gokbId', [gokbId: it.uuid])
                    }

                    if (match) {
                        it.id = match[0][0]
                        it.globalUID = match[0][1]
                    }

                    if (it.globalUID) { result[key].countInLaser++ }

                    result[key].all << it
                }

                result[key].count = result[key].deleted.size() + result[key].created.size() + result[key].countUpdated
            }
        }

        Map packageMap = gokbService.executeQuery(apiUrl, base + [componentType: 'Package'])
        process(packageMap as Map, 'package')

        Map platformMap = gokbService.executeQuery(apiUrl, base + [componentType: 'Platform'])
        process(platformMap as Map, 'platform')

        Map providerMap = gokbService.executeQuery(apiUrl, base + [componentType: 'Org'])
        process(providerMap as Map, 'provider')

        Map vendorMap = gokbService.executeQuery(apiUrl, base + [componentType: 'Vendor'])
        process(vendorMap as Map, 'vendor')

        result
    }
}
