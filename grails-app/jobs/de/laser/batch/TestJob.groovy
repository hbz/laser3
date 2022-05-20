package de.laser.batch

import de.laser.ContextService
import de.laser.Org
import de.laser.helper.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class TestJob extends AbstractJob {

    ContextService contextService

    static triggers = {

    //cron name:'TestJob', startDelay:0, cronExpression: "0/10 * * * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<List> configurationProperties = [ ConfigMapper.TEST_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && ConfigMapper.getTestJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start()) {
            return false
        }
        try {
            log.debug( 'Ping' )
            // provocate error @  WebUtils.retrieveGrailsWebRequest().getSession()
            Org ctx = contextService.getOrg()
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop()
    }
}
