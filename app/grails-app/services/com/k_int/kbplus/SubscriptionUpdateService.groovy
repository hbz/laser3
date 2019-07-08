package com.k_int.kbplus

import com.k_int.ClassUtils
import de.laser.SystemEvent
import de.laser.helper.RDStore
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

class SubscriptionUpdateService {

    def changeNotificationService
    def contextService

    /**
     * Cronjob-triggered.
     * Runs through all subscriptions having status "Intended" or "Current" and checks their dates:
     * - if state = planned, then check if start date is reached, if so: update to active, else do nothing
     * - else if state = active, then check if end date is reached, if so: update to terminated, else do nothing
     */
    void subscriptionCheck() {
        println "processing all intended subscriptions ..."
        def currentDate = new Date(System.currentTimeMillis())

        def updatedObjs = [:]

        // INTENDED -> CURRENT

        def intendedSubsIds1 = Subscription.where {
            status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate >= currentDate)
        }.collect{ it -> it.id }

        log.info("Intended subscriptions reached start date and are now running: " + intendedSubsIds1)

        if (intendedSubsIds1) {
            updatedObjs << ['intendedToCurrent' : intendedSubsIds1]

            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_CURRENT, ids: intendedSubsIds1]
            )

            log.debug("Writing events")
            intendedSubsIds1.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_CURRENT,
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
            updatedObjs << ['intendedToExpired' : intendedSubsIds2]

            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_EXPIRED, ids: intendedSubsIds2]
            )

            log.debug("Writing events")
            intendedSubsIds2.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED,
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
            updatedObjs << ['currentToExpired' : currentSubsIds]

            Subscription.executeUpdate(
                    'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                    [status: RDStore.SUBSCRIPTION_EXPIRED, ids: currentSubsIds]
            )

            log.debug("Writing events")
            currentSubsIds.each { id ->
                new EventLog(
                        event: 'SubscriptionUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED,
                        message: 'SQL Update',
                        tstp: currentDate
                ).save()
            }
        }

        SystemEvent.createEvent('SUB_UPDATE_SERVICE_PROCESSING', updatedObjs)

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
     * Refactors the preceding/following subscriptions to the new link model
     */
    int updateLinks() {
        int affected = 0
        def subsWithPrevious = Subscription.findAllByPreviousSubscriptionIsNotNull().collect { it -> [source:it.id,destination:it.previousSubscription.id] }
        subsWithPrevious.each { sub ->
            List<Links> linkList = Links.executeQuery('select l from Links as l where l.objectType = :subType and l.source = :source and l.destination = :destination and l.linkType = :linkType',[subType:Subscription.class.name,source:sub.source,destination:sub.destination,linkType:RDStore.LINKTYPE_FOLLOWS])
            if(linkList.size() == 0) {
                log.debug(sub.source+" follows "+sub.destination+", is being refactored")
                Links link = new Links()
                link.source = sub.source
                link.destination = sub.destination
                link.owner = Org.executeQuery('select o.org from OrgRole as o where o.roleType in :ownerRoles and o.sub in :context',[ownerRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER],context: [Subscription.get(sub.source),Subscription.get(sub.destination)]]).get(0)
                link.objectType = Subscription.class.name
                link.linkType = RDStore.LINKTYPE_FOLLOWS
                if(!link.save(flush:true))
                    log.error("error with refactoring subscription link: ${link.errors}")
                affected++
            }
            else if(linkList.size() > 0) {
                log.debug("Link already exists: ${sub.source} follows ${sub.destination} is/are link/s ##${linkList}")
            }
        }
        affected
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

    /**
     * Triggered from the Yoda menu
     * Loops through all IssueEntitlements and checks if there are inconcordances with their respective TIPPs. If so, and, if there is no pending change registered,
     * a new pending change is registered
     */
    void retriggerPendingChanges() {
        Org contextOrg = contextService.org
        Map<Subscription,Set<JSONElement>> currentPendingChanges = [:]
        List<PendingChange> list = PendingChange.findAllBySubscriptionIsNotNullAndStatus(RefdataValue.getByValueAndCategory('Pending','PendingChangeStatus'))
        list.each { pc ->
            Subscription subscription = ClassUtils.deproxy(pc.subscription)
            if(subscription.status != RDStore.SUBSCRIPTION_EXPIRED) {
                Set currSubChanges = currentPendingChanges.get(subscription) ?: []
                currSubChanges.add(JSON.parse(pc.changeDoc).changeDoc)
                currentPendingChanges.put(subscription,currSubChanges)
            }
        }
        //globalSourceSyncService.cleanUpGorm()
        log.debug("pending changes retrieved, go on with comparison ...")
        IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription sub where sub.instanceOf = null and ie.status != :deleted',[deleted:RDStore.TIPP_STATUS_DELETED]).eachWithIndex{ IssueEntitlement entry, int ctr ->
            if(entry.subscription.status != RDStore.SUBSCRIPTION_EXPIRED) {
                //log.debug("now processing ${entry.id} - ${entry.subscription.dropdownNamingConvention(contextOrg)}")
                boolean equivalent = false
                TitleInstancePackagePlatform underlyingTIPP = entry.tipp
                TitleInstancePackagePlatform.controlledProperties.each { cp ->
                    if(entry.hasProperty(cp) && entry."$cp") {
                        //log.debug("checking property ${cp} of ${underlyingTIPP.title.title} in subscription ${entry.subscription.name} ...")
                        if(cp == 'status') {
                            //temporary, I propose the merge of Entitlement Issue Status refdata category into TIPP Status
                            if(entry.status.value == 'Live' && underlyingTIPP.status.value == 'Current') {
                                equivalent = true
                            }
                        }
                        if(underlyingTIPP[cp] != entry[cp] && (entry[cp] != null && underlyingTIPP[cp] != '') && !equivalent) {
                            log.debug("difference registered at issue entitlement #${entry.id} - ${entry.subscription.dropdownNamingConvention(contextOrg)}, check if pending change exists ...")
                            //log.debug("setup change document")
                            Map changeDocument = [
                                    OID:"${underlyingTIPP.class.name}:${underlyingTIPP.id}",
                                    event:'TitleInstancePackagePlatform.updated',
                                    prop:cp,
                                    old:entry[cp],
                                    oldLabel:entry[cp].toString(),
                                    new:underlyingTIPP[cp],
                                    newLabel:underlyingTIPP[cp].toString()
                            ]
                            if(currentPendingChanges.get(entry.subscription)) {
                                Set registeredPC = currentPendingChanges.get(entry.subscription)
                                if(registeredPC.contains(changeDocument as JSONElement))
                                    log.debug("pending change found for ${entry.subscription.name}/${entry.tipp.title.title}'s ${cp} found , skipping ...")
                                else {
                                    log.debug("pending change not found - old ${cp}: ${entry[cp]} (${cp == 'status' ? entry[cp].owner.desc : ''}) vs. new ${cp}: ${underlyingTIPP[cp]} (${cp == 'status' ? underlyingTIPP[cp].owner.desc : ''})")
                                    underlyingTIPP.notifyDependencies_trait(changeDocument)
                                }
                            }
                            else {
                                log.debug("pending change not found, subscription has no pending changes - old ${cp}: ${entry[cp]} (${cp == 'status' ? entry[cp].owner.desc : ''}) vs. new ${cp}: ${underlyingTIPP[cp]} (${cp == 'status' ? underlyingTIPP[cp].owner.desc : ''})")
                                underlyingTIPP.notifyDependencies_trait(changeDocument)
                            }
                        }
                    }
                }
            }
        }
    }
}
