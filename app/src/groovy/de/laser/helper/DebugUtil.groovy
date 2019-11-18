package de.laser.helper

import de.laser.CacheService
import de.laser.ContextService
import de.laser.domain.SystemProfiler
import grails.util.Holders
import groovy.transform.CompileStatic

@CompileStatic
class DebugUtil {

    CacheService   cacheService   = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')
    ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
    EhcacheWrapper benchCache

    final static String CK_PREFIX_GLOBAL_INTERCEPTOR = 'DebugUtil/Session/'
    final static String CK_PREFIX_RANDOM             = 'DebugUtil/Random/'

    // for global interceptors
    DebugUtil(String cacheKeyPrefix) {
        benchCache = cacheService.getTTL300Cache(cacheKeyPrefix)
    }

    // for inner method benches
    DebugUtil() {
        benchCache = cacheService.getTTL300Cache(CK_PREFIX_RANDOM + UUID.randomUUID().toString())
    }

    // handling interceptor benches

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
        setBenchMark('abs(STEP_N - STEP_0)')

        List marks = (List) benchCache.get('')
        benchCache.remove('')

        marks
    }
}
