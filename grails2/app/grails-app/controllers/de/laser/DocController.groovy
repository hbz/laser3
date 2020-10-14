package de.laser


import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.AppUtils
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsClass
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocController extends AbstractDebugController {

	def springSecurityService
	def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

	@Secured(['ROLE_ADMIN'])
    def list() {
      	Map<String, Object> result = [:]
      	result.user = User.get(springSecurityService.principal.id)

		params.max = params.max ?: result.user?.getDefaultPageSize()

      	result.docInstanceList = Doc.list(params)
      	result.docInstanceTotal = Doc.count()
      	result
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		redirect controller: 'doc', action: 'show', params: params
		return // ----- deprecated
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def show() {
		Doc docInstance = Doc.get(params.id)
        if (!docInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label'), params.id])
            redirect action: 'list'
            return
        }

        [docInstance: docInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		redirect controller: 'doc', action: 'show', params: params
		return // ----- deprecated
    }

	@Secured(['ROLE_USER'])
	def createNote() {
		log.debug("Create note referer was ${request.getHeader('referer')} or ${request.request.RequestURL}")

		User user = User.get(springSecurityService.principal.id)
		GrailsClass domain_class = AppUtils.getDomainClass( params.ownerclass )

		if (domain_class) {
			def instance = domain_class.getClazz().get(params.ownerid)
			if (instance) {
				log.debug("Got owner instance ${instance}")

				Doc doc_content = new Doc(contentType: Doc.CONTENT_TYPE_STRING,
						title: params.licenseNoteTitle,
						content: params.licenseNote,
						type: RDStore.DOC_TYPE_NOTE,
						owner: contextService.org,
						user: user).save(flush:true)

				log.debug("Setting new context type to ${params.ownertp}..")

				DocContext doc_context = new DocContext(
						"${params.ownertp}": instance,
						owner: doc_content,
						doctype: RDStore.DOC_TYPE_NOTE)
				doc_context.save(flush:true)
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

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
	def editNote() {
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
						docInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
								[message(code: 'default.note.label')] as Object[],
								"Another user has updated this Doc while you were editing")
						//render view: 'edit', model: [docInstance: docInstance]
						redirect(url: request.getHeader('referer'))
						return
					}
				}

				docInstance.properties = params
				if(!docInstance.owner)
					docInstance.owner = contextService.org

				if (!docInstance.save(flush: true)) {
					//render view: 'edit', model: [docInstance: docInstance]
					redirect(url: request.getHeader('referer'))
					return
				}

				flash.message = message(code: 'default.updated.message', args: [message(code: 'default.note.label'), docInstance.title])
				//redirect action: 'show', id: docInstance.id
				redirect(url: request.getHeader('referer'))
				break
		}
	}

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
		Doc docInstance = Doc.get(params.id)
        if (!docInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label'), params.id])
            redirect action: 'list'
            return
        }

        try {
            docInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'doc.label'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'doc.label'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
