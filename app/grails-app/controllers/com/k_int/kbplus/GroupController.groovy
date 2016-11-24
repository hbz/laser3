package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException

class GroupController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [groupInstanceList: Group.list(params), groupInstanceTotal: Group.count()]
    }

    def create() {
		switch (request.method) {
		case 'GET':
        	[groupInstance: new Group(params)]
			break
		case 'POST':
	        def groupInstance = new Group(params)
	        if (!groupInstance.save(flush: true)) {
	            render view: 'create', model: [groupInstance: groupInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'group.label', default: 'Group'), groupInstance.id])
	        redirect action: 'show', id: groupInstance.id
			break
		}
    }

    def show() {
        def groupInstance = Group.get(params.id)
        if (!groupInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'group.label', default: 'Group'), params.id])
            redirect action: 'list'
            return
        }

        [groupInstance: groupInstance]
    }

    def edit() {
		switch (request.method) {
		case 'GET':
	        def groupInstance = Group.get(params.id)
	        if (!groupInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'group.label', default: 'Group'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [groupInstance: groupInstance]
			break
		case 'POST':
	        def groupInstance = Group.get(params.id)
	        if (!groupInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'group.label', default: 'Group'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (groupInstance.version > version) {
	                groupInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'group.label', default: 'Group')] as Object[],
	                          "Another user has updated this Group while you were editing")
	                render view: 'edit', model: [groupInstance: groupInstance]
	                return
	            }
	        }

	        groupInstance.properties = params

	        if (!groupInstance.save(flush: true)) {
	            render view: 'edit', model: [groupInstance: groupInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'group.label', default: 'Group'), groupInstance.id])
	        redirect action: 'show', id: groupInstance.id
			break
		}
    }

    def delete() {
        def groupInstance = Group.get(params.id)
        if (!groupInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'group.label', default: 'Group'), params.id])
            redirect action: 'list'
            return
        }

        try {
            groupInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'group.label', default: 'Group'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'group.label', default: 'Group'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
