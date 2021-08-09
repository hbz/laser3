package de.laser.workflow

import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore

class WfWorkflow extends WfWorkflowBase {

    static final String KEY = 'WF_WORKFLOW'

    @RefdataAnnotation(cat = RDConstants.WF_WORKFLOW_STATUS)
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

    List<WfTask> getSequence() {
        task ? task.getSequence() : []
    }

    Map<String, Object> getInfo() {

        Map<String, Object> info = [
            tasksOpen: 0,
            tasksCanceled: 0,
            tasksDone: 0,
            tasksNormal: 0,
            tasksOptional: 0,
            tasksImportant: 0
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

            if (task.priority == RDStore.WF_TASK_PRIORITY_NORMAL)       { info.tasksNormal++ }
            if (task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL)     { info.tasksOptional++ }
            if (task.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT)    { info.tasksImportant++ }
        }

        info
    }

    void remove() throws Exception {
        if (this.task) {
            this.task.remove()
        }
        this.delete()
    }
}
