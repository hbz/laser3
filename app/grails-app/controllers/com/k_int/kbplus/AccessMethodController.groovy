package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*
import grails.plugin.springsecurity.annotation.Secured

class AccessMethodController {

    def springSecurityService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'GET']
    

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
            //params.validFrom =  sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }
        if (params.validTo) {
            params.validTo = sdf.parse(params.validTo)
        } else {
            //params.validTo =sdf.parse(new Date().format( 'dd.MM.yyyy' ));
        }
        def AccessMethodInstance = new PlatformAccessMethod(params);

        if (params.validTo && params.validFrom && params.validTo.before(params.validFrom)) {
            flash.message = message(code: 'accessMethod.dateValidationError', args: [message(code: 'AccessMethod.label', default: 'Access Method'), AccessMethodInstance.accessMethod])
        } else {

            AccessMethodInstance.platform = Platform.get(params.platfId)
            AccessMethodInstance.platf = Platform.get(params.platfId)

            AccessMethodInstance.accessMethod = RefdataValue.get(params.accessMethod)

            AccessMethodInstance.save(flush: true)
            AccessMethodInstance.errors.toString()

            flash.message = message(code: 'default.created.message', args: [message(code: 'AccessMethod.label', default: 'Access Method'), AccessMethodInstance.accessMethod])
        }

        if (params.redirect) {
            redirect(url: request.getHeader('referer'), params: params)
        }
        else {
            redirect controller: 'platform', action: 'AccessMethods', id: params.platfId
        }
        
        //[personInstanceList: Person.list(params), personInstanceTotal: Person.count()]
    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
         def AccessMethodInstance = PlatformAccessMethod.get(params.id)
        
        def platform = AccessMethodInstance.platf;
        def platformId = platform.id;
        
        if (!AccessMethodInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }

        try {
            AccessMethodInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect controller: 'platform', action: 'AccessMethods', id: platformId
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'show', id: params.id
        }
        
    }
    
}
