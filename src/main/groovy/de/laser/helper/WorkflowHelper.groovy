package de.laser.helper

import de.laser.RefdataValue

class WorkflowHelper {

    static String getCssColorByStatus(RefdataValue status) {

        switch (status) {
            case    RDStore.WF_WORKFLOW_STATUS_OPEN :
            case    RDStore.WF_TASK_STATUS_OPEN :
            //case    RDStore.WF_CONDITION_STATUS_OPEN :
                return 'la-light-grey'
                break
            case    RDStore.WF_WORKFLOW_STATUS_CANCELED :
            case    RDStore.WF_TASK_STATUS_CANCELED :
                return 'orange'
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

    static String getCssIconAndColorByStatus(RefdataValue status) {

        switch (status) {
            case    RDStore.WF_WORKFLOW_STATUS_OPEN :
                return 'tasks la-light-grey'
                break
            case    RDStore.WF_TASK_STATUS_OPEN :
                return 'check circle outline la-light-grey'
                break
            case    RDStore.WF_WORKFLOW_STATUS_CANCELED :
            case    RDStore.WF_TASK_STATUS_CANCELED :
                return 'x icon orange'
                break
            case    RDStore.WF_WORKFLOW_STATUS_DONE :
            case    RDStore.WF_TASK_STATUS_DONE :
            //case    RDStore.WF_CONDITION_STATUS_DONE :
                return 'check green'
                break

            default:
                return ''
        }
    }

    static String getCssIconByTaskPriority(RefdataValue priority) {

        switch (priority) {
            case    RDStore.WF_TASK_PRIORITY_IMPORTANT :
                return 'arrow circle up'
                break
            case    RDStore.WF_TASK_PRIORITY_OPTIONAL :
                return 'arrow circle down'
                break
            case    RDStore.WF_TASK_PRIORITY_NORMAL :
                return 'circle'
                break

            default:
                return ' '
        }
    }
}