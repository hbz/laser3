package de.laser

import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional

@Transactional
class IssueEntitlementService {

    Subscription getTargetSubscription(Long subscriptionId) {
        Subscription s = Subscription.get(subscriptionId)
        if(s.instanceOf && s.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE)
            s.instanceOf
        s
    }

    Set<IssueEntitlement> getIssueEntitlements(Map configMap) {
        Subscription targetSub = getTargetSubscription(configMap.subscription)

    }
}
