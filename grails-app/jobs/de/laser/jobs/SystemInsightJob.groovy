package de.laser.jobs

import de.laser.SystemService
import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import groovy.util.logging.Slf4j

@Slf4j
class SystemInsightJob extends AbstractJob {

    SystemService systemService

    static triggers = {
        //cron name:'systemInsightJobTrigger', cronExpression: "0 55 6 * * ?"
        cron name:'systemInsightJobTrigger', cronExpression: "0 0/1 * * * ?" // debug only
        // cronExpression: "s m h D M W Y" - Year [optional]
    }

    static List<List> configurationProperties = [
            ConfigMapper.SYSTEM_INSIGHT_JOB_ACTIVE, ConfigMapper.SYSTEM_INSIGHT_INDEX,
    ]

    boolean isAvailable() {
        !jobIsRunning && ConfigMapper.getSystemInsightJobActive() && ConfigMapper.getSystemInsightIndex()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        // TODO ignored

        if (! start('SYSTEM_INFO_JOB_START')) {
            return false
        }
        try {
            if (! systemService.sendInsight_SystemEvents(ConfigMapper.getSystemInsightIndex())) {
                log.warn 'failed ..'
            }
        }
        catch (Exception e) {
            log.error( e.getMessage() )
        }
        stop('SYSTEM_INFO_JOB_STOP')
    }
}
