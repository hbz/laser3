package de.laser

import de.laser.finance.CostItem
import de.laser.oap.OrgAccessPointLink
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import groovy.sql.Sql
import org.springframework.context.MessageSource

/**
 * This service manages calls related to package management
 */
@Transactional
class PackageService {

    BatchUpdateService batchUpdateService
    ContextService contextService
    DeletionService deletionService
    LinkGenerator grailsLinkGenerator
    MessageSource messageSource

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
        Locale locale = LocaleUtils.getCurrentLocale()
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
    Long getCountOfCurrentTippIDs(de.laser.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select count(*) from TitleInstancePackagePlatform tipp where tipp.status = :current and tipp.pkg = :pkg',[current: RDStore.TIPP_STATUS_CURRENT, pkg: pkg])[0]
    }

    /**
     * Gets the count of titles in the package which are not marked as deleted in the given package
     * @param pkg the package whose titles should be counted
     * @return a count of non-deleted titles in the package
     */
    Long getCountOfNonDeletedTitles(de.laser.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select count(*) from TitleInstancePackagePlatform tipp where tipp.status != :removed and tipp.pkg = :pkg',[removed: RDStore.TIPP_STATUS_REMOVED, pkg: pkg])[0]
    }

    /**
     * Adds the given set of package titles, retrieved by native database query, to the given subscription. Insertion as issue entitlements is being done by native SQL as well as it performs much better than GORM
     * @param sql the SQL connection, established at latest in the calling method
     * @param subId the ID of the subscription whose holding should be enriched by the given title set
     * @param pkgId the ID of the package whose holding should be added to the subscription
     * @param hasPerpetualAccess the flag whether the title access have been purchased perpetually
     */
    void bulkAddHolding(Sql sql, Long subId, Long pkgId, boolean hasPerpetualAccess, Long consortiumId = null, Long sourceSubId = null) {
        String perpetualAccessCol = '', perpetualAccessColHeader = ''
        if(hasPerpetualAccess) {
            perpetualAccessColHeader = ', ie_perpetual_access_by_sub_fk'
            perpetualAccessCol = ", ${subId}"
        }
        if(consortiumId) {
            sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk ${perpetualAccessColHeader}) " +
                    "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${subId}, tipp_id, tipp_access_start_date, tipp_access_end_date, tipp_status_rv_fk ${perpetualAccessCol} from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and tipp_id in (select ie_tipp_fk from issue_entitlement where ie_subscription_fk = :consortiumId and ie_status_rv_fk != :removed) and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, consortiumId: consortiumId])
            sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated) " +
                    "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now() from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and tipp_id in (select ie_tipp_fk from issue_entitlement where ie_subscription_fk = :consortiumId and ie_status_rv_fk != :removed) and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, consortiumId: consortiumId])
        }
        else if(sourceSubId) {
            sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_notes ${perpetualAccessColHeader}) " +
                    "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${subId}, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_notes ${perpetualAccessCol} from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :sourceSubId and ie_status_rv_fk != :removed and tipp_pkg_fk = :pkgId and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, sourceSubId: sourceSubId])
            sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_date_created, ic_last_updated) " +
                    "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, now(), now() from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :sourceSubId and tipp_pkg_fk = :pkgId and ie_status_rv_fk != :removed and not exists(select ic_id from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id where ic_ie_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, sourceSubId: sourceSubId])
            sql.executeInsert("insert into price_item (pi_version, pi_ie_fk, pi_local_price, pi_local_currency_rv_fk, pi_date_created, pi_last_updated, pi_guid) " +
                    "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), pi_local_price, pi_local_currency_rv_fk, now(), now(), concat('priceitem:',gen_random_uuid()) from price_item join issue_entitlement on pi_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :sourceSubId and tipp_pkg_fk = :pkgId and ie_status_rv_fk != :removed and not exists(select pi_id from price_item join issue_entitlement on pi_ie_fk = ie_id where pi_ie_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, sourceSubId: sourceSubId])
        }
        else {
            sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk ${perpetualAccessColHeader}) " +
                    "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${subId}, tipp_id, tipp_access_start_date, tipp_access_end_date, tipp_status_rv_fk ${perpetualAccessCol} from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
            sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated) " +
                    "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now() from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
            sql.executeInsert("insert into price_item (pi_version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid) " +
                    "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), concat('priceitem:',gen_random_uuid()) from price_item join title_instance_package_platform on pi_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and not exists(select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk != :removed)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
        }
        if(hasPerpetualAccess) {
            Long ownerId = Subscription.get(subId).getSubscriber().id
            sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), "+subId+", now(), ie_tipp_fk, "+ownerId+" from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])
        }
    }

    boolean unlinkFromSubscription(de.laser.Package pkg, Subscription subscription, Org contextOrg, deletePackage) {
        unlinkFromSubscription(pkg, [subscription.id], contextOrg, deletePackage)
    }

    /**
     * Unlinks a subscription from the given package and removes resp. marks as delete every dependent object from that link such as cost items, pending change configurations etc.
     * The unlinking can be done iff no cost items are linked to the (subscription) package
     * @param subscription the {@link Subscription} from which the package should be detached
     * @param contextOrg the {@link de.laser.Org} whose cost items should be verified
     * @param deletePackage should the package be unlinked, too?
     * @return true if the unlink was successful, false otherwise
     */
    boolean unlinkFromSubscription(de.laser.Package pkg, List<Long> subList, Org contextOrg, deletePackage) {

        //Not Exist CostItem with Package
        if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.subscription.id in (:subIds) and ci.subPkg.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted',[pkg:pkg, deleted: RDStore.COST_ITEM_DELETED, subIds: subList, context: contextOrg])) {

            Map<String,Object> queryParams = [sub: subList, pkg_id: pkg.id]
            //delete matches
            //IssueEntitlement.withSession { Session session ->
            batchUpdateService.clearIssueEntitlements(queryParams)
            if (deletePackage) {
                removePackagePendingChanges(pkg, subList, true)
                SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg.id = :pkg_id and sp.subscription.id in (:sub)',queryParams).each { SubscriptionPackage delPkg ->
                    OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :subPkg", [subPkg:delPkg])
                    CostItem.executeQuery('select ci from CostItem ci where ci.costItemStatus != :deleted and ci.subPkg = :delPkg and ci.owner != :ctx', [delPkg: delPkg, deleted: RDStore.COST_ITEM_DELETED, ctx: contextOrg]).each { CostItem ci ->
                        PendingChange.construct([target:ci,owner:ci.owner,oldValue:ci.subPkg.getPackageName(),newValue:null,msgToken:PendingChangeConfiguration.COST_ITEM_PACKAGE_UNLINKED,status:RDStore.PENDING_CHANGE_PENDING])
                    }
                    CostItem.executeUpdate('update CostItem ci set ci.costItemStatus = :deleted, ci.subPkg = null, ci.sub = :sub where ci.subPkg = :delPkg',[delPkg: delPkg, sub:delPkg.subscription, deleted: RDStore.COST_ITEM_DELETED])
                    PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:delPkg])
                }
                PermanentTitle.executeUpdate("delete from PermanentTitle pt where pt.subscription.id in (:sub) and pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id)", queryParams)
                SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg.id=:pkg_id and sp.subscription.id in (:sub)", queryParams)
                //log.debug("before flush")
                //session.flush()
                //}
            }
            return true
        }else{
            log.error("!!! unlinkFromSubscription fail: CostItems are still linked -> [pkg:${pkg},sub:${subList.toListString()}]!!!!")
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
        //continue here with package unlinking!
        List<Long> tippIDs = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg', [pkg: pkg])
        if(confirmed) {
            count = PendingChange.executeUpdate('delete from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])
        }
        else {
            if(subIds) {
                count = PendingChange.executeQuery('select count(pc.id) from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])[0]
            }
        }
        count
    }

    /**
     * Clears title records which have been marked as removed.
     * The method is going to be locked while execution because the query may take time to complete
     * @return true if a cleanup could be performed, false
     */
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

    /**
     * Sets some generally valid parameters for the response; those are the context user / institution, customer type and package to be retrieved and whether this package is being subscribed (= is my package)
     * @param params the request parameter map
     * @return a {@link Map} containing general result data
     */
    Map<String, Object> getResultGenerics(Map params) {
        Map<String, Object> result = [user: contextService.getUser(), contextOrg: contextService.getOrg(), packageInstance: Package.get(params.id)]
        result.contextCustomerType = result.contextOrg.getCustomerType()
        int relationCheck = SubscriptionPackage.executeQuery('select count(sp) from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true)))', [pkg: result.packageInstance, context: result.contextOrg, current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED])[0]
        result.isMyPkg = relationCheck > 0
        result
    }
}