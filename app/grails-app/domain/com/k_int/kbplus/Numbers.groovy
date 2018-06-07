package com.k_int.kbplus

class Numbers {

    RefdataValue type
    Integer number
    Date startDate
    Date endDate

    Date dateCreated
    Date lastUpdated

    Org org

    static constraints = {
        type            (nullable:false, blank:false)
        number          (nullable:true,  blank:true)
        startDate       (nullable:false, blank:false)
        endDate         (nullable:true,  blank:true)

        lastUpdated     (nullable:false, blank:false)
        dateCreated     (nullable:false, blank:false)
        org             (nullable:false, blank:false)
    }

    static mapping = {
        id              column:'num_id'
        version         column:'num_version'

        type            column:'num_typ_rdv_fk'
        number          column:'num_number'
        startDate       column:'num_start_date'
        endDate         column:'num_end_date'

        lastUpdated     column:'num_lastUpdate_date'
        dateCreated     column:'num_create_date'
        org             column:'num_org_fk'
    }

    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
}
