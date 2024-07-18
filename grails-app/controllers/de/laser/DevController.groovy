package de.laser

 
import grails.plugin.springsecurity.annotation.Secured

/**
 * This is a controller for test functions
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController  {

    ContextService contextService

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def frontend() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        result
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def klodav() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        render view: 'klodav/index', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def icons() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        render view: 'klodav/icons', model: result
    }

    @Secured(['ROLE_ADMIN'])
    def buttons() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        render view: 'klodav/buttons', model: result
    }

    /**
     * JavaScript call area
     */
    @Secured(['ROLE_ADMIN'])
    def jse() {
        if (params.xhr_full) {
            render template: 'jse_xhr_full'
        }
        else if (params.xhr) {
            render template: 'jse_xhr'
        }
        else {
            render view: 'jse'
        }
    }
}
