package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class IdentifierController {

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
        [identifierInstanceList: Identifier.list(params), identifierInstanceTotal: Identifier.count()]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

		switch (request.method) {
		case 'GET':
        	[identifierInstance: new Identifier(params)]
			break
		case 'POST':
	        def identifierInstance = new Identifier(params)
	        if (!identifierInstance.save(flush: true)) {
	            render view: 'create', model: [identifierInstance: identifierInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'identifier.label', default: 'Identifier'), identifierInstance.id])
	        redirect action: 'show', id: identifierInstance.id
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {

        def identifierInstance = Identifier.get(params.id)
        if (!identifierInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
            redirect action: 'list'
            return
        }

        [identifierInstance: identifierInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

		switch (request.method) {
		case 'GET':
	        def identifierInstance = Identifier.get(params.id)
	        if (!identifierInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [identifierInstance: identifierInstance]
			break
		case 'POST':
	        def identifierInstance = Identifier.get(params.id)
	        if (!identifierInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (identifierInstance.version > version) {
	                identifierInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'identifier.label', default: 'Identifier')] as Object[],
	                          "Another user has updated this Identifier while you were editing")
	                render view: 'edit', model: [identifierInstance: identifierInstance]
	                return
	            }
	        }

	        identifierInstance.properties = params

	        if (!identifierInstance.save(flush: true)) {
	            render view: 'edit', model: [identifierInstance: identifierInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'identifier.label', default: 'Identifier'), identifierInstance.id])
	        redirect action: 'show', id: identifierInstance.id
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        redirect controller: 'home', action: 'index'
        return // ----- deprecated

        def identifierInstance = Identifier.get(params.id)
        if (!identifierInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
            redirect action: 'list'
            return
        }

        try {
            identifierInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'identifier.label', default: 'Identifier'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
