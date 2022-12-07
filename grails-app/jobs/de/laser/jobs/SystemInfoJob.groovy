package de.laser.jobs

import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

@Slf4j
class SystemInfoJob extends AbstractJob {

    static triggers = {
        cron name:'systemInfoJobTrigger', cronExpression: "0 55 6 * * ?"
        // cron name:'systemInfoJobTrigger', cronExpression: "0 0/2 * * * ?" // debug only
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
            // println 'SystemInfoJob'
            // TODO
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('SYSTEM_INFO_JOB_COMPLETE')
    }
}
