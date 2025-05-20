package de.laser.jobs

import de.laser.base.AbstractJob
import de.laser.system.SystemEvent
import groovy.util.logging.Slf4j

/**
 * This job triggers at 21:00 every day and clears titles marked as removed
 */
@Slf4j
class RemovedTitleJob extends AbstractJob {

    def packageService

    static triggers = {
       cron name:'RemoveTitleTrigger', cronExpression: "0 0 21 * * ?" //Fire at 21:00 every day
//        cron name:'RemoveTitleTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'RemoveTitleTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

    static List<List> configurationProperties = []

    boolean isAvailable() {
        !jobIsRunning && !packageService.titleCleanupRunning
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        SystemEvent sysEvent = start('REMOVE_TITLE_JOB_START')
        def crt = null

        if (! sysEvent) {
            return false
        }
        try {
            crt = packageService.clearRemovedTitles()
            if (!crt ) {
                log.warn( 'RemoveTitleJob failed. Maybe ignored due blocked removedTitleJob' )
            }
        }
        catch (Exception e) {
            log.error e.getMessage()
        }
        stopAndComplete(sysEvent, [returns: crt])
    }
}
