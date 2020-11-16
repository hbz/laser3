package de.laser.custom

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.resolvers.SpringSecurityRequestResolver

class CustomAuditRequestResolver extends SpringSecurityRequestResolver {

    def springSecurityService

    @Override
    String getCurrentActor() {
        String username = springSecurityService.getCurrentUser()?.username

        if (SpringSecurityUtils.isSwitched() && username){
            username = SpringSecurityUtils.switchedUserOriginalUsername + " AS " + username
        }

        return (username in [null, 'anonymousUser', 'SYS', 'N/A']) ? username : 'anonymised'
    }
}