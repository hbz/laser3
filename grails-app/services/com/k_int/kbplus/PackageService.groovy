package com.k_int.kbplus

import de.laser.DeletionService
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.PendingChange
import de.laser.PendingChangeConfiguration
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.finance.CostItem
import de.laser.helper.RDStore
import de.laser.oap.OrgAccessPointLink
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import groovy.sql.Sql
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This service manages calls related to package management
 */
@Transactional
class PackageService {

    MessageSource messageSource
    DeletionService deletionService
    LinkGenerator grailsLinkGenerator
    boolean titleCleanupRunning = false

    /**
     * Lists conflicts which prevent an unlinking of the package from the given subscription
     * @param pkg the package to be unlinked
     * @param subscription the subscription from which package should be unlinked
     * @param numOfPCs the count of pending changes
     * @param numOfIEs the count of issue entitlements
     * @param numOfCIs the count of cost items linked either to the subscription package or to titles in them
     * @return a list of conflicts, each of them a map naming the conflict details when unlinking
     */
    List listConflicts(de.laser.Package pkg,subscription,int numOfPCs,int numOfIEs,int numOfCIs) {
        Locale locale = LocaleContextHolder.getLocale()
        Map<String,Object> conflict_item_pkg = [name: messageSource.getMessage("subscription.details.unlink.linkedPackage",null,locale),
                                                details: [['link': grailsLinkGenerator.link(controller: 'package', action: 'show', id: pkg.id), 'text': pkg.name]],
                                                action: [actionRequired: false, text: messageSource.getMessage("subscription.details.unlink.unlink.singular",null,locale)]]
        List conflicts_list = [conflict_item_pkg]
        if (numOfIEs > 0) {
            Map<String,Object> conflict_item_ie = [name: messageSource.getMessage("subscription.details.unlink.packageIEs",null,locale),
                                                   details: [[number: numOfIEs,'text': messageSource.getMessage("default.ie",null,locale)]],
                                                   action: [actionRequired: false, text: messageSource.getMessage("subscription.details.unlink.delete.plural",null,locale)]]
            conflicts_list += conflict_item_ie
        }
        if (numOfPCs > 0) {
            Map<String,Object> conflict_item_pc = [name: messageSource.getMessage("subscription.details.unlink.pendingChanges",null,locale),
                                                   details: [[number: numOfPCs, 'text': messageSource.getMessage("default.pendingchanges",null,locale)]],
                                                   action: [actionRequired: false, text: messageSource.getMessage("subscription.details.unlink.delete.plural",null,locale)]]
            conflicts_list += conflict_item_pc
        }
        if (numOfCIs > 0) {
            Map<String,Object> conflict_item_ci = [name: messageSource.getMessage("subscription.details.unlink.costItems",null,locale),
                                                   details: [[number: numOfCIs, 'text': messageSource.getMessage("financials.costItem",null,locale)]],
                                                   action: [actionRequired: true, text: messageSource.getMessage("subscription.details.unlink.delete.impossible.plural",null,locale)]]
            conflicts_list += conflict_item_ci
        }
        def sp
        if(subscription instanceof Subscription)
            sp = SubscriptionPackage.findByPkgAndSubscription(pkg, subscription)
        else if(subscription instanceof List<Subscription>)
            sp = SubscriptionPackage.findAllByPkgAndSubscriptionInList(pkg, subscription)
        if(sp) {
            List accessPointLinks = []
            if (sp.oapls){
                Map detailItem = [number: sp.oapls.size(),'text':messageSource.getMessage("default.accessPoints",null,locale)]
                accessPointLinks.add(detailItem)
            }
            if (accessPointLinks) {
                Map<String,Object> conflict_item_oap = [name: messageSource.getMessage("subscription.details.unlink.accessPoints",null,locale),
                                                        details: accessPointLinks,
                                                        action: [actionRequired: false, text: messageSource.getMessage("subscription.details.unlink.delete.plural",null,locale)]]
                conflicts_list += conflict_item_oap
            }
        }
        conflicts_list
    }

    /**
     * Gets the database IDs of the titles in the given package
     * @param pkg the package whose titles should be retrieved
     * @return a set of database IDs
     */
    Set<Long> getCurrentTippIDs(de.laser.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.status = :current and tipp.pkg = :pkg',[current: RDStore.TIPP_STATUS_CURRENT, pkg: pkg])
    }

    /**
     * Gets the count of titles in the package which are not marked as deleted in the given package
     * @param pkg the package whose titles should be counted
     * @return a count of non-deleted titles in the package
     */
    Long getCountOfNonDeletedTitles(de.laser.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select count(tipp.id) from TitleInstancePackagePlatform tipp where tipp.status != :removed and tipp.pkg = :pkg',[removed: RDStore.TIPP_STATUS_REMOVED, pkg: pkg])[0]
    }

