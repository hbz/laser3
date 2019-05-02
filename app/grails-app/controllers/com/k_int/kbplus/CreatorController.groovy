package com.k_int.kbplus


import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.SpringSecurityUtils

import org.springframework.security.access.annotation.Secured
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class CreatorController extends AbstractDebugController {

	def springSecurityService
	def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@Secured(['ROLE_DATAMANAGER'])
    def index() {
        redirect action: 'list', params: params
    }

	@Secured(['ROLE_DATAMANAGER'])
    def list() {
		if (! params.max) {
			User user   = springSecurityService.getCurrentUser()
			params.max = user?.getDefaultPageSizeTMP()
		}
        [creatorInstanceList: Creator.list(params), creatorInstanceTotal: Creator.count()]
    }

	@Secured(['ROLE_DATAMANAGER'])
    def create() {
		switch (request.method) {
		case 'GET':
        	[creatorInstance: new Creator(params)]
			break
		case 'POST':
	        def creatorInstance = new Creator(params)
	        if (! creatorInstance.save(flush: true)) {
	            render view: 'create', model: [creatorInstance: creatorInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'creator.label', default: 'Creator'), creatorInstance.id])
	        redirect action: 'show', id: creatorInstance.id
			break
		}
    }

	@Secured(['ROLE_DATAMANAGER'])
    def show() {
        def creatorInstance = Creator.get(params.id)
        if (! creatorInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
            redirect action: 'list'
            return
        }

        [
				creatorInstance: creatorInstance,
				editable: SpringSecurityUtils.ifAnyGranted('ROLE_DATAMANAGER')
		]
    }

	@Secured(['ROLE_DATAMANAGER'])
    def edit() {
		redirect action: 'show', params: params
		switch (request.method) {
		case 'GET':
	        def creatorInstance = Creator.get(params.id)
	        if (! creatorInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [creatorInstance: creatorInstance]
			break
		case 'POST':
	        def creatorInstance = Creator.get(params.id)
	        if (! creatorInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (creatorInstance.version > version) {
	                creatorInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'creator.label', default: 'Creator')] as Object[],
	                          "Another user has updated this Creator while you were editing")
	                render view: 'edit', model: [creatorInstance: creatorInstance]
	                return
	            }
	        }

	        creatorInstance.properties = params

	        if (! creatorInstance.save(flush: true)) {
	            render view: 'edit', model: [creatorInstance: creatorInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'creator.label', default: 'Creator'), creatorInstance.id])
	        redirect action: 'show', id: creatorInstance.id
			break
		}
    }

	@Secured(['ROLE_DATAMANAGER'])
    def delete() {
        def creatorInstance = Creator.get(params.id)
        if (! creatorInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
            redirect action: 'list'
            return
        }

        try {
            creatorInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'creator.label', default: 'Creator'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
