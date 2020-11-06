package de.laser.ctrl

import de.laser.MyInstitutionController
import de.laser.Org
import de.laser.SurveyConfig
import de.laser.UserSetting
import de.laser.auth.User
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import de.laser.system.SystemAnnouncement
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

@Transactional
class MyInstitutionControllerService {

    def accessService
    def contextService
    def dashboardDueDatesService
    def filterService
    def pendingChangeService
    def springSecurityService
    def taskService

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    Map<String, Object> dashboard(MyInstitutionController controller, GrailsParameterMap params) {

        Map<String, Object> result = getResultGenerics(controller, params)

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            return [status: STATUS_ERROR, result: result]
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')
        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0
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

        def periodInDays = contextService.getUser().getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)

        // changes

        Map<String,Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"),periodInDays:periodInDays,max:result.max,pendingOffset:result.pendingOffset,acceptedOffset:result.acceptedOffset,pending:true,notifications:true]

        result.putAll(pendingChangeService.getChanges(pendingChangeConfigMap))

        // systemAnnouncements

        result.systemAnnouncements = SystemAnnouncement.getPublished(periodInDays)

        // tasks

        SimpleDateFormat sdFormat    = DateUtil.getSDF_NoTime()
        params.taskStatus = 'not done'
        def query       = filterService.getTaskQuery(params << [sort: 't.endDate', order: 'asc'], sdFormat)
        Org contextOrg  = contextService.getOrg()
        result.tasks    = taskService.getTasksByResponsibles(springSecurityService.getCurrentUser(), contextOrg, query)
        result.tasksCount    = result.tasks.size()
        result.enableMyInstFormFields = true // enable special form fields


        /*def announcement_type = RefdataValue.getByValueAndCategory('Announcement', RDConstants.DOCUMENT_TYPE)
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: result.max,offset:result.announcementOffset, sort: 'dateCreated', order: 'desc'])
        result.recentAnnouncementsCount = Doc.findAllByType(announcement_type).size()*/

        result.dueDates = dashboardDueDatesService.getDashboardDueDates( contextService.user, contextService.org, false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.getDashboardDueDates(contextService.user, contextService.org, false, false).size()

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


        [status: STATUS_OK, result: result]
    }

    Map<String, Object> getResultGenerics(MyInstitutionController controller, GrailsParameterMap params) {

        Map<String, Object> result = [:]

        switch(params.action){
            case 'currentSurveys':
            case 'surveyInfos':
            case 'surveyInfoFinish':
            case 'surveyInfosIssueEntitlements':
            case 'surveyResultFinish':
                result.user = User.get(springSecurityService.principal.id)
                break
            default:
                result.user = contextService.getUser()
        }
        result.institution = contextService.getOrg()
        result.editable = controller.checkIsEditable(result.user, result.institution)

        result
    }
}