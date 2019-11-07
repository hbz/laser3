package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
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


    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: ['GET', 'POST']]


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def addIpRange() {
        def orgAccessPoint = OrgAccessPoint.get(params.id)
        def org = orgAccessPoint.org;
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
                def isDuplicate = false;
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

            def accessPointData = new AccessPointData(params)
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

        def organisation = contextService.getOrg()
        params.orgInstance = organisation

        def availableLanguageKeys = ['accessPoint.option.remoteAccess', 'accessPoint.option.woRemoteAccess']
        def supportedLocales = ['en', 'de']
        Map localizedAccessPointNameSuggestions = [:]
        supportedLocales.each { locale ->
            availableLanguageKeys.each { key ->
                localizedAccessPointNameSuggestions[message(code : key, locale : locale)] = key
            }
        }
        def existingOapIpInstances = OrgAccessPoint.findAllByOrgAndAccessMethod(organisation, RefdataValue.findByValue('ip'))

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
            resultList.add(["${message(code : it)}" : "${message(code : it)}"])
        }
        resultList.add(["${message(code : 'accessPoint.option.customName')}" : ''])
        return resultList
    }


    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        def organisation = contextService.getOrg()
        params.orgInstance = organisation
        params.availableIpOptions = availableIPOptions()

        if (params.template) {
            def accessMethod = RefdataValue.findByValue(params.template)
            return render(template: 'create_' + accessMethod, model: [accessMethod: accessMethod, availableIpOptions : params.availableIpOptions])
        } else {
            if (!params.accessMethod) {
                params.accessMethod = RefdataValue.findByValue('ip');
            }
            params.accessMethod = RefdataValue.findByValue(params.accessMethod);

            return params
        }
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def create_ip() {
        // without the org somehow passed we can only create AccessPoints for the context org
        def orgInstance = contextService.getOrg()
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
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, 'Access Point Type')
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
        def orgInstance = contextService.getOrg()
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
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, 'Access Point Type')
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
        def orgInstance = contextService.getOrg()

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
            def accessPoint = new OrgAccessPoint();
            accessPoint.org = orgInstance
            accessPoint.name = params.name
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, 'Access Point Type')
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
        def orgInstance = contextService.getOrg()

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
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, 'Access Point Type')
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
        def orgInstance = contextService.getOrg()

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
            accessPoint.accessMethod = RefdataValue.getByValueAndCategory(params.accessMethod, 'Access Point Type')
            accessPoint.save(flush: true)

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_'+accessPoint.accessMethod.value, id: accessPoint.id
        }
    }

    @Secured(closure = { ctx.accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (ctx.accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && OrgAccessPoint.get(request.getRequestURI().split('/').last()).org == ctx.contextService.getOrg())
    })
    def delete() {
        def accessPoint = OrgAccessPoint.get(params.id)

        def org = accessPoint.org;
        def orgId = org.id;

        if (!accessPoint) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }

        try {
            accessPoint.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'accessPoint.label', default: 'Access Point'), accessPoint.name])
            redirect controller: 'organisation', action: 'accessPoints', id: orgId
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), accessPoint.id])
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

        def orgAccessPoint = OrgAccessPoint.get(params.id)

        String ipv4Format = (params.ipv4Format) ? params.ipv4Format : 'v4ranges'
        String ipv6Format = (params.ipv6Format) ? params.ipv6Format : 'v6ranges'
        Boolean autofocus = (params.autofocus) ? true : false

        //String ipv4Format = 'range'

        def org = orgAccessPoint.org;
        def orgId = org.id;

        def accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

        orgAccessPoint.getAllRefdataValues('IPv6 Address Formats')

        def ipv4Ranges = orgAccessPoint.getIpRangeStrings('ipv4', ipv4Format.substring(2))
        def ipv6Ranges = orgAccessPoint.getIpRangeStrings('ipv6', ipv6Format.substring(2))

        List currentSubIds = orgTypeService.getCurrentSubscriptions(contextService.getOrg()).collect{ it.id }

//        String qry = ""
//        qry = "select distinct(platf), spoap" +
//                " from " +
//                "    TitleInstancePackagePlatform tipp join tipp.pkg pkg join tipp.platform platf, " +
//                "    SubscriptionPackage subPkg join subPkg.subscriptionPackageOrgAccessPoint  spoap join spoap.orgAccessPoint ap" +
//                " where " +
//                "    subPkg.pkg = pkg and " +
//                "    ap.id  = :accessPointId "
//
//        def qryParams = [
//                accessPointId : params.id as Long
//        ]
//        List linkedPlatformsMap = Platform.executeQuery(qry,qryParams)
        def sort = params.sort ?: "LOWER(p.name)"
        def order = params.order ?: "ASC"

        def hql = "select new map(p as platform,oapl as aplink) from Platform p join p.oapp as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id} order by ${sort} ${order}"
        def linkedPlatformsMap = Platform.executeQuery(hql)

        def linkedSubscriptionsQuery = "select new map(s as subscription,oapl as aplink) from Subscription s join s.oapl as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id}"
        def linkedSubscriptionsMap = Subscription.executeQuery(linkedSubscriptionsQuery)

        switch (request.method) {
            case 'GET':
                [accessPoint           : orgAccessPoint, accessPointDataList: accessPointDataList, orgId: orgId,
                 platformList          : orgAccessPoint.getNotLinkedPlatforms(),
                 linkedPlatformsMap    : linkedPlatformsMap,
                 linkedSubscriptionsMap: linkedSubscriptionsMap,
                 ip                    : params.ip, editable: true,
                 ipv4Ranges            : ipv4Ranges, ipv4Format: ipv4Format,
                 ipv6Ranges            : ipv6Ranges, ipv6Format: ipv6Format,
                 autofocus             : autofocus,
                 orgInstance           : orgAccessPoint.org,
                 inContextOrg          : orgId == contextService.org.id
                ]
                break
            case 'POST':
                orgAccessPoint.properties = params;
                orgAccessPoint.save(flush: true)

                redirect controller: 'organisation', action: 'accessPoints', orgId: orgId
                break
        }
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
            def existingActiveAP = OrgAccessPointLink.findAll {
                active == true && platform == oapl.platform && oap == accessPoint
            }
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
