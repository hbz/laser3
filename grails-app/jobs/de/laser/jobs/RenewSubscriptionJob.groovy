package de.laser.jobs

import de.laser.RenewSubscriptionService
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

/**
 * This job, firing at 02:00 AM every day, checks if local subscriptions marked for automatic renewal have reached their end date and if so,
 * their a new subscription with is being created automatically for the next running year. Note: the update of the status flag is done one
 * hour later by the {@link StatusUpdateJob}!
 * @see de.laser.Subscription
 * @see RenewSubscriptionService#subscriptionRenewCheck()
 */
@Slf4j
class RenewSubscriptionJob extends AbstractJob {

    RenewSubscriptionService renewSubscriptionService

    static triggers = {
       cron name:'RenewSubscriptionTrigger', cronExpression: "0 0 2 * * ?" //Fire at 02:00 every day
    }

    static List<List> configurationProperties = []

    boolean isAvailable() {
        !jobIsRunning && !renewSubscriptionService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('SUB_RENEW_JOB_START')) {
            return false
        }
        try {
            if (!renewSubscriptionService.subscriptionRenewCheck()) {
                log.warn( 'RenewSubscriptionJob failed. Maybe ignored due blocked renewSubscriptionService' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('SUB_RENEW_JOB_COMPLETE')
    }
}
