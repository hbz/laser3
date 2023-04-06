package de.laser


import de.laser.auth.User

import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.utils.CodeUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.core.GrailsClass
import org.springframework.dao.DataIntegrityViolationException

/**
 * This controller manages notes for subscriptions, licenses or organisations.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DocController  {

	ContextService contextService

    static allowedMethods = [delete: 'POST']

	/**
	 * Creates a new note for a {@link Subscription}, {@link License} or {@link Org}
	 */
	@Secured(['ROLE_USER'])
	@Transactional
	def createNote() {
		log.debug("Create note referer was ${request.getHeader('referer')} or ${request.request.RequestURL}")

		User user = contextService.getUser()
		Class dc = CodeUtils.getDomainClass( params.ownerclass )

		if (dc) {
			def instance = dc.get(params.ownerid)
			if (instance) {
				log.debug("Got owner instance ${instance}")

				Doc doc_content = new Doc(contentType: Doc.CONTENT_TYPE_STRING,
						title: params.noteTitle,
						content: params.noteContent,
						type: RDStore.DOC_TYPE_NOTE,
						owner: contextService.getOrg(),
						user: user).save()

				log.debug("Setting new context type to ${params.ownertp}..")

				DocContext doc_context = new DocContext(
						"${params.ownertp}": instance,
						owner: doc_content)
				doc_context.save()
			}
			else {
				log.debug("no instance")
			}
		}
		else {
			log.debug("no type")
		}

		redirect(url: request.getHeader('referer'))
	}

	/**
	 * Edits an already existing note. The note to edit is given by params.id
	 */
	@DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'], wtc = DebugInfo.WITH_TRANSACTION)
	@Secured(closure = {
		ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN("INST_EDITOR")
	})
	def editNote() {
		Doc.withTransaction {
			switch (request.method) {
				case 'POST':
					Doc docInstance = Doc.get(params.id)
					if (!docInstance) {
						flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.note.label'), params.id]) as String
						redirect(url: request.getHeader('referer'))
						return
					}

					if (params.version) {
						Long version = params.long('version')
						if (docInstance.version > version) {
							docInstance.errors.rejectValue(
									'version',
									'default.optimistic.locking.failure',
									[message(code: 'default.note.label')] as Object[],
									'Another user has updated this Doc while you were editing'
							)
							redirect(url: request.getHeader('referer'))
							return
						}
					}

					docInstance.properties = params
					if (!docInstance.owner)
						docInstance.owner = contextService.getOrg()

					if (!docInstance.save()) {
						redirect(url: request.getHeader('referer'))
						return
					}

					flash.message = message(code: 'default.updated.message', args: [message(code: 'default.note.label'), docInstance.title]) as String
					redirect(url: request.getHeader('referer'))
					return
					break
			}
		}
	}

	/**
	 * Deletes the {@link Doc} given by params.id
	 */
	@DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'], wtc = DebugInfo.WITH_TRANSACTION)
	@Secured(closure = {
		ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN("INST_EDITOR")
	})
    def delete() {
		Doc.withTransaction {
			Doc docInstance = Doc.get(params.id)
			if (! docInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label'), params.id]) as String
				redirect action: 'list'
				return
			}

			try {
				docInstance.delete()
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'doc.label'), params.id]) as String
				redirect action: 'list'
				return
			}
			catch (DataIntegrityViolationException e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'doc.label'), params.id]) as String
				redirect action: 'show', id: params.id
				return
			}
		}
    }
}
