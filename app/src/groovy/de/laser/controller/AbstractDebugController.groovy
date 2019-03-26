package de.laser.controller

import de.laser.domain.SystemProfiler
import de.laser.helper.DebugUtil
import grails.converters.JSON
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile

abstract class AbstractDebugController {

    protected DebugUtil debugUtil = new DebugUtil(DebugUtil.CK_PREFIX_GLOBAL_INTERCEPTOR)

    def beforeInterceptor = {
        debugUtil.startSimpleBench(this.class.simpleName + ' ' + session.id)
    }

    def afterInterceptor = {
        def delta = debugUtil.stopSimpleBench(this.class.simpleName + ' ' + session.id)

        if (delta >= SystemProfiler.THRESHOLD_MS) {

            Map paramsToLog = [:]
            params.each{ key, value ->
                if (value instanceof String) {
                    paramsToLog.put(key, value)
                }
                else {
                    paramsToLog.put(key, value.getClass())
                }
            }
            def json = (paramsToLog as JSON)

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
            if (! global) {
                global = new SystemProfiler(uri: actionUri, ms: 1)
                global.save(flush:true)
            }
            else {
                SystemProfiler.executeUpdate('UPDATE SystemProfiler SET ms =:newValue WHERE id =:spId',
                [newValue: Integer.valueOf(global.ms) + 1, spId: global.id])
            }
        }
    }
}
