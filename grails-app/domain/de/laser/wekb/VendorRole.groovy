package de.laser.wekb

import de.laser.License
import de.laser.Subscription
import de.laser.traits.ShareableTrait
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

class VendorRole implements ShareableTrait, Comparable<VendorRole> {

    Date dateCreated
    Date lastUpdated
    Boolean isShared = false
    VendorRole sharedFrom

    static belongsTo = [
            vendor: Vendor,
            license: License,
            subscription: Subscription
    ]

    static mapping = {
        id column: 'vr_id'
        version column: 'vr_version'
        vendor column: 'vr_vendor_fk', index: 'vr_vendor_idx'
        license column: 'vr_license_fk', index: 'vr_license_idx'
        subscription column: 'vr_subscription_fk', index: 'vr_subscription_idx'
        isShared column: 'vr_is_shared'
        sharedFrom column: 'vr_shared_from_fk',index:'vr_shared_from_idx'
        dateCreated column: 'vr_date_created'
        lastUpdated column: 'vr_last_updated'
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
    int compareTo(VendorRole vr) {
        int result = vendor <=> vr.vendor
        if(!result && subscription && vr.subscription)
            result = subscription.name <=> vr.subscription.name
        else if(!result && license && vr.license)
            result = license.reference <=> vr.license.reference
        if(!result)
            id <=> vr.id
        result
    }
}
