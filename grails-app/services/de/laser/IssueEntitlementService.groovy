package de.laser

import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional

@Transactional
class IssueEntitlementService {

    FilterService filterService

    Subscription getTargetSubscription(Long subscriptionId) {
        Subscription s = Subscription.get(subscriptionId)
        if(s.instanceOf && s.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE)
            s.instanceOf
        s
    }

    Set<IssueEntitlement> getIssueEntitlements(Map params) {
        Subscription targetSub = getTargetSubscription(params.long('id'))
        Set<Package> targetPkg = targetSub.packages.pkg
        Map<String, Object> configMap = [subscription: targetSub, packages: targetPkg, ieStatus: params.ieStatus]
        Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(configMap)
        Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams), ieIds = []
        tippIds.collate(65000).each { List<Long> subset ->
            ieIds.addAll(IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie join ie.tipp tipp where tipp.id in (:subset) and ie.subscription = :subscription order by tipp.sortname', [subset: subset, subscription: targetSub]))
        }
        //test A
        SortedSet<IssueEntitlement> result = new TreeSet<IssueEntitlement>()
        /*
        Set<Long> ieSubset = ieIds.drop(0).take(10)
        result.addAll(IssueEntitlement.findAllByIdInList(ieSubset))
        */
        //test B: test for export
        /*
        Set<IssueEntitlement> result = []
        ieIds.eachWithIndex { Long ieId, int i ->
            println "processing entry ${i}"
            result << IssueEntitlement.get(ieId)
        }
        */
        result
    }
}
