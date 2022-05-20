package de.laser.batch

import de.laser.DataloadService
import de.laser.base.AbstractJob
import de.laser.helper.ConfigMapper
import groovy.util.logging.Slf4j

@Slf4j
class IndexUpdateJob extends AbstractJob {

    DataloadService dataloadService

    static triggers = {
        // Delay 120 seconds, run every 10 mins.
        cron name:'cronTrigger', startDelay:190000, cronExpression: "0 0/10 7-20 * * ?"
    }

    static List<List> configurationProperties = [ ConfigMapper.INDEX_UPDATE_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && !dataloadService.update_running && ConfigMapper.getIndexUpdateJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start()) {
            return false
        }
        try {
            if (!dataloadService.updateFTIndexes()) {
                log.warn( 'IndexUpdateJob failed. Maybe ignored due blocked dataloadService' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop()
    }
}
