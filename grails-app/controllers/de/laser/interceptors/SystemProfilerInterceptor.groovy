package de.laser.interceptors

import de.laser.ContextService
import de.laser.custom.CustomWebSocketConfig
import de.laser.helper.Profiler
import de.laser.cache.SessionCacheWrapper
import de.laser.system.SystemActivityProfiler

class SystemProfilerInterceptor implements grails.artefact.Interceptor {

    ContextService contextService

    SystemProfilerInterceptor() {
        matchAll().excludes(controller:  ~/(ajax|ajaxHtml|ajaxJson|ajaxOpen)/)
                  .excludes(uri: CustomWebSocketConfig.WS_STOMP + '/**') // websockets

    }

    boolean before() {
        SessionCacheWrapper cache = contextService.getSessionCache()
        Profiler debugUtil = (Profiler) cache.get(Profiler.SESSION_SYSTEMPROFILER)
        if (debugUtil) {
            debugUtil.startSimpleBench( Profiler.generateKey( getWebRequest() ) )
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
