package de.laser.interceptors

import de.laser.ContextService
import de.laser.helper.ProfilerUtils
import de.laser.cache.SessionCacheWrapper
import de.laser.system.SystemActivityProfiler

class SystemProfilerInterceptor implements grails.artefact.Interceptor {

    ContextService contextService

    SystemProfilerInterceptor() {
        matchAll().excludes(controller:  ~/(ajax|ajaxHtml|ajaxJson)/)
                  .excludes(uri: '/stomp/**') // websockets
                  .excludes(uri: '/topic/**') // websockets

    }

    boolean before() {
        SessionCacheWrapper cache = contextService.getSessionCache()
        ProfilerUtils debugUtil = (ProfilerUtils) cache.get(ProfilerUtils.SESSION_SYSTEMPROFILER)
        if (debugUtil) {
            debugUtil.startSimpleBench( ProfilerUtils.generateKey( getWebRequest() ) )
        }

        true
    }

    boolean after() {
        // triggered via AjaxOpenController.profiler()
        // println request.requestURL

        if (contextService.getUser()) {
            SystemActivityProfiler.addActiveUser(contextService.getUser())
        }

        true
    }
}
