package de.laser


import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.PackageService
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.titles.TitleInstance
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.BatchingPreparedStatementWrapper
import groovy.sql.BatchingStatementWrapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.MultipartFile

import java.util.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Transactional
class SubscriptionService {
    def contextService
    def accessService
    def subscriptionsQueryService
    MessageSource messageSource
    def escapeService
    def refdataService
    def propertyService
    FilterService filterService
    GenericOIDService genericOIDService
    LinksGenerationService linksGenerationService
    ComparisonService comparisonService
    PackageService packageService

    /**
     * ex MyInstitutionController.currentSubscriptions()
     * Gets the current subscriptions for the given institution
     * @param params the request parameter map
     * @param contextUser the user whose settings should be considered
     * @param contextOrg the institution whose subscriptions should be accessed
     * @return a result map containing a list of subscriptions and other site parameters
     */
    Map<String,Object> getMySubscriptions(GrailsParameterMap params, User contextUser, Org contextOrg) {
        Map<String,Object> result = [:]
        EhcacheWrapper cache = contextService.getCache("/subscriptions/filter/",ContextService.USER_SCOPE)
        if(cache && cache.get('subscriptionFilterCache')) {
            if(!params.resetFilter && !params.isSiteReloaded)
                params.putAll((GrailsParameterMap) cache.get('subscriptionFilterCache'))
            else params.remove('resetFilter')
            cache.remove('subscriptionFilterCache') //has to be executed in any case in order to enable cache updating
        }
        SwissKnife.setPaginationParams(result, params, contextUser)

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('init data fetch')
        pu.setBenchmark('consortia')
        result.availableConsortia = Combo.executeQuery("select c.toOrg from Combo as c where c.fromOrg = :fromOrg", [fromOrg: contextOrg])

        def consRoles = Role.findAll { authority == 'ORG_CONSORTIUM' }
        pu.setBenchmark('all consortia')
        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSetting os_ct, OrgSetting os_gs where 
                        os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and
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

        def date_restriction = null
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.checkMinUserOrgRole(contextUser, contextOrg, 'INST_EDITOR')

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                String[] defaultStatus = [RDStore.SUBSCRIPTION_CURRENT.id.toString()]
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

        pu.setBenchmark('get base query')
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextOrg)
        result.filterSet = tmpQ[2]
        List<Subscription> subscriptions
        pu.setBenchmark('fetch subscription data')
        if(params.sort == "providerAgency") {
            subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ).collect{ row -> row[0] }
        }
        else {
            subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]
        }
        result.allSubscriptions = subscriptions
        if(!params.exportXLS)
            result.num_sub_rows = subscriptions.size()

        result.date_restriction = date_restriction
        pu.setBenchmark('get properties')
        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)
        /* deactivated as statistics key is submitted nowhere, as of July 16th, '20
        if (OrgSetting.get(contextOrg, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID) instanceof OrgSetting){
            result.statsWibid = contextOrg.getIdentifierByType('wibid')?.value
            result.usageMode = accessService.checkPerm("ORG_CONSORTIUM") ? 'package' : 'institution'
        }
         */
        pu.setBenchmark('end properties')
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)
        pu.setBenchmark('fetch licenses')
        if(subscriptions)
            result.allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndSourceLicenseIsNotNullAndLinkType(result.subscriptions,RDStore.LINKTYPE_LICENSE)
        pu.setBenchmark('after licenses')
        List bm = pu.stopBenchmark()
        result.benchMark = bm
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

        ProfilerUtils pu = new ProfilerUtils()
        pu.setBenchmark('filterService')

        SwissKnife.setPaginationParams(result, params, contextUser)

        Map<String,Object> fsq = filterService.getOrgComboQuery(params+[comboType:RDStore.COMBO_TYPE_CONSORTIUM.value,sort:'o.sortname'], contextOrg)
        result.filterConsortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams)

        pu.setBenchmark('filterSubTypes & filterPropList')

        if(params.filterSet)
            result.filterSet = params.filterSet

        result.filterSubTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE).minus(RDStore.SUBSCRIPTION_TYPE_LOCAL)
        result.filterPropList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        /*
        String query = "select ci, subT, roleT.org from CostItem ci join ci.owner orgK join ci.sub subT join subT.instanceOf subK " +
                "join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                "where orgK = :org and orgK = roleK.org and roleK.roleType = :rdvCons " +
                "and orgK = roleTK.org and roleTK.roleType = :rdvCons " +
                "and roleT.roleType = :rdvSubscr "
        */

        // CostItem ci

        pu.setBenchmark('filter query')

        String query
        Long statusId
        Map qarams
        Map<Subscription,Set<License>> linkedLicenses = [:]

        if('withCostItems' in tableConf) {
            query = "select ci, subT, roleT.org " +
                    " from CostItem ci right outer join ci.sub subT join subT.instanceOf subK " +
                    " join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                    " where roleK.org = :org and roleK.roleType = :rdvCons " +
                    " and roleTK.org = :org and roleTK.roleType = :rdvCons " +
                    " and roleT.roleType in (:rdvSubscr) " +
                    " and ( ci is null or ci.costItemStatus != :deleted or ci.owner = :org )"
            qarams = [org      : contextOrg,
                      rdvCons  : RDStore.OR_SUBSCRIPTION_CONSORTIA,
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
                      rdvCons  : RDStore.OR_SUBSCRIPTION_CONSORTIA,
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
        else if(!params.filterSet) {
            query += " and roleT.org.id = :member "
            qarams.put('member', result.filterConsortiaMembers[0].id)
            params.member = result.filterConsortiaMembers[0].id
            result.defaultSet = true
        }

        if (params.identifier?.length() > 0) {
            query += " and exists (select ident from Identifier ident join ident.org ioorg " +
                    " where ioorg = roleT.org and LOWER(ident.value) like LOWER(:identifier)) "
            qarams.put('identifier', "%${params.identifier}%")
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

            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
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
            //params.hasPerpetualAccess = RDStore.YN_YES.id.toString()
            result.defaultSet = true
        }
        if(params.long("status") == RDStore.SUBSCRIPTION_CURRENT.id && params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) {
            statusQuery = " and (subT.status.id = :status or subT.hasPerpetualAccess = true) "
        }
        else if (params.hasPerpetualAccess) {
            query += " and subT.hasPerpetualAccess = :hasPerpetualAccess "
            qarams.put('hasPerpetualAccess', (params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) ? true : false)
        }
        query += statusQuery

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, query, 'subT', qarams)
            query = psq.query
            qarams = psq.queryParams
        }

        if (params.form?.size() > 0) {
            query += " and subT.form.id = :form "
            qarams.put('form', params.long('form'))
        }
        if (params.resource?.size() > 0) {
            query += " and subT.resource.id = :resource "
            qarams.put('resource', params.long('resource'))
        }
        if (params.subTypes?.size() > 0) {
            query += " and subT.type.id in (:subTypes) "
            qarams.put('subTypes', params.list('subTypes').collect { it -> Long.parseLong(it) })
        }

        if (params.subKinds?.size() > 0) {
            query += " and subT.kind.id in (:subKinds) "
            qarams.put('subKinds', params.list('subKinds').collect { Long.parseLong(it) })
        }

        if (params.isPublicForApi) {
            query += " and subT.isPublicForApi = :isPublicForApi "
            qarams.put('isPublicForApi', (params.isPublicForApi == RDStore.YN_YES.id.toString()) ? true : false)
        }

        if (params.hasPublishComponent) {
            query += " and subT.hasPublishComponent = :hasPublishComponent "
            qarams.put('hasPublishComponent', (params.hasPublishComponent == RDStore.YN_YES.id.toString()) ? true : false)
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

        String orderQuery = " order by roleT.org.sortname, subT.name, subT.startDate, subT.endDate"
        if (params.sort?.size() > 0) {
            orderQuery = " order by " + params.sort + " " + params.order
        }

        if(params.filterSet && !params.member && !params.validOn && !params.status && !params.filterPropDef && !params.filterProp && !params.form && !params.resource && !params.subTypes)
            result.filterSet = false

        //log.debug( query + " " + orderQuery )
        // log.debug( qarams )

        if('withCostItems' in tableConf) {
            pu.setBenchmark('costs init')

            List costs = CostItem.executeQuery(
                    query + " " + orderQuery, qarams
            )
            pu.setBenchmark('read off costs')
            //post filter; HQL cannot filter that parameter out
            result.costs = costs
            result.totalCount = costs.size()
            result.totalMembers = []
            costs.each { row ->
                result.totalMembers << row[2]
            }
            if(params.exportXLS || params.format)
                result.entries = costs
            else result.entries = costs.drop((int) result.offset).take((int) result.max)

            result.finances = {
                Map entries = [:]
                result.entries.each { obj ->
                    if (obj[0] && obj[0].owner.id == contextOrg.id) {
                        CostItem ci = (CostItem) obj[0]
                        if (!entries."${ci.billingCurrency}") {
                            entries."${ci.billingCurrency}" = 0.0
                        }

                        if (ci.costItemElementConfiguration == RDStore.CIEC_POSITIVE) {
                            entries."${ci.billingCurrency}" += ci.costInBillingCurrencyAfterTax
                        }
                        else if (ci.costItemElementConfiguration == RDStore.CIEC_NEGATIVE) {
                            entries."${ci.billingCurrency}" -= ci.costInBillingCurrencyAfterTax
                        }
                        //result.totalMembers << ci.sub.getSubscriber()
                    }
                    if (obj[1]) {
                        Subscription subCons = (Subscription) obj[1]
                        //linkedLicenses.put(subCons,Links.findAllByDestinationSubscriptionAndLinkType(subCons,RDStore.LINKTYPE_LICENSE).collect { Links row -> genericOIDService.resolveOID(row.source)})
                        linkedLicenses.put(subCons,Links.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType',[subscription:subCons,linkType:RDStore.LINKTYPE_LICENSE]))
                    }
                }
                entries
            }()
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

        result.pu = pu

        result
    }

    @Deprecated
    Set<Org> getAllTimeSubscribersForConsortiaSubscription(Set<Subscription> entrySet) {
        Org.executeQuery('select oo.org from OrgRole oo join oo.org org where oo.sub.instanceOf in (:entry) and oo.roleType in (:subscriberRoleTypes) order by org.sortname asc, org.name asc',[entry:entrySet,subscriberRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
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
            queryStart = "select s.id, s.name, s.startDate, s.endDate, s.status, so.org, so.roleType, s.instanceOf.id "
            params.joinQuery = "join s.orgRelations so"
        }

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getSubscriptionsConsortiaQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery(queryStart + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery(params)
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

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getSubscriptionsConsortiaQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))
            if (params.showSubscriber) {
                List parents = result.clone()
                Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]
                result.addAll(Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf in (:parents) and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parents: parents, subscriberRoleTypes:subscriberRoleTypes]))
            }

            tmpQ = getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getSubscriptionsLocalLicenseQuery(params)
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

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery(params)
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

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery(params)
            result.addAll(Subscription.executeQuery("select s " + tmpQ[0], tmpQ[1]))
        }

        result
    }

    /**
     * Retrieves consortial parent subscriptions matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent subscriptions matching the given filter
     */
    private List getSubscriptionsConsortiaQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.showParentsAndChildsSubs = params.showSubscriber
        queryParams.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        String joinQuery = params.joinQuery ?: ""
        List result = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, contextService.getOrg(), joinQuery)
        result
    }

    /**
     * Retrieves consortial member subscriptions matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent subscriptions matching the given filter
     */
    private List getSubscriptionsConsortialLicenseQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.orgRole = RDStore.OR_SUBSCRIBER.value
        queryParams.subTypes = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id
        String joinQuery = params.joinQuery ?: ""
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, contextService.getOrg(), joinQuery)
    }

    /**
     * Retrieves local licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    private List getSubscriptionsLocalLicenseQuery(Map params) {
        Map queryParams = [:]
        if (params?.status) {
            queryParams.status = params.status
        }
        queryParams.orgRole = RDStore.OR_SUBSCRIBER.value
        queryParams.subTypes = RDStore.SUBSCRIPTION_TYPE_LOCAL.id
        String joinQuery = params.joinQuery ?: ""
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, contextService.getOrg(), joinQuery)
    }

    /**
     * Gets the member subscriptions for the given consortial subscription
     * @param subscription the subscription whose members should be queried
     * @return a list of member subscriptions
     */
    List getValidSubChilds(Subscription subscription) {
        List<Subscription> validSubChildren = Subscription.executeQuery('select oo.sub from OrgRole oo where oo.sub.instanceOf = :sub and oo.roleType in (:subRoleTypes) order by oo.org.sortname asc, oo.org.name asc',[sub:subscription,subRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
        validSubChildren
    }

    /**
     * Gets the member subscriptions for the given consortial subscription with status current
     * @param subscription the subscription whose members should be queried
     * @return a sorted list of member subscriptions which are marked as current
     */
    List getCurrentValidSubChilds(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatus(
                subscription,
                RDStore.SUBSCRIPTION_CURRENT
        )
        if(validSubChilds) {
            validSubChilds = validSubChilds?.sort { a, b ->
                def sa = a.getSubscriber()
                def sb = b.getSubscriber()
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
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
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
                def sa = a.getSubscriber()
                def sb = b.getSubscriber()
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
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
                subscription,
                [RDStore.SUBSCRIPTION_CURRENT, RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION]
        )

        if(validSubChilds) {
            List orgs = OrgRole.findAllBySubInListAndRoleType(validSubChilds, RDStore.OR_SUBSCRIBER_CONS)

            if (orgs) {
                return orgs.org
            } else {
                return []
            }
        }else {
            return []
        }
    }

    /**
     * Gets the issue entitlements (= the titles) of the given subscription
     * @param subscription the subscription whose holding should be returned
     * @return a sorted list of issue entitlements
     */
    List getIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status != :ieStatus",
                        [sub: subscription, ieStatus: RDStore.TIPP_STATUS_REMOVED])
                : []
        ies.sort {it.sortname}
        ies
    }

    @Deprecated
    List getIssueEntitlementsWithFilter(Subscription subscription, params) {

        if(subscription) {
            String base_qry = null
            Map<String,Object> qry_params = [subscription: subscription]

            SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
            def date_filter
            if (params.asAt && params?.asAt?.length() > 0) {
                date_filter = sdf.parse(params.asAt)
                /*result.as_at_date = date_filter
                result.editable = false;*/
            } else {
                date_filter = new Date()
               /* result.as_at_date = date_filter*/
            }
            // We dont want this filter to reach SQL query as it will break it.
            def core_status_filter = params.sort == 'core_status'
            if (core_status_filter) params.remove('sort');

            if (params.filter) {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                if (params.mode != 'advanced') {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                    // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                    // qry_params.add(deleted_ie);
                    base_qry += "and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }
                base_qry += "and ( ( lower(ie.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = ie.tipp.id and ident.value like :identifier ) ) ) "
                qry_params.title = "%${params.filter.trim().toLowerCase()}%"
                qry_params.identifier = "%${params.filter}%"
            } else {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                if (params.mode != 'advanced') {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                    base_qry += " and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) ) "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }
            }

            if(params.mode != 'advanced') {
                base_qry += " and ie.status = :current "
                qry_params.current = RDStore.TIPP_STATUS_CURRENT
            }
            else {
                base_qry += " and ie.status != :ieStatus "
                qry_params.ieStatus = RDStore.TIPP_STATUS_REMOVED
            }

            if(params.ieAcceptStatusFixed) {
                base_qry += " and ie.acceptStatus = :ieAcceptStatus "
                qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            }

            if(params.ieAcceptStatusNotFixed) {
                base_qry += " and ie.acceptStatus != :ieAcceptStatus "
                qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            }

            if(params.summaryOfContent) {
                base_qry += " and lower(ie.tipp.summaryOfContent) like :summaryOfContent "
                qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            }

            if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
                base_qry += " and lower(ie.tipp.subjectReference) in (:subject_references)"
                qry_params.subject_references = params.list('subject_references').collect { ""+it.toLowerCase()+"" }
            }

            if (params.series_names && params.series_names != "" && params.list('series_names')) {
                base_qry += " and lower(ie.tipp.seriesName) in (:series_names)"
                qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
            }

            if(params.ebookFirstAutorOrFirstEditor) {
                base_qry += " and (lower(ie.tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
                qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                base_qry += " and ie.tipp.pkg.id = :pkgId "
                qry_params.pkgId = Long.parseLong(params.pkgfilter)
            }

            //dateFirstOnline from
            if(params.dateFirstOnlineFrom) {
                base_qry += " and (ie.tipp.dateFirstOnline is not null AND ie.tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
                qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)

            }
            //dateFirstOnline to
            if(params.dateFirstOnlineTo) {
                base_qry += " and (ie.tipp.dateFirstOnline is not null AND ie.tipp.dateFirstOnline <= :dateFirstOnlineTo) "
                qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
            }

            if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ie.${params.sort} ${params.order} "
            } else {
                base_qry += "order by lower(ie.sortname) asc"
            }

            List<IssueEntitlement> ies = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, [max: params.max, offset: params.offset])


            ies.sort { it.sortname }
            ies
        }else{
            List<IssueEntitlement> ies = []
            ies
        }
    }

    /**
     * Gets issue entitlements for the given subscription which are under consideration
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlements which are currently under consideration
     */
    List getIssueEntitlementsUnderConsideration(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.sortname}
        ies
    }

    /**
     * Gets issue entitlements for the given subscription which are under negotiation
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlements which are currently under negotiation
     */
    List getIssueEntitlementsUnderNegotiation(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.sortname}
        ies
    }

    /**
     * Gets issue entitlements for the given subscription which are not fixed yet
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlements which are not fixed
     */
    List getIssueEntitlementsNotFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.sortname}
        ies
    }

    /**
     * Counts the issue entitlements for the given subscription which are not fixed
     * @param subscription the subscription whose titles should be counted
     * @return a sorted list of issue entitlements which are not fixed
     */
    Integer countIssueEntitlementsNotFixed(Subscription subscription) {
        Integer iesCount = subscription?
                IssueEntitlement.executeQuery("select count(ie) from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0]
                : 0
        iesCount
    }

    /**
     * Retrieves issue entitlement IDs for the given subscription which are not fixed yet
     * @param subscription the subscription whose titles should be returned
     * @return a list of issue entitlements IDs which are not fixed
     */
    List<Long> getIssueEntitlementIDsNotFixed(Subscription subscription) {
        List<Long> ies = subscription?
                IssueEntitlement.executeQuery("select ie.id from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus order by ie.sortname",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies
    }

    /**
     * Gets issue entitlements for the given subscription which are fixed
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlements which are fixed
     */
    List getIssueEntitlementsFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.sortname}
        ies
    }

    /**
     * Counts the issue entitlements for the given subscription which are fixed
     * @param subscription the subscription whose titles should be counted
     * @return a sorted list of issue entitlements which are fixed
     */
    Integer countIssueEntitlementsFixed(Subscription subscription) {
        Integer countIes = subscription?
                IssueEntitlement.executeQuery("select count(ie) from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0]
                : 0
        countIes
    }

    /**
     * Gets issue entitlement IDs for the given subscription which are fixed
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of issue entitlement IDs which are fixed
     */
    List<Long> getIssueEntitlementIDsFixed(Subscription subscription) {
        List<Long> ies = subscription?
                IssueEntitlement.executeQuery("select ie.id from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies
    }

    @Deprecated
    List<Long> getTippIDsFixed(Subscription subscription) {
        List<Long> tipps = subscription?
                IssueEntitlement.executeQuery("select ie.tipp.id from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        tipps
    }

    /**
     * Gets the current issue entitlements for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return a sorted list of current issue entitlements
     */
    List getCurrentIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :cur",
                        [sub: subscription, cur: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.sortname}
        ies
    }

    /**
     * Gets the IDs of current issue entitlements for the given subscription
     * @param subscription the subscription whose titles should be returned
     * @return a list of issue entitlement IDs
     */
    List getCurrentIssueEntitlementIDs(Subscription subscription) {
        List<Long> ieIDs = subscription ? IssueEntitlement.executeQuery("select ie.id from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :cur and ie.acceptStatus = :fixed", [sub: subscription, cur: RDStore.TIPP_STATUS_CURRENT, fixed: RDStore.IE_ACCEPT_STATUS_FIXED]) : []
        ieIDs
    }

    @Deprecated
    List getVisibleOrgRelationsWithoutConsortia(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { OrgRole or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
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
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

    /**
     * Adds the given package to the given subscription. It may be specified if titles should be created as well or not
     * @param subscription the subscription whose holding should be enriched
     * @param pkg the package to link
     * @param createEntitlements should entitlements be created as well?
     */
    void addToSubscription(Subscription subscription, Package pkg, boolean createEntitlements) {
        Sql sql = GlobalService.obtainSqlConnection()
        sql.executeInsert('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_freeze_holding) values (0, :pkgId, :subId, false) on conflict on constraint sub_package_unique do nothing', [pkgId: pkg.id, subId: subscription.id])
        /*
        List<SubscriptionPackage> dupe = SubscriptionPackage.executeQuery(
                "from SubscriptionPackage where subscription = :sub and pkg = :pkg", [sub: subscription, pkg: pkg])

            // Step 3 - If createEntitlements ...
        */
        if ( createEntitlements ) {
            //List packageTitles = sql.rows("select * from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk = :current", [pkgId: pkg.id, current: RDStore.TIPP_STATUS_CURRENT.id])
            packageService.bulkAddHolding(sql, subscription.id, pkg.id, subscription.hasPerpetualAccess)
        }
    }

    /**
     * Adds the holding of the given package to the given member subscriptions, copying the stock of the given parent subscription if requested.
     * The method uses native SQL for copying the issue entitlements, (eventual) coverages and price items
     * @param subscription the parent {@link Subscription} whose holding serves as base
     * @param memberSubs the {@link List} of member {@link Subscription}s which should be linked to the given package
     * @param pkg the {@link de.laser.Package} to be linked
     * @param createEntitlements should {@link IssueEntitlement}s be created along with the linking?
     */
    void addToMemberSubscription(Subscription subscription, List<Subscription> memberSubs, Package pkg, boolean createEntitlements) {
        Sql sql = GlobalService.obtainSqlConnection()
        sql.withBatch('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_freeze_holding) values (0, :pkgId, :subId, false) on conflict on constraint sub_package_unique do nothing') { BatchingPreparedStatementWrapper stmt ->
            memberSubs.each { Subscription memberSub ->
                stmt.addBatch([pkgId: pkg.id, subId: memberSub.id])
            }
        }

        if ( createEntitlements ) {
            //List packageTitles = sql.rows("select * from title_instance_package_platform where tipp_pkg_fk = :pkgId and tipp_status_rv_fk = :current", [pkgId: pkg.id, current: RDStore.TIPP_STATUS_CURRENT.id])
            sql.withBatch('insert into issue_entitlement (ie_version, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_reason, ie_medium_rv_fk, ie_status_rv_fk, ie_accept_status_rv_fk, ie_name, ie_sortname, ie_perpetual_access_by_sub_fk) select ' +
                    '0, now(), now(), (select sub_id from subscription where sub_id = :subId), ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_reason, ie_medium_rv_fk, ie_status_rv_fk, ie_accept_status_rv_fk, ie_name, ie_sortname, (select case sub_has_perpetual_access when true then sub_id else null end from subscription where sub_id = :subId) from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id ' +
                    'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed') { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                }
            }
            sql.withBatch('insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo) select ' +
                    '0, (select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id ' +
                    'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed') { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                }
            }
            sql.withBatch('insert into price_item (version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid, pi_list_currency_rv_fk, pi_list_price) select ' +
                    "0, (select ie_id from issue_entitlement where ie_subscription_fk = :subId and ie_tipp_fk = tipp_id and ie_status_rv_fk = tipp_status_rv_fk), now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_currency_rv_fk, pi_list_price from price_item join issue_entitlement on pi_ie_fk = ie_id join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                    'where tipp_pkg_fk = :pkgId and ie_subscription_fk = :parentId and ie_status_rv_fk != :removed') { BatchingPreparedStatementWrapper stmt ->
                memberSubs.each { Subscription memberSub ->
                    stmt.addBatch([pkgId: pkg.id, subId: memberSub.id, parentId: subscription.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                }
            }
        }
    }

    /**
     * Copy from: {@link #addToSubscription(de.laser.Subscription, de.laser.Package, boolean)}
     * Adds the consortial title holding to the given member subscription and links the given package to the member
     * @param target the member subscription whose holding should be enriched
     * @param consortia the consortial subscription whose holding should be taken
     * @param pkg the package to be linked
     */
    void addToSubscriptionCurrentStock(Subscription target, Subscription consortia, Package pkg) {
        Sql sql = GlobalService.obtainSqlConnection()
        sql.executeInsert('insert into subscription_package (sp_version, sp_pkg_fk, sp_sub_fk, sp_freeze_holding) values (0, :pkgId, :subId, false) on conflict on constraint sub_package_unique do nothing', [pkgId: pkg.id, subId: target.id])
        //List consortiumHolding = sql.rows("select * from title_instance_package_platform join issue_entitlement on tipp_id = ie_tipp_fk where tipp_pkg_fk = :pkgId and ie_subscription_fk = :consortium and ie_status_rv_fk = :current", [pkgId: pkg.id, consortium: consortia.id, current: RDStore.TIPP_STATUS_CURRENT.id])
        packageService.bulkAddHolding(sql, target.id, pkg.id, target.hasPerpetualAccess)
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
                        accessEndDate: ie.tipp.accessEndDate,
                        acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED
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
            subscriptionPackage.freezeHolding = params.freezeHolding == 'on'
            if(params.freezeHoldingAudit == 'on' && !AuditConfig.getConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING))
                AuditConfig.addConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING)
            else if(params.freezeHoldingAudit != 'on' && AuditConfig.getConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING))
                AuditConfig.removeConfig(subscriptionPackage.subscription, SubscriptionPackage.FREEZE_HOLDING)
        }
    }

    /**
     * Builds the comparison map for the properties; inverting the relation subscription-properties to property-subscriptions
     * @param subsToCompare the subscriptions whose property sets should be compared
     * @param org the institution whose property definition groups should be considered
     * @return the inverse property map
     */
    Map regroupSubscriptionProperties(List<Subscription> subsToCompare, Org org) {
        LinkedHashMap result = [groupedProperties:[:],orphanedProperties:[:],privateProperties:[:]]
        subsToCompare.each{ sub ->
            Map allPropDefGroups = sub.getCalculatedPropDefGroups(org)
            allPropDefGroups.entrySet().each { propDefGroupWrapper ->
                //group group level
                //There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
                String wrapperKey = propDefGroupWrapper.getKey()
                if(wrapperKey.equals("orphanedProperties")) {
                    TreeMap orphanedProperties = result.orphanedProperties
                    orphanedProperties = comparisonService.buildComparisonTree(orphanedProperties,sub,propDefGroupWrapper.getValue())
                    result.orphanedProperties = orphanedProperties
                }
                else {
                    LinkedHashMap groupedProperties = result.groupedProperties
                    //group level
                    //Each group may have different property groups
                    propDefGroupWrapper.getValue().each { propDefGroup ->
                        PropertyDefinitionGroup groupKey
                        PropertyDefinitionGroupBinding groupBinding
                        switch(wrapperKey) {
                            case "global":
                                groupKey = (PropertyDefinitionGroup) propDefGroup
                                if(groupKey.isVisible)
                                    groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,null,sub))
                                break
                            case "local":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if(groupBinding.isVisible) {
                                        groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,sub))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                            case "member":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if(groupBinding.isVisible && groupBinding.isVisibleForConsortiaMembers) {
                                        groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,sub))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                        }
                    }
                    result.groupedProperties = groupedProperties
                }
            }
            TreeMap privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties,sub,sub.propertySet.findAll { it.type.tenant?.id == org.id })
            result.privateProperties = privateProperties
        }
        result
    }

    /**
     * Substitution call for {@link #addEntitlement(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, boolean)}
     * @param sub the subscription to which the title should be added
     * @param gokbId the we:kb ID of the title
     * @param issueEntitlementOverwrite eventually cached imported local data
     * @param withPriceData should price data be added as well?
     * @param acceptStatus the accept status of the issue entitlement
     * @return the result of {@link #addEntitlement(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, boolean)}
     */
    boolean addEntitlement(sub, gokbId, issueEntitlementOverwrite, withPriceData, acceptStatus){
        addEntitlement(sub, gokbId, issueEntitlementOverwrite, withPriceData, acceptStatus, false)
    }

    /**
     * Substitution call for {@link #addEntitlement(java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, boolean)}
     * @param sub the subscription to which the title should be added
     * @param gokbId the we:kb ID of the title
     * @param issueEntitlementOverwrite eventually cached imported local data
     * @param withPriceData should price data be added as well?
     * @param acceptStatus the accept status of the issue entitlement
     * @return true if the adding was successful, false otherwise
     */
    boolean addEntitlement(sub, gokbId, issueEntitlementOverwrite, withPriceData, acceptStatus, pickAndChoosePerpetualAccess) throws EntitlementCreationException {
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            throw new EntitlementCreationException("Unable to tipp ${gokbId}")
        }
        else if(IssueEntitlement.findAllBySubscriptionAndTippAndStatusInList(sub, tipp, [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_RETIRED])) {
            throw new EntitlementCreationException("Unable to create IssueEntitlement because IssueEntitlement exist with tipp ${gokbId}")
        }
        else if(IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)) {
            IssueEntitlement expected = IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)
            expected.status = RDStore.TIPP_STATUS_CURRENT
            if(!expected.save())
                throw new EntitlementCreationException(expected.errors.getAllErrors().toListString())
        }
        else {
            IssueEntitlement new_ie = new IssueEntitlement(
					status: tipp.status,
                    subscription: sub,
                    tipp: tipp,
                    name: tipp.name,
                    medium: tipp.medium,
                    ieReason: 'Manually Added by User',
                    acceptStatus: acceptStatus)
            new_ie.generateSortTitle()

            //fix for renewEntitlementsWithSurvey, overwrite TIPP status if holding's status differ
            IssueEntitlement parentIE = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(sub.instanceOf, tipp, RDStore.TIPP_STATUS_REMOVED)
            if(parentIE)
                new_ie.status = parentIE.status

            if(pickAndChoosePerpetualAccess || sub.hasPerpetualAccess){
                new_ie.perpetualAccessBySub = sub
            }

            Date accessStartDate, accessEndDate
            if(issueEntitlementOverwrite) {
                if(issueEntitlementOverwrite.accessStartDate) {
                    if(issueEntitlementOverwrite.accessStartDate instanceof String)
                        accessStartDate = DateUtils.parseDateGeneric(issueEntitlementOverwrite.accessStartDate)
                    else if(issueEntitlementOverwrite.accessStartDate instanceof Date)
                        accessStartDate = issueEntitlementOverwrite.accessStartDate
                    else accessStartDate = tipp.accessStartDate
                }
                else accessStartDate = tipp.accessStartDate
                if(issueEntitlementOverwrite.accessEndDate) {
                    if(issueEntitlementOverwrite.accessEndDate instanceof String) {
                        accessEndDate = DateUtils.parseDateGeneric(issueEntitlementOverwrite.accessEndDate)
                    }
                    else if(issueEntitlementOverwrite.accessEndDate instanceof Date) {
                        accessEndDate = issueEntitlementOverwrite.accessEndDate
                    }
                    else accessEndDate = tipp.accessEndDate
                }
                else accessEndDate = tipp.accessEndDate
            }
            else {
                accessStartDate = tipp.accessStartDate
                accessEndDate = tipp.accessEndDate
            }
            new_ie.accessStartDate = accessStartDate
            new_ie.accessEndDate = accessEndDate
            if (new_ie.save()) {
                Set coverageStatements
                Set fallback = tipp.coverages
                if(issueEntitlementOverwrite?.coverages) {
                    coverageStatements = issueEntitlementOverwrite.coverages
                }
                else {
                    coverageStatements = fallback
                }
                coverageStatements.eachWithIndex { covStmt, int c ->
                    IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                            startDate: covStmt.startDate ?: fallback[c]?.startDate,
                            startVolume: covStmt.startVolume ?: fallback[c]?.startVolume,
                            startIssue: covStmt.startIssue ?: fallback[c]?.startIssue,
                            endDate: covStmt.endDate ?: fallback[c]?.endDate,
                            endVolume: covStmt.endVolume ?: fallback[c]?.endVolume,
                            endIssue: covStmt.endIssue ?: fallback[c]?.endIssue,
                            coverageDepth: covStmt.coverageDepth ?: fallback[c]?.coverageDepth,
                            coverageNote: covStmt.coverageNote ?: fallback[c]?.coverageNote,
                            embargo: covStmt.embargo ?: fallback[c]?.embargo,
                            issueEntitlement: new_ie
                    )
                    if(!ieCoverage.save()) {
                        throw new EntitlementCreationException(ieCoverage.errors)
                    }
                }
                if(withPriceData) {
                    if(issueEntitlementOverwrite) {
                        if(issueEntitlementOverwrite instanceof IssueEntitlement) {
                            issueEntitlementOverwrite.priceItems.each { PriceItem priceItem ->
                                PriceItem pi = new PriceItem(
                                        startDate: priceItem.startDate ?: null,
                                        endDate: priceItem.endDate ?: null,
                                        listPrice: priceItem.listPrice ?: null,
                                        listCurrency: priceItem.listCurrency ?: null,
                                        localPrice: priceItem.localPrice ?: null,
                                        localCurrency: priceItem.localCurrency ?: null,
                                        issueEntitlement: new_ie
                                )
                                pi.setGlobalUID()
                                if (!pi.save()) {
                                    throw new EntitlementCreationException(pi.errors)
                                }
                            }

                        }
                        else {
                            PriceItem pi = new PriceItem(startDate: DateUtils.parseDateGeneric(issueEntitlementOverwrite.startDate),
                                    listPrice: issueEntitlementOverwrite.listPrice,
                                    listCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.listCurrency, 'Currency'),
                                    localPrice: issueEntitlementOverwrite.localPrice,
                                    localCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.localCurrency, 'Currency'),
                                    issueEntitlement: new_ie
                            )
                            pi.setGlobalUID()
                            if(pi.save())
                                return true
                            else {
                                throw new EntitlementCreationException(pi.errors)
                            }
                        }
                    }
                    else {
                        tipp.priceItems.each { PriceItem priceItem ->
                            PriceItem pi = new PriceItem(
                                    startDate: priceItem.startDate ?: null,
                                    endDate: priceItem.endDate ?: null,
                                    listPrice: priceItem.listPrice ?: null,
                                    listCurrency: priceItem.listCurrency ?: null,
                                    localPrice: priceItem.localPrice ?: null,
                                    localCurrency: priceItem.localCurrency ?: null,
                                    issueEntitlement: new_ie
                            )
                            pi.setGlobalUID()
                            if (!pi.save()) {
                                throw new EntitlementCreationException(pi.errors)
                            }
                        }
                    }
                }
                else return true
            } else {
                throw new EntitlementCreationException(new_ie.errors)
            }
        }
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
    boolean deleteEntitlementbyID(sub, id) {
        IssueEntitlement ie = IssueEntitlement.findWhere(id: Long.parseLong(id), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
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
     * Adds the given set of issue entitlements to the given subscription. The entitlement data may come directly from the package or be overwritten by individually negotiated content
     * @param sub the {@link Subscription} to which the entitlements should be attached
     * @param issueEntitlementOverwrites the {@link Map} containing the data to submit for each issue entitlement
     * @param checkMap the {@link Map} containing identifiers of titles which have been selected for the enrichment
     * @param withPriceData should price data be added as well?
     * @param acceptStatus the accept status reference data key
     * @param pickAndChoosePerpetualAccess are the given titles purchased perpetually?
     */
    void bulkAddEntitlements(Subscription sub, Map<String, Object> issueEntitlementOverwrites, Map<String, String> checkMap, withPriceData, Long acceptStatus, pickAndChoosePerpetualAccess) {
        Sql sql = GlobalService.obtainSqlConnection()
        Object[] keys = checkMap.keySet().toArray()
        Map<String, Object> fallbackMap = [:]
        sql.withTransaction {
            List<GroovyRowResult> accessStartEndDateRows = sql.rows('select tipp_gokb_id, tipp_access_start_date, tipp_access_end_date from title_instance_package_platform where tipp_gokb_id = any(:keys)', [keys: sql.connection.createArrayOf('varchar', keys)])
            accessStartEndDateRows.each { GroovyRowResult row ->
                fallbackMap.put(row['tipp_gokb_id'], [accessStartDate: row['tipp_access_start_date'], accessEndDate: row['tipp_access_end_date']])
            }
            List<GroovyRowResult> coverageRows = sql.rows('select tipp_gokb_id, tc_start_date, tc_start_issue, tc_start_volume, tc_embargo, tc_coverage_note, tc_coverage_depth, tc_end_date, tc_end_issue, tc_end_volume from tippcoverage join title_instance_package_platform on tc_tipp_fk = tipp_id where tipp_gokb_id = any(:keys)', [keys: sql.connection.createArrayOf('varchar', keys)])
            coverageRows.each { GroovyRowResult row ->
                Map fallbackRec = fallbackMap.get(row['tipp_gokb_id'])
                if(!fallbackRec)
                    fallbackRec = [:]
                Set<Map> coverages = fallbackRec.coverages
                if(!coverages)
                    coverages = []
                Map<String, Object> covStmt = [:]
                covStmt.startDate = row['tc_start_date']
                covStmt.startIssue = row['tc_start_issue']
                covStmt.startVolume = row['tc_start_volume']
                covStmt.endDate = row['tc_end_date']
                covStmt.endIssue = row['tc_end_issue']
                covStmt.endVolume = row['tc_end_volume']
                covStmt.coverageNote = row['tc_coverage_note']
                covStmt.coverageDepth = row['tc_coverage_depth']
                covStmt.embargo = row['tc_embargo']
                coverages << covStmt
                fallbackRec.coverages = coverages
                fallbackMap.put(row['tipp_gokb_id'], fallbackRec)
            }
            List<GroovyRowResult> priceRows = sql.rows('select tipp_gokb_id, pi_list_price, pi_list_currency_rv_fk from price_item join title_instance_package_platform on pi_tipp_fk = tipp_id where tipp_gokb_id = any(:keys)', [keys: sql.connection.createArrayOf('varchar', keys)])
            priceRows.each { GroovyRowResult row ->
                Map fallbackRec = fallbackMap.get(row['tipp_gokb_id'])
                if(!fallbackRec)
                    fallbackRec = [:]
                Set<Map> priceItems = fallbackRec.priceItems
                if(!priceItems)
                    priceItems = []
                Map<String, Object> piStmt = [:]
                piStmt.listPrice = row['pi_list_price']
                piStmt.listCurrency = row['pi_list_currency_rv_fk']
                piStmt.localPrice = null
                piStmt.localCurrency = null
                priceItems << piStmt
                fallbackRec.priceItems = priceItems
                fallbackMap.put(row['tipp_gokb_id'], fallbackRec)
            }
            sql.executeUpdate('update issue_entitlement set ie_status_rv_fk = :current from title_instance_package_platform where ie_tipp_fk = tipp_id and ie_status_rv_fk = :expected and tipp_gokb_id = any(:keys) and ie_subscription_fk = :subId', [current: RDStore.TIPP_STATUS_CURRENT.id, expected: RDStore.TIPP_STATUS_EXPECTED.id, keys: sql.connection.createArrayOf('varchar', keys), subId: sub.id])
            Set<Map<String, Object>> ieDirectMapSet = [], ieOverwriteMapSet = [], coverageDirectMapSet = [], coverageOverwriteMapSet = [], priceItemDirectSet = [], priceItemOverwriteSet = []
            checkMap.each { String wekbId, String checked ->
                if(checked == 'checked') {
                    println "processing ${wekbId}"
                    List<GroovyRowResult> existingEntitlements = sql.rows('select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId and ie_status_rv_fk = :current and tipp_gokb_id = :key',[subId: sub.id, current: RDStore.TIPP_STATUS_CURRENT.id, key: wekbId])
                    if(existingEntitlements.size() == 0) {
                        Map<String, Object> configMap = [wekbId: wekbId, subId: sub.id, acceptStatus: acceptStatus, removed: RDStore.TIPP_STATUS_REMOVED.id], coverageMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id], priceMap = [wekbId: wekbId, subId: sub.id, removed: RDStore.TIPP_STATUS_REMOVED.id]
                        if(pickAndChoosePerpetualAccess || sub.hasPerpetualAccess){
                            configMap.perpetualAccessBySub = sub.id
                        }
                        Map overwrite = (Map) issueEntitlementOverwrites.get(wekbId)
                        if(overwrite) {
                            if(overwrite.accessStartDate) {
                                configMap.accessStartDate = overwrite.accessStartDate
                            }
                            else configMap.accessStartDate = fallbackMap.get(wekbId)?.accessStartDate
                            if(overwrite.accessEndDate) {
                                configMap.accessEndDate = overwrite.accessEndDate
                            }
                            else configMap.accessEndDate = fallbackMap.get(wekbId)?.accessEndDate
                            List coverages
                            if(overwrite.coverages) {
                                overwrite.coverages.each { Map covStmt ->
                                    coverageMap.putAll(covStmt)
                                    coverageOverwriteMapSet << coverageMap
                                }
                            }
                            else if(fallbackMap.get(wekbId)?.coverages) {
                                fallbackMap.get(wekbId).coverages.each { Map covStmt ->
                                    coverageMap.putAll(covStmt)
                                    coverageOverwriteMapSet << coverageMap
                                }
                            }
                            if(withPriceData) {
                                if(overwrite.listPrice || overwrite.localPrice) {
                                    priceMap.listPrice = overwrite.listPrice
                                    priceMap.listCurrency = overwrite.listCurrency ? RefdataValue.getByValueAndCategory(overwrite.listCurrency, RDConstants.CURRENCY)?.id : null
                                    priceMap.localPrice = overwrite.localPrice
                                    priceMap.localCurrency = overwrite.localCurrency ? RefdataValue.getByValueAndCategory(overwrite.localCurrency, RDConstants.CURRENCY)?.id : null
                                    priceItemOverwriteSet << priceMap
                                }
                                else if(fallbackMap.get(wekbId)?.priceItems) {
                                    fallbackMap.get(wekbId).priceItems.each { Map priceStmt ->
                                        priceMap.putAll(priceStmt)
                                        priceItemOverwriteSet << priceMap
                                    }
                                }
                            }
                            ieOverwriteMapSet << configMap
                        }
                        else {
                            ieDirectMapSet << configMap
                            coverageDirectMapSet << coverageMap
                            if(withPriceData)
                                priceItemDirectSet << priceMap
                        }
                    }
                }
            }
            ieOverwriteMapSet.each { Map<String, Object> configMap ->
                String accessStartDate = null, accessEndDate = null
                if(configMap.accessStartDate)
                    accessStartDate = "'${ configMap.accessStartDate }'"
                if(configMap.accessEndDate)
                    accessEndDate = "'${ configMap.accessEndDate }'"
                sql.withBatch("insert into issue_entitlement (ie_version, ie_subscription_fk, ie_tipp_fk, ie_name, ie_sortname, ie_medium_rv_fk, ie_status_rv_fk, ie_reason, ie_accept_status_rv_fk, ie_access_start_date, ie_access_end_date, ie_perpetual_access_by_sub_fk) " +
                        "select 0, ${sub.id}, tipp_id, tipp_name, tipp_sort_name, tipp_medium_rv_fk, tipp_status_rv_fk, 'Manually Added by User', ${configMap.acceptStatus}, ${accessStartDate}, ${accessEndDate}, ${configMap.perpetualAccessBySub} from title_instance_package_platform where tipp_gokb_id = :wekbId") { BatchingStatementWrapper stmt ->
                    stmt.addBatch([wekbId: configMap.wekbId])
                }
            }
            coverageOverwriteMapSet.each { Map<String, Object> configMap ->
                if(configMap.startDate == '')
                    configMap.startDate = null
                else if(configMap.startDate)
                    configMap.startDate = new Timestamp(DateUtils.parseDateGeneric(configMap.startDate).getTime())
                if(configMap.endDate == '')
                    configMap.endDate = null
                else if(configMap.startDate)
                    configMap.startDate = new Timestamp(DateUtils.parseDateGeneric(configMap.endDate).getTime())
                sql.withBatch("insert into issue_entitlement_coverage (ic_version, ic_start_date, ic_start_issue, ic_start_volume, ic_end_date, ic_end_issue, ic_end_volume, ic_coverage_depth, ic_coverage_note, ic_embargo, ic_ie_fk) " +
                        "values (0, :startDate, :startIssue, :startVolume, :endDate, :endIssue, :endVolume, :coverageDepth, :coverageNote, :embargo, (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed))") { BatchingStatementWrapper stmt ->
                    stmt.addBatch(configMap)
                }
            }
            priceItemOverwriteSet.each { Map<String, Object> configMap ->
                sql.withBatch("insert into price_item (version, pi_guid, pi_list_price, pi_list_currency_rv_fk, pi_local_price, pi_local_currency_rv_fk, pi_ie_fk) " +
                        "values (0, concat('priceitem:',gen_random_uuid()), :listPrice, :listCurrency, :localPrice, :localCurrency, (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed))") { BatchingStatementWrapper stmt ->
                    stmt.addBatch(configMap)
                }
            }
            ieDirectMapSet.each { Map<String, Object> configMap ->
                sql.withBatch("insert into issue_entitlement (ie_version, ie_subscription_fk, ie_tipp_fk, ie_name, ie_sortname, ie_medium_rv_fk, ie_status_rv_fk, ie_reason, ie_accept_status_rv_fk, ie_access_start_date, ie_access_end_date, ie_perpetual_access_by_sub_fk) " +
                        "select 0, ${sub.id}, tipp_id, tipp_name, tipp_sort_name, tipp_medium_rv_fk, tipp_status_rv_fk, 'Manually Added by User', ${configMap.acceptStatus}, tipp_access_start_date, tipp_access_end_date, ${configMap.perpetualAccessBySub} from title_instance_package_platform where tipp_gokb_id = :wekbId") { BatchingStatementWrapper stmt ->
                    stmt.addBatch([wekbId: configMap.wekbId])
                }
            }
            coverageDirectMapSet.each { Map<String, Object> configMap ->
                sql.withBatch("insert into issue_entitlement_coverage (ic_version, ic_start_date, ic_start_issue, ic_start_volume, ic_end_date, ic_end_issue, ic_end_volume, ic_coverage_depth, ic_coverage_note, ic_embargo, ic_ie_fk) " +
                        "select 0, tc_start_date, tc_start_issue, tc_start_volume, tc_end_date, tc_end_issue, tc_end_volume, tc_coverage_depth, tc_coverage_note, tc_embargo, ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join tippcoverage on tc_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed") { BatchingStatementWrapper stmt ->
                    stmt.addBatch(configMap)
                }
            }
            priceItemDirectSet.each { Map<String, Object> configMap ->
                sql.withBatch("insert into price_item (version, pi_guid, pi_list_price, pi_list_currency_rv_fk, pi_ie_fk) " +
                        "select 0, concat('priceitem:',gen_random_uuid()), pi_list_price, pi_list_currency_rv_fk, ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join price_item on pi_tipp_fk = tipp_id where ie_subscription_fk = :subId and tipp_gokb_id = :wekbId and ie_status_rv_fk != :removed") { BatchingStatementWrapper stmt ->
                    stmt.addBatch(configMap)
                }
            }
        }

        /*
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            throw new EntitlementCreationException("Unable to tipp ${gokbId}")
        }
        else if(IssueEntitlement.findAllBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_CURRENT)) {
            throw new EntitlementCreationException("Unable to create IssueEntitlement because IssueEntitlement exist with tipp ${gokbId}")
        }
        else if(IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)) {
            IssueEntitlement expected = IssueEntitlement.findBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_EXPECTED)
            expected.status = RDStore.TIPP_STATUS_CURRENT
            if(!expected.save())
                throw new EntitlementCreationException(expected.errors.getAllErrors().toListString())
        }
        else {
            IssueEntitlement new_ie = new IssueEntitlement(
                    status: tipp.status,
                    subscription: sub,
                    tipp: tipp,
                    name: tipp.name,
                    medium: tipp.medium,
                    ieReason: 'Manually Added by User',
                    acceptStatus: acceptStatus)
            new_ie.generateSortTitle()

            if(pickAndChoosePerpetualAccess || sub.hasPerpetualAccess){
                new_ie.perpetualAccessBySub = sub
            }

            Date accessStartDate, accessEndDate
            if(issueEntitlementOverwrite) {
                if(issueEntitlementOverwrite.accessStartDate) {
                    if(issueEntitlementOverwrite.accessStartDate instanceof String)
                        accessStartDate = DateUtils.parseDateGeneric(issueEntitlementOverwrite.accessStartDate)
                    else if(issueEntitlementOverwrite.accessStartDate instanceof Date)
                        accessStartDate = issueEntitlementOverwrite.accessStartDate
                    else accessStartDate = tipp.accessStartDate
                }
                else accessStartDate = tipp.accessStartDate
                if(issueEntitlementOverwrite.accessEndDate) {
                    if(issueEntitlementOverwrite.accessEndDate instanceof String) {
                        accessEndDate = DateUtils.parseDateGeneric(issueEntitlementOverwrite.accessEndDate)
                    }
                    else if(issueEntitlementOverwrite.accessEndDate instanceof Date) {
                        accessEndDate = issueEntitlementOverwrite.accessEndDate
                    }
                    else accessEndDate = tipp.accessEndDate
                }
                else accessEndDate = tipp.accessEndDate
            }
            else {
                accessStartDate = tipp.accessStartDate
                accessEndDate = tipp.accessEndDate
            }
            new_ie.accessStartDate = accessStartDate
            new_ie.accessEndDate = accessEndDate
            if (new_ie.save()) {
                Set coverageStatements
                Set fallback = tipp.coverages
                if(issueEntitlementOverwrite?.coverages) {
                    coverageStatements = issueEntitlementOverwrite.coverages
                }
                else {
                    coverageStatements = fallback
                }
                coverageStatements.eachWithIndex { covStmt, int c ->
                    IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                            startDate: covStmt.startDate ?: fallback[c]?.startDate,
                            startVolume: covStmt.startVolume ?: fallback[c]?.startVolume,
                            startIssue: covStmt.startIssue ?: fallback[c]?.startIssue,
                            endDate: covStmt.endDate ?: fallback[c]?.endDate,
                            endVolume: covStmt.endVolume ?: fallback[c]?.endVolume,
                            endIssue: covStmt.endIssue ?: fallback[c]?.endIssue,
                            coverageDepth: covStmt.coverageDepth ?: fallback[c]?.coverageDepth,
                            coverageNote: covStmt.coverageNote ?: fallback[c]?.coverageNote,
                            embargo: covStmt.embargo ?: fallback[c]?.embargo,
                            issueEntitlement: new_ie
                    )
                    if(!ieCoverage.save()) {
                        throw new EntitlementCreationException(ieCoverage.errors)
                    }
                }
                if(withPriceData) {
                    if(issueEntitlementOverwrite) {
                        if(issueEntitlementOverwrite instanceof IssueEntitlement) {
                            issueEntitlementOverwrite.priceItems.each { PriceItem priceItem ->
                                PriceItem pi = new PriceItem(
                                        startDate: priceItem.startDate ?: null,
                                        endDate: priceItem.endDate ?: null,
                                        listPrice: priceItem.listPrice ?: null,
                                        listCurrency: priceItem.listCurrency ?: null,
                                        localPrice: priceItem.localPrice ?: null,
                                        localCurrency: priceItem.localCurrency ?: null,
                                        issueEntitlement: new_ie
                                )
                                pi.setGlobalUID()
                                if (pi.save())
                                    return true
                                else {
                                    throw new EntitlementCreationException(pi.errors)
                                }
                            }

                        }
                        else {
                            PriceItem pi = new PriceItem(startDate: DateUtils.parseDateGeneric(issueEntitlementOverwrite.startDate),
                                    listPrice: issueEntitlementOverwrite.listPrice,
                                    listCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.listCurrency, 'Currency'),
                                    localPrice: issueEntitlementOverwrite.localPrice,
                                    localCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.localCurrency, 'Currency'),
                                    issueEntitlement: new_ie
                            )
                            pi.setGlobalUID()
                            if(pi.save())
                                return true
                            else {
                                throw new EntitlementCreationException(pi.errors)
                            }
                        }
                    }
                    else {
                        tipp.priceItems.each { PriceItem priceItem ->
                            PriceItem pi = new PriceItem(
                                    startDate: priceItem.startDate ?: null,
                                    endDate: priceItem.endDate ?: null,
                                    listPrice: priceItem.listPrice ?: null,
                                    listCurrency: priceItem.listCurrency ?: null,
                                    localPrice: priceItem.localPrice ?: null,
                                    localCurrency: priceItem.localCurrency ?: null,
                                    issueEntitlement: new_ie
                            )
                            pi.setGlobalUID()
                            if (pi.save())
                                return true
                            else {
                                throw new EntitlementCreationException(pi.errors)
                            }
                        }
                    }
                }
                else return true
            } else {
                throw new EntitlementCreationException(new_ie.errors)
            }
        }
        */
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
            println 'error while rebasing TIPP ... rollback!'
            println e.message
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
        return ((subscription.getConsortia()?.id == contextOrg.id) && subscription._getCalculatedType() in
                [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE])
    }

    boolean areStatsAvailable(Collection<Platform> subscribedPlatforms, Collection<Long> refPkgs, Collection<Long> reportInstitutions) {
        Map<String, Object> checkParams = [max: 1, plat: subscribedPlatforms, refPkgs: refPkgs, reportInstitutions: reportInstitutions]
        int result = Counter4Report.executeQuery('select c4r.id from Counter4Report c4r join c4r.title tipp where c4r.platform in (:plat) and tipp.pkg.id in (:refPkgs) and c4r.reportInstitution.id in (:reportInstitutions)', checkParams).size()
        if(result == 0)
            result = Counter5Report.executeQuery('select c5r.id from Counter5Report c5r join c5r.title tipp where c5r.platform in (:plat) and tipp.pkg.id in (:refPkgs) and c5r.reportInstitution.id in (:reportInstitutions)', checkParams).size()
        result > 0
    }

    /**
     * (Un-)links the given subscription to the given license
     * @param sub the subscription to be linked
     * @param newLicense the license to link
     * @param unlink should the link being created or dissolved?
     * @return true if the (un-)linking was successful, false otherwise
     */
    boolean setOrgLicRole(Subscription sub, License newLicense, boolean unlink) {
        boolean success = false
        Links curLink = Links.findBySourceLicenseAndDestinationSubscriptionAndLinkType(newLicense,sub,RDStore.LINKTYPE_LICENSE)
        Org subscr = sub.getSubscriber()
        if(!unlink && !curLink) {
            try {
                if(Links.construct([source: newLicense, destination: sub, linkType: RDStore.LINKTYPE_LICENSE, owner: contextService.getOrg()])) {
                    RefdataValue licRole
                    switch(sub._getCalculatedType()) {
                        case CalculatedType.TYPE_PARTICIPATION: licRole = RDStore.OR_LICENSEE_CONS
                            break
                        case CalculatedType.TYPE_LOCAL: licRole = RDStore.OR_LICENSEE
                            break
                        case CalculatedType.TYPE_CONSORTIAL:
                        case CalculatedType.TYPE_ADMINISTRATIVE: licRole = RDStore.OR_LICENSING_CONSORTIUM
                            break
                        default: log.error("no role type determinable!")
                            break
                    }

                    if(licRole) {
                        OrgRole orgLicRole = OrgRole.findByLicAndOrgAndRoleType(newLicense,subscr,licRole)
                        if(!orgLicRole){
                            orgLicRole = OrgRole.findByLicAndOrgAndRoleType(newLicense,subscr,licRole)
                            if(orgLicRole && orgLicRole.lic != newLicense) {
                                orgLicRole.lic = newLicense
                            }
                            else {
                                orgLicRole = new OrgRole(lic: newLicense,org: subscr,roleType: licRole)
                            }
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
    Map subscriptionImport(MultipartFile tsvFile) {
        Locale locale = LocaleContextHolder.getLocale()
        Org contextOrg = contextService.getOrg()
        RefdataValue comboType
        String[] parentSubType
        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            comboType = RDStore.COMBO_TYPE_CONSORTIUM
            parentSubType = [RDStore.SUBSCRIPTION_KIND_CONSORTIAL.getI10n('value')]
        }
        Map colMap = [:]
        Map<String, Map> propMap = [:]
        Map candidates = [:]
        InputStream fileContent = tsvFile.getInputStream()
        List<String> rows = fileContent.text.split('\n')
        List<String> ignoredColHeads = [], multiplePropDefs = []
        rows[0].split('\t').eachWithIndex { String s, int c ->
            String headerCol = s.trim()
            if(headerCol.startsWith("\uFEFF"))
                headerCol = headerCol.substring(1)
            //important: when processing column headers, grab those which are reserved; default case: check if it is a name of a property definition; if there is no result as well, reject.
            switch(headerCol.toLowerCase()) {
                case "name": colMap.name = c
                    break
                case "member":
                case "teilnehmer": colMap.member = c
                    break
                case "vertrag":
                case "license": colMap.licenses = c
                    break
                case "elternlizenz":
                case "konsortiallizenz":
                case "parent subscription":
                case "consortial subscription":
                    if(accessService.checkPerm("ORG_CONSORTIUM"))
                        colMap.instanceOf = c
                    break
                case "status": colMap.status = c
                    break
                case "startdatum":
                case "laufzeit-beginn":
                case "start date": colMap.startDate = c
                    break
                case "enddatum":
                case "laufzeit-ende":
                case "end date": colMap.endDate = c
                    break
                case "kündigungsdatum":
                case "cancellation date":
                case "manual cancellation date": colMap.manualCancellationDate = c
                    break
                case "lizenztyp":
                case "subscription type":
                case "type": colMap.kind = c
                    break
                case "lizenzform":
                case "subscription form":
                case "form": colMap.form = c
                    break
                case "ressourcentyp":
                case "subscription resource":
                case "resource": colMap.resource = c
                    break
                case "anbieter":
                case "provider:": colMap.provider = c
                    break
                case "lieferant":
                case "agency": colMap.agency = c
                    break
                case "anmerkungen":
                case "notes": colMap.notes = c
                    break
                case "perpetual access":
                case "dauerhafter zugriff": colMap.hasPerpetualAccess = c
                    break
                case "publish component":
                case "publish-komponente": colMap.hasPublishComponent = c
                    break
                case "data exchange release":
                case "freigabe datenaustausch": colMap.isPublicForApi = c
                    break
                default:
                    //check if property definition
                    boolean isNotesCol = false
                    String propDefString = headerCol.toLowerCase()
                    if(headerCol.contains('$$notes')) {
                        isNotesCol = true
                        propDefString = headerCol.split('\\$\\$')[0].toLowerCase()
                    }
                    Map queryParams = [propDef:propDefString,contextOrg:contextOrg,subProp:PropertyDefinition.SUB_PROP]
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
                            propMap[propDef.class.name+':'+propDef.id] = [definition:defPair]
                        }
                    }
                    else if(possiblePropDefs.size() > 1)
                        multiplePropDefs << headerCol
                    else
                        ignoredColHeads << headerCol
                    break
            }
        }
        Set<String> globalErrors = []
        if(ignoredColHeads)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.colHeaderIgnored',[ignoredColHeads.join('</li><li>')].toArray(),locale)
        if(multiplePropDefs)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.multiplePropDefs',[multiplePropDefs.join('</li><li>')].toArray(),locale)
        rows.remove(0)
        rows.each { row ->
            Map mappingErrorBag = [:], candidate = [properties: [:]]
            List<String> cols = row.split('\t')
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
                        List<License> licCandidates = License.executeQuery("select oo.lic from OrgRole oo join oo.lic l where :idCandidate in (cast(l.id as string),l.globalUID) and oo.roleType in :roleTypes and oo.org = :contextOrg", [idCandidate: licenseKey.trim(), roleTypes: [RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE], contextOrg: contextOrg])
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
            //provider
            if(colMap.provider != null) {
                String providerIdCandidate = cols[colMap.provider]?.trim()
                if(providerIdCandidate) {
                    Long idCandidate = providerIdCandidate.isLong() ? Long.parseLong(providerIdCandidate) : null
                    Org provider = Org.findByIdOrGlobalUID(idCandidate,providerIdCandidate)
                    if(provider)
                        candidate.provider = "${provider.class.name}:${provider.id}"
                    else {
                        mappingErrorBag.noValidOrg = providerIdCandidate
                    }
                }
            }
            //agency
            if(colMap.agency != null) {
                String agencyIdCandidate = cols[colMap.agency]?.trim()
                if(agencyIdCandidate) {
                    Long idCandidate = agencyIdCandidate.isLong() ? Long.parseLong(agencyIdCandidate) : null
                    Org agency = Org.findByIdOrGlobalUID(idCandidate,agencyIdCandidate)
                    if(agency)
                        candidate.agency = "${agency.class.name}:${agency.id}"
                    else {
                        mappingErrorBag.noValidOrg = agencyIdCandidate
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
                Date manualCancellationDate = DateUtils.parseDateGeneric(cols[colMap.manualCancellationDate])
                if(manualCancellationDate)
                    candidate.manualCancellationDate = manualCancellationDate
            }
            //instanceOf(nullable:true, blank:false)
            if(colMap.instanceOf != null && colMap.member != null) {
                String idCandidate = cols[colMap.instanceOf].trim()
                String memberIdCandidate = cols[colMap.member].trim()
                if(idCandidate && memberIdCandidate) {
                    List<Subscription> parentSubs = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :contextOrg and oo.roleType in :roleTypes and :idCandidate in (cast(oo.sub.id as string),oo.sub.globalUID)",[contextOrg: contextOrg, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA], idCandidate: idCandidate])
                    // TODO [ticket=1789]
                    //  List<Org> possibleOrgs = Org.executeQuery("select distinct idOcc.org from IdentifierOccurrence idOcc, Combo c join idOcc.identifier id where c.fromOrg = idOcc.org and :idCandidate in (cast(idOcc.org.id as string),idOcc.org.globalUID) or (id.value = :idCandidate and id.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type",[idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
                    List<Org> possibleOrgs = Org.executeQuery("select distinct ident.org from Identifier ident, Combo c where c.fromOrg = ident.org and :idCandidate in (cast(ident.org.id as string), ident.org.globalUID) or (ident.value = :idCandidate and ident.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type", [idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
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
                        candidate.hasPerpetualAccess = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}" ? true : false)
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
                        candidate.hasPublishComponent = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}" ? true : false)
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
                        candidate.isPublicForApi = (yesNo == "${RDStore.YN_YES.class.name}:${RDStore.YN_YES.id}" ? true : false)
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
                if(cols[defPair.colno]) {
                    def v
                    if(defPair.refCategory) {
                        v = refdataService.retrieveRefdataValueOID(cols[defPair.colno].trim(),defPair.refCategory)
                        if(!v) {
                            mappingErrorBag.propValNotInRefdataValueSet = [cols[defPair.colno].trim(),defPair.refCategory]
                        }
                    }
                    else v = cols[defPair.colno]
                    propData.propValue = v
                }
                if(propInput.notesColno)
                    propData.propNote = cols[propInput.notesColno].trim()
                candidate.properties[k] = propData
            }
            //notes
            if(colMap.notes != null && cols[colMap.notes]?.trim()) {
                candidate.notes = cols[colMap.notes].trim()
            }
            candidates.put(candidate,mappingErrorBag)
        }
        [candidates: candidates, globalErrors: globalErrors, parentSubType: parentSubType]
    }

    /**
     * Processes after having checked the given import and creates the subscription instances with their depending objects
     * @param candidates the candidates to process
     * @param params the import parameter map, containing the flags which subscriptions should be taken and which not
     * @return a list of errors which have occurred, an empty list in case of success
     */
    List addSubscriptions(candidates,GrailsParameterMap params) {
        List errors = []
        Locale locale = LocaleContextHolder.getLocale()
        Org contextOrg = contextService.getOrg()
        SimpleDateFormat databaseDateFormatParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        candidates.eachWithIndex{ entry, int s ->
            if(params["take${s}"]) {
                //create object itself
                Subscription sub = new Subscription(name: entry.name,
                        status: genericOIDService.resolveOID(entry.status),
                        kind: genericOIDService.resolveOID(entry.kind),
                        form: genericOIDService.resolveOID(entry.form),
                        resource: genericOIDService.resolveOID(entry.resource),
                        type: contextOrg.getCustomerType() == "ORG_CONSORTIUM" ? RDStore.SUBSCRIPTION_TYPE_CONSORTIAL : RDStore.SUBSCRIPTION_TYPE_LOCAL,
                        isPublicForApi: entry.isPublicForApi,
                        hasPerpetualAccess: entry.hasPerpetualAccess,
                        hasPublishComponent: entry.hasPublishComponent,
                        identifier: UUID.randomUUID())
                sub.startDate = entry.startDate ? databaseDateFormatParser.parse(entry.startDate) : null
                sub.endDate = entry.endDate ? databaseDateFormatParser.parse(entry.endDate) : null
                sub.manualCancellationDate = entry.manualCancellationDate ? databaseDateFormatParser.parse(entry.manualCancellationDate) : null
                /* TODO [ticket=2276]
                if(sub.type == SUBSCRIPTION_TYPE_ADMINISTRATIVE)
                    sub.administrative = true*/
                sub.instanceOf = entry.instanceOf ? genericOIDService.resolveOID(entry.instanceOf) : null
                Org member = entry.member ? genericOIDService.resolveOID(entry.member) : null
                Org provider = entry.provider ? genericOIDService.resolveOID(entry.provider) : null
                Org agency = entry.agency ? genericOIDService.resolveOID(entry.agency) : null
                if(sub.instanceOf && member)
                    sub.isSlaved = RDStore.YN_YES
                if(sub.save()) {
                    sub.refresh() //needed for dependency processing
                    //create the org role associations
                    RefdataValue parentRoleType, memberRoleType
                    if(accessService.checkPerm("ORG_CONSORTIUM")) {
                        parentRoleType = RDStore.OR_SUBSCRIPTION_CONSORTIA
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
                        OrgRole providerRole = new OrgRole(roleType: RDStore.OR_PROVIDER, sub: sub, org: provider)
                        if(!providerRole.save()) {
                            errors << providerRole.errors
                        }
                    }
                    if(agency) {
                        OrgRole agencyRole = new OrgRole(roleType: RDStore.OR_AGENCY, sub: sub, org: agency)
                        if(!agencyRole.save()) {
                            errors << agencyRole.errors
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
                    if(entry.notes) {
                        Object[] args = [sdf.format(new Date())]
                        Doc docContent = new Doc(contentType: Doc.CONTENT_TYPE_STRING, content: entry.notes, title: messageSource.getMessage('myinst.subscriptionImport.notes.title',args,locale), type: RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE), owner: contextOrg, user: contextService.getUser())
                        if(docContent.save()) {
                            DocContext dc = new DocContext(subscription: sub, owner: docContent, doctype: RDStore.DOC_TYPE_NOTE)
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
    Map issueEntitlementEnrichment(InputStream stream, Set<Long> entIds, Subscription subscription, boolean uploadCoverageDates, boolean uploadPriceInfo) {

        Integer count = 0
        Integer countChangesPrice = 0
        Integer countChangesCoverageDates = 0

        ArrayList<String> rows = stream.text.split('\n')
        Map<String, Integer> colMap = [publicationTitleCol: -1, zdbCol: -1, onlineIdentifierCol: -1, printIdentifierCol: -1, dateFirstInPrintCol: -1, dateFirstOnlineCol: -1,
                                       startDateCol       : -1, startVolumeCol: -1, startIssueCol: -1,
                                       endDateCol         : -1, endVolumeCol: -1, endIssueCol: -1,
                                       accessStartDateCol : -1, accessEndDateCol: -1, coverageDepthCol: -1, coverageNotesCol: -1, embargoCol: -1,
                                       listPriceCol       : -1, listCurrencyCol: -1, listPriceEurCol: -1, listPriceUsdCol: -1, listPriceGbpCol: -1, localPriceCol: -1, localCurrencyCol: -1, priceDateCol: -1]
        //read off first line of KBART file
        rows[0].split('\t').eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
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
        //after having read off the header row, pop the first row
        rows.remove(0)
        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                       eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name), isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                       issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name), pisbn: IdentifierNamespace.findByNsAndNsType('pisbn', TitleInstancePackagePlatform.class.name),
                                                       doi  : IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name)]
        rows.eachWithIndex { row, int i ->
            log.debug("now processing entitlement ${i}")
            ArrayList<String> cols = row.split('\t')
            Map<String, Object> idCandidate
            if (colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.eissn, namespaces.isbn], value: cols[colMap.onlineIdentifierCol]]
            }
            else if (colMap.doiTitleCol >= 0 && cols[colMap.doiTitleCol]) {
                idCandidate = [namespaces: [namespaces.doi], value: cols[colMap.doiTitleCol]]
            }
            else if (colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.issn, namespaces.pisbn], value: cols[colMap.printIdentifierCol]]
            }
            else if (colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                idCandidate = [namespaces: [namespaces.zdb], value: cols[colMap.zdbCol]]
            }
            if (((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                    ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                    ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
            } else {

                List<Long> titleIds = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp join tipp.ids ident where ident.ns in :namespaces and ident.value = :value', [namespaces:idCandidate.namespaces, value:idCandidate.value])
                if (titleIds.size() > 0) {
                    List<IssueEntitlement> issueEntitlements = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.id in (:titleIds) and ie.subscription.id = :subId and ie.status != :ieStatus', [titleIds: titleIds, subId: subscription.id, ieStatus: RDStore.TIPP_STATUS_REMOVED])
                    if (issueEntitlements.size() > 0) {
                        IssueEntitlement issueEntitlement = issueEntitlements[0]
                        IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage()
                        count++
                        PriceItem priceItem = issueEntitlement.priceItems ? issueEntitlement.priceItems[0] : new PriceItem(issueEntitlement: issueEntitlement)
                        colMap.each { String colName, int colNo ->
                            if (colNo > -1 && cols[colNo]) {
                                String cellEntry = cols[colNo].trim()
                                if (uploadCoverageDates) {
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
                                        case "accessStartDateCol": issueEntitlement.accessStartDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : issueEntitlement.accessStartDate
                                            break
                                        case "accessEndDateCol": issueEntitlement.accessEndDate = cellEntry ? DateUtils.parseDateGeneric(cellEntry) : issueEntitlement.accessEndDate
                                            break
                                        case "embargoCol": ieCoverage.embargo = cellEntry ?: null
                                            break
                                        case "coverageDepthCol": ieCoverage.coverageDepth = cellEntry ?: null
                                            break
                                        case "coverageNotesCol": ieCoverage.coverageNote = cellEntry ?: null
                                            break
                                    }
                                }
                                if (uploadPriceInfo) {

                                    try {
                                        switch (colName) {
                                            case "listPriceCol": priceItem.listPrice = cellEntry ? escapeService.parseFinancialValue(cellEntry) : null
                                                break
                                            case "listCurrencyCol": priceItem.listCurrency = cellEntry ? RefdataValue.getByValueAndCategory(cellEntry, RDConstants.CURRENCY) : null
                                                break
                                            case "listPriceEurCol": priceItem.listPrice = cellEntry ? escapeService.parseFinancialValue(cellEntry) : null
                                                priceItem.listCurrency = RefdataValue.getByValueAndCategory("EUR", RDConstants.CURRENCY)
                                                break
                                            case "listPriceUsdCol": priceItem.listPrice = cellEntry ?  escapeService.parseFinancialValue(cellEntry) : null
                                                priceItem.listCurrency = RefdataValue.getByValueAndCategory("USD", RDConstants.CURRENCY)
                                                break
                                            case "listPriceGbpCol": priceItem.listPrice = cellEntry ? escapeService.parseFinancialValue(cellEntry) : null
                                                priceItem.listCurrency = RefdataValue.getByValueAndCategory("GBP", RDConstants.CURRENCY)
                                                break
                                            case "localPriceCol": priceItem.localPrice = cellEntry ? escapeService.parseFinancialValue(cellEntry) : null
                                                break
                                            case "localCurrencyCol": priceItem.localCurrency = RefdataValue.getByValueAndCategory(cellEntry, RDConstants.CURRENCY)
                                                break
                                            case "priceDateCol": priceItem.startDate = cellEntry  ? DateUtils.parseDateGeneric(cellEntry) : null
                                                break
                                        }
                                    }
                                    catch (NumberFormatException e) {
                                        log.error("Unparseable number ${cellEntry}")
                                    }
                                }
                            }
                        }

                        if(uploadCoverageDates && ieCoverage && !ieCoverage.findEquivalent(issueEntitlement.coverages)){
                            ieCoverage.issueEntitlement = issueEntitlement
                            if (!ieCoverage.save()) {
                                throw new EntitlementCreationException(ieCoverage.errors)
                            }else{
                                countChangesCoverageDates++
                            }
                        }
                        if(uploadPriceInfo && priceItem){
                            priceItem.setGlobalUID()
                            if (!priceItem.save()) {
                                throw new Exception(priceItem.errors.toString())
                            }else {

                                countChangesPrice++
                            }
                        }
                    }
                }
            }
        }

        /*println(count)
        println(countChangesCoverageDates)
        println(countChangesPrice)*/

        return [issueEntitlements: entIds.size(), processCount: count, processCountChangesCoverageDates: countChangesCoverageDates, processCountChangesPrice: countChangesPrice]
    }


    /**
     * Selects the given issue entitlements based on the given input stream
     * @param stream the file stream which contains the data to be selected
     * @param subscription the subscription whose holding should be accessed
     * @return a map containing the process result
     */
    Map issueEntitlementSelect(InputStream stream, Subscription subscription) {

        Integer count = 0
        Integer countSelectIEs = 0
        Map<String, Object> selectedIEs = [:]

        ArrayList<String> rows = stream.text.split('\n')
        Map<String, Integer> colMap = [zdbCol: -1, onlineIdentifierCol: -1, printIdentifierCol: -1, pick: -1]
        //read off first line of KBART file
        rows[0].split('\t').eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case "zdb_id": colMap.zdbCol = c
                    break
                case "print_identifier": colMap.printIdentifierCol = c
                    break
                case "online_identifier": colMap.onlineIdentifierCol = c
                    break
                case "print identifier": colMap.printIdentifierCol = c
                    break
                case "online identifier": colMap.onlineIdentifierCol = c
                    break
                case "pick": colMap.pick = c
                    break
            }
        }
        //after having read off the header row, pop the first row
        rows.remove(0)
        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNsAndNsType('zdb', TitleInstancePackagePlatform.class.name),
                                                       eissn: IdentifierNamespace.findByNsAndNsType('eissn', TitleInstancePackagePlatform.class.name), isbn: IdentifierNamespace.findByNsAndNsType('isbn',TitleInstancePackagePlatform.class.name),
                                                       issn : IdentifierNamespace.findByNsAndNsType('issn', TitleInstancePackagePlatform.class.name), pisbn: IdentifierNamespace.findByNsAndNsType('pisbn', TitleInstancePackagePlatform.class.name),
                                                       doi  : IdentifierNamespace.findByNsAndNsType('doi', TitleInstancePackagePlatform.class.name)]
        rows.eachWithIndex { row, int i ->
            log.debug("now processing entitlement ${i}")
            ArrayList<String> cols = row.split('\t')
            Map<String, Object> idCandidate
            if (colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.eissn, namespaces.isbn], value: cols[colMap.onlineIdentifierCol]]
            }
            else if (colMap.doiTitleCol >= 0 && cols[colMap.doiTitleCol]) {
                idCandidate = [namespaces: [namespaces.doi], value: cols[colMap.doiTitleCol]]
            }
            else if (colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.issn, namespaces.pisbn], value: cols[colMap.printIdentifierCol]]
            }
            else if (colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                idCandidate = [namespaces: [namespaces.zdb], value: cols[colMap.zdbCol]]
            }
            if (((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                    ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                    ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
            } else {

                List<Long> titleIds = TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp join tipp.ids ident where ident.ns in :namespaces and ident.value = :value', [namespaces:idCandidate.namespaces, value:idCandidate.value])
                if (titleIds.size() > 0) {
                    List<IssueEntitlement> issueEntitlements = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.id in (:titleIds) and ie.subscription.id = :subId and ie.status != :ieStatus', [titleIds: titleIds, subId: subscription.id, ieStatus: RDStore.TIPP_STATUS_REMOVED])
                    if (issueEntitlements.size() > 0) {
                        IssueEntitlement issueEntitlement = issueEntitlements[0]
                        count++

                        colMap.each { String colName, int colNo ->
                            if (colNo > -1 && cols[colNo]) {
                                String cellEntry = cols[colNo].trim()
                                    switch (colName) {
                                        case "pick":
                                            if(cellEntry.toLowerCase() == RDStore.YN_YES.value_de.toLowerCase() || cellEntry == RDStore.YN_YES.value_en.toLowerCase()) {
                                                selectedIEs[issueEntitlement.id.toString()] = 'checked'
                                                countSelectIEs++
                                            }
                                            break
                                    }
                            }
                        }
                    }
                }
            }
        }

        return [processCount: count, selectedIEs: selectedIEs, countSelectIEs: countSelectIEs]
    }

    @Deprecated
    def copySpecificSubscriptionEditorOfProvideryAndAgencies(Subscription sourceSub, Subscription targetSub){

        sourceSub.getProviders().each { provider ->
            RefdataValue refdataValue = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)

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

            Person.getPrivateByOrgAndObjectRespFromAddressbook(provider, sourceSub, 'Specific subscription editor', contextService.getOrg()).each { prs ->
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
        sourceSub.getAgencies().each { agency ->
            RefdataValue refdataValue = RefdataValue.getByValueAndCategory('Specific subscription editor', RDConstants.PERSON_RESPONSIBILITY)

            Person.getPublicByOrgAndObjectResp(agency, sourceSub, 'Specific subscription editor').each { prs ->
                if(!(agency in targetSub.orgRelations.org)){
                    OrgRole or = OrgRole.findByOrgAndSub(agency, sourceSub)
                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, or.properties)
                    newOrgRole.sub = targetSub
                    newOrgRole.save()
                }
                if(!PersonRole.findWhere(prs: prs, org: agency, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, org: agency, sub: targetSub, responsibilityType: refdataValue)
                    newPrsRole.save()
                }
            }

            Person.getPrivateByOrgAndObjectRespFromAddressbook(agency, sourceSub, 'Specific subscription editor', contextService.getOrg()).each { prs ->
                if(!(agency in targetSub.orgRelations.org)){
                    OrgRole or = OrgRole.findByOrgAndSub(agency, sourceSub)
                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, or.properties)
                    newOrgRole.sub = targetSub
                    newOrgRole.save()
                }
                if(!PersonRole.findWhere(prs: prs, org: agency, responsibilityType: refdataValue, sub: targetSub)){
                    PersonRole newPrsRole = new PersonRole(prs: prs, org: agency, sub: targetSub, responsibilityType: refdataValue)
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
        if (propDef.isIntegerType()) {
            int intVal = Integer.parseInt(value)
            prop.setIntValue(intVal)
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
                        println(prop.error)
                    }
                } else if(field == "dateValue") {
                    SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

                    def backup = prop."${field}"
                    try {
                        if (value && value.size() > 0) {
                            // parse new date
                            def parsed_date = sdf.parse(value)
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

    //-------------------------------------- cronjob section ----------------------------------------

    /**
     * Cronjob-triggered.
     * Processes subscriptions which have been marked with holdings to be freezed after their end time and sets all pending change
     * configurations to reject
     * @return true if the execution was successful, false otherwise
     */
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

}
