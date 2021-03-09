package de.laser.reporting.subscription

import de.laser.ContextService
import de.laser.IssueEntitlement
import de.laser.Links
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
                            label : 'Lizenz → Teilnehmer',
                            chart : 'bar',
                            chartLabels : []
                    ],
                    'subscription-entitlement-timeline' : [
                            label : 'Lizenz → Bestand',
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

            timeline.each{s ->
                result.data.add([
                        s.id,
                        s.name,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        Subscription.executeQuery(
                                'select count(m) from Subscription sub join sub.derivedSubscriptions m where sub = :sub',
                                [sub: s]
                        )[0],
                        sub == s
                ])
            }
        }
        else if (params.query == 'subscription-entitlement-timeline') {
            Subscription sub = Subscription.get(id)

            List<Subscription> timeline = getSubscriptionTimeline(sub)

            timeline.each{s ->
                result.data.add([
                        s.id,
                        s.name,
                        sdf.format(s.startDate),
                        sdf.format(s.endDate),
                        IssueEntitlement.executeQuery(
                                'select count(ie) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus',
                                [sub: s, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
                        )[0],
                        sub == s
                ])
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
