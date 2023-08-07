package de.laser

import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

/**
 * This controller manages calls for contact entity manipulation
 * @see Contact
 * @see Person
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class ContactController  {

	AddressbookService addressbookService
	ContextService contextService
	FormService formService

    static allowedMethods = [create: ['GET', 'POST'], delete: 'POST']

	/**
	 * index call, redirecting to the context institution's addressbook
	 */
	@Secured(['ROLE_USER'])
	def index() {
		redirect controller: 'myInstitution', action: 'addressbook'
	}

	/**
	 * Creating a new contact entity: takes the given parameters and constructs a new contact entity with them
	 * @return the contact view in case of success or returning to the creation page with an error message
	 */
	@DebugInfo(isInstEditor_or_ROLEADMIN = [], wtc = DebugInfo.WITH_TRANSACTION)
	@Secured(closure = {
		ctx.contextService.isInstEditor_or_ROLEADMIN()
	})
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
							flash.error = message(code: 'contact.create.email.error') as String
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

					flash.message = message(code: 'default.created.message', args: [message(code: 'contact.label'), contactInstance.id]) as String
					if (params.redirect) {
						redirect(url: request.getHeader('referer'), params: params)
						return
					} else {
						redirect action: 'show', id: contactInstance.id
						return
					}
					break
			}
		}
    }

	/**
	 * Deletes the given contact entity
	 * @return the contact list in case of success; the details view otherwise
	 */
	@DebugInfo(isInstEditor_or_ROLEADMIN = [], wtc = DebugInfo.WITH_TRANSACTION)
	@Secured(closure = {
		ctx.contextService.isInstEditor_or_ROLEADMIN()
	})
    def delete() {
		Contact.withTransaction {
			Contact contactInstance = Contact.get(params.id)
			if (!contactInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'contact.label'), params.id]) as String
				redirect action: 'list'
				return
			}
			if (!addressbookService.isContactEditable(contactInstance, contextService.getUser())) {
				redirect action: 'show', id: params.id
				return
			}

			try {
				contactInstance.delete()
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'contact.label'), params.id]) as String
				redirect action: 'list'
				return
			}
			catch (DataIntegrityViolationException e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'contact.label'), params.id]) as String
				redirect action: 'show', id: params.id
				return 
			}
		}
    }
}
