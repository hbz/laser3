package de.laser.workflow

import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore

class WfWorkflowPrototype extends WfWorkflowBase {

    def contextService

    static final String KEY = 'WF_WORKFLOW_PROTOTYPE'

    @RefdataAnnotation(cat = RDConstants.WF_WORKFLOW_STATE)
    RefdataValue state

    WfTaskPrototype task

    static mapping = {
                 id column: 'wfwp_id'
            version column: 'wfwp_version'
              state column: 'wfwp_state_rv_fk'
               task column: 'wfwp_task_fk'
              title column: 'wfwp_title'
        description column: 'wfwp_description', type: 'text'

        dateCreated column: 'wfwp_date_created'
        lastUpdated column: 'wfwp_last_updated'
    }

    static constraints = {
        title       (blank: false)
        task        (nullable: true)
        description (nullable: true)
    }

    boolean inUse() {
        task != null
    }

    List<WfTaskPrototype> getSequence() {
        task ? task.getSequence() : []
    }

    WfWorkflow instantiate(Long subId) throws Exception {

        WfWorkflow workflow = new WfWorkflow(
                title:       this.title,
                description: this.description,
                prototype:   this,
                owner:       contextService.getOrg(),
                status:      RDStore.WF_WORKFLOW_STATUS_OPEN,
                subscription: Subscription.get(subId)
        )
        if (this.task) {
            workflow.task = this.task.instantiate()
        }
        if (! workflow.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate(' + subId + ') : ' + workflow.getErrors().toString() )
        }

        workflow
    }
}
