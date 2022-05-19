package de.laser.batch

import de.laser.base.AbstractJob
import de.laser.system.SystemEvent

class RemovedTitleJob extends AbstractJob {

    def packageService

    static triggers = {
       cron name:'RemoveTitleTrigger', cronExpression: "0 0 21 * * ?" //Fire at 21:00 every day
//        cron name:'RemoveTitleTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'RemoveTitleTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

    boolean isAvailable() {
        !jobIsRunning && !packageService.titleCleanupRunning
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        SystemEvent.createEvent('REMOVE_TITLE_JOB_START')

        try {
            log.info("Execute::RemoveTitleJob - Start")

            if (!packageService.clearRemovedTitles() ) {
                log.warn( 'Failed. Maybe ignored due blocked removedTitleJob')
            }

            log.info("Execute::RemoveTitleJob - Finished")
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        SystemEvent.createEvent('REMOVE_TITLE_JOB_COMPLETE')

        jobIsRunning = false
    }
}
