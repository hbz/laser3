package de.laser.wekb

import de.laser.License
import de.laser.Subscription
import de.laser.traits.ShareableTrait
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

class ProviderRole implements ShareableTrait, Comparable<ProviderRole> {

    Date dateCreated
    Date lastUpdated
    Boolean isShared = false
    ProviderRole sharedFrom

    static belongsTo = [
            provider: Provider,
            license: License,
            subscription: Subscription
    ]

    static mapping = {
        id column: 'pr_id'
        version column: 'pr_version'
        provider column: 'pr_provider_fk', index: 'pr_provider_idx'
        license column: 'pr_license_fk', index: 'pr_license_idx'
        subscription column: 'pr_subscription_fk', index: 'pr_subscription_idx'
        isShared column: 'pr_is_shared'
        sharedFrom column: 'pr_shared_from_fk',index:'pr_shared_from_idx'
        dateCreated column: 'pr_date_created'
        lastUpdated column: 'pr_last_updated'
    }

    static constraints = {
        sharedFrom (nullable: true)
        license (nullable: true)
        subscription (nullable: true)
    }

    void beforeDelete(PostUpdateEvent event) {
        deleteShare_trait()
    }

    @Override
    int compareTo(ProviderRole pr) {
        int result = provider <=> pr.provider
        if(!result && subscription && pr.subscription)
            result = subscription.name <=> pr.subscription.name
        else if(!result && license && pr.license)
            result = license.reference <=> pr.license.reference
        if(!result)
            id <=> pr.id
        result
    }
}
