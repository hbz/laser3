package de.laser.workflow


import de.laser.helper.RDStore

class WfTaskPrototype extends WfTaskBase {

    def contextService

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
        title       (blank: false)
        description (nullable: true)
        condition   (nullable: true)
        child       (nullable: true)
        next        (nullable: true)
    }

    boolean inUse() {
        return child != null || next != null || getWorkflow() || getParent() || getPrevious()
    }

    List<WfTaskPrototype> getSequence() {
        List<WfTaskPrototype> sequence = []

        WfTaskPrototype t = this
        while (t) {
            sequence.add( t ); t = t.next
        }
        sequence
    }

    WfTask instantiate() throws Exception {

        WfTask task = new WfTask(
                title:       this.title,
                description: this.description,
                priority:    this.priority,
                status:      RDStore.WF_TASK_STATUS_OPEN
        )
        if (this.child) {
            task.child = this.child.instantiate()
        }
        if (this.next) {
            task.next = this.next.instantiate()
        }
        if (this.condition) {
            task.condition = this.condition.instantiate()
        }
        if (! task.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate() : ' + task.getErrors().toString() )
        }

        task
    }

    WfWorkflowPrototype getWorkflow() {
        List<WfWorkflowPrototype> result = WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where task = :current order by id', [current: this] )

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
}
