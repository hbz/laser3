package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition

class SubscriptionPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Subscription owner

    static mapping = {
        includes AbstractProperty.mapping

        id      column:'spp_id'
        version column:'spp_version'
        type    column:'spp_type_fk'
        owner   column:'spp_owner_fk'
    }

    static belongsTo = [
            type:  PropertyDefinition,
            owner: Subscription
    ]

    static constraints = {
        importFrom AbstractProperty

        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)
    }

    def copyValueAndNote(newProp){
        newProp = super.copyValueAndNote(newProp)
        newProp
    }
}
