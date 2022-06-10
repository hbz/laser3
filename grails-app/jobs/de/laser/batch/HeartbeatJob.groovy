package de.laser.batch

import de.laser.helper.ConfigMapper
import de.laser.system.SystemActivityProfiler
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class HeartbeatJob extends AbstractJob {

    static triggers = {
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

        } catch (Exception e) {
            log.error e.getMessage()
        }

        jobIsRunning = false
    }
}
