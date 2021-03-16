package de.laser.reporting.subscription

import de.laser.ContextService
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
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class SubscriptionReporting {

    static String KEY = 'subscription'

    static Map<String, Object> QUERY = [

            'Zeitleiste' : [
                    'member-timeline' : [
                            label : 'Entwicklung: Teilnehmer',
                            chart : 'bar',
                            chartLabels : [ 'Teilnehmer entfernt', 'Neue Teilnehmer', 'Aktuelle Teilnehmer' ]
                    ],
                    'entitlement-timeline' : [
                            label : 'Entwicklung: Bestand',
                            chart : 'bar',
                            chartLabels : [ 'Titel entfernt', 'Neue Titel', 'Aktuelle Titel' ]
                    ],
                    'cost-timeline' : [
                            label : 'Entwicklung: Kosten',
                            chart : 'bar',
                            chartLabels : [ 'Wert', 'Endpreis (nach Steuer)']
                    ]
            ]
    ]

    static List<String> getQueryLabels(GrailsParameterMap params) {
        List<String> meta = []

        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        Subscription sub = Subscription.get(params.id)

        QUERY.each {it ->
            if (it.value.containsKey(params.query)) {
                meta = [ it.key, it.value.get(params.query).label, "${sdf.format(sub.startDate)} - ${sdf.format(sub.endDate)}" ]
            }
        }
        meta
    }

    static Map<String, Object> query(GrailsParameterMap params) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Map<String, Object> result = [
                chart      : params.chart,
                query      : params.query,
                data       : [],
                dataDetails: []
        ]

        Long id = params.long('id')

        if (!id) {
        }
        else {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getSubscriptionTimeline(sub)

            if (params.query == 'member-timeline') {
                List<List<Long>> subIdLists = []

                timeline.eachWithIndex { s, i ->
                    subIdLists.add(Subscription.executeQuery(
                            'select m.id from Subscription sub join sub.derivedSubscriptions m where sub = :sub', [sub: s]
                    ))
                    result.data.add([
                            s.id,
                            s.name,
                            sub == s,
                            sdf.format(s.startDate),
                            sdf.format(s.endDate),
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
            else if (params.query == 'entitlement-timeline') {
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
                            sdf.format(s.startDate),
                            sdf.format(s.endDate),
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

                        dd.plusIdList = currTippIdList
                        dd.minusIdList = []

                        d[6] = dd.plusIdList.size()
                        d[7] = dd.minusIdList.size()
                    }
                }
            }
            else if (params.query == 'cost-timeline') {
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
                            sdf.format(s.startDate),
                            sdf.format(s.endDate),
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
        }
        result
    }

    static List<Subscription> getSubscriptionTimeline(Subscription sub) {
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
