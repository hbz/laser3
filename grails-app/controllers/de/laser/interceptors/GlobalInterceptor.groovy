package de.laser.interceptors

import de.laser.annotations.Check404
//import de.laser.custom.CustomWebSocketMessageBrokerConfig
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import grails.core.GrailsControllerClass
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus

import java.lang.annotation.Annotation
import java.lang.reflect.Method

/**
 * This interceptor class handles general checks before any controller call
 */
@Slf4j
class GlobalInterceptor implements grails.artefact.Interceptor {

    /**
     * defines which controller calls should be caught up, in this case every controller
     */
    GlobalInterceptor() {
        matchAll()
//                .excludes(uri: CustomWebSocketMessageBrokerConfig.WS_STOMP + '/**') // websockets
    }

    /**
     * Performs global checks in order to determine whether the call is valid or not
     * @return true if the request is valid, false otherwise
     */
    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Expires", "0")

        _handleGlobalUID(params)
        _handleWekbID(params)
        _handleDebugMode(params)

        _handleCheck404(params) // true | false
    }

    /**
     * Dummy method stub implementing abstract methods
     * @return true
     */
    boolean after() {
        true
    }

    /**
     * Checks if - in case an ID has been submitted - it is a global UID and the requested global UID points to a valid object
     * @param params the request parameter map; if found, the requested object is being passed into the parameter map
     */
    private void _handleGlobalUID(GrailsParameterMap params) {

        if (params.id && params.id.contains(':')) {
            try {
                String objName  = params.id.split(':')[0]
                Class dc = CodeUtils.getAllDomainClasses().find {it.simpleName == objName.capitalize() }

                if (!dc) {
                    // TODO - remove fallback - db cleanup, e.g. issueentitlement -> issueEntitlement
                    dc = CodeUtils.getAllDomainClasses().find {it.simpleName.equalsIgnoreCase( objName ) }
                }
                if (dc) {
                    def match = dc.findByGlobalUID(params.id)

                    if (match) {
                        log.debug("requested by globalUID: [ ${params.id} ] > ${dc} # ${match.id}")
                        params.id = match.getId()
                    }
                    else {
                        params.id = 0
                    }
                }
            }
            catch (Exception e) {
                params.id = 0
            }
        }
    }

    /**
     * Checks if - in case an ID has been submitted - it is a we:kb ID and the requested we:kb ID points to a valid object
     * @param params the request parameter map; if found, the requested object is being passed into the parameter map
     */
    private void _handleWekbID(GrailsParameterMap params) {

        if (params.id && params.id ==~ /[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}/) {
            try {
                String objName = getControllerClass()?.name

                switch (objName) {
                    case 'Organisation':
                        objName = 'Org'; break
                    case 'Tipp':
                        objName = 'TitleInstancePackagePlatform'; break
                }

                Class dc = CodeUtils.getAllDomainClasses().find {it.simpleName == objName }

                if (!dc) {
                    // TODO - remove fallback - db cleanup, e.g. issueentitlement -> issueEntitlement
                    dc = CodeUtils.getAllDomainClasses().find {it.simpleName.equalsIgnoreCase( objName ) }
                }
                if (dc) {
                    def match = dc.findByGokbId(params.id)

                    if (match) {
                        log.debug("requested by wekbID: [ ${params.id} ] > ${dc} # ${match.id}")
                        params.id = match.getId()
                    }
                    else {
                        params.id = 0
                    }
                }
            }
            catch (Exception e) {
                params.id = 0
            }
        }
    }

    /**
     * Sets the debug mode flag
     * @param params the request parameter map in which also the result is being set
     */
    private void _handleDebugMode(GrailsParameterMap params) {
        if (params.debug) {
            AppUtils.setDebugMode(params.debug)
        }
    }

    /**
     * Checks if - in case an ID has been submitted - it is a database ID and if an object may be retrieved with it
     * @param params the request parameter map; if found, the requested object is being passed into the parameter map
     */
    private boolean _handleCheck404(GrailsParameterMap params) {

        if (params.containsKey('id')) {
            GrailsControllerClass controller = getControllerClass()

            if (controller && !controller.name.startsWith('Ajax')) {
                Method cm = controller.clazz.declaredMethods.find { it.getName() == getActionName() && it.getAnnotation(Check404) }
                if (cm) {
                    Annotation cfa = cm.getAnnotation(Check404)
                    Class cls = (cfa.domain() != NullPointerException) ? cfa.domain(): CodeUtils.getDomainClassBySimpleName(controller.name)

                    if (cls && ! cls.get(params.id)) {
                        log.warn 'check404: ' + controller.name + '.' + getActionName() + ' #' + params.id + ' --> ' + cls

                        response.sendError(HttpStatus.SC_NOT_FOUND, Check404.KEY)
                        return false
                    }
                }
            }
        }
        true
    }
}
