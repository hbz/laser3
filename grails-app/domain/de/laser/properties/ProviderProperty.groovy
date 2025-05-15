package de.laser.properties

import de.laser.Org
import de.laser.wekb.Provider
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated

/**
 * The class's name is what it does: a property (implicitly general) to a {@link Provider}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * This flag is particularly important here because vendors are accessible by everyone.
 */
class ProviderProperty extends AbstractPropertyWithCalculatedLastUpdated {

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

    Provider owner

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id                   column: 'prp_id'
        version              column: 'prp_version'
        stringValue          column: 'prp_string_value', type: 'text'
        longValue            column: 'prp_long_value'
        decValue             column: 'prp_dec_value'
        refValue             column: 'prp_ref_value_rv_fk', index: 'prp_ref_value_idx'
        urlValue             column: 'prp_url_value'
        note                 column: 'prp_note', type: 'text'
        dateValue            column: 'prp_date_value'
        type                 column: 'prp_type_fk', index:'prp_type_idx'
        tenant               column: 'prp_tenant_fk', index:'prp_tenant_idx'
        owner                column: 'prp_owner_fk', index:'prp_owner_idx'
        isPublic             column: 'prp_is_public'
        dateCreated          column: 'prp_date_created'
        lastUpdated          column: 'prp_last_updated'
        lastUpdatedCascading column: 'prp_last_updated_cascading'
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
        owner: Provider
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
