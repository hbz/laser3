package de.laser.jobs

import de.laser.StatsSyncService
import de.laser.utils.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class StatsSyncJob extends AbstractJob {

    StatsSyncService statsSyncService

    static triggers = {
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

    static List<List> configurationProperties = [ ConfigMapper.LASER_STATS_SYNC_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && !statsSyncService.running && ConfigMapper.getLaserStatsSyncJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('STATS_SYNC_JOB_START')) {
            return false
        }
        try {
            statsSyncService.doFetch(true)
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('STATS_SYNC_JOB_COMPLETE')
    }
}
