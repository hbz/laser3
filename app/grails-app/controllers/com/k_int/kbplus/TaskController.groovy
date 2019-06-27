package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException
@Secured(['IS_AUTHENTICATED_FULLY'])
class TaskController extends AbstractDebugController {

	def springSecurityService
    def contextService
    def taskService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

	@Secured(['ROLE_ADMIN'])
    def list() {
		if (! params.max) {
			User user   = springSecurityService.getCurrentUser()
			params.max  = user?.getDefaultPageSizeTMP()
		}
        [taskInstanceList: Task.list(params), taskInstanceTotal: Task.count()]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        def contextOrg  = contextService.getOrg()
		def result      = taskService.getPreconditions(contextOrg)

		def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

		if (params.endDate)
			params.endDate = sdf.parse(params.endDate)

		switch (request.method) {
			/*case 'GET':
				result.taskInstance = new Task(params)
				result
				break*/
			case 'POST':
				def taskInstance = new Task(title: params.title, description: params.description, status: params.status.id, endDate: params.endDate)
				taskInstance.creator = contextService.getUser()
				taskInstance.createDate = new Date()

				//Bearbeiter festlegen
				if (params.responsible == "Org") {
					taskInstance.responsibleOrg = contextOrg
				}
				else if (params.responsible == "User") {
					taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id): contextService.getUser()
				}

				if (params.linkto == "license" && params.license) {
					taskInstance.license = License.get(params.license) ?: null
				}
				else if (params.linkto == "pkg" && params.pkg) {
					taskInstance.pkg = Package.get(params.pkg) ?: null
				}
				else if (params.linkto == "subscription" && params.subscription) {
					taskInstance.subscription = Subscription.get(params.subscription) ?: null
				}
				else if (params.linkto == "org" && params.org) {
					taskInstance.org = Org.get(params.org) ?: null
				}
				else if (params.linkto == "surveyConfig" && params.surveyConfig) {
					taskInstance.surveyConfig = SurveyConfig.get(params.surveyConfig) ?: null
				}

				if (!taskInstance.save(flush: true)) {
					/*result.taskInstance = taskInstance
					render view: 'create', model: result*/
					flash.error = message(code: 'default.not.created.message', args: [message(code: 'task.label', default: 'Task')])
					redirect(url: request.getHeader('referer'))
					return
				}

				flash.message = message(code: 'default.created.message', args: [message(code: 'task.label', default: 'Task'), taskInstance.title])

				redirect(url: request.getHeader('referer'))
				break
		}
    }

    @Secured(['ROLE_ADMIN'])
    def show() {
        def taskInstance = Task.get(params.id)
        if (! taskInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            //redirect action: 'list'
			redirect controller: 'myInstitution', action: 'dashboard'
            return
        }

        [taskInstance: taskInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        def contextOrg = contextService.getOrg()
        def result     = taskService.getPreconditions(contextOrg)

		def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

		if (params.endDate)
			params.endDate = sdf.parse(params.endDate)

		switch (request.method) {
		/*case 'GET':
            result.taskInstance = Task.get(params.id)
	        if (! result.taskInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
	            //redirect action: 'list'
				redirect controller: 'myInstitution', action: 'dashboard'
	            return
	        }

            result
			break*/
		case 'POST':
	        def taskInstance = Task.get(params.id)

			if(((!taskInstance.responsibleOrg) && taskInstance.responsibleUser != contextService.getUser()) && (taskInstance.responsibleOrg != contextOrg) && (taskInstance.creator != contextService.getUser()))
			{
				flash.error = message(code: 'task.edit.norights', args: [taskInstance.title])
				redirect(url: request.getHeader('referer'))
				return
			}

	        if (! taskInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
	            //redirect action: 'list'
				redirect controller: 'myInstitution', action: 'dashboard'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (taskInstance.version > version) {
	                taskInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'task.label', default: 'Task')] as Object[],
	                          "Another user has updated this Task while you were editing")

                    result.taskInstance = taskInstance
	                //render view: 'edit', model: result
					redirect(url: request.getHeader('referer'))
	                return
	            }
	        }

	        taskInstance.properties = params

			//Bearbeiter festlegen/ändern
			if (params.responsible == "Org") {
				taskInstance.responsibleOrg = contextOrg
				taskInstance.responsibleUser = null
			}
			else if (params.responsible == "User") {
				taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id): contextService.getUser()
				taskInstance.responsibleOrg = null
			}

	        if (! taskInstance.save(flush: true)) {
                result.taskInstance = taskInstance
	            /*render view: 'edit', model: result*/
				flash.error = message(code: 'default.not.updated.message', args: [message(code: 'task.label', default: 'Task'), taskInstance.title])
				redirect(url: request.getHeader('referer'))
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'task.label', default: 'Task'), taskInstance.title])
			redirect(url: request.getHeader('referer'))
			break
		}
    }

	@Secured(['permitAll']) // TODO
	def ajaxEdit() {
		def contextOrg = contextService.getOrg()
		def result     = taskService.getPreconditions(contextOrg)
		result.params = params
		result.taskInstance = Task.get(params.id)

		render template:"../templates/tasks/modal_edit", model: result
	}

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def taskInstance = Task.get(params.id)
		def tasktitel = taskInstance.title
        if (! taskInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label', default: 'Task'), params.id])
            //redirect action: 'list'
			redirect(url: request.getHeader('referer'))
			return
        }

		if(taskInstance.creator != contextService.getUser())
		{
			flash.error = message(code: 'task.delete.norights', args: [tasktitel])
			redirect(url: request.getHeader('referer'))
			return
		}

        try {

            taskInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label', default: 'Task'), tasktitel])
            //redirect action: 'list'
			redirect(url: request.getHeader('referer'))
        }
        catch (DataIntegrityViolationException e) {
			flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label', default: 'Task'),  tasktitel])
            //redirect action: 'show', id: params.id
			redirect(url: request.getHeader('referer'))
        }
    }
}
