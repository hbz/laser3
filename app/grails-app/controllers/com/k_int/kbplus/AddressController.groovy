package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured

import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class AddressController extends AbstractDebugController {

	def springSecurityService
	def addressbookService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_ADMIN'])
    def list() {
		params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [addressInstanceList: Address.list(params), addressInstanceTotal: Address.count()]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
			case 'GET':
				[addressInstance: new Address(params)]
				break
			case 'POST':
				def addressInstance = new Address(params)
				if (! addressInstance.save(flush: true)) {
					if (params.redirect) {
						redirect(url: request.getHeader('referer'), params: params)
					} else {
						render view: 'create', model: [addressInstance: addressInstance]
					}
					return
	        	}

			flash.message = message(code: 'default.created.message', args: [message(code: 'address.label', default: 'Address'), addressInstance.id])
			if (params.redirect) {
				redirect(url: request.getHeader('referer'), params: params)
			}
			else {
				redirect action: 'show', id: addressInstance.id
			}
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
        def addressInstance = Address.get(params.id)
        if (! addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }

        [
            addressInstance: addressInstance,
            editable: addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser())
        ] // TODO
    }

    @Deprecated
    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'address', action: 'show', params: params
        return // ----- deprecated

        def addressInstance = Address.get(params.id)
        if (! addressInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

		switch (request.method) {
		case 'GET':
	        [addressInstance: addressInstance]
			break
		case 'POST':
	        if (params.version) {
	            def version = params.version.toLong()
	            if (addressInstance.version > version) {
	                addressInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'address.label', default: 'Address')] as Object[],
	                          "Another user has updated this Address while you were editing")
	                render view: 'edit', model: [addressInstance: addressInstance]
	                return
	            }
	        }

	        addressInstance.properties = params

	        if (! addressInstance.save(flush: true)) {
	            render view: 'edit', model: [addressInstance: addressInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'address.label', default: 'Address'), addressInstance.id])
	        redirect action: 'show', id: addressInstance.id
			break
		}
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def addressInstance = Address.get(params.id)
        if (! addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isAddressEditable(addressInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

        try {
            addressInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
