package de.laser


import de.laser.auth.User
import de.laser.helper.EhcacheWrapper
import grails.gorm.transactions.Transactional
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.config.Configuration
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * This service provides the different caches used in the system
 */
@Transactional
class CacheService implements ApplicationContextAware {

    ApplicationContext applicationContext

    // global caches

    private Cache cache_ttl_300
    private Cache cache_ttl_1800

    private Cache shared_user_cache
    private Cache shared_org_cache

    /**
     * Initialises the cache manager
     * @return a new cache manager instance
     */
    CacheManager getCacheManager() {

        return CacheManager.newInstance()
    }

    /**
     * Retrieves the given cache; if it does not exist, it will be created
     * @param cacheManager the cache manager instance
     * @param cacheName the cache type to retrieve
     * @return the cache instance
     */
    Cache getCache(CacheManager cacheManager, String cacheName) {

        def cache = null

        if (cacheManager) {
            if (! cacheManager.getCache(cacheName)) {
                cacheManager.addCache(cacheName)
            }
            cache = cacheManager.getCache(cacheName)
        }

        return cache
    }

    /* --- */

    /**
     * Sets for the given prefix a global cache which lasts five minutes (300 seconds)
     * @param cacheKeyPrefix the cache key to set
     * @return a five minutes cache for the given prefix
     */
    EhcacheWrapper getTTL300Cache(String cacheKeyPrefix) {

        if (! cache_ttl_300) {

            String cacheName = 'TTL_300_CACHE'
            CacheManager cacheManager = getCacheManager()
            cache_ttl_300 = getCache(cacheManager, cacheName)

            cache_ttl_300.getCacheConfiguration().setTimeToLiveSeconds(300)
            cache_ttl_300.getCacheConfiguration().setTimeToIdleSeconds(300)
        }

        return new EhcacheWrapper(cache_ttl_300, cacheKeyPrefix)
    }

    /**
     * Sets for the given prefix a global cache which lasts 30 minutes (1800 seconds)
     * @param cacheKeyPrefix the cache key to set
     * @return a 30 minutes cache for the given prefix
     */
    EhcacheWrapper getTTL1800Cache(String cacheKeyPrefix) {

        if (! cache_ttl_1800) {

            String cacheName = 'TTL_1800_CACHE'
            CacheManager cacheManager = getCacheManager()
            cache_ttl_1800 = getCache(cacheManager, cacheName)

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

    /**
     * Gets a personalised cache for the given user which lasts for the whole session. A new cache record will be
     * created if it does not exist
     * @param user the user whose cache should be retrieved
     * @param cacheKeyPrefix the cache key to set
     * @return the user cache for the given prefix
     */
    EhcacheWrapper getSharedUserCache(User user, String cacheKeyPrefix) {

        if (! shared_user_cache) {

            String cacheName = 'SHARED_USER_CACHE'
            CacheManager cacheManager = getCacheManager()
            shared_user_cache = getCache(cacheManager, cacheName)

            shared_user_cache.getCacheConfiguration().setCopyOnRead(true)
        }

        return new EhcacheWrapper(shared_user_cache, "USER:${user.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    /**
     * Gets a cache dedicated to the given institution which lasts for the whole session. A new cache record will be
     * created if it does not exist
     * @param org the institution whose cache should be retrieved
     * @param cacheKeyPrefix the cache key to set
     * @return the institution cache for the given prefix
     */
    EhcacheWrapper getSharedOrgCache(Org org, String cacheKeyPrefix) {

        if (! shared_org_cache) {

            String cacheName = 'SHARED_ORG_CACHE'
            CacheManager cacheManager = getCacheManager()
            shared_org_cache = getCache(cacheManager, cacheName)

            shared_org_cache.getCacheConfiguration().setCopyOnRead(true)
        }

        return new EhcacheWrapper(shared_org_cache, "ORG:${org.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    /* --- */

    List getKeys() {
        cache.getKeys()
    }

    /**
     * Stores the given value under the given key on the given cache
     * @param cache the cache map to store the value
     * @param key the key under which the value should be stored
     * @param value the value to store
     */
    def put(def cache, String key, def value) {
        cache.put(new Element(key, value))
    }

    /**
     * Gets the given value under the given key from the given cache
     * @param cache the cache map where the value is stored
     * @param key the key under which the value is stored
     * @return the stored value or null if no value exists for the given key
     */
    def get(def cache, String key) {
        cache.get(key)?.objectValue
    }

    /**
     * Removes the cache entry under the given key
     * @param cache the cache from which the key should be removed
     * @param key the key to remove
     */
    def remove(def cache, String key) {
        cache.remove(key)
    }

    /**
     * Clears the given cache from all key-value mappings
     * @param cache the cache which should be cleared
     */
    def clear(def cache) {
        cache.removeAll()
    }

    /* --- */

    /**
     * Gets the dist storage path for the app caches
     * @param cm the cache manager whose disk storage path should be retrieved
     * @return the storage path for the given cache manager
     */
    String getDiskStorePath(CacheManager cm) {
        Configuration cfg = cm.getConfiguration()
        cfg.getDiskStoreConfiguration()?.getPath()
    }

    /**
     * Gets a generic store path for cache manager instances
     * @return a generic patch (substitutes call of {@link #getDiskStorePath(net.sf.ehcache.CacheManager)} with a new instance as argument)
     */
    String getDiskStorePath() {
        getDiskStorePath( CacheManager.newInstance() )
    }
}
