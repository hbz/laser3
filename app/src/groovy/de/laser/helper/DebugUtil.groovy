package de.laser.helper

import de.laser.CacheService
import grails.util.Holders
import groovy.transform.CompileStatic

@CompileStatic
class DebugUtil {

    CacheService cacheService = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')
    EhcacheWrapper benchCache = cacheService.getTTL300Cache('DebugUtil/benchCache for ')

    def startBench(String key) {
        benchCache.put(key, new Date())
    }

    long stopBench(String key) {
        long diff = 0

        Date date = (Date) benchCache.get(key)
        if (date) {
            diff = (new Date().getTime()) - date.getTime()
        }
        benchCache.remove(key)

        return diff
    }
}
