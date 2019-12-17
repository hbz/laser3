package de.laser.controller

import de.laser.ContextService
import de.laser.helper.DebugUtil
import de.laser.helper.EhcacheWrapper
import de.laser.helper.SessionCacheWrapper
import grails.util.Holders

abstract class AbstractDebugController {

    def debugService
    def contextService

    def beforeInterceptor = {

        SessionCacheWrapper cache = contextService.getSessionCache()
        DebugUtil debugUtil = cache.get('debugUtil')
        if (debugUtil) {
            debugUtil.startSimpleBench(actionUri)
        }
    }


    def afterInterceptor = {
        //DebugUtil debugUtil = debugService.getDebugUtilAsSingleton()

        //long delta = debugUtil.stopSimpleBench(session.id + '@' + actionUri)
        //debugUtil.updateSystemProfiler(delta, actionUri)

        // triggerd via AjaxController.notifyProfiler()
    }
}
