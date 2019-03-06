package de.laser.helper

import de.laser.CacheService
import grails.util.Holders
import groovy.transform.CompileStatic

import java.sql.Timestamp

@CompileStatic
class DebugUtil {

    CacheService   cacheService = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')
    EhcacheWrapper benchCache

    final static String CK_PREFIX_GLOBAL_INTERCEPTOR = 'DebugUtil/interceptor for '
    final static String CK_PREFIX_RANDOM             = 'DebugUtil/benchMark for random uuid '

    // for inner method benches
    DebugUtil() {
        benchCache = cacheService.getTTL300Cache(CK_PREFIX_RANDOM + UUID.randomUUID().toString())
    }

    // global interceptors
    DebugUtil(String cacheKeyPrefix) {
        benchCache = cacheService.getTTL300Cache(cacheKeyPrefix)
    }

    // simple interceptor bench

    def startSimpleBench(String key) {
        benchCache.put(key, new Date())
    }

    long stopSimpleBench(String key) {
        long diff = 0

        Date date = (Date) benchCache.get(key)
        if (date) {
            diff = (new Date().getTime()) - date.getTime()
        }
        benchCache.remove(key)

        return diff
    }

    // complex list with timestamps for inner method benches

    List setBenchMark(String step) {
        List marks = (List) benchCache.get('') ?: []
        marks.add([step, System.currentTimeMillis()])

        benchCache.put('', marks)
        marks
    }

    List stopBenchMark() {
        setBenchMark('finished')

        List marks = (List) benchCache.get('')
        benchCache.remove('')

        marks
    }
}
