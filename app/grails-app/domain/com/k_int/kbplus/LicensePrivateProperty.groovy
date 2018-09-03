package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import de.laser.traits.AuditTrait

import javax.persistence.Transient

class LicensePrivateProperty extends PrivateProperty /* implements AuditTrait */ {

    // AuditTrait
    // static auditable = true
    // static controlledProperties = ['stringValue','intValue','decValue','refValue','paragraph','note','dateValue']

    @Transient
    String paragraph
    @Transient
    def grailsApplication
    @Transient
    def messageSource
    @Transient
    def changeNotificationService

    PropertyDefinition type
    License owner

    static mapping = {
        includes AbstractProperty.mapping

        id      column:'lpp_id'
        version column:'lpp_version'
        owner   column:'lpp_owner_fk'
        type    column:'lpp_type_fk'

        paragraph type:'text'
    }

    static constraints = {
        importFrom AbstractProperty

        paragraph (nullable:true)
        owner     (nullable:false, blank:false)
    }

    static belongsTo = [
        type:   PropertyDefinition,
        owner:  License
    ]

    @Override
    def copyValueAndNote(newProp){
        newProp = super.copyValueAndNote(newProp)

        newProp.paragraph = paragraph
        newProp
    }

    /*
    @Transient
    def onChange = { oldMap,newMap ->
        log.debug("onChange LicensePrivateProperty")

        def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
        controlledProperties.each{ cp->
            if ( oldMap[cp] != newMap[cp] ) {
                log.debug("Change found on ${this.class.name}:${this.id}.")
                changeNotificationService.notifyChangeEvent([
                    OID: "${this.owner.class.name}:${this.owner.id}",
                    event:'PrivateProperty.updated',
                    prop: cp,
                    name: type.name,
                    type: this."${cp}".getClass().toString(),
                    old: oldMap[cp] instanceof RefdataValue? oldMap[cp].toString() : oldMap[cp],
                    new: newMap[cp] instanceof RefdataValue? newMap[cp].toString() : newMap[cp],
                    propertyOID: "${this.class.name}:${this.id}"
                ])
            }
        }
    }
 */

    @Transient
    def onDelete = { oldMap ->
        log.debug("onDelete LicensePrivateProperty")
        def oid = "${this.owner.class.name}:${this.owner.id}"
        def changeDoc = [ OID: oid,
                         event:'PrivateProperty.deleted',
                         prop: "${this.type.name}",
                         old: "",
                         new: "property removed",
                         name: this.type.name
                         ]
        def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
        // changeNotificationService.broadcastEvent("com.k_int.kbplus.License:${owner.id}", changeDoc);
        changeNotificationService.notifyChangeEvent(changeDoc)
    }
}
