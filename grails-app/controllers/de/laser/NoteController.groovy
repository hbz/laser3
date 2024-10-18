package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.utils.CodeUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages notes for subscriptions, licenses or organisations
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class NoteController {

	ContextService contextService
	AccessService accessService

	/**
	 * Creates a new note for a {@link Subscription}, {@link License} or {@link Org}
	 */
	@Secured(['ROLE_USER'])
	@Transactional
	def createNote() {
		String referer = request.getHeader('referer')

		// processing form#modalCreateNote
		log.debug("Create note referer was ${referer} or ${request.request.RequestURL}")

		User user = contextService.getUser()
		Class dc = CodeUtils.getDomainClass( params.ownerclass )

		if (dc) {
			def instance = dc.get(params.ownerid)
			if (instance) {
				log.debug("Got owner instance ${instance}")

				Doc doc_content = new Doc(
						contentType: Doc.CONTENT_TYPE_STRING,
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

		redirect(url: referer)
	}

	/**
	 * Edits an already existing note. The note to edit is given by params.id
	 */
	@DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor_or_ROLEADMIN()
	})
	def editNote() {
		// processing form#modalEditNote
		String referer = request.getHeader('referer')

		Doc.withTransaction {
			switch (request.method) {
				case 'POST':
					//					Doc docInstance = Doc.findByIdAndContentType(params.long('id'), Doc.CONTENT_TYPE_STRING)
					DocContext docContext = DocContext.get(params.long('dctx'))
					if (! accessService.hasAccessToDocNote(docContext)) {
						flash.error = message(code: 'default.noPermissions') as String
						redirect(url: referer)
						return
					}

					Doc docInstance = docContext.owner
					if (!docInstance) {
						flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.note.label'), params.id]) as String
						redirect(url: referer)
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
							redirect(url: referer)
							return
						}
					}

					docInstance.properties = params
					if (!docInstance.owner)
						docInstance.owner = contextService.getOrg()

					if (!docInstance.save()) {
						redirect(url: referer)
						return
					}

					flash.message = message(code: 'default.updated.message', args: [message(code: 'default.note.label'), docInstance.title]) as String
					redirect(url: referer)
					return
					break
			}
		}
	}

	@DebugInfo(isInstEditor_or_ROLEADMIN = [])
	@Secured(closure = {
		ctx.contextService.isInstEditor_or_ROLEADMIN()
	})
	def deleteNote() {
		log.debug("deleteNote: ${params}")

		if (params.deleteId) {
			DocContext docctx = DocContext.get(params.deleteId)

			if (accessService.hasAccessToDocNote(docctx)) {
				docctx.status = RDStore.DOC_CTX_STATUS_DELETED
				docctx.save()
				flash.message = message(code: 'default.deleted.general.message')
			}
			else {
				flash.error = message(code: 'default.noPermissions')
			}
		}

		if (params.redirectTab) {
			redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId, params: [tab: params.redirectTab] // subscription.membersSubscriptionsManagement
		} else {
			redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId
		}
	}
}
