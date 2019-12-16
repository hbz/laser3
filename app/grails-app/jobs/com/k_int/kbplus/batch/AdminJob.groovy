package com.k_int.kbplus.batch

import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class AdminJob extends AbstractJob {
    def grailsApplication
    def adminReminderService

    /* --> DISABLED
    static triggers = {
        // Delay 20 seconds, run every 10 mins.
        // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
        // Example - every 10 mins 0 0/10 * * * ?
        // At 5 past 2am on the first of every month - Sync stats
        cron name:'AdminTrigger', cronExpression: "0 0 7 * * ?"
        // cronExpression: "s m h D M W Y"
        //                  | | | | | | `- Year [optional]
        //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
        //                  | | | | `- Month, 1-12 or JAN-DEC
        //                  | | | `- Day of Month, 1-31, ?
        //                  | | `- Hour, 0-23
        //                  | `- Minute, 0-59
        //                  `- Second, 0-59
    }*/

    static configFlags = ['hbzMaster', 'AdminReminderJobActiv']

    boolean isAvailable() {
        !jobIsRunning && !adminReminderService.running
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
            log.debug("Execute::AdminJob")

            if (grailsApplication.config.hbzMaster == true && grailsApplication.config.AdminReminderJobActiv == true) {
                log.debug("This server is marked as hbzMaster");
                SystemEvent.createEvent('ADM_JOB_START')

                if (! adminReminderService.adminReminder()) {
                    log.warn( 'Failed. Maybe ignored due blocked adminReminderService')
                }
            }
            else {
                log.debug("This server is NOT marked as hbzMaster. NOT Running AdminJob batch job")
            }
        }
        catch (Exception e) {
            log.error(e)
        }

        jobIsRunning = false
    }
}

