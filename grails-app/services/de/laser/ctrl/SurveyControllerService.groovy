package de.laser.ctrl

import de.laser.AccessService
import de.laser.AddressbookService
import de.laser.AuditConfig
import de.laser.AuditService
import de.laser.CompareService
import de.laser.ComparisonService
import de.laser.ContextService
import de.laser.CopyElementsService
import de.laser.CustomerTypeService
import de.laser.DocContext
import de.laser.DocstoreService
import de.laser.EscapeService
import de.laser.ExportClickMeService
import de.laser.FilterService
import de.laser.FinanceService
import de.laser.GenericOIDService
import de.laser.GlobalService
import de.laser.GlobalSourceSyncService
import de.laser.GokbService
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlementGroup
import de.laser.License
import de.laser.Links
import de.laser.LinksGenerationService
import de.laser.Org
import de.laser.OrgRole
import de.laser.survey.SurveyPersonResult
import de.laser.utils.RandomUtils
import de.laser.wekb.Package
import de.laser.PackageService
import de.laser.PendingChange
import de.laser.PendingChangeConfiguration
import de.laser.PropertyService
import de.laser.wekb.Provider
import de.laser.wekb.ProviderRole
import de.laser.ProviderService
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.SubscriptionService
import de.laser.SubscriptionsQueryService
import de.laser.SurveyService
import de.laser.Task
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import de.laser.VendorService
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.finance.CostItem
import de.laser.finance.CostItemElementConfiguration
import de.laser.finance.Order
import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinitionGroup
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigPackage
import de.laser.survey.SurveyConfigProperties
import de.laser.SurveyController
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyLinks
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPackageResult
import de.laser.survey.SurveyResult
import de.laser.TaskService
import de.laser.auth.User
import de.laser.survey.SurveyUrl
import de.laser.survey.SurveyVendorResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.utils.SwissKnife
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.context.MessageSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartFile

import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService

