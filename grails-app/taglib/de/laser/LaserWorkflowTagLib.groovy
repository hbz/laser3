package de.laser

import de.laser.helper.DateUtils
import de.laser.workflow.WorkflowHelper
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
                    String fstr = (cnd.getProperty( f + '_title') ?: message(code:'workflow.field.noTitle.label'))

                    if (f.startsWith('checkbox')) {
                        fstr = ( cnd.getProperty( f ) == true ? '<i class="ui icon check square outline"></i> ' : '<i class="ui icon square outline la-light-grey"></i> ' ) + fstr
                    }
                    else if (f.startsWith('date')) {
                        fstr = ( cnd.getProperty( f )  ?
                                '<i class="icon calendar alternate outline"></i> ' + fstr + ': <strong>' + DateUtils.getLocalizedSDF_noTime().format(cnd.getProperty(f)) + '</strong>' :
                                '<i class="icon calendar alternate outline la-light-grey"></i> ' + fstr  )
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
                            fstr = '<i class="icon file"></i> ' + fstr + ': <strong>' + docStr + '</strong>'
                        }
                        else {
                            fstr = '<i class="icon file la-light-grey"></i> ' + fstr
                        }
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
        out <<   '<a href="' + g.createLink( controller:'ajaxHtml', action:'useWfXModal', params:attrs.params ) + '" class="ui label large wfModalLink ' + cssColor + '">'
        out <<     '<i class="ui icon ' + cssIcon + '" style="margin-left:0;"></i>'
        out <<   '</a>'
        out << '</span>'
    }

    def workflowTaskConditionField = { attrs, body ->

        String field = attrs.field
        WfCondition condition = attrs.condition as WfCondition

        if (field && condition) {
            String pTitle = (condition.getProperty(field + '_title') ?: message(code:'workflow.field.noTitle.label'))

            if (field.startsWith('checkbox')) {
                if (condition.getProperty(field + '_isTrigger')) {
                    pTitle = '<u>' + pTitle + '</u>'
                }
                if (condition.getProperty(field) == true) {
                    out << '<i class="icon check square outline"></i> ' + pTitle
                }
                else {
                    out << '<i class="icon square outline la-light-grey"></i> ' + pTitle
                }
            }
            else if (field.startsWith('date')) {
                if (condition.getProperty(field)) {
                    out << '<i class="icon calendar alternate outline"></i> ' + pTitle
                    out << ': ' + DateUtils.getLocalizedSDF_noTime().format(condition.getProperty(field))
                }
                else {
                    out << '<i class="icon calendar alternate outline la-light-grey"></i> ' + pTitle
                }
            }
            else if (field.startsWith('file')) {
                DocContext docctx = condition.getProperty(field) as DocContext
                if (docctx) {
                    String linkBody = message(code:'template.documents.missing')
                    if (docctx.owner?.title) {
                        linkBody = docctx.owner.title
                    }
                    else if (docctx.owner?.filename) {
                        linkBody = docctx.owner.filename
                    }
                    out << '<i class="icon file"></i> ' + pTitle
                    out << ': ' + g.link( [controller: 'docstore', id: docctx.owner.uuid], linkBody + ' (' + docctx.owner?.type?.getI10n('value') + ')')
                }
                else {
                    out << '<i class="icon file la-light-grey"></i> ' + pTitle
                }
            }
        }
        else {
            out << '[laser:conditionField]'
        }
    }
}
