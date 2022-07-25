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
     * Sets the picked institution as current context
     * @param context the institution used for the current session
     */
    void setOrg(Org context) {
        try {
            SessionCacheWrapper scw = getSessionCache()
            scw.put('contextOrg', context)
        }
        catch (Exception e) {
            log.warn('setOrg() - ' + e.getMessage())
        }
    }

    /**
     * Retrieves the institution used for the current session
     * @return the institution used for the session (the context org)
     */
    Org getOrg() {
        Org.withNewSession {
            try {
                SessionCacheWrapper scw = getSessionCache()

                def context = scw.get('contextOrg')
                if (! context) {
                    context = getUser()?.getSettingsValue(UserSetting.KEYS.DASHBOARD)

                    if (context) {
                        scw.put('contextOrg', context)
                    }
                }
                if (context) {
                    return (Org) GrailsHibernateUtil.unwrapIfProxy(context)
                }
            }
            catch (Exception e) {
                log.warn('getOrg() - ' + e.getMessage())
            }
            return null
        }
    }

    /**
     * Retrieves the user of the current session
     * @return the user object
     */
    User getUser() {
        //User.withNewSession { ?? LazyInitializationException in /profile/index 
            try {
                if (springSecurityService.isLoggedIn()) {
                    return (User) springSecurityService.getCurrentUser()
                }
            }
            catch (Exception e) {
                log.warn('getUser() - ' + e.getMessage())
            }
            return null
        //}
    }

    /**
     * Retrieves a list of the current user's affiliations
     * @return a list of {@link Org}s (institutions) to which the context user is affiliated
     */
    List<Org> getMemberships() {
        User user = getUser()
        user ? user.getAffiliationOrgs() : []
    }

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
