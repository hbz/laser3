package de.laser

import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.oap.OrgAccessPointLink
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service manages calls related to package management
 */
@Transactional
class PackageService {

    BatchUpdateService batchUpdateService
    ContextService contextService
    DeletionService deletionService
    GokbService gokbService
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

    def getWekbPackages(GrailsParameterMap params) {
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        if (!apiSource) {
            return null
        }
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> result = [
                flagContentGokb : true // gokbService.executeQuery
        ]
        result.curatoryGroupTypes = [
                [value: 'Provider', name: messageSource.getMessage('package.curatoryGroup.provider', null, locale)],
                [value: 'Vendor', name: messageSource.getMessage('package.curatoryGroup.vendor', null, locale)],
                [value: 'Other', name: messageSource.getMessage('package.curatoryGroup.other', null, locale)]
        ]
        result.automaticUpdates = [
                [value: 'true', name: messageSource.getMessage('package.index.result.automaticUpdates', null, locale)],
                [value: 'false', name: messageSource.getMessage('package.index.result.noAutomaticUpdates', null, locale)]
        ]

        result.editUrl = apiSource.editUrl

        Map<String, Object> queryParams = [componentType: "Package"]
        if (params.q) {
            result.filterSet = true
            queryParams.name = params.q
            queryParams.ids = ["Anbieter_Produkt_ID,${params.q}", "isil,${params.q}"]
        }

        if(params.pkgStatus) {
            result.filterSet = true
        }
        else if(!params.pkgStatus) {
            params.pkgStatus = [RDStore.TIPP_STATUS_CURRENT.value, RDStore.TIPP_STATUS_EXPECTED.value, RDStore.TIPP_STATUS_RETIRED.value, RDStore.TIPP_STATUS_DELETED.value]
        }
        queryParams.status = params.pkgStatus

        if (params.provider) {
            result.filterSet = true
            queryParams.provider = params.provider
        }

        if (params.curatoryGroup) {
            result.filterSet = true
            queryParams.curatoryGroupExact = params.curatoryGroup
        }

        if (params.curatoryGroupType) {
            result.filterSet = true
            queryParams.curatoryGroupType = params.curatoryGroupType
        }

        if (params.automaticUpdates) {
            result.filterSet = true
            queryParams.automaticUpdates = params.automaticUpdates
        }

        if (params.ddc) {
            result.filterSet = true
            Set<String> selDDC = []
            params.list("ddc").each { String key ->
                selDDC << RefdataValue.get(key).value
            }
            queryParams.ddc = selDDC
        }

        if(params.stubOnly)
            queryParams.stubOnly = params.stubOnly
        if(params.uuids)
            queryParams.uuids = params.uuids

        Map queryCuratoryGroups = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + '/groups', [:])
        if(!params.sort)
            params.sort = 'name'
        if(queryCuratoryGroups.code == 404) {
            result.error = messageSource.getMessage('wekb.error.'+queryCuratoryGroups.error, null, locale) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            result.putAll(gokbService.doQuery(result, params, queryParams))
        }
        result
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
     * Substitution call for {@link #unlinkFromSubscription(de.laser.Package, java.util.List, de.laser.Org, java.lang.Object)} with a single subscription
     * @param pkg the {@link de.laser.Package} to be unlinked
     * @param subscription the {@link Subscription} from which the package should be detached
     * @param contextOrg the {@link de.laser.Org} whose cost items should be verified
     * @param deletePackage should the package be unlinked, too?
     * @return
     */
    boolean unlinkFromSubscription(de.laser.Package pkg, Subscription subscription, Org contextOrg, deletePackage) {
        unlinkFromSubscription(pkg, [subscription.id], contextOrg, deletePackage)
    }

    /**
     * Unlinks a subscription from the given package and removes resp. marks as delete every dependent object from that link such as cost items, pending change configurations etc.
     * The unlinking can be done iff no cost items are linked to the (subscription) package
     * @param pkg the {@link de.laser.Package} to be unlinked
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
            PermanentTitle.executeUpdate("delete from PermanentTitle pt where pt.subscription.id in (:sub) and pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id)", queryParams)
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
    Map<String, Object> getResultGenerics(GrailsParameterMap params) {
        Map<String, Object> result = [user: contextService.getUser(), contextOrg: contextService.getOrg(), packageInstance: Package.get(params.id)]
        result.contextCustomerType = result.contextOrg.getCustomerType()
        int relationCheck = SubscriptionPackage.executeQuery('select count(sp) from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true)))', [pkg: result.packageInstance, context: result.contextOrg, current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED])[0]
        result.isMyPkg = relationCheck > 0

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]
        result.contextOrg = contextService.getOrg()
        result.contextCustomerType = result.contextOrg.getCustomerType()

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result
    }
}