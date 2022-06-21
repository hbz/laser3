package de.laser.reporting.report

import de.laser.Subscription
import de.laser.utils.DateUtils
import de.laser.cache.SessionCacheWrapper
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat

@Slf4j
class ReportingCache {

    static final CTX_GLOBAL         = 'MyInstitutionController/reporting/'
    static final CTX_SUBSCRIPTION   = 'SubscriptionController/reporting/'

    SessionCacheWrapper scw
    String ctx
    String token

    ReportingCache(String ctx) {
        this.ctx = ctx
        scw = new SessionCacheWrapper()
    }
    ReportingCache(String ctx, String token) {
        this.ctx = ctx
        this.token = token
        scw = new SessionCacheWrapper()
    }

    // ---

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

    Map<String, Object> get() {
        String ctxToken = token ? ctx + token : ctx
        scw.get(ctxToken) as Map<String, Object>
    }

    def put(Map<String, Object> value) {
        String ctxToken = token ? ctx + token : ctx
        scw.put(ctxToken, value)
    }

    def remove() {
        String ctxToken = token ? ctx + token : ctx
        scw.remove(ctxToken)
    }

    boolean exists () {
        Map<String, Object> cache = get()
        cache != null && ! cache.isEmpty()
    }

    // ---

    Map<String, Object> readMeta() {
        Map<String, Object> cache = get()
        cache.meta as Map<String, Object>
    }
    
    Map<String, Object> readFilterCache() {
        Map<String, Object> cache = get()
        cache.filterCache as Map<String, Object>
    }
    Map<String, Object> readQueryCache() {
        Map<String, Object> cache = get()
        cache.queryCache as Map<String, Object>
    }
    Map<String, Object> readDetailsCache() {
        Map<String, Object> cache = get()
        cache.detailsCache as Map<String, Object>
    }

    def writeFilterCache(Map<String, Object> filterCache) {
        Map<String, Object> cache = get()
        cache.filterCache = filterCache
        put( cache )
    }
    def writeQueryCache(Map<String, Object> queryCache) {
        Map<String, Object> cache = get()
        cache.queryCache = queryCache
        put( cache )
    }
    def writeDetailsCache(Map<String, Object> detailsCache) {
        Map<String, Object> cache = get()
        cache.detailsCache = detailsCache
        put( cache )
    }

    def intoFilterCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.filterCache.put(key, value)
        put( cache )
    }
    def intoQueryCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.queryCache.put(key, value)
        put( cache )
    }
    def intoDetailsCache(String key, Object value) {
        Map<String, Object> cache = get()
        cache.detailsCache.put(key, value)
        put( cache )
    }
}
