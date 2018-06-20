package com.k_int.kbplus

import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
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
            def accessPointInstance = new OrgAccessPoint(params);

            accessPointInstance.org = organisation
            accessPointInstance.organisation = organisation

            accessPointInstance.save(flush: true)
            accessPointInstance.errors.toString()

            flash.message = message(code: 'accessPoint.create.message', args: [accessPointInstance.name])
            redirect controller: 'accessPoint', action: 'edit_' + accessPointInstance.accessMethod.value.toLowerCase() , id: accessPointInstance.id
        }

    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
         def accessPointInstance = OrgAccessPoint.get(params.id)
        
        def org = accessPointInstance.org;
        def orgId = org.id;
        
        if (!accessPointInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }

        try {
            accessPointInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'accessPoint.label', default: 'Access Point'), accessPointInstance.name])
            redirect controller: 'organisations', action: 'accessPoints', id: orgId
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), accessPointInstance.id])
            redirect action: 'show', id: params.id
            coun   }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_ip() {
        def orgAccessPoint = OrgAccessPoint.get(params.id)

        def org = orgAccessPoint.org;
        def orgId = org.id;

        def accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);

//        IpRangeCollection ipRanges = new IpRangeCollection();
//        for (data in accessPointDataList) {
//            IpRange ipRange = IpRange.parseIpRange(data.data);
//            ipRanges.add(ipRange)
//        }
//        ipRanges = ipRanges.compact();

        switch (request.method) {
            case 'GET':
                [accessPointInstance: orgAccessPoint, accessPointDataList: accessPointDataList, orgId: orgId, ip: params.ip]
                break
            case 'POST':
                orgAccessPoint.properties = params;
                orgAccessPoint.save(flush: true)

                redirect controller: 'organisations', action: 'accessPoints', orgId: orgId
                break
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])

    def addIP() {
        try {
            IpRange.parseIpRange(params.ip)
            def orgAccessPoint = OrgAccessPoint.get(params.id)
            def org = orgAccessPoint.org;
            def orgId = org.id;

            def accessPointData = new AccessPointData(params)
            accessPointData.orgAccessPoint = orgAccessPoint
            accessPointData.datatype="IP"
            accessPointData.data = params.ip
            accessPointData.save(flush: true)

            redirect controller: 'accessPoint', action: 'edit_ip', id: params.id
        } catch (InvalidRangeException) {
            flash.error = message(code: 'accessPoint.invalid.ip', args: [params.ip])

            redirect controller: 'accessPoint', action: 'edit_ip', id: params.id, params: [ip: params.ip]
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def deleteData() {
        def accessPointData = AccessPointData.get(params.id)
        def accessPointInstance = accessPointData.orgAccessPoint;
        accessPointData.delete(flush: true)

        redirect controller: 'accessPoint', action: 'edit_ip', id: accessPointInstance.id
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_proxy() {
        def accessPointInstance = OrgAccessPoint.get(params.id)

        def org = accessPointInstance.org;
        def orgId = org.id;

        switch (request.method) {
            case 'GET':
                [accessPointInstance: accessPointInstance]
                break
            case 'POST':

                accessPointInstance.properties = params;

                accessPointInstance.save(flush: true)

                //flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
                redirect controller: 'organisations', action: 'accessPoints', id: orgId
                break
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_shibboleth() {
        def accessPointInstance = OrgAccessPoint.get(params.id)

        def org = accessPointInstance.org;
        def orgId = org.id;

        switch (request.method) {
            case 'GET':
                [accessPointInstance: accessPointInstance]
                break
            case 'POST':

                accessPointInstance.properties = params;

                //accessPointInstance.org = Org.get(1)
                //accessPointInstance.organisation = Org.get(1)
                accessPointInstance.save(flush: true)

                //flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
                redirect controller: 'organisations', action: 'accessPoints', id: orgId
                break
        }
    }

}