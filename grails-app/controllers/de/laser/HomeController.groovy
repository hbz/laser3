package de.laser

import de.laser.auth.User
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class HomeController {

    def springSecurityService
 
    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        log.debug("HomeController::index - ${springSecurityService.principal.id}");

        result.user = User.get(springSecurityService.principal.id)
        if (result.user) {

            if (UserSetting.get(result.user, UserSetting.KEYS.DASHBOARD) == UserSetting.SETTING_NOT_FOUND) {
                flash.message = message(code: 'profile.dash.not_set')
                redirect(controller: 'profile', action: 'index')
                return
            }
            redirect(controller: 'myInstitution', action: 'dashboard')
        }
        else {
            log.error("Unable to lookup user for principal id :: ${springSecurityService.principal.id}");
        }
    }
}
