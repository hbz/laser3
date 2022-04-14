package de.laser.batch

import de.laser.PendingChange
import de.laser.PendingChangeConfiguration
import de.laser.quartz.AbstractJob
import de.laser.system.SystemEvent

class RemovedTitleJob extends AbstractJob {

    def packageService

    static triggers = {
       cron name:'StatusUpdateTrigger', cronExpression: "0 0 21 * * ?" //Fire at 03:00 every day
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
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

            Set<PendingChange> titlesToRemove = PendingChange.executeQuery("select pc from PendingChange pc where pc.msgToken = :titleRemoved", [titleRemoved: PendingChangeConfiguration.TITLE_REMOVED])
            if (!packageService.clearRemovedTitles(titlesToRemove) ) {
                log.warn( 'Failed. Maybe ignored due blocked statusUpdateService')
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
