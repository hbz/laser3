package com.k_int.kbplus

class CronjobUpdateService {

    final INTENDED = RefdataValue.getByValueAndCategory('Intended','Subscription Status')
    final CURRENT =  RefdataValue.getByValueAndCategory('Current','Subscription Status')
    final EXPIRED =  RefdataValue.getByValueAndCategory('Expired','Subscription Status')

    /**
     * Cronjob-triggered.
     * Runs through all subscriptions having status "Intended" or "Current" and checks their dates:
     * - if state = planned, then check if start date is reached, if so: update to active, else do nothing
     * - else if state = active, then check if end date is reached, if so: update to terminated, else do nothing
     */
    void subscriptionCheck() {
        println "processing all intended subscriptions ..."
        Subscription.findAllByStatus(INTENDED).each { sub ->
            try {
                if (System.currentTimeMillis() > sub.startDate.time && System.currentTimeMillis() <= sub.endDate.time) {
                    log.info("Intended subscription with ID ${sub.id} has reached start date and is now running.")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: CURRENT, id: sub.id])
                    new EventLog(event:'CronjobUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + CURRENT, message:'SQL Update', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                } else if (System.currentTimeMillis() > sub.startDate.time && sub.endDate != null && System.currentTimeMillis() > sub.endDate.time) {
                    log.info("Intended subscription with ID ${sub.id} has reached start and end date and is now expired.")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: EXPIRED, id: sub.id])
                    new EventLog(event:'CronjobUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + EXPIRED, message:'SQL Update', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                }
            }
            catch (NullPointerException e) {
                log.info("Subscription with ID ${sub.id} has no start date.")
            }
        }
        println "processing all current subscriptions ..."
        Subscription.findAllByStatus(CURRENT).each { sub ->
            try {
                if (System.currentTimeMillis() > sub.startDate.time && sub.endDate != null && System.currentTimeMillis() > sub.endDate.time) {
                    log.info("Current subscription with ID ${sub.id} has reached end date and is now expired: ${sub.endDate.time} vs. ${System.currentTimeMillis()}")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: EXPIRED, id: sub.id])
                    new EventLog(event:'CronjobUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + EXPIRED, message:'SQL Update', tstp:new Date(System.currentTimeMillis())).save(flush:true)
                }
            }
            catch (NullPointerException e) {
                log.info("Subscription with ID ${sub.id} has no start date.")
            }
        }
    }
}
