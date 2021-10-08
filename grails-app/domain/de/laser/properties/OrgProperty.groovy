package de.laser.properties

import de.laser.Org
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated

/**
 * The class's name is what it does: a property (implicitly general) to a {@link de.laser.Org}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * This flag is particularly important here because organisations are accessible by everyone.
 */
class OrgProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    boolean isPublic = false

    String           stringValue
    Integer          intValue
    BigDecimal       decValue
    RefdataValue     refValue
    URL              urlValue
    String           note = ""
    Date             dateValue
    Org              tenant

    Org owner

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id                   column: 'op_id'
        version              column: 'op_version'
        stringValue          column: 'op_string_value', type: 'text'
        intValue             column: 'op_int_value'
        decValue             column: 'op_dec_value'
        refValue             column: 'op_ref_value_rv_fk'
        urlValue             column: 'op_url_value'
        note                 column: 'op_note', type: 'text'
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
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
        tenant      (nullable: true) //subject of discussion, for the moment, it cannot be determined exactly

        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: Org
    ]

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def beforeUpdate(){
        super.beforeUpdateHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }
}
