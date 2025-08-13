package de.laser.jobs

import de.laser.SystemService
import de.laser.UserAccountService
import de.laser.WekbNewsService
import de.laser.base.AbstractJob
import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import de.laser.utils.AppUtils
import groovy.util.logging.Slf4j

import java.time.LocalTime

/**
 * This job retrieves between 06:00 and 21:00 every 15 minutes the last changes in the we:kb
 */
@Slf4j
class MuleJob extends AbstractJob {

    SystemService systemService
    UserAccountService userAccountService
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

        Calendar now = Calendar.getInstance()
        boolean timeCheck_hourly    = _timeCheck(now, -1, 0) // hourly
        boolean timeCheck_0600      = _timeCheck(now, 6,  0) // once per day .. 6:00
        boolean timeCheck_0645      = _timeCheck(now, 6, 45) // once per day .. 6:45

        if (! sysEvent) {
            return false
        }
        try {
            userAccountService.unlockLockedUserAccounts()

//            if (timeCheck_hourly)) {
//            }
            if (timeCheck_0600) {
                wekbNewsService.clearCache()
            }
            if (timeCheck_0645) {
                if (AppUtils.getCurrentServer() in [AppUtils.LOCAL, AppUtils.PROD]) {
                    // TODO:
//                    userAccountService.expireUserAccounts()
//                    userAccountService.warnInactiveUserAccounts()
                }
                systemService.sendSystemInsightMails()
            }
            wekbNewsService.updateCache()
        }
        catch (Exception e) {
            log.error e.getMessage()
        }
        stopAndComplete(sysEvent)
    }

    private boolean _timeCheck(Calendar now, int hour, int minute) {
        boolean check = false
        if (now.get(Calendar.HOUR_OF_DAY) == hour || hour == -1) {
            if (now.get(Calendar.MINUTE) == minute || minute == -1) {
                check = true
            }
        }
        check
    }
}
