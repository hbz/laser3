package de.laser.reporting.local

import de.laser.FinanceService
import de.laser.IssueEntitlement
import de.laser.Links
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.ctrl.FinanceControllerService
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.SubscriptionQuery
import de.laser.reporting.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat
import java.time.Year

class SubscriptionReporting {

    static String KEY = 'subscription'
    static String NO_DATE = 'Keine Angabe'
    static String NO_STARTDATE = 'Ohne Laufzeit-Beginn'
    static String NO_ENDDATE = 'Ohne Laufzeit-Ende'

    static Map<String, Object> CONFIG = [

            base : [
                    query: [
                            default: [
                                    'Bestand' : [
                                            'tipp-publisherName'    : 'Verlag',
                                            'tipp-seriesName'       : 'Name der Reihe',
                                            'tipp-subjectReference' : 'Fachbereich',
                                            'tipp-titleType'        : 'Titel-Typ',
                                            'tipp-medium'           : 'Medium',
                                            //'tipp-platform'         : 'Plattform',
                                            //'tipp-package'          : 'Paket'
                                    ]
                            ]
                    ],

                    query2: [
                            default: [
                                    'Entwicklung' : [
                                            'timeline-entitlement' : [
                                                    label : 'Bestand',
                                                    chart : '1axis3values',
                                                    chartLabels : [ 'Titel entfernt', 'Neue Titel', 'Aktuelle Titel' ]
                                            ]
                                    ]
                            ],
                            consAtcons: [ // TODO
                                    'Entwicklung' : [
                                            'timeline-member' : [
                                                    label : 'Teilnehmer',
                                                    chart : '1axis3values',
                                                    chartLabels : [ 'Teilnehmer entfernt', 'Neue Teilnehmer', 'Aktuelle Teilnehmer' ]
                                            ],
                                            'timeline-cost' : [
                                                    label : 'Teilnehmerkosten',
                                                    chart : 'cost',
                                                    chartLabels : [ 'Wert', 'Endpreis (nach Steuer)']
                                            ],
                                            'timeline-entitlement' : [
                                                    label : 'Bestand',
                                                    chart : '1axis3values',
                                                    chartLabels : [ 'Titel entfernt', 'Neue Titel', 'Aktuelle Titel' ]
                                            ],
                                            'timeline-annualMember-subscription' : [
                                                    label       : 'Jahresring → Teilnehmerlizenz',
                                                    chart       : 'annualMember',
                                                    chartLabels : ['Teilnehmerlizenzen']
                                            ],
                                    ]
                            ],

                    ]
            ]
    ]

    static Map<String, Object> getCurrentQuery2Config(Subscription sub) {
        if (sub.getConsortia()) {
            if (! sub.getAllSubscribers()) {
//                println '- consortium @ subscriptionCons'
                return SubscriptionReporting.CONFIG.base.query2.consAtcons
            }
            else {
//                println '- consortium @ subscriptionMember'
//                println '- subscriber @ subscriptionMember'
            }
        }
        else {
//            println '- locals'
        }
        SubscriptionReporting.CONFIG.base.query2.default
    }

    static List<String> getTimelineQueryLabels(GrailsParameterMap params) {
        List<String> meta = []

        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        Subscription sub = Subscription.get(params.id)

        CONFIG.base.query2.each { cats ->
            cats.value.each {it ->
                if (it.value.containsKey(params.query)) {
                    String sd = sub.startDate ? sdf.format(sub.startDate) : NO_STARTDATE
                    String ed = sub.endDate ? sdf.format(sub.endDate) : NO_ENDDATE
                    meta = [ it.key, it.value.get(params.query).label, "${sd} - ${ed}" ]
                }
            }
        }
        meta
    }
    static List<String> getTimelineQueryLabelsForAnnual(GrailsParameterMap params) {
        List<String> meta = []

        CONFIG.base.query2.each { cats ->
            cats.value.each {it ->
                if (it.value.containsKey(params.query)) {
                    meta = [ it.key, it.value.get(params.query).label, "${params.id}" ]
                }
            }
        }
        meta
    }

    static Map<String, Object> query(GrailsParameterMap params) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Map<String, Object> result = BaseQuery.getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        Long id = params.long('id')

