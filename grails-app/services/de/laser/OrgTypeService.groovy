package de.laser


import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional

/**
 * This service is specifically for organisation type calls
 */
@Transactional
class OrgTypeService {

    ContextService contextService

    /**
     * @return List<Org> with orgType in ('Agency, Broker, Content Provider, Provider, Vendor'); generic
     */
    Collection<Org> getOrgsForTypeLicensor() {
        Set<Org> result = Org.executeQuery("select o from Org o join o.orgType as rt where rt.value in ('Agency', 'Broker', 'Content Provider', 'Provider', 'Vendor') order by o.name, lower(o.sortname)")
        result
    }

    /**
     * @return List<Long> with accessible (my) subscription ids
     */
    List<Long> getCurrentSubscriptionIds(Org context) {
        return Subscription.executeQuery("select oo.sub.id from OrgRole oo where oo.org = :subOrg and oo.roleType in (:roleTypes)", [subOrg: context, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]])
    }
}
