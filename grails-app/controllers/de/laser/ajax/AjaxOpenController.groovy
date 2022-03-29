package de.laser.ajax

import de.laser.ContextService
import de.laser.helper.*
import de.laser.system.SystemMessage
import de.laser.system.SystemProfiler
import de.laser.system.SystemSetting
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class AjaxOpenController {

    ContextService contextService

    @Secured(['permitAll'])
    def status() {
        Map result = [ status: 'error' ]
        try {
            result = [
                    status: 'ok',
                    maintenance: SystemSetting.findByName('MaintenanceMode').value == 'true',
                    messages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION) ? true : false,
                    interval: Integer.parseInt(SystemSetting.findByName('StatusUpdateInterval').value)
            ]
        } catch(Exception e) {
            log.error( e.getMessage() )
        }
        render result as JSON
    }

    @Secured(['permitAll'])
    def profiler() {
        Map<String, Object> result = [status: 'failed']
        SessionCacheWrapper cache = contextService.getSessionCache()
        ProfilerUtils pu = (ProfilerUtils) cache.get(ProfilerUtils.SYSPROFILER_SESSION)

        if (pu) {
            long delta = pu.stopSimpleBench(params.uri)

            SystemProfiler.update(delta, params.uri)

            result.uri = params.uri
            result.delta = delta
            result.status = 'ok'
        }

        render result as JSON
    }

    @Secured(['permitAll']) // TODO
    def messages() {
        render template: '/templates/systemMessages', model: [systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]
    }
}
