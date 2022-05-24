package de.laser.ajax

import de.laser.helper.*
import de.laser.system.SystemMessage
import de.laser.system.SystemProfiler
import de.laser.system.SystemSetting
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller contains public (= open) methods for system profiling
 */
@Secured(['permitAll'])
class AjaxOpenController {

    def contextService

    /**
     * This method checks and returns whether the maintenance mode (= a deploy is going to take place soon) has been activated
     * @return the state of the maintenance mode and the frequency how often the state should be checked. Default are 300 seconds
     */
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

    /**
     * Call to update the system profiler
     * @return the current profiler state as JSON
     */
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

    /**
     * Call to list the system messages
     */
    @Secured(['permitAll']) // TODO
    def messages() {
        render template: '/templates/systemMessages', model: [systemMessages: SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)]
    }
}
