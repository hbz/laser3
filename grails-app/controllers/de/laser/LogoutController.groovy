package de.laser

import de.laser.system.SystemActivityProfiler
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

// 2.0

/**
 * This controller is responsible for session destructing upon logout
 */
@Secured(['permitAll'])
class LogoutController {

	ContextService contextService

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// any pre-logout code here
		SystemActivityProfiler.removeActiveUser(contextService.getUser())

		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
