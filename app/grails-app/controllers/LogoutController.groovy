import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured

// 2.0

@Secured('permitAll')
class LogoutController {

	def sessionRegistry

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO put any pre-logout code here
		sessionRegistry.removeSessionInformation(session.getId())

		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
