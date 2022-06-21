package de.laser.properties

import de.laser.ContentItem
import de.laser.Org
import de.laser.PendingChangeService
import de.laser.Subscription
import de.laser.PendingChange
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import grails.converters.JSON
import grails.plugins.orm.auditable.Auditable
import org.grails.web.json.JSONElement
import org.springframework.context.i18n.LocaleContextHolder

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

        dateCreated (nullable: true)
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

    /**
     * This method is used by generic access method and reflects changes made to a subscription property to inheriting subscription properties.
     * @param changeDocument the map of changes being passed through to the inheriting properties
     */
    def notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")

        if (changeDocument.event.equalsIgnoreCase('SubscriptionProperty.updated')) {

            // legacy ++

            Locale locale = LocaleContextHolder.getLocale()
            ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.subscription."+changeDocument.prop, locale.toString())
            String description = BeanStore.getMessageSource().getMessage('default.accept.placeholder',null, locale)
            if (contentItemDesc) {
                description = contentItemDesc.content
            }
            else {
                ContentItem defaultMsg = ContentItem.findByKeyAndLocale("kbplus.change.subscription.default", locale.toString())
                if( defaultMsg)
                    description = defaultMsg.content
            }

            // legacy ++

            List<PendingChange> slavedPendingChanges = []

            List<SubscriptionProperty> depedingProps = SubscriptionProperty.findAllByInstanceOf( this )
            depedingProps.each{ scp ->

                String definedType = 'text'
                if (scp.type.isRefdataValueType()) {
                    definedType = 'rdv'
                }
                else if (scp.type.isDateType()) {
                    definedType = 'date'
                }

                // overwrite specials ..
                if (changeDocument.prop == 'note') {
                    definedType = 'text'
                    description = '(NOTE)'
                }

                List<String> msgParams = [
                        definedType,
                        "${scp.type.class.name}:${scp.type.id}",
                        (changeDocument.prop == 'note' ? "${changeDocument.oldLabel}" : "${changeDocument.old}"),
                        (changeDocument.prop == 'note' ? "${changeDocument.newLabel}" : "${changeDocument.new}"),
                        "${description}"
                ]

                PendingChange newPendingChange = BeanStore.getChangeNotificationService().registerPendingChange(
                        PendingChange.PROP_SUBSCRIPTION,
                        scp.owner,
                        scp.owner.getSubscriber(),
                        [
                                changeTarget:"${Subscription.class.name}:${scp.owner.id}",
                                changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                changeDoc:changeDocument
                        ],
                        PendingChange.MSG_SU02,
                        msgParams,
                        "Das Merkmal <strong>${scp.type.name}</strong> hat sich von <strong>\"${changeDocument.oldLabel?:changeDocument.old}\"</strong> zu <strong>\"${changeDocument.newLabel?:changeDocument.new}\"</strong> von der Lizenzvorlage geändert. " + description
                )
                if (newPendingChange && scp.owner.isSlaved) {
                    slavedPendingChanges << newPendingChange
                }
            }

            slavedPendingChanges.each { spc ->
                log.debug('autoAccept! performing: ' + spc)
                BeanStore.getPendingChangeService().performAccept(spc)
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('SubscriptionProperty.deleted')) {

            List<PendingChange> openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null and pc.payload is not null and pc.oid = :objectID",
                    [objectID: "${this.class.name}:${this.id}"] )
            openPD.each { pc ->
                if (pc.payload) {
                    JSONElement payload = JSON.parse(pc.payload)
                    if (payload.changeDoc) {
                        def scp = BeanStore.getGenericOIDService().resolveOID(payload.changeDoc.OID)
                        if (scp?.id == id) {
                            pc.delete()
                        }
                    }
                }
            }
        }

    }
}
