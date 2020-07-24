package com.k_int.kbplus

import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

import javax.persistence.Transient

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

    @Transient
    final static String READER_NUMBER_USER = 'User'
    @Transient
    final static String READER_NUMBER_PEOPLE = 'Population'
    @Transient
    final static String READER_NUMBER_SCIENTIFIC_STAFF = 'Scientific staff'
    @Transient
    final static String READER_NUMBER_FTE = 'FTE'
    @Transient
    final static String READER_NUMBER_STUDENTS = 'Students'
    @Transient
    final static Set<String> CONSTANTS_WITH_SEMESTER = [READER_NUMBER_STUDENTS,READER_NUMBER_SCIENTIFIC_STAFF,READER_NUMBER_FTE]
    @Transient
    final static Set<String> CONSTANTS_WITH_DUE_DATE = [READER_NUMBER_PEOPLE,READER_NUMBER_USER]

    static constraints = {
        //type            (blank:false)
        referenceGroup(blank: false, validator: { String val, ReaderNumber obj ->
            if (val in RefdataValue.findAllByValueInListAndOwner(CONSTANTS_WITH_SEMESTER, RefdataCategory.findByDesc(RDConstants.NUMBER_TYPE)) && (!obj.semester || obj.dueDate)) {
                return ['use students, FTE or scientific staff only with semester']
            }
            if ((val in RefdataValue.findAllByValueInListAndOwner(CONSTANTS_WITH_DUE_DATE, RefdataCategory.findByDesc(RDConstants.NUMBER_TYPE)) || !RefdataValue.executeQuery('select rdv from RefdataValue rdv join rdv.owner rdc where :value in (rdv.value,rdv.value_de,rdv.value_en) and rdc.desc = :desc', [value: val, desc: RDConstants.NUMBER_TYPE])) && (!obj.dueDate || obj.semester)) {
                return ['use user, people or non-refdata number types only with due date']
            }
        })
        value(nullable: true, blank: true)
        semester(nullable: true, blank: false, validator: { RefdataValue val, ReaderNumber obj ->
            if (obj.dueDate && obj.semester) {
                return ['no simultaneous due date and semester']
            }
        })
        dueDate(nullable: true, validator: { Date val, ReaderNumber obj ->
            if (obj.semester && obj.dueDate) {
                return ['no simultaneous due date and semester']
            }
        })
        org(blank: false)
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
