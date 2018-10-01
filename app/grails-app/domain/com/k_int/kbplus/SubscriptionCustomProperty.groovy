package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty
import de.laser.traits.AuditTrait

import javax.persistence.Transient

class SubscriptionCustomProperty extends CustomProperty implements AuditTrait {

    @Transient
    def changeNotificationService

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

    def copyValueAndNote(newProp){
        newProp = super.copyValueAndNote(newProp)
        newProp
    }

    @Transient
    def onDelete = { oldMap ->
        log.debug("onDelete SubscriptionCustomProperty")
        def oid = "${this.owner.class.name}:${this.owner.id}"
        def changeDoc = [ OID: oid,
                          event:'SubscriptionCustomProperty.deleted',
                          prop: "${this.type.name}",
                          old: "",
                          new: "property removed",
                          name: this.type.name
        ]
        //def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
        changeNotificationService.fireEvent(changeDoc)
    }
}
