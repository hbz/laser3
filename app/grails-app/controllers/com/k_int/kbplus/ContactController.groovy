package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured

import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class ContactController extends AbstractDebugController {

	def springSecurityService
	def addressbookService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER'])
    def list() {
		params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSizeTMP()
        [contactInstanceList: Contact.list(params), contactInstanceTotal: Contact.count()]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
		case 'GET':
        	[contactInstance: new Contact(params)]
			break
		case 'POST':
	        def contactInstance = new Contact(params)
	        if (! contactInstance.save(flush: true)) {
				if (params.redirect) {
					redirect(url: request.getHeader('referer'), params: params)
				} else {
					render view: 'create', model: [contactInstance: contactInstance]
				}
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'contact.label', default: 'Contact'), contactInstance.id])
			if (params.redirect) {
				redirect(url: request.getHeader('referer'), params: params)
			} else {
				redirect action: 'show', id: contactInstance.id
			}
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
        def contactInstance = Contact.get(params.id)
        if (! contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
            return
        }

		[
            contactInstance: contactInstance,
            editable: addressbookService.isContactEditable(contactInstance, springSecurityService.getCurrentUser())
		] // TODO
    }

	@Deprecated
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		redirect controller: 'contact', action: 'show', params: params
		return // ----- deprecated

        def contactInstance = Contact.get(params.id)
        if (! contactInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isContactEditable(contactInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

		switch (request.method) {
		case 'GET':
	        [contactInstance: contactInstance]
			break
		case 'POST':
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

	        if (! contactInstance.save(flush: true)) {
	            render view: 'edit', model: [contactInstance: contactInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'contact.label', default: 'Contact'), contactInstance.id])
	        redirect action: 'show', id: contactInstance.id
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def contactInstance = Contact.get(params.id)
        if (! contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label', default: 'Contact'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isContactEditable(contactInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
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
