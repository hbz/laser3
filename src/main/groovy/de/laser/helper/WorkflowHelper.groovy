package de.laser.helper

import de.laser.RefdataValue

class WorkflowHelper {

    static String getCssClassByStatus(RefdataValue status) {

        switch (status) {
            case    RDStore.WF_WORKFLOW_STATUS_OPEN :
            case    RDStore.WF_TASK_STATUS_OPEN :
            //case    RDStore.WF_CONDITION_STATUS_OPEN :
                return ''
                break
            case    RDStore.WF_WORKFLOW_STATUS_CANCELED :
            case    RDStore.WF_TASK_STATUS_CANCELED :
                return 'grey'
                break
            case    RDStore.WF_WORKFLOW_STATUS_DONE :
            case    RDStore.WF_TASK_STATUS_DONE :
            //case    RDStore.WF_CONDITION_STATUS_DONE :
                return 'green'
                break

            default:
                return ''
        }
    }
}
