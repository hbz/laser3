package de.laser

import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.base.AbstractReport
import de.laser.cache.EhcacheWrapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.remote.Wekb
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.stats.CounterCheck
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyInfo
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.RandomUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.ProviderRole
import de.laser.wekb.TIPPCoverage
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.BatchingStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.slurpersupport.GPathResult
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus
import org.springframework.web.multipart.MultipartFile

import java.math.RoundingMode
import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService
import java.util.regex.Pattern

@Transactional
class SubscriptionService {

    AuditService auditService
    BatchQueryService batchQueryService
    CacheService cacheService
    ContextService contextService
    EscapeService escapeService
    ExecutorService executorService
    ExportService exportService
    FilterService filterService
    GenericOIDService genericOIDService
    GlobalService globalService
    GokbService gokbService
    IssueEntitlementService issueEntitlementService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    PackageService packageService
    PropertyService propertyService
    ProviderService providerService
    RefdataService refdataService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyService surveyService
    TitleService titleService
    UserService userService


    /**
     * ex {@link MyInstitutionController#currentSubscriptions()}
     * Gets the current subscriptions for the given institution
     * @param params the request parameter map
     * @param contextUser the user whose settings should be considered
     * @param contextOrg the institution whose subscriptions should be accessed
     * @return a result map containing a list of subscriptions and other site parameters
     */
    Map<String,Object> getMySubscriptions(GrailsParameterMap params, User contextUser, Org contextOrg) {
        Map<String,Object> result = [:]
        EhcacheWrapper cache = contextService.getUserCache("/subscriptions/filter/")
        if(cache && cache.get('subscriptionFilterCache')) {
            if(!params.resetFilter && !params.isSiteReloaded)
                params.putAll((GrailsParameterMap) cache.get('subscriptionFilterCache'))
            else params.remove('resetFilter')
            cache.remove('subscriptionFilterCache') //has to be executed in any case in order to enable cache updating
        }
        SwissKnife.setPaginationParams(result, params, contextUser)

        Profiler prf = new Profiler()
        prf.setBenchmark('init data fetch')
        prf.setBenchmark('consortia')
        result.availableConsortia = Combo.executeQuery(
                'select c.toOrg from Combo as c where c.fromOrg = :fromOrg and c.type = :type',
                [fromOrg: contextOrg, type: RDStore.COMBO_TYPE_CONSORTIUM]
        )

        List<Role> consRoles = Role.findAll { authority in ['ORG_CONSORTIUM_BASIC', 'ORG_CONSORTIUM_PRO'] }
        prf.setBenchmark('all consortia')
        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSetting os_ct where 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and os_ct.roleValue in (:roles) 
                        order by lower(o.name)""",
                [roles: consRoles]
        )

        def viableOrgs = []

        if ( result.availableConsortia ){
            result.availableConsortia.each {
                viableOrgs.add(it)
            }
        }

        viableOrgs.add(contextOrg)

        String consortiaFilter = ''
        if(contextOrg.isCustomerType_Consortium() || contextOrg.isCustomerType_Support())
            consortiaFilter = 'and s.instanceOf = null'

        Set<Year> availableReferenceYears = Subscription.executeQuery('select s.referenceYear from OrgRole oo join oo.sub s where s.referenceYear != null and oo.org = :contextOrg '+consortiaFilter+' order by s.referenceYear desc', [contextOrg: contextOrg])
        result.referenceYears = availableReferenceYears

        def date_restriction = null
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = userService.hasFormalAffiliation(contextOrg, 'INST_EDITOR')

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                String[] defaultStatus = [RDStore.SUBSCRIPTION_CURRENT.id]
                params.status = defaultStatus
                //params.hasPerpetualAccess = RDStore.YN_YES.id.toString() as you wish, myladies ... as of May 16th, '22, the setting should be reverted
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        if(params.isSiteReloaded == "yes") {
            params.remove('isSiteReloaded')
            cache.put('subscriptionFilterCache', params)
        }

        prf.setBenchmark('get base query')
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, '', contextOrg)
        result.filterSet = tmpQ[2]
        List<Subscription> subscriptions
        prf.setBenchmark('fetch subscription data')
        subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]
        //impossible to sort in nothing ...
        if(params.sort == "provider") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String name1 = s1.getSortedProviders(params.order)[0]?.name.toLowerCase(), name2 = s2.getSortedProviders(params.order)[0]?.name.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!name1) {
                        if(!name2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!name2)
                            cmp = -1
                        else cmp = name1 <=> name2
                    }
                }
                else cmp = name2 <=> name1
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.name <=> s2.name : s2.name <=> s1.name
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.startDate <=> s2.startDate : s2.startDate <=> s1.startDate
                cmp
            }
        }
        else if(params.sort == "vendor") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String name1 = s1.getSortedVendors(params.order)[0]?.name.toLowerCase(), name2 = s2.getSortedVendors(params.order)[0]?.name.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!name1) {
                        if(!name2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!name2)
                            cmp = -1
                        else cmp = name1 <=> name2
                    }
                }
                else cmp = name2 <=> name1
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
        if(!params.exportXLS)
            result.num_sub_rows = subscriptions.size()

        result.date_restriction = date_restriction
        prf.setBenchmark('get properties')
        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        prf.setBenchmark('end properties')
        result.subIDs = subscriptions.collect { Subscription s -> s.id }
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)
        prf.setBenchmark('fetch licenses')
        if(subscriptions)
            result.allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndSourceLicenseIsNotNullAndLinkType(result.subscriptions,RDStore.LINKTYPE_LICENSE)
        prf.setBenchmark('after licenses')
        List bm = prf.stopBenchmark()
        result.benchMark = bm
        result
    }

    /**
     * ex {@link MyInstitutionController#currentSubscriptions()}
     * Gets the current subscription transfers for the given institution
     * @param params the request parameter map
     * @param contextUser the user whose settings should be considered
     * @param contextOrg the institution whose subscriptions should be accessed
     * @return a result map containing a list of subscription transfers and other site parameters
     */
    Map<String,Object> getMySubscriptionTransfer(GrailsParameterMap params, User contextUser, Org contextOrg) {
        Map<String,Object> result = [:]
        EhcacheWrapper cache = contextService.getUserCache("/subscriptionsTransfer/filter/")
        if(cache && cache.get('subscriptionsTransferFilterCache')) {
            if(!params.resetFilter && !params.isSiteReloaded)
                params.putAll((GrailsParameterMap) cache.get('subscriptionsTransferFilterCache'))
            else params.remove('resetFilter')
            cache.remove('subscriptionsTransferFilterCache') //has to be executed in any case in order to enable cache updating
        }
        SwissKnife.setPaginationParams(result, params, contextUser)

        result.editable = userService.hasFormalAffiliation(contextOrg, 'INST_EDITOR')

        SimpleDateFormat sdfyear = DateUtils.getSDF_yyyy()
        String currentYear = sdfyear.format(new Date())

        params.referenceYears = params.referenceYears ?: currentYear

        String consortiaFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            consortiaFilter = 'and s.instanceOf = null'

        Set<Year> availableReferenceYears = Subscription.executeQuery('select s.referenceYear from OrgRole oo join oo.sub s where s.referenceYear != null and oo.org = :contextOrg '+consortiaFilter+' order by s.referenceYear desc', [contextOrg: contextOrg])
        result.referenceYears = availableReferenceYears

        if(params.isSiteReloaded == "yes") {
            params.remove('isSiteReloaded')
            cache.put('subscriptionFilterCache', params)
        }


        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, '', contextOrg)
        result.filterSet = tmpQ[2]
        Set<Subscription> subscriptionsFromQuery
        Set<Subscription> subscriptions
        subscriptionsFromQuery = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]
        //candidate for ugliest bugfix ever ...

        params.sort = params.sort ?: 'provider'
        params.order = params.order ?: 'asc'
        if(params.sort){
            String newSort = "sub.${params.sort}"
            if(params.sort == 'provider'){
                subscriptions = Subscription.executeQuery("select sub from Subscription sub join sub.providerRelations oo where (sub.id in (:subscriptions)) order by oo.provider.name " + params.order + ", sub.name ", [subscriptions: subscriptionsFromQuery.id])
                subscriptions = subscriptions + Subscription.executeQuery("select sub from Subscription sub where sub.id in (:subscriptions) order by sub.name ", [subscriptions: subscriptionsFromQuery.id])
            }else if(params.sort == 'vendor'){
                subscriptions = Subscription.executeQuery("select sub from Subscription sub join sub.vendorRelations oo where (sub.id in (:subscriptions)) order by oo.vendor.name " + params.order + ", sub.name ", [subscriptions: subscriptionsFromQuery.id])
                subscriptions = subscriptions + Subscription.executeQuery("select sub from Subscription sub where sub.id in (:subscriptions) order by sub.name ", [subscriptions: subscriptionsFromQuery.id])
            }
            else {
                subscriptions = Subscription.executeQuery("select sub from Subscription sub join sub.providerRelations oo where sub.id in (:subscriptions) order by " + newSort + " " + params.order + ", oo.provider.name, sub.name ", [subscriptions: subscriptionsFromQuery.id])
            }
        }

        result.allSubscriptions = subscriptions
        if(!params.exportXLS)
            result.num_sub_rows = subscriptions.size()

        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        Set providerIds = providerService.getCurrentProviderIds( contextService.getOrg() )

        result.providers = providerIds.isEmpty() ? [] : Provider.findAllByIdInList(providerIds).sort { it?.name }

        result
    }


    /**
     * ex MyInstitutionController.manageConsortiaSubscriptions()
     * Gets the current subscriptions with their cost items for the given consortium
     * @param params the request parameter map
     * @param contextUser the user whose settings should be considered
     * @param contextOrg the institution whose subscriptions should be accessed
     * @return a result map containing a list of subscriptions and other site parameters
     */
    Map<String,Object> getMySubscriptionsForConsortia(GrailsParameterMap params,User contextUser, Org contextOrg,List<String> tableConf) {
        Map<String,Object> result = [:]

        Profiler prf = new Profiler()
        prf.setBenchmark('filterService')

        SwissKnife.setPaginationParams(result, params, contextUser)

        FilterService.Result fsr = filterService.getOrgComboQuery(params+[comboType:RDStore.COMBO_TYPE_CONSORTIUM.value,sort:'o.sortname'], contextOrg)
        if (fsr.isFilterSet) { params.filterSet = true }
        result.filterConsortiaMembers = Org.executeQuery(fsr.query, fsr.queryParams)

        prf.setBenchmark('filterSubTypes & filterPropList')

        if(params.filterSet)
            result.filterSet = params.filterSet

        result.filterSubTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE).minus(RDStore.SUBSCRIPTION_TYPE_LOCAL)
        result.filterPropList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        Set<Year> availableReferenceYears = Subscription.executeQuery('select s.referenceYear from OrgRole oo join oo.sub s where s.referenceYear != null and oo.org = :contextOrg and s.instanceOf != null order by s.referenceYear desc', [contextOrg: contextOrg])
        result.referenceYears = availableReferenceYears

        // CostItem ci

        prf.setBenchmark('filter query')

        String query
        Map qarams
        Map<Subscription,Set<License>> linkedLicenses = [:]

        if('withCostItems' in tableConf) {
            //database-side filtering; delivers strange duplicates: (case when ci.owner = :org then ci else null end) --> transpose into server filtering (again... beware of performance loss!)
            query = "select new map(ci as cost, subT as sub, roleT.org as orgs) " +
                    " from CostItem ci right outer join ci.sub subT join subT.instanceOf subK " +
                    " join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                    " where roleK.org = :org and roleK.roleType = :rdvCons " +
                    " and roleTK.org = :org and roleTK.roleType = :rdvCons " +
                    " and roleT.roleType in (:rdvSubscr) " +
                    " and ( (ci is null or ci.costItemStatus != :deleted) ) "
            qarams = [org      : contextOrg,
                      rdvCons  : RDStore.OR_SUBSCRIPTION_CONSORTIUM,
                      rdvSubscr: [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN],
                      deleted  : RDStore.COST_ITEM_DELETED
            ]
        }
        else if('onlyMemberSubs' in tableConf) {
            query = "select subT, roleT.org " +
                    " from Subscription subT join subT.instanceOf subK " +
                    " join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                    " where roleK.org = :org and roleK.roleType = :rdvCons " +
                    " and roleTK.org = :org and roleTK.roleType = :rdvCons " +
                    " and ( roleT.roleType = :rdvSubscr or roleT.roleType = :rdvSubscrHidden ) "
            qarams = [org      : contextOrg,
                      rdvCons  : RDStore.OR_SUBSCRIPTION_CONSORTIUM,
                      rdvSubscr: RDStore.OR_SUBSCRIBER_CONS,
                      rdvSubscrHidden: RDStore.OR_SUBSCRIBER_CONS_HIDDEN
            ]
        }

        if (params.selSubscription?.size() > 0) {
            query += " and subT.instanceOf in (:subscriptions) "
            Set<Subscription> subscriptions = []
            List<String> selSubs = params.selSubscription.split(',')
            selSubs.each { String sub ->
                subscriptions << genericOIDService.resolveOID(sub)
            }
            qarams.put('subscriptions', subscriptions)
        }

        if (params.member?.size() > 0) {
            query += " and roleT.org.id = :member "
            qarams.put('member', params.long('member'))
        }
        /*
        this performance improvement is not needed any more! Keep it nonetheless for the case of ...
        else if(!params.filterSet) {
            query += " and roleT.org.id = :member "
            qarams.put('member', result.filterConsortiaMembers[0].id)
            params.member = result.filterConsortiaMembers[0].id
            result.defaultSet = true
        }
        */

        if (params.identifier?.length() > 0) {
            query += " and exists (select ident from Identifier ident join ident.org ioorg " +
                    " where ioorg = roleT.org and LOWER(ident.value) like LOWER(:identifier)) "
            qarams.put('identifier', "%${params.identifier}%")
        }

        Map costFilter = params.findAll{ it -> it.toString().startsWith('iex:subCostItem.') }
        if(costFilter) {
            query += " and (ci.costItemElement.id in (:elems) or ci is null)" //otherwise, subscriptions without cost items will be omitted due to not-null join
            qarams.put('elems', costFilter.keySet().collect { String selElem -> Long.parseLong(selElem.split('\\.')[2]) })
        }

        if (params.validOn?.size() > 0) {
            result.validOn = params.validOn

            if('withCostItems' in tableConf) {
                query += " and ( "
                query += "( ci.startDate <= :validOn OR (ci.startDate is null AND (subT.startDate <= :validOn OR subT.startDate is null) ) ) and "
                query += "( ci.endDate >= :validOn OR (ci.endDate is null AND (subT.endDate >= :validOn OR subT.endDate is null) ) ) "
                query += ") "
            }
            else if('onlyMemberSubs' in tableConf) {
                query += " and ( "
                query += "(subT.startDate <= :validOn OR subT.startDate is null) and "
                query += "(subT.endDate <= :validOn OR subT.endDate is null)"
                query += ") "
            }

            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            qarams.put('validOn', new Timestamp(sdf.parse(params.validOn).getTime()))
        }

        String statusQuery = ""
        if (params.status?.size() > 0) {
            statusQuery = " and subT.status.id = :status "
            qarams.put('status',params.long('status'))
        } else if(!params.filterSet) {
            statusQuery = " and subT.status.id = :status "
            qarams.put('status',RDStore.SUBSCRIPTION_CURRENT.id)
            params.status = RDStore.SUBSCRIPTION_CURRENT.id
            //params.hasPerpetualAccess = RDStore.YN_YES.id
            result.defaultSet = true
        }
        if (params.long("status") == RDStore.SUBSCRIPTION_CURRENT.id && params.long('hasPerpetualAccess') == RDStore.YN_YES.id) {
            statusQuery = " and (subT.status.id = :status or subT.hasPerpetualAccess = true) "
        }
        else if (params.hasPerpetualAccess) {
            query += " and subT.hasPerpetualAccess = :hasPerpetualAccess "
            qarams.put('hasPerpetualAccess', (params.long('hasPerpetualAccess') == RDStore.YN_YES.id))
        }
        query += statusQuery

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, query, 'subT', qarams)
            query = psq.query
            qarams = psq.queryParams
        }

        if (params.form) {
            query += " and subT.form.id = :form "
            qarams.put('form', params.long('form'))
        }
        if (params.resource) {
            query += " and subT.resource.id = :resource "
            qarams.put('resource', params.long('resource'))
        }
        if (params.subTypes) {
            query += " and subT.type.id in (:subTypes) "
            qarams.put('subTypes', Params.getLongList(params, 'subTypes'))
        }

        if (params.subKinds) {
            query += " and subT.kind.id in (:subKinds) "
            qarams.put('subKinds', Params.getLongList(params, 'subKinds'))
        }

        if (params.isPublicForApi) {
            query += " and subT.isPublicForApi = :isPublicForApi "
            qarams.put('isPublicForApi', (params.long('isPublicForApi') == RDStore.YN_YES.id))
        }

        if (params.hasPublishComponent) {
            query += " and subT.hasPublishComponent = :hasPublishComponent "
            qarams.put('hasPublishComponent', (params.long('hasPublishComponent') == RDStore.YN_YES.id))
        }

        if (params.subRunTimeMultiYear || params.subRunTime) {

            if (params.subRunTimeMultiYear && !params.subRunTime) {
                query += " and subT.isMultiYear = :subRunTimeMultiYear "
                qarams.put('subRunTimeMultiYear', true)
            }else if (!params.subRunTimeMultiYear && params.subRunTime){
                query += " and subT.isMultiYear = :subRunTimeMultiYear "
                qarams.put('subRunTimeMultiYear', false)
            }
        }

        if (params.referenceYears) {
            query += " and subT.referenceYear in (:referenceYears) "
            Set<Year> referenceYears = []
            params.list('referenceYears').each { String referenceYear ->
                referenceYears << Year.parse(referenceYear)
            }
            qarams.put('referenceYears', referenceYears)
        }

        String orderQuery = " order by roleT.org.sortname, subT.name, subT.startDate, subT.endDate"
        if (params.sort?.size() > 0) {
            orderQuery = " order by " + params.sort + " " + params.order
        }

        if(params.filterSet && !params.member && !params.validOn && !params.status && !params.filterPropDef && !params.filterProp && !params.form && !params.resource && !params.subTypes)
            result.filterSet = false

        //log.debug( query + " " + orderQuery )
        // log.debug( qarams )

        if('withCostItems' in tableConf) {
            prf.setBenchmark('costs init')

            if (params.filterPvd) {
                query = query + " and exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.sub.id = subT.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and exists (select provRole from ProviderRole provRole where provRole.subscription = sub and provRole.provider.id in (:filterPvd))) "
                qarams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextOrg, filterPvd: Params.getLongList(params, 'filterPvd')]
            }
            if (params.filterVen) {
                query = query + " and exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.sub.id = subT.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and exists (select venRole from VendorRole venRole where venRole.subscription = sub and venRole.vendor.id in (:filterVen))) "
                qarams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextOrg, filterVen: Params.getLongList(params, 'filterVen')]
            }


            List allCosts = CostItem.executeQuery(query + " " + orderQuery, qarams) //.findAll { row -> row.cost == null || row.cost.owner.id == contextOrg.id } did not work
            Set costs = []
            // very ugly and subject of performance loss; keep an eye on that!
            allCosts.each { row ->
                if(row.cost == null || row.cost.owner.id == contextOrg.id) {
                    costs << row
                }
                else if(row.cost && row.cost.owner.id != contextOrg.id) {
                    row.cost = null
                    costs << row
                }
            }
            prf.setBenchmark('read off costs')
            //post filter; HQL cannot filter that parameter out
            result.costs = costs

            Map queryParamsProviders = [context: contextOrg]
            String queryProviders = 'select p from ProviderRole pvr join pvr.provider p where pvr.subscription in (select oo.sub from OrgRole oo where oo.org = :context) order by p.name',
            queryVendors = 'select v from VendorRole vr join vr.vendor v where vr.subscription in (select oo.sub from OrgRole oo where oo.org = :context) order by v.name'
            result.providers = Provider.executeQuery(queryProviders, queryParamsProviders) as Set<Provider>
            result.vendors = Vendor.executeQuery(queryVendors, queryParamsProviders) as Set<Vendor>
            result.totalCount = costs.size()
            Set<Subscription> uniqueSubs = []
            uniqueSubs.addAll(costs.sub)
            result.totalSubsCount = uniqueSubs.size()
            SortedSet<Org> totalMembers = new TreeSet<Org>()
            totalMembers.addAll(costs.orgs)
            result.totalMembers = totalMembers
            if(params.fileformat) {
                result.entries = costs
                result.entries.sub.each { Subscription subCons ->
                    //linkedLicenses.put(subCons,Links.findAllByDestinationSubscriptionAndLinkType(subCons,RDStore.LINKTYPE_LICENSE).collect { Links row -> genericOIDService.resolveOID(row.source)})
                    linkedLicenses.put(subCons, Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType', [subscription: subCons, linkType: RDStore.LINKTYPE_LICENSE]))
                }
            }
            else {
                result.entries = costs.drop((int) result.offset).take((int) result.max)

                result.finances = {
                    Map entries = [:]
                    result.entries.each { obj ->
                        if (obj.cost && obj.cost.owner.id == contextOrg.id) {
                            CostItem ci = (CostItem) obj.cost
                            if (!entries."${ci.billingCurrency}") {
                                entries."${ci.billingCurrency}" = 0.0
                            }
                            if (ci.costItemElementConfiguration == RDStore.CIEC_POSITIVE) {
                                entries."${ci.billingCurrency}" += ci.costInBillingCurrencyAfterTax
                            } else if (ci.costItemElementConfiguration == RDStore.CIEC_NEGATIVE) {
                                entries."${ci.billingCurrency}" -= ci.costInBillingCurrencyAfterTax
                            }
                            //result.totalMembers << ci.sub.getSubscriberRespConsortia()
                        }
                        if (obj.sub) {
                            Subscription subCons = (Subscription) obj.sub
                            //linkedLicenses.put(subCons,Links.findAllByDestinationSubscriptionAndLinkType(subCons,RDStore.LINKTYPE_LICENSE).collect { Links row -> genericOIDService.resolveOID(row.source)})
                            linkedLicenses.put(subCons, Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType', [subscription: subCons, linkType: RDStore.LINKTYPE_LICENSE]))
                        }
                    }
                    entries
                }()
            }
        }
        else if('onlyMemberSubs') {
            Set memberSubscriptions = Subscription.executeQuery(query,qarams)
            result.memberSubscriptions = memberSubscriptions
            if(memberSubscriptions) {
                memberSubscriptions.each { row ->
                    Subscription subCons = row[0]
                    linkedLicenses.put(subCons,Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:subCons,linkType:RDStore.LINKTYPE_LICENSE]))
                }
            }
            result.totalCount = memberSubscriptions.size()
            result.entries = memberSubscriptions.drop((int) result.offset).take((int) result.max)
        }
        result.linkedLicenses = linkedLicenses

        result.pu = prf

        result
    }

    /**
     * Gets a (filtered) list of subscriptions to which the context institution has reading rights
     * @param params the filter parameter map
     * @return a list of subscriptions matching the given filter
     */
    List getMySubscriptions_readRights(Map params){
        List result = []
        List tmpQ
        String queryStart = "select s "
        if(params.forDropdown) {
            queryStart = "select s.id, s.name, s.startDate, s.endDate, s.status, so.org, so.roleType, s.instanceOf.id, s.holdingSelection.id "
            params.joinQuery = "join s.orgRelations so"
        }

        if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            tmpQ = _getSubscriptionsConsortiaQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = _getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))
        }
        result
    }

    /**
     * Gets a (filtered) list of subscriptions to which the context institution has writing rights
     * @param params the filter parameter map
     * @return a list of subscriptions matching the given filter
     */
    List getMySubscriptions_writeRights(Map params){
        List result = []
        List tmpQ

        if (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            tmpQ = _getSubscriptionsConsortiaQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))
            if (params.showSubscriber) {
                List parents = result.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
                result.addAll(Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parents: parents, subscriberRoleTypes:subscriberRoleTypes]))
            }

            tmpQ = _getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))
        }
        if (params.showConnectedObjs){
            result.addAll(linksGenerationService.getAllLinkedSubscriptions(result, contextService.getUser()))
        }
        result.sort {it.dropdownNamingConvention()}
    }

    /**
     * Gets a (filtered) list of local and consortial subscriptions to which the context institution has reading rights
     * @param params the filter parameter map
     * @return a list of subscriptions matching the given filter
     */
    List getMySubscriptionsWithMyElements_readRights(Map params){
        List result = []
        List tmpQ

        if(contextService.getOrg().isCustomerType_Inst_Pro()) {

            tmpQ = _getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

        }
        result
    }

    /**
     * Gets a (filtered) list of local and consortial subscriptions to which the context institution has writing rights
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List getMySubscriptionsWithMyElements_writeRights(Map params){
        List result = []
        List tmpQ

        if(contextService.getOrg().isCustomerType_Inst_Pro()) {

            tmpQ = _getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = _getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))
        }

        result
    }

    /**
     * Retrieves consortial parent subscriptions matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent subscriptions matching the given filter
     */
    private List _getSubscriptionsConsortiaQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.showParentsAndChildsSubs = params.showSubscriber
        queryParams.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIUM.value
        String joinQuery = params.joinQuery ?: ""
        List result = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, joinQuery)
        result
    }

    /**
     * Retrieves consortial member subscriptions matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent subscriptions matching the given filter
     */
    private List _getSubscriptionsConsortialLicenseQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.orgRole = RDStore.OR_SUBSCRIBER.value
        queryParams.subTypes = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id
        String joinQuery = params.joinQuery ?: ""
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, joinQuery)
    }

    /**
     * Retrieves local licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    private List _getSubscriptionsLocalLicenseQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.orgRole = RDStore.OR_SUBSCRIBER.value
        queryParams.subTypes = RDStore.SUBSCRIPTION_TYPE_LOCAL.id
        String joinQuery = params.joinQuery ?: ""
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, joinQuery)
    }

    /**
     * Gets the member subscriptions for the given consortial subscription
     * @param subscription the subscription whose members should be queried
     * @return a list of member subscriptions
     */
    List<Subscription> getValidSubChilds(Subscription subscription) {
        List<Subscription> validSubChildren = Subscription.executeQuery('select oo.sub from OrgRole oo where oo.sub.instanceOf = :sub and oo.roleType in (:subRoleTypes) order by oo.org.sortname asc, oo.org.name asc',[sub:subscription,subRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
        validSubChildren
    }

    /**
     * Gets the member subscriptions for the given consortial subscription with status current
     * @param subscription the subscription whose members should be queried
     * @return a sorted list of member subscriptions which are marked as current
     */
    List getCurrentValidSubChilds(Subscription subscription) {
        List<Subscription> validSubChilds = Subscription.findAllByInstanceOfAndStatus(
                subscription,
                RDStore.SUBSCRIPTION_CURRENT
        )
        if(validSubChilds) {
            validSubChilds = validSubChilds?.sort { a, b ->
                Org sa = a.getSubscriberRespConsortia()
                Org sb = b.getSubscriberRespConsortia()
                (sa.sortname ?: sa.name ?: "")?.compareTo((sb.sortname ?: sb.name ?: ""))
            }
        }
        validSubChilds
    }

    /**
     * Gets the member subscriptions for the given consortial subscription matching to one of the following status:
     * <ul>
     *     <li>Current</li>
     *     <li>Under process of selection</li>
     *     <li>Intended</li>
     *     <li>Ordered</li>
     * </ul>
     * @param subscription the subscription whose members should be queried
     * @return a filtered list of member subscriptions
     */
    List getValidSurveySubChilds(Subscription subscription) {
        List<Subscription> validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
                subscription,
                [RDStore.SUBSCRIPTION_CURRENT,
                 RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION,
                 RDStore.SUBSCRIPTION_INTENDED,
                 RDStore.SUBSCRIPTION_ORDERED]
        )
        if(!validSubChilds){
            validSubChilds = Subscription.findAllByInstanceOfAndStatus(
                    subscription, subscription.status)
        }
        if(validSubChilds) {
            /*validSubChilds = validSubChilds?.sort { a, b ->
                def sa = a.getSubscriberRespConsortia()
                def sb = b.getSubscriberRespConsortia()
                (sa.sortname ?: sa.name ?: "")?.compareTo((sb.sortname ?: sb.name ?: ""))
            }*/
        }
        validSubChilds
    }

    /**
     * Gets the member subscribers for the given consortial subscription matching to one of the following status:
     * <ul>
     *     <li>Current</li>
     *     <li>Under process of selection</li>
     * </ul>
     * This method is to be used for afterward adding of members to the survey; irrelevant insitutions should be filtered out
     * @param subscription the subscription whose members should be queried
     * @return a filtered list subscribers
     */
    List getValidSurveySubChildOrgs(Subscription subscription) {
        List<Subscription> validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
                subscription,
                [RDStore.SUBSCRIPTION_CURRENT, RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION]
        )

        if(validSubChilds) {
            List<OrgRole> orgs = OrgRole.findAllBySubInListAndRoleType(validSubChilds, RDStore.OR_SUBSCRIBER_CONS)

            if (orgs) {
                return orgs.org
            } else {
                return []
            }
        }else {
            return []
        }
    }

    Set<Subscription> getSubscriptionsWithPossiblePerpetualTitles(Org subscriber) {
        //local subscriptions
        Set<Subscription> candidateSubscriptions = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :subscriber and oo.roleType = :local", [subscriber: subscriber, local: RDStore.OR_SUBSCRIBER])
        //consortial subscriptions with activated inheritance
        candidateSubscriptions.addAll(Subscription.executeQuery("select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscrCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')", [subscriber: subscriber, subscrCons: RDStore.OR_SUBSCRIBER_CONS]))
        candidateSubscriptions
    }

    /**
     * Gets the issue entitlements (= the titles) of the given subscription
     * @param subscription the subscription whose holding should be returned
     * @return a sorted list of issue entitlements
     */
    List getIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus order by ie.tipp.sortname",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])
                : []
        ies
    }

    /**
     * Gets issue entitlements for the given subscription which are under consideration
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlements which are currently under consideration
     */
    List getIssueEntitlementsUnderConsideration(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :ieStatus order by ie.tipp.sortname",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies
    }

    /**
     * Gets the current issue entitlements for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return integer of current issue entitlements
     */
    Integer countCurrentIssueEntitlements(Subscription subscription) {
        Integer countIes = subscription ?
                IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :ieStatus",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0]
                : 0
        countIes
    }

    /**
     * Gets the current issue entitlements for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return integer of all issue entitlements without removed
     */
    Integer countAllIssueEntitlements(Subscription subscription) {
        Integer countIes = subscription ?
                IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
                : 0
        countIes
    }

    Integer countCurrentIssueEntitlementsNotInIEGroup(Subscription subscription, IssueEntitlementGroup issueEntitlementGroup) {
        Integer countIes = subscription ?
                IssueEntitlement.executeQuery("select count(*) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :ieStatus " +
                        "and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])[0]
                : 0
        countIes
    }


    /**
     * Gets the current permanent titles for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return integer of current permanent titles
     */
    Integer countCurrentPermanentTitles(Subscription subscription) {
        return PermanentTitle.executeQuery("select count(*) from PermanentTitle as pi where pi.subscription = :sub and pi.issueEntitlement.status = :ieStatus",[sub: subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0]
    }

    /**
     * Gets the current permanent titles for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return integer of all permanent titles without removed
     */
    Integer countAllPermanentTitles(Subscription subscription) {
        return PermanentTitle.executeQuery("select count(*) from PermanentTitle as pi where pi.subscription = :sub and pi.issueEntitlement.status != :ieStatus",[sub: subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])[0]
    }

    /**
     * Gets the IDs of current issue entitlements for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return a list of issue entitlement IDs
     */
    List getCurrentIssueEntitlementIDs(Subscription subscription) {
        List<Long> ieIDs = subscription ? IssueEntitlement.executeQuery("select ie.id from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :cur ", [sub: subscription, cur: RDStore.TIPP_STATUS_CURRENT]) : []
        ieIDs
    }

    /**
     * Adds the cached title candidates to the holding and persists also eventually recorded enrichments of the titles
     * @param controller unused
     * @param params the request parameter map
     * @return OK if the persisting was successful, ERROR otherwise
     */
    Map<String,Object> processAddEntitlements(GrailsParameterMap params) {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            [result: null, status: SubscriptionControllerService.STATUS_ERROR]
        }
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            EhcacheWrapper cache = contextService.getUserCache("/subscription/addEntitlements/${result.subscription.id}")
            Map issueEntitlementCandidates = cache && cache.get('selectedTitles') ? cache.get('selectedTitles') : [:]
            if(!params.singleTitle) {
                Map checked = issueEntitlementCandidates.get('checked')
                if(checked) {
                    Set<Long> childSubIds = [], pkgIds = []
                    /*
                    if(params.withChildren == 'on') {
                        childSubIds.addAll(result.subscription.getDerivedSubscriptions().id)
                    }
                    */
                    checked.keySet().collate(65000).each { subSet ->
                        pkgIds.addAll(Package.executeQuery('select tipp.pkg.id from TitleInstancePackagePlatform tipp where tipp.gokbId in (:wekbIds)', [wekbIds: subSet]))
                    }
                    executorService.execute({
                        Thread.currentThread().setName("EntitlementEnrichment_${result.subscription.id}")
                        bulkAddEntitlements(result.subscription, checked.keySet(), false)
                        /*
                        if(params.withChildren == 'on') {
                            Sql sql = GlobalService.obtainSqlConnection()
                            try {
                                childSubIds.each { Long childSubId ->
                                    pkgIds.each { Long pkgId ->
                                        batchQueryService.bulkAddHolding(sql, childSubId, pkgId, result.subscription.hasPerpetualAccess, result.subscription.id)
                                    }
                                }
                            }
                            finally {
                                sql.close()
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
                    if(addSingleEntitlement(result.subscription, params.singleTitle, null, null))
                        log.debug("Added tipp ${params.singleTitle} to sub ${result.subscription.id}")
                    result.message = messageSource.getMessage('subscription.details.addEntitlements.titleAddToSub', args,locale)
                }
                catch(EntitlementCreationException e) {
                    result.error = e.getMessage()
                }
            }
            [result: result, status: SubscriptionControllerService.STATUS_OK]
        }
    }

    /**
     * Retrieves all visible organisational relationships for the given subscription, i.e. providers, agencies, etc.
     * @param subscription the subscription to retrieve the relations from
     * @return a sorted list of visible relations
     */
    List getVisibleOrgRelations(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { OrgRole or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name?.toLowerCase() }
    }

    /**
     * Retrieves all visible provider links for the given subscription
     * @param subscription the subscription to retrieve the relations from
     * @return a sorted list of visible relations
     */
    SortedSet<ProviderRole> getVisibleProviders(Subscription subscription) {
        SortedSet<ProviderRole> visibleProviderRelations = new TreeSet<ProviderRole>()
        visibleProviderRelations.addAll(ProviderRole.executeQuery('select pr from ProviderRole pr join pr.provider p where pr.subscription = :subscription order by p.name', [subscription: subscription]))
        visibleProviderRelations
    }

    /**
     * Retrieves all visible vendor links for the given subscription
     * @param subscription the subscription to retrieve the relations from
     * @return a sorted list of visible relations
     */
    SortedSet<VendorRole> getVisibleVendors(Subscription subscription) {
        SortedSet<VendorRole> visibleVendorRelations = new TreeSet<VendorRole>()
        visibleVendorRelations.addAll(VendorRole.executeQuery('select vr from VendorRole vr join vr.vendor v where vr.subscription = :subscription order by v.name', [subscription: subscription]))
        visibleVendorRelations
    }

    /**
     * Adds the given package to the given subscription. It may be specified if titles should be created as well or not
     * @param subscription the subscription whose holding should be enriched
     * @param pkg the package to link
     * @param createEntitlements should entitlements be created as well?
     */
    void addToSubscription(Subscription subscription, Package pkg, boolean createEntitlements) {
        Sql sql = GlobalService.obtainSqlConnection()
        try {
        sql.executeInsert('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_date_created, sp_last_updated) values (0, :pkgId, :subId, now(), now()) on conflict on constraint sub_package_unique do nothing', [pkgId: pkg.id, subId: subscription.id])
        /*
        List<SubscriptionPackage> dupe = SubscriptionPackage.executeQuery(
                "from SubscriptionPackage where subscription = :sub and pkg = :pkg", [sub: subscription, pkg: pkg])

            // Step 3 - If createEntitlements ...
        */
        if ( createEntitlements ) {
            //List packageTitles = sql.rows("select * from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk = :current", [pkgId: pkg.id, current: RDStore.TIPP_STATUS_CURRENT.id])
            batchQueryService.bulkAddHolding(sql, subscription.id, pkg.id, subscription.hasPerpetualAccess)
        }
        }
        finally {
            sql.close()
        }
    }

    /**
     * Adds the holding of the given package to the given member subscriptions, copying the stock of the given parent subscription if requested.
     * The method uses native SQL for copying the issue entitlements, (eventual) coverages and price items
     * @param subscription the parent {@link Subscription} whose holding serves as base
     * @param memberSubs the {@link List} of member {@link Subscription}s which should be linked to the given package
     * @param pkg the {@link de.laser.wekb.Package} to be linked
     * @param createEntitlements should {@link IssueEntitlement}s be created along with the linking?
     */
    void addToMemberSubscription(Subscription subscription, List<Subscription> memberSubs, Package pkg, boolean createEntitlements) {
        Sql sql = GlobalService.obtainSqlConnection()
        try {
        sql.withBatch('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_date_created, sp_last_updated) values (0, :pkgId, :subId, now(), now()) on conflict on constraint sub_package_unique do nothing') { BatchingPreparedStatementWrapper stmt ->
            memberSubs.each { Subscription memberSub ->
                stmt.addBatch([pkgId: pkg.id, subId: memberSub.id])
            }
        }

        if ( createEntitlements ) {
            int batchStep = 5000
            int total = sql.rows("select count(*) from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed", [pkgId: pkg.id, removed: RDStore.TIPP_STATUS_REMOVED.id])[0]["count"]
            //List packageTitles = sql.rows("select * from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk = :current", [pkgId: pkg.id, current: RDStore.TIPP_STATUS_CURRENT.id])
            sql.withBatch("insert into issue_entitlement (ie_version, ie_laser_id, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_perpetual_access_by_sub_fk) select " +
                    "0, concat('issueentitlement:',gen_random_uuid()), now(), now(), (select sub_id from subscription where sub_id = :subId), ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, (select case sub_has_perpetual_access when true then sub_id else null end from subscription where sub_id = :subId) from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                    "where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed limit :limit offset :offset") { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    for(int i = 0; i < total; i += batchStep) {
                        stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id, limit: batchStep, offset: i])
                    }
                }
            }
            if(subscription.hasPerpetualAccess) {
                //"insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), "+subId+", now(), ie_tipp_fk, "+ownerId+" from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId and tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_subscription_fk = :subId and pt_ie_fk = ie_id and pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)"
                // [subId: subId, pkgId: pkgId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId]
                sql.withBatch('insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select ' +
                        '0, (select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk = tipp_status_rv_fk), now(), :subId, now(), ie_tipp_fk, (select or_org_fk from org_role where or_sub_fk = :subId and or_roletype_fk = :subscrRole) from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id ' +
                        'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = (select or_org_fk from org_role where or_sub_fk = :subId and or_roletype_fk = :subscrRole))') { BatchingPreparedStatementWrapper stmt ->
                    memberSubs.each { Subscription memberSub ->
                        stmt.addBatch([subId: memberSub.id, parentId: subscription.id, pkgId: pkg.id, removed: RDStore.TIPP_STATUS_REMOVED.id, subscrRole: RDStore.OR_SUBSCRIBER_CONS.id])
                    }
                }
            }
            sql.withBatch('insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo) select ' +
                    '0, (select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id ' +
                    'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed') { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                }
            }
            sql.withBatch('insert into price_item (pi_version, pi_ie_fk, pi_date_created, pi_last_updated, pi_laser_id, pi_list_currency_rv_fk, pi_list_price) select ' +
                    "0, (select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_currency_rv_fk, pi_list_price from price_item join issue_entitlement on pi_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                    'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed') { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                }
            }
        }
        }
        finally {
            sql.close()
        }
    }

    /**
     * Copy from: {@link #addToSubscription(de.laser.Subscription, de.laser.wekb.Package, boolean)}
     * Adds the consortial title holding to the given member subscription and links the given package to the member
     * @param target the member subscription whose holding should be enriched
     * @param consortia the consortial subscription whose holding should be taken
     * @param pkg the package to be linked
     * @deprecated addToMemberSubscription() does the same thing
     */
    @Deprecated
    void addToSubscriptionCurrentStock(Subscription target, Subscription consortia, Package pkg, boolean withEntitlements) {
        Sql sql = GlobalService.obtainSqlConnection()
        try {
        sql.executeInsert('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_date_created, sp_last_updated) values (0, :pkgId, :subId, now(), now()) on conflict on constraint sub_package_unique do nothing', [pkgId: pkg.id, subId: target.id])
        //List consortiumHolding = sql.rows("select * from title_instance_package_platform join issue_entitlement on tipp_id = ie_tipp_fk where tipp_pkg_fk = :pkgId and ie_subscription_fk = :consortium and ie_status_rv_fk = :current", [pkgId: pkg.id, consortium: consortia.id, current: RDStore.TIPP_STATUS_CURRENT.id])
        if(withEntitlements)
            batchQueryService.bulkAddHolding(sql, target.id, pkg.id, target.hasPerpetualAccess, consortia.id)
        /*
        List<SubscriptionPackage> dupe = SubscriptionPackage.executeQuery(
                "from SubscriptionPackage where subscription = :sub and pkg = :pkg", [sub: target, pkg: pkg])

        if (! dupe){

            RefdataValue statusCurrent = RDStore.TIPP_STATUS_CURRENT

            SubscriptionPackage newSp = new SubscriptionPackage(subscription: target, pkg: pkg)
            newSp.save()

            IssueEntitlement.executeQuery(
                    "select ie from IssueEntitlement ie join ie.tipp tipp " +
                            "where tipp.pkg = :pkg and ie.status = :current and ie.subscription = :consortia ", [
                    pkg: pkg, current: statusCurrent, consortia: consortia
            ]).each { IssueEntitlement ie ->
                IssueEntitlement newIe = new IssueEntitlement(
                        status: statusCurrent,
                        subscription: target,
                        tipp: ie.tipp,
                        name: ie.name,
                        medium: ie.medium ?: ie.tipp.medium,
                        accessStartDate: ie.tipp.accessStartDate,
                        accessEndDate: ie.tipp.accessEndDate

                )
                newIe.generateSortTitle()
                if(newIe.save()) {
                    ie.tipp.coverages.each { TIPPCoverage covStmt ->
                        IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                                startDate: covStmt.startDate,
                                startVolume: covStmt.startVolume,
                                startIssue: covStmt.startIssue,
                                endDate: covStmt.endDate,
                                endVolume: covStmt.endVolume,
                                endIssue: covStmt.endIssue,
                                embargo: covStmt.embargo,
                                coverageDepth: covStmt.coverageDepth,
                                coverageNote: covStmt.coverageNote,
                                issueEntitlement: newIe
                        )
                        ieCoverage.save()
                    }
                    ie.priceItems.each { PriceItem pi ->
                        PriceItem priceItem = new PriceItem(
                                startDate: pi.startDate,
                                endDate: pi.endDate,
                                listPrice: pi.listPrice,
                                listCurrency: pi.listCurrency,
                                localPrice: pi.localPrice,
                                localCurrency: pi.localCurrency,
                                issueEntitlement: newIe
                        )
                        priceItem.save()
                    }
                }
            }
        }
         */
        }
        finally {
            sql.close()
        }
    }

    /**
     * Sets the submitted pending change behavior to the given subscription package
     * @param subscription the subscription to which the settings count
     * @param pkg the package whose title changes should be controlled
     * @param params the configuration map to be stored
     */
    void addPendingChangeConfiguration(Subscription subscription, Package pkg, Map<String, Object> params) {

        SubscriptionPackage subscriptionPackage = SubscriptionPackage.findBySubscriptionAndPkg(subscription, pkg)
        if(subscriptionPackage) {
            PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
                Map<String, Object> configMap = [subscriptionPackage: subscriptionPackage, settingKey: settingKey, withNotification: false]
                boolean auditable = false, memberNotification = false
                //Set because we have up to three keys in params with the settingKey
                Set<String> keySettings = params.keySet().findAll { k -> k.contains(settingKey) }
                keySettings.each { key ->
                    List<String> settingData = key.split('!§!')
                    switch (settingData[1]) {
                        case 'setting': configMap.settingValue = RefdataValue.get(params[key])
                            break
                        case 'auditable': auditable = true
                            break
                        case 'notificationAudit': memberNotification = true
                            break
                        case 'notification': configMap.withNotification = params[key] != null
                            break
                    }
                }
                try {
                    boolean hasConfig = AuditConfig.getConfig(subscriptionPackage.subscription, settingKey) != null
                    boolean hasNotificationConfig = AuditConfig.getConfig(subscriptionPackage.subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX) != null
                    PendingChangeConfiguration.construct(configMap) //create or update
                    if(!auditable && hasConfig) {
                        AuditConfig.removeConfig(subscriptionPackage.subscription, settingKey)
                    }
                    else if(auditable && !hasConfig) {
                        AuditConfig.addConfig(subscriptionPackage.subscription, settingKey)
                    }
                    if(!memberNotification && hasNotificationConfig) {
                        AuditConfig.removeConfig(subscriptionPackage.subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    }
                    else if(memberNotification && !hasNotificationConfig) {
                        AuditConfig.addConfig(subscriptionPackage.subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                    }
                }
                catch (CreationException e) {
                    log.error("ProcessLinkPackage -> PendingChangeConfiguration: " + e.message)
                }
            }
            /*
            subscriptionPackage.freezeHolding = params.freezeHolding == 'on'
            if(params.freezeHoldingAudit == 'on' && !AuditConfig.getConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING))
                AuditConfig.addConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING)
            else if(params.freezeHoldingAudit != 'on' && AuditConfig.getConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING))
                AuditConfig.removeConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING)
            */
        }
    }

    void switchPackageHoldingInheritance(Map configMap) {
        String prop = 'holdingSelection'
        Subscription sub = configMap.sub
        RefdataValue value = configMap.value
        sub.holdingSelection = value
        sub.save()
        Set<Subscription> members = Subscription.findAllByInstanceOf(sub)
        if(value == RDStore.SUBSCRIPTION_HOLDING_ENTIRE) {
            if(! AuditConfig.getConfig(sub, prop)) {
                AuditConfig.addConfig(sub, prop)

                members.each { Subscription m ->
                    m.setProperty(prop, sub.getProperty(prop))
                    m.save()
                }
            }
        }
        else {
            AuditConfig.removeConfig(sub, prop)
        }
        /*
        switch(value) {
            case RDStore.SUBSCRIPTION_HOLDING_ENTIRE:
                if(! AuditConfig.getConfig(sub, prop)) {
                    AuditConfig.addConfig(sub, prop)

                    members.each { Subscription m ->
                        m.setProperty(prop, sub.getProperty(prop))
                        m.save()
                    }
                }
                break
            case RDStore.SUBSCRIPTION_HOLDING_PARTIAL:
                members.each { Subscription m ->
                    m.setProperty(prop, sub.getProperty(prop))
                    m.save()
                }
                AuditConfig.removeConfig(sub, prop)
                break
            default:
                members.each { Subscription m ->
                    m.setProperty(prop, null)
                    m.save()
                }
                AuditConfig.removeConfig(sub, prop)
                break
        }
        */
    }

    /**
     * Adds the selected issue entitlement to the given subscription
     * @param sub the subscription to which the title should be added
     * @param gokbId the we:kb ID of the title
     * @param issueEntitlementOverwrite eventually cached imported local data
     * @param withPriceData should price data be added as well?
     * @param set ie in issueEntitlementGroup
     * @return true if the adding was successful, false otherwise
     * @throws EntitlementCreationException
     */
    boolean addSingleEntitlement(sub, gokbId, pickAndChoosePerpetualAccess, issueEntitlementGroup) {
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            log.error("Unable to tipp ${gokbId}")
        }else if(PermanentTitle.findByOwnerAndTipp(sub.getSubscriberRespConsortia(), tipp) || PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection'))", [subscriber: sub.getSubscriberRespConsortia(), subscriberCons: RDStore.OR_SUBSCRIBER_CONS, tipp: tipp]).size() > 0){
            log.error("Unable to create IssueEntitlement because IssueEntitlement exist as PermanentTitle")
        }
        else if(IssueEntitlement.findAllBySubscriptionAndTippAndStatusInList(sub, tipp, [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_RETIRED])) {
            log.error("Unable to create IssueEntitlement because IssueEntitlement exist with tipp ${gokbId}")
        }
        else if(IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)) {
            IssueEntitlement expected = IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)
            expected.status = RDStore.TIPP_STATUS_CURRENT
            if(!expected.save())
                log.error(expected.errors.getAllErrors().toListString())
        }
        else {
            IssueEntitlement new_ie = new IssueEntitlement(
					status: tipp.status,
                    subscription: sub,
                    tipp: tipp,
                    name: tipp.name,
                    medium: tipp.medium,
                    // ieReason: 'Manually Added by User'
                    )
            //new_ie.generateSortTitle()

            //fix for renewEntitlementsWithSurvey, overwrite TIPP status if holding's status differ
            IssueEntitlement parentIE = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(sub.instanceOf, tipp, RDStore.TIPP_STATUS_REMOVED)
            if(parentIE)
                new_ie.status = parentIE.status
            new_ie.accessStartDate = tipp.accessStartDate
            new_ie.accessEndDate = tipp.accessEndDate
            if (new_ie.save()) {

                if((pickAndChoosePerpetualAccess || sub.hasPerpetualAccess) && new_ie.status != RDStore.TIPP_STATUS_EXPECTED){
                    new_ie.perpetualAccessBySub = sub

                    if(!PermanentTitle.findByOwnerAndTipp(sub.getSubscriber(), tipp) && PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection'))", [subscriber: sub.getSubscriber(), subscriberCons: RDStore.OR_SUBSCRIBER_CONS, tipp: tipp]).size() == 0){
                        PermanentTitle permanentTitle = new PermanentTitle(subscription: sub,
                                issueEntitlement: new_ie,
                                tipp: tipp,
                                owner: sub.getSubscriber()).save()
                    }

                }

                if(issueEntitlementGroup) {
                    IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(ie: new_ie, ieGroup: issueEntitlementGroup)

                    if (!issueEntitlementGroupItem.save()) {
                        log.error(issueEntitlementGroupItem.errors)
                    }
                }
                true
            } else {
                log.error(new_ie.errors)
            }
        }
        false
    }

    Map<String, Object> selectEntitlementsWithKBART(MultipartFile inputFile, Map<String, Object> configMap) {
        Map<String, Object> result = issueEntitlementService.matchTippsFromFile(inputFile, configMap)
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime(LocaleUtils.getCurrentLocale())
        int perpetuallyPurchasedCount = 0
        Set<String> toBeAdded = []
        EhcacheWrapper userCache = contextService.getUserCache(configMap.progressCacheKey)
        if(result.matchedTitles) {
            int pointsPerIteration = 25/result.matchedTitles.size()
            result.matchedTitles.eachWithIndex { TitleInstancePackagePlatform match, Map<String, Object> externalTitleData, int i ->
                IssueEntitlement ieCheck = IssueEntitlement.findByTippAndSubscriptionAndStatusNotEqual(match, configMap.subscription, RDStore.TIPP_STATUS_REMOVED)
                //issue entitlement exists in subscription
                if(ieCheck) {
                    externalTitleData.put('found_in_package', RDStore.YN_YES.value)
                    result.notAddedTitles << externalTitleData
                }
                //issue entitlement does not exist in THIS subscription
                else {
                    boolean ptCheck = PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and (pt.owner = :subscriber or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))", [subscriber: configMap.subscription.getSubscriberRespConsortia(), subscriberCons: RDStore.OR_SUBSCRIBER_CONS, tipp: match]).size() > 0
                    //permanent title exists = title perpetually purchased!
                    if(ptCheck) {
                        perpetuallyPurchasedCount++
                        externalTitleData.put('found_in_package', RDStore.YN_YES.value)
                        externalTitleData.put('already_purchased_at', "${ptCheck.subscription.name} (${sdf.format(ptCheck.subscription.startDate)}-${sdf.format(ptCheck.subscription.endDate)})")
                        result.notAddedTitles << externalTitleData
                    }
                    //found nowhere ==> create new entitlement
                    else {
                        toBeAdded << match.gokbId
                    }
                }
                userCache.put('progress', 50+i*pointsPerIteration)
            }
        }
        userCache.put('progress', 75)
        if (toBeAdded) {
            Set<Long> childSubIds = [], pkgIds = []
            if(configMap.withChildrenKBART == 'on') {
                childSubIds.addAll(configMap.subscription.getDerivedSubscriptions().id)
            }
            pkgIds.addAll(Package.executeQuery('select tipp.pkg.id from TitleInstancePackagePlatform tipp where tipp.gokbId in (:wekbIds)', [wekbIds: toBeAdded]))
            //executorService.execute({
                //Thread.currentThread().setName("EntitlementEnrichment_${configMap.subscription.id}")
                bulkAddEntitlements(configMap.subscription, toBeAdded, configMap.subscription.hasPerpetualAccess)
                userCache.put('progress', 80)
                /*
                if(configMap.withChildrenKBART == 'on') {
                    Sql sql = GlobalService.obtainSqlConnection()
                    try {
                        childSubIds.each { Long childSubId ->
                            pkgIds.each { Long pkgId ->
                                batchQueryService.bulkAddHolding(sql, childSubId, pkgId, configMap.subscription.hasPerpetualAccess, configMap.subscription.id)
                            }
                        }
                    }
                    finally {
                        sql.close()
                    }
                }
                userCache.put('progress', 90)
                */
                if(globalService.isset(configMap, 'issueEntitlementGroupNewKBART') || globalService.isset(configMap, 'issueEntitlementGroupKBARTID')) {
                    IssueEntitlementGroup issueEntitlementGroup
                    if (configMap.issueEntitlementGroupNewKBART) {

                        IssueEntitlementGroup.withTransaction {
                            issueEntitlementGroup = IssueEntitlementGroup.findBySubAndName(configMap.subscription, params.issueEntitlementGroupNewKBART) ?: new IssueEntitlementGroup(sub: configMap.subscription, name: configMap.issueEntitlementGroupNewKBART).save()
                        }
                    }

                    if (configMap.issueEntitlementGroupKBARTID && configMap.issueEntitlementGroupKBARTID != '') {
                        issueEntitlementGroup = IssueEntitlementGroup.findById(Long.parseLong(configMap.issueEntitlementGroupKBARTID))
                    }

                    if (issueEntitlementGroup) {
                        issueEntitlementGroup.refresh()
                        Object[] keys = result.selectedTitles.toArray()
                        keys.each { String gokbUUID ->
                            TitleInstancePackagePlatform titleInstancePackagePlatform = TitleInstancePackagePlatform.findByGokbId(gokbUUID)
                            if (titleInstancePackagePlatform) {
                                IssueEntitlement ie = IssueEntitlement.findBySubscriptionAndTipp(configMap.subscription, titleInstancePackagePlatform)
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
            //})
            userCache.put('progress', 100)
            result.success = true
        }
        result.addedCount = toBeAdded.size()
        result.notAddedCount = result.toAddCount-result.addedCount
        result.perpetuallyPurchasedCount = perpetuallyPurchasedCount
        result
        /*
        InputStream stream = kbartFile.getInputStream()
        ArrayList<String> rows = stream.text.split('\n')
        int zdbCol = -1, onlineIdentifierCol = -1, printIdentifierCol = -1, titleUrlCol = -1, titleIdCol = -1, doiCol = -1
        Set<Package> subPkgs = SubscriptionPackage.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: subscription])
        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                       eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name),
                                                       isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                       issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name),
                                                       eisbn: IdentifierNamespace.findByNsAndNsType('eisbn', TitleInstancePackagePlatform.class.name),
                                                       doi: IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name),
                                                       title_id: IdentifierNamespace.findByNsAndNsType('title_id', TitleInstancePackagePlatform.class.name)]
        //read off first line of KBART file, pop the first row and prepare it for the error return list
        List titleRow = rows.remove(0).split('\t'), wrongTitles = [], truncatedRows = []
        titleRow.eachWithIndex { String headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case "zdb_id": zdbCol = c
                    break
                case "print_identifier": printIdentifierCol = c
                    break
                case "online_identifier": onlineIdentifierCol = c
                    break
                case "title_url": titleUrlCol = c
                    break
                case "title_id": titleIdCol = c
                    break
                case "doi_identifier": doiCol = c
                    break
            }
        }
        Set<String> selectedTitles = []
        rows.eachWithIndex{ String row, int i ->
            countRows++
            log.debug("now processing record ${i}")
            ArrayList<String> cols = row.split('\t', -1)
            if(cols.size() == titleRow.size()) {
                TitleInstancePackagePlatform match = null
                //cascade: 1. title_id, 2. title_url, 3. identifier map
                if(titleIdCol >= 0 && cols[titleIdCol] != null && !cols[titleIdCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[titleIdCol].trim(), ns: namespaces.title_id, removed: RDStore.TIPP_STATUS_REMOVED])
                    if(matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if(!match && titleUrlCol >= 0 && cols[titleUrlCol] != null && !cols[titleUrlCol].trim().isEmpty()) {
                    match = TitleInstancePackagePlatform.findByHostPlatformURLAndPkgInListAndStatusNotEqual(cols[titleUrlCol].trim(), subPkgs, RDStore.TIPP_STATUS_REMOVED)
                }
                if(!match && doiCol >= 0 && cols[doiCol] != null && !cols[doiCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[doiCol].trim(), ns: namespaces.doi, removed: RDStore.TIPP_STATUS_REMOVED])
                    if(matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if(!match && onlineIdentifierCol >= 0 && cols[onlineIdentifierCol] != null && !cols[onlineIdentifierCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: subPkgs, value: cols[onlineIdentifierCol].trim(), ns: [namespaces.eisbn, namespaces.eissn], removed: RDStore.TIPP_STATUS_REMOVED])
                    if(matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if(!match && printIdentifierCol >= 0 && cols[printIdentifierCol] != null && !cols[printIdentifierCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: subPkgs, value: cols[printIdentifierCol].trim(), ns: [namespaces.isbn, namespaces.issn], removed: RDStore.TIPP_STATUS_REMOVED])
                    if(matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if(!match && zdbCol >= 0 && cols[zdbCol] != null && !cols[zdbCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[zdbCol].trim(), ns: namespaces.zdb, removed: RDStore.TIPP_STATUS_REMOVED])
                    if(matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if(match) {
                    count++
                    IssueEntitlement ieMatch = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(subscription, match, RDStore.TIPP_STATUS_REMOVED)
                    if(ieMatch) {
                        wrongTitles << cols
                        countNotSelectTipps++
                    }
                    else {
                        selectedTitles << match.gokbId
                        countSelectTipps++
                    }
                }
                else
                {
                    wrongTitles << cols
                    countNotSelectTipps++
                }
            }
            else {
                truncatedRows << i
            }
        }
        [titleRow: titleRow, wrongTitles: wrongTitles, truncatedRows: truncatedRows.join(', '), selectedTitles: selectedTitles, processRows: countRows, processCount: count, countSelectTipps: countSelectTipps, countNotSelectTipps: countNotSelectTipps]
        */
    }

    /**
     * Deletes the given title from the given subscription
     * @param sub the subscription from which the title should be removed
     * @param gokbId the we:kb of the title to be removed
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteEntitlement(sub, gokbId) {
        IssueEntitlement ie = IssueEntitlement.findWhere(tipp: TitleInstancePackagePlatform.findByGokbId(gokbId), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
            ie.status = RDStore.TIPP_STATUS_REMOVED
            PermanentTitle permanentTitle = PermanentTitle.findByOwnerAndTipp(sub.getSubscriberRespConsortia(), ie.tipp)
            if (permanentTitle) {
                permanentTitle.delete()
            }

            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    /**
     * Deletes the given title from the given subscription
     * @param sub the subscription from which the title should be removed
     * @param id the database ID of the title to be removed
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteEntitlementByID(Subscription sub, String id, IssueEntitlementGroup ieg = null) {
        IssueEntitlement ie = IssueEntitlement.get(id)
        if(ie == null) {
            return false
        }
        else {
            if (ieg) {
                IssueEntitlementGroupItem iegi = IssueEntitlementGroupItem.findByIeGroupAndIe(ieg, ie)
                iegi.delete()
            }
            PermanentTitle permanentTitle = PermanentTitle.findByOwnerAndTipp(sub.getSubscriberRespConsortia(), ie.tipp)
            if (permanentTitle) {
                permanentTitle.delete()
            }
            ie.status = RDStore.TIPP_STATUS_REMOVED

            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    /**
     * Adds the given set of issue entitlements to the given subscription. The entitlement data may come directly from the package or be overwritten by individually negotiated content.
     * Because of the amount of data, the insert is done by native SQL
     * @param target the {@link Subscription} to which the entitlements should be attached
     * @param selectedTitles the {@link Map} containing identifiers of titles which have been selected for the enrichment
     * @param withPriceData should price data be added as well?
     * @param pickAndChoosePerpetualAccess are the given titles purchased perpetually?
     * @param issueEntitlementGroup if set, titles are being assigned to an issue entitlement group
     */
    void bulkAddEntitlements(Subscription sub, Set <String> selectedTitles, boolean pickAndChoosePerpetualAccess, IssueEntitlementGroup ieGroup = null) {
        Sql sql = GlobalService.obtainSqlConnection()
        try {
            Object[] keys = selectedTitles.toArray()
            sql.withTransaction {
                sql.executeUpdate('update issue_entitlement set ie_status_rv_fk = :current from title_instance_package_platform where ie_tipp_fk = tipp_id and ie_status_rv_fk = :expected and tipp_gokb_id = any(:keys) and ie_subscription_fk = :subId', [current: RDStore.TIPP_STATUS_CURRENT.id, expected: RDStore.TIPP_STATUS_EXPECTED.id, keys: sql.connection.createArrayOf('varchar', keys), subId: sub.id])
                Set<Map<String, Object>> ieDirectMapSet = []//, coverageDirectMapSet = [], priceItemDirectSet = []
                List<GroovyRowResult> existingEntitlements = sql.rows('select tipp_gokb_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId and ie_status_rv_fk != :removed', [subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                if(existingEntitlements) {
                    selectedTitles.removeAll(existingEntitlements['tipp_gokb_id'])
                }
                ieDirectMapSet.addAll(selectedTitles.collect { String wekbId ->
                    Map<String, Object> configMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id]
                    if(pickAndChoosePerpetualAccess || sub.hasPerpetualAccess){
                        configMap.perpetualAccessBySub = sub.id
                    }
                    configMap
                })
                /*
                selectedTitles.each { String wekbId ->
                    //log.debug "processing ${wekbId}"
                    List<GroovyRowResult> existingEntitlements = sql.rows('select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId and ie_status_rv_fk != :removed and tipp_gokb_id = :key',[subId: sub.id, current: RDStore.TIPP_STATUS_REMOVED.id, key: wekbId])
                    if(existingEntitlements.size() == 0) {
                        Map<String, Object> configMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id]//, coverageMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id], priceMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id]
                        if(pickAndChoosePerpetualAccess || sub.hasPerpetualAccess){
                            configMap.perpetualAccessBySub = sub.id
                        }
                        ieDirectMapSet << configMap
                        coverageDirectMapSet << coverageMap
                        priceItemDirectSet << priceMap
                    }
                }
                */
                ieDirectMapSet.eachWithIndex { Map<String, Object> configMap, int i ->
                    sql.withBatch(5000, "insert into issue_entitlement (ie_version, ie_laser_id, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_status_rv_fk, ie_access_start_date, ie_access_end_date, ie_perpetual_access_by_sub_fk) " +
                            "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${sub.id}, tipp_id, tipp_status_rv_fk, tipp_access_start_date, tipp_access_end_date, ${configMap.perpetualAccessBySub} from title_instance_package_platform where tipp_gokb_id = :wekbId") { BatchingStatementWrapper stmt ->
                        stmt.addBatch([wekbId: configMap.wekbId])
                    }
                }
                if(ieGroup) {
                    ieDirectMapSet.eachWithIndex { Map<String, Object> configMap, int i ->
                        sql.withBatch(5000, "insert into issue_entitlement_group_item (igi_version, igi_date_created, igi_last_updated, igi_ie_fk, igi_ie_group_fk) " +
                                "select 0, now(), now(), ie_id, ${ieGroup.id} from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_gokb_id = :wekbId and ie_subscription_fk = :subId") { stmt ->
                            stmt.addBatch([wekbId: configMap.wekbId, subId: sub.id])
                        }
                    }
                }
                if(sub.hasPerpetualAccess) {
                    Long ownerId = sub.getSubscriberRespConsortia().id
                    ieDirectMapSet.eachWithIndex { Map<String, Object> configMap, int i ->
                        sql.withBatch(5000, "insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) " +
                                "select 0, ie_id, now(), ie_subscription_fk, now(), ie_tipp_fk, "+ownerId+" from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_gokb_id = :wekbId and ie_subscription_fk = :subId") { BatchingPreparedStatementWrapper stmt ->
                            stmt.addBatch([wekbId: configMap.wekbId, subId: sub.id])
                        }
                    }
                }
                /*
                coverageDirectMapSet.each { Map<String, Object> configMap ->
                    sql.withBatch("insert into issue_entitlement_coverage (ic_version, ic_date_created, ic_last_updated, ic_start_date, ic_start_issue, ic_start_volume, ic_end_date, ic_end_issue, ic_end_volume, ic_coverage_depth, ic_coverage_note, ic_embargo, ic_ie_fk) " +
                            "select 0, now(), now(), tc_start_date, tc_start_issue, tc_start_volume, tc_end_date, tc_end_issue, tc_end_volume, tc_coverage_depth, tc_coverage_note, tc_embargo, ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join tippcoverage on tc_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed") { BatchingStatementWrapper stmt ->
                        stmt.addBatch(configMap)
                    }
                }
                priceItemDirectSet.each { Map<String, Object> configMap ->
                    sql.withBatch("insert into price_item (pi_version, pi_date_created, pi_last_updated, pi_laser_id, pi_list_price, pi_list_currency_rv_fk, pi_ie_fk) " +
                            "select 0, now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_price, pi_list_currency_rv_fk, ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join price_item on pi_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed") { BatchingStatementWrapper stmt ->
                        stmt.addBatch(configMap)
                    }
                }
                */
            }
        }
        finally {
            sql.close()
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
    Map<String,Object> renewEntitlementsWithSurvey(GrailsParameterMap params) {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            [result: null, status: SubscriptionControllerService.STATUS_ERROR]
        }
        else  {
            result.putAll(getRenewalGenerics(params))
            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
            if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                result.countCurrentPermanentTitles = surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(result.subscription, result.surveyConfig)
            }
            if(issueEntitlementGroup) {
                result.titleGroup = issueEntitlementGroup
                if(!result.surveyConfig.pickAndChoosePerpetualAccess)
                    result.countCurrentPermanentTitles = countCurrentIssueEntitlementsNotInIEGroup(result.subscription, issueEntitlementGroup)
            }
            else {
                result.titleGroup = null
                if(!result.surveyConfig.pickAndChoosePerpetualAccess)
                    result.countCurrentPermanentTitles = 0
            }
            Map<String, Object> parameterGenerics = issueEntitlementService.getParameterGenerics(result.configMap)
            Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                                identifierConfigMap = parameterGenerics.identifierConfigMap,
                                issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
            //build up title data
            if(result.configMap.containsKey('tippStatus') && result.configMap.containsKey('ieStatus')) {
                titleConfigMap.tippStatus = result.configMap.tippStatus[0]
                issueEntitlementConfigMap.ieStatus = result.configMap.ieStatus[0]
            }
            else if(!result.configMap.containsKey('status')) {
                titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
                issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
            }
            Map<String, Object> query = filterService.getTippSubsetQuery(titleConfigMap)
            Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
            if(result.configMap.identifier) {
                tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, result.configMap.identifier))
            }
            if (result.configMap.containsKey('hasPerpetualAccess')) {
                String permanentTitleStringQuery = "select tipp.hostPlatformURL from PermanentTitle pt join pt.tipp tipp where (pt.owner = :subscriber or pt.subscription in (select oo.sub.instanceOf from OrgRole oo where oo.org = :subscriber and oo.roleType = :subscriberRole)) and tipp.status = :current",
                permanentTitlesQuery = "select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg in (:currSubPkgs) and tipp.hostPlatformURL in (:subSet) and tipp.status = :current"
                Set<String> permanentTitleURLs = TitleInstancePackagePlatform.executeQuery(permanentTitleStringQuery, [subscriber: result.subscription.getSubscriber(), current: RDStore.TIPP_STATUS_CURRENT, subscriberRole: RDStore.OR_SUBSCRIBER_CONS])
                Set<Long> tippIDsPurchasedGlobally = []
                permanentTitleURLs.collate(65000).each { subSet ->
                    tippIDsPurchasedGlobally.addAll(TitleInstancePackagePlatform.executeQuery(permanentTitlesQuery, [subSet: subSet, currSubPkgs: result.subscription.packages.pkg, current: RDStore.TIPP_STATUS_CURRENT]))
                }
                if (RefdataValue.get(result.configMap.hasPerpetualAccess) == RDStore.YN_YES) {
                    tippIDs = tippIDs.intersect(tippIDsPurchasedGlobally)
                }
                else {
                    tippIDs.removeAll(tippIDsPurchasedGlobally)
                }
            }
            EhcacheWrapper userCache = contextService.getUserCache("/subscription/renewEntitlementsWithSurvey/${result.subscription.id}?${params.tab}")
            Map<String, Object> checkedCache = userCache.get('selectedTitles')

            if (!checkedCache || !params.containsKey('pagination')) {
                checkedCache = ["checked": [:]]
            }

            Set<Subscription> subscriptions = [], parentSubs = []
            if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                subscriptions = linksGenerationService.getSuccessionChain(result.subscription, 'sourceSubscription')
            }
            //else {
            subscriptions << result.subscription
            //}
            subscriptions.each { Subscription s ->
                if(s.instanceOf && auditService.getAuditConfig(s.instanceOf, 'holdingSelection'))
                    parentSubs << s.instanceOf
            }
            subscriptions.addAll(parentSubs)

            result.checkedCache = checkedCache.get('checked')
            result.checkedCount = result.checkedCache.findAll { it.value == 'checked' }.size()
            switch(params.tab) {
                case 'allTipps':
                    Map<String, Object> listPriceSums = issueEntitlementService.calculateListPriceSumsForTitles(tippIDs)
                    result.tippsListPriceSumEUR = listPriceSums.listPriceSumEUR
                    result.tippsListPriceSumUSD = listPriceSums.listPriceSumUSD
                    result.tippsListPriceSumGBP = listPriceSums.listPriceSumGBP
                    result.titlesList = tippIDs ? TitleInstancePackagePlatform.findAllByIdInList(tippIDs.drop(result.offset).take(result.max), [sort: result.sort, order: result.order]) : []
                    result.num_rows = tippIDs.size()
                    if (tippIDs.size() > 0 && tippIDs.size() == result.checkedCount) {
                        result.allChecked = "checked"
                    }
                    break
                case 'selectableTipps':
                    if(subscriptions) {
                        Set rows
                        if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                            rows = IssueEntitlement.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)', [subs: subscriptions, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                        }
                        else {
                            rows = IssueEntitlement.executeQuery('select ie.tipp.hostPlatformURL from IssueEntitlement ie where ie.subscription = :sub and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)', [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                        }
                        rows.addAll(issueEntitlementService.getPerpetuallyPurchasedTitleHostPlatformURLs(result.subscription.getSubscriber(), subscriptions))
                        rows.collate(65000).each { subSet ->
                            tippIDs.removeAll(TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.hostPlatformURL in (:subSet) and tipp.pkg in (:currSubPkgs)', [subSet: subSet, currSubPkgs: result.subscription.packages.pkg]))
                        }
                    }
                    if (tippIDs.size() > 0 && tippIDs.size() == result.checkedCount) {
                        result.allChecked = "checked"
                    }
                    Map<String, Object> listPriceSums = issueEntitlementService.calculateListPriceSumsForTitles(tippIDs)
                    result.tippsListPriceSumEUR = listPriceSums.listPriceSumEUR
                    result.tippsListPriceSumUSD = listPriceSums.listPriceSumUSD
                    result.tippsListPriceSumGBP = listPriceSums.listPriceSumGBP
                    result.titlesList = tippIDs ? TitleInstancePackagePlatform.findAllByIdInList(tippIDs.drop(result.offset).take(result.max), [sort: result.sort, order: result.order]) : []
                    result.num_rows = tippIDs.size()
                    break
                case 'selectedIEs':
                    if(issueEntitlementGroup) {
                        issueEntitlementConfigMap.tippIDs = tippIDs
                        Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetSQLQuery(issueEntitlementConfigMap)
                        Set<Long> sourceIEs = [], sourceTIPPs = []
                        List<GroovyRowResult> rows = batchQueryService.longArrayQuery(queryPart2.query, queryPart2.arrayParams, queryPart2.queryParams)
                        sourceIEs.addAll(rows["ie_id"])
                        sourceTIPPs.addAll(rows["tipp_id"])
                        result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs.drop(result.offset).take(result.max), [sort: result.sort, order: result.order]) : []
                        result.num_rows = sourceIEs.size()
                        if (sourceIEs.size() > 0 && sourceIEs.size() == result.checkedCount) {
                            result.allChecked = "checked"
                        }
                        Map<String, Object> listPriceSums = issueEntitlementService.calculateListPriceSumsForTitles(sourceTIPPs)
                        result.iesTotalListPriceSumEUR = listPriceSums.listPriceSumEUR
                        result.iesTotalListPriceSumUSD = listPriceSums.listPriceSumUSD
                        result.iesTotalListPriceSumGBP = listPriceSums.listPriceSumGBP
                        List counts = IssueEntitlement.executeQuery('select new map(count(*) as count, status as status) from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup = :titleGroup and iegi.ie = ie) group by status', [titleGroup: issueEntitlementGroup, sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])
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
                    break
                case 'currentPerpetualAccessIEs':
                    if(subscriptions) {
                        Set<Long> sourceIEs = [], sourceTIPPs = []
                        List rows
                        String orderClause
                        if(params.sort && params.order)
                            orderClause = "order by ${params.sort} ${params.order}"
                        else
                            orderClause = "order by ie.tipp.sortname asc"
                        if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                            rows = IssueEntitlement.executeQuery('select new map(ie.id as ie_id, tipp.id as tipp_id) from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :current and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) group by tipp.hostPlatformURL, tipp.id, ie.id '+orderClause, [subs: subscriptions, current: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                        }
                        else {
                            rows = IssueEntitlement.executeQuery('select new map(ie.id as ie_id, ie.tipp.id as tipp_id) from IssueEntitlement ie where ie.subscription = :sub and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) '+orderClause, [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                        }
                        Set<String> hostPlatformURLs = issueEntitlementService.getPerpetuallyPurchasedTitleHostPlatformURLs(result.subscription.getSubscriber(), subscriptions)
                        Set<Package> pkgs = Package.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription in (:subscriptions)', [subscriptions: subscriptions])
                        rows.addAll(IssueEntitlement.executeQuery("select new map(pt.issueEntitlement.id as ie_id, tipp.id as tipp_id) from PermanentTitle pt join pt.tipp tipp where tipp.hostPlatformURL in (:hostPlatformURLs) and tipp.pkg in (:pkgs) and ((pt.owner = :subscriber and pt.subscription.instanceOf = null) or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscrCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))", [subscriber: result.subscription.getSubscriber(), subscrCons: RDStore.OR_SUBSCRIBER_CONS, pkgs: pkgs, hostPlatformURLs: hostPlatformURLs]))
                        sourceIEs.addAll(rows["ie_id"])
                        sourceTIPPs.addAll(rows["tipp_id"])
                        result.sourceIEs = sourceIEs ? IssueEntitlement.findAllByIdInList(sourceIEs.drop(result.offset).take(result.max), [sort: result.sort, order: result.order]) : []
                        result.num_rows = sourceIEs.size()
                        Map<String, Object> listPriceSums = issueEntitlementService.calculateListPriceSumsForTitles(sourceTIPPs)
                        result.iesTotalListPriceSumEUR = listPriceSums.listPriceSumEUR
                        result.iesTotalListPriceSumUSD = listPriceSums.listPriceSumUSD
                        result.iesTotalListPriceSumGBP = listPriceSums.listPriceSumGBP
                    }
                    else {
                        result.sourceIEs = []
                        result.num_rows = 0
                    }
                    break
            }
            [result: result, status: SubscriptionControllerService.STATUS_OK]
        }
        /*


        else {
            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
            result.titleGroupID = issueEntitlementGroup ? issueEntitlementGroup.id : null
            result.titleGroup = issueEntitlementGroup
            result.preselectValues = params.preselectValues == 'on'

            Subscription previousSubscription = result.subscription._getCalculatedPreviousForSurvey()
            result.subscriber = result.subscription.getSubscriberRespConsortia()

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

                if (params.hasPerpetualAccess) {
                    params.hasPerpetualAccessBySubs = subscriptions
                }

                List<Long> sourceIEs = []


                <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                                ${surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subParticipant, surveyConfig)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:if>
                            <g:else>
                                ${subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subParticipant, ieGroup)} / ${surveyService.countIssueEntitlementsByIEGroup(subParticipant, surveyConfig)}
                            </g:else>

                if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                    result.countCurrentPermanentTitles = surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(subscriberSub, result.surveyConfig)
                }
                else {
                    result.countCurrentPermanentTitles = issueEntitlementGroup ? subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(subscriberSub, issueEntitlementGroup) : 0
                }


            if (result.surveyConfig.pickAndChoosePerpetualAccess) {
                result.countCurrentIEs = surveyService.countPerpetualAccessTitlesBySub(result.subscription)
            } else {
                result.countCurrentIEs = subscriptionService.countCurrentIssueEntitlements(result.subscription)
            }
            }

            result.subscriberSub = subscriberSub
            result.parentSubscription = baseSub
            //result.allSubscriptions = subscriptions
            result.previousSubscription = previousSubscription


            if(result.surveyInfo.owner.id == contextService.getOrg().id) {
                result.participant = result.subscriber
            }

            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyInfo)

            [result:result,status:STATUS_OK]
        }
        */
    }

    /**
     * Takes the submitted input and adds the selected titles to the (preliminary) subscription holding from cache
     * @param params the request parameter map
     * @return OK if the selection (adding and/or removing of issue entitlements) was successful, ERROR otherwise
     */
    Map<String,Object> processRenewEntitlementsWithSurvey(GrailsParameterMap params) {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            [result: null, status: SubscriptionControllerService.STATUS_ERROR]
        }
        else {
            Locale locale = LocaleUtils.getCurrentLocale()
            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)
            if(!result.editable) {
                [result: null, status: SubscriptionControllerService.STATUS_ERROR]
            }
            EhcacheWrapper userCache = contextService.getUserCache("/subscription/renewEntitlementsWithSurvey/${params.id}?${params.tab}")
            Map<String, Object> checkedCache = userCache.get('selectedTitles') ?: [:]

            result.checkedCache = checkedCache.get('checked')
            result.checked = result.checkedCache.findAll { it.value == 'checked' }
            if(result.checked.size() < 1){
                result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps',null,locale)
                [result: result, status: SubscriptionControllerService.STATUS_ERROR]
            }
            else if(params.process == "add" && result.checked.size() > 0) {
                Integer countTippsToAdd = 0
                result.checked.each {
                    TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(it.key)
                    if(tipp) {
                        try {

                            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)

                            if (!issueEntitlementGroup) {
                                issueEntitlementGroup = new IssueEntitlementGroup(surveyConfig: result.surveyConfig, sub: result.subscription, name: result.surveyConfig.issueEntitlementGroupName).save()
                            }

                            if (issueEntitlementGroup && addSingleEntitlement(result.subscription, tipp.gokbId, result.surveyConfig.pickAndChoosePerpetualAccess, issueEntitlementGroup)) {
                                log.debug("Added tipp ${tipp.gokbId} to sub ${result.subscription.id}")
                                ++countTippsToAdd
                            }
                        }
                        catch (EntitlementCreationException e) {
                            log.debug("Error: Adding tipp ${tipp} to sub ${result.subscription.id}: " + e.getMessage())
                            result.error = messageSource.getMessage('renewEntitlementsWithSurvey.noSelectedTipps', null, locale)
                            [result: result, status: SubscriptionControllerService.STATUS_ERROR]
                        }
                    }
                }
                if(countTippsToAdd > 0){
                    Object[] args = [countTippsToAdd]
                    result.message = messageSource.getMessage('renewEntitlementsWithSurvey.tippsToAdd',args,locale)
                }
            }
            else if(params.process == "remove" && result.checked.size() > 0) {
                Integer countIEsToDelete = 0
                result.checked.each {
                    IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
                    if(IssueEntitlement.countById(Long.parseLong(it.key)) && issueEntitlementGroup) {
                        if (deleteEntitlementByID(result.subscription, it.key, issueEntitlementGroup)) {
                            ++countIEsToDelete
                        }
                    }
                }
                if(countIEsToDelete > 0){
                    Object[] args = [countIEsToDelete]
                    result.message = messageSource.getMessage('renewEntitlementsWithSurvey.tippsToDelete',args,locale)
                }
            }
            userCache.remove('selectedTitles')
            [result:result,status:SubscriptionControllerService.STATUS_OK]
        }
    }

    Map<String, Object> addSingleEntitlementSurvey(GrailsParameterMap params) {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)
        if (result.subscription) {
            if(result.editable && params.containsKey('singleTitle')) {
                TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.singleTitle)
                try {

                    IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)

                    if (!issueEntitlementGroup) {
                        issueEntitlementGroup = new IssueEntitlementGroup(surveyConfig: result.surveyConfig, sub: result.subscription, name: result.surveyConfig.issueEntitlementGroupName).save()
                    }

                    if (issueEntitlementGroup && addSingleEntitlement(result.subscription, tipp.gokbId, result.surveyConfig.pickAndChoosePerpetualAccess, issueEntitlementGroup)) {
                        result.message = message(code: 'subscription.details.addEntitlements.titleAddToSub', args: [tipp.name]) as String
                    } else {
                        log.error("no issueEntitlementGroup found and no issueEntitlementGroup created, because it is not set a issueEntitlementGroupName in survey config!")
                    }
                }
                catch (EntitlementCreationException e) {
                    result.error = e.getMessage()
                }

            }
        } else {
            log.error("Unable to locate subscription instance")
        }
        result
    }

    void linkTitle(Subscription subscription, Package pkgToLink, TitleInstancePackagePlatform tippToLink, boolean linkToChildren) {
        addToSubscription(subscription, pkgToLink, false)
        addSingleEntitlement(subscription, tippToLink.gokbId, null, null)
        if(linkToChildren) {
            List<Subscription> memberSubs = Subscription.findAllByInstanceOf(subscription)
            addToMemberSubscription(subscription, memberSubs, pkgToLink, false)
            memberSubs.each {Subscription memberSub ->
                addSingleEntitlement(memberSub, tippToLink.gokbId, null, null)
            }
        }
    }

    Map<String, Object> exportRenewalEntitlements(GrailsParameterMap params) {
        Map<String,Object> result = getRenewalGenerics(params)
        Locale locale = LocaleUtils.getCurrentLocale()
        if (!result) {
            [result: null, status: SubscriptionControllerService.STATUS_ERROR]
        }
        else {
            EhcacheWrapper userCache = contextService.getUserCache("/subscription/renewEntitlementsWithSurvey/generateExport")
            userCache.put('progress', 0)
            String filename, extension
            switch(params.tab) {
                case 'allTipps': filename = escapeService.escapeString(messageSource.getMessage('renewEntitlementsWithSurvey.selectableTitles', null, locale) + '_' + result.subscription.dropdownNamingConvention())
                    break
                case 'selectableTipps': filename = escapeService.escapeString(messageSource.getMessage('renewEntitlementsWithSurvey.selectableTipps', null, locale) + '_' + result.subscription.dropdownNamingConvention())
                    break
                case 'selectedIEs': filename = escapeService.escapeString(messageSource.getMessage('renewEntitlementsWithSurvey.currentTitlesSelect', null, locale) + '_' + result.subscription.dropdownNamingConvention())
                    break
                case 'currentPerpetualAccessIEs': filename = escapeService.escapeString(messageSource.getMessage('renewEntitlementsWithSurvey.currentTitles', null, locale) + '_' + result.subscription.dropdownNamingConvention())
                    break
                case 'usage':
                    filename = "renewal_${params.reportType}_${params.platform}_${result.subscriber.id}_${result.subscription.id}"
                    if(params.metricType) {
                        filename += '_'+params.list('metricType').join('_')
                    }
                    if(params.accessType) {
                        filename += '_'+params.list('accessType').join('_')
                    }
                    if(params.accessMethod) {
                        filename += '_'+params.list('accessMethod').join('_')
                    }
                    break
            }
            switch(params.exportConfig) {
                case ExportService.KBART: extension = '.tsv'
                    break
                case ExportService.EXCEL: extension = '.xlsx'
                    break
            }
            if(filename && extension) {
                result.filename = filename
                filename += extension
                result.token = filename
                String dir = GlobalService.obtainTmpFileLocation()
                File f = new File(dir+'/'+filename)
                if(!f.exists()) {
                    IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
                    if(issueEntitlementGroup) {
                        result.titleGroup = issueEntitlementGroup
                    }
                    else {
                        result.titleGroup = null
                    }
                    Map<String, Object> parameterGenerics = issueEntitlementService.getParameterGenerics(result.configMap)
                    Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                                        identifierConfigMap = parameterGenerics.identifierConfigMap,
                                        issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
                    //build up title data
                    if(result.configMap.containsKey('tippStatus') && result.configMap.containsKey('ieStatus')) {
                        titleConfigMap.tippStatus = result.configMap.tippStatus[0]
                        issueEntitlementConfigMap.ieStatus = result.configMap.ieStatus[0]
                    }
                    else if(!result.configMap.containsKey('status')) {
                        titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
                        issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
                    }
                    Map<String, Object> query = filterService.getTippSubsetQuery(titleConfigMap), exportData = [:]
                    Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
                    if(result.identifier) {
                        tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, result.identifier))
                    }
                    userCache.put('progress', 20)
                    Set<Subscription> subscriptions = [], parentSubs = []
                    if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                        subscriptions = linksGenerationService.getSuccessionChain(result.subscription, 'sourceSubscription')
                    }
                    //else {
                    subscriptions << result.subscription
                    subscriptions.each { Subscription s ->
                        if(s.instanceOf && auditService.getAuditConfig(s.instanceOf, 'holdingSelection'))
                            parentSubs << s.instanceOf
                    }
                    subscriptions.addAll(parentSubs)
                    Set<String> perpetuallyPurchasedTitleURLs = issueEntitlementService.getPerpetuallyPurchasedTitleHostPlatformURLs(result.subscription.getSubscriber(), subscriptions)
                    switch(params.tab) {
                        case 'allTipps':
                            exportData = exportService.generateTitleExport([format: params.exportConfig, tippIDs: tippIDs, perpetuallyPurchasedTitleURLs: perpetuallyPurchasedTitleURLs, withPick: true])
                            break
                        case 'selectableTipps':
                            //}
                            if(subscriptions) {
                                Set rows
                                //and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) sense?
                                if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                                    rows = IssueEntitlement.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :current group by tipp.hostPlatformURL, tipp.id, ie.id', [subs: subscriptions, current: RDStore.TIPP_STATUS_CURRENT])
                                }
                                else {
                                    rows = IssueEntitlement.executeQuery('select ie.tipp.hostPlatformURL from IssueEntitlement ie where ie.subscription = :sub and ie.status = :ieStatus', [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                                }
                                rows.addAll(perpetuallyPurchasedTitleURLs)
                                rows.collate(65000).each { subSet ->
                                    tippIDs.removeAll(TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.hostPlatformURL in (:subSet) and tipp.pkg in (:currSubPkgs)', [subSet: subSet, currSubPkgs: result.subscription.packages.pkg]))
                                }
                            }
                            exportData = exportService.generateTitleExport([format: params.exportConfig, tippIDs: tippIDs, perpetuallyPurchasedTitleURLs: perpetuallyPurchasedTitleURLs, withPick: true])
                            break
                        case 'selectedIEs':
                            Set<Long> sourceTIPPs = []
                            if(issueEntitlementGroup) {
                                issueEntitlementConfigMap.tippIDs = tippIDs
                                Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetSQLQuery(issueEntitlementConfigMap)
                                List<GroovyRowResult> rows = batchQueryService.longArrayQuery(queryPart2.query, queryPart2.arrayParams, queryPart2.queryParams)
                                sourceTIPPs.addAll(rows["tipp_id"])
                            }
                            exportData = exportService.generateTitleExport([format: params.exportConfig, tippIDs: sourceTIPPs])
                            break
                        case 'currentPerpetualAccessIEs':
                            Set<Long> sourceTIPPs = []
                            if(subscriptions) {
                                List rows
                                if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                                    rows = IssueEntitlement.executeQuery('select new map(ie.id as ie_id, tipp.id as tipp_id) from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :current and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) group by tipp.hostPlatformURL, tipp.id, ie.id order by tipp.sortname', [subs: subscriptions, current: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                                }
                                else {
                                    rows = IssueEntitlement.executeQuery('select new map(ie.id as ie_id, ie.tipp.id as tipp_id) from IssueEntitlement ie where ie.subscription = :sub and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) order by ie.tipp.sortname', [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                                }
                                sourceTIPPs.addAll(rows["tipp_id"])
                                Set<String> hostPlatformURLs = issueEntitlementService.getPerpetuallyPurchasedTitleHostPlatformURLs(result.subscription.getSubscriber(), subscriptions)
                                Set<Package> pkgs = Package.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription in (:subscriptions)', [subscriptions: subscriptions])
                                sourceTIPPs.addAll(IssueEntitlement.executeQuery("select tipp.id as tipp_id from PermanentTitle pt join pt.tipp tipp where tipp.hostPlatformURL in (:hostPlatformURLs) and tipp.pkg in (:pkgs) and ((pt.owner = :subscriber and pt.subscription.instanceOf = null) or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscrCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))", [subscriber: result.subscription.getSubscriber(), subscrCons: RDStore.OR_SUBSCRIBER_CONS, pkgs: pkgs, hostPlatformURLs: hostPlatformURLs]))
                            }
                            exportData = exportService.generateTitleExport([format: params.exportConfig, tippIDs: sourceTIPPs])
                            break
                        case 'usage':
                            userCache.put('progress', 30)
                            SortedSet<Date> monthsInRing = new TreeSet<Date>()
                            Calendar startTime = GregorianCalendar.getInstance(), endTime = GregorianCalendar.getInstance()
                            if (result.subscription.startDate && result.subscription.endDate) {
                                startTime.setTime(result.subscription.startDate)
                                if (result.subscription.endDate < new Date())
                                    endTime.setTime(result.subscription.endDate)
                            }
                            else if (result.subscription.startDate) {
                                startTime.setTime(result.subscription.startDate)
                                endTime.setTime(new Date())
                            }
                            else {
                                //test access e.g.
                                startTime.set(Calendar.MONTH, 0)
                                startTime.set(Calendar.DAY_OF_MONTH, 1)
                                startTime.add(Calendar.YEAR, -1)
                                endTime.setTime(new Date())
                            }
                            Map<String, Object> sushiQueryMap = [revision: params.revision,
                                                                 reportType: params.reportType,
                                                                 metricTypes: params.metricType,
                                                                 accessTypes: params.accessType,
                                                                 accessMethods: params.accessMethod,
                                                                 customer: result.subscriber,
                                                                 platform: Platform.get(params.platform),
                                                                 startDate: startTime.getTime(),
                                                                 endDate: endTime.getTime()]
                            while (startTime.before(endTime)) {
                                monthsInRing << startTime.getTime()
                                startTime.add(Calendar.MONTH, 1)
                            }
                            Map<String, Object> requestResponse = exportService.getReports(sushiQueryMap)
                            if(requestResponse.containsKey("error") && requestResponse.error instanceof Map && requestResponse.error.code == 202) {
                                result.status202 = true
                            }
                            else if(requestResponse.containsKey("error")) {
                                userCache.put('progress', 100)
                                return [result: [error: messageSource.getMessage("default.stats.error.${requestResponse.error}", null, locale)], status: SubscriptionControllerService.STATUS_ERROR]
                            }
                            else {
                                userCache.put('progress', 40)
                                Map<String, Object> identifierInverseMap = issueEntitlementService.buildIdentifierInverseMap(tippIDs)
                                Map<Long, Map> allReports = [:]
                                Long titleMatch
                                int matched = 0
                                if(params.reportType in Counter5Report.COUNTER_5_REPORTS) {
                                    double pointsPerMatch = 20/requestResponse.items.size()
                                    for(def reportItem: requestResponse.items) {
                                        titleMatch = issueEntitlementService.matchReport(identifierInverseMap, exportService.buildIdentifierMap(reportItem, AbstractReport.COUNTER_5))
                                        if(titleMatch) {
                                            Map<String, Integer> reports = allReports.containsKey(titleMatch) ? allReports.get(titleMatch) : [:]
                                            //counter 5.0
                                            if(reportItem.containsKey('Performance')) {
                                                for(Map performance: reportItem.Performance) {
                                                    Date reportFrom = DateUtils.parseDateGeneric(performance.Period.Begin_Date)
                                                    //for(Map instance: performance.Instance) {
                                                    Map instance = performance.Instance[0]
                                                    reports.put(DateUtils.getSDF_yyyyMM().format(reportFrom), instance.Count)
                                                    //}
                                                }
                                            }
                                            //counter 5.1
                                            else if(reportItem.containsKey('Attribute_Performance')) {
                                                for (Map struct : reportItem.Attribute_Performance) {
                                                    for (Map.Entry performance : struct.Performance) {
                                                        for (Map.Entry instance : performance) {
                                                            //for (Map.Entry reportRow : instance.getValue()) {
                                                            Map.Entry reportRow = instance.getValue()[0]
                                                            reports.put(reportRow.getKey(), reportRow.getValue())
                                                            //}
                                                        }
                                                    }
                                                }
                                            }
                                            allReports.put(titleMatch, reports)
                                        }
                                        matched++
                                        double increment = matched*pointsPerMatch
                                        userCache.put('progress', 40+increment)
                                    }
                                }
                                else if(params.reportType in Counter4Report.COUNTER_4_REPORTS) {
                                    double pointsPerMatch = 20/requestResponse.reports.size()
                                    for (GPathResult reportItem: requestResponse.reports) {
                                        titleMatch = issueEntitlementService.matchReport(identifierInverseMap, exportService.buildIdentifierMap(reportItem, AbstractReport.COUNTER_4))
                                        if(titleMatch) {
                                            Map<String, Integer> reports = allReports.containsKey(titleMatch) ? allReports.get(titleMatch) : [:]
                                            for(GPathResult performance: reportItem.'ns2:ItemPerformance') {
                                                Date reportFrom = DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text())
                                                for(GPathResult instance: performance.'ns2:Instance'.findAll { instCand -> instCand.'ns2:MetricType'.text() == configMap.metricTypes }) {
                                                    reports.put(DateUtils.getSDF_yyyyMM().format(reportFrom), Integer.parseInt(instance.'ns2:Count'.text()))
                                                }
                                            }
                                            allReports.put(titleMatch, reports)
                                        }
                                        matched++
                                        double increment = matched*pointsPerMatch
                                        userCache.put('progress', 40+increment)
                                    }
                                }
                                userCache.put('progress', 60)
                                exportData = exportService.generateTitleExport([format: params.exportConfig, perpetuallyPurchasedTitleURLs: perpetuallyPurchasedTitleURLs, tippIDs: tippIDs, monthHeaders: monthsInRing, usageData: allReports, withPick: true])
                            }
                            break
                    }
                    if(!result.containsKey('status202')) {
                        userCache.put('progress', 80)
                        FileOutputStream out = new FileOutputStream (f)
                        switch(params.exportConfig) {
                            case ExportService.KBART:
                                out.withWriter { Writer writer ->
                                    writer.write ( exportService.generateSeparatorTableString ( exportData.titleRow, exportData.columnData, '\t'))
                                }
                                out.flush()
                                out.close()
                                break
                            case ExportService.EXCEL:
                                Map sheetData = [:]
                                sheetData[messageSource.getMessage('renewEntitlementsWithSurvey.selectableTitles', null, locale)] = exportData
                                SXSSFWorkbook wb = exportService.generateXLSXWorkbook(sheetData)
                                FileOutputStream fos = new FileOutputStream(f)
                                //--> to document
                                wb.write(fos)
                                fos.flush()
                                fos.close()
                                wb.dispose()
                                break
                        }
                    }
                }
            }
            userCache.put('progress', 100)
            [result: result, status: SubscriptionControllerService.STATUS_OK]
        }
    }

    /**
     * Remaps the issue entitlements of the given title to the given substitute
     * @param tipp the title whose data should be remapped
     * @param replacement the title which serves as substitute
     * @return true if the rebase was successful, false otherwise
     */
    boolean rebaseSubscriptions(TitleInstancePackagePlatform tipp, TitleInstancePackagePlatform replacement) {
        try {
            Map<String,TitleInstancePackagePlatform> rebasingParams = [old:tipp,replacement:replacement]
            IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp = :replacement where ie.tipp = :old',rebasingParams)
            Identifier.executeUpdate('update Identifier i set i.tipp = :replacement where i.tipp = :old',rebasingParams)
            TIPPCoverage.executeUpdate('update TIPPCoverage tc set tc.tipp = :replacement where tc.tipp = :old',rebasingParams)
            return true
        }
        catch (Exception e) {
            log.error 'error while rebasing TIPP ... rollback!'
            log.error e.getMessage()
            return false
        }
    }

    /**
     * Checks whether consortial functions should be displayed
     * @param contextOrg the institution whose permissions should be checked
     * @param subscription the subscription which should be accessed
     * @return true if the given institution is a consortium and if the subscription is a consortial parent subscription of the given institution,
     * false otherwise
     */
    boolean showConsortiaFunctions(Org contextOrg, Subscription subscription) {
        return ((subscription.getConsortium()?.id == contextOrg.id) && subscription._getCalculatedType() in
                [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE])
    }

    boolean areStatsAvailable(Subscription subscription) {
        Set<Platform> subscribedPlatforms = Platform.executeQuery(
                "select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription",
                [subscription: subscription]
        )
        if (!subscribedPlatforms) {
            subscribedPlatforms = Platform.executeQuery(
                    "select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription or ie.subscription = (select s.instanceOf from Subscription s where s = :subscription)",
                    [subscription: subscription]
            )
        }
        areStatsAvailable(subscribedPlatforms)
    }

    /**
     * Called from views
     * Checks if statistics are provided by at least one of the given platforms
     * @param subscribedPlatforms the {@link Platform}s to verify
     * @return true if at least one of the platforms provides usage statistics data, false if none
     */
    boolean areStatsAvailable(Collection<Platform> subscribedPlatforms) {
        boolean result = false
        subscribedPlatforms.each { Platform platformInstance ->
            if(!result) {
                Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: platformInstance.gokbId])
                if (queryResult) {
                    List records = queryResult.result
                    if(records) {
                        if(records[0].statisticsFormat != null)
                            result = true
                    }
                }
            }
        }
        result
    }

    /**
     * Unsets the given customer number
     * @param id the customer number ID to unser
     * @return true if the unsetting was successful, false otherwise
     */
    boolean unsetCustomerIdentifier(Long id) {
        CustomerIdentifier ci = CustomerIdentifier.get(id)
        ci.value = null
        ci.requestorKey = null
        // ci.note = null
        ci.save()
    }

    /**
     * (Un-)links the given subscription to/from the given license
     * @param sub the subscription to be linked
     * @param newLicense the license to link
     * @param unlink should the link being created or dissolved?
     * @return true if the (un-)linking was successful, false otherwise
     */
    boolean setOrgLicRole(Subscription sub, License newLicense, boolean unlink) {
        boolean success = false
        Links curLink = Links.findBySourceLicenseAndDestinationSubscriptionAndLinkType(newLicense,sub,RDStore.LINKTYPE_LICENSE)
        Org subscr = sub.getSubscriberRespConsortia()
        if(!unlink && !curLink) {
            try {
                if(Links.construct([source: newLicense, destination: sub, linkType: RDStore.LINKTYPE_LICENSE, owner: contextService.getOrg()])) {
                    RefdataValue licRole
                    switch(sub._getCalculatedType()) {
                        case CalculatedType.TYPE_PARTICIPATION:
                            licRole = RDStore.OR_LICENSEE_CONS
                            break
                        case CalculatedType.TYPE_LOCAL:
                            licRole = RDStore.OR_LICENSEE
                            break
                        case [ CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE ]:
                            licRole = RDStore.OR_LICENSING_CONSORTIUM
                            break
                        default: log.error("no role type determinable!")
                            break
                    }

                    if(licRole) {
                        OrgRole orgLicRole = OrgRole.findByLicAndOrgAndRoleType(newLicense,subscr,licRole)
                        if(!orgLicRole){
                            orgLicRole = new OrgRole(lic: newLicense,org: subscr, roleType: licRole)
                            if(!orgLicRole.save())
                                log.error(orgLicRole.errors.toString())
                        }
                        success = true
                    }
                    else log.error("role type missing for org-license linking!")
                }
            }
            catch (CreationException e) {
                log.error( e.toString() )
            }
        }
        else if(unlink && curLink) {
            License lic = curLink.sourceLicense
            curLink.delete() //delete() is void, no way to check whether errors occurred or not
            if(sub._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) {
                String linkedSubsQuery = "select li from Links li, OrgRole oo where li.sourceLicense = :lic and li.linkType = :linkType and li.destinationSubscription = oo.sub and oo.roleType in (:subscrTypes) and oo.org = :subscr"
                Set<Links> linkedSubs = Links.executeQuery(linkedSubsQuery, [lic: lic, linkType: RDStore.LINKTYPE_LICENSE, subscrTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], subscr: subscr])
                if (linkedSubs.size() < 1) {
                    log.info("no more license <-> subscription links between org -> removing licensee role")
                    OrgRole.executeUpdate("delete from OrgRole oo where oo.lic = :lic and oo.org = :subscriber", [lic: lic, subscriber: subscr])
                }
            }
            success = true
        }
        success
    }

    /**
     * Processes the given TSV file and generates subscription candidates based on the data read off
     * @param tsvFile the import file containing the subscription data
     * @return a map containing the candidates, the parent subscription type and the errors
     */
    Map subscriptionImport(List<String> headerRow, List<List<String>> rows) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Org contextOrg = contextService.getOrg()
        RefdataValue comboType
        String[] parentSubType = []
        if (contextService.getOrg().isCustomerType_Consortium()) {
            comboType = RDStore.COMBO_TYPE_CONSORTIUM
            parentSubType = [RDStore.SUBSCRIPTION_KIND_CONSORTIAL.getI10n('value')]
        }
        Map colMap = [:]
        Map<String, Map> propMap = [:], idMap = [:]
        Map candidates = [:]
        List<String> ignoredColHeads = [], multiplePropDefs = [], multipleIdentifierNamespaces = []
        int colCount = headerRow.size()
        headerRow.eachWithIndex { String headerCol, int c ->
            //important: when processing column headers, grab those which are reserved; default case: check if it is a name of a property definition; if there is no result as well, reject.
            switch(headerCol.toLowerCase()) {
                case "name": colMap.name = c
                    break
                case ["member", "einrichtung"]: colMap.member = c
                    break
                case ["vertrag", "license"]: colMap.licenses = c
                    break
                case ["elternlizenz / konsortiallizenz", "parent subscription / consortial subscription",
                      "elternlizenz", "konsortiallizenz", "parent subscription", "consortial subscription"]:
                    if(contextService.getOrg().isCustomerType_Consortium())
                        colMap.instanceOf = c
                    break
                case "status": colMap.status = c
                    break
                case ["startdatum", "laufzeit-beginn", "start date"]: colMap.startDate = c
                    break
                case ["enddatum", "laufzeit-ende", "end date"]: colMap.endDate = c
                    break
                case ["kündigungsdatum", "cancellation date", "manual cancellation date"]: colMap.manualCancellationDate = c
                    break
                case ["zuordnungsjahr", "reference year"]: colMap.referenceYear = c
                    break
                case ["automatische verlängerung", "automatic renewal"]: colMap.isAutomaticRenewAnnually = c
                    break
                case ["lizenztyp", "subscription type", "type"]: colMap.kind = c
                    break
                case ["lizenzform", "subscription form", "form"]: colMap.form = c
                    break
                case ["ressourcentyp", "subscription resource", "resource"]: colMap.resource = c
                    break
                case ["anbieter", "provider"]: colMap.provider = c
                    break
                case ["library supplier", "lieferant", "agency", "vendor"]: colMap.vendor = c
                    break
                case ["anmerkungen", "notes"]: colMap.notes = c
                    break
                case ["perpetual access", "dauerhafter zugriff"]: colMap.hasPerpetualAccess = c
                    break
                case ["publish component", "publish-komponente"]: colMap.hasPublishComponent = c
                    break
                case ["data exchange release", "freigabe daten"]: colMap.isPublicForApi = c
                    break
                case ["holding selection", "paketzuschnitt"]: colMap.holdingSelection = c
                    break
                default:
                    //check if property definition or identifier
                    boolean isNotesCol = false
                    String nameKeyString = headerCol.toLowerCase()
                    if(headerCol.contains('$$notes')) {
                        isNotesCol = true
                        nameKeyString = headerCol.split('\\$\\$')[0].toLowerCase()
                    }
                    Map queryParams = [propDef:nameKeyString,contextOrg:contextOrg,subProp:PropertyDefinition.SUB_PROP]
                    List<PropertyDefinition> possiblePropDefs = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr = :subProp and (lower(pd.name_de) = :propDef or lower(pd.name_en) = :propDef) and (pd.tenant = :contextOrg or pd.tenant = null)",queryParams)
                    if(possiblePropDefs.size() == 1) {
                        PropertyDefinition propDef = possiblePropDefs[0]
                        if(isNotesCol) {
                            propMap[propDef.class.name+':'+propDef.id].notesColno = c
                        }
                        else {
                            Map<String,Integer> defPair = [colno:c]
                            if(propDef.isRefdataValueType()) {
                                defPair.refCategory = propDef.refdataCategory
                            }
                            else if(propDef.isDateType()) {
                                defPair.isDate = true
                            }
                            propMap[propDef.class.name+':'+propDef.id] = [definition:defPair]
                        }
                    }
                    else if(possiblePropDefs.size() > 1)
                        multiplePropDefs << headerCol
                    else {
                        //next attempt: identifier
                        List<IdentifierNamespace> possibleIdentifierNamespaces = IdentifierNamespace.executeQuery("select ns from IdentifierNamespace ns where ns.nsType = :subType and (lower(ns.ns) = :idns or lower(ns.name_de) = :idns or lower(ns.name_en) = :idns)", [subType: IdentifierNamespace.NS_SUBSCRIPTION, idns: nameKeyString])
                        if(possibleIdentifierNamespaces.size() == 1) {
                            IdentifierNamespace idns = possibleIdentifierNamespaces[0]
                            Map<String,Integer> defPair = [colno:c]
                            idMap.put(genericOIDService.getOID(idns), [definition:defPair, namespace: idns])
                        }
                        else if(possibleIdentifierNamespaces.size() > 1)
                            multipleIdentifierNamespaces << headerCol
                        else
                            ignoredColHeads << headerCol
                    }
                    break
            }
        }
        Set<String> globalErrors = []
        if(ignoredColHeads)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.colHeaderIgnored',[ignoredColHeads.join('</li><li>')].toArray(),locale)
        if(multiplePropDefs)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.multiplePropDefs',[multiplePropDefs.join('</li><li>')].toArray(),locale)
        rows.eachWithIndex { List<String> cols, int rowno ->
            Map mappingErrorBag = [:], candidate = [properties: [:], ids: [:]]
            if(cols.size() == colCount) {
                //check if we have some mandatory properties ...
                //status(nullable:false, blank:false) -> to status, defaults to status not set
                if(colMap.status != null) {
                    String statusKey = cols[colMap.status].trim()
                    if(statusKey) {
                        String status = refdataService.retrieveRefdataValueOID(statusKey, RDConstants.SUBSCRIPTION_STATUS)
                        if(status) {
                            candidate.status = status
                        }
                        else {
                            //missing case one: invalid status key
                            //default to subscription not set
                            candidate.status = "${RDStore.SUBSCRIPTION_NO_STATUS.class.name}:${RDStore.SUBSCRIPTION_NO_STATUS.id}"
                            mappingErrorBag.noValidStatus = statusKey
                        }
                    }
                    else {
                        //missing case two: no status key set
                        //default to subscription not set
                        candidate.status = "${RDStore.SUBSCRIPTION_NO_STATUS.class.name}:${RDStore.SUBSCRIPTION_NO_STATUS.id}"
                        mappingErrorBag.statusNotSet = true
                    }
                }
                else {
                    //missing case three: the entire column is missing
                    //default to subscription not set
                    candidate.status = "${RDStore.SUBSCRIPTION_NO_STATUS.class.name}:${RDStore.SUBSCRIPTION_NO_STATUS.id}"
                    mappingErrorBag.statusNotSet = true
                }
                //moving on to optional attributes
                //name(nullable:true, blank:false) -> to name
                if(colMap.name != null) {
                    String name = cols[colMap.name].trim()
                    if(name)
                        candidate.name = name
                }
                //licenses
                if(colMap.licenses != null && cols[colMap.licenses]?.trim()) {
                    List<String> licenseKeys = cols[colMap.licenses].split(',')
                    candidate.licenses = []
                    licenseKeys.each { String licenseKey ->
                        List<License> licCandidates = License.executeQuery("select oo.lic from OrgRole oo join oo.lic l where :idCandidate in (cast(l.id as string),l.laserID) and oo.roleType in :roleTypes and oo.org = :contextOrg", [idCandidate: licenseKey.trim(), roleTypes: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE], contextOrg: contextOrg])
                        if (licCandidates.size() == 1) {
                            License license = licCandidates[0]
                            candidate.licenses << genericOIDService.getOID(license)
                        } else if (licCandidates.size() > 1) {
                            if(!mappingErrorBag.containsKey('multipleLicError'))
                                mappingErrorBag.multipleLicError = []
                            mappingErrorBag.multipleLicError << licenseKey
                        }
                        else {
                            if(!mappingErrorBag.containsKey('noValidLicense'))
                                mappingErrorBag.noValidLicense = []
                            mappingErrorBag.noValidLicense << licenseKey
                        }
                    }
                }
                //type(nullable:true, blank:false) -> to type
                if(colMap.kind != null) {
                    String typeKey = cols[colMap.kind].trim()
                    if(typeKey) {
                        String type = refdataService.retrieveRefdataValueOID(typeKey, RDConstants.SUBSCRIPTION_KIND)
                        if(type) {
                            candidate.kind = type
                        }
                        else {
                            mappingErrorBag.noValidType = typeKey
                        }
                    }
                }
                //form(nullable:true, blank:false) -> to form
                if(colMap.form != null) {
                    String formKey = cols[colMap.form].trim()
                    if(formKey) {
                        String form = refdataService.retrieveRefdataValueOID(formKey, RDConstants.SUBSCRIPTION_FORM)
                        if(form) {
                            candidate.form = form
                        }
                        else {
                            mappingErrorBag.noValidForm = formKey
                        }
                    }
                }
                //resource(nullable:true, blank:false) -> to resource
                if(colMap.resource != null) {
                    String resourceKey = cols[colMap.resource].trim()
                    if(resourceKey) {
                        String resource = refdataService.retrieveRefdataValueOID(resourceKey,RDConstants.SUBSCRIPTION_RESOURCE)
                        if(resource) {
                            candidate.resource = resource
                        }
                        else {
                            mappingErrorBag.noValidResource = resourceKey
                        }
                    }
                }
                //holdingSelection(nullable:true, blank:false) -> to holdingSelection
                if(colMap.holdingSelection != null) {
                    String holdingSelectionKey = cols[colMap.holdingSelection].trim()
                    if(holdingSelectionKey) {
                        String holdingSelection = refdataService.retrieveRefdataValueOID(holdingSelectionKey,RDConstants.SUBSCRIPTION_HOLDING)
                        if(holdingSelection) {
                            candidate.holdingSelection = holdingSelection
                        }
                        else {
                            mappingErrorBag.noValidHoldingSelection = holdingSelectionKey
                        }
                    }
                }
                //provider
                if(colMap.provider != null) {
                    String providerIdCandidate = cols[colMap.provider]?.trim()
                    if(providerIdCandidate) {
                        Long idCandidate = providerIdCandidate.isLong() ? Long.parseLong(providerIdCandidate) : null
                        Provider provider = Provider.findByIdOrLaserID(idCandidate,providerIdCandidate)
                        if(provider)
                            candidate.provider = "${provider.class.name}:${provider.id}"
                        else {
                            mappingErrorBag.noValidOrg = providerIdCandidate
                        }
                    }
                }
                //library supplier
                if(colMap.vendor != null) {
                    String librarySupplierIdCandidate = cols[colMap.vendor]?.trim()
                    if(librarySupplierIdCandidate) {
                        Long idCandidate = librarySupplierIdCandidate.isLong() ? Long.parseLong(librarySupplierIdCandidate) : null
                        Vendor librarySupplier = Vendor.findByIdOrLaserID(idCandidate,librarySupplierIdCandidate)
                        if(librarySupplier)
                            candidate.vendor = "${librarySupplier.class.name}:${librarySupplier.id}"
                        else {
                            mappingErrorBag.noValidOrg = librarySupplierIdCandidate
                        }
                    }
                }
                /*
                startDate(nullable:true, blank:false, validator: { val, obj ->
                    if(obj.startDate != null && obj.endDate != null) {
                        if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
                    }
                }) -> to startDate
                */
                Date startDate
                if(colMap.startDate != null) {
                    if(cols[colMap.startDate] instanceof Double)
                        startDate = DateUtil.getJavaDate(cols[colMap.startDate])
                    else
                        startDate = DateUtils.parseDateGeneric(cols[colMap.startDate].trim())
                }
                /*
                endDate(nullable:true, blank:false, validator: { val, obj ->
                    if(obj.startDate != null && obj.endDate != null) {
                        if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
                    }
                }) -> to endDate
                */
                Date endDate
                if(colMap.endDate != null) {
                    if(cols[colMap.endDate] instanceof Double)
                        endDate = DateUtil.getJavaDate(cols[colMap.endDate])
                    else
                        endDate = DateUtils.parseDateGeneric(cols[colMap.endDate].trim())
                }
                if(startDate && endDate) {
                    if(startDate <= endDate) {
                        candidate.startDate = startDate
                        candidate.endDate = endDate
                    }
                    else {
                        mappingErrorBag.startDateBeforeEndDate = true
                    }
                }
                else if(startDate && !endDate)
                    candidate.startDate = startDate
                else if(!startDate && endDate)
                    candidate.endDate = endDate
                //manualCancellationDate(nullable:true, blank:false)
                if(colMap.manualCancellationDate != null) {
                    Date manualCancellationDate
                    if(cols[colMap.manualCancellationDate] instanceof Double)
                        manualCancellationDate = DateUtil.getJavaDate(cols[colMap.manualCancellationDate])
                    else manualCancellationDate = DateUtils.parseDateGeneric(cols[colMap.manualCancellationDate])
                    if(manualCancellationDate)
                        candidate.manualCancellationDate = manualCancellationDate
                }
                //referenceYear(nullable:true, blank:false)
                if(colMap.referenceYear != null) {
                    Year referenceYear
                    if(cols[colMap.referenceYear] instanceof Double) {
                        referenceYear = Year.of(new BigDecimal(cols[colMap.referenceYear]).setScale(0, RoundingMode.HALF_UP).intValue())
                    }
                    else {
                        String yearKey = cols[colMap.referenceYear].trim()
                        if(yearKey) {
                            referenceYear = Year.parse(cols[colMap.referenceYear])
                        }
                    }
                    if(referenceYear)
                        candidate.referenceYear = referenceYear
                }
                //isAutomaticRenewAnnually
                if(colMap.isAutomaticRenewAnnually != null) {
                    if(startDate && endDate) {
                        String autoRenewKey = cols[colMap.isAutomaticRenewAnnually].trim()
                        if(autoRenewKey) {
                            String yesNo = refdataService.retrieveRefdataValueOID(autoRenewKey,RDConstants.Y_N)
                            if(yesNo) {
                                candidate.isAutomaticRenewAnnually = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}")
                            }
                            else {
                                mappingErrorBag.noValidAutoRenew = autoRenewKey
                            }
                        }
                    }
                    else {
                        mappingErrorBag.invalidDateRangeForRenew = true
                    }
                }
                //instanceOf(nullable:true, blank:false)
                if(colMap.instanceOf != null && colMap.member != null) {
                    String idCandidate = cols[colMap.instanceOf].trim()
                    String memberIdCandidate = cols[colMap.member].trim()
                    if(idCandidate && memberIdCandidate) {
                        List<Subscription> parentSubs = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :contextOrg and oo.roleType in :roleTypes and :idCandidate in (cast(oo.sub.id as string),oo.sub.laserID)",[contextOrg: contextOrg, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIUM], idCandidate: idCandidate])
                        List<Org> possibleOrgs = Org.executeQuery("select distinct ident.org from Identifier ident, Combo c where c.fromOrg = ident.org and :idCandidate in (cast(ident.org.id as string), ident.org.laserID) or (ident.value = :idCandidate and ident.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type", [idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
                        if(parentSubs.size() == 1) {
                            Subscription instanceOf = parentSubs[0]
                            candidate.instanceOf = "${instanceOf.class.name}:${instanceOf.id}"
                            if(!candidate.name)
                                candidate.name = instanceOf.name
                        }
                        else {
                            mappingErrorBag.noValidSubscription = idCandidate
                        }
                        if(possibleOrgs.size() == 1) {
                            //further check needed: is the subscriber linked per combo to the organisation?
                            Org member = possibleOrgs[0]
                            candidate.member = "${member.class.name}:${member.id}"
                        }
                        else if(possibleOrgs.size() > 1) {
                            mappingErrorBag.multipleOrgsError = possibleOrgs.collect { org -> org.sortname ?: org.name }
                        }
                        else {
                            mappingErrorBag.noValidOrg = memberIdCandidate
                        }
                    }
                    else {
                        if(!idCandidate && memberIdCandidate)
                            mappingErrorBag.instanceOfWithoutMember = true
                        if(idCandidate && !memberIdCandidate)
                            mappingErrorBag.memberWithoutInstanceOf = true
                    }
                }
                else {
                    if(colMap.instanceOf == null && colMap.member != null)
                        globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.memberWithoutInstanceOf',null,locale)
                    if(colMap.instanceOf != null && colMap.member == null)
                        globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.instanceOfWithoutMember',null,locale)
                }
                if(colMap.hasPerpetualAccess != null) {
                    String hasPerpetualAccessKey = cols[colMap.hasPerpetualAccess].trim()
                    if(hasPerpetualAccessKey) {
                        String yesNo = refdataService.retrieveRefdataValueOID(hasPerpetualAccessKey, RDConstants.Y_N)
                        if(yesNo) {
                            candidate.hasPerpetualAccess = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}")
                        }
                        else {
                            mappingErrorBag.noPerpetualAccessType = hasPerpetualAccessKey
                        }
                    }
                }
                if(colMap.hasPublishComponent != null) {
                    String hasPublishComponentKey = cols[colMap.hasPublishComponent].trim()
                    if(hasPublishComponentKey) {
                        String yesNo = refdataService.retrieveRefdataValueOID(hasPublishComponentKey, RDConstants.Y_N)
                        if(yesNo) {
                            candidate.hasPublishComponent = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}")
                        }
                        else {
                            mappingErrorBag.noPublishComponent = hasPublishComponentKey
                        }
                    }
                }
                if(colMap.isPublicForApi != null) {
                    String isPublicForApiKey = cols[colMap.isPublicForApi].trim()
                    if(isPublicForApiKey) {
                        String yesNo = refdataService.retrieveRefdataValueOID(isPublicForApiKey, RDConstants.Y_N)
                        if(yesNo) {
                            candidate.isPublicForApi = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}")
                        }
                        else {
                            mappingErrorBag.noPublicForApi = isPublicForApiKey
                        }
                    }
                }
                //properties -> propMap
                propMap.each { String k, Map propInput ->
                    Map defPair = propInput.definition
                    Map propData = [:]
                    def v
                    if(cols[defPair.colno] != null && defPair.isDate) {
                        if(cols[defPair.colno] instanceof Double)
                            v = DateUtil.getJavaDate(cols[defPair.colno])
                        else if(cols[defPair.colno].trim())
                            v = cols[defPair.colno].trim()
                    }
                    else if(cols[defPair.colno].trim()) {
                        if(defPair.refCategory) {
                            v = refdataService.retrieveRefdataValueOID(cols[defPair.colno].trim(),defPair.refCategory)
                            if(!v) {
                                mappingErrorBag.propValNotInRefdataValueSet = [cols[defPair.colno].trim(),defPair.refCategory]
                            }
                        }
                        else v = cols[defPair.colno]
                    }
                    if(v)
                        propData.propValue = v
                    if(propInput.notesColno)
                        propData.propNote = cols[propInput.notesColno].trim()
                    candidate.properties[k] = propData
                }
                //ids -> idMap
                idMap.each { String k, Map idInput ->
                    Map defPair = idInput.definition
                    IdentifierNamespace idns = idInput.namespace
                    Map idData = [:]
                    if(cols[defPair.colno].trim()) {
                        String v = cols[defPair.colno]
                        if(idns.validationRegex) {
                            Pattern pattern = ~/${idns.validationRegex}/
                            if(pattern.matcher(v).matches())
                                idData.idValue = v
                            else mappingErrorBag.identifierNamespaceRegexMismatch = [cols[defPair.colno].trim(), messageSource.getMessage("validation.${idns.ns}Match", null, locale)]
                        }
                        else
                            idData.idValue = v
                    }
                    candidate.ids[k] = idData
                }
                //notes
                if(colMap.notes != null && cols[colMap.notes]?.trim()) {
                    candidate.notes = cols[colMap.notes].trim()
                }
            }
            else {
                mappingErrorBag.rowMismatch = [rowno+1, cols.size(), colCount]
            }
            candidates.put(candidate,mappingErrorBag)
        }
        [candidates: candidates, globalErrors: globalErrors, parentSubType: parentSubType]
    }

    Map subscriptionImportExcel() {
        Map<String, Object> result = [:]

        result
    }

    /**
     * Processes after having checked the given import and creates the subscription instances with their depending objects
     * @param candidates the candidates to process
     * @param params the import parameter map, containing the flags which subscriptions should be taken and which not
     * @return a list of errors which have occurred, an empty list in case of success
     */
    List addSubscriptions(candidates,GrailsParameterMap params) {
        List errors = []
        Locale locale = LocaleUtils.getCurrentLocale()
        Org contextOrg = contextService.getOrg()
        SimpleDateFormat databaseDateFormatParser = DateUtils.getSDF_yyyyMMddTHHmmssZ()
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        candidates.eachWithIndex{ entry, int s ->
            if(params["take${s}"]) {
                //create object itself
                Subscription sub = new Subscription(name: entry.name,
                        status: genericOIDService.resolveOID(entry.status),
                        kind: genericOIDService.resolveOID(entry.kind),
                        form: genericOIDService.resolveOID(entry.form),
                        resource: genericOIDService.resolveOID(entry.resource),
                        type: (contextOrg.isCustomerType_Consortium()) ? RDStore.SUBSCRIPTION_TYPE_CONSORTIAL : RDStore.SUBSCRIPTION_TYPE_LOCAL,
                        isPublicForApi: entry.isPublicForApi,
                        hasPerpetualAccess: entry.hasPerpetualAccess,
                        hasPublishComponent: entry.hasPublishComponent,
                        identifier: RandomUtils.getUUID())
                sub.startDate = entry.startDate ? databaseDateFormatParser.parse(entry.startDate) : null
                sub.endDate = entry.endDate ? databaseDateFormatParser.parse(entry.endDate) : null
                sub.referenceYear = entry.referenceYear ? Year.of(entry.referenceYear.value) : null
                sub.manualCancellationDate = entry.manualCancellationDate ? databaseDateFormatParser.parse(entry.manualCancellationDate) : null
                sub.isAutomaticRenewAnnually = entry.isAutomaticRenewAnnually ?: false
                /* TODO [ticket=2276]
                if(sub.type == SUBSCRIPTION_TYPE_ADMINISTRATIVE)
                    sub.administrative = true*/
                sub.instanceOf = entry.instanceOf ? genericOIDService.resolveOID(entry.instanceOf) : null
                Org member = entry.member ? genericOIDService.resolveOID(entry.member) : null
                Provider provider = entry.provider ? genericOIDService.resolveOID(entry.provider) : null
                Vendor vendor = entry.vendor ? genericOIDService.resolveOID(entry.vendor) : null

                if(sub.save()) {
                    sub.refresh() //needed for dependency processing
                    //create the org role associations
                    RefdataValue parentRoleType, memberRoleType
                    if (contextService.getOrg().isCustomerType_Consortium()) {
                        parentRoleType = RDStore.OR_SUBSCRIPTION_CONSORTIUM
                        memberRoleType = RDStore.OR_SUBSCRIBER_CONS
                    }
                    else
                        parentRoleType = RDStore.OR_SUBSCRIBER
                    OrgRole parentRole = new OrgRole(roleType: parentRoleType, sub: sub, org: contextOrg)
                    if(!parentRole.save()) {
                        errors << parentRole.errors
                    }
                    if(memberRoleType && member) {
                        OrgRole memberRole = new OrgRole(roleType: memberRoleType, sub: sub, org: member)
                        if(!memberRole.save()) {
                            errors << memberRole.errors
                        }
                    }
                    entry.licenses.each { String licenseOID ->
                        License license = (License) genericOIDService.resolveOID(licenseOID)
                        setOrgLicRole(sub,license,false)
                    }
                    if(provider) {
                        ProviderRole providerRole = new ProviderRole(subscription: sub, provider: provider)
                        if(!providerRole.save()) {
                            errors << providerRole.errors
                        }
                    }
                    if(vendor) {
                        VendorRole vendorRole = new VendorRole(subscription: sub, vendor: vendor)
                        if(!vendorRole.save()) {
                            errors << vendorRole.errors
                        }
                    }
                    //process subscription properties
                    entry.properties.each { k, v ->
                        if(v.propValue?.trim()) {
                            log.debug("${k}:${v.propValue}")
                            PropertyDefinition propDef = (PropertyDefinition) genericOIDService.resolveOID(k)
                            List<String> valueList
                            if(propDef.multipleOccurrence) {
                                valueList = v?.propValue?.split(',')
                            }
                            else valueList = [v.propValue]
                            //in most cases, valueList is a list with one entry
                            valueList.each { value ->
                                try {
                                    createProperty(propDef,sub,contextOrg,value.trim(),v.propNote)
                                }
                                catch (Exception e) {
                                    errors << e.getMessage()
                                }
                            }
                        }
                    }
                    //process subscription identifiers
                    entry.ids.each { k, v ->
                        if(v.idValue?.trim()) {
                            log.debug("${k}:${v.idValue}")
                            IdentifierNamespace idns = (IdentifierNamespace) genericOIDService.resolveOID(k)
                            List<String> valueList
                            if(!idns.isUnique) {
                                valueList = v?.idValue?.split(',')
                            }
                            else valueList = [v.idValue]
                            //in most cases, valueList is a list with one entry
                            valueList.each { value ->
                                try {
                                    FactoryResult result = Identifier.constructWithFactoryResult([namespace: idns, value: value, reference: sub])
                                    result.status.each { String status ->
                                        if(status != FactoryResult.STATUS_OK)
                                            errors.add << status
                                    }
                                }
                                catch (Exception e) {
                                    errors << e.getMessage()
                                }
                            }
                        }
                    }
                    if(entry.notes) {
                        Object[] args = [sdf.format(new Date())]
                        Doc docContent = new Doc(contentType: Doc.CONTENT_TYPE_STRING, content: entry.notes, title: messageSource.getMessage('myinst.subscriptionImport.notes.title',args,locale), type: RDStore.DOC_TYPE_NOTE, owner: contextOrg, user: contextService.getUser())
                        if(docContent.save()) {
                            DocContext dc = new DocContext(subscription: sub, owner: docContent)
                            dc.save()
                        }
                    }
                }
                else {
                    errors << sub.errors
                }
            }
        }
        errors
    }

    /**
     * Updates the subscription holding with the given import data
     * @param stream the input stream containing the data to import
     * @param entIds the IDs of the issue entitlements to update
     * @param subscription the subscription whose holding should be updated
     * @param uploadCoverageDates should coverage dates be updated as well?
     * @param uploadPriceInfo should price dates be updated as well?
     * @return a map containing the processing results
     */
    Map issueEntitlementEnrichment(MultipartFile inputFile, Map<String, Object> configMap) {
        Map<String, Object> result = issueEntitlementService.matchTippsFromFile(inputFile, configMap)
        int count = 0
        int countChangesPrice = 0
        int countChangesCoverageDates = 0
        result.matchedTitles.each { TitleInstancePackagePlatform match, Map<String, Object> externalTitleData ->
            IssueEntitlement ieMatch = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(configMap.subscription, match, RDStore.TIPP_STATUS_REMOVED)
            if (ieMatch) {
                count++
                if (configMap.uploadCoverageDates) {
                    IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage()
                    result.titleRow.each { String colName ->
                        String cellEntry = externalTitleData.get(colName)?.trim()
                        if (cellEntry) {
                            switch (colName) {
                                case "date_first_issue_online": ieCoverage.startDate = DateUtils.parseDateGeneric(cellEntry)
                                    break
                                case "num_first_vol_online": ieCoverage.startVolume = cellEntry
                                    break
                                case "num_first_issue_online": ieCoverage.startIssue = cellEntry
                                    break
                                case "date_last_issue_online": ieCoverage.endDate = DateUtils.parseDateGeneric(cellEntry)
                                    break
                                case "num_last_vol_online": ieCoverage.endVolume = cellEntry
                                    break
                                case "num_last_issue_online": ieCoverage.endIssue = cellEntry
                                    break
                                case "embargo_info": ieCoverage.embargo = cellEntry
                                    break
                                case "coverage_depth": ieCoverage.coverageDepth = cellEntry
                                    break
                                case "notes": ieCoverage.coverageNote = cellEntry
                                    break
                            }
                        }
                    }
                    if (ieCoverage && !ieCoverage.findEquivalent(ieMatch.coverages)) {
                        ieCoverage.issueEntitlement = ieMatch
                        if (!ieCoverage.save()) {
                            throw new EntitlementCreationException(ieCoverage.errors)
                        } else {
                            countChangesCoverageDates++
                        }
                    }
                }
                if (configMap.uploadPriceInfo) {
                    result.titleRow.each { String colName ->
                        String cellEntry = externalTitleData.get(colName)?.trim()
                        if(cellEntry) {
                            PriceItem priceItem = null
                            try {
                                switch (colName) {
                                    case ["listprice_eur", "localprice_eur"]: priceItem = ieMatch.priceItems?.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_EUR } ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_EUR } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_EUR)
                                        priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                        break
                                    case ["listprice_usd", "localprice_usd"]: priceItem = ieMatch.priceItems?.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_USD } ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_USD } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_USD)
                                        priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                        break
                                    case ["listprice_gbp", "localprice_gbp"]: priceItem = ieMatch.priceItems?.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_GBP } ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_GBP } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_GBP)
                                        priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                        break
                                }
                            }
                            catch (NumberFormatException e) {
                                log.error("Unparseable number ${cellEntry}")
                            }
                            if (priceItem?.localPrice && priceItem?.localCurrency) {
                                priceItem.setLaserID()
                                if (!priceItem.save()) {
                                    throw new Exception(priceItem.errors.toString())
                                } else {
                                    countChangesPrice++
                                }
                            }
                        }
                    }
                }
            }
        }
        result.success = count > 0
        result.notAddedCount = result.toAddCount-count
        result.notSubscribedCount = result.matchedTitles.size()-count
        result.processCount = count
        result.processCountChangesCoverageDates = countChangesCoverageDates
        result.processCountChangesPrice = countChangesPrice
        result
        /*

        Set<Package> subPkgs = SubscriptionPackage.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: subscription])

        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                       eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name),
                                                       isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                       issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name),
                                                       eisbn: IdentifierNamespace.findByNsAndNsType('eisbn', TitleInstancePackagePlatform.class.name),
                                                       doi: IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name),
                                                       title_id: IdentifierNamespace.findByNsAndNsType('title_id', TitleInstancePackagePlatform.class.name)]

        ArrayList<String> rows = stream.text.split('\n')
        Map<String, Integer> colMap = [publicationTitleCol: -1, zdbCol: -1, onlineIdentifierCol: -1, printIdentifierCol: -1, dateFirstInPrintCol: -1, dateFirstOnlineCol: -1,
                                       startDateCol       : -1, startVolumeCol: -1, startIssueCol: -1,
                                       endDateCol         : -1, endVolumeCol: -1, endIssueCol: -1,
                                       accessStartDateCol : -1, accessEndDateCol: -1, coverageDepthCol: -1, coverageNotesCol: -1, embargoCol: -1,
                                       listPriceCol       : -1, listCurrencyCol: -1, listPriceEurCol: -1, listPriceUsdCol: -1, listPriceGbpCol: -1, localPriceCol: -1, localCurrencyCol: -1, priceDateCol: -1,
                                       titleUrlCol: -1, titleIdCol: -1, doiCol: -1, titleUrlCol: -1, ]
        //read off first line of KBART file
        List titleRow = rows.remove(0).split('\t'), wrongTitles = [], truncatedRows = []
        titleRow.eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case "title_url": colMap.titleUrlCol = c
                    break
                case "title_id": colMap.titleIdCol = c
                    break
                case "doi_identifier": colMap.doiCol = c
                    break
                case "zugriffs-url": colMap.titleUrlCol = c
                    break
                case "access url": colMap.titleUrlCol = c
                    break
                case "doi": colMap.doiCol= c
                    break
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
                case "listprice_eur": colMap.listPriceEurCol = c
                    break
                case "listprice_usd": colMap.listPriceUsdCol = c
                    break
                case "listprice_gbp": colMap.listPriceGbpCol = c
                    break
                case "localprice_eur": colMap.localPriceEurCol = c
                    break
                case "localprice_usd": colMap.localPriceUsdCol = c
                    break
                case "localprice_gbp": colMap.localPriceGbpCol = c
                    break
            }
        }
        rows.eachWithIndex { row, int i ->
            log.debug("now processing entitlement ${i}")
            ArrayList<String> cols = row.split('\t', -1)
            if(cols.size() == titleRow.size()) {
                TitleInstancePackagePlatform match = null
                //cascade: 1. title_id, 2. title_url, 3. identifier map
                if (colMap.titleIdCol >= 0 && cols[colMap.titleIdCol] != null && !cols[colMap.titleIdCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[colMap.titleIdCol].trim(), ns: namespaces.title_id, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if (!match && colMap.titleUrlCol >= 0 && cols[colMap.titleUrlCol] != null && !cols[colMap.titleUrlCol].trim().isEmpty()) {
                    match = TitleInstancePackagePlatform.findByHostPlatformURLAndPkgInListAndStatusNotEqual(cols[colMap.titleUrlCol].trim(), subPkgs, RDStore.TIPP_STATUS_REMOVED)
                }
                if (!match && colMap.doiCol >= 0 && cols[colMap.doiCol] != null && !cols[colMap.doiCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[colMap.doiCol].trim(), ns: namespaces.doi, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if (!match && colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol] != null && !cols[colMap.onlineIdentifierCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: subPkgs, value: cols[colMap.onlineIdentifierCol].trim(), ns: [namespaces.eisbn, namespaces.eissn], removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if (!match && colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol] != null && !cols[colMap.printIdentifierCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status != :removed', [subPkgs: subPkgs, value: cols[colMap.printIdentifierCol].trim(), ns: [namespaces.isbn, namespaces.issn], removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }
                if (!match && colMap.zdbCol >= 0 && cols[colMap.zdbCol] != null && !cols[colMap.zdbCol].trim().isEmpty()) {
                    List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status != :removed', [subPkgs: subPkgs, value: cols[colMap.zdbCol].trim(), ns: namespaces.zdb, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as TitleInstancePackagePlatform
                }

                if (match) {
                    //TODO use from here
                    IssueEntitlement ieMatch = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(subscription, match, RDStore.TIPP_STATUS_REMOVED)
                    if (ieMatch) {
                        count++
                        if (uploadCoverageDates) {
                            IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage()
                            colMap.each { String colName, int colNo ->
                                if (colNo > -1 && cols[colNo]) {
                                    String cellEntry = cols[colNo].trim()
                                    switch (colName) {
                                        case "startDateCol": ieCoverage.startDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : null
                                            break
                                        case "startVolumeCol": ieCoverage.startVolume = cellEntry ?: null
                                            break
                                        case "startIssueCol": ieCoverage.startIssue = cellEntry ?: null
                                            break
                                        case "endDateCol": ieCoverage.endDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : null
                                            break
                                        case "endVolumeCol": ieCoverage.endVolume = cellEntry ?: null
                                            break
                                        case "endIssueCol": ieCoverage.endIssue = cellEntry ?: null
                                            break
                                        case "accessStartDateCol": ieMatch.accessStartDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : ieMatch.accessStartDate
                                            break
                                        case "accessEndDateCol": ieMatch.accessEndDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : ieMatch.accessEndDate
                                            break
                                        case "embargoCol": ieCoverage.embargo = cellEntry ?: null
                                            break
                                        case "coverageDepthCol": ieCoverage.coverageDepth = cellEntry ?: null
                                            break
                                        case "coverageNotesCol": ieCoverage.coverageNote = cellEntry ?: null
                                            break
                                    }
                                }
                            }
                            if (ieCoverage && !ieCoverage.findEquivalent(ieMatch.coverages)) {
                                ieCoverage.issueEntitlement = ieMatch
                                if (!ieCoverage.save()) {
                                    throw new EntitlementCreationException(ieCoverage.errors)
                                } else {
                                    countChangesCoverageDates++
                                }
                            }
                        }
                        if (uploadPriceInfo) {
                            colMap.each { String colName, int colNo ->
                                if (colNo > -1 && cols[colNo]) {
                                    String cellEntry = cols[colNo].trim()
                                    if(cellEntry) {
                                        PriceItem priceItem
                                        try {
                                            switch (colName) {
                                                case ["listPriceEurCol", "localPriceEurCol"]: priceItem = ieMatch.priceItems ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_EUR } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_EUR)
                                                    priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                                    break
                                                case ["listPriceUsdCol", "localPriceUsdCol"]: priceItem = ieMatch.priceItems ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_USD } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_USD)
                                                    priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                                    break
                                                case ["listPriceGbpCol", "localPriceGbpCol"]: priceItem = ieMatch.priceItems ? ieMatch.priceItems.find { PriceItem pi -> pi.localCurrency == RDStore.CURRENCY_GBP } : new PriceItem(issueEntitlement: ieMatch, localCurrency: RDStore.CURRENCY_GBP)
                                                    priceItem.localPrice = escapeService.parseFinancialValue(cellEntry)
                                                    break
                                            }
                                        }
                                        catch (NumberFormatException e) {
                                            log.error("Unparseable number ${cellEntry}")
                                        }
                                        if (priceItem?.localPrice && priceItem?.localCurrency) {
                                            priceItem.setLaserID()
                                            if (!priceItem.save()) {
                                                throw new Exception(priceItem.errors.toString())
                                            } else {
                                                countChangesPrice++
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        wrongTitles << row
                    }
                } else {
                    wrongTitles << row
                }
            }
        }

        return [processCount: count, titleRow: titleRow, processCountChangesCoverageDates: countChangesCoverageDates, processCountChangesPrice: countChangesPrice, wrongTitles: wrongTitles, truncatedRows: truncatedRows.join(', ')]
        */
    }

    Map tippSelectForSurvey(MultipartFile inputFile, Map<String, Object> configMap) {
        Map<String, Object> result = issueEntitlementService.matchTippsFromFile(inputFile, configMap)
        if(!result.wrongSeparator) {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime(LocaleUtils.getCurrentLocale())
            EhcacheWrapper userCache = contextService.getUserCache(configMap.progressCacheKey)
            int pointsPerIteration = 25/result.matchedTitles.size(), perpetuallyPurchasedCount = 0
            Subscription subscriberSub = configMap.subscription
            SurveyConfig surveyConfig = SurveyConfig.get(configMap.surveyConfigID)
            SurveyInfo surveyInfo = surveyConfig.surveyInfo
            Org subscriber = subscriberSub.getSubscriberRespConsortia(), contextOrg = contextService.getOrg()
            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscriberSub)
            Set<String> toBeAdded = []
            userCache.put('progress', 50)
            result.matchedTitles.eachWithIndex{ TitleInstancePackagePlatform match, Map<String, Object> externalTitleData, int i ->
                IssueEntitlement ieInNewSub = IssueEntitlement.findByTippAndSubscriptionAndStatusNotEqual(match, subscriberSub, RDStore.TIPP_STATUS_REMOVED)
                boolean allowedToSelect
                PermanentTitle participantPerpetualAccessToTitle = PermanentTitle.findByTippAndOwner(match, subscriber)
                if(!participantPerpetualAccessToTitle)
                    participantPerpetualAccessToTitle = PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.tipp = :tipp and pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :subscriber and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection'))", [subscriber: subscriber, subscriberCons: RDStore.OR_SUBSCRIBER_CONS, tipp: match])[0]
                if (surveyConfig.pickAndChoosePerpetualAccess) {
                    allowedToSelect = !(participantPerpetualAccessToTitle) && (!ieInNewSub || (ieInNewSub && (contextOrg.id == surveyInfo.owner.id)))
                }
                else {
                    allowedToSelect = !ieInNewSub || (ieInNewSub && (contextOrg.id == surveyInfo.owner.id))
                }
                if (!ieInNewSub && allowedToSelect) {
                    toBeAdded << match.gokbId
                }
                else if (!allowedToSelect) {
                    externalTitleData.put('found_in_package', RDStore.YN_YES.value)
                    String subHeaderString = participantPerpetualAccessToTitle.subscription.name
                    if(participantPerpetualAccessToTitle.subscription.startDate)
                        subHeaderString += " (${sdf.format(participantPerpetualAccessToTitle.subscription.startDate)}-"
                    if(participantPerpetualAccessToTitle.subscription.endDate)
                        subHeaderString += "${sdf.format(participantPerpetualAccessToTitle.subscription.endDate)})"
                    externalTitleData.put('already_purchased_at', subHeaderString)
                    result.notAddedTitles << externalTitleData
                    perpetuallyPurchasedCount++
                }
                userCache.put('progress', 50+i*pointsPerIteration)
            }
            userCache.put('progress', 75)
            if (toBeAdded) {
                if (!issueEntitlementGroup) {
                    String groupName = IssueEntitlementGroup.countBySubAndName(subscriberSub,  surveyConfig.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(subscriberSub, surveyConfig.issueEntitlementGroupName) + 1) : surveyConfig.issueEntitlementGroupName
                    issueEntitlementGroup = new IssueEntitlementGroup(surveyConfig: surveyConfig, sub: subscriberSub, name: groupName)
                    if (!issueEntitlementGroup.save())
                        log.error(issueEntitlementGroup.getErrors().getAllErrors().toListString())
                }
                if(issueEntitlementGroup) {
                    bulkAddEntitlements(subscriberSub, toBeAdded, surveyConfig.pickAndChoosePerpetualAccess, issueEntitlementGroup)
                }
                userCache.put('progress', 100)
                result.success = true
            }
            result.addedCount = toBeAdded.size()
            result.notAddedCount = result.toAddCount-result.addedCount
            result.perpetuallyPurchasedCount = perpetuallyPurchasedCount
        }

        result
        /*
        InputStream stream = kbartFile.getInputStream()

        Integer countRows = 0
        Integer count = 0
        Integer countSelectTipps = 0
        Integer countNotSelectTipps = 0
        Org contextOrg = contextService.getOrg()
        Set selectedTipps = [], truncatedRows = []
        List wrongTitles = []
        List titleRow = []

        int zdbCol = -1, onlineIdentifierCol = -1, printIdentifierCol = -1, titleUrlCol = -1, titleIdCol = -1, doiCol = -1, pickCol = -1
        Set<Package> subPkgs = SubscriptionPackage.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription = :subscription', [subscription: subscription])
        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                       eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name),
                                                       isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                       issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name),
                                                       eisbn: IdentifierNamespace.findByNsAndNsType('eisbn', TitleInstancePackagePlatform.class.name),
                                                       doi: IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name),
                                                       title_id: IdentifierNamespace.findByNsAndNsType('title_id', TitleInstancePackagePlatform.class.name)]
        if(subPkgs) {

            ArrayList<String> rows = stream.text.split('\n')
            //read off first line of KBART file
            titleRow = rows.size() > 0 ? rows.remove(0).split('\t') : []
            titleRow.eachWithIndex { headerCol, int c ->
                switch (headerCol.toLowerCase().trim()) {
                    case "zdb_id": zdbCol = c
                        break
                    case "print_identifier": printIdentifierCol = c
                        break
                    case "online_identifier": onlineIdentifierCol = c
                        break
                    case "title_url": titleUrlCol = c
                        break
                    case "title_id": titleIdCol = c
                        break
                    case "doi_identifier": doiCol = c
                        break
                    case "print identifier": printIdentifierCol= c
                        break
                    case "online identifier": onlineIdentifierCol = c
                        break
                    case "zugriffs-url": titleUrlCol = c
                        break
                    case "access url": titleUrlCol = c
                        break
                    case "doi": doiCol= c
                        break
                    case "pick": pickCol = c
                        break
                }
            }
            rows.eachWithIndex { String row, int i ->
                countRows++
                log.debug("now processing entitlement ${i+1}")
                ArrayList<String> cols = row.split('\t', -1)
                if(cols.size() == titleRow.size()) {
                    if (pickCol >= 0 && cols[pickCol] != null && !cols[pickCol].trim().isEmpty()) {
                        TitleInstancePackagePlatform match = null
                        //cascade: 1. title_url, 2. title_id, 3. identifier map
                        if(titleUrlCol >= 0 && cols[titleUrlCol] != null && !cols[titleUrlCol].trim().isEmpty()) {
                            match = TitleInstancePackagePlatform.findByHostPlatformURLAndPkgInListAndStatus(cols[titleUrlCol].trim(), subPkgs, RDStore.TIPP_STATUS_CURRENT)
                        }
                        if(!match && titleIdCol >= 0 && cols[titleIdCol] != null && !cols[titleIdCol].trim().isEmpty()) {
                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status = :currentStatus', [subPkgs: subPkgs, value: cols[titleIdCol].trim(), ns: namespaces.title_id, currentStatus: RDStore.TIPP_STATUS_CURRENT])
                            if(matchList.size() == 1)
                                match = matchList[0] as TitleInstancePackagePlatform
                        }

                        if(!match && doiCol >= 0 && cols[doiCol] != null && !cols[doiCol].trim().isEmpty()) {
                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status = :currentStatus', [subPkgs: subPkgs, value: cols[doiCol].trim(), ns: namespaces.doi, currentStatus: RDStore.TIPP_STATUS_CURRENT])
                            if(matchList.size() == 1)
                                match = matchList[0] as TitleInstancePackagePlatform
                        }
                        if(!match && onlineIdentifierCol >= 0 && cols[onlineIdentifierCol] != null && !cols[onlineIdentifierCol].trim().isEmpty()) {
                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status = :currentStatus', [subPkgs: subPkgs, value: cols[onlineIdentifierCol].trim(), ns: [namespaces.eisbn, namespaces.eissn], currentStatus: RDStore.TIPP_STATUS_CURRENT])
                            if(matchList.size() == 1)
                                match = matchList[0] as TitleInstancePackagePlatform
                        }
                        if(!match && printIdentifierCol >= 0 && cols[printIdentifierCol] != null && !cols[printIdentifierCol].trim().isEmpty()) {
                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns in (:ns) and tipp.status = :currentStatus', [subPkgs: subPkgs, value: cols[printIdentifierCol].trim(), ns: [namespaces.isbn, namespaces.issn], currentStatus: RDStore.TIPP_STATUS_CURRENT])
                            if(matchList.size() == 1)
                                match = matchList[0] as TitleInstancePackagePlatform
                        }
                        if(!match && zdbCol >= 0 && cols[zdbCol] != null && !cols[zdbCol].trim().isEmpty()) {
                            List matchList = TitleInstancePackagePlatform.executeQuery('select id.tipp from Identifier id join id.tipp tipp where tipp.pkg in (:subPkgs) and id.value = :value and id.ns = :ns and tipp.status = :currentStatus', [subPkgs: subPkgs, value: cols[zdbCol].trim(), ns: namespaces.zdb, currentStatus: RDStore.TIPP_STATUS_CURRENT])
                            if(matchList.size() == 1)
                                match = matchList[0] as TitleInstancePackagePlatform
                        }
                        if(match) {
                            count++
                            String cellEntry = cols[pickCol].trim()
                            if (cellEntry.toLowerCase() == RDStore.YN_YES.value_de.toLowerCase() || cellEntry.toLowerCase() == RDStore.YN_YES.value_en.toLowerCase()) {
                                //TODO use from here
                                IssueEntitlement ieInNewSub = surveyService.titleContainedBySubscription(newSub, match, [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_EXPECTED])
                                boolean allowedToSelect = false
                                if (surveyConfig.pickAndChoosePerpetualAccess) {
                                    boolean participantPerpetualAccessToTitle = surveyService.hasParticipantPerpetualAccessToTitle3(newSub.getSubscriberRespConsortia(), match)
                                    allowedToSelect = !(participantPerpetualAccessToTitle) && (!ieInNewSub || (ieInNewSub && (contextOrg.id == surveyConfig.surveyInfo.owner.id)))
                                } else {
                                    allowedToSelect = !ieInNewSub || (ieInNewSub && (contextOrg.id == surveyConfig.surveyInfo.owner.id))
                                }

                                if (!ieInNewSub && allowedToSelect) {
                                    selectedTipps << match.gokbId
                                    countSelectTipps++
                                }
                                else if (!allowedToSelect) {
                                    countNotSelectTipps++
                                } else if(!ieInNewSub){
                                    wrongTitles << cols
                                }
                            }
                        }
                    }else{
                        wrongTitles << cols
                    }
                }
                else {
                    truncatedRows << i
                }
            }
        }
        return [titleRow: titleRow, processRows: countRows, processCount: count, selectedTipps: selectedTipps, countSelectTipps: countSelectTipps, countNotSelectTipps: countNotSelectTipps, wrongTitles: wrongTitles, truncatedRows: truncatedRows.join(', ')]
        */
    }

    @Deprecated
    def copySpecificSubscriptionEditorOfProvideryAndAgencies(Subscription sourceSub, Subscription targetSub){

        sourceSub.getProviders().each { provider ->
            RefdataValue refdataValue = RDStore.PRS_RESP_SPEC_SUB_EDITOR

            Person.getPublicByOrgAndObjectResp(provider, sourceSub, 'Specific subscription editor').each { prs ->

                if(!(provider in targetSub.orgRelations?.org)){
                    OrgRole or = OrgRole.findByOrgAndSub(provider, sourceSub)
                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, or.properties)
                    newOrgRole.sub = targetSub
                    newOrgRole.save()
                }

                if(!PersonRole.findWhere(prs: prs, org: provider, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, org: provider, sub: targetSub, responsibilityType: refdataValue)
                    newPrsRole.save()
                }
            }

            Person.getPrivateByOrgAndObjectRespFromAddressbook(provider, sourceSub, 'Specific subscription editor').each { prs ->
                if(!(provider in targetSub.orgRelations?.org)){
                    OrgRole or = OrgRole.findByOrgAndSub(provider, sourceSub)
                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, or.properties)
                    newOrgRole.sub = targetSub
                    newOrgRole.save()
                }

                if(!PersonRole.findWhere(prs: prs, org: provider, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, org: provider, sub: targetSub, responsibilityType: refdataValue)
                    newPrsRole.save()
                }
            }
        }
        sourceSub.getVendors().each { agency ->
            RefdataValue refdataValue = RDStore.PRS_RESP_SPEC_SUB_EDITOR

            Person.getPublicByOrgAndObjectResp(agency, sourceSub, 'Specific subscription editor').each { prs ->
                if(!(agency in targetSub.vendors)){
                    VendorRole vr = VendorRole.findByVendorAndSub(agency, sourceSub)
                    VendorRole newVendorRole = new VendorRole()
                    InvokerHelper.setProperties(newVendorRole, vr.properties)
                    newVendorRole.sub = targetSub
                    newVendorRole.save()
                }
                if(!PersonRole.findWhere(prs: prs, vendor: agency, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, vendor: agency, sub: targetSub, responsibilityType: refdataValue)
                    newPrsRole.save()
                }
            }

            Person.getPrivateByOrgAndObjectRespFromAddressbook(agency, sourceSub, 'Specific subscription editor').each { prs ->
                if(!(agency in targetSub.vendors)){
                    VendorRole vr = VendorRole.findByVendorAndSub(agency, sourceSub)
                    VendorRole newVendorRole = new VendorRole()
                    InvokerHelper.setProperties(newVendorRole, vr.properties)
                    newVendorRole.sub = targetSub
                    newVendorRole.save()
                }
                if(!PersonRole.findWhere(prs: prs, vendor: agency, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, vendor: agency, sub: targetSub, responsibilityType: refdataValue)
                    newPrsRole.save()
                }
            }
        }
    }

    /**
     * Creates a property of the given type for the given subscription with the given value and note.
     * Is a helper class for the subscription import
     * @param propDef the type of property (= property definition) to add
     * @param sub the subscription for which the property should be created
     * @param contextOrg the institution processing the creation
     * @param value the value of the property
     * @param note the note attached to the property
     */
    void createProperty(PropertyDefinition propDef, Subscription sub, Org contextOrg, String value, String note) {
        //check if private or custom property
        AbstractPropertyWithCalculatedLastUpdated prop
        if(propDef.tenant == contextOrg) {
            //process private property
            prop = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, sub, propDef, contextOrg)
        }
        else {
            //process custom property
            prop = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, sub, propDef, contextOrg)
        }
        if (propDef.isLongType()) {
            long longVal = Long.parseLong(value)
            prop.setLongValue(longVal)
        }
        else if (propDef.isBigDecimalType()) {
            BigDecimal decVal = new BigDecimal(value)
            prop.setDecValue(decVal)
        }
        else if (propDef.isRefdataValueType()) {
            RefdataValue rdv = (RefdataValue) genericOIDService.resolveOID(value)
            if(rdv)
                prop.setRefValue(rdv)
        }
        else if (propDef.isDateType()) {
            Date date = DateUtils.parseDateGeneric(value)
            if(date)
                prop.setDateValue(date)
        }
        else if (propDef.isURLType()) {
            URL url = new URL(value)
            if(url)
                prop.setUrlValue(url)
        }
        else {
            prop.setStringValue(value)
        }
        if(note)
            prop.note = note
        prop.save()
    }

    /**
     * Updates the properties inheriting from given property with the selected value
     * @param controller the controller calling the procedure
     * @param prop the property whose dependents should be updated
     * @param value the value to be taken
     */
    void updateProperty(def controller, AbstractPropertyWithCalculatedLastUpdated prop, def value) {

        String field = prop.type.getImplClassValueProperty()

        //Wenn eine Vererbung vorhanden ist.
        if(field && prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)){
            if(prop.instanceOf."${field}" == '' || prop.instanceOf."${field}" == null)
            {
                value = prop.instanceOf."${field}" ?: ''
            }else{
                //
                return
            }
        }

        if (value == '' && field) {
            // Allow user to set a rel to null be calling set rel ''
            prop[field] = null
            prop.save()
        } else {

            if (prop && value && field){

                if(field == "refValue") {
                    def binding_properties = ["${field}": value]
                    controller.bindData(prop, binding_properties)
                    //property.save(flush:true)
                    if(!prop.save(failOnError: true))
                    {
                        log.error("Error Property save: " +prop.error)
                    }
                } else if(field == "dateValue") {
                    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

                    def backup = prop."${field}"
                    try {
                        if (value && value.size() > 0) {
                            // parse new date
                            Date parsed_date = sdf.parse(value)
                            prop."${field}" = parsed_date
                        } else {
                            // delete existing date
                            prop."${field}" = null
                        }
                        prop.save(failOnError: true)
                    }
                    catch (Exception e) {
                        prop."${field}" = backup
                        log.error( e.toString() )
                    }
                } else if(field == "urlValue") {

                    def backup = prop."${field}"
                    try {
                        if (value && value.size() > 0) {
                            prop."${field}" = new URL(value)
                        } else {
                            // delete existing url
                            prop."${field}" = null
                        }
                        prop.save(failOnError: true)
                    }
                    catch (Exception e) {
                        prop."${field}" = backup
                        log.error( e.toString() )
                    }
                } else {
                    def binding_properties = [:]
                    if (prop."${field}" instanceof Double) {
                        value = Double.parseDouble(value)
                    }

                    binding_properties["${field}"] = value
                    controller.bindData(prop, binding_properties)

                    prop.save(failOnError: true)
                }
            }
        }

    }

    /**
     * ex AjaxController
     * Processes the inheritance (or clears it) of the identifiers for the given owner object
     * @param owner the {@link Subscription} or {@link License}
     * @param identifier the {@link Identifier} to be copied into or removed from the child objects
     */
    void inheritIdentifier(owner, Identifier identifier) {
        if (AuditConfig.getConfig(identifier, AuditConfig.COMPLETE_OBJECT)) {
            AuditConfig.removeAllConfigs(identifier)

            Identifier.findAllByInstanceOf(identifier).each{ Identifier id ->
                id.delete()
            }
        }
        else {
            String memberType
            if(owner instanceof Subscription)
                memberType = 'sub'
            else if(owner instanceof License)
                memberType = 'lic'
            if(memberType) {
                owner.getClass().findAllByInstanceOf(owner).each { member ->
                    Identifier existingIdentifier = Identifier.executeQuery('select id from Identifier id where id.'+memberType+' = :member and id.instanceOf = :id', [member: member, id: identifier])[0]
                    if (! existingIdentifier) {
                        List<Identifier> matchingIds = Identifier.executeQuery('select id from Identifier id where id.'+memberType+' = :member and id.value = :value and id.ns = :ns',[member: member, value: identifier.value, ns: identifier.ns])
                        // unbound id found with matching type, set backref
                        if (matchingIds) {
                            matchingIds.each { Identifier memberId ->
                                memberId.instanceOf = identifier
                                memberId.save()
                            }
                        }
                        else {
                            // no match found, creating new id with backref
                            Identifier.constructWithFactoryResult([value: identifier.value, note: identifier.note, parent: identifier, reference: member, namespace: identifier.ns])
                        }
                    }
                }
                AuditConfig.addConfig(identifier, AuditConfig.COMPLETE_OBJECT)
            }
        }
    }
    /**
     * Processes the inheritance (or clears it) of the alternative names for the given owner object
     * @param owner the {@link Subscription} or {@link License}
     * @param altName the {@link AlternativeName} to be copied into or removed from the child objects
     */
    void inheritAlternativeName(owner, AlternativeName altName) {
        if (AuditConfig.getConfig(altName, AuditConfig.COMPLETE_OBJECT)) {
            AuditConfig.removeAllConfigs(altName)

            AlternativeName.findAllByInstanceOf(altName).each{ AlternativeName a ->
                a.delete()
            }
        }
        else {
            String memberType
            if(owner instanceof Subscription)
                memberType = 'subscription'
            else if(owner instanceof License)
                memberType = 'license'
            if(memberType) {
                owner.getClass().findAllByInstanceOf(owner).each { member ->
                    AlternativeName existingAltName = AlternativeName.executeQuery('select altName from AlternativeName altName where altName.'+memberType+' = :member and altName.instanceOf = :altName', [member: member, altName: altName])[0]
                    if (! existingAltName) {
                        List<AlternativeName> matchingAltNames = AlternativeName.executeQuery('select altName from AlternativeName altName where altName.'+memberType+' = :member and altName.name = :name',[member: member, name: altName.name])
                        // unbound prop found with matching type, set backref
                        if (matchingAltNames) {
                            matchingAltNames.each { AlternativeName memberId ->
                                memberId.instanceOf = altName
                                memberId.save()
                            }
                        }
                        else {
                            // no match found, creating new prop with backref
                            Map<String, Object> configMap = [name: altName.name, instanceOf: altName]
                            if(owner instanceof Subscription)
                                configMap.subscription = member
                            else if(owner instanceof License)
                                configMap.license = member
                            AlternativeName.construct(configMap)
                        }
                    }
                }
                AuditConfig.addConfig(altName, AuditConfig.COMPLETE_OBJECT)
            }
        }
    }

    //-------------------------------------- helper section ----------------------------------------


    boolean checkThreadRunning(String threadName) {
        boolean threadRunning = false
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
        threadArray.each { Thread thread ->
            if (thread.name == threadName) {
                threadRunning = true
            }
        }
        threadRunning
    }

    String getCachedPackageName(String processName) {
        EhcacheWrapper ttl3600 = cacheService.getTTL3600Cache(processName)
        ttl3600.get('package')
    }

    void cachePackageName(String processName, String packageName) {
        EhcacheWrapper ttl3600 = cacheService.getTTL3600Cache(processName)
        ttl3600.put('package', packageName)
    }

    //-------------------------------------- cronjob section ----------------------------------------

    /**
     * Cronjob-triggered.
     * Processes subscriptions which have been marked with holdings to be frozen after their end time and sets all pending change
     * configurations to reject
     * @return true if the execution was successful, false otherwise
     * @deprecated is implicitlely done by {@link GlobalSourceSyncService#updateRecords(java.util.List, int, java.util.Set)} where only subscriptions are being considered whose end date has not been reached yet and {@link Subscription#holdingSelection} is set to Entire
     */
    @Deprecated
    boolean freezeSubscriptionHoldings() {
        boolean done, doneChild
        Date now = new Date()
        //on parent level
        Set<SubscriptionPackage> subPkgs = SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp join sp.subscription s where s.endDate != null and s.endDate <= :end and sp.freezeHolding = true', [end: now])
        //log.debug(subPkgs.toListString())
        if(subPkgs)
            done = PendingChangeConfiguration.executeUpdate('update PendingChangeConfiguration pcc set pcc.settingValue = :reject where pcc.subscriptionPackage in (:subPkgs)', [reject: RDStore.PENDING_CHANGE_CONFIG_REJECT, subPkgs: subPkgs]) > 0
        else done = false
        //on child level
        //Set<Subscription> subsWithFreezeAudit = AuditConfig.executeQuery('select ac.referenceId from AuditConfig ac where ac.referenceField = :freezeHoldingAudit', [freezeHoldingAudit: SubscriptionPackage.FREEZE_HOLDING]).collect { row -> Subscription.get(row) }
        //log.debug(subsWithFreezeAudit.toListString())
        Set<String> settingKeysAndNots = PendingChangeConfiguration.SETTING_KEYS+PendingChangeConfiguration.SETTING_KEYS.collect { String key -> key+PendingChangeConfiguration.NOTIFICATION_SUFFIX }
        doneChild = AuditConfig.executeUpdate('delete from AuditConfig ac where ac.referenceId in (select ac.referenceId from AuditConfig ac where ac.referenceField = :freezeHoldingAudit) and ac.referenceField in (:settingKeysAndNots)', [freezeHoldingAudit: SubscriptionPackage.FREEZE_HOLDING, settingKeysAndNots: settingKeysAndNots]) > 0
        done || doneChild
    }

    void setPermanentTitlesBySubscription(Subscription subscription) {
        if (subscription._getCalculatedType() in [CalculatedType.TYPE_LOCAL, CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_CONSORTIAL]) {
            if(!checkThreadRunning('permanentTitlesProcess_' + subscription.id)) {
                Long userId = contextService.getUser().id
                executorService.execute({
                    Thread.currentThread().setName('permanentTitlesProcess_' + subscription.id)
                    Long ownerId = subscription.getSubscriberRespConsortia().id, subId = subscription.id, start = System.currentTimeSeconds()
                    Sql sql = GlobalService.obtainSqlConnection()
                    try {
                    Connection connection = sql.dataSource.getConnection()

                    List<Long> status = [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id]
                    int countIeIDs = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.perpetualAccessBySub is null and ie.status.id in (:status)', [sub: subscription, status: status])[0]
                    log.debug("setPermanentTitlesBySubscription -> set perpetualAccessBySub of ${countIeIDs} IssueEntitlements to sub: " + subscription.id)

                    sql.executeUpdate('update issue_entitlement set ie_perpetual_access_by_sub_fk = :subId where ie_perpetual_access_by_sub_fk is null and ie_subscription_fk = :subId and ie_status_rv_fk = any(:idSet)', [idSet: connection.createArrayOf('bigint', [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id].toArray()), subId: subId])
                    sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), " + subId + ", now(), ie_tipp_fk, " + ownerId + " from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: subId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])

                    if (subscription.instanceOf == null && auditService.getAuditConfig(subscription, 'hasPerpetualAccess')) {
                        Set<Subscription> depending = Subscription.findAllByInstanceOf(subscription)
                        depending.eachWithIndex { dependingObj, i ->
                            subId = dependingObj.id
                            ownerId = dependingObj.getSubscriberRespConsortia().id
                            countIeIDs = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.perpetualAccessBySub is null and ie.status.id in (:status)', [sub: dependingObj, status: status])[0]
                            log.debug("setPermanentTitlesBySubscription (${i+1}/${depending.size()}) -> set perpetualAccessBySub of ${countIeIDs} IssueEntitlements to sub: " + dependingObj.id)

                            sql.executeUpdate('update issue_entitlement set ie_perpetual_access_by_sub_fk = :subId where ie_perpetual_access_by_sub_fk is null and ie_subscription_fk = :subId and ie_status_rv_fk = any(:idSet)', [idSet: connection.createArrayOf('bigint', [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id].toArray()), subId: subId])
                            sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), " + subId + ", now(), ie_tipp_fk, " + ownerId + " from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: subId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])

                        }
                    }
                    }
                    finally {
                        sql.close()
                    }
                    if(System.currentTimeSeconds()-start >= GlobalService.LONG_PROCESS_LIMBO) {
                        globalService.notifyBackgroundProcessFinish(userId, 'permanentTitlesProcess_' + subscription.id, messageSource.getMessage('subscription.details.unmarkPermanentTitles.completed' ,[subscription.name] as Object[], LocaleUtils.getCurrentLocale()))
                    }
                })
            }
        }
    }

    void setPermanentTitlesByPackage(Package pkg) {
        Org context = contextService.getOrg()
        if(!checkThreadRunning('permanentTitlesProcess_' + pkg.id + '_' + context.id)) {
            Set<Subscription> orgSubs = Subscription.executeQuery('select s from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where s.instanceOf = null and oo.org = :ctx and sp.pkg = :pkg', [ctx: context, pkg: pkg])
            orgSubs.each { Subscription s ->
                s.hasPerpetualAccess = true
                s.save()
            }
            executorService.execute({
                Thread.currentThread().setName('permanentTitlesProcess_' + pkg.id + '_' + context.id)
                Sql sql = GlobalService.obtainSqlConnection()
                try {
                Connection connection = sql.dataSource.getConnection()
                orgSubs.eachWithIndex { Subscription subscription, int s ->
                    Long ownerId = subscription.getSubscriberRespConsortia().id, subId = subscription.id

                    List<Long> status = [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id]
                    int countIeIDs = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.perpetualAccessBySub is null and ie.status.id in (:status)', [sub: subscription, status: status])[0]
                    log.debug("setPermanentTitlesBySubscription -> set perpetualAccessBySub of ${countIeIDs} IssueEntitlements to sub: " + subscription.id)

                    sql.executeUpdate('update issue_entitlement set ie_perpetual_access_by_sub_fk = :subId where ie_perpetual_access_by_sub_fk is null and ie_subscription_fk = :subId and ie_status_rv_fk = any(:idSet)', [idSet: connection.createArrayOf('bigint', [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id].toArray()), subId: subId])
                    sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), " + subId + ", now(), ie_tipp_fk, " + ownerId + " from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: subId, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])

                    if (subscription.instanceOf == null && auditService.getAuditConfig(subscription, 'hasPerpetualAccess')) {
                        Set<Subscription> depending = Subscription.findAllByInstanceOf(subscription)
                        depending.eachWithIndex { dependingObj, int i ->
                            ownerId = dependingObj.getSubscriberRespConsortia().id
                            countIeIDs = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.perpetualAccessBySub is null and ie.status.id in (:status)', [sub: dependingObj, status: status])[0]
                            log.debug("setPermanentTitlesBySubscription (${i + 1}/${depending.size()} at subscription ${s+1}/${orgSubs.size()}) -> set perpetualAccessBySub of ${countIeIDs} IssueEntitlements to sub: " + dependingObj.id)

                            sql.executeUpdate('update issue_entitlement set ie_perpetual_access_by_sub_fk = :subId where ie_perpetual_access_by_sub_fk is null and ie_subscription_fk = :subId and ie_status_rv_fk = any(:idSet)', [idSet: connection.createArrayOf('bigint', [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id].toArray()), subId: dependingObj.id])
                            sql.executeInsert("insert into permanent_title (pt_version, pt_ie_fk, pt_date_created, pt_subscription_fk, pt_last_updated, pt_tipp_fk, pt_owner_fk) select 0, ie_id, now(), " + dependingObj.id + ", now(), ie_tipp_fk, " + ownerId + " from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_status_rv_fk != :removed and ie_status_rv_fk = tipp_status_rv_fk and ie_subscription_fk = :subId and not exists(select pt_id from permanent_title where pt_tipp_fk = tipp_id and pt_owner_fk = :ownerId)", [subId: dependingObj.id, removed: RDStore.TIPP_STATUS_REMOVED.id, ownerId: ownerId])

                        }
                    }
                }
                }
                finally {
                    sql.close()
                }
            })
        }
    }

    void removePermanentTitlesBySubscription(Subscription subscription) {
        if (subscription._getCalculatedType() in [CalculatedType.TYPE_LOCAL, CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_CONSORTIAL]) {
            if(!checkThreadRunning('permanentTitlesProcess_' + subscription.id)) {
                long userId = contextService.getUser().id
                executorService.execute({
                    long start = System.currentTimeSeconds()
                    Thread.currentThread().setName('permanentTitlesProcess_' + subscription.id)
                    Sql sql = GlobalService.obtainSqlConnection()
                    try {
                    String ieQuery = 'select ie_id from issue_entitlement where ie_subscription_fk = :sub and ie_perpetual_access_by_sub_fk is not null',
                    countIeQuery = 'select count(*) from issue_entitlement where ie_subscription_fk = :sub and ie_perpetual_access_by_sub_fk is not null',
                    permQuery = 'select pt_id from permanent_title where pt_subscription_fk = :sub',
                    countPermQuery = 'select count(*) from permanent_title where pt_subscription_fk = :sub'
                    int parentIeCount = sql.rows(countIeQuery, [sub: subscription.id])[0]["count"],
                    parentPermCount = sql.rows(countPermQuery, [sub: subscription.id])[0]["count"]
                    log.debug("removePermanentTitlesBySubscription -> set perpetualAccessBySub of ${parentIeCount} IssueEntitlements to null: " + subscription.id)
                    int limit = 5000
                    for(int i = 0; i < parentIeCount; i += limit) {
                        String updateQuery = "update issue_entitlement set ie_perpetual_access_by_sub_fk = null where ie_id in (${ieQuery} limit ${limit})"
                        sql.executeUpdate(updateQuery, [sub: subscription.id])
                    }
                    for(int i = 0; i < parentPermCount; i += limit) {
                        String deleteQuery = "delete from permanent_title where pt_id in (${permQuery} limit ${limit})"
                        sql.executeUpdate(deleteQuery, [sub: subscription.id])
                    }


                    if (subscription.instanceOf == null && auditService.getAuditConfig(subscription, 'hasPerpetualAccess')) {
                        Set<Subscription> depending = Subscription.findAllByInstanceOf(subscription)
                        depending.eachWithIndex { Subscription dependingObj, int i->
                            int dependingIeCount = sql.rows(countIeQuery, [sub: dependingObj.id])[0]["count"],
                            dependingPermCount = sql.rows(countPermQuery, [sub: dependingObj.id])[0]["count"]
                            log.debug("removePermanentTitlesBySubscription (${i+1}/${depending.size()}) -> set perpetualAccessBySub of ${dependingIeCount} IssueEntitlements to null: " + dependingObj.id)
                            for(int j = 0; j < dependingIeCount; j += limit) {
                                String updateQuery = "update issue_entitlement set ie_perpetual_access_by_sub_fk = null where ie_id in (${ieQuery} limit ${limit})"
                                sql.executeUpdate(updateQuery, [sub: dependingObj.id])
                            }
                            for(int j = 0; j < dependingPermCount; j += limit) {
                                String deleteQuery = "delete from permanent_title where pt_id in (${permQuery} limit ${limit})"
                                sql.executeUpdate(deleteQuery, [sub: dependingObj.id])
                            }
                        }
                    }
                    }
                    finally {
                        sql.close()
                    }
                    if(System.currentTimeSeconds()-start >= GlobalService.LONG_PROCESS_LIMBO) {
                        globalService.notifyBackgroundProcessFinish(userId, 'permanentTitlesProcess_' + subscription.id, messageSource.getMessage('subscription.details.unmarkPermanentTitles.completed' ,[subscription.name] as Object[], LocaleUtils.getCurrentLocale()))
                    }
                })
            }
        }
    }

    void removePermanentTitlesByPackage(Package pkg) {
        Org context = contextService.getOrg()
        if(!checkThreadRunning('permanentTitlesProcess_' + pkg.id + '_' + context.id)) {
            Set<Subscription> orgSubs = Subscription.executeQuery('select s from SubscriptionPackage sp join sp.subscription s join s.orgRelations oo where s.instanceOf = null and oo.org = :ctx and sp.pkg = :pkg', [ctx: context, pkg: pkg])
            orgSubs.each { Subscription s ->
                s.hasPerpetualAccess = false
                s.save()
            }
            executorService.execute({
                Thread.currentThread().setName('permanentTitlesProcess_' + pkg.id + '_' + context.id)
                Sql sql = GlobalService.obtainSqlConnection()
                try {
                orgSubs.eachWithIndex { Subscription subscription, int s ->
                    String ieQuery = 'select ie_id from issue_entitlement where ie_subscription_fk = :sub and ie_perpetual_access_by_sub_fk is not null',
                           countIeQuery = 'select count(*) from issue_entitlement where ie_subscription_fk = :sub and ie_perpetual_access_by_sub_fk is not null',
                           permQuery = 'select pt_id from permanent_title where pt_subscription_fk = :sub',
                           countPermQuery = 'select count(*) from permanent_title where pt_subscription_fk = :sub'
                    int parentIeCount = sql.rows(countIeQuery, [sub: subscription.id])[0]["count"],
                        parentPermCount = sql.rows(countPermQuery, [sub: subscription.id])[0]["count"]
                    log.debug("removePermanentTitlesBySubscription -> set perpetualAccessBySub of ${parentIeCount} IssueEntitlements to null: " + subscription.id)
                    int limit = 5000
                    for(int i = 0; i < parentIeCount; i += limit) {
                        String updateQuery = "update issue_entitlement set ie_perpetual_access_by_sub_fk = null where ie_id in (${ieQuery} limit ${limit})"
                        sql.executeUpdate(updateQuery, [sub: subscription.id])
                    }
                    for(int i = 0; i < parentPermCount; i += limit) {
                        String deleteQuery = "delete from permanent_title where pt_id in (${permQuery} limit ${limit})"
                        sql.executeUpdate(deleteQuery, [sub: subscription.id])
                    }


                    if (subscription.instanceOf == null && auditService.getAuditConfig(subscription, 'hasPerpetualAccess')) {
                        Set<Subscription> depending = Subscription.findAllByInstanceOf(subscription)
                        depending.eachWithIndex { Subscription dependingObj, int i->
                            int dependingIeCount = sql.rows(countIeQuery, [sub: dependingObj.id])[0]["count"],
                                dependingPermCount = sql.rows(countPermQuery, [sub: dependingObj.id])[0]["count"]
                            log.debug("removePermanentTitlesByPackage (${i+1}/${depending.size()} at subscription ${s+1}/${orgSubs.size()}) -> set perpetualAccessBySub of ${dependingIeCount} IssueEntitlements to null: " + dependingObj.id)
                            for(int j = 0; j < dependingIeCount; j += limit) {
                                String updateQuery = "update issue_entitlement set ie_perpetual_access_by_sub_fk = null where ie_id in (${ieQuery} limit ${limit})"
                                sql.executeUpdate(updateQuery, [sub: dependingObj.id])
                            }
                            for(int j = 0; j < dependingPermCount; j += limit) {
                                String deleteQuery = "delete from permanent_title where pt_id in (${permQuery} limit ${limit})"
                                sql.executeUpdate(deleteQuery, [sub: dependingObj.id])
                            }
                        }
                    }
                }
                }
                finally {
                    sql.close()
                }
            })
        }
    }

    boolean checkPermanentTitleProcessRunning(Subscription subscription, Org context) {
        boolean permanentTitleProcessRunning, packageWide = false
        if(subscription.instanceOf) {
            for(SubscriptionPackage sp: subscription.instanceOf.packages) {
                packageWide = checkThreadRunning('permanentTitlesProcess_'+sp.pkg.id+'_'+context.id)
                if(packageWide)
                    break
            }
            permanentTitleProcessRunning = checkThreadRunning('permanentTitlesProcess_'+subscription.instanceOf.id) || packageWide
        }
        else {
            for(SubscriptionPackage sp: subscription.packages) {
                packageWide = checkThreadRunning('permanentTitlesProcess_'+sp.pkg.id+'_'+context.id)
                if(packageWide)
                    break
            }
            permanentTitleProcessRunning = checkThreadRunning('permanentTitlesProcess_'+subscription.id) || packageWide
        }
        permanentTitleProcessRunning
    }

    int countMultiYearSubInParentSub(Subscription subscription){
        return Subscription.executeQuery('select count(*) from Subscription s where s.instanceOf = :sub and s.isMultiYear = true', [sub: subscription])[0]
    }

    int countCustomSubscriptionPropertiesOfSub(Org contextOrg, Subscription subscription){
        return SubscriptionProperty.executeQuery('select count(*) from SubscriptionProperty where owner = :sub AND ((tenant = :contextOrg OR tenant is null) OR (tenant != :contextOrg AND isPublic = true)) AND type.tenant is null', [contextOrg: contextOrg, sub: subscription])[0]
    }

    int countPrivateSubscriptionPropertiesOfSub(Org contextOrg, Subscription subscription) {
        return SubscriptionProperty.executeQuery('select count(*) from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg)', [contextOrg: contextOrg, sub: subscription])[0]
    }

    int countCustomSubscriptionPropertiesOfMembersByParentSub(Org contextOrg, Subscription subscription){
        return SubscriptionProperty.executeQuery('select count(*) from SubscriptionProperty as sp where sp.owner.instanceOf = :sub AND ((sp.tenant = :contextOrg OR sp.tenant is null) OR (sp.tenant != :contextOrg AND sp.isPublic = true)) AND sp.type.tenant is null', [contextOrg: contextOrg, sub: subscription])[0]
    }

    int countCustomSubscriptionPropertyOfMembersByParentSub(Org contextOrg, Subscription subscription, PropertyDefinition propertyDefinition){
        return SubscriptionProperty.executeQuery('select count(*) from SubscriptionProperty as sp where sp.owner.instanceOf = :sub AND sp.type = :type AND ((sp.tenant = :contextOrg OR sp.tenant is null) OR (sp.tenant != :contextOrg AND sp.isPublic = true)) AND sp.type.tenant is null', [contextOrg: contextOrg, sub: subscription, type: propertyDefinition])[0]
    }

    int countPrivateSubscriptionPropertiesOfMembersByParentSub(Org contextOrg, Subscription subscription, PropertyDefinition propertyDefinition){
        return SubscriptionProperty.executeQuery('select count(*) from SubscriptionProperty as sp where sp.owner.instanceOf = :sub AND sp.type = :type AND (sp.type.tenant = :contextOrg AND sp.tenant = :contextOrg)', [contextOrg: contextOrg, sub: subscription, type: propertyDefinition])[0]
    }

    Map selectSubMembersWithImport(Map tableData) {

        Integer processCount = 0
        Integer processRow = 0

        List orgList = []

        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [gnd  : IdentifierNamespace.findByNsAndNsType('gnd_org_nr', Org.class.name),
                                                       isil: IdentifierNamespace.findByNsAndNsType('ISIL', Org.class.name),
                                                       ror: IdentifierNamespace.findByNsAndNsType('ROR ID',Org.class.name),
                                                       wib : IdentifierNamespace.findByNsAndNsType('wibid', Org.class.name),
                                                       dealId : IdentifierNamespace.findByNsAndNsType('deal_id', Org.class.name)]

        Map<String, Integer> colMap = [gndCol: -1, isilCol: -1, rorCol: -1, wibCol: -1, dealCol: -1,
                                       startDateCol: -1, endDateCol: -1, ]

        List titleRow = tableData.headerRow, rows = tableData.rows, wrongOrgs = [], truncatedRows = []
        titleRow.eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case ["laser-id", "laser-id (einrichtung)", "laser-id (institution)", "laser-id (einrichtungslizenz)", "laser-id (institution subscription)"]: colMap.uuidCol = c
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
                case "start date":
                case "laufzeit-beginn":
                case "startdatum":
                case "runtime (start)": colMap.startDateCol = c
                    break
                case "end date":
                case "laufzeit-ende":
                case "enddatum":
                case "runtime (end)": colMap.endDateCol = c
            }
        }
        rows.eachWithIndex { List cols, int i ->
            processRow++
            log.debug("now processing rows ${i}")
            if(cols.size() == titleRow.size()) {
                Org match = null
                if (colMap.uuidCol >= 0 && cols[colMap.uuidCol] != null && !cols[colMap.uuidCol].trim().isEmpty()) {
                    match = Org.findByLaserIDAndArchiveDateIsNull(cols[colMap.uuidCol].trim())
                }
                if(!match) {
                    if (colMap.wibCol >= 0 && cols[colMap.wibCol] != null && !cols[colMap.wibCol].trim().isEmpty()) {
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
                    if (!match && colMap.dealCol >= 0 && cols[colMap.dealCol] != null && !cols[colMap.dealCol].trim().isEmpty()) {
                        List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.dealCol].trim(), ns: namespaces.dealId])
                        if (matchList.size() == 1)
                            match = matchList[0] as Org
                    }
                }

                if (match) {
                    processCount++
                        Map orgMap = [orgId: match.id]
                        colMap.each { String colName, int colNo ->
                            if (colNo > -1 && cols[colNo]) {
                                String cellEntry = cols[colNo].trim()
                                    switch (colName) {
                                        case "startDateCol": orgMap.startDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : null
                                            break
                                        case "endDateCol": orgMap.endDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : null
                                            break
                                }
                            }
                        }
                    orgList << orgMap

                } else {
                    wrongOrgs << i+2
                }
            }else{
                truncatedRows << i+2
            }
        }

        return [orgList: orgList, processCount: processCount, processRow: processRow, wrongOrgs: wrongOrgs.join(', '), truncatedRows: truncatedRows.join(', ')]
    }

    Map uploadRequestorIDs(Platform platform, Map tableData) {
        Integer processCount = 0
        Integer processRow = 0
        List orgList = []
        List<List> rows = tableData.rows
        Map<String, Integer> colMap = [laserIDCol: -1, customerIdCol: -1, requestorIdCol: -1]
        List titleRow = tableData.headerRow, wrongOrgs = [], truncatedRows = []
        titleRow.eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case "laser-id": colMap.laserIDCol = c
                    break
                case ["customer id", "customer-id"]: colMap.customerIdCol = c
                    break
                case ["requestor id", "requestor-id", "api-key"]: colMap.requestorIdCol = c
                    break
            }
        }
        rows.eachWithIndex { List cols, int i ->
            processRow++
            log.debug("now processing row ${i}")
            if(cols.size() == titleRow.size()) {
                CustomerIdentifier match = null
                if (colMap.laserIDCol >= 0 && cols[colMap.laserIDCol] != null && !cols[colMap.laserIDCol].trim().isEmpty()) {
                    List matchList = CustomerIdentifier.executeQuery('select ci from CustomerIdentifier ci join ci.customer o where o.laserID = :laserID and ci.platform = :platform', [laserID: cols[colMap.laserIDCol].trim(), platform: platform])
                    if (matchList.size() == 1)
                        match = matchList[0] as CustomerIdentifier
                    if (match) {
                        processCount++
                        Map orgMap = [orgId: match.customer.id]
                        if (colMap.customerIdCol > -1 && cols[colMap.customerIdCol] && cols[colMap.customerIdCol].trim()) {
                            match.value = cols[colMap.customerIdCol].trim()
                        }
                        if(colMap.requestorIdCol > -1 && cols[colMap.requestorIdCol] && cols[colMap.requestorIdCol].trim()) {
                            match.requestorKey = cols[colMap.requestorIdCol].trim()
                        }
                        match.save()
                        orgList << orgMap
                    }
                    else {
                        wrongOrgs << i+1
                    }
                }
            }else{
                truncatedRows << i+1
            }
        }
        return [orgList: orgList, processCount: processCount, processRow: processRow, wrongOrgs: wrongOrgs.join(', '), truncatedRows: truncatedRows.join(', ')]
    }

    Map<String, Object> getRenewalGenerics(GrailsParameterMap params) {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(result) {
            SwissKnife.setPaginationParams(result, params, (User) result.user)
            result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
            result.surveyInfo = result.surveyConfig.surveyInfo
            result.subscriber = result.subscription.getSubscriberRespConsortia()
            result.editable = surveyService.isEditableSurvey(contextService.getOrg(), result.surveyInfo)
            Map rtParams = FilterLogic.resolveTabAndStatusForRenewalTabsMenu(params)
            if (rtParams.tab)    { params.tab = rtParams.tab }
            if (rtParams.subTab) { params.subTab = rtParams.subTab }
            if (rtParams.status) {
                params.tippStatus = rtParams.status
                params.ieStatus = rtParams.status
            }
            if(params.containsKey('status'))
                result.listOfStatus = Params.getRefdataList(params, 'status')
            else result.listOfStatus = [RDStore.TIPP_STATUS_CURRENT]
            result.sort = params.sort ?: 'tipp.sortname'
            result.order = params.order ?: 'asc'
            Map<String, Object> configMap = params.clone()
            Subscription baseSub = result.surveyConfig.subscription ?: result.subscription.instanceOf
            result.baseSub = baseSub
            configMap.subscription = result.subscription
            configMap.packages = baseSub.packages.pkg
            result.packageInstance = baseSub.packages.pkg[0] //there was an if check about baseSub.pkg
            result.countSelectedIEs = surveyService.countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)
            result.countAllTipps = TitleInstancePackagePlatform.countByPkgAndStatus(result.packageInstance, RDStore.TIPP_STATUS_CURRENT)
            result.configMap = configMap
        }
        result
    }


    /**
     * @return List<Long> with accessible (my) subscription ids
     */
    @Deprecated
    List<Long> getCurrentSubscriptionIds(Org context) {
        // moved from deleted OrgTypeService ..
        return Subscription.executeQuery("select oo.sub.id from OrgRole oo where oo.org = :subOrg and oo.roleType in (:roleTypes)",
                [subOrg: context, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIUM]])
    }
}
