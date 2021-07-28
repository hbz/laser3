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

    WfTaskPrototype child

    static mapping = {
                 id column: 'wfwp_id'
            version column: 'wfwp_version'
              state column: 'wfwp_state_rv_fk'
              child column: 'wfwp_child_fk'
              title column: 'wfwp_title'
        description column: 'wfwp_description', type: 'text'

        dateCreated column: 'wfwp_date_created'
        lastUpdated column: 'wfwp_last_updated'
    }

    static constraints = {
        title       (blank: false)
        child       (nullable: true)
        description (nullable: true)
    }

    boolean inUse() {
        child != null
    }

    List<WfTaskPrototype> getSequence() {
        child ? child.getSequence() : []
    }

    WfWorkflow instantiate() throws Exception {

        WfWorkflow workflow = new WfWorkflow(
                title:       this.title,
                description: this.description,
                prototype:   this,
                owner:       contextService.getOrg(),
                status:      RDStore.WF_WORKFLOW_STATUS_OPEN,
                subscription: Subscription.get(9542)
        )
        if (this.child) {
            workflow.child = this.child.instantiate()
        }
        if (! workflow.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate() : ' + workflow.getErrors().toString() )
        }

        workflow
    }
}
