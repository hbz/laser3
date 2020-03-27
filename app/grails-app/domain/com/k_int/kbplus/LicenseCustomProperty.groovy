package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.properties.PropertyDefinition
import de.laser.traits.AuditableTrait
import grails.converters.JSON

import javax.persistence.Transient

class LicenseCustomProperty extends CustomProperty implements AuditableTrait  {

    @Transient
    def genericOIDService
    @Transient
    def changeNotificationService
    @Transient
    def grailsApplication
    @Transient
    def messageSource
    @Transient
    def pendingChangeService
    @Transient
    def deletionService

    // AuditableTrait
    static auditable = true
    static controlledProperties = ['stringValue','intValue','decValue','refValue','paragraph','note','dateValue']

    PropertyDefinition type
    License owner
    LicenseCustomProperty instanceOf
    String paragraph

    Date dateCreated
    Date lastUpdated

    static mapping = {
        includes   AbstractProperty.mapping
        paragraph  type: 'text'
        owner      index:'lcp_owner_idx'

        dateCreated column: 'lcp_date_created'
        lastUpdated column: 'lcp_last_updated'
    }

    static constraints = {
        importFrom AbstractProperty
        instanceOf (nullable: true)
        paragraph  (nullable: true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: License
    ]

    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id)
    }

    @Override
    def copyInto(AbstractProperty newProp){
        newProp = super.copyInto(newProp)

        newProp.paragraph = paragraph
        newProp
    }

    @Transient
    def onDelete = { oldMap ->
        log.debug("onDelete LicenseCustomProperty")

        //def oid = "${this.owner.class.name}:${this.owner.id}"
        def oid = "${this.class.name}:${this.id}"
        Map<String, Object> changeDoc = [ OID: oid,
                     event:'LicenseCustomProperty.deleted',
                     prop: "${this.type.name}",
                     old: "",
                     new: "property removed",
                     name: this.type.name
                     ]

        changeNotificationService.fireEvent(changeDoc)
    }

    def notifyDependencies_trait(changeDocument) {
        log.debug("notifyDependencies_trait(${changeDocument})")

        if (changeDocument.event.equalsIgnoreCase('LicenseCustomProperty.updated')) {
            // legacy ++

            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.subscription."+changeDocument.prop, locale.toString())
            def description = messageSource.getMessage('default.accept.placeholder',null, locale)
            if (contentItemDesc) {
                description = contentItemDesc.content
            }
            else {
                ContentItem defaultMsg = ContentItem.findByKeyAndLocale("kbplus.change.subscription.default", locale.toString())
                if( defaultMsg)
                    description = defaultMsg.content
            }

            def propName
            try {
                // UGLY
                propName = changeDocument.name ? ((messageSource.getMessage("license.${changeDocument.name}", null, locale)) ?: (changeDocument.name)) : (messageSource.getMessage("license.${changeDocument.prop}", null, locale) ?: (changeDocument.prop))

            } catch(Exception e) {
                propName = changeDocument.name ?: changeDocument.prop
            }

            // legacy ++

            List<PendingChange> slavedPendingChanges = []

            def depedingProps = LicenseCustomProperty.findAllByInstanceOf( this )
            depedingProps.each{ lcp ->

                String definedType = 'text'
                if (lcp.type.type == RefdataValue.class.toString()) {
                    definedType = 'rdv'
                }
                else if (lcp.type.type == Date.class.toString()) {
                    definedType = 'date'
                }

                // overwrite specials ..
                if (changeDocument.prop == 'note') {
                    definedType = 'text'
                    description = '(NOTE)'
                }
                else if (changeDocument.prop == 'paragraph') {
                    definedType = 'text'
                    description = '(PARAGRAPH)'
                }

                def msgParams = [
                        definedType,
                        "${lcp.type.class.name}:${lcp.type.id}",
                        (changeDocument.prop in ['note', 'paragraph'] ? "${changeDocument.oldLabel}" : "${changeDocument.old}"),
                        (changeDocument.prop in ['note', 'paragraph'] ? "${changeDocument.newLabel}" : "${changeDocument.new}"),
                        "${description}"
                ]

                PendingChange newPendingChange =  changeNotificationService.registerPendingChange(
                        PendingChange.PROP_LICENSE,
                        lcp.owner,
                        lcp.owner.getLicensee(),
                        [
                                changeTarget:"com.k_int.kbplus.License:${lcp.owner.id}",
                                changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                changeDoc:changeDocument
                        ],
                        PendingChange.MSG_LI02,
                        msgParams,
                        "Das Merkmal <b>${lcp.type.name}</b> hat sich von <b>\"${changeDocument.oldLabel?:changeDocument.old}\"</b> zu <b>\"${changeDocument.newLabel?:changeDocument.new}\"</b> von der Vertragsvorlage geändert. " + description
                )
                if (newPendingChange && lcp.owner.isSlaved) {
                    slavedPendingChanges << newPendingChange
                }
            }

            slavedPendingChanges.each { spc ->
                log.debug('autoAccept! performing: ' + spc)
                pendingChangeService.performAccept(spc)
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('LicenseCustomProperty.deleted')) {

            List<PendingChange> openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null and pc.oid = :objectID",
                    [objectID: "${this.class.name}:${this.id}"] )
            openPD.each { pc ->
                if (pc.payload) {
                    def payload = JSON.parse(pc.payload)
                    if (payload.changeDoc) {
                        def scp = genericOIDService.resolveOID(payload.changeDoc.OID)
                        if (scp?.id == id) {
                            pc.delete(flush: true)
                        }
                    }
                }
            }
        }
    }
}
