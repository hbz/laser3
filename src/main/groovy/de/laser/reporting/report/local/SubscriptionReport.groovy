package de.laser.reporting.report.local

import de.laser.FinanceService
import de.laser.IssueEntitlement
import de.laser.Links
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.ctrl.FinanceControllerService
import de.laser.finance.CostItem
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.utils.LocaleUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

import java.text.SimpleDateFormat

class SubscriptionReport {

    static String KEY = 'subscription'
    static String NO_DATE = 'Keine Angabe'
    static String NO_STARTDATE = 'Ohne Laufzeit-Beginn'
    static String NO_ENDDATE = 'Ohne Laufzeit-Ende'

    static int NUMBER_OF_TIMELINE_ELEMENTS = 4

    static Map<String, Object> getCurrentConfig(Subscription sub) {

        String calcType = sub._getCalculatedType()

        if (calcType in [Subscription.TYPE_CONSORTIAL]) {
            return SubscriptionXCfg.CONFIG_CONS_AT_CONS
        }
        else if (calcType in [Subscription.TYPE_PARTICIPATION]) {
            if (sub.getConsortia().id == BeanStore.getContextService().getOrg().id) {
                return SubscriptionXCfg.CONFIG_CONS_AT_SUBSCR
            }
            else {
                return SubscriptionXCfg.CONFIG_PARTICIPATION
            }
        }
        else {
            return SubscriptionXCfg.CONFIG
        }
    }

    static Map<String, Object> getCurrentQueryConfig(Subscription sub) {
        getCurrentConfig( sub ).base.query.default
    }

    static Map<String, Object> getCurrentTimelineConfig(Subscription sub) {
        getCurrentConfig( sub ).base.timeline
    }

    static List<String> getTimelineQueryLabels(GrailsParameterMap params) {
        List<String> meta = []

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Subscription sub = Subscription.get(params.id)

        // TODO
        getCurrentConfig( Subscription.get(params.token.split('#')[1]) ).base.timeline.each { cats ->
            if (cats.value.containsKey(params.query)) {
                String sd = sub.startDate ? sdf.format(sub.startDate) : NO_STARTDATE
                String ed = sub.endDate ? sdf.format(sub.endDate) : NO_ENDDATE
                meta = [ getMessage( 'timeline'), getMessage( 'timeline.' + params.query ), "${sd} - ${ed}" ]
            }
        }
        meta
    }
    static List<String> getTimelineQueryLabelsForAnnual(GrailsParameterMap params) {
        List<String> meta = []

        // TODO
        getCurrentConfig( Subscription.get(params.token.split('#')[1]) ).base.timeline.each { cats ->
            if (cats.value.containsKey(params.query)) {
                meta = [ getMessage( 'timeline'), getMessage( 'timeline.' + params.query ), "${params.id}" ]
            }
        }
        meta
    }

    static Map<String, Object> query(GrailsParameterMap params) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        Map<String, Object> result = BaseQuery.getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        Long id = params.long('id')

        final int indexPlusList = 3
        final int indexMinusList = 4

