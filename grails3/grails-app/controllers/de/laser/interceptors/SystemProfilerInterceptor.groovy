package de.laser.interceptors

import de.laser.helper.ProfilerUtils
import de.laser.helper.SessionCacheWrapper

class SystemProfilerInterceptor implements grails.artefact.Interceptor {

    def contextService

    SystemProfilerInterceptor() {
        matchAll().excludes(controller: 'ajax')
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
        // triggered via AjaxController.notifyProfiler()

        true
    }
}
