package de.laser.properties

import de.laser.Org
import de.laser.PendingChangeService
import de.laser.Subscription
import de.laser.PendingChange
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import grails.plugins.orm.auditable.Auditable
import org.grails.web.json.JSONElement

/**
 * The class's name is what it does: a property (general / custom or private) to a {@link Subscription}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * As its parent object ({@link #owner}), it may be passed through member subscriptions (inheritance / auditable); the parent property is represented by {@link #instanceOf}.
 */
class SubscriptionProperty extends AbstractPropertyWithCalculatedLastUpdated implements Auditable {

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

    Subscription owner
    SubscriptionProperty instanceOf

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id          column: 'sp_id'
        version     column: 'sp_version'
        stringValue column: 'sp_string_value', type: 'text'
        intValue    column: 'sp_int_value'
        decValue    column: 'sp_dec_value'
        refValue    column: 'sp_ref_value_rv_fk'
        urlValue    column: 'sp_url_value'
        note        column: 'sp_note', type: 'text'
        dateValue   column: 'sp_date_value'
        instanceOf  column: 'sp_instance_of_fk', index: 'sp_instance_of_idx'
        owner       column: 'sp_owner_fk', index: 'sp_owner_idx'
        type        column: 'sp_type_fk', index: 'sp_type_idx'
        tenant      column: 'sp_tenant_fk', index: 'sp_tenant_idx'
        isPublic    column: 'sp_is_public'
        dateCreated column: 'sp_date_created'
        lastUpdated column: 'sp_last_updated'
        lastUpdatedCascading column: 'sp_last_updated_cascading'
    }

    static constraints = {
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
        instanceOf  (nullable: true)

        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type:  PropertyDefinition,
        owner: Subscription
    ]

    /**
     * Those are the fields watched by the inheritance
     * @return the {@link Collection} of watched field names
     */
    @Override
    Collection<String> getLogIncluded() {
        [ 'stringValue', 'intValue', 'decValue', 'refValue', 'note', 'dateValue' ]
    }

    /**
     * Those are the fields which are ignored by inheritance
     * @return the {@link Collection} of unwatched field names
     */
    @Override
    Collection<String> getLogExcluded() {
        [ 'version', 'lastUpdated', 'lastUpdatedCascading' ]
    }

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
        Map<String, Object> changes = super.beforeUpdateHandler()
        BeanStore.getAuditService().beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
        BeanStore.getAuditService().beforeDeleteHandler(this)
    }
    @Override
    def afterDelete() {
        super.afterDeleteHandler()
        BeanStore.getDeletionService().deleteDocumentFromIndex(BeanStore.getGenericOIDService().getOID(this), this.class.simpleName)
    }
}
