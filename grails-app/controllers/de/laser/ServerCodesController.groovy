package de.laser

import de.laser.helper.AppUtils
import de.laser.helper.ConfigMapper
import de.laser.helper.DateUtils
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import grails.web.Action
import grails.web.mapping.UrlMappingData
import org.grails.core.DefaultGrailsControllerClass
import org.grails.exceptions.ExceptionUtils
import org.grails.web.mapping.DefaultUrlMappingParser

/**
 * This controller handles the server code mapping output
 */
@Secured(['permitAll'])
class ServerCodesController {

    GrailsApplication grailsApplication

    /**
     * Shows the error page with stack trace extracts; acts on codes 403, 405 and 500
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
                    "Zeitpunkt: " + DateUtils.getSDF_NoZ().format( new Date() ) + nl +
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
     * Shows the unauthorised access page, mapping for code 401
     */
    def forbidden() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'forbidden', model: result
    }

    /**
     * Shows the resource not found page, mapping for code 404
     */
    def notFound() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]

        UrlMappingData umd = (new DefaultUrlMappingParser()).parse( request.forwardURI )
        DefaultGrailsControllerClass controller = grailsApplication.controllerClasses.find { it.logicalPropertyName == umd.tokens[0] }
        if (controller) {
            result.alternatives = controller.clazz.declaredMethods.findAll{
                it.getAnnotation(Action) && it.name in ['index', 'list', 'show']
            }.collect{
                if (it.name == 'show' && umd.tokens.size() == 3 && umd.tokens[2].isNumber()) {
                    "${g.createLink(controller: controller.logicalPropertyName, action: it.name, params: [id: umd.tokens[2]], absolute: true)}"
                } else {
                    "${g.createLink(controller: controller.logicalPropertyName, action: it.name, absolute: true)}"
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
}
