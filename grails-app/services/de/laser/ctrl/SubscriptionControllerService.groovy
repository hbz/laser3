package de.laser.ctrl

import com.k_int.kbplus.*
import de.laser.*
import de.laser.auth.User
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.OrgProperty
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.reporting.report.local.SubscriptionReport
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter5ApiSource
import de.laser.workflow.WfWorkflow
import grails.converters.JSON
import grails.doc.internal.StringEscapeCategory
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.BatchingStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.grails.orm.hibernate.cfg.PropertyConfig
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.transaction.TransactionStatus
import org.springframework.web.multipart.MultipartFile

import java.sql.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

/**
 * This service is a container for those methods of the {@link SubscriptionController} which contain complex data manipulation operations
 */
@Transactional
class SubscriptionControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    FilterService filterService
    FormService formService
    TaskService taskService
    AddressbookService addressbookService
    FinanceService financeService
    FinanceControllerService financeControllerService
    FactService factService
    SubscriptionService subscriptionService
    PropertyService propertyService
    ContextService contextService
    EscapeService escapeService
    ExecutorService executorService
    PackageService packageService
    GokbService gokbService
    SurveyService surveyService
    AuditService auditService
    GlobalSourceSyncService globalSourceSyncService
    LinksGenerationService linksGenerationService
    ExecutorWrapperService executorWrapperService
    GenericOIDService genericOIDService
    MessageSource messageSource
    WorkflowService workflowService
    ManagementService managementService
    PendingChangeService pendingChangeService
    def sessionFactory

    //-------------------------------------- general or ungroupable section -------------------------------------------

    /**
     * Loads the given subscription details and returns them to the details view. If mandatory properties are missing,
     * they will be created; that is why this method needs to be transactional
     * @param params the request parameter map
     * @return the given subscription's details
     */
    Map<String,Object> show(GrailsParameterMap params) {
        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('1')
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            pu.setBenchmark('this-n-that')
            if (result.institution) {
                result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
            }
            pu.setBenchmark('packages')
            // ---- pendingChanges : start
            result.pendingChangeConfigSettings = RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)
            /*if (executorWrapperService.hasRunningProcess(result.subscription)) {
                log.debug("PendingChange processing in progress")
                result.processingpc = true
            }
            else {
                //pc.msgParams null check is the legacy check; new pending changes should NOT be displayed here but on dashboard and only there!
                List<PendingChange> pendingChanges = PendingChange.executeQuery(
                        "select pc from PendingChange as pc where subscription = :sub and ( pc.status is null or pc.status = :status ) and pc.msgParams is not null order by pc.ts desc",
                        [sub: result.subscription, status: RDStore.PENDING_CHANGE_PENDING]
                )
                log.debug("pc result is ${result.pendingChanges}")
                if (result.subscription.isSlaved && ! pendingChanges.isEmpty()) {
                    log.debug("Slaved subscription, auto-accept pending changes")
                    List changesDesc = []
                    pendingChanges.each { change ->
                        if (!pendingChangeService.performAccept(change)) {
                            log.debug("Auto-accepting pending change has failed.")
                        } else {
                            changesDesc.add(change.desc)
                        }
                    }
                    //ERMS-1844 Hotfix: Änderungsmitteilungen ausblenden
                    //result.message = changesDesc
                } else {
                    result.pendingChanges = pendingChanges
                }
            }*/
            // ---- pendingChanges : end
            pu.setBenchmark('tasks')
            // TODO: experimental asynchronous task
            //def task_tasks = task {
            // tasks
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.subscription)
            Map<String,Object> preCon = taskService.getPreconditionsWithoutTargets(result.contextOrg)
            result << preCon
            Set<Long> excludes = [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id]
            if(result.institution.getCustomerType() == "ORG_CONSORTIUM")
                excludes << RDStore.OR_SUBSCRIPTION_CONSORTIA.id
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = result.subscription.orgRelations.findAll { OrgRole oo -> !(oo.roleType.id in excludes) }
            //}
            pu.setBenchmark('properties')
            // TODO: experimental asynchronous task
            //def task_properties = task {
            // -- private properties
            // create mandatory OrgPrivateProperties if not existing
            List<PropertyDefinition> mandatories = PropertyDefinition.getAllByDescrAndMandatoryAndTenant(PropertyDefinition.SUB_PROP, true, result.contextOrg)
            mandatories.each { PropertyDefinition pd ->
                if (!SubscriptionProperty.findAllByOwnerAndTypeAndTenantAndIsPublic(result.subscription, pd, result.contextOrg, false)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.subscription, pd, result.contextOrg)
                    if (newProp.hasErrors()) {
                        log.error(newProp.errors.toString())
                    } else {
                        log.debug("New subscription private property created via mandatory: " + newProp.type.name)
                    }
                }
            }
            // -- private properties
            result.modalPrsLinkRole = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.contextOrg)
            result.visiblePrsLinks = []
            result.subscription.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic) {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        if (result.contextOrg.id == pl.prs.tenant.id) {
                            result.visiblePrsLinks << pl
                        }
                    }
                }
            }
            //}
            if(ConfigUtils.getShowStatsInfo()) {
                pu.setBenchmark('usage')
                // TODO: experimental asynchronous task
                //def task_usage = task {
                // usage
                pu.setBenchmark('before platform query')
                Set suppliers = Platform.executeQuery('select plat.id from IssueEntitlement ie join ie.tipp tipp join tipp.platform plat where ie.subscription = :sub', [sub: result.subscription])
                if (suppliers.size() > 1) {
                    log.debug('Found different content platforms for this subscription, cannot show usage')
                } else {
                    pu.setBenchmark('before loading platform')
                    Long supplier_id = suppliers[0]
                    PlatformProperty platform = PlatformProperty.executeQuery('select pp from PlatformProperty pp join pp.type pd where pp.owner.id = :owner and pd.name = :name and pd.descr = :descr', [owner: supplier_id, name: 'NatStat Supplier ID', descr: PropertyDefinition.PLA_PROP])[0]
                    //        PlatformProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
                    pu.setBenchmark('before institutional usage identifier')
                    result.natStatSupplierId = platform?.stringValue ?: null
                    if (result.institutional_usage_identifier != OrgSetting.SETTING_NOT_FOUND) {
                        pu.setBenchmark('before usage data')
                        def fsresult = factService.generateUsageData(result.institution.id, supplier_id, result.subscription)
                        pu.setBenchmark('before usage data sub period')
                        def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(result.institution.id, supplier_id, result.subscription)
                        Set<RefdataValue> holdingTypes = RefdataValue.executeQuery('select tipp.titleType from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :context', [context: result.subscription])
                        if (!holdingTypes) {
                            log.debug('No types found, maybe there are no issue entitlements linked to subscription')
                        } else if (holdingTypes.size() > 1) {
                            log.info('Different content type for this license, cannot calculate Cost Per Use.')
                        } else if (!fsLicenseResult.isEmpty() && result.subscription.startDate) {
                            def existingReportMetrics = fsLicenseResult.y_axis_labels*.split(':')*.last()
                            pu.setBenchmark('before total cost per use')
                            def costPerUseMetricValuePair = factService.getTotalCostPerUse(result.subscription, holdingTypes.first(), existingReportMetrics)
                            if (costPerUseMetricValuePair) {
                                result.costPerUseMetric = costPerUseMetricValuePair[0]
                                result.totalCostPerUse = costPerUseMetricValuePair[1]
                                result.currencyCode = NumberFormat.getCurrencyInstance().getCurrency().currencyCode
                            }
                        }
                        result.statsWibid = result.institution.getIdentifierByType('wibid')?.value
                        if (result.statsWibid && result.natStatSupplierId) {
                            result.usageMode = accessService.checkPerm("ORG_CONSORTIUM") ? 'package' : 'institution'
                            result.usage = fsresult?.usage
                            result.missingMonths = fsresult?.missingMonths
                            result.missingSubscriptionMonths = fsLicenseResult?.missingMonths
                            result.x_axis_labels = fsresult?.x_axis_labels
                            result.y_axis_labels = fsresult?.y_axis_labels
                            result.lusage = fsLicenseResult?.usage
                            pu.setBenchmark('before last usage period for report type')
                            result.lastUsagePeriodForReportType = factService.getLastUsagePeriodForReportType(result.natStatSupplierId, result.statsWibid)
                            result.l_x_axis_labels = fsLicenseResult?.x_axis_labels
                            result.l_y_axis_labels = fsLicenseResult?.y_axis_labels
                        }
                    } else
                        log.info('institutional usage identifier not available')
                }/*
                if(SpringSecurityUtils.ifAnyGranted("ROLE_YODA")) {
                    result.tokens = []
                    Org requestee = result.subscription.getSubscriber()
                    def pw = OrgSetting.get(result.institution, OrgSetting.KEYS.LASERSTAT_SERVER_KEY)
                    if(pw == OrgSetting.SETTING_NOT_FOUND) {
                        pw = OrgSetting.add(result.institution, OrgSetting.KEYS.LASERSTAT_SERVER_KEY, org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(24))
                    }
                    SecretKeySpec secret = new SecretKeySpec(pw.getValue().getBytes(), "HmacSHA256")
                    Mac mac = Mac.getInstance("HmacSHA256")
                    mac.init(secret)
                    Set<String> platforms = Platform.executeQuery('select plat.globalUID from IssueEntitlement ie join ie.tipp tipp join tipp.platform plat where ie.subscription = :subscription',[subscription: result.subscription])
                    platforms.each { String platformGlobalUID ->
                        String data = "requestor=${result.institution.globalUID}&customer=${requestee.globalUID}&platform=${platformGlobalUID}"
                        byte[] rawHmac = mac.doFinal(data.getBytes())
                        result.tokens << rawHmac.encodeHex()
                        result.requestData = data
                    }
                    if(result.tokens.size() > 0)
                        result.subStats = true //development only
                }*/
            }
            //}
            pu.setBenchmark('costs')
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
            pu.setBenchmark('provider & agency filter')
            // TODO: experimental asynchronous task
            //def task_providerFilter = task {
            //}
            result.publicSubscriptionEditors = Person.getPublicByOrgAndObjectResp(null, result.subscription, 'Specific subscription editor')
            if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_ADMINISTRATIVE,CalculatedType.TYPE_CONSORTIAL]) {
                pu.setBenchmark('non-inherited member properties')
                List<Subscription> childSubs = result.subscription.getNonDeletedDerivedSubscriptions()
                if(childSubs) {
                    String localizedName
                    switch(LocaleContextHolder.getLocale()) {
                        case Locale.GERMANY:
                        case Locale.GERMAN: localizedName = "name_de"
                            break
                        default: localizedName = "name_en"
                            break
                    }
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )

                    result.memberProperties = memberProperties
                }
            }
            List bm = pu.stopBenchmark()
            result.benchMark = bm

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
            int offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.putAll(taskService.getTasks(offset, (User) result.user, (Org) result.institution, result.subscription))
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Reveals the inheritance history for the given subscription
     * @param controller unused
     * @param params the request parameter map
     * @return a list of audit log events for the given subscription
     */
    Map<String,Object> history(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)
            Map<String, Object> qry_params = [cname: result.subscription.class.name, poid: result.subscription.id.toString()] //persistentObjectId is of type String
            Set<AuditLogEvent> historyLines = AuditLogEvent.executeQuery("select e from AuditLogEvent as e where className = :cname and persistedObjectId = :poid order by id desc", qry_params)
            result.historyLinesTotal = historyLines.size()
            result.historyLines = historyLines.drop(result.offset).take(result.max)

            [result:result,status:STATUS_OK]
        }
    }

    @Deprecated
    Map<String,Object> changes(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)
            Map<String, Object> baseParams = [sub: result.subscription, stats: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED]]
            Set<PendingChange> todoHistoryLines = PendingChange.executeQuery("select pc from PendingChange as pc where pc.subscription = :sub and pc.status in (:stats) order by pc.ts desc", baseParams)
            result.todoHistoryLinesTotal = todoHistoryLines.size()
            result.todoHistoryLines = todoHistoryLines.drop(result.offset).take(result.max)

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
    Map<String, Object> stats(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        SwissKnife.setPaginationParams(result, params, result.user)
        if(!result)
            [result: null, status: STATUS_ERROR]
        else {
            ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
            Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: result.subscription])
            if(!subscribedPlatforms) {
                subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: result.subscription])
            }
            result.platformInstanceRecords = [:]
            subscribedPlatforms.each { Platform platformInstance ->
                Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${platformInstance.gokbId}")
                if (queryResult.error && queryResult.error == 404) {
                    result.wekbServerUnavailable = messageSource.getMessage('wekb.error.404', null, LocaleContextHolder.getLocale())
                }
                else if (queryResult.warning) {
                    List records = queryResult.warning.records
                    result.platformInstanceRecords[platformInstance.gokbId] = records ? records[0] : [:]
                }
            }
            //at this point, we should be sure that at least the parent subscription has a holding!
            Set<Subscription> refSubs
            if (params.statsForSurvey == true) {
                if(params.loadFor == 'allIEsStats')
                    refSubs = [result.subscription.instanceOf] //look at statistics of the whole set of titles, i.e. of the consortial parent subscription
                else if(params.loadFor == 'holdingIEsStats')
                    refSubs = result.subscription._getCalculatedPrevious() //look at the statistics of the member, i.e. the member's stock of the previous year
            }
            else if(subscriptionService.getCurrentIssueEntitlementIDs(result.subscription).size() > 0){
                refSubs = [result.subscription]
            }
            else refSubs = [result.subscription.instanceOf]
            Set<Counter4Report> c4usages = []
            Set<Counter5Report> c5usages = []
            List count4check = [], c4sums = [], count5check = [], c5sums = [], monthsInRing = []
            if(!params.tab)
                params.tab = 'total'
            if(subscribedPlatforms && refSubs) {
                String sort
                if(params.sort && !params.exportXLS) {
                    String secondarySort
                    switch(params.sort) {
                        case 'reportType': secondarySort = ", title.name asc, r.reportFrom desc"
                            break
                        case 'title.name': secondarySort = ", r.reportType asc, r.reportFrom desc"
                            break
                        case 'reportFrom': secondarySort = ", title.name asc, r.reportType asc"
                            break
                        default: secondarySort = ", title.name asc, r.reportType asc, r.reportFrom desc"
                            break
                    }
                    sort = "${params.sort} ${params.order} ${secondarySort}"
                }
                else {
                    sort = "title.name asc, r.reportType asc, r.metricType asc, r.reportFrom desc"
                }
                result.subscribedPlatforms = subscribedPlatforms
                ArrayList<Object> filterData = prepareFilter(params, result)
                String filter = filterData[0], dateRange = filterData[1], sqlDateRange = filterData[4]
                Map<String, Object> queryParams = filterData[2] as Map<String, Object>
                queryParams.refSubs = refSubs
                queryParams.acceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
                queryParams.current = RDStore.TIPP_STATUS_CURRENT
                monthsInRing.addAll(filterData[3] as List)
                result.dateRun = GregorianCalendar.getInstance().getTime()
                result.startDate = queryParams.startDate
                result.endDate = queryParams.endDate
                result.customer = queryParams.customer.id
                result.platforms = queryParams.platforms.collect { Platform plat -> plat.id }
                Map<String, Object> c5CheckParams = [customer: queryParams.customer, platforms: queryParams.platforms, refSubs: refSubs, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED, current: RDStore.TIPP_STATUS_CURRENT]
                if(dateRange) {
                    c5CheckParams.startDate = queryParams.startDate
                    c5CheckParams.endDate = queryParams.endDate
                }
                Session sess = sessionFactory.getCurrentSession()
                SQLQuery query = sess.createSQLQuery('select * from counter5report where c5r_report_institution_fk = :customer and c5r_platform_fk in (:platforms) and (c5r_title_fk in (select ie_tipp_fk from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk in (:refSubs) and ie_accept_status_rv_fk = :acceptStatus and ie_status_rv_fk = :current and tipp_status_rv_fk = :current) or c5r_title_fk is null)'+sqlDateRange)
                query.setParameter('customer', c5CheckParams.customer.id)
                query.setParameterList('platforms', c5CheckParams.platforms.collect { Platform plat -> plat.id })
                query.setParameterList('refSubs', refSubs.collect{ Subscription s -> s.id })
                query.setParameter('acceptStatus', RDStore.IE_ACCEPT_STATUS_FIXED.id)
                query.setParameter('current', RDStore.TIPP_STATUS_CURRENT.id)
                if(c5CheckParams.startDate)
                    query.setParameter('startDate', new Timestamp(c5CheckParams.startDate.getTime()))
                if(c5CheckParams.endDate)
                    query.setParameter('endDate', new Timestamp(c5CheckParams.endDate.getTime()))
                query.addEntity(Counter5Report)
                count5check.addAll(query.list())
                //count5check.addAll(Counter5Report.executeQuery('select count(r.id) from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus) or r.title is null)'+dateRange, c5CheckParams, [max: 1]))
                if(count5check.size() == 0) {
                    Set availableReportTypes = Counter4Report.executeQuery('select r.reportType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange+' order by r.reportType asc', c5CheckParams)
                    result.reportTypes = availableReportTypes
                    if(!params.reportType) {
                        if(availableReportTypes)
                            result.reportType = availableReportTypes[0]
                        else result.reportType = Counter4ApiSource.BOOK_REPORT_1
                    }
                    else result.reportType = params.reportType
                    filter += " and r.reportType in (:reportType) "
                    queryParams.reportType = result.reportType
                    Set availableMetricTypes = Counter4Report.executeQuery('select r.metricType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and r.reportType in (:reportType) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange, c5CheckParams+[reportType: result.reportType])
                    result.metricTypes = availableMetricTypes
                    if(!params.metricType) {
                        if(availableMetricTypes)
                            result.metricType = availableMetricTypes[0]
                        else result.metricType = 'ft_total'
                    }
                    else result.metricType = params.metricType
                    filter += " and r.metricType = :metricType "
                    queryParams.metricType = result.metricType
                    c4sums.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, r.category as reportCategory, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.reportFrom, r.metricType, r.reportType, r.category order by r.reportFrom asc, r.metricType asc', queryParams))
                    c4usages.addAll(Counter4Report.executeQuery('select r from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                    count4check.addAll(Counter4Report.executeQuery('select count(r.id) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange, queryParams))
                    result.total = count4check.get(0)
                    result.sums = c4sums
                    result.usages = c4usages
                    Map<String, Object> monthQueryParams = queryParams.clone()
                    monthQueryParams.remove('startDate')
                    monthQueryParams.remove('endDate')
                    monthQueryParams.monthsInRing = monthsInRing
                    result.monthsInRing = Counter4Report.executeQuery('select r.reportFrom from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null) and r.reportFrom in (:monthsInRing)'+filter+' group by r.reportFrom, r.metricType, r.reportType order by r.reportFrom asc, r.metricType asc', monthQueryParams) as Set
                }
                else {
                    Set availableReportTypes = Counter5Report.executeQuery('select lower(r.reportType) from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange+' order by r.reportType asc', c5CheckParams)
                    result.reportTypes = availableReportTypes
                    if(!params.reportType) {
                        if(availableReportTypes)
                            result.reportType = availableReportTypes[0]
                        else result.reportType = Counter5ApiSource.TITLE_MASTER_REPORT.toLowerCase()
                    }
                    else result.reportType = params.reportType
                    filter += " and lower(r.reportType) in (:reportType) "
                    queryParams.reportType = result.reportType
                    Set availableMetricTypes = [], availableAccessTypes = []
                    List metricAccessRows = Counter5Report.executeQuery('select r.metricType, r.accessType from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and lower(r.reportType) in (:reportType) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange, c5CheckParams+[reportType: result.reportType])
                    metricAccessRows.each { rows ->
                        availableMetricTypes << rows[0]
                        availableAccessTypes << rows[1]
                    }
                    result.metricTypes = availableMetricTypes
                    result.accessTypes = availableAccessTypes
                    if(!params.metricType) {
                        if(availableMetricTypes)
                            result.metricType = availableMetricTypes[0]
                        else result.metricType = 'Total_Item_Investigations'
                    }
                    else result.metricType = params.metricType
                    filter += " and r.metricType = :metricType "
                    queryParams.metricType = result.metricType
                    c5sums.addAll(Counter5Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.reportFrom, r.metricType, r.reportType order by r.reportFrom asc, r.metricType asc', queryParams))
                    c5usages.addAll(Counter5Report.executeQuery('select r from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                    result.total = Counter5Report.executeQuery('select count(r) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange, queryParams).get(0)
                    result.sums = c5sums
                    result.usages = c5usages
                    Map<String, Object> monthQueryParams = queryParams.clone()
                    monthQueryParams.remove('startDate')
                    monthQueryParams.remove('endDate')
                    monthQueryParams.monthsInRing = monthsInRing
                    result.monthsInRing = Counter5Report.executeQuery('select r.reportFrom from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null) and r.reportFrom in (:monthsInRing)'+filter+' group by r.reportFrom, r.metricType, r.reportType order by r.reportFrom asc, r.metricType asc', monthQueryParams) as Set
                }
            }
            else {
                result.metricTypes = []
                result.reportTypes = []
                result.monthsInRing = []
            }
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Gets the usage data for the given subscription and prepares the available report types in Excel sheets
     * @param params the request parameter map
     * @return a map containing the usage data
     */
    Map<String, Object> statsForExport(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result: null, status: STATUS_ERROR]
        else {
            Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: result.subscription])
            //at this point, we should be sure that at least the parent subscription has a holding!
            Set<Subscription> refSubs
            if (params.statsForSurvey == true) {
                if(params.loadFor == 'allIEsStats')
                    refSubs = [result.subscription.instanceOf] //look at statistics of the whole set of titles, i.e. of the consortial parent subscription
                else if(params.loadFor == 'holdingIEsStats')
                    refSubs = result.subscription._getCalculatedPrevious() //look at the statistics of the member, i.e. the member's stock of the previous year
            }
            else if(subscriptionService.getCurrentIssueEntitlementIDs(result.subscription).size() > 0){
                refSubs = [result.subscription]
            }
            else refSubs = [result.subscription.instanceOf]
            Set<Counter4Report> c4usages = []
            Set<Counter5Report> c5usages = []
            List c4sums = [], c5sums = [], monthsInRing = []
            if(subscribedPlatforms && refSubs) {
                String sort = "title.name asc, r.reportType asc, r.reportFrom desc"
                result.subscribedPlatforms = subscribedPlatforms
                ArrayList<Object> filterData = prepareFilter(params, result)
                String filter = '', dateRange = filterData[1]
                if(params.data != 'fetchAll')
                    filter += filterData[0]
                Map<String, Object> queryParams = filterData[2] as Map<String, Object>
                monthsInRing.addAll(filterData[3] as List)
                result.dateRun = GregorianCalendar.getInstance().getTime()
                result.startDate = queryParams.startDate
                result.endDate = queryParams.endDate
                queryParams.refSubs = refSubs
                queryParams.acceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
                queryParams.current = RDStore.TIPP_STATUS_CURRENT
                Map<String, Object> c5CheckParams = [customer: queryParams.customer, platforms: queryParams.platforms, refSubs: refSubs, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED, current: RDStore.TIPP_STATUS_CURRENT]
                if(dateRange) {
                    c5CheckParams.startDate = queryParams.startDate
                    c5CheckParams.endDate = queryParams.endDate
                }
                if(Counter5Report.executeQuery('select count(r.id) from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange, c5CheckParams)?.get(0) == 0) {
                    if(params.data == 'fetchFiltered' && params.reportType && params.metricType) {
                        filter += " and r.reportType in (:reportType) "
                        queryParams.reportType = params.reportType
                        filter += " and r.metricType = :metricType "
                        queryParams.metricType = params.metricType
                    }
                    Map<String, Object> platformReportParams = queryParams.clone()
                    platformReportParams.remove("refSubs")
                    platformReportParams.remove("acceptStatus")
                    platformReportParams.remove("current")
                    platformReportParams.pr = Counter4ApiSource.PLATFORM_REPORT_1
                    Set availableMetricTypes = Counter4Report.executeQuery('select r.metricType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and r.reportType in (:reportType) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange, c5CheckParams+[reportType: result.reportType])
                    result.metricTypes = availableMetricTypes
                    result.total = Counter4Report.executeQuery('select new map(r.metricType as metricType, r.reportType as reportType, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.reportType, r.metricType order by r.metricType asc', queryParams)
                    if(params.data == 'fetchAll')
                        result.total.addAll(Counter4Report.executeQuery('select new map(r.metricType as metricType, r.publisher as publisher, r.reportType as reportType, sum(r.reportCount) as reportCount) from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and r.reportType = :pr'+filter+dateRange+' group by r.reportType, r.metricType, r.publisher order by r.metricType asc', platformReportParams))
                    if(params.data == 'fetchAll') {
                        c4sums.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, r.category as reportCategory, r.platform as platform, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null) and r.reportType != :pr'+filter+dateRange+' group by r.reportFrom, r.metricType, r.reportType, r.category, r.platform order by r.reportFrom asc, r.metricType asc', queryParams+[pr: Counter4ApiSource.PLATFORM_REPORT_1]))
                        c4sums.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.publisher as publisher, r.reportFrom as reportMonth, r.metricType as metricType, r.category as reportCategory, r.platform as platform, sum(r.reportCount) as reportCount) from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and r.reportType = :pr' + filter + dateRange + ' group by r.reportFrom, r.metricType, r.reportType, r.category, r.platform, r.publisher order by r.reportFrom asc, r.metricType asc', platformReportParams))
                    }
                    else c4sums.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, r.category as reportCategory, r.platform as platform, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.reportFrom, r.metricType, r.reportType, r.category, r.platform order by r.reportFrom asc, r.metricType asc', queryParams))
                    c4usages.addAll(Counter4Report.executeQuery('select r from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                    result.sums = c4sums
                    result.usages = c4usages
                    result.reportTypes = Counter4Report.executeQuery('select r.reportType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange+' order by r.reportFrom asc', c5CheckParams)
                    result.revision = 'counter4'
                }
                else {
                    if(params.data == 'fetchFiltered' && params.reportType && params.metricType) {
                        filter += " and lower(r.reportType) in (:reportType) "
                        queryParams.reportType = params.reportType
                        filter += " and r.metricType = :metricType "
                        queryParams.metricType = params.metricType
                    }
                    Set availableMetricTypes = Counter5Report.executeQuery('select r.metricType from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and lower(r.reportType) in (:reportType) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange, c5CheckParams+[reportType: result.reportType])
                    result.metricTypes = availableMetricTypes
                    result.total = Counter5Report.executeQuery('select new map(r.metricType as metricType, r.reportType as reportType, sum(r.reportCount) as reportCount) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.metricType, r.reportType order by r.metricType asc', queryParams)
                    c5sums.addAll(Counter5Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, r.platform as platform, sum(r.reportCount) as reportCount) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' group by r.reportFrom, r.metricType, r.reportType, r.platform order by r.reportFrom asc, r.metricType asc', queryParams))
                    c5usages.addAll(Counter5Report.executeQuery('select r from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                    result.sums = c5sums
                    result.usages = c5usages
                    result.reportTypes = Counter5Report.executeQuery('select r.reportType from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and (r.title.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription in (:refSubs) and ie.acceptStatus = :acceptStatus and ie.status = :current and ie.tipp.status = :current) or r.title is null)'+dateRange+' order by r.reportFrom asc', c5CheckParams)
                    result.revision = 'counter5'
                }
            }
            result.monthsInRing = monthsInRing
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Takes the given request parameters and assembles the query filter for the usage query
     * @param params the request parameter map containing filter input
     * @param result the generics result map, containing implicite relevant data
     * @return a list of filter data, of structure:
     * <ol start="0">
     *     <li>HQL filter string</li>
     *     <li>HQL date range</li>
     *     <li>query parameter map</li>
     *     <li>months contained in the observed time span</li>
     *     <li>SQL date range</li>
     * </ol>
     */
    ArrayList<Object> prepareFilter(GrailsParameterMap params, Map<String, Object> result) {
        String dateRange, sqlDateRange, filter = ""
        List monthsInRing = []
        Map<String, Object> queryParams = [customer: result.subscription.getSubscriber(), platforms: result.subscribedPlatforms]
        Calendar startTime = GregorianCalendar.getInstance(), endTime = GregorianCalendar.getInstance()
        if(result.subscription.startDate && result.subscription.endDate) {
            dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
            sqlDateRange = " and c5r_report_from >= :startDate and c5r_report_to <= :endDate "
            if(params.tab == 'total' || params.data == 'fetchAll') {
                queryParams.startDate = result.subscription.startDate
                queryParams.endDate = result.subscription.endDate
            }
            else {
                Calendar filterTime = GregorianCalendar.getInstance()
                Date filterDate = DateUtils.getSDF_yearMonth().parse(params.tab)
                filterTime.setTime(filterDate)
                queryParams.startDate = filterDate
                filterTime.set(Calendar.DATE,filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
                queryParams.endDate = filterTime.getTime()
            }
            startTime.setTime(result.subscription.startDate)
            if(result.subscription.endDate < new Date())
                endTime.setTime(result.subscription.endDate)
        }
        else if(result.subscription.startDate) {
            dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
            sqlDateRange = " and c5r_report_from >= :startDate and c5r_report_to <= :endDate "
            if(params.tab == 'total' || params.data == 'fetchAll') {
                queryParams.startDate = result.subscription.startDate
                queryParams.endDate = new Date()
            }
            else {
                Calendar filterTime = GregorianCalendar.getInstance()
                Date filterDate = DateUtils.getSDF_yearMonth().parse(params.tab)
                filterTime.setTime(filterDate)
                queryParams.startDate = filterDate
                filterTime.set(Calendar.DATE, filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
                queryParams.endDate = filterTime.getTime()
            }
            startTime.setTime(result.subscription.startDate)
            endTime.setTime(new Date())
        }
        else {
            if(params.tab == 'total') {
                dateRange = ''
                sqlDateRange = ''
            }
            else {
                dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                sqlDateRange = " and c5r_report_from >= :startDate and c5r_report_to <= :endDate "
                Calendar filterTime = GregorianCalendar.getInstance()
                Date filterDate = DateUtils.getSDF_yearMonth().parse(params.tab)
                filterTime.setTime(filterDate)
                queryParams.startDate = filterDate
                filterTime.set(Calendar.DATE,filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
                queryParams.endDate = filterTime.getTime()
            }
            startTime.set(Calendar.DAY_OF_YEAR, 1)
            endTime.setTime(new Date())
        }
        if(params.tab && params.tab != 'total' && params.exportXLS && params.data != 'fetchAll') {
            monthsInRing << DateUtils.getSDF_yearMonth().parse(params.tab)
        }
        else {
            while(startTime.before(endTime)) {
                monthsInRing << startTime.getTime()
                startTime.add(Calendar.MONTH, 1)
            }
        }

        if(params.data != 'fetchAll') {
            if(params.series_names) {
                filter += " and title.seriesName in (:seriesName) "
                queryParams.seriesName = params.list("series_names")
            }
            if(params.subject_references) {
                filter += " and title.subjectReference in (:subjectReference) "
                queryParams.subjectReference = params.list("subject_references")
            }
            if(params.ddcs && params.list("ddcs").size() > 0) {
                filter += " and exists (select ddc.id from title.ddcs ddc where ddc.ddc.id in (:ddcs)) "
                queryParams.ddcs = []
                params.list("ddcs").each { String ddc ->
                    queryParams.ddcs << Long.parseLong(ddc)
                }
            }
            if(params.languages && params.list("languages").size() > 0) {
                filter += " and exists (select lang.id from title.languages lang where lang.language.id in (:languages)) "
                queryParams.languages = []
                params.list("languages").each { String lang ->
                    queryParams.languages << Long.parseLong(lang)
                }
            }

            if (params.filter) {
                filter += "and ( ( lower(title.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = title.id and ident.value like :identifier ) ) or ((lower(title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(title.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
                queryParams.title = "%${params.filter.trim().toLowerCase()}%"
                queryParams.identifier = "%${params.filter}%"
                queryParams.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                filter += " and title.pkg.id = :pkgId "
                queryParams.pkgId = Long.parseLong(params.pkgfilter)
            }

            if(params.summaryOfContent) {
                filter += " and lower(title.summaryOfContent) like :summaryOfContent "
                queryParams.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            }

            if(params.ebookFirstAutorOrFirstEditor) {
                filter += " and (lower(title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(title.firstEditor) like :ebookFirstAutorOrFirstEditor) "
                queryParams.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            }

            if(params.yearsFirstOnline) {
                filter += " and (Year(title.dateFirstOnline) in (:yearsFirstOnline)) "
                queryParams.yearsFirstOnline = params.list('yearsFirstOnline').collect { Integer.parseInt(it) }
            }

            if (params.identifier) {
                filter += "and ( exists ( from Identifier ident where ident.tipp.id = title.id and ident.value like :identifier ) ) "
                queryParams.identifier = "${params.identifier}"
            }

            if (params.publishers) {
                filter += "and lower(title.publisherName) in (:publishers) "
                queryParams.publishers = params.list('publishers').collect { it.toLowerCase() }
            }


            if (params.title_types && params.title_types != "" && params.list('title_types')) {
                filter += " and lower(title.titleType) in (:title_types)"
                queryParams.title_types = params.list('title_types').collect { ""+it.toLowerCase()+"" }
            }

        }
        [filter, dateRange, queryParams, monthsInRing, sqlDateRange]
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
            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            cal.setTimeInMillis(System.currentTimeMillis())
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            result.defaultStartYear = sdf.format(cal.getTime())
            cal.set(Calendar.MONTH, Calendar.DECEMBER)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            result.defaultEndYear = sdf.format(cal.getTime())
            if(accessService.checkPerm("ORG_CONSORTIUM")) {
                params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
                Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
                result.members = Org.executeQuery(fsq.query, fsq.queryParams, params)
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
            case RDStore.SUBSCRIPTION_TYPE_CONSORTIAL:
            case RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE:
                orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA
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

            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
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
        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('init')
        if(!result)
            [result:null,status:STATUS_ERROR]
        pu.setBenchmark('before org props')
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        //result.validSubChilds = Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parent:result.subscription,subscriberRoleTypes:subscriberRoleTypes])
        pu.setBenchmark('getting filtered subscribers')
        result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
        pu.setBenchmark('after sub schildren')
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
                    org.shortname = subscr.shortname
                    org.globalUID = subChild.globalUID
                    org.libraryType = subscr.libraryType
                    org.libraryNetwork = subscr.libraryNetwork
                    org.funderType = subscr.funderType
                    org.region = subscr.region
                    org.country = subscr.country
                    org.startDate = subChild.startDate ? subChild.startDate.format(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale())) : ''
                    org.endDate = subChild.endDate ? subChild.endDate.format(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale())) : ''
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
        List bm = pu.stopBenchmark()
        result.benchMark = bm
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
            Locale locale = LocaleContextHolder.getLocale()
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
            //the following two are arguments for a g.message-call on the view which expects an Object[]
            result.superOrgType = [messageSource.getMessage('consortium.superOrgType',null,locale)]
            result.memberType = [messageSource.getMessage('consortium.subscriber',null,locale)]
            result.propList= PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
            Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
            result.members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            result.members_disabled = Subscription.executeQuery("select oo.org.id from OrgRole oo join oo.sub s where s.instanceOf = :io",[io: result.subscription])
            result.validPackages = result.subscription.packages?.sort { it.pkg.name }
            result.memberLicenses = License.executeQuery("select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)",[subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE])

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Takes the submitted parameters and creates member subscription instances based on the given input
     * @param controller unused
     * @param params the input parameter map
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> processAddMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                RefdataValue subStatus = RefdataValue.get(params.subStatus) ?: RDStore.SUBSCRIPTION_CURRENT

                if (result.editable) {
                    List<Org> members = []
                    License licenseCopy
                    params.list('selectedOrgs').each { it ->
                        members << Org.findById(Long.valueOf(it))
                    }
                    List<Subscription> synShareTargetList = []
                    List<License> licensesToProcess = []
                    Set<Package> packagesToProcess = []
                    //copy package data
                    if(params.linkAllPackages) {
                        result.subscription.packages.each { SubscriptionPackage sp ->
                            packagesToProcess << sp.pkg
                        }
                    }
                    else if(params.packageSelection) {
                        List packageIds = params.list("packageSelection")
                        packageIds.each { spId ->
                            packagesToProcess << SubscriptionPackage.get(spId).pkg
                        }
                    }
                    if(params.generateSlavedLics == "all") {
                        String query = "select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)"
                        licensesToProcess.addAll(License.executeQuery(query, [subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE]))
                    }
                    else if(params.generateSlavedLics == "partial") {
                        List<String> licenseKeys = params.list("generateSlavedLicsReference")
                        licenseKeys.each { String licenseKey ->
                            licensesToProcess << genericOIDService.resolveOID(licenseKey)
                        }
                    }
                    List<String> excludes = PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key }
                    excludes << 'freezeHolding'
                    excludes.addAll(PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key+PendingChangeConfiguration.NOTIFICATION_SUFFIX})
                    Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name,result.subscription.id, excludes)
                    List<Subscription> memberSubs = []
                    members.each { Org cm ->
                        log.debug("Generating separate slaved instances for members")
                        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                        Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                        Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
                        Subscription memberSub = new Subscription(
                                type: result.subscription.type ?: null,
                                kind: result.subscription.kind ?: null,
                                status: subStatus,
                                name: result.subscription.name,
                                //name: result.subscription.name + " (" + (cm.get(0).shortname ?: cm.get(0).name) + ")",
                                startDate: startDate,
                                endDate: endDate,
                                administrative: result.subscription._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                                manualRenewalDate: result.subscription.manualRenewalDate,
                                /* manualCancellationDate: result.subscription.manualCancellationDate, */
                                identifier: UUID.randomUUID().toString(),
                                instanceOf: result.subscription,
                                isSlaved: true,
                                resource: result.subscription.resource ?: null,
                                form: result.subscription.form ?: null,
                                isMultiYear: params.checkSubRunTimeMultiYear ?: false
                        )
                        inheritedAttributes.each { attr ->
                            memberSub[attr.referenceField] = result.subscription[attr.referenceField]
                        }
                        if (!memberSub.save()) {
                            memberSub.errors.each { e ->
                                log.debug("Problem creating new sub: ${e}")
                            }
                            result.error = memberSub.errors
                        }
                        if (memberSub) {
                            if(result.subscription._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE) {
                                new OrgRole(org: cm, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS_HIDDEN).save()
                            }
                            else {
                                new OrgRole(org: cm, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS).save()
                            }
                            new OrgRole(org: result.institution, sub: memberSub, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIA).save()
                            synShareTargetList.add(memberSub)
                            SubscriptionProperty.findAllByOwner(result.subscription).each { SubscriptionProperty sp ->
                                AuditConfig ac = AuditConfig.getConfig(sp)
                                if (ac) {
                                    // multi occurrence props; add one additional with backref
                                    if (sp.type.multipleOccurrence) {
                                        SubscriptionProperty additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                        additionalProp = sp.copyInto(additionalProp)
                                        additionalProp.instanceOf = sp
                                        additionalProp.save()
                                    }
                                    else {
                                        // no match found, creating new prop with backref
                                        SubscriptionProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                        newProp = sp.copyInto(newProp)
                                        newProp.instanceOf = sp
                                        newProp.save()
                                    }
                                }
                            }
                            Identifier.findAllBySub(result.subscription).each { Identifier id ->
                                AuditConfig ac = AuditConfig.getConfig(id)
                                if(ac) {
                                    Identifier.constructWithFactoryResult([value: id.value, parent: id, reference: memberSub, namespace: id.ns])
                                }
                            }

                            memberSub.refresh()

                            licensesToProcess.each { License lic ->
                                subscriptionService.setOrgLicRole(memberSub,lic,false)
                            }
                            memberSubs << memberSub
                        }
                                //}
                    }

                    packagesToProcess.each { Package pkg ->
                        subscriptionService.addToMemberSubscription(result.subscription, memberSubs, pkg, params.linkWithEntitlements == 'on')
                        /*
                        if()
                            subscriptionService.addToSubscriptionCurrentStock(memberSub, result.subscription, pkg)
                        else
                            subscriptionService.addToSubscription(memberSub, pkg, false)
                        */
                    }

                    result.subscription.syncAllShares(synShareTargetList)
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
            Subscription memberSub = Subscription.get(Long.parseLong(params.memberSubID))
            Org org = Org.get(Long.parseLong(params.memberOrg))
            Subscription prevMemberSub = (result.navPrevSubscription.size() > 0) ? result.navPrevSubscription[0].getDerivedSubscriptionBySubscribers(org) : null
            Subscription nextMemberSub = (result.navNextSubscription.size() > 0) ? result.navNextSubscription[0].getDerivedSubscriptionBySubscribers(org) : null
            try {
                Links link
                if(params.prev && prevMemberSub) {
                    link = Links.construct([source: memberSub, destination: prevMemberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: result.contextOrg])
                }
                if(params.next && nextMemberSub) {
                    link = Links.construct([source: nextMemberSub, destination: memberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: result.contextOrg])
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
                     org: result.subscription.getSubscriber(),
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
    Map<String,Object> renewEntitlementsWithSurvey(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)

            Subscription newSub = result.subscription
            result.institution = result.contextOrg

            params.tab = params.tab ?: 'allIEs'

            result.preselectValues = params.preselectValues == 'on'

            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.surveyInfo = result.surveyConfig.surveyInfo

            Subscription previousSubscription = newSub._getCalculatedPreviousForSurvey()
            Subscription baseSub = result.surveyConfig.subscription ?: newSub.instanceOf

            result.subscriber = newSub.getSubscriber()

            result.subscriberSubs = []

            Set<Subscription> subscriptions = []
            if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                subscriptions = linksGenerationService.getSuccessionChain(newSub, 'sourceSubscription')
                subscriptions << newSub
                if (subscriptions) {
                    result.subscriberSubs = subscriptions.toList()
                }
            }

            if (params.hasPerpetualAccess) {
                    params.hasPerpetualAccessBySubs = subscriptions
            }

            List<Long> sourceIEs = []
            if(params.tab == 'allIEs') {
                Map query = filterService.getIssueEntitlementQuery(params, baseSub)
                sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            }
            if(params.tab == 'selectedIEs') {
                Map query = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusNotFixed: true], newSub)
                sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            }
            if(params.tab == 'currentIEs') {
                GrailsParameterMap parameterMap = params.clone()
                Map query = [:]
                if(subscriptions) {
                    query = filterService.getIssueEntitlementQuery(parameterMap + [ieAcceptStatusFixed: true], subscriptions)
                    List<Long> previousIes = previousSubscription ? IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams) : []
                    sourceIEs = sourceIEs + previousIes
                }

                query = filterService.getIssueEntitlementQuery(parameterMap+[ieAcceptStatusFixed: true], newSub)
                List<Long> currentIes = newSub ? IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams) : []
                sourceIEs = sourceIEs + currentIes

            }

            if(params.tab == 'toBeSelectedIEs') {
                Map allQuery = filterService.getIssueEntitlementQuery(params, baseSub)
                List<Long> allTippIDs = IssueEntitlement.executeQuery("select ie.tipp.id " + allQuery.query, allQuery.queryParams)
                Map query = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusNotFixed: true], newSub)
                List<Long> selectedTippIDs = IssueEntitlement.executeQuery("select ie.tipp.id " + query.query, query.queryParams)
                List<Long> toBeSelectedTippIDs = allTippIDs - selectedTippIDs
                allQuery.query = allQuery.query.replace("where", "where ie.tipp.id in (:tippIds) and ")
                allQuery.queryParams.tippIds = toBeSelectedTippIDs
                sourceIEs = IssueEntitlement.executeQuery("select ie.id " + allQuery.query, allQuery.queryParams)

            }

            if (result.hasPerpetualAccess) {
                params.hasPerpetualAccess = result.hasPerpetualAccess
            }

            result.countSelectedIEs = subscriptionService.countIssueEntitlementsNotFixed(newSub)
            if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
            } else {
                result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countIssueEntitlementsFixed(result.previousSubscription) : 0) + subscriptionService.countIssueEntitlementsFixed(result.subscription)
            }
            result.countAllIEs = subscriptionService.countIssueEntitlementsFixed(baseSub)
            result.toBeSelectedIEs = result.countAllIEs - result.countSelectedIEs

            result.num_ies_rows = sourceIEs.size()

            if(params.tab in ['allIEsStats', 'holdingIEsStats']) {
                //result = surveyService.getStatsForParticipant(result, params, newSub, result.subscriber, subscriptionService.getTippIDsFixed(baseSub))

                if(!params.tabStat)
                    params.tabStat = 'total'

                String oldTab = params.tab
                params.loadFor = oldTab
                params.tab = params.tabStat

                params.sort= params.sort ?: 'r.reportCount'
                params.order= params.order ?: 'desc'

                Map<String,Object> statsResult

                statsResult = stats(params)

                if (statsResult.status == SubscriptionControllerService.STATUS_ERROR) {
                    if (!statsResult.result) {
                        [result: null, status: STATUS_ERROR]
                        return
                    }
                } else {
                    result = result + statsResult.result
                }

                params.tab = oldTab

            }else {

                params.sort = params.sort ?: 'sortname'
                params.order = params.order ?: 'asc'
                result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs, [sort: params.sort, order: params.order, offset: result.offset, max: result.max]) : []

                /*Map query = filterService.getIssueEntitlementQuery(params, newSub)
                List<IssueEntitlement> targetIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
                result.targetIEs = []
                targetIEs.collate(32767).each {
                    result.targetIEs.addAll(IssueEntitlement.findAllByIdInList(targetIEs.take(32768)))
                }*/
            }

            result.newSub = newSub
            result.subscription = baseSub
            result.previousSubscription = previousSubscription


            if(result.surveyInfo.owner.id ==  result.contextOrg.id) {
                result.participant = result.subscriber
            }

            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)
            result.showStatisticByParticipant = surveyService.showStatisticByParticipant(result.surveyConfig.subscription, result.subscriber)


            if (result.editable) {
                SessionCacheWrapper sessionCache = contextService.getSessionCache()
                Map<String, Object> checkedCache = sessionCache.get("/subscription/renewEntitlementsWithSurvey/${newSub.id}?${params.tab}")

                if (!checkedCache) {
                    sessionCache.put("/subscription/renewEntitlementsWithSurvey/${newSub.id}?${params.tab}", ["checked": [:]])
                    checkedCache = sessionCache.get("/subscription/renewEntitlementsWithSurvey/${newSub.id}?${params.tab}")
                }

                if (params.kbartPreselect) {
                    //checkedCache.put('checked', [:])

                    MultipartFile kbartFile = params.kbartPreselect
                    InputStream stream = kbartFile.getInputStream()
                    result.selectProcess = subscriptionService.issueEntitlementSelect(stream, result.subscription)

                        if (result.selectProcess.selectedIEs) {
                            checkedCache.put('checked', result.selectProcess.selectedIEs)
                        }

                    params.remove("kbartPreselect")
                }

                result.checkedCache = checkedCache.get('checked')
                result.checkedCount = result.checkedCache.findAll { it.value == 'checked' }.size()


                result.allChecked = ""
                if (params.tab == 'allIEs' && result.countAllIEs > 0 && result.countAllIEs == result.checkedCount) {
                    result.allChecked = "checked"
                }
                else if (params.tab == 'selectedIEs' && result.countSelectedIEs > 0 && result.countSelectedIEs == result.checkedCount) {
                    result.allChecked = "checked"
                }
                else if (params.tab == 'currentIEs' && result.countCurrentIEs > 0 && result.countCurrentIEs == result.checkedCount) {
                    result.allChecked = "checked"
                }
                else if (params.tab == 'allIEsStats' && result.countAllIEs > 0 && result.countAllIEs == result.checkedCount) {
                    result.allChecked = "checked"
                }
            }

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
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each { Thread thread ->
                if (thread.name == 'PackageTransfer_'+result.subscription.id && !SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,Package.findByGokbId(params.addUUID))) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null,LocaleContextHolder.getLocale())
                    result.bulkProcessRunning = true
                }
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
            /*result.max = params.max ? params.int('max') : result.user.getDefaultPageSizeAsInteger()
            List gokbRecords = []
            ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true).each { ApiSource api ->
                gokbRecords << gokbService.getPackagesMap(api, params.q, false).records
            }
            result.sort = params.sort ?: 'name'
            result.order = params.order ?: 'asc'
            result.records = null
            if(gokbRecords) {
                Map filteredMap = [:]
                gokbRecords.each { apiRes ->
                    apiRes.each { rec ->
                        filteredMap[rec.uuid] = rec
                    }
                }
                result.records = filteredMap.values().toList().flatten()
            }
            if(result.records) {
                result.records.sort { x, y ->
                    if (result.order == 'desc') {
                        y."${result.sort}".toString().compareToIgnoreCase x."${result.sort}".toString()
                    } else {
                        x."${result.sort}".toString().compareToIgnoreCase y."${result.sort}".toString()
                    }
                }
                result.resultsTotal = result.records.size()
                Integer start = params.offset ? params.int('offset') : 0
                Integer end = params.offset ? result.max + params.int('offset') : result.max
                end = (end > result.records.size()) ? result.records.size() : end
                result.hits = result.records.subList(start, end)
            }*/

            ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

            SwissKnife.setPaginationParams(result, params, result.user)

            result.editUrl = apiSource.baseUrl
            String esQuery = "?componentType=Package"
            if (params.q) {
                result.filterSet = true
                //workaround for or-connection; find api supports only and-connection
                esQuery += "&name=${params.q}"
                esQuery += "&ids=Anbieter_Produkt_ID,*${params.q}*"
                esQuery += "&ids=isil,*${params.q}*"
            }

            if(params.provider) {
                result.filterSet = true
                esQuery += "&provider=${params.provider.replaceAll('&','ampersand')}"
            }

            if(params.curatoryGroup) {
                result.filterSet = true
                esQuery += "&curatoryGroupExact=${params.curatoryGroup.replaceAll('&','ampersand')}"
            }

            if(params.resourceTyp) {
                result.filterSet = true
                esQuery += "&contentType=${params.resourceTyp}"
            }

            if (params.ddc) {
                result.filterSet = true
                params.list("ddc").each { String key ->
                    esQuery += "&ddc=${RefdataValue.get(key).value}"
                }
            }

            //you rarely encounter it; ^ is the XOR operator in Java - if both options are set, we mean all curatory group types
            if (params.containsKey('curatoryGroupProvider') ^ params.containsKey('curatoryGroupOther')) {
                result.filterSet = true
                if(params.curatoryGroupProvider)
                    esQuery += "&curatoryGroupType=provider"
                else if(params.curatoryGroupOther)
                    esQuery += "&curatoryGroupType=other" //setting to this includes also missing ones, this is already implemented in we:kb
            }

            String sort = params.sort ? "&sort="+params.sort: "&sort=sortname"
            String order = params.order ? "&order="+params.order: "&order=asc"
            String max = params.max ? "&max=${params.max}": "&max=${result.max}"
            String offset = params.offset ? "&offset=${params.offset}": "&offset=${result.offset}"

            Map queryCuratoryGroups = gokbService.queryElasticsearch(apiSource.baseUrl+apiSource.fixToken+'/groups')
            if(queryCuratoryGroups.error && queryCuratoryGroups.error == 404) {
                result.error = messageSource.getMessage('wekb.error.404', null, LocaleContextHolder.getLocale())
                [result:result, status: STATUS_ERROR]
            }
            else {
                if(queryCuratoryGroups.warning) {
                    List recordsCuratoryGroups = queryCuratoryGroups.warning.result
                    result.curatoryGroups = recordsCuratoryGroups?.findAll {it.status == "Current"}
                }
                result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)

                Set records = []
                Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + '/find' + esQuery + sort + order + max + offset)
                if (queryResult.warning) {
                    records.addAll(queryResult.warning.records)
                    result.recordsCount = queryResult.warning.count
                    result.records = records
                }
                [result:result,status:STATUS_OK]
            }

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
                String pkgUUID = params.addUUID
                String addType = params.addType
                String addTypeChildren = params.addTypeChildren
                ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                result.source = apiSource.baseUrl
                GlobalRecordSource source = GlobalRecordSource.findByUriLikeAndRectype(result.source+'%', GlobalSourceSyncService.RECTYPE_TIPP)
                log.debug("linkPackage. Global Record Source URL: " +source.uri)
                globalSourceSyncService.source = source
                globalSourceSyncService.defineMapFields()
                //to be deployed in parallel thread
                executorService.execute({
                    Thread.currentThread().setName("PackageTransfer_"+result.subscription.id)
                    if(!Package.findByGokbId(pkgUUID)) {
                        try {
                            Map<String,Object> queryResult = globalSourceSyncService.fetchRecordJSON(false,[componentType:'TitleInstancePackagePlatform',pkg:pkgUUID,max:5000])
                            if(queryResult.error && queryResult.error == 404) {
                                log.error("we:kb server currently unavailable")
                            }
                            else {
                                Package.withNewTransaction {
                                    if(queryResult.records && queryResult.count > 0) {
                                        if(queryResult.count > 5000)
                                            globalSourceSyncService.processScrollPage(queryResult, 'TitleInstancePackagePlatform', null, pkgUUID)
                                        else
                                            globalSourceSyncService.updateRecords(queryResult.records, 0)
                                    }
                                    else {
                                        globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                                    }
                                }
                                Package pkgToLink = Package.findByGokbId(pkgUUID)
                                result.packageName = pkgToLink.name
                                log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                                subscriptionService.addToSubscription(result.subscription, pkgToLink, addType == 'With')
                                if(addTypeChildren) {
                                    subscriptionService.addToMemberSubscription(result.subscription, Subscription.findAllByInstanceOf(result.subscription), pkgToLink, addTypeChildren == 'WithForChildren')
                                }
                                subscriptionService.addPendingChangeConfiguration(result.subscription, pkgToLink, params.clone())
                            }
                        }
                        catch (Exception e) {
                            log.error("sync job has failed, please consult stacktrace as follows: ")
                            e.printStackTrace()
                        }
                    }
                    else {
                        Package pkgToLink = globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                        log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                        subscriptionService.addToSubscription(result.subscription, pkgToLink, addType == 'With')
                        if(addTypeChildren) {
                            subscriptionService.addToMemberSubscription(result.subscription, Subscription.findAllByInstanceOf(result.subscription), pkgToLink, addTypeChildren == 'WithForChildren')
                        }
                        subscriptionService.addPendingChangeConfiguration(result.subscription, pkgToLink, params.clone())
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
        Locale locale = LocaleContextHolder.getLocale()
        if(params.confirmed) {
            Set<Subscription> childSubs = Subscription.findAllByInstanceOf(result.subscription)
            boolean unlinkErrorChild = false
            childSubs.each { Subscription child ->
                if(!packageService.unlinkFromSubscription(result.package, child, result.institution, true)) {
                    unlinkErrorChild = true
                    return
                }
            }
            if(!unlinkErrorChild && packageService.unlinkFromSubscription(result.package, result.subscription, result.institution, true)){
                result.message = messageSource.getMessage('subscription.details.unlink.successfully',null,locale)
                [result:result,status:STATUS_OK]
            }else {
                result.error = messageSource.getMessage('subscription.details.unlink.notSuccessfully',null,locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            String query = "select ie.id from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
            Map<String,Object> queryParams = [sub: result.subscription, pkg_id: result.package.id]
            int numOfPCs = packageService.removePackagePendingChanges(result.package, [result.subscription.id], false)
            int numOfIEs = IssueEntitlement.executeQuery(query, queryParams).size()
            int numOfCIs = CostItem.findAllBySubPkg(SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,result.package)).size()
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
                    int numOfCIsChildSubs = childSubsPackages ? CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.subPkg in (:childSubsPackages) and ci.owner != :ctx and ci.costItemStatus != :deleted', [childSubsPackages: childSubsPackages, deleted: RDStore.COST_ITEM_DELETED, ctx: result.institution])[0] : 0
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
            Locale locale = LocaleContextHolder.getLocale()
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'PackageTransfer_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null,locale)
                }
                else if (it.name == 'EntitlementEnrichment_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.thread.running', null, locale)
                }
            }
            result.issueEntitlementEnrichment = params.issueEntitlementEnrichment
            SwissKnife.setPaginationParams(result, params, (User) result.user)
           /* List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where subscription = :sub and ( pc.status is null or pc.status = :status ) order by ts desc",
                    [sub: result.subscription, status: RDStore.PENDING_CHANGE_PENDING])
            result.pendingChanges = pendingChanges*/

            params.ieAcceptStatusFixed = true
            def query = filterService.getIssueEntitlementQuery(params, result.subscription)
            result.filterSet = query.filterSet
            Set<Long> entitlements = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            result.entitlementIDs = entitlements
            if(params.kbartPreselect) {
                MultipartFile kbartFile = params.kbartPreselect
                InputStream stream = kbartFile.getInputStream()
                result.enrichmentProcess = subscriptionService.issueEntitlementEnrichment(stream, entitlements, result.subscription, (params.uploadCoverageDates == 'on'), (params.uploadPriceInfo == 'on'))
                params.remove("kbartPreselect")
                params.remove("uploadCoverageDates")
                params.remove("uploadPriceInfo")
            }

            if(result.subscription.ieGroups.size() > 0) {
                Map configMap = params.clone()
                configMap.forCount = true
                def query2 = filterService.getIssueEntitlementQuery(configMap, result.subscription)
                result.num_ies = IssueEntitlement.executeQuery("select count(ie.id) " + query2.query, query2.queryParams)[0]
            }
            result.num_ies_rows = entitlements.size()
            if(entitlements) {
                String orderClause = 'order by ie.sortname, tipp.sortname'
                if(params.sort){
                    if(params.sort == 'startDate')
                        orderClause = "order by ic.startDate ${params.order}, lower(ie.sortname) asc "
                    else if(params.sort == 'endDate')
                        orderClause = "order by ic.endDate ${params.order}, lower(ie.sortname) asc "
                    else
                        orderClause = "order by ${params.sort} ${params.order} "
                }
                result.entitlements = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp left join ie.coverages ic where ie.id in (:entIDs) '+orderClause,[entIDs:entitlements.drop(result.offset).take(result.max)])
                result.journalsOnly = result.entitlements.find { IssueEntitlement ie -> ie.tipp.titleType != RDStore.TITLE_TYPE_JOURNAL.value } == null
            }
            else result.entitlements = []
            Set<SubscriptionPackage> deletedSPs = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.pkg.packageStatus == RDStore.PACKAGE_STATUS_DELETED }
            if(deletedSPs) {
                result.deletedSPs = []
                ApiSource source = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                deletedSPs.each { sp ->
                    result.deletedSPs << [name:sp.pkg.name,link:"${source.editUrl}/public/packageContent/?id=${sp.pkg.gokbId}"]
                }
            }
            Date now = new Date()
            if (now > result.subscription.endDate) {
                result.frozenHoldings = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.freezeHolding }.pkg
            }
            if (executorWrapperService.hasRunningProcess(result.subscription)) {
                result.processingpc = true
            }
            result.considerInBatch = ["sort", "order", "offset", "max", "status", "pkgfilter", "asAt", "series_name", "subject_reference", "ddc", "language", "yearsFirstOnline", "identifier", "title_types", "publishers", "coverageDepth", "inTitleGroups"]

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
            Map<SubscriptionPackage, Set<String>> pkgSettingMap = [:]
            Set<String> pendingOrWithNotification = []
            Set subscriptionHistory = []
            Set<PendingChange> changesOfPage = []
            Set<String> excludes = PendingChangeConfiguration.GENERIC_EXCLUDES
            result.subscription.packages.each { SubscriptionPackage sp ->
                Set<String> keysWithPendingOrNotification = sp.pendingChangeConfig.findAll { PendingChangeConfiguration pcc -> !(pcc.settingKey in excludes) && (pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT || pcc.withNotification) }.collect{ PendingChangeConfiguration pcc -> pcc.settingKey }
                keysWithPendingOrNotification << PendingChangeConfiguration.TITLE_REMOVED
                if(keysWithPendingOrNotification) {
                    pkgSettingMap.put(sp, keysWithPendingOrNotification)
                    pendingOrWithNotification.addAll(keysWithPendingOrNotification)
                }
            }

            /*OLD
            if(pkgList && pendingOrWithNotification) {
                String query1 = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg in (:packages) and pc.oid = null and pc.status = :history and pc.msgToken in (:pendingOrWithNotification)',
                       query2 = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg in (:packages) and pc.oid = null and pc.status = :history and pc.msgToken in (:pendingOrWithNotification)',
                       query3 = 'select pc.id from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg in (:packages) and pc.oid = null and pc.status = :history and pc.msgToken in (:pendingOrWithNotification)',
                       query1a = 'select pc.id,pc.tipp from PendingChange pc join pc.tipp.pkg pkg where pkg in (:packages) and pc.oid = (:subOid) and pc.status = :accepted',
                       query2a = 'select pc.id,pc.tippCoverage from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg in (:packages) and pc.oid = (:subOid) and pc.status = :accepted',
                       query3a = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg in (:packages) and pc.oid = (:subOid) and pc.status = :accepted'
                packageHistory.addAll(PendingChange.executeQuery(query1,[packages:pkgList,history:RDStore.PENDING_CHANGE_HISTORY,pendingOrWithNotification:pendingOrWithNotification]))
                packageHistory.addAll(PendingChange.executeQuery(query2,[packages:pkgList,history:RDStore.PENDING_CHANGE_HISTORY,pendingOrWithNotification:pendingOrWithNotification]))
                packageHistory.addAll(PendingChange.executeQuery(query3,[packages:pkgList,history:RDStore.PENDING_CHANGE_HISTORY,pendingOrWithNotification:pendingOrWithNotification]))
                subscriptionHistory.addAll(PendingChange.executeQuery(query1a,[packages:pkgList,accepted:RDStore.PENDING_CHANGE_ACCEPTED,subOid:genericOIDService.getOID(result.subscription)]))
                subscriptionHistory.addAll(PendingChange.executeQuery(query2a,[packages:pkgList,accepted:RDStore.PENDING_CHANGE_ACCEPTED,subOid:genericOIDService.getOID(result.subscription)]))
                subscriptionHistory.addAll(PendingChange.executeQuery(query3a,[packages:pkgList,accepted:RDStore.PENDING_CHANGE_ACCEPTED,subOid:genericOIDService.getOID(result.subscription)]))
                changesOfPage.addAll(PendingChange.findAllByIdInList(packageHistory,[sort:'ts',order:'asc']))
                subscriptionHistory.each { row ->
                    accepted << changesOfPage.find { PendingChange pc -> row[1] in [pc.tipp,pc.tippCoverage,pc.priceItem] }?.id
                }
            }*/

            params.sort = params.sort ?: 'ts'
            params.order = params.order ?: 'desc'
            params.eventType = params.eventType ?: PendingChangeConfiguration.TITLE_UPDATED
            String order = " order by pc.${params.sort} ${params.order}"
            if(pkgSettingMap && pendingOrWithNotification) {
                pkgSettingMap.each { SubscriptionPackage sp, Set<String> settings ->
                    Package pkg = sp.pkg
                    Date entryDate = sp.dateCreated
                    String query1a = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.oid = :subOid and pc.status in (:pendingStatus)',
                           query2a = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.oid = :subOid and pc.status in (:pendingStatus)'
                    //query3a = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status in (:pendingStatus)',
                    String query1b, query1c
                    if(params.eventType in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_DELETED] && params.eventType in settings)
                        query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and pc.status = :packageHistory'
                    else if(params.eventType == PendingChangeConfiguration.TITLE_UPDATED && params.eventType in settings)
                        query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.newValue = pc.newValue and pca.status in (:pendingStatus)) and exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tipp and ie.status != :removed and ie.subscription = :subscription) and pc.status = :packageHistory'
                    else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED && params.eventType in settings)
                        query1c = 'select pc.id from PendingChange pc join pc.tipp tipp join tipp.pkg pkg where pkg = :package and pc.msgToken = :eventType and exists(select ie from IssueEntitlement ie where ie.tipp = tipp and ie.subscription = :subscription and ie.status != :removed)'
                    String query2b
                    if(params.eventType in [PendingChangeConfiguration.NEW_COVERAGE, PendingChangeConfiguration.COVERAGE_DELETED] && params.eventType in settings)
                        query2b = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tippCoverage tcA where tcA = pc.tippCoverage and pca.oid = :subOid and pc.status in (:pendingStatus)) and exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tippCoverage.tipp and ie.status != :removed and ie.subscription = :subscription) and pc.status = :packageHistory'
                    else if(params.eventType == PendingChangeConfiguration.COVERAGE_UPDATED && params.eventType in settings)
                        query2b = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tippCoverage tcA where tcA = pc.tippCoverage and pca.oid = :subOid and pca.newValue = pc.newValue and pca.status in (:pendingStatus)) and exists (select ie.id from IssueEntitlement ie where ie.tipp = pc.tippCoverage.tipp and ie.status != :removed and ie.subscription = :subscription) and pc.status = :packageHistory'
                    //query3b = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status not in (:pendingStatus)',
                    String query1d = 'select pc.id from PendingChange pc where pc.subscription = :subscription and pc.msgToken = :eventType and pc.status not in (:pendingStatus)'
                    if(params.eventType in [PendingChangeConfiguration.NEW_TITLE, PendingChangeConfiguration.TITLE_UPDATED, PendingChangeConfiguration.TITLE_DELETED] && params.eventType in settings)
                        subscriptionHistory.addAll(PendingChange.executeQuery(query1a+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    else if(params.eventType in [PendingChangeConfiguration.NEW_COVERAGE, PendingChangeConfiguration.COVERAGE_UPDATED, PendingChangeConfiguration.COVERAGE_DELETED] && params.eventType in settings)
                        subscriptionHistory.addAll(PendingChange.executeQuery(query2a+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    //subscriptionHistory.addAll(PendingChange.executeQuery(query3a,[package: pkg, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    if(params.eventType == PendingChangeConfiguration.NEW_TITLE && params.eventType in settings)
                        changesOfPage.addAll(PendingChange.executeQuery(query1b+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
                    else if(params.eventType in [PendingChangeConfiguration.TITLE_UPDATED, PendingChangeConfiguration.TITLE_DELETED] && params.eventType in settings)
                        changesOfPage.addAll(PendingChange.executeQuery(query1b+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
                    else if(params.eventType in [PendingChangeConfiguration.NEW_COVERAGE, PendingChangeConfiguration.COVERAGE_UPDATED, PendingChangeConfiguration.COVERAGE_DELETED] && params.eventType in settings)
                        changesOfPage.addAll(PendingChange.executeQuery(query2b+order,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
                    else if(params.eventType == PendingChangeConfiguration.TITLE_REMOVED && params.eventType in settings)
                        changesOfPage.addAll(PendingChange.executeQuery(query1c+order,[package: pkg, eventType: params.eventType, subscription: sp.subscription, removed: RDStore.TIPP_STATUS_REMOVED]))
                    //changesOfPage.addAll(PendingChange.executeQuery(query3b,[packages: pkgList, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    changesOfPage.addAll(PendingChange.executeQuery(query1d+order,[pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], eventType: params.eventType, subscription: result.subscription]))
                }
            }

            params.tab = params.tab ?: 'changes'
            params.max = result.max
            params.offset = result.offset

            result.putAll(pendingChangeService.getCountsForPackages(pkgSettingMap))

            if(params.tab == 'changes') {
                result.changes = changesOfPage ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:changesOfPage) order by '+params.sort+' '+params.order, [changesOfPage: changesOfPage.drop(result.offset).take(result.max)]) : []
                result.num_change_rows = changesOfPage.size()
            }

            if(params.tab == 'acceptedChanges') {
                result.changes = subscriptionHistory ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:subscriptionHistory) order by '+params.sort+' '+params.order, [subscriptionHistory: subscriptionHistory.drop(result.offset).take(result.max)]) : []
                result.num_change_rows = subscriptionHistory.size()
            }

            result.apisources = ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

            result.packages = result.subscription.packages

            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Lists the titles in the package which have not yet been added to the subscription. If the call has been
     * called with an upload, the data provided with the given KBART file will be processed, so that
     * <ul>
     *     <li>either titles will be preselected based on the given identifiers</li>
     *     <li>or local price statements will be added to the title candidates</li>
     *     <li>or local coverage statements will be added to the title candidates</li>
     * </ul>
     * The data submitted will be stored in cache and may be corrected before actually add them to the issue
     * entitlements which in turn will be persisted and added to the subscription
     * @param controller unused
     * @param params the request parameter map
     * @return the title list; with or without the enriched information from a KBART upload
     */
    Map<String,Object> addEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            result.preselectValues = params.preselectValues == 'on'
            result.preselectCoverageDates = params.preselectCoverageDates == 'on'
            result.uploadPriceInfo = params.uploadPriceInfo == 'on'
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
            RefdataValue tipp_current = RDStore.TIPP_STATUS_CURRENT
            RefdataValue ie_current = RDStore.TIPP_STATUS_CURRENT
            RefdataValue ie_removed = RDStore.TIPP_STATUS_REMOVED
            List<Long> tippIDs = []
            List<TitleInstancePackagePlatform> tipps = []
            List errorList = []
            boolean filterSet = false
            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map checkedCache = sessionCache.get("/subscription/addEntitlements/${params.id}")

            if (!checkedCache) {
                sessionCache.put("/subscription/addEntitlements/${params.id}", [:])
                checkedCache = sessionCache.get("/subscription/addEntitlements/${params.id}")
            }
            Set<String> addedTipps = IssueEntitlement.executeQuery('select tipp.gokbId from IssueEntitlement ie join ie.tipp tipp where ie.status != :status and ie.subscription = :sub',[status:ie_removed,sub:result.subscription])
            /*result.subscription.issueEntitlements.each { ie ->
                if(ie instanceof IssueEntitlement && ie.status != ie_deleted)
                    addedTipps[ie.tipp] = ie.tipp.gokbId
            }*/
            // We need all issue entitlements from the parent subscription where no row exists in the current subscription for that item.
            String basequery
            Map<String,Object> qry_params = [subscription:result.subscription,tippStatus:tipp_current,issueEntitlementStatus:ie_current]

            params.subscription = result.subscription
            params.issueEntitlementStatus = ie_current
            params.addEntitlements = true
            List packages = []
            if(params.pkgfilter)
                packages << Package.get(params.pkgfilter)
            else packages = result.subscription.packages?.pkg
            Map<String, Object> query = filterService.getTippQuery(params, packages)
            result.filterSet = query.filterSet

            if(result.subscription.packages?.pkg) {
                Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
                if(tippIds)
                    tipps.addAll(TitleInstancePackagePlatform.findAllByIdInList(tippIds.drop(result.offset).take(result.max),[sort:'sortname']))
                //now, assemble the identifiers available to highlight
                Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                               eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name), isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                               issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name), pisbn: IdentifierNamespace.findByNsAndNsType('pisbn', TitleInstancePackagePlatform.class.name)]
                result.num_tipp_rows = tippIds.size()
                result.tipps = tipps
                result.tippIDs = tippIds
                Map<String, Object> identifiers = [zdbIds: [], onlineIds: [], printIds: [], unidentified: []]
                Map<String, Map> issueEntitlementOverwrite = [:]
                result.issueEntitlementOverwrite = [:]
                if(!params.pagination) {
                    checkedCache.put('checked', [:])
                }
                if (params.kbartPreselect && !params.pagination) {
                    MultipartFile kbartFile = params.kbartPreselect
                    identifiers.filename = kbartFile.originalFilename
                    InputStream stream = kbartFile.getInputStream()
                    ArrayList<String> rows = stream.text.split('\n')
                    Map<String, Integer> colMap = [publicationTitleCol: -1, zdbCol: -1, mediumCol: -1, onlineIdentifierCol: -1, printIdentifierCol: -1, dateFirstInPrintCol: -1, dateFirstOnlineCol: -1,
                                                   startDateCol       : -1, startVolumeCol: -1, startIssueCol: -1,
                                                   endDateCol         : -1, endVolumeCol: -1, endIssueCol: -1,
                                                   accessStartDateCol : -1, accessEndDateCol: -1, coverageDepthCol: -1, coverageNotesCol: -1, embargoCol: -1,
                                                   listPriceCol       : -1, listCurrencyCol: -1, listPriceEurCol: -1, listPriceUsdCol: -1, listPriceGbpCol: -1, localPriceCol: -1, localCurrencyCol: -1, priceDateCol: -1]
                    boolean isUniqueListpriceColumn = false
                    //read off first line of KBART file
                    rows[0].split('\t').eachWithIndex { String headerCol, int c ->
                        switch (headerCol.toLowerCase().trim()) {
                            case "zdb_id": colMap.zdbCol = c
                                break
                            case "medium": colMap.mediumCol = c
                                break
                            case "print_identifier": colMap.printIdentifierCol = c
                                break
                            case "online_identifier": colMap.onlineIdentifierCol = c
                                break
                            case "publication_title": colMap.publicationTitleCol = c
                                break
                            case "date_monograph_published_print": colMap.dateFirstInPrintCol = c
                                break
                            case "date_monograph_published_online": colMap.dateFirstOnlineCol = c
                                break
                            case "date_first_issue_online": colMap.startDateCol = c
                                break
                            case "num_first_vol_online": colMap.startVolumeCol = c
                                break
                            case "num_first_issue_online": colMap.startIssueCol = c
                                break
                            case "date_last_issue_online": colMap.endDateCol = c
                                break
                            case "num_last_vol_online": colMap.endVolumeCol = c
                                break
                            case "num_last_issue_online": colMap.endIssueCol = c
                                break
                            case "access_start_date": colMap.accessStartDateCol = c
                                break
                            case "access_end_date": colMap.accessEndDateCol = c
                                break
                            case "embargo_info": colMap.embargoCol = c
                                break
                            case "coverage_depth": colMap.coverageDepthCol = c
                                break
                            case "notes": colMap.coverageNotesCol = c
                                break
                            case "listprice_value": colMap.listPriceCol = c
                                break
                            case "listprice_currency": colMap.listCurrencyCol = c
                                break
                            case "listprice_eur": colMap.listPriceEurCol = c
                                break
                            case "listprice_usd": colMap.listPriceUsdCol = c
                                break
                            case "listprice_gbp": colMap.listPriceGbpCol = c
                                break
                            case "localprice_value": colMap.localPriceCol = c
                                break
                            case "localprice_currency": colMap.localCurrencyCol = c
                                break
                            case "price_date": colMap.priceDateCol = c
                                break
                        }
                    }
                    if(result.uploadPriceInfo) {
                        if ((colMap.listPriceCol > -1 && colMap.listCurrencyCol > -1) && (colMap.listPriceEurCol > -1 || colMap.listPriceGbpCol > -1 || colMap.listPriceUsdCol > -1)) {
                            errorList.add(messageSource.getMessage('subscription.details.addEntitlements.duplicatePriceColumn', null, locale))
                        } else if ((colMap.listPriceEurCol > -1 && colMap.listPriceUsdCol > -1) && (colMap.listPriceEurCol > -1 && colMap.listPriceGbpCol > -1) && (colMap.listPriceUsdCol > -1 && colMap.listPriceGbpCol > -1)) {
                            errorList.add(messageSource.getMessage('subscription.details.addEntitlements.duplicatePriceColumn', null, locale))
                        } else isUniqueListpriceColumn = true
                    }
                    Set<String> selectedTippIds = []
                    //after having read off the header row, pop the first row
                    rows.remove(0)
                    rows.eachWithIndex { row, int i ->
                        Map<String, Object> ieCandidate = [:]
                        ArrayList<String> cols = row.split('\t')
                        Map<String, Object> idCandidate = [:]
                        String ieCandIdentifier
                        if (colMap.zdbCol >= 0 && !cols[colMap.zdbCol]?.trim()?.isEmpty()) {
                            identifiers.zdbIds.add(cols[colMap.zdbCol])
                            idCandidate = [namespaces: namespaces.zdb, value: cols[colMap.zdbCol]]
                            if (issueEntitlementOverwrite[cols[colMap.zdbCol]])
                                ieCandidate = issueEntitlementOverwrite[cols[colMap.zdbCol]]
                            else ieCandIdentifier = cols[colMap.zdbCol]
                        }
                        if (colMap.onlineIdentifierCol >= 0 && !cols[colMap.onlineIdentifierCol]?.trim()?.isEmpty()) {
                            identifiers.onlineIds.add(cols[colMap.onlineIdentifierCol])
                            idCandidate = [namespaces: [], value: cols[colMap.onlineIdentifierCol]]
                            idCandidate.namespaces.add(namespaces.eissn)
                            idCandidate.namespaces.add(namespaces.isbn)
                            if (ieCandIdentifier == null && !issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                                ieCandIdentifier = cols[colMap.onlineIdentifierCol]
                            else if (issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                                ieCandidate = issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]]
                        }
                        if (colMap.printIdentifierCol >= 0 && !cols[colMap.printIdentifierCol]?.trim()?.isEmpty()) {
                            identifiers.printIds.add(cols[colMap.printIdentifierCol])
                            idCandidate = [namespaces: [], value: cols[colMap.printIdentifierCol]]
                            idCandidate.namespaces.add(namespaces.issn)
                            idCandidate.namespaces.add(namespaces.pisbn)
                            if (ieCandIdentifier == null && !issueEntitlementOverwrite[cols[colMap.printIdentifierCol]])
                                ieCandIdentifier = cols[colMap.printIdentifierCol]
                            else if (issueEntitlementOverwrite[cols[colMap.printIdentifierCol]])
                                ieCandidate = issueEntitlementOverwrite[cols[colMap.printIdentifierCol]]
                        }
                        if (((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                                ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                                ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
                            identifiers.unidentified.add('"' + cols[0] + '"')
                        } else {
                            //make checks ...
                            //is title in LAS:eR?
                            List<TitleInstancePackagePlatform> matchingTipps = TitleInstancePackagePlatform.executeQuery("select tipp from Identifier id join id.tipp tipp where id.value = :value and id.ns in (:ns) and tipp.pkg in (:subPkgs)",[value: idCandidate.value, ns: idCandidate.namespaces, subPkgs: result.subscription.packages.collect { SubscriptionPackage sp -> sp.pkg }]) //it is *always* possible to have multiple packages linked to a subscription!
                            if (matchingTipps) {
                                TitleInstancePackagePlatform tipp = matchingTipps.find { TitleInstancePackagePlatform matchingTipp -> matchingTipp.pkg in result.subscription.packages.pkg } as TitleInstancePackagePlatform
                                //is title already added?
                                if (addedTipps.contains(tipp.gokbId)) {
                                    errorList.add("${cols[colMap.publicationTitleCol]}&#9;${cols[colMap.zdbCol] && colMap.zdbCol ? cols[colMap.zdbCol] : " "}&#9;${cols[colMap.onlineIdentifierCol] && colMap.onlineIndentifierCol > -1 ? cols[colMap.onlineIdentifierCol] : " "}&#9;${cols[colMap.printIdentifierCol] && colMap.printIdentifierCol > -1 ? cols[colMap.printIdentifierCol] : " "}&#9;${messageSource.getMessage('subscription.details.addEntitlements.titleAlreadyAdded', null, locale)}")
                                }
                            }
                            else {
                                if(matchingTipps)
                                    errorList.add("${cols[colMap.publicationTitleCol]}&#9;${cols[colMap.zdbCol] && colMap.zdbCol > -1 ? cols[colMap.zdbCol] : " "}&#9;${cols[colMap.onlineIdentifierCol] && colMap.onlineIndentifierCol > -1 ? cols[colMap.onlineIdentifierCol] : " "}&#9;${cols[colMap.printIdentifierCol] && colMap.printIdentifierCol > -1 ? cols[colMap.printIdentifierCol] : " "}&#9;${messageSource.getMessage('subscription.details.addEntitlements.titleNotInPackage', null, locale)}")
                                else
                                    errorList.add("${cols[colMap.publicationTitleCol]}&#9;${cols[colMap.zdbCol] && colMap.zdbCol > -1 ? cols[colMap.zdbCol] : " "}&#9;${cols[colMap.onlineIdentifierCol] && colMap.onlineIndentifierCol > -1 ? cols[colMap.onlineIdentifierCol] : " "}&#9;${cols[colMap.printIdentifierCol] && colMap.printIdentifierCol > -1 ? cols[colMap.printIdentifierCol] : " "}&#9;${messageSource.getMessage('subscription.details.addEntitlements.titleNotInERMS', null, locale)}")
                            }
                        }
                        List<Map> ieCoverages
                        if (ieCandidate.coverages)
                            ieCoverages = ieCandidate.coverages
                        else ieCoverages = []
                        Map covStmt = [:]
                        colMap.each { String colName, int colNo ->
                            if (colNo > -1 && cols[colNo]) {
                                String cellEntry = cols[colNo].trim()
                                if (result.preselectCoverageDates) {
                                    switch (colName) {
                                        case "dateFirstInPrintCol": ieCandidate.dateFirstInPrint = cellEntry
                                            break
                                        case "dateFirstOnlineCol": ieCandidate.dateFirstOnline = cellEntry
                                            break
                                        case "startDateCol": covStmt.startDate = cellEntry
                                            break
                                        case "startVolumeCol": covStmt.startVolume = cellEntry
                                            break
                                        case "startIssueCol": covStmt.startIssue = cellEntry
                                            break
                                        case "endDateCol": covStmt.endDate = cellEntry
                                            break
                                        case "endVolumeCol": covStmt.endVolume = cellEntry
                                            break
                                        case "endIssueCol": covStmt.endIssue = cellEntry
                                            break
                                        case "accessStartDateCol": ieCandidate.accessStartDate = cellEntry
                                            break
                                        case "accessEndDateCol": ieCandidate.accessEndDate = cellEntry
                                            break
                                        case "embargoCol": covStmt.embargo = cellEntry
                                            break
                                        case "coverageDepthCol": covStmt.coverageDepth = cellEntry
                                            break
                                        case "coverageNotesCol": covStmt.coverageNote = cellEntry
                                            break
                                    }
                                }
                                if (result.uploadPriceInfo && isUniqueListpriceColumn) {
                                    try {
                                        switch (colName) {
                                            case "listPriceCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                                break
                                            case "listCurrencyCol": ieCandidate.listCurrency = RefdataValue.getByValueAndCategory(cellEntry, RDConstants.CURRENCY)?.value
                                                break
                                            case "listPriceEurCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                                ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("EUR", RDConstants.CURRENCY).value
                                                break
                                            case "listPriceUsdCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                                ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("USD", RDConstants.CURRENCY).value
                                                break
                                            case "listPriceGbpCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                                ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("GBP", RDConstants.CURRENCY).value
                                                break
                                            case "localPriceCol": ieCandidate.localPrice = escapeService.parseFinancialValue(cellEntry)
                                                break
                                            case "localCurrencyCol": ieCandidate.localCurrency = RefdataValue.getByValueAndCategory(cellEntry, RDConstants.CURRENCY)?.value
                                                break
                                            case "priceDateCol": ieCandidate.priceDate = cellEntry
                                                break
                                        }
                                    }
                                    catch (NumberFormatException e) {
                                        log.error("Unparseable number ${cellEntry}")
                                    }
                                }
                            }
                        }
                        if (ieCandIdentifier) {
                            Map<String, Object> unfilteredParams = [pkgs:result.subscription.packages.pkg, deleted:RDStore.TIPP_STATUS_REMOVED, value: ieCandIdentifier]
                            //check where indices are needed!
                            List<String> matches = TitleInstancePackagePlatform.executeQuery('select tipp.gokbId from TitleInstancePackagePlatform tipp join tipp.ids id where tipp.pkg in (:pkgs) and tipp.status != :deleted and id.value = :value',unfilteredParams)
                            ieCoverages.add(covStmt)
                            ieCandidate.coverages = ieCoverages
                            matches.each { String match ->
                                issueEntitlementOverwrite[match] = ieCandidate
                                selectedTippIds << match
                            }
                        }
                    }
                    result.checked = [:]
                    result.issueEntitlementOverwrite = issueEntitlementOverwrite
                    selectedTippIds.removeAll(addedTipps)
                    selectedTippIds.each { String wekbId ->
                        //println("located tipp: ${wekbId}")
                        result.checked[wekbId] = "checked"
                    }
                    result.identifiers = identifiers
                    if (result.identifiers && result.identifiers.unidentified.size() > 0) {
                        String unidentifiedTitles = result.identifiers.unidentified.join(", ")
                        String escapedFileName
                        try {
                            escapedFileName = StringEscapeCategory.encodeAsHtml(result.identifiers.filename)
                        }
                        catch (Exception | Error e) {
                            log.error(e.printStackTrace())
                            escapedFileName = result.identifiers.filename
                        }
                        Object[] args = [escapedFileName, unidentifiedTitles]
                        errorList.add(messageSource.getMessage('subscription.details.addEntitlements.unidentified', args, locale))
                    }
                    checkedCache.put('checked', result.checked)
                    checkedCache.put('issueEntitlementCandidates', result.issueEntitlementOverwrite)
                    params.remove("kbartPreselect")
                }
                /*
                if (!params.pagination) {
                List<Map> allTippsFiltered = TitleInstancePackagePlatform.executeQuery("select new map(tipp.titleType as titleType, tipp.gokbId as gokbId) from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status != :removed",unfilteredParams)
                    List<Map> identifierRows = Identifier.executeQuery("select new map(tipp.gokbId as gokbId, id.value as value, ns.ns as namespace) from Identifier id join id.ns ns join id.tipp tipp where tipp.pkg = :pkg and tipp.status != :removed",unfilteredParams)
                    Map<String, Set> allIdentifiers = [:]
                    identifierRows.each { Map row ->
                        Set<Map> ids = allIdentifiers.get(row.gokbId)
                        if(!ids)
                            ids = []
                        ids << [value: row.value, namespace: row.namespace]
                        allIdentifiers.put(row.gokbId, ids)
                    }
                    allTippsFiltered.eachWithIndex { tipp, int i ->
                        log.debug("now processing tipp ${i}")
                        String serial
                        String electronicSerial
                        String checked = ""
                        if (tipp.titleType == 'Book') {
                            serial = allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.pisbn.ns}?.value
                            electronicSerial = allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.isbn.ns }?.value
                        } else if (tipp.titleType == "Journal") {
                            serial = allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.issn.ns }?.value
                            electronicSerial = allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.eissn.ns }?.value
                        }
                        if (result.identifiers?.zdbIds?.find { allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.zdb.ns } }) {
                            checked = "checked"
                            result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[allIdentifiers.get(tipp.gokbId).find { Map data -> data.namespace == namespaces.zdb.ns }.value]
                        } else if (result.identifiers?.onlineIds?.find { String onlineId -> onlineId == electronicSerial }) {
                            checked = "checked"
                            result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[electronicSerial]
                        } else if (result.identifiers?.printIds?.find{ String printId -> printId == serial }) {
                            checked = "checked"
                            result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[serial]
                        }
                        if(checked)
                            result.checked[tipp.gokbId] = checked
                    }
                    }
                        */
                if(params.pagination) {
                    result.checked = checkedCache.get('checked')
                    result.issueEntitlementOverwrite = checkedCache.get('issueEntitlementCandidates')
                }
                if (errorList)
                    result.error = "<pre style='font-family:Lato,Arial,Helvetica,sans-serif;'>" + errorList.join("\n") + "</pre>"
            }
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
    Map<String,Object> processAddEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map cache = sessionCache.get("/subscription/addEntitlements/${result.subscription.id}")
            Map issueEntitlementCandidates = cache && cache.containsKey('issueEntitlementCandidates') ? cache.get('issueEntitlementCandidates') : [:]
            if(!params.singleTitle) {
                Map checked = cache.get('checked')
                if(checked) {
                    //executorService.execute({
                        //Thread.currentThread().setName("EntitlementEnrichment_${result.subscription.id}")
                        subscriptionService.bulkAddEntitlements(result.subscription, issueEntitlementCandidates, checked, Boolean.valueOf(params.uploadPriceInfo), RDStore.IE_ACCEPT_STATUS_FIXED.id, false)
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
                        //}
                    //})
                }
                else {
                    log.error('cache error or no titles selected')
                }
            }
            else if(params.singleTitle) {
                try {
                    Object[] args = [TitleInstancePackagePlatform.findByGokbId(params.singleTitle)?.name]
                    if(issueEntitlementCandidates?.get(params.singleTitle) || Boolean.valueOf(params.uploadPriceInfo))  {
                        if(subscriptionService.addEntitlement(result.subscription, params.singleTitle, issueEntitlementCandidates?.get(params.singleTitle), Boolean.valueOf(params.uploadPriceInfo), RDStore.IE_ACCEPT_STATUS_FIXED))
                            log.debug("Added tipp ${params.singleTitle} to sub ${result.subscription.id} with issue entitlement overwrites")
                        result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                    }
                    else if(subscriptionService.addEntitlement(result.subscription, params.singleTitle, null, true, RDStore.IE_ACCEPT_STATUS_FIXED))
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
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each { Thread thread ->
                if (thread.name == 'PackageTransfer_' + result.subscription.id) {
                    result.errMess = 'subscription.packages.resetToSubEnd.threadRunning'
                    [result: result, status: STATUS_ERROR]
                }
            }
            executorService.execute({
                Thread.currentThread().setName("PackageTransfer_${result.subscription.id}")
                SubscriptionPackage sp = SubscriptionPackage.get(params.subPkg)
                //need to use native sql because of performance and queries not doable in hql
                def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
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
                        println "now processing record ${i} of ${acceptedTitleUpdates.size()} entries"
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
                        println "now processing change ${idx} out of ${acceptedCoverageUpdates.size()} records"
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
                            println "now reverting change ${i} out of ${revertingChanges.size()} records at subscriptions ${j} out of ${revertingMap.subscriptions.size()} subscriptions"
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
                                println "now restoring deleted coverage ${i}"
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
                sp.freezeHolding = true
                sp.save()
                PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
                    AuditConfig.removeConfig(sp.subscription, settingKey)
                    AuditConfig.removeConfig(sp.subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    if(!AuditConfig.getConfig(sp.subscription, SubscriptionPackage.FREEZE_HOLDING))
                        AuditConfig.addConfig(sp.subscription, SubscriptionPackage.FREEZE_HOLDING)
                }
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
            SimpleDateFormat formatter = DateUtils.getSDF_NoTime()
            boolean error = false
            if(params.chkall == 'on') {
                params.ieAcceptStatusFixed = true
                Map<String, Object> query = filterService.getIssueEntitlementQuery(params, result.subscription)
                if(params.bulkOperation == "edit") {
                    if (params.bulk_access_start_date && (params.bulk_access_start_date.trim().length() > 0)) {
                        Date accessStartDate = formatter.parse(params.bulk_access_start_date)
                        String updateQuery = "update IssueEntitlement e set e.accessStartDate = :accessStartDate where e.id in (select ie.id ${query.query})"
                        IssueEntitlement.executeUpdate(updateQuery, query.queryParams+[accessStartDate: accessStartDate])
                    }
                    if (params.bulk_access_end_date && (params.bulk_access_end_date.trim().length() > 0)) {
                        Date accessEndDate = formatter.parse(params.bulk_access_end_date)
                        String updateQuery = "update IssueEntitlement e set e.accessEndDate = :accessEndDate where e.id in (select ie.id ${query.query})"
                        IssueEntitlement.executeUpdate(updateQuery, query.queryParams+[accessEndDate: accessEndDate])
                    }
                    if (params.titleGroupInsert && (params.titleGroupInsert.trim().length() > 0)) {
                        Sql sql = GlobalService.obtainSqlConnection()
                        params.select = 'bulkInsertTitleGroup'
                        if(!params.pkgIds && !params.pkgfilter)
                            params.pkgIds = result.subscription.packages.pkg.id
                        Map<String, Object> sqlQuery = filterService.prepareTitleSQLQuery(params, IssueEntitlement.class.name, sql)
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
                    }
                }
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
            }
            else {
                params.each { Map.Entry<Object,Object> p ->
                    if (p.key.startsWith('_bulkflag.') && (p.value == 'on')) {
                        String ie_to_edit = p.key.substring(10)
                        IssueEntitlement ie = IssueEntitlement.get(ie_to_edit)
                        if (params.bulkOperation == "edit") {
                            if (params.bulk_access_start_date && (params.bulk_access_start_date.trim().length() > 0)) {
                                ie.accessStartDate = formatter.parse(params.bulk_access_start_date)
                            }
                            if (params.bulk_access_end_date && (params.bulk_access_end_date.trim().length() > 0)) {
                                ie.accessEndDate = formatter.parse(params.bulk_access_end_date)
                            }
                            /* legacy???
                            if (params.bulk_medium.trim().length() > 0) {
                                RefdataValue selected_refdata = (RefdataValue) genericOIDService.resolveOID(params.bulk_medium.trim())
                                log.debug("Selected medium is ${selected_refdata}");
                                ie.medium = selected_refdata
                            }*/
                            if (params.titleGroupInsert && (params.titleGroupInsert.trim().length() > 0)) {
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
                            if (!ie.save()) {
                                log.error("Problem saving ${ie.errors.getAllErrors().toListString()}")
                                error = true
                            }
                        }
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
        Locale locale = LocaleContextHolder.getLocale()
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
     * Removes the given price item from the issue entitlement
     * @param params the request parameter map
     * @return OK if the removal was successful, ERROR otherwise
     */
    Map<String,Object> removePriceItem(GrailsParameterMap params) {
        PriceItem priceItem = PriceItem.get(params.priceItem)
        if(priceItem) {
            priceItem.delete()
            [result:null,status:STATUS_OK]
        }
        else {
            log.error("Issue entitlement priceItem with ID ${params.priceItem} could not be found")
            [result:null,status:STATUS_ERROR]
        }
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
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            log.error("Issue entitlement with ID ${params.issueEntitlement} could not be found")
            [result:null,status:STATUS_ERROR]
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
        Locale locale = LocaleContextHolder.getLocale()
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
     *
     * @param params
     * @return
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

    @Deprecated
    Map<String,Object> processRenewEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            List tippsToAdd = params."tippsToAdd".split(",")
            List tippsToDelete = params."tippsToDelete".split(",")
            tippsToAdd.each { tipp ->
                try {
                    if(subscriptionService.addEntitlement(result.subscription,tipp,null,false, RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION))
                        log.debug("Added tipp ${tipp} to sub ${result.subscription.id}")
                }
                catch (EntitlementCreationException e) {
                    result.error = e.getStackTrace()
                    [result:result,status:STATUS_ERROR]
                }
            }
            tippsToDelete.each { tipp ->
                if(subscriptionService.deleteEntitlement(result.subscription,tipp))
                    log.debug("Deleted tipp ${tipp} from sub ${result.subscription.id}")
            }
            if(params.process == "finalise") {
                SubscriptionPackage sp = SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,Package.get(params.packageId))
                sp.finishDate = new Date()
                if(!sp.save()) {
                    result.error = sp.errors
                    [result:result,status:STATUS_ERROR]
                }
                else {
                    Object[] args = [sp.pkg.name]
                    result.message = messageSource.getMessage('subscription.details.renewEntitlements.submitSuccess',args,LocaleContextHolder.getLocale())
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Takes the submitted input and adds the selected titles to the (preliminary) subscription holding from cache
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the selection (adding and/or removing of issue entitlements) was successful, ERROR otherwise
     */
    Map<String,Object> processRenewEntitlementsWithSurvey(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)
            if(!result.editable) {
                [result:null,status:STATUS_ERROR]
            }
            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map<String,Object> checkedCache = sessionCache.get("/subscription/renewEntitlementsWithSurvey/${params.id}?${params.tab}")

            result.checkedCache = checkedCache.get('checked')
            result.checked = result.checkedCache.findAll {it.value == 'checked'}


            List removeFromCache = []

            if(result.checked.size() < 1){
                result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps',null,locale)
                [result:result,status:STATUS_ERROR]

            }else if(params.process == "preliminary" && result.checked.size() > 0) {
                Integer countIEsToAdd = 0
                result.checked.each {
                    IssueEntitlement ie = IssueEntitlement.findById(it.key)
                    if(ie) {
                        TitleInstancePackagePlatform tipp = ie.tipp

                        boolean tippExistsInParentSub = false

                        if (IssueEntitlement.findByTippAndSubscriptionAndStatus(tipp, result.surveyConfig.subscription, RDStore.TIPP_STATUS_CURRENT)) {
                            tippExistsInParentSub = true
                        } else {
                            List<TitleInstancePackagePlatform> titleInstancePackagePlatformList = TitleInstancePackagePlatform.findAllByHostPlatformURLAndStatus(tipp.hostPlatformURL, RDStore.TIPP_STATUS_CURRENT)
                            titleInstancePackagePlatformList.each { TitleInstancePackagePlatform titleInstancePackagePlatform ->
                                if (IssueEntitlement.findByTippAndSubscriptionAndStatus(titleInstancePackagePlatform, result.surveyConfig.subscription, RDStore.TIPP_STATUS_CURRENT)) {
                                    tippExistsInParentSub = true
                                    tipp = titleInstancePackagePlatform
                                }
                            }
                        }

                        if (tippExistsInParentSub) {
                            try {
                                RefdataValue acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION

                                if(result.contextOrg.id == result.surveyConfig.surveyInfo.owner.id && SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, result.subscription.subscriber).finishDate != null){
                                    acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION
                                }

                                if (subscriptionService.addEntitlement(result.subscription, tipp.gokbId, ie, (ie.priceItems.size() > 0), acceptStatus, result.surveyConfig.pickAndChoosePerpetualAccess)) {
                                    log.debug("Added tipp ${tipp.gokbId} to sub ${result.subscription.id}")
                                    ++countIEsToAdd
                                    removeFromCache << it.key
                                }
                            }
                            catch (EntitlementCreationException e) {
                                log.debug("Error: Adding tipp ${tipp} to sub ${result.subscription.id}: " + e.getMessage())
                                result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps', null, locale)
                                [result: result, status: STATUS_ERROR]
                            }
                        }
                    }
                }
                if(countIEsToAdd > 0){
                    Object[] args = [countIEsToAdd]
                    result.message = messageSource.getMessage('renewEntitlementsWithSurvey.tippsToAdd',args,locale)
                }

                removeFromCache.each {
                    result.checked.remove(it)
                }
                checkedCache.put('checked',result.checked)

                [result:result,status:STATUS_OK]

            } else if(params.process == "remove" && result.checked.size() > 0) {
                Integer countIEsToDelete = 0
                result.checked.each {
                    try {
                        if (subscriptionService.deleteEntitlementbyID(result.subscription, it.key.toString())) {
                            ++countIEsToDelete
                            removeFromCache << it.key
                        }
                    }
                    catch (EntitlementCreationException e) {
                        result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps',null,locale)
                        [result:result,status:STATUS_ERROR]
                    }
                }
                if(countIEsToDelete > 0){
                    Object[] args = [countIEsToDelete]
                    result.message = messageSource.getMessage('renewEntitlementsWithSurvey.tippsToDelete',args,locale)
                }

                removeFromCache.each {
                    result.checked.remove(it)
                }
                checkedCache.put('checked',result.checked)

                [result:result,status:STATUS_OK]
            }
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
            Locale locale = LocaleContextHolder.getLocale()
            List<Links> previousSubscriptions = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_FOLLOWS)
            if (previousSubscriptions.size() > 0) {
                result.error = messageSource.getMessage('subscription.renewSubExist',null,locale)
                [result:result,status:STATUS_ERROR]
            }
            else {
                Date sub_startDate = params.subscription.start_date ? DateUtils.parseDateGeneric(params.subscription.start_date) : null
                Date sub_endDate = params.subscription.end_date ? DateUtils.parseDateGeneric(params.subscription.end_date) : null
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
                    if(accessService.checkPerm("ORG_CONSORTIUM")) {
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
                        if ((or.org.id == result.contextOrg.id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS,  RDStore.OR_SUBSCRIBER_CONS_HIDDEN])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = newSub
                            newOrgRole.save()
                        }
                    }
                    //link to previous subscription
                    Links prevLink = Links.construct([source: newSub, destination: result.subscription, linkType: RDStore.LINKTYPE_FOLLOWS, owner: result.contextOrg])
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
            Locale locale = LocaleContextHolder.getLocale()
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
                        if ((or.org.id == result.contextOrg.id) || (or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id])) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = result.targetObject
                            newOrgRole.save()
                        }
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

    //--------------------------------------------- admin section -------------------------------------------------

    @Deprecated
    Map<String,Object> pendingChanges(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Set<Subscription> validSubChilds = Subscription.executeQuery("select oo.sub from OrgRole oo join oo.sub sub join oo.org org where sub.instanceOf = :contextSub order by org.sortname asc, org.name asc",[contextSub:result.subscription])
            result.pendingChanges = [:]
            validSubChilds.each { Subscription member ->
                if (executorWrapperService.hasRunningProcess(member)) {
                    log.debug("PendingChange processing in progress")
                    result.processingpc = true
                } else {
                    List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where subscription.id = :subId and ( pc.status is null or pc.status = :status ) order by pc.ts desc",
                            [subId: member.id, status: RDStore.PENDING_CHANGE_PENDING]
                    )
                    result.pendingChanges << [("${member.id}".toString()): pendingChanges]
                }
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
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        params.remove("max")
        params.remove("offset")
        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
        if (params.filterPropDef) {
            fsq = propertyService.evalFilterQuery(params, fsq.query, 'o', fsq.queryParams)
        }
        fsq.query = fsq.query.replaceFirst("select o from ", "select o.id from ")
        List<Long> filteredOrgIds = Org.executeQuery(fsq.query, fsq.queryParams, params+[id:parentSub.id])
        Set rows = Subscription.executeQuery("select sub,org from OrgRole oo join oo.sub sub join oo.org org where sub.instanceOf = :parent order by org.sortname asc, org.name asc",[parent:parentSub])
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
        result.contextOrg = contextService.getOrg()
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
        result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(result.contextOrg, result.sourceObject)
        result.consortialView = result.showConsortiaFunctions
        result.editable = result.sourceObject?.isEditableBy(result.user)
        if (!result.editable) {
            //the explicit comparison against bool(true) should ensure that not only the existence of the parameter is checked but also its proper value
            if(params.copyMyElements == true) {
                if(accessService.checkPermAffiliation('ORG_INST','INST_EDITOR'))
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
        result.contextOrg = contextService.getOrg()
        result.contextCustomerType = result.contextOrg.getCustomerType()
        result.institution = result.subscription ? result.subscription?.subscriber : result.contextOrg //TODO temp, remove the duplicate

        if (result.subscription) {
            result.subscriptionConsortia = result.subscription.getConsortia()
            result.licenses = Links.findAllByDestinationSubscriptionAndLinkType(result.subscription, RDStore.LINKTYPE_LICENSE).collect { Links li -> li.sourceLicense }
            LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(result.subscription)
            result.hasNext = links.nextLink.size() > 0
            result.navPrevSubscription = links.prevLink
            result.navNextSubscription = links.nextLink
            if(result.subscription.instanceOf)
                result.auditConfigs = auditService.getAllAuditConfigs(result.subscription.instanceOf)
            else result.auditConfigs = auditService.getAllAuditConfigs(result.subscription)

            result.currentTitlesCounts = IssueEntitlement.executeQuery("select count(ie.id) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus ", [sub: result.subscription, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED])[0]

            if(result.contextCustomerType == "ORG_CONSORTIUM") {
                if(result.subscription.instanceOf){
                    List subscrCostCounts = CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED])
                    result.currentCostItemCounts = subscrCostCounts ? subscrCostCounts[0] : 0
                    result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                            [sub: result.subscription.instanceOf,
                             org: result.subscription.getSubscriber(),
                             invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                }else{
                    result.currentSurveysCounts = SurveyConfig.findAllBySubscription(result.subscription).size()
                    List subscrCostCounts = CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub.instanceOf = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED]),
                    ownCostCounts = CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED])
                    int subscrCount = subscrCostCounts ? subscrCostCounts[0] : 0
                    int ownCount = ownCostCounts ? ownCostCounts[0] : 0
                    result.currentCostItemCounts = "${ownCount}/${subscrCount}"
                }
                result.currentMembersCounts =  Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',[parent: result.subscription, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])[0]
            }else{
                result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                        [sub: result.subscription.instanceOf,
                         org: result.subscription.getSubscriber(),
                         invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                List subscrCostCounts = CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub = :sub and ci.isVisibleForSubscriber = true and ci.costItemStatus != :deleted', [sub: result.subscription, deleted: RDStore.COST_ITEM_DELETED])
                int subscrCount = subscrCostCounts ? subscrCostCounts[0] : 0
                if(result.contextCustomerType == "ORG_INST") {
                    List ownCostCounts = CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub = :sub and ci.owner = :ctx and ci.costItemStatus != :deleted', [sub: result.subscription, ctx: result.institution, deleted: RDStore.COST_ITEM_DELETED])
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
            result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(result.contextOrg, result.subscription)


            result.workflowCount = WfWorkflow.executeQuery(
                    'select count(wf) from WfWorkflow wf where wf.subscription = :sub and wf.owner = :ctxOrg',
                    [sub: result.subscription, ctxOrg: result.contextOrg]
            )[0]

            if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.subscription.isVisibleBy(result.user)) {
                    log.debug("--- NOT VISIBLE ---")
                    return null
                }
            }
            result.editable = result.subscription.isEditableBy(result.user)

            if (params.orgBasicMemberView){
                result.editable = false
            }

            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                if (!result.editable) {
                    log.debug("--- NOT EDITABLE ---")
                    return null
                }
            }
        }
        else {
            if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
                result.editable = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")
            }
        }
        result.consortialView = result.showConsortiaFunctions ?: result.contextOrg.getCustomerType() == "ORG_CONSORTIUM"

        Map args = [:]
        if (result.consortialView) {
            Locale locale = LocaleContextHolder.getLocale()

            args.superOrgType       = [messageSource.getMessage('consortium.superOrgType', null, locale)]
            args.memberTypeSingle   = [messageSource.getMessage('consortium.subscriber', null, locale)]
            args.memberType         = [messageSource.getMessage('consortium.subscriber', null, locale)]
            args.memberTypeGenitive = [messageSource.getMessage('consortium.subscriber', null, locale)]
        }
        result.args = args

        result
    }

    /**
     * Initialises the reporting index for the given subscription
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> reporting(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription sub = Subscription.get(params.id)

        result.token         = params.token ?: RandomStringUtils.randomAlphanumeric(16) // -> static token
        result.cfgQueryList    = SubscriptionReport.getCurrentQueryConfig( sub )
        result.cfgTimelineList = SubscriptionReport.getCurrentTimelineConfig( sub )

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

    /**
     * Gets the workflows linked to the given subscription
     * @param params the request parameter map
     * @return OK if the retrieval was successful, ERROR otherwise
     */
    Map<String,Object> workflows(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        if (params.cmd) {
            String[] cmd = params.cmd.split(':')
            if (cmd[0] in ['edit']) {
                result.putAll( workflowService.cmd(params) ) // @ workflows
            }
            else {
                result.putAll( workflowService.usage(params) ) // @ workflows
            }
        }
        if (params.info) {
            result.info = params.info // @ currentWorkflows @ dashboard
        }

        result.workflows = WfWorkflow.executeQuery(
                'select wf from WfWorkflow wf where wf.subscription = :sub and wf.owner = :ctxOrg order by wf.id desc',
                [sub: result.subscription, ctxOrg: result.contextOrg]
        )
        result.workflowCount = result.workflows.size()

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }
}
