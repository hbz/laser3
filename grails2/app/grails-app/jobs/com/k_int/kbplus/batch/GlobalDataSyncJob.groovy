package com.k_int.kbplus.batch

import de.laser.system.SystemEvent
import de.laser.helper.ConfigUtils
import de.laser.quartz.AbstractJob
import grails.core.GrailsApplication

class GlobalDataSyncJob extends AbstractJob {

    def globalSourceSyncService
    GrailsApplication grailsApplication

    static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // At 5 past 4am every day
    cron name:'globalDataSyncTrigger', startDelay:180000, cronExpression: "0 1 0 * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
    }

    static List<String> configFlags = ['globalDataSyncJobActiv']

    boolean isAvailable() {
        !jobIsRunning && !globalSourceSyncService.running
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
            log.debug("GlobalDataSyncJob");

            if ( ConfigUtils.getGlobalDataSyncJobActiv() ) {
                log.debug("Running GlobalDataSyncJob batch job")
                SystemEvent.createEvent('GD_SYNC_JOB_START')

                if (! globalSourceSyncService.startSync()) {
                    log.warn( 'Failed. Maybe ignored due blocked globalSourceSyncService')
                }

                SystemEvent.createEvent('GD_SYNC_JOB_COMPLETE')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        jobIsRunning = false
  }
}

