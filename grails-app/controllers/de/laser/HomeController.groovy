package de.laser

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller is for the main index routing
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class HomeController {

    ContextService contextService
    SpringSecurityService springSecurityService

    /**
     * The greeting committee in some kind. The webapp index redirected here as well
     * @return the institution dashboard; if no dashboard is defined, the profile page; null (resp. error page) if the user could not be found
     */
    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser().refresh()

        if (result.user) {
            log.debug("HomeController::index - ${result.user.id} - ${result.user.formalOrg}")
            if (! result.user.formalOrg) {
                flash.error = 'Es existiert noch keine Zugehörigkeit .. (TODO)'
                redirect(controller: 'profile', action: 'index')
                return
            }
            redirect(controller: 'myInstitution', action: 'dashboard')
        }
        else {
            log.error("Unable to lookup user for principal : ${springSecurityService.principal}")
        }
    }
}
