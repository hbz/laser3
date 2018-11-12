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
        log.debug("Execute::dashboardDueDatesJob");
//        if ( grailsApplication.config.KBPlusMaster == true ) {
//            log.debug("This server is marked as KBPlus master. Running DashboardDueDates batch job");
            dashboardDueDatesService.takeCareOfDueDates()
//        }
//        else if ( grailsApplication.config.hbzMaster == true && grailsApplication.config.StatsSyncJobActiv == true ) {
//            log.debug("This server is marked as KBPlus master. Running DashboardDueDates batch job");
//            dashboardDueDatesService.takeCareOfDueDates()
//        }
//        else {
//            log.debug("This server is NOT marked as KBPlus master. NOT Running DashboardDueDates batch job");
//        }
    }
}
