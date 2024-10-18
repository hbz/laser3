package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.storage.BeanStore
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
    WekbNewsService wekbNewsService
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
        result.tasks = taskService.getTasksByResponsibility(result.user as User, [query: fsr.query, queryParams: fsr.queryParams])
        result.tasksCount    = result.tasks.size()

        prf.setBenchmark('due dates')
        result.dueDates = dashboardDueDatesService.getDashboardDueDates( result.user, result.institution, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.countDashboardDueDates( result.user, result.institution)

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

        prf.setBenchmark('wekbNews')
        result.wekbNews = wekbNewsService.getCurrentNews()

        result.benchMark = prf.stopBenchmark()
        [status: STATUS_OK, result: result]
    }

    Map<String, Object> exportConfigs(MyInstitutionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenerics(controller, params)
        params.tab = params.tab ?: ExportClickMeService.ADDRESSBOOK

        result.editable = BeanStore.getContextService().isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)

        List notShowClickMe = []

        if(result.contextOrg.isCustomerType_Consortium()){
            notShowClickMe = [ExportClickMeService.CONSORTIAS]
        }else{
            notShowClickMe = [ExportClickMeService.CONSORTIA_PARTICIPATIONS, ExportClickMeService.INSTITUTIONS,
                              ExportClickMeService.SUBSCRIPTIONS_MEMBERS, ExportClickMeService.SUBSCRIPTIONS_TRANSFER,
                              ExportClickMeService.SURVEY_EVALUATION, ExportClickMeService.SURVEY_RENEWAL_EVALUATION,
                              ExportClickMeService.SURVEY_COST_ITEMS]
        }

        if(result.contextOrg.isCustomerType_Basic()){
            notShowClickMe << [ExportClickMeService.CONSORTIAS]
        }


        result.clickMeTypes = ExportClickMeService.CLICK_ME_TYPES - notShowClickMe
        result.clickMeConfigsCount= [:]
        result.clickMeTypes.each { String clickMeType ->
            result.clickMeConfigsCount[clickMeType] = ClickMeConfig.executeQuery('select count(*) from ClickMeConfig where contextOrg = :contextOrg and clickMeType = :clickMeType', [contextOrg: result.contextOrg, clickMeType: clickMeType])[0]
        }


        result.clickMeConfigs = ClickMeConfig.executeQuery('from ClickMeConfig where contextOrg = :contextOrg and clickMeType = :clickMeType order by configOrder', [contextOrg: result.contextOrg, clickMeType: params.tab])
        result.clickMeConfigsAllCount = ClickMeConfig.executeQuery('select count(*) from ClickMeConfig where contextOrg = :contextOrg ', [contextOrg: result.contextOrg])[0]

        [status: STATUS_OK, result: result]
    }

    Map<String, Object> exportConfigsActions(MyInstitutionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenerics(controller, params)
        result.editable = BeanStore.getContextService().isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
        result.tab = params.tab ?: ExportClickMeService.ADDRESSBOOK
        if(result.editable) {
            if (params.cmd == 'delete' && params.id) {
                ClickMeConfig clickMeConfig = ClickMeConfig.findByContextOrgAndId(result.contextOrg, params.id)

                if (clickMeConfig) {
                    clickMeConfig.delete()
                }
            }
            if (params.cmd in ['moveUp', 'moveDown'] && params.id) {
                ClickMeConfig toMoveClickMeConfig = ClickMeConfig.findByContextOrgAndId(result.contextOrg, params.id)
                Set<ClickMeConfig> sequence = ClickMeConfig.executeQuery('select cmc from ClickMeConfig as cmc where cmc.contextOrg = :contextOrg and cmc.clickMeType = :clickMeType order by cmc.configOrder', [contextOrg: result.contextOrg, clickMeType: params.tab]) as Set<ClickMeConfig>

                int idx = sequence.findIndexOf { it.id == toMoveClickMeConfig.id }
                int pos = toMoveClickMeConfig.configOrder
                ClickMeConfig toMoveClickMeConfig2

                if (params.cmd == 'moveUp') {
                    toMoveClickMeConfig2 = sequence.getAt(idx - 1)
                } else if (params.cmd == 'moveDown') {
                    toMoveClickMeConfig2 = sequence.getAt(idx + 1)
                }

                if (toMoveClickMeConfig2) {
                    toMoveClickMeConfig.configOrder = toMoveClickMeConfig2.configOrder
                    toMoveClickMeConfig.save()
                    toMoveClickMeConfig2.configOrder = pos
                    toMoveClickMeConfig2.save()
                }
            }

        }
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
                SwissKnife.setPaginationParams(result, params, user)
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