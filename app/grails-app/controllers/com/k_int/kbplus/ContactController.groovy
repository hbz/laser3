package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException

class ContactController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [contactInstanceList: Contact.list(params), contactInstanceTotal: Contact.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[contactInstance: new Contact(params)]
			break
		case 'POST':
	        def contactInstance = new Contact(params)
	        if (!contactInstance.save(flush: true)) {
	            render view: 'create', model: [contactInstance: contactInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'contact.label', default: 'Contact'), contactInstance.id])
	        redirect action: 'show', id: contactInstance.id
			break
		}
    }

    def show() {
        def contactInstance = Contact.get(params.id)
        if (!contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
            return
        }

        [contactInstance: contactInstance]
    }

    def edit() {
		switch (request.method) {
		case 'GET':
	        def contactInstance = Contact.get(params.id)
	        if (!contactInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [contactInstance: contactInstance]
			break
		case 'POST':
	        def contactInstance = Contact.get(params.id)
	        if (!contactInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (contactInstance.version > version) {
	                contactInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'contact.label', default: 'Contact')] as Object[],
	                          "Another user has updated this Contact while you were editing")
	                render view: 'edit', model: [contactInstance: contactInstance]
	                return
	            }
	        }

	        contactInstance.properties = params

	        if (!contactInstance.save(flush: true)) {
	            render view: 'edit', model: [contactInstance: contactInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'contact.label', default: 'Contact'), contactInstance.id])
	        redirect action: 'show', id: contactInstance.id
			break
		}
    }

    def delete() {
        def contactInstance = Contact.get(params.id)
        if (!contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
            return
        }

        try {
            contactInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
