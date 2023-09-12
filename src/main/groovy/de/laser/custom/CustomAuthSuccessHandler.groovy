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

/**
 * Implementation of request success handler
 */
class CustomAuthSuccessHandler extends CustomAjaxAwareAuthenticationSuccessHandler {

    SpringSecurityService springSecurityService
    UserService userService
    ContextService contextService

    /**
     * Handler after a successful authentication; setting the mandatory settings if the user logs in for the
     * first time.
     * See the super class(es) for the documentation of the arguments
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws ServletException
     * @throws IOException
     */
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