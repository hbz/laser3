package de.laser.workflow

import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
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

    @RefdataInfo(cat = RDConstants.WF_TASK_STATUS)
    RefdataValue status

    WfCondition condition
    WfTask next

    String comment

    // static belongsTo = [ workflow: WfWorkflow ]

    static mapping = {
                 id column: 'wft_id'
            version column: 'wft_version'
           priority column: 'wft_priority_rv_fk'
             status column: 'wft_status_rv_fk'
              title column: 'wft_title'
        description column: 'wft_description', type: 'text'
            comment column: 'wft_comment', type: 'text'
          condition column: 'wft_condition_fk'
               next column: 'wft_next_fk'

        dateCreated column: 'wft_date_created'
        lastUpdated column: 'wft_last_updated'
    }

    static constraints = {
        title       (blank: false)
        description (nullable: true)
        condition   (nullable: true)
        next        (nullable: true)
        comment     (nullable: true)
    }

    /**
     * Removes this task and the associated objects to it
     * @throws Exception
     */
    void remove() throws Exception {
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
        List<WfWorkflow> result = WfWorkflow.findAllByTask(this, [sort: 'id'])

        if (result.size() > 1) {
            log.debug('Multiple matches for WfTask.getWorkflow() ' + this.id + ' -> ' + result.collect{ it.id })
        }
        return result ? result.first() : null
    }

    /**
     * Retrieves the previous task of this task
     * @return the task to which this task is following (the preceding task)
     */
    WfTask getPrevious() {
        List<WfTask> result = WfTask.findAllByNext(this, [sort: 'id'])

        if (result.size() > 1) {
            log.debug('Multiple matches for WfTask.getPrevious() ' + this.id + ' -> ' + result.collect{ it.id })
        }
        return result ? result.first() : null
    }
}
