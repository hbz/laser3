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

    void remove() throws Exception {
        if (this.task) {
            this.task.remove()
        }
        this.delete()
    }
}
