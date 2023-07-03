package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.utils.DateUtils
import de.laser.helper.Profiler
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import de.laser.survey.SurveyInfo
import de.laser.system.SystemAnnouncement
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

/**
 * This service is a mirror of the {@link MyInstitutionController} for the data manipulation operations
 */
@Transactional
class MyInstitutionControllerService {

    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    FilterService filterService
    SurveyService surveyService
    TaskService taskService
    UserService userService
    WekbStatsService wekbStatsService
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

        if (! (result.user as User).isFormal(result.institution as Org)) {
            return [status: STATUS_ERROR, result: result]
        }

        result.is_inst_admin = userService.hasFormalAffiliation(result.user, result.institution, 'INST_ADM')

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

        //Map<String,Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.otherOrgPerm(result.institution, 'ORG_CONSORTIUM_BASIC'),periodInDays:periodInDays,max:result.max,offset:result.acceptedOffset]
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


        /*def announcement_type = RDStore.DOC_TYPE_ANNOUNCEMENT
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

//            List<WfWorkflow> myWfList  = workflows.findAll { it.user != null && it.user.id == result.user.id }
//            List<WfWorkflow> allWfList = workflows.findAll { it.user == null }
//
//            result.myWorkflowsCount  = myWfList.size()
//            result.allWorkflowsCount = allWfList.size()
//            result.myWorkflows  = myWfList.take(contextService.getUser().getPageSizeOrDefault())
//            result.allWorkflows = allWfList.take(contextService.getUser().getPageSizeOrDefault())

//            result.currentWorkflowsCount = result.myCurrentWorkflows.size() + result.allCurrentWorkflows.size()
//            result.currentWorkflows      = workflows.take(contextService.getUser().getPageSizeOrDefault())

        if (workflowService.hasUserPerm_edit()) {
            if (params.cmd) {
                String[] cmd = params.cmd.split(':')

                if (cmd[1] in [WfChecklist.KEY, WfCheckpoint.KEY]) { // light
                    workflowService.executeCmd(params)
//                    result.putAll(workflowService.cmd(params))
                }
            }
        }

        if (workflowService.hasUserPerm_read()){
            List<WfChecklist> workflows = []

            workflowService.sortByLastUpdated( WfChecklist.findAllByOwner(result.institution) ).each { clist ->
                Map info = clist.getInfo()

                if (info.status == RDStore.WF_WORKFLOW_STATUS_OPEN) {
                    workflows.add(clist)
                }
            }
            result.allChecklistsCount = workflows.size()
            result.allChecklists = workflows.take(contextService.getUser().getPageSizeOrDefault())
        }
        /*
        result.surveys = activeSurveyConfigs.groupBy {it?.id}
        result.countSurvey = result.surveys.size()
        */

        //int days = result.user.getSetting(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14).getValue()
        //result.wekbChanges = wekbStatsService.getCurrentChanges(days)
        result.wekbChanges = wekbStatsService.getCurrentChanges()

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

        result.user = user
        result.institution = org
        result.contextOrg = org
        result.contextCustomerType = org.getCustomerType()
        result.showConsortiaFunctions = org.isCustomerType_Consortium()
        switch (params.action) {
            case [ 'processEmptyLicense', 'currentLicenses', 'currentSurveys', 'dashboard', 'getChanges', 'getSurveys', 'emptyLicense', 'surveyInfoFinish' ]:
                result.editable = userService.hasFormalAffiliation(user, org, 'INST_EDITOR')
                break
            case [ 'addressbook', 'budgetCodes', 'tasks' ]:
                result.editable = userService.hasFormalAffiliation_or_ROLEADMIN(user, org, 'INST_EDITOR')
                break
            case 'surveyInfos':
                result.editable = surveyService.isEditableSurvey(org, SurveyInfo.get(params.id) ?: null)
                break
            case 'users':
                result.editable = contextService.isInstAdm_or_ROLEADMIN()
                break
            case 'managePropertyDefinitions':
                result.editable = false
                result.changeProperties = contextService.isInstEditor_or_ROLEADMIN()
                break
            default:
                result.editable = userService.hasFormalAffiliation_or_ROLEADMIN(user, org, 'INST_EDITOR')
        }

        result
    }
}