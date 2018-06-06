package com.k_int.kbplus

class Numbers {

    RefdataValue type
    Integer number
    Date valid_date

    Date dateCreated
    Date lastUpdated

    Org org

    static constraints = {
        type            (nullable:false, blank:false)
        number          (nullable:true,  blank:true)
        valid_date      (nullable:true, blank:false)

        lastUpdated     (nullable:false, blank:false)
        dateCreated     (nullable:false, blank:false)
        org             (nullable:false, blank:false)
    }

    static mapping = {
        id              column:'num_id'
        version         column:'num_version'

        type            column:'num_typ_rdv_fk'
        number          column:'num_number'
        valid_date      column:'num_valid_date'

        lastUpdated     column:'num_lastUpdate_date'
        dateCreated     column:'num_create_date'
        org             column:'num_org_fk'
    }
}
