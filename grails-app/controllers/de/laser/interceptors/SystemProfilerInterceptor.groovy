package de.laser.interceptors

import de.laser.ContextService
import de.laser.helper.ProfilerUtils
import de.laser.helper.SessionCacheWrapper
import de.laser.system.SystemActivityProfiler

class SystemProfilerInterceptor implements grails.artefact.Interceptor {

    ContextService contextService

    SystemProfilerInterceptor() {
        matchAll().excludes(controller: 'ajax')
                  .excludes(controller: 'ajaxHtml')
                  .excludes(controller: 'ajaxJson')
    }

    boolean before() {
        SessionCacheWrapper cache = contextService.getSessionCache()
        ProfilerUtils debugUtil = (ProfilerUtils) cache.get(ProfilerUtils.SYSPROFILER_SESSION)
        if (debugUtil) {
            debugUtil.startSimpleBench( ProfilerUtils.generateKey( getWebRequest() ) )
        }

        true
    }

    boolean after() {
        // triggered via AjaxOpenController.profiler()
        // triggered via AjaxOpenController.status()

        println request.requestURL

        if (contextService.getUser()) {
            SystemActivityProfiler.flagActiveUser(contextService.getUser())
        }

        true
    }
}
