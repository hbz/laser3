package com.k_int.kbplus

import de.laser.DeletionService
import de.laser.PendingChange
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.RDStore
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
        TitleInstancePackagePlatform.executeQuery('select count(tipp.id) from TitleInstancePackagePlatform tipp where tipp.status != :removed and tipp.pkg = :pkg',[deleted: RDStore.TIPP_STATUS_REMOVED, pkg: pkg])[0]
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
                "select 0, now(), now(), ${subId}, tipp_id, tipp_access_start_date, tipp_access_end_date, 'manually added by user', tipp_medium_rv_fk, tipp_status_rv_fk, ${RDStore.IE_ACCEPT_STATUS_FIXED.id}, tipp_name, tipp_sort_name ${perpetualAccessCol} from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [pkgId: pkgId, deleted: RDStore.TIPP_STATUS_REMOVED.id])
        sql.executeInsert("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo) " +
                "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), tc_start_date, tc_start_volume, tc_start_issue, tc_end_date, tc_end_volume, tc_end_issue, tc_coverage_depth, tc_coverage_note, tc_embargo from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
        sql.executeInsert("insert into price_item (version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid, pi_list_currency_rv_fk, pi_list_price) " +
                "select 0, (select ie_id from issue_entitlement where ie_tipp_fk = tipp_id and ie_subscription_fk = :subId and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_currency_rv_fk, pi_list_price from price_item join title_instance_package_platform on pi_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id])
    }

    boolean clearRemovedTitles(Set<PendingChange> titlesToRemove) {
        if(!titleCleanupRunning) {
            titleCleanupRunning = true
            Set<TitleInstancePackagePlatform> titles = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where not exists(select ie.id from IssueEntitlement ie where ie.tipp in (:toRemove)) and tipp in (:toRemove)",[toRemove: titlesToRemove.collect{ PendingChange pc -> pc.tipp }])
            //the query above is to ensure that no issue entitlements are going to be removed before!
            deletionService.deleteTIPPsCascaded(titles)
            titleCleanupRunning = false
            return true
        }
        else return false
    }
}