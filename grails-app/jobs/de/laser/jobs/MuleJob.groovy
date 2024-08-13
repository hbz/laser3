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
        SystemEvent sysEvent = start('MULE_START')

        if (! sysEvent) {
            return false
        }
        try {
            LocalTime now = LocalTime.now()

            systemService.maintainUnlockedUserAccounts()
            // every (full) hour
//            if (_checkTime(now, -1, 0)) {
//                systemService.maintainUnlockedUserAccounts()
//            }
            // once per day .. 6:00
//            if (_checkTime(now, 6, 0)) {
//                wekbNewsService.clearCache()
//            }
            // once per day .. 6:45
            if (_checkTime(now, 6, 45)) {
                systemService.sendSystemInsightMails()
//                systemService.maintainExpiredUserAccounts()
            }
            wekbNewsService.updateCache()
        }
        catch (Exception e) {
            log.error e.getMessage()
        }
        stopAndComplete(sysEvent)
    }

    private boolean _checkTime(LocalTime now, int hour, int minute) {
        if (now.getHour() == hour || hour == -1 ) {
            if (now.getMinute() == minute || minute == -1) {
                true
            }
        }
        false
    }
}
