package de.laser.workflow


import de.laser.storage.RDStore

/**
 * Represents a prototype task of a workflow. For the concrete representation (= the actual task to solve), see {@link WfTask}
 * @see WfConditionPrototype
 */
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
        title       (blank: false)
        description (nullable: true)
        condition   (nullable: true)
        child       (nullable: true)
        next        (nullable: true)
    }

    /**
     * Checks whether this prototype is in use by other instances
     * @return is there any association pointing to this prototype?
     */
    boolean inUse() {
        return child != null || next != null || getWorkflow() || getParent() || getPrevious()
    }

    /**
     * Gets a sequence of prototypes, starting from the current object
     * @return a {@link List} of {@link WfTaskPrototype}
     */
    List<WfTaskPrototype> getSequence() {
        List<WfTaskPrototype> sequence = []

        WfTaskPrototype t = this
        while (t) {
            sequence.add( t ); t = t.next
        }
        sequence
    }

    /**
     * Instantiates a new {@link WfTask} based on this prototype. If there are linked objects to this prototype, the linked objects will be instantiated as well
     * @return the complete task object
     * @throws Exception
     */
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

    /**
     * Retrieves the workflow prototype to which this prototype is belonging. If there are multiple workflow prototypes to which this prototype may belong, a warning is being emit. In such a case, the first object is being returned
     * @return the associated {@link WfWorkflowPrototype}
     */
    WfWorkflowPrototype getWorkflow() {
        List<WfWorkflowPrototype> result = WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where task = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getWorkflow()')
        }
        if (result) {
            return result.first() as WfWorkflowPrototype
        }
    }

    /**
     * Returns the parent prototype of this task prototype
     * @return the prototype of which this is a child (the parent prototype), the first match if there are multiple matches (ordered by id)
     */
    WfTaskPrototype getParent() {
        List<WfTaskPrototype> result = WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp where child = :current order by id', [current: this] )

        if (result.size() > 1) {
            log.warn( 'MULTIPLE MATCHES - getParent()')
        }
        if (result) {
            return result.first() as WfTaskPrototype
        }
    }

    /**
     * Returns the previous prototype of this task prototype
     * @return the prototype which this is following (the preceding prototype), the first match if there are multiple macthes (ordered by id)
     */
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
