package de.laser.traces

/**
 * This class keeps track of accessibility rights for a deleted object
 * @see DeletedObject
 */
class DelCombo {

    String accessibleOrg
    DeletedObject delObjTrace

    static mapping = {
        datasource 'storage'
        id column: 'delc_id'
        version column: 'delc_version'
        accessibleOrg column: 'delc_acc_org_guid', index: 'delc_org_trace_idx'
        delObjTrace column: 'delc_trace_fk', index: 'delc_org_trace_idx'
    }

    static constraints = {
    }
}
