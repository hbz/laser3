package de.laser.jobs

import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

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
        if (! start('REMOVE_TITLE_JOB_START')) {
            return false
        }
        try {
            if (!packageService.clearRemovedTitles() ) {
                log.warn( 'RemoveTitleJob failed. Maybe ignored due blocked removedTitleJob' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('REMOVE_TITLE_JOB_COMPLETE')
    }
}
