//package laser2; grails-upgrade: do not remove

import com.k_int.kbplus.auth.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.SavedRequest

import javax.servlet.http.HttpServletResponse

//import org.springframework.security.web.authentication.AbstractProcessingFilter

@Secured('permitAll')
class LoginController {

  GrailsApplication grailsApplication

  def authenticationTrustResolver
  def springSecurityService
  def instAdmService

  /**
   * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
   */
  def index = {
    if (springSecurityService.isLoggedIn()) {
      redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
    }
    else {
      redirect action: 'auth', params: params
    }
  }

  /**
   * Show the login page.
   */
  def auth = {
    log.debug("auth session:${request.session.id}")

    ConfigObject config = SpringSecurityUtils.securityConfig

    if (springSecurityService.isLoggedIn()) {
      log.debug("already logged in");
      redirect uri: config.successHandler.defaultTargetUrl
      return
    }
    else {
      log.debug("Attempting login");
    }

    SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
    // String requestUrl = savedRequest?.getRequestURL();
    log.debug("auth action - the original ua request was for...");

    String requestUrl = savedRequest?.getRedirectUrl();
    String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
    render view: 'auth', model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter]
  }

  /**
   * The redirect action for Ajax requests.
   */
  def authAjax = {
    response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
    response.sendError HttpServletResponse.SC_UNAUTHORIZED
  }

  /**
   * Show denied page.
   */
  def denied = {
    if (springSecurityService.isLoggedIn() &&
        authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
      // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
      redirect action: 'full', params: params
    }
  }

  /**
   * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
   */
  def full = {
    ConfigObject config = SpringSecurityUtils.securityConfig
    render view: 'auth', params: params,
      model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
              postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
  }

  /**
   * Callback after a failed login. Redirects to the auth page with a warning message.
   */
  def authfail = {

    def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
    String msg = ''
    def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
    if (exception) {
      if (exception instanceof AccountExpiredException) {
        msg = g.message(code: "springSecurity.errors.login.expired")
      }
      else if (exception instanceof CredentialsExpiredException) {
        msg = g.message(code: "springSecurity.errors.login.passwordExpired")
      }
      else if (exception instanceof DisabledException) {
        msg = g.message(code: "springSecurity.errors.login.disabled")
      }
      else if (exception instanceof LockedException) {
        msg = g.message(code: "springSecurity.errors.login.locked")
      }
      else {
        msg = g.message(code: "springSecurity.errors.login.fail")
      }
    }

    if (springSecurityService.isAjax(request)) {
      render([error: msg] as JSON)
    }
    else {
      flash.error = msg
      redirect action: 'auth', params: params
    }
  }

  /**
   * The Ajax success redirect url.
   */
  def ajaxSuccess = {
    render([success: true, username: springSecurityService.authentication.name] as JSON)
  }

  /**
   * The Ajax denied redirect url.
   */
  def ajaxDenied = {
    render([error: 'access denied'] as JSON)
  }

  /**
   * Resets for a given username the password. Has to be corrected later.
   */
  def resetForgottenPassword() {
    if(!params.forgotten_username) {
      flash.error = g.message(code:'menu.user.forgottenPassword.userMissing')
    }
    else {
      User user = User.findByUsername(params.forgotten_username)
      if (user) {
        String newPassword = User.generateRandomPassword()
        user.password = newPassword
        if (user.save(flush: true)) {
          flash.message = message(code: 'user.newPassword.successNoOutput')

          instAdmService.sendMail(user, 'Passwortänderung', '/mailTemplates/text/newPassword', [user: user, newPass: newPassword])
        }
      }
      else flash.error = g.message(code:'menu.user.forgottenPassword.userError')
    }
    redirect action: 'auth'

  }
}
