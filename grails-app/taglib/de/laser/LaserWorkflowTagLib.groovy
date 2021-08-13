package de.laser

import de.laser.helper.DateUtils
import de.laser.helper.WorkflowHelper
import de.laser.workflow.WfCondition
import de.laser.workflow.WfTask

class LaserWorkflowTagLib {

    static namespace = "laser"

    def workflowTask = { attrs, body ->

        WfTask task = attrs.task as WfTask

        String tooltip = '<p><strong>' + task.title + '</strong></p>' + ( task.description ? '<p>' + task.description + '</p>' : '')

        if (task.condition) {
            WfCondition cnd = task.condition
            tooltip = tooltip + '<div class="ui divider"></div>'

            if (cnd.type != 0) {
                List<String> fields = []
                cnd.getFields().each { f ->
                    String fstr = (cnd.getProperty( f + '_title') ?: 'Feld ohne Titel')

                    if (f.startsWith('checkbox')) {
                        fstr = fstr + ': ' + ( cnd.getProperty( f ) == true ? '<i class="ui icon check square outline"></i>' : '<i class="ui icon square outline"></i>' )
                    }
                    else if (f.startsWith('date')) {
                        fstr = fstr + ': ' + ( cnd.getProperty( f ) ?  '<strong>' + DateUtils.getSDF_NoTime().format(cnd.getProperty(f)) + '</strong>' : '' )
                    }
                    else if (f.startsWith('file')) {
                        DocContext docctx = cnd.getProperty( f ) as DocContext
                        String docStr = message(code:'template.documents.missing')

                        if (docctx) {
                            if (docctx.owner?.title) {
                                docStr = docctx.owner.title
                            }
                            else if (docctx.owner?.filename) {
                                docStr = docctx.owner.filename
                            }
                        }
                        fstr = fstr + ': ' + ( docctx ? '<strong>' + docStr + '</strong>' : '' )
                    }
                    fields.add( fstr )
                }
                tooltip = tooltip + '<p>' + fields.join('<br/>') + '</p>'
            }
            else {
                tooltip = tooltip + '<p><em>' + task.condition.getTypeAsRefdataValue().getI10n('value') + '</em></p>'
            }
        }

        String cssColor = WorkflowHelper.getCssColorByStatus(task.status)
        String cssIcon = WorkflowHelper.getCssIconByTaskPriority( task.priority )

        out << '<span class="la-popup-tooltip la-delay" data-position="top center" data-html="' + tooltip.encodeAsHTML() + '">'
        out <<   '<a href="' + g.createLink( controller:'ajaxHtml', action:'useWfXModal', params:attrs.params ) + '" class="ui circular compact button wfModalLink ' + cssColor + '">'
        out <<     '<i class="ui icon ' + cssIcon + '" style="margin-left:0;"></i>'
        out <<   '</a>'
        out << '</span>'
    }
}
