package de.laser.properties

import de.laser.Org
import de.laser.RefdataValue
import de.laser.wekb.Vendor
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated

/**
 * The class's name is what it does: a property (implicitly general) to a {@link Vendor}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * This flag is particularly important here because vendors are accessible by everyone.
 */
class VendorProperty extends AbstractPropertyWithCalculatedLastUpdated {

    PropertyDefinition type
    boolean isPublic = false

    String           stringValue
    Long             longValue
    BigDecimal       decValue
    RefdataValue     refValue
    URL              urlValue
    String           note = ""
    Date             dateValue
    Org              tenant

    Vendor owner

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id                   column: 'vp_id'
        version              column: 'vp_version'
        stringValue          column: 'vp_string_value', type: 'text'
        longValue            column: 'vp_long_value'
        decValue             column: 'vp_dec_value'
        refValue             column: 'vp_ref_value_rv_fk', index: 'vp_ref_value_idx'
        urlValue             column: 'vp_url_value'
        note                 column: 'vp_note', type: 'text'
        dateValue            column: 'vp_date_value'
        type                 column: 'vp_type_fk', index:'vp_type_idx'
        tenant               column: 'vp_tenant_fk', index:'vp_tenant_idx'
        owner                column: 'vp_owner_fk', index:'vp_owner_idx'
        isPublic             column: 'vp_is_public'
        dateCreated          column: 'vp_date_created'
        lastUpdated          column: 'vp_last_updated'
        lastUpdatedCascading column: 'vp_last_updated_cascading'
    }

    static constraints = {
        stringValue (nullable: true)
        longValue   (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: Vendor
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
