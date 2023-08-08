package de.laser

import de.laser.annotations.ShouldBePrivate_DoNotUse
import de.laser.auth.Perm
import de.laser.auth.PermGrant
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder


/**
 * This service handles calls related to the current session. Most used is the
 * institution check with {@link #getOrg} in order to check the user's affiliation used for the session
 * (the so-called context org)
 */
@Transactional
class ContextService {

    public final static String RCH_LASER_CONTEXT_ORG = 'laser.context.org'

    CacheService cacheService
    SpringSecurityService springSecurityService
    UserService userService

    // -- Formal/context object getter --

    /**
     * Retrieves the institution used for the current session
     * @return the institution used for the session (the context org)
     */
    Org getOrg() {
        RequestAttributes ra = RequestContextHolder.currentRequestAttributes()

        Org context = ra.getAttribute(RCH_LASER_CONTEXT_ORG, RequestAttributes.SCOPE_REQUEST) as Org
        if (context) {
            // log.debug 'using ' + RCH_LASER_CONTEXT_ORG
        }
        else {
            try {
                context = GrailsHibernateUtil.unwrapIfProxy(getUser()?.formalOrg) as Org

                log.debug 'setting ' + RCH_LASER_CONTEXT_ORG + ' for request .. ' + context
                ra.setAttribute(RCH_LASER_CONTEXT_ORG, context, RequestAttributes.SCOPE_REQUEST)

//                ra.getAttributeNames(RequestAttributes.SCOPE_REQUEST)
//                        .findAll {it.startsWith('laser')}
//                        .each {log.debug '' + it + ' : ' + ra.getAttribute(it, RequestAttributes.SCOPE_REQUEST)}
            }
            catch (Exception e) {
                log.warn('getOrg() - ' + e.getMessage())
            }
        }
        context
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

    // -- Formal checks @ user.formalOrg

    boolean isInstUser_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_USER', orgPerms)
    }
    boolean isInstEditor_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_EDITOR', orgPerms)
    }
    boolean isInstAdm_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_ADM', orgPerms)
    }

    // -- private

    private boolean _hasInstRoleAndPerm_or_ROLEADMIN(String instUserRole, String orgPerms) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        boolean check = userService.hasAffiliation_or_ROLEADMIN(getUser(), getOrg(), instUserRole)

        if (check && orgPerms) {
            check = _hasPerm(orgPerms)
        }
        check
    }

    @ShouldBePrivate_DoNotUse
    boolean _hasPerm(String orgPerms) {
        boolean check = false

        if (orgPerms) {
            def oss = OrgSetting.get(getOrg(), OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                orgPerms.split(',').each { op ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(op.toLowerCase().trim()), (Role) oss.getValue())
                }
            }
        } else {
            check = true
        }
        check
    }

    // ----- REFACTORING ?? -----

    /**
     * Replacement call for the abandoned ROLE_ORG_COM_EDITOR
     */
    // TODO
    boolean is_ORG_COM_EDITOR_or_ROLEADMIN() {
        isInstEditor_or_ROLEADMIN() && _hasPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    }

    // TODO
    boolean is_INST_EDITOR_or_ROLEADMIN_with_PERMS_BASIC(boolean inContextOrg) {
        boolean check = false
        if (isInstEditor_or_ROLEADMIN()) {
            check = _hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC) || (_hasPerm(CustomerTypeService.ORG_INST_BASIC) && inContextOrg)
        }
        check
    }

    // TODO
    boolean hasPermAsInstRoleAsConsortium_or_ROLEADMIN(String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        if (getUser() && getOrg() && instUserRole) {
            if (getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )) {
                if (userService.hasAffiliation_or_ROLEADMIN(getUser(), getOrg(), instUserRole)) {
                    return _hasPerm(orgPerms)
                }
            }
        }
        return false
    }
}
