package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.helper.EhcacheWrapper
import grails.plugin.cache.GrailsConcurrentMapCache
import grails.plugin.cache.GrailsConcurrentMapCacheManager
import grails.plugin.springsecurity.SpringSecurityService
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Status
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class CacheService implements ApplicationContextAware {

    ApplicationContext applicationContext
    SpringSecurityService springSecurityService

    final static EHCACHE = 'EHCACHE'
    final static PLUGINCACHE = 'PLUGINCACHE'

    // global caches

    private Cache cache_ttl_300
    private Cache cache_ttl_1800

    private Cache shared_user_cache
    private Cache shared_org_cache

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

    def getCache(def cacheManager, String cacheName) {

        def cache = null

        if (cacheManager) {
            if (cacheManager instanceof CacheManager) {
                if (! cacheManager.getCache(cacheName)) {
                    cacheManager.addCache(cacheName)
                }
                cache = cacheManager.getCache(cacheName)
            }
            else if (cacheManager instanceof GrailsConcurrentMapCacheManager) {
                cache = cacheManager.getCache(cacheName)
            }
        }

        return cache
    }

    /* --- */

    EhcacheWrapper getTTL300Cache(String cacheKeyPrefix) {

        if (! cache_ttl_300) {

            String cacheName = 'CACHE_TTL_300'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            Cache cache = (Cache) getCache(cacheManager, cacheName)

            cache?.getCacheConfiguration()?.setTimeToLiveSeconds(300)
            cache?.getCacheConfiguration()?.setTimeToIdleSeconds(300)
            cache_ttl_300 = cache
        }

        return new EhcacheWrapper(cache_ttl_300, cacheKeyPrefix)
    }

    EhcacheWrapper getTTL1800Cache(String cacheKeyPrefix) {

        if (! cache_ttl_1800) {

            String cacheName = 'CACHE_TTL_1800'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            Cache cache = (Cache) getCache(cacheManager, cacheName)

            cache?.getCacheConfiguration()?.setTimeToLiveSeconds(1800)
            cache?.getCacheConfiguration()?.setTimeToIdleSeconds(1800)
            cache_ttl_1800 = cache
        }

        return new EhcacheWrapper(cache_ttl_1800, cacheKeyPrefix)
    }

    /* --- */

    EhcacheWrapper getSharedUserCache(User user, String cacheKeyPrefix) {

        if (! shared_user_cache) {

            String cacheName = 'USER_CACHE_SHARED'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            shared_user_cache  = (Cache) getCache(cacheManager, cacheName)
        }

        return new EhcacheWrapper(shared_user_cache, "USER:${user.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    EhcacheWrapper getSharedOrgCache(Org org, String cacheKeyPrefix) {

        if (! shared_org_cache) {

            String cacheName = 'ORG_CACHE_SHARED'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            shared_org_cache = (Cache) getCache(cacheManager, cacheName)
        }

        return new EhcacheWrapper(shared_org_cache, "ORG:${org.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    /* --- */

    def put(def cache, String key, def value) {

        if (cache instanceof Cache) {
            //if (cache.getStatus() == Status.STATUS_ALIVE) { // TODO [ticket=2023] HOTFIX remove
                cache.put(new Element(key, value))
            //}
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.put( key, value )
        }
    }

    def get(def cache, String key) {

        if (cache instanceof Cache) {
            //if (cache.getStatus() == Status.STATUS_ALIVE) { // TODO [ticket=2023] HOTFIX remove
                cache.get(key)?.objectValue
            //}
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.get(key)
        }
    }

    def remove(def cache, String key) {

        if (cache instanceof Cache) {
            //if (cache.getStatus() == Status.STATUS_ALIVE) { // TODO [ticket=2023] HOTFIX remove
                cache.remove(key)
            //}
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            println " TODO -> IMPLEMENT GrailsConcurrentMapCache.remove()"
        }
    }

    def clear(def cache) {

        if (cache instanceof Cache) {
            //if (cache.getStatus() == Status.STATUS_ALIVE) { // TODO [ticket=2023] HOTFIX remove
                cache.removeAll()
            //}
        }
        else if (cache instanceof GrailsConcurrentMapCache) {
            cache.clear()
        }
    }
}
