package de.laser.helper

import de.laser.ContextService
import grails.util.Holders

//@CompileStatic
class DebugUtil {

    //CacheService   cacheService   = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')
    ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
    EhcacheWrapper benchCache

    final static String DU_SYSPROFILER_PREFIX   = 'DebugUtil/SystemProfiler:'
    final static String DU_BENCHMARK_PREFIX     = 'DebugUtil/Benchmark:'

    // for global interceptors
    DebugUtil(String cacheKeyPrefix, String scope) {
        benchCache = contextService.getCache(cacheKeyPrefix, scope)
    }

    // for inner method benches
    DebugUtil() {
        benchCache = contextService.getCache(DU_BENCHMARK_PREFIX + UUID.randomUUID().toString(), ContextService.USER_SCOPE)
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

    List setBenchmark(String step) {
        List marks = (List) benchCache.get('') ?: []
        marks.add([step, System.currentTimeMillis()])

        benchCache.put('', marks)
        marks
    }

    List stopBenchmark() {
        setBenchmark('abs(STEP_N - STEP_0)')

        List marks = (List) benchCache.get('')
        benchCache.remove('')

        marks
    }
}
