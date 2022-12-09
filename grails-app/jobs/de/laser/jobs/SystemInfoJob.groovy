package de.laser.jobs

import de.laser.SystemService
import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import groovy.util.logging.Slf4j

@Slf4j
class SystemInfoJob extends AbstractJob {

    SystemService systemService

    static triggers = {
        cron name:'systemInfoJobTrigger', cronExpression: "0 55 6 * * ?"
        // cron name:'systemInfoJobTrigger', cronExpression: "0 0/1 * * * ?" // debug only
        // cronExpression: "s m h D M W Y" - Year [optional]
    }

    static List<List> configurationProperties = [ ConfigMapper.SYSTEM_INFO_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && ConfigMapper.getSystemInfoJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('SYSTEM_INFO_JOB_START')) {
            return false
        }
        try {
            String test = systemService.getSystemInfoForJob()
            // TODO
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('SYSTEM_INFO_JOB_STOP')
    }
}
