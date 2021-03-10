package de.laser.reporting.subscription

import de.laser.ContextService
import de.laser.IssueEntitlement
import de.laser.Links
import de.laser.Org
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

class SubscriptionReportingManager {

    static String KEY = 'subscription'

    static Map<String, Object> QUERY = [

            'Zeitleiste' : [
                    'subscription-member-timeline' : [
                            label : 'Datum → Teilnehmer',
                            chart : 'bar',
                            chartLabels : []
                    ],
                    'subscription-entitlement-timeline' : [
                            label : 'Datum → Bestand',
                            chart : 'bar',
                            chartLabels : []
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
                result.data.add([
                        s.id,
                        s.name,
                        sub == s,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        Subscription.executeQuery(
                                'select m.id from Subscription sub join sub.derivedSubscriptions m where sub = :sub',
                                [sub: s]
                        ),
                ])
            }
            result.data.eachWithIndex{ data, i ->
                if (i>0) {
                    List<Long> currIdList = data[5]
                    List<Long> prevIdList = result.data.get(i - 1)[5]

                    List<Long> currMemberIdList = currIdList ? Org.executeQuery(
                            'select distinct ro.org.id from Subscription s join s.orgRelations ro where s.id in (:idList) and ro.roleType in (:roleTypes)',
                            [idList: currIdList, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                    ) : []
                    List<Long> prevMemberIdList = prevIdList ? Org.executeQuery(
                            'select distinct ro.org.id from Subscription s join s.orgRelations ro where s.id in (:idList) and ro.roleType in (:roleTypes)',
                            [idList: prevIdList, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                    ) : []

                    data[6] = currMemberIdList.size()
                    data[7] = currMemberIdList.minus(prevMemberIdList).size() // plus
                    data[8] = prevMemberIdList.minus(currMemberIdList).size() // minus
                }
                else {
                    List<Long> currMemberIdList = data[5] ? Org.executeQuery(
                            'select distinct ro.org.id from Subscription s join s.orgRelations ro where s.id in (:idList) and ro.roleType in (:roleTypes)',
                            [idList: data[5], roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                    ) : []

                    data[6] = currMemberIdList.size()
                    data[7] = currMemberIdList.size()
                    data[8] = 0
                }
            }
        }
        else if (params.query == 'subscription-entitlement-timeline') {
            Subscription sub = Subscription.get(id)
            List<Subscription> timeline = getSubscriptionTimeline(sub)

            timeline.eachWithIndex{ s, i  ->
                result.data.add([
                        s.id,
                        s.name,
                        sub == s,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
                        )
                ])
            }

            result.data.eachWithIndex{ data, i ->
                if (i>0) {
                    List<Long> currIdList = data[5]
                    List<Long> prevIdList = result.data.get(i - 1)[5]

                    List<Long> currTippIdList = currIdList ? Org.executeQuery(
                            'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)',
                            [idList: currIdList]
                    ) : []
                    List<Long> prevTippIdList = prevIdList ? Org.executeQuery(
                            'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)',
                            [idList: prevIdList]
                    ) : []

                    data[6] = currTippIdList.size()
                    data[7] = currTippIdList.minus(prevTippIdList).size() // plus
                    data[8] = prevTippIdList.minus(currTippIdList).size() // minus
                }
                else {
                    List<Long> currTippIdList = data[5] ? Org.executeQuery(
                            'select tipp.id from IssueEntitlement ie join ie.tipp tipp where ie.id in (:idList)',
                            [idList: data[5]]
                    ) : []

                    data[6] = currTippIdList.size()
                    data[7] = currTippIdList.size()
                    data[8] = 0
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
