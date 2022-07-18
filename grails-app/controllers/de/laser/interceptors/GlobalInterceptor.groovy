package de.laser.interceptors

import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import grails.core.GrailsClass

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
                GrailsClass obj = CodeUtils.getAllDomainArtefacts().find {it.name == objName.capitalize() }
                if (!obj) {
                    // TODO - remove fallback - db cleanup, e.g. issueentitlement -> issueEntitlement
                    obj = CodeUtils.getAllDomainArtefacts().find {it.name.equalsIgnoreCase( objName ) }
                }

                if (obj) {
                    def objClass = Class.forName( obj.getClazz().getName() )
                    def match    = objClass.findByGlobalUID(params.id)

                    if (match) {
                        log.debug("requested by globalUID: [ ${params.id} ] > ${objClass} # ${match.id}")
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
