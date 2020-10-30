package de.laser.ctrl

import com.k_int.kbplus.ExecutorWrapperService
import com.k_int.kbplus.FactService
import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.GlobalSourceSyncService
import com.k_int.kbplus.PackageService
import com.k_int.kbplus.PendingChangeService
import de.laser.AccessService
import de.laser.AddressbookService
import de.laser.ApiSource
import de.laser.AuditConfig
import de.laser.ContextService
import de.laser.EscapeService
import de.laser.FilterService
import de.laser.FinanceService
import de.laser.FormService
import de.laser.GokbService
import de.laser.GlobalRecordSource
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.IssueEntitlementCoverage
import de.laser.IssueEntitlementGroup
import de.laser.IssueEntitlementGroupItem
import de.laser.License
import de.laser.Links
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.OrgSetting
import de.laser.OrgTypeService
import de.laser.Package
import de.laser.PendingChange
import de.laser.PendingChangeConfiguration
import de.laser.Person
import de.laser.Platform
import de.laser.PropertyService
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionController
import de.laser.SubscriptionPackage
import de.laser.SubscriptionService
import de.laser.SurveyConfig
import de.laser.TaskService
import de.laser.TitleInstancePackagePlatform
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.helper.DateUtil
import de.laser.helper.EhcacheWrapper
import de.laser.helper.ProfilerUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
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
import groovy.util.slurpersupport.GPathResult
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
    OrgTypeService orgTypeService
    FactService factService
    SubscriptionService subscriptionService
    PropertyService propertyService
    ContextService contextService
    EscapeService escapeService
    ExecutorService executorService
    PackageService packageService
    GokbService gokbService
    GlobalSourceSyncService globalSourceSyncService
    LinksGenerationService linksGenerationService
    ExecutorWrapperService executorWrapperService
    PendingChangeService pendingChangeService
    GenericOIDService genericOIDService
    MessageSource messageSource

    //-------------------------------------- general or ungroupable section -------------------------------------------

    Map<String,Object> show(SubscriptionController controller, GrailsParameterMap params) {
        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('1')
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            pu.setBenchmark('this-n-that')
            if (result.institution) {
                result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
            }
            pu.setBenchmark('links')
            result.links = linksGenerationService.getSourcesAndDestinations(result.subscription,result.user)
            pu.setBenchmark('pending changes')
            // ---- pendingChanges : start
            if (executorWrapperService.hasRunningProcess(result.subscription)) {
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
            }
            // ---- pendingChanges : end
            pu.setBenchmark('tasks')
            // TODO: experimental asynchronous task
            //def task_tasks = task {
            // tasks
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.subscription)
            Map<String,Object> preCon = taskService.getPreconditionsWithoutTargets(result.contextOrg)
            result << preCon
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.subscription.orgRelations?.each { or ->
                if (!(or.org.id == result.contextOrg.id) && !(or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it.org.sortname }
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
            pu.setBenchmark('usage')
            // TODO: experimental asynchronous task
            //def task_usage = task {
            // usage
            List suppliers = Platform.executeQuery('select distinct(plat.id) from IssueEntitlement ie join ie.tipp tipp join tipp.platform plat where ie.subscription = :sub',[sub:result.subscription])
            if (suppliers.size() > 1) {
                log.debug('Found different content platforms for this subscription, cannot show usage')
            }
            else {
                Long supplier_id = suppliers[0]
                PlatformProperty platform = PlatformProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
                result.natStatSupplierId = platform?.stringValue ?: null
                result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
                if (result.institutional_usage_identifier) {
                        def fsresult = factService.generateUsageData(result.institution.id, supplier_id, result.subscription)
                        def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(result.institution.id, supplier_id, result.subscription)
                        def holdingTypes = result.subscription.getHoldingTypes() ?: null
                        if (!holdingTypes) {
                            log.debug('No types found, maybe there are no issue entitlements linked to subscription')
                        } else if (holdingTypes.size() > 1) {
                            log.info('Different content type for this license, cannot calculate Cost Per Use.')
                        } else if (!fsLicenseResult.isEmpty() && result.subscription.startDate) {
                            def existingReportMetrics = fsLicenseResult.y_axis_labels*.split(':')*.last()
                            def costPerUseMetricValuePair = factService.getTotalCostPerUse(result.subscription, holdingTypes.first(), existingReportMetrics)
                            if (costPerUseMetricValuePair) {
                                result.costPerUseMetric = costPerUseMetricValuePair[0]
                                result.totalCostPerUse = costPerUseMetricValuePair[1]
                                result.currencyCode = NumberFormat.getCurrencyInstance().getCurrency().currencyCode
                            }
                        }
                        result.statsWibid = result.institution.getIdentifierByType('wibid')?.value
                        if(result.statsWibid && result.natStatSupplierId) {
                            result.usageMode = accessService.checkPerm("ORG_CONSORTIUM") ? 'package' : 'institution'
                            result.usage = fsresult?.usage
                            result.missingMonths = fsresult?.missingMonths
                            result.missingSubscriptionMonths = fsLicenseResult?.missingMonths
                            result.x_axis_labels = fsresult?.x_axis_labels
                            result.y_axis_labels = fsresult?.y_axis_labels
                            result.lusage = fsLicenseResult?.usage
                            result.lastUsagePeriodForReportType = factService.getLastUsagePeriodForReportType(result.natStatSupplierId, result.statsWibid)
                            result.l_x_axis_labels = fsLicenseResult?.x_axis_labelsresult.l_y_axis_labels = fsLicenseResult?.y_axis_labels
                        }
                    }
            }
            //}
            pu.setBenchmark('costs')
            //cost items
            //params.forExport = true
            LinkedHashMap costItems = financeService.getCostItemsForSubscription(params, financeService.setResultGenerics(params))
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
            result.availableProviderList = orgTypeService.getOrgsForTypeProvider().minus(
                    OrgRole.executeQuery(
                            "select o from OrgRole oo join oo.org o where oo.sub.id = :sub and oo.roleType.value = 'Provider'",
                            [sub: result.subscription.id]
                    ))
            result.existingProviderIdList = []
            // performance problems: orgTypeService.getCurrentProviders(contextService.getOrg()).collect { it -> it.id }
            result.availableAgencyList = orgTypeService.getOrgsForTypeAgency().minus(
                    OrgRole.executeQuery(
                            "select o from OrgRole oo join oo.org o where oo.sub.id = :sub and oo.roleType.value = 'Agency'",
                            [sub: result.subscription.id]
                    ))
            result.existingAgencyIdList = []
            // performance problems: orgTypeService.getCurrentAgencies(contextService.getOrg()).collect { it -> it.id }
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

    Map<String,Object> tasks(SubscriptionController controller,GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
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

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    Map<String,Object> emptySubscription(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if(result.editable) {
            Calendar cal = GregorianCalendar.getInstance()
            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
        Map<String, Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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

            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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

    Map<String,Object> notes(SubscriptionController controller) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else [result:result,status:STATUS_OK]
    }

    Map<String,Object> documents(SubscriptionController controller) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else [result:result,status:STATUS_OK]
    }

    //--------------------------------- consortia members section ----------------------------------------------

    Map<String,Object> members(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_COLLECTIVE]
        result.validSubChilds = Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parent:result.subscription,subscriberRoleTypes:subscriberRoleTypes])

        ArrayList<Long> filteredOrgIds = controller.getOrgIdsForFilter()
        result.filteredSubChilds = []
        result.validSubChilds.each { Subscription sub ->
            List<Org> subscr = sub.getAllSubscribers()
            Set<Org> filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                if (params.subRunTimeMultiYear || params.subRunTime) {

                    if (params.subRunTimeMultiYear && !params.subRunTime) {
                        if(sub.isMultiYear) {
                            result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                        }
                    }else if (!params.subRunTimeMultiYear && params.subRunTime){
                        if(!sub.isMultiYear) {
                            result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                        }
                    }
                    else {
                        result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                    }
                }
                else {
                    result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                }
            }
        }
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                RefdataValue subStatus = RefdataValue.get(params.subStatus) ?: RDStore.SUBSCRIPTION_CURRENT
                RefdataValue role_sub       = RDStore.OR_SUBSCRIBER_CONS
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
                        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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

    //--------------------------------------- survey section -------------------------------------------

    Map<String,Object> surveys(SubscriptionController controller) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            result.surveys = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                    [sub: result.subscription.instanceOf,
                     org: result.contextOrg,
                     invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]])
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> surveysConsortia(SubscriptionController controller) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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
            params.sort = "name"
            //to be deployed in parallel thread
            if(params.addUUID) {
                String pkgUUID = params.addUUID
                GlobalRecordSource source = GlobalRecordSource.findByUri("${params.source}/gokb/oai/packages")
                log.debug("linkPackage. Global Record Source URL: " +source.baseUrl)
                globalSourceSyncService.source = source
                String addType = params.addType
                GPathResult packageRecord = globalSourceSyncService.fetchRecord(source.uri,'packages',[verb:'GetRecord', metadataPrefix:'gokb', identifier:params.addUUID])
                if(packageRecord && packageRecord.record?.header?.status?.text() != 'deleted') {
                    result.packageName = packageRecord.record.metadata.gokb.package.name
                    if(!Package.findByGokbId(pkgUUID)) {
                        executorService.execute({
                            Thread.currentThread().setName("PackageSync_"+result.subscription.id)
                            try {
                                globalSourceSyncService.defineMapFields()
                                globalSourceSyncService.updateNonPackageData(packageRecord.record.metadata.gokb.package)
                                globalSourceSyncService.createOrUpdatePackage(packageRecord.record.metadata.gokb.package)
                                Package pkgToLink = Package.findByGokbId(pkgUUID)
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
                            pkgToLink.addToSubscription(result.subscriptionInstance, true)
                        }
                        else if (addType == 'Without') {
                            pkgToLink.addToSubscription(result.subscriptionInstance, false)
                        }
                    }
                }
                else {
                    result.error = messageSource.getMessage('subscription.details.link.packageNotFound',null,locale)
                }
            }
            if (result.subscription.packages) {
                result.pkgs = []
                if (params.gokbApi) {
                    result.subscription.packages.each { sp ->
                        log.debug("Existing package ${sp.pkg.name} (Adding GOKb ID: ${sp.pkg.gokbId})")
                        result.pkgs.add(sp.pkg.gokbId)
                    }
                }
            } else {
                log.debug("Subscription has no linked packages yet")
            }
            result.max = params.max ? params.int('max') : result.user.getDefaultPageSizeAsInteger()
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
            }
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> unlinkPackage(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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
                    List<SubscriptionPackage> spChildSubs = SubscriptionPackage.findAllByPkgAndSubscriptionInList(result.package, childSubs)
                    String queryChildSubs = "select ie.id from IssueEntitlement ie, Package pkg where ie.subscription in (:sub) and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
                    Map<String,Object> queryParamChildSubs = [sub: childSubs, pkg_id: result.package.id]
                    int numOfPCsChildSubs = result.package.removePackagePendingChanges(childSubs.id, false)
                    int numOfIEsChildSubs = IssueEntitlement.executeQuery(queryChildSubs, queryParamChildSubs).size()
                    int numOfCIsChildSubs = CostItem.findAllBySubPkgInList(SubscriptionPackage.findAllBySubscriptionInListAndPkg(childSubs, result.package)).size()
                    conflictsList.addAll(packageService.listConflicts(result.packages,childSubs,numOfPCsChildSubs,numOfIEsChildSubs,numOfCIsChildSubs))
                }
            }
            result.conflict_list = conflictsList
            [result:result,status:STATUS_OK]
        }
    }

    //-------------------------------- issue entitlements holding --------------------------------------

    Map<String,Object> index(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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
                result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
                result.offset = params.offset ? Integer.parseInt(params.offset) : 0
            }
            boolean filterSet = false
            List<PendingChange> pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where subscription = :sub and ( pc.status is null or pc.status = :status ) order by ts desc",
                    [sub: result.subscription, status: RDStore.PENDING_CHANGE_PENDING])
            result.pendingChanges = pendingChanges
            String base_qry
            Map<String,Object> qry_params = [subscription: result.subscription]
            Date date_filter
            if (params.asAt && params.asAt.length() > 0) {
                SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
                base_qry += "and ( ( lower(ie.tipp.title.title) like :title ) or ( exists ( from Identifier ident where ident.ti.id = ie.tipp.title.id and ident.value like :identifier ) ) ) "
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
                base_qry += " and lower(ie.tipp.title.seriesName) like :seriesNames "
                qry_params.seriesNames = "%${params.seriesNames.trim().toLowerCase()}%"
                filterSet = true
            }
            if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
                base_qry += " and lower(ie.tipp.title.subjectReference) in (:subject_references)"
                qry_params.subject_references = params.list('subject_references').collect { ""+it.toLowerCase()+"" }
                filterSet = true
            }
            if (params.series_names && params.series_names != "" && params.list('series_names')) {
                base_qry += " and lower(ie.tipp.title.seriesName) in (:series_names)"
                qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
                filterSet = true
            }
            if ((params.sort != null) && (params.sort.length() > 0)) {
                if(params.sort == 'startDate')
                    base_qry += "order by ic.startDate ${params.order}, lower(ie.tipp.title.title) asc "
                else if(params.sort == 'endDate')
                    base_qry += "order by ic.endDate ${params.order}, lower(ie.tipp.title.title) asc "
                else
                    base_qry += "order by ie.${params.sort} ${params.order} "
            }
            else {
                base_qry += "order by lower(ie.tipp.title.title) asc"
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
            result.subjects = subscriptionService.getSubjects(entitlements.collect { IssueEntitlement ie -> ie.tipp.title.id})
            result.seriesNames = subscriptionService.getSeriesNames(entitlements.collect { IssueEntitlement ie -> ie.tipp.title.id})
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
                    result.deletedSPs << [name:sp.pkg.name,link:"${source.editUrl}/gokb/resource/show/${sp.pkg.gokbId}"]
                }
            }
            if (executorWrapperService.hasRunningProcess(result.subscription)) {
                result.processingpc = true
            }
            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> addEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
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
                result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeAsInteger()
                result.offset = params.offset ? Integer.parseInt(params.offset) : 0
            }
            RefdataValue tipp_current = RDStore.TIPP_STATUS_CURRENT
            RefdataValue ie_deleted = RDStore.TIPP_STATUS_DELETED
            RefdataValue ie_current = RDStore.TIPP_STATUS_CURRENT
            List<TitleInstancePackagePlatform> tipps = []
            List errorList = []
            boolean filterSet = false
            EhcacheWrapper checkedCache = contextService.getCache("/subscription/addEntitlements/${params.id}", contextService.USER_SCOPE)
            Map<TitleInstance,IssueEntitlement> addedTipps = [:]
            result.subscription.issueEntitlements.each { ie ->
                if(ie instanceof IssueEntitlement && ie.status != ie_deleted)
                    addedTipps[ie.tipp.title] = ie
            }
            // We need all issue entitlements from the parent subscription where no row exists in the current subscription for that item.
            String basequery
            Map<String,Object> qry_params = [subscription:result.subscription,tippStatus:tipp_current,issueEntitlementStatus:ie_current]
            if (params.filter) {
                log.debug("Filtering....");
                basequery = "select tipp from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and tipp.status = :tippStatus and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) ) and ( ( lower(tipp.title.title) like :title ) OR ( exists ( select ident from Identifier ident where ident.ti.id = tipp.title.id and ident.value like :idVal ) ) ) "
                qry_params.title = "%${params.filter.trim().toLowerCase()}%"
                qry_params.idVal = "%${params.filter}%"
                filterSet = true
            }
            else {
                basequery = "select tipp from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and tipp.status = :tippStatus and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) )"
            }
            if (params.endsAfter && params.endsAfter.length() > 0) {
                SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
                Date d = sdf.parse(params.endsAfter)
                basequery += " and (select max(tc.endDate) from TIPPCoverage tc where tc.tipp = tipp) >= :endDate"
                qry_params.endDate = d
                filterSet = true
            }
            if (params.startsBefore && params.startsBefore.length() > 0) {
                SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
                basequery += " order by tipp.title.title asc "
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
                    if(tipp.title instanceof BookInstance) {
                        serial = tipp.title.getIdentifierValue('pISBN')
                        electronicSerial = tipp?.title?.getIdentifierValue('ISBN')
                    }
                    else if(tipp.title instanceof JournalInstance) {
                        serial = tipp?.title?.getIdentifierValue('ISSN')
                        electronicSerial = tipp?.title?.getIdentifierValue('eISSN')
                    }
                    if(result.identifiers?.zdbIds?.indexOf(tipp.title.getIdentifierValue('zdb')) > -1) {
                        checked = "checked"
                        result.issueEntitlementOverwrite[tipp.gokbId] = issueEntitlementOverwrite[tipp.title.getIdentifierValue('zdb')]
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

    Map<String,Object> processAddEntitlements(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
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
                    Object[] args = [TitleInstancePackagePlatform.findByGokbId(params.singleTitle)?.title?.title]
                    if(issueEntitlementCandidates?.get(params.singleTitle) || Boolean.valueOf(params.uploadPriceInfo))  {
                        if(subscriptionService.addEntitlement(result.subscriptionInstance, params.singleTitle, issueEntitlementCandidates?.get(params.singleTitle), Boolean.valueOf(params.uploadPriceInfo), ie_accept_status))
                            log.debug("Added tipp ${params.singleTitle} to sub ${result.subscriptionInstance.id} with issue entitlement overwrites")
                        result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                    }
                    else if(subscriptionService.addEntitlement(result.subscriptionInstance, params.singleTitle, null, false, ie_accept_status))
                        log.debug("Added tipp ${params.singleTitle} to sub ${result.subscriptionInstance.id}")
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            SimpleDateFormat formatter = DateUtil.getSDF_NoTime()
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
                            RefdataValue selected_refdata = genericOIDService.resolveOID(params.bulk_medium.trim())
                            log.debug("Selected medium is ${selected_refdata}");
                            ie.medium = selected_refdata
                        }
                        if (params.titleGroup && (params.titleGroup.trim().length() > 0)) {
                            IssueEntitlementGroup entitlementGroup = IssueEntitlementGroup.get(Long.parseLong(params.titleGroup))
                            if(entitlementGroup && !IssueEntitlementGroupItem.findByIeGroupAndIe(entitlementGroup, ie)){
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
        Map<String, Object> result = controller.setResultGenericsAndCheckAccess(accessService.CHECK_VIEW_AND_EDIT)
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
                        if(issueEntitlementGroup && !IssueEntitlementGroupItem.findByIeAndIeGroup(result.ie, issueEntitlementGroup)) {
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
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(accessService.CHECK_VIEW_AND_EDIT)
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

    Map<String,Object> renewEntitlementsWithSurvey(GrailsParameterMap params) {
        [result:result,status:STATUS_OK]
    }

    //--------------------------------------------- admin section -------------------------------------------------

    Map<String,Object> pendingChanges(SubscriptionController controller) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
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

    ArrayList<Long> getOrgIdsForFilter(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        params.remove("max")
        params.remove("offset")
        if (accessService.checkPerm("ORG_CONSORTIUM"))
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        else if (accessService.checkPerm("ORG_INST_COLLECTIVE"))
            params.comboType = RDStore.COMBO_TYPE_DEPARTMENT.value
        Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)

        if (params.filterPropDef) {
            fsq = propertyService.evalFilterQuery(params, fsq.query, 'o', fsq.queryParams)
        }
        fsq.query = fsq.query.replaceFirst("select o from ", "select o.id from ")
        Org.executeQuery(fsq.query, fsq.queryParams, params)
    }

}
