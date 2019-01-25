package de.laser

import net.sf.ehcache.Cache
import com.k_int.kbplus.Org
import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import de.laser.helper.EhcacheWrapper
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession
import org.codehaus.groovy.grails.web.util.WebUtils

@CompileStatic
class ContextService {

    SpringSecurityService springSecurityService
    CacheService cacheService

    static final SERVER_LOCAL = 'SERVER_LOCAL'
    static final SERVER_DEV   = 'SERVER_DEV'
    static final SERVER_QA    = 'SERVER_QA'
    static final SERVER_PROD  = 'SERVER_PROD'

    void setOrg(Org context) {
        GrailsHttpSession session = WebUtils.retrieveGrailsWebRequest().getSession()
        session.setAttribute('contextOrg', context)
    }

    Org getOrg() {
        GrailsHttpSession session = WebUtils.retrieveGrailsWebRequest().getSession()
        def context = session.getAttribute('contextOrg') ?: getUser()?.getSettingsValue(UserSettings.KEYS.DASHBOARD)

        if (context) {
            (Org) GrailsHibernateUtil.unwrapIfProxy(context)
        }
        else {
            return null
        }
    }

    User getUser() {
        (User) springSecurityService.getCurrentUser()
    }

    List<Org> getMemberships() {
        getUser()?.authorizedOrgs
    }

    EhcacheWrapper getCache(String cacheKeyPrefix) {
        def cacheName    = "${getUser().username}#${getUser().id}"
        def cacheManager = cacheService.getCacheManager(cacheService.EHCACHE)
        Cache cache      = (Cache) cacheService.getCache(cacheManager, cacheName)

        return new EhcacheWrapper(cache, cacheKeyPrefix)
    }
}
