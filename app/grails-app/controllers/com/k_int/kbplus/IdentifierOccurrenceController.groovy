package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class IdentifierOccurrenceController {

	def springSecurityService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
		redirect controller: 'home', action: 'index'
		return // ----- deprecated

        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER'])
    def list() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

		params.max = params.max ?: ((User) springSecurityService.getCurrentUser())?.getDefaultPageSize()
        [identifierOccurrenceInstanceList: IdentifierOccurrence.list(params), identifierOccurrenceInstanceTotal: IdentifierOccurrence.count()]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

		switch (request.method) {
		case 'GET':
        	[identifierOccurrenceInstance: new IdentifierOccurrence(params)]
			break
		case 'POST':
	        def identifierOccurrenceInstance = new IdentifierOccurrence(params)
	        if (!identifierOccurrenceInstance.save(flush: true)) {
	            render view: 'create', model: [identifierOccurrenceInstance: identifierOccurrenceInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), identifierOccurrenceInstance.id])
	        redirect action: 'show', id: identifierOccurrenceInstance.id
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

        def identifierOccurrenceInstance = IdentifierOccurrence.get(params.id)
        if (!identifierOccurrenceInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
            redirect action: 'list'
            return
        }

        [identifierOccurrenceInstance: identifierOccurrenceInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

		switch (request.method) {
		case 'GET':
	        def identifierOccurrenceInstance = IdentifierOccurrence.get(params.id)
	        if (!identifierOccurrenceInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [identifierOccurrenceInstance: identifierOccurrenceInstance]
			break
		case 'POST':
	        def identifierOccurrenceInstance = IdentifierOccurrence.get(params.id)
	        if (!identifierOccurrenceInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (identifierOccurrenceInstance.version > version) {
	                identifierOccurrenceInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence')] as Object[],
	                          "Another user has updated this IdentifierOccurrence while you were editing")
	                render view: 'edit', model: [identifierOccurrenceInstance: identifierOccurrenceInstance]
	                return
	            }
	        }

	        identifierOccurrenceInstance.properties = params

	        if (!identifierOccurrenceInstance.save(flush: true)) {
	            render view: 'edit', model: [identifierOccurrenceInstance: identifierOccurrenceInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), identifierOccurrenceInstance.id])
	        redirect action: 'show', id: identifierOccurrenceInstance.id
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

        def identifierOccurrenceInstance = IdentifierOccurrence.get(params.id)
        if (!identifierOccurrenceInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
            redirect action: 'list'
            return
        }

        try {
            identifierOccurrenceInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'identifierOccurrence.label', default: 'IdentifierOccurrence'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
