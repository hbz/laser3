package de.laser

import de.laser.helper.AppUtils
import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtils
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import org.grails.exceptions.ExceptionUtils

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
                    "mailto:laser@hbz-nrw.de?subject=Fehlerbericht - " + ConfigUtils.getLaserSystemId() +
                    "&body=Ihre Fehlerbeschreibung (bitte angeben): " + nl + nl +
                    "URI: "     + request.forwardURI + nl +
                    "Zeitpunkt: " + DateUtils.getSDF_NoZ().format( new Date() ) + nl +
                    "System: "  + ConfigUtils.getLaserSystemId() + nl +
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
        render view:'error' , model: result
    }

    /**
     * Shows the unauthorised access page, mapping for code 401
     */
    def forbidden() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'forbidden' , model: result
    }

    /**
     * Shows the resource not found page, mapping for code 404
     */
    def notFound() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'notFound' , model: result
    }

    /**
     * Shows the service unavailable page
     */
    def unavailable() {
        Map<String, Object> result = [status: request.getAttribute('javax.servlet.error.status_code')]
        render view:'unavailable' , model: result
    }
}