        if (! id) {
        }
        else {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getTimeline(sub)

            if (params.query.startsWith('timeline')) {

                if (params.query == 'timeline-member') {
                    List<List<Long>> subIdLists = []

                    timeline.eachWithIndex { s, i ->
                        subIdLists.add(Subscription.executeQuery(
                                'select m.id from Subscription sub join sub.derivedSubscriptions m where sub = :sub', [sub: s]
                        ))
                        result.data.add([
                                s.id,
                                s.name,
                                sub == s,
                                s.startDate ? sdf.format(s.startDate) : NO_STARTDATE,
                                s.endDate ? sdf.format(s.endDate) : NO_ENDDATE,
                                subIdLists.get(i).size()
                        ])
                        result.dataDetails.add([
                                query: params.query,
                                id   : s.id,
                                label: ''
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

                            d[6] = dd.plusIdList.size()
                            d[7] = dd.minusIdList.size()
                        }
                        else {
                            List<Long> currMemberIdList = subIdLists.get(i) ? Org.executeQuery(orgHql, [idList: subIdLists.get(i), roleTypes: roleTypes]) : []

                            dd.idList = currMemberIdList
                            dd.plusIdList = currMemberIdList
                            dd.minusIdList = []

                            d[6] = dd.plusIdList.size()
                            d[7] = dd.minusIdList.size()
                        }
                    }
                }
                else if (params.query == 'timeline-entitlement') {
                    List<List<Long>> ieIdLists = []

                    timeline.eachWithIndex { s, i ->
                        ieIdLists.add(IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
                        ))
                        result.data.add([
                                s.id,
                                s.name,
                                sub == s,
                                s.startDate ? sdf.format(s.startDate) : NO_STARTDATE,
                                s.endDate ? sdf.format(s.endDate) : NO_ENDDATE,
                                ieIdLists.get(i).size()
                        ])
                        result.dataDetails.add([
                                query: params.query,
                                id   : s.id,
                                label: ''
                        ])
                    }

                    result.dataDetails.eachWithIndex { Map<String, Object> dd, i ->
                        List d = result.data.get(i)

                        String tippHql = 'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)'

                        if (i > 0) {
                            List<Long> currIdList = ieIdLists.get(i)
                            List<Long> prevIdList = ieIdLists.get(i - 1)

                            List<Long> currTippIdList = currIdList ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: currIdList]) : []
                            List<Long> prevTippIdList = prevIdList ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: prevIdList]) : []

                            dd.idList = currTippIdList
                            dd.plusIdList = currTippIdList.minus(prevTippIdList)
                            dd.minusIdList = prevTippIdList.minus(currTippIdList)

                            d[6] = dd.plusIdList.size()
                            d[7] = dd.minusIdList.size()
                        }
                        else {
                            List<Long> currTippIdList = ieIdLists.get(i) ? TitleInstancePackagePlatform.executeQuery(tippHql, [idList: ieIdLists.get(i)]) : []

                            dd.idList = currTippIdList
                            dd.plusIdList = currTippIdList
                            dd.minusIdList = []

                            d[6] = dd.plusIdList.size()
                            d[7] = dd.minusIdList.size()
                        }
                    }

                    //println result.dataDetails
                }
                else if (params.query == 'timeline-cost') {
                    GrailsParameterMap clone = params.clone() as GrailsParameterMap

                    FinanceService financeService = (FinanceService) Holders.grailsApplication.mainContext.getBean('financeService')
                    FinanceControllerService financeControllerService = (FinanceControllerService) Holders.grailsApplication.mainContext.getBean('financeControllerService')

                    timeline.eachWithIndex { s, i ->
                        clone.setProperty('id', s.id)
                        Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, financeControllerService.getResultGenerics(clone))

                        result.data.add([
                                s.id,
                                s.name,
                                sub == s,
                                s.startDate ? sdf.format(s.startDate) : NO_STARTDATE,
                                s.endDate ? sdf.format(s.endDate) : NO_ENDDATE,
                                finance.cons?.sums?.localSums?.localSum ?: 0,
                                finance.cons?.sums?.localSums?.localSumAfterTax ?: 0
                        ])
                        result.dataDetails.add([
                                query : params.query,
                                id    : s.id,
                                label : '',
                                idList: []
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

                    SubscriptionQuery.handleGenericAnnualXQuery(params.query, 'Subscription', subIdLists, result)

                    List newData = []
                    result.data.each { d ->
                        newData.add([
                            d[0], d[1],
                            (sub.startDate && sub.endDate) ? DateUtils.getYearAsInteger(sub.startDate) <= d[0] && DateUtils.getYearAsInteger(sub.endDate) >= d[0] : false,
                            d[1], d[2]
                        ])
                    }
                    result.data = newData
                }
            }

            else if (prefix == 'tipp') {

                List<TitleInstancePackagePlatform> idList = TitleInstancePackagePlatform.executeQuery(
                        'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id = :id and ie.status = :status and ie.acceptStatus = :acceptStatus',
                        [id: id, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
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
                                idList: Subscription.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.platform.id = :d order by tipp.sortName',
                                        [idList: idList, d: d[0]]
                                )
                        ])
                    }
                } */
                /* else if (params.query == 'tipp-package') {
                    result.data = Platform.executeQuery(
                            'select p.id, p.name, count(*) from TitleInstancePackagePlatform tipp join tipp.pkg p where tipp.id in (:idList) group by p.id order by p.name',
                            [idList: idList]
                    )
                    result.data.each { d ->
                        result.dataDetails.add([
                                query : params.query,
                                id    : d[0],
                                label : d[1],
                                idList: Subscription.executeQuery(
                                        'select tipp.id from TitleInstancePackagePlatform tipp where tipp.id in (:idList) and tipp.pkg.id = :d order by tipp.sortName',
                                        [idList: idList, d: d[0]]
                                )
                        ])
                    }
                } */

                result.put('objectReference', id) // workaround : XYZ
            }
        }
        result
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
                'select tipp.id from TitleInstancePackagePlatform tipp join tipp.' + refdata + ' p where tipp.id in (:idList) and p.id = :d order by tipp.sortName',
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
            if (tmp) { result.push(tmp) }
        }

        result
    }
}
