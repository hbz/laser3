package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.PrivateProperty

/**Org private properties are used to store Org related settings and options only for specific memberships**/
class OrgPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Org owner

    static mapping = {
        id      column:'opp_id'
        version column:'opp_version'
        type    column:'opp_type_fk'
        owner   column:'opp_owner_fk'
    }

    static constraints = {
        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Org
    ]
}
