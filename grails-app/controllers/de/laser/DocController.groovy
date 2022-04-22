package de.laser


import de.laser.auth.User
 
import de.laser.helper.AppUtils
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.core.GrailsClass
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocController  {

	ContextService contextService

    static allowedMethods = [delete: 'POST']

	@Deprecated
	@Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

	@Deprecated
	@Secured(['ROLE_ADMIN'])
    def list() {
      	Map<String, Object> result = [:]
      	result.user = contextService.getUser()

		params.max = params.max ?: result.user?.getDefaultPageSize()

      	result.docInstanceList = Doc.list(params)
      	result.docInstanceTotal = Doc.count()
      	result
    }

	@Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def show() {
		Doc docInstance = Doc.get(params.id)
        if (!docInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label'), params.id])
            redirect action: 'list'
            return
        }

        [docInstance: docInstance]
    }

	/**
	 * Creates a new note for a {@link Subscription}, {@link License} or {@link Org}
	 */
	@Secured(['ROLE_USER'])
	@Transactional
	def createNote() {
		log.debug("Create note referer was ${request.getHeader('referer')} or ${request.request.RequestURL}")

		User user = contextService.getUser()
		GrailsClass domain_class = AppUtils.getDomainClass( params.ownerclass )

		if (domain_class) {
			def instance = domain_class.getClazz().get(params.ownerid)
			if (instance) {
				log.debug("Got owner instance ${instance}")

				Doc doc_content = new Doc(contentType: Doc.CONTENT_TYPE_STRING,
						title: params.licenseNoteTitle,
						content: params.licenseNote,
						type: RDStore.DOC_TYPE_NOTE,
						owner: contextService.getOrg(),
						user: user).save()

				log.debug("Setting new context type to ${params.ownertp}..")

				DocContext doc_context = new DocContext(
						"${params.ownertp}": instance,
						owner: doc_content,
						doctype: RDStore.DOC_TYPE_NOTE)
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
	@DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
	def editNote() {
		Doc.withTransaction {
			switch (request.method) {
				case 'POST':
					Doc docInstance = Doc.get(params.id)
					if (!docInstance) {
						flash.message = message(code: 'default.not.found.message', args: [message(code: 'default.note.label'), params.id])
						//redirect action: 'list'
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
							//render view: 'edit', model: [docInstance: docInstance]
							redirect(url: request.getHeader('referer'))
							return
						}
					}

					docInstance.properties = params
					if (!docInstance.owner)
						docInstance.owner = contextService.getOrg()

					if (!docInstance.save()) {
						//render view: 'edit', model: [docInstance: docInstance]
						redirect(url: request.getHeader('referer'))
						return
					}

					flash.message = message(code: 'default.updated.message', args: [message(code: 'default.note.label'), docInstance.title])
					//redirect action: 'show', id: docInstance.id
					redirect(url: request.getHeader('referer'))
					return
					break
			}
		}
	}

	/**
	 * Deletes the {@link Doc} given by params.id
	 */
	@DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
		Doc.withTransaction {
			Doc docInstance = Doc.get(params.id)
			if (! docInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label'), params.id])
				redirect action: 'list'
				return
			}

			try {
				docInstance.delete()
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'doc.label'), params.id])
				redirect action: 'list'
				return
			}
			catch (DataIntegrityViolationException e) {
				flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'doc.label'), params.id])
				redirect action: 'show', id: params.id
				return
			}
		}
    }
}
