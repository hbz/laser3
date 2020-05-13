package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.properties.PropertyDefinition

class PlatformCustomProperty extends CustomProperty {

    PropertyDefinition type
    Platform owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes    AbstractProperty.mapping
        owner       index:'pcp_owner_idx'

        dateCreated column: 'pcp_date_created'
        lastUpdated column: 'pcp_last_updated'
    }

    static constraints = {
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner : Platform
    ]
}
