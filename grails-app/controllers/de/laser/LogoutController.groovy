package de.laser

import de.laser.system.SystemActivityProfiler
import de.laser.utils.SwissKnife
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller is responsible for session destructing upon logout
 */
@Secured(['IS_AUTHENTICATED_FULLY', 'IS_AUTHENTICATED_REMEMBERED'])
class LogoutController {

	ContextService contextService

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		log.info( '+ Logout ..... [' + SwissKnife.getRemoteHash(request) + '] -> ' + request.session.id )

		// any pre-logout code here
		SystemActivityProfiler.removeActiveUser(contextService.getUser())

		redirect( uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl ) // '/j_spring_security_logout'
	}
}
