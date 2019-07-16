package de.laser.batch

import com.k_int.kbplus.EventLog
import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class SubscriptionUpdateJob extends AbstractJob {

    def subscriptionUpdateService

    static triggers = {
        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

    static configFlags = []

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

        log.info("Execute::SubscriptionUpdateJob - Start");

        subscriptionUpdateService.subscriptionCheck()

        log.info("Execute::SubscriptionUpdateJob - Finished");

        SystemEvent.createEvent('SUB_UPDATE_JOB_COMPLETE')
        jobIsRunning = false
    }
}
