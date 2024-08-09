package de.laser.jobs

import de.laser.StatsSyncService
import de.laser.config.ConfigMapper
import de.laser.base.AbstractJob
import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

/**
 * Triggers at 04:00 A.M. every night and cleans up cached export files
 */
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

    static List<List> configurationProperties = [ ConfigMapper.STATS_SYNC_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && !statsSyncService.running && ConfigMapper.getStatsSyncJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        SystemEvent sysEvent = start('STATS_SYNC_JOB_START')

        if (! sysEvent) {
            return false
        }
        try {
            //statsSyncService.doFetch(true) changed as of ERMS-4834
            String usagePath = ConfigMapper.getStatsReportSaveLocation() ?: '/usage'
            File folder = new File(usagePath)
            if(!folder.exists())
                folder.mkdir()
            else {
                folder.listFiles().each { File oldReport ->
                    oldReport.delete()
                }
            }
        }
        catch (Exception e) {
            log.error e.getMessage()
        }

        stopAndComplete(sysEvent)
    }
}
