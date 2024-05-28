package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.helper.Params
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.PdfUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

class VendorController {

    ExportClickMeService exportClickMeService
    GenericOIDService genericOIDService
    GokbService gokbService
    TaskService taskService
    UserService userService
    VendorService vendorService
    WorkflowService workflowService

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'list' : 'menu.public.all_vendors'
    ]

    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def index() {
        redirect 'list'
    }

    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    def list() {
        Map<String, Object> result = vendorService.getResultGenerics(params), queryParams = [:]
        result.flagContentGokb = true // vendorService.getWekbVendorRecords()
        Map queryCuratoryGroups = gokbService.executeQuery(result.wekbApi.baseUrl + result.wekbApi.fixToken + '/groups', [:])
        if(queryCuratoryGroups.code == 404) {
            result.error = message(code: 'wekb.error.'+queryCuratoryGroups.error) as String
        }
        else {
            if (queryCuratoryGroups) {
                List recordsCuratoryGroups = queryCuratoryGroups.result
                result.curatoryGroups = recordsCuratoryGroups?.findAll { it.status == "Current" }
            }
            result.wekbRecords = vendorService.getWekbVendorRecords(params, result)
        }
        result.curatoryGroupTypes = [
                [value: 'Provider', name: message(code: 'package.curatoryGroup.provider')],
                [value: 'Vendor', name: message(code: 'package.curatoryGroup.vendor')],
                [value: 'Other', name: message(code: 'package.curatoryGroup.other')]
        ]
        List<String> queryArgs = []
        if(params.containsKey('nameContains')) {
            queryArgs << "(genfunc_filter_matcher(v.name, :name) = true or genfunc_filter_matcher(v.sortname, :name) = true)"
            queryParams.name = params.nameContains
        }
        if(params.containsKey('venStatus')) {
            queryArgs << "v.status in (:status)"
            queryParams.status = Params.getRefdataList(params, 'venStatus')
        }
        else if(!params.containsKey('venStatus') && !params.containsKey('filterSet')) {
            queryArgs << "v.status = :status"
            queryParams.status = "Current"
            params.venStatus = RDStore.VENDOR_STATUS_CURRENT.id
        }

        if(params.containsKey('qp_supportedLibrarySystems')) {
            queryArgs << "exists (select ls from v.supportedLibrarySystems ls where ls.librarySystem in (:librarySystems))"
            queryParams.put('librarySystems', Params.getRefdataList(params, 'qp_supportedLibrarySystems'))
        }

        if(params.containsKey('qp_electronicBillings')) {
            queryArgs << "exists (select eb from v.electronicBillings eb where eb.invoiceFormat in (:electronicBillings))"
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
        String vendorQuery = 'select v from Vendor v'
        if(queryArgs) {
            vendorQuery += ' where '+queryArgs.join(' and ')
        }
        if(params.containsKey('sort')) {
            vendorQuery += " order by ${params.sort} ${params.order ?: 'asc'}, v.name ${params.order ?: 'asc'} "
        }
        else
            vendorQuery += " order by v.sortname "
        Set<Vendor> vendorsTotal = Vendor.executeQuery(vendorQuery, queryParams)

        String message = message(code: 'export.all.vendors') as String
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
                        writer.write((String) exportClickMeService.exportVendors(vendorsTotal, selectedFields, ExportClickMeService.FORMAT.CSV, contactSwitch))
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
        else {
            result.vendorListTotal = vendorsTotal.size()
            result.vendorList = vendorsTotal.drop(result.offset).take(result.max)
            result
        }
    }

    @DebugInfo(isInstUser_denySupport_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport_or_ROLEADMIN()
    })
    @Check404(domain=Vendor)
    def show() {
        Map<String, Object> result = vendorService.getResultGenerics(params)
        if(params.containsKey('id')) {
            Vendor vendor = Vendor.get(params.id)
            result.vendor = vendor
            result.editable = vendor.gokbId ? false : userService.hasFormalAffiliation_or_ROLEADMIN(result.user, result.institution, 'INST_EDITOR')
            result.subEditable = userService.hasFormalAffiliation_or_ROLEADMIN(result.user, result.institution, 'INST_EDITOR')
            result.isMyVendor = vendorService.isMyVendor(vendor, result.institution)
            String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
            if(result.institution.isCustomerType_Consortium()) {
                subscriptionConsortiumFilter = 'and s.instanceOf = null'
                licenseConsortiumFilter = 'and l.instanceOf = null'
            }
            result.packages = Package.executeQuery('select pkg from PackageVendor pv join pv.pkg pkg, VendorRole vr, OrgRole oo join oo.sub s where pv.vendor = vr.vendor and vr.subscription = s and vr.vendor = :vendor and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution]) as Set<Package>
            result.platforms = Platform.executeQuery('select pkg.nominalPlatform from PackageVendor pv join pv.pkg pkg, VendorRole vr, OrgRole oo join oo.sub s where pkg.provider = :vendor and vr.subscription = s and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution]) as Set<Platform>
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.institution, vendor)
            result.subLinks = VendorRole.executeQuery('select vr from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution])
            result.licLinks = VendorRole.executeQuery('select vr from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, current: RDStore.LICENSE_CURRENT, context: result.institution])
            result.currentSubscriptionsCount = VendorRole.executeQuery('select count(vr) from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, context: result.institution])[0]
            result.currentLicensesCount = VendorRole.executeQuery('select count(vr) from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, context: result.institution])[0]

            workflowService.executeCmdAndUpdateResult(result, params)

            result
        }
        else {
            response.sendError(404)
            return
        }
    }

    @DebugInfo(isInstUser_or_ROLEADMIN = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
    })
    @Check404()
    def workflows() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        workflowService.executeCmdAndUpdateResult(result, params)
        result
    }

    /**
     * Assigns the given subject group to the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def addAttribute() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        if (!result.vendor) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        def newAttr = genericOIDService.resolveOID(params.get(params.field))
        if (result.editable) {
            switch(params.field) {
                case 'invoicingFormat':
                    if (!newAttr) {
                        flash.message = message(code: 'default.not.found.message', args: [message(code: 'vendor.invoicing.formats.label'), params.get(params.field)]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                    if (result.vendor.electronicBillings.find { ElectronicBilling eb -> eb.invoicingFormat.id == newAttr.id }) {
                        flash.message = message(code: 'default.err.alreadyExist', args: [message(code: 'vendor.invoicing.formats.label')]) as String
                        redirect(url: request.getHeader('referer'))
                        return
                    }
                        result.vendor.addToElectronicBillings(invoicingFormat: newAttr)
                    break
                case 'invoiceDispatch':
                    break
            }
            result.vendor.save()
        }

        redirect action: 'show', id: params.id
    }

    /**
     * Removes the given subject group from the given organisation
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def deleteAttribute() {
        Map<String, Object> result = vendorService.getResultGenerics(params)

        if (!result.vendor) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'vendor'), params.id]) as String
            redirect(url: request.getHeader('referer'))
            return
        }
        if (result.editable) {
            def attr = genericOIDService.resolveOID(params.removeObjectOID)
            switch(params.field) {
                case 'invoicingFormat': result.vendor.removeFromElectronicBillings(attr)
                    break
                case 'invoiceDispatch': result.vendor.removeFromInvoiceDispatchs(attr)
                    break
            }
            result.vendor.save()
            attr.delete()
        }

        redirect(url: request.getHeader('referer'))
    }
}
