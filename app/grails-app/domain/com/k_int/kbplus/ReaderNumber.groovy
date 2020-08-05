package com.k_int.kbplus

import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

class ReaderNumber {

    /*@RefdataAnnotation(cat = '?')
    RefdataValue type*/
    String referenceGroup

    Integer value
    @RefdataAnnotation(cat = RDConstants.SEMESTER)
    RefdataValue semester
    Date dueDate

    Date dateCreated
    Date lastUpdated

    Org org

    static constraints = {
        //type            (blank:false)
        referenceGroup  (blank:false)
        value           (nullable:true,  blank:true)
        semester        (nullable:true, blank:false)
        dueDate         (nullable:true, blank:false)

        org             (blank:false)
    }

    static mapping = {
        id              column:'num_id'
        version         column:'num_version'

        //type            column:'num_typ_rdv_fk'
        referenceGroup  column:'num_reference_group'
        value           column:'num_value'
        semester        column:'num_semester_rv_fk'
        dueDate         column:'num_due_date'

        lastUpdated     column:'num_last_updated'
        dateCreated     column:'num_date_created'
        org             column:'num_org_fk'
    }

    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
}
