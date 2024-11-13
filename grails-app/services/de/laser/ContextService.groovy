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
        return GrailsHibernateUtil.unwrapIfProxy(getUser()?.formalOrg) as Org

        /*
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
        */
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
    EhcacheWrapper getOrgCache(String cacheKeyPrefix) {
        cacheService.getSharedOrgCache(getOrg(), cacheKeyPrefix)
    }

    /**
     * Initialises the session cache
     * @return a new session cache wrapper instance
     */
    SessionCacheWrapper getSessionCache() {
        return new SessionCacheWrapper()
    }

    /**
     * Gets the cache key token for the given user
     * @return the user's cache key in format [user.id:user.formalOrg.id:user.formalRole.id]
     */
    String getFormalCacheKeyToken() {
        User user = getUser()
        Role customerType = OrgSetting.executeQuery(
                'select oss.roleValue from OrgSetting oss where oss.org = :org and oss.key = :key',
                [org: user.formalOrg, key: OrgSetting.KEYS.CUSTOMER_TYPE]
        )?.first()

        '[' + user.id + ':' + user.formalRole.id + ':' + user.formalOrg.id + ':' + (customerType ? customerType.id : '#') + ']'
    }

    // -- Formal checks @ user.formalOrg

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_USER
     */
    boolean isInstUser(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_USER, orgPerms, false)
    }

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_EDITOR
     */
    boolean isInstEditor(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_EDITOR, orgPerms, false)
    }

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_ADM
     */
    boolean isInstAdm(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_ADM, orgPerms, false)
    }

    @Deprecated
    boolean isInstEditor_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN(Role.INST_EDITOR, orgPerms, false)
    }

    /**
     * Checks if the context user belongs as a local administrator to an institution with the given customer types or is a superadmin
     * @param orgPerms the customer types to verify
     * @return true if the given permissions are granted, false otherwise
     * @see CustomerTypeService
     */
    @Deprecated
    boolean isInstAdm_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN(Role.INST_ADM, orgPerms, false)
    }

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_USER
     * <br>
     * Restriction: {@link de.laser.Org#isCustomerType_Support()} is FALSE
     */
    boolean isInstUser_denySupport(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_USER, orgPerms, true)
    }

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_EDITOR
     * <br>
     * Restriction: {@link de.laser.Org#isCustomerType_Support()} is FALSE
     */
    boolean isInstEditor_denySupport(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_EDITOR, orgPerms, true)
    }

    /**
     * Shortcut for {@link de.laser.UserService#hasFormalAffiliation(de.laser.Org, java.lang.String)}
     * with {@link de.laser.ContextService#getUser()}, {@link de.laser.ContextService#getOrg()} and ROLE.INST_ADM
     * <br>
     * Restriction: {@link de.laser.Org#isCustomerType_Support()} is FALSE
     */
    boolean isInstAdm_denySupport(String orgPerms = null) {
        _hasInstRoleAndPerm(Role.INST_ADM, orgPerms, true)
    }

    @Deprecated
    boolean isInstAdm_denySupport_or_ROLEADMIN(String orgPerms = null) {
        _hasInstRoleAndPerm_or_ROLEADMIN(Role.INST_ADM, orgPerms, true)
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

        _hasInstRoleAndPerm(instUserRole, orgPerms, denyCustomerTypeSupport)
    }

    private boolean _hasInstRoleAndPerm(String instUserRole, String orgPerms, boolean denyCustomerTypeSupport) {
        boolean check = userService.hasFormalAffiliation(getOrg(), instUserRole)

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
     * Do not use this method directly!
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
        _hasInstRoleAndPerm_or_ROLEADMIN(Role.INST_EDITOR, null, false) && _hasPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    }

    /**
     * Checks if the context user is an editor or a superadmin and if so, whether the context organisation is a consortium or an institution looking at its own data
     * @param inContextOrg are we in the context organisation?
     * @return true if the context organisation is a consortium or an institution looking at its own data, false otherwise
     */
    // TODO
    boolean is_INST_EDITOR_with_PERMS_BASIC(boolean inContextOrg) {
        boolean check = false
        if (isInstEditor()) {
            check = _hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC) || (_hasPerm(CustomerTypeService.ORG_INST_BASIC) && inContextOrg)
        }
        check
    }

    // -----

    /**
     * Performs the navigation panel check against cached entries
     * @param attrs the cached page call attributes
     * @return true if access is granted, false otherwise
     */
    boolean checkCachedNavPerms(GroovyPageAttributes attrs) {

        boolean check = false
        Org org = getOrg()

        EhcacheWrapper ttl1800 = cacheService.getTTL1800Cache('ContextService/checkCachedNavPerms')

        Map<String, Boolean> permsMap = [:]
        String permsKey = getFormalCacheKeyToken()

        if (ttl1800.get(permsKey)) {
            permsMap = ttl1800.get(permsKey) as Map<String, Boolean>
        }

        String perm =  attrs.instRole + ':' + attrs.orgPerm + ':' + attrs.affiliationOrg + ':' + attrs.specRole

        if (permsMap.get(perm) != null) {
            check = (boolean) permsMap.get(perm)
        }
        else {
            check = SpringSecurityUtils.ifAnyGranted(attrs.specRole ?: [])

            if (!check) {
                boolean instRoleCheck = attrs.instRole ? BeanStore.getUserService().hasAffiliation(org, attrs.instRole) : true
                boolean orgPermCheck  = attrs.orgPerm ? _hasPerm(attrs.orgPerm) : true

                check = instRoleCheck && orgPermCheck

                if (attrs.instRole && attrs.affiliationOrg && check) { // ???
                    check = BeanStore.getUserService().hasAffiliation(attrs.affiliationOrg, attrs.instRole)
                }
            }
            permsMap.put(perm, check)
            ttl1800.put(permsKey, permsMap)
        }

        check
    }

    // ----- DEPRECATED -----

    // moved from AccessService ..
    /**
     * Checks if
     * <ol>
     *     <li>the target institution is of the given customer type and the user has the given permissions granted at the target institution</li>
     *     <li>there is a combo relation to the given target institution</li>
     *     <li>or if the user has the given permissions granted at the context institution</li>
     * </ol>
     * @param attributes a configuration map:
     * [
     *      orgToCheck: context institution,
     *      orgPerms: customer type of the target institution
     *      instUserRole: user's rights for the target institution
     * ]
     * @return true if clauses one and two or three succeed, false otherwise
     */
    @Deprecated
    boolean otherOrgAndComboCheckPermAffiliation_or_ROLEADMIN(Org orgToCheck, String orgPerms, String instUserRole) {
        if (SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')) {
            return true
        }
        Org ctx = getOrg()

        // combo check @ contextUser/contextOrg
        boolean check1 = userService.hasFormalAffiliation(ctx, instUserRole) && _hasPerm(orgPerms)
        boolean check2 = (orgToCheck.id == ctx.id) || Combo.findByToOrgAndFromOrg(ctx, orgToCheck)

        // orgToCheck check @ otherOrg
        boolean check3 = (orgToCheck.id == ctx.id) && SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        // boolean check3 = (ctx.id == orgToCheck.id) && getUser()?.hasCtxAffiliation_or_ROLEADMIN(null) // legacy - no affiliation given

        (check1 && check2) || check3
    }
}
