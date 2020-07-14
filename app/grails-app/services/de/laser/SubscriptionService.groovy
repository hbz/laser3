package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtil
import de.laser.helper.DebugUtil
import de.laser.helper.FactoryResult
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import grails.transaction.Transactional
import grails.util.Holders
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Timestamp
import java.text.SimpleDateFormat

@Transactional
class SubscriptionService {
    def contextService
    def accessService
    def subscriptionsQueryService
    def docstoreService
    def messageSource
    def escapeService
    def refdataService
    FilterService filterService
    Locale locale
    def grailsApplication
    GenericOIDService genericOIDService

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }

    //ex SubscriptionController.currentSubscriptions()
    Map<String,Object> getMySubscriptions(GrailsParameterMap params, User contextUser, Org contextOrg) {
        Map<String,Object> result = [:]
        result.max = params.max ? Integer.parseInt(params.max) : contextUser.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        DebugUtil du = new DebugUtil()
        du.setBenchmark('init data fetch')
        du.setBenchmark('consortia')
        result.availableConsortia = Combo.executeQuery("select c.toOrg from Combo as c where c.fromOrg = ?", [contextOrg])

        def consRoles = Role.findAll { authority == 'ORG_CONSORTIUM' }
        du.setBenchmark('all consortia')
        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSettings os_ct, OrgSettings os_gs where 
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
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.checkMinUserOrgRole(contextUser, contextOrg, 'INST_EDITOR')

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }

        du.setBenchmark('get base query')
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextOrg)
        result.filterSet = tmpQ[2]
        List<Subscription> subscriptions
        du.setBenchmark('fetch subscription data')
        if(params.sort == "providerAgency") {
            subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ).collect{ row -> row[0] }
        }
        else {
            subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] ) //,[max: result.max, offset: result.offset]
        }
        result.allSubscriptions = subscriptions
        du.setBenchmark('fetch licenses')
        result.allLinkedLicenses = Links.findAllByDestinationInListAndLinkType(subscriptions.collect { Subscription s -> GenericOIDService.getOID(s) },RDStore.LINKTYPE_LICENSE)
        /*du.setBenchmark('process licenses')
        allLinkedLicenses.each { Links li ->
            Set<String> linkedLicenses = result.allLinkedLicenses.get(li.destination)
            if(!linkedLicenses)
                linkedLicenses = []
            linkedLicenses << li.source
            result.allLinkedLicenses.put(li.destination,linkedLicenses)
        }*/
        du.setBenchmark('after licenses')
        if(!params.exportXLS)
            result.num_sub_rows = subscriptions.size()

        result.date_restriction = date_restriction
        du.setBenchmark('get properties')
        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)
        if (OrgSettings.get(contextOrg, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID) instanceof OrgSettings){
            result.statsWibid = contextOrg.getIdentifierByType('wibid')?.value
            result.usageMode = accessService.checkPerm("ORG_CONSORTIUM") ? 'package' : 'institution'
        }
        du.setBenchmark('end properties')
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)
        List bm = du.stopBenchmark()
        result.benchMark = bm
        result
    }

    Map<String,Object> getMySubscriptionsForConsortia(GrailsParameterMap params,User contextUser, Org contextOrg,List<String> tableConf) {
        Map<String,Object> result = [:]

        DebugUtil du = new DebugUtil()
        du.setBenchmark('filterService')

        result.max = params.max ? Integer.parseInt(params.max) : contextUser.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        Map fsq = filterService.getOrgComboQuery([comboType:RDStore.COMBO_TYPE_CONSORTIUM.value,sort: 'o.sortname'], contextOrg)
        result.filterConsortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams)

        du.setBenchmark('filterSubTypes & filterPropList')

        if(params.filterSet)
            result.filterSet = params.filterSet

        result.filterSubTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE).minus(
                RDStore.SUBSCRIPTION_TYPE_LOCAL
        )
        result.filterPropList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        /*
        String query = "select ci, subT, roleT.org from CostItem ci join ci.owner orgK join ci.sub subT join subT.instanceOf subK " +
                "join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                "where orgK = :org and orgK = roleK.org and roleK.roleType = :rdvCons " +
                "and orgK = roleTK.org and roleTK.roleType = :rdvCons " +
                "and roleT.roleType = :rdvSubscr "
        */

        // CostItem ci

        du.setBenchmark('filter query')

        String query
        Map qarams

        if('withCostItems' in tableConf) {
            query = "select ci, subT, roleT.org " +
                    " from CostItem ci right outer join ci.sub subT join subT.instanceOf subK " +
                    " join subK.orgRelations roleK join subT.orgRelations roleTK join subT.orgRelations roleT " +
                    " where roleK.org = :org and roleK.roleType = :rdvCons " +
                    " and roleTK.org = :org and roleTK.roleType = :rdvCons " +
                    " and ( roleT.roleType = :rdvSubscr or roleT.roleType = :rdvSubscrHidden ) " +
                    " and ( ci is null or (ci.owner = :org and ci.costItemStatus != :deleted) )"
            qarams = [org      : contextOrg,
                      rdvCons  : RDStore.OR_SUBSCRIPTION_CONSORTIA,
                      rdvSubscr: RDStore.OR_SUBSCRIBER_CONS,
                      rdvSubscrHidden: RDStore.OR_SUBSCRIBER_CONS_HIDDEN,
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

            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
            qarams.put('validOn', new Timestamp(sdf.parse(params.validOn).getTime()))
        }

        if (params.status?.size() > 0) {
            query += " and subT.status.id = :status "
            qarams.put('status', params.long('status'))
        } else if(!params.filterSet) {
            query += " and subT.status.id = :status "
            qarams.put('status', RDStore.SUBSCRIPTION_CURRENT.id)
            params.status = RDStore.SUBSCRIPTION_CURRENT.id
            result.defaultSet = true
        }

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

        if (params.containsKey('subKinds')) {
            query += " and subT.kind.id in (:subKinds) "
            qarams.put('subKinds', params.list('subKinds').collect { Long.parseLong(it) })
        }

        if (params.isPublicForApi) {
            query += "and subT.isPublicForApi = :isPublicForApi "
            qarams.put('isPublicForApi', (params.isPublicForApi == RDStore.YN_YES.id.toString()) ? true : false)
        }

        if (params.hasPerpetualAccess) {
            query += "and subT.hasPerpetualAccess = :hasPerpetualAccess "
            qarams.put('hasPerpetualAccess', (params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) ? true : false)
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

        String orderQuery = " order by roleT.org.sortname, subT.name"
        if (params.sort?.size() > 0) {
            orderQuery = " order by " + params.sort + " " + params.order
        }

        if(params.filterSet && !params.member && !params.validOn && !params.status && !params.filterPropDef && !params.filterProp && !params.form && !params.resource && !params.subTypes)
            result.filterSet = false

        //log.debug( query + " " + orderQuery )
        // log.debug( qarams )

        if('withCostItems' in tableConf) {
            du.setBenchmark('costs')

            String totalMembersQuery = query.replace("ci, subT, roleT.org", "roleT.org")

            result.totalMembers    = CostItem.executeQuery(
                    totalMembersQuery, qarams
            )

            List costs = CostItem.executeQuery(
                    query + " " + orderQuery, qarams
            )
            result.totalCount = costs.size()
            if(params.exportXLS || params.format)
                result.entries = costs
            else result.entries = costs.drop((int) result.offset).take((int) result.max)

            result.finances = {
                Map entries = [:]
                result.entries.each { obj ->
                    if (obj[0]) {
                        CostItem ci = obj[0]
                        if (!entries."${ci.billingCurrency}") {
                            entries."${ci.billingCurrency}" = 0.0
                        }

                        if (ci.costItemElementConfiguration == RDStore.CIEC_POSITIVE) {
                            entries."${ci.billingCurrency}" += ci.costInBillingCurrencyAfterTax
                        }
                        else if (ci.costItemElementConfiguration == RDStore.CIEC_NEGATIVE) {
                            entries."${ci.billingCurrency}" -= ci.costInBillingCurrencyAfterTax
                        }
                    }
                }
                entries
            }()
        }
        else if('onlyMemberSubs') {
            Set<Subscription> memberSubscriptions = Subscription.executeQuery(query,qarams)
            result.memberSubscriptions = memberSubscriptions
            result.totalCount = memberSubscriptions.size()
            result.entries = memberSubscriptions.drop((int) result.offset).take((int) result.max)
        }

        List bm = du.stopBenchmark()
        result.benchMark = bm

        result
    }

    List getMySubscriptions_readRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getSubscriptionsConsortiaQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        } else {
           /* tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))*/

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        }
        result
    }

    List getMySubscriptions_writeRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getSubscriptionsConsortiaQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        } else {
            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        }
        result
    }

    List getMySubscriptionsWithMyElements_readRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        }
        result
    }

    List getMySubscriptionsWithMyElements_writeRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        }

        result
    }

    //Konsortiallizenzen
    private List getSubscriptionsConsortiaQuery() {
        Map params = [:]
//        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.showParentsAndChildsSubs = false
//        params.showParentsAndChildsSubs = 'true'
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Teilnehmerlizenzen
    private List getSubscriptionsConsortialLicenseQuery() {
        Map params = [:]
//        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Lokallizenzen
    private List getSubscriptionsLocalLicenseQuery() {
        Map params = [:]
//        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = RDStore.SUBSCRIPTION_TYPE_LOCAL.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    List getValidSubChilds(Subscription subscription) {
        List<Subscription> validSubChildren = Subscription.executeQuery('select oo.sub from OrgRole oo where oo.sub.instanceOf = :sub and oo.roleType in (:subRoleTypes) order by oo.org.sortname asc, oo.org.name asc',[sub:subscription,subRoleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])
        validSubChildren
    }

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

    List getValidSurveySubChilds(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
                subscription,
                [RDStore.SUBSCRIPTION_CURRENT, RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION]
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

    List getValidSurveySubChildOrgs(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusInList(
                subscription,
                [RDStore.SUBSCRIPTION_CURRENT, RDStore.SUBSCRIPTION_UNDER_PROCESS_OF_SELECTION]
        )

        List orgs = OrgRole.findAllBySubInListAndRoleType(validSubChilds, RDStore.OR_SUBSCRIBER_CONS)

        if(orgs){
            return orgs.org
        }else{
            return []
        }
    }

    List getIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status <> :del",
                        [sub: subscription, del: RDStore.TIPP_STATUS_DELETED])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getIssueEntitlementsWithFilter(Subscription subscription, params) {

        if(subscription) {
            String base_qry = null
            Map<String,Object> qry_params = [subscription: subscription]

            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
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
                base_qry += "and ( ( lower(ie.tipp.title.title) like :title ) or ( exists ( from Identifier ident where ident.ti.id = ie.tipp.title.id and ident.value like :identifier ) ) ) "
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
                base_qry += " and ie.status != :deleted "
                qry_params.deleted = RDStore.TIPP_STATUS_DELETED
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
                base_qry += " and lower(ie.tipp.title.summaryOfContent) like :summaryOfContent "
                qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            }

            if(params.seriesNames) {
                base_qry += " and lower(ie.tipp.title.seriesName) like :seriesNames "
                qry_params.seriesNames = "%${params.seriesNames.trim().toLowerCase()}%"
            }

            if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
                base_qry += " and lower(ie.tipp.title.subjectReference) in (:subject_references)"
                qry_params.subject_references = params.list('subject_references').collect { ""+it.toLowerCase()+"" }
            }

            if (params.series_names && params.series_names != "" && params.list('series_names')) {
                base_qry += " and lower(ie.tipp.title.seriesName) in (:series_names)"
                qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
            }

            if(params.ebookFirstAutorOrFirstEditor) {
                base_qry += " and (lower(ie.tipp.title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.title.firstEditor) like :ebookFirstAutorOrFirstEditor) "
                qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                base_qry += " and ie.tipp.pkg.id = :pkgId "
                qry_params.pkgId = Long.parseLong(params.pkgfilter)
            }

            //dateFirstOnline from
            if(params.dateFirstOnlineFrom) {
                base_qry += " and (ie.tipp.title.dateFirstOnline is not null AND ie.tipp.title.dateFirstOnline >= :dateFirstOnlineFrom) "
                qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)

            }
            //dateFirstOnline to
            if(params.dateFirstOnlineTo) {
                base_qry += " and (ie.tipp.title.dateFirstOnline is not null AND ie.tipp.title.dateFirstOnline <= :dateFirstOnlineTo) "
                qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
            }

            if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ie.${params.sort} ${params.order} "
            } else {
                base_qry += "order by lower(ie.tipp.title.title) asc"
            }

            List<IssueEntitlement> ies = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, [max: params.max, offset: params.offset])


            ies.sort { it.tipp.title.title }
            ies
        }else{
            List<IssueEntitlement> ies = []
            ies
        }
    }


    // Entscheidung steht aus
    List getIssueEntitlementsUnderConsideration(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }
    //In Verhandlung
    List getIssueEntitlementsUnderNegotiation(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getIssueEntitlementsNotFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getSelectedIssueEntitlementsBySurvey(Subscription subscription, SurveyInfo surveyInfo) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getIssueEntitlementsFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    Set<String> getSubjects(List titleIDs) {
        Set<String> subjects = []

        if(titleIDs){
            subjects = TitleInstance.executeQuery("select distinct(subjectReference) from TitleInstance where subjectReference is not null and id in (:titleIDs) order by subjectReference", [titleIDs: titleIDs])
        }
        if(subjects.size() == 0){
            subjects << messageSource.getMessage('titleInstance.noSubjectReference.label', null, locale)
        }

        subjects
    }

    Set<String> getSeriesNames(List titleIDs) {
        Set<String> seriesName = []

        if(titleIDs){
            seriesName = TitleInstance.executeQuery("select distinct(seriesName) from TitleInstance where seriesName is not null and id in (:titleIDs) order by seriesName", [titleIDs: titleIDs])
        }
        if(seriesName.size() == 0){
            seriesName << messageSource.getMessage('titleInstance.noSeriesName.label', null, locale)
        }
        seriesName

    }

    List getCurrentIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :cur",
                        [sub: subscription, cur: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getVisibleOrgRelationsWithoutConsortia(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

    List getVisibleOrgRelations(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }


    boolean deleteStatus(Subscription targetSub, def flash) {
        targetSub.status = RDStore.SUBSCRIPTION_NO_STATUS
        return save(targetSub, flash)
    }


    boolean copyStatus(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.status = sourceSub.status ?: null
        return save(targetSub, flash)
    }


    boolean deleteKind(Subscription targetSub, def flash) {
        targetSub.kind = null
        return save(targetSub, flash)
    }


    boolean copyKind(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.kind = sourceSub.kind ?: null
        return save(targetSub, flash)
    }


    boolean deleteForm(Subscription targetSub, def flash) {
        targetSub.form = null
        return save(targetSub, flash)
    }


    boolean copyForm(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.form = sourceSub.form ?: null
        return save(targetSub, flash)
    }


    boolean deleteResource(Subscription targetSub, def flash) {
        targetSub.resource = null
        return save(targetSub, flash)
    }


    boolean copyResource(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.resource = sourceSub.resource ?: null
        return save(targetSub, flash)
    }


    boolean deletePublicForApi(Subscription targetSub, def flash) {
        targetSub.isPublicForApi = false
        return save(targetSub, flash)
    }


    boolean copyPublicForApi(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.isPublicForApi = sourceSub.isPublicForApi
        return save(targetSub, flash)
    }


    boolean deletePerpetualAccess(Subscription targetSub, def flash) {
        targetSub.hasPerpetualAccess = false
        return save(targetSub, flash)
    }


    boolean copyPerpetualAccess(Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        targetSub.hasPerpetualAccess = sourceSub.hasPerpetualAccess
        return save(targetSub, flash)
    }


    boolean deleteLicenses(List<License> toDeleteLicenses, Subscription targetSub, def flash) {
        toDeleteLicenses.each { License lic ->
            setOrgLicRole(targetSub,lic,true)
        }
    }


    boolean copyLicenses(List<License> toCopyLicenses, Subscription targetSub, def flash) {
        toCopyLicenses.each { License lic ->
            setOrgLicRole(targetSub,lic,false)
        }
    }


    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetSub, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }


    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Subscription sourceSub, Subscription targetSub, def flash) {
        sourceSub.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                    Object[] args = [or?.roleType?.getI10n("value") + " " + or?.org?.name]
                    flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
                } else {
                    def newProperties = or.properties

                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, newProperties)
                    //Vererbung ausschalten
                    newOrgRole.sharedFrom  = null
                    newOrgRole.isShared = false
                    newOrgRole.sub = targetSub
                    save(newOrgRole, flash)
                }
            }
        }
    }


    boolean deletePackages(List<SubscriptionPackage> packagesToDelete, Subscription targetSub, def flash) {
        //alle IEs löschen, die zu den zu löschenden Packages gehören
//        targetSub.issueEntitlements.each{ ie ->
        getIssueEntitlements(targetSub).each{ ie ->
            if (packagesToDelete.find { subPkg -> subPkg?.pkg?.id == ie?.tipp?.pkg?.id } ) {
                ie.status = RDStore.TIPP_STATUS_DELETED
                save(ie, flash)
            }
        }

        //alle zugeordneten Packages löschen
        if (packagesToDelete) {

            packagesToDelete.each { subPkg ->
                OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg=?", [subPkg])
                PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:subPkg])

                CostItem.findAllBySubPkg(subPkg).each { costItem ->
                    costItem.subPkg = null
                    if(!costItem.sub){
                        costItem.sub = subPkg.subscription
                    }
                    costItem.save(flush: true)
                }
            }

            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp in (:packagesToDelete) and sp.subscription = :sub ",
                    [packagesToDelete: packagesToDelete, sub: targetSub])
        }
    }


    boolean copyPackages(List<SubscriptionPackage> packagesToTake, Subscription targetSub, def flash) {
        packagesToTake.each { subscriptionPackage ->
            if (targetSub.packages?.find { it.pkg?.id == subscriptionPackage.pkg?.id }) {
                Object[] args = [subscriptionPackage.pkg.name]
                flash.error += messageSource.getMessage('subscription.err.packageAlreadyExistsInTargetSub', args, locale)
            }
            else {

                List<OrgAccessPointLink> pkgOapls = OrgAccessPointLink.findAllByIdInList(subscriptionPackage.oapls.id)
                subscriptionPackage.properties.oapls = null
                subscriptionPackage.properties.pendingChangeConfig = null //copied in next step
                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                InvokerHelper.setProperties(newSubscriptionPackage, subscriptionPackage.properties)
                newSubscriptionPackage.subscription = targetSub

                if(save(newSubscriptionPackage, flash)){
                    pkgOapls.each{ oapl ->

                        def oaplProperties = oapl.properties
                        oaplProperties.globalUID = null
                        OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                        InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                        newOrgAccessPointLink.subPkg = newSubscriptionPackage
                        newOrgAccessPointLink.save()
                    }
                }
            }
        }
    }

    boolean copyPendingChangeConfiguration(Collection<PendingChangeConfiguration> configs, SubscriptionPackage target) {
        configs.each { PendingChangeConfiguration config ->
            Map<String,Object> configSettings = [subscriptionPackage:target,settingValue:config.settingValue,settingKey:config.settingKey,withNotification:config.withNotification]
            PendingChangeConfiguration newPcc = PendingChangeConfiguration.construct(configSettings)
            if(newPcc) {
                if(AuditConfig.getConfig(config.subscriptionPackage.subscription,config.settingKey) && !AuditConfig.getConfig(target.subscription,config.settingKey))
                    AuditConfig.addConfig(target.subscription,config.settingKey)
                else if(!AuditConfig.getConfig(config.subscriptionPackage.subscription,config.settingKey) && AuditConfig.getConfig(target.subscription,config.settingKey))
                    AuditConfig.removeConfig(target.subscription,config.settingKey)
            }
        }
    }

    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Subscription targetSub, def flash) {
        entitlementsToDelete.each {
            it.status = RDStore.TIPP_STATUS_DELETED
            save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetSub])
    }


    boolean copyEntitlements(List<IssueEntitlement> entitlementsToTake, Subscription targetSub, def flash) {
        entitlementsToTake.each { ieToTake ->
            if (ieToTake.status != RDStore.TIPP_STATUS_DELETED) {
                def list = getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != RDStore.TIPP_STATUS_DELETED}
                if (list?.size() > 0) {
                    // mich gibts schon! Fehlermeldung ausgeben!
                    Object[] args = [ieToTake.tipp.title.title]
                    flash.error += messageSource.getMessage('subscription.err.titleAlreadyExistsInTargetSub', args, locale)
                } else {
                    def properties = ieToTake.properties
                    properties.globalUID = null
                    IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                    InvokerHelper.setProperties(newIssueEntitlement, properties)
                    newIssueEntitlement.coverages = null
                    newIssueEntitlement.ieGroups = null
                    newIssueEntitlement.subscription = targetSub

                    if(save(newIssueEntitlement, flash)){
                        ieToTake.properties.coverages.each{ coverage ->

                            def coverageProperties = coverage.properties
                            IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                            InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                            newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                            newIssueEntitlementCoverage.save(flush: true)
                        }
                    }
                }
            }
        }
    }


    void copySubscriber(List<Subscription> subscriptionToTake, Subscription targetSub, def flash) {
        targetSub.refresh()
        List<Subscription> targetChildSubs = getValidSubChilds(targetSub)
        subscriptionToTake.each { Subscription subMember ->
            //Gibt es mich schon in der Ziellizenz?
            Org found = targetChildSubs?.find { Subscription targetSubChild -> targetSubChild.getSubscriber() == subMember.getSubscriber() }?.getSubscriber()

            if (found) {
                // mich gibts schon! Fehlermeldung ausgeben!
                Object[] args = [found.sortname ?: found.name]
                flash.error += messageSource.getMessage('subscription.err.subscriberAlreadyExistsInTargetSub', args, locale)
//                diffs.add(message(code:'pendingChange.message_CI01',args:[costTitle,g.createLink(mapping:'subfinance',controller:'subscription',action:'index',params:[sub:cci.sub.id]),cci.sub.name,cci.costInBillingCurrency,newCostItem
            } else {
                //ChildSub Exist
//                ArrayList<Links> prevLinks = Links.findAllByDestinationAndLinkTypeAndObjectType(subMember.id, RDStore.LINKTYPE_FOLLOWS, Subscription.class.name)
//                if (prevLinks.size() == 0) {

                    /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                            [result.subscriptionInstance, it.id])*/

                    def newSubscription = new Subscription(
                            isMultiYear: subMember.isMultiYear,
                            type: subMember.type,
                            kind: subMember.kind,
                            status: targetSub.status,
                            name: subMember.name,
                            startDate: subMember.isMultiYear ? subMember.startDate : targetSub.startDate,
                            endDate: subMember.isMultiYear ? subMember.endDate : targetSub.endDate,
                            manualRenewalDate: subMember.manualRenewalDate,
                            /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                            identifier: UUID.randomUUID().toString(),
                            instanceOf: targetSub,
                            //previousSubscription: subMember?.id,
                            isSlaved: subMember.isSlaved,
                            owner: targetSub.owner ? subMember.owner : null,
                            resource: targetSub.resource ?: null,
                            form: targetSub.form ?: null,
                            isPublicForApi: targetSub.isPublicForApi,
                            hasPerpetualAccess: targetSub.hasPerpetualAccess
                    )
                    newSubscription.save(flush:true)
                    //ERMS-892: insert preceding relation in new data model
                    if (subMember) {
                        Links prevLink = new Links(source: GenericOIDService.getOID(newSubscription), destination: GenericOIDService.getOID(subMember), linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org)
                        if (!prevLink.save()) {
                            log.error("Subscription linking failed, please check: ${prevLink.errors}")
                        }
                    }

                    if (subMember.propertySet) {
                        //customProperties
                        for (prop in subMember.propertySet) {
                            SubscriptionProperty copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription, isPublic: prop.isPublic, tenant: prop.tenant)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.save()
                            //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                        }
                    }
                    /*
                    if (subMember.privateProperties) {
                        //privatProperties
                        List tenantOrgs = OrgRole.executeQuery('select o.org from OrgRole as o where o.sub = :sub and o.roleType in (:roleType)', [sub: subMember, roleType: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]]).collect {
                            it -> it.id
                        }
                        subMember.privateProperties?.each { prop ->
                            if (tenantOrgs.indexOf(prop.type?.tenant?.id) > -1) {
                                def copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription)
                                copiedProp = prop.copyInto(copiedProp)
                                copiedProp.save()
                                //newSubscription.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                            }
                        }
                    }
                    */

                    if (subMember.packages && targetSub.packages) {
                        //Package
                        subMember.packages?.each { pkg ->
                            def pkgOapls = pkg.oapls
                            pkg.properties.oapls = null
                            pkg.properties.pendingChangeConfig = null
                            SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                            InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                            newSubscriptionPackage.subscription = newSubscription

                            if(newSubscriptionPackage.save()){
                                pkgOapls.each{ oapl ->

                                    def oaplProperties = oapl.properties
                                    oaplProperties.globalUID = null
                                    OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                                    InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                                    newOrgAccessPointLink.subPkg = newSubscriptionPackage
                                    newOrgAccessPointLink.save()
                                }
                            }
                        }
                    }
                    if (subMember.issueEntitlements && targetSub.issueEntitlements) {
                        subMember.issueEntitlements?.each { ie ->
                            if (ie.status != RDStore.TIPP_STATUS_DELETED) {
                                def ieProperties = ie.properties
                                ieProperties.globalUID = null

                                IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                                newIssueEntitlement.coverages = null
                                newIssueEntitlement.ieGroups = null
                                newIssueEntitlement.subscription = newSubscription

                                if(save(newIssueEntitlement, flash)){
                                    ie.properties.coverages.each{ coverage ->

                                        def coverageProperties = coverage.properties
                                        IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                                        InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                                        newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                                        newIssueEntitlementCoverage.save()
                                    }
                                }
                            }
                        }
                    }

                    //OrgRole
                    subMember.orgRelations?.each { or ->
                        if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIPTION_COLLECTIVE]) || (targetSub.orgRelations.size() >= 1)) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = newSubscription
                            newOrgRole.save(flush:true)
                            log.debug("new org role set: ${newOrgRole.sub} for ${newOrgRole.org.sortname}")
                        }
                    }

                    if (subMember.prsLinks && targetSub.prsLinks) {
                        //PersonRole
                        subMember.prsLinks?.each { prsLink ->
                            PersonRole newPersonRole = new PersonRole()
                            InvokerHelper.setProperties(newPersonRole, prsLink.properties)
                            newPersonRole.sub = newSubscription
                            newPersonRole.save()
                        }
                    }
