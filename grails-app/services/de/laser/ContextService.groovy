package de.laser

import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This service handles calls related to the current session. Most used is the
 * institution check with {@link #getOrg} in order to check the user's affiliation used for the session
 * (the so-called context org)
 */
@Transactional
class ContextService {

    CacheService cacheService
    SpringSecurityService springSecurityService

    /**
     * Retrieves the institution used for the current session
     * @return the institution used for the session (the context org)
     */
    Org getOrg() {
        // todo

        try {
            def context = getUser()?.formalOrg
            if (context) {
                return (Org) GrailsHibernateUtil.unwrapIfProxy(context)
            }
        }
        catch (Exception e) {
            log.warn('getOrg() - ' + e.getMessage())
        }
        return null

//            try {
//                SessionCacheWrapper scw = getSessionCache()
//
//                def context = scw.get('contextOrg')
//                if (! context) {
//                    context = getUser()?.formalOrg
//
//                    if (context) {
//                        scw.put('contextOrg', context)
//                    }
//                }
//                if (context) {
//                    return (Org) GrailsHibernateUtil.unwrapIfProxy(context)
//                }
//            }
//            catch (Exception e) {
//                log.warn('getOrg() - ' + e.getMessage())
//            }
//            return null
    }

    /**
     * Retrieves the user of the current session
     * @return the user object
     */
    User getUser() {
        try {
            if (springSecurityService.isLoggedIn()) {
                return (User) springSecurityService.getCurrentUser()
            }
        }
        catch (Exception e) {
            log.warn('getUser() - ' + e.getMessage())
        }
        return null
    }

    // -- Cache --

    EhcacheWrapper getUserCache(String cacheKeyPrefix) {
        cacheService.getSharedUserCache(getUser(), cacheKeyPrefix)
    }

    EhcacheWrapper getSharedOrgCache(String cacheKeyPrefix) {
        cacheService.getSharedOrgCache(getOrg(), cacheKeyPrefix)
    }

    /**
     * Initialises the session cache
     * @return a new session cache wrapper instance
     */
    SessionCacheWrapper getSessionCache() {
        return new SessionCacheWrapper()
    }
}
