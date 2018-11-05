package de.laser.helper

import grails.util.Holders
import net.sf.ehcache.Cache

class EhcacheWrapper {

    def cacheService = Holders.grailsApplication.mainContext.getBean('cacheService')

    private Cache cache
    private def keyPrefix

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
