package de.laser.batch

import com.k_int.kbplus.EventLog
import de.laser.SystemEvent
import de.laser.quartz.AbstractJob


class SurveyUpdateJob extends AbstractJob {

    def surveyUpdateService

    static triggers = {
        cron name:'SurveyUpdateJobTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
    }

    static configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !surveyUpdateService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }

        jobIsRunning = true
        new EventLog(event:'Execute::SurveyUpdateJob', message:'Start', tstp:new Date(System.currentTimeMillis())).save(flush:true)


        log.info("Execute::SurveyUpdateJob - Start");

        SystemEvent.createEvent('SURVEY_UPDATE_JOB_START')

        surveyUpdateService.surveyCheck()

        log.info("Execute::SurveyUpdateJob - Finished");

        SystemEvent.createEvent('SURVEY_UPDATE_JOB_COMPLETE')

        new EventLog(event:'Execute::SurveyUpdateJob', message:'Finished', tstp:new Date(System.currentTimeMillis())).save(flush:true)

        jobIsRunning = false
    }
}
