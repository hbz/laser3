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
    def laserPlan() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        result.mappingColsBasic = ["licence","asService", "accessRights"]
        result.mappingColsPro = ["erms", "propertiesUse", "propertiesCreation", "cost", "ie", "docs", "tasks", "notifications", "address", "budget", "reporting", "testSystem", "community", "wekb", "api"]
        result.mappingColsServiceBasic = ["support", "help", "handbook", "progression", "trainingFundamentals"]
        result.mappingColsServicePro = ["trainingIndividual", "userMeeting"]

        result
    }

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['ROLE_ADMIN'])
    def klodav() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
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
