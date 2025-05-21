package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.ctrl.FinanceControllerService
import de.laser.ctrl.SurveyControllerService
import de.laser.helper.Params
import de.laser.helper.Profiler
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.SubscriptionProperty
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.finance.CostItem
import de.laser.properties.PropertyDefinition
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyUrl
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.PdfUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.transaction.TransactionStatus
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Year

/**
 * This controller manages the survey-related calls
 * @see SurveyConfig
 * @see SurveyResult
 * @see SurveyInfo
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class SurveyController {

    ContextService contextService
    ComparisonService comparisonService
    CopyElementsService copyElementsService
    CustomerTypeService customerTypeService
    DocstoreService docstoreService
    ExportClickMeService exportClickMeService
    ExportService exportService
    GenericOIDService genericOIDService
    FilterService filterService
    FinanceControllerService financeControllerService
    LinksGenerationService linksGenerationService
    ProviderService providerService
    SubscriptionService subscriptionService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyControllerService surveyControllerService
    SurveyService surveyService

    //-----

    /**
     * Map containing menu alternatives if an unexisting object has been called
     */
    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'myInstitution/currentSurveys' : 'currentSurveys.label'
    ]

    //-----

    /**
     * Redirects the call to the survey details view
     * @return the survey details view of the given survey
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> redirectSurveyConfig() {
        SurveyConfig surveyConfig = SurveyConfig.get(params.id)

        redirect(action: 'show', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id])

    }

    /**
     * Loads for the context consortium the currently running surveys
     * @return a table view of the current surveys
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> currentSurveysConsortia() {
        Map<String, Object> result = [:]
        Profiler prf = new Profiler()
        prf.setBenchmark("init")
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO )

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.max = result.max
        params.offset = result.offset
        //params.filterStatus = params.filterStatus ?: ((params.size() > 4) ? "" : [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_READY.id, RDStore.SURVEY_IN_PROCESSING.id])
        prf.setBenchmark("before properties")

        result.propList = PropertyDefinition.executeQuery( "select surpro.surveyProperty from SurveyConfigProperties as surpro join surpro.surveyConfig surConfig join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg order by surpro.surveyProperty.name_de asc", [contextOrg: contextService.getOrg()]).groupBy {it}.collect {it.key}

        prf.setBenchmark("after properties")

        prf.setBenchmark("before surveyYears")
        result.surveyYears = SurveyInfo.executeQuery("select Year(startDate) from SurveyInfo where owner = :org and startDate != null group by YEAR(startDate) order by YEAR(startDate) desc", [org: contextService.getOrg()]) ?: []

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears.push(newYear)
            }
            params.validOnYear = [newYear]
        }

        prf.setBenchmark("after surveyYears and before current org ids of providers and vendors")
        result.providers = providerService.getCurrentProviders( contextService.getOrg() )
        prf.setBenchmark("after providers and vendors and before subscriptions")
        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIUM, 'activeInst': contextService.getOrg()])
        prf.setBenchmark("after subscriptions and before survey config query")
        FilterService.Result fsr = filterService.getSurveyConfigQueryConsortia(params, DateUtils.getLocalizedSDF_noTime(), contextService.getOrg())
        if (fsr.isFilterSet) { params.filterSet = true }

        prf.setBenchmark("after query, before survey config execution")
        result.surveys = SurveyInfo.executeQuery(fsr.query, fsr.queryParams, params)
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
                wb = (SXSSFWorkbook) surveyService.exportSurveyCostItems(result.surveys.collect {it[1]}, contextService.getOrg())
            }else{
                SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
                String datetoday = sdf.format(new Date())
                String filename = "${datetoday}_" + g.message(code: "survey.plural")
                //if(wb instanceof XSSFWorkbook) file += "x";
                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb = (SXSSFWorkbook) surveyService.exportSurveys(result.surveys.collect {it[1]}, contextService.getOrg())
            }
            
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return

        }else {
            prf.setBenchmark("before surveysCount")
            result.surveysCount = SurveyInfo.executeQuery(fsr.query, fsr.queryParams).size()
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String, Object> workflowsSurveysConsortia() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO )

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        params.max = result.max
        params.offset = result.offset

        params.tab = params.tab ?: 'created'

        result.surveyYears = SurveyInfo.executeQuery("select Year(startDate) from SurveyInfo where owner = :org and startDate != null group by YEAR(startDate) order by YEAR(startDate) desc", [org: contextService.getOrg()]) ?: []

        if (params.validOnYear == null || params.validOnYear == '') {
            SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
            String newYear = sdfyear.format(new Date())

            if(!(newYear in result.surveyYears)){
                result.surveyYears.push(newYear)
            }
            params.validOnYear = [newYear]
        }

        result.propList = PropertyDefinition.executeQuery( "select surpro.surveyProperty from SurveyConfigProperties as surpro join surpro.surveyConfig surConfig join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg order by surpro.surveyProperty.name_de asc", [contextOrg: contextService.getOrg()]).groupBy {it}.collect {it.key}

        result.providers = providerService.getCurrentProviders( contextService.getOrg() )

        result.subscriptions = Subscription.executeQuery("select DISTINCT s.name from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                " AND s.instanceOf is not null order by s.name asc ", ['roleType': RDStore.OR_SUBSCRIPTION_CONSORTIUM, 'activeInst': contextService.getOrg()])


        DateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        FilterService.Result fsr = filterService.getSurveyConfigQueryConsortia(params, sdFormat, contextService.getOrg())
        if (fsr.isFilterSet) { params.filterSet = true }

        result.surveys = SurveyInfo.executeQuery(fsr.query, fsr.queryParams, params)

        if ( params.exportXLSX ) {
            SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
            String datetoday = sdf.format(new Date())
            String filename = "${datetoday}_" + g.message(code: "survey.plural")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportSurveys(SurveyConfig.findAllByIdInList(result.surveys.collect {it[1].id}), contextService.getOrg())
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return

        }else {
            result.surveysCount = SurveyInfo.executeQuery(fsr.query, fsr.queryParams).size()
            result.countSurveyConfigs = surveyService.getSurveyConfigCounts(params)

            result.filterSet = params.filterSet ? true : false

            result
        }

    }

    /**
     * Call to create a general survey
     * @return the new survey form
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> createGeneralSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO )

        result
    }

    /**
     * Takes the given parameters and creates a new general survey based on the input
     * @return the new survey details view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateGeneralSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = true

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
                        configOrder: 1,
                        packageSurvey: (params.packageSurvey ?: false),
                        invoicingInformation: (params.invoicingInformation ?: false),
                        vendorSurvey: (params.vendorSurvey ?: false),
                        subscriptionSurvey: (params.subscriptionSurvey ?: false)
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
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> createSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        String consortiaFilter = 'and s.instanceOf = null'

        Set<Year> availableReferenceYears = Subscription.executeQuery('select s.referenceYear from OrgRole oo join oo.sub s where s.referenceYear != null and oo.org = :contextOrg '+consortiaFilter+' order by s.referenceYear desc', [contextOrg: contextService.getOrg()])
        result.referenceYears = availableReferenceYears

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        def date_restriction
        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = true

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        Set providerIds = providerService.getCurrentProviderIds( contextService.getOrg() )

        result.providers = providerIds.isEmpty() ? [] : Provider.findAllByIdInList(providerIds).sort { it?.name }

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, '', contextService.getOrg())
        result.filterSet = tmpQ[2]
        List<Subscription> subscriptions

        subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )

        if(params.sort == "provider") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String sortname1 = s1.getSortedProviders(params.order)[0]?.sortname?.toLowerCase(), sortname2 = s2.getSortedProviders(params.order)[0]?.sortname?.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!sortname1) {
                        if(!sortname2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!sortname2)
                            cmp = -1
                        else cmp = sortname1 <=> sortname2
                    }
                }
                else cmp = sortname2 <=> sortname1
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.name <=> s2.name : s2.name <=> s1.name
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.startDate <=> s2.startDate : s2.startDate <=> s1.startDate
                cmp
            }
        }
        else if(params.sort == "vendor") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String sortname1 = s1.getSortedVendors(params.order)[0]?.sortname?.toLowerCase(), sortname2 = s2.getSortedVendors(params.order)[0]?.sortname?.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!sortname1) {
                        if(!sortname2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!sortname2)
                            cmp = -1
                        else cmp = sortname1 <=> sortname2
                    }
                }
                else cmp = sortname2 <=> sortname1
                if(!cmp) {
                    cmp = params.order == 'asc' ? s1.name <=> s2.name : s2.name <=> s1.name
                }
                if(!cmp) {
                    cmp = params.order == 'asc' ? s1.startDate <=> s2.startDate : s2.startDate <=> s1.startDate
                }
                cmp
            }
        }
        result.allSubscriptions = subscriptions

        result.date_restriction = date_restriction
        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.getOrg())
        result.num_sub_rows = subscriptions.size()
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        if(subscriptions)
            result.allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndSourceLicenseIsNotNullAndLinkType(result.subscriptions,RDStore.LINKTYPE_LICENSE)

        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)
        result

    }

    /**
     * Call to create a title survey
     * @return the new survey form
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
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

        result.editable = true

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_INTENDED.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        Set providerIds = providerService.getCurrentProviderIds( contextService.getOrg() )

        result.providers = providerIds.isEmpty() ? [] : Provider.findAllByIdInList(providerIds).sort { it?.name }

        List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params)
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
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSubtoSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = true

        result.subscription = Subscription.get( params.long('sub') )
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
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSubtoIssueEntitlementsSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = true

        result.subscription = Subscription.get( params.long('sub') )
        result.pickAndChoose = true
        //double-check needed because menu is not being refreshed after xEditable change on sub/show
        if(AuditConfig.getConfig(result.subscription, 'holdingSelection')) {
            flash.error = message(code: 'subscription.details.addEntitlements.holdingInherited')
            redirect controller: 'subscription', action: 'show', params: [id: params.long('sub')]
            return
        }
        else if (!result.subscription) {
            redirect action: 'createIssueEntitlementsSurvey'
            return
        }

        result
    }

    /**
     * Takes the given parameters and creates a new subscription survey based on the input
     * @return the new survey details view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateSubscriptionSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = true

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

        Subscription subscription = Subscription.get( params.long('sub') )
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
                        subSurveyUseForTransfer: subSurveyUseForTransfer,
                        packageSurvey: (params.packageSurvey ?: false),
                        invoicingInformation: (params.invoicingInformation ?: false),
                        vendorSurvey: (params.vendorSurvey ?: false)
                )

                surveyConfig.save()

                //Wenn es eine Umfrage schon gibt, die als Übertrag dient. Dann ist es auch keine Lizenz Umfrage mit einem Teilnahme-Merkmal abfragt!
                if (subSurveyUseForTransfer) {
                    SurveyConfigProperties configProperty = new SurveyConfigProperties(
                            surveyProperty: PropertyStore.SURVEY_PROPERTY_PARTICIPATION,
                            surveyConfig: surveyConfig,
                            mandatoryProperty: true,
                            propertyOrder: 1)

                    /*SurveyConfigProperties configProperty2 = new SurveyConfigProperties(
                            surveyProperty: PropertyStore.SURVEY_PROPERTY_ORDER_NUMBER,
                            surveyConfig: surveyConfig)*/

                    /*if (configProperty.save() && configProperty2.save()) {
                        surveyService.addSubMembers(surveyConfig)
                    }*/

                    if (configProperty.save()) {
                        surveyService.addSubMembers(surveyConfig)
                    }
                }

                //Alle Title-Umfragen Teilnahme-Merkmale hinzufügen
                if (surveyConfig.pickAndChoose) {
                    SurveyConfigProperties configProperty = new SurveyConfigProperties(
                            surveyProperty: PropertyStore.SURVEY_PROPERTY_PARTICIPATION,
                            surveyConfig: surveyConfig,
                            mandatoryProperty: true,
                            propertyOrder: 1)

                    if (configProperty.save()) {
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
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processCreateIssueEntitlementsSurvey() {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.editable = true

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
            Subscription subscription = Subscription.get( params.long('sub') )
            if (subscription && !SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo)) {
                SurveyConfig surveyConfig = new SurveyConfig(
                        subscription: subscription,
                        configOrder: surveyInfo.surveyConfigs?.size() ? surveyInfo.surveyConfigs.size() + 1 : 1,
                        type: 'IssueEntitlementsSurvey',
                        surveyInfo: surveyInfo,
                        subSurveyUseForTransfer: false,
                        pickAndChoose: true,
                        pickAndChoosePerpetualAccess: (subscription.hasPerpetualAccess),
                        issueEntitlementGroupName: params.issueEntitlementGroupNew,
                        vendorSurvey: (params.vendorSurvey ?: false)
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    @Check404(domain=SurveyInfo)
    def show() {
        Map<String,Object> ctrlResult = surveyControllerService.show(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def editComment() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            render template: "/templates/generic_modal403", model: result
        }else {
            result.commentTyp   = params.commentTyp
            render template: "modal_comment_edit", model: result
        }
    }

    /**
     * Lists the titles subject of the given survey
     * @return a list view of the issue entitlements linked to the given survey
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyTitles() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyTitles(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Lists the documents attached to the given survey configuration
     * @return a list of the survey config documents
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyConfigDocs() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }

        result

    }

    /**
     * Lists the participants of the given survey. The result may be filtered
     * @return the participant list for the given tab
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyParticipants() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyParticipants(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if(!params.sub && ((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))){
                flash.error = message(code: 'filter.subscription.empty')
            }

            ctrlResult.result
        }

    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def addSurveyParticipants() {
        Map<String,Object> ctrlResult = surveyControllerService.addSurveyParticipants(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if(!params.sub && ((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))){
                flash.error = message(code: 'filter.subscription.empty')
            }

            ctrlResult.result
        }

    }

    /**
     * Lists the costs linked to the given survey, reflecting upcoming subscription costs
     * @return a list of costs linked to the survey
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyCostItems() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyCostItems(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(params.containsKey('costInformation')) {
                CostItem.withTransaction {
                    MultipartFile inputFile = request.getFile("costInformation")
                    if(inputFile && inputFile.size > 0) {
                        String filename = params.costInformation.originalFilename
                        RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                        String encoding = UniversalDetector.detectCharset(inputFile.getInputStream())
                        if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                            ctrlResult.result.putAll(surveyService.financeEnrichment(inputFile, encoding, pickedElement, ctrlResult.result.surveyConfig))
                        }
                        if(ctrlResult.result.containsKey('wrongIdentifiers')) {
                            //background of this procedure: the editor adding prices via file wishes to receive a "counter-file" which will then be sent to the provider for verification
                            String dir = GlobalService.obtainTmpFileLocation()
                            File f = new File(dir+"/${filename}_matchingErrors")
                            ctrlResult.result.token = "${filename}_matchingErrors"
                            String returnFile = exportService.generateSeparatorTableString(null, ctrlResult.result.wrongIdentifiers, '\t')
                            FileOutputStream fos = new FileOutputStream(f)
                            fos.withWriter { Writer w ->
                                w.write(returnFile)
                            }
                            fos.flush()
                            fos.close()
                        }
                        params.remove("costInformation")
                    }
                }
            }
            if(params.containsKey('selectedCostItemElement') && !ctrlResult.result.containsKey('wrongSeparator')) {
                RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                String query = 'select ci.id from CostItem ci where (ci.surveyConfigSubscription is null and ci.sub is null and ci.pkg is null) and ci.costItemStatus != :status and ci.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ci.costItemElement = :element and (ci.costInBillingCurrency = 0 or ci.costInBillingCurrency = null)'

                Set<CostItem> missing = CostItem.executeQuery(query, [status: RDStore.COST_ITEM_DELETED, surConfig: ctrlResult.result.surveyConfig, element: pickedElement])
                ctrlResult.result.missing = missing
            }

            if(!params.sub && ((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))){
                flash.error = message(code: 'filter.subscription.empty')
            }

            ctrlResult.result
        }

    }

    /**
     * Lists the costs linked to the given survey, reflecting upcoming subscription costs
     * @return a list of costs linked to the survey
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyCostItemsPackages() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyCostItemsPackages(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(params.containsKey('costInformation')) {
                CostItem.withTransaction {
                    MultipartFile inputFile = request.getFile("costInformation")
                    if(inputFile && inputFile.size > 0) {
                        String filename = params.costInformation.originalFilename
                        RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                        String encoding = UniversalDetector.detectCharset(inputFile.getInputStream())
                        Package pkg = params.selectedPackageID ? Package.get(params.long('selectedPackageID')) : null
                        if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"] && pkg) {
                            ctrlResult.result.putAll(surveyService.financeEnrichment(inputFile, encoding, pickedElement, ctrlResult.result.surveyConfig, pkg))
                        }
                        if(ctrlResult.result.containsKey('wrongIdentifiers')) {
                            //background of this procedure: the editor adding prices via file wishes to receive a "counter-file" which will then be sent to the provider for verification
                            String dir = GlobalService.obtainTmpFileLocation()
                            File f = new File(dir+"/${filename}_matchingErrors")
                            ctrlResult.result.token = "${filename}_matchingErrors"
                            String returnFile = exportService.generateSeparatorTableString(null, ctrlResult.result.wrongIdentifiers, '\t')
                            FileOutputStream fos = new FileOutputStream(f)
                            fos.withWriter { Writer w ->
                                w.write(returnFile)
                            }
                            fos.flush()
                            fos.close()
                        }
                        params.remove("costInformation")
                    }
                }
            }
            if(params.containsKey('selectedCostItemElement') && !ctrlResult.result.containsKey('wrongSeparator')) {
                RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                Package pkg = params.selectedPackageID ? Package.get(params.long('selectedPackageID')) : null
                String query = 'select ci.id from CostItem ci where (ci.surveyConfigSubscription is null and ci.sub is null and ci.pkg is not null) and ci.pkg = :pkg and ci.costItemStatus != :status and ci.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ci.costItemElement = :element and (ci.costInBillingCurrency = 0 or ci.costInBillingCurrency = null)'

                Set<CostItem> missing = CostItem.executeQuery(query, [pkg: pkg, status: RDStore.COST_ITEM_DELETED, surConfig: ctrlResult.result.surveyConfig, element: pickedElement])
                ctrlResult.result.missing = missing
            }

            if(!params.sub && ((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))){
                flash.error = message(code: 'filter.subscription.empty')
            }

            ctrlResult.result
        }

    }

    /**
     * Call to list the potential package candidates for linking
     * @return a list view of the packages in the we:kb ElasticSearch index or a redirect to an title list view
     * if a package UUID has been submitted with the call
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def linkSurveyPackage() {
        Map<String,Object> ctrlResult = surveyControllerService.linkSurveyPackage(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
                flash.message = ctrlResult.result.message
                ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processLinkSurveyPackage() {
        Map<String,Object> ctrlResult = surveyControllerService.processLinkSurveyPackage(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            ctrlResult.result
            redirect(action: 'surveyPackages', id: ctrlResult.result.surveyInfo.id)
            return
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def linkSurveySubscription() {
        Map<String,Object> ctrlResult = surveyControllerService.linkSurveySubscription(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            flash.message = ctrlResult.result.message
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processLinkSurveySubscription() {
        Map<String,Object> ctrlResult = surveyControllerService.processLinkSurveySubscription(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            ctrlResult.result
            redirect(action: 'surveySubscriptions', id: ctrlResult.result.surveyInfo.id)
            return
        }
    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> surveyCostItemsSubscriptions() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyCostItemsSubscriptions(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(params.containsKey('costInformation')) {
                CostItem.withTransaction {
                    MultipartFile inputFile = request.getFile("costInformation")
                    if(inputFile && inputFile.size > 0) {
                        String filename = params.costInformation.originalFilename
                        RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                        String encoding = UniversalDetector.detectCharset(inputFile.getInputStream())
                        Package pkg = params.selectedPackageID ? Package.get(params.long('selectedPackageID')) : null
                        if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"] && pkg) {
                            ctrlResult.result.putAll(surveyService.financeEnrichment(inputFile, encoding, pickedElement, ctrlResult.result.surveyConfig, pkg))
                        }
                        if(ctrlResult.result.containsKey('wrongIdentifiers')) {
                            //background of this procedure: the editor adding prices via file wishes to receive a "counter-file" which will then be sent to the provider for verification
                            String dir = GlobalService.obtainTmpFileLocation()
                            File f = new File(dir+"/${filename}_matchingErrors")
                            ctrlResult.result.token = "${filename}_matchingErrors"
                            String returnFile = exportService.generateSeparatorTableString(null, ctrlResult.result.wrongIdentifiers, '\t')
                            FileOutputStream fos = new FileOutputStream(f)
                            fos.withWriter { Writer w ->
                                w.write(returnFile)
                            }
                            fos.flush()
                            fos.close()
                        }
                        params.remove("costInformation")
                    }
                }
            }
            if(params.containsKey('selectedCostItemElement') && !ctrlResult.result.containsKey('wrongSeparator')) {
                RefdataValue pickedElement = RefdataValue.get(params.selectedCostItemElement)
                Package pkg = params.selectedPackageID ? Package.get(params.long('selectedPackageID')) : null
                String query = 'select ci.id from CostItem ci where (ci.surveyConfigSubscription is not null and ci.sub is null and ci.pkg is null) and ci.pkg = :pkg and ci.costItemStatus != :status and ci.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ci.costItemElement = :element and (ci.costInBillingCurrency = 0 or ci.costInBillingCurrency = null)'

                Set<CostItem> missing = CostItem.executeQuery(query, [pkg: pkg, status: RDStore.COST_ITEM_DELETED, surConfig: ctrlResult.result.surveyConfig, element: pickedElement])
                ctrlResult.result.missing = missing
            }

            if(!params.sub && ((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))){
                flash.error = message(code: 'filter.subscription.empty')
            }

            ctrlResult.result
        }

    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def linkSurveyVendor() {
        Map<String,Object> ctrlResult = surveyControllerService.linkSurveyVendor(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            flash.message = ctrlResult.result.message
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processLinkSurveyVendor() {
        Map<String,Object> ctrlResult = surveyControllerService.processLinkSurveyVendor(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            ctrlResult.result
            redirect(action: 'surveyVendors', id: ctrlResult.result.surveyInfo.id)
            return
        }
    }

    /**
     * Call to list the potential package candidates for linking
     * @return a list view of the packages in the we:kb ElasticSearch index or a redirect to an title list view
     * if a package UUID has been submitted with the call
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def surveyPackages() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyPackages(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
                ctrlResult.result
            }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def surveyVendors() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyVendors(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            ctrlResult.result
        }
    }

    /**
     * Call to list the potential package candidates for linking
     * @return a list view of the packages in the we:kb ElasticSearch index or a redirect to an title list view
     * if a package UUID has been submitted with the call
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def surveySubscriptions() {
        Map<String,Object> ctrlResult = surveyControllerService.surveySubscriptions(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            ctrlResult.result
        }
    }

    /**
     * Takes the given input and processes the change on the selected survey cost items
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> processSurveyCostItemsBulk() {
        Map<String,Object> ctrlResult = surveyControllerService.processSurveyCostItemsBulk(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            redirect(action: 'surveyCostItems', id: ctrlResult.result.surveyInfo.id, params: params+[selectedCostItemElementID: params.bulkSelectedCostItemElementID])
            return
        }


    }

    /**
     *
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    Map<String, Object> processSurveyCostItemsBulkWithUpload() {
        Map<String, Object> ctrlResult = surveyControllerService.processSurveyCostItemsBulkWithUpload(params)
        if (ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        } else {
            flash.error = ctrlResult.result.error

            if(ctrlResult.result.processCount > 0){
                flash.message = message(code: 'surveyCostItems.fileUpload.process', args: [ctrlResult.result.matchCount, ctrlResult.result.processCount, ctrlResult.result.costItemsCreatedCount])
            }

            redirect(url: request.getHeader('referer'))
            return
        }
    }

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def templateForSurveyCostItemsBulkWithUpload() {
        log.debug("templateForSurveyCostItemsBulkWithUpload :: ${params}")
        Map<String, Object> ctrlResult = surveyControllerService.getResultGenericsAndCheckAccess(params)

        String filename = "template_cost_import"

        ArrayList titles = ["LAS:eR-UUID", "WIB-ID", "ISIL", "ROR-ID", "GND-ID", "DEAL-ID"]
        titles.addAll([message(code: 'org.customerIdentifier'),
                       message(code: 'org.sortname.label'), message(code: 'default.name.label'),
                       message(code: 'financials.costItemElement'),
                       message(code: 'default.status.label'),
                       message(code: 'myinst.financeImport.elementSign'),
                       message(code: 'myinst.financeImport.invoiceTotal'), message(code: 'default.currency.label.utf'), message(code: 'myinst.financeImport.taxRate'), message(code: 'myinst.financeImport.taxType'),
                       message(code: 'myinst.financeImport.dateFrom'), message(code: 'myinst.financeImport.dateTo'), message(code: 'myinst.financeImport.title'), message(code: 'myinst.financeImport.description')])

        if(ctrlResult.surveyConfig.packageSurvey){
            titles.add('Anbieter-Produkt-ID')
        }
        ArrayList rowData = []
        ArrayList row
        SurveyOrg.findAllBySurveyConfig(ctrlResult.surveyConfig).each { SurveyOrg surveyOrg ->
            row = []
            Org org = surveyOrg.org
            String wibid = org.getIdentifierByType('wibid')?.value
            String isil = org.getIdentifierByType('ISIL')?.value
            String ror = org.getIdentifierByType('ROR ID')?.value
            String gng = org.getIdentifierByType('gnd_org_nr')?.value
            String deal = org.getIdentifierByType('deal_id')?.value
            row.add(org.globalUID)
            row.add((wibid != IdentifierNamespace.UNKNOWN && wibid != null) ? wibid : '')
            row.add((isil != IdentifierNamespace.UNKNOWN && isil != null) ? isil : '')
            row.add((ror != IdentifierNamespace.UNKNOWN && ror != null) ? ror : '')
            row.add((gng != IdentifierNamespace.UNKNOWN && gng != null) ? gng : '')
            row.add((deal != IdentifierNamespace.UNKNOWN && deal != null) ? deal : '')

            if(ctrlResult.surveyConfig.subscription){
                row.add(Platform.executeQuery('select ci.value from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer = (:customer) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription.instanceOf = :subscription)', [customer: surveyOrg.org, subscription: ctrlResult.surveyConfig.subscription]).join(', '))
            }else{
                row.add('')
            }
            row.add(org.sortname)
            row.add(org.name)
            row.add(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.getI10n('value'))
            row.add(RDStore.COST_ITEM_ACTUAL.getI10n('value'))
            row.add(RDStore.CIEC_POSITIVE.getI10n('value'))
            row.add('')
            row.add('EUR')
            row.add('')
            row.add('')
            row.add('')
            row.add('')
            row.add('')
            row.add('')
            row.add('')
            rowData.add(row)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        out.withWriter { writer ->
            writer.write(exportService.generateSeparatorTableString(titles, rowData, '\t'))
        }
        out.close()
        return

    }


    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def templateForSurveyParticipantsBulkWithUpload() {
        log.debug("templateForSurveyParticipantsBulkWithUpload :: ${params}")
        Map<String, Object> ctrlResult = surveyControllerService.getResultGenericsAndCheckAccess(params)

        String filename = "template_survey_participants_import"

        //params.orgType = RDStore.OT_INSTITUTION.id
        params.customerType = customerTypeService.getOrgInstRoles().id // ERMS-6009
        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        params.sub = ctrlResult.subscription

        FilterService.Result fsr = filterService.getOrgComboQuery(params, ctrlResult.institution as Org)

        List<Org> members = Org.executeQuery(fsr.query, fsr.queryParams, params)

        ArrayList titles = ["LAS:eR-UUID", "WIB-ID", "ISIL", "ROR-ID", "GND-ID", "DEAL-ID", message(code: 'org.sortname.label'), message(code: 'default.name.label'), message(code: 'org.libraryType.label'), message(code: 'surveyconfig.orgs.label')]

        ArrayList rowData = []
        ArrayList row
        members.each { Org org ->
            row = []

            String wibid = org.getIdentifierByType('wibid')?.value
            String isil = org.getIdentifierByType('ISIL')?.value
            String ror = org.getIdentifierByType('ROR ID')?.value
            String gng = org.getIdentifierByType('gnd_org_nr')?.value
            String deal = org.getIdentifierByType('deal_id')?.value
            row.add(org.globalUID)
            row.add((wibid != IdentifierNamespace.UNKNOWN && wibid != null) ? wibid : '')
            row.add((isil != IdentifierNamespace.UNKNOWN && isil != null) ? isil : '')
            row.add((ror != IdentifierNamespace.UNKNOWN && ror != null) ? ror : '')
            row.add((gng != IdentifierNamespace.UNKNOWN && gng != null) ? gng : '')
            row.add((deal != IdentifierNamespace.UNKNOWN && deal != null) ? deal : '')
            row.add(org.sortname)
            row.add(org.name)
            row.add(org.libraryType?.getI10n('value'))

            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(org, ctrlResult.surveyConfig)
            if(surveyOrg){
                row.add(RDStore.YN_YES.getI10n('value'))
            }

            rowData.add(row)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        out.withWriter { writer ->
            writer.write(exportService.generateSeparatorTableString(titles, rowData, '\t'))
        }
        out.close()
        return

    }

    /**
     * Marks the given survey as finished
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyWorkFlowInfos() {
        Map<String,Object> ctrlResult = surveyControllerService.setSurveyWorkFlowInfos(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error)
                flash.error = ctrlResult.result.error

            ctrlResult.result
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Marks the given survey property (i.e. survey question) as mandatory to fill out
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> actionsForSurveyProperty() {
        Map<String,Object> ctrlResult = surveyControllerService.actionsForSurveyProperty(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if(params.actionForSurveyProperty in ['deleteSurveyPropFromConfig', 'moveUp', 'moveDown']) {
                ctrlResult.result.surveyConfig.refresh()

                Map<String, Object> modelMap = [
                        editable                : ctrlResult.result.editable,
                        surveyInfo              : ctrlResult.result.surveyInfo,
                        surveyConfig            : ctrlResult.result.surveyConfig,
                        error                   : ctrlResult.result.error,
                        props_div               : "${params.props_div}", // JS markup id
                ]

                LinkedHashSet<SurveyConfigProperties> surveyProperties = null

                if(params.props_div == 'survey_private_properties'){
                    modelMap.surveyProperties = ctrlResult.result.surveyConfig.getPrivateSurveyConfigProperties()
                    modelMap.selectablePrivateProperties = true
                }
                else if(params.props_div == "survey_grouped_custom_properties_${params.pdg_id}"){
                    PropertyDefinitionGroup pdg = PropertyDefinitionGroup.get(Long.valueOf(params.pdg_id))
                    if(pdg) {
                        modelMap.surveyProperties = ctrlResult.result.surveyConfig.getSurveyConfigPropertiesByPropDefGroup(pdg)
                        modelMap.pdg = pdg
                    }
                }
                else if(params.props_div == 'survey_orphaned_properties'){
                    def allPropDefGroups = ctrlResult.result.surveyConfig.getCalculatedPropDefGroups(ctrlResult.result.surveyInfo.owner)
                    LinkedHashSet groupedProperties = []
                    allPropDefGroups.sorted?.each{ def entry ->
                        PropertyDefinitionGroup pdg = entry[1]
                        groupedProperties <<  ctrlResult.result.surveyConfig.getSurveyConfigPropertiesByPropDefGroup(pdg)
                    }

                    modelMap.surveyProperties = ctrlResult.result.surveyConfig.getOrphanedSurveyConfigProperties(groupedProperties)
                }

                modelMap.showSurveyPropertiesForOwer = true
                render(template: "/templates/survey/properties_table", model: modelMap)
                return
            }
            if(ctrlResult.result.error)
                flash.error = ctrlResult.result.error

            ctrlResult.result
        }

        redirect(url: request.getHeader('referer'))

    }


    /**
     * Marks the given transfer procedure as (un-)checked
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyTransferConfig() {
        Map<String,Object> ctrlResult = surveyControllerService.setSurveyTransferConfig(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error)
                flash.error = ctrlResult.result.error

            ctrlResult.result
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Call to evaluate the given survey's results; the results may be displayed as HTML or
     * exported as (configurable) Excel worksheet
     * @return the survey evaluation view, either as HTML or as (configurable) Excel worksheet
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0, ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyEvaluation() {
        //TODO: TODO MOE 2024
        Map<String,Object> ctrlResult = surveyControllerService.surveyEvaluation(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if (params.fileformat) {
                String message = g.message(code: 'renewalexport.renewals')
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                String datetoday = sdf.format(new Date())

                String filename
                Map<String, Object> selectedFields = [:]
                Set<String> contactSwitch = []
                if (params.filename) {
                    filename = params.filename
                } else {
                    filename = message + "_" + ctrlResult.result.surveyConfig.getSurveyName() + "_${datetoday}"
                }

                Map<String, Object> selectedFieldsRaw = params.findAll { it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put(it.key.replaceFirst('iex:', ''), it.value) }

                contactSwitch.addAll(params.list("contactSwitch"))
                contactSwitch.addAll(params.list("addressSwitch"))

                if (params.fileformat == 'xlsx') {
                    try {
                        SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSurveyEvaluation(ctrlResult.result, selectedFields, contactSwitch, ExportClickMeService.FORMAT.XLS, params.chartFilter)
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
                } else if (params.fileformat == 'csv') {
                    response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportClickMeService.exportSurveyEvaluation(ctrlResult.result, selectedFields, contactSwitch, ExportClickMeService.FORMAT.CSV, params.chartFilter))
                    }
                    out.close()
                }
            } else {
                ctrlResult.result
            }
        }
    }

    /**
     * Call to evaluate the given survey's results; the results may be displayed as HTML or
     * exported as (configurable) Excel worksheet
     * @return the survey evaluation view, either as HTML or as (configurable) Excel worksheet
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyPackagesEvaluation() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyPackagesEvaluation(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveySubscriptionsEvaluation() {
        Map<String,Object> ctrlResult = surveyControllerService.surveySubscriptonsEvaluation(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def surveyVendorsEvaluation() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyVendorsEvaluation(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    /**
     * Call to open the participant transfer view
     * @return the participant list with their selections
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> surveyTransfer() {
        Map<String,Object> ctrlResult = surveyControllerService.surveyTransfer(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Call to transfer the survey participants onto the next year's subscription
     * @return the subscription comparison view for the given subscriptions (the predecessor and the successor instances)
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processTransferParticipants() {
        Map<String,Object> ctrlResult = surveyControllerService.processTransferParticipants(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            redirect(action: 'compareMembersOfTwoSubs', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription?.id])
            return
        }


    }

    /**
     * Call to list the members; either those who completed the survey or those who did not
     * @return a list of the participants in the called tab view
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> openParticipantsAgain() {
        Map<String,Object> ctrlResult = surveyControllerService.openParticipantsAgain(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Call to list the members; either those who completed the survey or those who did not
     * @return a list of the participants in the called tab view
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> participantsReminder() {
        Map<String,Object> ctrlResult = surveyControllerService.participantsReminder(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Reopens the given survey for the given participant
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> actionsForParticipant() {
        Map<String,Object> ctrlResult = surveyControllerService.actionsForParticipant(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

        redirect(url: request.getHeader('referer'))

    }

    /**
     * Evaluates the general selection and the costs of the participant
     * @return the participant evaluation view
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> evaluationParticipant() {
        Map<String,Object> ctrlResult = surveyControllerService.evaluationParticipant(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error){
                flash.error = ctrlResult.result.error
            }
            ctrlResult.result
        }
    }

    /**
     * Generates a summary of the survey in PDF format for the survey participant
     * @return the PDF document to download
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    @Deprecated // Muss überarbeitet werden wegen Umbau der Teilnehmersicht auf die Umfrage
    Map<String,Object> generatePdfForParticipant() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        result.participant = Org.get(params.participant)

        result.surveyResults = []

        result.surveyConfig.getSortedProperties().each{ PropertyDefinition propertyDefinition ->
            result.surveyResults << SurveyResult.findByParticipantAndSurveyConfigAndType(result.participant, result.surveyConfig, propertyDefinition)
        }

        result.ownerId = result.surveyInfo.owner.id

        if(result.surveyConfig.subscription) {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(result.participant)
            result.visibleOrgRelations = []
            if(result.subscription) {
                result.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)

                if (result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                    result.previousSubscription = result.subscription._getCalculatedPreviousForSurvey()

                    /*result.previousIesListPriceSum = 0
                   if(result.previousSubscription){
                       result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                               'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                       [sub: result.previousSubscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                   }*/

                    result.sumListPriceSelectedIEsEUR = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_EUR)
                    result.sumListPriceSelectedIEsUSD = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_USD)
                    result.sumListPriceSelectedIEsGBP = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_GBP)


                    /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                             'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                             [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */

                    result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)
                    if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                        result.countCurrentPermanentTitles = surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(result.subscription, result.surveyConfig)
                    }
                    else {
                        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
                        result.countCurrentPermanentTitles = issueEntitlementGroup ? subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subscriberSub, issueEntitlementGroup) : 0
                    }

