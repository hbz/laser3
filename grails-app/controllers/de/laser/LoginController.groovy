package de.laser

import de.laser.auth.User
import de.laser.storage.BeanStore
import de.laser.system.SystemEvent
import de.laser.utils.CodeUtils
import de.laser.utils.PasswordUtils
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.web.mapping.UrlMappingInfo
import grails.web.mapping.UrlMappingsHolder
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
    SpringSecurityService springSecurityService
    MailSendService mailSendService

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
        log.debug 'Attempting login >>> ' + request.getRemoteAddr() + ' - ' + request.session.id

        ConfigObject config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            log.debug 'Already logged in'
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        DefaultSavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response) as DefaultSavedRequest
        if (savedRequest) {
            log.debug 'Saved original request >>> ' + savedRequest.getRequestURL()

            boolean fuzzyCheck = _fuzzyCheck(savedRequest)
            if (!fuzzyCheck) {
                String url = savedRequest.getRequestURL() + (savedRequest.getQueryString() ? '?' + savedRequest.getQueryString() : '')
                log.warn 'Login failed; invalid url; noted in system events >>> ' + request.getRemoteAddr() + ' - ' + url

                SystemEvent.createEvent('LOGIN_WARNING', [
                        url: url,
                        remote: request.getRemoteAddr(),
                        headers: savedRequest.getHeaderNames().findAll{
                          it in ['host', 'referer', 'cookie', 'user-agent']
                        }.collect{it + ': ' + savedRequest.getHeaderValues( it ).join(', ')}
                ])
            }
        }
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"

        render view: 'auth', model: [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter]
    }

  /**
   * The redirect action for Ajax requests.
   */
  def authAjax = {
    response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
    response.sendError HttpStatus.SC_UNAUTHORIZED

    render status: HttpStatus.SC_UNAUTHORIZED, text: HttpStatus.SC_UNAUTHORIZED
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
    //def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
    //def username = session[SpringSecurityUtils.SPRING_SECURITY_LAST_USERNAME_KEY]

    String msg = ''
    def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
    if (exception) {
      if (exception instanceof AccountExpiredException) {
        msg = g.message(code: "springSecurity.errors.login.expired")
      }
      else if (exception instanceof CredentialsExpiredException) {
        msg = g.message(code: "springSecurity.errors.login.passwordExpired")
      }
      else if (exception instanceof LockedException) {
        msg = g.message(code: "springSecurity.errors.login.locked")
      }
      else if (exception instanceof DisabledException) {
          msg = g.message(code: "springSecurity.errors.login.disabled")
      }
      else {
        msg = g.message(code: "springSecurity.errors.login.fail")
      }
    }
    log.warn 'Login failed >>> ' + request.getRemoteAddr() + ' - ' + msg

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
  @Transactional
  def resetForgottenPassword() {
    if(!params.forgotten_username) {
      flash.error = g.message(code:'menu.user.forgottenPassword.userMissing') as String
    }
    else {
      User user = User.findByUsername(params.forgotten_username)
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
    redirect action: 'auth'
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
            List<User> users = User.findAllByEmail(params.forgotten_username_mail)
            if (users.size() > 0) {
                flash.message = message(code: 'menu.user.forgottenUsername.success') as String
                users.each {User user ->
                    mailSendService.sendMailToUser(user, message(code: 'email.subject.forgottenUsername'), '/mailTemplates/text/forgtUsname', [user: user])
                }
            }
            else flash.error = g.message(code:'menu.user.forgottenUsername.userError') as String
        }
        redirect action: 'auth'
  }

    /**
     * Checks if the last requested page before a login warning aims to a valid page
     * @param savedRequest the request to check
     * @return true if the saved request call was valid, false otherwise
     */
    private boolean _fuzzyCheck(DefaultSavedRequest savedRequest) {

        if (!savedRequest) {
            return true
        }
        boolean valid = false

        UrlMappingsHolder urlMappingsHolder = BeanStore.getUrlMappingsHolder()
        UrlMappingInfo[] matchedMappingInfo = urlMappingsHolder.matchAll(savedRequest.getRequestURI())

        if (matchedMappingInfo.length > 0) {
            UrlMappingInfo mappingInfo = matchedMappingInfo.first()
            GrailsClass controller = mappingInfo.hasProperty('controllerClass') ? mappingInfo.controllerClass :
                    CodeUtils.getAllControllerArtefacts().find {
                        it.clazz.simpleName == mappingInfo.controllerName.capitalize() + 'Controller'
                    }

            if (controller && controller.name != 'ServerCodes') {
                boolean match = mappingInfo.hasProperty('info') ? controller.actionUriToViewName.find { it.key == mappingInfo.info.params.action } : false
                if (match) {
                    valid = true
                }
            }
        }
        valid
    }
}
