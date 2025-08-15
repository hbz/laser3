package de.laser.ajax

import de.laser.AlternativeName
import de.laser.CacheService
import de.laser.ControlledListService
import de.laser.CustomerIdentifier
import de.laser.CustomerTypeService
import de.laser.DiscoverySystemFrontend
import de.laser.DiscoverySystemIndex
import de.laser.DocContext
import de.laser.EscapeService
import de.laser.ExportClickMeService
import de.laser.ExportService
import de.laser.FileCryptService
import de.laser.GenericOIDService
import de.laser.GlobalService
import de.laser.HelpService
import de.laser.IssueEntitlementGroup
import de.laser.IssueEntitlementService
import de.laser.OrgSetting
import de.laser.PendingChangeService
import de.laser.AddressbookService
import de.laser.AccessService
import de.laser.PropertyService
import de.laser.SubscriptionService
import de.laser.StatsSyncService
import de.laser.SurveyService
import de.laser.WekbNewsService
import de.laser.WorkflowService
import de.laser.base.AbstractReport
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.ContextService
import de.laser.GokbService
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.remote.Wekb
import de.laser.survey.SurveyPersonResult
import de.laser.survey.SurveyVendorResult
import de.laser.utils.CodeUtils
import de.laser.utils.LocaleUtils
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.ReportingFilter
import de.laser.ReportingGlobalService
import de.laser.ReportingLocalService
import de.laser.Subscription
import de.laser.addressbook.Address
import de.laser.Doc
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.SubscriptionPackage
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.Task
import de.laser.TaskService
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.UserSetting
import de.laser.wekb.Vendor
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.ctrl.MyInstitutionControllerService
import de.laser.custom.CustomWkhtmltoxService
import de.laser.utils.DateUtils
import de.laser.utils.SwissKnife
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.DetailsExportManager
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.reporting.export.QueryExportManager
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint
import de.laser.workflow.WorkflowHelper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import groovy.xml.StreamingMarkupBuilder
import io.micronaut.http.HttpStatus
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.mozilla.universalchardet.UniversalDetector

import javax.servlet.ServletOutputStream
import java.math.RoundingMode
import java.nio.charset.Charset

/**
 * This controller manages HTML fragment rendering calls; object manipulation is done in the AjaxController!
 * For JSON rendering, see AjaxJsonController.
 * IMPORTANT: Only template rendering here, no object manipulation!
 * @see AjaxController
 * @see AjaxJsonController
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxHtmlController {

    CacheService cacheService
    ContextService contextService
    ControlledListService controlledListService
    CustomerTypeService customerTypeService
    CustomWkhtmltoxService wkhtmltoxService // custom
    EscapeService escapeService
    ExportService exportService
    ExportClickMeService exportClickMeService
    FileCryptService fileCryptService
    GenericOIDService genericOIDService
    GokbService gokbService
    HelpService helpService
    IssueEntitlementService issueEntitlementService
    LinksGenerationService linksGenerationService
    MyInstitutionControllerService myInstitutionControllerService
    PendingChangeService pendingChangeService
    ReportingGlobalService reportingGlobalService
    ReportingLocalService reportingLocalService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionService subscriptionService
    TaskService taskService
    AccessService accessService
    PropertyService propertyService
    StatsSyncService statsSyncService
    SurveyService surveyService
    WekbNewsService wekbNewsService
    WorkflowService workflowService

    /**
     * Test render call
     * @return a paragraph with sample text
     */
    @Secured(['ROLE_USER'])
    def test() {
        String result = '<p data-status="ok">OK'
        if (params.id) {
            result += ', ID:' + params.id
        }
        result += '</p>'
        render result
    }

    /**
     * Adds a new object stub to an xEditable enumeration.
     * Currently supported are {@link AlternativeName}s (params.object == "altname") or {@link de.laser.finance.PriceItem}s (params.object == "priceItem")
     * @return a template fragment for the new xEditable item
     */
    @Secured(['ROLE_USER'])
    def addObject() {
        def resultObj, owner = genericOIDService.resolveOID(params.owner)
        switch(params.object) {
            case "altname": Map<String, Object> config = [name: 'Unknown']
                if(owner instanceof License)
                    config.license = owner
                else if(owner instanceof Org)
                    config.org = owner
                else if(owner instanceof Provider)
                    config.provider = owner
                else if(owner instanceof Subscription)
                    config.subscription = owner
                else if(owner instanceof Vendor)
                    config.vendor = owner
                resultObj = AlternativeName.construct(config)
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, showConsortiaFunctions: contextService.getOrg().isCustomerType_Consortium(), ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "name", overwriteEditable: true]
                }
                break
            case "frontend":
                resultObj = new DiscoverySystemFrontend([org: owner, frontend: RDStore.GENERIC_NULL_VALUE]).save()
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "frontend", config: RDConstants.DISCOVERY_SYSTEM_FRONTEND, overwriteEditable: true]
                }
                break
            case "index":
                resultObj = new DiscoverySystemIndex([org: owner, index: RDStore.GENERIC_NULL_VALUE]).save()
                if(resultObj) {
                    render template: '/templates/ajax/newXEditable', model: [wrapper: params.object, ownObj: resultObj, objOID: genericOIDService.getOID(resultObj), field: "index", config: RDConstants.DISCOVERY_SYSTEM_INDEX, overwriteEditable: true]
                }
                break
            case "coverage":
                Map<String, Object> ctrlResult = subscriptionControllerService.addCoverage(params)
                if(ctrlResult.status == SubscriptionControllerService.STATUS_OK) {
                    render template: '/templates/tipps/coverages_accordion', model: [covStmt: ctrlResult.result.covStmt, tipp: ctrlResult.result.tipp, showEmbargo: true, objectTypeIsIE: true, overwriteEditable: true, counterCoverage: ctrlResult.result.counterCoverage] //editable check is implicitly done by call; the AJAX loading can be triggered iff editable == true
                }
                break
            case "priceItem":
                Map<String, Object> ctrlResult = subscriptionControllerService.addEmptyPriceItem(params)
                if(ctrlResult.status == SubscriptionControllerService.STATUS_OK) {
                    render template: '/templates/tipps/priceItem', model: [priceItem: ctrlResult.result.newItem, editable: true] //editable check is implicitly done by call; the AJAX loading can be triggered iff editable == true
                }
                break
        }
    }

    //-------------------------------------------------- myInstitution/dashboard ---------------------------------------

    /**
     * Loads the pending changes for the dashboard. Is still subject of refactoring as the assembly of the relevant data still takes very long
     * @return the accepted and pending changes tab fragment for the dashboard
     */
    @Secured(['ROLE_USER'])
    def getChanges() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
        int periodInDays = result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, 14)
        Map<String, Object> pendingChangeConfigMap = [
                contextOrg: contextService.getOrg(),
                consortialView: contextService.getOrg().isCustomerType_Consortium(),
                periodInDays: periodInDays
        ]
        Map<String, Object> changes = pendingChangeService.getSubscriptionChanges(pendingChangeConfigMap)
        changes.editable = result.editable
        changes.periodInDays = periodInDays
        render template: '/myInstitution/changesWrapper', model: changes
    }

    /**
     * Loads the survey tab for the dashboard, containing current surveys
     * @return the survey tab fragment for the dashboard
     */
    @Secured(['ROLE_USER'])
    def getSurveys() {
        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
//        SwissKnife.setPaginationParams(result, params, (User) result.user)
        List activeSurveyConfigs = SurveyConfig.executeQuery(
                "from SurveyConfig surConfig where exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig" +
                " AND surOrg.org = :org and surOrg.finishDate is null" +
                " AND surConfig.surveyInfo.status = :status) " +
                " order by surConfig.surveyInfo.endDate",
                [org: contextService.getOrg(), status: RDStore.SURVEY_SURVEY_STARTED])

        if (contextService.getOrg().isCustomerType_Consortium_Pro()){
            activeSurveyConfigs = SurveyConfig.executeQuery(
                    "from SurveyConfig surConfig where surConfig.surveyInfo.status = :status and surConfig.surveyInfo.owner = :org" +
                    " order by surConfig.surveyInfo.endDate",
                    [org: contextService.getOrg(), status: RDStore.SURVEY_SURVEY_STARTED])
        }

        if (result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_SURVEYS_MANDATORY_ONLY, RDStore.YN_NO) == RDStore.YN_YES) {
            result.surveysCount = activeSurveyConfigs.groupBy{ it?.id }.size()
            result.surveys      = activeSurveyConfigs.findAll{ it?.surveyInfo?.isMandatory == true }.groupBy{ it?.id }
        }
        else {
            result.surveys      = activeSurveyConfigs.groupBy{ it?.id }
            result.surveysCount = result.surveys.size()
        }
