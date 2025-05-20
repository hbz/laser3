package de.laser

import de.laser.finance.CostItem
import de.laser.storage.RDStore
import de.laser.survey.SurveyResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.wekb.Provider
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.util.WebUtils

import java.text.SimpleDateFormat
import java.time.Year

@Transactional
class InfoService {

    ContextService contextService
    FilterService filterService
    FinanceService financeService
    SubscriptionsQueryService subscriptionsQueryService

    static final String PROVIDER_QUERY_1 = '''select pro.id, sub.id, sub.startDate, sub.endDate, sub.referenceYear, sub.name, sub.status.id from ProviderRole pr
                                    join pr.subscription sub
                                    join pr.provider pro
                                    where sub.id in (:subIdList)
                                    order by pro.sortname, pro.name, sub.name, sub.startDate, sub.endDate, sub.referenceYear asc '''

    static final String PROVIDER_QUERY_2 = '''select pro.id, sub.id, sub.startDate, sub.endDate, sub.referenceYear, sub.name, sub.status.id from SubscriptionPackage subPkg 
                                    join subPkg.subscription sub 
                                    join subPkg.pkg pkg 
                                    join pkg.provider pro where sub.id in (:subIdList)'''

    static final String PROVIDER_QUERY_3 = '''select pro.id, sub.id, sub.startDate, sub.endDate, sub.referenceYear, sub.name, sub.status.id from SubscriptionPackage subPkg 
                                    join subPkg.subscription sub 
                                    join subPkg.pkg pkg 
                                    join pkg.nominalPlatform plt 
                                    join plt.provider pro where sub.id in (:subIdList)'''

    class Helper {
        static Map listToMap(List<List> list) {
            list.groupBy{ it[0] }.sort{ it -> RefdataValue.get(it.key).getI10n('value') }
        }

        static Map reduceMap(Map map) {
            map.collectEntries{ k,v -> [(k):(v.collect{ it[1] })] }
        }

        static Map<Long, List> getTimelineCatsMap(Map timelineMap) {
            Map<Long, List> cats = [:]
            timelineMap.collect{ k,v -> v }.each { y ->
                y.each { fc ->
                    Long fcKey = Long.valueOf(fc.key)
                    if (!cats.containsKey(fcKey)) {
                        cats.put(fcKey, [])
                    }
                    cats.put(fcKey, (cats.get(fcKey) + fc.value).unique())
                }
            }
            cats
        }

        static Map getTimelineMap(struct) {
            Map<String, Map> years = [:]
            IntRange timeline = (Integer.parseInt(Year.now().toString()) - 3)..(Integer.parseInt(Year.now().toString()) + 2)

            timeline.each { year ->
                String y = year.toString()
                years[y] = [:]

                struct.each { e ->
                    String s          = e[0] ? e[0].toString() : null
                    Integer startYear = e[2] ? DateUtils.getYearAsInteger(e[2]) : null
                    Integer endYear   = e[3] ? DateUtils.getYearAsInteger(e[3]) : null
                    boolean current = false

                    if (! startYear && endYear && year <= endYear) {
                        current = true
                    }
                    else if (! endYear && startYear && year >= startYear) {
                        current = true
                    }
                    else if (startYear <= year && year <= endYear) {
                        current = true
                    }
                    else if (!startYear && !endYear) {
                        current = true
                    }

                    if (current) {
                        if (! years[y][s]) {
                            years[y][s] = []
                        }
                        years[y][s] << e[1]
                    }
                }
            }
            years
        }
    }

