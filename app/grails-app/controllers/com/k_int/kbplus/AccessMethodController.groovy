package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import org.springframework.dao.DataIntegrityViolationException
import com.k_int.kbplus.auth.User
import com.k_int.properties.*
import grails.plugin.springsecurity.annotation.Secured

class AccessMethodController extends AbstractDebugController {

    def springSecurityService
    def contextService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], update: ['GET', 'POST'], delete: 'GET']
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        
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
        def accessMethod = new PlatformAccessMethod(params)

        if (params.validTo && params.validFrom && params.validTo.before(params.validFrom)) {
            flash.error = message(code: 'accessMethod.dateValidationError', args: [message(code: 'accessMethod.label', default: 'Access Method'), accessMethod.accessMethod])
        } else {

            accessMethod.platf = Platform.get(params.platfId)

            accessMethod.accessMethod = RefdataValue.get(params.accessMethod)

            accessMethod.save(flush: true)
            accessMethod.errors.toString()

            flash.message = message(code: 'accessMethod.create.message', args: [accessMethod.accessMethod.getI10n('value')])
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
         def accessMethod = PlatformAccessMethod.get(params.id)
        
        def platform = accessMethod.platf
        def platformId = platform.id
        
        try {
            accessMethod.delete(flush: true)
			flash.message = message(code: 'accessMethod.deleted', args: [accessMethod.accessMethod.getI10n('value')])
            redirect controller: 'platform', action: 'AccessMethods', id: platformId
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'show', id: params.id
        }
    }


    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
        def accessMethod= PlatformAccessMethod.get(params.id)
        def platf = accessMethod.getPlatf()

        [accessMethod: accessMethod, platfId: platf.id]

    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def update() {
        def accessMethod= PlatformAccessMethod.get(params.id)
        def platf = accessMethod.getPlatf()


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

        if (params.validTo == "" || params.validTo && params.validFrom && params.validTo.before(params.validFrom)) {
            flash.error = message(code: 'accessMethod.dateValidationError', args: [message(code: 'accessMethod.label', default: 'Access Method'), accessMethod.accessMethod])
            redirect controller: 'accessMethod', action: 'edit', id: accessMethod.id
        } else {
            accessMethod.validTo = params.validTo
            accessMethod.lastUpdated = new Date()

            accessMethod.save(flush: true)
            accessMethod.errors.toString()

            flash.message = message(code: 'accessMethod.create.message', args: [accessMethod.accessMethod.getI10n('value')])
            flash.message = message(code: 'accessMethod.updated', args: [accessMethod.accessMethod.getI10n('value')])
            redirect controller: 'platform', action: 'accessMethods', id: platf.id
        }
    }
}
