package de.laser


import de.laser.annotations.DebugInfo
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.convenience.Marker
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.ctrl.UserControllerService
import de.laser.custom.CustomWkhtmltoxService
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
import de.laser.survey.SurveyResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.workflow.WfWorkflow
import de.laser.workflow.WfChecklist
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.collections.BidiMap
import org.apache.commons.collections.bidimap.DualHashBidiMap
import org.apache.http.HttpStatus
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.transaction.TransactionStatus
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletOutputStream
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

    AccessService accessService
    AddressbookService addressbookService
    ContextService contextService
    ComparisonService comparisonService
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
    OrganisationService organisationService
    OrgTypeService orgTypeService
    PendingChangeService pendingChangeService
    PropertyService propertyService
    ReportingGlobalService reportingGlobalService
    SubscriptionsQueryService subscriptionsQueryService
    SubscriptionService subscriptionService
    SurveyService surveyService
    TaskService taskService
    UserControllerService userControllerService
    UserService userService
    CustomWkhtmltoxService wkhtmltoxService
    WorkflowService workflowService
    WorkflowOldService workflowOldService
    MailSendService mailSendService

    /**
     * The landing page after login; this is also the call when the home button is clicked
     * @return the {@link #dashboard()} view
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def index() {
        redirect(action:'dashboard')
    }

    /**
     * Call for the reporting module
     * @return the reporting entry view
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    def reporting() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.cfgFilterList = BaseConfig.FILTER
        result.cfgChartsList = BaseConfig.CHARTS

        if (params.init) {
            result.filter = params.filter
            result.xhr = true
            render template: '/myInstitution/reporting/filter/form', model: result
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
                    template: '/myInstitution/reporting/query/filterResult',
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
     * @see Platform
     * @see Subscription
     */
    @Secured(['ROLE_USER'])
    def currentPlatforms() {
        Map<String, Object> result = [:]

        result.flagContentGokb = true // gokbService.doQuery
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        String instanceFilter = "", perpetualFilter = ""
        boolean withPerpetualAccess = params.hasPerpetualAccess == RDStore.YN_YES.id.toString()
        if(withPerpetualAccess)
            perpetualFilter = " or s2.hasPerpetualAccess = true "

        Map<String, Object> subscriptionParams = [contextOrg:result.contextOrg, roleTypes:[RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA], current:RDStore.SUBSCRIPTION_CURRENT]
        if(result.contextOrg.isCustomerType_Consortium())
            instanceFilter += " and s2.instanceOf = null "
        String subscriptionQuery = 'select s2 from OrgRole oo join oo.sub s2 where oo.org = :contextOrg and oo.roleType in (:roleTypes) and (s2.status = :current'+perpetualFilter+')'+instanceFilter

        result.subscriptionMap = [:]
        result.platformInstanceList = []

        //if (subscriptionQuery) {
            String qry3 = "select distinct p, s, ${params.sort ?: 'p.normname'} from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg " +
                    "join pkg.nominalPlatform p left join p.org o " +
                    "where s in (${subscriptionQuery}) and p.gokbId in (:wekbIds) and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"

            Map qryParams3 = [
                    pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED
            ]
            qryParams3.putAll(subscriptionParams)

            Map<String, Object> queryParams = [componentType: "Platform"]

            if (params.q?.length() > 0) {
                result.filterSet = true
                queryParams.q = params.q
                qry3 += "and ("
                qry3 += "   genfunc_filter_matcher(o.name, :query) = true"
                qry3 += "   or genfunc_filter_matcher(o.sortname, :query) = true"
                qry3 += ")"
                qryParams3.put('query', "${params.q}")
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
                    queryParams.ipAuthentication = rdv.value
                }
            }

            if(params.shibbolethSupport) {
                result.filterSet = true
                List<String> shibbolethSupport = params.list("shibbolethSupport")
                queryParams.shibbolethAuthentication = []
                shibbolethSupport.each { String shibboleth ->
                    RefdataValue rdv = RefdataValue.get(shibboleth)
                    String shibb = rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value
                    queryParams.shibbolethAuthentication = shibb
                }
            }

            if(params.counterCertified) {
                result.filterSet = true
                List<String> counterCertified = params.list("counterCertified")
                queryParams.counterCeritified = []
                counterCertified.each { String counter ->
                    RefdataValue rdv = RefdataValue.get(counter)
                    String cert = rdv == RDStore.GENERIC_NULL_VALUE ? "null" : rdv.value
                    queryParams.counterCertified << cert
                }
            }
            List wekbIds = gokbService.doQuery([max:10000, offset:0], params.clone(), queryParams).records.collect { Map hit -> hit.uuid }

            qryParams3.wekbIds = wekbIds

            qry3 += " group by p, s"
            if(params.sort)
                qry3 += " order by ${params.sort} ${params.order}"
            else qry3 += " order by p.normname asc"

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

                if (s.status.value == RDStore.SUBSCRIPTION_CURRENT.value || (withPerpetualAccess && s.hasPerpetualAccess)) {
                    result.subscriptionMap.get(key).add(s)
                }
            }
        //}

        if (params.isMyX) {
            List xFilter = params.list('isMyX')
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
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
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

        result.is_inst_admin = userService.hasFormalAffiliation(result.user, result.institution, 'INST_ADM')

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

        if (contextService.hasPerm(CustomerTypeService.ORG_INST_PRO)) {
            Set<RefdataValue> roleTypes = []
            if(params.licTypes) {
                Set<String> licTypes = params.list('licTypes')
                licTypes.each { String licTypeId ->
                    roleTypes << RefdataValue.get(licTypeId)
                }
            }
            else roleTypes.addAll([RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])
            base_qry = "from License as l where ( exists ( select o from l.orgRelations as o where ( ( o.roleType in (:roleTypes) ) AND o.org = :lic_org ) ) )"
            qry_params = [roleTypes: roleTypes, lic_org:result.institution]
            if(result.editable)
                licenseFilterTable << "action"
            licenseFilterTable << "licensingConsortium"
        }
        else if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
            base_qry = "from License as l where exists ( select o from l.orgRelations as o where ( o.roleType = :roleTypeC AND o.org = :lic_org AND l.instanceOf is null AND NOT exists ( select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL ) ) )"
            qry_params = [roleTypeC:RDStore.OR_LICENSING_CONSORTIUM, roleTypeL:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
            licenseFilterTable << "memberLicenses"
            if(result.editable)
                licenseFilterTable << "action"
        }
        else {
            base_qry = "from License as l where exists ( select o from l.orgRelations as o where  o.roleType = :roleType AND o.org = :lic_org ) "
            qry_params = [roleType:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
            licenseFilterTable << "licensingConsortium"
        }
        result.licenseFilterTable = licenseFilterTable

        if(params.consortium) {
            base_qry += " and ( exists ( select o from l.orgRelations as o where o.roleType = :licCons and o.org.id in (:cons) ) ) "
            List<Long> consortia = []
            List<String> selCons = params.list('consortium')
            selCons.each { String sel ->
                consortia << Long.parseLong(sel)
            }
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

        if(params.licensor) {
            base_qry += " and ( exists ( select o from l.orgRelations as o where o.roleType in (:licCons) and o.org.id in (:licensors) ) ) "
            List<Long> licensors = []
            List<String> selLicensors = params.list('licensor')
            selLicensors.each { String sel ->
                licensors << Long.parseLong(sel)
            }
            qry_params += [licCons:[RDStore.OR_LICENSOR, RDStore.OR_AGENCY],licensors:licensors]
        }

        if(params.categorisation) {
            base_qry += " and l.licenseCategory.id in (:categorisations) "
            List<Long> categorisations = []
            List<String> selCategories = params.list('categorisation')
            selCategories.each { String sel ->
                categorisations << Long.parseLong(sel)
            }
            qry_params.categorisations = categorisations
        }



        if(params.status || !params.filterSubmit) {
            base_qry += " and l.status.id = :status "
            if(!params.filterSubmit) {
                params.status = RDStore.LICENSE_CURRENT.id
                result.filterSet = true
            }
            qry_params.status = params.status as Long
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
                    " ) "
            qry_params.name_filter = params['keyword-search']
            qry_params.licRoleTypes = [RDStore.OR_LICENSOR, RDStore.OR_LICENSING_CONSORTIUM]
            qry_params.linkType = RDStore.LINKTYPE_LICENSE //map key will be overwritten if set twice
            result.keyWord = params['keyword-search']
        }

        if(params.subKind || params.subStatus || !params.filterSubmit) {
            Set<String> subscrQueryFilter = ["oo.org = :context"]
            qry_params.context = result.institution

            if(params.subStatus || !params.filterSubmit) {
                subscrQueryFilter <<  "s.status.id = :subStatus"
                if(!params.filterSubmit) {
                    params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id
                    result.filterSet = true
                }
                qry_params.subStatus = params.subStatus as Long
            }

            if(params.subKind) {
                subscrQueryFilter << "s.kind.id in (:subKinds)"
                List<Long> subKinds = []
                List<String> selKinds = params.list('subKind')
                selKinds.each { String sel ->
                    subKinds << Long.parseLong(sel)
                }
                qry_params.subKinds = subKinds
            }

            if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
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
            Set<Links> allLinkedSubscriptions = Subscription.executeQuery("select li from Links li join li.destinationSubscription s join s.orgRelations oo where li.sourceLicense in (:licenses) and li.linkType = :linkType and s.status.id = :status and oo.org = :context order by s.name", [licenses: result.licenses, linkType: RDStore.LINKTYPE_LICENSE, status: qry_params.subStatus,context:result.institution])
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
        List orgRoles = OrgRole.findAllByOrgAndLicIsNotNull(result.institution)
        result.orgRoles = [:]
        orgRoles.each { OrgRole oo ->
            result.orgRoles.put(oo.lic.id,oo.roleType)
        }
        prf.setBenchmark('get consortia')
        Set<Org> consortia = Org.executeQuery("select os.org from OrgSetting os where os.key = 'CUSTOMER_TYPE' and os.roleValue in (select r from Role r where authority = 'ORG_CONSORTIUM_BASIC') order by os.org.name asc")
        prf.setBenchmark('get licensors')
        Set<Org> licensors = orgTypeService.getOrgsForTypeLicensor()
        Map<String,Set<Org>> orgs = [consortia:consortia,licensors:licensors]
        result.orgs = orgs

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
        Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.LIC_PROP],result.institution)
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

        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportLicenses(totalLicenses, selectedFields, result.institution, ExportClickMeService.FORMAT.XLS)
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
                //writer.write((String) _exportcurrentSubscription(result.allSubscriptions,"csv", result.institution))
                writer.write((String) exportClickMeService.exportLicenses(totalLicenses, selectedFields, result.institution, ExportClickMeService.FORMAT.CSV))
            }
            out.close()
        }
        else if(params.fileformat == 'pdf') {
            Map<String, Object> pdfOutput = exportClickMeService.exportLicenses(totalLicenses, selectedFields, result.institution, ExportClickMeService.FORMAT.PDF)
            Map<String, Object> pageStruct = [orientation: 'Landscape', width: pdfOutput.titleRow.size()*15, height: 35]
            if (pageStruct.width > 85*4)       { pageStruct.pageSize = 'A0' }
            else if (pageStruct.width > 85*3)  { pageStruct.pageSize = 'A1' }
            else if (pageStruct.width > 85*2)  { pageStruct.pageSize = 'A2' }
            else if (pageStruct.width > 85)    { pageStruct.pageSize = 'A3' }
            pdfOutput.struct = [pageStruct.pageSize + ' ' + pageStruct.orientation]
            byte[] pdf = wkhtmltoxService.makePdf(
                    view: '/templates/export/_individuallyExportPdf',
                    model: pdfOutput,
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
        /*
        if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            List rows = []
            totalLicenses.each { License licObj ->
                License license = (License) licObj
                List row = [[field:license.reference.replaceAll(',',' '),style:'bold']]
                List linkedSubs = license.getSubscriptions(result.institution).collect { sub ->
                    sub.name
                }
                row.add([field:linkedSubs.join(", "),style:null])
                row.add([field:license.licensingConsortium ? license.licensingConsortium.name : '',style:null])
                row.add([field:license.licensor ? license.licensor.name : '',style:null])
                row.add([field:license.startDate ? sdf.format(license.startDate) : '',style:null])
                row.add([field:license.endDate ? sdf.format(license.endDate) : '',style:null])
                row.addAll(exportService.processPropertyListValues(propertyDefinitions, 'xls', license, licChildMap, objectNames, result.institution))
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
        result
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
                    List linkedSubs = license.getSubscriptions(result.institution).collect { sub ->
                        sub.name.replaceAll(',',' ')
                    }
                    row.add(linkedSubs.join("; "))
                    row.add(license.licensingConsortium)
                    row.add(license.licensor)
                    row.add(license.startDate ? sdf.format(license.startDate) : '')
                    row.add(license.endDate ? sdf.format(license.endDate) : '')
                    row.addAll(row.addAll(exportService.processPropertyListValues(propertyDefinitions, 'csv', license, licChildMap, objectNames, result.institution)))
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def emptyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (! (result.user as User).isFormal(result.institution as Org)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]) as String
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

        result.is_inst_admin = userService.hasFormalAffiliation(result.user, result.institution, 'INST_EDITOR')

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
    @DebugInfo(isInstUser_or_ROLEADMIN = true, wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def processEmptyLicense() {
        License.withTransaction { TransactionStatus ts ->
            User user = contextService.getUser()
            Org org = contextService.getOrg()

            Set<RefdataValue> defaultOrgRoleType = []
            if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC))
                defaultOrgRoleType << RDStore.OT_CONSORTIUM.id.toString()
            else defaultOrgRoleType << RDStore.OT_INSTITUTION.id.toString()

            params.asOrgType = params.asOrgType ? [params.asOrgType] : defaultOrgRoleType


            if (! userService.hasFormalAffiliation(user, org, 'INST_EDITOR')) {
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
                if (params.asOrgType && (RDStore.OT_CONSORTIUM.id.toString() in params.asOrgType)) {
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
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def currentProviders() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
		Profiler prf = new Profiler()
		prf.setBenchmark('init')

        EhcacheWrapper cache = contextService.getSharedOrgCache('MyInstitutionController/currentProviders')
        List<Long> orgIds = []

        if (cache.get('orgIds')) {
            orgIds = cache.get('orgIds')
            log.debug('orgIds from cache')
        }
        else {
            orgIds = (orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( result.institution )).toList()
            cache.put('orgIds', orgIds)
        }

        result.orgRoles    = [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.sort = params.sort ?: " LOWER(o.sortname), LOWER(o.name)"
        params.subPerpetual = 'on'

        GrailsParameterMap tmpParams = (GrailsParameterMap) params.clone()
        tmpParams.constraint_orgIds = orgIds
        def fsq  = filterService.getOrgQuery(tmpParams)

        result.filterSet = params.filterSet ? true : false
        if (params.filterPropDef) {
            fsq = propertyService.evalFilterQuery(tmpParams, fsq.query, 'o', fsq.queryParams)
        }
        List orgListTotal = Org.findAll(fsq.query, fsq.queryParams)
        result.wekbRecords = organisationService.getWekbOrgRecords(params, result)

        if (params.isMyX) {
            List xFilter = params.list('isMyX')
            Set<Long> f1Result = []

            if (xFilter.contains('wekb_exclusive')) {
                f1Result.addAll( orgListTotal.findAll {it.gokbId != null }.collect{ it.id } )
            }
            if (xFilter.contains('wekb_not')) {
                f1Result.addAll( orgListTotal.findAll { it.gokbId == null }.collect{ it.id }  )
            }
            orgListTotal = orgListTotal.findAll { f1Result.contains(it.id) }
        }

        result.orgListTotal = orgListTotal.size()
        result.orgList = orgListTotal.drop((int) result.offset).take((int) result.max)

        String message = message(code: 'export.my.currentProviders') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        result.cachedContent = true

		List bm = prf.stopBenchmark()
		result.benchMark = bm

        /*if ( params.exportXLS ) {
            try {
                SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(orgListTotal, message, true, "xls")
                // Write the output to a file

                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()

                return
            }
            catch (Exception e) {
                log.error("Problem",e);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                return
            }
        }
        else */
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
        }

        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(orgListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.XLS, contactSwitch)

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
                writer.write((String) exportClickMeService.exportOrgs(orgListTotal,selectedFields, 'provider',ExportClickMeService.FORMAT.CSV,contactSwitch))
            }
            out.close()
        }
        result
    }

    /**
     * Retrieves the list of subscriptions the context institution currently holds. The query may be restricted by filter parameters.
     * Default filter setting is status: current or with perpetual access
     * @return a (filtered) list of subscriptions, either as direct html output or as export stream (CSV, Excel)
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def currentSubscriptions() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

		Profiler prf = new Profiler()
		//prf.setBenchmark('init')
        result.tableConfig = ['showActions','showLicense']
        result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

        result.compare = params.compare ?: ''

        // Write the output to a file
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String filename = "${datetoday}_" + g.message(code: "export.my.currentSubscriptions")

		//List bm = prf.stopBenchmark()
		//result.benchMark = bm
        Map<String, Object> selectedFields = [:]

        if(params.fileformat) {
            if (params.filename) {
                filename =params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }

        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, result.institution, ExportClickMeService.FORMAT.XLS)
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
                //writer.write((String) _exportcurrentSubscription(result.allSubscriptions,"csv", result.institution))
                writer.write((String) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, result.institution, ExportClickMeService.FORMAT.CSV))
            }
            out.close()
        }
        /*
        if ( params.exportXLS ) {

            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) _exportcurrentSubscription(result.allSubscriptions, "xls", result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else*/
        result
    }

    /**
     * Prepares the given list of subscriptions for the given export stream
     * @param subscriptions the filtered list of subscriptions
     * @param format the format in which the export should be made
     * @param contextOrg the institution whose perspective should be taken during export
     * @return the list of subscriptions wrapped in the given export format (Excel worksheet or character-separated table)
     * @see Subscription
     * @see Org
     */
    private def _exportcurrentSubscription(List<Subscription> subscriptions, String format, Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        boolean asCons = contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)
        List titles = ['Name',
                       g.message(code: 'globalUID.label'),
                       g.message(code: 'license.label'),
                       g.message(code: 'subscription.packages.label'),
                       g.message(code: 'consortium.label'),
                       g.message(code: 'default.provider.label'),
                       g.message(code: 'default.agency.label'),
                       g.message(code: 'subscription.startDate.label'),
                       g.message(code: 'subscription.endDate.label'),
                       g.message(code: 'subscription.manualCancellationDate.label'),
                       g.message(code: 'subscription.referenceYear.export.label'),
                       g.message(code: 'subscription.isMultiYear.label')]
        if(!asCons) {
            titles.add(g.message(code: 'subscription.isAutomaticRenewAnnually.label'))
        }
        titles.addAll([g.message(code: 'default.identifiers.label'),
                       g.message(code: 'default.status.label'),
                       g.message(code: 'subscription.kind.label'),
                       g.message(code: 'subscription.form.label'),
                       g.message(code: 'subscription.resource.label'),
                       g.message(code: 'subscription.isPublicForApi.label'),
                       g.message(code: 'subscription.hasPerpetualAccess.label'),
                       g.message(code: 'subscription.hasPublishComponent.label')])
        if(asCons) {
            titles.addAll([g.message(code: 'subscription.memberCount.label'),g.message(code: 'subscription.memberCostItemsCount.label')])
        }
        //Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP],contextOrg)
        Set<PropertyDefinition> propertyDefinitions = PropertyDefinition.executeQuery("select sp.type from SubscriptionProperty sp where (sp.owner in (:subscriptions) or sp.owner.instanceOf in (:subscriptions)) and (sp.tenant = :ctx or sp.isPublic = true)",[subscriptions: subscriptions, ctx:contextOrg])
        titles.addAll(exportService.loadPropListHeaders(propertyDefinitions))
        Map<Subscription,Set> licenseReferences = [:], subChildMap = [:]
        Map<Long,Integer> costItemCounts = [:]
        //List allProviders = OrgRole.findAllByRoleTypeAndSubIsNotNull(RDStore.OR_PROVIDER)
        //List allAgencies = OrgRole.findAllByRoleTypeAndSubIsNotNull(RDStore.OR_AGENCY)
        //List allIdentifiers = Identifier.findAllBySubIsNotNull()
        List allLicenses = Links.executeQuery("select li from Links li where li.destinationSubscription in (:subscriptions) and li.linkType = :linkType",[subscriptions:subscriptions, linkType:RDStore.LINKTYPE_LICENSE])
        List allCostItems = CostItem.executeQuery('select count(ci.id),s.instanceOf.id from CostItem ci join ci.sub s where s.instanceOf != null and (ci.costItemStatus != :ciDeleted or ci.costItemStatus = null) and ci.owner = :owner group by s.instanceOf.id',[ciDeleted:RDStore.COST_ITEM_DELETED,owner:contextOrg])
        /*allProviders.each { OrgRole provider ->
            Set subProviders = providers.get(provider.sub)
            if(!providers.get(provider.sub))
                subProviders = new TreeSet()
            String providerName = provider.org.name ? provider.org.name : ' '
            subProviders.add(providerName)
            providers.put(provider.sub,subProviders)
        }
        allAgencies.each { OrgRole agency ->
            Set subAgencies = agencies.get(agency.sub)
            if(!agencies.get(agency.sub))
                subAgencies = new TreeSet()
            String agencyName = agency.org.name ? agency.org.name : ' '
            subAgencies.add(agencyName)
            agencies.put(agency.sub,subAgencies)
        }
        allIdentifiers.each { Identifier identifier ->
            Set subIdentifiers = identifiers.get(identifier.sub)
            if(!identifiers.get(identifier.sub))
                subIdentifiers = new TreeSet()
            subIdentifiers.add("(${identifier.ns.ns}) ${identifier.value}")
            identifiers.put(identifier.sub,subIdentifiers)
        }*/
        allCostItems.each { row ->
            costItemCounts.put((Long) row[1],(Integer) row[0])
        }
        allLicenses.each { Links row ->
            Subscription s = row.destinationSubscription
            License l = row.sourceLicense
            Set subLicenses = licenseReferences.get(s)
            if(!subLicenses)
                subLicenses = new TreeSet()
            subLicenses.add(l.reference)
            licenseReferences.put(s,subLicenses)
        }
        List membershipCounts = Subscription.executeQuery('select count(s.id),s.instanceOf.id from Subscription s where s.instanceOf in (:parentSubs) group by s.instanceOf.id',[parentSubs:subscriptions])
        Map<Long,Integer> subscriptionMembers = [:]
        membershipCounts.each { row ->
            subscriptionMembers.put((Long) row[1],(Integer) row[0])
        }
        List<Subscription> childSubsOfSet = subscriptions.isEmpty() ? [] : Subscription.executeQuery('select s from Subscription s where s.instanceOf in (:parentSubs) and exists (select sp.id from SubscriptionProperty sp where sp.owner = s and sp.instanceOf = null and sp.tenant = :context)',[parentSubs:subscriptions,context:contextOrg])
        childSubsOfSet.each { Subscription child ->
            Set<Subscription> children = subChildMap.get(child.instanceOf)
            if(!children)
                children = []
            children << child
            subChildMap.put(child.instanceOf,children)
        }
        Map objectNames = [:]
        if(childSubsOfSet) {
            Set rows = OrgRole.executeQuery('select oo.sub,oo.org.sortname from OrgRole oo where oo.sub in (:subChildren) and oo.roleType in (:subscrTypes)',[subChildren:childSubsOfSet,subscrTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
            rows.each { row ->
                objectNames.put(row[0],row[1])
            }
        }
        List subscriptionData = []
        subscriptions.each { Subscription sub ->
            log.debug("now processing ${sub}")
            List row = []
            //TreeSet subProviders = sub.orgRelations.findAll { OrgRole oo -> oo.roleType == RDStore.OR_PROVIDER }.collect { OrgRole oo -> oo.org.name }
            //TreeSet subAgencies = sub.orgRelations.findAll { OrgRole oo -> oo.roleType == RDStore.OR_AGENCY }.collect { OrgRole oo -> oo.org.name }
            Set subProviders = OrgRole.executeQuery('select org.name from OrgRole oo join oo.org org where oo.sub = :sub and oo.roleType = :provider order by org.name', [sub: sub, provider: RDStore.OR_PROVIDER])
            Set subAgencies = OrgRole.executeQuery('select org.name from OrgRole oo join oo.org org where oo.sub = :sub and oo.roleType = :agency order by org.name', [sub: sub, agency: RDStore.OR_AGENCY])
            TreeSet subIdentifiers = sub.ids.collect { Identifier id -> "(${id.ns.ns}) ${id.value}" }
            switch (format) {
                case [ "xls", "xlsx" ]:
                    row.add([field: sub.name ?: "", style: 'bold'])
                    row.add([field: sub.globalUID, style: null])
                    row.add([field: licenseReferences.get(sub) ? licenseReferences.get(sub).join(", ") : '', style: null])
                    List packageNames = sub.packages?.collect {
                        it.pkg.name
                    }
                    row.add([field: packageNames ? packageNames.join(", ") : '', style: null])
                    row.add([field: sub.getConsortia()?.name ?: '', style: null])
                    row.add([field: subProviders.join(', '), style: null])
                    row.add([field: subAgencies.join(', '), style: null])
                    row.add([field: sub.startDate ? sdf.format(sub.startDate) : '', style: null])
                    row.add([field: sub.endDate ? sdf.format(sub.endDate) : '', style: null])
                    row.add([field: sub.manualCancellationDate ? sdf.format(sub.manualCancellationDate) : '', style: null])
                    row.add([field: sub.referenceYear ?: '', style: null])
                    row.add([field: sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    if(!asCons) {
                        row.add([field: sub.isAutomaticRenewAnnually ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    }
                    row.add([field: subIdentifiers.join(", "),style: null])
                    row.add([field: sub.status?.getI10n("value"), style: null])
                    row.add([field: sub.kind?.getI10n("value") ?: '', style: null])
                    row.add([field: sub.form?.getI10n("value") ?: '', style: null])
                    row.add([field: sub.resource?.getI10n("value") ?: '', style: null])
                    row.add([field: sub.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    row.add([field: sub.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    row.add([field: sub.hasPublishComponent ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    if(asCons) {
                        row.add([field: subscriptionMembers.get(sub.id) ?: 0, style: null])
                        row.add([field: costItemCounts.get(sub.id) ?: 0, style: null])
                    }
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions,format,sub,subChildMap,objectNames,contextOrg))
                    subscriptionData.add(row)
                    break
                case "csv":
                    row.add(sub.name ? sub.name.replaceAll(',',' ') : "")
                    row.add(sub.globalUID)
                    row.add(licenseReferences.get(sub) ? licenseReferences.get(sub).join("; ") : '')
                    List packageNames = sub.packages?.collect {
                        it.pkg.name
                    }
                    row.add(packageNames ? packageNames.join("; ") : '')
                    row.add(sub.getConsortia()?.name ?: '')
                    row.add(subProviders.join("; ").replace(',',''))
                    row.add(subAgencies.join("; ").replace(',',''))
                    row.add(sub.startDate ? sdf.format(sub.startDate) : '')
                    row.add(sub.endDate ? sdf.format(sub.endDate) : '')
                    row.add(sub.manualCancellationDate ? sdf.format(sub.manualCancellationDate) : '')
                    row.add(sub.referenceYear ?: '')
                    row.add(sub.isMultiYear ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                    if(!asCons) {
                        row.add(sub.isAutomaticRenewAnnually ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                    }
                    row.add(subIdentifiers.join("; "))
                    row.add(sub.status?.getI10n("value"))
                    row.add(sub.kind?.getI10n("value"))
                    row.add(sub.form?.getI10n("value"))
                    row.add(sub.resource?.getI10n("value"))
                    row.add(sub.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                    row.add(sub.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                    row.add(sub.hasPublishComponent ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                    if(asCons) {
                        row.add(subscriptionMembers.get(sub.id) ? (int) subscriptionMembers.get(sub.id) : 0)
                        row.add(costItemCounts.get(sub.id) ? (int) costItemCounts.get(sub.id) : 0)
                    }
                    row.addAll(exportService.processPropertyListValues(propertyDefinitions,format,sub,subChildMap,objectNames,contextOrg))
                    subscriptionData.add(row)
                    break
            }
        }
        switch(format) {
            case [ 'xls', 'xlsx' ]:
                Map sheetData = [:]
                sheetData[message(code: 'menu.my.subscriptions')] = [titleRow: titles, columnData: subscriptionData]
                return exportService.generateXLSXWorkbook(sheetData)
            case 'csv': return exportService.generateSeparatorTableString(titles, subscriptionData, ',')
        }
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def subscriptionsManagement() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        params.tab = params.tab ?: 'generalProperties'
        EhcacheWrapper cache = contextService.getUserCache("/subscriptionsManagement/subscriptionFilter/")
        Set<String> filterFields = ['q', 'identifier', 'referenceYears', 'status', 'filterPropDef', 'filterProp', 'form', 'resource', 'subKinds', 'isPublicForApi', 'hasPerpetualAccess', 'hasPublishComponent', 'holdingSelection', 'subRunTime', 'subRunTimeMultiYear', 'subType', 'consortia']
        filterFields.each { String subFilterKey ->
            if(params.containsKey('processOption')) {
                if(cache.get(subFilterKey))
                    params.put(subFilterKey, cache.get(subFilterKey))
            }
            else {
                if(params.get(subFilterKey))
                    cache.put(subFilterKey, params.get(subFilterKey))
                else cache.remove(subFilterKey)
            }
        }

        if(!(params.tab in ['notes', 'documents', 'properties'])){
            //Important
            if (!contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
                if(params.subTypes == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id.toString()){
                    flash.error = message(code: 'subscriptionsManagement.noPermission.forSubsWithTypeConsortial') as String
                }
                else if(RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id.toString() in params.list('subTypes')){
                    flash.error = message(code: 'subscriptionsManagement.noPermission.forSubsWithTypeConsortial') as String
                }

                params.subTypes = [RDStore.SUBSCRIPTION_TYPE_LOCAL.id.toString()]
            }
        }

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

        result

    }

    /**
     * Connects the context subscription with the given pair
     * @return void, redirects to referer
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
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
    @DebugInfo(isInstEditor_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
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
     * @see DocstoreService#unifiedDeleteDocuments()
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteDocuments() {
        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        String redir
        if(params.redirectAction == 'subscriptionsManagement') {
            redir = 'subscriptionsManagement'
        }

        redirect controller: 'myInstitution', action: redir ?: 'documents', params: redir == 'subscriptionsManagement' ? [tab: 'documents'] : null /*, fragment: 'docstab' */
    }

    /**
     * Opens a list of current issue entitlements hold by the context institution. The result may be filtered;
     * filters hold for the title, the platforms, provider and subscription parameters
     * @return a (filtered) list of issue entitlements
     * @see Subscription
     * @see Platform
     * @see IssueEntitlement
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def currentTitles() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
		Profiler prf = new Profiler()
		prf.setBenchmark('init')

        if(params.tab){
            if(params.tab == 'currentIEs'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }else if(params.tab == 'plannedIEs'){
                params.status = [RDStore.TIPP_STATUS_EXPECTED.id.toString()]
            }else if(params.tab == 'expiredIEs'){
                params.status = [RDStore.TIPP_STATUS_RETIRED.id.toString()]
            }else if(params.tab == 'deletedIEs'){
                params.status = [RDStore.TIPP_STATUS_DELETED.id.toString()]
            }else if(params.tab == 'allIEs'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString(), RDStore.TIPP_STATUS_EXPECTED.id.toString(), RDStore.TIPP_STATUS_RETIRED.id.toString(), RDStore.TIPP_STATUS_DELETED.id.toString()]
            }
        }
        else if(params.list('status').size() == 1) {
            if(params.list('status')[0] == RDStore.TIPP_STATUS_CURRENT.id.toString()){
                params.tab = 'currentIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_RETIRED.id.toString()){
                params.tab = 'expiredIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_EXPECTED.id.toString()){
                params.tab = 'plannedIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_DELETED.id.toString()){
                params.tab = 'deletedIEs'
            }
        }else{
            if(params.list('status').size() > 1){
                params.tab = 'allIEs'
            }else {
                params.tab = 'currentIEs'
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }
        }

        Set<RefdataValue> orgRoles = []
        String instanceFilter = ""
        List<String> queryFilter = []

        if (contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)) {
            orgRoles << RDStore.OR_SUBSCRIPTION_CONSORTIA
            queryFilter << " sub.instanceOf is null "
            instanceFilter += "and sub.instanceOf is null"
        }
        else {
            orgRoles << RDStore.OR_SUBSCRIBER
            orgRoles << RDStore.OR_SUBSCRIBER_CONS
        }

        // Set Date Restriction
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

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List filterSub = params.list("filterSub")
        if (filterSub == "all")
            filterSub = null
        List filterPvd = params.list("filterPvd")
        if (filterPvd == "all")
            filterPvd = null
        List filterHostPlat = params.list("filterHostPlat")
        if (filterHostPlat == "all")
            filterHostPlat = null
        log.debug("Using params: ${params}")

        Map<String,Object> qryParams = [
                institution: result.institution,
                //ieStatus: RDStore.TIPP_STATUS_REMOVED,
                current: RDStore.SUBSCRIPTION_CURRENT,
                orgRoles: orgRoles
        ]

        if(checkedDate) {
            queryFilter << ' ( :checkedDate >= coalesce(ie.accessStartDate,sub.startDate,tipp.accessStartDate) or (ie.accessStartDate is null and sub.startDate is null and tipp.accessStartDate is null) ) and ( :checkedDate <= coalesce(ie.accessEndDate,sub.endDate,tipp.accessEndDate) or (ie.accessEndDate is null and sub.endDate is null and tipp.accessEndDate is null)  or (sub.hasPerpetualAccess = true))'
            /*queryFilter << ' (ie.accessStartDate <= :checkedDate or ' +
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
                            ')'*/
            qryParams.checkedDate = checkedDate
        }

        if ((params.filter) && (params.filter.length() > 0)) {
            queryFilter << "genfunc_filter_matcher(tipp.name, :titlestr) = true "
            qryParams.titlestr = params.get('filter').toString()
        }

        if (filterSub) {
            queryFilter << "sub in (" + filterSub.join(", ") + ")"
        }

        if (filterHostPlat) {
            queryFilter << "tipp.platform in (" + filterHostPlat.join(", ") + ")"
        }

        //String havingClause = params.filterMultiIE ? 'having count(ie.ie_id) > 1' : ''

        String orderByClause
        if ((params.sort != null) && (params.sort.length() > 0)) {
            orderByClause = " order by ${params.sort} ${params.order} "
        }
        else {
            if (params.order == 'desc') {
                orderByClause = 'order by tipp.sortname desc, tipp.name desc'
            } else {
                orderByClause = 'order by tipp.sortname asc, tipp.name asc'
            }
        }

        String qryString = "from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join sub.orgRelations oo join ie.status status where sub.status = :current and oo.roleType in (:orgRoles) and oo.org = :institution "
        if(queryFilter)
            qryString += ' and '+queryFilter.join(' and ')

        Map<String,Object> qryParamsClone = qryParams.clone()
        if(!params.containsKey('fileformat') && !params.containsKey('exportKBart')) {
            List counts = IssueEntitlement.executeQuery('select new map(count(*) as count, status as status) '+ qryString+ ' and status != :ieStatus group by status', qryParamsClone+[ieStatus: RDStore.TIPP_STATUS_REMOVED])
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
            /*result.currentIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_CURRENT, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.plannedIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_EXPECTED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.expiredIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_RETIRED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.deletedIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status = :status and ie.status != :ieStatus', qryParamsClone + [status: RDStore.TIPP_STATUS_DELETED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.allIECounts = IssueEntitlement.executeQuery('select count(ie) '+ qryString+ ' and ie.tipp.status in (:status) and ie.status != :ieStatus', qryParamsClone + [status: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED], ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]*/
        }

        if(params.status != '' && params.status != null && params.list('status')) {
            List<Long> status = []
            params.list('status').each { String statusId ->
                status << Long.parseLong(statusId)
            }
            qryString += " and ie.tipp.status.id in (:status) "
            qryParams.status = status

        }

        Map<String, Object> selectedFields = [:]
        Set<Long> currentIssueEntitlements = []
        Set<Long> tippIDs = []
        if(params.fileformat) {
            String consFilter = ""
            Set<RefdataValue> roleTypes = []
            if(customerTypeService.isConsortium(result.contextCustomerType)) {
                consFilter = " and sub.instanceOf = null "
                roleTypes << RDStore.OR_SUBSCRIPTION_CONSORTIA
            }
            else roleTypes.addAll([RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])
            //load proxies instead of full objects ... maybe this gonna bring performance?
            tippIDs.addAll(TitleInstancePackagePlatform.executeQuery("select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.status != :ieStatus and ie.subscription in (select sub from OrgRole oo join oo.sub sub where oo.org = :contextOrg and oo.roleType in (:roleTypes) and (sub.status = :current or sub.hasPerpetualAccess = true)"+consFilter+")", [ieStatus: RDStore.TIPP_STATUS_REMOVED, contextOrg: result.institution, roleTypes: roleTypes, current: RDStore.SUBSCRIPTION_CURRENT], [sort: 'sortname']))
            //tipps.addAll(TitleInstancePackagePlatform.findAllByIdInList(titleIDs, [sort: 'sortname']))
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
        }
        else if(!params.containsKey('fileformat') && !params.containsKey('exportKBart')) {

            String query = ''
            Map queryMap = [:]
            //second filter needed because double-join on same table does deliver me empty results
            if (filterPvd) {
                query = " from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg join pkg.orgs oo where oo.roleType in (:cpRole) and oo.org.id in ("+filterPvd.join(", ")+") "
                queryMap = [cpRole:[RDStore.OR_CONTENT_PROVIDER,RDStore.OR_PROVIDER,RDStore.OR_AGENCY,RDStore.OR_PUBLISHER]]
            }
            else {
                query = qryString
                queryMap = qryParams

            }

            currentIssueEntitlements.addAll(IssueEntitlement.executeQuery('select ie.id '+query, queryMap))
            Set<TitleInstancePackagePlatform> allTitles = TitleInstancePackagePlatform.executeQuery('select tipp from IssueEntitlement ie join ie.tipp tipp where ie.id in (select ie.id ' + query + ') and ie.status != :ieStatus group by tipp, ie.id '+orderByClause,[ieStatus: RDStore.TIPP_STATUS_REMOVED]+queryMap, [max: result.max, offset: result.offset])
            result.subscriptions = Subscription.executeQuery('select distinct(sub) from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.roleType in (:orgRoles) and oo.org = :institution and (sub.status = :current or sub.hasPerpetualAccess = true) '+instanceFilter+" order by sub.name asc",[
                    institution: result.institution,
                    current: RDStore.SUBSCRIPTION_CURRENT,
                    orgRoles: orgRoles])
            if(result.subscriptions.size() > 0) {
                //Set<Long> allIssueEntitlements = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription in (:currentSubs)',[currentSubs:result.subscriptions])
                result.providers = Org.executeQuery('select org.id,org.name from TitleInstancePackagePlatform tipp join tipp.pkg pkg join pkg.orgs oo join oo.org org where tipp.id in (select tipp.id '+qryString+') group by org.id order by org.name asc',qryParams)
                result.hostplatforms = Platform.executeQuery('select plat.id,plat.name from TitleInstancePackagePlatform tipp join tipp.platform plat where tipp.id in (select tipp.id '+qryString+') group by plat.id order by plat.name asc',qryParams)
            }
            result.num_ti_rows = TitleInstancePackagePlatform.executeQuery('select count(*) from IssueEntitlement ie join ie.tipp tipp where ie.id in (select ie.id ' + query + ') and ie.status != :ieStatus ',[ieStatus: RDStore.TIPP_STATUS_REMOVED]+queryMap)[0]
            result.titles = allTitles

            result.filterSet = params.filterSet || defaultSet
        }

        String filename = "${message(code:'export.my.currentTitles')}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"

		List bm = prf.stopBenchmark()
		result.benchMark = bm
        if(params.exportKBart) {
            String dir = GlobalService.obtainFileStorageLocation()
            File f = new File(dir+'/'+filename)
            if(!f.exists()) {
                Map<String, Object> configMap = [:]
                configMap.putAll(params)
                configMap.validOn = checkedDate.getTime()
                String consFilter = result.institution.isCustomerType_Consortium() ? ' and s.instanceOf is null' : ''
                configMap.pkgIds = SubscriptionPackage.executeQuery('select sp.pkg.id from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where oo.org = :context and oo.roleType in (:subscrTypes)'+consFilter, [context: result.institution, subscrTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS]])
                FileOutputStream out = new FileOutputStream(f)
                Map<String,List> tableData = exportService.generateTitleExportKBART(configMap, IssueEntitlement.class.name)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.columnData,'\t'))
                }
                out.flush()
                out.close()
            }
            Map fileResult = [token: filename]
            render template: '/templates/bulkItemDownload', model: fileResult
            return
        }
        else if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(tippIDs, selectedFields, ExportClickMeService.FORMAT.XLS)
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            /*
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.validOn = checkedDate.getTime()
            String consFilter = result.institution.isCustomerType_Consortium() ? ' and s.instanceOf is null' : ''
            configMap.pkgIds = SubscriptionPackage.executeQuery('select sp.pkg.id from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where oo.org = :context and oo.roleType in (:subscrTypes)'+consFilter, [context: result.institution, subscrTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS]])
            Map<String,List> export = exportService.generateTitleExportCustom(configMap, IssueEntitlement.class.name) //all subscriptions, all packages
            Map sheetData = [:]
            sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            */

            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else if(params.fileformat == 'csv') {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
            response.contentType = "text/csv"

            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                writer.write(exportClickMeService.exportTipps(tippIDs,selectedFields,ExportClickMeService.FORMAT.CSV))
            }
            out.flush()
            out.close()
        }
        else
            result
    }

    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def currentPermanentTitles() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)


        if(params.tab){
            if(params.tab == 'currentIEs'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }else if(params.tab == 'plannedIEs'){
                //params.status = [RDStore.TIPP_STATUS_EXPECTED.id.toString()]
            }else if(params.tab == 'expiredIEs'){
                params.status = [RDStore.TIPP_STATUS_RETIRED.id.toString()]
            }else if(params.tab == 'deletedIEs'){
                params.status = [RDStore.TIPP_STATUS_DELETED.id.toString()]
            }else if(params.tab == 'allIEs'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString(), RDStore.TIPP_STATUS_EXPECTED.id.toString(), RDStore.TIPP_STATUS_RETIRED.id.toString(), RDStore.TIPP_STATUS_DELETED.id.toString()]
            }
        }
        else if(params.list('status').size() == 1) {
            if(params.list('status')[0] == RDStore.TIPP_STATUS_CURRENT.id.toString()){
                params.tab = 'currentIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_RETIRED.id.toString()){
                params.tab = 'expiredIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_EXPECTED.id.toString()){
                //params.tab = 'plannedIEs'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_DELETED.id.toString()){
                params.tab = 'deletedIEs'
            }
        }else{
            if(params.list('status').size() > 1){
                params.tab = 'allIEs'
            }else {
                params.tab = 'currentIEs'
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Map query = filterService.getPermanentTitlesQuery(params, result.institution)
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

        result.currentTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: result.institution, status: RDStore.TIPP_STATUS_CURRENT, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.plannedTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: result.institution, status: RDStore.TIPP_STATUS_EXPECTED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.expiredTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: result.institutionn, status: RDStore.TIPP_STATUS_RETIRED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.deletedTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status = :status and pt.issueEntitlement.status != :ieStatus", [org: result.institution, status: RDStore.TIPP_STATUS_DELETED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
        result.allTippCounts = PermanentTitle.executeQuery("select count(*) from PermanentTitle as pt where pt.owner = :org and pt.tipp.status in (:status) and pt.issueEntitlement.status != :ieStatus", [org: result.institution, status: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED], ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]

        //for tipp_ieFilter
        params.institution = result.institution
        params.filterForPermanentTitle = true

        result
    }

    /**
     * Opens a list of current packages subscribed by the context institution. The result may be filtered
     * by filter parameters
     * @return a list view of packages the institution has subscribed
     * @see SubscriptionPackage
     * @see Package
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def currentPackages() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)
        result.languages = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        List currentSubIds = []
        List idsCategory1  = []
        List idsCategory2  = []

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }

        List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())
        result.filterSet = tmpQ[2]
        currentSubIds = Subscription.executeQuery( "select s.id " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]

        idsCategory1 = OrgRole.executeQuery("select distinct (sub.id) from OrgRole where org=:org and roleType in (:roleTypes)", [
                org: contextService.getOrg(), roleTypes: [
                RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS
        ]
        ])
        idsCategory2 = OrgRole.executeQuery("select distinct (sub.id) from OrgRole where org=:org and roleType in (:roleTypes)", [
                org: contextService.getOrg(), roleTypes: [
                RDStore.OR_SUBSCRIPTION_CONSORTIA
        ]
        ])

        result.subscriptionMap = [:]
        result.packageList = []
        result.packageListTotal = 0

        if(currentSubIds) {
            long start = System.currentTimeMillis()
            String qry3 = "select distinct pkg, s from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg " +
                    "where s.id in (:currentSubIds) "

            qry3 += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"

            Map qryParams3 = [
                    currentSubIds  : currentSubIds,
                    pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED
            ]

            if (params.pkg_q?.length() > 0) {
                qry3 += " and ("
                qry3 += "   genfunc_filter_matcher(pkg.name, :query) = true"
                qry3 += ")"
                qryParams3.put('query', "${params.pkg_q}")
            }

            if (params.ddc && params.list('ddc').size() > 0) {
                qry3 += " and ((exists (select ddc.id from DeweyDecimalClassification ddc where ddc.ddc.id in (:ddcs) and ddc.tipp.pkg = pkg)) or (exists (select ddc.id from DeweyDecimalClassification ddc where ddc.ddc.id in (:ddcs) and ddc.pkg = pkg)))"
                qryParams3.put('ddcs', params.list("ddc").collect { String ddc -> Long.parseLong(ddc) })
            }

            qry3 += " group by pkg, s"
            qry3 += " order by pkg.name " + (params.order ?: 'asc')
            log.debug("before query: ${System.currentTimeMillis()-start}")
            List packageSubscriptionList = Subscription.executeQuery(qry3, qryParams3)
            /*, [max:result.max, offset:result.offset])) */
            log.debug("after query: ${System.currentTimeMillis()-start}")
            packageSubscriptionList.eachWithIndex { entry, int i ->
                // log.debug("processing entry ${i} at: ${System.currentTimeMillis()-start}")
                String key = 'package_' + entry[0].id

                if (! result.subscriptionMap.containsKey(key)) {
                    result.subscriptionMap.put(key, [])
                }
                if (entry[1].status?.value == RDStore.SUBSCRIPTION_CURRENT.value) {

                    if (idsCategory1.contains(entry[1].id)) {
                        result.subscriptionMap.get(key).add(entry[1])
                    }
                    else if (idsCategory2.contains(entry[1].id) && entry[1].instanceOf == null) {
                        result.subscriptionMap.get(key).add(entry[1])
                    }
                }
            }
            log.debug("after collect: ${System.currentTimeMillis()-start}")
            List tmp = (packageSubscriptionList.collect { it[0] }).unique()
            log.debug("after filter: ${System.currentTimeMillis()-start}")
            result.packageListTotal = tmp.size()
            result.packageList = tmp.drop(result.offset).take(result.max)
        }

        result
    }

    /**
     * Opens the dashboard for the user; showing important information regarding the context institution.
     * The information is grouped in tabs where information is being preloaded (except changes, notifications and surveys)
     * @return the dashboard view with the prefilled tabs
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true, ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def dashboard() {

        Map<String, Object> ctrlResult = myInstitutionControllerService.dashboard(this, params)

        if (ctrlResult.status == MyInstitutionControllerService.STATUS_ERROR) {
            flash.error = "You do not have permission to access ${ctrlResult.result.institution.name} pages. Please request access on the profile page"
            response.sendError(401)
                return
        }

        return ctrlResult.result
    }

    /**
     * Opens the modal to create a new task
     * @return the task creation modal
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def modal_create() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (! (result.user as User).isFormal(result.institution as Org)) {
            flash.error = "You do not have permission to access ${result.institution.name} pages. Please request access on the profile page";
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return;
        }

        Map<String, Object> preCon = taskService.getPreconditions(result.institution)
        result << preCon

        render template: '/templates/tasks/modal_create', model: result
    }

    /**
     * Call for the list of entitlement changes of the last 600 days
     * @return a list of changes to be accepted or rejected
     * @see PendingChange
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def changes() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.acceptedOffset = 0
        def periodInDays = 600
        Map<String,Object> pendingChangeConfigMap = [contextOrg: result.institution, consortialView:accessService.otherOrgPerm(result.institution, 'ORG_CONSORTIUM_BASIC'), periodInDays:periodInDays, max:result.max, offset:result.acceptedOffset]

        result.putAll(pendingChangeService.getChanges_old(pendingChangeConfigMap))

        result
    }

    /**
     * Call for the finance import starting page; the mappings are being explained here and an example sheet for submitting data to import
     * @return the finance import entry view
     */
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def generateFinanceImportWorksheet() {
        Subscription subscription = Subscription.get(params.id)
        Set<String> keys = ["title","element","elementSign","referenceCodes","budgetCode","status","invoiceTotal",
                            "currency","exchangeRate","taxType","taxRate","value","subscription","package",
                            "issueEntitlement","datePaid","financialYear","dateFrom","dateTo","invoiceDate",
                            "description","invoiceNumber","orderNumber"]
        Set<List<String>> identifierRows = []
        Set<String> colHeaders = []
        subscription.derivedSubscriptions.each { subChild ->
            List<String> row = []
            keys.eachWithIndex { String entry, int i ->
                colHeaders << message(code:"myinst.financeImport.${entry}")
                if(entry == "subscription") {
                    row[i] = subChild.globalUID
                }
                else row[i] = ""
            }
            identifierRows << row
        }
        String template = exportService.generateSeparatorTableString(colHeaders,identifierRows,",")
        response.setHeader("Content-disposition", "attachment; filename=\"bulk_upload_template_${escapeService.escapeString(subscription.name)}.csv\"")
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def processFinanceImport() {
        CostItem.withTransaction { TransactionStatus ts ->
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
            MultipartFile tsvFile = request.getFile("tsvFile") //this makes the withTransaction closure necessary
            if(tsvFile && tsvFile.size > 0) {
                String encoding = UniversalDetector.detectCharset(tsvFile.getInputStream())
                if(encoding == "UTF-8") {
                    result.filename = tsvFile.originalFilename
                    Map<String,Map> financialData = financeService.financeImport(tsvFile)
                    result.candidates = financialData.candidates
                    result.budgetCodes = financialData.budgetCodes
                    result.criticalErrors = [/*'ownerMismatchError',*/'noValidSubscription','multipleSubError','packageWithoutSubscription','noValidPackage','multipleSubPkgError',
                                             'packageNotInSubscription','entitlementWithoutPackageOrSubscription','noValidTitle','multipleTitleError','noValidEntitlement','multipleEntitlementError',
                                             'entitlementNotInSubscriptionPackage','multipleOrderError','multipleInvoiceError','invalidCurrencyError','invoiceTotalInvalid','valueInvalid','exchangeRateInvalid',
                                             'invalidTaxType','invalidYearFormat','noValidStatus','noValidElement','noValidSign']
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def subscriptionImport() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        List<String> mappingCols = ["name", "owner", "status", "type", "form", "resource", "provider", "agency", "startDate", "endDate",
                              "manualCancellationDate", "hasPerpetualAccess", "hasPublishComponent", "isPublicForApi",
                              "customProperties", "privateProperties", "notes"]
        if(result.institution.isCustomerType_Inst_Pro()) {
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def processSubscriptionImport() {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
            MultipartFile tsvFile = request.getFile("tsvFile") //this makes the transaction closure necessary
            if(tsvFile && tsvFile.size > 0) {
                String encoding = UniversalDetector.detectCharset(tsvFile.getInputStream())
                if(encoding == "UTF-8") {
                    result.filename = tsvFile.originalFilename
                    Map subscriptionData = subscriptionService.subscriptionImport(tsvFile)
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )
    })
    def currentSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate)", [org: result.institution]) ?: []

        //SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.tab = params.tab ?: 'new'

        //if(params.tab != 'new'){
            params.sort = 'surInfo.endDate DESC, LOWER(surInfo.name)'
        //}

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears << newYear
            }
            params.validOnYear = [newYear]
        }

        result.propList = PropertyDefinition.findAll( "select sur.type from SurveyResult as sur where sur.participant = :contextOrg order by sur.type.name_de asc", [contextOrg: result.contextOrg]).groupBy {it}.collect {it.key}

        result.allConsortia = Org.executeQuery(
                """select o from Org o, SurveyInfo surInfo where surInfo.owner = o
                        group by o order by lower(o.name) """
        )

        Set orgIds = orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( contextService.getOrg() )

        result.providers = orgIds.isEmpty() ? [] : Org.findAllByIdInList(orgIds).sort { it?.name }

        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIBER_CONS, 'activeInst': result.institution])

        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()


        def fsq = filterService.getParticipantSurveyQuery_New(params, sdFormat, result.institution)

        result.surveyResults = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)

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
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems(surveyConfigsforExport, result.institution)
            }else {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.plural")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys(surveyConfigsforExport, result.institution)
            }
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else {
            result.surveyResults = result.surveyResults.groupBy {it.id[1]}
            result.countSurveys = surveyService._getSurveyParticipantCounts_New(result.institution, params)

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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )
    })
    def surveyInfos() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.contextOrg = contextService.getOrg()

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(Long.parseLong(params.surveyConfigID.toString())) : result.surveyInfo.surveyConfigs[0]

        result.surveyResults = []

        result.surveyConfig.getSortedSurveyProperties().each{ PropertyDefinition propertyDefinition ->
            result.surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(result.institution, result.surveyConfig, propertyDefinition)
        }

        result.ownerId = result.surveyInfo.owner?.id

        if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.institution)
            result.formalOrg = result.user.formalOrg as Org
            // restrict visible for templates/links/orgLinksAsList
            result.costItemSums = [:]
            result.visibleOrgRelations = []
            if(result.subscription) {
                result.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.contextOrg.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

                //costs dataToDisplay
                result.dataToDisplay = ['subscr']
                result.offsets = [subscrOffset: 0]
                result.sortConfig = [subscrSort: 'sub.name', subscrOrder: 'asc']

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                //cost items
                //params.forExport = true
                LinkedHashMap costItems = result.subscription ? financeService.getCostItemsForSubscription(params, result) : null
                if (costItems?.subscr) {
                    result.costItemSums.subscrCosts = costItems.subscr.costItems
                }
		        result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
            }

            if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION]) {
                result.successorSubscription = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                Collection<AbstractPropertyWithCalculatedLastUpdated> props
                props = result.surveyConfig.subscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                if(result.successorSubscription){
                    props += result.successorSubscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                }
                result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
            }

            if (result.subscription && result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                result.previousSubscription = result.subscription._getCalculatedPreviousForSurvey()

                /*result.previousIesListPriceSum = 0
                if(result.previousSubscription){
                    result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                            'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                    [sub: result.previousSubscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                }*/

                result.sumListPriceSelectedIEs = surveyService.sumListPriceIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)


               /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                        'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                        [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */
                result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)
                result.countCurrentPermanentTitles = subscriptionService.countCurrentPermanentTitles(result.subscription, false)

