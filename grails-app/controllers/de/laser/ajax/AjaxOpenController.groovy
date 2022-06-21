package de.laser.ajax

import de.laser.ContextService
import de.laser.cache.SessionCacheWrapper
import de.laser.helper.*
import de.laser.system.SystemMessage
import de.laser.system.SystemProfiler
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller contains public (= open) methods for system profiling
 */
@Secured(['permitAll'])
class AjaxOpenController {

    ContextService contextService

    @Secured(['permitAll'])
    def test() {
        Map test = [ a: 1, b: 2, status: 'test_only', time: System.currentTimeMillis() ]
        render test as JSON
    }

    /**
     * Call to update the system profiler
     * @return the current profiler state as JSON
     */
    @Secured(['permitAll'])
    def profiler() {
        Map<String, Object> result = [status: 'failed']
        SessionCacheWrapper cache = contextService.getSessionCache()
        Profiler pu = (Profiler) cache.get(Profiler.SESSION_SYSTEMPROFILER)

        if (pu) {
            long delta = pu.stopSimpleBench(params.uri)

            SystemProfiler.update(delta, params.uri)

            result.uri = params.uri
            result.delta = delta
            result.status = 'ok'
        }

        render result as JSON
    }

    /**
     * Call to list the system messages
     */
    @Secured(['permitAll'])
    def messages() {
        render template: '/templates/systemMessages', model: [systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]
    }
}
