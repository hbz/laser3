package de.laser.batch

import de.laser.DashboardDueDatesService
import de.laser.system.SystemEvent
import de.laser.helper.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class DashboardDueDatesJob extends AbstractJob {

    DashboardDueDatesService dashboardDueDatesService

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 22 * * ?" //Fire at 22:00 every day
//        cron name:'DashboardDueDatesTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
    }

    static List<List> configurationProperties = [ ConfigMapper.IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE, ConfigMapper.IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS ]

    boolean isAvailable() {
        !jobIsRunning && !dashboardDueDatesService.update_running && (ConfigMapper.getIsUpdateDashboardTableInDatabase() || ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers())
    }

    def execute() {
        if (! (ConfigMapper.getIsUpdateDashboardTableInDatabase() || ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers())) {
            SystemEvent.createEvent('DBDD_JOB_IGNORE')
            log.info( 'DashboardDueDates - isUpdateDashboardTableInDatabase and/or isSendEmailsForDueDatesOfAllUsers are disabled in configuration' )
        }

        if (! start('DBDD_JOB_START')) {
            return false
        }
        try {
            if (! dashboardDueDatesService.takeCareOfDueDates(
                    ConfigMapper.getIsUpdateDashboardTableInDatabase(),
                    ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers(),
                    [:] //!!!!! flash as an empty container as placeholder! Mark that!
            )) {
                log.warn( 'Failed. Maybe ignored due blocked dashboardDueDatesService')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('DBDD_JOB_COMPLETE')
    }
}
