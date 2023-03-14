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

        String esQuery = "?componentType=Platform"

        if(params.q) {
            result.filterSet = true
            esQuery += "&q=${params.q}"
        }

        if(params.provider) {
            result.filterSet = true
            esQuery += "&provider=${params.provider}"
        }

        if(params.status) {
            result.filterSet = true
            esQuery += "&status=${RefdataValue.get(params.status).value}"
        }
        else if(!params.filterSet) {
            result.filterSet = true
            esQuery += "&status=Current"
            params.status = RDStore.PLATFORM_STATUS_CURRENT.id.toString()
        }

        if(params.ipSupport) {
            result.filterSet = true
            List<String> ipSupport = params.list("ipSupport")
            ipSupport.each { String ip ->
                RefdataValue rdv = RefdataValue.get(ip)
                esQuery += "&ipAuthentication=${rdv.value}"
            }
        }

        if(params.shibbolethSupport) {
            result.filterSet = true
            List<String> shibbolethSupport = params.list("shibbolethSupport")
            shibbolethSupport.each { String shibboleth ->
                RefdataValue rdv = RefdataValue.get(shibboleth)
                esQuery += "&shibbolethAuthentication=${rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value}"
            }
        }

        if(params.counterCertified) {
            result.filterSet = true
            List<String> counterCertified = params.list("counterCertified")
            counterCertified.each { String counter ->
                RefdataValue rdv = RefdataValue.get(counter)
                esQuery += "&counterCertified=${rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value}"
            }
        }

        Map wekbResultMap = gokbService.doQuery(result, params.clone(), esQuery)

        // ? --- copied from myInstitutionController.currentPlatforms()
        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [
                contextOrg: contextService.getOrg(),
                roleTypes:  [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA],
                current:    RDStore.SUBSCRIPTION_CURRENT,
                expired:    RDStore.SUBSCRIPTION_EXPIRED
        ]
        if (contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO']) {
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

            result.myPlatformIds = platformInstanceList.collect{ it.id }
        }
        // ? ---

//        result.putAll(gokbService.doQuery(result, params.clone(), esQuery))
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
        Platform platformInstance
        if(params.id instanceof Long || params.id.isLong())
            platformInstance = Platform.get(params.id)
        else if(params.id ==~ /[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}/)
            platformInstance = Platform.findByGokbId(params.id)
        else platformInstance = Platform.findByGlobalUID(params.id)

        result.platformInstance = platformInstance
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.editUrl = apiSource.editUrl.endsWith('/') ? apiSource.editUrl : apiSource.editUrl+'/'

        result.flagContentGokb = true // gokbService.queryElasticsearch
        Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${platformInstance.gokbId}")
        if (queryResult.error && queryResult.error == 404) {
            flash.error = message(code:'wekb.error.404') as String
        }
        else if (queryResult.warning) {
            List records = queryResult.warning.records
            result.platformInstanceRecord = records ? records[0] : [:]
            result.platformInstanceRecord.id = params.id
        }
        result.editable = accessService.checkPermAffiliationX('ORG_BASIC_MEMBER,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')

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
        if (contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO']) {
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
     * Currently inaccessible
     * Lists all access methods linked to the given platform
     */
    @Secured(['ROLE_USER'])
    @Check404()
    def accessMethods() {
        // TODO: editable is undefined
        def editable
        Platform platformInstance = Platform.get(params.id)

        List<PlatformAccessMethod> platformAccessMethodList = PlatformAccessMethod.findAllByPlatf(platformInstance, [sort: ["accessMethod": 'asc', "validFrom" : 'asc']])

        [platformInstance: platformInstance, platformAccessMethodList: platformAccessMethodList, editable: editable, params: params]
    }

    /**
     * Call for linking the platform to an access point.
     * Is a non-modal duplicate of {@link #dynamicApLink()} -
     * @deprecated use {@link #dynamicApLink} instead
     */
    @Deprecated
    @DebugInfo(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    @Check404()
    def link() {
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.id)

        Org selectedInstitution = contextService.getOrg()
        List<Org> authorizedOrgs = contextService.getUser().getAffiliationOrgs()

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
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        result
    }

    /**
     * Call to link a platform to another access point
     * @return renders the available options in a modal
     */
    @DebugInfo(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def dynamicApLink(){
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.platform_id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label'), params.platform_id]) as String
            redirect action: 'list'
            return
        }
        List<Org> authorizedOrgs = contextService.getUser().getAffiliationOrgs()
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
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        render(view: "_apLinkContent", model: result)
    }

    /**
     * Call to add a new derivation to the given platform
     * @return redirect to the referer
     */
    @DebugInfo(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
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
    @DebugInfo(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.removeDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @Deprecated
    @DebugInfo(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
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
    @DebugInfo(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeAccessPoint() {
        Map<String,Object> ctrlResult = platformControllerService.removeAccessPoint(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'link', params: [id:params.id]
    }
}
