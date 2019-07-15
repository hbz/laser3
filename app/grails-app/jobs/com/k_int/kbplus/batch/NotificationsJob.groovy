package com.k_int.kbplus.batch

import de.laser.quartz.AbstractJob

class NotificationsJob extends AbstractJob {

    def changeNotificationService
    def grailsApplication
    def reminderService

  static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // At zero seconds, 5 mins past 2am every day...
    cron name:'notificationsTrigger', cronExpression: "0 0/10 * * * ?"
  }

    static configFlags = ['hbzMaster']

    def execute() {
        log.debug("NotificationsJob");
        if ( grailsApplication.config.hbzMaster == true ) {
            log.debug("This server is marked as hbzMaster")
            changeNotificationService.aggregateAndNotifyChanges()

            log.debug("About to start the Reminders Job...")
            reminderService.runReminders()
        }
        else {
            log.debug("This server is NOT marked as hbzMaster .. nothing done")
        }
    }
}
