package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.properties.PropertyDefinition

class PlatformProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    Platform owner

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id          column: 'plp_id'
        version     column: 'plp_version'
        stringValue column: 'plp_string_value'
        intValue    column: 'plp_int_value'
        decValue    column: 'plp_dec_value'
        refValue    column: 'plp_ref_value_rv_fk'
        urlValue    column: 'plp_url_value'
        note        column: 'plp_note'
        dateValue   column: 'plp_date_value'
        owner       column: 'plp_owner_fk', index: 'plp_owner_idx'
        type        column: 'plp_type_fk', index: 'plp_type_idx'
        tenant      column: 'plp_tenant_fk', index: 'plp_tenant_idx'
        isPublic    column: 'plp_is_public'
        dateCreated column: 'plp_date_created'
        lastUpdated column: 'plp_last_updated'
        lastUpdatedCascading column: 'plp_last_updated_cascading'
    }

    static constraints = {
        importFrom  AbstractPropertyWithCalculatedLastUpdated
        tenant      (nullable: true, blank: false) //as no tenant can be determined for the moment, subject of discussion
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner : Platform
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
