package de.laser.cache

import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession

/**
 * This wrapper contains methods for the session cache handling.
 * The session cache holds until the session of a given user, i.e. from login to logout.
 * Session cache entries are all prefixed with "laserSessionCache_"
 * @see #keyPrefix
 */
class SessionCacheWrapper {

    private HttpSession session
    private String keyPrefix = 'laserSessionCache_'

    /**
     * Sets the session to which this cache is valid for if not set already
     */
    SessionCacheWrapper() {
        if (! session) {
            session = RequestContextHolder.currentRequestAttributes().getSession()
        }
    }

    /**
     * Gets the current session
     * @return the current {@link HttpSession}
     */
    HttpSession getSession() {
        session
    }

    /**
     * Lists the cache entries of the current session
     * @return a {@link Map} containing the entries of the current session cache
     */
    Map<String, Object> list() {
        Map<String, Object> result = [:]

        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                result.putAt( it.replaceFirst(keyPrefix, ''), get(it.replaceFirst(keyPrefix, '')) )
            }
        }
        result
    }

    /**
     * Lists the keys of cache entries of the current session cache
     * @return a {@link List} of session cache keys
     */
    List getKeys() {
        session.getAttributeNames().findAll{ it.startsWith(keyPrefix) }
    }

    /**
     * Stores the given value at the given key in the current session cache
     * @param key the key under which the record should be stored
     * @param value the value to cache
     */
    void put(String key, def value) {
        session.setAttribute(keyPrefix + key, value)
    }

    /**
     * Gets the record for the given key
     * @param key the record to get
     * @return the matching value or null if no record has been stored
     */
    def get(String key) {
        session.getAttribute(keyPrefix + key)
    }

    /**
     * Removes the given cache entry
     * @param key the key whose record to remove
     */
    void remove(String key) {
        session.removeAttribute(keyPrefix + key)
    }

    /**
     * Clears the current session cache, i.e. removes all entries from the cache
     */
    void clear() {
        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                remove(it.replaceFirst(keyPrefix, ''))
            }
        }
    }
}
