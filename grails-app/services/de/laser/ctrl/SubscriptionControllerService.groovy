package de.laser.ctrl


import de.laser.*
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.auth.User
import de.laser.base.AbstractReport
import de.laser.cache.EhcacheWrapper
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.remote.ApiSource
import de.laser.remote.GlobalRecordSource
import de.laser.reporting.report.local.SubscriptionReport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.config.ConfigMapper
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.VendorRole
import grails.converters.JSON
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.json.JsonSlurper
import groovy.sql.BatchingStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovy.xml.slurpersupport.GPathResult
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.grails.orm.hibernate.cfg.PropertyConfig
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus
import org.springframework.web.multipart.MultipartFile

import javax.sql.DataSource
import java.math.RoundingMode
import java.sql.Array
import java.sql.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.ExecutorService

/**
 * This service is a container for those methods of the {@link SubscriptionController} which contain complex data manipulation operations
 */
@Transactional
class SubscriptionControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AddressbookService addressbookService
    AuditService auditService
    BatchQueryService batchQueryService
    ContextService contextService
    DocstoreService docstoreService
    FactService factService
    ExecutorService executorService
    ExecutorWrapperService executorWrapperService
    ExportService exportService
    FilterService filterService
    FinanceControllerService financeControllerService
    FinanceService financeService
    FormService formService
    GenericOIDService genericOIDService
    GlobalService globalService
    GlobalSourceSyncService globalSourceSyncService
    GokbService gokbService
    IssueEntitlementService issueEntitlementService
    LinksGenerationService linksGenerationService
    ManagementService managementService
    MessageSource messageSource
    PackageService packageService
    PendingChangeService pendingChangeService
    PropertyService propertyService
    ProviderService providerService
    StatsSyncService statsSyncService
    SubscriptionService subscriptionService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyService surveyService
    TaskService taskService
    WorkflowService workflowService

    //-------------------------------------- general or ungroupable section -------------------------------------------

    /**
     * Loads the given subscription details and returns them to the details view. If mandatory properties are missing,
     * they will be created; that is why this method needs to be transactional
     * @param params the request parameter map
     * @return the given subscription's details
     */
    Map<String,Object> show(GrailsParameterMap params) {
        Profiler prf = new Profiler()
        prf.setBenchmark('1')
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            prf.setBenchmark('this-n-that')
            result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
            prf.setBenchmark('packages')

            result.pendingChangeConfigSettings = RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)

            prf.setBenchmark('tasks')
            // TODO: experimental asynchronous task
            //def task_tasks = task {
            // tasks
            result.tasks = taskService.getTasksByResponsibilityAndObject(result.user, result.subscription)

            prf.setBenchmark('properties')
            // TODO: experimental asynchronous task
            //def task_properties = task {
            // -- private properties
            // create mandatory OrgPrivateProperties if not existing
            List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.SUB_PROP, true, contextService.getOrg())
            mandatories.each { PropertyDefinition pd ->
                if (!SubscriptionProperty.findAllByOwnerAndTypeAndTenantAndIsPublic(result.subscription, pd, contextService.getOrg(), false)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.subscription, pd, contextService.getOrg())
                    if (newProp.hasErrors()) {
                        log.error(newProp.errors.toString())
                    } else {
                        log.debug("New subscription private property created via mandatory: " + newProp.type.name)
                    }
                }
            }
            // -- private properties
            result.modalPrsLinkRole = RDStore.PRS_RESP_SPEC_SUB_EDITOR
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())
            result.visiblePrsLinks = []
            result.subscription.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic) {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        if (contextService.getOrg().id == pl.prs.tenant.id) {
                            result.visiblePrsLinks << pl
                        }
                    }
                }
            }
            Set<RefdataValue> wekbFunctionTypes = [RDStore.PRS_FUNC_METADATA, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT]
            result.visiblePrsLinks.addAll(PersonRole.executeQuery('select pr from PersonRole pr where (pr.provider in (select pvr.provider from ProviderRole pvr where pvr.subscription = :s) or pr.provider in (select pkg.provider from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :s)) and pr.functionType in (:wekbFunctionTypes)', [s: result.subscription, wekbFunctionTypes: wekbFunctionTypes]))
            //}
            if(ConfigMapper.getShowStatsInfo()) {
                prf.setBenchmark('usage')
                // TODO: experimental asynchronous task
                //def task_usage = task {
                // usage
                prf.setBenchmark('before platform query')
                Set suppliers = Platform.executeQuery('select plat.id from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription = :sub', [sub: result.subscription])
                if (suppliers.size() > 1) {
                    log.debug('Found different content platforms for this subscription, cannot show usage')
                }
                else if(suppliers.size() == 1) {
                    prf.setBenchmark('before loading platform')
                    Long supplier_id = suppliers[0]
                    //PlatformProperty platform = PlatformProperty.executeQuery('select pp from PlatformProperty pp join pp.type pd where pp.owner.id = :owner and pd.name = :name and pd.descr = :descr', [owner: supplier_id, name: 'NatStat Supplier ID', descr: PropertyDefinition.PLA_PROP])[0]
                    //        PlatformProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyStore.PLA_NATSTAT_SID)
                    prf.setBenchmark('before institutional usage identifier')
                    result.natStatSupplierId = Platform.get(suppliers[0]).natstatSupplierID
                    if (result.institutional_usage_identifier != OrgSetting.SETTING_NOT_FOUND) {
                        prf.setBenchmark('before usage data')
                        def fsresult = factService.generateUsageData(result.institution.id, supplier_id, result.subscription)
                        prf.setBenchmark('before usage data sub period')
                        def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(result.institution.id, supplier_id, result.subscription)
                        Set<RefdataValue> holdingTypes = RefdataValue.executeQuery('select tipp.titleType from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :context and ie.status = :current', [context: result.subscription, current: RDStore.TIPP_STATUS_CURRENT])
                        if (!holdingTypes) {
                            log.debug('No types found, maybe there are no issue entitlements linked to subscription')
                        } else if (holdingTypes.size() > 1) {
                            log.info('Different content type for this license, cannot calculate Cost Per Use.')
                        } else if (!fsLicenseResult.isEmpty() && result.subscription.startDate) {
                            def existingReportMetrics = fsLicenseResult.y_axis_labels*.split(':')*.last()
                            prf.setBenchmark('before total cost per use')
                            def costPerUseMetricValuePair = factService.getTotalCostPerUse(result.subscription, holdingTypes.first(), existingReportMetrics)
                            if (costPerUseMetricValuePair) {
                                result.costPerUseMetric = costPerUseMetricValuePair[0]
                                result.totalCostPerUse = costPerUseMetricValuePair[1]
                                result.currencyCode = NumberFormat.getCurrencyInstance().getCurrency().currencyCode
                            }
                        }
                        result.statsWibid = result.institution.getIdentifierByType('wibid')?.value
                        if (result.statsWibid && result.natStatSupplierId) {
                            result.usageMode = contextService.getOrg().isCustomerType_Consortium() ? 'package' : 'institution'
                            result.usage = fsresult?.usage
                            result.missingMonths = fsresult?.missingMonths
                            result.missingSubscriptionMonths = fsLicenseResult?.missingMonths
                            result.x_axis_labels = fsresult?.x_axis_labels
                            result.y_axis_labels = fsresult?.y_axis_labels
                            result.lusage = fsLicenseResult?.usage
                            prf.setBenchmark('before last usage period for report type')
                            result.lastUsagePeriodForReportType = factService.getLastUsagePeriodForReportType(result.natStatSupplierId, result.statsWibid)
                            result.l_x_axis_labels = fsLicenseResult?.x_axis_labels
                            result.l_y_axis_labels = fsLicenseResult?.y_axis_labels
                        }
                    } else
                        log.info('institutional usage identifier not available')
                }
            }
            //}
            prf.setBenchmark('costs')
            //cost items
            params.subDetailsPage = true
            LinkedHashMap costItems = financeService.getCostItemsForSubscription(params, financeControllerService.getResultGenerics(params))
            result.costItemSums = [:]
            if (costItems.own) {
                result.costItemSums.ownCosts = costItems.own.sums
            }
            if (costItems.cons) {
                result.costItemSums.consCosts = costItems.cons.sums
            }
            if(costItems.coll) {
                result.costItemSums.collCosts = costItems.coll.sums
            }
            if (costItems.subscr) {
                result.costItemSums.subscrCosts = costItems.subscr.sums
            }
            prf.setBenchmark('provider & agency filter')
            // TODO: experimental asynchronous task
            //def task_providerFilter = task {
            //}
            result.publicSubscriptionEditors = Person.getPublicByOrgAndObjectResp(null, result.subscription, 'Specific subscription editor')
            if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_ADMINISTRATIVE,CalculatedType.TYPE_CONSORTIAL]) {
                prf.setBenchmark('non-inherited member properties')
                List<Subscription> childSubs = result.subscription.getNonDeletedDerivedSubscriptions()
                if(childSubs) {
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )

                    result.memberProperties = memberProperties
                }
            }

            workflowService.executeCmdAndUpdateResult(result, params)

            List bm = prf.stopBenchmark()
            result.benchMark = bm

            result.permanentTitlesProcessRunning = subscriptionService.checkPermanentTitleProcessRunning(result.subscription, result.institution)

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Loads the given subscription transfer details and returns them to the details view
     * @param params the request parameter map
     * @return the transfer steps concerning this subscription
     */
    Map<String,Object> subTransfer(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Loads the tasks attached to the given subscription
     * @param controller unused
     * @param params the request parameter map
     * @return the tasks for the given subscription
     */
    Map<String,Object> tasks(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            SwissKnife.setPaginationParams(result, params, result.user as User)
            result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (Subscription) result.subscription)['cmbTaskInstanceList']
            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------------------- statistics section -----------------------------------------------------------

    /**
     * Reveals the usage data for the given subscription. The usage data may be filtered; one report type and one metric type
     * is default. COUNTER 4 data is being retrieved as fallback if no COUNTER 5 data exists
     * @param params the request parameter map
     * @return the usage data, grouped by month
     */
    Map<String, Object> getStatsDataForCostPerUse(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.contextOrg = contextService.getOrg() // TODO: subscriptionControllerService.calculateCostPerUse()

        Profiler prf = new Profiler('statsLoading')
        prf.setBenchmark('start loading data')
        SwissKnife.setPaginationParams(result, params, result.user)
        if(!result)
            [result: null, status: STATUS_ERROR]
        else {
            result.dateRun = new Date()
            SimpleDateFormat yearFormat = DateUtils.getSDF_yyyy(), monthFormat = DateUtils.getSDF_yyyyMM()
            result.revision = params.revision
            //at this point, we should be sure that at least the parent subscription has a holding!
            Platform platform = Platform.get(params.platform)
            Set<IdentifierNamespace> namespaces = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISBN, TitleInstancePackagePlatform.class.name), IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, TitleInstancePackagePlatform.class.name)] as Set<IdentifierNamespace>
            IdentifierNamespace propIdNamespace = IdentifierNamespace.findByNs(platform.titleNamespace)
            namespaces.add(propIdNamespace)
            Subscription refSub
            prf.setBenchmark('before subscription determination')
            if(subscriptionService.getCurrentIssueEntitlementIDs(result.subscription).size() > 0){
                refSub = result.subscription
            }
            else refSub = result.subscription.instanceOf
            Map<String, Object> c4counts = [:], c4allYearCounts = [:], c5counts = [:]//, c5allYearCounts = [:], countSumsPerYear = [:]
            SortedSet datePoints = new TreeSet()
            String sort, groupKey
            Org customer = result.subscription.getSubscriberRespConsortia()
            result.customer = customer
            if(platform && refSub) {
                Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(result.subscription, 'sourceSubscription')
                subscriptions << result.subscription
                result.subscriptions = subscriptions
                Set<CostItem> refCostItems = CostItem.executeQuery('select ci from CostItem ci where ci.sub in (:subscriptions) and ci.costItemElementConfiguration = :positive order by ci.startDate asc', [subscriptions: subscriptions, positive: RDStore.CIEC_POSITIVE])
                Map<String, Object> titles = [:] //structure: namespace -> value -> tipp
                //Set<TitleInstancePackagePlatform> titlesSorted = [] //fallback structure to preserve sorting
                if(params.reportType in Counter4Report.COUNTER_4_TITLE_REPORTS || params.reportType in Counter5Report.COUNTER_5_TITLE_REPORTS) {
                    Map<String, Object> idSubsetQueryParams = [refSub: refSub, current: RDStore.TIPP_STATUS_CURRENT]
                    Object[] titleArray = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp, SubscriptionPackage sp where tipp.pkg = sp.pkg and sp.subscription = :refSub and tipp.status = :current', idSubsetQueryParams).toArray()
                    titles = fetchTitles(titleArray)
                }
                Map<String, Object> dateRanges = getDateRange(params, refCostItems)
                if(dateRanges.containsKey('alternatePeriodStart') && dateRanges.containsKey('alternatePeriodEnd')){
                    result.alternatePeriodStart = dateRanges.alternatePeriodStart
                    result.alternatePeriodEnd = dateRanges.alternatePeriodEnd
                }
                if(dateRanges.containsKey('startDate') && dateRanges.containsKey('endDate')) {
                    result.startDate = dateRanges.startDate
                    result.endDate = dateRanges.endDate
                    if (!(params.reportType in [Counter4Report.JOURNAL_REPORT_5, Counter5Report.JOURNAL_REQUESTS_BY_YOP])) {
                        datePoints.addAll(dateRanges.monthsInRing.collect { Date month -> monthFormat.format(month) })
                    }
                }
                //Counter5Report.withTransaction {
                Map<String, Object> queryParams = [revision: params.revision, reportType: params.reportType, customer: customer, platform: platform, metricTypes: params.list('metricType'), startDate: dateRanges.startDate, endDate: dateRanges.endDate]
                if(params.revision == AbstractReport.COUNTER_5) {
                    Set<String> metricTypes = []
                    if(params.metricType) {
                        queryParams.metricTypes = params.list('metricType').join('%7C')
                        result.metricTypes = params.metricType
                    }
                    if(params.accessType) {
                        result.accessTypes = params.list('accessType')
                        queryParams.accessTypes = params.list('accessType').join('%7C')
                    }
                    if(params.accessMethod) {
                        result.accessMethod = params.list('accessMethod')
                        queryParams.accessMethods = params.list('accessMethod').join('%7C')
                    }
                    Map<String, Object> requestResponse = exportService.getReports(queryParams)
                    if(requestResponse.containsKey('items')) {
                        //c5usages.addAll(Counter5Report.executeQuery('select r from Counter5Report r where r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+filter+' order by r.metricType, r.reportFrom', queryParams))
                        /*
                        structure:
                        (reportType is fixed, metricType as well)
                        {
                            metric: [
                                month1: sum1, month2: sum2, ..., monthn: sumn
                            ]
                        }
                         */
                        //c5usages.each { Counter5Report report -> }
                        for(Map reportItem : requestResponse.items) {
                            Map<String, String> identifierMap = exportService.buildIdentifierMap(reportItem, AbstractReport.COUNTER_5)
                            boolean titleMatch = (params.reportType in Counter5Report.COUNTER_5_TITLE_REPORTS && issueEntitlementService.matchReport(titles, identifierMap) != null) || params.reportType in Counter5Report.COUNTER_5_PLATFORM_REPORTS || params.reportType in Counter5Report.COUNTER_5_DATABASE_REPORTS
                            if(titleMatch) {
                                //counter 5.0
                                if(reportItem.containsKey('Performance')) {
                                    for(Map performance: reportItem.Performance) {
                                        Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
                                        //String year = yearFormat.format(reportFrom)
                                        for(Map instance: performance.Instance) {
                                            String metricType = instance.Metric_Type
                                            metricTypes << metricType
                                            Map<String, Object> metricDatePointSums = c5counts.containsKey(metricType) ? c5counts.get(metricType) : [total: 0]
                                            //if((!dateRanges.startDate || reportFrom >= dateRanges.startDate) && (!dateRanges.endDate || reportFrom <= dateRanges.endDate)) {
                                            String datePoint
                                            if(params.reportType == Counter5Report.JOURNAL_REQUESTS_BY_YOP) {
                                                datePoint = "YOP ${performance.YOP}"
                                            }
                                            else {
                                                datePoint = monthFormat.format(reportFrom)
                                            }
                                            datePoints << datePoint
                                            metricDatePointSums.total += instance.Count
                                            int monthCount = metricDatePointSums.get(datePoint) ?: 0
                                            monthCount += instance.Count
                                            metricDatePointSums.put(datePoint, monthCount)
                                            c5counts.put(instance.Metric_Type, metricDatePointSums)
                                            //}
                                            //int yearCount = metricYearSums.containsKey(year) ? metricYearSums.get(year) : 0
                                            //totalCount = countSumsPerYear.containsKey(year) ? countSumsPerYear.get(year) : 0
                                            //yearCount += instance.Count
                                            //totalCount += instance.Count
                                            //metricYearSums.put(year, yearCount)
                                            //countSumsPerYear.put(year, totalCount)
                                            //c5allYearCounts.put(metricType, metricYearSums)
                                            //allYears << year
                                        }
                                    }
                                }
                                //counter 5.1
                                else if(reportItem.containsKey('Attribute_Performance')) {
                                    for (Map struct : reportItem.Attribute_Performance) {
                                        for (Map.Entry performance : struct.Performance) {
                                            for (Map.Entry instance : performance) {
                                                String metricType = instance.getKey()
                                                metricTypes << metricType
                                                for (Map.Entry reportRow : instance.getValue()) {
                                                    Map<String, Object> metricDatePointSums = c5counts.containsKey(metricType) ? c5counts.get(metricType) : [total: 0]
                                                    String datePoint
                                                    if(params.reportType == Counter5Report.JOURNAL_REQUESTS_BY_YOP) {
                                                        datePoint = "YOP ${performance.YOP}"
                                                    }
                                                    else {
                                                        datePoint = reportRow.getKey()
                                                    }
                                                    datePoints << datePoint
                                                    metricDatePointSums.total += reportRow.getValue()
                                                    int monthCount = metricDatePointSums.get(datePoint) ?: 0
                                                    monthCount += reportRow.getValue()
                                                    metricDatePointSums.put(datePoint, monthCount)
                                                    c5counts.put(metricType, metricDatePointSums)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //result.allYearSums = c5allYearCounts
                        //result.allYears = allYears
                        result.sums = c5counts
                        result.datePoints = datePoints
                    }
                    else if(requestResponse.containsKey('error')) {
                        result.error = requestResponse.error
                        [result: result, status: STATUS_ERROR]
                    }
                }
                else {
                    if(params.metricType) {
                        queryParams.metricTypes = params.list('metricType')
                        result.metricTypes = params.metricType
                    }
                    else {
                        result.metricTypes = Counter4Report.METRIC_TYPES.valueOf(params.reportType).metricTypes
                    }
                    Map<String, Object> requestResponse = exportService.getReports(queryParams)
                    if(requestResponse.containsKey('reports')) {
                        Set<String> availableMetrics = requestResponse.reports.'**'.findAll { node -> node.name() == 'MetricType' }.collect { node -> node.text()}.toSet()
                        //c4usages.addAll(Counter4Report.executeQuery('select r from Counter4Report r where r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+filter+' order by r.metricType, r.reportFrom', queryParams))
                        /*
                        structure:
                        (reportType is fixed, metricType is not)
                        {
                            metric: [
                                month1: sum1, month2: sum2, ..., monthn: sumn
                            ]
                        }
                         */
                        //c4usages.each { Counter4Report report -> }
                        for(GPathResult reportItem: requestResponse.reports) {
                            Map<String, String> identifierMap = exportService.buildIdentifierMap(reportItem, AbstractReport.COUNTER_4)
                            boolean titleMatch = (params.reportType in Counter4Report.COUNTER_4_TITLE_REPORTS && issueEntitlementService.matchReport(titles, identifierMap) != null) || params.reportType in Counter4Report.COUNTER_4_PLATFORM_REPORTS
                            if(titleMatch) {
                                for(GPathResult performance: reportItem.'ns2:ItemPerformance') {
                                    Date reportFrom = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
                                    //String year = yearFormat.format(reportFrom)
                                    for(GPathResult instance: performance.'ns2:Instance') {
                                        String metricType = instance.'ns2:MetricType'.text()
                                        Integer count = Integer.parseInt(instance.'ns2:Count'.text())
                                        Map<String, Object> metricDatePointSums = c4counts.get(metricType) ?: [total: 0], metricYearSums = c4allYearCounts.get(metricType) ?: [:]
                                        //if((!dateRanges.startDate || reportFrom >= dateRanges.startDate) && (!dateRanges.endDate || reportFrom <= dateRanges.endDate)) {
                                            String datePoint
                                            if(params.reportType == Counter4Report.JOURNAL_REPORT_5) {
                                                datePoint = "YOP ${performance.'@PubYr'.text()}"
                                            }
                                            else {
                                                datePoint = monthFormat.format(reportFrom)
                                            }
                                            datePoints << datePoint
                                            metricDatePointSums.total += count
                                            int monthCount = metricDatePointSums.get(datePoint) ?: 0
                                            monthCount += count
                                            metricDatePointSums.put(datePoint, monthCount)
                                            c4counts.put(metricType, metricDatePointSums)
                                        //}
                                        //int yearCount = metricYearSums.containsKey(year) ? metricYearSums.get(year) : 0//
                                            //totalCount = countSumsPerYear.containsKey(year) ? countSumsPerYear.get(year) : 0
                                        //yearCount += count
                                        //totalCount += count
                                        //metricYearSums.put(year, yearCount)
                                        //countSumsPerYear.put(year, totalCount
                                        //c4allYearCounts.put(metricType, metricYearSums)
                                        //allYears << year
                                    }
                                }
                            }
                        }
                        //}
                        //result.allYearSums = c4allYearCounts
                        //result.allYears = allYears
                        result.sums = c4counts
                        result.datePoints = datePoints
                    }
                    else {
                        result.datePoints = []
                        result.error = requestResponse.error
                        [result: result, status: STATUS_ERROR]
                    }
                }
            }
            else {
                result.metricTypes = []
                result.reportTypes = []
                result.datePoints = []
                result.platforms = [] as JSON
            }
            //result.countSumsPerYear = countSumsPerYear
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Calculates a cost per use for the holding subscribed based on the given statistics data and filter configuration.
     * The method is under development as for the moment, the concept how cost per use should be calculated is not finalised yet.
     * The concept underlying the current implementation is as follows:
     * cf. https://www.o-bib.de/bib/article/view/5521/7935
     * legacy COUNTER 4 metric types: https://www.niso.org/schemas/sushi/counterElements4_1.xsd
     * There are calculated:
     * price/download -> metrics ft_epub, ft_html, ft_pdf, ft_ps, ft_total, sectioned_html, data_set, audio, video, image, multimedia, podcast (COUNTER 4) resp. Total_XYZ_Requests (COUNTER 5)
     * price/search -> metrics search_reg, search_fed (COUNTER 4) resp. Searches_Regular, Searches_Platform (COUNTER 5)
     * price/click -> metrics result_click (COUNTER 4) resp. ??? (COUNTER 5)
     * price/view -> metrics record_view, toc, abstract, reference (COUNTER 4) resp. XYZ_Investigations (COUNTER 5)
     * Questions:
     * <ol>
     *     <li>use all clicks or unique clicks? -> use for each metric a separate cost per use?</li>
     *     <li>the 100% encompasses everything. If I select several metrics, how should I calculate cost per use? Distribute equally?</li>
     * </ol>
     * response: I need to take the 100% of all clicks as well (i.e. of all metrics); the cost per use has thus to be calculated by the amount of clicks. So all clicks of all metrics altogether give the complete sum.
     * @param statsData the usage data loaded for the given customer institution and matched to the holding subscribed
     * @param config whose cost item elements should be used for the calculation – that of the consortium or that of the institution?
     * @return a {@link Map} containing the sum for each metric and cost considered for the calculation
     */
    Map<String, Object> calculateCostPerUse(Map<String, Object> statsData, String config) {
        Map<String, Map<String, BigDecimal>> costPerMetric = [:]
        Set<CostItem> allCostItems = []
        if(config == "own") {
            //Set<RefdataValue> elementsToUse = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :institution and ciec.useForCostPerUse = true', [institution: statsData.contextOrg])
            allCostItems = CostItem.executeQuery('select ci from CostItem ci where ci.owner = :ctx and ci.sub in (:subs) order by ci.startDate', [ctx: statsData.contextOrg, subs: statsData.subscriptions])
        }
        else if(config == "consortial") {
            Org consortium = statsData.subscription.getConsortium()
            //Set<RefdataValue> elementsToUse = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :institution and ciec.useForCostPerUse = true', [institution: consortium])
            allCostItems = CostItem.executeQuery('select ci from CostItem ci where ci.owner = :consortium and ci.sub in (:subs) and ci.isVisibleForSubscriber = true order by ci.startDate', [consortium: consortium, subs: statsData.subscriptions])
        }
        /*
         * acceptable cases:
         * one start and one end date
         * if one start date has been set: a subsequent cost with the same start date must also match the end date
         * now: consider the time selection
         * 1. no extrapolation
         * 2. check time spans
         * 2a. time spans match
         * 2b. selected is larger than covered by cost items
         * 2c. cost item coverage is larger than selected span
         *
         * cases 2a and 2b: take cost item span
         * case 2c: take selected span
         */
        //calculate 100% for each year - works only iff costs have been defined in year rings!
        Map<String, Map<String, Object>> costsAllYears = [:]
        Calendar costYear = GregorianCalendar.getInstance(), startYear = GregorianCalendar.getInstance()
        startYear.setTime(statsData.startDate)
        allCostItems.each { CostItem ci ->
            costYear.setTime(ci.startDate)
            Integer year = costYear.get(Calendar.YEAR)
            if(year >= startYear.get(Calendar.YEAR)) {
                String yearKey = year.toString()
                Map<String, Object> costsInYear = costsAllYears.containsKey(yearKey) ? costsAllYears.get(yearKey) : [total: 0.0, startDate: ci.startDate, endDate: ci.endDate]
                if(ci.startDate < costsInYear.startDate)
                    costsInYear.startDate = ci.startDate
                if(ci.endDate < costsInYear.endDate)
                    costsInYear.endDate = ci.endDate
                BigDecimal total = costsInYear.total
                switch(ci.costItemElementConfiguration) {
                    case RDStore.CIEC_POSITIVE: total += ci.costInBillingCurrencyAfterTax
                        break
                    case RDStore.CIEC_NEGATIVE: total -= ci.costInBillingCurrencyAfterTax
                        break
                }
                costsInYear.total = total
                costsAllYears.put(yearKey,costsInYear)
            }
        }
        BigDecimal allYearsTotal = 0.0
        costsAllYears.each { String year, Map<String, Object> costsInYear ->
            Calendar stCal = GregorianCalendar.getInstance(), endCal = GregorianCalendar.getInstance()
            stCal.setTime(costsInYear.startDate)
            endCal.setTime(costsInYear.endDate)
            int monthsCount = (endCal.get(Calendar.MONTH)+1) - (stCal.get(Calendar.MONTH)+1)
            costsInYear.partial = costsInYear.total / monthsCount
            allYearsTotal += costsInYear.total
            costsAllYears.put(year, costsInYear)
        }
        //log.debug("total cost for year: ${costForYear}, partial amount: ${partialCostForYear} for ${monthsCount} months total")
        if(costsAllYears) {
            //loop 1: metrics
            statsData.sums.each { String metricType, Map<String, Object> reportYearMetrics ->
                //loop 2: metrics in report year
                Map<String, BigDecimal> metricSums = costPerMetric.containsKey(metricType) ? costPerMetric.get(metricType) : [:]
                reportYearMetrics.each { String date, Integer count ->
                    BigDecimal metricSum = 0.0
                    if(count > 0) {
                        if(date == 'total') {
                            metricSum = (allYearsTotal / count).setScale(2, RoundingMode.HALF_UP)
                        }
                        else {
                            metricSum = (costsAllYears.get(date.split('-')[0]).partial / count).setScale(2, RoundingMode.HALF_UP)
                        }
                    }
                    metricSums.put(date, metricSum)
                }
                costPerMetric.put(metricType, metricSums)
            }
        }
        [costsAllYears: costsAllYears, costPerMetric: costPerMetric]
    }

    /**
     * Calculates the date ranges for the given query. Note that for internal usage, {@link LocalDate} is being used but
     * unfortunately, {@link Date} is required for the GORM model
     * @param params the request parameter map containing filter input
     * @param result the generics result map, containing implicite relevant data
     * @return a map of filter data, of structure:
     * <ol start="0">
     *     <li>HQL filter string</li>
     *     <li>HQL date range</li>
     *     <li>months contained in the observed time span</li>
     * </ol>
     */
    Map<String, Object> getDateRange(GrailsParameterMap params, Subscription subscription) {
        //String dateRange
        SortedSet<Date> monthsInRing = new TreeSet<Date>()
        Map<String, Object> dateRangeParams = [:]
        LocalDate startTime, endTime = LocalDate.now(), now = LocalDate.now()
        if(params.containsKey('startDate') && params.containsKey('endDate')) {
            if(params.containsKey('startDate')) {
                startTime = LocalDate.parse(params.startDate+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                dateRangeParams.startDate = Date.from(startTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
            if(params.containsKey('endDate')) {
                endTime = LocalDate.parse(params.endDate+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                dateRangeParams.endDate = Date.from(endTime.withDayOfMonth(endTime.getMonth().length(endTime.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
        }
        else {
            if(subscription.startDate && subscription.endDate) {
                //dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                if(!params.containsKey('tabStat') || params.tabStat == 'total') {
                    if(subscription.startDate > new Date()) {
                        LocalDate lastMonth = LocalDate.now()
                        lastMonth.minusMonths(1)
                        dateRangeParams.startDate = Date.from(lastMonth.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    }
                    else dateRangeParams.startDate = subscription.startDate
                    if(subscription.endDate <= Date.from(now.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        dateRangeParams.endDate = subscription.endDate
                    else dateRangeParams.endDate = Date.from(now.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                else {
                    LocalDate filterDate = LocalDate.parse(params.tabStat+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                    dateRangeParams.startDate = Date.from(filterDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    dateRangeParams.endDate = Date.from(filterDate.withDayOfMonth(filterDate.getMonth().length(filterDate.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                startTime = subscription.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                //is completely meaningless, but causes 500 if not dealt ...
                if(subscription.endDate < new Date() || subscription.startDate > new Date())
                    endTime = subscription.endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            else if(subscription.startDate) {
                //dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                if(!params.containsKey('tabStat') || params.tabStat == 'total') {
                    dateRangeParams.startDate = subscription.startDate
                    dateRangeParams.endDate = new Date()
                }
                else {
                    LocalDate filterDate = LocalDate.parse(params.tabStat+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                    dateRangeParams.startDate = Date.from(filterDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    dateRangeParams.endDate = Date.from(filterDate.withDayOfMonth(filterDate.getMonth().length(filterDate.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                startTime = subscription.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                endTime = LocalDate.now()
            }
            else {
                LocalDate lastYear = LocalDate.now().minus(1, ChronoUnit.YEARS)
                startTime = lastYear.with(TemporalAdjusters.firstDayOfYear())
                endTime = lastYear.with(TemporalAdjusters.lastDayOfYear())
                dateRangeParams.startDate = Date.from(startTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
                dateRangeParams.endDate = Date.from(endTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
            /*
            range spans over the whole availability of reports, no preset!
            else {
                if(!params.containsKey('tabStat') || params.tabStat == 'total') {
                    dateRange = ''
                }
                else {
                    dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                    LocalDate filterDate = LocalDate.parse(params.tabStat+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                    dateRangeParams.startDate = Date.from(filterDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    dateRangeParams.endDate = Date.from(filterDate.withDayOfMonth(filterDate.getMonth().length(filterDate.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                startTime = now.with(TemporalAdjusters.firstDayOfYear())
                endTime = now.with(TemporalAdjusters.lastDayOfYear())
            }*/
        }

        endTime = endTime.with(TemporalAdjusters.lastDayOfMonth())
        if(params.tabStat && params.tabStat != 'total' && params.exportXLS && params.data != 'fetchAll') {
            monthsInRing << DateUtils.getSDF_yyyyMM().parse(params.tabStat)
        }
        else if(startTime) {
            while(startTime.isBefore(endTime)) {
                monthsInRing << Date.from(startTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
                startTime = startTime.plusMonths(1)
            }
        }

        //titles have to be pre-fetched, title filter thus moved
        dateRangeParams+[monthsInRing: monthsInRing]
    }

    Map<String, Object> getDateRange(GrailsParameterMap params, Set<CostItem> costItems) {
        /*String dateRange
        SortedSet<Date> monthsInRing = new TreeSet<Date>()
        Map<String, Object> dateRangeParams = [:]
        LocalDate startTime, endTime = LocalDate.now(), now = LocalDate.now()
        if(params.containsKey('startDate') && params.containsKey('endDate')) {
            if(params.containsKey('startDate')) {
                startTime = LocalDate.parse(params.startDate+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                dateRangeParams.startDate = Date.from(startTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
                if(costItem.startDate && dateRangeParams.startDate < costItem.startDate) {
                    startTime = costItem.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    dateRangeParams.alternatePeriod = true
                }
            }
            if(params.containsKey('endDate')) {
                endTime = LocalDate.parse(params.endDate+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                dateRangeParams.endDate = Date.from(endTime.withDayOfMonth(endTime.getMonth().length(endTime.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                if(costItem.endDate && dateRangeParams.endDate > costItem.endDate) {
                    endTime = costItem.endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    dateRangeParams.alternatePeriod = true
                }
            }
        }
        else {
            if(costItem.startDate && costItem.endDate) {
                //dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                if(!params.containsKey('tabStat') || params.tabStat == 'total') {
                    if(costItem.startDate > new Date()) {
                        LocalDate lastMonth = LocalDate.now()
                        lastMonth.minusMonths(1)
                        dateRangeParams.startDate = Date.from(lastMonth.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    }
                    else dateRangeParams.startDate = costItem.startDate
                    if(costItem.endDate <= Date.from(now.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        dateRangeParams.endDate = costItem.endDate
                    else dateRangeParams.endDate = Date.from(now.withDayOfMonth(now.getMonth().length(now.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                else {
                    LocalDate filterDate = LocalDate.parse(params.tabStat+'-01', DateTimeFormatter.ofPattern('yyyy-MM-dd'))
                    dateRangeParams.startDate = Date.from(filterDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    dateRangeParams.endDate = Date.from(filterDate.withDayOfMonth(filterDate.getMonth().length(filterDate.isLeapYear())).atStartOfDay(ZoneId.systemDefault()).toInstant())
                }
                startTime = costItem.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                //is completely meaningless, but causes 500 if not dealt ...
                if(costItem.endDate < new Date() || costItem.startDate > new Date())
                    endTime = costItem.endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            else if(costItem.startDate) {
                //dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                dateRangeParams.startDate = costItem.startDate
                dateRangeParams.endDate = new Date()
                startTime = costItem.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                endTime = LocalDate.now()
            }
            else {
                LocalDate lastYear = LocalDate.now().minus(1, ChronoUnit.YEARS)
                startTime = lastYear.with(TemporalAdjusters.firstDayOfYear())
                endTime = lastYear.with(TemporalAdjusters.lastDayOfYear())
                dateRangeParams.startDate = Date.from(startTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
                dateRangeParams.endDate = Date.from(endTime.atStartOfDay(ZoneId.systemDefault()).toInstant())
            }
        }

        endTime = endTime.with(TemporalAdjusters.lastDayOfMonth())
        */
        if(params.containsKey('startDate') && params.containsKey('endDate')) {
            SortedSet<Date> monthsInRing = new TreeSet<Date>()
            Map<String, Object> dateRangeParams = [:]
            /*
            possible cases:
            1. date ranges match
            2. date ranges overlap
            3. date ranges are disjunct
            while
            a. selected start date before cost item start date
            b. selected end date before cost item start date (! that works!)
            c. selected start date after cost item end date
            d. selected end date after cost item end date
            gives (under exclusion that selected start date is after selected end date; should be prevented by frontend anyway)
            a == true && c == false || a == false && c == true then 2
            a == true && b == true || c == true && d == true then 3
             */
            Date selStartDate = DateUtils.parseDateGeneric(params.startDate), selEndDate = DateUtils.parseDateGeneric(params.endDate)
            Date startDate, endDate
            if(selStartDate >= costItems.last().endDate || selEndDate <= costItems.first().startDate) {
                [error: 'invalidDatesSelected']
            }
            else {
                if(selStartDate < costItems.first().startDate) {
                    startDate = costItems.first().startDate
                    dateRangeParams.alternatePeriodStart = startDate
                }
                else if(selStartDate >= costItems.first().startDate) {
                    startDate = selStartDate
                }
                if(selEndDate <= costItems.last().endDate) {
                    endDate = selEndDate
                }
                else if(selEndDate > costItems.last().endDate) {
                    endDate = costItems.last().endDate
                    if(costItems.last().endDate)
                        endDate = new Date()
                    dateRangeParams.alternatePeriodEnd = endDate
                }
                Calendar currMonth = GregorianCalendar.getInstance()
                currMonth.setTime(startDate)
                while(currMonth.getTime() < endDate) {
                    monthsInRing << currMonth.getTime()
                    currMonth.set(Calendar.MONTH, currMonth.get(Calendar.MONTH)+1)
                }
                dateRangeParams.startDate = startDate
                dateRangeParams.endDate = endDate
            }
            dateRangeParams+[monthsInRing: monthsInRing]
        }
        else [error: 'noDatesSelected']
    }

    /**
     * Assembles a set of titles currently contained in the given subscriptions' holding.
     * A map of identifiers is being returned for identifier matching without needing further queries or loops
     * @param refSub the {@link Subscription} whose entitlements should be queried; if none existent, the underlying package's are being queried
     * @return
     */
    Map<String, Object> fetchTitles(Object[] titleSet) {
        Map<String, Object> result = [:]
        /*
        desired:
        {
            printIdentifiers: {
                value: tipp
                value: tipp
            },
            printIdentifiers: {
                value: tipp
                value: tipp
            },
            dois: {
                value: tipp
                value: tipp
            },
            proprietaryIdentifiers: {
                value: tipp
                value: tipp
            }
        }
         */
        Sql sql = GlobalService.obtainSqlConnection()
        Object[] printIdentifierNamespaces = [IdentifierNamespace.ISBN, IdentifierNamespace.ISSN], onlineIdentifierNamespaces = [IdentifierNamespace.EISBN, IdentifierNamespace.EISSN]
        Array titleIDs = sql.getDataSource().getConnection().createArrayOf('bigint', titleSet)
        JsonSlurper slurper = new JsonSlurper()
        Map<String, String> printIdentifiers = [:], onlineIdentifiers = [:], doi = [:], url = [:], proprietaryIdentifiers = [:]
        List<GroovyRowResult> printIds = sql.rows("select json_agg(json_build_object(id_value, id_tipp_fk)) as print_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = any(:printIdentifiers)", [printIdentifiers: sql.getDataSource().getConnection().createArrayOf('varchar', printIdentifierNamespaces), tippIDs: titleIDs])
        List<GroovyRowResult> onlineIds = sql.rows("select json_agg(json_build_object(id_value, id_tipp_fk)) as online_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = any(:onlineIdentifiers)", [onlineIdentifiers: sql.getDataSource().getConnection().createArrayOf('varchar', onlineIdentifierNamespaces), tippIDs: titleIDs])
        List<GroovyRowResult> dois = sql.rows("select json_agg(json_build_object(id_value, id_tipp_fk)) as doi from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = :doi", [doi: IdentifierNamespace.DOI, tippIDs: titleIDs])
        List<GroovyRowResult> urls = sql.rows("select json_agg(json_build_object(tipp_host_platform_url, tipp_id)) as url from title_instance_package_platform where tipp_id = any(:tippIDs)", [tippIDs: titleIDs])
        List<GroovyRowResult> propIds = sql.rows("select json_agg(json_build_object(id_value, id_tipp_fk)) as proprietary_identifier from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = any(:tippIDs) and idns_ns = :proprietary", [tippIDs: titleIDs, proprietary: IdentifierNamespace.TITLE_ID])
        if(printIds[0]['print_identifier']) {
            List records = slurper.parseText(printIds[0]['print_identifier'].toString())
            printIdentifiers.putAll(records.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        }
        if(onlineIds[0]['online_identifier']) {
            List records = slurper.parseText(onlineIds[0]['online_identifier'].toString())
            onlineIdentifiers.putAll(records.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        }
        if(dois[0]['doi']) {
            List records = slurper.parseText(dois[0]['doi'].toString())
            doi.putAll(records.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        }
        if(urls[0]['url']) {
            List records = slurper.parseText(urls[0]['url'].toString())
            url.putAll(records.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        }
        if(propIds[0]['proprietary_identifier']) {
            List records = slurper.parseText(propIds[0]['proprietary_identifier'].toString())
            proprietaryIdentifiers.putAll(records.collectEntries { row -> [row.entrySet()[0].getKey(), row.entrySet()[0].getValue()] })
        }
        result.put('printIdentifiers', printIdentifiers)
        result.put('onlineIdentifiers', onlineIdentifiers)
        result.put('doi', doi)
        result.put('url', url)
        result.put('proprietaryIdentifiers', proprietaryIdentifiers)
        result
    }

    /**
     * Retrieves a set of available COUNTER reports for the given platforms. If a valid SUSHI API URL
     * has been registered for the platform, the /reports endpoint will be queried for the respective list of supported
     * reports. That does not work for platforms supporting COUNTER 4 only; then, the full set of reports will be
     * returned. In order to determine the COUNTER revision supported and the SUSHI API URL, the we:kb is being queried
     * to fetch the SUSHI configuration data
     * @param configMap the request parameter map
     * @return a {@link Set} of supported reports
     * @see Subscription
     * @see Platform
     */
    Set getAvailableReports(Map<String, Object> configMap, boolean withPlatformReports = true) {
        Set<String> allAvailableReports
        Set<Package> subscribedPackages = configMap.subscription.packages.pkg
        Map<RefdataValue, String> contentTypes = RefdataCategory.getAllRefdataValues([RDConstants.PACKAGE_CONTENT_TYPE, RDConstants.TITLE_MEDIUM]).collectEntries { RefdataValue rdv -> [rdv, rdv.value] }
        subscribedPackages.each { Package pkg ->
            RefdataValue contentType = pkg.contentType
            if(!contentType) {
                List titleTypes = TitleInstancePackagePlatform.executeQuery('select m from TitleInstancePackagePlatform tipp join tipp.medium m where tipp.pkg = :pkg and tipp.status != :removed group by m', [pkg: pkg, removed: RDStore.TIPP_STATUS_REMOVED])
                if(titleTypes.size() == 1)
                    contentType = titleTypes[0]
            }
            Platform platform = pkg.nominalPlatform
            Map<String, Object> queryResult = gokbService.executeQuery(ApiSource.getCurrent().getSushiSourcesURL(), [:])
            Map platformRecord
            if (queryResult) {
                Map<String, Object> records = queryResult
                if(records.counter4ApiSources.containsKey(platform.gokbId)) {
                    platformRecord = records.counter4ApiSources.get(platform.gokbId)
                }
                else if(records.counter5ApiSources.containsKey(platform.gokbId)) {
                    platformRecord = records.counter5ApiSources.get(platform.gokbId)
                }
            }
            if(platformRecord) {
                CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(configMap.subscription.getSubscriberRespConsortia(), platform)
                configMap.putAll(exportService.prepareSushiCall(platformRecord))
                if(configMap.revision && configMap.statsUrl && ci.value) {
                    if(configMap.revision == AbstractReport.COUNTER_5) {
                        allAvailableReports = new TreeSet<String>()
                        String queryArguments = exportService.buildQueryArguments(configMap, platformRecord, ci)
                        Map<String, Object> availableReports = statsSyncService.fetchJSONData(configMap.statsUrl + queryArguments, ci, true)
                        if(availableReports && availableReports.list) {
                            availableReports.list.each { listEntry ->
                                String reportType = listEntry["Report_ID"].toLowerCase()
                                if(reportType in Counter5Report.COUNTER_5_REPORTS) {
                                    if(reportType in Counter5Report.COUNTER_5_PLATFORM_REPORTS && withPlatformReports)
                                        allAvailableReports.add(reportType)
                                    else {
                                        switch(contentTypes.get(contentType)) {
                                            case 'Book': if(reportType in Counter5Report.COUNTER_5_BOOK_REPORTS)
                                                allAvailableReports.add(reportType)
                                                break
                                            case 'Journal': if(reportType in Counter5Report.COUNTER_5_JOURNAL_REPORTS)
                                                allAvailableReports.add(reportType)
                                                break
                                            default: allAvailableReports.add(reportType)
                                                break
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if(configMap.revision == AbstractReport.COUNTER_4) {
                        allAvailableReports = []
                        //unfortunately! I need to alert that there is no possibility to check whether the API supports the report!
                        if(withPlatformReports) {
                            allAvailableReports.addAll(Counter4Report.COUNTER_4_PLATFORM_REPORTS)
                            allAvailableReports.addAll(Counter4Report.COUNTER_4_DATABASE_REPORTS)
                        }
                        switch(contentTypes.get(contentType)) {
                            case 'Book': allAvailableReports.addAll(Counter4Report.COUNTER_4_BOOK_REPORTS)
                                break
                            case 'Journal': allAvailableReports.addAll(Counter4Report.COUNTER_4_JOURNAL_REPORTS)
                                break
                            default: allAvailableReports.addAll(Counter4Report.COUNTER_4_TITLE_REPORTS)
                                break
                        }
                    }
                }
            }
        }
        allAvailableReports
    }

    /**
     * Loads the additional filter lists for the requested report. Note that only the COUNTER 5 Master Reports support
     * accessType and accessMethod filters; the Standard Views have those implicitly set
     * @param params the request parameter map
     * @return a {@link Map} containing sets of metricTypes, accessTypes (for Master Reports) and accessMethods (for Master Reports)
     */
    Map<String, Object> loadFilterList(GrailsParameterMap params) {
        Set metricTypes = [], accessTypes = [], accessMethods = []
        try {
            if(params.reportType in Counter4Report.COUNTER_4_REPORTS)
                metricTypes.addAll(Counter4Report.METRIC_TYPES.valueOf(params.reportType).metricTypes)
            else if(params.reportType in Counter5Report.COUNTER_5_REPORTS) {
                metricTypes.addAll(Counter5Report.METRIC_TYPES.valueOf(params.reportType.toUpperCase()).metricTypes)
                if(params.reportType in [Counter5Report.TITLE_MASTER_REPORT, Counter5Report.ITEM_MASTER_REPORT])
                    accessTypes.addAll(Counter5Report.ACCESS_TYPES.valueOf(params.reportType.toUpperCase()).accessTypes)
                accessMethods.addAll(Counter5Report.ACCESS_METHODS.valueOf(params.reportType.toUpperCase()).accessMethods)
            }
        }
        catch (IllegalArgumentException e) {
            log.error("no filter setting for picked metric type!")
        }
        [metricTypes: metricTypes, accessTypes: accessTypes, accessMethods: accessMethods]
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    /**
     * Prepares the new subscription form
     * @param controller unused
     * @param params the request parameter map
     * @return a map containing defaults or ERROR if permission is not granted
     */
    Map<String,Object> emptySubscription(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(result.editable) {
            Calendar cal = GregorianCalendar.getInstance()
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            cal.setTimeInMillis(System.currentTimeMillis())
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            result.defaultStartYear = sdf.format(cal.getTime())
            cal.set(Calendar.MONTH, Calendar.DECEMBER)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            result.defaultEndYear = sdf.format(cal.getTime())
            if(contextService.getOrg().isCustomerType_Consortium()) {
                params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
                FilterService.Result fsr = filterService.getOrgComboQuery(params, result.institution as Org)
                if (fsr.isFilterSet) { params.filterSet = true }

                result.members = Org.executeQuery(fsr.query, fsr.queryParams, params)
            }
            [result:result,status:STATUS_OK]
        }
        else {
            result.errorMessage = 'default.notAuthorized.message'
            [result:result,status:STATUS_ERROR]
        }
    }

    /**
     * Takes the submitted parameters and creates a new subscription instance based on the given input
     * @param controller unused
     * @param params the input parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> processEmptySubscription(SubscriptionController controller, GrailsParameterMap params) {
        log.debug( params.toMapString() )
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        RefdataValue orgRole
        RefdataValue memberRole
        RefdataValue subType = RefdataValue.get(params.type)

        switch(subType) {
            case [ RDStore.SUBSCRIPTION_TYPE_CONSORTIAL, RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE ]:
                orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIUM
                memberRole = RDStore.OR_SUBSCRIBER_CONS
                break
            default:
                orgRole = RDStore.OR_SUBSCRIBER
                if (! subType)
                    subType = RDStore.SUBSCRIPTION_TYPE_LOCAL
                break
        }

        //result may be null, change is TODO
        if (result?.editable) {

            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
            Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
            RefdataValue status = RefdataValue.get(params.status)

            //beware: at this place, we cannot calculate the subscription type because essential data for the calculation is not persisted/available yet!
            boolean administrative = false
            if(subType == RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE)
                administrative = true

            Subscription new_sub = new Subscription(
                    type: subType,
                    kind: (subType == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL) ? RDStore.SUBSCRIPTION_KIND_CONSORTIAL : null,
                    name: params.newEmptySubName,
                    startDate: startDate,
                    endDate: endDate,
                    status: status,
                    administrative: administrative,
                    identifier: UUID.randomUUID().toString())

            if (new_sub.save()) {
                new OrgRole(org: result.institution, sub: new_sub, roleType: orgRole).save()

                if (result.consortialView){
                    List<Org> cons_members = []

                    params.list('selectedOrgs').each{ it ->
                        cons_members << Org.get(it)
                    }

                        //def cons_members = Combo.executeQuery("select c.fromOrg from Combo as c where c.toOrg = ?", [result.institution])

                    cons_members.each { cm ->
                        Subscription cons_sub = new Subscription(
                                type: subType,
                                kind: (subType == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL) ? RDStore.SUBSCRIPTION_KIND_CONSORTIAL : null,
                                name: params.newEmptySubName,
                                startDate: startDate,
                                endDate: endDate,
                                identifier: UUID.randomUUID().toString(),
                                status: status,
                                administrative: administrative,
                                instanceOf: new_sub,
                                isSlaved: true)
                        if (new_sub.administrative) {
                            new OrgRole(org: cm, sub: cons_sub, roleType: RDStore.OR_SUBSCRIBER_CONS_HIDDEN).save()
                        }
                        else {
                            new OrgRole(org: cm, sub: cons_sub, roleType: memberRole).save()
                        }
                        new OrgRole(org: result.institution, sub: cons_sub, roleType: orgRole).save()
                    }
                }
                result.newSub = new_sub
                [result:result,status:STATUS_OK]
            } else {
                new_sub.errors.each { e ->
                    log.debug("Problem creating new sub: ${e}");
                }
                result.errorMessage = new_sub.errors
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            [result:result,status:STATUS_ERROR]
        }
    }

    //--------------------------------------------- document section ----------------------------------------------

    /**
     * Gets the notes attached to the given subscription
     * @param controller unused
     * @param params the request parameter map
     * @return the list of notes for the given subscription, ERROR in case of missing grants
     */
    Map<String,Object> notes(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Gets the documents attached to the given subscription
     * @param controller unused
     * @param params the request parameter map
     * @return the list of documents for the given subscription, ERROR in case of missing grants
     */
    Map<String,Object> documents(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------- consortia members section ----------------------------------------------

    /**
     * Retrieves the members of the given consortial subscription
     * @param controller unused
     * @param params the request parameter map, containing also the filter input
     * @return the (filtered) result set, either with a basic set of information (for HTML display) or with further data (for Excel export)
     */
    Map<String,Object> members(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Profiler prf = new Profiler()
        prf.setBenchmark('init')
        if(!result)
            [result:null,status:STATUS_ERROR]
        prf.setBenchmark('before org props')
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        //result.validSubChilds = Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parent:result.subscription,subscriberRoleTypes:subscriberRoleTypes])
        prf.setBenchmark('getting filtered subscribers')
        result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
        prf.setBenchmark('after sub children')
        result.filterSet = params.filterSet ? true : false
        Set<Map<String,Object>> orgs = []
        if (params.exportXLS || params.format) {
            Map allContacts = Person.getPublicAndPrivateEmailByFunc('General contact person',result.institution)
            Map publicContacts = allContacts.publicContacts
            Map privateContacts = allContacts.privateContacts
            result.filteredSubChilds.each { row ->
                Subscription subChild = (Subscription) row.sub
                row.orgs.each { Org subscr ->
                    Map<String,Object> org = [:]
                    org.sub = subChild
                    org.name = subscr.name
                    org.sortname = subscr.sortname
                    org.globalUID = subChild.globalUID
                    org.libraryType = subscr.libraryType
                    org.libraryNetwork = subscr.libraryNetwork
                    org.funderType = subscr.funderType
                    org.region = subscr.region
                    org.country = subscr.country
                    org.startDate = subChild.startDate ? DateUtils.getLocalizedSDF_noTime().format( subChild.startDate ) : ''
                    org.endDate = subChild.endDate ? DateUtils.getLocalizedSDF_noTime().format( subChild.endDate ) : ''
                    org.isPublicForApi = subChild.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")
                    org.hasPerpetualAccess = subChild.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")
                    org.status = subChild.status
                    org.customProperties = subscr.propertySet.findAll{ OrgProperty op -> op.type.tenant == null && ((op.tenant?.id == result.institution.id && op.isPublic) || op.tenant == null) }
                    org.privateProperties = subscr.propertySet.findAll{ OrgProperty op -> op.type.tenant?.id == result.institution.id }
                    Set generalContacts = []
                    if (publicContacts.get(subscr))
                        generalContacts.addAll(publicContacts.get(subscr))
                    if (privateContacts.get(subscr))
                        generalContacts.addAll(privateContacts.get(subscr))
                    org.generalContacts = generalContacts.join("; ")
                    orgs << org
                }
            }
            result.orgs = orgs
        }
        List bm = prf.stopBenchmark()
        result.benchMark = bm
        [result:result,status:STATUS_OK]
    }

    /**
     * Opens a view containing the cost items of a subscription and comparing those with the equivalent cost items of the previous year ring,
     * i.e. from the same subscription of the last year
     * @param controller unused
     * @param params the request parameter map
     * @return a {@link Map} containing the comparison data
     */
    Map<String,Object> compareSubMemberCostItems(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]

        result.idSuffix = 'bulk'

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
        result.filterSet = params.filterSet ? true : false

        result.selectedCostItemElementID = params.selectedCostItemElementID ? Long.valueOf(params.selectedCostItemElementID) : RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id

        result.selectedCostItemElement = RefdataValue.get(result.selectedCostItemElementID)

        result.showBulkCostItems = params.showBulkCostItems ? params.showBulkCostItems : null

        String query = 'from CostItem ct where ct.costItemStatus != :status and ct.sub in (select sub from Subscription sub where sub.instanceOf = :parentSub) and ct.costItemElement is not null'

        result.costItemsByCostItemElement = CostItem.executeQuery(query, [status: RDStore.COST_ITEM_DELETED, parentSub: result.subscription]).groupBy { it.costItemElement }


        if (params.processBulkCostItems) {
            List<Long> selectedSubs = []
            params.list("selectedSubs").each { id ->
                selectedSubs << Long.parseLong(id)
            }
            if (selectedSubs && result.selectedCostItemElement) {
                List<Subscription> subscriptionList = Subscription.findAllByIdInList(selectedSubs)
                subscriptionList.each { Subscription memberSub ->
                    if (params.percentOnOldPrice) {
                        List<CostItem> previousSubCostItems
                        Subscription previousSub = memberSub._getCalculatedPreviousForSurvey()
                        if (previousSub) {
                            previousSubCostItems = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(previousSub, result.institution, result.selectedCostItemElement, RDStore.COST_ITEM_DELETED)
                        }

                        Double percentage = 1 + params.double('percentOnOldPrice') / 100
                        CostItem lastYearEquivalent = previousSubCostItems.size() == 1 ? previousSubCostItems[0] : null
                        if (lastYearEquivalent) {

                            List<CostItem> currentSubCostItems = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(memberSub, result.institution, result.selectedCostItemElement, RDStore.COST_ITEM_DELETED)

                            currentSubCostItems.each { CostItem ci ->
                                if (ci.sub) {
                                    ci.costInBillingCurrency = lastYearEquivalent.costInBillingCurrency ? BigDecimal.valueOf(lastYearEquivalent.costInBillingCurrency * percentage).setScale(2, RoundingMode.HALF_UP).toDouble() : ci.costInBillingCurrency
                                    ci.costInLocalCurrency = lastYearEquivalent.costInLocalCurrency ? BigDecimal.valueOf(lastYearEquivalent.costInLocalCurrency * percentage).setScale(2, RoundingMode.HALF_UP).toDouble() : ci.costInLocalCurrency
                                    int taxRate = 0 //fallback
                                    if (ci.taxKey)
                                        taxRate = ci.taxKey.taxRate
                                    ci.costInBillingCurrencyAfterTax = ci.costInBillingCurrency * (1.0 + (0.01 * taxRate))
                                    ci.costInLocalCurrencyAfterTax = ci.costInLocalCurrency * (1.0 + (0.01 * taxRate))
                                    ci.save()
                                }
                            }

                        }
                    } else if (params.percentOnCurrentPrice) {
                        Double percentage = 1 + params.double('percentOnCurrentPrice') / 100

                        List<CostItem> currentSubCostItems = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(memberSub, result.institution, result.selectedCostItemElement, RDStore.COST_ITEM_DELETED)

                        currentSubCostItems.each { CostItem ci ->
                            if (ci.sub) {
                                ci.costInBillingCurrency = ci.costInBillingCurrency ? BigDecimal.valueOf(ci.costInBillingCurrency * percentage).setScale(2, RoundingMode.HALF_UP).toDouble() : ci.costInBillingCurrency
                                ci.costInLocalCurrency = ci.costInLocalCurrency ? BigDecimal.valueOf(ci.costInLocalCurrency * percentage).setScale(2, RoundingMode.HALF_UP).toDouble() : ci.costInLocalCurrency
                                int taxRate = 0 //fallback
                                if (ci.taxKey)
                                    taxRate = ci.taxKey.taxRate
                                ci.costInBillingCurrencyAfterTax = ci.costInBillingCurrency * (1.0 + (0.01 * taxRate))
                                ci.costInLocalCurrencyAfterTax = ci.costInLocalCurrency * (1.0 + (0.01 * taxRate))
                                ci.save()
                            }
                        }
                    }
                }
            }

        }

        [result:result,status:STATUS_OK]
    }

    /**
     * Lists institutions and sets default parameters for the member adding form
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String,Object> addMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
            //the following two are arguments for a g.message-call on the view which expects an Object[]
            result.superOrgType = [messageSource.getMessage('consortium.superOrgType',null,locale)]
            result.memberType = [messageSource.getMessage('consortium.subscriber',null,locale)]
            result.propList= PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
            FilterService.Result fsr = filterService.getOrgComboQuery(params, result.institution as Org)
            if (fsr.isFilterSet) { params.filterSet = true }

            result.members = Org.executeQuery(fsr.query, fsr.queryParams, params)
            result.members_disabled = Subscription.executeQuery("select oo.org.id from OrgRole oo join oo.sub s where s.instanceOf = :io",[io: result.subscription])
            result.validPackages = result.subscription.packages?.sort { it.pkg.name }
            result.memberLicenses = License.executeQuery("select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)",[subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE])
            if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_ADMINISTRATIVE,CalculatedType.TYPE_CONSORTIAL]) {
                List<Subscription> childSubs = result.subscription.getNonDeletedDerivedSubscriptions()
                if(childSubs) {
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )

                    result.memberProperties = memberProperties
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Takes the submitted parameters and creates member subscription instances based on the given input
     * @param controller unused
     * @param params the input parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> processAddMembers(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        /*
        continue here with following procedure:
        - packages to process: collect in map
        - process that map on controller level for ensuring new transaction ==> make tabula rasa
        - implement holding selection switch and remove then the checkbox in the view
         */
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                RefdataValue subStatus = RefdataValue.get(params.subStatus) ?: RDStore.SUBSCRIPTION_CURRENT

                if (result.editable) {
                    List<Org> members = []
                    Map startEndDates = [:]

                    if(params.selectSubMembersWithImport?.filename){

                        MultipartFile importFile = params.selectSubMembersWithImport
                        InputStream stream = importFile.getInputStream()

                        result.selectSubMembersWithImport = subscriptionService.selectSubMembersWithImport(stream)

                        if(result.selectSubMembersWithImport.orgList){
                            result.selectSubMembersWithImport.orgList.each { it ->
                                members << Org.findById(Long.valueOf(it.orgId))
                                startEndDates.put("${it.orgId}", [startDate: it.startDate, endDate: it.endDate])
                            }
                        }


                    }else {
                        params.list('selectedOrgs').each { it ->
                            members << Org.findById(Long.valueOf(it))
                        }
                    }
                    List<String> excludes = PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key }
                    //excludes << 'freezeHolding'
                    excludes.add(PendingChangeConfiguration.TITLE_REMOVED)
                    excludes.add(PendingChangeConfiguration.TITLE_REMOVED+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    excludes.add(PendingChangeConfiguration.TITLE_DELETED)
                    excludes.add(PendingChangeConfiguration.TITLE_DELETED+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    excludes.addAll(PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key+PendingChangeConfiguration.NOTIFICATION_SUFFIX})
                    Set<AuditConfig> inheritedAttributes


                    Map<Long, List<Long>> memberSubIdsByParentSub = [:]
                    //needed for that the subscriptions are present in the moment of the parallel process
                    List<Subscription> subscriptions = [result.subscription]

                    if(members.size() > 0) {
                        Set<Subscription> nextSubs = linksGenerationService.getSuccessionChain(result.subscription, 'destinationSubscription')
                        nextSubs.each { Subscription nextSub ->
                            if (params.checkSubRunTimeMultiYear && params.containsKey('addToSubWithMultiYear_' + nextSub.id)) {
                                subscriptions << nextSub
                            }
                        }
                        subscriptions.eachWithIndex { Subscription currParent, int c ->
                            List<Long> memberSubIds = []
                            List<Subscription> synShareTargetList = []
                            List<License> licensesToProcess = []
                            if (params["generateSlavedLics_${currParent.id}"] == "all") {
                                String queryLic = "select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)"
                                licensesToProcess.addAll(License.executeQuery(queryLic, [subscription: result.subscription, linkType: RDStore.LINKTYPE_LICENSE]))
                            }
                            else if (params["generateSlavedLics_${currParent.id}"] == "partial") {
                                List<String> licenseKeys = params.list("generateSlavedLics_${currParent.id}")
                                licenseKeys.each { String licenseKey ->
                                    licensesToProcess << genericOIDService.resolveOID(licenseKey)
                                }
                            }

                            inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name, currParent.id, excludes)
                            members.each { Org cm ->
                                int existSubForOrg = Subscription.executeQuery("select count(*) from OrgRole oo join oo.sub s where s.instanceOf = :sub and oo.org = :org", [sub: currParent, org: cm])[0]
                                if (existSubForOrg == 0) {
                                    Date startDate = params.valid_from ? DateUtils.parseDateGeneric(params.valid_from) : null
                                    Date endDate = params.valid_to ? DateUtils.parseDateGeneric(params.valid_to) : null
                                    Year referenceYear = params.reference_year ? Year.parse(params.reference_year) : null

                                    if(startEndDates){
                                        Map startAndEndDate = startEndDates.get("${cm.id}")
                                        if(startAndEndDate) {
                                            startDate = startAndEndDate.startDate ?: startDate
                                            endDate = startAndEndDate.endDate ?: endDate
                                        }
                                    }
                                    Subscription memberSub = new Subscription(
                                            type: currParent.type ?: null,
                                            kind: currParent.kind ?: null,
                                            status: c == 0 ? subStatus : currParent.status,
                                            name: currParent.name,
                                            //name: result.subscription.name + " (" + (cm.get(0).sortname ?: cm.get(0).name) + ")",
                                            startDate: c == 0 ? startDate : currParent.startDate,
                                            endDate: c == 0 ? endDate : currParent.endDate,
                                            referenceYear: c == 0 ? referenceYear : currParent.referenceYear,
                                            administrative: currParent._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                                            manualRenewalDate: currParent.manualRenewalDate,
                                            /* manualCancellationDate: result.subscription.manualCancellationDate, */
                                            holdingSelection: currParent.holdingSelection ?: null,
                                            identifier: UUID.randomUUID().toString(),
                                            instanceOf: currParent,
                                            isSlaved: true,
                                            resource: currParent.resource ?: null,
                                            form: currParent.form ?: null,
                                            isMultiYear: params.checkSubRunTimeMultiYear ?: false
                                    )
                                    inheritedAttributes.each { attr ->
                                        memberSub[attr.referenceField] = currParent[attr.referenceField]
                                    }
                                    if (!memberSub.save()) {
                                        memberSub.errors.each { e ->
                                            log.debug("Problem creating new sub: ${e}")
                                        }
                                        result.error = memberSub.errors
                                    }
                                    if (memberSub) {
                                        if (currParent._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE) {
                                            new OrgRole(org: cm, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS_HIDDEN).save()
                                        } else {
                                            new OrgRole(org: cm, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS).save()
                                        }
                                        new OrgRole(org: result.institution, sub: memberSub, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIUM).save()
                                        synShareTargetList.add(memberSub)
                                        SubscriptionProperty.findAllByOwner(currParent).each { SubscriptionProperty sp ->
                                            AuditConfig ac = AuditConfig.getConfig(sp)
                                            if (ac) {
                                                // multi occurrence props; add one additional with backref
                                                if (sp.type.multipleOccurrence) {
                                                    SubscriptionProperty additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                                    additionalProp = sp.copyInto(additionalProp)
                                                    additionalProp.instanceOf = sp
                                                    additionalProp.save()
                                                } else {
                                                    // no match found, creating new prop with backref
                                                    SubscriptionProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                                    newProp = sp.copyInto(newProp)
                                                    newProp.instanceOf = sp
                                                    newProp.save()
                                                }
                                            }
                                        }
                                        Identifier.findAllBySub(currParent).each { Identifier id ->
                                            AuditConfig ac = AuditConfig.getConfig(id)
                                            if (ac) {
                                                Identifier.constructWithFactoryResult([value: id.value, parent: id, reference: memberSub, namespace: id.ns])
                                            }
                                        }
                                        memberSub = memberSub.refresh()
                                        licensesToProcess.each { License lic ->
                                            subscriptionService.setOrgLicRole(memberSub, lic, false)
                                        }

                                        if (cm.isCustomerType_Inst_Pro()) {
                                            PendingChange.construct([target: memberSub, oid: "${memberSub.getClass().getName()}:${memberSub.id}", msgToken: "pendingChange.message_SU_NEW_03", status: RDStore.PENDING_CHANGE_PENDING, owner: cm])
                                        }

                                        if (c == 0) {
                                            params.list('propRow').each { String rowKey ->
                                                if (params.containsKey('propValue' + rowKey) && params["propValue${rowKey}"] != "") {
                                                    PropertyDefinition propDef = PropertyDefinition.get(params["propId${rowKey}"])
                                                    String propValue = params["propValue${rowKey}"] as String
                                                    if (propDef.isRefdataValueType())
                                                        propValue = RefdataValue.class.name + ':' + propValue
                                                    subscriptionService.createProperty(propDef, memberSub, (Org) result.institution, propValue, params["propNote${rowKey}"] as String)
                                                }
                                            }
                                            if (params.customerIdentifier || params.requestorKey) {
                                                result.subscription.packages.each { SubscriptionPackage sp ->
                                                    CustomerIdentifier ci = new CustomerIdentifier(customer: cm, type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT, value: params.customerIdentifier, requestorKey: params.requestorKey, platform: sp.pkg.nominalPlatform, owner: result.institution, isPublic: true)
                                                    if (!ci.save())
                                                        log.error(ci.errors.getAllErrors().toListString())
                                                }
                                            }
                                        }

                                        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(currParent)
                                        Subscription prevMemberSub = (links.prevLink && links.prevLink.size() > 0) ? links.prevLink[0].getDerivedSubscriptionForNonHiddenSubscriber(cm) : null
                                        Subscription nextMemberSub = (links.nextLink && links.nextLink.size() > 0) ? links.nextLink[0].getDerivedSubscriptionForNonHiddenSubscriber(cm) : null
                                        try {
                                            if (prevMemberSub) {
                                                Links.construct([source: memberSub, destination: prevMemberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                                            }
                                            if (nextMemberSub) {
                                                Links.construct([source: nextMemberSub, destination: memberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                                            }
                                        }
                                        catch (CreationException e) {
                                            log.error("Problem linking to subscription: ${e.getStackTrace()}")
                                        }

                                        memberSubIds << memberSub.id
                                    }
                                }
                                //}
                            }

                            if (synShareTargetList)
                                currParent.syncAllShares(synShareTargetList)

                            if (memberSubIds) {
                                memberSubIdsByParentSub.put(currParent.id, memberSubIds)
                            }
                        }

                        Map<Long, Set<Package>> packagesToProcess = [:]
                        subscriptions.each { Subscription currParent ->
                            currParent = currParent.refresh()
                            Set<Package> packagesToProcessCurParent = []
                            if (params["linkAllPackages_${currParent.id}"]) {
                                currParent.packages.each { SubscriptionPackage sp ->
                                    packagesToProcessCurParent << sp.pkg
                                }
                            } else if (params["packageSelection_${currParent.id}"]) {
                                List packageIds = params.list("packageSelection_${currParent.id}")
                                packageIds.each { spId ->
                                    packagesToProcessCurParent << SubscriptionPackage.get(spId).pkg
                                }
                            }
                            if (packagesToProcessCurParent) {
                                packagesToProcess.put(currParent.id, packagesToProcessCurParent)
                            }
                        }
                        if(packagesToProcess)
                            result.packagesToProcess = packagesToProcess
                        result.parentSubscriptions = subscriptions
                        result.memberSubscriptions = memberSubIdsByParentSub
                    }
                } else {
                    [result:result,status:STATUS_ERROR]
                }
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Inserts afterwards a succession link between two member subscription; this procedure is used
     * if two subscription year rings have been inserted without taking the renewal procedure (upon initial
     * data insertion for example)
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the result map if the creation was successful, ERROR otherwise
     */
    Map<String,Object> linkNextPrevMemberSub(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Subscription memberSub = Subscription.get(params.long('memberSubID'))
            Org org = Org.get(params.long('memberOrg'))
            Subscription prevMemberSub = (result.navPrevSubscription.size() > 0) ? result.navPrevSubscription[0].getDerivedSubscriptionForNonHiddenSubscriber(org) : null
            Subscription nextMemberSub = (result.navNextSubscription.size() > 0) ? result.navNextSubscription[0].getDerivedSubscriptionForNonHiddenSubscriber(org) : null
            try {
                Links link
                if(params.prev && prevMemberSub) {
                    link = Links.construct([source: memberSub, destination: prevMemberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                }
                if(params.next && nextMemberSub) {
                    link = Links.construct([source: nextMemberSub, destination: memberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                }
                if(link) {
                    [result:result,status:STATUS_OK]
                }
                else [result:result,status:STATUS_ERROR]
            }
            catch (CreationException e) {
                log.error("Problem linking to subscription: ${e.getStackTrace()}")
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    /**
     * Manages the call for the given member management view, setting generic parameters
     * @param controller the controller instance
     * @param params the request parameter map
     * @param input_file a document file to be uploaded to several instances
     * @return OK if the permission grant check was successful, ERROR otherwise
     */
    Map<String,Object> membersSubscriptionsManagement(SubscriptionController controller, GrailsParameterMap params, input_file) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            params.tab = params.tab ?: 'generalProperties'

            result << managementService.subscriptionsManagement(controller, params, input_file)

            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------------- survey section -------------------------------------------

    /**
     * Lists the surveys related the given member subscription
     * @param controller unused
     * @param params the request parameter map
     * @return a list of surveys linked to the given member subscription
     */
    Map<String,Object> surveys(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.surveys = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                    [sub: result.subscription.instanceOf,
                     org: result.subscription.getSubscriberRespConsortia(),
                     invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]])

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Lists the surveys related to the given consortial subscription
     * @param controller unused
     * @param params the request parameter map
     * @return a list of surveys linked to the given consortial subscription
     */
    Map<String,Object> surveysConsortia(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.surveys = result.subscription ? SurveyConfig.findAllBySubscription(result.subscription) : null

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Initialises the title renewal process by loading the title selection view for the given member. The list of
     * selectable titles can be exported along with usage data also as an Excel worksheet which then may be reuploaded
     * again
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the result map containing defaults in case of success, ERROR otherwise
     */
    @Deprecated
    Map<String,Object> renewEntitlementsWithSurvey(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)

            Subscription subscriberSub = result.subscription
            result.institution = contextService.getOrg()
            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.surveyInfo = result.surveyConfig.surveyInfo

            Subscription previousSubscription = subscriberSub._getCalculatedPreviousForSurvey()
            Subscription baseSub = result.surveyConfig.subscription ?: subscriberSub.instanceOf
            result.subscriber = subscriberSub.getSubscriberRespConsortia()

            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, subscriberSub)
            if (!issueEntitlementGroup) {
                String groupName = IssueEntitlementGroup.countBySubAndName(subscriberSub,  result.surveyConfig.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(subscriberSub, result.surveyConfig.issueEntitlementGroupName) + 1) : result.surveyConfig.issueEntitlementGroupName
                issueEntitlementGroup = new IssueEntitlementGroup(surveyConfig: result.surveyConfig, sub: subscriberSub, name: groupName).save()
            }

            result.titleGroupID = issueEntitlementGroup ? issueEntitlementGroup.id.toString() : null
            result.titleGroup = issueEntitlementGroup

            params.tab = params.tab ?: 'allTipps'

            result.preselectValues = params.preselectValues == 'on'

            //result.subscriptionIDs = []

            Set<Subscription> subscriptions = []
            if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                subscriptions = linksGenerationService.getSuccessionChain(subscriberSub, 'sourceSubscription')
                //subscriptions << subscriberSub
                //result.subscriptionIDs = surveyService.subscriptionsOfOrg(result.subscriber)
            }
            else {
                //subscriptions << previousSubscription
                subscriptions << subscriberSub

            }

            if(!params.exportForImport) {
                result.preselectValues = params.preselectValues == 'on'

                //result.subscriptionIDs = []

                result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)
                //result.showStatisticByParticipant = surveyService.showStatisticByParticipant(result.surveyConfig.subscription, result.subscriber)

                result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(subscriberSub, result.surveyConfig)
                result.countAllTipps = baseSub.packages ? TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.status = :status and pkg in (:pkgs)", [pkgs: baseSub.packages.pkg, status: RDStore.TIPP_STATUS_CURRENT])[0] : 0


                if (result.editable) {
                    EhcacheWrapper userCache = contextService.getUserCache("/subscription/renewEntitlementsWithSurvey/${subscriberSub.id}?${params.tab}")
                    Map<String, Object> checkedCache = userCache.get('selectedTitles')

                    if (!checkedCache || !params.containsKey('pagination')) {
                        checkedCache = ["checked": [:]]
                    }

                    result.checkedCache = checkedCache.get('checked')
                    result.checkedCount = result.checkedCache.findAll { it.value == 'checked' }.size()

                    result.allChecked = ""
                    if (params.tab == 'allTipps' && result.countAllTipps > 0 && result.countAllTipps == result.checkedCount) {
                        result.allChecked = "checked"
                    }
                    if (params.tab == 'selectedIEs' && result.countSelectedIEs > 0 && result.countSelectedIEs == result.checkedCount) {
                        result.allChecked = "checked"
                    }
                }

                /* if (params.hasPerpetualAccess) {
                    params.hasPerpetualAccessBySubs = subscriptions
                }*/

                List<Long> sourceIEs = []
                if (params.tab == 'allTipps') {
                    params.status = [RDStore.TIPP_STATUS_CURRENT.id]
                    params.sort = params.sort ?: 'tipp.sortname'
                    params.order = params.order ?: 'asc'
                    Map<String, Object> query = filterService.getTippQuery(params, baseSub.packages.pkg)
                    result.filterSet = query.filterSet
                    List<Long> titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)

                    //sql bridge
                    Sql sql = GlobalService.obtainSqlConnection()
                    String mainQry = "select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk = any(:tippIDs)"
                    result.tippsListPriceSumEUR = sql.rows('select sum(pi.pi_list_price) as list_price_eur from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_EUR.id, tippIDs: sql.getDataSource().getConnection().createArrayOf('bigint', titlesList as Object[])])[0]['list_price_eur']
                    result.tippsListPriceSumUSD = sql.rows('select sum(pi.pi_list_price) as list_price_usd from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_USD.id, tippIDs: sql.getDataSource().getConnection().createArrayOf('bigint', titlesList as Object[])])[0]['list_price_usd']
                    result.tippsListPriceSumGBP = sql.rows('select sum(pi.pi_list_price) as list_price_gbp from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_GBP.id, tippIDs: sql.getDataSource().getConnection().createArrayOf('bigint', titlesList as Object[])])[0]['list_price_gbp']
                    sql.close()
                    /*
                    result.tippsListPriceSumEUR = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.tipp tipp ' +
                            'where p.listPrice is not null and p.listCurrency = :currency and tipp.status.id = :tiStatus and tipp.id in (' + query.query + ' )', [currency: RDStore.CURRENCY_EUR, tiStatus: RDStore.TIPP_STATUS_CURRENT.id] + query.queryParams)[0] ?: 0

                    result.tippsListPriceSumUSD = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.tipp tipp ' +
                            'where p.listPrice is not null and p.listCurrency = :currency and tipp.status.id = :tiStatus and tipp.id in (' + query.query + ' )', [currency: RDStore.CURRENCY_USD, tiStatus: RDStore.TIPP_STATUS_CURRENT.id] + query.queryParams)[0] ?: 0

                    result.tippsListPriceSumGBP = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.tipp tipp ' +
                            'where p.listPrice is not null and p.listCurrency = :currency and tipp.status.id = :tiStatus and tipp.id in (' + query.query + ' )', [currency: RDStore.CURRENCY_GBP, tiStatus: RDStore.TIPP_STATUS_CURRENT.id] + query.queryParams)[0] ?: 0
                    */

                    result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList.drop(result.offset).take(result.max), [sort: params.sort, order: params.order]) : []
                    result.num_rows = titlesList.size()

                    if (baseSub.packages) {
                        result.packageInstance = baseSub.packages.pkg[0]
                    }

                } else if (params.tab == 'selectedIEs') {
                    if (issueEntitlementGroup) {
                        params.sort = params.sort ?: 'tipp.sortname'
                        params.order = params.order ?: 'asc'

                        // params.currentIEs = 'currentIEs' for subTabs with UITagLib tabsItem
                        if(params.subTab){
                            if(params.subTab == 'currentIEs'){
                                params.currentIEs = 'currentIEs'
                                params.status = [RDStore.TIPP_STATUS_CURRENT.id]
                            }else if(params.subTab == 'plannedIEs'){
                                params.plannedIEs = 'plannedIEs'
                                params.status = [RDStore.TIPP_STATUS_EXPECTED.id]
                            }else if(params.subTab == 'expiredIEs'){
                                params.expiredIEs = 'expiredIEs'
                                params.status = [RDStore.TIPP_STATUS_RETIRED.id]
                            }else if(params.subTab == 'deletedIEs'){
                                params.deletedIEs = 'deletedIEs'
                                params.status = [RDStore.TIPP_STATUS_DELETED.id]
                            }else if(params.subTab == 'allIEs'){
                                params.allIEs = 'allIEs'
                                params.status = [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_EXPECTED.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id]
                            }
                        } else{
                                params.currentIEs = 'currentIEs'
                                params.status = [RDStore.TIPP_STATUS_CURRENT.id]
                        }
                        result.listOfStatus = Params.getRefdataList(params, 'status')

                        params.titleGroup = result.titleGroupID
                        Map query = filterService.getIssueEntitlementQuery(params, subscriberSub)
                        sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)

                        result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs, [sort: params.sort, order: params.order, offset: result.offset, max: result.max]) : []
                        result.num_rows = sourceIEs ? IssueEntitlement.countByIdInList(sourceIEs) : 0
                        //sql bridge
                        Sql sql = GlobalService.obtainSqlConnection()
                        String mainQry = "select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk in (select ie_tipp_fk from issue_entitlement where ie_id = any(:ieIDs))"
                        result.iesTotalListPriceSumEUR = sql.rows('select sum(pi.pi_list_price) as list_price_eur from (' +
                                mainQry +
                                ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_EUR.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_eur']
                        result.iesTotalListPriceSumUSD = sql.rows('select sum(pi.pi_list_price) as list_price_usd from (' +
                                mainQry +
                                ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_USD.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_usd']
                        result.iesTotalListPriceSumGBP = sql.rows('select sum(pi.pi_list_price) as list_price_gbp from (' +
                                mainQry +
                                ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_GBP.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_gbp']
                        sql.close()
                        /*
                        result.iesTotalListPriceSumEUR = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                                'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_EUR, ieIDs: sourceIEs])[0] ?: 0

                        result.iesTotalListPriceSumUSD = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                                'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_USD, ieIDs: sourceIEs])[0] ?: 0

                        result.iesTotalListPriceSumGBP = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                                'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_GBP, ieIDs: sourceIEs])[0] ?: 0
                        */

                        List counts = IssueEntitlement.executeQuery('select new map(count(*) as count, status as status) from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup.id = :titleGroup and iegi.ie = ie) group by status', [titleGroup: Long.parseLong(result.titleGroupID), sub: subscriberSub, ieStatus: RDStore.TIPP_STATUS_REMOVED])
                        result.allIECounts = 0
                        result.currentIECounts = 0
                        result.plannedIECounts = 0
                        result.expiredIECounts = 0
                        result.deletedIECounts = 0

                        counts.each { row ->
                            switch (row['status']) {
                                case RDStore.TIPP_STATUS_CURRENT: result.currentIECounts = row['count']
                                    break
                                case RDStore.TIPP_STATUS_EXPECTED: result.plannedIECounts = row['count']
                                    break
                                case RDStore.TIPP_STATUS_RETIRED: result.expiredIECounts = row['count']
                                    break
                                case RDStore.TIPP_STATUS_DELETED: result.deletedIECounts = row['count']
                                    break
                            }
                            result.allIECounts += row['count']
                        }
                    }
                } else if (params.tab == 'currentPerpetualAccessIEs') {
                    params.sort = params.sort ?: 'tipp.sortname'
                    params.order = params.order ?: 'asc'
                    GrailsParameterMap parameterMap = params.clone()
                    Map query = [:]
                    if (subscriptions) {
                        parameterMap.status = [RDStore.TIPP_STATUS_CURRENT.id]
                        parameterMap.hasPerpetualAccess = RDStore.YN_YES.id
                        query = filterService.getIssueEntitlementQuery(parameterMap, subscriptions)
                        //List<Long> previousIes = previousSubscription ? IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams) : []
                        sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
                    }
                    result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs, [sort: params.sort, order: params.order, offset: result.offset, max: result.max]) : []
                    result.num_rows = sourceIEs ? IssueEntitlement.countByIdInList(sourceIEs) : 0

                    //sql bridge
                    Sql sql = GlobalService.obtainSqlConnection()
                    String mainQry = "select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk in (select ie_tipp_fk from issue_entitlement where ie_id = any(:ieIDs))"
                    result.iesTotalListPriceSumEUR = sql.rows('select sum(pi.pi_list_price) as list_price_eur from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_EUR.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_eur']
                    result.iesTotalListPriceSumUSD = sql.rows('select sum(pi.pi_list_price) as list_price_usd from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_USD.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_usd']
                    result.iesTotalListPriceSumGBP = sql.rows('select sum(pi.pi_list_price) as list_price_gbp from (' +
                            mainQry +
                            ') as pi where pi.rn = 1', [currency: RDStore.CURRENCY_GBP.id, ieIDs: sql.getDataSource().getConnection().createArrayOf('bigint', sourceIEs as Object[])])[0]['list_price_gbp']
                    sql.close()
                    /*
                    result.iesTotalListPriceSumEUR = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                            'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_EUR, ieIDs: sourceIEs])[0] ?: 0

                    result.iesTotalListPriceSumUSD = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                            'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_USD, ieIDs: sourceIEs])[0] ?: 0

                    result.iesTotalListPriceSumGBP = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency ' +
                            'and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.id in (:ieIDs))', [currency: RDStore.CURRENCY_GBP, ieIDs: sourceIEs])[0] ?: 0
                    */
                }

                /*
                <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                                ${surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subParticipant, surveyConfig)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:if>
                            <g:else>
                                ${subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subParticipant, ieGroup)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:else>
                 */
                if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                    result.countCurrentPermanentTitles = surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subscriberSub, result.surveyConfig)
                }
                else {
                    result.countCurrentPermanentTitles = issueEntitlementGroup ? subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subscriberSub, issueEntitlementGroup) : 0
                }


/*            if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
            } else {
                result.countCurrentIEs = subscriptionService.countCurrentIssueEntitlements(result.subscription)
            }*/
            }

            result.subscriberSub = subscriberSub
            result.parentSubscription = baseSub
            //result.allSubscriptions = subscriptions
            result.previousSubscription = previousSubscription


            if(result.surveyInfo.owner.id ==  contextService.getOrg().id) {
                result.participant = result.subscriber
            }

            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)

            [result:result,status:STATUS_OK]
        }
    }

    //-------------------------------------- packages section ------------------------------------------

    /**
     * Queries the we:kb ElasticSearch index and returns a (filtered) list of packages which may be linked to the
     * given subscription
     * @param controller unused
     * @param params the request parameter map, containing also filter parameters to limit the package results
     * @return a filtered list of packages
     * @see ApiSource
     * @see Package
     */
    Map<String,Object> linkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            if(subscriptionService.checkThreadRunning('PackageTransfer_'+result.subscription.id) && !SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,Package.findByGokbId(params.addUUID))) {
                result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running.withPackage',[subscriptionService.getCachedPackageName('PackageTransfer_'+result.subscription.id)] as Object[], LocaleUtils.getCurrentLocale())
                result.bulkProcessRunning = true
            }
            if (result.subscription.packages) {
                result.pkgs = []
                    result.subscription.packages.each { sp ->
                        log.debug("Existing package ${sp.pkg.name} (Adding GOKb ID: ${sp.pkg.gokbId})")
                        result.pkgs.add(sp.pkg.gokbId)
                }
            } else {
                log.debug("Subscription has no linked packages yet")
            }
            result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)
            result.putAll(packageService.getWekbPackages(params))
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Takes the submitted parameters and links the given package to the given subscription. Consortia may link
     * the package on parent level only or also on member level; along with the linking, changes handling (the
     * pending change configuration) is also set up. If the package did not exist in the app on the moment of linking,
     * the data will be fetched from we:kb. The overall procedure may take time; it is thus deployed onto a parallel
     * process
     * @param controller unused
     * @param params the input parameter map
     * @return OK if the linking was successful, ERROR otherwise
     */
    Map<String,Object> processLinkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            boolean bulkProcessRunning = false
            if(params.addUUID && !bulkProcessRunning) {
                boolean createEntitlements = params.createEntitlements == 'on',
                linkToChildren = params.linkToChildren == 'on',
                createEntitlementsForChildren = params.createEntitlementsForChildren == 'on'
                String pkgUUID = params.addUUID
                result.source = ApiSource.getCurrent().baseUrl
                RefdataValue holdingSelection = RefdataValue.get(params.holdingSelection)
                if(params.holdingSelection) {
                    holdingSelection = RefdataValue.get(params.holdingSelection)
                    result.subscription.holdingSelection = holdingSelection
                    result.subscription.save()
                }
                else {
                    holdingSelection = GrailsHibernateUtil.unwrapIfProxy(result.subscription.holdingSelection)
                }
                result.holdingSelection = holdingSelection
                GlobalRecordSource source = GlobalRecordSource.findByUriLikeAndRectype(result.source+'%', GlobalSourceSyncService.RECTYPE_TIPP)
                log.debug("linkPackage. Global Record Source URL: " +source.uri)
                globalSourceSyncService.source = source
                globalSourceSyncService.defineMapFields()

                //to be deployed in parallel thread
                executorService.execute({
                    Thread.currentThread().setName("PackageTransfer_"+result.subscription.id)
                    subscriptionService.cachePackageName("PackageTransfer_"+result.subscription.id, params.pkgName)
                    long start = System.currentTimeSeconds()
                    if(!Package.findByGokbId(pkgUUID)) {
                        try {
                            Map<String,Object> queryResult = globalSourceSyncService.fetchRecordJSON(false,[componentType:'TitleInstancePackagePlatform',tippPackageUuid:pkgUUID,max:GlobalSourceSyncService.MAX_TIPP_COUNT_PER_PAGE,sort:'lastUpdated'])
                            if(queryResult.error && queryResult.error == 404) {
                                log.error("we:kb server currently unavailable")
                            }
                            else {
                                Package.withNewTransaction {
                                    if(queryResult.records && queryResult.count > 0) {
                                        if(queryResult.count >= GlobalSourceSyncService.MAX_TIPP_COUNT_PER_PAGE)
                                            globalSourceSyncService.processScrollPage(queryResult, 'TitleInstancePackagePlatform', null, pkgUUID)
                                        else
                                            globalSourceSyncService.updateRecords(queryResult.records, 0)
                                    }
                                    else {
                                        globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                                    }
                                }
                                Package pkgToLink = Package.findByGokbId(pkgUUID)
                                subscriptionService.addToSubscription(result.subscription, pkgToLink, createEntitlements)
                                if(linkToChildren) {
                                    if(holdingSelection == RDStore.SUBSCRIPTION_HOLDING_PARTIAL && auditService.getAuditConfig(result.subscription, 'holdingSelection')) {
                                        Subscription.findAllByInstanceOf(result.subscription).each { Subscription member ->
                                            subscriptionService.addToSubscriptionCurrentStock(member, result.subscription, pkgToLink, true)
                                        }
                                    }
                                    else
                                        subscriptionService.addToMemberSubscription(result.subscription, Subscription.findAllByInstanceOf(result.subscription), pkgToLink, createEntitlementsForChildren)
                                }
                            }
                        }
                        catch (Exception e) {
                            log.error("sync job has failed, please consult stacktrace as follows: ")
                            e.printStackTrace()
                        }
                    }
                    else {
                        Package pkgToLink = globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                        subscriptionService.cachePackageName("PackageTransfer_"+result.subscription.id, pkgToLink.name)
                        subscriptionService.addToSubscription(result.subscription, pkgToLink, createEntitlements)
                        if(linkToChildren) {
                            if(holdingSelection == RDStore.SUBSCRIPTION_HOLDING_PARTIAL && auditService.getAuditConfig(result.subscription, 'holdingSelection')) {
                                Subscription.findAllByInstanceOf(result.subscription).each { Subscription member ->
                                    subscriptionService.addToSubscriptionCurrentStock(member, result.subscription, pkgToLink, true)
                                }
                            }
                            else
                                subscriptionService.addToMemberSubscription(result.subscription, Subscription.findAllByInstanceOf(result.subscription), pkgToLink, createEntitlementsForChildren)
                        }
                    }
                    if(System.currentTimeSeconds()-start >= GlobalService.LONG_PROCESS_LIMBO) {
                        globalService.notifyBackgroundProcessFinish(result.user.id, "PackageTransfer_${result.subscription.id}", messageSource.getMessage('subscription.details.linkPackage.thread.completed', [result.subscription.name] as Object[], LocaleUtils.getCurrentLocale()))
                    }
                })
            }

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Unlinks the given package from the given subscription, eliminating eventual dependent objects. The unlinking
     * is taking place if the call has been confirmed
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the result of call in case of success (this may be the list of conflicts when the confirm has not been sent yet)
     * or ERROR in case of an error
     */
    Map<String,Object> unlinkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.package = Package.get(params.package)
        Locale locale = LocaleUtils.getCurrentLocale()
        boolean unlinkPkg
        if(params.confirmed && !subscriptionService.checkThreadRunning('PackageUnlink_'+result.subscription.id)) {
            Set<Subscription> subList = []
            if(params.containsKey('option')) {
                AuditConfig.removeConfig(result.subscription, 'holdingSelection')
                subList << result.subscription
                if(params.option in ['childWithIE', 'childOnlyIE'])
                    subList.addAll(Subscription.findAllByInstanceOf(result.subscription))
                unlinkPkg = params.option in ['withIE', 'childWithIE']
            }
            else {
                unlinkPkg = true
                subList << result.subscription
                subList.addAll(Subscription.findAllByInstanceOf(result.subscription))
            }
            executorService.execute({
                String threadName = 'PackageUnlink_'+result.subscription.id
                Thread.currentThread().setName(threadName)
                long start = System.currentTimeSeconds()
                if(packageService.unlinkFromSubscription(result.package, subList.id, result.institution, unlinkPkg)){
                    result.message = messageSource.getMessage('subscription.details.unlink.successfully',null,locale)
                    if(System.currentTimeSeconds()-start >= GlobalService.LONG_PROCESS_LIMBO) {
                        globalService.notifyBackgroundProcessFinish(result.user.id, threadName, messageSource.getMessage('subscription.details.unlink.thread.completed', [result.subscription.name] as Object[], locale))
                    }
                }else {
                    result.error = messageSource.getMessage('subscription.details.unlink.notSuccessfully',null,locale)
                }
            })
            [result:result,status:STATUS_OK]
        }
        else {
            String query = "select ie.id from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
            Map<String,Object> queryParams = [sub: result.subscription, pkg_id: result.package.id]
            int numOfPCs = packageService.removePackagePendingChanges(result.package, [result.subscription.id], false)
            int numOfIEs = IssueEntitlement.executeQuery(query, queryParams).size()
            SubscriptionPackage subscriptionPackage = SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,result.package)
            int numOfCIs = CostItem.findAllBySubAndPkg(subscriptionPackage.subscription, subscriptionPackage.pkg).size()
            List conflictsList = packageService.listConflicts(result.package,result.subscription,numOfPCs,numOfIEs,numOfCIs)
            //Automatisch Paket entknüpfen, wenn das Paket in der Elternlizenz entknüpft wird
            if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]){
                List<Subscription> childSubs = Subscription.findAllByInstanceOf(result.subscription)
                if (childSubs) {
                    String queryChildSubs = "select ie.id from IssueEntitlement ie, Package pkg where ie.subscription in (:sub) and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) and ie.status != :removed"
                    Map<String,Object> queryParamChildSubs = [sub: childSubs, pkg_id: result.package.id, removed: RDStore.TIPP_STATUS_REMOVED]
                    List childSubsPackages = SubscriptionPackage.findAllBySubscriptionInListAndPkg(childSubs, result.package)
                    int numOfPCsChildSubs = packageService.removePackagePendingChanges(result.package, childSubs.id, false)
                    int numOfIEsChildSubs = IssueEntitlement.executeQuery(queryChildSubs, queryParamChildSubs).size()
                    int numOfCIsChildSubs = childSubsPackages ? CostItem.executeQuery('select count(*) from CostItem ci where ci.sub in (:childSubs) and ci.pkg = :pkg and ci.owner != :ctx and ci.costItemStatus != :deleted', [pkg: result.package, childSubs: childSubs, deleted: RDStore.COST_ITEM_DELETED, ctx: result.institution])[0] : 0
                    if(numOfPCsChildSubs > 0 || numOfIEsChildSubs > 0 || numOfCIsChildSubs > 0) {
                        conflictsList.addAll(packageService.listConflicts(result.package, childSubs, numOfPCsChildSubs, numOfIEsChildSubs, numOfCIsChildSubs))
                    }
                }
            }
            result.conflict_list = conflictsList
            [result:result,status:STATUS_OK]
        }
    }

    //-------------------------------- issue entitlements holding --------------------------------------

    /**
     * Retrieves the current subscription holding; the list may be filtered with the given params. Titles may
     * also be enriched by individual parameters submitted by KBART file; this file is being processed here as well
     * @param controller unused
     * @param params the request parameters including filter data and / or an eventual enrichment file
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> index(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'PackageTransfer_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running.withPackage',[subscriptionService.getCachedPackageName(it.name)] as Object[],locale)
                }
                else if (it.name == 'EntitlementEnrichment_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.thread.running', null, locale)
                }
            }
            result.issueEntitlementEnrichment = params.issueEntitlementEnrichment
            SwissKnife.setPaginationParams(result, params, (User) result.user)

            //params.status = params.status ?: (result.subscription.hasPerpetualAccess ? [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id] : [RDStore.TIPP_STATUS_CURRENT.id])

            Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'IEs')
            if (ttParams.status) { params.status = ttParams.status }
            if (ttParams.tab)    { params.tab = ttParams.tab }

            Map query = filterService.getIssueEntitlementQuery(params, result.subscription)
            result.filterSet = query.filterSet
            Set entitlements = IssueEntitlement.executeQuery("select new map(ie.id as id, tipp.sortname as sortname) " + query.query, query.queryParams)
            result.entitlementIDs = entitlements
            if(params.kbartPreselect) {
                String filename = params.kbartPreselect.originalFilename

                int countQueryIes = entitlements.size()

                MultipartFile kbartFile = params.kbartPreselect
                InputStream stream = kbartFile.getInputStream()

                result.enrichmentProcess = subscriptionService.issueEntitlementEnrichment(stream, countQueryIes, result.subscription, (params.uploadCoverageDates == 'on'), (params.uploadPriceInfo == 'on'))

                params.remove("kbartPreselect")
                params.remove("uploadCoverageDates")
                params.remove("uploadPriceInfo")


                if (result.enrichmentProcess.wrongTitles) {
                    //background of this procedure: the editor adding titles via KBART wishes to receive a "counter-KBART" which will then be sent to the provider for verification
                    String dir = GlobalService.obtainFileStorageLocation()
                    File f = new File(dir+"/${filename}_matchingErrors")
                    String returnKBART = exportService.generateSeparatorTableString(result.enrichmentProcess.titleRow, result.enrichmentProcess.wrongTitles, '\t')
                    FileOutputStream fos = new FileOutputStream(f)
                    fos.withWriter { Writer w ->
                        w.write(returnKBART)
                    }
                    fos.flush()
                    fos.close()
                    result.token = "${filename}_matchingErrors"
                    result.fileformat = "kbart"
                    result.errorCount = result.enrichmentProcess.wrongTitles.size()
                    result.errorKBART = true
                }

                result.issueEntitlementEnrichment = true

            }

            if(result.subscription.ieGroups.size() > 0) {
                GrailsParameterMap paramsClone = params.clone()
                paramsClone.forCount = true
                Map query2 = filterService.getIssueEntitlementQuery(paramsClone, result.subscription)
                result.num_ies = IssueEntitlement.executeQuery("select count(*) " + query2.query, query2.queryParams)[0]
            }
            result.num_ies_rows = entitlements.size()
            if(entitlements && !params.containsKey('fileformat')) {
                String orderClause = 'order by tipp.sortname'
                if(params.sort){
                    if(params.sort == 'startDate')
                        orderClause = "order by ic.startDate ${params.order}, lower(tipp.sortname) "
                    else if(params.sort == 'endDate')
                        orderClause = "order by ic.endDate ${params.order}, lower(tipp.sortname) "
                    else {
                        if(params.sort.contains('sortname'))
                            orderClause = "order by tipp.sortname ${params.order}, tipp.name ${params.order} "
                        else
                            orderClause = "order by ${params.sort} ${params.order} "
                    }
                }
                Set filteredIDs = entitlements.drop(result.offset).take(result.max)
                result.entitlements = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp left join ie.coverages ic where ie.id in (:entIDs) '+orderClause,[entIDs:filteredIDs.id]) //please check eventual side effects on sorting! toSet() is needed because of coverage statement doublets!                result.journalsOnly = result.entitlements.find { IssueEntitlement ie -> ie.tipp.titleType != RDStore.TITLE_TYPE_JOURNAL.value } == null
            }
            else result.entitlements = []
            /*
            Date now = new Date()
            if (now > result.subscription.endDate) {
                result.frozenHoldings = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.freezeHolding }.pkg
            }
            */
            if (executorWrapperService.hasRunningProcess(result.subscription)) {
                result.processingpc = true
            }
            result.considerInBatch = ["sort", "order", "offset", "max", "status", "pkgfilter", "asAt", "series_name", "subject_reference", "ddc", "language", "yearsFirstOnline", "identifier", "title_types", "publishers", "coverageDepth", "inTitleGroups"]

            /*
            result.currentIECounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.status = :status and ie.status != :ieStatus", [sub: result.subscription, status: RDStore.TIPP_STATUS_CURRENT, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.plannedIECounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.status = :status and ie.status != :ieStatus", [sub: result.subscription, status: RDStore.TIPP_STATUS_EXPECTED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.expiredIECounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.status = :status and ie.status != :ieStatus", [sub: result.subscription, status: RDStore.TIPP_STATUS_RETIRED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.deletedIECounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.status = :status and ie.status != :ieStatus", [sub: result.subscription, status: RDStore.TIPP_STATUS_DELETED, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            result.allIECounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.status in (:status) and ie.status != :ieStatus", [sub: result.subscription, status: [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED], ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
            */
            List counts = IssueEntitlement.executeQuery('select new map(count(*) as count, status as status) from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus group by status', [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])
            result.allIECounts = 0
            result.currentIECounts = 0
            result.plannedIECounts = 0
            result.expiredIECounts = 0
            result.deletedIECounts = 0

            counts.each { row ->
                switch (row['status']) {
                    case RDStore.TIPP_STATUS_CURRENT: result.currentIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_EXPECTED: result.plannedIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_RETIRED: result.expiredIECounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_DELETED: result.deletedIECounts = row['count']
                        break
                }
                result.allIECounts += row['count']
            }

            result.permanentTitlesProcessRunning = subscriptionService.checkPermanentTitleProcessRunning(result.subscription, result.institution)

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Gets the changes applied to or applicable to the given subscription
     * @param params the request parameter map
     * @return a tab view of the changes, grouped by event type
     */
    Map<String,Object> entitlementChanges(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        SwissKnife.setPaginationParams(result,params,(User) result.user)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Map<SubscriptionPackage, Map<String, RefdataValue>> pkgSettingMap = [:]
            Set<String> pendingOrWithNotification = []
            Set subscriptionHistory = []
            Set<PendingChange> changesOfPage = []
            Set<String> excludes = PendingChangeConfiguration.GENERIC_EXCLUDES
            result.subscription.packages.each { SubscriptionPackage sp ->
                //(pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT || pcc.withNotification)
                /*
                Map<String, RefdataValue> keysWithPendingOrNotification = sp.pendingChangeConfig.findAll { PendingChangeConfiguration pcc -> !(pcc.settingKey in excludes) }.collectEntries { PendingChangeConfiguration pcc -> [pcc.settingKey, pcc.settingValue] }
                */
                Map<String, RefdataValue> keysWithPendingOrNotification = [:]
                keysWithPendingOrNotification.put(PendingChangeConfiguration.TITLE_REMOVED, RDStore.PENDING_CHANGE_CONFIG_PROMPT)
                if(keysWithPendingOrNotification) {
                    pkgSettingMap.put(sp, keysWithPendingOrNotification)
                    pendingOrWithNotification.addAll(keysWithPendingOrNotification.keySet())
                }
            }

            params.order = params.order ?: 'desc'
            if(!params.tab)
                params.tab = 'acceptedChanges'
            if(!params.eventType) {
                params.eventType = PendingChangeConfiguration.TITLE_REMOVED
                /*
                if(params.tab == 'acceptedChanges') {
                    params.eventType = PendingChangeConfiguration.NEW_TITLE
                }
                else if(params.tab == 'changes') {
                    params.eventType = PendingChangeConfiguration.TITLE_REMOVED
                }
                */
            }
            else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED && params.tab == 'acceptedChanges')
                params.eventType = PendingChangeConfiguration.NEW_TITLE

            if(!params.sort) {
                if(params.tab == 'acceptedChanges' && params.eventType == PendingChangeConfiguration.NEW_TITLE) {
                    params.sort = 'iec.actionDate'
                }
                else {
                    params.sort = 'tic.dateCreated'
                }
            }
            String order = " order by ${params.sort} ${params.order}"
            params.tab = params.tab ?: 'changes'
            if(pkgSettingMap && pendingOrWithNotification) {
                pkgSettingMap.each { SubscriptionPackage sp, Map<String, RefdataValue> settings ->
                    Set titleChanges = []
                    if(params.tab == 'changes' && settings.get(params.eventType) != RDStore.PENDING_CHANGE_CONFIG_REJECT) {
                        switch(params.eventType) {
                            case PendingChangeConfiguration.NEW_TITLE:
                                titleChanges.addAll(TitleChange.executeQuery('select tic from TitleChange tic join tic.tipp tipp where tic.event = :event and tipp.pkg = :pkg and not exists (select iec.id from IssueEntitlementChange iec where iec.status in (:processed) and iec.titleChange = tic and iec.subscription = :sub) and tic.dateCreated >= :entryDate '+order,
                                    [pkg: sp.pkg, sub: sp.subscription, event: params.eventType, processed: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED, RDStore.PENDING_CHANGE_SUPERSEDED], entryDate: sp.dateCreated]))
                                break
                            case PendingChangeConfiguration.TITLE_DELETED:
                                //check against both deleted or removed because otherwise, there are changes displayed!
                                titleChanges.addAll(TitleChange.executeQuery('select tic from TitleChange tic join tic.tipp tipp where tic.event = :event and tipp.pkg = :pkg and not exists (select ie.id from IssueEntitlement ie where ie.status in (:deleted) and ie.tipp = tipp and ie.subscription = :sub) and tic.dateCreated >= :entryDate '+order,
                                        [pkg: sp.pkg, sub: sp.subscription, event: PendingChangeConfiguration.TITLE_DELETED, deleted: [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED], entryDate: sp.dateCreated]))
                                break
                        }
                    }
                    else if(params.tab == 'acceptedChanges') {
                        switch(params.eventType) {
                            case PendingChangeConfiguration.TITLE_REMOVED:
                                /*
                                titleChanges.addAll(TitleChange.executeQuery('select tic from TitleChange tic join tic.tipp tipp where tic.event = :event and tipp.pkg = :pkg and not exists (select ie.id from IssueEntitlement ie where ie.status = :removed and ie.tipp = tipp and ie.subscription = :sub) and tic.dateCreated >= :entryDate '+order,
                                    [pkg: sp.pkg, sub: sp.subscription, event: PendingChangeConfiguration.TITLE_REMOVED, removed: RDStore.TIPP_STATUS_REMOVED, entryDate: sp.dateCreated]))
                                */
                                titleChanges.addAll(IssueEntitlement.executeQuery("select ie from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and ie.subscription = :sub and ie.status = :removed order by ie.lastUpdated desc", [pkg: sp.pkg, sub: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED], [max: result.max, offset: result.offset]))
                                break
                            case PendingChangeConfiguration.NEW_TITLE: titleChanges.addAll(IssueEntitlementChange.executeQuery('select iec from IssueEntitlementChange iec join iec.titleChange tic join tic.tipp tipp where tic.event = :event and tipp.pkg = :pkg and iec.subscription = :sub and iec.status = :accepted '+order,
                                        [pkg: sp.pkg, sub: sp.subscription, event: params.eventType, accepted: RDStore.PENDING_CHANGE_ACCEPTED]))
                                break
                            case PendingChangeConfiguration.TITLE_STATUS_CHANGED: titleChanges.addAll(TitleChange.executeQuery('select tic from TitleChange tic join tic.tipp tipp where tic.event = :event and tipp.pkg = :pkg'+order,
                                    [pkg: sp.pkg, event: params.eventType]))
                                break
                        }
                    }
                    result.changes = titleChanges.drop(result.offset).take(result.max)
                    result.num_change_rows = titleChanges.size()
                /* OLD

                    String query1a = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.oid = :subOid and pc.status in (:pendingStatus)'
                    //query3a = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status in (:pendingStatus)',
                    String query1b, query1c
                    if (params.eventType == PendingChangeConfiguration.NEW_TITLE)
                        query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and (not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and not exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.subscription = :subscription and ie.status != :removed)) and pc.status = :packageHistory'
                    else if (params.eventType == PendingChangeConfiguration.TITLE_UPDATED)
                        query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and (not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and (pca.newValue = pc.newValue or (pca.newValue is null and pc.newValue is null)) and pca.status in (:pendingStatus)) and exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status != :removed and ie.subscription = :subscription)) and pc.status = :packageHistory'
                    else if (params.eventType == PendingChangeConfiguration.TITLE_DELETED)
                        query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.tipp.status != :removed and (not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status not in (:deleted) and ie.subscription = :subscription)) and pc.status = :packageHistory'
                    else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED && params.eventType in settings.keySet())
                        query1c = 'select pc.id from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :package and pc.msgToken = :eventType and exists(select ie from IssueEntitlement ie where ie.tipp = tipp and ie.subscription = :subscription and ie.status != :removed)'
                    //query3b = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status not in (:pendingStatus)',
                    String query1d = 'select pc.id from PendingChange pc where pc.subscription = :subscription and pc.msgToken = :eventType and pc.status not in (:pendingStatus)'
                    if(params.eventType in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_UPDATED, PendingChangeConfiguration.TITLE_DELETED] && params.eventType in settings.keySet())
                        subscriptionHistory.addAll(PendingChange.executeQuery(query1a+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    //subscriptionHistory.addAll(PendingChange.executeQuery(query3a,[package: pkg, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    //if(params.eventType == PendingChangeConfiguration.NEW_TITLE && params.eventType in settings)
                    //    changesOfPage.addAll(PendingChange.executeQuery(query1b+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
                    if(params.eventType in settings && pcc?.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT || !pcc) {
                        if (params.eventType in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_UPDATED] && params.eventType in settings.keySet())
                            changesOfPage.addAll(PendingChange.executeQuery(query1b + order, [package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
                        else if (params.eventType == PendingChangeConfiguration.TITLE_DELETED && params.eventType in settings.keySet())
                            changesOfPage.addAll(PendingChange.executeQuery(query1b + order, [package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED, deleted: [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED]]))
                    }
                    if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED && params.eventType in settings.keySet())
                        changesOfPage.addAll(PendingChange.executeQuery(query1c+order,[package: pkg, eventType: params.eventType, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
                    //changesOfPage.addAll(PendingChange.executeQuery(query3b,[packages: pkgList, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    changesOfPage.addAll(PendingChange.executeQuery(query1d+order,[pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], eventType: params.eventType, subscription: result.subscription]))

                */
                }
            }
            params.max = result.max
            params.offset = result.offset

            result.putAll(pendingChangeService.getCountsForPackages(pkgSettingMap))

            /*
            if(params.tab == 'changes') {
                result.changes = changesOfPage ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:changesOfPage) order by '+params.sort+' '+params.order, [changesOfPage: changesOfPage.drop(result.offset).take(result.max)]) : []
                result.num_change_rows = changesOfPage.size()
            }

            if(params.tab == 'acceptedChanges') {
                result.changes = subscriptionHistory ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:subscriptionHistory) order by '+params.sort+' '+params.order, [subscriptionHistory: subscriptionHistory.drop(result.offset).take(result.max)]) : []
                result.num_change_rows = subscriptionHistory.size()
            }
            */

            result.apisources = [ ApiSource.getCurrent() ]

            result.packages = result.subscription.packages

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Lists the titles in the package which have not yet been added to the subscription
     * @param params the request parameter map
     * @return the title list; with or without the enriched information from a KBART upload
     */
    @Deprecated
    Map<String,Object> addEntitlements(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            //result.preselectValues = params.preselectValues == 'on'
            //result.preselectCoverageDates = params.preselectCoverageDates == 'on'
            //result.uploadPriceInfo = params.uploadPriceInfo == 'on'
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'PackageSync_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null,locale)
                }
                else if (it.name == 'EntitlementEnrichment_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.thread.running', null, locale)
                    result.blockSubmit = true
                }
            }
            SwissKnife.setPaginationParams(result, params, (User) result.user)
            List<TitleInstancePackagePlatform> tipps = []
            EhcacheWrapper userCache = contextService.getUserCache("/subscription/addEntitlements/${params.id}")
            Map checkedCache = userCache.get('selectedTitles')

            if (!checkedCache || !params.containsKey('pagination')) {
                checkedCache = [:]
            }

            result.subscriber = result.subscription.getSubscriberRespConsortia()
            params.issueEntitlementStatus = RDStore.TIPP_STATUS_CURRENT
            params.subscription = result.subscription
            params.addEntitlements = true
            List packages = []
            if(params.pkgfilter)
                packages << Package.get(params.pkgfilter)
            else packages = result.subscription.packages?.pkg

            Map<String, Object> query = filterService.getTippQuery(params, packages)
            result.filterSet = query.filterSet

            if(result.subscription.packages?.pkg) {
                Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
                Map<TitleInstancePackagePlatform, List<PermanentTitle>> permanentTitles = [:]
                if(tippIds) {
                    Set<Long> subset = tippIds.drop(result.offset).take(result.max)
                    tipps.addAll(TitleInstancePackagePlatform.findAllByIdInList(subset, [sort: 'sortname']))
                    tipps.each { TitleInstancePackagePlatform tipp ->
                        List<PermanentTitle> pts = surveyService.listParticipantPerpetualAccessToTitle(result.institution, tipp)
                        if(pts)
                            permanentTitles.put(tipp, pts)
                    }
                }

                result.num_tipp_rows = tippIds.size()
                result.tipps = tipps
                result.permanentTitles = permanentTitles
                if(permanentTitles.size() == tipps.size() && tipps.size() > 0)
                    result.allPerpetuallyBought = 'subscription.details.addEntitlements.allPerpetuallyBought'
                result.tippIDs = tippIds
                String filename
                if(params.pagination) {
                    result.checked = checkedCache
                }
            }

            result.checkedCache = checkedCache.get('checked')
            result.checkedCount = result.checkedCache.findAll { it.value == 'checked' }.size()
            result.countSelectedTipps = result.checkedCount

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Marks the given issue entitlement as deleted and thus removes it from the holding
     * @param params the request parameter map
     * @return OK if the status update was successful, ERROR otherwise
     */
    Map<String,Object> removeEntitlement(GrailsParameterMap params) {
        IssueEntitlement ie = IssueEntitlement.get(params.ieid)
        ie.status = RDStore.TIPP_STATUS_REMOVED

        PermanentTitle permanentTitle = PermanentTitle.findByOwnerAndTipp(ie.subscription.getSubscriberRespConsortia(), ie.tipp)
        if (permanentTitle) {
            permanentTitle.delete()
        }

        if(ie.save())
            [result:null,status:STATUS_OK]
        else [result:null,status:STATUS_ERROR]
    }

    /**
     * Removes the given issue entitlement and deletes also the corresponding issue entitlement group item
     * @param params the request parameter map
     * @return OK if the deletion and status update were successful, false otherwise
     */
    Map<String,Object> removeEntitlementWithIEGroups(GrailsParameterMap params) {
        IssueEntitlement ie = IssueEntitlement.get(params.ieid)
        RefdataValue oldStatus = ie.status
        ie.status = RDStore.TIPP_STATUS_REMOVED
        if(ie.save()){

            PermanentTitle permanentTitle = PermanentTitle.findByOwnerAndTipp(ie.subscription.getSubscriberRespConsortia(), ie.tipp)
            if (permanentTitle) {
                permanentTitle.delete()
            }

            if(IssueEntitlementGroupItem.findByIe(ie)) {
                if (IssueEntitlementGroupItem.executeUpdate("delete from IssueEntitlementGroupItem iegi where iegi.ie = :ie", [ie: ie])) {
                    return [result: null, status: STATUS_OK]
                } else {
                    ie.status = oldStatus
                    ie.save()
                    return [result: null, status: STATUS_ERROR]
                }
            }else{
                return [result: null, status: STATUS_OK]
            }
        }
        else {
            return [result:null,status:STATUS_ERROR]
        }
    }

    /**
     * Adds the cached title candidates to the holding and persists also eventually recorded enrichments of the titles
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the persisting was successful, ERROR otherwise
     */
    @Deprecated
    Map<String,Object> processAddEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            EhcacheWrapper cache = contextService.getUserCache("/subscription/addEntitlements/${result.subscription.id}")
            Map issueEntitlementCandidates = cache && cache.get('selectedTitles') ? cache.get('selectedTitles') : [:]
            if(!params.singleTitle) {
                Map checked = issueEntitlementCandidates.get('checked')
                if(checked) {
                    Set<Long> childSubIds = [], pkgIds = []
                    if(params.withChildren == 'on') {
                        childSubIds.addAll(result.subscription.getDerivedSubscriptions().id)
                    }
                    checked.keySet().collate(65000).each { subSet ->
                        pkgIds.addAll(Package.executeQuery('select tipp.pkg.id from TitleInstancePackagePlatform tipp where tipp.gokbId in (:wekbIds)', [wekbIds: subSet]))
                    }
                    executorService.execute({
                        Thread.currentThread().setName("EntitlementEnrichment_${result.subscription.id}")
                        subscriptionService.bulkAddEntitlements(result.subscription, checked.keySet(), result.subscription.hasPerpetualAccess)
                        if(params.withChildren == 'on') {
                            Sql sql = GlobalService.obtainSqlConnection()
                            childSubIds.each { Long childSubId ->
                                pkgIds.each { Long pkgId ->
                                    batchQueryService.bulkAddHolding(sql, childSubId, pkgId, result.subscription.hasPerpetualAccess, result.subscription.id)
                                }
                            }
                            sql.close()
                        }
                        //IssueEntitlement.withNewTransaction { TransactionStatus ts ->
                        /*
                        checked.each { k, v ->
                            if(v == 'checked') {
                                try {
                                    /*
                                | Boolean.valueOf(params.uploadPriceInfo))  {
                                        if(subscriptionService.addEntitlement(result.subscription, k, issueEntitlementCandidates?.get(k), Boolean.valueOf(params.uploadPriceInfo), ie_accept_status))
                                            log.debug("Added tipp ${k} to sub ${result.subscription.id} with issue entitlement overwrites")
                                    }
                                    else if(subscriptionService.addEntitlement(result.subscription,k,null,false, ie_accept_status)) {
                                        log.debug("Added tipp ${k} to sub ${result.subscription.id}")
                                    }

                                }
                                catch (EntitlementCreationException e) {
                                    result.error = e.getMessage()
                                }
                            }
                        }
                        */

                        if(params.process && params.process	== "withTitleGroup") {
                            IssueEntitlementGroup issueEntitlementGroup
                            if (params.issueEntitlementGroupNew) {

                                IssueEntitlementGroup.withTransaction {
                                    issueEntitlementGroup = IssueEntitlementGroup.findBySubAndName(result.subscription, params.issueEntitlementGroupNew) ?: new IssueEntitlementGroup(sub: result.subscription, name: params.issueEntitlementGroupNew).save()
                                }
                            }

                            if (params.issueEntitlementGroupID && params.issueEntitlementGroupID != '') {
                                issueEntitlementGroup = IssueEntitlementGroup.findById(params.long('issueEntitlementGroupID'))
                            }

                            if (issueEntitlementGroup) {
                                issueEntitlementGroup.refresh()
                                Object[] keys = checked.keySet().toArray()
                                keys.each { String gokbUUID ->
                                    IssueEntitlement.withTransaction { TransactionStatus ts ->
                                        TitleInstancePackagePlatform titleInstancePackagePlatform = TitleInstancePackagePlatform.findByGokbId(gokbUUID)
                                        if (titleInstancePackagePlatform) {
                                            IssueEntitlement ie = IssueEntitlement.findBySubscriptionAndTipp(result.subscription, titleInstancePackagePlatform)

                                            if (issueEntitlementGroup && !IssueEntitlementGroupItem.findByIe(ie)) {
                                                IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                                        ie: ie,
                                                        ieGroup: issueEntitlementGroup)

                                                if (!issueEntitlementGroupItem.save()) {
                                                    log.error("Problem saving IssueEntitlementGroupItem by manual adding ${issueEntitlementGroupItem.getErrors().getAllErrors().toListString()}")
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        cache.remove('selectedTitles')
                    })
                }
                else {
                    log.error('cache error or no titles selected')
                }
            }
            else if(params.singleTitle) {
                try {
                    Object[] args = [TitleInstancePackagePlatform.findByGokbId(params.singleTitle)?.name]
                    if(issueEntitlementCandidates?.get(params.singleTitle) || Boolean.valueOf(params.uploadPriceInfo))  {
                        if(subscriptionService.addSingleEntitlement(result.subscription, params.singleTitle, issueEntitlementCandidates?.get(params.singleTitle), Boolean.valueOf(params.uploadPriceInfo)))
                            log.debug("Added tipp ${params.singleTitle} to sub ${result.subscription.id} with issue entitlement overwrites")
                        result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                    }
                    else if(subscriptionService.addSingleEntitlement(result.subscription, params.singleTitle, null, true))
                        log.debug("Added tipp ${params.singleTitle} to sub ${result.subscription.id}")
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                }
                catch(EntitlementCreationException e) {
                    result.error = e.getMessage()
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Reverts the changes applied to the given subscription after its end of running. As the process
     * may affect a large amount of entries, the process is deployed to a parallel thread
     * @param params the request parameter map
     * @return OK if the execution was successful, false otherwise
     */
    Map<String,Object> resetHoldingToSubEnd(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(!result) {
            [result: null, status: STATUS_ERROR]
        }
        else {
            if (subscriptionService.checkThreadRunning('PackageTransfer_' + result.subscription.id)) {
                result.errMess = 'subscription.packages.resetToSubEnd.threadRunning'
                [result: result, status: STATUS_ERROR]
            }
            executorService.execute({
                Thread.currentThread().setName("PackageTransfer_${result.subscription.id}")
                SubscriptionPackage sp = SubscriptionPackage.get(params.subPkg)
                //need to use native sql because of performance and queries not doable in hql
                DataSource dataSource = BeanStore.getDataSource()
                Sql sql = new Sql(dataSource)
                //revert changes applied to the package
                //BEWARE: we have to restore the state to subscription end! If the entity has been modified several times, I need the closest change to the subscription end - and ONLY that!
                /*
                    inversion mapping:
                    new title: set new title to deleted
                    title updated: set old value
                    title deleted: set old title to current
                    new coverage: delete
                    coverage updated: set old value
                    coverage deleted: recreate old value (parse JSON in pc.oldValue)
                 */
                Map<String, PropertyConfig> ieColNames = GrailsDomainBinder.getMapping(IssueEntitlement).columns,
                                            covColNames = GrailsDomainBinder.getMapping(IssueEntitlementCoverage).columns
                sql.withTransaction {
                    //revert new titles
                    //log.debug("update issue_entitlement set ie_status_rv_fk = ${RDStore.TIPP_STATUS_REMOVED.id} where ie_tipp_fk in (select distinct pc_tipp_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where (sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id}) and pc_date_created > sub_end_date and tipp_pkg_fk = ${sp.pkg.id} and pc_status_rdv_fk = ${RDStore.PENDING_CHANGE_ACCEPTED.id} and (sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id}) and pc_tipp_fk is not null and pc_msg_token = ${PendingChangeConfiguration.NEW_TITLE}) and ie_subscription_fk in (select sub_id from subscription where sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id})")
                    sql.executeUpdate("update issue_entitlement set ie_status_rv_fk = :removed where ie_tipp_fk in (select distinct pc_tipp_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_date_created > sub_end_date and tipp_pkg_fk = :pkgId and pc_status_rdv_fk = :accepted and (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_tipp_fk is not null and pc_msg_token = :newTipp) and ie_subscription_fk in (select sub_id from subscription where sub_id = :subId or sub_parent_sub_fk = :subId)", [subId: sp.subscription.id, pkgId: sp.pkg.id, accepted: RDStore.PENDING_CHANGE_ACCEPTED.id, deleted: RDStore.TIPP_STATUS_REMOVED.id, newTipp: PendingChangeConfiguration.NEW_TITLE])
                    //revert updated titles
                    List<GroovyRowResult> acceptedTitleUpdates = sql.rows("select distinct on (pc_date_created::date, pc_target_property) pc_tipp_fk, pc_old_value, pc_target_property, split_part(pc_oid, ':', 2) as sub_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where pc_date_created > sub_end_date and tipp_pkg_fk = :pkgId and pc_status_rdv_fk = :accepted and (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_tipp_fk is not null and pc_msg_token = :tippUpdated order by pc_date_created::date", [subId: sp.subscription.id, pkgId: sp.pkg.id, tippUpdated: PendingChangeConfiguration.TITLE_UPDATED])
                    //need to revert changes one by one ... very ugly!
                    acceptedTitleUpdates.eachWithIndex { GroovyRowResult row, int i ->
                        log.debug "now processing record ${i} of ${acceptedTitleUpdates.size()} entries"
                        sql.executeUpdate("update issue_entitlement set ${ieColNames[row['pc_target_property']].column} = :oldValue where ie_tipp_fk = :tipp and ie_subscription_fk = :subscription", [oldValue: row['pc_old_value'], tipp: row['pc_tipp_fk'], subscription: row['sub_fk']])
                    }
                    //revert deleted titles
                    //log.debug("update issue_entitlement set ie_status_rv_fk = case when pc_old_value is not null then (select rdv_id from refdata_value join refdata_category on rdc_id = rdv_owner where rdv_value = pc_old_value and rdc_description = '${RDConstants.TIPP_STATUS}') else :current end from pending_change where where ie_tipp_fk = pc_tipp_fk and pc_tipp_fk in (select distinct on (pc_date_created) pc_tipp_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where (sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id}) and pc_date_created > sub_end_date and tipp_pkg_fk = ${sp.pkg.id} and pc_status_rdv_fk = ${RDStore.PENDING_CHANGE_ACCEPTED.id} and pc_tipp_fk is not null and pc_msg_token = ${PendingChangeConfiguration.TITLE_DELETED}) and ie_subscription_fk in (select sub_id from subscription where sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id})")
                    sql.executeUpdate("update issue_entitlement set ie_status_rv_fk = case when pc_old_value is not null then (select rdv_id from refdata_value join refdata_category on rdc_id = rdv_owner where rdv_value = pc_old_value and rdc_description = '${RDConstants.TIPP_STATUS}') else :current end from pending_change where ie_tipp_fk = pc_tipp_fk and pc_tipp_fk in (select distinct on (pc_date_created) pc_tipp_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_date_created > sub_end_date and tipp_pkg_fk = :pkgId and pc_status_rdv_fk = :accepted and pc_tipp_fk is not null and pc_msg_token = :titleDeleted order by pc_date_created) and ie_subscription_fk in (select sub_id from subscription where sub_id = :subId or sub_parent_sub_fk = :subId)", [subId: sp.subscription.id, pkgId: sp.pkg.id, accepted: RDStore.PENDING_CHANGE_ACCEPTED.id, current: RDStore.TIPP_STATUS_CURRENT.id, tippDeleted: PendingChangeConfiguration.TITLE_DELETED])
                    //revert new coverages
                    sql.executeUpdate("delete from issue_entitlement_coverage where ic_id in (select ic_id from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join subscription on ie_subscription_fk = sub_id where (sub_id = :subId or sub_parent_sub_fk = :subId) and ic_date_created > sub_end_date)", [subId: sp.subscription.id])
                    //revert updated coverages
                    //log.debug("select distinct on (pc_date_created::date, pc_target_property) pc_tc_fk, pc_msg_token, pc_old_value, pc_new_value, pc_target_property, split_part(pc_oid, ':', 2) as sub_fk from pending_change join tippcoverage on pc_tc_fk = tc_id join title_instance_package_platform on tc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where pc_date_created > sub_end_date and tipp_pkg_fk = ${sp.pkg.id} and pc_status_rdv_fk = ${RDStore.PENDING_CHANGE_ACCEPTED.id} and pc_oid in (select concat('${Subscription.class.name}:',sub_id) from subscription where sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id}) and pc_tc_fk is not null and pc_msg_token = ${PendingChangeConfiguration.COVERAGE_UPDATED} order by pc_date_created::date")
                    List<GroovyRowResult> acceptedCoverageUpdates = sql.rows("select distinct on (pc_date_created::date, pc_target_property) tc_tipp_fk, pc_tc_fk, pc_msg_token, pc_old_value, pc_new_value, pc_target_property, split_part(pc_oid, ':', 2) as sub_fk from pending_change join tippcoverage on pc_tc_fk = tc_id join title_instance_package_platform on tc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where pc_date_created > sub_end_date and tipp_pkg_fk = :pkgId and pc_status_rdv_fk = :accepted and pc_oid in (select concat('${Subscription.class.name}:',sub_id) from subscription where sub_id = :subId or sub_parent_sub_fk = :subId) and pc_tc_fk is not null and pc_msg_token = :covUpdated order by pc_date_created::date", [subId: sp.subscription.id, accepted: RDStore.PENDING_CHANGE_ACCEPTED.id, pkgId: sp.pkg.id, covUpdated: PendingChangeConfiguration.COVERAGE_UPDATED])
                    Map<Long, Map<String, Object>> revertingChanges = [:]
                    acceptedCoverageUpdates.eachWithIndex { GroovyRowResult row, int idx ->
                        log.debug "now processing change ${idx} out of ${acceptedCoverageUpdates.size()} records"
                        Map<String, Object> revertingMap = revertingChanges.get(row['pc_tc_fk'])
                        if(!revertingMap)
                            revertingMap = [subscriptions: new HashSet<Long>(), changes: []]
                        revertingMap.subscriptions << Long.parseLong(row['sub_fk'])
                        def oldValue, newValue
                        if (row['pc_target_property'] in ['startDate', 'endDate']) {
                            oldValue = row['pc_old_value'] ? new Timestamp(DateUtils.parseDateGeneric(row['pc_old_value']).getTime()) : null
                        } else {
                            oldValue = row['pc_old_value']
                        }
                        revertingMap.changes << [key: covColNames[row['pc_target_property']].column, value: oldValue]
                        revertingChanges.put(row['pc_tc_fk'], revertingMap)
                    }
                    revertingChanges.eachWithIndex { Long tc, Map<String, Object> revertingMap, int i ->
                        revertingMap.subscriptions.eachWithIndex { Long subKey, int j ->
                            log.debug "now reverting change ${i} out of ${revertingChanges.size()} records at subscriptions ${j} out of ${revertingMap.subscriptions.size()} subscriptions"
                            List<GroovyRowResult> revertingRecords = sql.rows("select ic_id from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join tippcoverage on ie_tipp_fk = tc_tipp_fk where tc_id = :tc and ie_subscription_fk = :subscription order by ic_id limit 1", [tc: tc, subscription: subKey])
                            if(revertingRecords.size() == 1) {
                                GroovyRowResult oldRec = revertingRecords.get(0)
                                revertingMap.changes.each { Map<String, Object> changeMap ->
                                    sql.executeUpdate("update issue_entitlement_coverage set ${changeMap.key} = :oldValue where ic_id = :icId", [oldValue: changeMap.value, icId: oldRec['ic_id']])
                                }
                            }
                        }
                    }
                    //revert deleted coverages
                    //log.debug("select pc_old_value, split_part(pc_oid, ':', 2) as sub_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where pc_date_created > sub_end_date and tipp_pkg_fk = ${sp.pkg.id} and (sub_id = ${sp.subscription.id} or sub_parent_sub_fk = ${sp.subscription.id}) and pc_msg_token = '${PendingChangeConfiguration.COVERAGE_DELETED}'")
                    List<GroovyRowResult> deletedCoverages = sql.rows("select pc_old_value, split_part(pc_oid, ':', 2) as sub_fk from pending_change join title_instance_package_platform on pc_tipp_fk = tipp_id join subscription on split_part(pc_oid, ':', 2)::bigint = sub_id where pc_date_created > sub_end_date and tipp_pkg_fk = :pkgId and (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_msg_token = :covDeleted and pc_status_rdv_fk = :accepted", [pkgId: sp.pkg.id, subId: sp.subscription.id, covDeleted: PendingChangeConfiguration.COVERAGE_DELETED, accepted: RDStore.PENDING_CHANGE_ACCEPTED.id])
                    if(deletedCoverages) {
                        sql.withBatch("insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_note, ic_coverage_depth, ic_embargo, ic_date_created, ic_last_updated) values (0, :ie, :startDate, :startVolume, :startIssue, :endDate, :endVolume, :endIssue, :note, :depth, :embargo, :dateCreated, :lastUpdated)") { BatchingStatementWrapper stmt ->
                            deletedCoverages.eachWithIndex { GroovyRowResult row, int i ->
                                log.debug "now restoring deleted coverage ${i}"
                                Map<String, Object> oldCoverage = JSON.parse(row['pc_old_value'])
                                //when migrating to dev: change deleted by removed
                                List<Long> ie = IssueEntitlement.executeQuery("select ie.id from IssueEntitlement ie where ie.tipp.id = :tippId and ie.subscription.id = :subId and ie.status != :ieStatus", [tippId: Integer.toUnsignedLong(oldCoverage.tipp.id), subId: Long.parseLong(row['sub_fk']), ieStatus: RDStore.TIPP_STATUS_REMOVED])
                                if(ie) {
                                    Map<String, Object> ieCovMap = [ie: ie[0],
                                                                    dateCreated: new Timestamp(DateUtils.parseDateGeneric(oldCoverage.dateCreated).getTime()),
                                                                    lastUpdated: new Timestamp(DateUtils.parseDateGeneric(oldCoverage.lastUpdated).getTime()),
                                                                    startDate: oldCoverage.startDate ? new Timestamp(DateUtils.parseDateGeneric(oldCoverage.startDate).getTime()) : null,
                                                                    startVolume: oldCoverage.startVolume,
                                                                    startIssue: oldCoverage.startIssue,
                                                                    endDate: oldCoverage.endDate ? new Timestamp(DateUtils.parseDateGeneric(oldCoverage.endDate).getTime()) : null,
                                                                    endVolume: oldCoverage.endVolume,
                                                                    endIssue: oldCoverage.endIssue,
                                                                    note: oldCoverage.coverageNote,
                                                                    depth: oldCoverage.coverageDepth,
                                                                    embargo: oldCoverage.embargo]
                                    stmt.addBatch(ieCovMap)
                                }
                            }
                        }
                    }
                    Set<Object> pendingStatus = [RDStore.PENDING_CHANGE_ACCEPTED.id, RDStore.PENDING_CHANGE_PENDING.id]
                    sql.executeUpdate("update pending_change set pc_status_rdv_fk = :rejected from subscription, subscription_package where split_part(pc_oid, ':', 2)::bigint = sub_id and split_part(pc_oid, ':', 2)::bigint = sp_sub_fk and sp_pkg_fk = :pkgId and (sub_id = :subId or sub_parent_sub_fk = :subId) and pc_date_created > sub_end_date and pc_status_rdv_fk = any(:pendingStatus)", [rejected: RDStore.PENDING_CHANGE_REJECTED.id, pendingStatus: sql.connection.createArrayOf('bigint', pendingStatus.toArray()), subId: sp.subscription.id, pkgId: sp.pkg.id])
                    sql.executeUpdate("update pending_change_configuration set pcc_with_notification = false, pcc_setting_value_rv_fk = :reject where pcc_sp_fk = :spId", [reject: RDStore.PENDING_CHANGE_CONFIG_REJECT.id, spId: sp.id])
                }
                //sp.freezeHolding = true
                sp.save()
                /*
                PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
                    AuditConfig.removeConfig(sp.subscription, settingKey)
                    AuditConfig.removeConfig(sp.subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    if(!AuditConfig.getConfig(sp.subscription, SubscriptionPackage.FREEZE_HOLDING))
                        AuditConfig.addConfig(sp.subscription, SubscriptionPackage.FREEZE_HOLDING)
                }
                */
            })
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Performs the given bulk operation on the subscription title holding
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the execution was successful, false otherwise
     */
    Map<String,Object> subscriptionBatchUpdate(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SimpleDateFormat formatter = DateUtils.getLocalizedSDF_noTime()
            boolean error = false
            if(params.chkall == 'on') {
                Map<String, Object> cfgMap = [:]
                cfgMap.putAll(params)
                Map<String, Object> query = filterService.getIssueEntitlementQuery(params.clone() as GrailsParameterMap, result.subscription)
                //if(params.bulkOperation == "edit") {
                    if(GlobalService.isset(params, 'bulk_local_price') && GlobalService.isset(params, 'bulk_local_currency')) {
                        NumberFormat format = NumberFormat.getInstance( LocaleUtils.getCurrentLocale() )
                        BigDecimal localPrice = format.parse(params.bulk_local_price).doubleValue()
                        RefdataValue localCurrency = RefdataValue.get(params.bulk_local_currency)
                        //self-reassign because of setting null currency
                        PriceItem.executeUpdate('update PriceItem pi set pi.localPrice = :localPrice, pi.localCurrency = :localCurrency where (pi.localCurrency = :localCurrency or ((pi.listCurrency = :localCurrency and pi.localCurrency = null) or (pi.listCurrency = null and pi.localCurrency = null))) and pi.issueEntitlement.id in (select ie.id '+query.query+')', query.queryParams+[localPrice: localPrice, localCurrency: localCurrency])
                        Sql sql = GlobalService.obtainSqlConnection()
                        sql.withBatch('insert into price_item (pi_version, pi_ie_fk, pi_guid, pi_date_created, pi_last_updated, pi_local_price, pi_local_currency_rv_fk) values (0, :id, :guid, now(), now(), :localPrice, :localCurrency)') { BatchingStatementWrapper stmt ->
                            IssueEntitlement.executeQuery('select ie.id '+query.query+' and not exists (select pi from PriceItem pi where pi.issueEntitlement = ie and (pi.localCurrency = :localCurrency or ((pi.listCurrency = :localCurrency and pi.localCurrency = null) or (pi.listCurrency = null and pi.localCurrency = null))))', query.queryParams+[localCurrency: localCurrency]).each { Long ieid ->
                                stmt.addBatch([id: ieid, guid: PriceItem.class.name+':'+UUID.randomUUID().toString(), localPrice: localPrice, localCurrency: localCurrency.id])
                            }
                        }
                    }
                    params.keySet().each { String bulkEditParam ->
                        if(GlobalService.isset(params, bulkEditParam)) {
                            switch(bulkEditParam) {
                                case 'bulk_access_start_date':
                                    Date accessStartDate = formatter.parse(params.bulk_access_start_date)
                                    String updateQuery = "update IssueEntitlement e set e.accessStartDate = :accessStartDate where e.id in (select ie.id ${query.query})"
                                    IssueEntitlement.executeUpdate(updateQuery, query.queryParams+[accessStartDate: accessStartDate])
                                    break
                                case 'bulk_access_end_date':
                                    Date accessEndDate = formatter.parse(params.bulk_access_end_date)
                                    String updateQuery = "update IssueEntitlement e set e.accessEndDate = :accessEndDate where e.id in (select ie.id ${query.query})"
                                    IssueEntitlement.executeUpdate(updateQuery, query.queryParams+[accessEndDate: accessEndDate])
                                    break
                                case 'bulk_notes':
                                    String updateQuery = "update IssueEntitlement e set e.notes = :notes where e.id in (select ie.id ${query.query})"
                                    IssueEntitlement.executeUpdate(updateQuery, query.queryParams+[notes: params.bulk_notes])
                                    break
                                case 'titleGroupInsert':
                                    Sql sql = GlobalService.obtainSqlConnection()
                                    cfgMap.select = 'bulkInsertTitleGroup'
                                    if(!params.pkgIds && !params.pkgfilter)
                                        cfgMap.pkgIds = result.subscription.packages.pkg.id
                                    Map<String, Object> sqlQuery = filterService.prepareTitleSQLQuery(cfgMap, IssueEntitlement.class.name, sql)
                                    //log.debug("insert into issue_entitlement_group_item (igi_version, igi_date_created, igi_ie_fk, igi_ie_group_fk, igi_last_updated) "+sqlQuery.query+" where "+sqlQuery.where+" and not exists(select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id)")
                                    //log.debug(sqlQuery.params.toMapString())
                                    sql.execute("insert into issue_entitlement_group_item (igi_version, igi_date_created, igi_ie_fk, igi_ie_group_fk, igi_last_updated) "+sqlQuery.query+" where "+sqlQuery.where+" and not exists(select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id)", sqlQuery.params)
                                    /*if(entitlementGroup && !IssueEntitlementGroupItem.findByIeGroupAndIe(entitlementGroup, ie) && !IssueEntitlementGroupItem.findByIe(ie)){
                                        IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                                ie: ie,
                                                ieGroup: entitlementGroup)
                                        if (!issueEntitlementGroupItem.save()) {
                                            log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                                        }
                                    }*/
                                    break
                                default:
                                    String updateClause
                                    Map<String, Object> updateParams
                                    switch(bulkEditParam) {
                                        case 'bulk_start_date': updateClause = "ic.startDate = :newStartDate"
                                            updateParams = [newStartDate: formatter.parse(params.bulk_start_date)]
                                            break
                                        case 'bulk_start_volume': updateClause = "ic.startVolume = :newStartVolume"
                                            updateParams = [newStartVolume: params.bulk_start_volume]
                                            break
                                        case 'bulk_start_issue': updateClause = "ic.startIssue = :newStartIssue"
                                            updateParams = [newStartIssue: params.bulk_start_issue]
                                            break
                                        case 'bulk_end_date': updateClause = "ic.endDate = :newEndDate"
                                            updateParams = [newEndDate: formatter.parse(params.bulk_end_date)]
                                            break
                                        case 'bulk_end_volume': updateClause = "ic.endVolume = :newEndVolume"
                                            updateParams = [newEndVolume: params.bulk_end_volume]
                                            break
                                        case 'bulk_end_issue': updateClause = "ic.endIssue = :newEndIssue"
                                            updateParams = [newEndIssue: params.bulk_end_issue]
                                            break
                                        case 'bulk_embargo': updateClause = "ic.embargo = :newEmbargo"
                                            updateParams = [newEmbargo: params.bulk_embargo]
                                    }
                                    if(updateParams && updateClause) {
                                        //prepare for cases when there are more than one coverage statement!
                                        String updateQuery = "update IssueEntitlementCoverage ic set ${updateClause} where ic.issueEntitlement.id in (select ie.id ${query.query})"
                                        IssueEntitlement.executeUpdate(updateQuery, query.queryParams+updateParams)
                                    }
                                    break
                            }
                        }
                    }
                //}
                /*
                else if(params.bulkOperation in ["remove", "removeWithChildren"]) {
                    if(params.bulkOperation == "removeWithChildren") {
                        //case statement fails to evaluate with parameters ... so, let's go the hard-coded way!
                        String deleteMemberQuery = "update IssueEntitlement e set e.status.id = ${RDStore.TIPP_STATUS_REMOVED.id} where e.subscription in (:subscriptions) and e.tipp in (select tipp ${query.query})"
                        query.queryParams.subscriptions.addAll(result.subscription.getDerivedSubscriptions())
                        IssueEntitlement.executeUpdate(deleteMemberQuery, query.queryParams)
                    }
                    else {
                        String deleteQuery = "update IssueEntitlement e set e.status.id = ${RDStore.TIPP_STATUS_REMOVED.id} where e.subscription in (:subscriptions) and e.id in (select ie.id ${query.query})"
                        IssueEntitlement.executeUpdate(deleteQuery, query.queryParams)
                    }
                }
                */
            }
            else {
                params.each { Map.Entry<Object,Object> p ->
                    if (p.key.startsWith('_bulkflag.') && (p.value == 'on')) {
                        String ie_to_edit = p.key.substring(10)
                        IssueEntitlement ie = IssueEntitlement.get(ie_to_edit)
                        //if (params.bulkOperation == "edit") {
                            if (GlobalService.isset(params, 'bulk_access_start_date')) {
                                ie.accessStartDate = formatter.parse(params.bulk_access_start_date)
                            }
                            if (GlobalService.isset(params, 'bulk_access_end_date')) {
                                ie.accessEndDate = formatter.parse(params.bulk_access_end_date)
                            }
                            if (GlobalService.isset(params, 'bulk_notes')) {
                                ie.notes = params.bulk_notes
                            }
                            if (GlobalService.isset(params, 'bulk_local_price') && GlobalService.isset(params, 'bulk_local_currency')) {
                                NumberFormat format = NumberFormat.getInstance( LocaleUtils.getCurrentLocale() )
                                BigDecimal localPrice = format.parse(params.bulk_local_price).doubleValue()
                                RefdataValue localCurrency = RefdataValue.get(params.bulk_local_currency)
                                List<PriceItem> items = PriceItem.executeQuery('select pi from PriceItem pi where pi.issueEntitlement = :ie and (pi.localCurrency = :localCurrency or ((pi.listCurrency = :localCurrency and pi.localCurrency = null) or (pi.listCurrency = null and pi.localCurrency = null)))', [ie: ie, localCurrency: localCurrency])
                                PriceItem pi
                                if(items) {
                                    pi = items[0]
                                }
                                else {
                                    pi = new PriceItem(issueEntitlement: ie)
                                    pi.setGlobalUID()
                                }
                                pi.localPrice = localPrice
                                pi.localCurrency = localCurrency //in case if localCurrency is null
                                pi.save()
                            }
                            if (params.titleGroupInsert) {
                                IssueEntitlementGroup entitlementGroup = IssueEntitlementGroup.get(params.titleGroupInsert)
                                if(entitlementGroup && !IssueEntitlementGroupItem.findByIe(ie)){
                                    IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                            ie: ie,
                                            ieGroup: entitlementGroup)
                                    if (!issueEntitlementGroupItem.save()) {
                                        log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                                    }
                                }
                            }
                            if (GlobalService.isset(params, 'bulk_start_date')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.startDate = :startDate where ic.issueEntitlement = :ie', [ie: ie, startDate: formatter.parse(params.bulk_start_date)])
                            }
                            if (GlobalService.isset(params, 'bulk_start_volume')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.startVolume = :startVolume where ic.issueEntitlement = :ie', [ie: ie, startVolume: params.bulk_start_volume])
                            }
                            if (GlobalService.isset(params, 'bulk_start_issue')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.startIssue = :startIssue where ic.issueEntitlement = :ie', [ie: ie, startIssue: params.bulk_start_issue])
                            }
                            if (GlobalService.isset(params, 'bulk_end_date')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.endDate = :endDate where ic.issueEntitlement = :ie', [ie: ie, endDate: formatter.parse(params.bulk_end_date)])
                            }
                            if (GlobalService.isset(params, 'bulk_end_volume')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.endVolume = :endVolume where ic.issueEntitlement = :ie', [ie: ie, endVolume: params.bulk_end_volume])
                            }
                            if (GlobalService.isset(params, 'bulk_end_issue')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.endIssue = :endIssue where ic.issueEntitlement = :ie', [ie: ie, endIssue: params.bulk_end_issue])
                            }
                            if (GlobalService.isset(params, 'bulk_embargo')) {
                                IssueEntitlementCoverage.executeUpdate('update IssueEntitlementCoverage ic set ic.embargo = :embargo where ic.issueEntitlement = :ie', [ie: ie, embargo: params.bulk_embargo])
                            }
                            if (!ie.save()) {
                                log.error("Problem saving ${ie.errors.getAllErrors().toListString()}")
                                error = true
                            }
                        //}
                        /*
                        else if (params.bulkOperation in ["remove", "removeWithChildren"]) {
                            if(ie.tipp.status == RDStore.TIPP_STATUS_REMOVED) {
                                log.debug("Updating ie ${ie.id} status to removed")
                                ie.status = RDStore.TIPP_STATUS_REMOVED
                            }
                            else {
                                log.debug("Updating ie ${ie.id} status to deleted")
                                ie.status = RDStore.TIPP_STATUS_REMOVED
                            }
                            if (ie.save()) {
                                if(params.bulkOperation == "removeWithChildren")
                                    IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.status = :newStatus where ie.subscription in (select s from Subscription s where s.instanceOf = :parent) and ie.tipp = :tipp', [parent: ie.subscription, tipp: ie.tipp, newStatus: ie.status])
                            }
                            else {
                                log.error("Problem saving ${ie.errors.getAllErrors().toListString()}")
                                error = true
                            }
                        }
                        */
                    }
                }
            }

            if(error)
                [result:result,status:STATUS_ERROR]
            else [result:result,status:STATUS_OK]
        }
    }

    /**
     * Adds an empty price item for the given issue entitlement
     * @param params the request parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> addEmptyPriceItem(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        if(params.ieid) {
            IssueEntitlement ie = IssueEntitlement.get(params.ieid)
            if(ie) {
                PriceItem pi = new PriceItem(issueEntitlement: ie)
                pi.setGlobalUID()
                if(!pi.save()) {
                    log.error(pi.errors.toString())
                    result.error = messageSource.getMessage('subscription.details.addEmptyPriceItem.priceItemNotSaved',null,locale)
                    [result:result,status:STATUS_ERROR]
                }
                result.newItem = pi
            }
            else {
                result.error = messageSource.getMessage('subscription.details.addEmptyPriceItem.issueEntitlementNotFound',null,locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            result.error = messageSource.getMessage('subscription.details.addEmptyPriceItem.noIssueEntitlement',null,locale)
            [result:result,status:STATUS_ERROR]
        }
        [result:result,status:STATUS_OK]
    }

    /**
     * Adds an empty coverage statement for the given issue entitlement
     * @param params the request parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> addCoverage(GrailsParameterMap params) {
        IssueEntitlement base = IssueEntitlement.get(params.issueEntitlement)
        if(base) {
            Map<String,Object> result = [subId:base.subscription.id]
            IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(issueEntitlement: base)
            if(ieCoverage.save()) {
                [result: result, status: STATUS_OK]
            }
            else {
                log.error("Error on creation new coverage statement: ${ieCoverage.errors}")
                [result: result, status: STATUS_ERROR]
            }
        }
        else {
            log.error("Issue entitlement with ID ${params.issueEntitlement} could not be found")
            [result: null, status: STATUS_ERROR]
        }
    }

    /**
     * Removes the given coverage statement from the issue entitlement
     * @param params the request parameter map
     * @return OK if the removal was successful, false otherwise
     */
    Map<String,Object> removeCoverage(GrailsParameterMap params) {
        IssueEntitlementCoverage ieCoverage = IssueEntitlementCoverage.get(params.ieCoverage)
        if(ieCoverage) {
            Map<String,Object> result = [subId:ieCoverage.issueEntitlement.subscription.id]
            PendingChange.executeUpdate('update PendingChange pc set pc.status = :rejected where pc.oid = :oid',[rejected:RDStore.PENDING_CHANGE_REJECTED,oid:"${ieCoverage.class.name}:${ieCoverage.id}"])
            ieCoverage.delete()
            [result:result,status:STATUS_OK]
        }
        else {
            log.error("Issue entitlement coverage with ID ${params.ieCoverage} could not be found")
            [result:null,status:STATUS_ERROR]
        }
    }

    /**
     * Opens the title group view to edit it, setting parameters for the view.
     * If the processing command has been submitted, the form data submitted will be taken and the group updated with the given data
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the editing / the call was successful, ERROR otherwise
     */
    Map<String,Object> editEntitlementGroupItem(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.ie = IssueEntitlement.get(params.ie)
        if(result.ie) {
            switch (params.cmd) {
                case 'edit': [result:result,status:STATUS_OK]
                    break
                case 'processing': List deleteIssueEntitlementGroupItem = []
                    result.ie.ieGroups.each{
                        if(!(it.ieGroup.id.toString() in params.list('titleGroup'))){
                            deleteIssueEntitlementGroupItem << it.id
                        }
                    }
                    if(deleteIssueEntitlementGroupItem){
                        IssueEntitlementGroupItem.executeUpdate("DELETE IssueEntitlementGroupItem iegi where iegi.id in (:iegiIDs)", [iegiIDs: deleteIssueEntitlementGroupItem])
                    }
                    params.list('titleGroup').each {
                        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.get(it)
                        if(issueEntitlementGroup && !IssueEntitlementGroupItem.findByIeAndIeGroup(result.ie, issueEntitlementGroup) && !IssueEntitlementGroupItem.findByIe(result.ie)) {
                            IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(ie: result.ie, ieGroup: issueEntitlementGroup)
                            if (!issueEntitlementGroupItem.save()) {
                                log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                            }
                        }
                    }
                    break
            }
            [result:result,status:STATUS_OK]
        }
        else [result:result,status:STATUS_ERROR]
    }

    /**
     * Takes the submitted parameters and creates a new issue entitlement group if a such does not exist for the
     * given subscription. Checked is the name of the group
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> processCreateEntitlementGroup(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        Locale locale = LocaleUtils.getCurrentLocale()
        if(!IssueEntitlementGroup.findBySubAndName(result.subscription, params.name)) {
            IssueEntitlementGroup issueEntitlementGroup = new IssueEntitlementGroup(name: params.name,
                    description: params.description ?: null,
                    sub: result.subscription)
            if(issueEntitlementGroup.save()) {
                [result:result,status:STATUS_OK]
            }
            else{
                result.error = messageSource.getMessage("issueEntitlementGroup.create.fail",null,locale)
            }
        }
        else {
            result.error = messageSource.getMessage('issueEntitlementGroup.create.alreadyExists',null,locale)
        }
        [result:result,status:STATUS_ERROR]
    }

    /**
     * Clears and removes the given {@link IssueEntitlementGroup}
     * @param params the request parameter map
     * @return OK if the removal succeeded, ERROR otherwise or if the issue entitlement group could not be found
     */
    Map<String,Object> removeEntitlementGroup(GrailsParameterMap params) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.get(params.titleGroup)
        if(issueEntitlementGroup) {
            Map<String,Object> queryParams = [issueEntitlementGroup: issueEntitlementGroup]
            IssueEntitlementGroupItem.executeUpdate("delete from IssueEntitlementGroupItem iegi where iegi.ieGroup = :issueEntitlementGroup", queryParams)
            IssueEntitlementGroup.executeUpdate("delete from IssueEntitlementGroup ieg where ieg = :issueEntitlementGroup", queryParams)
            [result:null,status:STATUS_OK]
        }
        else [result:null,status:STATUS_ERROR]
    }

    /**
     * Creates or removes the given {@link SubscriptionDiscountScale} entry
     * @param controller unused
     * @param params the parameter map containing the command and the new discount scale data
     * @return OK if the update was successful, ERROR if permissions are missing
     */
    Map<String,Object> manageDiscountScale(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            if(result.editable) {
                switch (params.cmd) {
                    case 'createDiscountScale':
                        SubscriptionDiscountScale subscriptionDiscountScale = new SubscriptionDiscountScale(
                                subscription: result.subscription,
                                name: params.name,
                                discount: params.discount,
                                note: params.note).save()
                        params.remove('cmd')

                        break
                    case 'removeDiscountScale':
                        if(params.discountScaleId){
                            SubscriptionDiscountScale sbs = SubscriptionDiscountScale.findById(params.long('discountScaleId'))
                            if(sbs) {
                                sbs.delete()
                            }
                        }
                        params.remove('cmd')
                        break
                }
            }
            params.remove('cmd')
            result.discountScales = result.subscription.discountScales
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Opens a list of potential subscriptions eligible as discount scale copy targets. If submitted,
     * a copy is being processed before reloading the list
     * @param controller unused
     * @param params the parameter map containing the data to process; either for the subscription filter or copy instructions
     * @return OK if the process and / or load was successful, ERROR on insufficient permissions
     */
    Map<String, Object> copyDiscountScales(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (!result)
            [result: null, status: STATUS_ERROR]
        else {
            if (result.editable) {

                if(params.processCopyButton == 'yes') {
                    result.copyDiscountScales = params.copyDiscountScale ? SubscriptionDiscountScale.findAllByIdInList(Params.getLongList(params, 'copyDiscountScale')) : null
                    if (result.copyDiscountScales) {
                        result.targetSubs = params.targetSubs ? Subscription.findAllByIdInList(Params.getLongList(params, 'targetSubs')) : null

                        if (result.targetSubs) {
                            result.targetSubs.each { Subscription sub ->
                                result.copyDiscountScales.each { SubscriptionDiscountScale subscriptionDiscountScale ->
                                    SubscriptionDiscountScale subDisSc = new SubscriptionDiscountScale(name: subscriptionDiscountScale.name,
                                            discount: subscriptionDiscountScale.discount,
                                            note: subscriptionDiscountScale.note,
                                            subscription: sub).save()
                                }
                            }
                        } else {
                            Locale locale = LocaleUtils.getCurrentLocale()
                            result.error = messageSource.getMessage('subscription.details.copyDiscountScales.process.error2', null, locale)
                        }

                    } else {
                        Locale locale = LocaleUtils.getCurrentLocale()
                        result.error = messageSource.getMessage('subscription.details.copyDiscountScales.process.error', null, locale)
                    }
                }

                SwissKnife.setPaginationParams(result, params, (User) result.user)
                Date date_restriction = null
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

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

                List tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params)
                result.filterSet = tmpQ[2]
                List subscriptions = Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1])

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

                if (subscriptions)
                    result.allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndSourceLicenseIsNotNullAndLinkType(result.subscriptions, RDStore.LINKTYPE_LICENSE)
            }

            result.discountScales = result.subscription.discountScales
            [result: result, status: STATUS_OK]
        }
    }

    //--------------------------------------------- renewal section ---------------------------------------------

    /**
     * Takes the submitted base parameters and creates a successor subscription instance with the given input.
     * The new instance will be linked automatically to the predecessor instance
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the copying and basic setup were successful, ERROR otherwise
     */
    Map<String,Object> processRenewSubscription(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            List<Links> previousSubscriptions = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_FOLLOWS)
            if (previousSubscriptions.size() > 0) {
                result.error = messageSource.getMessage('subscription.renewSubExist',null,locale)
                [result:result,status:STATUS_ERROR]
            }
            else {
                Date sub_startDate = params.subscription.start_date ? DateUtils.parseDateGeneric(params.subscription.start_date) : null
                Date sub_endDate = params.subscription.end_date ? DateUtils.parseDateGeneric(params.subscription.end_date) : null
                Year sub_refYear = params.subscription.reference_year ? Year.parse(params.subscription.reference_year) : null
                RefdataValue sub_status = params.subStatus ? RefdataValue.get(params.subStatus) : RDStore.SUBSCRIPTION_NO_STATUS
                boolean sub_isMultiYear = params.subscription.isMultiYear
                String new_subname = params.subscription.name
                Date manualCancellationDate = null
                use(TimeCategory) {
                    manualCancellationDate =  result.subscription.manualCancellationDate ? (result.subscription.manualCancellationDate + 1.year) : null
                }
                Subscription newSub = new Subscription(
                        name: new_subname,
                        startDate: sub_startDate,
                        endDate: sub_endDate,
                        referenceYear: sub_refYear,
                        manualCancellationDate: manualCancellationDate,
                        identifier: UUID.randomUUID().toString(),
                        isSlaved: result.subscription.isSlaved,
                        type: result.subscription.type,
                        kind: result.subscription.kind,
                        resource: result.subscription.resource,
                        form: result.subscription.form,
                        isPublicForApi: result.subscription.isPublicForApi,
                        hasPerpetualAccess: result.subscription.hasPerpetualAccess,
                        hasPublishComponent: result.subscription.hasPublishComponent,
                        holdingSelection: result.subscription.holdingSelection,
                        status: sub_status,
                        isMultiYear: sub_isMultiYear ?: false,
                        administrative: result.subscription.administrative,
                )
                if (!newSub.save()) {
                    log.error("Problem saving subscription ${newSub.errors}");
                    [result:result,status:STATUS_ERROR]
                }
                else {
                    log.debug("Save ok")
                    if(contextService.getOrg().isCustomerType_Consortium()) {
                        if (params.list('auditList')) {
                            //copy audit
                            params.list('auditList').each { auditField ->
                                //All ReferenceFields were copied!
                                //'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'form', 'resource'
                                AuditConfig.addConfig(newSub, auditField)
                            }
                        }
                    }
                    //Copy References
                    //OrgRole
                    result.subscription.orgRelations?.each { or ->
                        if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS,  RDStore.OR_SUBSCRIBER_CONS_HIDDEN])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = newSub
                            newOrgRole.save()
                        }
                    }
                    //link to previous subscription
                    Links prevLink = Links.construct([source: newSub, destination: result.subscription, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])
                    if (!prevLink.save()) {
                        log.error("Problem linking to previous subscription: ${prevLink.errors}")
                    }
                    result.newSub = newSub
                    if (params.targetObjectId == "null") params.remove("targetObjectId")
                        result.isRenewSub = true
                }
                [result:result,status:STATUS_OK]
            }
        }
    }

    //------------------------------------------------ copy section ---------------------------------------------

    /**
     * Initialises the copying of the given subscription, taking the base parameters and creating a copy of the instance
     * @param params the request parameter map
     * @return OK in case of success, ERROR otherwise
     */
    Map<String,Object> copySubscription(GrailsParameterMap params) {
        Map<String,Object> result = setCopyResultGenerics(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
            result.copyObject = true
            if (params.name && !result.targetObject) {
                String sub_name = params.name
                Subscription targetObject = new Subscription(name: sub_name,
                        status: RDStore.SUBSCRIPTION_NO_STATUS,
                        identifier: UUID.randomUUID().toString(),
                        type: result.sourceObject.type,
                        isSlaved: result.sourceObject.isSlaved,
                        administrative: result.sourceObject.administrative
                )
                //Copy InstanceOf
                if (params.targetObject?.copylinktoSubscription) {
                    targetObject.instanceOf = result.sourceObject.instanceOf ?: null
                }
                if (!targetObject.save()) {
                    log.error("Problem saving subscription ${targetObject.errors}")
                    result.error = messageSource.getMessage('default.save.error.general.message',null,locale)
                    [result:result,status:STATUS_ERROR]
                }
                else {
                    result.targetObject = targetObject
                    params.targetObjectId = genericOIDService.getOID(targetObject)
                    //Copy References
                    result.sourceObject.orgRelations.each { OrgRole or ->
                        if ((or.org.id == contextService.getOrg().id) || (or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = result.targetObject
                            newOrgRole.save()
                        }
                    }
                    VendorRole.findAllBySubscription(result.sourceObject).each { VendorRole vr ->
                        VendorRole newVendorRole = new VendorRole(subscription: result.targetObject, vendor: vr.vendor)
                        newVendorRole.save()
                    }
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Gets the copy parameters for the given copy section
     * @param params the request parameter map
     * @return OK if the retrieval was successful, containing also the data for the given step, false otherwise
     */
    Map<String,Object> copyElementsIntoSubscription(GrailsParameterMap params) {
        Map<String,Object> result = setCopyResultGenerics(params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if (params.isRenewSub) {
                result.isRenewSub = params.isRenewSub
            }
            if (params.fromSurvey && result.consortialView) {
                result.fromSurvey = params.fromSurvey
            }
            result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) ?: false
            if (params.copyObject) {
                result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
            }
            result.transferIntoMember = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_LOCAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
            result.allObjects_readRights = subscriptionService.getMySubscriptions_readRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
            result.allObjects_writeRights = subscriptionService.getMySubscriptions_writeRights([status: RDStore.SUBSCRIPTION_CURRENT.id])

            List<String> subTypSubscriberVisible = [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]
            result.isSubscriberVisible = result.sourceObject && result.targetObject &&
                    subTypSubscriberVisible.contains(result.sourceObject._getCalculatedType()) &&
                    subTypSubscriberVisible.contains(result.targetObject._getCalculatedType())
            if (params.targetObjectId) {
                result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
            }
            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Filters the member subscribers by the given parameter map
     * @param params the filter parameter map
     * @param parentSub the subscription whose members should be queried
     * @return a map of structure [sub: subscription, orgs: subscriber] containing the query results
     */
    List<Map> getFilteredSubscribers(GrailsParameterMap params, Subscription parentSub) {
        Map<String, Object> result = [:]

        result.institution = parentSub.getSubscriberRespConsortia()
        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        GrailsParameterMap orgParams = params.clone()
        orgParams.remove("sort")
        orgParams.remove("order")
        params.remove("max")
        params.remove("offset")
        String sort = " order by o.sortname "
        if(params.sort) {
            sort = " order by ${params.sort} "
            if(params.order)
                sort += params.order
        }
        FilterService.Result fsr = filterService.getOrgQuery(orgParams)
        if (fsr.isFilterSet) { orgParams.filterSet = true }

        if (params.filterPropDef) {
            Map<String, Object> efq = propertyService.evalFilterQuery(params, fsr.query, 'o', fsr.queryParams)
            fsr.query = efq.query
            fsr.queryParams = efq.queryParams as Map<String, Object>
        }
        List<Long> filteredOrgIds = Org.executeQuery('select o.id '+fsr.query, fsr.queryParams, orgParams+[id:parentSub.id])

        Set parentSubs = []
        if(params.showMembersSubWithMultiYear){
            params.subRunTimeMultiYear = true
            parentSubs = linksGenerationService.getSuccessionChain(parentSub, 'destinationSubscription')
            sort = " order by o.sortname, sub.referenceYear "
        }
        parentSubs << parentSub

        Set rows = Subscription.executeQuery("select sub,o from OrgRole oo join oo.sub sub join oo.org o where sub.instanceOf in (:parents) and o != :ctx "+sort,[parents:parentSubs, ctx: result.institution])
        List<Map> filteredSubChilds = []
        rows.each { row ->
            Org subscriber = row[1]
            Subscription subscription = row[0]
            if(subscriber.id in filteredOrgIds) {
                if (params.subRunTimeMultiYear || params.subRunTime) {
                    if (params.subRunTimeMultiYear && !params.subRunTime) {
                        if (subscription.isMultiYear) {
                            filteredSubChilds << [sub: subscription, orgs: subscriber]
                        }
                    } else if (!params.subRunTimeMultiYear && params.subRunTime) {
                        if (!subscription.isMultiYear) {
                            filteredSubChilds << [sub: subscription, orgs: subscriber]
                        }
                    } else {
                        filteredSubChilds << [sub: subscription, orgs: subscriber]
                    }
                }
                else {
                    filteredSubChilds << [sub: subscription, orgs: subscriber]
                }
            }
        }
        filteredSubChilds
    }

    /**
     * Sets generic parameters for the element copy calls, ensuring also permission grants
     * @param params the request parameter map
     * @return a result map with common parameters when permissions are granted, null otherwise, resulting in ERROR results
     */
    Map<String,Object> setCopyResultGenerics(GrailsParameterMap params) {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()

        if (params.sourceObjectId == "null")
            params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        if (params.targetObjectId == "null")
            params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(contextService.getOrg(), result.sourceObject)
        result.consortialView = result.showConsortiaFunctions
        result.editable = result.sourceObject?.isEditableBy(result.user)
        if (!result.editable) {
            //the explicit comparison against bool(true) should ensure that not only the existence of the parameter is checked but also its proper value
            if(params.copyMyElements == true) {
                if(contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO))
                    result
            }
            else null
        }
        else if(!result.sourceObject?.isVisibleBy(result.user))
            null
        else result
    }

    /**
     * Sets parameters used for many controller calls and checks permissions to the given subscription instance.
     * If no result map is being returned, the controller calls will return ERROR
     * @param params the request parameter map
     * @param checkOption the permissions to be checked
     * @return a result map with the basic parameters upon success, null on failure which results in returning ERROR status
     */
    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params, String checkOption) {

        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        result.subscription = Subscription.get(params.id)

        if (!params.id && params.subscription) {
            result.subscription = Subscription.get(params.subscription)
        }
        result.contextCustomerType = contextService.getOrg().getCustomerType()
        result.institution = result.subscription ? result.subscription?.getSubscriberRespConsortia() : contextService.getOrg() //TODO temp, remove the duplicate

        if (result.subscription) {
            result.subscriptionConsortia = result.subscription.getConsortium()
            result.inContextOrg = contextService.getOrg().id == result.subscription.getSubscriberRespConsortia().id
            result.licenses = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_LICENSE).collect { Links li -> li.sourceLicense }
            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.subscription)
            result.hasNext = links.nextLink.size() > 0
            result.navPrevSubscription = links.prevLink
            result.navNextSubscription = links.nextLink
            if(result.subscription.instanceOf)
                result.auditConfigs = auditService.getAllAuditConfigs(result.subscription.instanceOf)
            else result.auditConfigs = auditService.getAllAuditConfigs(result.subscription)
            result.titleManipulation = result.subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_PARTIAL
            result.titleManipulationBlocked = result.subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE && auditService.getAuditConfig(result.subscription, 'holdingSelection')

            result.currentTitlesCounts = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :status ", [sub: result.subscription, status: RDStore.TIPP_STATUS_CURRENT])[0]

            if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
                if(result.subscription.instanceOf){
                    List subscrCostCounts = CostItem.executeQuery('select count(*) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.surveyOrg = null and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: contextService.getOrg(), deleted: RDStore.COST_ITEM_DELETED])
                    result.currentCostItemCounts = subscrCostCounts ? subscrCostCounts[0] : 0
                    result.currentSurveysCounts = SurveyConfig.executeQuery("select count(*) from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                            [sub: result.subscription.instanceOf,
                             org: result.subscription.getSubscriberRespConsortia(),
                             invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]])[0]
                }else{
                    result.currentSurveysCounts = SurveyConfig.executeQuery("select count(*) from SurveyConfig as surConfig where surConfig.subscription = :sub", [sub: result.subscription])[0]
                    List subscrCostCounts = CostItem.executeQuery('select count(*) from CostItem ci where ci.sub.instanceOf = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED]),
                    ownCostCounts = CostItem.executeQuery('select count(*) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED])
                    int subscrCount = subscrCostCounts ? subscrCostCounts[0] : 0
                    int ownCount = ownCostCounts ? ownCostCounts[0] : 0
                    result.currentCostItemCounts = "${ownCount}/${subscrCount}"
                }
                result.currentMembersCounts =  Subscription.executeQuery('select count(*) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',[parent: result.subscription, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])[0]
            }else{
                result.currentSurveysCounts = SurveyConfig.executeQuery("select count(*) from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                        [sub: result.subscription.instanceOf,
                         org: result.subscription.getSubscriberRespConsortia(),
                         invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]])[0]
                List subscrCostCounts = CostItem.executeQuery('select count(*) from CostItem ci where ci.sub = :sub and ci.isVisibleForSubscriber = true and ci.costItemStatus != :deleted', [sub: result.subscription, deleted: RDStore.COST_ITEM_DELETED])
                int subscrCount = subscrCostCounts ? subscrCostCounts[0] : 0
                if(result.contextCustomerType == CustomerTypeService.ORG_INST_PRO) {
                    List ownCostCounts = CostItem.executeQuery('select count(*) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED])
                    int ownCount = ownCostCounts ? ownCostCounts[0] : 0
                    if(result.subscription.instanceOf)
                        result.currentCostItemCounts = "${ownCount}/${subscrCount}"
                    else
                        result.currentCostItemCounts = ownCount
                }
                else {
                    result.currentCostItemCounts = "${subscrCount}"
                }
            }
            result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(contextService.getOrg(), result.subscription)

            int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.subscription).size()
            int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.subscription).size()
            result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

            result.notesCount       = docstoreService.getNotesCount(result.subscription, contextService.getOrg())
            result.docsCount       = docstoreService.getDocsCount(result.subscription, contextService.getOrg())
            result.checklistCount   = workflowService.getWorkflowCount(result.subscription, contextService.getOrg())

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

            result.providerRoles = subscriptionService.getVisibleProviders(result.subscription)
            result.vendorRoles = subscriptionService.getVisibleVendors(result.subscription)
        }
        else {
            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                result.editable = contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
            }
        }
        result.consortialView = result.showConsortiaFunctions ?: contextService.getOrg().isCustomerType_Consortium()

        Map args = [:]
        if (result.consortialView) {
            Locale locale = LocaleUtils.getCurrentLocale()

            args.superOrgType       = [messageSource.getMessage('consortium.superOrgType', null, locale)]
            args.memberTypeSingle   = [messageSource.getMessage('consortium.subscriber', null, locale)]
            args.memberType         = [messageSource.getMessage('consortium.member.plural', null, locale)]
            args.memberTypeGenitive = [messageSource.getMessage('consortium.subscriber', null, locale)]
        }
        result.args = args

        result
    }

    //--------------------------------------------- reporting -------------------------------------------------

    /**
     * Initialises the reporting index for the given subscription
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> reporting(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription sub = Subscription.get(params.id)

//        result.token           = params.token ?: RandomStringUtils.randomAlphanumeric(24) // -> static token
        result.token           = params.token ?: RandomStringUtils.randomAlphanumeric(16) + '#' + params.id // -> static token
        result.cfgQueryList    = SubscriptionReport.getCurrentQueryConfig( sub )
        result.cfgChartsList   = BaseConfig.CHARTS
        result.cfgTimelineList = SubscriptionReport.getCurrentTimelineConfig( sub )

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    //--------------------------------------------- workflows -------------------------------------------------

    /**
     * Gets the workflows linked to the given subscription
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> workflows(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        workflowService.executeCmdAndUpdateResult(result, params)

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }
}
