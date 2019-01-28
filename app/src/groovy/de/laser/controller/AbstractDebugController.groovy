package de.laser.controller

import de.laser.domain.SystemProfiler
import de.laser.helper.DebugUtil
import grails.converters.JSON

abstract class AbstractDebugController {

    protected DebugUtil debugUtil = new DebugUtil()

    def beforeInterceptor = {
        debugUtil.startBench(this.class.simpleName + ' ' + session.id)
    }

    def afterInterceptor = {
        def delta = debugUtil.stopBench(this.class.simpleName + ' ' + session.id)

        if (delta >= SystemProfiler.THRESHOLD_MS) {
            def json = (params as JSON)
            (new SystemProfiler(
                    uri:      actionUri,
                    params:   json?.toString(),
                    ms:       delta,
                    context:  contextService?.getOrg()
            )).save(flush: true)
        }

        // added global counts
        SystemProfiler.withTransaction { status ->
            SystemProfiler global = SystemProfiler.findWhere(uri: actionUri, context: null, params: null)
            if (!global) {
                global = new SystemProfiler(uri: actionUri, ms: 0)
            }
            global.setMs(Integer.valueOf(global.ms) + 1)
            global.save(flush: true)
        }
    }
}
