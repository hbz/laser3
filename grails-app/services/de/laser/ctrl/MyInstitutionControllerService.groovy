package de.laser.ctrl

import de.laser.*
import de.laser.auth.User
import de.laser.helper.DateUtils
import de.laser.helper.ProfilerUtils
import de.laser.helper.RDStore
import de.laser.helper.SwissKnife
import de.laser.system.SystemAnnouncement
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

@Transactional
class MyInstitutionControllerService {

    def accessService
    def contextService
    def dashboardDueDatesService
    def filterService
    def pendingChangeService
    def surveyService
    def taskService

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    Map<String, Object> dashboard(MyInstitutionController controller, GrailsParameterMap params) {
        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('init')
        Map<String, Object> result = getResultGenerics(controller, params)

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            return [status: STATUS_ERROR, result: result]
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.pendingOffset = 0
        result.acceptedOffset = 0
        result.dashboardDueDatesOffset = 0
        switch(params.view) {
            case 'PendingChanges': result.pendingOffset = result.offset
                break
            case 'AcceptedChanges': result.acceptedOffset = result.offset
                break
            case 'dueDatesView': result.dashboardDueDatesOffset = result.offset
                break
        }

        def periodInDays = result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)

        // changes

        Map<String,Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"),periodInDays:periodInDays,max:result.max,pendingOffset:result.pendingOffset,acceptedOffset:result.acceptedOffset,pending:true,notifications:true]
        pu.setBenchmark('pending changes')
        result.putAll(pendingChangeService.getChanges(pendingChangeConfigMap))

        // systemAnnouncements
        pu.setBenchmark('system announcements')
        result.systemAnnouncements = SystemAnnouncement.getPublished(periodInDays)

        // tasks

        SimpleDateFormat sdFormat    = DateUtils.getSDF_NoTime()
        params.taskStatus = 'not done'
        def query       = filterService.getTaskQuery(params << [sort: 't.endDate', order: 'asc'], sdFormat)
        pu.setBenchmark('tasks')
        result.tasks    = taskService.getTasksByResponsibles(result.user, result.institution, query)
        result.tasksCount    = result.tasks.size()
        result.enableMyInstFormFields = true // enable special form fields


        /*def announcement_type = RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: result.max,offset:result.announcementOffset, sort: 'dateCreated', order: 'desc'])
        result.recentAnnouncementsCount = Doc.findAllByType(announcement_type).size()*/
        pu.setBenchmark('due dates')
        result.dueDates = dashboardDueDatesService.getDashboardDueDates( result.user, result.org, false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.getDashboardDueDates( result.user, result.org, false, false).size()
        pu.setBenchmark('surveys')
        List activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is null and surConfig.pickAndChoose = true and surConfig.surveyInfo.status = :status) " +
                " or exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surConfig.surveyInfo.status = :status and surResult.finishDate is null and surResult.participant = :org) " +
                " order by surConfig.surveyInfo.endDate",
                [org: result.institution,
                 status: RDStore.SURVEY_SURVEY_STARTED])

        if(accessService.checkPerm('ORG_CONSORTIUM')){
            activeSurveyConfigs = SurveyConfig.executeQuery("from SurveyConfig surConfig where surConfig.surveyInfo.status = :status  and surConfig.surveyInfo.owner = :org " +
                    " order by surConfig.surveyInfo.endDate",
                    [org: result.institution,
                     status: RDStore.SURVEY_SURVEY_STARTED])
        }

        result.surveys = activeSurveyConfigs.groupBy {it?.id}
        result.countSurvey = result.surveys.size()

        result.benchMark = pu.stopBenchmark()
        [status: STATUS_OK, result: result]
    }

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenerics(MyInstitutionController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        User user = contextService.getUser()
        Org org = contextService.getOrg()

//        switch (params.action){
//            case 'currentSurveys':
//            case 'surveyInfos':
//            case 'surveyInfoFinish':
//            case 'surveyInfosIssueEntitlements':
//            case 'surveyResultFinish':
//                result.user = user
//                break
//            default:
//                result.user = user
//        }

        result.user = user
        result.institution = org
        result.contextCustomerType = org.getCustomerType()
        switch (params.action) {
            case 'processEmptyLicense': //to be moved to LicenseController
            case 'currentLicenses':
            case 'currentSurveys':
            case 'dashboard':
            case 'emptyLicense': //to be moved to LicenseController
            case 'surveyInfoFinish':
            case 'surveyResultFinish':
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR')
                break
            case 'addressbook':
            case 'budgetCodes':
            case 'tasks':
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
                break
            case 'surveyInfos':
                result.editable = surveyService.isEditableSurvey(org, SurveyInfo.get(params.id) ?: null)
                break
            case 'surveyInfosIssueEntitlements':
                result.editable = surveyService.isEditableIssueEntitlementsSurvey(org, SurveyConfig.get(params.id))
                break
            case 'userList':
                result.editable = user.hasRole('ROLE_ADMIN') || user.hasAffiliation('INST_ADM')
                break
            case 'managePropertyDefinitions':
                result.editable = false
                break
            default:
                result.editable = accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA')
        }

        result
    }
}