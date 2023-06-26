package de.laser

import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.storage.RDStore
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

    // -- Formal --

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

    // -- CONTEXT CHECKS -- user.formalOrg based perm/role checks - all withFakeRole --

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
        _hasAffiliation(orgPerms, 'INST_USER')
    }
    boolean hasPermAsInstEditor_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasAffiliation(orgPerms, 'INST_EDITOR')
    }
    boolean hasPermAsInstAdm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasAffiliation(orgPerms, 'INST_ADM')
    }

    private boolean _hasAffiliation(String orgPerms, String instUserRole) {
        accessService._hasPermAndAffiliation_forCtxOrg_withFakeRole_forCtxUser(orgPerms.split(','), instUserRole)
        // accessService.ctxPermAffiliation(orgPerms, instUserRole)
    }

    // TODO
    boolean hasAffiliationForConsortium_or_ROLEADMIN(String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        if (getUser() && getOrg() && instUserRole) {
            if (getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )) {
                return _hasAffiliation(orgPerms, instUserRole)
            }
        }
        // accessService.ctxConsortiumCheckPermAffiliation_or_ROLEADMIN(orgPerms, instUserRole)

//        if (contextService.getUser() && contextService.getOrg() && instUserRole) {
//            if (contextService.getUser().hasCtxAffiliation_or_ROLEADMIN( instUserRole )) {
//                if (contextService.getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )) {
//                    return _hasPerm_forOrg_withFakeRole(orgPerms.split(','), contextService.getOrg())
//                }
//            }
//        }
        return false
    }
}
