package de.laser.workflow

import de.laser.RefdataValue
import de.laser.storage.RDStore
import de.laser.ui.Icon

/**
 * Helper class supporting the display of reports
 */
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
            case [ RDStore.WF_WORKFLOW_STATUS_DONE, RDStore.WF_TASK_STATUS_DONE ]:
                return 'green'
                break

            default:
                return ''
        }
    }

    static String getCssIconAndColorByStatus(RefdataValue status) {

        switch (status) {
            case RDStore.WF_WORKFLOW_STATUS_OPEN:
                return Icon.WORKFLOW + ' la-light-grey'
                break
            case RDStore.WF_TASK_STATUS_OPEN:
                return Icon.ATTR.WORKFLOW_CHECKPOINT + ' la-light-grey'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_CANCELED, RDStore.WF_TASK_STATUS_CANCELED ]:
                return Icon.SYM.NO + ' orange'
                break
            case [ RDStore.WF_WORKFLOW_STATUS_DONE, RDStore.WF_TASK_STATUS_DONE ]:
                return Icon.SYM.YES + ' green'
                break

            default:
                return ''
        }
    }
}