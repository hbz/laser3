package de.laser.batch

import de.laser.DataloadService
import de.laser.base.AbstractJob

class IndexUpdateJob extends AbstractJob {

    DataloadService dataloadService

    static triggers = {
        // Delay 120 seconds, run every 10 mins.
        cron name:'cronTrigger', startDelay:190000, cronExpression: "0 0/10 7-20 * * ?"
    }

    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !dataloadService.update_running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true

        try {
            log.debug("****Running Index Update Job****")

            if (! dataloadService.updateFTIndexes()) {
                log.warn( 'Failed. Maybe ignored due blocked dataloadService')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        jobIsRunning = false
    }
}
