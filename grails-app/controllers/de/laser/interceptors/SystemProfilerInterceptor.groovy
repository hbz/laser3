package de.laser.interceptors

import de.laser.ContextService
//import de.laser.custom.CustomWebSocketMessageBrokerConfig
import de.laser.helper.Profiler
import de.laser.cache.SessionCacheWrapper
import de.laser.system.SystemActivityProfiler

/**
 * This interceptor class handles the system profiling calls
 */
class SystemProfilerInterceptor implements grails.artefact.Interceptor {

    ContextService contextService

    /**
     * defines which controller calls should be caught up, in this case every controller except the AJAX controllers
     */
    SystemProfilerInterceptor() {
        matchAll().excludes(controller:  ~/(ajax|ajaxHtml|ajaxJson|ajaxOpen)/)
//                  .excludes(uri: CustomWebSocketMessageBrokerConfig.WS_STOMP + '/**') // websockets
    }

    /**
     * In case a system profiler has been started, benchmarking is being started for the given request
     * @return true
     */
    boolean before() {
        SessionCacheWrapper cache = contextService.getSessionCache()
        Profiler debugUtil = (Profiler) cache.get(Profiler.SESSION_SYSTEMPROFILER)
        if (debugUtil) {
            debugUtil.startSimpleBench( Profiler.generateKey( getWebRequest() ) )
        }
        true
    }

    /**
     * Call to add the context user to the list of active users
     * @return true
     * @see SystemActivityProfiler#addActiveUser(de.laser.auth.User)
     */
    boolean after() {
        // triggered via AjaxOpenController.profiler()
        // println request.requestURL

        if (contextService.getUser()) {
            SystemActivityProfiler.addActiveUser(contextService.getUser())
        }
        true
    }
}
