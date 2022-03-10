package de.laser

 
import grails.plugin.springsecurity.annotation.Secured

/**
 * This is a controller for test functions
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController  {

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def frontend() {
        Map<String, Object> result = [test:123]
        result
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
