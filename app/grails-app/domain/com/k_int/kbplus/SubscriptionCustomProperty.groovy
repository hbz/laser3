package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty
import de.laser.AuditConfig
import de.laser.traits.AuditTrait
import grails.converters.JSON

import javax.persistence.Transient

class SubscriptionCustomProperty extends CustomProperty implements AuditTrait {

    @Transient
    def genericOIDService
    @Transient
    def changeNotificationService
    @Transient
    def messageSource

    // AuditTrait
    static auditable = true
    static controlledProperties = ['stringValue','intValue','decValue','refValue','paragraph','note','dateValue']

    PropertyDefinition type
    Subscription owner
    SubscriptionCustomProperty instanceOf

    static mapping = {
        includes AbstractProperty.mapping
    }

    static constraints = {
        importFrom  AbstractProperty
        instanceOf (nullable: true)
    }

    static belongsTo = [
        type:  PropertyDefinition,
        owner: Subscription
    ]

    def copyInto(newProp){
        newProp = super.copyInto(newProp)
        newProp
    }

    @Transient
    def onDelete = { oldMap ->
        log.debug("onDelete SubscriptionCustomProperty")

        //def oid = "${this.owner.class.name}:${this.owner.id}"
        def oid = "${this.class.name}:${this.id}"
        def changeDoc = [ OID: oid,
                          event:'SubscriptionCustomProperty.deleted',
                          prop: "${this.type.name}",
                          old: "",
                          new: "property removed",
                          name: this.type.name
        ]

        changeNotificationService.fireEvent(changeDoc)
    }

    def notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")

        if (changeDocument.event.equalsIgnoreCase('SubscriptionCustomProperty.updated')) {

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

            // legacy ++

            def all = SubscriptionCustomProperty.findAllByInstanceOf( this )
            all.each{ depends ->

                changeNotificationService.registerPendingChange('subscription',
                        depends.owner,
                        // pendingChange.message_SU01
                        "<b>${depends.type.name}</b> hat sich von <b>\"${changeDocument.oldLabel?:changeDocument.old}\"</b> zu <b>\"${changeDocument.newLabel?:changeDocument.new}\"</b> von der Lizenzvorlage geändert. " + description,
                        depends.owner.getSubscriber(),
                        [
                                changeTarget:"com.k_int.kbplus.Subscription:${depends.owner.id}",
                                changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                changeDoc:changeDocument
                        ])
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('SubscriptionCustomProperty.deleted')) {

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
