package de.laser

import de.laser.auth.User
import de.laser.helper.EhcacheWrapper
import de.laser.helper.SessionCacheWrapper
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

    SpringSecurityService springSecurityService
    CacheService cacheService

    static final USER_SCOPE  = 'USER_SCOPE'
    static final ORG_SCOPE   = 'ORG_SCOPE'

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
        user ? user.authorizedOrgs : []
    }

    /**
     * Retrieves the session cache of the given scope for the given prefix
     * @param cacheKeyPrefix the prefix to retrieve
     * @param scope the cache scope used or to be used for the storage
     * @return the cache for the given key prefix
     */
    EhcacheWrapper getCache(String cacheKeyPrefix, String scope) {

        if (scope == ORG_SCOPE) {
            cacheService.getSharedOrgCache(getOrg(), cacheKeyPrefix)
        }
        else if (scope == USER_SCOPE) {
            cacheService.getSharedUserCache(getUser(), cacheKeyPrefix)
        }
    }

    /**
     * Initialises the session cache
     * @return a new session cache wrapper instance
     */
    SessionCacheWrapper getSessionCache() {
        return new SessionCacheWrapper()
    }
}
