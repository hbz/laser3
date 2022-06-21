package de.laser.jobs

import de.laser.ChangeNotificationService
import de.laser.utils.ConfigMapper
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
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
    static List<List> configurationProperties = [ ConfigMapper.NOTIFICATIONS_JOB_ACTIVE ]

    boolean isAvailable() {
        !jobIsRunning && !changeNotificationService.running && ConfigMapper.getNotificationsJobActive()
    }
    boolean isRunning() {
        jobIsRunning
    }

    def execute() {
        if (! start()) {
            return false
        }
        try {
            if (! changeNotificationService.aggregateAndNotifyChanges()) {
                log.warn( 'NotificationsJob failed. Maybe ignored due blocked changeNotificationService' )
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop()
    }
}
