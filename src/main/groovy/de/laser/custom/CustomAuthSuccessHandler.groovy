package de.laser.custom

import de.laser.ContextService
import de.laser.UserService
import de.laser.auth.User
import de.laser.helper.Profiler
import de.laser.cache.SessionCacheWrapper
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import org.springframework.security.core.Authentication

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthSuccessHandler extends CustomAjaxAwareAuthenticationSuccessHandler {

    SpringSecurityService springSecurityService
    UserService userService
    ContextService contextService

    @Override
    @Transactional
    void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws ServletException, IOException {

        if (! SpringSecurityUtils.isAjax(request)) {
            User user = springSecurityService.getCurrentUser()
            userService.initMandatorySettings(user)
        }

        SessionCacheWrapper cache = contextService.getSessionCache()
        cache.put(Profiler.SESSION_SYSTEMPROFILER, new Profiler(Profiler.SESSION_SYSTEMPROFILER))

        super.onAuthenticationSuccess(request, response, authentication)
    }
}