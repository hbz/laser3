package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.ctrl.FinanceControllerService
import de.laser.ctrl.SubscriptionControllerService
import de.laser.ctrl.SurveyControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.finance.Order
import de.laser.finance.PriceItem
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.finance.CostItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyLinks
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyUrl
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService

/**
 * This controller manages the survey-related calls
 * @see SurveyConfig
 * @see SurveyResult
 * @see SurveyInfo
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class SurveyController {

    AccessService accessService
    ContextService contextService
    ComparisonService comparisonService
    CompareService compareService
    CopyElementsService copyElementsService
    CustomWkhtmltoxService wkhtmltoxService
    DocstoreService docstoreService
    EscapeService escapeService
    ExecutorService executorService
    ExportClickMeService exportClickMeService
    ExportService exportService
    GenericOIDService genericOIDService
    FilterService filterService
    FinanceControllerService financeControllerService
    FinanceService financeService
    LinksGenerationService linksGenerationService
    OrgTypeService orgTypeService
    PropertyService propertyService
    SubscriptionService subscriptionService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyControllerService surveyControllerService
    SurveyService surveyService
    TaskService taskService
    UserService userService

    //-----

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'myInstitution/currentSurveys' : 'currentSurveys.label'
    ]

    //-----

    /**
     * Redirects the call to the survey details view
     * @return the survey details view of the given survey
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> redirectSurveyConfig() {
        SurveyConfig surveyConfig = SurveyConfig.get(params.id)

        redirect(action: 'show', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id])

    }

    /**
     * Loads for the context consortium the currently running surveys
     * @return a table view of the current surveys
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> currentSurveysConsortia() {
        Map<String, Object> result = [:]
        Profiler prf = new Profiler()
        prf.setBenchmark("init")
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.max = result.max
        params.offset = result.offset
        //params.filterStatus = params.filterStatus ?: ((params.size() > 4) ? "" : [RDStore.SURVEY_SURVEY_STARTED.id.toString(), RDStore.SURVEY_READY.id.toString(), RDStore.SURVEY_IN_PROCESSING.id.toString()])
        prf.setBenchmark("before properties")

        result.propList = PropertyDefinition.executeQuery( "select surpro.surveyProperty from SurveyConfigProperties as surpro join surpro.surveyConfig surConfig join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg order by surpro.surveyProperty.name_de asc", [contextOrg: result.institution]).groupBy {it}.collect {it.key}

        prf.setBenchmark("after properties")

        prf.setBenchmark("before surveyYears")
        result.surveyYears = SurveyInfo.executeQuery("select Year(startDate) from SurveyInfo where owner = :org and startDate != null group by YEAR(startDate) order by YEAR(startDate)", [org: result.institution]) ?: []

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears << newYear
            }
            params.validOnYear = [newYear]
        }

        prf.setBenchmark("after surveyYears and before current org ids of providers and agencies")
        result.providers = orgTypeService.getCurrentOrgsOfProvidersAndAgencies( (Org) result.institution )
        prf.setBenchmark("after providers and agencies and before subscriptions")
        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIA, 'activeInst': result.institution])
        prf.setBenchmark("after subscriptions and before survey config query")
        Map<String,Object> fsq = filterService.getSurveyConfigQueryConsortia(params, DateUtils.getLocalizedSDF_noTime(), (Org) result.institution)
        prf.setBenchmark("after query, before survey config execution")
        result.surveys = SurveyInfo.executeQuery(fsq.query, fsq.queryParams, params)
        prf.setBenchmark("after survey config execute")
        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            if ( params.surveyCostItems ) {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "surveyCostItems.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems(result.surveys.collect {it[1]}, result.institution)
            }else{
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.plural")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys(result.surveys.collect {it[1]}, result.institution)
            }
            
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return

        }else {
            prf.setBenchmark("before surveysCount")
            result.surveysCount = SurveyInfo.executeQuery(fsq.query, fsq.queryParams).size()
            result.filterSet = params.filterSet ? true : false
            prf.setBenchmark("output")
            result.benchMark = prf.stopBenchmark()
            result
        }
    }

    /**
     * Loads the current surveys with their workflows for the given consortium. The workflow list may also be exported as Excel worksheet
     * @return the table view of the current surveys with their attached workflows
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> workflowsSurveysConsortia() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.max = result.max
        params.offset = result.offset

        params.tab = params.tab ?: 'created'

        result.surveyYears = SurveyInfo.executeQuery("select Year(startDate) from SurveyInfo where owner = :org and startDate != null group by YEAR(startDate) order by YEAR(startDate)", [org: result.institution]) ?: []

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears << newYear
            }
            params.validOnYear = [newYear]
        }

        result.propList = PropertyDefinition.executeQuery( "select surpro.surveyProperty from SurveyConfigProperties as surpro join surpro.surveyConfig surConfig join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg order by surpro.surveyProperty.name_de asc", [contextOrg: result.institution]).groupBy {it}.collect {it.key}

        result.providers = orgTypeService.getCurrentOrgsOfProvidersAndAgencies( contextService.getOrg() )

        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIA, 'activeInst': result.institution])


        DateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        Map<String,Object> fsq = filterService.getSurveyConfigQueryConsortia(params, sdFormat, result.institution)

        result.surveys = SurveyInfo.executeQuery(fsq.query, fsq.queryParams, params)

        if ( params.exportXLSX ) {
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.plural")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportSurveys(SurveyConfig.findAllByIdInList(result.surveys.collect {it[1].id}), result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return

        }else {
            result.surveysCount = SurveyInfo.executeQuery(fsq.query, fsq.queryParams).size()
            result.countSurveyConfigs = surveyService.getSurveyConfigCounts(params)

            result.filterSet = params.filterSet ? true : false

            result
        }

    }

    /**
     * Call to create a general survey
     * @return the new survey form
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> createGeneralSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result
    }

    /**
     * Takes the given parameters and creates a new general survey based on the input
     * @return the new survey details view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateGeneralSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Date startDate = params.startDate ? sdf.parse(params.startDate) : null
        Date endDate = params.endDate ? sdf.parse(params.endDate) : null

        if(startDate != null && endDate != null) {
            if(startDate > endDate) {
                flash.error = g.message(code: "createSurvey.create.fail.startDateAndEndDate")
                redirect(action: 'createGeneralSurvey', params: params)
                return
            }
        }
        SurveyInfo surveyInfo
        SurveyInfo.withTransaction { TransactionStatus ts ->
                surveyInfo = new SurveyInfo(
                    name: params.name,
                    startDate: startDate,
                    endDate: endDate,
                    type: RDStore.SURVEY_TYPE_INTEREST,
                    owner: contextService.getOrg(),
                    status: RDStore.SURVEY_IN_PROCESSING,
                    comment: params.comment ?: null,
                    isSubscriptionSurvey: false,
                    isMandatory: params.mandatory ?: false
            )
            if (!(surveyInfo.save())) {
                flash.error = g.message(code: "createGeneralSurvey.create.fail")
                redirect(action: 'createGeneralSurvey', params: params)
                return
            }
            if (!SurveyConfig.findAllBySurveyInfo(surveyInfo)) {
                SurveyConfig surveyConfig = new SurveyConfig(
                        type: 'GeneralSurvey',
                        surveyInfo: surveyInfo,
                        configOrder: 1
                )
                if(!(surveyConfig.save())){
                    surveyInfo.delete()
                    flash.error = g.message(code: "createGeneralSurvey.create.fail")
                    redirect(action: 'createGeneralSurvey', params: params)
                    return
                }
            }
        }


        //flash.message = g.message(code: "createGeneralSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id
    }

    /**
     * Call to create a subscription survey
     * @return the new survey form
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> createSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        def date_restriction
        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        Set orgIds = orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( contextService.getOrg() )

        result.providers = orgIds.isEmpty() ? [] : Org.findAllByIdInList(orgIds, [sort: 'name'])

        List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())

        result.filterSet = tmpQ[2]
        List subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )
        //,[max: result.max, offset: result.offset]

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.getOrg())

        if (params.sort && params.sort.indexOf("§") >= 0) {
            switch (params.sort) {
                case "orgRole§provider":
                    subscriptions.sort { x, y ->
                        String a = x.getProviders().size() > 0 ? x.getProviders().first().name : ''
                        String b = y.getProviders().size() > 0 ? y.getProviders().first().name : ''
                        a.compareToIgnoreCase b
                    }
                    if (params.order.equals("desc"))
                        subscriptions.reverse(true)
                    break
            }
        }
        result.num_sub_rows = subscriptions.size()
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        result.allLinkedLicenses = [:]
        Set<Links> allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndLinkType(result.subscriptions ,RDStore.LINKTYPE_LICENSE)
        allLinkedLicenses.each { Links li ->
            Subscription s = li.destinationSubscription
            License l = li.sourceLicense
            Set<License> linkedLicenses = result.allLinkedLicenses.get(s)
            if(!linkedLicenses)
                linkedLicenses = []
            linkedLicenses << l
            result.allLinkedLicenses.put(s,linkedLicenses)
        }

        result

    }

    /**
     * Call to create a title survey
     * @return the new survey form
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> createIssueEntitlementsSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        def date_restriction

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_INTENDED.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        Set orgIds = orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies( contextService.getOrg() )

        result.providers = orgIds.isEmpty() ? [] : Org.findAllByIdInList(orgIds, [sort: 'name'])

        List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())
        result.filterSet = tmpQ[2]
        List subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )
        //,[max: result.max, offset: result.offset]

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.getOrg())

        if (params.sort && params.sort.indexOf("§") >= 0) {
            switch (params.sort) {
                case "orgRole§provider":
                    subscriptions.sort { x, y ->
                        String a = x.getProviders().size() > 0 ? x.getProviders().first().name : ''
                        String b = y.getProviders().size() > 0 ? y.getProviders().first().name : ''
                        a.compareToIgnoreCase b
                    }
                    if (params.order.equals("desc"))
                        subscriptions.reverse(true)
                    break
            }
        }
        result.num_ies_rows = subscriptions.size()
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        result.allLinkedLicenses = [:]
        if(result.subscriptions) {
            Set<Links> allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndLinkType(result.subscriptions, RDStore.LINKTYPE_LICENSE)
            allLinkedLicenses.each { Links li ->
                Subscription s = li.destinationSubscription
                License l = li.sourceLicense
                Set<License> linkedLicenses = result.allLinkedLicenses.get(s)
                if (!linkedLicenses)
                    linkedLicenses = []
                linkedLicenses << l
                result.allLinkedLicenses.put(s, linkedLicenses)
            }
        }

        result

    }

    /**
     * Call to add a subscription to a given survey
     * @return the survey details form in case of success, the survey creation form if the subscription is missing,
     * a redirect to the referer otherwise
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSubtoSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.subscription = Subscription.get(Long.parseLong(params.sub))
        if (!result.subscription) {
            redirect action: 'createSubscriptionSurvey'
            return
        }

        result

    }

    /**
     * Call to add a subscription to the given title survey
     * @return the survey details form in case of success, the survey creation form if the subscription is missing,
     * a redirect to the referer otherwise
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSubtoIssueEntitlementsSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.subscription = Subscription.get(Long.parseLong(params.sub))
        result.pickAndChoose = true
        if (!result.subscription) {
            redirect action: 'createIssueEntitlementsSurvey'
            return
        }

        result

    }

    /**
     * Takes the given parameters and creates a new subscription survey based on the input
     * @return the new survey details view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Date startDate = params.startDate ? sdf.parse(params.startDate) : null
        Date endDate = params.endDate ? sdf.parse(params.endDate) : null

        if(startDate != null && endDate != null) {
            if(startDate > endDate) {
                flash.error = g.message(code: "createSurvey.create.fail.startDateAndEndDate")
                redirect(action: 'addSubtoSubscriptionSurvey', params: params)
                return
            }
        }

        Subscription subscription = Subscription.get(Long.parseLong(params.sub))
        boolean subSurveyUseForTransfer = (SurveyConfig.findAllBySubscriptionAndSubSurveyUseForTransfer(subscription, true)) ? false : (params.subSurveyUseForTransfer ? true : false)
        SurveyInfo surveyInfo
        SurveyInfo.withTransaction { TransactionStatus ts ->
                    surveyInfo = new SurveyInfo(
                    name: params.name,
                    startDate: startDate,
                    endDate: endDate,
                    type: subSurveyUseForTransfer ? RDStore.SURVEY_TYPE_RENEWAL : RDStore.SURVEY_TYPE_SUBSCRIPTION,
                    owner: contextService.getOrg(),
                    status: RDStore.SURVEY_IN_PROCESSING,
                    comment: params.comment ?: null,
                    isSubscriptionSurvey: true,
                    isMandatory: subSurveyUseForTransfer ? true : (params.mandatory ?: false)
            )

            if (!(surveyInfo.save())) {
                flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
                redirect(action: 'addSubtoSubscriptionSurvey', params: params)
                return
            }

            if (subscription && !SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo)) {
                SurveyConfig surveyConfig = new SurveyConfig(
                        subscription: subscription,
                        configOrder: surveyInfo.surveyConfigs ? surveyInfo.surveyConfigs.size() + 1 : 1,
                        type: 'Subscription',
                        surveyInfo: surveyInfo,
                        subSurveyUseForTransfer: subSurveyUseForTransfer

                )

                surveyConfig.save()

                //Wenn es eine Umfrage schon gibt, die als Übertrag dient. Dann ist es auch keine Lizenz Umfrage mit einem Teilnahme-Merkmal abfragt!
                if (subSurveyUseForTransfer) {
                    SurveyConfigProperties configProperty = new SurveyConfigProperties(
                            surveyProperty: PropertyStore.SURVEY_PROPERTY_PARTICIPATION,
                            surveyConfig: surveyConfig,
                            mandatoryProperty: true)

                    SurveyConfigProperties configProperty2 = new SurveyConfigProperties(
                            surveyProperty: PropertyStore.SURVEY_PROPERTY_ORDER_NUMBER,
                            surveyConfig: surveyConfig)

                    if (configProperty.save() && configProperty2.save()) {
                        surveyService.addSubMembers(surveyConfig)
                    }
                }
            }
            else {
                surveyInfo.delete()
                flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
                redirect(action: 'addSubtoSubscriptionSurvey', params: params)
                return
            }
        }

        //flash.message = g.message(code: "createSubscriptionSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id

    }

    /**
     * Takes the given parameters and creates a new title survey based on the input
     * @return the new survey details view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateIssueEntitlementsSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Date startDate = params.startDate ? sdf.parse(params.startDate) : null
        Date endDate = params.endDate ? sdf.parse(params.endDate) : null

        if(startDate != null && endDate != null) {
            if(startDate > endDate) {
                flash.error = g.message(code: "createSurvey.create.fail.startDateAndEndDate")
                redirect(action: 'addSubtoIssueEntitlementsSurvey', params: params)
                return
            }
        }

        if(params.issueEntitlementGroupNew == ''){
            flash.error = g.message(code: "createSurvey.create.fail.issueEntitlementGroupNew")
            redirect(action: 'addSubtoIssueEntitlementsSurvey', params: params)
            return
        }

        SurveyInfo surveyInfo
        SurveyInfo.withTransaction { TransactionStatus ts ->
            surveyInfo = new SurveyInfo(
                    name: params.name,
                    startDate: startDate,
                    endDate: endDate,
                    type: RDStore.SURVEY_TYPE_TITLE_SELECTION,
                    owner: contextService.getOrg(),
                    status: RDStore.SURVEY_IN_PROCESSING,
                    comment: params.comment ?: null,
                    isSubscriptionSurvey: true,
                    isMandatory: params.mandatory ? true : false
            )
            if (!(surveyInfo.save())) {
                flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
                redirect(action: 'addSubtoIssueEntitlementsSurvey', params: params)
                return
            }
            Subscription subscription = Subscription.get(Long.parseLong(params.sub))
            if (subscription && !SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo)) {
                SurveyConfig surveyConfig = new SurveyConfig(
                        subscription: subscription,
                        configOrder: surveyInfo.surveyConfigs?.size() ? surveyInfo.surveyConfigs.size() + 1 : 1,
                        type: 'IssueEntitlementsSurvey',
                        surveyInfo: surveyInfo,
                        subSurveyUseForTransfer: false,
                        pickAndChoose: true,
                        pickAndChoosePerpetualAccess: params.pickAndChoosePerpetualAccess ? true : false,
                        issueEntitlementGroupName: params.issueEntitlementGroupNew
                )
                surveyConfig.save()
                surveyService.addSubMembers(surveyConfig)
            }
            else {
                surveyInfo.delete()
                flash.error = g.message(code: "createIssueEntitlementsSurvey.create.fail")
                redirect(action: 'addSubtoIssueEntitlementsSurvey', params: params)
                return
            }
        }
        //flash.message = g.message(code: "createIssueEntitlementsSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id
    }

    /**
     * Shows the given survey's details
     * @return the survey details view
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    @Check404(domain=SurveyInfo)
    def show() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        result.surveyLinksMessage = []

        if(SurveyLinks.executeQuery("from SurveyLinks where sourceSurvey = :surveyInfo and sourceSurvey.status = :startStatus and targetSurvey.status != sourceSurvey.status", [surveyInfo: result.surveyInfo, startStatus: RDStore.SURVEY_SURVEY_STARTED]).size() > 0){
            result.surveyLinksMessage  << message(code: 'surveyLinks.surveysNotStartet')
        }

        if(SurveyLinks.executeQuery("from SurveyLinks where sourceSurvey = :surveyInfo and sourceSurvey.status = :startStatus and targetSurvey.status = sourceSurvey.status and targetSurvey.endDate != sourceSurvey.endDate", [surveyInfo: result.surveyInfo, startStatus: RDStore.SURVEY_SURVEY_STARTED]).size() > 0){
            result.surveyLinksMessage  << message(code: 'surveyLinks.surveysNotSameEndDate')
        }

        if(params.commentTab){
            result.commentTab = params.commentTab
        }


        if(result.surveyInfo.surveyConfigs.size() >= 1  || params.surveyConfigID) {

            result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : result.surveyInfo.surveyConfigs[0]

            result.navigation = surveyService.getConfigNavigation(result.surveyInfo,  result.surveyConfig)

            if ( result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                result.authorizedOrgs = result.user.getAffiliationOrgs()

                // restrict visible for templates/links/orgLinksAsList
                result.visibleOrgRelations = []
                 result.surveyConfig.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

                result.subscription =  result.surveyConfig.subscription ?: null

                //costs dataToDisplay
               result.dataToDisplay = ['own','cons']
               result.offsets = [consOffset:0,ownOffset:0]
               result.sortConfig = [consSort:'oo.org.sortname',consOrder:'asc',
                                    ownSort:'ci.costTitle',ownOrder:'asc']

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                //cost items
                //params.forExport = true
                LinkedHashMap costItems = result.subscription ? financeService.getCostItemsForSubscription(params, result) : null
                result.costItemSums = [:]
                if (costItems?.own) {
                    result.costItemSums.ownCosts = costItems.own.sums
                }
                if (costItems?.cons) {
                    result.costItemSums.consCosts = costItems.cons.sums
                }
                result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)

                if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION]) {
                    result.successorSubscription = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                    Collection<AbstractPropertyWithCalculatedLastUpdated> props
                    props = result.surveyConfig.subscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                    if(result.successorSubscription){
                        props += result.successorSubscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                    }
                    result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
                }

            }
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg,  result.surveyConfig)

        }

        if ( params.exportXLSX ) {

            SXSSFWorkbook wb
            if ( params.surveyCostItems ) {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems([result.surveyConfig], result.institution)
            }else{
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys([result.surveyConfig], result.institution)
            }
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else {
            result
        }
    }

    /**
     * Lists the titles subject of the given survey
     * @return a list view of the issue entitlements linked to the given survey
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyTitles() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        /*String base_qry = null
        Map<String,Object> qry_params = [subscription: result.surveyConfig.subscription]

        Date date_filter
        date_filter = new Date()
        result.as_at_date = date_filter
        base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
        base_qry += " and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) ) "
        qry_params.startDate = date_filter
        qry_params.endDate = date_filter*/

        def query = filterService.getIssueEntitlementQuery(params, result.surveyConfig.subscription)
        result.filterSet = query.filterSet

        result.num_ies_rows = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams).size()

        result.entitlements = IssueEntitlement.executeQuery("select ie " + query.query, query.queryParams, [max: result.max, offset: result.offset])

        result

    }

    /**
     * Lists the documents attached to the given survey configuration
     * @return a list of the survey config documents
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyConfigDocs() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        result

    }

    /**
     * Lists the participants of the given survey. The result may be filtered
     * @return the participant list for the given tab
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyParticipants() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        // new: filter preset
        params.orgType = RDStore.OT_INSTITUTION.id.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU.id.toString()

        params.subStatus = (params.filterSet && !params.subStatus) ? null : (params.subStatus ?: RDStore.SUBSCRIPTION_CURRENT.id.toString())

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        params.sub = result.subscription
        Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
        String tmpQuery = "select o.id " + fsq.query.minus("select o ")
        List consortiaMemberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

        if (params.filterPropDef && consortiaMemberIds) {
            fsq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname", 'o', [oids: consortiaMemberIds])
        }
        result.consortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams, params)

        if(result.surveyConfig.pickAndChoose){

            List orgs = subscriptionService.getValidSurveySubChildOrgs(result.surveyConfig.subscription)
            result.consortiaMembers = orgs ? result.consortiaMembers.findAll{ (it.id in orgs.id)} : []
        }

        result.consortiaMembersCount = Org.executeQuery(fsq.query, fsq.queryParams).size()

        result.editable = (result.surveyInfo && result.surveyInfo.status.id != RDStore.SURVEY_IN_PROCESSING.id) ? false : result.editable

        Map<String,Object> surveyOrgs = result.surveyConfig.getSurveyOrgsIDs()

        result.selectedParticipants = surveyService.getfilteredSurveyOrgs(surveyOrgs.orgsWithoutSubIDs, fsq.query, fsq.queryParams, params)
        result.selectedSubParticipants = surveyService.getfilteredSurveyOrgs(surveyOrgs.orgsWithSubIDs, fsq.query, fsq.queryParams, params)

        params.tab = params.tab ?: (result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY ? 'selectedParticipants' : 'selectedSubParticipants')

        result

    }

    /**
     * Lists the costs linked to the given survey, reflecting upcoming subscription costs
     * @return a list of costs linked to the survey
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyCostItems() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        result.putAll(financeControllerService.getEditVars(result.institution))

        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }

        result.orgConfigurations = orgConfigurations as JSON

        params.tab = params.tab ?: 'selectedSubParticipants'

        // new: filter preset
        params.orgType = RDStore.OT_INSTITUTION.id.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU.id.toString()

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
        String tmpQuery = "select o.id " + fsq.query.minus("select o ")
        List consortiaMemberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

        if (params.filterPropDef && consortiaMemberIds) {
            fsq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname", 'o', [oids: consortiaMemberIds])
        }

        result.editable = (result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        //Only SurveyConfigs with Subscriptions
        result.surveyConfigs = result.surveyInfo.surveyConfigs.findAll { it.subscription != null }.sort {
            it.configOrder
        }

        params.surveyConfigID = params.surveyConfigID ?: result.surveyConfigs[0].id.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        Map<String,Object> surveyOrgs = result.surveyConfig?.getSurveyOrgsIDs()

        result.selectedParticipants = surveyService.getfilteredSurveyOrgs(surveyOrgs.orgsWithoutSubIDs, fsq.query, fsq.queryParams, params)
        result.selectedSubParticipants = surveyService.getfilteredSurveyOrgs(surveyOrgs.orgsWithSubIDs, fsq.query, fsq.queryParams, params)

        result.selectedCostItemElement = params.selectedCostItemElement ? params.selectedCostItemElement.toString() : RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id.toString()

        if(result.selectedSubParticipants && (params.sortOnCostItemsDown || params.sortOnCostItemsUp) && !params.sort){
            List<Subscription> orgSubscriptions = result.surveyConfig.orgSubscriptions()
            List<Org> selectedSubParticipants = result.selectedSubParticipants
            result.selectedSubParticipants = []

            String orderByQuery = " order by c.costInBillingCurrency"

            if(params.sortOnCostItemsUp){
                result.sortOnCostItemsUp = true
                orderByQuery = " order by c.costInBillingCurrency DESC"
                params.remove('sortOnCostItemsUp')
            }else {
                params.remove('sortOnCostItemsDown')
            }

            String query = "select c.sub from CostItem as c where c.sub in (:subList) and c.owner = :owner and c.costItemStatus != :status and c.costItemElement.id = :costItemElement " + orderByQuery

            List<Subscription> subscriptionList =  CostItem.executeQuery(query, [subList: orgSubscriptions, owner: result.surveyInfo.owner, status: RDStore.COST_ITEM_DELETED, costItemElement: Long.parseLong(result.selectedCostItemElement)])

            subscriptionList.each { Subscription sub ->
                Org org = sub.getSubscriber()
                if(selectedSubParticipants && org && org.id in selectedSubParticipants.id)
                    result.selectedSubParticipants << sub.getSubscriber()
            }
        }

        if (params.selectedCostItemElement) {
            params.remove('selectedCostItemElement')
        }

        result.idSuffix ="surveyCostItemsBulk"
        result

    }

    /**
     * Takes the given input and processes the change on the selected survey cost items
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processSurveyCostItemsBulk() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.putAll(financeControllerService.getEditVars(result.institution))
        List selectedMembers = params.list("selectedOrgs")

        if(selectedMembers) {

            RefdataValue billing_currency = null
            if (params.long('newCostCurrency')) //GBP,etc
            {
                billing_currency = RefdataValue.get(params.newCostCurrency)
            }
            SimpleDateFormat dateFormat = DateUtils.getLocalizedSDF_noTime()
            Closure newDate = { param, format ->
                Date date
                try {
                    date = dateFormat.parse(param)
                } catch (Exception e) {
                    log.debug("Unable to parse date : ${param} in format ${format}")
                }
                date
            }

            Date startDate = newDate(params.newStartDate, dateFormat.toPattern())
            Date endDate = newDate(params.newEndDate, dateFormat.toPattern())

            RefdataValue cost_item_status = (params.newCostItemStatus && params.newCostItemStatus != RDStore.GENERIC_NULL_VALUE.id.toString()) ? (RefdataValue.get(params.long('newCostItemStatus'))) : null
            RefdataValue cost_item_element = params.newCostItemElement ? (RefdataValue.get(params.long('newCostItemElement'))) : null
            RefdataValue cost_item_element_configuration = (params.ciec && params.ciec != 'null') ? RefdataValue.get(Long.parseLong(params.ciec)) : null

            String costDescription = params.newDescription ? params.newDescription.trim() : null
            String costTitle = params.newCostTitle ? params.newCostTitle.trim() : null

            Boolean billingSumRounding = params.newBillingSumRounding == 'on'
            Boolean finalCostRounding = params.newFinalCostRounding == 'on'

            NumberFormat format = NumberFormat.getInstance(LocaleUtils.getCurrentLocale())
            def cost_billing_currency = params.newCostInBillingCurrency ? format.parse(params.newCostInBillingCurrency).doubleValue() : null //0.00


            def tax_key = null
            if (!params.newTaxRate.contains("null")) {
                String[] newTaxRate = params.newTaxRate.split("§")
                RefdataValue taxType = (RefdataValue) genericOIDService.resolveOID(newTaxRate[0])
                int taxRate = Integer.parseInt(newTaxRate[1])
                switch (taxType.id) {
                    case RDStore.TAX_TYPE_TAXABLE.id:
                        switch (taxRate) {
                            case 5: tax_key = CostItem.TAX_TYPES.TAXABLE_5
                                break
                            case 7: tax_key = CostItem.TAX_TYPES.TAXABLE_7
                                break
                            case 16: tax_key = CostItem.TAX_TYPES.TAXABLE_16
                                break
                            case 19: tax_key = CostItem.TAX_TYPES.TAXABLE_19
                                break
                        }
                        break
                    case RDStore.TAX_TYPE_TAXABLE_EXEMPT.id:
                        tax_key = CostItem.TAX_TYPES.TAX_EXEMPT
                        break
                    case RDStore.TAX_TYPE_NOT_TAXABLE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                        break
                    case RDStore.TAX_TYPE_NOT_APPLICABLE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                        break
                    case RDStore.TAX_TYPE_REVERSE_CHARGE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_REVERSE_CHARGE
                        break
                }

            }
            CostItem.withTransaction { TransactionStatus ts ->
                List<CostItem> surveyCostItems = CostItem.executeQuery('select costItem from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and surOrg.org.id in (:orgIDs) and costItem.costItemStatus != :status', [survConfig:  result.surveyConfig, orgIDs: selectedMembers.collect{Long.parseLong(it)}, status: RDStore.COST_ITEM_DELETED])
                surveyCostItems.each { surveyCostItem ->
                    if(params.deleteCostItems == "true") {
                        surveyCostItem.delete()
                    }
                    else {
                        if (params.percentOnOldPrice) {
                            Double percentOnOldPrice = params.double('percentOnOldPrice', 0.00)
                            Subscription orgSub = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(surveyCostItem.surveyOrg.org)
                            CostItem costItem = CostItem.findBySubAndOwnerAndCostItemStatusNotEqualAndCostItemElement(orgSub, surveyCostItem.owner, RDStore.COST_ITEM_DELETED, RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE)
                            surveyCostItem.costInBillingCurrency = costItem ? (costItem.costInBillingCurrency * (1 + (percentOnOldPrice / 100))).round(2) : surveyCostItem.costInBillingCurrency
                        }
                        else {
                            surveyCostItem.costInBillingCurrency = cost_billing_currency ?: surveyCostItem.costInBillingCurrency
                        }

                        surveyCostItem.costItemElement = cost_item_element ?: surveyCostItem.costItemElement
                        surveyCostItem.costItemStatus = cost_item_status ?: surveyCostItem.costItemStatus
                        surveyCostItem.costTitle = costTitle ?: surveyCostItem.costTitle

                        surveyCostItem.costItemElementConfiguration = cost_item_element_configuration ?: surveyCostItem.costItemElementConfiguration

                        surveyCostItem.costDescription = costDescription ?: surveyCostItem.costDescription

                        surveyCostItem.startDate = startDate ?: surveyCostItem.startDate
                        surveyCostItem.endDate = endDate ?: surveyCostItem.endDate

                        surveyCostItem.billingCurrency = billing_currency ?: surveyCostItem.billingCurrency
                        //Not specified default to GDP
                        //surveyCostItem.costInLocalCurrency = cost_local_currency ?: surveyCostItem.costInLocalCurrency
                        surveyCostItem.billingSumRounding = billingSumRounding != surveyCostItem.billingSumRounding ? billingSumRounding : surveyCostItem.billingSumRounding
                        surveyCostItem.finalCostRounding = finalCostRounding != surveyCostItem.finalCostRounding ? finalCostRounding : surveyCostItem.finalCostRounding

                        //println( params.newFinalCostRounding)
                        //println( Boolean.valueOf(params.newFinalCostRounding))
                        //surveyCostItem.currencyRate = cost_currency_rate ?: surveyCostItem.currencyRate
                        surveyCostItem.taxKey = tax_key ?: surveyCostItem.taxKey
                        surveyCostItem.save()
                    }
                }
            }

        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Marks the given survey as finished
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyConfigFinish() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyConfig.configFinish = params.configFinish ?: false
        SurveyConfig.withTransaction { TransactionStatus ts ->
            if (result.surveyConfig.save()) {
                //flash.message = g.message(code: 'survey.change.successfull')
            } else {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the renewal sending flag as done for the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> workflowRenewalSent() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyInfo.isRenewalSent = params.renewalSent ?: false
        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (result.surveyInfo.save()) {
                //flash.message = g.message(code: 'survey.change.successfull')
            } else {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the establishment of survey cost items as done for the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> workflowCostItemsFinish() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyConfig.costItemsFinish = params.costItemsFinish ?: false

        SurveyConfig.withTransaction { TransactionStatus ts ->
            if (!result.surveyConfig.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the given survey property (i.e. survey question) as mandatory to fill out
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyPropertyMandatory() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.get(params.surveyConfigProperties)

        surveyConfigProperties.mandatoryProperty = params.mandatoryProperty ?: false

        SurveyConfigProperties.withTransaction { TransactionStatus ts ->
            if (!surveyConfigProperties.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the given survey as completed; is a toggle between survey completed and survey in evaluation
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyCompleted() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyInfo.status = params.surveyCompleted ? RDStore.SURVEY_COMPLETED : RDStore.SURVEY_IN_EVALUATION

        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (!result.surveyInfo.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the given transfer procedure as (un-)checked
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyTransferConfig() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        Map transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : [:]

        if(params.transferMembers != null)
        {
            transferWorkflow.transferMembers = params.transferMembers
        }

        if(params.transferSurveyCostItems != null)
        {
            transferWorkflow.transferSurveyCostItems = params.transferSurveyCostItems
        }

        if(params.transferSurveyProperties != null)
        {
            transferWorkflow.transferSurveyProperties = params.transferSurveyProperties
        }

        if(params.transferCustomProperties != null)
        {
            transferWorkflow.transferCustomProperties = params.transferCustomProperties
        }

        if(params.transferPrivateProperties != null)
        {
            transferWorkflow.transferPrivateProperties = params.transferPrivateProperties
        }

        result.surveyConfig.transferWorkflow = transferWorkflow ?  (new JSON(transferWorkflow)).toString() : null

        SurveyConfig.withTransaction { TransactionStatus ts ->
            if (!result.surveyConfig.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }


        redirect(url: request.getHeader('referer'))

    }

    /**
     * Call to evaluate the given survey's results; the results may be displayed as HTML or
     * exported as (configurable) Excel worksheet
     * @return the survey evaluation view, either as HTML or as (configurable) Excel worksheet
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyEvaluation() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        params.tab = params.tab ?: 'participantsViewAllFinish'

        /*if ( params.exportXLSX ) {
            SXSSFWorkbook wb
            if ( params.surveyCostItems ) {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems([result.surveyConfig], result.institution)
            }else {
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.label")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys([result.surveyConfig], result.institution)
            }
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else */
        String message = g.message(code: 'renewalexport.renewals')
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String datetoday = sdf.format(new Date())

        String filename
        Map<String, Object> selectedFields = [:]
        Set<String> contactSwitch = []
        if (params.filename) {
            filename =params.filename
        }
        else {
            filename = message + "_" + result.surveyConfig.getSurveyName() + "_${datetoday}"
        }
        if(params.fileformat) {
            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            contactSwitch.addAll(params.list("contactSwitch"))
        }
        if (params.fileformat == 'xlsx') {
            try {
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSurveyEvaluation(result, selectedFields, contactSwitch, ExportClickMeService.FORMAT.XLS)
                // Write the output to a file

                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            catch (Exception e) {
                log.error("Problem", e);
                response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                return
            }
        }
        else if(params.fileformat == 'csv') {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
            response.contentType = "text/csv"
            ServletOutputStream out = response.outputStream
            out.withWriter { writer ->
                writer.write((String) exportClickMeService.exportSurveyEvaluation(result, selectedFields, contactSwitch, ExportClickMeService.FORMAT.CSV))
            }
            out.close()
        }
        else {
            if (params.tab == 'participantsViewAllNotFinish') {
                params.participantsNotFinish = true
            } else if (params.tab == 'participantsViewAllFinish') {
                params.participantsFinish = true
            }
            result.participantsNotFinishTotal = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfig(result.surveyConfig).size()
            result.participantsFinishTotal = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig).size()
            result.participantsTotal = result.surveyConfig.orgs.size()

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)


            result.propList = result.surveyConfig.surveyProperties.surveyProperty

            if (result.surveyInfo.type.id in [RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_RENEWAL.id]) {
                result.propertiesChanged = [:]
                result.propertiesChangedByParticipant = []
                result.propList.sort { it.getI10n('name') }.each { PropertyDefinition propertyDefinition ->

                    PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(propertyDefinition.name, PropertyDefinition.SUB_PROP)
                    if (subPropDef) {
                        result.participants.each { SurveyOrg surveyOrg ->
                            Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                    [parentSub  : result.surveyConfig.subscription,
                                     participant: surveyOrg.org
                                    ])[0]
                            SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, result.contextOrg)
                            SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, result.contextOrg)

                            if (surveyResult && subscriptionProperty) {
                                String surveyValue = surveyResult.getValue()
                                String subValue = subscriptionProperty.getValue()
                                if (surveyValue != subValue) {
                                    Map changedMap = [:]
                                    changedMap.participant = surveyOrg.org

                                    result.propertiesChanged."${propertyDefinition.id}" = result.propertiesChanged."${propertyDefinition.id}" ?: []
                                    result.propertiesChanged."${propertyDefinition.id}" << changedMap

                                    result.propertiesChangedByParticipant << surveyOrg.org
                                }
                            }

                        }

                    }
                }
            }

            result.participants = result.participants.sort { it.org.sortname }

            result
        }
    }

    /**
     * Call to open the participant transfer view
     * @return the participant list with their selections
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> surveyTransfer() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

         Map<String,Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

        result.participants = SurveyResult.executeQuery(fsq.query, fsq.queryParams, params)

        result.propList    = result.surveyConfig.surveyProperties.surveyProperty

        if(result.surveyInfo.type.id in [RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_RENEWAL.id] ) {
            result.propertiesChanged = [:]
            result.propertiesChangedByParticipant = []
            result.propList.sort { it.getI10n('name') }.each { PropertyDefinition propertyDefinition ->

                PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(propertyDefinition.name, PropertyDefinition.SUB_PROP)
                if (subPropDef) {
                    result.surveyConfig.orgs.each { SurveyOrg surveyOrg ->
                        Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                [parentSub  : result.surveyConfig.subscription,
                                 participant: surveyOrg.org
                                ])[0]
                        SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, result.contextOrg)
                        SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, result.contextOrg)

                        if (surveyResult && subscriptionProperty) {
                            String surveyValue = surveyResult.getValue()
                            String subValue = subscriptionProperty.getValue()
                            if (surveyValue != subValue) {
                                Map changedMap = [:]
                                changedMap.participant = surveyOrg.org

                                result.propertiesChanged."${propertyDefinition.id}" = result.propertiesChanged."${propertyDefinition.id}" ?: []
                                result.propertiesChanged."${propertyDefinition.id}" << changedMap

                                result.propertiesChangedByParticipant << surveyOrg.org
                            }
                        }

                    }

                }
            }
        }

        result

    }

    /**
     * Call to transfer the survey participants onto the next year's subscription
     * @return the subscription comparison view for the given subscriptions (the predecessor and the successor instances)
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processTransferParticipants() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if(!params.targetSubscriptionId) {
            flash.error = g.message(code: "surveyTransfer.error.noSelectedSub")
            redirect(url: request.getHeader('referer'))
            return
        }
        Subscription.withTransaction { TransactionStatus ts ->
            result.parentSubscription = result.surveyConfig.subscription
            result.targetParentSub = Subscription.get(params.targetSubscriptionId)
            result.targetParentSubChilds = result.targetParentSub ? subscriptionService.getValidSubChilds(result.targetParentSub) : null
            result.targetParentSubParticipantsList = []
            result.targetParentSubChilds.each { sub ->
                Org org = sub.getSubscriber()
                result.targetParentSubParticipantsList << org

            }
            result.newSubs = []
            Integer countNewSubs = 0
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            Date startDate = params.startDate ? sdf.parse(params.startDate) : null
            Date endDate = params.endDate ? sdf.parse(params.endDate) : null
            params.list('selectedOrgs').each { orgId ->
                Org org = Org.get(orgId)
                if(org && result.targetParentSub && !(result.targetParentSubParticipantsList && org.id in result.targetParentSubParticipantsList.id)){
                    log.debug("Generating seperate slaved instances for members")
                    Subscription memberSub = new Subscription(
                            type: result.targetParentSub.type ?: null,
                            kind: result.targetParentSub.kind ?: null,
                            status: result.targetParentSub.status ?: null,
                            name: result.targetParentSub.name,
                            startDate: startDate,
                            endDate: endDate,
                            administrative: result.targetParentSub._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                            manualRenewalDate: result.targetParentSub.manualRenewalDate,
                            identifier: UUID.randomUUID().toString(),
                            instanceOf: result.targetParentSub,
                            isSlaved: true,
                            resource: result.targetParentSub.resource ?: null,
                            form: result.targetParentSub.form ?: null,
                            isPublicForApi: result.targetParentSub.isPublicForApi,
                            hasPerpetualAccess: result.targetParentSub.hasPerpetualAccess,
                            isMultiYear: false
                    )

                    if (!memberSub.save()) {
                        memberSub.errors.each { e ->
                            log.debug("Problem creating new sub: ${e}")
                        }
                    }

                    if (memberSub) {

                        new OrgRole(org: org, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS).save()
                        new OrgRole(org: result.institution, sub: memberSub, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA).save()


                        SubscriptionProperty.findAllByOwner(result.targetParentSub).each { scp ->
                            AuditConfig ac = AuditConfig.getConfig(scp)

                            if (ac) {
                                // multi occurrence props; add one additional with backref
                                if (scp.type.multipleOccurrence) {
                                    def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type, scp.tenant)
                                    additionalProp = scp.copyInto(additionalProp)
                                    additionalProp.instanceOf = scp
                                    additionalProp.save()
                                } else {
                                    // no match found, creating new prop with backref
                                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type, scp.tenant)
                                    newProp = scp.copyInto(newProp)
                                    newProp.instanceOf = scp
                                    newProp.save()
                                }
                            }
                        }
                    }

                    result.newSubs << memberSub
                }
                countNewSubs++
            }
            result.countNewSubs = countNewSubs
            if(result.newSubs?.size() > 0) {
                result.targetParentSub.syncAllShares(result.newSubs)
            }
            flash.message = message(code: 'surveyInfo.transfer.info', args: [countNewSubs, result.newSubs?.size() ?: 0]) as String
        }
        redirect(action: 'compareMembersOfTwoSubs', id: params.id, params: [surveyConfigID: result.surveyConfig.id, targetSubscriptionId: result.targetParentSub?.id])
    }

    /**
     * Call to list the members; either those who completed the survey or those who did not
     * @return a list of the participants in the called tab view
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> openParticipantsAgain() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        params.tab = params.tab ?: 'participantsViewAllFinish'
        if(params.tab == 'participantsViewAllNotFinish'){
            params.participantsNotFinish = true
        }
        if(params.tab == 'participantsViewAllFinish'){
            params.participantsFinish = true
        }

        result.participantsNotFinishTotal = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNull(result.surveyConfig).size()
        result.participantsFinishTotal = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig).size()

        Map<String,Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

        result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)

        result.propList    = result.surveyConfig.surveyProperties.surveyProperty

        result

    }

    /**
     * Opens the survey for the given participants and sends eventual reminders
     * @return the participation view with the counts of execution done
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processOpenParticipantsAgain() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.editable = (result.surveyInfo && result.surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

        Integer countReminderMails = 0
        Integer countOpenParticipants = 0
        boolean reminderMail = (params.openOption == 'ReminderMail')  ?: false
        boolean openAndSendMail = (params.openOption == 'OpenWithMail')  ?: false
        boolean open = (params.openOption == 'OpenWithoutMail') ?: false

        if (params.selectedOrgs && result.editable) {

            params.list('selectedOrgs').each { soId ->

                Org org = Org.get(Long.parseLong(soId))

                if(openAndSendMail || open) {
                    SurveyOrg.withTransaction { TransactionStatus ts ->
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(org, result.surveyConfig)

                        surveyOrg.finishDate = null
                        surveyOrg.save()
                        countOpenParticipants++
                    }
                }

                if(openAndSendMail) {
                    surveyService.emailsToSurveyUsersOfOrg(result.surveyInfo, org, false)
                }
                if(reminderMail) {
                    SurveyOrg.withTransaction { TransactionStatus ts ->
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(org, result.surveyConfig)

                        surveyOrg.reminderMailDate = new Date()
                        surveyOrg.save()
                    }

                    surveyService.emailsToSurveyUsersOfOrg(result.surveyInfo, org, true)
                    countReminderMails++
                }

            }
        }

        if(countReminderMails > 0){
            flash.message =  g.message(code: 'openParticipantsAgain.sendReminderMail.count', args: [countReminderMails])
        }

        if(countOpenParticipants > 0 && !openAndSendMail){
            flash.message =  g.message(code: 'openParticipantsAgain.open.count', args: [countOpenParticipants])
        }

        if(countOpenParticipants > 0 && openAndSendMail){
            flash.message =  g.message(code: 'openParticipantsAgain.openWithMail.count', args: [countOpenParticipants])
        }

        redirect(action: 'openParticipantsAgain', id: result.surveyInfo.id, params:[tab: params.tab, surveyConfigID: result.surveyConfig.id])

    }

    /**
     * Reopens the given survey for the given participant
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> openSurveyAgainForParticipant() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyOrg.withTransaction { TransactionStatus ts ->
            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

            surveyOrg.finishDate = null
            surveyOrg.save()
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the given survey as completed for the given participant
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> finishSurveyForParticipant() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyOrg.withTransaction { TransactionStatus ts ->
            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

            surveyOrg.finishDate = new Date()
            surveyOrg.save()
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Evaluates the general selection and the costs of the participant
     * @return the participant evaluation view
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> evaluationParticipant() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        /*
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        */

        result.participant = Org.get(params.participant)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : result.surveyInfo.surveyConfigs[0]

        result.surveyResults = []

        result.surveyConfig.getSortedSurveyProperties().each{ PropertyDefinition propertyDefinition ->
            result.surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(result.participant, result.surveyConfig, propertyDefinition)
        }


        result.ownerId = result.surveyInfo.owner.id

        if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.participant)
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.costItemSums = [:]
            if(result.subscription) {
                result.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

            //costs dataToDisplay
            result.dataToDisplay = ['subscr']
            result.offsets = [subscrOffset:0]
            result.sortConfig = [subscrSort:'sub.name',subscrOrder:'asc']
            //result.dataToDisplay = ['consAtSubscr']
            //result.offsets = [consOffset:0]
            //result.sortConfig = [consSort:'ci.costTitle',consOrder:'asc']

            result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
            //cost items
            //params.forExport = true
            LinkedHashMap costItems = result.subscription ? financeService.getCostItemsForSubscription(params, result) : null
            result.costItemSums = [:]
            /*if (costItems?.cons) {
                result.costItemSums.consCosts = costItems.cons.sums
            }*/
            if (costItems?.subscr) {
                result.costItemSums.subscrCosts = costItems.subscr.costItems
            }
            result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)

                if (result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                    result.previousSubscription = result.subscription._getCalculatedPreviousForSurvey()

                    /*result.previousIesListPriceSum = 0
                   if(result.previousSubscription){
                       result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                               'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                       [sub: result.previousSubscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                   }*/

                    result.iesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                            'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                            [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0


                    /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                             'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                             [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */

                    result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)

                    if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                        result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
                    } else {
                        result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
                    }

                    result.subscriber = result.participant

                }

        }
            if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION]) {
                if (!result.subscription) {
                    result.successorSubscriptionParent = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                    result.successorSubscription = result.successorSubscriptionParent ? result.successorSubscriptionParent.getDerivedSubscriptionBySubscribers(result.participant) : null
                } else {
                    result.successorSubscription = result.subscription._getCalculatedSuccessorForSurvey()
                }
                if (result.successorSubscription) {
                    List objects = []
                    if(result.subscription){
                        objects << result.subscription
                    }
                    objects << result.successorSubscription
                    result = result + compareService.compareProperties(objects)
                }
            }
        }

        result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)
        result.institution = result.participant

        result
    }

    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> generatePdfForParticipant() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        result.participant = Org.get(params.participant)

        result.surveyResults = []

        result.surveyConfig.getSortedSurveyProperties().each{ PropertyDefinition propertyDefinition ->
            result.surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(result.participant, result.surveyConfig, propertyDefinition)
        }

        result.ownerId = result.surveyInfo.owner.id

        if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.participant)
            result.visibleOrgRelations = []
            result.costItemSums = [:]
            if(result.subscription) {
                result.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

                result.dataToDisplay = ['subscr']
                result.offsets = [subscrOffset:0]
                result.sortConfig = [subscrSort:'sub.name',subscrOrder:'asc']

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()

                LinkedHashMap costItems = result.subscription ? financeService.getCostItemsForSubscription(params, result) : null
                result.costItemSums = [:]

                if (costItems?.subscr) {
                    result.costItemSums.subscrCosts = costItems.subscr.costItems
                }
                result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)

                if (result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                    result.previousSubscription = result.subscription._getCalculatedPreviousForSurvey()

                    /*result.previousIesListPriceSum = 0
                   if(result.previousSubscription){
                       result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                               'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                       [sub: result.previousSubscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                   }*/

                    result.iesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                            'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                            [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0


                    /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                             'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                             [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */

                    result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)
                    if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                        result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
                    } else {
                        result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
                    }
                    result.subscriber = result.participant

                }
            }

            if(result.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION]) {
                result.successorSubscription = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                Collection<AbstractPropertyWithCalculatedLastUpdated> props
                props = result.surveyConfig.subscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                if(result.successorSubscription){
                    props += result.successorSubscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}
                }
                result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
            }

            result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
        }

        result.institution = result.participant
        result.ownerView = (result.contextOrg.id == result.surveyInfo.owner.id)

        String pageSize = 'A4'
        String orientation = 'Portrait'

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()
        String filename

        if (params.filename) {
            filename = sdf.format(new Date()) + '_' + params.filename
        }
        else {
            filename = sdf.format(new Date()) + '_reporting'
        }

        def pdf = wkhtmltoxService.makePdf(
                view: '/survey/export/pdf/participantResult',
                model: result,
                // header: '',
                // footer: '',
                pageSize: pageSize,
                orientation: orientation,
                marginLeft: 10,
                marginTop: 15,
                marginBottom: 15,
                marginRight: 10
        )

        response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
        response.setContentType('application/pdf')
        response.outputStream.withStream { it << pdf }
        return
    }

    /**
     * Call to list all possible survey properties (i.e. the questions which may be asked in a survey)
     * @return a list of properties for the given consortium
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> allSurveyProperties() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.properties = surveyService.getSurveyProperties(result.institution)

        result.language = LocaleUtils.getCurrentLang()

        result

    }

    /**
     * Adds the given survey property to the survey configuration, i.e. inserts a new question for the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> addSurveyPropToConfig() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.surveyInfo && result.editable) {

            if (params.selectedProperty) {
                PropertyDefinition property = PropertyDefinition.get(Long.parseLong(params.selectedProperty))
                //Config is Sub
                if (params.surveyConfigID) {
                    SurveyConfig surveyConfig = SurveyConfig.get(Long.parseLong(params.surveyConfigID))

                    if (surveyService.addSurPropToSurvey(surveyConfig, property)) {

                        //flash.message = g.message(code: "surveyConfigs.property.add.successfully")

                    } else {
                        flash.error = g.message(code: "surveyConfigs.property.exists")
                    }
                }
            }
        }
        redirect(url: request.getHeader('referer'))

    }

    /**
     * Removes the given survey property from the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteSurveyPropFromConfig() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyConfigProperties surveyConfigProp = SurveyConfigProperties.get(params.id)

        SurveyInfo surveyInfo = surveyConfigProp.surveyConfig.surveyInfo

        result.editable = (surveyInfo && surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.editable) {
            SurveyConfigProperties.withTransaction { TransactionStatus ts ->
                try {
                    surveyConfigProp.delete()
                    //flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyProperty.label"), ''])
                }
                catch (DataIntegrityViolationException e) {
                    flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyProperty.label"), ''])
                }
            }

        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Creates a new survey property
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> createSurveyProperty() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        PropertyDefinition surveyProperty = PropertyDefinition.findWhere(
                name: params.pd_name,
                type: params.pd_type,
                tenant: result.institution,
                descr: PropertyDefinition.SVY_PROP
        )

        if ((!surveyProperty) && params.pd_name && params.pd_type) {
            RefdataCategory rdc
            if (params.refdatacategory) {
                rdc = RefdataCategory.findById(Long.parseLong(params.refdatacategory))
            }

            Map<String, Object> map = [
                    token       : params.pd_name,
                    category    : PropertyDefinition.SVY_PROP,
                    type        : params.pd_type,
                    rdc         : rdc ? rdc.getDesc() : null,
                    tenant      : result.institution.globalUID,
                    i10n        : [
                            name_de: params.pd_name,
                            name_en: params.pd_name,
                            expl_de: params.pd_expl,
                            expl_en: params.pd_expl
                    ]
            ]

            if (PropertyDefinition.construct(map)) {
                //flash.message = message(code: 'surveyProperty.create.successfully', args: [surveyProperty.name])
            } else {
                flash.error = message(code: 'surveyProperty.create.fail') as String
            }
        } else if (surveyProperty) {
            flash.error = message(code: 'surveyProperty.create.exist') as String
        } else {
            flash.error = message(code: 'surveyProperty.create.fail') as String
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Deletes the given survey property
     * @return redirects to the survey property listing
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteSurveyProperty() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        PropertyDefinition surveyProperty = PropertyDefinition.findByIdAndTenant(params.deleteId, result.institution)

        PropertyDefinition.withTransaction { TransactionStatus ts ->
            if (surveyProperty.countUsages()==0 && surveyProperty.tenant.id == result.institution.id) {
                surveyProperty.delete()
                //flash.message = message(code: 'default.deleted.message', args:[message(code: 'surveyProperty.label'), surveyProperty.getI10n('name')])
            }
        }

        redirect(action: 'allSurveyProperties', id: params.id)

    }

    /**
     * Adds the given institutions to the given survey as new participants
     * @return the updated survey participants list
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> addSurveyParticipants() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        SurveyConfig surveyConfig = SurveyConfig.get(params.surveyConfigID)
        SurveyInfo surveyInfo = surveyConfig.surveyInfo

        result.editable = (surveyInfo && surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

        if (params.selectedOrgs && result.editable) {
            SurveyOrg.withTransaction { TransactionStatus ts ->
                params.list('selectedOrgs').each { soId ->
                    Org org = Org.get(Long.parseLong(soId))
                    boolean existsMultiYearTerm = false
                    Subscription sub = surveyConfig.subscription
                    if (sub && !surveyConfig.pickAndChoose && surveyConfig.subSurveyUseForTransfer) {
                        Subscription subChild = sub.getDerivedSubscriptionBySubscribers(org)

                        if (subChild && subChild.isCurrentMultiYearSubscriptionNew()) {
                            existsMultiYearTerm = true
                        }

                    }

                    if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org)) && !existsMultiYearTerm) {
                        SurveyOrg surveyOrg = new SurveyOrg(
                                surveyConfig: surveyConfig,
                                org: org
                        )

                        if (!surveyOrg.save()) {
                            log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}");
                        } else {
                            if(surveyInfo.status in [RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]){
                                surveyConfig.surveyProperties.each { SurveyConfigProperties property ->

                                    SurveyResult surveyResult = new SurveyResult(
                                            owner: result.institution,
                                            participant: org ?: null,
                                            startDate: surveyInfo.startDate,
                                            endDate: surveyInfo.endDate ?: null,
                                            type: property.surveyProperty,
                                            surveyConfig: surveyConfig
                                    )

                                    if (surveyResult.save()) {
                                        log.debug( surveyResult.toString() )
                                    } else {
                                        log.error("Not create surveyResult: "+ surveyResult)
                                    }
                                }

                                if(surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED){
                                    surveyService.emailsToSurveyUsersOfOrg(surveyInfo, org, false)
                                }
                            }
                        }
                    }
                }
                surveyConfig.save()
            }
        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID]

    }

    /**
     * Opens the given survey to the public and sends reminders to the participants to call to fill the survey out
     * @return redirects to the survey details page
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processOpenSurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        boolean openFailByTitleSelection = false

        Date startDate = params.startNow ? new Date() : result.surveyInfo.startDate

        if (result.editable) {

            result.surveyConfigs = result.surveyInfo.surveyConfigs.sort { it.configOrder }
            SurveyConfig.withTransaction { TransactionStatus ts ->
                result.surveyConfigs.each { config ->
                    config.orgs.org.each { org ->
                        if(result.surveyInfo.type == RDStore.SURVEY_TYPE_TITLE_SELECTION){
                            Subscription subscription = config.subscription.getDerivedSubscriptionBySubscribers(org)

                            if(subscription.packages.size() == 0){
                                openFailByTitleSelection = true
                            }
                        }

                        if(!openFailByTitleSelection) {
                            config.surveyProperties.each { property ->
                                if (!SurveyResult.findWhere(owner: result.institution, participant: org, type: property.surveyProperty, surveyConfig: config)) {
                                    SurveyResult surveyResult = new SurveyResult(
                                            owner: result.institution,
                                            participant: org,
                                            startDate: startDate,
                                            endDate: result.surveyInfo.endDate ?: null,
                                            type: property.surveyProperty,
                                            surveyConfig: config
                                    )
                                    if (surveyResult.save()) {
                                        log.debug(surveyResult.toString())
                                    } else {
                                        log.error("Not create surveyResult: " + surveyResult)
                                    }
                                }
                            }
                        }
                    }
                }

                if(!openFailByTitleSelection) {
                    result.surveyInfo.status = params.startNow ? RDStore.SURVEY_SURVEY_STARTED : RDStore.SURVEY_READY
                    result.surveyInfo.save()
                    flash.message = params.startNow ? g.message(code: "openSurveyNow.successfully") : g.message(code: "openSurvey.successfully")
                }else {
                    flash.error = g.message(code: "openSurvey.openFailByTitleSelection.noPackagesYetAdded")
                }
            }

            if(!openFailByTitleSelection) {
                executorService.execute({
                    Thread.currentThread().setName('EmailsToSurveyUsers' + result.surveyInfo.id)
                    surveyService.emailsToSurveyUsers([result.surveyInfo.id])
                })
            }

        }

        redirect action: 'show', id: params.id
    }

    /**
     * Marks the given survey as in evaluation and closes further survey completion
     * @return either the evaluation view for the renewal or redirects to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processEndSurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if (result.editable) {
            SurveyInfo.withTransaction { TransactionStatus ts ->
                result.surveyInfo.status = RDStore.SURVEY_IN_EVALUATION
                result.surveyInfo.save()
                flash.message = g.message(code: "endSurvey.successfully")
            }
        }

        if(result.surveyConfig && result.surveyConfig.subSurveyUseForTransfer) {
            redirect action: 'renewalEvaluation', params: [surveyConfigID: result.surveyConfig.id, id: result.surveyInfo.id]
            return
        }else{
            redirect(uri: request.getHeader('referer'))
            return
        }
    }

    /**
     * Marks the survey as in processing and closes survey completion
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processBackInProcessingSurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if (result.editable) {
            SurveyInfo.withTransaction { TransactionStatus ts ->
                result.surveyInfo.status = RDStore.SURVEY_IN_PROCESSING
                result.surveyInfo.save()
            }
        }

        redirect(uri: request.getHeader('referer'))
    }

    /**
     * Opens the survey again after a break
     * @return the survey details view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> openSurveyAgain() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if(result.surveyInfo && result.surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id, RDStore.SURVEY_SURVEY_COMPLETED.id ]){
            SurveyInfo.withTransaction { TransactionStatus ts ->
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                Date endDate = params.newEndDate ? sdf.parse(params.newEndDate) : null

                if(result.surveyInfo.startDate != null && endDate != null) {
                    if(result.surveyInfo.startDate > endDate) {
                        flash.error = g.message(code: "openSurveyAgain.fail.startDateAndEndDate")
                        redirect(uri: request.getHeader('referer'))
                        return
                    }
                }

                result.surveyInfo.status = RDStore.SURVEY_SURVEY_STARTED
                result.surveyInfo.endDate = endDate
                result.surveyInfo.save()
            }
        }

        redirect action: 'show', id: params.id

    }

    /**
     * Removes the given survey participants from the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteSurveyParticipants() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (params.selectedOrgs && result.editable) {
            SurveyOrg.withTransaction { TransactionStatus ts ->
                params.list('selectedOrgs').each { soId ->
                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, Org.get(Long.parseLong(soId)))

                    CostItem.findAllBySurveyOrg(surveyOrg).each {
                        it.delete()
                    }

                    SurveyResult.findAllBySurveyConfigAndParticipant(result.surveyConfig, surveyOrg.org).each {
                        it.delete()
                    }

                    if (surveyOrg.delete()) {
                        //flash.message = g.message(code: "surveyParticipants.delete.successfully")
                    }
                }
            }
        }

        redirect(uri: request.getHeader('referer'))

    }

    /**
     * Call to delete the given documents from the survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteDocuments() {
        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect(uri: request.getHeader('referer'))
    }

    /**
     * Deletes the entire survey with attached objects
     * @return the survey list in case of success, a redirect to the referer otherwise
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteSurveyInfo() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.editable = (result.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])

        if (result.editable) {

            try {

                SurveyInfo surveyInfo = SurveyInfo.get(result.surveyInfo.id)
                SurveyInfo.withTransaction {

                    DocContext.executeUpdate("delete from DocContext dc where dc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    CostItem.executeUpdate("delete from CostItem ct where ct.surveyOrg.id in (:surveyOrgIDs)", [surveyOrgIDs: SurveyOrg.findAllBySurveyConfigInList(SurveyConfig.findAllBySurveyInfo(surveyInfo)).id])

                    SurveyOrg.executeUpdate("delete from SurveyOrg so where so.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyResult.executeUpdate("delete from SurveyResult sr where sr.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    Task.executeUpdate("delete from Task ta where ta.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyConfigProperties.executeUpdate("delete from SurveyConfigProperties scp where scp.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyConfig.executeUpdate("delete from SurveyConfig sc where sc.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    surveyInfo.delete()
                }

                flash.message = message(code: 'surveyInfo.delete.successfully') as String

                redirect action: 'currentSurveysConsortia'
                return
            }
            catch (DataIntegrityViolationException e) {
                flash.error = message(code: 'surveyInfo.delete.fail') as String

                redirect(uri: request.getHeader('referer'))
                return
            }
        }


    }

    /**
     * Call to edit the given survey cost item
     * @return the cost item editing modal
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
     Map<String,Object> editSurveyCostItem() {
        println('editSurveyCostItem')
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        result.putAll(financeControllerService.getEditVars(result.institution))
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        result.costItem = CostItem.findById(params.costItem)


        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }

        result.orgConfigurations = orgConfigurations as JSON
        //result.selectedCostItemElement = params.selectedCostItemElement ?: RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id.toString()

        result.participant = Org.get(params.participant)
        result.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, result.participant)


        result.mode = result.costItem ? "edit" : ""
        result.taxKey = result.costItem ? result.costItem.taxKey : null
        result.idSuffix = "edit_${result.costItem ? result.costItem.id : result.participant.id}"
        render(template: "/survey/costItemModal", model: result)
    }

    /**
     * Call to add a new survey cost item to every participant
     * @return the new cost item modal
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
     Object addForAllSurveyCostItem() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.putAll(financeControllerService.getEditVars(result.institution))

        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }

        result.orgConfigurations = orgConfigurations as JSON
        //result.selectedCostItemElement = params.selectedCostItemElement ?: RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id.toString()

        result.setting = 'bulkForAll'

        result.surveyOrgList = []

        if (params.get('orgsIDs')) {
            List idList = (params.get('orgsIDs')?.split(',').collect { Long.valueOf(it.trim()) }).toList()
            List<Org> orgList = Org.findAllByIdInList(idList)
            result.surveyOrgList = orgList.isEmpty() ? [] : SurveyOrg.findAllByOrgInListAndSurveyConfig(orgList, result.surveyConfig)
        }

        render(template: "/survey/costItemModal", model: result)
    }

    /**
     * Marks the survey as being in evaluation
     * @return redirects to the renewal evaluation view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setInEvaluation() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyInfo.status = RDStore.SURVEY_IN_EVALUATION

        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (!result.surveyInfo.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect action: 'renewalEvaluation', params:[surveyConfigID: result.surveyConfig.id, id: result.surveyInfo.id]

    }

    /**
     * Marks the survey as completed
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setCompleted() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyInfo.status = RDStore.SURVEY_COMPLETED

        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (!result.surveyInfo.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the survey as finished, i.e. evaluation is over, too
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setCompletedSurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SurveyInfo.withTransaction { TransactionStatus ts ->
            result.surveyInfo.status = RDStore.SURVEY_SURVEY_COMPLETED
            if (!result.surveyInfo.save()) {
                flash.error = g.message(code: 'survey.change.fail')
            }
        }


        redirect(url: request.getHeader('referer'))

    }

    /**
     * Sets the given comment for the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setSurveyConfigComment() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.surveyConfig.comment = params.comment

        result.surveyConfig.commentForNewParticipants = params.commentForNewParticipants

        SurveyConfig.withTransaction {
            if (!result.surveyConfig.save()) {
                flash.error = g.message(code: 'default.save.error.general.message')
            }
        }

        redirect(action: 'show', params: [id: params.id, surveyConfigID: result.surveyConfig.id, commentTab: params.commentTab])

    }

    /**
     * Call to load the evaluation of the renewal process. The evaluation data may be exported as (configurable)
     * Excel worksheet
     * @return the evaluation view for the given renewal survey
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> renewalEvaluation() {
        Map<String,Object> ctrlResult = surveyControllerService.renewalEvaluation(params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
                return
        }
        else {
            String message = g.message(code: 'renewalexport.renewals')
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            String datetoday = sdf.format(new Date())

            String filename
            Map<String, Object> selectedFields = [:]
            if(params.fileformat) {
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
                if (params.filename) {
                    filename =params.filename
                }
                else {
                    filename = message + "_" + ctrlResult.result.surveyConfig.getSurveyName() + "_${datetoday}"
                }
            }


            if (params.fileformat == 'xlsx') {
                try {
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportRenewalResult(ctrlResult.result, selectedFields, ExportClickMeService.FORMAT.XLS)
                    // Write the output to a file

                    response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                }
                catch (Exception e) {
                    log.error("Problem", e);
                    response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    return
                }
            }
            else if(params.fileformat == 'csv') {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportRenewalResult(ctrlResult.result, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
            }
            else
                ctrlResult.result
        }
    }

    /**
     * Call to show the differences between the respective institution's choices and the underlying subscription data
     * @return a modal to show the differences between this and next year ring's subscription parameters (= the selected
     * parameters by each member)
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> showPropertiesChanged() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
        String filename = "${datetoday}_" + g.message(code: "renewalEvaluation.propertiesChanged")

        if(params.tab == 'participantsViewAllNotFinish'){
            result.participants = SurveyOrg.executeQuery('select so from SurveyOrg so join so.org o where so.finishDate is null and so.surveyConfig = :cfg order by o.sortname', [cfg: result.surveyConfig])
            filename = filename +'_'+g.message(code: "surveyEvaluation.participantsViewAllNotFinish")
        }else if(params.tab == 'participantsViewAllFinish'){
            result.participants = SurveyOrg.executeQuery('select so from SurveyOrg so join so.org o where so.finishDate is not null and so.surveyConfig = :cfg order by o.sortname', [cfg: result.surveyConfig])
            filename = filename +'_'+g.message(code: "surveyEvaluation.participantsViewAllFinish")
        }else{
            result.participants = result.surveyConfig.orgs
            filename = filename +'_'+g.message(code: "surveyEvaluation.participantsView")
        }

        if(params.exportXLSX) {
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportPropertiesChanged(result.surveyConfig, result.participants, result.contextOrg)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else {

            result.changedProperties = []
            result.propertyDefinition = PropertyDefinition.findById(params.propertyDefinitionId)
            PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(result.propertyDefinition.name, PropertyDefinition.SUB_PROP)
            if (subPropDef) {
                result.participants.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
                    Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : result.surveyConfig.subscription,
                             participant: surveyOrg.org
                            ])[0]
                    SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, result.propertyDefinition, result.surveyConfig, result.contextOrg)
                    SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, result.contextOrg)

                    if (surveyResult && subscriptionProperty) {
                        String surveyValue = surveyResult.getValue()
                        String subValue = subscriptionProperty.getValue()
                        if (surveyValue != subValue) {
                            Map changedMap = [:]
                            changedMap.surveyResult = surveyResult
                            changedMap.subscriptionProperty = subscriptionProperty
                            changedMap.surveyValue = surveyValue
                            changedMap.subValue = subValue
                            changedMap.participant = surveyOrg.org
                            result.changedProperties << changedMap
                        }
                    }

                }

            }
        }

        render template: "/survey/modal_PropertiesChanged", model: result

    }

    /**
     * Call to copy the given survey
     * @return the view with the base parameters for the survey copy
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copySurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if(result.surveyInfo.type.id == RDStore.SURVEY_TYPE_INTEREST.id){
            result.workFlow = '2'
        }else{
            if(params.targetSubs){
                result.workFlow = '2'
            }else{
                result.workFlow = '1'
            }
        }

        if(result.workFlow == '1') {
            Date date_restriction = null
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

            if (params.validOn == null || params.validOn.trim() == '') {
                result.validOn = ""
            } else {
                result.validOn = params.validOn
                date_restriction = sdf.parse(params.validOn)
            }

            result.editable = accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )

            if (!result.editable) {
                flash.error = g.message(code: "default.notAutorized.message")
                redirect(url: request.getHeader('referer'))
            }

            if (!params.status) {
                if (params.isSiteReloaded != "yes") {
                    params.status = RDStore.SUBSCRIPTION_CURRENT.id
                    result.defaultSet = true
                } else {
                    params.status = 'FETCH_ALL'
                }
            }

            Set orgIds = orgTypeService.getCurrentOrgIdsOfProvidersAndAgencies(contextService.getOrg())

            result.providers = orgIds.isEmpty() ? [] : Org.findAllByIdInList(orgIds, [sort: 'name'])

            List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.getOrg())
            result.filterSet = tmpQ[2]
            List subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )
            //,[max: result.max, offset: result.offset]

            result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.getOrg())

            if (params.sort && params.sort.indexOf("§") >= 0) {
                switch (params.sort) {
                    case "orgRole§provider":
                        subscriptions.sort { x, y ->
                            String a = x.getProviders().size() > 0 ? x.getProviders().first().name : ''
                            String b = y.getProviders().size() > 0 ? y.getProviders().first().name : ''
                            a.compareToIgnoreCase b
                        }
                        if (params.order.equals("desc"))
                            subscriptions.reverse(true)
                        break
                }
            }
            result.num_sub_rows = subscriptions.size()
            result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)
        }

        if(result.surveyConfig.subscription) {
            String sourceLicensesQuery = "select li.sourceLicense from Links li where li.destinationSubscription = :sub and li.linkType = :linkType order by li.sourceLicense.sortableReference asc"
            result.sourceLicenses = License.executeQuery(sourceLicensesQuery, [sub: result.surveyConfig.subscription, linkType: RDStore.LINKTYPE_LICENSE])
        }
        
        result.targetSubs = params.targetSubs ? Subscription.findAllByIdInList(params.list('targetSubs').collect { it -> Long.parseLong(it) }): null

        result

    }

    /**
     * Call to add the subscription members as participants to the survey
     * @return the survey participants list
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> addSubMembersToSurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        surveyService.addSubMembers(result.surveyConfig)

        redirect(action: 'surveyParticipants', params: [id: result.surveyInfo.id, surveyConfigID: result.surveyConfig.id, tab: 'selectedSubParticipants'])

    }

    /**
     * Takes the submitted base parameters and creates a copy of the given survey
     * @return either the survey list view for consortia or the survey details view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processCopySurvey() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SurveyInfo baseSurveyInfo = result.surveyInfo
        SurveyConfig baseSurveyConfig = result.surveyConfig

        if (baseSurveyInfo && baseSurveyConfig) {

            result.targetSubs = params.targetSubs ? Subscription.findAllByIdInList(params.list('targetSubs').collect { it -> Long.parseLong(it) }): null

            List newSurveyIds = []

            if(result.targetSubs){
                SurveyInfo.withTransaction { TransactionStatus ts ->
                    result.targetSubs.each { sub ->
                        SurveyInfo newSurveyInfo = new SurveyInfo(
                                name: sub.name,
                                status: RDStore.SURVEY_IN_PROCESSING,
                                type: (baseSurveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL) ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sub, true) ? RDStore.SURVEY_TYPE_SUBSCRIPTION : baseSurveyInfo.type) : baseSurveyInfo.type,
                                startDate: params.copySurvey.copyDates ? baseSurveyInfo.startDate : null,
                                endDate: params.copySurvey.copyDates ? baseSurveyInfo.endDate : null,
                                comment: params.copySurvey.copyComment ? baseSurveyInfo.comment : null,
                                isMandatory: params.copySurvey.copyMandatory ? baseSurveyInfo.isMandatory : false,
                                owner: contextService.getOrg()
                        ).save()

                        SurveyConfig newSurveyConfig = new SurveyConfig(
                                type: baseSurveyConfig.type,
                                subscription: sub,
                                surveyInfo: newSurveyInfo,
                                comment: params.copySurvey.copySurveyConfigComment ? baseSurveyConfig.comment : null,
                                commentForNewParticipants: params.copySurvey.copySurveyConfigCommentForNewParticipants ? baseSurveyConfig.commentForNewParticipants : null,
                                configOrder: newSurveyInfo.surveyConfigs ? newSurveyInfo.surveyConfigs.size() + 1 : 1
                        ).save()

                        surveyService.copySurveyConfigCharacteristic(baseSurveyConfig, newSurveyConfig, params)

                        newSurveyIds << newSurveyInfo.id

                    }
                }
                redirect controller: 'survey', action: 'currentSurveysConsortia', params: [ids: newSurveyIds]
                return
            }else{
                SurveyInfo.withTransaction { TransactionStatus ts ->
                    SurveyInfo newSurveyInfo = new SurveyInfo(
                            name: params.name,
                            status: RDStore.SURVEY_IN_PROCESSING,
                            type: baseSurveyInfo.type,
                            startDate: params.copySurvey.copyDates ? baseSurveyInfo.startDate : null,
                            endDate: params.copySurvey.copyDates ? baseSurveyInfo.endDate : null,
                            comment: params.copySurvey.copyComment ? baseSurveyInfo.comment : null,
                            isMandatory: params.copySurvey.copyMandatory ? baseSurveyInfo.isMandatory : false,
                            owner: contextService.getOrg()
                    ).save()

                    SurveyConfig newSurveyConfig = new SurveyConfig(
                            type: baseSurveyConfig.type,
                            surveyInfo: newSurveyInfo,
                            comment: params.copySurvey.copySurveyConfigComment ? baseSurveyConfig.comment : null,
                            commentForNewParticipants: params.copySurvey.copySurveyConfigCommentForNewParticipants ? baseSurveyConfig.commentForNewParticipants : null,
                            configOrder: newSurveyInfo.surveyConfigs ? newSurveyInfo.surveyConfigs.size() + 1 : 1
                    ).save()
                    surveyService.copySurveyConfigCharacteristic(baseSurveyConfig, newSurveyConfig, params)
                    redirect controller: 'survey', action: 'show', params: [id: newSurveyInfo.id, surveyConfigID: newSurveyConfig.id]
                    return
                }
            }
        }

    }

    /**
     * Initialises the subscription renewal for the parent subscription after a survey
     * @return the view for the successor subscription base parameter's configuration
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> renewSubscriptionConsortiaWithSurvey() {

        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        result.institution = contextService.getOrg()
        if (!(result || accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO))) {
            response.sendError(401); return
        }

        Subscription subscription = Subscription.get(params.parentSub ?: null)

        SimpleDateFormat sdf = DateUtils.getSDF_ddMMyyyy()

        result.errors = []
        Date newStartDate
        Date newEndDate
        use(TimeCategory) {
            newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
            newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
        }
        params.surveyConfig = params.surveyConfig ?: null
        result.isRenewSub = true
        result.permissionInfo = [sub_startDate    : newStartDate ? sdf.format(newStartDate) : null,
                                 sub_endDate      : newEndDate ? sdf.format(newEndDate) : null,
                                 sub_referenceYear: subscription.referenceYear,
                                 sub_name         : subscription.name,
                                 sub_id           : subscription.id,
                                 sub_status       : RDStore.SUBSCRIPTION_INTENDED.id.toString(),
                                 sub_type         : subscription.type?.id?.toString(),
                                 sub_form         : subscription.form?.id?.toString(),
                                 sub_resource     : subscription.resource?.id?.toString(),
                                 sub_kind         : subscription.kind?.id?.toString(),
                                 sub_isPublicForApi : subscription.isPublicForApi ? RDStore.YN_YES.id.toString() : RDStore.YN_NO.id.toString(),
                                 //sub_hasPerpetualAccess : subscription.hasPerpetualAccess,
                                 sub_hasPerpetualAccess : subscription.hasPerpetualAccess ? RDStore.YN_YES.id.toString() : RDStore.YN_NO.id.toString(),
                                 sub_hasPublishComponent : subscription.hasPublishComponent ? RDStore.YN_YES.id.toString() : RDStore.YN_NO.id.toString()

        ]

        result.subscription = subscription
        result
    }

    /**
     * Takes the submitted input and creates a successor subscription instance. The successor is being automatically
     * linked to the predecessor instance. The element copy workflow is triggered right after
     * @return the subscription element copy starting view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     def processRenewalWithSurvey() {

        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!(result || accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO))) {
            response.sendError(401); return
        }

        Subscription baseSub = Subscription.get(params.parentSub ?: null)

        ArrayList<Links> previousSubscriptions = Links.findAllByDestinationSubscriptionAndLinkType(baseSub, RDStore.LINKTYPE_FOLLOWS)
        if (previousSubscriptions.size() > 0) {
            flash.error = message(code: 'subscription.renewSubExist') as String
        } else {
            Date sub_startDate = params.subscription.start_date ? DateUtils.parseDateGeneric(params.subscription.start_date) : null
            Date sub_endDate = params.subscription.end_date ? DateUtils.parseDateGeneric(params.subscription.end_date) : null
            Year sub_refYear = params.subscription.reference_year ? Year.parse(params.subscription.reference_year) : null
            def sub_status = params.subStatus
            RefdataValue sub_type = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL
            def sub_kind = params.subKind
            def sub_form = params.subForm
            def sub_resource = params.subResource
            def sub_hasPerpetualAccess = params.subHasPerpetualAccess == RDStore.YN_YES.id.toString()
            //def sub_hasPerpetualAccess = params.subHasPerpetualAccess
            def sub_hasPublishComponent = params.subHasPublishComponent == RDStore.YN_YES.id.toString()
            def sub_isPublicForApi = params.subIsPublicForApi == RDStore.YN_YES.id.toString()
            def old_subOID = params.subscription.old_subid
            def new_subname = params.subscription.name
            def manualCancellationDate = null

            use(TimeCategory) {
                manualCancellationDate =  baseSub.manualCancellationDate ? (baseSub.manualCancellationDate + 1.year) : null
            }
            Subscription.withTransaction { TransactionStatus ts ->
                Subscription newSub = new Subscription(
                        name: new_subname,
                        startDate: sub_startDate,
                        endDate: sub_endDate,
                        referenceYear: sub_refYear,
                        manualCancellationDate: manualCancellationDate,
                        identifier: java.util.UUID.randomUUID().toString(),
                        isSlaved: baseSub.isSlaved,
                        type: sub_type,
                        kind: sub_kind,
                        status: sub_status,
                        resource: sub_resource,
                        form: sub_form,
                        hasPerpetualAccess: sub_hasPerpetualAccess,
                        hasPublishComponent: sub_hasPublishComponent,
                        isPublicForApi: sub_isPublicForApi
                )

                if (!newSub.save()) {
                    log.error("Problem saving subscription ${newSub.errors}");
                    return newSub
                } else {

                    log.debug("Save ok")
                    if (params.list('auditList')) {
                        //copy audit
                        params.list('auditList').each { auditField ->
                            //All ReferenceFields were copied!
                            //'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'form', 'resource'
                            //println(auditField)
                            AuditConfig.addConfig(newSub, auditField)
                        }
                    }
                    //Copy References
                    //OrgRole
                    baseSub.orgRelations.each { OrgRole or ->

                        if ((or.org.id == result.institution.id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = newSub
                            newOrgRole.save()
                        }
                    }
                    //link to previous subscription
                    Links prevLink = Links.construct([source: newSub, destination: baseSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                    if (!prevLink) {
                        log.error("Problem linking to previous subscription: ${prevLink.errors}")
                    }
                    result.newSub = newSub

                    if (params.targetObjectId == "null") params.remove("targetObjectId")
                    result.isRenewSub = true

                    redirect controller: 'subscription', action: 'copyElementsIntoSubscription', params: [sourceObjectId: genericOIDService.getOID(Subscription.get(old_subOID)), targetObjectId: genericOIDService.getOID(newSub), isRenewSub: true, fromSurvey: true]
                    return

                }
            }
        }
    }

    /**
     * Exports the survey costs in an Excel worksheet
     * @return an Excel worksheet containing the survey cost data
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_USER'], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_USER')
    })
     def exportSurCostItems() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        //result.putAll(financeControllerService.setEditVars(result.institution))

        /*   def surveyInfo = SurveyInfo.findByIdAndOwner(params.id, result.institution) ?: null

           def surveyConfig = SurveyConfig.findByIdAndSurveyInfo(params.surveyConfigID, surveyInfo)*/

        if (params.exportXLSX) {
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.exportSurveyCostItems")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems([result.surveyConfig], result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        } else {
            redirect(uri: request.getHeader('referer'))
        }

    }

    /**
     * Call to copy the mail adresses of all participants
     * @return the modal containing the participant's mail addresses
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_USER'], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_USER')
    })
     Map<String,Object> copyEmailaddresses() {
        Map<String, Object> result = [:]
        result.modalID = params.targetId
        result.orgList = []

        if (params.get('orgListIDs')) {
            List idList = (params.get('orgListIDs').split(',').collect { Long.valueOf(it.trim()) }).toList()
            result.orgList = idList.isEmpty() ? [] : Org.findAllByIdInList(idList)
        }

        render(template: "/templates/copyEmailaddresses", model: result)
    }

    /**
     * Takes the submitted input and creates cost items based on the given parameters for every selected survey participant
     * @return a redirect to the referer
     */
    @DebugInfo(hasCtxAffiliation_or_ROLEADMIN = ['INST_EDITOR'], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.contextService.getUser()?.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')
    })
     Map<String,Object> newSurveyCostItem() {
        SimpleDateFormat dateFormat = DateUtils.getLocalizedSDF_noTime()

        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        def newCostItem = null
        result.putAll(financeControllerService.getEditVars(result.institution))

        try {
            log.debug("SurveyController::newCostItem() ${params}");


            User user = contextService.getUser()
            result.error = [] as List

            if (!userService.checkAffiliationAndCtxOrg(user, result.institution, 'INST_EDITOR')) {
                result.error = message(code: 'financials.permission.unauthorised', args: [result.institution ? result.institution.name : 'N/A']) as String
                response.sendError(HttpStatus.SC_FORBIDDEN)
                return
            }


            Closure newDate = { param, format ->
                Date date
                try {
                    date = dateFormat.parse(param)
                } catch (Exception e) {
                    log.debug("Unable to parse date : ${param} in format ${format}")
                }
                date
            }

            Date startDate = newDate(params.newStartDate, dateFormat.toPattern())
            Date endDate = newDate(params.newEndDate, dateFormat.toPattern())
            RefdataValue billing_currency = null
            if (params.long('newCostCurrency')) //GBP,etc
            {
                billing_currency = RefdataValue.get(params.newCostCurrency)
            }

            //def tempCurrencyVal       = params.newCostCurrencyRate?      params.double('newCostCurrencyRate',1.00) : 1.00//def cost_local_currency   = params.newCostInLocalCurrency?   params.double('newCostInLocalCurrency', cost_billing_currency * tempCurrencyVal) : 0.00
            RefdataValue cost_item_status = params.newCostItemStatus ? (RefdataValue.get(params.long('newCostItemStatus'))) : null;
            //estimate, commitment, etc
            RefdataValue cost_item_element = params.newCostItemElement ? (RefdataValue.get(params.long('newCostItemElement'))) : null
            //admin fee, platform, etc
            //moved to TAX_TYPES
            //RefdataValue cost_tax_type         = params.newCostTaxType ?          (RefdataValue.get(params.long('newCostTaxType'))) : null           //on invoice, self declared, etc

            NumberFormat format = NumberFormat.getInstance(LocaleUtils.getCurrentLocale())
            def cost_billing_currency = params.newCostInBillingCurrency ? format.parse(params.newCostInBillingCurrency).doubleValue() : 0.00
            //def cost_currency_rate = params.newCostCurrencyRate ? params.double('newCostCurrencyRate', 1.00) : 1.00
            //def cost_local_currency = params.newCostInLocalCurrency ? format.parse(params.newCostInLocalCurrency).doubleValue() : 0.00

            def cost_billing_currency_after_tax = params.newCostInBillingCurrencyAfterTax ? format.parse(params.newCostInBillingCurrencyAfterTax).doubleValue() : cost_billing_currency
            //def cost_local_currency_after_tax = params.newCostInLocalCurrencyAfterTax ? format.parse(params.newCostInLocalCurrencyAfterTax).doubleValue() : cost_local_currency
            //moved to TAX_TYPES
            //def new_tax_rate                      = params.newTaxRate ? params.int( 'newTaxRate' ) : 0
            def tax_key = null
            if (!params.newTaxRate.contains("null")) {
                String[] newTaxRate = params.newTaxRate.split("§")
                RefdataValue taxType = (RefdataValue) genericOIDService.resolveOID(newTaxRate[0])
                int taxRate = Integer.parseInt(newTaxRate[1])
                switch (taxType.id) {
                    case RDStore.TAX_TYPE_TAXABLE.id:
                        switch (taxRate) {
                            case 7: tax_key = CostItem.TAX_TYPES.TAXABLE_7
                                break
                            case 19: tax_key = CostItem.TAX_TYPES.TAXABLE_19
                                break
                        }
                        break
                    case RDStore.TAX_TYPE_TAXABLE_EXEMPT.id:
                        tax_key = CostItem.TAX_TYPES.TAX_EXEMPT
                        break
                    case RDStore.TAX_TYPE_NOT_TAXABLE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                        break
                    case RDStore.TAX_TYPE_NOT_APPLICABLE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                        break
                    case RDStore.TAX_TYPE_REVERSE_CHARGE.id:
                        tax_key = CostItem.TAX_TYPES.TAX_REVERSE_CHARGE
                        break
                }
            }
            RefdataValue cost_item_element_configuration = (params.ciec && params.ciec != 'null') ? RefdataValue.get(Long.parseLong(params.ciec)) : null

            boolean cost_item_isVisibleForSubscriber = false
            // (params.newIsVisibleForSubscriber ? (RefdataValue.get(params.newIsVisibleForSubscriber).value == 'Yes') : false)

            List surveyOrgsDo = []

            if (params.surveyOrg) {
                try {
                    surveyOrgsDo << genericOIDService.resolveOID(params.surveyOrg)
                } catch (Exception e) {
                    log.error("Non-valid surveyOrg sent ${params.surveyOrg}", e)
                }
            }

            if (params.get('surveyOrgs')) {
                List surveyOrgs = (params.get('surveyOrgs').split(',').collect {
                    String.valueOf(it.replaceAll("\\s", ""))
                }).toList()
                surveyOrgs.each {
                    try {

                        def surveyOrg = genericOIDService.resolveOID(it)
                        if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqual(surveyOrg,RDStore.COST_ITEM_DELETED)) {
                            surveyOrgsDo << surveyOrg
                        }
                    } catch (Exception e) {
                        log.error("Non-valid surveyOrg sent ${it}", e)
                    }
                }
            }

            /* if (params.surveyConfig) {
                 def surveyConfig = genericOIDService.resolveOID(params.surveyConfig)

                 surveyConfig.orgs.each {

                     if (!CostItem.findBySurveyOrg(it)) {
                         surveyOrgsDo << it
                     }
                 }
             }*/

            CostItem.withTransaction { TransactionStatus ts ->
                surveyOrgsDo.each { surveyOrg ->

                    if (!surveyOrg.existsMultiYearTerm()) {

                        if (params.oldCostItem && genericOIDService.resolveOID(params.oldCostItem)) {
                            newCostItem = genericOIDService.resolveOID(params.oldCostItem)
                        } else {
                            newCostItem = new CostItem()
                        }

                        newCostItem.owner = result.institution
                        newCostItem.surveyOrg = newCostItem.surveyOrg ?: surveyOrg
                        newCostItem.isVisibleForSubscriber = cost_item_isVisibleForSubscriber
                        newCostItem.costItemElement = cost_item_element
                        newCostItem.costItemStatus = cost_item_status
                        newCostItem.billingCurrency = billing_currency //Not specified default to GDP
                        //newCostItem.taxCode = cost_tax_type -> to taxKey
                        newCostItem.costTitle = params.newCostTitle ?: null
                        newCostItem.costInBillingCurrency = cost_billing_currency as Double
                        //newCostItem.costInLocalCurrency = cost_local_currency as Double

                        newCostItem.billingSumRounding = params.newBillingSumRounding ? true : false
                        newCostItem.finalCostRounding = params.newFinalCostRounding ? true : false
                        newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
                        //newCostItem.costInLocalCurrencyAfterTax = cost_local_currency_after_tax as Double
                        //newCostItem.currencyRate = cost_currency_rate as Double
                        //newCostItem.taxRate = new_tax_rate as Integer -> to taxKey
                        newCostItem.taxKey = tax_key
                        newCostItem.costItemElementConfiguration = cost_item_element_configuration

                        newCostItem.costDescription = params.newDescription ? params.newDescription.trim() : null

                        newCostItem.startDate = startDate ?: null
                        newCostItem.endDate = endDate ?: null

                        //newCostItem.includeInSubscription = null
                        //todo Discussion needed, nobody is quite sure of the functionality behind this...


                        if (!newCostItem.validate()) {
                            result.error = newCostItem.errors.allErrors.collect {
                                log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                                message(code: 'finance.addNew.error', args: [it.properties.field])
                            }
                        } else {
                            if (newCostItem.save()) {
                                /* def newBcObjs = []

                             params.list('newBudgetCodes').each { newbc ->
                                 def bc = genericOIDService.resolveOID(newbc)
                                 if (bc) {
                                     newBcObjs << bc
                                     if (! CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )) {
                                         new CostItemGroup(costItem: newCostItem, budgetCode: bc).save()
                                     }
                                 }
                             }

                             def toDelete = newCostItem.getBudgetcodes().minus(newBcObjs)
                             toDelete.each{ bc ->
                                 def cig = CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )
                                 if (cig) {
                                     log.debug('deleting ' + cig)
                                     cig.delete(flush:true)
                                 }
                             }*/

                            } else {
                                result.error = "Unable to save!"
                            }
                        }
                    }
                } // subsToDo.each
            }


        }
        catch (Exception e) {
            log.error("Problem in add cost item", e);
        }


        redirect(uri: request.getHeader('referer'))
    }

    /**
     * Call to compare the members of two given subscriptions, used to compare how many members had the given consortial subscription
     * in each year
     * @return a list of members for each subscription
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> compareMembersOfTwoSubs() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.parentSubscription = result.surveyConfig.subscription
        result.parentSubChilds = subscriptionService.getValidSubChilds(result.parentSubscription)
        if(result.surveyConfig.subSurveyUseForTransfer){
            result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        }else{
            result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        }
        result.targetSubscription =  result.parentSuccessorSubscription
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.superOrgType = []
        if(accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)) {
            result.superOrgType << message(code:'consortium.superOrgType')
        }

        result.participantsList = []

        result.parentParticipantsList = []
        result.parentSuccessortParticipantsList = []

        result.parentSubChilds.each { sub ->
            Org org = sub.getSubscriber()
            result.participantsList << org
            result.parentParticipantsList << org

        }

        result.parentSuccessorSubChilds.each { sub ->
            Org org = sub.getSubscriber()
            if(!(result.participantsList && org.id in result.participantsList.id)) {
                result.participantsList << org
            }
            result.parentSuccessortParticipantsList << org

        }

        result.participantsList = result.participantsList.sort{it.sortname}


        result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION
        if(result.surveyConfig.subSurveyUseForTransfer && result.parentSuccessorSubscription) {
            String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
            result.memberLicenses = License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE])
        }

        result

    }

    /**
     * Call to copy the survey cost items
     * @return a list of each participant's survey costs
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copySurveyCostItems() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.parentSubscription = result.surveyConfig.subscription
        if(result.surveyConfig.subSurveyUseForTransfer){
            result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        }else{
            result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        }
        result.targetSubscription =  result.parentSuccessorSubscription
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.participantsList = []

        result.parentSuccessortParticipantsList = []

        result.parentSuccessorSubChilds.each { sub ->
            Map newMap = [:]
            Org org = sub.getSubscriber()
            newMap.id = org.id
            newMap.sortname = org.sortname
            newMap.name = org.name
            newMap.newSub = sub
            newMap.oldSub = sub._getCalculatedPreviousForSurvey()

            newMap.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, org)
            newMap.surveyCostItem =newMap.surveyOrg ? CostItem.findBySurveyOrgAndCostItemStatusNotEqual(newMap.surveyOrg,RDStore.COST_ITEM_DELETED) : null

            result.participantsList << newMap

        }

        result.participantsList = result.participantsList.sort{it.sortname}

        result

    }

    /**
     * Takes the given parameters and creates copies of the given cost items, based on the submitted data
     * @return the survey cost copy overview
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopySurveyCostItems() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if(result.surveyConfig.subSurveyUseForTransfer){
            result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        }else{
            result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        }

        result.targetSubscription =  result.parentSuccessorSubscription

        Integer countNewCostItems = 0
        //RefdataValue costElement = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE
        CostItem.withTransaction { TransactionStatus ts ->
            params.list('selectedSurveyCostItem').each { costItemId ->

                CostItem costItem = CostItem.get(costItemId)
                Subscription participantSub = result.parentSuccessorSubscription?.getDerivedSubscriptionBySubscribers(costItem.surveyOrg.org)
                List participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED)
                if(costItem && participantSub && !participantSubCostItem){

                    Map properties = costItem.properties
                    CostItem copyCostItem = new CostItem()
                    InvokerHelper.setProperties(copyCostItem, properties)
                    copyCostItem.globalUID = null
                    copyCostItem.surveyOrg = null
                    copyCostItem.isVisibleForSubscriber = params.isVisibleForSubscriber ? true : false
                    copyCostItem.sub = participantSub
                    if(costItem.billingCurrency == RDStore.CURRENCY_EUR){
                        copyCostItem.currencyRate = 1.0
                        copyCostItem.costInLocalCurrency = costItem.costInBillingCurrency
                    }
                    Org org = participantSub.getSubscriber()
                    SurveyResult surveyResult = org ? SurveyResult.findBySurveyConfigAndParticipantAndTypeAndStringValueIsNotNull(result.surveyConfig, org, PropertyStore.SURVEY_PROPERTY_ORDER_NUMBER) : null

                    if(surveyResult){
                        Order order = new Order(orderNumber: surveyResult.getValue(), owner: result.institution)
                        if(order.save()) {
                            copyCostItem.order = order
                        }
                        else log.error(order.errors)
                    }

                    if(copyCostItem.save()) {
                        countNewCostItems++
                    }else {
                        log.debug("Error by proccessCopySurveyCostItems: "+ copyCostItem.errors)
                    }

                }

            }
        }
        flash.message = message(code: 'copySurveyCostItems.copy.success', args: [countNewCostItems]) as String
        redirect(action: 'copySurveyCostItems', id: params.id, params: [surveyConfigID: result.surveyConfig.id, targetSubscriptionId: result.targetSubscription.id])

    }

    /**
     * Call to open the transfer of survey cost items into the respective member subscriptions
     * @return a list of participants with their respective survey cost items
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copySurveyCostItemsToSub() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.parentSubscription = result.surveyConfig.subscription
        result.parentSubChilds = result.parentSubscription ? subscriptionService.getValidSubChilds(result.parentSubscription) : null

        result.participantsList = []

        result.parentSubChilds.each { sub ->
            Map newMap = [:]
            Org org = sub.getSubscriber()
            newMap.id = org.id
            newMap.sortname = org.sortname
            newMap.name = org.name
            newMap.newSub = sub

            newMap.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, org)
            newMap.surveyCostItem =newMap.surveyOrg ? CostItem.findBySurveyOrgAndCostItemStatusNotEqual(newMap.surveyOrg,RDStore.COST_ITEM_DELETED) : null

            result.participantsList << newMap

        }

        result.participantsList = result.participantsList.sort{it.sortname}

        result

    }

    /**
     * Takes the submitted parameters and copies the survey cost items into the subscriptions
     * @return the survey-subscription cost transfer view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopySurveyCostItemsToSub() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.parentSubscription = result.surveyConfig.subscription


        Integer countNewCostItems = 0
        //RefdataValue costElement = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE
        CostItem.withTransaction { TransactionStatus ->
            params.list('selectedSurveyCostItem').each { costItemId ->

                CostItem costItem = CostItem.get(costItemId)
                Subscription participantSub = result.parentSubscription?.getDerivedSubscriptionBySubscribers(costItem.surveyOrg.org)
                List participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED)
                if(costItem && participantSub && !participantSubCostItem){

                    Map properties = costItem.properties
                    CostItem copyCostItem = new CostItem()
                    InvokerHelper.setProperties(copyCostItem, properties)
                    copyCostItem.globalUID = null
                    copyCostItem.surveyOrg = null
                    copyCostItem.isVisibleForSubscriber = params.isVisibleForSubscriber ? true : false
                    copyCostItem.sub = participantSub
                    if(costItem.billingCurrency == RDStore.CURRENCY_EUR){
                        copyCostItem.currencyRate = 1.0
                        copyCostItem.costInLocalCurrency = costItem.costInBillingCurrency
                    }

                    if(copyCostItem.save()) {
                        countNewCostItems++
                    }else {
                        log.debug("Error by proccessCopySurveyCostItems: "+ copyCostItem.errors)
                    }

                }

            }
        }

        flash.message = message(code: 'copySurveyCostItems.copy.success', args: [countNewCostItems]) as String
        redirect(action: 'copySurveyCostItemsToSub', id: params.id, params: [surveyConfigID: result.surveyConfig.id])

    }

    /**
     * Call to open the property copying view from one subscription into another
     * @return the list of properties for each year ring
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copyProperties() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        params.tab = params.tab ?: 'surveyProperties'

        result.parentSubscription = result.surveyConfig.subscription
        if(result.surveyConfig.subSurveyUseForTransfer){
            result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        }else{
            result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        }

        if(!result.parentSubscription){
            result.parentSubscription = result.parentSuccessorSubscription
        }

        result.targetSubscription =  result.parentSuccessorSubscription
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.selectedProperty
        result.properties
        if(params.tab == 'surveyProperties') {
            result.properties = SurveyConfigProperties.findAllBySurveyConfig(result.surveyConfig).surveyProperty.findAll{it.tenant == null}
            result.properties -= PropertyStore.SURVEY_PROPERTY_PARTICIPATION
            result.properties -= PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2
            result.properties -= PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3
        }

        if(params.tab == 'customProperties') {
            result.properties = result.parentSubscription.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))}.type
        }

        if(params.tab == 'privateProperties') {
            result.properties = result.parentSubscription.propertySet.findAll{it.type.tenant?.id == result.contextOrg.id}.type
        }

        if(result.properties) {
            result.selectedProperty = params.selectedProperty ?: result.properties[0].id

            result.participantsList = []
            result.parentSuccessorSubChilds.each { sub ->

                Map newMap = [:]
                Org org = sub.getSubscriber()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = result.surveyConfig.subSurveyUseForTransfer ? sub._getCalculatedPreviousForSurvey() : result.parentSubscription.getDerivedSubscriptionBySubscribers(org)

                //println("new: ${newMap.newSub}, old: ${newMap.oldSub}")


                if (params.tab == 'surveyProperties') {
                    PropertyDefinition surProp = PropertyDefinition.get(result.selectedProperty)
                    newMap.surveyProperty = SurveyResult.findBySurveyConfigAndTypeAndParticipant(result.surveyConfig, surProp, org)
                    PropertyDefinition propDef = surProp ? PropertyDefinition.getByNameAndDescr(surProp.name, PropertyDefinition.SUB_PROP) : null


                    newMap.newCustomProperty = (sub && propDef) ? sub.propertySet.find {
                        it.type.id == propDef.id && it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                    } : null
                    newMap.oldCustomProperty = (newMap.oldSub && propDef) ? newMap.oldSub.propertySet.find {
                        it.type.id == propDef.id && it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                    } : null
                }
                if(params.tab == 'customProperties') {
                    newMap.newCustomProperty = (sub) ? sub.propertySet.find {
                        it.type.id == (result.selectedProperty instanceof Long ? result.selectedProperty : Long.parseLong(result.selectedProperty)) && it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                    } : null
                    newMap.oldCustomProperty = (newMap.oldSub) ? newMap.oldSub.propertySet.find {
                        it.type.id == (result.selectedProperty instanceof Long ? result.selectedProperty : Long.parseLong(result.selectedProperty)) && it.type.tenant == null && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                    } : null
                }

                if(params.tab == 'privateProperties') {
                    newMap.newPrivateProperty = (sub) ? sub.propertySet.find {
                        it.type.id == (result.selectedProperty instanceof Long ? result.selectedProperty : Long.parseLong(result.selectedProperty)) && it.type.tenant?.id == result.contextOrg.id
                    } : null
                    newMap.oldPrivateProperty = (newMap.oldSub) ? newMap.oldSub.propertySet.find {
                        it.type.id == (result.selectedProperty instanceof Long ? result.selectedProperty : Long.parseLong(result.selectedProperty)) && it.type.tenant?.id == result.contextOrg.id
                    } : null
                }


                result.participantsList << newMap
            }

            result.participantsList = result.participantsList.sort { it.sortname }
        }

        result

    }

    /**
     * Takes the submitted data and creates copies of the selected properties into the successor subscriptions
     * @return the property copy overview
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopyProperties() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if(result.surveyConfig.subSurveyUseForTransfer){
            result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        }else{
            result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        }
        result.targetSubscription =  result.parentSuccessorSubscription
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        if(params.list('selectedSub')) {
            result.selectedProperty
            PropertyDefinition propDef
            PropertyDefinition surveyProperty
            if (params.tab == 'surveyProperties') {
                result.selectedProperty = params.selectedProperty ?: null

                surveyProperty = params.copyProperty ? PropertyDefinition.get(Long.parseLong(params.copyProperty)) : null

                propDef = surveyProperty ? PropertyDefinition.getByNameAndDescr(surveyProperty.name, PropertyDefinition.SUB_PROP) : null
                if (!propDef && surveyProperty) {

                    Map<String, Object> map = [
                            token       : surveyProperty.name,
                            category    : 'Subscription Property',
                            type        : surveyProperty.type,
                            rdc         : (surveyProperty.isRefdataValueType()) ? surveyProperty.refdataCategory : null,
                            i10n        : [
                                    name_de: surveyProperty.getI10n('name', 'de'),
                                    name_en: surveyProperty.getI10n('name', 'en'),
                                    expl_de: surveyProperty.getI10n('expl', 'de'),
                                    expl_en: surveyProperty.getI10n('expl', 'en')
                            ]
                    ]
                    propDef = PropertyDefinition.construct(map)
                }

            } else {
                result.selectedProperty = params.selectedProperty ?: null
                propDef = params.selectedProperty ? PropertyDefinition.get(Long.parseLong(params.selectedProperty)) : null
            }

            Integer countSuccessfulCopy = 0

            if (propDef && params.list('selectedSub')) {
                params.list('selectedSub').each { subID ->
                    if (Long.parseLong(subID) in result.parentSuccessorSubChilds.id) {
                        Subscription sub = Subscription.get(Long.parseLong(subID))
                        Org org = sub.getSubscriber()
                        Subscription oldSub = sub._getCalculatedPreviousForSurvey()

                        AbstractPropertyWithCalculatedLastUpdated copyProperty
                        if (params.tab == 'surveyProperties') {
                            copyProperty = SurveyResult.findBySurveyConfigAndTypeAndParticipant(result.surveyConfig, surveyProperty, org)
                        } else {
                            if (params.tab == 'privateProperties') {
                                copyProperty = oldSub ? oldSub.propertySet.find {
                                    it.type.id == propDef.id && it.type.tenant.id == result.contextOrg.id
                                } : []
                            } else {
                                copyProperty = oldSub ? oldSub.propertySet.find {
                                    it.type.id == propDef.id && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                                } : []
                            }
                        }

                        if (copyProperty) {
                            if (propDef.tenant != null) {
                                //private Property
                                def existingProps = sub.propertySet.findAll {
                                    it.owner.id == sub.id && it.type.id == propDef.id && it.type.tenant.id == result.contextOrg.id
                                }
                                existingProps.removeAll { it.type.name != propDef.name } // dubious fix

                                if (existingProps.size() == 0 || propDef.multipleOccurrence) {
                                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, sub, propDef, result.contextOrg)
                                    if (newProp.hasErrors()) {
                                        log.error(newProp.errors.toString())
                                    } else {
                                        log.debug("New private property created: " + newProp.type.name)
                                        def newValue = copyProperty.getValue()
                                        if (copyProperty.type.isRefdataValueType()) {
                                            newValue = copyProperty.refValue ? copyProperty.refValue : null
                                        }
                                        def prop = _setNewProperty(newProp, newValue)
                                        countSuccessfulCopy++
                                    }
                                }
                            } else {
                                //custom Property
                                def existingProp = sub.propertySet.find {
                                    it.type.id == propDef.id && it.owner.id == sub.id && (it.tenant?.id == result.contextOrg.id || (it.tenant?.id != result.contextOrg.id && it.isPublic))
                                }

                                if (existingProp == null || propDef.multipleOccurrence) {
                                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, sub, propDef, result.contextOrg)
                                    if (newProp.hasErrors()) {
                                        log.error(newProp.errors.toString())
                                    } else {
                                        log.debug("New custom property created: " + newProp.type.name)
                                        def newValue = copyProperty.getValue()
                                        if (copyProperty.type.isRefdataValueType()) {
                                            newValue = copyProperty.refValue ? copyProperty.refValue : null
                                        }
                                        def prop = _setNewProperty(newProp, newValue)
                                        countSuccessfulCopy++
                                    }
                                }

                                /*if (existingProp) {
                            def customProp = SubscriptionCustomProperty.get(existingProp.id)
                            def prop = setNewProperty(customProp, copyProperty)
                        }*/
                            }
                        }
                    }
                }
            }
            flash.message = message(code: 'copyProperties.successful', args: [countSuccessfulCopy, message(code: 'copyProperties.' + params.tab) ,params.list('selectedSub').size()]) as String
        }

        redirect(action: 'copyProperties', id: params.id, params: [surveyConfigID: result.surveyConfig.id, tab: params.tab, selectedProperty: params.selectedProperty, targetSubscriptionId: result.targetSubscription?.id])

    }

    /**
     * Takes the given members and processes their renewal into the next year, i.e. creates new subscription instances for
     * the following year along with their depending data
     * @return a redirect to the member comparison view
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processTransferParticipantsByRenewal() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.parentSubscription = result.surveyConfig.subscription
        result.parentSubChilds = subscriptionService.getValidSubChilds(result.parentSubscription)
        result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION

        result.properties = []
        result.properties.addAll(SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty)

        result.multiYearTermFiveSurvey = null
        result.multiYearTermFourSurvey = null
        result.multiYearTermThreeSurvey = null
        result.multiYearTermTwoSurvey = null

        if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in result.properties.id) {
            result.multiYearTermFiveSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5
            result.properties.remove(result.multiYearTermFiveSurvey)

        }

        if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in result.properties.id) {
            result.multiYearTermFourSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4
            result.properties.remove(result.multiYearTermFourSurvey)

        }

        if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in result.properties.id) {
            result.multiYearTermThreeSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3
            result.properties.remove(result.multiYearTermThreeSurvey)
        }
        if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in result.properties.id) {
            result.multiYearTermTwoSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2
            result.properties.remove(result.multiYearTermTwoSurvey)

        }

        result.parentSuccessortParticipantsList = []

        result.parentSuccessorSubChilds.each { sub ->
            Org org = sub.getSubscriber()
            result.parentSuccessortParticipantsList << org

        }

        result.newSubs = []

        Integer countNewSubs = 0

        SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                [
                        owner      : result.institution.id,
                        surProperty: result.participationProperty.id,
                        surConfig  : result.surveyConfig.id,
                        refValue   : RDStore.YN_YES]).each {

            // Keine Kindlizenz in der Nachfolgerlizenz vorhanden
            if(!(result.parentSuccessortParticipantsList && it.participant.id in result.parentSuccessortParticipantsList.id)){

                Subscription oldSubofParticipant = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                        [parentSub  : result.parentSubscription,
                         participant: it.participant
                        ])[0]


                if(!oldSubofParticipant)
                {
                    oldSubofParticipant = result.parentSubscription
                }

                Date newStartDate = null
                Date newEndDate = null

                //Umfrage-Merkmal MJL2
                if (result.multiYearTermTwoSurvey) {

                    SurveyResult participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)
                    if (participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 2.year) : null
                        }
                            countNewSubs++
                            result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, params))
                    } else {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, params))
                    }

                }
                //Umfrage-Merkmal MJL3
                else if (result.multiYearTermThreeSurvey) {

                    SurveyResult participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                    if (participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 3.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, params))
                    }
                    else {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, params))
                    }
                }
                //Umfrage-Merkmal MJL4
                else if (result.multiYearTermFourSurvey) {

                    SurveyResult participantPropertyFour = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey)
                    if (participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 4.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, params))
                    }
                    else {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, params))
                    }
                }
                //Umfrage-Merkmal MJL5
                else if (result.multiYearTermFiveSurvey) {

                    SurveyResult participantPropertyFive = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey)
                    if (participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 5.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, params))
                    }
                    else {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                        }
                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, params))
                    }
                }else {
                    use(TimeCategory) {
                        newStartDate = oldSubofParticipant.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                        newEndDate = oldSubofParticipant.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                    }
                    countNewSubs++
                    result.newSubs.addAll(_processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, params))
                }
            }
        }

        //MultiYearTerm Subs
        result.parentSubChilds.each { sub ->
            if (sub.isCurrentMultiYearSubscriptionToParentSub()){
                sub.getAllSubscribers().each { org ->
                    if (!(result.parentSuccessortParticipantsList && org.id in result.parentSuccessortParticipantsList.id)) {

                        countNewSubs++
                        result.newSubs.addAll(_processAddMember(sub, result.parentSuccessorSubscription, org, sub.startDate, sub.endDate, true, params))
                    }
                }
            }

        }

        Set<Package> packagesToProcess = []

        //copy package data
        if(params.linkAllPackages) {
            result.parentSuccessorSubscription.packages.each { sp ->
                packagesToProcess << sp.pkg
            }
        }else if(params.packageSelection) {
            List packageIds = params.list("packageSelection")
            packageIds.each { spId ->
                packagesToProcess << SubscriptionPackage.get(spId).pkg
            }
        }

        boolean bulkProcessRunning = false
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
        threadArray.each {
            if (it.name == 'PackageTransfer_'+result.parentSuccessorSubscription.id) {
                bulkProcessRunning = true
            }
        }
        if(!bulkProcessRunning) {
            boolean withEntitlements = params.linkWithEntitlements == 'on'
            executorService.execute({
                Thread.currentThread().setName('PackageTransfer_'+result.parentSuccessorSubscription.id)
                packagesToProcess.each { pkg ->
                    subscriptionService.addToMemberSubscription(result.parentSuccessorSubscription, result.newSubs, pkg, withEntitlements)
                    /*result.newSubs.each { Subscription memberSub ->
                            if (linkWithEntitlements) {
                                subscriptionService.addToSubscriptionCurrentStock(memberSub, result.parentSuccessorSubscription, pkg)
                            }
                            else
                                subscriptionService.addToSubscription(memberSub, pkg, false)
                    }*/
                }
            })
        }

        result.countNewSubs = countNewSubs
        if(result.newSubs) {
            result.parentSuccessorSubscription.syncAllShares(result.newSubs)
        }
        flash.message = message(code: 'surveyInfo.transfer.info', args: [countNewSubs, result.newSubs.size() ?: 0]) as String


        redirect(action: 'compareMembersOfTwoSubs', id: params.id, params: [surveyConfigID: result.surveyConfig.id])


    }

    /**
     * Adds the given member to the given subscription, i.e. transfers the survey participant into a subscription member for the
     * next year's subscription
     * @param oldSub the predecessor member subscription
     * @param newParentSub the successor parent subscription
     * @param org the subscriber
     * @param newStartDate the new start date
     * @param newEndDate the new end date
     * @param multiYear is the new subscription a multi-year subscription?
     * @param params the request parameter map
     * @return the new member subscription instance
     */
    @DebugInfo(wtc = DebugInfo.IN_BETWEEN)
    private def _processAddMember(Subscription oldSub, Subscription newParentSub, Org org, Date newStartDate, Date newEndDate, boolean multiYear, params) {

        Org institution = contextService.getOrg()

        if (accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)) {

                License licenseCopy

                //def subLicense = newParentSub.owner

                List<License> licensesToProcess = []

                if(params.generateSlavedLics == "all") {
                    String query = "select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)"
                    licensesToProcess.addAll(License.executeQuery(query, [subscription:newParentSub, linkType:RDStore.LINKTYPE_LICENSE]))
                }
                else if(params.generateSlavedLics == "partial") {
                    List<String> licenseKeys = params.list("generateSlavedLicsReference")
                    licenseKeys.each { String licenseKey ->
                        licensesToProcess << genericOIDService.resolveOID(licenseKey)
                    }
                }


            log.debug("Generating seperate slaved instances for members")
            Subscription.withTransaction { TransactionStatus ts ->
                Date startDate = newStartDate ?: null
                Date endDate = newEndDate ?: null

                Subscription memberSub = new Subscription(
                        type: newParentSub.type ?: null,
                        kind: newParentSub.kind ?: null,
                        status: RDStore.SUBSCRIPTION_INTENDED,
                        name: newParentSub.name,
                        startDate: startDate,
                        endDate: endDate,
                        referenceYear: newParentSub.referenceYear ?: null,
                        administrative: newParentSub._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                        manualRenewalDate: newParentSub.manualRenewalDate,
                        identifier: UUID.randomUUID().toString(),
                        instanceOf: newParentSub,
                        isSlaved: true,
                        resource: newParentSub.resource ?: null,
                        form: newParentSub.form ?: null,
                        isPublicForApi: newParentSub.isPublicForApi,
                        hasPerpetualAccess: newParentSub.hasPerpetualAccess,
                        hasPublishComponent: newParentSub.hasPublishComponent,
                        isMultiYear: multiYear ?: false
                )

                if (!memberSub.save()) {
                    memberSub.errors.each { e ->
                        log.debug("Problem creating new sub: ${e}")
                    }
                }

                if (memberSub) {
                    if(accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)) {

                        new OrgRole(org: org, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS).save()
                        new OrgRole(org: institution, sub: memberSub, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA).save()

                        if(params.transferProviderAgency) {
                            newParentSub.getProviders().each { provider ->
                                new OrgRole(org: provider, sub: memberSub, roleType: RDStore.OR_PROVIDER).save()
                            }
                            newParentSub.getAgencies().each { provider ->
                                new OrgRole(org: provider, sub: memberSub, roleType: RDStore.OR_AGENCY).save()
                            }
                        }else if(params.providersSelection) {
                            List orgIds = params.list("providersSelection")
                            orgIds.each { orgID ->
                                new OrgRole(org: Org.get(orgID), sub: memberSub, roleType: RDStore.OR_PROVIDER).save()
                            }
                        }else if(params.agenciesSelection) {
                            List orgIds = params.list("agenciesSelection")
                            orgIds.each { orgID ->
                                new OrgRole(org: Org.get(orgID), sub: memberSub, roleType: RDStore.OR_AGENCY).save()
                            }
                        }

                    }

                    SubscriptionProperty.findAllByOwner(newParentSub).each { scp ->
                        AuditConfig ac = AuditConfig.getConfig(scp)

                        if (ac) {
                            // multi occurrence props; add one additional with backref
                            if (scp.type.multipleOccurrence) {
                                def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type, scp.tenant)
                                additionalProp = scp.copyInto(additionalProp)
                                additionalProp.instanceOf = scp
                                additionalProp.save()
                            }
                            else {
                                // no match found, creating new prop with backref
                                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type, scp.tenant)
                                newProp = scp.copyInto(newProp)
                                newProp.instanceOf = scp
                                newProp.save()
                            }
                        }
                    }

                    licensesToProcess.each { License lic ->
                        subscriptionService.setOrgLicRole(memberSub,lic,false)
                    }

                    if(oldSub){
                        Links.construct([linkType: RDStore.LINKTYPE_FOLLOWS, source: memberSub, destination: oldSub, owner: contextService.getOrg()]).save()
                    }

                    if (org.isCustomerType_Inst_Pro()) {
                        PendingChange.construct([target: memberSub, oid: "${memberSub.getClass().getName()}:${memberSub.id}", msgToken: "pendingChange.message_SU_NEW_01", status: RDStore.PENDING_CHANGE_PENDING, owner: org])
                    }

                    return memberSub
                }
            }
        }
    }

    /*
    private Map<String,Object> setResultGenericsAndCheckAccessforSub(checkOption) {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription.subscriber

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.subscription.isVisibleBy(result.user)) {
                log.debug("--- NOT VISIBLE ---")
                return null
            }
        }
        result.editable = result.subscription.isEditableBy(result.user)

        if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.editable) {
                log.debug("--- NOT EDITABLE ---")
                return null
            }
        }

        result
    }
    */

    /**
     * Updates the given property to the given value
     * @param property the property to update
     * @param value the value to set
     */
    @DebugInfo(wtc = DebugInfo.IN_BETWEEN)
    private def _setNewProperty(def property, def value) {

        String field = null

        if(property.type.isIntegerType()) {
            field = "intValue"
        }
        else if (property.type.isStringType())  {
            field = "stringValue"
        }
        else if (property.type.isBigDecimalType())  {
            field = "decValue"
        }
        else if (property.type.isDateType())  {
            field = "dateValue"
        }
        else if (property.type.isURLType())  {
            field = "urlValue"
        }
        else if (property.type.isRefdataValueType())  {
            field = "refValue"
        }

        //Wenn eine Vererbung vorhanden ist.
        if(field && property.hasProperty('instanceOf') && property.instanceOf && AuditConfig.getConfig(property.instanceOf)){
            if(property.instanceOf."${field}" == '' || property.instanceOf."${field}" == null)
            {
                value = property.instanceOf."${field}" ?: ''
            }else{
                //
                return
            }
        }

        PropertyDefinition.withTransaction { TransactionStatus ts ->
            if (value == '' && field) {
                // Allow user to set a rel to null be calling set rel ''
                property[field] = null
                property.save()
            } else {

                if (property && value && field){

                    if(field == "refValue") {
                        def binding_properties = ["${field}": value]
                        bindData(property, binding_properties)
                        //property.save(flush:true)
                        if(!property.save(failOnError: true))
                        {
                            println(property.error)
                        }
                    } else if(field == "dateValue") {
                        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

                        def backup = property."${field}"
                        try {
                            if (value && value.size() > 0) {
                                // parse new date
                                Date parsed_date = sdf.parse(value)
                                property."${field}" = parsed_date
                            } else {
                                // delete existing date
                                property."${field}" = null
                            }
                            property.save(failOnError: true)
                        }
                        catch (Exception e) {
                            property."${field}" = backup
                            log.error( e.toString() )
                        }
                    } else if(field == "urlValue") {

                        def backup = property."${field}"
                        try {
                            if (value && value.size() > 0) {
                                property."${field}" = new URL(value)
                            } else {
                                // delete existing url
                                property."${field}" = null
                            }
                            property.save(failOnError: true)
                        }
                        catch (Exception e) {
                            property."${field}" = backup
                            log.error( e.toString() )
                        }
                    } else {
                        def binding_properties = [:]
                        if(field == "decValue") {
                            value = new BigDecimal(value)
                        }

                        binding_properties["${field}"] = value
                        bindData(property, binding_properties)

                        property.save(failOnError: true)

                    }

                }
            }
        }
    }

    /**
     * Call for the given / next survey element copy procedure step. If data is submitted,
     * the call will process copying of the given survey elements
     * @return the given tab with the copy parameters
     */
    @DebugInfo(ctxInstUserCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = {
        ctx.accessService.ctxInstUserCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copyElementsIntoSurvey() {
        def result             = [:]
        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextOrg      = result.institution

        flash.error = ""
        flash.message = ""
        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId ?: params.id
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.editable = result.sourceObject.surveyInfo.isEditable()

        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.allObjects_readRights = SurveyConfig.executeQuery("select surConfig from SurveyConfig as surConfig join surConfig.surveyInfo as surInfo where surInfo.owner = :contextOrg order by surInfo.name", [contextOrg: result.contextOrg])
        //Nur Umfragen, die noch in Bearbeitung sind da sonst Umfragen-Prozesse zerstört werden.
        result.allObjects_writeRights = SurveyConfig.executeQuery("select surConfig from SurveyConfig as surConfig join surConfig.surveyInfo as surInfo where surInfo.owner = :contextOrg and surInfo.status = :status order by surInfo.name", [contextOrg: result.contextOrg, status: RDStore.SURVEY_IN_PROCESSING])

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                break
            case CopyElementsService.WORKFLOW_SUBSCRIBER:
                result << copyElementsService.copyObjectElements_Subscriber(params)
                result << copyElementsService.loadDataFor_Subscriber(params)
                break
            case CopyElementsService.WORKFLOW_PROPERTIES:
                result << copyElementsService.copyObjectElements_Properties(params)
                result << copyElementsService.loadDataFor_Properties(params)
                break
            case CopyElementsService.WORKFLOW_END:
                result << copyElementsService.copyObjectElements_Properties(params)
                if (params.targetObjectId){
                    flash.error = ""
                    flash.message = ""
                    redirect controller: 'survey', action: 'show', params: [id: result.targetObject.surveyInfo.id, surveyConfigID: result.targetObject.id]
                    return
                }
                break
            default:
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
        }

        if (params.targetObjectId) {
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
        result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS

        result
    }

    /**
     * Lists the tasks attached to the given survey
     * @return the list of tasks of the given survey
     */
    @DebugInfo(ctxPermAffiliation = [CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER'], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')
    })
    def tasks() {
        Map<String,Object> ctrlResult = surveyControllerService.tasks(this,params)
        if(ctrlResult.error == SurveyControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }

        ctrlResult.result
    }

    /**
     * Lists the notes attached to the given survey
     * @return the list of notes of the given survey
     */
    @DebugInfo(ctxPermAffiliation = [CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER'], ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')
    })
    def notes() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result) {
            response.sendError(401); return
        }

        result
    }

    /**
     * Set link to license or provider
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setProviderOrLicenseLink() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (params.license) {
                License license = genericOIDService.resolveOID(params.license)
                result.surveyInfo.license = license ?: result.surveyInfo.license

                if (!result.surveyInfo.save()) {
                    flash.error = g.message(code: 'surveyInfo.link.fail')
                }
            }

            if (params.provider) {
                Org provider = genericOIDService.resolveOID(params.provider)
                result.surveyInfo.provider = provider ?: result.surveyInfo.provider

                if (!result.surveyInfo.save()) {
                    flash.error = g.message(code: 'surveyInfo.link.fail')
                }
            }

            if (params.unlinkLicense) {
                result.surveyInfo.license = null

                if (!result.surveyInfo.save()) {
                    flash.error = g.message(code: 'surveyInfo.unlink.fail')
                }
            }

            if (params.unlinkProvider) {
                result.surveyInfo.provider = null

                if (!result.surveyInfo.save()) {
                    flash.error = g.message(code: 'surveyInfo.unlink.fail')
                }
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Set link to license or provider
     * @return a redirect to the referer
     */
    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = 1)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyLink() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        SurveyInfo.withTransaction { TransactionStatus ts ->
            if (params.linkSurvey) {
                SurveyInfo linkSurvey = SurveyInfo.get(params.linkSurvey)

                if (linkSurvey) {
                    if (SurveyLinks.findBySourceSurveyAndTargetSurvey(result.surveyInfo, linkSurvey)) {
                        flash.error = g.message(code: 'surveyLinks.link.exists')
                    } else {
                        SurveyLinks surveyLink = new SurveyLinks(sourceSurvey: result.surveyInfo, targetSurvey: linkSurvey)
                        if (!surveyLink.save()) {
                            flash.error = g.message(code: 'surveyInfo.link.fail')
                        } else {
                            if (params.bothDirection && !SurveyLinks.findBySourceSurveyAndTargetSurvey(linkSurvey, result.surveyInfo)) {
                                SurveyLinks surveyLink2 = new SurveyLinks(sourceSurvey: linkSurvey, targetSurvey: result.surveyInfo, bothDirection: true)
                                surveyLink.bothDirection = true

                                if (!surveyLink2.save() && !surveyLink.save()) {
                                    flash.error = g.message(code: 'surveyInfo.link.fail')
                                }
                            }
                        }
                    }
                }
            }

            if (params.unlinkSurveyLink) {
                SurveyLinks surveyLink = SurveyLinks.get(params.unlinkSurveyLink)
                if(surveyLink.bothDirection){
                    SurveyLinks surveyLink2 = SurveyLinks.findBySourceSurveyAndTargetSurvey(surveyLink.targetSurvey, surveyLink.sourceSurvey)
                    if(surveyLink2)
                        surveyLink2.delete()
                }
                surveyLink.delete()

            }
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugInfo(ctxInstEditorCheckPerm_or_ROLEADMIN = [CustomerTypeService.ORG_CONSORTIUM_PRO], wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.ctxInstEditorCheckPerm_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSurveyUrl() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        if(params.deleteSurveyUrl){
            SurveyConfig.withTransaction { TransactionStatus ts ->
                SurveyUrl surveyUrl = SurveyUrl.get(Long.parseLong(params.deleteSurveyUrl))
                surveyUrl.delete()
            }
        }else {
            if (result.surveyConfig.surveyUrls.size() >= 10) {
                flash.error = g.message(code: 'surveyconfig.url.fail.max10')
            } else {
                if (params.url) {
                    SurveyConfig.withTransaction { TransactionStatus ts ->
                        SurveyUrl surveyUrl = new SurveyUrl(url: params.url, urlComment: params.urlComment, surveyConfig: result.surveyConfig)
                        if (!surveyUrl.save()) {
                            flash.error = g.message(code: 'survey.change.fail')
                        }
                    }
                }
            }
        }

        redirect(url: request.getHeader('referer'))

    }
}
