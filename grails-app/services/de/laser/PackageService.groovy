package de.laser

import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.helper.Params
import de.laser.oap.OrgAccessPointLink
import de.laser.remote.Wekb
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional
import grails.web.mapping.LinkGenerator
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service manages calls related to package management
 */
@Transactional
class PackageService {

    BatchQueryService batchQueryService
    ContextService contextService
    DeletionService deletionService
    EscapeService escapeService
    GokbService gokbService
    LinkGenerator grailsLinkGenerator
    MessageSource messageSource
    GlobalSourceSyncService globalSourceSyncService
    SubscriptionsQueryService subscriptionsQueryService

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
    List listConflicts(de.laser.wekb.Package pkg, subscription, int numOfPCs, int numOfIEs, int numOfCIs) {
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

        result.baseUrl = Wekb.getURL()

        Map<String, Object> queryParams
        if (params.singleTitle) {
            queryParams = [componentType: "TitleInstancePackagePlatform"]
            result.filterSet = true
            queryParams.name = params.singleTitle
        }
        else queryParams = [componentType: "Package"]

        if (params.q) {
            result.filterSet = true
            queryParams.name = params.q
            queryParams.ids = ["Anbieter_Produkt_ID,${params.q}", "isil,${params.q}"]
        }

        if(params.pkgStatus) {
            result.filterSet = true
            queryParams.status = params.list('pkgStatus')
        }
        else if(!params.pkgStatus) {
            params.pkgStatus = [RDStore.TIPP_STATUS_CURRENT.value] //, RDStore.TIPP_STATUS_EXPECTED.value, RDStore.TIPP_STATUS_RETIRED.value, RDStore.TIPP_STATUS_DELETED.value //as of ERMS-6370, comments from April 24th and 25th, '25
            queryParams.status = params.pkgStatus
        }

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
            queryParams.ddc = Params.getRefdataList(params, 'ddc').collect { RefdataValue rv -> rv.value }
        }

        if (params.archivingAgency) {
            result.filterSet = true
            queryParams.archivingAgency = Params.getRefdataList(params, 'archivingAgency').collect { RefdataValue rv -> rv.value }
        }

        if (params.contentType) {
            result.filterSet = true
            queryParams.contentType = Params.getRefdataList(params, 'contentType').collect { RefdataValue rv -> rv.value }
        }

        if (params.paymentType) {
            result.filterSet = true
            queryParams.paymentType = Params.getRefdataList(params, 'paymentType').collect { RefdataValue rv -> rv.value }
        }

        if (params.openAccess) {
            result.filterSet = true
            queryParams.openAccess = Params.getRefdataList(params, 'openAccess').collect { RefdataValue rv -> rv.value }
        }

        if(params.stubOnly)
            queryParams.stubOnly = params.stubOnly
        if(params.uuids)
            queryParams.uuids = params.uuids

