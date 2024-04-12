package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.PdfUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

class VendorController {

    ExportClickMeService exportClickMeService
    GokbService gokbService
    UserService userService
    VendorService vendorService

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
        Map<String, Object> result = vendorService.getResultGenerics(params)
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
            Set<Map<String, Object>> vendorListTotal = result.wekbRecords.collect { String wekbId, Map data ->
                Vendor laserVendor = Vendor.findByGokbId(wekbId)
                String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
                if(result.institution.isCustomerType_Consortium()) {
                    subscriptionConsortiumFilter = 'and s.instanceOf = null'
                    licenseConsortiumFilter = 'and l.instanceOf = null'
                }
                data.contacts = laserVendor.contacts
                data.addresses = laserVendor.addresses
                data.packages = laserVendor.packages
                data.platforms = vendorService.getSubscribedPlatforms(laserVendor, result.institution)
                data.subscriptions = Subscription.executeQuery('select vr.subscription from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: laserVendor, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution])
                data.licenses = License.executeQuery('select vr.license from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [vendor: laserVendor, current: RDStore.LICENSE_CURRENT, context: result.institution])
                data
            }
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            switch(params.fileformat) {
                case 'xlsx':
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportVendors(vendorListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.XLS, contactSwitch)
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
                        writer.write((String) exportClickMeService.exportVendors(vendorListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.CSV, contactSwitch))
                    }
                    out.close()
                    return
                case 'pdf':
                    Map<String, Object> pdfOutput = exportClickMeService.exportVendors(vendorListTotal, selectedFields, 'provider', ExportClickMeService.FORMAT.PDF, contactSwitch)

                    byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                    response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                    response.setContentType('application/pdf')
                    response.outputStream.withStream { it << pdf }
                    return
            }
        }
        else {
            Set<Vendor> vendorsTotal = Vendor.findAllByGokbIdInList(result.wekbRecords.keySet())
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
            result.editable = false //hard set until it is not decided how to deal with current agencies
            result.subEditable = userService.hasFormalAffiliation_or_ROLEADMIN(result.user, result.institution, 'INST_EDITOR')
            result.isMyVendor = vendorService.isMyVendor(vendor, result.institution)
            result.platforms = vendor.packages.pkg.nominalPlatform
            String subscriptionConsortiumFilter = '', licenseConsortiumFilter = ''
            if(result.institution.isCustomerType_Consortium()) {
                subscriptionConsortiumFilter = 'and s.instanceOf = null'
                licenseConsortiumFilter = 'and l.instanceOf = null'
            }
            result.subLinks = VendorRole.executeQuery('select vr from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and s.status = :current and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, context: result.institution])
            result.licLinks = VendorRole.executeQuery('select vr from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and l.status = :current and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, current: RDStore.LICENSE_CURRENT, context: result.institution])
            result.currentSubscriptionsCount = VendorRole.executeQuery('select count(vr) from VendorRole vr join vr.subscription s join s.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+subscriptionConsortiumFilter, [vendor: vendor, context: result.institution])[0]
            result.currentLicensesCount = VendorRole.executeQuery('select count(vr) from VendorRole vr join vr.license l join l.orgRelations oo where vr.vendor = :vendor and oo.org = :context '+licenseConsortiumFilter, [vendor: vendor, context: result.institution])[0]
            Map queryResult = gokbService.executeQuery(result.wekbApi.baseUrl + result.wekbApi.fixToken + "/searchApi", [uuid: vendor.gokbId])
            if (queryResult.error && queryResult.error == 404) {
                result.error = message(code: 'wekb.error.404')
            }
            else if (queryResult) {
                List records = queryResult.result
                if(records) {
                    result.vendorWekbData = records[0]
                }
            }
            result
        }
        else {
            response.sendError(404)
            return
        }
    }


}
