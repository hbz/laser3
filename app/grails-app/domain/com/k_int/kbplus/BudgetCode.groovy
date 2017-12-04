package com.k_int.kbplus

class BudgetCode {

    Org owner
    String value

    static mapping = {
         id column:'bc_id'
    version column:'bc_version'
      owner column:'bc_owner_fk'
      value column:'bc_value'
    }

    static constraints = {
      owner (nullable:false, blank:false)
      value (nullable:false, blank:false)
    }
}
