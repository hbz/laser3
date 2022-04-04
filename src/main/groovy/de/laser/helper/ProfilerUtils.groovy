package de.laser.helper

import de.laser.ContextService
import de.laser.storage.BeanStorage
import org.grails.web.servlet.mvc.GrailsWebRequest

//@CompileStatic
class ProfilerUtils {

    //CacheService   cacheService   = BeanStorage.getCacheService()
    ContextService contextService = BeanStorage.getContextService()
    EhcacheWrapper benchCache

    final static String SYSPROFILER_SESSION = 'DebugUtil/Session/SystemProfiler'
    final static String BENCHMARK_LOCAL     = 'DebugUtil/Local/Benchmark'

    // for global interceptors; object stored in session caches
    ProfilerUtils(String cacheKeyPrefix) {
        benchCache = contextService.getCache(cacheKeyPrefix, ContextService.USER_SCOPE)
    }

    // for inner method benches; object not stored
    ProfilerUtils() {
        String cid = BENCHMARK_LOCAL + EhcacheWrapper.SEPARATOR + UUID.randomUUID().toString()
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

    // helper

    static String generateKey(GrailsWebRequest webRequest) {
        String uri = 'unkown'
        try {
            String cc = webRequest.getControllerClass().getLogicalPropertyName()
            String an = webRequest.getActionName()
            uri = '/' + cc + (an ? '/' + an : '')
        }
        catch(Exception e) {
        }
        uri
    }
}
