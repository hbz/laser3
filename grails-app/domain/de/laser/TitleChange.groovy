package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import groovy.util.logging.Slf4j

@Slf4j
@Deprecated
class TitleChange {

    TitleInstancePackagePlatform tipp
    String event
    String field
    String oldVal
    String newVal
    @RefdataInfo(cat = RefdataInfo.UNKOWN)
    RefdataValue oldRefVal
    @RefdataInfo(cat = RefdataInfo.UNKOWN)
    RefdataValue newRefVal
    Date oldDateVal
    Date newDateVal
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id              column: 'tic_id'
        version         column: 'tic_version'
        tipp            column: 'tic_tipp_fk', index: 'tic_tipp_idx'
        event           column: 'tic_event', type: 'text', index: 'tic_event_idx'
        field           column: 'tic_field', type: 'text'
        oldVal          column: 'tic_old_value', index: 'tic_old_value_idx', type: 'text'
        newVal          column: 'tic_new_value', index: 'tic_new_value_idx', type: 'text'
        oldRefVal       column: 'tic_old_ref_value_rv_fk', index: 'tic_old_value_rv_idx'
        newRefVal       column: 'tic_new_ref_value_rv_fk', index: 'tic_new_value_rv_idx'
        oldDateVal      column: 'tic_old_date_value', index: 'tic_old_date_value_idx'
        newDateVal      column: 'tic_new_date_value', index: 'tic_new_date_value_idx'
        dateCreated     column: 'tic_date_created'
        lastUpdated     column: 'tic_last_updated'
    }

    static constraints = {
        field(nullable: true, blank: false)
        oldVal(nullable: true, blank: false)
        newVal(nullable: true, blank: false)
        oldDateVal(nullable: true, blank: false)
        newDateVal(nullable: true, blank: false)
        oldRefVal(nullable: true, blank: false)
        newRefVal(nullable: true, blank: false)
    }

    @Deprecated
    static TitleChange construct(Map<String, Object> configMap) throws CreationException {
        TitleChange tic = new TitleChange(tipp: configMap.tipp, event: configMap.event)
        if(configMap.prop in PendingChange.DATE_FIELDS) {
            tic.oldDateVal = configMap.oldValue
            tic.newDateVal = configMap.newValue
        }
        else if(configMap.prop in PendingChange.REFDATA_FIELDS) {
            tic.oldRefVal = configMap.oldValue
            tic.newRefVal = configMap.newValue
        }
        else {
            tic.oldVal = configMap.oldValue
            tic.newVal = configMap.newValue
        }
        if(tic.save())
            tic
        else if(tic.errors)
            throw new CreationException(tic.errors)
        else {
            log.error("unknown error")
            null
        }
    }

}