//                }
            }
        }
    }


    boolean deleteTasks(List<Long> toDeleteTasks, Subscription targetSub, def flash) {
        boolean isInstAdm = contextService.getUser().hasAffiliation("INST_ADM")
        def userId = contextService.user.id
        toDeleteTasks.each { deleteTaskId ->
            Task dTask = Task.get(deleteTaskId)
            if (dTask) {
                if (dTask.creator.id == userId || isInstAdm) {
                    delete(dTask, flash)
                } else {
                    Object[] args = [messageSource.getMessage('task.label', null, locale), deleteTaskId]
                    flash.error += messageSource.getMessage('default.not.deleted.notAutorized.message', args, locale)
                }
            } else {
                Object[] args = [deleteTaskId]
                flash.error += messageSource.getMessage('subscription.err.taskDoesNotExist', args, locale)
            }
        }
    }


    boolean copyTasks(Subscription sourceSub, def toCopyTasks, Subscription targetSub, def flash) {
        toCopyTasks.each { tsk ->
            def task = Task.findBySubscriptionAndId(sourceSub, tsk)
            if (task) {
                if (task.status != RDStore.TASK_STATUS_DONE) {
                    Task newTask = new Task()
                    InvokerHelper.setProperties(newTask, task.properties)
                    newTask.systemCreateDate = new Date()
                    newTask.subscription = targetSub
                    save(newTask, flash)
                }
            }
        }
    }


    boolean copyAnnouncements(Subscription sourceSub, def toCopyAnnouncements, Subscription targetSub, def flash) {
        sourceSub.documents?.each { dctx ->
            if (dctx.id in toCopyAnnouncements) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                    Doc newDoc = new Doc()
                    InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                    save(newDoc, flash)
                    DocContext newDocContext = new DocContext()
                    InvokerHelper.setProperties(newDocContext, dctx.properties)
                    newDocContext.subscription = targetSub
                    newDocContext.owner = newDoc
                    save(newDocContext, flash)
                }
            }
        }
    }


    def deleteAnnouncements(List<Long> toDeleteAnnouncements, Subscription targetSub, def flash) {
        targetSub.documents.each {
            if (toDeleteAnnouncements.contains(it.id) && it.owner?.contentType == Doc.CONTENT_TYPE_STRING  && !(it.domain)){
                Map params = [deleteId: it.id]
                log.debug("deleteDocuments ${params}");
                docstoreService.unifiedDeleteDocuments(params)
            }
        }
    }



    boolean deleteDates(Subscription targetSub, def flash){
        targetSub.startDate = null
        targetSub.endDate = null
        return save(targetSub, flash)
    }

    boolean copyDates(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.setStartDate(sourceSub.getStartDate())
        targetSub.setEndDate(sourceSub.getEndDate())
        return save(targetSub, flash)
    }

    void copyIdentifiers(Subscription baseSub, List<String> toCopyIdentifiers, Subscription newSub, def flash) {
        toCopyIdentifiers.each{ identifierId ->
            def ownerSub = newSub
            Identifier sourceIdentifier = Identifier.get(identifierId)
            IdentifierNamespace namespace = sourceIdentifier.ns
            String value = sourceIdentifier.value

            if (ownerSub && namespace && value) {
                FactoryResult factoryResult = Identifier.constructWithFactoryResult([value: value, reference: ownerSub, namespace: namespace])

                factoryResult.setFlashScopeByStatus(flash)
            }
        }
    }

    void deleteIdentifiers(List<String> toDeleteIdentifiers, Subscription newSub, def flash) {
        int countDeleted = Identifier.executeUpdate('delete from Identifier i where i.id in (:toDeleteIdentifiers) and i.sub = :sub',
        [toDeleteIdentifiers: toDeleteIdentifiers, sub: newSub])
        Object[] args = [countDeleted]
        flash.message += messageSource.getMessage('identifier.delete.success', args, locale)
    }

    def deleteDocs(List<Long> toDeleteDocs, Subscription targetSub, def flash) {
        log.debug("toDeleteDocCtxIds: " + toDeleteDocs)
        def updated = DocContext.executeUpdate("UPDATE DocContext set status = :del where id in (:ids)",
        [del: RDStore.DOC_CTX_STATUS_DELETED, ids: toDeleteDocs])
        log.debug("Number of deleted (per Flag) DocCtxs: " + updated)
    }


    boolean copyDocs(Subscription sourceSub, def toCopyDocs, Subscription targetSub, def flash) {
        sourceSub.documents?.each { dctx ->
            if (dctx.id in toCopyDocs) {
                if (((dctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (dctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (dctx.status?.value != 'Deleted')) {
                    try {

                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        save(newDoc, flash)

                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        newDocContext.subscription = targetSub
                        newDocContext.owner = newDoc
                        save(newDocContext, flash)

                        String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'

                        Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                        Path target = new File("${fPath}/${newDoc.uuid}").toPath()
                        Files.copy(source, target)

                    }
                    catch (Exception e) {
                        log.error("Problem by Saving Doc in documentStorageLocation (Doc ID: ${dctx.owner.id} -> ${e})")
                    }
                }
            }
        }
    }


    boolean copyProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        SubscriptionProperty targetProp


        properties.each { AbstractPropertyWithCalculatedLastUpdated sourceProp ->
            targetProp = targetSub.propertySet.find { it.typeId == sourceProp.typeId && sourceProp.tenant.id == sourceProp.tenant }
            boolean isAddNewProp = sourceProp.type?.multipleOccurrence
            if ( (! targetProp) || isAddNewProp) {
                targetProp = new SubscriptionProperty(type: sourceProp.type, owner: targetSub)
                targetProp = sourceProp.copyInto(targetProp)
                targetProp.isPublic = sourceProp.isPublic //provisoric, should be moved into copyInto once migration is complete
                save(targetProp, flash)
                if (((sourceProp.id.toString() in auditProperties)) && targetProp.isPublic) {
                    //copy audit
                    if (!AuditConfig.getConfig(targetProp, AuditConfig.COMPLETE_OBJECT)) {
                        def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(SubscriptionProperty.class.name, sourceProp.id)
                        auditConfigs.each {
                            AuditConfig ac ->
                                //All ReferenceFields were copied!
                                AuditConfig.addConfig(targetProp, ac.referenceField)
                        }
                        if (!auditConfigs) {
                            AuditConfig.addConfig(targetProp, AuditConfig.COMPLETE_OBJECT)
                        }
                    }
                }
            } else {
                Object[] args = [sourceProp.type.getI10n("name") ?: sourceProp.class.getSimpleName()]
                flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
            }
        }
    }


    boolean deleteProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        if (true){
            properties.each { AbstractPropertyWithCalculatedLastUpdated prop ->
                AuditConfig.removeAllConfigs(prop)
            }
        }
        int anzCP = SubscriptionProperty.executeUpdate("delete from SubscriptionProperty p where p in (:properties) and p.tenant = :org and p.isPublic = true",[properties: properties, org: contextService.org])
        int anzPP = SubscriptionProperty.executeUpdate("delete from SubscriptionProperty p where p in (:properties) and p.tenant = :org and p.isPublic = false",[properties: properties, org: contextService.org])
    }

    private boolean delete(obj, flash) {
        if (obj) {
            obj.delete(flush: true)
            log.debug("Delete ${obj} ok")
        } else {
            flash.error += messageSource.getMessage('default.delete.error.message', null, locale)
        }
    }

    private boolean save(obj, flash){
        if (obj.save(flush:true)){
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, locale)
            return false
        }
    }

    Map regroupSubscriptionProperties(List<Subscription> subsToCompare) {
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
            privateProperties = comparisonService.buildComparisonTree(privateProperties,sub,sub.privateProperties)
            result.privateProperties = privateProperties
        }
        result
    }

    boolean addEntitlement(sub, gokbId, issueEntitlementOverwrite, withPriceData, acceptStatus) throws EntitlementCreationException {
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            throw new EntitlementCreationException("Unable to tipp ${gokbId}")
        }
        else if(IssueEntitlement.findAllBySubscriptionAndTippAndStatus(sub, tipp, RDStore.TIPP_STATUS_CURRENT))
            {
                throw new EntitlementCreationException("Unable to create IssueEntitlement because IssueEntitlement exist with tipp ${gokbId}")
        } else {
            IssueEntitlement new_ie = new IssueEntitlement(
					status: tipp.status,
                    subscription: sub,
                    tipp: tipp,
                    accessStartDate: issueEntitlementOverwrite?.accessStartDate ? DateUtil.parseDateGeneric(issueEntitlementOverwrite.accessStartDate) : tipp.accessStartDate,
                    accessEndDate: issueEntitlementOverwrite?.accessEndDate ? DateUtil.parseDateGeneric(issueEntitlementOverwrite.accessEndDate) : tipp.accessEndDate,
                    ieReason: 'Manually Added by User',
                    acceptStatus: acceptStatus)
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
                        throw new EntitlementCreationException(ieCoverage.getErrors())
                    }
                }
                if(withPriceData && issueEntitlementOverwrite) {
                    if(issueEntitlementOverwrite instanceof IssueEntitlement && issueEntitlementOverwrite.priceItem) {
                        PriceItem pi = new PriceItem(priceDate: issueEntitlementOverwrite.priceItem.priceDate ?: null,
                                listPrice: issueEntitlementOverwrite.priceItem.listPrice ?: null,
                                listCurrency: issueEntitlementOverwrite.priceItem.listCurrency ?: null,
                                localPrice: issueEntitlementOverwrite.priceItem.localPrice ?: null,
                                localCurrency: issueEntitlementOverwrite.priceItem.localCurrency ?: null,
                                issueEntitlement: new_ie
                        )
                        pi.setGlobalUID()
                        if(pi.save())
                            return true
                        else {
                            throw new EntitlementCreationException(pi.errors)
                        }

                    }
                    else {

                        PriceItem pi = new PriceItem(priceDate: DateUtil.parseDateGeneric(issueEntitlementOverwrite.priceDate),
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
                else return true
            } else {
                throw new EntitlementCreationException(new_ie.errors)
            }
        }
    }

    boolean deleteEntitlement(sub, gokbId) {
        IssueEntitlement ie = IssueEntitlement.findWhere(tipp: TitleInstancePackagePlatform.findByGokbId(gokbId), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
            ie.status = RDStore.TIPP_STATUS_DELETED
            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    boolean deleteEntitlementbyID(sub, id) {
        IssueEntitlement ie = IssueEntitlement.findWhere(id: Long.parseLong(id), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
            ie.status = RDStore.TIPP_STATUS_DELETED
            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    static boolean rebaseSubscriptions(TitleInstancePackagePlatform tipp, TitleInstancePackagePlatform replacement) {
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

    boolean setOrgLicRole(Subscription sub, License newLicense, boolean unlink) {
        boolean success = false
        Links curLink = Links.findBySourceAndDestinationAndLinkType(GenericOIDService.getOID(newLicense),GenericOIDService.getOID(sub),RDStore.LINKTYPE_LICENSE)
        Org subscr = sub.getSubscriber()
        if(!unlink && !curLink) {
            try {
                if(Links.construct([source: GenericOIDService.getOID(newLicense), destination: GenericOIDService.getOID(sub), linkType: RDStore.LINKTYPE_LICENSE, owner: contextService.org])) {
                    RefdataValue licRole
                    if(sub.getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE])
                        licRole = RDStore.OR_LICENSEE_CONS
                    else if(sub.getCalculatedType() in [CalculatedType.TYPE_COLLECTIVE,CalculatedType.TYPE_LOCAL])
                        licRole = RDStore.OR_LICENSEE
                    else if(sub.getCalculatedType() == CalculatedType.TYPE_CONSORTIAL)
                        licRole = RDStore.OR_LICENSING_CONSORTIUM
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
                                log.error(orgLicRole.errors)
                        }
                    }
                    success = true
                }
            }
            catch (CreationException e) {
                log.error(e)
            }
        }
        else if(unlink && curLink) {
            String sourceOID = curLink.source
            License lic = genericOIDService.resolveOID(sourceOID)
            curLink.delete() //delete() is void, no way to check whether errors occurred or not
            if(sub.getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) {
                Set<Links> linkedSubs = Links.executeQuery("select li from Links li where li.source = :lic and li.linkType = :linkType and li.destination in (select concat('${Subscription.class.name}:',oo.sub.id) from OrgRole oo where oo.roleType in (:subscrTypes) and oo.org = :subscr)", [lic: sourceOID, linkType: RDStore.LINKTYPE_LICENSE, subscrTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], subscr: subscr])
                if (linkedSubs.size() < 1) {
                    log.info("no more license <-> subscription links between org -> removing licensee role")
                    OrgRole.executeUpdate("delete from OrgRole oo where oo.lic = :lic and oo.org = :subscriber", [lic: lic, subscriber: subscr])
                }
            }
            success = true
        }
        success
    }

    Map subscriptionImport(CommonsMultipartFile tsvFile) {
        Org contextOrg = contextService.org
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
                case "start date": colMap.startDate = c
                    break
                case "enddatum":
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
                default:
                    //check if property definition
                    boolean isNotesCol = false
                    String propDefString = headerCol.toLowerCase()
                    if(headerCol.contains('$$notes')) {
                        isNotesCol = true
                        propDefString = headerCol.split('\\$\\$')[0].toLowerCase()
                    }
                    Map queryParams = [propDef:propDefString,contextOrg:contextOrg]
                    List<PropertyDefinition> possiblePropDefs = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where (lower(pd.name_de) = :propDef or lower(pd.name_en) = :propDef) and (pd.tenant = :contextOrg or pd.tenant = null)",queryParams)
                    if(possiblePropDefs.size() == 1) {
                        PropertyDefinition propDef = possiblePropDefs[0]
                        if(isNotesCol) {
                            propMap[propDef.class.name+':'+propDef.id].notesColno = c
                        }
                        else {
                            String refCategory = ""
                            if(propDef.type == RefdataValue.toString()) {
                                refCategory = propDef.refdataCategory
                            }
                            Map<String,Integer> defPair = [colno:c,refCategory:refCategory]
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
            if(colMap.licenses != null) {
                List<String> licenseKeys = cols[colMap.licenses].split(',')
                candidate.licenses = []
                licenseKeys.each { String licenseKey ->
                    List<License> licCandidates = License.executeQuery("select oo.lic from OrgRole oo join oo.lic l where :idCandidate in (cast(l.id as string),l.globalUID) and oo.roleType in :roleTypes and oo.org = :contextOrg",[idCandidate:licenseKey,roleTypes:[RDStore.OR_LICENSEE_CONS,RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE],contextOrg:contextOrg])
                    if(licCandidates.size() == 1) {
                        License license = licCandidates[0]
                        candidate.licenses << GenericOIDService.getOID(license)
                    }
                    else if(licCandidates.size() > 1)
                        mappingErrorBag.multipleLicenseError << licenseKey
                    else
                        mappingErrorBag.noValidLicense << licenseKey
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
                startDate = DateUtil.parseDateGeneric(cols[colMap.startDate].trim())
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
                endDate = DateUtil.parseDateGeneric(cols[colMap.endDate].trim())
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
                Date manualCancellationDate = DateUtil.parseDateGeneric(cols[colMap.manualCancellationDate])
                if(manualCancellationDate)
                    candidate.manualCancellationDate = manualCancellationDate
            }
            //instanceOf(nullable:true, blank:false)
            if(colMap.instanceOf != null && colMap.member != null) {
                String idCandidate = cols[colMap.instanceOf].trim()
                String memberIdCandidate = cols[colMap.member].trim()
                if(idCandidate && memberIdCandidate) {
                    List<Subscription> parentSubs = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :contextOrg and oo.roleType in :roleTypes and :idCandidate in (cast(oo.sub.id as string),oo.sub.globalUID)",[contextOrg: contextOrg, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIPTION_COLLECTIVE], idCandidate: idCandidate])
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
            if(colMap.notes != null && cols[colMap.notes].trim()) {
                candidate.notes = cols[colMap.notes].trim()
            }
            candidates.put(candidate,mappingErrorBag)
        }
        [candidates: candidates, globalErrors: globalErrors, parentSubType: parentSubType]
    }

    Map issueEntitlementEnrichment(InputStream stream, List<IssueEntitlement> issueEntitlements, boolean uploadCoverageDates, boolean uploadPriceInfo) {

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
        Map<String, IdentifierNamespace> namespaces = [zdb  : IdentifierNamespace.findByNs('zdb'),
                                                       eissn: IdentifierNamespace.findByNs('eissn'), isbn: IdentifierNamespace.findByNs('isbn'),
                                                       issn : IdentifierNamespace.findByNs('issn'), pisbn: IdentifierNamespace.findByNs('pisbn'),
                                                       doi  : IdentifierNamespace.findByNs('doi')]
        rows.eachWithIndex { row, int i ->
            log.debug("now processing entitlement ${i}")
            ArrayList<String> cols = row.split('\t')
            Map<String, Object> idCandidate
            if (colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                idCandidate = [namespaces: [namespaces.zdb], value: cols[colMap.zdbCol]]
            }
            if (colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.eissn, namespaces.isbn], value: cols[colMap.onlineIdentifierCol]]
            }
            if (colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                idCandidate = [namespaces: [namespaces.issn, namespaces.pisbn], value: cols[colMap.printIdentifierCol]]
            }
            if (colMap.doiTitleCol >= 0 && cols[colMap.doiTitleCol]) {
                idCandidate = [namespaces: [namespaces.doi], value: cols[colMap.doiTitleCol]]
            }
            if (((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                    ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                    ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
            } else {

                List<Long> titleIds = TitleInstance.executeQuery('select ti.id from TitleInstance ti join ti.ids ident where ident.ns in :namespaces and ident.value = :value', [namespaces:idCandidate.namespaces, value:idCandidate.value])
                if (titleIds.size() > 0) {
                    IssueEntitlement issueEntitlement = issueEntitlements.find { it.tipp.title.id in titleIds }
                    IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage()
                    if (issueEntitlement) {
                        count++
                        PriceItem priceItem = issueEntitlement.priceItem ?: new PriceItem(issueEntitlement: issueEntitlement)
                        colMap.each { String colName, int colNo ->
                            if (colNo > -1 && cols[colNo]) {
                                String cellEntry = cols[colNo].trim()
                                if (uploadCoverageDates) {
                                    switch (colName) {
                                        case "startDateCol": ieCoverage.startDate = cellEntry ? DateUtil.parseDateGeneric(cellEntry) : null
                                            break
                                        case "startVolumeCol": ieCoverage.startVolume = cellEntry ?: null
                                            break
                                        case "startIssueCol": ieCoverage.startIssue = cellEntry ?: null
                                            break
                                        case "endDateCol": ieCoverage.endDate = cellEntry ? DateUtil.parseDateGeneric(cellEntry) : null
                                            break
                                        case "endVolumeCol": ieCoverage.endVolume = cellEntry ?: null
                                            break
                                        case "endIssueCol": ieCoverage.endIssue = cellEntry ?: null
                                            break
                                        case "accessStartDateCol": issueEntitlement.accessStartDate = cellEntry ? DateUtil.parseDateGeneric(cellEntry) : issueEntitlement.accessStartDate
                                            break
                                        case "accessEndDateCol": issueEntitlement.accessEndDate = cellEntry ? DateUtil.parseDateGeneric(cellEntry) : issueEntitlement.accessEndDate
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
                                            case "priceDateCol": priceItem.priceDate = cellEntry  ? DateUtil.parseDateGeneric(cellEntry) : null
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
                            if (!ieCoverage.save(flush: true)) {
                                throw new EntitlementCreationException(ieCoverage.getErrors())
                            }else{
                                countChangesCoverageDates++
                            }
                        }
                        if(uploadPriceInfo && priceItem){
                            priceItem.setGlobalUID()
                            if (!priceItem.save(flush: true)) {
                                throw new Exception(priceItem.getErrors().toString())
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

        return [issueEntitlements: issueEntitlements.size(), processCount: count, processCountChangesCoverageDates: countChangesCoverageDates, processCountChangesPrice: countChangesPrice]
    }
}