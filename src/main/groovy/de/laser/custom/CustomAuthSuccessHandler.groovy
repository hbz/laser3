package de.laser.custom

import de.laser.auth.User
import de.laser.helper.ProfilerUtils
import de.laser.helper.SessionCacheWrapper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.springframework.security.core.Authentication

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthSuccessHandler extends CustomAjaxAwareAuthenticationSuccessHandler {

    def springSecurityService = Holders.grailsApplication.mainContext.getBean('springSecurityService')
    def userService = Holders.grailsApplication.mainContext.getBean('userService')
    def contextService = Holders.grailsApplication.mainContext.getBean('contextService')

    @Override
    @Transactional
    void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws ServletException, IOException {

        if (! SpringSecurityUtils.isAjax(request)) {
            User user = springSecurityService.getCurrentUser()
            userService.initMandatorySettings(user)
        }

        SessionCacheWrapper cache = contextService.getSessionCache()
        cache.put(ProfilerUtils.SYSPROFILER_SESSION, new ProfilerUtils(ProfilerUtils.SYSPROFILER_SESSION))

        super.onAuthenticationSuccess(request, response, authentication)
    }
}