package de.laser

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * This is a controller for test functions
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DevController  {

    ContextService contextService
    LicenseService licenseService

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

    @Secured(['ROLE_ADMIN'])
    def markdown() {
        Map<String, Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        render view: 'klodav/markdown', model: result
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

    @Secured(['ROLE_ADMIN'])
    def onixValidationPrecheck() {
        Org institution = contextService.getOrg()
        License license = License.get(params.id)
        Map<String, Object> result = [validationErrors: licenseService.precheckValidation(license, institution)]
        if(result.validationErrors == null) {
            redirect controller: 'license', action: 'show', params: [export: 'onix', id: params.id]
        }
        else
            render result as JSON
    }
}
