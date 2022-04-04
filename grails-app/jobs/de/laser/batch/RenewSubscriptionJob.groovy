package de.laser.batch

import de.laser.RenewSubscriptionService
import de.laser.base.AbstractJob
import de.laser.system.SystemEvent

class RenewSubscriptionJob extends AbstractJob {

    RenewSubscriptionService renewSubscriptionService

    static triggers = {
       cron name:'RenewSubscriptionTrigger', cronExpression: "0 0 2 * * ?" //Fire at 02:00 every day

    }

    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !renewSubscriptionService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        SystemEvent.createEvent('SUB_RENEW_JOB_START')

        try {
            log.info("Execute::RenewSubscriptionJob - Start")

            if (!renewSubscriptionService.subscriptionRenewCheck()) {
                log.warn( 'Failed. Maybe ignored due blocked renewSubscriptionService')
            }

            log.info("Execute::RenewSubscriptionJob - Finished")
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        SystemEvent.createEvent('SUB_RENEW_JOB_COMPLETE')

        jobIsRunning = false
    }
}
