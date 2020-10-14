package de.laser


import de.laser.auth.User
import de.laser.helper.EhcacheWrapper
import de.laser.helper.SessionCacheWrapper
import grails.plugin.springsecurity.SpringSecurityService
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.web.servlet.mvc.GrailsHttpSession
import org.grails.web.util.WebUtils

@CompileStatic
@Transactional
class ContextService {

    SpringSecurityService springSecurityService
    CacheService cacheService

    static final USER_SCOPE  = 'USER_SCOPE'
    static final ORG_SCOPE   = 'ORG_SCOPE'

    void setOrg(Org context) {
        try {
            GrailsHttpSession session = WebUtils.retrieveGrailsWebRequest().getSession()
            session.setAttribute('contextOrg', context)
        }
        catch (Exception e) {
            log.warn('accessing setOrg() without web request')
        }
    }

    Org getOrg() {
        try {
            GrailsHttpSession session  = WebUtils.retrieveGrailsWebRequest().getSession()

            def context = session.getAttribute('contextOrg')
            if (! context) {
                context = getUser()?.getSettingsValue(UserSetting.KEYS.DASHBOARD)

                if (context) {
                    session.setAttribute('contextOrg', context)
                }
            }

            if (context) {
                return (Org) GrailsHibernateUtil.unwrapIfProxy(context)
            }
        }
        catch (Exception e) {
            log.warn('accessing getOrg() without web request')
        }
        return null
    }

    User getUser() {
        (User) springSecurityService.getCurrentUser()
    }

    List<Org> getMemberships() {
        getUser()?.authorizedOrgs
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
