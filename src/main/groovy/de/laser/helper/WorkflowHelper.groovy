package de.laser.helper

import de.laser.RefdataValue
import de.laser.workflow.WfCondition
import de.laser.workflow.WfTask

class WorkflowHelper {

    static String getCssColorByStatus(RefdataValue status) {

        switch (status) {
            case    RDStore.WF_WORKFLOW_STATUS_OPEN :
            case    RDStore.WF_TASK_STATUS_OPEN :
            //case    RDStore.WF_CONDITION_STATUS_OPEN :
                return ''
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
            case    RDStore.WF_TASK_STATUS_OPEN :
            //case    RDStore.WF_CONDITION_STATUS_OPEN :
                return 'caret right grey'
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
                return 'arrow alternate circle up'
                break
            case    RDStore.WF_TASK_PRIORITY_OPTIONAL :
                return 'arrow alternate circle down'
                break
            case    RDStore.WF_TASK_PRIORITY_NORMAL :
                return 'circle'
                break

            default:
                return ' '
        }
    }

    static String getTaskTooltipMarkup(WfTask task) {

        String html = "<p><strong>${task.title}</strong></p>"

        if (task.description) {
            html = html + "<p>${task.description}</p>"
        }
        if (task.condition) {
            WfCondition cnd = task.condition

            if (cnd.type != 0) {
                List<String> fields = []
                cnd.getFields().each { f ->
                    String fstr = (cnd.getProperty( f + '_title') ?: 'Feld ohne Titel')

                    if (f.startsWith('checkbox')) {
                        if (cnd.getProperty( f ) == true) {
                            fstr = fstr + ': <i class="ui icon green check square outline"></i>'
                        }
                        else {
                            fstr = fstr + ': <i class="ui icon square outline"></i>'
                        }
                    }
                    else if (f.startsWith('date')) {
                        if (cnd.getProperty( f )) {
                            fstr = fstr + ': ' + DateUtils.getSDF_NoTime().format(cnd.getProperty(f))
                        }
                        else {
                            fstr = fstr + ': '
                        }
                    }
                    fields.add( '* ' + fstr )
                }
                html = html + '<p>' + fields.join('<br/>') + '</p>'
            }
            else {
                html = html + "<p><em>${task.condition.getTypeAsRefdataValue().getI10n('value')}</em></p>"
            }
        }
        html
    }
}