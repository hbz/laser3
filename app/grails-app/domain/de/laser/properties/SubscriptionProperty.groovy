package de.laser.properties

import de.laser.ContentItem
import com.k_int.kbplus.Org
import com.k_int.kbplus.PendingChangeService
import com.k_int.kbplus.Subscription
import de.laser.PendingChange
import de.laser.RefdataValue
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.interfaces.AuditableSupport
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement

class SubscriptionProperty extends AbstractPropertyWithCalculatedLastUpdated implements AuditableSupport {

    def genericOIDService
    def changeNotificationService
    def messageSource
    def pendingChangeService
    def deletionService
    def auditService

    static auditable            = [ ignore: ['version', 'lastUpdated', 'lastUpdatedCascading'] ]
    static controlledProperties = ['stringValue','intValue','decValue','refValue','note','dateValue']

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

    @Override
    Collection<String> getLogIncluded() {
        [ 'stringValue', 'intValue', 'decValue', 'refValue', 'note', 'dateValue' ]
    }
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

        auditService.beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()

        auditService.beforeDeleteHandler(this)
    }
    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id)
    }

    def notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")

        if (changeDocument.event.equalsIgnoreCase('SubscriptionProperty.updated')) {

            // legacy ++

            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.subscription."+changeDocument.prop, locale.toString())
            String description = messageSource.getMessage('default.accept.placeholder',null, locale)
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

                def msgParams = [
                        definedType,
                        "${scp.type.class.name}:${scp.type.id}",
                        (changeDocument.prop in ['note'] ? "${changeDocument.oldLabel}" : "${changeDocument.old}"),
                        (changeDocument.prop in ['note'] ? "${changeDocument.newLabel}" : "${changeDocument.new}"),
                        "${description}"
                ]

                PendingChange newPendingChange = changeNotificationService.registerPendingChange(
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
                pendingChangeService.performAccept(spc)
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('SubscriptionProperty.deleted')) {

            List<PendingChange> openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null and pc.payload is not null and pc.oid = :objectID",
                    [objectID: "${this.class.name}:${this.id}"] )
            openPD.each { pc ->
                if (pc.payload) {
                    JSONElement payload = JSON.parse(pc.payload)
                    if (payload.changeDoc) {
                        def scp = genericOIDService.resolveOID(payload.changeDoc.OID)
                        if (scp?.id == id) {
                            pc.delete(flush:true)
                        }
                    }
                }
            }
        }

    }
}
