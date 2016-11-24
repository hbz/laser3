package com.k_int.kbplus

class OrgGroup {
    
    static belongsTo = [
        org:    Org,
        group:  Group
      ]
    
    static mapping = {
        id      column:'og_id'
        version column:'version'
        org     column:'og_org_fk'
        group   column:'og_group_fk'
    }
    
    static constraints = {
        org     (nullable:false)
        group   (nullable:false)
    }
}
