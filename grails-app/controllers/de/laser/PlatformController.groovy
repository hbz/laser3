package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.ctrl.PlatformControllerService
import de.laser.annotations.DebugInfo
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

    AccessService accessService
    ContextService contextService
    GokbService gokbService
    PlatformControllerService platformControllerService

    //-----

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'platforms.all_platforms.label',
            'myInstitution/currentPlatforms' : 'menu.my.platforms'
    ]

    //-----

    /**
     * Landing point; redirects to the list of platforms
     */
    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    /**
     * Call to list all platforms in the we:kb ElasticSearch index.
     * Beware that a we:kb API connection has to be established for the list to work!
     * @return a list of all platforms currently recorded in the we:kb ElasticSearch index
     */
    @Secured(['ROLE_USER'])
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
            queryParams.q = params.q
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
            params.status = RDStore.PLATFORM_STATUS_CURRENT.id.toString()
        }

        if(params.ipSupport) {
            result.filterSet = true
            List<String> ipSupport = params.list("ipSupport")
            queryParams.ipAuthentication = []
            ipSupport.each { String ip ->
                RefdataValue rdv = RefdataValue.get(ip)
                queryParams.ipAuthentication << rdv.value
            }
        }

        if(params.shibbolethSupport) {
            result.filterSet = true
            List<String> shibbolethSupport = params.list("shibbolethSupport")
            queryParams.shibbolethAuthentication = []
            shibbolethSupport.each { String shibboleth ->
                RefdataValue rdv = RefdataValue.get(shibboleth)
                String auth = rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value
                queryParams.shibbolethAuthentication << auth
            }
        }

        if(params.counterCertified) {
            result.filterSet = true
            List<String> counterCertified = params.list("counterCertified")
            queryParams.counterCertified = []
            counterCertified.each { String counter ->
                RefdataValue rdv = RefdataValue.get(counter)
                String cert = rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value
                queryParams.counterCertified = cert
            }
        }

        // overridden pagination - all uuids are required
        Map wekbResultMap = gokbService.doQuery(result, [offset:0, max:1000, status: params.status], queryParams)

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
                    wekbIds    : wekbResultMap.records.collect { Map hit -> hit.uuid }
            ]

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
        }
        // ? ---

        if (params.isMyX) {
            List xFilter = params.list('isMyX')
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
    @Secured(['ROLE_USER'])
    @Check404()
    def show() {
        Map<String, Object> result = platformControllerService.getResultGenerics(params)
        Platform platformInstance = Platform.get(params.id)

        result.platformInstance = platformInstance
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'

        result.flagContentGokb = true // gokbService.queryElasticsearch
        result.platformInstanceRecord = [:]
        Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: platformInstance.gokbId])
        if (queryResult.error && queryResult.error == 404) {
            flash.error = message(code:'wekb.error.404') as String
        }
        else if (queryResult.warning) {
            List records = queryResult.warning.result
            result.platformInstanceRecord = records ? records[0] : [:]
            result.platformInstanceRecord.id = params.id
        }
        result.editable = contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_BASIC )

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
     * @deprecated use {@link #dynamicApLink} instead
     */
    @Deprecated
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
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
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'])
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_BASIC], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_BASIC)
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_BASIC], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_BASIC)
    })
    def removeDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.removeDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @Deprecated
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_BASIC], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_BASIC)
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_BASIC], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_BASIC)
    })
    def removeAccessPoint() {
        Map<String,Object> ctrlResult = platformControllerService.removeAccessPoint(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'link', params: [id:params.id]
    }
}
