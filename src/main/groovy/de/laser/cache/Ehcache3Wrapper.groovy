package de.laser.cache

import de.laser.Cache3Service
import de.laser.storage.BeanStore
import groovy.transform.CompileStatic
import org.ehcache.Cache

/**
 * Helper class to wrap the cache access methods
 * @see Cache3Service
 */
@CompileStatic
class Ehcache3Wrapper {

    public static final String SEPARATOR = '_'

    Cache3Service cache3Service = BeanStore.getCache3Service()

    private Cache cache // org.ehcache.Cache
    private String keyPrefix

    /**
     * Constructs a new cache wrapper with the given cache to use and the key prefix under which all keys will be stored
     * @param cache the cache to use
     * @param keyPrefix the key prefix to use for stored keys
     * @see Cache3Service
     */
    Ehcache3Wrapper(Cache cache, String keyPrefix) {
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
        List keys = []
        for (Cache.Entry entry : cache) {
            keys.add(entry.key)
        }
        keys.findAll{ it.toString().startsWith(keyPrefix + SEPARATOR) }
    }

    /**
     * Stores the given value under the given key on the given cache
     * @param key the key under which the value should be stored
     * @param value the value to store
     */
    def put(String key, def value) {
        cache3Service.put(cache, keyPrefix + SEPARATOR + key, value)
    }

    /**
     * Gets the given value under the given key from the given cache
     * @param key the key under which the value is stored
     * @return the stored value or null if no value exists for the given key
     */
    def get(String key) {
        cache3Service.get(cache, keyPrefix + SEPARATOR + key)
    }

    /**
     * Removes the cache entry under the given key
     * @param cache the cache from which the key should be removed
     * @param key the key to remove
     */
    def remove(String key) {
        cache3Service.remove(cache, keyPrefix + SEPARATOR + key)
    }

    /**
     * Clears the given cache from all key-value mappings
     * @param cache the cache which should be cleared
     */
    def clear() {
        cache3Service.clear(cache)
    }
}
