package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.properties.PropertyDefinition

/**Org custom properties are used to store Org related settings and options**/
class OrgCustomProperty extends CustomProperty {

    PropertyDefinition type
    Org owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes    AbstractPropertyWithCalculatedLastUpdated.mapping
        owner       index:'ocp_owner_idx'
        dateCreated column: 'ocp_date_created'
        lastUpdated column: 'ocp_last_updated'
    }

    static constraints = {
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: Org
    ]

    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
}
