package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*
import grails.plugin.springsecurity.annotation.Secured

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
               
        
        def accessPointInstance = new OrgAccessPoint(params);
        
        accessPointInstance.org = Org.get(1)
        accessPointInstance.organisation = Org.get(1)
        
        accessPointInstance.save(flush: true)
        accessPointInstance.errors.toString()
        
        flash.message = message(code: 'default.created.message', args: [message(code: 'accessPint.label', default: 'Access Point'), accessPointInstance.name])
        if (params.redirect) {
            redirect(url: request.getHeader('referer'), params: params)
        }
        else {
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
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit_ip() {
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

                //accessPointInstance.org = Org.get(1)
                //accessPointInstance.organisation = Org.get(1)
                accessPointInstance.save(flush: true)

                //flash.message = message(code: 'default.updated.message', args: [message(code: 'person.label', default: 'Person'), personInstance.id])
                redirect controller: 'organisations', action: 'accessPoints', id: orgId
                break
        }
    }
}