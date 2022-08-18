package de.laser.helper

import de.laser.ContextService
import de.laser.cache.EhcacheWrapper
import de.laser.storage.BeanStore
import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest

@CompileStatic
class Profiler {

    ContextService contextService = BeanStore.getContextService()
    EhcacheWrapper benchCache

    public static final String SESSION_SYSTEMPROFILER = 'Profiler/Session/SystemProfiler'
    public static final String USER_BENCHMARK = 'Profiler/User/Benchmark'

    // for global interceptors; object stored in session caches
    Profiler(String cacheKeyPrefix) {
        benchCache = contextService.getUserCache(cacheKeyPrefix)
    }

    // for inner method benches; object not stored
    Profiler() {
        String cid = USER_BENCHMARK + EhcacheWrapper.SEPARATOR + UUID.randomUUID().toString()
        benchCache = contextService.getUserCache(cid)
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
