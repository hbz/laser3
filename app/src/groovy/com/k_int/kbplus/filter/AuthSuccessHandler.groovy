package de.laser.helper

import com.k_int.kbplus.auth.User
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.util.Holders
import org.springframework.security.core.Authentication
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthSuccessHandler extends AjaxAwareAuthenticationSuccessHandler {

    def springSecurityService = Holders.grailsApplication.mainContext.getBean('springSecurityService')
    def userService = Holders.grailsApplication.mainContext.getBean('userService')

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws ServletException, IOException {

        User user = springSecurityService.getCurrentUser()
        userService.initMandatorySettings(user)

        super.onAuthenticationSuccess(request, response, authentication)
    }

}