@Transactional
class SurveyControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    AddressbookService addressbookService
    AuditService auditService
    CompareService compareService
    ComparisonService comparisonService
    ContextService contextService
    CopyElementsService copyElementsService
    CustomerTypeService customerTypeService
    DocstoreService docstoreService
    EscapeService escapeService
    ExecutorService executorService
    ExportClickMeService exportClickMeService
    GenericOIDService genericOIDService
    GlobalService globalService
    GlobalSourceSyncService globalSourceSyncService
    GokbService gokbService
    FilterService filterService
    FinanceControllerService financeControllerService
    FinanceService financeService
    LinksGenerationService linksGenerationService
    PackageService packageService
    PropertyService propertyService
    ProviderService providerService
    SubscriptionService subscriptionService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyControllerService surveyControllerService
    SurveyService surveyService
    TaskService taskService
    VendorService vendorService

    MessageSource messageSource


    /**
     * Shows the given survey's details
     * @return the survey details view
     */
    Map<String, Object> show(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.surveyLinksMessage = []

            if (SurveyLinks.executeQuery("from SurveyLinks where sourceSurvey = :surveyInfo and sourceSurvey.status = :startStatus and targetSurvey.status != sourceSurvey.status", [surveyInfo: result.surveyInfo, startStatus: RDStore.SURVEY_SURVEY_STARTED]).size() > 0) {
                result.surveyLinksMessage << messageSource.getMessage('surveyLinks.surveysNotStartet', null, result.locale)
            }

            if (SurveyLinks.executeQuery("from SurveyLinks where sourceSurvey = :surveyInfo and sourceSurvey.status = :startStatus and targetSurvey.status = sourceSurvey.status and targetSurvey.endDate != sourceSurvey.endDate", [surveyInfo: result.surveyInfo, startStatus: RDStore.SURVEY_SURVEY_STARTED]).size() > 0) {
                result.surveyLinksMessage << messageSource.getMessage('surveyLinks.surveysNotSameEndDate', null, result.locale)
            }

            result.navigation = surveyService.getConfigNavigation(result.surveyInfo, result.surveyConfig)

            if (result.surveyConfig.subscription) {

                // restrict visible for templates/links/orgLinksAsList
                result.visibleOrgRelations = []
                result.surveyConfig.subscription.orgRelations.each { OrgRole or ->
                    if (!(or.org.id == result.institution.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                        result.visibleOrgRelations << or
                    }
                }
                result.visibleOrgRelations.sort { it.org.sortname }

                result.subscription = result.surveyConfig.subscription ?: null

                result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                result.links = linksGenerationService.getSourcesAndDestinations(result.subscription, result.user)

                if (result.surveyConfig.subSurveyUseForTransfer) {
                    result.successorSubscriptionParent = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                    result.subscriptionParent = result.surveyConfig.subscription
                    Collection<AbstractPropertyWithCalculatedLastUpdated> props
                    props = result.subscriptionParent.propertySet.findAll { it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic)) }
                    if (result.successorSubscriptionParent) {
                        props += result.successorSubscriptionParent.propertySet.findAll { it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic)) }
                    }
                    result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
                }

            }
            result.tasks = taskService.getTasksByResponsibilityAndObject(result.user, result.surveyConfig)
            result.showSurveyPropertiesForOwer = true

            params.viewTab = params.viewTab ?: 'overview'

            if (params.commentTab) {
                result.commentTab = params.commentTab
            }else {
                if(!result.surveyConfig.subscription){
                    result.commentTab = 'commentForNewParticipants'
                }else {
                    result.commentTab = result.surveyConfig.getSurveyOrgsIDs().orgsWithSubIDs ? 'comment' : 'commentForNewParticipants'
                }
            }


            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Lists the titles subject of the given survey
     * @return a list view of the issue entitlements linked to the given survey
     */
    Map<String, Object> surveyTitles(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            SwissKnife.setPaginationParams(result, params, (User) result.user)

            if (result.subscription.packages) {
                // TODO: erms-5519 - fixed status - clearable filter still doesn't work
                // params.status = [RDStore.TIPP_STATUS_CURRENT.id]
                params.status = params.status ?: RDStore.TIPP_STATUS_CURRENT.id
                Map<String, Object> query = filterService.getTippQuery(params, result.subscription.packages.pkg)
                result.filterSet = query.filterSet
                List<Long> titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
                result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList.drop(result.offset).take(result.max), [sort: 'sortname']) : []
                result.num_tipp_rows = titlesList.size()

            } else {
                result.titlesList = []
                result.num_tipp_rows = 0
            }
            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Lists the participants of the given survey. The result may be filtered
     * @return the participant list for the given tab
     */
    Map<String, Object> surveyParticipants(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

            result.editable = (result.surveyInfo && result.surveyInfo.status.id != RDStore.SURVEY_IN_PROCESSING.id) ? false : result.editable

            result.participantsTotal = SurveyOrg.countBySurveyConfig(result.surveyConfig)

            if(params.subs){
                params.subs = Params.getLongList(params, 'subs').collect {Subscription.get(it)}
            }else{
                if((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))
                {
                    params.subs = params.subs ? Params.getLongList(params, 'subs').collect {Subscription.get(it)} : [result.subscription]
                }
            }

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery('select org '+ fsq.query, fsq.queryParams, params)

            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> addSurveyParticipants(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

            params.customerType = customerTypeService.getOrgInstRoles().id // ERMS-6009
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value

            if(params.subs){
                params.subs = Params.getLongList(params, 'subs').collect {Subscription.get(it)}
            }else{
                if((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))
                {
                    params.subs = params.subs ? Params.getLongList(params, 'subs').collect {Subscription.get(it)} : [result.subscription]
                }
            }

            GrailsParameterMap cloneParams = params.clone()
            cloneParams.removeAll { it.value != '' }
            cloneParams.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value

            FilterService.Result countFsr = filterService.getOrgComboQuery(cloneParams, result.institution as Org)
            if (countFsr.isFilterSet) {
                cloneParams.filterSet = true
            }

            String queryConsortiaMembersCount = countFsr.query.minus("select o ")
            queryConsortiaMembersCount = queryConsortiaMembersCount.split("order by")[0]
            result.consortiaMembersCount = Org.executeQuery("select count(*) " + queryConsortiaMembersCount, countFsr.queryParams)[0]

            FilterService.Result fsr = filterService.getOrgComboQuery(params, result.institution as Org)
            if (fsr.isFilterSet) {
                params.filterSet = true
            }

            String tmpQuery = "select o.id " + fsr.query.minus("select o ")
            List consortiaMemberIds = Org.executeQuery(tmpQuery, fsr.queryParams)

            if (params.filterPropDef && consortiaMemberIds) {
                Map<String, Object> efq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids) order by o.sortname", 'o', [oids: consortiaMemberIds])
                fsr.query = efq.query
                fsr.queryParams = efq.queryParams as Map<String, Object>
            }
            result.consortiaMembers = Org.executeQuery(fsr.query, fsr.queryParams, params)

            if (result.surveyConfig.pickAndChoose) {

                List orgs = subscriptionService.getValidSurveySubChildOrgs(result.surveyConfig.subscription)
                result.consortiaMembers = orgs ? result.consortiaMembers.findAll { (it.id in orgs.id) } : []
            }

            result.editable = (result.surveyInfo && result.surveyInfo.status.id != RDStore.SURVEY_IN_PROCESSING.id) ? false : result.editable

            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Lists the costs linked to the given survey, reflecting upcoming subscription costs
     * @return a list of costs linked to the survey
     */
    Map<String, Object> surveyCostItems(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.putAll(financeControllerService.getEditVars(result.institution))

            Map<Long, Object> orgConfigurations = [:]
            result.costItemElements.each { oc ->
                orgConfigurations.put(oc.costItemElement.id, oc.elementSign.id)
            }

            result.orgConfigurations = orgConfigurations as JSON

            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

            //result.editable = (result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

            if(params.subs){
                params.subs = Params.getLongList(params, 'subs').collect {Subscription.get(it)}
            }else{
                if((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))
                {
                    params.subs = params.subs ? Params.getLongList(params, 'subs').collect {Subscription.get(it)} : [result.subscription]
                }
            }

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery('select org '+ fsq.query, fsq.queryParams, params)

            if(params.selectedCostItemElementID){
                result.selectedCostItemElementID = Long.valueOf(params.selectedCostItemElementID)
            }else {
                List<RefdataValue> costItemElementsIds = CostItem.executeQuery('select ct.costItemElement.id from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null group by ct.costItemElement.id order by ct.costItemElement.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])
                if(costItemElementsIds.size() > 0){
                    if(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id in costItemElementsIds){
                        result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                    }else {
                        result.selectedCostItemElementID = costItemElementsIds[0]
                    }
                }else {
                    result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                }
            }



            result.countCostItems = CostItem.executeQuery('select count(*) from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null and ct.costItemElement = :costItemElement', [costItemElement: RefdataValue.get(result.selectedCostItemElementID), status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])[0]

            if (result.participants && (params.sortOnCostItemsDown || params.sortOnCostItemsUp) && !params.sort) {
                String orderByQuery = " order by c.costInBillingCurrency"

                if (params.sortOnCostItemsUp) {
                    result.sortOnCostItemsUp = true
                    orderByQuery = " order by c.costInBillingCurrency DESC"
                    params.remove('sortOnCostItemsUp')
                } else {
                    params.remove('sortOnCostItemsDown')
                }

                String query = "select c.surveyOrg.org from CostItem as c where c.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and c.owner = :owner and c.costItemStatus != :status and c.costItemElement.id = :costItemElement " + orderByQuery

                result.participants = CostItem.executeQuery(query, [surConfig: result.surveyConfig, owner: result.surveyInfo.owner, status: RDStore.COST_ITEM_DELETED, costItemElement: Long.valueOf(result.selectedCostItemElementID)])

            }

            if(result.surveyConfig.subscription){
                String queryCostItemSub = "select c from CostItem as c where c.pkg is null and c.sub in " +
                        "(select sub from Subscription sub join sub.orgRelations orgR where orgR.roleType in :roleTypes and sub.instanceOf = :instanceOfSub and orgR.org.id in (select surOrg.org from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig)) " +
                        "and c.owner = :owner and c.costItemStatus != :status and c.costItemElement is not null "

                result.costItemsByCostItemElementOfSubs = CostItem.executeQuery(queryCostItemSub, [surConfig: result.surveyConfig,
                                                                                                   owner: result.surveyInfo.owner,
                                                                                                   status: RDStore.COST_ITEM_DELETED,
                                                                                                   roleTypes : [RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_CONS],
                                                                                                   instanceOfSub: result.surveyConfig.subscription]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement }
            }

            if (params.selectedCostItemElementID) {
                params.remove('selectedCostItemElementID')
            }

            result.idSuffix = "surveyCostItemsBulk"

            String query = 'from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null'

            result.costItemsByCostItemElement = CostItem.executeQuery(query, [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement }

            SortedSet<RefdataValue> assignedCostItemElements = new TreeSet<RefdataValue>()
            assignedCostItemElements.addAll(CostItem.executeQuery('select ct.costItemElement '+query, [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.getI10n('value')})

            result.assignedCostItemElements = assignedCostItemElements

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Lists the costs linked to the given survey, reflecting upcoming subscription costs
     * @return a list of costs linked to the survey
     */
    Map<String, Object> surveyCostItemsPackages(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.putAll(financeControllerService.getEditVars(result.institution))

            Map<Long, Object> orgConfigurations = [:]
            result.costItemElements.each { oc ->
                orgConfigurations.put(oc.costItemElement.id, oc.elementSign.id)
            }

            result.orgConfigurations = orgConfigurations as JSON

            result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

            //result.editable = (result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

            if(params.subs){
                params.subs = Params.getLongList(params, 'subs').collect {Subscription.get(it)}
            }else{
                if((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))
                {
                    params.subs = params.subs ? Params.getLongList(params, 'subs').collect {Subscription.get(it)} : [result.subscription]
                }
            }

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery('select org '+ fsq.query, fsq.queryParams, params)

            if(params.selectedCostItemElementID){
                result.selectedCostItemElementID = Long.valueOf(params.selectedCostItemElementID)
            }else {
                List<RefdataValue> costItemElementsIds = CostItem.executeQuery('select ct.costItemElement.id from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null group by ct.costItemElement.id order by ct.costItemElement.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])
                if(costItemElementsIds.size() > 0){
                    if(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id in costItemElementsIds){
                        result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                    }else {
                        result.selectedCostItemElementID = costItemElementsIds[0]
                    }
                }else {
                    result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                }
            }

            result.selectedPackageID = params.selectedPackageID ? Long.valueOf(params.selectedPackageID) : SurveyConfigPackage.executeQuery('select scp.pkg.id from SurveyConfigPackage scp where scp.surveyConfig = :surConfig order by scp.pkg.name', [surConfig: result.surveyConfig])[0]


            if (result.participants && (params.sortOnCostItemsDown || params.sortOnCostItemsUp) && !params.sort) {
                String orderByQuery = " order by c.costInBillingCurrency"

                if (params.sortOnCostItemsUp) {
                    result.sortOnCostItemsUp = true
                    orderByQuery = " order by c.costInBillingCurrency DESC"
                    params.remove('sortOnCostItemsUp')
                } else {
                    params.remove('sortOnCostItemsDown')
                }

                String query = "select c.surveyOrg.org from CostItem as c where c.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and c.owner = :owner and c.costItemStatus != :status and c.costItemElement.id = :costItemElement and c.pkg.id = :pkg" + orderByQuery

                result.participants = CostItem.executeQuery(query, [surConfig: result.surveyConfig, owner: result.surveyInfo.owner, status: RDStore.COST_ITEM_DELETED, costItemElement: Long.valueOf(result.selectedCostItemElementID), pkg: Long.valueOf(result.selectedPackageID )])
            }

            if (params.selectedCostItemElementID) {
                params.remove('selectedCostItemElementID')
            }

            result.idSuffix = "surveyCostItemsBulk"

            String query = 'from CostItem ct where ct.pkg != null and ct.sub is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null'

            result.costItemsByPackages = []

            result.surveyConfig.surveyPackages.each{ SurveyConfigPackage surveyConfigPackage ->
                Map map = [:]
                map.pkg = surveyConfigPackage.pkg
                map.costItemsByCostItemElement = CostItem.executeQuery(query+ ' and ct.pkg = :pkg', [pkg: surveyConfigPackage.pkg, status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement}
                result.costItemsByPackages << map
            }

            result.costItemsByPackages= result.costItemsByPackages ? result.costItemsByPackages.sort{it.pkg.name} : []

            result.countCostItems = CostItem.executeQuery('select count(*) from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null and ct.costItemElement = :costItemElement and ct.pkg = :pkg', [pkg: Package.get(result.selectedPackageID), costItemElement: RefdataValue.get(result.selectedCostItemElementID), status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])[0]

            result.assignedPackages = CostItem.executeQuery('select ct.pkg.id '+query +' group by ct.pkg.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).collect {Package.get(it)}.sort {it.name}

            SortedSet<RefdataValue> assignedCostItemElements = new TreeSet<RefdataValue>()
            assignedCostItemElements.addAll(CostItem.executeQuery('select ct.costItemElement '+query, [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.getI10n('value')})
            result.assignedCostItemElements = assignedCostItemElements

            [result: result, status: STATUS_OK]
        }

    }

    Map<String,Object> surveyPackages(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            if(params.initial){
                params.remove('initial')
                result.initial = true
            }

            if(result.surveyConfig.surveyPackages){
                List uuidPkgs = SurveyConfigPackage.executeQuery("select scg.pkg.gokbId from SurveyConfigPackage scg where scg.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])
                params.uuids = uuidPkgs
                params.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                params.offset = params.offset ? Integer.parseInt(params.offset) : 0
                result.putAll(packageService.getWekbPackages(params))
            }else{
                result.records = []
                result.recordsCount = 0
            }
            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> linkSurveyPackage(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            params.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
            params.offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.putAll(packageService.getWekbPackages(params))

            result.uuidPkgs = SurveyConfigPackage.executeQuery("select scg.pkg.gokbId from SurveyConfigPackage scg where scg.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])

            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> processLinkSurveyPackage(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            if(params.addUUID) {
                Package pkg = Package.findByGokbId(params.addUUID)
                if(pkg) {
                    if(!SurveyConfigPackage.findByPkgAndSurveyConfig(pkg, result.surveyConfig)) {
                        SurveyConfigPackage surveyConfigPackage = new SurveyConfigPackage(surveyConfig: result.surveyConfig, pkg: pkg).save()
                    }
                }else {
                    pkg = packageService.createPackageWithWEKB(params.addUUID)
                    if(pkg)
                        SurveyConfigPackage surveyConfigPackage = new SurveyConfigPackage(surveyConfig: result.surveyConfig, pkg: pkg).save()
                }
                result.surveyConfig = result.surveyConfig.refresh()
                result.surveyPackagesCount = SurveyConfigPackage.executeQuery("select count(*) from SurveyConfigPackage where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]
                params.remove("addUUID")
            }else if(params.removeUUID) {
                Package pkg = Package.findByGokbId(params.removeUUID)
                if(pkg) {
                    SurveyConfigPackage.executeUpdate("delete from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig and scp.pkg = :pkg", [surveyConfig: result.surveyConfig, pkg: pkg])
                    result.surveyConfig = result.surveyConfig.refresh()
                    result.surveyPackagesCount = SurveyConfigPackage.executeQuery("select count(*) from SurveyConfigPackage where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]
                }
                params.remove("removeUUID")
            }else if(params.selectedPkgs || params.pkgListToggler) {

                if(params.processOption == 'unlinkPackages') {
                    List uuidPkgs = SurveyConfigPackage.executeQuery("select scg.pkg.gokbId from SurveyConfigPackage scg where scg.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])
                    params.uuids = uuidPkgs
                }

                result.putAll(packageService.getWekbPackages(params))

                List selectedPkgs = []
                if (params.pkgListToggler == 'on') {
                    result.records.each {
                            selectedPkgs << it.uuid
                    }
                } else selectedPkgs = params.list("selectedPkgs")


                if (selectedPkgs) {
                    selectedPkgs.each {
                        Package pkg = Package.findByGokbId(it)

                        if(params.processOption == 'unlinkPackages'){
                            if(pkg) {
                                SurveyConfigPackage.executeUpdate("delete from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig and scp.pkg = :pkg", [surveyConfig: result.surveyConfig, pkg: pkg])
                            }
                        }
                        if(params.processOption == 'linkPackages') {
                            if (pkg) {
                                if (!SurveyConfigPackage.findByPkgAndSurveyConfig(pkg, result.surveyConfig)) {
                                    SurveyConfigPackage surveyConfigPackage = new SurveyConfigPackage(surveyConfig: result.surveyConfig, pkg: pkg).save()
                                }
                            } else {
                                pkg = packageService.createPackageWithWEKB(it)
                                if (pkg)
                                    SurveyConfigPackage surveyConfigPackage = new SurveyConfigPackage(surveyConfig: result.surveyConfig, pkg: pkg).save()
                            }
                        }
                    }
                    params.remove("selectedPkgs")
                }
            }
            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> surveyVendors(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            result.propList = PropertyDefinition.findAllPublicAndPrivateVendorProp(contextService.getOrg())

            if(params.initial){
                params.isMyX = ['wekb_exclusive']
                params.remove('initial')
                result.initial = true
            }

            result.surveyVendorsCount = SurveyConfigVendor.executeQuery("select count(*) from SurveyConfigVendor where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]

            result.selectedVendorIdList = SurveyConfigVendor.executeQuery("select scv.vendor.id from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])
            params.ids = result.selectedVendorIdList
            result.putAll(vendorService.getWekbVendors(params))

            if (params.isMyX) {
                List<String> xFilter = params.list('isMyX')
                Set<Long> f1Result = [], f2Result = []
                boolean   f1Set = false, f2Set = false
                result.currentVendorIdList = Vendor.executeQuery('select vr.vendor.id from VendorRole vr, OrgRole oo join oo.sub s where s = vr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
                if (xFilter.contains('ismyx_exclusive')) {
                    f1Result.addAll( result.vendorTotal.findAll { result.currentVendorIdList.contains( it.id ) }.collect{ it.id } )
                    f1Set = true
                }
                if (xFilter.contains('ismyx_not')) {
                    f1Result.addAll( result.vendorTotal.findAll { ! result.currentVendorIdList.contains( it.id ) }.collect{ it.id }  )
                    f1Set = true
                }
                if (xFilter.contains('wekb_exclusive')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId != null && it.gokbId in result.wekbRecords.keySet() }.collect{ it.id } )
                    f2Set = true
                }
                if (xFilter.contains('wekb_not')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                    f2Set = true
                }

                if (f1Set) { result.vendorTotal = result.vendorTotal.findAll { f1Result.contains(it.id) } }
                if (f2Set) { result.vendorTotal = result.vendorTotal.findAll { f2Result.contains(it.id) } }
            }

            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> linkSurveyVendor(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            result.putAll(vendorService.getWekbVendors(params))

   /*         if(result.surveyConfig.subscription && params.initial){
                List providers = result.surveyConfig.subscription.getProviders()
                if(providers.size() > 0){
                    params.qp_providers = providers.id
                    params.remove('initial')
                }
            }*/

            if(params.initial){
                params.isMyX = ['wekb_exclusive']
                params.remove('initial')
            }

            if (params.isMyX) {
                List<String> xFilter = params.list('isMyX')
                Set<Long> f1Result = [], f2Result = []
                boolean   f1Set = false, f2Set = false
                result.currentVendorIdList = Vendor.executeQuery('select vr.vendor.id from VendorRole vr, OrgRole oo join oo.sub s where s = vr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
                if (xFilter.contains('ismyx_exclusive')) {
                    f1Result.addAll( result.vendorTotal.findAll { result.currentVendorIdList.contains( it.id ) }.collect{ it.id } )
                    f1Set = true
                }
                if (xFilter.contains('ismyx_not')) {
                    f1Result.addAll( result.vendorTotal.findAll { ! result.currentVendorIdList.contains( it.id ) }.collect{ it.id }  )
                    f1Set = true
                }
                if (xFilter.contains('wekb_exclusive')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId != null && it.gokbId in result.wekbRecords.keySet() }.collect{ it.id } )
                    f2Set = true
                }
                if (xFilter.contains('wekb_not')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                    f2Set = true
                }

                if (f1Set) { result.vendorTotal = result.vendorTotal.findAll { f1Result.contains(it.id) } }
                if (f2Set) { result.vendorTotal = result.vendorTotal.findAll { f2Result.contains(it.id) } }
            }

            result.vendorListTotal = result.vendorTotal.size()
            result.vendorList = result.vendorTotal.drop(result.offset).take(result.max)

            result.surveyVendorsCount = SurveyConfigVendor.executeQuery("select count(*) from SurveyConfigVendor where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]

            result.selectedVendorIdList = SurveyConfigVendor.executeQuery("select scv.vendor.id from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])

            [result: result, status: STATUS_OK]
        }
    }

    Map<String,Object> processLinkSurveyVendor(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            if(params.addVendor) {
                Vendor vendor = Vendor.findById(params.addVendor)
                if(vendor) {
                    if(!SurveyConfigVendor.findByVendorAndSurveyConfig(vendor, result.surveyConfig)) {
                        SurveyConfigVendor surveyConfigVendor = new SurveyConfigVendor(surveyConfig: result.surveyConfig, vendor: vendor).save()
                    }
                }
                params.remove("addVendor")
            }else if (params.removeVendor) {
                Vendor vendor = Vendor.findById(params.removeVendor)
                if(vendor) {
                    SurveyConfigVendor.executeUpdate("delete from SurveyConfigVendor scp where scp.surveyConfig = :surveyConfig and scp.vendor = :vendor", [surveyConfig: result.surveyConfig, vendor: vendor])
                }
                params.remove("removeVendor")
            }else if (params.vendorListToggler || params.selectedVendors) {

                result.putAll(vendorService.getWekbVendors(params))

                if (params.isMyX) {
                    List<String> xFilter = params.list('isMyX')
                    Set<Long> f1Result = [], f2Result = []
                    boolean f1Set = false, f2Set = false
                    result.currentVendorIdList = Vendor.executeQuery('select vr.vendor.id from VendorRole vr, OrgRole oo join oo.sub s where s = vr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
                    if (xFilter.contains('ismyx_exclusive')) {
                        f1Result.addAll(result.vendorTotal.findAll { result.currentVendorIdList.contains(it.id) }.collect { it.id })
                        f1Set = true
                    }
                    if (xFilter.contains('ismyx_not')) {
                        f1Result.addAll(result.vendorTotal.findAll { !result.currentVendorIdList.contains(it.id) }.collect { it.id })
                        f1Set = true
                    }
                    if (xFilter.contains('wekb_exclusive')) {
                        f2Result.addAll(result.vendorTotal.findAll { it.gokbId != null && it.gokbId in result.wekbRecords.keySet() }.collect { it.id })
                        f2Set = true
                    }
                    if (xFilter.contains('wekb_not')) {
                        f2Result.addAll(result.vendorTotal.findAll { it.gokbId == null }.collect { it.id })
                        f2Set = true
                    }

                    if (f1Set) {
                        result.vendorTotal = result.vendorTotal.findAll { f1Result.contains(it.id) }
                    }
                    if (f2Set) {
                        result.vendorTotal = result.vendorTotal.findAll { f2Result.contains(it.id) }
                    }
                }

                List selectedVendors
                if (params.vendorListToggler == 'on') {
                    selectedVendors = result.vendorTotal.id
                } else selectedVendors = Params.getLongList(params, "selectedVendors")

                if (selectedVendors) {
                    selectedVendors.each {
                        Vendor vendor = Vendor.findById(it)
                        if(params.processOption == 'unlinkVendors'){
                            if(vendor) {
                                SurveyConfigVendor.executeUpdate("delete from SurveyConfigVendor scp where scp.surveyConfig = :surveyConfig and scp.vendor = :vendor", [surveyConfig: result.surveyConfig, vendor: vendor])
                            }
                        }
                        if(params.processOption == 'linkVendors') {
                            if (vendor) {
                                if (!SurveyConfigVendor.findByVendorAndSurveyConfig(vendor, result.surveyConfig)) {
                                    SurveyConfigVendor surveyConfigVendor = new SurveyConfigVendor(surveyConfig: result.surveyConfig, vendor: vendor).save()
                                }
                            }
                        }
                    }
                    params.remove("selectedVendors")
                    params.remove("vendorListToggler")
                }
            }

            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Takes the given input and processes the change on the selected survey cost items
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> processSurveyCostItemsBulk(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.putAll(financeControllerService.getEditVars(result.institution))
            List selectedMembers = params.list("selectedOrgs")

            if (selectedMembers) {

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
                RefdataValue cost_item_element_configuration = (params.ciec && params.ciec != 'null') ? RefdataValue.get(params.long('ciec')) : null

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

                RefdataValue selectedCostItemElement = params.bulkSelectedCostItemElementID ? (RefdataValue.get(Long.valueOf(params.bulkSelectedCostItemElementID))) : null

                boolean costItemsForSurveyPackage = params.selectPkg == "true" ? true : false

                Package pkg
                if(costItemsForSurveyPackage){
                    pkg = params.selectedPackageID ? Package.get(params.long('selectedPackageID')) : null
                    if(pkg && !SurveyConfigPackage.findBySurveyConfigAndPkg(result.surveyConfig, pkg)){
                        pkg = null
                    }
                }

                List<CostItem> surveyCostItems

                if (costItemsForSurveyPackage) {
                    if (pkg) {
                        if (selectedCostItemElement) {
                            surveyCostItems = CostItem.executeQuery('select costItem from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and surOrg.org.id in (:orgIDs) and costItem.costItemStatus != :status and costItem.costItemElement = :selectedCostItemElement and costItem.pkg = :pkg', [pkg: pkg, selectedCostItemElement: selectedCostItemElement, survConfig: result.surveyConfig, orgIDs: selectedMembers.collect { Long.parseLong(it) }, status: RDStore.COST_ITEM_DELETED])
                        } else {
                            surveyCostItems = CostItem.executeQuery('select costItem from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and surOrg.org.id in (:orgIDs) and costItem.costItemStatus != :status and costItem.pkg = :pkg', [pkg: pkg, survConfig: result.surveyConfig, orgIDs: selectedMembers.collect { Long.parseLong(it) }, status: RDStore.COST_ITEM_DELETED])
                        }
                    }
                } else {
                    if (selectedCostItemElement) {
                        surveyCostItems = CostItem.executeQuery('select costItem from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and surOrg.org.id in (:orgIDs) and costItem.costItemStatus != :status and costItem.costItemElement = :selectedCostItemElement and costItem.pkg is null', [selectedCostItemElement: selectedCostItemElement, survConfig: result.surveyConfig, orgIDs: selectedMembers.collect { Long.parseLong(it) }, status: RDStore.COST_ITEM_DELETED])
                    } else {
                        surveyCostItems = CostItem.executeQuery('select costItem from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and surOrg.org.id in (:orgIDs) and costItem.costItemStatus != :status and costItem.pkg is null', [survConfig: result.surveyConfig, orgIDs: selectedMembers.collect { Long.parseLong(it) }, status: RDStore.COST_ITEM_DELETED])
                    }
                }
                    surveyCostItems.each { CostItem surveyCostItem ->
                        if (params.deleteCostItems == "true") {
                            surveyCostItem.delete()
                        } else {
                            if (params.percentOnOldPrice) {
                                Double percentOnOldPrice = params.double('percentOnOldPrice', 0.00)
                                Subscription orgSub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyCostItem.surveyOrg.org)
                                CostItem costItem = CostItem.findBySubAndOwnerAndCostItemStatusNotEqualAndCostItemElement(orgSub, surveyCostItem.owner, RDStore.COST_ITEM_DELETED, surveyCostItem.costItemElement)
                                surveyCostItem.costInBillingCurrency = costItem ? (costItem.costInBillingCurrency * (1 + (percentOnOldPrice / 100))).round(2) : surveyCostItem.costInBillingCurrency

                                int taxRate = 0 //fallback
                                if (surveyCostItem.taxKey)
                                    taxRate = surveyCostItem.taxKey.taxRate

                                surveyCostItem.costInBillingCurrencyAfterTax = surveyCostItem.costInBillingCurrency ? surveyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : surveyCostItem.costInBillingCurrency

                                if (surveyCostItem.billingSumRounding) {
                                    surveyCostItem.costInBillingCurrency = surveyCostItem.costInBillingCurrency ? Math.round(surveyCostItem.costInBillingCurrency) : surveyCostItem.costInBillingCurrency
                                }
                                if (surveyCostItem.finalCostRounding) {
                                    surveyCostItem.costInBillingCurrencyAfterTax = surveyCostItem.costInBillingCurrencyAfterTax ? Math.round(surveyCostItem.costInBillingCurrencyAfterTax) : surveyCostItem.costInBillingCurrencyAfterTax
                                }

                            } else if (params.percentOnSurveyPrice) {
                                Double percentOnSurveyPrice = params.double('percentOnSurveyPrice', 0.00)
                                surveyCostItem.costInBillingCurrency = percentOnSurveyPrice ? (surveyCostItem.costInBillingCurrency * (1 + (percentOnSurveyPrice / 100))).round(2) : surveyCostItem.costInBillingCurrency

                                int taxRate = 0 //fallback
                                if (surveyCostItem.taxKey)
                                    taxRate = surveyCostItem.taxKey.taxRate

                                surveyCostItem.costInBillingCurrencyAfterTax = surveyCostItem.costInBillingCurrency ? surveyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : surveyCostItem.costInBillingCurrencyAfterTax

                                if (surveyCostItem.billingSumRounding) {
                                    surveyCostItem.costInBillingCurrency = surveyCostItem.costInBillingCurrency ? Math.round(surveyCostItem.costInBillingCurrency) : surveyCostItem.costInBillingCurrency
                                }
                                if (surveyCostItem.finalCostRounding) {
                                    surveyCostItem.costInBillingCurrencyAfterTax = surveyCostItem.costInBillingCurrencyAfterTax ? Math.round(surveyCostItem.costInBillingCurrencyAfterTax) : surveyCostItem.costInBillingCurrencyAfterTax
                                }
                            } else {
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
                            if (surveyCostItem.billingSumRounding)
                                surveyCostItem.costInBillingCurrency = surveyCostItem.costInBillingCurrency ? Math.round(surveyCostItem.costInBillingCurrency) : surveyCostItem.costInBillingCurrency
                            surveyCostItem.finalCostRounding = finalCostRounding != surveyCostItem.finalCostRounding ? finalCostRounding : surveyCostItem.finalCostRounding

                            //println( params.newFinalCostRounding)
                            //println( Boolean.valueOf(params.newFinalCostRounding))
                            //surveyCostItem.currencyRate = cost_currency_rate ?: surveyCostItem.currencyRate
                            surveyCostItem.taxKey = tax_key ?: surveyCostItem.taxKey
                            surveyCostItem.save()
                        }
                    }

            }

            params.remove('selectedOrgs')
            params.removeAll { it.key.toString().contains('new') }
            params.remove('deleteCostItems')
            params.remove('percentOnOldPrice')
            params.remove('percentOnSurveyPrice')
            params.remove('ciec')
            params.remove('selectedCostItemElementID')
            params.remove('selectedPackageID')

            [result: result, status: STATUS_OK]
        }
    }

    Map<String, Object> processSurveyCostItemsBulkWithUpload(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if(params.costItemsFile?.filename) {

                MultipartFile importFile = params.costItemsFile

                Map<String, IdentifierNamespace> namespaces = [gnd : IdentifierNamespace.findByNsAndNsType('gnd_org_nr', Org.class.name),
                                                               isil: IdentifierNamespace.findByNsAndNsType('ISIL', Org.class.name),
                                                               ror : IdentifierNamespace.findByNsAndNsType('ROR ID', Org.class.name),
                                                               wib : IdentifierNamespace.findByNsAndNsType('wibid', Org.class.name),
                                                               dealId : IdentifierNamespace.findByNsAndNsType('deal_id', Org.class.name),
                                                               anbieterProduktID:  IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PKG_ID, Package.class.name)]
                String encoding = UniversalDetector.detectCharset(importFile.getInputStream())

                if(encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                    List<String> rows = importFile.getInputStream().getText(encoding).split('\n')
                    List<String> headerRow = rows.remove(0).split('\t')
                    Map<String, Integer> colMap = [:]

                    headerRow.eachWithIndex { String headerCol, int c ->
                        if (headerCol.startsWith("\uFEFF"))
                            headerCol = headerCol.substring(1)
                        switch (headerCol.toLowerCase().trim()) {
                            case ["laser-uuid", "las:er-uuid", "las:er-uuid (einrichtung)", "las:er-uuid (institution)", "las:er-uuid (einrichtungslizenz)", "las:er-uuid (institution subscription)"]: colMap.uuidCol = c
                                break
                            case "gnd-id": colMap.gndCol = c
                                break
                            case "isil": colMap.isilCol = c
                                break
                            case "ror-id": colMap.rorCol = c
                                break
                            case "wib-id": colMap.wibCol = c
                                break
                            case "deal-id": colMap.dealCol = c
                                break
                            case ["kundennummer", "customer identifier"]: colMap.customerIdentifier = c
                                break
                            case ["bezeichnung", "title"]: colMap.title = c
                                break
                            case "element": colMap.element = c
                                break
                            case ["kostenvorzeichen", "cost item sign"]: colMap.costItemSign = c
                                break
                            case "status": colMap.status = c
                                break
                            case ["rechnungssumme", "invoice total"]: colMap.invoiceTotal = c
                                break
                            case ["währung", "waehrung", "currency"]: colMap.currency = c
                                break
                            case ["steuerbar", "tax type"]: colMap.taxType = c
                                break
                            case ["steuersatz", "tax rate"]: colMap.taxRate = c
                                break
                            case ["datum von", "date from"]: colMap.dateFrom = c
                                break
                            case ["datum bis", "date to"]: colMap.dateTo = c
                                break
                            case ["anmerkung", "description"]: colMap.description = c
                                break
                            case ["sortiername", "sortname"]: colMap.sortname = c
                                break
                            case ["Anbieter-Produkt-ID", "Anbieter-Product-ID"]: colMap.anbieterProduktID = c
                                break
                            default: log.info("unhandled parameter type ${headerCol}, ignoring ...")
                                break
                        }
                    }

                    int processCount = 0
                    int matchCount = 0
                    int costItemsCreatedCount = 0
                    rows.eachWithIndex { String row, Integer r ->
                        log.debug("now processing entry ${r}")
                        List<String> cols = row.split('\t', -1)
                        Org match = null
                        if (colMap.uuidCol >= 0 && cols[colMap.uuidCol] != null && !cols[colMap.uuidCol].trim().isEmpty()) {
                            match = Org.findByGlobalUIDAndArchiveDateIsNull(cols[colMap.uuidCol].trim())
                        }

                        if (!match && colMap.wibCol >= 0 && cols[colMap.wibCol] != null && !cols[colMap.wibCol].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.wibCol].trim(), ns: namespaces.wib])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }
                        if (!match && colMap.isilCol >= 0 && cols[colMap.isilCol] != null && !cols[colMap.isilCol].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.isilCol].trim(), ns: namespaces.isil])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }
                        if (!match && colMap.gndCol >= 0 && cols[colMap.gndCol] != null && !cols[colMap.gndCol].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.gndCol].trim(), ns: namespaces.gnd])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }
                        if (!match && colMap.rorCol >= 0 && cols[colMap.rorCol] != null && !cols[colMap.rorCol].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.rorCol].trim(), ns: namespaces.ror])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }

                        if (!match && colMap.customerIdentifier >= 0 && cols[colMap.customerIdentifier] != null && !cols[colMap.customerIdentifier].trim().isEmpty()) {
                            List matchList =  Org.executeQuery('select ci.customer from CustomerIdentifier ci join ci.platform plat where ci.value = :customerIdentifier and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription.instanceOf = :subscription)', [customerIdentifier: cols[colMap.customerIdentifier].trim(), subscription: result.surveyConfig.subscription])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }

                        if (!match && colMap.dealCol >= 0 && cols[colMap.dealCol] != null && !cols[colMap.dealCol].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.dealCol].trim(), ns: namespaces.dealId])
                            if (matchList.size() == 1)
                                match = matchList[0] as Org
                        }
                         if (!match && colMap.sortname >= 0 && cols[colMap.sortname] != null && !cols[colMap.sortname].trim().isEmpty()) {
                            List matchList = Org.executeQuery('select org from Org org where org.sortname = :sortname and org.archiveDate is null', [sortname: cols[colMap.sortname].trim()])
                            if (matchList.size() == 1)
                                 match = matchList[0] as Org
                        }


                        processCount++
                        if (match) {
                            matchCount++

                            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(match, result.surveyConfig)
                            boolean createCostItem = false

                            if (surveyOrg) {
                                RefdataValue cost_item_element
                                if (colMap.element != null) {
                                    String elementKey = cols[colMap.element]
                                    if (elementKey) {
                                        cost_item_element = RefdataValue.getByValueAndCategory(elementKey, RDConstants.COST_ITEM_ELEMENT)
                                        if (!cost_item_element)
                                            cost_item_element = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_ITEM_ELEMENT, elementKey)
                                    }
                                }

                                if(!cost_item_element){
                                    cost_item_element = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE
                                }

                                Package pkg
                                if (params.costItemsForSurveyPackage&& result.surveyConfig.packageSurvey){
                                    if (colMap.anbieterProduktID >= 0 && cols[colMap.anbieterProduktID] != null && !cols[colMap.anbieterProduktID].trim().isEmpty()) {
                                        List matchList = Package.executeQuery('select pkg from Identifier id join id.pkg pkg where id.value = :value and id.ns = :ns and pkg.id in (select scp.pkg.id from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig)', [surveyConfig: result.surveyConfig, value: cols[colMap.anbieterProduktID].trim(), ns: namespaces.anbieterProduktID])
                                        if (matchList.size() == 1)
                                            pkg = matchList[0] as Package
                                    }

                                    if(pkg) {
                                        if (cost_item_element) {
                                            if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, cost_item_element, pkg)) {
                                                createCostItem = true
                                            }
                                        } else {
                                            if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, pkg)) {
                                                createCostItem = true
                                            }
                                        }
                                    }
                                } else {
                                    if (cost_item_element) {
                                        if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED, cost_item_element)) {
                                            createCostItem = true
                                        }
                                    } else {
                                        if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED)) {
                                            createCostItem = true
                                        }
                                    }
                                }

                                if (createCostItem) {
                                    CostItem costItem = new CostItem(owner: contextService.getOrg(), surveyOrg: surveyOrg, costItemElement: cost_item_element)

                                    if (cost_item_element && (cols[colMap.costItemSign] == null || cols[colMap.costItemSign] == "")) {
                                        costItem.costItemElementConfiguration = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(cost_item_element, contextService.getOrg()).elementSign
                                    }

                                    if (colMap.currency != null) {
                                        String currencyKey = cols[colMap.currency]
                                        if (currencyKey) {
                                            RefdataValue currency = RefdataValue.getByValueAndCategory(currencyKey, "Currency")
                                            if (currency)
                                                costItem.billingCurrency = currency
                                            if (currency == RDStore.CURRENCY_EUR)
                                                costItem.currencyRate = 1
                                        }
                                    }
                                    if (colMap.description != null) {
                                        costItem.costDescription = (cols[colMap.description].trim() != '') ? cols[colMap.description].trim() : null
                                    }
                                    if (colMap.title != null) {
                                        costItem.costTitle = (cols[colMap.title].trim() != '') ? cols[colMap.title].trim() : null
                                    }
                                    if (colMap.invoiceTotal != null && cols[colMap.invoiceTotal] != null) {
                                        try {
                                            costItem.costInBillingCurrency = escapeService.parseFinancialValue(cols[colMap.invoiceTotal])
                                        }
                                        catch (NumberFormatException e) {
                                            log.error("costInBillingCurrency NumberFormatException: " + e.printStackTrace())
                                        }
                                        catch (NullPointerException | ParseException e) {
                                            log.error("costInBillingCurrency NullPointerException | ParseException: " + e.printStackTrace())
                                        }
                                    }
                                    if (colMap.taxType != null && cols[colMap.taxType] != null) {
                                        String taxTypeKey = cols[colMap.taxType].toLowerCase()
                                        int taxRate = 0
                                        if (cols[colMap.taxRate]) {
                                            try {
                                                taxRate = Integer.parseInt(cols[colMap.taxRate])
                                            }
                                            catch (Exception e) {
                                                log.error("non-numeric tax rate parsed")
                                            }
                                        }
                                        if (taxTypeKey) {
                                            CostItem.TAX_TYPES taxKey
                                            switch (taxRate) {
                                                case 5: taxKey = CostItem.TAX_TYPES.TAXABLE_5
                                                    break
                                                case 7: taxKey = CostItem.TAX_TYPES.TAXABLE_7
                                                    break
                                                case 16: taxKey = CostItem.TAX_TYPES.TAXABLE_16
                                                    break
                                                case 19: taxKey = CostItem.TAX_TYPES.TAXABLE_19
                                                    break
                                                default:
                                                    RefdataValue taxType = RefdataValue.getByValueAndCategory(taxTypeKey, RDConstants.TAX_TYPE)
                                                    if (!taxType)
                                                        taxType = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.TAX_TYPE, taxTypeKey)
                                                    switch (taxType) {
                                                        case RDStore.TAX_TYPE_NOT_TAXABLE: taxKey = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                                                            break
                                                        case RDStore.TAX_TYPE_NOT_APPLICABLE: taxKey = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                                                            break
                                                        case RDStore.TAX_TYPE_TAXABLE_EXEMPT: taxKey = CostItem.TAX_TYPES.TAX_EXEMPT
                                                            break
                                                        case RDStore.TAX_TYPE_TAX_CONTAINED_19: taxKey = CostItem.TAX_TYPES.TAX_CONTAINED_19
                                                            break
                                                        case RDStore.TAX_TYPE_TAX_CONTAINED_7: taxKey = CostItem.TAX_TYPES.TAX_CONTAINED_7
                                                            break
                                                        case RDStore.TAX_TYPE_REVERSE_CHARGE: taxKey = CostItem.TAX_TYPES.TAX_REVERSE_CHARGE
                                                            break
                                                    }
                                                    break
                                            }
                                            if (taxKey)
                                                costItem.taxKey = taxKey
                                        }
                                    }
                                    if (colMap.status != null) {
                                        String statusKey = cols[colMap.status]
                                        RefdataValue status
                                        if (statusKey) {
                                            status = RefdataValue.getByValueAndCategory(statusKey, RDConstants.COST_ITEM_STATUS)
                                            if (!status)
                                                status = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_ITEM_STATUS, statusKey)
                                        }
                                        costItem.costItemStatus = status
                                    }
                                    if (colMap.costItemSign != null && cols[colMap.costItemSign] != null) {
                                        String elementSign = cols[colMap.costItemSign]
                                        if (elementSign) {
                                            RefdataValue ciec = RefdataValue.getByValueAndCategory(elementSign, RDConstants.COST_CONFIGURATION)
                                            if (!ciec)
                                                ciec = RefdataValue.getByCategoryDescAndI10nValueDe(RDConstants.COST_CONFIGURATION, elementSign)

                                            costItem.costItemElementConfiguration = ciec
                                        }
                                    }

                                    if (colMap.dateFrom != null) {
                                        Date startDate = DateUtils.parseDateGeneric(cols[colMap.dateFrom])
                                        if (startDate)
                                            costItem.startDate = startDate
                                    }

                                    if (colMap.dateTo != null) {
                                        Date endDate = DateUtils.parseDateGeneric(cols[colMap.dateTo])
                                        if (endDate)
                                            costItem.endDate = endDate
                                    }

                                    if(params.costItemsForSurveyPackage && pkg){
                                        costItem.pkg = pkg
                                    }

                                    if(costItem.save()){
                                        costItemsCreatedCount++
                                    }else {
                                        log.error("CostItem not create because: "+ costItem.errors.toString())
                                    }
                                }
                            }

                        }
                    }

                    result.processCount = processCount
                    result.matchCount = matchCount
                    result.costItemsCreatedCount = costItemsCreatedCount
                }
                else
                {
                    Object[] args = [encoding]
                    result.error = messageSource.getMessage('default.import.error.wrongCharset', args, result.locale)
                }
            }

            [result: result, status: STATUS_OK]
        }
    }


    /**
     * Marks the given survey as finished
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> setSurveyWorkFlowInfos(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }
            switch (params.setSurveyWorkFlowInfo) {
                case "setSurveyConfigFinish":
                    result.surveyConfig.configFinish = params.configFinish ?: false
                    if (result.surveyConfig.save()) {
                        //result.message = messageSource.getMessage('survey.change.successfull', null, result.locale)
                    } else {
                        result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
                    }
                    break
                case "workflowRenewalSent":
                    result.surveyInfo.isRenewalSent = params.renewalSent ?: false
                    result.surveyConfig.subscription.renewalSent = result.surveyInfo.isRenewalSent
                    result.surveyConfig.subscription.renewalSentDate = new Date()

                    result.surveyConfig.subscription.save()

                    if (result.surveyInfo.save()) {
                        //result.message = messageSource.getMessage('survey.change.successfull', null, result.locale)
                    } else {
                        result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
                    }
                    break
                case "workflowCostItemsFinish":
                    result.surveyConfig.costItemsFinish = params.costItemsFinish ?: false

                    if (!result.surveyConfig.save()) {
                        result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
                    }
                    break
            }
            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Marks the given transfer procedure as (un-)checked
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> setSurveyTransferConfig(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            Map transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : [:]

            List<Subscription> transferWorkflowSubs = []

            if (result.surveyConfig.subSurveyUseForTransfer) {
                Set<Subscription> nextSubs = linksGenerationService.getSuccessionChain(result.surveyConfig.subscription, 'destinationSubscription')

                int years = 1
                List<PropertyDefinition> surProperties = result.surveyConfig.surveyProperties.surveyProperty
                if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surProperties.id) {
                    years = 2
                }
                if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surProperties.id) {
                    years = 3
                }
                if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in surProperties.id) {
                    years = 4
                }
                if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in surProperties.id) {
                    years = 5
                }

                for (int i = 0; i < years; i++) {
                    if (nextSubs[i])
                        transferWorkflowSubs << nextSubs[i]
                }


                Subscription targetSubscription
                if (params.targetSubscriptionId) {
                    targetSubscription = Subscription.get(params.targetSubscriptionId)
                }

                if (targetSubscription && transferWorkflowSubs.size() > 0 && (targetSubscription in transferWorkflowSubs)) {
                    result.parentSuccessorSubscription = targetSubscription
                } else {
                    result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
                }

            } else {
                result.parentSuccessorSubscription = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
                transferWorkflowSubs << result.parentSuccessorSubscription
            }

            transferWorkflowSubs.eachWithIndex { Subscription subscription, int i ->
                if (subscription == result.parentSuccessorSubscription) {
                    if (i == 0) {
                        if (subscription == result.surveyConfig.subscription) {
                            if (params.transferMembers != null) {
                                transferWorkflow.transferMembers = params.transferMembers
                                if (result.surveyConfig.subSurveyUseForTransfer) {
                                    result.surveyConfig.subscription.participantTransferWithSurvey = transferWorkflow.transferMembers
                                    result.surveyConfig.subscription.save()
                                }
                            }

                            if (params.transferSurveyCostItems != null) {
                                transferWorkflow.transferSurveyCostItems = params.transferSurveyCostItems
                            }

                            if (params.transferSurveyCostItemPackage != null) {
                                transferWorkflow.transferSurveyCostItemPackage = params.transferSurveyCostItemPackage
                            }

                            if (params.transferSurveyProperties != null) {
                                transferWorkflow.transferSurveyProperties = params.transferSurveyProperties
                            }

                            if (params.transferCustomProperties != null) {
                                transferWorkflow.transferCustomProperties = params.transferCustomProperties
                            }

                            if (params.transferPrivateProperties != null) {
                                transferWorkflow.transferPrivateProperties = params.transferPrivateProperties
                            }

                            if (params.transferSubPackagesAndIes != null) {
                                transferWorkflow.transferSubPackagesAndIes = params.transferSubPackagesAndIes
                            }

                            if (params.transferSurveyPackages != null) {
                                transferWorkflow.transferSurveyPackages = params.transferSurveyPackages
                            }

                            if (params.transferSurveyVendors != null) {
                                transferWorkflow.transferSurveyVendors = params.transferSurveyVendors
                            }
                        }else {
                            Map transferWorkflowForSub = [:]
                            if (transferWorkflow["transferWorkflowForSub_${subscription.id}"]) {
                                transferWorkflowForSub = transferWorkflow["transferWorkflowForSub_${subscription.id}"]
                            } else {
                                transferWorkflowForSub = [:]
                            }

                            if (params.transferMembers != null) {
                                transferWorkflowForSub.transferMembers = params.transferMembers
                            }

                            if (params.transferSurveyCostItems != null) {
                                transferWorkflowForSub.transferSurveyCostItems = params.transferSurveyCostItems
                            }

                            if (params.transferSurveyCostItemPackage != null) {
                                transferWorkflowForSub.transferSurveyCostItemPackage = params.transferSurveyCostItemPackage
                            }

                            if (params.transferSurveyProperties != null) {
                                transferWorkflowForSub.transferSurveyProperties = params.transferSurveyProperties
                            }

                            if (params.transferCustomProperties != null) {
                                transferWorkflowForSub.transferCustomProperties = params.transferCustomProperties
                            }

                            if (params.transferPrivateProperties != null) {
                                transferWorkflowForSub.transferPrivateProperties = params.transferPrivateProperties
                            }

                            if (params.transferSubPackagesAndIes != null) {
                                transferWorkflowForSub.transferSubPackagesAndIes = params.transferSubPackagesAndIes
                            }

                            if (params.transferSurveyPackages != null) {
                                transferWorkflowForSub.transferSurveyPackages = params.transferSurveyPackages
                            }

                            if (params.transferSurveyVendors != null) {
                                transferWorkflowForSub.transferSurveyVendors = params.transferSurveyVendors
                            }
                            transferWorkflow["transferWorkflowForSub_${subscription.id}"] = transferWorkflowForSub
                        }
                    } else {
                        Map transferWorkflowForMultiYear = [:]
                        if (transferWorkflow["transferWorkflowForMultiYear_${subscription.id}"]) {
                            transferWorkflowForMultiYear = transferWorkflow["transferWorkflowForMultiYear_${subscription.id}"]
                        } else {
                            transferWorkflowForMultiYear = [:]
                        }

                        if (params.transferMembers != null) {
                            transferWorkflowForMultiYear.transferMembers = params.transferMembers
                        }

                        if (params.transferSurveyCostItems != null) {
                            transferWorkflowForMultiYear.transferSurveyCostItems = params.transferSurveyCostItems
                        }

                        if (params.transferSurveyCostItemPackage != null) {
                            transferWorkflowForMultiYear.transferSurveyCostItemPackage = params.transferSurveyCostItemPackage
                        }

                        if (params.transferSurveyProperties != null) {
                            transferWorkflowForMultiYear.transferSurveyProperties = params.transferSurveyProperties
                        }

                        if (params.transferCustomProperties != null) {
                            transferWorkflowForMultiYear.transferCustomProperties = params.transferCustomProperties
                        }

                        if (params.transferPrivateProperties != null) {
                            transferWorkflowForMultiYear.transferPrivateProperties = params.transferPrivateProperties
                        }

                        if (params.transferSubPackagesAndIes != null) {
                            transferWorkflowForMultiYear.transferSubPackagesAndIes = params.transferSubPackagesAndIes
                        }

                        if (params.transferSurveyPackages != null) {
                            transferWorkflowForMultiYear.transferSurveyPackages = params.transferSurveyPackages
                        }

                        if (params.transferSurveyVendors != null) {
                            transferWorkflowForMultiYear.transferSurveyVendors = params.transferSurveyVendors
                        }
                        transferWorkflow["transferWorkflowForMultiYear_${subscription.id}"] = transferWorkflowForMultiYear
                    }
                }
            }

            result.surveyConfig.transferWorkflow = transferWorkflow ? (new JSON(transferWorkflow)).toString() : null

            if (!result.surveyConfig.save()) {
                    result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
            }

            [result: result, status: STATUS_OK]
        }
    }


    /**
     * Gets the tasks to the given survey
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the tasks in case of success, ERROR otherwise
     */
    Map<String, Object> tasks(SurveyController controller, GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            SwissKnife.setPaginationParams(result, params, result.user as User)
            result.cmbTaskInstanceList = taskService.getTasks((User) result.user, (SurveyConfig) result.surveyConfig)['cmbTaskInstanceList']
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Collects the data for the given renewal survey for the evaluation view
     * @param params the request parameter map
     * @return OK with the retrieved data in case of success, ERROR otherwise
     */
    Map<String, Object> renewalEvaluation(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result)
            [result: null, status: STATUS_ERROR]
        else {

            result.parentSubscription = result.surveyConfig.subscription
            result.parentSubChilds = subscriptionService.getValidSubChilds(result.parentSubscription)
            result.parentSuccessorSubscription = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
            result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null


            result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION
            if (result.parentSuccessorSubscription) {
                String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
                result.memberLicenses = License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE])
            }


            /* if(result.parentSubChilds) {
             Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select distinct(sp.type) from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null",[subscriptionSet:validSubChildren,ctx:result.institution])
             propList.addAll(result.parentSubscription.propertySet.type)
             result.propList = propList
             result.filteredSubChilds = validSubChildren
             List<Subscription> childSubs = result.parentSubscription.getNonDeletedDerivedSubscriptions()
             if(childSubs) {
                 String localizedName
                 switch(LocaleUtils.getCurrentLocale()) {
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
         }*/

            result.properties = []
            result.properties.addAll(SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty.sort {
                it.getI10n('name')
            })


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

            List currentParticipantIDs = []
            result.orgsWithMultiYearTermSub = []
            //result.orgsLateCommers = []
            List orgsWithMultiYearTermOrgsID = []
            List orgsLateCommersOrgsID = []
            result.parentSubChilds.each { Subscription sub ->
                if (surveyService.existsCurrentMultiYearTermBySurveyUseForTransfer(result.surveyConfig, sub.getSubscriber())) {
                    result.orgsWithMultiYearTermSub << sub
                    orgsWithMultiYearTermOrgsID << sub.getSubscriberRespConsortia().id

                } else {
                    //println(sub)
                    currentParticipantIDs << sub.getSubscriberRespConsortia().id
                }
            }


            result.orgsWithParticipationInParentSuccessor = []
            result.parentSuccessorSubChilds.each { sub ->
                Org org = sub.getSubscriberRespConsortia()
                if (!(org.id in orgsWithMultiYearTermOrgsID) || !(org.id in currentParticipantIDs)) {
                    result.orgsWithParticipationInParentSuccessor << sub
                }
            }
            result.orgsWithParticipationInParentSuccessor = result.orgsWithParticipationInParentSuccessor.sort { it.getSubscriberRespConsortia().sortname }

            result.orgInsertedItself = []

            List<Org> orgInsertedItselfList = SurveyOrg.executeQuery("select surOrg.org from SurveyOrg as surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.orgInsertedItself = true", [surveyConfig: result.surveyConfig])

            List<Org> orgNotInsertedItselfList = SurveyOrg.executeQuery("select surOrg.org from SurveyOrg as surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.orgInsertedItself = false", [surveyConfig: result.surveyConfig])


            //Orgs with inserted it self to the survey
            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and (refValue = :refValue OR refValue is null) and participant in (:orgInsertedItselfList) order by participant.sortname",
                    [
                            owner                : result.institution.id,
                            surProperty          : result.participationProperty.id,
                            surConfig            : result.surveyConfig.id,
                            refValue             : RDStore.YN_NO,
                            orgInsertedItselfList: orgInsertedItselfList]).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.sub = surveyResult.participantSubscription
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                result.orgInsertedItself << newSurveyResult

            }

            result.orgsWithTermination = []
            String queryOrgsWithTermination = 'from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue  '
            Map queryMapOrgsWithTermination = [
                    owner      : result.institution.id,
                    surProperty: result.participationProperty.id,
                    surConfig  : result.surveyConfig.id,
                    refValue   : RDStore.YN_NO]

            if (orgNotInsertedItselfList.size() > 0) {
                queryOrgsWithTermination += ' and participant in (:orgNotInsertedItselfList) '
                queryMapOrgsWithTermination.orgNotInsertedItselfList = orgNotInsertedItselfList
            }

            //Orgs with termination there sub
            SurveyResult.executeQuery(queryOrgsWithTermination + " order by participant.sortname", queryMapOrgsWithTermination).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.sub = surveyResult.participantSubscription
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                result.orgsWithTermination << newSurveyResult

            }


            // Orgs that renew or new to Sub
            result.orgsContinuetoSubscription = []
            result.newOrgsContinuetoSubscription = []

            List<SurveyResult> surveyResults = SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                    [
                            owner      : result.institution.id,
                            surProperty: result.participationProperty.id,
                            surConfig  : result.surveyConfig.id,
                            refValue   : RDStore.YN_YES])
            surveyResults.each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                if (surveyResult.participant.id in currentParticipantIDs) {

                    newSurveyResult.sub = surveyResult.participantSubscription

                    //newSurveyResult.sub = result.parentSubscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyResult.participant)

                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        SurveyResult participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        SurveyResult participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree.comment
                            }
                        }
                    }

                    if (result.multiYearTermFourSurvey) {
                        newSurveyResult.newSubPeriodFoureStartDate = null
                        newSurveyResult.newSubPeriodFourEndDate = null

                        SurveyResult participantPropertyFour = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey)
                        if (participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFourStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFourEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 4.year) : null
                                newSurveyResult.participantPropertyFourComment = participantPropertyFour.comment
                            }
                        }
                    }

                    if (result.multiYearTermFiveSurvey) {
                        newSurveyResult.newSubPeriodFiveStartDate = null
                        newSurveyResult.newSubPeriodFiveEndDate = null

                        SurveyResult participantPropertyFive = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey)
                        if (participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFiveStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFiveEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 5.year) : null
                                newSurveyResult.participantPropertyFiveComment = participantPropertyFive.comment
                            }
                        }
                    }

                    result.orgsContinuetoSubscription << newSurveyResult
                }
                if (!(surveyResult.participant.id in currentParticipantIDs) && !(surveyResult.participant.id in orgsLateCommersOrgsID) && !(surveyResult.participant.id in orgsWithMultiYearTermOrgsID)) {


                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        SurveyResult participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        SurveyResult participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree.comment
                            }
                        }
                    }

                    if (result.multiYearTermFourSurvey) {
                        newSurveyResult.newSubPeriodFourStartDate = null
                        newSurveyResult.newSubPeriodFourEndDate = null

                        SurveyResult participantPropertyFour = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey)
                        if (participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFourStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFourEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 4.year) : null
                                newSurveyResult.participantPropertyFourComment = participantPropertyFour.comment
                            }
                        }
                    }

                    if (result.multiYearTermFiveSurvey) {
                        newSurveyResult.newSubPeriodFiveStartDate = null
                        newSurveyResult.newSubPeriodFiveEndDate = null

                        SurveyResult participantPropertyFive = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey)
                        if (participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFiveStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFiveEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 5.year) : null
                                newSurveyResult.participantPropertyFiveComment = participantPropertyFive.comment
                            }
                        }
                    }

                    result.newOrgsContinuetoSubscription << newSurveyResult
                }

            }


            //Orgs without really result
            result.orgsWithoutResult = []

            String queryOrgsWithoutResult = 'from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue is null '
            Map queryMapOrgsWithoutResult = [
                    owner      : result.institution.id,
                    surProperty: result.participationProperty.id,
                    surConfig  : result.surveyConfig.id]

            if (orgNotInsertedItselfList.size() > 0) {
                queryOrgsWithoutResult += ' and participant in (:orgNotInsertedItselfList) '
                queryMapOrgsWithoutResult.orgNotInsertedItselfList = orgNotInsertedItselfList
            }


            SurveyResult.executeQuery(queryOrgsWithoutResult + " order by participant.sortname", queryMapOrgsWithoutResult).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(it.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }


                if (surveyResult.participant.id in currentParticipantIDs) {
                    newSurveyResult.sub = surveyResult.participantSubscription
                    //newSurveyResult.sub = result.parentSubscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyResult.participant)
                } else {
                    newSurveyResult.sub = null
                }
                result.orgsWithoutResult << newSurveyResult
            }


            //MultiYearTerm Subs
            Integer sumParticipantWithSub = ((result.orgsContinuetoSubscription.groupBy {
                it.participant.id
            }.size()) + (result.orgsWithTermination.groupBy { it.participant.id }.size()) + (result.orgsWithMultiYearTermSub.size()))

            if (sumParticipantWithSub < result.parentSubChilds.size()) {
                /*def property = PropertyDefinition.getByNameAndDescr("Perennial term checked", PropertyDefinition.SUB_PROP)

            def removeSurveyResultOfOrg = []
            result.orgsWithoutResult.each { surveyResult ->
                if (surveyResult.participant.id in currentParticipantIDs && surveyResult.sub) {

                    if (property.isRefdataValueType()) {
                        if (surveyResult.sub.propertySet.find {
                            it.type.id == property.id
                        }?.refValue == RefdataValue.getByValueAndCategory('Yes', property.refdataCategory)) {

                            result.orgsWithMultiYearTermSub << surveyResult.sub
                            removeSurveyResultOfOrg << surveyResult
                        }
                    }
                }
            }
            removeSurveyResultOfOrg.each{ it
                result.orgsWithoutResult?.remove(it)
            }*/

                result.orgsWithMultiYearTermSub = result.orgsWithMultiYearTermSub.sort { it.getSubscriberRespConsortia().sortname }

            }

            result.propertiesChanged = [:]
            result.propertiesChangedByParticipant = []
            result.properties.sort { it.getI10n('name') }.each { PropertyDefinition propertyDefinition ->

                PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(propertyDefinition.name, PropertyDefinition.SUB_PROP)
                if (subPropDef) {
                    result.surveyConfig.orgs.each { SurveyOrg surveyOrg ->
                        Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                [parentSub  : result.surveyConfig.subscription,
                                 participant: surveyOrg.org
                                ])[0]
                        SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, contextService.getOrg())
                        SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, contextService.getOrg())

                        if (surveyResult && subscriptionProperty) {
                            String surveyValue = surveyResult.getValue()
                            String subValue = subscriptionProperty.getValue()
                            if (surveyValue && surveyValue != subValue) {
                                Map changedMap = [:]
                                //changedMap.surveyResult = surveyResult
                                //changedMap.subscriptionProperty = subscriptionProperty
                                //changedMap.surveyValue = surveyValue
                                //changedMap.subValue = subValue
                                changedMap.participant = surveyOrg.org

                                result.propertiesChanged."${propertyDefinition.id}" = result.propertiesChanged."${propertyDefinition.id}" ?: []
                                result.propertiesChanged."${propertyDefinition.id}" << changedMap

                                result.propertiesChangedByParticipant << surveyOrg.org
                            }
                        }

                    }

                }
            }

            result.totalOrgs = result.orgsContinuetoSubscription.size() + result.newOrgsContinuetoSubscription.size() + result.orgsWithMultiYearTermSub.size() + result.orgsWithTermination.size() + result.orgsWithParticipationInParentSuccessor.size() + result.orgsWithoutResult.size()

            result
        }
        [result: result, status: STATUS_OK]

    }

    /**
     * Call to evaluate the given survey's results; the results may be displayed as HTML or
     * exported as (configurable) Excel worksheet
     * @return the survey evaluation view, either as HTML or as (configurable) Excel worksheet
     */
    def surveyEvaluation(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {


            result.participantsNotFinishTotal = SurveyOrg.countByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
            result.participantsFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)
            result.participantsTotal = SurveyOrg.countBySurveyConfig(result.surveyConfig)

            params.tab = params.tab ?: (result.participantsFinishTotal > 0 ? 'participantsViewAllFinish' : 'participantsViewAllNotFinish')

            if (params.tab == 'participantsViewAllNotFinish') {
                params.participantsNotFinish = true
            } else if (params.tab == 'participantsViewAllFinish') {
                params.participantsFinish = true
            }

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
                            SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, contextService.getOrg())
                            SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, contextService.getOrg())

                            if (surveyResult && subscriptionProperty) {
                                String surveyValue = surveyResult.getValue()
                                String subValue = subscriptionProperty.getValue()
                                if (surveyValue && surveyValue != subValue) {
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

            if(!params.fileformat) {
                 List charts = surveyService.generatePropertyDataForCharts(result.surveyConfig, result.participants?.org)
                if(result.surveyConfig.vendorSurvey) {
                    charts = charts + surveyService.generateSurveyVendorDataForCharts(result.surveyConfig, result.participants?.org)
                }
                if(params.chartSort){
                    result.charts = [['property', 'value']] + charts.sort{it[1]}
                }else {
                    result.charts = [['property', 'value']] + charts
                }
            }

            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Call to evaluate the given survey's results; the results may be displayed as HTML or
     * exported as (configurable) Excel worksheet
     * @return the survey evaluation view, either as HTML or as (configurable) Excel worksheet
     */
    def surveyPackagesEvaluation(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.participantsNotFinishTotal = SurveyOrg.countByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
            result.participantsFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)
            result.participantsTotal = SurveyOrg.countBySurveyConfig(result.surveyConfig)

            params.tab = params.tab ?: (result.participantsFinishTotal > 0 ? 'participantsViewAllFinish' : 'participantsViewAllNotFinish')

            if (params.tab == 'participantsViewAllNotFinish') {
                params.participantsNotFinish = true
            } else if (params.tab == 'participantsViewAllFinish') {
                params.participantsFinish = true
            }

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)

            result.propList = result.surveyConfig.surveyProperties.surveyProperty

            result.participants = result.participants.sort { it.org.sortname }

            if(!params.fileformat) {
                List charts = surveyService.generateSurveyPackageDataForCharts(result.surveyConfig, result.participants?.org)

                if(params.chartSort){
                    result.charts = [['property', 'value']] + charts.sort{it[1]}
                }else {
                    result.charts = [['property', 'value']] + charts
                }
            }

            [result: result, status: STATUS_OK]
        }
    }

    def surveyVendorsEvaluation(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.participantsNotFinishTotal = SurveyOrg.countByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
            result.participantsFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)
            result.participantsTotal = SurveyOrg.countBySurveyConfig(result.surveyConfig)

            params.tab = params.tab ?: (result.participantsFinishTotal > 0 ? 'participantsViewAllFinish' : 'participantsViewAllNotFinish')

            if (params.tab == 'participantsViewAllNotFinish') {
                params.participantsNotFinish = true
            } else if (params.tab == 'participantsViewAllFinish') {
                params.participantsFinish = true
            }

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)


            result.propList = result.surveyConfig.surveyProperties.surveyProperty

            result.participants = result.participants.sort { it.org.sortname }

            result.charts = surveyService.generateSurveyVendorDataForCharts(result.surveyConfig, result.participants?.org)

            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Call to open the participant transfer view
     * @return the participant list with their selections
     */
    Map<String, Object> surveyTransfer(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.participantsNotFinishTotal = SurveyOrg.countByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
            result.participantsFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)
            result.participantsTotal = SurveyOrg.countBySurveyConfig(result.surveyConfig)

            params.tab = params.tab ?: (result.participantsFinishTotal > 0 ? 'participantsViewAllFinish' : 'participantsViewAllNotFinish')

            if (params.tab == 'participantsViewAllNotFinish') {
                params.participantsNotFinish = true
            } else if (params.tab == 'participantsViewAllFinish') {
                params.participantsFinish = true
            }

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
                            SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, contextService.getOrg())
                            SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, contextService.getOrg())

                            if (surveyResult && subscriptionProperty) {
                                String surveyValue = surveyResult.getValue()
                                String subValue = subscriptionProperty.getValue()
                                if (surveyValue && surveyValue != subValue) {
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

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to transfer the survey participants onto the next year's subscription
     * @return the subscription comparison view for the given subscriptions (the predecessor and the successor instances)
     */
    Map<String, Object> processTransferParticipants(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            if (!params.targetSubscriptionId) {
                result.error = messageSource.getMessage("surveyTransfer.error.noSelectedSub", null, result.locale)
                [result: result, status: STATUS_ERROR]
                return
            }

            result.parentSubscription = result.surveyConfig.subscription
            result.targetSubscription = Subscription.get(params.targetSubscriptionId)

            List<String> excludes = PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key }
            //excludes << 'freezeHolding'
            excludes.add(PendingChangeConfiguration.TITLE_REMOVED)
            excludes.add(PendingChangeConfiguration.TITLE_REMOVED + PendingChangeConfiguration.NOTIFICATION_SUFFIX)
            excludes.add(PendingChangeConfiguration.TITLE_DELETED)
            excludes.add(PendingChangeConfiguration.TITLE_DELETED + PendingChangeConfiguration.NOTIFICATION_SUFFIX)
            excludes.addAll(PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key + PendingChangeConfiguration.NOTIFICATION_SUFFIX })
            Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name, result.targetSubscription.id, excludes)

            result.newSubs = []
            Integer countNewSubs = 0
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            Date startDate = params.startDate ? sdf.parse(params.startDate) : null
            Date endDate = params.endDate ? sdf.parse(params.endDate) : null
            params.list('selectedOrgs').each { orgId ->
                Org org = Org.get(orgId)
                if (org && result.targetSubscription) {
                    log.debug("Generating seperate slaved instances for members")
                    Subscription oldSubofParticipant = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : result.parentSubscription,
                             participant: org
                            ])[0]

                    Subscription memberSub = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.targetSubscription, org, startDate, endDate, false, result.targetSubscription.status, inheritedAttributes, null, true, true, null, null)

                    if(memberSub) {
                        result.newSubs << memberSub
                    }
                }
                countNewSubs++
            }
            result.countNewSubs = countNewSubs
            if (result.newSubs?.size() > 0) {
                result.targetSubscription.syncAllShares(result.newSubs)
            }
            Object[] args = [countNewSubs, result.newSubs?.size() ?: 0]
            result.message = messageSource.getMessage('surveyInfo.transfer.info', args, result.locale) as String

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to list the members; either those who completed the survey or those who did not
     * @return a list of the participants in the called tab view
     */
    Map<String, Object> openParticipantsAgain(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            params.participantsFinish = true

            result.participantsFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)

            result.propList = result.surveyConfig.surveyProperties.surveyProperty

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to list the members; either those who completed the survey or those who did not
     * @return a list of the participants in the called tab view
     */
    Map<String, Object> participantsReminder(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            params.participantsNotFinish = true

            result.participantsNotFinishTotal = SurveyOrg.countBySurveyConfigAndFinishDateIsNull(result.surveyConfig)

            Map<String, Object> fsq = filterService.getSurveyOrgQuery(params, result.surveyConfig)

            result.participants = SurveyOrg.executeQuery(fsq.query, fsq.queryParams, params)

            result.propList = result.surveyConfig.surveyProperties.surveyProperty

            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Opens the survey for the given participants and sends eventual reminders
     * @return the participation view with the counts of execution done
     */
    /*
    @Deprecated
     Map<String,Object> processOpenParticipantsAgain(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {

        result.editable = contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO )

         if (!result.editable) {
                        [result: null, status: STATUS_ERROR]
                        return
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
            result.message =  messageSource.getMessage('openParticipantsAgain.sendReminderMail.count', args: [countReminderMails])
        }

        if(countOpenParticipants > 0 && !openAndSendMail){
            result.message =  messageSource.getMessage('openParticipantsAgain.open.count', args: [countOpenParticipants])
        }

        if(countOpenParticipants > 0 && openAndSendMail){
            result.message =  messageSource.getMessage('openParticipantsAgain.openWithMail.count', args: [countOpenParticipants])
        }

        redirect(action: 'openParticipantsAgain', id: result.surveyInfo.id, params:[tab: params.tab, surveyConfigID: result.surveyConfig.id])

    }*/

    /**
     * Reopens the given survey for the given participant
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> actionsForParticipant(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result.participant = params.participant ? Org.get(params.participant) : null
            if (result.participant) {
                switch (params.actionForParticipant) {
                    case "openSurveyAgainForParticipant":
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

                        surveyOrg.finishDate = null
                        surveyOrg.save()
                        break
                    case "finishSurveyForParticipant":
                        SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

                        surveyOrg.finishDate = new Date()
                        surveyOrg.save()
                        break
                }


            }

            [result: result, status: STATUS_OK]
        }
    }


    /**
     * Evaluates the general selection and the costs of the participant
     * @return the participant evaluation view
     */
    Map<String, Object> evaluationParticipant(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            /*
            if (!result.editable) {
                 [result:null,status:STATUS_ERROR] return
            }
            */

            result.participant = Org.get(params.participant)
            result.surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

            result.ownerId = result.surveyInfo.owner.id

            params.viewTab = params.viewTab ?: 'overview'

            result = surveyService.participantResultGenerics(result, result.participant, params)

            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)
            result.institution = result.participant

            [result: result, status: STATUS_OK]
        }

    }


    Map<String, Object> actionsForSurveyProperty(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            if(params.actionForSurveyProperty in ["moveUp", "moveDown"]){
                result.editable = result.editable
            }else {
                result.editable = (result.surveyInfo && result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable
            }
            if(result.editable) {
                switch (params.actionForSurveyProperty) {
                    case "setSurveyPropertyMandatory":
                        SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.get(params.surveyConfigProperties)

                        surveyConfigProperties.mandatoryProperty = params.mandatoryProperty ?: false

                        if (!surveyConfigProperties.save()) {
                            result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
                        }
                        break
                    case "deleteSurveyPropFromConfig":
                        if (params.surveyPropertyConfigId) {
                            SurveyConfigProperties surveyConfigProp = SurveyConfigProperties.get(params.surveyPropertyConfigId)
                            //SurveyInfo surveyInfo = surveyConfigProp.surveyConfig.surveyInfo
                            try {
                                surveyConfigProp.delete()
                                //result.message = messageSource.getMessage("default.deleted.message", args: [messageSource.getMessage("surveyProperty.label"), ''])
                            }
                            catch (DataIntegrityViolationException e) {
                                Object[] args = [messageSource.getMessage("surveyProperty.label", null, result.locale)]
                                result.error = messageSource.getMessage("default.not.deleted.message", args, result.locale)
                            }
                        }
                        break
                    case "createSurveyProperty":
                        PropertyDefinition surveyProperty = PropertyDefinition.findWhere(
                                name: params.pd_name,
                                type: params.pd_type,
                                tenant: result.institution,
                                descr: PropertyDefinition.SVY_PROP
                        )

                        if ((!surveyProperty) && params.pd_name && params.pd_type) {
                            RefdataCategory rdc
                            if (params.refdatacategory) {
                                rdc = RefdataCategory.findById(params.long('refdatacategory'))
                            }

                            Map<String, Object> map = [
                                    token   : params.pd_name,
                                    category: PropertyDefinition.SVY_PROP,
                                    type    : params.pd_type,
                                    rdc     : rdc ? rdc.getDesc() : null,
                                    tenant  : result.institution.globalUID,
                                    i10n    : [
                                            name_de: params.pd_name,
                                            name_en: params.pd_name,
                                            expl_de: params.pd_expl,
                                            expl_en: params.pd_expl
                                    ]
                            ]

                            if (PropertyDefinition.construct(map)) {

                            } else {
                                result.error = messageSource.getMessage('surveyProperty.create.fail', null, result.locale) as String
                            }
                        } else if (surveyProperty) {
                            result.error = messageSource.getMessage('surveyProperty.create.exist', null, result.locale) as String
                        } else {
                            result.error = messageSource.getMessage('surveyProperty.create.fail', null, result.locale) as String
                        }
                        break
                    case "deleteSurveyProperty":
                        PropertyDefinition surveyProperty = PropertyDefinition.findByIdAndTenant(params.deleteId, result.institution)


                        if (surveyProperty.countUsages() == 0 && surveyProperty.tenant.id == result.institution.id) {
                                surveyProperty.delete()
                                //result.message = messageSource.getMessage('default.deleted.message', args:[messageSource.getMessage('surveyProperty.label'), surveyProperty.getI10n('name')])
                        }
                        break
                    case "moveUp":
                    case "moveDown":

                        //Reorder in PropertyDefinitionGroup
                        List<Long> surveyPropertiesIDs = Params.getLongList(params, 'surveyPropertiesIDs')
                        SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.get(params.surveyPropertyConfigId)
                        Set<SurveyConfigProperties> sequence = SurveyConfigProperties.executeQuery("select scp from SurveyConfigProperties scp where scp.surveyConfig = :surveyConfig and scp.id in (:surveyPropertiesIDs) order by scp.propertyOrder", [surveyPropertiesIDs: surveyPropertiesIDs, surveyConfig: result.surveyConfig]) as Set<SurveyConfigProperties>

                        int idx = sequence.findIndexOf { it.id == surveyConfigProperties.id }
                        int pos = surveyConfigProperties.propertyOrder
                        SurveyConfigProperties surveyConfigProperties2

                        if (params.actionForSurveyProperty == 'moveUp') {
                            surveyConfigProperties2 = sequence.getAt(idx - 1)
                        } else if (params.actionForSurveyProperty == 'moveDown') {
                            surveyConfigProperties2 = sequence.getAt(idx + 1)
                        }

                        if (surveyConfigProperties2) {
                            surveyConfigProperties.propertyOrder = surveyConfigProperties2.propertyOrder
                            surveyConfigProperties.save()
                            surveyConfigProperties2.propertyOrder = pos
                            surveyConfigProperties2.save()
                        }

                        //Reorder in surveyconfig
                        LinkedHashSet groupedProperties = []
                        List<SurveyConfigProperties> surveyProperties = []
                        Map<String, Object> allPropDefGroups = result.surveyConfig.getCalculatedPropDefGroups(result.surveyInfo.owner)
                        allPropDefGroups.sorted.each{ def entry ->
                            PropertyDefinitionGroup pdg = entry[1]
                            LinkedHashSet<SurveyConfigProperties> orderSurveyProperties = result.surveyConfig.getSurveyConfigPropertiesByPropDefGroup(pdg)
                            if(orderSurveyProperties){
                                orderSurveyProperties = orderSurveyProperties.sort {it.propertyOrder}
                                surveyProperties = surveyProperties + orderSurveyProperties
                            }

                            groupedProperties << orderSurveyProperties
                        }

                        if(groupedProperties) {
                            LinkedHashSet<SurveyConfigProperties> orderSurveyProperties = result.surveyConfig.getOrphanedSurveyConfigProperties(groupedProperties)
                            if(orderSurveyProperties){
                                orderSurveyProperties = orderSurveyProperties.sort {it.propertyOrder}
                                surveyProperties = surveyProperties + orderSurveyProperties
                            }
                        }

                        LinkedHashSet<SurveyConfigProperties> orderSurveyProperties = result.surveyConfig.getPrivateSurveyConfigProperties()
                        if(orderSurveyProperties){
                            orderSurveyProperties = orderSurveyProperties.sort {it.propertyOrder}
                            surveyProperties = surveyProperties + orderSurveyProperties
                        }

                        surveyProperties.eachWithIndex{ SurveyConfigProperties surveyConfigProperty, int i ->
                            surveyConfigProperty.propertyOrder = i+1
                            surveyConfigProperty.save()
                        }

                        break
                }
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Adds the given institutions to the given survey as new participants
     * @return the updated survey participants list
     */
    Map<String, Object> actionSurveyParticipants(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            result.editable = (result.surveyInfo && result.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]) ? result.editable : false

            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
            }

            switch (params.actionSurveyParticipants) {
                case "addSurveyParticipants":
                    List<Org> members = []


                    if(params.selectMembersWithImport?.filename){

                        MultipartFile importFile = params.selectMembersWithImport
                        InputStream stream = importFile.getInputStream()

                        result.selectMembersWithImport = surveyService.selectSurveyMembersWithImport(stream)

                        if(result.selectMembersWithImport.orgList){
                            result.selectMembersWithImport.orgList.each { it ->
                                members << Org.findById(Long.valueOf(it.orgId))
                            }
                        }


                    }else {
                        params.list('selectedOrgs').each { it ->
                            members << Org.findById(Long.valueOf(it))
                        }
                    }

                    members.each { Org org ->
                            //boolean selectable = surveyService.selectableDespiteMultiYearTerm(result.surveyConfig, org)

                            if (!(SurveyOrg.findAllBySurveyConfigAndOrg(result.surveyConfig, org))) {
                                SurveyOrg surveyOrg = new SurveyOrg(
                                        surveyConfig: result.surveyConfig,
                                        org: org
                                )

                                if (!surveyOrg.save()) {
                                    log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}");
                                } else {
                                    if (result.surveyInfo.status in [RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]) {
                                        result.surveyConfig.surveyProperties.each { SurveyConfigProperties property ->

                                            SurveyResult surveyResult = new SurveyResult(
                                                    owner: result.institution,
                                                    participant: org ?: null,
                                                    startDate: result.surveyInfo.startDate,
                                                    endDate: result.surveyInfo.endDate ?: null,
                                                    type: property.surveyProperty,
                                                    surveyConfig: result.surveyConfig
                                            )

                                            if (surveyResult.save()) {
                                                //log.debug( surveyResult.toString() )
                                            } else {
                                                log.error("Not create surveyResult: " + surveyResult)
                                            }
                                        }

                                        if (result.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED) {
                                            surveyService.emailsToSurveyUsersOfOrg(result.surveyInfo, org, false)
                                        }


                                        surveyService.setDefaultPreferredConcatsForSurvey(result.surveyConfig, org)


                                        if(result.surveyConfig.pickAndChoose){
                                            Subscription participantSub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)
                                            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, participantSub)
                                            if (!issueEntitlementGroup && participantSub) {
                                                String groupName = IssueEntitlementGroup.countBySubAndName(participantSub, result.surveyConfig.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(participantSub, result.surveyConfig.issueEntitlementGroupName) + 1) : result.surveyConfig.issueEntitlementGroupName
                                                new IssueEntitlementGroup(surveyConfig: result.surveyConfig, sub: participantSub, name: groupName).save()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        result.surveyConfig.save()
                    break
                case "deleteSurveyParticipants":
                    if (params.selectedOrgs) {
                        params.list('selectedOrgs').each { soId ->
                            SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, Org.get(Long.parseLong(soId)))

                            CostItem.findAllBySurveyOrg(surveyOrg).each {
                                it.delete()
                            }

                            SurveyResult.findAllBySurveyConfigAndParticipant(result.surveyConfig, surveyOrg.org).each {
                                it.delete()
                            }

                            SurveyPackageResult.findAllBySurveyConfigAndParticipant(result.surveyConfig, surveyOrg.org).each {
                                it.delete()
                            }

                            SurveyVendorResult.findAllBySurveyConfigAndParticipant(result.surveyConfig, surveyOrg.org).each {
                                it.delete()
                            }

                            SurveyPersonResult.findAllBySurveyConfigAndParticipant(result.surveyConfig, surveyOrg.org).each {
                                it.delete()
                            }

                            if (surveyOrg.delete()) {
                                //result.message = messageSource.getMessage("surveyParticipants.delete.successfully", null, result.locale)
                            }
                        }
                    }
                    break
                case "addSubMembersToSurvey":
                    surveyService.addSubMembers(result.surveyConfig)
                    break
                case "addMultiYearSubMembersToSurvey":
                    surveyService.addMultiYearSubMembers(result.surveyConfig)
                    break
            }
        }
        [result: result, status: STATUS_OK]

    }

    /**
     * Deletes the entire survey with attached objects
     * @return the survey list in case of success, a redirect to the referer otherwise
     */
    Map<String, Object> deleteSurveyInfo(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result.editable = (result.surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])

            if (result.editable) {

                try {

                    SurveyInfo surveyInfo = SurveyInfo.get(result.surveyInfo.id)

                    DocContext.executeUpdate("delete from DocContext dc where dc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    CostItem.executeUpdate("delete from CostItem ct where ct.surveyOrg.id in (:surveyOrgIDs)", [surveyOrgIDs: SurveyOrg.findAllBySurveyConfigInList(SurveyConfig.findAllBySurveyInfo(surveyInfo)).id])

                    SurveyOrg.executeUpdate("delete from SurveyOrg so where so.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyResult.executeUpdate("delete from SurveyResult sr where sr.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    Task.executeUpdate("delete from Task ta where ta.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyConfigProperties.executeUpdate("delete from SurveyConfigProperties scp where scp.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyPackageResult.executeUpdate("delete from SurveyPackageResult sc where sc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyVendorResult.executeUpdate("delete from SurveyVendorResult sc where sc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyConfigPackage.executeUpdate("delete from SurveyConfigPackage sc where sc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyConfigVendor.executeUpdate("delete from SurveyConfigVendor sc where sc.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyUrl.executeUpdate("delete from SurveyUrl surU where surU.surveyConfig.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    SurveyLinks.executeUpdate("delete from SurveyLinks srL where srL.sourceSurvey.id = :surveyInfo or srL.targetSurvey.id = :surveyInfo", [surveyInfo: surveyInfo.id])

                    SurveyConfig.executeUpdate("delete from SurveyConfig sc where sc.id in (:surveyConfigIDs)", [surveyConfigIDs: SurveyConfig.findAllBySurveyInfo(surveyInfo).id])

                    surveyInfo.delete()


                    result.message = messageSource.getMessage('surveyInfo.delete.successfully', null, result.locale) as String
                }
                catch (DataIntegrityViolationException e) {
                    result.error = messageSource.getMessage('surveyInfo.delete.fail', null, result.locale) as String
                }
            }

            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Set status with new status
     * @return a {@link Map}
     */
    Map<String, Object> setStatus(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }
            switch (params.newStatus) {
                case "setInEvaluation":
                    result.surveyInfo.status = RDStore.SURVEY_IN_EVALUATION
                    result.surveyInfo.save()
                    break
                case "setCompleted":
                    result.surveyInfo.status = RDStore.SURVEY_COMPLETED
                    result.surveyInfo.save()
                    break
                case "setCompletedSurvey":
                        result.surveyInfo.status = RDStore.SURVEY_SURVEY_COMPLETED
                        result.surveyInfo.save()
                    break
                case "setSurveyCompleted":
                    result.surveyInfo.status = params.surveyCompleted ? RDStore.SURVEY_COMPLETED : RDStore.SURVEY_IN_EVALUATION
                    result.surveyInfo.save()
                    break
                case "processBackInProcessingSurvey":
                        result.surveyInfo.status = RDStore.SURVEY_IN_PROCESSING
                        result.surveyInfo.save()
                    break

                case "openSurveyAgain":
                    if (result.surveyInfo && result.surveyInfo.status.id in [RDStore.SURVEY_IN_EVALUATION.id, RDStore.SURVEY_COMPLETED.id, RDStore.SURVEY_SURVEY_COMPLETED.id]) {
                            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                            Date endDate = params.newEndDate ? sdf.parse(params.newEndDate) : null

                            if (result.surveyInfo.startDate != null && endDate != null) {
                                if (result.surveyInfo.startDate > endDate) {
                                    result.error = messageSource.getMessage("openSurveyAgain.fail.startDateAndEndDate", null, result.locale)
                                    return
                                }
                            }
                            result.surveyInfo.status = RDStore.SURVEY_SURVEY_STARTED
                            result.surveyInfo.endDate = endDate
                            result.surveyInfo.save()
                    }
                    break
                case "processEndSurvey":
                    result.surveyInfo.status = RDStore.SURVEY_IN_EVALUATION
                    result.surveyInfo.save()
                    result.message = messageSource.getMessage("endSurvey.successfully", null, result.locale)
                    break
                case "processOpenSurvey":
                    boolean openFailByTitleSelection = false

                    Date startDate = params.startNow ? new Date() : result.surveyInfo.startDate


                    result.surveyInfo.surveyConfigs.each { config ->
                        config.orgs.org.each { org ->
                            if (result.surveyInfo.type == RDStore.SURVEY_TYPE_TITLE_SELECTION) {
                                Subscription subscription = config.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)

                                if (subscription.packages.size() == 0) {
                                    openFailByTitleSelection = true
                                }
                            }

                            if (!openFailByTitleSelection) {
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
                                            //log.debug(surveyResult.toString())
                                        } else {
                                            log.error("Not create surveyResult: " + surveyResult)
                                        }
                                    }
                                }


                                surveyService.setDefaultPreferredConcatsForSurvey(config, org)


                                if(config.pickAndChoose){
                                    Subscription participantSub = config.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)
                                    IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(config, participantSub)
                                    if (!issueEntitlementGroup && participantSub) {
                                        String groupName = IssueEntitlementGroup.countBySubAndName(participantSub, config.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(participantSub, config.issueEntitlementGroupName) + 1) : config.issueEntitlementGroupName
                                        new IssueEntitlementGroup(surveyConfig: config, sub: participantSub, name: groupName).save()
                                    }
                                }
                            }
                        }
                    }

                    if (!openFailByTitleSelection) {
                        result.surveyInfo.status = params.startNow ? RDStore.SURVEY_SURVEY_STARTED : RDStore.SURVEY_READY
                        result.surveyInfo.startDate = startDate
                        result.surveyInfo.save()
                        result.message = params.startNow ? messageSource.getMessage("openSurveyNow.successfully", null, result.locale) : messageSource.getMessage("openSurvey.successfully", null, result.locale)
                    } else {
                        result.error = messageSource.getMessage("openSurvey.openFailByTitleSelection.noPackagesYetAdded", null, result.locale)
                    }


                    if (!openFailByTitleSelection && params.startNow) {
                        executorService.execute({
                            Thread.currentThread().setName('EmailsToSurveyUsers' + result.surveyInfo.id)
                            surveyService.emailsToSurveyUsers([result.surveyInfo.id])
                        })
                    }

                    break
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Sets the given comment for the given survey
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> setSurveyConfigComment(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            if(params.commentTyp == 'comment') {
                result.surveyConfig.comment = params.comment
            }
            if(params.commentTyp == 'commentForNewParticipants') {
                result.surveyConfig.commentForNewParticipants = params.commentForNewParticipants
            }


            if (!result.surveyConfig.save()) {
                result.error = messageSource.getMessage('default.save.error.general.message', null, result.locale)
            }


            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Call to copy the given survey
     * @return the view with the base parameters for the survey copy
     */
    Map<String, Object> copySurvey(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            SwissKnife.setPaginationParams(result, params, (User) result.user)

            if (result.surveyInfo.type.id == RDStore.SURVEY_TYPE_INTEREST.id) {
                result.workFlow = '2'
            } else {
                if (params.targetSubs) {
                    result.workFlow = '2'
                } else {
                    result.workFlow = '1'
                }
            }

            if (result.workFlow == '1') {
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

            if (result.surveyConfig.subscription) {
                String sourceLicensesQuery = "select li.sourceLicense from Links li where li.destinationSubscription = :sub and li.linkType = :linkType order by li.sourceLicense.sortableReference asc"
                result.sourceLicenses = License.executeQuery(sourceLicensesQuery, [sub: result.surveyConfig.subscription, linkType: RDStore.LINKTYPE_LICENSE])
            }

            result.targetSubs = params.targetSubs ? Subscription.findAllByIdInList(Params.getLongList(params, 'targetSubs')) : null

            [result: result, status: STATUS_OK]
        }

    }



    /**
     * Takes the submitted base parameters and creates a copy of the given survey
     * @return either the survey list view for consortia or the survey details view
     */
    Map<String, Object> processCopySurvey(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            SurveyInfo baseSurveyInfo = result.surveyInfo
            SurveyConfig baseSurveyConfig = result.surveyConfig

            if (baseSurveyInfo && baseSurveyConfig) {

                result.targetSubs = params.targetSubs ? Subscription.findAllByIdInList(Params.getLongList(params, 'targetSubs')) : null

                List newSurveyIds = []

                if (result.targetSubs) {

                        result.targetSubs.each { sub ->
                            SurveyInfo newSurveyInfo = new SurveyInfo(
                                    name: sub.name,
                                    status: RDStore.SURVEY_IN_PROCESSING,
                                    type: (baseSurveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL) ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sub, true) ? RDStore.SURVEY_TYPE_SUBSCRIPTION : baseSurveyInfo.type) : baseSurveyInfo.type,
                                    startDate: params.copySurvey.copyDates ? baseSurveyInfo.startDate : null,
                                    endDate: params.copySurvey.copyDates ? baseSurveyInfo.endDate : null,
                                    comment: params.copySurvey.copyComment ? baseSurveyInfo.comment : null,
                                    isMandatory: (baseSurveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL) ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sub, true) ? (params.copySurvey.copyMandatory ? baseSurveyInfo.isMandatory : false) : true) : (params.copySurvey.copyMandatory ? baseSurveyInfo.isMandatory : false),
                                    owner: contextService.getOrg(),
                                    isSubscriptionSurvey: (baseSurveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL) ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sub, true) ? false : true) : false,
                                    license: params.copySurvey.copyLicense ? baseSurveyInfo.license : null,
                                    provider: params.copySurvey.copyProvider ? baseSurveyInfo.provider : null,
                            ).save()

                            SurveyConfig newSurveyConfig = new SurveyConfig(
                                    type: baseSurveyConfig.type,
                                    subscription: sub,
                                    surveyInfo: newSurveyInfo,
                                    comment: params.copySurvey.copySurveyConfigComment ? baseSurveyConfig.comment : null,
                                    commentForNewParticipants: params.copySurvey.copySurveyConfigCommentForNewParticipants ? baseSurveyConfig.commentForNewParticipants : null,
                                    configOrder: newSurveyInfo.surveyConfigs ? newSurveyInfo.surveyConfigs.size() + 1 : 1,
                                    subSurveyUseForTransfer: (baseSurveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL) ? (SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sub, true) ? false : true) : false,
                                    vendorSurvey: (params.copySurvey.copyVendorSurvey ? baseSurveyConfig.vendorSurvey : false),
                                    packageSurvey: (params.copySurvey.copyPackageSurvey ? baseSurveyConfig.packageSurvey : false),
                                    invoicingInformation: (params.copySurvey.copyInvoicingInformation ? baseSurveyConfig.invoicingInformation : false)
                            ).save()

                            surveyService.copySurveyConfigCharacteristic(baseSurveyConfig, newSurveyConfig, params)

                            newSurveyIds << newSurveyInfo.id

                        }

                   result.newSurveyIds = newSurveyIds
                } else {
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
                                configOrder: newSurveyInfo.surveyConfigs ? newSurveyInfo.surveyConfigs.size() + 1 : 1,
                                vendorSurvey: (params.copySurvey.copyVendorSurvey ? baseSurveyConfig.vendorSurvey : false),
                                packageSurvey: (params.copySurvey.copyPackageSurvey ? baseSurveyConfig.packageSurvey : false),
                                invoicingInformation: (params.copySurvey.copyInvoicingInformation ? baseSurveyConfig.invoicingInformation : false)
                        ).save()
                        surveyService.copySurveyConfigCharacteristic(baseSurveyConfig, newSurveyConfig, params)
                    result.newSurveyInfo = newSurveyInfo
                    result.newSurveyConfig = newSurveyConfig
                }
            }
            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Initialises the subscription renewal for the parent subscription after a survey
     * @return the view for the successor subscription base parameter's configuration
     */
    Map<String, Object> renewSubscriptionConsortiaWithSurvey(GrailsParameterMap params) {

        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            Subscription subscription = Subscription.get(params.sourceSubId ?: null)

            SimpleDateFormat sdf = DateUtils.getSDF_ddMMyyyy()

            result.errors = []
            Date newStartDate
            Date newEndDate
            Year newReferenceYear = subscription.referenceYear ? subscription.referenceYear.plusYears(1) : null
            use(TimeCategory) {
                newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
                newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
            }
            params.surveyConfig = params.surveyConfig ?: null
            result.isRenewSub = true
            result.permissionInfo = [sub_startDate          : newStartDate ? sdf.format(newStartDate) : null,
                                     sub_endDate            : newEndDate ? sdf.format(newEndDate) : null,
                                     sub_referenceYear      : newReferenceYear,
                                     sub_name               : subscription.name,
                                     sub_id                 : subscription.id,
                                     sub_status             : RDStore.SUBSCRIPTION_INTENDED.id,
                                     sub_type               : subscription.type?.id,
                                     sub_form               : subscription.form?.id,
                                     sub_resource           : subscription.resource?.id,
                                     sub_kind               : subscription.kind?.id,
                                     sub_isPublicForApi     : subscription.isPublicForApi ? RDStore.YN_YES.id : RDStore.YN_NO.id,
                                     sub_hasPerpetualAccess : subscription.hasPerpetualAccess ? RDStore.YN_YES.id : RDStore.YN_NO.id,
                                     sub_hasPublishComponent: subscription.hasPublishComponent ? RDStore.YN_YES.id : RDStore.YN_NO.id,
                                     sub_holdingSelection   : subscription.holdingSelection?.id

            ]

            result.sourceSubscription = subscription
            result.parentSub = result.surveyConfig.subscription
            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Takes the submitted input and creates a successor subscription instance. The successor is being automatically
     * linked to the predecessor instance. The element copy workflow is triggered right after
     * @return the subscription element copy starting view
     */
    def processRenewalWithSurvey(GrailsParameterMap params) {

        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {

            Subscription baseSub = Subscription.get(params.sourceSubId ?: null)

            if(!baseSub){
                [result: null, status: STATUS_ERROR]
            }

            ArrayList<Links> previousSubscriptions = Links.findAllByDestinationSubscriptionAndLinkType(baseSub, RDStore.LINKTYPE_FOLLOWS)
            if (previousSubscriptions.size() > 0) {
                result.error = messageSource.getMessage('subscription.renewSubExist', null, result.locale) as String
            } else {
                Date sub_startDate = params.subscription.start_date ? DateUtils.parseDateGeneric(params.subscription.start_date) : null
                Date sub_endDate = params.subscription.end_date ? DateUtils.parseDateGeneric(params.subscription.end_date) : null
                Year sub_refYear = params.subscription.reference_year ? Year.parse(params.subscription.reference_year) : null
                def sub_status = params.subStatus
                RefdataValue sub_type = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL
                def sub_kind = params.subKind
                def sub_form = params.subForm
                def sub_resource = params.subResource
                boolean sub_hasPerpetualAccess = params.long('subHasPerpetualAccess') == RDStore.YN_YES.id
                boolean sub_hasPublishComponent = params.long('subHasPublishComponent') == RDStore.YN_YES.id
                boolean sub_isPublicForApi = params.long('subIsPublicForApi') == RDStore.YN_YES.id
                def sub_holdingSelection = params.subHoldingSelection
                def new_subname = params.subscription.name
                def manualCancellationDate = null

                use(TimeCategory) {
                    manualCancellationDate = baseSub.manualCancellationDate ? (baseSub.manualCancellationDate + 1.year) : null
                }
                    Subscription newSub = new Subscription(
                            name: new_subname,
                            startDate: sub_startDate,
                            endDate: sub_endDate,
                            referenceYear: sub_refYear,
                            manualCancellationDate: manualCancellationDate,
                            identifier: RandomUtils.getUUID(),
                            type: sub_type,
                            kind: sub_kind,
                            status: sub_status,
                            resource: sub_resource,
                            form: sub_form,
                            hasPerpetualAccess: sub_hasPerpetualAccess,
                            hasPublishComponent: sub_hasPublishComponent,
                            holdingSelection: sub_holdingSelection,
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

                    }

            }
            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Takes the submitted input and creates cost items based on the given parameters for every selected survey participant
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String, Object> createSurveyCostItem(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }
            SimpleDateFormat dateFormat = DateUtils.getLocalizedSDF_noTime()

            boolean costItemsForSurveyPackage = params.selectPkg == "true" ? true : false

            CostItem newCostItem = null
            result.putAll(financeControllerService.getEditVars(result.institution))

            try {
                log.debug("SurveyController::createSurveyCostItem() ${params}");

                result.error = [] as List

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
                boolean billingSumRounding = params.newBillingSumRounding ? true : false, finalCostRounding = params.newFinalCostRounding ? true : false
                Double cost_billing_currency = params.newCostInBillingCurrency ? format.parse(params.newCostInBillingCurrency).doubleValue() : 0.00
                //Double cost_currency_rate = 1.0
                //if(billing_currency != RDStore.CURRENCY_EUR) {
                //    cost_currency_rate = params.newCostCurrencyRate ? params.double('newCostCurrencyRate', 1.00) : 0.00
                //}
                //def cost_local_currency = params.newCostInLocalCurrency ? format.parse(params.newCostInLocalCurrency).doubleValue() : 0.00

                Double cost_billing_currency_after_tax = params.newCostInBillingCurrencyAfterTax ? format.parse(params.newCostInBillingCurrencyAfterTax).doubleValue() : cost_billing_currency
                if (billingSumRounding)
                    cost_billing_currency = Math.round(cost_billing_currency)
                if (finalCostRounding)
                    cost_billing_currency_after_tax = Math.round(cost_billing_currency_after_tax)
                //Double cost_local_currency = cost_billing_currency * cost_currency_rate
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
                RefdataValue cost_item_element_configuration = (params.ciec && params.ciec != 'null') ? RefdataValue.get(params.long('ciec')) : null

                boolean cost_item_isVisibleForSubscriber = false
                // (params.newIsVisibleForSubscriber ? (RefdataValue.get(params.newIsVisibleForSubscriber).value == 'Yes') : false)

                Package pkg
                if(costItemsForSurveyPackage){
                    pkg = params.newPackage ? Package.get(params.long('newPackage')) : null
                    if(pkg && !SurveyConfigPackage.findBySurveyConfigAndPkg(result.surveyConfig, pkg)){
                        pkg = null
                    }
                }

                List surveyOrgsDo = []

                List surveyOrgs = []

                if (params.surveyOrg) {
                    surveyOrgs << params.surveyOrg

                }

                if (params.get('surveyOrgs')) {
                    surveyOrgs = (params.get('surveyOrgs').split(',').collect {
                        String.valueOf(it.replaceAll("\\s", ""))
                    }).toList()
                }

                surveyOrgs.each {
                    try {

                        SurveyOrg surveyOrg = genericOIDService.resolveOID(it)
                        if(surveyOrg) {
                            if (params.oldCostItem) {
                                CostItem costItem = genericOIDService.resolveOID(params.oldCostItem)
                                if (costItem.surveyOrg == surveyOrg) {
                                    surveyOrgsDo << surveyOrg
                                }
                            }else{
                                if (costItemsForSurveyPackage) {
                                    if (pkg) {
                                        if (cost_item_element) {
                                            if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, cost_item_element, pkg)) {
                                                surveyOrgsDo << surveyOrg
                                            }
                                        } else {
                                            if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, pkg)) {
                                                surveyOrgsDo << surveyOrg
                                            }
                                        }
                                    }
                                } else {
                                    if (cost_item_element) {
                                        if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED, cost_item_element)) {
                                            surveyOrgsDo << surveyOrg
                                        }
                                    } else {
                                        if (!CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED)) {
                                            surveyOrgsDo << surveyOrg
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        log.error("Non-valid surveyOrg sent ${it}", e)
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
                    surveyOrgsDo.each { surveyOrg ->

                        boolean selectableDespiteMultiYearTerm = true //surveyService.selectableDespiteMultiYearTerm(surveyOrg.surveyConfig, surveyOrg.org)
                        if (selectableDespiteMultiYearTerm) {

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

                            newCostItem.billingSumRounding = billingSumRounding
                            newCostItem.finalCostRounding = finalCostRounding
                            newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
                            //newCostItem.costInLocalCurrencyAfterTax = cost_local_currency_after_tax as Double calculated on the fly
                            //newCostItem.currencyRate = cost_currency_rate as Double
                            //newCostItem.taxRate = new_tax_rate as Integer -> to taxKey
                            newCostItem.taxKey = tax_key
                            newCostItem.costItemElementConfiguration = cost_item_element_configuration

                            newCostItem.costDescription = params.newDescription ? params.newDescription.trim() : null

                            newCostItem.startDate = startDate ?: null
                            newCostItem.endDate = endDate ?: null

                            if(costItemsForSurveyPackage && pkg){
                                newCostItem.pkg = pkg
                            }

                            //newCostItem.includeInSubscription = null
                            //todo Discussion needed, nobody is quite sure of the functionality behind this...


                            if (!newCostItem.validate()) {
                                result.error = newCostItem.errors.allErrors.collect {
                                    log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                                    Object[] args = [it.properties.field]
                                    messageSource.getMessage('finance.addNew.error', args, result.locale)
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
            catch (Exception e) {
                log.error("Problem in add cost item", e);
            }
            [result: result, status: STATUS_OK]
        }
    }

    /**
     * Call for the transfer view of the participants in a renewal survey
     * @return a list of members for each subscription
     */
    Map<String, Object> compareMembersOfTwoSubs(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            result.superOrgType = []
            if (contextService.getOrg().isCustomerType_Consortium_Pro()) {
                result.superOrgType << messageSource.getMessage('consortium.superOrgType', null, result.locale)
            }

            result.participantsList = []

            result.parentParticipantsList = []
            result.parentSuccessortParticipantsList = []

            result.parentSubChilds.each { sub ->
                Org org = sub.getSubscriberRespConsortia()
                result.participantsList << org
                result.parentParticipantsList << org

            }

            result.parentSuccessorSubChilds.each { sub ->
                Org org = sub.getSubscriberRespConsortia()
                if (!(result.participantsList && org.id in result.participantsList.id)) {
                    result.participantsList << org
                }
                result.parentSuccessortParticipantsList << org

            }

            result.participantsList = result.participantsList.sort { it.sortname }


            result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION
            if (result.surveyConfig.subSurveyUseForTransfer && result.parentSuccessorSubscription) {
                String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
                result.memberLicenses = License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE])
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to copy the packages of subscription
     * @return a list of each participant's packages
     */
    Map<String, Object> copySubPackagesAndIes(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            result.participantsList = []

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = sub._getCalculatedPreviousForSurvey()

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            result.validPackages = result.parentSuccessorSubscription ? Package.executeQuery('select sp from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: result.parentSuccessorSubscription]) : []

            result.isLinkingRunning = subscriptionService.checkThreadRunning('PackageTransfer_' + result.parentSuccessorSubscription.id)

            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> copySurveyPackages(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            if('all' in params.list('selectedPackages') && params.list('selectedPackages').size() > 1){
                List selectedPackages = []
                params.list('selectedPackages').each {
                    if(it != 'all'){
                        selectedPackages << it
                    }
                }
                params.selectedPackages = selectedPackages
            }else {
                params.selectedPackages = params.selectedPackages ?: 'all'
            }

            List packages = []
            if(!('all' in params.list('selectedPackages')) && params.list('selectedPackages')){
                params.list('selectedPackages').each {
                    if(it != 'all'){
                        packages << Package.get(Long.valueOf(it))
                    }
                }
            }else {
                packages = SurveyConfigPackage.findAllBySurveyConfig(result.surveyConfig).pkg
            }

            result.participantsList = []

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = sub._getCalculatedPreviousForSurvey()
                newMap.surveyPackages = SurveyPackageResult.executeQuery("select spr.pkg from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant = :participant and spr.pkg in (:pkgs)", [pkgs: packages, surveyConfig: result.surveyConfig, participant: org])

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            result.isLinkingRunning = subscriptionService.checkThreadRunning('CopySurPkgs_' + result.parentSuccessorSubscription.id)

            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> copySurveyVendors(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            result.participantsList = []

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = sub._getCalculatedPreviousForSurvey()
                newMap.surveyVendors = SurveyVendorResult.executeQuery("select svr.vendor from SurveyVendorResult svr where svr.surveyConfig = :surveyConfig and svr.participant = :participant", [surveyConfig: result.surveyConfig, participant: org])

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> proccessCopySubPackagesAndIes(GrailsParameterMap params) {
        Map<String, Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            [result: null, status: STATUS_ERROR]
            return
        }

        result = surveyControllerService.getSubResultForTranfser(result, params)

        if (!subscriptionService.checkThreadRunning('PackageTransfer_' + result.parentSuccessorSubscription.id)) {
            String processOption = params.processOption
            Set<Subscription> subscriptions, permittedSubs = []
            if (params.containsKey("membersListToggler")) {
                subscriptions = result.parentSuccessorSubChilds
            } else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            subscriptions.each { Subscription selectedSub ->
                if (selectedSub.isEditableBy(result.user)) {
                    permittedSubs << selectedSub
                }
            }
            List selectedPackageKeys = params.list("selectedPackages")
            Set<Package> pkgsToProcess = []
            if (selectedPackageKeys.contains('all') && result.parentSuccessorSubscription) {
                pkgsToProcess.addAll(Package.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: result.parentSuccessorSubscription]))
            } else {
                selectedPackageKeys.each { String pkgKey ->
                    pkgsToProcess.add(Package.get(pkgKey))
                }
            }
            executorService.execute({
                Thread.currentThread().setName('PackageTransfer_' + result.parentSuccessorSubscription.id)
                pkgsToProcess.each { Package pkg ->
                    permittedSubs.each { Subscription selectedSub ->
                        SubscriptionPackage sp = SubscriptionPackage.findBySubscriptionAndPkg(selectedSub, pkg)
                        if (processOption =~ /^link/) {
                            if (!sp) {
                               /* if (result.parentSuccessorSubscription) {
                                    subscriptionService.addToSubscriptionCurrentStock(selectedSub, result.parentSuccessorSubscription, pkg, processOption == 'linkwithIE')
                                } else {
                                    subscriptionService.addToSubscription(selectedSub, pkg, processOption == 'linkwithIE')
                                }*/
                                subscriptionService.addToSubscription(selectedSub, pkg, processOption == 'linkwithIE')
                            }
                        }
                    }
                }
            })
        }

        [result: result, status: STATUS_OK]
    }

    Map<String, Object> proccessCopySurveyPackages(GrailsParameterMap params) {
        Map<String, Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            [result: null, status: STATUS_ERROR]
            return
        }

        result = surveyControllerService.getSubResultForTranfser(result, params)

        if (!subscriptionService.checkThreadRunning('CopySurPkgs_' + result.parentSuccessorSubscription.id)) {
            if('all' in params.list('selectedPackages') && params.list('selectedPackages').size() > 1){
                List selectedPackages = []
                params.list('selectedPackages').each {
                    if(it != 'all'){
                        selectedPackages << it
                    }
                }
                params.selectedPackages = selectedPackages
            }else {
                params.selectedPackages = params.selectedPackages ?: 'all'
            }

            List packages = []
            if(!('all' in params.list('selectedPackages')) && params.list('selectedPackages')){
                params.list('selectedPackages').each {
                    if(it != 'all'){
                        packages << Package.get(Long.valueOf(it))
                    }
                }
            }else {
                packages = SurveyConfigPackage.findAllBySurveyConfig(result.surveyConfig).pkg
            }

            Set<Subscription> subscriptions, permittedSubs = []
            if (params.containsKey("membersListToggler")) {
                subscriptions = result.parentSuccessorSubChilds
            } else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
            subscriptions.each { Subscription selectedSub ->
                if (selectedSub.isEditableBy(result.user)) {
                    permittedSubs << selectedSub
                }
            }

            boolean createEntitlements = params.createEntitlements == 'on'
           if(result.parentSuccessorSubscription && !auditService.getAuditConfig(result.parentSuccessorSubscription, 'holdingSelection')) {
               if (params.holdingSelection) {
                   RefdataValue holdingSelection = RefdataValue.get(params.holdingSelection)
                   permittedSubs.each { Subscription selectedSub ->
                       Org org = selectedSub.getSubscriberRespConsortia()
                       if(SurveyPackageResult.executeQuery("select count(*) from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant = :participant and spr.pkg in (:pkgs)", [pkgs: packages, surveyConfig: result.surveyConfig, participant: org])[0] > 0) {
                           selectedSub.holdingSelection = holdingSelection
                           selectedSub.save()
                       }
                   }
               }
           }


            executorService.execute({
                Thread.currentThread().setName('CopySurPkgs_' + result.parentSuccessorSubscription.id)
                    permittedSubs.each { Subscription selectedSub ->
                        selectedSub = selectedSub.refresh()
                        Org org = selectedSub.getSubscriberRespConsortia()
                        List<Package> surveyPackages = SurveyPackageResult.executeQuery("select spr.pkg from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant = :participant and spr.pkg in (:pkgs)", [pkgs: packages, surveyConfig: result.surveyConfig, participant: org])
                        surveyPackages.each { Package pkg ->
                        SubscriptionPackage sp = SubscriptionPackage.findBySubscriptionAndPkg(selectedSub, pkg)
                            if (!sp) {
                                subscriptionService.addToSubscription(selectedSub, pkg, createEntitlements)
                            }
                    }
                }
            })

        }


        [result: result, status: STATUS_OK]
    }

    Map<String, Object> proccessCopySurveyVendors(GrailsParameterMap params) {
        Map<String, Object> result = surveyControllerService.getResultGenericsAndCheckAccess(params)
        if (!result.editable) {
            [result: null, status: STATUS_ERROR]
            return
        }

        result = surveyControllerService.getSubResultForTranfser(result, params)

        Set<Subscription> subscriptions, permittedSubs = []
        if (params.containsKey("membersListToggler")) {
            subscriptions = result.parentSuccessorSubChilds
        } else subscriptions = Subscription.findAllByIdInList(params.list("selectedSubs"))
        subscriptions.each { Subscription selectedSub ->
            if (selectedSub.isEditableBy(result.user)) {
                permittedSubs << selectedSub
            }
        }


        permittedSubs.each { Subscription selectedSub ->
            Org org = selectedSub.getSubscriberRespConsortia()
            List<Vendor> vendorList = SurveyVendorResult.executeQuery("select svr.vendor from SurveyVendorResult svr where svr.surveyConfig = :surveyConfig and svr.participant = :participant", [surveyConfig: result.surveyConfig, participant: org])
            vendorList.each { Vendor vendor ->
                if (!VendorRole.findAllBySubscriptionAndVendor(selectedSub, vendor)) {
                    VendorRole new_link = new VendorRole(vendor: vendor, subscription: selectedSub)
                    if (!new_link.save()) {
                        log.error("Problem saving new vendor link ..")
                        new_link.errors.each { e ->
                            log.error(e.toString())
                        }
                    }
                }
            }
        }



        [result: result, status: STATUS_OK]
    }

    /**
     * Call to copy the survey cost items
     * @return a list of each participant's survey costs
     */
    Map<String, Object> copySurveyCostItems(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            if(params.selectedCostItemElementID){
                result.selectedCostItemElementID = Long.valueOf(params.selectedCostItemElementID)
            }else {
                List<RefdataValue> costItemElementsIds = CostItem.executeQuery('select ct.costItemElement.id from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null group by ct.costItemElement.id order by ct.costItemElement.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])
                if(costItemElementsIds.size() > 0){
                    if(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id in costItemElementsIds){
                        result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                    }else {
                        result.selectedCostItemElementID = costItemElementsIds[0]
                    }
                }else {
                    result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                }
            }

            result.selectedCostItemElement = RefdataValue.get(result.selectedCostItemElementID)

            result.participantsList = []

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = sub._getCalculatedPreviousForSurvey()

                newMap.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, org)
                newMap.surveyCostItem = newMap.surveyOrg ? CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(newMap.surveyOrg, RDStore.COST_ITEM_DELETED, result.selectedCostItemElement) : null

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            String query = 'from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null'

            result.costItemsByCostItemElement = CostItem.executeQuery(query, [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement }

            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> copySurveyCostItemPackage(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            if(params.selectedCostItemElementID){
                result.selectedCostItemElementID = Long.valueOf(params.selectedCostItemElementID)
            }else {
                List<RefdataValue> costItemElementsIds = CostItem.executeQuery('select ct.costItemElement.id from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null group by ct.costItemElement.id order by ct.costItemElement.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])
                if(costItemElementsIds.size() > 0){
                    if(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id in costItemElementsIds){
                        result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                    }else {
                        result.selectedCostItemElementID = costItemElementsIds[0]
                    }
                }else {
                    result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                }
            }

            result.selectedCostItemElement = RefdataValue.get(result.selectedCostItemElementID)

            result.selectedPackageID = params.selectedPackageID ? Long.valueOf(params.selectedPackageID) : CostItem.executeQuery('select ct.pkg.id from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null and ct.costItemElement = :costItemElement and ct.pkg is not null order by pkg.name', [costItemElement: RefdataValue.get(result.selectedCostItemElementID), status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])[0]
            result.pkg = Package.get(result.selectedPackageID)

            result.participantsList = []

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.org = org
                newMap.name = org.name
                newMap.newSub = sub
                newMap.oldSub = sub._getCalculatedPreviousForSurvey()

                newMap.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, org)
                newMap.surveyCostItem = newMap.surveyOrg && result.pkg ? CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkg(newMap.surveyOrg, RDStore.COST_ITEM_DELETED, result.selectedCostItemElement, result.pkg) : null

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            String query = 'from CostItem ct where ct.pkg is not null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig and surOrg.org in (:orgs)) and ct.costItemElement is not null'

            result.costItemsByPackages = []

            result.surveyConfig.surveyPackages.each{ SurveyConfigPackage surveyConfigPackage ->
                Map map = [:]
                map.pkg = surveyConfigPackage.pkg
                map.costItemsByCostItemElement = CostItem.executeQuery(query + ' and ct.pkg = :pkg', [pkg: surveyConfigPackage.pkg, status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig, orgs: result.participantsList.org]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement}
                result.costItemsByPackages << map
            }

            result.costItemsByPackages= result.costItemsByPackages.sort{it.pkg.name}

            [result: result, status: STATUS_OK]
        }

    }


    /**
     * Takes the given parameters and creates copies of the given cost items, based on the submitted data
     * @return the survey cost copy overview
     */
    Map<String, Object> proccessCopySurveyCostItems(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            Integer countNewCostItems = 0
            //RefdataValue costElement = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE

                params.list('selectedSurveyCostItem').each { costItemId ->

                    CostItem costItem = CostItem.get(costItemId)
                    Subscription participantSub = result.parentSuccessorSubscription?.getDerivedSubscriptionForNonHiddenSubscriber(costItem.surveyOrg.org)
                    List participantSubCostItem
                    if(result.surveyConfig.packageSurvey){
                        participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkgNotInList(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED, result.surveyConfig.surveyPackages.pkg)
                    }else {
                        participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED)
                    }

                      if (costItem && participantSub && !participantSubCostItem) {

                        Map properties = costItem.properties
                        CostItem copyCostItem = new CostItem()
                        InvokerHelper.setProperties(copyCostItem, properties)
                        copyCostItem.globalUID = null
                        copyCostItem.surveyOrg = null
                        copyCostItem.isVisibleForSubscriber = params.isVisibleForSubscriber ? true : false
                        copyCostItem.sub = participantSub

                        int taxRate = 0 //fallback
                        if (copyCostItem.taxKey)
                            taxRate = copyCostItem.taxKey.taxRate

                        if (copyCostItem.billingCurrency == RDStore.CURRENCY_EUR) {
                            copyCostItem.currencyRate = 1.0
                            copyCostItem.costInLocalCurrency = copyCostItem.costInBillingCurrency
                            copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrency ? copyCostItem.costInLocalCurrency * (1.0 + (0.01 * taxRate)) : null
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                        } else {
                            copyCostItem.currencyRate = 0.00
                            copyCostItem.costInLocalCurrency = null
                            copyCostItem.costInLocalCurrencyAfterTax = null
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                        }

                        if (copyCostItem.billingSumRounding) {
                            copyCostItem.costInBillingCurrency = copyCostItem.costInBillingCurrency ? Math.round(copyCostItem.costInBillingCurrency) : null
                            copyCostItem.costInLocalCurrency = copyCostItem.costInLocalCurrency ? Math.round(copyCostItem.costInLocalCurrency) : null
                        }
                        if (copyCostItem.finalCostRounding) {
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrencyAfterTax ? Math.round(copyCostItem.costInBillingCurrencyAfterTax) : null
                            copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrencyAfterTax ? Math.round(copyCostItem.costInLocalCurrencyAfterTax) : null
                        }

                        Org org = participantSub.getSubscriberRespConsortia()
                        SurveyResult surveyResult = org ? SurveyResult.findBySurveyConfigAndParticipantAndTypeAndStringValueIsNotNull(result.surveyConfig, org, PropertyStore.SURVEY_PROPERTY_ORDER_NUMBER) : null

                        if (surveyResult) {
                            Order order = new Order(orderNumber: surveyResult.getValue(), owner: result.institution)
                            if (order.save()) {
                                copyCostItem.order = order
                            } else log.error(order.errors)
                        }

                        if (copyCostItem.save()) {
                            countNewCostItems++
                        } else {
                            log.debug("Error by proccessCopySurveyCostItems: " + copyCostItem.errors)
                        }

                    }

                }

            result.countNewCostItems = countNewCostItems
            [result: result, status: STATUS_OK]
        }

    }

    Map<String, Object> proccessCopySurveyCostItemPackage(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            Integer countNewCostItems = 0
            //RefdataValue costElement = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE

            params.list('selectedSurveyCostItemPackage').each { costItemId ->

                CostItem costItem = CostItem.get(costItemId)
                Subscription participantSub = result.parentSuccessorSubscription?.getDerivedSubscriptionForNonHiddenSubscriber(costItem.surveyOrg.org)
                List participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkg(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED, costItem.pkg)
                if (costItem && participantSub && !participantSubCostItem) {

                    Map properties = costItem.properties
                    CostItem copyCostItem = new CostItem()
                    InvokerHelper.setProperties(copyCostItem, properties)
                    copyCostItem.globalUID = null
                    copyCostItem.surveyOrg = null
                    copyCostItem.isVisibleForSubscriber = params.isVisibleForSubscriber ? true : false
                    copyCostItem.sub = participantSub

                    int taxRate = 0 //fallback
                    if (copyCostItem.taxKey)
                        taxRate = copyCostItem.taxKey.taxRate

                    if (copyCostItem.billingCurrency == RDStore.CURRENCY_EUR) {
                        copyCostItem.currencyRate = 1.0
                        copyCostItem.costInLocalCurrency = copyCostItem.costInBillingCurrency
                        copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrency ? copyCostItem.costInLocalCurrency * (1.0 + (0.01 * taxRate)) : null
                        copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                    } else {
                        copyCostItem.currencyRate = 0.00
                        copyCostItem.costInLocalCurrency = null
                        copyCostItem.costInLocalCurrencyAfterTax = null
                        copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                    }

                    if (copyCostItem.billingSumRounding) {
                        copyCostItem.costInBillingCurrency = copyCostItem.costInBillingCurrency ? Math.round(copyCostItem.costInBillingCurrency) : null
                        copyCostItem.costInLocalCurrency = copyCostItem.costInLocalCurrency ? Math.round(copyCostItem.costInLocalCurrency) : null
                    }
                    if (copyCostItem.finalCostRounding) {
                        copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrencyAfterTax ? Math.round(copyCostItem.costInBillingCurrencyAfterTax) : null
                        copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrencyAfterTax ? Math.round(copyCostItem.costInLocalCurrencyAfterTax) : null
                    }

                    if (copyCostItem.save()) {
                        countNewCostItems++
                    } else {
                        log.debug("Error by proccessCopySurveyCostItems: " + copyCostItem.errors)
                    }

                }

            }

            result.countNewCostItems = countNewCostItems
            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to open the transfer of survey cost items into the respective member subscriptions
     * @return a list of participants with their respective survey cost items
     */
    Map<String, Object> copySurveyCostItemsToSub(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result.parentSubscription = result.surveyConfig.subscription
            result.parentSubChilds = result.parentSubscription ? subscriptionService.getValidSubChilds(result.parentSubscription) : null

            result.participantsList = []

            if(params.selectedCostItemElementID){
                result.selectedCostItemElementID = Long.valueOf(params.selectedCostItemElementID)
            }else {
                List<RefdataValue> costItemElementsIds = CostItem.executeQuery('select ct.costItemElement.id from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null group by ct.costItemElement.id order by ct.costItemElement.id', [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig])
                if(costItemElementsIds.size() > 0){
                    if(RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id in costItemElementsIds){
                        result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                    }else {
                        result.selectedCostItemElementID = costItemElementsIds[0]
                    }
                }else {
                    result.selectedCostItemElementID = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE.id
                }
            }

            result.selectedCostItemElement = RefdataValue.get(result.selectedCostItemElementID)

            result.parentSubChilds.each { sub ->
                Map newMap = [:]
                Org org = sub.getSubscriberRespConsortia()
                newMap.id = org.id
                newMap.sortname = org.sortname
                newMap.name = org.name
                newMap.newSub = sub

                newMap.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, org)
                newMap.surveyCostItem = newMap.surveyOrg ? CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(newMap.surveyOrg, RDStore.COST_ITEM_DELETED, result.selectedCostItemElement) : null

                result.participantsList << newMap

            }

            result.participantsList = result.participantsList.sort { it.sortname }

            String query = 'from CostItem ct where ct.pkg is null and ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = :surConfig) and ct.costItemElement is not null'

            result.costItemsByCostItemElement = CostItem.executeQuery(query, [status: RDStore.COST_ITEM_DELETED, surConfig: result.surveyConfig]).sort {it.costItemElement.getI10n('value')}.groupBy { it.costItemElement }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Takes the submitted parameters and copies the survey cost items into the subscriptions
     * @return the survey-subscription cost transfer view
     */
    Map<String, Object> proccessCopySurveyCostItemsToSub(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result.parentSubscription = result.surveyConfig.subscription


            Integer countNewCostItems = 0
            //RefdataValue costElement = RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE
                params.list('selectedSurveyCostItem').each { costItemId ->

                    CostItem costItem = CostItem.get(costItemId)
                    Subscription participantSub = result.parentSubscription?.getDerivedSubscriptionForNonHiddenSubscriber(costItem.surveyOrg.org)
                    List participantSubCostItem = CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNull(participantSub, result.institution, costItem.costItemElement, RDStore.COST_ITEM_DELETED)
                    if (costItem && participantSub && !participantSubCostItem) {

                        Map properties = costItem.properties
                        CostItem copyCostItem = new CostItem()
                        InvokerHelper.setProperties(copyCostItem, properties)
                        copyCostItem.globalUID = null
                        copyCostItem.surveyOrg = null
                        copyCostItem.isVisibleForSubscriber = params.isVisibleForSubscriber ? true : false
                        copyCostItem.sub = participantSub

                        int taxRate = 0 //fallback
                        if (copyCostItem.taxKey)
                            taxRate = copyCostItem.taxKey.taxRate

                        if (copyCostItem.billingCurrency == RDStore.CURRENCY_EUR) {
                            copyCostItem.currencyRate = 1.0
                            copyCostItem.costInLocalCurrency = copyCostItem.costInBillingCurrency
                            copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrency ? copyCostItem.costInLocalCurrency * (1.0 + (0.01 * taxRate)) : null
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                        } else {
                            copyCostItem.currencyRate = 0.00
                            copyCostItem.costInLocalCurrency = null
                            copyCostItem.costInLocalCurrencyAfterTax = null
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrency ? copyCostItem.costInBillingCurrency * (1.0 + (0.01 * taxRate)) : null
                        }

                        if (copyCostItem.billingSumRounding) {
                            copyCostItem.costInBillingCurrency = copyCostItem.costInBillingCurrency ? Math.round(copyCostItem.costInBillingCurrency) : null
                            copyCostItem.costInLocalCurrency = copyCostItem.costInLocalCurrency ? Math.round(copyCostItem.costInLocalCurrency) : null
                        }
                        if (copyCostItem.finalCostRounding) {
                            copyCostItem.costInBillingCurrencyAfterTax = copyCostItem.costInBillingCurrencyAfterTax ? Math.round(copyCostItem.costInBillingCurrencyAfterTax) : null
                            copyCostItem.costInLocalCurrencyAfterTax = copyCostItem.costInLocalCurrencyAfterTax ? Math.round(copyCostItem.costInLocalCurrencyAfterTax) : null
                        }

                        if (copyCostItem.save()) {
                            countNewCostItems++
                        } else {
                            log.debug("Error by proccessCopySurveyCostItems: " + copyCostItem.errors)
                        }

                    }

                }


            result.countNewCostItems = countNewCostItems

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Call to open the property copying view from one subscription into another
     * @return the list of properties for each year ring
     */
    Map<String, Object> copyProperties(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            params.tab = params.tab ?: 'surveyProperties'

            result = surveyControllerService.getSubResultForTranfser(result, params)

            result.selectedProperty
            result.properties
            if (params.tab == 'surveyProperties') {
                result.properties = SurveyConfigProperties.findAllBySurveyConfig(result.surveyConfig).surveyProperty
                result.properties -= PropertyStore.SURVEY_PROPERTY_PARTICIPATION
            }

            if (params.tab == 'customProperties') {
                //result.properties = result.parentSubscription.propertySet.findAll { it.type.tenant == null && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic)) }.type
                List<Subscription> childSubs = result.parentSubscription.getNonDeletedDerivedSubscriptions()
                if(childSubs) {
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.type.tenant is null and (sp.tenant = :context or (sp.tenant != :context and sp.isPublic = true)) and sp.instanceOf = null order by sp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )
                    result.properties = memberProperties
                }
            }

            if (params.tab == 'privateProperties') {
                //result.properties = result.parentSubscription.propertySet.findAll { it.type.tenant?.id == contextService.getOrg().id }.type

                List<Subscription> childSubs = result.parentSubscription.getNonDeletedDerivedSubscriptions()
                if(childSubs) {
                    String localizedName = LocaleUtils.getLocalizedAttributeName('name')
                    String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.type.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                    Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )

                    result.properties = memberProperties
                }
            }

            if (result.properties) {
                result.selectedProperty = params.selectedProperty ?: result.properties[0].id

                result.participantsList = []
                result.parentSuccessorSubChilds.each { sub ->

                    Map newMap = [:]
                    Org org = sub.getSubscriberRespConsortia()
                    newMap.id = org.id
                    newMap.org = org
                    newMap.sortname = org.sortname
                    newMap.name = org.name
                    newMap.newSub = sub
                    newMap.oldSub = result.surveyConfig.subscription ? (result.surveyConfig.subSurveyUseForTransfer ? sub._getCalculatedPreviousForSurvey() : result.parentSubscription.getDerivedSubscriptionForNonHiddenSubscriber(org)) : null

                    //println("new: ${newMap.newSub}, old: ${newMap.oldSub}")


                    if (params.tab == 'surveyProperties') {
                        PropertyDefinition surProp = PropertyDefinition.get(result.selectedProperty)
                        newMap.surveyProperty = SurveyResult.findBySurveyConfigAndTypeAndParticipant(result.surveyConfig, surProp, org)
                        newMap.propDef
                        if (surProp) {
                            if (surProp.tenant) {
                                newMap.propDef = PropertyDefinition.getByNameAndDescrAndTenant(surProp.name, PropertyDefinition.SUB_PROP, surProp.tenant)
                                newMap.newProperty = (sub && newMap.propDef) ? sub.propertySet.find {
                                    it.type.id == newMap.propDef.id && it.type.tenant?.id == contextService.getOrg().id
                                } : null
                                newMap.oldProperty = (newMap.oldSub && newMap.propDef) ? newMap.oldSub.propertySet.find {
                                    it.type.id == newMap.propDef.id && it.type.tenant?.id == contextService.getOrg().id
                                } : null
                            } else {
                                newMap.propDef = PropertyDefinition.getByNameAndDescr(surProp.name, PropertyDefinition.SUB_PROP)
                                newMap.newProperty = (sub && newMap.propDef) ? sub.propertySet.find {
                                    it.type.id == newMap.propDef.id && it.type.tenant == null && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                                } : null
                                newMap.oldProperty = (newMap.oldSub && newMap.propDef) ? newMap.oldSub.propertySet.find {
                                    it.type.id == newMap.propDef.id && it.type.tenant == null && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                                } : null
                            }
                        }

                    }
                    if (params.tab == 'customProperties') {
                        newMap.newCustomProperty = (sub) ? sub.propertySet.find {
                            it.type.id == Long.valueOf(result.selectedProperty) && it.type.tenant == null && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                        } : null
                        newMap.oldCustomProperty = (newMap.oldSub) ? newMap.oldSub.propertySet.find {
                            it.type.id == Long.valueOf(result.selectedProperty) && it.type.tenant == null && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                        } : null
                    }

                    if (params.tab == 'privateProperties') {
                        newMap.newPrivateProperty = (sub) ? sub.propertySet.find {
                            it.type.id == Long.valueOf(result.selectedProperty) && it.type.tenant?.id == contextService.getOrg().id
                        } : null
                        newMap.oldPrivateProperty = (newMap.oldSub) ? newMap.oldSub.propertySet.find {
                            it.type.id == Long.valueOf(result.selectedProperty) && it.type.tenant?.id == contextService.getOrg().id
                        } : null
                    }


                    result.participantsList << newMap
                }

                result.participantsList = result.participantsList.sort { it.sortname }
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Takes the submitted data and creates copies of the selected properties into the successor subscriptions
     * @return the property copy overview
     */
    Map<String, Object> proccessCopyProperties(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            if (params.list('selectedSub')) {
                result.selectedProperty
                PropertyDefinition propDef
                PropertyDefinition surveyProperty
                if (params.tab == 'surveyProperties') {
                    result.selectedProperty = params.selectedProperty ?: null

                    surveyProperty = params.copyProperty ? PropertyDefinition.get(params.long('copyProperty')) : null
                    if (surveyProperty) {
                        if (surveyProperty.tenant) {
                            propDef = PropertyDefinition.getByNameAndDescrAndTenant(surveyProperty.name, PropertyDefinition.SUB_PROP, surveyProperty.tenant)
                        }else {
                            propDef = PropertyDefinition.getByNameAndDescr(surveyProperty.name, PropertyDefinition.SUB_PROP)
                        }
                    }
                    if (!propDef && surveyProperty) {

                        Map<String, Object> map = [
                                token   : surveyProperty.name,
                                category: PropertyDefinition.SUB_PROP,
                                type    : surveyProperty.type,
                                rdc     : (surveyProperty.isRefdataValueType()) ? surveyProperty.refdataCategory : null,
                                i10n    : [
                                        name_de: surveyProperty.getI10n('name', 'de'),
                                        name_en: surveyProperty.getI10n('name', 'en'),
                                        expl_de: surveyProperty.getI10n('expl', 'de'),
                                        expl_en: surveyProperty.getI10n('expl', 'en')
                                ]
                        ]

                        if(surveyProperty.tenant){
                            map.tenant = surveyProperty.tenant.globalUID
                        }
                        propDef = PropertyDefinition.construct(map)
                    }

                } else {
                    result.selectedProperty = params.selectedProperty ?: null
                    propDef = params.selectedProperty ? PropertyDefinition.get(params.long('selectedProperty')) : null
                }

                Integer countSuccessfulCopy = 0

                if (propDef && params.list('selectedSub')) {
                    params.list('selectedSub').each { subID ->
                        if (Long.parseLong(subID) in result.parentSuccessorSubChilds.id) {
                            Subscription sub = Subscription.get(Long.parseLong(subID))
                            Org org = sub.getSubscriberRespConsortia()
                            Subscription oldSub = sub._getCalculatedPreviousForSurvey()

                            AbstractPropertyWithCalculatedLastUpdated copyProperty
                            if (params.tab == 'surveyProperties') {
                                copyProperty = SurveyResult.findBySurveyConfigAndTypeAndParticipant(result.surveyConfig, surveyProperty, org)

                                if (copyProperty && params.copyToSubAttribut) {
                                        if (surveyProperty == PropertyStore.SURVEY_PROPERTY_SUBSCRIPTION_FORM) {
                                            if (copyProperty.refValue) {
                                                sub.form = copyProperty.refValue
                                            }
                                        }

                                        if (surveyProperty == PropertyStore.SURVEY_PROPERTY_PUBLISHING_COMPONENT) {
                                            if (copyProperty.refValue == RDStore.YN_YES) {
                                                sub.hasPublishComponent = true
                                            }
                                        }
                                        sub.save()
                                }

                            } else {
                                if (params.tab == 'privateProperties') {
                                    copyProperty = oldSub ? oldSub.propertySet.find {
                                        it.type.id == propDef.id && it.type.tenant.id == contextService.getOrg().id
                                    } : []
                                } else {
                                    copyProperty = oldSub ? oldSub.propertySet.find {
                                        it.type.id == propDef.id && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                                    } : []
                                }
                            }

                            if (copyProperty) {
                                if (propDef.tenant != null) {
                                    //private Property
                                    def existingProps = sub.propertySet.findAll {
                                        it.owner.id == sub.id && it.type.id == propDef.id && it.type.tenant.id == contextService.getOrg().id
                                    }
                                    existingProps.removeAll { it.type.name != propDef.name } // dubious fix

                                    if (existingProps.size() == 0 || propDef.multipleOccurrence) {
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, sub, propDef, contextService.getOrg())
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
                                        it.type.id == propDef.id && it.owner.id == sub.id && (it.tenant?.id == contextService.getOrg().id || (it.tenant?.id != contextService.getOrg().id && it.isPublic))
                                    }

                                    if (existingProp == null || propDef.multipleOccurrence) {
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, sub, propDef, contextService.getOrg())
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
                Object[] args = [countSuccessfulCopy, messageSource.getMessage('copyProperties.' + params.tab, null, result.locale), params.list('selectedSub').size()]
                result.message = messageSource.getMessage('copyProperties.successful', args, result.locale) as String
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Takes the given members and processes their renewal into the next year, i.e. creates new subscription instances for
     * the following year along with their depending data
     * @return a redirect to the member comparison view
     */
    Map<String, Object> processTransferParticipantsByRenewal(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            result = surveyControllerService.getSubResultForTranfser(result, params)

            result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION

            result.properties = SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty
        

            result.multiYearTermFiveSurvey = null
            result.multiYearTermFourSurvey = null
            result.multiYearTermThreeSurvey = null
            result.multiYearTermTwoSurvey = null

            if (result.properties && PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in result.properties.id) {
                result.multiYearTermFiveSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5
                result.properties.remove(result.multiYearTermFiveSurvey)

            }

            if (result.properties && PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in result.properties.id) {
                result.multiYearTermFourSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4
                result.properties.remove(result.multiYearTermFourSurvey)

            }

            if (result.properties && PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in result.properties.id) {
                result.multiYearTermThreeSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3
                result.properties.remove(result.multiYearTermThreeSurvey)
            }
            if (result.properties && PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in result.properties.id) {
                result.multiYearTermTwoSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2
                result.properties.remove(result.multiYearTermTwoSurvey)

            }

            result.parentSuccessortParticipantsList = []

            result.parentSuccessorSubChilds.each { sub ->
                Org org = sub.getSubscriberRespConsortia()
                result.parentSuccessortParticipantsList << org

            }

            boolean addMembersOnlyToSuccesorSub = false
            Set<Subscription> successorSubs = result.parentSubscription._getCalculatedSuccessor()
            successorSubs.each { Subscription sub ->
                if (sub == result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()) {
                    addMembersOnlyToSuccesorSub = true
                }
            }


            result.newSubs = []

            int selectedMultiYearCount = 0
            result.nextSubs.eachWithIndex { Subscription subscription, int i ->
                if (subscription == result.parentSuccessorSubscription) {
                    selectedMultiYearCount = i + 1
                }
            }

            List<License> licensesToProcess = []

            if (params.generateSlavedLics == "all") {
                String query = "select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)"
                licensesToProcess.addAll(License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE]))
            } else if (params.generateSlavedLics == "partial") {
                List<String> licenseKeys = params.list("generateSlavedLicsReference")
                licenseKeys.each { String licenseKey ->
                    licensesToProcess << genericOIDService.resolveOID(licenseKey)
                }
            }

            List<String> excludes = PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key }
            //excludes << 'freezeHolding'
            excludes.add(PendingChangeConfiguration.TITLE_REMOVED)
            excludes.add(PendingChangeConfiguration.TITLE_REMOVED + PendingChangeConfiguration.NOTIFICATION_SUFFIX)
            excludes.add(PendingChangeConfiguration.TITLE_DELETED)
            excludes.add(PendingChangeConfiguration.TITLE_DELETED + PendingChangeConfiguration.NOTIFICATION_SUFFIX)
            excludes.addAll(PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key + PendingChangeConfiguration.NOTIFICATION_SUFFIX })
            Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name, result.parentSuccessorSubscription.id, excludes)

            boolean transferProvider = params.transferProvider ? true : false
            boolean transferVendor = params.transferVendor ? true : false
            List<Long> providersSelection = Params.getLongList(params, 'providersSelection')
            List<Long> vendorsSelection = Params.getLongList(params, 'vendorsSelection')

            Integer countNewSubs = 0

            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                    [
                            owner      : result.institution.id,
                            surProperty: result.participationProperty.id,
                            surConfig  : result.surveyConfig.id,
                            refValue   : RDStore.YN_YES]).each {

                // Keine Kindlizenz in der Nachfolgerlizenz vorhanden
                if (!(result.parentSuccessortParticipantsList && it.participant.id in result.parentSuccessortParticipantsList.id)) {

                    Subscription oldSubofParticipant = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : result.parentSubscription,
                             participant: it.participant
                            ])[0]


                    if (!oldSubofParticipant) {
                        oldSubofParticipant = result.parentSubscription
                    }

                    Date newStartDate = null
                    Date newEndDate = null

                    //Umfrage-Merkmal MJL2
                    SurveyResult participantPropertyTwo = result.multiYearTermTwoSurvey ? SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey) : null
                    //Umfrage-Merkmal MJL3
                    SurveyResult participantPropertyThree = result.multiYearTermThreeSurvey ? SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey) : null
                    //Umfrage-Merkmal MJL4
                    SurveyResult participantPropertyFour = result.multiYearTermFourSurvey ? SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey) : null
                    //Umfrage-Merkmal MJL5
                    SurveyResult participantPropertyFive = result.multiYearTermFiveSurvey ? SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey) : null

                    if (selectedMultiYearCount in [1, 2] && participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            if(oldSubofParticipant && oldSubofParticipant.endDate){
                                newStartDate = oldSubofParticipant.endDate + 1.day
                                newEndDate = oldSubofParticipant.endDate + 2.year
                            }else {
                                newStartDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.day) : null
                                newEndDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 2.year) : null
                            }

                        }
                        Subscription subscription = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                        if(subscription){
                            countNewSubs++
                            result.newSubs.addAll(subscription)
                        }
                    } else if (selectedMultiYearCount in [1, 2, 3] && participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            if(oldSubofParticipant && oldSubofParticipant.endDate){
                                newStartDate = oldSubofParticipant.endDate + 1.day
                                newEndDate = oldSubofParticipant.endDate + 3.year
                            }else {
                                newStartDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.day) : null
                                newEndDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 3.year) : null
                            }
                        }
                        Subscription subscription = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                        if(subscription){
                            countNewSubs++
                            result.newSubs.addAll(subscription)
                        }
                    } else if (selectedMultiYearCount in [1, 2, 3, 4] && participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            if(oldSubofParticipant && oldSubofParticipant.endDate){
                                newStartDate = oldSubofParticipant.endDate + 1.day
                                newEndDate = oldSubofParticipant.endDate + 4.year
                            }else {
                                newStartDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.day) : null
                                newEndDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 4.year) : null
                            }
                        }
                        Subscription subscription = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                        if(subscription){
                            countNewSubs++
                            result.newSubs.addAll(subscription)
                        }

                    } else if (selectedMultiYearCount in [1, 2, 3, 4, 5] && participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                        use(TimeCategory) {
                            if(oldSubofParticipant && oldSubofParticipant.endDate){
                                newStartDate = oldSubofParticipant.endDate + 1.day
                                newEndDate = oldSubofParticipant.endDate + 5.year
                            }else {
                                newStartDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.day) : null
                                newEndDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 5.year) : null
                            }
                        }
                        Subscription subscription = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, true, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                        if(subscription){
                            countNewSubs++
                            result.newSubs.addAll(subscription)
                        }

                    } else {
                        if (addMembersOnlyToSuccesorSub) {
                            use(TimeCategory) {
                                if(oldSubofParticipant && oldSubofParticipant.endDate){
                                    newStartDate = oldSubofParticipant.endDate + 1.day
                                    newEndDate = oldSubofParticipant.endDate + 1.year
                                }else {
                                    newStartDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.day) : null
                                    newEndDate = result.surveyConfig.subscription.endDate ? (result.surveyConfig.subscription.endDate + 1.year) : null
                                }
                            }
                            Subscription subscription = _processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant : null), result.parentSuccessorSubscription, it.participant, newStartDate, newEndDate, false, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                            if(subscription){
                                countNewSubs++
                                result.newSubs.addAll(subscription)
                            }

                        }
                    }
                }
            }

            //MultiYearTerm Subs
            result.parentSubChilds.each { sub ->
                if (surveyService.existsCurrentMultiYearTermBySurveyUseForTransfer(result.surveyConfig, sub.getSubscriber())) {
                    Org org = sub.getSubscriberRespConsortia()
                    if (!(result.parentSuccessortParticipantsList && org.id in result.parentSuccessortParticipantsList.id)) {
                        Subscription subscription = _processAddMember(sub, result.parentSuccessorSubscription, org, sub.startDate, sub.endDate, true, RDStore.SUBSCRIPTION_INTENDED, inheritedAttributes, licensesToProcess, transferProvider, transferVendor, providersSelection, vendorsSelection)
                        if(subscription){
                            countNewSubs++
                            result.newSubs.addAll(subscription)
                        }

                    }

                }

            }

            Set<Package> packagesToProcess = []

            //copy package data
            if (params.linkAllPackages) {
                result.parentSuccessorSubscription.packages.each { sp ->
                    packagesToProcess << sp.pkg
                }
            } else if (params.packageSelection) {
                List packageIds = params.list("packageSelection")
                packageIds.each { spId ->
                    packagesToProcess << SubscriptionPackage.get(spId).pkg
                }
            }
            String threadName = 'PackageTransfer_' + result.parentSuccessorSubscription.id
            if (packagesToProcess.size() > 0 && !subscriptionService.checkThreadRunning(threadName)) {
                boolean withEntitlements = params.linkWithEntitlements == 'on'
                executorService.execute({
                    Thread.currentThread().setName()
                    long start = System.currentTimeSeconds()
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
                    if (System.currentTimeSeconds() - start >= GlobalService.LONG_PROCESS_LIMBO) {
                        Object[] args = [result.parentSuccessorSubscription.name]
                        globalService.notifyBackgroundProcessFinish(result.user.id, threadName, messageSource.getMessage('subscription.details.linkPackage.thread.completed', args, result.locale))
                    }
                })
            }

            result.countNewSubs = countNewSubs
            if (result.newSubs) {
                result.parentSuccessorSubscription.syncAllShares(result.newSubs)
            }

            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Set link to license or provider
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String,Object> setProviderOrLicenseLink(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }

            if (params.license) {
                License license = genericOIDService.resolveOID(params.license)
                result.surveyInfo.license = license ?: result.surveyInfo.license

                if (!result.surveyInfo.save()) {
                    result.error = messageSource.getMessage('surveyInfo.link.fail', null, result.locale)
                }
            }

            if (params.provider) {
                Provider provider = genericOIDService.resolveOID(params.provider)
                result.surveyInfo.provider = provider ?: result.surveyInfo.provider

                if (!result.surveyInfo.save()) {
                    result.error = messageSource.getMessage('surveyInfo.link.fail', null, result.locale)
                }
            }

            if (params.unlinkLicense) {
                result.surveyInfo.license = null

                if (!result.surveyInfo.save()) {
                    result.error = messageSource.getMessage('surveyInfo.unlink.fail', null, result.locale)
                }
            }

            if (params.unlinkProvider) {
                result.surveyInfo.provider = null

                if (!result.surveyInfo.save()) {
                    result.error = messageSource.getMessage('surveyInfo.unlink.fail', null, result.locale)
                }
            }
            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Set link to another survey
     *  @return OK with the result map in case of success, ERROR otherwise
     */
    Map<String,Object> setSurveyLink(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }
            if (params.linkSurvey) {
                SurveyInfo linkSurvey = SurveyInfo.get(params.linkSurvey)

                if (linkSurvey) {
                    if (SurveyLinks.findBySourceSurveyAndTargetSurvey(result.surveyInfo, linkSurvey)) {
                        result.error = messageSource.getMessage('surveyLinks.link.exists', null, result.locale)
                    } else {
                        SurveyLinks surveyLink = new SurveyLinks(sourceSurvey: result.surveyInfo, targetSurvey: linkSurvey)
                        if (!surveyLink.save()) {
                            result.error = messageSource.getMessage('surveyInfo.link.fail', null, result.locale)
                        } else {
                            if (params.bothDirection && !SurveyLinks.findBySourceSurveyAndTargetSurvey(linkSurvey, result.surveyInfo)) {
                                SurveyLinks surveyLink2 = new SurveyLinks(sourceSurvey: linkSurvey, targetSurvey: result.surveyInfo, bothDirection: true)
                                surveyLink.bothDirection = true

                                if (!surveyLink2.save() && !surveyLink.save()) {
                                    result.error = messageSource.getMessage('surveyInfo.link.fail', null, result.locale)
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


            [result: result, status: STATUS_OK]
        }

    }

    /**
     * Adds an information link to the survey; above the pure URL also further comments accompanying the survey
     *  @return OK with the result map in case of success, ERROR otherwise
     * @see de.laser.survey.SurveyUrl
     */
    Map<String,Object> addSurveyUrl(GrailsParameterMap params) {
        Map<String, Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result: null, status: STATUS_ERROR]
        } else {
            if (!result.editable) {
                [result: null, status: STATUS_ERROR]
                return
            }
            if (result.surveyConfig.surveyUrls.size() >= 10) {
                result.error = messageSource.getMessage('surveyconfig.url.fail.max10', null, result.locale)
            }
            else {
                if (params.url && (params.url.startsWith('http://') || params.url.startsWith('https://'))) {
                        SurveyUrl surveyUrl = new SurveyUrl(url: params.url, urlComment: params.urlComment, surveyConfig: result.surveyConfig)
                        if (!surveyUrl.save()) {
                            result.error = messageSource.getMessage('survey.change.fail', null, result.locale)
                        }
                }else {
                    result.error = messageSource.getMessage('xEditable.validation.url', null, result.locale)
                }
            }
        }

        [result: result, status: STATUS_OK]
    }


    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets generic parameters common to many controller calls and checks permission grants
     * @param params the request parameter map
     * @return the result map with generic parameters
     */
    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.locale = LocaleUtils.getCurrentLocale()

        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.surveyInfo = SurveyInfo.get(params.id)
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(params.long('surveyConfigID')) : result.surveyInfo.surveyConfigs[0]
        result.surveyWithManyConfigs = (result.surveyInfo.surveyConfigs?.size() > 1)

        result.editable = result.surveyInfo.isEditable() ?: false

        if (!(result.user.isAdmin() || result.user.isYoda() || result.surveyInfo.owner.id == contextService.getOrg().id)) {
            return [result: null, status: STATUS_ERROR]
        }

        if (result.surveyConfig) {
            result.transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : null
        }

        int tc1 = taskService.getTasksByResponsibilityAndObject(result.user, result.surveyConfig).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.surveyConfig).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

        result.notesCount = docstoreService.getNotesCount(result.surveyConfig, contextService.getOrg())
        result.docsCount = docstoreService.getDocsCount(result.surveyConfig, contextService.getOrg())
        result.participantsCount = SurveyOrg.executeQuery("select count (*) from SurveyOrg where surveyConfig = :surveyConfig", [surveyConfig: result.surveyConfig])[0]
        result.surveyCostItemsCount = CostItem.executeQuery("select count(*) from CostItem where pkg is null and owner = :owner and costItemStatus != :status and surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig)", [surveyConfig: result.surveyConfig, owner: result.surveyInfo.owner, status: RDStore.COST_ITEM_DELETED])[0]
        result.surveyCostItemsPackagesCount = CostItem.executeQuery("select count(*) from CostItem where pkg is not null and sub is null and owner = :owner and costItemStatus != :status and surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig)", [surveyConfig: result.surveyConfig, owner: result.surveyInfo.owner, status: RDStore.COST_ITEM_DELETED])[0]
        result.surveyPackagesCount = SurveyConfigPackage.executeQuery("select count(*) from SurveyConfigPackage where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]
        result.surveyVendorsCount = SurveyConfigPackage.executeQuery("select count(*) from SurveyConfigVendor where surveyConfig = :surConfig", [surConfig: result.surveyConfig])[0]
        result.evaluationCount = SurveyOrg.executeQuery("select count (*) from SurveyOrg where surveyConfig = :surveyConfig and finishDate is not null", [surveyConfig: result.surveyConfig])[0] + '/' + SurveyOrg.executeQuery("select count (*) from SurveyOrg where surveyConfig = :surveyConfig", [surveyConfig: result.surveyConfig])[0]
        if(result.surveyConfig.subSurveyUseForTransfer) {
            Subscription successorSub  = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
            result.renewalEvaluationCount = (successorSub ? Subscription.executeQuery("select count (*)" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org in (:orgs) and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub",
                    [orgs          : SurveyOrg.executeQuery("select surOrg.org from SurveyOrg surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.finishDate is not null and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surOrg.surveyConfig and surResult.participant = surOrg.org and surResult.type = :type and surResult.refValue = :refValue)", [surveyConfig: result.surveyConfig, type: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, refValue: RDStore.YN_YES]),
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: successorSub])[0] : 0) + '/' + SurveyOrg.executeQuery("select count (*) from SurveyOrg surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.finishDate is not null and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surOrg.surveyConfig and surResult.participant = surOrg.org and surResult.type = :type and surResult.refValue = :refValue)", [surveyConfig: result.surveyConfig, type: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, refValue: RDStore.YN_YES])[0]

        }
        result.subscription = result.surveyConfig.subscription ?: null

        result
    }

    Map getSubResultForTranfser(Map result, GrailsParameterMap parameterMap) {
        result.parentSubscription = result.surveyConfig.subscription


        if (result.surveyConfig.subSurveyUseForTransfer) {
            List listMuliYearsSub = []
            int multiYears = 0
            List<PropertyDefinition> surProperties = result.surveyConfig.surveyProperties.surveyProperty
            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surProperties.id) {
                multiYears = 2
            }
            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surProperties.id) {
                multiYears = 3
            }
            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in surProperties.id) {
                multiYears = 4
            }
            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in surProperties.id) {
                multiYears = 5
            }

            Date startDate = result.parentSubscription.startDate
            Date endDate = result.parentSubscription.endDate
            for (int i = 0; i < multiYears; i++) {
                int newYear = i + 1
                Map mulitYearMap = [:]
                use(TimeCategory) {
                    startDate = endDate ? (endDate + 1.day) : null
                    endDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + newYear.year) : null
                }
                mulitYearMap.startDate = startDate
                mulitYearMap.endDate = endDate

                listMuliYearsSub << mulitYearMap
            }

            result.nextSubs = linksGenerationService.getSuccessionChain(result.parentSubscription, 'destinationSubscription')
            result.listMuliYearsSub = listMuliYearsSub

            Subscription targetSubscription
            if (parameterMap.targetSubscriptionId) {
                targetSubscription = Subscription.get(parameterMap.targetSubscriptionId)
            }

            if (targetSubscription && result.nextSubs.size() > 0 && (targetSubscription in result.nextSubs)) {
                result.parentSuccessorSubscription = targetSubscription
                Subscription previousSub = targetSubscription._getCalculatedPreviousForSurvey()

                if (previousSub)
                    result.parentSubscription = previousSub
            } else {
                result.parentSuccessorSubscription = result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()
            }

            if (result.transferWorkflow && result.transferWorkflow.containsKey('transferWorkflowForMultiYear_' + result.parentSuccessorSubscription.id)) {
                result.transferWorkflow = result.transferWorkflow['transferWorkflowForMultiYear_' + result.parentSuccessorSubscription.id]
            } else if (result.parentSuccessorSubscription != result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()) {
                result.transferWorkflow = [:]
            }


        } else {
            result.parentSuccessorSubscription = parameterMap.targetSubscriptionId ? Subscription.get(parameterMap.targetSubscriptionId) : null

            if (result.transferWorkflow && result.transferWorkflow.containsKey('transferWorkflowForSub_' + result.parentSuccessorSubscription.id)) {
                result.transferWorkflow = result.transferWorkflow['transferWorkflowForSub_' + result.parentSuccessorSubscription.id]
            } else if (result.parentSuccessorSubscription != result.surveyConfig.subscription?._getCalculatedSuccessorForSurvey()) {
                result.transferWorkflow = [:]
            }

        }
        result.targetSubscription = result.parentSuccessorSubscription
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null
        result.parentSubChilds = subscriptionService.getValidSubChilds(result.parentSubscription)

        result
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
    private def _processAddMember(Subscription oldSub, Subscription newParentSub, Org org, Date newStartDate, Date newEndDate, boolean multiYear, RefdataValue status, Set<AuditConfig> inheritedAttributes, List<License> licensesToProcess, boolean transferProvider, boolean transferVendor, List<Long> providersSelection, List<Long> vendorsSelection ) {

        Org institution = contextService.getOrg()
        Subscription memberSub

        if (institution.isCustomerType_Consortium_Pro() && !newParentSub.getDerivedSubscriptionForNonHiddenSubscriber(org)) {

            log.debug("Generating seperate slaved instances for members")
                Date startDate = newStartDate ?: null
                Date endDate = newEndDate ?: null
                //subject to be removed in 3.3

                memberSub = new Subscription(
                        type: newParentSub.type ?: null,
                        kind: newParentSub.kind ?: null,
                        status: status,
                        name: newParentSub.name,
                        startDate: startDate,
                        endDate: endDate,
                        referenceYear: newParentSub.referenceYear ?: null,
                        administrative: newParentSub._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                        manualRenewalDate: newParentSub.manualRenewalDate,
                        identifier: RandomUtils.getUUID(),
                        instanceOf: newParentSub,
                        resource: newParentSub.resource ?: null,
                        form: newParentSub.form ?: null,
                        isPublicForApi: newParentSub.isPublicForApi,
                        hasPerpetualAccess: newParentSub.hasPerpetualAccess,
                        hasPublishComponent: newParentSub.hasPublishComponent,
                        holdingSelection: newParentSub.holdingSelection ?: null,
                        isMultiYear: multiYear ?: false
                )

                inheritedAttributes.each { AuditConfig attr ->
                    memberSub[attr.referenceField] = memberSub[attr.referenceField]
                }
                if (!memberSub.save()) {
                    memberSub.errors.each { e ->
                        log.debug("Problem creating new sub: ${e}")
                    }
                }

                if (memberSub) {

                    new OrgRole(org: org, sub: memberSub, roleType: RDStore.OR_SUBSCRIBER_CONS).save()
                    new OrgRole(org: institution, sub: memberSub, roleType: RDStore.OR_SUBSCRIPTION_CONSORTIUM).save()

                    if (transferProvider) {
                        newParentSub.getProviders().each { provider ->
                            new ProviderRole(provider: provider, subscription: memberSub).save()
                        }
                    }
                    else {
                        if (providersSelection) {
                            providersSelection.each { providerID ->
                                new ProviderRole(provider: Provider.get(providerID), subscription: memberSub).save()
                            }
                        }
                    }
                    if (transferVendor) {
                        newParentSub.getVendors().each { vendor ->
                            new VendorRole(vendor: vendor, subscription: memberSub).save()
                        }
                    }
                    else {
                        if (vendorsSelection) {
                            vendorsSelection.each { vendorID ->
                                new VendorRole(vendor: Vendor.get(vendorID), subscription: memberSub).save()
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
                            } else {
                                // no match found, creating new prop with backref
                                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type, scp.tenant)
                                newProp = scp.copyInto(newProp)
                                newProp.instanceOf = scp
                                newProp.save()
                            }
                        }
                    }

                    Identifier.findAllBySub(newParentSub).each { Identifier id ->
                        AuditConfig ac = AuditConfig.getConfig(id)
                        if (ac) {
                            Identifier.constructWithFactoryResult([value: id.value, parent: id, reference: memberSub, namespace: id.ns])
                        }
                    }

                    if(AuditConfig.getConfig(newParentSub, 'holdingSelection') && newParentSub.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE){
                        newParentSub.packages.each { SubscriptionPackage subscriptionPackage ->
                            subscriptionService.addToSubscriptionCurrentStock(memberSub, newParentSub, subscriptionPackage.pkg, false)
                        }
                    }

                    memberSub = memberSub.refresh()

                    licensesToProcess.each { License lic ->
                        subscriptionService.setOrgLicRole(memberSub, lic, false)
                    }

                    if (oldSub) {
                        Links.construct([linkType: RDStore.LINKTYPE_FOLLOWS, source: memberSub, destination: oldSub, owner: institution]).save()
                    }

                    if (org.isCustomerType_Inst_Pro()) {
                        PendingChange.construct([target: memberSub, oid: "${memberSub.getClass().getName()}:${memberSub.id}", msgToken: "pendingChange.message_SU_NEW_01", status: RDStore.PENDING_CHANGE_PENDING, owner: org])
                    }

                }
        }
        return memberSub
    }

    /*
    private Map<String,Object> setResultGenericsAndCheckAccessforSub(checkOption) {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription.getSubscriberRespConsortia()

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
    private def _setNewProperty(def property, def value) {

        String field = null

        if (property.type.isLongType()) {
            field = "longValue"
        } else if (property.type.isStringType()) {
            field = "stringValue"
        } else if (property.type.isBigDecimalType()) {
            field = "decValue"
        } else if (property.type.isDateType()) {
            field = "dateValue"
        } else if (property.type.isURLType()) {
            field = "urlValue"
        } else if (property.type.isRefdataValueType()) {
            field = "refValue"
        }

        //Wenn eine Vererbung vorhanden ist.
        if (field && property.hasProperty('instanceOf') && property.instanceOf && AuditConfig.getConfig(property.instanceOf)) {
            if (property.instanceOf."${field}" == '' || property.instanceOf."${field}" == null) {
                value = property.instanceOf."${field}" ?: ''
            } else {
                //
                return
            }
        }

            if (value == '' && field) {
                // Allow user to set a rel to null be calling set rel ''
                property[field] = null
                property.save()
            } else {

                if (property && value && field) {

                    if (field == "refValue") {
                        property["${field}"] = value
                        //property.save(flush:true)
                        if (!property.save(failOnError: true)) {
                            log.error("Error Property save: " + property.error)
                        }
                    } else if (field == "dateValue") {
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
                            log.error(e.toString())
                        }
                    } else if (field == "urlValue") {

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
                            log.error(e.toString())
                        }
                    } else {
                        def binding_properties = [:]
                        if (field == "decValue") {
                            value = new BigDecimal(value)
                        }

                        property["${field}"] = value

                        property.save(failOnError: true)

                    }

                }
            }

    }

}