        if (! id) {
            // 1axis3values ['id', 'name', 'value', 'plus', 'minus', 'annual', 'isCurrent' ]
            // annualMember ['id', 'name', 'value', 'isCurrent']
            // cost         ['id', 'name', 'valueCons', 'valueConsTax', 'annual', 'isCurrent']
        }
        else {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getTimeline(sub)

            if (prefix == 'timeline') {

                long timelineIsCurrentId = id

                if (params.query == 'timeline-member') {
                    List<List<Long>> subIdLists = []

                    timeline.eachWithIndex { s, i ->
                        subIdLists.add(Subscription.executeQuery(
                                'select m.id from Subscription sub join sub.derivedSubscriptions m where sub = :sub', [sub: s]
                        ))
                        List data = [
                                s.id,
                                s.name,
                                subIdLists.get(i).size(),
                                [],
                                [],
                                (s.startDate ? sdf.format(s.startDate) : NO_STARTDATE) + ' - ' + (s.endDate ? sdf.format(s.endDate) : NO_ENDDATE),
                                sub == s
                        ]
                        result.data.add(data)
                        result.dataDetails.add([
                                query: params.query,
                                id   : s.id,
                                label: data[5]
                        ])
                    }

                    result.dataDetails.eachWithIndex { Map<String, Object> dd, i ->
                        List d = result.data.get(i)

                        String orgHql = 'select distinct ro.org.id from Subscription s join s.orgRelations ro where s.id in (:idList) and ro.roleType in (:roleTypes)'
                        List<RefdataValue> roleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]

                        if (i > 0) {
                            List<Long> currIdList = subIdLists.get(i)
                            List<Long> prevIdList = subIdLists.get(i - 1)

                            List<Long> currMemberIdList = currIdList ? Org.executeQuery(orgHql, [idList: currIdList, roleTypes: roleTypes]) : []
                            List<Long> prevMemberIdList = prevIdList ? Org.executeQuery(orgHql, [idList: prevIdList, roleTypes: roleTypes]) : []

                            dd.idList = currMemberIdList
                            dd.plusIdList = currMemberIdList.minus(prevMemberIdList)
                            dd.minusIdList = prevMemberIdList.minus(currMemberIdList)

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                        else {
                            List<Long> currMemberIdList = subIdLists.get(i) ? Org.executeQuery(orgHql, [idList: subIdLists.get(i), roleTypes: roleTypes]) : []

                            dd.idList = currMemberIdList
                            dd.plusIdList = currMemberIdList
                            dd.minusIdList = []

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                    }
                }
                else if (params.query == 'timeline-entitlement') {
                    List<List<Long>> ieIdLists = []

                    timeline.eachWithIndex { s, i ->
                        ieIdLists.add(IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT]
                        ))
                        List data = [
                                s.id,
                                s.name,
                                ieIdLists.get(i).size(),
                                [],
                                [],
                                (s.startDate ? sdf.format(s.startDate) : NO_STARTDATE) + ' - ' + (s.endDate ? sdf.format(s.endDate) : NO_ENDDATE),
                                sub == s
                        ]
                        result.data.add(data)
                        result.dataDetails.add([
                                query: params.query,
                                id   : s.id,
                                label: data[5]
                        ])
                    }

//                    result.dataDetails.eachWithIndex { Map<String, Object> dd, i ->
//                        List d = result.data.get(i)
//
//                        String tippHql = 'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)'
//
//                        if (i > 0) {
//                            List<Long> currIdList = ieIdLists.get(i)
//                            List<Long> prevIdList = ieIdLists.get(i - 1)
//
//                            List<Long> currTippIdList = currIdList ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: currIdList]) : []
//                            List<Long> prevTippIdList = prevIdList ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: prevIdList]) : []
//
//                            dd.idList = currTippIdList
//                            dd.plusIdList = currTippIdList.minus(prevTippIdList)
//                            dd.minusIdList = prevTippIdList.minus(currTippIdList)
//
//                            d[indexPlusList] = dd.plusIdList.size()
//                            d[indexMinusList] = dd.minusIdList.size()
//                        }
//                        else {
//                            List<Long> currTippIdList = ieIdLists.get(i) ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: ieIdLists.get(i)]) : []
//
//                            dd.idList = currTippIdList
//                            dd.plusIdList = currTippIdList
//                            dd.minusIdList = []
//
//                            d[indexPlusList] = dd.plusIdList.size()
//                            d[indexMinusList] = dd.minusIdList.size()
//                        }
//                    }

                    result.dataDetails.eachWithIndex { Map<String, Object> dd, i ->
                        List d = result.data.get(i)

                        String tippUrlHql = 'select tipp.id, tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)'
                        String tippHql    = 'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)'

                        if (i > 0) {
                            List<Long> currIdList = ieIdLists.get(i)
                            List<Long> prevIdList = ieIdLists.get(i - 1)

                            Map<Long, String> currTippUrlMap = (currIdList ? TitleInstancePackagePlatform.executeQuery(tippUrlHql, [idList: currIdList]).collectEntries {[it[0], it[1]]} : [:]) as Map<Long, String>
                            Map<Long, String> prevTippUrlMap = (prevIdList ? TitleInstancePackagePlatform.executeQuery(tippUrlHql, [idList: prevIdList]).collectEntries {[it[0], it[1]]} : [:]) as Map<Long, String>

                            dd.idList = currTippUrlMap.keySet() as List<Long>
                            dd.plusIdList = []
                            dd.minusIdList = []

                            currTippUrlMap.each { curr ->
                                if (curr.value) {
                                    if (! prevTippUrlMap.values().contains(curr.value)) { dd.plusIdList << curr.key }
                                } else {
                                    if (! prevTippUrlMap.keySet().contains(curr.key)) { dd.plusIdList << curr.key }
                                }
                            }
                            prevTippUrlMap.each { prev ->
                                if (prev.value) {
                                    if (! currTippUrlMap.values().contains(prev.value)) { dd.minusIdList << prev.key }
                                } else {
                                    if (! currTippUrlMap.keySet().contains(prev.key)) { dd.minusIdList << prev.key }
                                }
                            }

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                        else {
                            List<Long> currTippIdList = ieIdLists.get(i) ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: ieIdLists.get(i)]) : []

                            dd.idList = currTippIdList
                            dd.plusIdList = currTippIdList
                            dd.minusIdList = []

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                    }
                }
                else if (params.query == 'timeline-package') {
                    List<List<Long>> pkgIdLists = []

                    timeline.eachWithIndex { s, i ->
                        pkgIdLists.add(de.laser.Package.executeQuery(
                                'select distinct ie.tipp.pkg.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT]
                        ))
                        List data = [
                                s.id,
                                s.name,
                                pkgIdLists.get(i).size(),
                                [],
                                [],
                                (s.startDate ? sdf.format(s.startDate) : NO_STARTDATE) + ' - ' + (s.endDate ? sdf.format(s.endDate) : NO_ENDDATE),
                                sub == s
                        ]
                        result.data.add(data)
                        result.dataDetails.add([
                                query: params.query,
                                id   : s.id,
                                label: data[5]
                        ])
                    }

                    result.dataDetails.eachWithIndex { Map<String, Object> dd, i ->
                        List d = result.data.get(i)

                        if (i > 0) {
                            List<Long> currIdList = pkgIdLists.get(i)
                            List<Long> prevIdList = pkgIdLists.get(i - 1)

                            dd.idList = currIdList
                            dd.plusIdList = currIdList.minus(prevIdList)
                            dd.minusIdList = prevIdList.minus(currIdList)

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                        else {
                            List<Long> currPkgIdList = pkgIdLists.get(i)

                            dd.idList = currPkgIdList
                            dd.plusIdList = currPkgIdList
                            dd.minusIdList = []

                            d[indexPlusList] = dd.plusIdList.size()
                            d[indexMinusList] = dd.minusIdList.size()
                        }
                    }
                }
                else if (params.query in ['timeline-member-cost', 'timeline-participant-cost']) {
                    GrailsParameterMap clone = params.clone() as GrailsParameterMap

                    // ApplicationTagLib g = BeanStore.getApplicationTagLib()
                    FinanceService financeService = BeanStore.getFinanceService()
                    FinanceControllerService financeControllerService = BeanStore.getFinanceControllerService()

                    timeline.eachWithIndex { s, i ->
                        clone.setProperty('id', s.id)

                        Map<String, Object> fsCifsMap = financeControllerService.getResultGenerics(clone)
                        fsCifsMap.put('max', 5000)
                        //println fsCifsMap // todo - remove
                        Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, fsCifsMap)
                        //println finance // todo - remove
                        //List<CostItem> relevantCostItems = finance.cons.costItems.findAll{ it.costItemElementConfiguration in [RDStore.CIEC_POSITIVE, RDStore.CIEC_NEGATIVE]} ?: []
                        def typeDependingCosts = finance.cons ?: finance.subscr

                        List<CostItem> neutralCostItems = (typeDependingCosts.costItems.findAll{ it.costItemElementConfiguration == RDStore.CIEC_NEUTRAL } ?: []) as List<CostItem>
                        List<Double> vncList  = neutralCostItems.collect{Double cilc = it.costInLocalCurrency ?: 0.0; it.finalCostRounding ? cilc.round(0) : cilc.round(2) }
                        List<Double> vnctList = neutralCostItems.collect{it.getCostInLocalCurrencyAfterTax() }

                        List data = [
                                s.id,
                                s.name,
                                vncList ? vncList.sum() : 0.0,
                                vnctList ? vnctList.sum() : 0.0,
                                typeDependingCosts?.sums?.localSums?.localSum ?: 0,
                                typeDependingCosts?.sums?.localSums?.localSumAfterTax ?: 0,
                                (s.startDate ? sdf.format(s.startDate) : NO_STARTDATE) + ' - ' + (s.endDate ? sdf.format(s.endDate) : NO_ENDDATE),
                                sub == s
                        ]
                        result.data.add(data)
                        result.dataDetails.add([
                                query   : params.query,
                                id      : s.id,
                                label   : data[6],
                                idList  : typeDependingCosts.costItems.collect{ it.id },
                                vnc     : ( Math.round(data[2] * 100) / 100 ).doubleValue(),
                                vnct    : ( Math.round(data[3] * 100) / 100 ).doubleValue(),
                                vc      : ( Math.round(data[4] * 100) / 100 ).doubleValue(),
                                vct     : ( Math.round(data[5] * 100) / 100 ).doubleValue()
                        ])
                    }
                }
                else if (params.query == 'timeline-annualMember-subscription') {
                    List<Long> subIdLists = []

                    if (timeline) {
                        subIdLists = Subscription.executeQuery(
                                'select distinct s.id from Subscription s join s.orgRelations oo where s.instanceOf in (:timeline) and oo.roleType in :subscriberRoleTypes',
                                            [timeline: timeline, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                        )
                    }

                    BaseQuery.handleGenericAnnualXQuery(params.query, 'Subscription', subIdLists, result)
                    List newData = []
                    result.data.each { d ->
                        boolean isCurrent = (sub.startDate && sub.endDate) ? DateUtils.getYearAsInteger(sub.startDate) <= d[0] && DateUtils.getYearAsInteger(sub.endDate) >= d[0] : false
                        if (isCurrent) {
                            timelineIsCurrentId = Long.valueOf(d[0] as String)
                        }
                        newData.add([
                            d[0], d[1], d[2], isCurrent
                        ])
                    }
                    result.data = newData
                }

                // keep all data for correct processing and only then limit

                int timelineFromIdx = 0
                result.data.eachWithIndex{ List entry, int idx ->
                    if (entry[0] == timelineIsCurrentId) { timelineFromIdx = idx }
                }
                int timelineToIdx = (timelineFromIdx + NUMBER_OF_TIMELINE_ELEMENTS)

                if ((result.data.size() <= NUMBER_OF_TIMELINE_ELEMENTS) || timelineToIdx > result.data.size()) {
                    result.data = (result.data as List).takeRight(NUMBER_OF_TIMELINE_ELEMENTS)
                    result.dataDetails = (result.dataDetails as List).takeRight(NUMBER_OF_TIMELINE_ELEMENTS)
                }
                else {
                    result.data = (result.data as List).subList(timelineFromIdx, timelineToIdx)
                    result.dataDetails = (result.dataDetails as List).subList(timelineFromIdx, timelineToIdx)
                }

            }

            else if (prefix == 'tipp') {

                List<Long> idList = TitleInstancePackagePlatform.executeQuery(
                        'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id = :id and ie.status = :status ',
                        [id: id, status: RDStore.TIPP_STATUS_CURRENT]
                )

                if (params.query == 'tipp-seriesName') {

                    processSimpleTippQuery(params.query, 'seriesName', idList, result)
                }
                else if (params.query == 'tipp-subjectReference') {

                    processSimpleTippQuery(params.query, 'subjectReference', idList, result)
                }
                else if (params.query == 'tipp-titleType') {

                    processSimpleTippQuery(params.query, 'titleType', idList, result)
                }
                else if (params.query == 'tipp-publisherName') {

                    processSimpleTippQuery(params.query, 'publisherName', idList, result)
                }
                else if (params.query == 'tipp-medium') {

                    processSimpleTippRefdataQuery(params.query, 'medium', idList, result)
                }
                else if (params.query == 'tipp-dateFirstOnline') {

                    String tippYear = 'year(tipp.dateFirstOnline)'
                    List<String> PROPERTY_QUERY = [
                            'select ' + tippYear + ', ' + tippYear + ', count(*) ',
                            'and ' + tippYear + ' is not null group by ' + tippYear + ' order by ' + tippYear
                    ]

                    BaseQuery.handleGenericQuery(
                            params.query,
                            PROPERTY_QUERY[0] + 'from TitleInstancePackagePlatform tipp where tipp.id in (:idList) ' + PROPERTY_QUERY[1],
                            'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and ' + tippYear + ' = :d order by tipp.dateFirstOnline',
                            'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.dateFirstOnline is null',
                            idList,
                            result
                    )
                }
                else if (params.query == 'tipp-ddcs') {

                    TitleInstancePackagePlatform.executeQuery(
                            'select ddc.ddc.id, count(*) from DeweyDecimalClassification ddc where ddc.tipp.id in (:idList) group by ddc.ddc.id',
                            [idList: idList]
                    ).each { tmp ->
                        String label = RefdataValue.get(tmp[0]).getI10n('value')
                        result.data.add([tmp[0], label, tmp[1]])

                        result.dataDetails.add([
                                query : params.query,
                                id    : tmp[0],
                                label : label,
                                idList: TitleInstancePackagePlatform.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp, DeweyDecimalClassification ddc ' +
                                        'where tipp.id in (:idList) and ddc.tipp = tipp and ddc.ddc.id = :d order by tipp.sortname',
                                        [idList: idList, d: tmp[0]]
                                )
                        ])
                    }

                    List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                    List<Long> noDataList = nonMatchingIdList ? TitleInstancePackagePlatform.executeQuery(
                            'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList)', [idList: nonMatchingIdList]
                    ) : []

                    BaseQuery.handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                }
                else if (params.query == 'tipp-languages') {

                    TitleInstancePackagePlatform.executeQuery(
                            'select lang.language.id, count(*) from Language lang where lang.tipp.id in (:idList) group by lang.language.id',
                            [idList: idList]
                    ).each { tmp ->
                        String label = RefdataValue.get(tmp[0]).getI10n('value')
                        result.data.add([tmp[0], label, tmp[1]])

                        result.dataDetails.add([
                                query : params.query,
                                id    : tmp[0],
                                label : label,
                                idList: TitleInstancePackagePlatform.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp, Language lang ' +
                                                'where tipp.id in (:idList) and lang.tipp = tipp and lang.language.id = :d order by tipp.sortname',
                                        [idList: idList, d: tmp[0]]
                                )
                        ])
                    }

                    List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                    List<Long> noDataList = nonMatchingIdList ? TitleInstancePackagePlatform.executeQuery(
                            'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList)', [idList: nonMatchingIdList]
                    ) : []

                    BaseQuery.handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_DATA_LABEL, noDataList, result)
                }
                /* else if (params.query == 'tipp-platform') {

                    result.data = Platform.executeQuery(
                            'select p.id, p.name, count(*) from TitleInstancePackagePlatform tipp join tipp.platform p where tipp.id in (:idList) group by p.id order by p.name',
                            [idList: idList]
                    )
                    result.data.each { d ->
                        result.dataDetails.add([
                                query : params.query,
                                id    : d[0],
                                label : d[1],
                                idList: TitleInstancePackagePlatform.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.platform.id = :d order by tipp.sortname',
                                        [idList: idList, d: d[0]]
                                )
                        ])
                    }
                }
                else if (params.query == 'tipp-package') {
                    result.data = Platform.executeQuery(
                            'select p.id, p.name, count(*) from TitleInstancePackagePlatform tipp join tipp.pkg p where tipp.id in (:idList) group by p.id order by p.name',
                            [idList: idList]
                    )
                    result.data.each { d ->
                        result.dataDetails.add([
                                query : params.query,
                                id    : d[0],
                                label : d[1],
                                idList: TitleInstancePackagePlatform.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.pkg.id = :d order by tipp.sortname',
                                        [idList: idList, d: d[0]]
                                )
                        ])
                    }
                } */

                result.put('objectReference', id) // workaround : XYZ
            }

            else if (prefix == 'member') {

                List<Long> idList = Org.executeQuery(
                        'select distinct ro.org.id from Subscription s join s.derivedSubscriptions m join m.orgRelations ro where s.id = :id and ro.roleType in (:roleTypes)',
                        [id: id, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                )

                if (params.query == 'member-country') {
                    processSimpleMemberRefdataQuery(params.query, 'country', idList, result)
                }
                else if (params.query == 'member-customerType') {

                    BaseQuery.handleGenericRoleQuery(
                            params.query,
                            'select r.id, r.authority, count(*) from Org o, OrgSetting oss, Role r where oss.org = o and oss.key = \'CUSTOMER_TYPE\' and o.id in (:idList) and oss.roleValue = r group by r.id',
                            'select o.id from Org o, OrgSetting oss where oss.org = o and oss.key = \'CUSTOMER_TYPE\' and o.id in (:idList) and oss.roleValue.id = :d order by o.sortname, o.name',
                            'select distinct o.id from Org o where o.id in (:idList) and not exists (select oss from OrgSetting oss where oss.org = o and oss.key = \'CUSTOMER_TYPE\')',
                            idList,
                            result
                    )
                }
                else if (params.query == 'member-eInvoicePortal') {
                    processSimpleMemberRefdataQuery(params.query, 'eInvoicePortal', idList, result)

                }else if (params.query == 'member-funderHskType') {
                    processSimpleMemberRefdataQuery(params.query, 'funderHskType', idList, result)
                }
                else if (params.query == 'member-funderType') {
                    processSimpleMemberRefdataQuery(params.query, 'funderType', idList, result)
                }
                else if (params.query == 'member-libraryNetwork') {
                    processSimpleMemberRefdataQuery(params.query, 'libraryNetwork', idList, result)
                }
                else if (params.query == 'member-libraryType') {
                    processSimpleMemberRefdataQuery(params.query, 'libraryType', idList, result)
                }
                else if (params.query == 'member-orgType') {

                    BaseQuery.handleGenericRefdataQuery(
                            params.query,
                            'select p.id, p.value_de, count(*) from Org o join o.orgType p where o.id in (:idList) group by p.id, p.value_de order by p.value_de',
                            'select o.id from Org o join o.orgType p where o.id in (:idList) and p.id = :d order by o.sortname, o.name',
                            'select distinct o.id from Org o where o.id in (:idList) and not exists (select ot from o.orgType ot)',
                            idList,
                            result
                    )
                }
                else if (params.query == 'member-region') {
                    processSimpleMemberRefdataQuery(params.query, 'region', idList, result)
                }
                else if (params.query == 'member-subjectGroup') {

                    BaseQuery.handleGenericRefdataQuery(
                            params.query,
                            'select p.id, p.value_de, count(*) from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList) group by p.id, p.value_de order by p.value_de',
                            'select o.id from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList) and p.id = :d order by o.sortname, o.name',
                            'select distinct o.id from Org o where o.id in (:idList) and not exists (select osg from OrgSubjectGroup osg where osg.org = o)',
                            idList,
                            result
                    )
                }
            }
        }

        result
    }

    static void processSimpleMemberRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

        List<String> PROPERTY_QUERY = [
                'select p.id, p.value_de, count(*) ',
                ' group by p.id, p.value_de order by p.value_de'
        ]

        BaseQuery.handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Org o join o.' + refdata + ' p where o.id in (:idList)' + PROPERTY_QUERY[1],
                'select o.id from Org o join o.' + refdata + ' p where o.id in (:idList) and p.id = :d order by o.sortname, o.name',
                'select distinct o.id from Org o where o.id in (:idList) and o.' + refdata + ' is null',
                idList,
                result
        )
    }

    static void processSimpleTippQuery(String query, String property, List idList, Map<String, Object> result) {

        List<String> PROPERTY_QUERY = [
                'select tipp.' + property + ', tipp.' + property + ', count(*) ',
                ' and tipp.' + property + ' is not null and tipp.' + property + ' != \'\' group by tipp.' + property + ' order by tipp.' + property
        ]

        BaseQuery.handleGenericQuery(
                query,
                PROPERTY_QUERY[0] + 'from TitleInstancePackagePlatform tipp where tipp.id in (:idList)' + PROPERTY_QUERY[1],
                'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.' + property + ' = :d order by tipp.' + property,
                'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.' + property + ' is null or tipp.' + property + ' = \'\'',
                idList,
                result
        )
    }

    static void processSimpleTippRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

        List<String> PROPERTY_QUERY = [
                'select p.id, p.value_de, count(*) ',
                ' group by p.id, p.value_de order by p.value_de'
        ]

        BaseQuery.handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from TitleInstancePackagePlatform tipp join tipp.' + refdata + ' p where tipp.id in (:idList)' + PROPERTY_QUERY[1],
                'select tipp.id from TitleInstancePackagePlatform tipp join tipp.' + refdata + ' p where tipp.id in (:idList) and p.id = :d order by tipp.sortname',
                'select distinct tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.' + refdata + ' is null',
                idList,
                result
        )
    }

    static List<Subscription> getTimeline(Subscription sub) {
        List<Subscription> result = [sub]

        Closure<Subscription> getPrev = { s ->
            Links.executeQuery(
                    'select li.destinationSubscription from Links li where li.sourceSubscription = :sub and li.linkType = :linkType',
                    [sub: s, linkType: RDStore.LINKTYPE_FOLLOWS])[0]
        }
        Closure<Subscription> getNext = { s ->
            Links.executeQuery(
                    'select li.sourceSubscription from Links li where li.destinationSubscription = :sub and li.linkType = :linkType',
                    [sub: s, linkType: RDStore.LINKTYPE_FOLLOWS])[0]
        }

        Subscription tmp = sub
        while (tmp) {
            tmp = getPrev(tmp)
            if (tmp) { result.add(0, tmp) }
        }
        tmp = sub
        while (tmp) {
            tmp = getNext(tmp)
            if (tmp) { result.add(tmp) }
        }
        result
    }

    static String getMessage(String token) {
        MessageSource messageSource = BeanStore.getMessageSource()
        messageSource.getMessage('reporting.local.subscription.' + token, null, LocaleUtils.getCurrentLocale())
    }

    static String getQueryLabel(String qKey, List qValues) {
        //println 'getQueryLabel(): ' + qKey + ' - ' + qValues
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

        if (qValues[0].startsWith('generic')) {
            messageSource.getMessage('reporting.cfg.' + qValues[0], null, locale)
        } else {
            messageSource.getMessage('reporting.local.subscription.query.' + qKey, null, locale) // TODO
        }
    }
}
