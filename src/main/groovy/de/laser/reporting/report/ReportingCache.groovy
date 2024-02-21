package de.laser.reporting.report

import de.laser.Subscription
import de.laser.cache.EhcacheWrapper
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

/**
 * This class is responsible for temporary storage of query results
 */
@Slf4j
class ReportingCache {

    static final CTX_GLOBAL         = 'MyInstitutionController/reporting'
    static final CTX_SUBSCRIPTION   = 'SubscriptionController/reporting'

    EhcacheWrapper ttl3600
    String token

    /**
     * Constructor call
     * Initialises the cache for the given context and storage token. The cache persists for an hour (= 3600 seconds)
     * @param ctx the user/institution context
     * @param token the token under which the report result is going to be stored
     */
    ReportingCache(String ctx, String token) {
        this.token = token
        ttl3600  = BeanStore.getCacheService().getTTL3600Cache(ctx + '/' + BeanStore.getContextService().getUser().id) // user bound
    }

    // ---

    /**
     * Sets up the reporting cache for the given subscription and sets some basic configuration for the report
     * @param id the {@link Subscription} ID to use
     * @param token the token under which the report is going to be stored
     * @return the cache holder
     */
    static ReportingCache initSubscriptionCache(long id, String token) {
        log.debug 'initSubscriptionCache( ' + token + ' )'

        Subscription sub = Subscription.get(id)
        String filterResult = sub.name

        if (sub.startDate || sub.endDate) {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            filterResult += ' (' + (sub.startDate ? sdf.format(sub.startDate) : '') + ' - ' + (sub.endDate ? sdf.format(sub.endDate) : '')  + ')'
        }

        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, token )
        rCache.put( [
                filterCache: [:],
                queryCache: [:]
        ] )
        rCache.intoFilterCache('result', filterResult)

        rCache
    }

    // ---

    /**
     * Returns the whole report
     * @return the report data stored under the given token
     */
    Map<String, Object> get() {
        ttl3600.get(token) as Map<String, Object>
    }

    /**
     * Stures the report data
     * @param value the report data as a {@link Map}
     */
    def put(Map<String, Object> value) {
        ttl3600.put(token , value)
    }

    /**
     * Removes the report from the cache
     */
    def remove() {
        ttl3600.remove(token)
    }

    /**
     * Checks if there is already a cache store for this report
     * @return true if a cache exists and if there are values stored in it, false otherwise
     */
    boolean exists () {
        Map<String, Object> cache = get()
        cache != null && ! cache.isEmpty()
    }

    // ---

    /**
     * Reads and returns the basic configuration parameters of the current report
     * @return the configuration map
     */
    Map<String, Object> readMeta() {
        Map<String, Object> cache = get()
        cache.meta as Map<String, Object>
    }

    /**
     * Returns the filter cache valid for this report
     * @return the filter cache map
     */
    Map<String, Object> readFilterCache() {
        Map<String, Object> cache = get()
        cache.filterCache as Map<String, Object>
    }

    /**
     * Returns the query cache valid for this report
     * @return the query cache map
     */
    Map<String, Object> readQueryCache() {
        Map<String, Object> cache = get()
        cache.queryCache as Map<String, Object>
    }

    /**
     * Returns the details cache valid for this report
     * @return the details cache map
     */
    Map<String, Object> readDetailsCache() {
        Map<String, Object> cache = get()
        cache.detailsCache as Map<String, Object>
    }

    /**
     * Updates the currently valid filter cache
     * @param filterCache the updated filter map to store
     */
    def writeFilterCache(Map<String, Object> filterCache) {
        Map<String, Object> cache = get()
        cache.filterCache = filterCache
        put( cache )
    }

    /**
     * Updates the currently valid query cache
     * @param queryCache the updated query map to store
     */
    def writeQueryCache(Map<String, Object> queryCache) {
        Map<String, Object> cache = get()
        cache.queryCache = queryCache
        put( cache )
    }

    /**
     * Updates the currently valid details cache
     * @param detailsCache the updated details map to store
     */
    def writeDetailsCache(Map<String, Object> detailsCache) {
        Map<String, Object> cache = get()
        cache.detailsCache = detailsCache
        put( cache )
    }

    /**
     * Updates a certain setting in the filter cache
     * @param key the key to be updated in the filter
     * @param value the updated value to store
     */
    def intoFilterCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.filterCache.put(key, value)
        put( cache )
    }

    /**
     * Updates a certain setting in the query cache
     * @param key the key to be updated in the query cache
     * @param value the updated value to store
     */
    def intoQueryCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.queryCache.put(key, value)
        put( cache )
    }

    /**
     * Updates a certain setting in the details cache
     * @param key the key to be updated in the details cache
     * @param value the updated value to store
     */
    def intoDetailsCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.detailsCache.put(key, value)
        put( cache )
    }
}
