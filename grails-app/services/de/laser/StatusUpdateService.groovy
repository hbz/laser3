package de.laser

import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional
import org.springframework.transaction.TransactionStatus

/**
 * This service handles due date status updates for licenses and subscriptions
 * @see SurveyUpdateService
 */
@Transactional
class StatusUpdateService extends AbstractLockableService {

    def globalSourceSyncService
    def changeNotificationService
    def genericOIDService
    def contextService
     //def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

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
            Date currentDate = new Date()
            //Date currentDate = DateUtil.SDF_NoZ.parse("2020-05-30 03:00:00")

            Map<String,Object> updatedObjs = [:]

            // INTENDED -> CURRENT

            Set<Long> intendedSubsIds1 = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and s.isMultiYear = false',
            [status: RDStore.SUBSCRIPTION_INTENDED, currentDate: currentDate])

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

           Set<Long> intendedSubsIds2 = Subscription.executeQuery('select s.id from Subscription s left join s.instanceOf parent where s.status = :status and ((parent != null and parent.startDate < :currentDate) or '+
                   '(parent = null and s.startDate < :currentDate )) and s.isMultiYear = true',
                   [status: RDStore.SUBSCRIPTION_INTENDED, currentDate: currentDate])

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

            Set<Long> intendedSubsIds3 = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and (s.endDate != null and s.endDate < :currentDate) and s.isMultiYear = false',
                    [status: RDStore.SUBSCRIPTION_INTENDED,currentDate: currentDate])

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

            Set<Long> intendedSubsIds4 = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and (s.endDate != null and s.endDate < :currentDate) and s.isMultiYear = true',
                    [status: RDStore.SUBSCRIPTION_INTENDED, currentDate: currentDate])

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

            Set<Long> currentSubsIds = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and (s.endDate != null and s.endDate < :currentDate) and s.isMultiYear = false',
                    [status: RDStore.SUBSCRIPTION_CURRENT, currentDate: currentDate])

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

            Set<Long> currentSubsIds2 = Subscription.executeQuery('select s.id from Subscription s left join s.instanceOf parent where s.status = :status and s.startDate < :currentDate and (s.endDate != null and ((parent != null and parent.endDate < :currentDate) or s.endDate < :currentDate)) and s.isMultiYear = true',
                [status: RDStore.SUBSCRIPTION_CURRENT,currentDate: currentDate])

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

            /*
            def currentSubsIds3 = Subscription.where {
                status == RDStore.SUBSCRIPTION_CURRENT && instanceOf.startDate > currentDate && (endDate != null && (instanceOf.endDate > currentDate)) && isMultiYear == true
            }.collect{ it.id }

            log.info("Current subscriptions reached end date and are now intended perennial (${currentDate}): " + currentSubsIds3)

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
            Date currentDate = new Date()

            Map<String,Object> updatedObjs = [:]

            // INTENDED -> CURRENT

            Set<Long> intendedLicsIds1 = License.executeQuery('select l.id from License l where l.status = :status and (l.startDate != null and l.startDate < :currentDate) ',
                    [status: RDStore.LICENSE_INTENDED,currentDate: currentDate])

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

            Set<Long> currentLicsIds = License.executeQuery('select l.id from License l where l.status = :status and (l.startDate != null and l.startDate < :currentDate) and (l.endDate != null and l.endDate < :currentDate)',
                    [status: RDStore.LICENSE_CURRENT, currentDate: currentDate])

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

            Set<Long> intendedLicsIds2 = License.executeQuery('select l.id from License l where l.status = :status and (l.startDate != null and l.startDate < :currentDate) and (l.endDate != null and l.endDate < :currentDate)',
                    [status: RDStore.LICENSE_INTENDED, currentDate: currentDate])

            log.info("Intended licenses reached start and end date and are now expired (${currentDate}): " + intendedLicsIds2)

            if (intendedLicsIds2) {
                updatedObjs << ["intendedToExpired (${intendedLicsIds2.size()})" : intendedLicsIds2]

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

            return true
        }
        else {
            log.warn("License check already running ... not starting again.")
            return false
        }
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
     */
    void retriggerPendingChanges(String packageUUID) {
        Package pkg = Package.findByGokbId(packageUUID)
        Set<SubscriptionPackage> allSPs = SubscriptionPackage.findAllByPkg(pkg)
        //Set<SubscriptionPackage> allSPs = SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.subscription.status = :status and sp.pkg = :pkg and sp.subscription.instanceOf is null',[status:RDStore.SUBSCRIPTION_CURRENT,pkg:pkg]) //activate for debugging
        SubscriptionPackage.withTransaction { TransactionStatus stat ->
            allSPs.each { SubscriptionPackage sp ->
                //for session refresh
                Set<IssueEntitlement> currentIEs = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.status != :removed and ie.subscription = :sub and ie.tipp.pkg = :pkg',[sub:sp.subscription,pkg:pkg,removed:RDStore.TIPP_STATUS_REMOVED])
                //A and B are naming convention for A (old entity which is out of sync) and B (new entity with data up to date)
                currentIEs.eachWithIndex { IssueEntitlement ieA, int index ->
                    Map<String,Object> changeMap = [target:ieA.subscription]
                    String changeDesc
                    if(ieA.tipp.status != RDStore.TIPP_STATUS_REMOVED) {
                        TitleInstancePackagePlatform tippB = TitleInstancePackagePlatform.get(ieA.tipp.id) //for session refresh
                        Set<Map<String,Object>> diffs = globalSourceSyncService.getTippDiff(ieA,tippB)
                        diffs.each { Map<String,Object> diff ->
                            log.debug("now processing entry #${index}, payload: ${diff}")
                            if(diff.prop == 'coverage') {
                                //the city Coventry is beautiful, isn't it ... but here is the COVerageENTRY meant.
                                diff.covDiffs.each { covEntry ->
                                    def tippCov = covEntry.target
                                    switch(covEntry.event) {
                                        case 'update': IssueEntitlementCoverage ieCov = (IssueEntitlementCoverage) tippCov.findEquivalent(ieA.coverages)
                                            if(ieCov) {
                                                covEntry.diffs.each { covDiff ->
                                                    changeDesc = PendingChangeConfiguration.COVERAGE_UPDATED
                                                    changeMap.oid = genericOIDService.getOID(ieA)
                                                    changeMap.prop = covDiff.prop
                                                    changeMap.oldValue = ieCov[covDiff.prop]
                                                    changeMap.newValue = covDiff.newValue
                                                    changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                                                }
                                            }
                                            else {
                                                changeDesc = PendingChangeConfiguration.NEW_COVERAGE
                                                changeMap.oid = genericOIDService.getOID(tippCov)
                                                changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                                            }
                                            break
                                        case 'add':
                                            changeDesc = PendingChangeConfiguration.NEW_COVERAGE
                                            changeMap.oid = genericOIDService.getOID(tippCov)
                                            changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                                            break
                                        case 'delete':
                                            IssueEntitlementCoverage ieCov = (IssueEntitlementCoverage) tippCov.findEquivalent(ieA.coverages)
                                            if(ieCov) {
                                                changeDesc = PendingChangeConfiguration.COVERAGE_DELETED
                                                changeMap.oid = genericOIDService.getOID(ieCov)
                                                changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                                            }
                                            break
                                    }
                                }
                            }
                            else {
                                changeDesc = PendingChangeConfiguration.TITLE_UPDATED
                                changeMap.oid = genericOIDService.getOID(ieA)
                                changeMap.prop = diff.prop
                                if(diff.prop in PendingChange.REFDATA_FIELDS)
                                    changeMap.oldValue = ieA[diff.prop].id
                                else if(diff.prop in ['hostPlatformURL'])
                                    changeMap.oldValue = diff.oldValue
                                else
                                    changeMap.oldValue = ieA[diff.prop]
                                changeMap.newValue = diff.newValue
                                changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                            }
                        }
                    }
                    else {
                        changeDesc = PendingChangeConfiguration.TITLE_DELETED
                        changeMap.oid = genericOIDService.getOID(ieA)
                        changeNotificationService.determinePendingChangeBehavior(changeMap,changeDesc,sp)
                    }
                }
                Set<TitleInstancePackagePlatform> currentTIPPs = sp.subscription.issueEntitlements.collect { IssueEntitlement ie -> ie.tipp }
                Set<TitleInstancePackagePlatform> inexistentTIPPs = pkg.tipps.findAll { TitleInstancePackagePlatform tipp -> !currentTIPPs.contains(tipp) && tipp.status != RDStore.TIPP_STATUS_REMOVED }
                inexistentTIPPs.each { TitleInstancePackagePlatform tippB ->
                    log.debug("adding new TIPP ${tippB} to subscription ${sp.subscription.id}")
                    changeNotificationService.determinePendingChangeBehavior([target:sp.subscription,oid:genericOIDService.getOID(tippB)],PendingChangeConfiguration.NEW_TITLE,sp)
                }
                stat.flush()
                //sess.clear()
                // //propertyInstanceMap.get().clear()
            }
        }
    }

    /**
     * Triggered from the Yoda menu
     * Loops through all {@link Doc}ument objects without owner but with a {@link DocContext} for a {@link Subscription} or {@link License} and assigns the ownership
     * to the respective subscriber/licensee.
     */
    @Deprecated
    void assignNoteOwners() {
        Set<DocContext> docsWithoutOwner = DocContext.executeQuery('select dc from DocContext dc where dc.owner.owner = null and (dc.subscription != null or dc.license != null)')
        docsWithoutOwner.each { DocContext dc ->
            Org documentOwner
            if(dc.subscription) {
                if(dc.isShared) {
                    documentOwner = dc.subscription.getConsortia()
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
     * @deprecated Micha says new users should start from a clean environment, the feature is not required any more
     */
    @Deprecated
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
                if(!sub.save())
                    [sub,sub.errors]
            }
        }
        true
    }
}
