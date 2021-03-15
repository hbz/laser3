package de.laser.reporting.subscription

import de.laser.ContextService
import de.laser.FinanceService
import de.laser.IssueEntitlement
import de.laser.Links
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
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
                    'subscription-member-timeline' : [
                            label : 'Entwicklung: Teilnehmer',
                            chart : 'bar',
                            chartLabels : [ 'Teilnehmer entfernt', 'Neue Teilnehmer', 'Aktuelle Teilnehmer' ]
                    ],
                    'subscription-entitlement-timeline' : [
                            label : 'Entwicklung: Bestand',
                            chart : 'bar',
                            chartLabels : [ 'Titel entfernt', 'Neue Titel', 'Aktuelle Titel' ]
                    ],
                    'subscription-costs-timeline' : [
                            label : 'Entwicklung: Kosten',
                            chart : 'bar',
                            chartLabels : [ 'Wert', 'Endpreis (nach Steuer)']
                    ]
            ]
    ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

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
        else if (params.query == 'subscription-member-timeline') {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getSubscriptionTimeline(sub)

            timeline.eachWithIndex{ s, i ->
                Map<String, Object> details = [
                        query   : params.query,
                        id      : s.id,
                        label   : '',
                        idList  : Subscription.executeQuery(
                                'select m.id from Subscription sub join sub.derivedSubscriptions m where sub = :sub',
                                [sub: s]
                        )
                ]
                result.dataDetails.add( details )

                result.data.add([
                        s.id,
                        s.name,
                        sub == s,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        details.idList.size() as Long
                ])
            }

            result.dataDetails.eachWithIndex{ Map<String, Object> dd, i ->
                List d = result.data.get(i)

                String orgHql = 'select distinct ro.org.id from Subscription s join s.orgRelations ro where s.id in (:idList) and ro.roleType in (:roleTypes)'
                List< RefdataValue> roleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]

                if (i>0) {
                    List<Long> currIdList = dd.idList
                    List<Long> prevIdList = result.dataDetails.get(i - 1).idList

                    List<Long> currMemberIdList = currIdList ? Org.executeQuery( orgHql, [idList: currIdList, roleTypes: roleTypes] ) : []
                    List<Long> prevMemberIdList = prevIdList ? Org.executeQuery( orgHql, [idList: prevIdList, roleTypes: roleTypes] ) : []

                    d[6] = currMemberIdList.minus(prevMemberIdList).size() // plus
                    d[7] = prevMemberIdList.minus(currMemberIdList).size() // minus
                }
                else {
                    List<Long> currMemberIdList = dd.idList ? Org.executeQuery( orgHql, [idList: dd.idList, roleTypes: roleTypes] ) : []

                    d[6] = currMemberIdList.size()
                    d[7] = 0
                }
            }
        }
        else if (params.query == 'subscription-entitlement-timeline') {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getSubscriptionTimeline(sub)

            timeline.eachWithIndex{ s, i  ->
                Map<String, Object> details = [
                        query   : params.query,
                        id      : s.id,
                        label   : '',
                        idList  : IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
                        )
                ]
                result.dataDetails.add( details )

                result.data.add([
                        s.id,
                        s.name,
                        sub == s,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        details.idList.size() as Long
                ])
            }

            result.dataDetails.eachWithIndex{ Map<String, Object> dd, i ->
                List d = result.data.get(i)

                String tippHql = 'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)'

                if (i>0) {
                    List<Long> currIdList = dd.idList
                    List<Long> prevIdList = result.dataDetails.get(i - 1).idList

                    List<Long> currTippIdList = currIdList ? Org.executeQuery( tippHql, [idList: currIdList] ) : []
                    List<Long> prevTippIdList = prevIdList ? Org.executeQuery( tippHql, [idList: prevIdList] ) : []

                    d[6] = currTippIdList.minus(prevTippIdList).size() // plus
                    d[7] = prevTippIdList.minus(currTippIdList).size() // minus
                }
                else {
                    List<Long> currTippIdList = dd.idList ? Org.executeQuery( tippHql, [idList: dd.idList] ) : []

                    d[6] = currTippIdList.size()
                    d[7] = 0
                }
            }
        }
        else if (params.query == 'subscription-costs-timeline') {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getSubscriptionTimeline(sub)

            FinanceService financeService = (FinanceService) Holders.grailsApplication.mainContext.getBean('financeService')
            FinanceControllerService financeControllerService = (FinanceControllerService) Holders.grailsApplication.mainContext.getBean('financeControllerService')

            GrailsParameterMap clone = params.clone() as GrailsParameterMap

            timeline.eachWithIndex { s, i ->
                clone.setProperty('id', s.id)
                Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, financeControllerService.getResultGenerics(clone))

                Map<String, Object> details = [
                        query   : params.query,
                        id      : s.id,
                        label   : '',
                        idList  : []
                ]
                result.dataDetails.add( details )

                result.data.add([
                    s.id,
                    s.name,
                    sub == s,
                    sdf.format(s.startDate),
                    sdf.format(s.endDate),
                    finance.cons?.sums?.localSums?.localSum ?: 0,
                    finance.cons?.sums?.localSums?.localSumAfterTax ?: 0
                ])
            }
//                data[8]  = finance.subscr?.sums?.localSums?.localSum ?: 0
//                data[9]  = finance.subscr?.sums?.localSums?.localSumAfterTax ?: 0
//                data[10] = finance.own?.sums?.localSums?.localSum ?: 0
//                data[11] = finance.own?.sums?.localSums?.localSumAfterTax ?: 0

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
