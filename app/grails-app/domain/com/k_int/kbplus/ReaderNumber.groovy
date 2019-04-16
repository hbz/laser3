package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class ReaderNumber {

    /*@RefdataAnnotation(cat = '?')
    RefdataValue type*/
    String referenceGroup

    Integer value
    @RefdataAnnotation(cat = 'Semester')
    RefdataValue semester
    Date dueDate

    Date dateCreated
    Date lastUpdated

    Org org

    static constraints = {
        //type            (nullable:false, blank:false)
        referenceGroup  (nullable:false, blank:false)
        value           (nullable:true,  blank:true)
        semester        (nullable:true, blank:false)
        dueDate         (nullable:true, blank:false)

        org             (nullable:false, blank:false)
    }

    static mapping = {
        id              column:'num_id'
        version         column:'num_version'

        //type            column:'num_typ_rdv_fk'
        referenceGroup  column:'num_reference_group'
        value           column:'num_value'
        semester        column:'num_semester_rv_fk'
        dueDate         column:'num_due_date'

        lastUpdated     column:'num_lastUpdate_date'
        dateCreated     column:'num_create_date'
        org             column:'num_org_fk'
    }

    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
}
