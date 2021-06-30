/* Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.laser.custom

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.RequestCache

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
class CustomAjaxAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	protected RequestCache requestCache

	/** Dependency injection for the Ajax success url, e.g. '/login/ajaxSuccess'. */
	String ajaxSuccessUrl

	@Override
	void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {

		boolean ajax = SpringSecurityUtils.isAjax(request)
		boolean grmc = request.getCookies().find{ it.name == 'grails_remember_me' }

		// GPSPRINGSECURITYCORE-240
		if (ajax && !grmc) {
			requestCache.removeRequest request, response
		}

		try {
			if (ajax && !grmc) {
				clearAuthenticationAttributes request
				if (logger.debugEnabled) {
					logger.debug 'Redirecting to Ajax Success Url: ' + ajaxSuccessUrl
				}
				redirectStrategy.sendRedirect request, response, ajaxSuccessUrl
			}
			else {
				super.onAuthenticationSuccess request, response, authentication
			}
		}
		finally {
			// always remove the saved request
			requestCache.removeRequest request, response
		}
	}

	@Override
	void setRequestCache(RequestCache cache) {
		super.setRequestCache cache
		requestCache = cache
	}
}
