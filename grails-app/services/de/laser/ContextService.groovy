package de.laser

import de.laser.auth.User
import de.laser.helper.EhcacheWrapper
import de.laser.helper.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class ContextService {

    SpringSecurityService springSecurityService
    CacheService cacheService

    static final USER_SCOPE  = 'USER_SCOPE'
    static final ORG_SCOPE   = 'ORG_SCOPE'

    void setOrg(Org context) {
        try {
            SessionCacheWrapper scw = getSessionCache()
            scw.put('contextOrg', context)
        }
        catch (Exception e) {
            log.warn('setOrg() - ' + e.getMessage())
        }
    }

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

    List<Org> getMemberships() {
        User user = getUser()
        user ? user.authorizedOrgs : []
    }

    EhcacheWrapper getCache(String cacheKeyPrefix, String scope) {

        if (scope == ORG_SCOPE) {
            cacheService.getSharedOrgCache(getOrg(), cacheKeyPrefix)
        }
        else if (scope == USER_SCOPE) {
            cacheService.getSharedUserCache(getUser(), cacheKeyPrefix)
        }
    }

    SessionCacheWrapper getSessionCache() {
        return new SessionCacheWrapper()
    }
}
