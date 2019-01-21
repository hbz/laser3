package com.k_int.kbplus

import de.laser.helper.RDStore

class SubscriptionUpdateService {

    /**
     * Cronjob-triggered.
     * Runs through all subscriptions having status "Intended" or "Current" and checks their dates:
     * - if state = planned, then check if start date is reached, if so: update to active, else do nothing
     * - else if state = active, then check if end date is reached, if so: update to terminated, else do nothing
     */
    void subscriptionCheck() {
        println "processing all intended subscriptions ..."
        def currentDate = new Date(System.currentTimeMillis())

        // INTENDED -> CURRENT

        def intendedSubsIds1 = Subscription.where {
            status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate >= currentDate)
        }.collect{ it -> it.id }

        log.info("Intended subscriptions reached start date and are now running: " + intendedSubsIds1)

        if (intendedSubsIds1) {
            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_CURRENT, ids: intendedSubsIds1]
            )

            log.debug("Writing events")
            intendedSubsIds1.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + CURRENT,
                        message: 'SQL Update',
                        tstp: currentDate
                ).save()
            }
        }

        // INTENDED -> EXPIRED

        def intendedSubsIds2 = Subscription.where {
            status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate < currentDate)
        }.collect{ it -> it.id }

        log.info("Intended subscriptions reached start date and end date are now expired: " + intendedSubsIds2)

        if (intendedSubsIds2) {
            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_EXPIRED, ids: intendedSubsIds2]
            )

            log.debug("Writing events")
            intendedSubsIds2.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + EXPIRED,
                        message: 'SQL Update',
                        tstp: currentDate
                ).save()
            }
        }

        // CURRENT -> EXPIRED

        def currentSubsIds = Subscription.where {
            status == RDStore.SUBSCRIPTION_CURRENT && startDate < currentDate && (endDate != null && endDate < currentDate)
        }.collect{ it -> it.id }

        log.info("Current subscriptions reached end date and are now expired: " + currentSubsIds)

        if (currentSubsIds) {
            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_EXPIRED, ids: currentSubsIds]
            )

            log.debug("Writing events")
            currentSubsIds.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + EXPIRED,
                        message: 'SQL Update',
                        tstp: currentDate
                ).save()
            }
        }


        /*
        Subscription.findAllByStatus(INTENDED).each { sub ->
            try {
                if (currentTime > sub.startDate.time && currentTime <= sub.endDate.time) {
                    log.info("Intended subscription with ID ${sub.id} has reached start date and is now running.")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: CURRENT, id: sub.id])
                    new EventLog(event:'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + CURRENT, message:'SQL Update', tstp:new Date(currentTime)).save(flush:true)
                }
                else if (currentTime > sub.startDate.time && sub.endDate != null && currentTime > sub.endDate.time) {
                    log.info("Intended subscription with ID ${sub.id} has reached start and end date and is now expired.")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: EXPIRED, id: sub.id])
                    new EventLog(event:'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + EXPIRED, message:'SQL Update', tstp:new Date(currentTime)).save(flush:true)
                }
            }
            catch (NullPointerException e) {
                log.info("Subscription with ID ${sub.id} has no start date.")
            }
        }
        println "processing all current subscriptions ..."
        Subscription.findAllByStatus(CURRENT).each { sub ->
            try {
                if (currentTime > sub.startDate.time && sub.endDate != null && currentTime > sub.endDate.time) {
                    log.info("Current subscription with ID ${sub.id} has reached end date and is now expired: ${sub.endDate.time} vs. ${currentTime}")
                    Subscription.executeUpdate('UPDATE Subscription sub SET sub.status =:status WHERE sub.id =:id',[status: EXPIRED, id: sub.id])
                    new EventLog(event:'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + sub.id + ' Status: ' + EXPIRED, message:'SQL Update', tstp:new Date(currentTime)).save(flush:true)
                }
            }
            catch (NullPointerException e) {
                log.info("Subscription with ID ${sub.id} has no start date.")
            }
        }*/

    }

    /**
     * Triggered from the Yoda menu
     * Refactors the preceding/following subscriptions to the new link model - to reset, delete the database table
     */
    void updateLinks() {
        def subsWithPrevious = Subscription.findAllByPreviousSubscriptionIsNotNull().collect { it -> [source:it.id,destination:it.previousSubscription.id] }
        subsWithPrevious.each { sub ->
            log.debug(sub.source+" follows "+sub.destination)
            Links link = new Links()
            link.source = sub.source
            link.destination = sub.destination
            link.owner = Org.executeQuery('select o.org from OrgRole as o where o.roleType in :ownerRoles and o.sub in :context',[ownerRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER],context: [Subscription.get(sub.source),Subscription.get(sub.destination)]]).get(0)
            link.objectType = Subscription.class.name
            link.linkType = RDStore.LINKTYPE_FOLLOWS
            if(!link.save(flush:true))
                log.error("error with refactoring subscription link: ${link.errors}")
        }
    }

    /**
     * Triggered from the Yoda menu
     * Sets the status of every subscription without start date to null as of ERMS-847
     */
    boolean startDateCheck() {
        def subsWithoutStartDate = Subscription.findAllByStartDateIsNullAndStatus(RDStore.SUBSCRIPTION_CURRENT).collect { it -> it.id }
        if(subsWithoutStartDate) {
            Subscription.executeUpdate('UPDATE Subscription SET status = null where id IN (:subs)',[subs:subsWithoutStartDate])
            log.debug("Writing events")
            log.info("${subsWithoutStartDate.size()} subscriptions affected")
            return true
        }
        else {
            return false
        }
    }

}
