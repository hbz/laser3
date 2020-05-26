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
            cache_ttl_300 = (Cache) getCache(cacheManager, cacheName)

            cache_ttl_300.getCacheConfiguration().setTimeToLiveSeconds(300)
            cache_ttl_300.getCacheConfiguration().setTimeToIdleSeconds(300)
        }

        return new EhcacheWrapper(cache_ttl_300, cacheKeyPrefix)
    }

    EhcacheWrapper getTTL1800Cache(String cacheKeyPrefix) {

        if (! cache_ttl_1800) {

            String cacheName = 'CACHE_TTL_1800'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            cache_ttl_1800 = (Cache) getCache(cacheManager, cacheName)

            cache_ttl_1800.getCacheConfiguration().setTimeToLiveSeconds(1800)
            cache_ttl_1800.getCacheConfiguration().setTimeToIdleSeconds(1800)
        }

        return new EhcacheWrapper(cache_ttl_1800, cacheKeyPrefix)
    }

    /* --- */

    /*
    Copy Cache
    A copy cache can have two behaviors: it can copy Element instances it returns, when copyOnRead is true and copy elements it stores, when copyOnWrite to true.
    -> A copy-on-read cache can be useful when you can't let multiple threads access the same Element instance (and the value it holds) concurrently.
    For example, where the programming model doesn't allow it, or you want to isolate changes done concurrently from each other.
    -> Copy on write also lets you determine exactly what goes in the cache and when (i.e., when the value that will be in the cache will be in state it was when it actually was put in cache).
    All mutations to the value, or the element, after the put operation will not be reflected in the cache.
    By default, the copy operation will be performed using standard Java object serialization. For some applications, however, this might not be good (or fast) enough.
    You can configure your own CopyStrategy, which will be used to perform these copy operations. For example, you could easily implement use cloning rather than Serialization.
    For more information about copy caches, see “Passing Copies Instead of References” in the Configuration Guide for Ehcache.
    */

    EhcacheWrapper getSharedUserCache(User user, String cacheKeyPrefix) {

        if (! shared_user_cache) {

            String cacheName = 'USER_CACHE_SHARED'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            shared_user_cache  = (Cache) getCache(cacheManager, cacheName)

            shared_user_cache.getCacheConfiguration().setCopyOnRead(true)
        }

        return new EhcacheWrapper(shared_user_cache, "USER:${user.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    EhcacheWrapper getSharedOrgCache(Org org, String cacheKeyPrefix) {

        if (! shared_org_cache) {

            String cacheName = 'ORG_CACHE_SHARED'
            CacheManager cacheManager = (CacheManager) getCacheManager(EHCACHE)
            shared_org_cache = (Cache) getCache(cacheManager, cacheName)

            shared_org_cache.getCacheConfiguration().setCopyOnRead(true)
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
