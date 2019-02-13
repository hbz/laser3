package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty
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

    // AuditableTrait
    static auditable = true
    static controlledProperties = ['stringValue','intValue','decValue','refValue','paragraph','note','dateValue']

    PropertyDefinition type
    License owner
    LicenseCustomProperty instanceOf
    String paragraph

    static mapping = {
        includes   AbstractProperty.mapping
        paragraph  type: 'text'
        owner      index:'lcp_owner_idx'
    }

    static constraints = {
        importFrom AbstractProperty
        instanceOf (nullable: true)
        paragraph  (nullable: true)
    }

    static belongsTo = [
        type : PropertyDefinition,
        owner: License
    ]

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
    def changeDoc = [ OID: oid,
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

            def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.subscription."+changeDocument.prop, locale.toString())
            def description = messageSource.getMessage('default.accept.placeholder',null, locale)
            if (contentItemDesc) {
                description = contentItemDesc.content
            }
            else {
                def defaultMsg = ContentItem.findByKeyAndLocale("kbplus.change.subscription.default", locale.toString())
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

            def slavedPendingChanges = []

            def depedingProps = LicenseCustomProperty.findAllByInstanceOf( this )
            depedingProps.each{ lcp ->

                def definedType = 'text'
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

                def newPendingChange =  changeNotificationService.registerPendingChange(
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
                if (newPendingChange && lcp.owner.isSlaved?.value == "Yes") {
                    slavedPendingChanges << newPendingChange
                }
            }

            slavedPendingChanges.each { spc ->
                log.debug('autoAccept! performing: ' + spc)
                def user = null
                pendingChangeService.performAccept(spc.getId(), user)
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('LicenseCustomProperty.deleted')) {

            def openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null" )
            openPD.each { pc ->
                def event = JSON.parse(pc.changeDoc)
                def scp = genericOIDService.resolveOID(event.changeDoc.OID)
                if (scp?.id == id) {
                    pc.delete(flush: true)
                }
            }
        }
    }
}