    Map<String, Object> getInfo_ConsAtInst(Org consortium, Org member) {

        Map<String, Object> result = [:]

        // --- subscriptions ---

        def (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([
                org: member,
                actionName: 'manageMembers',
                status: 'FETCH_ALL'
        ])

//        println base_qry; println qry_params

        List<List> subStruct = Subscription.executeQuery('select s.status.id, s.id, s.startDate, s.endDate, s.isMultiYear, s.referenceYear ' + base_qry, qry_params)
//        result.subscriptionMap = Helper.reduceMap(Helper.listToMap(subStruct))
        result.subscriptionTimelineMap = Helper.getTimelineMap(subStruct)
        result.subscriptionMap = Helper.getTimelineCatsMap(result.subscriptionTimelineMap)

//        println '\nsubStruct: ' + subStruct; println '\nsubscriptionMap: ' + result.subscriptionMap; println '\nsubscriptionTimelineMap: ' + result.subscriptionTimelineMap

        // --- licenses ---

        String licenseQuery = ''' from License as l where (
                                        exists ( select o from l.orgRelations as o where ( o.roleType = :roleTypeC AND o.org = :activeInst ) )
                                        AND l.instanceOf is not null
                                        AND exists ( select orgR from OrgRole as orgR where orgR.lic = l and orgR.org = :org )
                                    ) order by l.sortableReference, l.reference, l.startDate, l.endDate, l.instanceOf asc '''

        List<List> licStruct = License.executeQuery(
                'select l.status.id, l.id, l.startDate, l.endDate, l.openEnded ' + licenseQuery, [
                    org: member,
                    activeInst: contextService.getOrg(),
                    roleTypeC: RDStore.OR_LICENSING_CONSORTIUM
        ])
//        result.licenseMap = Helper.reduceMap(Helper.listToMap(licStruct))
        result.licenseTimelineMap = Helper.getTimelineMap(licStruct)
        result.licenseMap = Helper.getTimelineCatsMap(result.licenseTimelineMap)

//        println '\nlicStruct: ' + licStruct; println '\nlicenseMap: ' + result.licenseMap; println '\nlicenseTimelineMap: ' + result.licenseTimelineMap

        // --- provider ---

        Map providerParams = [
                subIdList: subStruct.collect { it[1] }
        ]

        List<List> providerStruct1 = Provider.executeQuery(PROVIDER_QUERY_1, providerParams)
        List<List> providerStruct2 = Provider.executeQuery(PROVIDER_QUERY_2, providerParams)
        List<List> providerStruct3 = Provider.executeQuery(PROVIDER_QUERY_3, providerParams)

//        Map providerMap = Helper.listToMap(providerStruct)
        List<List> providerStruct = (providerStruct1 + providerStruct2 + providerStruct3).unique()

        Map providerMap = providerStruct.groupBy{ it[0] }.sort{ it -> Provider.get(it.key).sortname ?: Provider.get(it.key).name }

//        println '\nproviderStruct: ' + providerStruct; println '\nproviderMap: ' + providerMap

        result.providerMap = providerMap.collectEntries{ k,v -> [(k):(v.collect{ it[1] })] }
        result.providerTimelineMap = Helper.getTimelineMap(providerStruct)

//        println '\nproviderTimelineMap: ' + result.providerTimelineMap

//        result.providerMap.each{subStatus, list ->
//            list.each{struct ->
//                Subscription sub = Subscription.get(struct[1])
//                List<CostItem> subCostItems = CostItem.executeQuery(
//                        ''' select ci from CostItem as ci right join ci.sub sub join sub.orgRelations oo
//                        where ci.owner = :owner
//                        and sub = :sub
//                        and oo.roleType = :roleType
//                        and ci.surveyOrg = null
//                        and ci.costItemStatus != :deleted
//                        order by ci.costTitle asc ''',
//                        [
//                                owner               : consortium,
//                                sub                 : sub,
//                                roleType            : RDStore.OR_SUBSCRIPTION_CONSORTIUM,
//                                deleted             : RDStore.COST_ITEM_DELETED
//                        ]
//                )
//                struct << [
//                        costItems   : subCostItems,
//                        sums        : financeService.calculateResults(subCostItems.id)
//                ]
//            }
//        }
//        println '\nproviderMap: ' + result.providerMap

        // --- surveys ---

//        List<SurveyInfo> surveyStruct =  SurveyInfo.executeQuery(
//                '''select so.finishDate != null, si.id, si.status.id, so.org.id, so.finishDate, sc.subscription.id
//                        from SurveyOrg so
//                        join so.surveyConfig sc
//                        join sc.surveyInfo si
//                        where so.org = :org and si.owner = :owner
//                        order by si.name, si.startDate, si.endDate ''',
//                [org: member, owner: consortium]
//        )
//
//        Map surveyMap = surveyStruct.groupBy{ it[0] } // Helper.listToMap(surveyStruct)
//        result.surveyMap = surveyMap.collectEntries{ k,v -> [(k):(v.collect{ [ it[1], it[4], it[5] ] })] }
////        println 'surveyMap: ' + result.surveyMap

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        GrailsParameterMap surveyParams = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
        surveyParams.owner = consortium

        result.surveyMap = [:]
        ['notFinish', 'finish', 'open', 'termination'].sort().each{
            surveyParams.tab = it
            FilterService.Result fsr = filterService.getParticipantSurveyQuery_New(surveyParams, sdf, member)
            if (fsr.isFilterSet) { surveyParams.filterSet = true }

            List sr = SurveyResult.executeQuery(fsr.query, fsr.queryParams)
            if (sr /*|| it == 'open' */) {
                result.surveyMap[it] = sr
            }
        }

        List<List> surveyStruct = []
        result.surveyMap.each{it -> it.value.each{e -> surveyStruct << [it.key, e, e[0].startDate, e[0].endDate]}}
        result.surveyTimelineMap = Helper.getTimelineMap(surveyStruct)

//        println '\nsurveyMap: ' + result.surveyMap; println '\nsurveyStruct: ' + surveyStruct; println '\nsurveyTimelineMap: ' + result.surveyTimelineMap

        // --- costs ---

        String costItemQuery = '''select ci from CostItem ci
                                    left join ci.costItemElementConfiguration ciec
                                    left join ci.costItemElement cie
                                    join ci.owner orgC
                                    join ci.sub sub
                                    join sub.instanceOf subC
                                    join subC.orgRelations roleC
                                    join sub.orgRelations roleMC
                                    join sub.orgRelations oo
                                    where orgC = :org and orgC = roleC.org and roleMC.roleType = :consortialType and oo.roleType in (:subscrType)
                                    and oo.org in (:filterConsMembers) and sub.status = :filterSubStatus
                                    and ci.surveyOrg = null and ci.costItemStatus != :deleted
                                    order by oo.org.sortname asc, sub.name, ciec.value desc, cie.value_''' + LocaleUtils.getCurrentLang() + ' desc '

        List<CostItem> consCostItems = CostItem.executeQuery( costItemQuery, [
                org                 : consortium,
                consortialType      : RDStore.OR_SUBSCRIPTION_CONSORTIUM,
                subscrType          : [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN],
                filterConsMembers   : [member],
                filterSubStatus     : RDStore.SUBSCRIPTION_CURRENT,
                deleted             : RDStore.COST_ITEM_DELETED
        ]
        )
        result.costs = [
                costItems   : consCostItems,
                sums        : financeService.calculateResults(consCostItems.id)
        ]
//        println result.costs

        result
    }

    Map<String, Object> getInfo_Inst(Org institution) {

        Map<String, Object> result = [:]

        // --- subscriptions ---

        def (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([
                org: institution,
                status: 'FETCH_ALL'
        ])

//        println base_qry; println qry_params

        List<List> subStruct = Subscription.executeQuery('select s.status.id, s.id, s.startDate, s.endDate, s.isMultiYear, s.referenceYear ' + base_qry, qry_params)
//        result.subscriptionMap = Helper.reduceMap(Helper.listToMap(subStruct))
        result.subscriptionTimelineMap = Helper.getTimelineMap(subStruct)
        result.subscriptionMap = Helper.getTimelineCatsMap(result.subscriptionTimelineMap)

//        println '\nsubStruct: ' + subStruct; println '\nsubscriptionMap: ' + result.subscriptionMap; println '\nsubscriptionTimelineMap: ' + result.subscriptionTimelineMap

        // --- licenses ---

        String licenseQuery = ''' from License as l where 
                                    ( exists ( select o from l.orgRelations as o where ( ( o.roleType in (:roleTypes) ) AND o.org = :org ) ) )
                                    order by l.sortableReference, l.reference, l.startDate, l.endDate, l.instanceOf asc '''

        List<List> licStruct = License.executeQuery(
                'select l.status.id, l.id, l.startDate, l.endDate, l.openEnded ' + licenseQuery, [
                    org: institution,
                    roleTypes: [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS]
        ])
//        result.licenseMap = Helper.reduceMap(Helper.listToMap(licStruct))
        result.licenseTimelineMap = Helper.getTimelineMap(licStruct)
        result.licenseMap = Helper.getTimelineCatsMap(result.licenseTimelineMap)

//        println '\nlicStruct: ' + licStruct; println '\nlicenseMap: ' + result.licenseMap; println '\nlicenseTimelineMap: ' + result.licenseTimelineMap

        // --- provider ---

        Map providerParams = [
                subIdList: subStruct.collect { it[1] }
        ]

        List<List> providerStruct1 = Provider.executeQuery(PROVIDER_QUERY_1, providerParams)
        List<List> providerStruct2 = Provider.executeQuery(PROVIDER_QUERY_2, providerParams)
        List<List> providerStruct3 = Provider.executeQuery(PROVIDER_QUERY_3, providerParams)

//        Map providerMap = Helper.listToMap(providerStruct)
        List<List> providerStruct = (providerStruct1 + providerStruct2 + providerStruct3).unique()

//        Map providerMap = Helper.listToMap(providerStruct)
        Map providerMap = providerStruct.groupBy{ it[0] }.sort{ it -> Provider.get(it.key).sortname ?: Provider.get(it.key).name }

//        println '\nproviderStruct: ' + providerStruct; println '\nproviderMap: ' + providerMap

        result.providerMap = providerMap.collectEntries{ k,v -> [(k):(v.collect{ it[1] })] }
        result.providerTimelineMap = Helper.getTimelineMap(providerStruct)

//        println '\nproviderMap: ' + result.providerMap; println '\nproviderTimelineMap: ' + result.providerTimelineMap

        // --- surveys ---

//        SurveyResult.findAllByParticipant(institution)

//        List<SurveyInfo> surveyStruct =  SurveyInfo.executeQuery(
//                '''select so.finishDate != null, si.id, si.status.id, so.org.id, so.finishDate, sc.subscription.id
//                        from SurveyOrg so
//                        join so.surveyConfig sc
//                        join sc.surveyInfo si
//                        where so.org = :org and si.owner = :owner
//                        order by si.name, si.startDate, si.endDate ''',
//                [org: result.orgInstance, owner: result.institution]
//        )
//
//        Map surveyMap = surveyStruct.groupBy{ it[0] } // listToMap(surveyStruct)
//        result.surveyMap = surveyMap.collectEntries{ k,v -> [(k):(v.collect{ [ it[1], it[4], it[5] ] })] }
////        println 'surveyMap: ' + result.surveyMap

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        GrailsParameterMap surveyParams = new GrailsParameterMap(WebUtils.retrieveGrailsWebRequest().getCurrentRequest())
//        surveyParams.owner = institution

        result.surveyMap = [:]
        ['notFinish', 'finish', 'open', 'termination'].sort().each{
            surveyParams.tab = it
            FilterService.Result fsr = filterService.getParticipantSurveyQuery_New(surveyParams, sdf, institution)
            if (fsr.isFilterSet) { surveyParams.filterSet = true }

            List sr = SurveyResult.executeQuery(fsr.query, fsr.queryParams)
            if (sr /*|| it == 'open' */) {
                result.surveyMap[it] = sr
            }
        }

        List<List> surveyStruct = []
        result.surveyMap.each{it -> it.value.each{e -> surveyStruct << [it.key, e, e[0].startDate, e[0].endDate]}}
        result.surveyTimelineMap = Helper.getTimelineMap(surveyStruct)

//        println '\nsurveyMap: ' + result.surveyMap; println '\nsurveyStruct: ' + surveyStruct; println '\nsurveyTimelineMap: ' + result.surveyTimelineMap

        // --- costs ---

//        String costItemQuery = '''select ci from CostItem ci
//                                    left join ci.costItemElementConfiguration ciec
//                                    left join ci.costItemElement cie
//                                    join ci.owner orgC
//                                    join ci.sub sub
//                                    join sub.instanceOf subC
//                                    join subC.orgRelations roleC
//                                    join sub.orgRelations roleMC
//                                    join sub.orgRelations oo
//                                    where orgC = :org and orgC = roleC.org and roleMC.roleType = :consortialType and oo.roleType in (:subscrType)
//                                    and oo.org in (:filterConsMembers) and sub.status = :filterSubStatus
//                                    and ci.surveyOrg = null and ci.costItemStatus != :deleted
//                                    order by oo.org.sortname asc, sub.name, ciec.value desc, cie.value_''' + LocaleUtils.getCurrentLang() + ' desc '
//
//        List<CostItem> consCostItems = CostItem.executeQuery( costItemQuery, [
//                org                 : institution,
//                consortialType      : RDStore.OR_SUBSCRIPTION_CONSORTIUM,
//                subscrType          : [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN],
//                filterConsMembers   : [institution],
//                filterSubStatus     : RDStore.SUBSCRIPTION_CURRENT,
//                deleted             : RDStore.COST_ITEM_DELETED
//        ]
//        )
//        result.costs = [
//                costItems   : consCostItems,
//                sums        : financeService.calculateResults(consCostItems.id)
//        ]
        result.costs = [
                costItems   : [:],
                sums        : [:]
        ]
//        println result.costs

        result
    }
}
