package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty

class SubscriptionCustomProperty extends CustomProperty {

    static mapping = {
        includes AbstractProperty.mapping
    }

    static belongsTo = [
        type:  PropertyDefinition,
        owner: Subscription
    ]

    def copyValueAndNote(newProp){
        newProp = super.copyValueAndNote(newProp)
        newProp
    }
}
