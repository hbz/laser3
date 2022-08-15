package de.laser.workflow

import de.laser.RefdataValue
import de.laser.storage.RDStore

class WorkflowHelper {

    static String getCssColorByStatus(RefdataValue status) {

        switch (status) {
            case [ RDStore.WF_WORKFLOW_STATUS_OPEN ]:
                return 'la-light-grey'
                break
            case [ RDStore.WF_TASK_STATUS_OPEN ]:
                return 'sc_darkgrey'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_CANCELED, RDStore.WF_TASK_STATUS_CANCELED ]:
                return 'orange'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_DONE,RDStore.WF_TASK_STATUS_DONE ]:
                return 'green'
                break

            default:
                return ''
        }
    }

    static String getCssIconAndColorByStatus(RefdataValue status) {

        switch (status) {
            case RDStore.WF_WORKFLOW_STATUS_OPEN:
                return 'tasks la-light-grey'
                break
            case RDStore.WF_TASK_STATUS_OPEN:
                return 'circle outline la-light-grey'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_CANCELED, RDStore.WF_TASK_STATUS_CANCELED ]:
                return 'x icon orange'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_DONE,RDStore.WF_TASK_STATUS_DONE ]:
                return 'check green'
                break

            default:
                return ''
        }
    }

    static String getCssIconByTaskPriority(RefdataValue priority) {

        switch (priority) {
            case RDStore.WF_TASK_PRIORITY_IMPORTANT:
                return 'arrow circle up'
                break
            case RDStore.WF_TASK_PRIORITY_OPTIONAL:
                return 'arrow circle down'
                break
            case RDStore.WF_TASK_PRIORITY_NORMAL:
                return 'circle'
                break

            default:
                return ' '
        }
    }
}