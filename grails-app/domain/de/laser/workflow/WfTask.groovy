package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataAnnotation
import de.laser.storage.RDConstants

/**
 * Represents a task in a workflow, containing a {@link WfCondition} to solve it. A task may be in one of the following status ({@link RDConstants#WF_TASK_STATUS}):
 * <ul>
 *     <li>open</li>
 *     <li>canceled</li>
 *     <li>done</li>
 * </ul>
 * Moreover, tasks may be prioritised according to the following reference values ({@link RDConstants#WF_TASK_PRIORITY}):
 * <ul>
 *     <li>normal</li>
 *     <li>optional</li>
 *     <li>important</li>
 * </ul>
 */
class WfTask extends WfTaskBase {

    static final String KEY = 'WF_TASK'

    @RefdataAnnotation(cat = RDConstants.WF_TASK_STATUS)
    RefdataValue status

    WfCondition condition

    WfTask child
    WfTask next

    String comment

    // static belongsTo = [ workflow: WfWorkflow ]

    static mapping = {
                 id column: 'wft_id'
            version column: 'wft_version'
           priority column: 'wft_priority_rv_fk'
             status column: 'wft_status_rv_fk'
               //type column: 'wft_type_rv_fk'
              title column: 'wft_title'
        description column: 'wft_description', type: 'text'
            comment column: 'wft_comment', type: 'text'
          condition column: 'wft_condition_fk'
              child column: 'wft_child_fk'
               next column: 'wft_next_fk'

        dateCreated column: 'wft_date_created'
        lastUpdated column: 'wft_last_updated'
    }

    static constraints = {
        title       (blank: false)
        description (nullable: true)
        condition   (nullable: true)
        child       (nullable: true)
        next        (nullable: true)
        comment     (nullable: true)
    }

    /**
     * Gets the sequence of tasks, beginning from the current one
     * @return a {@link List} of tasks, ordered by sequence
     */
    List<WfTask> getSequence() {
        List<WfTask> sequence = []

        WfTask t = this
        while (t) {
            sequence.add( t ); t = t.next
        }
        sequence
    }

    /**
     * Removes this task and the associated objects to it
     * @throws Exception
     */
    void remove() throws Exception {
        if (this.child) {
            this.child.remove()
        }
        if (this.next) {
            this.next.remove()
        }
        if (this.condition) {
            this.condition.remove()
        }
        this.delete()
    }

    def afterUpdate() {
        condition?.afterUpdate()
    }

    /**
     * Retrieves the {@link WfWorkflow} to which this task is associated
     * @return the {@link WfWorkflow}
     */
    WfWorkflow getWorkflow() {
        WfWorkflow.findByTask( this )
    }

    /**
     * Retrieves the parent task of this task
     * @return the task of which this is a child (the parent task)
     */
    WfTask getParent() {
        WfTask.findByChild( this )
    }

    /**
     * Retrieves the previous task of this task
     * @return the task to which this task is following (the preceding task)
     */
    WfTask getPrevious() {
        WfTask.findByNext( this )
    }
}
