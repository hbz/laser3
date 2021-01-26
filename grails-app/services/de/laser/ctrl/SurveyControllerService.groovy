package de.laser.ctrl

import de.laser.AccessService
import de.laser.ContextService
import de.laser.LicenseController
import de.laser.SurveyConfig
import de.laser.SurveyController
import de.laser.SurveyInfo
import de.laser.Task
import de.laser.TaskService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class SurveyControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    TaskService taskService
    MessageSource messageSource

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.institution = contextService.getOrg()
        result.contextOrg = contextService.getOrg()
        result.user = contextService.getUser()

        result.surveyInfo = SurveyInfo.get(params.id)
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(Long.parseLong(params.surveyConfigID.toString())) : result.surveyInfo.surveyConfigs[0]
        result.surveyWithManyConfigs = (result.surveyInfo.surveyConfigs?.size() > 1)

        result.editable = result.surveyInfo.isEditable() ?: false

        if (result.surveyConfig) {
            result.transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : null
        }

        result.subscription = result.surveyConfig.subscription ?: null

        result
    }

    Map<String,Object> tasks(SurveyController controller, GrailsParameterMap params) {
        Locale locale = LocaleContextHolder.getLocale()
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if (params.deleteId) {
                Task dTask = Task.get(params.deleteId)
                if (dTask && dTask.creator.id == result.user.id) {
                    try {
                        Object[] args = [messageSource.getMessage('task.label',null,locale), dTask.title]
                        result.message = messageSource.getMessage('default.deleted.message', args, locale)
                        dTask.delete()
                    }
                    catch (Exception e) {
                        Object[] args = [messageSource.getMessage('task.label',null,locale), params.deleteId]
                        result.error = messageSource.getMessage('default.not.deleted.message', args, locale)
                        [result:result,status:STATUS_ERROR]
                    }
                }
            }
            int offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.taskInstanceList = taskService.getTasksByResponsiblesAndObject(result.user, contextService.getOrg(), result.surveyConfig)
            result.taskInstanceCount = result.taskInstanceList.size()
            result.taskInstanceList = taskService.chopOffForPageSize(result.taskInstanceList, result.user, offset)
            result.myTaskInstanceList = taskService.getTasksByCreatorAndObject(result.user,  result.license)
            result.myTaskInstanceCount = result.myTaskInstanceList.size()
            result.myTaskInstanceList = taskService.chopOffForPageSize(result.myTaskInstanceList, result.user, offset)
            [result:result,status:STATUS_OK]
        }
    }

}