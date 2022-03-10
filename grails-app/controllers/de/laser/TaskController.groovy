package de.laser

import de.laser.auth.User
 
import de.laser.helper.DateUtils
import de.laser.annotations.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

import java.text.SimpleDateFormat

/**
 * This controller handles generic task-related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TaskController  {

    def contextService
    def taskService

    static allowedMethods = [create: 'POST', edit: 'POST', delete: 'POST']

	@Deprecated
    @Secured(['ROLE_ADMIN'])
    def index() {
        redirect action: 'list', params: params
    }

	@Deprecated
	@Secured(['ROLE_ADMIN'])
    def list() {
		if (! params.max) {
			User user   = contextService.getUser()
			params.max  = user?.getDefaultPageSize()
		}
        [taskInstanceList: Task.list(params), taskInstanceTotal: Task.count()]
    }

	/**
	 * Processes the submitted input parameters and creates a new task for the given owner object
	 * @return a redirect to the referer
	 */
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		Task.withTransaction {
			def contextOrg  = contextService.getOrg()
			SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

			if (params.endDate) {
				params.endDate = sdf.parse(params.endDate)
			}

			Task taskInstance = new Task(title: params.title, description: params.description, status: params.status.id, systemCreateDate: new Date(), endDate: params.endDate)
			taskInstance.creator = contextService.getUser()
			taskInstance.createDate = new Date()

				//Bearbeiter festlegen
				if (params.responsible == "Org") {
					taskInstance.responsibleOrg = contextOrg
				}
				else if (params.responsible == "User") {
					taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id): contextService.getUser()
				}

				if (params.linkto == "license" && params.license && params.license != 'null') {
					taskInstance.license = License.get(params.license) ?: null
				}
				else if (params.linkto == "pkg" && params.pkg && params.pkg != 'null') {
					taskInstance.pkg = Package.get(params.pkg) ?: null
				}
				else if (params.linkto == "subscription" && params.subscription && params.subscription != 'null') {
					taskInstance.subscription = Subscription.get(params.subscription) ?: null
				}
				else if (params.linkto == "org" && params.org && params.org != 'null') {
					taskInstance.org = Org.get(params.org) ?: null
				}
				else if (params.linkto == "surveyConfig" && params.surveyConfig && params.surveyConfig != 'null') {
					taskInstance.surveyConfig = SurveyConfig.get(params.surveyConfig) ?: null
				}

				if (!taskInstance.save()) {
					flash.error = message(code: 'default.not.created.message', args: [message(code: 'task.label')])
					redirect(url: request.getHeader('referer'))
					return
				}

				flash.message = message(code: 'default.created.message', args: [message(code: 'task.label'), taskInstance.title])

				redirect(url: request.getHeader('referer'))
		}
    }

	/**
	 * Call to create a new task
	 * @return the task creation modal
	 */
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def _modal_create() {
        def contextOrg  = contextService.getOrg()
		def result      = taskService.getPreconditions(contextOrg)
		result.validSubscriptionsList = new ArrayList()
		result.validSubscriptions.each{
			result.validSubscriptionsList.add([it.id, it.dropdownNamingConvention(contextService.getOrg())])
		}
		render template: "/templates/tasks/modal_create", model: result
    }

	@Deprecated
    @Secured(['ROLE_ADMIN'])
    def show() {
		Task taskInstance = Task.get(params.id)
        if (! taskInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.id])
			redirect controller: 'myInstitution', action: 'dashboard'
            return
        }

        [taskInstance: taskInstance]
    }

	/**
	 * Processes the submitted input and updates the given task instance with the given parameters
	 * @return a redirect to the referer
	 */
	@DebugAnnotation(test='hasAffiliation("INST_USER")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def edit() {
		Task.withTransaction {
			Org contextOrg = contextService.getOrg()
			def result = taskService.getPreconditionsWithoutTargets(contextOrg)

			SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

			if (params.endDate) {
				params.endDate = sdf.parse(params.endDate)
			}

			Task taskInstance = Task.get(params.id)

			if (((!taskInstance.responsibleOrg) && taskInstance.responsibleUser != contextService.getUser()) && (taskInstance.responsibleOrg != contextOrg) && (taskInstance.creator != contextService.getUser())) {
				flash.error = message(code: 'task.edit.norights', args: [taskInstance.title])
				redirect(url: request.getHeader('referer'))
				return
			}

			if (!taskInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.id])
				redirect controller: 'myInstitution', action: 'dashboard'
				return
			}

			if (params.version) {
				Long version = params.long('version')
				if (taskInstance.version > version) {
					taskInstance.errors.rejectValue(
							'version',
							'default.optimistic.locking.failure',
							[message(code: 'task.label')] as Object[],
							"Another user has updated this Task while you were editing"
					)

					result.taskInstance = taskInstance
					redirect(url: request.getHeader('referer'))
					return
				}
			}

			taskInstance.properties = params

			//Bearbeiter festlegen/ändern
			if (params.responsible == "Org") {
				taskInstance.responsibleOrg = contextOrg
				taskInstance.responsibleUser = null
			} else if (params.responsible == "User") {
				taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id) : contextService.getUser()
				taskInstance.responsibleOrg = null
			}

			if (!taskInstance.save()) {
				result.taskInstance = taskInstance
				flash.error = message(code: 'default.not.updated.message', args: [message(code: 'task.label'), taskInstance.title])
				redirect(url: request.getHeader('referer'))
				return
			}

			flash.message = message(code: 'default.updated.message', args: [message(code: 'task.label'), taskInstance.title])
			redirect(url: request.getHeader('referer'))
		}
    }

	/**
	 * Call to edit the given task instance
	 * @return the task editing modal
	 */
	@Secured(['permitAll']) // TODO
	def ajaxEdit() {
		Org contextOrg = contextService.getOrg()
		def result     = taskService.getPreconditionsWithoutTargets(contextOrg)
		result.params = params
		result.taskInstance = Task.get(params.id)

		render template: "/templates/tasks/modal_edit", model: result
	}

	/**
	 * Call to delete the given task instance
	 * @return a redirect to the referer
	 */
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")', wtc = 2)
	@Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
		Task.withTransaction {
			Task taskInstance = Task.get(params.id)
			String tasktitel = taskInstance.title

			if (!taskInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.id])
				redirect(url: request.getHeader('referer'))
				return
			}

			if (taskInstance.creator != contextService.getUser()) {
				flash.error = message(code: 'task.delete.norights', args: [tasktitel])
				redirect(url: request.getHeader('referer'))
				return
			}

			try {
				taskInstance.delete()
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), tasktitel])
				redirect(url: request.getHeader('referer'))
			}
			catch (DataIntegrityViolationException e) {
				flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), tasktitel])
				redirect(url: request.getHeader('referer'))
			}
		}
    }
}
