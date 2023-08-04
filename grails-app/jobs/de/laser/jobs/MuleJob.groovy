package de.laser.jobs

import de.laser.SystemService
import de.laser.WekbStatsService
import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

@Slf4j
class MuleJob extends AbstractJob {

    SystemService systemService
    WekbStatsService wekbStatsService

    static triggers = {
    cron name: 'muleTrigger', startDelay:10000, cronExpression: "0 0/15 6-21 * * ?"
    // cronExpression: "s m h D M W Y"
    }

    static List<List> configurationProperties = [ ConfigMapper.MULE_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start()) {
            return false
        }
        try {
            SystemEvent sysEvent = SystemEvent.createEvent('MULE_START')
            long start_time = System.currentTimeMillis()

            wekbStatsService.updateCache()
            //systemService.sendSystemInsightMails()

            double elapsed = ((System.currentTimeMillis() - start_time) / 1000).round(2)
            sysEvent.changeTo('MULE_COMPLETE', [s: elapsed])

        } catch (Exception e) {
            log.error e.getMessage()
        }

        stop()
    }
}
