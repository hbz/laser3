package de.Laser.batch

import com.k_int.kbplus.EventLog
import de.laser.SystemEvent
import de.laser.quartz.AbstractJob

class SubscriptionUpdateJob extends AbstractJob {

  def cronjobUpdateService

  static triggers = {
    cron name:'SubscriptionUpdateTrigger', cronExpression: "0 0 3 * * ?" //Fire at 03:00 every day
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /15 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every 15th minute
//        cron name:'SubscriptionUpdateTrigger', cronExpression: "0 /3 * * * ?" //ONLY FOR DEVELOPMENT AND TESTS: Fire every three minutes
  }

    static configFlags = []

 def execute() {
   log.info("Execute::SubscriptionUpdateJob - Start");

   // TODO: remove due SystemEvent
   new EventLog(event:'Execute::SubscriptionUpdateJob', message:'Start', tstp:new Date(System.currentTimeMillis())).save(flush:true)

   SystemEvent.createEvent('SUB_UPDATE_JOB_START')

   cronjobUpdateService.subscriptionCheck()
   log.info("Execute::SubscriptionUpdateJob - Finished");

   SystemEvent.createEvent('SUB_UPDATE_JOB_COMPLETE')

   // TODO: remove due SystemEvent
   new EventLog(event:'Execute::SubscriptionUpdateJob', message:'Finished', tstp:new Date(System.currentTimeMillis())).save(flush:true)
 }

}
