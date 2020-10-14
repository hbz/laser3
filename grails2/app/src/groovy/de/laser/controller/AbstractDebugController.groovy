package de.laser.controller


import de.laser.helper.ProfilerUtils
import de.laser.helper.SessionCacheWrapper

abstract class AbstractDebugController {

    def contextService

    def beforeInterceptor = {

        SessionCacheWrapper cache = contextService.getSessionCache()
        ProfilerUtils pu = (ProfilerUtils) cache.get(ProfilerUtils.SYSPROFILER_SESSION)
        if (pu) {
            pu.startSimpleBench( ProfilerUtils.generateKey( getWebRequest() ))
        }
    }


    def afterInterceptor = {

        // triggerd via AjaxController.notifyProfiler()
    }
}
