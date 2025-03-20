package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.ctrl.PlatformControllerService
import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.properties.PropertyDefinition
import de.laser.remote.Wekb
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import de.laser.wekb.Platform
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls to platforms.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PlatformController  {

    ContextService contextService
    FilterService filterService
    GokbService gokbService
    PlatformControllerService platformControllerService
    PropertyService propertyService

    //-----

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    /**
     * Map containing menu alternatives if an unexisting object has been called
     */
    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'platforms.all_platforms.label',
            'myInstitution/currentPlatforms' : 'menu.my.platforms'
    ]

    //-----

    /**
     * Landing point; redirects to the list of platforms
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Call to list all platforms in the we:kb ElasticSearch index.
     * Beware that a we:kb API connection has to be established for the list to work!
     * @return a list of all platforms currently recorded in the we:kb ElasticSearch index
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def list() {
        Map<String, Object> result = [
                user: contextService.getUser(),
                baseUrl: Wekb.getURL(),
                myPlatformIds: [],
                flagContentGokb : true, // gokbService.doQuery
                propList: PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.PLA_PROP], contextService.getOrg())
        ]
        SwissKnife.setPaginationParams(result, params, (User) result.user)
        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [:])
        if(queryCuratoryGroups.code == 404) {
            result.error = message(code: 'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        Map<String, Object> queryParams = filterService.getWekbPlatformFilterParams(params)

        // overridden pagination - all uuids are required
        Map wekbResultMap = gokbService.doQuery(result, [offset:0, max:10000, status: params.status], queryParams)
        if(!wekbResultMap)
            result.error = message(code: 'wekb.error.404') as String

        // ? --- copied from myInstitutionController.currentPlatforms()
        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [
                contextOrg: contextService.getOrg(),
                roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM],
                current:    RDStore.SUBSCRIPTION_CURRENT,
                expired:    RDStore.SUBSCRIPTION_EXPIRED
        ]
        if (contextService.getOrg().isCustomerType_Consortium()) {
            instanceFilter += " and s.instanceOf = null "
        }

        String subscriptionQuery = 'select s.id from OrgRole oo join oo.sub s where oo.org = :contextOrg and oo.roleType in (:roleTypes) and (s.status = :current or (s.status = :expired and s.hasPerpetualAccess = true))' + instanceFilter

        //if (subscriptionQuery) {
            String qry3 =
                    "select distinct p, s, p.normname from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, " +
                    "TitleInstancePackagePlatform tipp join tipp.platform p left join p.org o " +
                    "where tipp.pkg = pkg and s in (${subscriptionQuery}) and p.gokbId in (:wekbIds) " +
                    "and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted)) " +
                    "and ((tipp.status is null) or (tipp.status != :tippRemoved)) " +
                    "group by p, s order by p.normname asc"

            Map qryParams3 = [
                    pkgDeleted : RDStore.PACKAGE_STATUS_DELETED,
                    tippRemoved: RDStore.TIPP_STATUS_REMOVED,
                    wekbIds    : wekbResultMap.records.collect { Map hit -> hit.uuid }
            ]
            qryParams3.putAll(subscriptionParams)

            List platformSubscriptionList = []
            List platformInstanceList = []
            Map  subscriptionMap = [:]

            if (qryParams3.wekbIds) {
                platformSubscriptionList.addAll(Platform.executeQuery(qry3, qryParams3))
            }

            platformSubscriptionList.each { entry ->
                Platform pl = (Platform) entry[0]
                Subscription s = (Subscription) entry[1]

                String key = 'platform_' + pl.id

                if (! subscriptionMap.containsKey(key)) {
                    subscriptionMap.put(key, [])
                    platformInstanceList.add(pl)
                }

                if (s.status.value == RDStore.SUBSCRIPTION_CURRENT.value) {
                    subscriptionMap.get(key).add(s)
                }
            }

            result.myPlatformsUuids = platformInstanceList.collect{ it.gokbId }
            result.myPlatformIds    = platformInstanceList.collect{ it.id }
        //}
        // ? ---

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<String> f1Result = [], f2Result = []
            boolean     f1Set = false, f2Set = false

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( wekbResultMap.records.findAll { result.myPlatformsUuids.contains(it.uuid) }.collect{ it.uuid } )
                f1Set = true
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( wekbResultMap.records.findAll { ! result.myPlatformsUuids.contains(it.uuid) }.collect{ it.uuid } )
                f1Set = true
            }
            if (xFilter.contains('wekb_exclusive')) {
                f2Result.addAll( wekbResultMap.records.findAll {
                    if (it.providerUuid) { return true }
//                    Platform p = Platform.findByGokbId(it.uuid)
//                    return (p && p.gokbId != null)
                    Platform p = Platform.findByGokbId(it.uuid)
                    if (p && p.org) { return p.org.gokbId != null } else { return false }
                }.collect{ it.uuid } )
                f2Set = true
            }
            if (xFilter.contains('wekb_not')) {
                f2Result.addAll( wekbResultMap.records.findAll {
//                    Platform p = Platform.findByGokbId(it.uuid)
//                    return (!p || p.gokbId == null)
                    if (it.providerUuid) { return false }
                    return Platform.findByGokbId(it.uuid)?.org?.gokbId == null
                }.collect{ it.uuid } )
                f2Set = true
            }

            if (f1Set) { wekbResultMap.records = wekbResultMap.records.findAll { f1Result.contains(it.uuid) } }
            if (f2Set) { wekbResultMap.records = wekbResultMap.records.findAll { f2Result.contains(it.uuid) } }
        }

        if (params.filterPropDef) {
            result.filterSet = true
            String propQuery = "select p.gokbId from Platform p where p.gokbId in (:wekbIds)"
            Map<String, Object> propParams = [wekbIds: wekbResultMap.records.collect { Map hit -> hit.uuid }]
            Map<String, Object> psq = propertyService.evalFilterQuery(params, propQuery, 'p', propParams)
            propQuery = psq.query
            propParams.putAll(psq.queryParams)
            wekbResultMap.records = wekbResultMap.records.findAll { Platform.executeQuery(propQuery, propParams).contains(it.uuid) }
        }
        wekbResultMap.recordsCount = wekbResultMap.records.size()
        wekbResultMap.records      = wekbResultMap.records.drop((int) result.offset).take((int) result.max) // pagination

        result.putAll(wekbResultMap)

        result
    }

    /**
     * Shows the details page for the given platform. Note that there are fields not mirrored in the app
     * but fetched on-the-fly from we:kb; a we:kb ElasticSearch API is thus necessary
     * @return the details view of the platform
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Check404()
    def show() {
        Map<String, Object> result = platformControllerService.getResultGenerics(params)
        Platform platformInstance = result.platformInstance

        result.baseUrl = Wekb.getURL()

        result.flagContentGokb = true // gokbService.executeQuery
        result.platformInstanceRecord = [:]
        Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: platformInstance.gokbId])
        if ((queryResult.error && queryResult.error == 404) || !queryResult) {
            flash.error = message(code:'wekb.error.404') as String
        }
        else if (queryResult) {
            List records = queryResult.result
            result.platformInstanceRecord = records ? records[0] : [:]
            result.platformInstanceRecord.id = params.id
        }
        result.editable = contextService.isInstEditor()

        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap " +
                    "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id} " +
                    "and oapl.subPkg is null order by LOWER(ap.name)"
        result.orgAccessPointList = OrgAccessPointLink.executeQuery(hql,[institution : contextService.getOrg()])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution " +
            "and not exists (" +
            "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true " +
            "and oapl.platform.id = ${platformInstance.id} and oapl.subPkg is null) order by lower(oap.name)"
        result.accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : contextService.getOrg()])

        result.selectedInstitution = contextService.getOrg().id

        // ? --- copied from myInstitutionController.currentPlatforms()
        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [
                contextOrg: contextService.getOrg(),
                roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM],
                current:    RDStore.SUBSCRIPTION_CURRENT,
                expired:    RDStore.SUBSCRIPTION_EXPIRED
        ]
        if (contextService.getOrg().isCustomerType_Consortium()) {
            instanceFilter += " and s.instanceOf = null "
        }

        Set<Long> idsCurrentSubscriptions = Subscription.executeQuery(
                'select s.id from OrgRole oo join oo.sub s where oo.org = :contextOrg and oo.roleType in (:roleTypes) and (s.status = :current or (s.status = :expired and s.hasPerpetualAccess = true))' + instanceFilter,
                subscriptionParams
        )

        if (idsCurrentSubscriptions) {
            String qry3 =
                    "select distinct p, s, p.normname from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, " +
                            "TitleInstancePackagePlatform tipp join tipp.platform p left join p.org o " +
                            "where tipp.pkg = pkg and s.id in (:subIds) and p.gokbId in (:wekbIds) " +
                            "and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted)) " +
                            "and ((tipp.status is null) or (tipp.status != :tippRemoved)) " +
                            "group by p, s order by p.normname asc"

            Map qryParams3 = [
                    subIds     : idsCurrentSubscriptions,
                    pkgDeleted : RDStore.PACKAGE_STATUS_DELETED,
                    tippRemoved: RDStore.TIPP_STATUS_REMOVED,
                    wekbIds    : [ platformInstance.gokbId ]
            ]
            result.isMyPlatform = Platform.executeQuery(qry3, qryParams3) ? true : false
        }
        // ? ---

        result
    }

    /**
     * Call to add a new derivation to the given platform
     * @return redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def addDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.addDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to remove a new derivation to the given platform
     * @return redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def removeDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.removeDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def linkAccessPoint() {
        OrgAccessPoint apInstance
        if (params.AccessPoints) {
            apInstance = OrgAccessPoint.get(params.AccessPoints)
        }
            if (!apInstance) {
                flash.error = 'No valid Accesspoint id given'
                redirect action: 'show', params: [id: params.platform_id]
                return
            }
            else {
                Map<String,Object> ctrlResult = platformControllerService.linkAccessPoint(params, apInstance)
                if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
                    flash.error = ctrlResult.result.error
                }
                redirect(url: request.getHeader('referer'))
                return
            }
    }
}
