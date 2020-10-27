package de.laser

import de.laser.helper.ConfigUtils
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured
import org.grails.exceptions.ExceptionUtils

@Secured(['permitAll'])
class ServerCodesController {

    GrailsApplication grailsApplication

    def error() {

        Map<String, Object> result = [
                exception: request.getAttribute('exception'),
                mailString: ''
        ]

        if (result.exception) {

            Throwable exception = (Throwable) result.exception
            Throwable root = ExceptionUtils.getRootCause(exception)

            String nl = " %0D%0A"

            result.mailString =
                    "mailto:laser@hbz-nrw.de?subject=Fehlerbericht - " + ConfigUtils.getLaserSystemId() +
                    "&body=Ihre Fehlerbeschreibung (bitte angeben): " + nl + nl +
                    "URI: " + request.forwardURI + nl +
                    "Zeitpunkt: " + new Date().format( 'dd.MM.yyyy HH:mm:ss' ) + nl +
                    "System: " + ConfigUtils.getLaserSystemId() + nl +
                    "Branch: " + grailsApplication.metadata['repository.branch'] + nl +
                    "Commit: " + grailsApplication.metadata['repository.revision.number'] + nl +
                    "Class: " + (root?.class?.name ?: exception.class.name) + nl

            if (exception.message) {
                result.mailString += "Message: " + exception.message + nl
            }
            if (root?.message != exception.message) {
                result.mailString += "Cause: " + root.message + nl
            }
        }

        result
    }

    def forbidden() {
        Map<String, Object> result = [:]
        result
    }

    def notFound() {
        Map<String, Object> result = [:]
        result
    }

    def unavailable() {
        Map<String, Object> result = [:]
        result
    }
}
