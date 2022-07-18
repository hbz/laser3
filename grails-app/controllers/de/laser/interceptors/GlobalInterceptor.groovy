package de.laser.interceptors

import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import groovy.util.logging.Slf4j

@Slf4j
class GlobalInterceptor implements grails.artefact.Interceptor {

    GlobalInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Expires", "0")

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
        if (params.debug) {
            AppUtils.setDebugMode(params.debug)
        }

        true
    }

    boolean after() {
        true
    }
}
