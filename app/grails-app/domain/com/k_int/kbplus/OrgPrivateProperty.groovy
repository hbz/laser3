package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import com.k_int.kbplus.abstract_domain.PrivateProperty
import javax.persistence.Transient

/**Org private properties are used to store Org related settings and options only for specific members**/
class OrgPrivateProperty extends PrivateProperty {
  
  static belongsTo = [
      type :    PropertyDefinition,
      owner:    Org,
      tenant:   Org
  ]
  
  PropertyDefinition type
  Org owner
  Org tenant
}
