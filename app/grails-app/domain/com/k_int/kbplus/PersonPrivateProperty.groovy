package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.PrivateProperty

/**Person private properties are used to store Person related settings and options only for specific memberships**/
class PersonPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Person owner

    static mapping = {
        id      column:'ppp_id'
        version column:'ppp_version'
        type    column:'ppp_type_fk'
        owner   column:'ppp_owner_fk'
    }

    static constraints = {
        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Person
    ]
}
