package de.laser.batch

import de.laser.SystemService
import de.laser.custom.CustomWebSocketConfig
import de.laser.helper.ConfigMapper
import de.laser.system.SystemActivityProfiler
import de.laser.base.AbstractJob
import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.util.logging.Slf4j
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageSendingOperations

@Slf4j
class HeartbeatJob extends AbstractJob {

    GrailsApplication grailsApplication
    SimpMessageSendingOperations brokerMessagingTemplate
    SystemService systemService

    static triggers = {
//    cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0/5 * * * * ?"
    cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0 0/5 * * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<List> configurationProperties = [ ConfigMapper.QUARTZ_HEARTBEAT ]

    boolean isAvailable() {
        !jobIsRunning
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        //
        //  to reduce effort, simple logging is sufficient here
        //
        if (! start()) {
            return false
        }
        try {
            ConfigMapper.setConfig( ConfigMapper.QUARTZ_HEARTBEAT, new Date() )

            SystemActivityProfiler.update()

            // org.springframework.messaging.simp.SimpMessageSendingOperations extends org.springframework.messaging.core.MessageSendingOperations
            // +-- org.springframework.messaging.simp.SimpMessagingTemplate
            //     +-- org.springframework.messaging.core.AbstractMessageSendingTemplate
            //             -> convertAndSend(D destination, Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //                  -> doConvert(Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //		            -> send(D destination, Message<?> message);
            //                         ^ org.springframework.messaging.simp.SimpMessagingTemplate

            brokerMessagingTemplate.convertAndSend( CustomWebSocketConfig.WS_TOPIC_STATUS, new JSON(systemService.getStatus()).toString(false) )

        } catch (Exception e) {
            log.error e.getMessage()
        }

        jobIsRunning = false
    }
}
