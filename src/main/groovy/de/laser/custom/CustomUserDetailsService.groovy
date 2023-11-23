package de.laser.custom

import de.laser.utils.CodeUtils
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import grails.plugin.springsecurity.SpringSecurityUtils

import groovy.util.logging.Slf4j

/**
 * Mapping class for the custom user details management to the common Grails user details management
 */
@Slf4j
class CustomUserDetailsService implements GrailsUserDetailsService {

    static final GrantedAuthority NO_ROLE = new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)

    GrailsApplication grailsApplication

    /**
     * Fetches for the user matching the given name the details; roles may be fetched in addition
     * @param username the name to which the user including details should be retrieved
     * @param loadRoles should the roles be fetched as well?
     * @return the {@link UserDetails} for the given username
     * @throws UsernameNotFoundException
     */
    @Transactional(readOnly=true, noRollbackFor=[IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {

        ConfigObject conf = SpringSecurityUtils.securityConfig
        String userClassName = conf.userLookup.userDomainClassName
        GrailsClass dc = CodeUtils.getDomainArtefact(userClassName)
        if (!dc) {
            throw new IllegalArgumentException("The specified user domain class '$userClassName' is not a domain class")
        }

        Class<?> User = dc.clazz

        def user = User.createCriteria().get {
            if(conf.userLookup.usernameIgnoreCase) {
                eq((conf.userLookup.usernamePropertyName), username, [ignoreCase: true])
            }
            else {
                eq((conf.userLookup.usernamePropertyName), username)
            }
        }

        if (!user) {
            log.warn 'User not found: {}', username
            throw new NoStackUsernameNotFoundException()
        }

        Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
        createUserDetails user, authorities
    }

    /**
     *
     * @param username the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        loadUserByUsername username, true
    }

    /**
     * Collects the rights of the given user
     * @param user the user whose authorities should be returned
     * @param username unused
     * @param loadRoles dummy flag; if set to false, an empty list is being returned to fill up a gap
     * @return a {@link Collection} of {@link GrantedAuthority} rights granted to the given user
     */
    protected Collection<GrantedAuthority> loadAuthorities(user, String username, boolean loadRoles) {
        if (!loadRoles) {
            return []
        }

        def conf = SpringSecurityUtils.securityConfig

        String authoritiesPropertyName = conf.userLookup.authoritiesPropertyName
        String authorityPropertyName = conf.authority.nameField

        boolean useGroups = conf.useRoleGroups
        String authorityGroupPropertyName = conf.authority.groupAuthorityNameField

        Collection<?> userAuthorities = user."$authoritiesPropertyName"
        def authorities

        if (useGroups) {
            if (authorityGroupPropertyName) {
                authorities = userAuthorities.collect { it."$authorityGroupPropertyName" }.flatten().unique().collect { new SimpleGrantedAuthority(it."$authorityPropertyName") }
            }
            else {
                log.warn 'Attempted to use group authorities, but the authority name field for the group class has not been defined.'
            }
        }
        else {
            authorities = userAuthorities.collect { new SimpleGrantedAuthority(it."$authorityPropertyName") }
        }
        authorities ?: [NO_ROLE]
    }

    /**
     * Maps the basic user data and the rights to a UserDetails representation
     * @param user the basic user object
     * @param authorities the institution to which the user belongs to resp. the rights the user has
     * @return the {@link UserDetails} of the given user
     */
    protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {

        def conf = SpringSecurityUtils.securityConfig

        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."$usernamePropertyName"
        String password = user."$passwordPropertyName"
        boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
        boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
        boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false

        new CustomUserDetails(
                username,
                password,
                enabled,
                !accountExpired,
                !passwordExpired,
                !accountLocked,
                authorities,
                user.id
        )
    }
}
