package de.laser.batch


import de.laser.system.SystemEvent
import de.laser.quartz.AbstractJob

class StatusUpdateJob extends AbstractJob {

    def statusUpdateService
    def subscriptionService

    static triggers = {
       cron name:'StatusUpdateTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !statusUpdateService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        SystemEvent.createEvent('SUB_UPDATE_JOB_START')

        try {
            log.info("Execute::SubscriptionUpdateJob - Start")

            if (!statusUpdateService.subscriptionCheck() || !statusUpdateService.licenseCheck() || !subscriptionService.freezeSubscriptionHoldings() ) {
                log.warn( 'Failed. Maybe ignored due blocked statusUpdateService')
            }

            log.info("Execute::SubscriptionUpdateJob - Finished")
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        SystemEvent.createEvent('SUB_UPDATE_JOB_COMPLETE')

        jobIsRunning = false
    }
}
