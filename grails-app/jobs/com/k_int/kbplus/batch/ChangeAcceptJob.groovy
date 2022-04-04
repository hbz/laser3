package com.k_int.kbplus.batch

import com.k_int.kbplus.PendingChangeService
import de.laser.PendingChange
import de.laser.system.SystemEvent
import de.laser.helper.RDStore
import de.laser.base.AbstractJob

class ChangeAcceptJob extends AbstractJob {

  PendingChangeService pendingChangeService

  /*static triggers = {
   // Delay 20 seconds, run every 10 mins.
   // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
   // Example - every 10 mins 0 0/10 * * * ? 
   // At 5 past 3am every day
   cron name:'changeAcceptJobTrigger', startDelay:180000, cronExpression: "0 0 4 * * ?"
   // cronExpression: "s m h D M W Y"
   //                  | | | | | | `- Year [optional]
   //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
   //                  | | | | `- Month, 1-12 or JAN-DEC
   //                  | | | `- Day of Month, 1-31, ?
   //                  | | `- Hour, 0-23
   //                  | `- Minute, 0-59
   //                  `- Second, 0-59
 }*/
    static List<String> configFlags = []

    boolean isAvailable() {
        !jobIsRunning && !pendingChangeService.running
    }
    boolean isRunning() {
        jobIsRunning
    }

    /**
    * Accept pending changes from master subscriptions on slave subscriptions
    **/
    def execute(){
        if (! isAvailable()) {
            return false
        }
        jobIsRunning = true
        SystemEvent.createEvent('CAJ_JOB_START')

        try {
            // Get all changes associated with slaved subscriptions
            String subQueryStr = "select pc.id from PendingChange as pc where subscription.isSlaved = true and ( pc.status is null or pc.status = :status ) order by pc.ts desc"
            def subPendingChanges = PendingChange.executeQuery(subQueryStr, [ status: RDStore.PENDING_CHANGE_PENDING ])
            log.debug(subPendingChanges.size() +" pending changes have been found for slaved subscriptions")

            //refactoring: replace link table with instanceOf
            //def licQueryStr = "select pc.id from PendingChange as pc join pc.license.incomingLinks lnk where lnk.isSlaved.value = 'Yes' and ( pc.status is null or pc.status = ? ) order by pc.ts desc"
            String licQueryStr = "select pc.id from PendingChange as pc where license.isSlaved = true and ( pc.status is null or pc.status = :status ) order by pc.ts desc"
            def licPendingChanges = PendingChange.executeQuery(licQueryStr, [ status: RDStore.PENDING_CHANGE_PENDING ])
            log.debug( licPendingChanges.size() +" pending changes have been found for slaved licenses")

            if (! pendingChangeService.performMultipleAcceptsForJob(subPendingChanges, licPendingChanges)) {
                log.warn( 'Failed. Maybe ignored due blocked pendingChangeService')
            }

            log.debug("****Change Accept Job Complete*****")
        }
        catch (Exception e) {
            log.error( e.toString() )
        }

        SystemEvent.createEvent('CAJ_JOB_COMPLETE')

        jobIsRunning = false
    }
}