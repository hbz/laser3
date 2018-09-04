package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty
import de.laser.traits.AuditTrait

import javax.persistence.Transient

class LicenseCustomProperty extends CustomProperty implements AuditTrait  {

    @Transient
    def changeNotificationService

    // AuditTrait
    static auditable = true
    static controlledProperties = ['stringValue','intValue','decValue','refValue','paragraph','note','dateValue']

  @Transient
  String paragraph

    static mapping = {
        includes AbstractProperty.mapping

        paragraph    type: 'text'
    }

    static constraints = {
        importFrom AbstractProperty

        paragraph(nullable: true)
    }

    def copyValueAndNote(newProp){
        newProp = super.copyValueAndNote(newProp)

        newProp.paragraph = paragraph
        newProp
    }

  @Transient
  def grailsApplication

  @Transient
  def messageSource

  static belongsTo = [
      type : PropertyDefinition,
      owner: License
  ]

  PropertyDefinition type
  License owner

  @Transient
  def onDelete = { oldMap ->
    log.debug("onDelete LicenseCustomProperty")
    def oid = "${this.owner.class.name}:${this.owner.id}"
    def changeDoc = [ OID: oid,
                     event:'LicenseCustomProperty.deleted',
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
