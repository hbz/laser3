package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.ctrl.PlatformControllerService
import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.remote.ApiSource
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls to platforms.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class PlatformController  {

    ContextService contextService
    GokbService gokbService
    PlatformControllerService platformControllerService

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
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Call to list all platforms in the we:kb ElasticSearch index.
     * Beware that a we:kb API connection has to be established for the list to work!
     * @return a list of all platforms currently recorded in the we:kb ElasticSearch index
     */
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def list() {
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        Map<String, Object> result = [
                user: contextService.getUser(),
                editUrl: apiSource.editUrl,
                myPlatformIds: [],
                flagContentGokb : true // gokbService.doQuery
        ]
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Map queryParams = [componentType: "Platform"]

        if(params.q) {
            result.filterSet = true
            queryParams.name = params.q
        }

        if(params.provider) {
            result.filterSet = true
            queryParams.provider = params.provider
        }

        if(params.status) {
            result.filterSet = true
            queryParams.status = RefdataValue.get(params.status).value
        }
        else if(!params.filterSet) {
            result.filterSet = true
            queryParams.status = "Current"
            params.status = RDStore.PLATFORM_STATUS_CURRENT.id
        }

        if (params.ipSupport) {
            result.filterSet = true
            queryParams.ipAuthentication = Params.getRefdataList(params, 'ipSupport').collect{ it.value }
        }
        if (params.shibbolethSupport) {
            result.filterSet = true
            queryParams.shibbolethAuthentication = Params.getRefdataList(params, 'shibbolethSupport').collect{ (it == RDStore.GENERIC_NULL_VALUE) ? 'null' : it.value }
        }
        if (params.counterCertified) {
            result.filterSet = true
            queryParams.counterCertified = Params.getRefdataList(params, 'counterCertified').collect{ (it == RDStore.GENERIC_NULL_VALUE) ? 'null' : it.value }
        }

        // overridden pagination - all uuids are required
        Map wekbResultMap = gokbService.doQuery(result, [offset:0, max:1000, status: params.status], queryParams)
        if(!wekbResultMap)
            result.error = message(code: 'wekb.error.404') as String

        // ? --- copied from myInstitutionController.currentPlatforms()
        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [
                contextOrg: contextService.getOrg(),
                roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA],
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
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    @Check404()
    def show() {
        Map<String, Object> result = platformControllerService.getResultGenerics(params)
        Platform platformInstance = result.platformInstance
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'

        result.flagContentGokb = true // gokbService.executeQuery
        result.platformInstanceRecord = [:]
        Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: platformInstance.gokbId])
        if ((queryResult.error && queryResult.error == 404) || !queryResult) {
            flash.error = message(code:'wekb.error.404') as String
        }
        else if (queryResult) {
            List records = queryResult.result
            result.platformInstanceRecord = records ? records[0] : [:]
            result.platformInstanceRecord.id = params.id
        }
        result.editable = contextService.isInstEditor_or_ROLEADMIN()

        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap " +
                    "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id} " +
                    "and oapl.subPkg is null order by LOWER(ap.name)"
        result.orgAccessPointList = OrgAccessPointLink.executeQuery(hql,[institution : result.contextOrg])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution " +
            "and not exists (" +
            "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true " +
            "and oapl.platform.id = ${platformInstance.id} and oapl.subPkg is null) order by lower(oap.name)"
        result.accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : result.contextOrg])

        result.selectedInstitution = result.contextOrg.id

        // ? --- copied from myInstitutionController.currentPlatforms()
        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [
                contextOrg: contextService.getOrg(),
                roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA],
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
     * Call for linking the platform to an access point.
     * Is a non-modal duplicate of {@link #dynamicApLink()} -
     * @deprecated use {@link #dynamicApLink()} instead
     */
    @Deprecated
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
    })
    @Check404()
    def link() {
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.id)

        Org selectedInstitution = contextService.getOrg()

        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
            hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        List<OrgAccessPointLink> results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
            notActiveAPLinkQuery += "and not exists ("
            notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
            notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        List<OrgAccessPoint> accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = contextService.getUser().formalOrg ? [contextService.getUser().formalOrg] : []
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        result
    }

    /**
     * Call to link a platform to another access point
     * @return renders the available options in a modal
     */
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
    })
    def dynamicApLink(){
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.platform_id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label'), params.platform_id]) as String
            redirect action: 'list'
            return
        }
        Org selectedInstitution =  contextService.getOrg()
        if (params.institution_id){
            selectedInstitution = Org.get(params.institution_id)
        }
        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
        hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        List<OrgAccessPointLink> results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
        notActiveAPLinkQuery += "and not exists ("
        notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
        notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        List<OrgAccessPoint> accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = contextService.getUser().formalOrg ? [contextService.getUser().formalOrg] : []
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        render(view: "_apLinkContent", model: result)
    }

    /**
     * Call to add a new derivation to the given platform
     * @return redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
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
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
    })
    def removeDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.removeDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @Deprecated
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
    })
    def linkAccessPoint() {
        OrgAccessPoint apInstance
        if (params.AccessPoints){
            apInstance = OrgAccessPoint.get(params.AccessPoints)
            if (!apInstance) {
                flash.error = 'No valid Accesspoint id given'
                redirect action: 'link', params: [id:params.id]
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

    @Deprecated
    @DebugInfo(isInstEditor_denySupport_or_ROLEADMIN = [], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport_or_ROLEADMIN()
    })
    def removeAccessPoint() {
        Map<String,Object> ctrlResult = platformControllerService.removeAccessPoint(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'link', params: [id:params.id]
    }
}
