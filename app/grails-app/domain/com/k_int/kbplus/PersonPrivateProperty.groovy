package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.PrivateProperty
import javax.persistence.Transient

/**Person private properties are used to store Person related settings and options only for specific members**/
class PersonPrivateProperty extends PrivateProperty {
  
  static belongsTo = [
      type :    PropertyDefinition,
      owner:    Person,
      tenant:   Org
  ]
  
  PropertyDefinition type
  Person    owner
  Org       tenant
}
