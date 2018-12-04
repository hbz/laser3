package de.laser.controller

import de.laser.domain.SystemBench
import de.laser.helper.DebugUtil
import grails.converters.JSON

abstract class AbstractDebugController {

    protected DebugUtil debugUtil = new DebugUtil()

    def beforeInterceptor = {
        debugUtil.startBench(this.class.simpleName + '_' + session.id)
    }

    def afterInterceptor = {
        def delta = debugUtil.stopBench(this.class.simpleName + '_' + session.id)

        if (delta >= SystemBench.THRESHOLD_MS) {
            (new SystemBench(
                    uri:    actionUri,
                    params: params as JSON,
                    ms:     delta,
                    context: contextService.getOrg()
            )).save()
        }
    }
}
