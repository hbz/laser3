package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty

class SubscriptionCustomProperty extends CustomProperty {

    static belongsTo = [
        type:  PropertyDefinition,
        owner: Subscription
    ]
}