//        result.surveys = result.surveys.drop((int) result.offset).take((int) result.max)
//        result.surveysOffset = result.offset

        render template: '/myInstitution/surveys', model: result
    }



    @Secured(['ROLE_USER'])
    def checkCounterAPIConnection() {
        EhcacheWrapper userCache = contextService.getUserCache("/subscription/checkCounterAPIConnection")
        userCache.put('progress', 0)
        userCache.put('label', 'Bereite Überprüfung vor ...')
        Map<String, Object> result = [:]
        Subscription subscription = Subscription.get(params.id)
        Map allPlatforms = gokbService.executeQuery(Wekb.getSushiSourcesURL(), [:])
        List errorRows = []
        Set<String> titleRow = ['Customer Key', 'Requestor ID', 'Error']
        Set<Platform> subscribedPlatforms = Platform.executeQuery('select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :sub', [sub: subscription])
        if (allPlatforms) {
            double platPercentage = (1/subscribedPlatforms.size())*100
            Calendar start = GregorianCalendar.getInstance(), end = GregorianCalendar.getInstance()
            start.set(Calendar.MONTH, 0)
            start.set(Calendar.DAY_OF_MONTH, 1)
            end.set(Calendar.MONTH, 0)
            end.set(Calendar.DAY_OF_MONTH, 31)
            Date startDate = start.getTime(), endDate = end.getTime()
            subscribedPlatforms.eachWithIndex { Platform plat, int p ->
                Map platformRecord
                Map<String, String> statsSource
                String defaultReport = null
                if(allPlatforms.counter4ApiSources.containsKey(plat.gokbId)) {
                    platformRecord = allPlatforms.counter4ApiSources.get(plat.gokbId)
                    statsSource = exportService.prepareSushiCall(platformRecord)
                }
                else if(allPlatforms.counter5ApiSources.containsKey(plat.gokbId)) {
                    platformRecord = allPlatforms.counter5ApiSources.get(plat.gokbId)
                    statsSource = exportService.prepareSushiCall(platformRecord)
                    Map<String, Object> availableReports = statsSyncService.fetchJSONData(statsSource.statsUrl, null, true)
                    if(availableReports.containsKey('list'))
                        defaultReport = "/"+availableReports.list[0]["Report_ID"].toLowerCase()
                    else {
                        defaultReport = "/pr_p1"
                        //ugly and temp fix for HERDT which as sole provider does not support platform reports yet ...
                        if(plat.gokbId == "f4f4f5d6-9f8c-49cc-b47f-54fadee1e0d8")
                            defaultReport = "/tr"
                    }
                }
                Set<CustomerIdentifier> memberCustomerIdentifiers = CustomerIdentifier.executeQuery('select ci from CustomerIdentifier ci, OrgRole oo join oo.org org join oo.sub sub where ci.customer = org and sub.instanceOf = :parent and oo.roleType = :subscrRole and ci.platform = :platform', [parent: subscription, subscrRole: RDStore.OR_SUBSCRIBER_CONS, platform: plat])
                memberCustomerIdentifiers.eachWithIndex { CustomerIdentifier customerId, int i ->
                    userCache.put('label', "Überprüfe Kundennummer ${customerId.value}/${customerId.requestorKey} von ${customerId.customer.sortname}")
                    Map checkResult = [:]
                    if(statsSource.revision == AbstractReport.COUNTER_4) {
                        StreamingMarkupBuilder requestBuilder = new StreamingMarkupBuilder()
                        Date now = new Date()
                        def requestBody = requestBuilder.bind {
                            mkp.xmlDeclaration()
                            mkp.declareNamespace(x: "http://schemas.xmlsoap.org/soap/envelope/")
                            mkp.declareNamespace(cou: "http://www.niso.org/schemas/sushi/counter")
                            mkp.declareNamespace(sus: "http://www.niso.org/schemas/sushi")
                            x.Envelope {
                                x.Header {}
                                x.Body {
                                    cou.ReportRequest(Created: DateUtils.getSDF_yyyyMMddTHHmmss().format(now), ID: '?') {
                                        sus.Requestor {
                                            sus.ID(customerId.requestorKey)
                                            sus.Name('?')
                                            sus.Email('?')
                                        }
                                        sus.CustomerReference {
                                            sus.ID(customerId.value)
                                            sus.Name('?')
                                        }
                                        sus.ReportDefinition(Name: 'PR1', Release: 4) {
                                            sus.Filters {
                                                sus.UsageDateRange {
                                                    sus.Begin(DateUtils.getSDF_yyyyMMdd().format(startDate))
                                                    //if (currentYearEnd.before(calendarConfig.now))
                                                    sus.End(DateUtils.getSDF_yyyyMMdd().format(endDate))
                                                    /*else {
                                                        sus.End(calendarConfig.now.format("yyyy-MM-dd"))
                                                    }*/
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        checkResult = statsSyncService.fetchXMLData(statsSource.statsUrl, customerId, requestBody)
                    }
                    else if(statsSource.revision == AbstractReport.COUNTER_5) {
                        String url = statsSource.statsUrl + defaultReport
                        url += exportService.buildQueryArguments([revision: AbstractReport.COUNTER_5], platformRecord, customerId)
                        if(platformRecord.counterR5SushiPlatform)
                            url += "&platform=${platformRecord.counterR5SushiPlatform}"
                        url += "&begin_date=${DateUtils.getSDF_yyyyMM().format(startDate)}&end_date=${DateUtils.getSDF_yyyyMM().format(endDate)}"
                        checkResult = statsSyncService.fetchJSONData(url, customerId)
                    }
                    if(checkResult.containsKey('error')) {
                        if(checkResult.error && !(checkResult.error in [3030, 3031, 3060]) && !(checkResult.error instanceof HttpStatus && checkResult.error.code == 202)) {
                            List errorRow = []
                            errorRow << exportClickMeService.createTableCell(ExportClickMeService.FORMAT.XLS, customerId.value)
                            errorRow << exportClickMeService.createTableCell(ExportClickMeService.FORMAT.XLS, customerId.requestorKey)
                            String errMess = message(code: "default.stats.error.${checkResult.error}")
                            if(errMess.contains('default.stats.error'))
                                errMess = checkResult.error
                            errorRow << exportClickMeService.createTableCell(ExportClickMeService.FORMAT.XLS, errMess)
                            errorRows << errorRow
                        }
                    }
                    userCache.put('progress', (p*platPercentage)+(i/memberCustomerIdentifiers.size())*100)
                }
            }
            if(errorRows) {
                String dir = GlobalService.obtainTmpFileLocation()
                File f = new File(dir+"/${escapeService.escapeString(subscription.name)}_counterAPIErrors")
                result.token = "${escapeService.escapeString(subscription.name)}_counterAPIErrors"
                FileOutputStream fos = new FileOutputStream(f)
                Map sheetData = [:]
                sheetData.put(message(code: 'myinst.financeImport.post.error.matchingErrors.sheetName'), [titleRow: titleRow, columnData: errorRows])
                SXSSFWorkbook wb = (SXSSFWorkbook) exportService.generateXLSXWorkbook(sheetData)
                wb.write(fos)
                fos.flush()
                fos.close()
                wb.dispose()
            }
            else result.success = true
        }
        else {
            result.wekbUnavailable = true
        }
        userCache.put('progress', 100)
        render template: '/templates/stats/counterCheckResult', model: result
    }

    /**
     * Call to render the flyout to display recent changes in the we:kb knowledge base
     * @return the template fragment for the changes
     */
    @Secured(['ROLE_USER'])
    def wekbNewsFlyout() {
        log.debug('ajaxHtmlController.wekbNewsFlyout ' + params)

        Map<String, Object> result = myInstitutionControllerService.getResultGenerics(null, params)
        result.wekbNews = wekbNewsService.getCurrentNews()
        result.tmplView = 'details'

        render template: '/myInstitution/dashboard/wekbNews', model: result
    }

    //-------------------------------------------------- subscription/show ---------------------------------------------

    /**
     * Gets the subscription and license links for the given subscription or license
     * @return the fragment listing the links going out from the given object
     */
    @Secured(['ROLE_USER'])
    def getLinks() {
        Map<String, Object> result = [user:contextService.getUser(), contextOrg:contextService.getOrg(), subscriptionLicenseLink:params.subscriptionLicenseLink]

        def entry = genericOIDService.resolveOID(params.entry)
        result.entry = entry
        result.editable = entry.isEditableBy(result.user)
        if(entry instanceof Subscription) {
            result.subscription = (Subscription) entry
            result.atConsortialParent = contextService.getOrg().id == result.subscription.getConsortium()?.id && !result.subscription.instanceOf ? "true" : "false"
        }
        else if(entry instanceof License) {
            result.license = (License) entry
            result.atConsortialParent = contextService.getOrg() == result.license.getLicensingConsortium() && !result.license.instanceOf ? "true" : "false"
        }
        List<RefdataValue> linkTypes = RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)
        if(result.subscriptionLicenseLink) {
            linkTypes.removeIf({ RefdataValue rdv -> (rdv != RDStore.LINKTYPE_LICENSE) })
        }
        else linkTypes.remove(RDStore.LINKTYPE_LICENSE)
        result.links = linksGenerationService.getSourcesAndDestinations(entry, result.user, linkTypes)
        render template: '/templates/links/linksListing', model: result
    }

    /**
     * Gets the data of the packages linked to the given subscription
     * @return the package details fragment
     */
    @Secured(['ROLE_USER'])
    def getPackageData() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription), curatoryGroups: []], packageMetadata = [:]

        result.contextCustomerType = contextService.getOrg().getCustomerType()
        result.showConsortiaFunctions = contextService.getOrg().isCustomerType_Consortium()
        result.roleLinks = result.subscription.orgRelations.findAll { OrgRole oo -> !(oo.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIUM]) }
        result.roleObject = result.subscription
        result.roleRespValue = RDStore.PRS_RESP_SPEC_SUB_EDITOR.value
        result.editmode = result.subscription.isEditableBy(contextService.getUser())
        result.accessConfigEditable = contextService.isInstEditor(CustomerTypeService.ORG_INST_BASIC) || (contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC) && result.subscription.getSubscriberRespConsortia().id == contextService.getOrg().id)
        result.subscription.packages.pkg.gokbId.each { String uuid ->
            Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: uuid])
            if (queryResult) {
                List records = queryResult.result
                packageMetadata.put(uuid, records[0])
            }
        }
        result.packageMetadata = packageMetadata
        render template: '/subscription/packages', model: result
    }

    /**
     * Gets the data of the linked packages to the subscription which is target of the given survey
     * @return the package details fragment for the survey view
     */
    @Secured(['ROLE_USER'])
    def getGeneralPackageData() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription)]

        result.packages = []
        result.subscription.packages.each { SubscriptionPackage subscriptionPackage ->
            Map packageInfos = [:]

            packageInfos.packageInstance = subscriptionPackage.pkg

            Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: subscriptionPackage.pkg.gokbId])
            if (queryResult.error && queryResult.error == 404) {
                flash.error = message(code: 'wekb.error.404') as String
            } else if (queryResult) {
                List records = queryResult.result
                packageInfos.packageInstanceRecord = records ? records[0] : [:]
            }
            result.packages << packageInfos
        }

        render template: '/survey/generalPackageData', model: result
    }

    /**
     * Gets the data of the linked packages to the subscription which is target of the given survey
     * @return the package details fragment for the survey view
     */
    @Secured(['ROLE_USER'])
    def getIeInfos() {
        Map<String,Object> result = [subscription:Subscription.get(params.subscription)]

        render template: '/survey/ieInfos', model: result
    }

    @Secured(['ROLE_USER'])
    def getSurveyTitlesCount() {
        Map<String,Object> result = [:]
        int titlesInIEGroup, titlesNotInIEGroup
        SurveyConfig surveyConfig = SurveyConfig.get(params.surveyConfig)
        Subscription subParticipant = Subscription.get(params.subParticipant)
        IssueEntitlementGroup ieGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subParticipant)
        if(surveyConfig.pickAndChoosePerpetualAccess) {
            titlesInIEGroup = surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)
            titlesNotInIEGroup = surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subParticipant, surveyConfig)
        }
        else {
            titlesInIEGroup = surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)
            titlesNotInIEGroup = subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subParticipant, ieGroup)
        }
        result.titlesInIEGroup = titlesInIEGroup
        result.titlesNotInIEGroup = titlesNotInIEGroup
        render template: '/templates/survey/surveyTitlesCount', model: result
    }

    @Secured(['ROLE_USER'])
    def updatePricesSelection() {
        Map<String,Object> result = [:]
        String sub = params.sub ?: params.id
        EhcacheWrapper userCache = contextService.getUserCache("/subscription/${params.referer}/${sub}")
        Map<String,Object> cache = userCache.get('selectedTitles')
        if(cache) {
            Map checked = cache.get('checked')
            Set<Long> tippIDs = []
            checked.each { String key, String value ->
                if(value == 'checked')
                    tippIDs << Long.parseLong(key)
            }
            Map<String, Object> listPriceSums = issueEntitlementService.calculateListPriceSumsForTitles(tippIDs)
            result.selectionListPriceEuro = listPriceSums.listPriceSumEUR
            result.selectionListPriceUSD = listPriceSums.listPriceSumUSD
            result.selectionListPriceGBP = listPriceSums.listPriceSumGBP
        }

        render template: '/templates/survey/priceList', model: result
    }

    /**
     * Generates a list of selectable metrics or access types for the given report types in the statistics filter
     * @return a {@link List} of available metric types
     */
    @Secured(['ROLE_USER'])
    def loadFilterList() {
        Map<String, Object> result = subscriptionControllerService.loadFilterList(params)
        result.multiple = params.multiple ? Boolean.valueOf(params.multiple) : true
        render template: "/templates/filter/statsFilter", model: result
    }

    /**
     * Retrieves a list of {@link Provider}s for table view
     * @return the result of {@link de.laser.ControlledListService#getProviders(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupProviders() {
        Map<String, Object> model = [:], result = controlledListService.getProviders(params)
        model.providerList = result.results
        model.unlink = params.containsKey('unlink')
        if(GlobalService.isset(params, 'parent')) {
            Object s = genericOIDService.resolveOID(params.parent)
            Set currProvLinks
            if(s instanceof Subscription) {
                currProvLinks = Provider.executeQuery('select pvr.provider.id from ProviderRole pvr where pvr.subscription = :subscription', [subscription: s])
                model.currProviders = currProvLinks
                model.allChecked = model.providerList.size() > 0 && model.providerList.id.intersect(model.currProviders).size() == model.providerList.size()
                if (s.instanceOf) {
                    model.currProvSharedLinks = Provider.executeQuery('select pvr.provider.id from ProviderRole pvr where pvr.subscription = :subscription and pvr.isShared = true', [subscription: s.instanceOf])
                } else model.currProvSharedLinks = [:]
            }
            if(s instanceof License) {
                currProvLinks = Provider.executeQuery('select pvr.provider.id from ProviderRole pvr where pvr.license = :license', [license: s])
                model.currProviders = currProvLinks
                model.allChecked = model.providerList.size() > 0 && model.providerList.id.intersect(model.currProviders).size() == model.providerList.size()
                if (s.instanceOf) {
                    model.currProvSharedLinks = Provider.executeQuery('select pvr.provider.id from ProviderRole pvr where pvr.license = :license and pvr.isShared = true', [license: s.instanceOf])
                } else model.currProvSharedLinks = [:]
            }
        }
        else {
            model.currProviders = []
            model.currProvSharedLinks = [:]
        }
        model.tmplShowCheckbox = true
        model.tmplConfigShow = ['abbreviatedName', 'name', 'altname', 'isWekbCurated']
        model.fixedHeader = 'la-ignore-fixed'
        render template: "/templates/filter/providerFilterTable", model: model
    }

    /**
     * Retrieves a list of {@link Vendor}s for table view
     * @return the result of {@link de.laser.ControlledListService#getVendors(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @Secured(['ROLE_USER'])
    def lookupVendors() {
        Map<String, Object> model = [:], result = controlledListService.getVendors(params)
        model.vendorList = result.results
        model.unlink = params.containsKey('unlink')
        if(GlobalService.isset(params, 'parent')) {
            Object s = genericOIDService.resolveOID(params.parent)
            Set currVenLinks
            if(s instanceof Subscription) {
                currVenLinks = Vendor.executeQuery('select vr.vendor.id from VendorRole vr where vr.subscription = :subscription', [subscription: s])
                model.currVendors = currVenLinks
                model.allChecked = model.vendorList.size() > 0 && model.vendorList.id.intersect(model.currVendors).size() == model.vendorList.size()
                if (s.instanceOf)
                    model.currVenSharedLinks = Vendor.executeQuery('select vr.vendor.id from VendorRole vr where vr.subscription = :subscription and vr.isShared = true', [subscription: s.instanceOf])
                else model.currVenSharedLinks = [:]
            }
            if(s instanceof License) {
                currVenLinks = Vendor.executeQuery('select vr.vendor.id from VendorRole vr where vr.license = :license', [license: s])
                model.currVendors = currVenLinks
                model.allChecked = model.vendorList.size() > 0 && model.vendorList.id.intersect(model.currVendors).size() == model.vendorList.size()
                if (s.instanceOf)
                    model.currVenSharedLinks = Vendor.executeQuery('select vr.vendor.id from VendorRole vr where vr.license = :license and vr.isShared = true', [license: s.instanceOf])
                else model.currVenSharedLinks = [:]
            }
        }
        else {
            model.currVendors = []
            model.currVenSharedLinks = [:]
        }
        model.tmplShowCheckbox = true
        model.tmplConfigShow = ['abbreviatedName', 'name', 'isWekbCurated', 'linkVendors']
        model.fixedHeader = 'la-ignore-fixed'
        render template: "/templates/filter/vendorFilterTable", model: model
    }

    def renderMarkdown() {
        String text = params.text ?: ''
        render helpService.parseMarkdown2(text)
    }

    /**
     * Opens the edit modal for the given note
     */
    @Secured(['ROLE_USER'])
    def editNote() {
        Map<String, Object> result = [ params: params ]

        DocContext dctx = DocContext.findById(params.long('dctx'))
        if (accessService.hasAccessToDocNote(dctx, AccessService.WRITE)) {
            result.docContext   = dctx
            result.noteInstance = dctx.owner
            render template: "/templates/notes/modal_edit", model: result
        }
        else {
            render template: "/templates/generic_modal403", model: result
        }
    }

    /**
     * Opens the view modal for the given note
     */
    @Secured(['ROLE_USER'])
    def readNote() {
        Map<String, Object> result = [ params: params ]

        DocContext dctx = DocContext.findById(params.long('dctx'))
        if (accessService.hasAccessToDocNote(dctx, AccessService.READ)) {
            result.docContext   = dctx
            result.noteInstance = dctx.owner
            render template: "/templates/notes/modal_read", model: result
        }
        else {
            render template: "/templates/generic_modal403", model: result
        }
    }

    /**
     * Opens the task creation modal
     */
    @Secured(['ROLE_USER'])
    def createTask() {
        Map<String, Object> result = taskService.getPreconditions()

        render template: "/templates/tasks/modal_create", model: result
    }

    /**
     * Opens the task editing modal
     */
    @Secured(['ROLE_USER'])
    def editTask() {
        Map<String, Object> result = [ params: params ]
        Task task = Task.get(params.id)

        if (accessService.hasAccessToTask(task, AccessService.WRITE, true)) {
            result.taskInstance = task
            render template: "/templates/tasks/modal_edit", model: result
        }
        else {
            render template: "/templates/generic_modal403", model: result
        }
    }

    /**
     * Opens the task reading modal
     */
    @Secured(['ROLE_USER'])
    def readTask() {
        Map<String, Object> result = [ params: params ]
        Task task = Task.get(params.id)

        if (accessService.hasAccessToTask(task, AccessService.READ, true)) { // TODO ??? WRITE
            result.taskInstance = task
            render template: "/templates/tasks/modal_read", model: result
        }
        else {
            render template: "/templates/generic_modal403", model: result
        }
    }

    /**
     * Opens the address creation modal and sets the underlying parameters
     */
    @Secured(['ROLE_USER'])
    def createAddress() {
        Map<String, Object> model = [:]
        model.prsId = params.prsId
        model.redirect = params.redirect
        model.typeId = params.typeId ? Long.valueOf(params.typeId) : null
        model.hideType = params.hideType
        model.contextOrg = contextService.getOrg()

        switch(params.addressFor) {
            case 'addressForInstitution':
                if(params.orgId)
                    model.orgId = params.orgId
                else
                    model.orgList = Org.executeQuery(
                            "select o from Org o, OrgSetting os where os.org = o and os.key = :ct and os.roleValue in (:roles) order by LOWER(o.sortname) nulls last",
                            [ct: OrgSetting.KEYS.CUSTOMER_TYPE, roles: customerTypeService.getOrgInstRoles()]
                    )
                model.tenant = model.contextOrg.id
                break
            case 'addressForProvider':
                if(params.providerId)
                    model.providerId = params.providerId
                else
                    model.providerList = Provider.executeQuery("from Provider p order by LOWER(p.name)")
                model.tenant = model.contextOrg.id
                break
            case 'addressForVendor':
                if(params.vendorId)
                    model.vendorId = params.vendorId
                else
                    model.vendorList = Vendor.executeQuery("from Vendor v order by LOWER(v.name)")
                model.tenant = model.contextOrg.id
                break
            default: model.orgId = params.orgId ?: model.contextOrg.id
                break
        }

        if (model.orgId && model.typeId) {
            String messageCode = 'addressFormModalLibraryAddress'
            if (model.typeId == RDStore.ADDRESS_TYPE_LEGAL_PATRON.id)  {
                messageCode = 'addressFormModalLegalPatronAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_BILLING.id)  {
                messageCode = 'addressFormModalBillingAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_POSTAL.id)   {
                messageCode = 'addressFormModalPostalAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_DELIVERY.id) {
                messageCode = 'addressFormModalDeliveryAddress'
            }
            else if (model.typeId == RDStore.ADDRESS_TYPE_LIBRARY.id)  {
                messageCode = 'addressFormModalLibraryAddress'
            }

            model.modalText = message(code: 'default.create.label', args: [message(code: messageCode)])
        } else {
            model.modalText = message(code: 'default.new.label', args: [message(code: 'person.address.label')])
        }
        model.modalMsgSave = message(code: 'default.button.create.label')
        model.url = [controller: 'addressbook', action: 'createAddress']

        render template: "/addressbook/addressFormModal", model: model
    }

    /**
     * Opens the edit modal for an existing address
     */
    @Secured(['ROLE_USER'])
    def editAddress() {
        Map<String, Object> model = [
            addressInstance : Address.get(params.id)
        ]

        if (accessService.hasAccessToAddress(model.addressInstance as Address, AccessService.WRITE)) {
            model.modalId = 'addressFormModal'
            String messageCode = 'person.address.label'
            model.typeId = model.addressInstance.type.id
            if(model.addressInstance.org) {
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)]) + ' (' + model.addressInstance.org.toString() + ')'
            }
            else{
                model.modalText = message(code: 'default.edit.label', args: [message(code: messageCode)])
            }
            model.modalMsgSave = message(code: 'default.button.save_changes')
            model.url = [controller: 'addressbook', action: 'editAddress']

            render template: "/addressbook/addressFormModal", model: model
        }
        else {
            render template: "/templates/generic_modal403", model: model
        }
    }

    /**
     * Opens the contact entity creation modal and sets the underlying parameters
     */
    @Secured(['ROLE_USER'])
    def createPerson() {
        Map<String, Object> result = [:]
        result.tenant = contextService.getOrg()
        result.modalId = 'personModal'
        result.presetFunctionType = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        result.showContacts = params.showContacts == "true" ? true : ''
        result.addContacts = params.showContacts == "true" ? true : ''
        result.org = params.org ? Org.get(params.long('org')) : null
        result.provider = params.provider ? Provider.get(params.long('provider')) : null
        result.vendor = params.vendor ? Vendor.get(params.long('vendor')) : null
        result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_INVOICING_CONTACT, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_OA_CONTACT, RDStore.PRS_FUNC_SURVEY_CONTACT]
        if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            result.functions << RDStore.PRS_FUNC_GASCO_CONTACT
        }
        result.positions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

        switch(params.contactFor) {
            case 'contactPersonForInstitution':
                result.isPublic = false
                if (result.org) {
                    result.modalText = message(code: "person.create_new.contactPersonForInstitution.label") + ' (' + result.org.toString() + ')'
                } else {
                    result.modalText = message(code: "person.create_new.contactPersonForInstitution.label")
                    result.orgList = Org.executeQuery(
                            "select o from Org o, OrgSetting os where os.org = o and os.key = :ct and os.roleValue in (:roles) order by LOWER(o.sortname)",
                            [ct: OrgSetting.KEYS.CUSTOMER_TYPE, roles: customerTypeService.getOrgInstRoles()]
                    )
                }
                break
            case 'contactPersonForProvider':
            case 'contactPersonForProviderPublic':
                result.isPublic    = params.contactFor == 'contactPersonForProviderPublic'
                Set<RefdataValue> excludes = [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                if(params.existsWekbRecord)
                    excludes.addAll([RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA])
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - excludes
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if (result.provider) {
                    result.modalText = message(code: "person.create_new.contactPersonForProvider.label") + ' (' + result.provider.name + ')'
                }
                else {
                    result.modalText = message(code: "person.create_new.contactPersonForProvider.label")
                    result.provList = Provider.executeQuery("from Provider p order by LOWER(p.name)")
                }
                break
            case 'contactPersonForVendor':
            case 'contactPersonForVendorPublic':
                result.isPublic    = params.contactFor == 'contactPersonForVendorPublic'
                Set<RefdataValue> excludes = [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                if(params.existsWekbRecord)
                    excludes.addAll([RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_METADATA])
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - excludes
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                if (result.vendor) {
                    result.modalText = message(code: "person.create_new.contactPersonForVendor.label") + ' (' + result.vendor.name + ')'
                }
                else {
                    result.modalText = message(code: "person.create_new.contactPersonForVendor.label")
                    result.venList = Vendor.executeQuery("from Vendor v order by LOWER(v.name)")
                }
                break
            case 'contactPersonForPublic':
                result.isPublic    = true
                result.modalText = message(code: "person.create_new.contactPersonForPublic.label")
                break
        }
        result.url = [controller: 'addressbook', action: 'createPerson']

        render template: "/addressbook/personFormModal", model: result
    }

    /**
     * Opens the edit modal for an existing contact entity
     */
    @Secured(['ROLE_USER'])
    def editPerson() {
        Map<String, Object> result = [
            personInstance : Person.get(params.id)
        ]

        if (accessService.hasAccessToPerson(result.personInstance as Person, AccessService.WRITE)) {
            result.org = result.personInstance.getBelongsToOrg()
            result.vendor = PersonRole.executeQuery("select distinct(pr.vendor) from PersonRole as pr where pr.prs = :person ", [person: result.personInstance])[0]
            result.provider = PersonRole.executeQuery("select distinct(pr.provider) from PersonRole as pr where pr.prs = :person ", [person: result.personInstance])[0]
            result.functions = [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, RDStore.PRS_FUNC_CONTACT_PRS, RDStore.PRS_FUNC_INVOICING_CONTACT, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_OA_CONTACT, RDStore.PRS_FUNC_SURVEY_CONTACT]
            if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
                result.functions << RDStore.PRS_FUNC_GASCO_CONTACT
            }
            result.positions = PersonRole.getAllRefdataValues(RDConstants.PERSON_POSITION) - [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS]

            if (result.org || (params.org && params.org instanceof String)) {
                result.org = params.org ? Org.get(params.long('org')) : result.org
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForInstitution.label")]) + ' (' + result.org.toString() + ')'
            }
            else if(result.provider != null || params.containsKey('provider')) {
                Provider prv = Provider.get(params.long('provider'))
                if(prv)
                    result.provider = prv
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForProvider.label")]) + ' (' + result.provider.toString() + ')'
                result.contactPersonForProviderPublic = result.personInstance.isPublic
            }
            else if(result.vendor != null || params.containsKey('vendor')) {
                Vendor ven = Vendor.get(params.long('vendor'))
                if(ven)
                    result.vendor = ven
                result.functions = PersonRole.getAllRefdataValues(RDConstants.PERSON_FUNCTION) - [RDStore.PRS_FUNC_GASCO_CONTACT, RDStore.PRS_FUNC_RESPONSIBLE_ADMIN, RDStore.PRS_FUNC_FC_LIBRARY_ADDRESS, RDStore.PRS_FUNC_FC_LEGAL_PATRON_ADDRESS, RDStore.PRS_FUNC_FC_POSTAL_ADDRESS, RDStore.PRS_FUNC_FC_DELIVERY_ADDRESS]
                result.positions = [RDStore.PRS_POS_ACCOUNT, RDStore.PRS_POS_DIREKTION, RDStore.PRS_POS_DIREKTION_ASS, RDStore.PRS_POS_RB, RDStore.PRS_POS_SD, RDStore.PRS_POS_SS, RDStore.PRS_POS_TS]
                result.modalText = message(code: 'default.edit.label', args: [message(code: "person.contactPersonForVendor.label")]) + ' (' + result.vendor.toString() + ')'
                result.contactPersonForVendorPublic = result.personInstance.isPublic
            }
            else {
                result.modalText = message(code: 'default.edit.label', args: [message(code: 'person.label')])
            }

            result.modalId = 'personModal'
            result.modalMsgSave = message(code: 'default.button.save_changes')
            result.showContacts = params.showContacts == "true" ? true : ''
            result.addContacts = params.showContacts == "true" ? true : ''
            result.isPublic = result.personInstance.isPublic
//            result.editable = addressbookService.isPersonEditable(result.personInstance, contextService.getUser())
            result.editable = true // ??
            result.tmplShowDeleteButton = result.editable
            result.url = [controller: 'addressbook', action: 'editPerson', id: result.personInstance.id]

            render template: "/addressbook/personFormModal", model: result
        }
        else {
            render template: "/templates/generic_modal403", model: result
        }
    }

    /**
     * Retrieves the contact fields for an entity modal
     */
    @Secured(['ROLE_USER'])
    def contactFields() {
        render template: "/addressbook/contactFields"
    }

    /**
     * Loads for the subscription-license link table the properties table for a license linked to the triggering subscription
     */
    @Secured(['ROLE_USER'])
    def getLicensePropertiesForSubscription() {
        License loadFor = License.get(params.loadFor)
        if (loadFor) {
            Map<String, Object> derivedPropDefGroups = loadFor.getCalculatedPropDefGroups(contextService.getOrg())
            render view: '/subscription/_licProp', model: [license: loadFor, derivedPropDefGroups: derivedPropDefGroups, linkId: params.linkId]
        }
    }

    /**
     * Opens the modal for selection title with kbart upload
     */
    @Secured(['ROLE_USER'])
    def kbartSelectionUpload() {
        log.debug('ajaxHtmlController.kbartSelectionUpload ' + params)
        Map<String,Object> result = [subscription:Subscription.get(params.id)]
        result.institution = contextService.getOrg()
        result.tab = params.tab
        result.referer = params.referer
        result.headerToken = params.headerToken
        result.withPick = params.containsKey('withPick')
        result.withIDOnly = params.containsKey('withIDOnly')
        result.progressCacheKey = params.progressCacheKey

        if(params.surveyConfigID){
            result.surveyConfig = SurveyConfig.findById(params.surveyConfigID)
        }

        render template: '/subscription/KBARTSelectionUploadFormModal', model: result
    }

    /**
     * Opens the modal for selection title with kbart upload
     */
    @Secured(['ROLE_USER'])
    def linkTitleModal() {
        log.debug('ajaxHtmlController.linkTitleModal ' + params)
        Map<String,Object> result = [isConsortium: contextService.getOrg().isCustomerType_Consortium(), header: message(code: params.headerToken)]
        result.tipp = TitleInstancePackagePlatform.get(params.tippID)
        if(params.containsKey('fixedSubscription'))
            result.fixedSubscription = Subscription.get(params.fixedSubscription)
        render template: '/title/linkTitle', model: result
    }

    // ----- surveyInfos -----

    /**
     * Checks if the preconditions for a survey submission are given
     * @return the modal depending on the survey's completion status
     */
    @Secured(['ROLE_USER'])
    def getSurveyFinishModal() {
        Map<String, Object> result = [:]
        SurveyInfo surveyInfo = SurveyInfo.get(params.id)
        SurveyConfig surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.surveyConfigID) : surveyInfo.surveyConfigs[0]
        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(contextService.getOrg(), surveyConfig)
        List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(contextService.getOrg(), surveyConfig)
        boolean noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)?.refValue == RDStore.YN_NO || (SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION2) && SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION2).refValue != RDStore.YN_YES))
        result.surveyInfo = surveyInfo
        result.surveyConfig = surveyConfig
        result.noParticipation = noParticipation
        result.surveyResult = SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)

        boolean allResultHaveValue = true
        List<String> notProcessedMandatoryProperties = []
        //see ERMS-5815

        boolean existsMultiYearTerm = surveyService.existsCurrentMultiYearTermBySurveyUseForTransfer(surveyConfig, contextService.getOrg())
        if(!noParticipation) {
            surveyResults.each { SurveyResult surre ->
                SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surre.type)
                if (surveyConfigProperties.mandatoryProperty && !surre.isResultProcessed() && !existsMultiYearTerm) {
                    allResultHaveValue = false
                    notProcessedMandatoryProperties << surre.type.getI10n('name')
                }
            }

            if(((SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION)?.refValue == RDStore.YN_YES || SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_PARTICIPATION2)?.refValue == RDStore.YN_YES) || surveyConfig.surveyInfo.isMandatory) && surveyConfig.invoicingInformation && (!surveyOrg.address || (SurveyPersonResult.countByParticipantAndSurveyConfigAndBillingPerson(contextService.getOrg(), surveyConfig, true) == 0))){

                if (!surveyOrg.address) {
                    result.error = g.message(code: 'surveyResult.finish.invoicingInformation.address')
                } else if (SurveyPersonResult.countByParticipantAndSurveyConfigAndBillingPerson(contextService.getOrg(), surveyConfig, true) == 0) {
                    result.error = g.message(code: 'surveyResult.finish.invoicingInformation.contact')
                }

            }
            else if(surveyConfig.surveyInfo.isMandatory && surveyConfig.vendorSurvey) {
                if(SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING)) {
                    boolean vendorInvoicing = SurveyResult.findByParticipantAndSurveyConfigAndType(contextService.getOrg(), surveyConfig, PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING)?.refValue == RDStore.INVOICE_PROCESSING_VENDOR
                    int vendorCount = SurveyVendorResult.executeQuery('select count (*) from SurveyVendorResult spr ' +
                            'where spr.surveyConfig = :surveyConfig and spr.participant = :participant', [surveyConfig: surveyConfig, participant: contextService.getOrg()])[0]
                    if (vendorInvoicing && vendorCount == 0) {
                        result.error = g.message(code: 'surveyResult.finish.vendorSurvey')
                    } else if (!vendorInvoicing && vendorCount > 0) {
                        result.error = g.message(code: 'surveyResult.finish.vendorSurvey.wrongVendor')
                    }
                }
            }else if(surveyConfig.surveyInfo.isMandatory && surveyConfig.subscriptionSurvey) {

            }
            else if (notProcessedMandatoryProperties.size() > 0) {
                result.error = message(code: "confirm.dialog.concludeBinding.survey.notProcessedMandatoryProperties", args: [notProcessedMandatoryProperties.join(', ')])
            }
        }



        if(!result.error) {
            if (surveyConfig.subSurveyUseForTransfer && noParticipation) {
                result.message = message(code: "confirm.dialog.concludeBinding.survey")
            } else if (noParticipation || allResultHaveValue) {
                result.message = message(code: "confirm.dialog.concludeBinding.survey")
            } else if (!noParticipation && !allResultHaveValue) {
                result.message = message(code: "confirm.dialog.concludeBinding.surveyIncomplete")
            }
        }


        render template: '/templates/survey/modalSurveyFinish', model: result
    }

    // ----- reporting -----

    /**
     * Retrieves the filter history and bookmarks for the given reporting view.
     * If a command is being submitted, the cache is being updated. The updated view is being rendered afterwards
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    def reporting() {
        Map<String, Object> result = [
                tab: params.tab
        ]

        String cachePref = ReportingCache.CTX_GLOBAL + '/' + BeanStore.getContextService().getUser().id // user bound
        EhcacheWrapper ttl3600 = cacheService.getTTL3600Cache(cachePref)

        List<String> reportingKeys = ttl3600.getKeys().findAll { it.startsWith(cachePref + '_') } as List<String>
        List<String> reportingTokens = reportingKeys.collect { it.replace(cachePref + '_', '')}

        if (params.context == BaseConfig.KEY_MYINST) {

            if (params.cmd == 'deleteHistory') {
                reportingTokens.each {it -> ttl3600.remove( it ) }
            }
            else if (params.token) {
                if (params.cmd == 'addBookmark') {
                    ReportingCache rc = new ReportingCache(ReportingCache.CTX_GLOBAL, params.token)
                    ReportingFilter rf = ReportingFilter.construct(
                            rc,
                            contextService.getUser(),
                            BaseConfig.getFilterLabel(rc.readMeta().filter.toString()) + ' - ' + DateUtils.getLocalizedSDF_noTime().format(System.currentTimeMillis()),
                            rc.readFilterCache().result.replaceAll('<strong>', '').replaceAll('</strong>', '') as String
                    )
                    result.lastAddedBookmarkId = rf.id
                }
                else if (params.cmd == 'deleteBookmark') {
                    ReportingFilter rf = ReportingFilter.findByTokenAndOwner(params.token, contextService.getUser())
                    if (rf) {
                        rf.delete()
                    }
                }
            }
        }
        result.bookmarks     = ReportingFilter.findAllByOwner( contextService.getUser(), [sort: 'lastUpdated', order: 'desc'] )
        result.filterHistory = reportingTokens.sort { a,b -> ttl3600.get(b).meta.timestamp <=> ttl3600.get(a).meta.timestamp }.take(5)

        render template: '/myInstitution/reporting/historyAndBookmarks', model: result
    }

    /**
     * Retrieves the details for the given charts
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    def chartDetails() {
        // TODO - SESSION TIMEOUTS

        Map<String, Object> result = [
                token:  params.token,
                query:  params.query
        ]
        result.id = params.id ? (params.id != 'null' ? params.long('id') : '') : ''

        if (params.context == BaseConfig.KEY_MYINST) {
            reportingGlobalService.doChartDetails( result, params ) // manipulates result
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            if (params.idx) {
                // TODO !!!!
                params.idx = params.idx.replaceFirst(params.id + ':', '') // TODO !!!!
                // TODO !!!!
            }
            reportingLocalService.doChartDetails( result, params ) // manipulates result
        }

        render template: result.tmpl, model: result
    }

    /**
     * Assembles the chart details and outputs the result in the given format.
     * Currently supported formats are:
     * <ul>
     *     <li>CSV</li>
     *     <li>Excel</li>
     *     <li>PDF</li>
     * </ul>
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    def chartDetailsExport() {

        Map<String, Object> selectedFieldsRaw = params.findAll { it -> it.toString().startsWith('cde:') }
        Map<String, Object> selectedFields = [:]
        selectedFieldsRaw.each { it -> selectedFields.put(it.key.replaceFirst('cde:', ''), it.value) }

        String filename = params.filename ?: BaseExportHelper.getFileName()
        ReportingCache rCache
        BaseDetailsExport export
        Map<String, Object> detailsCache

        if (params.context == BaseConfig.KEY_MYINST) {
            rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            if (rCache.exists()) {
                detailsCache = GlobalExportHelper.getDetailsCache(params.token)
                export = DetailsExportManager.createGlobalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )

            if (rCache.exists()) {
                detailsCache = LocalExportHelper.getDetailsCache(params.token)
                export = DetailsExportManager.createLocalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }

        if (export && detailsCache) {

            if (params.fileformat == 'csv') {

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.csv"')
                response.contentType = 'text/csv'

                List<String> rows = DetailsExportManager.exportAsList(
                        export,
                        detailsCache.idList as List<Long>,
                        'csv',
                        [hideEmptyResults: params.containsKey('hideEmptyResults-csv')]
                )

                ServletOutputStream out = response.outputStream
                out.withWriter { w ->
                    rows.each { r ->
                        w.write(r + '\n')
                    }
                }
                out.close()
            }
            else if (params.fileformat == 'xlsx') {

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.xlsx"')
                response.contentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

                Workbook wb = DetailsExportManager.exportAsWorkbook(
                        export,
                        detailsCache.idList as List<Long>,
                        'xlsx',
                        [   hideEmptyResults: params.containsKey('hideEmptyResults-xlsx'),
                            insertNewLines: params.containsKey('insertNewLines-xlsx'),
                            useHyperlinks: params.containsKey('useHyperlinks-xlsx') ]
                )

                ServletOutputStream out = response.outputStream
                wb.write(out)
                out.close()
            }
            else if (params.fileformat == 'pdf') {

                Map<String, Boolean> options = [
                        hideEmptyResults: params.containsKey('hideEmptyResults-pdf'),
                        useHyperlinks: params.containsKey('useHyperlinks-pdf'),
                        useLineNumbers: params.containsKey('useLineNumbers-pdf'),
                        useSmallFont: params.containsKey('useSmallFont-pdf'),
                        pageFormat: params.get('pageFormat-pdf') != 'auto'
                ]

                List<List<String>> content = DetailsExportManager.exportAsList(
                        export,
                        detailsCache.idList as List<Long>,
                        'pdf',
                        options
                )

                String view = ''
                Map<String, Object> model = [:]
                Map<String, Object> struct = [:]

                List<String> pf = BaseExportHelper.PDF_OPTIONS.get(params.get('pageFormat-pdf'))
                if ( pf[0] != 'auto' ) {
                    struct.pageSize = pf[0]
                    struct.orientation = pf[1]
                }
                else {
                    struct = BaseExportHelper.calculatePdfPageStruct(content, 'chartDetailsExport')
                }

                if (params.context == BaseConfig.KEY_MYINST) {
                    view    = '/myInstitution/reporting/export/pdf/pdfTmpl_generic_details'
                    model   = [
                            filterLabels: GlobalExportHelper.getCachedFilterLabels(params.token),
                            filterResult: GlobalExportHelper.getCachedFilterResult(params.token),
                            queryLabels : GlobalExportHelper.getCachedQueryLabels(params.token),
                            title       : filename,
                            header      : content.remove(0),
                            content     : content,
                            // struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation],
                            options     : options
                    ]
                }
                else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
                    view    = '/subscription/reporting/export/pdf/pdfTmpl_generic_details'
                    model   = [
                            //filterLabels: LocalExportHelper.getCachedFilterLabels(params.token),
                            filterResult: LocalExportHelper.getCachedFilterResult(params.token),
                            queryLabels : LocalExportHelper.getCachedQueryLabels(params.token),
                            title       : filename,
                            header      : content.remove(0),
                            content     : content,
                            // struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation],
                            options     : options
                    ]
                }

                byte[] pdf = wkhtmltoxService.makePdf(
                        view: view,
                        model: model,
                        // header: '',
                        // footer: '',
                        pageSize: struct.pageSize,
                        orientation: struct.orientation,
                        marginLeft: 10,
                        marginTop: 15,
                        marginBottom: 15,
                        marginRight: 10
                )

                response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
            }
        }
    }

    /**
     * Assembles the chart query and outputs the result in the given format.
     * Currently supported formats are:
     * <ul>
     *     <li>CSV</li>
     *     <li>Excel</li>
     *     <li>PDF</li>
     * </ul>
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    def chartQueryExport() {

        ReportingCache rCache
        BaseQueryExport export
        List<String> queryLabels = []
        String filename = params.filename ?: BaseExportHelper.getFileName()

        if (params.context == BaseConfig.KEY_MYINST) {
            rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            if (rCache.exists()) {
                export      = QueryExportManager.createExport( params.token, BaseConfig.KEY_MYINST )
                queryLabels = GlobalExportHelper.getIncompleteQueryLabels( params.token )
                //detailsCache = GlobalExportHelper.getDetailsCache(params.token)
                //export = DetailsExportManager.createGlobalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION) {
            rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )

            if (rCache.exists()) {
                export      = QueryExportManager.createExport( params.token, BaseConfig.KEY_SUBSCRIPTION )
                queryLabels = LocalExportHelper.getCachedQueryLabels( params.token )
                //detailsCache = LocalExportHelper.getDetailsCache(params.token)
                //export = DetailsExportManager.createLocalExport(params.token, selectedFields)
            }
            else {
                redirect(url: request.getHeader('referer')) // TODO
                return
            }
        }

        if (params.fileformat == 'csv') {

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.csv"')
            response.contentType = 'text/csv'

            List<String> rows = QueryExportManager.exportAsList( export, 'csv' )

            ServletOutputStream out = response.outputStream
            out.withWriter { w ->
                rows.each { r ->
                    w.write( r + '\n')
                }
            }
            out.close()
        }
        else if (params.fileformat == 'xlsx') {

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.xlsx"')
            response.contentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

            Workbook wb = QueryExportManager.exportAsWorkbook( export, 'xlsx' )

            ServletOutputStream out = response.outputStream
            wb.write(out)
            out.close()
        }
        else if (params.fileformat == 'pdf') {
            // TODO
            // TODO
            // TODO
            // TODO
            List<List<String>> content = QueryExportManager.exportAsList(export, 'pdf')

            Map<String, Object> struct = [:]

//            if (params.contentType == 'table') {
//                struct = ExportHelper.calculatePdfPageStruct(content, 'chartQueryExport')
//            }
//            if (params.contentType == 'image') {
            // struct = ExportHelper.calculatePdfPageStruct(content, 'chartQueryExport-image') // TODO

//                struct = [
//                        width       : Float.parseFloat( params.imageSize.split(':')[0] ),
//                        height      : Float.parseFloat( params.imageSize.split(':')[1] ),
//                        pageSize    : 'A4',
//                        orientation : 'Portrait'
//                ] as Map<String, Object>
//
//                struct.whr = struct.width / struct.height
//                if (struct.height < 400 && struct.whr >= 2) {
//                    struct.orientation = 'Landscape'
//                }

            //Map<String, Object> queryCache = BaseQuery.getQueryCache( params.token )
            //queryCache.put( 'tmpBase64Data', params.imageData )
//            }

            Map<String, Object> model = [
                    token:        params.token,
                    filterLabels: GlobalExportHelper.getCachedFilterLabels(params.token),
                    filterResult: GlobalExportHelper.getCachedFilterResult(params.token),
                    queryLabels : queryLabels,
                    //imageData   : params.imageData,
                    //tmpBase64Data : BaseQuery.getQueryCache( params.token ).get( 'tmpBase64Data' ),
                    contentType : params.contentType,
                    title       : filename,
                    header      : content.remove(0),
                    content     : content,
                    struct      : [struct.width, struct.height, struct.pageSize + ' ' + struct.orientation]
            ]

            byte[] pdf = wkhtmltoxService.makePdf(
                    view: '/myInstitution/reporting/export/pdf/pdfTmpl_generic_query',
                    model: model,
                    // header: '',
                    // footer: '',
                    pageSize: struct.pageSize,
                    orientation: struct.orientation,
                    marginLeft: 10,
                    marginTop: 15,
                    marginBottom: 15,
                    marginRight: 10
            )

            response.setHeader('Content-disposition', 'attachment; filename="' + filename + '.pdf"')
            response.setContentType('application/pdf')
            response.outputStream.withStream { it << pdf }

//                render view: '/myInstitution/reporting/export/pdf/pdf_generic_query', model: model
        }
    }

    // ----- workflows -----

    /**
     * Call to render the flyout containing the steps of a given workflow
     * @return the template containing the data for the flyout
     */
    @Secured(['ROLE_USER'])
    def workflowFlyout() {
        Map<String, Object> result = [
                tmplCmd:    'usage',
                tmplFormUrl: createLink(controller: 'myInstitution', action: 'currentWorkflows')
        ]

        if (params.cmd) {
            String[] cmd = params.cmd.split(':')

            if (cmd[1] in [WfChecklist.KEY, WfCheckpoint.KEY] ) {
                result.putAll( workflowService.executeCmd(params) ) // TODO !!!
            }
        }
//        if (params.info) {
//            result.info = params.info // @ currentWorkflows @ dashboard
//        }

        if (params.key) {
            String[] key = (params.key as String).split(':')
            result.prefix = key[0]

            if (result.prefix == WfChecklist.KEY) {
                result.clist      = WfChecklist.get( key[1] )
            }
//            else if (result.prefix == WfCheckpoint.KEY) {
//                result.checkpoint     = WfCheckpoint.get( key[1] )
//                result.tmplModalTitle = g.message(code:'task.label') + ': ' +  result.checkpoint.title
//            }
        }
        result.referer = request.getHeader('referer')

        WfChecklist toCheck = result.clist as WfChecklist
        if (!accessService.hasAccessToWorkflow(toCheck, AccessService.READ, true)) {
            render template: "/templates/generic_flyout403"
        }
        else {
            render template: '/templates/workflow/flyout', model: result
        }
    }

    /**
     * Opens the modal for a given object, containing workflow details for the given object. Along that, the form
     * processing parameters are set according to the object type being treated
     * @return the template fragment for the workflow modal
     */
    @Secured(['ROLE_USER'])
    def workflowModal() {
        Map<String, Object> result = [
                tmplCmd:    'usage',
                tmplFormUrl: createLink(controller: 'myInstitution', action: 'currentWorkflows')
        ]

        if (params.key) {
            String[] key = (params.key as String).split(':')
            //println key

            result.prefix = key[3]

            // myInstitution::action:WF_X:id
            // subscription:id:action:WF_X:id
            if (key[0] in [License.class.name, Org.class.name, Provider.class.name, Subscription.class.name, Vendor.class.name]) {

                if (key[0] == License.class.name) {
                    result.targetObject = License.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'lic', action: key[2], id: key[1])
                }
                else if (key[0] == Org.class.name){
                    result.targetObject = Org.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'org', action: key[2], id: key[1])
                }
                else if (key[0] == Provider.class.name){
                    result.targetObject = Provider.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'provider', action: key[2], id: key[1])
                }
                else if (key[0] == Subscription.class.name) {
                    result.targetObject = Subscription.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'subscription', action: key[2], id: key[1])
                }
                else if (key[0] == Vendor.class.name) {
                    result.targetObject = Vendor.get( key[1] )
                    result.tmplFormUrl  = createLink(controller: 'vendor', action: key[2], id: key[1])
                }

            }
            else if (key[0] == 'myInstitution') {
                result.tmplFormUrl  = createLink(controller: 'myInstitution', action: key[2])
                if (key[2] == 'dashboard') {
                    result.tmplFormUrl = result.tmplFormUrl + '?view=Workflows'
                }
            }

            if (result.prefix == WfChecklist.KEY) {
                result.checklist      = WfChecklist.get( key[4] )
                result.tmplModalTitle = g.message(code:'workflow.label') as String
            }
            else if (result.prefix == WfCheckpoint.KEY) {
                result.checkpoint     = WfCheckpoint.get( key[4] )

                if (result.checkpoint.done) {
                    result.tmplModalTitle = '<i class="' + WorkflowHelper.getCssIconAndColorByStatus( RDStore.WF_TASK_STATUS_DONE ) + '"></i>&nbsp;' + result.checkpoint.title
                }
                else {
                    result.tmplModalTitle = '<i class="' + WorkflowHelper.getCssIconAndColorByStatus( RDStore.WF_TASK_STATUS_OPEN ) + '"></i>&nbsp;' + result.checkpoint.title
                }
            }
        }
        if (params.info) {
            result.info = params.info
        }

        WfChecklist toCheck = result.checklist ? result.checklist as WfChecklist : (result.checkpoint as WfCheckpoint).getChecklist()
        if (!accessService.hasAccessToWorkflow(toCheck, AccessService.READ, true)) {
            render template: "/templates/generic_modal403"
        }
        else {
            render template: '/templates/workflow/modal', model: result
        }
    }

    // ----- titles -----

    /**
     * Retrieves detailed title information to a given entitlement and opens a modal showing those details
     */
    @Secured(['ROLE_USER'])
    Map<String,Object> showAllTitleInfos() {
        Map<String, Object> result = [:]

        result.tipp = params.tippID ? TitleInstancePackagePlatform.get(params.tippID) : null
        result.ie = params.ieID ? IssueEntitlement.get(params.ieID) : null
        result.showPackage = params.showPackage
        result.showPlattform = params.showPlattform
        result.showCompact = params.showCompact
        result.showEmptyFields = params.showEmptyFields

        render template: "/templates/titles/title_modal", model: result

    }

    /**
     * Retrieves detailed title information to a given entitlement and opens a modal showing those details
     */
    @Secured(['ROLE_USER'])
    Map<String,Object> showAllTitleInfosAccordion() {
        Map<String, Object> result = [:]

        result.tipp = params.tippID ? TitleInstancePackagePlatform.get(params.tippID) : null
        result.ie = params.ieID ? IssueEntitlement.get(params.ieID) : null
        result.showPackage = params.showPackage
        result.showPlattform = params.showPlattform
        result.showCompact = params.showCompact
        result.showEmptyFields = params.showEmptyFields

        render template: "/templates/titles/title_long_accordion", model: result

    }

    /**
     * Opens a modal containing a preview of the given document if rights are granted and the file being found.
     * The preview is being generated according to the MIME type of the requested document; the document key is
     * expected as docContextID
     * @return the template containing a preview of the document (either document viewer or fulltext extract)
     */
    @Secured(['ROLE_USER'])
    def documentPreview() {
        Map<String, Object> result = [
                modalId : 'document-preview-' + params.dctx,
                modalTitle : message(code: 'template.documents.preview')
        ]

        try {
            DocContext docCtx = DocContext.findById(params.long('dctx'))

            if (docCtx) {
                if (accessService.hasAccessToDocument(docCtx, AccessService.READ)) {
                    Doc doc = docCtx.owner

                    result.docCtx = docCtx
                    result.doc = doc
                    result.modalTitle = doc.title

                    Map<String, String> mimeTypes = Doc.getPreviewMimeTypes()
                    if (mimeTypes.containsKey(doc.mimeType)) {
                        String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                        File f = new File(fPath + '/' +  doc.uuid)
                        if (f.exists()) {

                            File decTmpFile = fileCryptService.decryptToTmpFile(f, doc.ckey)
                            if (mimeTypes.get(doc.mimeType) == 'raw') {
                                result.docBase64 = decTmpFile.getBytes().encodeBase64()
                                result.docDataType = doc.mimeType
                            }
                            else if (mimeTypes.get(doc.mimeType) == 'encode') {
                                String charset = UniversalDetector.detectCharset(decTmpFile) ?: Charset.defaultCharset()
                                result.docBase64 = decTmpFile.getText(charset).encodeAsRaw().getBytes().encodeBase64()
                                result.docDataType = 'text/plain;charset=' + charset
                            }
                            else {
                                result.error = message(code: 'template.documents.preview.error') as String
                            }
                            decTmpFile.delete()
                        }
                        else {
                            result.error = message(code: 'template.documents.preview.fileNotFound') as String
                        }
                    }
                    else {
                        result.error = message(code: 'template.documents.preview.unsupportedMimeType') as String
                    }
                }
                else {
                    result.warning = message(code: 'template.documents.preview.forbidden') as String
                }
            }
            else {
                result.error = message(code: 'template.documents.preview.fileNotFound') as String
            }
        }
        catch (Exception e) {
            result.error = message(code: 'template.documents.preview.error') as String
            log.error e.getMessage()
        }

        render template: '/templates/documents/preview', model: result
    }

    @Secured(['ROLE_USER'])
    def createPropertiesModal() {
        Map<String, Object> result = [:]
        Org contextOrg = contextService.getOrg()
        User user = contextService.getUser()

        String lang = LocaleUtils.getCurrentLang()
        switch (params.objectTyp) {
            case SurveyInfo.class.simpleName:
                SurveyInfo surveyInfo = SurveyInfo.get(params.id)
                SurveyConfig surveyConfig = surveyInfo.surveyConfigs[0]
                result.editable = surveyService.isEditableSurvey(contextService.getOrg(), surveyInfo)
                if ((surveyConfig && result.editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING) && params.onlyPrivateProperties == 'false') {
                    result.allPropDefGroups = PropertyDefinitionGroup.executeQuery('select pdg from PropertyDefinitionGroup pdg where pdg.ownerType = :ownerType and pdg.tenant = :tenant order by pdg.order asc', [tenant: contextOrg, ownerType: PropertyDefinition.getDescrClass(PropertyDefinition.SVY_PROP)])
                    result.orphanedProperties = propertyService.getOrphanedPropertyDefinition(PropertyDefinition.SVY_PROP)

                }
                if ((surveyConfig && result.editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING) && params.onlyPrivateProperties == 'true') {
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.SVY_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: SurveyInfo.class.name, objectId: surveyInfo.id])
                result.object = surveyInfo
                break
            case Subscription.class.simpleName:
                Subscription subscription = Subscription.get(params.id)
                result.editable = subscription.isEditableBy(user)
                if((result.editable || contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)) && params.onlyPrivateProperties == 'false') {
                    result.allPropDefGroups = PropertyDefinitionGroup.executeQuery('select pdg from PropertyDefinitionGroup pdg where pdg.ownerType = :ownerType and pdg.tenant = :tenant order by pdg.order asc', [tenant: contextOrg, ownerType: PropertyDefinition.getDescrClass(PropertyDefinition.SUB_PROP)])
                    result.orphanedProperties = propertyService.getOrphanedPropertyDefinition(PropertyDefinition.SUB_PROP)
                }

                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true') {
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.SUB_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }

                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: Subscription.class.name, objectId: subscription.id])
                result.object = subscription
                break
            case License.class.simpleName:
                License license = License.get(params.id)
                result.editable = license.isEditableBy(user)
                if((result.editable || contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)) && params.onlyPrivateProperties == 'false') {
                    result.allPropDefGroups = PropertyDefinitionGroup.executeQuery('select pdg from PropertyDefinitionGroup pdg where pdg.ownerType = :ownerType and pdg.tenant = :tenant order by pdg.order asc', [tenant: contextOrg, ownerType: PropertyDefinition.getDescrClass(PropertyDefinition.LIC_PROP)])
                    result.orphanedProperties = propertyService.getOrphanedPropertyDefinition(PropertyDefinition.LIC_PROP)
                }

                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true' )  {
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.LIC_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: License.class.name, objectId: license.id])
                result.object = license
                break
            case Org.class.simpleName:
                Org org = Org.get(params.id)
                boolean isEditable = false

                boolean inContextOrg          = org.id == contextService.getOrg().id
                boolean userIsYoda            = contextService.getUser().isYoda()
                boolean userIsAdmin           = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
                boolean userHasEditableRights = userIsAdmin || contextService.isInstEditor()
                if (inContextOrg) {
                    isEditable = userHasEditableRights
                }
                else {
                    switch (contextService.getOrg().getCustomerType()){
                        case [ CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO ] :
                            switch (org.getCustomerType()){
                                case CustomerTypeService.ORG_INST_BASIC:        isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_INST_PRO:          isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_BASIC:  isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_PRO:    isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_SUPPORT:           isEditable = userIsYoda; break
                            }
                            break
                        case [ CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_SUPPORT ] :
                            switch (org.getCustomerType()){
                                case CustomerTypeService.ORG_INST_BASIC:        isEditable = userHasEditableRights; break
                                case CustomerTypeService.ORG_INST_PRO:          isEditable = userHasEditableRights; break
                                case CustomerTypeService.ORG_CONSORTIUM_BASIC:  isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_CONSORTIUM_PRO:    isEditable = userIsYoda; break
                                case CustomerTypeService.ORG_SUPPORT:           isEditable = userIsYoda; break
                            }
                            break
                    }
                }
                result.editable = isEditable
                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true') {
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.ORG_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: Org.class.name, objectId: org.id])
                result.object = org
                break
            case Provider.class.simpleName:
                Provider provider = Provider.get(params.id)
                result.editable = contextService.isInstEditor()
                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true' ){
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.PRV_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: Provider.class.name, objectId: provider.id])
                result.object = provider
                break
            case Platform.class.simpleName:
                Platform platform = Platform.get(params.id)
                result.editable = contextService.isInstEditor()
                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true' ){
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.PLA_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: Platform.class.name, objectId: platform.id])
                result.object = platform
                break
            case Vendor.class.simpleName:
                Vendor vendor = Vendor.get(params.id)
                result.editable = contextService.isInstEditor()
                if((result.editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO ) || contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )) && params.onlyPrivateProperties == 'true' ){
                    result.privateProperties = PropertyDefinition.findAllByDescrAndTenant(PropertyDefinition.VEN_PROP, contextService.getOrg(), [sort: 'name_' + lang])
                }
                result.propertyCreateUrl = createLink(controller: 'ajaxHtml', action: 'processCreateProperties', params: [objectClass: Vendor.class.name, objectId: vendor.id])
                result.object = vendor
                break
        }

        render template: '/templates/properties/createPropertyModal', model: result
    }

    @Secured(['ROLE_USER'])
    def processCreateProperties() {
        Map<String, Object> result = [:]
        result.error = []

        switch (params.objectClass) {
            case SurveyInfo.class.name:
                SurveyInfo surveyInfo = SurveyInfo.get(params.objectId)
                SurveyConfig surveyConfig = surveyInfo.surveyConfigs[0]
                boolean editable = surveyService.isEditableSurvey(contextService.getOrg(), surveyInfo)
                if (surveyConfig && editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING) {
                    params.list('propertyDefinition').each { pd ->
                        PropertyDefinition property = PropertyDefinition.get(Long.parseLong(pd))
                        if (!surveyService.addSurPropToSurvey(surveyConfig, property)) {
                            result.error << property.getI10n('name')
                        }
                    }
                }
                break
            case [Subscription.class.name, License.class.name, Org.class.name, Provider.class.name, Platform.class.name, Vendor.class.name]:
                def owner = CodeUtils.getDomainClass(params.objectClass)?.get(params.objectId)
                if (owner) {
                    params.list('propertyDefinition').each { pd ->
                        PropertyDefinition property = PropertyDefinition.get(Long.parseLong(pd))
                        if (!propertyService.addPropertyToObject(owner, property)) {
                            result.error << property.getI10n('name')
                        }
                    }
                } else {
                    flash.error = message(code: "default.error")
                }
                break
        }

        if(result.error.size() > 0) {
            flash.error = message(code: "properties.exists ", args: [result.error.join(', ')])
        }

        redirect(url: request.getHeader('referer'))

    }

}