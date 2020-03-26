package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.uni_freiburg.ub.IpRange
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonOutput
import org.springframework.dao.DataIntegrityViolationException

class AccessPointController extends AbstractDebugController {

    def springSecurityService
    def contextService

    def subscriptionsQueryService
    def orgTypeService


    static allowedMethods = [create: ['GET', 'POST'], delete: ['GET', 'POST'], dynamicSubscriptionList: ['POST']]


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def addIpRange() {
        def orgAccessPoint = OrgAccessPoint.get(params.id)
        Org org = orgAccessPoint.org;
        def orgId = org.id;
        // need to check if contextOrg == orgAccessPoint.org for ORG_CONSORTIUM? The template has no editable elements
        // in that context (would need to fake a post request), similar for deleteIpRange method.
        def validRanges = []
        def invalidRanges = []
        // allow multiple ip ranges as input (must be separated by whitespaces)
        def ipCol = params.ip.replaceAll("\\s+", " ").split(" ");
        for (range in ipCol) {
            try {
                // check if given input string is a valid ip range
                def ipRange = IpRange.parseIpRange(range)
                def accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

                // so far we know that the input string represents a valid ip range
                // check if the input string is already saved
                boolean isDuplicate = false
                for (accessPointData in accessPointDataList) {
                    if (accessPointData.getInputStr() == ipRange.toInputString()) {
                        isDuplicate = true
                    }
                }
                if (!isDuplicate) {
                    validRanges << ipRange
                }
            } catch (InvalidRangeException) {
                invalidRanges << range
            }
        }

        // persist all valid ranges
        for (ipRange in validRanges) {
            def jsonData = JsonOutput.toJson([
                    inputStr  : ipRange.toInputString(),
                    startValue: ipRange.lowerLimit.toHexString(),
                    endValue  : ipRange.upperLimit.toHexString()]
            )

            AccessPointData accessPointData = new AccessPointData(params)
            accessPointData.orgAccessPoint = orgAccessPoint
            accessPointData.datatype = 'ip' + ipRange.getIpVersion()
            accessPointData.data = jsonData
            accessPointData.save(flush: true)

            orgAccessPoint.lastUpdated = new Date()
            orgAccessPoint.save(flush: true)
        }

        if (invalidRanges) {
            // return only those input strings to UI which represent a invalid ip range
            flash.error = message(code: 'accessPoint.invalid.ip', args: [invalidRanges.join(' ')])
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod, id: params.id, params: [ip: invalidRanges.join(' '), ipv4Format: params.ipv4Format, ipv6Format: params.ipv6Format]
        } else {
            redirect controller: 'accessPoint', action: 'edit_' + params.accessMethod, id: params.id, params: [ipv4Format: params.ipv4Format, ipv6Format: params.ipv6Format, autofocus: true]
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }

    /**
     * Check for existing name in all supported locales and return available suggestions for IP Access Method
     * A simpler solution would be nice
     * TODO move out of controller
     * @return
     */
    private def availableIPOptions() {

        Org organisation = contextService.getOrg()
        params.orgInstance = organisation

        def availableLanguageKeys = ['accessPoint.option.remoteAccess', 'accessPoint.option.woRemoteAccess']
        def supportedLocales = ['en', 'de']
        Map localizedAccessPointNameSuggestions = [:]
        supportedLocales.each { locale ->
            availableLanguageKeys.each { key ->
                localizedAccessPointNameSuggestions[message(code : key, locale : locale)] = key
            }
        }
        def existingOapIpInstances = OrgAccessPoint.findAllByOrgAndAccessMethod(organisation, RefdataValue.getByValue('ip'))

        if (existingOapIpInstances) {
            existingOapIpInstances.each { it ->
             if (localizedAccessPointNameSuggestions.keySet().contains(it.name)){
                 if (localizedAccessPointNameSuggestions[it.name]) {
                     availableLanguageKeys.removeAll {languageKey ->
                         languageKey == localizedAccessPointNameSuggestions[it.name]
                     }
                 }
             }
            }
        }
        def resultList = []
        availableLanguageKeys.each { it ->
            resultList.add(["${it}" : "${message(code : it)}"])
        }
        resultList.add(["accessPoint.option.customName" : ''])
        return resultList
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def dynamicSubscriptionList() {
        List currentSubIds = orgTypeService.getCurrentSubscriptionIds(contextService.getOrg())
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        String qry = """
            Select p, sp, s from Platform p
            JOIN p.oapp as oapl
            JOIN oapl.subPkg as sp
            JOIN sp.subscription as s
            WHERE oapl.active=true and oapl.oap=${orgAccessPoint.id}
            AND s.id in (:currentSubIds)
            AND EXISTS (SELECT 1 FROM OrgAccessPointLink ioapl 
                where ioapl.subPkg=oapl.subPkg and ioapl.platform=p and ioapl.oap is null)
"""
        if (params.checked == "true"){
            qry += " AND s.status = ${RDStore.SUBSCRIPTION_CURRENT.id}"
        }

        ArrayList linkedPlatformSubscriptionPackages = Platform.executeQuery(qry, [currentSubIds: currentSubIds])
        return render(template: "linked_subs_table", model: [linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages, params:params])
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        Org organisation = contextService.getOrg()
        params.orgInstance = organisation
        params.availableIpOptions = availableIPOptions()

        if (params.template) {
            RefdataValue accessMethod = RefdataValue.getByValueAndCategory(params.template, RDConstants.ACCESS_POINT_TYPE)
            return render(template: 'create_' + accessMethod, model: [accessMethod: accessMethod, availableIpOptions : params.availableIpOptions])
        } else {
            if (!params.accessMethod) {
                params.accessMethod = RefdataValue.getByValueAndCategory('ip', RDConstants.ACCESS_POINT_TYPE).value
            }
            params.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE);
            return params
        }

    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_ip() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()
        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPoint();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_oa() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()
        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (! params.entityId) {
            flash.error = message(code: 'accessPoint.require.entityId')
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPointOA();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.entityId = params.entityId
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_proxy() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()
        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPoint();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_vpn() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)
        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPointVpn();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_ezproxy() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (! params.url) {
            flash.error = message(code: 'accessPoint.require.url', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)
        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPointEzproxy()
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.url = params.url
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_shibboleth() {
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = contextService.getOrg()

        if (! params.name) {
            flash.error = message(code: 'accessPoint.require.name')
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        if (! params.entityId) {
            flash.error = message(code: 'accessPoint.require.entityId')
            redirect(controller: "accessPoint", action: "create", params: params)
            return
        }

        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)
        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(controller: "accessPoint", action: "create", params: params)
        } else {
            def accessPoint = new OrgAccessPointShibboleth();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.entityId = params.entityId
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, RDConstants.ACCESS_POINT_TYPE)
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = { ctx.accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (ctx.accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && OrgAccessPoint.get(request.getRequestURI().split('/').last()).org == ctx.contextService.getOrg())
    })
    def delete() {
        OrgAccessPoint accessPoint = OrgAccessPoint.get(params.id)
        if (!accessPoint) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label'), params.id])
            redirect(url: request.getHeader("referer"))
            return
        }

        Org org = accessPoint.org;
        Long oapPlatformLinkCount = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNull(true, accessPoint)
        Long oapSubscriptionLinkCount = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNotNull(true, accessPoint)

        if ( oapPlatformLinkCount != 0 || oapSubscriptionLinkCount != 0){
            flash.message = message(code: 'accessPoint.list.deleteDisabledInfo', args: [oapPlatformLinkCount, oapSubscriptionLinkCount])
            redirect(url: request.getHeader("referer"))
            return
        }
        def orgId = org.id;

        try {
            accessPoint.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'accessPoint.label'), accessPoint.name])
            redirect controller: 'organisation', action: 'accessPoints', id: orgId
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label'), accessPoint.id])
            redirect action: 'show', id: params.id
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_ip() {
        _edit()
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_vpn() {
        _edit()
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_oa() {
        _edit()
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_proxy() {
        _edit()
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_ezproxy() {
        _edit()
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_shibboleth() {
        _edit()
    }

    def private _edit() {
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        Org org = orgAccessPoint.org;
        Long orgId = org.id;

        String ipv4Format = (params.ipv4Format) ? params.ipv4Format : 'v4ranges'
        String ipv6Format = (params.ipv6Format) ? params.ipv6Format : 'v6ranges'
        Boolean autofocus = (params.autofocus) ? true : false

        ArrayList accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

        orgAccessPoint.getAllRefdataValues(RDConstants.IPV6_ADDRESS_FORMAT)

        String[] ipv4Ranges = orgAccessPoint.getIpRangeStrings('ipv4', ipv4Format.substring(2))
        String[] ipv6Ranges = orgAccessPoint.getIpRangeStrings('ipv6', ipv6Format.substring(2))

        List currentSubIds = orgTypeService.getCurrentSubscriptionIds(contextService.getOrg())

        def sort = params.sort ?: "LOWER(p.name)"
        def order = params.order ?: "ASC"

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
             accessPoint           : orgAccessPoint, accessPointDataList: accessPointDataList, orgId: orgId,
             platformList          : orgAccessPoint.getNotLinkedPlatforms(),
             linkedPlatforms    : linkedPlatforms,
             linkedPlatformSubscriptionPackages : linkedPlatformSubscriptionPackages,
             ip                    : params.ip, editable: true,
             ipv4Ranges            : ipv4Ranges, ipv4Format: ipv4Format,
             ipv6Ranges            : ipv6Ranges, ipv6Format: ipv6Format,
             autofocus             : autofocus,
             orgInstance           : orgAccessPoint.org,
             inContextOrg          : orgId == contextService.org.id,
             activeSubsOnly        : true,
        ]
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def deleteIpRange() {
        def accessPointData = AccessPointData.get(params.id)
        def accessPoint = accessPointData.orgAccessPoint;
        accessPointData.delete(flush: true)

        redirect(url: request.getHeader('referer'))
        //redirect controller: 'accessPoint', action: 'edit_'+params.accessMethod, id: accessPoint.id, params: [autofocus: true]
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkPlatform() {
        def accessPoint = OrgAccessPoint.get(params.accessPointId)
        def oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = accessPoint
        if (params.platforms) {
            oapl.platform = Platform.get(params.platforms)
            String hql = "select oap from OrgAccessPoint oap " +
                "join oap.oapp as oapl where oapl.active = true and oapl.platform.id =${accessPoint.id} and oapl.oap=:oap and oapl.subPkg is null order by LOWER(oap.name)"
            def existingActiveAP = OrgAccessPoint.executeQuery(hql, ['oap' : accessPoint])

            if (! existingActiveAP.isEmpty()){
                flash.error = "Existing active AccessPoint for platform"
                redirect(url: request.getHeader('referer'))
                return
            }
            if (! oapl.save()) {
                flash.error = "Could not link AccessPoint to Platform"
            }
        }
        redirect(url: request.getHeader('referer'))
        //redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def unlinkPlatform() {
        def aoplInstance = OrgAccessPointLink.get(params.id)
        aoplInstance.active = false
        if (! aoplInstance.save()) {
            log.debug("Error updateing AccessPoint for platform")
            log.debug(aopl.errors)
            // TODO flash
        }
        redirect(url: request.getHeader('referer'))
        //redirect controller: 'accessPoint', action: 'edit_ip', id: aoplInstance.oap.id, params: [autofocus: true]
    }
}
