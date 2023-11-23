package de.laser.custom

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.resolvers.SpringSecurityRequestResolver

/**
 * Implementation of audit request handler
 */
class CustomAuditRequestResolver extends SpringSecurityRequestResolver {

    SpringSecurityService springSecurityService

    /**
     * Returns the current actor. If the actor is other than anonymous, an anonymised string will be returned
     * to fulfill data protection requests
     * @return one of null, anonymousUser, SYS, N/A, anonymised
     */
    @Override
    String getCurrentActor() {
        String username = springSecurityService.getCurrentUser()?.username

        if (SpringSecurityUtils.isSwitched() && username){
            username = SpringSecurityUtils.switchedUserOriginalUsername + " AS " + username
        }

        return (username in [null, 'anonymousUser', 'SYS', 'N/A']) ? username : 'anonymised'
    }
}