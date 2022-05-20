package de.laser.batch

import de.laser.SurveyUpdateService
import de.laser.system.SystemEvent
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class SurveyUpdateJob extends AbstractJob {

    SurveyUpdateService surveyUpdateService

    static triggers = {
        cron name:'SurveyUpdateJobTrigger', cronExpression: "0 0 23 * * ?" //Fire at 23:00 every day
    }

    static List<List> configurationProperties = []

    boolean isAvailable() {
        !jobIsRunning && !surveyUpdateService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start('SURVEY_UPDATE_JOB_START')) {
            return false
        }
        try {
            if (! surveyUpdateService.surveyCheck()) {
                log.warn('Failed. Maybe ignored due blocked surveyUpdateService')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('SURVEY_UPDATE_JOB_COMPLETE')
    }
}
