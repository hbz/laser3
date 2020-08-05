package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.FormService
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class ContactController extends AbstractDebugController {

	def springSecurityService
	def addressbookService
	def contextService
	FormService formService

    static allowedMethods = [create: ['GET', 'POST'], delete: 'POST']

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
				Contact contactInstance = new Contact(params)

				if(RDStore.CCT_EMAIL == contactInstance.contentType){
					if ( !formService.validateEmailAddress(contactInstance.content) ) {
						flash.error = message(code:'contact.create.email.error')
						redirect(url: request.getHeader('referer'))
						return
					}
				}

	        if (! contactInstance.save(flush: true)) {
				if (params.redirect) {
					redirect(url: request.getHeader('referer'), params: params)
				} else {
					render view: 'create', model: [contactInstance: contactInstance]
				}
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'contact.label'), contactInstance.id])
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
		Contact contactInstance = Contact.get(params.id)
        if (! contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label'), params.id])
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
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
		Contact contactInstance = Contact.get(params.id)
        if (! contactInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label'), params.id])
            redirect action: 'list'
            return
        }
        if (! addressbookService.isContactEditable(contactInstance, springSecurityService.getCurrentUser())) {
            redirect action: 'show', id: params.id
            return
        }

        try {
            contactInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'contact.label'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'contact.label'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