/*                    if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                        result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
                    } else {
                        result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
                    }*/
                    result.subscriber = result.participant

                }
            }

            if(result.surveyConfig.subSurveyUseForTransfer) {
                result.successorSubscriptionParent = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                result.subscriptionParent = result.surveyConfig.subscription
                Collection<AbstractPropertyWithCalculatedLastUpdated> props
                props = result.subscriptionParent.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic))}
                if(result.successorSubscriptionParent){
                    props += result.successorSubscriptionParent.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic))}
                }
                result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
            }

            result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
        }

        result.institution = result.participant
        result.ownerView = (contextService.getOrg().id == result.surveyInfo.owner.id)

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()
        String filename

        if (params.filename) {
            filename = sdf.format(new Date()) + '_' + params.filename
        }
        else {
            filename = sdf.format(new Date()) + '_reporting'
        }

        byte[] pdf = PdfUtils.getPdf(result, PdfUtils.PORTRAIT_FIXED_A4, '/survey/export/pdf/participantResult')

        response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
        response.setContentType('application/pdf')
        response.outputStream.withStream { it << pdf }
        return
    }

    /**
     * Call to list all possible survey properties (i.e. the questions which may be asked in a survey)
     * @return a list of properties for the given consortium
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> allSurveyProperties() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        if (!contextService.isInstUser( CustomerTypeService.ORG_CONSORTIUM_PRO )) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.properties = surveyService.getSurveyProperties(result.institution)
        result.language = LocaleUtils.getCurrentLang()

        result
    }


    /**
     * Adds the given institutions to the given survey as new participants
     * @return the updated survey participants list
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> actionSurveyParticipants() {
        Map<String,Object> ctrlResult = surveyControllerService.actionSurveyParticipants(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if(ctrlResult.result.selectMembersWithImport){
                if(ctrlResult.result.selectMembersWithImport.truncatedRows){
                    flash.message = message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.selectProcess.truncatedRows', args: [ctrlResult.result.selectMembersWithImport.processCount, ctrlResult.result.selectMembersWithImport.processRow, ctrlResult.result.selectMembersWithImport.wrongOrgs, ctrlResult.result.selectMembersWithImport.truncatedRows])
                }else if(ctrlResult.result.selectMembersWithImport.wrongOrgs){
                    flash.message = message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.selectProcess.wrongOrgs', args: [ctrlResult.result.selectMembersWithImport.processCount, ctrlResult.result.selectMembersWithImport.processRow, ctrlResult.result.selectMembersWithImport.wrongOrgs])
                }
                else {
                    flash.message = message(code: 'surveyParticipants.addParticipants.option.selectMembersWithFile.selectProcess', args: [ctrlResult.result.selectMembersWithImport.processCount, ctrlResult.result.selectMembersWithImport.processRow])
                }
            }

            ctrlResult.result
            redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID, tab: params.tab]
            return
        }



    }


    /**
     * Call to delete the given documents from the survey
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteDocuments() {
        log.debug("deleteDocuments ${params}")
        if(params.instanceId){
            params.surveyConfigID = params.instanceId
            SurveyConfig surveyConfig = SurveyConfig.get(params.long('surveyConfigID'))
            if(surveyConfig) {
                params.id = surveyConfig.surveyInfo.id
            }
        }
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        if(result.editable) {
            docstoreService.deleteDocument(params)
        }

        redirect(uri: request.getHeader('referer'))
    }

    /**
     * Deletes the entire survey with attached objects
     * @return the survey list in case of success, a redirect to the referer otherwise
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> deleteSurveyInfo() {
        Map<String,Object> ctrlResult = surveyControllerService.deleteSurveyInfo(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.message) {
                flash.message = ctrlResult.result.message

                redirect action: 'currentSurveysConsortia'
                return
            }
            else if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error

                redirect(uri: request.getHeader('referer'))
                return
            }else{
                redirect(uri: request.getHeader('referer'))
                return
            }
        }

    }

    /**
     * Marks the survey as being in evaluation
     * @return redirects to the renewal evaluation view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setStatus() {
        Map<String,Object> ctrlResult = surveyControllerService.setStatus(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            if(ctrlResult.result.error)
            flash.error = ctrlResult.result.error

            if(ctrlResult.result.message)
                flash.message = ctrlResult.result.message

            ctrlResult.result
        }

        if(params.newStatus == 'setInEvaluation') {
            redirect action: 'renewalEvaluation', params: [surveyConfigID: ctrlResult.result.surveyConfig.id, id: ctrlResult.result.surveyInfo.id]
            return
        }else if(params.newStatus == 'processEndSurvey' && ctrlResult.result.surveyConfig && ctrlResult.result.surveyConfig.subSurveyUseForTransfer) {
            redirect action: 'renewalEvaluation', params: [surveyConfigID: ctrlResult.result.surveyConfig.id, id: ctrlResult.result.surveyInfo.id]
            return
        }else {
            redirect(url: request.getHeader('referer'))
        }

    }


    /**
     * Sets the given comment for the given survey
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> setSurveyConfigComment() {
        Map<String,Object> ctrlResult = surveyControllerService.setSurveyConfigComment(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error){
                flash.error = ctrlResult.result.error
            }

            ctrlResult.result
            redirect(action: 'show', params: [id: params.id, surveyConfigID: ctrlResult.result.surveyConfig.id, commentTab: params.commentTab])
            return
        }



    }

    /**
     * Call to load the evaluation of the renewal process. The evaluation data may be exported as (configurable)
     * Excel worksheet
     * @return the evaluation view for the given renewal survey
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> renewalEvaluation() {
        //TODO: MOE 2024
        Map<String,Object> ctrlResult = surveyControllerService.renewalEvaluation(params)
        if (ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            response.sendError(401)
                return
        }
        else {
            String message = g.message(code: 'renewalexport.renewals')
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            String datetoday = sdf.format(new Date())

            String filename
            Map<String, Object> selectedFields = [:]
            Set<String> contactSwitch = []
            if(params.fileformat) {
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
                if (params.filename) {
                    filename =params.filename
                }
                else {
                    filename = message + "_" + ctrlResult.result.surveyConfig.getSurveyName() + "_${datetoday}"
                }
                contactSwitch.addAll(params.list("contactSwitch"))
                contactSwitch.addAll(params.list("addressSwitch"))
            }


            if (params.fileformat == 'xlsx') {
                try {
                    SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportRenewalResult(ctrlResult.result, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)
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
                    writer.write((String) exportClickMeService.exportRenewalResult(ctrlResult.result, selectedFields, ExportClickMeService.FORMAT.CSV, contactSwitch))
                }
                out.close()
            }
            else
                ctrlResult.result
        }
    }

    /**
     * Call to copy the given survey
     * @return the view with the base parameters for the survey copy
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copySurvey() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurvey(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Takes the submitted base parameters and creates a copy of the given survey
     * @return either the survey list view for consortia or the survey details view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processCopySurvey() {
        Map<String,Object> ctrlResult = surveyControllerService.processCopySurvey(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {

            ctrlResult.result

            if(ctrlResult.result.targetSubs) {
                redirect controller: 'survey', action: 'currentSurveysConsortia', params: [validOnYear: 'all' ,ids: ctrlResult.result.newSurveyIds]
                return
            }else {
                redirect controller: 'survey', action: 'show', params: [id: ctrlResult.result.newSurveyInfo.id, surveyConfigID: ctrlResult.result.newSurveyConfig.id]
                return
            }
        }



    }

    /**
     * Initialises the subscription renewal for the parent subscription after a survey
     * @return the view for the successor subscription base parameter's configuration
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> renewSubscriptionConsortiaWithSurvey() {
        Map<String,Object> ctrlResult = surveyControllerService.renewSubscriptionConsortiaWithSurvey(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    /**
     * Takes the submitted input and creates a successor subscription instance. The successor is being automatically
     * linked to the predecessor instance. The element copy workflow is triggered right after
     * @return the subscription element copy starting view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     def processRenewalWithSurvey() {
        Map<String,Object> ctrlResult = surveyControllerService.processRenewalWithSurvey(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
                redirect(url: request.getHeader('referer'))
                return
            }else {
                redirect controller: 'subscription', action: 'copyElementsIntoSubscription', params: [sourceObjectId: genericOIDService.getOID(Subscription.get(params.sourceSubId)), targetObjectId: genericOIDService.getOID(ctrlResult.result.newSub), isRenewSub: true, fromSurvey: ctrlResult.result.surveyConfig.id]
                return
            }
        }
    }

    /**
     * Exports the survey costs in an Excel worksheet
     * @return an Excel worksheet containing the survey cost data
     */
    @DebugInfo(isInstUser_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
     def exportSurCostItems() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }

        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String datetoday = sdf.format(new Date())
        String filename = "${datetoday}_" + g.message(code: "survey.exportSurveyCostItems")

        if(params.fileformat == 'xlsx') {
            if (params.filename) {
                filename =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            Set<String> contactSwitch = []
            contactSwitch.addAll(params.list("contactSwitch"))
            contactSwitch.addAll(params.list("addressSwitch"))
            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportSurveyCostItemsForOwner(result.surveyConfig, selectedFields, ExportClickMeService.FORMAT.XLS, contactSwitch)

            response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }else {
            redirect(uri: request.getHeader('referer'))
        }
    }

    /**
     * Takes the submitted input and creates cost items based on the given parameters for every selected survey participant
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
     Map<String,Object> createSurveyCostItem() {
        Map<String,Object> ctrlResult = surveyControllerService.createSurveyCostItem(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
            }
            ctrlResult.result
        }
        redirect(uri: request.getHeader('referer'))
    }

    /**
     * Call for the transfer view of the participants in a renewal survey
     * @return a list of members for each subscription
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> compareMembersOfTwoSubs() {
        Map<String,Object> ctrlResult = surveyControllerService.compareMembersOfTwoSubs(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Call to copy the packages of subscription
     * @return a list of each participant's packages
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copySubPackagesAndIes() {
        Map<String,Object> ctrlResult = surveyControllerService.copySubPackagesAndIes(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copySurveyPackages() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurveyPackages(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copySurveyVendors() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurveyVendors(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> proccessCopySubPackagesAndIes() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySubPackagesAndIes(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            redirect(action: 'copySubPackagesAndIes', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription.id])
            return
        }

    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> proccessCopySurveyPackages() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySurveyPackages(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            redirect(action: 'copySurveyPackages', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription.id])
            return
        }

    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> proccessCopySurveyVendors() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySurveyVendors(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            redirect(action: 'copySurveyVendors', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription.id])
            return
        }

    }

        /**
     * Call to copy the survey cost items
     * @return a list of each participant's survey costs
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copySurveyCostItems() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurveyCostItems(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copySurveyCostItemPackage() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurveyCostItemPackage(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }
    }

    /**
     * Takes the given parameters and creates copies of the given cost items, based on the submitted data
     * @return the survey cost copy overview
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopySurveyCostItems() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySurveyCostItems(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            flash.message = message(code: 'copySurveyCostItems.copy.success', args: [ctrlResult.result.countNewCostItems]) as String
            ctrlResult.result
            redirect(action: 'copySurveyCostItems', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription.id])
            return
        }

        redirect(uri: request.getHeader('referer'))

    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> proccessCopySurveyCostItemPackage() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySurveyCostItemPackage(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            flash.message = message(code: 'copySurveyCostItems.copy.success', args: [ctrlResult.result.countNewCostItems]) as String
            ctrlResult.result
            redirect(action: 'copySurveyCostItems', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription.id])
            return
        }

        redirect(uri: request.getHeader('referer'))

    }

    /**
     * Call to open the transfer of survey cost items into the respective member subscriptions
     * @return a list of participants with their respective survey cost items
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copySurveyCostItemsToSub() {
        Map<String,Object> ctrlResult = surveyControllerService.copySurveyCostItemsToSub(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Takes the submitted parameters and copies the survey cost items into the subscriptions
     * @return the survey-subscription cost transfer view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopySurveyCostItemsToSub() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopySurveyCostItemsToSub(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            flash.message = message(code: 'copySurveyCostItems.copy.success', args: [ctrlResult.result.countNewCostItems]) as String
            ctrlResult.result
            redirect(action: 'copySurveyCostItemsToSub', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id])
            return
        }



    }

    /**
     * Call to open the property copying view from one subscription into another
     * @return the list of properties for each year ring
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copyProperties() {
        Map<String,Object> ctrlResult = surveyControllerService.copyProperties(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
        }

    }

    /**
     * Takes the submitted data and creates copies of the selected properties into the successor subscriptions
     * @return the property copy overview
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> proccessCopyProperties() {
        Map<String,Object> ctrlResult = surveyControllerService.proccessCopyProperties(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            if(ctrlResult.result.message) {
                flash.message = ctrlResult.result.message
            }

            ctrlResult.result

            redirect(action: 'copyProperties', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, tab: params.tab, selectedProperty: params.selectedProperty, targetSubscriptionId: ctrlResult.result.targetSubscription?.id])
            return
        }



    }

    /**
     * Takes the given members and processes their renewal into the next year, i.e. creates new subscription instances for
     * the following year along with their depending data
     * @return a redirect to the member comparison view
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> processTransferParticipantsByRenewal() {
        Map<String,Object> ctrlResult = surveyControllerService.processTransferParticipantsByRenewal(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            flash.message = message(code: 'surveyInfo.transfer.info', args: [ctrlResult.result.countNewSubs, ctrlResult.result.newSubs.size() ?: 0]) as String

            redirect(action: 'compareMembersOfTwoSubs', id: params.id, params: [surveyConfigID: ctrlResult.result.surveyConfig.id, targetSubscriptionId: ctrlResult.result.targetSubscription?.id])
            return
        }



    }

    /**
     * Call for the given / next survey element copy procedure step. If data is submitted,
     * the call will process copying of the given survey elements
     * @return the given tab with the copy parameters
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
     Map<String,Object> copyElementsIntoSurvey() {
        def result             = [:]
        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()

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

        result.allObjects_readRights = SurveyConfig.executeQuery("select surConfig from SurveyConfig as surConfig join surConfig.surveyInfo as surInfo where surInfo.owner = :contextOrg order by surInfo.name", [contextOrg: contextService.getOrg()])
        //Nur Umfragen, die noch in Bearbeitung sind da sonst Umfragen-Prozesse zerstört werden.
        result.allObjects_writeRights = SurveyConfig.executeQuery("select surConfig from SurveyConfig as surConfig join surConfig.surveyInfo as surInfo where surInfo.owner = :contextOrg and surInfo.status = :status order by surInfo.name", [contextOrg: contextService.getOrg(), status: RDStore.SURVEY_IN_PROCESSING])

        switch (params.workFlowPart) {
            case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                break
            case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copyElementsService.copyObjectElements_DocsTasksWorkflows(params)
                result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.ORG_CONSORTIUM_PRO)
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    def notes() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        result
    }

    /**
     * Set link to license or provider
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setProviderOrLicenseLink() {
        Map<String,Object> ctrlResult = surveyControllerService.setProviderOrLicenseLink(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
            }
        }

        redirect(uri: request.getHeader('referer'))

    }

    /**
     * Set link to another survey
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> setSurveyLink() {
        Map<String,Object> ctrlResult = surveyControllerService.setSurveyLink(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
            }
        }

        redirect(uri: request.getHeader('referer'))

    }

    /**
     * Adds an information link to the survey; above the pure URL also further comments accompanying the survey
     * @return a redirect to the referer
     * @see SurveyUrl
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> addSurveyUrl() {
        Map<String,Object> ctrlResult = surveyControllerService.addSurveyUrl(params)
        if(ctrlResult.status == SurveyControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }else {
            ctrlResult.result
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
            }
        }

        redirect(uri: request.getHeader('referer'))

    }

    //---------------------------------------------RENDER TEMPLATES--------------------------------------------------------------------------------------------

    /**
     * Call to copy the mail addresses of all participants
     * @return the modal containing the participant's mail addresses
     */
    @DebugInfo(isInstUser_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    Map<String,Object> copyEmailaddresses() {
        Map<String, Object> result = [:]
        result.modalID = params.targetId
        result.orgList = []

        if (params.get('orgListIDs')) {
            List<Long> idList = Params.getLongList_forCommaSeparatedString(params, 'orgListIDs')
            result.orgList = idList.isEmpty() ? [] : Org.findAllByIdInList(idList)
        }

        render(template: "/templates/copyEmailaddresses", model: result)
    }

    @DebugInfo(isInstEditor_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    Map<String,Object> openTransferParticipantsModal() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }

        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result = surveyControllerService.getSubResultForTranfser(result, params)

        result.superOrgType = []
        if(contextService.getOrg().isCustomerType_Consortium_Pro()) {
            result.superOrgType << message(code:'consortium.superOrgType')
        }

        if(result.surveyConfig.subSurveyUseForTransfer && result.parentSuccessorSubscription) {
            String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
            result.memberLicenses = License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE])
        }

        render(template: 'transferParticipantsModal', model: result)
    }

    /**
     * Call to edit the given survey cost item
     * @return the cost item editing modal
     */
    @DebugInfo(isInstEditor_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    Map<String,Object> editSurveyCostItem() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        result.putAll(financeControllerService.getEditVars(result.institution))
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }
        result.costItem = params.costItem ? CostItem.findById(params.costItem) : null


        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }

        result.orgConfigurations = orgConfigurations as JSON
        //result.selectedCostItemElementID = params.selectedCostItemElementID ? Long.valueOf(params.selectedCostItemElementID) : RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id

        result.participant = Org.get(params.participant)
        result.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, result.participant)


        result.mode = result.costItem ? "edit" : ""
        result.selectedCostItemElementID = params.selectedCostItemElementID ? Long.valueOf(params.selectedCostItemElementID) : null
        result.selectedPackageID = params.selectedPackageID ? Long.valueOf(params.selectedPackageID) : null
        if(params.selectPkg == "true"){
            result.selectPkg = params.selectPkg
        }

        result.selectedSurveyConfigSubscriptionID = params.selectedSurveyConfigSubscriptionID ? Long.valueOf(params.selectedSurveyConfigSubscriptionID) : null
        if(params.selectSubscription == "true"){
            result.selectSubscription = params.selectSubscription
        }
        result.taxKey = result.costItem ? result.costItem.taxKey : null
        result.idSuffix = "edit_${result.costItem ? result.costItem.id : result.participant.id}"
        result.modalID = 'surveyCostItemModal'
        render(template: "/survey/costItemModal", model: result)
    }

    /**
     * Call to add a new survey cost item to every participant
     * @return the new cost item modal
     */
    @DebugInfo(isInstEditor_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    Object addForAllSurveyCostItem() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
        if (!result.editable) {
            response.sendError(HttpStatus.SC_FORBIDDEN); return
        }

        result.putAll(financeControllerService.getEditVars(result.institution))

        Map<Long,Object> orgConfigurations = [:]
        result.costItemElements.each { oc ->
            orgConfigurations.put(oc.costItemElement.id,oc.elementSign.id)
        }

        result.orgConfigurations = orgConfigurations as JSON
        //result.selectedCostItemElementID = params.selectedCostItemElementID ? Long.valueOf(params.selectedCostItemElementID) : RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id

        result.setting = 'bulkForAll'

        result.surveyOrgList = []

        if (params.get('orgsIDs')) {
            List<Long> idList = Params.getLongList_forCommaSeparatedString(params, 'orgsIDs')
            List<Org> orgList = Org.findAllByIdInList(idList)
            result.surveyOrgList = orgList.isEmpty() ? [] : SurveyOrg.findAllByOrgInListAndSurveyConfig(orgList, result.surveyConfig)
        }

        if(params.selectPkg == "true"){
            result.selectPkg = params.selectPkg
        }

        result.selectedCostItemElementID = params.selectedCostItemElementID ? Long.valueOf(params.selectedCostItemElementID) : null
        result.selectedPackageID = params.selectedPackageID ? Long.valueOf(params.selectedPackageID) : null

        result.selectedSurveyConfigSubscriptionID = params.selectedSurveyConfigSubscriptionID ? Long.valueOf(params.selectedSurveyConfigSubscriptionID) : null
        if(params.selectSubscription == "true"){
            result.selectSubscription = params.selectSubscription
        }

        result.modalID = 'addForAllSurveyCostItem'
        result.idSuffix = 'addForAllSurveyCostItem'
        render(template: "/survey/costItemModal", model: result)
    }

    /**
     * Call to show the differences between the respective institution's choices and the underlying subscription data
     * @return a modal to show the differences between this and next year ring's subscription parameters (= the selected
     * parameters by each member)
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> showPropertiesChanged() {
        Map<String,Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if(result.status == SurveyControllerService.STATUS_ERROR) {
            if (!result.result) {
                response.sendError(401)
                return
            }
        }
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
            SXSSFWorkbook wb = (SXSSFWorkbook) surveyService.exportPropertiesChanged(result.surveyConfig, result.participants, contextService.getOrg())
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
                    SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, result.propertyDefinition, result.surveyConfig, contextService.getOrg())
                    SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, contextService.getOrg())

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
}
