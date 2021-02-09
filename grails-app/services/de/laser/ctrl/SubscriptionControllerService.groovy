package de.laser.ctrl

import com.k_int.kbplus.*
import de.laser.*
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
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
import de.laser.titles.BookInstance
import de.laser.titles.JournalInstance
import de.laser.titles.TitleInstance
import grails.doc.internal.StringEscapeCategory
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory
import groovy.util.slurpersupport.GPathResult
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
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
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = result.subscription.orgRelations.findAll { OrgRole oo -> !(oo.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]) }
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
                    PlatformProperty platform = PlatformProperty.executeQuery('select pp from PlatformProperty pp join pp.type pd where pp.owner.id = :owner and pd.name = :name and pd.descr = :descr', [owner: supplier_id, name: 'NatStat Supplied ID', descr: PropertyDefinition.PLA_PROP])
                    //        PlatformProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
                    pu.setBenchmark('before institutional usage identifier')
                    result.natStatSupplierId = platform?.stringValue ?: null
                    if (result.institutional_usage_identifier != OrgSetting.SETTING_NOT_FOUND) {
                        pu.setBenchmark('before usage data')
                        def fsresult = factService.generateUsageData(result.institution.id, supplier_id, result.subscription)
                        pu.setBenchmark('before usage data sub period')
                        def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(result.institution.id, supplier_id, result.subscription)
                        Set<RefdataValue> holdingTypes = RefdataValue.executeQuery('select ti.medium from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :context', [context: result.subscription])
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
                            result.l_x_axis_labels = fsLicenseResult?.x_axis_labelsresult.l_y_axis_labels = fsLicenseResult?.y_axis_labels
                        }
                    } else
                        log.info('institutional usage identifier not available')
                }
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
            if (params.deleteId) {
                Locale locale = LocaleContextHolder.getLocale()
                Task dTask = Task.get(params.deleteId)
                if (dTask && dTask.creator.id == result.user.id) {
                    try {
                        Object[] args = [messageSource.getMessage('task.label',null,locale), dTask.title]
                        result.message = messageSource.getMessage('default.deleted.message',args,locale)
                        dTask.delete()
                    }
                    catch (Exception e) {
                        log.error(e)
                        Object[] args = [messageSource.getMessage('task.label',null,locale), params.deleteId]
                        result.error = messageSource.getMessage('default.not.deleted.message', args, locale)
                    }
                }
            }
            int offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.taskInstanceList = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.subscription)
            result.taskInstanceCount = result.taskInstanceList.size()
            result.taskInstanceList = taskService.chopOffForPageSize(result.taskInstanceList, result.user, offset)

            result.myTaskInstanceList = taskService.getTasksByCreatorAndObject(result.user,  result.subscription)
            result.myTaskInstanceCount = result.myTaskInstanceList.size()
            result.myTaskInstanceList = taskService.chopOffForPageSize(result.myTaskInstanceList, result.user, offset)

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
        if(!result)
            [result:null,status:STATUS_ERROR]
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
        //result.validSubChilds = Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parent:result.subscription,subscriberRoleTypes:subscriberRoleTypes])
        result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
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
            Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
            result.members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            result.members_disabled = Subscription.executeQuery("select oo.org.id from OrgRole oo join oo.sub s where s.instanceOf = :io",[io: result.subscription])
            result.validPackages = result.subscription.packages?.sort { it.pkg.name }
            result.memberLicenses = License.executeQuery("select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType",[subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE])

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
                        String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
                        licensesToProcess.addAll(License.executeQuery(query, [subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE]))
                    }
                    else if(params.generateSlavedLics == "partial") {
                        List<String> licenseKeys = params.list("generateSlavedLicsReference")
                        licenseKeys.each { String licenseKey ->
                            licensesToProcess << genericOIDService.resolveOID(licenseKey)
                        }
                    }
                    Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name,result.subscription.id, PendingChangeConfiguration.SETTING_KEYS)
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

                                    memberSub.refresh()

                                    packagesToProcess.each { Package pkg ->
                                        if(params.linkWithEntitlements)
                                            pkg.addToSubscriptionCurrentStock(memberSub, result.subscription)
                                        else
                                            pkg.addToSubscription(memberSub, false)
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
                }
                if(params.next && nextMemberSub) {
                    link = Links.construct([source: nextMemberSub, destination: memberSub, linkType: RDStore.LINKTYPE_FOLLOWS, owner: result.contextOrg])
                }
                if(link) {
                    result.redirect = link.id
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
            result.validPackages = result.subscription.packages
            result.filteredSubChilds = getFilteredSubscribers(params,result.subscription)
            result.childWithCostItems = CostItem.executeQuery('select ci.subPkg from CostItem ci where ci.subPkg.subscription in (:filteredSubChildren)',[filteredSubChildren:result.filteredSubChilds.collect { row -> row.sub }])
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
                    selectedMembers.each { id ->
                        Subscription subChild = Subscription.get(Long.parseLong(id))
                        if (params.processOption == 'linkwithIE' || params.processOption == 'linkwithoutIE') {
                            if (!(pkg_to_link in subChild.packages.pkg)) {
                                if (params.processOption == 'linkwithIE') {
                                    pkg_to_link.addToSubscriptionCurrentStock(subChild, result.subscription)

                                } else {
                                    pkg_to_link.addToSubscription(subChild, false)
                                }
                            }
                        }
                        if (params.processOption == 'unlinkwithIE' || params.processOption == 'unlinkwithoutIE') {
                            if (pkg_to_link in subChild.packages.pkg) {
                                if (params.processOption == 'unlinkwithIE') {
                                    pkg_to_link.unlinkFromSubscription(subChild, true)
                                } else {
                                    pkg_to_link.unlinkFromSubscription(subChild, false)
                                }
                            }
                        }
                    }
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
                if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg = :sp',[sp:sp])) {
                    if (params.withIE) {
                        if(sp.pkg.unlinkFromSubscription(sp.subscription, true)){
                            result.message << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.withIE.successful',null,locale)
                        }
                        else {
                            result.error << messageSource.getMessage('subscription.linkPackagesMembers.unlinkInfo.withIE.fail',null,locale)
                        }
                    }
                    else {
                        if(sp.pkg.unlinkFromSubscription(sp.subscription, false)){
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
                                it.type.id == filterPropDef.id && it.owner.id == subChild.id && it.tenant.id == result.institution.id
                            }
                            if (existingProp && !(existingProp.hasProperty('instanceOf') && existingProp.instanceOf && AuditConfig.getConfig(existingProp.instanceOf))){
                                try {
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
                        change << messageSource.getMessage('subscription.hasPerpetualAccess.label',null,locale)
                    }
                    if(params.hasPerpetuaLAccess && auditService.getAuditConfig(subChild.instanceOf, 'hasPerpetualAccess')) {
                        noChange << messageSource.getMessage('subscription.hasPerpetualAccess.label',null,locale)
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

            result.editUrl = apiSource.baseUrl+apiSource.fixToken
            String esQuery = "?componentType=Package"
            if(params.q) {
                result.filterSet = true
                //for ElasticSearch
                esQuery += "&name=${params.q}"
                //the result set has to be broadened down by IdentifierNamespace queries! Problematic if the package is not in LAS:eR yet!
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


            /*
            to implement:
            - provider
            - componentType
            - series
            - subjectArea
            - curatoryGroup
            - year (combination of dateFirstPrint and dateFirstOnline)
             */

            String sort = params.sort ?: "&sort=sortname"
            String order = params.order ?: "&order=asc"
            String max = params.max ? "&max=${params.max}": "&max=${result.max}"
            String offset = params.offset ? "&offset=${params.offset}": "&offset=${result.offset}"

            Map queryCuratoryGroups = gokbService.queryElasticsearch(apiSource.baseUrl+apiSource.fixToken+'/groups')
            if(queryCuratoryGroups.warning) {
                List recordsCuratoryGroups = queryCuratoryGroups.warning.result
                result.curatoryGroups = recordsCuratoryGroups
            }


            Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl+apiSource.fixToken+'/find'+esQuery+sort+order+max+offset)
            if(queryResult.warning) {
                List records = queryResult.warning.records
                result.recordsCount = queryResult.warning.count
                result.records = records
            }

            [result:result,status:STATUS_OK]
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
            //to be deployed in parallel thread
            if(params.addUUID) {
                String pkgUUID = params.addUUID
                String addType = params.addType
                if(!Package.findByGokbId(pkgUUID)) {
                    ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                    result.source = apiSource.baseUrl+apiSource.fixToken
                    GlobalRecordSource source = GlobalRecordSource.findByUri("${result.source}/gokb/api/")
                    log.debug("linkPackage. Global Record Source URL: " +source.baseUrl)
                    globalSourceSyncService.source = source
                    executorService.execute({
                        Thread.currentThread().setName("PackageSync_"+result.subscription.id)
                        try {
                            globalSourceSyncService.defineMapFields()
                            Map<String,Object> queryResult = globalSourceSyncService.fetchRecordJSON(false,[componentType:'TitleInstancePackagePlatform',pkg:pkgUUID])
                            globalSourceSyncService.updateRecords(queryResult.records)
                            Package pkgToLink = Package.findByGokbId(pkgUUID)
                            result.packageName = pkgToLink.name
                            log.debug("Add package ${addType} entitlements to subscription ${result.subscription}")
                            if (addType == 'With') {
                                pkgToLink.addToSubscription(result.subscription, true)
                            }
                            else if (addType == 'Without') {
                                pkgToLink.addToSubscription(result.subscription, false)
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
                    if (addType == 'With') {
                        pkgToLink.addToSubscription(result.subscription, true)
                    }
                    else if (addType == 'Without') {
                        pkgToLink.addToSubscription(result.subscription, false)
                    }
                }

                if (addType != null && addType != '') {
                    Package pkgToLink = Package.findByGokbId(pkgUUID)
                    SubscriptionPackage subscriptionPackage = SubscriptionPackage.findBySubscriptionAndPkg(result.subscription, pkgToLink)
                    if(subscriptionPackage) {
                        PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
                            Map<String, Object> configMap = [subscriptionPackage: subscriptionPackage, settingKey: settingKey, withNotification: false]
                            boolean auditable = false
                            //Set because we have up to three keys in params with the settingKey
                            Set<String> keySettings = params.keySet().findAll { k -> k.contains(settingKey) }
                            keySettings.each { key ->
                                List<String> settingData = key.split('!§!')
                                switch (settingData[1]) {
                                    case 'setting': configMap.settingValue = RefdataValue.get(params[key])
                                        break
                                    case 'notification': configMap.withNotification = params[key] != null
                                        break
                                    case 'auditable': auditable = params[key] != null
                                        break
                                }
                            }
                            try {
                                PendingChangeConfiguration.construct(configMap)
                                boolean hasConfig = AuditConfig.getConfig(subscriptionPackage.subscription, settingKey) != null
                                if (auditable && !hasConfig) {
                                    AuditConfig.addConfig(subscriptionPackage.subscription, settingKey)
                                } else if (!auditable && hasConfig) {
                                    AuditConfig.removeConfig(subscriptionPackage.subscription, settingKey)
                                }
                            }
                            catch (CreationException e) {
                                log.error("ProcessLinkPackage -> PendingChangeConfiguration: " + e.message)
                            }
                        }
                    }
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
            if(result.package.unlinkFromSubscription(result.subscription, true)){
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
            }
            result.issueEntitlementEnrichment = params.issueEntitlementEnrichment
            if(params.format || params.exportXLSX || params.exportKBart) {
                result.max = 10000
                result.offset = 0
            }
            else {
                SwissKnife.setPaginationParams(result, params, (User) result.user)
            }
            boolean filterSet = false
            List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where subscription = :sub and ( pc.status is null or pc.status = :status ) order by ts desc",
                    [sub: result.subscription, status: RDStore.PENDING_CHANGE_PENDING])
            result.pendingChanges = pendingChanges
            String base_qry
            Map<String,Object> qry_params = [subscription: result.subscription]
            Date date_filter
            if (params.asAt && params.asAt.length() > 0) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                date_filter = sdf.parse(params.asAt)
                result.as_at_date = date_filter
                result.editable = false
            }
            if (params.filter) {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                if (date_filter) {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                    // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                    // qry_params.add(deleted_ie);
                    base_qry += "and ( ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.tipp.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null) OR ( ie.subscription.hasPerpetualAccess = true ) ) ) "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }
                base_qry += "and ( ( lower(ie.tipp.sortName) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = ie.tipp.id and ident.value like :identifier ) ) ) "
                qry_params.title = "%${params.filter.trim().toLowerCase()}%"
                qry_params.identifier = "%${params.filter}%"
                filterSet = true
            }
            else {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                /*if (params.mode != 'advanced') {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                    base_qry += " and ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null)  or (ie.subscription.hasPerpetualAccess = true) ) ) "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }*/
            }
            if(params.mode != 'advanced') {
                base_qry += " and ie.status = :current "
                qry_params.current = RDStore.TIPP_STATUS_CURRENT
            }
            else {
                base_qry += " and ie.status != :deleted "
                qry_params.deleted = RDStore.TIPP_STATUS_DELETED
            }
            base_qry += " and ie.acceptStatus = :ieAcceptStatus "
            qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            if (params.pkgfilter && (params.pkgfilter != '')) {
                base_qry += " and ie.tipp.pkg.id = :pkgId "
                qry_params.pkgId = Long.parseLong(params.pkgfilter)
                filterSet = true
            }
            if (params.titleGroup && (params.titleGroup != '')) {
                base_qry += " and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup.id = :titleGroup and iegi.ie = ie) "
                qry_params.titleGroup = Long.parseLong(params.titleGroup)
            }
            if(params.seriesNames) {
                base_qry += " and lower(ie.tipp.seriesName) like :seriesNames "
                qry_params.seriesNames = "%${params.seriesNames.trim().toLowerCase()}%"
                filterSet = true
            }
            if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
                base_qry += " and lower(ie.tipp.subjectReference) in (:subject_references)"
                qry_params.subject_references = params.list('subject_references').collect { ""+it.toLowerCase()+"" }
                filterSet = true
            }
            if (params.series_names && params.series_names != "" && params.list('series_names')) {
                base_qry += " and lower(ie.tipp.seriesName) in (:series_names)"
                qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
                filterSet = true
            }
            if ((params.sort != null) && (params.sort.length() > 0)) {
                if(params.sort == 'startDate')
                    base_qry += "order by ic.startDate ${params.order}, lower(ie.tipp.sortName) asc "
                else if(params.sort == 'endDate')
                    base_qry += "order by ic.endDate ${params.order}, lower(ie.tipp.sortName) asc "
                else
                    base_qry += "order by ie.${params.sort} ${params.order} "
            }
            else {
                base_qry += "order by lower(ie.tipp.name) asc"
            }
            result.filterSet = filterSet
            Set<IssueEntitlement> entitlements = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params)
            if(params.kbartPreselect) {
                MultipartFile kbartFile = params.kbartPreselect
                InputStream stream = kbartFile.getInputStream()
                List issueEntitlements = entitlements.toList()
                result.enrichmentProcess = subscriptionService.issueEntitlementEnrichment(stream, issueEntitlements, (params.uploadCoverageDates == 'on'), (params.uploadPriceInfo == 'on'))
                params.remove("kbartPreselect")
                params.remove("uploadCoverageDates")
                params.remove("uploadPriceInfo")
            }
            result.subjects = subscriptionService.getSubjects(entitlements.collect { IssueEntitlement ie -> ie.tipp.id})
            result.seriesNames = subscriptionService.getSeriesNames(entitlements.collect { IssueEntitlement ie -> ie.tipp.id})
            if(result.subscription.ieGroups.size() > 0) {
                result.num_ies = subscriptionService.getIssueEntitlementsWithFilter(result.subscription, [offset: 0, max: 5000]).size()
            }
            result.num_sub_rows = entitlements.size()
            result.entitlements = entitlements.drop(result.offset).take(result.max)
            Set<SubscriptionPackage> deletedSPs = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.pkg.packageStatus == RDStore.PACKAGE_STATUS_DELETED}
            if(deletedSPs) {
                result.deletedSPs = []
                ApiSource source = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI,true)
                deletedSPs.each { sp ->
                    result.deletedSPs << [name:sp.pkg.name,link:"${source.editUrl}/gokb/public/packageContent/?id=${sp.pkg.gokbId}"]
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
        if(!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            List<Package> pkgList = result.subscription.packages.collect { SubscriptionPackage sp -> sp.pkg }
            List<Map<String,Object>> entitlementDiffs = []
            List<PendingChange> pkgChanges = PendingChange.executeQuery('select pc from PendingChange pc where pc.status = :pkgHistory and pc.pkg in (:pkgList)',[pkgList:pkgList])
            List<PendingChange> tippChanges = PendingChange.executeQuery('select pc from PendingChange pc where pc.status = :pkgHistory and pc.tipp.pkg in (:pkgList) and pc.msgToken = :updated',[pkgList:pkgList,updated:PendingChangeConfiguration.TITLE_UPDATED])
            List<PendingChange> covChanges = PendingChange.executeQuery('select pc from PendingChange pc where pc.status = :pkgHistory and pc.tippCoverage.tipp.pkg in (:pkgList) and pc.msgToken = :updated',[pkgList:pkgList,updated:PendingChangeConfiguration.COVERAGE_UPDATED])
            /*
                get all changes which has occurred
                list all changes in general
                offer for each update for which prompt is the setting (explicitely or by missing setting) a change map with parameter links to accept the change
                listing must be restricted to subscription package! explicite diff listing only for updates
                creations and deletions have to be indicated only by numbers (number of tipps / tippCoverages vs. issue entitlements / ieCoverages)
            */
            List<IssueEntitlement> iesConcerned = IssueEntitlement.findAllByTippInListAndStatusNotEqual(tippChanges.collect { PendingChange pc -> pc.tipp },RDStore.TIPP_STATUS_DELETED)
            List<IssueEntitlement> ieCovsConcerned = IssueEntitlement.executeQuery('select ie from IssueEntitlementCoverage ieCov join ieCov.issueEntitlement ie join ie.tipp tipp where tipp in (:tipps) and ie.status != :deleted',[tipps:tippChanges.collect { PendingChange pc -> pc.tipp },daleted:RDStore.TIPP_STATUS_DELETED])
            tippChanges.each { PendingChange change ->
                IssueEntitlement issueEntitlement = iesConcerned.find { IssueEntitlement ie -> ie.tipp == change.tipp }
                switch(change.msgToken) {
                    case PendingChangeConfiguration.TITLE_UPDATED:
                        if(issueEntitlement[change.prop] != change.tipp[change.prop])
                            entitlementDiffs << [prop:change.prop,oldValue:issueEntitlement[change.prop],newValue:change.tipp[change.prop],tipp:change.tipp,msgToken:change.msgToken]
                        break
                }
            }
            result
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
            }
            if(params.exportXLSX || params.exportKBart || params.format) {
                result.max = 10000
                result.offset = 0
            }
            else {
                SwissKnife.setPaginationParams(result, params, (User) result.user)
            }
            RefdataValue tipp_current = RDStore.TIPP_STATUS_CURRENT
            RefdataValue ie_deleted = RDStore.TIPP_STATUS_DELETED
            RefdataValue ie_current = RDStore.TIPP_STATUS_CURRENT
            List<TitleInstancePackagePlatform> tipps = []
            List errorList = []
            boolean filterSet = false
            EhcacheWrapper checkedCache = contextService.getCache("/subscription/addEntitlements/${params.id}", contextService.USER_SCOPE)
            Map<TitleInstancePackagePlatform,IssueEntitlement> addedTipps = [:]
            result.subscription.issueEntitlements.each { ie ->
                if(ie instanceof IssueEntitlement && ie.status != ie_deleted)
                    addedTipps[ie.tipp] = ie
            }
            // We need all issue entitlements from the parent subscription where no row exists in the current subscription for that item.
            String basequery
            Map<String,Object> qry_params = [subscription:result.subscription,tippStatus:tipp_current,issueEntitlementStatus:ie_current]
            if (params.filter) {
                log.debug("Filtering....");
                basequery = "select tipp from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and tipp.status = :tippStatus and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) ) and ( ( lower(tipp.name) like :title ) OR ( exists ( select ident from Identifier ident where ident.tipp.id = tipp.id and ident.value like :idVal ) ) ) "
                qry_params.title = "%${params.filter.trim().toLowerCase()}%"
                qry_params.idVal = "%${params.filter}%"
                filterSet = true
            }
            else {
                basequery = "select tipp from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and tipp.status = :tippStatus and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) )"
            }
            if (params.endsAfter && params.endsAfter.length() > 0) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                Date d = sdf.parse(params.endsAfter)
                basequery += " and (select max(tc.endDate) from TIPPCoverage tc where tc.tipp = tipp) >= :endDate"
                qry_params.endDate = d
                filterSet = true
            }
            if (params.startsBefore && params.startsBefore.length() > 0) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                Date d = sdf.parse(params.startsBefore)
                basequery += " and (select min(tc.startDate) from TIPPCoverage tc where tc.tipp = tipp) <= :startDate"
                qry_params.startDate = d
                filterSet = true
            }
            if (params.pkgfilter && (params.pkgfilter != '')) {
                basequery += " and tipp.pkg.gokbId = :pkg "
                qry_params.pkg = params.pkgfilter
                filterSet = true
            }
            if ((params.sort != null) && (params.sort.length() > 0)) {
                basequery += " order by tipp.${params.sort} ${params.order} "
                filterSet = true
            }
            else {
                basequery += " order by tipp.sortName asc "
            }
            result.filterSet = filterSet
            tipps.addAll(TitleInstancePackagePlatform.executeQuery(basequery, qry_params))
            result.num_tipp_rows = tipps.size()
            result.tipps = tipps.drop(result.offset).take(result.max)
            Map identifiers = [zdbIds:[],onlineIds:[],printIds:[],unidentified:[]]
            Map<String,Map> issueEntitlementOverwrite = [:]
            result.issueEntitlementOverwrite = [:]
            if(params.kbartPreselect && !params.pagination) {
                MultipartFile kbartFile = params.kbartPreselect
                identifiers.filename = kbartFile.originalFilename
                InputStream stream = kbartFile.getInputStream()
                ArrayList<String> rows = stream.text.split('\n')
                Map<String,Integer> colMap = [publicationTitleCol:-1,zdbCol:-1, onlineIdentifierCol:-1, printIdentifierCol:-1, dateFirstInPrintCol:-1, dateFirstOnlineCol:-1,
                                              startDateCol:-1, startVolumeCol:-1, startIssueCol:-1,
                                              endDateCol:-1, endVolumeCol:-1, endIssueCol:-1,
                                              accessStartDateCol:-1, accessEndDateCol:-1, coverageDepthCol:-1, coverageNotesCol:-1, embargoCol:-1,
                                              listPriceCol:-1, listCurrencyCol:-1, listPriceEurCol:-1, listPriceUsdCol:-1, listPriceGbpCol:-1, localPriceCol:-1, localCurrencyCol:-1, priceDateCol:-1]
                boolean isUniqueListpriceColumn = false
                //read off first line of KBART file
                rows[0].split('\t').eachWithIndex { String headerCol, int c ->
                    switch(headerCol.toLowerCase().trim()) {
                        case "zdb_id": colMap.zdbCol = c
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
                if((colMap.listPriceCol > -1 && colMap.listCurrencyCol > -1) && (colMap.listPriceEurCol > -1 || colMap.listPriceGbpCol > -1 || colMap.listPriceUsdCol > -1)) {
                    errorList.add(messageSource.getMessage('subscription.details.addEntitlements.duplicatePriceColumn',null,locale))
                }
                else if((colMap.listPriceEurCol > -1 && colMap.listPriceUsdCol > -1) && (colMap.listPriceEurCol > -1 && colMap.listPriceGbpCol > -1) && (colMap.listPriceUsdCol > -1 && colMap.listPriceGbpCol > -1 )) {
                    errorList.add(messageSource.getMessage('subscription.details.addEntitlements.duplicatePriceColumn',null,locale))
                }
                else isUniqueListpriceColumn = true
                //after having read off the header row, pop the first row
                rows.remove(0)
                //now, assemble the identifiers available to highlight
                Map<String, IdentifierNamespace> namespaces = [zdb  :IdentifierNamespace.findByNs('zdb'),
                                                               eissn:IdentifierNamespace.findByNs('eissn'), isbn:IdentifierNamespace.findByNs('isbn'),
                                                               issn :IdentifierNamespace.findByNs('issn'), pisbn:IdentifierNamespace.findByNs('pisbn')]
                rows.eachWithIndex { row, int i ->
                    log.debug("now processing entitlement ${i}")
                    Map<String,Object> ieCandidate = [:]
                    ArrayList<String> cols = row.split('\t')
                    Map<String,Object> idCandidate
                    String ieCandIdentifier
                    if(colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                        identifiers.zdbIds.add(cols[colMap.zdbCol])
                        idCandidate = [namespaces:[namespaces.zdb],value:cols[colMap.zdbCol]]
                        if(issueEntitlementOverwrite[cols[colMap.zdbCol]])
                            ieCandidate = issueEntitlementOverwrite[cols[colMap.zdbCol]]
                        else ieCandIdentifier = cols[colMap.zdbCol]
                    }
                    if(colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                        identifiers.onlineIds.add(cols[colMap.onlineIdentifierCol])
                        idCandidate = [namespaces:[namespaces.eissn,namespaces.isbn],value:cols[colMap.onlineIdentifierCol]]
                        if(ieCandIdentifier == null && !issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                            ieCandIdentifier = cols[colMap.onlineIdentifierCol]
                        else if(issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]])
                            ieCandidate = issueEntitlementOverwrite[cols[colMap.onlineIdentifierCol]]
                    }
                    if(colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                        identifiers.printIds.add(cols[colMap.printIdentifierCol])
                        idCandidate = [namespaces:[namespaces.issn,namespaces.pisbn],value:cols[colMap.printIdentifierCol]]
                        if(ieCandIdentifier == null && !issueEntitlementOverwrite[cols[colMap.printIdentifierCol]])
                            ieCandIdentifier = cols[colMap.printIdentifierCol]
                        else if(issueEntitlementOverwrite[cols[colMap.printIdentifierCol]])
                            ieCandidate = issueEntitlementOverwrite[cols[colMap.printIdentifierCol]]
                    }
                    if(((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                            ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                            ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
                        identifiers.unidentified.add('"'+cols[0]+'"')
                    }
                    else {
                        //make checks ...
                        //is title in LAS:eR?
                        Identifier id = Identifier.findByValueAndNsInList(idCandidate.value,idCandidate.namespaces)
                        if(id && id.ti) {
                            //is title already added?
                            if(addedTipps.get(id.ti)) {
                                errorList.add("${cols[colMap.publicationTitleCol]}&#9;${cols[colMap.zdbCol] && colMap.zdbCol ? cols[colMap.zdbCol] : " "}&#9;${cols[colMap.onlineIdentifierCol] && colMap.onlineIndentifierCol > -1 ? cols[colMap.onlineIdentifierCol] : " "}&#9;${cols[colMap.printIdentifierCol] && colMap.printIdentifierCol > -1 ? cols[colMap.printIdentifierCol] : " "}&#9;${messageSource.getMessage('subscription.details.addEntitlements.titleAlreadyAdded',null,locale)}")
                            }
                        }
                        else if(!id) {
                            errorList.add("${cols[colMap.publicationTitleCol]}&#9;${cols[colMap.zdbCol] && colMap.zdbCol > -1 ? cols[colMap.zdbCol] : " "}&#9;${cols[colMap.onlineIdentifierCol] && colMap.onlineIndentifierCol > -1 ? cols[colMap.onlineIdentifierCol] : " "}&#9;${cols[colMap.printIdentifierCol] && colMap.printIdentifierCol > -1 ? cols[colMap.printIdentifierCol] : " "}&#9;${messageSource.getMessage('subscription.details.addEntitlements.titleNotInERMS',null,locale)}")
                        }
                    }
                    List<Map> ieCoverages
                    if(ieCandidate.coverages)
                        ieCoverages = ieCandidate.coverages
                    else ieCoverages = []
                    Map covStmt = [:]
                    colMap.each { String colName, int colNo ->
                        if(colNo > -1 && cols[colNo]) {
                            String cellEntry = cols[colNo].trim()
                            if(result.preselectCoverageDates) {
                                switch(colName) {
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
                            if(result.uploadPriceInfo && isUniqueListpriceColumn) {
                                try {
                                    switch(colName) {
                                        case "listPriceCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                            break
                                        case "listCurrencyCol": ieCandidate.listCurrency = RefdataValue.getByValueAndCategory(cellEntry, RDConstants.CURRENCY)?.value
                                            break
                                        case "listPriceEurCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                            ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("EUR",RDConstants.CURRENCY).value
                                            break
                                        case "listPriceUsdCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                            ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("USD",RDConstants.CURRENCY).value
                                            break
                                        case "listPriceGbpCol": ieCandidate.listPrice = escapeService.parseFinancialValue(cellEntry)
                                            ieCandidate.listCurrency = RefdataValue.getByValueAndCategory("GBP",RDConstants.CURRENCY).value
                                            break
                                        case "localPriceCol": ieCandidate.localPrice = escapeService.parseFinancialValue(cellEntry)
                                            break
                                        case "localCurrencyCol": ieCandidate.localCurrency = RefdataValue.getByValueAndCategory(cellEntry,RDConstants.CURRENCY)?.value
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
                    if(ieCandIdentifier) {
                        ieCoverages.add(covStmt)
                        ieCandidate.coverages = ieCoverages
                        issueEntitlementOverwrite[ieCandIdentifier] = ieCandidate
                    }
                }
                result.identifiers = identifiers
                params.remove("kbartPreselect")
            }
            if(!params.pagination) {
                result.checked = [:]
                tipps.each { tipp ->
                    String serial
                    String electronicSerial
                    String checked = ""
                    if(tipp.titleType.contains('Book')) {
                        serial = tipp.getIdentifierValue('pISBN')
                        electronicSerial = tipp.getIdentifierValue('ISBN')
                    }
                    else if(tipp.titleType == "Journal") {
                        serial = tipp.getIdentifierValue('ISSN')
                        electronicSerial = tipp.getIdentifierValue('eISSN')
                    }
                    if(result.identifiers?.zdbIds?.indexOf(tipp.getIdentifierValue('zdb')) > -1) {
                        checked = "checked"
                        result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[tipp.getIdentifierValue('zdb')]
                    }
                    else if(result.identifiers?.onlineIds?.indexOf(electronicSerial) > -1) {
                        checked = "checked"
                        result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[electronicSerial]
                    }
                    else if(result.identifiers?.printIds?.indexOf(serial) > -1) {
                        checked = "checked"
                        result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[serial]
                    }
                    result.checked[tipp.gokbId] = checked
                }
                if(result.identifiers && result.identifiers.unidentified.size() > 0) {
                    String unidentifiedTitles = result.identifiers.unidentified.join(", ")
                    String escapedFileName
                    try {
                        escapedFileName = StringEscapeCategory.encodeAsHtml(result.identifiers.filename)
                    }
                    catch (Exception | Error e) {
                        log.error(e.printStackTrace())
                        escapedFileName = result.identifiers.filename
                    }
                    errorList.add(messageSource.getMessage('subscription.details.addEntitlements.unidentified',[escapedFileName, unidentifiedTitles],locale))
                }
                checkedCache.put('checked',result.checked)
                checkedCache.put('issueEntitlementCandidates',result.issueEntitlementOverwrite)
            }
            else {
                result.checked = checkedCache.get('checked')
                result.issueEntitlementOverwrite = checkedCache.get('issueEntitlementCandidates')
            }
            if(errorList)
                result.error = "<pre style='font-family:Lato,Arial,Helvetica,sans-serif;'>"+errorList.join("\n")+"</pre>"
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
                    checked.each { k,v ->
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

                            }
                            catch (EntitlementCreationException e) {
                                result.error = e.getMessage()
                            }
                        }
                    }
                    Object[] args = [addTitlesCount]
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.titlesAddToSub',args,locale)
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
                        if (params.bulk_medium.trim().length() > 0) {
                            RefdataValue selected_refdata = (RefdataValue) genericOIDService.resolveOID(params.bulk_medium.trim())
                            log.debug("Selected medium is ${selected_refdata}");
                            ie.medium = selected_refdata
                        }
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
        Map<String,Object> result = getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
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
                        if (subscriptionService.addEntitlement(result.subscription, tipp.gokbId, ie, (ie.priceItem != null), RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION)) {
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
            null
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

            result.currentTitlesCounts = IssueEntitlement.findAllBySubscriptionAndStatusAndAcceptStatus(result.subscription, RDStore.TIPP_STATUS_CURRENT, RDStore.IE_ACCEPT_STATUS_FIXED).size()

            if(result.contextCustomerType == "ORG_CONSORTIUM") {
                if(result.subscription.instanceOf){
                    result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                            [sub: result.subscription.instanceOf,
                             org: result.subscription.getSubscriber(),
                             invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                }else{
                    result.currentSurveysCounts = SurveyConfig.findAllBySubscription(result.subscription).size()
                }
                result.currentMembersCounts =  Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',[parent: result.subscription, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]).size()
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

}
