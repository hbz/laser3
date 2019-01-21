package de.laser.helper

import de.laser.CacheService
import grails.util.Holders
import groovy.transform.CompileStatic
import net.sf.ehcache.Cache

@CompileStatic
class EhcacheWrapper {

    CacheService cacheService = (CacheService) Holders.grailsApplication.mainContext.getBean('cacheService')

    private Cache cache
    private String keyPrefix

    EhcacheWrapper(Cache cache, String keyPrefix) {
        this.cache = cache
        this.keyPrefix = keyPrefix ?: ''
    }

    def getCache() {
        cache
    }
    def put(String key, def value) {
        cacheService.put(cache, keyPrefix + key, value)
    }
    def get(String key) {
        cacheService.get(cache, keyPrefix + key)
    }
    def clear() {
        cacheService.clear(cache)
    }
}
