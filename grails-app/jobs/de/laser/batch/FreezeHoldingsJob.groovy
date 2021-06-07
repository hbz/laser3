package de.laser.batch

import de.laser.SubscriptionService
import de.laser.quartz.AbstractJob
import de.laser.system.SystemEvent

class FreezeHoldingsJob extends AbstractJob {

    SubscriptionService subscriptionService

    static triggers = {

    cron name:'FreezeHoldingsTrigger', startDelay:0, cronExpression: "0 30 3 1 1 ? *"
    //cron name:'FreezeHoldingsTrigger', cronExpression: "0/10 * * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every ten seconds
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning // no service needed
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true
        SystemEvent.createEvent('FREEZE_HOLDING_JOB_START')
        subscriptionService.freezeSubscriptionHoldings()
        SystemEvent.createEvent('FREEZE_HOLDING_JOB_COMPLETE')
        jobIsRunning = false
    }
}
