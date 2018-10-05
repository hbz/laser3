package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import de.laser.traits.AuditTrait

import javax.persistence.Transient

class LicensePrivateProperty extends PrivateProperty {

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
    def copyInto(newProp){
        newProp = super.copyInto(newProp)

        newProp.paragraph = paragraph
        newProp
    }

    @Transient
    def onDelete = { oldMap ->
        log.debug("onDelete LicensePrivateProperty")
        def oid = "${this.owner.class.name}:${this.owner.id}"
        def changeDoc = [ OID: oid,
                         event:'LicensePrivateProperty.deleted',
                         prop: "${this.type.name}",
                         old: "",
                         new: "property removed",
                         name: this.type.name
                         ]
        def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
        // changeNotificationService.broadcastEvent("com.k_int.kbplus.License:${owner.id}", changeDoc);
        changeNotificationService.fireEvent(changeDoc)
    }
}
