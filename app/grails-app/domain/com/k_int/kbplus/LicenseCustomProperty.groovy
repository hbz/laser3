package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.CustomProperty
import javax.persistence.Transient

class LicenseCustomProperty extends CustomProperty {

  // %{-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Begin Copied From Class AbstractProperty <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< --}%
  @Transient
  String          paragraph

    static mapping = {
        paragraph    type: 'text'
        note         type: 'text'
    }

    static constraints = {
    stringValue(nullable: true)
    intValue(nullable: true)
    decValue(nullable: true)
    refValue(nullable: true)
    paragraph(nullable: true)
    note(nullable: true)
    dateValue(nullable: true)
  }

  @Transient
  def getValueType(){
    if(stringValue) return "stringValue"
    if(intValue)    return "intValue"
    if(decValue)    return "decValue"
    if(refValue)    return "refValue"
    if(paragraph)   return "paragraph"
    if(dateValue)   return "dateValue"
  }

  @Override
  public String toString(){
    if(stringValue) return stringValue
    if(intValue)    return intValue.toString()
    if(decValue)    return decValue.toString()
    if(refValue)    return refValue.toString()
    if(paragraph)   return paragraph
    if(dateValue)   return dateValue.toString()
  }

  def copyValueAndNote(newProp){
    if(stringValue)     newProp.stringValue = stringValue
    else if(intValue)   newProp.intValue    = intValue
    else if(decValue)   newProp.decValue    = decValue
    else if(paragraph)  newProp.paragraph   = paragraph
    else if(refValue)   newProp.refValue    = refValue
    else if(dateValue)  newProp.refValue    = dateValue
    newProp.note = note
    newProp
  }
  // %{-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> End Copied From Class AbstractProperty <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< --}%

  @Transient
  def grailsApplication

  @Transient
  def messageSource


  static auditable = true

  static belongsTo = [
      type : PropertyDefinition,
      owner: License
  ]

  PropertyDefinition type
  License owner

  @Transient
  def onChange = { oldMap,newMap ->
    log.debug("onChange LicenseCustomProperty")
    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
      controlledProperties.each{ cp->
          if ( oldMap[cp] != newMap[cp] ) {
          log.debug("Change found on ${this.class.name}:${this.id}.")
          changeNotificationService.notifyChangeEvent([
                           OID: "${this.owner.class.name}:${this.owner.id}",
                           event:'CustomProperty.updated',
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
  @Transient
  def onDelete = { oldMap ->
    log.debug("onDelete LicenseCustomProperty")
    def oid = "${this.owner.class.name}:${this.owner.id}"
    def changeDoc = [ OID: oid,
                     event:'CustomProperty.deleted',  
                     prop: "${this.type.name}",
                     old: "",
                     new: "property removed",
                     name: this.type.name
                     ]
    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
    // changeNotificationService.broadcastEvent("com.k_int.kbplus.License:${owner.id}", changeDoc);
    changeNotificationService.notifyChangeEvent(changeDoc) 
  }
  

  @Transient
  def onSave = {
    log.debug("LicenseCustomProperty inserted")
  }
}
