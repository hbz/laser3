package de.laser.ctrl

import com.k_int.kbplus.*
import de.laser.*
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractReport
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
import de.laser.reporting.local.SubscriptionReporting
import grails.doc.internal.StringEscapeCategory
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.transaction.TransactionStatus
import org.springframework.web.multipart.MultipartFile

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService

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

    //-------------------------------------- general or ungroupable section -------------------------------------------

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
            //params.forExport = true
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

    Map<String, Object> stats(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        SwissKnife.setPaginationParams(result, params, result.user)
        if(!result)
            [result: null, status: STATUS_ERROR]
        else {
            Set<Platform> subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: result.subscription])
            Set<Counter4Report> c4usages = []
            Set<Counter5Report> c5usages = []
            List count4check = [], c4sums = [], count5check = [], c5sums = [], monthsInRing = []
            if(subscribedPlatforms) {
                String sort, dateRange
                Map<String, Object> queryParams = [customer: result.subscription.getSubscriber(), platforms: subscribedPlatforms]
                if(params.sort) {
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
                    sort = "title.name asc, r.reportType asc, r.reportFrom desc"
                }
                Calendar startTime = GregorianCalendar.getInstance(), endTime = GregorianCalendar.getInstance(), now = GregorianCalendar.getInstance()
                if(result.subscription.startDate && result.subscription.endDate) {
                    dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                    if(params.reportMonth) {
                        Calendar filterTime = GregorianCalendar.getInstance()
                        Date filterDate = DateUtils.parseDateGeneric(params.reportMonth)
                        filterTime.setTime(filterDate)
                        queryParams.startDate = filterDate
                        filterTime.set(Calendar.DATE,filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
                        queryParams.endDate = filterTime.getTime()
                    }
                    else {
                        queryParams.startDate = result.subscription.startDate
                        queryParams.endDate = result.subscription.endDate
                    }
                    startTime.setTime(result.subscription.startDate)
                    if(result.subscription.endDate < new Date())
                        endTime.setTime(result.subscription.endDate)
                }
                else if(result.subscription.startDate) {
                    dateRange = " and r.reportFrom >= :startDate "
                    if(params.reportMonth) {
                        dateRange += "and r.reportTo <= :endDate "
                        Calendar filterTime = GregorianCalendar.getInstance()
                        Date filterDate = DateUtils.parseDateGeneric(params.reportMonth)
                        filterTime.setTime(filterDate)
                        queryParams.startDate = filterDate
                        filterTime.set(Calendar.MONTH,filterTime.getActualMaximum(Calendar.MONTH))
                        queryParams.endDate = filterTime.getTime()
                    }
                    else
                        queryParams.startDate = result.subscription.startDate
                    startTime.setTime(result.subscription.startDate)
                }
                else {
                    if(params.reportMonth) {
                        dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
                        Calendar filterTime = GregorianCalendar.getInstance()
                        Date filterDate = DateUtils.parseDateGeneric(params.reportMonth)
                        filterTime.setTime(filterDate)
                        queryParams.startDate = filterDate
                        filterTime.set(Calendar.MONTH,filterTime.getActualMaximum(Calendar.MONTH))
                        queryParams.endDate = filterTime.getTime()
                    }
                    else
                        dateRange = ''
                    startTime.set(2018, 1, 1)
                }
                while(startTime.before(endTime)) {
                    monthsInRing << startTime.getTime()
                    startTime.add(Calendar.MONTH, 1)
                }
                String filter = ""
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
                c4usages.addAll(Counter4Report.executeQuery('select r from Counter4Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                c4sums.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter4Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange+' group by r.reportFrom, r.reportType, r.metricType order by r.reportFrom asc, r.reportType asc', queryParams))
                count4check.addAll(Counter4Report.executeQuery('select count(r.id) from Counter4Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange, queryParams))
                c5sums.addAll(Counter5Report.executeQuery('select new map(r.reportType as reportType, r.reportFrom as reportMonth, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter4Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange+' group by r.reportFrom, r.reportType, r.metricType order by r.reportFrom asc, r.reportType asc', queryParams))
                c5usages.addAll(Counter5Report.executeQuery('select r from Counter5Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange+' order by '+sort, queryParams, [max: result.max, offset: result.offset]))
                count5check.addAll(Counter5Report.executeQuery('select count(r.id) from Counter5Report r join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)'+filter+dateRange, queryParams))
            }
            result.c4usages = c4usages
            result.c4total = count4check.size() > 0 ? count4check[0] as int : 0
            result.c5usages = c5usages
            result.c5total = count5check.size() > 0 ? count5check[0] as int : 0
            result.monthsInRing = monthsInRing
            if(!params.tab)
                params.tab = 'counter4'
            switch(params.tab) {
                case 'counter4': result.total = result.c4total
                    result.sums = c4sums
                    result.usages = result.c4usages
                    break
                case 'counter5': result.total = result.c5total
                    result.sums = c5sums
                    result.usages = result.c5usages
                    break
            }
            [result: result, status: STATUS_OK]
        }
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

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

    Map<String,Object> notes(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> documents(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            [result:result,status:STATUS_OK]
        }
    }

    //--------------------------------- consortia members section ----------------------------------------------

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

                            packagesToProcess.each { Package pkg ->
                                if(params.linkWithEntitlements)
                                    subscriptionService.addToSubscriptionCurrentStock(memberSub, result.subscription, pkg)
                                else
                                    subscriptionService.addToSubscription(memberSub, pkg, false)
                            }

                            licensesToProcess.each { License lic ->
                                subscriptionService.setOrgLicRole(memberSub,lic,false)
                            }

                        }
                                //}
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
                    result.redirect = prevMemberSub.id
                }
                if(params.next && nextMemberSub) {
                    link = Links.construct([source: nextMemberSub, destination: memberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: result.contextOrg])
                    result.redirect = nextMemberSub.id
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

    Map<String,Object> linkLicenseMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.parentLicenses = Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:result.subscription,linkType:RDStore.LINKTYPE_LICENSE])
            result.validLicenses = []
            if(result.parentLicenses) {
                result.validLicenses.addAll(License.findAllByInstanceOfInList(result.parentLicenses))
            }
            result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> processLinkLicenseMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Locale locale = LocaleContextHolder.getLocale()
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                Set<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.subscription)
                List selectedMembers = params.list("selectedMembers")
                List<GString> changeAccepted = []
                validSubChilds.each { Subscription subChild ->
                    if (selectedMembers.contains(subChild.id.toString())) { //toString needed for type check
                        License newLicense = License.get(params.license_All)
                        if(subscriptionService.setOrgLicRole(subChild,newLicense,params.processOption == 'unlinkLicense'))
                            changeAccepted << "${subChild.name} (${messageSource.getMessage('subscription.linkInstance.label',null,locale)} ${subChild.getSubscriber().sortname})"
                    }
                }
                if (changeAccepted) {
                    result.message = changeAccepted.join(', ')
                }
                [result:result,status:STATUS_OK]
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    Map<String,Object> processUnLinkLicenseMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Locale locale = LocaleContextHolder.getLocale()
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                List selectedMembers = params.list("selectedMembers")
                Set<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.subscription)
                List<GString> removeLic = []
                validSubChilds.each { Subscription subChild ->
                    if(subChild.id in selectedMembers || params.unlinkAll == 'true') {
                        Links.findAllByDestinationSubscriptionAndLinkType(subChild,RDStore.LINKTYPE_LICENSE).each { Links li ->
                            if (subscriptionService.setOrgLicRole(subChild,li.sourceLicense,true)) {
                                removeLic << "${subChild.name} (${messageSource.getMessage('subscription.linkInstance.label',null,locale)} ${subChild.getSubscriber().sortname})"
                            }
                        }
                    }

                }
                if (removeLic) {
                    result.message = removeLic.join(', ')
                }
                [result:result,status:STATUS_OK]
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    Map<String,Object> linkPackagesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'linkPackageMembers_'+result.subscription.id) {
                    result.message = messageSource.getMessage('subscription.details.linkPackagesMembers.thread.running',null,LocaleContextHolder.getLocale())
                    result.isLinkingRunning = true
                }
            }
            result.validPackages = result.subscription.packages
            result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
            result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubChildren) and ci.costItemStatus != :deleted and ci.owner = :context',[context:result.institution,deleted:RDStore.COST_ITEM_DELETED,filteredSubChildren:result.filteredSubChilds.collect { row -> row.sub }])
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> processLinkPackagesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            if(formService.validateToken(params)) {
                List selectedMembers = params.list("selectedMembers")
                if(selectedMembers && params.package_All){
                    Package pkg_to_link = SubscriptionPackage.get(params.package_All).pkg
                    executorService.execute({
                        IssueEntitlement.withNewTransaction { TransactionStatus ts ->
                            Thread.currentThread().setName("linkPackageMembers_${result.subscription.id}")
                            selectedMembers.each { id ->
                                Subscription subChild = Subscription.get(Long.parseLong(id))
                                if (params.processOption == 'linkwithIE' || params.processOption == 'linkwithoutIE') {
                                    if (!(pkg_to_link in subChild.packages.pkg)) {
                                        if (params.processOption == 'linkwithIE') {
                                            subscriptionService.addToSubscriptionCurrentStock(subChild, result.subscription, pkg_to_link)
                                        } else {
                                            subscriptionService.addToSubscription(subChild, pkg_to_link, false)
                                        }
                                    }
                                }
                                if (params.processOption == 'unlinkwithIE' || params.processOption == 'unlinkwithoutIE') {
                                    if (pkg_to_link in subChild.packages.pkg) {
                                        if (params.processOption == 'unlinkwithIE') {
                                            pkg_to_link.unlinkFromSubscription(subChild, result.institution, true)
                                        } else {
                                            pkg_to_link.unlinkFromSubscription(subChild, result.institution, false)
                                        }
                                    }
                                }
                            }
                        }
                    })
                    [result:result,status:STATUS_OK]
                }
                else {
                    if(!selectedMembers) {
                        result.error = messageSource.getMessage('subscription.linkPackagesMembers.noSelectedMember',null,locale)
                    }
                    if(!params.package_All) {
                        result.error = messageSource.getMessage('subscription.linkPackagesMembers.noSelectedPackage',null,locale)
                    }
                    [result:result,status:STATUS_ERROR]
                }
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    Map<String,Object> processUnLinkPackagesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            result.parentPackages = result.subscription.packages
            result.message = []
            result.error = []
            List<SubscriptionPackage> validSubChildPackages = SubscriptionPackage.executeQuery("select sp from SubscriptionPackage sp join sp.subscription sub where sub.instanceOf = :parent",[parent:result.subscription])
            validSubChildPackages.each { SubscriptionPackage sp ->
                if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg = :sp and ci.costItemStatus != :deleted and ci.owner = :context',[sp:sp,deleted:RDStore.COST_ITEM_DELETED,context:result.institution])) {
                    if (Boolean.valueOf(params.withIE)) {
                        if(sp.pkg.unlinkFromSubscription(sp.subscription, result.institution, true)){
                            result.message << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.withIE.successful',null,locale)
                        }
                        else {
                            result.error << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.withIE.fail',null,locale)
                        }
                    }
                    else {
                        if(sp.pkg.unlinkFromSubscription(sp.subscription, result.institution, false)){
                            result.message << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.onlyPackage.successful',null,locale)
                        }
                        else {
                            result.error << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.onlyPackage.fail',null,locale)
                        }
                    }
                }
                else {
                    Object[] args = [sp.pkg.name,sp.subscription.getSubscriber().name]
                    result.error << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.costsExisting',args,locale)
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> propertiesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null
            params.remove('filterPropDef')
            Set<Subscription> validSubChildren = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.sub.instanceOf = :parent and oo.roleType = :roleType order by oo.org.sortname asc",[parent:result.subscription,roleType:RDStore.OR_SUBSCRIBER_CONS])
            if(validSubChildren) {
                result.validSubChilds = validSubChildren
                Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select distinct(sp.type) from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null",[subscriptionSet:validSubChildren,ctx:result.institution])
                propList.addAll(result.subscription.propertySet.type)
                result.propList = propList
                result.filteredSubChilds = validSubChildren
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
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> processPropertiesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                Locale locale = LocaleContextHolder.getLocale()
                PropertyDefinition filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null
                if (filterPropDef && params.filterPropValue) {
                    int newProperties = 0
                    int changeProperties = 0
                    List selectedMembers = params.list("selectedMembers")
                    Object[] args
                    if(selectedMembers) {
                        Set<Subscription> subChildren = Subscription.findAllByIdInList(selectedMembers)
                        subChildren.each { Subscription subChild ->
                            List<SubscriptionProperty> existingProps = []
                            String propDefFlag
                            if (filterPropDef.tenant == result.institution) {
                                //private Property
                                existingProps.addAll(subChild.propertySet.findAll { SubscriptionProperty sp ->
                                    sp.owner.id == subChild.id && sp.type.id == filterPropDef.id
                                })
                                propDefFlag = PropertyDefinition.PRIVATE_PROPERTY
                            }
                            else {
                                //custom Property
                                existingProps.addAll(subChild.propertySet.findAll { SubscriptionProperty sp ->
                                    sp.type.id == filterPropDef.id && sp.owner.id == subChild.id && sp.tenant.id == result.institution.id
                                })
                                propDefFlag = PropertyDefinition.CUSTOM_PROPERTY
                            }
                            if (existingProps.size() == 0 || filterPropDef.multipleOccurrence) {
                                AbstractPropertyWithCalculatedLastUpdated newProp = PropertyDefinition.createGenericProperty(propDefFlag, subChild, filterPropDef, result.institution)
                                if (newProp.hasErrors()) {
                                    log.error(newProp.errors.toString())
                                } else {
                                    log.debug("New property created: " + newProp.type.name)
                                    newProperties++
                                    subscriptionService.updateProperty(controller, newProp, params.filterPropValue)
                                }
                            }
                            if (existingProps.size() == 1){
                                SubscriptionProperty privateProp = SubscriptionProperty.get(existingProps[0].id)
                                changeProperties++
                                subscriptionService.updateProperty(controller, privateProp, params.filterPropValue)
                            }
                        }
                        args = [newProperties, changeProperties]
                        result.message = messageSource.getMessage('subscription.propertiesMembers.successful',args,locale)
                        [result:result,status:STATUS_OK]
                    }
                    else{
                        args = [newProperties, changeProperties]
                        result.error = messageSource.getMessage('subscription.propertiesMembers.successful', args, locale)
                        [result:result,status:STATUS_ERROR]
                    }
                }
                else [result:result,status:STATUS_ERROR]
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    Map<String,Object> processDeletePropertiesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            List<Subscription> validSubChilds = Subscription.findAllByInstanceOf(result.subscription)
            if (params.filterPropDef) {
                PropertyDefinition filterPropDef = (PropertyDefinition) genericOIDService.resolveOID(params.filterPropDef.replace(" ", ""))
                int deletedProperties = 0
                if (filterPropDef) {
                    result.filterPropDef = filterPropDef
                    validSubChilds.each { Subscription subChild ->
                        SubscriptionProperty existingProp
                        if (filterPropDef.tenant == result.institution) {
                            //private Property
                            existingProp = subChild.propertySet.find { SubscriptionProperty sp ->
                                sp.owner.id == subChild.id && sp.type.id == filterPropDef.id
                            }
                            if (existingProp){
                                try {
                                    subChild.propertySet.remove(existingProp)
                                    existingProp.delete()
                                    deletedProperties++
                                }
                                catch (Exception e) {
                                    log.error( e.toString() )
                                }
                            }
                        }
                        else {
                            //custom Property
                            existingProp = subChild.propertySet.find { SubscriptionProperty sp ->
                                sp.type.id == filterPropDef.id && sp.owner.id == subChild.id && sp.tenant.id == result.institution.id
                            }
                            if (existingProp && !(existingProp.hasProperty('instanceOf') && existingProp.instanceOf && AuditConfig.getConfig(existingProp.instanceOf))){
                                try {
                                    subChild.propertySet.remove(existingProp)
                                    existingProp.delete()
                                    deletedProperties++
                                }
                                catch (Exception e){
                                    log.error( e.toString() )
                                }
                            }
                        }
                    }
                    Object[] args = [deletedProperties]
                    result.message = messageSource.getMessage('subscription.propertiesMembers.deletedProperties', args, LocaleContextHolder.getLocale())
                }
            }
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> subscriptionPropertiesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
            if(params.tab == 'providerAgency') {
                result.modalPrsLinkRole = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)
                result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(result.institution)
                result.visibleOrgRelations = OrgRole.executeQuery("select oo from OrgRole oo join oo.org org where oo.sub = :parent and oo.roleType in (:roleTypes) order by org.name asc",[parent:result.subscription,roleTypes:[RDStore.OR_PROVIDER,RDStore.OR_AGENCY]])
            }
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> processSubscriptionPropertiesMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            List selectedMembers = params.list("selectedMembers")
            if(selectedMembers) {
                Set change = [], noChange = []
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
                Set<Subscription> subChildren = Subscription.findAllByIdInList(selectedMembers)
                subChildren.each { Subscription subChild ->
                    if(startDate && !auditService.getAuditConfig(subChild.instanceOf, 'startDate')) {
                        subChild.startDate = startDate
                        change << messageSource.getMessage('default.startDate.label',null,locale)
                    }
                    if(startDate && auditService.getAuditConfig(subChild.instanceOf, 'startDate')) {
                        noChange << messageSource.getMessage('default.startDate.label',null,locale)
                    }
                    if(endDate && !auditService.getAuditConfig(subChild.instanceOf, 'endDate')) {
                        subChild.endDate = endDate
                        change << messageSource.getMessage('default.endDate.label',null,locale)
                    }
                    if(endDate && auditService.getAuditConfig(subChild.instanceOf, 'endDate')) {
                        noChange << messageSource.getMessage('default.endDate.label',null,locale)
                    }
                    if(params.status && !auditService.getAuditConfig(subChild.instanceOf, 'status')) {
                        subChild.status = RefdataValue.get(params.status) ?: subChild.status
                        change << messageSource.getMessage('subscription.status.label',null,locale)
                    }
                    if(params.status && auditService.getAuditConfig(subChild.instanceOf, 'status')) {
                        noChange << messageSource.getMessage('subscription.status.label',null,locale)
                    }
                    if(params.kind && !auditService.getAuditConfig(subChild.instanceOf, 'kind')) {
                        subChild.kind = RefdataValue.get(params.kind) ?: subChild.kind
                        change << messageSource.getMessage('subscription.kind.label',null,locale)
                    }
                    if(params.kind && auditService.getAuditConfig(subChild.instanceOf, 'kind')) {
                        noChange << messageSource.getMessage('subscription.kind.label',null,locale)
                    }
                    if(params.form && !auditService.getAuditConfig(subChild.instanceOf, 'form')) {
                        subChild.form = RefdataValue.get(params.form) ?: subChild.form
                        change << messageSource.getMessage('subscription.form.label',null,locale)
                    }
                    if(params.form && auditService.getAuditConfig(subChild.instanceOf, 'form')) {
                        noChange << messageSource.getMessage('subscription.form.label',null,locale)
                    }
                    if(params.resource && !auditService.getAuditConfig(subChild.instanceOf, 'resource')) {
                        subChild.resource = RefdataValue.get(params.resource) ?: subChild.resource
                        change << messageSource.getMessage('subscription.resource.label',null,locale)
                    }
                    if(params.resource && auditService.getAuditConfig(subChild.instanceOf, 'resource')) {
                        noChange << messageSource.getMessage('subscription.resource.label',null,locale)
                    }
                    if(params.isPublicForApi && !auditService.getAuditConfig(subChild.instanceOf, 'isPublicForApi')) {
                        subChild.isPublicForApi = RefdataValue.get(params.isPublicForApi) == RDStore.YN_YES
                        change << messageSource.getMessage('subscription.isPublicForApi.label',null,locale)
                    }
                    if(params.isPublicForApi && auditService.getAuditConfig(subChild.instanceOf, 'isPublicForApi')) {
                        noChange << messageSource.getMessage('subscription.isPublicForApi.label',null,locale)
                    }
                    if(params.hasPerpetualAccess && !auditService.getAuditConfig(subChild.instanceOf, 'hasPerpetualAccess')) {
                        subChild.hasPerpetualAccess = RefdataValue.get(params.hasPerpetualAccess) == RDStore.YN_YES
                        //subChild.hasPerpetualAccess = RefdataValue.get(params.hasPerpetualAccess)
                        change << messageSource.getMessage('subscription.hasPerpetualAccess.label',null,locale)
                    }
                    if(params.hasPerpetuaLAccess && auditService.getAuditConfig(subChild.instanceOf, 'hasPerpetualAccess')) {
                        noChange << messageSource.getMessage('subscription.hasPerpetualAccess.label',null,locale)
                    }
                    if(params.hasPublishComponent && !auditService.getAuditConfig(subChild.instanceOf, 'hasPublishComponent')) {
                        subChild.hasPublishComponent = RefdataValue.get(params.hasPublishComponent) == RDStore.YN_YES
                        change << messageSource.getMessage('subscription.hasPublishComponent.label',null,locale)
                    }
                    if(params.hasPublishComponent && auditService.getAuditConfig(subChild.instanceOf, 'hasPublishComponent')) {
                        noChange << messageSource.getMessage('subscription.hasPublishComponent.label',null,locale)
                    }
                    if (subChild.isDirty()) {
                        subChild.save()
                    }
                }
                [result:result,status:STATUS_OK]
            }
            else {
                result.error = messageSource.getMessage('subscription.subscriptionPropertiesMembers.noSelectedMember',null,locale)
                [result:result,status:STATUS_ERROR]
            }
        }
    }

    //--------------------------------------- survey section -------------------------------------------

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

    Map<String,Object> renewEntitlementsWithSurvey(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)

            Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : Subscription.get(params.id)
            Subscription baseSub = result.surveyConfig.subscription ?: newSub.instanceOf
            params.id = newSub.id
            params.sourceObjectId = baseSub.id
            params.tab = params.tab ?: 'allIEs'
            List<IssueEntitlement> sourceIEs
            if(params.tab == 'allIEs') {
                Map query = filterService.getIssueEntitlementQuery(params, baseSub)
                sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            }
            if(params.tab == 'selectedIEs') {
                Map query = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusNotFixed: true], newSub)
                sourceIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            }

            Map query = filterService.getIssueEntitlementQuery(params, newSub)
            List<IssueEntitlement> targetIEs = IssueEntitlement.executeQuery("select ie.id " + query.query, query.queryParams)
            List<Long> allIEs = subscriptionService.getIssueEntitlementIDsFixed(baseSub)
            List<Long> notFixedIEs = subscriptionService.getIssueEntitlementIDsNotFixed(newSub)

            result.countSelectedIEs = notFixedIEs.size()
            result.countAllIEs = allIEs.size()
            result.countAllSourceIEs = sourceIEs.size()
            result.num_ies_rows = sourceIEs.size()
            //subscriptionService.getIssueEntitlementsFixed(baseSub).size()
            result.sourceIEIDs = sourceIEs
            result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs.drop(result.offset).take(result.max)) : []
            result.targetIEs = []
            targetIEs.collate(32767).each {
                result.targetIEs.addAll(IssueEntitlement.findAllByIdInList(targetIEs.take(32768)))
            }
            result.newSub = newSub
            result.subscription = baseSub
            result.subscriber = result.newSub.getSubscriber()
            result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)

            [result:result,status:STATUS_OK]
        }
    }

    //-------------------------------------- packages section ------------------------------------------

    Map<String,Object> linkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
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
                esQuery += "&providerName=${params.provider}"
            }

            if(params.curatoryGroup) {
                result.filterSet = true
                esQuery += "&curatoryGroup=${params.curatoryGroup}"
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

    Map<String,Object> processLinkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleContextHolder.getLocale()
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each { Thread thread ->
                if (thread.name == 'PackageSync_'+result.subscription.id && !SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,Package.findByGokbId(params.addUUID))) {
                    result.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null,locale)
                }
            }
            if(params.addUUID) {
                String pkgUUID = params.addUUID
                String addType = params.addType
                String addTypeChildren = params.addTypeChildren
                if(!Package.findByGokbId(pkgUUID)) {
                    ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                    result.source = apiSource.baseUrl
                    GlobalRecordSource source = GlobalRecordSource.findByUriLike(result.source+'%')
                    log.debug("linkPackage. Global Record Source URL: " +source.uri)
                    globalSourceSyncService.source = source
                    //to be deployed in parallel thread
                    executorService.execute({
                        Thread.currentThread().setName("PackageSync_"+result.subscription.id)
                        try {
                            globalSourceSyncService.defineMapFields()
                            Map<String,Object> queryResult = globalSourceSyncService.fetchRecordJSON(false,[componentType:'TitleInstancePackagePlatform',pkg:pkgUUID,max:5000])
                            if(queryResult.error && queryResult.error == 404) {
                                log.error("we:kb server currently unavailable")
                            }
                            else {
                                if(queryResult.records && queryResult.count > 0) {
                                    globalSourceSyncService.updateRecords(queryResult.records, 0)
                                }
                                else {
                                    globalSourceSyncService.createOrUpdatePackage(pkgUUID)
                                }
                                Package pkgToLink = Package.findByGokbId(pkgUUID)
                                result.packageName = pkgToLink.name
                                log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                                subscriptionService.addToSubscription(result.subscription, pkgToLink, addType == 'With')
                                if(addTypeChildren) {
                                    Subscription.findAllByInstanceOf(result.subscription).each { Subscription childSub ->
                                        subscriptionService.addToSubscription(childSub, pkgToLink, addTypeChildren == 'WithForChildren')
                                    }
                                }
                                subscriptionService.addPendingChangeConfiguration(result.subscription, pkgToLink, params.clone())
                            }
                        }
                        catch (Exception e) {
                            log.error("sync job has failed, please consult stacktrace as follows: ")
                            e.printStackTrace()
                        }
                    })
                }
                else {
                    Package pkgToLink = Package.findByGokbId(pkgUUID)
                    log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                    subscriptionService.addToSubscription(result.subscription, pkgToLink, addType == 'With')
                    if(addTypeChildren) {
                        Subscription.findAllByInstanceOf(result.subscription).each { Subscription childSub ->
                            subscriptionService.addToSubscription(childSub, pkgToLink, addTypeChildren == 'WithForChildren')
                        }
                    }
                    subscriptionService.addPendingChangeConfiguration(result.subscription, pkgToLink, params.clone())
                }
            }

            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> unlinkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.package = Package.get(params.package)
        Locale locale = LocaleContextHolder.getLocale()
        if(params.confirmed) {
            if(result.package.unlinkFromSubscription(result.subscription, result.institution, true)){
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
            int numOfPCs = result.package.removePackagePendingChanges([result.subscription.id], false)
            int numOfIEs = IssueEntitlement.executeQuery(query, queryParams).size()
            int numOfCIs = CostItem.findAllBySubPkg(SubscriptionPackage.findBySubscriptionAndPkg(result.subscription,result.package)).size()
            List conflictsList = packageService.listConflicts(result.package,result.subscription,numOfPCs,numOfIEs,numOfCIs)
            //Automatisch Paket entknüpfen, wenn das Paket in der Elternlizenz entknüpft wird
            if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]){
                List<Subscription> childSubs = Subscription.findAllByInstanceOf(result.subscription)
                if (childSubs) {
                    String queryChildSubs = "select ie.id from IssueEntitlement ie, Package pkg where ie.subscription in (:sub) and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
                    Map<String,Object> queryParamChildSubs = [sub: childSubs, pkg_id: result.package.id]
                    List childSubsPackages = SubscriptionPackage.findAllBySubscriptionInListAndPkg(childSubs, result.package)
                    int numOfPCsChildSubs = result.package.removePackagePendingChanges(childSubs.id, false)
                    int numOfIEsChildSubs = IssueEntitlement.executeQuery(queryChildSubs, queryParamChildSubs).size()
                    int numOfCIsChildSubs = childSubsPackages ? CostItem.findAllBySubPkgInList(childSubsPackages).size() : 0
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

    Map<String,Object> index(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleContextHolder.getLocale()
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'PackageSync_'+result.subscription.id) {
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
                List issueEntitlements = entitlements.toList()
                result.enrichmentProcess = subscriptionService.issueEntitlementEnrichment(stream, issueEntitlements, (params.uploadCoverageDates == 'on'), (params.uploadPriceInfo == 'on'))
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
                String orderClause = 'order by tipp.sortName asc'
                if(params.sort){
                    if(params.sort == 'startDate')
                        orderClause = "order by ic.startDate ${params.order}, lower(ie.tipp.sortName) asc "
                    else if(params.sort == 'endDate')
                        orderClause = "order by ic.endDate ${params.order}, lower(ie.tipp.sortName) asc "
                    else
                        orderClause = "order by ${params.sort} ${params.order} "
                }
                result.entitlements = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp tipp left join ie.coverages ic where ie.id in (:entIDs) '+orderClause,[entIDs:entitlements.drop(result.offset).take(result.max)])
                result.journalsOnly = result.entitlements.find { IssueEntitlement ie -> ie.tipp.titleType != RDStore.TITLE_TYPE_JOURNAL.value } == null
            }
            else result.entitlements = []
            Set<SubscriptionPackage> deletedSPs = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.pkg.packageStatus == RDStore.PACKAGE_STATUS_DELETED}
            if(deletedSPs) {
                result.deletedSPs = []
                ApiSource source = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                deletedSPs.each { sp ->
                    result.deletedSPs << [name:sp.pkg.name,link:"${source.editUrl}/public/packageContent/?id=${sp.pkg.gokbId}"]
                }
            }
            if (executorWrapperService.hasRunningProcess(result.subscription)) {
                result.processingpc = true
            }

            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> entitlementChanges(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        SwissKnife.setPaginationParams(result,params,(User) result.user)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            List<SubscriptionPackage> pkgList = []
            Set<String> pendingOrWithNotification = []
            Set<Long> packageHistory = [], accepted = []
            Set subscriptionHistory = []
            Set<PendingChange> changesOfPage = []
            result.subscription.packages.each { SubscriptionPackage sp ->
                pkgList << sp
                pendingOrWithNotification.addAll(sp.pendingChangeConfig.findAll { PendingChangeConfiguration pcc -> pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT || pcc.withNotification }.collect{ PendingChangeConfiguration pcc -> pcc.settingKey })
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

            if(pkgList && pendingOrWithNotification) {
                pkgList.each { SubscriptionPackage sp ->
                    Package pkg = sp.pkg
                    Date entryDate = sp.dateCreated
                    String query = 'select pc.id from PendingChange pc where pc.pkg = :package and pc.oid = null and pc.status = :history ',
                           query1a = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.oid = :subOid and pc.status in (:pendingStatus)',
                           query2a = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and pc.oid = :subOid and pc.status in (:pendingStatus)',
                    //query3a = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status in (:pendingStatus)',
                           query1b = 'select pc.id from PendingChange pc join pc.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tipp tippA where tippA = pc.tipp and pca.oid = :subOid and pca.status in (:pendingStatus)) and pc.status = :packageHistory',
                           query2b = 'select pc.id from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg = :package and pc.ts >= :entryDate and pc.msgToken = :eventType and not exists (select pca.id from PendingChange pca join pca.tippCoverage tcA where tcA = pc.tippCoverage and pca.oid = :subOid and pc.status in (:pendingStatus)) and pc.status = :packageHistory',
                    //query3b = 'select pc.id,pc.priceItem from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg = :package and pc.oid = (:subOid) and pc.status not in (:pendingStatus)',
                           query1c = 'select pc.id from PendingChange pc where pc.subscription = :subscription and pc.msgToken = :eventType and pc.status not in (:pendingStatus)'
                    subscriptionHistory.addAll(PendingChange.executeQuery(query,[package: pkg, history: RDStore.PENDING_CHANGE_HISTORY]))
                    subscriptionHistory.addAll(PendingChange.executeQuery(query1a,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    subscriptionHistory.addAll(PendingChange.executeQuery(query2a,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    //subscriptionHistory.addAll(PendingChange.executeQuery(query3a,[package: pkg, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    changesOfPage.addAll(PendingChange.executeQuery(query1b,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
                    changesOfPage.addAll(PendingChange.executeQuery(query2b,[package: pkg, entryDate: entryDate, eventType: params.eventType, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription), packageHistory: RDStore.PENDING_CHANGE_HISTORY]))
                    //changesOfPage.addAll(PendingChange.executeQuery(query3b,[packages: pkgList, pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_HISTORY, RDStore.PENDING_CHANGE_REJECTED], subOid: genericOIDService.getOID(result.subscription)]))
                    changesOfPage.addAll(PendingChange.executeQuery(query1c,[pendingStatus: [RDStore.PENDING_CHANGE_ACCEPTED, RDStore.PENDING_CHANGE_REJECTED], eventType: params.eventType, subscription: result.subscription]))
                }
            }

            params.tab = params.tab ?: 'changes'
            params.sort = params.sort ?: 'ts'
            params.order = params.order ?: 'desc'
            params.max = result.max
            params.offset = result.offset

            result.countPendingChanges = changesOfPage.size()
            result.countAcceptedChanges = subscriptionHistory.size()

            if(params.tab == 'changes') {
                result.changes = changesOfPage ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:changesOfPage) order by '+params.sort+' '+params.order, [changesOfPage: changesOfPage], [max: result.max, offset: result.offset]) : []
                result.num_change_rows = changesOfPage.size()
            }

            if(params.tab == 'acceptedChanges') {
                result.changes = subscriptionHistory ? PendingChange.executeQuery('select pc from PendingChange pc where pc.id in (:subscriptionHistory) order by '+params.sort+' '+params.order, [subscriptionHistory: subscriptionHistory], [max: result.max, offset: result.offset]) : []
                result.num_change_rows = subscriptionHistory.size()
            }

            result.apisources = ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

            result.packages = result.subscription.packages

            [result:result,status:STATUS_OK]
        }
    }

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
            RefdataValue ie_deleted = RDStore.TIPP_STATUS_DELETED
            RefdataValue ie_current = RDStore.TIPP_STATUS_CURRENT
            List<Long> tippIDs = []
            List<TitleInstancePackagePlatform> tipps = []
            List errorList = []
            boolean filterSet = false
            EhcacheWrapper checkedCache = contextService.getCache("/subscription/addEntitlements/${params.id}", contextService.USER_SCOPE)
            Set<String> addedTipps = IssueEntitlement.executeQuery('select tipp.gokbId from IssueEntitlement ie join ie.tipp tipp where ie.status != :deleted and ie.subscription = :sub',[deleted:ie_deleted,sub:result.subscription])
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
            Map<String, Object> query = filterService.getTippQuery(params, result.subscription.packages?.pkg)
            result.filterSet = query.filterSet

            if(result.subscription.packages?.pkg) {
                Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
                if(tippIds)
                    tipps.addAll(TitleInstancePackagePlatform.findAllByIdInList(tippIds.drop(result.offset).take(result.max),[sort:'sortName']))
                //now, assemble the identifiers available to highlight
                Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNs('zdb'),
                                                               eissn: IdentifierNamespace.findByNs('eissn'), isbn: IdentifierNamespace.findByNs('isbn'),
                                                               issn : IdentifierNamespace.findByNs('issn'), pisbn: IdentifierNamespace.findByNs('pisbn')]
                result.num_tipp_rows = tippIds.size()
                result.tipps = tipps
                Map<String, Object> identifiers = [zdbIds: [], onlineIds: [], printIds: [], unidentified: []]
                Map<String, Map> issueEntitlementOverwrite = [:]
                result.issueEntitlementOverwrite = [:]
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
                    //after having read off the header row, pop the first row
                    rows.remove(0)
                    rows.eachWithIndex { row, int i ->
                        Map<String, Object> ieCandidate = [:]
                        ArrayList<String> cols = row.split('\t')
                        Map<String, Object> idCandidate = [:]
                        String ieCandIdentifier
                        if (colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                            identifiers.zdbIds.add(cols[colMap.zdbCol])
                            idCandidate = [namespaces: namespaces.zdb, value: cols[colMap.zdbCol]]
                            if (issueEntitlementOverwrite[cols[colMap.zdbCol]])
                                ieCandidate = issueEntitlementOverwrite[cols[colMap.zdbCol]]
                            else ieCandIdentifier = cols[colMap.zdbCol]
                        }
                        if (colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                            identifiers.onlineIds.add(cols[colMap.onlineIdentifierCol])
                            idCandidate = [namespaces: [], value: cols[colMap.onlineIdentifierCol]]
                            idCandidate.namespaces.add(namespaces.eissn)
                            idCandidate.namespaces.add(namespaces.isbn)
                            if (ieCandIdentifier == null && !issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                                ieCandIdentifier = cols[colMap.onlineIdentifierCol]
                            else if (issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                                ieCandidate = issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]]
                        }
                        if (colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
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
                                TitleInstancePackagePlatform tipp = matchingTipps.find { TitleInstancePackagePlatform matchingTipp -> matchingTipp.pkg == result.subscription.packages.pkg } as TitleInstancePackagePlatform
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
                            ieCoverages.add(covStmt)
                            ieCandidate.coverages = ieCoverages
                            issueEntitlementOverwrite[ieCandIdentifier] = ieCandidate
                        }
                    }
                    result.checked = [:]
                    Map<String, Object> unfilteredParams = [pkg:result.subscription.packages.pkg, deleted:RDStore.TIPP_STATUS_DELETED]
                    Set<String> selectedTippIds = [], identifierValues = []
                    identifiers.values().each { subList ->
                        if(subList instanceof List)
                            identifierValues.addAll(subList)
                    }
                    identifierValues.collate(32700).each { List<String> chunk ->
                        unfilteredParams.idList = chunk
                        selectedTippIds.addAll(TitleInstancePackagePlatform.executeQuery('select tipp.gokbId from TitleInstancePackagePlatform tipp join tipp.ids id where tipp.pkg = :pkg and tipp.status != :deleted and id.value in (:idList)',unfilteredParams))
                        selectedTippIds.removeAll(addedTipps)
                    }
                    selectedTippIds.each { String wekbId ->
                        println("located tipp: ${wekbId}")
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
                List<Map> allTippsFiltered = TitleInstancePackagePlatform.executeQuery("select new map(tipp.titleType as titleType, tipp.gokbId as gokbId) from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.status != :deleted",unfilteredParams)
                    List<Map> identifierRows = Identifier.executeQuery("select new map(tipp.gokbId as gokbId, id.value as value, ns.ns as namespace) from Identifier id join id.ns ns join id.tipp tipp where tipp.pkg = :pkg and tipp.status != :deleted",unfilteredParams)
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

    Map<String,Object> removeEntitlement(GrailsParameterMap params) {
        IssueEntitlement ie = IssueEntitlement.get(params.ieid)
        ie.status = RDStore.TIPP_STATUS_DELETED
        if(ie.save())
            [result:null,status:STATUS_OK]
        else [result:null,status:STATUS_ERROR]
    }

    Map<String,Object> removeEntitlementWithIEGroups(GrailsParameterMap params) {
        IssueEntitlement ie = IssueEntitlement.get(params.ieid)
        RefdataValue oldStatus = ie.status
        ie.status = RDStore.TIPP_STATUS_DELETED
        if(ie.save()){
            if(IssueEntitlementGroupItem.executeUpdate("delete from IssueEntitlementGroupItem iegi where iegi.ie = :ie", [ie: ie]))
            {
                return [result:null,status:STATUS_OK]
            }else {
                ie.status = oldStatus
                ie.save()
                return [result:null,status:STATUS_ERROR]
            }
        }
        else {
            return [result:null,status:STATUS_ERROR]
        }
    }

    Map<String,Object> processAddEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            RefdataValue ie_accept_status = RDStore.IE_ACCEPT_STATUS_FIXED
            int addTitlesCount = 0
            EhcacheWrapper cache = contextService.getCache("/subscription/addEntitlements/${result.subscription.id}", contextService.USER_SCOPE)
            Map issueEntitlementCandidates = cache.get('issueEntitlementCandidates')
            if(!params.singleTitle) {
                Map checked = cache.get('checked')
                if(checked) {
                    executorService.execute({
                        Thread.currentThread().setName("EntitlementEnrichment_${result.subscription.id}")
                        IssueEntitlement.withNewTransaction { TransactionStatus ts ->
                            checked.each { k, v ->
                                if(v == 'checked') {
                                    try {
                                        if(issueEntitlementCandidates?.get(k) || Boolean.valueOf(params.uploadPriceInfo))  {
                                            if(subscriptionService.addEntitlement(result.subscription, k, issueEntitlementCandidates?.get(k), Boolean.valueOf(params.uploadPriceInfo), ie_accept_status))
                                                log.debug("Added tipp ${k} to sub ${result.subscription.id} with issue entitlement overwrites")
                                        }
                                        else if(subscriptionService.addEntitlement(result.subscription,k,null,false, ie_accept_status)) {
                                            log.debug("Added tipp ${k} to sub ${result.subscription.id}")
                                        }
                                        addTitlesCount++
                                        if(addTitlesCount > 0 && addTitlesCount % 10000 == 0) {
                                            log.debug("interim flush at ${addTitlesCount}")
                                            ts.flush()
                                        }
                                    }
                                    catch (EntitlementCreationException e) {
                                        result.error = e.getMessage()
                                    }
                                }
                            }
                        }
                        Object[] args = [addTitlesCount]
                        result.message = messageSource.getMessage('subscription.details.addEntitlements.titlesAddToSub',args,locale)
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
                        if(subscriptionService.addEntitlement(result.subscription, params.singleTitle, issueEntitlementCandidates?.get(params.singleTitle), Boolean.valueOf(params.uploadPriceInfo), ie_accept_status))
                            log.debug("Added tipp ${params.singleTitle} to sub ${result.subscription.id} with issue entitlement overwrites")
                        result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                    }
                    else if(subscriptionService.addEntitlement(result.subscription, params.singleTitle, null, false, ie_accept_status))
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

    Map<String,Object> subscriptionBatchUpdate(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SimpleDateFormat formatter = DateUtils.getSDF_NoTime()
            boolean error = false
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
                        if (params.titleGroup && (params.titleGroup.trim().length() > 0)) {
                            IssueEntitlementGroup entitlementGroup = IssueEntitlementGroup.get(Long.parseLong(params.titleGroup))
                            if(entitlementGroup && !IssueEntitlementGroupItem.findByIeGroupAndIe(entitlementGroup, ie) && !IssueEntitlementGroupItem.findByIe(ie)){
                                IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                        ie: ie,
                                        ieGroup: entitlementGroup)
                                if (!issueEntitlementGroupItem.save()) {
                                    log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                                }
                            }
                        }
                        if (!ie.save()) {
                            log.error("Problem saving ${ie.errors}")
                            error = true
                        }
                    } else if (params.bulkOperation == "remove") {
                        log.debug("Updating ie ${ie.id} status to deleted")
                        ie.status = RDStore.TIPP_STATUS_DELETED
                        if (!ie.save()) {
                            log.error("Problem saving ${ie.errors}")
                            error = true
                        }
                    }
                }
            }
            if(error)
                [result:result,status:STATUS_ERROR]
            else [result:result,status:STATUS_OK]
        }
    }

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

    Map<String,Object> processRenewEntitlementsWithSurvey(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            Locale locale = LocaleContextHolder.getLocale()
            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.editable = surveyService.isEditableIssueEntitlementsSurvey(result.institution, result.surveyConfig)
            if(!result.editable) {
                [result:null,status:STATUS_ERROR]
            }

            if(!params."iesToAdd"){
                result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps',null,locale)
                [result:result,status:STATUS_ERROR]

            }else if(params.process == "preliminary" && params."iesToAdd") {
                List iesToAdd = params."iesToAdd".split(",")
                Integer countIEsToAdd = 0
                iesToAdd.each { ieID ->
                    IssueEntitlement ie = IssueEntitlement.findById(ieID)
                    TitleInstancePackagePlatform tipp = ie.tipp
                    try {
                        if (subscriptionService.addEntitlement(result.subscription, tipp.gokbId, ie, (ie.priceItems != null), RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION)) {
                            log.debug("Added tipp ${tipp.gokbId} to sub ${result.subscription.id}")
                            ++countIEsToAdd
                        }
                    }
                    catch (EntitlementCreationException e) {
                        log.debug("Error: Adding tipp ${tipp} to sub ${result.subscription.id}: " + e.getMessage())
                        result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps',null,locale)
                        [result:result,status:STATUS_ERROR]
                    }
                }
                if(countIEsToAdd > 0){
                    Object[] args = [countIEsToAdd]
                    result.message = messageSource.getMessage('renewEntitlementsWithSurvey.tippsToAdd',args,locale)
                }
                [result:result,status:STATUS_OK]

            } else if(params.process == "remove" && params."iesToAdd") {
                List iesToAdd = params."iesToAdd".split(",")
                Integer countIEsToDelete = 0
                iesToAdd.each { ieID ->
                    try {
                        if (subscriptionService.deleteEntitlementbyID(result.subscription, ieID)) {
                            ++countIEsToDelete
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
                [result:result,status:STATUS_OK]
            }
        }
    }

    //--------------------------------------------- renewal section ---------------------------------------------

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
                    result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                            [sub: result.subscription.instanceOf,
                             org: result.subscription.getSubscriber(),
                             invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                }else{
                    result.currentSurveysCounts = SurveyConfig.findAllBySubscription(result.subscription).size()
                }
                result.currentMembersCounts =  Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',[parent: result.subscription, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])[0]
            }else{
                result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                        [sub: result.subscription.instanceOf,
                         org: result.subscription.getSubscriber(),
                         invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
            }
            result.showConsortiaFunctions = subscriptionService.showConsortiaFunctions(result.contextOrg, result.subscription)

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


    Map<String,Object> reporting(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription sub = Subscription.get(params.id)

        result.token         = params.token ?: RandomStringUtils.randomAlphanumeric(16)
        result.cfgQueryList  = SubscriptionReporting.CONFIG.base.query.default
        result.cfgQueryList2 = SubscriptionReporting.getCurrentQuery2Config( sub )

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }

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

        [result: result, status: (result ? STATUS_OK : STATUS_ERROR)]
    }
}
