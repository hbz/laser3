package de.laser.batch

import de.laser.RenewSubscriptionService
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

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
