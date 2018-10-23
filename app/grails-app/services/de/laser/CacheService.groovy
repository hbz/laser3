package de.laser

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import grails.plugin.cache.GrailsConcurrentMapCache
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element

class CacheService implements ApplicationContextAware {

    ApplicationContext applicationContext
    def springSecurityService

    final static EHCACHE = 'EHCACHE'
    final static PLUGINCACHE = 'PLUGINCACHE'

    def getCacheManager(def type) {

        if (type == EHCACHE) {
            // http://www.ehcache.org/generated/2.10.4/pdf/Integrations.pdf
            return CacheManager.getCacheManager('__DEFAULT__')
        }
        else if (type == PLUGINCACHE) {
            // http://grails-plugins.github.io/grails-cache/
            return applicationContext.grailsCacheManager
        }
    }

    def getCache(def cacheManager, def cacheName) {

        def cache

        if (cacheManager) {
            if (! cacheManager.getCache(cacheName)) {
                cacheManager.addCache(cacheName)
            }
            cache = cacheManager.getCache(cacheName)
        }

        return cache
    }

    def put(def cache, String key, def value) {

        if (cache instanceof Cache) {
            cache.put( new Element(key, value) )
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.put( key, value )
        }
    }

    def get(def cache, String key) {

        if (cache instanceof Cache) {
            cache.get(key)?.objectValue
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.get(key)
        }
    }

    def clear(def cache) {

        if (cache instanceof Cache) {
            cache.removeAll()
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.clear()
        }
    }
}
