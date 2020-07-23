package com.k_int.kbplus

class BudgetCode {

    Org    owner
    String value
    String descr

    Date dateCreated
    Date lastUpdated

    static mapping = {
         id column:'bc_id'
    version column:'bc_version'
      owner column:'bc_owner_fk', index:'bc_owner_idx'
      value column:'bc_value'
      descr column:'bc_description'
      dateCreated column: 'bc_date_created'
      lastUpdated column: 'bc_last_updated'
    }

    static constraints = {
        owner (blank:false)
        value (blank:false, unique: 'owner')
        descr (nullable:true,  blank:true, maxSize:2048)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
