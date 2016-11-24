package com.k_int.kbplus

class Group {

    String name
    String definition
    RefdataValue type // TODO specify
    Org    owner
    
    static belongsTo = [
        owner:    Org
    ]
    
    static mapping = {
        id         column:'gr_id'
        version    column:'version'
        name       column:'gr_name'
        definition column:'gr_definition'
        type       column:'gr_type_rv_fk'
        owner      column:'gr_owner_fk'
    }
    
    static constraints = {
        owner      (nullable:false)
    }
}
