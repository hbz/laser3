package de.laser.batch


import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class SubscriptionUpdateJob extends AbstractJob {

    def subscriptionUpdateService
/* ----> DISABLED
    static triggers = {
        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

 */

    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !subscriptionUpdateService.running
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
            log.info("Execute::SubscriptionUpdateJob - Start");

            if (! subscriptionUpdateService.subscriptionCheck()) {
                log.warn( 'Failed. Maybe ignored due blocked subscriptionUpdateService')
            }

            log.info("Execute::SubscriptionUpdateJob - Finished");
        }
        catch (Exception e) {
            log.error(e)
        }
        SystemEvent.createEvent('SUB_UPDATE_JOB_COMPLETE')

        jobIsRunning = false
    }
}
