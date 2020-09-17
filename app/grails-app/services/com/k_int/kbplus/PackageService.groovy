package com.k_int.kbplus

import de.laser.LinksGenerationService
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class PackageService {

    LinksGenerationService linksGenerationService

    Map<String,Object> getTitlesForYearRings(Map<String, Object> configMap) {
        Set<String> labels = []
        List<Map> series = []
        Set<Subscription> linkedSubscriptionsOfCurrentYear = Subscription.executeQuery('select sub from SubscriptionPackage sp join sp.subscription sub join sub.orgRelations oo where sp.pkg = :package and sp.subscription.instanceOf is null and sub.status = :current and oo.org = :context',[package:Package.get(configMap.package),current:RDStore.SUBSCRIPTION_CURRENT,context:configMap.institution])
        Map<Subscription,List<Integer>> subscriptionTitles = [:]
        Calendar c = Calendar.getInstance()
        linkedSubscriptionsOfCurrentYear.each { Subscription currentSub ->
            List<Integer> yearRingTitle = []
            //in the following, we estimate that each subscription is linked to the concerned package only
            //Set<Subscription> precedingSubscriptions = Subscription.executeQuery('select li from Links li where :subscriptionOID = li.source and li.linkType = :linkType',[subscriptionOID:genericOIDService.getOID(currentSub),linkType: RDStore.LINKTYPE_FOLLOWS]).collect { li -> li.getOther(currentSub) }
            Set precedingSubscriptions = linksGenerationService.getSuccessionChain(currentSub, 'source')
            precedingSubscriptions.each { precObj ->
                Subscription prec = (Subscription) precObj
                c.setTime(prec.startDate)
                labels << c.get(Calendar.YEAR).toString()
                yearRingTitle << prec.issueEntitlements.size()
            }
            c.setTime(currentSub.startDate)
            labels << c.get(Calendar.YEAR).toString()
            yearRingTitle << currentSub.issueEntitlements.size()
            //Set<Subscription> followingSubscriptions = Subscription.executeQuery('select li from Links li where :subscriptionOID = li.destination and li.linkType = :linkType',[subscriptionOID:genericOIDService.getOID(currentSub),linkType: RDStore.LINKTYPE_FOLLOWS]).collect { li -> li.getOther(currentSub) }
            Set followingSubscriptions = linksGenerationService.getSuccessionChain(currentSub, 'destination')
            followingSubscriptions.each { follObj ->
                Subscription foll = (Subscription) follObj
                c.setTime(foll.startDate)
                labels << c.get(Calendar.YEAR).toString()
                yearRingTitle << foll.issueEntitlements.size()
            }
            subscriptionTitles.put(currentSub,yearRingTitle)
        }
        subscriptionTitles.each { Subscription k, List<Integer> v ->
            series.add([name:k.dropdownNamingConvention(configMap.institution),data:v])
        }
        [labels:labels,series:series]
    }

}