/*                if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                    result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
                } else {
                    result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
                }*/


                result.subscriber = result.subscription.getSubscriber()
            }

        }

        if ( params.exportXLSX ) {
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.label")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportSurveys([result.surveyConfig], result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else {
            withFormat {
                html {
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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )
    })
    def surveyInfoFinish() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyInfo surveyInfo = SurveyInfo.get(params.id)
        SurveyConfig surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : surveyInfo.surveyConfigs[0]
        boolean sendMailToSurveyOwner = false

        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.institution, surveyConfig)

        List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(result.institution, surveyConfig)

        boolean allResultHaveValue = true
        List<PropertyDefinition> notProcessedMandatoryProperties = []
        surveyResults.each { SurveyResult surre ->
            SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surre.type)
            if (surveyConfigProperties.mandatoryProperty && !surre.isResultProcessed() && !surveyOrg.existsMultiYearTerm()) {
                allResultHaveValue = false
                notProcessedMandatoryProperties << surre.type.getI10n('name')
            }
        }

        boolean noParticipation = false
        if(surveyInfo.isMandatory) {
            if(surveyConfig && surveyConfig.subSurveyUseForTransfer){
                noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(result.institution, surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
            }
        }

        if(notProcessedMandatoryProperties.size() > 0){
            flash.error = message(code: "confirm.dialog.concludeBinding.survey.notProcessedMandatoryProperties", args: [notProcessedMandatoryProperties.join(', ')]) as String
        }
        else if(noParticipation || allResultHaveValue){
            surveyOrg.finishDate = new Date()
            if (!surveyOrg.save()) {
                flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess') as String
            } else {
                flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess') as String
                sendMailToSurveyOwner = true
            }
        }
        else if(!noParticipation && allResultHaveValue){
            surveyOrg.finishDate = new Date()
            if (!surveyOrg.save()) {
                flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess') as String
            } else {
                flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess') as String
                sendMailToSurveyOwner = true
            }
        }

        if(sendMailToSurveyOwner) {
            mailSendService.emailToSurveyOwnerbyParticipationFinish(surveyInfo, result.institution)
            mailSendService.emailToSurveyParticipationByFinish(surveyInfo, result.institution)
        }


        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_INST_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )
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
        Org org = result.institution

        result.editable = (surveyInfo && surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

        if(result.institution.id == surveyInfo.owner.id) {
            org = params.participant ? Org.get(params.participant) : null
        }

        if (org && surveyLink && result.editable) {

            SurveyOrg.withTransaction { TransactionStatus ts ->
                    boolean existsMultiYearTerm = false
                    Subscription sub = surveyConfig.subscription
                    if (sub && !surveyConfig.pickAndChoose && surveyConfig.subSurveyUseForTransfer) {
                        Subscription subChild = sub.getDerivedSubscriptionBySubscribers(org)

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
                            }
                        }
                    }
                surveyConfig.save()
                }

            if(result.institution.id == surveyInfo.owner.id){
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
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    def users() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Map filterParams = params
        filterParams.org = genericOIDService.getOID(result.institution)

        result.users = userService.getUserSet(filterParams)
        result.titleMessage = "${result.institution}"
        result.inContextOrg = true
        result.orgInstance = result.institution
        result.multipleAffiliationsWarning = true

        result.navConfig = [
                orgInstance: result.institution, inContextOrg: result.inContextOrg
        ]
        result.filterConfig = [
                filterableRoles:Role.findAllByRoleType('user'), orgField: false
        ]
        result.tmplConfig = [
                editable: result.editable,
                editor: result.user,
                editLink: 'editUser',
                deleteLink: 'deleteUser',
                users: result.users,
                showAllAffiliations: false,
                modifyAccountEnability: SpringSecurityUtils.ifAllGranted('ROLE_YODA')
        ]
        result.total = result.users.size()

        render view: '/user/global/list', model: result
    }

    /**
     * Call to delete a given user
     * @return the user deletion page where the details of the given user are being enumerated
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
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
                User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

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
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
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
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
    })
    def createUser() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.orgInstance = result.institution
        result.editor = result.user
        result.inContextOrg = true
        result.availableOrgs = [ result.orgInstance ]

        render view: '/user/global/create', model: result
    }

    /**
     * Processes the submitted parameters and creates a new user for the context institution
     * @return a redirect to the profile edit page on success, back to the user creation page otherwise
     */
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
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
    @DebugInfo(isInstAdm_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstAdm_or_ROLEADMIN()
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def addressbook() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        params.sort = params.sort ?: 'pr.org.sortname'
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
            configMap = [function:[], position: [], type: [], sort: 'pr.org.sortname']
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

        Set<String> filterFields = ['org', 'prs', 'filterPropDef', 'filterProp', 'function', 'position', 'showOnlyContactPersonForInstitution', 'showOnlyContactPersonForProviderAgency']
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

        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }

        /*
        if(params.exportXLS) {
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) exportService.exportAddressbook('xlsx', visiblePersons)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }
        else */
        if(params.fileformat == 'xlsx') {

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportAddresses(visiblePersons, visibleAddresses, selectedFields, params.exportOnlyContactPersonForInstitution == 'true', params.exportOnlyContactPersonForProviderAgency == 'true', ExportClickMeService.FORMAT.XLS)

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
            out.withWriter { Writer writer ->
                writer.write((String) exportService.exportAddressbook('csv', visiblePersons))
            }
            out.close()
        }
        else {
            result
        }
      }

    /**
     * Call for the current budget code overview of the institution
     * @return a list of budget codes the context institution currently holds
     * @see BudgetCode
     * @see CostItemGroup
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
                                owner: result.institution,
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
                    if (bc && bc.owner.id == result.institution.id) {
                        BudgetCode.executeUpdate('delete from BudgetCode bc where bc.id = :bcid', [bcid: bc.id])
                    }
                }

            }
            Set<BudgetCode> allBudgetCodes = BudgetCode.findAllByOwner(result.institution, [sort: 'value'])
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    def tasks() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if ( ! params.sort) {
            params.sort = "t.endDate"
            params.order = "asc"
        }
        if ( ! params.ctrlFilterSend) {
            params.taskStatus = RDStore.TASK_STATUS_OPEN.id as String
        }
        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        Map<String, Object> queryForFilter = filterService.getTaskQuery(params, sdFormat)
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        result.taskInstanceList = taskService.getTasksByResponsibles(result.user, result.institution, queryForFilter)
        result.taskInstanceList = taskService.chopOffForPageSize(result.taskInstanceList, result.user, offset)

        result.myTaskInstanceList = taskService.getTasksByCreator(result.user,  queryForFilter, null)
        result.myTaskInstanceList = taskService.chopOffForPageSize(result.myTaskInstanceList, result.user, offset)

        Map<String, Object> preCon = taskService.getPreconditions(result.institution)
        result << preCon

        //log.debug(result.taskInstanceList.toString())
        //log.debug(result.myTaskInstanceList.toString())
        result
    }

    /**
     * After consideration, this workflow actually makes no sense as a consortium is no administrative unit but a flexible
     * one, determined by subscriptions. Is thus a legacy construct, based on a misunderstanding of concept.
     * Call for listing institutions eligible to be attached to or detached from the context consortium
     * @return a list of institutions
     */
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def addMembers() {
        Combo.withTransaction {
            Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

            // new: filter preset
            result.comboType = 'Consortium'
            params.orgType = RDStore.OT_INSTITUTION.id.toString()
            params.orgSector = RDStore.O_SECTOR_HIGHER_EDU.id.toString()

            if (params.selectedOrgs) {
                if (formService.validateToken(params)) {
                    log.debug('adding orgs to consortia/institution')

                    params.list('selectedOrgs').each { soId ->
                        Map<String, Object> map = [
                                toOrg  : result.institution,
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

            Map<String, Object> fsq = filterService.getOrgQuery(params)
            List<Org> availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, params)
            Set<Org> currentMembers = Org.executeQuery('select c.fromOrg from Combo c where c.toOrg = :current and c.type = :comboType', [current: result.institution, comboType: RefdataValue.getByValueAndCategory(result.comboType, RDConstants.COMBO_TYPE)])
            result.availableOrgs = availableOrgs - currentMembers

            result
        }
    }

    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    def currentMarkers() {
        log.debug 'currentMarkers()'

        Marker.TYPE fMarkerType = Marker.TYPE.WEKB_CHANGES
        if (params.filterMarkerType) {
            fMarkerType = Marker.TYPE.get(params.filterMarkerType)
        }

        // TODO -- permissions --

        Map<String, Object> result = [
                myMarkedObjects: [
                        org: markerService.getObjectsByClassAndType(Org.class, fMarkerType),
                        pkg: markerService.getObjectsByClassAndType(Package.class, fMarkerType),
                        plt: markerService.getObjectsByClassAndType(Platform.class, fMarkerType)
                ],
                myXMap:             markerService.getMyXMap(),
                filterMarkerType:   fMarkerType.value
        ]
        result
    }

    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO], ctrlService = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
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
            if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_AGENCY.id.toString()) {
                idQuery = idQuery + ' and wf.org is not null'
                idQuery = idQuery + ' and exists (select ot from wf.org.orgType as ot where ot = :orgType )'
                queryParams.put('orgType', RDStore.OT_AGENCY)
            }
            if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION.id.toString()) {
                idQuery = idQuery + ' and wf.org is not null'
                idQuery = idQuery + ' and exists (select ot from wf.org.orgType as ot where ot = :orgType )'
                queryParams.put('orgType', RDStore.OT_INSTITUTION)
            }
            else if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE.id.toString()) {
                idQuery = idQuery + ' and wf.license is not null'
            }
            else if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_OWNER.id.toString()) {
                idQuery = idQuery + ' and wf.org = :ctxOrg'
            }
            else if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER.id.toString()) {
                idQuery = idQuery + ' and wf.org is not null'
                idQuery = idQuery + ' and exists (select ot from wf.org.orgType as ot where ot = :orgType )'
                queryParams.put('orgType', RDStore.OT_PROVIDER)
            }
            if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION.id.toString()) {
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
            if (result.filterStatus == RDStore.WF_WORKFLOW_STATUS_OPEN.id.toString()) {
                result.currentWorkflows = result.openWorkflows
            }
            else if (result.filterStatus == RDStore.WF_WORKFLOW_STATUS_DONE.id.toString()) {
                result.currentWorkflows = result.doneWorkflows
            }
        }

        result.total = result.currentWorkflows.size()
        result.currentWorkflows = workflowService.sortByLastUpdated(result.currentWorkflows).drop(result.offset).take(result.max) // pagination

        result
    }

    /**
     * Call for the overview of current workflows for the context institution
     * @return the entry view for the workflows, loading current cache settings
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    @Deprecated
    def currentWorkflowsOld() {
        Map<String, Object> result = [:]

        SessionCacheWrapper cache = contextService.getSessionCache()
        String urlKey = 'myInstitution/currentWorkflows'
        String fmKey  = urlKey + '/_filterMap'
        String pmKey  = urlKey + '/_paginationMap'

        if (params.size() == 2 && params.containsKey('controller') && params.containsKey('action')) { // first visit
            cache.remove(fmKey)
            cache.remove(pmKey)
        }
        else if (params.cmd) { // modal,delete, etc. - process and remove form params
            result.putAll( workflowOldService.usage(params) )
            params.clear()
        }

        Map filter = params.findAll{ it.key.startsWith('filter') }
        params.remove('filter') // remove reset/control flag

        Map pagination = [
                tab:    params.tab    ?: 'open',
                offset: params.offset ? params.int('offset') : 0,
                max:    params.max    ? params.int('max') : contextService.getUser().getPageSizeOrDefault()
        ]
        if (! params.tab && cache.get(pmKey)?['tab']) {
            pagination.put('tab', cache.get(pmKey)['tab'] as String) // remember last tab
        }

        if (filter.get('filter') == 'reset') {
            cache.remove(fmKey)
        }
        else if (filter.get('filter') == 'true') { // remove control flag + store filter
            filter.remove('filter')
            cache.put(fmKey, filter)
        }

        cache.put(pmKey, pagination) // store pagination

        if (cache.get(fmKey)) { result.putAll(cache.get(fmKey) as Map) }
        if (cache.get(pmKey)) { result.putAll(cache.get(pmKey) as Map) }

        String idQuery = 'select wf.id from WfWorkflow wf where wf.owner = :ctxOrg'
        Map<String, Object> queryParams = [ctxOrg: contextService.getOrg()]

//        result.currentSubscriptions = WfWorkflow.executeQuery(
//                'select distinct sub from WfWorkflow wf join wf.subscription sub where wf.owner = :ctxOrg order by sub.name', queryParams
//        )
//        result.currentProviders = result.currentSubscriptions ? Org.executeQuery(
//                'select distinct ooo.org from OrgRole ooo where ooo.sub in (:subscriptions) and ooo.roleType = :provider',
//                [subscriptions: result.currentSubscriptions, provider: RDStore.OR_PROVIDER]
//        ) : []

        result.currentPrototypes = WfWorkflow.executeQuery(
                'select distinct wf.prototype.id, wf.prototypeTitle, wf.prototypeVariant from WfWorkflow wf where wf.owner = :ctxOrg order by wf.prototypeTitle, wf.prototypeVariant', queryParams
        ).collect{ it ->
            String hash = ((it[0]).toString() + it[1] + it[2]).md5()

            return [hash: hash, id: it[0], title: it[1], variant: it[2]]
        }

        if (result.filterPrototypeMeta) {
            Map<String, Object> match = result.currentPrototypes.find{ it.hash == result.filterPrototypeMeta } as Map
            if (match) {
                idQuery = idQuery + ' and wf.prototype.id = :fpId and wf.prototypeTitle = :fpTitle and wf.prototypeVariant = :fpVariant'
                queryParams.put('fpId', match.id)
                queryParams.put('fpTitle', match.title)
                queryParams.put('fpVariant', match.variant)
            } else {
                idQuery = idQuery + ' and wf.prototype is null' // always false
            }
        }
        if (result.filterTargetType) {
            if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION.id) {
                idQuery = idQuery + ' and wf.org is not null'
            }
            else if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE.id) {
                idQuery = idQuery + ' and wf.license is not null'
            }
            else if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER.id) {
                idQuery = idQuery + ' and wf.org is not null'
            }
            if (result.filterTargetType == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION.id) {
                idQuery = idQuery + ' and wf.subscription is not null'
            }
            idQuery = idQuery + ' and wf.prototype.targetType = :targetType'
            queryParams.put('targetType', RefdataValue.get(result.filterTargetType))
        }
        if (result.filterStatus) {
            idQuery = idQuery + ' and wf.status = :status'
            queryParams.put('status', RefdataValue.get(result.filterStatus))
        }
        if (result.filterUser) {
            if (result.filterUser == 'all') {
                idQuery = idQuery + ' and wf.user = null'
            }
            else {
                idQuery = idQuery + ' and wf.user = :user'
                queryParams.put('user', User.get(result.filterUser))
            }
        }
        if (result.filterProvider) {
            idQuery = idQuery + ' and exists (select ooo from OrgRole ooo join ooo.sub sub where ooo.org = :provider and ooo.roleType = :roleType and sub = wf.subscription)'
            queryParams.put('roleType', RDStore.OR_PROVIDER)
            queryParams.put('provider', Org.get(result.filterProvider))
        }
        if (result.filterSubscription) {
            idQuery = idQuery + ' and wf.subscription = :subscription'
            queryParams.put('subscription', Subscription.get(result.filterSubscription))
        }

        List<Long> workflowIds = WfWorkflow.executeQuery(idQuery + ' order by wf.id desc', queryParams)

        String statusQuery = 'select wf.id from WfWorkflow wf where wf.id in (:idList) and wf.status = :status'
        String resultQuery = 'select wf from WfWorkflow wf where wf.id in (:idList)'

        result.currentWorkflowIds_open     = WfWorkflow.executeQuery(statusQuery, [idList: workflowIds, status: RDStore.WF_WORKFLOW_STATUS_OPEN])
        result.currentWorkflowIds_canceled = WfWorkflow.executeQuery(statusQuery, [idList: workflowIds, status: RDStore.WF_WORKFLOW_STATUS_CANCELED])
        result.currentWorkflowIds_done     = WfWorkflow.executeQuery(statusQuery, [idList: workflowIds, status: RDStore.WF_WORKFLOW_STATUS_DONE])

        if (result.tab == 'open') {
            result.currentWorkflows = workflowOldService.sortByLastUpdated( WfWorkflow.executeQuery(resultQuery, [idList: result.currentWorkflowIds_open])).drop(result.offset).take(result.max)
        }
        else if (result.tab == 'canceled') {
            result.currentWorkflows = workflowOldService.sortByLastUpdated( WfWorkflow.executeQuery(resultQuery, [idList: result.currentWorkflowIds_canceled])).drop(result.offset).take(result.max)
        }
        else if (result.tab == 'done') {
            result.currentWorkflows = workflowOldService.sortByLastUpdated( WfWorkflow.executeQuery(resultQuery, [idList: result.currentWorkflowIds_done])).drop(result.offset).take(result.max)
        }
        result.total = workflowIds.size()

        result
    }

    /**
     * Call for the table view of those consortia which are linked to the context institution
     * @return a list of those institutions on whose consortial subscriptions the context institution is participating
     */
    @Secured(['ROLE_USER'])
    def currentConsortia() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Profiler prf = new Profiler()
        prf.setBenchmark('start')

        // new: filter preset
        result.comboType = RDStore.COMBO_TYPE_CONSORTIUM
        //params.orgSector    = RDStore.O_SECTOR_HIGHER_EDU?.id?.toString()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
        GrailsParameterMap queryParams = params.clone() as GrailsParameterMap

        result.filterSet    = params.filterSet ? true : false

        queryParams.comboType = result.comboType.value
        queryParams.invertDirection = true
        Map<String, Object> fsq = filterService.getOrgComboQuery(queryParams, result.institution)
        //def tmpQuery = "select o.id " + fsq.query.minus("select o ")
        //def memberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

		prf.setBenchmark('query')

        List totalConsortia      = Org.executeQuery(fsq.query, fsq.queryParams)
        result.totalConsortia    = totalConsortia
        result.consortiaCount    = totalConsortia.size()
        result.consortia         = totalConsortia.drop((int) result.offset).take((int) result.max)
        String header = message(code: 'menu.my.consortia')
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
        }

        /*
        if ( params.exportXLS ) {

            SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(totalConsortia, header, true, 'xls')
            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else */
        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalConsortia, selectedFields, 'consortium', ExportClickMeService.FORMAT.XLS, contactSwitch)

            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else if(params.fileformat == 'csv') {
            response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
            response.contentType = "text/csv"
            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                writer.write((String) organisationService.exportOrg(totalConsortia,header,true,"csv"))
            }
            out.close()
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
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
                            toOrg: result.institution,
                            fromOrg: Org.get(Long.parseLong(soId)),
                            type: RDStore.COMBO_TYPE_CONSORTIUM
                    )
                    cmb.delete()
                }
            }
        }
        //params.orgSector    = RDStore.O_SECTOR_HIGHER_EDU?.id?.toString()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        if(!params.subStatus) {
            if(!params.filterSet) {
                params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
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
        Map<String, Object> fsq = filterService.getOrgComboQuery(params, result.institution)
        String tmpQuery = "select o.id " + fsq.query.minus("select o ")
        List memberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

        Map queryParamsProviders = [
                subOrg      : result.institution,
                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA],
                paRoleTypes : [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
        ]

        Map queryParamsSubs = [
                subOrg      : result.institution,
                subRoleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA],
                paRoleTypes : [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
        ]

        String queryProviders = '''select distinct(or_pa.org) from OrgRole or_pa 
join or_pa.sub sub 
join sub.orgRelations or_sub where
    ( sub = or_sub.sub and or_sub.org = :subOrg ) and
    ( or_sub.roleType in (:subRoleTypes) ) and
        ( or_pa.roleType in (:paRoleTypes) )'''

        String querySubs = '''select distinct(or_pa.sub) from OrgRole or_pa 
join or_pa.sub sub 
join sub.orgRelations or_sub where
    ( sub = or_sub.sub and or_sub.org = :subOrg ) and
    ( or_sub.roleType in (:subRoleTypes) ) and
        ( or_pa.roleType in (:paRoleTypes) ) and sub.instanceOf is null'''

        if (params.subStatus) {
            queryProviders +=  " and (sub.status = :subStatus)"
            querySubs +=  " and (sub.status = :subStatus)"
            RefdataValue subStatus = RefdataValue.get(params.subStatus)
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



        List<Org> providers = Org.executeQuery(queryProviders, queryParamsProviders)


/*        List<Subscription> subscriptions = []
        if(providers || params.filterPvd) {
            querySubs += " and or_pa.org.id in (:providers)"
            if(params.filterPvd){
                queryParamsSubs << [providers: params.list('filterPvd').collect { Long.parseLong(it) }]
            }
            else {
                queryParamsSubs << [providers: providers.collect { it.id }]
            }
            subscriptions = Subscription.executeQuery(querySubs, queryParamsSubs)
        }
        result.subscriptions = subscriptions*/

        result.providers = providers

		prf.setBenchmark('query')

        if (params.filterPropDef && memberIds) {
            fsq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname asc", 'o', [oids: memberIds])
        }

        List totalMembers      = Org.executeQuery(fsq.query, fsq.queryParams)
        if(params.orgListToggler) {
            Combo.withTransaction {
                Combo.executeUpdate('delete from Combo c where c.toOrg = :context and c.fromOrg.id in (:ids)', [context: result.institution, ids: memberIds])
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
        if(params.fileformat == 'xlsx') {
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalMembers, selectedFields, 'member', ExportClickMeService.FORMAT.XLS, contactSwitch, configMap)

            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        else if(params.fileformat == 'csv') {
            response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
            response.contentType = "text/csv"
            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                writer.write((String) exportClickMeService.exportOrgs(totalMembers, selectedFields, 'member', ExportClickMeService.FORMAT.CSV, contactSwitch, configMap))
            }
            out.close()
        }
        else {
            result
        }
    }

    /**
     * Call to list consortial member institutions along with their subscriptions and costs.
     * The list may be displayed as HTML or rendered as file, either as Excel worksheet or comma separated file
     * @return a list enumerating cost item entries with the subscription and member institution attached to the cost items
     * @see CostItem
     * @see Subscription
     * @see Org
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def manageConsortiaSubscriptions() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params), selectedFields = [:]
        result.tableConfig = ['withCostItems']
        result.putAll(subscriptionService.getMySubscriptionsForConsortia(params,result.user,result.institution,result.tableConfig))
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

        if(params.fileformat == 'xlsx') {
            //result.entries has already been filtered in service method
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportConsortiaParticipations(result.entries, selectedFields, result.institution, contactSwitch, ExportClickMeService.FORMAT.XLS)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else if(params.fileformat == 'csv') {
            //result.entries has already been filtered in service method
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
            response.contentType = "text/csv"
            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                //writer.write((String) _exportcurrentSubscription(result.allSubscriptions,"csv", result.institution))
                writer.write((String) exportClickMeService.exportConsortiaParticipations(result.entries, selectedFields, result.institution, contactSwitch, ExportClickMeService.FORMAT.CSV))
            }
            out.close()
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
                    [ctx: result.institution,
                     roleType: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS,
                     email: RDStore.CCT_EMAIL])
            persons.each {  personRow ->
                Person person = (Person) personRow[1]
                PersonRole pr = person.roleLinks.find{ PersonRole p -> p.org != result.institution}
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
                           message(code: 'subscription.referenceYear.export.label'), message(code:'subscription.isPublicForApi.label'),message(code:'subscription.hasPerpetualAccess.label'),
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
            response.setHeader("Content-disposition","attachment; filename=\"${filename}\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
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
                                   message(code: 'subscription.referenceYear.export.label'), message(code: 'subscription.isPublicForApi.label'), message(code: 'subscription.hasPerpetualAccess.label'),
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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def manageParticipantSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        Profiler prf = new Profiler()
        prf.setBenchmark('filterService')

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        DateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()

        result.participant = Org.get(Long.parseLong(params.id))

        params.tab = params.tab ?: 'new'

        if(params.tab != 'new'){
            params.sort = 'surInfo.endDate DESC, LOWER(surInfo.name)'
        }

        /*if (params.validOnYear == null || params.validOnYear == '') {
            def sdfyear = DateUtils.getSDF_yyyy()
            params.validOnYear = sdfyear.format(new Date())
        }*/

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate)", [org: result.participant]) ?: []

        params.consortiaOrg = result.institution

        Map<String, Object> fsq = filterService.getParticipantSurveyQuery_New(params, sdFormat, result.participant)

        result.surveyResults = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)

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
            result.countSurveys = surveyService._getSurveyParticipantCounts_New(result.participant, params)

            result
        }
    }

    /**
     * Manages calls about the property groups the context institution defined. With a given parameter,
     * editing may be done on the given property group
     * @return in every case, the list of property groups; the list may be exported as Excel with the usage data as well, then, an Excel worksheet is being returned
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
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
                result.createOrUpdate = message(code:'default.button.save.label')
                render template: '/templates/properties/propertyGroupModal', model: result
                return
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
                                propDefGroup = new PropertyDefinitionGroup(
                                        name: params.name,
                                        description: params.description,
                                        tenant: result.institution,
                                        ownerType: ownerType,
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

                            params.list('propertyDefinition')?.each { pd ->

                                new PropertyDefinitionGroupItem(
                                        propDef: pd,
                                        propDefGroup: propDefGroup
                                ).save()
                            }
                        }
                    }
                }
                break
        }

        Set<PropertyDefinitionGroup> unorderedPdgs = PropertyDefinitionGroup.findAllByTenant(result.institution, [sort: 'name'])
        result.propDefGroups = [:]
        PropertyDefinition.AVAILABLE_GROUPS_DESCR.each { String propDefGroupType ->
            result.propDefGroups.put(propDefGroupType,unorderedPdgs.findAll { PropertyDefinitionGroup pdg -> pdg.ownerType == PropertyDefinition.getDescrClass(propDefGroupType)})
        }

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
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
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

        result.availableDescrs = [PropertyDefinition.SUB_PROP,PropertyDefinition.LIC_PROP,PropertyDefinition.PRS_PROP,PropertyDefinition.PLA_PROP,PropertyDefinition.ORG_PROP]

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Set<PropertyDefinition> propList = []
        if(params.descr) {
           propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr = :descr and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                    [ctx:result.institution, descr: params.descr])
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

        //Set<Subscription> validSubChildren = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.sub.instanceOf = :parent order by oo.org.sortname asc",[parent:result.parentSub])
        /*Sortieren
        result.validSubChilds = validSubChilds.sort { Subscription a, Subscription b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }*/
        //result.validSubChilds = validSubChildren

        if(propDef) {
            result.putAll(propertyService.getAvailableProperties(propDef, result.institution, params))
            result.countObjWithoutProp = result.withoutProp.size()
            result.countObjWithProp = result.withProp.size()
            result.withoutProp.eachWithIndex { obj, int i ->
                if(i >= result.withoutPropOffset && i < result.withoutPropOffset+result.max)
                    result.objectsWithoutProp << propertyService.processObjects(obj,result.institution,propDef)
            }
            result.withProp.eachWithIndex { obj, int i ->
                if(i >= result.withPropOffset && i < result.withPropOffset+result.max)
                    result.filteredObjs << propertyService.processObjects(obj,result.institution,propDef)
            }
            result.filterPropDef = propDef
        }

        /*
        def oldID = params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { Subscription sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID*/
        //prepare next pagination
        params.withoutPropOffset = result.withoutPropOffset
        params.withPropOffset = result.withPropOffset

        result
    }

    /**
     * Call to process a bulk assign of a property definition to a given set of objects
     * @return the updated view with the assigned property definitions
     */
    @DebugInfo(hasPermAsInstEditor_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
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
                    def owner = resolveOwner(pd, id)
                    if (owner) {
                        AbstractPropertyWithCalculatedLastUpdated prop = owner.propertySet.find { exProp -> exProp.type.id == pd.id && exProp.tenant.id == result.institution.id }
                        if (!prop || pd.multipleOccurrence) {
                            prop = PropertyDefinition.createGenericProperty(propertyType, owner, pd, result.institution)
                            if (propertyService.setPropValue(prop, params.filterPropValue)) {
                                if (id in withAudit) {
                                    owner.getClass().findAllByInstanceOf(owner).each { member ->
                                        AbstractPropertyWithCalculatedLastUpdated memberProp = PropertyDefinition.createGenericProperty(propertyType, member, prop.type, result.institution)
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
                    processDeleteProperties(pd, selectedObjects, result.institution)
                }
                else {
                    params.list("selectedObjects").each { String id ->
                        def owner = resolveOwner(pd, id)
                        if (owner) {
                            AbstractPropertyWithCalculatedLastUpdated prop = owner.propertySet.find { exProp -> exProp.type.id == pd.id && exProp.tenant.id == result.institution.id }
                            if (prop) {
                                propertyService.setPropValue(prop, params.filterPropValue)
                            }
                        }
                    }
                }
            }
            redirect action: 'manageProperties', params: [filterPropDef: params.filterPropDef]
            return
        }
    }

    /**
     * Call to remove the given property definition from the given objects
     * @param propDef the property definition to remove
     * @param selectedObjects the objects from which the property should be unassigned
     * @param contextOrg the institution whose properties should be removed
     */
    def processDeleteProperties(PropertyDefinition propDef, selectedObjects, Org contextOrg) {
        PropertyDefinition.withTransaction {
            int deletedProperties = 0
            selectedObjects.each { ownerId ->
                def owner = resolveOwner(propDef, ownerId)
                Set<AbstractPropertyWithCalculatedLastUpdated> existingProps = owner.propertySet.findAll {
                    it.owner.id == owner.id && it.type.id == propDef.id && it.tenant?.id == contextOrg.id && !AuditConfig.getConfig(it)
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
    def resolveOwner(PropertyDefinition pd, String id) {
        def owner
        switch(pd.descr) {
            case PropertyDefinition.SUB_PROP: owner = Subscription.get(id)
                break
            case PropertyDefinition.LIC_PROP: owner = License.get(id)
                break
            case PropertyDefinition.ORG_PROP: owner = Org.get(id)
                break
            case PropertyDefinition.PRS_PROP: owner = Person.get(id)
                break
            case PropertyDefinition.PLA_PROP: owner = Platform.get(id)
                break
        }
        owner
    }

    /**
     * Displays and manages private property definitions for this institution.
     * If the add command is specified (i.e. params.cmd is set), this method inserts a new private property definition;
     * usage is restricted to the context institution.
     * To add a custom property definition (which is usable for every institution), the route is {@link de.laser.ajax.AjaxController#addCustomPropertyType()}
     * (but consider the annotation there!)
     */
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def managePrivatePropertyDefinitions() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        switch(params.cmd) {
            case 'add':List rl = propertyService.addPrivatePropertyDefinition(params)
                flash."${rl[0]}" = rl[1]
                if(rl[2])
                    result.desc = rl[2]
                break
            case 'toggleMandatory':
                PropertyDefinition.withTransaction { TransactionStatus ts ->
                    PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.pd)
                    pd.mandatory = !pd.mandatory
                    pd.save()
                }
                break
            case 'toggleMultipleOccurrence':
                PropertyDefinition.withTransaction { TransactionStatus ts ->
                    PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.pd)
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
                            int count = propertyService.replacePropertyDefinitions(pdFrom, pdTo, Boolean.valueOf(params.overwrite), false)
                            flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [count, oldName, newName]) as String
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

        Map<String, Set<PropertyDefinition>> propDefs = [:]
        Set<String> availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR
        if (result.institution.isCustomerType_Inst_Pro())
            availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR-PropertyDefinition.SVY_PROP
        availablePrivDescs.each { String it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, result.institution, [sort: 'name_'+result.languageSuffix]) // ONLY private properties!
            propDefs[it] = itResult
        }

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
    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    Object managePropertyDefinitions() {
        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if(params.xcgPdTo) {
            PropertyDefinition pdFrom = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdFrom)
            PropertyDefinition pdTo = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdTo)
            String oldName = pdFrom.tenant ? "${pdFrom.getI10n("name")} (priv.)" : pdFrom.getI10n("name")
            String newName = pdTo.tenant ? "${pdTo.getI10n("name")} (priv.)" : pdTo.getI10n("name")
            if (pdFrom && pdTo) {
                try {
                    int count = propertyService.replacePropertyDefinitions(pdFrom, pdTo, params.overwrite == 'on', false)
                    flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [count, oldName, newName])
                }
                catch (Exception e) {
                    e.printStackTrace()
                    flash.error = message(code: 'menu.institutions.replace_prop.error', args: [oldName, newName])
                }
            }
        }
                //PropertyDefinition.withTransaction { TransactionStatus ts ->
                    switch(params.cmd) {
                        /*
                        case 'toggleMandatory': pd.mandatory = !pd.mandatory
                            pd.save()
                            break
                        case 'toggleMultipleOccurrence': pd.multipleOccurrence = !pd.multipleOccurrence
                            pd.save()
                            break
                         */
                        case 'replacePropertyDefinition':
                            break
                            /*
                        case 'deletePropertyDefinition':
                            if (! pd.isHardData) {
                                try {
                                    pd.delete()
                                    flash.message = message(code:'propertyDefinition.delete.success',[pd.getI10n('name')])
                                }
                                catch(Exception e) {
                                    flash.error = message(code:'propertyDefinition.delete.failure.default',[pd.getI10n('name')])
                                }
                            }
                            break
                        */
                    }
                //}

        result.languageSuffix = LocaleUtils.getCurrentLang()

        Map<String,Set<PropertyDefinition>> propDefs = [:]
        PropertyDefinition.AVAILABLE_CUSTOM_DESCR.each { it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: 'name_'+result.languageSuffix]) // NO private properties!
            propDefs[it] = itResult
        }

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
    private _deletePrivatePropertyDefinition(params) {
        PropertyDefinition.withTransaction {
            log.debug("delete private property definition for institution: " + params)

            String messages = ""
            Org tenant = contextService.getOrg()
            def deleteIds = params.list('deleteIds')

            deleteIds.each { did ->
                Long id = Long.parseLong(did)
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
    @DebugInfo(isInstUser_or_ROLEADMIN = true)
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def copyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if(params.id)
        {
            License license = License.get(params.id)
            boolean isEditable = license.isEditableBy(result.user)

            if (! (userService.hasFormalAffiliation(result.user, result.institution, 'INST_EDITOR'))) {
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

    @DebugInfo(hasPermAsInstUser_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.contextService.hasPermAsInstUser_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def currentSubscriptionsTransfer() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.putAll(subscriptionService.getMySubscriptionTransfer(params,result.user,result.institution))

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
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, result.institution, ExportClickMeService.FORMAT.XLS, true)
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
                //writer.write((String) _exportcurrentSubscription(result.allSubscriptions,"csv", result.institution))
                writer.write((String) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, result.institution, ExportClickMeService.FORMAT.CSV,  true))
            }
            out.close()
        }

        result
    }


}
