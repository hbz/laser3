package de.laser

import de.laser.annotations.ShouldBePrivate_DoNotUse
import de.laser.auth.Perm
import de.laser.auth.PermGrant
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.taglib.GroovyPageAttributes
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder


/**
 * This service handles calls related to the current session. Most used is the
 * institution check with {@link #getOrg} in order to check the user's affiliation used for the session
 * (the so-called context org)
 */
@Transactional
class ContextService {

    public final static String RCH_LASER_CONTEXT_ORG    = 'laser_context_org'

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

//                log.debug 'setting ' + RCH_LASER_CONTEXT_ORG + ' for request .. ' + context
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

    /**
     * Retrieves the user cache with the given prefix for the context user
     * @param cacheKeyPrefix the cache entry to retrieve
     * @return the {@link EhcacheWrapper} matching to the given prefix
     * @see {@link User}
     */
    EhcacheWrapper getUserCache(String cacheKeyPrefix) {
        cacheService.getSharedUserCache(getUser(), cacheKeyPrefix)
    }

    /**
     * Retrieves the organisation cache with the given prefix for the context institution
     * @param cacheKeyPrefix the cache entry to retrieve
     * @return the {@link EhcacheWrapper} matching to the given prefix
     * @see {@link Org}
     */
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

    /**
     * Checks if the context user belongs to an institution with the given customer types or is a superadmin
     * @param orgPerms the customer types to verify
     * @return true if the given permissions are granted, false otherwise
     * @see CustomerTypeService
     */
    boolean isInstUser_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_USER', orgPerms, false)
    }

    /**
     * Checks if the context user belongs as an editor to an institution with the given customer types or is a superadmin
     * @param orgPerms the customer types to verify
     * @return true if the given permissions are granted, false otherwise
     * @see CustomerTypeService
     */
    boolean isInstEditor_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_EDITOR', orgPerms, false)
    }

    /**
     * Checks if the context user belongs as a local administrator to an institution with the given customer types or is a superadmin
     * @param orgPerms the customer types to verify
     * @return true if the given permissions are granted, false otherwise
     * @see CustomerTypeService
     */
    boolean isInstAdm_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_ADM', orgPerms, false)
    }

    boolean isInstUser_denySupport_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_USER', orgPerms, true)
    }

    boolean isInstEditor_denySupport_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_EDITOR', orgPerms, true)
    }

    boolean isInstAdm_denySupport_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN('INST_ADM', orgPerms, true)
    }

    // -- private

    /**
     * Checks if the context user is either a superadmin or has the given role at the context institution and if this institution is of the given customer type
     * @param instUserRole the user role type to check
     * @param orgPerms the customer type to check
     * @return true if the user role and institution customer type match or superadministration role has been granted, false otherwise
     * @see User
     * @see CustomerTypeService
     */
    private boolean _hasInstRoleAndPerm_or_ROLEADMIN(String instUserRole, String orgPerms, boolean denyCustomerTypeSupport) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        boolean check = userService.hasAffiliation_or_ROLEADMIN(getUser(), getOrg(), instUserRole)

        if (check && denyCustomerTypeSupport) {
            check = !getOrg().isCustomerType_Support()
        }
        if (check && orgPerms) {
            check = _hasPerm(orgPerms)
        }
        check
    }

    /**
     * Checks if the context organisation is an institution at all and if it is of the given customer type.
     * If no customer type is being submitted, this check returns true (= substitution call)
     * @param orgPerms the customer type(s) to check
     * @return true if the context institution has the given customer type or no customer type has been submitted, false otherwise
     * @see OrgSetting
     * @see CustomerTypeService
     */
    @ShouldBePrivate_DoNotUse
    boolean _hasPerm(String orgPerms) {
        boolean check = false

        if (orgPerms) {
            def oss = OrgSetting.get(getOrg(), OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                orgPerms.split(',').each { op ->
                    if (!check) {
                        check = PermGrant.findByPermAndRole(Perm.findByCode(op.toLowerCase().trim()), (Role) oss.getValue())
                    }
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
     * @return true if editor rights are granted or the context institution is a consortium, false otherwise
     */
    // TODO
    boolean is_ORG_COM_EDITOR_or_ROLEADMIN() {
        isInstEditor_or_ROLEADMIN() && _hasPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    }

    /**
     * Checks if the context user is an editor or a superadmin and if so, whether the context organisation is a consortium or an institution looking at its own data
     * @param inContextOrg are we in the context organisation?
     * @return true if the context organisation is a consortium or an institution looking at its own data, false otherwise
     */
    // TODO
    boolean is_INST_EDITOR_or_ROLEADMIN_with_PERMS_BASIC(boolean inContextOrg) {
        boolean check = false
        if (isInstEditor_or_ROLEADMIN()) {
            check = _hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC) || (_hasPerm(CustomerTypeService.ORG_INST_BASIC) && inContextOrg)
        }
        check
    }

    /**
     * Checks if the context organisation is the given consortium type and the context user has the given role at that consortium
     * @param orgPerms the customer types to check
     * @param instUserRole the user role type to check
     * @return true if the context user is a superadmin or if the context user belongs with the given rights to the context organisation and that is a consortium, false otherwise
     * @see User
     * @see Org
     */
    // TODO
//    boolean hasPermAsInstRoleAsConsortium_or_ROLEADMIN(String orgPerms, String instUserRole) {
//        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
//            return true
//        }
//        if (getUser() && getOrg() && instUserRole) {
//            if (getOrg().getAllOrgTypeIds().contains( RDStore.OT_CONSORTIUM.id )) {
//                if (userService.hasAffiliation_or_ROLEADMIN(getUser(), getOrg(), instUserRole)) {
//                    return _hasPerm(orgPerms)
//                }
//            }
//        }
//        return false
//    }

    // -----

    boolean checkCachedNavPerms(GroovyPageAttributes attrs) {

        boolean check = false
        User user = getUser()
        Org org = getOrg()

        EhcacheWrapper cache = cacheService.getTTL1800Cache('contextService/checkCachedNavPerms/' + user.id + ':' + user.formalOrg.id + ':' + user.formalRole.id)

        Map<String, Boolean> permsMap = [:]
        String permsKey = 'ctxOrg:' + org.id

        if (cache.get(permsKey)) {
            permsMap = cache.get(permsKey) as Map<String, Boolean>
        }

        String perm =  attrs.instRole + ':' + attrs.orgPerm + ':' + attrs.affiliationOrg + ':' + attrs.specRole

        if (permsMap.get(perm) != null) {
            check = (boolean) permsMap.get(perm)
        }
        else {
            check = SpringSecurityUtils.ifAnyGranted(attrs.specRole ?: [])

            if (!check) {
                boolean instRoleCheck = attrs.instRole ? BeanStore.getUserService().hasAffiliation_or_ROLEADMIN(user, org, attrs.instRole) : true
                boolean orgPermCheck  = attrs.orgPerm ? _hasPerm(attrs.orgPerm) : true

                check = instRoleCheck && orgPermCheck

                if (attrs.instRole && attrs.affiliationOrg && check) { // ???
                    check = BeanStore.getUserService().hasAffiliation_or_ROLEADMIN(user, attrs.affiliationOrg, attrs.instRole)
                    // check = user.hasOrgAffiliation_or_ROLEADMIN(attrs.affiliationOrg, attrs.instRole)
                }
            }
            permsMap.put(perm, check)
            cache.put(permsKey, permsMap)
        }

        check
    }
}
