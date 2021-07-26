package de.laser.workflow

class WfTaskPrototype extends WfTaskBase {

    static final String KEY = 'WF_TASK_PROTOTYPE'

    WfConditionPrototype condition

    WfTaskPrototype child
    WfTaskPrototype next

    static mapping = {
                 id column: 'wftp_id'
            version column: 'wftp_version'
           priority column: 'wftp_priority_rv_fk'
               //type column: 'wftp_type_rv_fk'
          condition column: 'wftp_condition_fk'
              child column: 'wftp_child_fk'
               next column: 'wftp_next_fk'
              title column: 'wftp_title'
        description column: 'wftp_description', type: 'text'

        dateCreated column: 'wftp_date_created'
        lastUpdated column: 'wftp_last_updated'
    }

    static constraints = {
        description (nullable: true, blank: false)
        condition   (nullable: true)
        child       (nullable: true)
        next        (nullable: true)
    }

    List<WfTaskPrototype> getStructure() {
        List<WfTaskPrototype> struct = []

        WfTaskPrototype t = this
        while (t) {
            struct.add( t ); t = t.next
        }
        struct
    }

    WfWorkflowPrototype getWorkflow() {
        List<WfWorkflowPrototype> result = WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where child = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getWorkflow()')
        }
        if (result) {
            return result.first() as WfWorkflowPrototype
        }
    }

    WfTaskPrototype getParent() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp where child = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getParent()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }

    WfTaskPrototype getPrevious() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp where next = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getPrevious()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }

    boolean inStructure() {
        return getWorkflow() || getParent() || getPrevious()
    }
}
