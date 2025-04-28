package de.laser

import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.annotations.DebugInfo
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.convenience.Marker
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.ctrl.UserControllerService
import de.laser.finance.CostInformationDefinition
import de.laser.finance.CostInformationDefinitionGroup
import de.laser.remote.Wekb
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
 
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.CostItemGroup
import de.laser.helper.*
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupItem
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyLinks
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPersonResult
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyVendorResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import de.laser.workflow.WfChecklist
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.TransactionStatus
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletOutputStream
import java.security.MessageDigest
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * This is one of the central controllers as it manages every call related to the context institution.
 * The context institution is that one which the user logged in has picked for his/her current session; every call
 * done here supposes that the institution to be used for permission checks is the context institution.
 * For the definition of institution, see {@link Org}
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class MyInstitutionController  {

    AddressbookService addressbookService
    CacheService cacheService
    ContextService contextService
    CustomerTypeService customerTypeService
    DeletionService deletionService
    DocstoreService docstoreService
    ExportClickMeService exportClickMeService
    EscapeService escapeService
    ExportService exportService
    FilterService filterService
    FinanceService financeService
    FormService formService
    GenericOIDService genericOIDService
    GokbService gokbService
    InstitutionsService institutionsService
    LinksGenerationService linksGenerationService
    ManagementService managementService
    MarkerService markerService
    MyInstitutionControllerService myInstitutionControllerService
    PackageService packageService
    PropertyService propertyService
    ProviderService providerService
    ReportingGlobalService reportingGlobalService
    SubscriptionsQueryService subscriptionsQueryService
    SubscriptionService subscriptionService
    SurveyService surveyService
    TaskService taskService
    UserControllerService userControllerService
    UserService userService
    VendorService vendorService
    WorkflowService workflowService
    MailSendService mailSendService

    /**
     * The landing page after login; this is also the call when the home button is clicked
     * @return the {@link #dashboard()} view
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def index() {
        redirect(action:'dashboard')
    }

    /**
     * Call for the reporting module
     * @return the reporting entry view
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    def reporting() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.cfgFilterList = BaseConfig.FILTER
        result.cfgChartsList = BaseConfig.CHARTS

        if (params.init) {
            result.filter = params.filter
            result.xhr = true
            render template: '/myInstitution/reporting/filter/filter_form', model: result
            return
        }
        else if (params.filter) {
            reportingGlobalService.doFilter(result, params) // manipulates result, clones params

            Map<String, Object> cacheMap = [
                    meta : [
                        filter:     params.filter,
                        timestamp:  System.currentTimeMillis()
                    ],
                    filterCache: [
                        map:    [:],
                        labels: [:],
                        data:   [:]
                    ]
            ]

            params.findAll { it.key.startsWith(BaseConfig.FILTER_PREFIX) }.each { it ->
                if (it.value) {
                    cacheMap.filterCache.map.put(it.key, it.value) // println ' -------------> ' + it.key + ' : ' + it.value
                }
            }
            cacheMap.filterCache.labels.putAll( result.filterResult.labels )
            cacheMap.filterCache.data.putAll( result.filterResult.data )

            PageRenderer groovyPageRenderer = BeanStore.getGroovyPageRenderer()
            cacheMap.filterCache.result = groovyPageRenderer.render(
                    template: '/myInstitution/reporting/query/query_filterResult',
                    model: [ filter: params.filter, filterResult: result.filterResult ]
            ).replaceAll('\\s+', ' ').trim()

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, result.token as String)
            rCache.put( cacheMap )
        }

        render view: 'reporting/index', model: result
    }

    /**
     * Lists the platforms which are linked by any current subscription or subscription with perpetual access to the context institution.
     * The list results may be filtered by filter parameters
     * @return the platform list view
     * @see de.laser.wekb.Platform
     * @see Subscription
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentPlatforms() {
        Map<String, Object> result = [:]

        result.flagContentGokb = true // gokbService.doQuery
        SwissKnife.setPaginationParams(result, params, contextService.getUser())
        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.PLA_PROP], contextService.getOrg())
        Map<String, Object> baseSubscriptionParams = [contextOrg:contextService.getOrg(), roleTypes:[RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM]]
        Map<String, Object> subscriptionParams = baseSubscriptionParams.clone()
        baseSubscriptionParams.current = RDStore.SUBSCRIPTION_CURRENT

        String instanceFilter = "", subFilter = ""
        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        boolean withPerpetualAccess = params.long('hasPerpetualAccess') == RDStore.YN_YES.id
        if(params.status != 'FETCH_ALL') {
            subFilter += "s.status.id in (:status)"
            subscriptionParams.status = Params.getLongList(params, 'status')
            if(withPerpetualAccess) {
                if(RDStore.SUBSCRIPTION_CURRENT.id in subscriptionParams.status) {
                    subscriptionParams.expired = RDStore.SUBSCRIPTION_EXPIRED.id
                    subFilter += " or (s.status.id = :expired and s.hasPerpetualAccess = true) "
                }
                else subFilter += " and s.hasPerpetualAccess = true "
            }
            else if(params.long('hasPerpetualAccess') == RDStore.YN_NO.id) {
                subFilter += " and s.hasPerpetualAccess = false "
            }
        }


        if(contextService.getOrg().isCustomerType_Consortium())
            instanceFilter += " and s.instanceOf = null "
        String subscriptionQuery = 'select s.id from OrgRole oo join oo.sub s where oo.org = :contextOrg and oo.roleType in (:roleTypes) and ('+subFilter+')'+instanceFilter
        Set<Long> subIds = []
        String subQueryFilter = ""
        if(subFilter) {
            subIds.addAll(Subscription.executeQuery(subscriptionQuery, subscriptionParams))
            subQueryFilter = " and s.id in (:subIds)"
        }

        result.subscriptionMap = [:]
        result.platformInstanceList = []

        String qry3 = "select distinct plat, s, ${params.sort ?: 'plat.name'} from Platform plat join plat.provider p, SubscriptionPackage sp join sp.subscription s join sp.pkg pkg where plat = pkg.nominalPlatform " +
                "and plat.gokbId in (:wekbIds) and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted)) ${subQueryFilter}"

        Map qryParams3 = [
                pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED,
        ]
        if(subFilter)
            qryParams3.subIds = subIds

        Map<String, Object> queryParams = filterService.getWekbPlatformFilterParams(params)
        /*
        Map<String, Object> queryParams = [componentType: "Platform"]

        if (params.q?.length() > 0) {
            result.filterSet = true
            queryParams.name = params.q
        }

        if (params.filterPropDef) {
            result.filterSet = true
            Map<String, Object> psq = propertyService.evalFilterQuery(params, qry3, 'p', qryParams3)
            qry3 = psq.query
            qryParams3.putAll(psq.queryParams)
        }

        if(params.provider) {
            result.filterSet = true
            queryParams.provider = params.provider
        }

        if(Params.getLongList(params, 'platStatus')) {
            result.filterSet = true
            queryParams.status = RefdataValue.findAllByIdInList(Params.getLongList(params, 'platStatus')).value
        }
        else if(!params.filterSet) {
            result.filterSet = true
            queryParams.status = "Current"
            params.platStatus = RDStore.PLATFORM_STATUS_CURRENT.id
        }

        if(params.ipSupport) {
            result.filterSet = true
            queryParams.ipAuthentication = Params.getRefdataList(params, 'ipSupport').collect{ it.value }
        }
        if(params.shibbolethSupport) {
            result.filterSet = true
            queryParams.shibbolethAuthentication = Params.getRefdataList(params, 'shibbolethSupport').collect{ (it == RDStore.GENERIC_NULL_VALUE) ? 'null' : it.value }
        }
        if(params.counterCertified) {
            result.filterSet = true
            queryParams.counterCertified = Params.getRefdataList(params, 'counterCertified').collect{ (it == RDStore.GENERIC_NULL_VALUE) ? 'null' : it.value }
        }
        if(params.counterSushiSupport) {
            result.filterSet = true
            queryParams.counterSushiSupport = params.list('counterSushiSupport') //ask David about proper convention
        }
        if (params.curatoryGroup) {
            result.filterSet = true
            queryParams.curatoryGroupExact = params.curatoryGroup
        }
        if (params.curatoryGroupType) {
            result.filterSet = true
            queryParams.curatoryGroupType = params.curatoryGroupType
        }
        */
        List wekbIds = []
        Map<String, Object> wekbParams = params.clone()
        if(!wekbParams.containsKey('sort'))
            wekbParams.sort = 'name'
        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [componentType: "Platform"])
        if(queryCuratoryGroups.code == 404) {
            result.error = message(code: 'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                Set<String> myPlatformWekbIds = Platform.executeQuery('select plat.gokbId from Platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo join sp.pkg pkg where oo.org = :contextOrg and oo.roleType in (:roleTypes) and s.status = :current '+instanceFilter+')', baseSubscriptionParams)
                List myCuratoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" && it.platformUuid in myPlatformWekbIds }
                //post-filter because we:kb delivers the platform UUID in the map as well ==> uniqueness is explicitly given!
                Set curatoryGroupSet = []
                myCuratoryGroups.each { Map myCG ->
                    myCG.remove('platformUuid')
                    curatoryGroupSet << myCG
                }
                result.curatoryGroups = curatoryGroupSet
            }
            wekbIds.addAll(gokbService.doQuery([max:10000, offset:0], wekbParams, queryParams).records.collect { Map hit -> hit.uuid })
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        qryParams3.wekbIds = wekbIds

        qry3 += " group by plat, s"
        if(params.sort)
            qry3 += " order by ${params.sort} ${params.order}"
        else qry3 += " order by plat.name, s.name, s.startDate desc"

        List platformSubscriptionList = []
        if(wekbIds)
            platformSubscriptionList.addAll(Platform.executeQuery(qry3, qryParams3))

        log.debug("found ${platformSubscriptionList.size()} in list ..")
        /*, [max:result.max, offset:result.offset])) */

        platformSubscriptionList.each { entry ->
            Platform pl = (Platform) entry[0]
            Subscription s = (Subscription) entry[1]

            String key = 'platform_' + pl.id

            if (! result.subscriptionMap.containsKey(key)) {
                result.subscriptionMap.put(key, [])
                result.platformInstanceList.add(pl)
            }

            if (s.status.value == RDStore.SUBSCRIPTION_CURRENT.value || (withPerpetualAccess && s.hasPerpetualAccess && result.subscriptionMap.get(key).size() < 5)) {
                result.subscriptionMap.get(key).add(s)
            }
        }
        //}

        /*
        disused; we expect we:kb being the only source as of phone call from May 7th, '24
        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('wekb_exclusive')) {
                f1Result.addAll( result.platformInstanceList.findAll {
                    if (it.org) { return it.org.gokbId != null } else { return false }
                }.collect{ it.id } )
            }
            if (xFilter.contains('wekb_not')) {
                f1Result.addAll( result.platformInstanceList.findAll {
                    return it.org?.gokbId == null
                }.collect{ it.id } )
            }
            result.platformInstanceList = result.platformInstanceList.findAll { f1Result.contains(it.id) }
        }
        */

        result.platformInstanceTotal = result.platformInstanceList.size()
        result.cachedContent = true

        result
    }

    /**
     * Returns a view of the current licenses (and subscriptions linked to them) the institution holds. The results may be filtered
     * both for subscriptions and licenses. That means that a subscription filter is attached to the
     * query; licenses without a subscription may get lost if there is no subscription linked to it!
     * @return the license list view
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def currentLicenses() {

        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        EhcacheWrapper cache = contextService.getUserCache("/license/filter/")
        if(cache && cache.get('licenseFilterCache')) {
            if(!params.resetFilter && !params.filterSet)
                params.putAll((GrailsParameterMap) cache.get('licenseFilterCache'))
            else params.remove('resetFilter')
            cache.remove('licenseFilterCache') //has to be executed in any case in order to enable cache updating
        }
		Profiler prf = new Profiler()
		prf.setBenchmark('init')

        Date date_restriction = null
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.LIC_PROP], contextService.getOrg())
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.max      = params.format ? 10000 : result.max
        result.offset   = params.format? 0 : result.offset
        result.compare = params.compare ?: ''

        String base_qry
        Map qry_params

        result.filterSet = params.filterSet ? true : false
        if(result.filterSet) {
            cache.put('licenseFilterCache', params)
        }

        Set<String> licenseFilterTable = []
        if (! contextService.getOrg().isCustomerType_Support()) {
            licenseFilterTable << "provider"
            licenseFilterTable << "vendor"
            licenseFilterTable << "processing"
        }

        if (contextService.getOrg().isCustomerType_Inst_Pro()) {
            Set<RefdataValue> roleTypes = []
            if(params.licTypes) {
                Set<String> licTypes = params.list('licTypes')
                licTypes.each { String licTypeId ->
                    roleTypes << RefdataValue.get(licTypeId)
                }
            }
            else roleTypes.addAll([RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])
            base_qry = "from License as l where ( exists ( select o from l.orgRelations as o where ( ( o.roleType in (:roleTypes) ) AND o.org = :lic_org ) ) )"
            qry_params = [roleTypes: roleTypes, lic_org: contextService.getOrg()]
            if(result.editable)
                licenseFilterTable << "action"
            licenseFilterTable << "licensingConsortium"
        }
        else if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            base_qry = "from License as l where exists ( select o from l.orgRelations as o where ( o.roleType = :roleTypeC AND o.org = :lic_org AND l.instanceOf is null AND NOT exists ( select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL ) ) )"
            qry_params = [roleTypeC:RDStore.OR_LICENSING_CONSORTIUM, roleTypeL:RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
            licenseFilterTable << "memberLicenses"
            if(result.editable)
                licenseFilterTable << "action"
        }
        else {
            base_qry = "from License as l where exists ( select o from l.orgRelations as o where  o.roleType = :roleType AND o.org = :lic_org ) "
            qry_params = [roleType:RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
            licenseFilterTable << "licensingConsortium"
        }
        result.licenseFilterTable = licenseFilterTable

        if (params.consortium) {
            base_qry += " and ( exists ( select o from l.orgRelations as o where o.roleType = :licCons and o.org.id in (:cons) ) ) "
            List<Long> consortia = Params.getLongList(params, 'consortium')
            qry_params += [licCons:RDStore.OR_LICENSING_CONSORTIUM, cons:consortia]
        }

        if (date_restriction) {
            base_qry += " and ( ( l.startDate <= :date_restr and l.endDate >= :date_restr ) OR l.startDate is null OR l.endDate is null ) "
            qry_params += [date_restr: date_restriction]
            qry_params += [date_restr: date_restriction]
        }

        // eval property filter

        if (params.filterPropDef) {
            Map<String, Object> psq = propertyService.evalFilterQuery(params, base_qry, 'l', qry_params)
            base_qry = psq.query
            qry_params = psq.queryParams
        }

        if (params.provider) {
            base_qry += " and ( exists ( select pvr from l.providerRelations as pvr where pvr.provider.id in (:providers) ) ) "
            List<Long> providers = Params.getLongList(params, 'provider')
            qry_params += [providers:providers]
        }

        if (params.vendor) {
            base_qry += " and ( exists ( select vr from l.vendorRelations as vr where vr.vendor.id in (:vendors) ) ) "
            List<Long> vendors = Params.getLongList(params, 'vendor')
            qry_params += [vendors: vendors]
        }

        if (params.categorisation) {
            base_qry += " and l.licenseCategory.id in (:categorisations) "
            qry_params.categorisations = Params.getLongList(params, 'categorisation')
        }

        if(params.status || !params.filterSubmit) {
            base_qry += " and l.status.id = :status "
            if(!params.filterSubmit) {
                params.status = RDStore.LICENSE_CURRENT.id
                result.filterSet = true
            }
            qry_params.status = params.long('status')
        }


        if ((params['keyword-search'] != null) && (params['keyword-search'].trim().length() > 0)) {
            // filter by license
            base_qry += " and ( genfunc_filter_matcher(l.reference, :name_filter) = true "+
                    " or exists ( select orgR from OrgRole as orgR where orgR.lic = l and "+
                    "   orgR.roleType in (:licRoleTypes) and ( "+
                    " genfunc_filter_matcher(orgR.org.name, :name_filter) = true "+
                    " or genfunc_filter_matcher(orgR.org.sortname, :name_filter) = true "+
                    " ) ) " +
                    " or exists ( select li.id from Links li where li.sourceLicense = l and li.linkType = :linkType and genfunc_filter_matcher(li.destinationSubscription.name, :name_filter) = true ) " +
                    " or exists ( select altname.license from AlternativeName altname where altname.license = l and genfunc_filter_matcher(altname.name, :name_filter) = true ) " +
                    " or exists ( select li.id from AlternativeName altname, Links li where altname.subscription = li.destinationSubscription and li.sourceLicense = l and li.linkType = :linkType and genfunc_filter_matcher(altname.name, :name_filter) = true ) " +
                    " ) "
            qry_params.name_filter = params['keyword-search']
            qry_params.licRoleTypes = [RDStore.OR_LICENSOR, RDStore.OR_LICENSING_CONSORTIUM]
            qry_params.linkType = RDStore.LINKTYPE_LICENSE //map key will be overwritten if set twice
            result.keyWord = params['keyword-search']
        }

        if(params.subKind || params.subStatus || !params.filterSubmit) {
            Set<String> subscrQueryFilter = ["oo.org = :context"]
            qry_params.context = contextService.getOrg()

            if(params.subStatus || !params.filterSubmit) {
                subscrQueryFilter <<  "s.status.id = :subStatus"
                if(!params.filterSubmit) {
                    params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id
                    result.filterSet = true
                }
                qry_params.subStatus = params.long('subStatus')
            }

            if (params.subKind) {
                subscrQueryFilter << "s.kind.id in (:subKinds)"
                qry_params.subKinds = Params.getLongList(params, 'subKind')
            }

            if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
                subscrQueryFilter << "s.instanceOf is null"
            }

            base_qry += " and ( exists ( select li from Links li join li.destinationSubscription s left join s.orgRelations oo where li.sourceLicense = l and li.linkType = :linkType and "+subscrQueryFilter.join(" and ")+" ) )" //or ( not exists ( select li from Links li where li.sourceLicense = l and li.linkType = :linkType ) )
            qry_params.linkType = RDStore.LINKTYPE_LICENSE
        }


        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by l.${params.sort} ${params.order}"
        } else {
            base_qry += " order by lower(trim(l.reference)) asc"
        }

        prf.setBenchmark('execute query')
        log.debug("select l ${base_qry}")
        List<License> totalLicenses = License.executeQuery( "select l " + base_qry, qry_params )
        result.licenseCount = totalLicenses.size()
        prf.setBenchmark('get subscriptions')

        result.licenses = totalLicenses.drop((int) result.offset).take((int) result.max)
        if(result.licenses) {
            Set<Links> allLinkedSubscriptions = Subscription.executeQuery("select li from Links li join li.destinationSubscription s join s.orgRelations oo where li.sourceLicense in (:licenses) and li.linkType = :linkType and s.status.id = :status and oo.org = :context order by s.name", [licenses: result.licenses, linkType: RDStore.LINKTYPE_LICENSE, status: qry_params.subStatus, context: contextService.getOrg()])
            Map<License,Set<Subscription>> subscriptionLicenseMap = [:]
            allLinkedSubscriptions.each { Links li ->
                Set<Subscription> subscriptions = subscriptionLicenseMap.get(li.sourceLicense)
                if(!subscriptions)
                    subscriptions = []
                subscriptions << li.destinationSubscription
                subscriptionLicenseMap.put(li.sourceLicense,subscriptions)
            }
            result.allLinkedSubscriptions = subscriptionLicenseMap
        }
        List orgRoles = OrgRole.findAllByOrgAndLicIsNotNull(contextService.getOrg())
        result.orgRoles = [:]
        orgRoles.each { OrgRole oo ->
            result.orgRoles.put(oo.lic.id,oo.roleType)
        }
        prf.setBenchmark('get consortia')
        Set<Org> consortia = Org.executeQuery(
                "select os.org from OrgSetting os where os.key = 'CUSTOMER_TYPE' and os.roleValue in (select r from Role r where authority in (:consList)) order by os.org.name asc",
                [consList: ['ORG_CONSORTIUM_BASIC', 'ORG_CONSORTIUM_PRO']]
        )
        prf.setBenchmark('get licensors')
        Map<String,Set<Org>> orgs = [consortia:consortia]
        result.orgs = orgs
        result.providers = providerService.getCurrentProviders(contextService.getOrg())
        result.vendors = vendorService.getCurrentVendors(contextService.getOrg())

		List bm = prf.stopBenchmark()
		result.benchMark = bm

        SimpleDateFormat sdfNoPoint = DateUtils.getSDF_noTimeNoPoint()
        String filename = "${sdfNoPoint.format(new Date())}_${g.message(code: 'export.my.currentLicenses')}"
        List titles = [
                g.message(code:'license.details.reference'),
                g.message(code:'license.details.linked_subs'),
                g.message(code:'consortium'),
                g.message(code:'default.ProviderAgency.singular'),
                g.message(code:'license.startDate.label'),
                g.message(code:'license.endDate.label')
        ]
        Map<License,Set<License>> licChildMap = [:]
        List<License> childLicsOfSet = totalLicenses.isEmpty() ? [] : License.findAllByInstanceOfInList(totalLicenses)
        childLicsOfSet.each { License child ->
            Set<License> children = licChildMap.get(child.instanceOf)
            if(!children)
                children = []
            children << child
            licChildMap.put(child.instanceOf,children)
        }
        Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.LIC_PROP], contextService.getOrg())
        titles.addAll(exportService.loadPropListHeaders(propertyDefinitions))
        Map objectNames = [:]
        if(childLicsOfSet) {
            Set rows = OrgRole.executeQuery('select oo.sub,oo.org.sortname from OrgRole oo where oo.sub in (:subChildren) and oo.roleType = :licType',[subChildren:childLicsOfSet,licType:RDStore.OR_LICENSEE_CONS])
            rows.each { row ->
                //log.debug("now processing ${row[0]}:${row[1]}")
                objectNames.put(row[0],row[1])
            }
        }
        Map<String, Object> selectedFields = [:]

        if(params.fileformat) {
            if (params.filename) {
                filename = params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }
        switch(params.fileformat) {
            case 'xlsx':
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportLicenses(totalLicenses, selectedFields, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            case 'csv':
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportLicenses(totalLicenses, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
                return
            case 'pdf':
                Map<String, Object> pdfOutput = exportClickMeService.exportLicenses(totalLicenses, selectedFields, ExportClickMeService.FORMAT.PDF)

                byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
                return
        }
        result
        /*
        if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            List rows = []
            totalLicenses.each { License licObj ->
                License license = (License) licObj
                List row = [[field:license.reference.replaceAll(',',' '),style:'bold']]
                List linkedSubs = license.getSubscriptions(contextService.getOrg()).collect { sub ->
                    sub.name
                }
                row.add([field:linkedSubs.join(", "),style:null])
                row.add([field:license.licensingConsortium ? license.licensingConsortium.name : '',style:null])
                row.add([field:license.licensor ? license.licensor.name : '',style:null])
                row.add([field:license.startDate ? sdf.format(license.startDate) : '',style:null])
                row.add([field:license.endDate ? sdf.format(license.endDate) : '',style:null])
                row.addAll(exportService.processPropertyListValues(propertyDefinitions, 'xls', license, licChildMap, objectNames, contextService.getOrg()))
                rows.add(row)
            }
            Map sheetData = [:]
            sheetData[g.message(code:'menu.my.licenses')] = [titleRow:titles,columnData:rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else if(params.exportPDF) {
            result.licenses = totalLicenses
            Map<String, Object> pageStruct = [
                    width       : 85,
                    height      : 35,
                    pageSize    : 'A4',
                    orientation : 'Portrait'
            ]
            result.struct = [pageStruct.width, pageStruct.height, pageStruct.pageSize + ' ' + pageStruct.orientation]
            byte[] pdf = wkhtmltoxService.makePdf(
                    view: '/myInstitution/currentLicensesPdf',
                    model: result,
                    pageSize: pageStruct.pageSize,
                    orientation: pageStruct.orientation,
                    marginLeft: 10,
                    marginRight: 10,
                    marginTop: 15,
                    marginBottom: 15
            )
            response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
            response.setContentType('application/pdf')
            response.outputStream.withStream { it << pdf }
            return
        }
        */
            /*
        withFormat {
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                List rows = []
                totalLicenses.each { licObj ->
                    License license = (License) licObj
                    List row = [license.reference.replaceAll(',',' ')]
                    List linkedSubs = license.getSubscriptions(contextService.getOrg()).collect { sub ->
                        sub.name.replaceAll(',',' ')
                    }
                    row.add(linkedSubs.join("; "))
                    row.add(license.licensingConsortium)
                    row.add(license.licensor)
                    row.add(license.startDate ? sdf.format(license.startDate) : '')
                    row.add(license.endDate ? sdf.format(license.endDate) : '')
                    row.addAll(row.addAll(exportService.processPropertyListValues(propertyDefinitions, 'csv', license, licChildMap, objectNames, contextService.getOrg())))
                    rows.add(row)
                }
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(titles,rows,','))
                }
                out.close()
            }
        }
            */
    }

    /**
     * Call to create a new license
     * @return the form view to enter the new license parameters
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def emptyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (! (result.user as User).isFormal(contextService.getOrg())) {
            flash.error = message(code:'myinst.error.noMember', args:[contextService.getOrg().name]) as String
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return;
        }

        GregorianCalendar cal = new GregorianCalendar()
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        cal.setTimeInMillis(System.currentTimeMillis())
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        result.defaultStartYear = sdf.format(cal.getTime())

        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)

        result.defaultEndYear = sdf.format(cal.getTime())

        result.licenses = [] // ERMS-2431
        result.numLicenses = 0

        if (params.sub) {
            result.sub         = params.sub
            result.subInstance = Subscription.get(params.sub)
        }

        result
    }

    /**
     * Creates a new license based on the parameters submitted
     * @return the license details view ({@link LicenseController#show()}) of the new license record
     */
    @DebugInfo(isInstUser = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def processEmptyLicense() {
        License.withTransaction { TransactionStatus ts ->
            User user = contextService.getUser()
            Org org = contextService.getOrg()

            boolean isConsOrSupport     = org.isCustomerType_Consortium() || org.isCustomerType_Support()

            if (! contextService.isInstEditor()) {
                flash.error = message(code:'myinst.error.noAdmin', args:[org.name]) as String
                response.sendError(HttpStatus.SC_FORBIDDEN)
                // render(status: '403', text:"You do not have permission to access ${org.name}. Please request access on the profile page");
                return
            }
            License baseLicense = params.baselicense ? License.get(params.baselicense) : null
            //Nur wenn von Vorlage ist
            if (baseLicense) {
                if (! baseLicense.hasPerm("view", user)) {
                    log.debug("return 403....")
                    flash.error = message(code: 'myinst.newLicense.error') as String
                    response.sendError(HttpStatus.SC_FORBIDDEN)
                    return
                }
                else {
                    def copyLicense = institutionsService.copyLicense(baseLicense, params, InstitutionsService.CUSTOM_PROPERTIES_COPY_HARD)

                    if (copyLicense.hasErrors()) {
                        log.error("Problem saving license ${copyLicense.errors}")
                        render view: 'editLicense', model: [licenseInstance: copyLicense]
                    } else {
                        copyLicense.reference = params.licenseName
                        copyLicense.startDate = DateUtils.parseDateGeneric(params.licenseStartDate)
                        copyLicense.endDate = DateUtils.parseDateGeneric(params.licenseEndDate)

                        if (copyLicense.save()) {
                            flash.message = message(code: 'license.createdfromTemplate.message') as String
                        }

                        if( params.sub) {
                            Subscription subInstance = Subscription.get(params.sub)
                            subscriptionService.setOrgLicRole(subInstance,copyLicense,false)
                            //subInstance.owner = copyLicense
                            //subInstance.save(flush: true)
                        }

                        redirect controller: 'license', action: 'show', params: params, id: copyLicense.id
                        return
                    }
                }
            }

            License licenseInstance = new License(
                    reference: params.licenseName,
                    startDate:params.licenseStartDate ? DateUtils.parseDateGeneric(params.licenseStartDate) : null,
                    endDate: params.licenseEndDate ? DateUtils.parseDateGeneric(params.licenseEndDate) : null,
                    status: RefdataValue.get(params.status),
                    openEnded: RDStore.YNU_UNKNOWN
            )

            if (!licenseInstance.save()) {
                log.error(licenseInstance.errors.toString())
                flash.error = message(code:'license.create.error') as String
                redirect action: 'emptyLicense'
                return
            }
            else {
                log.debug("Save ok")

                log.debug("adding org link to new license")
                OrgRole orgRole
                if (isConsOrSupport) {
                    orgRole = new OrgRole(lic: licenseInstance, org: org, roleType: RDStore.OR_LICENSING_CONSORTIUM)
                } else {
                    orgRole = new OrgRole(lic: licenseInstance, org: org, roleType: RDStore.OR_LICENSEE)
                }

                if (!orgRole.save()) {
                    log.error("Problem saving org links to license ${orgRole.errors}");
                }

                redirect controller: 'license', action: 'show', params: params, id: licenseInstance.id
                return
            }
        }
    }

    /**
     * Opens a list of all provider / agency {@link Org}s which are linked by {@link OrgRole} to any subscription.
     * The list results may be filtered with filter parameters
     * @return a list of matching {@link Org} records, as html or as export pipe (Excel / CSV)
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentProviders() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params), queryParams = [:]
		Profiler prf = new Profiler()
		prf.setBenchmark('init')

        result.propList    = PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr = :def and (pd.tenant is null or pd.tenant = :tenant) order by pd.name_de asc", [
                def: PropertyDefinition.PRV_PROP,
                tenant: contextService.getOrg()
        ])

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List<String> queryArgs = []

        String providerQuery = 'select p from ProviderRole pr join pr.provider p, OrgRole oo join oo.sub s where pr.subscription = s and oo.org = :contextOrg'
        queryParams.contextOrg = contextService.getOrg()

        if(params.containsKey('subStatus')) {
            providerQuery += ' and (s.status in (:subStatus) '
            queryParams.subStatus = Params.getRefdataList(params, 'subStatus')
        }
        else {
            providerQuery += ' and (s.status = :subStatus '
            queryParams.subStatus = RDStore.SUBSCRIPTION_CURRENT
        }

        if(params.containsKey('subPerpetualAccess')) {
            boolean withPerpetualAccess = params.subPerpetualAccess == 'on'
            if(withPerpetualAccess) {
                if(queryParams.subStatus?.contains(RDStore.SUBSCRIPTION_CURRENT)) {
                    providerQuery += ' or (s.status = :expired and s.hasPerpetualAccess = true) '
                    queryParams.expired = RDStore.SUBSCRIPTION_EXPIRED
                }
                else providerQuery += ' and s.hasPerpetualAccess = true '
            }
            providerQuery += ')' //opened in line 1100 or 1105
            if(params.subPerpetualAccess == RDStore.YN_NO)
                providerQuery += ' and s.hasPerpetualAccess = false '
        }
        else providerQuery += ')' //opened in line 1100 or 1105

        result.filterSet = params.filterSet ? true : false
        if (params.filterPropDef) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, providerQuery, 'p', queryParams)
            providerQuery = efq.query
            queryParams = efq.queryParams as Map<String, Object>
        }

        Map queryCuratoryGroups = gokbService.executeQuery(Wekb.getGroupsURL(), [:])
        if(queryCuratoryGroups.error == 404) {
            result.error = message(code:'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            else result.curatoryGroups = []
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        if(params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(p.name, :name) = true or genfunc_filter_matcher(p.sortname, :name) = true)"
            queryParams.name = params.nameContains
        }
        if(params.containsKey('provStatus')) {
            queryArgs << "p.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'provStatus')
        }
        else if(!params.containsKey('provStatus') && !params.containsKey('filterSet')) {
            queryArgs << "p.status.value = :status"
            queryParams.status = "Current"
            params.provStatus = RDStore.PROVIDER_STATUS_CURRENT.id
        }

        if(params.containsKey('inhouseInvoicing')) {
            boolean inhouseInvoicing = params.inhouseInvoicing == 'on'
            if(inhouseInvoicing)
                queryArgs << "p.inhouseInvoicing = true"
            else queryArgs << "p.inhouseInvoicing = false"
        }

        if(params.containsKey('qp_invoicingVendors')) {
            queryArgs << "exists (select iv from p.invoicingVendors iv where iv.vendor.id in (:vendors))"
            queryParams.put('vendors', Params.getLongList(params, 'qp_invoicingVendors'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from p.electronicBillings eb where eb.invoicingFormat in (:electronicBillings))"
            queryParams.put('electronicBillings', Params.getRefdataList(params, 'qp_electronicBillings'))
        }

        if(params.containsKey('qp_invoiceDispatchs')) {
            queryArgs << "exists (select idi from p.invoiceDispatchs idi where idi.invoiceDispatch in (:invoiceDispatchs))"
            queryParams.put('invoiceDispatchs', Params.getRefdataList(params, 'qp_invoiceDispatchs'))
        }

        if(params.containsKey('curatoryGroup') || params.containsKey('curatoryGroupType')) {
            queryArgs << "p.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }
        if(queryArgs) {
            providerQuery += ' and '+queryArgs.join(' and ')
        }
        if(params.containsKey('sort')) {
            providerQuery += " order by ${params.sort} ${params.order ?: 'asc'}, p.name ${params.order ?: 'asc'} "
        }
        else
            providerQuery += " order by p.name "
        Set<Provider> providerListTotal = Provider.executeQuery(providerQuery, queryParams)

        result.wekbRecords = providerService.getWekbProviderRecords(params, result)

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('wekb_exclusive')) {
                f1Result.addAll( providerListTotal.findAll {it.gokbId != null }.collect{ it.id } )
            }
            if (xFilter.contains('wekb_not')) {
                f1Result.addAll( providerListTotal.findAll { it.gokbId == null }.collect{ it.id }  )
            }
            providerListTotal = providerListTotal.findAll { f1Result.contains(it.id) }
        }

        result.providersTotal = providerListTotal.size()
        result.allProviders = providerListTotal
        result.providerList = providerListTotal.drop((int) result.offset).take((int) result.max)

        String message = message(code: 'export.my.currentProviders') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        result.cachedContent = true

		List bm = prf.stopBenchmark()
		result.benchMark = bm

        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []

        if(params.fileformat) {
            if (params.filename) {
                filename = params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportProviders(providerListTotal, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)

                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportProviders(providerListTotal,selectedFields,ExportClickMeService.FORMAT.CSV,contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportProviders(providerListTotal, selectedFields, ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        result
    }

    /**
     * Opens a list of all {@link Vendor}s which are linked by {@link de.laser.wekb.VendorRole} to any subscription.
     * The list results may be filtered with filter parameters
     * @return a list of matching {@link Vendor} records, as html or as export pipe (Excel / CSV)
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentVendors() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        Profiler prf = new Profiler()
        prf.setBenchmark('init')

        /*
        EhcacheWrapper cache = contextService.getOrgCache('MyInstitutionController/currentProviders')
        List<Long> orgIds = []

        if (cache.get('orgIds')) {
            orgIds = cache.get('orgIds')
            log.debug('orgIds from cache')
        }
        else {
            orgIds = (orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( contextService.getOrg() )).toList()
            cache.put('orgIds', orgIds)
        }
         */

        result.propList    = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.VEN_PROP], contextService.getOrg())

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.filterSet = params.filterSet ? true : false
        /*
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
        */
        result.wekbRecords = vendorService.getWekbVendorRecords(params, result)
        /*
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        */

        result.flagContentGokb = true // vendorService.getWekbVendorRecords()
        String query = "select v from VendorRole vr join vr.vendor v, OrgRole oo join oo.sub s where vr.subscription = s and oo.org = :contextOrg"

        Map<String, Object> queryParams = [contextOrg: contextService.getOrg()]
        if (params.filterPropDef) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, query, 'v', queryParams)
            query = efq.query
            queryParams = efq.queryParams as Map<String, Object>
        }

        if(params.containsKey('subStatus')) {
            query += ' and (s.status in (:status) '
            queryParams.status = Params.getRefdataList(params, 'subStatus')
        }
        else if(!params.containsKey('filterSet')) {
            query += ' and (s.status = :status '
            queryParams.status = RDStore.SUBSCRIPTION_CURRENT
            params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id
        }

        if(params.containsKey('subPerpetualAccess')) {
            boolean withPerpetualAccess = params.subPerpetualAccess == RDStore.YN_YES
            if(withPerpetualAccess) {
                if(queryParams.status?.contains(RDStore.SUBSCRIPTION_CURRENT)) {
                    query += ' or (s.status = :expired and s.hasPerpetualAccess = true) '
                    queryParams.expired = RDStore.SUBSCRIPTION_EXPIRED
                }
                else query += ' and s.hasPerpetualAccess = true '
            }
            query += ')' //opened in line 1100 or 1105
            if(params.subPerpetualAccess == RDStore.YN_NO)
                query += ' and s.hasPerpetualAccess = false '
        }
        else if(params.containsKey('subStatus') || !params.containsKey('filterSet')) query += ')' //opened in line 1100 or 1105
        Set<String> queryArgs = []
        if(params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(v.name, :name) = true or genfunc_filter_matcher(v.sortname, :name) = true)"
            queryParams.put('name', params.nameContains)
        }
        if(params.containsKey('qp_supportedLibrarySystems')) {
            queryArgs << "exists (select ls from v.supportedLibrarySystems ls where ls.librarySystem in (:librarySystems))"
            queryParams.put('librarySystems', Params.getRefdataList(params, 'qp_supportedLibrarySystems'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from v.electronicBillings eb where eb.invoicingFormat in (:electronicBillings))"
            queryParams.put('electronicBillings', Params.getRefdataList(params, 'qp_electronicBillings'))
        }

        if(params.containsKey('qp_invoiceDispatchs')) {
            queryArgs << "exists (select idi from v.invoiceDispatchs idi where idi.invoiceDispatch in (:invoiceDispatchs))"
            queryParams.put('invoiceDispatchs', Params.getRefdataList(params, 'qp_invoiceDispatchs'))
        }

        if(params.containsKey('curatoryGroup') || params.containsKey('curatoryGroupType')) {
            queryArgs << "v.gokbId in (:wekbIds)"
            queryParams.wekbIds = result.wekbRecords.keySet()
        }
        if(queryArgs) {
            query += ' and '+queryArgs.join(' and ')
        }
        if(params.containsKey('sort')) {
            query += " order by ${params.sort} ${params.order ?: 'asc'}, v.name ${params.order ?: 'asc'} "
        }
        else
            query += " order by v.name "

        String currentSubQuery = "select vr from VendorRole vr, OrgRole oo join oo.sub s where vr.subscription = s and s.status = :current and oo.org = :contextOrg order by s.name, s.startDate desc"
        Map<String, Object> currentSubParams = [current: RDStore.SUBSCRIPTION_CURRENT, contextOrg: contextService.getOrg()]
        List<VendorRole> vendorRoleRows = VendorRole.executeQuery(currentSubQuery, currentSubParams)
        result.currentSubscriptions = [:]
        vendorRoleRows.each { VendorRole vr ->
            Set<Subscription> currentSubscriptions = result.currentSubscriptions.containsKey(vr.vendor.id) ? result.currentSubscriptions.get(vr.vendor.id) : []
            currentSubscriptions << vr.subscription
            result.currentSubscriptions.put(vr.vendor.id, currentSubscriptions)
        }

        Set<Vendor> vendorsTotal = Vendor.executeQuery(query, queryParams)

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('wekb_exclusive')) {
                f1Result.addAll( vendorsTotal.findAll {it.gokbId != null }.collect{ it.id } )
            }
            if (xFilter.contains('wekb_not')) {
                f1Result.addAll( vendorsTotal.findAll { it.gokbId == null }.collect{ it.id }  )
            }
            vendorsTotal = vendorsTotal.findAll { f1Result.contains(it.id) }
        }
        result.vendorListTotal = vendorsTotal.size()
        result.allVendors = vendorsTotal
        result.vendorList = vendorsTotal.drop(result.offset).take(result.max)

        String message = message(code: 'export.my.currentVendors') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        //result.cachedContent = true

        List bm = prf.stopBenchmark()
        result.benchMark = bm

        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []

        if(params.fileformat) {
            if (params.filename) {
                filename = params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)

                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportVendors(vendorsTotal,selectedFields, ExportClickMeService.FORMAT.CSV,contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else
            result
    }

    /**
     * Retrieves the list of subscriptions the context institution currently holds. The query may be restricted by filter parameters.
     * Default filter setting is status: current or with perpetual access
     * @return a (filtered) list of subscriptions, either as direct html output or as export stream (CSV, Excel)
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def currentSubscriptions() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.tableConfig = ['showActions','showLicense']
        if (! contextService.getOrg().isCustomerType_Support()) {
            result.tableConfig << "showPackages"
            result.tableConfig << "showProviders"
            result.tableConfig << "showVendors"
            result.tableConfig << "showInvoicing"
        }

        result.putAll(subscriptionService.getMySubscriptions(params, result.user, contextService.getOrg()))

        result.compare = params.compare ?: ''

        // Write the output to a file
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String filename = "${datetoday}_" + g.message(code: "export.my.currentSubscriptions")

        Map<String, Object> selectedFields = [:]

        if(params.fileformat) {
            if (params.filename) {
                filename =params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }
        switch(params.fileformat) {
            case 'xlsx':
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            case 'pdf':
                Map<String, Object> pdfOutput = exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, ExportClickMeService.FORMAT.PDF)

                byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
                return
            case 'csv':
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
                return
        }
        result
    }


    /**
     * Call for the consortium member management views. This method dispatches the call to the appropriate
     * tab view and returns the data for the called tab
     * @return one of the following views:
     * <ol>
     *     <li>generalProperties</li>
     *     <li>linkLicense</li>
     *     <li>linkPackages</li>
     *     <li>properties</li>
     *     <li>providerAgency</li>
     *     <li>documents</li>
     *     <li>notes</li>
     *     <li>multiYear</li>
     *     <li>customerIdentifiers</li>
     * </ol>
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def subscriptionsManagement() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        Profiler prf = new Profiler('subMgmt')
        prf.setBenchmark('start loading data')
        params.tab = params.tab ?: 'generalProperties'
        EhcacheWrapper filterCache = contextService.getUserCache("/subscriptionsManagement/subscriptionFilter/")
        Set<String> filterFields = ['q', 'identifier', 'referenceYears', 'status', 'filterPropDef', 'filterProp', 'form', 'resource', 'subKinds', 'isPublicForApi', 'hasPerpetualAccess', 'hasPublishComponent', 'holdingSelection', 'subRunTime', 'subRunTimeMultiYear', 'subType', 'consortia']
        filterFields.each { String subFilterKey ->
            if(params.containsKey('processOption')) {
                if(filterCache.get(subFilterKey))
                    params.put(subFilterKey, filterCache.get(subFilterKey))
            }
            else {
                if(params.get(subFilterKey))
                    filterCache.put(subFilterKey, params.get(subFilterKey))
                else filterCache.remove(subFilterKey)
            }
        }

        if(!(params.tab in ['notes', 'documents', 'properties'])){
            //Important
            if (!contextService.getOrg().isCustomerType_Consortium()) {
                if (Params.getLongList(params, 'subTypes').contains(RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id)){
                    flash.error = message(code: 'subscriptionsManagement.noPermission.forSubsWithTypeConsortial') as String
                }
                params.subTypes = [RDStore.SUBSCRIPTION_TYPE_LOCAL.id]
            }
        }
        prf.setBenchmark('get data')
        if(params.tab == 'documents' && params.processOption == 'newDoc') {
            def input_file = request.getFile("upload_file")
            if (input_file.size == 0) {
                flash.error = message(code: 'template.emptyDocument.file') as String
                redirect(url: request.getHeader('referer'))
                return
            }
            params.original_filename = input_file.originalFilename
            params.mimeType = input_file.contentType
            result << managementService.subscriptionsManagement(this, params, input_file)
        }
        else if (params.tab == 'documents' && params.bulk_op) {
            docstoreService.bulkDocOperation(params, result, flash)
            result << managementService.subscriptionsManagement(this, params)
        }else{
            result << managementService.subscriptionsManagement(this, params)
        }
        //at end because cache may get cleared after a process
        EhcacheWrapper paginationCache = cacheService.getTTL1800Cache("/${params.controller}/subscriptionManagement/${params.tab}/${result.user.id}/pagination")
        result.selectionCache = paginationCache.checkedMap ?: [:]
        result.benchMark = prf.stopBenchmark()
        result

    }

    /**
     * Connects the context subscription with the given pair
     * @return void, redirects to referer
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def linkObjects() {
        Map<String,Object> ctrlResult = linksGenerationService.createOrUpdateLink(params)
        if(ctrlResult.status == LinksGenerationService.STATUS_ERROR)
            flash.error = ctrlResult.error
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Removes the given link
     * @return void, redirects to referer
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unlinkObjects() {
        linksGenerationService.deleteLink(params.oid)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Opens the documents view for the given institution; here, the private document section is being opened
     * @return the table view of documents linked to the institution itself (i.e. the interior document sharing section)
     * @see Doc
     * @see DocContext
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    Map documents() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (params.bulk_op) {
            docstoreService.bulkDocOperation(params, result, flash)
        }
        result
    }

    /**
     * Call to delete a given document
     * @return the document table view ({@link #documents()})
     * @see DocstoreService#deleteDocument()
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deleteDocuments() {
        docstoreService.deleteDocument(params)

        String redir
        if(params.redirectAction == 'subscriptionsManagement') {
            redir = 'subscriptionsManagement'
        }
        else if(params.redirectAction == 'currentSubscriptionsTransfer'){
            redirect(uri: request.getHeader('referer'))
            return
        }
        else if(params.redirectAction) {
            redir = params.redirectAction
        }

        redirect controller: 'myInstitution', action: redir ?: 'documents', params: redir == 'subscriptionsManagement' ? [tab: 'documents'] : null
    }

    /**
     * Opens a list of current issue entitlements hold by the context institution. The result may be filtered;
     * filters hold for the title, the platforms, provider and subscription parameters
     * @return a (filtered) list of issue entitlements
     * @see Subscription
     * @see Platform
     * @see IssueEntitlement
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Deprecated
    def currentTitles() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
		Profiler prf = new Profiler()
		prf.setBenchmark('init')

        Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'IEs')
        if (ttParams.status) { params.status = ttParams.status }
        if (ttParams.tab)    { params.tab = ttParams.tab }

        Set<RefdataValue> orgRoles = []
        List<String> queryFilter = [], subscriptionQueryFilter = []

        if (contextService.getOrg().isCustomerType_Consortium()) {
            orgRoles << RDStore.OR_SUBSCRIPTION_CONSORTIUM
            subscriptionQueryFilter << " sub.instanceOf = null "
        }
        else {
            orgRoles << RDStore.OR_SUBSCRIBER
            orgRoles << RDStore.OR_SUBSCRIBER_CONS
        }

        /* Set Date Restriction
        Date checkedDate = null

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        boolean defaultSet = false
        if (params.validOn == null) {
            result.validOn = sdf.format(new Date())
            checkedDate = sdf.parse(result.validOn)
            defaultSet = true
            log.debug("Getting titles as of ${checkedDate} (current)")
        } else if (params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            checkedDate = sdf.parse(params.validOn)
            log.debug("Getting titles as of ${checkedDate} (given)")
        }
        */

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List<Long> filterSub = []
        if(params.containsKey('filterSub')) {
            //it is unclear for me why it is unable to use the params.list() method resp. why Grails does not recognise the filter param as list ...
            if(params.filterSub.contains(',')) {
                params.filterSub.split(',').each { String oid ->
                    filterSub << Long.parseLong(oid.split(':')[1])
                }
            }
            else if(params.filterSub.length() > 0) {
                filterSub << Long.parseLong(params.filterSub.split(':')[1])
            }
        }
        List<Provider> filterPvd = []
        if(params.containsKey('filterPvd')) {
            if(params.filterPvd.contains(',')) {
                params.filterPvd.split(',').each { String oid ->
                    Provider pvd = genericOIDService.resolveOID(oid)
                    if(pvd)
                        filterPvd << pvd
                }
            }
            else {
                Provider pvd = genericOIDService.resolveOID(params.filterPvd)
                if(pvd)
                    filterPvd << pvd
            }
        }
        List<Vendor> filterVen = []
        if(params.containsKey('filterVen')) {
            if(params.filterVen.contains(',')) {
                params.filterVen.split(',').each { String oid ->
                    Vendor ven = genericOIDService.resolveOID(oid)
                    if(ven)
                        filterVen << ven
                }
            }
            else {
                Vendor ven = genericOIDService.resolveOID(params.filterVen)
                if(ven)
                    filterVen << ven
            }
        }
        List<Platform> filterHostPlat = []
        if(params.containsKey('filterHostPlat')) {
            if(params.filterHostPlat.contains(',')) {
                params.filterHostPlat.split(',').each { String oid ->
                    Platform hostPlat = genericOIDService.resolveOID(oid)
                    if(hostPlat)
                        filterHostPlat << hostPlat
                }
            }
            else {
                Platform hostPlat = genericOIDService.resolveOID(params.filterHostPlat)
                if(hostPlat)
                    filterHostPlat << hostPlat
            }
        }
        log.debug("Using params: ${params}")

        Map<String,Object> qryParams = [:], subQryParams = [
                institution: contextService.getOrg(),
                //removed: RDStore.TIPP_STATUS_REMOVED
        ]

        /*
        if(checkedDate) {
            queryFilter << ' ( :checkedDate >= coalesce(ie.accessStartDate,sub.startDate,tipp.accessStartDate) or (ie.accessStartDate is null and sub.startDate is null and tipp.accessStartDate is null) ) and ( :checkedDate <= coalesce(ie.accessEndDate,sub.endDate,tipp.accessEndDate) or (ie.accessEndDate is null and sub.endDate is null and tipp.accessEndDate is null)  or (sub.hasPerpetualAccess = true))'
            queryFilter << ' (ie.accessStartDate <= :checkedDate or ' +
                              '(ie.accessStartDate is null and ' +
                                '(sub.startDate <= :checkedDate or ' +
                                  '(sub.startDate is null and ' +
                                    '(tipp.accessStartDate <= :checkedDate or tipp.accessStartDate is null)' +
                                  ')' +
                                ')' +
                              ')' +
                            ') and ' +
                            '(ie.accessEndDate >= :checkedDate or ' +
                              '(ie.accessEndDate > :checkedDate and sub.hasPerpetualAccess = true) or ' +
                                '(ie.accessEndDate is null and ' +
                                  '(sub.endDate >= :checkedDate or ' +
                                    '(sub.endDate > :checkedDate and sub.hasPerpetualAccess = true) or ' +
                                      '(sub.endDate is null and ' +
                                        '(tipp.accessEndDate >= :checkedDate or ' +
                                          '(tipp.accessEndDate > :checkedDate and sub.hasPerpetualAccess = true) or ' +
                                        'tipp.accessEndDate is null)' +
                                      ')' +
                                  ')' +
                                ')' +
                            ')'
            qryParams.checkedDate = checkedDate
        }*/

        if ((params.filter) && (params.filter.length() > 0)) {
            queryFilter << "ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where genfunc_filter_matcher(tipp.name, :titlestr) = true or genfunc_filter_matcher(tipp.firstAuthor, :titlestr) = true or genfunc_filter_matcher(tipp.firstEditor, :titlestr) = true )"
            qryParams.titlestr = params.filter
        }

        if (filterSub) {
            subscriptionQueryFilter << "sub.id in (:selSubs)"
            subQryParams.selSubs = filterSub
        }
        else {
            //temp restriction on current subscriptions
            subscriptionQueryFilter << "(sub.status = :subStatus or sub.hasPerpetualAccess = true)"
            subQryParams.subStatus = RDStore.SUBSCRIPTION_CURRENT
        }
        String pkgJoin = ""
        if (filterHostPlat) {
            pkgJoin = "join sub.packages sp join sp.pkg pkg"
            subscriptionQueryFilter << "pkg.nominalPlatform in (:platforms)"
            subQryParams.platforms = filterHostPlat
        }

        if (filterPvd) {
            pkgJoin = "join sub.packages sp join sp.pkg pkg"
            subscriptionQueryFilter << "pkg.provider = :selPvd"
            subQryParams.selPvd = filterPvd
        }
        if (filterVen) {
            pkgJoin = "join sub.packages sp join sp.pkg pkg"
            subscriptionQueryFilter << "pkg in (select pv.pkg from PackageVendor pv where pv.vendor in (:selVen))"
            subQryParams.selVen = filterVen
        }
        Set<Long> subIds = Subscription.executeQuery("select sub.id from Subscription sub join sub.orgRelations oo "+pkgJoin+" where oo.org = :institution and "+subscriptionQueryFilter.join(" and "), subQryParams)
        qryParams.subIds = subIds
        List<String> countQueryFilter = queryFilter.clone()
        Map<String, Object> countQueryParams = qryParams.clone()
        prf.setBenchmark('before sub IDs')

        if (params.status) {
            queryFilter << "ie.status in (:status)"
            qryParams.status = Params.getRefdataList(params, 'status')
        }
        countQueryFilter << "ie.status != :removed"
        countQueryParams.removed = RDStore.TIPP_STATUS_REMOVED

        //String havingClause = params.filterMultiIE ? 'having count(ie.ie_id) > 1' : ''


        String qryString = "from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id in (:subIds) "
        //String qryString = "from TitleInstancePackagePlatform tipp where exists (select ie.id from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo join sub.packages sp join sp.pkg pkg where ie.tipp = tipp and oo.org = :institution "

        String countQueryString = " from IssueEntitlement ie where ie.subscription.id in (:subIds) "
        if(queryFilter) {
            qryString += ' and ' + queryFilter.join(' and ')
        }
        if(countQueryFilter) {
            countQueryString += ' and ' + countQueryFilter.join(' and ')
        }

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256")
        Map<String, Object> cachingKeys = params.clone()
        cachingKeys.remove("controller")
        cachingKeys.remove("action")
        cachingKeys.remove("offset")
        cachingKeys.remove("max")
        String checksum = "${result.user.id}_${cachingKeys.entrySet().join('_')}"
        messageDigest.update(checksum.getBytes())
        EhcacheWrapper subCache = cacheService.getTTL300Cache("/myInstitution/currentTitles/subCache/${messageDigest.digest().encodeHex()}")
        if(!params.containsKey('fileformat')) {
            prf.setBenchmark('before counts')
            List counts = subCache.get('counts')
            if(!counts) {
                counts = IssueEntitlement.executeQuery('select new map(count(*) as count, ie.status as status) ' + countQueryString + ' group by ie.status', countQueryParams)
                subCache.put('counts', counts)
            }
            result.allIECounts = 0
            counts.each { row ->
                switch (row['status']) {
                    case RDStore.TIPP_STATUS_CURRENT: result.currentIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_EXPECTED: result.plannedIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_RETIRED: result.expiredIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_DELETED: result.deletedIECounts = row['count']
                        break
                }
                result.allIECounts += row['count']
            }
            switch(params.tab) {
                case 'currentIEs': result.num_ti_rows = result.currentIECounts
                    break
                case 'plannedIEs': result.num_ti_rows = result.plannedIECounts
                    break
                case 'expiredIEs': result.num_ti_rows = result.expredIECounts
                    break
                case 'deletedIEs': result.num_ti_rows = result.deletedIECounts
                    break
                case 'allIEs': result.num_ti_rows = result.allIECounts
                    break
            }
            /*result.currentIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_CURRENT, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.plannedIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_EXPECTED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.expiredIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_RETIRED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.deletedIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_DELETED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.allIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status in (:status) and ie.status != :ieStatus', qryParamsClone + [status: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED], ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]*/
        }

        //add order by clause because of group by clause in the count query
        String orderByClause
        if ((params.sort != null) && (params.sort.length() > 0)) {
            orderByClause = " order by ${params.sort} ${params.order}, tipp.sortname "
        }
        else {
            if (params.order == 'desc') {
                orderByClause = ' order by tipp.sortname desc, tipp.name desc'
            } else {
                orderByClause = ' order by tipp.sortname asc, tipp.name asc'
            }
        }
        qryString += orderByClause
        //log.debug(qryString.replace(':subIds', subIds.join(',')))

        Map<String, Object> selectedFields = [:]
        Set<Long> allTitles = subCache.get("titleIDs") ?: []
        if(subIds) {
            if(params.containsKey('fileformat') && params.fileformat != 'kbart') {
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            }
            else if(!params.containsKey('fileformat')) {
                prf.setBenchmark('before tipp IDs')
                if(!allTitles) {
                    allTitles = TitleInstancePackagePlatform.executeQuery('select tipp.id '+qryString,qryParams)
                    subCache.put("titleIDs", allTitles)
                }
                prf.setBenchmark('before full objects')
                result.titles = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:tippIDs)'+orderByClause, [tippIDs: allTitles.drop(result.offset).take(result.max)])
            }
        }

        String filename = "${message(code:'export.my.currentTitles')}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}",
               tokenBase = "${result.user}${params.values().join('')}${params.fileformat}"
        messageDigest.update(tokenBase.getBytes())
        String token = messageDigest.digest().encodeHex()

		List bm = prf.stopBenchmark()
		result.benchMark = bm
        if(params.containsKey('fileformat')) {
            String dir = GlobalService.obtainTmpFileLocation()
            File f = new File(dir+'/'+token)
            Map fileResult
            switch(params.fileformat) {
                case 'kbart':
                    if(!f.exists()) {
                        Map<String, Object> configMap = params.clone()
                        configMap.defaultSubscriptionFilter = true
                        //configMap.validOn = checkedDate.getTime()
                        if(filterSub)
                            configMap.subscriptions = Subscription.findAllByIdInList(filterSub)
                        /*
                        else
                            configMap.subscriptions = SubscriptionPackage.executeQuery('select s.id from Subscription s join s.orgRelations oo where oo.org = :context and oo.roleType in (:subscrTypes) and s.status = :current'+instanceFilter, [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg(), subscrTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS]]).toSet()
                        */
                        Map<String,Collection> tableData = exportService.generateTitleExport(configMap) //TODO migrate if method will be reactivated
                        if(tableData.columnData.size() > 0) {
                            FileOutputStream out = new FileOutputStream(f)
                            out.withWriter { writer ->
                                writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.columnData,'\t'))
                            }
                            out.flush()
                            out.close()
                            fileResult = [token: token, filenameDisplay: filename, fileformat: 'kbart']
                        }
                    }
                    break
                case 'xlsx':
                    if(!f.exists()) {
                        if(!allTitles) {
                            allTitles = TitleInstancePackagePlatform.executeQuery('select tipp.id '+qryString,qryParams)
                            subCache.put("titleIDs", allTitles)
                        }
                        SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(allTitles, selectedFields, ExportClickMeService.FORMAT.XLS)
                        FileOutputStream out = new FileOutputStream(f)
                        wb.write(out)
                        out.flush()
                        out.close()
                        wb.dispose()
                        /*
                        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
                        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        Map<String, Object> configMap = [:]
                        configMap.putAll(params)
                        configMap.validOn = checkedDate.getTime()
                        String consFilter = contextService.getOrg().isCustomerType_Consortium() ? ' and s.instanceOf is null' : ''
                        configMap.pkgIds = SubscriptionPackage.executeQuery('select sp.pkg.id from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where oo.org = :context and oo.roleType in (:subscrTypes)'+consFilter, [context: contextService.getOrg(), subscrTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS]])
                        Map<String,List> export = exportService.generateTitleExportCustom(configMap, IssueEntitlement.class.name) //all subscriptions, all packages
                        Map sheetData = [:]
                        sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
                        SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
                        workbook.write(response.outputStream)

                        response.outputStream.flush()
                        response.outputStream.close()
                        */
                    }
                    fileResult = [token: token, filenameDisplay: filename, fileformat: 'xlsx']
                    break
                case 'csv':
                    //response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                    //response.contentType = "text/csv"
                    //ServletOutputStream out = response.outputStream
                    if(!f.exists()) {
                        if(!allTitles) {
                            allTitles = TitleInstancePackagePlatform.executeQuery('select tipp.id '+qryString,qryParams)
                            subCache.put("titleIDs", allTitles)
                        }
                        FileOutputStream out = new FileOutputStream(f)
                        out.withWriter { writer ->
                            writer.write(exportClickMeService.exportTipps(allTitles,selectedFields,ExportClickMeService.FORMAT.CSV))
                        }
                        out.flush()
                        out.close()
                    }
                    fileResult = [token: token, filenameDisplay: filename, fileformat: 'csv']
                    break
            }
            render template: '/templates/bulkItemDownload', model: fileResult
        }
        else result
    }

    /**
     * Opens a list view of the current set of titles which have been subscribed permanently.
     * The list may be filtered
     * @return a list of permanent titles with the given status
     * @see PermanentTitle
     * @see FilterService#getPermanentTitlesQuery(grails.web.servlet.mvc.GrailsParameterMap, de.laser.Org)
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentPermanentTitles() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'IEs', true)
        if (ttParams.status) { params.status = ttParams.status }
        if (ttParams.tab)    { params.tab = ttParams.tab }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Map query = filterService.getPermanentTitlesQuery(params, contextService.getOrg())
        result.filterSet = query.filterSet
        Set tipps = TitleInstancePackagePlatform.executeQuery("select pt.tipp.id " + query.query, query.queryParams)
        result.tippIDs = tipps

        result.num_tipp_rows = tipps.size()

        String orderClause = 'order by tipp.sortname'
        if(params.sort){
                if(params.sort.contains('sortname'))
                    orderClause = "order by tipp.sortname ${params.order}, tipp.name ${params.order} "
                else
                    orderClause = "order by ${params.sort} ${params.order} "
        }
        Set filteredIDs = result.tippIDs.drop(result.offset).take(result.max)
        result.titles = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:tippIDs) '+orderClause, [tippIDs: filteredIDs])

        result.currentTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: contextService.getOrg(), status: RDStore.TIPP_STATUS_CURRENT, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.plannedTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: contextService.getOrg(), status: RDStore.TIPP_STATUS_EXPECTED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.expiredTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: contextService.getOrg(), status: RDStore.TIPP_STATUS_RETIRED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.deletedTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: contextService.getOrg(), status: RDStore.TIPP_STATUS_DELETED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.allTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status in (:status) and pt.issueEntitlement.status != :ieStatus", [org: contextService.getOrg(), status: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED], ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]

        //for tipp_ieFilter
        params.institution = contextService.getOrg().id
        params.filterForPermanentTitle = true

        result
    }

    /**
     * Opens a list of current packages subscribed by the context institution. The result may be filtered
     * by filter parameters
     * @return a list view of packages the institution has subscribed
     * @see SubscriptionPackage
     * @see de.laser.wekb.Package
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentPackages() {

        Map<String, Object> wekbQryParams = params.clone(), result = [:]
        SwissKnife.setPaginationParams(result, params, contextService.getUser())

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        Map<String, Object> subQryParams = params.clone() //with default status
        subQryParams.remove('sort')

        List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(subQryParams)
        result.filterSet = tmpQ[2]
        List currentSubIds = Subscription.executeQuery( "select s.id " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]

        List idsCategory1 = OrgRole.executeQuery("select distinct (sub.id) from OrgRole where org=:org and roleType in (:roleTypes)", [
                org: contextService.getOrg(), roleTypes: [
                RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS
        ]
        ])
        List idsCategory2 = OrgRole.executeQuery("select distinct (sub.id) from OrgRole where org=:org and roleType in (:roleTypes)", [
                org: contextService.getOrg(), roleTypes: [
                RDStore.OR_SUBSCRIPTION_CONSORTIUM
        ]
        ])

        result.subscriptionMap = [:]
        result.records = []
        result.packageListTotal = 0

        if(currentSubIds) {
            long start = System.currentTimeMillis()
            String qry3 = "select distinct pkg.gokbId, s, pkg.name, (select o.name from OrgRole oo join oo.org o where oo.pkg = pkg) as provider, pkg.nominalPlatform.name as nominalPlatform, (select count(*) from TitleInstancePackagePlatform tipp where tipp.pkg = pkg and tipp.status = :current) as tippCount from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg " +
                    "where s.id in (:currentSubIds) "

            qry3 += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"

            Map qryParams3 = [
                    currentSubIds  : currentSubIds,
                    current: RDStore.TIPP_STATUS_CURRENT,
                    pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED
            ]

            qry3 += " group by pkg, s, provider, pkg.nominalPlatform.name"
            String sort
            switch(params.sort) {
                case [null, 'name']: sort = "pkg.name"
                    break
                case "currentTippCount": sort = "tippCount"
                    break
                case "nominalPlatform.name": sort = "nominalPlatform"
                    break
                case "provider.name": sort = "provider"
                    break
                default: sort = params.sort
                    break
            }
            qry3 += " order by ${sort} " + (params.order ?: 'asc') + ", pkg.name asc"
            List packageSubscriptionList = Subscription.executeQuery(qry3, qryParams3)
            /*, [max:result.max, offset:result.offset])) */
            Set<String> currentPackageUuids = packageSubscriptionList.collect { entry -> entry[0] }
            wekbQryParams.uuids = currentPackageUuids
            wekbQryParams.max = currentPackageUuids.size()
            wekbQryParams.offset = 0
            wekbQryParams.my = true
            Map<String, Object> remote = packageService.getWekbPackages(wekbQryParams)
            result.curatoryGroups = remote.curatoryGroups
            result.curatoryGroupTypes = remote.curatoryGroupTypes
            result.automaticUpdates = remote.automaticUpdates
            result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)
            result.languages = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)
            Set tmp = []
            packageSubscriptionList.eachWithIndex { entry, int i ->
                String key = 'package_' + entry[0]

                if (! result.subscriptionMap.containsKey(key)) {
                    result.subscriptionMap.put(key, [])
                }
                boolean display

                if(params.status == "FETCH_ALL") {
                    if(entry[1].status?.id == RDStore.SUBSCRIPTION_EXPIRED.id) {
                        display = (entry[1].hasPerpetualAccess || result.subscriptionMap.get(key).size() < 5)
                    }
                    else display = true
                }
                else if(RDStore.SUBSCRIPTION_CURRENT.id in Params.getLongList(params, "status")) {
                    if(entry[1].status?.id == RDStore.SUBSCRIPTION_EXPIRED.id) {
                        display = (entry[1].hasPerpetualAccess || result.subscriptionMap.get(key).size() < 5)
                    }
                    else display = entry[1].status?.id == RDStore.SUBSCRIPTION_CURRENT.id
                }
                else if(RDStore.SUBSCRIPTION_EXPIRED.id in Params.getLongList(params, "status")) {
                    display = (entry[1].hasPerpetualAccess || result.subscriptionMap.get(key).size() < 5)
                }
                else {
                    display = entry[1].status?.id == params.long("status")
                }
                if (display) {

                    if (idsCategory1.contains(entry[1].id)) {
                        result.subscriptionMap.get(key).add(entry[1])
                    }
                    else if (idsCategory2.contains(entry[1].id) && entry[1].instanceOf == null) {
                        result.subscriptionMap.get(key).add(entry[1])
                    }
                }
                Map<String, Object> definiteRec = [:], wekbRec = remote.records.find { Map remoteRec -> remoteRec.uuid == entry[0] }
                if(wekbRec)
                    definiteRec.putAll(wekbRec)
                else if(!params.containsKey('curatoryGroup') && !params.containsKey('curatoryGroupType') && !params.containsKey('automaticUpdates')) {
                    definiteRec.put('uuid', entry[0])
                }
                if(definiteRec.size() > 0)
                    tmp << definiteRec
            }
            result.packageListTotal = tmp.size()
            //there are records among them which are already purged ...
            result.records = tmp.drop(result.offset).take(result.max)
        }

        result
    }

    /**
     * Opens the dashboard for the user; showing important information regarding the context institution.
     * The information is grouped in tabs where information is being preloaded (except changes, notifications and surveys)
     * @return the dashboard view with the prefilled tabs
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def dashboard() {
        Map<String, Object> ctrlResult = myInstitutionControllerService.dashboard(this, params)

        // todo
        if (contextService.getOrg().isCustomerType_Support()) {
            render view: 'dashboard_support', model: ctrlResult.result
        }
        else {
            if (ctrlResult.status == MyInstitutionControllerService.STATUS_ERROR) {
                flash.error = "You do not have permission to access. Please request access on the profile page"
                response.sendError(401)
                return
            }
            /*
            if (ctrlResult.result.completedProcesses.size() > 0) {
                flash.message = ctrlResult.result.completedProcesses.join('<br>')
            }
            */

            return ctrlResult.result
        }
    }

    /**
     * Opens the modal to create a new task
     * @return the task creation modal
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def modal_create() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (! (result.user as User).isFormal(contextService.getOrg())) {
            flash.error = "You do not have permission to access. Please request access on the profile page";
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return;
        }

        Map<String, Object> preCon = taskService.getPreconditions()
        result << preCon

        render template: '/templates/tasks/modal_create', model: result
    }

    /**
     * Call for the finance import starting page; the mappings are being explained here and an example sheet for submitting data to import
     * @return the finance import entry view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def financeImport() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.mappingCols = ["subscription","package","issueEntitlement","budgetCode","referenceCodes","orderNumber","invoiceNumber","status",
                              "element","elementSign","currency","invoiceTotal","exchangeRate","value","taxType","taxRate","invoiceDate","financialYear","title","description","datePaid","dateFrom","dateTo"/*,"institution"*/]
        result
    }

    /**
     * Generates a customised work sheet for import cost items for the given subscriptions
     * @return a CSV template with subscription OIDs and empty data
     * @see Subscription
     * @see CostItem
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def generateFinanceImportWorksheet() {
        Subscription subscription = Subscription.get(params.id)
        Set<String> keys = ["subscription", "subscriber.sortname", "subscriber.name", "package","issueEntitlement","budgetCode","referenceCodes","orderNumber","invoiceNumber","status",
                            "element","elementSign","currency","invoiceTotal","exchangeRate","value","taxType","taxRate","invoiceDate","financialYear","title","description","datePaid","dateFrom","dateTo"]
        Set<List<String>> subscriptionRows = []
        Set<String> colHeaders = []
        colHeaders.addAll(keys.collect { String entry -> message(code:"myinst.financeImport.${entry}") })
        Subscription.executeQuery('select sub from OrgRole oo join oo.sub sub join oo.org org where oo.roleType in (:roleType) and sub.instanceOf = :parent order by org.sortname', [roleType: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_CONS], parent: subscription]).each { Subscription subChild ->
            List<String> row = []
            keys.eachWithIndex { String entry, int i ->
                if(entry == "subscription") {
                    row[i] = subChild.globalUID
                }else if(entry == "subscriber.sortname") {
                    row[i] = subChild.getSubscriberRespConsortia().sortname
                }else if(entry == "subscriber.name") {
                    row[i] = subChild.getSubscriberRespConsortia().name
                }
                else row[i] = ""
            }
            subscriptionRows << row
        }
        String template = exportService.generateSeparatorTableString(colHeaders,subscriptionRows,"\t")
        response.setHeader("Content-disposition", "attachment; filename=\"${escapeService.escapeString(subscription.name)}_finances.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        out.withWriter { writer ->
            writer.write(template)
        }
        out.close()
    }

    /**
     * Reads off data from the uploaded import sheet and prepares data for import. The user may check after the
     * processing whether the imported data is read correctly or not
     * @return the control view with the import preparation result
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def processFinanceImport() {
        CostItem.withTransaction { TransactionStatus ts ->
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
            MultipartFile tsvFile = request.getFile("tsvFile") //this makes the withTransaction closure necessary
            if(tsvFile && tsvFile.size > 0) {
                String encoding = UniversalDetector.detectCharset(tsvFile.getInputStream())
                if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                    result.filename = tsvFile.originalFilename
                    Map<String,Map> financialData = financeService.financeImport(tsvFile, encoding)
                    result.headerRow = financialData.headerRow
                    result.candidates = financialData.candidates
                    result.budgetCodes = financialData.budgetCodes
                    if(financialData.errorRows) {
                        //background of this procedure: the editor adding titles via KBART wishes to receive a "counter-KBART" which will then be sent to the provider for verification
                        String dir = GlobalService.obtainTmpFileLocation()
                        File f = new File(dir+"/${result.filename}_errors")
                        if(!f.exists()) {
                            List headerRow = financialData.errorRows.remove(0)
                            String returnFile = exportService.generateSeparatorTableString(headerRow, financialData.errorRows, '\t')
                            FileOutputStream fos = new FileOutputStream(f)
                            fos.withWriter { Writer w ->
                                w.write(returnFile)
                            }
                            fos.flush()
                            fos.close()
                        }
                        result.errorCount = financialData.errorRows.size()
                        result.errMess = 'myinst.financeImport.post.error.matchingErrors'
                        result.token = "${result.filename}_errors"
                    }
                    /*result.criticalErrors = ['ownerMismatchError','noValidSubscription','multipleSubError','packageWithoutSubscription','noValidPackage','multipleSubPkgError','noCurrencyError','invalidCurrencyError',
                                             'packageNotInSubscription','entitlementWithoutPackageOrSubscription','noValidTitle','multipleTitleError','noValidEntitlement','multipleEntitlementError',
                                             'entitlementNotInSubscriptionPackage','multipleOrderError','multipleInvoiceError','invalidCurrencyError','invoiceTotalInvalid','valueInvalid','exchangeRateInvalid',
                                             'invalidTaxType','invalidYearFormat','noValidStatus','noValidElement','noValidSign']*/
                    render view: 'postProcessingFinanceImport', model: result
                }
                else {
                    flash.error = message(code:'default.import.error.wrongCharset',args:[encoding]) as String
                    redirect(url: request.getHeader('referer'))
                }
            }
            else {
                flash.error = message(code:'default.import.error.noFileProvided') as String
                redirect(url: request.getHeader('referer'))
            }
        }
    }

    /**
     * Call for the subscription import starting page; the mappings are being explained here and an example sheet for submitting data to import
     * @return the subscription import entry view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def subscriptionImport() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        List<String> mappingCols = ["name", "owner", "status", "type", "form", "resource", "provider", "vendor", "startDate", "endDate",
                              "manualCancellationDate", "referenceYear", "hasPerpetualAccess", "hasPublishComponent", "isPublicForApi",
                              "customProperties", "privateProperties", "identifiers", "notes"]
        if(contextService.getOrg().isCustomerType_Inst_Pro()) {
            mappingCols.add(mappingCols.indexOf("manualCancellationDate"), "isAutomaticRenewAnnually")
        }
        result.mappingCols = mappingCols
        result
    }

    /**
     * Reads off data from the uploaded import sheet and prepares data for import. The user may check after the
     * processing whether the imported data is read correctly or not
     * @return the control view with the import preparation result
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def processSubscriptionImport() {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
            MultipartFile tsvFile = request.getFile("tsvFile") //this makes the transaction closure necessary
            if(tsvFile && tsvFile.size > 0) {
                String encoding = UniversalDetector.detectCharset(tsvFile.getInputStream())
                if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                    result.filename = tsvFile.originalFilename
                    Map subscriptionData = subscriptionService.subscriptionImport(tsvFile, encoding)
                    if(subscriptionData.globalErrors) {
                        flash.error = "<h3>${message([code:'myinst.subscriptionImport.post.globalErrors.header'])}</h3><p>${subscriptionData.globalErrors.join('</p><p>')}</p>"
                        redirect(action: 'subscriptionImport')
                        return
                    }
                    result.candidates = subscriptionData.candidates
                    result.parentSubType = subscriptionData.parentSubType
                    result.criticalErrors = ['multipleOrgsError','noValidOrg','noValidSubscription']
                    render view: 'postProcessingSubscriptionImport', model: result
                }
                else {
                    flash.error = message(code:'default.import.error.wrongCharset',args:[encoding]) as String
                    redirect(url: request.getHeader('referer'))
                }
            }
            else {
                flash.error = message(code:'default.import.error.noFileProvided') as String
                redirect(url: request.getHeader('referer'))
            }
    }

    /**
     * Opens a list of current surveys concerning the context institution. The list may be filtered by filter parameters.
     * Default filter setting is current year
     * @return a (filtered) list of surveys, either displayed as html or returned as Excel worksheet
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_INST_BASIC )
    })
    def currentSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate) desc", [org: contextService.getOrg()]) ?: []

        //SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.tab = params.tab ?: 'open'

        //if(params.tab != 'new'){
            params.sort = params.sort ?: 'surInfo.endDate DESC, LOWER(surInfo.name)'
        //}

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears.push(newYear)
            }
            //params.validOnYear = [newYear]
        }

        result.propList = PropertyDefinition.findAll( "select sur.type from SurveyResult as sur where sur.participant = :contextOrg order by sur.type.name_de asc", [contextOrg: contextService.getOrg()]).groupBy {it}.collect {it.key}

        result.allConsortia = Org.executeQuery(
                """select o from Org o, SurveyInfo surInfo where surInfo.owner = o
                        group by o order by lower(o.name) """
        )

        Set providerIds = providerService.getCurrentProviderIds( contextService.getOrg() )

        result.providers = providerIds.isEmpty() ? [] : Provider.findAllByIdInList(providerIds).sort { it?.name }

        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIBER_CONS, 'activeInst': contextService.getOrg()])

        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()

        FilterService.Result fsr = filterService.getParticipantSurveyQuery_New(params, sdFormat, contextService.getOrg())
        if (fsr.isFilterSet) { params.filterSet = true }

        result.surveyResults = SurveyResult.executeQuery(fsr.query, fsr.queryParams, params)

        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            List surveyConfigsforExport = result.surveyResults.collect {it[1]}
            if ( params.surveyCostItems ) {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "surveyCostItems.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems(surveyConfigsforExport, contextService.getOrg())
            }else {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.plural")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys(surveyConfigsforExport, contextService.getOrg())
            }
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else {
            result.surveyResults = result.surveyResults.groupBy {it.id[1]}
            result.countSurveys = surveyService._getSurveyParticipantCounts_New(contextService.getOrg(), params)

            withFormat {
                html {

                    result
                }
            }
        }
    }

    /**
     * Call to retrieve detailed information for a given survey; this view is callable for consortium members and single users.
     * The view may be rendered as html or as Excel worksheet to download
     * @return the details view of the given survey
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_INST_BASIC )
    })
    def surveyInfos() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.long('surveyConfigID')) : result.surveyInfo.surveyConfigs[0]
        result.surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(contextService.getOrg(), result.surveyConfig)

        if(!result.surveyOrg){
            response.sendError(401); return
        }

        result.ownerId = result.surveyInfo.owner?.id

        params.viewTab = params.viewTab ?: 'overview'

        result = surveyService.participantResultGenerics(result, contextService.getOrg(), params)

        if ( params.exportXLSX ) {
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.label")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportSurveys([result.surveyConfig], contextService.getOrg())
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else {
            withFormat {
                html {
                    if(result.error){
                        flash.error = result.error
                    }
                    result
                }
            }
        }

    }

    /**
     * This call processes the submission of the survey participant that the survey has been completed; checks are being
     * performed whether further subscription has been checked (if mandatory) or if all values have been completed.
     * If those checks were successful, the survey is marked as finished so that no further editing is possible unless
     * the consortium reopens the survey for the member upon his request. In case of pick and choose title surveys, the
     * selected issue entitlements marked as under negotiation will be marked as definitive so that the issue entitlements
     * pass definitively into the holding of the next year's subscription
     * @return void, returns to the survey details page ({@link #surveyInfos()})
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )
    })
    def surveyInfoFinish() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyInfo surveyInfo = SurveyInfo.get(params.id)
        SurveyConfig surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : surveyInfo.surveyConfigs[0]
        boolean sendSurveyFinishMail = false

        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(contextService.getOrg(), surveyConfig)

        List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(contextService.getOrg(), surveyConfig)
        result.minimalInput = false

        if(surveyConfig.type != SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
            surveyResults.each { SurveyResult surre ->
                if (surre.getValue() != null)
                    result.minimalInput = true
            }
        }else {
            result.minimalInput = true
        }

        if(result.minimalInput) {
            boolean allResultHaveValue = true
            /*
            see ERMS-5815 - it should not be necessary to fill out answers if(f) no participation is intended - after check with Melanie, this behavior should be generalised
             */
            boolean noParticipation = SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)?.refValue == RDStore.YN_NO
            List<PropertyDefinition> notProcessedMandatoryProperties = []
            if(!noParticipation) {
                surveyResults.each { SurveyResult surre ->
                    SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surre.type)
                    if (surveyConfigProperties.mandatoryProperty && !surre.isResultProcessed() && !surveyOrg.existsMultiYearTerm()) {
                        allResultHaveValue = false
                        notProcessedMandatoryProperties << surre.type.getI10n('name')
                    }
                }
                if((SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)?.refValue == RDStore.YN_YES || surveyConfig.surveyInfo.isMandatory) && surveyConfig.invoicingInformation && (!surveyOrg.address || (SurveyPersonResult.countByParticipantAndSurveyConfigAndBillingPerson(contextService.getOrg(), surveyConfig, true) == 0))){
                    allResultHaveValue = false
                    flash.error = g.message(code: 'surveyResult.finish.invoicingInformation')
                }else if(SurveyPersonResult.countByParticipantAndSurveyConfigAndSurveyPerson(contextService.getOrg(), surveyConfig, true) == 0){
                    allResultHaveValue = false
                    flash.error = g.message(code: 'surveyResult.finish.surveyContact')
                }
                else if(surveyConfig.surveyInfo.isMandatory && surveyConfig.vendorSurvey) {
                    if(SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING)) {
                        boolean vendorInvoicing = SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING)?.refValue == RDStore.INVOICE_PROCESSING_VENDOR
                        int vendorCount = SurveyVendorResult.executeQuery('select count (*) from SurveyVendorResult spr ' +
                                'where spr.surveyConfig = :surveyConfig and spr.participant = :participant', [surveyConfig: surveyConfig, participant: contextService.getOrg()])[0]
                        if (vendorInvoicing && vendorCount == 0) {
                            allResultHaveValue = false
                            flash.error = g.message(code: 'surveyResult.finish.vendorSurvey')
                        } else if (!vendorInvoicing && vendorCount > 0) {
                            allResultHaveValue = false
                            flash.error = g.message(code: 'surveyResult.finish.vendorSurvey.wrongVendor')
                        }
                    }
                }

            }


            /*
            if (surveyInfo.isMandatory) {
                if (surveyConfig && surveyConfig.subSurveyUseForTransfer) {
                    noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
                }
            }
            */

            if (!noParticipation && notProcessedMandatoryProperties.size() > 0) {
                flash.error = message(code: "confirm.dialog.concludeBinding.survey.notProcessedMandatoryProperties", args: [notProcessedMandatoryProperties.join(', ')]) as String
            } else if ((noParticipation && !surveyConfig.invoicingInformation) || allResultHaveValue) {
                surveyOrg.finishDate = new Date()
                if (!surveyOrg.save()) {
                    flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess') as String
                } else {
                    flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess') as String
                    sendSurveyFinishMail = true
                }
            } else if (!noParticipation && allResultHaveValue) {
                surveyOrg.finishDate = new Date()
                if (!surveyOrg.save()) {
                    flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess') as String
                } else {
                    flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess') as String
                    sendSurveyFinishMail = true
                }
            }

            if(surveyConfig.subSurveyUseForTransfer && noParticipation){
                SurveyResult surveyResult = SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)
                surveyResult.comment = params.surveyResultComment
                surveyResult.save()
            }

            if (sendSurveyFinishMail) {
                boolean sendMailToSurveyOwner = true

                if (!surveyInfo.isMandatory && OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT_ONLY_BY_MANDATORY) != OrgSetting.SETTING_NOT_FOUND && OrgSetting.get(surveyInfo.owner, OrgSetting.KEYS.MAIL_SURVEY_FINISH_RESULT_ONLY_BY_MANDATORY).rdValue == RDStore.YN_YES) {
                    int countAllResultsIsRefNo = 0
                    int countAllResultsIsRef = 0
                    surveyResults.each { SurveyResult surre ->
                        if (surre.type.isRefdataValueType()) {
                            countAllResultsIsRef++
                            if (surre.refValue == RDStore.YN_NO || surre.refValue == null) {
                                countAllResultsIsRefNo++
                            }
                        }
                    }

                    if (countAllResultsIsRefNo == countAllResultsIsRef) {
                        sendMailToSurveyOwner = false
                    }

                }

                if (sendMailToSurveyOwner) {
                    mailSendService.emailToSurveyOwnerbyParticipationFinish(surveyInfo, contextService.getOrg())
                }
                mailSendService.emailToSurveyParticipationByFinish(surveyInfo, contextService.getOrg())
            }
        }else {
            flash.error = g.message(code: 'surveyResult.finish.inputNecessary')
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to participate on a survey linked to the given survey. The survey link is being resolved and the
     * institution called this link will join as participant on the linked survey. Other members of the institution
     * concerned will be notified as well
     * @return redirects to the information of the related survey
     * @see SurveyLinks
     * @see SurveyInfo
     * @see SurveyConfig
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )
    })
    def surveyLinkOpenNewSurvey() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyLinks surveyLink = SurveyLinks.get(params.surveyLink)
        SurveyInfo surveyInfo = surveyLink.targetSurvey
        SurveyConfig surveyConfig = surveyInfo.surveyConfigs[0]
        Org org = contextService.getOrg()

        result.editable = (surveyInfo && surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

        if(contextService.getOrg().id == surveyInfo.owner.id) {
            org = params.participant ? Org.get(params.participant) : null
        }

        if (org && surveyLink && result.editable) {

            SurveyOrg.withTransaction { TransactionStatus ts ->
                    boolean existsMultiYearTerm = false
                    Subscription sub = surveyConfig.subscription
                    if (sub && !surveyConfig.pickAndChoose && surveyConfig.subSurveyUseForTransfer) {
                        Subscription subChild = sub.getDerivedSubscriptionForNonHiddenSubscriber(org)

                        if (subChild && subChild.isCurrentMultiYearSubscriptionNew()) {
                            existsMultiYearTerm = true
                        }

                    }

                    if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org)) && !existsMultiYearTerm) {
                        SurveyOrg surveyOrg = new SurveyOrg(
                                surveyConfig: surveyConfig,
                                org: org,
                                orgInsertedItself: true
                        )

                        if (!surveyOrg.save()) {
                            log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}")
                            flash.error = message(code: 'surveyLinks.participateToSurvey.fail')
                        } else {
                            if(surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]){
                                surveyConfig.surveyProperties.each { SurveyConfigProperties property ->
                                    if (!SurveyResult.findWhere(owner: surveyInfo.owner, participant: org, type: property.surveyProperty, surveyConfig: surveyConfig)) {
                                        SurveyResult surveyResult = new SurveyResult(
                                                owner: surveyInfo.owner,
                                                participant: org ?: null,
                                                startDate: surveyInfo.startDate,
                                                endDate: surveyInfo.endDate ?: null,
                                                type: property.surveyProperty,
                                                surveyConfig: surveyConfig
                                        )

                                        if (surveyResult.save()) {
                                            //log.debug(surveyResult.toString())
                                        } else {
                                            log.error("Not create surveyResult: " + surveyResult)
                                            flash.error = message(code: 'surveyLinks.participateToSurvey.fail')
                                        }
                                    }
                                }

                                surveyService.emailsToSurveyUsersOfOrg(surveyInfo, org, false)
                                //flash.message = message(code: 'surveyLinks.participateToSurvey.success')


                                surveyService.setDefaultPreferredConcatsForSurvey(surveyConfig, org)

                            }
                        }
                    }
                surveyConfig.save()
                }

            if(contextService.getOrg().id == surveyInfo.owner.id){
                redirect(controller: 'survey', action: 'evaluationParticipant', id: surveyInfo.id, params: [participant: org.id])
            } else{
                redirect(action: 'surveyInfos', id: surveyInfo.id)
            }

        }else {
            redirect(url: request.getHeader('referer'))
        }

    }

    /**
     * Lists the users of the context institution
     * @return a list of users affiliated to the context institution
     * @see User
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def users() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        GrailsParameterMap filterParams = params.clone() as GrailsParameterMap
        filterParams.max = result.max
        filterParams.offset = result.offset
        filterParams.org = contextService.getOrg().id

        Map userData = userService.getUserMap(filterParams)
        result.total = userData.count
        result.users = userData.data
        result.titleMessage = "${contextService.getOrg()}"
        result.inContextOrg = true
        result.institution = contextService.getOrg()
        result.orgInstance = contextService.getOrg()
        result.multipleAffiliationsWarning = true

        result.navConfig = [
                orgInstance: contextService.getOrg(), inContextOrg: result.inContextOrg
        ]
        result.filterConfig = [
                filterableRoles:Role.findAllByRoleType('user'), orgField: false
        ]
        result.tmplConfig = [
                editable: result.editable,
                editLink: 'editUser',
                deleteLink: 'deleteUser',
                users: result.users,
                showAllAffiliations: false
        ]

        render view: '/user/global/list', model: result
    }

    /**
     * Call to delete a given user
     * @return the user deletion page where the details of the given user are being enumerated
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def deleteUser() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)

        if (! result.editable) {
            redirect controller: 'myInstitution', action: 'users'
            return
        }

        if (result.user) {
            if (result.user.formalOrg && (! (result.user as User).isFormal(contextService.getOrg()))) {
                flash.error = message(code: 'user.delete.error.foreignOrg') as String
                redirect action: 'editUser', params: [uoid: params.uoid]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = User.get(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user as User, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user as User, null, DeletionService.DRY_RUN)
            }

            result.substituteList = User.executeQuery(
                    'select distinct u from User u where u.formalOrg = :ctxOrg and u != :self and u.formalRole = :instAdm order by u.username',
                    [ctxOrg: result.orgInstance, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
            )
        }

        render view: '/user/global/delete', model: result
    }

    /**
     * Call to edit the given user
     * @return the user details view for editing the profile
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def editUser() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)

        if (! result.user || ! result.editable) {
            redirect controller: 'myInstitution', action: 'users'
            return
        }

        result.availableComboDeptOrgs = [ result.orgInstance ]
        result.manipulateAffiliations = true

        result.orgLabel = message(code:'default.institution') as String

        render view: '/user/global/edit', model: result
    }

    /**
     * Call to create a new user for the context institution
     * @return the form to enter the new user's parameters
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def createUser() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.orgInstance = contextService.getOrg()
        result.inContextOrg = true
        result.availableOrgs = [ result.orgInstance ]

        render view: '/user/global/create', model: result
    }

    /**
     * Processes the submitted parameters and creates a new user for the context institution
     * @return a redirect to the profile edit page on success, back to the user creation page otherwise
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def processCreateUser() {
        def success = userService.addNewUser(params,flash)
        //despite IntelliJ's warnings, success may be an array other than the boolean true
        if(success instanceof User) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), success.id]) as String
            redirect action: 'editUser', params: [uoid: genericOIDService.getOID(success)]
            return
        }
        else if(success instanceof List) {
            flash.error = success.join('<br>')
            redirect action: 'createUser'
            return
        }
    }

    /**
     * Attaches a given user to the given institution
     * @return the user editing view
     */
    @DebugInfo(isInstAdm = [])
    @Secured(closure = {
        ctx.contextService.isInstAdm()
    })
    def setAffiliation() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect action: 'editUser', params: [uoid: params.uoid]
            return
        }

        userService.setAffiliation(result.user as User, params.org, params.formalRole, flash)
        redirect action: 'editUser', params: [uoid: params.uoid]
    }

    /**
     * Opens the internal address book for the context institution
     * @return a list view of the institution-internal contacts
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def addressbook() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        params.sort = params.sort ?: 'sortname'
        params.tab = params.tab ?: 'contacts'
        EhcacheWrapper cache = contextService.getUserCache("/myInstitution/addressbook/")
        switch(params.tab) {
            case 'contacts':
                result.personOffset = result.offset
                result.addressOffset = cache.get('addressOffset') ?: 0
                break
            case 'addresses':
                result.addressOffset = result.offset
                result.personOffset = cache.get('personOffset') ?: 0
                break
        }
        cache.put('personOffset', result.personOffset)
        cache.put('addressOffset', result.addressOffset)
        List visiblePersons = [], visibleAddresses = []
        Map<String, Object> selectedFields = [:], configMap = params.clone()
        String filename = escapeService.escapeString("${message(code: 'menu.institutions.myAddressbook')}_${DateUtils.getSDF_yyyyMMdd().format(new Date())}")
        if(params.fileformat) {
            if (params.filename) {
                filename = params.filename
            }
            configMap = [function:[], position: [], type: [], sort: 'sortname']
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('ief:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('ief:', ''), it.value ) }
            selectedFields.each { String key, value ->
                List<String> field = key.split('\\.')
                if(field.size() > 1) {
                    if(field[0] == 'function') {
                        configMap.function << field[1]
                    }
                    else if(field[0] == 'position') {
                        configMap.position << field[1]
                    }
                    else if(field[0] == 'type') {
                        configMap.type << field[1]
                    }
                }
            }
            visiblePersons.addAll(addressbookService.getVisiblePersons("addressbook", configMap))
            visibleAddresses.addAll(addressbookService.getVisibleAddresses("addressbook", configMap))
            selectedFieldsRaw.clear()
            selectedFields.clear()
            selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }
        else {
            visiblePersons.addAll(addressbookService.getVisiblePersons("addressbook", configMap+[offset: result.personOffset]))
            visibleAddresses.addAll(addressbookService.getVisibleAddresses("addressbook", configMap+[offset: result.addressOffset]))
        }

        Set<String> filterFields = ['org', 'prs', 'filterPropDef', 'filterProp', 'function', 'position', 'showOnlyContactPersonForInstitution', 'showOnlyContactPersonForProvider', 'showOnlyContactPersonForVendor']
        result.filterSet = params.keySet().any { String selField -> selField in filterFields }

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: contextService.getOrg() // private properties
                )

        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.personOffset).take(result.max)
        result.num_visibleAddresses = visibleAddresses.size()
        result.addresses = visibleAddresses.drop(result.addressOffset).take(result.max)

        /*
        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }
        */
        Map<String, String> emailAddresses = [:]
        visiblePersons.each { Person p ->
            Contact mail = Contact.findByPrsAndContentType(p, RDStore.CCT_EMAIL)
            if(mail) {
                String oid
                if(p.roleLinks.org[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.org[0])
                }
                else if(p.roleLinks.provider[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.provider[0])
                }
                else if(p.roleLinks.vendor[0]) {
                    oid = genericOIDService.getOID(p.roleLinks.vendor[0])
                }
                if(oid) {
                    Set<String> mails = emailAddresses.get(oid)
                    if(!mails)
                        mails = []
                    mails << mail.content
                    emailAddresses.put(oid, mails)
                }
            }
        }
        result.emailAddresses = emailAddresses

        if(params.fileformat) {
            switch(params.fileformat) {
                case 'xlsx': SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportAddresses(visiblePersons, visibleAddresses, selectedFields, params.exportOnlyContactPersonForInstitution == 'true', params.exportOnlyContactPersonForProvider == 'true', params.exportOnlyContactPersonForVendor == 'true', null, ExportClickMeService.FORMAT.XLS)
                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { Writer writer ->
                        writer.write((String) exportClickMeService.exportAddresses(visiblePersons, visibleAddresses, selectedFields, params.exportOnlyContactPersonForInstitution == 'true', params.exportOnlyContactPersonForProvider == 'true', params.exportOnlyContactPersonForVendor == 'true', params.tab, ExportClickMeService.FORMAT.CSV))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportAddresses(visiblePersons, visibleAddresses, selectedFields, params.exportOnlyContactPersonForInstitution == 'true', params.exportOnlyContactPersonForProvider == 'true', params.exportOnlyContactPersonForVendor == 'true', null, ExportClickMeService.FORMAT.PDF)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else result
    }

    /**
     * Call for the current budget code overview of the institution
     * @return a list of budget codes the context institution currently holds
     * @see BudgetCode
     * @see CostItemGroup
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    Map<String, Object> budgetCodes() {
        BudgetCode.withTransaction {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

            if (result.editable) {

                flash.message = null
                flash.error = null

                if (params.cmd == "newBudgetCode") {
                    if (params.bc) {
                        BudgetCode bc = new BudgetCode(
                                owner: contextService.getOrg(),
                                value: params.bc,
                                descr: params.descr
                        )
                        if (bc.save()) {
                            flash.message = "Neuer Budgetcode wurde angelegt."
                        } else {
                            flash.error = "Der neue Budgetcode konnte nicht angelegt werden."
                        }
                    }
                }
                else if (params.cmd == "deleteBudgetCode") {
                    BudgetCode bc = (BudgetCode) genericOIDService.resolveOID(params.bc)
                    if (bc && bc.owner.id == contextService.getOrg().id) {
                        BudgetCode.executeUpdate('delete from BudgetCode bc where bc.id = :bcid', [bcid: bc.id])
                    }
                }

            }
            Set<BudgetCode> allBudgetCodes = BudgetCode.findAllByOwner(contextService.getOrg(), [sort: 'value'])
            Map<BudgetCode, Integer> costItemGroups = [:]
            if (allBudgetCodes) {
                BudgetCode.executeQuery('select new map(cig.budgetCode as budgetCode, count(cig.costItem) as usageCount) from CostItemGroup cig where cig.budgetCode in (:allBudgetCodes) group by cig.budgetCode', [allBudgetCodes: allBudgetCodes]).each { Map entry ->
                    costItemGroups.put(entry.budgetCode, entry.usageCount)
                }
            }
            result.budgetCodes = allBudgetCodes
            result.costItemGroups = costItemGroups

            if (params.redirect) {
                redirect(url: request.getHeader('referer'), params: params)
            }

            result
        }
    }

    /**
     * Call for the overview of tasks for the given user in the context institution
     * @return a table view of tasks
     * @see Task
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    def tasks() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if ( ! params.sort) {
            params.sort = "t.endDate"
            params.order = "asc"
        }
        if ( ! params.ctrlFilterSend) {
            params.taskStatus = RDStore.TASK_STATUS_OPEN.id
        }
        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        FilterService.Result fsr = filterService.getTaskQuery(params, sdFormat)

        SwissKnife.setPaginationParams(result, params, result.user as User)

        List<Task> taskInstanceList     = taskService.getTasksByResponsibility(result.user as User, [query: fsr.query, queryParams: fsr.queryParams])
        List<Task> myTaskInstanceList   = taskService.getTasksByCreator(result.user as User, [query: fsr.query, queryParams: fsr.queryParams])

        result.taskCount    = taskInstanceList.size()
        result.myTaskCount  = myTaskInstanceList.size()

        result.cmbTaskInstanceList = (taskInstanceList + myTaskInstanceList).unique()

        Map<String, Object> preCon = taskService.getPreconditions()
        result << preCon

        result
    }

    /**
     * After consideration, this workflow actually makes no sense as a consortium is no administrative unit but a flexible
     * one, determined by subscriptions. Is thus a legacy construct, based on a misunderstanding of concept.
     * Call for listing institutions eligible to be attached to or detached from the context consortium
     * @return a list of institutions
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def addMembers() {
        Combo.withTransaction {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

            // new: filter preset
            result.comboType = 'Consortium'
            params.customerType = customerTypeService.getOrgInstRoles().id

            if (params.selectedOrgs) {
                if (formService.validateToken(params)) {
                    log.debug('adding orgs to consortia/institution')

                    params.list('selectedOrgs').each { soId ->
                        Map<String, Object> map = [
                                toOrg  : contextService.getOrg(),
                                fromOrg: Org.findById(Long.parseLong(soId)),
                                type   : RefdataValue.getByValueAndCategory(result.comboType, RDConstants.COMBO_TYPE)
                        ]
                        if (! Combo.findWhere(map)) {
                            Combo cmb = new Combo(map)
                            cmb.save()
                        }
                    }

                }
                redirect action: 'manageMembers'
                return
            }
            result.filterSet = params.filterSet ? true : false

            FilterService.Result fsr = filterService.getOrgQuery(params)
            List<Org> availableOrgs = Org.executeQuery(fsr.query, fsr.queryParams, params)
            Set<Org> currentMembers = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :current and c.type = :comboType', [current: contextService.getOrg(), comboType: RefdataValue.getByValueAndCategory(result.comboType, RDConstants.COMBO_TYPE)])
            result.availableOrgs = availableOrgs - currentMembers

            result
        }
    }

    /**
     * Call to load the current watchlist of objects coming from we:kb
     * @return a {@link Map} containing the {@link Org}s, {@link de.laser.wekb.Package}s and {@link Platform}s currently being observed
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentMarkers() {
//        log.debug 'currentMarkers()'

        Marker.TYPE markerType = Marker.TYPE.WEKB_CHANGES
        if (params.filterMarkerType) {
            markerType = Marker.TYPE.get(params.filterMarkerType)
        }

        // TODO -- permissions --

        Map<String, Object> result = [
                myMarkedObjects: [
                        org:        markerService.getMyObjectsByClassAndType(Org.class, markerType),
                        provider:   markerService.getMyObjectsByClassAndType(Provider.class, markerType),
                        vendor:     markerService.getMyObjectsByClassAndType(Vendor.class, markerType),
                        platform:   markerService.getMyObjectsByClassAndType(Platform.class, markerType),
                        package:    markerService.getMyObjectsByClassAndType(Package.class, markerType),
                        tipp:       markerService.getMyObjectsByClassAndType(TitleInstancePackagePlatform.class, markerType)
                ],
                myXMap:         markerService.getMyCurrentXMap(), // TODO
                markerType:     markerType
        ]
        result
    }

    /**
     * Call to open the workflows currently under process. A filter is being used to fetch the workflows; this may either be called or set
     * @return the (filtered) list of workflows currently under process at the context institution
     * @see WfChecklist
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    def currentWorkflows() {

        SessionCacheWrapper cache = contextService.getSessionCache()
        String urlKey = 'myInstitution/currentWorkflows'
        String fmKey  = urlKey + '/_filterMap'

        Map<String, Object> result = [
                offset: params.offset ? params.int('offset') : 0,
                max:    params.max    ? params.int('max') : contextService.getUser().getPageSizeOrDefault()
        ]

        // filter

        Map<String, Object> filter = params.findAll{ it.key.startsWith('filter') } as Map

        if (filter.get('filter') == 'reset') {
            cache.remove(fmKey)
        }
        else if (filter.get('filter') == 'true') {
            filter.remove('filter') // remove control flag
            cache.put(fmKey, filter)   // store filter settings
        }
        else if (params.size() == 2 && params.containsKey('controller') && params.containsKey('action')) { // first visit
            cache.remove(fmKey)
        }

        if (cache.get(fmKey)) {
            result.putAll(cache.get(fmKey) as Map) // restore filter settings
        }

        if (params.cmd) {
            result.putAll(workflowService.executeCmd(params))
            params.clear()
        }

        String idQuery = 'select wf.id from WfChecklist wf where wf.owner = :ctxOrg'
        Map<String, Object> queryParams = [ctxOrg: contextService.getOrg()]

        if (result.filterTargetType) {
            Long filterTargetType = Long.valueOf(result.filterTargetType)

            if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_VENDOR.id) {
                idQuery = idQuery + ' and wf.vendor is not null'
            }
            if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION.id) {
                idQuery = idQuery + ' and wf.org is not null and exists (select os from OrgSetting os where os.org = wf.org and os.key = :ct and os.roleValue in (:roles))'
                queryParams.put('ct', OrgSetting.KEYS.CUSTOMER_TYPE)
                queryParams.put('roles', customerTypeService.getOrgInstRoles())
            }
            else if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE.id) {
                idQuery = idQuery + ' and wf.license is not null'
            }
            else if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_OWNER.id) {
                idQuery = idQuery + ' and wf.org = :ctxOrg'
            }
            else if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER.id) {
                idQuery = idQuery + ' and wf.provider is not null'
            }
            if (filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION.id) {
                idQuery = idQuery + ' and wf.subscription is not null'
            }
        }

        if (result.filterTemplates) {
            if (result.filterTemplates == 'yes') {
                idQuery = idQuery + ' and wf.template = true'
            }
            else if (result.filterTemplates == 'no') {
                idQuery = idQuery + ' and wf.template = false'
            }
        }

        // result

        List<Long> checklistIds = WfChecklist.executeQuery(idQuery + ' order by wf.id desc', queryParams)

        result.openWorkflows = []
        result.doneWorkflows = []

        String resultQuery = 'select wf from WfChecklist wf where wf.id in (:idList)'

        WfChecklist.executeQuery(resultQuery, [idList: checklistIds]).each{ clist ->
            Map info = clist.getInfo()

            if (info.status == RDStore.WF_WORKFLOW_STATUS_OPEN) {
                result.openWorkflows << clist
            }
            else if (info.status == RDStore.WF_WORKFLOW_STATUS_DONE) {
                result.doneWorkflows << clist
            }
        }
        result.currentWorkflows = WfChecklist.executeQuery(resultQuery, [idList: checklistIds])

        if (result.filterStatus) {
            Long filterStatus = Long.valueOf(result.filterStatus)

            if (filterStatus == RDStore.WF_WORKFLOW_STATUS_OPEN.id) {
                result.currentWorkflows = result.openWorkflows
            }
            else if (filterStatus == RDStore.WF_WORKFLOW_STATUS_DONE.id) {
                result.currentWorkflows = result.doneWorkflows
            }
        }

        result.total = result.currentWorkflows.size()
        result.currentWorkflows = workflowService.sortByLastUpdated(result.currentWorkflows).drop(result.offset).take(result.max) // pagination

        result
    }

    /**
     * Call for the table view of those consortia which are linked to the context institution
     * @return a list of those institutions on whose consortial subscriptions the context institution is participating
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def currentConsortia() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Profiler prf = new Profiler()
        prf.setBenchmark('start')

        // new: filter preset
        result.comboType = RDStore.COMBO_TYPE_CONSORTIUM
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id
        GrailsParameterMap queryParams = params.clone() as GrailsParameterMap

        result.filterSet    = params.filterSet ? true : false

        queryParams.comboType = result.comboType.value
        queryParams.invertDirection = true
        FilterService.Result fsr = filterService.getOrgComboQuery(queryParams, contextService.getOrg())
        if (fsr.isFilterSet) { queryParams.filterSet = true }

		prf.setBenchmark('query')

        List totalConsortia      = Org.executeQuery(fsr.query, fsr.queryParams)
        result.totalConsortia    = totalConsortia
        result.consortiaCount    = totalConsortia.size()
        result.consortia         = totalConsortia.drop((int) result.offset).take((int) result.max)

        String exportHeader = message(code: 'export.my.consortia')
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        // Write the output to a file
        String file = "${sdf.format(new Date())}_"+exportHeader

		List bm = prf.stopBenchmark()
		result.benchMark = bm
        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []
        if(params.fileformat) {
            if (params.filename) {
                file = params.filename
            }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalConsortia, selectedFields, 'consortium', ExportClickMeService.FORMAT.XLS, contactSwitch)

                    response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                case 'csv':
                    response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportOrgs(totalConsortia, selectedFields, 'consortium', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportOrgs(totalConsortia, selectedFields, 'consortium', ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ file +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            result
        }
    }

    /**
     * Call to list all member institutions which are linked by combo to the given context consortium.
     * The result may be filtered by organisational and subscription parameters
     * @return the list of consortial member institutions
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def manageMembers() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params), configMap = params.clone()

        Profiler prf = new Profiler()
        prf.setBenchmark('start')

        // new: filter preset
        result.comboType = RDStore.COMBO_TYPE_CONSORTIUM
        if (params.selectedOrgs && !params.containsKey("orgListToggler")) {
            log.debug('remove orgs from consortia')

            Combo.withTransaction { TransactionStatus ts ->
                params.list('selectedOrgs').each { soId ->
                    Combo cmb = Combo.findWhere(
                            toOrg: contextService.getOrg(),
                            fromOrg: Org.get(Long.parseLong(soId)),
                            type: RDStore.COMBO_TYPE_CONSORTIUM
                    )
                    cmb.delete()
                }
            }
        }
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        if(!params.subStatus) {
            if(!params.filterSet) {
                params.subStatus = [RDStore.SUBSCRIPTION_CURRENT.id, RDStore.GENERIC_NULL_VALUE.id]
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false
        /*if(!params.subPerpetual) {
            if(!params.filterSet) {
                params.subPerpetual = "on"
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false*/

        params.comboType = result.comboType.value
        FilterService.Result fsr = filterService.getOrgComboQuery(params, contextService.getOrg())
        if (fsr.isFilterSet) { params.filterSet = true }

        String tmpQuery = "select o.id " + fsr.query.minus("select o ")
        List memberIds = Org.executeQuery(tmpQuery, fsr.queryParams)

        Map queryParamsProviders = [
                subOrg      : contextService.getOrg(),
                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIUM]
        ]

        Map queryParamsSubs = [
                subOrg      : contextService.getOrg(),
                subRoleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIUM]
        ]

        String queryProviders = '''select distinct(pvr.provider) from OrgRole or_pa, ProviderRole pvr 
join or_pa.sub sub 
join sub.orgRelations or_sub where
    ( sub = or_sub.sub and or_sub.org = :subOrg ) and
    ( or_sub.roleType in (:subRoleTypes) ) and
        ( pvr.subscription = sub )'''

        String querySubs = '''select distinct(or_pa.sub) from OrgRole or_pa 
join or_pa.sub sub 
join sub.orgRelations or_sub where
    ( sub = or_sub.sub and or_sub.org = :subOrg ) and
    ( or_sub.roleType in (:subRoleTypes) ) and
        ( or_pa.roleType in (:paRoleTypes) ) and sub.instanceOf is null'''

        if (params.subStatus) {
            queryProviders +=  " and (sub.status in (:subStatus))"
            querySubs +=  " and (sub.status in (:subStatus))"
            List<RefdataValue> subStatus = Params.getRefdataList(params, 'subStatus')
            queryParamsProviders << [subStatus: subStatus]
            queryParamsSubs << [subStatus: subStatus]
        }
        if (params.subValidOn || params.subPerpetual) {
            if (params.subValidOn && !params.subPerpetual) {
                queryProviders += " and (sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null)"
                querySubs += " and (sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null)"
                queryParamsProviders << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                queryParamsSubs << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
            }
            else if (params.subValidOn && params.subPerpetual) {
                queryProviders += " and ((sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null or sub.hasPerpetualAccess = true))"
                querySubs += " and ((sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null or sub.hasPerpetualAccess = true))"
                queryParamsProviders << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                queryParamsSubs << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
            }
        }

        List<Provider> providers = Provider.executeQuery(queryProviders, queryParamsProviders)
        result.providers = providers

		prf.setBenchmark('query')

        if (params.filterPropDef && memberIds) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname asc", 'o', [oids: memberIds])
            fsr.query = efq.query
            fsr.queryParams = efq.queryParams as Map<String, Object>
        }

        List totalMembers = Org.executeQuery(fsr.query, fsr.queryParams)
        if(params.orgListToggler) {
            Combo.withTransaction {
                Combo.executeUpdate('delete from Combo c where c.toOrg = :context and c.fromOrg.id in (:ids)', [context: contextService.getOrg(), ids: memberIds])
            }
        }
        result.totalMembers    = totalMembers
        result.membersCount    = totalMembers.size()
        result.members         = totalMembers.drop((int) result.offset).take((int) result.max)
        String header = message(code: 'menu.my.insts')
        String exportHeader = message(code: 'export.my.insts')

        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        // Write the output to a file
        String file = "${sdf.format(new Date())}_"+exportHeader

		List bm = prf.stopBenchmark()
		result.benchMark = bm
        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []
        if(params.fileformat) {
            if (params.filename) {
                file = params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
        }

        /*if ( params.exportXLS ) {
            SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(totalMembers, header, true, 'xls')
            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        else */
        switch(params.fileformat) {
            case 'xlsx': SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalMembers, selectedFields, 'member', ExportClickMeService.FORMAT.XLS, contactSwitch, configMap)
                response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return //IntelliJ cannot know that the return prevents an obsolete redirect
            case 'csv':
                response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportOrgs(totalMembers, selectedFields, 'member', ExportClickMeService.FORMAT.CSV, contactSwitch, configMap))
                }
                out.close()
                return
            case 'pdf':
                Map<String, Object> pdfOutput = exportClickMeService.exportOrgs(totalMembers, selectedFields, 'member', ExportClickMeService.FORMAT.PDF, contactSwitch, configMap)

                byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                response.setHeader('Content-disposition', 'attachment; filename="'+ file +'.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
                return
        }
        result
    }

    /**
     * Call to list consortial member institutions along with their subscriptions and costs.
     * The list may be displayed as HTML or rendered as file, either as Excel worksheet or comma separated file
     * @return a list enumerating cost item entries with the subscription and member institution attached to the cost items
     * @see CostItem
     * @see Subscription
     * @see Org
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def manageConsortiaSubscriptions() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params), selectedFields = [:]
        result.tableConfig = ['withCostItems']
        if (! contextService.getOrg().isCustomerType_Support()) {
            result.tableConfig << "showPackages"
            result.tableConfig << "showProviders"
            result.tableConfig << "showVendors"
            result.tableConfig << "showInfoFlyout"
        }

        result.putAll(subscriptionService.getMySubscriptionsForConsortia(params,result.user, contextService.getOrg(), result.tableConfig))
        Date datetoday = new Date()
        String filename = "${DateUtils.getSDF_yyyyMMdd().format(datetoday)}_" + g.message(code: "export.my.consortiaSubscriptions")
        Set<String> contactSwitch = []
        if(params.fileformat) {
            if (params.filename) {
                filename = params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
        }

        switch(params.fileformat) {
            case 'xlsx':
                //result.entries has already been filtered in service method
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportConsortiaParticipations(result.entries, selectedFields, contactSwitch, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            case 'pdf':
                Map<String, Object> pdfOutput = exportClickMeService.exportConsortiaParticipations(result.entries, selectedFields, contactSwitch, ExportClickMeService.FORMAT.PDF)

                byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
                return
            case 'csv':
                //result.entries has already been filtered in service method
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportConsortiaParticipations(result.entries, selectedFields, contactSwitch, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
                return
        }
        result
        /*
        Profiler prf = result.pu
        prf.setBenchmark("after subscription loading, before providers")
        //LinkedHashMap<Subscription,List<Org>> providers = [:]
        Map<Org,Set<String>> mailAddresses = [:]
        BidiMap subLinks = new DualHashBidiMap()
        if(params.format || params.exportXLS) {
            List<Subscription> subscriptions = result.entries.collect { entry -> (Subscription) entry[1] } as List<Subscription>
            Links.executeQuery("select l from Links l where (l.sourceSubscription in (:targetSubscription) or l.destinationSubscription in (:targetSubscription)) and l.linkType = :linkType",[targetSubscription:subscriptions,linkType:RDStore.LINKTYPE_FOLLOWS]).each { Links link ->
                if(link.sourceSubscription && link.destinationSubscription) {
                    Set<Subscription> destinations = subLinks.get(link.sourceSubscription)
                    if(destinations)
                        destinations << link.destinationSubscription
                    subLinks.put(link.sourceSubscription, destinations)
                }
            }
            OrgRole.findAllByRoleTypeInList([RDStore.OR_PROVIDER,RDStore.OR_AGENCY]).each { it ->
                List<Org> orgs = providers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                providers.put(it.sub,orgs)
            }
            List persons = Person.executeQuery("select c.content,c.prs from Contact c where c.prs in (select p from Person as p inner join p.roleLinks pr where " +
                    "( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) ) and pr.functionType = :roleType) and c.contentType = :email",
                    [ctx: contextService.getOrg(),
                     roleType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                     email: RDStore.CCT_EMAIL])
            persons.each {  personRow ->
                Person person = (Person) personRow[1]
                PersonRole pr = person.roleLinks.find{ PersonRole p -> p.org != contextService.getOrg()}
                if(pr) {
                    Org org = pr.org
                    Set<String> addresses = mailAddresses.get(org)
                    String mailAddress = (String) personRow[0]
                    if(!addresses) {
                        addresses = []
                    }
                    addresses << mailAddress
                    mailAddresses.put(org,addresses)
                }
            }
        }
        */
        /*
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        prf.setBenchmark("before xls")
        if(params.exportXLS) {
            XSSFWorkbook wb = new XSSFWorkbook()
            POIXMLProperties xmlProps = wb.getProperties()
            POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
            coreProps.setCreator(message(code:'laser'))
            XSSFCellStyle lineBreaks = wb.createCellStyle()
            lineBreaks.setWrapText(true)
            XSSFCellStyle csPositive = wb.createCellStyle()
            csPositive.setFillForegroundColor(new XSSFColor(new java.awt.Color(198,239,206)))
            csPositive.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            XSSFCellStyle csNegative = wb.createCellStyle()
            csNegative.setFillForegroundColor(new XSSFColor(new java.awt.Color(255,199,206)))
            csNegative.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            XSSFCellStyle csNeutral = wb.createCellStyle()
            csNeutral.setFillForegroundColor(new XSSFColor(new java.awt.Color(255,235,156)))
            csNeutral.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            SXSSFWorkbook workbook = new SXSSFWorkbook(wb,50)
            workbook.setCompressTempFiles(true)
            SXSSFSheet sheet = workbook.createSheet(message(code:'menu.my.consortiaSubscriptions'))
            sheet.flushRows(10)
            sheet.setAutobreaks(true)
            Row headerRow = sheet.createRow(0)
            headerRow.setHeightInPoints(16.75f)
            List titles = [message(code:'sidewide.number'),message(code:'myinst.consortiaSubscriptions.member'), message(code:'org.mainContact.label'),message(code:'default.subscription.label'),message(code:'globalUID.label'),
                           message(code:'license.label'), message(code:'myinst.consortiaSubscriptions.packages'),message(code:'myinst.consortiaSubscriptions.provider'),message(code:'myinst.consortiaSubscriptions.runningTimes'),
                           message(code: 'subscription.referenceYear.label'), message(code:'subscription.isPublicForApi.label'),message(code:'subscription.hasPerpetualAccess.label'),
                           message(code:'financials.amountFinal'),"${message(code:'financials.isVisibleForSubscriber')} / ${message(code:'financials.costItemConfiguration')}"]
            titles.eachWithIndex{ titleName, int i ->
                Cell cell = headerRow.createCell(i)
                cell.setCellValue(titleName)
            }
            sheet.createFreezePane(0,1)
            Row row
            Cell cell
            int rownum = 1
            int sumcell = 12
            int sumTitleCell = 11
            result.entries.eachWithIndex { entry, int sidewideNumber ->
                //log.debug("processing entry ${sidewideNumber} ...")
                CostItem ci = (CostItem) entry.cost ?: new CostItem()
                Subscription subCons = (Subscription) entry.sub
                Org subscr = (Org) entry.org
                int cellnum = 0
                row = sheet.createRow(rownum)
                //sidewide number
                //log.debug("insert sidewide number")
                cell = row.createCell(cellnum++)
                cell.setCellValue(rownum)
                //sortname
                //log.debug("insert sortname")
                cell = row.createCell(cellnum++)
                String subscrName = ""
                if(subscr.sortname) subscrName += subscr.sortname
                subscrName += "(${subscr.name})"
                cell.setCellValue(subscrName)
                //log.debug("insert general contacts")
                //general contacts
                Set<String> generalContacts = mailAddresses.get(subscr)
                cell = row.createCell(cellnum++)
                if(generalContacts)
                    cell.setCellValue(generalContacts.join('; '))
                //subscription name
                //log.debug("insert subscription name")
                cell = row.createCell(cellnum++)
                String subscriptionString = subCons.name
                //if(subCons._getCalculatedPrevious()) //avoid! Makes 5846 queries!!!!!
                if(subLinks.getKey(subCons.id))
                    subscriptionString += " (${message(code:'subscription.hasPreviousSubscription')})"
                cell.setCellValue(subscriptionString)
                //subscription globalUID
                //log.debug("insert subscription global UID")
                cell = row.createCell(cellnum++)
                cell.setCellValue(subCons.globalUID)
                //license name
                //log.debug("insert license name")
                cell = row.createCell(cellnum++)
                if(result.linkedLicenses.get(subCons)) {
                    List<String> references = result.linkedLicenses.get(subCons).collect { License l -> l.reference }
                    if(references)
                        cell.setCellValue(references.join("\n"))
                    else cell.setCellValue(" ")
                }
                //packages
                //log.debug("insert package name")
                cell = row.createCell(cellnum++)
                cell.setCellStyle(lineBreaks)
                List<String> packageNames = []
                subCons.packages.each { subPkg ->
                    packageNames << subPkg.pkg.name
                }
                if(packageNames)
                    cell.setCellValue(packageNames.join("\n"))
                else cell.setCellValue(" ")
                //provider
                //log.debug("insert provider name")
                cell = row.createCell(cellnum++)
                cell.setCellStyle(lineBreaks)
                List<String> providerNames = []
                subCons.orgRelations.findAll{ OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.each { OrgRole p ->
                    //log.debug("Getting provider ${p.org}")
                    providerNames << p.org.name
                }
                if(providerNames)
                    cell.setCellValue(providerNames.join("\n"))
                else cell.setCellValue(" ")
                //running time from / to
                //log.debug("insert running times")
                cell = row.createCell(cellnum++)
                String dateString = ""
                if(ci.id) {
                    if(ci.getDerivedStartDate()) dateString += sdf.format(ci.getDerivedStartDate())
                    if(ci.getDerivedEndDate()) dateString += " - ${sdf.format(ci.getDerivedEndDate())}"
                }
                cell.setCellValue(dateString)
                //reference year
                //log.debug("insert reference year")
                cell = row.createCell(cellnum++)
                String refYearString = " "
                if(subCons.referenceYear) {
                    refYearString = subCons.referenceYear.toString()
                }
                cell.setCellValue(refYearString)
                //is public for api
                //log.debug("insert api flag")
                cell = row.createCell(cellnum++)
                cell.setCellValue(ci.sub?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                //has perpetual access
                //log.debug("insert perpetual access flag")
                cell = row.createCell(cellnum++)
                cell.setCellValue(ci.sub?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                //final sum
                //log.debug("insert final sum")
                cell = row.createCell(cellnum++)
                if(ci.id && ci.costItemElementConfiguration) {
                    switch(ci.costItemElementConfiguration) {
                        case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
                            break
                        case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
                            break
                        case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
                            break
                    }
                    cell.setCellValue(formatNumber([number:ci.costInBillingCurrencyAfterTax ?: 0.0,type:'currency',currencySymbol:ci.billingCurrency ?: 'EUR']))
                }
                //cost item sign and visibility
                //log.debug("insert cost sign and visiblity")
                cell = row.createCell(cellnum++)
                String costSignAndVisibility = ""
                if(ci.id) {
                    if(ci.isVisibleForSubscriber) {
                        costSignAndVisibility += message(code:'financials.isVisibleForSubscriber')+" / "
                    }
                    if(ci.costItemElementConfiguration) {
                        costSignAndVisibility += ci.costItemElementConfiguration.getI10n("value")
                    }
                    else
                        costSignAndVisibility += message(code:'financials.costItemConfiguration.notSet')
                }
                cell.setCellValue(costSignAndVisibility)
                rownum++
            }
            rownum++
            sheet.createRow(rownum)
            rownum++
            Row sumRow = sheet.createRow(rownum)
            cell = sumRow.createCell(sumTitleCell)
            cell.setCellValue(message(code:'financials.export.sums'))
            rownum++
            result.finances.each { entry ->
                sumRow = sheet.createRow(rownum)
                cell = sumRow.createCell(sumTitleCell)
                cell.setCellValue("${message(code:'financials.sum.billing')} ${entry.key}")
                cell = sumRow.createCell(sumcell)
                cell.setCellValue(formatNumber([number:entry.value,type:'currency',currencySymbol: entry.key]))
                rownum++
            }
            for(int i = 0;i < titles.size();i++) {
                try {
                    sheet.trackColumnForAutoSizing(i)
                    sheet.autoSizeColumn(i)
                }
                catch (NullPointerException e) {
                    log.error("Null value in column ${i}")
                }
            }
            String filename = "${DateUtils.getSDF_noTimeNoPoint().format(new Date())}_${g.message(code:'export.my.consortiaSubscriptions')}.xlsx"
        }
        else if(params.exportPDF) {

        }
        else {
            result.benchMark = prf.stopBenchmark()
            withFormat {
                html {
                    result
                }
                csv {
                    List titles = [message(code: 'sidewide.number'), message(code: 'myinst.consortiaSubscriptions.member'), message(code: 'org.mainContact.label'), message(code: 'default.subscription.label'), message(code: 'globalUID.label'),
                                   message(code: 'license.label'), message(code: 'myinst.consortiaSubscriptions.packages'), message(code: 'myinst.consortiaSubscriptions.provider'), message(code: 'myinst.consortiaSubscriptions.runningTimes'),
                                   message(code: 'subscription.referenceYear.label'), message(code: 'subscription.isPublicForApi.label'), message(code: 'subscription.hasPerpetualAccess.label'),
                                   message(code: 'financials.amountFinal'), "${message(code: 'financials.isVisibleForSubscriber')} / ${message(code: 'financials.costItemConfiguration')}"]
                    List columnData = []
                    List row
                    result.entries.eachWithIndex { entry, int sidewideNumber ->
                        row = []
                        //log.debug("processing entry ${sidewideNumber} ...")
                        CostItem ci = (CostItem) entry[0] ?: new CostItem()
                        Subscription subCons = (Subscription) entry[1]
                        Org subscr = (Org) entry[2]
                        int cellnum = 0
                        //sidewide number
                        //log.debug("insert sidewide number")
                        cellnum++
                        row.add(sidewideNumber)
                        //sortname
                        //log.debug("insert sortname")
                        cellnum++
                        String subscrName = ""
                        if (subscr.sortname) subscrName += subscr.sortname
                        subscrName += "(${subscr.name})"
                        row.add(subscrName.replaceAll(',', ' '))
                        //log.debug("insert general contacts")
                        //general contacts
                        Set<String> generalContacts = mailAddresses.get(subscr)
                        if (generalContacts)
                            row.add(generalContacts.join('; '))
                        else row.add(' ')
                        //subscription name
                        //log.debug("insert subscription name")
                        cellnum++
                        String subscriptionString = subCons.name
                        //if(subCons._getCalculatedPrevious()) //avoid! Makes 5846 queries!!!!!
                        if (subLinks.getKey(subCons.id))
                            subscriptionString += " (${message(code: 'subscription.hasPreviousSubscription')})"
                        row.add(subscriptionString.replaceAll(',', ' '))
                        //subscription global uid
                        //log.debug("insert global uid")
                        cellnum++
                        row.add(subCons.globalUID)
                        //license name
                        //log.debug("insert license name")
                        cellnum++
                        if (result.linkedLicenses.get(subCons)) {
                            List<String> references = result.linkedLicenses.get(subCons).collect { License l -> l.reference.replace(',', ' ') }
                            row.add(references.join(' '))
                        } else row.add(' ')
                        //packages
                        //log.debug("insert package name")
                        cellnum++
                        String packagesString = " "
                        subCons.packages.each { subPkg ->
                            packagesString += "${subPkg.pkg.name} "
                        }
                        row.add(packagesString.replaceAll(',', ' '))
                        //provider
                        //log.debug("insert provider name")
                        cellnum++
                        List<String> providerNames = []
                        subCons.orgRelations.findAll{ OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.each { OrgRole p ->
                            //log.debug("Getting provider ${p.org}")
                            providerNames << p.org.name
                        }
                        row.add(providerNames.join( ' '))
                        //running time from / to
                        //log.debug("insert running times")
                        cellnum++
                        String dateString = " "
                        if (ci.id) {
                            if (ci.getDerivedStartDate()) dateString += sdf.format(ci.getDerivedStartDate())
                            if (ci.getDerivedEndDate()) dateString += " - ${sdf.format(ci.getDerivedEndDate())}"
                        }
                        row.add(dateString)
                        //reference year
                        //log.debug("insert reference year")
                        cellnum++
                        String refYearString = " "
                        if(subCons.referenceYear) {
                            refYearString = subCons.referenceYear.toString()
                        }
                        row.add(refYearString)
                        //is public for api
                        //log.debug("insert api flag")
                        cellnum++
                        row.add(ci.sub?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                        //has perpetual access
                        //log.debug("insert perpetual access flag")
                        cellnum++
                        row.add(ci.sub?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                        //final sum
                        //log.debug("insert final sum")
                        cellnum++
                        if (ci.id && ci.costItemElementConfiguration) {
                            row.add("${ci.costInBillingCurrencyAfterTax ?: 0.0} ${ci.billingCurrency ?: 'EUR'}")
                        } else row.add(" ")
                        //cost item sign and visibility
                        //log.debug("insert cost sign and visiblity")
                        cellnum++
                        String costSignAndVisibility = " "
                        if (ci.id) {
                            if (ci.isVisibleForSubscriber) {
                                costSignAndVisibility += message(code: 'financials.isVisibleForSubscriber') + " / "
                            }
                            if (ci.costItemElementConfiguration) {
                                costSignAndVisibility += ci.costItemElementConfiguration.getI10n("value")
                            } else
                                costSignAndVisibility += message(code: 'financials.costItemConfiguration.notSet')
                        }
                        row.add(costSignAndVisibility)
                        columnData.add(row)
                    }
                    columnData.add([])
                    columnData.add([])
                    row = []
                    //sumcell = 12
                    //sumTitleCell = 11
                    for (int h = 0; h < 11; h++) {
                        row.add(" ")
                    }
                    row.add(message(code: 'financials.export.sums'))
                    columnData.add(row)
                    columnData.add([])
                    result.finances.each { entry ->
                        row = []
                        for (int h = 0; h < 11; h++) {
                            row.add(" ")
                        }
                        row.add("${message(code: 'financials.sum.billing')} ${entry.key}")
                        row.add("${entry.value} ${entry.key}")
                        columnData.add(row)
                    }
                    String filename = "${DateUtils.getSDF_noTimeNoPoint().format(new Date())}_${g.message(code: 'export.my.consortiaSubscriptions')}.csv"
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                    response.contentType = "text/csv"
                    response.outputStream.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(titles, columnData, ','))
                    }
                    response.outputStream.flush()
                    response.outputStream.close()
                }
            }
        }
        */
    }

    /**
     * Call for the consortium to list the surveys a given institution is participating at.
     * The result may be displayed as HTML or exported as Excel worksheet
     * @return a list of surveys the context consortium set up and the given institution is participating at
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def manageParticipantSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Profiler prf = new Profiler()
        prf.setBenchmark('filterService')

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        DateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()

        result.participant = Org.get(params.long('id'))

        params.tab = params.tab ?: 'open'

        result.reminder = params.reminder

        if(params.tab != 'new'){
            params.sort = 'surInfo.endDate DESC, LOWER(surInfo.name)'
        }

        /*if (params.validOnYear == null || params.validOnYear == '') {
            def sdfyear = DateUtils.getSDF_yyyy()
            params.validOnYear = sdfyear.format(new Date())
        }*/

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate) desc", [org: result.participant]) ?: []

        params.consortiaOrg = contextService.getOrg()

        FilterService.Result fsr = filterService.getParticipantSurveyQuery_New(params, sdFormat, result.participant as Org)
        if (fsr.isFilterSet) { params.filterSet = true }

        result.surveyResults = SurveyResult.executeQuery(fsr.query, fsr.queryParams, params)

        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.plural")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) surveyService.exportSurveysOfParticipant(result.surveyResults.collect{it[1]}, result.participant)

            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else {
            result.surveyResults = result.surveyResults.groupBy {it.id[1]}
            result.surveyResultsCount =result.surveyResults.size()
            result.countSurveys = surveyService._getSurveyParticipantCounts_New(result.participant, params)

            result
        }
    }

    /**
     * Manages calls about the property groups the context institution defined. With a given parameter,
     * editing may be done on the given property group
     * @return in every case, the list of property groups; the list may be exported as Excel with the usage data as well, then, an Excel worksheet is being returned
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def managePropertyGroups() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        //result.editable = true // true, because action is protected (is it? I doubt; INST_USERs have at least reading rights to this page!)
        switch(params.cmd) {
            case 'new':
                result.formUrl = g.createLink([controller: 'myInstitution', action: 'managePropertyGroups'])
                result.createOrUpdate = message(code:'default.button.create.label')
                render template: '/templates/properties/propertyGroupModal', model: result
                return
            case 'edit':
                result.pdGroup = genericOIDService.resolveOID(params.oid)
                result.formUrl = g.createLink([controller: 'myInstitution', action: 'managePropertyGroups'])
                result.costInformationsInUse = params.containsKey('costInformationsInUse')
                result.createOrUpdate = message(code:'default.button.save.label')
                render template: '/templates/properties/propertyGroupModal', model: result
                return
            case ['moveUp', 'moveDown']:
                PropertyDefinitionGroup.withTransaction { TransactionStatus ts ->
                    PropertyDefinitionGroup pdg = (PropertyDefinitionGroup) genericOIDService.resolveOID(params.oid)
                    Set<PropertyDefinitionGroup> groupSet = PropertyDefinitionGroup.executeQuery('select pdg from PropertyDefinitionGroup pdg where pdg.ownerType = :objType and pdg.tenant = :tenant order by pdg.order', [objType: pdg.ownerType, tenant: contextService.getOrg()])
                    int idx = groupSet.findIndexOf { it.id == pdg.id }
                    int pos = pdg.order
                    PropertyDefinitionGroup pdg2

                    if (params.cmd == 'moveUp')        { pdg2 = groupSet.getAt(idx-1) }
                    else if (params.cmd == 'moveDown') { pdg2 = groupSet.getAt(idx+1) }

                    if (pdg2) {
                        pdg.order = pdg2.order
                        pdg.save()
                        pdg2.order = pos
                        pdg2.save()
                    }
                }
                break
            case 'delete':
                PropertyDefinitionGroup pdg = (PropertyDefinitionGroup) genericOIDService.resolveOID(params.oid)
                PropertyDefinitionGroup.withTransaction { TransactionStatus ts ->
                    try {
                        pdg.delete()
                        flash.message = message(code:'propertyDefinitionGroup.delete.success', args:[pdg.name]) as String
                    }
                    catch (e) {
                        flash.error = message(code:'propertyDefinitionGroup.delete.failure', args:[pdg.name]) as String
                    }
                }
                break
            case 'processing':
                if(formService.validateToken(params)) {
                    if(params.containsKey('costInformationsInUse')) {
                        CostInformationDefinitionGroup.withTransaction { TransactionStatus ts ->
                            CostInformationDefinitionGroup.executeUpdate(
                                    "DELETE CostInformationDefinitionGroup cifg WHERE cifg.tenant = :ctx",
                                    [ctx: contextService.getOrg()]
                            )

                            params.list('costInformationDefinition').each { cif ->
                                new CostInformationDefinitionGroup(
                                        costInformationDefinition: cif,
                                        tenant: contextService.getOrg()
                                ).save()
                            }
                        }
                    }
                    else {
                        boolean valid
                        PropertyDefinitionGroup propDefGroup
                        String ownerType = params.prop_descr ? PropertyDefinition.getDescrClass(params.prop_descr) : null

                        PropertyDefinitionGroup.withTransaction { TransactionStatus ts ->
                            if (params.oid) {
                                propDefGroup = (PropertyDefinitionGroup) genericOIDService.resolveOID(params.oid)
                                propDefGroup.name = params.name ?: propDefGroup.name
                                propDefGroup.description = params.description
                                propDefGroup.ownerType = ownerType

                                if (propDefGroup.save()) {
                                    valid = true
                                    flash.message = message(code: 'propertyDefinitionGroup.create.success', args: [propDefGroup.name]) as String
                                }
                                else {
                                    flash.error = message(code: 'propertyDefinitionGroup.create.error') as String
                                }
                            }
                            else {
                                if (params.name && ownerType) {
                                    int position = PropertyDefinitionGroup.executeQuery('select coalesce(max(pdg.order), 0) from PropertyDefinitionGroup pdg where pdg.ownerType = :objType and pdg.tenant = :tenant', [objType: ownerType, tenant: contextService.getOrg()])[0]
                                    propDefGroup = new PropertyDefinitionGroup(
                                            name: params.name,
                                            description: params.description,
                                            tenant: contextService.getOrg(),
                                            ownerType: ownerType,
                                            order: position + 1,
                                            isVisible: true
                                    )
                                    if (propDefGroup.save()) {
                                        valid = true
                                        flash.message = message(code: 'propertyDefinitionGroup.create.success', args: [propDefGroup.name]) as String
                                    }
                                    else {
                                        flash.error = message(code: 'propertyDefinitionGroup.create.error') as String
                                    }
                                } else {
                                    flash.error = message(code: 'propertyDefinitionGroup.create.missing') as String
                                }
                            }
                            if (valid) {
                                PropertyDefinitionGroupItem.executeUpdate(
                                        "DELETE PropertyDefinitionGroupItem pdgi WHERE pdgi.propDefGroup = :pdg",
                                        [pdg: propDefGroup]
                                )

                                params.list('propertyDefinition').each { pd ->
                                    new PropertyDefinitionGroupItem(
                                            propDef: pd,
                                            propDefGroup: propDefGroup
                                    ).save()
                                }
                            }
                        }
                    }
                }
                break
        }

        Set<PropertyDefinitionGroup> unorderedPdgs = PropertyDefinitionGroup.executeQuery('select pdg from PropertyDefinitionGroup pdg where pdg.tenant = :tenant order by pdg.order asc', [tenant: contextService.getOrg()])
        result.propDefGroups = [:]
        PropertyDefinition.AVAILABLE_GROUPS_DESCR.each { String propDefGroupType ->
            result.propDefGroups.put(propDefGroupType,unorderedPdgs.findAll { PropertyDefinitionGroup pdg -> pdg.ownerType == PropertyDefinition.getDescrClass(propDefGroupType)})
        }
        result.costInformationDefinitionsInUse = CostInformationDefinitionGroup.findAllByTenant(contextService.getOrg())

        if(params.cmd == 'exportXLS') {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyGroupUsageXLS(result.propDefGroups))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.propertyGroups')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else
            result
    }

    /**
     * Call to display the current usage for the given property in the system
     * @return a form view of the given property definition with their usage in the context institution's objects
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def manageProperties() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.objectsWithoutProp = []
        result.filteredObjs = []
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        SwissKnife.setPaginationParams(result, params, result.user)

        result.availableDescrs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR - PropertyDefinition.SVY_PROP

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Set<PropertyDefinition> propList = []
        if(params.descr) {
           propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr = :descr and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                    [ctx: contextService.getOrg(), descr: params.descr])
            result.propList = propList
        }

        EhcacheWrapper cache = contextService.getUserCache("/manageProperties")
        result.selectedWithout = cache.get('without') ?: []
        result.selectedWith = cache.get('with') ?: []
        result.selectedAudit = cache.get('audit') ?: []
        if(params.offset && params.setWithout == 'true')
            result.withoutPropOffset = Integer.parseInt(params.offset.toString())
        else if(params.withoutPropOffset)
            result.withoutPropOffset = params.withoutPropOffset as int
        else result.withoutPropOffset = 0
        if(params.offset && params.setWith == 'true')
            result.withPropOffset = Integer.parseInt(params.offset.toString())
        else if(params.withPropOffset)
            result.withPropOffset = params.withPropOffset as int
        else result.withPropOffset = 0
        PropertyDefinition propDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        //params.remove('filterPropDef')

        if(propDef) {
            result.putAll(propertyService.getAvailableProperties(propDef, contextService.getOrg(), params))
            result.countObjWithoutProp = result.withoutProp.size()
            result.countObjWithProp = result.withProp.size()
            result.withoutProp.eachWithIndex { obj, int i ->
                if(i >= result.withoutPropOffset && i < result.withoutPropOffset+result.max)
                    result.objectsWithoutProp << propertyService.processObjects(obj, propDef)
            }
            result.withProp.eachWithIndex { obj, int i ->
                if(i >= result.withPropOffset && i < result.withPropOffset+result.max)
                    result.filteredObjs << propertyService.processObjects(obj, propDef)
            }
            result.filterPropDef = propDef
        }

        //prepare next pagination
        params.withoutPropOffset = result.withoutPropOffset
        params.withPropOffset = result.withPropOffset

        result
    }

    /**
     * Call to process a bulk assign of a property definition to a given set of objects
     * @return the updated view with the assigned property definitions
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def processManageProperties() {
        PropertyDefinition.withTransaction {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
            log.debug(params.toMapString())

            PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.filterPropDef)
            List withAudit = params.list("withAudit")
            String propertyType = pd.tenant ? PropertyDefinition.PRIVATE_PROPERTY : PropertyDefinition.CUSTOM_PROPERTY

            if (params.newObjects) {
                params.list("newObjects").each { String id ->
                    def owner = _resolveOwner(pd, id)
                    if (owner) {
                        AbstractPropertyWithCalculatedLastUpdated prop = owner.propertySet.find { exProp -> exProp.type.id == pd.id && exProp.tenant.id == contextService.getOrg().id }
                        if (!prop || pd.multipleOccurrence) {
                            prop = PropertyDefinition.createGenericProperty(propertyType, owner, pd, contextService.getOrg())
                            if (propertyService.setPropValue(prop, params.filterPropValue)) {
                                if (id in withAudit) {
                                    owner.getClass().findAllByInstanceOf(owner).each { member ->
                                        AbstractPropertyWithCalculatedLastUpdated memberProp = PropertyDefinition.createGenericProperty(propertyType, member, prop.type, contextService.getOrg())
                                        memberProp = prop.copyInto(memberProp)
                                        memberProp.instanceOf = prop
                                        memberProp.save()
                                        AuditConfig.addConfig(prop, AuditConfig.COMPLETE_OBJECT)
                                    }
                                }
                            } else log.error(prop.errors.toString())
                        }
                    }
                }
            }
            if (params.selectedObjects) {
                if (params.deleteProperties) {
                    List selectedObjects = params.list("selectedObjects")
                    _processDeleteProperties(pd, selectedObjects)
                }
                else {
                    params.list("selectedObjects").each { String id ->
                        def owner = _resolveOwner(pd, id)
                        if (owner) {
                            AbstractPropertyWithCalculatedLastUpdated prop = owner.propertySet.find { exProp -> exProp.type.id == pd.id && exProp.tenant.id == contextService.getOrg().id }
                            if (prop) {
                                propertyService.setPropValue(prop, params.filterPropValue)
                            }
                        }
                    }
                }
            }
            redirect action: 'manageProperties', params: [descr: pd.descr, filterPropDef: params.filterPropDef]
            return
        }
    }

    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def manageRefdatas() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.rdCategories = RefdataCategory.executeQuery('from RefdataCategory order by desc_' + LocaleUtils.getCurrentLang())

        result
    }

    /**
     * Call to remove the given property definition from the given objects
     * @param propDef the property definition to remove
     * @param selectedObjects the objects from which the property should be unassigned
     */
    private def _processDeleteProperties(PropertyDefinition propDef, selectedObjects) {
        PropertyDefinition.withTransaction {
            int deletedProperties = 0
            selectedObjects.each { ownerId ->
                def owner = _resolveOwner(propDef, ownerId)
                Set<AbstractPropertyWithCalculatedLastUpdated> existingProps = owner.propertySet.findAll {
                    it.owner.id == owner.id && it.type.id == propDef.id && it.tenant?.id == contextService.getOrg().id && !AuditConfig.getConfig(it)
                }

                existingProps.each { AbstractPropertyWithCalculatedLastUpdated prop ->
                    owner.propertySet.remove(prop)
                    owner.save()
                    prop.delete()
                    deletedProperties++
                }
            }
        }
    }

    /**
     * Resolves for the given identifier the object matching to the object type of the given property definition
     * @param pd the property definition whose property's owner should be retrieved
     * @param id the identifier of the object to retrieve
     * @return the object matching the property definition's object type and the given identifier
     * @see PropertyDefinition#descr
     */
    private def _resolveOwner(PropertyDefinition pd, String id) {
        def owner
        switch(pd.descr) {
            case PropertyDefinition.PRS_PROP: owner = Person.get(id)
                break
            case PropertyDefinition.PRV_PROP: owner = Provider.get(id)
                break
            case PropertyDefinition.SUB_PROP: owner = Subscription.get(id)
                break
            case PropertyDefinition.ORG_PROP: owner = Org.get(id)
                break
            case PropertyDefinition.PLA_PROP: owner = Platform.get(id)
                break
            case PropertyDefinition.LIC_PROP: owner = License.get(id)
                break
            case PropertyDefinition.VEN_PROP: owner = Vendor.get(id)
                break
        }
        owner
    }

    /**
     * Displays and manages private property definitions for this institution.
     * If the add command is specified (i.e. params.cmd is set), this method inserts a new private property definition;
     * usage is restricted to the context institution.
     * To add a custom property definition (which is usable for every institution).
     * (but consider the annotation there!)
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def managePrivatePropertyDefinitions() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        switch(params.cmd) {
            case 'add': List rl
                if(params.pd_descr == CostInformationDefinition.COST_INFORMATION) {
                    rl = propertyService.addPrivateCostInformation(params)
                }
                else {
                    rl = propertyService.addPrivatePropertyDefinition(params)
                }
                flash."${rl[0]}" = rl[1]
                if(rl[2])
                    result.desc = rl[2]
                break
            case 'toggleMandatory':
                PropertyDefinition.withTransaction { TransactionStatus ts ->
                    PropertyDefinition pd = PropertyDefinition.get(params.long('pd'))
                    pd.mandatory = !pd.mandatory
                    pd.save()
                }
                break
            case 'toggleMultipleOccurrence':
                PropertyDefinition.withTransaction { TransactionStatus ts ->
                    PropertyDefinition pd = PropertyDefinition.get(params.long('pd'))
                    pd.multipleOccurrence = !pd.multipleOccurrence
                    pd.save()
                }
                break
            case 'replacePropertyDefinition':
                if(params.xcgPdTo) {
                    PropertyDefinition pdFrom = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdFrom)
                    PropertyDefinition pdTo = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdTo)
                    String oldName = pdFrom.tenant ? "${pdFrom.getI10n("name")} (priv.)" : pdFrom.getI10n("name")
                    String newName = pdTo.tenant ? "${pdTo.getI10n("name")} (priv.)" : pdTo.getI10n("name")
                    if (pdFrom && pdTo) {
                        try {
                            Map<String, Integer> counts = propertyService.replacePropertyDefinitions(pdFrom, pdTo, Boolean.valueOf(params.overwrite), false)
                            if(counts.success == 0 && counts.failures == 0) {
                                String instanceType
                                switch(pdFrom.descr) {
                                    case PropertyDefinition.LIC_PROP: instanceType = message(code: 'menu.institutions.replace_prop.licenses')
                                        break
                                    case PropertyDefinition.PRS_PROP: instanceType = message(code: 'menu.institutions.replace_prop.persons')
                                        break
                                    case PropertyDefinition.SUB_PROP: instanceType = message(code: 'menu.institutions.replace_prop.subscriptions')
                                        break
                                    case PropertyDefinition.SVY_PROP: instanceType = message(code: 'menu.institutions.replace_prop.surveys')
                                        break
                                    default: instanceType = message(code: 'menu.institutions.replace_prop.default')
                                        break
                                }
                                flash.message = message(code: 'menu.institutions.replace_prop.noChanges', args: [instanceType]) as String
                            }
                            else
                                flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [counts.success, counts.failures, oldName, newName]) as String
                        }
                        catch (Exception e) {
                            e.printStackTrace()
                            flash.error = message(code: 'menu.institutions.replace_prop.error', args: [oldName, newName]) as String
                        }
                    }
                }
                break
            case 'delete': flash.message = _deletePrivatePropertyDefinition(params)
                break
        }

        result.languageSuffix = LocaleUtils.getCurrentLang()

        Map<String, Set> propDefs = [:]
        Set<String> availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR
        if (contextService.getOrg().isCustomerType_Inst_Pro())
            availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR-PropertyDefinition.SVY_PROP
        availablePrivDescs.each { String it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, contextService.getOrg(), [sort: 'name_'+result.languageSuffix]) // ONLY private properties!
            propDefs[it] = itResult
        }
        Set<CostInformationDefinition> costInformationDefs = CostInformationDefinition.findAllByTenant(contextService.getOrg(), [sort: 'name_'+result.languageSuffix]) // ONLY private cost informations!
        propDefs.put(CostInformationDefinition.COST_INFORMATION, costInformationDefs)

        result.propertyDefinitions = propDefs

        def (usedPdList, attrMap, multiplePdList) = propertyService.getUsageDetails() // [List<Long>, Map<String, Object>, List<Long>]
        result.usedPdList = usedPdList
        result.attrMap = attrMap
        result.multiplePdList = multiplePdList
        //result.editable = true // true, because action is protected (it is not, cf. ERMS-2132! INST_USERs do have reading access to this page!)
        result.propertyType = 'private'
        if(params.cmd == 'exportXLS') {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyUsageExportXLS(propDefs))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.privateProperties')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else
            result
    }

    /**
     * Call to display the current general public property definitions of the system
     * @return a read-only list of public / general property definitions with the usages of objects owned by the context institution
     * @see AdminController#managePropertyDefinitions()
     */
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    Object managePropertyDefinitions() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.languageSuffix = LocaleUtils.getCurrentLang()

        Map<String,Set> propDefs = [:]
        PropertyDefinition.AVAILABLE_PUBLIC_DESCR.each { it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: 'name_'+result.languageSuffix]) // NO private properties!
            propDefs[it] = itResult
        }
        Set<CostInformationDefinition> costInformationDefs = CostInformationDefinition.findAllByTenantIsNull([sort: 'name_'+result.languageSuffix]) // NO private cost informations!
        propDefs.put(CostInformationDefinition.COST_INFORMATION, costInformationDefs)

        def (usedPdList, attrMap, multiplePdList) = propertyService.getUsageDetails() // [List<Long>, Map<String, Object>, List<Long>]
        result.propertyDefinitions = propDefs
        result.attrMap = attrMap
        result.usedPdList = usedPdList

        result.propertyType = 'custom'
        if(params.cmd == 'exportXLS') {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyUsageExportXLS(propDefs))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.customProperties')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else
            render view: 'managePropertyDefinitions', model: result
    }

    /**
     * Deletes the given private property definition for this institution
     * @param params the parameter map containing the property definition parameters
     * @return success or error messages
     */
    private _deletePrivatePropertyDefinition(GrailsParameterMap params) {
        PropertyDefinition.withTransaction {
            log.debug("delete private property definition for institution: " + params)

            String messages = ""
            Org tenant = contextService.getOrg()

            Params.getLongList(params, 'deleteIds').each { id ->
                PropertyDefinition privatePropDef = PropertyDefinition.findWhere(id: id, tenant: tenant)
                if (privatePropDef) {

                    try {
                        if (privatePropDef.mandatory) {
                            privatePropDef.mandatory = false
                            privatePropDef.save()

                            // delete inbetween created mandatories
                            Class.forName(privatePropDef.getImplClass())?.findAllByType(privatePropDef)?.each { prop ->
                                prop.delete()
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.toString())
                    }

                    String oldPropertyName = privatePropDef.getI10n('name')
                    privatePropDef.delete()
                    messages += message(code: 'default.deleted.message', args: [message(code: "propertyDefinition.${privatePropDef.descr}.create.label"), oldPropertyName])
                }
            }
            messages
        }
    }

    /**
     * Call to open the license copy view
     * @return the entry point view of the license copy process
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def copyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if(params.id)
        {
            License license = License.get(params.id)
            boolean isEditable = license.isEditableBy(result.user)

            if (! contextService.isInstEditor()) {
                flash.error = message(code:'license.permissionInfo.noPerms') as String
                response.sendError(HttpStatus.SC_FORBIDDEN)
                return;
            }

            if(isEditable){
                redirect controller: 'license', action: 'copyLicense', params: [sourceObjectId: genericOIDService.getOID(license), copyObject: true]
                return
            }else {
                flash.error = message(code:'license.permissionInfo.noPerms') as String
                response.sendError(HttpStatus.SC_FORBIDDEN)
                return;
            }
        }
    }

    /**
     * Call to open a list of current subscription transfers. This view creates an overview of the subscriptions being transferred into the next
     * phase of subscription; the output may either be rendered in the frontend or exported as a file (Excel, CSV)
     * @return the (filtered) list of subscription transfers currently running
     * @see SubscriptionService#getMySubscriptionTransfer(grails.web.servlet.mvc.GrailsParameterMap, de.laser.auth.User, de.laser.Org)
     */
    @DebugInfo(isInstUser = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def currentSubscriptionsTransfer() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.putAll(subscriptionService.getMySubscriptionTransfer(params, result.user, contextService.getOrg()))

        // Write the output to a file
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String filename = "${datetoday}_" + g.message(code: "export.my.currentSubscriptionsTransfer")

        Map<String, Object> selectedFields = [:]

        if(params.fileformat) {
            if (params.filename) {
                filename =params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }

        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, ExportClickMeService.FORMAT.XLS, true)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else if(params.fileformat == 'csv') {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
            response.contentType = "text/csv"
            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                writer.write((String) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, ExportClickMeService.FORMAT.CSV,  true))
            }
            out.close()
        }

        render view: customerTypeService.getCustomerTypeDependingView('currentSubscriptionsTransfer'), model: result
    }

    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    def exportConfigs() {
        Map<String, Object> ctrlResult = myInstitutionControllerService.exportConfigs(this, params)

        ctrlResult.result
    }

    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_PRO)
    })
    def exportConfigsActions() {
        Map<String, Object> ctrlResult = myInstitutionControllerService.exportConfigsActions(this, params)

        redirect(action: 'exportConfigs', params: [tab: ctrlResult.result.tab])
    }

    // -- Redirects to OrganisationController --

//    @Secured(['ROLE_USER'])
//    def readerNumber() {
//        redirect controller: 'organisation', action: 'readerNumber', id: contextService.getOrg().id
//    }
}
