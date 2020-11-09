package de.laser

import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class HomeController {

    def contextService
    def springSecurityService
 
    @Secured(['ROLE_USER'])
    def index() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()

        if (result.user) {
            log.debug("HomeController::index - ${result.user.id}")
            if (UserSetting.get(result.user, UserSetting.KEYS.DASHBOARD) == UserSetting.SETTING_NOT_FOUND) {
                flash.message = message(code: 'profile.dash.not_set')
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
