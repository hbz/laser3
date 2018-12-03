package de.Laser.batch

class DashboardDueDatesJob {
    def dashboardDueDatesService
    def grailsApplication

    static triggers = {
        cron name:'DashboardDueDatesTrigger', cronExpression: "0 0 22 * * ?" //Fire at 22:00 every day
//        cron name:'DashboardDueDatesTrigger', cronExpression: "* /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'DashboardDueDatesTrigger', cronExpression: "* /1 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 2nd minute
    }

    def execute() {
        if (grailsApplication.config.isUpdateDashboardTableInDatabase || grailsApplication.config.isSendEmailsForDueDatesOfAllUsers) {
            log.debug("Execute::dashboardDueDatesJob");
            dashboardDueDatesService.takeCareOfDueDates(
                    grailsApplication.config.isUpdateDashboardTableInDatabase,
                    grailsApplication.config.isSendEmailsForDueDatesOfAllUsers
            )
        } else {
            log.debug("DashboardDueDates batch job: isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file");
        }
    }
}
