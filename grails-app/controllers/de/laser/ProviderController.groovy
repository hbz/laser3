package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.User
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
    WorkflowService workflowService

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
        Map<String, Object> result = [:]
        result.propList    = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())
        result.user        = contextService.getUser()

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

        params.sort        = params.sort ?: " LOWER(o.sortname), LOWER(o.name)"


        result.filterSet = params.filterSet ? true : false

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Set<Provider> providerListTotal = []
        result.wekbRecords = providerService.getWekbProviderRecords(params, result)
        if (params.curatoryGroup || params.curatoryGroupType)
            providerListTotal = providerListTotal.findAll { Provider provider -> provider.gokbId in result.wekbRecords.keySet() }

        if (params.isMyX) {
            List<String> xFilter = params.list('isMyX')
            Set<Long> f1Result = [], f2Result = []
            boolean   f1Set = false, f2Set = false

            if (xFilter.contains('ismyx_exclusive')) {
                f1Result.addAll( providerListTotal.findAll { result.currentProviderIdList.contains( it.id ) }.collect{ it.id } )
                f1Set = true
            }
            if (xFilter.contains('ismyx_not')) {
                f1Result.addAll( providerListTotal.findAll { ! result.currentProviderIdList.contains( it.id ) }.collect{ it.id }  )
                f1Set = true
            }
            if (xFilter.contains('wekb_exclusive')) {
                f2Result.addAll( providerListTotal.findAll { it.gokbId != null }.collect{ it.id } )
                f2Set = true
            }
            if (xFilter.contains('wekb_not')) {
                f2Result.addAll( providerListTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                f2Set = true
            }

            if (f1Set) { providerListTotal = providerListTotal.findAll { f1Result.contains(it.id) } }
            if (f2Set) { providerListTotal = providerListTotal.findAll { f2Result.contains(it.id) } }
        }

        result.providerListTotal = providerListTotal.size()
        result.providerList      = providerListTotal.drop((int) result.offset).take((int) result.max)

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
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportProviders(providerListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.XLS, contactSwitch)
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
                        writer.write((String) exportClickMeService.exportProviders(providerListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportProviders(providerListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.PDF, contactSwitch)

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
        workflowService.executeCmdAndUpdateResult(result, params)
        result
    }
}
