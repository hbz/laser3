package de.laser


import de.laser.ctrl.AccessPointControllerService
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import de.laser.wekb.Platform
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import java.text.SimpleDateFormat

/**
 * This controller is responsible for manipulating access points. See the domain definitions how access points are being used.
 * This controller has been migrated from Grails 2 to Grails 3; during that migration, object manipulation methods have been
 * moved to services. The service containing this controller's object-manipulating methods is {@link AccessPointControllerService}
 * @see OrgAccessPoint
 * @see de.uni_freiburg.ub.IpRange
 * @see AccessPointControllerService
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AccessPointController  {

    AccessPointControllerService accessPointControllerService
    AccessPointService accessPointService
    ContextService contextService
    EscapeService escapeService
    SubscriptionService subscriptionService

    //static allowedMethods = [create: ['GET', 'POST'], delete: ['GET', 'POST'], dynamicSubscriptionList: ['POST'], dynamicPlatformList: ['POST']]

    /**
     * Adds a new IP range with the given parameters. The distinction between v4 and v6 just as the validation is done in the service
     * @see AccessPointControllerService#addIpRange(grails.web.servlet.mvc.GrailsParameterMap)
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
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

    /**
     * Adds a new Mail Domain with the given parameters.
     * @see AccessPointControllerService#addMailDomain(grails.web.servlet.mvc.GrailsParameterMap)
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def addMailDomain() {
        Map<String,Object> ctrlResult = accessPointControllerService.addMailDomain(params)
        if (ctrlResult.status == AccessPointControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod.toLowerCase(), id: params.id
            return
        } else {
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod.toLowerCase(), id: params.id
        }
    }

    /**
     * Loads a list of subscriptions linked to the given {@link OrgAccessPoint}
     * @returns a table view listing the resulting subscriptions
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def dynamicSubscriptionList() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        List<Long> currentSubIds = subscriptionService.getCurrentSubscriptionIds(orgAccessPoint.org)
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

        Org org = orgAccessPoint.org
        Long orgId = org.id
        boolean inContextOrg = (orgId == contextService.getOrg().id)
        ArrayList linkedPlatformSubscriptionPackages = Platform.executeQuery(qry, [currentSubIds: currentSubIds])
        render(template: "linked_subs_table", model: [linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages, inContextOrg: inContextOrg, params:params])
    }

    /**
     * Loads a list of platforms linked to the given {@link OrgAccessPoint}
     * @return a table view listing the resulting platforms
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def dynamicPlatformList() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        List<Long> currentSubIds = subscriptionService.getCurrentSubscriptionIds(orgAccessPoint.org)
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
            ArrayList linkedSubs = Subscription.executeQuery(qry2 + ' order by s.name', [currentSubIds: currentSubIds])
            it['linkedSubs'] = linkedSubs
        }
        Org org = orgAccessPoint.org
        Long orgId = org.id
        boolean inContextOrg = (orgId == contextService.getOrg().id)

        render(template: "linked_platforms_table", model: [linkedPlatforms: linkedPlatforms, inContextOrg: inContextOrg,  params:params, accessPoint: orgAccessPoint])
    }

    /**
     * Opens the access point creation view form the specified access type; if an IP access point should be configured, already existing ones are filtered out
     * @return the access point creation view which in turn outputs the fragment with the fields which need to be filled out
     * @see OrgAccessPoint
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def create() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        Org organisation = contextService.getOrg().isCustomerType_Consortium() ? Org.get(params.id) : contextService.getOrg()
        result.institution = contextService.getOrg()
        result.contextCustomerType = contextService.getOrg().getCustomerType()
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

    /**
     * Takes the given input parameters and builds a new access point for the institution
     * @return the edit view in case of success, the creation form page otherwise
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def processCreate() {
        RefdataValue accessMethod = RefdataValue.get(params.long('accessMethod'))
        Map<String,Object> ctrlResult = accessPointControllerService.createAccessPoint(params,accessMethod)
        if (ctrlResult.status == AccessPointControllerService.STATUS_ERROR) {
            flash.message = ctrlResult.result.error
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }
        else {
            redirect controller: 'accessPoint', action: 'edit_'+ctrlResult.result.accessPoint.accessMethod.value.toLowerCase(), id: ctrlResult.result.accessPoint.id
            return
        }
    }

    /**
     * Handles the deletion call of the given access point to the service
     * @return the list of institution's access points in case of success
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
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

    /**
     * Handles the call for editing an IP based access point
     * @return the IP editing view
     * @see OrgAccessPointLink
     * @see de.uni_freiburg.ub.Ipv4Address
     * @see de.uni_freiburg.ub.Ipv6Address
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_ip() {
        _edit("ip")
    }

    /**
     * Handles the call for editing an OpenAthens based access point
     * @return the OpenAthens editing view
     * @see de.laser.oap.OrgAccessPointOA
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_oa() {
        _edit("oa")
    }

    /**
     * Handles the call for editing a proxy based access point
     * @return the proxy editing view
     * @see de.laser.oap.OrgAccessPointProxy
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_proxy() {
        _edit("proxy")
    }

    /**
     * Handles the call for editing an OCLC EZProxy based access point
     * @return the EZProxy editing view
     * @see de.laser.oap.OrgAccessPointEzproxy
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_ezproxy() {
        _edit("ezproxy")
    }

    /**
     * Handles the call for editing a Shibboleth based access point
     * @return the Shibboleth editing view
     * @see de.laser.oap.OrgAccessPointShibboleth
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_shibboleth() {
        _edit("shibboleth")
    }

    /**
     * Handles the call for editing a Mail-Domain based access point
     * @return the Mail-Domain editing view
     * @see de.laser.oap.OrgAccessPoint
     */
    @DebugInfo(isInstEditor = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def edit_maildomain() {
        _edit("mailDomain")
    }

    /**
     * Collects the different edit calls and returns the data. It is also possible to export attached access methods to the given point type;
     * in that case, the attached access methods are being collected and printed to a table
     * @return the editing view for the given access point or the export table if an export parameter has been specified
     */
    private _edit(String accessMethod) {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        Org org = orgAccessPoint.org
        Long orgId = org.id
        boolean inContextOrg = (orgId == contextService.getOrg().id)
        if (params.exportXLSX) {
            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + escapeService.escapeString(orgAccessPoint.name)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints([orgAccessPoint], ExportClickMeService.FORMAT.XLS)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else {
            Map<String, Object> accessPointDataList = [:]
            Boolean autofocus = (params.autofocus) ? true : false
            Boolean activeChecksOnly = (params.checked == 'false')

            if(accessMethod in ['ip', 'ezproxy', 'proxy']) {
                accessPointDataList = orgAccessPoint.getAccessPointIpRanges()
            }else if(accessMethod == 'mailDomain'){
                accessPointDataList = orgAccessPoint.getAccessPointMailDomains()
            }

            List<Long> currentSubIds = subscriptionService.getCurrentSubscriptionIds(orgAccessPoint.org)
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
                ArrayList linkedSubs = Subscription.executeQuery(qry2 + ' order by s.name, s.status', [currentSubIds: currentSubIds])
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
                    platformList                      : orgAccessPoint.getNotLinkedPlatforms(),
                    linkedPlatforms                   : linkedPlatforms,
                    linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages,
                    ip                                : params.ip,
                    editable                          : contextService.is_INST_EDITOR_with_PERMS_BASIC( inContextOrg ),
                    autofocus                         : autofocus,
                    orgInstance                       : orgAccessPoint.org,
                    inContextOrg                      : inContextOrg,
                    contextCustomerType               : contextService.getOrg().getCustomerType(),
                    activeSubsOnly                    : activeChecksOnly,
                    institution                       : contextService.getOrg(),
                    tab                               : (params.tab ?: "IPv4")
            ]
        }
    }

    /**
     * Handles the deletion call for the given IP range to the service
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def deleteAccessPointData() {
        Org organisation = contextService.getOrg().isCustomerType_Consortium() ? Org.get(params.orgInstance) : contextService.getOrg()
        boolean inContextOrg = organisation.id == contextService.getOrg().id

        if (contextService.is_INST_EDITOR_with_PERMS_BASIC( inContextOrg )){
            accessPointService.deleteAccessPointData(AccessPointData.get(params.id))
        } else {
            flash.error = message(code: 'default.noPermissions') as String
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Controller call to link an access point to a given platform
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def linkPlatform() {
        Map<String,Object> result = accessPointService.linkPlatform(params)
        if(result.error)
            flash.error = result.error
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Controller call to unlink an access point from a given platform
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unlinkPlatform() {
        Map<String,Object> result = accessPointService.unlinkPlatform(OrgAccessPointLink.get(params.id))
        if(result.error)
            flash.error = result.error
        redirect(url: request.getHeader('referer'))
    }
}
