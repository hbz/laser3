package de.laser.custom

import de.laser.auth.User
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

/**
 * Contains the details of a user logged in; maps the {@link User} domain to {@link GrailsUser}
 */
class CustomUserDetails extends GrailsUser {

    User user

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
