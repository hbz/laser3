package de.laser

import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This service handles calls related to the current session. Most used is the
 * institution check with {@link #getOrg} in order to check the user's affiliation used for the session
 * (the so-called context org)
 */
@Transactional
class ContextService {

    AccessService accessService
    CacheService cacheService
    SpringSecurityService springSecurityService

    /**
     * Retrieves the institution used for the current session
     * @return the institution used for the session (the context org)
     */
    Org getOrg() {
        // todo

        try {
            Org context = getUser()?.formalOrg
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

    // -- Context checks -- user.formalOrg based perm/role checks - all withFakeRole --
    // TODO - refactoring

    /**
     * Permission check (granted by customer type) for the current context org.
     */
    boolean hasPerm(String orgPerms) {
        accessService._hasPerm_forOrg_withFakeRole(orgPerms.split(','), getOrg())
    }
    boolean hasPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        hasPerm(orgPerms)
    }
    boolean hasPermAsInstUser_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        accessService._hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_USER')
    }
    boolean hasPermAsInstEditor_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        accessService._hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_EDITOR')
    }
    boolean hasPermAsInstAdm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        accessService._hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), 'INST_ADM')
    }

    // TODO
    boolean hasAffiliationX(String orgPerms, String instUserRole) {
        accessService.ctxPermAffiliation(orgPerms, instUserRole)
        // -> accessService._hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser
        // + - -> user.hasCtxAffiliation_or_ROLEADMIN
        //     + -> userService.checkAffiliation_or_ROLEADMIN
        // + - -> accessService._hasPerm_forOrg_withFakeRole
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
