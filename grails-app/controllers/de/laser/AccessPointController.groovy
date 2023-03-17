package de.laser


import de.laser.ctrl.AccessPointControllerService
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
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
    AccessService accessService
    ContextService contextService
    GenericOIDService genericOIDService
    EscapeService escapeService
    OrgTypeService orgTypeService


    //static allowedMethods = [create: ['GET', 'POST'], delete: ['GET', 'POST'], dynamicSubscriptionList: ['POST'], dynamicPlatformList: ['POST']]

    /**
     * Adds a new IP range with the given parameters. The distinction between v4 and v6 just as the validation is done in the service
     * @see AccessPointControllerService#addIpRange(grails.web.servlet.mvc.GrailsParameterMap)
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation(CustomerTypeService.PERMS_BASIC, "INST_EDITOR")
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
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation(CustomerTypeService.PERMS_BASIC, "INST_EDITOR")
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

    /**
     * Loads a list of platforms linked to the given {@link OrgAccessPoint}
     * @return a table view listing the resulting platforms
     */
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

    /**
     * Opens the access point creation view form the specified access type; if an IP access point should be configured, already existing ones are filtered out
     * @return the access point creation view which in turn outputs the fragment with the fields which need to be filled out
     * @see OrgAccessPoint
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX(CustomerTypeService.PERMS_BASIC, "INST_EDITOR", "ROLE_ADMIN")
    })
    def create() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        Org organisation = accessService.checkPerm("ORG_CONSORTIUM_BASIC") ? Org.get(params.id) : contextService.getOrg()
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

    /**
     * Takes the given input parameters and builds a new access point for the institution
     * @return the edit view in case of success, the creation form page otherwise
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX(CustomerTypeService.PERMS_BASIC, "INST_EDITOR", "ROLE_ADMIN")
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
            redirect controller: 'accessPoint', action: 'edit_'+ctrlResult.result.accessPoint.accessMethod.value.toLowerCase(), id: ctrlResult.result.accessPoint.id
            return
        }
    }

    /**
     * Handles the deletion call of the given access point to the service
     * @return the list of institution's access points in case of success
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX(CustomerTypeService.PERMS_BASIC, "INST_EDITOR", "ROLE_ADMIN")
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
    @Secured(['ROLE_USER'])
    def edit_ip() {
        _edit("ip")
    }

    /**
     * Handles the call for editing a VPN based access point
     * @return the VPN editing view
     * @see de.laser.oap.OrgAccessPointVpn
     */
    @Secured(['ROLE_USER'])
    def edit_vpn() {
        _edit("vpn")
    }

    /**
     * Handles the call for editing an OpenAthens based access point
     * @return the OpenAthens editing view
     * @see de.laser.oap.OrgAccessPointOA
     */
    @Secured(['ROLE_USER'])
    def edit_oa() {
        _edit("oa")
    }

    /**
     * Handles the call for editing a proxy based access point
     * @return the proxy editing view
     * @see de.laser.oap.OrgAccessPointProxy
     */
    @Secured(['ROLE_USER'])
    def edit_proxy() {
        _edit("proxy")
    }

    /**
     * Handles the call for editing an OCLC EZProxy based access point
     * @return the EZProxy editing view
     * @see de.laser.oap.OrgAccessPointEzproxy
     */
    @Secured(['ROLE_USER'])
    def edit_ezproxy() {
        _edit("ezproxy")
    }

    /**
     * Handles the call for editing a Shibboleth based access point
     * @return the Shibboleth editing view
     * @see de.laser.oap.OrgAccessPointShibboleth
     */
    @Secured(['ROLE_USER'])
    def edit_shibboleth() {
        _edit("shibboleth")
    }

    /**
     * Handles the call for editing a Mail-Domain based access point
     * @return the Mail-Domain editing view
     * @see de.laser.oap.OrgAccessPoint
     */
    @Secured(['ROLE_USER'])
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
        Org contextOrg = contextService.getOrg()
        boolean inContextOrg = (orgId == contextOrg.id)
        if (params.exportXLSX) {
            SXSSFWorkbook wb
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + escapeService.escapeString(orgAccessPoint.name)
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb = (SXSSFWorkbook) accessPointService.exportAccessPoints([orgAccessPoint], contextService.getOrg())
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
            List<Long> currentSubIds = orgTypeService.getCurrentSubscriptionIds(orgAccessPoint.org)
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
                    editable                          : ((accessService.checkPermAffiliation('ORG_INST_BASIC', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM_BASIC', 'INST_EDITOR'))),
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

    /**
     * Handles the deletion call for the given IP range to the service
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation(CustomerTypeService.PERMS_BASIC, "INST_EDITOR")
    })
    def deleteAccessPointData() {
        Org organisation = accessService.checkPerm("ORG_CONSORTIUM_BASIC") ? Org.get(params.id) : contextService.getOrg()
        boolean inContextOrg = organisation.id == contextService.getOrg().id

        if(((accessService.checkPermAffiliation('ORG_INST_BASIC', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM_BASIC', 'INST_EDITOR')))){
            accessPointService.deleteAccessPointData(AccessPointData.get(params.id))
        }else {
            flash.error = message(code: 'default.noPermissions') as String
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Controller call to link an access point to a given platform
     */
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation(CustomerTypeService.PERMS_BASIC, "INST_EDITOR")
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
    @DebugInfo(perm=CustomerTypeService.PERMS_BASIC, affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation(CustomerTypeService.PERMS_BASIC, "INST_EDITOR")
    })
    def unlinkPlatform() {
        Map<String,Object> result = accessPointService.unlinkPlatform(OrgAccessPointLink.get(params.id))
        if(result.error)
            flash.error = result.error
        redirect(url: request.getHeader('referer'))
    }
}
