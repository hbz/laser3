package de.laser.custom.auth

import de.laser.auth.User
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

/**
 * Contains the details of a user logged in; maps the {@link User} domain to {@link GrailsUser}
 */
class CustomUserDetails extends GrailsUser {

    User user

    /**
     * Constructor class for the user container
     * @param username name of user
     * @param password the user's password
     * @param enabled is the account enabled?
     * @param accountNonExpired is the account not expired (i.e. active)?
     * @param credentialsNonExpired are the account credentials not expired (i.e. valid)?
     * @param accountNonLocked is the account not locked?
     * @param authorities the permissions granted to the account
     * @param id the user database ID
     */
    CustomUserDetails (
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<GrantedAuthority> authorities,
            long id
    ) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)

        this.user = User.findById(id)
    }
}
