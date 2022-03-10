package de.laser.properties

import de.laser.Org
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated

/**
 * The class's name is what it does: a property (implicitly general) to a {@link de.laser.Platform}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * This flag is particularly important here because platforms are accessible by everyone.
 */
class PlatformProperty extends AbstractPropertyWithCalculatedLastUpdated {

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

    Platform owner

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id          column: 'plp_id'
        version     column: 'plp_version'
        stringValue column: 'plp_string_value', type: 'text'
        intValue    column: 'plp_int_value'
        decValue    column: 'plp_dec_value'
        refValue    column: 'plp_ref_value_rv_fk'
        urlValue    column: 'plp_url_value'
        note        column: 'plp_note', type: 'text'
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
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
        tenant      (nullable: true) //as no tenant can be determined for the moment, subject of discussion

        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner : Platform
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
