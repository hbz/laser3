package de.laser.jobs

import de.laser.StatusUpdateService
import de.laser.SubscriptionService
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class StatusUpdateJob extends AbstractJob {

    StatusUpdateService statusUpdateService
    SubscriptionService subscriptionService

    static triggers = {
       cron name:'StatusUpdateTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'StatusUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
    }

    static List<List> configurationProperties = []

    boolean isAvailable() {
        !jobIsRunning && !statusUpdateService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('SUB_UPDATE_JOB_START')) {
            return false
        }
        try {
            if (!statusUpdateService.subscriptionCheck() || !statusUpdateService.licenseCheck() ) {
                log.warn( 'StatusUpdateJob failed. Maybe ignored due blocked statusUpdateService' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('SUB_UPDATE_JOB_COMPLETE')
    }
}
