package de.laser.batch

import de.laser.SystemEvent
import de.laser.helper.ConfigUtils
import de.laser.quartz.AbstractJob
import org.codehaus.groovy.grails.commons.GrailsApplication

class StatsSyncJob extends AbstractJob {

    def statsSyncService
    GrailsApplication grailsApplication

    static triggers = {
        // Delay 20 seconds, run every 10 mins.
        // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
        // Example - every 10 mins 0 0/10 * * * ?
        // At 5 past 2am on the first of every month - Sync stats
        //cron name:'statsSyncTrigger', startDelay:10, cronExpression: "* 10 * * * ?"
        // cronExpression: "s m h D M W Y"
        //                  | | | | | | `- Year [optional]
        //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
        //                  | | | | `- Month, 1-12 or JAN-DEC
        //                  | | | `- Day of Month, 1-31, ?
        //                  | | `- Hour, 0-23
        //                  | `- Minute, 0-59
        //                  `- Second, 0-59
    }

    static List<String> configFlags = ['StatsSyncJobActiv']

    boolean isAvailable() {
        !jobIsRunning && !statsSyncService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        try {
            log.debug("Execute::statsSyncJob")

            if (ConfigUtils.getStatsSyncJobActiv()) {
                log.debug("Running Stats SYNC batch job")
                SystemEvent.createEvent('STATS_SYNC_JOB_START')

                statsSyncService.doSync()
                //if (! statsSyncService.doSync()) {
                //    log.warn( 'Failed. Maybe ignored due blocked statsSyncService')
                //}

                SystemEvent.createEvent('STATS_SYNC_JOB_COMPLETE')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        jobIsRunning = false
    }
}