        Map<String, Object> args = [:]
        if(params.containsKey('my'))
            args.componentType = "Package"
        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), args)
        if(!params.sort)
            params.sort = 'name'
        if(queryCuratoryGroups.code == 404) {
            result.error = messageSource.getMessage('wekb.error.'+queryCuratoryGroups.error, null, locale) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                if(params.containsKey('my')) {
                    Org contextOrg = contextService.getOrg()
                    String instanceFilter = contextOrg.isCustomerType_Consortium() ? 'and s.instanceOf = null' : ''
                    Set<String> myPackageWekbIds = Package.executeQuery('select pkg.gokbId from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo join sp.pkg pkg where oo.org = :contextOrg and oo.roleType in (:roleTypes) and s.status = :current ' + instanceFilter, [contextOrg: contextOrg, roleTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM], current: RDStore.SUBSCRIPTION_CURRENT])
                    List myCuratoryGroups = recordsCuratoryGroups?.findAll { it.status == 'Current' && it.packageUuid in myPackageWekbIds }
                    //post-filter because we:kb delivers the package UUID in the map as well ==> uniqueness is explicitly given!
                    Set curatoryGroupSet = []
                    myCuratoryGroups.each { Map myCG ->
                        myCG.remove('packageUuid')
                        curatoryGroupSet << myCG
                    }
                    result.curatoryGroups = curatoryGroupSet
                }
                else result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
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
    Long getCountOfCurrentTippIDs(de.laser.wekb.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select count(*) from TitleInstancePackagePlatform tipp where tipp.status = :current and tipp.pkg = :pkg',[current: RDStore.TIPP_STATUS_CURRENT, pkg: pkg])[0]
    }

    /**
     * Gets the count of titles in the package which are not marked as deleted in the given package
     * @param pkg the package whose titles should be counted
     * @return a count of non-deleted titles in the package
     */
    Long getCountOfNonDeletedTitles(de.laser.wekb.Package pkg) {
        TitleInstancePackagePlatform.executeQuery('select count(*) from TitleInstancePackagePlatform tipp where tipp.status != :removed and tipp.pkg = :pkg',[removed: RDStore.TIPP_STATUS_REMOVED, pkg: pkg])[0]
    }

    /**
     * Substitution call for {@link #unlinkFromSubscription(Package, java.util.List, de.laser.Org, java.lang.Object)} with a single subscription
     * @param pkg the {@link Package} to be unlinked
     * @param subscription the {@link Subscription} from which the package should be detached
     * @param contextOrg the {@link de.laser.Org} whose cost items should be verified
     * @param deletePackage should the package be unlinked, too?
     * @return
     */
    boolean unlinkFromSubscription(de.laser.wekb.Package pkg, Subscription subscription, Org contextOrg, deletePackage) {
        unlinkFromSubscription(pkg, [subscription.id], contextOrg, deletePackage)
    }

    /**
     * Unlinks a subscription from the given package and removes resp. marks as delete every dependent object from that link such as cost items, pending change configurations etc.
     * The unlinking can be done iff no cost items are linked to the (subscription) package
     * @param pkg the {@link Package} to be unlinked
     * @param subscription the {@link Subscription} from which the package should be detached
     * @param contextOrg the {@link de.laser.Org} whose cost items should be verified
     * @param deletePackage should the package be unlinked, too?
     * @return true if the unlink was successful, false otherwise
     */
    boolean unlinkFromSubscription(de.laser.wekb.Package pkg, List<Long> subList, Org contextOrg, deletePackage) {

        //Not Exist CostItem with Package
        if(!CostItem.executeQuery('select ci from CostItem ci where ci.sub.id in (:subIds) and ci.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted',[pkg:pkg, deleted: RDStore.COST_ITEM_DELETED, subIds: subList, context: contextOrg])) {

            Map<String,Object> queryParams = [sub: subList, pkg_id: pkg.id]
            //delete matches
            //IssueEntitlement.withSession { Session session ->
            batchQueryService.clearIssueEntitlements(queryParams)
            PermanentTitle.executeUpdate("delete from PermanentTitle pt where pt.subscription.id in (:sub) and pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id)", queryParams)
            if (deletePackage) {
                removePackagePendingChanges(pkg, subList, true)
                SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg.id = :pkg_id and sp.subscription.id in (:sub)',queryParams).each { SubscriptionPackage delPkg ->
                    OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :subPkg", [subPkg:delPkg])
                    CostItem.executeQuery('select ci from CostItem ci where ci.costItemStatus != :deleted and ci.pkg = :delPkg and ci.sub = :sub and ci.owner != :ctx', [sub: delPkg.subscription, delPkg: delPkg.pkg, deleted: RDStore.COST_ITEM_DELETED, ctx: contextOrg]).each { CostItem ci ->
                        PendingChange.construct([target:ci,owner:ci.owner,oldValue:ci.pkg.name,newValue:null,msgToken:PendingChangeConfiguration.COST_ITEM_PACKAGE_UNLINKED,status:RDStore.PENDING_CHANGE_PENDING])
                    }
                    CostItem.executeUpdate('update CostItem ci set ci.costItemStatus = :deleted, ci.pkg = null, ci.sub = :sub where ci.pkg = :delPkg',[delPkg: delPkg.pkg, sub:delPkg.subscription, deleted: RDStore.COST_ITEM_DELETED])
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
    int removePackagePendingChanges(de.laser.wekb.Package pkg, List subIds, boolean confirmed) {
        int count = 0
        List<Long> tippIDs = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg', [pkg: pkg])
        if(confirmed) {
            count = PendingChange.executeUpdate('delete from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])
        }
        else {
            if(subIds) {
                count = PendingChange.executeQuery('select count(*) from PendingChange pc where (pc.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId) and pc.oid in (:subOIDs))', [pkgId: pkg.id, subOIDs: subIds.collect { subId -> Subscription.class.name+':'+subId }])[0]
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
        Map<String, Object> result = [
                user: contextService.getUser(),
                contextOrg: contextService.getOrg(),
                packageInstance: Package.get(params.id)
        ]

        result.contextCustomerType = contextService.getOrg().getCustomerType()
        int relationCheck = SubscriptionPackage.executeQuery('select count(*) from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription in (select oo.sub from OrgRole oo join oo.sub sub where oo.org = :context and (sub.status = :current or (sub.status = :expired and sub.hasPerpetualAccess = true)))', [pkg: result.packageInstance, context: contextService.getOrg(), current: RDStore.SUBSCRIPTION_CURRENT, expired: RDStore.SUBSCRIPTION_EXPIRED])[0]
        result.isMyPkg = relationCheck > 0

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status", [pkg: result.packageInstance, status: RDStore.TIPP_STATUS_DELETED])[0]

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([status: 'FETCH_ALL', linkedPkg: result.packageInstance, count: true], '', contextService.getOrg())
        result.subscriptionCounts = result.packageInstance ? Subscription.executeQuery( "select count(*) " + tmpQ[0], tmpQ[1] )[0] : 0

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result
    }

    Package createPackageWithWEKB(String pkgUUID){
        Package pkg
            try {
                Map<String,Object> queryResult = globalSourceSyncService.fetchRecordJSON(false,[componentType:'TitleInstancePackagePlatform',tippPackageUuid:pkgUUID,max:GlobalSourceSyncService.MAX_TIPP_COUNT_PER_PAGE,sort:'lastUpdated'])
                if(queryResult.error && queryResult.error == 404) {
                    log.error("we:kb server currently unavailable")
                }
                else {
                    Package.withNewTransaction {
                        if(queryResult.records && queryResult.count > 0) {
                            if(queryResult.count >= GlobalSourceSyncService.MAX_TIPP_COUNT_PER_PAGE)
                                globalSourceSyncService.processScrollPage(queryResult, 'TitleInstancePackagePlatform', null, pkgUUID)
                            else
                                globalSourceSyncService.updateRecords(queryResult.records, 0)
                        }
                        else {
                            globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                        }
                    }
                    pkg = Package.findByGokbId(pkgUUID)
                }
            }
            catch (Exception e) {
                log.error("sync job has failed, please consult stacktrace as follows: ")
                e.printStackTrace()
            }
        return pkg
    }
}