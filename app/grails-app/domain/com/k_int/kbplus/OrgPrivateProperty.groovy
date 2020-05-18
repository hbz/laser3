package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition

/**Org private properties are used to store Org related settings and options only for specific memberships**/
class OrgPrivateProperty extends PrivateProperty {

    PropertyDefinition type
    Org owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes AbstractPropertyWithCalculatedLastUpdated.mapping

        id      column:'opp_id'
        version column:'opp_version'
        type    column:'opp_type_fk'
        owner   column:'opp_owner_fk', index:'opp_owner_idx'

        dateCreated column: 'opp_date_created'
        lastUpdated column: 'opp_last_updated'
    }

    static constraints = {
        importFrom AbstractPropertyWithCalculatedLastUpdated

        type    (nullable:false, blank:false)
        owner   (nullable:false, blank:false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  Org
    ]
}
