package de.laser.jobs

import de.laser.DashboardDueDatesService
import de.laser.system.SystemEvent
import de.laser.config.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

/**
 * This job takes care about the update and mail reminder sending of currently due dates. It is scheduled at 06:00 AM every day
 * @see de.laser.dates.DashboardDueDate
 */
@Slf4j
class DashboardDueDatesJob extends AbstractJob {

    DashboardDueDatesService dashboardDueDatesService

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 6 * * ?" //Fire at 06:00 every day
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

        SystemEvent sysEvent = start('DBDD_JOB_START')

        if (! sysEvent) {
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
        stopAndComplete(sysEvent)
    }
}
