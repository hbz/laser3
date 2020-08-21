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

    final static String READER_NUMBER_USER = 'User'
    final static String READER_NUMBER_PEOPLE = 'Population'
    final static String READER_NUMBER_SCIENTIFIC_STAFF = 'Scientific staff'
    final static String READER_NUMBER_FTE = 'FTE'
    final static String READER_NUMBER_STUDENTS = 'Students'
    final static Set<String> CONSTANTS_HIGH_SCHOOL = [READER_NUMBER_STUDENTS, READER_NUMBER_SCIENTIFIC_STAFF, READER_NUMBER_FTE]
    final static Set<String> CONSTANTS_STATE_LIBRARY = [READER_NUMBER_USER] //for further extension
    final static Set<String> CONSTANTS_PUBLIC_LIBRARY = [READER_NUMBER_PEOPLE] //for further extension

    static constraints = {
        //type            (blank:false)
        referenceGroup(blank: false)
        value(nullable: true)
        semester(nullable: true, validator: { RefdataValue val, ReaderNumber obj ->
            if (obj.dueDate && obj.semester) {
                return ['no simultaneous due date and semester']
            }
        })
        dueDate(nullable: true, validator: { Date val, ReaderNumber obj ->
            if (obj.semester && obj.dueDate) {
                return ['no simultaneous due date and semester']
            }
        })
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
