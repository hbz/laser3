package de.laser.workflow

import de.laser.License
import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore

/**
 * Represents a prototype of a workflow which may be implemented in different contexts (for different {@link de.laser.Org}s and {@link Subscription}s)
 * For the concrete workflow, see {@link WfWorkflow}
 * @see WfTaskPrototype
 */
class WfWorkflowPrototype extends WfWorkflowBase {

    static final String KEY = 'WF_WORKFLOW_PROTOTYPE'

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_TARGET_TYPE)
    RefdataValue targetType

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_TARGET_ROLE)
    RefdataValue targetRole

    @RefdataInfo(cat = RDConstants.WF_WORKFLOW_STATE)
    RefdataValue state

    String variant

    WfTaskPrototype task

    static mapping = {
                      id column: 'wfwp_id'
                 version column: 'wfwp_version'
            variant column: 'wfwp_variant'
              state column: 'wfwp_state_rv_fk'
               task column: 'wfwp_task_fk'
              title column: 'wfwp_title'
        description column: 'wfwp_description', type: 'text'

         targetType column: 'wfwp_target_type_rv_fk'
         targetRole column: 'wfwp_target_role_rv_fk'

        dateCreated column: 'wfwp_date_created'
        lastUpdated column: 'wfwp_last_updated'
    }

    static constraints = {
        variant          (blank: false)
        title            (blank: false)
        task             (nullable: true)
        description      (nullable: true)
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
        List<WfTaskPrototype> sequence = []

        if (task) {
            if (hasCircularReferences()) {
                return sequence
            }
            WfTaskPrototype t = task

            while (t) {
                sequence.add( t ); t = t.next
            }
        }
        sequence
    }

    boolean hasCircularReferences() {
        List<WfTaskPrototype> sequence = []
        boolean hasCircularReferences = false

        if (task) {
            WfTaskPrototype t = task

            while (t && !hasCircularReferences) {
                if (sequence.contains(t)) {
                    log.debug 'Invalid data: Circular reference found! ' + t + ' @ ' + task
                    hasCircularReferences = true
                }
                else {
                    sequence.add( t ); t = t.next
                }
            }
        }
        hasCircularReferences
    }

    /**
     * Instantiates a new workflow defined by the properties of this prototype, attaches this to the given {@link Subscription} and assigns it to the context {@link de.laser.Org}.
     * The linked {@link WfTaskPrototype}s are instantiated as well
     * @param subId the id of the {@link Subscription} to link this workflow to
     * @return the instantiated complete {@link WfWorkflow} object
     * @throws Exception
     */
    WfWorkflow instantiate(Object target) throws Exception {

        WfWorkflow workflow = new WfWorkflow(
                title:              this.title,
                description:        this.description,
                prototype:              this,
                prototypeTitle:         this.title,
                prototypeVariant:       this.variant,
                prototypeLastUpdated:   this.getInfo().lastUpdated as Date,
                owner:              BeanStore.getContextService().getOrg(),
                status:             RDStore.WF_WORKFLOW_STATUS_OPEN,
                org:                target instanceof Org ? target : null,
                license:            target instanceof License ? target : null,
                subscription:       target instanceof Subscription ? target : null,
        )
        if (this.task) {
            workflow.task = this.task.instantiate()
        }
        if (! workflow.validate()) {
            log.debug( '[ ' + this.id + ' ].instantiate(' + target + ') : ' + workflow.getErrors().toString() )
        }

        workflow
    }

    Map<String, Object> getInfo() {

        Map<String, Object> info = [
            lastUpdated: lastUpdated
        ]

        List<WfTaskPrototype> sequence = []
        getSequence().each { task ->
            sequence.add(task)
        }
        sequence.each {task ->
            // TODO
            if (task.lastUpdated > info.lastUpdated) { info.lastUpdated = task.lastUpdated }
            if (task.condition && task.condition.lastUpdated > info.lastUpdated) { info.lastUpdated = task.condition.lastUpdated }
        }

        info
    }
}
