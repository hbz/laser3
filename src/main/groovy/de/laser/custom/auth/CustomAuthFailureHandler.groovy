package de.laser.custom.auth

import de.laser.SystemService
import de.laser.auth.User
import de.laser.system.SystemEvent
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    @Transactional
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        log.debug( '+ Login failed ..... [' + SwissKnife.getRemoteHash(request) + ']' )

        if (exception instanceof BadCredentialsException) {
            try {
                String uname = request.getParameter('username')
                User user = User.findByUsername(uname)

                user.invalidLoginAttempts = (user.invalidLoginAttempts ?: 0 ) + 1
                if (user.invalidLoginAttempts >= SystemService.UA_FLAG_LOCKED_AFTER_INVALID_ATTEMPTS) {
                    user.accountLocked = true

                    SystemEvent.createEvent('SYSTEM_UA_FLAG_LOCKED', [locked: [usr.id, usr.username, usr.invalidLoginAttempts]])
                }
                user.save()
            }
            catch(Exception e) {}
        }

        super.onAuthenticationFailure(request, response, exception)
    }
}