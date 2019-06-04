package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.uni_freiburg.ub.IpRange
import groovy.json.JsonOutput
import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import grails.plugin.springsecurity.annotation.Secured

class AccessPointController extends AbstractDebugController {

    def springSecurityService
    def contextService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: ['GET','POST']]
    

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        
        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
        if (params.validFrom) {
            params.validFrom = sdf.parse(params.validFrom)
        } else {
            params.validFrom = new Date();
        }
        if (params.validTo) {
            params.validTo = sdf.parse(params.validTo)
        }

        def organisation = Org.get(params.orgId);

        def oap = OrgAccessPoint.findAllByNameAndOrg(params.name, organisation)

        if (oap) {
            flash.error = message(code: 'accessPoint.duplicate.error', args: [params.name])
            redirect(url: request.getHeader('referer'), params: params)
        } else {
            def accessPoint = new OrgAccessPoint(params);

            accessPoint.org = organisation

            accessPoint.save(flush: true)
            accessPoint.errors.toString()

            flash.message = message(code: 'accessPoint.create.message', args: [accessPoint.name])
            redirect controller: 'accessPoint', action: 'edit_' + accessPoint.accessMethod.value.toLowerCase() , id: accessPoint.id
        }

    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
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
        def orgAccessPoint = OrgAccessPoint.get(params.id)

        String ipv4Format = (params.ipv4Format) ? params.ipv4Format : 'v4cidr'
        String ipv6Format = (params.ipv6Format) ? params.ipv6Format : 'v6cidr'
        Boolean autofocus = (params.autofocus) ? true : false

        //String ipv4Format = 'range'

        def org = orgAccessPoint.org;
        def orgId = org.id;

        def accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

        orgAccessPoint.getAllRefdataValues('IPv6 Address Formats')

        def ipv4Ranges = orgAccessPoint.getIpRangeStrings('ipv4', ipv4Format.substring(2))
        def ipv6Ranges = orgAccessPoint.getIpRangeStrings('ipv6', ipv6Format.substring(2))

        def hql = "select new map(p as platform,oapl as aplink) from Platform p join p.oapp as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id}"
        def linkedPlatformsMap = Platform.executeQuery(hql)
        def linkedSubscriptionsQuery = "select new map(s as subscription,oapl as aplink) from Subscription s join s.oapl as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id}"
        def linkedSubscriptionsMap = Subscription.executeQuery(linkedSubscriptionsQuery)

        switch (request.method) {
            case 'GET':
                [accessPoint: orgAccessPoint, accessPointDataList: accessPointDataList, orgId: orgId,
                 platformList: orgAccessPoint.getNotLinkedPlatforms(),
                 linkedPlatformsMap: linkedPlatformsMap,
                 subscriptionList: orgAccessPoint.getNotLinkedSubscriptions(),
                 linkedSubscriptionsMap: linkedSubscriptionsMap,
                 ip: params.ip, editable: true,
                 ipv4Ranges: ipv4Ranges, ipv4Format: ipv4Format,
                 ipv6Ranges: ipv6Ranges, ipv6Format: ipv6Format,
                 autofocus: autofocus,
                 orgInstance: orgAccessPoint.org
                ]
                break
            case 'POST':
                orgAccessPoint.properties = params;
                orgAccessPoint.save(flush: true)

                redirect controller: 'organisation', action: 'accessPoints', orgId: orgId
                break
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def addIpRange() {
        try {
            def ipRange = IpRange.parseIpRange(params.ip)
            def orgAccessPoint = OrgAccessPoint.get(params.id)
            def org = orgAccessPoint.org;
            def orgId = org.id;

            def jsonData = JsonOutput.toJson([
                    inputStr : params.ip,
                    startValue: ipRange.lowerLimit.toHexString(),
                    endValue: ipRange.upperLimit.toHexString()]
            )

            def accessPointData = new AccessPointData(params)
            accessPointData.orgAccessPoint = orgAccessPoint
            accessPointData.datatype= 'ip' + ipRange.getIpVersion()
            accessPointData.data = jsonData
            accessPointData.save(flush: true)

            orgAccessPoint.lastUpdated = new Date()
            orgAccessPoint.save(flush: true)

            redirect controller: 'accessPoint', action: 'edit_ip', id: params.id, params: [ipv4Format: params.ipv4Format, ipv6Format: params.ipv6Format, autofocus: true]
        } catch (InvalidRangeException) {
            flash.error = message(code: 'accessPoint.invalid.ip', args: [params.ip])

            redirect controller: 'accessPoint', action: 'edit_ip', id: params.id, params: [ip: params.ip, ipv4Format: params.ipv4Format, ipv6Format: params.ipv6Format]
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def linkSubscription() {
        def accessPoint = OrgAccessPoint.get(params.id)
        def oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = accessPoint
        oapl.subscription = Subscription.get(params.subscriptions)
        def existingActiveAP = OrgAccessPointLink.findAll {
            active == true && subscription == oapl.subscription && oap == accessPoint
        }
        if (! existingActiveAP.isEmpty()){
            flash.error = "Existing active AccessPoint for Subscription"
            redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
        }
        if (! oapl.save()) {
            flash.error = "Could not link AccessPoint to Subscription"
            redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
        }
        redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def linkPlatform() {
        def accessPoint = OrgAccessPoint.get(params.id)
        def oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = accessPoint
        oapl.platform = Platform.get(params.platforms)
        def existingActiveAP = OrgAccessPointLink.findAll {
            active == true && platform == oapl.platform && oap == accessPoint
        }
        if (! existingActiveAP.isEmpty()){
            flash.error = "Existing active AccessPoint for platform"
            redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
        }
        if (! oapl.save()) {
            flash.error = "Could not link AccessPoint to Platform"
            redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
        }
        redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def deleteIpRange() {
        def accessPointData = AccessPointData.get(params.id)
        def accessPoint = accessPointData.orgAccessPoint;
        accessPointData.delete(flush: true)

        redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def removeAPLink() {
        def aoplInstance = OrgAccessPointLink.get(params.id)
        aoplInstance.active = false
        if (! aoplInstance.save()) {
            log.debug("Error updateing AccessPoint for platform")
            log.debug(aopl.errors)
            // TODO flash
        }
        redirect controller: 'accessPoint', action: 'edit_ip', id: aoplInstance.oap.id, params: [autofocus: true]
    }

}