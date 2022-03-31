package de.laser

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.session.SessionRegistryImpl

// 2.0

/**
 * This controller is responsible for session destructing upon logout
 */
@Secured('permitAll')
class LogoutController {

	SessionRegistryImpl sessionRegistry

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO put any pre-logout code here
		sessionRegistry.removeSessionInformation(session.getId())

		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
