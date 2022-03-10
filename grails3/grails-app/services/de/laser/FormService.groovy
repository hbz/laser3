package de.laser

import de.laser.helper.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This service holds generic methods releated to form handling
 */
//@CompileStatic
@Transactional
class FormService {

    def contextService

    final static String FORM_SERVICE_TOKEN = 'FORM_SERVICE_TOKEN'

    /**
     * Generates a new individual token to ensure unique form submission
     * @return a randomised unique token as MD5 hash
     */
    String getNewToken() {
        return "${UUID.randomUUID()}:${System.currentTimeMillis()}".encodeAsMD5()
    }

    /**
     * Checks if the form token has been submitted only one time
     * @param params the request parameter map containing the token
     * @return true iff the token has been submitted only one time (i.e. there is no cache call match), false otherwise
     */
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

    /**
     * Validates the given string against the email regular expression
     * @param email the mail address to check
     * @return true if the given input is a valid email address, false otherwise
     */
    boolean validateEmailAddress(String email) {
        String mailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})/
        return ( email ==~ mailPattern )
    }

    /**
     * Validates the given string against the telephone number regular expression
     * @param phoneNumber the phone number to check
     * @return true if the given input is a valid telephone number, false otherwise
     */
    boolean validatePhoneNumber(String phoneNumber) {
        String phonePattern = /(\+|\(\d+\))?[ -\/]?(\d+[ -\/]?)+/
        return ( phoneNumber ==~ phonePattern )
    }
}