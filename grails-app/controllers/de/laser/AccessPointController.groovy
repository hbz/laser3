package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.ctrl.AccessPointControllerService
import de.laser.helper.DateUtils
import de.laser.annotations.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class AccessPointController  {

    def contextService
    def orgTypeService
    GenericOIDService genericOIDService
    AccessService accessService
    AccessPointControllerService accessPointControllerService
    AccessPointService accessPointService
    EscapeService escapeService


    //static allowedMethods = [create: ['GET', 'POST'], delete: ['GET', 'POST'], dynamicSubscriptionList: ['POST'], dynamicPlatformList: ['POST']]

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def addIpRange() {
        Map<String,Object> ctrlResult = accessPointControllerService.addIpRange(params)
        if (ctrlResult.status == AccessPointControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod, id: params.id, params: [ip: ctrlResult.result.invalidRanges.join(' '), tab: params.tab]
            return
        } else {
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod, id: params.id, params: [autofocus: true, tab: params.tab]
            return
        }
    }

    @Secured(['ROLE_USER'])
    def dynamicSubscriptionList() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        List<Long> currentSubIds = orgTypeService.getCurrentSubscriptionIds(orgAccessPoint.org)
        String qry = """
            Select p, sp, s from Platform p
            JOIN p.oapp as oapl
            JOIN oapl.subPkg as sp
            JOIN sp.subscription as s
            WHERE oapl.active=true and oapl.oap=${orgAccessPoint.id}
            AND s.id in (:currentSubIds)
            AND EXISTS (SELECT 1 FROM OrgAccessPointLink ioapl 
                where ioapl.subPkg=oapl.subPkg and ioapl.platform=p and ioapl.oap is null)"""
        if (params.checked == "true"){
            qry += " AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"
        }
        ArrayList linkedPlatformSubscriptionPackages = Platform.executeQuery(qry, [currentSubIds: currentSubIds])
        render(template: "linked_subs_table", model: [linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages, params:params])
    }

    @Secured(['ROLE_USER'])
    def dynamicPlatformList() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        List<Long> currentSubIds = orgTypeService.getCurrentSubscriptionIds(orgAccessPoint.org)
        List<HashMap> linkedPlatforms = accessPointService.getLinkedPlatforms(params,orgAccessPoint)
        linkedPlatforms.each() {
            String qry2 = """
            SELECT distinct s from Subscription s
            JOIN s.packages as sp
            JOIN sp.pkg as pkg
            JOIN pkg.tipps as tipps
            WHERE s.id in (:currentSubIds)
            AND tipps.platform.id = ${it.platform.id}
            AND NOT EXISTS 
            (SELECT ioapl from OrgAccessPointLink ioapl
                WHERE ioapl.active=true and ioapl.subPkg=sp and ioapl.oap is null)"""
            if (params.checked == "true"){
                qry2 += " AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"
            }
            ArrayList linkedSubs = Subscription.executeQuery(qry2, [currentSubIds: currentSubIds])
            it['linkedSubs'] = linkedSubs
        }
        render(template: "linked_platforms_table", model: [linkedPlatforms: linkedPlatforms, params:params, accessPoint: orgAccessPoint])
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        Org organisation = accessService.checkPerm("ORG_CONSORTIUM") ? Org.get(params.id) : contextService.getOrg()
        result.institution = contextService.getOrg()
        result.contextCustomerType = result.institution.getCustomerType()
        result.orgInstance = organisation
        result.inContextOrg = result.orgInstance.id == contextService.getOrg().id
        result.availableOptions = accessPointService.availableOptions(organisation)
        if (params.template) {
            RefdataValue accessMethod = RefdataValue.getByValueAndCategory(params.template, RDConstants.ACCESS_POINT_TYPE)
            render(template: 'createAccessPoint', model: [accessMethod: accessMethod, availableOptions : result.availableOptions])
        } else {
            result.accessMethod = params.accessMethod ? RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE) : RDStore.ACCESS_POINT_TYPE_IP
            result
        }

    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processCreate() {
        RefdataValue accessMethod = (RefdataValue) genericOIDService.resolveOID(params.accessMethod)
        Map<String,Object> ctrlResult = accessPointControllerService.createAccessPoint(params,accessMethod)
        if (ctrlResult.status == AccessPointControllerService.STATUS_ERROR) {
            flash.message = ctrlResult.result.error
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }
        else {
            redirect controller: 'accessPoint', action: 'edit_'+ctrlResult.result.accessPoint.accessMethod.value, id: ctrlResult.result.accessPoint.id
            return
        }
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def delete() {
        Map<String,Object> ctrlResult = accessPointControllerService.delete(params)
        if(ctrlResult.status == AccessPointControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                redirect(url: request.getHeader("referer"))
                return
            }
        }
        else {
            flash.message = ctrlResult.result.message
            redirect controller: 'organisation', action: 'accessPoints', id: ctrlResult.result.orgId
            return
        }
    }

    @Secured(['ROLE_USER'])
    def edit_ip() {
        _edit()
        /*
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        Org org = orgAccessPoint.org
        Long orgId = org.id
        Org contextOrg = contextService.org
        boolean inContextOrg = (orgId == contextOrg.id)

        if (params.exportXLSX) {
            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtil.getSDF_NoTimeNoPoint()
            String datetoday = sdf.format(new Date(System.currentTimeMillis()))
            String filename = "${datetoday}_" + escapeService.escapeString(orgAccessPoint.name)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints([orgAccessPoint], contextService.org)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else {

            Boolean autofocus = (params.autofocus) ? true : false
            Boolean activeChecksOnly = (params.checked == 'false') ? false : true

            Map<String, Object> accessPointDataList = orgAccessPoint.getAccessPointIpRanges()

            orgAccessPoint.getAllRefdataValues(RDConstants.IPV6_ADDRESS_FORMAT)

            List<Long> currentSubIds = orgTypeService.getCurrentSubscriptionIds(orgAccessPoint.org)

            String sort = params.sort ?: "LOWER(p.name)"
            String order = params.order ?: "ASC"
            String qry1 = "select new map(p as platform,oapl as aplink) from Platform p join p.oapp as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id} and oapl.subPkg is null order by ${sort} ${order}"

            ArrayList<HashMap> linkedPlatforms = Platform.executeQuery(qry1)
            linkedPlatforms.each() {
                String qry2 = """
            SELECT distinct s from Subscription s
            JOIN s.packages as sp
            JOIN sp.pkg as pkg
            JOIN pkg.tipps as tipps
            WHERE s.id in (:currentSubIds)
            AND tipps.platform.id = ${it.platform.id}
            AND NOT EXISTS 
            (SELECT ioapl from OrgAccessPointLink ioapl
                WHERE ioapl.active=true and ioapl.subPkg=sp and ioapl.oap is null)
"""
                if (activeChecksOnly) {
                    qry2 += " AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"
                }
                ArrayList linkedSubs = Subscription.executeQuery(qry2, [currentSubIds: currentSubIds])
                it['linkedSubs'] = linkedSubs
            }

            String qry3 = """
            Select p, sp, s from Platform p
            JOIN p.oapp as oapl
            JOIN oapl.subPkg as sp
            JOIN sp.subscription as s
            WHERE oapl.active=true and oapl.oap=${orgAccessPoint.id}
            AND s.id in (:currentSubIds) 
            AND EXISTS (SELECT 1 FROM OrgAccessPointLink ioapl 
                where ioapl.subPkg=oapl.subPkg and ioapl.platform=p and ioapl.oap is null)
            AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}    
"""
            ArrayList linkedPlatformSubscriptionPackages = Platform.executeQuery(qry3, [currentSubIds: currentSubIds])

            return [
                    accessPoint                       : orgAccessPoint,
                    accessPointDataList               : accessPointDataList,
                    orgId                             : orgId,
                    platformList                      : orgAccessPoint.getNotLinkedPlatforms(),
                    linkedPlatforms                   : linkedPlatforms,
                    linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages,
                    ip                                : params.ip,
                    editable                          : ((accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))),
                    autofocus                         : autofocus,
                    orgInstance                       : orgAccessPoint.org,
                    inContextOrg                      : inContextOrg,
                    activeSubsOnly                    : activeChecksOnly,
                    institution                       : contextOrg
            ]
        }
        */
    }

    @Secured(['ROLE_USER'])
    def edit_vpn() {
        _edit()
    }

    @Secured(['ROLE_USER'])
    def edit_oa() {
        _edit()
    }

    @Secured(['ROLE_USER'])
    def edit_proxy() {
        _edit()
    }

    @Secured(['ROLE_USER'])
    def edit_ezproxy() {
        _edit()
    }

    @Secured(['ROLE_USER'])
    def edit_shibboleth() {
        _edit()
    }

    private _edit() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        Org org = orgAccessPoint.org
        Long orgId = org.id
        Org contextOrg = contextService.getOrg()
        boolean inContextOrg = (orgId == contextOrg.id)
        if (params.exportXLSX) {
            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_NoTimeNoPoint()
            String datetoday = sdf.format(new Date(System.currentTimeMillis()))
            String filename = "${datetoday}_" + escapeService.escapeString(orgAccessPoint.name)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints([orgAccessPoint], contextService.getOrg())
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
        }
        else {
            Boolean autofocus = (params.autofocus) ? true : false
            Boolean activeChecksOnly = (params.checked == 'false')
            Map<String, Object> accessPointDataList = orgAccessPoint.getAccessPointIpRanges()
            List<Long> currentSubIds = orgTypeService.getCurrentSubscriptionIds(orgAccessPoint.org)
            orgAccessPoint.getAllRefdataValues(RDConstants.IPV6_ADDRESS_FORMAT)
            List<HashMap> linkedPlatforms = accessPointService.getLinkedPlatforms(params, orgAccessPoint)
            linkedPlatforms.each() {
                String qry2 = """
            SELECT distinct s from Subscription s
            JOIN s.packages as sp
            JOIN sp.pkg as pkg
            JOIN pkg.tipps as tipps
            WHERE s.id in (:currentSubIds)
            AND tipps.platform.id = ${it.platform.id}
            AND NOT EXISTS 
            (SELECT ioapl from OrgAccessPointLink ioapl
                WHERE ioapl.active=true and ioapl.subPkg=sp and ioapl.oap is null)"""
                if (activeChecksOnly) {
                    qry2 += " AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"
                }
                ArrayList linkedSubs = Subscription.executeQuery(qry2, [currentSubIds: currentSubIds])
                it['linkedSubs'] = linkedSubs
            }
            ArrayList linkedPlatformSubscriptionPackages = []
            if(currentSubIds) {
                String qry3 = """
            Select p, sp, s from Platform p
            JOIN p.oapp as oapl
            JOIN oapl.subPkg as sp
            JOIN sp.subscription as s
            WHERE oapl.active=true and oapl.oap=${orgAccessPoint.id}
            AND s.id in (:currentSubIds) 
            AND EXISTS (SELECT 1 FROM OrgAccessPointLink ioapl 
                where ioapl.subPkg=oapl.subPkg and ioapl.platform=p and ioapl.oap is null)
            AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"""
                linkedPlatformSubscriptionPackages.addAll(Platform.executeQuery(qry3, [currentSubIds: currentSubIds]))
            }
            return [
                    accessPoint                       : orgAccessPoint,
                    accessPointDataList               : accessPointDataList,
                    rgId                              : orgId,
                    platformList                      : orgAccessPoint.getNotLinkedPlatforms(),
                    linkedPlatforms                   : linkedPlatforms,
                    linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages,
                    ip                                : params.ip,
                    editable                          : ((accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))),
                    autofocus                         : autofocus,
                    orgInstance                       : orgAccessPoint.org,
                    inContextOrg                      : inContextOrg,
                    contextCustomerType               : contextOrg.getCustomerType(),
                    activeSubsOnly                    : activeChecksOnly,
                    institution                       : contextOrg,
                    tab                               : (params.tab ?: "IPv4")
            ]
        }
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def deleteIpRange() {
        accessPointService.deleteIpRange(AccessPointData.get(params.id))
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkPlatform() {
        Map<String,Object> result = accessPointService.linkPlatform(params)
        if(result.error)
            flash.error = result.error
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def unlinkPlatform() {
        Map<String,Object> result = accessPointService.unlinkPlatform(OrgAccessPointLink.get(params.id))
        if(result.error)
            flash.error = result.error
        redirect(url: request.getHeader('referer'))
    }
}
