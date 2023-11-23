package de.laser.jobs

import de.laser.GlobalSourceSyncService
import de.laser.config.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

/**
 * This job triggers the overnight title and package data synchronisation with the we:kb knowledge base.
 * It is scheduled for 00:01 AM (i.e. one minute after midnight) every day
 */
@Slf4j
class GlobalDataSyncJob extends AbstractJob {

    GlobalSourceSyncService globalSourceSyncService

    static triggers = {
    cron name:'globalDataSyncTrigger', startDelay:180000, cronExpression: "0 1 0 * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<List> configurationProperties = [ ConfigMapper.GLOBAL_DATA_SYNC_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && !globalSourceSyncService.running && ConfigMapper.getGlobalDataSyncJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('GD_SYNC_JOB_START')) {
            return false
        }
        try {
            if (! globalSourceSyncService.startSync()) {
                log.warn( 'GlobalDataSyncJob failed. Maybe ignored due blocked globalSourceSyncService' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('GD_SYNC_JOB_COMPLETE')
  }
}

