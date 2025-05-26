package de.laser

import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import grails.gorm.transactions.Transactional
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.xml.XmlConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * This service provides the different caches used in the system
 */
@Transactional
class CacheService implements ApplicationContextAware {

    ApplicationContext applicationContext

    public static final String TTL_300_CACHE     = 'TTL_300_CACHE'
    public static final String TTL_1800_CACHE    = 'TTL_1800_CACHE'
    public static final String TTL_3600_CACHE    = 'TTL_3600_CACHE'
    public static final String SHARED_USER_CACHE = 'SHARED_USER_CACHE'
    public static final String SHARED_ORG_CACHE  = 'SHARED_ORG_CACHE'

    private CacheManager cacheManager

    // global caches

    private Cache cache_ttl_300
    private Cache cache_ttl_1800
    private Cache cache_ttl_3600
    private Cache shared_user_cache
    private Cache shared_org_cache


    void initCaches() {
        if (!cacheManager) {
            XmlConfiguration xmlConfig = new XmlConfiguration(this.class.classLoader.getResource('ehcache3.xml'))

            cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig)
            cacheManager.init()

            cache_ttl_300  = getCache(TTL_300_CACHE)
            cache_ttl_1800 = getCache(TTL_1800_CACHE)
            cache_ttl_3600 = getCache(TTL_3600_CACHE)

            shared_user_cache = getCache(SHARED_USER_CACHE)
            shared_org_cache  = getCache(SHARED_ORG_CACHE)
        }
    }

    /**
     * Initialises the cache manager
     * @return a new cache manager instance
     */
    CacheManager getEhcache3Manager() {
        cacheManager
    }

//    List getCaches() {
//        cacheManager.getRuntimeConfiguration().caches
//    }

    List<String> getCacheNames() {
        cacheManager.getRuntimeConfiguration().getCacheConfigurations().keySet().toList()
    }

    /* --- */

    /**
     * Retrieves the given cache; if it does not exist, it will be created
     * @param cacheManager the cache manager instance
     * @param cacheName the cache type to retrieve
     * @return the cache instance
     */
    Cache getCache(String cacheName) {
        cacheManager.getCache(cacheName, Object.class, Object.class) // TODO
    }

    def getCacheStatistics(String cacheName) {
//        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer()
//        ObjectName name = new ObjectName("*:type=CacheStatistics,*,Cache=" + cacheName)
//
//        Set<ObjectName> beans = mbeanServer.queryNames(name, null)
//        ObjectName[] objArray = beans.toArray(new ObjectName[beans.size()])
//        CacheStatisticsMXBean stats = JMX.newMBeanProxy(mbeanServer, objArray[0], CacheStatisticsMXBean.class)
//        stats
    }

    /* --- */

    /**
     * Sets for the given prefix a global cache which lasts five minutes (300 seconds)
     * @param cacheKeyPrefix the cache key to set
     * @return a five minutes cache for the given prefix
     */
    EhcacheWrapper getTTL300Cache(String cacheKeyPrefix) {
        return new EhcacheWrapper(cache_ttl_300, cacheKeyPrefix)
    }

    /**
     * Sets for the given prefix a global cache which lasts 30 minutes (1800 seconds)
     * @param cacheKeyPrefix the cache key to set
     * @return a 30 minutes cache for the given prefix
     */
    EhcacheWrapper getTTL1800Cache(String cacheKeyPrefix) {
        return new EhcacheWrapper(cache_ttl_1800, cacheKeyPrefix)
    }

    /**
     * Sets for the given prefix a global cache which lasts 60 minutes (3600 seconds)
     * @param cacheKeyPrefix the cache key to set
     * @return a 30 minutes cache for the given prefix
     */
    EhcacheWrapper getTTL3600Cache(String cacheKeyPrefix) {
        return new EhcacheWrapper(cache_ttl_3600, cacheKeyPrefix)
    }

    /**
     * Gets a personalised cache for the given user which lasts for the whole session. A new cache record will be
     * created if it does not exist
     * @param user the user whose cache should be retrieved
     * @param cacheKeyPrefix the cache key to set
     * @return the user cache for the given prefix
     */
    EhcacheWrapper getSharedUserCache(User user, String cacheKeyPrefix) {
//            shared_user_cache.getCacheConfiguration().setCopyOnRead(true)
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
//            shared_org_cache.getCacheConfiguration().setCopyOnRead(true)
        return new EhcacheWrapper(shared_org_cache, "ORG:${org.id}" + EhcacheWrapper.SEPARATOR + cacheKeyPrefix)
    }

    /* --- */

    /**
     * Stores the given value under the given key on the given cache
     * @param cache the cache map to store the value
     * @param key the key under which the value should be stored
     * @param value the value to store
     */
    def put(Cache cache, String key, def value) {
        cache.put(key, value)
    }

    /**
     * Gets the given value under the given key from the given cache
     * @param cache the cache map where the value is stored
     * @param key the key under which the value is stored
     * @return the stored value or null if no value exists for the given key
     */
    def get(Cache cache, String key) {
        cache.get(key)
    }

    /**
     * Removes the cache entry under the given key
     * @param cache the cache from which the key should be removed
     * @param key the key to remove
     */
    def remove(Cache cache, String key) {
        cache.remove(key)
    }

    /**
     * Clears the given cache from all key-value mappings
     * @param cache the cache which should be cleared
     */
    def clear(Cache cache) {
        for (Cache.Entry entry : cache) {
            cache.remove(entry.key)
        }
    }

    /* --- */

    /**
     * Gets the dist storage path for the app caches
     * @param cm the cache manager whose disk storage path should be retrieved
     * @return the storage path for the given cache manager
     */
    String getDiskStorePath(CacheManager cm) {
//        Configuration cfg = cm.getConfiguration()
//        cfg.getDiskStoreConfiguration()?.getPath()
        'TODO'
    }

    /**
     * Gets a generic store path for cache manager instances
     * @return a generic patch (substitutes call of {@link #getDiskStorePath(net.sf.ehcache.CacheManager)} with a new instance as argument)
     */
    String getDiskStorePath() {
        getDiskStorePath( getEhcache3Manager() )
    }
}
