package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.Role
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

	// NO CUSTOMER_TYPE / PERM required

	ContextService contextService
	AccessService accessService

	/**
	 * Creates a new note for a {@link Subscription}, {@link License} or {@link Org}
	 */
	@DebugInfo(isInstEditor = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
	def createNote() {
		// processing form#modalCreateNote
		String referer = request.getHeader('referer')
//		log.debug("Create note referer was ${referer} or ${request.request.RequestURL}")

		Doc.withTransaction {
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

					DocContext docctx = new DocContext(
							"${params.ownertp}": instance,
							owner: doc_content)
					docctx.save()
				}
				else {
					flash.error = message(code: 'default.save.error.general.message')
				}
			}
			else {
				flash.error = message(code: 'default.save.error.general.message')
			}
		}

		redirect(url: referer)
	}

	/**
	 * Edits an already existing note. The note to edit is given by params.id
	 */
	@DebugInfo(isInstEditor = [], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
	def editNote() {
		// processing form#modalEditNote
		String referer = request.getHeader('referer')

		Doc.withTransaction {
			DocContext docctx = DocContext.get(params.long('dctx'))
			if (accessService.hasAccessToDocNote(docctx, AccessService.WRITE)) {

				Doc doc = docctx.owner
				if (doc) {
					if (params.version) {
						Long version = params.long('version')
						if (doc.version > version) {
							doc.errors.rejectValue(
									'version',
									'default.optimistic.locking.failure',
									[message(code: 'default.note.label')] as Object[],
									'Another user has updated this Doc while you were editing'
							)
							redirect(url: referer)
							return
						}
					}
					doc.properties = params

					if (!doc.owner)
						doc.owner = contextService.getOrg()

					if (doc.save()) {
						flash.message = message(code: 'default.updated.message', args: [message(code: 'default.note.label'), doc.title])
					}
					else {
						flash.error = message(code: 'default.save.error.general.message')
					}
				}
				else {
					flash.error = message(code: 'default.not.found.message', args: [message(code: 'default.note.label'), params.id])
				}
			}
			else {
				flash.error = message(code: 'default.noPermissions')
			}
		}

		redirect(url: referer)
	}

	@DebugInfo(isInstEditor = [])
	@Secured(closure = {
		ctx.contextService.isInstEditor()
	})
	def deleteNote() {
		log.debug("deleteNote: ${params}")

		if (params.deleteId) {
			DocContext docctx = DocContext.get(params.deleteId)

			if (accessService.hasAccessToDocNote(docctx, AccessService.WRITE)) {
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
		}
		else {
			redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId
		}
	}
}
