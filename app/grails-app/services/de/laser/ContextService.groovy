package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.UserSettings
import de.laser.helper.EhcacheWrapper
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.util.WebUtils

class ContextService {

    def springSecurityService
    def cacheService

    static final SERVER_LOCAL = 'SERVER_LOCAL'
    static final SERVER_DEV   = 'SERVER_DEV'
    static final SERVER_QA    = 'SERVER_QA'
    static final SERVER_PROD  = 'SERVER_PROD'

    def setOrg(Org context) {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        session.setAttribute('contextOrg', context)
    }

    def getOrg() {
        def session = WebUtils.retrieveGrailsWebRequest().getSession()
        def context = session.getAttribute('contextOrg') ?: getUser()?.getSettingsValue(UserSettings.KEYS.DASHBOARD)
//        context?.refresh()
        GrailsHibernateUtil.unwrapIfProxy(context) // fix: unwrap proxy
    }

    def getUser() {
        springSecurityService.getCurrentUser()
    }

    def getMemberships() {
        getUser()?.authorizedOrgs
    }

    def getCache(def cacheKeyPrefix) {
        def cacheName    = "${getUser().username}#${getUser().id}"
        def cacheManager = cacheService.getCacheManager(cacheService.EHCACHE)
        def cache        = cacheService.getCache(cacheManager, cacheName)

        return new EhcacheWrapper(cache, cacheKeyPrefix)
    }
}
