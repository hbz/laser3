package de.laser.workflow

import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.storage.RDStore

/**
 * Represents a workflow. It is based on a {@link WfWorkflowPrototype} and may contain several {@link WfTask}s. A workflow is linked to a {@link Subscription} to which tasks should be done in an ordered way and owned by an
 * {@link Org} whose members should solve the tasks in this workflow. Moreover, a workflow may be finalised or under testing, see the controlled list for {@link RDConstants#WF_WORKFLOW_STATE} and put in different stages, according
 * to {@link RDConstants#WF_WORKFLOW_STATUS}:
 * <ul>
 *     <li>open</li>
 *     <li>canceled</li>
 *     <li>done</li>
 * </ul>
 */
class WfWorkflow extends WfWorkflowBase {

    static final String KEY = 'WF_WORKFLOW'

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_STATUS)
    RefdataValue status

    WfWorkflowPrototype prototype
    WfTask task
    Subscription subscription
    Org owner

    String comment

    static mapping = {
                 id column: 'wfw_id'
            version column: 'wfw_version'
             status column: 'wfw_status_rv_fk'
          prototype column: 'wfw_prototype_fk'
               task column: 'wfw_task_fk'
       subscription column: 'wfw_subscription_fk'
              owner column: 'wfw_owner_fk'
              title column: 'wfw_title'
        description column: 'wfw_description', type: 'text'
            comment column: 'wfw_comment', type: 'text'

        dateCreated column: 'wfw_date_created'
        lastUpdated column: 'wfw_last_updated'
    }

    static constraints = {
        title       (blank: false)
        task        (nullable: true)
        description (nullable: true)
        comment     (nullable: true)
    }

    /**
     * Retrieves the tasks in this workflow, ordered by the intellectually set order. Each {@link WfTask} is linked by the {@link WfTask#next} property
     * @return the {@link List} of {@link WfTask}s in this workflow
     */
    List<WfTask> getSequence() {
        task ? task.getSequence() : []
    }

    /**
     * Assembles a summary of information of this workflow. This summary contains:
     * <ul>
     *     <li>how many tasks are open?</li>
     *     <li>how many tasks are canceled?</li>
     *     <li>how many tasks are done?</li>
     *     <li>how many tasks are of normal priority?</li>
     *     <li>how many tasks are optional?</li>
     *     <li>how many tasks are important?</li>
     *     <li>how many tasks are blockers of normal priority?</li>
     *     <li>how many tasks are blockers of importance?</li>
     *     <li>when was the last update done on this workflow?</li>
     * </ul>
     * @return a {@link Map} containing the relevant information
     */
    Map<String, Object> getInfo() {

        Map<String, Object> info = [
            tasksOpen: 0,
            tasksCanceled: 0,
            tasksDone: 0,
            tasksNormal: 0,
            tasksOptional: 0,
            tasksImportant: 0,
            tasksNormalBlocking: 0,
            tasksImportantBlocking: 0,
            lastUpdated: lastUpdated
        ]

        List<WfTask> sequence = []

        getSequence().each{ task ->
            sequence.add(task)
            if (task.child) {
                sequence.addAll( task.child.getSequence() )
            }
        }

        sequence.each{task ->
            if (task.status == RDStore.WF_TASK_STATUS_OPEN)     { info.tasksOpen++ }
            if (task.status == RDStore.WF_TASK_STATUS_CANCELED) { info.tasksCanceled++ }
            if (task.status == RDStore.WF_TASK_STATUS_DONE)     { info.tasksDone++ }

            if (task.priority == RDStore.WF_TASK_PRIORITY_NORMAL)       {
                info.tasksNormal++
                if (task.status != RDStore.WF_TASK_STATUS_DONE) {
                    info.tasksNormalBlocking++
                }
            }
            if (task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL)     { info.tasksOptional++ }
            if (task.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT)    {
                info.tasksImportant++
                if (task.status != RDStore.WF_TASK_STATUS_DONE) {
                    info.tasksImportantBlocking++
                }
            }

            // TODO
            if (task.lastUpdated > info.lastUpdated) { info.lastUpdated = task.lastUpdated }
            if (task.condition && task.condition.lastUpdated > info.lastUpdated) { info.lastUpdated = task.condition.lastUpdated }
        }

        info
    }

    /**
     * Removes this workflow and all tasks linked to it
     * @throws Exception
     */
    void remove() throws Exception {
        if (this.task) {
            this.task.remove() //this calls the succession chain as well
        }
        this.delete()
    }
}
