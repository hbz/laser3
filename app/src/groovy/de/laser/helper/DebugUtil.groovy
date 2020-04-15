package de.laser.helper

import de.laser.ContextService
import grails.util.Holders
import org.codehaus.groovy.grails.web.util.WebUtils

//@CompileStatic
class DebugUtil {

    //CacheService   cacheService   = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')
    ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
    EhcacheWrapper benchCache

    final static String SYSPROFILER_SESSION = 'DebugUtil/Session/SystemProfiler'
    final static String BENCHMARK_LOCAL     = 'DebugUtil/Local/Benchmark'

    // for global interceptors; object stored in session caches
    DebugUtil(String cacheKeyPrefix) {
        String uri = WebUtils.retrieveGrailsWebRequest().getCurrentRequest().getForwardURI()

        print"DebugUtil [cacheKeyPrefix: ${cacheKeyPrefix}, user: ${contextService.getUser()?.id}, uri: ${uri}]"

        benchCache = contextService.getCache(cacheKeyPrefix, ContextService.USER_SCOPE)
    }

    // for inner method benches; object not stored
    DebugUtil() {
        String cid = BENCHMARK_LOCAL + EhcacheWrapper.SEPARATOR + UUID.randomUUID().toString()
        String uri = WebUtils.retrieveGrailsWebRequest().getCurrentRequest().getForwardURI()

        print"DebugUtil [cid: ${cid}, user: ${contextService.getUser()?.id}, uri: ${uri}]"

        benchCache = contextService.getCache(cid, ContextService.USER_SCOPE)
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
        setBenchmark('sum (step_1 .. step_n-1)')

        List marks = (List) benchCache.get('')
        benchCache.remove('')

        marks
    }
}
