package de.laser.controller


import de.laser.helper.DebugUtil
import de.laser.helper.SessionCacheWrapper

abstract class AbstractDebugController {

    def contextService

    def beforeInterceptor = {

        // TODO: grails-upgrade: http://docs.grails.org/latest/guide/theWebLayer.html#interceptors

        SessionCacheWrapper cache = contextService.getSessionCache()
        DebugUtil debugUtil = cache.get(DebugUtil.SYSPROFILER_SESSION)
        if (debugUtil) {
            debugUtil.startSimpleBench(actionUri)
        }
    }


    def afterInterceptor = {

        // TODO: grails-upgrade: http://docs.grails.org/latest/guide/theWebLayer.html#interceptors

        //DebugUtil debugUtil = debugService.getDebugUtilAsSingleton()

        //long delta = debugUtil.stopSimpleBench(session.id + '@' + actionUri)
        //debugUtil.updateSystemProfiler(delta, actionUri)

        // triggerd via AjaxController.notifyProfiler()
    }
}
