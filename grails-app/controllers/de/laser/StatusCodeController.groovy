package de.laser

import de.laser.annotations.Check404
import de.laser.utils.AppUtils
import de.laser.config.ConfigMapper
import de.laser.utils.DateUtils
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugin.springsecurity.annotation.Secured
import grails.web.Action
import grails.web.mapping.UrlMappingData
import org.grails.exceptions.ExceptionUtils
import org.grails.web.mapping.DefaultUrlMappingParser

/**
 * This controller handles the server code mapping output
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class StatusCodeController {

    GrailsApplication grailsApplication

    /**
     * Shows the error page with stack trace extracts; acts on codes 405 and 500
     */
    def error() {
        Map<String, Object> result = [
                exception: request.getAttribute('exception') ?: request.getAttribute('javax.servlet.error.exception'),
                status: request.getAttribute('javax.servlet.error.status_code'),
                mailString: ''
        ]

        if (result.exception) {

            Throwable exception = (Throwable) result.exception
            Throwable root = ExceptionUtils.getRootCause(exception)

            String nl = " %0D%0A"

            result.mailString =
                    "mailto:laser@hbz-nrw.de?subject=Fehlerbericht - " + ConfigMapper.getLaserSystemId() +
                    "&body=Ihre Fehlerbeschreibung (bitte angeben): " + nl + nl +
                    "URI: "     + request.forwardURI + nl +
                    "Zeitpunkt: " + DateUtils.getLocalizedSDF_noZ().format( new Date() ) + nl +
                    "System: "  + ConfigMapper.getLaserSystemId() + nl +
                    "Branch: "  + AppUtils.getMeta('git.branch') + nl +
                    "Commit: "  + AppUtils.getMeta('git.commit.id') + nl +
                    "Class: "   + (root?.class?.name ?: exception.class.name) + nl

            if (exception.message) {
                result.mailString += "Message: " + exception.message + nl
            }
            if (root?.message != exception.message) {
                result.mailString += "Cause: " + root.message + nl
            }
        }
        render view:'error', model: result
    }

    /**
     * Shows the unauthorised access page, mapping for code 401, 403
     */
    def forbidden() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'forbidden', model: result
    }

    /**
     * Shows the resource not found page, mapping for code 404
     */
    def notFound() {
        Map<String, Object> result = [
                status: request.getAttribute('javax.servlet.error.status_code'),
                alternatives: [:]
        ]
        GrailsClass controller = getControllerClass()

        if (controller) {
            if (request.getAttribute('javax.servlet.error.message') == Check404.KEY) {
                controller.clazz[ Check404.CHECK404_ALTERNATIVES ].each{ action, label ->
                    if (action.contains('/')) {
                        result.alternatives << [ (g.createLink(uri: (action.startsWith('/') ? action : '/' + action), absolute: true)) : message(code: label) ]
                    } else {
                        result.alternatives << [ (g.createLink(controller: controller.logicalPropertyName, action: action, absolute: true)) : message(code: label) ]
                    }
                }
            }
            else {
                UrlMappingData umd = (new DefaultUrlMappingParser()).parse( request.forwardURI )

                controller.clazz.declaredMethods.findAll {
                    it.getAnnotation(Action) && it.name in ['show', 'list']
                }.each {
                    if (it.name == 'show' && umd.tokens.size() == 3 && umd.tokens[2].isNumber()) {
                        result.alternatives << [ (g.createLink(controller: controller.logicalPropertyName, action: 'show', params: [id: umd.tokens[2]], absolute: true)) : '' ]
                    }
                    else {
                        result.alternatives << [ (g.createLink(controller: controller.logicalPropertyName, action: it.name, absolute: true)) : '' ]
                    }
                }
            }
        }

        render view:'notFound', model: result
    }

    /**
     * Shows the service unavailable page
     */
    def unavailable() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'unavailable', model: result
    }

    /**
     * Fallback for every other failure showing status 404
     */
    def fallback() {
        // UrlMappings -> "/**"
        log.debug 'StatusCodeController.fallback: ' + request.getRequestURL()

        Map<String, Object> result = [
                status: '404*',
                alternatives: [:]
        ]
        render view:'notFound', model: result
    }
}
