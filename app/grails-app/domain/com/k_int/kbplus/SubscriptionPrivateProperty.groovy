package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition

class SubscriptionPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Subscription owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes AbstractProperty.mapping

        id      column:'spp_id'
        version column:'spp_version'
        type    column:'spp_type_fk'
        owner   column:'spp_owner_fk', index:'spp_owner_idx'

        dateCreated column: 'spp_date_created'
        lastUpdated column: 'spp_last_updated'
    }

    static belongsTo = [
            type:  PropertyDefinition,
            owner: Subscription
    ]

    static constraints = {
        importFrom AbstractProperty

        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}
