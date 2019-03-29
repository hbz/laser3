package de.laser.batch

import com.k_int.kbplus.EventLog
import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class DashboardDueDatesJob extends AbstractJob {

    def dashboardDueDatesService
    def grailsApplication

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 22 * * ?" //Fire at 22:00 every day
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /5 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 5th minute
    }

    static configFlags = ['isUpdateDashboardTableInDatabase', 'isSendEmailsForDueDatesOfAllUsers']

    def execute() {
        if (grailsApplication.config.isUpdateDashboardTableInDatabase || grailsApplication.config.isSendEmailsForDueDatesOfAllUsers) {
            log.info("Execute::dashboardDueDatesJob - Start");

            // TODO: remove due SystemEvent
            new EventLog(event:'Execute::dashboardDueDatesJob', message:'Start', tstp:new Date(System.currentTimeMillis())).save(flush:true)

            SystemEvent.createEvent('DBDD_JOB_START')

            dashboardDueDatesService.takeCareOfDueDates(
                    grailsApplication.config.isUpdateDashboardTableInDatabase,
                    grailsApplication.config.isSendEmailsForDueDatesOfAllUsers,
                    [:]
            )
            log.info("Execute::dashboardDueDatesJob - Finished");

            // TODO: remove due SystemEvent
            new EventLog(event:'Execute::dashboardDueDatesJob', message:'Finished', tstp:new Date(System.currentTimeMillis())).save(flush:true)

            SystemEvent.createEvent('DBDD_JOB_COMPLETE')?.save(flush:true)

        } else {
            log.info("DashboardDueDates batch job: isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file");

            // TODO: remove due SystemEvent
            new EventLog(event:'DashboardDueDates batch job: isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file', message:'XXX', tstp:new Date(System.currentTimeMillis())).save(flush:true)

            SystemEvent.createEvent('DBDD_JOB_IGNORE')
        }
    }
}
