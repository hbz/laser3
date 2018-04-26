package de.Laser.batch

class StatsSyncJob {
    def statsSyncService
    def grailsApplication

    static triggers = {
        // Delay 20 seconds, run every 10 mins.
        // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
        // Example - every 10 mins 0 0/10 * * * ?
        // At 5 past 2am on the first of every month - Sync stats
        //cron name:'statsSyncTrigger', startDelay:10, cronExpression: "* 10 * * * ?"
        // cronExpression: "s m h D M W Y"
        //                  | | | | | | `- Year [optional]
        //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
        //                  | | | | `- Month, 1-12 or JAN-DEC
        //                  | | | `- Day of Month, 1-31, ?
        //                  | | `- Hour, 0-23
        //                  | `- Minute, 0-59
        //                  `- Second, 0-59
    }

    def execute() {
        log.debug("Execute::statsSyncJob");
        if ( grailsApplication.config.KBPlusMaster == true ) {
            log.debug("This server is marked as KBPlus master. Running Stats SYNC batch job");
            statsSyncService.doSync()
        }
        else if ( grailsApplication.config.hbzMaster == true && grailsApplication.config.StatsSyncJobActiv == true ) {
            log.debug("This server is marked as KBPlus master. Running Stats SYNC batch job");
            statsSyncService.doSync()
        }
        else {
            log.debug("This server is NOT marked as KBPlus master. NOT Running Stats SYNC batch job");
        }
    }
}
