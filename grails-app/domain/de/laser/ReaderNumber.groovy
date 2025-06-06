package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.utils.DateUtils
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat
import java.time.Year

/**
 * Represents a registered reader count entry for a library. It may be grouped by a reference group and a temporal entity: one of semester or due date.
 * A reference group is linked to a temporal entity and we cannot have both due date and semester set. Only high schools use semesters; the reference groups selectable by them are thus linked to semester. All others go with due date.
 * See readerNumber.gsp for the groups selectable by each institution type
 */
@Slf4j
class ReaderNumber {

    @RefdataInfo(cat = RDConstants.NUMBER_TYPE)
    RefdataValue referenceGroup
    String dateGroupNote //counts for every number for a given date

    BigDecimal value
    @RefdataInfo(cat = RDConstants.SEMESTER)
    RefdataValue semester
    //direct conversion via SQL is not possible because java.time.Year is stored by GORM as bytea
    @Deprecated
    Date dueDate
    Year year

    Date dateCreated
    Date lastUpdated

    Org org

    static constraints = {
        dateGroupNote(nullable: true, blank: false)
        value(nullable: true)
        semester(nullable: true, validator: { RefdataValue val, ReaderNumber obj ->
            if (obj.year && obj.semester) {
                return ['no simultaneous year and semester']
            }
        })
        dueDate(nullable: true) //deprecated and to be removed; cannot be removed because of impossible direct conversion via SQL
        year(nullable: true, validator: { Year val, ReaderNumber obj ->
            if (obj.semester && obj.year) {
                return ['no simultaneous year and semester']
            }
        })
    }

    static mapping = {
        id              column:'num_id'
        version         column:'num_version'

        referenceGroup  column:'num_reference_group_rv_fk'
        dateGroupNote   column:'num_date_group_note', type: 'text'
        value           column:'num_value'
        semester        column:'num_semester_rv_fk'
        dueDate         column:'num_due_date' //deprecated and to be removed; cannot be removed because of impossible direct conversion via SQL
        year            column:'num_year'

        lastUpdated     column:'num_last_updated'
        dateCreated     column:'num_date_created'
        org             column:'num_org_fk'
    }

    /**
     * Constructor method to build a reader number record with defined configuration parameters.
     * It updates a given record, matching by reference group and temporal unit (one of {@link #year} or {@link #semester}) or creates a new one, if it does not exist
     * @param configMap the {@link Map} containing the record arguments
     * @return the new reader number record
     */
    static ReaderNumber construct(Map configMap) {
        Map<String, Object> rnData = [:]
        rnData.org = Org.get(configMap.orgid)
        rnData.referenceGroup = RefdataValue.get(configMap.referenceGroup)
        ReaderNumber readerNumber
        if (configMap.year)
            rnData.year = Year.parse(configMap.year)
        else if (configMap.semester)
            rnData.semester = RefdataValue.get(configMap.semester)
        if(rnData.containsKey('year'))
            readerNumber = ReaderNumber.findByOrgAndReferenceGroupAndYear(rnData.org, rnData.referenceGroup, rnData.year)
        else if(rnData.containsKey('semester'))
            readerNumber = ReaderNumber.findByOrgAndReferenceGroupAndSemester(rnData.org, rnData.referenceGroup, rnData.semester)
        if(!readerNumber)
            readerNumber = new ReaderNumber(rnData)
        readerNumber.value = configMap.value
        if(readerNumber.save())
            readerNumber
        else {
            log.error(readerNumber.getErrors().getAllErrors().toString())
            null
        }
    }

}
