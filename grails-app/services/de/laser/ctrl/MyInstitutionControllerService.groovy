package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.utils.DateUtils
import de.laser.helper.Profiler
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.survey.SurveyInfo
import de.laser.system.SystemAnnouncement
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

/**
 * This service is a mirror of the {@link MyInstitutionController} for the data manipulation operations
 */
@Transactional
class MyInstitutionControllerService {

    CacheService cacheService
    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    FilterService filterService
    SurveyService surveyService
    TaskService taskService
    UserService userService
    WekbStatsService wekbStatsService
    WorkflowService workflowService
    MessageSource messageSource

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

        //completed processes
        /*
        deactivated as incomplete
        Set<String> processes = []
        EhcacheWrapper cache = cacheService.getTTL1800Cache("finish_${result.user.id}")
        if(cache) {
            cache.getKeys().each { String key ->
                processes << cache.get(key.split("finish_${result.user.id}_")[1])
            }
        }
        result.completedProcesses = processes
        */

        // systemAnnouncements
        prf.setBenchmark('system announcements')
        result.systemAnnouncements = SystemAnnouncement.getPublished(periodInDays)

        // tasks

        SimpleDateFormat sdFormat    = DateUtils.getLocalizedSDF_noTime()
        params.taskStatus = 'not done'
        FilterService.Result fsr = filterService.getTaskQuery(params << [sort: 't.endDate', order: 'asc'], sdFormat)
        prf.setBenchmark('tasks')
        result.tasks = taskService.getTasksByResponsibles(result.user as User, result.institution as Org, [query: fsr.query, queryParams: fsr.queryParams])
        result.tasksCount    = result.tasks.size()
        result.enableMyInstFormFields = true // enable special form fields


        /*def announcement_type = RDStore.DOC_TYPE_ANNOUNCEMENT
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: result.max,offset:result.announcementOffset, sort: 'dateCreated', order: 'desc'])
        result.recentAnnouncementsCount = Doc.findAllByType(announcement_type).size()*/
        prf.setBenchmark('due dates')
        result.dueDates = dashboardDueDatesService.getDashboardDueDates( result.user, result.institution, false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.countDashboardDueDates( result.user, result.institution, false, false)
        /* -> to AJAX
        pu.setBenchmark('surveys')
        List activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is null AND surConfig.surveyInfo.status = :status) " +
                " order by surConfig.surveyInfo.endDate",
                [org: result.institution,
                 status: RDStore.SURVEY_SURVEY_STARTED])
        */
        prf.setBenchmark('workflows')
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
        prf.setBenchmark('wekbChanges')
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
        result.subId = params.subId
        result.tooltip = messageSource.getMessage('license.filter.member', null, LocaleUtils.getCurrentLocale())
        if(org.isCustomerType_Consortium())
            result.tooltip = messageSource.getMessage('license.member', null, LocaleUtils.getCurrentLocale())
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