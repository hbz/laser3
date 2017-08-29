package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty

/**SystemAdmin custom properties are used to store system related settings and options**/
class SystemAdminCustomProperty extends CustomProperty {

    PropertyDefinition type
    SystemAdmin owner

    static belongsTo = [
        type : PropertyDefinition,
        owner: SystemAdmin
    ]
}
