package com.k_int.kbplus.batch

import de.laser.system.SystemActivityProfiler
import de.laser.quartz.AbstractJob
import grails.core.GrailsApplication

class HeartbeatJob extends AbstractJob {

    GrailsApplication grailsApplication

    static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // Every 10 mins
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

        jobIsRunning = false
    }
}
