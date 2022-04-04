package de.laser.workflow

import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataAnnotation
import de.laser.storage.BeanStorage
import de.laser.helper.RDConstants
import de.laser.helper.RDStore

/**
 * Represents a prototype of a workflow which may be implemented in different contexts (for different {@link de.laser.Org}s and {@link Subscription}s)
 * For the concrete workflow, see {@link WfWorkflow}
 * @see WfTaskPrototype
 */
class WfWorkflowPrototype extends WfWorkflowBase {

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

    /**
     * Checks whether this prototype is in use
     * @return is there a {@link WfTaskPrototype} linked to this prototype?
     */
    boolean inUse() {
        task != null
    }

    /**
     * Gets the sequence of task prototypes in this workflow
     * @return a {@link List} of {@link WfTaskPrototype}s, ordered in intellectualy set order (defined by {@link WfTaskPrototype#next})
     */
    List<WfTaskPrototype> getSequence() {
        task ? task.getSequence() : []
    }

    /**
     * Instantiates a new workflow defined by the properties of this prototype, attaches this to the given {@link Subscription} and assigns it to the context {@link de.laser.Org}.
     * The linked {@link WfTaskPrototype}s are instantiated as well
     * @param subId the id of the {@link Subscription} to link this workflow to
     * @return the instantiated complete {@link WfWorkflow} object
     * @throws Exception
     */
    WfWorkflow instantiate(Long subId) throws Exception {

        WfWorkflow workflow = new WfWorkflow(
                title:       this.title,
                description: this.description,
                prototype:   this,
                owner:       BeanStorage.getContextService().getOrg(),
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
