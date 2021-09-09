package de.laser.helper

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
    def getSession() {
        session
    }

    def list() {
        Map<String, Object> result = [:]

        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                result << ["${it.replaceFirst(keyPrefix, '')}" : get(it.replaceFirst(keyPrefix, ''))]
            }
        }
        result
    }
    List getKeys() {
        session.getAttributeNames().findAll{ it.startsWith(keyPrefix) }
    }
    def put(String key, def value) {
        session.setAttribute(keyPrefix + key, value)
    }
    def get(String key) {
        session.getAttribute(keyPrefix + key)
    }
    def remove(String key) {
        session.removeAttribute(keyPrefix + key)
    }
    def clear() {
        session.getAttributeNames().each {
            if (it.startsWith(keyPrefix)){
                remove(it.replaceFirst(keyPrefix, ''))
            }
        }
    }
}
