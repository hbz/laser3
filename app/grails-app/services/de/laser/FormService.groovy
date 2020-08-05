package de.laser

import de.laser.helper.SessionCacheWrapper
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

//@CompileStatic
@Transactional
class FormService {

    def springSecurityService
    def contextService

    final static String FORM_SERVICE_TOKEN = 'FORM_SERVICE_TOKEN'

    String getNewToken() {
        return "${UUID.randomUUID()}:${System.currentTimeMillis()}".encodeAsMD5()
    }

    boolean validateToken(GrailsParameterMap params) {
        if (springSecurityService.getCurrentUser()) {
            String token = params.get(FormService.FORM_SERVICE_TOKEN)

            if (token) {
                SessionCacheWrapper cw = contextService.getSessionCache()

                token = "FormService/Token/" + token
                String md5 = params.toString().encodeAsMD5()
                String given = cw.get(token)

                if (given && md5 == given) {
                    log.debug(token + ' : ' + md5 + ' found. Request ignored ..')
                    return false
                }
                cw.put(token, md5)
            }
            return true
        }
        return false
    }

    boolean validateEmailAddress(String email) {
        def mailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})/
        return ( email ==~ mailPattern )
    }

}