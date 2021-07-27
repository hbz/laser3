package de.laser.workflow

class WfWorkflowPrototype extends WfWorkflowBase {

    static final String KEY = 'WF_WORKFLOW_PROTOTYPE'

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

    List<WfTaskPrototype> getStructure() {
        child ? child.getStructure() : []
    }

    boolean inStructure() {
         child != null
    }
}
