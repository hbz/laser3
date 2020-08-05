package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.properties.PropertyDefinition

/**Org custom properties are used to store Org related settings and options**/
class OrgProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    Org owner
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id                   column: 'op_id'
        version              column: 'op_version'
        stringValue          column: 'op_string_value'
        intValue             column: 'op_int_value'
        decValue             column: 'op_dec_value'
        refValue             column: 'op_ref_value_rv_fk'
        urlValue             column: 'op_url_value'
        note                 column: 'op_note'
        dateValue            column: 'op_date_value'
        type                 column: 'op_type_fk', index:'op_type_idx'
        tenant               column: 'op_tenant_fk', index:'op_tenant_idx'
        owner                column: 'op_owner_fk', index:'op_owner_idx'
        isPublic             column: 'op_is_public'
        dateCreated          column: 'op_date_created'
        lastUpdated          column: 'op_last_updated'
        lastUpdatedCascading column: 'op_last_updated_cascading'
    }

    static constraints = {
        importFrom AbstractPropertyWithCalculatedLastUpdated
        tenant (nullable: true, blank: false) //subject of discussion, for the moment, it cannot be determined exactly
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
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
