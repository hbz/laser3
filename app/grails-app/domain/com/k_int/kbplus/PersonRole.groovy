package com.k_int.kbplus

class PersonRole {

    RefdataValue roleType // TODO specify
    
    static belongsTo = [
        prs:     Person,
        roleType:RefdataValue
      ]
    
    static mapping = {
        id       column:'pr_id'
        version  column:'version'
        prs      column:'pr_prs_fk'
        roleType column:'pr_roleType_rv_fk'
    }
    
    static constraints = {
        prs      (nullable:false)
        roleType (nullable:false)
    }
}
