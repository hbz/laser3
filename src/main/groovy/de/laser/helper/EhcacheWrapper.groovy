package de.laser.helper

import de.laser.CacheService
import grails.util.Holders
import groovy.transform.CompileStatic
import net.sf.ehcache.Cache

/**
 * Helper class to wrap the cache access methods
 * @see CacheService
 */
@CompileStatic
class EhcacheWrapper {

    final static String SEPARATOR = '_'

    CacheService cacheService = BeanStore.getCacheService()

    private Cache cache // net.sf.ehcache.Cache
    private String keyPrefix

    /**
     * Constructs a new cache wrapper with the given cache to use and the key prefix under which all keys will be stored
     * @param cache the cache to use
     * @param keyPrefix the key prefix to use for stored keys
     * @see CacheService
     */
    EhcacheWrapper(Cache cache, String keyPrefix) {
        this.cache = cache
        this.keyPrefix = keyPrefix ?: ''
    }

    /**
     * Retrieves the cache instance
     * @return the cache instance
     */
    def getCache() {
        cache
    }

    /**
     * Lists all cache keys in the underlying cache
     * @return a list of keys matching with the cache key prefix and a separator character, i.e. which have been set up by the app
     */
    List getKeys() {
        cache.getKeys().findAll{ it.toString().startsWith(keyPrefix + SEPARATOR) }
    }

    /**
     * Stores the given value under the given key on the given cache
     * @param key the key under which the value should be stored
     * @param value the value to store
     */
    def put(String key, def value) {
        cacheService.put(cache, keyPrefix + SEPARATOR + key, value)
    }

    /**
     * Gets the given value under the given key from the given cache
     * @param key the key under which the value is stored
     * @return the stored value or null if no value exists for the given key
     */
    def get(String key) {
        cacheService.get(cache, keyPrefix + SEPARATOR + key)
    }

    /**
     * Removes the cache entry under the given key
     * @param cache the cache from which the key should be removed
     * @param key the key to remove
     */
    def remove(String key) {
        cacheService.remove(cache, keyPrefix + SEPARATOR + key)
    }

    /**
     * Clears the given cache from all key-value mappings
     * @param cache the cache which should be cleared
     */
    def clear() {
        cacheService.clear(cache)
    }
}
