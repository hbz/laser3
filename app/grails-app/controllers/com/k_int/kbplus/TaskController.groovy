package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import org.springframework.dao.DataIntegrityViolationException

class TaskController {

	def springSecurityService
    def contextService
    def taskService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
		if (! params.max) {
			User user   = springSecurityService.getCurrentUser()
			params.max  = user?.getDefaultPageSize()
		}
        [taskInstanceList: Task.list(params), taskInstanceTotal: Task.count()]
    }

    def create() {
        def contextOrg  = contextService.getOrg(springSecurityService.getCurrentUser())
		def result      = taskService.getPreconditions(contextOrg)

		switch (request.method) {
		case 'GET':
            result.taskInstance = new Task(params)
            result
			break
		case 'POST':
	        def taskInstance = new Task(params)
	        if (! taskInstance.save(flush: true)) {
                result.taskInstance = taskInstance
	            render view: 'create', model: result
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'task.label', default: 'Task'), taskInstance.id])
	        redirect action: 'show', id: taskInstance.id
			break
		}
    }

    def show() {
        def taskInstance = Task.get(params.id)
        if (! taskInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            redirect action: 'list'
            return
        }

        [taskInstance: taskInstance]
    }

    def edit() {
        def contextOrg = contextService.getOrg(springSecurityService.getCurrentUser())
        def result     = taskService.getPreconditions(contextOrg)

		switch (request.method) {
		case 'GET':
            result.taskInstance = Task.get(params.id)
	        if (! result.taskInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
	            redirect action: 'list'
	            return
	        }

            result
			break
		case 'POST':
	        def taskInstance = Task.get(params.id)
	        if (! taskInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (taskInstance.version > version) {
	                taskInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'task.label', default: 'Task')] as Object[],
	                          "Another user has updated this Task while you were editing")

                    result.taskInstance = taskInstance
	                render view: 'edit', model: result
	                return
	            }
	        }

	        taskInstance.properties = params

	        if (! taskInstance.save(flush: true)) {
                result.taskInstance = taskInstance
	            render view: 'edit', model: result
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'task.label', default: 'Task'), taskInstance.id])
	        redirect action: 'show', id: taskInstance.id
			break
		}
    }

    def delete() {
        def taskInstance = Task.get(params.id)
        if (! taskInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            redirect action: 'list'
            return
        }

        try {
            taskInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
