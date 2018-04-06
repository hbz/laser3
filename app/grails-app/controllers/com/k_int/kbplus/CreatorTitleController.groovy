package com.k_int.kbplus


import com.k_int.kbplus.auth.User
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.annotation.Secured
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_YODA'])
class CreatorTitleController {

	def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@Secured(['ROLE_YODA'])
    def index() {
        redirect action: 'list', params: params
    }

	@Secured(['ROLE_YODA'])
    def list() {
		if (! params.max) {
			User user   = springSecurityService.getCurrentUser()
			params.max = user?.getDefaultPageSize()
		}
        [creatorTitleInstanceList: CreatorTitle.list(params), creatorTitleInstanceTotal: CreatorTitle.count()]
    }

	@Secured(['ROLE_YODA'])
    def create() {
		switch (request.method) {
		case 'GET':
        	[creatorTitleInstance: new CreatorTitle(params)]
			break
		case 'POST':
	        def creatorTitleInstance = new CreatorTitle(params)
	        if (! creatorTitleInstance.save(flush: true)) {
	            render view: 'create', model: [creatorTitleInstance: creatorTitleInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), creatorTitleInstance.id])
	        redirect action: 'show', id: creatorTitleInstance.id
			break
		}
    }

	@Secured(['ROLE_YODA'])
    def show() {
        def creatorTitleInstance = CreatorTitle.get(params.id)
        if (! creatorTitleInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
            redirect action: 'list'
            return
        }

        [creatorTitleInstance: creatorTitleInstance]
    }

	@Secured(['ROLE_YODA'])
    def edit() {
		switch (request.method) {
		case 'GET':
	        def creatorTitleInstance = CreatorTitle.get(params.id)
	        if (! creatorTitleInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [creatorTitleInstance: creatorTitleInstance]
			break
		case 'POST':
	        def creatorTitleInstance = CreatorTitle.get(params.id)
	        if (! creatorTitleInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (creatorTitleInstance.version > version) {
	                creatorTitleInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'creatorTitle.label', default: 'CreatorTitle')] as Object[],
	                          "Another user has updated this CreatorTitle while you were editing")
	                render view: 'edit', model: [creatorTitleInstance: creatorTitleInstance]
	                return
	            }
	        }

	        creatorTitleInstance.properties = params

	        if (! creatorTitleInstance.save(flush: true)) {
	            render view: 'edit', model: [creatorTitleInstance: creatorTitleInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), creatorTitleInstance.id])
	        redirect action: 'show', id: creatorTitleInstance.id
			break
		}
    }

	@Secured(['ROLE_YODA'])
    def delete() {
        def creatorTitleInstance = CreatorTitle.get(params.id)
        if (! creatorTitleInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
            redirect action: 'list'
            return
        }

        try {
            creatorTitleInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'creatorTitle.label', default: 'CreatorTitle'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
