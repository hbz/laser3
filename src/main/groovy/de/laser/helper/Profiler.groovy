package de.laser.helper

import de.laser.ContextService
import de.laser.cache.EhcacheWrapper
import de.laser.storage.BeanStore
import groovy.transform.CompileStatic
import org.grails.web.servlet.mvc.GrailsWebRequest

/**
 * Helper class to make performance measurements in the app
 */
@CompileStatic
class Profiler {

    ContextService contextService = BeanStore.getContextService()
    EhcacheWrapper benchCache

    public static final String SESSION_SYSTEMPROFILER = 'Profiler/Session/SystemProfiler'
    public static final String USER_BENCHMARK = 'Profiler/User/Benchmark'

    /**
     * Constructor
     * For global interceptors; object stored in session caches
     */
    Profiler(String cacheKeyPrefix) {
        benchCache = contextService.getUserCache(cacheKeyPrefix)
    }

    /**
     * Constructor
     * For inner method benches; object not stored
     */
    Profiler() {
        String cid = USER_BENCHMARK + EhcacheWrapper.SEPARATOR + UUID.randomUUID().toString()
        benchCache = contextService.getUserCache(cid)
    }

    // handling interceptor benches

    /**
     * Starts a profile measurement, initialising the cache
     * @param key the key to use for identification
     */
    def startSimpleBench(String key) {
        benchCache.put(key, new Date())
    }

    /**
     * Stops a simple measurement and returns the running time between start and stop
     * @param key the key identifying the benchmark
     * @return the running time of the request
     */
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

    /**
     * Sets a profile measurement step
     * @param step the step name to mark
     * @return the current list of benchmarks
     */
    List setBenchmark(String step) {
        List marks = (List) benchCache.get('') ?: []
        marks.add([step, System.currentTimeMillis()])

        benchCache.put('', marks)
        marks
    }

    /**
     * Stops a complex benchmarking, sets the stop marker and returns the complete list
     * @return the list of steps
     */
    List stopBenchmark() {
        setBenchmark('sum (step_1 .. step_n-1)')

        List marks = (List) benchCache.get('')
        benchCache.remove('')

        marks
    }

    /**
     * Helper method to fetch the current action name
     */
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
