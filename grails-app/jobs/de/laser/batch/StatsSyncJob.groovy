package de.laser.batch

import de.laser.StatsSyncService
import de.laser.system.SystemEvent
import de.laser.helper.ConfigMapper
import de.laser.base.AbstractJob

class StatsSyncJob extends AbstractJob {

    StatsSyncService statsSyncService

    static triggers = {
        // Delay 20 seconds, run every 10 mins.
        // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
        // Example - every 10 mins 0 0/10 * * * ?
        // At 4am each Sunday - Sync stats
        cron name:'statsSyncTrigger', cronExpression: "0 0 4 ? * *"
        //cron name:'statsSyncTrigger', cronExpression: "0 0/10 * * * ?" //debug only!
        // cronExpression: "s m h D M W Y"
        //                  | | | | | | `- Year [optional]
        //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
        //                  | | | | `- Month, 1-12 or JAN-DEC
        //                  | | | `- Day of Month, 1-31, ?
        //                  | | `- Hour, 0-23
        //                  | `- Minute, 0-59
        //                  `- Second, 0-59
    }

    static List<String> configFlags = ['laserStatsSyncJobActive']

    boolean isAvailable() {
        !jobIsRunning && !statsSyncService.running && Boolean.valueOf(ConfigMapper.getLaserStatsSyncJobActive())
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

            if (ConfigMapper.getLaserStatsSyncJobActive()) {
                log.debug("Running Stats SYNC batch job")
                SystemEvent.createEvent('STATS_SYNC_JOB_START')

                statsSyncService.doFetch(true)
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
