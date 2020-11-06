package de.laser

import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class ContactController  {

	def addressbookService
	ContextService contextService
	FormService formService

    static allowedMethods = [create: ['GET', 'POST'], delete: 'POST']

	@Secured(['ROLE_USER'])
	def index() {
		redirect controller: 'myInstitution', action: 'addressbook'
	}

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def create() {
		Contact.withTransaction {
			switch (request.method) {
				case 'GET':
					[contactInstance: new Contact(params)]
					break
				case 'POST':
					Contact contactInstance = new Contact(params)

					if (RDStore.CCT_EMAIL == contactInstance.contentType) {
						if (!formService.validateEmailAddress(contactInstance.content)) {
							flash.error = message(code: 'contact.create.email.error')
							redirect(url: request.getHeader('referer'))
							return
						}
					}

					if (!contactInstance.save()) {
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
            editable: addressbookService.isContactEditable(contactInstance, contextService.getUser())
		] // TODO
    }

	@Deprecated
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def edit() {
		redirect controller: 'contact', action: 'show', params: params
		return // ----- deprecated
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def delete() {
		Contact.withTransaction {
			Contact contactInstance = Contact.get(params.id)
			if (!contactInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label'), params.id])
				redirect action: 'list'
				return
			}
			if (!addressbookService.isContactEditable(contactInstance, contextService.getUser())) {
				redirect action: 'show', id: params.id
				return
			}

			try {
				contactInstance.delete()
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'contact.label'), params.id])
				redirect action: 'list'
			}
			catch (DataIntegrityViolationException e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'contact.label'), params.id])
				redirect action: 'show', id: params.id
			}
		}
    }
}
