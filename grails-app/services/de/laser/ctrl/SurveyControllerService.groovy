package de.laser.ctrl

import de.laser.ContextService
import de.laser.SurveyConfig
import de.laser.SurveyInfo
import de.laser.auth.User
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class SurveyControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    SpringSecurityService springSecurityService

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.institution = contextService.getOrg()
        result.contextOrg = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

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

}