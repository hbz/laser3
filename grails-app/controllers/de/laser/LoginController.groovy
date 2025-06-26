package de.laser

import de.laser.auth.User
import de.laser.system.SystemEvent
import de.laser.utils.PasswordUtils
import de.laser.utils.SwissKnife
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

/**
 * The controller manages authentication handling
 */
@Secured(['permitAll'])
class LoginController {

    def authenticationTrustResolver

    GrailsApplication grailsApplication
    MailSendService mailSendService
    SpringSecurityService springSecurityService
    UserService userService

    /**
    * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
    */
    def index = {
        if (springSecurityService.isLoggedIn()) {
            redirect( uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl )
        }
        else {
            redirect( uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl, params: params )
        }
    }

    /**
    * Show the login page.
    */
    def auth = {
        ConfigObject config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            log.debug '+ Already logged in'
            forward( uri: config.successHandler.defaultTargetUrl )
            return
        }

        DefaultSavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response) as DefaultSavedRequest
        if (savedRequest) {

            String rh = SwissKnife.getRemoteHash(request)

            boolean fuzzyCheck = SwissKnife.fuzzyCheck(savedRequest)
            if (!fuzzyCheck) {
                String url = savedRequest.getRequestURL() + (savedRequest.getQueryString() ? '?' + savedRequest.getQueryString() : '')
                log.warn '+ Login attempt / Invalid url / Noted in system events ..... [' + rh + '] -> ' + url

                SystemEvent.createEvent('LOGIN_WARNING', [
                        url: url,
                        remote: request.getRemoteAddr(),
                        hash: rh,
                        headers: savedRequest.getHeaderNames().findAll{
                          it in ['host', 'referer', 'cookie', 'user-agent']
                        }.collect{it + ': ' + savedRequest.getHeaderValues( it ).join(', ')}
                ])
            }
            else {
                log.debug '+ Login attempt / Saved original request  ..... [' + rh + '] -> ' + savedRequest.getRequestURL()
            }
        }
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"

        render( view: 'auth', model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter] )
    }

    /**
    * Show denied page.
    */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
            authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect( action: 'full', params: params )
        }
    }

    /**
    * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
    */
    def full = {
        ConfigObject config = SpringSecurityUtils.securityConfig
        render(
                view: 'auth', params: params,
                model: [
                    hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                    postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"
                ]
        )
    }

    /**
    * Callback after a failed login. Redirects to the auth page with a warning message.
    */
    def authfail = {
    //def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
    //def username = session[SpringSecurityUtils.SPRING_SECURITY_LAST_USERNAME_KEY]
        String msg = ''
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]

        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = g.message(code: 'springSecurity.errors.login.expired')
            }
            else if (exception instanceof CredentialsExpiredException) {
                msg = g.message(code: 'springSecurity.errors.login.passwordExpired')
            }
            else if (exception instanceof LockedException) {
                msg = g.message(code: 'springSecurity.errors.login.locked') + ' ' + g.message(code: 'springSecurity.errors.login.locked.duration')
            }
            else if (exception instanceof DisabledException) {
                msg = g.message(code: 'springSecurity.errors.login.disabled')
            }
            else {
                msg = g.message(code: 'springSecurity.errors.login.fail')
            }
        }
        log.warn '+ Login failed ..... [' + SwissKnife.getRemoteHash(request) + '] -> ' + msg

        if (springSecurityService.isAjax(request)) {
            render( [error: msg] as JSON )
        }
        else {
            flash.error = msg
            params.remove('controller')
            params.remove('action')

            redirect( uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl, params: params )
        }
    }

    /**
     * The redirect action for Ajax requests.
     */
    def ajaxAuth = {
        response.setHeader( 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl )
        response.sendError( HttpStatus.SC_UNAUTHORIZED )

        render( status: HttpStatus.SC_UNAUTHORIZED, text: HttpStatus.SC_UNAUTHORIZED )
    }

    /**
    * The Ajax success redirect url.
    */
    def ajaxSuccess = {
        render( [success: true, username: springSecurityService.authentication.name] as JSON )
    }

    /**
    * The Ajax denied redirect url.
    */
    def ajaxDenied = {
        render( [error: 'access denied'] as JSON )
    }

    /**
    * Resets for a given username the password. Has to be corrected later.
    */
    @Transactional
    def resetForgottenPassword() {
        if(!params.forgotten_username) {
            flash.error = g.message(code:'menu.user.forgottenPassword.userMissing') as String
        }
        else {
            User user = userService.getUserByUsername(params.forgotten_username)
            if (user) {
                String newPassword = PasswordUtils.getRandomUserPassword()
                user.password = newPassword
                if (user.save()) {
                    flash.message = message(code: 'user.newPassword.successNoOutput') as String
                    mailSendService.sendMailToUser(user, message(code: 'email.subject.forgottenPassword'), '/mailTemplates/text/newPassword', [user: user, newPass: newPassword])
                }
            }
            else flash.error = g.message(code:'menu.user.forgottenPassword.userError') as String
        }
        redirect( uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl )
    }

    /**
     * Triggers the sending of the forgotten username to the given mail address
     * @return redirects to the login page
     */
    def getForgottenUsername() {
        if(!params.forgotten_username_mail) {
            flash.error = g.message(code:'menu.user.forgottenUsername.userMissing') as String
        }
        else {
            List<User> users = userService.getAllUsersByEmail(params.forgotten_username_mail)
            if (users.size() > 0) {
                flash.message = message(code: 'menu.user.forgottenUsername.success') as String
                users.each { User user ->
                    mailSendService.sendMailToUser(user, message(code: 'email.subject.forgottenUsername'), '/mailTemplates/text/forgtUsname', [user: user])
                }
            }
            else flash.error = g.message(code:'menu.user.forgottenUsername.userError') as String
        }
        redirect( uri: SpringSecurityUtils.securityConfig.auth.loginFormUrl )
    }
}
