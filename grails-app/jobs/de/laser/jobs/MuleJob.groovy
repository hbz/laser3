package de.laser.jobs

import de.laser.SystemService
import de.laser.WekbNewsService
import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

import java.time.LocalTime

/**
 * This job retrieves between 06:00 and 21:00 every 15 minutes the last changes in the we:kb
 */
@Slf4j
class MuleJob extends AbstractJob {

    SystemService systemService
    WekbNewsService wekbNewsService

    static triggers = {
        cron name: 'muleTrigger', startDelay:10000, cronExpression: "0 0/15 6-21 * * ?"
//        cron name: 'muleTrigger', startDelay:10000, cronExpression: "0 0/5 6-21 * * ?" // local test
        // cronExpression: "s m h D M W Y"
    }

    static List<List> configurationProperties = [ ConfigMapper.MULE_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && ConfigMapper.getMuleJobActive()
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

            wekbNewsService.updateCache()

            // only once per day ..

            if (Math.abs(LocalTime.parse('06:45').toSecondOfDay() - LocalTime.now().toSecondOfDay()) < 300) {
                systemService.sendSystemInsightMails()
            }

            double elapsed = ((System.currentTimeMillis() - start_time) / 1000).round(2)
            sysEvent.changeTo('MULE_COMPLETE', [s: elapsed])

        } catch (Exception e) {
            log.error e.getMessage()
        }

        stop()
    }
}
