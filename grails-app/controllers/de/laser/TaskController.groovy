package de.laser

import de.laser.annotations.Check404
import de.laser.auth.Role
import de.laser.auth.User
 
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.survey.SurveyConfig
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

/**
 * This controller handles generic task-related calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TaskController  {

	AccessService accessService
    ContextService contextService

	//-----

	/**
	 * Map containing menu alternatives if an unexisting object has been called
	 */
	public static final Map<String, String> CHECK404_ALTERNATIVES = [
			'myInstitution/tasks' : 'menu.institutions.tasks'
	]

	//-----

	/**
	 * Processes the submitted input parameters and creates a new task for the given owner object
	 * @return a redirect to the referer
	 */
	@DebugInfo(isInstEditor = [CustomerTypeService.PERMS_PRO], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor(CustomerTypeService.PERMS_PRO)
	})
    def createTask() {
		String referer = request.getHeader('referer')

		Task.withTransaction {
			SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

			if (params.endDate) {
				params.endDate = sdf.parse(params.endDate)
			}

			Task taskInstance = new Task(title: params.title, description: params.description, status: params.status.id, systemCreateDate: new Date(), endDate: params.endDate)
			taskInstance.creator = contextService.getUser()
			taskInstance.createDate = new Date()

				//Bearbeiter festlegen
				if (params.responsible == "Org") {
					taskInstance.responsibleOrg = contextService.getOrg()
				}
				else if (params.responsible == "User") {
					taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id): contextService.getUser()
				}

				if (params.linkto == "license" && params.license && params.license != 'null') {
					taskInstance.license = License.get(params.license) ?: null
				}
				else if (params.linkto == "subscription" && params.subscription && params.subscription != 'null') {
					taskInstance.subscription = Subscription.get(params.subscription) ?: null
				}
				else if (params.linkto == "org" && params.org && params.org != 'null') {
					taskInstance.org = Org.get(params.org) ?: null
				}
				else if (params.linkto == "provider" && params.provider && params.provider != 'null') {
					taskInstance.provider = Provider.get(params.provider) ?: null
				}
				else if (params.linkto == "vendor" && params.vendor && params.vendor != 'null') {
					taskInstance.vendor = Vendor.get(params.vendor) ?: null
				}
				else if (params.linkto == "surveyConfig" && params.surveyConfig && params.surveyConfig != 'null') {
					taskInstance.surveyConfig = SurveyConfig.get(params.surveyConfig) ?: null
				}
				else if (params.linkto == "tipp" && params.tipp && params.tipp != 'null') {
					taskInstance.tipp = TitleInstancePackagePlatform.get(params.tipp) ?: null
				}

				if (!taskInstance.save()) {
					flash.error = message(code: 'default.not.created.message', args: [message(code: 'task.label')]) as String
					redirect(url: referer)
					return
				}

				flash.message = message(code: 'default.created.message', args: [message(code: 'task.label'), taskInstance.title]) as String

				redirect(url: referer)
		}
    }

	/**
	 * Processes the submitted input and updates the given task instance with the given parameters
	 * @return a redirect to the referer
	 */
	@DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
	})
	@Check404()
    def editTask() {
		String referer = request.getHeader('referer')

		Task.withTransaction {
            Map<String, Object> result = [
					contextOrg : contextService.getOrg()
			]
			Task taskInstance = Task.get(params.id)

			if (!accessService.hasAccessToTask(taskInstance, AccessService.WRITE)) {
				flash.error = message(code: 'default.noPermissions') as String
				redirect(url: referer)
				return
			}

			SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
			if (params.endDate) {
				params.endDate = sdf.parse(params.endDate)
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
					redirect(url: referer)
					return
				}
			}

			taskInstance.properties = params

			//Bearbeiter festlegen/ändern
			if (params.responsible == "Org") {
				taskInstance.responsibleOrg = contextService.getOrg()
				taskInstance.responsibleUser = null
			} else if (params.responsible == "User") {
				taskInstance.responsibleUser = (params.responsibleUser.id != 'null') ? User.get(params.responsibleUser.id) : contextService.getUser()
				taskInstance.responsibleOrg = null
			}

			if (!taskInstance.save()) {
				result.taskInstance = taskInstance
				flash.error = message(code: 'default.not.updated.message', args: [message(code: 'task.label'), taskInstance.title]) as String
				redirect(url: referer)
				return
			}

			flash.message = message(code: 'default.updated.message', args: [message(code: 'task.label'), taskInstance.title]) as String
			redirect(url: referer)
		}
    }

	/**
	 * Deletes the given task
	 */
	@DebugInfo(isInstEditor = [CustomerTypeService.PERMS_PRO], withTransaction = 1)
	@Secured(closure = {
		ctx.contextService.isInstEditor(CustomerTypeService.PERMS_PRO)
	})
	def deleteTask() { // moved from AjaxController

		if (params.deleteId) {
			Task.withTransaction {
				Task dTask = Task.get(params.deleteId)
				if (accessService.hasAccessToTask(dTask, AccessService.WRITE)) {
					try {
						flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), dTask.title]) as String
						dTask.delete()
					}
					catch (Exception e) {
						log.error(e)
						flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), dTask.title]) as String
					}
				} else {
					if (!dTask) {
						flash.error = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.deleteId]) as String
					} else {
						flash.error = message(code: 'default.noPermissions') as String
					}
				}
			}
		}
		if(params.returnToShow) {
			redirect action: 'show', id: params.id, controller: params.returnToShow
			return
		}
		redirect(url: request.getHeader('referer'))
	}
}
