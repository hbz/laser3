package de.laser

import com.k_int.kbplus.DocstoreService
import com.k_int.kbplus.ExportService
import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.InstitutionsService
import de.laser.annotations.DebugAnnotation
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.ctrl.UserControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.finance.PriceItem
import com.k_int.kbplus.PendingChangeService
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
import de.laser.storage.BeanStorage
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.workflow.WfWorkflow
import de.laser.workflow.WfWorkflowPrototype
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.collections.BidiMap
import org.apache.commons.collections.bidimap.DualHashBidiMap
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
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate

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

    /**
     * The landing page after login; this is also the call when the home button is clicked
     * @return the {@link #dashboard()} view
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def index() {
        redirect(action:'dashboard')
    }

    /**
     * Call for the reporting module
     * @return the reporting entry view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
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

            PageRenderer groovyPageRenderer = BeanStorage.getGroovyPageRenderer()
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
		ProfilerUtils pu = new ProfilerUtils()
		pu.setBenchmark('init')

        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        pu.setBenchmark("before loading subscription ids")

        String instanceFilter = ""
        Map<String, Object> subscriptionParams = [contextOrg:result.contextOrg, roleTypes:[RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA], current:RDStore.SUBSCRIPTION_CURRENT, expired:RDStore.SUBSCRIPTION_EXPIRED]
        if(result.contextOrg.getCustomerType() == "ORG_CONSORTIUM")
            instanceFilter += " and s.instanceOf = null "
        Set<Long> idsCurrentSubscriptions = Subscription.executeQuery('select s.id from OrgRole oo join oo.sub s where oo.org = :contextOrg and oo.roleType in (:roleTypes) and (s.status = :current or (s.status = :expired and s.hasPerpetualAccess = true))'+instanceFilter,subscriptionParams)


        result.subscriptionMap = [:]
        result.platformInstanceList = []

        if (idsCurrentSubscriptions) {
            String qry3 = "select distinct p, s, ${params.sort ?: 'p.normname'} from SubscriptionPackage subPkg join subPkg.subscription s join subPkg.pkg pkg, " +
                    "TitleInstancePackagePlatform tipp join tipp.platform p left join p.org o " +
                    "where tipp.pkg = pkg and s.id in (:subIds) and p.gokbId in (:wekbIds)"

            qry3 += " and ((pkg.packageStatus is null) or (pkg.packageStatus != :pkgDeleted))"
            qry3 += " and ((tipp.status is null) or (tipp.status != :tippDeleted))"

            def qryParams3 = [
                    subIds         : idsCurrentSubscriptions,
                    pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED,
                    tippDeleted    : RDStore.TIPP_STATUS_DELETED
            ]

            String esQuery = "?componentType=Platform"

            if (params.q?.length() > 0) {
                result.filterSet = true
                esQuery += "&q=${params.q}"
                qry3 += "and ("
                qry3 += "   genfunc_filter_matcher(o.name, :query) = true"
                qry3 += "   or genfunc_filter_matcher(o.sortname, :query) = true"
                qry3 += "   or genfunc_filter_matcher(o.shortname, :query) = true "
                qry3 += ")"
                qryParams3.put('query', "${params.q}")
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
            List wekbIds = gokbService.doQuery([max:10000, offset:0], params.clone(), esQuery).records.collect { Map hit -> hit.uuid }

            qryParams3.wekbIds = wekbIds

            qry3 += " group by p, s"
            if(params.sort)
                qry3 += " order by ${params.sort} ${params.order}"
            else qry3 += " order by p.normname asc"

            pu.setBenchmark("before loading platforms")
            List platformSubscriptionList = []
            if(wekbIds)
                platformSubscriptionList.addAll(Platform.executeQuery(qry3, qryParams3))

            log.debug("found ${platformSubscriptionList.size()} in list ..")
            /*, [max:result.max, offset:result.offset])) */
            pu.setBenchmark("before platform subscription list loop")
            platformSubscriptionList.each { entry ->
                Platform pl = (Platform) entry[0]
                Subscription s = (Subscription) entry[1]

                String key = 'platform_' + pl.id

                if (! result.subscriptionMap.containsKey(key)) {
                    result.subscriptionMap.put(key, [])
                    result.platformInstanceList.add(pl)
                }

                if (s.status.value == RDStore.SUBSCRIPTION_CURRENT.value) {
                    result.subscriptionMap.get(key).add(s)
                }
            }
        }
        result.platformInstanceTotal    = result.platformInstanceList.size()

        result.cachedContent = true

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        result
    }

    /**
     * Returns a view of the current licenses (and subscriptions linked to them) the institution holds. The results may be filtered
     * both for subscriptions and licenses. That means that a subscription filter is attached to the
     * query; licenses without a subscription may get lost if there is no subscription linked to it!
     * @return the license list view
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def currentLicenses() {

        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        EhcacheWrapper cache = contextService.getCache("/license/filter/",ContextService.USER_SCOPE)
        if(cache && cache.get('licenseFilterCache')) {
            if(!params.resetFilter && !params.filterSet)
                params.putAll((GrailsParameterMap) cache.get('licenseFilterCache'))
            else params.remove('resetFilter')
            cache.remove('licenseFilterCache') //has to be executed in any case in order to enable cache updating
        }
		ProfilerUtils pu = new ProfilerUtils()
		pu.setBenchmark('init')

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        def date_restriction = null
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

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

        if (accessService.checkPerm("ORG_INST")) {
            base_qry = "from License as l where ( exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) )"
            qry_params = [roleType1:RDStore.OR_LICENSEE, roleType2:RDStore.OR_LICENSEE_CONS, lic_org:result.institution]
            if(result.editable)
                licenseFilterTable << "action"
            licenseFilterTable << "licensingConsortium"
        }
        else if (accessService.checkPerm("ORG_CONSORTIUM")) {
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
            def psq = propertyService.evalFilterQuery(params, base_qry, 'l', qry_params)
            base_qry = psq.query
            qry_params = psq.queryParams
        }

        if(params.licensor) {
            base_qry += " and ( exists ( select o from l.orgRelations as o where o.roleType = :licCons and o.org.id in (:licensors) ) ) "
            List<Long> licensors = []
            List<String> selLicensors = params.list('licensor')
            selLicensors.each { String sel ->
                licensors << Long.parseLong(sel)
            }
            qry_params += [licCons:RDStore.OR_LICENSOR,licensors:licensors]
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
                    " or genfunc_filter_matcher(orgR.org.shortname, :name_filter) = true "+
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

            if(accessService.checkPerm("ORG_CONSORTIUM")) {
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

        //log.debug("query = ${base_qry}");
        //log.debug("params = ${qry_params}");
        pu.setBenchmark('execute query')
        log.debug("select l ${base_qry}")
        List<License> totalLicenses = License.executeQuery( "select l " + base_qry, qry_params )
        result.licenseCount = totalLicenses.size()
        pu.setBenchmark('get subscriptions')

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
        pu.setBenchmark('get consortia')
        Set<Org> consortia = Org.executeQuery("select os.org from OrgSetting os where os.key = 'CUSTOMER_TYPE' and os.roleValue in (select r from Role r where authority = 'ORG_CONSORTIUM') order by os.org.name asc")
        pu.setBenchmark('get licensors')
        Set<Org> licensors = orgTypeService.getOrgsForTypeLicensor()
        Map<String,Set<Org>> orgs = [consortia:consortia,licensors:licensors]
        result.orgs = orgs

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        SimpleDateFormat sdfNoPoint = DateUtils.getSDF_NoTimeNoPoint()
        String filename = "${sdfNoPoint.format(new Date())}_${g.message(code: 'export.my.currentLicenses')}"
        List titles = [
                g.message(code:'license.details.reference'),
                g.message(code:'license.details.linked_subs'),
                g.message(code:'consortium'),
                g.message(code:'license.licensor.label'),
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
                log.debug("now processing ${row[0]}:${row[1]}")
                objectNames.put(row[0],row[1])
            }
        }
        if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            List rows = []
            totalLicenses.each { License licObj ->
                License license = (License) licObj
                List row = [[field:license.reference.replaceAll(',',' '),style:'bold']]
                List linkedSubs = license.subscriptions.collect { sub ->
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
        withFormat {
            html result
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                List rows = []
                totalLicenses.each { licObj ->
                    License license = (License) licObj
                    List row = [license.reference.replaceAll(',',' ')]
                    List linkedSubs = license.subscriptions.collect { sub ->
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
    }

    /**
     * Call to create a new license
     * @return the form view to enter the new license parameters
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def emptyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            return;
        }

        def cal = new java.util.GregorianCalendar()
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        cal.setTimeInMillis(System.currentTimeMillis())
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        result.defaultStartYear = sdf.format(cal.getTime())

        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)

        result.defaultEndYear = sdf.format(cal.getTime())

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

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
    @DebugAnnotation(test='hasAffiliation("INST_USER")', wtc = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def processEmptyLicense() {
        License.withTransaction { TransactionStatus ts ->
            User user = contextService.getUser()
            Org org = contextService.getOrg()

            Set<RefdataValue> defaultOrgRoleType = []
            if(accessService.checkPerm("ORG_CONSORTIUM"))
                defaultOrgRoleType << RDStore.OT_CONSORTIUM.id.toString()
            else defaultOrgRoleType << RDStore.OT_INSTITUTION.id.toString()

            params.asOrgType = params.asOrgType ? [params.asOrgType] : defaultOrgRoleType


            if (! accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR')) {
                flash.error = message(code:'myinst.error.noAdmin', args:[org.name])
                response.sendError(401)
                // render(status: '401', text:"You do not have permission to access ${org.name}. Please request access on the profile page");
                return
            }
            def baseLicense = params.baselicense ? License.get(params.baselicense) : null
            //Nur wenn von Vorlage ist
            if (baseLicense) {
                if (!baseLicense?.hasPerm("view", user)) {
                    log.debug("return 401....")
                    flash.error = message(code: 'myinst.newLicense.error')
                    response.sendError(401)
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
                            flash.message = message(code: 'license.createdfromTemplate.message')
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
                flash.error = message(code:'license.create.error')
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
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def currentProviders() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
		ProfilerUtils pu = new ProfilerUtils()
		pu.setBenchmark('init')

        EhcacheWrapper cache = contextService.getCache('MyInstitutionController/currentProviders', contextService.ORG_SCOPE)
        List orgIds = []

        if (cache.get('orgIds')) {
            orgIds = cache.get('orgIds')
            log.debug('orgIds from cache')
        }
        else {

            List<Org> matches = Org.executeQuery("""
select distinct(or_pa.org) from OrgRole or_pa 
join or_pa.sub sub 
join sub.orgRelations or_sub where
    ( sub = or_sub.sub and or_sub.org = :subOrg ) and
    ( or_sub.roleType in (:subRoleTypes) ) and
        ( or_pa.roleType in (:paRoleTypes) )
""", [
        subOrg:       result.institution,
        subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA],
        paRoleTypes:  [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
    ])
            orgIds = matches.collect{ it.id }
            cache.put('orgIds', orgIds)
        }

        result.orgRoles    = [RDStore.OR_PROVIDER, RDStore.OR_AGENCY]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.sort = params.sort ?: " LOWER(o.shortname), LOWER(o.name)"
        params.subPerpetual = 'on'

        GrailsParameterMap tmpParams = (GrailsParameterMap) params.clone()
        tmpParams.constraint_orgIds = orgIds
        def fsq  = filterService.getOrgQuery(tmpParams)

        result.filterSet = params.filterSet ? true : false
        if (params.filterPropDef) {
            fsq = propertyService.evalFilterQuery(tmpParams, fsq.query, 'o', fsq.queryParams)
        }
        List orgListTotal = Org.findAll(fsq.query, fsq.queryParams)
        result.orgListTotal = orgListTotal.size()
        result.orgList = orgListTotal.drop((int) result.offset).take((int) result.max)

        def message = g.message(code: 'export.my.currentProviders')
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        result.cachedContent = true

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        if ( params.exportXLS ) {
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
                response.sendError(500)
                return
            }
        }
        else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename = params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(orgListTotal, selectedFields, 'provider')

            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
        }
        withFormat {
            html {
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) organisationService.exportOrg(orgListTotal,message,true,"csv"))
                }
                out.close()
            }
        }
    }

    /**
     * Retrieves the list of subscriptions the context institution currently holds. The query may be restricted by filter parameters.
     * Default filter setting is status: current or with perpetual access
     * @return a (filtered) list of subscriptions, either as direct html output or as export stream (CSV, Excel)
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def currentSubscriptions() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

		ProfilerUtils pu = new ProfilerUtils()
		//pu.setBenchmark('init')
        result.tableConfig = ['showActions','showLicense']
        result.putAll(subscriptionService.getMySubscriptions(params,result.user,result.institution))

        result.compare = params.compare ?: ''

        // Write the output to a file
        SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String filename = "${datetoday}_" + g.message(code: "export.my.currentSubscriptions")

		//List bm = pu.stopBenchmark()
		//result.benchMark = bm

        if ( params.exportXLS ) {

            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) exportcurrentSubscription(result.allSubscriptions, "xls", result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        }else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptions(result.allSubscriptions, selectedFields, result.institution)

            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
        }

        withFormat {
            html {
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportcurrentSubscription(result.allSubscriptions,"csv", result.institution))
                }
                out.close()
            }
        }
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
    private def exportcurrentSubscription(List<Subscription> subscriptions, String format,Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
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
                       g.message(code: 'default.identifiers.label'),
                       g.message(code: 'default.status.label'),
                       g.message(code: 'subscription.kind.label'),
                       g.message(code: 'subscription.form.label'),
                       g.message(code: 'subscription.resource.label'),
                       g.message(code: 'subscription.isPublicForApi.label'),
                       g.message(code: 'subscription.hasPerpetualAccess.label'),
                       g.message(code: 'subscription.hasPublishComponent.label')]
        boolean asCons = false
        if(accessService.checkPerm('ORG_CONSORTIUM')) {
            asCons = true
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
                log.debug("now processing ${row[0]}:${row[1]}")
                objectNames.put(row[0],row[1])
            }
        }
        List subscriptionData = []
        subscriptions.each { Subscription sub ->
            List row = []
            TreeSet subProviders = sub.orgRelations.findAll { OrgRole oo -> oo.roleType == RDStore.OR_PROVIDER }.collect { OrgRole oo -> oo.org.name }
            TreeSet subAgencies = sub.orgRelations.findAll { OrgRole oo -> oo.roleType == RDStore.OR_AGENCY }.collect { OrgRole oo -> oo.org.name }
            TreeSet subIdentifiers = sub.ids.collect { Identifier id -> "(${id.ns.ns}) ${id.value}" }
            switch (format) {
                case "xls":
                case "xlsx":
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
            case 'xls':
            case 'xlsx':
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def subscriptionsManagement() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        params.tab = params.tab ?: 'generalProperties'

        //Important
        if(accessService.checkPerm('ORG_CONSORTIUM')) {
            params.subTypes = [RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id.toString()]
        }else{
            params.subTypes = [RDStore.SUBSCRIPTION_TYPE_LOCAL.id.toString()]
        }

        if(params.tab == 'documents' && params.processOption == 'newDoc') {
            def input_file = request.getFile("upload_file")
            if (input_file.size == 0) {
                flash.error = message(code: 'template.emptyDocument.file')
                redirect(url: request.getHeader('referer'))
                return
            }
            params.original_filename = input_file.originalFilename
            params.mimeType = input_file.contentType
            result << managementService.subscriptionsManagement(this, params, input_file)
        }else{
            result << managementService.subscriptionsManagement(this, params)
        }

        result

    }

    /**
     * Connects the context subscription with the given pair
     * @return void, redirects to referer
     */
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
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
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    Map documents() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result
    }

    /**
     * Call to delete a given document
     * @return the document table view ({@link #documents()})
     * @see com.k_int.kbplus.DocstoreService#unifiedDeleteDocuments()
     */
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def deleteDocuments() {
        def ctxlist = []

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'myInstitution', action: 'documents' /*, fragment: 'docstab' */
    }

    /**
     * Opens a list of current issue entitlements hold by the context institution. The result may be filtered;
     * filters hold for the title, the platforms, provider and subscription parameters
     * @return a (filtered) list of issue entitlements
     * @see Subscription
     * @see Platform
     * @see IssueEntitlement
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def currentTitles() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
		ProfilerUtils pu = new ProfilerUtils()
		pu.setBenchmark('init')

        Set<RefdataValue> orgRoles = []
        String instanceFilter = ""
        List<String> queryFilter = []

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
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

        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
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
                deleted: RDStore.TIPP_STATUS_DELETED,
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
        if (params.order == 'desc') {
            orderByClause = 'order by tipp.sortname desc, tipp.name desc'
        } else {
            orderByClause = 'order by tipp.sortname asc, tipp.name asc'
        }

        String qryString = "select ie.id from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join sub.orgRelations oo where ie.status != :deleted and sub.status = :current and oo.roleType in (:orgRoles) and oo.org = :institution "
        if(queryFilter)
            qryString += ' and '+queryFilter.join(' and ')

        Set<Long> currentIssueEntitlements = IssueEntitlement.executeQuery(qryString+' group by tipp, ie.id order by ie.sortname asc',qryParams)
        //second filter needed because double-join on same table does deliver me empty results
        if (filterPvd) {
            currentIssueEntitlements = IssueEntitlement.executeQuery("select ie.id from IssueEntitlement ie join ie.tipp tipp join tipp.orgs oo where oo.roleType in (:cpRole) and oo.org.id in ("+filterPvd.join(", ")+") order by ie.sortname asc",[cpRole:[RDStore.OR_CONTENT_PROVIDER,RDStore.OR_PROVIDER,RDStore.OR_AGENCY,RDStore.OR_PUBLISHER]])
        }
        Set<TitleInstancePackagePlatform> allTitles = currentIssueEntitlements ? TitleInstancePackagePlatform.executeQuery('select tipp from IssueEntitlement ie join ie.tipp tipp where ie.id in (:ids) '+orderByClause,[ids:currentIssueEntitlements.drop(result.offset).take(result.max)]) : []
        result.subscriptions = Subscription.executeQuery('select sub from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.roleType in (:orgRoles) and oo.org = :institution and sub.status = :current '+instanceFilter+" order by sub.name asc",[
                institution: result.institution,
                current: RDStore.SUBSCRIPTION_CURRENT,
                orgRoles: orgRoles]).toSet()
        if(result.subscriptions.size() > 0) {
            Set<Long> allIssueEntitlements = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription in (:currentSubs)',[currentSubs:result.subscriptions])
            result.providers = Org.executeQuery('select org.id,org.name from IssueEntitlement ie join ie.tipp tipp join tipp.orgs oo join oo.org org where ie.id in ('+qryString+' group by tipp, ie.id order by ie.sortname asc) group by org.id order by org.name asc',qryParams)
            result.hostplatforms = Platform.executeQuery('select plat.id,plat.name from IssueEntitlement ie join ie.tipp tipp join tipp.platform plat where ie.id in ('+qryString+' group by tipp, ie.id order by ie.sortname asc) group by plat.id order by plat.name asc',qryParams)
        }
        result.num_ti_rows = currentIssueEntitlements.size()
        result.titles = allTitles

        result.filterSet = params.filterSet || defaultSet
        String filename = "${message(code:'export.my.currentTitles')}_${DateUtils.SDF_NoTimeNoPoint.format(new Date())}"

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        if(params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String,List> tableData = exportService.generateTitleExportKBART(currentIssueEntitlements, IssueEntitlement.class.name)
            out.withWriter { writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.columnData,'\t'))
            }
            out.flush()
            out.close()
        }
        else if(params.exportXLSX) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String,List> export = exportService.generateTitleExportCustom(currentIssueEntitlements, IssueEntitlement.class.name)
            Map sheetData = [:]
            sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else {
            withFormat {
                html {
                    result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                    response.contentType = "text/csv"

                    ServletOutputStream out = response.outputStream
                    Map<String,List> tableData = exportService.generateTitleExportCSV(currentIssueEntitlements, IssueEntitlement.class.name)
                    out.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.rows,'|'))
                    }
                    out.flush()
                    out.close()
                }
                /*json {
                    def map = [:]
                    exportService.addTitlesToMap(map, result.titles)
                    def content = map as JSON

                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.json\"")
                    response.contentType = "application/json"

                    render content
                }
                xml {
                    def doc = exportService.buildDocXML("TitleList")
                    exportService.addTitleListXML(doc, doc.getDocumentElement(), result.titles)

                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xml\"")
                    response.contentType = "text/xml"
                    exportService.streamOutXML(doc, response.outputStream)
                }
                */
            }
        }
    }

    /**
     * Opens a list of current packages subscribed by the context institution. The result may be filtered
     * by filter parameters
     * @return a list view of packages the institution has subscribed
     * @see SubscriptionPackage
     * @see Package
     */
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def currentPackages() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.contextOrg = contextService.getOrg()
        result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)
        result.languages = RefdataCategory.getAllRefdataValues(RDConstants.LANGUAGE_ISO)
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        //def cache = contextService.getCache('MyInstitutionController/currentPackages/', contextService.ORG_SCOPE)

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

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())
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

            def qryParams3 = [
                    currentSubIds  : currentSubIds,
                    pkgDeleted     : RDStore.PACKAGE_STATUS_DELETED
            ]

            if (params.pkg_q?.length() > 0) {
                qry3 += " and ("
                qry3 += "   genfunc_filter_matcher(pkg.name, :query) = true"
                qry3 += ")"
                qryParams3.put('query', "${params.pkg_q}")
            }

            if (params.ddc?.length() > 0) {
                qry3 += " and ((exists (select ddc.id from DeweyDecimalClassification ddc where ddc.ddc in (:ddcs) and ddc.tipp = tipp)) or (exists (select ddc.id from DeweyDecimalClassification ddc where ddc.ddc in (:ddcs) and ddc.pkg = pkg)))"
                qryParams3.put('ddcs', RefdataValue.findAllByIdInList(params.list("ddc").collect { String ddc -> Long.parseLong(ddc) }))
            }

            qry3 += " group by pkg, s"
            qry3 += " order by pkg.name " + (params.order ?: 'asc')
            log.debug("before query: ${System.currentTimeMillis()-start}")
            List packageSubscriptionList = Subscription.executeQuery(qry3, qryParams3)
            /*, [max:result.max, offset:result.offset])) */
            log.debug("after query: ${System.currentTimeMillis()-start}")
            packageSubscriptionList.eachWithIndex { entry, int i ->
                log.debug("processing entry ${i} at: ${System.currentTimeMillis()-start}")
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
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")', ctrlService = 2)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def modal_create() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to access ${result.institution.name} pages. Please request access on the profile page";
            response.sendError(401)
            return;
        }

        def preCon      = taskService.getPreconditions(result.institution)
        result << preCon

        render template: '/templates/tasks/modal_create', model: result
    }

    /**
     * Call for the list of entitlement changes of the last 600 days
     * @return a list of changes to be accepted or rejected
     * @see PendingChange
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def changes() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.acceptedOffset = 0
        def periodInDays = 600
        Map<String,Object> pendingChangeConfigMap = [contextOrg: result.institution, consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"), periodInDays:periodInDays, max:result.max, offset:result.acceptedOffset]

        result.putAll(pendingChangeService.getChanges_old(pendingChangeConfigMap))

        result
    }

    //@DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    //@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def announcements() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.itemsTimeWindow = 365
        result.recentAnnouncements = Doc.executeQuery(
                'select d from Doc d where d.type = :type and d.dateCreated >= :tsCheck order by d.dateCreated desc',
                [type: RDStore.DOC_TYPE_ANNOUNCEMENT, tsCheck: MigrationHelper.localDateToSqlDate( LocalDate.now().minusDays(365) )]
        )
        result.num_announcements = result.recentAnnouncements.size()

        result
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def changeLog() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        def exporting = ( params.format == 'csv' ? true : false )

        result.institutional_objects = []

        if ( exporting ) {
          result.max = 1000000;
          result.offset = 0;
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)
        }

        PendingChange.executeQuery('select distinct(pc.license) from PendingChange as pc where pc.owner = :owner', [owner: result.institution]).each {
          result.institutional_objects.add([License.class.name + ':' + it.id, "${message(code:'license.label')}: " + it.reference])
        }
        PendingChange.executeQuery('select distinct(pc.subscription) from PendingChange as pc where pc.owner = :owner', [owner: result.institution]).each {
          result.institutional_objects.add([Subscription.class.name + ':' + it.id, "${message(code:'subscription')}: " + it.name])
        }

        if ( params.restrict == 'ALL' )
          params.restrict=null

        String base_query = " from PendingChange as pc where owner = :o"
        Map qry_params = [o: result.institution]
        if ( ( params.restrict != null ) && ( params.restrict.trim().length() > 0 ) ) {
          def o =  genericOIDService.resolveOID(params.restrict)
          if ( o != null ) {
            if ( o instanceof License ) {
                base_query += ' and license = :l'
                qry_params.put('l', o)
            }
            else {
                base_query += ' and subscription = :s'
                qry_params.put('s', o)
            }
          }
        }

        result.num_changes = PendingChange.executeQuery("select pc.id "+base_query, qry_params).size()


        withFormat {
            html {
            result.changes = PendingChange.executeQuery("select pc "+base_query+"  order by ts desc", qry_params, [max: result.max, offset:result.offset])
                result
            }
            csv {
                SimpleDateFormat dateFormat = DateUtils.getSDF_NoTime()
                def changes = PendingChange.executeQuery("select pc "+base_query+"  order by ts desc", qry_params)
                response.setHeader("Content-disposition", "attachment; filename=\"${escapeService.escapeString(result.institution.name)}_changes.csv\"")
                response.contentType = "text/csv"

                def out = response.outputStream
                out.withWriter { w ->
                  w.write('Date,ChangeId,Actor, SubscriptionId,LicenseId,Description\n')
                  changes.each { c ->
                    def line = "\"${dateFormat.format(c.ts)}\",\"${c.id}\",\"${c.user?.displayName?:''}\",\"${c.subscription?.id ?:''}\",\"${c.license?.id?:''}\",\"${c.desc}\"\n".toString()
                    w.write(line)
                  }
                }
                out.close()
            }

        }
    }

    /**
     * Call for the finance import starting page; the mappings are being explained here and an example sheet for submitting data to import
     * @return the finance import entry view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def financeImport() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.mappingCols = ["title","element","elementSign","referenceCodes","budgetCode","status","invoiceTotal",
                              "currency","exchangeRate","taxType","taxRate","value","subscription","package",
                              "issueEntitlement","datePaid","financialYear","dateFrom","dateTo","invoiceDate",
                              "description","invoiceNumber","orderNumber"/*,"institution"*/]
        result
    }

    /**
     * Generates a customised work sheet for import cost items for the given subscriptions
     * @return a CSV template with subscription OIDs and empty data
     * @see Subscription
     * @see CostItem
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", wtc = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
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
                    flash.error = message(code:'default.import.error.wrongCharset',args:[encoding])
                    redirect(url: request.getHeader('referer'))
                }
            }
            else {
                flash.error = message(code:'default.import.error.noFileProvided')
                redirect(url: request.getHeader('referer'))
            }
        }
    }

    /**
     * Call for the subscription import starting page; the mappings are being explained here and an example sheet for submitting data to import
     * @return the subscription import entry view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def subscriptionImport() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.mappingCols = ["name", "owner", "status", "type", "form", "resource", "provider", "agency", "startDate", "endDate",
                              "manualCancellationDate", "hasPerpetualAccess", "hasPublishComponent", "isPublicForApi",
                              "customProperties", "privateProperties", "notes"]
        result
    }

    /**
     * Reads off data from the uploaded import sheet and prepares data for import. The user may check after the
     * processing whether the imported data is read correctly or not
     * @return the control view with the import preparation result
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", wtc = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
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
                    flash.error = message(code:'default.import.error.wrongCharset',args:[encoding])
                    redirect(url: request.getHeader('referer'))
                }
            }
            else {
                flash.error = message(code:'default.import.error.noFileProvided')
                redirect(url: request.getHeader('referer'))
            }
    }

    /**
     * Opens a list of current surveys concerning the context institution. The list may be filtered by filter parameters.
     * Default filter setting is current year
     * @return a (filtered) list of surveys, either displayed as html or returned as Excel worksheet
     */
    @DebugAnnotation(perm="ORG_BASIC_MEMBER", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER", "INST_USER", "ROLE_ADMIN")
    })
    def currentSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate)", [org: result.institution]) ?: []

        //SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.tab = params.tab ?: 'new'

        if(params.tab != 'new'){
            params.sort = 'surInfo.endDate DESC, LOWER(surInfo.name)'
        }

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSimpleDateFormatByToken('default.date.format.onlyYear')
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears << newYear
            }
            params.validOnYear = [newYear]
        }

        result.propList = PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList order by pd.name_de asc", [defList: [PropertyDefinition.SVY_PROP]])


        result.allConsortia = Org.executeQuery(
                """select o from Org o, SurveyInfo surInfo where surInfo.owner = o
                        group by o order by lower(o.name) """
        )

        Set orgIds = orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( contextService.getOrg() )

        result.providers = orgIds.isEmpty() ? [] : Org.findAllByIdInList(orgIds).sort { it?.name }

        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIBER_CONS, 'activeInst': result.institution])

        SimpleDateFormat sdFormat = DateUtils.getSDF_NoTime()


        def fsq = filterService.getParticipantSurveyQuery_New(params, sdFormat, result.institution)

        result.surveyResults = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)

        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            List surveyConfigsforExport = result.surveyResults.collect {it[1]}
            if ( params.surveyCostItems ) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "surveyCostItems.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems(surveyConfigsforExport, result.institution)
            }else {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
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
            result.countSurveys = surveyService.getSurveyParticipantCounts_New(result.institution, params)

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
    @DebugAnnotation(perm="ORG_BASIC_MEMBER", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER", "INST_USER", "ROLE_ADMIN")
    })
    def surveyInfos() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.contextOrg = contextService.getOrg()

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(Long.parseLong(params.surveyConfigID.toString())) : result.surveyInfo.surveyConfigs[0]

        result.surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(result.institution, result.surveyConfig).sort { it.surveyConfig.configOrder }

        result.ownerId = result.surveyInfo.owner?.id

        if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.institution)
            result.authorizedOrgs = result.user.authorizedOrgs
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

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
                //cost items
                //params.forExport = true
                LinkedHashMap costItems = result.subscription ? financeService.getCostItemsForSubscription(params, result) : null
                if (costItems?.subscr) {
                    result.costItemSums.subscrCosts = costItems.subscr.costItems
                }
		        result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
            }

            if(result.surveyConfig.subSurveyUseForTransfer) {
                result.successorSubscription = result.surveyConfig.subscription._getCalculatedSuccessor()

                result.customProperties = result.successorSubscription ? comparisonService.comparePropertiesWithAudit(result.surveyConfig.subscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))} + result.successorSubscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}, true, true) : null
            }

            if (result.subscription && result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                result.previousSubscription = result.subscription._getCalculatedPrevious()

                /*result.previousIesListPriceSum = 0
                if(result.previousSubscription){
                    result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                            'where p.listPrice is not null and ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus',
                    [sub: result.previousSubscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                }*/

                result.iesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                        'where p.listPrice is not null and ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus',
                        [sub: result.subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0


               /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                        'where p.listPrice is not null and ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus',
                        [sub: result.subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */

                result.countSelectedIEs = subscriptionService.countIssueEntitlementsNotFixed(result.subscription)
                result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countIssueEntitlementsFixed(result.previousSubscription) : 0) + subscriptionService.countIssueEntitlementsFixed(result.subscription)

                result.subscriber = result.subscription.getSubscriber()
            }

        }

        if ( params.exportXLSX ) {
            SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
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

    @Deprecated
    @DebugAnnotation(perm="ORG_BASIC_MEMBER", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER", "INST_USER", "ROLE_ADMIN")
    })
    def surveyInfosIssueEntitlements() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        /*result.surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(result.institution, result.surveyConfig).sort { it.surveyConfig.configOrder }

        result.subscription = result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.institution)

        result.ies = subscriptionService.getIssueEntitlementsNotFixed(result.subscription)
        result.iesListPriceSum = 0.0
        result.ies.each{ IssueEntitlement ie ->
            Double priceSum = 0.0

            ie.priceItems.each { PriceItem priceItem ->
                priceSum = priceItem.listPrice ?: 0.0
            }
            result.iesListPriceSum = result.iesListPriceSum + priceSum
        }


        result.iesFix = subscriptionService.getIssueEntitlementsFixed(result.subscription)
        result.iesFixListPriceSum = 0.0
        result.iesFix.each{ IssueEntitlement ie ->
            Double priceSum = 0.0

            ie.priceItems.each { PriceItem priceItem ->
                priceSum = priceItem.listPrice ?: 0.0
            }
            result.iesFixListPriceSum = result.iesListPriceSum + priceSum
        }


        result.ownerId = result.surveyConfig.surveyInfo.owner?.id ?: null

        if(result.subscription) {
            result.authorizedOrgs = result.user?.authorizedOrgs
            result.contextOrg = contextService.getOrg()
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.subscription.orgRelations.each { OrgRole or ->
                if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it.org.sortname }
	        result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
        }
        result*/

        redirect(action: 'surveyInfos', id: result.surveyInfo.id, params:[surveyConfigID: result.surveyConfig.id])
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
    @DebugAnnotation(perm="ORG_BASIC_MEMBER", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyInfoFinish() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyInfo surveyInfo = SurveyInfo.get(params.id)
        SurveyConfig surveyConfig = SurveyConfig.get(params.surveyConfigID)
        boolean sendMailToSurveyOwner = false

        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.institution, surveyConfig)

        IssueEntitlement.withTransaction { TransactionStatus ts ->
            if(surveyConfig && surveyConfig.pickAndChoose){

                def ies = subscriptionService.getIssueEntitlementsUnderConsideration(surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.institution))
                ies.each { ie ->
                    ie.acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION
                    ie.save()
                }

                /*if(ies.size() > 0) {*/

                if (surveyOrg && surveyConfig) {
                    surveyOrg.finishDate = new Date()
                    if (!surveyOrg.save()) {
                        flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess')
                    } else {
                        flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess')
                        sendMailToSurveyOwner = true
                    }
                } else {
                    flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess')
                }
                /*}else {
                    flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccessEmptyIEs')
                }*/
            }
        }


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
                noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(result.institution, surveyConfig, RDStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
            }
        }

        if(notProcessedMandatoryProperties.size() > 0){
            flash.error = message(code: "confirm.dialog.concludeBinding.survey.notProcessedMandatoryProperties", args: [notProcessedMandatoryProperties.join(', ')])
        }
        else if(noParticipation || allResultHaveValue){
            surveyOrg.finishDate = new Date()
            if (!surveyOrg.save()) {
                flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess')
            } else {
                flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess')
                sendMailToSurveyOwner = true
            }
        }
        else if(!noParticipation && !allResultHaveValue){
            surveyOrg.finishDate = new Date()
            if (!surveyOrg.save()) {
                flash.error = message(code: 'renewEntitlementsWithSurvey.submitNotSuccess')
            } else {
                flash.message = message(code: 'renewEntitlementsWithSurvey.submitSuccess')
                sendMailToSurveyOwner = true
            }
        }

        if(sendMailToSurveyOwner) {
            surveyService.emailToSurveyOwnerbyParticipationFinish(surveyInfo, result.institution)
            surveyService.emailToSurveyParticipationByFinish(surveyInfo, result.institution)
        }


        redirect(url: request.getHeader('referer'))
    }

    /**
     * Lists the users of the context institution
     * @return a list of users affiliated to the context institution
     * @see User
     * @see de.laser.auth.UserOrg
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
    def deleteUser() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)

        if (! result.editable) {
            redirect controller: 'myInstitution', action: 'users'
            return
        }

        if (result.user) {
            List<Org> affils = Org.executeQuery('select distinct uo.org from UserOrg uo where uo.user = :user',
                    [user: result.user])

            if (affils.size() > 1) {
                flash.error = message(code: 'user.delete.error.multiAffils') as String
                redirect action: 'editUser', params: [uoid: params.uoid]
                return
            }
            else if (affils.size() == 1 && (affils.get(0).id != contextService.getOrg().id)) {
                flash.error = message(code: 'user.delete.error.foreignOrg') as String
                redirect action: 'editUser', params: [uoid: params.uoid]
                return
            }

            if (params.process && result.editable) {
                User userReplacement = (User) genericOIDService.resolveOID(params.userReplacement)

                result.delResult = deletionService.deleteUser(result.user, userReplacement, false)
            }
            else {
                result.delResult = deletionService.deleteUser(result.user, null, DeletionService.DRY_RUN)
            }

            result.substituteList = User.executeQuery(
                    'select distinct u from User u join u.affiliations ua where ua.org = :ctxOrg and u != :self and ua.formalRole = :instAdm order by u.username',
                    [ctxOrg: result.orgInstance, self: result.user, instAdm: Role.findByAuthority('INST_ADM')]
            )
        }

        render view: '/user/global/delete', model: result
    }

    /**
     * Call to edit the given user
     * @return the user details view for editing the profile
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
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
     * @see de.laser.auth.UserOrg
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_ADM") })
    def addAffiliation() {
        Map<String, Object> result = userControllerService.getResultGenericsERMS3067(params)
        if (! result.editable) {
            flash.error = message(code: 'default.noPermissions') as String
            redirect action: 'editUser', params: [uoid: params.uoid]
            return
        }
        userService.addAffiliation(result.user,params.org,params.formalRole,flash)
        redirect action: 'editUser', params: [uoid: params.uoid]
    }

    /**
     * Opens the internal address book for the context institution
     * @return a list view of the institution-internal contacts
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def addressbook() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        params.sort = params.sort ?: 'pr.org.name'

        List visiblePersons = addressbookService.getVisiblePersons("addressbook",params)

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: contextService.getOrg() // private properties
                )

        result.num_visiblePersons = visiblePersons.size()
        result.visiblePersons = visiblePersons.drop(result.offset).take(result.max)

        if (visiblePersons){
            result.emailAddresses = Contact.executeQuery("select c.content from Contact c where c.prs in (:persons) and c.contentType = :contentType",
                    [persons: visiblePersons, contentType: RDStore.CCT_EMAIL])
        }

        result
      }

    /**
     * Call for the current budget code overview of the institution
     * @return a list of budget codes the context institution currently holds
     * @see BudgetCode
     * @see CostItemGroup
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", specRole="ROLE_ADMIN", wtc = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_USER", "ROLE_ADMIN")
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
            Map<BudgetCode, List<CostItemGroup>> costItemGroups = [:]
            if (allBudgetCodes) {
                List<CostItemGroup> ciGroupsForBC = CostItemGroup.findAllByBudgetCodeInList(allBudgetCodes)
                ciGroupsForBC.each { CostItemGroup cig ->
                    List<CostItemGroup> ciGroupForBC = costItemGroups.get(cig.budgetCode)
                    if (!ciGroupForBC) {
                        ciGroupForBC = []
                    }
                    ciGroupForBC << cig
                    costItemGroups.put(cig.budgetCode, ciGroupForBC)
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", wtc = 1)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    def tasks() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if ( ! params.sort) {
            params.sort = "t.endDate"
            params.order = "asc"
        }
        SimpleDateFormat sdFormat = DateUtils.getSDF_NoTime()
        def queryForFilter = filterService.getTaskQuery(params, sdFormat)
        int offset = params.offset ? Integer.parseInt(params.offset) : 0
        result.taskInstanceList = taskService.getTasksByResponsibles(result.user, result.institution, queryForFilter)
        result.taskInstanceCount = result.taskInstanceList.size()
        result.taskInstanceList = taskService.chopOffForPageSize(result.taskInstanceList, result.user, offset)

        result.myTaskInstanceList = taskService.getTasksByCreator(result.user,  queryForFilter, null)
        result.myTaskInstanceCount = result.myTaskInstanceList.size()
        result.myTaskInstanceList = taskService.chopOffForPageSize(result.myTaskInstanceList, result.user, offset)

        def preCon = taskService.getPreconditions(result.institution)
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
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_EDITOR",specRole="ROLE_ADMIN, ROLE_ORG_EDITOR", wtc = 2)
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_EDITOR","ROLE_ADMIN, ROLE_ORG_EDITOR") })
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

    /**
     * Call for the overview of current workflows for the context institution
     * @return the entry view for the workflows, loading current cache settings
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER", ctrlService = 1)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER") })
    def currentWorkflows() {
        Map<String, Object> result = [:]

        if (params.cmd) {
            result.putAll( workflowService.usage(params) )
        }

        SessionCacheWrapper cache = contextService.getSessionCache()
        String urlKey = 'myInstitution/currentWorkflows'
        String fmKey = urlKey + '/_filterMap'
        String pmKey = urlKey + '/_paginationMap'

        Map<String, Object> filterMap = params.findAll{ it.key.startsWith('filter') }

        if (cache) {
            if (request.getHeader('referer')) {
                try {
                    URL url = URI.create(request.getHeader('referer')).toURL()
                    if (! url.getPath().endsWith(urlKey)) {
                        cache.put(fmKey, [ filterStatus: RDStore.WF_WORKFLOW_STATUS_OPEN.id ])
                        cache.remove(pmKey)
                    }
                } catch (Exception e) { }
            }
            if (filterMap.get('filter') == 'false') {
                cache.put(fmKey, [ filterStatus: RDStore.WF_WORKFLOW_STATUS_OPEN.id ])
                cache.remove(pmKey)
            }
            else {
                if (filterMap) {
                    cache.put(fmKey, filterMap)
                    cache.remove(pmKey)
                }
                if (! cache.get(pmKey) || params.max || params.offset) {
                    cache.put(pmKey, [
                            max:    params.max ? params.int('max') : contextService.getUser().getDefaultPageSizeAsInteger(),
                            offset: params.offset ? params.int('offset') : 0
                    ])
                }
            }

            if (cache.get(fmKey)) {
                params.putAll( cache.get(fmKey) as Map )
            }
            if (cache.get(pmKey)) {
                params.putAll( cache.get(pmKey) as Map )
            }
            else {
                params.putAll( [
                        max: contextService.getUser().getDefaultPageSizeAsInteger(),
                        offset: 0
                ] )
            }
        }

        String query = 'select wf from WfWorkflow wf where wf.owner = :ctxOrg'
        Map<String, Object> queryParams = [ctxOrg: contextService.getOrg()]

        result.currentSubscriptions = WfWorkflow.executeQuery(
                'select distinct sub from WfWorkflow wf join wf.subscription sub where wf.owner = :ctxOrg order by sub.name', queryParams
        )
        result.currentPrototypes = WfWorkflow.executeQuery(
                'select distinct wf.prototype from WfWorkflow wf where wf.owner = :ctxOrg', queryParams
        )
        result.currentProviders = result.currentSubscriptions ? Org.executeQuery(
                'select distinct ooo.org from OrgRole ooo where ooo.sub in (:subscriptions) and ooo.roleType = :provider',
                [subscriptions: result.currentSubscriptions, provider: RDStore.OR_PROVIDER]
        ) : []

        if (params.filterPrototype) {
            query = query + ' and wf.prototype = :prototype'
            queryParams.put('prototype', WfWorkflowPrototype.get(params.filterPrototype))
        }
        if (params.filterProvider) {
            query = query + ' and exists (select ooo from OrgRole ooo join ooo.sub sub where ooo.org = :provider and ooo.roleType = :roleType and sub = wf.subscription)'
            queryParams.put('roleType', RDStore.OR_PROVIDER)
            queryParams.put('provider', Org.get(params.filterProvider))
        }
        if (params.filterStatus) {
            query = query + ' and wf.status = :status'
            queryParams.put('status', RefdataValue.get(params.filterStatus))
        }
        if (params.filterSubscription) {
            query = query + ' and wf.subscription = :subscription'
            queryParams.put('subscription', Subscription.get(params.filterSubscription))
        }

        result.currentWorkflows = WfWorkflow.executeQuery(query + ' order by wf.id desc', queryParams)

        if (params.filterPriority) {
            List<WfWorkflow> matches = []
            RefdataValue priority = RefdataValue.get(params.filterPriority)

            result.currentWorkflows.each{ wf ->
                boolean match = false
                wf.getSequence().each { t ->
                    if (t.priority == priority) {
                        match = true
                    }
                    else {
                        if (t.child) {
                            t.child.getSequence().each { c ->
                                if (c.priority == priority) {
                                    match = true
                                }
                            }
                        }
                    }
                }
                if (match) {
                    matches.add(wf)
                }
                result.currentWorkflows = matches
            }
        }

        result.total = result.currentWorkflows.size()
        result.currentWorkflows = result.currentWorkflows.drop(params.offset).take(params.max)

        result
    }

    /**
     * Call for the table view of those consortia which are linked to the context institution
     * @return a list of those institutions on whose consortial subscriptions the context institution is participating
     */
    @Secured(['ROLE_USER'])
    def currentConsortia() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('start')

        // new: filter preset
        result.comboType = RDStore.COMBO_TYPE_CONSORTIUM
        //params.orgSector    = RDStore.O_SECTOR_HIGHER_EDU?.id?.toString()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
        Map queryParams = params.clone()
        //queryParams.subPerpetual = "on"
        //result.propList     = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        /*
        if(!params.subStatus) {
            if(!params.filterSet) {
                params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false
        if(!params.subPerpetual) {
            if(!params.filterSet) {
                params.subPerpetual = "on"
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false
        */
        result.filterSet    = params.filterSet ? true : false

        queryParams.comboType = result.comboType.value
        queryParams.invertDirection = true
        def fsq = filterService.getOrgComboQuery(queryParams, result.institution)
        //def tmpQuery = "select o.id " + fsq.query.minus("select o ")
        //def memberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

		pu.setBenchmark('query')

        List totalConsortia      = Org.executeQuery(fsq.query, fsq.queryParams)
        result.totalConsortia    = totalConsortia
        result.consortiaCount    = totalConsortia.size()
        result.consortia         = totalConsortia.drop((int) result.offset).take((int) result.max)
        //String header
        //String exportHeader

        //header = message(code: 'menu.my.insts')
        //exportHeader = message(code: 'export.my.consortia')
        //SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
        // Write the output to a file
        //String file = "${sdf.format(new Date())}_"+exportHeader

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        result
        /*
        if ( params.exportXLS ) {

            SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(totalMembers, header, true, 'xls')
            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
        }
        else if(params.exportClickMeExcel) {
            if (params.filename) {
                file =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalMembers, selectedFields)

            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
        }
        else {
            withFormat {
                html {
                    result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) organisationService.exportOrg(totalMembers,header,true,"csv"))
                    }
                    out.close()
                }
            }
        }
        */
    }

    /**
     * Call to list all member institutions which are linked by combo to the given context consortium.
     * The result may be filtered by organisational and subscription parameters
     * @return the list of consortial member institutions
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER", specRole="ROLE_ADMIN,ROLE_ORG_EDITOR", wtc = 1)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM","INST_USER","ROLE_ADMIN,ROLE_ORG_EDITOR")
    })
    def manageMembers() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('start')

        // new: filter preset
        result.comboType = RDStore.COMBO_TYPE_CONSORTIUM
        if (params.selectedOrgs) {
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

        result.propList     = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        if(!params.subStatus) {
            if(!params.filterSet) {
                params.subStatus = RDStore.SUBSCRIPTION_CURRENT.id.toString()
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false
        if(!params.subPerpetual) {
            if(!params.filterSet) {
                params.subPerpetual = "on"
                result.filterSet = true
            }
        }
        else result.filterSet    = params.filterSet ? true : false

        params.comboType = result.comboType.value
        def fsq = filterService.getOrgComboQuery(params, result.institution)
        def tmpQuery = "select o.id " + fsq.query.minus("select o ")
        def memberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

		pu.setBenchmark('query')

        if (params.filterPropDef && memberIds) {
            fsq                      = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname asc", 'o', [oids: memberIds])
        }

        List totalMembers      = Org.executeQuery(fsq.query, fsq.queryParams)
        result.totalMembers    = totalMembers
        result.membersCount    = totalMembers.size()
        result.members         = totalMembers.drop((int) result.offset).take((int) result.max)
        String header
        String exportHeader

        header = message(code: 'menu.my.insts')
        exportHeader = message(code: 'export.my.consortia')
        SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
        // Write the output to a file
        String file = "${sdf.format(new Date())}_"+exportHeader

		List bm = pu.stopBenchmark()
		result.benchMark = bm

        if ( params.exportXLS ) {

            SXSSFWorkbook wb = (SXSSFWorkbook) organisationService.exportOrg(totalMembers, header, true, 'xls')
            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        else if(params.exportClickMeExcel) {
            if (params.filename) {
                file =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportOrgs(totalMembers, selectedFields, 'institution')

            response.setHeader "Content-disposition", "attachment; filename=\"${file}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return //IntelliJ cannot know that the return prevents an obsolete redirect
        }
        else {
            withFormat {
                html {
                    result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${file}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) organisationService.exportOrg(totalMembers,header,true,"csv"))
                    }
                    out.close()
                }
            }
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
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_USER", "ROLE_ADMIN") })
    def manageConsortiaSubscriptions() {

        Map<String,Object> result = myInstitutionControllerService.getResultGenerics(this, params)
        result.tableConfig = ['withCostItems']
        result.putAll(subscriptionService.getMySubscriptionsForConsortia(params,result.user,result.institution,result.tableConfig))
        ProfilerUtils pu = result.pu
        pu.setBenchmark("after subscription loading, before providers")
        //LinkedHashMap<Subscription,List<Org>> providers = [:]
        Map<Org,Set<String>> mailAddresses = [:]
        BidiMap subLinks = new DualHashBidiMap()
        if(params.format || params.exportXLS) {
            List<Subscription> subscriptions = result.entries.collect { entry -> (Subscription) entry[1] } as List<Subscription>
            Links.executeQuery("select l from Links l where (l.sourceSubscription in (:targetSubscription) or l.destinationSubscription in (:targetSubscription)) and l.linkType = :linkType",[targetSubscription:subscriptions,linkType:RDStore.LINKTYPE_FOLLOWS]).each { Links link ->
                if(link.sourceSubscription && link.destinationSubscription)
                subLinks.put(link.sourceSubscription,link.destinationSubscription)
            }
            /*OrgRole.findAllByRoleTypeInList([RDStore.OR_PROVIDER,RDStore.OR_AGENCY]).each { it ->
                List<Org> orgs = providers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                providers.put(it.sub,orgs)
            }*/
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

        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        pu.setBenchmark("before xls")
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
            List titles = [message(code:'sidewide.number'),message(code:'myinst.consortiaSubscriptions.member'), message(code:'org.mainContact.label'),message(code:'myinst.consortiaSubscriptions.subscription'),message(code:'globalUID.label'),
                           message(code:'license.label'), message(code:'myinst.consortiaSubscriptions.packages'),message(code:'myinst.consortiaSubscriptions.provider'),message(code:'myinst.consortiaSubscriptions.runningTimes'),
                           message(code:'subscription.isPublicForApi.label'),message(code:'subscription.hasPerpetualAccess.label'),
                           message(code:'financials.amountFinal'),"${message(code:'financials.isVisibleForSubscriber')} / ${message(code:'financials.costItemConfiguration')}"]
            titles.eachWithIndex{ titleName, int i ->
                Cell cell = headerRow.createCell(i)
                cell.setCellValue(titleName)
            }
            sheet.createFreezePane(0,1)
            Row row
            Cell cell
            int rownum = 1
            int sumcell = 11
            int sumTitleCell = 10
            result.entries.eachWithIndex { entry, int sidewideNumber ->
                log.debug("processing entry ${sidewideNumber} ...")
                CostItem ci = (CostItem) entry[0] ?: new CostItem()
                Subscription subCons = (Subscription) entry[1]
                Org subscr = (Org) entry[2]
                int cellnum = 0
                row = sheet.createRow(rownum)
                //sidewide number
                log.debug("insert sidewide number")
                cell = row.createCell(cellnum++)
                cell.setCellValue(rownum)
                //sortname
                log.debug("insert sortname")
                cell = row.createCell(cellnum++)
                String subscrName = ""
                if(subscr.sortname) subscrName += subscr.sortname
                subscrName += "(${subscr.name})"
                cell.setCellValue(subscrName)
                log.debug("insert general contacts")
                //general contacts
                Set<String> generalContacts = mailAddresses.get(subscr)
                cell = row.createCell(cellnum++)
                if(generalContacts)
                    cell.setCellValue(generalContacts.join('; '))
                //subscription name
                log.debug("insert subscription name")
                cell = row.createCell(cellnum++)
                String subscriptionString = subCons.name
                //if(subCons._getCalculatedPrevious()) //avoid! Makes 5846 queries!!!!!
                if(subLinks.getKey(subCons.id))
                    subscriptionString += " (${message(code:'subscription.hasPreviousSubscription')})"
                cell.setCellValue(subscriptionString)
                //subscription globalUID
                log.debug("insert subscription global UID")
                cell = row.createCell(cellnum++)
                cell.setCellValue(subCons.globalUID)
                //license name
                log.debug("insert license name")
                cell = row.createCell(cellnum++)
                if(result.linkedLicenses.get(subCons)) {
                    List<String> references = result.linkedLicenses.get(subCons).collect { License l -> l.reference }
                    cell.setCellValue(references.join("\n"))
                }
                //packages
                log.debug("insert package name")
                cell = row.createCell(cellnum++)
                cell.setCellStyle(lineBreaks)
                List<String> packageNames = []
                subCons.packages.each { subPkg ->
                    packageNames << subPkg.pkg.name
                }
                cell.setCellValue(packageNames.join("\n"))
                //provider
                log.debug("insert provider name")
                cell = row.createCell(cellnum++)
                cell.setCellStyle(lineBreaks)
                List<String> providerNames = []
                subCons.orgRelations.findAll{ OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.each { OrgRole p ->
                    log.debug("Getting provider ${p.org}")
                    providerNames << p.org.name
                }
                cell.setCellValue(providerNames.join("\n"))
                //running time from / to
                log.debug("insert running times")
                cell = row.createCell(cellnum++)
                String dateString = ""
                if(ci.id) {
                    if(ci.getDerivedStartDate()) dateString += sdf.format(ci.getDerivedStartDate())
                    if(ci.getDerivedEndDate()) dateString += " - ${sdf.format(ci.getDerivedEndDate())}"
                }
                cell.setCellValue(dateString)
                //is public for api
                log.debug("insert api flag")
                cell = row.createCell(cellnum++)
                cell.setCellValue(ci.sub?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                //has perpetual access
                log.debug("insert perpetual access flag")
                cell = row.createCell(cellnum++)
                cell.setCellValue(ci.sub?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                //final sum
                log.debug("insert final sum")
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
                log.debug("insert cost sign and visiblity")
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
                    sheet.autoSizeColumn(i)
                }
                catch (NullPointerException e) {
                    log.error("Null value in column ${i}")
                }
            }
            String filename = "${DateUtils.SDF_NoTimeNoPoint.format(new Date())}_${g.message(code:'export.my.consortiaSubscriptions')}.xlsx"
            response.setHeader("Content-disposition","attachment; filename=\"${filename}\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else {
            result.benchMark = pu.stopBenchmark()
            withFormat {
                html {
                    result
                }
                csv {
                    List titles = [message(code: 'sidewide.number'), message(code: 'myinst.consortiaSubscriptions.member'), message(code: 'org.mainContact.label'), message(code: 'myinst.consortiaSubscriptions.subscription'), message(code: 'globalUID.label'),
                                   message(code: 'license.label'), message(code: 'myinst.consortiaSubscriptions.packages'), message(code: 'myinst.consortiaSubscriptions.provider'), message(code: 'myinst.consortiaSubscriptions.runningTimes'),
                                   message(code: 'subscription.isPublicForApi.label'), message(code: 'subscription.hasPerpetualAccess.label'),
                                   message(code: 'financials.amountFinal'), "${message(code: 'financials.isVisibleForSubscriber')} / ${message(code: 'financials.costItemConfiguration')}"]
                    List columnData = []
                    List row
                    result.entries.eachWithIndex { entry, int sidewideNumber ->
                        row = []
                        log.debug("processing entry ${sidewideNumber} ...")
                        CostItem ci = (CostItem) entry[0] ?: new CostItem()
                        Subscription subCons = (Subscription) entry[1]
                        Org subscr = (Org) entry[2]
                        int cellnum = 0
                        //sidewide number
                        log.debug("insert sidewide number")
                        cellnum++
                        row.add(sidewideNumber)
                        //sortname
                        log.debug("insert sortname")
                        cellnum++
                        String subscrName = ""
                        if (subscr.sortname) subscrName += subscr.sortname
                        subscrName += "(${subscr.name})"
                        row.add(subscrName.replaceAll(',', ' '))
                        log.debug("insert general contacts")
                        //general contacts
                        Set<String> generalContacts = mailAddresses.get(subscr)
                        if (generalContacts)
                            row.add(generalContacts.join('; '))
                        else row.add(' ')
                        //subscription name
                        log.debug("insert subscription name")
                        cellnum++
                        String subscriptionString = subCons.name
                        //if(subCons._getCalculatedPrevious()) //avoid! Makes 5846 queries!!!!!
                        if (subLinks.getKey(subCons.id))
                            subscriptionString += " (${message(code: 'subscription.hasPreviousSubscription')})"
                        row.add(subscriptionString.replaceAll(',', ' '))
                        //subscription global uid
                        log.debug("insert global uid")
                        cellnum++
                        row.add(subCons.globalUID)
                        //license name
                        log.debug("insert license name")
                        cellnum++
                        if (result.linkedLicenses.get(subCons)) {
                            List<String> references = result.linkedLicenses.get(subCons).collect { License l -> l.reference.replace(',', ' ') }
                            row.add(references.join(' '))
                        } else row.add(' ')
                        //packages
                        log.debug("insert package name")
                        cellnum++
                        String packagesString = " "
                        subCons.packages.each { subPkg ->
                            packagesString += "${subPkg.pkg.name} "
                        }
                        row.add(packagesString.replaceAll(',', ' '))
                        //provider
                        log.debug("insert provider name")
                        cellnum++
                        List<String> providerNames = []
                        subCons.orgRelations.findAll{ OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.each { OrgRole p ->
                            log.debug("Getting provider ${p.org}")
                            providerNames << p.org.name
                        }
                        row.add(providerNames.join( ' '))
                        //running time from / to
                        log.debug("insert running times")
                        cellnum++
                        String dateString = " "
                        if (ci.id) {
                            if (ci.getDerivedStartDate()) dateString += sdf.format(ci.getDerivedStartDate())
                            if (ci.getDerivedEndDate()) dateString += " - ${sdf.format(ci.getDerivedEndDate())}"
                        }
                        row.add(dateString)
                        //is public for api
                        log.debug("insert api flag")
                        cellnum++
                        row.add(ci.sub?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                        //has perpetual access
                        log.debug("insert perpetual access flag")
                        cellnum++
                        row.add(ci.sub?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"))
                        //final sum
                        log.debug("insert final sum")
                        cellnum++
                        if (ci.id && ci.costItemElementConfiguration) {
                            row.add("${ci.costInBillingCurrencyAfterTax ?: 0.0} ${ci.billingCurrency ?: 'EUR'}")
                        } else row.add(" ")
                        //cost item sign and visibility
                        log.debug("insert cost sign and visiblity")
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
                    //sumcell = 11
                    //sumTitleCell = 10
                    for (int h = 0; h < 10; h++) {
                        row.add(" ")
                    }
                    row.add(message(code: 'financials.export.sums'))
                    columnData.add(row)
                    columnData.add([])
                    result.finances.each { entry ->
                        row = []
                        for (int h = 0; h < 10; h++) {
                            row.add(" ")
                        }
                        row.add("${message(code: 'financials.sum.billing')} ${entry.key}")
                        row.add("${entry.value} ${entry.key}")
                        columnData.add(row)
                    }
                    String filename = "${DateUtils.SDF_NoTimeNoPoint.format(new Date())}_${g.message(code: 'export.my.consortiaSubscriptions')}.csv"
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
    }

    /**
     * Call for the consortium to list the surveys a given institution is participating at.
     * The result may be displayed as HTML or exported as Excel worksheet
     * @return a list of surveys the context consortium set up and the given institution is participating at
     */
    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = { ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_USER", "ROLE_ADMIN") })
    def manageParticipantSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('filterService')

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        DateFormat sdFormat = DateUtils.getSDF_NoTime()

        result.participant = Org.get(Long.parseLong(params.id))

        params.tab = params.tab ?: 'new'

        if(params.tab != 'new'){
            params.sort = 'surInfo.endDate DESC, LOWER(surInfo.name)'
        }

        /*if (params.validOnYear == null || params.validOnYear == '') {
            def sdfyear = new java.text.SimpleDateFormat(message(code: 'default.date.format.onlyYear'))
            params.validOnYear = sdfyear.format(new Date())
        }*/

        result.surveyYears = SurveyOrg.executeQuery("select Year(surorg.surveyConfig.surveyInfo.startDate) from SurveyOrg surorg where surorg.org = :org and surorg.surveyConfig.surveyInfo.startDate != null group by YEAR(surorg.surveyConfig.surveyInfo.startDate) order by YEAR(surorg.surveyConfig.surveyInfo.startDate)", [org: result.participant]) ?: []

        params.consortiaOrg = result.institution

        def fsq = filterService.getParticipantSurveyQuery_New(params, sdFormat, result.participant)

        result.surveyResults = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)

        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
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
            result.countSurveys = surveyService.getSurveyParticipantCounts_New(result.participant, params)

            result
        }
    }

    /**
     * Manages calls about the property groups the context institution defined. With a given parameter,
     * editing may be done on the given property group
     * @return in every case, the list of property groups; the list may be exported as Excel with the usage data as well, then, an Excel worksheet is being returned
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", wtc = 1)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
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
            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyGroupUsageXLS(result.propDefGroups))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.propertyGroups')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else
            result
    }

    /**
     * Call to display the current usage for the given property in the system
     * @return a form view of the given property definition with their usage in the context institution's objects
     */
    @DebugAnnotation(perm = "ORG_INST,ORG_CONSORTIUM", affil = "INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
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
        EhcacheWrapper cache = contextService.getCache("/manageProperties", contextService.USER_SCOPE)
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

        String localizedName
        switch(LocaleContextHolder.getLocale()) {
            case [ Locale.GERMANY, Locale.GERMAN ]:
                localizedName = "name_de"
                break
            default: localizedName = "name_en"
                break
        }
        //result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.org)
        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:result.institution,availableTypes:[PropertyDefinition.SUB_PROP,PropertyDefinition.LIC_PROP,PropertyDefinition.PRS_PROP,PropertyDefinition.PLA_PROP,PropertyDefinition.ORG_PROP]])
        result.propList = propList

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
    @DebugAnnotation(perm = "ORG_INST,ORG_CONSORTIUM", affil = "INST_EDITOR", wtc = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
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
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", wtc = 1)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
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
                            flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [count, oldName, newName])
                        }
                        catch (Exception e) {
                            e.printStackTrace()
                            flash.error = message(code: 'menu.institutions.replace_prop.error', args: [oldName, newName])
                        }
                    }
                }
                break
            case 'delete': flash.message = deletePrivatePropertyDefinition(params)
                break
        }

        result.languageSuffix = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())

        Map<String, Set<PropertyDefinition>> propDefs = [:]
        Set<String> availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR
        if(result.institution.getCustomerType() == "ORG_INST")
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
            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyUsageExportXLS(propDefs))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.privateProperties')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else
            result
    }

    /**
     * Call to display the current general public property definitions of the system
     * @return a read-only list of public / general property definitions with the usages of objects owned by the context institution
     * @see AdminController#managePropertyDefinitions()
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", wtc = 1)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
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

        result.languageSuffix = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())

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
            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(exportService.generatePropertyUsageExportXLS(propDefs))
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${message(code:'export.my.customProperties')}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
        }
        else
            render view: 'managePrivatePropertyDefinitions', model: result
    }

    /**
     * If a user is affiliated to several institutions, this call changes the context institution to the given one and redirects
     * the user to the dashboard page
     * @return the dashboard view of the picked context institution
     */
    @Secured(['ROLE_USER'])
    def switchContext() {
        User user = contextService.getUser()
        Org org = (Org) genericOIDService.resolveOID(params.oid)

        if (user && org && org.id in user.getAuthorizedOrgsIds()) {
            log.debug('switched context to: ' + org)
            contextService.setOrg(org)
        }
        redirect action:'dashboard', params:params.remove('oid')
    }

    /**
     * Deletes the given private property definition for this institution
     * @param params the parameter map containing the property definition parameters
     * @return success or error messages
     */
    private deletePrivatePropertyDefinition(params) {
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
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def copyLicense() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(this, params)

        if(params.id)
        {
            License license = License.get(params.id)
            boolean isEditable = license.isEditableBy(result.user)

            if (! (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR'))) {
                flash.error = message(code:'license.permissionInfo.noPerms')
                response.sendError(401)
                return;
            }

            if(isEditable){
                redirect controller: 'license', action: 'copyLicense', params: [sourceObjectId: genericOIDService.getOID(license), copyObject: true]
                return
            }else {
                flash.error = message(code:'license.permissionInfo.noPerms')
                response.sendError(401)
                return;
            }
        }
    }


}
