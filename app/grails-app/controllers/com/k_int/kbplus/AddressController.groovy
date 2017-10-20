package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import grails.plugins.springsecurity.Secured

import org.springframework.dao.DataIntegrityViolationException

class AddressController {

	def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
		if (! params.max) {
			User user   = springSecurityService.getCurrentUser()
			params.max = user?.getDefaultPageSize()
		}
        [addressInstanceList: Address.list(params), addressInstanceTotal: Address.count()]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
		switch (request.method) {
		case 'GET':
        	[addressInstance: new Address(params)]
			break
		case 'POST':
	        def addressInstance = new Address(params)
	        if (!addressInstance.save(flush: true)) {
	            render view: 'create', model: [addressInstance: addressInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'address.label', default: 'Address'), addressInstance.id])
	        redirect action: 'show', id: addressInstance.id
			break
		}
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
        def addressInstance = Address.get(params.id)
        if (!addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
            return
        }

        [addressInstance: addressInstance]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
		switch (request.method) {
		case 'GET':
	        def addressInstance = Address.get(params.id)
	        if (!addressInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [addressInstance: addressInstance]
			break
		case 'POST':
	        def addressInstance = Address.get(params.id)
	        if (!addressInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
	            redirect action: 'list'
	            return
	        }

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

	        if (!addressInstance.save(flush: true)) {
	            render view: 'edit', model: [addressInstance: addressInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'address.label', default: 'Address'), addressInstance.id])
	        redirect action: 'show', id: addressInstance.id
			break
		}
    }
    
    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
        def addressInstance = Address.get(params.id)
        if (!addressInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'address.label', default: 'Address'), params.id])
            redirect action: 'list'
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