    /**
     * Adds the given set of package titles, retrieved by native database query, to the given subscription. Insertion as issue entitlements is being done by native SQL as well as it performs much better than GORM
     * @param sql the SQL connection, established at latest in the calling method
     * @param subId the ID of the subscription whose holding should be enriched by the given title set
     * @param pkgId the ID of the package whose holding should be added to the subscription
     * @param hasPerpetualAccess the flag whether the title access have been purchased perpetually
     */
    void bulkAddHolding(Sql sql, Long subId, Long pkgId, boolean hasPerpetualAccess) {
        String perpetualAccessCol = '', perpetualAccessColHeader = ''
        if(hasPerpetualAccess) {
            perpetualAccessColHeader = ', ie_perpetual_access_by_sub_fk'
            perpetualAccessCol = ", ${subId}"
        }

        sql.executeInsert("insert into issue_entitlement (ie_version, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_reason, ie_medium_rv_fk, ie_status_rv_fk, ie_accept_status_rv_fk, ie_name, ie_sortname ${perpetualAccessColHeader}) " +
                "select 0, now(), now(), ${subId}, tipp_id, tipp_access_start_date, tipp_access_end_date, 'manually added by user', tipp_medium_rv_fk, tipp_status_rv_fk, ${RDStore.IE_ACCEPT_STATUS_FIXED.id}, tipp_name, tipp_sort_name ${perpetualAccessCol} from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
        sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo) " +
                "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), tc_start_date, tc_start_volume, tc_start_issue, tc_end_date, tc_end_volume, tc_end_issue, tc_coverage_depth, tc_coverage_note, tc_embargo from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
        sql.executeInsert("insert into price_item (version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid, pi_list_currency_rv_fk, pi_list_price) " +
                "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_currency_rv_fk, pi_list_price from price_item join title_instance_package_platform on pi_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
    }

    /**
     * Unlinks a subscription from the given package and removes resp. marks as delete every dependent object from that link such as cost items, pending change configurations etc.
     * The unlinking can be done iff no cost items are linked to the (subscription) package
     * @param subscription the {@link Subscription} from which the package should be detached
     * @param contextOrg the {@link de.laser.Org} whose cost items should be verified
     * @param deleteEntitlements should the linked entitlements being deleted?
     * @return true if the unlink was successful, false otherwise
     */
    boolean unlinkFromSubscription(de.laser.Package pkg, Subscription subscription, Org contextOrg, deleteEntitlements) {
        SubscriptionPackage subPkg = SubscriptionPackage.findByPkgAndSubscription(pkg, subscription)

        //Not Exist CostItem with Package
        if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.subscription = :sub and ci.subPkg.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted',[pkg:pkg, deleted: RDStore.COST_ITEM_DELETED, sub:subscription, context: contextOrg])) {

            if (deleteEntitlements) {
                List<Long> subList = [subscription.id]
                Map<String,Object> queryParams = [sub: subList, pkg_id: pkg.id]
                //delete matches
                //IssueEntitlement.withSession { Session session ->
                String updateQuery = "update IssueEntitlement ie set ie.status.id = case when ((select tipp.status.id from IssueEntitlement et join et.tipp tipp where et.id = ie.id) = ${RDStore.TIPP_STATUS_REMOVED.id}) then ${RDStore.TIPP_STATUS_REMOVED.id} else ${RDStore.TIPP_STATUS_DELETED.id} end where ie.subscription.id in (:sub) and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
                IssueEntitlement.executeUpdate(updateQuery, queryParams)
                removePackagePendingChanges(pkg, subList, true)

                SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription.id in (:subList)',[subList:subList,pkg:pkg]).each { SubscriptionPackage delPkg ->
                    OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :subPkg", [subPkg:delPkg])
                    CostItem.executeUpdate('update CostItem ci set ci.costItemStatus = :deleted, ci.subPkg = null, ci.sub = :sub where ci.subPkg = :delPkg',[delPkg: delPkg, sub:delPkg.subscription, deleted: RDStore.COST_ITEM_DELETED])
                    PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:delPkg])
                }

                SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg=:pkg and sp.subscription.id in (:subList)", [pkg:pkg, subList:subList])
                //log.debug("before flush")
                //session.flush()
                return true
                //}
            } else {

                if (subPkg) {
                    OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :sp", [sp: subPkg])
                    CostItem.findAllBySubPkg(subPkg).each { costItem ->
                        costItem.subPkg = null
                        costItem.costItemStatus = RDStore.COST_ITEM_DELETED
                        if (!costItem.sub) {
                            costItem.sub = subPkg.subscription
                        }
                        costItem.save()
                    }
                    PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:subPkg])
                }

                SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription = :sub ", [pkg: pkg, sub:subscription])
                return true
            }
        }else{
            log.error("!!! unlinkFromSubscription fail: CostItems are still linked -> [pkg:${pkg},sub:${subscription}]!!!!")
            return false
        }
    }



    /**
     * Clears the changes pending on the given package
     * @param subIds the {@link List} of {@link Subscription} identifiers to be checked
     * @param confirmed should the deletion really be executed?
     * @return the number of deleted entries
     */
    int removePackagePendingChanges(de.laser.Package pkg, List subIds, boolean confirmed) {
        int count = 0
        List<Long> tippIDs = pkg.tipps.id
        if(confirmed) {
            count = PendingChange.executeUpdate('delete from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs) and pc.payload is null))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])
            List oldStylePCs = PendingChange.executeQuery('select pc.id, pc.payload from PendingChange pc where pc.subscription.id in (:subIds)', [subIds: subIds])
            List<Long> pcsToDelete = []
            oldStylePCs.eachWithIndex { pc, int i ->
                def payload = JSON.parse(pc[1])
                if (payload.tippID) {
                    pcsToDelete << pc[0]
                }else if (payload.tippId) {
                    pcsToDelete << pc[0]
                } else if (payload.changeDoc) {
                    def (oid_class, ident) = payload.changeDoc.OID.split(":")
                    if (oid_class == TitleInstancePackagePlatform.class.name && tippIDs.contains(ident.toLong())) {
                        pcsToDelete << pc[0]
                    }
                } else {
                    log.error("Could not decide if we should delete the pending change id:${pc[0]} - ${payload}")
                }
                if(i % 32500 == 0 && i > 0) {
                    count += PendingChange.executeUpdate("delete from PendingChange where id in (:del_list)", [del_list: pcsToDelete])
                    pcsToDelete.clear()
                }
            }
        }
        else {
            if(subIds) {
                count = PendingChange.executeQuery('select count(pc.id) from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs) and pc.payload is null))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])[0]
                List oldStylePCs = PendingChange.executeQuery('select pc.payload from PendingChange pc where pc.subscription.id in (:subIds)', [subIds: subIds])
                oldStylePCs.each { String pc
                    def payload = JSON.parse(pc)
                    if (payload.tippID) {
                        count++
                    }else if (payload.tippId) {
                        count++
                    } else if (payload.changeDoc) {
                        def (oid_class, ident) = payload.changeDoc.OID.split(":")
                        if (oid_class == TitleInstancePackagePlatform.class.name && tippIDs.contains(ident.toLong())) {
                            count++
                        }
                    } else {
                        log.error("Could not decide if we should delete the pending change id:${pc} - ${payload}")
                    }
                }
            }
        }
        count
        //log.debug("begin remove pending changes")
        /*
        String tipp_class = TitleInstancePackagePlatform.class.getName()
        List<Long> tipp_ids = TitleInstancePackagePlatform.executeQuery(
                "select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId", [pkgId: pkg.id]
        )
        List pendingChanges = subIds ? PendingChange.executeQuery(
                "select pc.id, pc.payload from PendingChange pc where pc.subscription.id in (:subIds)" , [subIds: subIds]
        ) : []

        List pc_to_delete = []
        pendingChanges.each { pc ->
            //log.debug("begin pending changes")
            if(pc[1]){
                def payload = JSON.parse(pc[1])
                if (payload.tippID) {
                    pc_to_delete << pc[0]
                }else if (payload.tippId) {
                    pc_to_delete << pc[0]
                } else if (payload.changeDoc) {
                    def (oid_class, ident) = payload.changeDoc.OID.split(":")
                    if (oid_class == tipp_class && tipp_ids.contains(ident.toLong())) {
                        pc_to_delete << pc[0]
                    }
                } else {
                    log.error("Could not decide if we should delete the pending change id:${pc[0]} - ${payload}")
                }
            }
            else {
                pc_to_delete << pc[0]
            }
        }
        if (confirmed && pc_to_delete) {
            String del_pc_query = "delete from PendingChange where id in (:del_list) "
            if(pc_to_delete.size() > 32766) { //cf. https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
                pc_to_delete.collate(32766).each { subList ->
                    log.debug("Deleting Pending Change Slice: ${subList}")
                    executeUpdate(del_pc_query,[del_list:subList])
                }
            }
            else {
                log.debug("Deleting Pending Changes: ${pc_to_delete}")
                executeUpdate(del_pc_query, [del_list: pc_to_delete])
            }
        } else {
            return pc_to_delete.size()
        }
         */
    }

    boolean clearRemovedTitles() {
        if(!titleCleanupRunning) {
            titleCleanupRunning = true
            Set<TitleInstancePackagePlatform> titles = TitleInstancePackagePlatform.executeQuery("select pc.tipp from PendingChange pc where pc.msgToken = :titleRemoved and not exists(select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status != :removed)",[titleRemoved: PendingChangeConfiguration.TITLE_REMOVED, removed: RDStore.TIPP_STATUS_REMOVED])
            //the query above is to ensure that no issue entitlements are going to be removed before!
            if(titles) {
                deletionService.deleteTIPPsCascaded(titles)
            }
            titleCleanupRunning = false
            return true
        }
        else return false
    }
}