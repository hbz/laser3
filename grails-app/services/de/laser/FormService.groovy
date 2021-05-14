package de.laser

import de.laser.helper.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

//@CompileStatic
@Transactional
class FormService {

    def contextService

    final static String FORM_SERVICE_TOKEN = 'FORM_SERVICE_TOKEN'

    String getNewToken() {
        return "${UUID.randomUUID()}:${System.currentTimeMillis()}".encodeAsMD5()
    }

    boolean validateToken(GrailsParameterMap params) {
        if (contextService.getUser()) {
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
        String mailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})/
        return ( email ==~ mailPattern )
    }

    boolean validatePhoneNumber(String phoneNumber) {
        String phonePattern = /(\+|\(\d+\))?[ -\/]?(\d+[ -\/]?)+/
        return ( phoneNumber ==~ phonePattern )
    }
}