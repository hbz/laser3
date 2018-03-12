package com.k_int.kbplus.batch



class AdminJob {
    def grailsApplication
    def AdminReminderService

    static triggers = {
        // Delay 20 seconds, run every 10 mins.
        // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
        // Example - every 10 mins 0 0/10 * * * ?
        // At 5 past 2am on the first of every month - Sync stats
        cron name:'AdminTrigger', cronExpression: "30 0 7 * * ?"
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
        log.debug("Execute::AdminJob");
        if ( grailsApplication.config.hbzMaster == true  && grailsApplication.config.AdminReminderJobActiv == true) {
            log.debug("This server is marked as hbz master");
            AdminReminderService.AdminReminder();
        }
        else {
            log.debug("This server is NOT marked as hbz master. NOT Running AdminJob batch job");
        }
    }
}

