package de.laser.properties

import de.laser.License
import de.laser.Org
import de.laser.PendingChangeService
import de.laser.PendingChange
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import grails.plugins.orm.auditable.Auditable
import org.grails.web.json.JSONElement
import org.springframework.context.MessageSource

/**
 * The class's name is what it does: a property (general / custom or private) to a {@link de.laser.License}.
 * The flag whether it is visible by everyone or not is determined by the {@link #isPublic} flag.
 * As its parent object ({@link #owner}), it may be passed through member subscriptions (inheritance / auditable); the parent property is represented by {@link #instanceOf}.
 * Next to the property value, it may continue a {@link #paragraph} of an underlying contract text.
 */
class LicenseProperty extends AbstractPropertyWithCalculatedLastUpdated implements Auditable {

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

    License owner
    LicenseProperty instanceOf
    String paragraph

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id          column: 'lp_id'
        version     column: 'lp_version'
        stringValue column: 'lp_string_value', type: 'text'
        intValue    column: 'lp_int_value'
        decValue    column: 'lp_dec_value'
        refValue    column: 'lp_ref_value_rv_fk'
        urlValue    column: 'lp_url_value'
        note        column: 'lp_note', type: 'text'
        dateValue   column: 'lp_date_value'
        instanceOf  column: 'lp_instance_of_fk', index: 'lp_instance_of_idx'
        paragraph   column: 'lp_paragraph', type: 'text'
        owner       column: 'lp_owner_fk', index:'lcp_owner_idx'
        type        column: 'lp_type_fk', index: 'lp_type_idx'
        tenant      column: 'lp_tenant_fk', index: 'lp_tenant_idx'
        isPublic    column: 'lp_is_public'
        dateCreated column: 'lp_date_created'
        lastUpdated column: 'lp_last_updated'
        lastUpdatedCascading column: 'lp_last_updated_cascading'
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
        paragraph   (nullable: true)

        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: License
    ]

    /**
     * The list of fields watched for inheritance
     * @return a {@link Collection} of field names watched
     */
    @Override
    Collection<String> getLogIncluded() {
        [ 'stringValue', 'intValue', 'decValue', 'refValue', 'paragraph', 'note', 'dateValue' ]
    }

    /**
     * The list of fields disregarded for inheritance
     * @return a {@link Collection} of field names excluded from inheritance
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

    /**
     * Extends the superclass method by the license paragraph
     * @param newProp the new license property to be processed
     * @return the property enriched with this copy base's values and the paragraph
     */
    @Override
    def copyInto(AbstractPropertyWithCalculatedLastUpdated newProp){
        newProp = super.copyInto(newProp)

        newProp.paragraph = paragraph
        newProp
    }
}
