package de.laser.jobs

import de.laser.PendingChangeService
import de.laser.PendingChange
import de.laser.storage.RDStore
import de.laser.base.AbstractJob
import groovy.util.logging.Slf4j

@Slf4j
class ChangeAcceptJob extends AbstractJob {

  PendingChangeService pendingChangeService

  /*static triggers = {
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
    static List<List> configurationProperties = []

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

        if (! start('CAJ_JOB_START')) {
            return false
        }
        try {
            // Get all changes associated with slaved subscriptions
            String subQueryStr = "select pc.id from PendingChange as pc where subscription.isSlaved = true and ( pc.status is null or pc.status = :status ) order by pc.ts desc"
            def subPendingChanges = PendingChange.executeQuery(subQueryStr, [ status: RDStore.PENDING_CHANGE_PENDING ])
            log.debug(subPendingChanges.size() +" pending changes have been found for slaved subscriptions")

            //refactoring: replace link table with instanceOf
            //def licQueryStr = "select pc.id from PendingChange as pc join pc.license.incomingLinks lnk where lnk.isSlaved.value = 'Yes' and ( pc.status is null or pc.status = ? ) order by pc.ts desc"
            String licQueryStr = "select pc.id from PendingChange as pc where license.isSlaved = true and ( pc.status is null or pc.status = :status ) order by pc.ts desc"
            List licPendingChanges = PendingChange.executeQuery(licQueryStr, [ status: RDStore.PENDING_CHANGE_PENDING ])
            log.debug( licPendingChanges.size() +" pending changes have been found for slaved licenses")

            if (! pendingChangeService.performMultipleAcceptsForJob(subPendingChanges, licPendingChanges)) {
                log.warn( 'Failed. Maybe ignored due blocked pendingChangeService')
            }
        }
        catch (Exception e) {
            log.error( e.toString() )
        }
        stop('CAJ_JOB_COMPLETE')
    }
}