package de.laser.batch

import de.laser.SystemService
import de.laser.system.SystemActivityProfiler
import de.laser.base.AbstractJob
import grails.converters.JSON
import grails.core.GrailsApplication
import org.springframework.messaging.simp.SimpMessageSendingOperations

class HeartbeatJob extends AbstractJob {

    GrailsApplication grailsApplication
    SimpMessageSendingOperations brokerMessagingTemplate
    SystemService systemService

    static triggers = {
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins: 0 0/10 * * * ?
    cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0/10 * * * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<String> configFlags = ['quartzHeartbeat']

    boolean isAvailable() {
        !jobIsRunning // no service needed
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        log.debug("Heartbeat Job")
        grailsApplication.config.quartzHeartbeat = new Date()

        SystemActivityProfiler.update()

        try {
            // org.springframework.messaging.simp.SimpMessageSendingOperations extends org.springframework.messaging.core.MessageSendingOperations
            // +-- org.springframework.messaging.simp.SimpMessagingTemplate
            //     +-- org.springframework.messaging.core.AbstractMessageSendingTemplate
            //             -> convertAndSend(D destination, Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //                  -> doConvert(Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //		            -> send(D destination, Message<?> message);
            //                         ^ org.springframework.messaging.simp.SimpMessagingTemplate

            brokerMessagingTemplate.convertAndSend '/topic/status', new JSON(systemService.getStatus()).toString(false)
        } catch (Exception e) {
            log.error e.getMessage()
        }

        jobIsRunning = false
    }
}
