package de.laser.ajax

import de.laser.ContextService
import de.laser.helper.*
import de.laser.system.SystemMessage
import de.laser.system.SystemProfiler
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class AjaxOpenController {

    ContextService contextService

    @Secured(['permitAll'])
    def test() {
        Map test = [ a: 1, b: 2, status: 'test_only', time: System.currentTimeMillis() ]
        render test as JSON
    }

    @Secured(['permitAll'])
    def profiler() {
        Map<String, Object> result = [status: 'failed']
        SessionCacheWrapper cache = contextService.getSessionCache()
        ProfilerUtils pu = (ProfilerUtils) cache.get(ProfilerUtils.SESSION_SYSTEMPROFILER)

        if (pu) {
            long delta = pu.stopSimpleBench(params.uri)

            SystemProfiler.update(delta, params.uri)

            result.uri = params.uri
            result.delta = delta
            result.status = 'ok'
        }

        render result as JSON
    }

    @Secured(['permitAll'])
    def messages() {
        render template: '/templates/systemMessages', model: [systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]
    }
}
