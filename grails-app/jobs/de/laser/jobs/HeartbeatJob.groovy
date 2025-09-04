package de.laser.jobs

import de.laser.WekbNewsService
import de.laser.annotations.UnstableFeature
import de.laser.system.MuleCache
import de.laser.system.SystemActivityProfiler
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j
//import org.springframework.messaging.simp.SimpMessagingTemplate

/**
 * Records every five minutes the current system activity
 * @see SystemActivityProfiler
 */
@Slf4j
class HeartbeatJob extends AbstractJob {

//    SystemService systemService
    WekbNewsService wekbNewsService
//    SimpMessagingTemplate brokerMessagingTemplate

    static triggers = {
    cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0 0/5 * * * ?"
    //cron name:'heartbeatTrigger', startDelay:10000, cronExpression: "0/10 * * * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<List> configurationProperties = []

    boolean isAvailable() {
        !jobIsRunning
    }
    boolean isRunning() {
        jobIsRunning
    }

    @UnstableFeature
    def execute() {
        if (! simpleStart(true)) { // shrinking logs
            return false
        }
        try {
            MuleCache.updateEntry( MuleCache.CFG.SYSTEM_HEARTBEAT, new Date() )
            SystemActivityProfiler.update()

            // org.springframework.messaging.simp.SimpMessageSendingOperations extends org.springframework.messaging.core.MessageSendingOperations
            // +-- org.springframework.messaging.simp.SimpMessagingTemplate
            //     +-- org.springframework.messaging.core.AbstractMessageSendingTemplate
            //             -> convertAndSend(D destination, Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //                  -> doConvert(Object payload, @Nullable Map<String, Object> headers, @Nullable MessagePostProcessor postProcessor);
            //		            -> send(D destination, Message<?> message);
            //                         ^ org.springframework.messaging.simp.SimpMessagingTemplate

            // String status = new JSON(systemService.getStatusMessage()).toString(false)
            // brokerMessagingTemplate.convertAndSend( CustomWebSocketMessageBrokerConfig.WS_TOPIC_STATUS, status )

        } catch (Exception e) {
            log.error e.getMessage()
        }

        simpleStop(true) // shrinking logs
    }
}
