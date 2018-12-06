package de.laser.controller

import de.laser.domain.SystemProfiler
import de.laser.helper.DebugUtil
import grails.converters.JSON

abstract class AbstractDebugController {

    protected DebugUtil debugUtil = new DebugUtil()

    def beforeInterceptor = {
        debugUtil.startBench(this.class.simpleName + '_' + session.id)
    }

    def afterInterceptor = {
        def delta = debugUtil.stopBench(this.class.simpleName + '_' + session.id)

        if (delta >= SystemProfiler.THRESHOLD_MS) {
            def json = (params as JSON)
            (new SystemProfiler(
                    uri:      actionUri,
                    params:   json?.toString(),
                    ms:       delta,
                    context:  contextService.getOrg()
            )).save()
        }
    }
}
