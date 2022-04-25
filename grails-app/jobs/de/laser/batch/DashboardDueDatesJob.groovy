package de.laser.batch

import de.laser.DashboardDueDatesService
import de.laser.system.SystemEvent
import de.laser.helper.ConfigMapper
import de.laser.base.AbstractJob

class DashboardDueDatesJob extends AbstractJob {

    DashboardDueDatesService dashboardDueDatesService

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 22 * * ?" //Fire at 22:00 every day
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /5 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 5th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /1 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every minute
    }

    static List<String> configFlags = ['isUpdateDashboardTableInDatabase', 'isSendEmailsForDueDatesOfAllUsers']

    boolean isAvailable() {
        !jobIsRunning && !dashboardDueDatesService.update_running
    }

    def execute() {
        if (! isAvailable()) {
            return false
        }
        setJobStart()

        try {
            if (ConfigMapper.getIsUpdateDashboardTableInDatabase() || ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers()) {
                log.info("Execute::dashboardDueDatesJob - Start");

                SystemEvent.createEvent('DBDD_JOB_START')

                if (! dashboardDueDatesService.takeCareOfDueDates(
                        ConfigMapper.getIsUpdateDashboardTableInDatabase(),
                        ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers(),
                        [:] //!!!!! flash as an empty container as placeholder! Mark that!
                )) {
                    log.warn( 'Failed. Maybe ignored due blocked dashboardDueDatesService')
                }

                log.info("Execute::dashboardDueDatesJob - Finished");

                SystemEvent.createEvent('DBDD_JOB_COMPLETE')

            } else {
                log.info("DashboardDueDates batch job: isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file");

                SystemEvent.createEvent('DBDD_JOB_IGNORE')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        setJobEnd()
    }
}
