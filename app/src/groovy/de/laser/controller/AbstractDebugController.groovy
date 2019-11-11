package de.laser.controller

import de.laser.helper.DebugUtil

abstract class AbstractDebugController {

    def debugService

    def beforeInterceptor = {
        DebugUtil debugUtil = debugService.getDebugUtilAsSingleton()

        debugUtil.startSimpleBench(session.id + '#' + actionUri)
    }


    def afterInterceptor = {
        //DebugUtil debugUtil = debugService.getDebugUtilAsSingleton()

        //long delta = debugUtil.stopSimpleBench(session.id + '@' + actionUri)
        //debugUtil.updateSystemProfiler(delta, actionUri)

        // triggerd via AjaxController.notifyProfiler()
    }
}
