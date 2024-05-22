package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.helper.Params
import de.laser.properties.PropertyDefinition
import de.laser.remote.ApiSource
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

class ProviderController {

    ContextService contextService
    ExportClickMeService exportClickMeService
    FilterService filterService
    GokbService gokbService
    ProviderService providerService
    TaskService taskService
    DocstoreService docstoreService
    WorkflowService workflowService
    UserService userService

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'menu.public.all_providers'
    ]

    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def index() {
        redirect 'list'
    }

    /**
     * Call to list non-academic institutions such as providers. The list may be rendered
     * as HTML or a configurable Excel worksheet or CSV file. The export contains more fields
     * than the HTML table due to reasons of space in the HTML page
     * @return a list of provider organisations, either as HTML table or as Excel/CSV export
     * @see OrganisationService#exportOrg(java.util.List, java.lang.Object, boolean, java.lang.String)
     * @see ExportClickMeService#getExportOrgFields(java.lang.String)
     */
    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def list() {
        Map<String, Object> result = [institution: contextService.getOrg(), user: contextService.getUser()], queryParams = [:]
        result.propList    = PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr = :def and (pd.tenant is null or pd.tenant = :tenant) order by pd.name_de asc", [
                def: PropertyDefinition.PRV_PROP,
                tenant: result.institution
        ])

        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.wekbApi = apiSource
        Map queryCuratoryGroups = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + '/groups', [:])
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
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        List<String> queryArgs = []
        result.wekbRecords = providerService.getWekbProviderRecords(params, result)
        if(params.containsKey('providerNameContains')) {
            queryArgs << "(genfunc_filter_matcher(p.name, :name) = true or genfunc_filter_matcher(p.sortname, :name) = true)"
            queryParams.name = params.providerNameContains
        }
        if(params.containsKey('provStatus')) {
            queryArgs << "p.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'provStatus')
        }
        else if(!params.containsKey('provStatus') && !params.containsKey('filterSet')) {
            queryArgs << "p.status = :status"
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
            queryArgs << "exists (select iv from p.invoicingVendors iv where iv.vendor in (:vendors))"
            queryParams.put('vendors', Params.getLongList(params, 'qp_invoicingVendors'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from p.electronicBillings eb where eb.invoiceFormat in (:electronicBillings))"
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
        String providerQuery = 'select p from Provider p'
        if(queryArgs) {
            providerQuery += ' where '+queryArgs.join(' and ')
        }
        if(params.containsKey('sort')) {
            providerQuery += " order by ${params.sort} ${params.order ?: 'asc'}, p.name ${params.order ?: 'asc'} "
        }
        else
            providerQuery += " order by p.sortname "
        Set<Provider> providersTotal = Provider.executeQuery(providerQuery, queryParams)


        result.filterSet = params.filterSet ? true : false

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = [], f2Result = []
            boolean   f1Set = false, f2Set = false

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( providersTotal.findAll { result.currentProviderIdList.contains( it.id ) }.collect{ it.id } )
                f1Set = true
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( providersTotal.findAll { ! result.currentProviderIdList.contains( it.id ) }.collect{ it.id }  )
                f1Set = true
            }
            if (xFilter.contains('wekb_exclusive')) {
                f2Result.addAll( providersTotal.findAll { it.gokbId != null }.collect{ it.id } )
                f2Set = true
            }
            if (xFilter.contains('wekb_not')) {
                f2Result.addAll( providersTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                f2Set = true
            }

            if (f1Set) { providersTotal = providersTotal.findAll { f1Result.contains(it.id) } }
            if (f2Set) { providersTotal = providersTotal.findAll { f2Result.contains(it.id) } }
        }

        result.providersTotal = providersTotal.size()
        result.providerList   = providersTotal.drop((int) result.offset).take((int) result.max)

        String message = message(code: 'export.all.providers') as String
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())
        String filename = message+"_${datetoday}"

        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []
        if(params.fileformat) {
            if (params.filename) {
                filename =params.filename
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportProviders(providersTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.XLS, contactSwitch)
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
                        writer.write((String) exportClickMeService.exportProviders(providersTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportProviders(providersTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            result
        }
    }

    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def show() {
        Map<String, Object> result = providerService.getResultGenericsAndCheckAccess(params)

        if (! result) {
            response.sendError(401)
            return
        }
        if(result.error)
            flash.error = result.error //to display we:kb's eventual 404
        if(params.containsKey('id')) {
            Provider provider = Provider.get(params.id)
            result.provider = provider
            result.editable = provider.gokbId ? false : userService.hasFormalAffiliation_or_ROLEADMIN(result.user, result.institution, 'INST_EDITOR')
            result.subEditable = userService.hasFormalAffiliation_or_ROLEADMIN(result.user, result.institution, 'INST_EDITOR')
            result.isMyProvider = providerService.isMyProvider(provider, result.institution)
            String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
            if(result.institution.isCustomerType_Consortium()) {
                subscriptionConsortiumFilter = 'and s.instanceOf = null'
                licenseConsortiumFilter = 'and l.instanceOf = null'
            }
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, provider)
            result.subLinks = ProviderRole.executeQuery('select pvr from ProviderRole pvr join pvr.subscription s join s.orgRelations oo where pvr.provider = :provider and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution])
            result.licLinks = ProviderRole.executeQuery('select pvr from ProviderRole pvr join pvr.license l join l.orgRelations oo where pvr.provider = :provider and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [provider: provider, current: RDStore.LICENSE_CURRENT, context: result.institution])
            result.currentSubscriptionsCount = ProviderRole.executeQuery('select count(pvr) from ProviderRole pvr join pvr.subscription s join s.orgRelations oo where pvr.provider = :provider and oo.org = :context '+subscriptionConsortiumFilter, [provider: provider, context: result.institution])[0]
            result.currentLicensesCount = ProviderRole.executeQuery('select count(pvr) from ProviderRole pvr join pvr.license l join l.orgRelations oo where pvr.provider = :provider and oo.org = :context '+licenseConsortiumFilter, [provider: provider, context: result.institution])[0]
            result
        }
        //workflowService.executeCmdAndUpdateResult(result, params)
        result
    }
}
