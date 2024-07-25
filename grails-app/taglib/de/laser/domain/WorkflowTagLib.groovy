package de.laser.domain

import de.laser.ContextService
import de.laser.RefdataValue
import de.laser.ui.Btn
import de.laser.ui.Icon
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.workflow.WorkflowHelper
import de.laser.workflow.WfChecklist
import de.laser.workflow.WfCheckpoint

class WorkflowTagLib {

    ContextService contextService

    static namespace = 'uiWorkflow'

    def statusIcon = { attrs, body ->

        WfChecklist checklist

        Map<String, Object> info = [:]
        String iconSize = attrs.get('size') ?: 'large'

        if (attrs.checklist) {
            checklist = attrs.checklist as WfChecklist
            info = checklist.getInfo()
            out << '<i class="' + WorkflowHelper.getCssIconAndColorByStatus(info.status as RefdataValue) + ' ' + iconSize + '"></i>'
        }
    }

    def checkpoint = { attrs, body ->

        WfCheckpoint cpoint = attrs.checkpoint as WfCheckpoint

        String tooltip = '<p><strong>' + cpoint.title + '</strong></p>' + ( cpoint.comment ? '<p>' + cpoint.comment + '</p>' : '')
        tooltip = tooltip + '<div class="ui divider"></div>'

        List<String> fields = []

        fields.add( (cpoint.date ?
                '<i class="' + Icon.SYM.DATE + '"></i> ' + message(code:'workflow.checkpoint.date') + ': <strong>' + DateUtils.getLocalizedSDF_noTime().format(cpoint.date) + '</strong>' :
                '<i class="' + Icon.SYM.DATE + ' la-light-grey"></i> ' + message(code:'workflow.checkpoint.noDate')
        ))
        fields.add( (cpoint.done == true ?
                '<i class="' + Icon.SYM.CHECKBOX_CHECKED + '"></i> ' + message(code:'workflow.checkpoint.done') :
                '<i class="' + Icon.SYM.CHECKBOX + ' la-light-grey"></i> ' + message(code:'workflow.checkpoint.open')
        ))

        tooltip = tooltip + '<p>' + fields.join('<br/>') + '</p>'

        String cssColor = WorkflowHelper.getCssColorByStatus( cpoint.done ? RDStore.WF_TASK_STATUS_DONE : RDStore.WF_TASK_STATUS_OPEN )
        String cssIcon = cpoint.done ? Icon.SYM.YES : Icon.ATTR.WORKFLOW_CHECKPOINT

        out << '<span class="la-popup-tooltip" data-position="top center" data-html="' + tooltip.encodeAsHTML() + '">'
        out <<   '<a href="' + g.createLink( controller:'ajaxHtml', action:'workflowModal', params:attrs.params ) + '" class="' + Btn.ICON.SIMPLE + ' wfModalLink">'
        out <<     '<i class="' + cssIcon + ' ' + cssColor + '" style="margin-left:0;"></i>'
        out <<   '</a>'
        out << '</span>'
    }
}
