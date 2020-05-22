package com.k_int.kbplus

import com.k_int.ClassUtils
import de.laser.SystemEvent
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import de.laser.interfaces.CalculatedType
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

class StatusUpdateService extends AbstractLockableService {

    def changeNotificationService
    def contextService

    /**
     * Cronjob-triggered.
     * Runs through all subscriptions having status "Intended" or "Current" and checks their dates:
     * - if state = planned, then check if start date is reached, if so: update to active, else do nothing
     * - else if state = active, then check if end date is reached, if so: update to terminated, else do nothing
     */
    boolean subscriptionCheck() {
        if(!running) {
            running = true
            println "processing all intended subscriptions ..."
            def currentDate = new Date(System.currentTimeMillis())

            def updatedObjs = [:]

            // INTENDED -> CURRENT

            def intendedSubsIds1 = Subscription.where {
                status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate >= currentDate) && isMultiYear == false
            }.collect{ it.id }

            log.info("Intended subscriptions reached start date and are now running (${currentDate}): " + intendedSubsIds1)

            if (intendedSubsIds1) {
                updatedObjs << ["intendedToCurrent (${intendedSubsIds1.size()})" : intendedSubsIds1]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_CURRENT, ids: intendedSubsIds1]
                )

                intendedSubsIds1.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_CURRENT)
                }
            }

            // MultiYear Sub INTENDED -> CURRENT

           def intendedSubsIds2 = Subscription.where {
                status == RDStore.SUBSCRIPTION_INTENDED &&
                        ((instanceOf != null && instanceOf.startDate < currentDate && instanceOf.endDate != null && instanceOf.endDate >= currentDate)
                                || (instanceOf == null && startDate < currentDate && endDate != null && endDate >= currentDate)) && isMultiYear == true
            }.collect{ it.id }

            log.info("Intended perennial subscriptions reached start date and are now running (${currentDate}): " + intendedSubsIds2)

            if (intendedSubsIds2) {
                updatedObjs << ["MultiYear_intendedToCurrent (${intendedSubsIds2.size()})" : intendedSubsIds2]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_CURRENT, ids: intendedSubsIds2]
                )

                intendedSubsIds2.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_CURRENT)
                }
            }

            // INTENDED -> EXPIRED

            def intendedSubsIds3 = Subscription.where {
                status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate < currentDate) && isMultiYear == false
            }.collect{ it.id }

            log.info("Intended subscriptions reached start date and end date are now expired (${currentDate}): " + intendedSubsIds3)

            if (intendedSubsIds3) {
                updatedObjs << ["intendedToExpired (${intendedSubsIds3.size()})" : intendedSubsIds3]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_EXPIRED, ids: intendedSubsIds3]
                )

                intendedSubsIds3.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED)
                }
            }

            // MultiYear Sub INTENDED -> EXPIRED

            def intendedSubsIds4 = Subscription.where {
                status == RDStore.SUBSCRIPTION_INTENDED && startDate < currentDate && (endDate != null && endDate < currentDate) && isMultiYear == true
            }.collect{ it.id }

            log.info("Intended subscriptions reached start date and end date are now expired pernennial (${currentDate}): " + intendedSubsIds4)

            if (intendedSubsIds4) {
                updatedObjs << ["MultiYear_intendedToExpired (${intendedSubsIds4.size()})" : intendedSubsIds4]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_EXPIRED, ids: intendedSubsIds4]
                )

                intendedSubsIds4.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED)
                }
            }

            // CURRENT -> EXPIRED

            def currentSubsIds = Subscription.where {
                status == RDStore.SUBSCRIPTION_CURRENT && startDate < currentDate && (endDate != null && endDate < currentDate) && isMultiYear == false
            }.collect{ it.id }

            log.info("Current subscriptions reached end date and are now expired (${currentDate}): " + currentSubsIds)

            if (currentSubsIds) {
                updatedObjs << ["currentToExpired (${currentSubsIds.size()})" : currentSubsIds]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_EXPIRED, ids: currentSubsIds]
                )

                currentSubsIds.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED)
                }
            }

            // MultiYear Sub CURRENT -> EXPIRED

            def currentSubsIds2 = Subscription.where {
                status == RDStore.SUBSCRIPTION_CURRENT && startDate < currentDate && (endDate != null && (instanceOf.endDate < currentDate || endDate < currentDate)) && isMultiYear == true
            }.collect{ it.id }

            log.info("Current subscriptions reached end date and are now expired (${currentDate}): " + currentSubsIds2)

            if (currentSubsIds2) {
                updatedObjs << ["MultiYear_currentPerennialToExpired (${currentSubsIds2.size()})" : currentSubsIds2]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_EXPIRED, ids: currentSubsIds2]
                )

                currentSubsIds2.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_EXPIRED)
                }
            }

            // CURRENT PERENNIAL -> INTENDED PERENNIAL

            /**
            def currentSubsIds3 = Subscription.where {
                status == RDStore.SUBSCRIPTION_CURRENT && instanceOf.startDate > currentDate && (endDate != null && (instanceOf.endDate > currentDate)) && isMultiYear == true
            }.collect{ it.id }

            log.info("Current subscriptions reached end date and are now intended pernennial (${currentDate}): " + currentSubsIds3)

            if (currentSubsIds3) {
                updatedObjs << ['currentPerennialToIntendedPerennial' : currentSubsIds3]

                Subscription.executeUpdate(
                        'UPDATE Subscription sub SET sub.status =:status WHERE sub.id in (:ids)',
                        [status: RDStore.SUBSCRIPTION_INTENDED_PERENNIAL, ids: currentSubsIds3]
                )

                currentSubsIds3.each { id ->
                    log.info('StatusUpdateService UPDATE subscriptions WHERE ID ' + id + ' Status: ' + RDStore.SUBSCRIPTION_INTENDED_PERENNIAL)
                }
            }**/

            SystemEvent.createEvent('SUB_UPDATE_SERVICE_PROCESSING', updatedObjs)
            running = false

            return true
        }
        else {
            log.warn("Subscription check already running ... not starting again.")
            return false
        }
    }

    /**
     * Cronjob-triggered.
     * Runs through all subscriptions having status "Intended" or "Current" and checks their dates:
     * - if state = planned, then check if start date is reached, if so: update to active, else do nothing
     * - else if state = active, then check if end date is reached, if so: update to terminated, else do nothing
     */
    boolean licenseCheck() {
        if(!running) {
            running = true
            println "processing all intended licenses ..."
            Date currentDate = new Date(System.currentTimeMillis())

            Map<String,Object> updatedObjs = [:]

            // INTENDED -> CURRENT

            Set<Long> intendedLicsIds1 = License.where {
                status == RDStore.LICENSE_INTENDED && (startDate != null && startDate < currentDate) && (endDate != null && endDate >= currentDate)
            }.collect{ it.id }

            log.info("Intended licenses reached start date and are now running (${currentDate}): " + intendedLicsIds1)

            if (intendedLicsIds1) {
                updatedObjs << ["intendedToCurrent (${intendedLicsIds1.size()})" : intendedLicsIds1]

                Subscription.executeUpdate(
                        'UPDATE License lic SET lic.status =:status WHERE lic.id in (:ids)',
                        [status: RDStore.LICENSE_CURRENT, ids: intendedLicsIds1]
                )

                intendedLicsIds1.each { id ->
                    log.info('StatusUpdateService UPDATE license WHERE ID ' + id + ' Status: ' + RDStore.LICENSE_CURRENT)
                }
            }

            // CURRENT -> EXPIRED

            Set<Long> currentLicsIds = License.where {
                status == RDStore.LICENSE_CURRENT && (startDate != null && startDate < currentDate) && (endDate != null && endDate < currentDate)
            }.collect{ it.id }

            log.info("Current licenses reached end date and are now expired (${currentDate}): " + currentLicsIds)

            if (currentLicsIds) {
                updatedObjs << ["currentToExpired (${currentLicsIds.size()})" : currentLicsIds]

                Subscription.executeUpdate(
                        'UPDATE License lic SET lic.status =:status WHERE lic.id in (:ids)',
                        [status: RDStore.LICENSE_EXPIRED, ids: currentLicsIds]
                )

                currentLicsIds.each { id ->
                    log.info('StatusUpdateService UPDATE license WHERE ID ' + id + ' Status: ' + RDStore.LICENSE_EXPIRED)
                }
            }

            // INTENDED -> EXPIRED

            Set<Long> intendedLicsIds2 = License.where {
                status == RDStore.LICENSE_INTENDED && (startDate != null && startDate < currentDate) && (endDate != null && endDate < currentDate)
            }.collect{ it.id }

            log.info("Intended licenses reached start and end date and are now expired (${currentDate}): " + intendedLicsIds2)

            if (intendedLicsIds2) {
                updatedObjs << ["currentToExpired (${intendedLicsIds2.size()})" : intendedLicsIds2]

                Subscription.executeUpdate(
                        'UPDATE License lic SET lic.status =:status WHERE lic.id in (:ids)',
                        [status: RDStore.LICENSE_EXPIRED, ids: intendedLicsIds2]
                )

                intendedLicsIds2.each { id ->
                    log.info('StatusUpdateService UPDATE license WHERE ID ' + id + ' Status: ' + RDStore.LICENSE_EXPIRED)
                }
            }

            SystemEvent.createEvent('LIC_UPDATE_SERVICE_PROCESSING', updatedObjs)
            running = false
        }
        else {
            log.warn("License check already running ... not starting again.")
            return false
        }
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
        def subsWithoutStartDate = Subscription.findAllByStartDateIsNullAndStatus(RDStore.SUBSCRIPTION_CURRENT).collect { it.id }
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
     * !!!! BEWARE !!!! The modifications for ERMS-1581 are NOT implemented! This method does thus not work!
     */
    void retriggerPendingChanges() {
        Org contextOrg = contextService.org
        Map<Subscription,Set<JSONElement>> currentPendingChanges = [:]
        List<PendingChange> list = PendingChange.findAllBySubscriptionIsNotNullAndStatus(RefdataValue.getByValueAndCategory('Pending', RDConstants.PENDING_CHANGE_STATUS))
        list.each { pc ->
            Subscription subscription = ClassUtils.deproxy(pc.subscription)
            if(subscription.status != RDStore.SUBSCRIPTION_EXPIRED) {
                Set currSubChanges = currentPendingChanges.get(subscription) ?: []
                currSubChanges.add(JSON.parse(pc.payload).changeDoc)
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

    /**
     * Triggered from the Yoda menu
     * Loops through all {@link Doc}ument objects without owner but with a {@link DocContext} for a {@link Subscription} or {@link License} and assigns the ownership
     * to the respective subscriber/licensee.
     */
    void assignNoteOwners() {
        Set<DocContext> docsWithoutOwner = DocContext.executeQuery('select dc from DocContext dc where dc.owner.owner = null and (dc.subscription != null or dc.license != null)')
        docsWithoutOwner.each { DocContext dc ->
            Org documentOwner
            if(dc.subscription) {
                if(dc.isShared) {
                    if(dc.subscription.getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
                        documentOwner = dc.subscription.getConsortia()
                    else if(dc.subscription.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE)
                        documentOwner = dc.subscription.getCollective()
                }
                else
                    documentOwner = dc.subscription.getSubscriber()
                log.debug("now processing note ${dc.owner.id} for subscription ${dc.subscription.id} whose probable owner is ${documentOwner}")
            }
            else if(dc.license) {
                documentOwner = dc.license.getLicensee()
                log.debug("now processing note ${dc.owner.id} for license ${dc.license.id} whose probable owner is ${documentOwner}")
            }
            if(documentOwner) {
                dc.owner.owner = documentOwner
                dc.owner.save()
            }
        }
    }

    /**
     * Triggered from the Admin menu
     * Takes every test subscription, processes them by name and counts their respective years up by one
     */
    def updateQASubscriptionDates() {
        //if someone changes the names, we are all screwed up since UUIDs may not persist when database is changed!
        List<String> names = ['Daten A (Test)','Daten A', 'E-Book-Pick', 'Journal-Paket', 'Journal-Paket_Extrem', 'Journal-Paket_extrem', 'Musterdatenbank', 'Datenbank', 'Datenbank 2']
        names.each { name ->
            List<Subscription> subscriptions = Subscription.findAllByName(name)
            subscriptions.each { sub ->
                log.debug("updating ${sub.name} of ${sub.getSubscriber()}")
                Calendar cal = Calendar.getInstance()
                List<String> dateFieldKeys = ['startDate','endDate','manualCancellationDate']
                dateFieldKeys.each { String field ->
                    if(sub[field]) {
                        cal.setTime(sub[field])
                        cal.add(Calendar.YEAR, 1)
                        sub[field] = cal.getTime()
                    }
                }
                if(!sub.save(flush:true)) //damn it! But here, it is unavoidable.
                    [sub,sub.errors]
            }
        }
        true
    }
}
