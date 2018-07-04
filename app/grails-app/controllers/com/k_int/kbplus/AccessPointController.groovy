package com.k_int.kbplus

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.net.util.SubnetUtils
import org.codehaus.groovy.grails.validation.routines.InetAddressValidator
import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*
import grails.plugin.springsecurity.annotation.Secured
import ubfr.Exception.InvalidRangeException
import ubfr.IpRange
import ubfr.IpRangeCollection

class AccessPointController {

    def springSecurityService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: ['GET','POST']]
    

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSize()
        [personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSize()
        
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
            redirect controller: 'organisations', action: 'accessPoints', id: orgId
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), accessPoint.id])
            redirect action: 'show', id: params.id
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_ip() {
        def orgAccessPoint = OrgAccessPoint.get(params.id)

        String ipv4Format = (params.ipv4Format) ? params.ipv4Format : 'cidr'
        String ipv6Format = (params.ipv6Format) ? params.ipv6Format : 'cidr'
        Boolean autofocus = (params.autofocus) ? true : false

        //String ipv4Format = 'range'

        def org = orgAccessPoint.org;
        def orgId = org.id;

        def accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

        orgAccessPoint.getAllRefdataValues('IPv6 Address Formats')


        def ipv4Ranges = orgAccessPoint.getIpRangeStrings('ipv4', ipv4Format)
        def ipv6Ranges = orgAccessPoint.getIpRangeStrings('ipv6', ipv6Format)

        switch (request.method) {
            case 'GET':
                [accessPoint: orgAccessPoint, accessPointDataList: accessPointDataList, orgId: orgId,
                 ip: params.ip, editable: true,
                 ipv4Ranges: ipv4Ranges, ipv4Format: ipv4Format,
                 ipv6Ranges: ipv6Ranges, ipv6Format: ipv6Format,
                        autofocus: autofocus
                ]
                break
            case 'POST':
                orgAccessPoint.properties = params;
                orgAccessPoint.save(flush: true)

                redirect controller: 'organisations', action: 'accessPoints', orgId: orgId
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
    def deleteIpRange() {
        def accessPointData = AccessPointData.get(params.id)
        def accessPoint = accessPointData.orgAccessPoint;
        accessPointData.delete(flush: true)

        redirect controller: 'accessPoint', action: 'edit_ip', id: accessPoint.id, params: [autofocus: true]
    }

}