package de.laser.custom.auth

import de.laser.ContextService
import de.laser.UserService
import de.laser.auth.User
import de.laser.helper.Profiler
import de.laser.cache.SessionCacheWrapper
import de.laser.system.SystemEvent
import de.laser.utils.SwissKnife
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.springframework.security.core.Authentication

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Implementation of request success handler
 */
@Slf4j
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
     * @param authentication the {@link Authentication} object which was created during
     * the authentication process.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    @Transactional
    void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws ServletException, IOException {

        log.info( '+ Login ..... [' + SwissKnife.getRemoteHash(request) + '] -> ' + request.session.id )

        User user = springSecurityService.getCurrentUser() as User
        user.lastLogin = new Date()
        user.invalidLoginAttempts = 0

        // ERMS-6706 (TMP)
        if (! user.password.startsWith('{bcrypt}')) {
            String info1 = 'legacy password updated for user #' + user.id
            String info2 = ' (' + user.password + ')'

            user.setPassword(request.getParameter('password'))

            log.info( info1 )
            SystemEvent.createEvent('USER_INFO', [password: info1 + info2])
        }
        user.save()

        if (! SpringSecurityUtils.isAjax(request)) {
            userService.initMandatorySettings(user)
        }

        SessionCacheWrapper cache = contextService.getSessionCache()
        cache.put(Profiler.SESSION_SYSTEMPROFILER, new Profiler(Profiler.SESSION_SYSTEMPROFILER))

        super.onAuthenticationSuccess(request, response, authentication)
    }
}