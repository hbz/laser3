package com.k_int.kbplus

class Cluster {

    String name
    String definition
    RefdataValue type // TODO specify
    
    static belongsTo = [
        owner:    Org
    ]
    
    static mapping = {
        id         column:'cl_id'
        version    column:'version'
        name       column:'cl_name'
        definition column:'cl_definition'
        type       column:'cl_type_rv_fk'
        owner      column:'cl_owner_fk'
    }
    
    static constraints = {
        owner      (nullable:false)
    }
}
