package com.k_int.kbplus

class IdentifierNamespace {

  String ns
  RefdataValue nsType
  Boolean hide
  String validationRegex
  String family
  Boolean nonUnique

  static mapping = {
    id column:'idns_id'
    ns column:'idns_ns'
    nsType column:'idns_type_fk'
    hide column:'idns_hide'
    validationRegex column:'idns_val_regex'
    family column:'idns_family'
    nonUnique column:'idns_non_unique'
  }

  static constraints = {
    nsType nullable:true, blank:false
    hide nullable:true, blank:false
    validationRegex nullable:true, blank:false
    family          nullable:true, blank:false
    nonUnique       nullable:true, blank:false
  }
}
