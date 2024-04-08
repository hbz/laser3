package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.utils.LocaleUtils
import grails.plugin.springsecurity.annotation.Secured

class VendorController {

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
        Set<Vendor> vendorsTotal = Vendor.findAllByGokbIdInList(result.wekbRecords.keySet())
        result.vendorListTotal = vendorsTotal.size()
        result.vendorList = vendorsTotal.drop(result.offset).take(result.max)
        result
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
