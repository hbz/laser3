package de.laser

import de.laser.annotations.ShouldBePrivate_DoNotUse
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
    UserService userService

    // -- Formal/context object getter --

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

    // -- Formal checks @ user.isFormal(user.formalRole, user.formalOrg)

    boolean isInstUser_or_ROLEADMIN() {
        _hasInstRole_or_ROLEADMIN('INST_USER')
    }
    boolean isInstEditor_or_ROLEADMIN() {
        _hasInstRole_or_ROLEADMIN('INST_EDITOR')
    }
    boolean isInstAdm_or_ROLEADMIN() {
        _hasInstRole_or_ROLEADMIN('INST_ADM')
    }

    // -- Formal checks @ user.formalOrg.perm

    /**
     * Permission check (granted by customer type) for the current context org.
     */
    boolean hasPerm(String orgPerms) {
        accessService.otherOrgPerm(getOrg(), orgPerms)
    }
    boolean hasPerm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        hasPerm(orgPerms)
    }

    // -- Formal checks @ user.formalOrg.perm + user.isFormal(role, formalOrg)

    boolean hasPermAsInstUser_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndInstRole(orgPerms, 'INST_USER')
    }
    boolean hasPermAsInstEditor_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndInstRole(orgPerms, 'INST_EDITOR')
    }
    boolean hasPermAsInstAdm_or_ROLEADMIN(String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        _hasPermAndInstRole(orgPerms, 'INST_ADM')
    }

    boolean hasPermAsInstRoleAsConsortium_or_ROLEADMIN(String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        if (getUser() && getOrg() && instUserRole) {
            if (getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )) {
                return _hasPermAndInstRole(orgPerms, instUserRole)
            }
        }
        return false
    }

    // -- private

    private boolean _hasInstRole_or_ROLEADMIN(String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        userService.hasAffiliation_or_ROLEADMIN(getUser(), getOrg(), instUserRole)
    }

    @ShouldBePrivate_DoNotUse
    boolean _hasPermAndInstRole(String orgPerms, String instUserRole) {
        if (getUser() && instUserRole) {
            if (_hasInstRole_or_ROLEADMIN(instUserRole)) {
                return hasPerm(orgPerms)
            }
        }
        return false
    }

    // ----- REFACTORING ?? -----

    /**
     * Replacement call for the abandoned ROLE_ORG_COM_EDITOR
     */
    // TODO
    boolean is_ORG_COM_EDITOR() {
        _hasPermAndInstRole(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')
    }

    // TODO
    boolean is_INST_EDITOR_with_PERMS_BASIC(boolean inContextOrg) {
        boolean a = _hasPermAndInstRole(CustomerTypeService.ORG_INST_BASIC, 'INST_EDITOR') && inContextOrg
        boolean b = _hasPermAndInstRole(CustomerTypeService.ORG_CONSORTIUM_BASIC, 'INST_EDITOR')

        return (a || b)
    }
}
