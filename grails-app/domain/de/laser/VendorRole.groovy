package de.laser

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
        sharedFrom column: 'vr_shared_from_fk'
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
    int compareTo(VendorRole v) {
        int result = vendor.sortname <=> v.vendor.sortname
        if(!result && subscription && v.subscription)
            result = subscription.name <=> v.subscription.name
        else if(!result && license && v.license)
            result = license.reference <=> v.license.reference
        if(!result)
            id <=> v.id
        result
    }
}
