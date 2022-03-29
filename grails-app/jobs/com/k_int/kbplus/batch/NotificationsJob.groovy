package com.k_int.kbplus.batch

import com.k_int.kbplus.ChangeNotificationService
import de.laser.helper.ConfigUtils
import de.laser.quartz.AbstractJob

class NotificationsJob extends AbstractJob {

    ChangeNotificationService changeNotificationService

    /* ----> DISABLED
    
  static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // At zero seconds, 5 mins past 2am every day...
    cron name:'notificationsTrigger', cronExpression: "0 0 0/1 * * ?"
  }
    */
    static List<String> configFlags = ['notificationsJobActive']

    boolean isAvailable() {
        !jobIsRunning && !changeNotificationService.running
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
            log.debug("NotificationsJob")

            if (ConfigUtils.getNotificationsJobActive()) {
                if (! changeNotificationService.aggregateAndNotifyChanges()) {
                    log.warn( 'Failed. Maybe ignored due blocked changeNotificationService')
                }
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        jobIsRunning = false
    }
}
