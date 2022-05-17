package de.laser.cache

import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession

class SessionCacheWrapper {

    private HttpSession session
    private String keyPrefix = 'laserSessionCache_'

    SessionCacheWrapper() {
        if (! session) {
            session = RequestContextHolder.currentRequestAttributes().getSession()
        }
    }
    HttpSession getSession() {
        session
    }

    Map<String, Object> list() {
        Map<String, Object> result = [:]

        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                result.putAt( it.replaceFirst(keyPrefix, ''), get(it.replaceFirst(keyPrefix, '')) )
            }
        }
        result
    }
    List getKeys() {
        session.getAttributeNames().findAll{ it.startsWith(keyPrefix) }
    }
    void put(String key, def value) {
        session.setAttribute(keyPrefix + key, value)
    }
    def get(String key) {
        session.getAttribute(keyPrefix + key)
    }
    void remove(String key) {
        session.removeAttribute(keyPrefix + key)
    }
    void clear() {
        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                remove(it.replaceFirst(keyPrefix, ''))
            }
        }
    }
}
