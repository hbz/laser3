package de.laser.batch


import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class DashboardDueDatesJob extends AbstractJob {

    def dashboardDueDatesService
    def grailsApplication

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 22 * * ?" //Fire at 22:00 every day
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /5 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 5th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /1 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every minute
    }

    static configFlags = ['isUpdateDashboardTableInDatabase', 'isSendEmailsForDueDatesOfAllUsers']

    boolean isAvailable() {
        !jobIsRunning && !dashboardDueDatesService.update_running
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
            if (grailsApplication.config.isUpdateDashboardTableInDatabase || grailsApplication.config.isSendEmailsForDueDatesOfAllUsers) {
                log.info("Execute::dashboardDueDatesJob - Start");

                SystemEvent.createEvent('DBDD_JOB_START')

                if (! dashboardDueDatesService.takeCareOfDueDates(
                        grailsApplication.config.isUpdateDashboardTableInDatabase,
                        grailsApplication.config.isSendEmailsForDueDatesOfAllUsers,
                        [:]
                )) {
                    log.warn( 'Failed. Maybe ignored due blocked dashboardDueDatesService')
                }

                log.info("Execute::dashboardDueDatesJob - Finished");

                SystemEvent.createEvent('DBDD_JOB_COMPLETE')?.save(flush:true)

            } else {
                log.info("DashboardDueDates batch job: isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file");

                SystemEvent.createEvent('DBDD_JOB_IGNORE')
            }
        }
        catch (Exception e) {
            log.error(e)
        }

        jobIsRunning = false
    }
}
