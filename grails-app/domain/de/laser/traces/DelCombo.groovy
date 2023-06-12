package de.laser.traces

class DelCombo {

    String accessibleOrg
    Long delObjTrace

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
