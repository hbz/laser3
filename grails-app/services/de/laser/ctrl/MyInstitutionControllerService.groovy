package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.utils.DateUtils
import de.laser.helper.Profiler
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import de.laser.survey.SurveyInfo
import de.laser.system.SystemAnnouncement
import de.laser.workflow.WfWorkflow
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

/**
 * This service is a mirror of the {@link MyInstitutionController} for the data manipulation operations
 */
@Transactional
class MyInstitutionControllerService {

    AccessService accessService
    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    FilterService filterService
    SurveyService surveyService
    TaskService taskService
    WorkflowService workflowService

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    /**
     * Gets important information regarding the context institution for the user to display it on the dashboard.
     * The information is grouped in tabs where information is being preloaded (except changes, notifications and surveys)
     * @param controller the controller instance
     * @param params the request parameter map
     * @return OK if the request was successful, ERROR otherwise
     */
    Map<String, Object> dashboard(MyInstitutionController controller, GrailsParameterMap params) {
        Profiler prf = new Profiler()
        prf.setBenchmark('init')
        Map<String, Object> result = getResultGenerics(controller, params)

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            return [status: STATUS_ERROR, result: result]
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.acceptedOffset = 0
        result.pendingOffset = 0
        result.dashboardDueDatesOffset = 0
        switch(params.view) {
            case 'AcceptedChanges': result.acceptedOffset = result.offset
                break
            case 'PendingChanges': result.pendingOffset = result.offset
                break
            case 'dueDatesView': result.dashboardDueDatesOffset = result.offset
                break
        }

        def periodInDays = result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)

        // changes -> to AJAX

        //Map<String,Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"),periodInDays:periodInDays,max:result.max,offset:result.acceptedOffset]
        //pu.setBenchmark('pending changes')
        //result.putAll(pendingChangeService.getChanges(pendingChangeConfigMap))

        // systemAnnouncements
        prf.setBenchmark('system announcements')
        result.systemAnnouncements = SystemAnnouncement.getPublished(periodInDays)

        // tasks

        SimpleDateFormat sdFormat    = DateUtils.getLocalizedSDF_noTime()
        params.taskStatus = 'not done'
        def query       = filterService.getTaskQuery(params << [sort: 't.endDate', order: 'asc'], sdFormat)
        prf.setBenchmark('tasks')
        result.tasks    = taskService.getTasksByResponsibles(result.user, result.institution, query)
        result.tasksCount    = result.tasks.size()
        result.enableMyInstFormFields = true // enable special form fields


        /*def announcement_type = RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: result.max,offset:result.announcementOffset, sort: 'dateCreated', order: 'desc'])
        result.recentAnnouncementsCount = Doc.findAllByType(announcement_type).size()*/
        prf.setBenchmark('due dates')
        result.dueDates = dashboardDueDatesService.getDashboardDueDates( result.user, result.institution, false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.getDashboardDueDates( result.user, result.institution, false, false).size()
        /* -> to AJAX
        pu.setBenchmark('surveys')
        List activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is null AND surConfig.surveyInfo.status = :status) " +
                " order by surConfig.surveyInfo.endDate",
                [org: result.institution,
                 status: RDStore.SURVEY_SURVEY_STARTED])
        */

        if (workflowService.hasUserPerm_read()){
            /*activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where surConfig.surveyInfo.status = :status  and surConfig.surveyInfo.owner = :org " +
                    " order by surConfig.surveyInfo.endDate",
                    [org: result.institution,
                     status: RDStore.SURVEY_SURVEY_STARTED])*/

            if (params.cmd && params.cmd.contains(WfWorkflow.KEY)) {
                workflowService.usage(params)
            }

            List<WfWorkflow> workflows = workflowService.sortByLastUpdated(
                    WfWorkflow.findAllByOwnerAndStatus(result.institution as Org, RDStore.WF_WORKFLOW_STATUS_OPEN)
            )

            result.currentWorkflowsCount = workflows.size()
            result.currentWorkflows = workflows.take(contextService.getUser().getPageSizeOrDefault())
        }
        /*
        result.surveys = activeSurveyConfigs.groupBy {it?.id}
        result.countSurvey = result.surveys.size()
        */
        result.benchMark = prf.stopBenchmark()
        [status: STATUS_OK, result: result]
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets parameters used in many calls of the controller such as the context user, institution and permissions
     * @param controller unused
     * @param params the request parameter map
     * @return a map containing generic parameters
     */
    Map<String, Object> getResultGenerics(MyInstitutionController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        User user = contextService.getUser()
        Org org = contextService.getOrg()

//        switch (params.action){
//            case 'currentSurveys':
//            case 'surveyInfos':
//            case 'surveyInfoFinish':
//                result.user = user
//                break
//            default:
//                result.user = user
//        }

        result.user = user
        result.institution = org
        result.contextOrg = org
        result.contextCustomerType = org.getCustomerType()
        result.showConsortiaFunctions = result.contextCustomerType == "ORG_CONSORTIUM"
        switch (params.action) {
            case [ 'processEmptyLicense', 'currentLicenses', 'currentSurveys', 'dashboard', 'getChanges', 'getSurveys', 'emptyLicense', 'surveyInfoFinish' ]:
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR')
                break
            case [ 'addressbook', 'budgetCodes', 'tasks' ]:
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
                break
            case 'surveyInfos':
                result.editable = surveyService.isEditableSurvey(org, SurveyInfo.get(params.id) ?: null)
                break
            case 'users':
                result.editable = user.hasRole('ROLE_ADMIN') || user.hasAffiliation('INST_ADM')
                break
            case 'managePropertyDefinitions':
                result.editable = false
                result.changeProperties = user.hasRole('ROLE_ADMIN') || user.hasAffiliation('INST_EDITOR')
                break
            default:
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA')
        }

        result
    }
}