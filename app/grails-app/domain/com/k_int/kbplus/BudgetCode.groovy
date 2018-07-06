package com.k_int.kbplus

class BudgetCode {

    Org    owner
    String value
    String descr

    static mapping = {
         id column:'bc_id'
    version column:'bc_version'
      owner column:'bc_owner_fk'
      value column:'bc_value'
      descr column:'bc_description'
    }

    static constraints = {
        owner (nullable:false, blank:false)
        value (nullable:false, blank:false, unique: 'owner')
        descr (nullable:true,  blank:true, maxSize:2048)
    }
}
