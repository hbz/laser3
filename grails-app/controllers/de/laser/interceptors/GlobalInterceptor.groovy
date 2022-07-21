package de.laser.interceptors

import de.laser.annotations.CheckFor404
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import grails.core.GrailsControllerClass
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.apache.http.HttpStatus

@Slf4j
class GlobalInterceptor implements grails.artefact.Interceptor {

    GlobalInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Expires", "0")

        _handleGlobalUID(params)
        _handleDebugMode(params)

        _handle404(params) // true | false
    }

    boolean after() {
        true
    }

    private void _handleGlobalUID(GrailsParameterMap params) {

        if (params.id?.contains(':')) {
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

    private void _handleDebugMode(GrailsParameterMap params) {
        if (params.debug) {
            AppUtils.setDebugMode(params.debug)
        }
    }

    private boolean _handle404(GrailsParameterMap params) {

        if (params.containsKey('id')) {
            GrailsControllerClass controller = getControllerClass()

            if (controller && !controller.name.startsWith('Ajax')) {
                if (controller.clazz.declaredMethods.find { it.getName() == getActionName() && it.getAnnotation(CheckFor404) }) {
                    String clsName = (controller.name == 'Organisation') ? 'Org' : controller.name
                    Class cls = CodeUtils.getDomainClassBySimpleName(clsName)

                    if (cls && ! cls.get(params.id)) {
                        log.warn 'catch404: ' + controller.name + '.' + getActionName() + ' #' + params.id + ' --> ' + clsName + ' - ' + cls + ' - ' + cls?.get(params.id)

                        response.sendError(HttpStatus.SC_NOT_FOUND, CheckFor404.KEY)
                        return false
                    }
                }
            }
        }
        true
    }
}
