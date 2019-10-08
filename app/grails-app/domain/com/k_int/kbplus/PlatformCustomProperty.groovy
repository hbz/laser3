package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.properties.PropertyDefinition

class PlatformCustomProperty extends CustomProperty {

    PropertyDefinition type
    Platform owner

    static mapping = {
        includes    AbstractProperty.mapping
        owner       index:'pcp_owner_idx'
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner : Platform
    ]
}
