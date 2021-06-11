package de.laser.interceptors

import grails.core.GrailsClass

class GlobalInterceptor implements grails.artefact.Interceptor {

    GlobalInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma", "no-cache")
        response.setHeader("Expires", "0")

        // --> workaround for 'org.grails.plugins:rendering:2.0.3'

        //println 'ct: ' + response.getContentType() + ' ; ce: ' + response.getCharacterEncoding() + ' ; mt: ' + response.getMimeType()

        // valid:
        // ct: null ; ce: ISO-8859-1 ; mt: MimeType { name=*/*,extension=all,parameters=[q:1.0] }
        // ct: text/html;charset=UTF-8 ; ce: UTF-8 ; mt: MimeType { name=*/*,extension=all,parameters=[q:1.0] }
        // invalid:
        // ct: null ; ce: ISO-8859-1 ; mt: MimeType { name=*/*,extension=all,parameters=[q:1.0] }
        // ct: null ; ce: ISO-8859-1 ; mt: MimeType { name=*/*,extension=all,parameters=[q:1.0] }

        //response.setHeader("Content-Type", "text/html;charset=UTF-8") // affects only invalid responses !?

        // <--

        if (params.id?.contains(':')) {
            try {
                String objName  = params.id.split(':')[0]
                GrailsClass obj = grailsApplication.getArtefacts("Domain").find {it.name == objName.capitalize() }

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

        true
    }

    boolean after() {
        // --> workaround for 'org.grails.plugins:rendering:2.0.3'
        //println 'ct: ' + response.getContentType() + ' ; ce: ' + response.getCharacterEncoding() + ' ; mt: ' + response.getMimeType()
        // <--

        true
    }
}
